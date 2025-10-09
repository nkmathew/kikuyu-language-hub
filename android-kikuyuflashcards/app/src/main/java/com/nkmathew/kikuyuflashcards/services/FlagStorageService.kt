package com.nkmathew.kikuyuflashcards.services

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service to manage flagged translations and their reasons
 */
class FlagStorageService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("flagged_translations", Context.MODE_PRIVATE)
    private val FLAGGED_ITEMS_KEY = "flagged_items"
    private val FLAG_REASONS_KEY = "flag_reasons"
    
    /**
     * Add a card to the flagged list
     */
    fun flagCard(cardId: String) {
        val flaggedItems = getFlaggedItems().toMutableSet()
        flaggedItems.add(cardId)
        saveFlaggedItems(flaggedItems.toList())
    }
    
    /**
     * Remove a card from the flagged list
     */
    fun unflagCard(cardId: String) {
        val flaggedItems = getFlaggedItems().toMutableSet()
        flaggedItems.remove(cardId)
        saveFlaggedItems(flaggedItems.toList())
        
        // Also remove the reason if it exists
        removeFlagReason(cardId)
    }
    
    /**
     * Check if a card is flagged
     */
    fun isCardFlagged(cardId: String): Boolean {
        return getFlaggedItems().contains(cardId)
    }
    
    /**
     * Get all flagged card IDs
     */
    fun getFlaggedItems(): Set<String> {
        val flaggedJson = prefs.getString(FLAGGED_ITEMS_KEY, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(flaggedJson)
            val items = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                items.add(jsonArray.getString(i))
            }
            items
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * Save flagged items to storage
     */
    private fun saveFlaggedItems(items: List<String>) {
        val jsonArray = JSONArray()
        items.forEach { jsonArray.put(it) }
        prefs.edit().putString(FLAGGED_ITEMS_KEY, jsonArray.toString()).apply()
    }
    
    /**
     * Add or update a flag reason
     */
    fun setFlagReason(cardId: String, reason: String) {
        val reasons = getFlagReasons().toMutableMap()
        reasons[cardId] = reason
        saveFlagReasons(reasons)
    }
    
    /**
     * Get flag reason for a card
     */
    fun getFlagReason(cardId: String): String? {
        return getFlagReasons()[cardId]
    }
    
    /**
     * Remove flag reason for a card
     */
    fun removeFlagReason(cardId: String) {
        val reasons = getFlagReasons().toMutableMap()
        reasons.remove(cardId)
        saveFlagReasons(reasons)
    }
    
    /**
     * Get all flag reasons
     */
    fun getFlagReasons(): Map<String, String> {
        val reasonsJson = prefs.getString(FLAG_REASONS_KEY, "{}") ?: "{}"
        return try {
            val jsonObject = JSONObject(reasonsJson)
            val reasons = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                reasons[key] = jsonObject.getString(key)
            }
            reasons
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Save flag reasons to storage
     */
    private fun saveFlagReasons(reasons: Map<String, String>) {
        val jsonObject = JSONObject()
        reasons.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        prefs.edit().putString(FLAG_REASONS_KEY, jsonObject.toString()).apply()
    }
    
    /**
     * Clear all flagged items and reasons
     */
    fun clearAllFlags() {
        prefs.edit()
            .remove(FLAGGED_ITEMS_KEY)
            .remove(FLAG_REASONS_KEY)
            .apply()
    }
    
    /**
     * Get count of flagged items
     */
    fun getFlaggedCount(): Int {
        return getFlaggedItems().size
    }
}
