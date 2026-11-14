package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class MusicTrackMode {
    MENU,
    COMBAT,
    BOSS
}

class SynthMusicEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var musicJob: Job? = null
    private val sampleRate = 22050

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var musicVolume: Float = 0.65f

    @Volatile
    var trackMode: MusicTrackMode = MusicTrackMode.MENU

    @Volatile
    private var isForeground: Boolean = true

    fun start() {
        isForeground = true
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            runSynthLoop()
        }
    }

    fun pause() {
        isForeground = false
    }

    fun resume() {
        isForeground = true
        if (musicJob?.isActive != true) {
            start()
        }
    }

    fun stop() {
        isForeground = false
        musicJob?.cancel()
        musicJob = null
    }

    private fun midiToHz(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    private suspend fun runSynthLoop() {
        val chunkSamples = sampleRate / 10 // 100ms chunks for responsive transitions
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(chunkSamples * 4)

        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: return

        val buffer = ShortArray(chunkSamples)
        var bassPhase = 0.0
        var arpPhase = 0.0
        var padPhase = 0.0
        var kickPhase = 0.0
        var globalSampleIdx = 0L

        // Dark Synthwave progressions (D minor / E Phrygian dark cyberpunk scales)
        val menuBassProgression = intArrayOf(38, 38, 34, 34, 31, 31, 33, 36)
        val menuArpNotes = intArrayOf(62, 65, 69, 74, 69, 65, 62, 57)

        val combatBassProgression = intArrayOf(38, 38, 41, 38, 34, 34, 36, 33)
        val combatArpNotes = intArrayOf(62, 65, 69, 72, 74, 72, 69, 65)

        val bossBassProgression = intArrayOf(36, 36, 37, 36, 34, 34, 33, 31)
        val bossArpNotes = intArrayOf(60, 63, 67, 73, 72, 67, 63, 61)

        try {
            track.play()
            while (scope.isActive) {
                val vol = if (soundEnabled && isForeground) musicVolume.coerceIn(0f, 1f) else 0f
                if (vol <= 0.01f) {
                    buffer.fill(0)
                    track.write(buffer, 0, buffer.size)
                    kotlinx.coroutines.delay(85L)
                    continue
                }

                val mode = trackMode
                val bpm = when (mode) {
                    MusicTrackMode.MENU -> 106.0
                    MusicTrackMode.COMBAT -> 128.0
                    MusicTrackMode.BOSS -> 144.0
                }
                val sixteenthSec = (60.0 / bpm) / 4.0
                val samplesPerStep = (sampleRate * sixteenthSec).toInt().coerceAtLeast(1)

                val bassScale = when (mode) {
                    MusicTrackMode.MENU -> menuBassProgression
                    MusicTrackMode.COMBAT -> combatBassProgression
                    MusicTrackMode.BOSS -> bossBassProgression
                }
                val arpScale = when (mode) {
                    MusicTrackMode.MENU -> menuArpNotes
                    MusicTrackMode.COMBAT -> combatArpNotes
                    MusicTrackMode.BOSS -> bossArpNotes
                }

                for (i in 0 until chunkSamples) {
                    val step = (globalSampleIdx / samplesPerStep).toInt()
                    val posInStep = (globalSampleIdx % samplesPerStep).toDouble() / samplesPerStep
                    val barIdx = (step / 16) % bassScale.size
                    val stepInBar = step % 16

                    // 1. Driving Synthwave Saw-Sine Bass
                    val bassMidi = bassScale[barIdx] + if (step % 2 == 1) 12 else 0
                    val bassFreq = midiToHz(bassMidi)
                    bassPhase += 2.0 * PI * bassFreq / sampleRate
                    val bassEnv = exp(-posInStep * 3.2)
                    val bassWave = (sin(bassPhase) + 0.45 * sin(bassPhase * 2.0) + 0.2 * sin(bassPhase * 3.0)) * bassEnv

                    // 2. Arpeggiator Lead
                    val arpMidi = arpScale[(step + barIdx * 2) % arpScale.size]
                    val arpFreq = midiToHz(arpMidi)
                    arpPhase += 2.0 * PI * arpFreq / sampleRate
                    val arpEnv = exp(-posInStep * 4.5)
                    val arpWave = (sin(arpPhase) + 0.3 * sin(arpPhase * 2.0)) * arpEnv

                    // 3. Warm Dark Pad
                    val padFreq = midiToHz(bassScale[barIdx] + 24)
                    padPhase += 2.0 * PI * padFreq / sampleRate
                    val padWave = sin(padPhase) * 0.25

                    // 4. Electronic Kick & Snare (stronger in Combat & Boss)
                    var drumSignal = 0.0
                    if (stepInBar % 4 == 0) {
                        val kickFreq = 135.0 * exp(-posInStep * 8.5) + 42.0
                        kickPhase += 2.0 * PI * kickFreq / sampleRate
                        drumSignal += sin(kickPhase) * exp(-posInStep * 5.0) * 0.75
                    } else if (mode != MusicTrackMode.MENU && (stepInBar == 4 || stepInBar == 12)) {
                        val snareEnv = exp(-posInStep * 9.0)
                        drumSignal += Random.nextDouble(-0.7, 0.7) * snareEnv * 0.45
                    } else if (mode == MusicTrackMode.BOSS && stepInBar % 2 == 1) {
                        val hatEnv = exp(-posInStep * 16.0)
                        drumSignal += Random.nextDouble(-0.4, 0.4) * hatEnv * 0.22
                    }

                    val mixed = (bassWave * 0.36 + arpWave * 0.24 + padWave * 0.18 + drumSignal * 0.34) * vol * 0.48
                    buffer[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
                    globalSampleIdx++
                }

                track.write(buffer, 0, buffer.size)
            }
        } catch (_: Exception) {
            // Ignore audio track interruptions
        } finally {
            runCatching {
                track.stop()
                track.release()
            }
        }
    }
}
