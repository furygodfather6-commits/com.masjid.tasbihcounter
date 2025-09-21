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
import com.masjid.tasbihcounter.*
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeChange: (ThemeSetting) -> Unit,
    onAdvancedThemeChange: (AdvancedTheme) -> Unit,
    onVibrationModeChange: (VibrationMode) -> Unit,
    onSoundModeChange: (SoundMode) -> Unit,
    onVibrationStrengthChange: (Float) -> Unit,
    onCountingSpeedChange: (Float) -> Unit,
    onBackgroundCountingChange: (Boolean) -> Unit,
    onAdditionalButtonControlChange: (AdditionalButtonControl) -> Unit,
    onFullScreenTapChange: (Boolean) -> Unit,
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
                    selectedOption = settings.theme.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedTheme = ThemeSetting.valueOf(it.replace(" ", "_"))
                        onThemeChange(selectedTheme)
                    }
                )
            }
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
                    options = VibrationMode.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.vibrationMode.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedMode = VibrationMode.valueOf(it.replace(" ", "_"))
                        onVibrationModeChange(selectedMode)
                    }
                )
            }
            item {
                SliderSettingItem(
                    title = "Vibration Strength",
                    value = settings.vibrationStrength,
                    onValueChange = onVibrationStrengthChange
                )
            }
            item {
                SegmentedButtonSetting(
                    title = "Sound Mode",
                    options = SoundMode.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.soundMode.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedMode = SoundMode.valueOf(it.replace(" ", "_"))
                        onSoundModeChange(selectedMode)
                    }
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Section 3: Counter Behavior
            item { SettingsGroup("Counter Behavior") }
            item {
                val speedValue = 2.0f - settings.countingSpeed
                val df = DecimalFormat("#.#")
                SliderSettingItem(
                    title = "Counting Speed (${df.format(settings.countingSpeed)}s)",
                    value = speedValue,
                    onValueChange = {
                        onCountingSpeedChange(2.0f - it)
                    },
                    valueRange = 0.5f..1.9f
                )
            }
            item {
                SwitchSettingItem(
                    title = "Background Counting",
                    checked = settings.backgroundCountingEnabled,
                    onCheckedChange = onBackgroundCountingChange
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Section 4: Additional Controls
            item { SettingsGroup("Additional Controls") }
            item {
                SegmentedButtonSetting(
                    title = "Count with Additional Buttons",
                    options = AdditionalButtonControl.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.additionalButtonControl.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedControl = AdditionalButtonControl.valueOf(it.replace(" ", "_"))
                        onAdditionalButtonControlChange(selectedControl)
                    }
                )
            }
            item {
                SwitchSettingItem(
                    title = "Full Screen Tap Counting",
                    checked = settings.fullScreenTapEnabled,
                    onCheckedChange = onFullScreenTapChange
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
fun SliderSettingItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title, fontSize = 16.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
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