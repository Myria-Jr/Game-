package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT weaponId FROM unlocked_weapons")
    fun observeUnlockedWeapons(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedWeapon(entity: UnlockedWeaponEntity)

    @Query("SELECT skinId FROM unlocked_skins")
    fun observeUnlockedSkins(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedSkin(entity: UnlockedSkinEntity)

    @Query("SELECT outfitId FROM unlocked_outfits")
    fun observeUnlockedOutfits(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedOutfit(entity: UnlockedOutfitEntity)

    @Query("SELECT paintId FROM unlocked_car_paints")
    fun observeUnlockedCarPaints(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedCarPaint(entity: UnlockedCarPaintEntity)

    @Query("SELECT abilityId FROM unlocked_abilities")
    fun observeUnlockedAbilities(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedAbility(entity: UnlockedAbilityEntity)

    @Query("SELECT missionId FROM claimed_missions")
    fun observeClaimedMissions(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClaimedMission(entity: ClaimedMissionEntity)

    @Query("SELECT * FROM world_progress")
    fun observeWorldProgress(): Flow<List<WorldProgressEntity>>

    @Query("SELECT * FROM world_progress WHERE worldId = :worldId")
    suspend fun getWorldProgressOnce(worldId: String): WorldProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorldProgress(entity: WorldProgressEntity)
}
