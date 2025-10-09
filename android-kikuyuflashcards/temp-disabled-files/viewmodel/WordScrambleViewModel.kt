package com.nkmathew.kikuyuflashcards.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity
import com.nkmathew.kikuyuflashcards.data.repository.PhraseRepository
import com.nkmathew.kikuyuflashcards.ui.game.WordScrambleState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordScrambleViewModel(private val context: Context) : ViewModel() {
    
    private val repository = PhraseRepository(context)
    
    private val _gameState = MutableStateFlow(WordScrambleState())
    val gameState: StateFlow<WordScrambleState> = _gameState.asStateFlow()
    
    private var timerJob: Job? = null
    private var allPhrases: List<PhraseEntity> = emptyList()
    private var gameWords: List<PhraseEntity> = emptyList()
    
    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            allPhrases = repository.getAllPhrasesAsLegacy().map { phrase ->
                PhraseEntity(english = phrase.english, kikuyu = phrase.kikuyu, category = "GREETINGS")
            }
        }
    }
    
    fun startGame() {
        viewModelScope.launch {
            if (allPhrases.isEmpty()) {
                allPhrases = repository.getAllPhrasesAsLegacy().map { phrase ->
                    PhraseEntity(english = phrase.english, kikuyu = phrase.kikuyu, category = "GREETINGS")
                }
            }
            
            // Select words that are good for scrambling (single words, not too long)
            gameWords = allPhrases.filter { 
                it.kikuyu.length in 4..12 && !it.kikuyu.contains(" ") 
            }.shuffled().take(10)
            
            if (gameWords.isNotEmpty()) {
                val firstWord = gameWords.first()
                _gameState.value = WordScrambleState(
                    currentPhrase = firstWord,
                    scrambledWord = scrambleWord(firstWord.kikuyu),
                    userInput = "",
                    currentWordIndex = 0,
                    totalWords = gameWords.size,
                    score = 0,
                    timeRemaining = 60,
                    isGameActive = true,
                    isGameComplete = false,
                    showHint = false,
                    streak = 0,
                    hintsUsed = 0,
                    maxHints = 3
                )
                
                startTimer()
            }
        }
    }
    
    private fun scrambleWord(word: String): String {
        val chars = word.toMutableList()
        // Ensure the word is actually scrambled
        var scrambled: String
        do {
            chars.shuffle()
            scrambled = chars.joinToString("")
        } while (scrambled == word && word.length > 2)
        
        return scrambled
    }
    
    fun updateInput(input: String) {
        _gameState.value = _gameState.value.copy(userInput = input)
    }
    
    fun submitAnswer() {
        val currentState = _gameState.value
        val currentPhrase = currentState.currentPhrase ?: return
        
        val isCorrect = currentState.userInput.trim().equals(currentPhrase.kikuyu, ignoreCase = true)
        
        viewModelScope.launch {
            if (isCorrect) {
                repository.recordCorrectAnswer(currentPhrase.id)
                
                val newStreak = currentState.streak + 1
                val scoreIncrease = 100 + (newStreak * 20) + if (currentState.showHint) 0 else 50 // Bonus for no hint
                
                _gameState.value = currentState.copy(
                    score = currentState.score + scoreIncrease,
                    streak = newStreak
                )
                
                nextWord()
            } else {
                repository.recordIncorrectAnswer(currentPhrase.id)
                
                _gameState.value = currentState.copy(
                    streak = 0,
                    userInput = "" // Clear wrong answer
                )
            }
        }
    }
    
    fun useHint() {
        val currentState = _gameState.value
        if (currentState.hintsUsed >= currentState.maxHints || currentState.showHint) return
        
        _gameState.value = currentState.copy(
            showHint = true,
            hintsUsed = currentState.hintsUsed + 1
        )
    }
    
    fun skipWord() {
        val currentState = _gameState.value
        val currentPhrase = currentState.currentPhrase ?: return
        
        viewModelScope.launch {
            // Record as incorrect when skipped
            repository.recordIncorrectAnswer(currentPhrase.id)
            
            _gameState.value = currentState.copy(streak = 0)
            nextWord()
        }
    }
    
    private fun nextWord() {
        val currentState = _gameState.value
        val nextIndex = currentState.currentWordIndex + 1
        
        if (nextIndex >= currentState.totalWords) {
            // Game complete
            _gameState.value = currentState.copy(
                isGameActive = false,
                isGameComplete = true
            )
            stopTimer()
        } else {
            // Next word
            val nextPhrase = gameWords[nextIndex]
            _gameState.value = currentState.copy(
                currentPhrase = nextPhrase,
                scrambledWord = scrambleWord(nextPhrase.kikuyu),
                userInput = "",
                currentWordIndex = nextIndex,
                showHint = false
            )
        }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.timeRemaining > 0 && _gameState.value.isGameActive) {
                delay(1000)
                _gameState.value = _gameState.value.copy(
                    timeRemaining = _gameState.value.timeRemaining - 1
                )
            }
            
            // Time's up
            if (_gameState.value.timeRemaining <= 0) {
                _gameState.value = _gameState.value.copy(
                    isGameActive = false,
                    isGameComplete = true
                )
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}