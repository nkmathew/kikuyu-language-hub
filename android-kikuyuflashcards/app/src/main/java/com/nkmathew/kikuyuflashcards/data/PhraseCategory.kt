package com.nkmathew.kikuyuflashcards.data

enum class PhraseCategory(val displayName: String, val description: String) {
    GREETINGS("Greetings", "Common greetings and pleasantries"),
    FAMILY("Family", "Family members and relationships"),
    FOOD("Food", "Food items, cooking, and dining"),
    NUMBERS("Numbers", "Numbers, counting, and quantities"),
    TIME("Time", "Time expressions, days, months"),
    WEATHER("Weather", "Weather conditions and seasons"),
    EMOTIONS("Emotions", "Feelings and emotional expressions"),
    TRANSPORTATION("Transportation", "Travel, vehicles, and directions"),
    BUSINESS("Business", "Work, commerce, and professional terms"),
    MEDICAL("Medical", "Health, body parts, and medical terms"),
    EDUCATION("Education", "School, learning, and academic terms"),
    RELIGION("Religion", "Spiritual and religious expressions");

    companion object {
        fun fromString(category: String): PhraseCategory {
            return values().find { it.name.equals(category, ignoreCase = true) } ?: GREETINGS
        }
    }
}