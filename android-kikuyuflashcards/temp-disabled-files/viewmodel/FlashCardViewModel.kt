package com.nkmathew.kikuyuflashcards.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.Phrase
import kotlinx.coroutines.launch

class FlashCardViewModel(
    private val context: Context, 
    private val categoryFilter: com.nkmathew.kikuyuflashcards.data.PhraseCategory? = null
) : ViewModel() {
    
    companion object {
        private const val PREFS_NAME = "FlashCardPrefs"
        private const val KEY_LAST_POSITION = "last_position"
    }

    private lateinit var flashCardManager: FlashCardManager
    private lateinit var sharedPreferences: SharedPreferences

    var currentPhrase = mutableStateOf<Phrase?>(null)
        private set
    
    var currentIndex = mutableStateOf(0)
        private set
    
    var totalPhrases = mutableStateOf(0)
        private set

    var isLoading = mutableStateOf(true)
        private set

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                flashCardManager = FlashCardManager(context)
                
                totalPhrases.value = flashCardManager.getTotalPhrases()
                
                // Load last position
                val lastPosition = sharedPreferences.getInt(KEY_LAST_POSITION, 0)
                if (lastPosition in 0 until totalPhrases.value) {
                    flashCardManager.setCurrentIndex(lastPosition)
                }
                
                updateCurrentPhrase()
                isLoading.value = false
            } catch (e: Exception) {
                // Handle error
                isLoading.value = false
            }
        }
    }

    fun navigateToNext() {
        if (::flashCardManager.isInitialized) {
            flashCardManager.getNextPhrase()
            updateCurrentPhrase()
            saveCurrentPosition()
        }
    }

    fun navigateToPrevious() {
        if (::flashCardManager.isInitialized) {
            flashCardManager.getPreviousPhrase()
            updateCurrentPhrase()
            saveCurrentPosition()
        }
    }

    fun navigateToRandom() {
        if (::flashCardManager.isInitialized) {
            flashCardManager.getRandomPhrase()
            updateCurrentPhrase()
            saveCurrentPosition()
        }
    }

    private fun updateCurrentPhrase() {
        if (::flashCardManager.isInitialized) {
            currentPhrase.value = flashCardManager.getCurrentPhrase()
            currentIndex.value = flashCardManager.getCurrentIndex()
        }
    }

    private fun saveCurrentPosition() {
        if (::sharedPreferences.isInitialized && ::flashCardManager.isInitialized) {
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_LAST_POSITION, flashCardManager.getCurrentIndex())
            editor.apply()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentPosition()
    }
}