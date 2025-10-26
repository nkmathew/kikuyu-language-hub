package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SettingsActivity : ComponentActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var soundManager: SoundManager
    
    companion object {
        const val PREFS_NAME = "KikuyuFlashCardsSettings"
        const val PREF_SHOW_PRONUNCIATION_BUTTON = "show_pronunciation_button"
        const val PREF_SHOW_SPEAK_BOTH_BUTTON = "show_speak_both_button"
        const val PREF_SHOW_KIKUYU_SOUND_BUTTON = "show_kikuyu_sound_button"
        const val PREF_AUTO_PLAY_PRONUNCIATION = "auto_play_pronunciation"
        
        // Helper methods to check settings from other activities
        fun isShowPronunciationButtonEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SHOW_PRONUNCIATION_BUTTON, true)
        }
        
        fun isShowSpeakBothButtonEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SHOW_SPEAK_BOTH_BUTTON, true)
        }
        
        fun isShowKikuyuSoundButtonEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SHOW_KIKUYU_SOUND_BUTTON, false) // Default disabled
        }
        
        fun isAutoPlayPronunciationEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_AUTO_PLAY_PRONUNCIATION, false)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        soundManager = SoundManager(this)
        
        createSettingsUI()
    }
    
    private fun createSettingsUI() {
        // Use proper theme manager
        val isDarkTheme = ThemeManager.isDarkTheme(this)
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121212") else ContextCompat.getColor(this, R.color.md_theme_light_background)
        val textColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 32, 32) // Top padding will be set by insets
            gravity = Gravity.TOP
            setBackgroundColor(backgroundColor)
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "⚙️ Settings"
            textSize = 28f
            setTextColor(if (isDarkTheme) Color.WHITE else ContextCompat.getColor(this@SettingsActivity, R.color.md_theme_light_primary))
            setPadding(0, 0, 0, 32)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        rootLayout.addView(titleText)
        
        // Audio Settings Section
        addSectionHeader(rootLayout, "🔊 Audio Settings", textColor)
        
        // Show pronunciation button toggle
        val pronunciationToggle = createToggleRow(
            title = "Show Pronunciation Buttons",
            description = "Display 🔊 buttons on flashcards",
            prefKey = PREF_SHOW_PRONUNCIATION_BUTTON,
            defaultValue = true,
            textColor = textColor,
            isDarkTheme = isDarkTheme
        )
        rootLayout.addView(pronunciationToggle)
        
        // Show speak both languages button toggle
        val speakBothToggle = createToggleRow(
            title = "Show \"Speak Both Languages\" Button",
            description = "Display button to speak English and Kikuyu",
            prefKey = PREF_SHOW_SPEAK_BOTH_BUTTON,
            defaultValue = true,
            textColor = textColor,
            isDarkTheme = isDarkTheme
        )
        rootLayout.addView(speakBothToggle)
        
        // Show Kikuyu sound button toggle
        val kikuyuSoundToggle = createToggleRow(
            title = "Show Kikuyu Sound Button",
            description = "Display 🔊 button when Kikuyu translation is shown",
            prefKey = PREF_SHOW_KIKUYU_SOUND_BUTTON,
            defaultValue = false, // Default disabled as requested
            textColor = textColor,
            isDarkTheme = isDarkTheme
        )
        rootLayout.addView(kikuyuSoundToggle)
        
        // Auto-play pronunciation toggle
        val autoPlayToggle = createToggleRow(
            title = "Auto-play Pronunciation",
            description = "Automatically speak when cards are shown",
            prefKey = PREF_AUTO_PLAY_PRONUNCIATION,
            defaultValue = false,
            textColor = textColor,
            isDarkTheme = isDarkTheme
        )
        rootLayout.addView(autoPlayToggle)
        
        // Appearance Settings Section
        addSectionHeader(rootLayout, "🎨 Appearance", textColor)
        
        // Theme selector
        val themeSelector = createThemeSelector(textColor, isDarkTheme)
        rootLayout.addView(themeSelector)
        
        // Test Audio Section
        addSectionHeader(rootLayout, "🎵 Test Audio", textColor)
        
        val testButtonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }
        
        // Test English pronunciation
        val testEnglishButton = Button(this).apply {
            text = "🔊 Test English"
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setTextColor(Color.WHITE)
            
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(ContextCompat.getColor(this@SettingsActivity, R.color.pronunciation_blue))
            }
            background = buttonBg
            elevation = 6f
            
            setOnClickListener {
                soundManager.speakEnglish("Hello")
                Toast.makeText(this@SettingsActivity, "Testing English pronunciation", Toast.LENGTH_SHORT).show()
            }
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 16, 0)
            this.layoutParams = layoutParams
        }
        
        // Test Kikuyu pronunciation
        val testKikuyuButton = Button(this).apply {
            text = "🔊 Test Kikuyu"
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setTextColor(Color.WHITE)
            
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(ContextCompat.getColor(this@SettingsActivity, R.color.category_teal))
            }
            background = buttonBg
            elevation = 6f
            
            setOnClickListener {
                soundManager.speakKikuyu("Wĩ mwega")
                Toast.makeText(this@SettingsActivity, "Testing Kikuyu pronunciation", Toast.LENGTH_SHORT).show()
            }
        }
        
        testButtonsLayout.addView(testEnglishButton)
        testButtonsLayout.addView(testKikuyuButton)
        rootLayout.addView(testButtonsLayout)
        
        // Back button
        val backButton = Button(this).apply {
            text = "🏠 Back to Home"
            textSize = 16f
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            
            val buttonBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(if (isDarkTheme) Color.parseColor("#424242") else ContextCompat.getColor(this@SettingsActivity, R.color.md_theme_light_outline))
            }
            background = buttonBg
            elevation = 4f
            
            setOnClickListener { finish() }
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 32, 0, 0)
            layoutParams.gravity = Gravity.CENTER
            this.layoutParams = layoutParams
        }
        rootLayout.addView(backButton)
        
        setContentView(rootLayout)
        
        // Handle system insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top + 48) // Add 48dp top margin
            insets
        }
    }
    
    private fun addSectionHeader(parent: LinearLayout, title: String, textColor: Int) {
        val headerText = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(textColor)
            setPadding(0, 24, 0, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        parent.addView(headerText)
    }
    
    private fun createThemeSelector(textColor: Int, isDarkTheme: Boolean): LinearLayout {
        val containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            
            // Create background
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
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "App Theme"
            textSize = 16f
            setTextColor(textColor)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        containerLayout.addView(titleText)
        
        // Description
        val descriptionText = TextView(this).apply {
            text = "Choose your preferred app appearance"
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#AAAAAA") else Color.parseColor("#666666"))
            setPadding(0, 0, 0, 16)
        }
        containerLayout.addView(descriptionText)
        
        // Radio button group
        val currentTheme = ThemeManager.getCurrentTheme(this)
        
        ThemeManager.ThemeMode.values().forEach { themeMode ->
            val radioRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            
            val radioButton = RadioButton(this).apply {
                isChecked = currentTheme == themeMode
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Uncheck all other radio buttons in the group
                        for (i in 0 until containerLayout.childCount) {
                            val child = containerLayout.getChildAt(i)
                            if (child is LinearLayout) {
                                val radio = child.getChildAt(0)
                                if (radio is RadioButton && radio != this) {
                                    radio.isChecked = false
                                }
                            }
                        }
                        
                        ThemeManager.setTheme(this@SettingsActivity, themeMode)
                        Toast.makeText(this@SettingsActivity, 
                            "Theme changed to ${themeMode.displayName}", 
                            Toast.LENGTH_SHORT).show()
                        
                        // Recreate activity to apply theme immediately
                        recreate()
                    }
                }
            }
            
            val labelText = TextView(this).apply {
                text = themeMode.displayName
                textSize = 16f
                setTextColor(textColor)
                setPadding(16, 0, 0, 0)
            }
            
            radioRow.addView(radioButton)
            radioRow.addView(labelText)
            containerLayout.addView(radioRow)
        }
        
        return containerLayout
    }
    
    private fun createToggleRow(
        title: String,
        description: String,
        prefKey: String,
        defaultValue: Boolean,
        textColor: Int,
        isDarkTheme: Boolean,
        requiresRestart: Boolean = false
    ): LinearLayout {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER_VERTICAL
            
            // Create background
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
        }
        
        // Text container
        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            this.layoutParams = layoutParams
        }
        
        // Title text
        val titleText = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textColor)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Description text
        val descriptionText = TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(if (isDarkTheme) Color.parseColor("#AAAAAA") else Color.parseColor("#666666"))
            setPadding(0, 4, 0, 0)
        }
        
        textContainer.addView(titleText)
        textContainer.addView(descriptionText)
        
        // Toggle switch
        val toggleSwitch = Switch(this).apply {
            isChecked = sharedPreferences.getBoolean(prefKey, defaultValue)
            
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean(prefKey, isChecked).apply()
                
                Toast.makeText(this@SettingsActivity, 
                    if (isChecked) "$title enabled" else "$title disabled", 
                    Toast.LENGTH_SHORT).show()
            }
        }
        
        rowLayout.addView(textContainer)
        rowLayout.addView(toggleSwitch)
        
        return rowLayout
    }
}