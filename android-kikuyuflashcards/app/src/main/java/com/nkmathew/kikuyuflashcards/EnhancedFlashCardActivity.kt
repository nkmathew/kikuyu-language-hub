package com.nkmathew.kikuyuflashcards

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.ui.views.StudyCardView

/**
 * Activity that displays enhanced flashcards with filtering capabilities
 */
class EnhancedFlashCardActivity : AppCompatActivity() {

    // Views
    private lateinit var studyCardView: StudyCardView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var difficultyChipGroup: ChipGroup
    private lateinit var positionTextView: TextView

    // Managers
    private lateinit var flashCardManager: FlashCardManagerV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enhanced_flashcard)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        // Set up action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Enhanced Flashcards"
        }

        // Initialize views
        studyCardView = findViewById(R.id.studyCardView)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        difficultyChipGroup = findViewById(R.id.difficultyChipGroup)
        positionTextView = findViewById(R.id.positionTextView)

        // Initialize flash card manager
        flashCardManager = FlashCardManagerV2(this)

        // Set up category filter chips
        setupCategoryChips()

        // Set up difficulty filter chips
        setupDifficultyChips()

        // Set up flashcard listeners
        setupFlashcardListeners()

        // Show initial card
        updateCurrentCard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.enhanced_flashcard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_list_mode -> {
                // Switch to list mode
                val intent = Intent(this, StudyListActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Set up category filter chips
     */
    private fun setupCategoryChips() {
        // Add "All Categories" chip
        addChipToGroup(
            categoryChipGroup,
            "All Categories",
            null,
            true
        )

        // Add a chip for each available category
        flashCardManager.getAvailableCategories().forEach { category ->
            addChipToGroup(
                categoryChipGroup,
                Categories.getCategoryDisplayName(category),
                category,
                false
            )
        }

        // Set listener for category chip selection
        categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            // Get the selected chip
            val chip = findViewById<Chip>(checkedIds[0])
            val category = chip.tag as String?

            // Apply category filter
            flashCardManager.setCategory(category)

            // Update card
            updateCurrentCard()
        }
    }

    /**
     * Set up difficulty filter chips
     */
    private fun setupDifficultyChips() {
        // Add "All Levels" chip
        addChipToGroup(
            difficultyChipGroup,
            "All Levels",
            null,
            true
        )

        // Add a chip for each difficulty level
        flashCardManager.getAvailableDifficulties().forEach { difficulty ->
            addChipToGroup(
                difficultyChipGroup,
                DifficultyLevels.getDifficultyDisplayName(difficulty),
                difficulty,
                false
            )
        }

        // Set listener for difficulty chip selection
        difficultyChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            // Get the selected chip
            val chip = findViewById<Chip>(checkedIds[0])
            val difficulty = chip.tag as String?

            // Apply difficulty filter
            flashCardManager.setDifficulty(difficulty)

            // Update card
            updateCurrentCard()
        }
    }

    /**
     * Set up flashcard listeners
     */
    private fun setupFlashcardListeners() {
        // Set up next button
        studyCardView.onMarkKnownListener = {
            flashCardManager.getNextEntry()
            updateCurrentCard()
        }

        // Set up previous button
        studyCardView.onMarkUnknownListener = {
            flashCardManager.getPreviousEntry()
            updateCurrentCard()
        }
    }

    /**
     * Helper to add a chip to a ChipGroup
     */
    private fun addChipToGroup(
        chipGroup: ChipGroup,
        text: String,
        tag: String?,
        isChecked: Boolean
    ) {
        val chip = Chip(this).apply {
            this.text = text
            this.tag = tag
            this.isCheckable = true
            this.isChecked = isChecked
        }
        chipGroup.addView(chip)
    }

    /**
     * Update the UI with the current card
     */
    private fun updateCurrentCard() {
        // Get current entry
        val currentEntry = flashCardManager.getCurrentEntry()

        // Update position text
        val currentIndex = flashCardManager.getCurrentIndex() + 1
        val totalEntries = flashCardManager.getTotalEntries()
        positionTextView.text = "Card $currentIndex of $totalEntries"

        // Update flash card view
        currentEntry?.let {
            studyCardView.setEntry(it)
        }
    }
}