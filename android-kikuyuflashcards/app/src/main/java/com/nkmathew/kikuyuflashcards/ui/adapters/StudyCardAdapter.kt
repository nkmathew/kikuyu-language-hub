package com.nkmathew.kikuyuflashcards.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.ui.views.StudyCardView

/**
 * Adapter for displaying study cards in a RecyclerView
 */
class StudyCardAdapter(
    private val onCardStatusChanged: (String, Boolean) -> Unit
) : RecyclerView.Adapter<StudyCardAdapter.StudyCardViewHolder>() {

    private var cards = listOf<FlashcardEntry>()
    private val knownCards = mutableSetOf<String>()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyCardViewHolder {
        val studyCardView = StudyCardView(parent.context)
        return StudyCardViewHolder(studyCardView)
    }

    override fun onBindViewHolder(holder: StudyCardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card, knownCards.contains(card.id))
    }

    override fun getItemCount(): Int = cards.size

    /**
     * ViewHolder for study cards
     */
    inner class StudyCardViewHolder(
        private val studyCardView: StudyCardView
    ) : RecyclerView.ViewHolder(studyCardView) {

        fun bind(card: FlashcardEntry, isKnown: Boolean) {
            studyCardView.setEntry(card)
            studyCardView.setKnown(isKnown)
            
            studyCardView.onMarkKnownListener = {
                onCardStatusChanged(card.id, true)
            }
            
            studyCardView.onMarkUnknownListener = {
                onCardStatusChanged(card.id, false)
            }
        }
    }
}
