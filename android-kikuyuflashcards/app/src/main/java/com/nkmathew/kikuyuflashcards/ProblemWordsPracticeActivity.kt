package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.SoundManager
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.models.SourceInfo
import com.nkmathew.kikuyuflashcards.FailureTracker
import com.nkmathew.kikuyuflashcards.utils.ButtonStyleHelper

/**
 * ProblemWordsPracticeActivity - Focused practice session for problem words
 */
class ProblemWordsPracticeActivity : AppCompatActivity() {
    
    private lateinit var failureTracker: FailureTracker
    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    
    // UI Components
    private lateinit var titleText: TextView
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var backButton: LinearLayout
    
    // Practice state
    private var problemWords: List<FailureTracker.DifficultyWord> = emptyList()
    private var currentWordIndex = 0
    private var currentWord: FailureTracker.DifficultyWord? = null
    private var currentOptions: List<String> = emptyList()
    private var score = 0
    private var correctAnswers = 0
    private var totalAttempts = 0
    private var practiceActive = false
    private var currentQuestionStartTime = 0L
    private var isDarkTheme = true
    private var autoAdvanceHandler: android.os.Handler? = null
    private var autoAdvanceRunnable: Runnable? = null
    
    companion object {
        private const val TAG = "ProblemWordsPracticeActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        isDarkTheme = ThemeManager.isDarkTheme(this)

        // Initialize managers
        failureTracker = FailureTracker(this)
        flashCardManager = FlashCardManagerV2(this)
        soundManager = SoundManager(this)

        setContentView(createLayout())

        loadProblemWords()
    }
    
    private fun createLayout(): ScrollView {
        val rootLayout = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // Header
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        
        titleText = TextView(this).apply {
            text = "🎯 Problem Words Practice"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(if (isDarkTheme) ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
        }

        val subtitleText = TextView(this).apply {
            text = "Choose the correct translation from multiple choices"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.text_secondary))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 0)
        }
        
        headerContainer.addView(titleText)
        headerContainer.addView(subtitleText)
        
        // Progress section
        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            background = createProgressBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        scoreText = TextView(this).apply {
            text = "🏆 Score: 0"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#4CAF50") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.success_green))
            setPadding(8, 4, 8, 4)
        }

        progressText = TextView(this).apply {
            text = "📊 Progress: 0/0"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#BBBBBB") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_secondary))
            setPadding(8, 4, 8, 4)
        }
        
        progressContainer.addView(scoreText)
        progressContainer.addView(progressText)
        
        // Question area
        val questionCard = createQuestionCard()
        
        // Options area - replace input with multiple choice buttons
        val optionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            visibility = View.GONE
        }

        this.optionsContainer = optionsContainer
        
        // Buttons
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        
        // Removed checkButton - now using multiple choice options directly
        
        // Start button removed - practice begins automatically
        
        backButton = ButtonStyleHelper.createSecondaryButton(
            context = this,
            text = "🏠 Back to Problem Words",
            isDarkTheme = isDarkTheme
        ) { view ->
            animateButtonPress(view)
            finish()
        }
        
        buttonContainer.addView(backButton)
        
        mainContainer.addView(headerContainer)
        mainContainer.addView(progressContainer)
        mainContainer.addView(questionCard)
        mainContainer.addView(optionsContainer)
        mainContainer.addView(buttonContainer)
        
        rootLayout.addView(mainContainer)
        return rootLayout
    }
    
    private fun createProgressBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(if (isDarkTheme) Color.parseColor("#2D2D2D") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_surfaceVariant))
            setStroke(1, if (isDarkTheme) Color.parseColor("#444444") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_outline))
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
            text = "🎯 Problem Words Practice\n\nLoading your challenging words..."
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
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
            setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.WHITE)
            setStroke(3, if (isDarkTheme) Color.parseColor("#6200EE") else ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.quiz_purple))
        }
    }

    private fun createOptionButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(if (isDarkTheme) Color.parseColor("#2D2D2D") else Color.parseColor("#F5F5F5"))
            setStroke(2, if (isDarkTheme) Color.parseColor("#555555") else Color.parseColor("#CCCCCC"))
        }
    }
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, colorRes))
        }
    }

    private fun generateMultipleChoiceOptions(correctAnswer: String, allWords: List<FailureTracker.DifficultyWord>): List<String> {
        val options = mutableListOf<String>()
        options.add(correctAnswer) // Add correct answer first

        // Get other Kikuyu words as distractors (excluding the correct answer)
        val distractors = allWords
            .filter { it.kikuyuText != correctAnswer }
            .map { it.kikuyuText }
            .shuffled()
            .take(3) // Take 3 distractors from problem words
            .toMutableList()

        // If we don't have enough distractors from problem words, get additional words from FlashCardManager
        if (distractors.size < 3) {
            val additionalDistractorsList = flashCardManager.getAllEntries()
                .map { it.kikuyu }
                .filter { it != correctAnswer }
                .filter { !options.contains(it) } // Exclude already added options
                .shuffled()
                .take(3 - distractors.size) // Take remaining needed distractors

            distractors.addAll(additionalDistractorsList)
        }

        options.addAll(distractors)
        return options.shuffled() // Shuffle all options
    }

    private fun createOptionButton(optionText: String, isCorrect: Boolean): LinearLayout {
        return ButtonStyleHelper.createPrimaryButton(
            context = this,
            text = optionText,
            isDarkTheme = isDarkTheme
        ) { view ->
            if (!practiceActive) return@createPrimaryButton

            animateButtonPress(view)
            handleOptionSelected(optionText, isCorrect)
        }.apply {
            // Override text color for option buttons
            val textView = getChildAt(0) as TextView
            textView.setTextColor(Color.WHITE)

            // Add proper margins
            (layoutParams as LinearLayout.LayoutParams).setMargins(0, ButtonStyleHelper.STANDARD_MARGIN, 0, ButtonStyleHelper.STANDARD_MARGIN)
        }
    }
    
    private fun loadProblemWords() {
        problemWords = failureTracker.getWordsNeedingAttention(15)
        
        if (problemWords.isEmpty()) {
            // Fallback to any problem words
            problemWords = failureTracker.getProblemWords(10)
        }
        
        if (problemWords.isEmpty()) {
            questionText.text = "🎉 Great job!\n\nNo problem words found. You're doing amazing!"
            backButton.visibility = View.VISIBLE
        } else {
            // Start practice automatically
            startPractice()
        }

        updateProgressDisplay()
    }
    
    private fun startPractice() {
        if (problemWords.isEmpty()) {
            Toast.makeText(this, "No problem words available for practice", Toast.LENGTH_SHORT).show()
            return
        }

        practiceActive = true
        currentWordIndex = 0
        score = 0
        correctAnswers = 0
        totalAttempts = 0

        // Show practice UI
        optionsContainer.visibility = View.VISIBLE

        currentWord = problemWords[currentWordIndex]
        showNextQuestion()

        soundManager.playButtonSound()
    }

    // Start button finding methods removed - no longer needed
    
    private fun showNextQuestion() {
        if (currentWordIndex >= problemWords.size) {
            endPracticeSession()
            return
        }

        currentWord = problemWords[currentWordIndex]
        currentQuestionStartTime = System.currentTimeMillis()

        // Cancel any pending auto-advance
        cancelAutoAdvance()

        val word = currentWord!!
        questionText.text = "🧠 Choose the correct translation for:\n\n\"${word.englishText}\"\n\nYou've struggled with this word ${word.failureCount} times"
        questionText.setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)

        // Generate multiple choice options
        currentOptions = generateMultipleChoiceOptions(word.kikuyuText, problemWords)

        // Clear and populate options container
        optionsContainer.removeAllViews()
        currentOptions.forEach { option ->
            val isCorrect = option == word.kikuyuText
            val optionButton = createOptionButton(option, isCorrect)
            optionsContainer.addView(optionButton)
        }

        updateProgressDisplay()
    }
    
    private fun handleOptionSelected(selectedAnswer: String, isCorrect: Boolean) {
        val word = currentWord!!
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime

        totalAttempts++

        if (isCorrect) {
            correctAnswers++
            score += 10

            // Record success with failure tracker
            val entry = FlashcardEntry(
                id = word.phraseId,
                english = word.englishText,
                kikuyu = word.kikuyuText,
                category = word.category,
                difficulty = "medium",
                source = SourceInfo(origin = "Problem Words")
            )
            failureTracker.recordSuccess(entry, FailureTracker.LearningMode.FLASHCARD, responseTime)

            showCorrectFeedback()
        } else {
            // Record failure with failure tracker
            val entry = FlashcardEntry(
                id = word.phraseId,
                english = word.englishText,
                kikuyu = word.kikuyuText,
                category = word.category,
                difficulty = "medium",
                source = SourceInfo(origin = "Problem Words")
            )

            failureTracker.recordFailure(
                entry = entry,
                failureType = FailureTracker.FailureType.MULTIPLE_CHOICE_ERROR,
                learningMode = FailureTracker.LearningMode.FLASHCARD,
                userAnswer = selectedAnswer,
                correctAnswer = word.kikuyuText,
                responseTime = responseTime
            )

            showIncorrectFeedback(selectedAnswer, word.kikuyuText)
        }

        // Disable all option buttons and schedule auto-advance
        disableAllOptionButtons()
        scheduleAutoAdvance()

        updateProgressDisplay()
    }

    private fun disableAllOptionButtons() {
        for (i in 0 until optionsContainer.childCount) {
            val button = optionsContainer.getChildAt(i) as LinearLayout
            button.isClickable = false
            button.alpha = 0.6f
        }
    }
    
    private fun showCorrectFeedback() {
        val word = currentWord!!
        questionText.text = "✅ CORRECT!\n\n\"${word.englishText}\"\n${word.kikuyuText}\n\n+10 points!\n\n→ Next word in 2.5 seconds..."
        questionText.setTextColor(if (isDarkTheme) Color.parseColor("#4CAF50") else ContextCompat.getColor(this, R.color.success_green))

        animateSuccess()
        soundManager.playButtonSound()
    }

    private fun showIncorrectFeedback(userAnswer: String, correctAnswer: String) {
        val word = currentWord!!
        questionText.text = "❌ Not quite right.\n\n\"${word.englishText}\"\nYour choice: \"$userAnswer\"\nCorrect: \"$correctAnswer\"\n\nKeep practicing this word!\n\n→ Next word in 2.5 seconds..."
        questionText.setTextColor(if (isDarkTheme) Color.parseColor("#CF6679") else ContextCompat.getColor(this, R.color.md_theme_light_error))

        animateError()
    }
    
    /**
     * Schedule auto-advance to the next word after a delay
     */
    private fun scheduleAutoAdvance() {
        cancelAutoAdvance() // Cancel any existing auto-advance

        autoAdvanceHandler = android.os.Handler(android.os.Looper.getMainLooper())
        autoAdvanceRunnable = Runnable {
            currentWordIndex++

            // Reset question text color
            questionText.setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)

            if (currentWordIndex < problemWords.size) {
                // Refresh the problem words list to get updated data after correct answers
                refreshProblemWordsList()
                showNextQuestion()
            } else {
                endPracticeSession()
            }
        }

        // Auto-advance after 2.5 seconds (2500ms)
        autoAdvanceHandler?.postDelayed(autoAdvanceRunnable!!, 2500)
    }

    /**
     * Refresh the problem words list to get updated data after correct answers
     */
    private fun refreshProblemWordsList() {
        // Reload the problem words list using the same logic as initial load
        var updatedProblemWords = failureTracker.getWordsNeedingAttention(15)

        if (updatedProblemWords.isEmpty()) {
            // Fallback to any problem words (same as initial load)
            updatedProblemWords = failureTracker.getProblemWords(10)
        }

        if (updatedProblemWords.isEmpty()) {
            // No more problem words, end the session
            endPracticeSession()
            return
        }

        // Simply replace the problem words list with the fresh list
        problemWords = updatedProblemWords

        // Reset to the next available word (simplified logic)
        if (currentWordIndex >= problemWords.size) {
            currentWordIndex = 0 // Start from beginning if we've reached the end
        }

        Log.d("ProblemWordsPractice", "Refreshed problem words list: ${problemWords.size} words, current index: $currentWordIndex")
    }

    /**
     * Cancel any pending auto-advance
     */
    private fun cancelAutoAdvance() {
        autoAdvanceRunnable?.let { runnable ->
            autoAdvanceHandler?.removeCallbacks(runnable)
        }
        autoAdvanceRunnable = null
    }
    
    private fun updateProgressDisplay() {
        progressText.text = "📊 Progress: ${currentWordIndex + 1}/${problemWords.size}"
        scoreText.text = "🏆 Score: $score"
    }
    
    private fun endPracticeSession() {
        practiceActive = false

        // Cancel any pending auto-advance
        cancelAutoAdvance()

        val accuracy = if (totalAttempts > 0) {
            ((correctAnswers.toFloat() / totalAttempts) * 100).toInt()
        } else 0

        val resultsMessage = buildString {
            appendLine("🎯 Practice Session Complete!")
            appendLine()
            appendLine("Final Score: $score")
            appendLine("Accuracy: $accuracy%")
            appendLine("Correct Answers: $correctAnswers/$totalAttempts")
            appendLine()

            when {
                accuracy >= 80 -> appendLine("🌟 Excellent work! You're mastering these words!")
                accuracy >= 60 -> appendLine("👍 Good job! Keep practicing to improve further.")
                accuracy >= 40 -> appendLine("💪 Nice effort! Focus on the challenging words.")
                else -> appendLine("📚 Keep practicing! Every attempt helps you learn.")
            }
        }

        questionText.text = resultsMessage
        questionText.setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)

        // Hide practice UI elements
        optionsContainer.visibility = View.GONE

        Toast.makeText(this, "Practice Complete! Score: $score", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up auto-advance handler to prevent memory leaks
        cancelAutoAdvance()
    }

    private fun animateButtonPress(button: View) {
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
}