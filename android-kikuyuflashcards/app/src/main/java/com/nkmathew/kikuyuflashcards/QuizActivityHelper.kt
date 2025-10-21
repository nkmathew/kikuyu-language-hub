package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Helper class for QuizActivity with utilities for filtering and managing quiz questions
 */
class QuizActivityHelper(private val context: Context) {
    companion object {
        private const val TAG = "QuizActivityHelper"

        // Keywords that indicate test/debug entries that should be excluded from quizzes
        private val TEST_KEYWORDS = listOf(
            "numbered entry",
            "sample vocab",
            "test entry",
            "debug"
        )
    }

    /**
     * Filter out debug/test entries that shouldn't be shown in quizzes
     */
    fun filterQuizEntries(allEntries: List<FlashcardEntry>): List<FlashcardEntry> {
        val filteredEntries = allEntries.filter { entry ->
            // Check if the entry contains any test keywords
            val isTestEntry = TEST_KEYWORDS.any { keyword ->
                entry.english.lowercase().contains(keyword.lowercase()) ||
                entry.kikuyu.lowercase().contains(keyword.lowercase())
            }

            // Only include real (non-test) entries
            !isTestEntry
        }

        Log.d(TAG, "Filtered ${allEntries.size} total entries to ${filteredEntries.size} valid quiz entries")

        return if (filteredEntries.size >= 10) {
            filteredEntries
        } else {
            // If we don't have enough entries after filtering, use all entries
            Log.w(TAG, "Not enough valid entries after filtering (${filteredEntries.size}), using all entries")
            allEntries
        }
    }

    /**
     * Generate random quiz questions from the filtered entries
     */
    fun generateQuizQuestions(
        flashCardManager: FlashCardManagerV2,
        count: Int
    ): List<FlashcardEntry> {
        // Get all entries and filter out test/debug entries
        val validEntries = filterQuizEntries(flashCardManager.getAllEntries())

        // Generate random quiz questions
        val selectedEntries = mutableListOf<FlashcardEntry>()
        val usedIndices = mutableSetOf<Int>()

        // Try to get unique entries
        for (i in 0 until count) {
            if (usedIndices.size >= validEntries.size) {
                // We've used all available entries, start reusing
                usedIndices.clear()
            }

            // Find an unused random entry
            var attempts = 0
            var randomIndex: Int

            do {
                randomIndex = validEntries.indices.random()
                attempts++
            } while (usedIndices.contains(randomIndex) && attempts < 10)

            usedIndices.add(randomIndex)
            selectedEntries.add(validEntries[randomIndex])
        }

        return selectedEntries
    }
}