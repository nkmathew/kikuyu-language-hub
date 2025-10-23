package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages saving and restoring scroll positions for StudyListActivity
 */
class StudyListPositionManager(private val context: Context) {
    companion object {
        private const val TAG = "StudyListPositionManager"
        private const val PREFS_NAME = "study_list_position_prefs"
        private const val KEY_POSITION = "position"
        private const val KEY_CATEGORY = "category"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_SORT = "sort_mode"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save the current scroll position and list state
     */
    fun savePosition(
        position: Int,
        category: String?,
        difficulty: String?,
        sortMode: String,
        totalItems: Int
    ) {
        if (position < 0 || totalItems <= 0) return

        // Create a composite key based on filters to save different positions for different filter combinations
        val key = generateKey(category, difficulty, sortMode)

        prefs.edit().apply {
            putInt("$key:$KEY_POSITION", position)
            putString("$key:$KEY_CATEGORY", category)
            putString("$key:$KEY_DIFFICULTY", difficulty)
            putString("$key:$KEY_SORT", sortMode)
            apply()
        }

        Log.d(TAG, "Saved position $position for $key (total items: $totalItems)")
    }

    /**
     * Get the last saved position for the current filter combination
     */
    fun getLastPosition(category: String?, difficulty: String?, sortMode: String): Int {
        val key = generateKey(category, difficulty, sortMode)
        val position = prefs.getInt("$key:$KEY_POSITION", 0)
        Log.d(TAG, "Retrieved position $position for $key")
        return position
    }

    /**
     * Check if we have a position saved that we should restore
     */
    fun hasLastPosition(category: String?, difficulty: String?, sortMode: String): Boolean {
        val key = generateKey(category, difficulty, sortMode)
        return prefs.contains("$key:$KEY_POSITION")
    }

    /**
     * Clear all saved positions (for troubleshooting)
     */
    fun clearAllPositions() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Cleared all saved positions")
    }

    /**
     * Generate a consistent key for the current filter combination
     */
    private fun generateKey(category: String?, difficulty: String?, sortMode: String): String {
        val categoryPart = category ?: "all"
        val difficultyPart = difficulty ?: "all"
        val sortPart = sortMode.ifEmpty { "default" }
        return "$categoryPart:$difficultyPart:$sortPart"
    }
}