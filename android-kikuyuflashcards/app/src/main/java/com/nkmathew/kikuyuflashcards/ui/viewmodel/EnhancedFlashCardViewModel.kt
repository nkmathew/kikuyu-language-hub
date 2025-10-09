package com.nkmathew.kikuyuflashcards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nkmathew.kikuyuflashcards.FlashCardManagerV2
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced flashcard screen
 */
class EnhancedFlashCardViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize FlashCardManager
    private val flashCardManager = FlashCardManagerV2(application.applicationContext)

    // UI State
    private val _uiState = MutableStateFlow(EnhancedFlashCardUiState())
    val uiState: StateFlow<EnhancedFlashCardUiState> = _uiState.asStateFlow()

    // Available filter options
    val availableCategories: List<String>
        get() = flashCardManager.getAvailableCategories()

    val availableDifficulties: List<String>
        get() = flashCardManager.getAvailableDifficulties()

    // Total entries
    val totalEntries: Int
        get() = flashCardManager.getTotalEntries()

    init {
        // Initialize state with the current entry
        updateCurrentEntry()
    }

    /**
     * Update the current entry in UI state
     */
    private fun updateCurrentEntry() {
        viewModelScope.launch {
            val entry = flashCardManager.getCurrentEntry()
            _uiState.update { currentState ->
                currentState.copy(
                    currentEntry = entry,
                    currentIndex = flashCardManager.getCurrentIndex(),
                    totalEntries = flashCardManager.getTotalEntries()
                )
            }
        }
    }

    /**
     * Set the current filter category
     */
    fun setCategory(category: String?) {
        viewModelScope.launch {
            flashCardManager.setCategory(category)
            _uiState.update { currentState ->
                currentState.copy(
                    selectedCategory = category,
                    currentIndex = flashCardManager.getCurrentIndex()
                )
            }
            updateCurrentEntry()
        }
    }

    /**
     * Set the current filter difficulty
     */
    fun setDifficulty(difficulty: String?) {
        viewModelScope.launch {
            flashCardManager.setDifficulty(difficulty)
            _uiState.update { currentState ->
                currentState.copy(
                    selectedDifficulty = difficulty,
                    currentIndex = flashCardManager.getCurrentIndex()
                )
            }
            updateCurrentEntry()
        }
    }

    /**
     * Move to the next entry
     */
    fun nextEntry() {
        viewModelScope.launch {
            flashCardManager.getNextEntry()
            _uiState.update { currentState ->
                currentState.copy(
                    isFlipped = false,
                    currentIndex = flashCardManager.getCurrentIndex()
                )
            }
            updateCurrentEntry()
        }
    }

    /**
     * Move to the previous entry
     */
    fun previousEntry() {
        viewModelScope.launch {
            flashCardManager.getPreviousEntry()
            _uiState.update { currentState ->
                currentState.copy(
                    isFlipped = false,
                    currentIndex = flashCardManager.getCurrentIndex()
                )
            }
            updateCurrentEntry()
        }
    }

    /**
     * Move to a random entry
     */
    fun randomEntry() {
        viewModelScope.launch {
            flashCardManager.getRandomEntry()
            _uiState.update { currentState ->
                currentState.copy(
                    isFlipped = false,
                    currentIndex = flashCardManager.getCurrentIndex()
                )
            }
            updateCurrentEntry()
        }
    }

    /**
     * Flip the current card
     */
    fun flipCard() {
        _uiState.update { currentState ->
            currentState.copy(isFlipped = !currentState.isFlipped)
        }
    }

    /**
     * Start a new learning session
     */
    fun startSession() {
        flashCardManager.startSession()
    }

    /**
     * Get the FlashCardManager instance (for use in the UI)
     */
    fun getFlashCardManager(): FlashCardManagerV2 {
        return flashCardManager
    }

    /**
     * UI state for the enhanced flashcard screen
     */
    data class EnhancedFlashCardUiState(
        val currentEntry: FlashcardEntry? = null,
        val isFlipped: Boolean = false,
        val currentIndex: Int = 0,
        val totalEntries: Int = 0,
        val selectedCategory: String? = null,
        val selectedDifficulty: String? = null
    )
}