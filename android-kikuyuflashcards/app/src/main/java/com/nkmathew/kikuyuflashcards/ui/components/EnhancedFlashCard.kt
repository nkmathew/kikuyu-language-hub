package com.nkmathew.kikuyuflashcards.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry

/**
 * Enhanced flashcard component that displays additional metadata from the new schema
 */
@Composable
fun EnhancedFlashCard(
    entry: FlashcardEntry,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Card styles based on difficulty
    val cardBackground = when (entry.difficulty) {
        DifficultyLevels.BEGINNER -> Brush.verticalGradient(
            listOf(Color(0xFF43A047).copy(alpha = 0.1f), Color(0xFF43A047).copy(alpha = 0.2f))
        )
        DifficultyLevels.INTERMEDIATE -> Brush.verticalGradient(
            listOf(Color(0xFFFB8C00).copy(alpha = 0.1f), Color(0xFFFB8C00).copy(alpha = 0.2f))
        )
        DifficultyLevels.ADVANCED -> Brush.verticalGradient(
            listOf(Color(0xFFE53935).copy(alpha = 0.1f), Color(0xFFE53935).copy(alpha = 0.2f))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF2196F3).copy(alpha = 0.1f), Color(0xFF2196F3).copy(alpha = 0.2f))
        )
    }

    val qualityBadgeColor = entry.quality?.let {
        when {
            it.confidenceScore >= 4.5f -> Color(0xFF43A047) // Green
            it.confidenceScore >= 4.0f -> Color(0xFF8BC34A) // Light Green
            it.confidenceScore >= 3.5f -> Color(0xFFFFA000) // Amber
            it.confidenceScore >= 3.0f -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFF44336) // Red
        }
    } ?: Color(0xFF9E9E9E) // Gray default

    // Animation for card flip
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        label = "Content Alpha"
    )

    // Create card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onFlip() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackground)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with metadata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    CategoryBadge(category = entry.category)

                    // Difficulty Badge
                    DifficultyBadge(difficulty = entry.difficulty)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .alpha(contentAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFlipped) {
                        // Kikuyu side (large text)
                        Text(
                            text = entry.kikuyu,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // English side
                        Text(
                            text = entry.english,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Additional information (shown when flipped)
                if (isFlipped) {
                    ExtraInformation(entry = entry)
                }

                // Navigation and action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Previous"
                        )
                    }

                    IconButton(onClick = onFlip) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Flip Card"
                        )
                    }

                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next"
                        )
                    }
                }

                // Source information
                entry.source?.let { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Source: ${source.origin}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quality badge if available
                        entry.quality?.let { quality ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier
                                    .height(20.dp)
                                    .padding(horizontal = 4.dp),
                                color = qualityBadgeColor,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%.1f", quality.confidenceScore),
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    val (icon, color) = when (category) {
        Categories.VOCABULARY -> Icons.Default.MenuBook to Color(0xFF2196F3)
        Categories.PROVERBS -> Icons.Default.Lightbulb to Color(0xFF9C27B0)
        Categories.GRAMMAR -> Icons.Default.TextFormat to Color(0xFF4CAF50)
        Categories.CONJUGATIONS -> Icons.Default.Refresh to Color(0xFFF44336)
        Categories.CULTURAL -> Icons.Default.Public to Color(0xFF795548)
        Categories.NUMBERS -> Icons.Default.Numbers to Color(0xFF607D8B)
        Categories.PHRASES -> Icons.Default.Chat to Color(0xFFFF9800)
        else -> Icons.Default.Category to Color(0xFF9E9E9E)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Categories.getCategoryDisplayName(category).replace(Regex("^[^\\w]*"), ""),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: String) {
    val (icon, color) = when (difficulty) {
        DifficultyLevels.BEGINNER -> Icons.Default.SentimentSatisfied to Color(0xFF43A047)
        DifficultyLevels.INTERMEDIATE -> Icons.Default.SentimentNeutral to Color(0xFFFB8C00)
        DifficultyLevels.ADVANCED -> Icons.Default.SentimentVeryDissatisfied to Color(0xFFE53935)
        else -> Icons.Default.Help to Color(0xFF9E9E9E)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = DifficultyLevels.getDifficultyDisplayName(difficulty)
                    .replace(Regex("^[^\\w]*"), ""),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ExtraInformation(entry: FlashcardEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Context
        entry.context?.let { context ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context,
                    fontSize = 14.sp
                )
            }
        }

        // Cultural notes
        entry.culturalNotes?.let { notes ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = notes,
                    fontSize = 14.sp
                )
            }
        }

        // Grammatical info (if available and flipped)
        entry.grammaticalInfo?.let { info ->
            info.partOfSpeech?.let { pos ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TextFormat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Part of speech: $pos",
                        fontSize = 14.sp
                    )
                }
            }

            // Other grammatical info fields if relevant
            val grammaticalFields = listOfNotNull(
                info.verbClass?.let { "Verb class: $it" },
                info.nounClass?.let { "Noun class: $it" },
                info.infinitive?.let { "Infinitive: $it" },
                info.tense?.let { "Tense: $it" },
                info.subjectMarker?.let { "Subject marker: $it" }
            )

            if (grammaticalFields.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grammaticalFields.forEach { field ->
                        Text(
                            text = field,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Examples
        if (entry.examples.isNotEmpty()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Examples:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    entry.examples.take(2).forEach { example ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = example.kikuyu,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = example.english,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                example.context?.let { context ->
                                    Text(
                                        text = context,
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Show "more examples" if there are more than 2
                    if (entry.examples.size > 2) {
                        Text(
                            text = "+${entry.examples.size - 2} more examples",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}