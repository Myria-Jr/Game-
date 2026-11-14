package com.example.game.engine

import com.example.audio.SoundEffectsEngine
import com.example.data.PlayerMetaState
import com.example.game.model.AppLanguage
import com.example.game.model.CameraMode
import com.example.game.model.CharacterOutfitId
import com.example.game.model.MonsterType
import com.example.game.model.SpecialAbilityId
import com.example.game.model.SupplyKind
import com.example.game.model.TimeOfDayMode
import com.example.game.model.UpgradeType
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponId
import com.example.game.model.WeaponSkinId
import com.example.game.model.WeatherKind
import com.example.game.model.WorldId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class VehicleEntity(
    val id: Int,
    var x: Float,
    var z: Float,
    var yaw: Float,
    var speed: Float = 0f,
    var hp: Float,
    val maxHp: Float,
    var nitro: Float = 100f
)

data class MonsterEntity(
    val id: Int,
    val type: MonsterType,
    var x: Float,
    var z: Float,
    var yaw: Float,
    var hp: Float,
    val maxHp: Float,
    var slowTimer: Float = 0f,
    var attackCooldown: Float = 0f,
    var hitFlash: Float = 0f
)

data class ProjectileEntity(
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vz: Float,
    val damage: Float,
    var life: Float,
    val fromMonster: Boolean,
    var remainingPierce: Int,
    val slowFactor: Float,
    val color: FloatArray,
    val hitIds: MutableSet<Int> = mutableSetOf()
)

data class SentryTurretEntity(
    val x: Float,
    val z: Float,
    var yaw: Float,
    var remainingLife: Float = 12f,
    var fireCooldown: Float = 0f
)

data class SupplyDropEntity(
    val id: Int,
    val kind: SupplyKind,
    val x: Float,
    val z: Float,
    var pulsePhase: Float = 0f
)

data class ShockwaveEffect(
    val x: Float,
    val z: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val color: FloatArray = floatArrayOf(1.0f, 0.55f, 0.12f)
)

data class MinimapBlip(
    val nx: Float,
    val nz: Float,
    val kind: BlipKind
)

enum class BlipKind {
    PLAYER, VEHICLE, MONSTER, BOSS, SUPPLY, TURRET
}

data class HudSnapshot(
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val ammo: Int = 16,
    val maxAmmo: Int = 16,
    val secondaryAmmo: Int = 8,
    val secondaryMaxAmmo: Int = 8,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val inVehicle: Boolean = false,
    val nearVehicle: Boolean = false,
    val vehicleHp: Float = 0f,
    val vehicleMaxHp: Float = 200f,
    val vehicleNitro: Float = 100f,
    val speedKmh: Int = 0,
    val wave: Int = 1,
    val monstersRemaining: Int = 0,
    val waveBreakSeconds: Int = 0,
    val bossName: String? = null,
    val bossHp: Float = 0f,
    val bossMaxHp: Float = 1f,
    val sessionCoins: Int = 0,
    val sessionKills: Int = 0,
    val weapon: WeaponId = WeaponId.PISTOL,
    val secondaryWeapon: WeaponId = WeaponId.SHOTGUN,
    val isSecondaryActive: Boolean = false,
    val skin: WeaponSkinId = WeaponSkinId.STANDARD_STEEL,
    val outfit: CharacterOutfitId = CharacterOutfitId.WASTELAND_RANGER,
    val carPaint: VehiclePaintId = VehiclePaintId.BIOME_DEFAULT,
    val ability: SpecialAbilityId = SpecialAbilityId.EMP_SHOCKWAVE,
    val abilityCooldown: Float = 0f,
    val abilityCooldownMax: Float = 14f,
    val shieldActiveRemaining: Float = 0f,
    val worldId: WorldId = WorldId.CITY,
    val cameraMode: CameraMode = CameraMode.MODE_3D,
    val weather: WeatherKind = WeatherKind.CLEAR,
    val timeOfDayLabel: String = "Nacht",
    val fps: Int = 60,
    val showFps: Boolean = true,
    val joystickScale: Float = 1.0f,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val bannerMessage: String? = null,
    val minimapBlips: List<MinimapBlip> = emptyList(),
    val playerX: Float = 0f,
    val playerZ: Float = 0f
)

class WorldSimulation(
    val world: GeneratedWorld,
    meta: PlayerMetaState,
    private val sound: SoundEffectsEngine
) {
    val primaryWeapon: WeaponId = meta.equippedWeapon
    val secondaryWeapon: WeaponId = meta.secondaryWeapon
    var usingSecondarySlot: Boolean = false
        private set

    val weapon: WeaponId
        get() = if (usingSecondarySlot) secondaryWeapon else primaryWeapon

    val skin: WeaponSkinId = meta.equippedSkin
    val outfit: CharacterOutfitId = meta.equippedOutfit
    val carPaint: VehiclePaintId = meta.equippedCarPaint
    val ability: SpecialAbilityId = meta.equippedAbility
    var language: AppLanguage = meta.language

    var graphicsQuality: Int = meta.graphicsQuality
    var showFps: Boolean = meta.showFps
    var weatherEnabled: Boolean = meta.weatherEnabled
    var timeOfDayMode: TimeOfDayMode = meta.timeOfDayMode
    var joystickScale: Float = meta.joystickScale
    var aimSensitivity: Float = meta.aimSensitivity
    var autoFireAssist: Boolean = meta.autoFireAssist
    var currentFps: Int = 60

    private val damageMultiplier = 1f + meta.upgradeLevel(UpgradeType.DAMAGE_BOOST) * 0.12f
    private val moveSpeedMultiplier = 1f + meta.upgradeLevel(UpgradeType.MOVE_SPEED) * 0.08f
    private val maxPlayerHp = 100f + meta.upgradeLevel(UpgradeType.MAX_HEALTH) * 25f
    private val maxVehicleHp = 220f + meta.upgradeLevel(UpgradeType.CAR_ARMOR) * 35f
    private val ramDamageMultiplier = 1f + meta.upgradeLevel(UpgradeType.CAR_ARMOR) * 0.22f

    // Player state
    var playerX: Float = 0f
    var playerZ: Float = 0f
    var playerYaw: Float = 0f
    var cameraYaw: Float = 0f
    var playerHp: Float = maxPlayerHp
    var playerHitFlash: Float = 0f
    var shieldActiveTimer: Float = 0f
    var worldClock: Float = 0f

    // Dual Weapon state
    private var primaryAmmo: Int = primaryWeapon.magazineSize
    private var secondaryAmmo: Int = secondaryWeapon.magazineSize
    var ammoInMag: Int
        get() = if (usingSecondarySlot) secondaryAmmo else primaryAmmo
        set(value) {
            if (usingSecondarySlot) secondaryAmmo = value else primaryAmmo = value
        }

    var isReloading: Boolean = false
    private var reloadElapsedMs: Long = 0L
    private var fireCooldownMs: Long = 0L
    var muzzleFlashTimer: Float = 0f

    // Special Ability state
    var abilityCooldown: Float = 0f
    val turrets = mutableListOf<SentryTurretEntity>()

    // Vehicle state
    val vehicles = mutableListOf<VehicleEntity>()
    var activeVehicleId: Int? = null

    // Entities
    val monsters = mutableListOf<MonsterEntity>()
    val projectiles = mutableListOf<ProjectileEntity>()
    val supplies = mutableListOf<SupplyDropEntity>()
    val shockwaves = mutableListOf<ShockwaveEffect>()

    // Wave & progression state
    var wave: Int = 1
    private var waveBreakTimer: Float = 1.5f
    private var pendingSpawns = mutableListOf<MonsterType>()
    private var spawnTickTimer: Float = 0f
    var sessionCoins: Int = 0
    var sessionKills: Int = 0
    var isPaused: Boolean = false
    var isGameOver: Boolean = false
    var cameraMode: CameraMode = meta.preferredCameraMode

    private var bannerMessage: String? = null
    private var bannerTimer: Float = 0f
    private var supplyTimer: Float = 6f
    private var nextEntityId: Int = 100
    private val rng = Random(System.currentTimeMillis())

    // Input state (updated from Compose HUD)
    @Volatile var moveStickX: Float = 0f
    @Volatile var moveStickY: Float = 0f
    @Volatile var aimStickX: Float = 0f
    @Volatile var aimStickY: Float = 0f
    @Volatile var fireButtonHeld: Boolean = false
    @Volatile var steerLeftHeld: Boolean = false
    @Volatile var steerRightHeld: Boolean = false
    @Volatile var gasHeld: Boolean = false
    @Volatile var brakeHeld: Boolean = false
    @Volatile var nitroHeld: Boolean = false

    init {
        world.initialVehicles.forEachIndexed { idx, spec ->
            vehicles.add(
                VehicleEntity(
                    id = idx + 1,
                    x = spec.x,
                    z = spec.z,
                    yaw = spec.yaw,
                    hp = maxVehicleHp,
                    maxHp = maxVehicleHp
                )
            )
        }
        spawnInitialSupplies()
        prepareWave(1)
    }

    fun resetInputs() {
        moveStickX = 0f
        moveStickY = 0f
        aimStickX = 0f
        aimStickY = 0f
        fireButtonHeld = false
        steerLeftHeld = false
        steerRightHeld = false
        gasHeld = false
        brakeHeld = false
        nitroHeld = false
    }

    @Synchronized
    fun applyLiveSettings(meta: PlayerMetaState) {
        language = meta.language
        graphicsQuality = meta.graphicsQuality
        showFps = meta.showFps
        weatherEnabled = meta.weatherEnabled
        timeOfDayMode = meta.timeOfDayMode
        joystickScale = meta.joystickScale
        aimSensitivity = meta.aimSensitivity
        autoFireAssist = meta.autoFireAssist
        sound.enabled = meta.soundEnabled
        sound.sfxVolume = meta.sfxVolume
    }

    private fun spawnInitialSupplies() {
        val kinds = listOf(SupplyKind.AMMO_CRATE, SupplyKind.COIN_CACHE, SupplyKind.MEDKIT, SupplyKind.REPAIR_KIT)
        world.supplyAnchors.take(5).forEachIndexed { i, (sx, sz) ->
            supplies.add(SupplyDropEntity(nextEntityId++, kinds[i % kinds.size], sx, sz))
        }
    }

    private fun prepareWave(waveNum: Int) {
        wave = waveNum
        pendingSpawns.clear()
        val count = ((6 + waveNum * 4) * world.worldId.monsterSpawnMultiplier).toInt()
        for (i in 0 until count) {
            val roll = rng.nextFloat()
            val type = when {
                waveNum >= 3 && roll < 0.16f -> MonsterType.BRUTE
                waveNum >= 2 && roll < 0.38f -> MonsterType.SPITTER
                waveNum >= 2 && roll < 0.58f -> MonsterType.STALKER
                else -> MonsterType.RUNNER
            }
            pendingSpawns.add(type)
        }
        if (waveNum % 3 == 0) {
            pendingSpawns.add(MonsterType.BOSS_TITAN)
            val msg = if (language == AppLanguage.DE) {
                "BOSS-ALARM: ${world.worldId.bossTitle(language)}!"
            } else {
                "BOSS ALERT: ${world.worldId.bossTitle(language)}!"
            }
            showBanner(msg, 4.0f)
            sound.playBossAlert()
        } else {
            val msg = if (language == AppLanguage.DE) "WELLE $waveNum STARTET" else "WAVE $waveNum INCOMING"
            showBanner(msg, 2.4f)
        }
        spawnTickTimer = 0.25f
    }

    fun showBanner(text: String, duration: Float = 2.5f) {
        bannerMessage = text
        bannerTimer = duration
    }

    @Synchronized
    fun switchWeaponSlot() {
        if (primaryWeapon == secondaryWeapon) return
        usingSecondarySlot = !usingSecondarySlot
        isReloading = false
        reloadElapsedMs = 0L
        fireCooldownMs = 120L
        sound.playWeaponSwap()
        showBanner(weapon.title(language), 1.2f)
    }

    @Synchronized
    fun activateSpecialAbility() {
        if (isPaused || isGameOver || abilityCooldown > 0f) return
        abilityCooldown = ability.cooldownSec
        sound.playAbilityActivate()

        when (ability) {
            SpecialAbilityId.EMP_SHOCKWAVE -> {
                val empDmg = 140f * damageMultiplier
                val empRadius = 18.5f
                shockwaves.add(
                    ShockwaveEffect(
                        x = playerX,
                        z = playerZ,
                        radius = 1.2f,
                        maxRadius = empRadius,
                        alpha = 1.0f,
                        color = floatArrayOf(0.14f, 0.88f, 1.0f)
                    )
                )
                val mIter = monsters.iterator()
                while (mIter.hasNext()) {
                    val m = mIter.next()
                    val dist = hypot(m.x - playerX, m.z - playerZ)
                    if (dist <= empRadius) {
                        m.hp -= empDmg
                        m.hitFlash = 1f
                        m.slowTimer = 4.5f
                        if (m.hp <= 0f) {
                            onMonsterKilled(m)
                            mIter.remove()
                        }
                    }
                }
                showBanner(if (language == AppLanguage.DE) "EMP-NOVA ENTLADEN!" else "EMP NOVA UNLEASHED!", 1.8f)
            }

            SpecialAbilityId.SENTRY_TURRET -> {
                val tx = playerX + sin(playerYaw) * 2.2f
                val tz = playerZ + cos(playerYaw) * 2.2f
                turrets.add(SentryTurretEntity(x = tx, z = tz, yaw = playerYaw, remainingLife = 12f))
                showBanner(if (language == AppLanguage.DE) "GESCHÜTZTURM AKTIV!" else "SENTRY TURRET DEPLOYED!", 1.8f)
            }

            SpecialAbilityId.OVERDRIVE_SHIELD -> {
                shieldActiveTimer = 6.0f
                playerHp = (playerHp + 35f).coerceAtMost(maxPlayerHp)
                vehicles.forEach { it.hp = (it.hp + 45f).coerceAtMost(maxVehicleHp) }
                shockwaves.add(
                    ShockwaveEffect(
                        x = playerX,
                        z = playerZ,
                        radius = 1.0f,
                        maxRadius = 8.0f,
                        alpha = 0.9f,
                        color = floatArrayOf(0.98f, 0.82f, 0.16f)
                    )
                )
                showBanner(if (language == AppLanguage.DE) "OVERDRIVE-SCHILD AKTIV!" else "OVERDRIVE SHIELD ACTIVE!", 1.8f)
            }
        }
    }

    @Synchronized
    fun triggerReload() {
        if (!isReloading && ammoInMag < weapon.magazineSize) {
            isReloading = true
            reloadElapsedMs = 0L
        }
    }

    @Synchronized
    fun toggleVehicle() {
        if (isGameOver || isPaused) return
        resetInputs()
        val currentId = activeVehicleId
        if (currentId != null) {
            val car = vehicles.firstOrNull { it.id == currentId }
            activeVehicleId = null
            if (car != null) {
                val exitOffsetX = -cos(car.yaw) * 3.0f
                val exitOffsetZ = sin(car.yaw) * 3.0f
                playerX = (car.x + exitOffsetX).coerceIn(-world.halfExtent + 6f, world.halfExtent - 6f)
                playerZ = (car.z + exitOffsetZ).coerceIn(-world.halfExtent + 6f, world.halfExtent - 6f)
            }
            return
        }
        val nearest = findNearestEnterableVehicle() ?: return
        activeVehicleId = nearest.id
        sound.playCarIgnition()
        showBanner(
            if (language == AppLanguage.DE) "FAHRZEUG AKTIV • RAMME MONSTER!" else "VEHICLE ACTIVE • RAM THE HORDE!",
            1.8f
        )
    }

    private fun findNearestEnterableVehicle(): VehicleEntity? {
        return vehicles
            .filter { it.hp > 5f }
            .minByOrNull { hypot(it.x - playerX, it.z - playerZ) }
            ?.takeIf { hypot(it.x - playerX, it.z - playerZ) <= 6.2f }
    }

    @Synchronized
    fun step(rawDt: Float) {
        if (isPaused || isGameOver) return
        val dt = rawDt.coerceIn(0.001f, 0.05f)
        val dtMs = (dt * 1000f).toLong().coerceAtLeast(1L)
        worldClock += dt

        if (bannerTimer > 0f) {
            bannerTimer -= dt
            if (bannerTimer <= 0f) bannerMessage = null
        }
        if (playerHitFlash > 0f) playerHitFlash = (playerHitFlash - dt * 3f).coerceAtLeast(0f)
        if (muzzleFlashTimer > 0f) muzzleFlashTimer = (muzzleFlashTimer - dt * 10f).coerceAtLeast(0f)
        if (abilityCooldown > 0f) abilityCooldown = (abilityCooldown - dt).coerceAtLeast(0f)
        if (shieldActiveTimer > 0f) shieldActiveTimer = (shieldActiveTimer - dt).coerceAtLeast(0f)

        updateReloadAndCooldown(dtMs)

        val drivingCar = activeVehicleId?.let { id -> vehicles.firstOrNull { it.id == id } }
        if (drivingCar != null) {
            updateDriving(drivingCar, dt)
        } else {
            updateOnFoot(dt)
        }

        // Handle firing: manual fire, right aim stick, or Auto-Fire Assist when enemy is in range
        val aimMag = hypot(aimStickX, aimStickY)
        val autoTargetInRange = autoFireAssist && monsters.any {
            hypot(it.x - playerX, it.z - playerZ) <= weapon.range * 0.82f
        }
        val wantsFire = fireButtonHeld || aimMag > 0.32f || (autoTargetInRange && drivingCar == null)
        if (wantsFire) {
            tryFire(drivingCar)
        }

        updateTurrets(dt)
        updateWaveSpawning(dt)
        updateMonsters(dt, drivingCar)
        updateProjectiles(dt)
        updateSupplies(dt)
        updateShockwaves(dt)
    }

    private fun updateReloadAndCooldown(dtMs: Long) {
        if (fireCooldownMs > 0L) fireCooldownMs = (fireCooldownMs - dtMs).coerceAtLeast(0L)
        if (isReloading) {
            reloadElapsedMs += dtMs
            if (reloadElapsedMs >= weapon.reloadMs) {
                ammoInMag = weapon.magazineSize
                isReloading = false
                reloadElapsedMs = 0L
            }
        }
    }

    private fun updateOnFoot(dt: Float) {
        val sx = moveStickX
        val sz = moveStickY
        val mag = hypot(sx, sz)

        val ax = aimStickX
        val az = aimStickY
        val aimMag = hypot(ax, az)
        val sens = aimSensitivity.coerceIn(0.5f, 1.6f)

        // Update aim yaw when right stick is used
        if (aimMag > 0.2f) {
            val targetYaw = if (cameraMode == CameraMode.MODE_2D) {
                atan2(ax, az)
            } else {
                val localYaw = atan2(-ax, -az)
                cameraYaw + localYaw
            }
            playerYaw = lerpAngle(playerYaw, targetYaw, dt * 12f * sens)
        }

        if (mag > 0.08f) {
            val shieldBoost = if (shieldActiveTimer > 0f) 1.25f else 1.0f
            val moveSpeed = 11.5f * moveSpeedMultiplier * shieldBoost * mag.coerceAtMost(1f)
            val (worldDx, worldDz) = if (cameraMode == CameraMode.MODE_2D) {
                sx to sz
            } else {
                val forwardX = sin(cameraYaw)
                val forwardZ = cos(cameraYaw)
                val rightX = -cos(cameraYaw)
                val rightZ = sin(cameraYaw)
                val forwardInput = -sz
                val strafeInput = sx
                (forwardX * forwardInput + rightX * strafeInput) to (forwardZ * forwardInput + rightZ * strafeInput)
            }

            val norm = hypot(worldDx, worldDz).coerceAtLeast(0.001f)
            val vx = (worldDx / norm) * moveSpeed
            val vz = (worldDz / norm) * moveSpeed

            playerX += vx * dt
            playerZ += vz * dt
            val resolved = resolveObstacleCollision(playerX, playerZ, 0.75f)
            playerX = resolved.first
            playerZ = resolved.second

            if (aimMag <= 0.2f) {
                val moveYaw = atan2(vx, vz)
                playerYaw = lerpAngle(playerYaw, moveYaw, dt * 8f * sens)
                if (cameraMode == CameraMode.MODE_3D) {
                    cameraYaw = lerpAngle(cameraYaw, playerYaw, dt * 2.2f * sens)
                }
            }
        }
        if (aimMag > 0.2f && cameraMode == CameraMode.MODE_3D) {
            cameraYaw = lerpAngle(cameraYaw, playerYaw, dt * 3.5f * sens)
        }
    }

    private fun updateDriving(car: VehicleEntity, dt: Float) {
        val throttle = when {
            gasHeld -> 1f
            brakeHeld -> -0.65f
            moveStickY < -0.2f -> (-moveStickY).coerceAtMost(1f)
            moveStickY > 0.2f -> -moveStickY.coerceAtMost(1f) * 0.65f
            else -> 0f
        }
        val steerInput = when {
            steerLeftHeld -> 1f
            steerRightHeld -> -1f
            abs(moveStickX) > 0.15f -> -moveStickX.coerceIn(-1f, 1f)
            else -> 0f
        }

        val shieldBoost = if (shieldActiveTimer > 0f) 1.2f else 1.0f
        val usingNitro = nitroHeld && car.nitro > 4f && throttle > 0f
        val maxSpeed = (if (usingNitro) 44f else 31f) * shieldBoost
        val accel = if (usingNitro) 42f else 24f

        if (usingNitro) {
            car.nitro = (car.nitro - 28f * dt).coerceAtLeast(0f)
        } else {
            car.nitro = (car.nitro + 10f * dt).coerceAtMost(100f)
        }

        if (abs(throttle) > 0.05f) {
            car.speed = (car.speed + throttle * accel * dt).coerceIn(-14f, maxSpeed)
        } else {
            val drag = 14f * dt
            car.speed = when {
                car.speed > drag -> car.speed - drag
                car.speed < -drag -> car.speed + drag
                else -> 0f
            }
        }

        if (abs(car.speed) > 0.8f) {
            val turnDir = if (car.speed >= 0f) 1f else -1f
            val speedFactor = (abs(car.speed) / 16f).coerceIn(0.35f, 1.15f)
            car.yaw += steerInput * 2.4f * aimSensitivity * speedFactor * turnDir * dt
        }

        val vx = sin(car.yaw) * car.speed
        val vz = cos(car.yaw) * car.speed
        val nextX = car.x + vx * dt
        val nextZ = car.z + vz * dt

        val (rx, rz, hitWall) = resolveObstacleCollisionDetailed(nextX, nextZ, 2.0f)
        if (hitWall) {
            car.speed *= -0.25f
        }
        car.x = rx
        car.z = rz

        playerX = car.x
        playerZ = car.z
        playerYaw = car.yaw
        cameraYaw = lerpAngle(cameraYaw, car.yaw, dt * 6.5f)

        if (car.hp <= 0f) {
            car.hp = 0f
            activeVehicleId = null
            showBanner(if (language == AppLanguage.DE) "FAHRZEUG ZERSTÖRT!" else "VEHICLE DESTROYED!", 2.5f)
        }
    }

    private fun tryFire(drivingCar: VehicleEntity?) {
        if (isReloading || fireCooldownMs > 0L) return
        if (ammoInMag <= 0) {
            triggerReload()
            return
        }

        var aimAngle = playerYaw
        val aimMag = hypot(aimStickX, aimStickY)

        if (aimMag <= 0.25f || drivingCar != null) {
            val bestTarget = monsters
                .filter { hypot(it.x - playerX, it.z - playerZ) <= weapon.range }
                .minByOrNull { hypot(it.x - playerX, it.z - playerZ) }
            if (bestTarget != null) {
                aimAngle = atan2(bestTarget.x - playerX, bestTarget.z - playerZ)
                if (drivingCar == null) playerYaw = aimAngle
            }
        }

        ammoInMag--
        fireCooldownMs = weapon.fireRateMs
        muzzleFlashTimer = 1.0f

        val spawnDist = if (drivingCar != null) 1.8f else 1.2f
        val startX = playerX + sin(aimAngle) * spawnDist
        val startZ = playerZ + cos(aimAngle) * spawnDist
        val startY = if (drivingCar != null) 2.2f else 1.35f

        for (p in 0 until weapon.pellets) {
            val spread = (rng.nextFloat() - 0.5f) * weapon.spreadRad
            val shotYaw = aimAngle + spread
            val speed = weapon.projectileSpeed
            projectiles.add(
                ProjectileEntity(
                    x = startX,
                    y = startY,
                    z = startZ,
                    vx = sin(shotYaw) * speed,
                    vz = cos(shotYaw) * speed,
                    damage = weapon.damage * damageMultiplier,
                    life = weapon.range / speed,
                    fromMonster = false,
                    remainingPierce = weapon.pierceCount,
                    slowFactor = weapon.slowFactor,
                    color = skin.tracerColor
                )
            )
        }

        sound.playShoot(
            isShotgun = weapon == WeaponId.SHOTGUN,
            isPlasma = weapon == WeaponId.PLASMA_RIFLE || weapon == WeaponId.SNIPER
        )

        if (ammoInMag <= 0) triggerReload()
    }

    private fun updateTurrets(dt: Float) {
        val iter = turrets.iterator()
        while (iter.hasNext()) {
            val t = iter.next()
            t.remainingLife -= dt
            if (t.remainingLife <= 0f) {
                iter.remove()
                continue
            }
            if (t.fireCooldown > 0f) t.fireCooldown -= dt
            val target = monsters
                .filter { hypot(it.x - t.x, it.z - t.z) <= 44f }
                .minByOrNull { hypot(it.x - t.x, it.z - t.z) }
            if (target != null) {
                t.yaw = atan2(target.x - t.x, target.z - t.z)
                if (t.fireCooldown <= 0f) {
                    t.fireCooldown = 0.22f
                    val spd = 85f
                    projectiles.add(
                        ProjectileEntity(
                            x = t.x + sin(t.yaw) * 0.9f,
                            y = 1.4f,
                            z = t.z + cos(t.yaw) * 0.9f,
                            vx = sin(t.yaw) * spd,
                            vz = cos(t.yaw) * spd,
                            damage = 24f * damageMultiplier,
                            life = 0.65f,
                            fromMonster = false,
                            remainingPierce = 1,
                            slowFactor = 1f,
                            color = floatArrayOf(1.0f, 0.55f, 0.15f)
                        )
                    )
                    sound.playShoot(isShotgun = false, isPlasma = false)
                }
            }
        }
    }

    private fun updateWaveSpawning(dt: Float) {
        if (pendingSpawns.isNotEmpty()) {
            spawnTickTimer -= dt
            if (spawnTickTimer <= 0f && monsters.size < 34) {
                val type = pendingSpawns.removeAt(0)
                spawnMonster(type)
                spawnTickTimer = if (type == MonsterType.BOSS_TITAN) 1.2f else 0.55f
            }
        } else if (monsters.isEmpty()) {
            if (waveBreakTimer <= 0f) {
                waveBreakTimer = 3.2f
                val bonus = 25 + wave * 10
                val msg = if (language == AppLanguage.DE) {
                    "WELLE $wave GESCHAFFT! +$bonus COINS"
                } else {
                    "WAVE $wave CLEARED! +$bonus COINS"
                }
                showBanner(msg, 2.5f)
                sessionCoins += bonus
                sound.playCoin()
            } else {
                waveBreakTimer -= dt
                if (waveBreakTimer <= 0f) {
                    prepareWave(wave + 1)
                }
            }
        }
    }

    private fun spawnMonster(type: MonsterType) {
        val angle = rng.nextFloat() * (2f * PI.toFloat())
        val dist = 36f + rng.nextFloat() * 24f
        val mx = (playerX + sin(angle) * dist).coerceIn(-world.halfExtent + 8f, world.halfExtent - 8f)
        val mz = (playerZ + cos(angle) * dist).coerceIn(-world.halfExtent + 8f, world.halfExtent - 8f)
        val waveScale = 1f + (wave - 1) * 0.14f
        val hp = type.baseHp * waveScale
        monsters.add(
            MonsterEntity(
                id = nextEntityId++,
                type = type,
                x = mx,
                z = mz,
                yaw = atan2(playerX - mx, playerZ - mz),
                hp = hp,
                maxHp = hp
            )
        )
    }

    private fun updateMonsters(dt: Float, drivingCar: VehicleEntity?) {
        val iterator = monsters.iterator()
        while (iterator.hasNext()) {
            val m = iterator.next()
            if (m.hitFlash > 0f) m.hitFlash = (m.hitFlash - dt * 5f).coerceAtLeast(0f)
            if (m.slowTimer > 0f) m.slowTimer = (m.slowTimer - dt).coerceAtLeast(0f)
            if (m.attackCooldown > 0f) m.attackCooldown = (m.attackCooldown - dt).coerceAtLeast(0f)

            val dx = playerX - m.x
            val dz = playerZ - m.z
            val dist = hypot(dx, dz).coerceAtLeast(0.01f)
            m.yaw = atan2(dx, dz)

            val slowMult = if (m.slowTimer > 0f) 0.52f else 1.0f
            val moveSpd = m.type.speed * slowMult

            val desiredDist = if (m.type == MonsterType.SPITTER) 16f else (m.type.radius + 0.9f)
            if (dist > desiredDist) {
                val nx = m.x + (dx / dist) * moveSpd * dt
                val nz = m.z + (dz / dist) * moveSpd * dt
                val (rx, rz) = resolveObstacleCollision(nx, nz, m.type.radius * 0.8f)
                m.x = rx
                m.z = rz
            }

            // Vehicle ramming collision!
            if (drivingCar != null && abs(drivingCar.speed) > 8.5f && dist < m.type.radius + 2.35f) {
                val ramDamage = abs(drivingCar.speed) * 4.6f * ramDamageMultiplier
                m.hp -= ramDamage
                m.hitFlash = 1f
                m.x -= (dx / dist) * 4.5f
                m.z -= (dz / dist) * 4.5f
                if (shieldActiveTimer <= 0f) {
                    drivingCar.hp = (drivingCar.hp - 5.5f).coerceAtLeast(0f)
                }
                sound.playHit()
                if (m.hp <= 0f) {
                    onMonsterKilled(m)
                    iterator.remove()
                    continue
                }
            }

            if (m.attackCooldown <= 0f) {
                when (m.type) {
                    MonsterType.SPITTER -> {
                        if (dist < 28f) {
                            m.attackCooldown = 2.0f
                            val speed = 26f
                            projectiles.add(
                                ProjectileEntity(
                                    x = m.x,
                                    y = 1.4f,
                                    z = m.z,
                                    vx = (dx / dist) * speed,
                                    vz = (dz / dist) * speed,
                                    damage = m.type.damage,
                                    life = 1.5f,
                                    fromMonster = true,
                                    remainingPierce = 1,
                                    slowFactor = 1f,
                                    color = floatArrayOf(0.45f, 1.0f, 0.25f)
                                )
                            )
                        }
                    }
                    MonsterType.BOSS_TITAN -> {
                        if (dist < 6.5f) {
                            m.attackCooldown = 1.6f
                            applyDamageToPlayerOrCar(m.type.damage, drivingCar)
                            shockwaves.add(ShockwaveEffect(m.x, m.z, 1.5f, 9.0f, 0.9f))
                        }
                    }
                    else -> {
                        val hitRange = m.type.radius + if (drivingCar != null) 2.4f else 1.2f
                        if (dist <= hitRange) {
                            m.attackCooldown = 1.05f
                            applyDamageToPlayerOrCar(m.type.damage, drivingCar)
                        }
                    }
                }
            }
        }
    }

    private fun applyDamageToPlayerOrCar(damage: Float, drivingCar: VehicleEntity?) {
        if (shieldActiveTimer > 0f) {
            // Overdrive shield absorbs incoming damage!
            return
        }
        if (drivingCar != null) {
            drivingCar.hp = (drivingCar.hp - damage).coerceAtLeast(0f)
            if (drivingCar.hp <= 0f) {
                activeVehicleId = null
                showBanner(if (language == AppLanguage.DE) "FAHRZEUG ZERSTÖRT!" else "VEHICLE DESTROYED!", 2.5f)
            }
        } else {
            playerHp = (playerHp - damage).coerceAtLeast(0f)
            playerHitFlash = 1f
            if (playerHp <= 0f) {
                isGameOver = true
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val drivingCar = activeVehicleId?.let { id -> vehicles.firstOrNull { it.id == id } }
        val pIter = projectiles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.z += p.vz * dt
            p.life -= dt

            if (p.life <= 0f || hitsObstacle(p.x, p.z)) {
                pIter.remove()
                continue
            }

            if (p.fromMonster) {
                val hitRadius = if (drivingCar != null) 2.2f else 0.95f
                if (hypot(p.x - playerX, p.z - playerZ) <= hitRadius) {
                    applyDamageToPlayerOrCar(p.damage, drivingCar)
                    pIter.remove()
                }
            } else {
                val mIter = monsters.iterator()
                var removeProj = false
                while (mIter.hasNext()) {
                    val m = mIter.next()
                    if (p.hitIds.contains(m.id)) continue
                    if (hypot(p.x - m.x, p.z - m.z) <= m.type.radius + 0.55f) {
                        p.hitIds.add(m.id)
                        m.hp -= p.damage
                        m.hitFlash = 1f
                        if (p.slowFactor < 1f) m.slowTimer = 2.2f
                        p.remainingPierce--
                        sound.playHit()

                        if (m.hp <= 0f) {
                            onMonsterKilled(m)
                            mIter.remove()
                        }
                        if (p.remainingPierce <= 0) {
                            removeProj = true
                            break
                        }
                    }
                }
                if (removeProj) pIter.remove()
            }
        }
    }

    private fun onMonsterKilled(m: MonsterEntity) {
        sessionKills++
        sessionCoins += m.type.coinReward
        if (m.type == MonsterType.BOSS_TITAN) {
            sound.playCoin()
            val msg = if (language == AppLanguage.DE) {
                "BOSS BESIEGT! +${m.type.coinReward} COINS"
            } else {
                "BOSS DEFEATED! +${m.type.coinReward} COINS"
            }
            showBanner(msg, 3.0f)
            supplies.add(SupplyDropEntity(nextEntityId++, SupplyKind.MEDKIT, m.x, m.z))
        }
    }

    private fun updateSupplies(dt: Float) {
        supplyTimer -= dt
        if (supplyTimer <= 0f && supplies.size < 10) {
            supplyTimer = 8.5f
            val anchor = world.supplyAnchors.random(rng)
            val kind = SupplyKind.entries.random(rng)
            supplies.add(SupplyDropEntity(nextEntityId++, kind, anchor.first, anchor.second))
        }

        val drivingCar = activeVehicleId?.let { id -> vehicles.firstOrNull { it.id == id } }
        val sIter = supplies.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.pulsePhase += dt * 4f
            val pickupRadius = if (drivingCar != null) 3.4f else 2.0f
            if (hypot(s.x - playerX, s.z - playerZ) <= pickupRadius) {
                when (s.kind) {
                    SupplyKind.MEDKIT -> {
                        playerHp = (playerHp + 45f).coerceAtMost(maxPlayerHp)
                        showBanner(if (language == AppLanguage.DE) "+45 LEBEN" else "+45 HEALTH", 1.5f)
                    }
                    SupplyKind.REPAIR_KIT -> {
                        vehicles.forEach { it.hp = maxVehicleHp }
                        showBanner(if (language == AppLanguage.DE) "FAHRZEUGE REPARIERT" else "VEHICLES REPAIRED", 1.5f)
                    }
                    SupplyKind.AMMO_CRATE -> {
                        primaryAmmo = primaryWeapon.magazineSize
                        secondaryAmmo = secondaryWeapon.magazineSize
                        isReloading = false
                        showBanner(if (language == AppLanguage.DE) "VOLLE MUNITION" else "FULL AMMO", 1.5f)
                    }
                    SupplyKind.COIN_CACHE -> {
                        sessionCoins += 45
                        showBanner(if (language == AppLanguage.DE) "+45 SCHROTT-COINS" else "+45 SCRAP COINS", 1.5f)
                    }
                }
                sound.playCoin()
                sIter.remove()
            }
        }
    }

    private fun updateShockwaves(dt: Float) {
        val iter = shockwaves.iterator()
        while (iter.hasNext()) {
            val sw = iter.next()
            sw.radius += dt * 15f
            sw.alpha = (1f - sw.radius / sw.maxRadius).coerceAtLeast(0f)
            if (sw.radius >= sw.maxRadius) iter.remove()
        }
    }

    private fun hitsObstacle(x: Float, z: Float): Boolean {
        if (abs(x) > world.halfExtent - 2f || abs(z) > world.halfExtent - 2f) return true
        return world.obstacles.any { o ->
            abs(x - o.x) < o.halfW && abs(z - o.z) < o.halfD
        }
    }

    private fun resolveObstacleCollision(x: Float, z: Float, radius: Float): Pair<Float, Float> {
        val res = resolveObstacleCollisionDetailed(x, z, radius)
        return res.first to res.second
    }

    private fun resolveObstacleCollisionDetailed(
        x: Float,
        z: Float,
        radius: Float
    ): Triple<Float, Float, Boolean> {
        var cx = x.coerceIn(-world.halfExtent + 5f, world.halfExtent - 5f)
        var cz = z.coerceIn(-world.halfExtent + 5f, world.halfExtent - 5f)
        var hit = (cx != x || cz != z)

        for (o in world.obstacles) {
            val dx = cx - o.x
            val dz = cz - o.z
            val overlapX = (o.halfW + radius) - abs(dx)
            val overlapZ = (o.halfD + radius) - abs(dz)
            if (overlapX > 0f && overlapZ > 0f) {
                hit = true
                if (overlapX < overlapZ) {
                    cx += if (dx >= 0f) overlapX else -overlapX
                } else {
                    cz += if (dz >= 0f) overlapZ else -overlapZ
                }
            }
        }
        return Triple(cx, cz, hit)
    }

    fun currentDaylightFactor(): Float {
        return when (timeOfDayMode) {
            TimeOfDayMode.DAY -> 1.55f
            TimeOfDayMode.DUSK -> 1.15f
            TimeOfDayMode.NIGHT -> 0.85f
            TimeOfDayMode.DYNAMIC -> {
                val cycle = (sin(worldClock * 0.08f) + 1f) * 0.5f // 0..1
                0.82f + cycle * 0.72f
            }
        }
    }

    @Synchronized
    fun buildHudSnapshot(): HudSnapshot {
        val car = activeVehicleId?.let { id -> vehicles.firstOrNull { it.id == id } }
        val nearCar = if (car == null) findNearestEnterableVehicle() != null else false
        val activeBoss = monsters.firstOrNull { it.type == MonsterType.BOSS_TITAN }

        val blips = mutableListOf<MinimapBlip>()
        val scale = world.halfExtent
        blips.add(MinimapBlip(playerX / scale, playerZ / scale, BlipKind.PLAYER))
        vehicles.filter { it.hp > 5f && it.id != activeVehicleId }.forEach { v ->
            blips.add(MinimapBlip(v.x / scale, v.z / scale, BlipKind.VEHICLE))
        }
        turrets.forEach { t ->
            blips.add(MinimapBlip(t.x / scale, t.z / scale, BlipKind.TURRET))
        }
        supplies.forEach { s ->
            blips.add(MinimapBlip(s.x / scale, s.z / scale, BlipKind.SUPPLY))
        }
        monsters.forEach { m ->
            blips.add(
                MinimapBlip(
                    m.x / scale,
                    m.z / scale,
                    if (m.type == MonsterType.BOSS_TITAN) BlipKind.BOSS else BlipKind.MONSTER
                )
            )
        }

        val reloadFrac = if (isReloading) {
            (reloadElapsedMs.toFloat() / weapon.reloadMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val standbyGun = if (usingSecondarySlot) primaryWeapon else secondaryWeapon
        val standbyAmmo = if (usingSecondarySlot) primaryAmmo else secondaryAmmo
        val activeWeather = if (weatherEnabled) world.worldId.defaultWeather else WeatherKind.CLEAR

        return HudSnapshot(
            hp = playerHp,
            maxHp = maxPlayerHp,
            ammo = ammoInMag,
            maxAmmo = weapon.magazineSize,
            secondaryAmmo = standbyAmmo,
            secondaryMaxAmmo = standbyGun.magazineSize,
            isReloading = isReloading,
            reloadProgress = reloadFrac,
            inVehicle = car != null,
            nearVehicle = nearCar,
            vehicleHp = car?.hp ?: 0f,
            vehicleMaxHp = maxVehicleHp,
            vehicleNitro = car?.nitro ?: 0f,
            speedKmh = car?.let { (abs(it.speed) * 4.2f).toInt() } ?: 0,
            wave = wave,
            monstersRemaining = monsters.size + pendingSpawns.size,
            waveBreakSeconds = if (monsters.isEmpty() && pendingSpawns.isEmpty()) waveBreakTimer.toInt().coerceAtLeast(0) else 0,
            bossName = activeBoss?.let { world.worldId.bossTitle(language) },
            bossHp = activeBoss?.hp ?: 0f,
            bossMaxHp = activeBoss?.maxHp ?: 1f,
            sessionCoins = sessionCoins,
            sessionKills = sessionKills,
            weapon = weapon,
            secondaryWeapon = standbyGun,
            isSecondaryActive = usingSecondarySlot,
            skin = skin,
            outfit = outfit,
            carPaint = carPaint,
            ability = ability,
            abilityCooldown = abilityCooldown,
            abilityCooldownMax = ability.cooldownSec,
            shieldActiveRemaining = shieldActiveTimer,
            worldId = world.worldId,
            cameraMode = cameraMode,
            weather = activeWeather,
            timeOfDayLabel = timeOfDayMode.title(language),
            fps = currentFps,
            showFps = showFps,
            joystickScale = joystickScale,
            isPaused = isPaused,
            isGameOver = isGameOver,
            bannerMessage = bannerMessage,
            minimapBlips = blips,
            playerX = playerX,
            playerZ = playerZ
        )
    }

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        var diff = (b - a) % (2f * PI.toFloat())
        if (diff > PI) diff -= (2f * PI.toFloat())
        if (diff < -PI) diff += (2f * PI.toFloat())
        return a + diff * t.coerceIn(0f, 1f)
    }
}
