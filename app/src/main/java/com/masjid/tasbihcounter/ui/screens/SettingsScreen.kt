// Path: app/src/main/java/com/masjid/tasbihcounter/ui/screens/SettingsScreen.kt
package com.masjid.tasbihcounter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeChange: (ThemeSetting) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item { SettingsGroup("General Settings") }
            item {
                SegmentedButtonSetting(
                    title = "App Theme",
                    options = ThemeSetting.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.theme.name.replace(" ", "_"),
                    onOptionSelect = {
                        val selectedTheme = ThemeSetting.valueOf(it.replace(" ", "_"))
                        onThemeChange(selectedTheme)
                    }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingsGroup("Feedback Settings") }
            item {
                SwitchSettingItem(
                    title = "Vibration on Tap",
                    checked = settings.isVibrationOn,
                    onCheckedChange = onVibrationToggle
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonSetting(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onOptionSelect(label) },
                    selected = label == selectedOption
                ) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SwitchSettingItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = null)
    }
}