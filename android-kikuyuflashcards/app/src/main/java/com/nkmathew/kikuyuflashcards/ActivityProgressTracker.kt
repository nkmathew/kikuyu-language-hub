package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences

/**
 * Unified tracker for progress across all learning activities
 * This class provides methods for calculating and storing progress
 * for each learning activity type
 */
class ActivityProgressTracker(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "ActivityProgressTracker"

        // Progress keys for each activity type
        private const val KEY_FLASH_CARDS_PROGRESS = "flash_cards_progress"
        private const val KEY_FLASH_STYLE_PROGRESS = "flash_style_progress"
        private const val KEY_STUDY_LIST_PROGRESS = "study_list_progress"
        private const val KEY_QUIZ_PROGRESS = "quiz_progress"
        private const val KEY_FILL_BLANK_PROGRESS = "fill_blank_progress"
        private const val KEY_SENTENCE_UNSCRAMBLE_PROGRESS = "sentence_unscramble_progress"
        private const val KEY_VOWEL_HUNT_PROGRESS = "vowel_hunt_progress"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val flashCardManager: FlashCardManagerV2 by lazy { FlashCardManagerV2(context) }
    private val progressManager: ProgressManager by lazy { ProgressManager(context) }

    /**
     * Get progress for a specific activity type identified by its ID
     */
    fun getProgressForActivity(activityId: String): Float {
        return when (activityId) {
            "flashcard_style" -> getFlashCardStyleProgress()
            "study_list" -> getStudyListProgress()
            "flagged_translations" -> getFlaggedProgress()
            "quiz" -> getQuizProgress()
            "fill_blank" -> getFillBlankProgress()
            "sentence_unscramble" -> getSentenceUnscrambleProgress()
            "vowel_hunt" -> getVowelHuntProgress()
            else -> 0f
        }
    }

    /**
     * Calculate Flash Card Style progress
     */
    private fun getFlashCardStyleProgress(): Float {
        val totalCards = flashCardManager.getTotalEntries()
        if (totalCards == 0) return 0f

        val viewedCards = progressManager.getTotalCardsViewed()
        return (viewedCards.toFloat() / totalCards).coerceIn(0f, 1f)
    }

    /**
     * Calculate Study List progress
     */
    private fun getStudyListProgress(): Float {
        val totalCards = flashCardManager.getTotalEntries()
        if (totalCards == 0) return 0f

        val viewedCards = progressManager.getTotalCardsViewed()
        return (viewedCards.toFloat() / totalCards).coerceIn(0f, 1f)
    }

    /**
     * Calculate Flagged Translations progress
     */
    private fun getFlaggedProgress(): Float {
        // We'll consider this a completed activity if the user has viewed at least half the cards
        val totalCards = flashCardManager.getTotalEntries()
        if (totalCards == 0) return 0f

        val viewedCards = progressManager.getTotalCardsViewed()
        return (viewedCards.toFloat() / (totalCards / 2)).coerceIn(0f, 1f)
    }

    /**
     * Calculate Quiz progress
     */
    private fun getQuizProgress(): Float {
        val totalAnswered = progressManager.getQuizTotalAnswered()
        val totalCards = flashCardManager.getTotalEntries()
        if (totalCards == 0) return 0f

        return (totalAnswered.toFloat() / totalCards).coerceIn(0f, 1f)
    }

    /**
     * Calculate Fill in the Blank progress
     */
    private fun getFillBlankProgress(): Float {
        // Use a stored progress value or estimate from card views
        return (prefs.getFloat(KEY_FILL_BLANK_PROGRESS, 0f) +
                (progressManager.getTotalCardsViewed().toFloat() / flashCardManager.getTotalEntries())).coerceIn(0f, 1f) / 2f
    }

    /**
     * Calculate Sentence Unscramble progress
     */
    private fun getSentenceUnscrambleProgress(): Float {
        // Use a stored progress value or estimate from card views
        return (prefs.getFloat(KEY_SENTENCE_UNSCRAMBLE_PROGRESS, 0f) +
                (progressManager.getTotalCardsViewed().toFloat() / flashCardManager.getTotalEntries())).coerceIn(0f, 1f) / 2f
    }

    /**
     * Calculate Vowel Hunt progress
     */
    private fun getVowelHuntProgress(): Float {
        // Use a stored progress value or estimate from card views
        return (prefs.getFloat(KEY_VOWEL_HUNT_PROGRESS, 0f) +
                (progressManager.getTotalCardsViewed().toFloat() / flashCardManager.getTotalEntries())).coerceIn(0f, 1f) / 2f
    }

    /**
     * Update progress for a specific activity
     */
    fun updateProgress(activityId: String, progress: Float) {
        val key = when (activityId) {
            "flashcard_style" -> KEY_FLASH_STYLE_PROGRESS
            "study_list" -> KEY_STUDY_LIST_PROGRESS
            "quiz" -> KEY_QUIZ_PROGRESS
            "fill_blank" -> KEY_FILL_BLANK_PROGRESS
            "sentence_unscramble" -> KEY_SENTENCE_UNSCRAMBLE_PROGRESS
            "vowel_hunt" -> KEY_VOWEL_HUNT_PROGRESS
            else -> return
        }

        prefs.edit().putFloat(key, progress).apply()
    }

    /**
     * Get a formatted resume message for a specific activity
     */
    fun getResumeMessage(activityId: String): String {
        return when (activityId) {
            "flashcard_style" -> {
                val category = flashCardManager.getCurrentCategory()
                val categoryName = category?.let { flashCardManager.getCategoryDisplayName(it) } ?: "All Categories"
                val index = flashCardManager.getCurrentIndex() + 1
                val total = flashCardManager.getTotalEntries()
                "Continue $categoryName ($index/$total)"
            }
            "study_list" -> "Continue Study List"
            "quiz" -> {
                val answered = progressManager.getQuizTotalAnswered()
                val correct = progressManager.getQuizCorrectAnswers()
                "Continue Quiz ($correct/$answered correct)"
            }
            "fill_blank" -> "Continue Fill-in-the-Blank"
            "sentence_unscramble" -> "Continue Sentence Unscramble"
            "vowel_hunt" -> "Continue Vowel Hunt"
            else -> "Continue Learning"
        }
    }
}