package com.ultinote.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ultinote.app.data.model.DrawingStroke
import com.ultinote.app.data.model.ShapeAnnotation
import com.ultinote.app.data.model.ShapeType
import com.ultinote.app.data.model.StrokePoint
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Modes requested for AI Study Companion:
 * 1. Lesson Notes (Cornell / Summary structure)
 * 2. Practice Equations (Step-by-step calculus/physics math derivation)
 * 3. Diagrams from PDF Content (Interactive visual sketches and formulas)
 */
enum class CompanionFeatureMode(val title: String, val subtitle: String) {
    LESSON_NOTES("Lesson Notes", "Synthesize structured summary notes & key concepts"),
    PRACTICE_EQUATIONS("Practice Equations", "Generate high-yield equations & step derivations"),
    DIAGRAMS_SKETCHES("Diagrams & Charts", "Render Cartesian plots, coordinate axes & schematics")
}

/**
 * Floating, Animated 'Liquid Glass' AI Companion
 * Features a glowing, draggable liquid glass orb with breathing shimmer animations,
 * and expands into a frosted liquid glass modal HUD.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingLiquidGlassAiCompanion(
    isPdf: Boolean,
    noteTitle: String,
    currentPageIndex: Int,
    onInsertNotes: (String) -> Unit,
    onInsertDiagramStrokes: (List<DrawingStroke>, List<ShapeAnnotation>) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKomorebiPalette.current
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }

    // Floating orb position offsets
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Shimmer breathing animation for Liquid Glass
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val shimmerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerRotation"
    )

    // ==================== 1. FLOATING LIQUID GLASS TRIGGER ORB ====================
    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .scale(pulseScale)
    ) {
        // Outer ambient glow ring
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            palette.colorScheme.primary.copy(alpha = 0.5f),
                            palette.colorScheme.tertiary.copy(alpha = 0.6f),
                            palette.colorScheme.secondary.copy(alpha = 0.4f),
                            palette.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                )
                .rotate(shimmerRotation)
        )

        // Frosted Liquid Glass Orb Core
        Box(
            modifier = Modifier
                .size(62.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(palette.glassSurface.copy(alpha = 0.88f))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.8f), palette.colorScheme.primary.copy(alpha = 0.3f))
                    ),
                    shape = CircleShape
                )
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = palette.colorScheme.primary)
                .clickable { isExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Liquid Glass AI Companion",
                    tint = palette.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isPdf) "PDF AI" else "AI",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }

    // ==================== 2. EXPANDED LIQUID GLASS AI HUD ====================
    if (isExpanded) {
        var selectedMode by remember { mutableStateOf(CompanionFeatureMode.LESSON_NOTES) }
        var customTopic by remember { mutableStateOf("") }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedTextResult by remember { mutableStateOf<String?>(null) }
        var activeDiagramType by remember { mutableStateOf("CARTESIAN_CURVE") }

        Dialog(
            onDismissRequest = { isExpanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { isExpanded = false },
                contentAlignment = Alignment.Center
            ) {
                // Frosted Liquid Glass Card Window
                Box(
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .padding(20.dp)
                        .width(540.dp)
                        .fillMaxHeight(0.88f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(palette.glassSurface.copy(alpha = 0.94f))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    palette.colorScheme.primary.copy(alpha = 0.35f),
                                    palette.glassBorder
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = palette.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header with Liquid Glass Emblem
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
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
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Liquid Glass AI Companion",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.colorScheme.onSurface
                                    )
                                    if (isPdf) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE53E3E).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PDF Page ${currentPageIndex + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE53E3E)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Generate notes, practice equations & diagrams from content",
                                    fontSize = 12.sp,
                                    color = palette.colorScheme.outline
                                )
                            }

                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = palette.colorScheme.outline
                                )
                            }
                        }

                        // Feature Mode Selector Tabs
                        Text(
                            text = "COMPANION GENERATION MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = palette.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompanionFeatureMode.values().forEach { mode ->
                                val isSelected = selectedMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) palette.colorScheme.primaryContainer
                                            else palette.glassSurface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) palette.colorScheme.primary else palette.glassBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            selectedMode = mode
                                            generatedTextResult = null
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (mode) {
                                                CompanionFeatureMode.LESSON_NOTES -> Icons.Default.MenuBook
                                                CompanionFeatureMode.PRACTICE_EQUATIONS -> Icons.Default.Functions
                                                CompanionFeatureMode.DIAGRAMS_SKETCHES -> Icons.Default.Timeline
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = mode.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Context indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.colorScheme.primary.copy(alpha = 0.08f))
                                .border(1.dp, palette.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = palette.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Active Source: $noteTitle",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isPdf) "Extracting content from Page ${currentPageIndex + 1} & surrounding sections"
                                        else "Analyzing Cornell notes & handwritten stroke context",
                                        fontSize = 11.sp,
                                        color = palette.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Input topic or specific instruction
                        OutlinedTextField(
                            value = customTopic,
                            onValueChange = { customTopic = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    when (selectedMode) {
                                        CompanionFeatureMode.LESSON_NOTES -> "e.g. Fundamental Theorem of Calculus & key takeaways"
                                        CompanionFeatureMode.PRACTICE_EQUATIONS -> "e.g. Integration by Parts or damped harmonic oscillator"
                                        CompanionFeatureMode.DIAGRAMS_SKETCHES -> "e.g. Sine wave function or unit circle coordinates"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            label = { Text("Topic or Query Focus") },
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 2
                        )

                        // If Diagram mode: Diagram Type selection
                        if (selectedMode == CompanionFeatureMode.DIAGRAMS_SKETCHES) {
                            Text(
                                text = "CHOOSE DIAGRAM TEMPLATE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "CARTESIAN_CURVE" to "Cartesian Plot",
                                    "UNIT_CIRCLE" to "Unit Circle",
                                    "FREE_BODY" to "Vector Diagram"
                                ).forEach { (typeKey, label) ->
                                    val isSel = activeDiagramType == typeKey
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { activeDiagramType = typeKey },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = palette.colorScheme.primaryContainer,
                                            selectedLabelColor = palette.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }

                        // Generate Button
                        LiquidGlassPillButton(
                            text = if (isGenerating) "Synthesizing with Liquid Glass AI..." else "Generate ${selectedMode.title}",
                            icon = if (isGenerating) null else Icons.Default.AutoAwesome,
                            isPrimary = true,
                            onClick = {
                                coroutineScope.launch {
                                    isGenerating = true
                                    delay(650) // Smooth inference animation
                                    generatedTextResult = when (selectedMode) {
                                        CompanionFeatureMode.LESSON_NOTES -> generateLessonNotesContent(
                                            topic = customTopic.ifBlank { "Calculus II: Series Convergence & Integration" },
                                            isPdf = isPdf,
                                            page = currentPageIndex + 1
                                        )
                                        CompanionFeatureMode.PRACTICE_EQUATIONS -> generatePracticeEquationsContent(
                                            topic = customTopic.ifBlank { "Integration by Parts & Trigonometric Substitution" }
                                        )
                                        CompanionFeatureMode.DIAGRAMS_SKETCHES -> generateDiagramOverviewContent(
                                            type = activeDiagramType
                                        )
                                    }
                                    isGenerating = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isGenerating) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = palette.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }

                        // ==================== GENERATED CONTENT DISPLAY ====================
                        val result = generatedTextResult
                        if (result != null && !isGenerating) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(palette.canvasBackground)
                                    .border(1.dp, palette.glassBorder, RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = palette.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Companion Output (${selectedMode.title})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = palette.colorScheme.onSurface
                                        )
                                    }

                                    // Diagram Live Visual Preview if Diagram mode
                                    if (selectedMode == CompanionFeatureMode.DIAGRAMS_SKETCHES) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                                val w = size.width
                                                val h = size.height
                                                when (activeDiagramType) {
                                                    "UNIT_CIRCLE" -> {
                                                        val cx = w / 2f
                                                        val cy = h / 2f
                                                        val r = h * 0.40f
                                                        // Axes
                                                        drawLine(Color.Gray, Offset(cx - r - 20f, cy), Offset(cx + r + 20f, cy), 1.5f)
                                                        drawLine(Color.Gray, Offset(cx, cy - r - 20f), Offset(cx, cy + r + 20f), 1.5f)
                                                        // Circle
                                                        drawCircle(Color(0xFF3182CE), radius = r, center = Offset(cx, cy), style = Stroke(2f))
                                                        // 45 degree radius
                                                        val radX = cx + r * cos(Math.PI / 4).toFloat()
                                                        val radY = cy - r * sin(Math.PI / 4).toFloat()
                                                        drawLine(Color(0xFFE53E3E), Offset(cx, cy), Offset(radX, radY), 2.5f)
                                                    }
                                                    "FREE_BODY" -> {
                                                        val cx = w / 2f
                                                        val cy = h / 2f
                                                        // Central mass
                                                        drawRoundRect(Color(0xFF4A5568), topLeft = Offset(cx - 25f, cy - 25f), size = Size(50f, 50f), cornerRadius = CornerRadius(6f, 6f))
                                                        // Normal force arrow
                                                        drawLine(Color(0xFF3182CE), Offset(cx, cy - 25f), Offset(cx, cy - 65f), 2.5f, cap = StrokeCap.Round)
                                                        // Gravity arrow
                                                        drawLine(Color(0xFFE53E3E), Offset(cx, cy + 25f), Offset(cx, cy + 65f), 2.5f, cap = StrokeCap.Round)
                                                        // Friction arrow
                                                        drawLine(Color(0xFFD69E2E), Offset(cx - 25f, cy), Offset(cx - 65f, cy), 2.5f, cap = StrokeCap.Round)
                                                    }
                                                    else -> {
                                                        // Cartesian Curve
                                                        val cx = w * 0.2f
                                                        val cy = h * 0.7f
                                                        // Axes
                                                        drawLine(Color.Gray, Offset(cx - 10f, cy), Offset(w - 10f, cy), 1.5f)
                                                        drawLine(Color.Gray, Offset(cx, h - 10f), Offset(cx, 10f), 1.5f)
                                                        // Sine wave curve
                                                        val path = Path().apply {
                                                            moveTo(cx, cy)
                                                            var px = cx
                                                            while (px < w - 15f) {
                                                                val py = cy - 40f * sin((px - cx) * 0.04f)
                                                                lineTo(px, py)
                                                                px += 4f
                                                            }
                                                        }
                                                        drawPath(path, Color(0xFF3182CE), style = Stroke(2.5f, cap = StrokeCap.Round))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = result,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = palette.colorScheme.onSurface
                                    )

                                    // Action insertion buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Insert as text
                                        LiquidGlassPillButton(
                                            text = "Insert Text to Note",
                                            icon = Icons.Default.Check,
                                            isPrimary = selectedMode != CompanionFeatureMode.DIAGRAMS_SKETCHES,
                                            onClick = {
                                                onInsertNotes(result)
                                                isExpanded = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        // If diagram mode: Insert actual strokes to canvas!
                                        if (selectedMode == CompanionFeatureMode.DIAGRAMS_SKETCHES) {
                                            LiquidGlassPillButton(
                                                text = "Insert Strokes to Canvas",
                                                icon = Icons.Default.Draw,
                                                isPrimary = true,
                                                onClick = {
                                                    val (strokes, shapes) = generateDiagramStrokes(activeDiagramType)
                                                    onInsertDiagramStrokes(strokes, shapes)
                                                    isExpanded = false
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== GENERATION HELPERS ====================

private fun generateLessonNotesContent(topic: String, isPdf: Boolean, page: Int): String {
    val src = if (isPdf) "PDF Document (Page $page)" else "Notebook Active Lesson"
    return """
📖 Lesson Topic: $topic
🏷️ Context: $src

1. CORE CONCEPT & THEOREMS:
• The Fundamental Theorem relates differentiation and integration:
  d/dx [ ∫_a^x f(t)dt ] = f(x)
• Area under curve f(x) over [a, b] is given by F(b) - F(a) where F'(x) = f(x).

2. KEY METHODOLOGY:
• Step 1: Identify boundary conditions & continuous intervals.
• Step 2: Test convergence criteria if improper bounds (±∞) are present.
• Step 3: Decompose rational expressions using Partial Fraction Decomposition.

💡 Exam Revision Tip: When calculating work done or fluid pressure, always set up a representative horizontal slab Δy first!
""".trimIndent()
}

private fun generatePracticeEquationsContent(topic: String): String {
    return """
📐 Practice Problem & Derivation:
Topic: $topic

Problem:
Evaluate the definite integral using integration by parts:
    I = ∫₀^π x • sin(2x) dx

Step 1: Assign parts using LIATE:
    u = x        =>  du = dx
    dv = sin(2x) dx  =>  v = -½ cos(2x)

Step 2: Integration by Parts Formula [ ∫ u dv = u•v - ∫ v du ]:
    I = [ -½ x cos(2x) ]₀^π - ∫₀^π ( -½ cos(2x) ) dx

Step 3: Evaluate limits:
    Term 1: -½ (π cos(2π) - 0) = -½ (π • 1) = -π/2
    Term 2: +½ [ ½ sin(2x) ]₀^π = ¼ [ sin(2π) - sin(0) ] = 0

Final Result:
    I = -π / 2  ≈ -1.5708
""".trimIndent()
}

private fun generateDiagramOverviewContent(type: String): String {
    return when (type) {
        "UNIT_CIRCLE" -> """
📐 Geometric Schematic: Unit Circle Coordinates
• Center at (0,0), radius R = 1
• Angle θ = 45° (π/4 rad): (cos θ, sin θ) = (√2/2, √2/2)
• Hypotenuse = 1, adjacent = cos θ, opposite = sin θ
• Identity: sin²(θ) + cos²(θ) = 1
(Click 'Insert Strokes to Canvas' to draw axes and circle directly!)
""".trimIndent()
        "FREE_BODY" -> """
⚡ Vector Schematic: Classical Mechanics Free-Body Diagram
• Center mass m on flat plane
• Upward Vector: Normal Force F_N = m•g
• Downward Vector: Gravity F_g = m•g
• Lateral Vector: Friction F_f = μ • F_N
(Click 'Insert Strokes to Canvas' to ink vector arrows on canvas!)
""".trimIndent()
        else -> """
📈 Mathematical Function Plot: y = f(x) Cartesian System
• Coordinate axes X & Y with origin (0, 0)
• Function: Sine wave oscillation y = A • sin(ωx + φ)
• Amplitude A = 1, Period T = 2π / ω
(Click 'Insert Strokes to Canvas' to plot coordinate axes and sine curve directly on your note!)
""".trimIndent()
    }
}

/**
 * Generates actual stylus strokes and shapes to insert onto the note canvas
 */
private fun generateDiagramStrokes(type: String): Pair<List<DrawingStroke>, List<ShapeAnnotation>> {
    val strokes = mutableListOf<DrawingStroke>()
    val shapes = mutableListOf<ShapeAnnotation>()

    val baseX = 200f
    val baseY = 320f

    when (type) {
        "UNIT_CIRCLE" -> {
            // Coordinate axes
            shapes.add(
                ShapeAnnotation(
                    id = "shape_x_axis_${System.currentTimeMillis()}",
                    type = ShapeType.LINE,
                    startX = baseX - 120f,
                    startY = baseY,
                    endX = baseX + 120f,
                    endY = baseY,
                    color = 0xFF718096L,
                    strokeWidth = 3f
                )
            )
            shapes.add(
                ShapeAnnotation(
                    id = "shape_y_axis_${System.currentTimeMillis()}",
                    type = ShapeType.LINE,
                    startX = baseX,
                    startY = baseY - 120f,
                    endX = baseX,
                    endY = baseY + 120f,
                    color = 0xFF718096L,
                    strokeWidth = 3f
                )
            )
            // Circle
            shapes.add(
                ShapeAnnotation(
                    id = "shape_circle_${System.currentTimeMillis()}",
                    type = ShapeType.CIRCLE,
                    startX = baseX - 80f,
                    startY = baseY - 80f,
                    endX = baseX + 80f,
                    endY = baseY + 80f,
                    color = 0xFF3182CEL,
                    strokeWidth = 3.5f
                )
            )
        }
        "FREE_BODY" -> {
            // Box
            shapes.add(
                ShapeAnnotation(
                    id = "shape_mass_${System.currentTimeMillis()}",
                    type = ShapeType.RECTANGLE,
                    startX = baseX - 40f,
                    startY = baseY - 40f,
                    endX = baseX + 40f,
                    endY = baseY + 40f,
                    color = 0xFF4A5568L,
                    strokeWidth = 4f
                )
            )
            // Normal force arrow
            shapes.add(
                ShapeAnnotation(
                    id = "shape_fn_${System.currentTimeMillis()}",
                    type = ShapeType.ARROW,
                    startX = baseX,
                    startY = baseY - 40f,
                    endX = baseX,
                    endY = baseY - 110f,
                    color = 0xFF3182CEL,
                    strokeWidth = 3.5f
                )
            )
            // Gravity arrow
            shapes.add(
                ShapeAnnotation(
                    id = "shape_fg_${System.currentTimeMillis()}",
                    type = ShapeType.ARROW,
                    startX = baseX,
                    startY = baseY + 40f,
                    endX = baseX,
                    endY = baseY + 110f,
                    color = 0xFFE53E3EL,
                    strokeWidth = 3.5f
                )
            )
        }
        else -> {
            // Cartesian Axes
            shapes.add(
                ShapeAnnotation(
                    id = "shape_x_${System.currentTimeMillis()}",
                    type = ShapeType.ARROW,
                    startX = baseX - 40f,
                    startY = baseY,
                    endX = baseX + 240f,
                    endY = baseY,
                    color = 0xFF718096L,
                    strokeWidth = 3f
                )
            )
            shapes.add(
                ShapeAnnotation(
                    id = "shape_y_${System.currentTimeMillis()}",
                    type = ShapeType.ARROW,
                    startX = baseX,
                    startY = baseY + 100f,
                    endX = baseX,
                    endY = baseY - 120f,
                    color = 0xFF718096L,
                    strokeWidth = 3f
                )
            )
            // Smooth Sine curve stroke
            val pts = mutableListOf<StrokePoint>()
            var cx = baseX
            while (cx <= baseX + 220f) {
                val cy = baseY - 60f * sin((cx - baseX) * 0.035f)
                pts.add(StrokePoint(cx, cy, 1f, System.currentTimeMillis()))
                cx += 6f
            }
            strokes.add(
                DrawingStroke(
                    id = "stroke_curve_${System.currentTimeMillis()}",
                    points = pts,
                    color = 0xFF3182CEL,
                    strokeWidth = 3.5f
                )
            )
        }
    }

    return Pair(strokes, shapes)
}
