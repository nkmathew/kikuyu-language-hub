package com.nkmathew.kikuyuflashcards

data class Phrase(
    val english: String,
    val kikuyu: String,
    val category: String = "general"
) {
    // For backward compatibility with adapter
    val text: String get() = english
    
    companion object {
        // Category constants
        const val GREETINGS = "greetings"
        const val EMOTIONS = "emotions"
        const val BASIC_WORDS = "basic_words"
        const val VERBS = "verbs"
        const val NOUNS = "nouns"
        const val QUESTIONS = "questions"
        const val TIME = "time"
        const val GENERAL = "general"
        
        // Category display names
        fun getCategoryDisplayName(category: String): String {
            return when (category) {
                GREETINGS -> "👋 Greetings"
                EMOTIONS -> "❤️ Emotions & Feelings"
                BASIC_WORDS -> "🔤 Basic Words"
                VERBS -> "⚡ Action Verbs"
                NOUNS -> "📦 Nouns & Objects"
                QUESTIONS -> "❓ Questions"
                TIME -> "⏰ Time & Dates"
                else -> "📚 General"
            }
        }
    }
}