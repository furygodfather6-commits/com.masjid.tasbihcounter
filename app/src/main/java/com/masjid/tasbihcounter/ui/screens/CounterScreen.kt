// Path: app/src/main/java/com/masjid/tasbihcounter/ui/screens/CounterScreen.kt
package com.masjid.tasbihcounter.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.Tasbih
import com.masjid.tasbihcounter.ThemeSetting // Yeh line jodi gayi hai
import com.masjid.tasbihcounter.ui.theme.RetroTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    tasbih: Tasbih,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemeCustomization: () -> Unit,
    onNavigateToCelestial: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pulse = remember { Animatable(1f) }

    val animatedCount by animateIntAsState(
        targetValue = tasbih.count,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "countAnimation"
    )
    val progress = tasbih.count.toFloat() / tasbih.target.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "progressAnimation"
    )

    val typography = if (settings.theme == ThemeSetting.RETRO_ARCADE) {
        RetroTypography
    } else {
        MaterialTheme.typography
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = onNavigateToList) {
                        Icon(Icons.Default.List, contentDescription = "Tasbih List", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToThemeCustomization) {
                        Icon(Icons.Filled.ColorLens, contentDescription = "Customize Theme", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToCelestial) {
                        Icon(Icons.Default.Star, contentDescription = "Celestial Counter", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tasbih.name,
                    style = typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onIncrement()
                            if (settings.isVibrationOn) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            coroutineScope.launch {
                                pulse.snapTo(1.1f)
                                pulse.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AuroraRing(
                    modifier = Modifier.size(300.dp),
                    progress = animatedProgress,
                    pulse = pulse.value,
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedCount",
                        style = typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Target: ${tasbih.target}",
                        style = typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onReset) {
                    Text("Reset", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AuroraRing(
    modifier: Modifier = Modifier,
    progress: Float,
    pulse: Float,
    colors: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val brush = Brush.sweepGradient(colors)

    val innerRingColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulse
        scaleY = pulse
        rotationZ = rotation
    }) {
        val strokeWidth = 80f

        drawArc(
            brush = Brush.sweepGradient(colors.map { it.copy(alpha = 0.3f) }),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth + 40f)
        )

        drawArc(
            color = innerRingColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}