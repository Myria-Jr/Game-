package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ProgressRepository
import com.example.game.model.AppLanguage
import com.example.game.model.CharacterOutfitId
import com.example.game.model.MissionId
import com.example.game.model.SpecialAbilityId
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponId
import com.example.game.model.WeaponSkinId
import com.example.game.model.WorldId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: ProgressRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = ProgressRepository(db.gameDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun appNameLockerArsenalMissionsAndSettingsPersistenceWork() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Deadzone Drive", context.getString(R.string.app_name))

        repo.ensureInitialized()
        repo.recordMatchResult(WorldId.CITY, earnedCoins = 1500, kills = 30, waveReached = 4)

        // Unlock & equip secondary weapon + special ability in Arsenal
        assertTrue(repo.buyOrEquipWeapon(WeaponId.SHOTGUN, currentlyUnlocked = false, asSecondary = true))
        assertTrue(repo.buyOrEquipAbility(SpecialAbilityId.SENTRY_TURRET, currentlyUnlocked = false))

        // Unlock & equip Outfit, Car Paint, and Weapon Skin in Locker
        assertTrue(repo.buyOrEquipOutfit(CharacterOutfitId.CYBER_SAMURAI, currentlyUnlocked = false))
        assertTrue(repo.buyOrEquipCarPaint(VehiclePaintId.NIGHT_INTERCEPTOR, currentlyUnlocked = false))
        assertTrue(repo.buyOrEquipSkin(WeaponSkinId.TOXIC_NEON, currentlyUnlocked = false))

        // Claim completed mission (FIRST_BLOOD requires 25 kills, we have 30)
        assertTrue(repo.claimMissionReward(MissionId.FIRST_BLOOD))

        // Switch language to English and update music volume
        repo.updateSettings { it.copy(languageCode = AppLanguage.EN.code, musicVolume = 0.5f) }

        val state = repo.metaState.first()
        assertEquals(WeaponId.SHOTGUN, state.secondaryWeapon)
        assertEquals(SpecialAbilityId.SENTRY_TURRET, state.equippedAbility)
        assertEquals(CharacterOutfitId.CYBER_SAMURAI, state.equippedOutfit)
        assertEquals(VehiclePaintId.NIGHT_INTERCEPTOR, state.equippedCarPaint)
        assertEquals(WeaponSkinId.TOXIC_NEON, state.equippedSkin)
        assertEquals(AppLanguage.EN, state.language)
        assertEquals(0.5f, state.musicVolume, 0.01f)
        assertTrue(MissionId.FIRST_BLOOD in state.claimedMissions)
    }
}
