package com.nkmathew.kikuyuflashcards.data.repository

import android.content.Context
import android.util.Log
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.data.PhraseCategory
import com.nkmathew.kikuyuflashcards.data.dao.PhraseDao
import com.nkmathew.kikuyuflashcards.data.database.AppDatabase
import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PhraseRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val phraseDao: PhraseDao = database.phraseDao()
    
    companion object {
        private const val TAG = "PhraseRepository"
    }
    
    // Convert PhraseEntity to legacy Phrase for compatibility
    private fun PhraseEntity.toPhrase() = Phrase(english, kikuyu)
    
    // Convert legacy Phrase to PhraseEntity
    private fun Phrase.toPhraseEntity(category: PhraseCategory = PhraseCategory.GREETINGS, difficulty: Int = 1) = 
        PhraseEntity(
            english = english,
            kikuyu = kikuyu,
            category = category.name,
            difficulty = difficulty
        )
    
    // Initialize database with JSON data if empty
    suspend fun initializeDatabase() {
        val count = phraseDao.getPhrasesCount()
        if (count == 0) {
            Log.d(TAG, "Database is empty, populating from JSON...")
            populateFromJson()
        }
    }
    
    private suspend fun populateFromJson() {
        try {
            val inputStream = context.assets.open("kikuyu-phrases.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)

            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("phrases")
            val entities = mutableListOf<PhraseEntity>()
            
            for (i in 0 until jsonArray.length()) {
                val phraseObject = jsonArray.getJSONObject(i)
                val english = phraseObject.getString("english")
                val kikuyu = phraseObject.getString("kikuyu")
                
                // Categorize phrases based on content (simple heuristic)
                val category = categorizePhrase(english)
                val difficulty = calculateDifficulty(english, kikuyu)
                
                entities.add(
                    PhraseEntity(
                        english = english,
                        kikuyu = kikuyu,
                        category = category.name,
                        difficulty = difficulty
                    )
                )
            }
            
            phraseDao.insertPhrases(entities)
            Log.d(TAG, "Successfully populated database with ${entities.size} phrases")
            
        } catch (e: IOException) {
            Log.e(TAG, "Error loading JSON file", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating database", e)
        }
    }
    
    // Simple categorization based on keywords
    private fun categorizePhrase(english: String): PhraseCategory {
        val lowerText = english.lowercase()
        return when {
            lowerText.contains("hello") || lowerText.contains("good") || lowerText.contains("hi") -> PhraseCategory.GREETINGS
            lowerText.contains("mother") || lowerText.contains("father") || lowerText.contains("family") -> PhraseCategory.FAMILY
            lowerText.contains("food") || lowerText.contains("eat") || lowerText.contains("water") -> PhraseCategory.FOOD
            lowerText.contains("one") || lowerText.contains("two") || lowerText.contains("three") || lowerText.matches(".*\\d.*".toRegex()) -> PhraseCategory.NUMBERS
            lowerText.contains("time") || lowerText.contains("day") || lowerText.contains("hour") -> PhraseCategory.TIME
            lowerText.contains("rain") || lowerText.contains("sun") || lowerText.contains("weather") -> PhraseCategory.WEATHER
            lowerText.contains("happy") || lowerText.contains("sad") || lowerText.contains("love") -> PhraseCategory.EMOTIONS
            lowerText.contains("car") || lowerText.contains("bus") || lowerText.contains("travel") -> PhraseCategory.TRANSPORTATION
            lowerText.contains("work") || lowerText.contains("business") || lowerText.contains("money") -> PhraseCategory.BUSINESS
            lowerText.contains("doctor") || lowerText.contains("health") || lowerText.contains("medicine") -> PhraseCategory.MEDICAL
            lowerText.contains("school") || lowerText.contains("learn") || lowerText.contains("student") -> PhraseCategory.EDUCATION
            lowerText.contains("god") || lowerText.contains("pray") || lowerText.contains("church") -> PhraseCategory.RELIGION
            else -> PhraseCategory.GREETINGS
        }
    }
    
    // Calculate difficulty based on phrase length and complexity
    private fun calculateDifficulty(english: String, kikuyu: String): Int {
        val avgLength = (english.length + kikuyu.length) / 2
        val wordCount = english.split(" ").size
        
        return when {
            avgLength <= 10 && wordCount <= 2 -> 1  // Very Easy
            avgLength <= 20 && wordCount <= 3 -> 2  // Easy
            avgLength <= 35 && wordCount <= 5 -> 3  // Medium
            avgLength <= 50 && wordCount <= 7 -> 4  // Hard
            else -> 5  // Very Hard
        }
    }
    
    // Flow-based data access
    fun getAllPhrases(): Flow<List<PhraseEntity>> = phraseDao.getAllPhrases()
    
    fun getPhrasesByCategory(category: PhraseCategory): Flow<List<PhraseEntity>> = 
        phraseDao.getPhrasesByCategory(category.name)
    
    fun getPhrasesByDifficulty(difficulty: Int): Flow<List<PhraseEntity>> = 
        phraseDao.getPhrasesByDifficulty(difficulty)
    
    fun getBookmarkedPhrases(): Flow<List<PhraseEntity>> = phraseDao.getBookmarkedPhrases()
    
    // Legacy compatibility methods
    suspend fun getAllPhrasesAsLegacy(): List<Phrase> = 
        phraseDao.getAllPhrases().first().map { it.toPhrase() }
    
    suspend fun getRandomPhrases(count: Int): List<PhraseEntity> = 
        phraseDao.getRandomPhrases(count)
    
    suspend fun getRandomPhrasesByCategory(category: PhraseCategory, count: Int): List<PhraseEntity> = 
        phraseDao.getRandomPhrasesByCategory(category.name, count)
    
    // Learning progress methods
    suspend fun recordCorrectAnswer(phraseId: Long) {
        phraseDao.recordCorrectAnswer(phraseId)
    }
    
    suspend fun recordIncorrectAnswer(phraseId: Long) {
        phraseDao.recordIncorrectAnswer(phraseId)
    }
    
    suspend fun updateBookmarkStatus(phraseId: Long, isBookmarked: Boolean) {
        phraseDao.updateBookmarkStatus(phraseId, isBookmarked)
    }
    
    suspend fun getPhrasesNeedingReview(): List<PhraseEntity> {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return phraseDao.getPhrasesNeedingReview(oneDayAgo)
    }
    
    // Statistics
    suspend fun getPhrasesCount(): Int = phraseDao.getPhrasesCount()
    
    suspend fun getPhrasesCountByCategory(category: PhraseCategory): Int = 
        phraseDao.getPhrasesCountByCategory(category.name)
    
    suspend fun getAllCategories(): List<PhraseCategory> = 
        phraseDao.getAllCategories().map { PhraseCategory.fromString(it) }
}