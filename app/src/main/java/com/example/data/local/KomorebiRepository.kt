package com.example.data.local

import android.content.Context
import com.example.data.model.CoverStyle
import com.example.data.model.DrawingStroke
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PageEntity
import com.example.data.model.PaperTemplate
import com.example.data.model.ShapeAnnotation
import com.example.data.model.ShapeType
import com.example.data.model.StrokePoint
import com.example.data.model.TextAnnotation
import com.example.data.model.ToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KomorebiRepository(
    private val context: Context,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val pageDao: PageDao
) {
    constructor(context: Context, database: KomorebiDatabase) : this(
        context = context,
        folderDao = database.folderDao(),
        noteDao = database.noteDao(),
        pageDao = database.pageDao()
    )

    // Folders
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    fun getFoldersByParent(parentId: String?): Flow<List<FolderEntity>> = folderDao.getFoldersByParent(parentId)
    suspend fun getFolder(id: String): FolderEntity? = folderDao.getFolderById(id)

    suspend fun createFolder(name: String, parentId: String? = null, colorHex: String = "#8DA399", icon: String = "folder"): String {
        val id = UUID.randomUUID().toString()
        val folder = FolderEntity(
            id = id,
            name = name,
            parentId = parentId,
            colorHex = colorHex,
            iconName = icon,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        folderDao.insertFolder(folder)
        ensureLocalStorageDirectory(folder.name)
        return id
    }

    suspend fun updateFolder(folder: FolderEntity) {
        folderDao.updateFolder(folder.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteFolder(folderId: String) {
        folderDao.deleteFolderById(folderId)
    }

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val pdfNotes: Flow<List<NoteEntity>> = noteDao.getPdfNotes()
    val calendarNotes: Flow<List<NoteEntity>> = noteDao.getAllCalendarNotes()

    fun getNotesInFolder(folderId: String?): Flow<List<NoteEntity>> = noteDao.getNotesByFolder(folderId)
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    fun getNotesForDate(dateStr: String): Flow<List<NoteEntity>> = noteDao.getNotesByDate(dateStr)
    suspend fun getNote(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun createNote(
        title: String,
        folderId: String? = null,
        coverStyle: CoverStyle = CoverStyle.BOTANICAL,
        template: PaperTemplate = PaperTemplate.RULED,
        linkedDate: String? = null,
        isPdf: Boolean = false,
        pdfFilePath: String? = null
    ): String {
        val noteId = UUID.randomUUID().toString()
        val note = NoteEntity(
            id = noteId,
            folderId = folderId,
            title = title,
            coverStyle = coverStyle,
            defaultTemplate = template,
            pageCount = 1,
            isFavorite = false,
            isPinned = false,
            tags = if (isPdf) "PDF, Reference" else "Notes",
            linkedDate = linkedDate,
            isPdf = isPdf,
            pdfFilePath = pdfFilePath,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        noteDao.insertNote(note)

        // Create first blank page
        val firstPage = PageEntity(
            id = UUID.randomUUID().toString(),
            noteId = noteId,
            pageIndex = 0,
            template = template,
            strokesJson = "[]",
            textBlocksJson = "[]",
            shapesJson = "[]",
            stickersJson = "[]",
            pdfPageIndex = 0,
            updatedAt = System.currentTimeMillis()
        )
        pageDao.insertPage(firstPage)

        return noteId
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun renameNote(noteId: String, newTitle: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(noteId: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePin(noteId: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateTags(noteId: String, tags: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(tags = tags, updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveNoteToFolder(noteId: String, folderId: String?) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(folderId = folderId, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(noteId: String) {
        pageDao.deletePagesForNote(noteId)
        noteDao.deleteNoteById(noteId)
    }

    // Pages
    fun getPagesForNote(noteId: String): Flow<List<PageEntity>> = pageDao.getPagesForNote(noteId)
    suspend fun getPagesList(noteId: String): List<PageEntity> = pageDao.getPagesListForNote(noteId)

    suspend fun addPage(noteId: String, template: PaperTemplate = PaperTemplate.RULED): PageEntity {
        val existing = pageDao.getPagesListForNote(noteId)
        val newIndex = existing.size
        val newPage = PageEntity(
            id = UUID.randomUUID().toString(),
            noteId = noteId,
            pageIndex = newIndex,
            template = template,
            strokesJson = "[]",
            textBlocksJson = "[]",
            shapesJson = "[]",
            stickersJson = "[]",
            pdfPageIndex = newIndex,
            updatedAt = System.currentTimeMillis()
        )
        pageDao.insertPage(newPage)

        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(pageCount = newIndex + 1, updatedAt = System.currentTimeMillis()))
        }
        return newPage
    }

    suspend fun updatePage(page: PageEntity) {
        pageDao.updatePage(page.copy(updatedAt = System.currentTimeMillis()))
        val note = noteDao.getNoteById(page.noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deletePage(noteId: String, pageId: String) {
        pageDao.deletePageById(pageId)
        val remaining = pageDao.getPagesListForNote(noteId)
        for (i in remaining.indices) {
            if (remaining[i].pageIndex != i) {
                pageDao.updatePage(remaining[i].copy(pageIndex = i))
            }
        }
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(pageCount = maxOf(1, remaining.size), updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun duplicatePage(noteId: String, pageId: String): PageEntity? {
        val existing = pageDao.getPagesListForNote(noteId)
        val src = existing.firstOrNull { it.id == pageId } ?: return null
        val newPage = src.copy(
            id = UUID.randomUUID().toString(),
            pageIndex = existing.size,
            pdfPageIndex = src.pdfPageIndex,
            updatedAt = System.currentTimeMillis()
        )
        pageDao.insertPage(newPage)
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.updateNote(note.copy(pageCount = existing.size + 1, updatedAt = System.currentTimeMillis()))
        }
        return newPage
    }

    suspend fun movePage(noteId: String, fromIndex: Int, toIndex: Int) {
        val existing = pageDao.getPagesListForNote(noteId).toMutableList()
        if (fromIndex !in existing.indices || toIndex !in existing.indices) return
        val item = existing.removeAt(fromIndex)
        existing.add(toIndex, item)
        for (i in existing.indices) {
            if (existing[i].pageIndex != i) {
                pageDao.updatePage(existing[i].copy(pageIndex = i))
            }
        }
    }

    // Local file persistence helper
    private fun ensureLocalStorageDirectory(folderName: String) {
        try {
            val rootDocs = File(context.getExternalFilesDir(null), "UltiNote/$folderName")
            if (!rootDocs.exists()) {
                rootDocs.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun importImageFromUri(uri: android.net.Uri): File? = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "UltiNoteImages")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val target = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(target).use { output -> input.copyTo(output) }
            }
            if (target.exists() && target.length() > 0) return@withContext target
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    // Seed default sample notes if empty - demo data removed per user request
    suspend fun seedInitialDataIfEmpty(samplePdfPath: String? = null) = withContext(Dispatchers.IO) {
        // App starts clean without demo data
    }
}
