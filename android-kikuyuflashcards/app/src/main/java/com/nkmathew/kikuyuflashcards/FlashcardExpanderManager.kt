package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.models.ExampleSentence

/**
 * Manager that expands entries with examples into individual flashcards for memorization mode.
 * This allows treating each example as a separate flashcard in flashcard mode while keeping
 * them grouped in study mode.
 */
class FlashcardExpanderManager(private val context: Context) {

    companion object {
        private const val TAG = "FlashcardExpanderMgr"
    }

    /**
     * Expand entries with examples into individual flashcards for memorization mode
     *
     * @param entries The original list of entries that may contain examples
     * @return An expanded list where each example is its own flashcard
     */
    fun expandEntriesToFlashcards(entries: List<FlashcardEntry>): List<FlashcardEntry> {
        val expandedCards = mutableListOf<FlashcardEntry>()
        var exampleCount = 0

        try {
            Log.d(TAG, "Expanding ${entries.size} entries with examples for flashcard mode")

            // For each entry
            for (entry in entries) {
                // Add the main entry first
                expandedCards.add(entry)

                // Add each example as a separate flashcard if there are examples
                if (!entry.examples.isNullOrEmpty()) {
                    Log.d(TAG, "Entry ${entry.id} has ${entry.examples!!.size} examples to expand")
                    entry.examples.forEach { example ->
                        // Create a new ID that's deterministic but unique
                        val exampleId = "${entry.id}_ex_${example.kikuyu.hashCode()}"

                        // Create a new flashcard entry for each example
                        val exampleEntry = FlashcardEntry(
                            id = exampleId,
                            english = example.english,
                            kikuyu = example.kikuyu,
                            category = entry.category,
                            subcategory = entry.subcategory,
                            difficulty = entry.difficulty,
                            context = example.context ?: entry.context,
                            culturalNotes = entry.culturalNotes,
                            // No nested examples to avoid infinite recursion
                            examples = null,
                            grammaticalInfo = entry.grammaticalInfo,
                            tags = entry.tags + listOf("example"),
                            quality = entry.quality,
                            source = entry.source
                        )

                        expandedCards.add(exampleEntry)
                        exampleCount++
                    }
                }
            }

            Log.d(TAG, "Expanded ${entries.size} entries into ${expandedCards.size} flashcards (added $exampleCount examples)")

        } catch (e: Exception) {
            Log.e(TAG, "Error expanding entries to flashcards: ${e.message}", e)
        }

        return expandedCards
    }
}