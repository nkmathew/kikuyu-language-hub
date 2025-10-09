package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Enhanced version of PositionManager that supports complex position keys
 * for tracking positions across category and difficulty combinations
 */
class PositionManagerV2(context: Context) {
    companion object {
        private const val TAG = "PositionManagerV2"
        private const val PREFS_NAME = "kikuyu_positions_v2"

        // Key-based position tracking
        private const val KEY_POSITION_PREFIX = "pos_key_"
        private const val KEY_TOTAL_PREFIX = "total_key_"
        private const val KEY_PROGRESS_PREFIX = "progress_key_"

        // Session timeout
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
    }

    // Standard position manager for backward compatibility
    private val standardPositionManager = PositionManager(context)

    // Shared preferences for this manager
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save position with a composite key (e.g., "vocabulary:beginner")
     */
    fun savePositionWithKey(position: Int, key: String, totalCards: Int) {
        try {
            val editor = prefs.edit()

            // Save position with key
            val positionKey = KEY_POSITION_PREFIX + key
            val totalKey = KEY_TOTAL_PREFIX + key
            val progressKey = KEY_PROGRESS_PREFIX + key

            editor.putInt(positionKey, position)
            editor.putInt(totalKey, totalCards)

            // Calculate and save progress percentage
            val progressPercent = if (totalCards > 0) {
                ((position + 1) * 100) / totalCards
            } else 0
            editor.putInt(progressKey, progressPercent)

            // Also update standard position tracking for backward compatibility
            val parts = key.split(":")
            if (parts.size >= 2) {
                val category = if (parts[0] == "all") null else parts[0]
                standardPositionManager.savePosition(position, category, totalCards)
            } else {
                standardPositionManager.savePosition(position, key, totalCards)
            }

            editor.apply()
            Log.d(TAG, "Position saved with key $key: $position/$totalCards ($progressPercent%)")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving position with key: $key", e)
        }
    }

    /**
     * Get last position for a composite key
     */
    fun getLastPositionWithKey(key: String): Int {
        try {
            val positionKey = KEY_POSITION_PREFIX + key
            return prefs.getInt(positionKey, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving position for key: $key", e)
            return 0
        }
    }

    /**
     * Get total card count for a composite key
     */
    fun getTotalCountWithKey(key: String): Int {
        try {
            val totalKey = KEY_TOTAL_PREFIX + key
            return prefs.getInt(totalKey, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving total count for key: $key", e)
            return 0
        }
    }

    /**
     * Get progress percentage for a composite key
     */
    fun getProgressWithKey(key: String): Int {
        try {
            val progressKey = KEY_PROGRESS_PREFIX + key
            return prefs.getInt(progressKey, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving progress for key: $key", e)
            return 0
        }
    }

    /**
     * Check if we should restore the last position based on session timeout
     * Delegates to standard position manager
     */
    fun shouldRestorePosition(): Boolean {
        return standardPositionManager.shouldRestorePosition()
    }

    /**
     * Start a new learning session
     * Delegates to standard position manager
     */
    fun startSession() {
        standardPositionManager.startSession()
    }

    /**
     * Get progress information for a specific composite key
     */
    fun getKeyProgress(key: String): KeyProgress {
        val position = getLastPositionWithKey(key)
        val totalCards = getTotalCountWithKey(key)
        val progressPercent = getProgressWithKey(key)

        return KeyProgress(
            key = key,
            lastPosition = position,
            totalCards = totalCards,
            progressPercent = progressPercent,
            hasProgress = position > 0
        )
    }

    /**
     * Reset position data for a specific key
     */
    fun resetKeyProgress(key: String) {
        try {
            val editor = prefs.edit()
            val positionKey = KEY_POSITION_PREFIX + key
            val totalKey = KEY_TOTAL_PREFIX + key
            val progressKey = KEY_PROGRESS_PREFIX + key

            editor.remove(positionKey)
            editor.remove(totalKey)
            editor.remove(progressKey)
            editor.apply()

            Log.d(TAG, "Reset progress for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting key progress: $key", e)
        }
    }

    /**
     * Get all keys that have position data
     */
    fun getAllKeys(): List<String> {
        val keys = mutableListOf<String>()
        try {
            val allPrefs = prefs.all

            // Extract keys from preference keys
            allPrefs.keys.forEach { prefKey ->
                if (prefKey.startsWith(KEY_POSITION_PREFIX)) {
                    val key = prefKey.removePrefix(KEY_POSITION_PREFIX)
                    keys.add(key)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving all keys", e)
        }

        return keys
    }

    /**
     * Get progress for all keys
     */
    fun getAllKeyProgress(): List<KeyProgress> {
        val progressList = mutableListOf<KeyProgress>()
        val keys = getAllKeys()

        for (key in keys) {
            progressList.add(getKeyProgress(key))
        }

        return progressList.sortedByDescending { it.progressPercent }
    }

    /**
     * Data class for key progress
     */
    data class KeyProgress(
        val key: String,
        val lastPosition: Int,
        val totalCards: Int,
        val progressPercent: Int,
        val hasProgress: Boolean
    ) {
        /**
         * Parse key into components (typically category:difficulty)
         */
        fun parseKey(): Pair<String, String> {
            val parts = key.split(":")
            val category = if (parts.size > 0) parts[0] else "all"
            val difficulty = if (parts.size > 1) parts[1] else "all"
            return Pair(category, difficulty)
        }
    }
}