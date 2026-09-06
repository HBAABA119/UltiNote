package com.example.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object RulerGeometry {
    // Check if point is near either long edge of ruler, and if so, snap it to the edge line
    fun snapPointToRulerEdge(point: Offset, ruler: RulerState, snapThreshold: Float = 40f): Offset? {
        if (!ruler.isVisible) return null

        val rad = (ruler.angle * PI / 180.0).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)

        // Translate point relative to ruler center
        val dx = point.x - ruler.center.x
        val dy = point.y - ruler.center.y

        // Rotate into ruler local space
        val localX = dx * cosA + dy * sinA
        val localY = -dx * sinA + dy * cosA

        val halfW = ruler.length / 2f
        val halfH = ruler.height / 2f

        // Check if within the x span of the ruler (with slight margin)
        if (localX < -halfW - 20f || localX > halfW + 20f) return null

        // Check distance to top edge (localY = -halfH)
        val distToTop = abs(localY - (-halfH))
        val distToBottom = abs(localY - halfH)

        val targetLocalY = when {
            distToTop < snapThreshold && distToTop <= distToBottom -> -halfH
            distToBottom < snapThreshold -> halfH
            else -> return null
        }

        // Clamp localX to ruler length
        val clampedLocalX = localX.coerceIn(-halfW, halfW)

        // Rotate back to global space
        val globalDx = clampedLocalX * cosA - targetLocalY * sinA
        val globalDy = clampedLocalX * sinA + targetLocalY * cosA

        return Offset(ruler.center.x + globalDx, ruler.center.y + globalDy)
    }

    fun drawRulerOnCanvas(scope: DrawScope, ruler: RulerState) {
        if (!ruler.isVisible) return

        val rad = (ruler.angle * PI / 180.0).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)

        val halfW = ruler.length / 2f
        val halfH = ruler.height / 2f

        // Draw tick marks along top edge
        val step = 15f // tick interval
        var curX = -halfW
        var tickIndex = 0

        while (curX <= halfW) {
            val isMajor = tickIndex % 5 == 0
            val tickLen = if (isMajor) 18f else 10f

            val localX = curX
            val localYTop = -halfH
            val localYTickEnd = -halfH + tickLen

            val gx1 = ruler.center.x + (localX * cosA - localYTop * sinA)
            val gy1 = ruler.center.y + (localX * sinA + localYTop * cosA)

            val gx2 = ruler.center.x + (localX * cosA - localYTickEnd * sinA)
            val gy2 = ruler.center.y + (localX * sinA + localYTickEnd * cosA)

            scope.drawLine(
                color = Color(0x992D3748),
                start = Offset(gx1, gy1),
                end = Offset(gx2, gy2),
                strokeWidth = if (isMajor) 1.8f else 1.0f
            )

            curX += step
            tickIndex++
        }
    }
}

@Composable
fun InteractiveRulerWidget(
    rulerState: RulerState,
    onRulerChange: (RulerState) -> Unit,
    onClose: () -> Unit
) {
    if (!rulerState.isVisible) return

    val density = LocalDensity.current
    val lengthDp = with(density) { rulerState.length.toDp() }
    val heightDp = with(density) { rulerState.height.toDp() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (rulerState.center.x - rulerState.length / 2f).roundToInt(),
                    (rulerState.center.y - rulerState.height / 2f).roundToInt()
                )
            }
            .size(lengthDp, heightDp)
            .rotate(rulerState.angle)
            .shadow(10.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xDCFDFCF7))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onRulerChange(
                        rulerState.copy(
                            center = rulerState.center + dragAmount
                        )
                    )
                }
            }
    ) {
        // Content inside ruler
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Angle indicator & quick rotate
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x334A6B56),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "${rulerState.angle.roundToInt()}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = {
                    val nextAngle = (rulerState.angle + 45f) % 360f
                    onRulerChange(rulerState.copy(angle = nextAngle))
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RotateRight,
                    contentDescription = "Rotate 45 degrees",
                    tint = Color(0xFF4A6B56),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "KOMOREBI STRAIGHTEDGE",
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = Color(0x884A6B56)
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hide ruler",
                    tint = Color(0xFF718096),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
