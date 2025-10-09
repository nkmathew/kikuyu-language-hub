package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import kotlin.random.Random

/**
 * Enhanced FlashCardManager that uses the new CuratedContentManager
 * to load and manage content from curated sources
 */
class FlashCardManagerV2(private val context: Context) {
    companion object {
        private const val TAG = "FlashCardManagerV2"
    }

    // Core managers
    private val curatedContentManager = CuratedContentManager(context)
    private val positionManager = PositionManagerV2(context)

    // Data state
    private val allEntries = mutableListOf<FlashcardEntry>()
    private val filteredEntries = mutableListOf<FlashcardEntry>()
    private val random = Random.Default

    // Filtering state
    private var currentIndex = 0
    private var currentCategory: String? = null
    private var currentDifficulty: String? = null
    private var isShuffleMode = false

    init {
        try {
            Log.d(TAG, "FlashCardManagerV2: Initializing")

            // Load all entries from the CuratedContentManager
            allEntries.addAll(curatedContentManager.allEntries)

            // Initialize filtered collections with all content
            filteredEntries.addAll(allEntries)

            Log.d(TAG, "FlashCardManagerV2: Loaded ${allEntries.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "FlashCardManagerV2: Error during initialization", e)
        }
    }

    // Navigation methods

    /**
     * Get the current entry being viewed
     */
    fun getCurrentEntry(): FlashcardEntry? {
        return if (filteredEntries.isEmpty()) null else filteredEntries[currentIndex]
    }


    /**
     * Move to and return the next entry
     */
    fun getNextEntry(): FlashcardEntry? {
        if (filteredEntries.isEmpty()) return null
        currentIndex = (currentIndex + 1) % filteredEntries.size
        saveCurrentPosition()
        return getCurrentEntry()
    }


    /**
     * Move to and return the previous entry
     */
    fun getPreviousEntry(): FlashcardEntry? {
        if (filteredEntries.isEmpty()) return null
        currentIndex = (currentIndex - 1 + filteredEntries.size) % filteredEntries.size
        saveCurrentPosition()
        return getCurrentEntry()
    }


    /**
     * Move to and return a random entry
     */
    fun getRandomEntry(): FlashcardEntry? {
        if (filteredEntries.isEmpty()) return null
        currentIndex = random.nextInt(filteredEntries.size)
        return getCurrentEntry()
    }


    // Position management

    /**
     * Set the current index to a specific position
     */
    fun setCurrentIndex(index: Int, savePosition: Boolean = true): Boolean {
        return if (index in 0 until filteredEntries.size) {
            currentIndex = index
            Log.d(TAG, "Set current index to: $index")

            if (savePosition) {
                saveCurrentPosition()
            }
            true
        } else {
            Log.w(TAG, "Invalid index: $index, valid range: 0-${filteredEntries.size - 1}")
            false
        }
    }

    /**
     * Get the current index position
     */
    fun getCurrentIndex(): Int = currentIndex

    /**
     * Get the total number of entries after filtering
     */
    fun getTotalEntries(): Int = filteredEntries.size

    // Category management

    /**
     * Get the count of entries in a specific category
     */
    fun getTotalEntriesInCategory(category: String): Int {
        return allEntries.count { it.category == category }
    }


    /**
     * Get all available categories from the loaded content
     */
    fun getAvailableCategories(): List<String> {
        return allEntries.map { it.category }.distinct().sorted()
    }

    /**
     * Get all available difficulty levels from the loaded content
     */
    fun getAvailableDifficulties(): List<String> {
        return allEntries.map { it.difficulty }.distinct().sorted()
    }

    // Filtering methods

    /**
     * Set the current category filter
     */
    fun setCategory(category: String?): Boolean {
        return try {
            val previousCategory = currentCategory
            currentCategory = category

            applyFilters()

            // If the filtered list is empty, restore previous state
            if (filteredEntries.isEmpty()) {
                Log.w(TAG, "No entries found for category: $category")
                currentCategory = previousCategory
                applyFilters()
                return false
            }

            // Restore last position for this category if available
            restoreLastPosition()
            Log.d(TAG, "Set category to: $category, entries available: ${filteredEntries.size}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting category: ${e.message}", e)
            false
        }
    }

    /**
     * Set the current difficulty filter
     */
    fun setDifficulty(difficulty: String?): Boolean {
        return try {
            val previousDifficulty = currentDifficulty
            currentDifficulty = difficulty

            applyFilters()

            // If the filtered list is empty, restore previous state
            if (filteredEntries.isEmpty()) {
                Log.w(TAG, "No entries found for difficulty: $difficulty")
                currentDifficulty = previousDifficulty
                applyFilters()
                return false
            }

            // Restore last position for this combination if available
            restoreLastPosition()
            Log.d(TAG, "Set difficulty to: $difficulty, entries available: ${filteredEntries.size}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting difficulty: ${e.message}", e)
            false
        }
    }

    /**
     * Apply current category and difficulty filters to the entries list
     */
    private fun applyFilters() {
        // Clear filtered collections
        filteredEntries.clear()

        // Create a copy of all entries to work with
        val baseEntries = allEntries.toList()

        // Apply filters
        val filtered = baseEntries.filter { entry ->
            // Apply category filter if set
            val categoryMatch = currentCategory?.let { entry.category == it } ?: true

            // Apply difficulty filter if set
            val difficultyMatch = currentDifficulty?.let { entry.difficulty == it } ?: true

            // Entry must match both filters to be included
            categoryMatch && difficultyMatch
        }

        // Update filtered collections
        filteredEntries.addAll(filtered)

        // Reset index if needed
        if (currentIndex >= filteredEntries.size) {
            currentIndex = 0
        }
    }

    /**
     * Get the current category filter
     */
    fun getCurrentCategory(): String? = currentCategory

    /**
     * Get the current difficulty filter
     */
    fun getCurrentDifficulty(): String? = currentDifficulty

    /**
     * Toggle or set shuffle mode
     */
    fun setShuffleMode(shuffle: Boolean) {
        isShuffleMode = shuffle
    }

    /**
     * Check if shuffle mode is enabled
     */
    fun isShuffleMode(): Boolean = isShuffleMode

    // Position Management Methods

    /**
     * Save the current position to persistent storage
     */
    private fun saveCurrentPosition() {
        // Include both category and difficulty in the position key
        val key = buildPositionKey()
        positionManager.savePositionWithKey(currentIndex, key, filteredEntries.size)
    }

    /**
     * Build a position key that includes category and difficulty
     */
    private fun buildPositionKey(): String {
        val categoryPart = currentCategory ?: "all"
        val difficultyPart = currentDifficulty ?: "all"
        return "$categoryPart:$difficultyPart"
    }

    /**
     * Restore the last saved position for the current category/difficulty combo
     */
    private fun restoreLastPosition() {
        if (positionManager.shouldRestorePosition()) {
            val key = buildPositionKey()
            val lastPosition = positionManager.getLastPositionWithKey(key)
            if (lastPosition < filteredEntries.size) {
                currentIndex = lastPosition
                Log.d(TAG, "Restored position: $lastPosition for key: $key")
            }
        }
    }

    /**
     * Get position manager for external access
     */
    fun getPositionManager(): PositionManagerV2 = positionManager

    /**
     * Get a user-friendly message for continuing learning
     */
    fun getContinueLearningMessage(): String? {
        // Create custom message that includes category and difficulty info
        val key = buildPositionKey()
        val lastPosition = positionManager.getLastPositionWithKey(key)
        val totalCount = positionManager.getTotalCountWithKey(key)

        if (lastPosition < 0 || totalCount <= 0) {
            return null
        }

        val categoryDisplay = currentCategory?.let { getCategoryDisplayName(it) } ?: "All Categories"
        val difficultyDisplay = currentDifficulty?.let { getDifficultyDisplayName(it) } ?: "All Levels"

        return "Continue learning $categoryDisplay ($difficultyDisplay) from card ${lastPosition + 1}/$totalCount"
    }

    /**
     * Start a new learning session
     */
    fun startSession() {
        positionManager.startSession()
    }

    /**
     * Check if we should show the "continue where you left off" option
     */
    fun shouldShowContinueOption(): Boolean {
        return positionManager.shouldRestorePosition()
    }

    /**
     * Get all entries for generating options, respecting current filters
     */
    fun getAllEntries(): List<FlashcardEntry> {
        // Apply current category filter if set
        val categoryFiltered = if (currentCategory != null) {
            allEntries.filter { it.category == currentCategory }
        } else {
            allEntries
        }

        // Apply current difficulty filter if set
        return if (currentDifficulty != null) {
            categoryFiltered.filter { it.difficulty == currentDifficulty }
        } else {
            categoryFiltered
        }
    }


    // Helper methods for display

    /**
     * Get a display-friendly name for a category
     */
    fun getCategoryDisplayName(category: String): String {
        // Use the category display name from the new schema
        return com.nkmathew.kikuyuflashcards.models.Categories.getCategoryDisplayName(category)
    }

    /**
     * Get a display-friendly name for a difficulty level
     */
    fun getDifficultyDisplayName(difficulty: String): String {
        return com.nkmathew.kikuyuflashcards.models.DifficultyLevels.getDifficultyDisplayName(difficulty)
    }

    /**
     * Get a direct reference to the CuratedContentManager
     */
    fun getCuratedContentManager(): CuratedContentManager {
        return curatedContentManager
    }
}