package com.nkmathew.kikuyuflashcards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.services.FlagStorageService
import com.nkmathew.kikuyuflashcards.ui.adapters.StudyCardAdapter

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
    private lateinit var exportButton: Button
    private lateinit var shareButton: Button
    private lateinit var studyCardAdapter: StudyCardAdapter

    // Managers
    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var flagStorageService: FlagStorageService

    // State
    private val knownCards = mutableSetOf<String>()
    private val flaggedCards = mutableSetOf<String>()
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
        exportButton = findViewById(R.id.exportButton)
        shareButton = findViewById(R.id.shareButton)

        // Initialize managers
        flashCardManager = FlashCardManagerV2(this)
        flagStorageService = FlagStorageService(this)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up category filter chips
        setupCategoryChips()

        // Set up difficulty filter chips
        setupDifficultyChips()

        // Set up export buttons
        setupExportButtons()

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
        studyCardAdapter = StudyCardAdapter(
            onCardStatusChanged = { cardId, isKnown ->
                if (isKnown) {
                    knownCards.add(cardId)
                } else {
                    knownCards.remove(cardId)
                }
                updateProgress()
            },
            onCardFlagged = { cardId ->
                if (flaggedCards.contains(cardId)) {
                    // Unflag the card
                    flaggedCards.remove(cardId)
                    flagStorageService.unflagCard(cardId)
                } else {
                    // Flag the card
                    flaggedCards.add(cardId)
                    flagStorageService.flagCard(cardId)
                }
                studyCardAdapter.setFlaggedCards(flaggedCards)
            }
        )

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
        
        // Load flagged cards
        flaggedCards.clear()
        flaggedCards.addAll(flagStorageService.getFlaggedItems())
        
        studyCardAdapter.updateCards(cards)
        studyCardAdapter.setFlaggedCards(flaggedCards)
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
     * Set up export buttons
     */
    private fun setupExportButtons() {
        exportButton.setOnClickListener {
            copyAllToClipboard()
        }

        shareButton.setOnClickListener {
            shareAsEmail()
        }
    }

    /**
     * Copy all cards to clipboard
     */
    private fun copyAllToClipboard() {
        val cards = flashCardManager.getAllEntries()
        if (cards.isEmpty()) {
            Toast.makeText(this, "No cards to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cards.joinToString("\n\n") { card ->
            "[${card.category}] ${card.id}\n${card.kikuyu} - ${card.english}\nDifficulty: ${card.difficulty}"
        }

        val clip = ClipData.newPlainText("Kikuyu Flashcards", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "${cards.size} cards copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /**
     * Share cards as email
     */
    private fun shareAsEmail() {
        val cards = flashCardManager.getAllEntries()
        if (cards.isEmpty()) {
            Toast.makeText(this, "No cards to share", Toast.LENGTH_SHORT).show()
            return
        }

        val emailBody = "Kikuyu Flashcards (${cards.size} items):\n\n" +
                cards.joinToString("\n") { card ->
                    "• [${card.category}] ${card.id}\n  ${card.kikuyu} - ${card.english}\n  Difficulty: ${card.difficulty}"
                }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, emailBody)
            putExtra(Intent.EXTRA_SUBJECT, "Kikuyu Flashcards")
        }

        startActivity(Intent.createChooser(intent, "Share Kikuyu Flashcards"))
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
