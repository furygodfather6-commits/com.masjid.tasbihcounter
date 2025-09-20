// Path: app/src/main/java/com/masjid/tasbihcounter/MainActivity.kt
package com.masjid.tasbihcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masjid.tasbihcounter.ui.theme.MasjidTasbihCounterTheme // <-- Yahan bhi theme ka naam update ho gaya hai

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MasjidTasbihCounterTheme { // <-- Yahan updated theme istemal ho raha hai
                Surface(modifier = Modifier.fillMaxSize()) {
                    TasbihScreen()
                }
            }
        }
    }
}

@Composable
fun TasbihScreen(mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val animatedCount by animateIntAsState(
        targetValue = uiState.count,
        animationSpec = tween(durationMillis = 500),
        label = "countAnimation"
    )
    val progress = (uiState.count.toFloat() / uiState.target.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tasbih Counter",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Target: ${uiState.target}",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressBar(
                progress = animatedProgress,
                strokeWidth = 20.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                foregroundColor = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false, radius = 150.dp),
                        onClick = {
                            mainViewModel.incrementCount()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$animatedCount",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                mainViewModel.resetCount()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }) {
                Text(text = "Reset", fontSize = 16.sp)
            }
            Button(onClick = {
                val nextTarget = when (uiState.target) {
                    33 -> 100
                    100 -> 1000
                    else -> 33
                }
                mainViewModel.setTarget(nextTarget)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }) {
                Text(text = "Set Target", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CircularProgressBar(
    progress: Float,
    strokeWidth: Dp,
    backgroundColor: Color,
    foregroundColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
        drawArc(
            color = foregroundColor,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasbihScreenPreview() {
    MasjidTasbihCounterTheme {
        TasbihScreen()
    }
}