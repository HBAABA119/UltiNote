package com.ultinote.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.canvas.TidyLevel
import com.ultinote.app.data.local.KomorebiRepository
import com.ultinote.app.data.local.UserPreferencesRepository
import com.ultinote.app.data.model.AppThemeOption
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.data.model.PressureSensitivity
import com.ultinote.app.ink.ConvertLanguages
import com.ultinote.app.ui.theme.AppFontOption
import com.ultinote.app.ui.theme.FontRepository
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.ui.theme.ThemeRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    repository: KomorebiRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalKomorebiPalette.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val prefs = remember(context) { UserPreferencesRepository.get(context) }
    val currentTheme by ThemeRepository.currentTheme.collectAsState()
    val currentFont by FontRepository.currentFont.collectAsState()

    val savedStylus by prefs.stylusOnly.collectAsState(initial = false)
    val savedSmoothing by prefs.smoothing.collectAsState(initial = true)
    val savedAutoSnap by prefs.autoSnap.collectAsState(initial = true)
    val savedPressure by prefs.pressureMult.collectAsState(initial = 1.0f)
    val savedTidy by prefs.tidyLevel.collectAsState(initial = TidyLevel.SUBTLE)
    val savedConvertLang by prefs.convertLang.collectAsState(initial = "en")
    val savedLefty by prefs.leftHanded.collectAsState(initial = false)

    var stylusOnlyDefault by remember(savedStylus) { mutableStateOf(savedStylus) }
    var highPrecisionSmoothing by remember(savedSmoothing) { mutableStateOf(savedSmoothing) }
    var autoSnapDefault by remember(savedAutoSnap) { mutableStateOf(savedAutoSnap) }
    var leftHanded by remember(savedLefty) { mutableStateOf(savedLefty) }
    var tidyLevel by remember(savedTidy) { mutableStateOf(savedTidy) }
    var langMenuOpen by remember { mutableStateOf(false) }
    var pressureLevel by remember(savedPressure) {
        mutableStateOf(
            when {
                savedPressure >= 1.4f -> PressureSensitivity.SOFT
                savedPressure <= 0.75f -> PressureSensitivity.FIRM
                else -> PressureSensitivity.MEDIUM
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Aesthetic Themes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.toolbarBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Aesthetic Themes
            Text(
                text = "AESTHETIC STATIONERY THEMES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppThemeOption.values().forEach { themeOption ->
                    val isSelected = currentTheme == themeOption
                    val themePalette = ThemeRepository.getPalette(themeOption)

                    Card(
                        modifier = Modifier
                            .width(165.dp)
                            .clickable {
                                ThemeRepository.setTheme(themeOption)
                                scope.launch { prefs.setTheme(themeOption) }
                                Toast.makeText(context, "Applied ${themeOption.displayName} Theme", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themePalette.toolbarBackground),
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(themePalette.colorScheme.primary)
                            )
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themePalette.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themePalette.colorScheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themePalette.canvasBackground)
                                        .border(1.dp, Color(0x33000000), CircleShape)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = themePalette.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = themeOption.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = themePalette.colorScheme.onSurface
                            )
                            Text(
                                text = when (themeOption) {
                                    AppThemeOption.MATCHA_CREAM -> "Zen tea paper tones"
                                    AppThemeOption.SAKURA_PASTEL -> "Cherry blossom pastels"
                                    AppThemeOption.LAVENDER_TWILIGHT -> "Gentle violet serenity"
                                    AppThemeOption.OBSIDIAN_DARK -> "Midnight OLED stealth"
                                    AppThemeOption.VINTAGE_PAPER -> "Aged parchment sepia"
                                    AppThemeOption.NORDIC_FROST -> "Minimal Scandinavian"
                                },
                                fontSize = 11.sp,
                                color = themePalette.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section: Aesthetic Typography & Fonts
            Text(
                text = "AESTHETIC LIQUID GLASS TYPOGRAPHY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppFontOption.values().forEach { fontOption ->
                    val isFontSelected = currentFont == fontOption

                    Card(
                        modifier = Modifier
                            .width(165.dp)
                            .clickable {
                                FontRepository.setFont(fontOption)
                                scope.launch { prefs.setFont(fontOption) }
                                Toast.makeText(context, "Applied ${fontOption.displayName}", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFontSelected) palette.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else palette.colorScheme.surface
                        ),
                        border = if (isFontSelected) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(palette.colorScheme.primary)
                            )
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Aa",
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = palette.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isFontSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Font",
                                        tint = palette.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = fontOption.displayName,
                                fontFamily = fontOption.fontFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.onSurface
                            )
                            Text(
                                text = fontOption.subtitle,
                                fontFamily = fontOption.fontFamily,
                                fontSize = 11.sp,
                                color = palette.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = fontOption.sampleText,
                                fontFamily = fontOption.fontFamily,
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = palette.colorScheme.outline
                            )
                        }
                    }
                }
            }
            Text(
                text = "TABLET & STYLUS CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Palm Rejection (Stylus Inking Only)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Fingers only zoom & pan, stylus writes without palm interference. Works with S-Pen, USI + generic active stylus.", fontSize = 12.sp, color = palette.colorScheme.outline)
                        }
                        Switch(
                            checked = stylusOnlyDefault,
                            onCheckedChange = {
                                stylusOnlyDefault = it
                                scope.launch { prefs.setStylusOnly(it) }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bezier Midpoint Smoothing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Interpolates pen stroke points into silky smooth handwriting curves", fontSize = 12.sp, color = palette.colorScheme.outline)
                        }
                        Switch(
                            checked = highPrecisionSmoothing,
                            onCheckedChange = {
                                highPrecisionSmoothing = it
                                scope.launch { prefs.setSmoothing(it) }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Shape Snap", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Circles, lines + rectangles snap clean automatically", fontSize = 12.sp, color = palette.colorScheme.outline)
                        }
                        Switch(
                            checked = autoSnapDefault,
                            onCheckedChange = {
                                autoSnapDefault = it
                                scope.launch { prefs.setAutoSnap(it) }
                            }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Stylus pressure feel — ${pressureLevel.label}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Soft = light touch makes bold lines (great for class). Firm = you press hard for width.", fontSize = 12.sp, color = palette.colorScheme.outline)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PressureSensitivity.values().forEach { level ->
                                FilterChip(
                                    selected = pressureLevel == level,
                                    onClick = {
                                        pressureLevel = level
                                        scope.launch { prefs.setPressureMult(level.multiplier) }
                                    },
                                    label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                                )
                            }
                        }
                        androidx.compose.material3.Slider(
                            value = pressureLevel.multiplier,
                            onValueChange = {},
                            enabled = false,
                            valueRange = 0.4f..1.8f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Handwriting tidy — works in every language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Subtle de-wobbles invisibly (default). Strong also straightens near-axis lines. Off keeps raw ink.", fontSize = 12.sp, color = palette.colorScheme.outline)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TidyLevel.values().forEach { level ->
                                FilterChip(
                                    selected = tidyLevel == level,
                                    onClick = {
                                        tidyLevel = level
                                        scope.launch { prefs.setTidyLevel(level) }
                                    },
                                    label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Left-handed layout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Mirrors the tablet rails so tools sit under your right thumb", fontSize = 12.sp, color = palette.colorScheme.outline)
                        }
                        Switch(
                            checked = leftHanded,
                            onCheckedChange = {
                                leftHanded = it
                                scope.launch { prefs.setLeftHanded(it) }
                            }
                        )
                    }
                }
            }

            // Section: Convert-to-text language (27 packs, English built in)
            Text(
                text = "HANDWRITING CONVERT LANGUAGE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val currentLang = ConvertLanguages.forTag(savedConvertLang)
                    Text("Lasso-select ink → Convert → typed text. Ink is always kept.", fontSize = 12.sp, color = palette.colorScheme.onSurfaceVariant)
                    Box {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { langMenuOpen = true }
                        ) {
                            Text(
                                text = "${currentLang.nativeName} (${currentLang.englishName}) ▾",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        DropdownMenu(expanded = langMenuOpen, onDismissRequest = { langMenuOpen = false }) {
                            ConvertLanguages.all.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.nativeName} — ${lang.englishName}") },
                                    onClick = {
                                        scope.launch { prefs.setConvertLang(lang.tag) }
                                        langMenuOpen = false
                                        Toast.makeText(context, "Convert language: ${lang.englishName}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = "Each pack (~20MB) downloads once on first convert, then works fully offline. Urdu + Arabic align right automatically.",
                        fontSize = 11.sp,
                        color = palette.colorScheme.outline
                    )
                }
            }

            // Section: Local-First Data Storage
            Text(
                text = "LOCAL-FIRST PERSISTENCE & STORAGE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = null, tint = palette.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Local Storage Directory", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(context.getExternalFilesDir(null)?.absolutePath ?: "/storage/emulated/0/Android/data/com.ultinote.app/files", fontSize = 11.sp, color = palette.colorScheme.outline)
                        }
                    }

                    Text(
                        text = "All notes, folders, drawings, and PDF annotations are stored in Room SQLite with full offline-first persistence. No accounts or cloud sync required.",
                        fontSize = 12.sp,
                        color = palette.colorScheme.onSurfaceVariant
                    )
                }
            }

            // About UltiNote
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = palette.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("UltiNote v1.2.1 — local-first school notebook", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Handwriting with pressure + palm rejection, PDF annotation, layers, photos, stickers, planner. AI companion + cloud sync are Coming Soon — your notes stay on-device for now.", fontSize = 11.sp, color = palette.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Coming soon — not in this build", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = palette.colorScheme.primary)
                    Text("• AI Study Companion (Gemini) — summarize, practice Qs, diagrams\n• Cloud sync + backup across phone + tablet\nFor now everything is 100% offline in UltiNoteExports / UltiNoteImages on your device.", fontSize = 12.sp, color = palette.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
