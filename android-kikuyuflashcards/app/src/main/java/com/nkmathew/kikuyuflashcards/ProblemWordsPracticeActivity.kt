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
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.FailureTracker

/**
 * ProblemWordsPracticeActivity - Focused practice session for problem words
 */
class ProblemWordsPracticeActivity : AppCompatActivity() {
    
    private lateinit var failureTracker: FailureTracker
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    
    // UI Components
    private lateinit var titleText: TextView
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var questionText: TextView
    private lateinit var answerInput: EditText
    private lateinit var checkButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    
    // Practice state
    private var problemWords: List<FailureTracker.DifficultyWord> = emptyList()
    private var currentWordIndex = 0
    private var currentWord: FailureTracker.DifficultyWord? = null
    private var score = 0
    private var correctAnswers = 0
    private var totalAttempts = 0
    private var practiceActive = false
    private var currentQuestionStartTime = 0L
    
    companion object {
        private const val TAG = "ProblemWordsPracticeActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        
        // Initialize managers
        failureTracker = FailureTracker(this)
        flashCardManager = FlashCardManager(this)
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
            setTextColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
        }
        
        val subtitleText = TextView(this).apply {
            text = "Focused practice on challenging words"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.text_secondary))
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
            setTextColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.success_green))
            setPadding(8, 4, 8, 4)
        }
        
        progressText = TextView(this).apply {
            text = "📊 Progress: 0/0"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_secondary))
            setPadding(8, 4, 8, 4)
        }
        
        progressContainer.addView(scoreText)
        progressContainer.addView(progressText)
        
        // Question area
        val questionCard = createQuestionCard()
        
        // Input area
        val inputContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
        }
        
        answerInput = EditText(this).apply {
            hint = "Type the Kikuyu translation..."
            textSize = 16f
            setPadding(24, 16, 24, 16)
            background = createInputBackground()
            setTextColor(Color.BLACK)
            setSingleLine(true)
            visibility = View.GONE
        }
        
        // Buttons
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        
        checkButton = Button(this).apply {
            text = "✓ Check Answer"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                checkAnswer() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
            visibility = View.GONE
        }
        
        val startButton = Button(this).apply {
            text = "🚀 START PRACTICE"
            textSize = 18f
            setOnClickListener { 
                animateButtonPress(this)
                startPractice() 
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
        }
        
        nextButton = Button(this).apply {
            text = "Next Word →"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                nextWord() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_secondary)
            visibility = View.GONE
        }
        
        backButton = Button(this).apply {
            text = "🏠 Back to Problem Words"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                finish() 
            }
            setPadding(20, 10, 20, 10)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_outline)
        }
        
        buttonContainer.addView(checkButton)
        buttonContainer.addView(startButton)
        buttonContainer.addView(nextButton)
        buttonContainer.addView(backButton)
        
        mainContainer.addView(headerContainer)
        mainContainer.addView(progressContainer)
        mainContainer.addView(questionCard)
        mainContainer.addView(inputContainer)
        mainContainer.addView(buttonContainer)
        
        rootLayout.addView(mainContainer)
        return rootLayout
    }
    
    private fun createProgressBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_surfaceVariant))
            setStroke(1, ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_outline))
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
            text = "Get ready to practice your problem words!\n\nTap START to begin."
            textSize = 18f
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
            setStroke(3, ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.quiz_purple))
        }
    }
    
    private fun createInputBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(Color.WHITE)
            setStroke(2, ContextCompat.getColor(this@ProblemWordsPracticeActivity, R.color.md_theme_light_secondary))
        }
    }
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@ProblemWordsPracticeActivity, colorRes))
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
        
        // Hide start button, show practice UI
        checkButton.visibility = View.VISIBLE
        answerInput.visibility = View.VISIBLE
        
        // Hide the start button
        val buttonContainer = checkButton.parent as ViewGroup
        for (i in 0 until buttonContainer.childCount) {
            val child = buttonContainer.getChildAt(i)
            if (child is Button && child.text.toString().contains("START")) {
                child.visibility = View.GONE
                break
            }
        }
        
        currentWord = problemWords[currentWordIndex]
        showNextQuestion()
        
        soundManager.playButtonSound()
    }
    
    private fun showNextQuestion() {
        if (currentWordIndex >= problemWords.size) {
            endPracticeSession()
            return
        }
        
        currentWord = problemWords[currentWordIndex]
        currentQuestionStartTime = System.currentTimeMillis()
        
        // Reset UI
        answerInput.text.clear()
        nextButton.visibility = View.GONE
        
        val word = currentWord!!
        questionText.text = "🧠 Recall Practice\n\nType the Kikuyu translation for:\n\n\"${word.englishText}\"\n\nYou've struggled with this word ${word.failureCount} times"
        
        // Focus on input
        answerInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(answerInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        
        updateProgressDisplay()
    }
    
    private fun checkAnswer() {
        val userAnswer = answerInput.text.toString().trim()
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show()
            return
        }
        
        val word = currentWord!!
        val isCorrect = userAnswer.equals(word.kikuyuText, ignoreCase = true)
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        
        totalAttempts++
        
        if (isCorrect) {
            correctAnswers++
            score += 10
            
            // Record success with failure tracker
            val phrase = Phrase(
                english = word.englishText,
                kikuyu = word.kikuyuText,
                category = word.category
            )
            failureTracker.recordSuccess(phrase, FailureTracker.LearningMode.FLASHCARD, responseTime)
            
            showCorrectFeedback()
        } else {
            // Record failure with failure tracker
            val phrase = Phrase(
                english = word.englishText,
                kikuyu = word.kikuyuText,
                category = word.category
            )
            
            failureTracker.recordFailure(
                phrase = phrase,
                failureType = FailureTracker.FailureType.RECALL_ERROR,
                learningMode = FailureTracker.LearningMode.FLASHCARD,
                userAnswer = userAnswer,
                correctAnswer = word.kikuyuText,
                responseTime = responseTime
            )
            
            showIncorrectFeedback(userAnswer, word.kikuyuText)
        }
        
        // Hide input elements and show next button
        answerInput.visibility = View.GONE
        checkButton.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
        
        updateProgressDisplay()
    }
    
    private fun showCorrectFeedback() {
        val word = currentWord!!
        questionText.text = "✅ CORRECT!\n\n\"${word.englishText}\"\n${word.kikuyuText}\n\n+10 points!"
        questionText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
        
        animateSuccess()
        soundManager.playButtonSound()
    }
    
    private fun showIncorrectFeedback(userAnswer: String, correctAnswer: String) {
        val word = currentWord!!
        questionText.text = "❌ Not quite right.\n\n\"${word.englishText}\"\nYour answer: \"$userAnswer\"\nCorrect: \"$correctAnswer\"\n\nKeep practicing this word!"
        questionText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error))
        
        animateError()
    }
    
    private fun nextWord() {
        currentWordIndex++
        
        // Reset question text color
        questionText.setTextColor(Color.BLACK)
        
        // Show input elements again
        answerInput.visibility = View.VISIBLE
        checkButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        
        if (currentWordIndex < problemWords.size) {
            showNextQuestion()
        } else {
            endPracticeSession()
        }
    }
    
    private fun updateProgressDisplay() {
        progressText.text = "📊 Progress: ${currentWordIndex + 1}/${problemWords.size}"
        scoreText.text = "🏆 Score: $score"
    }
    
    private fun endPracticeSession() {
        practiceActive = false
        
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
        questionText.setTextColor(Color.BLACK)
        
        // Hide practice UI elements
        answerInput.visibility = View.GONE
        checkButton.visibility = View.GONE
        nextButton.visibility = View.GONE
        
        Toast.makeText(this, "Practice Complete! Score: $score", Toast.LENGTH_LONG).show()
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
}