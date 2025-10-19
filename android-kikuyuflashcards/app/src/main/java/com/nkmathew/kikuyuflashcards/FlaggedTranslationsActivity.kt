package com.nkmathew.kikuyuflashcards

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import com.nkmathew.kikuyuflashcards.services.FlagStorageService
import com.nkmathew.kikuyuflashcards.ui.adapters.FlaggedCardAdapter
import java.io.File
import java.io.FileWriter

/**
 * Activity that displays flagged translations for review and export
 */
class FlaggedTranslationsActivity : AppCompatActivity() {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var exportButton: Button
    private lateinit var shareButton: Button
    private lateinit var clearAllButton: Button
    private lateinit var flaggedCardAdapter: FlaggedCardAdapter

    // Services
    private lateinit var flagStorageService: FlagStorageService
    private lateinit var flashCardManager: FlashCardManagerV2

    // State
    private var flaggedCards = listOf<FlashcardEntry>()
    private val flagReasons = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flagged_translations)

        // Apply dark theme
        ThemeManager.setTheme(this, ThemeManager.ThemeMode.DARK)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        exportButton = findViewById(R.id.exportButton)
        shareButton = findViewById(R.id.shareButton)
        clearAllButton = findViewById(R.id.clearAllButton)

        // Initialize services
        flagStorageService = FlagStorageService(this)
        flashCardManager = FlashCardManagerV2(this)

        // Set up click listeners
        setupClickListeners()

        // Set up RecyclerView
        setupRecyclerView()

        // Load flagged items
        loadFlaggedItems()
    }

    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        exportButton.setOnClickListener {
            exportFlaggedItems()
        }

        shareButton.setOnClickListener {
            shareFlaggedItems()
        }

        clearAllButton.setOnClickListener {
            clearAllFlags()
        }
    }

    /**
     * Set up RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        flaggedCardAdapter = FlaggedCardAdapter(
            onRemoveFlag = { cardId ->
                removeFlag(cardId)
            },
            onAddReason = { cardId ->
                showReasonDialog(cardId)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = flaggedCardAdapter
    }

    /**
     * Load flagged items from storage
     */
    private fun loadFlaggedItems() {
        val flaggedIds = flagStorageService.getFlaggedItems()
        val allCards = flashCardManager.getAllEntries()
        
        // Filter to only flagged cards
        flaggedCards = allCards.filter { it.id in flaggedIds }
        
        // Load flag reasons
        flagReasons.clear()
        flagReasons.putAll(flagStorageService.getFlagReasons())
        
        // Update UI
        updateUI()
    }

    /**
     * Update UI based on current state
     */
    private fun updateUI() {
        if (flaggedCards.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            emptyStateTextView.visibility = android.view.View.VISIBLE
            emptyStateTextView.text = "No flagged translations found.\nFlag translations in the study list to see them here."
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyStateTextView.visibility = android.view.View.GONE
            flaggedCardAdapter.updateCards(flaggedCards, flagReasons)
        }
    }

    /**
     * Remove flag from a card
     */
    private fun removeFlag(cardId: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove Flag")
            .setMessage("Are you sure you want to remove the flag from this translation?")
            .setPositiveButton("Remove") { _, _ ->
                flagStorageService.unflagCard(cardId)
                loadFlaggedItems()
                Toast.makeText(this, "Flag removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show dialog to add/edit flag reason
     */
    private fun showReasonDialog(cardId: String) {
        val editText = EditText(this).apply {
            setText(flagReasons[cardId] ?: "")
            hint = "Enter reason for flagging this translation..."
            minLines = 3
            maxLines = 5
        }

        AlertDialog.Builder(this)
            .setTitle("Flag Reason")
            .setMessage("Why is this translation flagged?")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val reason = editText.text.toString().trim()
                if (reason.isNotEmpty()) {
                    flagStorageService.setFlagReason(cardId, reason)
                    flagReasons[cardId] = reason
                    flaggedCardAdapter.updateCards(flaggedCards, flagReasons)
                    Toast.makeText(this, "Reason saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Export flagged items to clipboard
     */
    private fun exportFlaggedItems() {
        if (flaggedCards.isEmpty()) {
            Toast.makeText(this, "No flagged items to export", Toast.LENGTH_SHORT).show()
            return
        }

        val exportText = buildString {
            appendLine("Flagged Kikuyu Translations (${flaggedCards.size} items):")
            appendLine()
            
            flaggedCards.forEach { card ->
                appendLine("• ${card.id}")
                appendLine("  ${card.kikuyu} - ${card.english}")
                appendLine("  Difficulty: ${card.difficulty} | Category: ${card.category}")
                if (card.culturalNotes?.isNotEmpty() == true) {
                    appendLine("  Notes: ${card.culturalNotes}")
                }
                if (card.source?.origin?.isNotEmpty() == true) {
                    appendLine("  Source: ${card.source.origin}")
                }
                flagReasons[card.id]?.let { reason ->
                    appendLine("  ⚠️ Flag Reason: $reason")
                }
                appendLine()
            }
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Flagged Translations", exportText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, "Flagged translations copied to clipboard", Toast.LENGTH_LONG).show()
    }

    /**
     * Share flagged items via email to developer
     */
    private fun shareFlaggedItems() {
        if (flaggedCards.isEmpty()) {
            Toast.makeText(this, "No flagged items to share", Toast.LENGTH_SHORT).show()
            return
        }

        val shareText = buildString {
            appendLine("Flagged Kikuyu Translations (${flaggedCards.size} items):")
            appendLine()

            flaggedCards.forEach { card ->
                appendLine("• ${card.id}")
                appendLine("  ${card.kikuyu} - ${card.english}")
                appendLine("  Difficulty: ${card.difficulty} | Category: ${card.category}")
                if (card.culturalNotes?.isNotEmpty() == true) {
                    appendLine("  Notes: ${card.culturalNotes}")
                }
                if (card.source?.origin?.isNotEmpty() == true) {
                    appendLine("  Source: ${card.source.origin}")
                }
                flagReasons[card.id]?.let { reason ->
                    appendLine("  ⚠️ Flag Reason: $reason")
                }
                appendLine()
            }
        }

        // Create email intent specifically for the developer
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:kipkoechmathew+kikuyuflashards@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Flagged Kikuyu Translations (${flaggedCards.size} items)")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found. Please install an email app to share.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Export flagged items to file
     */
    private fun exportToFile() {
        if (flaggedCards.isEmpty()) {
            Toast.makeText(this, "No flagged items to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportText = buildString {
                appendLine("Flagged Kikuyu Translations (${flaggedCards.size} items):")
                appendLine("Exported on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                appendLine()
                
                flaggedCards.forEach { card ->
                    appendLine("• ${card.id}")
                    appendLine("  ${card.kikuyu} - ${card.english}")
                    appendLine("  Difficulty: ${card.difficulty} | Category: ${card.category}")
                    if (card.culturalNotes?.isNotEmpty() == true) {
                        appendLine("  Notes: ${card.culturalNotes}")
                    }
                    if (card.source?.origin?.isNotEmpty() == true) {
                        appendLine("  Source: ${card.source.origin}")
                    }
                    flagReasons[card.id]?.let { reason ->
                        appendLine("  ⚠️ Flag Reason: $reason")
                    }
                    appendLine()
                }
            }

            val fileName = "flagged_translations_${System.currentTimeMillis()}.txt"
            val file = File(getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write(exportText)
            }
            
            Toast.makeText(this, "Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Clear all flags
     */
    private fun clearAllFlags() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Flags")
            .setMessage("This will remove all flagged translations and their reasons. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                flagStorageService.clearAllFlags()
                loadFlaggedItems()
                Toast.makeText(this, "All flags cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
