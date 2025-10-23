package com.nkmathew.kikuyuflashcards

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nkmathew.kikuyuflashcards.services.FlagStorageService
import com.nkmathew.kikuyuflashcards.ui.adapters.StudyListAdapter
import com.nkmathew.kikuyuflashcards.ui.decorations.SimpleDividerItemDecoration
import com.nkmathew.kikuyuflashcards.ui.decorations.VerticalMarginItemDecoration

/**
 * Activity that displays flashcards in list mode similar to Next.js app
 * Shows all cards with English and Kikuyu side by side
 */
class StudyListActivity : AppCompatActivity() {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var studyListAdapter: StudyListAdapter
    private lateinit var jumpToCardFab: FloatingActionButton

    // Managers
    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var flagStorageService: FlagStorageService
    private lateinit var positionManager: StudyListPositionManager

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
        jumpToCardFab = findViewById(R.id.jumpToCardFab)

        // Initialize managers - explicitly set study mode (not flashcard mode)
        flashCardManager = FlashCardManagerV2(this)
        flashCardManager.setFlashcardMode(false)
        android.util.Log.d("StudyListActivity", "Setting flashcard mode: false (study mode)")
        flagStorageService = FlagStorageService(this)
        positionManager = StudyListPositionManager(this)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up FAB
        setupJumpToCardFab()

        // Load initial cards
        loadCards()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Save position before leaving
            saveScrollPosition()
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Save position when activity is paused (user navigates away or app goes to background)
        saveScrollPosition()
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
                // Save current position before changing category
                saveScrollPosition()
                flashCardManager.setCategory(category)
                loadCards()
            },
            onDifficultyChanged = { difficulty ->
                // Save current position before changing difficulty
                saveScrollPosition()
                flashCardManager.setDifficulty(difficulty)
                loadCards()
            },
            onSortChanged = { sortMode ->
                // Save current position before changing sort mode
                saveScrollPosition()
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

        // Add vertical margin decoration between items (12dp)
        recyclerView.addItemDecoration(VerticalMarginItemDecoration(this, 12))

        // Add subtle divider between items
        recyclerView.addItemDecoration(SimpleDividerItemDecoration(this))

        // Add scroll listener to show/hide FAB
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Show FAB when scrolling up, hide when scrolling down
                if (dy < 0 && jumpToCardFab.visibility != View.VISIBLE) {
                    jumpToCardFab.show()
                } else if (dy > 0 && jumpToCardFab.visibility == View.VISIBLE) {
                    jumpToCardFab.hide()
                }
            }
        })
    }

    /**
     * Set up the Jump To Card FAB
     */
    private fun setupJumpToCardFab() {
        jumpToCardFab.setOnClickListener {
            showJumpToCardDialog()
        }
    }

    /**
     * Show dialog to enter card number
     */
    private fun showJumpToCardDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_jump_to_card, null)
        val cardNumberEditText = dialogView.findViewById<EditText>(R.id.cardNumberEditText)

        AlertDialog.Builder(this)
            .setTitle("Jump to Card")
            .setMessage("Enter a card number (1-${totalCards})")
            .setView(dialogView)
            .setPositiveButton("Jump") { _, _ ->
                val cardNumberText = cardNumberEditText.text.toString()
                if (cardNumberText.isNotEmpty()) {
                    try {
                        val cardNumber = cardNumberText.toInt()
                        jumpToCard(cardNumber)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Jump to a specific card position
     */
    private fun jumpToCard(cardNumber: Int) {
        if (cardNumber in 1..totalCards) {
            // Account for header position (position 0)
            // In the adapter: position 0 is header, position 1 is first card (card #1)
            // So we need to add the header offset to go from card number to adapter position
            val positionInAdapter = cardNumber // Card number is already 1-based, matching adapter position with header
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(positionInAdapter, 0)

            // Briefly highlight the card by scrolling slightly past it and then back
            recyclerView.postDelayed({
                // Smooth scroll to make it clear which card we jumped to
                recyclerView.smoothScrollBy(0, 20)
                recyclerView.postDelayed({
                    recyclerView.smoothScrollBy(0, -20)
                }, 100)
            }, 100)

            Toast.makeText(this, "Jumped to card $cardNumber", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Card number out of range", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Load cards and update the adapter
     */
    private fun loadCards() {
        android.util.Log.d("StudyListActivity", "🔍 DEBUG: Starting loadCards()")

        var cards = flashCardManager.getAllEntries()
        android.util.Log.d("StudyListActivity", "🔍 DEBUG: flashCardManager.getAllEntries() returned ${cards.size} cards")

        // Apply sorting based on current sort mode
        cards = when (currentSortMode) {
            "short_first" -> cards.sortedBy { it.kikuyu.length + it.english.length }
            "long_first" -> cards.sortedByDescending { it.kikuyu.length + it.english.length }
            else -> cards // default order
        }

        totalCards = cards.size
        cardsStudied = 0

        android.util.Log.d("StudyListActivity", "🔍 DEBUG: After sorting and setting totalCards = $totalCards")

        // Load flagged cards
        flaggedCards.clear()
        flaggedCards.addAll(flagStorageService.getFlaggedItems())
        android.util.Log.d("StudyListActivity", "🔍 DEBUG: Loaded ${flaggedCards.size} flagged cards")

        studyListAdapter.updateCards(cards)
        studyListAdapter.setFlaggedCards(flaggedCards)
        studyListAdapter.updateProgress(totalCards, cardsStudied)

        android.util.Log.d("StudyListActivity", "🔍 DEBUG: Updated adapter with ${cards.size} cards")
        android.util.Log.d("StudyListActivity", "Loaded ${cards.size} study cards (in study mode, not expanding examples)")

        // Restore saved position if available
        restoreScrollPosition()
    }

    /**
     * Save the current scroll position
     */
    private fun saveScrollPosition() {
        try {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val position = layoutManager.findFirstVisibleItemPosition()

            if (position > 0) {  // Skip header position (0)
                val category = flashCardManager.getCurrentCategory()
                val difficulty = flashCardManager.getCurrentDifficulty()

                positionManager.savePosition(
                    position = position,
                    category = category,
                    difficulty = difficulty,
                    sortMode = currentSortMode,
                    totalItems = totalCards
                )

                android.util.Log.d("StudyListActivity",
                    "Saved position $position (category: $category, difficulty: $difficulty, sort: $currentSortMode)")
            }
        } catch (e: Exception) {
            android.util.Log.e("StudyListActivity", "Error saving scroll position: ${e.message}", e)
        }
    }

    /**
     * Restore the saved scroll position if available
     */
    private fun restoreScrollPosition() {
        try {
            val category = flashCardManager.getCurrentCategory()
            val difficulty = flashCardManager.getCurrentDifficulty()

            if (positionManager.hasLastPosition(category, difficulty, currentSortMode)) {
                val position = positionManager.getLastPosition(category, difficulty, currentSortMode)

                if (position > 0 && position < totalCards) {
                    // Post with delay to ensure adapter has completed its work
                    recyclerView.post {
                        (recyclerView.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(position, 0)

                        android.util.Log.d("StudyListActivity",
                            "Restored position $position (category: $category, difficulty: $difficulty, sort: $currentSortMode)")

                        // Brief visual indicator of scroll position change
                        recyclerView.postDelayed({
                            // Flash a brief scroll to highlight the position
                            recyclerView.smoothScrollBy(0, 20)
                            recyclerView.postDelayed({
                                recyclerView.smoothScrollBy(0, -20)
                            }, 100)
                        }, 300)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("StudyListActivity", "Error restoring scroll position: ${e.message}", e)
        }
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