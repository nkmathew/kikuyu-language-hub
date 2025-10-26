package com.nkmathew.kikuyuflashcards.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkmathew.kikuyuflashcards.Phrase
import com.nkmathew.kikuyuflashcards.ui.components.FlashCard
import com.nkmathew.kikuyuflashcards.ui.components.NavigationButtons
import com.nkmathew.kikuyuflashcards.ui.components.ProgressIndicator
import com.nkmathew.kikuyuflashcards.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashCardScreen(
    currentPhrase: Phrase?,
    currentIndex: Int,
    totalPhrases: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onBackToHome: (() -> Unit)? = null,
    categoryFilter: com.nkmathew.kikuyuflashcards.data.PhraseCategory? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    
    LaunchedEffect(currentPhrase) {
        // Reset offset when phrase changes
        offsetX.snapTo(0f)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (onBackToHome != null) {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Text(
                            text = categoryFilter?.displayName ?: "Study Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = onBackToHome) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to Home"
                            )
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val threshold = 100f
                            when {
                                offsetX.value > threshold -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPrevious()
                                }
                                offsetX.value < -threshold -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNext()
                                }
                            }
                            // Reset offset (no need for coroutines in drag gesture)
                            // offsetX will be reset by LaunchedEffect when phrase changes
                        }
                    ) { _, dragAmount ->
                        // Update offset during drag (simplified for now)
                        // Full drag implementation can be added later
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Progress Indicator
                ProgressIndicator(
                    currentIndex = currentIndex,
                    totalCount = totalPhrases
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Flash Cards
                if (currentPhrase != null) {
                    // English Card
                    FlashCard(
                        text = currentPhrase.english,
                        isEnglish = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Kikuyu Card
                    FlashCard(
                        text = currentPhrase.kikuyu,
                        isEnglish = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Loading or error state
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading phrases...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation Buttons
                NavigationButtons(
                    onPreviousClick = onPrevious,
                    onNextClick = onNext,
                    canGoBack = currentIndex > 0,
                    canGoForward = currentIndex < totalPhrases - 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Instructions
                Text(
                    text = "Swipe left/right to navigate • Tap buttons for navigation",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}