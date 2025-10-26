package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.nkmathew.kikuyuflashcards.models.Categories

/**
 * PositionManager handles intelligent position memory across different categories
 * and learning sessions, allowing users to continue where they left off.
 */
class PositionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PositionManager"
        private const val PREFS_NAME = "kikuyu_positions"
        
        // Preference keys
        private const val KEY_GLOBAL_POSITION = "global_position"
        private const val KEY_LAST_CATEGORY = "last_category"
        private const val KEY_LAST_SESSION_TIME = "last_session_time"
        private const val KEY_TOTAL_CARDS_SEEN = "total_cards_seen"
        private const val KEY_SESSION_COUNT = "session_count"
        
        // Category-specific position keys (prefix + category name)
        private const val CATEGORY_PREFIX = "category_pos_"
        private const val CATEGORY_PROGRESS_PREFIX = "category_progress_"
        
        // Position restoration settings
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
        private const val MIN_POSITION_SAVE_INTERVAL = 5000L // 5 seconds
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var lastSaveTime = 0L
    
    /**
     * Save the current position for the active category
     */
    fun savePosition(position: Int, category: String?, totalCards: Int) {
        // Throttle save operations to avoid excessive writes
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveTime < MIN_POSITION_SAVE_INTERVAL) {
            return
        }
        
        try {
            val editor = prefs.edit()
            
            // Save global position
            editor.putInt(KEY_GLOBAL_POSITION, position)
            editor.putString(KEY_LAST_CATEGORY, category)
            editor.putLong(KEY_LAST_SESSION_TIME, currentTime)
            
            // Save category-specific position
            if (category != null) {
                val categoryKey = CATEGORY_PREFIX + category
                val progressKey = CATEGORY_PROGRESS_PREFIX + category
                editor.putInt(categoryKey, position)
                
                // Calculate and save progress percentage
                val progressPercent = if (totalCards > 0) {
                    ((position + 1) * 100) / totalCards
                } else 0
                editor.putInt(progressKey, progressPercent)
            }
            
            // Update statistics
            val totalCardsSeen = prefs.getInt(KEY_TOTAL_CARDS_SEEN, 0)
            if (position > totalCardsSeen) {
                editor.putInt(KEY_TOTAL_CARDS_SEEN, position)
            }
            
            editor.apply()
            lastSaveTime = currentTime
            
            Log.d(TAG, "Position saved: $position in category: ${category ?: "All"}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving position", e)
        }
    }
    
    /**
     * Get the last saved position for a specific category
     */
    fun getLastPosition(category: String?): Int {
        return try {
            if (category != null) {
                val categoryKey = CATEGORY_PREFIX + category
                prefs.getInt(categoryKey, 0)
            } else {
                prefs.getInt(KEY_GLOBAL_POSITION, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving position for category: $category", e)
            0
        }
    }
    
    /**
     * Check if we should restore the last position based on session timeout
     */
    fun shouldRestorePosition(): Boolean {
        val lastSessionTime = prefs.getLong(KEY_LAST_SESSION_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastSession = currentTime - lastSessionTime
        
        return lastSessionTime > 0 && timeSinceLastSession <= SESSION_TIMEOUT_MS
    }
    
    /**
     * Get information about the last learning session for display
     */
    fun getLastSessionInfo(): SessionInfo {
        val lastCategory = prefs.getString(KEY_LAST_CATEGORY, null)
        val lastPosition = getLastPosition(lastCategory)
        val lastSessionTime = prefs.getLong(KEY_LAST_SESSION_TIME, 0)
        val totalCardsSeen = prefs.getInt(KEY_TOTAL_CARDS_SEEN, 0)
        
        return SessionInfo(
            lastCategory = lastCategory,
            lastPosition = lastPosition,
            lastSessionTime = lastSessionTime,
            totalCardsSeen = totalCardsSeen,
            shouldRestore = shouldRestorePosition()
        )
    }
    
    /**
     * Get progress information for a specific category
     */
    fun getCategoryProgress(category: String): CategoryProgress {
        val positionKey = CATEGORY_PREFIX + category
        val progressKey = CATEGORY_PROGRESS_PREFIX + category
        
        val position = prefs.getInt(positionKey, 0)
        val progressPercent = prefs.getInt(progressKey, 0)
        
        return CategoryProgress(
            category = category,
            lastPosition = position,
            progressPercent = progressPercent,
            hasProgress = position > 0
        )
    }
    
    /**
     * Get progress for all categories that have been accessed
     */
    fun getAllCategoryProgress(): List<CategoryProgress> {
        val progressList = mutableListOf<CategoryProgress>()
        
        try {
            val allPrefs = prefs.all
            val categories = mutableSetOf<String>()
            
            // Extract category names from preference keys
            allPrefs.keys.forEach { key ->
                if (key.startsWith(CATEGORY_PREFIX)) {
                    val category = key.removePrefix(CATEGORY_PREFIX)
                    categories.add(category)
                }
            }
            
            // Get progress for each category
            categories.forEach { category ->
                progressList.add(getCategoryProgress(category))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving category progress", e)
        }
        
        return progressList.sortedByDescending { it.progressPercent }
    }
    
    /**
     * Reset position data for a specific category
     */
    fun resetCategoryProgress(category: String) {
        try {
            val editor = prefs.edit()
            val positionKey = CATEGORY_PREFIX + category
            val progressKey = CATEGORY_PROGRESS_PREFIX + category
            
            editor.remove(positionKey)
            editor.remove(progressKey)
            editor.apply()
            
            Log.d(TAG, "Reset progress for category: $category")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting category progress", e)
        }
    }
    
    /**
     * Reset all position data
     */
    fun resetAllProgress() {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "All position data reset")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting all progress", e)
        }
    }
    
    /**
     * Mark the start of a new learning session
     */
    fun startSession() {
        try {
            val editor = prefs.edit()
            val sessionCount = prefs.getInt(KEY_SESSION_COUNT, 0)
            editor.putInt(KEY_SESSION_COUNT, sessionCount + 1)
            editor.putLong(KEY_LAST_SESSION_TIME, System.currentTimeMillis())
            editor.apply()
            
            Log.d(TAG, "New session started: ${sessionCount + 1}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting session", e)
        }
    }
    
    /**
     * Get the total number of learning sessions
     */
    fun getSessionCount(): Int {
        return prefs.getInt(KEY_SESSION_COUNT, 0)
    }
    
    /**
     * Get formatted text for "continue where you left off" message
     */
    fun getContinueLearningMessage(flashCardManager: FlashCardManager): String? {
        val sessionInfo = getLastSessionInfo()
        
        if (!sessionInfo.shouldRestore || sessionInfo.lastPosition <= 0) {
            return null
        }
        
        val categoryName = sessionInfo.lastCategory?.let { 
            Categories.getCategoryDisplayName(it) 
        } ?: "All Categories"
        
        // For now, we'll use a default total since we don't have access to the manager
        val totalCards = sessionInfo.lastPosition + 10 // Estimate
        
        val progressPercent = if (totalCards > 0) {
            ((sessionInfo.lastPosition + 1) * 100) / totalCards
        } else 0
        
        return "Continue $categoryName\nCard ${sessionInfo.lastPosition + 1} of $totalCards ($progressPercent%)"
    }
    
    /**
     * Data class for session information
     */
    data class SessionInfo(
        val lastCategory: String?,
        val lastPosition: Int,
        val lastSessionTime: Long,
        val totalCardsSeen: Int,
        val shouldRestore: Boolean
    )
    
    /**
     * Data class for category progress
     */
    data class CategoryProgress(
        val category: String,
        val lastPosition: Int,
        val progressPercent: Int,
        val hasProgress: Boolean
    )
}