package com.masjid.tasbihcounter.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartCounting: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val ivoryBackground = Color(0xFFFBF7EE)
    val tealPrimary = Color(0xFF14937C)
    val amberSecondary = Color(0xFFF2A33A)
    val neutralInk = Color(0xFF1B1F23)
    val jadeSeparator = Color(0xFF0E7C66)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tasbih Counter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = neutralInk,
                        letterSpacing = (28 * 0.01).sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ivoryBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = ivoryBackground,
                contentColor = tealPrimary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on Home */ },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onStartCounting,
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Counter") },
                    label = { Text("Counter") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO: Implement Mosque Screen */ },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Mosque") },
                    label = { Text("Mosque") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        containerColor = ivoryBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            TasbihIllustration(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(1f),
                beadColor1 = tealPrimary,
                beadColor2 = amberSecondary,
                separatorColor = jadeSeparator
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PaginationDots()
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStartCounting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        "Start Counting",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (16 * 0.01).sp
                    )
                }
            }
        }
    }
}

@Composable
fun TasbihIllustration(
    modifier: Modifier = Modifier,
    beadColor1: Color,
    beadColor2: Color,
    separatorColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "locket_swing")
    val swingAngle by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swingAngle"
    )

    Canvas(modifier = modifier) {
        val radius = size.width / 2.5f
        val beadRadius = 18f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(beadColor1.copy(alpha = 0.12f), Color.Transparent),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f
        )

        for (i in 0 until 99) {
            val angle = i * (360f / 105)
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            val beadColor = when {
                i % 11 == 10 -> separatorColor
                (i / 11) % 2 == 0 -> beadColor1
                else -> beadColor2
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), beadColor, beadColor.copy(alpha = 0.7f)),
                    center = Offset(x - 5, y - 5),
                    radius = beadRadius
                ),
                radius = beadRadius,
                center = Offset(x, y)
            )
        }

        rotate(degrees = swingAngle, pivot = Offset(center.x, center.y - radius)) {
            val locketCenter = Offset(center.x, center.y)
            val locketRadius = size.width / 5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD9A441), Color(0xFFC99431)),
                    center = locketCenter,
                    radius = locketRadius
                ),
                radius = locketRadius,
                center = locketCenter
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = locketRadius,
                center = locketCenter,
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
fun PaginationDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == 0) Color(0xFF14937C) else Color(0xFF9098A1).copy(
                            alpha = 0.3f
                        )
                    )
            )
        }
    }
}