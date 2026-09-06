package com.ultinote.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

enum class PaperTemplate {
    BLANK,
    RULED,
    GRID,
    DOTTED,
    CORNELL,
    PLANNER_WEEKLY,
    DARK_PAPER,
    ENGINEERING_GRID,
    MUSIC_STAVE,
    PASTEL_WASH
}

enum class ToolType {
    PEN_BALLPOINT,
    PEN_FOUNTAIN,
    PEN_BRUSH,
    HIGHLIGHTER,
    ERASER_STROKE,
    ERASER_PRECISION,
    RULER,
    SHAPE,
    LASSO,
    TEXT,
    STICKER,
    IMAGE
}

enum class ShapeType {
    LINE,
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    ARROW,
    STAR
}

enum class CoverStyle {
    BOTANICAL,
    SAKURA,
    CELESTIAL,
    LEATHER_BROWN,
    MINIMAL_MATCHA,
    LAVENDER_MIST,
    OBSIDIAN_SLATE
}

enum class AppThemeOption(val displayName: String, val description: String) {
    MATCHA_CREAM("Matcha Cream", "Warm organic sage, ivory paper & natural tones"),
    SAKURA_PASTEL("Sakura Pastel", "Soft cherry blossom pink, rose & cream"),
    LAVENDER_TWILIGHT("Lavender Twilight", "Periwinkle, soft lilac & dreamy violet"),
    OBSIDIAN_DARK("Obsidian Dark", "Luxury slate graphite & vibrant pastel neon ink"),
    VINTAGE_PAPER("Vintage Parchment", "Warm antique sepia, aged paper & brass"),
    NORDIC_FROST("Nordic Frost", "Minimalist glacial blue, clean white & deep indigo")
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null,
    val colorHex: String = "#8DA399",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val folderId: String? = null,
    val title: String,
    val coverStyle: CoverStyle = CoverStyle.BOTANICAL,
    val defaultTemplate: PaperTemplate = PaperTemplate.RULED,
    val pageCount: Int = 1,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val tags: String = "", // Comma-separated
    val linkedDate: String? = null, // "YYYY-MM-DD" for calendar planner
    val isPdf: Boolean = false,
    val pdfFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

enum class LayerBlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    DARKEN,
    LIGHTEN,
    OVERLAY
}

data class DrawingLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Layer 1",
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f,
    val blendMode: LayerBlendMode = LayerBlendMode.NORMAL,
    val isLocked: Boolean = false,
    val order: Int = 0
) : Serializable

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val pageIndex: Int,
    val template: PaperTemplate = PaperTemplate.RULED,
    val strokesJson: String = "[]",
    val textBlocksJson: String = "[]",
    val shapesJson: String = "[]",
    val stickersJson: String = "[]",
    val layersJson: String = "[]",
    val imagesJson: String = "[]",
    val pdfPageIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class DrawingStroke(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val color: Long, // Color as Long / ARGB
    val strokeWidth: Float,
    val toolType: ToolType = ToolType.PEN_BALLPOINT,
    val alpha: Float = 1.0f,
    val isHighlighter: Boolean = false,
    val layerId: String = "default"
) : Serializable

data class TextAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val x: Float,
    val y: Float,
    val fontSize: Float = 18f,
    val color: Long = 0xFF2D3748,
    val isBold: Boolean = false,
    val layerId: String = "default"
) : Serializable

data class ShapeAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ShapeType,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val strokeWidth: Float = 3f,
    val color: Long = 0xFF2D3748,
    val isFilled: Boolean = false,
    val layerId: String = "default"
) : Serializable

data class StickerAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val stickerKey: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val layerId: String = "default"
) : Serializable

data class PhotoAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String,
    val x: Float = 120f,
    val y: Float = 200f,
    val width: Float = 600f,
    val height: Float = 450f,
    val rotation: Float = 0f,
    val layerId: String = "default"
) : Serializable

/**
 * Pressure curve tuning for active stylus (S-Pen, USI, Apple Pencil via BT, generic).
 * Stored in DataStore as float multiplier.
 */
enum class PressureSensitivity(val multiplier: Float, val label: String) {
    SOFT(1.6f, "Soft — light touch, bold lines"),
    MEDIUM(1.0f, "Medium — balanced"),
    FIRM(0.6f, "Firm — press hard for width")
}

/**
 * Single source of truth for sticker keys -> visible glyph.
 * Fixes old bug where keys like "star" were drawn literally as text.
 */
object StickerCatalog {
    val all: LinkedHashMap<String, String> = linkedMapOf(
        "star" to "⭐",
        "check" to "✅",
        "ribbon" to "🎀",
        "important" to "❗",
        "fire" to "🔥",
        "idea" to "💡",
        "question" to "❓",
        "heart" to "❤️",
        "flag" to "🚩",
        "pin" to "📌",
        "math" to "📐",
        "book" to "📚",
        "pencil" to "✏️",
        "calendar" to "📅",
        "clock" to "⏰",
        "trophy" to "🏆",
        "warning" to "⚠️",
        "sparkle" to "✨",
        "target" to "🎯",
        "memo" to "📝"
    )

    fun glyphFor(key: String): String = all[key] ?: all.values.firstOrNull { it == key } ?: "⭐"
}
