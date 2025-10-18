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
    private lateinit var slideshowButton: Button

    // For slideshow mode
    private var isSlideshowRunning = false
    private val slideshowHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val slideshowFlipRunnable = Runnable { autoFlipInSlideshow() }
    private val slideshowAdvanceRunnable = Runnable { autoAdvanceInSlideshow() }
    private val FLIP_DELAY_MS = 2000L // 2 seconds before flipping
    private val ADVANCE_DELAY_MS = 4000L // 4 seconds before next card

    private lateinit var flashCardManager: FlashCardManagerV2
    private lateinit var flagStorageService: FlagStorageService

    private var currentCardIndex = 0
    private var cards = listOf<FlashcardEntry>()
    private val knownCards = mutableSetOf<String>()
    private val flaggedCards = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_style)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

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
        slideshowButton = findViewById(R.id.slideshowButton)
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
            stopSlideshow()
            showPreviousCard()
        }

        nextButton.setOnClickListener {
            stopSlideshow()
            showNextCard()
        }

        slideshowButton.setOnClickListener {
            toggleSlideshow()
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
            updateProgress() // Update position indicator when showing a new card
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
            updateProgress() // Update position indicator
        }
    }

    private fun showNextCard() {
        if (currentCardIndex < cards.size - 1) {
            currentCardIndex++
            showCurrentCard()
            updateProgress() // Update position indicator
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

    // Slideshow functionality
    private fun toggleSlideshow() {
        if (isSlideshowRunning) {
            stopSlideshow()
        } else {
            startSlideshow()
        }
    }

    private fun startSlideshow() {
        isSlideshowRunning = true
        slideshowButton.text = "■ Stop Slideshow"
        slideshowButton.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_color))

        // Disable navigation buttons during slideshow
        previousButton.isEnabled = false
        nextButton.isEnabled = false

        // Start with front side showing
        if (flashCardView.isCardFlipped()) {
            flashCardView.flipCard()
            slideshowHandler.postDelayed(slideshowFlipRunnable, FLIP_DELAY_MS * 2) // Longer pause before starting
        } else {
            // Start the slideshow cycle
            slideshowHandler.postDelayed(slideshowFlipRunnable, FLIP_DELAY_MS)
        }
    }

    private fun stopSlideshow() {
        if (isSlideshowRunning) {
            isSlideshowRunning = false
            slideshowButton.text = "▶ Start Slideshow"
            slideshowButton.setBackgroundColor(ContextCompat.getColor(this,
                if (ThemeManager.isDarkTheme(this))
                    R.color.md_theme_dark_secondary
                else
                    R.color.md_theme_light_secondary))

            // Remove pending callbacks
            slideshowHandler.removeCallbacks(slideshowFlipRunnable)
            slideshowHandler.removeCallbacks(slideshowAdvanceRunnable)

            // Re-enable buttons based on position
            updateButtonStates()
        }
    }

    private fun autoFlipInSlideshow() {
        if (isSlideshowRunning) {
            flashCardView.flipCard()
            // Schedule advancement to next card
            slideshowHandler.postDelayed(slideshowAdvanceRunnable, ADVANCE_DELAY_MS)
        }
    }

    private fun autoAdvanceInSlideshow() {
        if (isSlideshowRunning) {
            // Check if we can move to the next card
            if (currentCardIndex < cards.size - 1) {
                currentCardIndex++
                showCurrentCard()
                updateProgress() // Update position indicator
                // Schedule flip for the next card
                slideshowHandler.postDelayed(slideshowFlipRunnable, FLIP_DELAY_MS)
            } else {
                // We've reached the end, stop slideshow
                Toast.makeText(this, "Slideshow completed", Toast.LENGTH_SHORT).show()
                stopSlideshow()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopSlideshow()
    }
}

