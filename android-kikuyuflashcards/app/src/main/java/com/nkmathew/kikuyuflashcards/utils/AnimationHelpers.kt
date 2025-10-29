package com.nkmathew.kikuyuflashcards.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.*

/**
 * Utility class for creating smooth animations throughout the app
 */
object AnimationHelpers {
    
    /**
     * Creates a 3D flip animation between two views (like a card flip)
     */
    fun create3DFlipAnimation(
        frontView: View,
        backView: View,
        duration: Long = 600L,
        onMidFlip: (() -> Unit)? = null
    ): AnimatorSet {
        val animatorSet = AnimatorSet()
        
        // First half: flip front view out
        val flipOut = ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f).apply {
            this.duration = duration / 2
            interpolator = AccelerateInterpolator()
        }
        
        // Second half: flip back view in
        val flipIn = ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f).apply {
            this.duration = duration / 2
            interpolator = DecelerateInterpolator()
        }
        
        // Set up the sequence
        flipOut.addUpdateListener { animation ->
            if (animation.animatedFraction >= 1.0f) {
                frontView.visibility = View.INVISIBLE
                backView.visibility = View.VISIBLE
                onMidFlip?.invoke()
            }
        }
        
        animatorSet.playSequentially(flipOut, flipIn)
        return animatorSet
    }
    
    /**
     * Creates a smooth slide transition animation
     */
    fun createSlideAnimation(
        view: View,
        direction: SlideDirection,
        distance: Float = 300f,
        duration: Long = 300L
    ): ObjectAnimator {
        return when (direction) {
            SlideDirection.LEFT -> ObjectAnimator.ofFloat(view, "translationX", 0f, -distance)
            SlideDirection.RIGHT -> ObjectAnimator.ofFloat(view, "translationX", 0f, distance)
            SlideDirection.UP -> ObjectAnimator.ofFloat(view, "translationY", 0f, -distance)
            SlideDirection.DOWN -> ObjectAnimator.ofFloat(view, "translationY", 0f, distance)
        }.apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * Creates a bounce animation for button presses
     */
    fun createBounceAnimation(view: View, scale: Float = 0.95f, duration: Long = 150L): AnimatorSet {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, scale),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, scale)
            )
            this.duration = duration / 2
            interpolator = AccelerateInterpolator()
        }
        
        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", scale, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", scale, 1f)
            )
            this.duration = duration / 2
            interpolator = BounceInterpolator()
        }
        
        return AnimatorSet().apply {
            playSequentially(scaleDown, scaleUp)
        }
    }
    
    /**
     * Creates a fade transition animation
     */
    fun createFadeAnimation(
        view: View,
        fromAlpha: Float,
        toAlpha: Float,
        duration: Long = 300L
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * Creates a scale animation with overshoot for highlighting
     */
    fun createHighlightAnimation(view: View, duration: Long = 400L): AnimatorSet {
        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f)
            )
            this.duration = duration / 2
            interpolator = OvershootInterpolator()
        }
        
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f)
            )
            this.duration = duration / 2
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        return AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
        }
    }
    
    /**
     * Creates a shake animation for errors or attention
     */
    fun createShakeAnimation(view: View, intensity: Float = 10f, duration: Long = 500L): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "translationX", 0f, intensity, -intensity, intensity, -intensity, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * Creates a pulse animation for continuous highlighting
     */
    fun createPulseAnimation(view: View, minScale: Float = 0.95f, maxScale: Float = 1.05f): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "scaleX", minScale, maxScale).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            
            // Also animate Y scale
            val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", minScale, maxScale).apply {
                duration = 1000L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            // Start both animations together
            scaleYAnimator.start()
        }
    }
    
    /**
     * Creates a reveal animation that scales from center
     */
    fun createRevealAnimation(view: View, duration: Long = 400L): AnimatorSet {
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 0f
        
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            )
            this.duration = duration
            interpolator = OvershootInterpolator()
        }
    }
    
    /**
     * Creates a smooth color transition animation
     */
    fun createColorTransition(
        view: View,
        fromColor: Int,
        toColor: Int,
        duration: Long = 300L,
        onUpdate: (Int) -> Unit
    ): ValueAnimator {
        return ValueAnimator.ofArgb(fromColor, toColor).apply {
            this.duration = duration
            addUpdateListener { animation ->
                onUpdate(animation.animatedValue as Int)
            }
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Creates a smooth slide-in animation for new questions
     */
    fun createSlideInFromRight(view: View, duration: Long = 400L): AnimatorSet {
        view.translationX = view.width.toFloat()
        view.alpha = 0f

        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "translationX", view.width.toFloat(), 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            )
            this.duration = duration
            interpolator = DecelerateInterpolator()
        }
    }

    /**
     * Creates a smooth slide-out animation for old questions
     */
    fun createSlideOutToLeft(view: View, duration: Long = 400L): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "translationX", 0f, -view.width.toFloat()),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            )
            this.duration = duration
            interpolator = AccelerateInterpolator()
        }
    }

    /**
     * Creates a sparkle/celebration animation for correct answers
     */
    fun createCelebrationAnimation(view: View, duration: Long = 600L): AnimatorSet {
        return AnimatorSet().apply {
            val rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f)

            playTogether(rotate, scaleX, scaleY)
            this.duration = duration
            interpolator = OvershootInterpolator()
        }
    }

    /**
     * Creates a subtle loading pulse animation
     */
    fun createLoadingPulse(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", 1f, 0.6f, 1f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Creates a staggered animation sequence
     */
    fun createStaggeredAnimation(views: List<View>, duration: Long = 300L, delay: Long = 50L): AnimatorSet {
        val animators = mutableListOf<ObjectAnimator>()

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f

            val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                this.duration = duration
                startDelay = index * delay
            }

            val translateAnim = ObjectAnimator.ofFloat(view, "translationY", 50f, 0f).apply {
                this.duration = duration
                startDelay = index * delay
                interpolator = OvershootInterpolator()
            }

            animators.add(alphaAnim)
            animators.add(translateAnim)
        }

        return AnimatorSet().apply {
            playTogether(*animators.toTypedArray())
        }
    }

    /**
     * Direction enum for slide animations
     */
    enum class SlideDirection {
        LEFT, RIGHT, UP, DOWN
    }
}