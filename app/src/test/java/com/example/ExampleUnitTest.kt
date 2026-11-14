package com.example

import com.example.audio.SoundEffectsEngine
import com.example.data.PlayerMetaState
import com.example.game.engine.WorldMapGenerator
import com.example.game.engine.WorldSimulation
import com.example.game.model.AppLanguage
import com.example.game.model.CameraMode
import com.example.game.model.CharacterOutfitId
import com.example.game.model.SpecialAbilityId
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponId
import com.example.game.model.WeaponSkinId
import com.example.game.model.WorldId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun allSixWorldsGenerateLargeDrivableMapsAndBilingualTitles() {
        for (worldId in WorldId.entries) {
            val generated = WorldMapGenerator.generate(worldId)
            assertEquals(195f, generated.halfExtent)
            assertTrue("Expected road grid for $worldId", generated.roads.size >= 10)
            assertTrue("Expected obstacles for $worldId", generated.obstacles.size >= 40)
            assertTrue("Expected drivable cars for $worldId", generated.initialVehicles.size >= 4)
            assertTrue(worldId.title(AppLanguage.DE).isNotBlank())
            assertTrue(worldId.title(AppLanguage.EN).isNotBlank())
        }
    }

    @Test
    fun simulationSupportsDualWeaponsSpecialAbilitiesLockerAndDriving() {
        val world = WorldMapGenerator.generate(WorldId.CITY)
        val meta = PlayerMetaState(
            equippedWeapon = WeaponId.ASSAULT_RIFLE,
            secondaryWeapon = WeaponId.SHOTGUN,
            equippedSkin = WeaponSkinId.TOXIC_NEON,
            equippedOutfit = CharacterOutfitId.CYBER_SAMURAI,
            equippedCarPaint = VehiclePaintId.SYNTH_DRIFTER,
            equippedAbility = SpecialAbilityId.SENTRY_TURRET,
            preferredCameraMode = CameraMode.MODE_3D,
            soundEnabled = false
        )
        val sim = WorldSimulation(world, meta, SoundEffectsEngine().apply { enabled = false })

        // Verify primary & secondary weapon switching
        assertEquals(WeaponId.ASSAULT_RIFLE, sim.weapon)
        sim.switchWeaponSlot()
        assertEquals(WeaponId.SHOTGUN, sim.weapon)
        sim.switchWeaponSlot()
        assertEquals(WeaponId.ASSAULT_RIFLE, sim.weapon)

        // Activate Sentry Turret special ability
        assertTrue(sim.turrets.isEmpty())
        sim.activateSpecialAbility()
        assertEquals(1, sim.turrets.size)
        assertTrue(sim.abilityCooldown > 0f)

        // Toggle 3D -> 2D
        sim.cameraMode = CameraMode.MODE_2D
        assertEquals(CameraMode.MODE_2D, sim.cameraMode)

        // Move near starter car and enter it
        val starterCar = sim.vehicles.first()
        sim.playerX = starterCar.x
        sim.playerZ = starterCar.z
        sim.toggleVehicle()
        assertTrue(sim.buildHudSnapshot().inVehicle)

        // Drive forward with gas
        sim.gasHeld = true
        repeat(30) { sim.step(0.016f) }
        assertTrue("Car should accelerate when gas is held", starterCar.speed > 5f)

        // Exit vehicle and fire weapon
        sim.gasHeld = false
        sim.toggleVehicle()
        assertFalse(sim.buildHudSnapshot().inVehicle)
        sim.fireButtonHeld = true
        sim.step(0.016f)
        assertTrue("Weapon should fire projectile", sim.projectiles.isNotEmpty())
    }
}
