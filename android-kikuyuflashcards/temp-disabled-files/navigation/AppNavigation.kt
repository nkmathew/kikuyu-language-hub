package com.nkmathew.kikuyuflashcards.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nkmathew.kikuyuflashcards.data.PhraseCategory
import com.nkmathew.kikuyuflashcards.ui.screens.*
import com.nkmathew.kikuyuflashcards.ui.viewmodel.FlashCardViewModel
// import com.nkmathew.kikuyuflashcards.ui.viewmodel.QuizViewModel
// import com.nkmathew.kikuyuflashcards.ui.viewmodel.WordScrambleViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onModeSelected = { mode ->
                    navController.navigate(mode)
                },
                onCategorySelected = { category ->
                    navController.navigate("study/${category.name}")
                }
            )
        }
        
        composable("study") {
            val context = LocalContext.current
            val viewModel: FlashCardViewModel = viewModel { FlashCardViewModel(context) }
            
            val currentPhrase by viewModel.currentPhrase
            val currentIndex by viewModel.currentIndex
            val totalPhrases by viewModel.totalPhrases
            val isLoading by viewModel.isLoading
            
            if (!isLoading) {
                FlashCardScreen(
                    currentPhrase = currentPhrase,
                    currentIndex = currentIndex,
                    totalPhrases = totalPhrases,
                    onNext = viewModel::navigateToNext,
                    onPrevious = viewModel::navigateToPrevious,
                    onBackToHome = {
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            }
        }
        
        composable("study/{category}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category") ?: "GREETINGS"
            val category = PhraseCategory.fromString(categoryName)
            
            val context = LocalContext.current
            val viewModel: FlashCardViewModel = viewModel { FlashCardViewModel(context, category) }
            
            val currentPhrase by viewModel.currentPhrase
            val currentIndex by viewModel.currentIndex
            val totalPhrases by viewModel.totalPhrases
            val isLoading by viewModel.isLoading
            
            if (!isLoading) {
                FlashCardScreen(
                    currentPhrase = currentPhrase,
                    currentIndex = currentIndex,
                    totalPhrases = totalPhrases,
                    onNext = viewModel::navigateToNext,
                    onPrevious = viewModel::navigateToPrevious,
                    onBackToHome = {
                        navController.popBackStack("home", inclusive = false)
                    },
                    categoryFilter = category
                )
            }
        }
        
        composable("quiz") {
            // Placeholder for Quiz Mode - coming soon
            HomeScreen(
                onModeSelected = { mode ->
                    navController.navigate(mode)
                },
                onCategorySelected = { category ->
                    navController.navigate("study/${category.name}")
                }
            )
        }
        
        composable("scramble") {
            // Placeholder for Word Scramble - coming soon
            HomeScreen(
                onModeSelected = { mode ->
                    navController.navigate(mode)
                },
                onCategorySelected = { category ->
                    navController.navigate("study/${category.name}")
                }
            )
        }
        
        composable("speed") {
            // TODO: Implement Speed Round screen
            HomeScreen(
                onModeSelected = { mode ->
                    navController.navigate(mode)
                },
                onCategorySelected = { category ->
                    navController.navigate("study/${category.name}")
                }
            )
        }
        
        composable("memory") {
            // TODO: Implement Memory Game screen
            HomeScreen(
                onModeSelected = { mode ->
                    navController.navigate(mode)
                },
                onCategorySelected = { category ->
                    navController.navigate("study/${category.name}")
                }
            )
        }
    }
}