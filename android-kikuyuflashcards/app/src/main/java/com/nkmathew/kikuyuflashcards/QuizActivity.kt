package com.nkmathew.kikuyuflashcards

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlin.random.Random
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import android.widget.ScrollView
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.View

class QuizActivity : ComponentActivity() {
    companion object {
        private const val TAG = "QuizActivity"
        private const val QUIZ_LENGTH = 50 // Increased from 20 to 50 to show more quiz questions
    }

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var quizHelper: QuizActivityHelper
    private lateinit var quizConfigManager: QuizConfigManager
    private lateinit var quizStateManager: QuizStateManager
    private lateinit var questionText: TextView
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var scoreText: TextView
    private lateinit var loadingOverlay: LinearLayout
    private lateinit var loadingSpinner: ProgressBar
    private var feedbackCard: LinearLayout? = null

    private var score = 0
    private var currentQuestionIndex = 0
    private var currentQuestion: QuizQuestion? = null
    private val random = Random.Default
    private var correctAnswerIndex = -1
    private var quizLength = QUIZ_LENGTH // Will be updated from config dialog

    // Pre-generated quiz questions to ensure quality content
    private val quizQuestions = mutableListOf<FlashcardEntry>()

    // Quiz state for restoration
    private var quizId: String? = null
    private val answeredQuestions = mutableListOf<AnsweredQuestion>()

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
            quizConfigManager = QuizConfigManager(this)
            quizStateManager = QuizStateManager(this)

            if (flashCardManager.getTotalEntries() < 4) {
                Toast.makeText(this, "Need at least 4 phrases for quiz mode", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Check if there's a saved quiz to resume
            if (quizStateManager.hasSavedQuiz()) {
                showResumeQuizDialog()
            } else {
                // Show configuration dialog before starting quiz
                showQuizConfigDialog()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing quiz", e)
            Toast.makeText(this, "Error starting quiz", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Shows a configuration dialog for quiz settings
     */
    private fun showQuizConfigDialog() {
        // Create dialog container with Material 3 styling
        val dialogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainer))
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
            text = "Quiz Configuration"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurface))
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        titleContainer.addView(titleIcon)
        titleContainer.addView(titleText)

        // Quiz length section
        val lengthSectionTitle = TextView(this).apply {
            text = "Quiz Length"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            setPadding(0, 0, 0, 16)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        // Length options container
        val lengthOptionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        // Track selected length and difficulty
        var selectedLength = quizConfigManager.getQuizLength()
        var selectedDifficulty = quizConfigManager.getDifficultyFilter()

        // Create length option buttons
        val lengthButtons = mutableListOf<Button>()
        QuizConfigManager.PRESET_LENGTHS.forEach { length ->
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
            text = "Difficulty Filter (Optional)"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            setPadding(0, 24, 0, 16)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        // Difficulty options container
        val difficultyOptionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        val difficultyOptions = listOf(
            "all" to "All Difficulties",
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
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSecondary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_secondary))
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

        // Start quiz button
        val startButton = Button(this).apply {
            text = "Start Quiz"
            setOnClickListener {
                soundManager.playButtonSound()

                // Save configuration
                quizConfigManager.setQuizLength(selectedLength)
                quizConfigManager.setDifficultyFilter(selectedDifficulty)

                // Update quiz length
                quizLength = selectedLength

                Log.d(TAG, "Starting quiz with length: $quizLength, difficulty: $selectedDifficulty")

                // Close dialog and start quiz
                (it.parent as? ViewGroup)?.let { parent ->
                    (parent.parent as? ViewGroup)?.let { grandParent ->
                        grandParent.removeView(parent)
                    }
                }

                // Pre-generate quiz questions with filtered content
                generateQuizQuestions()

                // Create UI and start quiz
                createQuizUI()
                startQuiz()
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onPrimary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary),
                    ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primaryContainer)
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
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.overlay_color))
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
     * Shows a dialog allowing user to resume a previous quiz or start new
     */
    private fun showResumeQuizDialog() {
        val savedState = quizStateManager.loadQuizState()
        if (savedState == null) {
            // No saved state found, show config dialog instead
            showQuizConfigDialog()
            return
        }

        // Create dialog container with Material 3 styling
        val dialogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainer))
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
            text = "Resume Quiz?"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurface))
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        titleContainer.addView(titleIcon)
        titleContainer.addView(titleText)

        // Info text about saved quiz
        val infoText = TextView(this).apply {
            val progress = "${savedState.currentQuestionIndex + 1} / ${savedState.quizLength}"
            val scoreText = "${savedState.score} correct"
            text = "You have an unfinished quiz:\n\nProgress: $progress\nScore: $scoreText\n\nWould you like to continue where you left off?"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
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

        // New Quiz button
        val newQuizButton = Button(this).apply {
            text = "New Quiz"
            setOnClickListener {
                soundManager.playButtonSound()
                // Clear saved state and show config dialog
                quizStateManager.clearQuizState()

                // Close this dialog
                (it.parent as? ViewGroup)?.let { parent ->
                    (parent.parent as? ViewGroup)?.let { grandParent ->
                        grandParent.removeView(parent)
                    }
                }

                // Show config dialog
                showQuizConfigDialog()
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSecondary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_secondary))
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
            text = "Resume Quiz"
            setOnClickListener {
                soundManager.playButtonSound()

                // Close dialog
                (it.parent as? ViewGroup)?.let { parent ->
                    (parent.parent as? ViewGroup)?.let { grandParent ->
                        grandParent.removeView(parent)
                    }
                }

                // Resume the quiz
                resumeQuizFromState(savedState)
            }
            setPadding(32, 20, 32, 20)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onPrimary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary),
                    ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primaryContainer)
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

        actionButtonsContainer.addView(newQuizButton)
        actionButtonsContainer.addView(resumeButton)

        // Add all sections to dialog container
        dialogContainer.addView(titleContainer)
        dialogContainer.addView(infoText)
        dialogContainer.addView(actionButtonsContainer)

        // Create dialog overlay
        val dialogOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.overlay_color))
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
     * Resumes a quiz from a saved state
     */
    private fun resumeQuizFromState(state: QuizState) {
        // Restore quiz configuration
        quizLength = state.quizLength
        currentQuestionIndex = state.currentQuestionIndex
        score = state.score
        quizId = state.quizId

        // Restore answered questions history
        answeredQuestions.clear()
        answeredQuestions.addAll(state.answeredQuestions)

        // Restore the quiz questions by their IDs
        quizQuestions.clear()
        state.questionIds.forEach { questionId ->
            // Find the flashcard entry with this ID
            for (i in 0 until flashCardManager.getTotalEntries()) {
                flashCardManager.setCurrentIndex(i)
                val entry = flashCardManager.getCurrentEntry()
                if (entry?.id == questionId) {
                    quizQuestions.add(entry)
                    break
                }
            }
        }

        Log.d(TAG, "Resuming quiz: ${state.currentQuestionIndex}/${state.quizLength}, Score: ${state.score}")

        // Create UI and continue quiz
        createQuizUI()
        generateNextQuestion()
    }

    /**
     * Creates a selectable option button for the config dialog
     */
    private fun createOptionButton(text: String, isSelected: Boolean, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener {
                soundManager.playButtonSound()
                onClick()
            }
            setPadding(24, 18, 24, 18)
            textSize = 16f
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            elevation = 4f

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }

            updateOptionButtonState(this, isSelected)
        }
    }

    /**
     * Updates the visual state of an option button
     */
    private fun updateOptionButtonState(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this, R.color.md_theme_dark_primary),
                    ContextCompat.getColor(this, R.color.md_theme_dark_primaryContainer)
                )
            ).apply {
                cornerRadius = 16f
                setStroke(4, ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary))
            }
            button.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_onPrimary))
            button.elevation = 8f
        } else {
            button.background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainerHigh))
                setStroke(2, ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_outline))
            }
            button.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_onSurfaceVariant))
            button.elevation = 2f
        }
    }

    /**
     * Pre-generate high-quality quiz questions from the flashcard entries
     */
    private fun generateQuizQuestions() {
        quizQuestions.clear()
        quizQuestions.addAll(quizHelper.generateQuizQuestions(flashCardManager, quizLength * 2))

        Log.d(TAG, "Generated ${quizQuestions.size} quiz questions for the session")
    }

    /**
     * Creates a modern gradient background drawable
     */
    private fun createGradientBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ContextCompat.getColor(this, R.color.md_theme_dark_surfaceContainerHighest),
                ContextCompat.getColor(this, R.color.md_theme_dark_surface),
                ContextCompat.getColor(this, R.color.md_theme_dark_surfaceContainerLowest)
            )
        )
    }

    /**
     * Creates a card-style background with gradient and rounded corners
     */
    private fun createQuestionCardBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ContextCompat.getColor(this, R.color.md_theme_dark_primaryContainer),
                ContextCompat.getColor(this, R.color.md_theme_dark_surfaceContainer)
            )
        ).apply {
            cornerRadius = 24f
        }
    }

    /**
     * Creates styled option button background with gradient
     */
    private fun createOptionButtonBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                ContextCompat.getColor(this, R.color.md_theme_dark_primary),
                ContextCompat.getColor(this, R.color.md_theme_dark_primaryContainer)
            )
        ).apply {
            cornerRadius = 20f
        }
    }

    private fun createQuizUI() {
        // Create scrollable container
        val scrollView = ScrollView(this).apply {
            background = createGradientBackground()
        }

        // Create main content layout
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 24, 24)
            gravity = Gravity.CENTER
        }

        // Header container with title and progress
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 32)
            gravity = Gravity.CENTER
        }

        // Title with enhanced typography
        val titleText = TextView(this).apply {
            text = "Kikuyu Quiz"
            textSize = 32f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onBackground))
            setPadding(0, 0, 0, 8)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            letterSpacing = 0.02f
        }

        val titleIcon = TextView(this).apply {
            text = "🧠"
            textSize = 40f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        headerContainer.addView(titleIcon)
        headerContainer.addView(titleText)

        // Progress section with visual progress bar
        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        progressText = TextView(this).apply {
            text = "Question 1 of $quizLength"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
            setPadding(0, 0, 0, 8)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }

        // Visual progress bar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = quizLength
            progress = 0
            progressDrawable = createProgressBarDrawable()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                16
            ).apply {
                setMargins(32, 0, 32, 8)
            }
        }

        // Score display with enhanced styling
        scoreText = TextView(this).apply {
            text = "Score: 0 / 0"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
            setPadding(0, 8, 0, 0)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }

        progressContainer.addView(progressText)
        progressContainer.addView(progressBar)
        progressContainer.addView(scoreText)

        // Question card with enhanced styling
        val questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createQuestionCardBackground()
            setPadding(32, 32, 32, 32)
            elevation = 12f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }

        questionText = TextView(this).apply {
            text = "Loading question..."
            textSize = 22f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onPrimaryContainer))
            gravity = Gravity.CENTER
            setSingleLine(false)
            maxLines = 6
            minHeight = 200
            setLineSpacing(8f, 1.2f)
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }

        questionCard.addView(questionText)

        // Options layout with improved spacing
        val optionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        fun createOptionButton(index: Int): Button = Button(this).apply {
            setOnClickListener {
                soundManager.playButtonSound()
                checkAnswer(index)
            }
            setPadding(32, 24, 32, 24)
            textSize = 17f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onPrimary))
            background = createOptionButtonBackground()
            elevation = 6f
            stateListAnimator = null // Remove default state animator for custom effects
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            letterSpacing = 0.01f

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
            minHeight = 80
            setSingleLine(false)
            maxLines = 3
        }

        option1Button = createOptionButton(0)
        option2Button = createOptionButton(1)
        option3Button = createOptionButton(2)
        option4Button = createOptionButton(3)

        optionsLayout.addView(option1Button)
        optionsLayout.addView(option2Button)
        optionsLayout.addView(option3Button)
        optionsLayout.addView(option4Button)

        // Action buttons layout with enhanced styling
        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)

            // Add bottom margin to ensure buttons don't get hidden by navigation bar
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 80) // Add 80dp bottom margin
            this.layoutParams = layoutParams
        }

        val backButton = Button(this).apply {
            text = "← Back"
            setOnClickListener {
                soundManager.playButtonSound()
                finish()
            }
            setPadding(28, 18, 28, 18)
            textSize = 15f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSecondary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)

            background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_secondary))
            }

            elevation = 6f
        }

        val restartButton = Button(this).apply {
            text = "🔄 Restart"
            setOnClickListener {
                soundManager.playButtonSound()
                restartQuiz()
            }
            setPadding(28, 18, 28, 18)
            textSize = 15f
            setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onTertiary))
            isAllCaps = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)

            background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary))
            }

            elevation = 6f

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(24, 0, 0, 0)
            this.layoutParams = layoutParams
        }

        actionLayout.addView(backButton)
        actionLayout.addView(restartButton)

        // Add all views to root layout
        rootLayout.addView(headerContainer)
        rootLayout.addView(progressContainer)
        rootLayout.addView(questionCard)
        rootLayout.addView(optionsLayout)
        rootLayout.addView(actionLayout)

        scrollView.addView(rootLayout)

        // Create loading overlay with spinner (initially hidden)
        loadingOverlay = createLoadingOverlay()

        // Create a frame layout to hold both scroll view and loading overlay
        val frameLayout = android.widget.FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        frameLayout.addView(scrollView)
        frameLayout.addView(loadingOverlay)

        setContentView(frameLayout)

        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top + 16,      // Add 16dp top margin
                bottom = systemBars.bottom + 16  // Add padding at the bottom to account for system bars
            )
            insets
        }
    }

    /**
     * Creates a loading overlay with animated spinner for question transitions
     */
    private fun createLoadingOverlay(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.overlay_color))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            alpha = 0f
            elevation = 100f // Ensure it's above all other views

            // Container for spinner and text
            val spinnerContainer = LinearLayout(this@QuizActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(48, 48, 48, 48)
                background = GradientDrawable().apply {
                    cornerRadius = 32f
                    setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainer))
                }
                elevation = 16f
            }

            // Loading spinner with Material 3 colors
            loadingSpinner = ProgressBar(this@QuizActivity).apply {
                indeterminateDrawable?.let { drawable ->
                    androidx.core.graphics.drawable.DrawableCompat.setTint(
                        drawable,
                        ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary)
                    )
                }
                layoutParams = LinearLayout.LayoutParams(
                    120,
                    120
                )
            }

            // Loading text
            val loadingText = TextView(this@QuizActivity).apply {
                text = "Loading next question..."
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurface))
                gravity = Gravity.CENTER
                setPadding(0, 24, 0, 0)
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            }

            spinnerContainer.addView(loadingSpinner)
            spinnerContainer.addView(loadingText)
            addView(spinnerContainer)
        }
    }

    /**
     * Shows the loading spinner with fade-in animation
     */
    private fun showLoadingSpinner() {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate()
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    /**
     * Hides the loading spinner with fade-out animation
     */
    private fun hideLoadingSpinner() {
        loadingOverlay.animate()
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                loadingOverlay.visibility = View.GONE
            }
            .start()
    }

    /**
     * Creates a detailed feedback card showing what the user selected vs the correct answer
     */
    private fun createFeedbackCard(
        userSelectedText: String,
        correctAnswerText: String,
        question: QuizQuestion
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainer))
                setStroke(3, ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_outline))
            }
            setPadding(24, 24, 24, 24)
            elevation = 8f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }

            // Title
            val titleText = TextView(this@QuizActivity).apply {
                text = "Answer Review"
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 16)
                gravity = Gravity.CENTER
            }
            addView(titleText)

            // Divider
            val divider1 = View(this@QuizActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_outline))
            }
            addView(divider1)

            // Your answer section (highlighted in red)
            val yourAnswerLabel = TextView(this@QuizActivity).apply {
                text = "Your Answer:"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_error))
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8)
            }
            addView(yourAnswerLabel)

            val yourAnswerCard = LinearLayout(this@QuizActivity).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    cornerRadius = 12f
                    colors = intArrayOf(
                        ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_error),
                        ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_errorContainer)
                    )
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }

                val yourAnswerText = TextView(this@QuizActivity).apply {
                    text = userSelectedText
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onError))
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                }
                addView(yourAnswerText)

                // Try to find the meaning of the user's incorrect choice
                val userAnswerPhrase = findPhraseByText(userSelectedText, question.isEnglishToKikuyu)
                if (userAnswerPhrase != null) {
                    val userAnswerMeaning = if (question.isEnglishToKikuyu) {
                        userAnswerPhrase.english
                    } else {
                        userAnswerPhrase.kikuyu
                    }

                    val meaningText = TextView(this@QuizActivity).apply {
                        text = "Meaning: $userAnswerMeaning"
                        textSize = 13f
                        setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onErrorContainer))
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.ITALIC)
                        setPadding(0, 8, 0, 0)
                    }
                    addView(meaningText)
                }
            }
            addView(yourAnswerCard)

            // Correct answer section (highlighted in green)
            val correctAnswerLabel = TextView(this@QuizActivity).apply {
                text = "Correct Answer:"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.success_green))
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8)
            }
            addView(correctAnswerLabel)

            val correctAnswerCard = LinearLayout(this@QuizActivity).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    cornerRadius = 12f
                    colors = intArrayOf(
                        ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary),
                        ContextCompat.getColor(this@QuizActivity, R.color.success_green)
                    )
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }

                val correctAnswerTextView = TextView(this@QuizActivity).apply {
                    text = correctAnswerText
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onTertiary))
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                }
                addView(correctAnswerTextView)

                // Show the correct translation/meaning
                val correctMeaning = if (question.isEnglishToKikuyu) {
                    question.phrase.english
                } else {
                    question.phrase.kikuyu
                }

                val meaningText = TextView(this@QuizActivity).apply {
                    text = "Means: $correctMeaning"
                    textSize = 13f
                    setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onTertiaryContainer))
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.ITALIC)
                    setPadding(0, 8, 0, 0)
                }
                addView(meaningText)
            }
            addView(correctAnswerCard)

            // Contextual information section
            if (question.phrase.category.isNotEmpty() ||
                question.phrase.context != null ||
                question.phrase.culturalNotes != null ||
                (question.phrase.examples != null && question.phrase.examples.isNotEmpty())) {

                val divider2 = View(this@QuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_outline))
                }
                addView(divider2)

                val contextLabel = TextView(this@QuizActivity).apply {
                    text = "Additional Information:"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_primary))
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, 12)
                }
                addView(contextLabel)

                // Category and difficulty
                val categoryText = TextView(this@QuizActivity).apply {
                    val categoryDisplay = question.phrase.category.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                    val difficultyEmoji = when (question.phrase.difficulty.lowercase()) {
                        "easy", "beginner" -> "🟢"
                        "medium", "intermediate" -> "🟠"
                        "hard", "advanced" -> "🔴"
                        else -> "⚪"
                    }
                    text = "Category: $categoryDisplay  |  Difficulty: $difficultyEmoji ${question.phrase.difficulty}"
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
                    setPadding(0, 0, 0, 8)
                }
                addView(categoryText)

                // Context if available
                question.phrase.context?.let { context ->
                    val contextText = TextView(this@QuizActivity).apply {
                        text = "Context: $context"
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
                        setPadding(0, 0, 0, 8)
                        setLineSpacing(4f, 1.1f)
                    }
                    addView(contextText)
                }

                // Cultural notes if available
                question.phrase.culturalNotes?.let { notes ->
                    val notesText = TextView(this@QuizActivity).apply {
                        text = "Cultural Note: $notes"
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary))
                        setPadding(0, 0, 0, 8)
                        setLineSpacing(4f, 1.1f)
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.ITALIC)
                    }
                    addView(notesText)
                }

                // Example if available
                question.phrase.examples?.firstOrNull()?.let { example ->
                    val exampleText = TextView(this@QuizActivity).apply {
                        text = "Example:\n${example.english}\n${example.kikuyu}"
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
                        setPadding(12, 8, 0, 0)
                        setLineSpacing(4f, 1.1f)
                        background = GradientDrawable().apply {
                            cornerRadius = 8f
                            setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceContainerHigh))
                        }
                        setPadding(12, 8, 12, 8)
                    }
                    addView(exampleText)
                }
            }
        }
    }

    /**
     * Helper method to find a phrase by its text (English or Kikuyu)
     */
    private fun findPhraseByText(text: String, isEnglishToKikuyu: Boolean): FlashcardEntry? {
        for (i in 0 until flashCardManager.getTotalEntries()) {
            flashCardManager.setCurrentIndex(i)
            val phrase = flashCardManager.getCurrentEntry()
            if (phrase != null) {
                val matchText = if (isEnglishToKikuyu) phrase.kikuyu else phrase.english
                if (matchText == text) {
                    return phrase
                }
            }
        }
        return null
    }

    /**
     * Creates a custom progress bar drawable with gradient
     */
    private fun createProgressBarDrawable(): LayerDrawable {
        // Background track
        val backgroundDrawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceVariant))
            cornerRadius = 8f
        }

        // Progress gradient
        val progressDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                ContextCompat.getColor(this, R.color.md_theme_dark_tertiary),
                ContextCompat.getColor(this, R.color.md_theme_dark_primary)
            )
        ).apply {
            cornerRadius = 8f
        }

        return LayerDrawable(arrayOf(backgroundDrawable, progressDrawable)).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }
    }

    private fun startQuiz() {
        currentQuestionIndex = 0
        score = 0
        progressBar.progress = 0

        // Generate new quiz ID for this session
        quizId = "quiz_${System.currentTimeMillis()}"

        // Clear answered questions history
        answeredQuestions.clear()

        generateNextQuestion()
    }

    private fun restartQuiz() {
        // Clear saved quiz state before restarting
        quizStateManager.clearQuizState()
        startQuiz()
        Toast.makeText(this, "Quiz restarted!", Toast.LENGTH_SHORT).show()
    }

    private fun generateNextQuestion() {
        if (currentQuestionIndex >= quizLength) {
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
        progressText.text = "Question ${currentQuestionIndex + 1} of $quizLength"
        progressBar.progress = currentQuestionIndex
        scoreText.text = "Score: $score / ${if (currentQuestionIndex == 0) 0 else currentQuestionIndex}"

        // Set button text and reset state with original styling
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        buttons.forEachIndexed { index, button ->
            if (index < options.size) {
                button.text = options[index]
                button.isEnabled = true
                button.background = createOptionButtonBackground()
                button.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_onPrimary))
                button.elevation = 6f
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
        val accuracy = if (quizLength > 0) (score * 100) / quizLength else 0
        val statsMessage = """
            🎉 Quiz Complete!

            Final Score: $score / $quizLength
            Accuracy: $accuracy%

            Overall Progress:
            Total Quiz Answers: ${progressManager.getQuizTotalAnswered()}
            Overall Accuracy: ${"%.1f".format(progressManager.getQuizAccuracy())}%
            Current Streak: ${progressManager.getCurrentStreak()}
            Best Streak: ${progressManager.getBestStreak()}
        """.trimIndent()

        questionText.text = statsMessage
        progressText.text = "Quiz Finished"
        progressBar.progress = quizLength
        scoreText.text = "Great job! 🎊"

        // Hide option buttons
        listOf(option1Button, option2Button, option3Button, option4Button).forEach {
            it.text = ""
            it.isEnabled = false
        }

        // Archive completed quiz and clear state
        if (quizId != null) {
            val completedState = QuizState(
                quizId = quizId!!,
                quizLength = quizLength,
                currentQuestionIndex = quizLength,
                score = score,
                questionIds = quizQuestions.map { it.id },
                answeredQuestions = answeredQuestions,
                timestamp = System.currentTimeMillis(),
                isCompleted = true
            )
            quizStateManager.archiveCompletedQuiz(completedState)
            Log.d(TAG, "Quiz completed and archived")
        }

        Toast.makeText(this, "Quiz completed! Check your stats.", Toast.LENGTH_LONG).show()
    }

    private fun checkAnswer(selectedOptionIndex: Int) {
        val question = currentQuestion ?: return
        val isCorrect = selectedOptionIndex == correctAnswerIndex

        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        val selectedButton = buttons[selectedOptionIndex]

        // Record the answered question
        val answeredQuestion = AnsweredQuestion(
            questionId = question.phrase.id,
            questionText = question.questionText,
            selectedAnswer = question.options[selectedOptionIndex],
            correctAnswer = question.correctAnswer,
            isCorrect = isCorrect,
            timestamp = System.currentTimeMillis()
        )
        answeredQuestions.add(answeredQuestion)

        // Disable all buttons
        buttons.forEach { it.isEnabled = false }

        // Update button styles with better colors and enhanced visual feedback
        buttons.forEachIndexed { index, button ->
            val background = GradientDrawable().apply {
                cornerRadius = 20f
                when {
                    index == correctAnswerIndex -> {
                        // Correct answer - gradient green with improved visibility
                        colors = intArrayOf(
                            ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_tertiary),
                            ContextCompat.getColor(this@QuizActivity, R.color.success_green)
                        )
                        gradientType = GradientDrawable.LINEAR_GRADIENT
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT
                        button.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onTertiary))
                        button.elevation = 10f
                    }
                    index == selectedOptionIndex && !isCorrect -> {
                        // Incorrect selection - gradient red with improved visibility
                        colors = intArrayOf(
                            ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_error),
                            ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_errorContainer)
                        )
                        gradientType = GradientDrawable.LINEAR_GRADIENT
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT
                        button.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onError))
                        button.elevation = 10f
                    }
                    else -> {
                        // Other options - dim them
                        setColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_surfaceVariant))
                        button.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.md_theme_dark_onSurfaceVariant))
                        button.elevation = 2f
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

            // Remove any existing feedback card
            feedbackCard?.let { card ->
                (card.parent as? ViewGroup)?.removeView(card)
            }

            // Create and show detailed feedback card for incorrect answers
            val userSelectedText = question.options[selectedOptionIndex]
            val correctAnswerText = question.correctAnswer
            feedbackCard = createFeedbackCard(userSelectedText, correctAnswerText, question)

            // Find the options layout and add feedback card below it
            // The content view is a FrameLayout containing the ScrollView
            val frameLayout = findViewById<android.widget.FrameLayout>(android.R.id.content)
            val scrollView = frameLayout?.getChildAt(0) as? ScrollView
            val rootLayout = scrollView?.getChildAt(0) as? LinearLayout

            rootLayout?.let { root ->
                // Find the index of the options layout (which contains the option buttons)
                var optionsLayoutIndex = -1
                for (i in 0 until root.childCount) {
                    val child = root.getChildAt(i)
                    if (child is LinearLayout) {
                        // Check if this layout contains our option buttons
                        val containsButtons = (0 until child.childCount).any { j ->
                            val grandchild = child.getChildAt(j)
                            grandchild == option1Button || grandchild == option2Button ||
                            grandchild == option3Button || grandchild == option4Button
                        }
                        if (containsButtons) {
                            optionsLayoutIndex = i
                            break
                        }
                    }
                }

                // Add feedback card after the options layout
                if (optionsLayoutIndex >= 0 && optionsLayoutIndex < root.childCount - 1) {
                    root.addView(feedbackCard, optionsLayoutIndex + 1)

                    // Animate the feedback card entrance
                    feedbackCard?.let { card ->
                        card.alpha = 0f
                        card.scaleY = 0.8f
                        card.animate()
                            .alpha(1f)
                            .scaleY(1f)
                            .setDuration(400)
                            .setInterpolator(android.view.animation.OvershootInterpolator())
                            .start()

                        // Scroll to show the feedback card
                        scrollView?.postDelayed({
                            scrollView.smoothScrollTo(0, card.top)
                        }, 100)
                    }
                }
            }
        }

        currentQuestionIndex++

        // Move to next question after a longer delay to allow reading feedback
        val delayTime = if (isCorrect) 2000L else 4500L // Longer delay for wrong answers
        selectedButton.postDelayed({
            // Remove feedback card before transitioning
            feedbackCard?.let { card ->
                card.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        (card.parent as? ViewGroup)?.removeView(card)
                        feedbackCard = null
                    }
                    .start()
            }

            // Show loading spinner after feedback card fades
            selectedButton.postDelayed({
                showLoadingSpinner()

                // Wait for the spinner animation (700ms delay for smooth UX)
                selectedButton.postDelayed({
                    generateNextQuestion()
                    hideLoadingSpinner()
                }, 700)
            }, 250)
        }, delayTime)
    }

    override fun onPause() {
        super.onPause()
        // Save quiz state when pausing (e.g., user presses home or switches apps)
        saveQuizState()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
        // Save quiz state when destroying (e.g., user presses back)
        saveQuizState()
    }

    /**
     * Saves the current quiz state for later resumption
     */
    private fun saveQuizState() {
        // Only save if we have an active quiz and it's not completed
        if (quizId == null || currentQuestionIndex >= quizLength) {
            return
        }

        val state = QuizState(
            quizId = quizId!!,
            quizLength = quizLength,
            currentQuestionIndex = currentQuestionIndex,
            score = score,
            questionIds = quizQuestions.map { it.id },
            answeredQuestions = answeredQuestions,
            timestamp = System.currentTimeMillis(),
            isCompleted = false
        )

        quizStateManager.saveQuizState(state)
        Log.d(TAG, "Quiz state saved: ${state.currentQuestionIndex}/${state.quizLength}")
    }
}
