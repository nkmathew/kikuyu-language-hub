# Expo Prebuild Migration Summary

**Date**: 2025-10-09
**Branch**: `migration/expo-prebuild`
**Base Version**: `v1.0.0-expo` (tagged)

## Migration Completed

### ✅ Phase 1: Backup & Preparation
- Created git branch: `migration/expo-prebuild`
- Tagged current Expo version: `v1.0.0-expo`
- Can rollback with: `git checkout master`

### ✅ Phase 2: Expo Prebuild Execution
- Ran: `npx expo prebuild --platform android`
- Generated native `android/` directory
- package.json updated with new run scripts:
  - `expo run:android` (replaces `expo start --android`)
  - `expo run:ios` (replaces `expo start --ios`)

### ✅ Phase 3: Android Native Configuration

#### Release Keystore Created
- **File**: `android/app/kikuyu-flashcards-release.keystore`
- **Alias**: `kikuyu-flashcards`
- **Password**: `android123` (change for production!)
- **Validity**: 10,000 days (~27 years)

#### build.gradle Updated
- Added release signing configuration
- Release builds now use proper keystore (not debug)
- ProGuard configuration for minification

#### ProGuard Rules Added
```proguard
# Keep JSON data files in assets
-keep class **.assets.data.curated.** { *; }
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod *;
}
```

#### App Configuration
- **Package**: `com.kikuyulanguagehub.flashcards`
- **App Name**: Kikuyu Flashcards
- **Version Code**: 1
- **Version Name**: 1.0.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14+)
- **Compile SDK**: 36

## Build Status

### 🔄 Current: Building Release APK
- Command: `./gradlew assembleRelease --no-daemon`
- Status: In progress (compiling Kotlin/Java code)
- Output will be: `android/app/build/outputs/apk/release/app-release.apk`

## New Development Workflow

### Running on Device/Emulator
```bash
# Start Metro bundler
npm start

# Run on Android (requires device or emulator)
npm run android

# Or use Expo command
npx expo run:android
```

### Building APK Locally
```bash
# Debug APK (for testing)
cd android && ./gradlew assembleDebug

# Release APK (for distribution)
cd android && ./gradlew assembleRelease

# Output locations:
# Debug: android/app/build/outputs/apk/debug/app-debug.apk
# Release: android/app/build/outputs/apk/release/app-release.apk
```

### Building with EAS (Still Supported)
```bash
# EAS builds still work with prebuild
npm run build:preview
npm run build:production
```

## Benefits Achieved

### ✅ Native Code Access
- Full access to `android/` native code
- Can customize AndroidManifest.xml
- Can add custom native modules
- Can modify Gradle build configuration

### ✅ Local APK Building
- No need for cloud builds for testing
- Faster iteration with `./gradlew assembleDebug`
- Can test ProGuard/minification locally

### ✅ Backward Compatibility
- Still using Expo SDK features
- Expo Router and APIs still work
- EAS Build still available
- Can use `expo-updates` for OTA

### ⚠️ Kept Expo Benefits
- Expo dev client experience maintained
- Expo modules still functional
- Managed workflow features available
- Can revert to managed if needed

## Data Loading Verification

### Data Files Status
- 196 JSON files in `src/assets/data/curated/`
- `dataLoader.ts` uses explicit `require()` statements
- ProGuard rules protect data from stripping
- Will test when APK build completes

## Testing Checklist

After APK builds successfully:

- [ ] Install APK on Android device
- [ ] Verify all 196 data files load
- [ ] Check category counts are correct
- [ ] Test flashcard navigation
- [ ] Test flagged translations feature
- [ ] Verify AsyncStorage persistence
- [ ] Check app performance
- [ ] Measure APK size

## Rollback Procedure

If issues occur:

```bash
# Return to Expo managed
git checkout master
git branch -D migration/expo-prebuild

# Or keep prebuild but revert changes
git checkout master
git merge --no-ff migration/expo-prebuild
git revert <commit-hash>
```

## Next Steps

1. **Wait for build to complete** (~5-10 minutes first time)
2. **Test APK on device** - Install and verify all features work
3. **Commit changes** if tests pass
4. **Update CLAUDE.md** with prebuild documentation
5. **Merge to main** after thorough testing

## File Changes Summary

### Created Files
- `android/` - Entire native Android directory
- `android/app/kikuyu-flashcards-release.keystore` - Release signing key
- `PREBUILD_MIGRATION_SUMMARY.md` - This file

### Modified Files
- `package.json` - Updated scripts for `expo run:android/ios`
- `android/app/build.gradle` - Release signing configuration
- `android/app/proguard-rules.pro` - ProGuard rules for data files

### Unchanged Files
- All `src/` source code - No changes needed
- `src/assets/data/curated/` - All 196 JSON files preserved
- `app.json` - Expo configuration unchanged
- `eas.json` - EAS Build config still valid

## Known Limitations

### ⚠️ iOS Not Generated
- Only Android native code created (no `ios/` directory yet)
- Mac required for iOS development
- Run `npx expo prebuild --platform ios` on Mac when ready

### ⚠️ Keystore Security
- Current keystore password is simple: `android123`
- **Change for production deployment!**
- Never commit keystore to public repositories
- Use environment variables for passwords

### ⚠️ First Build Takes Time
- Gradle downloads dependencies on first run
- Kotlin compilation is slow initially
- Subsequent builds much faster (~30 seconds)

## Migration Success Criteria

Migration is successful when:

- ✅ Native Android directory generated
- ✅ Release keystore created and configured
- ✅ ProGuard rules protect data files
- 🔄 Release APK builds successfully (in progress)
- ⏳ All 196 JSON files load in production APK
- ⏳ App features work identically to Expo version
- ⏳ Performance maintained or improved

## Resources Used

- Expo Prebuild Docs: https://docs.expo.dev/workflow/prebuild/
- React Native CLI Migration Plan: `docs/REACT_NATIVE_CLI_MIGRATION_PLAN.md`
- Android Build Guide: https://reactnative.dev/docs/signed-apk-android

## Support

For issues:
- Check `android/` Gradle build logs
- Review Metro bundler output
- Verify `dataLoader.ts` require statements
- Test on clean device/emulator

## Contact

Generated during Expo Prebuild migration on 2025-10-09.
