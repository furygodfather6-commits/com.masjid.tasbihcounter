// Path: app/src/main/java/com/masjid/tasbihcounter/ui/NeumorphicStyle.kt
package com.masjid.tasbihcounter.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neumorphic(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color.White.copy(alpha = 0.5f),
    darkShadowColor: Color = Color.Black.copy(alpha = 0.2f),
    shadowRadius: Dp = 4.dp
) = this.then(
    drawBehind {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val radius = cornerRadius.toPx()
        val blurRadius = shadowRadius.toPx()

        // Dark shadow (neeche/right)
        frameworkPaint.color = android.graphics.Color.TRANSPARENT
        frameworkPaint.setShadowLayer(
            blurRadius,
            blurRadius / 2,
            blurRadius / 2,
            darkShadowColor.hashCode()
        )
        drawRoundRect(
            color = backgroundColor,
            size = this.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )

        // Light shadow (upar/left)
        frameworkPaint.color = android.graphics.Color.TRANSPARENT
        frameworkPaint.setShadowLayer(
            blurRadius,
            -blurRadius / 2,
            -blurRadius / 2,
            lightShadowColor.hashCode()
        )
        drawRoundRect(
            color = backgroundColor,
            size = this.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )
    }
)