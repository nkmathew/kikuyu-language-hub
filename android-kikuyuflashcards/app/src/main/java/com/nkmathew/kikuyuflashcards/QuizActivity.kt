package com.nkmathew.kikuyuflashcards

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlin.random.Random

class QuizActivity : ComponentActivity() {
    companion object {
        private const val TAG = "QuizActivity"
        private const val QUIZ_LENGTH = 10
    }
    
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var questionText: TextView
    private lateinit var progressText: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var scoreText: TextView
    
    private var score = 0
    private var currentQuestionIndex = 0
    private var currentQuestion: QuizQuestion? = null
    private val random = Random.Default
    private var correctAnswerIndex = -1
    
    data class QuizQuestion(
        val phrase: Phrase,
        val questionText: String,
        val correctAnswer: String,
        val options: List<String>,
        val isEnglishToKikuyu: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            flashCardManager = FlashCardManager(this)
            soundManager = SoundManager(this)
            progressManager = ProgressManager(this)
            
            if (flashCardManager.getTotalPhrases() < 4) {
                Toast.makeText(this, "Need at least 4 phrases for quiz mode", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            createQuizUI()
            startQuiz()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing quiz", e)
            Toast.makeText(this, "Error starting quiz", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun createQuizUI() {
        // Create layout programmatically
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 32, 32)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_background))
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "🧠 Kikuyu Quiz Mode"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onBackground))
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
        }
        
        // Progress indicator
        progressText = TextView(this).apply {
            text = "Question 1 of $QUIZ_LENGTH"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onSurfaceVariant))
            setPadding(0, 0, 0, 8)
            gravity = Gravity.CENTER
        }
        
        // Score display
        scoreText = TextView(this).apply {
            text = "Score: 0 / 0"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
        }
        
        // Question
        questionText = TextView(this).apply {
            text = "Loading question..."
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onBackground))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_surfaceVariant))
            setPadding(24, 24, 24, 24)
            gravity = Gravity.CENTER
            setSingleLine(false)
            maxLines = 4
            minHeight = 120
        }
        
        // Options layout
        val optionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }
        
        fun createOptionButton(index: Int): Button = Button(this).apply {
            setOnClickListener { 
                soundManager.playButtonSound()
                checkAnswer(index) 
            }
            setPadding(24, 16, 24, 16)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onPrimary))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_primary))
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }
        
        option1Button = createOptionButton(0)
        option2Button = createOptionButton(1)
        option3Button = createOptionButton(2)
        option4Button = createOptionButton(3)
        
        optionsLayout.addView(option1Button)
        optionsLayout.addView(option2Button)
        optionsLayout.addView(option3Button)
        optionsLayout.addView(option4Button)
        
        // Action buttons layout
        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        
        val backButton = Button(this).apply {
            text = "← Back to Cards"
            setOnClickListener { 
                soundManager.playButtonSound()
                finish() 
            }
            setPadding(24, 16, 24, 16)
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onSecondary))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_secondary))
        }
        
        val restartButton = Button(this).apply {
            text = "🔄 Restart Quiz"
            setOnClickListener { 
                soundManager.playButtonSound()
                restartQuiz()
            }
            setPadding(24, 16, 24, 16)
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_onTertiary))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_light_tertiary))
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(32, 0, 0, 0)
            this.layoutParams = layoutParams
        }
        
        actionLayout.addView(backButton)
        actionLayout.addView(restartButton)
        
        // Add all views to root layout
        rootLayout.addView(titleText)
        rootLayout.addView(progressText)
        rootLayout.addView(scoreText)
        rootLayout.addView(questionText)
        rootLayout.addView(optionsLayout)
        rootLayout.addView(actionLayout)
        
        setContentView(rootLayout)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top + 32) // Add 32dp top margin
            insets
        }
    }
    
    private fun startQuiz() {
        currentQuestionIndex = 0
        score = 0
        generateNextQuestion()
    }
    
    private fun restartQuiz() {
        startQuiz()
        Toast.makeText(this, "Quiz restarted!", Toast.LENGTH_SHORT).show()
    }
    
    private fun generateNextQuestion() {
        if (currentQuestionIndex >= QUIZ_LENGTH) {
            showQuizComplete()
            return
        }
        
        val questionPhrase = flashCardManager.getRandomPhrase()
        if (questionPhrase == null) {
            questionText.text = "No questions available"
            return
        }
        
        val isEnglishToKikuyu = random.nextBoolean()
        val questionPrompt = if (isEnglishToKikuyu) questionPhrase.english else questionPhrase.kikuyu
        val correctAnswer = if (isEnglishToKikuyu) questionPhrase.kikuyu else questionPhrase.english
        
        // Generate wrong answers
        val wrongAnswers = generateWrongAnswers(questionPhrase, isEnglishToKikuyu)
        val allOptions = (wrongAnswers + correctAnswer).shuffled()
        correctAnswerIndex = allOptions.indexOf(correctAnswer)
        
        currentQuestion = QuizQuestion(
            phrase = questionPhrase,
            questionText = questionPrompt,
            correctAnswer = correctAnswer,
            options = allOptions,
            isEnglishToKikuyu = isEnglishToKikuyu
        )
        
        updateQuizUI(questionPrompt, allOptions, isEnglishToKikuyu)
    }
    
    private fun updateQuizUI(question: String, options: List<String>, isEnglishToKikuyu: Boolean) {
        val direction = if (isEnglishToKikuyu) "🇬🇧 → 🇰🇪" else "🇰🇪 → 🇬🇧"
        val prompt = if (isEnglishToKikuyu) "What is the Kikuyu translation?" else "What is the English translation?"
        
        questionText.text = "$direction\n$prompt\n\n\"$question\""
        progressText.text = "Question ${currentQuestionIndex + 1} of $QUIZ_LENGTH"
        scoreText.text = "Score: $score / $currentQuestionIndex"
        
        // Set button text and reset state
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        buttons.forEachIndexed { index, button ->
            if (index < options.size) {
                button.text = options[index]
                button.isEnabled = true
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_light_primary))
            } else {
                button.text = ""
                button.isEnabled = false
            }
        }
    }
    
    private fun generateWrongAnswers(correctPhrase: Phrase, isEnglishToKikuyu: Boolean): List<String> {
        val wrongAnswers = mutableListOf<String>()
        val allPhrases = mutableListOf<Phrase>()
        
        // Collect all phrases
        for (i in 0 until flashCardManager.getTotalPhrases()) {
            flashCardManager.setCurrentIndex(i)
            val phrase = flashCardManager.getCurrentPhrase()
            if (phrase != null && phrase != correctPhrase) {
                allPhrases.add(phrase)
            }
        }
        
        // Extract the appropriate text (English or Kikuyu)
        val candidateAnswers = allPhrases.map { 
            if (isEnglishToKikuyu) it.kikuyu else it.english 
        }.distinct()
        
        // Take 3 random wrong options
        candidateAnswers.shuffled().take(3).forEach { wrongAnswers.add(it) }
        
        // Fill with fallback options if needed
        while (wrongAnswers.size < 3) {
            wrongAnswers.add(if (isEnglishToKikuyu) "Kikuyu option ${wrongAnswers.size + 1}" else "English option ${wrongAnswers.size + 1}")
        }
        
        return wrongAnswers
    }
    
    private fun showQuizComplete() {
        val accuracy = if (QUIZ_LENGTH > 0) (score * 100) / QUIZ_LENGTH else 0
        val statsMessage = """
            🎉 Quiz Complete!
            
            Final Score: $score / $QUIZ_LENGTH
            Accuracy: $accuracy%
            
            Overall Progress:
            Total Quiz Answers: ${progressManager.getQuizTotalAnswered()}
            Overall Accuracy: ${"%.1f".format(progressManager.getQuizAccuracy())}%
            Current Streak: ${progressManager.getCurrentStreak()}
            Best Streak: ${progressManager.getBestStreak()}
        """.trimIndent()

        questionText.text = statsMessage
        progressText.text = "Quiz Finished"
        scoreText.text = "Great job! 🎊"

        // Hide option buttons
        listOf(option1Button, option2Button, option3Button, option4Button).forEach { 
            it.text = ""
            it.isEnabled = false 
        }

        Toast.makeText(this, "Quiz completed! Check your stats.", Toast.LENGTH_LONG).show()
    }
    
    private fun checkAnswer(selectedOptionIndex: Int) {
        val question = currentQuestion ?: return
        val isCorrect = selectedOptionIndex == correctAnswerIndex
        
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        val selectedButton = buttons[selectedOptionIndex]
        
        // Disable all buttons
        buttons.forEach { it.isEnabled = false }
        
        // Update button colors
        buttons.forEachIndexed { index, button ->
            when {
                index == correctAnswerIndex -> {
                    button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                }
                index == selectedOptionIndex && !isCorrect -> {
                    button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }
            }
        }
        
        // Record the answer and update score
        progressManager.recordQuizAnswer(isCorrect)
        if (isCorrect) {
            score++
            soundManager.playCorrectSound()
            Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show()
            
            // Play achievement sound for milestones
            if (score % 5 == 0) {
                soundManager.playAchievementSound()
            }
        } else {
            soundManager.playWrongSound()
            Toast.makeText(this, "❌ Correct answer: ${question.correctAnswer}", Toast.LENGTH_LONG).show()
        }
        
        currentQuestionIndex++
        
        // Move to next question after delay
        selectedButton.postDelayed({
            generateNextQuestion()
        }, 2000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
    }
}

