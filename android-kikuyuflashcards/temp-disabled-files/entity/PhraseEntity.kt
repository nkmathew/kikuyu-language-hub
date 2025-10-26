package com.nkmathew.kikuyuflashcards.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nkmathew.kikuyuflashcards.data.PhraseCategory

@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val english: String,
    val kikuyu: String,
    val category: String,
    val difficulty: Int = 1, // 1-5 scale
    val lastReviewed: Long = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val audioUrl: String? = null,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: PhraseCategory
        get() = PhraseCategory.fromString(category)
    
    val totalAttempts: Int
        get() = correctCount + incorrectCount
    
    val successRate: Float
        get() = if (totalAttempts > 0) correctCount.toFloat() / totalAttempts else 0f
    
    val needsReview: Boolean
        get() {
            val oneDay = 24 * 60 * 60 * 1000
            val daysSinceReview = (System.currentTimeMillis() - lastReviewed) / oneDay
            return when {
                successRate >= 0.8 -> daysSinceReview >= 7  // Review weekly if doing well
                successRate >= 0.6 -> daysSinceReview >= 3  // Review every 3 days if okay
                else -> daysSinceReview >= 1                // Review daily if struggling
            }
        }
}