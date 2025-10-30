package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import java.util.*

/**
 * FailureTracker - Tracks and analyzes user learning failures
 * 
 * Features:
 * - Track failed answers across all learning modes
 * - Categorize failures by type (translation, recognition, recall, etc.)
 * - Maintain failure history and trends
 * - Generate problem words lists
 * - Track improvement over time
 */
class FailureTracker(private val context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences("FailureTracker", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "FailureTracker"
        private const val KEY_FAILURE_RECORDS = "failure_records"
        private const val KEY_SESSION_DATA = "session_data"
        private const val KEY_DIFFICULTY_WORDS = "difficulty_words"
        private const val MAX_FAILURE_RECORDS = 1000
        private const val FAILURE_DECAY_DAYS = 30 // Remove failures older than 30 days
    }
    
    data class FailureRecord(
        val phraseId: String,
        val englishText: String,
        val kikuyuText: String,
        val category: String,
        val failureType: FailureType,
        val learningMode: LearningMode,
        val timestamp: Long,
        val userAnswer: String = "",
        val correctAnswer: String = "",
        val difficulty: String = "medium",
        val responseTime: Long = 0L // Time taken to answer in milliseconds
    )
    
    enum class FailureType {
        TRANSLATION_ERROR,        // Wrong translation
        RECOGNITION_ERROR,        // Didn't recognize the word
        RECALL_ERROR,            // Couldn't recall the word
        SPELLING_ERROR,          // Close but incorrect spelling
        CONFUSION_ERROR,         // Mixed up with similar words
        TIMEOUT_ERROR,           // Ran out of time
        MULTIPLE_CHOICE_ERROR,   // Wrong multiple choice selection
        FILL_BLANK_ERROR,        // Wrong fill-in-the-blank answer
        CLOZE_ERROR,             // Wrong cloze test answer
        CATEGORY_ERROR,          // Wrong category identification
        SENTENCE_STRUCTURE_ERROR, // Wrong word order in sentence unscramble
        VOWEL_ERROR              // Specifically vowel-related errors
    }
    
    enum class LearningMode {
        FLASHCARD,              // Standard flashcard mode
        TYPE_IN_RECALL,         // Type-in recall mode
        FILL_BLANK,             // Fill-in-the-blank mode
        CLOZE_TEST,             // Cloze test mode
        SPEED_MATCH,            // Speed match game
        MULTIPLE_ANSWERS,       // Multiple answers game
        WORD_ASSOCIATION,       // Word association game
        BEAT_CLOCK,             // Beat the clock game
        STREAK_MASTER,          // Streak master game
        SENTENCE_UNSCRAMBLE,    // Sentence unscramble mode
        VOWEL_HUNT              // Vowel Hunt mode
    }
    
    data class DifficultyWord(
        val phraseId: String,
        val englishText: String,
        val kikuyuText: String,
        val category: String,
        val failureCount: Int,
        val lastFailure: Long,
        val improvementStreak: Int = 0,
        val masteryLevel: MasteryLevel = MasteryLevel.STRUGGLING,
        val failureTypes: Set<FailureType> = emptySet(),
        val averageResponseTime: Long = 0L
    )
    
    enum class MasteryLevel {
        STRUGGLING,     // High failure rate, recent failures
        CHALLENGING,    // Moderate failure rate, improving
        LEARNING,       // Low failure rate, occasional mistakes
        MASTERED        // No recent failures, consistent success
    }
    
    data class SessionStats(
        val sessionId: String,
        val startTime: Long,
        val endTime: Long = 0,
        val totalAttempts: Int = 0,
        val totalFailures: Int = 0,
        val failuresByType: Map<FailureType, Int> = emptyMap(),
        val failuresByMode: Map<LearningMode, Int> = emptyMap(),
        val improvedWords: Set<String> = emptySet(),
        val newProblemWords: Set<String> = emptySet()
    )
    
    private var failureRecords: MutableList<FailureRecord> = mutableListOf()
    private var currentSession: SessionStats? = null
    private var difficultyWords: MutableMap<String, DifficultyWord> = mutableMapOf()
    
    init {
        loadData()
        cleanupOldRecords()
        startNewSession()
    }
    
    /**
     * Record a failure for analytics and tracking
     */
    fun recordFailure(
        entry: FlashcardEntry,
        failureType: FailureType,
        learningMode: LearningMode,
        userAnswer: String = "",
        correctAnswer: String = "",
        difficulty: String = "medium",
        responseTime: Long = 0L
    ) {
        val failureRecord = FailureRecord(
            phraseId = entry.id,
            englishText = entry.english,
            kikuyuText = entry.kikuyu,
            category = entry.category,
            failureType = failureType,
            learningMode = learningMode,
            timestamp = System.currentTimeMillis(),
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            difficulty = difficulty,
            responseTime = responseTime
        )
        
        // Add to records
        failureRecords.add(failureRecord)
        
        // Update difficulty word tracking
        updateDifficultyWord(entry, failureType, responseTime)
        
        // Update session stats
        updateSessionStats(failureRecord)
        
        // Limit records size
        if (failureRecords.size > MAX_FAILURE_RECORDS) {
            failureRecords.removeAt(0)
        }
        
        saveData()
        
        Log.d(TAG, "Recorded failure: ${entry.english} - $failureType in $learningMode")
    }
    
    /**
     * Record a successful answer for improvement tracking
     */
    fun recordSuccess(entry: FlashcardEntry, learningMode: LearningMode, responseTime: Long = 0L) {
        val phraseId = entry.id
        
        difficultyWords[phraseId]?.let { difficultyWord ->
            // Update improvement streak
            val updatedWord = difficultyWord.copy(
                improvementStreak = difficultyWord.improvementStreak + 1,
                averageResponseTime = calculateNewResponseTime(difficultyWord.averageResponseTime, responseTime),
                masteryLevel = calculateMasteryLevel(difficultyWord)
            )
            
            difficultyWords[phraseId] = updatedWord
            
            // Track in session
            currentSession?.let { session ->
                currentSession = session.copy(improvedWords = session.improvedWords + phraseId)
            }
            
            saveData()
        }
        
        Log.d(TAG, "Recorded success: ${entry.english}")
    }
    
    /**
     * Get the most problematic words for the user
     */
    fun getProblemWords(limit: Int = 20): List<DifficultyWord> {
        return difficultyWords.values
            .sortedWith(compareByDescending<DifficultyWord> { it.failureCount }
                .thenBy { it.lastFailure })
            .take(limit)
    }
    
    /**
     * Get words by specific failure type
     */
    fun getWordsByFailureType(failureType: FailureType): List<DifficultyWord> {
        return difficultyWords.values
            .filter { it.failureTypes.contains(failureType) }
            .sortedByDescending { it.failureCount }
    }
    
    /**
     * Get words by category
     */
    fun getProblemWordsByCategory(category: String): List<DifficultyWord> {
        return difficultyWords.values
            .filter { it.category.equals(category, ignoreCase = true) }
            .sortedByDescending { it.failureCount }
    }
    
    /**
     * Get words that need the most attention (high failure rate, recent failures)
     */
    fun getWordsNeedingAttention(limit: Int = 10): List<DifficultyWord> {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000) // 7 days ago
        
        return difficultyWords.values
            .filter { 
                it.failureCount >= 3 && 
                it.lastFailure > weekAgo &&
                it.masteryLevel != MasteryLevel.MASTERED
            }
            .sortedWith(compareByDescending<DifficultyWord> { 
                // Priority score: recent failures + high failure count
                val recencyScore = if (it.lastFailure > weekAgo) 10 else 0
                val frequencyScore = minOf(it.failureCount, 10)
                recencyScore + frequencyScore
            }.thenBy { it.masteryLevel })
            .take(limit)
    }
    
    /**
     * Get improvement trends
     */
    fun getImprovementTrends(phraseId: String): List<FailureRecord> {
        return failureRecords
            .filter { it.phraseId == phraseId }
            .sortedBy { it.timestamp }
    }
    
    /**
     * Get failure statistics
     */
    fun getFailureStats(): Map<String, Any> {
        val now = System.currentTimeMillis()
        val dayAgo = now - (24 * 60 * 60 * 1000)
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        val recentFailures = failureRecords.filter { it.timestamp > dayAgo }
        val weeklyFailures = failureRecords.filter { it.timestamp > weekAgo }
        
        val failuresByType = failureRecords
            .groupBy { it.failureType }
            .mapValues { it.value.size }
        
        val failuresByMode = failureRecords
            .groupBy { it.learningMode }
            .mapValues { it.value.size }
        
        val failuresByDifficulty = failureRecords
            .groupBy { it.difficulty }
            .mapValues { it.value.size }
        
        return mapOf<String, Any>(
            "total_failures" to failureRecords.size,
            "recent_failures_24h" to recentFailures.size,
            "weekly_failures" to weeklyFailures.size,
            "unique_problem_words" to difficultyWords.size,
            "failures_by_type" to failuresByType,
            "failures_by_mode" to failuresByMode,
            "failures_by_difficulty" to failuresByDifficulty,
            "most_common_failure_type" to (failuresByType.maxByOrNull { it.value }?.key?.toString() ?: ""),
            "most_challenging_mode" to (failuresByMode.maxByOrNull { it.value }?.key?.toString() ?: "")
        )
    }
    
    /**
     * Clear failure history for a word (when mastered)
     */
    fun clearFailuresForWord(phraseId: String) {
        difficultyWords.remove(phraseId)
        failureRecords.removeAll { it.phraseId == phraseId }
        saveData()
        
        Log.d(TAG, "Cleared failures for mastered word: $phraseId")
    }
    
    /**
     * Reset all failure tracking data
     */
    fun resetAllData() {
        failureRecords.clear()
        difficultyWords.clear()
        currentSession = null
        preferences.edit().clear().apply()
        startNewSession()
        
        Log.d(TAG, "Reset all failure tracking data")
    }
    
    // Private helper methods
    
    private fun updateDifficultyWord(entry: FlashcardEntry, failureType: FailureType, responseTime: Long) {
        val phraseId = entry.id
        
        val existingWord = difficultyWords[phraseId]
        val updatedWord = if (existingWord != null) {
            existingWord.copy(
                failureCount = existingWord.failureCount + 1,
                lastFailure = System.currentTimeMillis(),
                improvementStreak = 0, // Reset improvement streak on failure
                masteryLevel = MasteryLevel.STRUGGLING, // Reset to struggling on failure
                failureTypes = existingWord.failureTypes + failureType,
                averageResponseTime = calculateNewResponseTime(existingWord.averageResponseTime, responseTime)
            )
        } else {
            DifficultyWord(
                phraseId = phraseId,
                englishText = entry.english,
                kikuyuText = entry.kikuyu,
                category = entry.category,
                failureCount = 1,
                lastFailure = System.currentTimeMillis(),
                failureTypes = setOf(failureType),
                averageResponseTime = responseTime
            )
        }
        
        difficultyWords[phraseId] = updatedWord
    }
    
    private fun updateSessionStats(failureRecord: FailureRecord) {
        currentSession?.let { session ->
            currentSession = session.copy(
                totalAttempts = session.totalAttempts + 1,
                totalFailures = session.totalFailures + 1,
                failuresByType = session.failuresByType + (failureRecord.failureType to (session.failuresByType[failureRecord.failureType] ?: 0) + 1),
                failuresByMode = session.failuresByMode + (failureRecord.learningMode to (session.failuresByMode[failureRecord.learningMode] ?: 0) + 1),
                newProblemWords = if (difficultyWords[failureRecord.phraseId]?.failureCount == 1) {
                    session.newProblemWords + failureRecord.phraseId
                } else {
                    session.newProblemWords
                }
            )
        }
    }
    
    private fun calculateNewResponseTime(currentAvg: Long, newTime: Long): Long {
        return if (currentAvg == 0L) {
            newTime
        } else {
            (currentAvg + newTime) / 2
        }
    }
    
    private fun calculateMasteryLevel(difficultyWord: DifficultyWord): MasteryLevel {
        val now = System.currentTimeMillis()
        val daysSinceLastFailure = (now - difficultyWord.lastFailure) / (24 * 60 * 60 * 1000)
        
        return when {
            difficultyWord.improvementStreak >= 5 && daysSinceLastFailure >= 7 -> MasteryLevel.MASTERED
            difficultyWord.improvementStreak >= 3 && daysSinceLastFailure >= 3 -> MasteryLevel.LEARNING
            difficultyWord.failureCount <= 3 && daysSinceLastFailure >= 1 -> MasteryLevel.LEARNING
            difficultyWord.failureCount >= 10 || daysSinceLastFailure < 1 -> MasteryLevel.STRUGGLING
            else -> MasteryLevel.CHALLENGING
        }
    }
    
    private fun startNewSession() {
        currentSession = SessionStats(
            sessionId = UUID.randomUUID().toString(),
            startTime = System.currentTimeMillis()
        )
    }
    
    private fun cleanupOldRecords() {
        val cutoffTime = System.currentTimeMillis() - (FAILURE_DECAY_DAYS * 24 * 60 * 60 * 1000L)
        
        val beforeCount = failureRecords.size
        failureRecords.removeAll { it.timestamp < cutoffTime }
        
        // Also clean up difficulty words for words that haven't failed recently
        difficultyWords.entries.removeAll { it.value.lastFailure < cutoffTime }
        
        if (failureRecords.size != beforeCount) {
            Log.d(TAG, "Cleaned up ${beforeCount - failureRecords.size} old failure records")
            saveData()
        }
    }
    
    private fun loadData() {
        try {
            // Load failure records
            val recordsJson = preferences.getString(KEY_FAILURE_RECORDS, null)
            if (recordsJson != null) {
                val type = object : TypeToken<MutableList<FailureRecord>>() {}.type
                failureRecords = gson.fromJson(recordsJson, type) ?: mutableListOf()
            }
            
            // Load difficulty words
            val difficultyWordsJson = preferences.getString(KEY_DIFFICULTY_WORDS, null)
            if (difficultyWordsJson != null) {
                val type = object : TypeToken<MutableMap<String, DifficultyWord>>() {}.type
                difficultyWords = gson.fromJson(difficultyWordsJson, type) ?: mutableMapOf()
            }
            
            Log.d(TAG, "Loaded ${failureRecords.size} failure records and ${difficultyWords.size} difficulty words")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading failure tracking data", e)
            // Reset data on error
            failureRecords = mutableListOf()
            difficultyWords = mutableMapOf()
        }
    }
    
    private fun saveData() {
        try {
            val recordsJson = gson.toJson(failureRecords)
            val difficultyWordsJson = gson.toJson(difficultyWords)
            
            preferences.edit()
                .putString(KEY_FAILURE_RECORDS, recordsJson)
                .putString(KEY_DIFFICULTY_WORDS, difficultyWordsJson)
                .apply()
            
            Log.d(TAG, "Saved ${failureRecords.size} failure records and ${difficultyWords.size} difficulty words")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving failure tracking data", e)
        }
    }
}