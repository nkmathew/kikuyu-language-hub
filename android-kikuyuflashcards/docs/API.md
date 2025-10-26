# API Documentation

## FailureTracker API

The core analytics engine for tracking learning performance across all modes.

### Data Classes

#### FailureRecord
```kotlin
data class FailureRecord(
    val phraseId: String,           // Unique phrase identifier
    val englishText: String,        // English phrase
    val kikuyuText: String,         // Kikuyu translation
    val category: String,           // Learning category
    val failureType: FailureType,   // Specific error classification
    val learningMode: LearningMode, // Activity context
    val timestamp: Long,            // When failure occurred
    val userAnswer: String,         // User's incorrect response
    val correctAnswer: String,      // Expected correct answer
    val difficulty: String,         // Assessed difficulty level
    val responseTime: Long          // Time taken to respond (ms)
)
```

#### DifficultyWord
```kotlin
data class DifficultyWord(
    val phrase: Phrase,             // Core phrase data
    val failureCount: Int,          // Total failures
    val lastFailureTime: Long,      // Most recent failure
    val learningMode: String,       // Primary learning context
    val masteryLevel: String        // Current mastery assessment
)
```

### Enums

#### FailureType
```kotlin
enum class FailureType {
    TRANSLATION_ERROR,      // Incorrect translation
    RECOGNITION_ERROR,      // Failed to recognize phrase
    RECALL_ERROR,          // Could not recall from memory
    SPELLING_ERROR,        // Minor spelling mistakes
    TIMEOUT_ERROR,         // Took too long to respond
    MULTIPLE_CHOICE_ERROR, // Wrong multiple choice selection
    FILL_BLANK_ERROR,      // Fill-in-the-blank errors
    CLOZE_ERROR,          // Cloze test comprehension errors
    WORD_ASSOCIATION_ERROR, // Word association mistakes
    SPEED_MATCH_ERROR     // Speed matching errors
}
```

#### LearningMode
```kotlin
enum class LearningMode {
    FLASHCARD,            // Traditional flashcard learning
    TYPE_IN_RECALL,       // Type-in recall exercises
    FILL_BLANK,          // Fill-in-the-blank activities
    CLOZE_TEST,          // Cloze comprehension tests
    SPEED_MATCH,         // Speed matching games
    MULTIPLE_ANSWERS,    // Multiple response selection
    WORD_ASSOCIATION,    // Word association exercises
    BEAT_CLOCK,          // Time-based challenges
    STREAK_MASTER        // Streak-based learning
}
```

### Core Methods

#### Recording Learning Events

```kotlin
// Record a learning failure
fun recordFailure(
    phrase: Phrase,
    failureType: FailureType,
    learningMode: LearningMode,
    userAnswer: String = "",
    correctAnswer: String = "",
    difficulty: String = "medium",
    responseTime: Long = 0L
)

// Record a learning success
fun recordSuccess(
    phrase: Phrase,
    learningMode: LearningMode,
    responseTime: Long = 0L
)
```

#### Analytics Retrieval

```kotlin
// Get words needing attention (smart filtering)
fun getWordsNeedingAttention(
    limit: Int = 20,
    maxDaysSinceFailure: Int = 7
): List<DifficultyWord>

// Get problem words with filtering
fun getProblemWords(
    sortBy: String = "failure_count",    // "failure_count", "last_failed", "learning_mode"
    filterBy: String = "all"             // "all", "struggling", "challenging", "learning"
): List<DifficultyWord>

// Get comprehensive statistics
fun getStatistics(): Map<String, Any>
```

#### Data Management

```kotlin
// Clear old failure records (automatic cleanup)
fun cleanupOldData(maxAgeMs: Long = TimeUnit.DAYS.toMillis(30))

// Manual data export (for backup/analysis)
fun exportData(): String

// Reset specific phrase progress
fun resetPhraseProgress(phraseId: String)
```

## FlashCardManager API

Enhanced data management with position memory and session tracking.

### Core Methods

```kotlin
// Get current phrase with context
fun getCurrentPhrase(): Phrase?

// Navigation with memory
fun moveToNext(): Boolean
fun moveToPrevious(): Boolean
fun setCurrentIndex(index: Int)

// Session management
fun startSession()
fun endSession()
fun getSessionStats(): Map<String, Any>

// Data access
fun getAllPhrases(): List<Phrase>
fun getPhrasesInCategory(category: String): List<Phrase>
fun searchPhrases(query: String): List<Phrase>
```

## Data Models

### Phrase
```kotlin
data class Phrase(
    val english: String,     // English text
    val kikuyu: String,      // Kikuyu translation
    val category: String = "",// Learning category
    val difficulty: String = "medium", // Difficulty level
    val pronunciation: String = ""     // Phonetic guide (future)
)
```

### Usage Examples

#### Basic Failure Tracking
```kotlin
// Initialize tracker
val failureTracker = FailureTracker(context)

// Record a spelling error in flashcard mode
failureTracker.recordFailure(
    phrase = currentPhrase,
    failureType = FailureTracker.FailureType.SPELLING_ERROR,
    learningMode = FailureTracker.LearningMode.FLASHCARD,
    userAnswer = "kikyu",
    correctAnswer = "kikuyu",
    responseTime = 3500L
)

// Record a successful recall
failureTracker.recordSuccess(
    phrase = currentPhrase,
    learningMode = FailureTracker.LearningMode.TYPE_IN_RECALL,
    responseTime = 2100L
)
```

#### Analytics Queries
```kotlin
// Get words needing immediate attention
val problemWords = failureTracker.getWordsNeedingAttention(
    limit = 10,
    maxDaysSinceFailure = 3
)

// Get comprehensive learning statistics
val stats = failureTracker.getStatistics()
val totalFailures = stats["total_failures"] as Int
val averageResponseTime = stats["average_response_time"] as Long
val mostCommonFailureType = stats["most_common_failure_type"] as String
```

#### Cross-Mode Intelligence
```kotlin
// The system automatically correlates performance across learning modes
// A word that's difficult in flashcards might also appear in problem words
// for fill-in-the-blank exercises, providing comprehensive learning insights
```

## Data Persistence

### Storage Format
- **Technology**: Gson serialization + SharedPreferences
- **Location**: Private app storage (secure, local-only)
- **Format**: JSON structure for failure records and analytics
- **Cleanup**: Automatic removal of old records (30-day retention)

### Privacy & Security
- All data stored locally on device
- No cloud synchronization or external transmission
- User controls all learning data
- Can be cleared through app settings

## Integration Guidelines

### For New Learning Activities

1. **Initialize FailureTracker** in `onCreate()`
2. **Record timing** for response time tracking
3. **Classify failures** appropriately by type
4. **Specify learning mode** context
5. **Handle edge cases** (timeouts, partial answers)

### Example Integration
```kotlin
class NewLearningActivity : AppCompatActivity() {
    private lateinit var failureTracker: FailureTracker
    private var questionStartTime = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        failureTracker = FailureTracker(this)
    }
    
    private fun startQuestion() {
        questionStartTime = System.currentTimeMillis()
    }
    
    private fun processAnswer(userAnswer: String, correctAnswer: String) {
        val responseTime = System.currentTimeMillis() - questionStartTime
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)
        
        if (isCorrect) {
            failureTracker.recordSuccess(
                phrase = currentPhrase,
                learningMode = FailureTracker.LearningMode.YOUR_MODE,
                responseTime = responseTime
            )
        } else {
            val failureType = determineFailureType(userAnswer, correctAnswer)
            failureTracker.recordFailure(
                phrase = currentPhrase,
                failureType = failureType,
                learningMode = FailureTracker.LearningMode.YOUR_MODE,
                userAnswer = userAnswer,
                correctAnswer = correctAnswer,
                responseTime = responseTime
            )
        }
    }
}
```

## Performance Considerations

- **Memory Usage**: Efficient data structures with automatic cleanup
- **Storage Size**: JSON compression and old data removal
- **Query Performance**: Indexed by phrase ID and timestamp
- **Real-time Updates**: Immediate analytics availability
- **Background Processing**: No blocking operations on UI thread