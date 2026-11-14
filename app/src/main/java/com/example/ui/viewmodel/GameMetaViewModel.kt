package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MusicTrackMode
import com.example.audio.SynthMusicEngine
import com.example.data.AppDatabase
import com.example.data.PlayerMetaState
import com.example.data.ProgressRepository
import com.example.game.model.AppLanguage
import com.example.game.model.CameraMode
import com.example.game.model.CharacterOutfitId
import com.example.game.model.MissionId
import com.example.game.model.SpecialAbilityId
import com.example.game.model.TimeOfDayMode
import com.example.game.model.UpgradeType
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponId
import com.example.game.model.WeaponSkinId
import com.example.game.model.WorldId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameMetaViewModel(application: Application) : AndroidViewModel(application) {
    val repository: ProgressRepository = ProgressRepository(
        AppDatabase.getInstance(application).gameDao()
    )

    val musicEngine = SynthMusicEngine()

    val metaState: StateFlow<PlayerMetaState> = repository.metaState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PlayerMetaState()
    )

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            metaState.collect { state ->
                musicEngine.soundEnabled = state.soundEnabled
                musicEngine.musicVolume = state.musicVolume
            }
        }
        musicEngine.start()
    }

    override fun onCleared() {
        super.onCleared()
        musicEngine.stop()
    }

    fun setMusicMode(mode: MusicTrackMode) {
        musicEngine.trackMode = mode
    }

    fun onAppPause() {
        musicEngine.pause()
    }

    fun onAppResume() {
        musicEngine.resume()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun isDe(): Boolean = metaState.value.language == AppLanguage.DE

    fun onWeaponAction(weapon: WeaponId, asSecondary: Boolean = false) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val unlocked = weapon in metaState.value.unlockedWeapons
            val ok = repository.buyOrEquipWeapon(weapon, unlocked, asSecondary)
            _toastMessage.value = when {
                !ok -> if (isDe()) "Nicht genug Coins für ${weapon.title(lang)}" else "Not enough coins for ${weapon.title(lang)}"
                unlocked -> if (asSecondary) {
                    if (isDe()) "${weapon.title(lang)} als Zweitwaffe ausgerüstet" else "${weapon.title(lang)} equipped as Secondary"
                } else {
                    if (isDe()) "${weapon.title(lang)} als Primärwaffe ausgerüstet" else "${weapon.title(lang)} equipped as Primary"
                }
                else -> if (isDe()) "${weapon.title(lang)} freigeschaltet & ausgerüstet!" else "${weapon.title(lang)} unlocked & equipped!"
            }
        }
    }

    fun onAbilityAction(ability: SpecialAbilityId) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val unlocked = ability in metaState.value.unlockedAbilities
            val ok = repository.buyOrEquipAbility(ability, unlocked)
            _toastMessage.value = when {
                !ok -> if (isDe()) "Nicht genug Coins für ${ability.title(lang)}" else "Not enough coins for ${ability.title(lang)}"
                unlocked -> if (isDe()) "${ability.title(lang)} aktiviert" else "${ability.title(lang)} equipped"
                else -> if (isDe()) "${ability.title(lang)} freigeschaltet!" else "${ability.title(lang)} unlocked!"
            }
        }
    }

    fun onSkinAction(skin: WeaponSkinId) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val unlocked = skin in metaState.value.unlockedSkins
            val ok = repository.buyOrEquipSkin(skin, unlocked)
            _toastMessage.value = when {
                !ok -> if (isDe()) "Nicht genug Coins für ${skin.title(lang)}" else "Not enough coins for ${skin.title(lang)}"
                unlocked -> if (isDe()) "Waffen-Skin \"${skin.title(lang)}\" ausgerüstet" else "Skin \"${skin.title(lang)}\" equipped"
                else -> if (isDe()) "Waffen-Skin \"${skin.title(lang)}\" gekauft!" else "Skin \"${skin.title(lang)}\" unlocked!"
            }
        }
    }

    fun onOutfitAction(outfit: CharacterOutfitId) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val unlocked = outfit in metaState.value.unlockedOutfits
            val ok = repository.buyOrEquipOutfit(outfit, unlocked)
            _toastMessage.value = when {
                !ok -> if (isDe()) "Nicht genug Coins für ${outfit.title(lang)}" else "Not enough coins for ${outfit.title(lang)}"
                unlocked -> if (isDe()) "Outfit \"${outfit.title(lang)}\" angelegt" else "Outfit \"${outfit.title(lang)}\" equipped"
                else -> if (isDe()) "Outfit \"${outfit.title(lang)}\" freigeschaltet!" else "Outfit \"${outfit.title(lang)}\" unlocked!"
            }
        }
    }

    fun onCarPaintAction(paint: VehiclePaintId) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val unlocked = paint in metaState.value.unlockedCarPaints
            val ok = repository.buyOrEquipCarPaint(paint, unlocked)
            _toastMessage.value = when {
                !ok -> if (isDe()) "Nicht genug Coins für ${paint.title(lang)}" else "Not enough coins for ${paint.title(lang)}"
                unlocked -> if (isDe()) "Auto-Lack \"${paint.title(lang)}\" aktiv" else "Car paint \"${paint.title(lang)}\" equipped"
                else -> if (isDe()) "Auto-Lack \"${paint.title(lang)}\" gekauft!" else "Car paint \"${paint.title(lang)}\" unlocked!"
            }
        }
    }

    fun onUpgradeAction(type: UpgradeType) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val ok = repository.buyUpgrade(type)
            _toastMessage.value = if (ok) {
                if (isDe()) "${type.title(lang)} verbessert!" else "${type.title(lang)} upgraded!"
            } else {
                if (isDe()) "Nicht genug Coins oder Max-Stufe erreicht" else "Not enough coins or Max level reached"
            }
        }
    }

    fun onClaimMission(mission: MissionId) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val ok = repository.claimMissionReward(mission)
            if (ok) {
                _toastMessage.value = if (isDe()) {
                    "+${mission.rewardCoins} Coins für \"${mission.title(lang)}\" erhalten!"
                } else {
                    "+${mission.rewardCoins} Coins claimed for \"${mission.title(lang)}\"!"
                }
            }
        }
    }

    fun onWorldAction(world: WorldId, onReadyToPlay: ((PlayerMetaState) -> Unit)? = null) {
        viewModelScope.launch {
            val lang = metaState.value.language
            val record = metaState.value.worldRecords[world]
            val isUnlocked = record?.unlocked ?: (world.unlockCost == 0)
            val ok = repository.unlockOrSelectWorld(world)
            if (ok) {
                if (!isUnlocked) {
                    _toastMessage.value = if (isDe()) {
                        "Welt \"${world.title(lang)}\" freigeschaltet!"
                    } else {
                        "World \"${world.title(lang)}\" unlocked!"
                    }
                }
                val latest = repository.getLatestMetaState()
                onReadyToPlay?.invoke(latest)
            } else {
                _toastMessage.value = if (isDe()) {
                    "Benötigt ${world.unlockCost} Schrott-Coins"
                } else {
                    "Requires ${world.unlockCost} Scrap Coins"
                }
            }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            repository.setSoundEnabled(!metaState.value.soundEnabled)
        }
    }

    fun setPreferredCameraMode(mode: CameraMode) {
        viewModelScope.launch {
            repository.setPreferredCameraMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(languageCode = language.code) }
        }
    }

    fun setMusicVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        musicEngine.musicVolume = v
        viewModelScope.launch {
            repository.updateSettings { it.copy(musicVolume = v, soundEnabled = v > 0.01f || it.sfxVolume > 0.01f) }
        }
    }

    fun setSfxVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        viewModelScope.launch {
            repository.updateSettings { it.copy(sfxVolume = v, soundEnabled = v > 0.01f || it.musicVolume > 0.01f) }
        }
    }

    fun setGraphicsQuality(level: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(graphicsQuality = level.coerceIn(0, 2)) }
        }
    }

    fun setTimeOfDayMode(mode: TimeOfDayMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(timeOfDayMode = mode.name) }
        }
    }

    fun setWeatherEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(weatherEnabled = enabled) }
        }
    }

    fun setShowFps(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showFps = enabled) }
        }
    }

    fun setAutoFireAssist(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(autoFireAssist = enabled) }
        }
    }

    fun setJoystickScale(scale: Float) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(joystickScale = scale.coerceIn(0.85f, 1.25f)) }
        }
    }

    fun setAimSensitivity(sens: Float) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(aimSensitivity = sens.coerceIn(0.6f, 1.5f)) }
        }
    }
}
