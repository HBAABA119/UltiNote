package com.ultinote.app.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.ui.theme.KomorebiAestheticPalette

object PaperBackgroundRenderer {

    fun drawPaperTemplate(
        scope: DrawScope,
        template: PaperTemplate,
        palette: KomorebiAestheticPalette,
        width: Float,
        height: Float
    ) {
        val lineCol = palette.paperLineColor
        val gridCol = palette.paperGridColor
        val marginCol = Color(0x66D4708A) // subtle rose margin line

        when (template) {
            PaperTemplate.BLANK -> {
                // Pure clean paper, no lines
            }

            PaperTemplate.RULED -> {
                // Margin vertical line
                scope.drawLine(
                    color = marginCol,
                    start = Offset(72f, 0f),
                    end = Offset(72f, height),
                    strokeWidth = 2f
                )

                // Ruled lines
                var y = 90f
                while (y < height - 30f) {
                    scope.drawLine(
                        color = lineCol,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.2f
                    )
                    y += 36f
                }
            }

            PaperTemplate.GRID -> {
                val step = 28f
                var x = 0f
                while (x < width) {
                    scope.drawLine(
                        color = gridCol,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = 0f
                while (y < height) {
                    scope.drawLine(
                        color = gridCol,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }

            PaperTemplate.DOTTED -> {
                val step = 28f
                var y = 28f
                while (y < height) {
                    var x = 28f
                    while (x < width) {
                        scope.drawCircle(
                            color = gridCol.copy(alpha = 0.5f),
                            radius = 1.5f,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }

            PaperTemplate.CORNELL -> {
                val cueColumnWidth = minOf(200f, width * 0.28f)
                val summaryHeight = 160f

                // Cue column line
                scope.drawLine(
                    color = palette.colorScheme.primary.copy(alpha = 0.5f),
                    start = Offset(cueColumnWidth, 0f),
                    end = Offset(cueColumnWidth, height - summaryHeight),
                    strokeWidth = 2f
                )

                // Summary separator line
                scope.drawLine(
                    color = palette.colorScheme.primary.copy(alpha = 0.5f),
                    start = Offset(0f, height - summaryHeight),
                    end = Offset(width, height - summaryHeight),
                    strokeWidth = 2f
                )

                // Notes area ruled lines
                var y = 80f
                while (y < height - summaryHeight - 20f) {
                    scope.drawLine(
                        color = lineCol,
                        start = Offset(cueColumnWidth, y),
                        end = Offset(width, y),
                        strokeWidth = 1.2f
                    )
                    y += 34f
                }

                // Header cue line
                scope.drawLine(
                    color = lineCol,
                    start = Offset(0f, 65f),
                    end = Offset(width, 65f),
                    strokeWidth = 1.5f
                )
            }

            PaperTemplate.PLANNER_WEEKLY -> {
                val days = 7
                val colWidth = width / days
                val topBarHeight = 70f
                val habitsBoxHeight = 140f

                // Top bar
                scope.drawLine(
                    color = palette.colorScheme.primary.copy(alpha = 0.4f),
                    start = Offset(0f, topBarHeight),
                    end = Offset(width, topBarHeight),
                    strokeWidth = 2f
                )

                // Bottom habits line
                scope.drawLine(
                    color = palette.colorScheme.primary.copy(alpha = 0.4f),
                    start = Offset(0f, height - habitsBoxHeight),
                    end = Offset(width, height - habitsBoxHeight),
                    strokeWidth = 2f
                )

                // Vertical day dividers
                for (i in 1 until days) {
                    val x = colWidth * i
                    scope.drawLine(
                        color = lineCol,
                        start = Offset(x, 0f),
                        end = Offset(x, height - habitsBoxHeight),
                        strokeWidth = 1.5f
                    )
                }

                // Hourly ticks inside columns
                var y = topBarHeight + 40f
                while (y < height - habitsBoxHeight - 20f) {
                    scope.drawLine(
                        color = lineCol.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += 40f
                }
            }

            PaperTemplate.DARK_PAPER -> {
                // Blackboard subtle grid
                val step = 32f
                var x = 0f
                while (x < width) {
                    scope.drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = 0f
                while (y < height) {
                    scope.drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }

            PaperTemplate.ENGINEERING_GRID -> {
                val fine = 14f
                val major = fine * 5
                var x = 0f
                while (x < width) {
                    val isMajor = (x % major) < 1f
                    scope.drawLine(
                        color = if (isMajor) palette.colorScheme.primary.copy(alpha = 0.35f) else gridCol.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = if (isMajor) 1.5f else 0.8f
                    )
                    x += fine
                }
                var y = 0f
                while (y < height) {
                    val isMajor = (y % major) < 1f
                    scope.drawLine(
                        color = if (isMajor) palette.colorScheme.primary.copy(alpha = 0.35f) else gridCol.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = if (isMajor) 1.5f else 0.8f
                    )
                    y += fine
                }
            }

            PaperTemplate.MUSIC_STAVE -> {
                val staveSpacing = 8f
                var topStave = 80f
                while (topStave < height - 120f) {
                    for (line in 0 until 5) {
                        val ly = topStave + (line * staveSpacing)
                        scope.drawLine(
                            color = lineCol.copy(alpha = 0.6f),
                            start = Offset(40f, ly),
                            end = Offset(width - 40f, ly),
                            strokeWidth = 1.2f
                        )
                    }
                    topStave += 90f
                }
            }

            PaperTemplate.PASTEL_WASH -> {
                // Soft watercolor dot wash
                val step = 36f
                var y = 36f
                while (y < height) {
                    var x = 36f
                    while (x < width) {
                        scope.drawCircle(
                            color = palette.colorScheme.primary.copy(alpha = 0.2f),
                            radius = 2f,
                            center = Offset(x, y)
                        )
                        x += step
                    }
                    y += step
                }
            }
        }
    }
}
