package com.nkmathew.kikuyuflashcards

import android.app.Application

class KikuyuFlashCardsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme based on saved preference
        ThemeManager.initializeTheme(this)
    }
}