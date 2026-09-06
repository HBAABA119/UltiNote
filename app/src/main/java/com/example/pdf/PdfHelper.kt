package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.data.local.SerializationHelpers
import com.example.data.model.DrawingStroke
import com.example.data.model.PageEntity
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.TextAnnotation
import com.example.data.model.ToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    // Generate a bundled sample math textbook PDF for immediate annotation
    fun getOrCreateSampleMathPdf(context: Context): File {
        val sampleFile = File(context.getExternalFilesDir(null) ?: context.filesDir, "Calculus_Linear_Algebra_Reference.pdf")
        if (sampleFile.exists() && sampleFile.length() > 0) {
            return sampleFile
        }

        try {
            val document = PdfDocument()

            // Page 1: Calculus Chapter
            val pageInfo1 = PdfDocument.PageInfo.Builder(792, 1120, 1).create()
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas

            // Draw header background
            val headerPaint = Paint().apply {
                color = Color.parseColor("#EBF3EF")
                style = Paint.Style.FILL
            }
            canvas1.drawRect(0f, 0f, 792f, 160f, headerPaint)

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1B382B")
                textSize = 28f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas1.drawText("CHAPTER 4: TECHNIQUES OF INTEGRATION", 50f, 70f, titlePaint)

            val subPaint = Paint().apply {
                color = Color.parseColor("#4A6B56")
                textSize = 18f
                isAntiAlias = true
            }
            canvas1.drawText("Department of Mathematics • Advanced Calculus Series", 50f, 105f, subPaint)

            val textPaint = Paint().apply {
                color = Color.parseColor("#2D3748")
                textSize = 16f
                isAntiAlias = true
            }

            val bodyLines = listOf(
                "4.1 Integration by Parts (Product Rule in Reverse)",
                "For any differentiable functions u(x) and v(x), the integration by parts formula states:",
                "",
                "           \u222B u dv = u \u2022 v - \u222B v du",
                "",
                "Strategy: Choose u according to the LIATE priority ranking:",
                "  1. L - Logarithmic functions (ln x, log\u2082 x)",
                "  2. I - Inverse trigonometric functions (arctan x, arcsin x)",
                "  3. A - Algebraic expressions (x\u00B2, 3x, polynomials)",
                "  4. T - Trigonometric functions (sin x, cos x)",
                "  5. E - Exponential functions (e\u02E3, 2\u02E3)",
                "",
                "Example 4.1.2: Evaluate \u222B x \u2022 e^(2x) dx",
                "  Let u = x         =>  du = dx",
                "  Let dv = e^(2x)dx =>   v = (1/2) e^(2x)",
                "  Applying formula: \u222B x e^(2x) dx = (x/2) e^(2x) - \u222B (1/2) e^(2x) dx",
                "                                  = (x/2) e^(2x) - (1/4) e^(2x) + C",
                "",
                "Practice Problem 1: Find \u222B x\u00B2 \u2022 ln(x) dx using the LIATE rule.",
                "Practice Problem 2: Evaluate the definite integral from 0 to \u03C0 of x \u2022 sin(x) dx.",
                "",
                "-----------------------------------------------------------------------------------------",
                "4.2 Trigonometric Substitution",
                "For integrals containing \u221A(a\u00B2 - x\u00B2), substitute x = a \u2022 sin \u03B8, where dx = a \u2022 cos \u03B8 d\u03B8."
            )

            var curY = 210f
            for (line in bodyLines) {
                if (line.startsWith("4.1") || line.startsWith("4.2") || line.startsWith("Practice")) {
                    textPaint.isFakeBoldText = true
                    textPaint.color = Color.parseColor("#1A202C")
                } else if (line.contains("\u222B") || line.contains("=>")) {
                    textPaint.isFakeBoldText = true
                    textPaint.color = Color.parseColor("#2B6CB0")
                } else {
                    textPaint.isFakeBoldText = false
                    textPaint.color = Color.parseColor("#374151")
                }
                canvas1.drawText(line, 50f, curY, textPaint)
                curY += 28f
            }

            document.finishPage(page1)

            // Page 2: Linear Algebra Chapter
            val pageInfo2 = PdfDocument.PageInfo.Builder(792, 1120, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            canvas2.drawRect(0f, 0f, 792f, 160f, headerPaint)
            canvas2.drawText("CHAPTER 7: EIGENVALUES & MATRIX DIAGONALIZATION", 50f, 70f, titlePaint)
            canvas2.drawText("Linear Systems & Vector Spaces • Reference Manual", 50f, 105f, subPaint)

            val linearLines = listOf(
                "7.1 The Characteristic Equation",
                "Let A be an n \u00D7 n square matrix. A scalar \u03BB is called an eigenvalue of A if there exists",
                "a non-zero vector v such that:",
                "",
                "           A \u2022 v = \u03BB \u2022 v    or    (A - \u03BB \u2022 I) \u2022 v = 0",
                "",
                "To ensure non-trivial solutions, the characteristic polynomial must vanish:",
                "",
                "           det(A - \u03BB \u2022 I) = 0",
                "",
                "Theorem 7.3: Diagonalization Condition",
                "An n \u00D7 n matrix A is diagonalizable if and only if A has n linearly independent eigenvectors.",
                "In that case, A = P \u2022 D \u2022 P\u207B\u00B9 where D is diagonal containing the eigenvalues.",
                "",
                "Application in Quantum Mechanics & Machine Learning:",
                "• Principal Component Analysis (PCA) computes eigenvectors of covariance matrices.",
                "• Markov chain steady-state distributions correspond to eigenvalue \u03BB = 1."
            )

            var curY2 = 210f
            for (line in linearLines) {
                if (line.startsWith("7.1") || line.startsWith("Theorem") || line.startsWith("Application")) {
                    textPaint.isFakeBoldText = true
                    textPaint.color = Color.parseColor("#1A202C")
                } else if (line.contains("det(") || line.contains("A \u2022 v")) {
                    textPaint.isFakeBoldText = true
                    textPaint.color = Color.parseColor("#2B6CB0")
                } else {
                    textPaint.isFakeBoldText = false
                    textPaint.color = Color.parseColor("#374151")
                }
                canvas2.drawText(line, 50f, curY2, textPaint)
                curY2 += 28f
            }

            document.finishPage(page2)

            val outputStream = FileOutputStream(sampleFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sampleFile
    }

    // Render a page of a PDF file to a Bitmap
    suspend fun renderPdfPage(context: Context, pdfFile: File, pageIndex: Int, targetWidth: Int = 1200): Bitmap? = withContext(Dispatchers.IO) {
        if (!pdfFile.exists() || pdfFile.length() == 0L) return@withContext null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            val page = renderer.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            canvasFillWhite(bitmap)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            return@withContext bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun canvasFillWhite(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
    }

    // Dynamic pressure stroke width scaling helper
    fun calculatePressureWidth(baseWidth: Float, pressure: Float, toolType: ToolType): Float {
        return when (toolType) {
            ToolType.PEN_FOUNTAIN -> {
                baseWidth * (0.35f + pressure * 1.5f).coerceIn(baseWidth * 0.3f, baseWidth * 2.2f)
            }
            ToolType.PEN_BRUSH -> {
                baseWidth * (0.25f + pressure * 2.25f).coerceIn(baseWidth * 0.25f, baseWidth * 2.8f)
            }
            ToolType.PEN_BALLPOINT -> {
                baseWidth * (0.75f + pressure * 0.5f).coerceIn(baseWidth * 0.6f, baseWidth * 1.4f)
            }
            ToolType.HIGHLIGHTER -> baseWidth
            else -> baseWidth * (0.7f + pressure * 0.6f)
        }
    }

    // Export note pages into a single high-quality PDF document, combining imported PDF base with canvas annotations
    suspend fun exportNoteToPdf(
        context: Context,
        noteTitle: String,
        pages: List<PageEntity>,
        pdfFilePath: String? = null,
        pageWidth: Int = 1200,
        pageHeight: Int = 1696
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "KomorebiExports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val cleanTitle = noteTitle.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val outputFile = File(exportDir, "${cleanTitle}_HQ_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()

        // Prepare PDF renderer if an imported PDF base is present
        var importedPfd: ParcelFileDescriptor? = null
        var importedRenderer: PdfRenderer? = null
        if (pdfFilePath != null) {
            val baseFile = File(pdfFilePath)
            if (baseFile.exists() && baseFile.length() > 0) {
                try {
                    importedPfd = ParcelFileDescriptor.open(baseFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    importedRenderer = PdfRenderer(importedPfd)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        try {
            for ((index, pageEntity) in pages.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val pdfPage = document.startPage(pageInfo)
                val canvas = pdfPage.canvas

                var pdfBaseDrawn = false

                // 1. Draw Imported PDF Base Page if available
                if (importedRenderer != null) {
                    val targetPdfPageIndex = pageEntity.pdfPageIndex.coerceIn(0, (importedRenderer.pageCount - 1).coerceAtLeast(0))
                    if (targetPdfPageIndex < importedRenderer.pageCount) {
                        try {
                            val rPage = importedRenderer.openPage(targetPdfPageIndex)
                            val baseBitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
                            canvasFillWhite(baseBitmap)
                            rPage.render(baseBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            rPage.close()

                            canvas.drawBitmap(baseBitmap, 0f, 0f, null)
                            baseBitmap.recycle()
                            pdfBaseDrawn = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // If no PDF base, render warm parchment paper background and custom template grid/lines
                if (!pdfBaseDrawn) {
                    val bgPaint = Paint().apply {
                        color = Color.parseColor("#FDFBF7")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                    drawTemplateOnCanvas(canvas, pageEntity.template, pageWidth.toFloat(), pageHeight.toFloat())
                }

                // 2. Draw Text Annotations
                val textBlocks = SerializationHelpers.jsonToTextBlocks(pageEntity.textBlocksJson)
                for (tb in textBlocks) {
                    val tPaint = Paint().apply {
                        color = (tb.color and 0xFFFFFFFFL).toInt()
                        textSize = tb.fontSize * 1.35f
                        isFakeBoldText = tb.isBold
                        isAntiAlias = true
                    }
                    val lines = tb.text.split("\n")
                    var yOff = tb.y
                    for (line in lines) {
                        canvas.drawText(line, tb.x, yOff, tPaint)
                        yOff += tb.fontSize * 1.4f
                    }
                }

                // 3. Draw Shapes
                val shapes = SerializationHelpers.jsonToShapes(pageEntity.shapesJson)
                for (shape in shapes) {
                    val sPaint = Paint().apply {
                        color = (shape.color and 0xFFFFFFFFL).toInt()
                        strokeWidth = shape.strokeWidth * 1.25f
                        style = if (shape.isFilled) Paint.Style.FILL else Paint.Style.STROKE
                        isAntiAlias = true
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    when (shape.type) {
                        ShapeType.LINE -> canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, sPaint)
                        ShapeType.RECTANGLE -> {
                            val left = minOf(shape.startX, shape.endX)
                            val top = minOf(shape.startY, shape.endY)
                            val right = maxOf(shape.startX, shape.endX)
                            val bottom = maxOf(shape.startY, shape.endY)
                            canvas.drawRect(left, top, right, bottom, sPaint)
                        }
                        ShapeType.CIRCLE -> {
                            val radius = Math.hypot(
                                (shape.endX - shape.startX).toDouble(),
                                (shape.endY - shape.startY).toDouble()
                            ).toFloat() / 2f
                            val cx = (shape.startX + shape.endX) / 2f
                            val cy = (shape.startY + shape.endY) / 2f
                            canvas.drawCircle(cx, cy, radius, sPaint)
                        }
                        ShapeType.TRIANGLE -> {
                            val path = android.graphics.Path().apply {
                                val apexX = (shape.startX + shape.endX) / 2f
                                moveTo(apexX, shape.startY)
                                lineTo(shape.endX, shape.endY)
                                lineTo(shape.startX, shape.endY)
                                close()
                            }
                            canvas.drawPath(path, sPaint)
                        }
                        ShapeType.ARROW -> {
                            canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, sPaint)
                            val angle = Math.atan2(
                                (shape.endY - shape.startY).toDouble(),
                                (shape.endX - shape.startX).toDouble()
                            )
                            val arrowHeadLen = 28f
                            val arrowAngle = Math.PI / 6.0
                            val x1 = (shape.endX - arrowHeadLen * Math.cos(angle - arrowAngle)).toFloat()
                            val y1 = (shape.endY - arrowHeadLen * Math.sin(angle - arrowAngle)).toFloat()
                            val x2 = (shape.endX - arrowHeadLen * Math.cos(angle + arrowAngle)).toFloat()
                            val y2 = (shape.endY - arrowHeadLen * Math.sin(angle + arrowAngle)).toFloat()
                            canvas.drawLine(shape.endX, shape.endY, x1, y1, sPaint)
                            canvas.drawLine(shape.endX, shape.endY, x2, y2, sPaint)
                        }
                        else -> canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, sPaint)
                    }
                }

                // 4. Draw Drawing Strokes with Dynamic Pressure Width
                val strokes = SerializationHelpers.jsonToStrokes(pageEntity.strokesJson)
                for (stroke in strokes) {
                    if (stroke.points.size < 2) continue
                    val strokePaint = Paint().apply {
                        color = (stroke.color and 0xFFFFFFFFL).toInt()
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                        if (stroke.isHighlighter) {
                            alpha = (stroke.alpha * 255).toInt().coerceIn(30, 200)
                        }
                    }

                    for (i in 0 until stroke.points.size - 1) {
                        val p1 = stroke.points[i]
                        val p2 = stroke.points[i + 1]
                        val avgPressure = (p1.pressure + p2.pressure) / 2f
                        strokePaint.strokeWidth = calculatePressureWidth(stroke.strokeWidth, avgPressure, stroke.toolType)
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)
                    }
                }

                document.finishPage(pdfPage)
            }
        } finally {
            try {
                importedRenderer?.close()
                importedPfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fos = FileOutputStream(outputFile)
        document.writeTo(fos)
        fos.flush()
        fos.close()
        document.close()

        outputFile
    }

    /**
     * Share exported PDF document via Android System Share Sheet
     */
    fun sharePdfFile(context: Context, file: File, noteTitle: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, noteTitle)
                putExtra(android.content.Intent.EXTRA_TEXT, "Exported PDF notes: $noteTitle")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(shareIntent, "Share High-Quality PDF").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Render a fast, crisp thumbnail for a page of an imported PDF
     */
    suspend fun renderPdfPageThumbnail(context: Context, pdfFile: File, pageIndex: Int, targetWidth: Int = 300): Bitmap? = withContext(Dispatchers.IO) {
        if (!pdfFile.exists() || pdfFile.length() == 0L) return@withContext null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            val page = renderer.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(100)

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            canvasFillWhite(bitmap)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            return@withContext bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun drawTemplateOnCanvas(canvas: Canvas, template: com.example.data.model.PaperTemplate, w: Float, h: Float) {
        val linePaint = Paint().apply {
            color = Color.parseColor("#D5DDD6")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val marginPaint = Paint().apply {
            color = Color.parseColor("#EBB3BA")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        when (template) {
            com.example.data.model.PaperTemplate.RULED -> {
                canvas.drawLine(70f, 0f, 70f, h, marginPaint)
                var y = 80f
                while (y < h - 40f) {
                    canvas.drawLine(0f, y, w, y, linePaint)
                    y += 32f
                }
            }
            com.example.data.model.PaperTemplate.GRID -> {
                var x = 0f
                while (x < w) {
                    canvas.drawLine(x, 0f, x, h, linePaint)
                    x += 24f
                }
                var y = 0f
                while (y < h) {
                    canvas.drawLine(0f, y, w, y, linePaint)
                    y += 24f
                }
            }
            com.example.data.model.PaperTemplate.CORNELL -> {
                canvas.drawLine(180f, 0f, 180f, h - 140f, marginPaint)
                canvas.drawLine(0f, h - 140f, w, h - 140f, marginPaint)
                var y = 80f
                while (y < h - 150f) {
                    canvas.drawLine(180f, y, w, y, linePaint)
                    y += 32f
                }
            }
            else -> {}
        }
    }

    suspend fun getPdfPageCount(context: Context, pdfFile: File): Int = withContext(Dispatchers.IO) {
        if (!pdfFile.exists() || pdfFile.length() == 0L) return@withContext 1
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            return@withContext renderer.pageCount
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 1
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun importPdfFromUri(context: Context, uri: Uri, targetFileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val docsDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "UltiNoteDocs")
            if (!docsDir.exists()) docsDir.mkdirs()

            val cleanName = targetFileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(docsDir, "${System.currentTimeMillis()}_$cleanName")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0) {
                return@withContext targetFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
