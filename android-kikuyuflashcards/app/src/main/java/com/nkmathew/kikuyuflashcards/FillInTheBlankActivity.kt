package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
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
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.ProgressManager
import com.nkmathew.kikuyuflashcards.SoundManager
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.FailureTracker
import kotlin.random.Random

/**
 * FillInTheBlankActivity - Learning mode where users complete Kikuyu phrases with missing words
 * 
 * Features:
 * - Multiple difficulty levels (Easy, Medium, Hard)
 * - Blank words removed from Kikuyu phrases
 * - Multiple choice or text input options
 * - Progress tracking and scoring
 * - Hints and learning feedback
 */
class FillInTheBlankActivity : AppCompatActivity() {
    
    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker
    
    // UI Components
    private lateinit var questionText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var checkButton: Button
    private lateinit var hintButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    
    // Game state
    private var currentPhrase: FlashcardEntry? = null
    private var blankWord = ""
    private var fullPhrase = ""
    private var displayPhrase = ""
    private var score = 0
    private var totalQuestions = 0
    private var hintsUsed = 0
    private var difficulty = "medium" // easy, medium, hard
    // Always multiple choice now
    private var currentQuestionStartTime = 0L
    
    companion object {
        private const val TAG = "FillInTheBlankActivity"
        private const val MAX_HINTS = 3
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
            text = "📝 Fill in the Blank"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 32)
        }
        
        // Difficulty indicator
        val difficultyText = TextView(this).apply {
            text = "Difficulty: ${difficulty.uppercase()}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_secondary))
            setPadding(0, 0, 0, 24)
        }
        
        // Score display
        scoreText = TextView(this).apply {
            text = "Score: $score/$totalQuestions"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.success_green))
            setPadding(0, 0, 0, 16)
        }
        
        // Progress indicator
        progressText = TextView(this).apply {
            text = "Question 1 of ${flashCardManager.getTotalEntries()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 32)
        }
        
        // Question container with card styling
        val questionContainer = createQuestionCard()
        
        // Input/Options area
        optionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 24)
            gravity = Gravity.CENTER
        }
        
        // Buttons
        val buttonContainer = createButtonContainer()
        
        // Add all components to main container
        mainContainer.addView(titleText)
        mainContainer.addView(difficultyText)
        mainContainer.addView(scoreText)
        mainContainer.addView(progressText)
        mainContainer.addView(questionContainer)
        mainContainer.addView(optionsContainer)
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
            setTextColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_onSurface))
            gravity = Gravity.CENTER
            setLineSpacing(1.3f, 1f)
        }
        
        cardContainer.addView(questionText)
        return cardContainer
    }
    
    private fun createCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_surface))
            setStroke(3, ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_primary))
        }
    }
    
    private fun createButtonContainer(): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        
        // Text input option has been removed
        
        // Check button
        checkButton = Button(this).apply {
            text = "✓ Check Answer"
            textSize = 16f
            setOnClickListener {
                animateButtonPress(this@apply)
                // This button is not used anymore since we only have multiple choice
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
            visibility = View.GONE
        }
        
        // Hint button
        hintButton = Button(this).apply {
            text = "💡 Hint ($MAX_HINTS remaining)"
            textSize = 14f
            setOnClickListener {
                animateButtonPress(this@apply)
                showHint()
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.info_blue)
        }
        
        // Next button
        nextButton = Button(this).apply {
            text = "Next Question →"
            textSize = 16f
            setOnClickListener {
                animateButtonPress(this@apply)
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
                animateButtonPress(this@apply)
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
                animateButtonPress(this@apply)
                finish()
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_dark_outline)
        }
        
        // Add all buttons to container
        buttonContainer.addView(checkButton)
        buttonContainer.addView(hintButton)
        buttonContainer.addView(nextButton)
        buttonContainer.addView(problemWordsButton)
        buttonContainer.addView(backButton)
        
        return buttonContainer
    }
    
    // Input background removed - no longer needed
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@FillInTheBlankActivity, colorRes))
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
        checkButton.visibility = View.GONE
        nextButton.visibility = View.GONE
        hintButton.visibility = View.VISIBLE
        optionsContainer.removeAllViews()
        
        // Create fill-in-the-blank question
        createBlankQuestion()
        
        // Update progress
        val currentIndex = flashCardManager.getCurrentIndex()
        val totalPhrases = flashCardManager.getTotalEntries()
        progressText.text = "Question ${currentIndex + 1} of $totalPhrases"
        
        // Update hint button
        hintButton.text = "💡 Hint (${MAX_HINTS - hintsUsed} remaining)"
    }
    
    private fun createBlankQuestion() {
        val phrase = currentPhrase ?: return
        fullPhrase = phrase.kikuyu
        
        // Select word to blank based on difficulty
        blankWord = selectWordToBlank(fullPhrase)
        
        // Create display phrase with blank
        displayPhrase = fullPhrase.replace(blankWord, "_____")
        
        // All difficulty levels now use multiple choice
        questionText.text = "Complete the Kikuyu phrase:\n\nEnglish: \"${phrase.english}\"\n\nKikuyu: $displayPhrase"

        // Adjust the number of options based on difficulty
        createMultipleChoiceOptions()
    }
    
    private fun selectWordToBlank(phrase: String): String {
        val words = phrase.split(" ").filter { it.isNotEmpty() }
        
        return when (difficulty) {
            "easy" -> {
                // Select a shorter, common word (3-5 characters)
                val easyWords = words.filter { it.length in 3..5 }
                if (easyWords.isNotEmpty()) {
                    easyWords[Random.nextInt(easyWords.size)]
                } else {
                    words[Random.nextInt(words.size)]
                }
            }
            "medium" -> {
                // Select any word except very short or long ones
                val mediumWords = words.filter { it.length in 4..8 }
                if (mediumWords.isNotEmpty()) {
                    mediumWords[Random.nextInt(mediumWords.size)]
                } else {
                    words[Random.nextInt(words.size)]
                }
            }
            "hard" -> {
                // Select longer or more complex words
                val hardWords = words.filter { it.length >= 6 }
                if (hardWords.isNotEmpty()) {
                    hardWords[Random.nextInt(hardWords.size)]
                } else {
                    words[words.size / 2] // Middle word as fallback
                }
            }
            else -> words[Random.nextInt(words.size)]
        }
    }
    
    private fun createMultipleChoiceOptions() {
        // Generate options including correct answer
        val options = generateMultipleChoiceOptions()
        
        // Create option buttons
        for (option in options) {
            val optionButton = Button(this@FillInTheBlankActivity).apply {
                text = option
                textSize = 16f
                setOnClickListener {
                    animateButtonPress(this@apply)
                    checkMultipleChoiceAnswer(option)
                }
                setPadding(24, 16, 24, 16)
                setTextColor(Color.BLACK)
                background = createOptionButtonBackground()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }
            optionsContainer.addView(optionButton)
        }
    }
    
    private fun createOptionButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(Color.WHITE)
            setStroke(2, ContextCompat.getColor(this@FillInTheBlankActivity, R.color.md_theme_dark_secondary))
        }
    }
    
    private fun generateMultipleChoiceOptions(): List<String> {
        val options = mutableListOf<String>()
        options.add(blankWord) // Correct answer
        
        // Get other words from the phrase set for distractors
        val allPhrases = flashCardManager.getAllEntries()
        val allWords = allPhrases
            .flatMap { it.kikuyu.split(" ") }
            .filter { it.isNotEmpty() && it != blankWord }
            .distinct()
        
        // Add distractors based on difficulty
        val distractorCount = when (difficulty) {
            "easy" -> 3
            "medium" -> 4
            "hard" -> 5
            else -> 4
        }
        
        // Select distractors with similar length or starting letter for harder difficulty
        val distractors = if (difficulty == "hard") {
            // Hard mode: words with similar starting letter or length
            allWords
                .filter { it.startsWith(blankWord.firstOrNull()?.toString() ?: "") || 
                         kotlin.math.abs(it.length - blankWord.length) <= 2 }
                .shuffled()
                .take(distractorCount)
        } else {
            allWords.shuffled().take(distractorCount)
        }
        
        options.addAll(distractors)
        
        // Ensure we have enough options
        while (options.size < distractorCount + 1 && allWords.isNotEmpty()) {
            val remainingWords = allWords.filter { !options.contains(it) }
            if (remainingWords.isNotEmpty()) {
                options.add(remainingWords.random())
            } else {
                break
            }
        }
        
        return options.shuffled().take(distractorCount + 1)
    }
    
    // Text input methods removed

    private fun checkMultipleChoiceAnswer(selectedAnswer: String) {
        val isCorrect = selectedAnswer.equals(blankWord, ignoreCase = true)
        processAnswerResult(isCorrect, selectedAnswer)
    }
    
    private fun processAnswerResult(isCorrect: Boolean, userAnswer: String) {
        totalQuestions++
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        
        // Track with failure tracker
        val phrase = currentPhrase ?: return
        
        if (isCorrect) {
            score++

            // Record success with detailed information
            failureTracker.recordSuccess(
                entry = phrase,
                learningMode = FailureTracker.LearningMode.FILL_BLANK,
                responseTime = responseTime
            )

            showCorrectFeedback()
        } else {
            // More detailed failure classification
            val failureType = when {
                userAnswer.isEmpty() -> FailureTracker.FailureType.TIMEOUT_ERROR
                userAnswer.length >= blankWord.length - 1 && userAnswer.length <= blankWord.length + 1 ->
                    FailureTracker.FailureType.SPELLING_ERROR
                userAnswer.first() == blankWord.first() && userAnswer.last() == blankWord.last() ->
                    FailureTracker.FailureType.VOWEL_ERROR // Special case for vowel errors
                else -> FailureTracker.FailureType.FILL_BLANK_ERROR
            }

            // Record failure with enhanced tracking information
            failureTracker.recordFailure(
                entry = currentPhrase!!,
                failureType = failureType,
                learningMode = FailureTracker.LearningMode.FILL_BLANK,
                userAnswer = userAnswer,
                correctAnswer = blankWord,
                difficulty = difficulty,
                responseTime = responseTime
            )
            
            showIncorrectFeedback(userAnswer)
        }
        
        // Hide input/options and show result
        hideAllInputOptions()
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
    
    private fun hideAllInputOptions() {
        checkButton.visibility = View.GONE
        hintButton.visibility = View.GONE
        optionsContainer.removeAllViews()
    }
    
    private fun showCorrectFeedback() {
        questionText.text = "$displayPhrase\n\n✅ Correct! The missing word is: \"$blankWord\""
        questionText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
        
        // Animate success
        animateQuestionSuccess()
    }
    
    private fun showIncorrectFeedback(userAnswer: String) {
        // Build base feedback message
        var feedbackMessage = "$displayPhrase\n\n❌ Not quite. You said: \"$userAnswer\""

        // Try to find what the user's answer means in the vocabulary
        val userAnswerTranslation = findTranslationForWord(userAnswer)
        if (userAnswerTranslation != null) {
            feedbackMessage += "\n(\"$userAnswer\" means \"$userAnswerTranslation\")"
        }

        feedbackMessage += "\nThe correct answer is: \"$blankWord\""

        questionText.text = feedbackMessage
        questionText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_error))

        // Animate shake for incorrect answer
        animateQuestionShake()
    }

    /**
     * Search for a word in the vocabulary and return its translation
     * Returns the translation if found, null otherwise
     */
    private fun findTranslationForWord(word: String): String? {
        if (word.isBlank()) return null

        // Normalize the word for comparison (lowercase, trim)
        val normalizedWord = word.trim().lowercase()

        // Get all entries from the flashcard manager
        val allEntries = flashCardManager.getAllEntries()

        // Search through all entries to find matches
        for (entry in allEntries) {
            // Check if the word matches the Kikuyu text (case-insensitive)
            if (entry.kikuyu.lowercase().trim() == normalizedWord) {
                return entry.english
            }

            // Check if the word matches the English text (case-insensitive)
            if (entry.english.lowercase().trim() == normalizedWord) {
                return entry.kikuyu
            }

            // Also check if the word is part of a multi-word phrase
            // Split both English and Kikuyu into words and check for matches
            val kikuyuWords = entry.kikuyu.split(" ").map { it.trim().lowercase() }
            val englishWords = entry.english.split(" ").map { it.trim().lowercase() }

            if (kikuyuWords.contains(normalizedWord)) {
                // Found the word in Kikuyu, return the English translation
                return entry.english
            }

            if (englishWords.contains(normalizedWord)) {
                // Found the word in English, return the Kikuyu translation
                return entry.kikuyu
            }
        }

        return null
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
    
    private fun showHint() {
        if (hintsUsed >= MAX_HINTS) {
            Toast.makeText(this, "No more hints available!", Toast.LENGTH_SHORT).show()
            return
        }
        
        hintsUsed++
        hintButton.text = "💡 Hint (${MAX_HINTS - hintsUsed} remaining)"
        
        // Generate hint based on hints used
        val hint = when (hintsUsed) {
            1 -> "💡 Hint 1: The word starts with \"${blankWord.firstOrNull()?.uppercase() ?: "?"}\""
            2 -> "💡 Hint 2: The word has ${blankWord.length} letters"
            3 -> "💡 Hint 3: The word ends with \"${blankWord.takeLast(2).uppercase()}\""
            else -> "No more hints available"
        }
        
        Toast.makeText(this, hint, Toast.LENGTH_LONG).show()
        
        if (hintsUsed >= MAX_HINTS) {
            hintButton.isEnabled = false
        }
    }
    
    private fun animateButtonPress(button: Button) {
        val animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f)
        animator.duration = 100
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    private fun animateQuestionSuccess() {
        val scaleUp = ObjectAnimator.ofFloat(questionText, "scaleY", 1f, 1.1f, 1f)
        scaleUp.duration = 300
        scaleUp.interpolator = BounceInterpolator()
        scaleUp.start()
    }
    
    private fun animateQuestionShake() {
        val shake = ObjectAnimator.ofFloat(questionText, "translationX", 0f, -20f, 20f, -10f, 10f, 0f)
        shake.duration = 500
        shake.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
    }
}