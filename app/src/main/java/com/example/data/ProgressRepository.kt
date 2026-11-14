package com.example.data

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class WorldRecord(
    val worldId: WorldId,
    val unlocked: Boolean,
    val bestWave: Int,
    val bestKills: Int
)

data class PlayerMetaState(
    val coins: Int = 220,
    val equippedWeapon: WeaponId = WeaponId.PISTOL,
    val secondaryWeapon: WeaponId = WeaponId.SHOTGUN,
    val equippedSkin: WeaponSkinId = WeaponSkinId.STANDARD_STEEL,
    val equippedOutfit: CharacterOutfitId = CharacterOutfitId.WASTELAND_RANGER,
    val equippedCarPaint: VehiclePaintId = VehiclePaintId.BIOME_DEFAULT,
    val equippedAbility: SpecialAbilityId = SpecialAbilityId.EMP_SHOCKWAVE,
    val selectedWorld: WorldId = WorldId.CITY,
    val preferredCameraMode: CameraMode = CameraMode.MODE_3D,
    val soundEnabled: Boolean = true,
    val musicVolume: Float = 0.65f,
    val sfxVolume: Float = 0.80f,
    val language: AppLanguage = AppLanguage.DE,
    val graphicsQuality: Int = 2,
    val showFps: Boolean = true,
    val weatherEnabled: Boolean = true,
    val timeOfDayMode: TimeOfDayMode = TimeOfDayMode.DYNAMIC,
    val joystickScale: Float = 1.0f,
    val aimSensitivity: Float = 1.0f,
    val autoFireAssist: Boolean = true,
    val totalKills: Int = 0,
    val totalGames: Int = 0,
    val unlockedWeapons: Set<WeaponId> = setOf(WeaponId.PISTOL),
    val unlockedSkins: Set<WeaponSkinId> = setOf(WeaponSkinId.STANDARD_STEEL),
    val unlockedOutfits: Set<CharacterOutfitId> = setOf(CharacterOutfitId.WASTELAND_RANGER),
    val unlockedCarPaints: Set<VehiclePaintId> = setOf(VehiclePaintId.BIOME_DEFAULT),
    val unlockedAbilities: Set<SpecialAbilityId> = setOf(SpecialAbilityId.EMP_SHOCKWAVE),
    val claimedMissions: Set<MissionId> = emptySet(),
    val worldRecords: Map<WorldId, WorldRecord> = emptyMap(),
    val upgradeLevels: Map<UpgradeType, Int> = emptyMap()
) {
    fun upgradeLevel(type: UpgradeType): Int = upgradeLevels[type] ?: 0

    fun missionProgress(mission: MissionId): Int = when (mission) {
        MissionId.FIRST_BLOOD, MissionId.HUNDRED_HUNTER -> totalKills
        MissionId.WAVE_SURVIVOR_3, MissionId.WAVE_SURVIVOR_6 ->
            worldRecords.values.maxOfOrNull { it.bestWave } ?: 0
        MissionId.ARSENAL_COLLECTOR -> unlockedWeapons.size
        MissionId.STYLE_ICON -> unlockedSkins.size + unlockedOutfits.size + unlockedCarPaints.size
        MissionId.WORLD_EXPLORER -> worldRecords.values.count { it.unlocked }
        MissionId.VETERAN_DRIVER -> totalGames
    }

    fun isMissionClaimable(mission: MissionId): Boolean =
        mission !in claimedMissions && missionProgress(mission) >= mission.targetValue

    fun claimableMissionCount(): Int =
        MissionId.entries.count { isMissionClaimable(it) }
}

private data class CosmeticUnlocks(
    val skins: List<String>,
    val outfits: List<String>,
    val paints: List<String>,
    val abilities: List<String>,
    val claimedMissions: List<String>
)

class ProgressRepository(private val dao: GameDao) {

    private val cosmeticsFlow: Flow<CosmeticUnlocks> = combine(
        dao.observeUnlockedSkins(),
        dao.observeUnlockedOutfits(),
        dao.observeUnlockedCarPaints(),
        dao.observeUnlockedAbilities(),
        dao.observeClaimedMissions()
    ) { skins, outfits, paints, abilities, missions ->
        CosmeticUnlocks(skins, outfits, paints, abilities, missions)
    }

    val metaState: Flow<PlayerMetaState> = combine(
        dao.observeProfile(),
        dao.observeUnlockedWeapons(),
        cosmeticsFlow,
        dao.observeWorldProgress()
    ) { profile, weaponIds, cosmetics, worlds ->
        val p = profile ?: PlayerProfileEntity()
        val weapons = weaponIds.mapNotNull { runCatching { WeaponId.valueOf(it) }.getOrNull() }
            .toSet() + WeaponId.PISTOL
        val skins = cosmetics.skins.mapNotNull { runCatching { WeaponSkinId.valueOf(it) }.getOrNull() }
            .toSet() + WeaponSkinId.STANDARD_STEEL
        val outfits = cosmetics.outfits.mapNotNull { runCatching { CharacterOutfitId.valueOf(it) }.getOrNull() }
            .toSet() + CharacterOutfitId.WASTELAND_RANGER
        val paints = cosmetics.paints.mapNotNull { runCatching { VehiclePaintId.valueOf(it) }.getOrNull() }
            .toSet() + VehiclePaintId.BIOME_DEFAULT
        val abilities = cosmetics.abilities.mapNotNull { runCatching { SpecialAbilityId.valueOf(it) }.getOrNull() }
            .toSet() + SpecialAbilityId.EMP_SHOCKWAVE
        val claimed = cosmetics.claimedMissions.mapNotNull { runCatching { MissionId.valueOf(it) }.getOrNull() }
            .toSet()

        val recordMap = WorldId.entries.associateWith { w ->
            val found = worlds.firstOrNull { it.worldId == w.name }
            WorldRecord(
                worldId = w,
                unlocked = found?.unlocked ?: (w.unlockCost == 0),
                bestWave = found?.bestWave ?: 0,
                bestKills = found?.bestKills ?: 0
            )
        }

        val primaryGun = runCatching { WeaponId.valueOf(p.equippedWeaponId) }.getOrDefault(WeaponId.PISTOL)
        val rawSecondary = runCatching { WeaponId.valueOf(p.secondaryWeaponId) }.getOrDefault(WeaponId.PISTOL)
        val secondaryGun = if (rawSecondary in weapons && rawSecondary != primaryGun) {
            rawSecondary
        } else {
            weapons.firstOrNull { it != primaryGun } ?: primaryGun
        }

        PlayerMetaState(
            coins = p.coins,
            equippedWeapon = primaryGun,
            secondaryWeapon = secondaryGun,
            equippedSkin = runCatching { WeaponSkinId.valueOf(p.equippedSkinId) }
                .getOrDefault(WeaponSkinId.STANDARD_STEEL),
            equippedOutfit = runCatching { CharacterOutfitId.valueOf(p.equippedOutfitId) }
                .getOrDefault(CharacterOutfitId.WASTELAND_RANGER),
            equippedCarPaint = runCatching { VehiclePaintId.valueOf(p.equippedCarPaintId) }
                .getOrDefault(VehiclePaintId.BIOME_DEFAULT),
            equippedAbility = runCatching { SpecialAbilityId.valueOf(p.equippedAbilityId) }
                .getOrDefault(SpecialAbilityId.EMP_SHOCKWAVE),
            selectedWorld = runCatching { WorldId.valueOf(p.selectedWorldId) }
                .getOrDefault(WorldId.CITY),
            preferredCameraMode = runCatching { CameraMode.valueOf(p.preferredCameraMode) }
                .getOrDefault(CameraMode.MODE_3D),
            soundEnabled = p.soundEnabled,
            musicVolume = p.musicVolume.coerceIn(0f, 1f),
            sfxVolume = p.sfxVolume.coerceIn(0f, 1f),
            language = AppLanguage.fromCode(p.languageCode),
            graphicsQuality = p.graphicsQuality.coerceIn(0, 2),
            showFps = p.showFps,
            weatherEnabled = p.weatherEnabled,
            timeOfDayMode = runCatching { TimeOfDayMode.valueOf(p.timeOfDayMode) }
                .getOrDefault(TimeOfDayMode.DYNAMIC),
            joystickScale = p.joystickScale.coerceIn(0.8f, 1.3f),
            aimSensitivity = p.aimSensitivity.coerceIn(0.5f, 1.6f),
            autoFireAssist = p.autoFireAssist,
            totalKills = p.totalKills,
            totalGames = p.totalGames,
            unlockedWeapons = weapons,
            unlockedSkins = skins,
            unlockedOutfits = outfits,
            unlockedCarPaints = paints,
            unlockedAbilities = abilities,
            claimedMissions = claimed,
            worldRecords = recordMap,
            upgradeLevels = mapOf(
                UpgradeType.MAX_HEALTH to p.healthUpgradeLvl,
                UpgradeType.DAMAGE_BOOST to p.damageUpgradeLvl,
                UpgradeType.CAR_ARMOR to p.carArmorUpgradeLvl,
                UpgradeType.MOVE_SPEED to p.speedUpgradeLvl
            )
        )
    }

    suspend fun getLatestMetaState(): PlayerMetaState = metaState.first()

    suspend fun ensureInitialized() {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(PlayerProfileEntity())
        }
        dao.insertUnlockedWeapon(UnlockedWeaponEntity(WeaponId.PISTOL.name))
        dao.insertUnlockedSkin(UnlockedSkinEntity(WeaponSkinId.STANDARD_STEEL.name))
        dao.insertUnlockedOutfit(UnlockedOutfitEntity(CharacterOutfitId.WASTELAND_RANGER.name))
        dao.insertUnlockedCarPaint(UnlockedCarPaintEntity(VehiclePaintId.BIOME_DEFAULT.name))
        dao.insertUnlockedAbility(UnlockedAbilityEntity(SpecialAbilityId.EMP_SHOCKWAVE.name))
        for (world in WorldId.entries) {
            if (dao.getWorldProgressOnce(world.name) == null) {
                dao.upsertWorldProgress(
                    WorldProgressEntity(
                        worldId = world.name,
                        unlocked = world.unlockCost == 0,
                        bestWave = 0,
                        bestKills = 0
                    )
                )
            }
        }
    }

    suspend fun buyOrEquipWeapon(
        weapon: WeaponId,
        currentlyUnlocked: Boolean,
        asSecondary: Boolean = false
    ): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (currentlyUnlocked) {
            val updated = if (asSecondary) {
                profile.copy(secondaryWeaponId = weapon.name)
            } else {
                profile.copy(equippedWeaponId = weapon.name)
            }
            dao.upsertProfile(updated)
            return true
        }
        if (profile.coins < weapon.unlockCost) return false
        dao.insertUnlockedWeapon(UnlockedWeaponEntity(weapon.name))
        val updated = if (asSecondary) {
            profile.copy(
                coins = profile.coins - weapon.unlockCost,
                secondaryWeaponId = weapon.name
            )
        } else {
            profile.copy(
                coins = profile.coins - weapon.unlockCost,
                equippedWeaponId = weapon.name
            )
        }
        dao.upsertProfile(updated)
        return true
    }

    suspend fun buyOrEquipSkin(skin: WeaponSkinId, currentlyUnlocked: Boolean): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (currentlyUnlocked) {
            dao.upsertProfile(profile.copy(equippedSkinId = skin.name))
            return true
        }
        if (profile.coins < skin.unlockCost) return false
        dao.insertUnlockedSkin(UnlockedSkinEntity(skin.name))
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - skin.unlockCost,
                equippedSkinId = skin.name
            )
        )
        return true
    }

    suspend fun buyOrEquipOutfit(outfit: CharacterOutfitId, currentlyUnlocked: Boolean): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (currentlyUnlocked) {
            dao.upsertProfile(profile.copy(equippedOutfitId = outfit.name))
            return true
        }
        if (profile.coins < outfit.unlockCost) return false
        dao.insertUnlockedOutfit(UnlockedOutfitEntity(outfit.name))
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - outfit.unlockCost,
                equippedOutfitId = outfit.name
            )
        )
        return true
    }

    suspend fun buyOrEquipCarPaint(paint: VehiclePaintId, currentlyUnlocked: Boolean): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (currentlyUnlocked) {
            dao.upsertProfile(profile.copy(equippedCarPaintId = paint.name))
            return true
        }
        if (profile.coins < paint.unlockCost) return false
        dao.insertUnlockedCarPaint(UnlockedCarPaintEntity(paint.name))
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - paint.unlockCost,
                equippedCarPaintId = paint.name
            )
        )
        return true
    }

    suspend fun buyOrEquipAbility(ability: SpecialAbilityId, currentlyUnlocked: Boolean): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (currentlyUnlocked) {
            dao.upsertProfile(profile.copy(equippedAbilityId = ability.name))
            return true
        }
        if (profile.coins < ability.unlockCost) return false
        dao.insertUnlockedAbility(UnlockedAbilityEntity(ability.name))
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - ability.unlockCost,
                equippedAbilityId = ability.name
            )
        )
        return true
    }

    suspend fun claimMissionReward(mission: MissionId): Boolean {
        val current = getLatestMetaState()
        if (!current.isMissionClaimable(mission)) return false
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.insertClaimedMission(ClaimedMissionEntity(mission.name))
        dao.upsertProfile(profile.copy(coins = profile.coins + mission.rewardCoins))
        return true
    }

    suspend fun buyUpgrade(type: UpgradeType): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val currentLvl = when (type) {
            UpgradeType.MAX_HEALTH -> profile.healthUpgradeLvl
            UpgradeType.DAMAGE_BOOST -> profile.damageUpgradeLvl
            UpgradeType.CAR_ARMOR -> profile.carArmorUpgradeLvl
            UpgradeType.MOVE_SPEED -> profile.speedUpgradeLvl
        }
        if (currentLvl >= type.maxLevel) return false
        val cost = type.costForNextLevel(currentLvl)
        if (profile.coins < cost) return false
        val updated = when (type) {
            UpgradeType.MAX_HEALTH -> profile.copy(
                coins = profile.coins - cost,
                healthUpgradeLvl = currentLvl + 1
            )
            UpgradeType.DAMAGE_BOOST -> profile.copy(
                coins = profile.coins - cost,
                damageUpgradeLvl = currentLvl + 1
            )
            UpgradeType.CAR_ARMOR -> profile.copy(
                coins = profile.coins - cost,
                carArmorUpgradeLvl = currentLvl + 1
            )
            UpgradeType.MOVE_SPEED -> profile.copy(
                coins = profile.coins - cost,
                speedUpgradeLvl = currentLvl + 1
            )
        }
        dao.upsertProfile(updated)
        return true
    }

    suspend fun unlockOrSelectWorld(world: WorldId): Boolean {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val existing = dao.getWorldProgressOnce(world.name)
        val isUnlocked = existing?.unlocked ?: (world.unlockCost == 0)
        if (isUnlocked) {
            dao.upsertProfile(profile.copy(selectedWorldId = world.name))
            return true
        }
        if (profile.coins < world.unlockCost) return false
        dao.upsertWorldProgress(
            WorldProgressEntity(
                worldId = world.name,
                unlocked = true,
                bestWave = existing?.bestWave ?: 0,
                bestKills = existing?.bestKills ?: 0
            )
        )
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - world.unlockCost,
                selectedWorldId = world.name
            )
        )
        return true
    }

    suspend fun setPreferredCameraMode(mode: CameraMode) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(profile.copy(preferredCameraMode = mode.name))
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(profile.copy(soundEnabled = enabled))
    }

    suspend fun updateSettings(transform: (PlayerProfileEntity) -> PlayerProfileEntity) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(transform(profile))
    }

    suspend fun recordMatchResult(
        world: WorldId,
        earnedCoins: Int,
        kills: Int,
        waveReached: Int
    ) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins + earnedCoins.coerceAtLeast(0),
                totalKills = profile.totalKills + kills.coerceAtLeast(0),
                totalGames = profile.totalGames + 1
            )
        )
        val existing = dao.getWorldProgressOnce(world.name)
        dao.upsertWorldProgress(
            WorldProgressEntity(
                worldId = world.name,
                unlocked = true,
                bestWave = maxOf(existing?.bestWave ?: 0, waveReached),
                bestKills = maxOf(existing?.bestKills ?: 0, kills)
            )
        )
    }
}
