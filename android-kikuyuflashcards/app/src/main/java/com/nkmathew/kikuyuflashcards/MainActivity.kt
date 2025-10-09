package com.nkmathew.kikuyuflashcards

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivity : ComponentActivity() {
    
    private lateinit var soundManager: SoundManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        soundManager = SoundManager(this)
        
        // Check theme setting using proper ThemeManager
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121212") else ContextCompat.getColor(this, R.color.md_theme_light_background)
        
        // Create enhanced layout with modern background
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 32, 32) // Top padding will be set by insets
            gravity = Gravity.CENTER
            setBackgroundColor(backgroundColor)
        }
        
        // Enhanced title with Material 3 styling
        val titleText = TextView(this).apply {
            text = "🇰🇪 Kikuyu Flash Cards"
            textSize = 36f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Enhanced welcome message
        val welcomeText = TextView(this).apply {
            text = "Wĩ mwega! Welcome to your Kikuyu learning journey"
            textSize = 18f
            setTextColor(if (isDarkTheme) Color.parseColor("#CCCCCC") else ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_onSurfaceVariant))
            setPadding(16, 0, 16, 24)
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.2f)
        }
        
        // Enhanced features list
        val featuresText = TextView(this).apply {
            text = "✨ Enhanced Features:\n\n🎨 Beautiful Material 3 Design\n👆 Intuitive Swipe Navigation\n🎯 Interactive Quiz Mode\n🔊 Audio Pronunciation\n📊 Progress Analytics\n📚 Organized Categories"
            textSize = 16f
            setTextColor(if (isDarkTheme) Color.parseColor("#EEEEEE") else ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_onSurface))
            setPadding(24, 20, 24, 32)
            gravity = Gravity.CENTER
            setLineSpacing(6f, 1.3f)
            
            // Add subtle background
            val featuresBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(if (isDarkTheme) Color.parseColor("#1E1E1E") else ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_surfaceContainerLowest))
                cornerRadius = 16f
            }
            background = featuresBg
        }
        
        // Enhanced start button
        val startButton = Button(this).apply {
            text = "🚀 Start Learning Flash Cards"
            textSize = 18f
            setOnClickListener { 
                soundManager.playButtonSound()
                startFlashCards() 
            }
            setPadding(40, 24, 40, 24)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Create primary gradient background
            val buttonBg = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_primary),
                    ContextCompat.getColor(this@MainActivity, R.color.button_hover)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
            }
            background = buttonBg
            
            elevation = 8f
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }
        
        // Enhanced quiz button
        val quizButton = Button(this).apply {
            text = "🧠 Test Your Knowledge (Quiz)"
            textSize = 18f
            setOnClickListener { 
                soundManager.playButtonSound()
                startQuiz() 
            }
            setPadding(40, 24, 40, 24)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Create secondary gradient background
            val buttonBg = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@MainActivity, R.color.quiz_purple),
                    ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_tertiary)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
            }
            background = buttonBg
            
            elevation = 6f
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }
        
        // Enhanced statistics button
        val statsButton = Button(this).apply {
            text = "📊 View Learning Statistics"
            textSize = 18f
            setOnClickListener { 
                soundManager.playButtonSound()
                startStatistics() 
            }
            setPadding(40, 24, 40, 24)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Create tertiary gradient background
            val buttonBg = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_secondary),
                    ContextCompat.getColor(this@MainActivity, R.color.success_green)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
            }
            background = buttonBg
            
            elevation = 6f
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.layoutParams = layoutParams
        }
        
        // Enhanced problem words button
        val problemWordsButton = Button(this).apply {
            text = "🎯 Practice Problem Words"
            textSize = 18f
            setOnClickListener { 
                soundManager.playButtonSound()
                startProblemWords() 
            }
            setPadding(40, 24, 40, 24)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Create problem words gradient background (warning themed)
            val buttonBg = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ContextCompat.getColor(this@MainActivity, R.color.warning_orange),
                    ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_error)
                )
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 28f
            }
            background = buttonBg
            
            elevation = 6f
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            this.layoutParams = layoutParams
        }
        
        // Enhanced settings button
        val settingsButton = Button(this).apply {
            text = "⚙️ Settings"
            textSize = 16f
            setOnClickListener { 
                soundManager.playButtonSound()
                startSettings() 
            }
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Create settings gradient background
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_outline))
            }
            background = buttonBg
            
            elevation = 4f
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 24, 0, 0)
            layoutParams.gravity = Gravity.CENTER
            this.layoutParams = layoutParams
        }
        
        rootLayout.addView(titleText)
        rootLayout.addView(welcomeText)
        rootLayout.addView(featuresText)
        rootLayout.addView(startButton)
        rootLayout.addView(quizButton)
        rootLayout.addView(statsButton)
        rootLayout.addView(problemWordsButton)
        rootLayout.addView(settingsButton)
        
        setContentView(rootLayout)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top + 48) // Add 48dp top margin
            insets
        }
    }
    
    private fun startFlashCards() {
        val intent = Intent(this, CategorySelectorActivity::class.java)
        startActivity(intent)
    }
    
    private fun startQuiz() {
        val intent = Intent(this, QuizActivity::class.java)
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
}

// Extension function to set margins
fun Button.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(left, top, right, bottom)
    this.layoutParams = params
}