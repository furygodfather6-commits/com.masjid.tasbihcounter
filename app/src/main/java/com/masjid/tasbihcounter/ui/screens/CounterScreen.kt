package com.masjid.tasbihcounter.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.TasbihSequenceState
import com.masjid.tasbihcounter.ThemeSetting
import com.masjid.tasbihcounter.ui.theme.RetroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Data class for the new animation
data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    sequenceState: TasbihSequenceState,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemeCustomization: () -> Unit,
    onNavigateToAdvancedCounter: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pulse = remember { Animatable(1f) }
    var isAutoCounting by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoCounting) {
        while (isAutoCounting) {
            onIncrement()
            if (settings.isVibrationOn) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            delay(1000L)
        }
    }

    val currentTasbih = sequenceState.currentTasbih

    if (currentTasbih == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Tasbih loaded.")
        }
        return
    }

    val animatedCount by animateIntAsState(
        targetValue = currentTasbih.count,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "countAnimation"
    )

    val progress = if (sequenceState.overallTarget > 0) {
        sequenceState.totalCount.toFloat() / sequenceState.overallTarget.toFloat()
    } else {
        0f
    }
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
                    text = currentTasbih.name,
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
                            if (!isAutoCounting) {
                                onIncrement()
                                if (settings.isVibrationOn) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                coroutineScope.launch {
                                    pulse.snapTo(1.1f)
                                    pulse.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (settings.theme) {
                    ThemeSetting.GALAXY_DREAM -> {
                        GalaxyDreamRing(
                            modifier = Modifier.size(300.dp),
                            progress = animatedProgress,
                            pulse = pulse.value
                        )
                    }
                    else -> {
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
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Cycle: ${sequenceState.cycleCount}",
                        style = typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$animatedCount",
                        style = typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Total: ${sequenceState.totalCount} / ${sequenceState.overallTarget}",
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        isAutoCounting = false
                        onReset()
                    }) {
                        Text(
                            "Reset",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { isAutoCounting = !isAutoCounting }) {
                        Icon(
                            imageVector = if (isAutoCounting) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Auto-count",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalaxyDreamRing(modifier: Modifier = Modifier, progress: Float, pulse: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val stars = remember {
        List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.5f + 0.5f,
                alpha = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulse
        scaleY = pulse
    }) {
        val width = size.width
        val height = size.height

        stars.forEach { star ->
            drawCircle(
                color = Color.White,
                center = Offset(star.x * width, star.y * height),
                radius = star.radius,
                alpha = star.alpha
            )
        }

        drawIntoCanvas {
            val paint = Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = 20f
            }
            val frameworkPaint = paint.asFrameworkPaint()

            frameworkPaint.color = Color.Transparent.toArgb()
            frameworkPaint.setShadowLayer(30f, 0f, 0f, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f).toArgb())
            it.drawCircle(center, size.minDimension / 2.5f, paint)

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 25f, cap = StrokeCap.Round)
            )

            rotate(rotation) {
                drawArc(
                    color = MaterialTheme.colorScheme.tertiary,
                    startAngle = 0f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f)))
                )
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