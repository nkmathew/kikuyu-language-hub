package com.nkmathew.kikuyuflashcards.data.dao

import androidx.room.*
import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    
    @Query("SELECT * FROM phrases ORDER BY id ASC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE category = :category ORDER BY id ASC")
    fun getPhrasesByCategory(category: String): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE difficulty = :difficulty ORDER BY id ASC")
    fun getPhrasesByDifficulty(difficulty: Int): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE isBookmarked = 1 ORDER BY id ASC")
    fun getBookmarkedPhrases(): Flow<List<PhraseEntity>>
    
    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhraseById(id: Long): PhraseEntity?
    
    @Query("SELECT * FROM phrases ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomPhrases(count: Int): List<PhraseEntity>
    
    @Query("SELECT * FROM phrases WHERE category = :category ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomPhrasesByCategory(category: String, count: Int): List<PhraseEntity>
    
    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun getPhrasesCount(): Int
    
    @Query("SELECT COUNT(*) FROM phrases WHERE category = :category")
    suspend fun getPhrasesCountByCategory(category: String): Int
    
    @Query("SELECT DISTINCT category FROM phrases ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT * FROM phrases WHERE lastReviewed < :threshold ORDER BY lastReviewed ASC")
    suspend fun getPhrasesNeedingReview(threshold: Long): List<PhraseEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)
    
    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)
    
    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
    
    @Query("DELETE FROM phrases")
    suspend fun deleteAllPhrases()
    
    @Query("UPDATE phrases SET correctCount = correctCount + 1, lastReviewed = :timestamp WHERE id = :id")
    suspend fun recordCorrectAnswer(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE phrases SET incorrectCount = incorrectCount + 1, lastReviewed = :timestamp WHERE id = :id")
    suspend fun recordIncorrectAnswer(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE phrases SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Long, isBookmarked: Boolean)
}