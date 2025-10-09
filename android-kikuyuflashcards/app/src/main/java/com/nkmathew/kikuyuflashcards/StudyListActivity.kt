package com.nkmathew.kikuyuflashcards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.services.FlagStorageService
import com.nkmathew.kikuyuflashcards.ui.adapters.StudyListAdapter

/**
 * Activity that displays flashcards in list mode similar to Next.js app
 * Shows all cards with English and Kikuyu side by side
 */
class StudyListActivity : AppCompatActivity() {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var studyListAdapter: StudyListAdapter

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

        // Initialize managers
        flashCardManager = FlashCardManagerV2(this)
        flagStorageService = FlagStorageService(this)

        // Set up RecyclerView
        setupRecyclerView()

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
        studyListAdapter = StudyListAdapter(
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
                studyListAdapter.setFlaggedCards(flaggedCards)
            },
            onCategoryChanged = { category ->
                flashCardManager.setCategory(category)
                loadCards()
            },
            onDifficultyChanged = { difficulty ->
                flashCardManager.setDifficulty(difficulty)
                loadCards()
            },
            onSortChanged = { sortMode ->
                currentSortMode = sortMode
                loadCards()
            },
            onExportClicked = {
                copyAllToClipboard()
            },
            onShareClicked = {
                shareAllCards()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studyListAdapter
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
        
        studyListAdapter.updateCards(cards)
        studyListAdapter.setFlaggedCards(flaggedCards)
        studyListAdapter.updateProgress(totalCards, cardsStudied)
    }

    /**
     * Update progress display
     */
    private fun updateProgress() {
        cardsStudied = knownCards.size
        studyListAdapter.updateProgress(totalCards, cardsStudied)
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
        val cardTexts = cards.map { "${it.kikuyu} - ${it.english}" }
        val fullText = cardTexts.joinToString("\n")
        
        val clip = ClipData.newPlainText("Kikuyu Flashcards", fullText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, "All cards copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /**
     * Share all cards
     */
    private fun shareAllCards() {
        val cards = flashCardManager.getAllEntries()
        if (cards.isEmpty()) {
            Toast.makeText(this, "No cards to share", Toast.LENGTH_SHORT).show()
            return
        }

        val cardTexts = cards.map { "${it.kikuyu} - ${it.english}" }
        val fullText = cardTexts.joinToString("\n")
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, fullText)
            putExtra(Intent.EXTRA_SUBJECT, "Kikuyu Flashcards")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share flashcards"))
    }
}