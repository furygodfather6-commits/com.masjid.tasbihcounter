package com.masjid.tasbihcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
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
            // ViewModel को यहाँ केवल एक बार बनाया गया है
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            MasjidTasbihCounterTheme(themeSetting = uiState.settings.theme) {
                // ViewModel और UI State को TasbihApp में पास किया गया है
                TasbihApp(
                    uiState = uiState,
                    viewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
fun TasbihApp(uiState: AppUiState, viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val activeTasbih = uiState.tasbihList.find { it.id == uiState.activeTasbihId }

    LaunchedEffect(activeTasbih, uiState.tasbihList) {
        if (activeTasbih == null && uiState.tasbihList.isNotEmpty()) {
            // ViewModel के फ़ंक्शन को सही ढंग से कॉल किया गया
            viewModel.selectTasbih(uiState.tasbihList.first().id)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    onStartCounting = { currentScreen = Screen.COUNTER },
                    onNavigateToHistory = { currentScreen = Screen.HISTORY },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.HISTORY -> {
                HistoryScreen(
                    sessions = uiState.history,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChange = { viewModel.updateTheme(it) }, // ViewModel का उपयोग
                    onVibrationToggle = { viewModel.toggleVibration(it) }, // ViewModel का उपयोग
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.LIST -> {
                TasbihListScreen(
                    tasbihList = uiState.tasbihList,
                    onSelectTasbih = {
                        viewModel.selectTasbih(it.id) // ViewModel का उपयोग
                        currentScreen = Screen.COUNTER
                    },
                    onAddTasbih = { name, target -> viewModel.addTasbih(name, target) }, // ViewModel का उपयोग
                    onDeleteTasbih = { viewModel.deleteTasbih(it.id) }, // ViewModel का उपयोग
                    onBack = { if (uiState.tasbihList.isNotEmpty()) currentScreen = Screen.COUNTER }
                )
            }
            Screen.COUNTER -> {
                if (activeTasbih != null) {
                    CounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { viewModel.incrementActiveTasbih() }, // ViewModel का उपयोग
                        onReset = { viewModel.resetActiveTasbih() }, // ViewModel का उपयोग
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                        onNavigateToList = { currentScreen = Screen.LIST },
                        onNavigateToThemeCustomization = { currentScreen = Screen.THEME_CUSTOMIZATION },
                        onNavigateToAdvancedCounter = { currentScreen = Screen.ADVANCED_COUNTER }
                    )
                } else {
                    LaunchedEffect(Unit){
                        currentScreen = Screen.LIST
                    }
                }
            }
            Screen.THEME_CUSTOMIZATION -> {
                ThemeCustomizationScreen(
                    settings = uiState.settings,
                    onThemeChange = { viewModel.updateTheme(it) }, // ViewModel का उपयोग
                    onBack = { currentScreen = Screen.COUNTER }
                )
            }
            Screen.ADVANCED_COUNTER -> {
                if (activeTasbih != null) {
                    AdvancedCounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { viewModel.incrementActiveTasbih() }, // ViewModel का उपयोग
                        onReset = { viewModel.resetActiveTasbih() }, // ViewModel का उपयोग
                        onBack = { currentScreen = Screen.COUNTER }
                    )
                } else {
                    LaunchedEffect(Unit){
                        currentScreen = Screen.LIST
                    }
                }
            }
        }
    }
}