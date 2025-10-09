package com.nkmathew.kikuyuflashcards

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.FlashCardManager
import com.nkmathew.kikuyuflashcards.SoundManager
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.FailureTracker

/**
 * ProblemWordsActivity - Displays words that users struggle with most
 * 
 * Features:
 * - Sort problem words by failure count, recency, or category
 * - Filter words by failure type or difficulty
 * - Practice sessions focused on problem words
 * - Progress tracking and improvement visualization
 * - Word mastery management
 */
class ProblemWordsActivity : AppCompatActivity() {
    
    private lateinit var failureTracker: FailureTracker
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    
    // UI Components
    private lateinit var titleText: TextView
    private lateinit var statsText: TextView
    private lateinit var filterSpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var wordsContainer: LinearLayout
    private lateinit var practiceButton: Button
    private lateinit var backButton: Button
    private lateinit var clearDataButton: Button
    
    // Data
    private var problemWords: List<FailureTracker.DifficultyWord> = emptyList()
    private var currentFilter = FilterType.ALL_WORDS
    private var currentSort = SortType.FAILURE_COUNT
    
    enum class FilterType {
        ALL_WORDS,
        RECENT_FAILURES,
        TRANSLATION_ERRORS,
        RECALL_ERRORS,
        NEEDING_ATTENTION,
        BY_CATEGORY
    }
    
    enum class SortType {
        FAILURE_COUNT,
        RECENT_FAILURES,
        ALPHABETICAL,
        CATEGORY,
        MASTERY_LEVEL
    }
    
    companion object {
        private const val TAG = "ProblemWordsActivity"
        private const val PRACTICE_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        failureTracker = FailureTracker(this)
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        
        setContentView(createLayout())
        
        setupSpinners()
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
        val headerContainer = createHeader()
        
        // Stats section
        val statsContainer = createStatsSection()
        
        // Filter and sort controls
        val controlsContainer = createControlsSection()
        
        // Words list
        wordsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }
        
        // Button section
        val buttonContainer = createButtonSection()
        
        mainContainer.addView(headerContainer)
        mainContainer.addView(statsContainer)
        mainContainer.addView(controlsContainer)
        mainContainer.addView(wordsContainer)
        mainContainer.addView(buttonContainer)
        
        rootLayout.addView(mainContainer)
        return rootLayout
    }
    
    private fun createHeader(): LinearLayout {
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        
        titleText = TextView(this).apply {
            text = "📚 Problem Words"
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
        }
        
        val subtitleText = TextView(this).apply {
            text = "Focus on words that need extra practice"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.text_secondary))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        
        headerContainer.addView(titleText)
        headerContainer.addView(subtitleText)
        
        return headerContainer
    }
    
    private fun createStatsSection(): LinearLayout {
        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = createStatsBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        statsText = TextView(this).apply {
            text = "Loading statistics..."
            textSize = 14f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        
        statsContainer.addView(statsText)
        return statsContainer
    }
    
    private fun createStatsBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_surfaceVariant))
            setStroke(1, ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_outline))
        }
    }
    
    private fun createControlsSection(): LinearLayout {
        val controlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Filter spinner
        val filterContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 8, 0)
            }
        }
        
        val filterLabel = TextView(this).apply {
            text = "Filter:"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.text_secondary))
            gravity = Gravity.CENTER
        }
        
        filterSpinner = Spinner(this).apply {
            background = createSpinnerBackground()
        }
        
        filterContainer.addView(filterLabel)
        filterContainer.addView(filterSpinner)
        
        // Sort spinner
        val sortContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 0, 0, 0)
            }
        }
        
        val sortLabel = TextView(this).apply {
            text = "Sort by:"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.text_secondary))
            gravity = Gravity.CENTER
        }
        
        sortSpinner = Spinner(this).apply {
            background = createSpinnerBackground()
        }
        
        sortContainer.addView(sortLabel)
        sortContainer.addView(sortSpinner)
        
        controlsContainer.addView(filterContainer)
        controlsContainer.addView(sortContainer)
        
        return controlsContainer
    }
    
    private fun createSpinnerBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(Color.WHITE)
            setStroke(1, ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_secondary))
        }
    }
    
    private fun createButtonSection(): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        
        // Practice button
        practiceButton = Button(this).apply {
            text = "🎯 Practice Problem Words"
            textSize = 18f
            setOnClickListener { 
                animateButtonPress(this)
                startProblemWordsPractice() 
            }
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.success_green)
            isEnabled = false // Will be enabled when there are problem words
        }
        
        // Action buttons row
        val actionButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        
        clearDataButton = Button(this).apply {
            text = "🗑️ Clear Data"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                showClearDataConfirmation() 
            }
            setPadding(20, 12, 20, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_error)
        }
        
        backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 14f
            setOnClickListener { 
                animateButtonPress(this)
                finish() 
            }
            setPadding(20, 12, 20, 12)
            setTextColor(Color.WHITE)
            background = createButtonBackground(R.color.md_theme_light_outline)
        }
        
        actionButtonsContainer.addView(clearDataButton)
        actionButtonsContainer.addView(backButton)
        
        buttonContainer.addView(practiceButton)
        buttonContainer.addView(actionButtonsContainer)
        
        return buttonContainer
    }
    
    private fun createButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@ProblemWordsActivity, colorRes))
        }
    }
    
    private fun setupSpinners() {
        // Setup filter spinner
        val filterOptions = listOf(
            "All Problem Words",
            "Recent Failures",
            "Translation Errors",
            "Recall Errors", 
            "Need Attention",
            "By Category"
        )
        
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filterAdapter
        
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = FilterType.values()[position]
                refreshWordList()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup sort spinner
        val sortOptions = listOf(
            "Failure Count",
            "Recent Failures",
            "Alphabetical",
            "Category",
            "Mastery Level"
        )
        
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter
        
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = SortType.values()[position]
                refreshWordList()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun loadProblemWords() {
        // Load words based on current filter
        problemWords = when (currentFilter) {
            FilterType.ALL_WORDS -> failureTracker.getProblemWords(50)
            FilterType.RECENT_FAILURES -> getRecentProblemWords()
            FilterType.TRANSLATION_ERRORS -> failureTracker.getWordsByFailureType(FailureTracker.FailureType.TRANSLATION_ERROR)
            FilterType.RECALL_ERRORS -> failureTracker.getWordsByFailureType(FailureTracker.FailureType.RECALL_ERROR)
            FilterType.NEEDING_ATTENTION -> failureTracker.getWordsNeedingAttention(20)
            FilterType.BY_CATEGORY -> getWordsByCategory()
        }
        
        // Sort words based on current sort
        problemWords = sortWords(problemWords)
        
        // Update UI
        updateStatsDisplay()
        refreshWordList()
    }
    
    private fun getRecentProblemWords(): List<FailureTracker.DifficultyWord> {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000)
        
        return failureTracker.getProblemWords(50)
            .filter { it.lastFailure > threeDaysAgo }
    }
    
    private fun getWordsByCategory(): List<FailureTracker.DifficultyWord> {
        // For now, get all words and let user see categories
        // In a real implementation, you might show category selection first
        return failureTracker.getProblemWords(50)
    }
    
    private fun sortWords(words: List<FailureTracker.DifficultyWord>): List<FailureTracker.DifficultyWord> {
        return when (currentSort) {
            SortType.FAILURE_COUNT -> words.sortedByDescending { it.failureCount }
            SortType.RECENT_FAILURES -> words.sortedByDescending { it.lastFailure }
            SortType.ALPHABETICAL -> words.sortedBy { it.englishText.lowercase() }
            SortType.CATEGORY -> words.sortedBy { it.category.lowercase() }
            SortType.MASTERY_LEVEL -> words.sortedBy { it.masteryLevel }
        }
    }
    
    private fun updateStatsDisplay() {
        val stats = failureTracker.getFailureStats()
        val totalProblemWords = problemWords.size
        
        val statsText = buildString {
            appendLine("📊 Learning Statistics")
            appendLine()
            appendLine("Total Problem Words: $totalProblemWords")
            appendLine("Total Failures: ${stats["total_failures"]}")
            appendLine("Recent Failures (24h): ${stats["recent_failures_24h"]}")
            
            val mostCommonType = stats["most_common_failure_type"]
            if (mostCommonType != null) {
                appendLine("Common Issue: ${mostCommonType.toString().replace("_", " ").lowercase()}")
            }
        }
        
        this.statsText.text = statsText
        
        // Enable practice button if there are problem words
        practiceButton.isEnabled = totalProblemWords > 0
    }
    
    private fun refreshWordList() {
        wordsContainer.removeAllViews()
        
        if (problemWords.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "🎉 No problem words found!\n\nKeep up the great work!"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.success_green))
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 48)
            }
            wordsContainer.addView(emptyText)
            return
        }
        
        problemWords.forEach { word ->
            val wordCard = createProblemWordCard(word)
            wordsContainer.addView(wordCard)
        }
        
        Log.d(TAG, "Displayed ${problemWords.size} problem words")
    }
    
    private fun createProblemWordCard(word: FailureTracker.DifficultyWord): LinearLayout {
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = createWordCardBackground(word.masteryLevel)
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        // Word content
        val wordContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // English and Kikuyu text
        val englishText = TextView(this).apply {
            text = "🇬🇧 ${word.englishText}"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.BLACK)
        }
        
        val kikuyuText = TextView(this).apply {
            text = "🇰🇪 ${word.kikuyuText}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_secondary))
            setPadding(0, 4, 0, 0)
        }
        
        // Category and stats
        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 0)
        }
        
        val categoryText = TextView(this).apply {
            text = "📁 ${word.category}"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.text_secondary))
        }
        
        val failureCountText = TextView(this).apply {
            text = "❌ ${word.failureCount} failures"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.md_theme_light_error))
            setPadding(16, 0, 0, 0)
        }
        
        val masteryText = TextView(this).apply {
            text = "📈 ${getMasteryLevelDisplay(word.masteryLevel)}"
            textSize = 12f
            setTextColor(getMasteryLevelColor(word.masteryLevel))
            setPadding(16, 0, 0, 0)
        }
        
        statsRow.addView(categoryText)
        statsRow.addView(failureCountText)
        statsRow.addView(masteryText)
        
        // Failure types (if any)
        if (word.failureTypes.isNotEmpty()) {
            val failureTypesText = TextView(this).apply {
                text = "⚠️ Issues: ${word.failureTypes.joinToString(", ") { it.toString().replace("_", " ").lowercase() }}"
                textSize = 11f
                setTextColor(ContextCompat.getColor(this@ProblemWordsActivity, R.color.warning_orange))
                setPadding(0, 4, 0, 0)
            }
            wordContent.addView(failureTypesText)
        }
        
        // Action buttons
        val actionButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 12, 0, 0)
        }
        
        val practiceWordButton = Button(this).apply {
            text = "📖 Practice"
            textSize = 12f
            setOnClickListener { 
                animateButtonPress(this)
                practiceSingleWord(word) 
            }
            setPadding(16, 8, 16, 8)
            setTextColor(Color.WHITE)
            background = createSmallButtonBackground(R.color.md_theme_light_secondary)
        }
        
        val markMasteredButton = Button(this).apply {
            text = "✅ Mastered"
            textSize = 12f
            setOnClickListener { 
                animateButtonPress(this)
                markWordAsMastered(word) 
            }
            setPadding(16, 8, 16, 8)
            setTextColor(Color.WHITE)
            background = createSmallButtonBackground(R.color.success_green)
        }
        
        actionButtons.addView(practiceWordButton)
        actionButtons.addView(markMasteredButton)
        
        wordContent.addView(englishText)
        wordContent.addView(kikuyuText)
        wordContent.addView(statsRow)
        wordContent.addView(actionButtons)
        
        cardContainer.addView(wordContent)
        return cardContainer
    }
    
    private fun createWordCardBackground(masteryLevel: FailureTracker.MasteryLevel): GradientDrawable {
        val color = when (masteryLevel) {
            FailureTracker.MasteryLevel.STRUGGLING -> ContextCompat.getColor(this, R.color.md_theme_light_error)
            FailureTracker.MasteryLevel.CHALLENGING -> ContextCompat.getColor(this, R.color.warning_orange)
            FailureTracker.MasteryLevel.LEARNING -> ContextCompat.getColor(this, R.color.info_blue)
            FailureTracker.MasteryLevel.MASTERED -> ContextCompat.getColor(this, R.color.success_green)
        }
        
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(Color.WHITE)
            setStroke(3, color)
        }
    }
    
    private fun createSmallButtonBackground(colorRes: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@ProblemWordsActivity, colorRes))
        }
    }
    
    private fun getMasteryLevelDisplay(level: FailureTracker.MasteryLevel): String {
        return when (level) {
            FailureTracker.MasteryLevel.STRUGGLING -> "Struggling"
            FailureTracker.MasteryLevel.CHALLENGING -> "Challenging"
            FailureTracker.MasteryLevel.LEARNING -> "Learning"
            FailureTracker.MasteryLevel.MASTERED -> "Mastered"
        }
    }
    
    private fun getMasteryLevelColor(level: FailureTracker.MasteryLevel): Int {
        return when (level) {
            FailureTracker.MasteryLevel.STRUGGLING -> ContextCompat.getColor(this, R.color.md_theme_light_error)
            FailureTracker.MasteryLevel.CHALLENGING -> ContextCompat.getColor(this, R.color.warning_orange)
            FailureTracker.MasteryLevel.LEARNING -> ContextCompat.getColor(this, R.color.info_blue)
            FailureTracker.MasteryLevel.MASTERED -> ContextCompat.getColor(this, R.color.success_green)
        }
    }
    
    private fun startProblemWordsPractice() {
        if (problemWords.isEmpty()) {
            Toast.makeText(this, "No problem words to practice", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, ProblemWordsPracticeActivity::class.java)
        intent.putExtra("practice_mode", "problem_words")
        startActivityForResult(intent, PRACTICE_REQUEST_CODE)
        
        soundManager.playButtonSound()
    }
    
    private fun practiceSingleWord(word: FailureTracker.DifficultyWord) {
        val intent = Intent(this, FlashCardActivity::class.java)
        intent.putExtra("focus_phrase_english", word.englishText)
        intent.putExtra("focus_phrase_kikuyu", word.kikuyuText)
        intent.putExtra("practice_mode", "single_problem_word")
        startActivity(intent)
        
        soundManager.playButtonSound()
    }
    
    private fun markWordAsMastered(word: FailureTracker.DifficultyWord) {
        val confirmationDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Mark as Mastered")
            .setMessage("Are you sure you've mastered \"${word.englishText}\"?\n\nThis will clear all failure history for this word.")
            .setPositiveButton("Yes, Mark as Mastered") { _, _ ->
                failureTracker.clearFailuresForWord(word.phraseId)
                loadProblemWords() // Refresh the list
                
                Toast.makeText(this, "✅ Word marked as mastered!", Toast.LENGTH_SHORT).show()
                soundManager.playButtonSound()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        confirmationDialog.show()
    }
    
    private fun showClearDataConfirmation() {
        val confirmationDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all failure tracking data.\n\nThis action cannot be undone.\n\nAre you sure?")
            .setPositiveButton("Clear All Data") { _, _ ->
                failureTracker.resetAllData()
                loadProblemWords()
                
                Toast.makeText(this, "🗑️ All failure data cleared", Toast.LENGTH_SHORT).show()
                soundManager.playButtonSound()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        confirmationDialog.show()
    }
    
    private fun animateButtonPress(button: Button) {
        val animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f)
        animator.duration = 100
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PRACTICE_REQUEST_CODE) {
            // Refresh the problem words list after practice session
            loadProblemWords()
        }
    }
}