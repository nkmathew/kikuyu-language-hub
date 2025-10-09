package com.nkmathew.kikuyuflashcards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var difficultyDropdown: AutoCompleteTextView
    private lateinit var progressTextView: TextView
    private lateinit var exportButton: Button
    private lateinit var shareButton: Button
    private lateinit var sortDropdown: AutoCompleteTextView
    private lateinit var studyCardAdapter: StudyCardAdapter

    // Managers
    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var flagStorageService: FlagStorageService

    // State
    private val knownCards = mutableSetOf<String>()
    private val flaggedCards = mutableSetOf<String>()
    private var totalCards = 0
    private var cardsStudied = 0
    private var currentSortMode = "default" // default, short_first, long_first

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_list)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        // Set up action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Study Mode"
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        categoryDropdown = findViewById(R.id.categoryDropdown)
        difficultyDropdown = findViewById(R.id.difficultyDropdown)
        progressTextView = findViewById(R.id.progressTextView)
        exportButton = findViewById(R.id.exportButton)
        shareButton = findViewById(R.id.shareButton)
        sortDropdown = findViewById(R.id.sortDropdown)

        // Initialize managers
        flashCardManager = FlashCardManagerV2(this)
        flagStorageService = FlagStorageService(this)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up category filter dropdown
        setupCategoryDropdown()

        // Set up difficulty filter dropdown
        setupDifficultyDropdown()

        // Set up export buttons
        setupExportButtons()

        // Set up sort dropdown
        setupSortDropdown()

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
     * Set up category filter dropdown
     */
    private fun setupCategoryDropdown() {
        val categories = mutableListOf("All Categories")
        val categoryMap = mutableMapOf<String, String?>()
        categoryMap["All Categories"] = null
        
        flashCardManager.getAvailableCategories().forEach { category ->
            val displayName = Categories.getCategoryDisplayName(category)
            categories.add(displayName)
            categoryMap[displayName] = category
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)
        categoryDropdown.setText("All Categories", false)

        categoryDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categories[position]
            val category = categoryMap[selectedCategory]
            
            // Apply category filter
            flashCardManager.setCategory(category)
            
            // Reload cards
            loadCards()
        }
    }

    /**
     * Set up difficulty filter dropdown
     */
    private fun setupDifficultyDropdown() {
        val difficulties = mutableListOf("All Levels")
        val difficultyMap = mutableMapOf<String, String?>()
        difficultyMap["All Levels"] = null
        
        flashCardManager.getAvailableDifficulties().forEach { difficulty ->
            val displayName = DifficultyLevels.getDifficultyDisplayName(difficulty)
            difficulties.add(displayName)
            difficultyMap[displayName] = difficulty
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, difficulties)
        difficultyDropdown.setAdapter(adapter)
        difficultyDropdown.setText("All Levels", false)

        difficultyDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedDifficulty = difficulties[position]
            val difficulty = difficultyMap[selectedDifficulty]
            
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
        var cards = flashCardManager.getAllEntries()
        
        // Apply sorting based on current sort mode
        cards = when (currentSortMode) {
            "short_first" -> cards.sortedBy { it.kikuyu.length + it.english.length }
            "long_first" -> cards.sortedByDescending { it.kikuyu.length + it.english.length }
            else -> cards // default order
        }
        
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
     * Set up sort dropdown
     */
    private fun setupSortDropdown() {
        val sortOptions = listOf("Default", "Short First", "Long First")
        val sortMap = mapOf(
            "Default" to "default",
            "Short First" to "short_first", 
            "Long First" to "long_first"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sortOptions)
        sortDropdown.setAdapter(adapter)
        sortDropdown.setText("Default", false)

        sortDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedSort = sortOptions[position]
            currentSortMode = sortMap[selectedSort] ?: "default"
            loadCards() // Reload cards with new sort
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

}
