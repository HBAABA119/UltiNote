package com.ultinote.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Centralized motion tokens for the native-feel design system.
 * Springs are used for gesture-driven transforms (press, selection),
 * tweens for ambient transitions (fades, backgrounds).
 */
object NativeMotion {
    // Gesture-driven springs
    val pressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val navSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val layoutSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /** D-valued variant of [layoutSpring] for animating sizes with [animateDpAsState]. */
    val layoutSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    // Ambient tweens
    val fast = tween<Float>(durationMillis = 140, easing = LinearEasing)

    val normal = tween<Float>(durationMillis = 240, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))

    val slow = tween<Float>(durationMillis = 380, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
}
