# Implementation Plan for Kikuyu Flashcards App Enhancements

## 1. Update Learning Modes in Navigation Menu

- [x] Remove redundant "Flash Cards" (1st entry) from learning modes list
- [x] Remove redundant "Enhanced Cards" (2nd entry) from learning modes list
- [x] Keep "Flash Card Style" as the main flashcards experience
- [x] Remove corresponding methods: `startFlashCards()` and `startEnhancedFlashCards()`
- [x] Update click handlers to reflect these changes

## 2. Enhance Flash Card Style with Category Selection

- [x] Modify FlashCardStyleActivity.kt to include category selection UI
- [x] Show number of cards in each category
- [x] Allow category selection to filter cards
- [x] Store selected category in SharedPreferences
- [x] Update activity title to show selected category name

## 3. Add Progress Visualization to Learning Modes List

- [x] Implement gradient background progress indicators in `createLearningModeCard()`
- [x] Create method to calculate progress percentage for each learning mode
- [x] Design gradient backgrounds that fill based on progress (transparent to color)
- [x] Add progress percentage to description text

## 4. Make All Activities Resumable from Home Screen

- [x] Create comprehensive "Quick Actions" section with multiple activity buttons
- [x] Implement horizontally scrollable view for resumable activities
- [x] Create unified ActivityProgressTracker class for standardized progress tracking
- [x] Implement per-activity progress tracking:
  - [x] Flash Card Style Activity
  - [x] Fill In The Blank Activity
  - [x] Sentence Unscramble Activity
  - [x] Vowel Hunt Activity
  - [x] Quiz Activity

## 5. Home Screen Layout Updates

- [x] Enhance Quick Actions section with activity-specific resume buttons
- [x] Show "Continue from where you left off" message for each activity
- [x] Add visual progress indicators throughout the UI
- [x] Improve spacing and visual hierarchy for better user experience
- [x] Add new Learning Progress section with overall progress bar