// Path: app/src/main/java/com/masjid/tasbihcounter/MainActivity.kt
package com.masjid.tasbihcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masjid.tasbihcounter.ui.screens.CounterScreen
import com.masjid.tasbihcounter.ui.screens.SettingsScreen
import com.masjid.tasbihcounter.ui.screens.TasbihListScreen
import com.masjid.tasbihcounter.ui.screens.Screen
import com.masjid.tasbihcounter.ui.screens.CelestialCounterScreen
import com.masjid.tasbihcounter.ui.ThemeCustomizationScreen
import com.masjid.tasbihcounter.ui.theme.MasjidTasbihCounterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            MasjidTasbihCounterTheme(themeSetting = uiState.settings.theme) {
                TasbihApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun TasbihApp(mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(Screen.COUNTER) }
    val activeTasbih = uiState.tasbihList.find { it.id == uiState.activeTasbihId }

    LaunchedEffect(activeTasbih, uiState.tasbihList) {
        if (activeTasbih == null && uiState.tasbihList.isNotEmpty()) {
            mainViewModel.selectTasbih(uiState.tasbihList.first().id)
        } else if (uiState.tasbihList.isEmpty()){
            currentScreen = Screen.LIST
        }
    }

    Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.LIST -> {
                TasbihListScreen(
                    tasbihList = uiState.tasbihList,
                    onSelectTasbih = {
                        mainViewModel.selectTasbih(it.id)
                        currentScreen = Screen.COUNTER
                    },
                    onAddTasbih = { name, target -> mainViewModel.addTasbih(name, target) },
                    onDeleteTasbih = { mainViewModel.deleteTasbih(it.id) },
                    onBack = { if (uiState.tasbihList.isNotEmpty()) currentScreen = Screen.COUNTER }
                )
            }
            Screen.COUNTER -> {
                if (activeTasbih != null) {
                    CounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { mainViewModel.incrementActiveTasbih() },
                        onReset = { mainViewModel.resetActiveTasbih() },
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                        onNavigateToList = { currentScreen = Screen.LIST },
                        onNavigateToThemeCustomization = { currentScreen = Screen.THEME_CUSTOMIZATION },
                        onNavigateToCelestial = { currentScreen = Screen.CELESTIAL_COUNTER }
                    )
                } else {
                    LaunchedEffect(Unit){
                        currentScreen = Screen.LIST
                    }
                }
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChange = { mainViewModel.updateTheme(it) },
                    onVibrationToggle = { mainViewModel.toggleVibration(it) },
                    onBack = { currentScreen = Screen.COUNTER }
                )
            }
            Screen.THEME_CUSTOMIZATION -> {
                ThemeCustomizationScreen(
                    settings = uiState.settings,
                    onThemeChange = { mainViewModel.updateTheme(it) },
                    onBack = { currentScreen = Screen.COUNTER }
                )
            }
            Screen.CELESTIAL_COUNTER -> {
                if (activeTasbih != null) {
                    CelestialCounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { mainViewModel.incrementActiveTasbih() },
                        onReset = { mainViewModel.resetActiveTasbih() },
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