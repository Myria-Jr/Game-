package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.game.model.AppLanguage
import com.example.game.model.CameraMode
import com.example.game.model.CharacterOutfitId
import com.example.game.model.SpecialAbilityId
import com.example.game.model.TimeOfDayMode
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponId
import com.example.game.model.WeaponSkinId
import com.example.game.model.WorldId

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 220,
    val equippedWeaponId: String = WeaponId.PISTOL.name,
    val secondaryWeaponId: String = WeaponId.SHOTGUN.name,
    val equippedSkinId: String = WeaponSkinId.STANDARD_STEEL.name,
    val equippedOutfitId: String = CharacterOutfitId.WASTELAND_RANGER.name,
    val equippedCarPaintId: String = VehiclePaintId.BIOME_DEFAULT.name,
    val equippedAbilityId: String = SpecialAbilityId.EMP_SHOCKWAVE.name,
    val selectedWorldId: String = WorldId.CITY.name,
    val preferredCameraMode: String = CameraMode.MODE_3D.name,
    val soundEnabled: Boolean = true,
    val musicVolume: Float = 0.65f,
    val sfxVolume: Float = 0.80f,
    val languageCode: String = AppLanguage.DE.code,
    val graphicsQuality: Int = 2,
    val showFps: Boolean = true,
    val weatherEnabled: Boolean = true,
    val timeOfDayMode: String = TimeOfDayMode.DYNAMIC.name,
    val joystickScale: Float = 1.0f,
    val aimSensitivity: Float = 1.0f,
    val autoFireAssist: Boolean = true,
    val totalKills: Int = 0,
    val totalGames: Int = 0,
    val healthUpgradeLvl: Int = 0,
    val damageUpgradeLvl: Int = 0,
    val carArmorUpgradeLvl: Int = 0,
    val speedUpgradeLvl: Int = 0
)

@Entity(tableName = "unlocked_weapons")
data class UnlockedWeaponEntity(
    @PrimaryKey val weaponId: String
)

@Entity(tableName = "unlocked_skins")
data class UnlockedSkinEntity(
    @PrimaryKey val skinId: String
)

@Entity(tableName = "unlocked_outfits")
data class UnlockedOutfitEntity(
    @PrimaryKey val outfitId: String
)

@Entity(tableName = "unlocked_car_paints")
data class UnlockedCarPaintEntity(
    @PrimaryKey val paintId: String
)

@Entity(tableName = "unlocked_abilities")
data class UnlockedAbilityEntity(
    @PrimaryKey val abilityId: String
)

@Entity(tableName = "claimed_missions")
data class ClaimedMissionEntity(
    @PrimaryKey val missionId: String
)

@Entity(tableName = "world_progress")
data class WorldProgressEntity(
    @PrimaryKey val worldId: String,
    val unlocked: Boolean,
    val bestWave: Int = 0,
    val bestKills: Int = 0
)
