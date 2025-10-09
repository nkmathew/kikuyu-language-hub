package com.nkmathew.kikuyuflashcards

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class CategorySelectorActivity : ComponentActivity() {
    
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    private lateinit var progressManager: ProgressManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        progressManager = ProgressManager(this)
        
        createCategoryUI()
    }
    
    private fun createCategoryUI() {
        // Create scrollable root layout
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_background))
        }
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 24, 24) // Top padding will be set by insets
        }
        
        // Header section
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        
        val titleText = TextView(this).apply {
            text = "🎆 Choose Your Focus"
            textSize = 32f
            setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        val subtitleText = TextView(this).apply {
            text = "Select a category to start your Kikuyu learning journey ✨"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onSurfaceVariant))
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 20)
            setLineSpacing(4f, 1.2f)
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(subtitleText)
        
        // Stats overview
        val statsLayout = createStatsOverview()
        
        // Category buttons layout
        val categoriesLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }
        
        val categoriesTitle = TextView(this).apply {
            text = "🎯 Learning Categories"
            textSize = 22f
            setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        categoriesLayout.addView(categoriesTitle)
        
        // Check if we should show continue learning option
        if (flashCardManager.shouldShowContinueOption()) {
            val continueMessage = flashCardManager.getContinueLearningMessage()
            if (continueMessage != null) {
                val continueButton = createCategoryButton(
                    icon = "⏯️",
                    title = "Continue Learning",
                    subtitle = continueMessage,
                    count = 0,
                    isPrimary = true
                ) {
                    soundManager.playButtonSound()
                    // Restore the last category and position
                    val sessionInfo = flashCardManager.getPositionManager().getLastSessionInfo()
                    flashCardManager.setCategory(sessionInfo.lastCategory)
                    startFlashCardsWithMode(sessionInfo.lastCategory?.let { Phrase.getCategoryDisplayName(it) } ?: "All Categories")
                }
                categoriesLayout.addView(continueButton)
            }
        }
        
        // Get available categories and create enhanced buttons
        val availableCategories = flashCardManager.getAvailableCategories()
        
        // Add "All Categories" button with enhanced styling
        val allCategoriesButton = createCategoryButton(
            icon = "📚",
            title = "All Categories",
            subtitle = "Practice everything",
            count = flashCardManager.getTotalPhrases(),
            isPrimary = false
        ) {
            soundManager.playButtonSound()
            flashCardManager.setCategory(null)
            startFlashCardsWithMode("All Categories")
        }
        categoriesLayout.addView(allCategoriesButton)
        
        // Add enhanced category buttons
        availableCategories.forEach { category ->
            val displayName = Phrase.getCategoryDisplayName(category)
            val count = flashCardManager.getTotalPhrasesInCategory(category)
            val (icon, title) = extractIconAndTitle(displayName)
            val subtitle = getCategoryDescription(category)
            
            val categoryButton = createCategoryButton(
                icon = icon,
                title = title,
                subtitle = subtitle,
                count = count,
                isPrimary = false
            ) {
                soundManager.playButtonSound()
                flashCardManager.setCategory(category)
                startFlashCardsWithMode(displayName)
            }
            categoriesLayout.addView(categoryButton)
        }
        
        // Action buttons
        val actionLayout = createActionButtons()
        
        // Add all views to root layout
        rootLayout.addView(headerLayout)
        rootLayout.addView(statsLayout)
        rootLayout.addView(categoriesLayout)
        rootLayout.addView(actionLayout)
        
        scrollView.addView(rootLayout)
        setContentView(scrollView)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            rootLayout.updatePadding(top = 24) // Add 24dp top margin to content
            insets
        }
    }
    
    private fun createStatsOverview(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_primaryContainer))
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 24)
            this.layoutParams = layoutParams
            
            val stats = progressManager.getProgressStats()
            
            // Create stat items
            addView(createStatItem("📖", "${stats.totalCardsViewed}", "Cards Viewed"))
            addView(createStatItem("🎯", "${stats.quizCorrectAnswers}", "Quiz Correct"))
            addView(createStatItem("🔥", "${stats.currentStreak}", "Current Streak"))
            addView(createStatItem("⏱️", "${stats.sessionCount}", "Sessions"))
        }
    }
    
    private fun createStatItem(icon: String, value: String, label: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            
            val layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            this.layoutParams = layoutParams
            
            val iconView = TextView(this@CategorySelectorActivity).apply {
                text = icon
                textSize = 20f
                gravity = Gravity.CENTER
            }
            
            val valueView = TextView(this@CategorySelectorActivity).apply {
                text = value
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onPrimaryContainer))
                gravity = Gravity.CENTER
            }
            
            val labelView = TextView(this@CategorySelectorActivity).apply {
                text = label
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onPrimaryContainer))
                gravity = Gravity.CENTER
            }
            
            addView(iconView)
            addView(valueView)
            addView(labelView)
        }
    }
    
    private fun createCategoryButton(
        icon: String,
        title: String,
        subtitle: String,
        count: Int,
        isPrimary: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 20, 24, 20)
            gravity = Gravity.CENTER_VERTICAL
            
            // Create enhanced gradient background
            val gradientDrawable = if (isPrimary) {
                GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_primary),
                        ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_primaryContainer)
                    )
                )
            } else {
                GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_surfaceContainerHigh),
                        ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_surfaceContainer)
                    )
                )
            }.apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
            }
            background = gradientDrawable
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
            
            // Enhanced elevation and shadow
            elevation = if (isPrimary) 8f else 4f
            translationZ = 2f
            
            setOnClickListener { onClick() }
            
            // Enhanced icon with better styling
            val iconView = TextView(this@CategorySelectorActivity).apply {
                text = icon
                textSize = 28f
                setPadding(0, 0, 20, 0)
                gravity = Gravity.CENTER
            }
            
            // Text content
            val textLayout = LinearLayout(this@CategorySelectorActivity).apply {
                orientation = LinearLayout.VERTICAL
                this.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            
            val titleView = TextView(this@CategorySelectorActivity).apply {
                text = title
                textSize = 20f
                val textColor = if (isPrimary) {
                    Color.WHITE
                } else {
                    ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onSurface)
                }
                setTextColor(textColor)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            
            val subtitleView = TextView(this@CategorySelectorActivity).apply {
                text = subtitle
                textSize = 15f
                val textColor = if (isPrimary) {
                    Color.WHITE
                } else {
                    ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onSurfaceVariant)
                }
                setTextColor(textColor)
                setPadding(0, 4, 0, 0)
            }
            
            textLayout.addView(titleView)
            textLayout.addView(subtitleView)
            
            // Enhanced count badge (hide for continue button)
            val countView = TextView(this@CategorySelectorActivity).apply {
                if (count > 0) {
                    text = "$count"
                    visibility = android.view.View.VISIBLE
                } else {
                    text = "▶"
                    visibility = android.view.View.VISIBLE
                }
                textSize = 16f
                setPadding(16, 10, 16, 10)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                
                // Create circular badge background
                val badgeBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.achievement_gold))
                }
                background = badgeBg
                
                elevation = 2f
            }
            
            addView(iconView)
            addView(textLayout)
            addView(countView)
        }
    }
    
    private fun createActionButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
            
            val backButton = Button(this@CategorySelectorActivity).apply {
                text = "← Back to Home"
                textSize = 16f
                setOnClickListener { 
                    soundManager.playButtonSound()
                    finish() 
                }
                setPadding(24, 16, 24, 16)
                setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onSecondary))
                setBackgroundColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_secondary))
                
                val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                buttonParams.setMargins(0, 0, 16, 0)
                this.layoutParams = buttonParams
            }
            
            val quizButton = Button(this@CategorySelectorActivity).apply {
                text = "🧠 Take Quiz"
                textSize = 16f
                setOnClickListener { 
                    soundManager.playButtonSound()
                    startActivity(Intent(this@CategorySelectorActivity, QuizActivity::class.java))
                }
                setPadding(24, 16, 24, 16)
                setTextColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_onTertiary))
                setBackgroundColor(ContextCompat.getColor(this@CategorySelectorActivity, R.color.md_theme_light_tertiary))
            }
            
            addView(backButton)
            addView(quizButton)
        }
    }
    
    private fun extractIconAndTitle(displayName: String): Pair<String, String> {
        val parts = displayName.split(" ", limit = 2)
        return if (parts.size >= 2) {
            parts[0] to parts[1]
        } else {
            "📚" to displayName
        }
    }
    
    private fun getCategoryDescription(category: String): String {
        return when (category) {
            "greetings" -> "Essential daily greetings"
            "emotions" -> "Express feelings & moods"
            "basic_words" -> "Fundamental vocabulary"
            "verbs" -> "Action words & movements"
            "nouns" -> "People, places & things"
            "questions" -> "Asking & answering"
            "time" -> "Days, months & time"
            else -> "Learn these phrases"
        }
    }
    
    private fun startFlashCardsWithMode(categoryName: String) {
        val intent = Intent(this, FlashCardActivity::class.java).apply {
            putExtra("category_mode", categoryName)
        }
        startActivity(intent)
        finish() // Close the category selector
    }
}