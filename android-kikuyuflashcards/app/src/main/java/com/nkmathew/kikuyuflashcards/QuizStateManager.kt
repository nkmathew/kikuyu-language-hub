package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.google.gson.Gson

/**
 * Represents a complete quiz state for saving/restoring sessions
 */
data class QuizState(
    val quizId: String,  // Unique identifier for this quiz session
    val quizLength: Int,
    val currentQuestionIndex: Int,
    val score: Int,
    val questionIds: List<String>,  // IDs of pre-generated questions
    val answeredQuestions: List<AnsweredQuestion>,
    val timestamp: Long,
    val isCompleted: Boolean
)

/**
 * Represents a single answered question for history tracking
 */
data class AnsweredQuestion(
    val questionId: String,
    val questionText: String,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timestamp: Long
)

/**
 * Manages quiz state persistence for saving and resuming quiz sessions
 *
 * This manager handles:
 * - Saving current quiz progress
 * - Resuming interrupted quizzes
 * - Tracking quiz history
 */
class QuizStateManager(private val context: Context) {
    companion object {
        private const val TAG = "QuizStateManager"
        private const val PREFS_NAME = "quiz_state"
        private const val KEY_CURRENT_QUIZ = "current_quiz_state"
        private const val KEY_SAVED_QUIZZES = "saved_quiz_states"
        private const val MAX_SAVED_QUIZZES = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the current quiz state
     */
    fun saveQuizState(state: QuizState) {
        val json = gson.toJson(state)
        prefs.edit().putString(KEY_CURRENT_QUIZ, json).apply()
        Log.d(TAG, "Quiz state saved: Question ${state.currentQuestionIndex}/${state.quizLength}")
    }

    /**
     * Loads the current quiz state
     *
     * @return The saved QuizState or null if none exists or if there was an error loading
     */
    fun loadQuizState(): QuizState? {
        val json = prefs.getString(KEY_CURRENT_QUIZ, null) ?: return null
        return try {
            gson.fromJson(json, QuizState::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quiz state", e)
            null
        }
    }

    /**
     * Checks if there's a saved quiz in progress
     */
    fun hasSavedQuiz(): Boolean {
        val state = loadQuizState()
        return state != null && !state.isCompleted
    }

    /**
     * Clears the current quiz state
     */
    fun clearQuizState() {
        prefs.edit().remove(KEY_CURRENT_QUIZ).apply()
        Log.d(TAG, "Quiz state cleared")
    }

    /**
     * Archives a completed quiz to history
     */
    fun archiveCompletedQuiz(state: QuizState) {
        val savedQuizzesJson = prefs.getString(KEY_SAVED_QUIZZES, null)
        val savedQuizzes = if (savedQuizzesJson != null) {
            try {
                gson.fromJson(savedQuizzesJson, Array<QuizState>::class.java).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Add new quiz
        savedQuizzes.add(0, state)

        // Keep only recent quizzes
        if (savedQuizzes.size > MAX_SAVED_QUIZZES) {
            savedQuizzes.subList(MAX_SAVED_QUIZZES, savedQuizzes.size).clear()
        }

        // Save
        val json = gson.toJson(savedQuizzes)
        prefs.edit().putString(KEY_SAVED_QUIZZES, json).apply()

        // Clear current state
        clearQuizState()

        Log.d(TAG, "Quiz archived to history. Total quizzes in history: ${savedQuizzes.size}")
    }

    /**
     * Gets quiz history
     */
    fun getQuizHistory(): List<QuizState> {
        val json = prefs.getString(KEY_SAVED_QUIZZES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<QuizState>::class.java).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quiz history", e)
            emptyList()
        }
    }
}