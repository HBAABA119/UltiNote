package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId IS :parentId ORDER BY name ASC")
    fun getFoldersByParent(parentId: String?): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId IS :folderId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderId: String?): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isPdf = 1 ORDER BY updatedAt DESC")
    fun getPdfNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE linkedDate = :date ORDER BY updatedAt DESC")
    fun getNotesByDate(date: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE linkedDate IS NOT NULL ORDER BY linkedDate ASC")
    fun getAllCalendarNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
}

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE noteId = :noteId ORDER BY pageIndex ASC")
    fun getPagesForNote(noteId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE noteId = :noteId ORDER BY pageIndex ASC")
    suspend fun getPagesListForNote(noteId: String): List<PageEntity>

    @Query("SELECT * FROM pages WHERE noteId = :noteId AND pageIndex = :index LIMIT 1")
    suspend fun getPageByIndex(noteId: String, index: Int): PageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Update
    suspend fun updatePage(page: PageEntity)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deletePageById(id: String)

    @Query("DELETE FROM pages WHERE noteId = :noteId")
    suspend fun deletePagesForNote(noteId: String)
}
