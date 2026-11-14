package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlayerMetaState
import com.example.game.model.AppLanguage
import com.example.game.model.CameraMode
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
fun MainMenuScreen(
    meta: PlayerMetaState,
    onPlayClick: () -> Unit,
    onWorldsClick: () -> Unit,
    onArsenalClick: () -> Unit,
    onLockerClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleLanguage: (AppLanguage) -> Unit,
    onTogglePreferredCamera: (CameraMode) -> Unit
) {
    val s = LocalStrings.current
    val lang = meta.language
    val claimableCount = meta.claimableMissionCount()
    val totalCosmetics = meta.unlockedSkins.size + meta.unlockedOutfits.size + meta.unlockedCarPaints.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Command Header: Synthwave Offline Status + DE/EN Switch + Coins + Audio + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = TacticalSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = s.offlineReady,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }

                    // Instant DE / EN Toggle Pill
                    Surface(
                        color = TacticalSurfaceVariant,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, TacticalBorder),
                        modifier = Modifier
                            .clickable {
                                val next = if (lang == AppLanguage.DE) AppLanguage.EN else AppLanguage.DE
                                onToggleLanguage(next)
                            }
                            .testTag("language_toggle_pill")
                    ) {
                        Text(
                            text = if (lang == AppLanguage.DE) "DE 🇩🇪" else "EN 🇬🇧",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoinPill(coins = meta.coins)
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TacticalSurface)
                            .border(1.dp, TacticalBorder, CircleShape)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (meta.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Audio",
                            tint = if (meta.soundEnabled) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TacticalSurface)
                            .border(1.dp, CyberCyan.copy(alpha = 0.6f), CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = s.settingsTitle,
                            tint = CyberCyan
                        )
                    }
                }
            }

            // Hero Key Art Banner with Mode & Weather Chips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, TacticalBorder, RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_deadzone),
                    contentDescription = "Deadzone Drive Key Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBg.copy(alpha = 0.15f),
                                    ObsidianBg.copy(alpha = 0.55f),
                                    ObsidianBg.copy(alpha = 0.96f)
                                )
                            )
                        )
                )
                // Top-Right Weather & Time Badge
                Surface(
                    color = ObsidianBg.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(meta.selectedWorld.accentColorHex)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${meta.timeOfDayMode.title(lang)} • ${meta.selectedWorld.defaultWeather.title(lang)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(meta.selectedWorld.accentColorHex),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = s.tagline,
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberPrimary
                    )
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Active Deployment Zone + 2D/3D Switch + Loadout & Locker Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(1.dp, TacticalBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.activeZone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = meta.selectedWorld.title(lang),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(meta.selectedWorld.accentColorHex)
                            )
                            Text(
                                text = meta.selectedWorld.subtitle(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 2D / 3D Default Camera Switch
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TacticalSurfaceVariant)
                                .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CameraMode.entries.forEach { mode ->
                                val selected = meta.preferredCameraMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) CyberCyan else Color.Transparent)
                                        .clickable { onTogglePreferredCamera(mode) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("menu_camera_${mode.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (mode == CameraMode.MODE_3D) "3D" else "2D",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) ObsidianBg else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Dual Weapon + Special Ability Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadoutMiniPill(
                            label = s.primarySlot,
                            value = meta.equippedWeapon.title(lang),
                            accent = AmberPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        LoadoutMiniPill(
                            label = s.secondarySlot,
                            value = meta.secondaryWeapon.title(lang),
                            accent = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        LoadoutMiniPill(
                            label = s.specialAbility,
                            value = meta.equippedAbility.shortLabel(lang),
                            accent = Color(meta.equippedAbility.accentHex),
                            modifier = Modifier.weight(0.85f)
                        )
                    }

                    // Locker Cosmetics Row (Outfit, Car Paint, Gun Skin)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalSurfaceVariant)
                            .clickable { onLockerClick() }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${s.outfitLabel}: ${meta.equippedOutfit.title(lang)}  •  ${s.carPaintLabel}: ${meta.equippedCarPaint.title(lang)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${s.weaponSkinLabel}: ${meta.equippedSkin.title(lang)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(meta.equippedSkin.accentHex)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ColorSwatchDot(meta.equippedOutfit.primaryHex, meta.equippedOutfit.accentHex)
                            ColorSwatchDot(meta.equippedCarPaint.primaryHex, meta.equippedCarPaint.accentHex)
                            ColorSwatchDot(meta.equippedSkin.primaryHex, meta.equippedSkin.accentHex)
                        }
                    }
                }
            }

            // Primary Play Button
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = Color(0xFF1A0800)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("play_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.startDeployment,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2x2 Navigation Grid: Worlds, Arsenal, Locker, Missions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuActionCard(
                    title = s.worldsTitle,
                    subtitle = s.worldsSubtitle(meta.worldRecords.count { it.value.unlocked }),
                    icon = Icons.Filled.Public,
                    accent = CyberCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("worlds_button"),
                    onClick = onWorldsClick
                )
                MenuActionCard(
                    title = s.arsenalTitle,
                    subtitle = s.arsenalSubtitle(meta.unlockedWeapons.size),
                    icon = Icons.Filled.Hardware,
                    accent = AmberPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("shop_button"),
                    onClick = onArsenalClick
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuActionCard(
                    title = s.lockerTitle,
                    subtitle = s.lockerSubtitle(totalCosmetics),
                    icon = Icons.Filled.Checkroom,
                    accent = Color(0xFFE879F9),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("locker_button"),
                    onClick = onLockerClick
                )
                MenuActionCard(
                    title = s.missionsTitle,
                    subtitle = s.missionsSubtitle(claimableCount),
                    icon = Icons.Filled.EmojiEvents,
                    accent = if (claimableCount > 0) ToxicGreen else ScrapGold,
                    badgeCount = claimableCount,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("missions_button"),
                    onClick = onMissionsClick
                )
            }

            // Career Stats Strip
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(1.dp, TacticalBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(label = s.monstersDefeated, value = "${meta.totalKills}", color = AmberPrimary)
                    StatColumn(label = s.deployments, value = "${meta.totalGames}", color = CyberCyan)
                    StatColumn(
                        label = s.bestWave,
                        value = "${meta.worldRecords.values.maxOfOrNull { it.bestWave } ?: 0}",
                        color = ScrapGold
                    )
                }
            }

            // Quick Survival Tip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = s.tipText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadoutMiniPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ColorSwatchDot(primaryHex: Long, accentHex: Long) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(primaryHex), Color(accentHex))))
            .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape)
    )
}

@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier) {
    Surface(
        color = TacticalSurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, ScrapGold.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = "Coins",
                tint = ScrapGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$coins",
                style = MaterialTheme.typography.titleMedium,
                color = ScrapGold
            )
        }
    }
}

@Composable
private fun MenuActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = BorderStroke(1.dp, if (badgeCount > 0) accent else TacticalBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accent)
                }
                if (badgeCount > 0) {
                    Surface(
                        color = accent,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$badgeCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = ObsidianBg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
