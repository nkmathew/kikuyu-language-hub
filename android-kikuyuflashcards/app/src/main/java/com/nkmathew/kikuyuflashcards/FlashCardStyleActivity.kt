package com.nkmathew.kikuyuflashcards

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
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
    private lateinit var timerTextView: TextView
    private lateinit var flipButton: Button
    private lateinit var knownButton: MaterialButton
    private lateinit var unknownButton: MaterialButton
    private lateinit var copyButton: MaterialButton
    private lateinit var flagButton: MaterialButton
    private lateinit var previousButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var slideshowButton: Button
    private lateinit var timerSettingButton: ImageButton

    // For slideshow mode
    private var isSlideshowRunning = false
    private val slideshowHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val slideshowFlipRunnable = Runnable { autoFlipInSlideshow() }
    private val slideshowAdvanceRunnable = Runnable { autoAdvanceInSlideshow() }
    private val countdownRunnable = Runnable { updateCountdownTimer() }

    // Default timer settings
    private var FLIP_DELAY_MS = 2000L // 2 seconds before flipping
    private var ADVANCE_DELAY_MS = 4000L // 4 seconds before next card
    private var countdownTimeRemaining = 0L
    private val COUNTDOWN_INTERVAL_MS = 100L // Update timer every 100ms

    // Preference keys
    private val PREFS_NAME = "FlashcardSettings"
    private val PREF_FLIP_DELAY = "flip_delay"
    private val PREF_ADVANCE_DELAY = "advance_delay"

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
        timerTextView = findViewById(R.id.timerTextView)
        flipButton = findViewById(R.id.flipButton)
        knownButton = findViewById(R.id.knownButton)
        unknownButton = findViewById(R.id.unknownButton)
        copyButton = findViewById(R.id.copyButton)
        flagButton = findViewById(R.id.flagButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        slideshowButton = findViewById(R.id.slideshowButton)
        timerSettingButton = findViewById(R.id.timerSettingButton)

        // Load saved timer settings
        loadTimerSettings()
    }

    /**
     * Load timer settings from SharedPreferences
     */
    private fun loadTimerSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        FLIP_DELAY_MS = prefs.getLong(PREF_FLIP_DELAY, 2000L)
        ADVANCE_DELAY_MS = prefs.getLong(PREF_ADVANCE_DELAY, 4000L)
    }

    /**
     * Save timer settings to SharedPreferences
     */
    private fun saveTimerSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(PREF_FLIP_DELAY, FLIP_DELAY_MS)
        editor.putLong(PREF_ADVANCE_DELAY, ADVANCE_DELAY_MS)
        editor.apply()
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

        timerSettingButton.setOnClickListener {
            showTimerSettingsDialog()
        }
    }

    /**
     * Show dialog to configure timer settings
     */
    private fun showTimerSettingsDialog() {
        // Create a dialog layout with two sliders
        val dialogView = layoutInflater.inflate(R.layout.dialog_timer_settings, null)

        // Use simple AlertDialog for now
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Slideshow Timer Settings")
            .setMessage("Adjust the timing for card flips and advances")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Get values from sliders and save them
                val flipSlider = dialogView.findViewById<SeekBar>(R.id.flipDelaySeekBar)
                val advanceSlider = dialogView.findViewById<SeekBar>(R.id.advanceDelaySeekBar)

                // Convert slider values (0-100) to milliseconds (500-5000)
                FLIP_DELAY_MS = 500L + (flipSlider.progress * 45L) // 500ms to 5000ms
                ADVANCE_DELAY_MS = 1000L + (advanceSlider.progress * 90L) // 1000ms to 10000ms

                // Save the settings
                saveTimerSettings()

                // Show toast confirmation
                Toast.makeText(
                    this,
                    "Timer settings saved: Flip ${FLIP_DELAY_MS/1000}s, Advance ${ADVANCE_DELAY_MS/1000}s",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Set initial slider values
        val flipSlider = dialogView.findViewById<SeekBar>(R.id.flipDelaySeekBar)
        val advanceSlider = dialogView.findViewById<SeekBar>(R.id.advanceDelaySeekBar)
        flipSlider.progress = ((FLIP_DELAY_MS - 500L) / 45L).toInt().coerceIn(0, 100)
        advanceSlider.progress = ((ADVANCE_DELAY_MS - 1000L) / 90L).toInt().coerceIn(0, 100)

        // Update labels as sliders move
        val flipLabel = dialogView.findViewById<TextView>(R.id.flipDelayLabel)
        val advanceLabel = dialogView.findViewById<TextView>(R.id.advanceDelayLabel)

        flipSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seconds = (500L + progress * 45L) / 1000f
                flipLabel.text = "Flip Delay: ${String.format("%.1f", seconds)}s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        advanceSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seconds = (1000L + progress * 90L) / 1000f
                advanceLabel.text = "Next Card Delay: ${String.format("%.1f", seconds)}s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set initial labels
        val flipSeconds = FLIP_DELAY_MS / 1000f
        val advanceSeconds = ADVANCE_DELAY_MS / 1000f
        flipLabel.text = "Flip Delay: ${String.format("%.1f", flipSeconds)}s"
        advanceLabel.text = "Next Card Delay: ${String.format("%.1f", advanceSeconds)}s"

        dialog.show()
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
            flashCardView.setCard(card, currentCardIndex) // Pass position for alternating colors
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

            // Update flag button appearance based on state
            if (isFlagged) {
                // Red background with white icon when flagged
                flagButton.setBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_dark_error))
                // Use ColorStateList for icon tint
                flagButton.iconTint = ContextCompat.getColorStateList(this, R.color.white)
                // Set stroke width to 0
                flagButton.strokeWidth = 0
            } else {
                // Transparent with primary color stroke and icon when not flagged
                flagButton.setBackgroundColor(Color.TRANSPARENT)
                // Use ColorStateList for icon tint
                flagButton.iconTint = ContextCompat.getColorStateList(this, R.color.md_theme_dark_primary)
                // Set stroke width using dimensions resource
                flagButton.strokeWidth = resources.getDimensionPixelSize(R.dimen.material_emphasis_medium)
            }
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

    /**
     * Start the slideshow with countdown timer
     */
    private fun startSlideshow() {
        isSlideshowRunning = true
        slideshowButton.text = "■ Stop Slideshow"
        slideshowButton.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_color))

        // Disable navigation buttons during slideshow
        previousButton.isEnabled = false
        nextButton.isEnabled = false

        // Show the timer
        timerTextView.visibility = View.VISIBLE

        // Start with front side showing
        if (flashCardView.isCardFlipped()) {
            flashCardView.flipCard()
            // Longer pause before starting
            countdownTimeRemaining = FLIP_DELAY_MS * 2
            slideshowHandler.postDelayed(slideshowFlipRunnable, FLIP_DELAY_MS * 2)
        } else {
            // Start the slideshow cycle
            countdownTimeRemaining = FLIP_DELAY_MS
            slideshowHandler.postDelayed(slideshowFlipRunnable, FLIP_DELAY_MS)
        }

        // Start the countdown timer
        updateCountdownTimer()
    }

    /**
     * Update the countdown timer display
     */
    private fun updateCountdownTimer() {
        if (isSlideshowRunning) {
            // Update the timer display
            val seconds = countdownTimeRemaining / 1000f
            timerTextView.text = String.format("%.1fs", seconds)

            // Decrement the timer
            countdownTimeRemaining -= COUNTDOWN_INTERVAL_MS
            if (countdownTimeRemaining >= 0) {
                // Schedule the next update
                slideshowHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop the slideshow and hide the countdown timer
     */
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
            slideshowHandler.removeCallbacks(countdownRunnable)

            // Hide the timer
            timerTextView.visibility = View.GONE

            // Re-enable buttons based on position
            updateButtonStates()
        }
    }

    /**
     * Auto flip the card and start countdown for next card
     */
    private fun autoFlipInSlideshow() {
        if (isSlideshowRunning) {
            flashCardView.flipCard()

            // Reset countdown for next advance
            countdownTimeRemaining = ADVANCE_DELAY_MS

            // Schedule advancement to next card
            slideshowHandler.postDelayed(slideshowAdvanceRunnable, ADVANCE_DELAY_MS)
        }
    }

    /**
     * Advance to the next card in slideshow and reset countdown timer
     */
    private fun autoAdvanceInSlideshow() {
        if (isSlideshowRunning) {
            // Check if we can move to the next card
            if (currentCardIndex < cards.size - 1) {
                currentCardIndex++
                showCurrentCard()
                updateProgress() // Update position indicator

                // Reset countdown for next flip
                countdownTimeRemaining = FLIP_DELAY_MS

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

