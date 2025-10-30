package com.nkmathew.kikuyuflashcards

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.nkmathew.kikuyuflashcards.models.Categories

class MainActivityWithBottomNav : ComponentActivity() {
    
    private lateinit var soundManager: SoundManager
    private lateinit var contentContainer: LinearLayout
    private lateinit var bottomNavLayout: LinearLayout
    private var currentActiveTab = "home"
    private lateinit var activityProgressTracker: ActivityProgressTracker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        soundManager = SoundManager(this)
        activityProgressTracker = ActivityProgressTracker(this)

        createBottomNavigationUI()
    }
    
    private fun createBottomNavigationUI() {
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val backgroundColor = if (isDarkTheme) ContextCompat.getColor(this, R.color.md_theme_dark_background) else ContextCompat.getColor(this, R.color.md_theme_light_background)
        
        // Main container
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }
        
        // Content container (scrollable)
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // This will take up remaining space above bottom nav
            )
        }
        
        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
        }
        
        // Show home content by default
        showHomeContent()
        
        scrollView.addView(contentContainer)
        
        // Bottom navigation
        val bottomNav = createBottomNavigation(isDarkTheme)
        
        // Set initial active tab after bottom nav is created
        setActiveTab("home")
        
        mainLayout.addView(scrollView)
        mainLayout.addView(bottomNav)
        
        setContentView(mainLayout)
        
        // Handle system insets to avoid overlap with system navigation
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Apply top padding for status bar
            view.updatePadding(top = systemBars.top)
            
            // Apply bottom padding to bottom navigation for system nav bar
            bottomNav.updatePadding(bottom = navigationBars.bottom)
            
            insets
        }
    }
    
    private fun createBottomNavigation(isDarkTheme: Boolean): LinearLayout {
        val bottomNavLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8) // Reduced padding for smaller height
            setBackgroundColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.parseColor("#F8F9FA"))
            
            // Add subtle top border and elevation
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.parseColor("#F8F9FA"))
                setStroke(2, if (isDarkTheme) Color.parseColor("#333333") else Color.parseColor("#E0E0E0"))
            }
            setBackground(background)
            elevation = 8f
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Navigation items
        val navItems = listOf(
            NavItem("🏠", "Home", "home"),
            NavItem("📚", "Learn", "learn"),
            NavItem("🚩", "Flagged", "flagged"),
            NavItem("📊", "Stats", "stats"),
            NavItem("⚙️", "Settings", "settings")
        )
        
        navItems.forEachIndexed { index, item ->
            val navButton = createNavButton(item, isDarkTheme, index)
            bottomNavLayout.addView(navButton)
        }
        
        this@MainActivityWithBottomNav.bottomNavLayout = bottomNavLayout
        return bottomNavLayout
    }
    
    private fun createNavButton(item: NavItem, isDarkTheme: Boolean, index: Int): LinearLayout {
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8) // Reduced padding
            
            val layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            this.layoutParams = layoutParams
            
            setOnClickListener {
                soundManager.playButtonSound()
                setActiveTab(item.id)
                when (item.id) {
                    "home" -> showHomeContent()
                    "learn" -> showLearnContent()
                    "flagged" -> startFlaggedTranslations()
                    "stats" -> showStatsContent()
                    "settings" -> showSettingsContent()
                }
            }
            
            // Add ripple effect
            isClickable = true
            isFocusable = true
            val rippleColor = if (isDarkTheme) Color.parseColor("#444444") else Color.parseColor("#E0E0E0")
            val ripple = android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(rippleColor),
                null,
                null
            )
            background = ripple
        }
        
        // Icon
        val iconText = TextView(this).apply {
            text = item.icon
            textSize = 20f // Reduced icon size
            gravity = Gravity.CENTER
            tag = "icon_${item.id}" // Add tag for easy access
        }
        
        // Label
        val labelText = TextView(this).apply {
            text = item.label
            textSize = 10f // Reduced label size
            gravity = Gravity.CENTER
            setPadding(0, 2, 0, 0) // Reduced spacing
            tag = "label_${item.id}" // Add tag for easy access
        }
        
        // Active indicator
        val indicator = TextView(this).apply {
            text = "●"
            textSize = 8f
            gravity = Gravity.CENTER
            setPadding(0, 2, 0, 0)
            tag = "indicator_${item.id}"
            visibility = if (item.id == currentActiveTab) android.view.View.VISIBLE else android.view.View.GONE
            setTextColor(ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary))
        }
        
        // Update colors based on active state
        updateNavButtonColors(iconText, labelText, item.id == currentActiveTab, isDarkTheme)
        
        buttonContainer.addView(iconText)
        buttonContainer.addView(labelText)
        buttonContainer.addView(indicator)
        
        // Store reference for later updates
        buttonContainer.tag = "nav_${item.id}"
        
        return buttonContainer
    }
    
    private fun setActiveTab(tabId: String) {
        currentActiveTab = tabId
        updateBottomNavIndicators()
    }
    
    private fun updateBottomNavIndicators() {
        val navItems = listOf("home", "learn", "flagged", "stats", "settings")
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        
        navItems.forEach { itemId ->
            val navContainer = bottomNavLayout.findViewWithTag<LinearLayout>("nav_$itemId")
            navContainer?.let { container ->
                val iconText = container.findViewWithTag<TextView>("icon_$itemId")
                val labelText = container.findViewWithTag<TextView>("label_$itemId")
                val indicator = container.findViewWithTag<TextView>("indicator_$itemId")
                
                val isActive = itemId == currentActiveTab
                indicator?.visibility = if (isActive) android.view.View.VISIBLE else android.view.View.GONE
                updateNavButtonColors(iconText, labelText, isActive, isDarkTheme)
            }
        }
    }
    
    private fun updateNavButtonColors(iconText: TextView?, labelText: TextView?, isActive: Boolean, isDarkTheme: Boolean) {
        val activeColor = if (isDarkTheme) {
            ContextCompat.getColor(this, R.color.md_theme_dark_primary)
        } else {
            ContextCompat.getColor(this, R.color.md_theme_light_primary)
        }
        val inactiveColor = if (isDarkTheme) Color.parseColor("#888888") else Color.parseColor("#666666")

        iconText?.setTextColor(if (isActive) activeColor else inactiveColor)
        labelText?.setTextColor(if (isActive) activeColor else inactiveColor)
    }
    
    private fun showHomeContent() {
        contentContainer.removeAllViews()

        val isDarkTheme = ThemeManager.isDarkTheme(this)

        // Welcome section
        val titleText = TextView(this).apply {
            text = "🇰🇪 Kikuyu Flash Cards"
            textSize = 32f
            setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val welcomeText = TextView(this).apply {
            text = "Wĩ mwega! Welcome to your Kikuyu learning journey"
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurfaceVariant))
            setPadding(16, 0, 16, 24)
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.2f)
        }

        // Quick Actions section (moved to top for better visibility)
        val activityCard = createActivityCard(isDarkTheme)

        // Quick stats card
        val statsCard = createStatsCard(isDarkTheme)

        // Recent Activities Section
        val recentActivitiesCard = createRecentActivitiesCard(isDarkTheme)

        // Add views in new order
        contentContainer.addView(titleText)
        contentContainer.addView(welcomeText)
        contentContainer.addView(activityCard)
        contentContainer.addView(recentActivitiesCard)
        contentContainer.addView(statsCard)
    }

    /**
     * Create a card showing recent activities
     */
    private fun createRecentActivitiesCard(isDarkTheme: Boolean): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (isDarkTheme)
                    ContextCompat.getColor(context, R.color.md_theme_dark_surfaceContainerHigh)
                    else ContextCompat.getColor(context, R.color.md_theme_light_surfaceContainerHigh))
            }
            setBackground(background)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }

        val titleText = TextView(this).apply {
            text = "🎓 Learning Progress"
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }

        // Progress summary
        val progressManager = ProgressManager(this)
        val stats = progressManager.getProgressStats()

        val progressSummaryText = TextView(this).apply {
            text = "You've viewed ${stats.totalCardsViewed} cards and answered " +
                   "${stats.quizCorrectAnswers} quiz questions correctly. " +
                   "Current learning streak: ${stats.currentStreak} days!"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else Color.parseColor("#666666"))
            setPadding(0, 0, 0, 16)
            setLineSpacing(4f, 1.2f)
        }

        // Progress bar showing overall progress
        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        // Create progress bar view
        val progressView = android.view.View(this)
        val progressPercent = (stats.totalCardsViewed.toFloat() / 100f).coerceIn(0.05f, 1f)
        val progressBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(ContextCompat.getColor(this@MainActivityWithBottomNav,
                if (isDarkTheme) R.color.md_theme_dark_primary else R.color.md_theme_light_primary))
        }
        progressView.background = progressBg

        val progressParams = LinearLayout.LayoutParams(
            0,
            24,
            progressPercent
        )
        progressParams.marginEnd = 8
        progressView.layoutParams = progressParams

        // Create remaining bar view
        val remainingView = android.view.View(this)
        val remainingPercent = 1f - progressPercent
        val remainingBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.parseColor("#333333"))
        }
        remainingView.background = remainingBg

        val remainingParams = LinearLayout.LayoutParams(
            0,
            24,
            remainingPercent
        )
        remainingView.layoutParams = remainingParams

        progressContainer.addView(progressView)
        progressContainer.addView(remainingView)

        cardLayout.addView(titleText)
        cardLayout.addView(progressSummaryText)
        cardLayout.addView(progressContainer)

        return cardLayout
    }
    
    private fun showLearnContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        
        val titleText = TextView(this).apply {
            text = "📚 Learning Modes"
            textSize = 28f
            setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Learning mode cards
        val learningModes = listOf(
            LearningMode("🎯 Flash Cards", "Flip-style cards with categories", "flashcard_style"),
            LearningMode("📋 Study List", "Side-by-side learning mode", "study_list"),
            LearningMode("🚩 Flagged Translations", "Review flagged translations", "flagged_translations"),
            LearningMode("🧠 Quiz Mode", "Test your knowledge", "quiz"),
            LearningMode("✏️ Fill Blanks", "Complete the sentences", "fill_blank"),
            LearningMode("🔀 Sentence Unscramble", "Drag words to correct order", "sentence_unscramble"),
            LearningMode("🔤 Vowel Hunt", "Find the correct vowels", "vowel_hunt")
        )
        
        contentContainer.addView(titleText)
        
        learningModes.forEach { mode ->
            val modeCard = createLearningModeCard(mode, isDarkTheme)
            contentContainer.addView(modeCard)
        }
    }
    
    
    private fun createStatsCard(isDarkTheme: Boolean): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F5"))
            }
            setBackground(background)
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }
        
        val titleText = TextView(this).apply {
            text = "📊 Your Progress"
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }
        
        // Get category totals from FlashCardManager
        val flashCardManager = FlashCardManagerV2(this)
        val categoryTotals = getCategoryTotals(flashCardManager)
        
        val statsText = TextView(this).apply {
            text = "• Words learned: 127\n• Accuracy: 78%\n• Current streak: 5 days\n\n📚 Category Totals:\n$categoryTotals"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else Color.parseColor("#666666"))
            setLineSpacing(4f, 1.3f)
        }
        
        cardLayout.addView(titleText)
        cardLayout.addView(statsText)
        
        return cardLayout
    }
    
    private fun createActivityCard(isDarkTheme: Boolean): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F5"))
            }
            setBackground(background)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.layoutParams = layoutParams
        }

        val titleText = TextView(this).apply {
            text = "🎯 Quick Actions"
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }

        // Create horizontal scroll view for activity buttons
        val scrollView = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
        }

        // Container for activity buttons
        val activityButtonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add activity buttons for resumable activities
        val resumableActivities = listOf(
            Triple("flashcard_style", "🎯 Flash Cards", R.color.md_theme_dark_primary),
            Triple("study_list", "📋 Study", R.color.md_theme_dark_secondary),
            Triple("quiz", "🧠 Quiz", R.color.md_theme_dark_tertiary),
            Triple("fill_blank", "✏️ Fill Blanks", R.color.md_theme_dark_secondary),
            Triple("sentence_unscramble", "🔀 Unscramble", R.color.md_theme_dark_tertiary),
            Triple("vowel_hunt", "🔤 Vowel Hunt", R.color.md_theme_dark_secondary)
        )

        // Add activity buttons
        for ((activityId, title, color) in resumableActivities) {
            // Get progress for this activity
            val progress = activityProgressTracker.getProgressForActivity(activityId)

            // Create resume message
            val resumeMessage = activityProgressTracker.getResumeMessage(activityId)

            // Create activity button
            val button = createResumeButton(
                title = title,
                description = resumeMessage,
                progress = progress,
                colorResId = color,
                isDarkTheme = isDarkTheme
            ) {
                // Handle click based on activity ID
                when (activityId) {
                    "flashcard_style" -> startFlashCardStyle(false) // Don't show category selection
                    "study_list" -> startStudyList()
                    "quiz" -> startQuiz()
                    "fill_blank" -> startFillBlank()
                    "sentence_unscramble" -> startSentenceUnscramble()
                    "vowel_hunt" -> startVowelHunt()
                }
            }

            activityButtonsContainer.addView(button)
        }

        // Add the activity buttons container to the scroll view
        scrollView.addView(activityButtonsContainer)

        // Add title and scrolling activity buttons
        cardLayout.addView(titleText)
        cardLayout.addView(scrollView)

        return cardLayout
    }

    /**
     * Create a resume activity button
     */
    private fun createResumeButton(
        title: String,
        description: String,
        progress: Float,
        colorResId: Int,
        isDarkTheme: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)

            // Set size
            val layoutParams = LinearLayout.LayoutParams(
                240, // Fixed width for consistent buttons
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = 12
            this.layoutParams = layoutParams

            // Create background with progress gradient
            val buttonBg = if (progress > 0) {
                // Show progress gradient
                val gradientStart = ContextCompat.getColor(this@MainActivityWithBottomNav, colorResId)
                val gradientEnd = ContextCompat.getColor(this@MainActivityWithBottomNav,
                    if (isDarkTheme) R.color.md_theme_dark_surfaceContainerHigh else R.color.md_theme_light_surfaceContainerHigh)

                GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(gradientStart, gradientEnd)).apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    setGradientCenter(progress, 0.5f)
                }
            } else {
                // Solid background
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(this@MainActivityWithBottomNav, colorResId))
                }
            }

            background = buttonBg
            elevation = 4f

            // Make clickable
            isClickable = true
            isFocusable = true
            setOnClickListener {
                soundManager.playButtonSound()
                onClick()
            }

            // Title
            val titleText = TextView(this@MainActivityWithBottomNav).apply {
                text = title
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(8, 8, 8, 4)
            }

            // Description
            val descText = TextView(this@MainActivityWithBottomNav).apply {
                text = description
                textSize = 12f
                setTextColor(Color.WHITE)
                alpha = 0.9f
                gravity = Gravity.CENTER
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(8, 0, 8, 8)
            }

            // Progress indicator
            val progressIndicator = if (progress > 0) {
                TextView(this@MainActivityWithBottomNav).apply {
                    text = "${(progress * 100).toInt()}%"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_inverseSurface))
                    }
                    setPadding(8, 2, 8, 2)
                    alpha = 0.9f
                }
            } else null

            addView(titleText)
            addView(descText)
            progressIndicator?.let { addView(it) }
        }
    }
    
    /**
     * Get category totals for display in stats
     */
    private fun getCategoryTotals(flashCardManager: FlashCardManagerV2): String {
        val categories = flashCardManager.getAvailableCategories()
        val totals = categories.map { category ->
            val count = flashCardManager.getTotalEntriesInCategory(category)
            val displayName = Categories.getCategoryDisplayName(category)
            "• $displayName: $count"
        }
        return totals.joinToString("\n")
    }
    
    private fun createLearningModeCard(mode: LearningMode, isDarkTheme: Boolean): LinearLayout {
        // Get progress for this activity
        val progress = activityProgressTracker.getProgressForActivity(mode.id)

        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 16, 20, 16)
            gravity = Gravity.CENTER_VERTICAL

            // Create gradient background based on progress
            val gradientDrawable = if (progress > 0) {
                // Calculate colors for progress-based gradient
                // We use a linear gradient from right to left (reverse of normal) so the filled part is on the left
                val gradientStart = ContextCompat.getColor(this@MainActivityWithBottomNav,
                    if (isDarkTheme) R.color.md_theme_dark_primary else R.color.md_theme_light_primary)

                // The end color is transparent primary color (or surface color)
                val gradientEnd = ContextCompat.getColor(this@MainActivityWithBottomNav,
                    if (isDarkTheme) R.color.md_theme_dark_surfaceContainerHigh else R.color.md_theme_light_surfaceContainerHigh)

                // Create the gradient drawable
                GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, intArrayOf(gradientEnd, gradientStart)).apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    // Set the gradient center point based on progress (1.0 - progress because we're using RIGHT_LEFT orientation)
                    setGradientCenter(1.0f - progress, 0.5f)
                }
            } else {
                // No progress, use solid background
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F5"))
                }
            }

            background = gradientDrawable

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 12)
            this.layoutParams = layoutParams

            isClickable = true
            setOnClickListener {
                soundManager.playButtonSound()
                when (mode.id) {
                    "flashcard_style" -> startFlashCardStyle()
                    "study_list" -> startStudyList()
                    "flagged_translations" -> startFlaggedTranslations()
                    "quiz" -> startQuiz()
                    "fill_blank" -> startFillBlank()
                    "sentence_unscramble" -> startSentenceUnscramble()
                    "vowel_hunt" -> startVowelHunt()
                }
            }
        }

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleText = TextView(this).apply {
            text = mode.title
            textSize = 16f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val descText = TextView(this).apply {
            val description = if (progress > 0) {
                // For quiz mode, check if there's an active quiz
                if (mode.id == "quiz") {
                    val quizStateManager = QuizStateManager(this@MainActivityWithBottomNav)
                    val quizState = quizStateManager.loadQuizState()

                    if (quizState != null && !quizState.isCompleted) {
                        // Show current quiz progress as primary metric
                        val quizProgress = ((quizState.currentQuestionIndex.toFloat() / quizState.quizLength) * 100).toInt()
                        "${mode.description} • Current quiz: $quizProgress% • Overall: ${(progress * 100).toInt()}%"
                    } else {
                        "${mode.description} • ${(progress * 100).toInt()}% complete"
                    }
                } else {
                    "${mode.description} • ${(progress * 100).toInt()}% complete"
                }
            } else {
                mode.description
            }

            text = description
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else Color.parseColor("#666666"))
            setPadding(0, 4, 0, 0)
        }

        val arrowText = TextView(this).apply {
            text = "→"
            textSize = 20f
            setTextColor(if (isDarkTheme) Color.parseColor("#888888") else Color.parseColor("#AAAAAA"))
        }

        textContainer.addView(titleText)
        textContainer.addView(descText)
        cardLayout.addView(textContainer)
        cardLayout.addView(arrowText)

        return cardLayout
    }
    
    private fun showStatsContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val progressManager = ProgressManager(this)
        val flashCardManager = FlashCardManagerV2(this)
        
        // Title
        val titleText = TextView(this).apply {
            text = "📊 Learning Analytics"
            textSize = 28f
            setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        val subtitleText = TextView(this).apply {
            text = "Track your Kikuyu learning progress 🚀"
            textSize = 16f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurfaceVariant))
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 20)
            setLineSpacing(4f, 1.2f)
        }
        
        // Get statistics
        val stats = progressManager.getProgressStats()
        
        // Create stats sections
        val overviewSection = createStatsOverviewSection(stats, flashCardManager, isDarkTheme)
        val flashCardSection = createStatsFlashCardSection(stats, flashCardManager, isDarkTheme)
        val quizSection = createStatsQuizSection(stats, isDarkTheme)
        val streakSection = createStatsStreakSection(stats, isDarkTheme)
        
        contentContainer.addView(titleText)
        contentContainer.addView(subtitleText)
        contentContainer.addView(overviewSection)
        contentContainer.addView(flashCardSection)
        contentContainer.addView(quizSection)
        contentContainer.addView(streakSection)
    }
    
    
    private fun showSettingsContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val sharedPreferences = getSharedPreferences("KikuyuFlashCardsSettings", MODE_PRIVATE)
        
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            textSize = 28f
            setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Audio Settings
        val audioSection = createSettingsSection("🔊 Audio Settings", listOf(
            "Show Pronunciation Button" to sharedPreferences.getBoolean("show_pronunciation_button", true),
            "Show Speak Both Button" to sharedPreferences.getBoolean("show_speak_both_button", true),
            "Auto-play Pronunciation" to sharedPreferences.getBoolean("auto_play_pronunciation", false)
        ), sharedPreferences, isDarkTheme)
        
        // App Settings
        val appSection = createSettingsSection("📱 App Settings", listOf(
            "Dark Theme" to ThemeManager.isDarkTheme(this)
        ), sharedPreferences, isDarkTheme)
        
        contentContainer.addView(titleText)
        contentContainer.addView(audioSection)
        contentContainer.addView(appSection)
    }
    
    // Navigation methods (these launch full activities for learning modes)
    private fun startCategorySelectorFlashCards() {
        val intent = Intent(this, CategorySelectorActivity::class.java)
        startActivity(intent)
    }
    
    private fun startStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }
    
    private fun startProblemWords() {
        val intent = Intent(this, ProblemWordsActivity::class.java)
        startActivity(intent)
    }
    
    private fun startSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    // Helper methods for embedded content
    private fun createStatsOverviewSection(stats: ProgressManager.ProgressStats, flashCardManager: FlashCardManagerV2, isDarkTheme: Boolean): LinearLayout {
        val totalActivities = stats.totalCardsViewed + stats.quizTotalAnswered
        val learningEfficiency = if (totalActivities > 0) {
            ((stats.quizCorrectAnswers.toFloat() / totalActivities) * 100).toInt()
        } else 0
        
        return createStatsCard("🎯 Learning Overview", listOf(
            "Total Activities" to "$totalActivities",
            "Learning Efficiency" to "$learningEfficiency%",
            "Available Phrases" to "${flashCardManager.getTotalEntries()}",
            "Categories" to "${flashCardManager.getAvailableCategories().size}"
        ), isDarkTheme)
    }
    
    private fun createStatsFlashCardSection(stats: ProgressManager.ProgressStats, flashCardManager: FlashCardManagerV2, isDarkTheme: Boolean): LinearLayout {
        val averageSwipesPerCard = if (stats.totalCardsViewed > 0) {
            "%.1f".format(stats.totalSwipes.toFloat() / stats.totalCardsViewed)
        } else "0.0"
        
        return createStatsCard("📚 Flash Cards Progress", listOf(
            "Cards Viewed" to "${stats.totalCardsViewed}",
            "Total Swipes" to "${stats.totalSwipes}",
            "Avg Swipes/Card" to averageSwipesPerCard,
            "Completion" to "${(stats.totalCardsViewed * 100 / flashCardManager.getTotalEntries().coerceAtLeast(1))}%"
        ), isDarkTheme)
    }
    
    private fun createStatsQuizSection(stats: ProgressManager.ProgressStats, isDarkTheme: Boolean): LinearLayout {
        val wrongAnswers = stats.quizTotalAnswered - stats.quizCorrectAnswers
        val accuracyColor = when {
            stats.quizAccuracy >= 80 -> "🟢"
            stats.quizAccuracy >= 60 -> "🟡"
            else -> "🔴"
        }
        
        return createStatsCard("🧠 Quiz Performance", listOf(
            "Questions Answered" to "${stats.quizTotalAnswered}",
            "Correct Answers" to "${stats.quizCorrectAnswers}",
            "Wrong Answers" to "$wrongAnswers",
            "Accuracy $accuracyColor" to "${"%.1f".format(stats.quizAccuracy)}%"
        ), isDarkTheme)
    }
    
    private fun createStatsStreakSection(stats: ProgressManager.ProgressStats, isDarkTheme: Boolean): LinearLayout {
        val streakEmoji = when {
            stats.currentStreak >= 10 -> "🔥🔥🔥"
            stats.currentStreak >= 5 -> "🔥🔥"
            stats.currentStreak > 0 -> "🔥"
            else -> "💤"
        }
        
        return createStatsCard("⚡ Learning Streaks", listOf(
            "Current Streak $streakEmoji" to "${stats.currentStreak}",
            "Best Streak 🏆" to "${stats.bestStreak}",
            "Streak Status" to getStreakStatus(stats.currentStreak),
            "Next Milestone" to getNextMilestone(stats.currentStreak)
        ), isDarkTheme)
    }
    
    private fun createStatsCard(title: String, content: List<Pair<String, String>>, isDarkTheme: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_surfaceContainerHigh) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_surfaceContainerHigh))
            }
            setBackground(background)
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
            
            elevation = 4f
            
            // Title
            val titleView = TextView(this@MainActivityWithBottomNav).apply {
                text = title
                textSize = 18f
                setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
                setPadding(0, 0, 0, 12)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            addView(titleView)
            
            // Content
            content.forEach { (label, value) ->
                val itemLayout = LinearLayout(this@MainActivityWithBottomNav).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                    weightSum = 2f
                }
                
                val labelView = TextView(this@MainActivityWithBottomNav).apply {
                    text = label
                    textSize = 14f
                    setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_onSurface) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurface))
                    this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                
                val valueView = TextView(this@MainActivityWithBottomNav).apply {
                    text = value
                    textSize = 15f
                    setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
                    gravity = Gravity.END
                    this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                
                itemLayout.addView(labelView)
                itemLayout.addView(valueView)
                addView(itemLayout)
            }
        }
    }
    
    private fun createSettingsSection(title: String, settings: List<Pair<String, Boolean>>, sharedPreferences: android.content.SharedPreferences, isDarkTheme: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_surfaceContainerHigh) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_surfaceContainerHigh))
            }
            setBackground(background)
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
            
            elevation = 4f
            
            // Title
            val titleView = TextView(this@MainActivityWithBottomNav).apply {
                text = title
                textSize = 18f
                setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_primary) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
                setPadding(0, 0, 0, 12)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            addView(titleView)
            
            // Settings
            settings.forEach { (label, isEnabled) ->
                val itemLayout = LinearLayout(this@MainActivityWithBottomNav).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                    gravity = Gravity.CENTER_VERTICAL
                }
                
                val labelView = TextView(this@MainActivityWithBottomNav).apply {
                    text = label
                    textSize = 16f
                    setTextColor(if (isDarkTheme) ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_dark_onSurface) else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurface))
                    this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                
                val switchView = Switch(this@MainActivityWithBottomNav).apply {
                    isChecked = isEnabled
                    setOnCheckedChangeListener { _, isChecked ->
                        soundManager.playButtonSound()
                        val editor = sharedPreferences.edit()
                        when (label) {
                            "Show Pronunciation Button" -> editor.putBoolean("show_pronunciation_button", isChecked)
                            "Show Speak Both Button" -> editor.putBoolean("show_speak_both_button", isChecked)
                            "Auto-play Pronunciation" -> editor.putBoolean("auto_play_pronunciation", isChecked)
                            "Dark Theme" -> {
                                editor.putBoolean("dark_theme", isChecked)
                                // Note: Theme change would require activity restart to take effect
                            }
                        }
                        editor.apply()
                    }
                }
                
                itemLayout.addView(labelView)
                itemLayout.addView(switchView)
                addView(itemLayout)
            }
        }
    }
    
    private fun getStreakStatus(currentStreak: Int): String {
        return when {
            currentStreak >= 20 -> "Legend!"
            currentStreak >= 15 -> "Expert"
            currentStreak >= 10 -> "Advanced"
            currentStreak >= 5 -> "Getting Good"
            currentStreak > 0 -> "Building"
            else -> "Start Fresh"
        }
    }
    
    private fun getNextMilestone(currentStreak: Int): String {
        return when {
            currentStreak < 5 -> "${5 - currentStreak} to Good"
            currentStreak < 10 -> "${10 - currentStreak} to Advanced"
            currentStreak < 15 -> "${15 - currentStreak} to Expert"
            currentStreak < 20 -> "${20 - currentStreak} to Legend"
            else -> "Max Achieved!"
        }
    }

    // Activity Launchers

    private fun startStudyList() {
        val intent = Intent(this, StudyListActivity::class.java)
        startActivity(intent)
    }

    private fun startFlashCardStyle(showCategorySelection: Boolean = true) {
        val intent = Intent(this, FlashCardStyleActivity::class.java).apply {
            putExtra("show_category_selection", showCategorySelection)
        }
        startActivity(intent)
    }

    private fun startFlaggedTranslations() {
        val intent = Intent(this, FlaggedTranslationsActivity::class.java)
        startActivity(intent)
    }

    private fun startQuiz() {
        val intent = Intent(this, QuizActivity::class.java)
        startActivity(intent)
    }

    private fun startFillBlank() {
        val intent = Intent(this, FillInTheBlankActivity::class.java)
        startActivity(intent)
    }

    private fun startSentenceUnscramble() {
        val intent = Intent(this, SentenceUnscrambleActivity::class.java)
        startActivity(intent)
    }

    private fun startVowelHunt() {
        val intent = Intent(this, VowelHuntActivity::class.java)
        startActivity(intent)
    }

    // Data classes
    data class NavItem(val icon: String, val label: String, val id: String)
    data class LearningMode(val title: String, val description: String, val id: String)
}