// Path: app/src/main/java/com/masjid/tasbihcounter/AppSettings.kt
package com.masjid.tasbihcounter

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: ThemeSetting = ThemeSetting.SYSTEM,
    val isVibrationOn: Boolean = true,
    // Neeche di gayi settings hum future mein add karenge, abhi ke liye unki jagah banayi hai
    val isSoundOn: Boolean = false,
    val tapAnywhere: Boolean = true,
    val isLeftHanded: Boolean = false
)

enum class ThemeSetting {
    LIGHT, DARK, SYSTEM
}