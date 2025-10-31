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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
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
    private var score = 0
    private var streak = 0
    private var bestStreak = 0
    private var currentQuestion = 0
    private var totalQuestions = 10
    private var correctAnswers = 0
    private var gameActive = false
    private var difficulty = "medium" // easy, medium, hard

    // Current question data
    private var currentPhrase: FlashcardEntry? = null
    private var isCurrentTranslationCorrect = false
    private var currentQuestionStartTime = 0L

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

        // Get difficulty from intent if provided
        difficulty = intent.getStringExtra("difficulty") ?: "medium"

        setContentView(createLayout())
        setupGame()
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

        // Decide if this question will show a correct translation or an incorrect one
        isCurrentTranslationCorrect = Random.nextBoolean()

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

    private fun endGame() {
        gameActive = false

        // Save stats
        with(gamePreferences.edit()) {
            putInt(KEY_BEST_STREAK, maxOf(bestStreak, streak))
            putInt(KEY_GAMES_PLAYED, gamePreferences.getInt(KEY_GAMES_PLAYED, 0) + 1)
            putInt(KEY_HIGH_SCORE, maxOf(gamePreferences.getInt(KEY_HIGH_SCORE, 0), score))
            apply()
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
        progressManager.endSession()
    }
}