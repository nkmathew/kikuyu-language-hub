package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    
    private const val PREFS_NAME = "KikuyuFlashCardsSettings"
    private const val PREF_THEME_MODE = "theme_mode"
    
    enum class ThemeMode(val value: String, val displayName: String) {
        LIGHT("light", "Light"),
        DARK("dark", "Dark"),
        FOLLOW_SYSTEM("follow_system", "Follow System")
    }
    
    fun setTheme(context: Context, themeMode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_THEME_MODE, themeMode.value).apply()
        
        applyTheme(themeMode)
    }
    
    fun getCurrentTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(PREF_THEME_MODE, ThemeMode.FOLLOW_SYSTEM.value)
        return ThemeMode.values().find { it.value == savedTheme } ?: ThemeMode.FOLLOW_SYSTEM
    }
    
    fun applyTheme(themeMode: ThemeMode) {
        when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun isDarkTheme(context: Context): Boolean {
        val currentTheme = getCurrentTheme(context)
        return when (currentTheme) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.FOLLOW_SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    fun initializeTheme(context: Context) {
        val currentTheme = getCurrentTheme(context)
        applyTheme(currentTheme)
    }
}