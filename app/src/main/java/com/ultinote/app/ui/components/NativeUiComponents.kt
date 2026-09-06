package com.ultinote.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Composable-level pressable container with iOS-style scale/fade feedback.
 * Prefer [Modifier.nativePressable] for inline use inside modifier chains.
 */
@Composable
fun nativePressable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    role: Role? = null,
    contentDescriptionText: String? = null,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.72f else 1f,
        animationSpec = tween(90),
        label = "pressAlpha",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clip(shape)
            .then(if (contentDescriptionText != null) Modifier.semantics { this.contentDescription = contentDescriptionText } else Modifier)
            .then(if (role != null) Modifier.semantics { this.role = role } else Modifier)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val up = waitForUpOrCancellation()
                    pressed = false
                    if (up != null) onClick()
                }
            },
    ) {
        content()
    }
}

/**
 * Tile-level pressable with a softer bounce, tuned for cards and rows.
 */
@Composable
fun tilePressable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 500f),
        label = "tileScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val up = waitForUpOrCancellation()
                    pressed = false
                    if (up != null) onClick()
                }
            },
    ) {
        content()
    }
}

/**
 * iOS-style tappable settings row with highlight feedback, optional icon and trailing slot.
 */
@Composable
fun NativeSettingNavTile(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        targetValue = if (pressed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f) else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(120),
        label = "rowBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .heightIn(min = 52.dp)
            .pointerInput(onClick) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val up = waitForUpOrCancellation()
                    pressed = false
                    if (up != null) onClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (trailing != null) trailing()
    }
}

/**
 * iOS-style inset divider that starts after the leading icon column.
 */
@Composable
fun NativeSettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    )
}

/**
 * iOS-style grouped switch tile with title, optional subtitle, and Material switch.
 */
@Composable
fun NativeSettingSwitchTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
            ),
        )
    }
}
