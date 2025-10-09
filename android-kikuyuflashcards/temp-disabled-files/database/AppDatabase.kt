package com.nkmathew.kikuyuflashcards.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nkmathew.kikuyuflashcards.data.dao.PhraseDao
import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity

@Database(
    entities = [PhraseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun phraseDao(): PhraseDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "kikuyu_flashcards_db"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database will be populated when first accessed
            }
        }
    }
}