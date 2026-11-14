package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.game.model.SpecialAbilityId
import com.example.game.model.UpgradeType
import com.example.game.model.WeaponId
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
fun ShopScreen(
    meta: PlayerMetaState,
    onWeaponAction: (WeaponId, Boolean) -> Unit,
    onAbilityAction: (SpecialAbilityId) -> Unit,
    onUpgradeAction: (UpgradeType) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val s = LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(s.tabWeapons, s.tabAbilities, s.tabUpgrades)

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
                            .testTag("shop_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = s.arsenalTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                CoinPill(coins = meta.coins)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dual-Weapon + Special Tech Loadout Showcase
            ArsenalLoadoutBanner(meta = meta)

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TacticalSurface,
                contentColor = AmberPrimary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmberPrimary
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
                                color = if (selectedTab == index) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("shop_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> ArsenalWeaponsTab(meta, onWeaponAction)
                1 -> SpecialAbilitiesTab(meta, onAbilityAction)
                else -> UpgradesTab(meta, onUpgradeAction)
            }
        }
    }
}

@Composable
private fun ArsenalLoadoutBanner(meta: PlayerMetaState) {
    val s = LocalStrings.current
    val lang = meta.language
    val prim = Color(meta.equippedSkin.primaryHex)
    val acc = Color(meta.equippedSkin.accentHex)

    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = BorderStroke(1.dp, acc.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${s.primarySlot}: ${meta.equippedWeapon.title(lang)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberPrimary
                    )
                    Text(
                        text = "${s.secondarySlot}: ${meta.secondaryWeapon.title(lang)}  •  ${s.specialAbility}: ${meta.equippedAbility.title(lang)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ObsidianBg, TacticalSurfaceVariant, ObsidianBg)
                        )
                    )
            ) {
                val cx = size.width * 0.44f
                val cy = size.height * 0.52f
                val bodyW = when (meta.equippedWeapon) {
                    WeaponId.PISTOL -> 86.dp.toPx()
                    WeaponId.SHOTGUN -> 148.dp.toPx()
                    WeaponId.ASSAULT_RIFLE -> 160.dp.toPx()
                    WeaponId.SNIPER -> 200.dp.toPx()
                    WeaponId.MINIGUN -> 175.dp.toPx()
                    WeaponId.PLASMA_RIFLE -> 158.dp.toPx()
                }
                val bodyH = if (meta.equippedWeapon == WeaponId.MINIGUN) 24.dp.toPx() else 17.dp.toPx()

                drawRoundRect(
                    color = prim,
                    topLeft = Offset(cx - bodyW * 0.35f, cy),
                    size = Size(15.dp.toPx(), 22.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = prim,
                    topLeft = Offset(cx - bodyW * 0.45f, cy - bodyH * 0.6f),
                    size = Size(bodyW * 0.72f, bodyH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = acc,
                    topLeft = Offset(cx + bodyW * 0.15f, cy - bodyH * 0.45f),
                    size = Size(bodyW * 0.44f, bodyH * 0.55f),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                drawRect(
                    color = acc.copy(alpha = 0.75f),
                    topLeft = Offset(cx + bodyW * 0.62f, cy - 2.dp.toPx()),
                    size = Size(38.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun ArsenalWeaponsTab(
    meta: PlayerMetaState,
    onWeaponAction: (WeaponId, Boolean) -> Unit
) {
    val s = LocalStrings.current
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(WeaponId.entries, key = { it.name }) { weapon ->
            val unlocked = weapon in meta.unlockedWeapons
            val isPrimary = meta.equippedWeapon == weapon
            val isSecondary = meta.secondaryWeapon == weapon
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(
                    width = if (isPrimary || isSecondary) 2.dp else 1.dp,
                    color = when {
                        isPrimary -> AmberPrimary
                        isSecondary -> CyberCyan
                        else -> TacticalBorder
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weapon_card_${weapon.name}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(weapon.category(lang).uppercase(), style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                            Text(weapon.title(lang), style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                        if (!unlocked) {
                            val canAfford = meta.coins >= weapon.unlockCost
                            Button(
                                onClick = { onWeaponAction(weapon, false) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ScrapGold,
                                    contentColor = ObsidianBg,
                                    disabledContainerColor = TacticalSurfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("${weapon.unlockCost} C", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onWeaponAction(weapon, false) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPrimary) AmberPrimary else TacticalSurfaceVariant,
                                        contentColor = if (isPrimary) ObsidianBg else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isPrimary) s.primaryEquipped else s.equipPrimary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                Button(
                                    onClick = { onWeaponAction(weapon, true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSecondary) CyberCyan else TacticalSurfaceVariant,
                                        contentColor = if (isSecondary) ObsidianBg else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isSecondary) s.secondaryEquipped else s.equipSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                    Text(weapon.description(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatMiniBar(s.statDamage, ((weapon.damage * weapon.pellets) / 130f).coerceIn(0.15f, 1f), AmberPrimary, Modifier.weight(1f))
                        StatMiniBar(s.statFireRate, (120f / weapon.fireRateMs).coerceIn(0.15f, 1f), CyberCyan, Modifier.weight(1f))
                        StatMiniBar(s.statRange, (weapon.range / 95f).coerceIn(0.2f, 1f), ScrapGold, Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun SpecialAbilitiesTab(
    meta: PlayerMetaState,
    onAbilityAction: (SpecialAbilityId) -> Unit
) {
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(SpecialAbilityId.entries, key = { it.name }) { ability ->
            val unlocked = ability in meta.unlockedAbilities
            val equipped = meta.equippedAbility == ability
            val acc = Color(ability.accentHex)

            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(if (equipped) 2.dp else 1.dp, if (equipped) acc else TacticalBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ability_card_${ability.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(acc.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = acc,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COOLDOWN: ${ability.cooldownSec.toInt()}S",
                            style = MaterialTheme.typography.labelSmall,
                            color = acc
                        )
                        Text(ability.title(lang), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            text = ability.description(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LockerEquipButton(
                        unlocked = unlocked,
                        equipped = equipped,
                        cost = ability.unlockCost,
                        canAfford = meta.coins >= ability.unlockCost,
                        onClick = { onAbilityAction(ability) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun UpgradesTab(meta: PlayerMetaState, onUpgradeAction: (UpgradeType) -> Unit) {
    val s = LocalStrings.current
    val lang = meta.language
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(UpgradeType.entries, key = { it.name }) { upgrade ->
            val lvl = meta.upgradeLevel(upgrade)
            val maxed = lvl >= upgrade.maxLevel
            val cost = upgrade.costForNextLevel(lvl)
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(1.dp, TacticalBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upgrade_card_${upgrade.name}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(upgrade.title(lang), style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(upgrade.description(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onUpgradeAction(upgrade) },
                            enabled = !maxed && meta.coins >= cost,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maxed) ToxicGreen else ScrapGold,
                                contentColor = ObsidianBg,
                                disabledContainerColor = TacticalSurfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (maxed) s.upgradeMax else "$cost C",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..upgrade.maxLevel) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(if (i <= lvl) CyberCyan else TacticalSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun StatMiniBar(label: String, fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { fraction },
            color = color,
            trackColor = TacticalSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}
