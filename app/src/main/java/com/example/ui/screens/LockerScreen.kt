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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PlayerMetaState
import com.example.game.model.CharacterOutfitId
import com.example.game.model.VehiclePaintId
import com.example.game.model.WeaponSkinId
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ScrapGold
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceVariant
import com.example.ui.theme.ToxicGreen

@Composable
fun LockerScreen(
    meta: PlayerMetaState,
    onOutfitAction: (CharacterOutfitId) -> Unit,
    onCarPaintAction: (VehiclePaintId) -> Unit,
    onSkinAction: (WeaponSkinId) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val s = LocalStrings.current
    val lang = meta.language
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(s.tabOutfits, s.tabCarPaints, s.tabWeaponSkins)

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
                            .testTag("locker_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = s.lockerTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                CoinPill(coins = meta.coins)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Locker Stage Showcase (shows Survivor + Armored Vehicle + Weapon side-by-side!)
            LockerStagePreview(meta = meta)

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TacticalSurface,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberCyan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTab == index) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("locker_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> OutfitsTab(meta, onOutfitAction)
                1 -> CarPaintsTab(meta, onCarPaintAction)
                else -> WeaponSkinsLockerTab(meta, onSkinAction)
            }
        }
    }
}

@Composable
private fun LockerStagePreview(meta: PlayerMetaState) {
    val lang = meta.language
    val outfit = meta.equippedOutfit
    val carPaint = meta.equippedCarPaint
    val skin = meta.equippedSkin

    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = BorderStroke(1.dp, Color(outfit.accentHex).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${outfit.title(lang)}  •  ${carPaint.title(lang)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = skin.title(lang),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(skin.accentHex)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(ObsidianBg, TacticalSurfaceVariant)
                        )
                    )
            ) {
                val w = size.width
                val h = size.height

                // Stage floor line
                drawLine(
                    color = Color(outfit.accentHex).copy(alpha = 0.45f),
                    start = Offset(w * 0.06f, h * 0.84f),
                    end = Offset(w * 0.94f, h * 0.84f),
                    strokeWidth = 2.dp.toPx()
                )

                // Left: Survivor Character in Outfit + Held Gun in Skin
                val px = w * 0.24f
                val py = h * 0.84f
                val armorCol = Color(outfit.armorColor[0], outfit.armorColor[1], outfit.armorColor[2])
                val helmetCol = Color(outfit.helmetColor[0], outfit.helmetColor[1], outfit.helmetColor[2])
                val visorCol = Color(outfit.visorColor[0], outfit.visorColor[1], outfit.visorColor[2])

                // Legs & Torso
                drawRoundRect(
                    color = armorCol,
                    topLeft = Offset(px - 16.dp.toPx(), py - 46.dp.toPx()),
                    size = Size(32.dp.toPx(), 46.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Helmet
                drawRoundRect(
                    color = helmetCol,
                    topLeft = Offset(px - 13.dp.toPx(), py - 68.dp.toPx()),
                    size = Size(26.dp.toPx(), 22.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Glowing Visor
                drawRoundRect(
                    color = visorCol,
                    topLeft = Offset(px - 8.dp.toPx(), py - 61.dp.toPx()),
                    size = Size(18.dp.toPx(), 7.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                // Equipped Gun in Skin
                val gunPrim = Color(skin.primaryHex)
                val gunAcc = Color(skin.accentHex)
                drawRoundRect(
                    color = gunPrim,
                    topLeft = Offset(px + 2.dp.toPx(), py - 34.dp.toPx()),
                    size = Size(38.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                drawRoundRect(
                    color = gunAcc,
                    topLeft = Offset(px + 20.dp.toPx(), py - 32.dp.toPx()),
                    size = Size(24.dp.toPx(), 5.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Right: Armored Survival Vehicle in Car Paint
                val cx = w * 0.68f
                val cy = h * 0.82f
                val carHull = Color(carPaint.primaryHex)
                val carTrim = Color(carPaint.accentHex)
                val beamCol = Color(carPaint.headlightColor[0], carPaint.headlightColor[1], carPaint.headlightColor[2])

                // Car Cabin
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(cx - 32.dp.toPx(), cy - 42.dp.toPx()),
                    size = Size(58.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Car Chassis
                drawRoundRect(
                    color = carHull,
                    topLeft = Offset(cx - 52.dp.toPx(), cy - 26.dp.toPx()),
                    size = Size(104.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Neon Trim Stripe
                drawRect(
                    color = carTrim,
                    topLeft = Offset(cx - 48.dp.toPx(), cy - 16.dp.toPx()),
                    size = Size(96.dp.toPx(), 4.dp.toPx())
                )
                // Headlight beam
                drawCircle(
                    color = beamCol,
                    radius = 5.dp.toPx(),
                    center = Offset(cx + 50.dp.toPx(), cy - 17.dp.toPx())
                )
                // Wheels
                drawCircle(Color(0xFF0F172A), radius = 11.dp.toPx(), center = Offset(cx - 30.dp.toPx(), cy - 4.dp.toPx()))
                drawCircle(carTrim, radius = 4.dp.toPx(), center = Offset(cx - 30.dp.toPx(), cy - 4.dp.toPx()))
                drawCircle(Color(0xFF0F172A), radius = 11.dp.toPx(), center = Offset(cx + 30.dp.toPx(), cy - 4.dp.toPx()))
                drawCircle(carTrim, radius = 4.dp.toPx(), center = Offset(cx + 30.dp.toPx(), cy - 4.dp.toPx()))
            }
        }
    }
}

@Composable
private fun OutfitsTab(meta: PlayerMetaState, onOutfitAction: (CharacterOutfitId) -> Unit) {
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(CharacterOutfitId.entries, key = { it.name }) { outfit ->
            val unlocked = outfit in meta.unlockedOutfits
            val equipped = meta.equippedOutfit == outfit
            val prim = Color(outfit.primaryHex)
            val acc = Color(outfit.accentHex)

            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) acc else TacticalBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("outfit_card_${outfit.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(prim, acc)))
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(outfit.rarity(lang).uppercase(), style = MaterialTheme.typography.labelSmall, color = acc)
                        Text(outfit.title(lang), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            text = outfit.description(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LockerEquipButton(
                        unlocked = unlocked,
                        equipped = equipped,
                        cost = outfit.unlockCost,
                        canAfford = meta.coins >= outfit.unlockCost,
                        onClick = { onOutfitAction(outfit) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun CarPaintsTab(meta: PlayerMetaState, onCarPaintAction: (VehiclePaintId) -> Unit) {
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(VehiclePaintId.entries, key = { it.name }) { paint ->
            val unlocked = paint in meta.unlockedCarPaints
            val equipped = meta.equippedCarPaint == paint
            val prim = Color(paint.primaryHex)
            val acc = Color(paint.accentHex)

            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) acc else TacticalBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("carpaint_card_${paint.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(prim, acc)))
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(paint.rarity(lang).uppercase(), style = MaterialTheme.typography.labelSmall, color = acc)
                        Text(paint.title(lang), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            text = paint.description(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LockerEquipButton(
                        unlocked = unlocked,
                        equipped = equipped,
                        cost = paint.unlockCost,
                        canAfford = meta.coins >= paint.unlockCost,
                        onClick = { onCarPaintAction(paint) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun WeaponSkinsLockerTab(meta: PlayerMetaState, onSkinAction: (WeaponSkinId) -> Unit) {
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(WeaponSkinId.entries, key = { it.name }) { skin ->
            val unlocked = skin in meta.unlockedSkins
            val equipped = meta.equippedSkin == skin
            val prim = Color(skin.primaryHex)
            val acc = Color(skin.accentHex)
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) acc else TacticalBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skin_card_${skin.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(prim, acc)))
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(skin.rarity(lang).uppercase(), style = MaterialTheme.typography.labelSmall, color = acc)
                        Text(skin.title(lang), style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    LockerEquipButton(
                        unlocked = unlocked,
                        equipped = equipped,
                        cost = skin.unlockCost,
                        canAfford = meta.coins >= skin.unlockCost,
                        onClick = { onSkinAction(skin) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun LockerEquipButton(
    unlocked: Boolean,
    equipped: Boolean,
    cost: Int,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    Button(
        onClick = onClick,
        enabled = unlocked || canAfford,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                equipped -> ToxicGreen
                unlocked -> CyberCyan
                else -> ScrapGold
            },
            contentColor = ObsidianBg,
            disabledContainerColor = TacticalSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (equipped) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(s.equippedBtn, style = MaterialTheme.typography.labelLarge)
        } else if (unlocked) {
            Text(s.equipBtn, style = MaterialTheme.typography.labelLarge)
        } else {
            Text("$cost C", style = MaterialTheme.typography.labelLarge)
        }
    }
}
