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
                        Screen.COUNTER, Screen.HISTORY, Screen.SETTINGS, Screen.LIST -> currentScreen = Screen.HOME
                        Screen.THEME_CUSTOMIZATION, Screen.ADVANCED_COUNTER -> currentScreen = Screen.COUNTER
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

    LaunchedEffect(activeTasbih, uiState.tasbihList) {
        if (activeTasbih == null && uiState.tasbihList.isNotEmpty()) {
            viewModel.selectTasbih(uiState.tasbihList.first().id)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    onStartCounting = { onScreenChange(Screen.COUNTER) },
                    onNavigateToHistory = { onScreenChange(Screen.HISTORY) },
                    onNavigateToSettings = { onScreenChange(Screen.SETTINGS) }
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
            Screen.LIST -> {
                TasbihListScreen(
                    tasbihList = uiState.tasbihList,
                    onSelectTasbih = {
                        viewModel.selectTasbih(it.id)
                        onScreenChange(Screen.COUNTER)
                    },
                    onAddTasbih = { name, target -> viewModel.addTasbih(name, target) },
                    onDeleteTasbih = { viewModel.deleteTasbih(it.id) },
                    onBack = { if (uiState.tasbihList.isNotEmpty()) onScreenChange(Screen.COUNTER) }
                )
            }
            Screen.COUNTER -> {
                if (activeTasbih != null) {
                    CounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { viewModel.incrementActiveTasbih() },
                        onReset = { viewModel.resetActiveTasbih() },
                        onNavigateToSettings = { onScreenChange(Screen.SETTINGS) },
                        onNavigateToList = { onScreenChange(Screen.LIST) },
                        onNavigateToThemeCustomization = { onScreenChange(Screen.THEME_CUSTOMIZATION) },
                        onNavigateToAdvancedCounter = { onScreenChange(Screen.ADVANCED_COUNTER) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        onScreenChange(Screen.LIST)
                    }
                }
            }
            Screen.THEME_CUSTOMIZATION -> {
                ThemeCustomizationScreen(
                    settings = uiState.settings,
                    onThemeChange = { viewModel.updateTheme(it) },
                    onBack = { onScreenChange(Screen.COUNTER) }
                )
            }
            Screen.ADVANCED_COUNTER -> {
                if (activeTasbih != null) {
                    AdvancedCounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { viewModel.incrementActiveTasbih() },
                        onReset = { viewModel.resetActiveTasbih() },
                        onBack = { onScreenChange(Screen.COUNTER) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        onScreenChange(Screen.LIST)
                    }
                }
            }
        }
    }
}