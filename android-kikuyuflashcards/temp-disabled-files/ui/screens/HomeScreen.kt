package com.nkmathew.kikuyuflashcards.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nkmathew.kikuyuflashcards.data.PhraseCategory
import com.nkmathew.kikuyuflashcards.ui.theme.*

data class LearningMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: androidx.compose.ui.graphics.Color,
    val isComingSoon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onModeSelected: (String) -> Unit,
    onCategorySelected: (PhraseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    val learningModes = listOf(
        LearningMode(
            title = "Study Mode",
            description = "Browse flashcards at your own pace",
            icon = Icons.Default.School,
            route = "study",
            color = GradientStart
        ),
        LearningMode(
            title = "Quiz Mode",
            description = "Test your knowledge with multiple choice",
            icon = Icons.Default.Quiz,
            route = "quiz",
            color = EnglishCardPrimary
        ),
        LearningMode(
            title = "Word Scramble",
            description = "Unscramble Kikuyu words",
            icon = Icons.Default.Shuffle,
            route = "scramble",
            color = KikuyuCardPrimary
        ),
        LearningMode(
            title = "Speed Round",
            description = "Quick-fire translation challenges",
            icon = Icons.Default.Timer,
            route = "speed",
            color = GradientEnd
        ),
        LearningMode(
            title = "Memory Game",
            description = "Match English and Kikuyu pairs",
            icon = Icons.Default.Games,
            route = "memory",
            color = MaterialTheme.colorScheme.tertiary,
            isComingSoon = true
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kikuyu Flash Cards",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.05f),
                            GradientEnd.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome section
            item {
                WelcomeCard()
            }
            
            // Learning modes section
            item {
                Text(
                    text = "Learning Modes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(learningModes) { mode ->
                LearningModeCard(
                    mode = mode,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!mode.isComingSoon) {
                            onModeSelected(mode.route)
                        }
                    }
                )
            }
            
            // Categories section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Browse by Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                CategoriesGrid(
                    onCategorySelected = { category ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Kikuyu Learning!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Choose a learning mode below to start your journey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LearningModeCard(
    mode: LearningMode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(mode.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mode.icon,
                    contentDescription = mode.title,
                    tint = mode.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (mode.isComingSoon) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Soon",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go to ${mode.title}",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CategoriesGrid(
    onCategorySelected: (PhraseCategory) -> Unit
) {
    val categories = listOf(
        PhraseCategory.GREETINGS to "👋",
        PhraseCategory.FAMILY to "👨‍👩‍👧‍👦",
        PhraseCategory.FOOD to "🍽️",
        PhraseCategory.NUMBERS to "🔢",
        PhraseCategory.TIME to "⏰",
        PhraseCategory.WEATHER to "🌤️",
        PhraseCategory.EMOTIONS to "😊",
        PhraseCategory.TRANSPORTATION to "🚗",
        PhraseCategory.BUSINESS to "💼",
        PhraseCategory.MEDICAL to "🏥",
        PhraseCategory.EDUCATION to "📚",
        PhraseCategory.RELIGION to "⛪"
    )
    
    // Split into rows of 3
    val chunkedCategories = categories.chunked(3)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCategories.forEach { (category, emoji) ->
                    CategoryCard(
                        category = category,
                        emoji = emoji,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if row is not complete
                repeat(3 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: PhraseCategory,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}