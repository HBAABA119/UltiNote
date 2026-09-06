package com.ultinote.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ultinote.app.data.model.DrawingLayer
import com.ultinote.app.data.model.LayerBlendMode
import com.ultinote.app.ui.theme.LiquidGlassTheme
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import java.util.UUID

/**
 * Floating Liquid Glass Layers Panel for complex digital note-taking and sketching.
 * Enables users to:
 * 1. Create, reorder, and remove drawing layers.
 * 2. Toggle layer visibility and locking.
 * 3. Independently adjust layer opacity (0% to 100%).
 * 4. Configure Photoshop/Procreate-grade blend modes (Normal, Multiply, Screen, Darken, Lighten, Overlay).
 */
@Composable
fun LayersPanel(
    layers: List<DrawingLayer>,
    activeLayerId: String,
    onSelectActiveLayer: (String) -> Unit,
    onAddLayer: (String) -> Unit,
    onRemoveLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onUpdateOpacity: (String, Float) -> Unit,
    onUpdateBlendMode: (String, LayerBlendMode) -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit,
    onClose: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    val glassStyle = LiquidGlassTheme.current

    LiquidGlassDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(palette.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = palette.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Drawing Layers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )
                        Text(
                            text = "${layers.size} layers active • Stack order",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add layer button
                    IconButton(
                        onClick = {
                            val newName = "Layer ${layers.size + 1}"
                            onAddLayer(newName)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Layer",
                            tint = palette.colorScheme.primary
                        )
                    }
                    // Close button
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Layers",
                            tint = palette.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Layers Stack List (rendered top to bottom)
            val displayLayers = layers.sortedByDescending { it.order }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(displayLayers, key = { _, layer -> layer.id }) { index, layer ->
                    val isActive = layer.id == activeLayerId

                    LayerItemCard(
                        layer = layer,
                        isActive = isActive,
                        canMoveUp = index > 0,
                        canMoveDown = index < displayLayers.size - 1,
                        canDelete = layers.size > 1,
                        onSelect = { onSelectActiveLayer(layer.id) },
                        onToggleVisibility = { onToggleVisibility(layer.id) },
                        onToggleLock = { onToggleLock(layer.id) },
                        onOpacityChange = { newOpacity -> onUpdateOpacity(layer.id, newOpacity) },
                        onBlendModeChange = { mode -> onUpdateBlendMode(layer.id, mode) },
                        onMoveUp = { onMoveLayerUp(layer.id) },
                        onMoveDown = { onMoveLayerDown(layer.id) },
                        onDelete = { onRemoveLayer(layer.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset Presets Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Sketch", "Color", "Highlighter").forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAddLayer(preset)
                                }
                                .border(1.dp, palette.glassBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            color = palette.glassSurface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "+ $preset",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerItemCard(
    layer: DrawingLayer,
    isActive: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onOpacityChange: (Float) -> Unit,
    onBlendModeChange: (LayerBlendMode) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    var showBlendMenu by remember { mutableStateOf(false) }

    val activeBorder = if (isActive) {
        Brush.linearGradient(
            colors = listOf(
                palette.colorScheme.primary.copy(alpha = 0.85f),
                palette.colorScheme.primary.copy(alpha = 0.35f),
                palette.colorScheme.primary.copy(alpha = 0.65f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                palette.glassBorder.copy(alpha = 0.45f),
                palette.glassBorder.copy(alpha = 0.15f)
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(if (isActive) 1.8.dp else 1.dp, activeBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect),
        color = if (isActive) palette.colorScheme.primaryContainer.copy(alpha = 0.22f) else palette.glassSurface.copy(alpha = 0.65f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Visibility, Lock, Name, Active Badge, Reorder, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Visibility Toggle
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (layer.isVisible) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Lock Toggle
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Toggle Lock",
                            tint = if (layer.isLocked) palette.colorScheme.error else palette.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Layer Name
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = layer.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = palette.colorScheme.onSurface
                            )
                            if (isActive) {
                                Surface(
                                    color = palette.colorScheme.primary,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Controls: Reorder & Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (canMoveUp) palette.colorScheme.onSurface else palette.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (canMoveDown) palette.colorScheme.onSurface else palette.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Layer",
                                tint = palette.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Opacity Slider & Blend Mode Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Opacity readout & slider
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Opacity",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(layer.opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )
                    }
                    Slider(
                        value = layer.opacity,
                        onValueChange = onOpacityChange,
                        valueRange = 0.05f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.colorScheme.primary,
                            activeTrackColor = palette.colorScheme.primary,
                            inactiveTrackColor = palette.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Blend Mode Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showBlendMenu = true }
                            .border(1.dp, palette.glassBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        color = palette.glassSurface,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = layer.blendMode.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showBlendMenu,
                        onDismissRequest = { showBlendMenu = false }
                    ) {
                        LayerBlendMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (mode == layer.blendMode) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (mode == layer.blendMode) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = palette.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onBlendModeChange(mode)
                                    showBlendMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
