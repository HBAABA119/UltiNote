package com.ultinote.app.canvas

import com.ultinote.app.data.model.DrawingStroke
import com.ultinote.app.data.model.StrokePoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Geometric handwriting tidying. Deliberately language-blind: it only fixes
 * wobble, slant and jitter, so Urdu, Arabic, German and math all improve
 * identically without any recognition or language models.
 *
 * - OFF: raw ink, nothing touched.
 * - SUBTLE (default): de-wobble via light smoothing. Invisible magic.
 * - STRONG: heavier smoothing + near-axis line straightening for export-ready notes.
 */
enum class TidyLevel { OFF, SUBTLE, STRONG }

object HandwritingTidy {

    fun tidy(stroke: DrawingStroke, level: TidyLevel): DrawingStroke {
        if (level == TidyLevel.OFF || stroke.points.size < 4) return stroke
        var pts = resampleEvenly(stroke.points, 4f)
        if (pts.size < 4) return stroke
        pts = smooth(pts, passes = if (level == TidyLevel.SUBTLE) 1 else 2)
        if (level == TidyLevel.STRONG) pts = straightenIfLineLike(pts)
        return stroke.copy(points = pts)
    }

    /** Classic even-spacing resample (lerps x, y, pressure, time). Keeps endpoints exact. */
    private fun resampleEvenly(points: List<StrokePoint>, spacing: Float): List<StrokePoint> {
        if (points.size < 2 || spacing <= 0f) return points
        val out = mutableListOf(points[0])
        var anchor = points[0]
        var carried = 0f
        var i = 1
        while (i < points.size) {
            val next = points[i]
            val d = hypot((next.x - anchor.x).toDouble(), (next.y - anchor.y).toDouble()).toFloat()
            if (d < 1e-6f) {
                i++
                continue
            }
            if (carried + d >= spacing) {
                val t = (spacing - carried) / d
                val q = StrokePoint(
                    x = anchor.x + (next.x - anchor.x) * t,
                    y = anchor.y + (next.y - anchor.y) * t,
                    pressure = anchor.pressure + (next.pressure - anchor.pressure) * t,
                    timestamp = anchor.timestamp + ((next.timestamp - anchor.timestamp) * t).toLong()
                )
                out.add(q)
                anchor = q
                carried = 0f
            } else {
                carried += d
                anchor = next
                i++
            }
        }
        if (out.last() != points.last()) out.add(points.last())
        return out
    }

    /** Moving-average smoothing; endpoints are preserved by clamping the window. */
    private fun smooth(points: List<StrokePoint>, passes: Int): List<StrokePoint> {
        var pts = points
        repeat(passes) {
            pts = pts.mapIndexed { i, p ->
                val a = pts[(i - 1).coerceAtLeast(0)]
                val b = pts[(i + 1).coerceAtMost(pts.size - 1)]
                p.copy(
                    x = (a.x + p.x * 2f + b.x) / 4f,
                    y = (a.y + p.y * 2f + b.y) / 4f
                )
            }
        }
        return pts
    }

    /**
     * If the stroke is already line-like (end-to-end vs path length) AND near an
     * axis, replace it with the clean straight chord. Curves are never touched,
     * so C-shapes, circles and script letters survive intact.
     */
    private fun straightenIfLineLike(points: List<StrokePoint>): List<StrokePoint> {
        if (points.size < 4) return points
        val first = points.first()
        val last = points.last()
        val endDist = hypot((last.x - first.x).toDouble(), (last.y - first.y).toDouble()).toFloat()
        if (endDist < 30f) return points
        var pathLen = 0f
        for (i in 1 until points.size) {
            pathLen += hypot(
                (points[i].x - points[i - 1].x).toDouble(),
                (points[i].y - points[i - 1].y).toDouble()
            ).toFloat()
        }
        if (pathLen <= 0f || endDist / pathLen < 0.92f) return points
        val angleDeg = Math.toDegrees(atan2((last.y - first.y).toDouble(), (last.x - first.x).toDouble()))
        val nearAxis = listOf(0.0, 90.0, 180.0, -90.0, -180.0).any { abs(angleDeg - it) < 12.0 }
        if (!nearAxis) return points
        // Snap the chord axis-aligned and redistribute intermediate points along it.
        val horizontal = abs(last.x - first.x) >= abs(last.y - first.y)
        val n = points.size
        return points.mapIndexed { i, p ->
            val f = i.toFloat() / (n - 1).toFloat()
            if (horizontal) {
                val flatY = (first.y + last.y) / 2f
                p.copy(
                    x = first.x + (last.x - first.x) * f,
                    y = flatY
                )
            } else {
                val flatX = (first.x + last.x) / 2f
                p.copy(
                    x = flatX,
                    y = first.y + (last.y - first.y) * f
                )
            }
        }
    }
}
