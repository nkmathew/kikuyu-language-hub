package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ProgressManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ProgressManager"
        private const val PREFS_NAME = "kikuyu_progress"
        
        // Preference keys
        private const val KEY_TOTAL_CARDS_VIEWED = "total_cards_viewed"
        private const val KEY_TOTAL_SWIPES = "total_swipes"
        private const val KEY_QUIZ_TOTAL_ANSWERED = "quiz_total_answered"
        private const val KEY_QUIZ_CORRECT_ANSWERS = "quiz_correct_answers"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_SESSION_COUNT = "session_count"
        private const val KEY_TOTAL_SESSION_TIME = "total_session_time"
        private const val KEY_LAST_SESSION_DATE = "last_session_date"
        
        // Achievement thresholds
        private const val ACHIEVEMENT_CARDS_VIEWED_BRONZE = 10
        private const val ACHIEVEMENT_CARDS_VIEWED_SILVER = 50
        private const val ACHIEVEMENT_CARDS_VIEWED_GOLD = 100
        private const val ACHIEVEMENT_QUIZ_SCORE_BRONZE = 5
        private const val ACHIEVEMENT_QUIZ_SCORE_SILVER = 15
        private const val ACHIEVEMENT_QUIZ_SCORE_GOLD = 25
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var sessionStartTime: Long = 0
    
    init {
        sessionStartTime = System.currentTimeMillis()
        incrementSessionCount()
    }
    
    // Card viewing progress
    fun incrementCardsViewed() {
        val current = prefs.getInt(KEY_TOTAL_CARDS_VIEWED, 0)
        prefs.edit().putInt(KEY_TOTAL_CARDS_VIEWED, current + 1).apply()
        Log.d(TAG, "Cards viewed: ${current + 1}")
        
        // Check achievements
        checkCardsViewedAchievements(current + 1)
    }
    
    fun incrementSwipes() {
        val current = prefs.getInt(KEY_TOTAL_SWIPES, 0)
        prefs.edit().putInt(KEY_TOTAL_SWIPES, current + 1).apply()
    }
    
    // Quiz progress
    fun recordQuizAnswer(isCorrect: Boolean) {
        val totalAnswered = prefs.getInt(KEY_QUIZ_TOTAL_ANSWERED, 0)
        val correctAnswers = prefs.getInt(KEY_QUIZ_CORRECT_ANSWERS, 0)
        
        prefs.edit()
            .putInt(KEY_QUIZ_TOTAL_ANSWERED, totalAnswered + 1)
            .putInt(KEY_QUIZ_CORRECT_ANSWERS, if (isCorrect) correctAnswers + 1 else correctAnswers)
            .apply()
        
        // Update streak
        updateStreak(isCorrect)
        
        // Check achievements
        if (isCorrect) {
            checkQuizAchievements(correctAnswers + 1)
        }
    }
    
    private fun updateStreak(isCorrect: Boolean) {
        val currentStreak = if (isCorrect) {
            prefs.getInt(KEY_CURRENT_STREAK, 0) + 1
        } else {
            0
        }
        
        val bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)
        val newBestStreak = maxOf(currentStreak, bestStreak)
        
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_BEST_STREAK, newBestStreak)
            .apply()
    }
    
    private fun incrementSessionCount() {
        val sessionCount = prefs.getInt(KEY_SESSION_COUNT, 0)
        prefs.edit().putInt(KEY_SESSION_COUNT, sessionCount + 1).apply()
    }
    
    // Session management
    fun endSession() {
        val sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000 // in seconds
        val totalSessionTime = prefs.getLong(KEY_TOTAL_SESSION_TIME, 0)
        
        prefs.edit()
            .putLong(KEY_TOTAL_SESSION_TIME, totalSessionTime + sessionDuration)
            .putLong(KEY_LAST_SESSION_DATE, System.currentTimeMillis())
            .apply()
        
        Log.d(TAG, "Session ended. Duration: ${sessionDuration}s")
    }
    
    // Statistics getters
    fun getTotalCardsViewed(): Int = prefs.getInt(KEY_TOTAL_CARDS_VIEWED, 0)
    fun getTotalSwipes(): Int = prefs.getInt(KEY_TOTAL_SWIPES, 0)
    fun getQuizTotalAnswered(): Int = prefs.getInt(KEY_QUIZ_TOTAL_ANSWERED, 0)
    fun getQuizCorrectAnswers(): Int = prefs.getInt(KEY_QUIZ_CORRECT_ANSWERS, 0)
    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT_STREAK, 0)
    fun getBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)
    fun getSessionCount(): Int = prefs.getInt(KEY_SESSION_COUNT, 0)
    fun getTotalSessionTime(): Long = prefs.getLong(KEY_TOTAL_SESSION_TIME, 0)
    fun getLastSessionDate(): Long = prefs.getLong(KEY_LAST_SESSION_DATE, 0)
    
    fun getQuizAccuracy(): Float {
        val total = getQuizTotalAnswered()
        return if (total > 0) {
            (getQuizCorrectAnswers().toFloat() / total) * 100
        } else {
            0f
        }
    }
    
    fun getAverageSessionTime(): Long {
        val sessionCount = getSessionCount()
        return if (sessionCount > 0) {
            getTotalSessionTime() / sessionCount
        } else {
            0L
        }
    }
    
    // Achievement checking
    private fun checkCardsViewedAchievements(cardsViewed: Int) {
        when {
            cardsViewed == ACHIEVEMENT_CARDS_VIEWED_BRONZE -> {
                Log.d(TAG, "Achievement: Bronze - Viewed $ACHIEVEMENT_CARDS_VIEWED_BRONZE cards!")
            }
            cardsViewed == ACHIEVEMENT_CARDS_VIEWED_SILVER -> {
                Log.d(TAG, "Achievement: Silver - Viewed $ACHIEVEMENT_CARDS_VIEWED_SILVER cards!")
            }
            cardsViewed == ACHIEVEMENT_CARDS_VIEWED_GOLD -> {
                Log.d(TAG, "Achievement: Gold - Viewed $ACHIEVEMENT_CARDS_VIEWED_GOLD cards!")
            }
        }
    }
    
    private fun checkQuizAchievements(correctAnswers: Int) {
        when {
            correctAnswers == ACHIEVEMENT_QUIZ_SCORE_BRONZE -> {
                Log.d(TAG, "Achievement: Bronze - $ACHIEVEMENT_QUIZ_SCORE_BRONZE correct quiz answers!")
            }
            correctAnswers == ACHIEVEMENT_QUIZ_SCORE_SILVER -> {
                Log.d(TAG, "Achievement: Silver - $ACHIEVEMENT_QUIZ_SCORE_SILVER correct quiz answers!")
            }
            correctAnswers == ACHIEVEMENT_QUIZ_SCORE_GOLD -> {
                Log.d(TAG, "Achievement: Gold - $ACHIEVEMENT_QUIZ_SCORE_GOLD correct quiz answers!")
            }
        }
    }
    
    // Reset functionality
    fun resetProgress() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Progress reset")
    }
    
    // Progress data class for UI
    data class ProgressStats(
        val totalCardsViewed: Int,
        val totalSwipes: Int,
        val quizTotalAnswered: Int,
        val quizCorrectAnswers: Int,
        val quizAccuracy: Float,
        val currentStreak: Int,
        val bestStreak: Int,
        val sessionCount: Int,
        val totalSessionTime: Long,
        val averageSessionTime: Long,
        val lastSessionDate: Long
    )
    
    fun getProgressStats(): ProgressStats {
        return ProgressStats(
            totalCardsViewed = getTotalCardsViewed(),
            totalSwipes = getTotalSwipes(),
            quizTotalAnswered = getQuizTotalAnswered(),
            quizCorrectAnswers = getQuizCorrectAnswers(),
            quizAccuracy = getQuizAccuracy(),
            currentStreak = getCurrentStreak(),
            bestStreak = getBestStreak(),
            sessionCount = getSessionCount(),
            totalSessionTime = getTotalSessionTime(),
            averageSessionTime = getAverageSessionTime(),
            lastSessionDate = getLastSessionDate()
        )
    }
}