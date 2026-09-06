package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dynamic Folder Icon Generator using SVG / Canvas shapes.
 * The icon visually expands, layers, and morphs in detail and scale
 * proportionally based on the number of notes contained within the folder.
 */
@Composable
fun DynamicFolderIcon(
    iconName: String,
    noteCount: Int,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    // Dynamic scaling based on file count
    val targetSize: Dp = when {
        noteCount == 0 -> 34.dp
        noteCount <= 2 -> 38.dp
        noteCount <= 5 -> 44.dp
        else -> 50.dp
    }

    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "FolderIconSize"
    )

    val detailTier: Int = when {
        noteCount == 0 -> 0 // Minimal / Empty
        noteCount <= 2 -> 1 // Light (1-2 notes)
        noteCount <= 5 -> 2 // Medium (3-5 notes)
        else -> 3           // Dense / Heavy (6+ notes)
    }

    Box(
        modifier = modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(animatedSize)) {
            val w = size.width
            val h = size.height

            when (iconName.lowercase()) {
                "calculate", "calculator" -> drawCalculatorIcon(w, h, tintColor, detailTier)
                "book", "menubook", "math" -> drawBookStackIcon(w, h, tintColor, detailTier)
                "science", "flask", "biotech" -> drawScienceFlaskIcon(w, h, tintColor, detailTier)
                "palette", "art" -> drawPaletteIcon(w, h, tintColor, detailTier)
                "code", "terminal" -> drawCodeTerminalIcon(w, h, tintColor, detailTier)
                "calendar", "schedule", "event_note" -> drawCalendarPlannerIcon(w, h, tintColor, detailTier)
                "music", "musicnote" -> drawMusicWaveIcon(w, h, tintColor, detailTier)
                "star", "priority" -> drawStarIcon(w, h, tintColor, detailTier)
                else -> drawSleeveFolderIcon(w, h, tintColor, detailTier)
            }
        }
    }
}

// 1. DYNAMIC CALCULATOR: SVG Shape scaling from simple body to complex scientific grid
private fun DrawScope.drawCalculatorIcon(w: Float, h: Float, color: Color, tier: Int) {
    val bodyCorner = 6f
    // Calculator Outer Body
    drawRoundRect(
        color = color.copy(alpha = 0.22f),
        size = Size(w, h),
        cornerRadius = CornerRadius(bodyCorner, bodyCorner)
    )
    drawRoundRect(
        color = color,
        size = Size(w, h),
        cornerRadius = CornerRadius(bodyCorner, bodyCorner),
        style = Stroke(width = 2.2f)
    )

    // LCD Screen at Top
    val screenH = h * 0.26f
    val screenPad = w * 0.12f
    val screenW = w - (screenPad * 2f)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(screenPad, h * 0.12f),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset(screenPad, h * 0.12f),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 1.2f)
    )

    // LCD Content line
    if (tier >= 1) {
        drawLine(
            color = color,
            start = Offset(screenPad + 4f, h * 0.12f + screenH * 0.5f),
            end = Offset(screenPad + screenW - 6f, h * 0.12f + screenH * 0.5f),
            strokeWidth = 1.5f
        )
    }

    // Dynamic Keypad Grid: Expands as file count grows
    val keyStartY = h * 0.46f
    val keyH = (h - keyStartY - (h * 0.1f))
    val rows = if (tier >= 2) 3 else 2
    val cols = if (tier >= 3) 4 else 3

    val btnW = (screenW - ((cols - 1) * 3f)) / cols
    val btnH = (keyH - ((rows - 1) * 3f)) / rows

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val bx = screenPad + c * (btnW + 3f)
            val by = keyStartY + r * (btnH + 3f)
            val isAction = (c == cols - 1) || (r == rows - 1 && c == 0)

            drawRoundRect(
                color = if (isAction) color else color.copy(alpha = 0.45f),
                topLeft = Offset(bx, by),
                size = Size(btnW, btnH),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
        }
    }

    // Solar Panel or Tier 3 Integral Accent
    if (tier >= 3) {
        drawCircle(
            color = color,
            radius = 2.5f,
            center = Offset(w - screenPad - 4f, h * 0.12f + 4f)
        )
    }
}

// 2. DYNAMIC BOOK STACK: Visually stacks 1 to 4 textbooks with ribbons and spines
private fun DrawScope.drawBookStackIcon(w: Float, h: Float, color: Color, tier: Int) {
    val count = when (tier) {
        0 -> 1
        1 -> 2
        2 -> 3
        else -> 4
    }

    val bookH = (h * 0.65f) / count.toFloat()
    val startY = h * 0.88f - (count * bookH)

    for (i in 0 until count) {
        val y = startY + (i * bookH)
        val inset = if (i % 2 == 1) 3f else 0f
        val bw = w - inset - 4f
        val alpha = 0.5f + (i * 0.15f)

        // Book Cover
        drawRoundRect(
            color = color.copy(alpha = alpha.coerceIn(0.3f, 0.95f)),
            topLeft = Offset(4f + inset, y),
            size = Size(bw, bookH - 2f),
            cornerRadius = CornerRadius(2.5f, 2.5f)
        )
        // Book Spine Highlight
        drawRoundRect(
            color = color,
            topLeft = Offset(4f + inset, y),
            size = Size(bw, bookH - 2f),
            cornerRadius = CornerRadius(2.5f, 2.5f),
            style = Stroke(width = 1.5f)
        )
        // Book Pages Edge
        drawRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(bw - 4f + inset, y + 2f),
            size = Size(4f, bookH - 6f)
        )
    }

    // Ribbon Bookmark peeking out for 2+ books
    if (tier >= 1) {
        val ribbonPath = Path().apply {
            moveTo(w * 0.45f, startY)
            lineTo(w * 0.45f, startY - 8f)
            lineTo(w * 0.52f, startY - 5f)
            lineTo(w * 0.59f, startY - 8f)
            lineTo(w * 0.59f, startY)
            close()
        }
        drawPath(ribbonPath, color = color)
    }
}

// 3. DYNAMIC SCIENCE FLASK: Erlenmeyer flask with rising fluid levels and bubbles
private fun DrawScope.drawScienceFlaskIcon(w: Float, h: Float, color: Color, tier: Int) {
    val neckW = w * 0.26f
    val neckH = h * 0.32f
    val neckLeft = (w - neckW) / 2f

    // Outer Flask Glass Body
    val flaskPath = Path().apply {
        moveTo(neckLeft, 4f)
        lineTo(neckLeft + neckW, 4f)
        lineTo(neckLeft + neckW, neckH)
        lineTo(w - 3f, h - 4f)
        lineTo(3f, h - 4f)
        lineTo(neckLeft, neckH)
        close()
    }

    // Glass Outline
    drawPath(
        path = flaskPath,
        color = color.copy(alpha = 0.3f),
        style = Fill
    )
    drawPath(
        path = flaskPath,
        color = color,
        style = Stroke(width = 2f, join = StrokeJoin.Round)
    )

    // Fluid Level based on Note Count Tier
    val fluidFillPercent = when (tier) {
        0 -> 0.15f
        1 -> 0.35f
        2 -> 0.55f
        else -> 0.78f
    }

    val liquidTopY = (h - 4f) - ((h - neckH) * fluidFillPercent)
    val factor = (liquidTopY - neckH) / (h - 4f - neckH)
    val liquidLeft = neckLeft - (neckLeft - 3f) * (1f - factor)
    val liquidRight = (neckLeft + neckW) + (w - 3f - (neckLeft + neckW)) * (1f - factor)

    val liquidPath = Path().apply {
        moveTo(liquidLeft, liquidTopY)
        lineTo(liquidRight, liquidTopY)
        lineTo(w - 4f, h - 5f)
        lineTo(4f, h - 5f)
        close()
    }
    drawPath(liquidPath, color = color.copy(alpha = 0.65f))

    // Bubbles & Measurement ticks for tier 2+
    if (tier >= 2) {
        // Measurement markers
        drawLine(color = color, start = Offset(liquidLeft + 2f, liquidTopY + 4f), end = Offset(liquidLeft + 7f, liquidTopY + 4f), strokeWidth = 1.2f)
        drawLine(color = color, start = Offset(liquidLeft + 2f, liquidTopY + 10f), end = Offset(liquidLeft + 8f, liquidTopY + 10f), strokeWidth = 1.2f)

        // Floating bubbles
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 2.2f, center = Offset(w * 0.48f, liquidTopY + 8f))
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 1.6f, center = Offset(w * 0.62f, liquidTopY + 13f))
    }
}

// 4. DYNAMIC ART PALETTE: Artist palette with color wells scaling with file density
private fun DrawScope.drawPaletteIcon(w: Float, h: Float, color: Color, tier: Int) {
    drawOval(
        color = color.copy(alpha = 0.25f),
        topLeft = Offset(2f, 4f),
        size = Size(w - 4f, h - 8f)
    )
    drawOval(
        color = color,
        topLeft = Offset(2f, 4f),
        size = Size(w - 4f, h - 8f),
        style = Stroke(width = 2f)
    )

    // Thumb Hole
    drawCircle(
        color = Color.White,
        radius = w * 0.1f,
        center = Offset(w * 0.72f, h * 0.58f)
    )
    drawCircle(
        color = color,
        radius = w * 0.1f,
        center = Offset(w * 0.72f, h * 0.58f),
        style = Stroke(width = 1.5f)
    )

    // Paint wells
    val dots = when (tier) {
        0 -> 1
        1 -> 2
        2 -> 3
        else -> 5
    }
    val dotCoords = listOf(
        Offset(w * 0.32f, h * 0.28f),
        Offset(w * 0.52f, h * 0.25f),
        Offset(w * 0.24f, h * 0.48f),
        Offset(w * 0.28f, h * 0.68f),
        Offset(w * 0.48f, h * 0.75f)
    )
    for (i in 0 until dots.coerceAtMost(dotCoords.size)) {
        drawCircle(color = color, radius = 3.2f, center = dotCoords[i])
    }
}

// 5. DYNAMIC CODE TERMINAL: Code prompt with syntax lines
private fun DrawScope.drawCodeTerminalIcon(w: Float, h: Float, color: Color, tier: Int) {
    drawRoundRect(
        color = color.copy(alpha = 0.2f),
        size = Size(w, h),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = color,
        size = Size(w, h),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 2f)
    )
    // Terminal Top Header Bar
    drawRect(color = color.copy(alpha = 0.35f), topLeft = Offset(0f, 0f), size = Size(w, h * 0.28f))
    drawCircle(color = color, radius = 2f, center = Offset(6f, h * 0.14f))
    drawCircle(color = color.copy(alpha = 0.7f), radius = 2f, center = Offset(13f, h * 0.14f))

    // Code prompt ">_"
    val promptPath = Path().apply {
        moveTo(6f, h * 0.46f)
        lineTo(11f, h * 0.54f)
        lineTo(6f, h * 0.62f)
    }
    drawPath(promptPath, color = color, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    // Code lines scaling with note count
    val lineCount = when (tier) {
        0 -> 1
        1 -> 2
        else -> 3
    }
    for (i in 0 until lineCount) {
        val y = h * (0.50f + (i * 0.15f))
        drawLine(
            color = color,
            start = Offset(16f, y),
            end = Offset(w - (8f + (i * 6f)), y),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

// 6. DYNAMIC CALENDAR PLANNER: Spiral binding and dates grid
private fun DrawScope.drawCalendarPlannerIcon(w: Float, h: Float, color: Color, tier: Int) {
    drawRoundRect(
        color = color.copy(alpha = 0.2f),
        size = Size(w, h),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = color,
        size = Size(w, h),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 2f)
    )
    // Calendar Header
    drawRoundRect(
        color = color,
        topLeft = Offset(0f, 0f),
        size = Size(w, h * 0.32f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Spiral Binding Rings
    for (i in 0 until 4) {
        val rx = w * (0.22f + i * 0.20f)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(rx - 1.5f, 0f),
            size = Size(3f, h * 0.16f),
            cornerRadius = CornerRadius(1.5f, 1.5f)
        )
    }

    // Date grid dots scaling with files
    val rows = if (tier >= 2) 3 else 2
    for (r in 0 until rows) {
        for (c in 0 until 4) {
            val dx = w * (0.2f + c * 0.2f)
            val dy = h * (0.45f + r * 0.18f)
            drawCircle(color = color.copy(alpha = 0.6f), radius = 2f, center = Offset(dx, dy))
        }
    }
}

// 7. DYNAMIC MUSIC WAVE: Audio waves and notes
private fun DrawScope.drawMusicWaveIcon(w: Float, h: Float, color: Color, tier: Int) {
    // Musical Note Base
    val noteHeadRadius = 4f
    drawCircle(color = color, radius = noteHeadRadius, center = Offset(w * 0.35f, h * 0.72f))
    drawLine(color = color, start = Offset(w * 0.35f + noteHeadRadius, h * 0.72f), end = Offset(w * 0.35f + noteHeadRadius, h * 0.26f), strokeWidth = 2.2f)

    if (tier >= 1) {
        drawCircle(color = color, radius = noteHeadRadius, center = Offset(w * 0.72f, h * 0.62f))
        drawLine(color = color, start = Offset(w * 0.72f + noteHeadRadius, h * 0.62f), end = Offset(w * 0.72f + noteHeadRadius, h * 0.18f), strokeWidth = 2.2f)
        // Beam
        drawLine(color = color, start = Offset(w * 0.35f + noteHeadRadius, h * 0.26f), end = Offset(w * 0.72f + noteHeadRadius, h * 0.18f), strokeWidth = 3f)
    }

    // Acoustic rings/particles
    if (tier >= 2) {
        drawArc(
            color = color.copy(alpha = 0.5f),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.75f, h * 0.12f),
            size = Size(w * 0.25f, h * 0.4f),
            style = Stroke(width = 1.8f)
        )
    }
}

// 8. DYNAMIC STAR: Multi-pointed shining star with sparkling particles
private fun DrawScope.drawStarIcon(w: Float, h: Float, color: Color, tier: Int) {
    val cx = w / 2f
    val cy = h / 2f
    val outerR = w * 0.45f
    val innerR = w * 0.20f

    val starPath = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = (i * Math.PI / points) - (Math.PI / 2)
        val x = cx + (r * Math.cos(angle)).toFloat()
        val y = cy + (r * Math.sin(angle)).toFloat()
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()

    drawPath(starPath, color = color.copy(alpha = 0.3f), style = Fill)
    drawPath(starPath, color = color, style = Stroke(width = 2f, join = StrokeJoin.Round))

    if (tier >= 2) {
        drawCircle(color = color, radius = 2f, center = Offset(w * 0.85f, h * 0.25f))
        drawCircle(color = color, radius = 1.6f, center = Offset(w * 0.15f, h * 0.75f))
    }
}

// 9. DYNAMIC SLEEVE FOLDER (Default)
private fun DrawScope.drawSleeveFolderIcon(w: Float, h: Float, color: Color, tier: Int) {
    val tabW = w * 0.42f
    val tabH = h * 0.25f

    val folderPath = Path().apply {
        moveTo(2f, tabH)
        lineTo(tabW, tabH)
        lineTo(tabW + 6f, tabH + 5f)
        lineTo(w - 2f, tabH + 5f)
        lineTo(w - 2f, h - 2f)
        lineTo(2f, h - 2f)
        close()
    }
    drawPath(folderPath, color = color.copy(alpha = 0.3f), style = Fill)
    drawPath(folderPath, color = color, style = Stroke(width = 2f, join = StrokeJoin.Round))

    // Sheets peeking inside
    val sheetCount = tier.coerceAtMost(3)
    for (i in 0 until sheetCount) {
        val y = tabH + 8f + (i * 5f)
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(8f, y),
            end = Offset(w - (10f + (i * 4f)), y),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}
