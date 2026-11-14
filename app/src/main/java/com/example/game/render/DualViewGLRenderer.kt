package com.example.game.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.engine.MonsterEntity
import com.example.game.engine.ProjectileEntity
import com.example.game.engine.SentryTurretEntity
import com.example.game.engine.ShockwaveEffect
import com.example.game.engine.SupplyDropEntity
import com.example.game.engine.VehicleEntity
import com.example.game.engine.WorldObstacle
import com.example.game.engine.WorldSimulation
import com.example.game.model.CameraMode
import com.example.game.model.MonsterType
import com.example.game.model.ObstacleKind
import com.example.game.model.SupplyKind
import com.example.game.model.WeaponId
import com.example.game.model.WeatherKind
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class DualViewGLRenderer(
    private val simulation: WorldSimulation,
    private val onHudUpdate: () -> Unit
) : GLSurfaceView.Renderer {

    private lateinit var shader: GlShaderProgram
    private lateinit var meshes: GlMeshes

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var aspectRatio: Float = 1f
    private var lastFrameNs: Long = 0L
    private var hudAccumulator: Float = 0f
    private var animClock: Float = 0f
    private var cameraBlend: Float = if (simulation.cameraMode == CameraMode.MODE_3D) 1f else 0f
    private var smoothedFps: Float = 60f

    private var currentEyeX = 0f
    private var currentEyeY = 14f
    private var currentEyeZ = -18f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        shader = GlShaderProgram()
        meshes = GlMeshes()
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearDepthf(1.0f)
        lastFrameNs = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / height.coerceAtLeast(1).toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val rawDt = ((now - lastFrameNs) / 1_000_000_000f)
        val dt = rawDt.coerceIn(0.001f, 0.05f)
        lastFrameNs = now
        animClock += dt

        if (rawDt > 0.0005f) {
            val instantFps = (1f / rawDt).coerceIn(10f, 120f)
            smoothedFps = smoothedFps * 0.9f + instantFps * 0.1f
            simulation.currentFps = smoothedFps.toInt()
        }

        simulation.step(dt)
        hudAccumulator += dt
        if (hudAccumulator >= 0.06f) {
            hudAccumulator = 0f
            onHudUpdate()
        }

        val targetBlend = if (simulation.cameraMode == CameraMode.MODE_3D) 1f else 0f
        cameraBlend += (targetBlend - cameraBlend) * (dt * 7.5f).coerceAtMost(1f)

        val daylight = simulation.currentDaylightFactor()
        val baseSky = simulation.world.worldId.skyColor
        val skyR = (baseSky[0] * daylight).coerceIn(0.02f, 0.85f)
        val skyG = (baseSky[1] * daylight).coerceIn(0.02f, 0.85f)
        val skyB = (baseSky[2] * daylight).coerceIn(0.03f, 0.92f)

        GLES20.glClearColor(skyR, skyG, skyB, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        shader.use()
        setupCameraMatrices()

        GLES20.glUniform3f(shader.uFogColor, skyR, skyG, skyB)
        val qualityFactor = when (simulation.graphicsQuality) {
            0 -> 1.35f
            1 -> 1.0f
            else -> 0.72f
        }
        val fogDensity = (0.0018f + cameraBlend * 0.0046f) * qualityFactor
        GLES20.glUniform1f(shader.uFogDensity, fogDensity)
        GLES20.glUniform3f(shader.uCameraPos, currentEyeX, currentEyeY, currentEyeZ)

        // Take thread-safe copies of dynamic entity lists
        val vehiclesCopy: List<VehicleEntity>
        val monstersCopy: List<MonsterEntity>
        val projectilesCopy: List<ProjectileEntity>
        val suppliesCopy: List<SupplyDropEntity>
        val turretsCopy: List<SentryTurretEntity>
        val shockwavesCopy: List<ShockwaveEffect>
        synchronized(simulation) {
            vehiclesCopy = simulation.vehicles.toList()
            monstersCopy = simulation.monsters.toList()
            projectilesCopy = simulation.projectiles.toList()
            suppliesCopy = simulation.supplies.toList()
            turretsCopy = simulation.turrets.toList()
            shockwavesCopy = simulation.shockwaves.toList()
        }

        drawGroundAndRoads(daylight)
        drawObstacles()
        drawSupplies(suppliesCopy)
        drawTurrets(turretsCopy)
        drawVehicles(vehiclesCopy)
        if (simulation.activeVehicleId == null) {
            drawPlayerOnFoot()
        }
        drawMonsters(monstersCopy)
        drawProjectilesAndEffects(projectilesCopy, shockwavesCopy)
        if (simulation.weatherEnabled) {
            drawWeatherParticles()
        }
    }

    private fun setupCameraMatrices() {
        val px = simulation.playerX
        val pz = simulation.playerZ
        val yaw = simulation.cameraYaw
        val inCar = simulation.activeVehicleId != null
        val carSpeed = simulation.activeVehicleId?.let { id ->
            simulation.vehicles.firstOrNull { it.id == id }?.speed ?: 0f
        } ?: 0f

        val dist3D = if (inCar) 16.5f + abs(carSpeed) * 0.14f else 11.5f
        val height3D = if (inCar) 8.2f else 6.8f
        val lookAhead3D = if (inCar) 6.5f else 4.2f

        val eye3DX = px - sin(yaw) * dist3D
        val eye3DZ = pz - cos(yaw) * dist3D
        val center3DX = px + sin(yaw) * lookAhead3D
        val center3DZ = pz + cos(yaw) * lookAhead3D

        val height2D = if (inCar) 64f else 50f
        val eye2DX = px
        val eye2DZ = pz + 0.35f

        currentEyeX = lerp(eye2DX, eye3DX, cameraBlend)
        currentEyeY = lerp(height2D, height3D, cameraBlend)
        currentEyeZ = lerp(eye2DZ, eye3DZ, cameraBlend)

        val centerX = lerp(px, center3DX, cameraBlend)
        val centerY = lerp(0f, 1.3f, cameraBlend)
        val centerZ = lerp(pz, center3DZ, cameraBlend)

        val fov = lerp(46f, 60f, cameraBlend)
        Matrix.perspectiveM(projectionMatrix, 0, fov, aspectRatio, 0.8f, 260f)
        Matrix.setLookAtM(
            viewMatrix, 0,
            currentEyeX, currentEyeY, currentEyeZ,
            centerX, centerY, centerZ,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    private fun drawGroundAndRoads(daylight: Float) {
        val world = simulation.world
        val baseGround = world.worldId.groundColor
        val ground = floatArrayOf(
            (baseGround[0] * daylight).coerceAtMost(1f),
            (baseGround[1] * daylight).coerceAtMost(1f),
            (baseGround[2] * daylight).coerceAtMost(1f)
        )
        val extent = world.halfExtent * 2.1f

        drawQuad(0f, 0f, 0f, extent, extent, ground, emissive = 0f)

        val roadCol = world.worldId.roadColor
        val accent = world.worldId.obstacleAccentColor
        for (road in world.roads) {
            drawQuad(road.x, 0.03f, road.z, road.halfW * 2f, road.halfD * 2f, roadCol, emissive = 0f)
            if (road.isHorizontal) {
                drawQuad(road.x, 0.05f, road.z, road.halfW * 1.95f, 0.45f, accent, emissive = 0.15f)
            } else {
                drawQuad(road.x, 0.05f, road.z, 0.45f, road.halfD * 1.95f, accent, emissive = 0.15f)
            }
        }
    }

    private fun drawObstacles() {
        val px = simulation.playerX
        val pz = simulation.playerZ
        val prim = simulation.world.worldId.obstaclePrimaryColor
        val acc = simulation.world.worldId.obstacleAccentColor
        val cullDist = when (simulation.graphicsQuality) {
            0 -> 95f
            1 -> 125f
            else -> 155f
        }

        for (o in simulation.world.obstacles) {
            if (abs(o.x - px) > cullDist || abs(o.z - pz) > cullDist) continue
            drawSingleObstacle(o, prim, acc)
        }
    }

    private fun drawSingleObstacle(o: WorldObstacle, prim: FloatArray, acc: FloatArray) {
        val shade = 0.85f + (o.variant * 0.08f)
        val bodyCol = floatArrayOf(
            (prim[0] * shade).coerceAtMost(1f),
            (prim[1] * shade).coerceAtMost(1f),
            (prim[2] * shade).coerceAtMost(1f)
        )
        when (o.kind) {
            ObstacleKind.SKYSCRAPER -> {
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 2f, o.height, o.halfD * 2f, 0f, bodyCol)
                drawBox(o.x, o.height - 0.5f, o.z, o.halfW * 2.04f, 0.7f, o.halfD * 2.04f, 0f, acc, emissive = 0.55f)
            }
            ObstacleKind.NEON_BILLBOARD -> {
                drawBox(o.x, o.height * 0.5f, o.z, 0.7f, o.height, 0.7f, 0f, bodyCol)
                drawBox(o.x, o.height * 0.72f, o.z, o.halfW * 2f, 2.8f, 0.9f, 0f, acc, emissive = 0.75f)
            }
            ObstacleKind.PINE_TREE -> {
                drawBox(o.x, 1.8f, o.z, 0.9f, 3.6f, 0.9f, 0f, acc)
                drawBox(o.x, 4.8f, o.z, o.halfW * 1.9f, 3.2f, o.halfD * 1.9f, 0.3f, bodyCol)
                drawBox(o.x, 7.6f, o.z, o.halfW * 1.25f, 3.0f, o.halfD * 1.25f, 0.7f, bodyCol)
            }
            ObstacleKind.CABIN -> {
                drawBox(o.x, 2.0f, o.z, o.halfW * 2f, 4.0f, o.halfD * 2f, 0f, acc)
                drawBox(o.x, 4.5f, o.z, o.halfW * 2.2f, 1.1f, o.halfD * 2.2f, 0f, bodyCol)
            }
            ObstacleKind.CANYON_MESA -> {
                drawBox(o.x, o.height * 0.35f, o.z, o.halfW * 2f, o.height * 0.7f, o.halfD * 2f, 0f, bodyCol)
                drawBox(o.x, o.height * 0.85f, o.z, o.halfW * 1.4f, o.height * 0.35f, o.halfD * 1.4f, 0f, acc)
            }
            ObstacleKind.RUIN_PILLAR -> {
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 1.6f, o.height, o.halfD * 1.6f, 0f, acc)
                drawBox(o.x, 0.6f, o.z, o.halfW * 2.1f, 1.2f, o.halfD * 2.1f, 0f, bodyCol)
            }
            ObstacleKind.BUNKER -> {
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 2f, o.height, o.halfD * 2f, 0f, bodyCol)
                drawBox(o.x, o.height * 0.65f, o.z, o.halfW * 2.05f, 0.6f, o.halfD * 2.05f, 0f, acc, emissive = 0.4f)
            }
            ObstacleKind.RADAR_TOWER -> {
                drawBox(o.x, o.height * 0.45f, o.z, 2.0f, o.height * 0.9f, 2.0f, 0f, bodyCol)
                drawBox(o.x, o.height * 0.94f, o.z, 4.6f, 1.2f, 0.8f, animClock * 1.8f, acc, emissive = 0.5f)
            }
            ObstacleKind.SUBWAY_PILLAR -> {
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 1.8f, o.height, o.halfD * 1.8f, 0f, bodyCol)
                drawBox(o.x, 2.2f, o.z, o.halfW * 1.85f, 0.7f, o.halfD * 1.85f, 0f, acc, emissive = 0.5f)
            }
            ObstacleKind.TRAIN_CAR -> {
                drawBox(o.x, 2.0f, o.z, o.halfW * 2f, 3.6f, o.halfD * 2f, 0f, bodyCol)
                drawBox(o.x, 2.2f, o.z, o.halfW * 2.03f, 0.55f, o.halfD * 2.03f, 0f, acc, emissive = 0.35f)
            }
            ObstacleKind.CONTAINER -> {
                val containerColors = arrayOf(
                    floatArrayOf(0.76f, 0.24f, 0.18f),
                    floatArrayOf(0.18f, 0.46f, 0.68f),
                    floatArrayOf(0.78f, 0.56f, 0.16f),
                    floatArrayOf(0.22f, 0.54f, 0.36f)
                )
                val c = containerColors[o.variant % containerColors.size]
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 2f, o.height, o.halfD * 2f, 0f, c)
            }
            ObstacleKind.FACTORY_CHIMNEY -> {
                drawBox(o.x, o.height * 0.5f, o.z, o.halfW * 1.4f, o.height, o.halfD * 1.4f, 0f, bodyCol)
                drawBox(o.x, o.height - 0.6f, o.z, o.halfW * 1.55f, 1.0f, o.halfD * 1.55f, 0f, acc, emissive = 0.65f)
            }
        }
    }

    private fun drawTurrets(turrets: List<SentryTurretEntity>) {
        val baseCol = floatArrayOf(0.22f, 0.26f, 0.32f)
        val barrelCol = floatArrayOf(1.0f, 0.45f, 0.16f)
        for (t in turrets) {
            drawBox(t.x, 0.55f, t.z, 1.1f, 1.1f, 1.1f, 0f, baseCol)
            drawBox(t.x, 1.35f, t.z, 0.75f, 0.50f, 0.95f, t.yaw, barrelCol, emissive = 0.35f)
            drawBox(
                t.x + sin(t.yaw) * 0.65f,
                1.38f,
                t.z + cos(t.yaw) * 0.65f,
                0.24f,
                0.24f,
                1.1f,
                t.yaw,
                barrelCol,
                emissive = 0.65f
            )
        }
    }

    private fun drawVehicles(vehicles: List<VehicleEntity>) {
        val paint = simulation.carPaint
        val carBaseColor = if (paint.useWorldDefault) {
            simulation.world.worldId.vehicleColor
        } else {
            paint.chassisColor
        }
        val trimColor = paint.trimColor
        val headlightColor = paint.headlightColor
        val darkWheel = floatArrayOf(0.09f, 0.09f, 0.11f)
        val glassColor = floatArrayOf(0.12f, 0.22f, 0.30f)

        for (car in vehicles) {
            if (car.hp <= 0f) continue
            val isActive = car.id == simulation.activeVehicleId
            val hullCol = if (isActive) carBaseColor else floatArrayOf(carBaseColor[0] * 0.82f, carBaseColor[1] * 0.82f, carBaseColor[2] * 0.82f)

            drawBox(car.x, 0.9f, car.z, 2.4f, 1.0f, 4.4f, car.yaw, hullCol)
            // Side neon/paint trim stripe
            drawBox(car.x, 1.05f, car.z, 2.46f, 0.22f, 4.1f, car.yaw, trimColor, emissive = 0.45f)
            drawBox(car.x - sin(car.yaw) * 0.25f, 1.72f, car.z - cos(car.yaw) * 0.25f, 2.0f, 0.8f, 2.2f, car.yaw, glassColor)

            if (isActive) {
                val skin = simulation.skin
                drawBox(car.x, 2.3f, car.z, 0.55f, 0.35f, 0.8f, car.yaw, skin.primaryColor, emissive = 0.2f)
                drawBox(
                    car.x + sin(car.yaw) * 0.55f,
                    2.32f,
                    car.z + cos(car.yaw) * 0.55f,
                    0.22f, 0.22f, 1.25f,
                    car.yaw,
                    skin.accentColor,
                    emissive = 0.45f
                )
                if (simulation.shieldActiveTimer > 0f) {
                    drawQuad(car.x, 0.18f, car.z, 5.6f, 5.6f, floatArrayOf(0.98f, 0.85f, 0.20f), emissive = 0.9f, alpha = 0.45f)
                }
            }

            val fx = sin(car.yaw) * 2.25f
            val fz = cos(car.yaw) * 2.25f
            val rx = cos(car.yaw) * 0.78f
            val rz = -sin(car.yaw) * 0.78f
            drawBox(car.x + fx + rx, 0.9f, car.z + fz + rz, 0.38f, 0.28f, 0.16f, car.yaw, headlightColor, emissive = 0.9f)
            drawBox(car.x + fx - rx, 0.9f, car.z + fz - rz, 0.38f, 0.28f, 0.16f, car.yaw, headlightColor, emissive = 0.9f)

            val wx = sin(car.yaw) * 1.4f
            val wz = cos(car.yaw) * 1.4f
            val wheelOutX = cos(car.yaw) * 1.28f
            val wheelOutZ = -sin(car.yaw) * 1.28f
            drawBox(car.x + wx + wheelOutX, 0.45f, car.z + wz + wheelOutZ, 0.38f, 0.85f, 0.85f, car.yaw, darkWheel)
            drawBox(car.x + wx - wheelOutX, 0.45f, car.z + wz - wheelOutZ, 0.38f, 0.85f, 0.85f, car.yaw, darkWheel)
            drawBox(car.x - wx + wheelOutX, 0.45f, car.z - wz + wheelOutZ, 0.38f, 0.85f, 0.85f, car.yaw, darkWheel)
            drawBox(car.x - wx - wheelOutX, 0.45f, car.z - wz - wheelOutZ, 0.38f, 0.85f, 0.85f, car.yaw, darkWheel)
        }
    }

    private fun drawPlayerOnFoot() {
        val px = simulation.playerX
        val pz = simulation.playerZ
        val yaw = simulation.playerYaw
        val flash = simulation.playerHitFlash
        val outfit = simulation.outfit
        val armorCol = floatArrayOf(
            (outfit.armorColor[0] + flash * 0.7f).coerceAtMost(1f),
            outfit.armorColor[1],
            outfit.armorColor[2]
        )

        // Torso & legs using equipped Locker Outfit!
        drawBox(px, 0.95f, pz, 0.95f, 1.35f, 0.65f, yaw, armorCol)
        drawBox(px, 1.95f, pz, 0.62f, 0.62f, 0.62f, yaw, outfit.helmetColor)
        drawBox(px + sin(yaw) * 0.28f, 1.98f, pz + cos(yaw) * 0.28f, 0.46f, 0.20f, 0.16f, yaw, outfit.visorColor, emissive = 0.85f)

        if (simulation.shieldActiveTimer > 0f) {
            drawQuad(px, 0.16f, pz, 3.2f, 3.2f, floatArrayOf(0.98f, 0.85f, 0.20f), emissive = 0.9f, alpha = 0.50f)
        }

        val skin = simulation.skin
        val gunLen = when (simulation.weapon) {
            WeaponId.PISTOL -> 0.75f
            WeaponId.SHOTGUN -> 1.25f
            WeaponId.ASSAULT_RIFLE -> 1.35f
            WeaponId.SNIPER -> 1.85f
            WeaponId.MINIGUN -> 1.55f
            WeaponId.PLASMA_RIFLE -> 1.40f
        }
        val gunWidth = if (simulation.weapon == WeaponId.MINIGUN) 0.40f else 0.22f
        val sideX = -cos(yaw) * 0.42f
        val sideZ = sin(yaw) * 0.42f
        val fwdX = sin(yaw) * (0.45f + gunLen * 0.35f)
        val fwdZ = cos(yaw) * (0.45f + gunLen * 0.35f)

        drawBox(
            px + sideX + fwdX,
            1.28f,
            pz + sideZ + fwdZ,
            gunWidth,
            0.26f,
            gunLen,
            yaw,
            skin.primaryColor,
            emissive = 0.15f
        )
        drawBox(
            px + sideX + fwdX * 1.15f,
            1.34f,
            pz + sideZ + fwdZ * 1.15f,
            gunWidth * 0.75f,
            0.14f,
            gunLen * 0.65f,
            yaw,
            skin.accentColor,
            emissive = 0.55f
        )

        if (simulation.muzzleFlashTimer > 0.1f) {
            val mx = px + sideX + sin(yaw) * (0.55f + gunLen)
            val mz = pz + sideZ + cos(yaw) * (0.55f + gunLen)
            drawBox(mx, 1.30f, mz, 0.42f, 0.42f, 0.42f, yaw, skin.tracerColor, emissive = 1.0f)
        }
    }

    private fun drawMonsters(monsters: List<MonsterEntity>) {
        for (m in monsters) {
            drawSingleMonster(m)
        }
    }

    private fun drawSingleMonster(m: MonsterEntity) {
        val base = m.type.color
        val acc = m.type.accentColor
        val flash = m.hitFlash
        val slowed = m.slowTimer > 0f
        val bodyCol = floatArrayOf(
            (base[0] + flash * 0.7f).coerceAtMost(1f),
            (base[1] + flash * 0.7f + if (slowed) 0.25f else 0f).coerceAtMost(1f),
            (base[2] + flash * 0.7f + if (slowed) 0.45f else 0f).coerceAtMost(1f)
        )

        val w = m.type.radius * 1.45f
        val h = m.type.height
        val bob = sin(animClock * 9f + m.id) * 0.12f

        drawBox(m.x, h * 0.45f + bob, m.z, w, h * 0.72f, w * 0.85f, m.yaw, bodyCol)
        val headY = h * 0.86f + bob
        drawBox(
            m.x + sin(m.yaw) * (w * 0.35f),
            headY,
            m.z + cos(m.yaw) * (w * 0.35f),
            w * 0.6f,
            h * 0.16f,
            w * 0.35f,
            m.yaw,
            acc,
            emissive = 0.75f
        )

        if (m.type == MonsterType.BOSS_TITAN) {
            drawBox(m.x, h * 0.70f + bob, m.z, w * 1.38f, 1.2f, w * 0.65f, m.yaw, acc, emissive = 0.4f)
            drawBox(m.x, h * 1.08f + bob, m.z, w * 0.55f, 0.85f, w * 0.55f, animClock * 2f, acc, emissive = 0.9f)
        }
    }

    private fun drawSupplies(supplies: List<SupplyDropEntity>) {
        for (s in supplies) {
            val col = when (s.kind) {
                SupplyKind.MEDKIT -> floatArrayOf(0.18f, 0.92f, 0.42f)
                SupplyKind.REPAIR_KIT -> floatArrayOf(0.20f, 0.82f, 0.98f)
                SupplyKind.AMMO_CRATE -> floatArrayOf(0.98f, 0.55f, 0.15f)
                SupplyKind.COIN_CACHE -> floatArrayOf(0.98f, 0.84f, 0.14f)
            }
            val bobY = 0.75f + sin(s.pulsePhase) * 0.25f
            drawBox(s.x, bobY, s.z, 1.2f, 1.0f, 1.2f, s.pulsePhase * 0.5f, col, emissive = 0.65f)
            drawBox(s.x, 4.2f, s.z, 0.22f, 7.0f, 0.22f, 0f, col, emissive = 0.85f)
        }
    }

    private fun drawProjectilesAndEffects(
        projectiles: List<ProjectileEntity>,
        shockwaves: List<ShockwaveEffect>
    ) {
        for (p in projectiles) {
            val size = if (p.fromMonster) 0.55f else 0.32f
            val yaw = atan2(p.vx, p.vz)
            drawBox(p.x, p.y, p.z, size, size, size * 2.4f, yaw, p.color, emissive = 0.95f)
        }
        for (sw in shockwaves) {
            drawQuad(sw.x, 0.14f, sw.z, sw.radius * 2f, sw.radius * 2f, sw.color, emissive = 0.85f, alpha = sw.alpha * 0.55f)
        }
    }

    private fun drawWeatherParticles() {
        val weather = simulation.world.worldId.defaultWeather
        if (weather == WeatherKind.CLEAR) return

        val px = simulation.playerX
        val pz = simulation.playerZ
        val col = when (weather) {
            WeatherKind.ACID_RAIN -> floatArrayOf(0.30f, 0.92f, 0.75f)
            WeatherKind.ASH_STORM -> floatArrayOf(0.95f, 0.55f, 0.22f)
            WeatherKind.BLIZZARD -> floatArrayOf(0.90f, 0.95f, 1.0f)
            WeatherKind.CLEAR -> return
        }
        val sy = if (weather == WeatherKind.ACID_RAIN) 1.35f else 0.28f
        val sx = if (weather == WeatherKind.ACID_RAIN) 0.08f else 0.24f

        for (i in 0 until 28) {
            val seed = i * 37.3f
            val ox = ((sin(seed) * 26f) + animClock * 2.2f) % 26f
            val oz = ((cos(seed * 1.3f) * 26f) + animClock * 1.5f) % 26f
            val fallSpeed = if (weather == WeatherKind.ACID_RAIN) 18f else 7.5f
            val py = 14f - ((animClock * fallSpeed + i * 1.9f) % 13.5f)
            drawBox(px + ox, py, pz + oz, sx, sy, sx, 0f, col, emissive = 0.7f)
        }
    }

    private fun drawBox(
        x: Float,
        y: Float,
        z: Float,
        sx: Float,
        sy: Float,
        sz: Float,
        yawRad: Float,
        rgb: FloatArray,
        emissive: Float = 0f
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        if (yawRad != 0f) {
            Matrix.rotateM(modelMatrix, 0, Math.toDegrees(yawRad.toDouble()).toFloat(), 0f, 1f, 0f)
        }
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniform4f(shader.uColor, rgb[0], rgb[1], rgb[2], 1f)
        GLES20.glUniform1f(shader.uEmissive, emissive)

        GLES20.glEnableVertexAttribArray(shader.aPosition)
        GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, 0, meshes.cubeVertexBuffer)
        GLES20.glEnableVertexAttribArray(shader.aNormal)
        GLES20.glVertexAttribPointer(shader.aNormal, 3, GLES20.GL_FLOAT, false, 0, meshes.cubeNormalBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, meshes.cubeVertexCount)
    }

    private fun drawQuad(
        x: Float,
        y: Float,
        z: Float,
        sx: Float,
        sz: Float,
        rgb: FloatArray,
        emissive: Float = 0f,
        alpha: Float = 1f
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.scaleM(modelMatrix, 0, sx, 1f, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniform4f(shader.uColor, rgb[0], rgb[1], rgb[2], alpha)
        GLES20.glUniform1f(shader.uEmissive, emissive)

        GLES20.glEnableVertexAttribArray(shader.aPosition)
        GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, 0, meshes.quadVertexBuffer)
        GLES20.glEnableVertexAttribArray(shader.aNormal)
        GLES20.glVertexAttribPointer(shader.aNormal, 3, GLES20.GL_FLOAT, false, 0, meshes.quadNormalBuffer)

        if (alpha < 1f) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, meshes.quadVertexCount)
        if (alpha < 1f) {
            GLES20.glDisable(GLES20.GL_BLEND)
        }
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
