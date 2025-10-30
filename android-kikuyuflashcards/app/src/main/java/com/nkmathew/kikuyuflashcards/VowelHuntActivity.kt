package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import kotlin.random.Random

/**
 * VowelHuntActivity - Learning mode where users identify the correct vowel in Kikuyu words
 *
 * Features:
 * - Focuses on learning correct vowel placement in Kikuyu words
 * - Multiple choice options for vowel selection
 * - Difficulty levels with progressively challenging words
 * - Progress tracking and scoring
 * - Problem word tracking for vowel-specific issues
 */
class VowelHuntActivity : AppCompatActivity() {

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker

    // UI Components
    private lateinit var questionText: TextView
    private lateinit var modifiedWordText: TextView
    private lateinit var vowelOptionsContainer: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    // Game state
    private var currentPhrase: FlashcardEntry? = null
    private var originalWord = ""
    private var modifiedWord = ""
    private var correctVowel = ' '
    private var correctPosition = -1
    private var score = 0
    private var totalQuestions = 0
    private var difficulty = "medium" // easy, medium, hard
    private var currentQuestionStartTime = 0L

    companion object {
        private const val TAG = "VowelHuntActivity"
        private val KIKUYU_VOWELS = listOf('a', 'e', 'i', 'o', 'u')
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        // Initialize managers
        flashCardManager = FlashCardManagerV2(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        failureTracker = FailureTracker(this)

        // Get difficulty from intent or default to medium
        difficulty = intent.getStringExtra("difficulty") ?: "medium"

        val layoutView = createLayout()
        setContentView(layoutView)

        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(layoutView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top + 48) // Add 48dp top margin
            insets
        }

        startNewQuestion()
    }

    private fun createLayout(): ScrollView {
        val rootLayout = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 32, 48) // Top padding will be set by insets
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Title
        val titleText = TextView(this).apply {
            text = "🔤 Vowel Hunt"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 32)
        }

        // Difficulty indicator
        val difficultyText = TextView(this).apply {
            text = "Difficulty: ${difficulty.uppercase()}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_secondary))
            setPadding(0, 0, 0, 24)
        }

        // Score display
        scoreText = TextView(this).apply {
            text = "Score: $score/$totalQuestions"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.success_green))
            setPadding(0, 0, 0, 16)
        }

        // Progress indicator
        progressText = TextView(this).apply {
            text = "Question 1 of ${flashCardManager.getTotalEntries()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 32)
        }

        // Question container with card styling
        val questionContainer = createQuestionCard()

        // Vowel options container
        vowelOptionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 24)
            gravity = Gravity.CENTER
        }

        // Buttons container
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        // Next button
        nextButton = Button(this).apply {
            text = "Next Question →"
            textSize = 16f
            setOnClickListener {
                animateButtonPress(this)
                startNewQuestion()
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_dark_secondary)
            visibility = View.GONE
        }

        // Practice Problem Words button
        val problemWordsButton = Button(this).apply {
            text = "🎯 Practice Problem Words"
            textSize = 14f
            setOnClickListener {
                animateButtonPress(this)
                startProblemWordsActivity()
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_dark_tertiary)
        }

        // Back button
        backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 14f
            setOnClickListener {
                animateButtonPress(this)
                finish()
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_dark_outline)
        }

        buttonContainer.addView(nextButton)
        buttonContainer.addView(problemWordsButton)
        buttonContainer.addView(backButton)

        // Add all components to main container
        mainContainer.addView(titleText)
        mainContainer.addView(difficultyText)
        mainContainer.addView(scoreText)
        mainContainer.addView(progressText)
        mainContainer.addView(questionContainer)
        mainContainer.addView(vowelOptionsContainer)
        mainContainer.addView(buttonContainer)

        rootLayout.addView(mainContainer)
        return rootLayout
    }

    private fun createQuestionCard(): LinearLayout {
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = createCardBackground()
            elevation = 8f
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        questionText = TextView(this).apply {
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_onSurface))
            gravity = Gravity.CENTER
            setLineSpacing(1.3f, 1f)
        }

        modifiedWordText = TextView(this).apply {
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_primary))
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }

        cardContainer.addView(questionText)
        cardContainer.addView(modifiedWordText)
        return cardContainer
    }

    private fun createCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_surface))
            setStroke(3, ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_primary))
        }
    }

    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@VowelHuntActivity, colorRes))
        }
    }

    private fun createVowelButton(vowel: Char): Button {
        return Button(this).apply {
            text = vowel.toString().uppercase()
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@VowelHuntActivity, R.color.md_theme_dark_onSecondaryContainer))
            background = createButtonBackground(R.color.md_theme_dark_secondaryContainer)

            // Set button size and margins
            val size = resources.displayMetrics.density * 60
            val layoutParams = LinearLayout.LayoutParams(size.toInt(), size.toInt())
            layoutParams.setMargins(16, 8, 16, 8)
            this.layoutParams = layoutParams

            // Set click listener
            setOnClickListener {
                animateButtonPress(this)
                checkAnswer(vowel)
            }
        }
    }

    private fun startNewQuestion() {
        // Get a random phrase
        currentPhrase = flashCardManager.getRandomEntry()
        if (currentPhrase == null) {
            Toast.makeText(this, "No phrases available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Track question start time
        currentQuestionStartTime = System.currentTimeMillis()

        // Reset UI state
        nextButton.visibility = View.GONE
        vowelOptionsContainer.removeAllViews()

        // Create vowel hunt question
        createVowelHuntQuestion()

        // Update progress
        val currentIndex = flashCardManager.getCurrentIndex()
        val totalPhrases = flashCardManager.getTotalEntries()
        progressText.text = "Question ${currentIndex + 1} of $totalPhrases"
    }

    private fun createVowelHuntQuestion() {
        val phrase = currentPhrase ?: return

        // Extract a word from the phrase that has vowels
        val words = phrase.kikuyu.split(" ").filter { word ->
            word.length >= 4 && word.any { it in KIKUYU_VOWELS }
        }

        if (words.isEmpty()) {
            // If no suitable words found, get a new phrase
            startNewQuestion()
            return
        }

        // Select a random word from the filtered list
        originalWord = words.random()

        // Find a vowel position to modify
        val vowelPositions = mutableListOf<Int>()
        originalWord.forEachIndexed { index, char ->
            if (char.lowercaseChar() in KIKUYU_VOWELS) {
                vowelPositions.add(index)
            }
        }

        if (vowelPositions.isEmpty()) {
            // If no vowels found, get a new phrase
            startNewQuestion()
            return
        }

        // Select a random vowel position based on difficulty
        correctPosition = when (difficulty) {
            "easy" -> vowelPositions.first() // Always the first vowel for easy
            "hard" -> vowelPositions.last() // Last vowel for hard
            else -> vowelPositions.random() // Random vowel for medium
        }

        correctVowel = originalWord[correctPosition].lowercaseChar()

        // Create modified word with placeholder for the vowel
        modifiedWord = originalWord.substring(0, correctPosition) + "_" + originalWord.substring(correctPosition + 1)

        // Display the question
        questionText.text = "Select the correct vowel:\n\nEnglish: \"${phrase.english}\""
        modifiedWordText.text = modifiedWord

        // Add vowel options
        for (vowel in KIKUYU_VOWELS) {
            val button = createVowelButton(vowel)
            vowelOptionsContainer.addView(button)
        }
    }

    private fun checkAnswer(selectedVowel: Char) {
        val isCorrect = selectedVowel.lowercaseChar() == correctVowel
        processAnswerResult(isCorrect, selectedVowel)
    }

    private fun processAnswerResult(isCorrect: Boolean, selectedVowel: Char) {
        totalQuestions++
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        val phrase = currentPhrase ?: return

        if (isCorrect) {
            score++

            // Record success
            failureTracker.recordSuccess(
                entry = phrase,
                learningMode = FailureTracker.LearningMode.VOWEL_HUNT,
                responseTime = responseTime
            )

            showCorrectFeedback()
        } else {
            // Record failure
            val userAnswer = originalWord.substring(0, correctPosition) + selectedVowel +
                             originalWord.substring(correctPosition + 1)

            failureTracker.recordFailure(
                entry = phrase,
                failureType = FailureTracker.FailureType.VOWEL_ERROR,
                learningMode = FailureTracker.LearningMode.VOWEL_HUNT,
                userAnswer = userAnswer,
                correctAnswer = originalWord,
                difficulty = difficulty,
                responseTime = responseTime
            )

            showIncorrectFeedback(selectedVowel)
        }

        // Disable vowel buttons
        for (i in 0 until vowelOptionsContainer.childCount) {
            vowelOptionsContainer.getChildAt(i).isEnabled = false
        }

        // Show next button
        nextButton.visibility = View.VISIBLE

        // Update score display
        scoreText.text = "Score: $score/$totalQuestions"

        // Play appropriate sound
        if (isCorrect) {
            soundManager.playButtonSound()
        }

        // Show problem words count after a few questions
        if (totalQuestions % 5 == 0) {
            val problemWordCount = getProblemWordsCount()
            if (problemWordCount > 0) {
                Toast.makeText(this,
                    "You have $problemWordCount words to practice. Tap '🎯 Practice Problem Words' to review.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showCorrectFeedback() {
        questionText.text = "✅ Correct!\n\nThe word is: $originalWord\n\nEnglish: \"${currentPhrase?.english}\""
        modifiedWordText.text = originalWord
        modifiedWordText.setTextColor(ContextCompat.getColor(this, R.color.success_green))

        // Animate success
        animateQuestionSuccess()
    }

    private fun showIncorrectFeedback(selectedVowel: Char) {
        val userWord = originalWord.substring(0, correctPosition) + selectedVowel +
                       originalWord.substring(correctPosition + 1)

        questionText.text = "❌ Not quite.\n\nYou selected: $userWord\n\nCorrect word: $originalWord\n\nEnglish: \"${currentPhrase?.english}\""
        modifiedWordText.text = userWord
        modifiedWordText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_error))

        // Animate shake for incorrect answer
        animateQuestionShake()
    }

    private fun animateQuestionSuccess() {
        val scaleX = ObjectAnimator.ofFloat(modifiedWordText, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(modifiedWordText, "scaleY", 1f, 1.2f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = BounceInterpolator()
            start()
        }
    }

    private fun animateQuestionShake() {
        val shake = ObjectAnimator.ofFloat(modifiedWordText, "translationX", 0f, 15f, -15f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 500
        shake.interpolator = AccelerateDecelerateInterpolator()
        shake.start()
    }

    private fun animateButtonPress(button: View) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f)
        scaleDown.duration = 100
        scaleDown.interpolator = AccelerateDecelerateInterpolator()

        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1f)
        scaleUp.duration = 100
        scaleUp.interpolator = AccelerateDecelerateInterpolator()

        val sequence = AnimatorSet()
        sequence.playSequentially(scaleDown, scaleUp)
        sequence.start()
    }

    /**
     * Navigate to ProblemWordsActivity to practice problem words
     */
    private fun startProblemWordsActivity() {
        // Show a summary toast before navigating
        val problemWordCount = getProblemWordsCount()
        if (problemWordCount > 0) {
            Toast.makeText(this,
                "You have $problemWordCount problem words to practice",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Navigate to problem words activity
        val intent = Intent(this, ProblemWordsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Get the count of problem words for this user
     */
    private fun getProblemWordsCount(): Int {
        return failureTracker.getProblemWords().size
    }
}