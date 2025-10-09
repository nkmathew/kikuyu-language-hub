package com.nkmathew.kikuyuflashcards.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CuratedContentTest {

    private val gson: Gson = GsonBuilder().create()

    @Test
    fun testParseCuratedContent() {
        // Sample JSON content based on the new schema
        val jsonContent = """
            {
                "metadata": {
                    "schema_version": "1.0",
                    "created_date": "2025-10-07T00:00:00Z",
                    "last_updated": "2025-10-07T00:00:00Z",
                    "curator": "Test Curator",
                    "source_files": ["test1.txt", "test2.txt"],
                    "total_entries": 2,
                    "description": "Test description"
                },
                "entries": [
                    {
                        "id": "test_001",
                        "english": "Test English",
                        "kikuyu": "Test Kikuyu",
                        "category": "vocabulary",
                        "difficulty": "beginner",
                        "source": {
                            "origin": "Test Origin",
                            "created_date": "2025-10-07T00:00:00Z"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Parse JSON
        val curatedContent = gson.fromJson(jsonContent, CuratedContent::class.java)

        // Verify metadata
        assertNotNull(curatedContent.metadata)
        assertEquals("1.0", curatedContent.metadata.schemaVersion)
        assertEquals("2025-10-07T00:00:00Z", curatedContent.metadata.createdDate)
        assertEquals("2025-10-07T00:00:00Z", curatedContent.metadata.lastUpdated)
        assertEquals("Test Curator", curatedContent.metadata.curator)
        assertEquals(2, curatedContent.metadata.sourceFiles.size)
        assertEquals("test1.txt", curatedContent.metadata.sourceFiles[0])
        assertEquals("test2.txt", curatedContent.metadata.sourceFiles[1])
        assertEquals(2, curatedContent.metadata.totalEntries)
        assertEquals("Test description", curatedContent.metadata.description)

        // Verify entries
        assertEquals(1, curatedContent.entries.size)
        val entry = curatedContent.entries[0]
        assertEquals("test_001", entry.id)
        assertEquals("Test English", entry.english)
        assertEquals("Test Kikuyu", entry.kikuyu)
        assertEquals("vocabulary", entry.category)
        assertEquals("beginner", entry.difficulty)

        // Verify source
        assertNotNull(entry.source)
        assertEquals("Test Origin", entry.source.origin)
        assertEquals("2025-10-07T00:00:00Z", entry.source.createdDate)
    }

    @Test
    fun testParseWithFlashcardsArray() {
        // Sample JSON content using "flashcards" instead of "entries"
        val jsonContent = """
            {
                "metadata": {
                    "schema_version": "1.0",
                    "created_date": "2025-10-07T00:00:00Z",
                    "total_entries": 1,
                    "curator": "Test Curator",
                    "source_files": ["test.txt"]
                },
                "flashcards": [
                    {
                        "id": "test_001",
                        "english": "Test English",
                        "kikuyu": "Test Kikuyu",
                        "category": "vocabulary",
                        "difficulty": "beginner",
                        "source": {
                            "origin": "Test Origin"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Parse JSON
        val curatedContent = gson.fromJson(jsonContent, CuratedContent::class.java)

        // Verify it still works with "flashcards" field
        assertEquals(1, curatedContent.entries.size)
        assertEquals("test_001", curatedContent.entries[0].id)
    }

    @Test
    fun testParseComplexEntry() {
        // Sample JSON with nested objects and optional fields
        val jsonContent = """
            {
                "metadata": {
                    "schema_version": "1.0",
                    "created_date": "2025-10-07T00:00:00Z",
                    "total_entries": 1,
                    "curator": "Test Curator",
                    "source_files": ["test.txt"]
                },
                "entries": [
                    {
                        "id": "complex_001",
                        "english": "I drank tea and ate bread",
                        "kikuyu": "Ndanyua cai na ndarĩa mũgate",
                        "category": "conjugations",
                        "subcategory": "recent_past_tense",
                        "difficulty": "intermediate",
                        "context": "Recent past tense - actions that just happened",
                        "cultural_notes": "Tea is a common beverage in Kikuyu culture",
                        "grammatical_info": {
                            "part_of_speech": "verb",
                            "tense": "recent_past",
                            "subject_marker": "nda-",
                            "infinitive": "kũnyua (to drink), kũrĩa (to eat)"
                        },
                        "examples": [
                            {
                                "english": "I drank water",
                                "kikuyu": "Ndanyuire maĩ",
                                "context": "Similar pattern"
                            }
                        ],
                        "tags": ["past tense", "daily actions", "verbs"],
                        "quality": {
                            "verified": true,
                            "confidence_score": 4.5,
                            "source_quality": "native_speaker",
                            "reviewer": "Test Reviewer",
                            "review_date": "2025-10-07T00:00:00Z"
                        },
                        "source": {
                            "origin": "Test Origin",
                            "attribution": "Test Attribution",
                            "license": "educational_use",
                            "url": "https://example.com",
                            "created_date": "2025-10-01T00:00:00Z",
                            "last_updated": "2025-10-07T00:00:00Z"
                        }
                    }
                ]
            }
        """.trimIndent()

        // Parse JSON
        val curatedContent = gson.fromJson(jsonContent, CuratedContent::class.java)
        val entry = curatedContent.entries[0]

        // Verify complex fields
        assertEquals("complex_001", entry.id)
        assertEquals("recent_past_tense", entry.subcategory)
        assertEquals("Recent past tense - actions that just happened", entry.context)
        assertEquals("Tea is a common beverage in Kikuyu culture", entry.culturalNotes)

        // Verify grammatical info
        assertNotNull(entry.grammaticalInfo)
        assertEquals("verb", entry.grammaticalInfo?.partOfSpeech)
        assertEquals("recent_past", entry.grammaticalInfo?.tense)
        assertEquals("nda-", entry.grammaticalInfo?.subjectMarker)

        // Verify examples
        assertEquals(1, entry.examples.size)
        val example = entry.examples[0]
        assertEquals("I drank water", example.english)
        assertEquals("Ndanyuire maĩ", example.kikuyu)
        assertEquals("Similar pattern", example.context)

        // Verify tags
        assertEquals(3, entry.tags.size)
        assertEquals("past tense", entry.tags[0])

        // Verify quality
        assertNotNull(entry.quality)
        assertEquals(true, entry.quality?.verified)
        assertEquals(4.5f, entry.quality?.confidenceScore)
        assertEquals("native_speaker", entry.quality?.sourceQuality)
        assertEquals("Test Reviewer", entry.quality?.reviewer)

        // Verify source
        assertEquals("Test Origin", entry.source.origin)
        assertEquals("Test Attribution", entry.source.attribution)
        assertEquals("educational_use", entry.source.license)
        assertEquals("https://example.com", entry.source.url)
        assertEquals("2025-10-01T00:00:00Z", entry.source.createdDate)
        assertEquals("2025-10-07T00:00:00Z", entry.source.lastUpdated)
    }

    @Test
    fun testCategoriesUtility() {
        // Test category display names
        assertEquals("📚 Vocabulary", Categories.getCategoryDisplayName(Categories.VOCABULARY))
        assertEquals("🦉 Proverbs", Categories.getCategoryDisplayName(Categories.PROVERBS))
        assertEquals("📝 Grammar", Categories.getCategoryDisplayName(Categories.GRAMMAR))
        assertEquals("🔄 Conjugations", Categories.getCategoryDisplayName(Categories.CONJUGATIONS))
        assertEquals("🏮 Cultural", Categories.getCategoryDisplayName(Categories.CULTURAL))
        assertEquals("🔢 Numbers", Categories.getCategoryDisplayName(Categories.NUMBERS))
        assertEquals("💬 Phrases", Categories.getCategoryDisplayName(Categories.PHRASES))
        assertEquals("📚 General", Categories.getCategoryDisplayName("unknown"))

        // Test getAllCategories
        val allCategories = Categories.getAllCategories()
        assertEquals(7, allCategories.size)
        assert(allCategories.contains(Categories.VOCABULARY))
        assert(allCategories.contains(Categories.PHRASES))
    }

    @Test
    fun testDifficultyLevelsUtility() {
        // Test difficulty display names
        assertEquals("🟢 Beginner", DifficultyLevels.getDifficultyDisplayName(DifficultyLevels.BEGINNER))
        assertEquals("🟠 Intermediate", DifficultyLevels.getDifficultyDisplayName(DifficultyLevels.INTERMEDIATE))
        assertEquals("🔴 Advanced", DifficultyLevels.getDifficultyDisplayName(DifficultyLevels.ADVANCED))
        assertEquals("⚪ Unknown", DifficultyLevels.getDifficultyDisplayName("unknown"))

        // Test getAllDifficulties
        val allDifficulties = DifficultyLevels.getAllDifficulties()
        assertEquals(3, allDifficulties.size)
        assert(allDifficulties.contains(DifficultyLevels.BEGINNER))
        assert(allDifficulties.contains(DifficultyLevels.INTERMEDIATE))
        assert(allDifficulties.contains(DifficultyLevels.ADVANCED))
    }
}