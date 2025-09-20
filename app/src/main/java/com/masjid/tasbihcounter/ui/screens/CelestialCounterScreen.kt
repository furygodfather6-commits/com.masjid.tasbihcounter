// Path: app/src/main/java/com/masjid/tasbihcounter/ui/screens/CelestialCounterScreen.kt
package com.masjid.tasbihcounter.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.Tasbih
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelestialCounterScreen(
    tasbih: Tasbih,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val progress = tasbih.count.toFloat() / tasbih.target.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "progressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "celestial_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tasbih.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .systemBarsPadding()
                .clickable {
                    onIncrement()
                    if (settings.isVibrationOn) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Animated Orb
            Canvas(modifier = Modifier.size(300.dp)) {
                drawCircle(
                    color = Color.Cyan.copy(alpha = alpha),
                    radius = size.width / 2,
                    style = Stroke(width = 16.dp.toPx())
                )
            }

            // Tasbih Counter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${tasbih.count}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Text(
                    text = "Target: ${tasbih.target}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}