package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.MusicTrackMode
import com.example.data.PlayerMetaState
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.stringsFor
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.LockerScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.WorldSelectScreen
import com.example.ui.theme.DeadzoneDriveTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.viewmodel.ActiveMatchViewModel
import com.example.ui.viewmodel.GameMetaViewModel

private enum class AppScreen {
    MENU,
    WORLDS,
    ARSENAL,
    LOCKER,
    MISSIONS,
    SETTINGS,
    GAMEPLAY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            DeadzoneDriveTheme {
                DeadzoneDriveApp()
            }
        }
    }
}

@Composable
fun DeadzoneDriveApp(
    metaViewModel: GameMetaViewModel = viewModel(),
    matchViewModel: ActiveMatchViewModel = viewModel()
) {
    val meta by metaViewModel.metaState.collectAsStateWithLifecycle()
    val hud by matchViewModel.hudState.collectAsStateWithLifecycle()
    val toastMsg by metaViewModel.toastMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentScreen by remember { mutableStateOf(AppScreen.MENU) }
    var previousScreenBeforeSettings by remember { mutableStateOf(AppScreen.MENU) }
    var matchInstanceKey by remember { mutableIntStateOf(0) }

    // Pause / resume Synthwave music when app enters background/foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> metaViewModel.onAppResume()
                Lifecycle.Event.ON_PAUSE -> metaViewModel.onAppPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamically switch Synthwave music mode between Menu, Combat, and Boss waves!
    LaunchedEffect(currentScreen, hud.bossName, hud.isPaused, hud.isGameOver) {
        val mode = when {
            currentScreen != AppScreen.GAMEPLAY -> MusicTrackMode.MENU
            hud.isPaused || hud.isGameOver -> MusicTrackMode.MENU
            hud.bossName != null -> MusicTrackMode.BOSS
            else -> MusicTrackMode.COMBAT
        }
        metaViewModel.setMusicMode(mode)
    }

    // Keep live match simulation synced with settings changes
    LaunchedEffect(meta) {
        matchViewModel.syncLiveSettings(meta)
    }

    LaunchedEffect(toastMsg) {
        val msg = toastMsg
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            metaViewModel.clearToast()
        }
    }

    fun launchMatch(stateToUse: PlayerMetaState = meta) {
        matchViewModel.startNewMatch(stateToUse)
        matchInstanceKey++
        currentScreen = AppScreen.GAMEPLAY
    }

    CompositionLocalProvider(LocalStrings provides stringsFor(meta.language)) {
        Scaffold(
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.MENU -> MainMenuScreen(
                            meta = meta,
                            onPlayClick = { launchMatch(meta) },
                            onWorldsClick = { currentScreen = AppScreen.WORLDS },
                            onArsenalClick = { currentScreen = AppScreen.ARSENAL },
                            onLockerClick = { currentScreen = AppScreen.LOCKER },
                            onMissionsClick = { currentScreen = AppScreen.MISSIONS },
                            onSettingsClick = {
                                previousScreenBeforeSettings = AppScreen.MENU
                                currentScreen = AppScreen.SETTINGS
                            },
                            onToggleSound = { metaViewModel.toggleSound() },
                            onToggleLanguage = { metaViewModel.setLanguage(it) },
                            onTogglePreferredCamera = { metaViewModel.setPreferredCameraMode(it) }
                        )

                        AppScreen.WORLDS -> WorldSelectScreen(
                            meta = meta,
                            onSelectOrUnlockAndPlay = { world ->
                                metaViewModel.onWorldAction(world) { freshMeta ->
                                    launchMatch(freshMeta)
                                }
                            },
                            onSelectOnly = { world ->
                                metaViewModel.onWorldAction(world)
                            },
                            onBack = { currentScreen = AppScreen.MENU }
                        )

                        AppScreen.ARSENAL -> ShopScreen(
                            meta = meta,
                            onWeaponAction = { weapon, asSecondary ->
                                metaViewModel.onWeaponAction(weapon, asSecondary)
                            },
                            onAbilityAction = { metaViewModel.onAbilityAction(it) },
                            onUpgradeAction = { metaViewModel.onUpgradeAction(it) },
                            onBack = { currentScreen = AppScreen.MENU }
                        )

                        AppScreen.LOCKER -> LockerScreen(
                            meta = meta,
                            onOutfitAction = { metaViewModel.onOutfitAction(it) },
                            onCarPaintAction = { metaViewModel.onCarPaintAction(it) },
                            onSkinAction = { metaViewModel.onSkinAction(it) },
                            onBack = { currentScreen = AppScreen.MENU }
                        )

                        AppScreen.MISSIONS -> MissionsScreen(
                            meta = meta,
                            onClaimMission = { metaViewModel.onClaimMission(it) },
                            onBack = { currentScreen = AppScreen.MENU }
                        )

                        AppScreen.SETTINGS -> SettingsScreen(
                            meta = meta,
                            onLanguageChange = { metaViewModel.setLanguage(it) },
                            onMusicVolumeChange = { metaViewModel.setMusicVolume(it) },
                            onSfxVolumeChange = { metaViewModel.setSfxVolume(it) },
                            onGraphicsQualityChange = { metaViewModel.setGraphicsQuality(it) },
                            onTimeOfDayChange = { metaViewModel.setTimeOfDayMode(it) },
                            onWeatherEnabledChange = { metaViewModel.setWeatherEnabled(it) },
                            onShowFpsChange = { metaViewModel.setShowFps(it) },
                            onAutoFireAssistChange = { metaViewModel.setAutoFireAssist(it) },
                            onJoystickScaleChange = { metaViewModel.setJoystickScale(it) },
                            onAimSensitivityChange = { metaViewModel.setAimSensitivity(it) },
                            onBack = { currentScreen = previousScreenBeforeSettings }
                        )

                        AppScreen.GAMEPLAY -> GamePlayScreen(
                            matchViewModel = matchViewModel,
                            matchInstanceKey = matchInstanceKey,
                            onOpenSettingsFromPause = {
                                previousScreenBeforeSettings = AppScreen.GAMEPLAY
                                currentScreen = AppScreen.SETTINGS
                            },
                            onRestartMatch = { launchMatch(meta) },
                            onExitToMenu = { currentScreen = AppScreen.MENU }
                        )
                    }
                }
            }
        }
    }
}
