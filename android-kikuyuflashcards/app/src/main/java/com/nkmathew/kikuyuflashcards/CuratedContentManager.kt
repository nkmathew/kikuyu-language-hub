package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nkmathew.kikuyuflashcards.models.*
import java.io.IOException

/**
 * Manages loading and accessing curated content from JSON files in assets directory
 */
class CuratedContentManager(private val context: Context) {
    companion object {
        private const val TAG = "CuratedContentManager"
        private const val CURATED_CONTENT_DIR = "curated-content"
    }

    private val gson: Gson = GsonBuilder().create()
    private val _allEntries = mutableListOf<FlashcardEntry>()
    private var _metadata = mutableMapOf<String, Metadata>()

    // Public read-only access to all loaded entries
    val allEntries: List<FlashcardEntry>
        get() = _allEntries

    // Number of entries loaded
    val entryCount: Int
        get() = _allEntries.size

    // All available categories from loaded content
    val availableCategories: List<String>
        get() = _allEntries.map { it.category }.distinct().sorted()

    // All available difficulty levels from loaded content
    val availableDifficulties: List<String>
        get() = _allEntries.map { it.difficulty }.distinct().sorted()

    /**
     * Initialize and load all content from curated files
     */
    init {
        try {
            Log.d(TAG, "Initializing CuratedContentManager")
            loadCuratedContent()
            Log.d(TAG, "Successfully loaded ${_allEntries.size} entries from curated content")
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
        }
    }

    /**
     * Load all curated content from the curated-content directory
     */
    private fun loadCuratedContent() {
        try {
            // List all files in the curated-content directory and subdirectories
            val curatedFiles = listCuratedFiles()
            if (curatedFiles.isEmpty()) {
                Log.w(TAG, "No curated content files found in $CURATED_CONTENT_DIR")
                return
            }

            Log.d(TAG, "Found ${curatedFiles.size} curated content files")

            // Load each curated file
            for (filePath in curatedFiles) {
                loadCuratedFile(filePath)
            }

        } catch (e: IOException) {
            Log.e(TAG, "Error accessing curated content directory: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading curated content: ${e.message}")
        }
    }

    /**
     * List all JSON files in the curated-content directory and subdirectories
     */
    private fun listCuratedFiles(): List<String> {
        val result = mutableListOf<String>()

        try {
            // Get list of all files in the curated-content directory
            val files = context.assets.list(CURATED_CONTENT_DIR) ?: emptyArray()

            // Process each file/directory
            for (fileName in files) {
                val path = "$CURATED_CONTENT_DIR/$fileName"

                // Check if it's a directory
                val subFiles = context.assets.list(path)
                if (subFiles != null && subFiles.isNotEmpty()) {
                    // It's a directory, add all JSON files
                    for (subFile in subFiles) {
                        if (subFile.endsWith(".json")) {
                            result.add("$path/$subFile")
                        }
                    }
                } else if (fileName.endsWith(".json")) {
                    // It's a JSON file directly in the curated-content directory
                    result.add(path)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error listing curated content files: ${e.message}")
        }

        return result
    }

    /**
     * Load a single curated content file
     */
    private fun loadCuratedFile(filePath: String) {
        try {
            context.assets.open(filePath).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }

                try {
                    // Parse curated content
                    val curatedContent = gson.fromJson(json, CuratedContent::class.java)

                    // Store metadata with file path as key
                    _metadata[filePath] = curatedContent.metadata

                    // Add entries to the master list
                    _allEntries.addAll(curatedContent.entries)

                    Log.d(TAG, "Loaded ${curatedContent.entries.size} entries from $filePath")
                } catch (e: com.google.gson.JsonSyntaxException) {
                    Log.e(TAG, "Error parsing curated JSON file $filePath: ${e.message}")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading curated file $filePath: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading curated file $filePath: ${e.message}")
        }
    }

    /**
     * Get all entries matching a specific category
     */
    fun getEntriesByCategory(category: String): List<FlashcardEntry> {
        return _allEntries.filter { it.category == category }
    }

    /**
     * Get all entries matching a specific difficulty level
     */
    fun getEntriesByDifficulty(difficulty: String): List<FlashcardEntry> {
        return _allEntries.filter { it.difficulty == difficulty }
    }

    /**
     * Get all entries matching both category and difficulty
     */
    fun getEntriesByCategoryAndDifficulty(category: String, difficulty: String): List<FlashcardEntry> {
        return _allEntries.filter { it.category == category && it.difficulty == difficulty }
    }

    /**
     * Get an entry by its unique ID
     */
    fun getEntryById(id: String): FlashcardEntry? {
        return _allEntries.find { it.id == id }
    }

    /**
     * Get all metadata loaded from curated content files
     */
    fun getAllMetadata(): Map<String, Metadata> {
        return _metadata.toMap()
    }

}