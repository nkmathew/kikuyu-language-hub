package com.nkmathew.kikuyuflashcards.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity
import com.nkmathew.kikuyuflashcards.data.repository.PhraseRepository
import com.nkmathew.kikuyuflashcards.ui.game.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuizViewModel(private val context: Context) : ViewModel() {
    
    private val repository = PhraseRepository(context)
    
    private val _gameState = MutableStateFlow(QuizGameState())
    val gameState: StateFlow<QuizGameState> = _gameState.asStateFlow()
    
    private var timerJob: Job? = null
    private var allPhrases: List<PhraseEntity> = emptyList()
    private var currentQuestions: List<QuizQuestion> = emptyList()
    
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
            
            currentQuestions = generateQuestions(10)
            
            _gameState.value = QuizGameState(
                currentQuestion = currentQuestions.firstOrNull(),
                currentQuestionIndex = 0,
                totalQuestions = currentQuestions.size,
                score = 0,
                timeRemaining = 30,
                isGameActive = true,
                isGameComplete = false,
                selectedAnswerIndex = null,
                showCorrectAnswer = false,
                streak = 0,
                bestStreak = 0
            )
            
            startTimer()
        }
    }
    
    private fun generateQuestions(count: Int): List<QuizQuestion> {
        if (allPhrases.size < 4) return emptyList()
        
        val questions = mutableListOf<QuizQuestion>()
        val usedPhrases = mutableSetOf<PhraseEntity>()
        
        repeat(count) {
            val availablePhrases = allPhrases.filter { it !in usedPhrases }
            if (availablePhrases.isEmpty()) return@repeat
            
            val correctPhrase = availablePhrases.random()
            usedPhrases.add(correctPhrase)
            
            val questionType = QuizQuestionType.values().random()
            val wrongAnswers = allPhrases.filter { it != correctPhrase }.shuffled().take(3)
            
            val question = when (questionType) {
                QuizQuestionType.ENGLISH_TO_KIKUYU -> {
                    val options = (wrongAnswers.map { it.kikuyu } + correctPhrase.kikuyu).shuffled()
                    val correctIndex = options.indexOf(correctPhrase.kikuyu)
                    
                    QuizQuestion(
                        phrase = correctPhrase,
                        questionText = correctPhrase.english,
                        options = options,
                        correctAnswerIndex = correctIndex,
                        questionType = questionType
                    )
                }
                QuizQuestionType.KIKUYU_TO_ENGLISH -> {
                    val options = (wrongAnswers.map { it.english } + correctPhrase.english).shuffled()
                    val correctIndex = options.indexOf(correctPhrase.english)
                    
                    QuizQuestion(
                        phrase = correctPhrase,
                        questionText = correctPhrase.kikuyu,
                        options = options,
                        correctAnswerIndex = correctIndex,
                        questionType = questionType
                    )
                }
                QuizQuestionType.AUDIO_TO_TEXT -> {
                    // For now, same as English to Kikuyu
                    val options = (wrongAnswers.map { it.kikuyu } + correctPhrase.kikuyu).shuffled()
                    val correctIndex = options.indexOf(correctPhrase.kikuyu)
                    
                    QuizQuestion(
                        phrase = correctPhrase,
                        questionText = correctPhrase.english,
                        options = options,
                        correctAnswerIndex = correctIndex,
                        questionType = questionType
                    )
                }
            }
            
            questions.add(question)
        }
        
        return questions
    }
    
    fun selectAnswer(answerIndex: Int) {
        val currentState = _gameState.value
        if (!currentState.isGameActive || currentState.selectedAnswerIndex != null) return
        
        val isCorrect = answerIndex == currentState.currentQuestion?.correctAnswerIndex
        val newStreak = if (isCorrect) currentState.streak + 1 else 0
        val scoreIncrease = if (isCorrect) {
            100 + (newStreak * 10) // Base score + streak bonus
        } else 0
        
        _gameState.value = currentState.copy(
            selectedAnswerIndex = answerIndex,
            showCorrectAnswer = true,
            score = currentState.score + scoreIncrease,
            streak = newStreak,
            bestStreak = maxOf(currentState.bestStreak, newStreak)
        )
        
        // Record answer in repository
        viewModelScope.launch {
            currentState.currentQuestion?.let { question ->
                if (isCorrect) {
                    repository.recordCorrectAnswer(question.phrase.id)
                } else {
                    repository.recordIncorrectAnswer(question.phrase.id)
                }
            }
        }
        
        stopTimer()
    }
    
    fun nextQuestion() {
        val currentState = _gameState.value
        val nextIndex = currentState.currentQuestionIndex + 1
        
        if (nextIndex >= currentState.totalQuestions) {
            // Game complete
            _gameState.value = currentState.copy(
                isGameActive = false,
                isGameComplete = true
            )
        } else {
            // Next question
            _gameState.value = currentState.copy(
                currentQuestion = currentQuestions[nextIndex],
                currentQuestionIndex = nextIndex,
                selectedAnswerIndex = null,
                showCorrectAnswer = false,
                timeRemaining = 30
            )
            startTimer()
        }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.timeRemaining > 0 && _gameState.value.isGameActive && _gameState.value.selectedAnswerIndex == null) {
                delay(1000)
                _gameState.value = _gameState.value.copy(
                    timeRemaining = _gameState.value.timeRemaining - 1
                )
            }
            
            // Time's up - auto select wrong answer
            if (_gameState.value.timeRemaining <= 0 && _gameState.value.selectedAnswerIndex == null) {
                selectAnswer(-1) // Invalid index to mark as wrong
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