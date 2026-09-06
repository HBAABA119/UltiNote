package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PaperTemplate
import com.example.ui.theme.LocalKomorebiPalette
import com.example.util.HapticFeedbackManager
import com.example.util.rememberHapticFeedbackManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Floating, blurred, semi-transparent liquid glass widgets for the Home / Library Screen.
 * Provides quick access to recent folders, fast note creation with preset templates,
 * and immediate access to the most recent document.
 */
@Composable
fun LiquidGlassHomeWidgetsSection(
    folders: List<FolderEntity>,
    recentNotes: List<NoteEntity>,
    activeFolderId: String?,
    hapticManager: HapticFeedbackManager = rememberHapticFeedbackManager(),
    onSelectFolder: (String?) -> Unit,
    onQuickCreateNote: (PaperTemplate) -> Unit,
    onOpenCreateFolder: () -> Unit,
    onOpenImportPdf: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val palette = LocalKomorebiPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WIDGET 1: Floating Liquid Glass Quick Creation Hub
        LiquidGlassCard(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = palette.glassSurface.copy(alpha = 0.88f),
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(palette.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAdd,
                                contentDescription = null,
                                tint = palette.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QUICK NOTE CREATION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // PDF Import chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    hapticManager.performButtonTapHaptic()
                                    onOpenImportPdf()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = palette.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PDF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // New Folder chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    hapticManager.performButtonTapHaptic()
                                    onOpenCreateFolder()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreateNewFolder,
                                    contentDescription = null,
                                    tint = palette.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Folder",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Template Selector Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val templates = listOf(
                        PaperTemplate.RULED to "Ruled",
                        PaperTemplate.CORNELL to "Cornell",
                        PaperTemplate.GRID to "Grid",
                        PaperTemplate.BLANK to "Blank"
                    )

                    for ((tmpl, label) in templates) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.colorScheme.surface.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                palette.glassBorder.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    hapticManager.performStrokeStartHaptic()
                                    onQuickCreateNote(tmpl)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = palette.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // WIDGET 2: Recent Folders Floating Liquid Glass Carousel
        if (folders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RECENT & FAVORITE FOLDERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.outline,
                        letterSpacing = 1.sp
                    )

                    if (activeFolderId != null) {
                        Text(
                            text = "Show All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.colorScheme.primary,
                            modifier = Modifier.clickable {
                                hapticManager.performButtonTapHaptic()
                                onSelectFolder(null)
                            }
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    // "All Notes" Root folder card
                    item {
                        val isAllSelected = activeFolderId == null
                        FolderGlassPillWidget(
                            name = "All Notes",
                            iconName = "folder",
                            colorHex = "#769382",
                            isSelected = isAllSelected,
                            onClick = {
                                hapticManager.performButtonTapHaptic()
                                onSelectFolder(null)
                            }
                        )
                    }

                    // Folders
                    items(folders) { folder ->
                        val isSelected = folder.id == activeFolderId
                        FolderGlassPillWidget(
                            name = folder.name,
                            iconName = folder.iconName,
                            colorHex = folder.colorHex,
                            isSelected = isSelected,
                            onClick = {
                                hapticManager.performButtonTapHaptic()
                                onSelectFolder(folder.id)
                            }
                        )
                    }
                }
            }
        }

        // WIDGET 3: Most Recent Note Glass Spotlight
        if (recentNotes.isNotEmpty()) {
            val latestNote = recentNotes.maxByOrNull { it.updatedAt }
            if (latestNote != null) {
                RecentNoteGlassSpotlightWidget(
                    note = latestNote,
                    onClick = {
                        hapticManager.performStrokeStartHaptic()
                        onOpenNote(latestNote.id)
                    }
                )
            }
        }
    }
}

/**
 * Semi-transparent Liquid Glass Pill for Folders
 */
@Composable
private fun FolderGlassPillWidget(
    name: String,
    iconName: String,
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val folderColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        palette.colorScheme.primary
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) palette.colorScheme.primaryContainer else palette.glassSurface.copy(alpha = 0.85f),
        label = "folderGlassBg"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = animatedBg,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            brush = if (isSelected) {
                Brush.linearGradient(listOf(palette.colorScheme.primary, palette.colorScheme.secondary))
            } else {
                Brush.verticalGradient(listOf(palette.glassBorder, palette.glassBorder.copy(alpha = 0.2f)))
            }
        ),
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(folderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFolderSvgIcon(iconName),
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) palette.colorScheme.onPrimaryContainer else palette.colorScheme.onSurface
            )
        }
    }
}

/**
 * Floating glass spotlight for the last modified note
 */
@Composable
private fun RecentNoteGlassSpotlightWidget(
    note: NoteEntity,
    onClick: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormat.format(Date(note.updatedAt)) }

    LiquidGlassCard(
        shape = RoundedCornerShape(20.dp),
        backgroundColor = palette.glassSurface.copy(alpha = 0.85f),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    palette.colorScheme.primary,
                                    palette.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (note.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )
                        if (note.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFE5A100),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "Last edited $formattedDate • ${note.pageCount} ${if (note.pageCount == 1) "page" else "pages"}",
                        fontSize = 11.sp,
                        color = palette.colorScheme.outline
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.colorScheme.primaryContainer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = palette.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resume",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
