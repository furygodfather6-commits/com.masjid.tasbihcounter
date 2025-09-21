package com.masjid.tasbihcounter

import kotlinx.serialization.Serializable

enum class AdvancedTheme {
    SERENE_POND,
    ETERNAL_CLOCK,
    INK_FLOW
}

@Serializable
data class AppSettings(
    val theme: ThemeSetting = ThemeSetting.SYSTEM,
    val advancedTheme: AdvancedTheme = AdvancedTheme.SERENE_POND,
    val isVibrationOn: Boolean = true,
    val isSoundOn: Boolean = false,
    val tapAnywhere: Boolean = true,
    val isLeftHanded: Boolean = false
)

enum class ThemeSetting {
    LIGHT, DARK, SYSTEM, MECCA_MIDNIGHT, RETRO_ARCADE
}