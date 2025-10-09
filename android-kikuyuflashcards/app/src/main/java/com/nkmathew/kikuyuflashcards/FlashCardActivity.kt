package com.nkmathew.kikuyuflashcards

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.nkmathew.kikuyuflashcards.utils.AnimationHelpers
import com.nkmathew.kikuyuflashcards.FailureTracker

class FlashCardActivity : ComponentActivity(), SwipeGestureDetector.SwipeListener {
    
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    private lateinit var failureTracker: FailureTracker
    private lateinit var englishText: TextView
    private lateinit var kikuyuText: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var progressText: TextView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var englishCardLayout: android.widget.RelativeLayout
    private lateinit var kikuyuCardLayout: android.widget.RelativeLayout
    private lateinit var typeInInput: EditText
    private lateinit var checkAnswerButton: Button
    
    // Card interaction states
    private var isShowingKikuyu = false
    private var isTypeInMode = false
    private var cardDifficulty = CardDifficulty.UNKNOWN
    private var lastInteractionTime = 0L
    
    // Failure tracking
    private var typeInStartTime = 0L
    
    enum class CardDifficulty {
        KNOWN, LEARNING, DIFFICULT, MASTERED, UNKNOWN
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        failureTracker = FailureTracker(this)
        gestureDetector = GestureDetector(this, SwipeGestureDetector(this))
        
        // Start a new learning session for position tracking
        flashCardManager.startSession()
        
        // Check theme setting using proper ThemeManager
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121212") else ContextCompat.getColor(this, R.color.md_theme_light_background)
        val textColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        
        // Create enhanced root layout with modern background
        val rootLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(24, 0, 24, 32) // Top padding will be set by insets
            gravity = Gravity.CENTER
            setBackgroundColor(backgroundColor)
        }
        
        // Get category mode from intent
        val categoryMode = intent.getStringExtra("category_mode") ?: "All Categories"
        
        // Enhanced title with Material 3 styling
        val titleText = TextView(this).apply {
            text = "📚 $categoryMode"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 8)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Enhanced swipe instructions with better styling
        val instructionText = TextView(this).apply {
            text = "👈 Swipe for Next • Swipe for Previous 👉\n👆 Random Card • Toggle Translation 👇"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_onSurfaceVariant))
            setPadding(16, 8, 16, 24)
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.2f)
        }
        
        // Enhanced progress indicator with better styling
        progressText = TextView(this).apply {
            text = "📊 1 / ${flashCardManager.getTotalPhrases()}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_primary))
            setPadding(16, 8, 16, 20)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            // Add subtle background
            val progressBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_primaryContainer))
                cornerRadius = 24f
            }
            background = progressBg
        }
        
        // English phrase card container
        englishCardLayout = android.widget.RelativeLayout(this)
        
        // English phrase card with enhanced Material 3 styling and gradient
        englishText = TextView(this).apply {
            text = flashCardManager.getCurrentPhrase()?.english ?: "No phrases available"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(40, 64, 40, 64)
            gravity = Gravity.CENTER
            setSingleLine(false)
            maxLines = 4
            setLineSpacing(8f, 1.3f)
            setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Create beautiful gradient background
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    ContextCompat.getColor(this@FlashCardActivity, R.color.english_card_gradient_start),
                    ContextCompat.getColor(this@FlashCardActivity, R.color.english_card_gradient_end)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
            }
            background = gradientDrawable
            
            // Enhanced elevation and shadow
            elevation = 12f
            translationZ = 4f
            
            // Add click listener for card flip animation
            setOnClickListener { performCardFlip() }
            id = android.view.View.generateViewId()
        }
        
        // Enhanced English speaker button with modern styling (conditional)
        val englishSpeakerButton = Button(this).apply {
            text = "🔊"
            textSize = 20f
            setPadding(20, 20, 20, 20)
            setTextColor(Color.WHITE)
            
            // Create circular button background
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.pronunciation_blue))
            }
            background = buttonBg
            
            elevation = 8f
            translationZ = 2f
            
            // Show/hide based on settings
            visibility = if (SettingsActivity.isShowPronunciationButtonEnabled(this@FlashCardActivity)) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            setOnClickListener { 
                animateButtonPress(this)
                val currentPhrase = flashCardManager.getCurrentPhrase()
                if (currentPhrase != null) {
                    soundManager.speakEnglish(currentPhrase.english)
                }
            }
            
            val layoutParams = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
            layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
            layoutParams.setMargins(0, 8, 8, 0)
            this.layoutParams = layoutParams
        }
        
        englishCardLayout.addView(englishText)
        englishCardLayout.addView(englishSpeakerButton)
        
        // Kikuyu translation card container
        val kikuyuCardLayout = android.widget.RelativeLayout(this)
        
        // Kikuyu translation card with enhanced Material 3 styling and gradient
        kikuyuText = TextView(this).apply {
            text = ""  // Initially hidden
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(40, 64, 40, 64)
            gravity = Gravity.CENTER
            setSingleLine(false)
            maxLines = 4
            setLineSpacing(8f, 1.3f)
            setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Create beautiful gradient background
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    ContextCompat.getColor(this@FlashCardActivity, R.color.kikuyu_card_gradient_start),
                    ContextCompat.getColor(this@FlashCardActivity, R.color.kikuyu_card_gradient_end)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
            }
            background = gradientDrawable
            
            // Enhanced elevation and shadow
            elevation = 12f
            translationZ = 4f
            
            // Initially hidden
            alpha = 0f
            
            // Add click listener for card flip animation
            setOnClickListener { performCardFlip() }
            id = android.view.View.generateViewId()
        }
        
        // Enhanced Kikuyu speaker button with modern styling (conditional)
        val kikuyuSpeakerButton = Button(this).apply {
            text = "🔊"
            textSize = 20f
            setPadding(20, 20, 20, 20)
            setTextColor(Color.WHITE)
            
            // Create circular button background
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.category_teal))
            }
            background = buttonBg
            
            elevation = 8f
            translationZ = 2f
            alpha = 0f // Initially hidden with card
            
            // Show/hide based on Kikuyu sound button setting (separate from general pronunciation)
            visibility = if (SettingsActivity.isShowKikuyuSoundButtonEnabled(this@FlashCardActivity)) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            setOnClickListener { 
                animateButtonPress(this)
                val currentPhrase = flashCardManager.getCurrentPhrase()
                if (currentPhrase != null && kikuyuText.text.isNotEmpty()) {
                    soundManager.speakKikuyu(currentPhrase.kikuyu)
                }
            }
            
            val layoutParams = android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
            layoutParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
            layoutParams.setMargins(0, 8, 8, 0)
            this.layoutParams = layoutParams
        }
        
        kikuyuCardLayout.addView(kikuyuText)
        kikuyuCardLayout.addView(kikuyuSpeakerButton)
        
        // Navigation buttons
        val buttonLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }
        
        previousButton = Button(this).apply {
            text = "👈 Previous"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                soundManager.playButtonSound()
                onPreviousClicked() 
            }
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            
            // Enhanced button styling with gradient
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_secondary))
            }
            background = buttonBg
            
            elevation = 6f
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 16, 0)
            this.layoutParams = layoutParams
        }
        
        nextButton = Button(this).apply {
            text = "Next 👉"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                soundManager.playButtonSound()
                onNextClicked() 
            }
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            
            // Enhanced button styling with gradient
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_primary))
            }
            background = buttonBg
            
            elevation = 6f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Add spacing between buttons
        buttonLayout.addView(previousButton)
        buttonLayout.addView(nextButton)
        
        // Type-in recall button
        val typeInButton = Button(this).apply {
            text = "✏️ Type Answer"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                soundManager.playButtonSound()
                toggleTypeInMode()
            }
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            
            // Enhanced button styling with gradient
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.success_green))
            }
            background = buttonBg
            
            elevation = 6f
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 0, 0, 0)
            this.layoutParams = layoutParams
        }
        
        buttonLayout.addView(typeInButton)
        
        // Enhanced back button with modern styling
        val backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 16f
            setOnClickListener { 
                animateButtonPress(this)
                soundManager.playButtonSound()
                finish() 
            }
            setPadding(28, 18, 28, 18)
            setTextColor(Color.WHITE)
            
            // Enhanced button styling
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_outline))
            }
            background = buttonBg
            
            elevation = 4f
            setTypeface(null, android.graphics.Typeface.NORMAL)
            
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 32, 0, 0)
            this.layoutParams = layoutParams
        }
        
        // Enhanced pronunciation control button (conditional)
        val pronunciationButton = Button(this).apply {
            text = "🎵 Speak Both Languages"
            textSize = 16f
            setPadding(32, 16, 32, 16)
            setTextColor(Color.WHITE)
            
            // Create beautiful gradient background
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@FlashCardActivity, R.color.md_theme_light_tertiary),
                    ContextCompat.getColor(this@FlashCardActivity, R.color.quiz_purple)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
            }
            background = gradientDrawable
            
            elevation = 6f
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Show/hide based on settings
            visibility = if (SettingsActivity.isShowSpeakBothButtonEnabled(this@FlashCardActivity)) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            setOnClickListener { 
                animateButtonPress(this)
                val currentPhrase = flashCardManager.getCurrentPhrase()
                if (currentPhrase != null) {
                    soundManager.speakPhrase(currentPhrase, "both")
                }
            }
            
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 0)
            this.layoutParams = layoutParams
        }
        
        // Add all views to root layout
        rootLayout.addView(titleText)
        rootLayout.addView(instructionText)
        rootLayout.addView(progressText)
        rootLayout.addView(englishCardLayout)
        rootLayout.addView(kikuyuCardLayout)
        rootLayout.addView(pronunciationButton)
        rootLayout.addView(buttonLayout)
        rootLayout.addView(backButton)
        
        // Set up touch handling for swipe gestures
        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        
        setContentView(rootLayout)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top + 32) // Add 32dp top margin
            insets
        }
        
        updateUI()
    }
    
    private fun onPreviousClicked() {
        animateCardTransition("right")
        flashCardManager.getPreviousPhrase()
        updateUIWithAnimation()
    }
    
    private fun onNextClicked() {
        animateCardTransition("left") 
        flashCardManager.getNextPhrase()
        updateUIWithAnimation()
    }
    
    private fun updateUI() {
        try {
            val currentPhrase = flashCardManager.getCurrentPhrase()
            val totalPhrases = flashCardManager.getTotalPhrases()
            val currentIndex = flashCardManager.getCurrentIndex()
            
            if (currentPhrase != null && totalPhrases > 0) {
                englishText.text = currentPhrase.english.takeIf { it.isNotBlank() } ?: "No English text available"
                kikuyuText.text = currentPhrase.kikuyu.takeIf { it.isNotBlank() } ?: "No Kikuyu text available"
                progressText.text = "${currentIndex + 1} / $totalPhrases"
                
                // Enable/disable buttons based on circular navigation
                previousButton.isEnabled = totalPhrases > 0
                nextButton.isEnabled = totalPhrases > 0
                
                // Auto-play pronunciation if enabled
                if (SettingsActivity.isAutoPlayPronunciationEnabled(this)) {
                    // Auto-play English with a slight delay
                    englishText.postDelayed({
                        soundManager.speakEnglish(currentPhrase.english)
                    }, 500)
                }
            } else {
                // Handle empty or invalid state
                englishText.text = "No phrases available for this category"
                kikuyuText.text = "Please select a different category or check your data"
                progressText.text = "0 / 0"
                
                // Disable navigation when no phrases available
                previousButton.isEnabled = false
                nextButton.isEnabled = false
                
                Toast.makeText(this, "No phrases available in current category", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("FlashCardActivity", "Error updating UI: ${e.message}", e)
            englishText.text = "Error loading phrase"
            kikuyuText.text = "Please restart the app"
            progressText.text = "Error"
            previousButton.isEnabled = false
            nextButton.isEnabled = false
            Toast.makeText(this, "An error occurred. Please restart the app.", Toast.LENGTH_LONG).show()
        }
    }
    
    // Swipe gesture implementation with enhanced visual feedback and sound
    override fun onSwipeLeft() {
        // Swipe left = next card
        soundManager.playSwipeSound()
        progressManager.incrementCardsViewed()
        progressManager.incrementSwipes()
        
        animateSwipeFeedback("left", englishText)
        animateCardTransition("left")
        
        flashCardManager.getNextPhrase()
        updateUIWithAnimation()
        Toast.makeText(this, "Next phrase →", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSwipeRight() {
        // Swipe right = previous card  
        soundManager.playSwipeSound()
        progressManager.incrementSwipes()
        
        animateSwipeFeedback("right", englishText)
        animateCardTransition("right")
        
        flashCardManager.getPreviousPhrase()
        updateUIWithAnimation()
        Toast.makeText(this, "Previous phrase ←", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSwipeUp() {
        // Swipe up = random phrase
        soundManager.playSwipeSound()
        progressManager.incrementCardsViewed()
        progressManager.incrementSwipes()
        
        animateSwipeFeedback("up", englishText)
        
        flashCardManager.getRandomPhrase()
        updateUIWithAnimation()
        Toast.makeText(this, "Random phrase ↑", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSwipeDown() {
        // Swipe down = toggle translation visibility
        soundManager.playSwipeSound()
        progressManager.incrementSwipes()
        
        if (kikuyuText.alpha == 0f) {
            // Show Kikuyu translation with flip animation
            flipToKikuyu()
        } else {
            // Hide Kikuyu translation with flip animation
            flipToEnglish()
        }
    }
    
    // Animation Methods
    private fun animateButtonPress(button: Button) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.play(scaleDown).with(scaleDownY)
        animatorSet.play(scaleUp).with(scaleUpY).after(scaleDown)
        animatorSet.duration = 100
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }
    
    private fun flipToKikuyu() {
        if (kikuyuText.alpha == 1f) return // Already showing Kikuyu
        
        soundManager.playButtonSound()
        
        // Get the speaker button from kikuyu layout
        val kikuyuSpeakerButton = (kikuyuText.parent as android.widget.RelativeLayout).getChildAt(1) as Button
        
        // Flip animation: hide English, show Kikuyu
        val hideEnglish = ObjectAnimator.ofFloat(englishText, "alpha", 1f, 0f)
        val showKikuyu = ObjectAnimator.ofFloat(kikuyuText, "alpha", 0f, 1f)
        val showKikuyuSpeaker = ObjectAnimator.ofFloat(kikuyuSpeakerButton, "alpha", 0f, 1f)
        
        val rotateOut = ObjectAnimator.ofFloat(englishText, "rotationY", 0f, 90f)
        val rotateIn = ObjectAnimator.ofFloat(kikuyuText, "rotationY", -90f, 0f)
        
        hideEnglish.duration = 200
        showKikuyu.duration = 200
        showKikuyuSpeaker.duration = 200
        rotateOut.duration = 200
        rotateIn.duration = 200
        
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        rotateIn.interpolator = AccelerateDecelerateInterpolator()
        
        // Set Kikuyu text before animation
        kikuyuText.text = flashCardManager.getCurrentPhrase()?.kikuyu ?: ""
        
        val animatorSet = AnimatorSet()
        animatorSet.play(hideEnglish).with(rotateOut)
        animatorSet.play(showKikuyu).with(rotateIn).with(showKikuyuSpeaker).after(hideEnglish)
        animatorSet.start()
        
        Toast.makeText(this, "💭 Tap to see English • 🔊 Tap speaker to hear", Toast.LENGTH_SHORT).show()
    }
    
    private fun flipToEnglish() {
        if (englishText.alpha == 1f) return // Already showing English
        
        soundManager.playButtonSound()
        
        // Get the speaker button from kikuyu layout
        val kikuyuSpeakerButton = (kikuyuText.parent as android.widget.RelativeLayout).getChildAt(1) as Button
        
        // Flip animation: hide Kikuyu, show English
        val hideKikuyu = ObjectAnimator.ofFloat(kikuyuText, "alpha", 1f, 0f)
        val hideKikuyuSpeaker = ObjectAnimator.ofFloat(kikuyuSpeakerButton, "alpha", 1f, 0f)
        val showEnglish = ObjectAnimator.ofFloat(englishText, "alpha", 0f, 1f)
        
        val rotateOut = ObjectAnimator.ofFloat(kikuyuText, "rotationY", 0f, 90f)
        val rotateIn = ObjectAnimator.ofFloat(englishText, "rotationY", -90f, 0f)
        
        hideKikuyu.duration = 200
        hideKikuyuSpeaker.duration = 200
        showEnglish.duration = 200
        rotateOut.duration = 200
        rotateIn.duration = 200
        
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        rotateIn.interpolator = AccelerateDecelerateInterpolator()
        
        val animatorSet = AnimatorSet()
        animatorSet.play(hideKikuyu).with(rotateOut).with(hideKikuyuSpeaker)
        animatorSet.play(showEnglish).with(rotateIn).after(hideKikuyu)
        animatorSet.start()
        
        Toast.makeText(this, "💭 Tap to see Kikuyu • 🔊 Tap speaker to hear", Toast.LENGTH_SHORT).show()
    }
    
    private fun animateCardTransition(direction: String) {
        val translateDistance = 300f
        val startX = if (direction == "left") 0f else 0f
        val endX = if (direction == "left") -translateDistance else translateDistance
        
        // Slide out current card
        val slideOut = ObjectAnimator.ofFloat(englishText, "translationX", startX, endX)
        val fadeOut = ObjectAnimator.ofFloat(englishText, "alpha", 1f, 0.3f)
        
        slideOut.duration = 150
        fadeOut.duration = 150
        slideOut.interpolator = AccelerateDecelerateInterpolator()
        
        val slideOutSet = AnimatorSet()
        slideOutSet.play(slideOut).with(fadeOut)
        slideOutSet.start()
        
        // Slide in new card after delay
        englishText.postDelayed({
            englishText.translationX = -endX
            englishText.alpha = 0.3f
            
            val slideIn = ObjectAnimator.ofFloat(englishText, "translationX", -endX, 0f)
            val fadeIn = ObjectAnimator.ofFloat(englishText, "alpha", 0.3f, 1f)
            
            slideIn.duration = 150
            fadeIn.duration = 150
            slideIn.interpolator = OvershootInterpolator()
            
            val slideInSet = AnimatorSet()
            slideInSet.play(slideIn).with(fadeIn)
            slideInSet.start()
        }, 50)
    }
    
    private fun updateUIWithAnimation() {
        // Bounce animation for progress text
        val bounceAnimator = ObjectAnimator.ofFloat(progressText, "scaleY", 1f, 1.2f, 1f)
        bounceAnimator.duration = 300
        bounceAnimator.interpolator = BounceInterpolator()
        bounceAnimator.start()
        
        // Update content after animation delay
        englishText.postDelayed({
            updateUI()
            // Reset translation visibility and card state
            resetCardVisibility()
        }, 200)
    }
    
    private fun resetCardVisibility() {
        // Reset both cards to initial state
        kikuyuText.alpha = 0f
        englishText.alpha = 1f
        kikuyuText.text = ""
        isShowingKikuyu = false
        
        // Reset speaker button visibility
        val kikuyuSpeakerButton = (kikuyuText.parent as android.widget.RelativeLayout).getChildAt(1) as Button
        kikuyuSpeakerButton.alpha = 0f
        
        // Ensure English card is fully visible
        englishText.rotationY = 0f
        kikuyuText.rotationY = 0f
    }
    
    private fun animateSwipeFeedback(direction: String, targetView: TextView) {
        val color = when (direction) {
            "left" -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
            "right" -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
            "up" -> ContextCompat.getColor(this, android.R.color.holo_purple)
            "down" -> ContextCompat.getColor(this, android.R.color.darker_gray)
            else -> ContextCompat.getColor(this, R.color.md_theme_light_primary)
        }
        
        val originalColor = when (targetView) {
            englishText -> ContextCompat.getColor(this, R.color.md_theme_light_primary)
            else -> ContextCompat.getColor(this, R.color.md_theme_light_secondary)
        }
        
        // Color flash animation
        val colorAnimator = ValueAnimator.ofArgb(originalColor, color, originalColor)
        colorAnimator.duration = 300
        colorAnimator.addUpdateListener { animator ->
            targetView.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
        
        // Scale pulse animation
        val scaleX = ObjectAnimator.ofFloat(targetView, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(targetView, "scaleY", 1f, 1.05f, 1f)
        
        val scaleSet = AnimatorSet()
        scaleSet.play(scaleX).with(scaleY)
        scaleSet.duration = 200
        scaleSet.interpolator = AccelerateDecelerateInterpolator()
        scaleSet.start()
    }
    
    
    // Enhanced card flip method for click interactions
    private fun performCardFlip() {
        soundManager.playButtonSound()
        
        if (isShowingKikuyu) {
            flipToEnglish()
            isShowingKikuyu = false
        } else {
            flipToKikuyu()
            isShowingKikuyu = true
        }
        
        // Track card interaction for difficulty assessment
        trackCardInteraction()
    }
    
    // Track card interaction for difficulty assessment
    private fun trackCardInteraction() {
        val currentPhrase = flashCardManager.getCurrentPhrase() ?: return
        
        // Simple heuristic: if user flips quickly, they might know the word
        // If they take time or flip multiple times, they might be struggling
        val currentTime = System.currentTimeMillis()
        val timeSinceLastInteraction = currentTime - lastInteractionTime
        lastInteractionTime = currentTime
        
        if (timeSinceLastInteraction < 2000) { // Quick flip
            cardDifficulty = when (cardDifficulty) {
                CardDifficulty.UNKNOWN -> CardDifficulty.LEARNING
                CardDifficulty.LEARNING -> CardDifficulty.KNOWN
                CardDifficulty.KNOWN -> CardDifficulty.MASTERED
                else -> cardDifficulty
            }
        } else { // Slow flip or hesitation
            cardDifficulty = when (cardDifficulty) {
                CardDifficulty.UNKNOWN, CardDifficulty.MASTERED -> CardDifficulty.LEARNING
                CardDifficulty.KNOWN -> CardDifficulty.LEARNING
                CardDifficulty.LEARNING -> CardDifficulty.DIFFICULT
                else -> cardDifficulty
            }
        }
        
        // Log for future analytics
        Log.d("FlashCardActivity", "Card ${currentPhrase.english} marked as $cardDifficulty")
    }
    
    // Toggle type-in recall mode
    private fun toggleTypeInMode() {
        isTypeInMode = !isTypeInMode
        
        if (isTypeInMode) {
            showTypeInRecallDialog()
        } else {
            // Return to normal flashcard mode
            updateUI()
        }
    }
    
    // Show type-in recall dialog
    private fun showTypeInRecallDialog() {
        val currentPhrase = flashCardManager.getCurrentPhrase() ?: return
        
        // Create dialog for type-in recall
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Type the Kikuyu translation:")
        builder.setMessage("English: \"${currentPhrase.english}\"\n\nType your answer:")
        
        // Create input field
        val input = android.widget.EditText(this)
        input.hint = "Type Kikuyu translation here..."
        input.setTextColor(Color.BLACK)
        builder.setView(input)
        
        // Set up buttons
        builder.setPositiveButton("Check Answer") { dialog, _ ->
            val userAnswer = input.text.toString().trim()
            checkTypeInAnswer(userAnswer, currentPhrase.kikuyu)
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            isTypeInMode = false
            dialog.dismiss()
        }
        
        builder.setNeutralButton("Show Answer") { dialog, _ ->
            showAnswerDialog(currentPhrase)
            dialog.dismiss()
        }
        
        // Show dialog
        builder.show()
        
        // Set start time for failure tracking
        typeInStartTime = System.currentTimeMillis()
        
        // Focus on input field and show keyboard
        input.requestFocus()
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    // Check type-in answer and provide feedback
    private fun checkTypeInAnswer(userAnswer: String, correctAnswer: String) {
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)
        val isClose = userAnswer.isNotEmpty() && 
                     (correctAnswer.contains(userAnswer, ignoreCase = true) || 
                      userAnswer.contains(correctAnswer, ignoreCase = true))
        val responseTime = System.currentTimeMillis() - typeInStartTime
        
        when {
            isCorrect -> {
                Toast.makeText(this, "✅ Correct! Well done!", Toast.LENGTH_LONG).show()
                soundManager.playButtonSound()
                cardDifficulty = CardDifficulty.MASTERED
                
                // Record success
                flashCardManager.getCurrentPhrase()?.let { phrase ->
                    failureTracker.recordSuccess(phrase, FailureTracker.LearningMode.TYPE_IN_RECALL, responseTime)
                }
            }
            isClose -> {
                Toast.makeText(this, "🟡 Close! The correct answer is: \"$correctAnswer\"", Toast.LENGTH_LONG).show()
                cardDifficulty = CardDifficulty.LEARNING
                
                // Record partial success as spelling error
                flashCardManager.getCurrentPhrase()?.let { phrase ->
                    failureTracker.recordFailure(
                        phrase = phrase,
                        failureType = FailureTracker.FailureType.SPELLING_ERROR,
                        learningMode = FailureTracker.LearningMode.TYPE_IN_RECALL,
                        userAnswer = userAnswer,
                        correctAnswer = correctAnswer,
                        difficulty = "medium",
                        responseTime = responseTime
                    )
                }
            }
            else -> {
                Toast.makeText(this, "❌ Not quite. The correct answer is: \"$correctAnswer\"", Toast.LENGTH_LONG).show()
                cardDifficulty = CardDifficulty.DIFFICULT
                
                // Record failure
                flashCardManager.getCurrentPhrase()?.let { phrase ->
                    val failureType = when {
                        userAnswer.isEmpty() -> FailureTracker.FailureType.TIMEOUT_ERROR
                        else -> FailureTracker.FailureType.RECALL_ERROR
                    }
                    
                    failureTracker.recordFailure(
                        phrase = phrase,
                        failureType = failureType,
                        learningMode = FailureTracker.LearningMode.TYPE_IN_RECALL,
                        userAnswer = userAnswer,
                        correctAnswer = correctAnswer,
                        difficulty = "medium",
                        responseTime = responseTime
                    )
                }
            }
        }
        
        // Move to next card after feedback
        englishText.postDelayed({
            onNextClicked()
        }, 2000)
    }
    
    // Show answer dialog with learning options
    private fun showAnswerDialog(phrase: Phrase) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Answer")
        builder.setMessage("English: \"${phrase.english}\"\nKikuyu: \"${phrase.kikuyu}\"")
        
        builder.setPositiveButton("I knew this") { _, _ ->
            cardDifficulty = CardDifficulty.KNOWN
            Toast.makeText(this, "Marked as known", Toast.LENGTH_SHORT).show()
            
            // Record as success (user knew the answer)
            failureTracker.recordSuccess(phrase, FailureTracker.LearningMode.FLASHCARD, 0L)
            
            onNextClicked()
        }
        
        builder.setNegativeButton("Still learning") { _, _ ->
            cardDifficulty = CardDifficulty.LEARNING
            Toast.makeText(this, "Marked as learning", Toast.LENGTH_SHORT).show()
            
            // Record as recognition error (user needed to see the answer)
            failureTracker.recordFailure(
                phrase = phrase,
                failureType = FailureTracker.FailureType.RECOGNITION_ERROR,
                learningMode = FailureTracker.LearningMode.FLASHCARD,
                userAnswer = "[NEEDED TO SEE ANSWER]",
                correctAnswer = phrase.kikuyu,
                difficulty = "medium",
                responseTime = 0L
            )
            
            onNextClicked()
        }
        
        builder.setNeutralButton("Difficult") { _, _ ->
            cardDifficulty = CardDifficulty.DIFFICULT
            Toast.makeText(this, "Marked as difficult", Toast.LENGTH_SHORT).show()
            
            // Record as recall error (user found it difficult)
            failureTracker.recordFailure(
                phrase = phrase,
                failureType = FailureTracker.FailureType.RECALL_ERROR,
                learningMode = FailureTracker.LearningMode.FLASHCARD,
                userAnswer = "[DIFFICULT]",
                correctAnswer = phrase.kikuyu,
                difficulty = "medium",
                responseTime = 0L
            )
            
            // Don't advance - let user study more
        }
        
        builder.show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
    }
}