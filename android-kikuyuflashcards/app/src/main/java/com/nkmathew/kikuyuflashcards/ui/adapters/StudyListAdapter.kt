package com.nkmathew.kikuyuflashcards.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.ui.views.StudyCardView

/**
 * Adapter for displaying study list with header and cards
 */
class StudyListAdapter(
    private val onCardStatusChanged: (String, Boolean) -> Unit,
    private val onCardFlagged: (String) -> Unit,
    private val onCategoryChanged: (String?) -> Unit,
    private val onDifficultyChanged: (String?) -> Unit,
    private val onSortChanged: (String) -> Unit,
    private val onExportClicked: () -> Unit,
    private val onShareClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CARD = 1
    }

    private var cards = listOf<FlashcardEntry>()
    private val knownCards = mutableSetOf<String>()
    private val flaggedCards = mutableSetOf<String>()
    private var totalCards = 0
    private var cardsStudied = 0

    /**
     * Update the list of cards
     */
    fun updateCards(newCards: List<FlashcardEntry>) {
        cards = newCards
        notifyDataSetChanged()
    }

    /**
     * Set known cards
     */
    fun setKnownCards(knownCardIds: Set<String>) {
        knownCards.clear()
        knownCards.addAll(knownCardIds)
        notifyDataSetChanged()
    }
    
    /**
     * Set flagged cards
     */
    fun setFlaggedCards(flaggedCardIds: Set<String>) {
        flaggedCards.clear()
        flaggedCards.addAll(flaggedCardIds)
        notifyDataSetChanged()
    }

    /**
     * Update progress
     */
    fun updateProgress(total: Int, studied: Int) {
        totalCards = total
        cardsStudied = studied
        notifyItemChanged(0) // Update header
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.header_study_list, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_CARD -> {
                val studyCardView = StudyCardView(parent.context)
                CardViewHolder(studyCardView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is CardViewHolder -> {
                val cardPosition = position - 1 // Subtract 1 for header
                val card = cards[cardPosition]
                // Pass the position (1-based) and total count to the card
                // Use cardPosition + 1 for 1-based position to match ProblemWordsActivity
                holder.bind(card, cardPosition + 1, cards.size)
            }
        }
    }

    override fun getItemCount(): Int = cards.size + 1 // +1 for header

    inner class HeaderViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        private val categoryDropdown: AutoCompleteTextView = itemView.findViewById(R.id.categoryDropdown)
        private val difficultyDropdown: AutoCompleteTextView = itemView.findViewById(R.id.difficultyDropdown)
        private val sortDropdown: AutoCompleteTextView = itemView.findViewById(R.id.sortDropdown)
        private val exportButton: Button = itemView.findViewById(R.id.exportButton)
        private val shareButton: Button = itemView.findViewById(R.id.shareButton)

        init {
            setupDropdowns()
            setupButtons()
        }

        private fun setupDropdowns() {
            // Category dropdown
            val categories = mutableListOf("All Categories")
            val categoryMap = mutableMapOf<String, String?>()
            categoryMap["All Categories"] = null

            // Add available categories
            categories.addAll(listOf("Vocabulary", "Greetings", "Numbers", "Family", "Colors"))
            categories.forEach { category ->
                categoryMap[category] = category.lowercase()
            }

            val categoryAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, categories)
            categoryDropdown.setAdapter(categoryAdapter)
            categoryDropdown.setText("All Categories", false)

            categoryDropdown.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = categories[position]
                val category = categoryMap[selectedCategory]
                onCategoryChanged(category)
            }

            // Difficulty dropdown
            val difficulties = listOf("All Levels", "Beginner", "Intermediate", "Advanced")
            val difficultyAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, difficulties)
            difficultyDropdown.setAdapter(difficultyAdapter)
            difficultyDropdown.setText("All Levels", false)

            difficultyDropdown.setOnItemClickListener { _, _, position, _ ->
                val selectedDifficulty = difficulties[position]
                val difficulty = if (selectedDifficulty == "All Levels") null else selectedDifficulty.lowercase()
                onDifficultyChanged(difficulty)
            }

            // Sort dropdown
            val sortOptions = listOf("Default", "Short First", "Long First")
            val sortMap = mapOf(
                "Default" to "default",
                "Short First" to "short_first",
                "Long First" to "long_first"
            )

            val sortAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, sortOptions)
            sortDropdown.setAdapter(sortAdapter)
            sortDropdown.setText("Default", false)

            sortDropdown.setOnItemClickListener { _, _, position, _ ->
                val selectedSort = sortOptions[position]
                val sortMode = sortMap[selectedSort] ?: "default"
                onSortChanged(sortMode)
            }
        }

        private fun setupButtons() {
            exportButton.setOnClickListener { onExportClicked() }
            shareButton.setOnClickListener { onShareClicked() }
        }

        fun bind() {
            val progressPercent = if (totalCards > 0) (cardsStudied * 100) / totalCards else 0
            progressTextView.text = "Progress: $cardsStudied/$totalCards cards known ($progressPercent%)"
        }
    }

    inner class CardViewHolder(private val studyCardView: StudyCardView) : RecyclerView.ViewHolder(studyCardView) {
        fun bind(card: FlashcardEntry, position: Int, totalCount: Int) {
            studyCardView.setEntry(card)
            studyCardView.setKnown(knownCards.contains(card.id))
            studyCardView.setFlagged(flaggedCards.contains(card.id))
            studyCardView.setPosition(position, totalCount)

            // Set up click listeners
            studyCardView.setOnClickListener {
                val isKnown = knownCards.contains(card.id)
                onCardStatusChanged(card.id, !isKnown)
            }

            // Set up flag button click listener
            studyCardView.onFlagListener = {
                onCardFlagged(card.id)
                // Update the UI immediately to show flag state change
                studyCardView.setFlagged(flaggedCards.contains(card.id))
            }
        }
    }
}
