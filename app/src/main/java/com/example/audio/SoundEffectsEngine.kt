package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEffectsEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    @Volatile
    var enabled: Boolean = true

    @Volatile
    var sfxVolume: Float = 0.80f

    private val shootPistolPcm by lazy { generateBurst(durationMs = 85, startFreq = 520.0, endFreq = 140.0, noiseMix = 0.55f) }
    private val shootShotgunPcm by lazy { generateBurst(durationMs = 150, startFreq = 260.0, endFreq = 70.0, noiseMix = 0.78f) }
    private val shootPlasmaPcm by lazy { generateBurst(durationMs = 130, startFreq = 1150.0, endFreq = 310.0, noiseMix = 0.15f) }
    private val hitPcm by lazy { generateBurst(durationMs = 55, startFreq = 210.0, endFreq = 95.0, noiseMix = 0.45f) }
    private val coinPcm by lazy { generateChime(durationMs = 140, freq1 = 880.0, freq2 = 1320.0) }
    private val bossRoarPcm by lazy { generateBurst(durationMs = 340, startFreq = 130.0, endFreq = 55.0, noiseMix = 0.50f) }
    private val carStartPcm by lazy { generateBurst(durationMs = 190, startFreq = 95.0, endFreq = 195.0, noiseMix = 0.30f) }
    private val abilityPcm by lazy { generateBurst(durationMs = 240, startFreq = 280.0, endFreq = 980.0, noiseMix = 0.20f) }
    private val weaponSwapPcm by lazy { generateChime(durationMs = 90, freq1 = 520.0, freq2 = 760.0) }

    fun playShoot(isShotgun: Boolean = false, isPlasma: Boolean = false) {
        if (!enabled || sfxVolume <= 0.01f) return
        val pcm = when {
            isPlasma -> shootPlasmaPcm
            isShotgun -> shootShotgunPcm
            else -> shootPistolPcm
        }
        playBuffer(pcm, 0.36f * sfxVolume)
    }

    fun playHit() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(hitPcm, 0.28f * sfxVolume)
    }

    fun playCoin() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(coinPcm, 0.34f * sfxVolume)
    }

    fun playBossAlert() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(bossRoarPcm, 0.45f * sfxVolume)
    }

    fun playCarIgnition() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(carStartPcm, 0.38f * sfxVolume)
    }

    fun playAbilityActivate() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(abilityPcm, 0.40f * sfxVolume)
    }

    fun playWeaponSwap() {
        if (!enabled || sfxVolume <= 0.01f) return
        playBuffer(weaponSwapPcm, 0.30f * sfxVolume)
    }

    private fun playBuffer(samples: ShortArray, volume: Float) {
        val clampedVol = volume.coerceIn(0f, 1f)
        scope.launch {
            runCatching {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.setVolume(clampedVol)
                track.play()
                kotlinx.coroutines.delay((samples.size * 1000L) / sampleRate + 35L)
                track.release()
            }
        }
    }

    private fun generateBurst(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        noiseMix: Float
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-t * 5.0)
            val tone = sin(phase)
            val noise = Random.nextDouble(-1.0, 1.0)
            val sample = (tone * (1.0 - noiseMix) + noise * noiseMix) * env
            out[i] = (sample * Short.MAX_VALUE * 0.75).toInt().coerceIn(-32767, 32767).toShort()
        }
        return out
    }

    private fun generateChime(durationMs: Int, freq1: Double, freq2: Double): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val freq = if (t < 0.45) freq1 else freq2
            val env = exp(-t * 4.2)
            val sample = sin(2.0 * PI * freq * i / sampleRate) * env
            out[i] = (sample * Short.MAX_VALUE * 0.7).toInt().coerceIn(-32767, 32767).toShort()
        }
        return out
    }
}
