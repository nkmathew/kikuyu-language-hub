package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log

/**
 * Manages Fact or Fiction configuration settings with persistent storage
 *
 * Allows storing and retrieving game length preference, difficulty filters,
 * and other game-related settings. Settings persist across app restarts.
 */
class FactOrFictionConfigManager(private val context: Context) {
    companion object {
        private const val TAG = "FactOrFictionConfigManager"
        private const val PREFS_NAME = "fact_or_fiction_config"
        private const val KEY_GAME_LENGTH = "game_length"
        private const val KEY_DIFFICULTY_FILTER = "difficulty_filter"
        private const val DEFAULT_GAME_LENGTH = 10

        /**
         * Available preset game lengths for user selection
         */
        val PRESET_LENGTHS = listOf(5, 10, 20, 50)
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the user's preferred game length
     */
    fun getGameLength(): Int = prefs.getInt(KEY_GAME_LENGTH, DEFAULT_GAME_LENGTH)

    /**
     * Save the user's game length preference
     */
    fun setGameLength(length: Int) {
        Log.d(TAG, "Setting game length to $length")
        prefs.edit().putInt(KEY_GAME_LENGTH, length).apply()
    }

    /**
     * Get the difficulty filter if set
     *
     * @return Difficulty level string or "medium" if no filter is set
     */
    fun getDifficultyFilter(): String = prefs.getString(KEY_DIFFICULTY_FILTER, "medium") ?: "medium"

    /**
     * Set a difficulty filter for the game
     *
     * @param filter Difficulty level to filter by (easy, medium, or hard)
     */
    fun setDifficultyFilter(filter: String) {
        Log.d(TAG, "Setting difficulty filter to $filter")
        prefs.edit().putString(KEY_DIFFICULTY_FILTER, filter).apply()
    }
}