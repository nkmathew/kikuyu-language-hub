package com.nkmathew.kikuyuflashcards.ui.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.models.ExampleSentence
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Study card view that displays flashcards in list mode similar to Next.js app
 * Shows English and Kikuyu side by side with action buttons
 */
class StudyCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // UI Components
    private val cardView: CardView
    private val knownStatusBadge: TextView
    private val categoryBadge: TextView
    private val difficultyBadge: TextView
    private val qualityBadge: TextView
    private val englishTextView: TextView
    private val kikuyuTextView: TextView
    private val additionalInfoContainer: LinearLayout
    private val contextContainer: LinearLayout
    private val contextTextView: TextView
    private val culturalNotesContainer: LinearLayout
    private val culturalNotesTextView: TextView
    private val examplesContainer: LinearLayout
    private val sourceInfoContainer: TextView
    private val knownButton: TextView
    private val unknownButton: TextView
    private val copyButton: ImageView
    private val flagButton: ImageView

    // State
    private var currentEntry: FlashcardEntry? = null
    private var isKnown = false
    private var isFlagged = false
    private var showAdditionalInfo = false

    // Callbacks
    var onMarkKnownListener: (() -> Unit)? = null
    var onMarkUnknownListener: (() -> Unit)? = null
    var onFlagListener: (() -> Unit)? = null

    init {
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.view_study_card, this, true)

        // Find views
        cardView = findViewById(R.id.cardView)
        knownStatusBadge = findViewById(R.id.knownStatusBadge)
        categoryBadge = findViewById(R.id.categoryBadge)
        difficultyBadge = findViewById(R.id.difficultyBadge)
        qualityBadge = findViewById(R.id.qualityBadge)
        englishTextView = findViewById(R.id.englishTextView)
        kikuyuTextView = findViewById(R.id.kikuyuTextView)
        additionalInfoContainer = findViewById(R.id.additionalInfoContainer)
        contextContainer = findViewById(R.id.contextContainer)
        contextTextView = findViewById(R.id.contextTextView)
        culturalNotesContainer = findViewById(R.id.culturalNotesContainer)
        culturalNotesTextView = findViewById(R.id.culturalNotesTextView)
        examplesContainer = findViewById(R.id.examplesContainer)
        sourceInfoContainer = findViewById(R.id.sourceInfoContainer)
        knownButton = findViewById(R.id.knownButton)
        unknownButton = findViewById(R.id.unknownButton)
        copyButton = findViewById(R.id.copyButton)
        flagButton = findViewById(R.id.flagButton)

        // Set up click listeners
        knownButton.setOnClickListener {
            if (isKnown) {
                // Mark as unknown
                isKnown = false
                updateKnownState()
                onMarkUnknownListener?.invoke()
            } else {
                // Mark as known
                isKnown = true
                updateKnownState()
                onMarkKnownListener?.invoke()
            }
        }

        unknownButton.setOnClickListener {
            isKnown = false
            updateKnownState()
            onMarkUnknownListener?.invoke()
        }

        copyButton.setOnClickListener {
            copyCardToClipboard()
        }

        flagButton.setOnClickListener {
            onFlagListener?.invoke()
        }

        // Toggle additional info on card click
        cardView.setOnClickListener {
            showAdditionalInfo = !showAdditionalInfo
            updateAdditionalInfoVisibility()
        }
    }

    /**
     * Set the current flashcard entry to display
     */
    fun setEntry(entry: FlashcardEntry) {
        currentEntry = entry
        updateUI()
    }

    /**
     * Set whether this card is marked as known
     */
    fun setKnown(known: Boolean) {
        isKnown = known
        updateKnownState()
    }

    /**
     * Get whether this card is marked as known
     */
    fun isCardKnown(): Boolean = isKnown
    
    /**
     * Set whether this card is flagged
     */
    fun setFlagged(flagged: Boolean) {
        isFlagged = flagged
        updateFlagState()
    }
    
    /**
     * Get whether this card is flagged
     */
    fun isCardFlagged(): Boolean = isFlagged

    /**
     * Update the UI with the current entry
     */
    private fun updateUI() {
        val entry = currentEntry ?: return

        // Set the basic text content
        englishTextView.text = entry.english
        kikuyuTextView.text = entry.kikuyu

        // Set category badge
        categoryBadge.text = Categories.getCategoryDisplayName(entry.category)
        setCategoryBadgeStyle(entry.category)

        // Set difficulty badge
        difficultyBadge.text = DifficultyLevels.getDifficultyDisplayName(entry.difficulty)
        setDifficultyBadgeStyle(entry.difficulty)

        // Set quality badge if available
        entry.quality?.let { quality ->
            qualityBadge.text = "★ ${String.format("%.1f", quality.confidenceScore)}"
            setQualityBadgeColor(quality.confidenceScore)
            qualityBadge.isVisible = true
        } ?: run {
            qualityBadge.isVisible = false
        }

        // Set source information
        sourceInfoContainer.text = "Source: ${entry.source.origin}"

        // Set additional information
        updateAdditionalInfo(entry)

        // Update known state
        updateKnownState()
    }

    /**
     * Update additional information section
     */
    private fun updateAdditionalInfo(entry: FlashcardEntry) {
        // Context
        entry.context?.let {
            contextTextView.text = it
            contextContainer.isVisible = true
        } ?: run {
            contextContainer.isVisible = false
        }

        // Cultural notes
        entry.culturalNotes?.let {
            culturalNotesTextView.text = it
            culturalNotesContainer.isVisible = true
        } ?: run {
            culturalNotesContainer.isVisible = false
        }

        // Examples
        if (entry.examples.isNotEmpty()) {
            // Clear existing examples
            examplesContainer.removeAllViews()

            // Add up to 2 examples
            entry.examples.take(2).forEach { example ->
                addExampleView(example)
            }

            // Show "more examples" if there are more than 2
            if (entry.examples.size > 2) {
                val moreView = TextView(context)
                moreView.text = "+${entry.examples.size - 2} more examples"
                moreView.textSize = 12f
                moreView.setTextColor(ContextCompat.getColor(context, R.color.secondary_color))
                moreView.gravity = android.view.Gravity.END
                examplesContainer.addView(moreView)
            }

            examplesContainer.isVisible = true
        } else {
            examplesContainer.isVisible = false
        }
    }

    /**
     * Add an example view to the examples container
     */
    private fun addExampleView(example: ExampleSentence) {
        val exampleView = LinearLayout(context)
        exampleView.orientation = LinearLayout.VERTICAL
        exampleView.setPadding(8, 8, 8, 8)
        exampleView.setBackgroundColor(Color.parseColor("#10000000")) // Light gray

        val kikuyuView = TextView(context)
        kikuyuView.text = example.kikuyu
        kikuyuView.textSize = 14f
        kikuyuView.setTypeface(null, android.graphics.Typeface.BOLD)
        exampleView.addView(kikuyuView)

        val englishView = TextView(context)
        englishView.text = example.english
        englishView.textSize = 12f
        exampleView.addView(englishView)

        example.context?.let { contextText ->
            val contextView = TextView(context)
            contextView.text = contextText
            contextView.textSize = 10f
            contextView.setTypeface(null, android.graphics.Typeface.ITALIC)
            exampleView.addView(contextView)
        }

        examplesContainer.addView(exampleView)

        // Add spacing between examples
        val space = View(context)
        space.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            8
        )
        examplesContainer.addView(space)
    }

    /**
     * Update the known state UI
     */
    private fun updateKnownState() {
        if (isKnown) {
            knownStatusBadge.isVisible = true
            knownButton.text = "☑"
            knownButton.setTextColor(ContextCompat.getColor(context, R.color.success_color))
            unknownButton.isVisible = false
        } else {
            knownStatusBadge.isVisible = false
            knownButton.text = "☐"
            knownButton.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onSurfaceVariant))
            unknownButton.isVisible = false
        }
    }
    
    /**
     * Update the flag state UI
     */
    private fun updateFlagState() {
        if (isFlagged) {
            flagButton.setColorFilter(ContextCompat.getColor(context, R.color.warning_color))
            flagButton.setBackgroundColor(ContextCompat.getColor(context, R.color.warning_color))
        } else {
            flagButton.setColorFilter(Color.parseColor("#888888"))
            flagButton.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Update additional info visibility
     */
    private fun updateAdditionalInfoVisibility() {
        additionalInfoContainer.isVisible = showAdditionalInfo
    }

    /**
     * Copy card content to clipboard
     */
    private fun copyCardToClipboard() {
        val entry = currentEntry ?: return

        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var copyText = "Source: ${entry.english}\nTranslation: ${entry.kikuyu}"
            
            // Note: Pronunciation not available in current FlashcardEntry model
            
            // Add context if available
            entry.context?.let { copyText += "\nContext: $it" }
            
            // Add cultural notes if available
            entry.culturalNotes?.let { copyText += "\nCultural Note: $it" }
            
            // Add examples if available
            if (entry.examples.isNotEmpty()) {
                val firstExample = entry.examples[0]
                copyText += "\nExample Source: ${firstExample.english}\nExample Translation: ${firstExample.kikuyu}"
            }
            
            // Add source info
            copyText += "\nContent Source: ${entry.source.origin}"
            
            val clip = ClipData.newPlainText("Kikuyu Flashcard", copyText)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Set the category badge style based on category
     */
    private fun setCategoryBadgeStyle(category: String) {
        val color = when (category) {
            Categories.VOCABULARY -> Color.parseColor("#2196F3") // Blue
            Categories.PROVERBS -> Color.parseColor("#9C27B0") // Purple
            Categories.GRAMMAR -> Color.parseColor("#4CAF50") // Green
            Categories.CONJUGATIONS -> Color.parseColor("#F44336") // Red
            Categories.CULTURAL -> Color.parseColor("#795548") // Brown
            Categories.NUMBERS -> Color.parseColor("#607D8B") // Blue Gray
            Categories.PHRASES -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#9E9E9E") // Gray
        }

        categoryBadge.setTextColor(color)
        categoryBadge.setBackgroundColor(Color.argb(25, Color.red(color), Color.green(color), Color.blue(color)))
    }

    /**
     * Set the difficulty badge style based on difficulty
     */
    private fun setDifficultyBadgeStyle(difficulty: String) {
        val color = when (difficulty) {
            DifficultyLevels.BEGINNER -> Color.parseColor("#43A047") // Green
            DifficultyLevels.INTERMEDIATE -> Color.parseColor("#FB8C00") // Orange
            DifficultyLevels.ADVANCED -> Color.parseColor("#E53935") // Red
            else -> Color.parseColor("#9E9E9E") // Gray
        }

        difficultyBadge.setTextColor(color)
        difficultyBadge.setBackgroundColor(Color.argb(25, Color.red(color), Color.green(color), Color.blue(color)))
    }

    /**
     * Set the quality badge color based on score
     */
    private fun setQualityBadgeColor(score: Float) {
        val color = when {
            score >= 4.5f -> Color.parseColor("#43A047") // Green
            score >= 4.0f -> Color.parseColor("#8BC34A") // Light Green
            score >= 3.5f -> Color.parseColor("#FFA000") // Amber
            score >= 3.0f -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#F44336") // Red
        }

        qualityBadge.setBackgroundColor(color)
    }
}
