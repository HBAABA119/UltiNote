package com.example.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.data.model.DrawingLayer
import com.example.data.model.DrawingStroke
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.StickerAnnotation
import com.example.data.model.TextAnnotation
import com.example.data.model.ToolType

data class RulerState(
    val isVisible: Boolean = false,
    val center: Offset = Offset(400f, 600f),
    val angle: Float = 0f, // in degrees
    val length: Float = 500f,
    val height: Float = 90f
)

data class PageSnapshot(
    val strokes: List<DrawingStroke>,
    val shapes: List<ShapeAnnotation>,
    val textBlocks: List<TextAnnotation>,
    val stickers: List<StickerAnnotation>,
    val layers: List<DrawingLayer> = emptyList()
)

object PalettePresets {
    val Inks = listOf(
        0xFF1E2822L, // Deep Forest Graphite
        0xFF2D3748L, // Charcoal Ink
        0xFF1A365DL, // Midnight Navy
        0xFF742A2AL, // Burgundy Crimson
        0xFF4A6B56L, // Matcha Sage
        0xFF2C5282L, // Sapphire Blue
        0xFF6B46C1L, // Royal Purple
        0xFF975A16L  // Amber Oak
    )

    val Pastels = listOf(
        0xFFD4708AL, // Sakura Blossom
        0xFF7E6AB5L, // Lavender Mist
        0xFF5A7A66L, // Matcha Latte
        0xFFE9B072L, // Honey Oat
        0xFF5C9EADL, // Glacial Blue
        0xFFE2847AL, // Coral Peach
        0xFF7EA04DL, // Pistachio
        0xFF9E768FL  // Heather Mauve
    )

    val Highlighters = listOf(
        0x55F6E05EL, // Sunshine Yellow
        0x5568D391L, // Mint Green
        0x5563B3EDL, // Sky Cyan
        0x55F687B3L, // Blossom Pink
        0x55B794F4L, // Soft Lilac
        0x55FBD38DL  // Warm Peach
    )

    val StrokeWidths = listOf(1.5f, 3.0f, 5.5f, 9.0f, 16.0f)
}
