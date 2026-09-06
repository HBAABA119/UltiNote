package com.ultinote.app.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.data.local.SerializationHelpers
import com.ultinote.app.data.model.NoteEntity
import com.ultinote.app.data.model.PageEntity
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.pdf.PdfHelper
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.util.HapticFeedbackManager
import com.ultinote.app.util.rememberHapticFeedbackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Liquid Glass Real-Time PDF Page Thumbnail Navigation Drawer / Bottom Sheet.
 * Generates high-fidelity page preview thumbnails directly from the PDF renderer
 * and overlays canvas annotations for seamless multi-page document navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPageThumbnailNavigationSheet(
    note: NoteEntity?,
    pages: List<PageEntity>,
    currentPageIndex: Int,
    hapticManager: HapticFeedbackManager = rememberHapticFeedbackManager(),
    onSelectPage: (Int) -> Unit,
    onAddPageWithTemplate: (PaperTemplate) -> Unit,
    onDeletePage: (PageEntity) -> Unit,
    onDuplicatePage: (PageEntity) -> Unit = {},
    onMovePage: (Int, Int) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalKomorebiPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    var showTemplateMenu by remember { mutableStateOf(false) }

    // Scroll to current page on launch
    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex in pages.indices) {
            listState.animateScrollToItem(currentPageIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.glassBackground.copy(alpha = 0.94f),
        contentColor = palette.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(palette.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Page Thumbnails & Navigation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )
                        Text(
                            text = "${pages.size} pages • Tap thumbnail to jump",
                            fontSize = 12.sp,
                            color = palette.colorScheme.outline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add Page Button with Template Dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.colorScheme.primary,
                            modifier = Modifier.clickable {
                                hapticManager.performButtonTapHaptic()
                                showTemplateMenu = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Page",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTemplateMenu,
                            onDismissRequest = { showTemplateMenu = false }
                        ) {
                            PaperTemplate.values().forEach { tmpl ->
                                DropdownMenuItem(
                                    text = { Text(tmpl.name.replace("_", " ")) },
                                    onClick = {
                                        hapticManager.performButtonTapHaptic()
                                        onAddPageWithTemplate(tmpl)
                                        showTemplateMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Scrollable Thumbnail Previews
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                itemsIndexed(pages) { index, page ->
                    val isSelected = index == currentPageIndex
                    PdfThumbnailCard(
                        note = note,
                        page = page,
                        pageNumber = index + 1,
                        isSelected = isSelected,
                        canMoveLeft = index > 0,
                        canMoveRight = index < pages.size - 1,
                        onSelect = {
                            hapticManager.performPageTurnHaptic()
                            onSelectPage(index)
                        },
                        onDelete = if (pages.size > 1) {
                            {
                                hapticManager.performButtonTapHaptic()
                                onDeletePage(page)
                            }
                        } else null,
                        onDuplicate = {
                            hapticManager.performButtonTapHaptic()
                            onDuplicatePage(page)
                        },
                        onMoveLeft = {
                            hapticManager.performButtonTapHaptic()
                            onMovePage(index, index - 1)
                        },
                        onMoveRight = {
                            hapticManager.performButtonTapHaptic()
                            onMovePage(index, index + 1)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Page Thumbnail Card with Live PDF rendering and Liquid Glass aesthetics
 */
@Composable
fun PdfThumbnailCard(
    note: NoteEntity?,
    page: PageEntity,
    pageNumber: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    onMoveLeft: (() -> Unit)? = null,
    onMoveRight: (() -> Unit)? = null,
    canMoveLeft: Boolean = false,
    canMoveRight: Boolean = false
) {
    val context = LocalContext.current
    val palette = LocalKomorebiPalette.current

    // Asynchronously render PDF page thumbnail
    val thumbnailBitmap by produceState<Bitmap?>(initialValue = null, key1 = note?.pdfFilePath, key2 = page.pdfPageIndex) {
        value = withContext(Dispatchers.IO) {
            val pdfPath = note?.pdfFilePath
            val pdfFile = if (pdfPath != null) File(pdfPath) else if (note?.isPdf == true) PdfHelper.getOrCreateSampleMathPdf(context) else null
            if (pdfFile != null && pdfFile.exists()) {
                PdfHelper.renderPdfPageThumbnail(context, pdfFile, page.pdfPageIndex, targetWidth = 260)
            } else {
                null
            }
        }
    }

    val strokeCount = remember(page.strokesJson) {
        SerializationHelpers.jsonToStrokes(page.strokesJson).size
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(160.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .shadow(
                    elevation = if (isSelected) 10.dp else 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = if (isSelected) palette.colorScheme.primary else Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(palette.canvasBackground)
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    brush = if (isSelected) {
                        Brush.linearGradient(
                            listOf(
                                palette.colorScheme.primary,
                                palette.colorScheme.secondary
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                palette.glassBorder,
                                palette.glassBorder.copy(alpha = 0.2f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onSelect() }
        ) {
            // PDF Page Rendered Image
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap!!.asImageBitmap(),
                    contentDescription = "Page $pageNumber Thumbnail",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Template Blank / Ruled placeholder preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDFBF7)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = page.template.name.replace("_", " "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        if (strokeCount > 0) {
                            Text(
                                text = "$strokeCount annotations",
                                fontSize = 10.sp,
                                color = palette.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Annotation count pill if drawing annotations exist
            if (strokeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "✍ $strokeCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Delete button if multi-page
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Page",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Current Active indicator badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.colorScheme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Page number and label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Page $pageNumber",
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurface
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "• Active",
                    fontSize = 11.sp,
                    color = palette.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Duplicate + reorder controls (school-friendly page management)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            if (onDuplicate != null) {
                Text(
                    text = "Duplicate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.colorScheme.primary,
                    modifier = Modifier.clickable { onDuplicate() }.padding(4.dp)
                )
            }
            if (canMoveLeft && onMoveLeft != null) {
                Text(
                    text = "◀",
                    fontSize = 13.sp,
                    color = palette.colorScheme.onSurface,
                    modifier = Modifier.clickable { onMoveLeft() }.padding(4.dp)
                )
            }
            if (canMoveRight && onMoveRight != null) {
                Text(
                    text = "▶",
                    fontSize = 13.sp,
                    color = palette.colorScheme.onSurface,
                    modifier = Modifier.clickable { onMoveRight() }.padding(4.dp)
                )
            }
        }
    }
}
