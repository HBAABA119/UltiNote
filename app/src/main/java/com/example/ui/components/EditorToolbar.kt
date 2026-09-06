package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canvas.PalettePresets
import com.example.data.model.ShapeType
import com.example.data.model.StickerCatalog
import com.example.data.model.ToolType
import com.example.ui.theme.LocalKomorebiPalette

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditorToolbar(
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    activeTool: ToolType,
    currentColor: Long,
    currentStrokeWidth: Float,
    currentShapeType: ShapeType,
    rulerVisible: Boolean,
    stylusOnly: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onSelectTool: (ToolType) -> Unit,
    onSelectColor: (Long) -> Unit,
    onSelectStrokeWidth: (Float) -> Unit,
    onSelectShapeType: (ShapeType) -> Unit,
    onToggleRuler: () -> Unit,
    onToggleStylusOnly: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenAiCompanion: () -> Unit,
    onAddSticker: (String) -> Unit,
    onAddImage: () -> Unit = {},
    isLayersOpen: Boolean = false,
    onToggleLayers: (() -> Unit)? = null
) {
    val palette = LocalKomorebiPalette.current
    var showColorDialog by remember { mutableStateOf(false) }
    var showShapeMenu by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showWidthMenu by remember { mutableStateOf(false) }
    var showPenMenu by remember { mutableStateOf(false) }
    val stickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LiquidGlassCard(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = palette.glassSurface,
        elevation = 4.dp
    ) {
        if (isVertical) {
            // TABLET LANDSCAPE: Elegant Vertical Floating Liquid Glass Rail
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Undo / Redo
                IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(modifier = Modifier.width(28.dp).height(1.dp).background(palette.glassBorder))

                // Tools — vertical rail (tablet landscape + portrait rail)
                Box {
                    ToolIconButton(
                        icon = when (activeTool) {
                            ToolType.PEN_FOUNTAIN -> Icons.Default.Brush
                            ToolType.PEN_BRUSH -> Icons.Default.Brush
                            else -> Icons.Default.Create
                        },
                        label = "Pen",
                        isSelected = activeTool == ToolType.PEN_BALLPOINT || activeTool == ToolType.PEN_FOUNTAIN || activeTool == ToolType.PEN_BRUSH,
                        onClick = {
                            if (activeTool == ToolType.PEN_BALLPOINT || activeTool == ToolType.PEN_FOUNTAIN || activeTool == ToolType.PEN_BRUSH) {
                                showPenMenu = true
                            } else {
                                onSelectTool(ToolType.PEN_BALLPOINT)
                            }
                        }
                    )
                    DropdownMenu(expanded = showPenMenu, onDismissRequest = { showPenMenu = false }) {
                        DropdownMenuItem(text = { Text("Ballpoint — stable + neat") }, onClick = { onSelectTool(ToolType.PEN_BALLPOINT); showPenMenu = false })
                        DropdownMenuItem(text = { Text("Fountain — pressure flair") }, onClick = { onSelectTool(ToolType.PEN_FOUNTAIN); showPenMenu = false })
                        DropdownMenuItem(text = { Text("Brush — bold + soft") }, onClick = { onSelectTool(ToolType.PEN_BRUSH); showPenMenu = false })
                    }
                }

                ToolIconButton(
                    icon = Icons.Default.Highlight,
                    label = "Highlighter",
                    isSelected = activeTool == ToolType.HIGHLIGHTER,
                    onClick = { onSelectTool(ToolType.HIGHLIGHTER) }
                )

                ToolIconButton(
                    icon = Icons.Default.CropSquare,
                    label = "Eraser",
                    isSelected = activeTool == ToolType.ERASER_STROKE,
                    onClick = { onSelectTool(ToolType.ERASER_STROKE) }
                )

                ToolIconButton(
                    icon = Icons.Default.CleaningServices,
                    label = "Precision eraser",
                    isSelected = activeTool == ToolType.ERASER_PRECISION,
                    onClick = { onSelectTool(ToolType.ERASER_PRECISION) }
                )

                ToolIconButton(
                    icon = Icons.Default.Gesture,
                    label = "Lasso select",
                    isSelected = activeTool == ToolType.LASSO,
                    onClick = { onSelectTool(ToolType.LASSO) }
                )

                ToolIconButton(
                    icon = Icons.Default.Hardware,
                    label = "Ruler",
                    isSelected = rulerVisible,
                    onClick = onToggleRuler
                )

                Box {
                    ToolIconButton(
                        icon = when (currentShapeType) {
                            ShapeType.LINE -> Icons.Default.Timeline
                            ShapeType.RECTANGLE -> Icons.Default.Square
                            ShapeType.CIRCLE -> Icons.Default.Circle
                            ShapeType.TRIANGLE -> Icons.Default.ChangeHistory
                            ShapeType.ARROW -> Icons.Default.NorthEast
                            ShapeType.STAR -> Icons.Default.Star
                        },
                        label = "Shapes",
                        isSelected = activeTool == ToolType.SHAPE,
                        onClick = {
                            onSelectTool(ToolType.SHAPE)
                            showShapeMenu = true
                        }
                    )

                    DropdownMenu(expanded = showShapeMenu, onDismissRequest = { showShapeMenu = false }) {
                        ShapeType.values().forEach { shapeType ->
                            DropdownMenuItem(text = { Text(shapeType.name) }, onClick = { onSelectShapeType(shapeType); showShapeMenu = false })
                        }
                    }
                }

                ToolIconButton(
                    icon = Icons.Default.TextFields,
                    label = "Text Box",
                    isSelected = activeTool == ToolType.TEXT,
                    onClick = { onSelectTool(ToolType.TEXT) }
                )

                ToolIconButton(
                    icon = Icons.Default.Star,
                    label = "Stickers",
                    isSelected = activeTool == ToolType.STICKER,
                    onClick = { showStickerSheet = true }
                )

                ToolIconButton(
                    icon = Icons.Default.Image,
                    label = "Insert photo",
                    isSelected = activeTool == ToolType.IMAGE,
                    onClick = {
                        onSelectTool(ToolType.IMAGE)
                        onAddImage()
                    }
                )

                Box(modifier = Modifier.width(28.dp).height(1.dp).background(palette.glassBorder))

                // Color Chip
                val displayColor = Color(currentColor)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(displayColor)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showColorDialog = true }
                )

                // Stroke Width Indicator
                Box {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { showWidthMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((currentStrokeWidth * 1.5f).coerceIn(4f, 18f).dp)
                                .clip(CircleShape)
                                .background(palette.colorScheme.onSurface)
                        )
                    }

                    DropdownMenu(expanded = showWidthMenu, onDismissRequest = { showWidthMenu = false }) {
                        PalettePresets.StrokeWidths.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.toInt()} pt") },
                                onClick = { onSelectStrokeWidth(w); showWidthMenu = false }
                            )
                        }
                    }
                }

                // Stylus Only Palm Rejection Toggle
                ToolIconButton(
                    icon = Icons.Default.PanTool,
                    label = if (stylusOnly) "Stylus Only" else "Touch & Stylus",
                    isSelected = stylusOnly,
                    onClick = onToggleStylusOnly
                )

                if (onToggleLayers != null) {
                    ToolIconButton(
                        icon = Icons.Default.Layers,
                        label = "Layers",
                        isSelected = isLayersOpen,
                        onClick = onToggleLayers
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // AI Companion Pill
                IconButton(
                    onClick = onOpenAiCompanion,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Companion",
                        tint = palette.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // PORTRAIT / COMPACT: Horizontal Floating Glass Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo & Redo
                IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant
                    )
                }

                IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant
                    )
                }

                ToolbarDivider()

                Box {
                    ToolIconButton(
                        icon = when (activeTool) {
                            ToolType.PEN_FOUNTAIN, ToolType.PEN_BRUSH -> Icons.Default.Brush
                            else -> Icons.Default.Create
                        },
                        label = "Pen",
                        isSelected = activeTool == ToolType.PEN_BALLPOINT || activeTool == ToolType.PEN_FOUNTAIN || activeTool == ToolType.PEN_BRUSH,
                        onClick = { onSelectTool(ToolType.PEN_BALLPOINT) }
                    )
                }

                ToolIconButton(
                    icon = Icons.Default.Highlight,
                    label = "Highlighter",
                    isSelected = activeTool == ToolType.HIGHLIGHTER,
                    onClick = { onSelectTool(ToolType.HIGHLIGHTER) }
                )

                ToolIconButton(
                    icon = Icons.Default.CropSquare,
                    label = "Eraser",
                    isSelected = activeTool == ToolType.ERASER_STROKE,
                    onClick = { onSelectTool(ToolType.ERASER_STROKE) }
                )

                ToolIconButton(
                    icon = Icons.Default.CleaningServices,
                    label = "Precision",
                    isSelected = activeTool == ToolType.ERASER_PRECISION,
                    onClick = { onSelectTool(ToolType.ERASER_PRECISION) }
                )

                ToolIconButton(
                    icon = Icons.Default.Gesture,
                    label = "Lasso",
                    isSelected = activeTool == ToolType.LASSO,
                    onClick = { onSelectTool(ToolType.LASSO) }
                )

                ToolIconButton(
                    icon = Icons.Default.Hardware,
                    label = "Ruler",
                    isSelected = rulerVisible,
                    onClick = onToggleRuler
                )

                Box {
                    ToolIconButton(
                        icon = when (currentShapeType) {
                            ShapeType.LINE -> Icons.Default.Timeline
                            ShapeType.RECTANGLE -> Icons.Default.Square
                            ShapeType.CIRCLE -> Icons.Default.Circle
                            ShapeType.TRIANGLE -> Icons.Default.ChangeHistory
                            ShapeType.ARROW -> Icons.Default.NorthEast
                            ShapeType.STAR -> Icons.Default.Star
                        },
                        label = "Shapes",
                        isSelected = activeTool == ToolType.SHAPE,
                        onClick = {
                            onSelectTool(ToolType.SHAPE)
                            showShapeMenu = true
                        }
                    )

                    DropdownMenu(expanded = showShapeMenu, onDismissRequest = { showShapeMenu = false }) {
                        ShapeType.values().forEach { shapeType ->
                            DropdownMenuItem(text = { Text(shapeType.name) }, onClick = { onSelectShapeType(shapeType); showShapeMenu = false })
                        }
                    }
                }

                ToolIconButton(
                    icon = Icons.Default.TextFields,
                    label = "Text Box",
                    isSelected = activeTool == ToolType.TEXT,
                    onClick = { onSelectTool(ToolType.TEXT) }
                )

                ToolIconButton(
                    icon = Icons.Default.Star,
                    label = "Stickers",
                    isSelected = activeTool == ToolType.STICKER,
                    onClick = { showStickerSheet = true }
                )

                ToolIconButton(
                    icon = Icons.Default.Image,
                    label = "Photo",
                    isSelected = activeTool == ToolType.IMAGE,
                    onClick = {
                        onSelectTool(ToolType.IMAGE)
                        onAddImage()
                    }
                )

                ToolbarDivider()

                // Color Chips
                val displayColor = Color(currentColor)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(displayColor)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showColorDialog = true }
                )

                val quickColors = if (activeTool == ToolType.HIGHLIGHTER) PalettePresets.Highlighters.take(3) else PalettePresets.Pastels.take(3)
                for (col in quickColors) {
                    val c = Color(col)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { onSelectColor(col) }
                    )
                }

                ToolbarDivider()

                // Stroke Width Selector
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { showWidthMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((currentStrokeWidth * 1.5f).coerceIn(4f, 20f).dp)
                                .clip(CircleShape)
                                .background(palette.colorScheme.onSurface)
                        )
                    }

                    DropdownMenu(expanded = showWidthMenu, onDismissRequest = { showWidthMenu = false }) {
                        PalettePresets.StrokeWidths.forEach { w ->
                            DropdownMenuItem(text = { Text("${w.toInt()} pt") }, onClick = { onSelectStrokeWidth(w); showWidthMenu = false })
                        }
                    }
                }

                ToolIconButton(
                    icon = Icons.Default.PanTool,
                    label = if (stylusOnly) "Stylus Only" else "Touch & Stylus",
                    isSelected = stylusOnly,
                    onClick = onToggleStylusOnly
                )

                if (onToggleLayers != null) {
                    ToolIconButton(
                        icon = Icons.Default.Layers,
                        label = "Layers",
                        isSelected = isLayersOpen,
                        onClick = onToggleLayers
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = palette.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onOpenAiCompanion() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Study Companion",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Companion",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    // Full sticker picker — real emoji grid, no more 4-item menu
    if (showStickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStickerSheet = false },
            sheetState = stickerSheetState,
            containerColor = palette.glassSurface
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Study stickers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.colorScheme.onSurface
                )
                Text(
                    text = "Tap to drop onto your page. Then use Lasso to select + move logic via Duplicate.",
                    fontSize = 12.sp,
                    color = palette.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StickerCatalog.all.forEach { (key, glyph) ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable {
                                    onAddSticker(key)
                                    showStickerSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = glyph, fontSize = 26.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Liquid Glass Color Picker Dialog
    if (showColorDialog) {
        LiquidGlassDialog(onDismissRequest = { showColorDialog = false }) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Aesthetic Ink Palette",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.colorScheme.onSurface
                )

                Text("Classic Stationery Inks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PalettePresets.Inks.forEach { c ->
                        ColorSelectCircle(colorValue = c, isSelected = currentColor == c, onSelect = { onSelectColor(c); showColorDialog = false })
                    }
                }

                Text("Pastel Notes & Highlighting", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PalettePresets.Pastels.forEach { c ->
                        ColorSelectCircle(colorValue = c, isSelected = currentColor == c, onSelect = { onSelectColor(c); showColorDialog = false })
                    }
                }

                Text("Fluorescent Highlighters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PalettePresets.Highlighters.forEach { c ->
                        ColorSelectCircle(colorValue = c, isSelected = currentColor == c, onSelect = { onSelectColor(c); showColorDialog = false })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showColorDialog = false }) {
                        Text("Close", color = palette.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSelectCircle(
    colorValue: Long,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(colorValue))
            .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
            .clickable { onSelect() }
    )
}

@Composable
private fun ToolIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) palette.colorScheme.primaryContainer else Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurface.copy(alpha = 0.75f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToolbarDivider() {
    val palette = LocalKomorebiPalette.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(palette.glassBorder)
    )
}
