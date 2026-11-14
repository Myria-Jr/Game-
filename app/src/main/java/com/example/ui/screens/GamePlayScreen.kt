package com.example.ui.screens

import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.render.DualViewGLRenderer
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ScrapGold
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceVariant
import com.example.ui.theme.ToxicGreen
import com.example.ui.viewmodel.ActiveMatchViewModel

@Composable
fun GamePlayScreen(
    matchViewModel: ActiveMatchViewModel,
    matchInstanceKey: Int,
    onOpenSettingsFromPause: () -> Unit,
    onRestartMatch: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val hud by matchViewModel.hudState.collectAsStateWithLifecycle()
    val simulation = matchViewModel.simulation ?: return
    val s = LocalStrings.current
    val lang = s.language
    val lifecycleOwner = LocalLifecycleOwner.current
    var glViewRef by remember { mutableStateOf<GLSurfaceView?>(null) }

    BackHandler {
        if (!hud.isGameOver) {
            matchViewModel.setPaused(!hud.isPaused)
        } else {
            onExitToMenu()
        }
    }

    DisposableEffect(lifecycleOwner, glViewRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> {
                    glViewRef?.onPause()
                    if (!hud.isGameOver) matchViewModel.setPaused(true)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Unified OpenGL ES 3D + 2D Viewport
        key(matchInstanceKey) {
            AndroidView(
                factory = { ctx ->
                    GLSurfaceView(ctx).apply {
                        setEGLContextClientVersion(2)
                        setRenderer(
                            DualViewGLRenderer(
                                simulation = simulation,
                                onHudUpdate = { matchViewModel.refreshHudFromRenderer() }
                            )
                        )
                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        glViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // HUD Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopHudBar(
                    hud = hud,
                    onToggleCameraMode = { matchViewModel.toggleCameraMode() },
                    onPauseClick = { matchViewModel.setPaused(true) }
                )

                val bossName = hud.bossName
                if (bossName != null) {
                    BossHealthBar(name = bossName, hp = hud.bossHp, maxHp = hud.bossMaxHp)
                }

                AnimatedVisibility(
                    visible = hud.bannerMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = ObsidianBg.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, AmberPrimary)
                    ) {
                        Text(
                            text = hud.bannerMessage.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = ScrapGold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom Control Zone
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action Bar: Vehicle Enter/Exit + Weapon Swap + Special Ability + Reload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hud.inVehicle || hud.nearVehicle) {
                        Button(
                            onClick = { matchViewModel.toggleVehicle() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hud.inVehicle) AlertRed else CyberCyan,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("vehicle_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (hud.inVehicle) s.hudExitCar else s.hudEnterCar,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Special Ability Button (EMP / Turret / Overdrive Shield)
                    val abilityReady = hud.abilityCooldown <= 0.05f
                    val abilityAccent = Color(hud.ability.accentHex)
                    Button(
                        onClick = { matchViewModel.activateSpecialAbility() },
                        enabled = abilityReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = abilityAccent,
                            contentColor = ObsidianBg,
                            disabledContainerColor = TacticalSurfaceVariant.copy(alpha = 0.82f),
                            disabledContentColor = abilityAccent
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("special_ability_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (abilityReady) {
                                hud.ability.shortLabel(lang)
                            } else {
                                "${hud.ability.shortLabel(lang)} ${hud.abilityCooldown.toInt() + 1}s"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Dual Weapon Swap Button
                    if (hud.weapon != hud.secondaryWeapon) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { matchViewModel.switchWeaponSlot() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalSurfaceVariant.copy(alpha = 0.90f),
                                contentColor = CyberCyan
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("weapon_swap_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = s.hudSwitchWeapon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hud.secondaryWeapon.title(lang).take(9)} (${hud.secondaryAmmo})",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    if (!hud.inVehicle) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { matchViewModel.triggerReload() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalSurfaceVariant.copy(alpha = 0.88f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = s.hudReload,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (hud.inVehicle) {
                    // Car Driving Controls: Left Steering, Right Gas/Brake/Nitro/Fire
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HoldActionButton(
                                text = "◀",
                                accent = CyberCyan,
                                modifier = Modifier
                                    .size(width = 74.dp, height = 68.dp)
                                    .testTag("car_steer_left"),
                                onHeldChanged = { simulation.steerLeftHeld = it }
                            )
                            HoldActionButton(
                                text = "▶",
                                accent = CyberCyan,
                                modifier = Modifier
                                    .size(width = 74.dp, height = 68.dp)
                                    .testTag("car_steer_right"),
                                onHeldChanged = { simulation.steerRightHeld = it }
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HoldActionButton(
                                    text = s.hudFire,
                                    accent = AmberPrimary,
                                    modifier = Modifier
                                        .size(width = 84.dp, height = 54.dp)
                                        .testTag("car_fire_button"),
                                    onHeldChanged = { simulation.fireButtonHeld = it }
                                )
                                HoldActionButton(
                                    text = "${s.hudNitro} ${hud.vehicleNitro.toInt()}%",
                                    accent = ScrapGold,
                                    modifier = Modifier
                                        .size(width = 92.dp, height = 54.dp)
                                        .testTag("car_nitro_button"),
                                    onHeldChanged = { simulation.nitroHeld = it }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HoldActionButton(
                                    text = s.hudBrake,
                                    accent = AlertRed,
                                    modifier = Modifier
                                        .size(width = 84.dp, height = 62.dp)
                                        .testTag("car_brake_button"),
                                    onHeldChanged = { simulation.brakeHeld = it }
                                )
                                HoldActionButton(
                                    text = s.hudGas,
                                    accent = ToxicGreen,
                                    modifier = Modifier
                                        .size(width = 92.dp, height = 62.dp)
                                        .testTag("car_gas_button"),
                                    onHeldChanged = { simulation.gasHeld = it }
                                )
                            }
                        }
                    }
                } else {
                    // On-Foot Dual Joysticks + Tap Fire option
                    val scaledStickSize = (118f * hud.joystickScale).dp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        VirtualJoystick(
                            label = if (lang == com.example.game.model.AppLanguage.DE) "LAUFEN" else "MOVE",
                            accent = CyberCyan,
                            size = scaledStickSize,
                            onStickChanged = { x, y ->
                                simulation.moveStickX = x
                                simulation.moveStickY = y
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HoldActionButton(
                                text = s.hudFire,
                                accent = AmberPrimary,
                                modifier = Modifier
                                    .size(width = 68.dp, height = 68.dp)
                                    .testTag("foot_fire_button"),
                                onHeldChanged = { simulation.fireButtonHeld = it }
                            )
                            VirtualJoystick(
                                label = if (lang == com.example.game.model.AppLanguage.DE) "ZIELEN" else "AIM",
                                accent = AmberPrimary,
                                size = scaledStickSize,
                                onStickChanged = { x, y ->
                                    simulation.aimStickX = x
                                    simulation.aimStickY = y
                                }
                            )
                        }
                    }
                }
            }
        }

        // Pause Overlay
        if (hud.isPaused && !hud.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(1.5.dp, TacticalBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth(0.86f)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = s.hudPausedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${hud.worldId.title(lang)} • ${s.hudWave} ${hud.wave}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberCyan
                        )
                        Button(
                            onClick = { matchViewModel.setPaused(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = ObsidianBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(s.hudResume, style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = onOpenSettingsFromPause,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.settingsTitle, color = CyberCyan, style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = onRestartMatch,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(s.hudRestart, color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = {
                                matchViewModel.commitRewardsBeforeExit()
                                onExitToMenu()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(s.hudMainMenu, color = ScrapGold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Game Over Summary Overlay
        if (hud.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.86f))
                    .testTag("game_over_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(2.dp, AlertRed),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.88f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = s.hudGameOver,
                            style = MaterialTheme.typography.headlineMedium,
                            color = AlertRed
                        )
                        Text(
                            text = hud.worldId.title(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${hud.wave}", style = MaterialTheme.typography.headlineMedium, color = CyberCyan)
                                Text(s.hudWave, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${hud.sessionKills}", style = MaterialTheme.typography.headlineMedium, color = AmberPrimary)
                                Text(s.hudKills, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+${hud.sessionCoins}", style = MaterialTheme.typography.headlineMedium, color = ScrapGold)
                                Text(s.hudCoins, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onRestartMatch,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = ObsidianBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(s.hudRestart, style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = onExitToMenu,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(s.hudMainMenu, color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
