package com.nkmathew.kikuyuflashcards.ui.game

import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity

data class WordScrambleState(
    val currentPhrase: PhraseEntity? = null,
    val scrambledWord: String = "",
    val userInput: String = "",
    val currentWordIndex: Int = 0,
    val totalWords: Int = 10,
    val score: Int = 0,
    val timeRemaining: Int = 60, // seconds
    val isGameActive: Boolean = false,
    val isGameComplete: Boolean = false,
    val showHint: Boolean = false,
    val streak: Int = 0,
    val hintsUsed: Int = 0,
    val maxHints: Int = 3
)

data class ScrambledWord(
    val original: String,
    val scrambled: String,
    val hint: String
)

data class WordScrambleResult(
    val totalWords: Int,
    val correctWords: Int,
    val score: Int,
    val timeSpent: Int,
    val hintsUsed: Int,
    val streak: Int,
    val accuracy: Float = if (totalWords > 0) correctWords.toFloat() / totalWords else 0f
)