package com.nkmathew.kikuyuflashcards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkmathew.kikuyuflashcards.FlashCardManagerV2
import com.nkmathew.kikuyuflashcards.models.Categories
import com.nkmathew.kikuyuflashcards.models.DifficultyLevels
import com.nkmathew.kikuyuflashcards.ui.components.EnhancedFlashCard

/**
 * Screen that displays enhanced flashcards with filtering options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFlashCardScreen(
    flashCardManager: FlashCardManagerV2,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State
    var isFlipped by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(flashCardManager.getCurrentIndex()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }

    // Get available categories and difficulties
    val availableCategories = remember { flashCardManager.getAvailableCategories() }
    val availableDifficulties = remember { flashCardManager.getAvailableDifficulties() }

    // Compute current entry
    val currentEntry = remember(currentIndex, selectedCategory, selectedDifficulty) {
        flashCardManager.getCurrentEntry()
    }

    // Update index in FlashCardManager when it changes
    LaunchedEffect(currentIndex) {
        flashCardManager.setCurrentIndex(currentIndex)
    }

    // Apply filters when they change
    LaunchedEffect(selectedCategory, selectedDifficulty) {
        if (selectedCategory != null) {
            flashCardManager.setCategory(selectedCategory)
        }
        if (selectedDifficulty != null) {
            flashCardManager.setDifficulty(selectedDifficulty)
        }
        // Reset currentIndex to keep it within bounds after filtering
        currentIndex = flashCardManager.getCurrentIndex()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enhanced Flashcards") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Category filter
                    Text(
                        text = "Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All") },
                                leadingIcon = {
                                    if (selectedCategory == null) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }

                        items(availableCategories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(Categories.getCategoryDisplayName(category)) },
                                leadingIcon = {
                                    if (selectedCategory == category) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Difficulty filter
                    Text(
                        text = "Difficulty",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDifficulty == null,
                                onClick = { selectedDifficulty = null },
                                label = { Text("All") },
                                leadingIcon = {
                                    if (selectedDifficulty == null) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }

                        items(availableDifficulties) { difficulty ->
                            FilterChip(
                                selected = selectedDifficulty == difficulty,
                                onClick = { selectedDifficulty = difficulty },
                                label = {
                                    Text(DifficultyLevels.getDifficultyDisplayName(difficulty))
                                },
                                leadingIcon = {
                                    if (selectedDifficulty == difficulty) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Display counter
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Card ${currentIndex + 1} of ${flashCardManager.getTotalEntries()}",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Flashcard
            if (currentEntry != null) {
                EnhancedFlashCard(
                    entry = currentEntry,
                    isFlipped = isFlipped,
                    onFlip = { isFlipped = !isFlipped },
                    onNext = {
                        flashCardManager.getNextEntry()
                        currentIndex = flashCardManager.getCurrentIndex()
                        isFlipped = false
                    },
                    onPrevious = {
                        flashCardManager.getPreviousEntry()
                        currentIndex = flashCardManager.getCurrentIndex()
                        isFlipped = false
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No cards found for the selected filters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = {
                            selectedCategory = null
                            selectedDifficulty = null
                        }) {
                            Text("Clear Filters")
                        }
                    }
                }
            }

            // Bottom actions
            BottomAppBar(
                actions = {
                    IconButton(onClick = {
                        isFlipped = !isFlipped
                    }) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Flip Card"
                        )
                    }

                    IconButton(onClick = {
                        val randomEntry = flashCardManager.getRandomEntry()
                        currentIndex = flashCardManager.getCurrentIndex()
                        isFlipped = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Random Card"
                        )
                    }

                    IconButton(onClick = {
                        selectedCategory = null
                        selectedDifficulty = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Clear Filters"
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            // Show continue where left off dialog
                            flashCardManager.startSession()
                        }
                    ) {
                        Icon(Icons.Filled.Bookmark, "Save Position")
                    }
                }
            )
        }
    }
}