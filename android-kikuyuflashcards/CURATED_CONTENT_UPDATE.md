# Curated Content Update for Kikuyu Flashcards Android App

This document describes the updates made to the Kikuyu Flashcards Android app to support the new curated JSON schema format.

## Overview of Changes

The Android app has been enhanced to support the rich, metadata-heavy JSON schema used by the web app and backend. These changes enable the Android app to display more comprehensive and educational content, including difficulty levels, quality scores, cultural notes, grammatical information, and examples.

## Key Components Added

### 1. Data Models (Kotlin Classes)

- **CuratedContent.kt**: Root data class representing the full JSON file structure with metadata and entries.
- **Metadata**: Information about the JSON file itself (curator, source files, timestamps).
- **FlashcardEntry**: Core data model for individual entries with all rich metadata.
- **Supporting Models**: SourceInfo, QualityInfo, ExampleSentence, GrammaticalInfo, etc.
- **Utility Classes**: Categories and DifficultyLevels for constants and helper methods.

### 2. Content Management

- **CuratedContentManager**: Loads content from both legacy and curated JSON files, handling directory traversal.
- **FlashCardManagerV2**: Enhanced manager with support for new filtering capabilities (difficulty, etc.).
- **PhraseAdapter**: Converts between new and legacy data models for backward compatibility.
- **PositionManagerV2**: Extended position tracking with support for combined category+difficulty filters.

### 3. UI Components (Jetpack Compose)

- **EnhancedFlashCard**: Rich flashcard component that displays the additional metadata.
- **EnhancedFlashCardScreen**: Screen that manages the enhanced flashcards with filtering.
- **EnhancedFlashCardViewModel**: ViewModel to handle the business logic and state management.
- **UI Utilities**: Category badges, difficulty badges, and other visual components.

### 4. Content Sync

- **sync-android-content.py**: Script to sync curated content from backend or web app to the Android assets.

## Implementation Details

### Content Loading Flow

1. **CuratedContentManager** first tries to load legacy content from `kikuyu-phrases.json`.
2. It then scans the `assets/curated-content` directory for category subdirectories.
3. Each JSON file in these directories is loaded and parsed.
4. All entries are combined in a master list with appropriate metadata.

### Backward Compatibility

- **Legacy Format Support**: The app still supports the old JSON format for backward compatibility.
- **PhraseAdapter**: Converts between FlashcardEntry and Phrase models to maintain backward compatibility.
- **Dual Property Access**: The FlashcardEntry model includes legacy getters like `text` for backward compatibility.

### Enhanced Filtering Capabilities

- **Category Filtering**: Filter by content category (vocabulary, phrases, etc.).
- **Difficulty Filtering**: Filter by difficulty level (beginner, intermediate, advanced).
- **Combined Filtering**: Apply both category and difficulty filters simultaneously.
- **Position Memory**: Track reading position separately for each category+difficulty combination.

### UI Enhancements

- **Dynamic Styling**: Card styling based on difficulty level and content category.
- **Metadata Display**: Badges for category, difficulty, and quality score.
- **Expanded Information**: Cultural notes, grammatical details, example sentences, etc.
- **Improved Filtering**: UI controls for selecting categories and difficulty levels.

## Testing

- **Model Tests**: Verify correct parsing of the new JSON schema.
- **Adapter Tests**: Ensure proper conversion between legacy and new models.
- **ContentManager Tests**: Validate content loading from multiple files.
- **Filter Tests**: Confirm that filtering by category and difficulty works correctly.

## Usage Instructions

### Syncing Content

To sync curated content from the backend or web app to the Android assets directory:

```bash
# From web app
python sync-android-content.py --source webapp

# From backend
python sync-android-content.py --source backend
```

This will copy all JSON files from the source directory to `app/src/main/assets/curated-content/`.

### Directory Structure

The Android app expects content to be organized as follows:

```
assets/
├── kikuyu-phrases.json (legacy file, can be empty)
└── curated-content/
    ├── vocabulary/
    │   └── *.json
    ├── phrases/
    │   └── *.json
    ├── grammar/
    │   └── *.json
    ├── conjugations/
    │   └── *.json
    ├── proverbs/
    │   └── *.json
    ├── cultural/
    │   └── *.json
    └── file_registry.json
```

### Using the Enhanced Components

To use the enhanced components in an activity:

```kotlin
// Initialize ViewModel
val viewModel = ViewModelProvider(this).get(EnhancedFlashCardViewModel::class.java)

// Use in Compose UI
setContent {
    EnhancedFlashCardScreen(
        flashCardManager = viewModel.getFlashCardManager(),
        onNavigateBack = { finish() }
    )
}
```

## Future Work

- **Audio Pronunciation**: Support for audio files referenced in the schema.
- **Image Support**: Display images referenced in the schema.
- **Interactive Examples**: Make example sentences interactive.
- **Content Update**: Automatic content updates from backend API.
- **User Contributions**: Allow users to flag or suggest corrections to content.

## Conclusion

These updates significantly enhance the Kikuyu Flashcards Android app, bringing it in line with the rich educational content available in the web version. The app now provides a more comprehensive language learning experience with cultural context, grammatical details, and difficulty-appropriate content filtering.