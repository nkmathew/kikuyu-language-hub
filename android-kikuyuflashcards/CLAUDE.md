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
│   │   │   ├── *.kt               # Core Kotlin classes
│   │   │   └── MainActivityWithBottomNav.kt    # Main Activity with bottom navigation
│   │   ├── res/                   # Resources (layouts, colors, etc.)
│   │   ├── assets/                # Data files (curated JSON content)
│   │   └── AndroidManifest.xml    # App configuration
│   ├── build.gradle               # App-level build configuration
│   └── debug.keystore            # Debug signing key
├── gradle/
│   └── wrapper/                   # Gradle wrapper files
├── text-notes/                   # Documentation and notes
├── build.gradle                  # Project-level build configuration
├── settings.gradle               # Project settings
└── gradle.properties             # Gradle configuration
```

### Core Components (Current Architecture)
#### Main Activity & Navigation
- **MainActivityWithBottomNav.kt** - Main activity with bottom navigation and consolidated UI
  - Vertical Quick Actions list (no horizontal scrolling)
  - Streamlined "Your Progress" card (category totals removed)
  - Learning mode cards with bottom progress bars and 50% markers
  - Bottom navigation with 5 tabs: Home, Learn, Flagged, Stats, Settings

#### Learning Activities
- **FlashCardStyleActivity.kt** - Flip-style flashcard learning
- **StudyListActivity.kt** - Side-by-side learning mode
- **QuizActivity.kt** - Multiple choice quiz with progress tracking
- **FillInTheBlankActivity.kt** - Complete the sentences exercise
- **SentenceUnscrambleActivity.kt** - Drag words to correct order
- **VowelHuntActivity.kt** - Find correct vowels in words
- **FlaggedTranslationsActivity.kt** - Review flagged translations

#### Data Management & Analytics
- **FlashCardManagerV2.kt** - Data management and phrase loading
- **ProgressManager.kt** - Progress tracking and statistics
- **ActivityProgressTracker.kt** - Per-activity progress monitoring
- **QuizStateManager.kt** - Quiz session state management
- **SoundManager.kt** - Audio feedback system

#### Data Models
- **Phrase.kt** - English-Kikuyu phrase data structure
- **Categories.kt** - Content categorization

#### Persistent Storage
- **JSON Content Files** - 196 curated files in `assets/curated-content/`
- **SharedPreferences** - Settings and progress storage

## Development Commands

### Build Commands (Updated for Kotlin)
```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build artifacts (recommended before major changes)
./gradlew clean

# Build and install on connected device
./gradlew installDebug

# Build with verbose logging (for troubleshooting build issues)
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

# View logs for main activity and progress tracking
adb logcat -s MainActivityWithBottomNav ProgressManager ActivityProgressTracker

# View logs for learning activities
adb logcat -s FlashCardStyleActivity StudyListActivity QuizActivity
```

### Lint and Code Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

## Coding Standards & Conventions

### Kotlin Coding Style
- **Class Names**: PascalCase (e.g., `MainActivityWithBottomNav`, `FlashCardManagerV2`)
- **Method Names**: camelCase (e.g., `getCurrentPhrase`, `navigateToNextPhrase`)
- **Variable Names**: camelCase (e.g., `currentIndex`, `flashCardManager`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `TAG`, `PREFS_NAME`)
- **Extension Functions**: camelCase with descriptive names (e.g., `loadPhraseData()`, `calculateProgress()`)

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
```kotlin
companion object {
    private const val TAG = "ClassName"
}

Log.d(TAG, "Method: Description of action")
Log.e(TAG, "Method: Error description", exception)
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
- **Language**: Kotlin
- **UI Framework**: Android Views with Material Design components

### Key Dependencies
```gradle
// Core Android Components
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Data Storage & JSON
implementation 'com.google.code.gson:gson:2.10.1'

// Testing
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
```

### Environment Files
- **gradle.properties** - Project-wide Gradle settings
- **local.properties** - Local SDK path configuration
- **debug.keystore** - Debug signing configuration

## Common Tasks & Patterns

### Adding New Content
1. Edit or add JSON files in `app/src/main/assets/curated-content/`
2. Follow existing naming convention: `{category}_kikuyu_batch_{number}_{category}.json`
3. Rebuild and test the app
4. Content is automatically available in learning modes

### Adding New Learning Modes
1. Create new Activity in `com.nkmathew.kikuyuflashcards` package
2. Integrate ProgressManager for tracking (see existing activities as examples)
3. Add navigation entry in MainActivityWithBottomNav learning modes list
4. Add activity to ActivityProgressTracker for progress monitoring
5. Test with progress tracking to ensure proper analytics capture

### Progress Tracking Integration
- **All learning activities** automatically record progress and statistics
- **Per-activity progress** tracked with completion percentages
- **Session management** with start/end time tracking
- **Progress persistence** across app sessions
- **Bottom navigation** provides access to detailed stats

### Data Management
- Content loaded from curated JSON files on app startup
- **Progress data** persisted using SharedPreferences
- **Activity state** maintained across sessions
- **Real-time progress** tracking during learning sessions
- Position and progress automatically saved on pause/destroy

### Statistics Access
- **Stats Tab**: View comprehensive learning analytics
- **Home Screen**: Quick progress overview with key metrics
- **Learning Mode Cards**: Show individual activity progress with visual indicators
- **Bottom Progress Bars**: Visual progress representation with 50% markers

## File Access Guidelines

### Safe to Modify
- **Source Files**: All files in `app/src/main/java/com/nkmathew/kikuyuflashcards/`
- **Resources**: All files in `app/src/main/res/` (layouts, colors, strings)
- **Content Files**: All files in `app/src/main/assets/curated-content/`
- **Build Configuration**: `app/build.gradle`
- **Progress Storage**: Automatic via ProgressManager (uses SharedPreferences internally)

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
- **Main Activity**: `app/src/main/java/com/nkmathew/kikuyuflashcards/MainActivityWithBottomNav.kt`
- **Content Files**: `app/src/main/assets/curated-content/` (196 JSON files)
- **Resources**: `app/src/main/res/` (layouts, colors, strings, etc.)
- **App Configuration**: `app/src/main/AndroidManifest.xml`
- **Progress Managers**: `app/src/main/java/com/nkmathew/kikuyuflashcards/ProgressManager.kt`, `ActivityProgressTracker.kt`

### Useful Debugging Commands
```bash
# Install and start app
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nkmathew.kikuyuflashcards/.MainActivityWithBottomNav

# Clear app data
adb shell pm clear com.nkmathew.kikuyuflashcards

# View current activity
adb shell dumpsys activity activities
```

### Development Notes
- **Current Content**: 196 curated JSON files with vocabulary, grammar, conjugations, and phrases
- **Learning Features**: 7 learning modes with comprehensive progress tracking
- **UI Architecture**: Bottom navigation with 5 tabs (Home, Learn, Flagged, Stats, Settings)
- **Progress System**: Real-time progress tracking with visual indicators and bottom progress bars
- **Content Structure**: Categorized content with batch files for organized learning
- **State Persistence**: All progress and settings automatically saved
- **Audio System**: SoundManager for button click feedback
- **Theme Support**: Dark theme with consistent Material Design styling
- **Performance**: Efficient content loading with optimized memory usage

## 🎯 Current UI Features & Implementation

### Recent UI Improvements (Latest Update)
The app has been enhanced with significant UI improvements based on user feedback:

#### ✅ Vertical Quick Actions
- **Before**: Horizontal scrolling list requiring user interaction
- **After**: Vertical list layout displaying all actions immediately
- **Implementation**: `LinearLayout.VERTICAL` orientation in `MainActivityWithBottomNav.kt:496-503`
- **Benefit**: Improved accessibility and reduced user friction

#### ✅ Streamlined Progress Card
- **Before**: Category totals section taking significant vertical space
- **After**: Focused key metrics (words learned, quiz answers, accuracy, current streak)
- **Implementation**: Simplified `createStatsCard()` method in `MainActivityWithBottomNav.kt:455-460`
- **Benefit**: Better use of screen real estate with essential information only

#### ✅ Enhanced Learning Mode Cards
- **Before**: Bright gradient backgrounds with poor text readability
- **After**: Solid backgrounds with high contrast text and bottom progress indicators
- **Implementation**: Replaced gradient backgrounds with solid colors in `createLearningModeCard()` method
- **Benefit**: Improved readability and professional appearance

#### ✅ Bottom Progress Bars with 50% Markers
- **Before**: No visual progress indicators in learning mode cards
- **After**: Horizontal progress bars at bottom of each card with white 50% marker line
- **Implementation**: Complex layout system in `MainActivityWithBottomNav.kt:772-860`
- **Benefit**: Clear visual representation of learning progress with halfway milestone

### UI Architecture Overview
- **Navigation**: 5-tab bottom navigation (Home, Learn, Flagged, Stats, Settings)
- **Layout System**: Android Views with Material Design components
- **Theme**: Dark theme with consistent color scheme and typography
- **Progress Visualization**: Bottom progress bars with percentage indicators and milestone markers
- **Responsive Design**: Adaptive layouts for different screen sizes and orientations

### Key UI Components
- **MainActivityWithBottomNav**: Main container with bottom navigation and consolidated home screen
- **Learning Mode Cards**: Interactive cards with progress indicators and navigation to activities
- **Quick Actions Section**: Vertical list of resumable activities with progress indicators
- **Stats Cards**: Simplified progress overview with key learning metrics
- **Bottom Navigation**: Consistent navigation across all app sections

### Design Principles
- **Material Design 3**: Modern design system with consistent components and interactions
- **Accessibility**: High contrast text, clear visual hierarchy, and intuitive navigation
- **Performance**: Efficient view rendering and smooth animations
- **User Experience**: Minimal friction for accessing learning content and tracking progress