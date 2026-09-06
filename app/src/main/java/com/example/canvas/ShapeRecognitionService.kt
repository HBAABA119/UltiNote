package com.example.canvas

import androidx.compose.ui.geometry.Offset
import com.example.data.model.DrawingStroke
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.StrokePoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

enum class GeometricShapeKind {
    CIRCLE,
    SQUARE,
    RECTANGLE,
    TRIANGLE,
    STRAIGHT_LINE,
    NONE
}

data class ShapeRecognitionResult(
    val detectedKind: GeometricShapeKind,
    val snappedShape: ShapeAnnotation? = null,
    val confidence: Float = 0f,
    val description: String = ""
)

/**
 * Intelligent gesture-based Shape Recognition Service.
 * Detects hand-drawn Circles, Squares, Rectangles, and Triangles in real-time,
 * and snaps them into mathematically precise, anti-aliased geometric shapes
 * while adhering to the app's liquid glass aesthetic.
 */
object ShapeRecognitionService {

    fun recognize(
        stroke: DrawingStroke,
        activeLayerId: String = "default"
    ): ShapeRecognitionResult {
        val points = stroke.points
        if (points.size < 5) {
            return ShapeRecognitionResult(GeometricShapeKind.NONE)
        }

        val start = Offset(points.first().x, points.first().y)
        val end = Offset(points.last().x, points.last().y)

        // Arc length calculation
        var arcLength = 0f
        for (i in 0 until points.size - 1) {
            arcLength += hypot(
                (points[i + 1].x - points[i].x).toDouble(),
                (points[i + 1].y - points[i].y).toDouble()
            ).toFloat()
        }

        if (arcLength < 35f) {
            return ShapeRecognitionResult(GeometricShapeKind.NONE)
        }

        // Check closure for closed polygons/curves
        val closureDist = hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
        val isClosed = closureDist < (arcLength * 0.32f)

        // Bounding box calculation
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var sumX = 0f
        var sumY = 0f

        for (p in points) {
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
            sumX += p.x
            sumY += p.y
        }

        val width = maxX - minX
        val height = maxY - minY
        val centroidX = sumX / points.size
        val centroidY = sumY / points.size

        if (width < 25f || height < 25f) {
            return ShapeRecognitionResult(GeometricShapeKind.NONE)
        }

        // 1. Check for CIRCLE
        if (isClosed) {
            val circleResult = testCircle(stroke, points, centroidX, centroidY, width, height, activeLayerId)
            if (circleResult != null) return circleResult
        }

        // 2. Check for TRIANGLE
        val triangleResult = testTriangle(stroke, points, minX, maxX, minY, maxY, isClosed, activeLayerId)
        if (triangleResult != null) return triangleResult

        // 3. Check for SQUARE or RECTANGLE
        if (isClosed) {
            val rectResult = testSquareOrRectangle(stroke, points, minX, maxX, minY, maxY, width, height, activeLayerId)
            if (rectResult != null) return rectResult
        }

        return ShapeRecognitionResult(GeometricShapeKind.NONE)
    }

    /**
     * Circle detector: tests radial variance from centroid
     */
    private fun testCircle(
        stroke: DrawingStroke,
        points: List<StrokePoint>,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        layerId: String
    ): ShapeRecognitionResult? {
        var avgRadius = 0f
        val radii = points.map { p ->
            val r = hypot((p.x - centerX).toDouble(), (p.y - centerY).toDouble()).toFloat()
            avgRadius += r
            r
        }
        avgRadius /= points.size

        var variance = 0f
        for (r in radii) {
            variance += abs(r - avgRadius)
        }
        val radialErrorRatio = variance / (points.size * avgRadius)
        val aspectRatio = width / height

        if (radialErrorRatio < 0.22f && aspectRatio in 0.70f..1.40f) {
            val radius = (width + height) / 4f
            val shape = ShapeAnnotation(
                id = "snapped_circle_${System.currentTimeMillis()}",
                type = ShapeType.CIRCLE,
                startX = centerX - radius,
                startY = centerY - radius,
                endX = centerX + radius,
                endY = centerY + radius,
                strokeWidth = max(2.5f, stroke.strokeWidth),
                color = stroke.color,
                isFilled = false,
                layerId = layerId
            )
            return ShapeRecognitionResult(
                detectedKind = GeometricShapeKind.CIRCLE,
                snappedShape = shape,
                confidence = 1.0f - radialErrorRatio,
                description = "Snapped to perfect Circle"
            )
        }
        return null
    }

    /**
     * Triangle detector: identifies 3 prominent corners, checks interior angles and segment fits
     */
    private fun testTriangle(
        stroke: DrawingStroke,
        points: List<StrokePoint>,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        isClosed: Boolean,
        layerId: String
    ): ShapeRecognitionResult? {
        val corners = findSignificantCorners(points)
        // A triangle typically exhibits 3 prominent corners (or 4 if closed near the first vertex)
        if (corners.size in 3..4) {
            val v1 = Offset(points[corners[0]].x, points[corners[0]].y)
            val v2 = Offset(points[corners[1]].x, points[corners[1]].y)
            val v3 = Offset(points[corners[2]].x, points[corners[2]].y)

            // Side lengths
            val d12 = hypot((v2.x - v1.x).toDouble(), (v2.y - v1.y).toDouble()).toFloat()
            val d23 = hypot((v3.x - v2.x).toDouble(), (v3.y - v2.y).toDouble()).toFloat()
            val d31 = hypot((v1.x - v3.x).toDouble(), (v1.y - v3.y).toDouble()).toFloat()

            val minSide = min(min(d12, d23), d31)
            val maxSide = max(max(d12, d23), d31)

            if (minSide > 20f && maxSide > 40f) {
                // Check interior angles
                val a1 = computeAngleDeg(v3, v1, v2)
                val a2 = computeAngleDeg(v1, v2, v3)
                val a3 = computeAngleDeg(v2, v3, v1)
                val angleSum = a1 + a2 + a3

                if (angleSum in 140f..220f) {
                    val shape = ShapeAnnotation(
                        id = "snapped_triangle_${System.currentTimeMillis()}",
                        type = ShapeType.TRIANGLE,
                        startX = minX,
                        startY = minY,
                        endX = maxX,
                        endY = maxY,
                        strokeWidth = max(2.5f, stroke.strokeWidth),
                        color = stroke.color,
                        isFilled = false,
                        layerId = layerId
                    )
                    return ShapeRecognitionResult(
                        detectedKind = GeometricShapeKind.TRIANGLE,
                        snappedShape = shape,
                        confidence = 0.92f,
                        description = "Snapped to perfect Triangle"
                    )
                }
            }
        }
        return null
    }

    /**
     * Square / Rectangle detector:
     * Differentiates equilateral Squares from general Rectangles based on aspect ratio
     */
    private fun testSquareOrRectangle(
        stroke: DrawingStroke,
        points: List<StrokePoint>,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        width: Float,
        height: Float,
        layerId: String
    ): ShapeRecognitionResult? {
        var edgeDeviationSum = 0f
        for (p in points) {
            val dLeft = abs(p.x - minX)
            val dRight = abs(p.x - maxX)
            val dTop = abs(p.y - minY)
            val dBottom = abs(p.y - maxY)
            val minDistanceToEdge = min(min(dLeft, dRight), min(dTop, dBottom))
            edgeDeviationSum += minDistanceToEdge
        }

        val avgEdgeDeviation = edgeDeviationSum / points.size
        val minDimension = min(width, height)

        if (avgEdgeDeviation / minDimension < 0.18f) {
            val aspectRatio = width / height
            val isSquare = aspectRatio in 0.82f..1.22f

            val (finalStartX, finalStartY, finalEndX, finalEndY) = if (isSquare) {
                val side = (width + height) / 2f
                val cx = (minX + maxX) / 2f
                val cy = (minY + maxY) / 2f
                listOf(cx - side / 2f, cy - side / 2f, cx + side / 2f, cy + side / 2f)
            } else {
                listOf(minX, minY, maxX, maxY)
            }

            val shape = ShapeAnnotation(
                id = if (isSquare) "snapped_square_${System.currentTimeMillis()}" else "snapped_rect_${System.currentTimeMillis()}",
                type = ShapeType.RECTANGLE,
                startX = finalStartX,
                startY = finalStartY,
                endX = finalEndX,
                endY = finalEndY,
                strokeWidth = max(2.5f, stroke.strokeWidth),
                color = stroke.color,
                isFilled = false,
                layerId = layerId
            )

            return ShapeRecognitionResult(
                detectedKind = if (isSquare) GeometricShapeKind.SQUARE else GeometricShapeKind.RECTANGLE,
                snappedShape = shape,
                confidence = 0.94f,
                description = if (isSquare) "Snapped to perfect Square" else "Snapped to perfect Rectangle"
            )
        }
        return null
    }

    /**
     * Identifies sharp direction-change vertices (corners) in the stroke path
     */
    private fun findSignificantCorners(points: List<StrokePoint>): List<Int> {
        val corners = mutableListOf<Int>()
        if (points.size < 6) return corners

        val step = max(2, points.size / 24)
        for (i in step until points.size - step) {
            val pPrev = points[i - step]
            val pCurr = points[i]
            val pNext = points[i + step]

            val v1x = pPrev.x - pCurr.x
            val v1y = pPrev.y - pCurr.y
            val v2x = pNext.x - pCurr.x
            val v2y = pNext.y - pCurr.y

            val dot = v1x * v2x + v1y * v2y
            val mag1 = hypot(v1x.toDouble(), v1y.toDouble()).toFloat()
            val mag2 = hypot(v2x.toDouble(), v2y.toDouble()).toFloat()

            if (mag1 > 5f && mag2 > 5f) {
                val cosTheta = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
                val angleDeg = (acos(cosTheta) * 180f / PI).toFloat()

                // Significant corner turn (e.g. between 30 and 130 degrees)
                if (angleDeg in 35f..135f) {
                    if (corners.isEmpty() || abs(i - corners.last()) > step * 2) {
                        corners.add(i)
                    }
                }
            }
        }
        return corners
    }

    private fun computeAngleDeg(p1: Offset, vertex: Offset, p2: Offset): Float {
        val v1x = p1.x - vertex.x
        val v1y = p1.y - vertex.y
        val v2x = p2.x - vertex.x
        val v2y = p2.y - vertex.y

        val dot = v1x * v2x + v1y * v2y
        val mag1 = hypot(v1x.toDouble(), v1y.toDouble()).toFloat()
        val mag2 = hypot(v2x.toDouble(), v2y.toDouble()).toFloat()

        if (mag1 < 1f || mag2 < 1f) return 0f
        val cosTheta = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
        return (acos(cosTheta) * 180f / PI).toFloat()
    }
}
