package com.nkmathew.kikuyuflashcards.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the full curated content JSON file structure
 */
data class CuratedContent(
    val metadata: Metadata,
    // Support both "entries" and "flashcards" for backward compatibility
    @SerializedName(value = "entries", alternate = ["flashcards"])
    val entries: List<FlashcardEntry> = emptyList()
)

/**
 * Metadata section of the curated content file
 */
data class Metadata(
    @SerializedName("schema_version")
    val schemaVersion: String,
    @SerializedName("created_date")
    val createdDate: String,
    @SerializedName("last_updated")
    val lastUpdated: String? = null,
    val curator: String,
    @SerializedName("source_files")
    val sourceFiles: List<String> = emptyList(),
    @SerializedName("total_entries")
    val totalEntries: Int,
    val description: String? = null
)

/**
 * Individual flashcard entry
 */
data class FlashcardEntry(
    val id: String,
    val english: String,
    val kikuyu: String,
    val category: String,
    val subcategory: String? = null,
    val difficulty: String,
    val context: String? = null,
    @SerializedName("cultural_notes")
    val culturalNotes: String? = null,
    val examples: List<ExampleSentence>? = null,
    @SerializedName("grammatical_info")
    val grammaticalInfo: GrammaticalInfo? = null,
    val tags: List<String> = emptyList(),
    val quality: QualityInfo? = null,
    val source: SourceInfo
) {
    // For backward compatibility with existing code that uses the 'text' property
    val text: String get() = english
}

/**
 * Source attribution information
 */
data class SourceInfo(
    val origin: String,
    val attribution: String? = null,
    val license: String? = null,
    val url: String? = null,
    @SerializedName("created_date")
    val createdDate: String? = null,
    @SerializedName("last_updated")
    val lastUpdated: String? = null
)

/**
 * Quality verification information
 */
data class QualityInfo(
    val verified: Boolean = false,
    @SerializedName("confidence_score")
    val confidenceScore: Float = 0.0f,
    @SerializedName("source_quality")
    val sourceQuality: String? = null,
    val reviewer: String? = null,
    @SerializedName("review_date")
    val reviewDate: String? = null
)

/**
 * Example sentences showing usage
 */
data class ExampleSentence(
    val english: String,
    val kikuyu: String,
    val context: String? = null
)

/**
 * Grammatical information about the entry
 */
data class GrammaticalInfo(
    @SerializedName("part_of_speech")
    val partOfSpeech: String? = null,
    @SerializedName("verb_class")
    val verbClass: String? = null,
    @SerializedName("noun_class")
    val nounClass: String? = null,
    val infinitive: String? = null,
    val tense: String? = null,
    @SerializedName("subject_marker")
    val subjectMarker: String? = null
) {
    // Allow access to custom properties via a map
    private val additionalProperties = mutableMapOf<String, Any>()

    fun getAdditionalProperty(key: String): Any? {
        return additionalProperties[key]
    }

    fun setAdditionalProperty(key: String, value: Any) {
        additionalProperties[key] = value
    }
}

/**
 * Pronunciation information
 */
data class PronunciationInfo(
    val ipa: String? = null,
    val simplified: String? = null,
    @SerializedName("audio_file")
    val audioFile: String? = null
)

/**
 * Category types as defined in the schema
 */
object Categories {
    const val VOCABULARY = "vocabulary"
    const val PROVERBS = "proverbs"
    const val GRAMMAR = "grammar"
    const val CONJUGATIONS = "conjugations"
    const val CULTURAL = "cultural"
    const val NUMBERS = "numbers"
    const val PHRASES = "phrases"

    fun getAllCategories(): List<String> {
        return listOf(
            VOCABULARY,
            PROVERBS,
            GRAMMAR,
            CONJUGATIONS,
            CULTURAL,
            NUMBERS,
            PHRASES
        )
    }

    fun getCategoryDisplayName(category: String): String {
        return when (category) {
            VOCABULARY -> "📚 Vocabulary"
            PROVERBS -> "🦉 Proverbs"
            GRAMMAR -> "📝 Grammar"
            CONJUGATIONS -> "🔄 Conjugations"
            CULTURAL -> "🏮 Cultural"
            NUMBERS -> "🔢 Numbers"
            PHRASES -> "💬 Phrases"
            else -> "📚 General"
        }
    }
}

/**
 * Difficulty levels as defined in the schema
 */
object DifficultyLevels {
    const val BEGINNER = "beginner"
    const val INTERMEDIATE = "intermediate"
    const val ADVANCED = "advanced"

    fun getAllDifficulties(): List<String> {
        return listOf(BEGINNER, INTERMEDIATE, ADVANCED)
    }

    fun getDifficultyDisplayName(difficulty: String): String {
        return when (difficulty) {
            BEGINNER -> "🟢 Beginner"
            INTERMEDIATE -> "🟠 Intermediate"
            ADVANCED -> "🔴 Advanced"
            else -> "⚪ Unknown"
        }
    }
}