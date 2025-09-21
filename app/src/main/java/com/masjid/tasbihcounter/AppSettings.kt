package com.masjid.tasbihcounter

import kotlinx.serialization.Serializable

enum class AdvancedTheme {
    SERENE_POND,
    ETERNAL_CLOCK,
    INK_FLOW
}

enum class VibrationMode {
    OFF,
    TAP,
    AUTO_COUNT,
    TAP_AUTO_COUNT
}

enum class SoundMode {
    OFF,
    TAP,
    AUTO_COUNT,
    TAP_AUTO_COUNT
}

enum class AdditionalButtonControl {
    OFF,
    EARPHONE,
    VOLUME
}

@Serializable
data class AppSettings(
    val theme: ThemeSetting = ThemeSetting.GALAXY_DREAM,
    val advancedTheme: AdvancedTheme = AdvancedTheme.ETERNAL_CLOCK,
    val vibrationMode: VibrationMode = VibrationMode.TAP,
    val soundMode: SoundMode = SoundMode.OFF,
    val vibrationStrength: Float = 0.5f,
    val tapAnywhere: Boolean = true,
    val isLeftHanded: Boolean = false,
    val countingSpeed: Float = 1.0f, // Changed to Float for slider
    val backgroundCountingEnabled: Boolean = false,
    val additionalButtonControl: AdditionalButtonControl = AdditionalButtonControl.OFF,
    val fullScreenTapEnabled: Boolean = true
)

enum class ThemeSetting {
    LIGHT, SYSTEM, RETRO_ARCADE, GALAXY_DREAM, NEBULA_BURST
}