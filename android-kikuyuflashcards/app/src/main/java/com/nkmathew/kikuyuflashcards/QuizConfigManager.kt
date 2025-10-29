package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log

/**
 * Manages quiz configuration settings with persistent storage
 *
 * Allows storing and retrieving quiz length preference, difficulty filters,
 * and other quiz-related settings. Settings persist across app restarts.
 */
class QuizConfigManager(private val context: Context) {
    companion object {
        private const val TAG = "QuizConfigManager"
        private const val PREFS_NAME = "quiz_config"
        private const val KEY_QUIZ_LENGTH = "quiz_length"
        private const val KEY_DIFFICULTY_FILTER = "difficulty_filter"
        private const val DEFAULT_QUIZ_LENGTH = 20

        /**
         * Available preset quiz lengths for user selection
         */
        val PRESET_LENGTHS = listOf(10, 20, 50, 100)
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the user's preferred quiz length
     */
    fun getQuizLength(): Int = prefs.getInt(KEY_QUIZ_LENGTH, DEFAULT_QUIZ_LENGTH)

    /**
     * Save the user's quiz length preference
     */
    fun setQuizLength(length: Int) {
        Log.d(TAG, "Setting quiz length to $length")
        prefs.edit().putInt(KEY_QUIZ_LENGTH, length).apply()
    }

    /**
     * Get the difficulty filter if set
     *
     * @return Difficulty level string or "all" if no filter is set
     */
    fun getDifficultyFilter(): String = prefs.getString(KEY_DIFFICULTY_FILTER, "all") ?: "all"

    /**
     * Set a difficulty filter for the quiz
     *
     * @param filter Difficulty level to filter by, or "all" for no filtering
     */
    fun setDifficultyFilter(filter: String) {
        Log.d(TAG, "Setting difficulty filter to $filter")
        prefs.edit().putString(KEY_DIFFICULTY_FILTER, filter).apply()
    }
}