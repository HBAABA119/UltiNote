package com.ultinote.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.ui.theme.NativeMotion

/**
 * Modifier.nativePressable: iOS/Samsung-style press feedback.
 * Scales the element down on press with a spring and fires a soft haptic.
 * Use before [androidx.compose.ui.draw.clip] so the press bounds match the shape.
 */
fun Modifier.nativePressable(
    scaleDown: Float = 0.96f,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed(
    factory = {
        val haptics = LocalHapticFeedback.current
        var pressed by remember { mutableStateOf(false) }

        val scale by animateFloatAsState(
            targetValue = if (pressed) scaleDown else 1f,
            animationSpec = NativeMotion.pressSpring,
            label = "nativePressableScale",
        )

        this
            .scale(scale)
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
            }
    },
)

/**
 * Full-screen scaffold background with a soft brand gradient.
 * Content lambda receives [BoxWithConstraintsScope] so call sites can read
 * maxWidth/maxHeight and adapt to phone / tablet / foldable / landscape.
 */
@Composable
fun NativeScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    val palette = LocalKomorebiPalette.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.colorScheme.background,
                        palette.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        palette.colorScheme.background,
                    ),
                ),
            ),
    ) {
        content()
    }
}

/**
 * Large iOS-style top bar with bold title and light subtitle.
 */
@Composable
fun NativeLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val palette = LocalKomorebiPalette.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            navigationIcon()
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (navigationIcon != null) 4.dp else 0.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = palette.colorScheme.onSurface,
                maxLines = 1,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.colorScheme.outline,
                    maxLines = 1,
                )
            }
        }
        if (actions != null) {
            actions()
        }
    }
}

/**
 * Rounded, floating search field in the app's glass style.
 */
@Composable
fun NativeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val palette = LocalKomorebiPalette.current
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = palette.glassSurface,
        tonalElevation = 1.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(placeholder, color = palette.colorScheme.outline)
                },
                leadingIcon = leadingIcon,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = palette.colorScheme.primary,
                    focusedTextColor = palette.colorScheme.onSurface,
                    unfocusedTextColor = palette.colorScheme.onSurface,
                ),
            )
            if (trailingContent != null) {
                Box(Modifier.padding(end = 6.dp)) {
                    trailingContent()
                }
            }
        }
    }
}

/**
 * Section header with optional trailing action (e.g. "See all", "New Folder").
 */
@Composable
fun NativeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = LocalKomorebiPalette.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (action != null) {
            action()
        }
    }
}

/**
 * Centered empty state with icon slot, bold title and muted message.
 */
@Composable
fun NativeEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    val palette = LocalKomorebiPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = palette.colorScheme.outline,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * iOS inset-grouped settings card: rounded container with a labeled header.
 */
@Composable
fun NativeSettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScopeAlias.() -> Unit,
) {
    val palette = LocalKomorebiPalette.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = palette.colorScheme.outline,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = palette.glassSurface,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                val receiver = ColumnScopeAlias
                content(receiver)
            }
        }
    }
}

/** Structural alias so NativeSettingsGroup reads like an iOS grouped list. */
object ColumnScopeAlias

/**
 * Elevated surface card with consistent radius.
 */
@Composable
fun NativeSurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalKomorebiPalette.current

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor ?: palette.glassSurface,
        tonalElevation = 2.dp,
    ) {
        Box(content = content)
    }
}

/**
 * Animated calendar day cell with selection scale, note dot, and accessibility.
 */
@Composable
fun NativeAnimatedDayCell(
    dayNum: Int,
    isSelected: Boolean,
    hasNotes: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKomorebiPalette.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = NativeMotion.pressSpring,
        label = "dayCellScale",
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .scale(scale)
            .nativePressable(scaleDown = 0.88f, onClick = onClick)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> palette.colorScheme.primary
                    hasNotes -> palette.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else -> Color.Transparent
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayNum.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || hasNotes) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                palette.colorScheme.onPrimary
            } else {
                palette.colorScheme.onSurface
            },
        )
        if (hasNotes && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(palette.colorScheme.primary),
            )
        }
    }
}
