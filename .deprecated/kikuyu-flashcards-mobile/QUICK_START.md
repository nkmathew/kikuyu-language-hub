# Quick Start Guide - Kikuyu Flashcards Mobile

## 🚀 Getting Started (5 Minutes)

### 1. Install Dependencies
```bash
cd kikuyu-flashcards-mobile
npm install
```

### 2. Start Development Server
```bash
npx expo start
```

You'll see a QR code in the terminal.

### 3. Run on Device/Simulator

#### Option A: Physical Device (Easiest)
1. Install **Expo Go** app from App Store or Google Play
2. Scan the QR code with your camera (iOS) or Expo Go app (Android)
3. App will load on your device!

#### Option B: iOS Simulator (Mac only)
```bash
# Press 'i' in the terminal or run:
npx expo start --ios
```

#### Option C: Android Emulator
```bash
# Press 'a' in the terminal or run:
npx expo start --android
```

## 📱 App Features

### Home Screen
- Browse 5 categories: Vocabulary, Phrases, Grammar, Conjugations, Proverbs
- See card counts for each category
- Tap any category to explore

### Category Screen
- View total cards available
- Select difficulty levels:
  - ✅ Beginner
  - ✅ Intermediate
  - ✅ Advanced
- Multiple selections allowed
- Start studying with selected difficulties

### Flashcard Screen
- **Tap to flip** between Kikuyu and English
- **Navigate** with Previous/Next buttons
- **Progress bar** shows position
- **Finish** button appears on last card

### Progress Screen
- Placeholder for future tracking features
- Will include:
  - Study statistics
  - Streak tracking
  - Spaced repetition
  - Performance analytics

## 🎯 Sample Data Included

The POC includes 9 flashcards across 3 batches:

### Vocabulary (Batch 001) - 5 cards
- Household items
- Animals
- Nature/home items
- Basic verbs
- Question patterns

### Phrases (Batch 002) - 2 cards
- Common greetings
- Simple questions about school

### Grammar (Batch 002) - 2 cards
- Possession structure
- Noun class connectives

**Total:** 9 flashcards for demonstration

## 🛠 Development Commands

```bash
# Start development server
npm start

# Run on iOS simulator (Mac only)
npm run ios

# Run on Android emulator
npm run android

# Run in web browser (limited functionality)
npm run web

# Clear cache and restart
npx expo start --clear
```

## 📂 Project Structure

```
kikuyu-flashcards-mobile/
├── src/
│   ├── screens/              # UI screens
│   │   ├── HomeScreen.tsx    # Category browser
│   │   ├── CategoryScreen.tsx    # Difficulty selector
│   │   ├── FlashcardScreen.tsx   # Study interface
│   │   └── ProgressScreen.tsx    # Progress tracker
│   ├── navigation/
│   │   └── AppNavigator.tsx      # Navigation config
│   ├── lib/
│   │   └── dataLoader.ts         # Data management
│   ├── types/
│   │   └── flashcard.ts          # TypeScript types
│   └── assets/
│       └── data/                 # Bundled flashcards
├── App.tsx                       # Entry point
├── package.json
└── README.md
```

## 🔧 Troubleshooting

### "Metro bundler not responding"
```bash
npx expo start --clear
```

### "Unable to resolve module"
```bash
rm -rf node_modules
npm install
npx expo start --clear
```

### "Network connection failed"
- Ensure phone and computer are on same WiFi
- Try using tunnel mode: `npx expo start --tunnel`

### TypeScript errors
```bash
# Restart TypeScript server in VS Code
# Cmd+Shift+P > "TypeScript: Restart TS Server"
```

### iOS simulator not opening
```bash
# Install iOS simulator via Xcode
# Or use: npx expo run:ios
```

### Android emulator not starting
```bash
# Ensure Android Studio is installed
# Open AVD Manager and start an emulator manually
```

## 📊 Performance Tips

### For Smooth Animations
- Enable JS Dev Mode toggle off in Expo Go
- Test on physical device (simulators can be slower)

### For Faster Reloads
- Use Fast Refresh (automatic)
- Shake device and select "Reload"

## 🎨 Customization

### Change Theme Colors
Edit `src/screens/*.tsx` and update StyleSheet colors:
```tsx
const styles = StyleSheet.create({
  primaryColor: '#2563eb',  // Change to your brand color
  // ...
});
```

### Add More Flashcards
1. Copy JSON files to `src/assets/data/curated/[category]/`
2. Import in `src/lib/dataLoader.ts`
3. Add to `bundledData` object

### Modify Navigation
Edit `src/navigation/AppNavigator.tsx` to add/remove screens

## 📱 Testing on Real Device

### iOS (requires Mac + Xcode)
```bash
# Build development client
eas build --profile development --platform ios

# Install on connected device
# Scan QR code from EAS build output
```

### Android
```bash
# Build APK
eas build --profile development --platform android

# Download APK and install on device
# Or use: adb install path/to/app.apk
```

## 🚢 Building for Production

### Prerequisites
1. Create Expo account: https://expo.dev
2. Install EAS CLI: `npm install -g eas-cli`
3. Login: `eas login`

### Build Commands
```bash
# iOS (requires Apple Developer account $99/year)
eas build --profile production --platform ios
eas submit --platform ios

# Android (requires Google Play account $25 one-time)
eas build --profile production --platform android
eas submit --platform android
```

## 📚 Next Steps

1. **Study some flashcards!** Get familiar with the app flow
2. **Read CONVERSION_NOTES.md** to understand the architecture
3. **Read README.md** for full feature roadmap
4. **Experiment with code** - make it your own!

## 🤝 Contributing

To add all 307+ flashcards from the web version:

```bash
# Copy all data files
cp -r ../flashcards-app/public/data/curated/* src/assets/data/curated/

# Update dataLoader.ts to import all batches
# (See CONVERSION_NOTES.md for details)
```

## 📞 Support

- **Expo Documentation:** https://docs.expo.dev
- **React Native Docs:** https://reactnative.dev
- **React Navigation:** https://reactnavigation.org

## ✅ Verification Checklist

After installation, verify:
- [ ] App starts without errors
- [ ] Home screen shows 5 categories
- [ ] Can navigate to Category screen
- [ ] Can select difficulty levels
- [ ] Flashcards load and display
- [ ] Tap to flip works
- [ ] Previous/Next navigation works
- [ ] Progress bar updates

If all checks pass, you're ready to develop! 🎉
