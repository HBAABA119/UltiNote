package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canvas.InteractiveRulerWidget
import com.example.canvas.NoteCanvasView
import com.example.canvas.PageSnapshot
import com.example.canvas.PalettePresets
import com.example.canvas.RulerState
import com.example.data.local.KomorebiRepository
import com.example.data.local.SerializationHelpers
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.DrawingLayer
import com.example.data.model.DrawingStroke
import com.example.data.model.LayerBlendMode
import com.example.data.model.NoteEntity
import com.example.data.model.PageEntity
import com.example.data.model.PaperTemplate
import com.example.data.model.PhotoAnnotation
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.StickerAnnotation
import com.example.data.model.TextAnnotation
import com.example.data.model.ToolType
import com.example.pdf.PdfHelper
import com.example.ui.components.AiStudyCompanionSheet
import com.example.ui.components.EditorToolbar
import com.example.ui.components.FloatingLiquidGlassAiCompanion
import com.example.ui.components.LayersPanel
import com.example.ui.components.LiquidGlassInputDialog
import com.example.ui.components.LiquidGlassPillSidebar
import com.example.ui.components.LiquidGlassPillTopBar
import com.example.ui.components.PdfPageThumbnailNavigationSheet
import com.example.ui.components.TextEditDialog
import com.example.ui.theme.LocalKomorebiPalette
import com.example.util.HapticFeedbackManager
import com.example.util.rememberHapticFeedbackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    repository: KomorebiRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalKomorebiPalette.current
    val coroutineScope = rememberCoroutineScope()
    val hapticManager = rememberHapticFeedbackManager()

    var note by remember { mutableStateOf<NoteEntity?>(null) }
    val pagesState by repository.getPagesForNote(noteId).collectAsState(initial = emptyList())
    var currentPageIndex by remember { mutableIntStateOf(0) }

    // All notes in the same folder for AI Companion reference
    val folderNotes by repository.allNotes.collectAsState(initial = emptyList())

    // Canvas Tools & State
    var activeTool by remember { mutableStateOf(ToolType.PEN_BALLPOINT) }
    var activeColor by remember { mutableLongStateOf(PalettePresets.Inks[0]) }
    var activeStrokeWidth by remember { mutableFloatStateOf(3.0f) }
    var activeShapeType by remember { mutableStateOf(ShapeType.RECTANGLE) }
    var rulerState by remember { mutableStateOf(RulerState(isVisible = false)) }
    var stylusOnlyInking by remember { mutableStateOf(false) }
    var pressureMult by remember { mutableFloatStateOf(1.0f) }

    // Load persisted prefs (stylus, pressure, smoothing) — local-first, no cloud
    val prefs = remember(context) { UserPreferencesRepository.get(context) }
    val savedStylusOnly by prefs.stylusOnly.collectAsState(initial = false)
    val savedPressure by prefs.pressureMult.collectAsState(initial = 1.0f)
    val savedAutoSnap by prefs.autoSnap.collectAsState(initial = true)
    LaunchedEffect(savedStylusOnly) { stylusOnlyInking = savedStylusOnly }
    LaunchedEffect(savedPressure) { pressureMult = savedPressure }

    // Page Content & Layers
    val currentStrokes = remember { mutableStateListOf<DrawingStroke>() }
    val currentShapes = remember { mutableStateListOf<ShapeAnnotation>() }
    val currentTextBlocks = remember { mutableStateListOf<TextAnnotation>() }
    val currentStickers = remember { mutableStateListOf<StickerAnnotation>() }
    val currentPhotos = remember { mutableStateListOf<PhotoAnnotation>() }
    val currentLayers = remember { mutableStateListOf<DrawingLayer>() }
    var activeLayerId by remember { mutableStateOf("layer_1") }

    // Undo / Redo Stacks
    val undoStack = remember { mutableStateListOf<PageSnapshot>() }
    val redoStack = remember { mutableStateListOf<PageSnapshot>() }

    // PDF rendering state
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Dialogs & Sheets
    var showPagesOverview by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAiCompanionSheet by remember { mutableStateOf(false) }
    var showLayersPanel by remember { mutableStateOf(false) }
    var autoCorrectionEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(savedAutoSnap) { autoCorrectionEnabled = savedAutoSnap }
    val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Text Editing State
    var pendingTextOffset by remember { mutableStateOf<Offset?>(null) }
    var editingTextBlock by remember { mutableStateOf<TextAnnotation?>(null) }

    // Load Note Info
    LaunchedEffect(noteId) {
        note = repository.getNote(noteId)
    }

    // Load active page content when currentPageIndex or pages list changes
    LaunchedEffect(pagesState, currentPageIndex) {
        if (pagesState.isNotEmpty() && currentPageIndex in pagesState.indices) {
            val page = pagesState[currentPageIndex]
            currentStrokes.clear()
            currentStrokes.addAll(SerializationHelpers.jsonToStrokes(page.strokesJson))

            currentShapes.clear()
            currentShapes.addAll(SerializationHelpers.jsonToShapes(page.shapesJson))

            currentTextBlocks.clear()
            currentTextBlocks.addAll(SerializationHelpers.jsonToTextBlocks(page.textBlocksJson))

            currentStickers.clear()
            currentStickers.addAll(SerializationHelpers.jsonToStickers(page.stickersJson))

            currentPhotos.clear()
            try {
                currentPhotos.addAll(SerializationHelpers.jsonToPhotos(page.imagesJson))
            } catch (_: Exception) { }

            currentLayers.clear()
            val loadedLayers = SerializationHelpers.jsonToLayers(page.layersJson)
            if (loadedLayers.isEmpty()) {
                val defaultLayer = DrawingLayer(
                    id = "layer_1",
                    name = "Base Artwork",
                    isVisible = true,
                    opacity = 1.0f,
                    blendMode = LayerBlendMode.NORMAL,
                    isLocked = false,
                    order = 0
                )
                currentLayers.add(defaultLayer)
                activeLayerId = "layer_1"
            } else {
                currentLayers.addAll(loadedLayers)
                if (currentLayers.none { it.id == activeLayerId }) {
                    activeLayerId = currentLayers.first().id
                }
            }

            undoStack.clear()
            redoStack.clear()

            // If note is PDF, render the PDF page
            val currentNote = note
            if (currentNote != null && currentNote.isPdf) {
                val pdfFile = if (currentNote.pdfFilePath != null) File(currentNote.pdfFilePath) else PdfHelper.getOrCreateSampleMathPdf(context)
                pdfBitmap = PdfHelper.renderPdfPage(context, pdfFile, page.pdfPageIndex)
            }
        }
    }

    // Save Page to Room helper
    fun saveActivePage() {
        if (pagesState.isEmpty() || currentPageIndex !in pagesState.indices) return
        val page = pagesState[currentPageIndex]
        val updatedPage = page.copy(
            strokesJson = SerializationHelpers.strokesToJson(currentStrokes.toList()),
            shapesJson = SerializationHelpers.shapesToJson(currentShapes.toList()),
            textBlocksJson = SerializationHelpers.textBlocksToJson(currentTextBlocks.toList()),
            stickersJson = SerializationHelpers.stickersToJson(currentStickers.toList()),
            imagesJson = SerializationHelpers.photosToJson(currentPhotos.toList()),
            layersJson = SerializationHelpers.layersToJson(currentLayers.toList())
        )
        coroutineScope.launch(Dispatchers.IO) {
            repository.updatePage(updatedPage)
        }
    }

    fun pushUndoSnapshot() {
        undoStack.add(
            PageSnapshot(
                strokes = currentStrokes.toList(),
                shapes = currentShapes.toList(),
                textBlocks = currentTextBlocks.toList(),
                stickers = currentStickers.toList(),
                layers = currentLayers.toList()
            )
        )
        // cap stacks so long study sessions don't OOM on low-end school tablets
        if (undoStack.size > 80) undoStack.removeAt(0)
        redoStack.clear()
    }

    // Lasso helpers — operate on parent lists so undo stays correct
    fun deleteLassoSelection(strokeIds: Set<String>, shapeIds: Set<String>, textIds: Set<String>, stickerIds: Set<String>) {
        if (strokeIds.isEmpty() && shapeIds.isEmpty() && textIds.isEmpty() && stickerIds.isEmpty()) return
        pushUndoSnapshot()
        currentStrokes.removeAll { it.id in strokeIds }
        currentShapes.removeAll { it.id in shapeIds }
        currentTextBlocks.removeAll { it.id in textIds }
        currentStickers.removeAll { it.id in stickerIds }
        saveActivePage()
        Toast.makeText(context, "Deleted ${strokeIds.size + shapeIds.size + textIds.size + stickerIds.size} items", Toast.LENGTH_SHORT).show()
    }

    fun duplicateLassoSelection(strokeIds: Set<String>, shapeIds: Set<String>, textIds: Set<String>, stickerIds: Set<String>) {
        if (strokeIds.isEmpty() && shapeIds.isEmpty() && textIds.isEmpty() && stickerIds.isEmpty()) return
        pushUndoSnapshot()
        currentStrokes.filter { it.id in strokeIds }.forEach { s ->
            currentStrokes.add(s.copy(id = UUID.randomUUID().toString(), points = s.points.map { it.copy(x = it.x + 36f, y = it.y + 36f) }))
        }
        currentShapes.filter { it.id in shapeIds }.forEach { sh ->
            currentShapes.add(sh.copy(id = UUID.randomUUID().toString(), startX = sh.startX + 36f, startY = sh.startY + 36f, endX = sh.endX + 36f, endY = sh.endY + 36f))
        }
        currentTextBlocks.filter { it.id in textIds }.forEach { t ->
            currentTextBlocks.add(t.copy(id = UUID.randomUUID().toString(), x = t.x + 36f, y = t.y + 36f))
        }
        currentStickers.filter { it.id in stickerIds }.forEach { st ->
            currentStickers.add(st.copy(id = UUID.randomUUID().toString(), x = st.x + 36f, y = st.y + 36f))
        }
        saveActivePage()
        Toast.makeText(context, "Duplicated selection", Toast.LENGTH_SHORT).show()
    }

    // Photo picker — declared after save/push helpers so the callback can use them.
    // Insert textbook photos, whiteboard shots, diagrams via system picker (no storage permission needed).
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val file = repository.importImageFromUri(uri)
                if (file != null) {
                    withContext(Dispatchers.Main) {
                        pushUndoSnapshot()
                        currentPhotos.add(
                            PhotoAnnotation(
                                filePath = file.absolutePath,
                                x = 120f,
                                y = 220f + (currentPhotos.size * 40f),
                                width = 620f,
                                height = 460f,
                                layerId = activeLayerId
                            )
                        )
                        saveActivePage()
                        activeTool = ToolType.PEN_BALLPOINT
                        Toast.makeText(context, "Photo added to page", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Could not import that image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val activeTemplate = if (pagesState.isNotEmpty() && currentPageIndex in pagesState.indices) {
        pagesState[currentPageIndex].template
    } else {
        PaperTemplate.RULED
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Phone portrait + tablet portrait (vertical) -> top bar + bottom dock.
        // Wide landscape (tablet landscape, foldable, desktop) -> side rail + vertical toolbar.
        // This keeps one-hand phone use + two-hand tablet + stylus workflows all comfortable.
        val isTabletLandscape = maxWidth >= 700.dp && maxWidth > maxHeight

        Scaffold(
            topBar = {
                if (!isTabletLandscape) {
                    LiquidGlassPillTopBar(
                        title = note?.title ?: "Note Editor",
                        subtitle = if (note?.isPdf == true) "PDF Document Annotation" else "${activeTemplate.name.replace("_", " ")} Notebook",
                        currentPage = currentPageIndex,
                        totalPages = pagesState.size,
                        autoSnapEnabled = autoCorrectionEnabled,
                        onBack = onBack,
                        onTitleClick = { showRenameDialog = true },
                        onPreviousPage = {
                            if (currentPageIndex > 0) {
                                hapticManager.performPageTurnHaptic()
                                saveActivePage()
                                currentPageIndex--
                            }
                        },
                        onNextPage = {
                            if (currentPageIndex < pagesState.size - 1) {
                                hapticManager.performPageTurnHaptic()
                                saveActivePage()
                                currentPageIndex++
                            }
                        },
                        onPagesOverview = {
                            hapticManager.performButtonTapHaptic()
                            showPagesOverview = true
                        },
                        onToggleAutoSnap = {
                            autoCorrectionEnabled = !autoCorrectionEnabled
                            hapticManager.performToolSwitchHaptic()
                            coroutineScope.launch { prefs.setAutoSnap(autoCorrectionEnabled) }
                            Toast.makeText(
                                context,
                                if (autoCorrectionEnabled) "Auto Drawing Snap: ON" else "Auto Snap: OFF",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onExportPdf = {
                            saveActivePage()
                            hapticManager.performButtonTapHaptic()
                            Toast.makeText(context, "Rendering high-quality PDF with annotations...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                val currentPages = repository.getPagesList(noteId)
                                val exported = PdfHelper.exportNoteToPdf(
                                    context = context,
                                    noteTitle = note?.title ?: "Note",
                                    pages = currentPages,
                                    pdfFilePath = note?.pdfFilePath
                                )
                                Toast.makeText(context, "High-Quality PDF created! Opening share...", Toast.LENGTH_SHORT).show()
                                PdfHelper.sharePdfFile(context, exported, note?.title ?: "Note")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!isTabletLandscape) {
                    EditorToolbar(
                        isVertical = false,
                        activeTool = activeTool,
                        currentColor = activeColor,
                        currentStrokeWidth = activeStrokeWidth,
                        currentShapeType = activeShapeType,
                        rulerVisible = rulerState.isVisible,
                        stylusOnly = stylusOnlyInking,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                        onSelectTool = {
                            activeTool = it
                            hapticManager.performToolSwitchHaptic()
                        },
                        onSelectColor = {
                            activeColor = it
                            hapticManager.performToolSwitchHaptic()
                        },
                        onSelectStrokeWidth = { activeStrokeWidth = it },
                        onSelectShapeType = {
                            activeShapeType = it
                            hapticManager.performToolSwitchHaptic()
                        },
                        onToggleRuler = {
                            rulerState = rulerState.copy(isVisible = !rulerState.isVisible)
                            hapticManager.performToolSwitchHaptic()
                        },
                        onToggleStylusOnly = {
                            stylusOnlyInking = !stylusOnlyInking
                            hapticManager.performToolSwitchHaptic()
                            coroutineScope.launch { prefs.setStylusOnly(stylusOnlyInking) }
                            Toast.makeText(
                                context,
                                if (stylusOnlyInking) "Stylus-only mode enabled (Palm Rejection Active)" else "Touch & Stylus mode active",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onUndo = {
                            if (undoStack.isNotEmpty()) {
                                hapticManager.performButtonTapHaptic()
                                val lastSnapshot = undoStack.removeAt(undoStack.size - 1)
                                redoStack.add(
                                    PageSnapshot(
                                        strokes = currentStrokes.toList(),
                                        shapes = currentShapes.toList(),
                                        textBlocks = currentTextBlocks.toList(),
                                        stickers = currentStickers.toList(),
                                        layers = currentLayers.toList()
                                    )
                                )
                                currentStrokes.clear()
                                currentStrokes.addAll(lastSnapshot.strokes)
                                currentShapes.clear()
                                currentShapes.addAll(lastSnapshot.shapes)
                                currentTextBlocks.clear()
                                currentTextBlocks.addAll(lastSnapshot.textBlocks)
                                currentStickers.clear()
                                currentStickers.addAll(lastSnapshot.stickers)
                                if (lastSnapshot.layers.isNotEmpty()) {
                                    currentLayers.clear()
                                    currentLayers.addAll(lastSnapshot.layers)
                                    if (currentLayers.none { it.id == activeLayerId }) {
                                        activeLayerId = currentLayers.first().id
                                    }
                                }
                                saveActivePage()
                            }
                        },
                        onRedo = {
                            if (redoStack.isNotEmpty()) {
                                val nextSnapshot = redoStack.removeAt(redoStack.size - 1)
                                undoStack.add(
                                    PageSnapshot(
                                        strokes = currentStrokes.toList(),
                                        shapes = currentShapes.toList(),
                                        textBlocks = currentTextBlocks.toList(),
                                        stickers = currentStickers.toList(),
                                        layers = currentLayers.toList()
                                    )
                                )
                                currentStrokes.clear()
                                currentStrokes.addAll(nextSnapshot.strokes)
                                currentShapes.clear()
                                currentShapes.addAll(nextSnapshot.shapes)
                                currentTextBlocks.clear()
                                currentTextBlocks.addAll(nextSnapshot.textBlocks)
                                currentStickers.clear()
                                currentStickers.addAll(nextSnapshot.stickers)
                                if (nextSnapshot.layers.isNotEmpty()) {
                                    currentLayers.clear()
                                    currentLayers.addAll(nextSnapshot.layers)
                                    if (currentLayers.none { it.id == activeLayerId }) {
                                        activeLayerId = currentLayers.first().id
                                    }
                                }
                                saveActivePage()
                            }
                        },
                        onOpenAiCompanion = { showAiCompanionSheet = true },
                        onAddSticker = { key ->
                            pushUndoSnapshot()
                            currentStickers.add(
                                StickerAnnotation(
                                    stickerKey = key,
                                    x = 300f,
                                    y = 400f,
                                    scale = 1.0f
                                )
                            )
                            saveActivePage()
                        },
                        onAddImage = { photoPicker.launch("image/*") },
                        isLayersOpen = showLayersPanel,
                        onToggleLayers = {
                            showLayersPanel = !showLayersPanel
                            hapticManager.performToolSwitchHaptic()
                        }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTabletLandscape) {
                    LiquidGlassPillSidebar(
                        title = note?.title ?: "Note Editor",
                        subtitle = if (note?.isPdf == true) "PDF Doc" else activeTemplate.name.replace("_", " "),
                        currentPage = currentPageIndex,
                        totalPages = pagesState.size,
                        autoSnapEnabled = autoCorrectionEnabled,
                        onBack = onBack,
                        onTitleClick = { showRenameDialog = true },
                        onPreviousPage = {
                            if (currentPageIndex > 0) {
                                hapticManager.performPageTurnHaptic()
                                saveActivePage()
                                currentPageIndex--
                            }
                        },
                        onNextPage = {
                            if (currentPageIndex < pagesState.size - 1) {
                                hapticManager.performPageTurnHaptic()
                                saveActivePage()
                                currentPageIndex++
                            }
                        },
                        onPagesOverview = {
                            hapticManager.performButtonTapHaptic()
                            showPagesOverview = true
                        },
                        onToggleAutoSnap = {
                            autoCorrectionEnabled = !autoCorrectionEnabled
                            hapticManager.performToolSwitchHaptic()
                            coroutineScope.launch { prefs.setAutoSnap(autoCorrectionEnabled) }
                            Toast.makeText(
                                context,
                                if (autoCorrectionEnabled) "Auto Shape & Line Snap: ON" else "Auto Snap: OFF",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onExportPdf = {
                            saveActivePage()
                            hapticManager.performButtonTapHaptic()
                            Toast.makeText(context, "Rendering high-quality PDF with annotations...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                val currentPages = repository.getPagesList(noteId)
                                val exported = PdfHelper.exportNoteToPdf(
                                    context = context,
                                    noteTitle = note?.title ?: "Note",
                                    pages = currentPages,
                                    pdfFilePath = note?.pdfFilePath
                                )
                                Toast.makeText(context, "High-Quality PDF created! Opening share...", Toast.LENGTH_SHORT).show()
                                PdfHelper.sharePdfFile(context, exported, note?.title ?: "Note")
                            }
                        },
                        onTriggerAiCompanion = { showAiCompanionSheet = true }
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        EditorToolbar(
                            isVertical = true,
                            activeTool = activeTool,
                            currentColor = activeColor,
                            currentStrokeWidth = activeStrokeWidth,
                            currentShapeType = activeShapeType,
                            rulerVisible = rulerState.isVisible,
                            stylusOnly = stylusOnlyInking,
                            canUndo = undoStack.isNotEmpty(),
                            canRedo = redoStack.isNotEmpty(),
                            onSelectTool = {
                                activeTool = it
                                hapticManager.performToolSwitchHaptic()
                            },
                            onSelectColor = {
                                activeColor = it
                                hapticManager.performToolSwitchHaptic()
                            },
                            onSelectStrokeWidth = { activeStrokeWidth = it },
                            onSelectShapeType = {
                                activeShapeType = it
                                hapticManager.performToolSwitchHaptic()
                            },
                            onToggleRuler = {
                                rulerState = rulerState.copy(isVisible = !rulerState.isVisible)
                                hapticManager.performToolSwitchHaptic()
                            },
                            onToggleStylusOnly = {
                                stylusOnlyInking = !stylusOnlyInking
                                hapticManager.performToolSwitchHaptic()
                                coroutineScope.launch { prefs.setStylusOnly(stylusOnlyInking) }
                                Toast.makeText(
                                    context,
                                    if (stylusOnlyInking) "Stylus-only mode enabled (Palm Rejection Active)" else "Touch & Stylus mode active",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onUndo = {
                                if (undoStack.isNotEmpty()) {
                                    hapticManager.performButtonTapHaptic()
                                    val lastSnapshot = undoStack.removeAt(undoStack.size - 1)
                                    redoStack.add(
                                        PageSnapshot(
                                            strokes = currentStrokes.toList(),
                                            shapes = currentShapes.toList(),
                                            textBlocks = currentTextBlocks.toList(),
                                            stickers = currentStickers.toList()
                                        )
                                    )
                                    currentStrokes.clear()
                                    currentStrokes.addAll(lastSnapshot.strokes)
                                    currentShapes.clear()
                                    currentShapes.addAll(lastSnapshot.shapes)
                                    currentTextBlocks.clear()
                                    currentTextBlocks.addAll(lastSnapshot.textBlocks)
                                    currentStickers.clear()
                                    currentStickers.addAll(lastSnapshot.stickers)
                                    saveActivePage()
                                }
                            },
                            onRedo = {
                                if (redoStack.isNotEmpty()) {
                                    val nextSnapshot = redoStack.removeAt(redoStack.size - 1)
                                    undoStack.add(
                                        PageSnapshot(
                                            strokes = currentStrokes.toList(),
                                            shapes = currentShapes.toList(),
                                            textBlocks = currentTextBlocks.toList(),
                                            stickers = currentStickers.toList()
                                        )
                                    )
                                    currentStrokes.clear()
                                    currentStrokes.addAll(nextSnapshot.strokes)
                                    currentShapes.clear()
                                    currentShapes.addAll(nextSnapshot.shapes)
                                    currentTextBlocks.clear()
                                    currentTextBlocks.addAll(nextSnapshot.textBlocks)
                                    currentStickers.clear()
                                    currentStickers.addAll(nextSnapshot.stickers)
                                    saveActivePage()
                                }
                            },
                            onOpenAiCompanion = { showAiCompanionSheet = true },
                            onAddSticker = { key ->
                                pushUndoSnapshot()
                                currentStickers.add(
                                    StickerAnnotation(
                                        stickerKey = key,
                                        x = 300f,
                                        y = 400f,
                                        scale = 1.0f
                                    )
                                )
                                saveActivePage()
                            },
                            onAddImage = { photoPicker.launch("image/*") }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Note Drawing Canvas
                    NoteCanvasView(
                        modifier = Modifier.fillMaxSize(),
                        template = activeTemplate,
                        pdfBitmap = pdfBitmap,
                        currentTool = activeTool,
                        currentColor = activeColor,
                        currentStrokeWidth = activeStrokeWidth,
                        currentShapeType = activeShapeType,
                        rulerState = rulerState,
                        stylusOnlyInking = stylusOnlyInking,
                        autoCorrectionEnabled = autoCorrectionEnabled,
                        pressureMult = pressureMult,
                        strokes = currentStrokes,
                        shapes = currentShapes,
                        textBlocks = currentTextBlocks,
                        stickers = currentStickers,
                        photos = currentPhotos,
                        layers = currentLayers,
                        activeLayerId = activeLayerId,
                        hapticManager = hapticManager,
                        onAddStroke = { stroke ->
                            pushUndoSnapshot()
                            currentStrokes.add(stroke)
                            saveActivePage()
                        },
                        onEraseStroke = { strokeId ->
                            pushUndoSnapshot()
                            currentStrokes.removeAll { it.id == strokeId }
                            saveActivePage()
                        },
                        onEraseShape = { id ->
                            pushUndoSnapshot()
                            currentShapes.removeAll { it.id == id }
                            saveActivePage()
                        },
                        onEraseText = { id ->
                            pushUndoSnapshot()
                            currentTextBlocks.removeAll { it.id == id }
                            saveActivePage()
                        },
                        onEraseSticker = { id ->
                            pushUndoSnapshot()
                            currentStickers.removeAll { it.id == id }
                            saveActivePage()
                        },
                        onLassoDelete = { sIds, shIds, tIds, stIds -> deleteLassoSelection(sIds, shIds, tIds, stIds) },
                        onLassoDuplicate = { sIds, shIds, tIds, stIds -> duplicateLassoSelection(sIds, shIds, tIds, stIds) },
                        onAddShape = { shape ->
                            pushUndoSnapshot()
                            currentShapes.add(shape)
                            saveActivePage()
                        },
                        onTapForText = { offset ->
                            pendingTextOffset = offset
                        },
                        onTapTextItem = { textAnnotation ->
                            editingTextBlock = textAnnotation
                        }
                    )

                    // On-screen Interactive Ruler Widget
                    if (rulerState.isVisible) {
                        InteractiveRulerWidget(
                            rulerState = rulerState,
                            onRulerChange = { rulerState = it },
                            onClose = { rulerState = rulerState.copy(isVisible = false) }
                        )
                    }

                    // Floating Animated Liquid Glass AI Companion
                    FloatingLiquidGlassAiCompanion(
                        isPdf = note?.isPdf == true,
                        noteTitle = note?.title ?: "Note",
                        currentPageIndex = currentPageIndex,
                        onInsertNotes = { generatedNotes ->
                            pushUndoSnapshot()
                            val newTextBlock = TextAnnotation(
                                id = "ai_note_${System.currentTimeMillis()}",
                                text = generatedNotes,
                                x = 60f,
                                y = 140f,
                                fontSize = 14f,
                                isBold = false,
                                color = 0xFF2D3748L
                            )
                            currentTextBlocks.add(newTextBlock)
                            saveActivePage()
                            Toast.makeText(context, "AI Lesson Notes added to page!", Toast.LENGTH_SHORT).show()
                        },
                        onInsertDiagramStrokes = { strokes, shapes ->
                            pushUndoSnapshot()
                            currentStrokes.addAll(strokes)
                            currentShapes.addAll(shapes)
                            saveActivePage()
                            Toast.makeText(context, "AI Diagram inserted directly into canvas!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }

    // Real-Time PDF Page Thumbnail Navigation Bottom Sheet / Drawer
    if (showPagesOverview) {
        PdfPageThumbnailNavigationSheet(
            note = note,
            pages = pagesState,
            currentPageIndex = currentPageIndex,
            hapticManager = hapticManager,
            onSelectPage = { selectedIdx ->
                saveActivePage()
                currentPageIndex = selectedIdx
                showPagesOverview = false
            },
            onAddPageWithTemplate = { newTemplate ->
                coroutineScope.launch {
                    val newPage = repository.addPage(noteId, newTemplate)
                    saveActivePage()
                    currentPageIndex = pagesState.size
                    showPagesOverview = false
                }
            },
            onDeletePage = { pageToDelete ->
                coroutineScope.launch {
                    repository.deletePage(noteId, pageToDelete.id)
                    if (currentPageIndex >= pagesState.size - 1) {
                        currentPageIndex = (pagesState.size - 2).coerceAtLeast(0)
                    }
                }
            },
            onDuplicatePage = { pageToDup ->
                coroutineScope.launch {
                    saveActivePage()
                    repository.duplicatePage(noteId, pageToDup.id)
                    Toast.makeText(context, "Page duplicated", Toast.LENGTH_SHORT).show()
                }
            },
            onMovePage = { from, to ->
                coroutineScope.launch {
                    saveActivePage()
                    repository.movePage(noteId, from, to)
                    currentPageIndex = to
                }
            },
            onDismiss = { showPagesOverview = false }
        )
    }

    // Text Insertion Dialog
    val pendingOffset = pendingTextOffset
    if (pendingOffset != null) {
        TextEditDialog(
            initialText = "",
            initialSize = 18f,
            initialBold = false,
            onConfirm = { text, size, isBold ->
                pushUndoSnapshot()
                currentTextBlocks.add(
                    TextAnnotation(
                        text = text,
                        x = pendingOffset.x,
                        y = pendingOffset.y,
                        fontSize = size,
                        color = activeColor,
                        isBold = isBold
                    )
                )
                saveActivePage()
                pendingTextOffset = null
            },
            onDismiss = { pendingTextOffset = null }
        )
    }

    // Text Editing Dialog
    val editingBlock = editingTextBlock
    if (editingBlock != null) {
        TextEditDialog(
            initialText = editingBlock.text,
            initialSize = editingBlock.fontSize,
            initialBold = editingBlock.isBold,
            onConfirm = { text, size, isBold ->
                pushUndoSnapshot()
                val idx = currentTextBlocks.indexOfFirst { it.id == editingBlock.id }
                if (idx != -1) {
                    currentTextBlocks[idx] = editingBlock.copy(text = text, fontSize = size, isBold = isBold)
                }
                saveActivePage()
                editingTextBlock = null
            },
            onDelete = {
                pushUndoSnapshot()
                currentTextBlocks.removeAll { it.id == editingBlock.id }
                saveActivePage()
                editingTextBlock = null
            },
            onDismiss = { editingTextBlock = null }
        )
    }

    // Liquid Glass Note Rename Dialog
    if (showRenameDialog) {
        LiquidGlassInputDialog(
            title = "Rename Notebook",
            subtitle = "Choose a clear, descriptive title",
            initialValue = note?.title ?: "",
            placeholder = "Notebook title...",
            confirmText = "Update",
            icon = Icons.Default.Edit,
            onConfirm = { newTitle ->
                coroutineScope.launch {
                    repository.renameNote(noteId, newTitle)
                    note = repository.getNote(noteId)
                }
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    // AI Study Companion Sheet
    if (showAiCompanionSheet) {
        AiStudyCompanionSheet(
            sheetState = aiSheetState,
            availableNotes = folderNotes,
            currentNoteTitle = note?.title ?: "Current Note",
            onDismiss = { showAiCompanionSheet = false },
            onInsertIntoNote = { companionText ->
                pushUndoSnapshot()
                currentTextBlocks.add(
                    TextAnnotation(
                        text = companionText,
                        x = 80f,
                        y = 120f + (currentTextBlocks.size * 90f),
                        fontSize = 16f,
                        color = activeColor,
                        isBold = false
                    )
                )
                saveActivePage()
                Toast.makeText(context, "Study companion notes inserted!", Toast.LENGTH_SHORT).show()
            },
            onCreateNewStudyNote = { title, companionText ->
                coroutineScope.launch {
                    val newNoteId = repository.createNote(
                        title = title,
                        folderId = note?.folderId,
                        template = PaperTemplate.CORNELL
                    )
                    // Insert content into the new note
                    val newPages = repository.getPagesList(newNoteId)
                    if (newPages.isNotEmpty()) {
                        val textList = listOf(
                            TextAnnotation(
                                text = companionText,
                                x = 80f,
                                y = 140f,
                                fontSize = 16f,
                                color = 0xFF2D3748L
                            )
                        )
                        repository.updatePage(
                            newPages[0].copy(
                                textBlocksJson = SerializationHelpers.textBlocksToJson(textList)
                            )
                        )
                    }
                    Toast.makeText(context, "Created study notebook: $title", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
