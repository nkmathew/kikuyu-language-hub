package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
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
 * SentenceUnscrambleActivity - Learning mode where users rearrange Kikuyu sentence words in correct order
 *
 * Features:
 * - Drag and drop interface for reordering words
 * - Multiple difficulty levels (Easy, Medium, Hard)
 * - Entire phrases scrambled for learning
 * - Progress tracking and scoring
 * - Problem word tracking
 */
class SentenceUnscrambleActivity : AppCompatActivity() {

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker

    // UI Components
    private lateinit var questionText: TextView
    private lateinit var sourceWordsContainer: LinearLayout
    private lateinit var targetSentenceContainer: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var checkButton: Button
    private lateinit var hintButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    // Game state
    private var currentPhrase: FlashcardEntry? = null
    private var originalPhrase = ""
    private var scrambledWords = mutableListOf<String>()
    private var correctWordOrder = mutableListOf<String>()
    private var currentWordOrder = mutableListOf<String>()
    private var score = 0
    private var totalQuestions = 0
    private var hintsUsed = 0
    private var difficulty = "medium" // easy, medium, hard
    private var currentQuestionStartTime = 0L

    companion object {
        private const val TAG = "SentenceUnscrambleActivity"
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
            text = "🔀 Sentence Unscramble"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 32)
        }

        // Difficulty indicator
        val difficultyText = TextView(this).apply {
            text = "Difficulty: ${difficulty.uppercase()}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_secondary))
            setPadding(0, 0, 0, 24)
        }

        // Score display
        scoreText = TextView(this).apply {
            text = "Score: $score/$totalQuestions"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.success_green))
            setPadding(0, 0, 0, 16)
        }

        // Progress indicator
        progressText = TextView(this).apply {
            text = "Question 1 of ${flashCardManager.getTotalEntries()}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 32)
        }

        // Question container with card styling
        val questionContainer = createQuestionCard()

        // Target sentence container (where words will be arranged)
        targetSentenceContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 24, 16, 24)
            gravity = Gravity.CENTER
            background = createContainerBackground(R.color.md_theme_dark_surfaceContainerHigh)
            // Set up drop target
            setOnDragListener(createDropListener())

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 16)
            this.layoutParams = layoutParams

            // Add instruction text
            val instructionText = TextView(this@SentenceUnscrambleActivity).apply {
                text = "Drag words here to form the sentence"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_onSurfaceVariant))
                gravity = Gravity.CENTER
                tag = "instruction"
            }
            addView(instructionText)
        }

        // Source words container (scrambled words)
        sourceWordsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Changed to vertical to allow words to wrap naturally
            setPadding(16, 24, 16, 24)
            gravity = Gravity.CENTER
            background = createContainerBackground(R.color.md_theme_dark_surfaceContainerLow)

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 24)
            this.layoutParams = layoutParams
        }

        // Buttons
        val buttonContainer = createButtonContainer()

        // Add all components to main container
        mainContainer.addView(titleText)
        mainContainer.addView(difficultyText)
        mainContainer.addView(scoreText)
        mainContainer.addView(progressText)
        mainContainer.addView(questionContainer)
        mainContainer.addView(targetSentenceContainer)
        mainContainer.addView(sourceWordsContainer)
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
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_onSurface))
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
            setColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_surface))
            setStroke(3, ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_primary))
        }
    }

    private fun createContainerBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, colorRes))
            setStroke(2, ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_outline))
        }
    }

    private fun createButtonContainer(): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        // Check button
        checkButton = Button(this).apply {
            text = "✓ Check Answer"
            textSize = 16f
            setOnClickListener {
                animateButtonPress(this)
                checkAnswer()
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
        }

        // Hint button
        hintButton = Button(this).apply {
            text = "💡 Hint ($MAX_HINTS remaining)"
            textSize = 14f
            setOnClickListener {
                animateButtonPress(this)
                showHint()
            }
            setPadding(24, 12, 24, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_dark_tertiary)
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

        // Add all buttons to container
        buttonContainer.addView(checkButton)
        buttonContainer.addView(hintButton)
        buttonContainer.addView(nextButton)
        buttonContainer.addView(problemWordsButton)
        buttonContainer.addView(backButton)

        return buttonContainer
    }

    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, colorRes))
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
        checkButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        hintButton.visibility = View.VISIBLE

        // Clear containers
        sourceWordsContainer.removeAllViews()
        targetSentenceContainer.removeAllViews()

        // Add instruction to target container
        val instructionText = TextView(this).apply {
            text = "Drag words here to form the sentence"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_onSurfaceVariant))
            gravity = Gravity.CENTER
            tag = "instruction"
        }
        targetSentenceContainer.addView(instructionText)

        // Create unscramble question
        createUnscrambleQuestion()

        // Update progress
        val currentIndex = flashCardManager.getCurrentIndex()
        val totalPhrases = flashCardManager.getTotalEntries()
        progressText.text = "Question ${currentIndex + 1} of $totalPhrases"

        // Update hint button
        hintButton.text = "💡 Hint (${MAX_HINTS - hintsUsed} remaining)"
    }

    private fun createUnscrambleQuestion() {
        val phrase = currentPhrase ?: return

        // Save original phrase
        originalPhrase = phrase.kikuyu

        // Split phrase into words
        val words = originalPhrase.split(" ").filter { it.isNotEmpty() }
        correctWordOrder = words.toMutableList()

        // Create scrambled version based on difficulty
        scrambledWords = when (difficulty) {
            "easy" -> createEasyScramble(words)
            "medium" -> createMediumScramble(words)
            "hard" -> createHardScramble(words)
            else -> createMediumScramble(words)
        }

        // Display the question
        questionText.text = "Arrange the Kikuyu words to match:\n\nEnglish: \"${phrase.english}\""

        // Add draggable word elements to source container
        for (word in scrambledWords) {
            addDraggableWord(word)
        }
    }

    private fun createEasyScramble(words: List<String>): MutableList<String> {
        // For easy, just swap a few words (up to 50% of words)
        val result = words.toMutableList()
        if (result.size <= 1) return result

        val swapCount = (result.size / 2).coerceAtLeast(1)
        repeat(swapCount) {
            val idx1 = Random.nextInt(result.size)
            var idx2 = Random.nextInt(result.size)
            while (idx1 == idx2) {
                idx2 = Random.nextInt(result.size)
            }
            val temp = result[idx1]
            result[idx1] = result[idx2]
            result[idx2] = temp
        }

        return result
    }

    private fun createMediumScramble(words: List<String>): MutableList<String> {
        // For medium, random shuffle
        return words.shuffled().toMutableList()
    }

    private fun createHardScramble(words: List<String>): MutableList<String> {
        // For hard, reverse the order (completely opposite) and then shuffle a bit more
        val result = words.reversed().toMutableList()
        if (result.size <= 2) return result.shuffled().toMutableList()

        // Swap a few more words to make it harder
        val swapCount = result.size / 2
        repeat(swapCount) {
            val idx1 = Random.nextInt(result.size)
            var idx2 = Random.nextInt(result.size)
            while (idx1 == idx2) {
                idx2 = Random.nextInt(result.size)
            }
            val temp = result[idx1]
            result[idx1] = result[idx2]
            result[idx2] = temp
        }

        return result
    }

    private fun addDraggableWord(word: String) {
        val wordView = TextView(this).apply {
            text = word
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_onPrimaryContainer))
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = createWordBackground(R.color.md_theme_dark_primaryContainer)
            setPadding(24, 16, 24, 16)
            gravity = Gravity.CENTER
            elevation = 4f

            // Add margins for spacing
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 8, 8, 8)
            this.layoutParams = layoutParams

            // Set tag with word for identification
            tag = word

            // Enable drag functionality
            setOnTouchListener(createDragListener())
        }

        sourceWordsContainer.addView(wordView)
    }

    private fun createWordBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(ContextCompat.getColor(this@SentenceUnscrambleActivity, colorRes))
        }
    }

    private fun createDragListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start drag operation
                    val dragData = View.DragShadowBuilder(view)
                    view.startDragAndDrop(null, dragData, view, 0)

                    // Make view "disappear" temporarily
                    view.visibility = View.INVISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun createDropListener(): View.OnDragListener {
        return View.OnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Remove instruction text if it exists
                    val instruction = targetSentenceContainer.findViewWithTag<TextView>("instruction")
                    instruction?.let { targetSentenceContainer.removeView(it) }
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Highlight the drop area
                    (view.background as? GradientDrawable)?.setStroke(
                        4,
                        ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_primary)
                    )
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Restore normal appearance
                    (view.background as? GradientDrawable)?.setStroke(
                        2,
                        ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_outline)
                    )
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Handle the drop
                    val draggedView = event.localState as View
                    val sourceContainer = draggedView.parent as ViewGroup

                    // Add word to target container
                    sourceContainer.removeView(draggedView)
                    (view as ViewGroup).addView(draggedView)
                    draggedView.visibility = View.VISIBLE

                    // Update current word order
                    updateCurrentWordOrder()

                    // Restore normal appearance
                    (view.background as? GradientDrawable)?.setStroke(
                        2,
                        ContextCompat.getColor(this@SentenceUnscrambleActivity, R.color.md_theme_dark_outline)
                    )
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Ensure view is visible if drag fails
                    val draggedView = event.localState as View
                    draggedView.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun updateCurrentWordOrder() {
        currentWordOrder.clear()

        // Get all child views in the target container
        for (i in 0 until targetSentenceContainer.childCount) {
            val child = targetSentenceContainer.getChildAt(i)
            val word = child.tag as? String ?: continue
            currentWordOrder.add(word)
        }
    }

    private fun checkAnswer() {
        if (currentWordOrder.isEmpty()) {
            Toast.makeText(this, "Please arrange the words first", Toast.LENGTH_SHORT).show()
            return
        }

        val isCorrect = currentWordOrder.joinToString(" ") == correctWordOrder.joinToString(" ")
        processAnswerResult(isCorrect)
    }

    private fun processAnswerResult(isCorrect: Boolean) {
        totalQuestions++
        val responseTime = System.currentTimeMillis() - currentQuestionStartTime

        // Track with failure tracker
        val phrase = currentPhrase ?: return

        if (isCorrect) {
            score++

            // Record success with detailed information
            failureTracker.recordSuccess(
                entry = phrase,
                learningMode = FailureTracker.LearningMode.SENTENCE_UNSCRAMBLE,
                responseTime = responseTime
            )

            showCorrectFeedback()
        } else {
            // Record failure with enhanced tracking information
            failureTracker.recordFailure(
                entry = phrase,
                failureType = FailureTracker.FailureType.SENTENCE_STRUCTURE_ERROR,
                learningMode = FailureTracker.LearningMode.SENTENCE_UNSCRAMBLE,
                userAnswer = currentWordOrder.joinToString(" "),
                correctAnswer = correctWordOrder.joinToString(" "),
                difficulty = difficulty,
                responseTime = responseTime
            )

            showIncorrectFeedback()
        }

        // Hide input options and show result
        hideAnswerOptions()
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

    private fun hideAnswerOptions() {
        checkButton.visibility = View.GONE
        hintButton.visibility = View.GONE
    }

    private fun showCorrectFeedback() {
        questionText.text = "✅ Correct!\n\nKikuyu: \"${correctWordOrder.joinToString(" ")}\"\n\nEnglish: \"${currentPhrase?.english}\""
        questionText.setTextColor(ContextCompat.getColor(this, R.color.success_green))

        // Animate success
        animateQuestionSuccess()
    }

    private fun showIncorrectFeedback() {
        questionText.text = "❌ Not quite.\n\nYou said: \"${currentWordOrder.joinToString(" ")}\"\n\nCorrect Kikuyu: \"${correctWordOrder.joinToString(" ")}\"\n\nEnglish: \"${currentPhrase?.english}\""
        questionText.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_error))

        // Animate shake for incorrect answer
        animateQuestionShake()
    }

    private fun animateQuestionSuccess() {
        val scaleX = ObjectAnimator.ofFloat(questionText, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(questionText, "scaleY", 1f, 1.05f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = BounceInterpolator()
            start()
        }
    }

    private fun animateQuestionShake() {
        val shake = ObjectAnimator.ofFloat(questionText, "translationX", 0f, 15f, -15f, 15f, -15f, 6f, -6f, 0f)
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

    private fun showHint() {
        if (hintsUsed >= MAX_HINTS) {
            Toast.makeText(this, "No more hints available!", Toast.LENGTH_SHORT).show()
            return
        }

        hintsUsed++
        hintButton.text = "💡 Hint (${MAX_HINTS - hintsUsed} remaining)"

        // Generate hint based on hints used and difficulty
        when (hintsUsed) {
            1 -> {
                // First hint: Show the first word
                Toast.makeText(this,
                    "The sentence starts with: \"${correctWordOrder.firstOrNull() ?: ""}\"",
                    Toast.LENGTH_LONG
                ).show()
            }
            2 -> {
                // Second hint: Show the last word
                Toast.makeText(this,
                    "The sentence ends with: \"${correctWordOrder.lastOrNull() ?: ""}\"",
                    Toast.LENGTH_LONG
                ).show()
            }
            3 -> {
                // Final hint: Show multiple word pairs
                val hintText = if (correctWordOrder.size > 3) {
                    "Word pairs: ${correctWordOrder[0]} ${correctWordOrder[1]}, " +
                    "... ${correctWordOrder[correctWordOrder.size - 2]} ${correctWordOrder.last()}"
                } else {
                    "Word order: ${correctWordOrder.joinToString(" ")}"
                }
                Toast.makeText(this, hintText, Toast.LENGTH_LONG).show()
            }
        }

        if (hintsUsed >= MAX_HINTS) {
            hintButton.isEnabled = false
        }
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