package com.masjid.tasbihcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masjid.tasbihcounter.ui.ThemeCustomizationScreen
import com.masjid.tasbihcounter.ui.screens.*
import com.masjid.tasbihcounter.ui.theme.MasjidTasbihCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf(Screen.HOME) }

            val onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when (currentScreen) {
                        Screen.HOME -> finish()
                        Screen.COUNTER, Screen.HISTORY, Screen.SETTINGS, Screen.LIST, Screen.PROGRESS -> currentScreen = Screen.HOME
                        Screen.THEME_CUSTOMIZATION -> currentScreen = Screen.HOME
                        Screen.ADVANCED_COUNTER -> currentScreen = Screen.LIST
                    }
                }
            }

            DisposableEffect(Unit) {
                onBackPressedDispatcher.addCallback(onBackPressedCallback)
                onDispose {
                    onBackPressedCallback.remove()
                }
            }

            MasjidTasbihCounterTheme(themeSetting = uiState.settings.theme) {
                TasbihApp(
                    uiState = uiState,
                    viewModel = mainViewModel,
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it }
                )
            }
        }
    }
}

@Composable
fun TasbihApp(
    uiState: AppUiState,
    viewModel: MainViewModel,
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    val activeTasbih = uiState.tasbihList.find { it.id == uiState.activeTasbihId }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    onStartCounting = { onScreenChange(Screen.LIST) },
                    onNavigateToTheme = { onScreenChange(Screen.THEME_CUSTOMIZATION) },
                    onNavigateToSettings = { onScreenChange(Screen.SETTINGS) },
                    onNavigateToProgress = { onScreenChange(Screen.PROGRESS) }
                )
            }
            Screen.LIST -> {
                AdvancedTasbihScreen(
                    tasbihList = uiState.tasbihList,
                    onAddNew = {
                        viewModel.addTasbih("New Tasbih", 100)
                    },
                    onEdit = { /* TODO: Implement Edit Screen */ },
                    onDelete = { viewModel.deleteTasbih(it.id) },
                    onSelect = {
                        viewModel.selectTasbih(it.id)
                        onScreenChange(Screen.ADVANCED_COUNTER)
                    },
                    onBack = { onScreenChange(Screen.HOME) }
                )
            }
            Screen.ADVANCED_COUNTER -> {
                if (activeTasbih != null) {
                    AdvancedCounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { viewModel.incrementCustomTasbih(activeTasbih.id) },
                        onReset = { viewModel.resetCustomTasbih(activeTasbih.id) },
                        onBack = { onScreenChange(Screen.LIST) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        onScreenChange(Screen.LIST)
                    }
                }
            }
            Screen.PROGRESS -> {
                ProgressScreen(
                    progressRecords = uiState.progressRecords,
                    onBack = { onScreenChange(Screen.HOME) }
                )
            }
            Screen.HISTORY -> {
                HistoryScreen(
                    sessions = uiState.history,
                    onBack = { onScreenChange(Screen.HOME) }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChange = { viewModel.updateTheme(it) },
                    onVibrationToggle = { viewModel.toggleVibration(it) },
                    onBack = { onScreenChange(Screen.HOME) }
                )
            }
            Screen.COUNTER -> {
                CounterScreen(
                    sequenceState = uiState.sequenceState,
                    settings = uiState.settings,
                    onIncrement = { viewModel.incrementSequenceCounter() },
                    onReset = { viewModel.resetSequenceCounter() },
                    onNavigateToSettings = { onScreenChange(Screen.SETTINGS) },
                    onNavigateToList = { onScreenChange(Screen.LIST) },
                    onNavigateToThemeCustomization = { onScreenChange(Screen.THEME_CUSTOMIZATION) },
                    onNavigateToAdvancedCounter = { /* Iski zaroorat nahi */ }
                )
            }
            Screen.THEME_CUSTOMIZATION -> {
                ThemeCustomizationScreen(
                    settings = uiState.settings,
                    onThemeChange = { viewModel.updateTheme(it) },
                    onBack = { onScreenChange(Screen.HOME) }
                )
            }
        }
    }
}