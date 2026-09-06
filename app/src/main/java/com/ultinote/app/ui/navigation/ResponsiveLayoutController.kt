package com.ultinote.app.ui.navigation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.ui.components.nativePressable
import com.ultinote.app.ui.theme.LiquidGlassTheme
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.ui.theme.NativeMotion
import com.ultinote.app.util.HapticFeedbackManager
import com.ultinote.app.util.rememberHapticFeedbackManager

enum class NavDestination(val label: String, val icon: ImageVector) {
    LIBRARY("Library", Icons.Default.ViewAgenda),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    FOLDERS("Folders", Icons.Default.Folder),
    THEMES("Themes", Icons.Default.Palette)
}

/**
 * Responsive Layout Controller that dynamically detects device rotation and switches
 * primary UI navigation between a floating translucent glass bottom bar in Portrait mode,
 * and a pill-shaped, translucent glass sidebar in Landscape mode.
 */
@Composable
fun ResponsiveNavigationScaffold(
    showNavigation: Boolean = true,
    currentDestination: NavDestination = NavDestination.LIBRARY,
    onNavigate: (NavDestination) -> Unit,
    onQuickCreateNote: () -> Unit,
    onOpenAiCompanion: (() -> Unit)? = null,
    hapticManager: HapticFeedbackManager = rememberHapticFeedbackManager(),
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val palette = LocalKomorebiPalette.current

    if (!showNavigation) {
        content(false)
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || maxWidth > maxHeight

        if (isLandscape) {
            // LANDSCAPE MODE: Pill-Shaped, Translucent Glass Sidebar
            Row(modifier = Modifier.fillMaxSize()) {
                TranslucentGlassPillSidebar(
                    currentDestination = currentDestination,
                    onNavigate = { dest ->
                        hapticManager.performToolSwitchHaptic()
                        onNavigate(dest)
                    },
                    onQuickCreateNote = {
                        hapticManager.performSnapHaptic()
                        onQuickCreateNote()
                    },
                    onOpenAiCompanion = onOpenAiCompanion
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    content(true)
                }
            }
        } else {
            // PORTRAIT MODE: Floating Liquid Glass Bottom Bar
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    content(false)
                }

                // Docked Floating Glass Bottom Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    TranslucentGlassBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = { dest ->
                            hapticManager.performToolSwitchHaptic()
                            onNavigate(dest)
                        },
                        onQuickCreateNote = {
                            hapticManager.performSnapHaptic()
                            onQuickCreateNote()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Pill-shaped, translucent glass sidebar used exclusively in landscape mode.
 * Features specular gradient border, ambient edge glow, and rounded pill contours.
 */
@Composable
fun TranslucentGlassPillSidebar(
    modifier: Modifier = Modifier,
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onQuickCreateNote: () -> Unit,
    onOpenAiCompanion: (() -> Unit)? = null
) {
    val palette = LocalKomorebiPalette.current
    val glassStyle = LiquidGlassTheme.current
    val sidebarShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)

    Surface(
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .shadow(
                elevation = 12.dp,
                shape = sidebarShape,
                spotColor = glassStyle.edgeGlowColor,
                ambientColor = glassStyle.edgeGlowColor.copy(alpha = 0.12f)
            )
            .border(1.2.dp, glassStyle.borderBrush, sidebarShape),
        shape = sidebarShape,
        color = palette.glassBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(glassStyle.surfaceBrush)
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: App Branding & Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.colorScheme.primary)
                            .shadow(6.dp, CircleShape, spotColor = palette.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "UltiNote",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Native Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Create Note Pill Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .nativePressable(scaleDown = 0.94f, onClick = onQuickCreateNote)
                        .border(1.dp, palette.glassHighlight, CircleShape),
                    color = palette.colorScheme.primary,
                    shape = CircleShape,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Note",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Note",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Vertical Navigation Items
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavDestination.values().forEach { dest ->
                        val isSelected = currentDestination == dest

                        val itemShape = RoundedCornerShape(18.dp)
                        val itemBorder = if (isSelected) {
                            Brush.linearGradient(
                                listOf(
                                    palette.colorScheme.primary.copy(alpha = 0.7f),
                                    palette.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }

                        val indicatorAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = NativeMotion.navSpring,
                            label = "sidebarIndicator"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .nativePressable(scaleDown = 0.97f, onClick = { onNavigate(dest) })
                                .border(if (isSelected) 1.2.dp else 0.dp, itemBorder, itemShape),
                            color = palette.colorScheme.primaryContainer.copy(alpha = 0.45f * indicatorAlpha),
                            shape = itemShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnimatedContent(
                                    targetState = isSelected,
                                    transitionSpec = {
                                        (scaleIn(initialScale = 0.85f) + fadeIn())
                                            .togetherWith(scaleOut() + fadeOut())
                                    },
                                    label = "sidebarIcon"
                                ) { selected ->
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (selected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(if (selected) 22.dp else 20.dp)
                                    )
                                }
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action: AI Companion or Theme badge
            if (onOpenAiCompanion != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenAiCompanion)
                        .border(1.dp, palette.glassBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    color = palette.glassSurface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                            .background(palette.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Assistant",
                                tint = palette.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "AI Companion",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pill-shaped floating translucent glass bottom navigation bar used in portrait mode.
 */
@Composable
fun TranslucentGlassBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onQuickCreateNote: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val glassStyle = LiquidGlassTheme.current
    val barShape = RoundedCornerShape(28.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = barShape,
                spotColor = glassStyle.edgeGlowColor,
                ambientColor = glassStyle.edgeGlowColor.copy(alpha = 0.16f)
            )
            .border(1.2.dp, glassStyle.borderBrush, barShape),
        shape = barShape,
        color = palette.glassBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassStyle.surfaceBrush)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.values().forEach { dest ->
                val isSelected = currentDestination == dest

                val itemShape = RoundedCornerShape(20.dp)
                val pillWidth by animateDpAsState(
                    targetValue = if (isSelected) 104.dp else 46.dp,
                    animationSpec = NativeMotion.layoutSpringDp,
                    label = "bottomNavPill"
                )
                Surface(
                    modifier = Modifier
                        .width(pillWidth)
                        .clip(itemShape)
                        .nativePressable(scaleDown = 0.92f, onClick = { onNavigate(dest) }),
                    color = if (isSelected) palette.colorScheme.primaryContainer.copy(alpha = 0.65f) else Color.Transparent,
                    shape = itemShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = if (isSelected) 12.dp else 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.label,
                            tint = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(NativeMotion.fast) + scaleIn(initialScale = 0.8f),
                            exit = fadeOut(NativeMotion.fast) + scaleOut()
                        ) {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Create Floating Pill
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .nativePressable(scaleDown = 0.88f, onClick = onQuickCreateNote)
                    .border(1.2.dp, palette.glassHighlight, CircleShape),
                color = palette.colorScheme.primary,
                shape = CircleShape,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Note",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
