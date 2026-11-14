package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.engine.BlipKind
import com.example.game.engine.HudSnapshot
import com.example.game.model.CameraMode
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ScrapGold
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceVariant
import com.example.ui.theme.ToxicGreen
import kotlin.math.hypot

@Composable
fun TopHudBar(
    hud: HudSnapshot,
    onToggleCameraMode: () -> Unit,
    onPauseClick: () -> Unit
) {
    val s = LocalStrings.current
    val lang = s.language

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left: HP, Car HP/Ammo, Wave, FPS & Coins
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = ObsidianBg.copy(alpha = 0.80f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (hud.shieldActiveRemaining > 0f) ScrapGold else TacticalBorder)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (hud.shieldActiveRemaining > 0f) {
                                "SHIELD ${hud.shieldActiveRemaining.toInt()}s"
                            } else {
                                "HP ${hud.hp.toInt()}/${hud.maxHp.toInt()}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hud.shieldActiveRemaining > 0f) ScrapGold else Color.White
                        )
                        LinearProgressIndicator(
                            progress = { (hud.hp / hud.maxHp).coerceIn(0f, 1f) },
                            color = when {
                                hud.shieldActiveRemaining > 0f -> ScrapGold
                                hud.hp < hud.maxHp * 0.3f -> AlertRed
                                else -> ToxicGreen
                            },
                            trackColor = TacticalSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }

                    if (hud.inVehicle) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CAR ${hud.vehicleHp.toInt()} • ${hud.speedKmh} km/h",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                            LinearProgressIndicator(
                                progress = { (hud.vehicleHp / hud.vehicleMaxHp).coerceIn(0f, 1f) },
                                color = CyberCyan,
                                trackColor = TacticalSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (hud.isReloading) {
                                    s.hudReloading
                                } else {
                                    "${hud.weapon.title(lang)}: ${hud.ammo}/${hud.maxAmmo}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(hud.skin.accentHex)
                            )
                            Text(
                                text = "${hud.sessionCoins} C",
                                style = MaterialTheme.typography.labelMedium,
                                color = ScrapGold
                            )
                        }
                    }
                }
            }

            // Wave pill + 2D/3D Toggle + Pause + optional FPS/Weather badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ObsidianBg.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TacticalBorder)
                ) {
                    Text(
                        text = if (hud.waveBreakSeconds > 0) {
                            "${s.hudWave} ${hud.wave} • ${s.hudBreak} ${hud.waveBreakSeconds}s"
                        } else {
                            "${s.hudWave} ${hud.wave} • ${hud.monstersRemaining} ${s.hudEnemies}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                    )
                }

                // Instant 2D / 3D Switcher
                Surface(
                    color = CyberCyan.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clickable(onClick = onToggleCameraMode)
                        .testTag("camera_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ViewInAr,
                            contentDescription = "2D/3D",
                            tint = ObsidianBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (hud.cameraMode == CameraMode.MODE_3D) "3D" else "2D",
                            style = MaterialTheme.typography.labelLarge,
                            color = ObsidianBg
                        )
                    }
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBg.copy(alpha = 0.80f))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            if (hud.showFps) {
                Text(
                    text = "${hud.fps} FPS • ${hud.timeOfDayLabel} • ${hud.weather.title(lang)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Live Radar Minimap
        RadarMinimap(
            hud = hud,
            modifier = Modifier.size(92.dp)
        )
    }
}

@Composable
fun BossHealthBar(name: String, hp: Float, maxHp: Float) {
    Surface(
        color = ObsidianBg.copy(alpha = 0.88f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, AlertRed),
        modifier = Modifier.fillMaxWidth(0.86f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "BOSS: ${name.uppercase()} (${hp.toInt()} HP)",
                style = MaterialTheme.typography.labelLarge,
                color = ScrapGold
            )
            LinearProgressIndicator(
                progress = { (hp / maxHp).coerceIn(0f, 1f) },
                color = AlertRed,
                trackColor = TacticalSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun RadarMinimap(hud: HudSnapshot, modifier: Modifier = Modifier) {
    val accent = Color(hud.worldId.accentColorHex)
    Surface(
        color = ObsidianBg.copy(alpha = 0.84f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f)),
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.padding(6.dp)) {
            val w = size.width
            val h = size.height
            val roadPositions = listOf(0.14f, 0.29f, 0.50f, 0.71f, 0.86f)
            for (p in roadPositions) {
                drawLine(
                    color = TacticalBorder.copy(alpha = 0.75f),
                    start = Offset(0f, h * p),
                    end = Offset(w, h * p),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = TacticalBorder.copy(alpha = 0.75f),
                    start = Offset(w * p, 0f),
                    end = Offset(w * p, h),
                    strokeWidth = 1.5f
                )
            }

            for (blip in hud.minimapBlips) {
                val bx = ((blip.nx + 1f) * 0.5f).coerceIn(0.04f, 0.96f) * w
                val by = ((blip.nz + 1f) * 0.5f).coerceIn(0.04f, 0.96f) * h
                when (blip.kind) {
                    BlipKind.MONSTER -> drawCircle(AlertRed, radius = 2.4.dp.toPx(), center = Offset(bx, by))
                    BlipKind.BOSS -> drawCircle(ScrapGold, radius = 4.5.dp.toPx(), center = Offset(bx, by))
                    BlipKind.VEHICLE -> drawRect(
                        color = CyberCyan,
                        topLeft = Offset(bx - 3.dp.toPx(), by - 3.dp.toPx()),
                        size = Size(6.dp.toPx(), 6.dp.toPx())
                    )
                    BlipKind.TURRET -> drawCircle(AmberPrimary, radius = 3.2.dp.toPx(), center = Offset(bx, by))
                    BlipKind.SUPPLY -> drawCircle(ToxicGreen, radius = 2.8.dp.toPx(), center = Offset(bx, by))
                    BlipKind.PLAYER -> drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(bx, by))
                }
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    label: String,
    accent: Color,
    size: Dp = 116.dp,
    onStickChanged: (Float, Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(size)
            .pointerInput(size) {
                val maxRadius = (this.size.width / 2f) * 0.72f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    fun updateFromPos(pos: Offset) {
                        val delta = pos - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > maxRadius) {
                            Offset(delta.x / dist * maxRadius, delta.y / dist * maxRadius)
                        } else delta
                        knobOffset = clamped
                        onStickChanged(
                            (clamped.x / maxRadius).coerceIn(-1f, 1f),
                            (clamped.y / maxRadius).coerceIn(-1f, 1f)
                        )
                    }
                    updateFromPos(down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        updateFromPos(change.position)
                        change.consume()
                    }
                    knobOffset = Offset.Zero
                    onStickChanged(0f, 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val r = this.size.minDimension / 2f
            drawCircle(
                color = ObsidianBg.copy(alpha = 0.52f),
                radius = r
            )
            drawCircle(
                color = accent.copy(alpha = 0.55f),
                radius = r,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.85f),
                radius = r * 0.36f,
                center = center + knobOffset
            )
        }
        if (knobOffset == Offset.Zero) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun HoldActionButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onHeldChanged: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (pressed) accent else ObsidianBg.copy(alpha = 0.72f)
            )
            .border(2.dp, accent, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    onHeldChanged(true)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        change.consume()
                    }
                    pressed = false
                    onHeldChanged(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (pressed) ObsidianBg else Color.White
        )
    }
}
