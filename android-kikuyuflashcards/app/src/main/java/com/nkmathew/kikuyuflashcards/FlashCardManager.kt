package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.models.SourceInfo

class FlashCardManager(private val context: Context) {
    companion object {
        private const val TAG = "FlashCardManager"
    }

    private val allPhrases: MutableList<FlashcardEntry> = mutableListOf()
    private val filteredPhrases: MutableList<FlashcardEntry> = mutableListOf()
    private var currentIndex = 0
    private val random = Random.Default
    private var isShuffleMode = false
    private var currentCategory: String? = null
    private val positionManager = PositionManager(context)

    init {
        try {
            Log.d(TAG, "FlashCardManager: Initializing")
            allPhrases.addAll(loadPhrasesFromJSON())
            filteredPhrases.addAll(allPhrases)
            Log.d(TAG, "FlashCardManager: Loaded ${allPhrases.size} phrases")
        } catch (e: Exception) {
            Log.e(TAG, "FlashCardManager: Error during initialization", e)
        }
    }

    fun getCurrentPhrase(): FlashcardEntry? {
        return if (filteredPhrases.isEmpty()) null else filteredPhrases[currentIndex]
    }

    fun getNextPhrase(): FlashcardEntry? {
        if (filteredPhrases.isEmpty()) return null
        currentIndex = (currentIndex + 1) % filteredPhrases.size
        saveCurrentPosition()
        return getCurrentPhrase()
    }

    fun getPreviousPhrase(): FlashcardEntry? {
        if (filteredPhrases.isEmpty()) return null
        currentIndex = (currentIndex - 1 + filteredPhrases.size) % filteredPhrases.size
        saveCurrentPosition()
        return getCurrentPhrase()
    }

    fun getRandomPhrase(): FlashcardEntry? {
        if (filteredPhrases.isEmpty()) return null
        currentIndex = random.nextInt(filteredPhrases.size)
        return getCurrentPhrase()
    }

    fun setCurrentIndex(index: Int, savePosition: Boolean = true): Boolean {
        return if (index in 0 until filteredPhrases.size) {
            currentIndex = index
            Log.d(TAG, "Set current index to: $index")
            
            // Save position automatically
            if (savePosition) {
                saveCurrentPosition()
            }
            true
        } else {
            Log.w(TAG, "Invalid index: $index, valid range: 0-${filteredPhrases.size - 1}")
            false
        }
    }

    fun getCurrentIndex(): Int = currentIndex

    fun getTotalPhrases(): Int = filteredPhrases.size
    
    fun getTotalPhrasesInCategory(category: String): Int {
        return allPhrases.count { it.category == category }
    }
    
    fun getAvailableCategories(): List<String> {
        return allPhrases.map { it.category }.distinct().sorted()
    }
    
    fun setCategory(category: String?): Boolean {
        return try {
            val previousCategory = currentCategory
            currentCategory = category
            filteredPhrases.clear()
            
            if (category == null) {
                filteredPhrases.addAll(allPhrases)
            } else {
                val categoryPhrases = allPhrases.filter { it.category == category }
                if (categoryPhrases.isEmpty()) {
                    Log.w(TAG, "No phrases found for category: $category")
                    // Restore previous state
                    currentCategory = previousCategory
                    if (previousCategory == null) {
                        filteredPhrases.addAll(allPhrases)
                    } else {
                        filteredPhrases.addAll(allPhrases.filter { it.category == previousCategory })
                    }
                    return false
                }
                filteredPhrases.addAll(categoryPhrases)
            }
            
            // Restore last position for this category if available
            restoreLastPosition()
            Log.d(TAG, "Set category to: $category, phrases available: ${filteredPhrases.size}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting category: ${e.message}", e)
            false
        }
    }
    
    fun getCurrentCategory(): String? = currentCategory

    fun setShuffleMode(shuffle: Boolean) {
        isShuffleMode = shuffle
    }

    fun isShuffleMode(): Boolean = isShuffleMode

    private fun loadPhrasesFromJSON(): List<FlashcardEntry> {
        val phrases = mutableListOf<FlashcardEntry>()
        
        try {
            context.assets.open("kikuyu-phrases.json").use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                
                val jsonObject = JSONObject(json)
                val jsonArray = jsonObject.getJSONArray("phrases")
                
                // Pre-allocate list size for better performance
                val phrasesList = ArrayList<FlashcardEntry>(jsonArray.length())
                
                for (i in 0 until jsonArray.length()) {
                    val phraseObject = jsonArray.getJSONObject(i)
                    val english = phraseObject.getString("english")
                    val kikuyu = phraseObject.getString("kikuyu")
                    val category = phraseObject.optString("category", "general")
                    
                    phrasesList.add(FlashcardEntry(
                        id = "${english.hashCode()}",
                        english = english,
                        kikuyu = kikuyu,
                        category = category,
                        difficulty = "medium",
                        source = SourceInfo(origin = "Legacy JSON")
                    ))
                }
                
                phrases.addAll(phrasesList)
                Log.d(TAG, "Successfully loaded ${phrases.size} phrases from JSON")
            }
        } catch (e: IOException) {
            Log.e(TAG, "IO Error loading JSON file: ${e.message}", e)
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading phrases: ${e.message}", e)
        }
        
        return phrases
    }
    
    // Position Management Methods
    
    /**
     * Save the current position to persistent storage
     */
    private fun saveCurrentPosition() {
        positionManager.savePosition(currentIndex, currentCategory, filteredPhrases.size)
    }
    
    /**
     * Restore the last saved position for the current category
     */
    private fun restoreLastPosition() {
        if (positionManager.shouldRestorePosition()) {
            val lastPosition = positionManager.getLastPosition(currentCategory)
            if (lastPosition < filteredPhrases.size) {
                currentIndex = lastPosition
                Log.d(TAG, "Restored position: $lastPosition for category: $currentCategory")
            }
        }
    }
    
    /**
     * Get position manager for external access
     */
    fun getPositionManager(): PositionManager = positionManager
    
    /**
     * Get a user-friendly message for continuing learning
     */
    fun getContinueLearningMessage(): String? {
        return positionManager.getContinueLearningMessage(this)
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
     * Get all phrases for generating multiple choice options
     */
    fun getAllPhrases(): List<FlashcardEntry> {
        return if (currentCategory == null) {
            allPhrases
        } else {
            allPhrases.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }
    }
}