package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PlayerMetaState
import com.example.game.model.AppLanguage
import com.example.game.model.MissionId
import com.example.game.model.TimeOfDayMode
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ScrapGold
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceVariant
import com.example.ui.theme.ToxicGreen

@Composable
fun MissionsScreen(
    meta: PlayerMetaState,
    onClaimMission: (MissionId) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val s = LocalStrings.current
    val lang = meta.language

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("missions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = s.missionsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                CoinPill(coins = meta.coins)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MissionId.entries, key = { it.name }) { mission ->
                    val progress = meta.missionProgress(mission)
                    val claimed = mission in meta.claimedMissions
                    val claimable = meta.isMissionClaimable(mission)
                    val fraction = (progress.toFloat() / mission.targetValue.toFloat()).coerceIn(0f, 1f)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                        border = BorderStroke(
                            width = if (claimable) 2.dp else 1.dp,
                            color = when {
                                claimable -> ToxicGreen
                                claimed -> CyberCyan.copy(alpha = 0.45f)
                                else -> TacticalBorder
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mission_card_${mission.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ScrapGold.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = null,
                                            tint = ScrapGold
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = mission.title(lang),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = mission.description(lang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { onClaimMission(mission) },
                                    enabled = claimable,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (claimed) CyberCyan else ToxicGreen,
                                        contentColor = ObsidianBg,
                                        disabledContainerColor = TacticalSurfaceVariant,
                                        disabledContentColor = if (claimed) CyberCyan else ScrapGold
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (claimed) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(s.claimedBadge, style = MaterialTheme.typography.labelLarge)
                                    } else if (claimable) {
                                        Text("+${mission.rewardCoins} C ${s.claimReward}", style = MaterialTheme.typography.labelLarge)
                                    } else {
                                        Text("+${mission.rewardCoins} C", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    color = if (claimable || claimed) ToxicGreen else AmberPrimary,
                                    trackColor = TacticalSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    text = "${progress.coerceAtMost(mission.targetValue)}/${mission.targetValue}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    meta: PlayerMetaState,
    onLanguageChange: (AppLanguage) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onGraphicsQualityChange: (Int) -> Unit,
    onTimeOfDayChange: (TimeOfDayMode) -> Unit,
    onWeatherEnabledChange: (Boolean) -> Unit,
    onShowFpsChange: (Boolean) -> Unit,
    onAutoFireAssistChange: (Boolean) -> Unit,
    onJoystickScaleChange: (Float) -> Unit,
    onAimSensitivityChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val s = LocalStrings.current
    val lang = meta.language

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = s.settingsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            // 1. Language Card (Deutsch / English)
            SettingsSectionCard(title = s.settingsLanguage, accent = CyberCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLanguage.entries.forEach { option ->
                        val selected = meta.language == option
                        Button(
                            onClick = { onLanguageChange(option) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) CyberCyan else TacticalSurfaceVariant,
                                contentColor = if (selected) ObsidianBg else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("lang_option_${option.code}")
                        ) {
                            Text(
                                text = if (option == AppLanguage.DE) "DEUTSCH" else "ENGLISH",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // 2. Music & Audio Card
            SettingsSectionCard(title = s.settingsAudio, accent = AmberPrimary) {
                Text(
                    text = s.musicStyleHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabeledSliderRow(
                    label = "${s.musicVolume}: ${(meta.musicVolume * 100).toInt()}%",
                    value = meta.musicVolume,
                    valueRange = 0f..1f,
                    accent = AmberPrimary,
                    onValueChange = onMusicVolumeChange
                )
                LabeledSliderRow(
                    label = "${s.sfxVolume}: ${(meta.sfxVolume * 100).toInt()}%",
                    value = meta.sfxVolume,
                    valueRange = 0f..1f,
                    accent = CyberCyan,
                    onValueChange = onSfxVolumeChange
                )
            }

            // 3. Graphics, Time of Day & Weather Card
            SettingsSectionCard(title = s.settingsGraphics, accent = ScrapGold) {
                Text(
                    text = s.graphicsQuality,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val labels = listOf(s.qualityLow, s.qualityMedium, s.qualityUltra)
                    labels.forEachIndexed { idx, label ->
                        val selected = meta.graphicsQuality == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) ScrapGold else TacticalSurfaceVariant)
                                .clickable { onGraphicsQualityChange(idx) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) ObsidianBg else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = s.timeOfDay,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeOfDayMode.entries.forEach { mode ->
                        val selected = meta.timeOfDayMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) CyberCyan else TacticalSurfaceVariant)
                                .clickable { onTimeOfDayChange(mode) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) ObsidianBg else Color.White
                            )
                        }
                    }
                }

                ToggleRow(
                    label = s.weatherEffects,
                    checked = meta.weatherEnabled,
                    onCheckedChange = onWeatherEnabledChange
                )
                ToggleRow(
                    label = s.showFpsCounter,
                    checked = meta.showFps,
                    onCheckedChange = onShowFpsChange
                )
            }

            // 4. Controls & Combat Assist Card
            SettingsSectionCard(title = s.settingsControls, accent = ToxicGreen) {
                ToggleRow(
                    label = s.autoFireAssist,
                    checked = meta.autoFireAssist,
                    onCheckedChange = onAutoFireAssistChange
                )
                Text(
                    text = s.autoFireHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabeledSliderRow(
                    label = "${s.joystickSize}: ${(meta.joystickScale * 100).toInt()}%",
                    value = meta.joystickScale,
                    valueRange = 0.85f..1.25f,
                    accent = ToxicGreen,
                    onValueChange = onJoystickScaleChange
                )
                LabeledSliderRow(
                    label = "${s.aimSensitivity}: ${(meta.aimSensitivity * 100).toInt()}%",
                    value = meta.aimSensitivity,
                    valueRange = 0.6f..1.5f,
                    accent = CyberCyan,
                    onValueChange = onAimSensitivityChange
                )
            }

            // 5. GitHub Upload Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = s.githubExportTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = s.githubExportBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = BorderStroke(1.dp, TacticalBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
            content()
        }
    }
}

@Composable
private fun LabeledSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = TacticalSurfaceVariant
            )
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBg,
                checkedTrackColor = ToxicGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TacticalSurfaceVariant
            )
        )
    }
}
