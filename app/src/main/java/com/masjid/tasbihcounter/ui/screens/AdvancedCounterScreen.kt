package com.masjid.tasbihcounter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.AdvancedTheme
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.Tasbih
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Ripple(
    val center: Offset,
    val creationTime: Long = System.currentTimeMillis()
)

data class KoiFish(
    var x: Float,
    var y: Float,
    var angle: Float,
    val speed: Float = 2f,
    val color: Color
)

@Composable
fun AdvancedCounterScreen(
    tasbih: Tasbih,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Crossfade(targetState = settings.advancedTheme, label = "AdvancedThemeCrossfade") { theme ->
        when (theme) {
            AdvancedTheme.SERENE_POND -> SerenePondTheme(tasbih, onIncrement, onReset, onBack)
            else -> SerenePondTheme(tasbih, onIncrement, onReset, onBack)
        }
    }
}

@Composable
fun SerenePondTheme(
    tasbih: Tasbih,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val ripples = remember { mutableStateListOf<Ripple>() }
    val coroutineScope = rememberCoroutineScope()
    var showButtons by remember { mutableStateOf(false) }

    val waterColor = Color(0xFF0A2E36)
    val sandColor = Color(0xFFC2B280)
    val waterGradient = Brush.verticalGradient(
        colors = listOf(waterColor.copy(alpha = 0.8f), waterColor)
    )

    val koiFishList = remember {
        mutableStateListOf<KoiFish>().apply {
            repeat(5) {
                add(KoiFish(
                    x = Random.nextFloat() * 1000,
                    y = Random.nextFloat() * 2000,
                    angle = Random.nextFloat() * 360f,
                    color = listOf(Color(0xFFFFA500), Color.White, Color.Red).random()
                ))
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pond_animation")
    val animationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "animationTime"
    )

    LaunchedEffect(Unit) {
        showButtons = true
        delay(3000)
        showButtons = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(waterGradient)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onIncrement()
                    ripples.add(Ripple(center = offset))
                    coroutineScope.launch {
                        delay(1500)
                        ripples.removeIf { it.center == offset }
                    }
                    showButtons = false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(sandColor)

            if (animationTrigger > 0) {
                koiFishList.forEach { fish ->
                    fish.x += cos(Math.toRadians(fish.angle.toDouble())).toFloat() * fish.speed
                    fish.y += sin(Math.toRadians(fish.angle.toDouble())).toFloat() * fish.speed
                    if (fish.x < 0 || fish.x > size.width || fish.y < 0 || fish.y > size.height) {
                        fish.angle = (fish.angle + 180 + Random.nextInt(-30, 30)) % 360
                    }
                    drawCircle(fish.color, radius = 25f, center = Offset(fish.x, fish.y))
                }
            }

            drawRect(waterGradient)

            ripples.forEach { ripple ->
                val elapsedTime = System.currentTimeMillis() - ripple.creationTime
                val progress = (elapsedTime / 1500f).coerceIn(0f, 1f)
                val currentRadius = size.width * progress
                val alpha = 1f - progress
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.5f),
                    radius = currentRadius,
                    center = ripple.center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            }

            val stoneRadius = size.minDimension / 4f
            drawCircle(color = Color(0xFF424242), radius = stoneRadius, center = center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent),
                    center = center.copy(y = center.y - stoneRadius * 0.5f),
                    radius = stoneRadius
                ),
                radius = stoneRadius,
                center = center
            )

            drawIntoCanvas {
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = 45.sp.toPx()
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
                }
                it.nativeCanvas.drawText(
                    tasbih.count.toString(),
                    center.x,
                    center.y - 10.sp.toPx(),
                    paint
                )
                paint.textSize = 20.sp.toPx()
                it.nativeCanvas.drawText(
                    "Target: ${tasbih.target}",
                    center.x,
                    center.y + 30.sp.toPx(),
                    paint
                )
            }
        }

        AnimatedVisibility(visible = showButtons, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Button(onClick = onBack, shape = CircleShape) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        AnimatedVisibility(visible = showButtons, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Button(onClick = onReset, shape = CircleShape) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
            }
        }
    }
}