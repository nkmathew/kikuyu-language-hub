package com.nkmathew.kikuyuflashcards.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Adapter for displaying flagged cards in a RecyclerView
 */
class FlaggedCardAdapter(
    private val onRemoveFlag: (String) -> Unit,
    private val onAddReason: (String) -> Unit
) : RecyclerView.Adapter<FlaggedCardAdapter.FlaggedCardViewHolder>() {

    private var cards = listOf<FlashcardEntry>()
    private var flagReasons = mapOf<String, String>()

    /**
     * Update the list of cards and reasons
     */
    fun updateCards(newCards: List<FlashcardEntry>, reasons: Map<String, String>) {
        cards = newCards
        flagReasons = reasons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlaggedCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flagged_card, parent, false)
        return FlaggedCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlaggedCardViewHolder, position: Int) {
        val card = cards[position]
        val reason = flagReasons[card.id]
        holder.bind(card, reason, position + 1, cards.size)
    }

    override fun getItemCount(): Int = cards.size

    /**
     * ViewHolder for flagged cards
     */
    inner class FlaggedCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val kikuyuText: TextView = itemView.findViewById(R.id.kikuyuText)
        private val englishText: TextView = itemView.findViewById(R.id.englishText)
        private val difficultyText: TextView = itemView.findViewById(R.id.difficultyText)
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val culturalNotesText: TextView = itemView.findViewById(R.id.culturalNotesText)
        private val sourceText: TextView = itemView.findViewById(R.id.sourceText)
        private val reasonContainer: View = itemView.findViewById(R.id.reasonContainer)
        private val reasonText: TextView = itemView.findViewById(R.id.reasonText)
        private val addReasonButton: TextView = itemView.findViewById(R.id.addReasonButton)
        private val removeButton: TextView = itemView.findViewById(R.id.removeButton)
        private val positionBadge: TextView = itemView.findViewById(R.id.positionBadge)

        fun bind(card: FlashcardEntry, reason: String?, position: Int, total: Int) {
            kikuyuText.text = card.kikuyu
            englishText.text = card.english
            difficultyText.text = card.difficulty
            categoryText.text = card.category

            // Set position badge
            positionBadge.text = position.toString()

            // Show cultural notes if available
            if (card.culturalNotes?.isNotEmpty() == true) {
                culturalNotesText.text = card.culturalNotes
                culturalNotesText.visibility = View.VISIBLE
            } else {
                culturalNotesText.visibility = View.GONE
            }

            // Show source if available
            if (card.source?.origin?.isNotEmpty() == true) {
                sourceText.text = "Source: ${card.source.origin}"
                sourceText.visibility = View.VISIBLE
            } else {
                sourceText.visibility = View.GONE
            }

            // Show reason if available
            if (reason?.isNotEmpty() == true) {
                reasonText.text = reason
                reasonContainer.visibility = View.VISIBLE
                addReasonButton.visibility = View.GONE
            } else {
                reasonContainer.visibility = View.GONE
                addReasonButton.visibility = View.VISIBLE
            }

            // Set up click listeners
            addReasonButton.setOnClickListener {
                onAddReason(card.id)
            }

            reasonContainer.setOnClickListener {
                onAddReason(card.id)
            }

            removeButton.setOnClickListener {
                onRemoveFlag(card.id)
            }
        }
    }
}
