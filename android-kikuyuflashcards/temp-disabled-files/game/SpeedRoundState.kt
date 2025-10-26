package com.nkmathew.kikuyuflashcards.ui.game

import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity

data class SpeedRoundState(
    val currentPhrase: PhraseEntity? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 20,
    val score: Int = 0,
    val timeRemaining: Int = 90, // 90 seconds total
    val timePerQuestion: Int = 5, // 5 seconds per question
    val isGameActive: Boolean = false,
    val isGameComplete: Boolean = false,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val speedBonus: Int = 0, // Bonus points for quick answers
    val questionStartTime: Long = 0L
)

data class SpeedRoundQuestion(
    val phrase: PhraseEntity,
    val showEnglish: Boolean, // true = show English, answer Kikuyu; false = show Kikuyu, answer English
    val answerTime: Long = 0L // Time taken to answer (for bonus calculation)
)

data class SpeedRoundResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val totalTime: Int,
    val averageTimePerQuestion: Float,
    val maxStreak: Int,
    val speedBonus: Int,
    val accuracy: Float = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
)