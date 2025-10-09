package com.nkmathew.kikuyuflashcards.ui.game

import com.nkmathew.kikuyuflashcards.data.entity.PhraseEntity

data class QuizGameState(
    val currentQuestion: QuizQuestion? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val score: Int = 0,
    val timeRemaining: Int = 30, // seconds
    val isGameActive: Boolean = false,
    val isGameComplete: Boolean = false,
    val selectedAnswerIndex: Int? = null,
    val showCorrectAnswer: Boolean = false,
    val streak: Int = 0,
    val bestStreak: Int = 0
)

data class QuizQuestion(
    val phrase: PhraseEntity,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val questionType: QuizQuestionType
)

enum class QuizQuestionType {
    ENGLISH_TO_KIKUYU,  // Show English, select Kikuyu
    KIKUYU_TO_ENGLISH,  // Show Kikuyu, select English
    AUDIO_TO_TEXT       // Future: Play audio, select text
}

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val timeSpent: Int,
    val streak: Int,
    val accuracy: Float = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
)