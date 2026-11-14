package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PlayerMetaState
import com.example.game.model.WorldId
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ScrapGold
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceVariant

@Composable
fun WorldSelectScreen(
    meta: PlayerMetaState,
    onSelectOrUnlockAndPlay: (WorldId) -> Unit,
    onSelectOnly: (WorldId) -> Unit,
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
                            .testTag("worlds_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = s.worldsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                CoinPill(coins = meta.coins)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(WorldId.entries, key = { it.name }) { world ->
                    val record = meta.worldRecords[world]
                    val unlocked = record?.unlocked ?: (world.unlockCost == 0)
                    val isSelected = meta.selectedWorld == world
                    val accent = Color(world.accentColorHex)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accent else TacticalBorder
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("world_card_${world.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BiomePreviewCanvas(
                                    world = world,
                                    modifier = Modifier
                                        .size(width = 108.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = world.title(lang),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White
                                        )
                                        Row {
                                            repeat(world.difficultyStars) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = ScrapGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${world.subtitle(lang)} • ${world.defaultWeather.title(lang)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent
                                    )
                                    Text(
                                        text = world.description(lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Boss: ${world.bossTitle(lang)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberPrimary
                                    )
                                    Text(
                                        text = "${s.hudWave} ${record?.bestWave ?: 0} • ${record?.bestKills ?: 0} ${s.hudKills}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (unlocked) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (!isSelected) {
                                            Button(
                                                onClick = { onSelectOnly(world) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = TacticalSurfaceVariant,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(s.selectBtn, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                        Button(
                                            onClick = { onSelectOrUnlockAndPlay(world) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = accent,
                                                contentColor = ObsidianBg
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(s.startBtn, style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                } else {
                                    val canAfford = meta.coins >= world.unlockCost
                                    Button(
                                        onClick = { onSelectOnly(world) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ScrapGold,
                                            contentColor = ObsidianBg,
                                            disabledContainerColor = TacticalSurfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${world.unlockCost} C", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun BiomePreviewCanvas(world: WorldId, modifier: Modifier = Modifier) {
    val sky = Color(world.skyColor[0], world.skyColor[1], world.skyColor[2])
    val ground = Color(world.groundColor[0], world.groundColor[1], world.groundColor[2])
    val road = Color(world.roadColor[0], world.roadColor[1], world.roadColor[2])
    val obs = Color(world.obstaclePrimaryColor[0], world.obstaclePrimaryColor[1], world.obstaclePrimaryColor[2])
    val acc = Color(world.accentColorHex)
    val carCol = Color(world.vehicleColor[0], world.vehicleColor[1], world.vehicleColor[2])

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(sky, Offset.Zero, Size(w, h * 0.55f))
        drawRect(ground, Offset(0f, h * 0.55f), Size(w, h * 0.45f))
        drawRect(road, Offset(0f, h * 0.68f), Size(w, h * 0.22f))
        drawRect(acc, Offset(0f, h * 0.78f), Size(w, 2.dp.toPx()))

        drawRect(obs, Offset(w * 0.10f, h * 0.24f), Size(w * 0.16f, h * 0.36f))
        drawRect(obs, Offset(w * 0.34f, h * 0.16f), Size(w * 0.20f, h * 0.44f))
        drawRect(obs, Offset(w * 0.66f, h * 0.28f), Size(w * 0.22f, h * 0.32f))
        drawRect(acc, Offset(w * 0.36f, h * 0.20f), Size(w * 0.16f, h * 0.06f))

        drawRect(carCol, Offset(w * 0.22f, h * 0.64f), Size(w * 0.28f, h * 0.14f))
        drawCircle(Color.Black, radius = 4.dp.toPx(), center = Offset(w * 0.28f, h * 0.80f))
        drawCircle(Color.Black, radius = 4.dp.toPx(), center = Offset(w * 0.44f, h * 0.80f))
        drawCircle(Color.Cyan, radius = 2.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.70f))
    }
}
