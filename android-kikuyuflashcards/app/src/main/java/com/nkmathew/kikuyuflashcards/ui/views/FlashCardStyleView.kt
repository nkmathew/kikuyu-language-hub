package com.nkmathew.kikuyuflashcards.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.R
import com.nkmathew.kikuyuflashcards.ThemeManager
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Custom view that displays a flashcard with flip animation
 * Borrows the flipping behavior from React Native FlashCard.tsx
 */
class FlashCardStyleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isFlipped = false
    private var currentCard: FlashcardEntry? = null

    // Views
    private lateinit var frontView: View
    private lateinit var backView: View
    private lateinit var frontText: TextView
    private lateinit var backText: TextView
    private lateinit var frontLabel: TextView
    private lateinit var backLabel: TextView
    private lateinit var frontDifficulty: TextView
    private lateinit var backDifficulty: TextView

    init {
        initializeViews()
    }

    private fun initializeViews() {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.view_flashcard_style, this, true)

        frontView = cardView.findViewById(R.id.frontView)
        backView = cardView.findViewById(R.id.backView)
        frontText = cardView.findViewById(R.id.frontText)
        backText = cardView.findViewById(R.id.backText)
        frontLabel = cardView.findViewById(R.id.frontLabel)
        backLabel = cardView.findViewById(R.id.backLabel)
        frontDifficulty = cardView.findViewById(R.id.frontDifficulty)
        backDifficulty = cardView.findViewById(R.id.backDifficulty)

        // Set up click listener for flip
        setOnClickListener {
            flipCard()
        }
    }

    fun setCard(card: FlashcardEntry) {
        currentCard = card
        isFlipped = false
        updateCardContent()
        showFront()
    }

    fun flipCard() {
        if (isFlipped) {
            flipToFront()
        } else {
            flipToBack()
        }
    }

    private fun flipToBack() {
        val frontAnimator = ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f)
        val backAnimator = ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f)

        frontAnimator.duration = 300
        backAnimator.duration = 300

        frontAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                frontView.visibility = View.GONE
                backView.visibility = View.VISIBLE
                backAnimator.start()
            }
        })

        frontAnimator.start()
        isFlipped = true
    }

    private fun flipToFront() {
        val backAnimator = ObjectAnimator.ofFloat(backView, "rotationY", 0f, -90f)
        val frontAnimator = ObjectAnimator.ofFloat(frontView, "rotationY", 90f, 0f)

        backAnimator.duration = 300
        frontAnimator.duration = 300

        backAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                backView.visibility = View.GONE
                frontView.visibility = View.VISIBLE
                frontAnimator.start()
            }
        })

        backAnimator.start()
        isFlipped = false
    }

    private fun showFront() {
        frontView.visibility = View.VISIBLE
        backView.visibility = View.GONE
        frontView.rotationY = 0f
        backView.rotationY = -90f
    }

    private fun updateCardContent() {
        currentCard?.let { card ->
            // Front side (English)
            frontText.text = card.english
            frontLabel.text = "English"
            frontDifficulty.text = card.difficulty.uppercase()

            // Back side (Kikuyu)
            backText.text = card.kikuyu
            backLabel.text = "Kikuyu"
            backDifficulty.text = card.difficulty.uppercase()

            // Set difficulty colors
            val difficultyColor = when (card.difficulty.lowercase()) {
                "beginner" -> ContextCompat.getColor(context, R.color.success_color)
                "intermediate" -> ContextCompat.getColor(context, R.color.warning_color)
                "advanced" -> ContextCompat.getColor(context,
                    if (ThemeManager.isDarkTheme(context)) R.color.md_theme_dark_error else R.color.md_theme_light_error)
                else -> ContextCompat.getColor(context,
                    if (ThemeManager.isDarkTheme(context)) R.color.md_theme_dark_tertiary else R.color.secondary_color)
            }

            frontDifficulty.setTextColor(difficultyColor)
            backDifficulty.setTextColor(difficultyColor)
        }
    }

    fun isCardFlipped(): Boolean = isFlipped
}

