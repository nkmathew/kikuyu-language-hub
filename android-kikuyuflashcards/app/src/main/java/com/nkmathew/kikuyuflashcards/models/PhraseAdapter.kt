package com.nkmathew.kikuyuflashcards.models

import com.nkmathew.kikuyuflashcards.Phrase

/**
 * Adapter class to convert between new curated FlashcardEntry and legacy Phrase models
 */
object PhraseAdapter {
    /**
     * Convert a FlashcardEntry from the new curated schema to a Phrase for backward compatibility
     */
    fun toPhraseModel(flashcardEntry: FlashcardEntry): Phrase {
        return Phrase(
            english = flashcardEntry.english,
            kikuyu = flashcardEntry.kikuyu,
            // Map the new category to an existing category if possible, or use GENERAL
            category = mapToLegacyCategory(flashcardEntry.category, flashcardEntry.subcategory)
        )
    }

    /**
     * Convert a Phrase model to a FlashcardEntry (with minimal information)
     */
    fun toFlashcardEntry(phrase: Phrase): FlashcardEntry {
        return FlashcardEntry(
            id = generateId(phrase),
            english = phrase.english,
            kikuyu = phrase.kikuyu,
            category = mapToNewCategory(phrase.category),
            difficulty = DifficultyLevels.BEGINNER,  // Default
            source = SourceInfo(
                origin = "Legacy kikuyu-phrases.json",
                createdDate = "",
                lastUpdated = ""
            )
        )
    }

    /**
     * Generate a unique ID for a legacy phrase
     */
    private fun generateId(phrase: Phrase): String {
        // Create a simple hash-based ID
        val hash = (phrase.english.hashCode() + phrase.kikuyu.hashCode()).toString(16)
        return "legacy_${hash}"
    }

    /**
     * Map new category to legacy category
     */
    private fun mapToLegacyCategory(newCategory: String, subcategory: String?): String {
        return when (newCategory) {
            Categories.VOCABULARY -> {
                when (subcategory) {
                    "emotions" -> Phrase.EMOTIONS
                    "time" -> Phrase.TIME
                    "questions" -> Phrase.QUESTIONS
                    "verbs" -> Phrase.VERBS
                    "nouns" -> Phrase.NOUNS
                    "basic" -> Phrase.BASIC_WORDS
                    else -> Phrase.GENERAL
                }
            }
            Categories.PHRASES -> {
                when (subcategory) {
                    "greetings" -> Phrase.GREETINGS
                    "questions" -> Phrase.QUESTIONS
                    else -> Phrase.GENERAL
                }
            }
            else -> Phrase.GENERAL
        }
    }

    /**
     * Map legacy category to new category
     */
    private fun mapToNewCategory(legacyCategory: String): String {
        return when (legacyCategory) {
            Phrase.GREETINGS -> Categories.PHRASES
            Phrase.EMOTIONS, Phrase.BASIC_WORDS, Phrase.NOUNS -> Categories.VOCABULARY
            Phrase.VERBS -> Categories.CONJUGATIONS
            Phrase.QUESTIONS, Phrase.TIME -> Categories.PHRASES
            else -> Categories.VOCABULARY
        }
    }
}