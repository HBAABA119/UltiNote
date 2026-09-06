package com.ultinote.app.canvas

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.canvas.CatmullRomSplineInterpolator
import com.ultinote.app.canvas.GeometricShapeKind
import com.ultinote.app.canvas.ShapeRecognitionService
import com.ultinote.app.data.model.DrawingLayer
import com.ultinote.app.data.model.DrawingStroke
import com.ultinote.app.data.model.LayerBlendMode
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.data.model.PhotoAnnotation
import com.ultinote.app.data.model.ShapeAnnotation
import com.ultinote.app.data.model.ShapeType
import com.ultinote.app.data.model.StickerAnnotation
import com.ultinote.app.data.model.StickerCatalog
import com.ultinote.app.data.model.StrokePoint
import com.ultinote.app.data.model.TextAnnotation
import com.ultinote.app.data.model.ToolType
import com.ultinote.app.pdf.PdfHelper
import com.ultinote.app.ui.components.LiquidGlassCard
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.util.HapticFeedbackManager
import com.ultinote.app.util.rememberHapticFeedbackManager
import java.io.File
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun LayerBlendMode.toComposeBlendMode(): BlendMode = when (this) {
    LayerBlendMode.NORMAL -> BlendMode.SrcOver
    LayerBlendMode.MULTIPLY -> BlendMode.Multiply
    LayerBlendMode.SCREEN -> BlendMode.Screen
    LayerBlendMode.DARKEN -> BlendMode.Darken
    LayerBlendMode.LIGHTEN -> BlendMode.Lighten
    LayerBlendMode.OVERLAY -> BlendMode.Overlay
}

@Composable
fun NoteCanvasView(
    modifier: Modifier = Modifier,
    template: PaperTemplate,
    pdfBitmap: Bitmap? = null,
    currentTool: ToolType,
    currentColor: Long,
    currentStrokeWidth: Float,
    currentShapeType: ShapeType,
    rulerState: RulerState,
    stylusOnlyInking: Boolean = false,
    autoCorrectionEnabled: Boolean = true,
    pressureMult: Float = 1.0f,
    strokes: List<DrawingStroke>,
    shapes: List<ShapeAnnotation>,
    textBlocks: List<TextAnnotation>,
    stickers: List<StickerAnnotation>,
    photos: List<PhotoAnnotation> = emptyList(),
    layers: List<DrawingLayer> = emptyList(),
    activeLayerId: String = "default",
    hapticManager: HapticFeedbackManager = rememberHapticFeedbackManager(),
    onAddStroke: (DrawingStroke) -> Unit,
    onEraseStroke: (String) -> Unit,
    onEraseShape: (String) -> Unit = {},
    onEraseText: (String) -> Unit = {},
    onEraseSticker: (String) -> Unit = {},
    onAddShape: (ShapeAnnotation) -> Unit,
    onTapForText: (Offset) -> Unit,
    onTapTextItem: (TextAnnotation) -> Unit,
    onLassoDelete: (strokeIds: Set<String>, shapeIds: Set<String>, textIds: Set<String>, stickerIds: Set<String>) -> Unit = { _, _, _, _ -> },
    onLassoDuplicate: (strokeIds: Set<String>, shapeIds: Set<String>, textIds: Set<String>, stickerIds: Set<String>) -> Unit = { _, _, _, _ -> }
) {
    val palette = LocalKomorebiPalette.current
    val textMeasurer = rememberTextMeasurer()

    // Smooth Pan & Zoom State for Digital Planning Workflows
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Live in-progress drawing & shape state
    val activePoints = remember { mutableStateListOf<StrokePoint>() }
    var shapeDragStart by remember { mutableStateOf<Offset?>(null) }
    var shapeDragCurrent by remember { mutableStateOf<Offset?>(null) }
    var previousEraserPos by remember { mutableStateOf<Offset?>(null) }

    // Lasso rectangular selection (reliable for school use on finger + stylus)
    var lassoStart by remember { mutableStateOf<Offset?>(null) }
    var lassoCurrent by remember { mutableStateOf<Offset?>(null) }
    var selectedStrokeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedShapeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedTextIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedStickerIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    fun clearSelection() {
        selectedStrokeIds = emptySet()
        selectedShapeIds = emptySet()
        selectedTextIds = emptySet()
        selectedStickerIds = emptySet()
    }

    // Photo bitmap cache (decoded off the draw path)
    var photoBitmaps by remember { mutableStateOf<Map<String, androidx.compose.ui.graphics.ImageBitmap>>(emptyMap()) }
    androidx.compose.runtime.LaunchedEffect(photos) {
        try {
            val map = mutableMapOf<String, androidx.compose.ui.graphics.ImageBitmap>()
            for (ph in photos) {
                val f = File(ph.filePath)
                if (!f.exists()) continue
                // Downsample to avoid OOM on large classroom photos / textbook scans
                val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath, opts)
                if (bmp != null) {
                    map[ph.id] = bmp.asImageBitmap()
                    // free native copy after upload; compose holds its own
                    if (!bmp.isRecycled) bmp.recycle()
                }
            }
            photoBitmaps = map
        } catch (_: Exception) { }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasDeskMat)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
                // Text tap detector
                .pointerInput(currentTool) {
                    detectTapGestures { tapOffset ->
                        val localOffset = (tapOffset - panOffset) / zoomScale
                        if (currentTool == ToolType.TEXT) {
                            val tappedBlock = textBlocks.find { b ->
                                abs(b.x - localOffset.x) < 100f && abs(b.y - localOffset.y) < 40f
                            }
                            if (tappedBlock != null) {
                                onTapTextItem(tappedBlock)
                            } else {
                                onTapForText(localOffset)
                            }
                        }
                    }
                }
                // High-performance combined gesture recognizer with Catmull-Rom spline interpolation & multi-layer awareness
                // Supports: pen pressure, S-Pen eraser button, 2-finger zoom, stylus-only palm rejection, lasso select, precision eraser
                .pointerInput(currentTool, currentColor, currentStrokeWidth, rulerState, currentShapeType, stylusOnlyInking, activeLayerId, layers) {
                    awaitEachGesture {
                        val activeLayer = layers.find { it.id == activeLayerId }
                        if (activeLayer?.isLocked == true) {
                            // Layer is locked: reject drawing operations to preserve artwork
                            return@awaitEachGesture
                        }

                        val splineStreamFilter = CatmullRomSplineInterpolator.StreamFilter()
                        splineStreamFilter.reset()

                        val down = awaitFirstDown(requireUnconsumed = false)
                        var isMultiTouchPinch = false

                        // S-Pen / USI barrel-button eraser arrives as Eraser type on newer Compose. Treat as eraser instantly.
                        val downIsEraserButton = try {
                            down.type == PointerType.Eraser
                        } catch (_: Exception) { false }

                        val isDrawingTool = currentTool in listOf(
                            ToolType.PEN_BALLPOINT,
                            ToolType.PEN_FOUNTAIN,
                            ToolType.PEN_BRUSH,
                            ToolType.HIGHLIGHTER,
                            ToolType.RULER,
                            ToolType.SHAPE
                        )
                        val effectiveTool = if (downIsEraserButton) ToolType.ERASER_STROKE else currentTool
                        if (effectiveTool in listOf(ToolType.PEN_BALLPOINT, ToolType.PEN_FOUNTAIN, ToolType.PEN_BRUSH, ToolType.HIGHLIGHTER, ToolType.RULER, ToolType.SHAPE)) {
                            hapticManager.performStrokeStartHaptic()
                            if (effectiveTool != ToolType.SHAPE) {
                                val initPos = (down.position - panOffset) / zoomScale
                                val initPressure = if (down.pressure > 0.05f) down.pressure else 1.0f
                                val initPt = StrokePoint(initPos.x, initPos.y, initPressure)
                                val smoothedInitial = splineStreamFilter.push(initPt)
                                activePoints.addAll(smoothedInitial)
                            }
                        }
                        if (effectiveTool == ToolType.LASSO) {
                            val lp = (down.position - panOffset) / zoomScale
                            lassoStart = lp
                            lassoCurrent = lp
                            // starting a new lasso clears old selection
                            clearSelection()
                        }

                        do {
                            val event = awaitPointerEvent()
                            val pointerCount = event.changes.size

                            if (pointerCount >= 2) {
                                // Multi-touch: two-finger pinch-to-zoom and panning — always allowed, even in stylus-only mode
                                isMultiTouchPinch = true
                                activePoints.clear()
                                shapeDragStart = null
                                shapeDragCurrent = null
                                lassoStart = null
                                lassoCurrent = null

                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                val newScale = (zoomScale * zoom).coerceIn(0.5f, 4.0f)
                                zoomScale = newScale
                                panOffset += pan

                                event.changes.forEach { it.consume() }
                            } else if (!isMultiTouchPinch && pointerCount == 1) {
                                val change = event.changes[0]
                                val isStylus = change.type == PointerType.Stylus
                                val isEraserBtn = try { change.type == PointerType.Eraser } catch (_: Exception) { false }

                                // If user enabled palm-rejection stylus-only inking, ignore single finger touches for drawing
                                // Finger pans instead. Stylus + S-Pen eraser always draw/erase.
                                if (stylusOnlyInking && !isStylus && !isEraserBtn && effectiveTool != ToolType.LASSO) {
                                    // Finger pans if stylus-only is active
                                    if (change.positionChanged()) {
                                        panOffset += (change.position - change.previousPosition)
                                        change.consume()
                                    }
                                } else {
                                    // Active inking or erasing
                                    val localPos = (change.position - panOffset) / zoomScale
                                    // Pressure fallback: some fingers / old screens report 0. Treat as 1.0.
                                    val pressure = if (change.pressure > 0.05f) change.pressure else 1.0f

                                    if (change.pressed) {
                                        val toolNow = if (isEraserBtn) ToolType.ERASER_STROKE else effectiveTool
                                        when (toolNow) {
                                            ToolType.PEN_BALLPOINT,
                                            ToolType.PEN_FOUNTAIN,
                                            ToolType.PEN_BRUSH,
                                            ToolType.HIGHLIGHTER,
                                            ToolType.RULER -> {
                                                val finalPos = if (rulerState.isVisible) {
                                                    RulerGeometry.snapPointToRulerEdge(localPos, rulerState) ?: localPos
                                                } else {
                                                    localPos
                                                }
                                                // Real-time Catmull-Rom spline stream interpolation
                                                val rawPoint = StrokePoint(finalPos.x, finalPos.y, pressure)
                                                val smoothedPoints = splineStreamFilter.push(rawPoint)
                                                activePoints.addAll(smoothedPoints)
                                                change.consume()
                                            }

                                            ToolType.SHAPE -> {
                                                if (shapeDragStart == null) {
                                                    shapeDragStart = localPos
                                                }
                                                shapeDragCurrent = localPos
                                                change.consume()
                                            }

                                            ToolType.LASSO -> {
                                                lassoCurrent = localPos
                                                change.consume()
                                            }

                                            ToolType.ERASER_STROKE -> {
                                                eraseAtPointExtended(localPos, previousEraserPos, 36f, strokes, shapes, textBlocks, stickers, onEraseStroke, onEraseShape, onEraseText, onEraseSticker)
                                                previousEraserPos = localPos
                                                change.consume()
                                            }

                                            ToolType.ERASER_PRECISION -> {
                                                eraseAtPointExtended(localPos, previousEraserPos, 14f, strokes, shapes, textBlocks, stickers, onEraseStroke, onEraseShape, onEraseText, onEraseSticker)
                                                previousEraserPos = localPos
                                                change.consume()
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Gesture completed / lifted
                        previousEraserPos = null
                        if (!isMultiTouchPinch) {
                            if (isDrawingTool) {
                                hapticManager.performStrokeEndHaptic()
                            }
                            when (effectiveTool) {
                                ToolType.PEN_BALLPOINT,
                                ToolType.PEN_FOUNTAIN,
                                ToolType.PEN_BRUSH,
                                ToolType.HIGHLIGHTER,
                                ToolType.RULER -> {
                                    val finalTail = splineStreamFilter.finish()
                                    activePoints.addAll(finalTail)

                                    if (activePoints.size >= 2) {
                                        val isHighlighter = effectiveTool == ToolType.HIGHLIGHTER
                                        val rawStroke = DrawingStroke(
                                             points = activePoints.toList(),
                                             color = currentColor,
                                             strokeWidth = currentStrokeWidth,
                                             toolType = effectiveTool,
                                             alpha = if (isHighlighter) 0.38f else 1.0f,
                                             isHighlighter = isHighlighter,
                                             layerId = activeLayerId
                                        )

                                        if (autoCorrectionEnabled && effectiveTool != ToolType.HIGHLIGHTER && effectiveTool != ToolType.RULER) {
                                            val shapeResult = ShapeRecognitionService.recognize(rawStroke, activeLayerId)
                                            if (shapeResult.snappedShape != null) {
                                                hapticManager.performSnapHaptic()
                                                onAddShape(shapeResult.snappedShape)
                                            } else {
                                                val result = StrokeAutoCorrection.analyzeAndCorrectStroke(rawStroke, enabled = true)
                                                if (result.correctedShape != null) {
                                                    hapticManager.performSnapHaptic()
                                                    onAddShape(result.correctedShape.copy(layerId = activeLayerId))
                                                } else if (result.correctedStroke != null) {
                                                    hapticManager.performSnapHaptic()
                                                    onAddStroke(result.correctedStroke.copy(layerId = activeLayerId))
                                                } else {
                                                    onAddStroke(rawStroke)
                                                }
                                            }
                                        } else {
                                            onAddStroke(rawStroke)
                                        }
                                    }
                                    activePoints.clear()
                                }

                                ToolType.SHAPE -> {
                                    val start = shapeDragStart
                                    val end = shapeDragCurrent
                                    if (start != null && end != null && hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()) > 15) {
                                        val shape = ShapeAnnotation(
                                            type = currentShapeType,
                                            startX = start.x,
                                            startY = start.y,
                                            endX = end.x,
                                            endY = end.y,
                                            strokeWidth = currentStrokeWidth,
                                            color = currentColor,
                                            isFilled = false,
                                            layerId = activeLayerId
                                        )
                                        hapticManager.performSnapHaptic()
                                        onAddShape(shape)
                                    }
                                    shapeDragStart = null
                                    shapeDragCurrent = null
                                }

                                ToolType.LASSO -> {
                                    val s = lassoStart
                                    val e = lassoCurrent
                                    if (s != null && e != null && hypot((e.x - s.x).toDouble(), (e.y - s.y).toDouble()) > 20) {
                                        val left = minOf(s.x, e.x)
                                        val right = maxOf(s.x, e.x)
                                        val top = minOf(s.y, e.y)
                                        val bottom = maxOf(s.y, e.y)
                                        fun inRect(x: Float, y: Float) = x in left..right && y in top..bottom
                                        val selStrokes = strokes.filter { st -> st.points.any { inRect(it.x, it.y) } }.map { it.id }.toSet()
                                        val selShapes = shapes.filter { sh ->
                                            inRect(sh.startX, sh.startY) || inRect(sh.endX, sh.endY) ||
                                                    inRect((sh.startX + sh.endX) / 2f, (sh.startY + sh.endY) / 2f)
                                        }.map { it.id }.toSet()
                                        val selTexts = textBlocks.filter { inRect(it.x, it.y) }.map { it.id }.toSet()
                                        val selStickers = stickers.filter { inRect(it.x, it.y) }.map { it.id }.toSet()
                                        selectedStrokeIds = selStrokes
                                        selectedShapeIds = selShapes
                                        selectedTextIds = selTexts
                                        selectedStickerIds = selStickers
                                        if (selStrokes.isNotEmpty() || selShapes.isNotEmpty() || selTexts.isNotEmpty() || selStickers.isNotEmpty()) {
                                            hapticManager.performSnapHaptic()
                                        }
                                    }
                                    lassoStart = null
                                    lassoCurrent = null
                                }

                                else -> {}
                            }
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw Page Paper Base
            drawRect(color = palette.canvasBackground, size = size)

            // 2. Draw PDF Background Page if present
            if (pdfBitmap != null && !pdfBitmap.isRecycled) {
                val imgBitmap = pdfBitmap.asImageBitmap()
                val scale = min(canvasW / imgBitmap.width.toFloat(), canvasH / imgBitmap.height.toFloat())
                val destW = imgBitmap.width * scale
                val destH = imgBitmap.height * scale
                val destX = (canvasW - destW) / 2f
                val destY = 20f

                drawImage(
                    image = imgBitmap,
                    dstOffset = androidx.compose.ui.unit.IntOffset(destX.toInt(), destY.toInt()),
                    dstSize = androidx.compose.ui.unit.IntSize(destW.toInt(), destH.toInt())
                )
            } else {
                // Draw paper background grid/ruled pattern
                PaperBackgroundRenderer.drawPaperTemplate(
                    scope = this,
                    template = template,
                    palette = palette,
                    width = canvasW,
                    height = canvasH
                )
            }

            // Layer-Aware Rendering Pipeline (Draw ordered by layer order from bottom to top)
            val effectiveLayers = if (layers.isEmpty()) {
                listOf(DrawingLayer(id = "default", name = "Base Layer", isVisible = true, opacity = 1.0f, blendMode = LayerBlendMode.NORMAL, isLocked = false, order = 0))
            } else {
                layers.sortedBy { it.order }
            }

            for (layer in effectiveLayers) {
                if (!layer.isVisible) continue

                val layerAlpha = layer.opacity
                val layerBlend = layer.blendMode.toComposeBlendMode()

                // 3. Draw Saved Highlighter Strokes in this layer
                strokes.filter { (it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty())) && it.isHighlighter }.forEach { stroke ->
                    drawSmoothStroke(stroke = stroke, isLive = false, layerAlpha = layerAlpha, blendMode = layerBlend, pressureMult = pressureMult, isSelected = stroke.id in selectedStrokeIds)
                }

                // 4. Draw Saved Shapes in this layer
                shapes.filter { it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty()) }.forEach { shape ->
                    drawShapeItem(shape = shape, isPreview = false, layerAlpha = layerAlpha, blendMode = layerBlend, isSelected = shape.id in selectedShapeIds)
                }

                // 5. Draw Saved Opaque Pen Strokes in this layer
                strokes.filter { (it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty())) && !it.isHighlighter }.forEach { stroke ->
                    drawSmoothStroke(stroke = stroke, isLive = false, layerAlpha = layerAlpha, blendMode = layerBlend, pressureMult = pressureMult, isSelected = stroke.id in selectedStrokeIds)
                }

                // 6. Draw Saved Text Annotations in this layer
                textBlocks.filter { it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty()) }.forEach { textBlock ->
                    drawTextItem(textBlock = textBlock, textMeasurer = textMeasurer, layerAlpha = layerAlpha, isSelected = textBlock.id in selectedTextIds)
                }

                // 7. Draw Saved Stickers in this layer
                stickers.filter { it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty()) }.forEach { sticker ->
                    drawStickerItem(sticker = sticker, textMeasurer = textMeasurer, layerAlpha = layerAlpha, isSelected = sticker.id in selectedStickerIds)
                }

                // 7b. Draw embedded photos for this layer
                photos.filter { it.layerId == layer.id || (layer.id == "default" && it.layerId.isEmpty()) }.forEach { photo ->
                    val cached = photoBitmaps[photo.id]
                    if (cached != null) {
                        try {
                            drawImage(
                                image = cached,
                                dstOffset = androidx.compose.ui.unit.IntOffset(photo.x.toInt(), photo.y.toInt()),
                                dstSize = androidx.compose.ui.unit.IntSize(photo.width.toInt().coerceAtLeast(20), photo.height.toInt().coerceAtLeast(20))
                            )
                            // subtle frame
                            drawRect(
                                color = Color.White.copy(alpha = 0.85f * layerAlpha),
                                topLeft = Offset(photo.x - 3f, photo.y - 3f),
                                size = Size(photo.width + 6f, photo.height + 6f),
                                style = Stroke(width = 2f)
                            )
                        } catch (_: Exception) { }
                    }
                }
            }

            // 8. Draw Live Drawing Stroke in Progress (with real-time Catmull-Rom smoothing)
            if (activePoints.size >= 2) {
                val isHighlighter = currentTool == ToolType.HIGHLIGHTER
                val activeLayerObj = layers.find { it.id == activeLayerId }
                val liveAlpha = activeLayerObj?.opacity ?: 1.0f
                val liveBlend = activeLayerObj?.blendMode?.toComposeBlendMode() ?: BlendMode.SrcOver
                val liveStroke = DrawingStroke(
                    points = activePoints.toList(),
                    color = currentColor,
                    strokeWidth = currentStrokeWidth,
                    toolType = currentTool,
                    alpha = if (isHighlighter) 0.38f else 1.0f,
                    isHighlighter = isHighlighter,
                    layerId = activeLayerId
                )
                drawSmoothStroke(stroke = liveStroke, isLive = true, layerAlpha = liveAlpha, blendMode = liveBlend, pressureMult = pressureMult)
            }

            // 9. Draw Live Shape Preview in Progress
            val liveStart = shapeDragStart
            val liveEnd = shapeDragCurrent
            if (liveStart != null && liveEnd != null) {
                val activeLayerObj = layers.find { it.id == activeLayerId }
                val liveAlpha = activeLayerObj?.opacity ?: 1.0f
                val liveBlend = activeLayerObj?.blendMode?.toComposeBlendMode() ?: BlendMode.SrcOver
                drawShapeItem(
                    shape = ShapeAnnotation(
                        type = currentShapeType,
                        startX = liveStart.x,
                        startY = liveStart.y,
                        endX = liveEnd.x,
                        endY = liveEnd.y,
                        strokeWidth = currentStrokeWidth,
                        color = currentColor,
                        isFilled = false,
                        layerId = activeLayerId
                    ),
                    isPreview = true,
                    layerAlpha = liveAlpha,
                    blendMode = liveBlend
                )
            }

            // 9b. Lasso selection rectangle
            val ls = lassoStart
            val lc = lassoCurrent
            if (ls != null && lc != null) {
                val left = minOf(ls.x, lc.x)
                val top = minOf(ls.y, lc.y)
                val w = abs(lc.x - ls.x)
                val h = abs(lc.y - ls.y)
                drawRect(
                    color = Color(0xFF2B5F70).copy(alpha = 0.18f),
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
                drawRect(
                    color = Color(0xFF2B5F70),
                    topLeft = Offset(left, top),
                    size = Size(w, h),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Lasso selection action pill
        val selCount = selectedStrokeIds.size + selectedShapeIds.size + selectedTextIds.size + selectedStickerIds.size
        AnimatedVisibility(
            visible = selCount > 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
        ) {
            LiquidGlassCard(
                shape = RoundedCornerShape(50.dp),
                backgroundColor = palette.glassSurface,
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$selCount selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )
                    Text(
                        text = "Duplicate",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.primary,
                        modifier = Modifier.clickable {
                            onLassoDuplicate(selectedStrokeIds, selectedShapeIds, selectedTextIds, selectedStickerIds)
                            clearSelection()
                        }.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "Delete",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A),
                        modifier = Modifier.clickable {
                            onLassoDelete(selectedStrokeIds, selectedShapeIds, selectedTextIds, selectedStickerIds)
                            clearSelection()
                        }.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        color = palette.colorScheme.onSurface,
                        modifier = Modifier.clickable { clearSelection() }.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // Floating Liquid Glass Zoom & Pan HUD for Digital Planning
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = palette.glassSurface,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            zoomScale = (zoomScale - 0.25f).coerceIn(0.5f, 4.0f)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = palette.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${(zoomScale * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable {
                                zoomScale = 1.0f
                                panOffset = Offset.Zero
                            }
                            .padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            zoomScale = (zoomScale + 0.25f).coerceIn(0.5f, 4.0f)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = palette.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Reset Fit Page button
                    IconButton(
                        onClick = {
                            zoomScale = 1.0f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Reset Zoom to 100%",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Low-latency Quadratic Bezier Midpoint Curve Smoothing Engine with Dynamic Stylus Pressure Scaling
private fun DrawScope.drawSmoothStroke(
    stroke: DrawingStroke,
    isLive: Boolean,
    layerAlpha: Float = 1.0f,
    blendMode: BlendMode = BlendMode.SrcOver,
    pressureMult: Float = 1.0f,
    isSelected: Boolean = false
) {
    if (stroke.points.isEmpty()) return

    val strokeColor = Color(stroke.color).copy(alpha = stroke.alpha * layerAlpha)

    // selection glow pass
    if (isSelected) {
        // draw a soft halo behind selected strokes
        val haloPath = Path().apply {
            if (stroke.points.size == 1) {
                val p = stroke.points[0]
                addOval(androidx.compose.ui.geometry.Rect(Offset(p.x - 14f, p.y - 14f), Size(28f, 28f)))
            } else {
                moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) lineTo(stroke.points[i].x, stroke.points[i].y)
            }
        }
        drawPath(
            path = haloPath,
            color = Color(0xFF2B5F70).copy(alpha = 0.35f),
            style = Stroke(width = stroke.strokeWidth + 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    if (stroke.points.size == 1) {
        val p = stroke.points[0]
        val singleWidth = PdfHelper.calculatePressureWidth(stroke.strokeWidth, p.pressure, stroke.toolType, pressureMult)
        drawCircle(
            color = strokeColor,
            radius = singleWidth / 2f,
            center = Offset(p.x, p.y),
            blendMode = blendMode
        )
        return
    }

    // Highlighters maintain uniform translucent ribbon width
    if (stroke.isHighlighter) {
        val path = Path()
        val p0 = stroke.points[0]
        path.moveTo(p0.x, p0.y)

        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            path.quadraticTo(prev.x, prev.y, midX, midY)
        }

        val last = stroke.points.last()
        path.lineTo(last.x, last.y)

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = stroke.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            blendMode = blendMode
        )
        return
    }

    // Dynamic pressure-responsive strokes for Ballpoint, Fountain, and Brush pens
    val points = stroke.points
    var prevMid = Offset(points[0].x, points[0].y)
    var prevWidth = PdfHelper.calculatePressureWidth(stroke.strokeWidth, points[0].pressure, stroke.toolType, pressureMult)

    for (i in 1 until points.size) {
        val pPrev = points[i - 1]
        val pCurr = points[i]
        val currMid = Offset((pPrev.x + pCurr.x) / 2f, (pPrev.y + pCurr.y) / 2f)
        val targetWidth = PdfHelper.calculatePressureWidth(
            stroke.strokeWidth,
            (pPrev.pressure + pCurr.pressure) / 2f,
            stroke.toolType,
            pressureMult
        )

        val segPath = Path().apply {
            moveTo(prevMid.x, prevMid.y)
            quadraticTo(pPrev.x, pPrev.y, currMid.x, currMid.y)
        }

        val segAvgWidth = (prevWidth + targetWidth) / 2f
        drawPath(
            path = segPath,
            color = strokeColor,
            style = Stroke(
                width = segAvgWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            blendMode = blendMode
        )

        prevMid = currMid
        prevWidth = targetWidth
    }

    val lastPoint = points.last()
    val lastWidth = PdfHelper.calculatePressureWidth(stroke.strokeWidth, lastPoint.pressure, stroke.toolType, pressureMult)
    drawLine(
        color = strokeColor,
        start = prevMid,
        end = Offset(lastPoint.x, lastPoint.y),
        strokeWidth = (prevWidth + lastWidth) / 2f,
        cap = StrokeCap.Round,
        blendMode = blendMode
    )
}

// Draw geometric shape
private fun DrawScope.drawShapeItem(
    shape: ShapeAnnotation,
    isPreview: Boolean = false,
    layerAlpha: Float = 1.0f,
    blendMode: BlendMode = BlendMode.SrcOver,
    isSelected: Boolean = false
) {
    val effectiveAlpha = (if (isPreview) 0.65f else 1.0f) * layerAlpha
    val paintColor = Color(shape.color).copy(alpha = effectiveAlpha)
    val strokeStyle = Stroke(
        width = shape.strokeWidth + if (isSelected) 3f else 0f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    when (shape.type) {
        ShapeType.LINE -> {
            drawLine(
                color = paintColor,
                start = Offset(shape.startX, shape.startY),
                end = Offset(shape.endX, shape.endY),
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round,
                blendMode = blendMode
            )
        }

        ShapeType.RECTANGLE -> {
            val left = min(shape.startX, shape.endX)
            val top = min(shape.startY, shape.endY)
            val right = max(shape.startX, shape.endX)
            val bottom = max(shape.startY, shape.endY)
            val rectSize = Size(right - left, bottom - top)

            // Anti-aliased subtle translucent liquid-glass inner fill
            drawRect(
                color = paintColor.copy(alpha = 0.12f * layerAlpha),
                topLeft = Offset(left, top),
                size = rectSize,
                blendMode = blendMode
            )
            // Crisp anti-aliased border
            drawRect(
                color = paintColor,
                topLeft = Offset(left, top),
                size = rectSize,
                style = strokeStyle,
                blendMode = blendMode
            )
        }

        ShapeType.CIRCLE -> {
            val radius = hypot(
                (shape.endX - shape.startX).toDouble(),
                (shape.endY - shape.startY).toDouble()
            ).toFloat() / 2f
            val cx = (shape.startX + shape.endX) / 2f
            val cy = (shape.startY + shape.endY) / 2f

            // Anti-aliased subtle translucent liquid-glass inner fill
            drawCircle(
                color = paintColor.copy(alpha = 0.12f * layerAlpha),
                radius = radius,
                center = Offset(cx, cy),
                blendMode = blendMode
            )
            // Crisp anti-aliased border
            drawCircle(
                color = paintColor,
                radius = radius,
                center = Offset(cx, cy),
                style = strokeStyle,
                blendMode = blendMode
            )
        }

        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                val apexX = (shape.startX + shape.endX) / 2f
                moveTo(apexX, shape.startY)
                lineTo(shape.endX, shape.endY)
                lineTo(shape.startX, shape.endY)
                close()
            }
            // Anti-aliased subtle translucent liquid-glass inner fill
            drawPath(path = path, color = paintColor.copy(alpha = 0.12f * layerAlpha), blendMode = blendMode)
            // Crisp anti-aliased outline
            drawPath(path = path, color = paintColor, style = strokeStyle, blendMode = blendMode)
        }

        ShapeType.ARROW -> {
            drawLine(
                color = paintColor,
                start = Offset(shape.startX, shape.startY),
                end = Offset(shape.endX, shape.endY),
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round,
                blendMode = blendMode
            )
            val angle = Math.atan2(
                (shape.endY - shape.startY).toDouble(),
                (shape.endX - shape.startX).toDouble()
            )
            val arrowHeadLen = 22f
            val arrowAngle = Math.PI / 6.0

            val x1 = shape.endX - arrowHeadLen * Math.cos(angle - arrowAngle).toFloat()
            val y1 = shape.endY - arrowHeadLen * Math.sin(angle - arrowAngle).toFloat()
            val x2 = shape.endX - arrowHeadLen * Math.cos(angle + arrowAngle).toFloat()
            val y2 = shape.endY - arrowHeadLen * Math.sin(angle + arrowAngle).toFloat()

            drawLine(color = paintColor, start = Offset(shape.endX, shape.endY), end = Offset(x1, y1), strokeWidth = shape.strokeWidth, cap = StrokeCap.Round, blendMode = blendMode)
            drawLine(color = paintColor, start = Offset(shape.endX, shape.endY), end = Offset(x2, y2), strokeWidth = shape.strokeWidth, cap = StrokeCap.Round, blendMode = blendMode)
        }

        ShapeType.STAR -> {
            val cx = (shape.startX + shape.endX) / 2f
            val cy = (shape.startY + shape.endY) / 2f
            val outerR = hypot((shape.endX - shape.startX).toDouble(), (shape.endY - shape.startY).toDouble()).toFloat() / 2f
            val innerR = outerR * 0.45f
            val starPath = Path()

            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val theta = i * Math.PI / 5.0 - Math.PI / 2.0
                val px = cx + (r * Math.cos(theta)).toFloat()
                val py = cy + (r * Math.sin(theta)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(path = starPath, color = paintColor.copy(alpha = 0.12f * layerAlpha), blendMode = blendMode)
            drawPath(path = starPath, color = paintColor, style = strokeStyle, blendMode = blendMode)
        }
    }
}

// Draw formatted text annotation
private fun DrawScope.drawTextItem(textBlock: TextAnnotation, textMeasurer: TextMeasurer, layerAlpha: Float = 1.0f, isSelected: Boolean = false) {
    if (isSelected) {
        val measure = textMeasurer.measure(
            text = textBlock.text,
            style = TextStyle(fontSize = textBlock.fontSize.sp, fontWeight = if (textBlock.isBold) FontWeight.Bold else FontWeight.Normal)
        )
        drawRect(
            color = Color(0xFF2B5F70).copy(alpha = 0.22f),
            topLeft = Offset(textBlock.x - 6f, textBlock.y - 6f),
            size = Size(measure.size.width + 12f, measure.size.height + 12f)
        )
    }
    val textResult = textMeasurer.measure(
        text = textBlock.text,
        style = TextStyle(
            fontSize = textBlock.fontSize.sp,
            fontWeight = if (textBlock.isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(textBlock.color).copy(alpha = layerAlpha)
        )
    )

    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(textBlock.x, textBlock.y)
    )
}

// Draw stationery sticker — keys mapped to emoji via StickerCatalog (fixes literal "star" text bug)
private fun DrawScope.drawStickerItem(sticker: StickerAnnotation, textMeasurer: TextMeasurer, layerAlpha: Float = 1.0f, isSelected: Boolean = false) {
    val glyph = StickerCatalog.glyphFor(sticker.stickerKey)
    if (isSelected) {
        drawCircle(
            color = Color(0xFF2B5F70).copy(alpha = 0.25f),
            radius = 26f * sticker.scale,
            center = Offset(sticker.x + 14f * sticker.scale, sticker.y + 14f * sticker.scale)
        )
    }
    val textResult = textMeasurer.measure(
        text = glyph,
        style = TextStyle(fontSize = (28f * sticker.scale).sp, color = Color.White.copy(alpha = layerAlpha))
    )
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(sticker.x, sticker.y)
    )
}

// Stroke-level eraser calculation with true path intersection detection
private fun eraseAtPoint(
    point: Offset,
    previousPoint: Offset?,
    strokes: List<DrawingStroke>,
    onErase: (String) -> Unit
) {
    val eraseRadius = 36f
    for (stroke in strokes) {
        val intersects = StrokeAutoCorrection.doesEraserIntersectStroke(
            eraserCurrent = point,
            eraserPrevious = previousPoint,
            stroke = stroke,
            eraserRadius = eraseRadius
        )
        if (intersects) {
            onErase(stroke.id)
            break
        }
    }
}

// Extended eraser: strokes + shapes + text + stickers. Precision = 14px, stroke = 36px.
private fun eraseAtPointExtended(
    point: Offset,
    previousPoint: Offset?,
    radius: Float,
    strokes: List<DrawingStroke>,
    shapes: List<ShapeAnnotation>,
    texts: List<TextAnnotation>,
    stickers: List<StickerAnnotation>,
    onEraseStroke: (String) -> Unit,
    onEraseShape: (String) -> Unit,
    onEraseText: (String) -> Unit,
    onEraseSticker: (String) -> Unit
) {
    for (stroke in strokes) {
        val hit = try {
            StrokeAutoCorrection.doesEraserIntersectStroke(point, previousPoint, stroke, radius)
        } catch (_: Exception) { false }
        if (hit) { onEraseStroke(stroke.id); return }
    }
    // shapes: distance to bbox / segment
    for (sh in shapes) {
        val inBox = point.x in (minOf(sh.startX, sh.endX) - radius)..(maxOf(sh.startX, sh.endX) + radius) &&
                point.y in (minOf(sh.startY, sh.endY) - radius)..(maxOf(sh.startY, sh.endY) + radius)
        if (inBox) { onEraseShape(sh.id); return }
    }
    for (t in texts) {
        if (abs(t.x - point.x) < 90f + radius && abs(t.y - point.y) < 36f + radius) { onEraseText(t.id); return }
    }
    for (s in stickers) {
        if (hypot((s.x - point.x).toDouble(), (s.y - point.y).toDouble()) < 34f * s.scale + radius) { onEraseSticker(s.id); return }
    }
}
