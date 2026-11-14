package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsEngine
import com.example.data.AppDatabase
import com.example.data.PlayerMetaState
import com.example.data.ProgressRepository
import com.example.game.engine.HudSnapshot
import com.example.game.engine.WorldMapGenerator
import com.example.game.engine.WorldSimulation
import com.example.game.model.CameraMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActiveMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProgressRepository(AppDatabase.getInstance(application).gameDao())
    val soundEngine = SoundEffectsEngine()

    private var _simulation: WorldSimulation? = null
    val simulation: WorldSimulation?
        get() = _simulation

    private val _hudState = MutableStateFlow(HudSnapshot())
    val hudState: StateFlow<HudSnapshot> = _hudState.asStateFlow()

    private var rewardsSavedForCurrentMatch = false

    fun startNewMatch(meta: PlayerMetaState) {
        soundEngine.enabled = meta.soundEnabled
        soundEngine.sfxVolume = meta.sfxVolume
        val generatedWorld = WorldMapGenerator.generate(meta.selectedWorld)
        val sim = WorldSimulation(generatedWorld, meta, soundEngine)
        _simulation = sim
        rewardsSavedForCurrentMatch = false
        _hudState.value = sim.buildHudSnapshot()
    }

    fun syncLiveSettings(meta: PlayerMetaState) {
        val sim = _simulation ?: return
        sim.applyLiveSettings(meta)
        _hudState.value = sim.buildHudSnapshot()
    }

    fun refreshHudFromRenderer() {
        val sim = _simulation ?: return
        val snapshot = sim.buildHudSnapshot()
        _hudState.value = snapshot
        if (snapshot.isGameOver && !rewardsSavedForCurrentMatch) {
            saveMatchRewards(sim)
        }
    }

    fun toggleCameraMode() {
        val sim = _simulation ?: return
        val next = if (sim.cameraMode == CameraMode.MODE_3D) CameraMode.MODE_2D else CameraMode.MODE_3D
        sim.cameraMode = next
        _hudState.value = sim.buildHudSnapshot()
        viewModelScope.launch {
            repository.setPreferredCameraMode(next)
        }
    }

    fun setPaused(paused: Boolean) {
        val sim = _simulation ?: return
        if (paused) sim.resetInputs()
        sim.isPaused = paused
        _hudState.value = sim.buildHudSnapshot()
    }

    fun toggleVehicle() {
        val sim = _simulation ?: return
        sim.toggleVehicle()
        _hudState.value = sim.buildHudSnapshot()
    }

    fun triggerReload() {
        val sim = _simulation ?: return
        sim.triggerReload()
        _hudState.value = sim.buildHudSnapshot()
    }

    fun switchWeaponSlot() {
        val sim = _simulation ?: return
        sim.switchWeaponSlot()
        _hudState.value = sim.buildHudSnapshot()
    }

    fun activateSpecialAbility() {
        val sim = _simulation ?: return
        sim.activateSpecialAbility()
        _hudState.value = sim.buildHudSnapshot()
    }

    fun commitRewardsBeforeExit() {
        val sim = _simulation ?: return
        if (!rewardsSavedForCurrentMatch) {
            saveMatchRewards(sim)
        }
    }

    private fun saveMatchRewards(sim: WorldSimulation) {
        rewardsSavedForCurrentMatch = true
        val coins = sim.sessionCoins
        val kills = sim.sessionKills
        val wave = sim.wave
        val worldId = sim.world.worldId
        viewModelScope.launch {
            repository.recordMatchResult(
                world = worldId,
                earnedCoins = coins,
                kills = kills,
                waveReached = wave
            )
        }
    }
}
