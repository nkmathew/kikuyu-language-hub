package com.nkmathew.kikuyuflashcards

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.ui.adapters.StudyCardAdapter
import com.nkmathew.kikuyuflashcards.ui.views.StudyCardView

/**
 * Activity that displays flashcards in list mode similar to Next.js app
 * Shows all cards with English and Kikuyu side by side
 */
class StudyListActivity : AppCompatActivity() {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var difficultyChipGroup: ChipGroup
    private lateinit var progressTextView: TextView
    private lateinit var studyCardAdapter: StudyCardAdapter

    // Managers
    private lateinit var flashCardManager: FlashCardManagerV2

    // State
    private val knownCards = mutableSetOf<String>()
    private var totalCards = 0
    private var cardsStudied = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_list)

        // Set up action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Study Mode"
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        difficultyChipGroup = findViewById(R.id.difficultyChipGroup)
        progressTextView = findViewById(R.id.progressTextView)

        // Initialize flash card manager
        flashCardManager = FlashCardManagerV2(this)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up category filter chips
        setupCategoryChips()

        // Set up difficulty filter chips
        setupDifficultyChips()

        // Load initial cards
        loadCards()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Set up RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        studyCardAdapter = StudyCardAdapter { cardId, isKnown ->
            if (isKnown) {
                knownCards.add(cardId)
            } else {
                knownCards.remove(cardId)
            }
            updateProgress()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studyCardAdapter
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

            // Reload cards
            loadCards()
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

            // Reload cards
            loadCards()
        }
    }

    /**
     * Load cards and update the adapter
     */
    private fun loadCards() {
        val cards = flashCardManager.getAllEntries()
        totalCards = cards.size
        cardsStudied = 0
        
        studyCardAdapter.updateCards(cards)
        updateProgress()
    }

    /**
     * Update progress display
     */
    private fun updateProgress() {
        val knownCount = knownCards.size
        val progressPercentage = if (totalCards > 0) (knownCount * 100) / totalCards else 0
        
        progressTextView.text = "Progress: $knownCount/$totalCards cards known ($progressPercentage%)"
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
}
