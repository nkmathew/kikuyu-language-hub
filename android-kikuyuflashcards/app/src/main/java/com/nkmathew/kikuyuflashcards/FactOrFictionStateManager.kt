package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.google.gson.Gson

/**
 * Represents a complete Fact or Fiction game state for saving/restoring sessions
 */
data class FactOrFictionState(
    val gameId: String,  // Unique identifier for this game session
    val gameLength: Int,
    val currentQuestionIndex: Int,
    val score: Int,
    val streak: Int,
    val correctAnswers: Int,
    val questionIds: List<String>,  // IDs of phrases used in this session
    val questionCorrectness: List<Boolean>,  // Whether each translation was correct or incorrect
    val answeredQuestions: List<FactOrFictionAnsweredQuestion>,
    val timestamp: Long,
    val isCompleted: Boolean,
    val difficulty: String
)

/**
 * Represents a single answered question for history tracking
 */
data class FactOrFictionAnsweredQuestion(
    val questionId: String,
    val englishText: String,
    val kikuyuText: String,
    val wasTranslationCorrect: Boolean,
    val userAnsweredFact: Boolean,
    val isCorrect: Boolean,
    val timestamp: Long,
    val responseTime: Long
)

/**
 * Manages Fact or Fiction state persistence for saving and resuming game sessions
 *
 * This manager handles:
 * - Saving current game progress
 * - Resuming interrupted games
 * - Tracking game history
 */
class FactOrFictionStateManager(private val context: Context) {
    companion object {
        private const val TAG = "FactOrFictionStateMgr"
        private const val PREFS_NAME = "fact_or_fiction_state"
        private const val KEY_CURRENT_GAME = "current_game_state"
        private const val KEY_SAVED_GAMES = "saved_game_states"
        private const val MAX_SAVED_GAMES = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the current game state
     */
    fun saveGameState(state: FactOrFictionState) {
        val json = gson.toJson(state)
        prefs.edit().putString(KEY_CURRENT_GAME, json).apply()
        Log.d(TAG, "Game state saved: Question ${state.currentQuestionIndex}/${state.gameLength}")
    }

    /**
     * Loads the current game state
     *
     * @return The saved FactOrFictionState or null if none exists or if there was an error loading
     */
    fun loadGameState(): FactOrFictionState? {
        val json = prefs.getString(KEY_CURRENT_GAME, null) ?: return null
        return try {
            gson.fromJson(json, FactOrFictionState::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game state", e)
            null
        }
    }

    /**
     * Checks if there's a saved game in progress
     */
    fun hasSavedGame(): Boolean {
        val state = loadGameState()
        return state != null && !state.isCompleted
    }

    /**
     * Clears the current game state
     */
    fun clearGameState() {
        prefs.edit().remove(KEY_CURRENT_GAME).apply()
        Log.d(TAG, "Game state cleared")
    }

    /**
     * Archives a completed game to history
     */
    fun archiveCompletedGame(state: FactOrFictionState) {
        val savedGamesJson = prefs.getString(KEY_SAVED_GAMES, null)
        val savedGames = if (savedGamesJson != null) {
            try {
                gson.fromJson(savedGamesJson, Array<FactOrFictionState>::class.java).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Add new game
        savedGames.add(0, state)

        // Keep only recent games
        if (savedGames.size > MAX_SAVED_GAMES) {
            savedGames.subList(MAX_SAVED_GAMES, savedGames.size).clear()
        }

        // Save
        val json = gson.toJson(savedGames)
        prefs.edit().putString(KEY_SAVED_GAMES, json).apply()

        // Clear current state
        clearGameState()

        Log.d(TAG, "Game archived to history. Total games in history: ${savedGames.size}")
    }

    /**
     * Gets game history
     */
    fun getGameHistory(): List<FactOrFictionState> {
        val json = prefs.getString(KEY_SAVED_GAMES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<FactOrFictionState>::class.java).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game history", e)
            emptyList()
        }
    }
}