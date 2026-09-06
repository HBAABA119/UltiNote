package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalKomorebiPalette

// Helper to map folder icon string names to SVG vector icons
fun getFolderSvgIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "calculate", "calculator" -> Icons.Default.Calculate
        "book", "menubook", "math" -> Icons.Default.MenuBook
        "science", "flask", "biotech" -> Icons.Default.Science
        "palette", "art" -> Icons.Default.Palette
        "calendar", "schedule", "event_note" -> Icons.Default.CalendarMonth
        "code", "terminal" -> Icons.Default.Code
        "music", "musicnote" -> Icons.Default.MusicNote
        "star", "priority" -> Icons.Default.Star
        "bookmark" -> Icons.Default.Bookmark
        else -> Icons.Default.Folder
    }
}

val AvailableFolderSvgIcons = listOf(
    "calculate" to Icons.Default.Calculate,
    "book" to Icons.Default.MenuBook,
    "science" to Icons.Default.Science,
    "palette" to Icons.Default.Palette,
    "calendar" to Icons.Default.CalendarMonth,
    "code" to Icons.Default.Code,
    "music" to Icons.Default.MusicNote,
    "star" to Icons.Default.Star,
    "bookmark" to Icons.Default.Bookmark,
    "folder" to Icons.Default.Folder
)

/**
 * Liquid Glass Container with specular border highlights, glassmorphic gradient, and frosted edge glow
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    elevation: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val glassStyle = com.example.ui.theme.LiquidGlassTheme.current

    val finalBorderBrush = borderBrush ?: if (borderColor != null) {
        Brush.linearGradient(
            colors = listOf(
                borderColor.copy(alpha = 0.75f),
                borderColor.copy(alpha = 0.18f),
                borderColor.copy(alpha = 0.50f)
            )
        )
    } else {
        glassStyle.borderBrush
    }

    val finalBackgroundBrush = backgroundBrush ?: if (backgroundColor != null) {
        Brush.verticalGradient(listOf(backgroundColor, backgroundColor))
    } else {
        glassStyle.surfaceBrush
    }

    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = glassStyle.edgeGlowColor,
                ambientColor = glassStyle.edgeGlowColor.copy(alpha = 0.08f)
            )
            .border(1.2.dp, finalBorderBrush, shape)
            .background(finalBackgroundBrush, shape),
        shape = shape,
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Liquid Glass Modal Dialog with luminous frosted styling, specular highlights and backdrop blur simulation
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) {
    val palette = LocalKomorebiPalette.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.glassSurface.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
                .clickable(onClick = onDismissRequest),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable(enabled = false, onClick = {})
                    .padding(24.dp)
                    .width(460.dp)
            ) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = palette.glassSurface,
                    borderColor = palette.glassBorder,
                    elevation = 20.dp
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Aesthetic Liquid Glass Confirmation & System Prompt Dialog
 */
@Composable
fun LiquidGlassConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalKomorebiPalette.current

    LiquidGlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                else palette.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            )
                            .border(
                                1.dp,
                                if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else palette.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDestructive) MaterialTheme.colorScheme.error else palette.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = palette.colorScheme.outline
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cancelText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.colorScheme.outline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                LiquidGlassPillButton(
                    text = confirmText,
                    isPrimary = true,
                    onClick = onConfirm
                )
            }
        }
    }
}

/**
 * Dynamic Folder UI Component with Visual Pile Fill Effect & Dynamic SVG Icon
 * When notes pile up inside the folder, stacked paper sheets peek out of the sleeve,
 * and the custom SVG emblem (calculator, books, etc.) visually expands and scales with file density!
 */
@Composable
fun LiquidGlassFolderCard(
    folderName: String,
    iconName: String,
    colorHex: String,
    noteCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKomorebiPalette.current

    val folderColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        palette.colorScheme.primary
    }

    Box(
        modifier = modifier
            .width(190.dp)
            .height(165.dp)
            .clickable(onClick = onClick)
    ) {
        // DYNAMIC FILL EFFECT: Stacked paper sheets peeking out from the sleeve!
        if (noteCount > 0) {
            // Sheet 1: Slight tilt for 1+ notes
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(152.dp)
                    .height(98.dp)
                    .offset(y = 6.dp)
                    .rotate(if (noteCount >= 3) -3f else 0f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, Color(0x22000000), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            // Sheet 2: Second stacked page for 3+ notes
            if (noteCount >= 3) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(145.dp)
                        .height(98.dp)
                        .offset(y = 3.dp)
                        .rotate(2.5f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(palette.canvasBackground.copy(alpha = 0.98f))
                        .border(1.dp, Color(0x22000000), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }

            // Sheet 3: Dense thick stack for 6+ notes
            if (noteCount >= 6) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(138.dp)
                        .height(98.dp)
                        .offset(y = 0.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(folderColor.copy(alpha = 0.28f))
                        .border(1.dp, folderColor.copy(alpha = 0.45f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
        }

        // Front Liquid Glass Folder Sleeve
        LiquidGlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(134.dp),
            shape = RoundedCornerShape(22.dp),
            backgroundColor = if (isSelected) folderColor.copy(alpha = 0.28f) else palette.glassSurface,
            borderColor = if (isSelected) folderColor else palette.glassBorder,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: DYNAMIC SVG ICON (Visually scales with file count!) + note count pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamic SVG Folder Emblem: Calculator, Books, Flask, Art, Code etc.
                    DynamicFolderIcon(
                        iconName = iconName,
                        noteCount = noteCount,
                        tintColor = folderColor
                    )

                    // Count badge with dynamic glow if filled
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (noteCount > 0) folderColor.copy(alpha = 0.20f)
                                else Color.Black.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (noteCount > 0) folderColor.copy(alpha = 0.4f) else Color.Transparent,
                                CircleShape
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (noteCount == 1) "1 note" else "$noteCount notes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (noteCount > 0) folderColor else palette.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bottom: Folder Name
                Column {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = if (noteCount == 0) "Empty sleeve" else if (noteCount < 3) "Lightly filled" else "Active binder (${noteCount} items)",
                        fontSize = 11.sp,
                        color = palette.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Liquid Glass Pill Button
 */
@Composable
fun LiquidGlassPillButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val palette = LocalKomorebiPalette.current
    val shape = RoundedCornerShape(16.dp)

    val bg = if (isPrimary) palette.colorScheme.primary else palette.glassSurface
    val contentColor = if (isPrimary) palette.colorScheme.onPrimary else palette.colorScheme.onSurface

    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(
                1.dp,
                if (isPrimary) Color.Transparent else palette.glassBorder,
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = contentColor
        )
    }
}

/**
 * Aesthetic Liquid Glass Text Input Dialog (for Note Rename, New Note, New Folder)
 */
@Composable
fun LiquidGlassInputDialog(
    title: String,
    subtitle: String = "",
    initialValue: String = "",
    placeholder: String = "Enter name...",
    confirmText: String = "Save",
    icon: ImageVector? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }
    val palette = LocalKomorebiPalette.current

    LiquidGlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .border(1.dp, palette.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = palette.colorScheme.outline
                        )
                    }
                }
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = { Text(placeholder, fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = palette.glassSurface,
                    unfocusedContainerColor = palette.glassSurface.copy(alpha = 0.5f),
                    focusedIndicatorColor = palette.colorScheme.primary,
                    unfocusedIndicatorColor = palette.glassBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                LiquidGlassPillButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    isPrimary = false
                )
                LiquidGlassPillButton(
                    text = confirmText,
                    onClick = {
                        if (textValue.isNotBlank()) {
                            onConfirm(textValue.trim())
                        }
                    },
                    isPrimary = true
                )
            }
        }
    }
}

/**
 * Liquid Glass Pill-Shaped Top Bar for Note Editor (Portrait / Phone mode)
 */
@Composable
fun LiquidGlassPillTopBar(
    title: String,
    subtitle: String,
    currentPage: Int,
    totalPages: Int,
    autoSnapEnabled: Boolean,
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onPagesOverview: () -> Unit,
    onToggleAutoSnap: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKomorebiPalette.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(26.dp),
            backgroundColor = palette.glassSurface,
            borderColor = palette.glassBorder,
            elevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.colorScheme.onSurface
                    )
                }

                // Clickable Note Title (opens Rename Dialog)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onTitleClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = palette.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = palette.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        maxLines = 1,
                        color = palette.colorScheme.outline
                    )
                }

                // Auto-Correction / Snap Toggle Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (autoSnapEnabled) palette.colorScheme.primary.copy(alpha = 0.20f)
                            else Color.Transparent
                        )
                        .clickable(onClick = onToggleAutoSnap)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Auto Drawing Correction Snap",
                        tint = if (autoSnapEnabled) palette.colorScheme.primary else palette.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Page Navigation Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(0.8.dp, palette.glassBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = currentPage > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentPage > 0) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${currentPage + 1}/${totalPages.coerceAtLeast(1)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable(onClick = onPagesOverview)
                            .padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onNextPage,
                        enabled = currentPage < totalPages - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (currentPage < totalPages - 1) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Grid Overview Button
                IconButton(onClick = onPagesOverview, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Pages Overview",
                        tint = palette.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Export PDF Button
                IconButton(onClick = onExportPdf, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export PDF",
                        tint = palette.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Floating Liquid Glass Pill-Shaped Sidebar for Tablet Horizontal Mode.
 * Transforms the standard top bar into an ergonomic, frosted vertical pill on the tablet's left edge!
 */
@Composable
fun LiquidGlassPillSidebar(
    title: String,
    subtitle: String,
    currentPage: Int,
    totalPages: Int,
    autoSnapEnabled: Boolean,
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onPagesOverview: () -> Unit,
    onToggleAutoSnap: () -> Unit,
    onExportPdf: () -> Unit,
    onTriggerAiCompanion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKomorebiPalette.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(28.dp),
            backgroundColor = palette.glassSurface,
            borderColor = palette.glassBorder,
            elevation = 16.dp,
            modifier = Modifier.width(68.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top group: Back & Note Emblem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Back Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(palette.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Title & Rename Pill
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .border(1.dp, palette.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onTitleClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename: $title",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Auto-Drawing Correction Snap Toggle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (autoSnapEnabled) palette.colorScheme.primary.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (autoSnapEnabled) palette.colorScheme.primary else palette.glassBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(onClick = onToggleAutoSnap),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Auto Drawing Snap ${if (autoSnapEnabled) "Active" else "Off"}",
                            tint = if (autoSnapEnabled) palette.colorScheme.primary else palette.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Middle group: Paging Navigation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(0.8.dp, palette.glassBorder, RoundedCornerShape(20.dp))
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = currentPage > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Previous page",
                            tint = if (currentPage > 0) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${currentPage + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onPagesOverview)
                    )

                    Text(
                        text = "/${totalPages.coerceAtLeast(1)}",
                        fontSize = 10.sp,
                        color = palette.colorScheme.outline
                    )

                    IconButton(
                        onClick = onNextPage,
                        enabled = currentPage < totalPages - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Next page",
                            tint = if (currentPage < totalPages - 1) palette.colorScheme.onSurface else palette.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom group: Pages Grid, PDF Export, AI Companion Trigger
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Page Grid Overview
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(palette.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .clickable(onClick = onPagesOverview),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Grid overview",
                            tint = palette.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // PDF Export
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(palette.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .clickable(onClick = onExportPdf),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export PDF",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Liquid Glass AI Companion trigger
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        palette.colorScheme.primary.copy(alpha = 0.45f),
                                        palette.colorScheme.secondary.copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .border(1.dp, palette.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onTriggerAiCompanion),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Study Companion",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

