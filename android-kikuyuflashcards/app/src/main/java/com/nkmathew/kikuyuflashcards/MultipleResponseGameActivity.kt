package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.ProgressManager
import com.nkmathew.kikuyuflashcards.SoundManager
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.FailureTracker
import kotlin.random.Random

/**
 * MultipleResponseGameActivity - Gamified learning with multiple response types
 * 
 * Game Modes:
 * - Speed Match: Quick matching challenges
 * - Multiple Answers: Select all correct translations
 * - Word Association: Connect related terms
 * - Beat the Clock: Time-based challenges
 * - Streak Master: Maintain correct answer streaks
 */
class MultipleResponseGameActivity : AppCompatActivity() {
    
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var gamePreferences: SharedPreferences
    private lateinit var failureTracker: FailureTracker
    
    // UI Components
    private lateinit var gameTitleText: TextView
    private lateinit var scoreText: TextView
    private lateinit var streakText: TextView
    private lateinit var timerText: TextView
    private lateinit var levelText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var progressProgressBar: ProgressBar
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    
    // Game state
    private var gameMode = "speed_match" // speed_match, multiple_answers, word_association, beat_clock, streak_master
    private var difficulty = "medium" // easy, medium, hard
    private var score = 0
    private var streak = 0
    private var bestStreak = 0
    private var level = 1
    private var currentQuestion = 0
    private var totalQuestions = 10
    private var correctAnswers = 0
    private var gameActive = false
    private var gamePaused = false
    private var timeRemaining = 0
    private var maxTime = 60 // seconds
    
    // Failure tracking
    private var currentQuestionStartTime = 0L
    
    // Timer
    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    // Current question data
    private var currentPhrase: Phrase? = null
    private var correctOptions: MutableList<String> = mutableListOf()
    private var allOptions: MutableList<String> = mutableListOf()
    private var selectedAnswers: MutableSet<String> = mutableSetOf()
    
    companion object {
        private const val TAG = "MultipleResponseGameActivity"
        private const val TIMER_UPDATE_INTERVAL = 100L // 100ms for smooth updates
        private const val PREFS_NAME = "GamePreferences"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_TOTAL_SCORE = "total_score"
    }
    
    data class GameScore(
        var score: Int = 0,
        var streak: Int = 0,
        var level: Int = 1,
        var correctAnswers: Int = 0,
        var totalQuestions: Int = 0
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        
        // Initialize managers and preferences
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        failureTracker = FailureTracker(this)
        gamePreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Get game parameters from intent
        gameMode = intent.getStringExtra("gameMode") ?: "speed_match"
        difficulty = intent.getStringExtra("difficulty") ?: "medium"
        
        setContentView(createLayout())
        
        loadGameStats()
        setupGame()
    }
    
    private fun createLayout(): ScrollView {
        val rootLayout = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // Game header
        val headerContainer = createGameHeader()
        
        // Stats bar
        val statsContainer = createStatsBar()
        
        // Progress bar
        progressProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
            ).apply {
                setMargins(0, 16, 0, 24)
            }
        }
        
        // Question area
        val questionCard = createQuestionCard()
        
        // Options area
        optionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }
        
        // Button area
        val buttonContainer = createButtonContainer()
        
        // Add all components
        mainContainer.addView(headerContainer)
        mainContainer.addView(statsContainer)
        mainContainer.addView(progressProgressBar)
        mainContainer.addView(questionCard)
        mainContainer.addView(optionsContainer)
        mainContainer.addView(buttonContainer)
        
        rootLayout.addView(mainContainer)
        return rootLayout
    }
    
    private fun createGameHeader(): LinearLayout {
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        
        gameTitleText = TextView(this).apply {
            text = getGameTitle()
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
        }
        
        val subtitleText = TextView(this).apply {
            text = "Difficulty: ${difficulty.uppercase()} | Mode: ${gameMode.replace("_", " ").uppercase()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.text_secondary))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        
        headerContainer.addView(gameTitleText)
        headerContainer.addView(subtitleText)
        
        return headerContainer
    }
    
    private fun createStatsBar(): LinearLayout {
        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            background = createStatsBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Score
        scoreText = TextView(this).apply {
            text = "🏆 Score: $score"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.success_green))
            setPadding(12, 8, 12, 8)
        }
        
        // Streak
        streakText = TextView(this).apply {
            text = "🔥 Streak: $streak"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.streak_fire))
            setPadding(12, 8, 12, 8)
        }
        
        // Level
        levelText = TextView(this).apply {
            text = "⭐ Level: $level"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.achievement_gold))
            setPadding(12, 8, 12, 8)
        }
        
        // Timer (for timed modes)
        timerText = TextView(this).apply {
            text = "⏱️ ${maxTime}s"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.md_theme_light_secondary))
            setPadding(12, 8, 12, 8)
            visibility = if (gameMode == "beat_clock") View.VISIBLE else View.GONE
        }
        
        statsContainer.addView(scoreText)
        statsContainer.addView(streakText)
        statsContainer.addView(levelText)
        statsContainer.addView(timerText)
        
        return statsContainer
    }
    
    private fun createStatsBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.md_theme_light_surfaceVariant))
            setStroke(1, ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.md_theme_light_outline))
        }
    }
    
    private fun createQuestionCard(): LinearLayout {
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = createQuestionCardBackground()
            elevation = 8f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        questionText = TextView(this).apply {
            text = "Press START to begin the game!"
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setLineSpacing(1.4f, 1f)
        }
        
        cardContainer.addView(questionText)
        return cardContainer
    }
    
    private fun createQuestionCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.WHITE)
            setStroke(3, ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.quiz_purple))
        }
    }
    
    private fun createButtonContainer(): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        
        // Control buttons row
        val controlButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        
        startButton = Button(this).apply {
            text = "🎮 START GAME"
            textSize = 18f
            setOnClickListener { 
                animateButtonPress(this)
                startGame() 
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
        }
        
        pauseButton = Button(this).apply {
            text = "⏸️ PAUSE"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                togglePause() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.warning_orange)
            visibility = View.GONE
        }
        
        nextButton = Button(this).apply {
            text = "NEXT →"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                nextQuestion() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_secondary)
            visibility = View.GONE
        }
        
        controlButtonsContainer.addView(startButton)
        controlButtonsContainer.addView(pauseButton)
        controlButtonsContainer.addView(nextButton)
        
        // Back button
        backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                finishGame() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_outline)
        }
        
        buttonContainer.addView(controlButtonsContainer)
        buttonContainer.addView(backButton)
        
        return buttonContainer
    }
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@MultipleResponseGameActivity, colorRes))
        }
    }
    
    private fun getGameTitle(): String {
        return when (gameMode) {
            "speed_match" -> "⚡ Speed Match"
            "multiple_answers" -> "🎯 Multiple Answers"
            "word_association" -> "🔗 Word Association"
            "beat_clock" -> "⏰ Beat the Clock"
            "streak_master" -> "🔥 Streak Master"
            else -> "🎮 Multiple Response Game"
        }
    }
    
    private fun setupGame() {
        // Reset game state
        score = 0
        streak = 0
        level = 1
        currentQuestion = 0
        correctAnswers = 0
        gameActive = false
        gamePaused = false
        selectedAnswers.clear()
        
        // Set game parameters based on mode
        when (gameMode) {
            "speed_match" -> {
                totalQuestions = 10
                maxTime = 30
            }
            "multiple_answers" -> {
                totalQuestions = 8
                maxTime = 45
            }
            "word_association" -> {
                totalQuestions = 12
                maxTime = 60
            }
            "beat_clock" -> {
                totalQuestions = 15
                maxTime = 90
                timeRemaining = maxTime
            }
            "streak_master" -> {
                totalQuestions = 20 // No limit, play until mistake
                maxTime = 120
            }
            else -> {
                totalQuestions = 10
                maxTime = 60
            }
        }
        
        updateUI()
    }
    
    private fun startGame() {
        gameActive = true
        gamePaused = false
        
        // Update UI
        startButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        
        // Start with first question
        generateQuestion()
        
        // Start timer for timed modes
        if (gameMode == "beat_clock") {
            timeRemaining = maxTime
            startTimer()
        }
        
        soundManager.playButtonSound()
    }
    
    private fun generateQuestion() {
        if (currentQuestion >= totalQuestions && gameMode != "streak_master") {
            endGame()
            return
        }
        
        currentPhrase = flashCardManager.getRandomPhrase()
        if (currentPhrase == null) {
            Toast.makeText(this, "No phrases available!", Toast.LENGTH_SHORT).show()
            endGame()
            return
        }
        
        // Clear previous answers
        selectedAnswers.clear()
        optionsContainer.removeAllViews()
        
        // Generate question based on game mode
        when (gameMode) {
            "speed_match" -> generateSpeedMatchQuestion()
            "multiple_answers" -> generateMultipleAnswersQuestion()
            "word_association" -> generateWordAssociationQuestion()
            "beat_clock" -> generateBeatClockQuestion()
            "streak_master" -> generateStreakMasterQuestion()
            else -> generateSpeedMatchQuestion()
        }
        
        currentQuestion++
        updateProgressBar()
    }
    
    private fun generateSpeedMatchQuestion() {
        val phrase = currentPhrase!!
        questionText.text = "Quick! Match the English to Kikuyu:\n\n\"${phrase.english}\""
        
        // Generate options with time pressure
        correctOptions.clear()
        allOptions.clear()
        
        correctOptions.add(phrase.kikuyu)
        allOptions.add(phrase.kikuyu)
        
        // Add 3 distractors
        val allPhrases = flashCardManager.getAllPhrases()
        val distractors = allPhrases
            .filter { it.kikuyu != phrase.kikuyu }
            .map { it.kikuyu }
            .shuffled()
            .take(3)
        
        allOptions.addAll(distractors)
        allOptions.shuffle()
        
        createOptionButtons(singleAnswer = true)
    }
    
    private fun generateMultipleAnswersQuestion() {
        val phrase = currentPhrase!!
        questionText.text = "Select ALL correct translations for:\n\n\"${phrase.english}\""
        
        correctOptions.clear()
        allOptions.clear()
        
        // Primary correct answer
        correctOptions.add(phrase.kikuyu)
        allOptions.add(phrase.kikuyu)
        
        // Add related correct answers (variations, synonyms)
        val allPhrases = flashCardManager.getAllPhrases()
        val relatedAnswers = allPhrases
            .filter { it.english.lowercase().contains(phrase.english.lowercase().substring(0, 3)) }
            .map { it.kikuyu }
            .filter { it != phrase.kikuyu }
            .take(1)
        
        correctOptions.addAll(relatedAnswers)
        allOptions.addAll(relatedAnswers)
        
        // Add distractors
        val distractors = allPhrases
            .filter { !correctOptions.contains(it.kikuyu) }
            .map { it.kikuyu }
            .shuffled()
            .take(4)
        
        allOptions.addAll(distractors)
        allOptions.shuffle()
        
        createOptionButtons(singleAnswer = false)
    }
    
    private fun generateWordAssociationQuestion() {
        val phrase = currentPhrase!!
        questionText.text = "Which words are related to \"${phrase.english}\"?"
        
        correctOptions.clear()
        allOptions.clear()
        
        // Correct answer is the Kikuyu translation
        correctOptions.add(phrase.kikuyu)
        allOptions.add(phrase.kikuyu)
        
        // Add related words (same category, similar meaning)
        val allPhrases = flashCardManager.getAllPhrases()
        val relatedWords = allPhrases
            .filter { it.category == phrase.category && it.kikuyu != phrase.kikuyu }
            .map { it.kikuyu }
            .shuffled()
            .take(2)
        
        correctOptions.addAll(relatedWords)
        allOptions.addAll(relatedWords)
        
        // Add unrelated distractors
        val distractors = allPhrases
            .filter { it.category != phrase.category && !correctOptions.contains(it.kikuyu) }
            .map { it.kikuyu }
            .shuffled()
            .take(3)
        
        allOptions.addAll(distractors)
        allOptions.shuffle()
        
        createOptionButtons(singleAnswer = false)
    }
    
    private fun generateBeatClockQuestion() {
        // Mix of different question types for time pressure
        val questionTypes = listOf("translation", "recognition", "matching")
        val questionType = questionTypes.random()
        
        when (questionType) {
            "translation" -> generateSpeedMatchQuestion()
            "recognition" -> {
                val phrase = currentPhrase!!
                questionText.text = "What does this mean?\n\n\"${phrase.kikuyu}\""
                
                correctOptions.clear()
                allOptions.clear()
                correctOptions.add(phrase.english)
                allOptions.add(phrase.english)
                
                val allPhrases = flashCardManager.getAllPhrases()
                val distractors = allPhrases
                    .filter { it.english != phrase.english }
                    .map { it.english }
                    .shuffled()
                    .take(3)
                
                allOptions.addAll(distractors)
                allOptions.shuffle()
                
                createOptionButtons(singleAnswer = true)
            }
            "matching" -> generateMultipleAnswersQuestion()
            else -> generateSpeedMatchQuestion()
        }
    }
    
    private fun generateStreakMasterQuestion() {
        // Progressive difficulty - questions get harder as streak increases
        val phrase = currentPhrase!!
        
        when {
            streak < 5 -> {
                // Easy: direct translation
                questionText.text = "Keep the streak alive!\n\n\"${phrase.english}\""
                generateSpeedMatchQuestion()
            }
            streak < 10 -> {
                // Medium: multiple answers
                questionText.text = "Streak: $streak! Select correct translations:\n\n\"${phrase.english}\""
                generateMultipleAnswersQuestion()
            }
            else -> {
                // Hard: word association
                questionText.text = "STREAK MASTER! Streak: $streak\nRelated words for \"${phrase.english}\"?"
                generateWordAssociationQuestion()
            }
        }
    }
    
    private fun createOptionButtons(singleAnswer: Boolean) {
        allOptions.forEachIndexed { index, option ->
            val optionButton = Button(this).apply {
                text = option
                textSize = 16f
                setOnClickListener { 
                    animateButtonPress(this)
                    handleOptionSelection(option, singleAnswer)
                }
                setPadding(20, 16, 20, 16)
                setTextColor(Color.BLACK)
                background = createOptionButtonBackground()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                tag = "option_$index"
            }
            optionsContainer.addView(optionButton)
        }
    }
    
    private fun createOptionButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(Color.WHITE)
            setStroke(2, ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.md_theme_light_secondary))
        }
    }
    
    private fun handleOptionSelection(selectedOption: String, singleAnswer: Boolean) {
        if (!gameActive || gamePaused) return
        
        if (singleAnswer) {
            // Single answer mode - immediate checking
            selectedAnswers.clear()
            selectedAnswers.add(selectedOption)
            checkSingleAnswer()
        } else {
            // Multiple answer mode - allow multiple selections
            if (selectedAnswers.contains(selectedOption)) {
                selectedAnswers.remove(selectedOption)
            } else {
                selectedAnswers.add(selectedOption)
            }
            
            updateOptionButtonStates()
            
            // Auto-submit when selected answers equal correct options count
            if (selectedAnswers.size == correctOptions.size) {
                checkMultipleAnswers()
            }
        }
    }
    
    private fun updateOptionButtonStates() {
        for (i in 0 until optionsContainer.childCount) {
            val button = optionsContainer.getChildAt(i) as Button
            val option = button.text.toString()
            
            if (selectedAnswers.contains(option)) {
                // Selected state
                button.background.mutate().setColorFilter(
                    ContextCompat.getColor(this, R.color.md_theme_light_primary),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                button.setTextColor(Color.WHITE)
            } else {
                // Unselected state
                button.background.mutate().clearColorFilter()
                button.setTextColor(Color.BLACK)
            }
        }
    }
    
    private fun checkSingleAnswer() {
        val isCorrect = selectedAnswers.firstOrNull() in correctOptions
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        val selectedAnswer = selectedAnswers.firstOrNull() ?: ""
        
        if (isCorrect) {
            correctAnswers++
            score += calculateScore()
            streak++
            if (streak > bestStreak) bestStreak = streak
            
            // Record success
            currentPhrase?.let { phrase ->
                if (correctOptions.contains(phrase.kikuyu)) {
                    val learningMode = getLearningModeFromGameMode()
                    failureTracker.recordSuccess(phrase, learningMode, responseTime)
                }
            }
            
            showCorrectFeedback()
        } else {
            streak = 0
            
            // Record failure
            currentPhrase?.let { phrase ->
                if (correctOptions.contains(phrase.kikuyu)) {
                    val learningMode = getLearningModeFromGameMode()
                    val failureType = determineFailureType(selectedAnswer, phrase.kikuyu)
                    
                    failureTracker.recordFailure(
                        phrase = phrase,
                        failureType = failureType,
                        learningMode = learningMode,
                        userAnswer = selectedAnswer,
                        correctAnswer = phrase.kikuyu,
                        difficulty = difficulty,
                        responseTime = responseTime
                    )
                }
            }
            
            showIncorrectFeedback()
        }
        
        updateUI()
        
        // Auto-advance after delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (gameActive) {
                nextQuestion()
            }
        }, 1500)
    }
    
    private fun checkMultipleAnswers() {
        val allCorrectSelected = correctOptions.all { it in selectedAnswers }
        val noIncorrectSelected = selectedAnswers.all { it in correctOptions }
        val isCorrect = allCorrectSelected && noIncorrectSelected
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        
        if (isCorrect) {
            correctAnswers++
            score += calculateScore() * 2 // Bonus for multiple answers
            streak++
            if (streak > bestStreak) bestStreak = streak
            
            // Record success for all correct options
            currentPhrase?.let { phrase ->
                if (correctOptions.contains(phrase.kikuyu)) {
                    val learningMode = getLearningModeFromGameMode()
                    failureTracker.recordSuccess(phrase, learningMode, responseTime)
                }
            }
            
            showCorrectFeedback()
        } else {
            streak = 0
            
            // Record failures for missed correct options and incorrect selections
            currentPhrase?.let { phrase ->
                val learningMode = getLearningModeFromGameMode()
                
                if (correctOptions.contains(phrase.kikuyu) && !selectedAnswers.contains(phrase.kikuyu)) {
                    // Failed to select a correct option
                    failureTracker.recordFailure(
                        phrase = phrase,
                        failureType = FailureTracker.FailureType.MULTIPLE_CHOICE_ERROR,
                        learningMode = learningMode,
                        userAnswer = "[NOT SELECTED]",
                        correctAnswer = phrase.kikuyu,
                        difficulty = difficulty,
                        responseTime = responseTime
                    )
                } else if (!correctOptions.contains(phrase.kikuyu) && selectedAnswers.contains(phrase.kikuyu)) {
                    // Selected an incorrect option
                    failureTracker.recordFailure(
                        phrase = phrase,
                        failureType = FailureTracker.FailureType.MULTIPLE_CHOICE_ERROR,
                        learningMode = learningMode,
                        userAnswer = phrase.kikuyu,
                        correctAnswer = "[INCORRECT SELECTION]",
                        difficulty = difficulty,
                        responseTime = responseTime
                    )
                }
            }
            
            showIncorrectFeedback()
        }
        
        updateUI()
        
        // Show correct answers
        highlightCorrectAnswers()
        
        // Show next button
        pauseButton.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
    }
    
    private fun calculateScore(): Int {
        val baseScore = 10
        val difficultyBonus = when (difficulty) {
            "easy" -> 0
            "medium" -> 5
            "hard" -> 10
            else -> 0
        }
        val streakBonus = (streak / 5) * 5
        val levelBonus = (level - 1) * 2
        
        return baseScore + difficultyBonus + streakBonus + levelBonus
    }
    
    private fun showCorrectFeedback() {
        questionText.text = "${questionText.text}\n\n✅ CORRECT! +${calculateScore()} points"
        questionText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
        
        // Animate success
        animateSuccess()
        
        soundManager.playButtonSound()
    }
    
    private fun showIncorrectFeedback() {
        val correctAnswer = correctOptions.firstOrNull()
        questionText.text = "${questionText.text}\n\n❌ Not quite! Correct: \"$correctAnswer\""
        questionText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error))
        
        // Animate shake
        animateError()
        
        // Handle streak master mode - game over on mistake
        if (gameMode == "streak_master") {
            Handler(Looper.getMainLooper()).postDelayed({
                endGame()
            }, 2000)
        }
    }
    
    private fun highlightCorrectAnswers() {
        for (i in 0 until optionsContainer.childCount) {
            val button = optionsContainer.getChildAt(i) as Button
            val option = button.text.toString()
            
            if (correctOptions.contains(option)) {
                if (selectedAnswers.contains(option)) {
                    // Correctly selected
                    button.background.mutate().setColorFilter(
                        ContextCompat.getColor(this, R.color.success_green),
                        android.graphics.PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    // Missed correct answer
                    button.background.mutate().setColorFilter(
                        ContextCompat.getColor(this, R.color.warning_orange),
                        android.graphics.PorterDuff.Mode.SRC_ATOP
                    )
                }
                button.setTextColor(Color.WHITE)
            } else if (selectedAnswers.contains(option)) {
                // Incorrectly selected
                button.background.mutate().setColorFilter(
                    ContextCompat.getColor(this, R.color.md_theme_light_error),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                button.setTextColor(Color.WHITE)
            }
        }
    }
    
    private fun nextQuestion() {
        if (!gameActive) return
        
        // Reset question text color
        questionText.setTextColor(Color.BLACK)
        
        // Reset buttons
        pauseButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        
        // Level progression
        if (correctAnswers > 0 && correctAnswers % 5 == 0) {
            level++
            showLevelUp()
        }
        
        currentQuestionStartTime = System.currentTimeMillis()
        generateQuestion()
    }
    
    private fun showLevelUp() {
        val levelUpText = TextView(this).apply {
            text = "🎉 LEVEL UP! Now Level $level! 🎉"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@MultipleResponseGameActivity, R.color.achievement_gold))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
        }
        
        optionsContainer.addView(levelUpText, 0)
        
        // Animate level up
        val scaleUp = ObjectAnimator.ofFloat(levelUpText, "scaleY", 0f, 1.2f, 1f)
        scaleUp.duration = 800
        scaleUp.interpolator = BounceInterpolator()
        scaleUp.start()
        
        // Remove after delay
        Handler(Looper.getMainLooper()).postDelayed({
            optionsContainer.removeView(levelUpText)
        }, 2000)
    }
    
    private fun togglePause() {
        gamePaused = !gamePaused
        
        if (gamePaused) {
            pauseButton.text = "▶️ RESUME"
            timerRunnable?.let { timerHandler.removeCallbacks(it) }
            questionText.text = "GAME PAUSED\n\nTap Resume to continue"
        } else {
            pauseButton.text = "⏸️ PAUSE"
            if (gameMode == "beat_clock") {
                startTimer()
            }
            generateQuestion()
        }
    }
    
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && gameMode == "beat_clock") {
                    timeRemaining--
                    updateTimerText()
                    
                    if (timeRemaining <= 0) {
                        endGame()
                    } else {
                        timerHandler.postDelayed(this, TIMER_UPDATE_INTERVAL)
                    }
                }
            }
        }
        timerHandler.post(timerRunnable!!)
    }
    
    private fun updateTimerText() {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        timerText.text = "⏱️ ${minutes}:${seconds.toString().padStart(2, '0')}"
        
        // Change color when time is running out
        if (timeRemaining <= 10) {
            timerText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error))
        } else if (timeRemaining <= 30) {
            timerText.setTextColor(ContextCompat.getColor(this, R.color.warning_orange))
        }
    }
    
    private fun updateProgressBar() {
        val progress = when (gameMode) {
            "streak_master" -> 100 // Always full for streak master
            else -> ((currentQuestion.toFloat() / totalQuestions) * 100).toInt()
        }
        progressProgressBar.progress = progress
    }
    
    private fun updateUI() {
        scoreText.text = "🏆 Score: $score"
        streakText.text = "🔥 Streak: $streak"
        levelText.text = "⭐ Level: $level"
        updateProgressBar()
    }
    
    private fun animateButtonPress(button: Button) {
        val animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f)
        animator.duration = 100
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    private fun animateSuccess() {
        val bounce = ObjectAnimator.ofFloat(questionText, "scaleY", 1f, 1.1f, 1f)
        bounce.duration = 300
        bounce.interpolator = BounceInterpolator()
        bounce.start()
    }
    
    private fun animateError() {
        val shake = ObjectAnimator.ofFloat(questionText, "translationX", 0f, -20f, 20f, -10f, 10f, 0f)
        shake.duration = 500
        shake.start()
    }
    
    private fun loadGameStats() {
        bestStreak = gamePreferences.getInt(KEY_BEST_STREAK, 0)
    }
    
    private fun saveGameStats() {
        val highScore = gamePreferences.getInt(KEY_HIGH_SCORE, 0)
        val gamesPlayed = gamePreferences.getInt(KEY_GAMES_PLAYED, 0)
        val totalScore = gamePreferences.getLong(KEY_TOTAL_SCORE, 0L)
        
        with(gamePreferences.edit()) {
            putInt(KEY_HIGH_SCORE, maxOf(highScore, score))
            putInt(KEY_BEST_STREAK, maxOf(bestStreak, streak))
            putInt(KEY_GAMES_PLAYED, gamesPlayed + 1)
            putLong(KEY_TOTAL_SCORE, totalScore + score)
            apply()
        }
    }
    
    private fun endGame() {
        gameActive = false
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        
        saveGameStats()
        
        // Show final results
        val accuracy = if (correctAnswers > 0) {
            ((correctAnswers.toFloat() / maxOf(currentQuestion, 1)) * 100).toInt()
        } else 0
        
        val resultsMessage = buildString {
            appendLine("🎮 GAME OVER!")
            appendLine()
            appendLine("Final Score: $score")
            appendLine("Best Streak: $bestStreak")
            appendLine("Level Reached: $level")
            appendLine("Accuracy: $accuracy%")
            appendLine("Correct Answers: $correctAnswers/${currentQuestion}")
            
            if (gameMode == "beat_clock") {
                appendLine("Time Bonus: ${timeRemaining * 2}")
            }
        }
        
        questionText.text = resultsMessage
        questionText.setTextColor(Color.BLACK)
        
        // Update buttons
        pauseButton.visibility = View.GONE
        nextButton.visibility = View.GONE
        startButton.visibility = View.VISIBLE
        startButton.text = "🔄 PLAY AGAIN"
        
        // Clear options
        optionsContainer.removeAllViews()
        
        Toast.makeText(this, "Game Complete! Final Score: $score", Toast.LENGTH_LONG).show()
    }
    
    private fun finishGame() {
        if (gameActive) {
            endGame()
        } else {
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        progressManager.endSession()
    }
    
    // Helper methods for failure tracking
    
    private fun getLearningModeFromGameMode(): FailureTracker.LearningMode {
        return when (gameMode) {
            "speed_match" -> FailureTracker.LearningMode.SPEED_MATCH
            "multiple_answers" -> FailureTracker.LearningMode.MULTIPLE_ANSWERS
            "word_association" -> FailureTracker.LearningMode.WORD_ASSOCIATION
            "beat_clock" -> FailureTracker.LearningMode.BEAT_CLOCK
            "streak_master" -> FailureTracker.LearningMode.STREAK_MASTER
            else -> FailureTracker.LearningMode.MULTIPLE_ANSWERS
        }
    }
    
    private fun determineFailureType(userAnswer: String, correctAnswer: String): FailureTracker.FailureType {
        return when {
            userAnswer.isEmpty() -> FailureTracker.FailureType.TIMEOUT_ERROR
            userAnswer.equals(correctAnswer, ignoreCase = true) -> FailureTracker.FailureType.RECOGNITION_ERROR
            userAnswer.length >= correctAnswer.length - 1 && userAnswer.length <= correctAnswer.length + 1 -> 
                FailureTracker.FailureType.SPELLING_ERROR
            else -> FailureTracker.FailureType.MULTIPLE_CHOICE_ERROR
        }
    }
}