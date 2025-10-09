package com.nkmathew.kikuyuflashcards.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkmathew.kikuyuflashcards.ui.game.QuizGameState
import com.nkmathew.kikuyuflashcards.ui.game.QuizQuestion
import com.nkmathew.kikuyuflashcards.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    gameState: QuizGameState,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (gameState.isGameActive) {
                QuizTopBar(
                    currentQuestion = gameState.currentQuestionIndex + 1,
                    totalQuestions = gameState.totalQuestions,
                    score = gameState.score,
                    timeRemaining = gameState.timeRemaining,
                    streak = gameState.streak
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.1f),
                            GradientEnd.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                !gameState.isGameActive && !gameState.isGameComplete -> {
                    QuizStartScreen(
                        onStartGame = onStartGame,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                gameState.isGameComplete -> {
                    QuizResultScreen(
                        gameState = gameState,
                        onPlayAgain = onStartGame,
                        onEndGame = onEndGame,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                gameState.currentQuestion != null -> {
                    QuizQuestionContent(
                        question = gameState.currentQuestion!!,
                        selectedAnswerIndex = gameState.selectedAnswerIndex,
                        showCorrectAnswer = gameState.showCorrectAnswer,
                        onAnswerSelected = { index ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAnswerSelected(index)
                        },
                        onNextQuestion = onNextQuestion,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizTopBar(
    currentQuestion: Int,
    totalQuestions: Int,
    score: Int,
    timeRemaining: Int,
    streak: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress
            Text(
                text = "$currentQuestion/$totalQuestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Score
            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.titleMedium,
                color = KikuyuPrimary
            )
            
            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time",
                    tint = if (timeRemaining <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${timeRemaining}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (timeRemaining <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Streak
            if (streak > 1) {
                Text(
                    text = "🔥 $streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuizStartScreen(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Mode",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Test your knowledge with multiple choice questions!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KikuyuPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Start Quiz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    selectedAnswerIndex: Int?,
    showCorrectAnswer: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Answer Options
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEachIndexed { index, option ->
                AnswerButton(
                    text = option,
                    isSelected = selectedAnswerIndex == index,
                    isCorrect = showCorrectAnswer && index == question.correctAnswerIndex,
                    isWrong = showCorrectAnswer && selectedAnswerIndex == index && index != question.correctAnswerIndex,
                    enabled = selectedAnswerIndex == null,
                    onClick = { onAnswerSelected(index) }
                )
            }
        }
        
        if (showCorrectAnswer) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNextQuestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KikuyuPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next Question")
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCorrect -> MaterialTheme.colorScheme.primary
        isWrong -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isCorrect || isWrong || isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuizResultScreen(
    gameState: QuizGameState,
    onPlayAgain: () -> Unit,
    onEndGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = if (gameState.totalQuestions > 0) {
        (gameState.score.toFloat() / (gameState.totalQuestions * 100)) * 100
    } else 0f

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${gameState.score}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = KikuyuPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Accuracy", "${accuracy.toInt()}%")
                    StatItem("Best Streak", "${gameState.bestStreak}")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onEndGame,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Exit")
            }
            
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KikuyuPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Play Again")
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}