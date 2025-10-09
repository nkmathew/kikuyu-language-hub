package com.nkmathew.kikuyuflashcards.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.models.ExampleSentence
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Enhanced flashcard view that displays the rich metadata from curated content
 */
class EnhancedFlashCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // UI Components
    private val cardView: CardView
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
    private val grammaticalInfoContainer: LinearLayout
    private val grammaticalInfoTextView: TextView
    private val examplesContainer: LinearLayout
    private val sourceInfoContainer: TextView
    private val previousButton: ImageButton
    private val flipButton: ImageButton
    private val nextButton: ImageButton

    // State
    private var currentEntry: FlashcardEntry? = null
    private var isFlipped = false

    // Callbacks
    var onFlipListener: (() -> Unit)? = null
    var onNextListener: (() -> Unit)? = null
    var onPreviousListener: (() -> Unit)? = null

    init {
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.view_enhanced_flashcard, this, true)

        // Find views
        cardView = findViewById(R.id.contentContainer)
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
        grammaticalInfoContainer = findViewById(R.id.grammaticalInfoContainer)
        grammaticalInfoTextView = findViewById(R.id.grammaticalInfoTextView)
        examplesContainer = findViewById(R.id.examplesContainer)
        sourceInfoContainer = findViewById(R.id.sourceInfoContainer)
        previousButton = findViewById(R.id.previousButton)
        flipButton = findViewById(R.id.flipButton)
        nextButton = findViewById(R.id.nextButton)

        // Set up click listeners
        flipButton.setOnClickListener {
            flipCard()
            onFlipListener?.invoke()
        }

        previousButton.setOnClickListener {
            onPreviousListener?.invoke()
        }

        nextButton.setOnClickListener {
            onNextListener?.invoke()
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
     * Flip the card to show the other side
     */
    fun flipCard() {
        isFlipped = !isFlipped
        updateCardSide()
    }

    /**
     * Reset to front side
     */
    fun resetToFront() {
        isFlipped = false
        updateCardSide()
    }

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

        // Set additional information if available
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

        // Grammatical information
        entry.grammaticalInfo?.let { info ->
            val parts = mutableListOf<String>()

            info.partOfSpeech?.let { parts.add("Part of speech: $it") }
            info.verbClass?.let { parts.add("Verb class: $it") }
            info.nounClass?.let { parts.add("Noun class: $it") }
            info.infinitive?.let { parts.add("Infinitive: $it") }
            info.tense?.let { parts.add("Tense: $it") }
            info.subjectMarker?.let { parts.add("Subject marker: $it") }

            if (parts.isNotEmpty()) {
                grammaticalInfoTextView.text = parts.joinToString("\n")
                grammaticalInfoContainer.isVisible = true
            } else {
                grammaticalInfoContainer.isVisible = false
            }
        } ?: run {
            grammaticalInfoContainer.isVisible = false
        }

        // Examples
        if (entry.examples.isNotEmpty()) {
            // Clear existing examples
            examplesContainer.removeAllViews()

            // Keep the title view
            val titleView = TextView(context)
            titleView.text = "Examples:"
            titleView.textSize = 14f
            titleView.setTypeface(null, android.graphics.Typeface.BOLD)
            examplesContainer.addView(titleView)

            // Add up to 2 examples
            entry.examples.take(2).forEach { example ->
                addExampleView(example)
            }

            // Show "more examples" if there are more than 2
            if (entry.examples.size > 2) {
                val moreView = TextView(context)
                moreView.text = "+${entry.examples.size - 2} more examples"
                moreView.textSize = 12f
                moreView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                moreView.gravity = android.view.Gravity.END
                examplesContainer.addView(moreView)
            }

            examplesContainer.isVisible = true
        } else {
            examplesContainer.isVisible = false
        }

        // Update card side (front or back)
        updateCardSide()
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
     * Update the card side based on isFlipped state
     */
    private fun updateCardSide() {
        englishTextView.isVisible = !isFlipped
        kikuyuTextView.isVisible = isFlipped
        additionalInfoContainer.isVisible = isFlipped
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

        // Set text color
        categoryBadge.setTextColor(color)

        // Set background color with 10% opacity
        val backgroundColor = Color.argb(
            25, // 10% opacity
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        categoryBadge.setBackgroundColor(backgroundColor)
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

        // Set text color
        difficultyBadge.setTextColor(color)

        // Set background color with 10% opacity
        val backgroundColor = Color.argb(
            25, // 10% opacity
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        difficultyBadge.setBackgroundColor(backgroundColor)
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