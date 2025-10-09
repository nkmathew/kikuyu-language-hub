package com.nkmathew.kikuyuflashcards.models

import com.nkmathew.kikuyuflashcards.Phrase
import org.junit.Assert.assertEquals
import org.junit.Test

class PhraseAdapterTest {

    @Test
    fun testToPhraseModel() {
        // Create a sample FlashcardEntry
        val entry = FlashcardEntry(
            id = "test_001",
            english = "Hello",
            kikuyu = "Wĩmwega",
            category = Categories.PHRASES,
            subcategory = "greetings",
            difficulty = DifficultyLevels.BEGINNER,
            source = SourceInfo(
                origin = "Test Origin"
            )
        )

        // Convert to Phrase
        val phrase = PhraseAdapter.toPhraseModel(entry)

        // Verify conversion
        assertEquals("Hello", phrase.english)
        assertEquals("Wĩmwega", phrase.kikuyu)
        assertEquals(Phrase.GREETINGS, phrase.category)

        // Verify text property for backward compatibility
        assertEquals("Hello", phrase.text)
    }

    @Test
    fun testToFlashcardEntry() {
        // Create a sample Phrase
        val phrase = Phrase(
            english = "Good morning",
            kikuyu = "Ũhoro wa rũciinĩ",
            category = Phrase.GREETINGS
        )

        // Convert to FlashcardEntry
        val entry = PhraseAdapter.toFlashcardEntry(phrase)

        // Verify conversion
        assertEquals("Good morning", entry.english)
        assertEquals("Ũhoro wa rũciinĩ", entry.kikuyu)
        assertEquals(Categories.PHRASES, entry.category) // Should map greetings to phrases
        assertEquals(DifficultyLevels.BEGINNER, entry.difficulty) // Default difficulty
        assertEquals("Legacy kikuyu-phrases.json", entry.source.origin)
    }

    @Test
    fun testCategoryMapping() {
        // Test mapping each category type

        // Vocabulary (with subcategories)
        val vocabEntry = FlashcardEntry(
            id = "vocab_001",
            english = "Test vocabulary",
            kikuyu = "Test kikuyu",
            category = Categories.VOCABULARY,
            subcategory = "emotions",
            difficulty = DifficultyLevels.BEGINNER,
            source = SourceInfo(origin = "Test")
        )
        assertEquals(Phrase.EMOTIONS, PhraseAdapter.toPhraseModel(vocabEntry).category)

        // Test each subcategory mapping
        val vocabSubcategoryMappings = mapOf(
            "emotions" to Phrase.EMOTIONS,
            "time" to Phrase.TIME,
            "questions" to Phrase.QUESTIONS,
            "verbs" to Phrase.VERBS,
            "nouns" to Phrase.NOUNS,
            "basic" to Phrase.BASIC_WORDS,
            "other" to Phrase.GENERAL
        )

        for ((subcategory, expectedCategory) in vocabSubcategoryMappings) {
            val entry = vocabEntry.copy(subcategory = subcategory)
            assertEquals(expectedCategory, PhraseAdapter.toPhraseModel(entry).category)
        }

        // Phrases (with subcategories)
        val phrasesSubcategoryMappings = mapOf(
            "greetings" to Phrase.GREETINGS,
            "questions" to Phrase.QUESTIONS,
            "other" to Phrase.GENERAL
        )

        for ((subcategory, expectedCategory) in phrasesSubcategoryMappings) {
            val entry = FlashcardEntry(
                id = "phrase_001",
                english = "Test phrase",
                kikuyu = "Test kikuyu",
                category = Categories.PHRASES,
                subcategory = subcategory,
                difficulty = DifficultyLevels.BEGINNER,
                source = SourceInfo(origin = "Test")
            )
            assertEquals(expectedCategory, PhraseAdapter.toPhraseModel(entry).category)
        }

        // Other categories should map to GENERAL
        val otherCategories = listOf(
            Categories.PROVERBS,
            Categories.GRAMMAR,
            Categories.CULTURAL,
            Categories.NUMBERS
        )

        for (category in otherCategories) {
            val entry = FlashcardEntry(
                id = "other_001",
                english = "Test other",
                kikuyu = "Test kikuyu",
                category = category,
                difficulty = DifficultyLevels.BEGINNER,
                source = SourceInfo(origin = "Test")
            )
            assertEquals(Phrase.GENERAL, PhraseAdapter.toPhraseModel(entry).category)
        }
    }

    @Test
    fun testLegacyCategoryMapping() {
        // Test mapping from legacy categories to new schema categories
        val legacyCategoryMappings = mapOf(
            Phrase.GREETINGS to Categories.PHRASES,
            Phrase.EMOTIONS to Categories.VOCABULARY,
            Phrase.BASIC_WORDS to Categories.VOCABULARY,
            Phrase.NOUNS to Categories.VOCABULARY,
            Phrase.VERBS to Categories.CONJUGATIONS,
            Phrase.QUESTIONS to Categories.PHRASES,
            Phrase.TIME to Categories.PHRASES,
            Phrase.GENERAL to Categories.VOCABULARY
        )

        for ((legacyCategory, expectedCategory) in legacyCategoryMappings) {
            val phrase = Phrase(
                english = "Test",
                kikuyu = "Test",
                category = legacyCategory
            )
            val entry = PhraseAdapter.toFlashcardEntry(phrase)
            assertEquals(expectedCategory, entry.category)
        }
    }

    @Test
    fun testIdGeneration() {
        // Ensure IDs are generated consistently for the same phrase
        val phrase = Phrase(
            english = "Hello world",
            kikuyu = "Halo thĩ",
            category = Phrase.GREETINGS
        )

        val entry1 = PhraseAdapter.toFlashcardEntry(phrase)
        val entry2 = PhraseAdapter.toFlashcardEntry(phrase)

        // Same input should produce same ID
        assertEquals(entry1.id, entry2.id)

        // Different phrases should have different IDs
        val differentPhrase = Phrase(
            english = "Different text",
            kikuyu = "Different kikuyu",
            category = Phrase.GREETINGS
        )

        val differentEntry = PhraseAdapter.toFlashcardEntry(differentPhrase)
        assert(entry1.id != differentEntry.id)
    }
}