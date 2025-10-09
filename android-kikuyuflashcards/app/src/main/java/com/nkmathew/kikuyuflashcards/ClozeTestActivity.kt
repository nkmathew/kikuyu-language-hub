package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.ProgressManager
import com.nkmathew.kikuyuflashcards.SoundManager
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.FailureTracker
import kotlin.random.Random

/**
 * ClozeTestActivity - Advanced learning mode where users complete contextual sentences
 * 
 * Cloze tests focus on:
 * - Contextual understanding of Kikuyu phrases
 * - Multiple blanks in longer sentences
 * - Word bank matching exercises
 * - Contextual comprehension
 * - Progressive difficulty levels
 */
class ClozeTestActivity : AppCompatActivity() {
    
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker
    
    // UI Components
    private lateinit var contextText: TextView
    private lateinit var blanksContainer: LinearLayout
    private lateinit var wordBankContainer: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var checkButton: Button
    private lateinit var resetButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    
    // Game state
    private var currentPhrases: List<Phrase> = emptyList()
    private var blanks: MutableList<ClozeBlank> = mutableListOf()
    private var wordBank: MutableList<String> = mutableListOf()
    private var score = 0
    private var totalBlanks = 0
    private var currentQuestionIndex = 0
    private var difficulty = "medium" // easy, medium, hard
    private var testMode = "context" // context, matching, comprehension
    
    // Timer
    private var startTime = 0L
    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    // Failure tracking
    private var currentQuestionStartTime = 0L
    
    companion object {
        private const val TAG = "ClozeTestActivity"
        private const val TIMER_UPDATE_INTERVAL = 1000L // 1 second
    }
    
    data class ClozeBlank(
        val id: Int,
        val correctAnswer: String,
        var userInput: String = "",
        val position: Int,
        val isFilled: Boolean = false
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        
        // Initialize managers
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        failureTracker = FailureTracker(this)
        
        // Get parameters from intent
        difficulty = intent.getStringExtra("difficulty") ?: "medium"
        testMode = intent.getStringExtra("testMode") ?: "context"
        
        val layoutView = createLayout()
        setContentView(layoutView)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(layoutView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            // Apply content padding to the inner container
            val mainContainer = (view as ScrollView).getChildAt(0)
            mainContainer?.updatePadding(top = 32)
            insets
        }
        
        startNewTest()
    }
    
    private fun createLayout(): ScrollView {
        val rootLayout = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 24, 32)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = when (testMode) {
                "context" -> "🧩 Cloze Test - Context"
                "matching" -> "🔗 Cloze Test - Word Matching"
                "comprehension" -> "📖 Cloze Test - Comprehension"
                else -> "🧩 Cloze Test"
            }
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 16)
        }
        
        // Subtitle with difficulty
        val subtitleText = TextView(this).apply {
            text = "Difficulty: ${difficulty.uppercase()} | Mode: ${testMode.uppercase()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 24)
        }
        
        // Stats row
        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        
        scoreText = TextView(this).apply {
            text = "Score: $score/$totalBlanks"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.success_green))
            setPadding(16, 8, 16, 8)
        }
        
        timerText = TextView(this).apply {
            text = "Time: 0:00"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.text_secondary))
            setPadding(16, 8, 16, 8)
        }
        
        progressText = TextView(this).apply {
            text = "Question 1 of 5"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.text_secondary))
            setPadding(16, 8, 16, 8)
        }
        
        statsContainer.addView(scoreText)
        statsContainer.addView(timerText)
        statsContainer.addView(progressText)
        
        // Main content area
        val contentCard = createContentCard()
        
        // Button area
        val buttonContainer = createButtonContainer()
        
        // Add all components
        mainContainer.addView(titleText)
        mainContainer.addView(subtitleText)
        mainContainer.addView(statsContainer)
        mainContainer.addView(contentCard)
        mainContainer.addView(buttonContainer)
        
        rootLayout.addView(mainContainer)
        return rootLayout
    }
    
    private fun createContentCard(): LinearLayout {
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = createCardBackground()
            elevation = 8f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Context text area
        contextText = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.BLACK)
            setLineSpacing(1.4f, 1f)
            setPadding(0, 0, 0, 24)
        }
        
        // Blanks area
        blanksContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }
        
        // Word bank title
        val wordBankTitle = TextView(this).apply {
            text = "📝 Word Bank - Drag words to the blanks above:"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_secondary))
            setPadding(0, 16, 0, 12)
        }
        
        // Word bank area
        wordBankContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        
        cardContainer.addView(contextText)
        cardContainer.addView(blanksContainer)
        cardContainer.addView(wordBankTitle)
        cardContainer.addView(wordBankContainer)
        
        return cardContainer
    }
    
    private fun createCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.WHITE)
            setStroke(2, ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_primary))
        }
    }
    
    private fun createButtonContainer(): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        
        // Action buttons row
        val actionButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        
        checkButton = Button(this).apply {
            text = "✓ Check Answers"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                checkAnswers() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
        }
        
        resetButton = Button(this).apply {
            text = "↻ Reset"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                resetCurrentTest() 
            }
            setPadding(20, 10, 20, 10)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.warning_orange)
        }
        
        actionButtonsContainer.addView(checkButton)
        actionButtonsContainer.addView(resetButton)
        
        // Navigation buttons row
        val navButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        
        nextButton = Button(this).apply {
            text = "Next Test →"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                startNewTest() 
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_secondary)
            visibility = View.GONE
        }
        
        backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                finish() 
            }
            setPadding(20, 10, 20, 10)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_outline)
        }
        
        navButtonsContainer.addView(nextButton)
        navButtonsContainer.addView(backButton)
        
        buttonContainer.addView(actionButtonsContainer)
        buttonContainer.addView(navButtonsContainer)
        
        return buttonContainer
    }
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(ContextCompat.getColor(this@ClozeTestActivity, colorRes))
        }
    }
    
    private fun startNewTest() {
        // Reset state
        blanks.clear()
        wordBank.clear()
        currentQuestionIndex++
        currentQuestionStartTime = System.currentTimeMillis()
        
        // Hide next button, show action buttons
        nextButton.visibility = View.GONE
        checkButton.visibility = View.VISIBLE
        resetButton.visibility = View.VISIBLE
        
        // Generate test content based on mode
        when (testMode) {
            "context" -> generateContextTest()
            "matching" -> generateMatchingTest()
            "comprehension" -> generateComprehensionTest()
            else -> generateContextTest()
        }
        
        // Update UI
        updateProgressText()
        setupBlanksUI()
        setupWordBankUI()
        
        // Start timer
        startTimer()
    }
    
    private fun generateContextTest() {
        // Get 2-4 related phrases for context
        val phraseCount = when (difficulty) {
            "easy" -> 2
            "medium" -> 3
            "hard" -> 4
            else -> 3
        }
        
        currentPhrases = mutableListOf<Phrase>().apply {
            for (i in 0 until phraseCount) {
                val phrase = flashCardManager.getRandomPhrase()
                phrase?.let { add(it) }
            }
        }
        
        if (currentPhrases.isEmpty()) {
            Toast.makeText(this, "No phrases available for test", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Create contextual passage
        val contextBuilder = StringBuilder()
        val blankPositions = mutableListOf<Int>()
        
        currentPhrases.forEachIndexed { index, phrase ->
            if (index > 0) contextBuilder.append(" ")
            
            val words = phrase.kikuyu.split(" ")
            val blankIndex = when (difficulty) {
                "easy" -> words.size / 2 // Middle word
                "medium" -> Random.nextInt(words.size)
                "hard" -> Random.nextInt(words.size)
                else -> words.size / 2
            }
            
            words.forEachIndexed { wordIndex, word ->
                if (wordIndex == blankIndex) {
                    val blankId = blanks.size
                    blanks.add(ClozeBlank(blankId, word, position = blankId))
                    contextBuilder.append(" [BLANK$blankId] ")
                    blankPositions.add(blankId)
                } else {
                    contextBuilder.append(word)
                }
                
                if (wordIndex < words.size - 1) {
                    contextBuilder.append(" ")
                }
            }
            
            // Add punctuation
            if (index < currentPhrases.size - 1) {
                contextBuilder.append(if (Random.nextBoolean()) "." else ",")
            } else {
                contextBuilder.append(".")
            }
        }
        
        // Create word bank (correct answers + distractors)
        wordBank.addAll(blanks.map { it.correctAnswer })
        
        // Add distractors from other phrases
        val allPhrases = flashCardManager.getAllPhrases()
        val distractors = allPhrases
            .flatMap { it.kikuyu.split(" ") }
            .filter { it !in wordBank && it.length > 2 }
            .shuffled()
            .take(blanks.size)
        
        wordBank.addAll(distractors)
        wordBank.shuffle()
        
        // Set context text (showing English for reference)
        val englishContext = currentPhrases.joinToString(" ") { it.english }
        contextText.text = "Kikuyu Context:\n${contextBuilder.toString()}\n\nEnglish Reference:\n$englishContext"
    }
    
    private fun generateMatchingTest() {
        // Get phrases for word bank matching
        val phraseCount = when (difficulty) {
            "easy" -> 3
            "medium" -> 4
            "hard" -> 5
            else -> 4
        }
        
        currentPhrases = mutableListOf<Phrase>().apply {
            for (i in 0 until phraseCount) {
                val phrase = flashCardManager.getRandomPhrase()
                phrase?.let { add(it) }
            }
        }
        
        if (currentPhrases.isEmpty()) {
            Toast.makeText(this, "No phrases available for test", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Create matching exercise
        val contextBuilder = StringBuilder()
        contextBuilder.append("Match the Kikuyu phrases with their English meanings:\n\n")
        
        currentPhrases.forEachIndexed { index, phrase ->
            val blankId = blanks.size
            blanks.add(ClozeBlank(blankId, phrase.kikuyu, position = index))
            
            contextBuilder.append("${index + 1}. [BLANK$blankId] = ${phrase.english}\n")
        }
        
        // Word bank with all Kikuyu phrases + distractors
        wordBank.addAll(currentPhrases.map { it.kikuyu })
        
        // Add distractors
        val allPhrases = flashCardManager.getAllPhrases()
        val distractors = allPhrases
            .filter { it !in currentPhrases }
            .map { it.kikuyu }
            .shuffled()
            .take(2)
        
        wordBank.addAll(distractors)
        wordBank.shuffle()
        
        contextText.text = contextBuilder.toString()
    }
    
    private fun generateComprehensionTest() {
        // Create a short story or dialogue
        val storyPhrases = mutableListOf<Phrase>()
        val phraseCount = when (difficulty) {
            "easy" -> 3
            "medium" -> 4
            "hard" -> 5
            else -> 4
        }
        
        for (i in 0 until phraseCount) {
            val phrase = flashCardManager.getRandomPhrase()
            phrase?.let { 
                storyPhrases.add(it)
                // Add some blanks for comprehension
                if (Random.nextBoolean() || i == phraseCount - 1) {
                    val words = it.kikuyu.split(" ")
                    if (words.isNotEmpty()) {
                        val blankWord = words[Random.nextInt(words.size)]
                        val blankId = blanks.size
                        blanks.add(ClozeBlank(blankId, blankWord, position = blankId))
                    }
                }
            }
        }
        
        currentPhrases = storyPhrases
        
        if (currentPhrases.isEmpty()) {
            Toast.makeText(this, "No phrases available for test", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Create comprehension passage
        val storyBuilder = StringBuilder()
        storyBuilder.append("Read the Kikuyu passage and fill in the missing words:\n\n")
        
        currentPhrases.forEachIndexed { index, phrase ->
            if (index > 0) storyBuilder.append(" ")
            
            var phraseText = phrase.kikuyu
            blanks.forEach { blank ->
                if (phraseText.contains(blank.correctAnswer)) {
                    phraseText = phraseText.replace(blank.correctAnswer, "[BLANK${blank.id}]")
                }
            }
            
            storyBuilder.append(phraseText)
            
            if (index < currentPhrases.size - 1) {
                storyBuilder.append(". ")
            } else {
                storyBuilder.append(".\n\nEnglish Translation:\n")
                currentPhrases.forEach { p ->
                    storyBuilder.append("${p.english}. ")
                }
            }
        }
        
        // Word bank
        wordBank.addAll(blanks.map { it.correctAnswer })
        
        // Add related distractors
        val allWords = currentPhrases
            .flatMap { it.kikuyu.split(" ") }
            .filter { it !in wordBank && it.length > 2 }
        
        wordBank.addAll(allWords.shuffled().take(blanks.size))
        wordBank.shuffle()
        
        contextText.text = storyBuilder.toString()
    }
    
    private fun setupBlanksUI() {
        blanksContainer.removeAllViews()
        
        blanks.forEach { blank ->
            val blankContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            
            val blankLabel = TextView(this).apply {
                text = "Blank ${blank.id + 1}:"
                textSize = 14f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 16, 0)
            }
            
            val blankInput = EditText(this).apply {
                hint = "Enter answer..."
                textSize = 14f
                setPadding(16, 12, 16, 12)
                background = createInputBackground()
                setTextColor(Color.BLACK)
                id = blank.id
                layoutParams = LinearLayout.LayoutParams(
                    200,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            blankContainer.addView(blankLabel)
            blankContainer.addView(blankInput)
            blanksContainer.addView(blankContainer)
        }
    }
    
    private fun setupWordBankUI() {
        wordBankContainer.removeAllViews()
        
        wordBank.forEach { word ->
            val wordButton = Button(this).apply {
                text = word
                textSize = 12f
                setOnClickListener { 
                    animateButtonPress(this)
                    fillBlankWithWord(word)
                }
                setPadding(12, 8, 12, 8)
                setTextColor(Color.BLACK)
                background = createWordButtonBackground()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(4, 4, 4, 4)
                }
            }
            wordBankContainer.addView(wordButton)
        }
    }
    
    private fun fillBlankWithWord(word: String) {
        // Find first empty blank
        val emptyBlank = blanks.find { it.userInput.isEmpty() }
        if (emptyBlank != null) {
            emptyBlank.userInput = word
            
            // Update the corresponding EditText
            val blankInput = findViewById<EditText>(emptyBlank.id)
            blankInput?.setText(word)
            
            // Disable the word button
            val wordButton = (wordBankContainer.getChildAt(wordBank.indexOf(word)) as? Button)
            wordButton?.isEnabled = false
            wordButton?.alpha = 0.5f
            
            soundManager.playButtonSound()
        }
    }
    
    private fun createInputBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(Color.WHITE)
            setStroke(1, ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_outline))
        }
    }
    
    private fun createWordButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_surfaceVariant))
            setStroke(1, ContextCompat.getColor(this@ClozeTestActivity, R.color.md_theme_light_secondary))
        }
    }
    
    private fun checkAnswers() {
        var correctCount = 0
        totalBlanks += blanks.size
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime
        
        blanks.forEach { blank ->
            if (blank.userInput.equals(blank.correctAnswer, ignoreCase = true)) {
                correctCount++
                score++
                
                // Record success for this blank
                currentPhrases.forEach { phrase ->
                    if (phrase.kikuyu.equals(blank.correctAnswer, ignoreCase = true)) {
                        failureTracker.recordSuccess(phrase, FailureTracker.LearningMode.CLOZE_TEST, responseTime / blanks.size)
                    }
                }
            } else {
                // Record failure for this blank
                val failureType = when {
                    blank.userInput.isEmpty() -> FailureTracker.FailureType.TIMEOUT_ERROR
                    blank.userInput.length >= blank.correctAnswer.length - 1 && blank.userInput.length <= blank.correctAnswer.length + 1 -> 
                        FailureTracker.FailureType.SPELLING_ERROR
                    else -> FailureTracker.FailureType.CLOZE_ERROR
                }
                
                currentPhrases.forEach { phrase ->
                    if (phrase.kikuyu.equals(blank.correctAnswer, ignoreCase = true)) {
                        failureTracker.recordFailure(
                            phrase = phrase,
                            failureType = failureType,
                            learningMode = FailureTracker.LearningMode.CLOZE_TEST,
                            userAnswer = blank.userInput,
                            correctAnswer = blank.correctAnswer,
                            difficulty = difficulty,
                            responseTime = responseTime / blanks.size
                        )
                    }
                }
            }
        }
        
        // Update score
        scoreText.text = "Score: $score/$totalBlanks"
        
        // Show feedback
        val percentage = if (blanks.isNotEmpty()) (correctCount * 100) / blanks.size else 0
        val message = when {
            percentage == 100 -> "🎉 Perfect! All answers correct!"
            percentage >= 80 -> "👏 Excellent! $correctCount/${blanks.size} correct!"
            percentage >= 60 -> "👍 Good job! $correctCount/${blanks.size} correct!"
            percentage >= 40 -> "💪 Keep practicing! $correctCount/${blanks.size} correct!"
            else -> "📚 Review and try again! $correctCount/${blanks.size} correct!"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Highlight correct/incorrect answers
        highlightAnswers()
        
        // Show next button, hide check button
        checkButton.visibility = View.GONE
        resetButton.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
        
        // Stop timer
        stopTimer()
        
        soundManager.playButtonSound()
    }
    
    private fun highlightAnswers() {
        blanks.forEach { blank ->
            val blankInput = findViewById<EditText>(blank.id)
            blankInput?.let { input ->
                val isCorrect = blank.userInput.equals(blank.correctAnswer, ignoreCase = true)
                val color = if (isCorrect) {
                    ContextCompat.getColor(this, R.color.success_green)
                } else {
                    ContextCompat.getColor(this, R.color.md_theme_light_error)
                }
                
                input.background.mutate().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP)
                
                if (!isCorrect) {
                    input.setText(blank.correctAnswer)
                }
            }
        }
    }
    
    private fun resetCurrentTest() {
        blanks.forEach { blank ->
            blank.userInput = ""
            
            val blankInput = findViewById<EditText>(blank.id)
            blankInput?.setText("")
            
            // Reset background color
            blankInput?.background?.mutate()?.clearColorFilter()
        }
        
        // Re-enable all word bank buttons
        for (i in 0 until wordBankContainer.childCount) {
            val wordButton = wordBankContainer.getChildAt(i) as? Button
            wordButton?.isEnabled = true
            wordButton?.alpha = 1.0f
        }
        
        soundManager.playButtonSound()
    }
    
    private fun updateProgressText() {
        progressText.text = "Question $currentQuestionIndex"
    }
    
    private fun startTimer() {
        startTime = System.currentTimeMillis()
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / (1000 * 60)) % 60
                
                timerText.text = String.format("Time: %d:%02d", minutes, seconds)
                
                timerHandler.postDelayed(this, TIMER_UPDATE_INTERVAL)
            }
        }
        timerHandler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }
    
    private fun animateButtonPress(button: Button) {
        val animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f)
        animator.duration = 100
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        progressManager.endSession()
    }
}