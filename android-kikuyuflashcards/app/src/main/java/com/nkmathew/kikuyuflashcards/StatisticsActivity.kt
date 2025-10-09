package com.nkmathew.kikuyuflashcards

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
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : ComponentActivity() {
    
    private lateinit var progressManager: ProgressManager
    private lateinit var flashCardManager: FlashCardManager
    private lateinit var soundManager: SoundManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        progressManager = ProgressManager(this)
        flashCardManager = FlashCardManager(this)
        soundManager = SoundManager(this)
        
        createStatsUI()
    }
    
    private fun createStatsUI() {
        // Create scrollable root layout for better content display
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_background))
        }
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 24, 24) // Top padding will be set by insets
        }
        
        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        
        val titleText = TextView(this).apply {
            text = "📊 Learning Analytics"
            textSize = 32f
            setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        val subtitleText = TextView(this).apply {
            text = "Track your Kikuyu learning progress 🚀"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_onSurfaceVariant))
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 20)
            setLineSpacing(4f, 1.2f)
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(subtitleText)
        
        // Get statistics
        val stats = progressManager.getProgressStats()
        
        // Create enhanced stats sections
        rootLayout.addView(headerLayout)
        rootLayout.addView(createOverviewSection(stats))
        rootLayout.addView(createFlashCardSection(stats))
        rootLayout.addView(createQuizSection(stats))
        rootLayout.addView(createStreakSection(stats))
        rootLayout.addView(createSessionSection(stats))
        rootLayout.addView(createActionButtons())
        
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
    
    private fun createStatsCard(title: String, content: List<Pair<String, String>>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            
            // Create enhanced card background with gradient
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_surfaceContainerHigh),
                    ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_surfaceContainer)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
            }
            background = gradientDrawable
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 20)
            this.layoutParams = layoutParams
            
            // Enhanced elevation and shadow
            elevation = 6f
            translationZ = 2f
            
            // Enhanced card title
            val titleView = TextView(this@StatisticsActivity).apply {
                text = title
                textSize = 20f
                setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_primary))
                setPadding(0, 0, 0, 16)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            addView(titleView)
            
            // Card content
            content.forEach { (label, value) ->
                val itemLayout = LinearLayout(this@StatisticsActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                    weightSum = 2f
                }
                
                val labelView = TextView(this@StatisticsActivity).apply {
                    text = label
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_onSurface))
                    this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                
                val valueView = TextView(this@StatisticsActivity).apply {
                    text = value
                    textSize = 17f
                    setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_primary))
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
    
    private fun createOverviewSection(stats: ProgressManager.ProgressStats): LinearLayout {
        val totalActivities = stats.totalCardsViewed + stats.quizTotalAnswered
        val learningEfficiency = if (totalActivities > 0) {
            ((stats.quizCorrectAnswers.toFloat() / totalActivities) * 100).toInt()
        } else 0
        
        return createStatsCard("🎯 Learning Overview", listOf(
            "Total Activities" to "$totalActivities",
            "Learning Efficiency" to "$learningEfficiency%",
            "Available Phrases" to "${flashCardManager.getTotalPhrases()}",
            "Categories" to "${flashCardManager.getAvailableCategories().size}"
        ))
    }
    
    private fun createFlashCardSection(stats: ProgressManager.ProgressStats): LinearLayout {
        val averageSwipesPerCard = if (stats.totalCardsViewed > 0) {
            "%.1f".format(stats.totalSwipes.toFloat() / stats.totalCardsViewed)
        } else "0.0"
        
        return createStatsCard("📚 Flash Cards Progress", listOf(
            "Cards Viewed" to "${stats.totalCardsViewed}",
            "Total Swipes" to "${stats.totalSwipes}",
            "Avg Swipes/Card" to averageSwipesPerCard,
            "Completion" to "${(stats.totalCardsViewed * 100 / flashCardManager.getTotalPhrases().coerceAtLeast(1))}%"
        ))
    }
    
    private fun createQuizSection(stats: ProgressManager.ProgressStats): LinearLayout {
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
        ))
    }
    
    private fun createStreakSection(stats: ProgressManager.ProgressStats): LinearLayout {
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
        ))
    }
    
    private fun createSessionSection(stats: ProgressManager.ProgressStats): LinearLayout {
        val lastSessionText = if (stats.lastSessionDate > 0) {
            val date = Date(stats.lastSessionDate)
            val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            formatter.format(date)
        } else "No sessions"
        
        return createStatsCard("⏰ Session Analytics", listOf(
            "Total Sessions" to "${stats.sessionCount}",
            "Total Time" to formatDuration(stats.totalSessionTime),
            "Average Session" to formatDuration(stats.averageSessionTime),
            "Last Session" to lastSessionText
        ))
    }
    
    private fun createActionButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 0)
            this.layoutParams = layoutParams
            
            // Reset progress button
            val resetButton = Button(this@StatisticsActivity).apply {
                text = "🔄 Reset Progress"
                textSize = 16f
                setOnClickListener { 
                    soundManager.playButtonSound()
                    progressManager.resetProgress()
                    recreate() // Refresh the activity to show updated stats
                }
                setPadding(24, 16, 24, 16)
                setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_onError))
                setBackgroundColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_error))
                
                val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                buttonParams.setMargins(0, 0, 16, 0)
                this.layoutParams = buttonParams
            }
            
            // Back button
            val backButton = Button(this@StatisticsActivity).apply {
                text = "← Back to Home"
                textSize = 16f
                setOnClickListener { 
                    soundManager.playButtonSound()
                    finish() 
                }
                setPadding(24, 16, 24, 16)
                setTextColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_onPrimary))
                setBackgroundColor(ContextCompat.getColor(this@StatisticsActivity, R.color.md_theme_light_primary))
            }
            
            addView(resetButton)
            addView(backButton)
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
    
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        progressManager.endSession()
    }
}

