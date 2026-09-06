package com.ultinote.app.canvas

import androidx.compose.ui.geometry.Offset
import com.ultinote.app.data.model.DrawingStroke
import com.ultinote.app.data.model.ShapeAnnotation
import com.ultinote.app.data.model.ShapeType
import com.ultinote.app.data.model.StrokePoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class RecognizedShapeType {
    STRAIGHT_LINE,
    SMOOTH_CURVE,
    CIRCLE_OR_ELLIPSE,
    SQUARE,
    RECTANGLE,
    TRIANGLE,
    NONE
}

data class AutoCorrectionResult(
    val correctedStroke: DrawingStroke? = null,
    val correctedShape: ShapeAnnotation? = null,
    val recognizedType: RecognizedShapeType = RecognizedShapeType.NONE,
    val message: String = ""
)

/**
 * Intelligent Auto-Drawing Shape & Line Correction Engine.
 * Analyzes raw stylus/touch strokes and:
 * 1. Fixes crooked/wobbly lines into pristine straight lines (with axis snapping).
 * 2. Fixes jittery curved lines into silky-smooth geometric bezier/circular curves.
 * 3. Detects closed/near-closed shapes and converts to precise Circles, Ellipses, or Rectangles.
 */
object StrokeAutoCorrection {

    fun analyzeAndCorrectStroke(
        stroke: DrawingStroke,
        enabled: Boolean = true
    ): AutoCorrectionResult {
        if (!enabled || stroke.points.size < 4) {
            return AutoCorrectionResult(correctedStroke = stroke, recognizedType = RecognizedShapeType.NONE)
        }

        val points = stroke.points
        val start = Offset(points.first().x, points.first().y)
        val end = Offset(points.last().x, points.last().y)

        // 1. Calculate Arc Length & Euclidean Distance
        var arcLength = 0f
        for (i in 0 until points.size - 1) {
            arcLength += hypot(
                (points[i + 1].x - points[i].x).toDouble(),
                (points[i + 1].y - points[i].y).toDouble()
            ).toFloat()
        }

        val directDistance = hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()

        if (arcLength < 20f) {
            return AutoCorrectionResult(correctedStroke = stroke, recognizedType = RecognizedShapeType.NONE)
        }

        // 2. Check for Geometric Shapes (Circle, Square, Rectangle, Triangle)
        val recognized = ShapeRecognitionService.recognize(stroke, stroke.layerId)
        if (recognized.snappedShape != null) {
            val recType = when (recognized.detectedKind) {
                GeometricShapeKind.CIRCLE -> RecognizedShapeType.CIRCLE_OR_ELLIPSE
                GeometricShapeKind.SQUARE -> RecognizedShapeType.SQUARE
                GeometricShapeKind.RECTANGLE -> RecognizedShapeType.RECTANGLE
                GeometricShapeKind.TRIANGLE -> RecognizedShapeType.TRIANGLE
                else -> RecognizedShapeType.NONE
            }
            return AutoCorrectionResult(
                correctedShape = recognized.snappedShape,
                recognizedType = recType,
                message = recognized.description
            )
        }

        // 3. Check for Straight Line (Crooked line to straight line correction)
        // Ratio of direct Euclidean distance to cumulative arc length
        val straightnessRatio = directDistance / arcLength
        var maxPerpendicularDeviation = 0f

        for (pt in points) {
            val distToChord = distancePointToSegment(Offset(pt.x, pt.y), start, end)
            if (distToChord > maxPerpendicularDeviation) {
                maxPerpendicularDeviation = distToChord
            }
        }

        val normalizedDeviation = if (directDistance > 0f) maxPerpendicularDeviation / directDistance else 1f

        if (straightnessRatio > 0.88f && normalizedDeviation < 0.12f && directDistance > 35f) {
            // Recognize and straighten crooked line!
            var finalStart = start
            var finalEnd = end

            // Axis snap check: snap to horizontal, vertical, or 45 degrees if close within 6 degrees
            val dx = end.x - start.x
            val dy = end.y - start.y
            val angleDeg = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()
            val snapTolerance = 6.5f

            if (abs(angleDeg) < snapTolerance || abs(abs(angleDeg) - 180f) < snapTolerance) {
                // Snap to horizontal
                val avgY = (start.y + end.y) / 2f
                finalStart = Offset(start.x, avgY)
                finalEnd = Offset(end.x, avgY)
            } else if (abs(abs(angleDeg) - 90f) < snapTolerance) {
                // Snap to vertical
                val avgX = (start.x + end.x) / 2f
                finalStart = Offset(avgX, start.y)
                finalEnd = Offset(avgX, end.y)
            }

            // Generate clean linear points
            val numSubdivisions = (directDistance / 12f).roundToInt().coerceIn(6, 40)
            val straightenedPoints = mutableListOf<StrokePoint>()
            for (i in 0..numSubdivisions) {
                val t = i.toFloat() / numSubdivisions
                val px = finalStart.x + t * (finalEnd.x - finalStart.x)
                val py = finalStart.y + t * (finalEnd.y - finalStart.y)
                straightenedPoints.add(StrokePoint(px, py, 1.0f, System.currentTimeMillis()))
            }

            val corrected = stroke.copy(points = straightenedPoints)
            return AutoCorrectionResult(
                correctedStroke = corrected,
                recognizedType = RecognizedShapeType.STRAIGHT_LINE,
                message = "Snapped to pristine straight line"
            )
        }

        // 4. Check for Smooth Curve (Removes wobbles/jitter, fits smooth curve)
        if (arcLength > 60f && points.size >= 6) {
            val smoothed = fitSmoothCurvedLine(stroke, points)
            if (smoothed != null) {
                return AutoCorrectionResult(
                    correctedStroke = smoothed,
                    recognizedType = RecognizedShapeType.SMOOTH_CURVE,
                    message = "Fitted smooth curvature"
                )
            }
        }

        return AutoCorrectionResult(correctedStroke = stroke, recognizedType = RecognizedShapeType.NONE)
    }

    /**
     * Detects geometric circle or rectangle from a closed stroke
     */
    private fun detectClosedShape(
        stroke: DrawingStroke,
        points: List<StrokePoint>
    ): AutoCorrectionResult? {
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

        val centerX = sumX / points.size
        val centerY = sumY / points.size
        val width = maxX - minX
        val height = maxY - minY

        if (width < 30f || height < 30f) return null

        // Measure radial variance from centroid
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

        // If radial distance variance is small, it's a circle/ellipse!
        val aspectRatio = width / height
        if (radialErrorRatio < 0.20f && aspectRatio in 0.72f..1.38f) {
            val radius = (width + height) / 4f
            val circleShape = ShapeAnnotation(
                id = "shape_auto_circle_${System.currentTimeMillis()}",
                type = ShapeType.CIRCLE,
                startX = centerX - radius,
                startY = centerY - radius,
                endX = centerX + radius,
                endY = centerY + radius,
                strokeWidth = stroke.strokeWidth,
                color = stroke.color,
                isFilled = false
            )
            return AutoCorrectionResult(
                correctedShape = circleShape,
                recognizedType = RecognizedShapeType.CIRCLE_OR_ELLIPSE,
                message = "Converted to perfect circle"
            )
        }

        // Check if points cluster near the bounding box edges (Rectangle test)
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

        if (avgEdgeDeviation / minDimension < 0.16f) {
            val rectShape = ShapeAnnotation(
                id = "shape_auto_rect_${System.currentTimeMillis()}",
                type = ShapeType.RECTANGLE,
                startX = minX,
                startY = minY,
                endX = maxX,
                endY = maxY,
                strokeWidth = stroke.strokeWidth,
                color = stroke.color,
                isFilled = false
            )
            return AutoCorrectionResult(
                correctedShape = rectShape,
                recognizedType = RecognizedShapeType.RECTANGLE,
                message = "Converted to perfect rectangle"
            )
        }

        return null
    }

    /**
     * Fits a smooth Bezier/spline through sample anchor points of the stroke,
     * stripping micro-jitters and producing an elegant, flowing curve.
     */
    private fun fitSmoothCurvedLine(
        stroke: DrawingStroke,
        points: List<StrokePoint>
    ): DrawingStroke? {
        val count = points.size
        // Sample anchor points along the curve
        val step = max(1, count / 8)
        val controlPoints = mutableListOf<StrokePoint>()
        for (i in 0 until count step step) {
            controlPoints.add(points[i])
        }
        if (controlPoints.last() != points.last()) {
            controlPoints.add(points.last())
        }

        if (controlPoints.size < 3) return null

        // Chaikin / Catmull-Rom smoothing subdivision
        val smoothPoints = mutableListOf<StrokePoint>()
        smoothPoints.add(controlPoints.first())

        for (i in 0 until controlPoints.size - 1) {
            val p0 = if (i > 0) controlPoints[i - 1] else controlPoints[i]
            val p1 = controlPoints[i]
            val p2 = controlPoints[i + 1]
            val p3 = if (i + 2 < controlPoints.size) controlPoints[i + 2] else p2

            // Generate interpolated points on Catmull-Rom spline
            val segments = 6
            for (stepIdx in 1..segments) {
                val t = stepIdx.toFloat() / segments
                val t2 = t * t
                val t3 = t2 * t

                val x = 0.5f * ((2f * p1.x) +
                        (-p0.x + p2.x) * t +
                        (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2 +
                        (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3)

                val y = 0.5f * ((2f * p1.y) +
                        (-p0.y + p2.y) * t +
                        (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2 +
                        (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3)

                val avgPressure = (p1.pressure * (1f - t)) + (p2.pressure * t)
                smoothPoints.add(StrokePoint(x, y, avgPressure, System.currentTimeMillis()))
            }
        }

        return stroke.copy(points = smoothPoints)
    }

    /**
     * True Path Intersection Detection for Eraser:
     * Calculates distance from point or line segment to all segments of a stroke.
     */
    fun doesEraserIntersectStroke(
        eraserCurrent: Offset,
        eraserPrevious: Offset?,
        stroke: DrawingStroke,
        eraserRadius: Float = 36f
    ): Boolean {
        val points = stroke.points
        if (points.isEmpty()) return false

        if (points.size == 1) {
            val p = Offset(points[0].x, points[0].y)
            return hypot((p.x - eraserCurrent.x).toDouble(), (p.y - eraserCurrent.y).toDouble()) <= eraserRadius
        }

        // Check intersection with each segment (P_i, P_{i+1})
        for (i in 0 until points.size - 1) {
            val a = Offset(points[i].x, points[i].y)
            val b = Offset(points[i + 1].x, points[i + 1].y)

            // 1. Point-to-segment distance for current eraser position
            if (distancePointToSegment(eraserCurrent, a, b) <= eraserRadius) {
                return true
            }

            // 2. Segment-to-segment intersection between eraser trajectory and stroke segment
            if (eraserPrevious != null) {
                if (doLineSegmentsIntersect(a, b, eraserPrevious, eraserCurrent)) {
                    return true
                }
                if (distancePointToSegment(eraserPrevious, a, b) <= eraserRadius) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Minimum distance from point P to line segment AB
     */
    fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return hypot((p.x - a.x).toDouble(), (p.y - a.y).toDouble()).toFloat()

        var t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
        t = max(0f, min(1f, t))
        val projX = a.x + t * (b.x - a.x)
        val projY = a.y + t * (b.y - a.y)
        return hypot((p.x - projX).toDouble(), (p.y - projY).toDouble()).toFloat()
    }

    /**
     * Determines whether line segments AB and CD intersect
     */
    fun doLineSegmentsIntersect(a: Offset, b: Offset, c: Offset, d: Offset): Boolean {
        fun ccw(p1: Offset, p2: Offset, p3: Offset): Float {
            return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)
        }

        val ccw1 = ccw(a, b, c)
        val ccw2 = ccw(a, b, d)
        val ccw3 = ccw(c, d, a)
        val ccw4 = ccw(c, d, b)

        return ((ccw1 * ccw2) < 0f) && ((ccw3 * ccw4) < 0f)
    }
}
