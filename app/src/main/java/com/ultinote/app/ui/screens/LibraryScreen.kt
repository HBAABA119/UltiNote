package com.ultinote.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.data.local.KomorebiRepository
import com.ultinote.app.data.model.CoverStyle
import com.ultinote.app.data.model.FolderEntity
import com.ultinote.app.data.model.NoteEntity
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.pdf.PdfHelper
import com.ultinote.app.ui.components.AvailableFolderSvgIcons
import com.ultinote.app.ui.components.LiquidGlassCard
import com.ultinote.app.ui.components.LiquidGlassConfirmDialog
import com.ultinote.app.ui.components.LiquidGlassDialog
import com.ultinote.app.ui.components.LiquidGlassFolderCard
import com.ultinote.app.ui.components.LiquidGlassHomeWidgetsSection
import com.ultinote.app.ui.components.LiquidGlassPillButton
import com.ultinote.app.ui.components.getFolderSvgIcon
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import com.ultinote.app.util.rememberHapticFeedbackManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val FolderPastelColors = listOf(
    "#769382", // Sage Green
    "#D98880", // Sakura Rose
    "#A594F9", // Lavender Mist
    "#F5B041", // Warm Honey
    "#5DADE2", // Clear Glacial
    "#48C9B0", // Soft Mint
    "#566573"  // Slate Graphite
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    repository: KomorebiRepository,
    onOpenNote: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val palette = LocalKomorebiPalette.current
    val hapticManager = rememberHapticFeedbackManager()

    val allFolders by repository.allFolders.collectAsState(initial = emptyList())
    val allNotes by repository.allNotes.collectAsState(initial = emptyList())

    // Active Folder Navigation (null = root level)
    var activeFolderId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyFavorites by remember { mutableStateOf(false) }

    // Dialog States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToEditTags by remember { mutableStateOf<NoteEntity?>(null) }

    // Storage: UltiNote is local-first + scoped-storage friendly.
    // App-specific files (UltiNote/, UltiNoteImages/, UltiNoteExports/) + SAF picker need NO permission on Android 10+.
    // We only ask legacy READ on old devices and READ_MEDIA_IMAGES for photo insert on Android 13+.
    var hasFullStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                true
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var showPermissionExplainer by remember { mutableStateOf(false) }

    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasFullStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else perms.values.any { it }
    }

    val imagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* photo picker uses SAF, optional */ }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasFullStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    true
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Nothing to request — SAF + app-specific storage just works. Nudge image permission on 13+ for picker niceness.
            if (Build.VERSION.SDK_INT >= 33) {
                try { imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES) } catch (_: Exception) { }
            }
            hasFullStoragePermission = true
            return
        } else {
            legacyStoragePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // PDF Document Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val targetName = "Imported_${System.currentTimeMillis()}.pdf"
                    val importedFile = PdfHelper.importPdfFromUri(context, uri, targetName)
                    if (importedFile != null && importedFile.exists()) {
                        val pagesCount = PdfHelper.getPdfPageCount(context, importedFile)
                        val newNoteId = repository.createNote(
                            title = importedFile.nameWithoutExtension,
                            folderId = activeFolderId,
                            coverStyle = CoverStyle.CELESTIAL,
                            template = PaperTemplate.RULED,
                            isPdf = true,
                            pdfFilePath = importedFile.absolutePath
                        )
                        // Create pages for PDF
                        for (p in 1 until pagesCount) {
                            repository.addPage(newNoteId, PaperTemplate.RULED)
                        }
                        Toast.makeText(context, "Imported PDF ($pagesCount pages)", Toast.LENGTH_SHORT).show()
                        onOpenNote(newNoteId)
                    } else {
                        Toast.makeText(context, "Unable to read PDF file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error importing PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Filter notes by search and active folder — search hits title + tags, pinned floats to top
    val displayedNotes = allNotes.filter { note ->
        val matchesFolder = if (activeFolderId == null) true else note.folderId == activeFolderId
        val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.tags.contains(searchQuery, ignoreCase = true)
        val matchesFav = !filterOnlyFavorites || note.isFavorite
        matchesFolder && matchesSearch && matchesFav
    }.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })

    val activeFolder = allFolders.find { it.id == activeFolderId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(palette.ambientGradient)
            )
    ) {
        val isTabletLandscape = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // TABLET ADAPTIVE NAVIGATION: Sleek Liquid Glass Side Navigation Rail
            if (isTabletLandscape) {
                Surface(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(palette.glassBorder, palette.glassBorder.copy(alpha = 0.1f))
                            ),
                            RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                        ),
                    color = palette.glassBackground,
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // UltiNote Logo Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.colorScheme.primary)
                                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = palette.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "UltiNote",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.colorScheme.onSurface,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Liquid Glass Studio",
                                        fontSize = 11.sp,
                                        color = palette.colorScheme.primary
                                    )
                                }
                            }

                            // Primary Navigation Links
                            LiquidGlassPillButton(
                                text = "Library & Folders",
                                icon = Icons.Default.FolderSpecial,
                                onClick = { activeFolderId = null },
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LiquidGlassPillButton(
                                text = "Calendar Planner",
                                icon = Icons.Default.CalendarMonth,
                                onClick = onNavigateToCalendar,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LiquidGlassPillButton(
                                text = "Aesthetic Themes",
                                icon = Icons.Default.Settings,
                                onClick = onNavigateToSettings,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Action Buttons
                            Text(
                                text = "QUICK ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = palette.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LiquidGlassPillButton(
                                text = "New Note",
                                icon = Icons.Default.NoteAdd,
                                onClick = { showCreateNoteDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LiquidGlassPillButton(
                                text = "New Folder",
                                icon = Icons.Default.CreateNewFolder,
                                onClick = { showCreateFolderDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LiquidGlassPillButton(
                                text = "Import PDF",
                                icon = Icons.Default.PictureAsPdf,
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Tablet info tag
                        Text(
                            text = "Tablet Landscape Mode • Stylus Ready",
                            fontSize = 11.sp,
                            color = palette.colorScheme.outline
                        )
                    }
                }
            }

            // Main Content Area
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activeFolder != null) {
                                    IconButton(onClick = { activeFolderId = null }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back to all folders",
                                            tint = palette.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Column {
                                    Text(
                                        text = activeFolder?.name ?: "UltiNote Library",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${displayedNotes.size} items • ${allFolders.size} folders",
                                        fontSize = 12.sp,
                                        color = palette.colorScheme.outline
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!isTabletLandscape) {
                                IconButton(onClick = { showCreateFolderDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.CreateNewFolder,
                                        contentDescription = "New Folder",
                                        tint = palette.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "Import PDF",
                                        tint = palette.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = onNavigateToCalendar) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = palette.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = onNavigateToSettings) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = palette.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                floatingActionButton = {
                    if (!isTabletLandscape) {
                        LiquidGlassPillButton(
                            text = "New Note",
                            icon = Icons.Default.Add,
                            onClick = { showCreateNoteDialog = true },
                            isPrimary = true,
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                        )
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Document Storage Access Permission Banner
                    if (!hasFullStoragePermission) {
                        item {
                            LiquidGlassCard(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = palette.glassSurface,
                                elevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(palette.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FolderShared,
                                                contentDescription = null,
                                                tint = palette.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "All Documents & Storage Access",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Grant permission so your PDF textbooks and folders survive uninstalls",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = palette.colorScheme.outline
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    LiquidGlassPillButton(
                                        text = "Grant",
                                        icon = Icons.Default.Check,
                                        isPrimary = true,
                                        onClick = { showPermissionExplainer = true }
                                    )
                                }
                            }
                        }
                    }

                    // Floating Liquid Glass Home Screen Widgets: Quick Note, Folders & Recent Note Spotlight
                    item {
                        LiquidGlassHomeWidgetsSection(
                            folders = allFolders,
                            recentNotes = allNotes,
                            activeFolderId = activeFolderId,
                            hapticManager = hapticManager,
                            onSelectFolder = { selectedFolderId ->
                                activeFolderId = selectedFolderId
                            },
                            onQuickCreateNote = { template ->
                                coroutineScope.launch {
                                    val templateName = template.name.lowercase().replaceFirstChar { it.uppercase() }
                                    val newNoteId = repository.createNote(
                                        title = "$templateName Note",
                                        folderId = activeFolderId,
                                        coverStyle = CoverStyle.MINIMAL_MATCHA,
                                        template = template
                                    )
                                    Toast.makeText(context, "Created $templateName Notebook", Toast.LENGTH_SHORT).show()
                                    onOpenNote(newNoteId)
                                }
                            },
                            onOpenCreateFolder = { showCreateFolderDialog = true },
                            onOpenImportPdf = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            onOpenNote = onOpenNote
                        )
                    }

                    // Search & Filter Bar
                    item {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = palette.glassSurface,
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = palette.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search notes, formulas, topics...", fontSize = 14.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                FilterChip(
                                    selected = filterOnlyFavorites,
                                    onClick = { filterOnlyFavorites = !filterOnlyFavorites },
                                    label = { Text("Favorites", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (filterOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // FOLDERS SECTION (Displayed with Dynamic Visual Fill effect!)
                    if (activeFolderId == null) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FOLDERS & BINDERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = palette.colorScheme.primary
                                )
                                TextButton(onClick = { showCreateFolderDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = palette.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Folder", fontSize = 12.sp, color = palette.colorScheme.primary)
                                }
                            }

                            if (allFolders.isEmpty()) {
                                LiquidGlassCard(
                                    shape = RoundedCornerShape(20.dp),
                                    backgroundColor = palette.glassSurface,
                                    elevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = palette.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No Folders Yet",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap '+ New Folder' to organize your books, classes, and notes",
                                            fontSize = 12.sp,
                                            color = palette.colorScheme.outline
                                        )
                                    }
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    allFolders.forEach { folder ->
                                        val count = allNotes.count { it.folderId == folder.id }
                                        var showMenu by remember { mutableStateOf(false) }

                                        Box {
                                            LiquidGlassFolderCard(
                                                folderName = folder.name,
                                                iconName = folder.iconName,
                                                colorHex = folder.colorHex,
                                                noteCount = count,
                                                isSelected = activeFolderId == folder.id,
                                                onClick = { activeFolderId = folder.id },
                                                onLongClick = { showMenu = true }
                                            )

                                            // Context Menu for rename/delete
                                            IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = palette.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Rename & Edit Icon") },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                                    onClick = {
                                                        showMenu = false
                                                        folderToEdit = folder
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete Folder") },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                    onClick = {
                                                        showMenu = false
                                                        folderToDelete = folder
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // NOTES SECTION
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeFolder != null) "NOTES IN ${activeFolder.name.uppercase()}" else "RECENT NOTEBOOKS & PDFS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = palette.colorScheme.primary
                            )

                            Row {
                                TextButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = palette.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import PDF", fontSize = 12.sp, color = palette.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(onClick = { showCreateNoteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = palette.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Note", fontSize = 12.sp, color = palette.colorScheme.primary)
                                }
                            }
                        }

                        if (displayedNotes.isEmpty()) {
                            LiquidGlassCard(
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = palette.glassSurface,
                                elevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NoteAdd,
                                        contentDescription = null,
                                        tint = palette.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Notes Yet",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Create a handwritten notebook or import a PDF document to start taking notes",
                                        fontSize = 12.sp,
                                        color = palette.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                displayedNotes.forEach { note ->
                                    var showNoteMenu by remember { mutableStateOf(false) }

                                    Box(
                                        modifier = Modifier
                                            .width(185.dp)
                                            .height(240.dp)
                                    ) {
                                        LiquidGlassCard(
                                            shape = RoundedCornerShape(20.dp),
                                            backgroundColor = palette.glassSurface,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onOpenNote(note.id) }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // Cover Header
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                if (note.isPdf) listOf(Color(0xFFE53935), Color(0xFFC62828))
                                                                else when (note.coverStyle) {
                                                                    CoverStyle.BOTANICAL -> listOf(Color(0xFF4A6B56), Color(0xFF2E4537))
                                                                    CoverStyle.SAKURA -> listOf(Color(0xFFC45A77), Color(0xFF8B3A50))
                                                                    CoverStyle.CELESTIAL -> listOf(Color(0xFF6B58A3), Color(0xFF3F3268))
                                                                    CoverStyle.LEATHER_BROWN -> listOf(Color(0xFF7A4F2E), Color(0xFF4A2F1A))
                                                                    CoverStyle.MINIMAL_MATCHA -> listOf(Color(0xFF8DA399), Color(0xFF5F786C))
                                                                    CoverStyle.LAVENDER_MIST -> listOf(Color(0xFFA594F9), Color(0xFF6E56CF))
                                                                    CoverStyle.OBSIDIAN_SLATE -> listOf(Color(0xFF2B2F38), Color(0xFF16181D))
                                                                }
                                                            )
                                                        )
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        if (note.isPdf) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color.White)
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                                            }
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color.White.copy(alpha = 0.25f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("${note.pageCount}p", fontSize = 10.sp, color = Color.White)
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    repository.toggleFavorite(note.id)
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                                contentDescription = "Favorite",
                                                                tint = if (note.isFavorite) Color(0xFFFFD700) else Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }

                                                    Icon(
                                                        imageVector = if (note.isPdf) Icons.Default.PictureAsPdf else Icons.Default.MenuBook,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.35f),
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .align(Alignment.BottomEnd)
                                                    )
                                                }

                                                // Note Info
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                            if (note.isPinned) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PushPin,
                                                                    contentDescription = "Pinned",
                                                                    tint = palette.colorScheme.primary,
                                                                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = note.title,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                color = palette.colorScheme.onSurface,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { showNoteMenu = true },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.MoreVert,
                                                                contentDescription = "More",
                                                                tint = palette.colorScheme.outline,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.updatedAt)) +
                                                                (if (note.tags.isNotBlank()) " • ${note.tags.take(28)}" else ""),
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        color = palette.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }

                                        // Note Actions Dropdown
                                        DropdownMenu(
                                            expanded = showNoteMenu,
                                            onDismissRequest = { showNoteMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Open Notebook") },
                                                leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                                onClick = {
                                                    showNoteMenu = false
                                                    onOpenNote(note.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (note.isPinned) "Unpin from top" else "Pin to top") },
                                                leadingIcon = { Icon(if (note.isPinned) Icons.Default.PushPin else Icons.Default.PushPin, contentDescription = null) },
                                                onClick = {
                                                    showNoteMenu = false
                                                    coroutineScope.launch { repository.togglePin(note.id) }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (note.isFavorite) "Remove favorite" else "Mark favorite") },
                                                leadingIcon = { Icon(if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null) },
                                                onClick = {
                                                    showNoteMenu = false
                                                    coroutineScope.launch { repository.toggleFavorite(note.id) }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Edit tags") },
                                                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                                                onClick = {
                                                    showNoteMenu = false
                                                    noteToEditTags = note
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Note") },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showNoteMenu = false
                                                    noteToDelete = note
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        // ==================== LIQUID GLASS CUSTOM PROMPT DIALOGS ====================

        // 1. Create Folder Dialog (with SVG Icon selector and pastel color picker!)
        if (showCreateFolderDialog) {
            var newFolderName by remember { mutableStateOf("") }
            var selectedIcon by remember { mutableStateOf("folder") }
            var selectedColor by remember { mutableStateOf(FolderPastelColors[0]) }

            LiquidGlassDialog(onDismissRequest = { showCreateFolderDialog = false }) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Aesthetic Folder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        placeholder = { Text("e.g. Calculus & Physics, Chemistry") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // SVG Icon Picker (Calculator, Science, Book, Art, etc.)
                    Text(
                        text = "CHOOSE FOLDER EMBLEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = palette.colorScheme.primary
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AvailableFolderSvgIcons.forEach { (iconKey, vector) ->
                            val isSelected = selectedIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) palette.colorScheme.primaryContainer
                                        else palette.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) palette.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Color Palette Selector
                    Text(
                        text = "PASTEL COLOR THEME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = palette.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FolderPastelColors.forEach { colorHex ->
                            val parsed = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                            val isSelected = selectedColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                    .clickable { selectedColor = colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateFolderDialog = false }) {
                            Text("Cancel", color = palette.colorScheme.outline)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassPillButton(
                            text = "Create Folder",
                            isPrimary = true,
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    coroutineScope.launch {
                                        repository.createFolder(
                                            name = newFolderName.trim(),
                                            parentId = activeFolderId,
                                            colorHex = selectedColor,
                                            icon = selectedIcon
                                        )
                                        showCreateFolderDialog = false
                                        Toast.makeText(context, "Created folder $newFolderName", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // 2. Edit / Rename Folder Dialog
        if (folderToEdit != null) {
            val target = folderToEdit!!
            var editName by remember { mutableStateOf(target.name) }
            var editIcon by remember { mutableStateOf(target.iconName) }
            var editColor by remember { mutableStateOf(target.colorHex) }

            LiquidGlassDialog(onDismissRequest = { folderToEdit = null }) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Folder: ${target.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AvailableFolderSvgIcons.forEach { (iconKey, vector) ->
                            val isSelected = editIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.colorScheme.primaryContainer else palette.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { editIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) palette.colorScheme.primary else palette.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { folderToEdit = null }) {
                            Text("Cancel", color = palette.colorScheme.outline)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassPillButton(
                            text = "Save Changes",
                            isPrimary = true,
                            onClick = {
                                coroutineScope.launch {
                                    repository.updateFolder(
                                        target.copy(name = editName.trim(), iconName = editIcon, colorHex = editColor)
                                    )
                                    folderToEdit = null
                                }
                            }
                        )
                    }
                }
            }
        }

        // 3. Delete Folder Confirmation Dialog
        if (folderToDelete != null) {
            val target = folderToDelete!!
            LiquidGlassConfirmDialog(
                title = "Delete Folder?",
                message = "Are you sure you want to delete '${target.name}'? Notes inside will remain safely in your library root.",
                confirmText = "Delete Folder",
                cancelText = "Cancel",
                icon = Icons.Default.Delete,
                isDestructive = true,
                onConfirm = {
                    coroutineScope.launch {
                        repository.deleteFolder(target.id)
                        folderToDelete = null
                        if (activeFolderId == target.id) activeFolderId = null
                    }
                },
                onDismiss = { folderToDelete = null }
            )
        }

        // 4. Create Note Dialog
        if (showCreateNoteDialog) {
            var noteTitle by remember { mutableStateOf("") }
            var selectedTemplate by remember { mutableStateOf(PaperTemplate.RULED) }
            var selectedCover by remember { mutableStateOf(CoverStyle.BOTANICAL) }

            LiquidGlassDialog(onDismissRequest = { showCreateNoteDialog = false }) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "New Notebook",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Notebook Title") },
                        placeholder = { Text("e.g. Calculus Lecture 1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("PAPER TEMPLATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.colorScheme.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            PaperTemplate.RULED to "Ruled",
                            PaperTemplate.GRID to "Grid",
                            PaperTemplate.DOTTED to "Dotted",
                            PaperTemplate.CORNELL to "Cornell",
                            PaperTemplate.BLANK to "Blank"
                        ).forEach { (tpl, label) ->
                            FilterChip(
                                selected = selectedTemplate == tpl,
                                onClick = { selectedTemplate = tpl },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Text("COVER AESTHETIC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.colorScheme.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            CoverStyle.BOTANICAL to "Botanical",
                            CoverStyle.SAKURA to "Sakura",
                            CoverStyle.CELESTIAL to "Celestial",
                            CoverStyle.MINIMAL_MATCHA to "Matcha",
                            CoverStyle.OBSIDIAN_SLATE to "Obsidian"
                        ).forEach { (cover, label) ->
                            FilterChip(
                                selected = selectedCover == cover,
                                onClick = { selectedCover = cover },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateNoteDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassPillButton(
                            text = "Create & Open",
                            isPrimary = true,
                            onClick = {
                                if (noteTitle.isNotBlank()) {
                                    coroutineScope.launch {
                                        val newId = repository.createNote(
                                            title = noteTitle.trim(),
                                            folderId = activeFolderId,
                                            coverStyle = selectedCover,
                                            template = selectedTemplate
                                        )
                                        showCreateNoteDialog = false
                                        onOpenNote(newId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // 5. Delete Note Confirmation Dialog
        if (noteToDelete != null) {
            val target = noteToDelete!!
            LiquidGlassConfirmDialog(
                title = "Delete Notebook?",
                message = "Are you sure you want to delete '${target.title}'? This action cannot be undone.",
                confirmText = "Delete Notebook",
                cancelText = "Keep Note",
                icon = Icons.Default.Delete,
                isDestructive = true,
                onConfirm = {
                    coroutineScope.launch {
                        repository.deleteNote(target.id)
                        noteToDelete = null
                    }
                },
                onDismiss = { noteToDelete = null }
            )
        }

        // 5b. Edit Tags Dialog — comma-separated, searchable from the top bar
        if (noteToEditTags != null) {
            val target = noteToEditTags!!
            var tagsText by remember(target.id) { mutableStateOf(target.tags) }
            com.ultinote.app.ui.components.LiquidGlassDialog(onDismissRequest = { noteToEditTags = null }) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Edit tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = palette.colorScheme.onSurface)
                    Text("Example: math, chapter-4, exam — search finds them instantly.", fontSize = 12.sp, color = palette.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        placeholder = { Text("math, physics, exam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { noteToEditTags = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                repository.updateTags(target.id, tagsText.trim())
                                noteToEditTags = null
                            }
                        }) { Text("Save tags") }
                    }
                }
            }
        }

        // 6. Storage & Document Access Permission Dialog
        if (showPermissionExplainer) {
            LiquidGlassDialog(onDismissRequest = { showPermissionExplainer = false }) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = null,
                                tint = palette.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Device Document Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "UltiNote is an offline, local-first note-taking and PDF study app. To ensure all your folders, Cornell notes, and textbook annotations stay permanently saved and survive application re-installation, UltiNote needs permission to manage documents on your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "✓ 100% offline & private — no external servers\n✓ Direct PDF textbook imports\n✓ Notes remain safe even if the app is reinstalled",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = palette.colorScheme.outline
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showPermissionExplainer = false }) {
                            Text("Later")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassPillButton(
                            text = "Grant Access",
                            icon = Icons.Default.Check,
                            isPrimary = true,
                            onClick = {
                                showPermissionExplainer = false
                                requestStorageAccess()
                            }
                        )
                    }
                }
            }
        }
    }
}
