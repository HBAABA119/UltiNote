package com.ultinote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.ui.theme.LocalKomorebiPalette

@Composable
fun TextEditDialog(
    initialText: String = "",
    initialSize: Float = 18f,
    initialBold: Boolean = false,
    onConfirm: (text: String, size: Float, isBold: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val palette = LocalKomorebiPalette.current
    var text by remember { mutableStateOf(initialText) }
    var fontSize by remember { mutableFloatStateOf(initialSize) }
    var isBold by remember { mutableStateOf(initialBold) }

    LiquidGlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialText.isEmpty()) "Insert Aesthetic Text" else "Edit Text Block",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.colorScheme.onSurface
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type notes, formulas, or headings...") },
                maxLines = 6,
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Font Size: ${fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.colorScheme.onSurface
                )

                FilterChip(
                    selected = isBold,
                    onClick = { isBold = !isBold },
                    label = { Text("Bold") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Slider(
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 12f..36f,
                steps = 12
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = palette.colorScheme.outline)
                }

                Spacer(modifier = Modifier.width(8.dp))

                LiquidGlassPillButton(
                    text = "Save",
                    isPrimary = true,
                    onClick = {
                        if (text.isNotBlank()) {
                            onConfirm(text, fontSize, isBold)
                        }
                        onDismiss()
                    }
                )
            }
        }
    }
}

