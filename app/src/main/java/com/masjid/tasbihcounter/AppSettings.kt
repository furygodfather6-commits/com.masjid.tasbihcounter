package com.masjid.tasbihcounter

import kotlinx.serialization.Serializable

enum class AdvancedTheme {
    SERENE_POND,
    ETERNAL_CLOCK,
    INK_FLOW
}

@Serializable
data class AppSettings(
    val theme: ThemeSetting = ThemeSetting.GALAXY_DREAM, // Default is now the new theme
    val advancedTheme: AdvancedTheme = AdvancedTheme.ETERNAL_CLOCK,
    val isVibrationOn: Boolean = true,
    val isSoundOn: Boolean = false,
    val tapAnywhere: Boolean = true,
    val isLeftHanded: Boolean = false
)

enum class ThemeSetting {
    LIGHT, SYSTEM, RETRO_ARCADE, GALAXY_DREAM, NEBULA_BURST // New theme added
}