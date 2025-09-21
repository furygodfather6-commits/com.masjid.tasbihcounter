package com.masjid.tasbihcounter.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjid.tasbihcounter.*
import com.masjid.tasbihcounter.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Data class for the new animation
data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)

data class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var angle: Float,
    var alpha: Float,
    val color: Color
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pulse = remember { Animatable(1f) }
    var isAutoCounting by remember { mutableStateOf(false) }

    // ## COUNTING SPEED KI LOGIC UPDATE KI GAYI HAI ##
    val autoCountingDelay = (settings.countingSpeed * 1000).toLong()

    LaunchedEffect(isAutoCounting, autoCountingDelay) { // Delay ko dependency banaya
        while (isAutoCounting) {
            onIncrement()
            if (settings.vibrationMode == VibrationMode.AUTO_COUNT || settings.vibrationMode == VibrationMode.TAP_AUTO_COUNT) {
                vibrate(context, settings.vibrationStrength)
            }
            delay(autoCountingDelay)
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
        containerColor = if (settings.theme == ThemeSetting.NEBULA_BURST) NebulaDeepSpace else MaterialTheme.colorScheme.background
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
                                // ## VIBRATION LOGIC UPDATE ##
                                if (settings.vibrationMode == VibrationMode.TAP || settings.vibrationMode == VibrationMode.TAP_AUTO_COUNT) {
                                    vibrate(context, settings.vibrationStrength)
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
                    ThemeSetting.NEBULA_BURST -> {
                        NebulaBurstRing(
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

// Helper function to vibrate with strength
private fun vibrate(context: Context, strength: Float) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val amplitude = (255 * strength).toInt().coerceIn(1, 255)
        vibrator.vibrate(VibrationEffect.createOneShot(100, amplitude))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}

@Composable
fun NebulaBurstRing(modifier: Modifier = Modifier, progress: Float, pulse: Float) {
    val particles = remember { mutableStateListOf<Particle>() }
    val animationTrigger by rememberInfiniteTransition(label = "nebula_animation").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "animationTrigger"
    )

    LaunchedEffect(animationTrigger) {
        if (particles.size < 100) {
            particles.add(
                Particle(
                    x = 0.5f,
                    y = 0.5f,
                    speed = Random.nextFloat() * 0.02f + 0.01f,
                    angle = Random.nextFloat() * 360f,
                    alpha = 1f,
                    color = listOf(SupernovaRed, CosmicDust, HyperdriveBlue).random()
                )
            )
        }

        particles.forEach {
            it.x += cos(Math.toRadians(it.angle.toDouble())).toFloat() * it.speed
            it.y += sin(Math.toRadians(it.angle.toDouble())).toFloat() * it.speed
            it.alpha -= 0.01f
        }
        particles.removeAll { it.alpha <= 0 }
    }

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulse
        scaleY = pulse
    }) {
        val width = size.width
        val height = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SupernovaRed.copy(alpha = 0.1f),
                    CosmicDust.copy(alpha = 0.1f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / 2.2f,
        )

        particles.forEach {
            drawCircle(
                color = it.color,
                center = Offset(it.x * width, it.y * height),
                radius = 2.dp.toPx(),
                alpha = it.alpha
            )
        }

        drawArc(
            brush = Brush.sweepGradient(
                listOf(
                    SupernovaRed,
                    CosmicDust,
                    HyperdriveBlue,
                    SupernovaRed
                )
            ),
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round)
        )
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

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

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.5f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / 2.5f,
        )

        drawArc(
            brush = Brush.sweepGradient(
                listOf(
                    primaryColor,
                    secondaryColor,
                    primaryColor
                )
            ),
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = 25f, cap = StrokeCap.Round)
        )

        this.rotate(degrees = rotation) {
            drawArc(
                color = tertiaryColor,
                startAngle = 0f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f)))
            )
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