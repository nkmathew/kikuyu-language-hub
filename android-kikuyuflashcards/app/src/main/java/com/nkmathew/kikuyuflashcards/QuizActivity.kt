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
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

class QuizActivity : ComponentActivity() {
    companion object {
        private const val TAG = "QuizActivity"
        private const val QUIZ_LENGTH = 50 // Increased from 20 to 50 to show more quiz questions
    }

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var quizHelper: QuizActivityHelper
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

    // Pre-generated quiz questions to ensure quality content
    private val quizQuestions = mutableListOf<FlashcardEntry>()
    
    data class QuizQuestion(
        val phrase: FlashcardEntry,
        val questionText: String,
        val correctAnswer: String,
        val options: List<String>,
        val isEnglishToKikuyu: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply dark theme consistently
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        try {
            flashCardManager = FlashCardManagerV2(this)
            soundManager = SoundManager(this)
            progressManager = ProgressManager(this)
            quizHelper = QuizActivityHelper(this)

            if (flashCardManager.getTotalEntries() < 4) {
                Toast.makeText(this, "Need at least 4 phrases for quiz mode", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Pre-generate quiz questions with filtered content
            generateQuizQuestions()

            // Create UI and start quiz
            createQuizUI()
            startQuiz()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing quiz", e)
            Toast.makeText(this, "Error starting quiz", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Pre-generate high-quality quiz questions from the flashcard entries
     */
    private fun generateQuizQuestions() {
        quizQuestions.clear()
        quizQuestions.addAll(quizHelper.generateQuizQuestions(flashCardManager, QUIZ_LENGTH * 2))

        Log.d(TAG, "Generated ${quizQuestions.size} quiz questions for the session")
    }
    
    private fun createQuizUI() {
        // Create layout programmatically
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 32, 32)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_background))
        }

        // Title
        val titleText = TextView(this).apply {
            text = "🧠 Kikuyu Quiz Mode"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onBackground))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // Progress indicator
        progressText = TextView(this).apply {
            text = "Question 1 of $QUIZ_LENGTH"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
            setPadding(0, 0, 0, 12)
            gravity = Gravity.CENTER
        }

        // Score display
        scoreText = TextView(this).apply {
            text = "Score: 0 / 0"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 28)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // Question
        questionText = TextView(this).apply {
            text = "Loading question..."
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurface))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceVariant))
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER
            setSingleLine(false)
            maxLines = 5
            minHeight = 180
            elevation = 8f
        }
        
        // Options layout
        val optionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 0)
        }

        fun createOptionButton(index: Int): Button = Button(this).apply {
            setOnClickListener {
                soundManager.playButtonSound()
                checkAnswer(index)
            }
            setPadding(28, 20, 28, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onPrimary))
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            elevation = 4f

            // Make buttons rounded and styled
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 20)
            this.layoutParams = layoutParams
            minHeight = 120
            setSingleLine(false)
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
            setPadding(0, 32, 0, 0)
        }

        val backButton = Button(this).apply {
            text = "← Back to Cards"
            setOnClickListener {
                soundManager.playButtonSound()
                finish()
            }
            setPadding(24, 16, 24, 16)
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSecondary))

            // Rounded button with gradient
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 12f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_secondary))
            }

            elevation = 4f
        }

        val restartButton = Button(this).apply {
            text = "🔄 Restart Quiz"
            setOnClickListener {
                soundManager.playButtonSound()
                restartQuiz()
            }
            setPadding(24, 16, 24, 16)
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onTertiary))

            // Rounded button with gradient
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 12f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary))
            }

            elevation = 4f

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

        // Get question from our pre-generated filtered list
        val questionIndex = if (quizQuestions.isEmpty()) {
            // Fallback if we have no pre-generated questions
            Log.w(TAG, "No pre-generated questions available, using random entry")
            -1
        } else {
            // Use a different question each time, with wraparound if needed
            currentQuestionIndex % quizQuestions.size
        }

        val questionPhrase = if (questionIndex >= 0 && questionIndex < quizQuestions.size) {
            quizQuestions[questionIndex]
        } else {
            // Fallback to random if we can't use pre-generated
            flashCardManager.getRandomEntry()
        }

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
    
    private fun generateWrongAnswers(correctPhrase: FlashcardEntry, isEnglishToKikuyu: Boolean): List<String> {
        val wrongAnswers = mutableListOf<String>()
        val allPhrases = mutableListOf<FlashcardEntry>()
        
        // Collect all phrases
        for (i in 0 until flashCardManager.getTotalEntries()) {
            flashCardManager.setCurrentIndex(i)
            val phrase = flashCardManager.getCurrentEntry()
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

        // Update button styles with better colors and rounded corners
        buttons.forEachIndexed { index, button ->
            val background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 16f
                when {
                    index == correctAnswerIndex -> {
                        // Correct answer - green with improved visibility
                        setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary))
                    }
                    index == selectedOptionIndex && !isCorrect -> {
                        // Incorrect selection - red with improved visibility
                        setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_error))
                    }
                    else -> {
                        // Other options - dim them
                        setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceVariant))
                        button.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
                    }
                }
            }
            button.background = background
        }

        // Record the answer and update score
        progressManager.recordQuizAnswer(isCorrect)
        if (isCorrect) {
            score++
            soundManager.playCorrectSound()

            // Show correct answer feedback with better styling
            val correctToast = Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT)
            correctToast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            correctToast.show()

            // Play achievement sound for milestones
            if (score % 5 == 0) {
                soundManager.playAchievementSound()
            }
        } else {
            soundManager.playWrongSound()

            // Show correct answer feedback with better styling
            val wrongToast = Toast.makeText(this, "❌ Correct answer: ${question.correctAnswer}", Toast.LENGTH_LONG)
            wrongToast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            wrongToast.show()
        }

        currentQuestionIndex++

        // Move to next question after a short delay
        selectedButton.postDelayed({
            generateNextQuestion()
        }, 2000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
    }
}

