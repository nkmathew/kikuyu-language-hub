# Expo to React Native CLI Migration - Execution Plan

Complete removal of Expo and migration to pure React Native CLI for the Kikuyu Flashcards mobile app.

## Current State

**Framework**: Expo SDK 54 with prebuild
**Dependencies**: 2 Expo packages (`expo`, `expo-status-bar`)
**Native Code**: Already have `android/` from Expo prebuild
**Data**: 196 JSON files in `src/assets/data/curated/`
**Features**: Navigation, AsyncStorage, Flashcards, Flagged Translations

## Migration Strategy

### Option A: In-Place Migration (Recommended)
Keep existing project structure, remove Expo dependencies, update configurations.

**Pros:**
- Faster migration (1-2 hours)
- Keep git history
- Less risk of breaking changes
- Android directory already configured

**Cons:**
- May have leftover Expo configuration
- Need to manually update Metro config

### Option B: Fresh React Native CLI Project
Create new RN CLI project, copy src code over.

**Pros:**
- Clean slate
- Latest RN CLI defaults
- No Expo artifacts

**Cons:**
- Longer migration (3-4 hours)
- Lose git history unless careful
- Need to reconfigure everything

## Recommended: Option A (In-Place Migration)

## Step-by-Step Execution

### Step 1: Remove Expo Dependencies

**Remove from package.json:**
```bash
npm uninstall expo expo-status-bar
```

**Add React Native equivalents:**
```bash
npm install react-native@0.81.4
```

### Step 2: Update Scripts

**Before:**
```json
{
  "scripts": {
    "start": "expo start",
    "android": "expo run:android",
    "ios": "expo run:ios"
  }
}
```

**After:**
```json
{
  "scripts": {
    "start": "react-native start",
    "android": "react-native run-android",
    "ios": "react-native run-ios"
  }
}
```

### Step 3: Replace Expo Status Bar

**Find all instances:**
```bash
grep -r "expo-status-bar" src/
```

**Replace:**
```typescript
// Before
import { StatusBar } from 'expo-status-bar';
<StatusBar style="auto" />

// After
import { StatusBar } from 'react-native';
<StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
```

### Step 4: Update Entry Point

**Current**: Uses Expo entry point via `app.json`

**Create** `index.js` (React Native standard):
```javascript
import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
```

**Update** `app.json`:
```json
{
  "name": "KikuyuFlashcards",
  "displayName": "Kikuyu Flashcards"
}
```

### Step 5: Update Android Configuration

**android/settings.gradle** - Remove Expo autolinking:
```gradle
// Remove these lines
apply from: new File(["node", "--print", "require.resolve('expo/package.json')"].execute(null, rootDir).text.trim(), "../scripts/autolinking.gradle")
useExpoModules()

// Keep only:
rootProject.name = 'Kikuyu Flashcards'
apply from: file("../node_modules/@react-native-community/cli-platform-android/native_modules.gradle")
applyNativeModulesSettingsGradle(settings)
include ':app'
```

**android/build.gradle** - Remove Expo plugins:
```gradle
// Remove expo-gradle-plugin references
// Keep only React Native defaults
```

**android/app/build.gradle** - Update entry file:
```gradle
project.ext.react = [
    entryFile: "index.js",  // Change from Expo's dynamic resolution
    enableHermes: true
]
```

### Step 6: Update Metro Config

**Create** `metro.config.js`:
```javascript
const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');

const config = {
  resolver: {
    assetExts: ['json', 'png', 'jpg', 'jpeg', 'svg'],
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
```

### Step 7: Clean Build Artifacts

```bash
cd android
./gradlew clean
cd ..
rm -rf node_modules
npm install
```

### Step 8: Update iOS Configuration (if needed)

**ios/Podfile** - Remove Expo autolinking:
```ruby
# Remove
use_expo_modules!

# Keep
use_react_native!(
  :path => config[:reactNativePath],
  :hermes_enabled => true
)
```

## File Changes Checklist

### Files to Modify

- [ ] `package.json` - Remove Expo deps, update scripts
- [ ] `index.js` - Create React Native entry point (currently `index.ts`)
- [ ] `App.tsx` - Remove Expo StatusBar
- [ ] `android/settings.gradle` - Remove Expo autolinking
- [ ] `android/build.gradle` - Remove Expo plugins
- [ ] `android/app/build.gradle` - Update entry file
- [ ] `metro.config.js` - Create with JSON asset support
- [ ] All screens using `expo-status-bar` - Replace with RN StatusBar

### Files to Delete

- [ ] `app.json` expo-specific config (keep minimal name/displayName)
- [ ] Any `.expo/` directories
- [ ] `eas.json` (already removed)

### Files to Keep

- ✅ `src/` - All source code (no changes needed)
- ✅ `src/assets/data/curated/` - All 196 JSON files
- ✅ `android/` - Native Android code (with modifications)
- ✅ `android/app/kikuyu-flashcards-release.keystore` - Release keystore

## Dependencies Comparison

### Before (Expo)
```json
{
  "expo": "~54.0.12",
  "expo-status-bar": "~3.0.8",
  "react-native": "0.81.4"
}
```

### After (Pure RN CLI)
```json
{
  "react-native": "0.81.4"
}
```

**Result**: Only core React Native, no Expo!

## Testing Checklist

After migration, test:

- [ ] App launches successfully
- [ ] Metro bundler starts
- [ ] All 196 JSON files load
- [ ] Category screen shows counts
- [ ] Flashcard navigation works
- [ ] Flip animations work
- [ ] Flag/unflag functionality works
- [ ] AsyncStorage persists data
- [ ] Status bar displays correctly
- [ ] Back button works on Android
- [ ] Tab navigation works
- [ ] APK builds successfully
- [ ] APK installs and runs on device

## Build Commands (Post-Migration)

```bash
# Development
npm start                           # Start Metro
npm run android                     # Run on Android device

# Production
npm run build:android:release       # Build release APK
npm run clean:android               # Clean build
```

## Rollback Plan

If migration fails:

```bash
git checkout migration/expo-prebuild
npm install
```

## Expected Issues & Solutions

### Issue 1: Metro Can't Resolve Modules

**Error**: `Unable to resolve module`

**Solution**: Clear cache and reinstall
```bash
rm -rf node_modules
npm install
npm start -- --reset-cache
```

### Issue 2: Android Build Fails

**Error**: `Could not determine the dependencies of task ':app:compileReleaseJavaWithJavac'`

**Solution**: Clean and rebuild
```bash
cd android
./gradlew clean
./gradlew assembleRelease
```

### Issue 3: App Crashes on Launch

**Error**: `Application has stopped`

**Solution**: Check entry point registration in `index.js`

### Issue 4: JSON Files Not Loading

**Error**: `Cannot read property 'flashcards' of undefined`

**Solution**: Update Metro config to include JSON in asset extensions

## Timeline

- **Step 1-2**: Remove Expo deps, update scripts (10 min)
- **Step 3**: Replace StatusBar imports (15 min)
- **Step 4**: Create entry point (10 min)
- **Step 5**: Update Android config (30 min)
- **Step 6**: Metro config (10 min)
- **Step 7**: Clean and rebuild (20 min)
- **Step 8**: Testing (30 min)

**Total**: ~2 hours

## Benefits After Migration

✅ **No Expo Overhead** - Smaller bundle size (~20-30% reduction)
✅ **Faster Startup** - No Expo initialization layer
✅ **Full Native Control** - Direct React Native access
✅ **Simpler Dependencies** - Only RN core
✅ **Standard RN Workflow** - Industry standard tooling
✅ **Better Performance** - Less abstraction layers

## Final Verification

After migration completes:

1. **Size Comparison**:
```bash
# Before (with Expo)
ls -lh android/app/build/outputs/apk/release/app-release.apk

# After (pure RN CLI)
# Should be 20-30% smaller
```

2. **Startup Time**: Measure with `adb logcat | grep "Displayed"`

3. **Bundle Size**: Check Metro output for module count

## Next Steps After Migration

1. Build and test release APK
2. Update all documentation
3. Remove any remaining Expo references
4. Commit changes
5. Merge to main
6. Celebrate! 🎉

## Command Summary

```bash
# Execute migration
npm uninstall expo expo-status-bar
npm install
# ... make file changes ...
cd android && ./gradlew clean && cd ..
npm start

# Test build
npm run build:android:release

# Result: Pure React Native CLI app with no Expo!
```
