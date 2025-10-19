package com.nkmathew.kikuyuflashcards

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
        // Get popular reasons for quick selection
        val popularReasons = flagStorageService.getPopularReasons(3)

        // Create custom dialog view with EditText and quick select buttons
        val dialogView = layoutInflater.inflate(R.layout.dialog_flag_reason, null)
        val editText = dialogView.findViewById<EditText>(R.id.reasonEditText)
        val quickReasonsLayout = dialogView.findViewById<LinearLayout>(R.id.quickReasonsLayout)

        // Set existing reason if available
        editText.setText(flagReasons[cardId] ?: "")

        // If we have popular reasons, show them as quick select buttons
        if (popularReasons.isNotEmpty()) {
            popularReasons.forEach { reason ->
                val button = Button(this).apply {
                    text = reason
                    textSize = 12f
                    setBackgroundColor(getColor(R.color.md_theme_light_surfaceVariant))
                    setTextColor(getColor(R.color.md_theme_light_onSurfaceVariant))

                    // Set click listener to fill the EditText with this reason
                    setOnClickListener {
                        editText.setText(reason)
                    }

                    // Set layout params
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 8.dpToPx()
                    }
                }
                quickReasonsLayout.addView(button)
            }
        } else {
            // Hide the quick reasons section if no popular reasons
            dialogView.findViewById<TextView>(R.id.quickReasonsTitle).visibility = View.GONE
            quickReasonsLayout.visibility = View.GONE
        }

        // Create a dialog to ask for the reason
        AlertDialog.Builder(this)
            .setTitle("Flag Reason")
            .setMessage("Why is this translation flagged?")
            .setView(dialogView)
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
     * Includes a JSON format of the flagged translations
     * Requires a reason for each flagged card
     */
    private fun shareFlaggedItems() {
        if (flaggedCards.isEmpty()) {
            Toast.makeText(this, "No flagged items to share", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if all flagged cards have reasons
        val cardsWithoutReasons = flaggedCards.filter { card ->
            flagReasons[card.id].isNullOrBlank()
        }

        if (cardsWithoutReasons.isNotEmpty()) {
            // Some cards are missing reasons, prompt user to add them
            promptForMissingReasons(cardsWithoutReasons)
            return
        }

        // All cards have reasons, proceed with email
        sendFlaggedItemsEmail()
    }

    /**
     * Prompt user to add reasons for flagged cards that don't have them
     */
    private fun promptForMissingReasons(cardsWithoutReasons: List<FlashcardEntry>) {
        // Get the first card without a reason
        val card = cardsWithoutReasons.first()

        // Get popular reasons for quick selection
        val popularReasons = flagStorageService.getPopularReasons(3)

        // Create custom dialog view with EditText and quick select buttons
        val dialogView = layoutInflater.inflate(R.layout.dialog_flag_reason, null)
        val editText = dialogView.findViewById<EditText>(R.id.reasonEditText)
        val quickReasonsLayout = dialogView.findViewById<LinearLayout>(R.id.quickReasonsLayout)

        // If we have popular reasons, show them as quick select buttons
        if (popularReasons.isNotEmpty()) {
            popularReasons.forEach { reason ->
                val button = Button(this).apply {
                    text = reason
                    textSize = 12f
                    setBackgroundColor(getColor(R.color.md_theme_light_surfaceVariant))
                    setTextColor(getColor(R.color.md_theme_light_onSurfaceVariant))

                    // Set click listener to fill the EditText with this reason
                    setOnClickListener {
                        editText.setText(reason)
                    }

                    // Set layout params
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 8.dpToPx()
                    }
                }
                quickReasonsLayout.addView(button)
            }
        } else {
            // Hide the quick reasons section if no popular reasons
            dialogView.findViewById<TextView>(R.id.quickReasonsTitle).visibility = View.GONE
            quickReasonsLayout.visibility = View.GONE
        }

        // Create a dialog to ask for the reason
        val dialog = AlertDialog.Builder(this)
            .setTitle("Reason Required")
            .setMessage("Please provide a reason for flagging:\n\n${card.kikuyu} - ${card.english}")
            .setView(dialogView)
            .setCancelable(false) // User must take action
            .setPositiveButton("Save", null) // Set to null initially, will override below
            .setNegativeButton("Cancel Email") { _, _ ->
                // User canceled, don't send email
                Toast.makeText(this, "Email canceled", Toast.LENGTH_SHORT).show()
            }
            .create()

        // Show the dialog
        dialog.show()

        // Override the positive button to prevent dialog dismiss when reason is empty
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val reason = editText.text.toString().trim()
            if (reason.isNotEmpty()) {
                // Save the reason
                flagStorageService.setFlagReason(card.id, reason)
                flagReasons[card.id] = reason
                flaggedCardAdapter.updateCards(flaggedCards, flagReasons)
                Toast.makeText(this, "Reason saved", Toast.LENGTH_SHORT).show()

                // Dismiss this dialog
                dialog.dismiss()

                // Check if there are more cards without reasons
                val remainingCardsWithoutReasons = flaggedCards.filter {
                    flagReasons[it.id].isNullOrBlank()
                }

                if (remainingCardsWithoutReasons.isNotEmpty()) {
                    // Prompt for the next card's reason
                    promptForMissingReasons(remainingCardsWithoutReasons)
                } else {
                    // All cards now have reasons, send email
                    sendFlaggedItemsEmail()
                }
            } else {
                // User didn't provide a reason, show error
                Toast.makeText(this, "A reason is required", Toast.LENGTH_SHORT).show()
                // Don't dismiss the dialog
            }
        }
    }

    /**
     * Extension function to convert dp to pixels
     */
    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    /**
     * Send email with all flagged items that now have reasons
     */
    private fun sendFlaggedItemsEmail() {
        // Create human-readable summary
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
                // All cards should have reasons at this point
                val reason = flagReasons[card.id] ?: "No reason provided"
                appendLine("  ⚠️ Flag Reason: $reason")
                appendLine()
            }

            appendLine("\n----------\nJSON DATA BELOW:\n----------\n")
        }

        // Create JSON data array
        val jsonData = buildJsonData()

        // Create email intent specifically for the developer
        // First try with ACTION_SENDTO (more secure but sometimes loses subject/body)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:kipkoechmathew+kikuyuflashards@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Flagged Kikuyu Translations (${flaggedCards.size} items)")
            putExtra(Intent.EXTRA_TEXT, shareText + jsonData)
        }

        // Fallback to ACTION_SEND (will show more apps but always includes subject/body)
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // MIME type for email
            putExtra(Intent.EXTRA_EMAIL, arrayOf("kipkoechmathew+kikuyuflashards@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Flagged Kikuyu Translations (${flaggedCards.size} items)")
            putExtra(Intent.EXTRA_TEXT, shareText + jsonData)
        }

        // Try to find apps that can handle the intent
        try {
            // First try the primary intent
            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send email using..."))
            } else {
                // If no app is found for ACTION_SENDTO, try the fallback
                startActivity(Intent.createChooser(fallbackIntent, "Send email using..."))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found. Please install an email app to share.", Toast.LENGTH_SHORT).show()
            // Log the error for debugging
            android.util.Log.e("FlaggedTranslations", "Error sending email: ${e.message}")
        }
    }

    /**
     * Build JSON data format for flagged translations
     */
    private fun buildJsonData(): String {
        val jsonBuilder = StringBuilder()
        jsonBuilder.append("{\n")
        jsonBuilder.append("  \"flagged_translations\": [\n")

        flaggedCards.forEachIndexed { index, card ->
            jsonBuilder.append("    {\n")
            jsonBuilder.append("      \"id\": \"${escapeJson(card.id)}\",\n")
            jsonBuilder.append("      \"kikuyu\": \"${escapeJson(card.kikuyu)}\",\n")
            jsonBuilder.append("      \"english\": \"${escapeJson(card.english)}\",\n")
            jsonBuilder.append("      \"category\": \"${escapeJson(card.category)}\",\n")
            jsonBuilder.append("      \"difficulty\": \"${escapeJson(card.difficulty)}\",\n")

            // Add reason if available
            flagReasons[card.id]?.let { reason ->
                jsonBuilder.append("      \"flag_reason\": \"${escapeJson(reason)}\",\n")
            }

            // Add optional fields
            card.culturalNotes?.let {
                if (it.isNotEmpty()) {
                    jsonBuilder.append("      \"cultural_notes\": \"${escapeJson(it)}\",\n")
                }
            }

            // Add source info
            jsonBuilder.append("      \"source\": {\n")
            jsonBuilder.append("        \"origin\": \"${escapeJson(card.source?.origin ?: "Unknown")}\"\n")
            jsonBuilder.append("      }\n")

            // Add comma for all items except the last one
            if (index < flaggedCards.size - 1) {
                jsonBuilder.append("    },\n")
            } else {
                jsonBuilder.append("    }\n")
            }
        }

        jsonBuilder.append("  ]\n")
        jsonBuilder.append("}\n")

        return jsonBuilder.toString()
    }

    /**
     * Helper method to escape JSON string values properly
     */
    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
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
