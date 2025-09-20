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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF0F0F0),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val MeccaMidnightColorScheme = darkColorScheme(
    primary = KaabaGold,
    secondary = FajrBlue,
    tertiary = HaramWhite,
    background = MeccaNight,
    surface = StoneGray,
    onPrimary = MeccaNight,
    onSecondary = HaramWhite,
    onTertiary = MeccaNight,
    onBackground = HaramWhite,
    onSurface = HaramWhite
)
private val RetroArcadeColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonCyan,
    background = ArcadeBlack,
    surface = ArcadeGray,
    onPrimary = ArcadeBlack,
    onSecondary = ArcadeBlack,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MasjidTasbihCounterTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeSetting) {
        ThemeSetting.LIGHT -> LightColorScheme
        ThemeSetting.DARK -> DarkColorScheme
        ThemeSetting.MECCA_MIDNIGHT -> MeccaMidnightColorScheme
        ThemeSetting.RETRO_ARCADE -> RetroArcadeColorScheme
        ThemeSetting.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Yahan par naya case add kiya gaya hai
            val isLight = when (themeSetting) {
                ThemeSetting.LIGHT -> true
                ThemeSetting.DARK, ThemeSetting.MECCA_MIDNIGHT, ThemeSetting.RETRO_ARCADE -> false
                ThemeSetting.SYSTEM -> !isSystemInDarkTheme()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}