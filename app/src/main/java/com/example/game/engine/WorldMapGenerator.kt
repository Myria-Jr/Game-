package com.example.game.engine

import com.example.game.model.ObstacleKind
import com.example.game.model.SupplyKind
import com.example.game.model.WorldId
import kotlin.math.abs
import kotlin.random.Random

data class WorldObstacle(
    val id: Int,
    val x: Float,
    val z: Float,
    val halfW: Float,
    val halfD: Float,
    val height: Float,
    val kind: ObstacleKind,
    val variant: Int
)

data class RoadStrip(
    val x: Float,
    val z: Float,
    val halfW: Float,
    val halfD: Float,
    val isHorizontal: Boolean
)

data class SpawnVehicleSpec(
    val x: Float,
    val z: Float,
    val yaw: Float
)

data class GeneratedWorld(
    val worldId: WorldId,
    val halfExtent: Float,
    val roads: List<RoadStrip>,
    val obstacles: List<WorldObstacle>,
    val initialVehicles: List<SpawnVehicleSpec>,
    val supplyAnchors: List<Pair<Float, Float>>
)

object WorldMapGenerator {
    const val WORLD_HALF_EXTENT = 195f

    fun generate(worldId: WorldId): GeneratedWorld {
        val rng = Random(worldId.ordinal * 9173 + 42)
        val roads = buildRoadNetwork(worldId)
        val obstacles = mutableListOf<WorldObstacle>()
        var nextId = 1

        // Perimeter boundary barriers / cliffs / walls
        val wallStep = 26f
        var pos = -WORLD_HALF_EXTENT + 10f
        while (pos <= WORLD_HALF_EXTENT - 10f) {
            val borderKind = borderObstacleKind(worldId)
            obstacles.add(WorldObstacle(nextId++, pos, -WORLD_HALF_EXTENT + 3f, 12f, 3f, 8f, borderKind, 0))
            obstacles.add(WorldObstacle(nextId++, pos, WORLD_HALF_EXTENT - 3f, 12f, 3f, 8f, borderKind, 1))
            obstacles.add(WorldObstacle(nextId++, -WORLD_HALF_EXTENT + 3f, pos, 3f, 12f, 8f, borderKind, 2))
            obstacles.add(WorldObstacle(nextId++, WORLD_HALF_EXTENT - 3f, pos, 3f, 12f, 8f, borderKind, 3))
            pos += wallStep
        }

        // Biome-specific landmarks & cover placed outside roads and center spawn plaza
        val targetCount = when (worldId) {
            WorldId.CITY -> 95
            WorldId.FOREST -> 130
            WorldId.DESERT_CANYON -> 85
            WorldId.SNOW_BASE -> 80
            WorldId.SUBWAY_TUNNEL -> 105
            WorldId.INDUSTRIAL_PORT -> 95
        }

        var attempts = 0
        while (obstacles.size < targetCount + 60 && attempts < 1400) {
            attempts++
            val ox = rng.nextFloat() * (WORLD_HALF_EXTENT * 1.82f) - (WORLD_HALF_EXTENT * 0.91f)
            val oz = rng.nextFloat() * (WORLD_HALF_EXTENT * 1.82f) - (WORLD_HALF_EXTENT * 0.91f)

            // Keep central spawn plaza open
            if (abs(ox) < 20f && abs(oz) < 20f) continue

            val (kind, hw, hd, h) = pickObstacleSpec(worldId, rng)

            // Keep road lanes drivable
            if (overlapsRoad(ox, oz, hw + 2.5f, hd + 2.5f, roads)) continue

            // Avoid overlapping other obstacles
            val collides = obstacles.any { other ->
                abs(ox - other.x) < (hw + other.halfW + 3.2f) &&
                    abs(oz - other.z) < (hd + other.halfD + 3.2f)
            }
            if (collides) continue

            obstacles.add(
                WorldObstacle(
                    id = nextId++,
                    x = ox,
                    z = oz,
                    halfW = hw,
                    halfD = hd,
                    height = h,
                    kind = kind,
                    variant = rng.nextInt(4)
                )
            )
        }

        val vehicles = listOf(
            SpawnVehicleSpec(x = 9.5f, z = 4.0f, yaw = 0f),
            SpawnVehicleSpec(x = -80f, z = 0f, yaw = (Math.PI / 2).toFloat()),
            SpawnVehicleSpec(x = 80f, z = -80f, yaw = Math.PI.toFloat()),
            SpawnVehicleSpec(x = -80f, z = 80f, yaw = 0f),
            SpawnVehicleSpec(x = 80f, z = 80f, yaw = -(Math.PI / 2).toFloat())
        )

        val supplyAnchors = listOf(
            0f to -45f,
            45f to 0f,
            0f to 45f,
            -45f to 0f,
            -80f to -80f,
            80f to -80f,
            -80f to 80f,
            80f to 80f,
            0f to -130f,
            130f to 0f,
            0f to 130f,
            -130f to 0f
        )

        return GeneratedWorld(
            worldId = worldId,
            halfExtent = WORLD_HALF_EXTENT,
            roads = roads,
            obstacles = obstacles,
            initialVehicles = vehicles,
            supplyAnchors = supplyAnchors
        )
    }

    private fun buildRoadNetwork(worldId: WorldId): List<RoadStrip> {
        val roadHalfWidth = when (worldId) {
            WorldId.CITY -> 8.5f
            WorldId.FOREST -> 6.5f
            WorldId.DESERT_CANYON -> 7.5f
            WorldId.SNOW_BASE -> 9.0f
            WorldId.SUBWAY_TUNNEL -> 8.0f
            WorldId.INDUSTRIAL_PORT -> 8.5f
        }
        val span = WORLD_HALF_EXTENT - 6f
        val list = mutableListOf<RoadStrip>()
        val coords = listOf(-140f, -80f, 0f, 80f, 140f)
        for (c in coords) {
            list.add(RoadStrip(x = 0f, z = c, halfW = span, halfD = roadHalfWidth, isHorizontal = true))
            list.add(RoadStrip(x = c, z = 0f, halfW = roadHalfWidth, halfD = span, isHorizontal = false))
        }
        return list
    }

    private fun overlapsRoad(
        x: Float,
        z: Float,
        hw: Float,
        hd: Float,
        roads: List<RoadStrip>
    ): Boolean {
        return roads.any { r ->
            abs(x - r.x) < (hw + r.halfW) && abs(z - r.z) < (hd + r.halfD)
        }
    }

    private fun borderObstacleKind(worldId: WorldId): ObstacleKind = when (worldId) {
        WorldId.CITY -> ObstacleKind.SKYSCRAPER
        WorldId.FOREST -> ObstacleKind.PINE_TREE
        WorldId.DESERT_CANYON -> ObstacleKind.CANYON_MESA
        WorldId.SNOW_BASE -> ObstacleKind.BUNKER
        WorldId.SUBWAY_TUNNEL -> ObstacleKind.SUBWAY_PILLAR
        WorldId.INDUSTRIAL_PORT -> ObstacleKind.CONTAINER
    }

    private fun pickObstacleSpec(worldId: WorldId, rng: Random): QuadSpec {
        return when (worldId) {
            WorldId.CITY -> {
                if (rng.nextFloat() < 0.78f) {
                    val w = 6f + rng.nextFloat() * 7f
                    val d = 6f + rng.nextFloat() * 7f
                    val h = 12f + rng.nextFloat() * 24f
                    QuadSpec(ObstacleKind.SKYSCRAPER, w, d, h)
                } else {
                    QuadSpec(ObstacleKind.NEON_BILLBOARD, 3.8f, 1.4f, 8.5f)
                }
            }
            WorldId.FOREST -> {
                if (rng.nextFloat() < 0.85f) {
                    val r = 2.0f + rng.nextFloat() * 1.8f
                    QuadSpec(ObstacleKind.PINE_TREE, r, r, 9f + rng.nextFloat() * 6f)
                } else {
                    QuadSpec(ObstacleKind.CABIN, 5.5f, 4.5f, 5.2f)
                }
            }
            WorldId.DESERT_CANYON -> {
                if (rng.nextFloat() < 0.65f) {
                    val w = 5.5f + rng.nextFloat() * 7f
                    val d = 5.5f + rng.nextFloat() * 7f
                    QuadSpec(ObstacleKind.CANYON_MESA, w, d, 7f + rng.nextFloat() * 12f)
                } else {
                    QuadSpec(ObstacleKind.RUIN_PILLAR, 2.2f, 2.2f, 7f + rng.nextFloat() * 4f)
                }
            }
            WorldId.SNOW_BASE -> {
                if (rng.nextFloat() < 0.72f) {
                    QuadSpec(ObstacleKind.BUNKER, 5.5f + rng.nextFloat() * 4f, 4.5f + rng.nextFloat() * 3f, 4.8f)
                } else {
                    QuadSpec(ObstacleKind.RADAR_TOWER, 2.8f, 2.8f, 14f)
                }
            }
            WorldId.SUBWAY_TUNNEL -> {
                if (rng.nextFloat() < 0.65f) {
                    QuadSpec(ObstacleKind.SUBWAY_PILLAR, 2.2f, 2.2f, 11f)
                } else {
                    val horiz = rng.nextBoolean()
                    if (horiz) QuadSpec(ObstacleKind.TRAIN_CAR, 9.5f, 2.4f, 4.2f)
                    else QuadSpec(ObstacleKind.TRAIN_CAR, 2.4f, 9.5f, 4.2f)
                }
            }
            WorldId.INDUSTRIAL_PORT -> {
                if (rng.nextFloat() < 0.75f) {
                    val horiz = rng.nextBoolean()
                    if (horiz) QuadSpec(ObstacleKind.CONTAINER, 6.2f, 2.4f, 3.8f + rng.nextInt(2) * 3.2f)
                    else QuadSpec(ObstacleKind.CONTAINER, 2.4f, 6.2f, 3.8f + rng.nextInt(2) * 3.2f)
                } else {
                    QuadSpec(ObstacleKind.FACTORY_CHIMNEY, 3.2f, 3.2f, 18f)
                }
            }
        }
    }

    private data class QuadSpec(
        val kind: ObstacleKind,
        val hw: Float,
        val hd: Float,
        val h: Float
    )
}
