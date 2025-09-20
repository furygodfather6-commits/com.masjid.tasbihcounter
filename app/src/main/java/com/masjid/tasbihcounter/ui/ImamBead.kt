// Path: app/src/main/java/com/masjid/tasbihcounter/ui/ImamBead.kt
package com.masjid.tasbihcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ImamBead(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(28.dp)) {
        val radius = size.width / 2
        val center = Offset(x = radius, y = radius)

        // Silver jaisa gradient
        val silverDark = Color(0xFFAFAFAF)
        val silverLight = Color(0xFFE5E5E5)

        // Parchhaai
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(x = radius + 3, y = radius + 3),
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Base color
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(silverDark, silverLight),
                center = center,
                radius = radius
            ),
            radius = radius
        )

        // Highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
                center = Offset(x = radius * 0.8f, y = radius * 0.8f),
                radius = radius * 0.7f
            ),
            radius = radius,
            center = center
        )

        // Imam par chhota sa design
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = radius * 0.3f,
            style = Stroke(width = 2f)
        )
    }
}