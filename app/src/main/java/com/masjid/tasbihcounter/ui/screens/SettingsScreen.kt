package com.masjid.tasbihcounter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.AdvancedTheme
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeChange: (ThemeSetting) -> Unit,
    onAdvancedThemeChange: (AdvancedTheme) -> Unit, // New
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Section 1: Appearance
            item { SettingsGroup("Appearance") }
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
            // ## YAHAN PAR NAYA OPTION ADD KIYA GAYA HAI ##
            item {
                SegmentedButtonSetting(
                    title = "Advanced Counter Theme",
                    options = AdvancedTheme.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.advancedTheme.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedTheme = AdvancedTheme.valueOf(it.replace(" ", "_"))
                        onAdvancedThemeChange(selectedTheme)
                    }
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Section 2: Audio & Feedback
            item { SettingsGroup("Audio & Feedback") }
            item {
                SegmentedButtonSetting(
                    title = "Vibration Mode",
                    options = listOf("On Tap", "On Target", "Off"),
                    selectedOption = "On Tap", // Placeholder
                    onOptionSelect = { /* TODO */ }
                )
            }
            item {
                var sliderPosition by remember { mutableStateOf(0.5f) }
                SliderSettingItem(
                    title = "Vibration Strength",
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it }
                )
            }
            item {
                SegmentedButtonSetting(
                    title = "Sound Mode",
                    options = listOf("On Tap", "On Target", "Off"),
                    selectedOption = "Off", // Placeholder
                    onOptionSelect = { /* TODO */ }
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Section 3: Counter Behavior
            item { SettingsGroup("Counter Behavior") }
            item {
                SwitchSettingItem(
                    title = "Limit Count Setting",
                    checked = false, // Placeholder
                    onCheckedChange = { /* TODO */ }
                )
            }
            item {
                SegmentedButtonSetting(
                    title = "Counting Speed",
                    options = listOf("Slow", "Normal", "Fast"),
                    selectedOption = "Normal", // Placeholder
                    onOptionSelect = { /* TODO */ }
                )
            }
            item {
                SwitchSettingItem(
                    title = "Background Counting",
                    checked = false, // Placeholder
                    onCheckedChange = { /* TODO */ }
                )
            }
            item {
                ClickableSettingItem(
                    title = "Additional Settings",
                    onClick = { /* TODO: Navigate to another screen */ }
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
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onOptionSelect(label) },
                    selected = label.replace(" ", "_") == selectedOption.replace(" ", "_")
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

@Composable
fun SliderSettingItem(title: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title, fontSize = 16.sp)
        Slider(
            value = value,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun ClickableSettingItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}