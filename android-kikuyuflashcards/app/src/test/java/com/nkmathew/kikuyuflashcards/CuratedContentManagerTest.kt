package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.res.AssetManager
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

@RunWith(MockitoJUnitRunner::class)
class CuratedContentManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var assetManager: AssetManager

    private val legacyJson = """
        {
            "phrases": [
                {
                    "english": "Hello",
                    "kikuyu": "Wĩmwega",
                    "category": "greetings"
                },
                {
                    "english": "How are you?",
                    "kikuyu": "Ũhana atĩa?",
                    "category": "greetings"
                }
            ]
        }
    """.trimIndent()

    private val curatedVocabJson = """
        {
            "metadata": {
                "schema_version": "1.0",
                "created_date": "2025-10-07T00:00:00Z",
                "curator": "Test Curator",
                "source_files": ["test.txt"],
                "total_entries": 1,
                "description": "Test vocabulary"
            },
            "entries": [
                {
                    "id": "vocab_001",
                    "english": "Tree",
                    "kikuyu": "Mũtĩ",
                    "category": "vocabulary",
                    "difficulty": "beginner",
                    "source": {
                        "origin": "Test Origin"
                    }
                }
            ]
        }
    """.trimIndent()

    private val curatedPhrasesJson = """
        {
            "metadata": {
                "schema_version": "1.0",
                "created_date": "2025-10-07T00:00:00Z",
                "curator": "Test Curator",
                "source_files": ["test.txt"],
                "total_entries": 1,
                "description": "Test phrases"
            },
            "entries": [
                {
                    "id": "phrase_001",
                    "english": "Good afternoon",
                    "kikuyu": "Ũhoro wa mĩaraho",
                    "category": "phrases",
                    "difficulty": "beginner",
                    "source": {
                        "origin": "Test Origin"
                    }
                }
            ]
        }
    """.trimIndent()

    @Before
    fun setup() {
        `when`(context.assets).thenReturn(assetManager)
    }

    @Test
    fun testLoadLegacyContentOnly() {
        // Mock asset manager to return legacy content only
        val inputStream = ByteArrayInputStream(legacyJson.toByteArray())
        whenever(assetManager.open("kikuyu-phrases.json")).thenReturn(inputStream)
        whenever(assetManager.list("curated-content")).thenReturn(null)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Verify the content was loaded
        assert(curatedContentManager.allEntries.size == 2)
        assert(curatedContentManager.allEntries.any { it.english == "Hello" })
        assert(curatedContentManager.allEntries.any { it.english == "How are you?" })
    }

    @Test
    fun testLoadCuratedContentOnly() {
        // Mock legacy file not found
        whenever(assetManager.open("kikuyu-phrases.json")).thenThrow(IOException())

        // Mock curated content directories
        whenever(assetManager.list("curated-content")).thenReturn(arrayOf("vocabulary", "phrases"))
        whenever(assetManager.list("curated-content/vocabulary")).thenReturn(arrayOf("test_vocab.json"))
        whenever(assetManager.list("curated-content/phrases")).thenReturn(arrayOf("test_phrases.json"))

        // Mock file content
        val vocabStream = ByteArrayInputStream(curatedVocabJson.toByteArray())
        val phrasesStream = ByteArrayInputStream(curatedPhrasesJson.toByteArray())
        whenever(assetManager.open("curated-content/vocabulary/test_vocab.json")).thenReturn(vocabStream)
        whenever(assetManager.open("curated-content/phrases/test_phrases.json")).thenReturn(phrasesStream)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Verify the content was loaded
        assert(curatedContentManager.allEntries.size == 2)
        assert(curatedContentManager.allEntries.any { it.english == "Tree" && it.category == Categories.VOCABULARY })
        assert(curatedContentManager.allEntries.any { it.english == "Good afternoon" && it.category == Categories.PHRASES })
    }

    @Test
    fun testLoadBothLegacyAndCuratedContent() {
        // Mock legacy content
        val legacyStream = ByteArrayInputStream(legacyJson.toByteArray())
        whenever(assetManager.open("kikuyu-phrases.json")).thenReturn(legacyStream)

        // Mock curated content directories
        whenever(assetManager.list("curated-content")).thenReturn(arrayOf("vocabulary"))
        whenever(assetManager.list("curated-content/vocabulary")).thenReturn(arrayOf("test_vocab.json"))

        // Mock file content
        val vocabStream = ByteArrayInputStream(curatedVocabJson.toByteArray())
        whenever(assetManager.open("curated-content/vocabulary/test_vocab.json")).thenReturn(vocabStream)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Verify the content was loaded from both sources
        assert(curatedContentManager.allEntries.size == 3)
        assert(curatedContentManager.allEntries.any { it.english == "Hello" })
        assert(curatedContentManager.allEntries.any { it.english == "How are you?" })
        assert(curatedContentManager.allEntries.any { it.english == "Tree" })
    }

    @Test
    fun testFilteringByCategory() {
        // Mock asset manager to return legacy content
        val legacyStream = ByteArrayInputStream(legacyJson.toByteArray())
        whenever(assetManager.open("kikuyu-phrases.json")).thenReturn(legacyStream)

        // Mock curated content directories
        whenever(assetManager.list("curated-content")).thenReturn(arrayOf("vocabulary", "phrases"))
        whenever(assetManager.list("curated-content/vocabulary")).thenReturn(arrayOf("test_vocab.json"))
        whenever(assetManager.list("curated-content/phrases")).thenReturn(arrayOf("test_phrases.json"))

        // Mock file content
        val vocabStream = ByteArrayInputStream(curatedVocabJson.toByteArray())
        val phrasesStream = ByteArrayInputStream(curatedPhrasesJson.toByteArray())
        whenever(assetManager.open("curated-content/vocabulary/test_vocab.json")).thenReturn(vocabStream)
        whenever(assetManager.open("curated-content/phrases/test_phrases.json")).thenReturn(phrasesStream)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Test filtering by category
        val vocabularyEntries = curatedContentManager.getEntriesByCategory(Categories.VOCABULARY)
        assert(vocabularyEntries.size == 1)
        assert(vocabularyEntries[0].english == "Tree")

        val phrasesEntries = curatedContentManager.getEntriesByCategory(Categories.PHRASES)
        assert(phrasesEntries.size == 1)
        assert(phrasesEntries[0].english == "Good afternoon")

        // Legacy content is converted to entries and should be filtered by mapped category
        val greetingsEntries = curatedContentManager.toPhraseList(
            curatedContentManager.allEntries.filter { it.english == "Hello" || it.english == "How are you?" }
        )
        assert(greetingsEntries.size == 2)
        assert(greetingsEntries.all { it.category == "greetings" })
    }

    @Test
    fun testFilteringByDifficulty() {
        // Mock curated content with different difficulties
        val curatedJson = """
            {
                "metadata": {
                    "schema_version": "1.0",
                    "created_date": "2025-10-07T00:00:00Z",
                    "curator": "Test Curator",
                    "source_files": ["test.txt"],
                    "total_entries": 3,
                    "description": "Test content"
                },
                "entries": [
                    {
                        "id": "test_beginner",
                        "english": "Beginner",
                        "kikuyu": "Beginner",
                        "category": "vocabulary",
                        "difficulty": "beginner",
                        "source": {
                            "origin": "Test Origin"
                        }
                    },
                    {
                        "id": "test_intermediate",
                        "english": "Intermediate",
                        "kikuyu": "Intermediate",
                        "category": "vocabulary",
                        "difficulty": "intermediate",
                        "source": {
                            "origin": "Test Origin"
                        }
                    },
                    {
                        "id": "test_advanced",
                        "english": "Advanced",
                        "kikuyu": "Advanced",
                        "category": "vocabulary",
                        "difficulty": "advanced",
                        "source": {
                            "origin": "Test Origin"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Mock legacy content not found
        whenever(assetManager.open("kikuyu-phrases.json")).thenThrow(IOException())

        // Mock curated content directories
        whenever(assetManager.list("curated-content")).thenReturn(arrayOf("vocabulary"))
        whenever(assetManager.list("curated-content/vocabulary")).thenReturn(arrayOf("difficulties.json"))

        // Mock file content
        val difficultiesStream = ByteArrayInputStream(curatedJson.toByteArray())
        whenever(assetManager.open("curated-content/vocabulary/difficulties.json")).thenReturn(difficultiesStream)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Test filtering by difficulty
        val beginnerEntries = curatedContentManager.getEntriesByDifficulty(DifficultyLevels.BEGINNER)
        assert(beginnerEntries.size == 1)
        assert(beginnerEntries[0].english == "Beginner")

        val intermediateEntries = curatedContentManager.getEntriesByDifficulty(DifficultyLevels.INTERMEDIATE)
        assert(intermediateEntries.size == 1)
        assert(intermediateEntries[0].english == "Intermediate")

        val advancedEntries = curatedContentManager.getEntriesByDifficulty(DifficultyLevels.ADVANCED)
        assert(advancedEntries.size == 1)
        assert(advancedEntries[0].english == "Advanced")
    }

    @Test
    fun testFilteringByCategoryAndDifficulty() {
        // Mock curated content with different categories and difficulties
        val curatedJson = """
            {
                "metadata": {
                    "schema_version": "1.0",
                    "created_date": "2025-10-07T00:00:00Z",
                    "curator": "Test Curator",
                    "source_files": ["test.txt"],
                    "total_entries": 4,
                    "description": "Test content"
                },
                "entries": [
                    {
                        "id": "vocab_beginner",
                        "english": "Vocab Beginner",
                        "kikuyu": "Vocab Beginner",
                        "category": "vocabulary",
                        "difficulty": "beginner",
                        "source": {
                            "origin": "Test Origin"
                        }
                    },
                    {
                        "id": "vocab_intermediate",
                        "english": "Vocab Intermediate",
                        "kikuyu": "Vocab Intermediate",
                        "category": "vocabulary",
                        "difficulty": "intermediate",
                        "source": {
                            "origin": "Test Origin"
                        }
                    },
                    {
                        "id": "phrases_beginner",
                        "english": "Phrases Beginner",
                        "kikuyu": "Phrases Beginner",
                        "category": "phrases",
                        "difficulty": "beginner",
                        "source": {
                            "origin": "Test Origin"
                        }
                    },
                    {
                        "id": "phrases_intermediate",
                        "english": "Phrases Intermediate",
                        "kikuyu": "Phrases Intermediate",
                        "category": "phrases",
                        "difficulty": "intermediate",
                        "source": {
                            "origin": "Test Origin"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Mock legacy content not found
        whenever(assetManager.open("kikuyu-phrases.json")).thenThrow(IOException())

        // Mock curated content directories
        whenever(assetManager.list("curated-content")).thenReturn(arrayOf("test"))
        whenever(assetManager.list("curated-content/test")).thenReturn(arrayOf("combined.json"))

        // Mock file content
        val combinedStream = ByteArrayInputStream(curatedJson.toByteArray())
        whenever(assetManager.open("curated-content/test/combined.json")).thenReturn(combinedStream)

        // Create CuratedContentManager
        val curatedContentManager = CuratedContentManager(context)

        // Test filtering by both category and difficulty
        val vocabBeginnerEntries = curatedContentManager.getEntriesByCategoryAndDifficulty(
            Categories.VOCABULARY, DifficultyLevels.BEGINNER
        )
        assert(vocabBeginnerEntries.size == 1)
        assert(vocabBeginnerEntries[0].english == "Vocab Beginner")

        val phrasesIntermediateEntries = curatedContentManager.getEntriesByCategoryAndDifficulty(
            Categories.PHRASES, DifficultyLevels.INTERMEDIATE
        )
        assert(phrasesIntermediateEntries.size == 1)
        assert(phrasesIntermediateEntries[0].english == "Phrases Intermediate")
    }
}