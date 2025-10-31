package com.nkmathew.kikuyuflashcards

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import java.util.UUID
import kotlin.random.Random

/**
 * FactOrFictionActivity - True/False game for checking translation accuracy
 *
 * Game Features:
 * - Present translation pairs (Kikuyu-English)
 * - User decides if the translation is accurate (Fact) or incorrect (Fiction)
 * - Score tracking and progress monitoring
 * - Analytics integration with FailureTracker
 * - Multiple difficulty levels affecting complexity of incorrect translations
 */
class FactOrFictionActivity : AppCompatActivity() {

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker
    private lateinit var gamePreferences: SharedPreferences
    private lateinit var configManager: FactOrFictionConfigManager
    private lateinit var stateManager: FactOrFictionStateManager

    // UI Components
    private lateinit var titleText: TextView
    private lateinit var scoreText: TextView
    private lateinit var streakText: TextView
    private lateinit var questionCard: LinearLayout
    private lateinit var questionText: TextView
    private lateinit var translationText: TextView
    private lateinit var factButton: Button
    private lateinit var fictionButton: Button
    private lateinit var feedbackText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var startButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    // Game state
    private var gameId = UUID.randomUUID().toString()
    private var score = 0
    private var streak = 0
    private var bestStreak = 0
    private var currentQuestion = 0
    private var totalQuestions = 10 // Will be updated from config dialog
    private var correctAnswers = 0
    private var gameActive = false
    private var difficulty = "medium" // easy, medium, hard

    // Current question data
    private var currentPhrase: FlashcardEntry? = null
    private var isCurrentTranslationCorrect = false
    private var currentQuestionStartTime = 0L

    // Game state for restoration
    private val questionIds = mutableListOf<String>()
    private val questionCorrectness = mutableListOf<Boolean>()
    private val answeredQuestions = mutableListOf<FactOrFictionAnsweredQuestion>()

    // Stats
    private var factCorrectCount = 0
    private var fictionCorrectCount = 0
    private var factMistakeCount = 0
    private var fictionMistakeCount = 0

    companion object {
        private const val TAG = "FactOrFictionActivity"
        private const val PREFS_NAME = "FactOrFictionPrefs"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_GAMES_PLAYED = "games_played"
    }

    private lateinit var activityProgressTracker: ActivityProgressTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        // Initialize managers
        flashCardManager = FlashCardManagerV2(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        failureTracker = FailureTracker(this)
        gamePreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        configManager = FactOrFictionConfigManager(this)
        stateManager = FactOrFictionStateManager(this)
        activityProgressTracker = ActivityProgressTracker(this)

        // Get difficulty from intent if provided
        difficulty = intent.getStringExtra("difficulty") ?: configManager.getDifficultyFilter()

        // Session is automatically started when ProgressManager is initialized

        // Check if there's a saved game to resume
        if (stateManager.hasSavedGame()) {
            showResumeGameDialog()
        } else {
            // Show configuration dialog before starting game
            showGameConfigDialog()
        }
    }

    private fun createLayout(): ScrollView {
        val rootScrollView = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Game header
        titleText = TextView(this).apply {
            text = "✓✗ Fact or Fiction"
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryColor))
            gravity = Gravity.CENTER
        }

        val subtitleText = TextView(this).apply {
            text = "Test your translation accuracy | Difficulty: ${difficulty.uppercase()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.textSecondaryColor))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }

        // Stats bar
        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.surfaceVariantColor),
                12f
            )
        }

        scoreText = TextView(this).apply {
            text = "🏆 Score: 0"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.successColor))
            setPadding(16, 8, 16, 8)
        }

        streakText = TextView(this).apply {
            text = "🔥 Streak: 0"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.streak_fire)) // Using existing color as it's not in ThemeColors
            setPadding(16, 8, 16, 8)
        }

        statsContainer.addView(scoreText)
        statsContainer.addView(streakText)

        // Progress bar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
            ).apply {
                setMargins(0, 16, 0, 24)
            }
        }

        // Question card
        questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            background = createRoundedBackground(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.cardBgColor), 16f)
        }

        questionText = TextView(this).apply {
            text = "Is this translation correct?"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.cardTextColor))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        translationText = TextView(this).apply {
            text = "Press START to begin"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.NORMAL)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.cardTextColor))
            gravity = Gravity.CENTER
        }

        // Feedback text
        feedbackText = TextView(this).apply {
            text = ""
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.cardTextColor))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
            visibility = View.GONE
        }

        questionCard.addView(questionText)
        questionCard.addView(translationText)
        questionCard.addView(feedbackText)

        // Buttons container
        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        factButton = Button(this).apply {
            text = "✓ FACT"
            textSize = 18f
            setPadding(32, 16, 32, 16)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonPrimaryTextColor))
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.successColor),
                24f
            )
            setOnClickListener {
                handleAnswerSelection(true)
                animateButtonPress(this)
            }
            visibility = View.GONE
        }

        // Space between buttons
        val buttonSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(48, 1)
        }

        fictionButton = Button(this).apply {
            text = "✗ FICTION"
            textSize = 18f
            setPadding(32, 16, 32, 16)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonPrimaryTextColor))
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.errorColor),
                24f
            )
            setOnClickListener {
                handleAnswerSelection(false)
                animateButtonPress(this)
            }
            visibility = View.GONE
        }

        buttonsContainer.addView(factButton)
        buttonsContainer.addView(buttonSpacer)
        buttonsContainer.addView(fictionButton)

        // Control buttons
        val controlButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        startButton = Button(this).apply {
            text = "🎮 START GAME"
            textSize = 18f
            setPadding(32, 16, 32, 16)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonPrimaryTextColor))
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.successColor),
                24f
            )
            setOnClickListener {
                startGame()
                animateButtonPress(this)
            }
        }

        nextButton = Button(this).apply {
            text = "NEXT →"
            textSize = 16f
            setPadding(32, 16, 32, 16)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonSecondaryTextColor))
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.secondaryColor),
                24f
            )
            setOnClickListener {
                nextQuestion()
                animateButtonPress(this)
            }
            visibility = View.GONE
        }

        backButton = Button(this).apply {
            text = "🏠 BACK TO HOME"
            textSize = 14f
            setPadding(24, 12, 24, 12)
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonTertiaryTextColor))
            background = createRoundedBackground(
                ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.outlineColor),
                24f
            )
            setOnClickListener {
                finish()
                animateButtonPress(this)
            }
        }

        controlButtonsContainer.addView(startButton)
        controlButtonsContainer.addView(nextButton)
        controlButtonsContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 16)
        })
        controlButtonsContainer.addView(backButton)

        // Add all components to main container
        mainContainer.addView(titleText)
        mainContainer.addView(subtitleText)
        mainContainer.addView(statsContainer)
        mainContainer.addView(progressBar)
        mainContainer.addView(questionCard)
        mainContainer.addView(buttonsContainer)
        mainContainer.addView(controlButtonsContainer)

        rootScrollView.addView(mainContainer)
        return rootScrollView
    }

    private fun createRoundedBackground(color: Int, cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
    }

    private fun setupGame() {
        // Reset game state
        score = 0
        streak = 0
        currentQuestion = 0
        totalQuestions = 10
        correctAnswers = 0
        gameActive = false

        // Reset stats
        factCorrectCount = 0
        fictionCorrectCount = 0
        factMistakeCount = 0
        fictionMistakeCount = 0

        // Load saved stats
        bestStreak = gamePreferences.getInt(KEY_BEST_STREAK, 0)

        // Set initial UI state
        updateUI()
    }

    private fun startGame() {
        gameActive = true

        // Update UI
        startButton.visibility = View.GONE
        factButton.visibility = View.VISIBLE
        fictionButton.visibility = View.VISIBLE

        // Start with first question
        generateQuestion()

        soundManager.playButtonSound()
    }

    private fun generateQuestion() {
        if (currentQuestion >= totalQuestions) {
            endGame()
            return
        }

        // Reset feedback
        feedbackText.visibility = View.GONE

        // Get a random entry from the flash card manager
        currentPhrase = flashCardManager.getRandomEntry()
        if (currentPhrase == null) {
            Toast.makeText(this, "No phrases available!", Toast.LENGTH_SHORT).show()
            endGame()
            return
        }

        // Track the phrase ID for state persistence
        currentPhrase?.let { phrase ->
            questionIds.add(phrase.id)
        }

        // Decide if this question will show a correct translation or an incorrect one
        isCurrentTranslationCorrect = Random.nextBoolean()

        // Track whether this translation is correct (for state persistence)
        questionCorrectness.add(isCurrentTranslationCorrect)

        if (isCurrentTranslationCorrect) {
            // Show correct translation
            showTranslationPair(currentPhrase!!.english, currentPhrase!!.kikuyu)
        } else {
            // Show incorrect translation
            val incorrectTranslation = generateIncorrectTranslation(currentPhrase!!)
            showTranslationPair(currentPhrase!!.english, incorrectTranslation)
        }

        // Record start time for response timing
        currentQuestionStartTime = System.currentTimeMillis()

        // Update progress
        currentQuestion++
        updateProgressBar()

        // Save game state after generating a new question
        saveGameState()
    }

    private fun generateIncorrectTranslation(phrase: FlashcardEntry): String {
        // TODO: Implement more sophisticated incorrect translation generation
        // This is a placeholder that will be enhanced in future commits

        // For now, just get another random phrase with a different translation
        val allPhrases = flashCardManager.getAllEntries()
            .filter { it.id != phrase.id }
            .filter { it.category == phrase.category } // Same category for plausibility

        return if (allPhrases.isNotEmpty()) {
            allPhrases.random().kikuyu
        } else {
            // Fallback if no other phrases are available
            phrase.kikuyu.reversed()
        }
    }

    private fun showTranslationPair(english: String, kikuyu: String) {
        translationText.text = "${english}\n=\n${kikuyu}"
    }

    private fun handleAnswerSelection(userSelectedFact: Boolean) {
        if (!gameActive) return

        val isCorrect = userSelectedFact == isCurrentTranslationCorrect
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime

        // Disable buttons to prevent multiple answers
        factButton.isEnabled = false
        fictionButton.isEnabled = false

        if (isCorrect) {
            handleCorrectAnswer(userSelectedFact)
        } else {
            handleIncorrectAnswer(userSelectedFact)
        }

        // Record state for answered question
        currentPhrase?.let { phrase ->
            val kikuyuText = if (isCurrentTranslationCorrect) {
                phrase.kikuyu
            } else {
                // Get the incorrect translation that was displayed
                val displayedTranslation = translationText.text.toString().split("\n=\n")[1]
                displayedTranslation
            }

            // Add answered question to our tracking list
            answeredQuestions.add(
                FactOrFictionAnsweredQuestion(
                    questionId = phrase.id,
                    englishText = phrase.english,
                    kikuyuText = kikuyuText,
                    wasTranslationCorrect = isCurrentTranslationCorrect,
                    userAnsweredFact = userSelectedFact,
                    isCorrect = isCorrect,
                    timestamp = System.currentTimeMillis(),
                    responseTime = responseTime
                )
            )

            // Save state after each answer
            saveGameState()
        }

        // Record analytics
        recordAnalytics(isCorrect, userSelectedFact, responseTime)

        // Show next button
        nextButton.visibility = View.VISIBLE

        // Auto advance after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (gameActive) {
                nextQuestion()
            }
        }, 2000)
    }

    private fun handleCorrectAnswer(userSelectedFact: Boolean) {
        correctAnswers++
        score += calculateScore()
        streak++
        if (streak > bestStreak) bestStreak = streak

        // Update specific stats
        if (userSelectedFact) {
            factCorrectCount++
        } else {
            fictionCorrectCount++
        }

        // Show feedback
        feedbackText.text = if (userSelectedFact) {
            "✓ Correct! The translation is accurate."
        } else {
            "✓ Correct! The translation is inaccurate.\n\nCorrect translation: ${currentPhrase?.kikuyu}"
        }
        feedbackText.setTextColor(ContextCompat.getColor(this, ThemeColors.successColor))
        feedbackText.visibility = View.VISIBLE

        // Play success sound and animation
        soundManager.playCorrectSound()
        animateSuccess()
    }

    private fun handleIncorrectAnswer(userSelectedFact: Boolean) {
        streak = 0

        // Update specific stats
        if (userSelectedFact) {
            factMistakeCount++
        } else {
            fictionMistakeCount++
        }

        // Show feedback
        feedbackText.text = if (userSelectedFact) {
            "✗ Incorrect. The translation is NOT accurate.\n\nCorrect translation: ${currentPhrase?.kikuyu}"
        } else {
            "✗ Incorrect. The translation IS accurate."
        }
        feedbackText.setTextColor(ContextCompat.getColor(this, ThemeColors.errorColor))
        feedbackText.visibility = View.VISIBLE

        // Play error sound and animation
        soundManager.playWrongSound()
        soundManager.playWrongAnswerVibration()
        animateError()
    }

    private fun recordAnalytics(isCorrect: Boolean, userSelectedFact: Boolean, responseTime: Long) {
        currentPhrase?.let { phrase ->
            if (isCorrect) {
                // Record success
                failureTracker.recordSuccess(
                    phrase,
                    FailureTracker.LearningMode.FACT_OR_FICTION,
                    responseTime
                )
            } else {
                // Record failure
                val failureType = FailureTracker.FailureType.TRUTH_EVALUATION_ERROR

                failureTracker.recordFailure(
                    entry = phrase,
                    failureType = failureType,
                    learningMode = FailureTracker.LearningMode.FACT_OR_FICTION,
                    userAnswer = if (userSelectedFact) "FACT" else "FICTION",
                    correctAnswer = if (isCurrentTranslationCorrect) "FACT" else "FICTION",
                    difficulty = difficulty,
                    responseTime = responseTime
                )
            }
        }
    }

    private fun nextQuestion() {
        if (!gameActive) return

        // Re-enable buttons for next question
        factButton.isEnabled = true
        fictionButton.isEnabled = true

        // Hide next button
        nextButton.visibility = View.GONE

        // Clear feedback
        feedbackText.visibility = View.GONE

        // Reset text color
        questionText.setTextColor(ContextCompat.getColor(this, ThemeColors.cardTextColor))
        translationText.setTextColor(ContextCompat.getColor(this, ThemeColors.cardTextColor))

        // Generate next question
        generateQuestion()

        // Update UI
        updateUI()
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

        return baseScore + difficultyBonus + streakBonus
    }

    private fun updateProgressBar() {
        val progress = ((currentQuestion.toFloat() / totalQuestions) * 100).toInt()
        progressBar.progress = progress
    }

    private fun updateUI() {
        scoreText.text = "🏆 Score: $score"
        streakText.text = "🔥 Streak: $streak"
        updateProgressBar()
    }

    /**
     * Shows a configuration dialog for game settings
     */
    private fun showGameConfigDialog() {
        // Create dialog container with Material 3 styling
        val dialogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_surfaceContainer))
            }
            elevation = 16f
        }

        // Dialog title with icon
        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val titleIcon = TextView(this).apply {
            text = "⚙️"
            textSize = 32f
            setPadding(0, 0, 12, 0)
        }

        val titleText = TextView(this).apply {
            text = "Game Configuration"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_onSurface))
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        titleContainer.addView(titleIcon)
        titleContainer.addView(titleText)

        // Game length section
        val lengthSectionTitle = TextView(this).apply {
            text = "Game Length"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 16)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        // Length options container
        val lengthOptionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        // Track selected length and difficulty
        var selectedLength = configManager.getGameLength()
        var selectedDifficulty = configManager.getDifficultyFilter()

        // Create length option buttons
        val lengthButtons = mutableListOf<Button>()
        FactOrFictionConfigManager.PRESET_LENGTHS.forEach { length ->
            val button = createOptionButton(
                text = "$length Questions",
                isSelected = length == selectedLength,
                onClick = {
                    selectedLength = length
                    // Update button states
                    lengthButtons.forEach { btn ->
                        val btnLength = btn.tag as Int
                        updateOptionButtonState(btn, btnLength == selectedLength)
                    }
                }
            ).apply {
                tag = length
            }
            lengthButtons.add(button)
            lengthOptionsContainer.addView(button)
        }

        // Difficulty section
        val difficultySectionTitle = TextView(this).apply {
            text = "Difficulty Level"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_primary))
            setPadding(0, 24, 0, 16)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        // Difficulty options container
        val difficultyOptionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        val difficultyOptions = listOf(
            "easy" to "Easy",
            "medium" to "Medium",
            "hard" to "Hard"
        )

        // Create difficulty option buttons
        val difficultyButtons = mutableListOf<Button>()
        difficultyOptions.forEach { (value, label) ->
            val button = createOptionButton(
                text = label,
                isSelected = value == selectedDifficulty,
                onClick = {
                    selectedDifficulty = value
                    // Update button states
                    difficultyButtons.forEach { btn ->
                        val btnValue = btn.tag as String
                        updateOptionButtonState(btn, btnValue == selectedDifficulty)
                    }
                }
            ).apply {
                tag = value
            }
            difficultyButtons.add(button)
            difficultyOptionsContainer.addView(button)
        }

        // Action buttons container
        val actionButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        // Cancel button
        val cancelButton = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                soundManager.playButtonSound()
                finish() // Return to previous screen
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonSecondaryTextColor))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonSecondaryBgColor))
            }
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 12, 0)
            }
        }

        // Start game button
        val startButton = Button(this).apply {
            text = "Start Game"
            setOnClickListener {
                soundManager.playButtonSound()

                // Save configuration
                configManager.setGameLength(selectedLength)
                configManager.setDifficultyFilter(selectedDifficulty)

                // Update game parameters
                totalQuestions = selectedLength
                difficulty = selectedDifficulty

                Log.d(TAG, "Starting game with length: $totalQuestions, difficulty: $difficulty")

                // Initialize UI and game
                setContentView(createLayout())
                setupGame()
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonPrimaryTextColor))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryColor),
                    ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryContainerColor)
                )
            ).apply {
                cornerRadius = 20f
            }
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(12, 0, 0, 0)
            }
        }

        actionButtonsContainer.addView(cancelButton)
        actionButtonsContainer.addView(startButton)

        // Add all sections to dialog container
        dialogContainer.addView(titleContainer)
        dialogContainer.addView(lengthSectionTitle)
        dialogContainer.addView(lengthOptionsContainer)
        dialogContainer.addView(difficultySectionTitle)
        dialogContainer.addView(difficultyOptionsContainer)
        dialogContainer.addView(actionButtonsContainer)

        // Create dialog overlay
        val dialogOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.overlay_color))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        dialogOverlay.addView(dialogContainer)

        // Show dialog with animation
        setContentView(dialogOverlay)

        // Animate dialog entry
        dialogContainer.alpha = 0f
        dialogContainer.scaleX = 0.8f
        dialogContainer.scaleY = 0.8f
        dialogContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
    }

    /**
     * Creates an option button for config dialog
     */
    private fun createOptionButton(text: String, isSelected: Boolean, onClick: () -> Unit): Button {
        val button = Button(this).apply {
            this.text = text
            setPadding(16, 12, 16, 12)
            setOnClickListener {
                onClick()
                soundManager.playButtonSound()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            textSize = 16f
        }

        // Set initial state
        updateOptionButtonState(button, isSelected)
        return button
    }

    /**
     * Updates the visual state of an option button
     */
    private fun updateOptionButtonState(button: Button, isSelected: Boolean) {
        if (isSelected) {
            // Selected state
            button.setTextColor(ContextCompat.getColor(this, ThemeColors.buttonPrimaryTextColor))
            button.background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryColor))
            }
            button.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        } else {
            // Unselected state
            button.setTextColor(ContextCompat.getColor(this, ThemeColors.textPrimaryColor))
            button.background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.surfaceVariantColor))
                setStroke(2, ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.outlineColor))
            }
            button.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
    }

    /**
     * Shows a dialog allowing user to resume a previous game or start new
     */
    private fun showResumeGameDialog() {
        val savedState = stateManager.loadGameState()
        if (savedState == null) {
            // No saved state found, show config dialog instead
            showGameConfigDialog()
            return
        }

        // Create dialog container with Material 3 styling
        val dialogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_surfaceContainer))
            }
            elevation = 16f
        }

        // Dialog title with icon
        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val titleIcon = TextView(this).apply {
            text = "⏸️"
            textSize = 32f
            setPadding(0, 0, 12, 0)
        }

        val titleText = TextView(this).apply {
            text = "Resume Game?"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_onSurface))
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        titleContainer.addView(titleIcon)
        titleContainer.addView(titleText)

        // Info text about saved game
        val infoText = TextView(this).apply {
            val progress = "${savedState.currentQuestionIndex + 1} / ${savedState.gameLength}"
            val scoreText = "${savedState.score} points"
            text = "You have an unfinished game:\n\nProgress: $progress\nScore: $scoreText\n\nWould you like to continue where you left off?"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.md_theme_dark_onSurfaceVariant))
            setPadding(0, 0, 0, 32)
            gravity = Gravity.CENTER
            setLineSpacing(8f, 1.2f)
        }

        // Action buttons container
        val actionButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        // New Game button
        val newGameButton = Button(this).apply {
            text = "New Game"
            setOnClickListener {
                soundManager.playButtonSound()
                // Clear saved state and show config dialog
                stateManager.clearGameState()
                showGameConfigDialog()
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonSecondaryTextColor))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonSecondaryBgColor))
            }
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 12, 0)
            }
        }

        // Resume button
        val resumeButton = Button(this).apply {
            text = "Resume Game"
            setOnClickListener {
                soundManager.playButtonSound()
                // Restore state and continue game
                restoreGameState(savedState)
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.buttonPrimaryTextColor))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryColor),
                    ContextCompat.getColor(this@FactOrFictionActivity, ThemeColors.primaryContainerColor)
                )
            ).apply {
                cornerRadius = 20f
            }
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(12, 0, 0, 0)
            }
        }

        actionButtonsContainer.addView(newGameButton)
        actionButtonsContainer.addView(resumeButton)

        // Add all views to dialog container
        dialogContainer.addView(titleContainer)
        dialogContainer.addView(infoText)
        dialogContainer.addView(actionButtonsContainer)

        // Create dialog overlay
        val dialogOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(ContextCompat.getColor(this@FactOrFictionActivity, R.color.overlay_color))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        dialogOverlay.addView(dialogContainer)

        // Show dialog with animation
        setContentView(dialogOverlay)

        // Animate dialog entry
        dialogContainer.alpha = 0f
        dialogContainer.scaleX = 0.8f
        dialogContainer.scaleY = 0.8f
        dialogContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
    }

    /**
     * Restores a saved game state
     */
    private fun restoreGameState(savedState: FactOrFictionState) {
        // Restore game parameters
        gameId = savedState.gameId
        totalQuestions = savedState.gameLength
        currentQuestion = savedState.currentQuestionIndex
        score = savedState.score
        streak = savedState.streak
        correctAnswers = savedState.correctAnswers
        difficulty = savedState.difficulty

        // Restore question history
        questionIds.clear()
        questionIds.addAll(savedState.questionIds)

        questionCorrectness.clear()
        questionCorrectness.addAll(savedState.questionCorrectness)

        answeredQuestions.clear()
        answeredQuestions.addAll(savedState.answeredQuestions)

        // Create UI and continue game
        setContentView(createLayout())
        setupGame()

        // Set game as active and continue with next question
        gameActive = true
        startButton.visibility = View.GONE
        factButton.visibility = View.VISIBLE
        fictionButton.visibility = View.VISIBLE

        // Update UI to reflect current state
        updateUI()
        updateProgressBar()

        // Generate the next question
        generateQuestion()
    }

    /**
     * Saves the current game state for later resumption
     */
    private fun saveGameState() {
        if (!gameActive || currentQuestion <= 0) return

        val state = FactOrFictionState(
            gameId = gameId,
            gameLength = totalQuestions,
            currentQuestionIndex = currentQuestion - 1, // Adjust because generateQuestion already incremented
            score = score,
            streak = streak,
            correctAnswers = correctAnswers,
            questionIds = questionIds.toList(),
            questionCorrectness = questionCorrectness.toList(),
            answeredQuestions = answeredQuestions.toList(),
            timestamp = System.currentTimeMillis(),
            isCompleted = currentQuestion >= totalQuestions,
            difficulty = difficulty
        )

        stateManager.saveGameState(state)
        Log.d(TAG, "Game state saved at question ${currentQuestion}/$totalQuestions")

        // Update progress for quick actions
        val progressPercentage = (currentQuestion.toFloat() / totalQuestions).coerceIn(0f, 1f)
        activityProgressTracker.updateProgress("fact_fiction", progressPercentage)
    }

    private fun endGame() {
        gameActive = false

        // Save stats
        with(gamePreferences.edit()) {
            putInt(KEY_BEST_STREAK, maxOf(bestStreak, streak))
            putInt(KEY_GAMES_PLAYED, gamePreferences.getInt(KEY_GAMES_PLAYED, 0) + 1)
            putInt(KEY_HIGH_SCORE, maxOf(gamePreferences.getInt(KEY_HIGH_SCORE, 0), score))
            apply()
        }

        // Mark game as completed and archive it
        if (currentQuestion > 0) {
            val state = FactOrFictionState(
                gameId = gameId,
                gameLength = totalQuestions,
                currentQuestionIndex = currentQuestion - 1,
                score = score,
                streak = streak,
                correctAnswers = correctAnswers,
                questionIds = questionIds.toList(),
                questionCorrectness = questionCorrectness.toList(),
                answeredQuestions = answeredQuestions.toList(),
                timestamp = System.currentTimeMillis(),
                isCompleted = true,
                difficulty = difficulty
            )
            stateManager.archiveCompletedGame(state)
        }

        // Calculate final stats
        val accuracy = if (totalQuestions > 0) {
            ((correctAnswers.toFloat() / totalQuestions) * 100).toInt()
        } else 0

        val factAccuracy = if (factCorrectCount + factMistakeCount > 0) {
            ((factCorrectCount.toFloat() / (factCorrectCount + factMistakeCount)) * 100).toInt()
        } else 0

        val fictionAccuracy = if (fictionCorrectCount + fictionMistakeCount > 0) {
            ((fictionCorrectCount.toFloat() / (fictionCorrectCount + fictionMistakeCount)) * 100).toInt()
        } else 0

        // Show results
        val resultsMessage = buildString {
            appendLine("🎮 GAME COMPLETE!")
            appendLine()
            appendLine("Final Score: $score")
            appendLine("Best Streak: $bestStreak")
            appendLine("Overall Accuracy: $accuracy%")
            appendLine()
            appendLine("FACT answers: $factCorrectCount correct / ${factCorrectCount + factMistakeCount} total")
            appendLine("FICTION answers: $fictionCorrectCount correct / ${fictionCorrectCount + fictionMistakeCount} total")
            appendLine()

            when {
                accuracy >= 80 -> appendLine("🌟 Excellent work! You're a translation master!")
                accuracy >= 60 -> appendLine("👍 Good job! Keep practicing to improve further.")
                else -> appendLine("📚 Keep practicing! You'll get better with time.")
            }
        }

        // Update UI with results
        questionText.text = "Game Results"
        translationText.text = resultsMessage
        feedbackText.visibility = View.GONE

        // Update buttons
        factButton.visibility = View.GONE
        fictionButton.visibility = View.GONE
        nextButton.visibility = View.GONE
        startButton.visibility = View.VISIBLE
        startButton.text = "🔄 PLAY AGAIN"

        // Show toast with score
        Toast.makeText(this, "Game Complete! Final Score: $score", Toast.LENGTH_LONG).show()
    }

    private fun animateButtonPress(button: View) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(scaleDown, scaleUp)
        animatorSet.duration = 150
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateSuccess() {
        val bounce = ObjectAnimator.ofFloat(questionCard, "scaleY", 1f, 1.05f, 1f)
        bounce.duration = 300
        bounce.interpolator = BounceInterpolator()
        bounce.start()
    }

    private fun animateError() {
        val shake = ObjectAnimator.ofFloat(questionCard, "translationX", 0f, -20f, 20f, -10f, 10f, 0f)
        shake.duration = 500
        shake.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Update the progress one last time to ensure it's saved
        if (gameActive && currentQuestion > 0) {
            val progressPercentage = (currentQuestion.toFloat() / totalQuestions).coerceIn(0f, 1f)
            activityProgressTracker.updateProgress("fact_fiction", progressPercentage)
        }

        // End the session
        progressManager.endSession()
    }
}