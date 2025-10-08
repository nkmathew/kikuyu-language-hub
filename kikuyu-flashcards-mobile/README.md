# Kikuyu Flashcards Mobile - Proof of Concept

React Native (Expo) conversion of the Next.js flashcards app.

## Features

✅ **Implemented in POC:**
- Home screen with category selection
- Category screen with difficulty filtering
- Flashcard study screen with flip animations
- Progress tracking placeholder
- Bottom tab navigation
- Bundled JSON data (3 sample batches)
- TypeScript support
- Cross-platform (iOS & Android)

🚧 **To Be Implemented:**
- AsyncStorage for progress tracking
- Spaced repetition algorithm
- Search functionality
- All flashcard batches (currently only 3 sample batches)
- Audio pronunciation
- Dark mode
- Study statistics
- Offline sync
- Push notifications for reminders

## Project Structure

```
kikuyu-flashcards-mobile/
├── src/
│   ├── screens/           # App screens
│   │   ├── HomeScreen.tsx
│   │   ├── CategoryScreen.tsx
│   │   ├── FlashcardScreen.tsx
│   │   └── ProgressScreen.tsx
│   ├── navigation/        # Navigation setup
│   │   └── AppNavigator.tsx
│   ├── lib/              # Business logic
│   │   └── dataLoader.ts
│   ├── types/            # TypeScript types
│   │   └── flashcard.ts
│   └── assets/           # Bundled data
│       └── data/
│           └── curated/
│               ├── vocabulary/
│               ├── phrases/
│               └── grammar/
├── App.tsx               # Entry point
└── package.json
```

## Setup & Run

### Prerequisites
- Node.js 18+
- npm or yarn
- Expo Go app (for testing on device)

### Installation

```bash
cd kikuyu-flashcards-mobile
npm install
```

### Run Development Server

```bash
npx expo start
```

Then:
- Press `i` for iOS simulator
- Press `a` for Android emulator
- Scan QR code with Expo Go app on physical device

## Key Differences from Next.js Version

### 1. Data Loading
**Next.js (Web):**
```typescript
const response = await fetch(`/data/curated/${filePath}`);
const data = await response.json();
```

**React Native:**
```typescript
import batch001 from '../assets/data/curated/vocabulary/batch_001.json';
// Data is bundled with app
```

### 2. Navigation
**Next.js:** App Router with file-based routing
**React Native:** React Navigation with programmatic routing

### 3. UI Components
**Next.js:** HTML elements + Tailwind CSS
**React Native:** React Native components + StyleSheet

### 4. Storage
**Next.js:** localStorage/sessionStorage
**React Native:** AsyncStorage (to be implemented)

## Sample Data Included

- **Vocabulary:** Batch 001 (5 cards)
- **Phrases:** Batch 002 (2 cards)
- **Grammar:** Batch 002 (2 cards)

Total: **9 flashcards** bundled for POC demonstration

## Converting Full Dataset

To bundle all 307+ flashcards:

1. Copy all JSON files from `flashcards-app/public/data/curated/` to `src/assets/data/curated/`
2. Import them in `src/lib/dataLoader.ts`
3. Add to the `bundledData` object

**Note:** This will increase app size by ~2-3MB. For production, consider:
- Lazy loading categories
- Fetching from API with caching
- Hybrid approach (bundle core + fetch updates)

## Tech Stack

- **React Native:** 0.76.5
- **Expo SDK:** ~54.0.0
- **React Navigation:** ^7.0.14
- **TypeScript:** ~5.3.3
- **AsyncStorage:** ^2.1.0 (installed but not yet used)

## Next Steps for Full Conversion

### Phase 1: Core Features (1-2 weeks)
- [ ] Implement AsyncStorage for progress tracking
- [ ] Add all flashcard batches
- [ ] Implement search functionality
- [ ] Add spaced repetition algorithm

### Phase 2: Enhanced UX (1-2 weeks)
- [ ] Add audio pronunciation (expo-speech)
- [ ] Implement dark mode
- [ ] Add study statistics
- [ ] Swipe gestures for cards
- [ ] Custom fonts for Kikuyu characters

### Phase 3: Advanced Features (2-3 weeks)
- [ ] Offline sync with backend API
- [ ] Push notifications
- [ ] Achievements/gamification
- [ ] Social features (share progress)
- [ ] Export study data

### Phase 4: Production (1-2 weeks)
- [ ] Testing on iOS and Android
- [ ] Performance optimization
- [ ] App store assets (icons, screenshots)
- [ ] Privacy policy & terms
- [ ] Submit to App Store and Google Play

## Comparison: Web vs Mobile

| Feature | Next.js Web | React Native Mobile |
|---------|-------------|---------------------|
| **Offline** | Limited (PWA) | Full offline support |
| **Performance** | Server-rendered | Native performance |
| **Distribution** | URL/PWA | App stores |
| **Updates** | Instant | App update required* |
| **Device APIs** | Limited | Full access |
| **Development** | Web tools | Mobile simulators |
| **Bundle Size** | ~500KB initial | ~15-20MB total |

*With Expo OTA updates, can push updates without app store review

## POC Demonstration

1. **Home Screen:** Browse 5 categories
2. **Category Screen:** Select difficulty levels
3. **Study Screen:**
   - Flip cards with tap
   - Navigate with Previous/Next
   - Progress bar tracks position
4. **Progress Screen:** Placeholder for future features

## License

Same as parent project (Kikuyu Language Hub)
