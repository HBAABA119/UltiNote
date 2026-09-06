package com.ultinote.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ultinote.app.data.model.FolderEntity
import com.ultinote.app.data.model.NoteEntity
import com.ultinote.app.data.model.PageEntity

@Database(
    entities = [
        FolderEntity::class,
        NoteEntity::class,
        PageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KomorebiDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun pageDao(): PageDao

    companion object {
        @Volatile
        private var INSTANCE: KomorebiDatabase? = null

        fun getDatabase(context: Context): KomorebiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KomorebiDatabase::class.java,
                    "ultinote_notes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
