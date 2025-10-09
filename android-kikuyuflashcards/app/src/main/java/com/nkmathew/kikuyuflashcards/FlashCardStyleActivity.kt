package com.nkmathew.kikuyuflashcards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.services.FlagStorageService
import com.nkmathew.kikuyuflashcards.ui.views.FlashCardStyleView

/**
 * Activity that displays flashcards in a flip-style format similar to the React Native app
 * Borrows the flipping animation and button layout from FlashCard.tsx
 */
class FlashCardStyleActivity : AppCompatActivity() {

    private lateinit var flashCardView: FlashCardStyleView
    private lateinit var progressTextView: TextView
    private lateinit var flipButton: Button
    private lateinit var knownButton: Button
    private lateinit var unknownButton: Button
    private lateinit var copyButton: Button
    private lateinit var flagButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var flagStorageService: FlagStorageService

    private var currentCardIndex = 0
    private var cards = listOf<FlashcardEntry>()
    private val knownCards = mutableSetOf<String>()
    private val flaggedCards = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_style)

        // Set up action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Flash Cards"
        }

        initializeViews()
        initializeManagers()
        setupClickListeners()
        loadCards()
    }

    private fun initializeViews() {
        flashCardView = findViewById(R.id.flashCardView)
        progressTextView = findViewById(R.id.progressTextView)
        flipButton = findViewById(R.id.flipButton)
        knownButton = findViewById(R.id.knownButton)
        unknownButton = findViewById(R.id.unknownButton)
        copyButton = findViewById(R.id.copyButton)
        flagButton = findViewById(R.id.flagButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
    }

    private fun initializeManagers() {
        flashCardManager = FlashCardManagerV2(this)
        flagStorageService = FlagStorageService(this)
    }

    private fun setupClickListeners() {
        flipButton.setOnClickListener {
            flashCardView.flipCard()
        }

        knownButton.setOnClickListener {
            markCardAsKnown()
        }

        unknownButton.setOnClickListener {
            markCardAsUnknown()
        }

        copyButton.setOnClickListener {
            copyCurrentCard()
        }

        flagButton.setOnClickListener {
            toggleFlag()
        }

        previousButton.setOnClickListener {
            showPreviousCard()
        }

        nextButton.setOnClickListener {
            showNextCard()
        }
    }

    private fun loadCards() {
        cards = flashCardManager.getAllEntries()
        if (cards.isNotEmpty()) {
            currentCardIndex = 0
            showCurrentCard()
            updateProgress()
        } else {
            Toast.makeText(this, "No cards available", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showCurrentCard() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            flashCardView.setCard(card)
            updateButtonStates()
        }
    }

    private fun updateButtonStates() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            val isKnown = knownCards.contains(card.id)
            val isFlagged = flaggedCards.contains(card.id)

            knownButton.visibility = if (isKnown) View.GONE else View.VISIBLE
            unknownButton.visibility = if (isKnown) View.VISIBLE else View.GONE

            flagButton.text = if (isFlagged) "🚩 Flagged" else "🚩"
        }

        previousButton.isEnabled = currentCardIndex > 0
        nextButton.isEnabled = currentCardIndex < cards.size - 1
    }

    private fun markCardAsKnown() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            knownCards.add(card.id)
            updateButtonStates()
            updateProgress()
            showNextCard()
        }
    }

    private fun markCardAsUnknown() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            knownCards.remove(card.id)
            updateButtonStates()
            updateProgress()
        }
    }

    private fun toggleFlag() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            if (flaggedCards.contains(card.id)) {
                flaggedCards.remove(card.id)
                flagStorageService.unflagCard(card.id)
            } else {
                flaggedCards.add(card.id)
                flagStorageService.flagCard(card.id)
            }
            updateButtonStates()
        }
    }

    private fun copyCurrentCard() {
        if (currentCardIndex < cards.size) {
            val card = cards[currentCardIndex]
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = "${card.kikuyu} - ${card.english}\nDifficulty: ${card.difficulty}\nCategory: ${card.category}"
            val clip = ClipData.newPlainText("Kikuyu Flashcard", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Card copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPreviousCard() {
        if (currentCardIndex > 0) {
            currentCardIndex--
            showCurrentCard()
        }
    }

    private fun showNextCard() {
        if (currentCardIndex < cards.size - 1) {
            currentCardIndex++
            showCurrentCard()
        }
    }

    private fun updateProgress() {
        val progress = "${currentCardIndex + 1}/${cards.size}"
        val knownCount = knownCards.size
        progressTextView.text = "Progress: $progress | Known: $knownCount"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

