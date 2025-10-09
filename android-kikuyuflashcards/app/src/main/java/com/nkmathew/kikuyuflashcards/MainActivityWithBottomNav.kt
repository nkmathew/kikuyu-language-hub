package com.nkmathew.kikuyuflashcards

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivityWithBottomNav : ComponentActivity() {
    
    private lateinit var soundManager: SoundManager
    private lateinit var contentContainer: LinearLayout
    private lateinit var bottomNavLayout: LinearLayout
    private var currentActiveTab = "home"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        soundManager = SoundManager(this)
        
        createBottomNavigationUI()
    }
    
    private fun createBottomNavigationUI() {
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121212") else ContextCompat.getColor(this, R.color.md_theme_light_background)
        
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
            NavItem("📊", "Stats", "stats"),
            NavItem("🎯", "Practice", "practice"),
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
                    "stats" -> showStatsContent()
                    "practice" -> showPracticeContent()
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
            setTextColor(ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
        val navItems = listOf("home", "learn", "stats", "practice", "settings")
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
        val activeColor = ContextCompat.getColor(this, R.color.md_theme_light_primary)
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
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
        
        // Quick stats card
        val statsCard = createStatsCard(isDarkTheme)
        
        // Recent activity card
        val activityCard = createActivityCard(isDarkTheme)
        
        contentContainer.addView(titleText)
        contentContainer.addView(welcomeText)
        contentContainer.addView(statsCard)
        contentContainer.addView(activityCard)
    }
    
    private fun showLearnContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        
        val titleText = TextView(this).apply {
            text = "📚 Learning Modes"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Learning mode cards
        val learningModes = listOf(
            LearningMode("🚀 Flash Cards", "Interactive card-based learning", "flashcards"),
            LearningMode("✨ Enhanced Cards", "Rich content with metadata", "enhanced_flashcards"),
            LearningMode("📋 Study List", "Side-by-side learning mode", "study_list"),
            LearningMode("🚩 Flagged Translations", "Review flagged translations", "flagged_translations"),
            LearningMode("🧠 Quiz Mode", "Test your knowledge", "quiz"),
            LearningMode("✏️ Fill Blanks", "Complete the sentences", "fill_blank"),
            LearningMode("📖 Cloze Test", "Reading comprehension", "cloze"),
            LearningMode("🎮 Games", "Fun learning activities", "games")
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
        
        val statsText = TextView(this).apply {
            text = "• Words learned: 127\n• Accuracy: 78%\n• Current streak: 5 days"
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
        
        val actionButton = Button(this).apply {
            text = "Continue Learning"
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setTextColor(Color.WHITE)
            
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            }
            background = buttonBg
            
            setOnClickListener {
                soundManager.playButtonSound()
                startCategorySelectorFlashCards()
            }
        }
        
        cardLayout.addView(titleText)
        cardLayout.addView(actionButton)
        
        return cardLayout
    }
    
    private fun createLearningModeCard(mode: LearningMode, isDarkTheme: Boolean): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 16, 20, 16)
            gravity = Gravity.CENTER_VERTICAL
            
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
            layoutParams.setMargins(0, 0, 0, 12)
            this.layoutParams = layoutParams
            
            isClickable = true
            setOnClickListener {
                soundManager.playButtonSound()
                when (mode.id) {
                    "flashcards" -> startCategorySelectorFlashCards()
                    "enhanced_flashcards" -> startEnhancedFlashCards()
                    "study_list" -> startStudyList()
                    "flagged_translations" -> startFlaggedTranslations()
                    "quiz" -> startQuiz()
                    "fill_blank" -> startFillBlank()
                    "cloze" -> startCloze()
                    "games" -> startGames()
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
            text = mode.description
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
        val flashCardManager = FlashCardManager(this)
        
        // Title
        val titleText = TextView(this).apply {
            text = "📊 Learning Analytics"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
    
    private fun showPracticeContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        
        val titleText = TextView(this).apply {
            text = "🎯 Practice Problem Words"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        val descText = TextView(this).apply {
            text = "Focus on words you've struggled with in the past"
            textSize = 16f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurfaceVariant))
            setPadding(16, 0, 16, 24)
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.2f)
        }
        
        val practiceButton = Button(this).apply {
            text = "Start Practice Session"
            textSize = 18f
            setPadding(40, 24, 40, 24)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
                setColor(ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.warning_orange))
            }
            background = buttonBg
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
            
            setOnClickListener {
                soundManager.playButtonSound()
                startActivity(Intent(this@MainActivityWithBottomNav, ProblemWordsPracticeActivity::class.java))
            }
        }
        
        contentContainer.addView(titleText)
        contentContainer.addView(descText)
        contentContainer.addView(practiceButton)
    }
    
    private fun showSettingsContent() {
        contentContainer.removeAllViews()
        
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val sharedPreferences = getSharedPreferences("KikuyuFlashCardsSettings", MODE_PRIVATE)
        
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
    private fun createStatsOverviewSection(stats: ProgressManager.ProgressStats, flashCardManager: FlashCardManager, isDarkTheme: Boolean): LinearLayout {
        val totalActivities = stats.totalCardsViewed + stats.quizTotalAnswered
        val learningEfficiency = if (totalActivities > 0) {
            ((stats.quizCorrectAnswers.toFloat() / totalActivities) * 100).toInt()
        } else 0
        
        return createStatsCard("🎯 Learning Overview", listOf(
            "Total Activities" to "$totalActivities",
            "Learning Efficiency" to "$learningEfficiency%",
            "Available Phrases" to "${flashCardManager.getTotalPhrases()}",
            "Categories" to "${flashCardManager.getAvailableCategories().size}"
        ), isDarkTheme)
    }
    
    private fun createStatsFlashCardSection(stats: ProgressManager.ProgressStats, flashCardManager: FlashCardManager, isDarkTheme: Boolean): LinearLayout {
        val averageSwipesPerCard = if (stats.totalCardsViewed > 0) {
            "%.1f".format(stats.totalSwipes.toFloat() / stats.totalCardsViewed)
        } else "0.0"
        
        return createStatsCard("📚 Flash Cards Progress", listOf(
            "Cards Viewed" to "${stats.totalCardsViewed}",
            "Total Swipes" to "${stats.totalSwipes}",
            "Avg Swipes/Card" to averageSwipesPerCard,
            "Completion" to "${(stats.totalCardsViewed * 100 / flashCardManager.getTotalPhrases().coerceAtLeast(1))}%"
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
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_surfaceContainerHigh))
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
                setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
                    setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurface))
                    this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                
                val valueView = TextView(this@MainActivityWithBottomNav).apply {
                    text = value
                    textSize = 15f
                    setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_surfaceContainerHigh))
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
                setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_primary))
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
                    setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivityWithBottomNav, R.color.md_theme_light_onSurface))
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
    private fun startFlashCards() {
        val intent = Intent(this, FlashCardActivity::class.java)
        startActivity(intent)
    }

    private fun startEnhancedFlashCards() {
        val intent = Intent(this, EnhancedFlashCardActivity::class.java)
        startActivity(intent)
    }

    private fun startStudyList() {
        val intent = Intent(this, StudyListActivity::class.java)
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

    private fun startCloze() {
        val intent = Intent(this, ClozeTestActivity::class.java)
        startActivity(intent)
    }

    private fun startGames() {
        val intent = Intent(this, MultipleResponseGameActivity::class.java)
        startActivity(intent)
    }

    // Data classes
    data class NavItem(val icon: String, val label: String, val id: String)
    data class LearningMode(val title: String, val description: String, val id: String)
}