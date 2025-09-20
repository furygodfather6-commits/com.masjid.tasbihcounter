// Path: app/src/main/java/com/masjid/tasbihcounter/ui/TasbihBead.kt
package com.masjid.tasbihcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TasbihBead(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    isCounted: Boolean = false,
    beadColor: Color
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = size.toPx() / 2
        val center = Offset(x = radius, y = radius)

        val finalColor = if (isCounted) beadColor else Color.DarkGray

        // Parchhaai (Shadow)
        val shadowBrush = Brush.radialGradient(
            colors = listOf(finalColor.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(x = radius + 2, y = radius + 2),
            radius = radius
        )
        drawCircle(brush = shadowBrush, radius = radius, center = center)


        // Daane ka base color
        val baseBrush = Brush.radialGradient(
            colors = listOf(finalColor.copy(alpha = 0.8f), finalColor),
            center = center,
            radius = radius
        )
        drawCircle(brush = baseBrush, radius = radius, center = center)

        // Upar se aati hui roshni (Highlight)
        val highlightBrush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(x = radius * 0.8f, y = radius * 0.8f),
            radius = radius * 0.8f
        )
        drawCircle(brush = highlightBrush, radius = radius, center = center)
    }
}