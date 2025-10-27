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
    }

    /**
     * Get the section header IDs to filter out
     *
     * This is used to filter out entries that are grammar descriptions or section headers
     * rather than actual translation content. These entries were marked with "content_type": "section_header"
     * in the JSON data, but we filter them by ID here since the FlashcardEntry model doesn't have a contentType field.
     */
    private fun getSectionHeaderIds(): List<String> {
        return listOf(
            // Conjugation section headers
            "conj-022-002", "conj-025-001", "conj-026-001", "conj-026-002",

            // Grammar section headers
            "gram-010-001", "gram-010-002", "grammar-012-001", "grammar-012-002",
            "grammar-013-001", "grammar-014-001", "grammar-014-002", "grammar-014-003",
            "grammar-016-001", "grammar-017-001", "grammar-017-002", "grammar-017-003",
            "grammar-018-001", "grammar-018-002", "grammar-019-001", "grammar-019-002",
            "grammar-020-001", "grammar-020-002", "grammar-021-001", "grammar-021-002",
            "grammar-022-001", "grammar-022-002", "grammar-023-001", "grammar-023-002",
            "grammar-024-001", "grammar-025-001", "grammar-027-001", "grammar-027-002",

            // Other section headers
            "unknown-61085435", "vocab-011-005"
        )
    }

    /**
     * Filter out section header entries that shouldn't be shown in quizzes
     */
    fun filterQuizEntries(allEntries: List<FlashcardEntry>): List<FlashcardEntry> {
        // Get section header IDs to filter out
        val sectionHeaderIds = getSectionHeaderIds()

        // Filter out section headers by ID
        val filteredEntries = allEntries.filterNot { entry ->
            sectionHeaderIds.contains(entry.id)
        }

        val removedCount = allEntries.size - filteredEntries.size
        Log.d(TAG, "Filtered ${allEntries.size} total entries to ${filteredEntries.size} valid quiz entries (removed $removedCount section headers)")

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
        // Get all entries and filter out section headers
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