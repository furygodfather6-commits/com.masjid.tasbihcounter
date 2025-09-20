// Path: app/src/main/java/com/masjid/tasbihcounter/ui/theme/Theme.kt
package com.masjid.tasbihcounter.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.masjid.tasbihcounter.ThemeSetting

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), background = Color(0xFF121212), surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C)
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), background = Color(0xFFF0F0F0), surface = Color.White,
    onPrimary = Color.White, onBackground = Color.Black, onSurface = Color.Black,
    surfaceVariant = Color(0xFFE0E0E0)
)

@Composable
fun MasjidTasbihCounterTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when(themeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}