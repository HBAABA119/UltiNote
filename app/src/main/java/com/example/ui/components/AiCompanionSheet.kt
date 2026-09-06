package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteEntity
import com.example.ui.theme.LocalKomorebiPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AiStudyMode(val title: String, val promptTemplate: String) {
    SUMMARY("Chapter Summary", "Summarize core concepts, theorems, and definitions with intuitive explanations"),
    PRACTICE_QUESTIONS("Practice Questions", "Generate 3 high-yield exam practice problems with full step-by-step calculus solutions"),
    DIAGRAM_FORMULAS("Formulas & Cheatsheet", "Synthesize essential formulas, integrals, and coordinate geometry reference box"),
    FLASHCARDS("Concept Flashcards", "Create question-and-answer revision cards for rapid recall")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiStudyCompanionSheet(
    sheetState: SheetState,
    availableNotes: List<NoteEntity>,
    currentNoteTitle: String,
    onDismiss: () -> Unit,
    onInsertIntoNote: (String) -> Unit,
    onCreateNewStudyNote: (String, String) -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf(AiStudyMode.PRACTICE_QUESTIONS) }
    var userPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<String?>(null) }

    // Referenced notes selection in folder
    val selectedDocTitles = remember {
        mutableStateListOf<String>().apply {
            add(currentNoteTitle)
            val other = availableNotes.firstOrNull { it.title != currentNoteTitle }
            if (other != null) add(other.title)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.toolbarBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(palette.colorScheme.primary, palette.colorScheme.tertiary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Study Companion",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Komorebi Study Companion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )
                    Text(
                        text = "Local-first smart note generator & exam prep",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sheet",
                        tint = palette.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Source Material Selection Box
            Text(
                text = "REFERENCED BOOKS & NOTES IN FOLDER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (note in availableNotes) {
                    val isSelected = selectedDocTitles.contains(note.title)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedDocTitles.remove(note.title)
                            else selectedDocTitles.add(note.title)
                        },
                        label = {
                            Text(
                                text = note.title,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (note.isPdf) Icons.Default.Description else Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.colorScheme.primaryContainer,
                            selectedLabelColor = palette.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Study Mode Selector
            Text(
                text = "ACTION MODE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiStudyMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.colorScheme.primary,
                            selectedLabelColor = palette.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Prompt / Lesson Specification
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Topic or specific question (e.g., 'Integration by Parts practice')") },
                placeholder = { Text("e.g. Generate 3 problems from Chapter 4 with diagram sketches") },
                shape = RoundedCornerShape(14.dp),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Generate Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isGenerating = true
                        delay(600) // Smooth processing feeling
                        generatedResult = generateCompanionContent(
                            mode = selectedMode,
                            topic = userPrompt.ifBlank { "Calculus II Integration & Series" },
                            referencedDocs = selectedDocTitles.toList()
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Synthesizing Study Notes...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate for this Lesson")
                }
            }

            // Results Display Card
            AnimatedVisibility(visible = generatedResult != null) {
                val resultText = generatedResult ?: ""
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.canvasBackground),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = palette.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generated Study Companion Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = palette.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Insert or Save
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onInsertIntoNote(resultText)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = palette.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Insert to Note", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    onCreateNewStudyNote(
                                        "${selectedMode.title} - ${userPrompt.ifBlank { "Study Guide" }}",
                                        resultText
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Notebook", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Local mock companion generation engine (ready to connect with Gemini API or local inference)
private fun generateCompanionContent(
    mode: AiStudyMode,
    topic: String,
    referencedDocs: List<String>
): String {
    val sources = if (referencedDocs.isEmpty()) "Active Lesson" else referencedDocs.joinToString(", ")
    return when (mode) {
        AiStudyMode.PRACTICE_QUESTIONS -> """
📚 Sources: $sources
Topic: $topic

Problem 1: Evaluate the indefinite integral:
    \u222B x\u00B2 • e^(3x) dx
• Step 1: Let u = x\u00B2 => du = 2x dx; dv = e^(3x) dx => v = (1/3) e^(3x)
• Step 2: Apply formula: (1/3) x\u00B2 e^(3x) - (2/3) \u222B x e^(3x) dx
• Step 3: Repeat integration by parts on \u222B x e^(3x) dx
• Final Answer: (e^(3x) / 27) [ 9x\u00B2 - 6x + 2 ] + C

Problem 2: Determine if the alternating series converges:
    \u2211_{n=1}^\u221E (-1)^(n+1) • [ n / (2n\u00B2 + 1) ]
• Check: lim_{n->\u221E} a_n = 0 and a_{n+1} \u2264 a_n
• Conclusion: Converges conditionally by Leibniz Alternating Series Test!
""".trimIndent()

        AiStudyMode.SUMMARY -> """
📚 Sources: $sources
Key Takeaways & Conceptual Architecture:

1. Integration by Parts Strategy:
   Remember the LIATE mnemonic (Logarithmic, Inverse Trig, Algebraic, Trig, Exponential).
   Always differentiate the term that simplifies faster.

2. Divergence vs Convergence Criteria:
   • p-Series: \u2211 (1 / n^p) converges if and only if p > 1.
   • Ratio Test: If L < 1 absolutely convergent; if L > 1 diverges.
   • Integral Test: Function must be continuous, positive, and decreasing on [1, \u221E).

💡 Study Tip: Sketch the curve f(x) before performing trigonometric substitution to verify the right triangle legs!
""".trimIndent()

        AiStudyMode.DIAGRAM_FORMULAS -> """
📐 Formula Quick Reference Sheet:
• \u222B u dv = u•v - \u222B v du
• \u222B tan(x) dx = ln|sec(x)| + C
• \u222B sec(x) dx = ln|sec(x) + tan(x)| + C
• Taylor Series: f(x) = \u2211_{n=0}^\u221E [f^(n)(a) / n!] • (x - a)^n
• Maclaurin Series for e^x = 1 + x + x\u00B2/2! + x\u00B3/3! + ...
• Matrix Eigenvalue Condition: det(A - \u03BB I) = 0
""".trimIndent()

        AiStudyMode.FLASHCARDS -> """
Card 1: When should you use the Comparison Test?
A: When a series closely resembles a known p-series or geometric series and all terms are strictly positive.

Card 2: What is the radius of convergence for a power series?
A: R = 1 / lim_{n->\u221E} |a_{n+1} / a_n| found via the Ratio Test.

Card 3: What does the Wronskian test determine?
A: Linear independence of solutions to linear differential equations.
""".trimIndent()
    }
}
