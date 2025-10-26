# CLAUDE.md - Kikuyu Flash Cards Project Guide

## Project Overview

**Kikuyu Flash Cards** is a modern Android mobile application designed to help users learn Kikuyu (Gĩkũyũ) language through interactive flashcards. The app displays English phrases with their Kikuyu translations, supporting navigation through swipe gestures and button controls.

- **Target Platform**: Android (minimum SDK 24, target SDK 35)
- **Language**: Kotlin (modernized from Java)
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with Compose + ViewModel
- **App Package**: `com.nkmathew.kikuyuflashcards`

## 🎯 Comprehensive Learning System (Latest Update)

The app has been enhanced with a complete adaptive learning ecosystem:
- **Advanced Learning Modes**: FlashCards, Fill-in-the-Blank, Cloze Tests, Multiple Response Games, Type-in Recall
- **Intelligent Failure Tracking**: Comprehensive analytics system that tracks problem words across all learning modes
- **Adaptive Problem Words Practice**: Dedicated screens for focused practice on challenging vocabulary
- **Smart Analytics**: 10 failure types, 9 learning modes, mastery level assessment, and response time tracking
- **Modern UI/UX**: Material 3 Design, enhanced animations, gradient backgrounds, and interactive feedback
- **Kotlin Architecture**: 100% Kotlin codebase with modern Android patterns

## Architecture & Structure

### High-Level Architecture (Advanced Learning System)
```
MainActivity (Learning Hub)
    ↓
┌─ FlashCardActivity (Adaptive Cards)
├─ FillInTheBlankActivity (Contextual Learning)  
├─ ClozeTestActivity (Comprehension)
├─ MultipleResponseGameActivity (Gamified Learning)
├─ ProblemWordsActivity (Analytics Dashboard)
└─ ProblemWordsPracticeActivity (Targeted Practice)
    ↓
FailureTracker (AI Analytics Engine)
    ↓
FlashCardManager (Data Management)
    ↓
JSON Asset File + Persistent Analytics Storage
```

### Advanced Tech Stack
- **Learning Analytics**: FailureTracker with comprehensive failure type classification
- **Adaptive Learning**: Smart problem word identification and targeted practice
- **Modern UI**: Material 3 with dynamic gradients and interactive animations
- **Data Persistence**: Gson serialization with SharedPreferences for analytics storage
- **Response Tracking**: Millisecond-precision timing and mastery level assessment
- **Cross-Mode Intelligence**: Failure patterns tracked across all learning activities

### Key Directories (Updated)
```
kikuyu-flash-cards/
├── app/
│   ├── src/main/
│   │   ├── java/com/nkmathew/kikuyuflashcards/
│   │   │   ├── ui/
│   │   │   │   ├── theme/          # Material 3 theme files
│   │   │   │   ├── components/     # Reusable Compose components
│   │   │   │   ├── screens/        # Screen composables
│   │   │   │   └── viewmodel/      # ViewModels for state management
│   │   │   ├── *.kt               # Core Kotlin classes
│   │   │   └── MainActivity.kt    # Compose Activity
│   │   ├── res/                   # Legacy resources (still used for icons)
│   │   ├── assets/                # Data files (kikuyu-phrases.json)
│   │   └── AndroidManifest.xml    # App configuration
│   ├── build.gradle               # App-level build (updated for Compose)
│   └── debug.keystore            # Debug signing key
├── gradle/
│   └── libs.versions.toml        # Version catalog (updated for Kotlin)
├── text-notes/                   # Documentation and notes
├── build.gradle                  # Project-level build configuration
├── settings.gradle               # Project settings
└── gradle.properties             # Gradle configuration
```

### Core Components (Advanced Learning System)
#### Learning Activity Classes
- **MainActivity.kt** - Central learning hub with navigation to all modes
- **FlashCardActivity.kt** - Enhanced flashcards with type-in recall and flip animations
- **FillInTheBlankActivity.kt** - Contextual learning with multiple difficulty levels
- **ClozeTestActivity.kt** - Advanced comprehension tests with word bank matching
- **MultipleResponseGameActivity.kt** - Gamified learning with 5 game modes and streak tracking
- **ProblemWordsActivity.kt** - Analytics dashboard showing words needing attention
- **ProblemWordsPracticeActivity.kt** - Focused practice sessions for struggling vocabulary

#### Analytics & Intelligence Engine
- **FailureTracker.kt** - Comprehensive learning analytics with 10 failure types and 9 learning modes
- **FlashCardManager.kt** - Enhanced data management with position memory and session tracking
- **ProgressManager.kt** - Session management and progress persistence
- **SoundManager.kt** - Audio feedback system

#### Data Models & Utilities
- **Phrase.kt** - Enhanced data class for English-Kikuyu phrase pairs with categorization
- **SwipeGestureDetector.kt** - Advanced gesture handling for swipe navigation
- **AnimationHelpers.kt** - Smooth transition and interaction animations

#### Persistent Storage
- **kikuyu-phrases.json** - Comprehensive phrase database with metadata
- **SharedPreferences + Gson** - Analytics storage for failure tracking and mastery levels

## Development Commands

### Build Commands (Updated for Kotlin/Compose)
```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build artifacts (recommended before major changes)
./gradlew clean

# Build and install on connected device
./gradlew installDebug

# Build with verbose logging (for troubleshooting Compose issues)
./gradlew assembleDebug --info
```

### Test Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

### Development Server/Debugging
```bash
# Enable debug logging and install
./gradlew installDebug

# View logs
adb logcat -s MainActivity FlashCardManager
```

### Lint and Code Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

## Coding Standards & Conventions

### Java Coding Style
- **Class Names**: PascalCase (e.g., `MainActivity`, `FlashCardManager`)
- **Method Names**: camelCase (e.g., `getCurrentPhrase`, `navigateToNextPhrase`)
- **Variable Names**: camelCase (e.g., `currentIndex`, `flashCardManager`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `TAG`, `PREFS_NAME`)

### Import Conventions
- Android framework imports first
- Third-party library imports
- Local package imports last
- Alphabetical ordering within each group

### Error Handling Patterns
- Use try-catch blocks with proper logging using `Log.d/e/w`
- Log errors with meaningful tags and messages
- Provide user feedback through Toast messages for critical errors
- Use null checks before accessing objects

### Logging Pattern
```java
private static final String TAG = "ClassName";
Log.d(TAG, "Method: Description of action");
Log.e(TAG, "Method: Error description", exception);
```

### Activity Lifecycle Management
- Save state in `onPause()` and `onDestroy()`
- Load saved state in `onCreate()`
- Use SharedPreferences for simple data persistence

## Development Workflow

### Git Workflow
- **Main Branch**: `master`
- **Commit Messages**: Descriptive, present tense (e.g., "Add swipe gesture support")
- **File Organization**: Keep related changes in single commits

### Branch Naming
- Feature branches: `feature/description`
- Bug fixes: `fix/issue-description`
- Improvements: `enhance/feature-name`

## Configuration & Environment

### Android Configuration
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Java Version**: 11

### Key Dependencies
```gradle
// UI Components
implementation libs.appcompat           // AndroidX AppCompat
implementation libs.material            // Material Design Components
implementation 'androidx.recyclerview:recyclerview:1.2.1'
implementation 'androidx.cardview:cardview:1.0.0'

// Analytics & Data Persistence
implementation 'com.google.code.gson:gson:2.10.1'  // JSON serialization for failure tracking

// Testing
testImplementation libs.junit
androidTestImplementation libs.ext.junit
androidTestImplementation libs.espresso.core
```

### Environment Files
- **gradle.properties** - Project-wide Gradle settings
- **local.properties** - Local SDK path configuration
- **debug.keystore** - Debug signing configuration

## Common Tasks & Patterns

### Adding New Phrases
1. Edit `app/src/main/assets/kikuyu-phrases.json`
2. Add new phrase object with "english" and "kikuyu" keys
3. Rebuild and test the app
4. **Note**: New phrases automatically integrate with failure tracking system

### Adding New Learning Modes
1. Create new Activity in `com.nkmathew.kikuyuflashcards` package
2. Integrate FailureTracker for analytics (see existing activities as examples)
3. Add navigation button in MainActivity
4. Define appropriate failure types and learning mode enum values
5. Test with failure tracking to ensure proper analytics capture

### Failure Tracking Integration
- **All learning activities** automatically record successes and failures
- **10 failure types** tracked: Translation, Recognition, Recall, Spelling, Timeout, Multiple Choice, etc.
- **Response time tracking** with millisecond precision
- **Mastery level assessment** based on performance patterns
- **Cross-mode analytics** for comprehensive learning insights

### Data Management
- Phrases loaded from JSON asset file on app startup
- **Failure analytics** persisted using Gson + SharedPreferences
- **Position memory** maintained across sessions
- **Learning progress** tracked per phrase and learning mode
- Current position automatically saved on pause/destroy

### Analytics Access
- **ProblemWordsActivity**: View words needing attention with filtering and sorting
- **ProblemWordsPracticeActivity**: Targeted practice for struggling vocabulary
- **Automatic failure categorization** based on user response patterns
- **Smart difficulty assessment** with adaptive thresholds

## File Access Guidelines

### Safe to Modify
- **Source Files**: All files in `app/src/main/java/` (including new FailureTracker and Problem Words activities)
- **Resources**: All files in `app/src/main/res/`
- **Data File**: `app/src/main/assets/kikuyu-phrases.json`
- **Build Configuration**: `app/build.gradle` (updated with Gson dependency)
- **Analytics Storage**: Automatic via FailureTracker (uses SharedPreferences internally)

### Handle with Care
- **Manifest**: `AndroidManifest.xml` (may affect app permissions)
- **Gradle Files**: `build.gradle`, `settings.gradle` (affects build process)
- **Keystore**: `debug.keystore` (required for debug builds)

### Do Not Modify
- **Generated Files**: Everything in `app/build/`
- **Gradle Wrapper**: Files in `gradle/wrapper/`
- **IDE Files**: `.idea/` directory contents

## Known Issues & Gotchas

### Performance Considerations
- JSON file is loaded synchronously on main thread (acceptable for current size)
- Consider AsyncTask or background thread if phrase count grows significantly
- UI updates happen on main thread - avoid heavy processing in UI methods

### Android Compatibility
- Minimum SDK 24 ensures broad device compatibility
- Material Design components may look different on older Android versions
- Swipe gestures work on all supported versions

### Memory Management
- JSON data held in memory throughout app lifecycle
- SharedPreferences accessed synchronously (fine for simple data)
- No memory leaks with current implementation

### Common Pitfalls
- Always check if FlashCardManager has phrases before accessing
- Handle null returns from getCurrentPhrase() method
- Ensure proper bounds checking when setting current index
- Save state frequently to prevent data loss

## Quick Reference

### Most Frequently Used Commands
```bash
# Build and install debug version
./gradlew installDebug

# View app logs
adb logcat -s MainActivity FlashCardManager

# Clean and rebuild
./gradlew clean assembleDebug
```

### Key File Locations
- **Main Activity**: `app/src/main/java/com/nkmathew/kikuyuflashcards/MainActivity.java`
- **Data File**: `app/src/main/assets/kikuyu-phrases.json`
- **Main Layout**: `app/src/main/res/layout/activity_main.xml`
- **App Configuration**: `app/src/main/AndroidManifest.xml`

### Useful Debugging Commands
```bash
# Install and start app
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nkmathew.kikuyuflashcards/.MainActivity

# Clear app data
adb shell pm clear com.nkmathew.kikuyuflashcards

# View current activity
adb shell dumpsys activity activities
```

### Development Notes
- **Current Phrase Count**: Loaded from JSON asset file (check actual count in kikuyu-phrases.json)
- **Advanced Learning Features**: 5 learning modes with comprehensive analytics and problem word tracking
- **Failure Analytics**: Automatic tracking of 10 failure types across 9 learning modes with response time analysis
- **Adaptive Practice**: Smart identification of problem words with targeted practice sessions
- **Navigation**: Enhanced with "🎯 Practice Problem Words" button for accessing analytics dashboard
- **State Persistence**: Position, learning progress, and failure analytics saved automatically
- **Gesture Support**: Enhanced swipe gestures with haptic feedback and animations
- **Cross-Mode Intelligence**: Learning patterns tracked across all activities for comprehensive insights

## 🧠 Intelligent Failure Tracking System

### Overview
The app features a sophisticated analytics engine that automatically tracks learning patterns and identifies problem words across all learning modes. This system provides adaptive, personalized learning experiences.

### Failure Tracking Features

#### 📊 Comprehensive Analytics
- **10 Failure Types**: Translation Error, Recognition Error, Recall Error, Spelling Error, Timeout Error, Multiple Choice Error, Fill Blank Error, Cloze Error, Word Association Error, Speed Match Error
- **9 Learning Modes**: Flashcard, Type-in Recall, Fill Blank, Cloze Test, Speed Match, Multiple Answers, Word Association, Beat Clock, Streak Master
- **4 Mastery Levels**: Struggling, Challenging, Learning, Mastered
- **Response Time Tracking**: Millisecond-precision timing for performance analysis

#### 🎯 Problem Word Identification
- **Smart Filtering**: Automatically identifies words needing attention based on failure frequency and recency
- **Adaptive Thresholds**: Dynamic difficulty assessment based on user performance patterns
- **Cross-Mode Analysis**: Tracks performance across different learning activities for comprehensive insights
- **Session-Based Tracking**: Maintains learning context within practice sessions

#### 📈 Analytics Dashboard (ProblemWordsActivity)
- **Filter Options**: All Words, Struggling, Challenging, Learning, Mastered
- **Sort Options**: Failure Count, Last Failed, Learning Mode, Mastery Level
- **Detailed Statistics**: Total failures, recent performance, improvement trends
- **Action Buttons**: Practice specific words, view detailed analytics, mark as mastered

#### 🎯 Targeted Practice (ProblemWordsPracticeActivity)
- **Focused Sessions**: Practice only the most challenging vocabulary
- **Immediate Feedback**: Real-time success/failure tracking with visual indicators
- **Progress Tracking**: Session score and completion percentage
- **Adaptive Difficulty**: Automatically adjusts based on performance

### Implementation Details

#### Core Classes
```kotlin
// Analytics Engine
FailureTracker.kt                    // Core tracking logic and data structures
├── recordFailure()                  // Log failure with detailed context
├── recordSuccess()                  // Track successful responses
├── getProblemWords()               // Smart filtering of difficult words
├── getWordsNeedingAttention()      // Priority-based word selection
└── getStatistics()                 // Comprehensive analytics data

// User Interface
ProblemWordsActivity.kt             // Analytics dashboard and word management
├── displayProblemWords()           // Render word cards with statistics
├── filterAndSort()                 // Dynamic filtering and sorting
└── launchPracticeSession()         // Start targeted practice

ProblemWordsPracticeActivity.kt     // Focused practice sessions
├── loadProblemWords()              // Load challenging vocabulary
├── checkAnswer()                   // Validate responses with failure tracking
└── updateProgress()                // Real-time session tracking
```

#### Data Structures
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
    val responseTime: Long          // Time taken to respond
)

data class DifficultyWord(
    val phrase: Phrase,             // Core phrase data
    val failureCount: Int,          // Total failures
    val lastFailureTime: Long,      // Most recent failure
    val learningMode: String,       // Primary learning context
    val masteryLevel: String        // Current mastery assessment
)
```

#### Integration Across Learning Modes
- **FlashCardActivity**: Type-in recall and self-assessment tracking
- **FillInTheBlankActivity**: Contextual comprehension and spelling analysis
- **ClozeTestActivity**: Multi-blank comprehension and word bank performance
- **MultipleResponseGameActivity**: Game mode performance across 5 different game types

### Usage Patterns

#### For Learners
1. **Use any learning mode** - failures are automatically tracked
2. **Access analytics** via "🎯 Practice Problem Words" button on main screen
3. **Review problem words** with detailed failure statistics and filtering options
4. **Practice targeted sessions** focusing only on challenging vocabulary
5. **Track improvement** through mastery level progression and reduced failure rates

#### For Developers
1. **Integrate FailureTracker** in new learning activities (see existing implementations)
2. **Record failures** with appropriate failure type and learning mode context
3. **Track response times** for performance analysis
4. **Update UI** based on analytics data for personalized learning experiences

### Data Persistence
- **Gson Serialization**: Efficient JSON storage of analytics data
- **SharedPreferences**: Reliable persistence across app sessions
- **Automatic Cleanup**: Old failure records automatically pruned to maintain performance
- **Privacy-First**: All analytics data stored locally on device

### Dependencies for Future Enhancement
The `text-notes/chatgpt-notes.md` file contains comprehensive suggestions for additional Android libraries that could enhance the app, including:
- **Room Database** for better data management
- **ViewModel + LiveData** for reactive UI
- **ViewPager2** for improved card swiping
- **Lottie** for animations
- **DataStore** for modern preferences storage

Refer to `text-notes/future-considerations.md` for a phased implementation roadmap.