# React Native CLI Migration Plan

## Overview

Plan for migrating the Kikuyu Flashcards mobile app from Expo managed workflow to React Native CLI for more control over native code and better performance.

## Current State

- **Framework**: Expo SDK 54
- **React Native**: 0.81.4
- **Build System**: EAS Build
- **Navigation**: React Navigation 7
- **Data Loading**: Explicit require() for 196 JSON files
- **Storage**: AsyncStorage
- **Package**: com.kikuyulanguagehub.flashcards

## Migration Goals

1. **Full native control** - Access to native Android/iOS code
2. **Reduced bundle size** - Remove Expo overhead
3. **Better performance** - Native build optimization
4. **Custom native modules** - If needed for future features
5. **Faster development builds** - Direct device deployment

## Migration Strategy

### Phase 1: Assessment & Preparation

#### 1.1 Audit Dependencies
- [ ] List all Expo-specific dependencies
- [ ] Identify React Native CLI alternatives
- [ ] Check for packages requiring native linking
- [ ] Document breaking changes

**Current Expo Dependencies:**
```json
"expo": "~54.0.12"
"expo-status-bar": "~3.0.8"
"@react-native-async-storage/async-storage": "2.2.0" ✅ (already RN CLI compatible)
```

#### 1.2 Backup Current State
- [ ] Create git branch: `migration/expo-to-rn-cli`
- [ ] Tag current working version: `v1.0.0-expo`
- [ ] Export current EAS build configuration
- [ ] Document all current features that work

#### 1.3 Development Environment Setup
- [ ] Install Android Studio with SDK
- [ ] Install Xcode (for iOS, Mac only)
- [ ] Install React Native CLI: `npm install -g react-native-cli`
- [ ] Configure Java JDK 17+
- [ ] Set up Android emulator or connect physical device
- [ ] Configure environment variables (ANDROID_HOME, etc.)

### Phase 2: Create New React Native CLI Project

#### 2.1 Initialize New Project
```bash
# Create new RN CLI project
npx react-native@latest init KikuyuFlashcardsCLI --version 0.81.4

# Or use latest stable version
npx react-native@latest init KikuyuFlashcardsCLI
```

#### 2.2 Configure Project Structure
- [ ] Set package name: `com.kikuyulanguagehub.flashcards`
- [ ] Configure app name: "Kikuyu Flashcards"
- [ ] Set up directory structure matching current app
- [ ] Copy assets (icon, splash, data files)

#### 2.3 Configure Build Systems

**Android (build.gradle):**
```gradle
android {
    compileSdkVersion 34
    defaultConfig {
        applicationId "com.kikuyulanguagehub.flashcards"
        minSdkVersion 23
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
    }
}
```

**iOS (Info.plist):**
```xml
<key>CFBundleDisplayName</key>
<string>Kikuyu Flashcards</string>
<key>CFBundleIdentifier</key>
<string>com.kikuyulanguagehub.flashcards</string>
```

### Phase 3: Migrate Code & Dependencies

#### 3.1 Install Core Dependencies
```bash
# Navigation
npm install @react-navigation/native @react-navigation/native-stack @react-navigation/bottom-tabs
npm install react-native-screens react-native-safe-area-context

# Storage
npm install @react-native-async-storage/async-storage

# Link native modules
cd android && ./gradlew clean
cd ios && pod install  # Mac only
```

#### 3.2 Migrate Source Code

**Files to migrate:**
- [ ] Copy `src/` directory entirely
- [ ] Copy `assets/` directory with all 196 JSON files
- [ ] Copy TypeScript types from `src/types/`
- [ ] Copy navigation setup from `src/navigation/`
- [ ] Copy all screens from `src/screens/`
- [ ] Copy data loader from `src/lib/dataLoader.ts`
- [ ] Copy storage utilities from `src/lib/storage.ts`

**Files to modify:**
- [ ] Remove expo-status-bar imports
- [ ] Replace with `react-native` StatusBar
- [ ] Update imports to use React Native CLI conventions
- [ ] Remove any Expo-specific APIs

#### 3.3 Replace Expo-Specific Features

| Expo Feature | React Native CLI Alternative |
|--------------|------------------------------|
| expo-status-bar | react-native StatusBar API |
| Expo Constants | react-native-device-info |
| Expo Asset | Direct require() (already using) |
| Expo Splash Screen | react-native-splash-screen |
| Expo Updates | CodePush or custom OTA solution |

#### 3.4 Configure TypeScript
- [ ] Copy `tsconfig.json` from Expo project
- [ ] Adjust paths if needed for RN CLI structure
- [ ] Install type definitions: `@types/react-native`

### Phase 4: Asset Migration

#### 4.1 Data Files (196 JSON files)
- [ ] Copy entire `src/assets/data/curated/` directory
- [ ] Verify dataLoader.ts works with new structure
- [ ] Test that all 196 files can be required
- [ ] Run data loading test on device

#### 4.2 Images & Icons
- [ ] Android: Copy to `android/app/src/main/res/`
  - mipmap-hdpi, mipmap-mdpi, mipmap-xhdpi, mipmap-xxhdpi, mipmap-xxxhdpi
- [ ] iOS: Copy to `ios/KikuyuFlashcards/Images.xcassets/`
- [ ] Generate all required icon sizes
- [ ] Set up splash screen for both platforms

#### 4.3 Fonts (if any custom fonts used)
- [ ] Android: Place in `android/app/src/main/assets/fonts/`
- [ ] iOS: Place in `ios/KikuyuFlashcards/` and link in Info.plist
- [ ] Update font references in code

### Phase 5: Native Configuration

#### 5.1 Android Setup

**AndroidManifest.xml:**
```xml
<manifest>
  <uses-permission android:name="android.permission.INTERNET" />
  <application
    android:name=".MainApplication"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:allowBackup="false"
    android:theme="@style/AppTheme">
    <!-- Activity configuration -->
  </application>
</manifest>
```

**ProGuard rules (android/app/proguard-rules.pro):**
```proguard
# Keep JSON data files
-keep class **.data.curated.** { *; }
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod *;
}
```

#### 5.2 iOS Setup (Mac only)

**Podfile configuration:**
```ruby
platform :ios, '13.0'
require_relative '../node_modules/react-native/scripts/react_native_pods'

target 'KikuyuFlashcards' do
  config = use_native_modules!
  use_react_native!(:path => config[:reactNativePath])
end
```

**Info.plist additions:**
```xml
<key>UIViewControllerBasedStatusBarAppearance</key>
<false/>
<key>UIRequiresFullScreen</key>
<true/>
```

### Phase 6: Testing & Validation

#### 6.1 Feature Testing Checklist
- [ ] App launches successfully
- [ ] All 196 JSON files load correctly
- [ ] Category screen shows correct counts
- [ ] Flashcard navigation works
- [ ] Flip animation works
- [ ] Flag/unflag functionality works
- [ ] Flagged translations screen shows items
- [ ] Data persists across app restarts (AsyncStorage)
- [ ] Back button navigation works on Android
- [ ] Tab navigation works on both platforms
- [ ] Dark mode/theme works (if implemented)

#### 6.2 Performance Testing
- [ ] Measure app startup time
- [ ] Check memory usage with all data loaded
- [ ] Test with 1000+ flashcards in single session
- [ ] Verify smooth animations (60fps)
- [ ] Check bundle size reduction vs Expo

#### 6.3 Device Testing
- [ ] Test on Android emulator
- [ ] Test on physical Android device (minimum API 23)
- [ ] Test on iOS simulator (Mac only)
- [ ] Test on physical iOS device (Mac only)
- [ ] Test on different screen sizes

### Phase 7: Build & Release Configuration

#### 7.1 Android Release Build

**Generate keystore:**
```bash
keytool -genkeypair -v -storetype PKCS12 -keystore kikuyu-flashcards.keystore \
  -alias kikuyu-flashcards -keyalg RSA -keysize 2048 -validity 10000
```

**Configure gradle.properties:**
```properties
KIKUYU_UPLOAD_STORE_FILE=kikuyu-flashcards.keystore
KIKUYU_UPLOAD_KEY_ALIAS=kikuyu-flashcards
KIKUYU_UPLOAD_STORE_PASSWORD=***
KIKUYU_UPLOAD_KEY_PASSWORD=***
```

**Update app/build.gradle:**
```gradle
android {
    signingConfigs {
        release {
            storeFile file(KIKUYU_UPLOAD_STORE_FILE)
            storePassword KIKUYU_UPLOAD_STORE_PASSWORD
            keyAlias KIKUYU_UPLOAD_KEY_ALIAS
            keyPassword KIKUYU_UPLOAD_KEY_PASSWORD
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**Build APK:**
```bash
cd android
./gradlew assembleRelease
# Output: android/app/build/outputs/apk/release/app-release.apk
```

**Build AAB (for Google Play):**
```bash
cd android
./gradlew bundleRelease
# Output: android/app/build/outputs/bundle/release/app-release.aab
```

#### 7.2 iOS Release Build (Mac only)

- [ ] Open project in Xcode
- [ ] Configure signing & capabilities
- [ ] Set up provisioning profile
- [ ] Archive build for distribution
- [ ] Upload to App Store Connect

### Phase 8: CI/CD Setup (Optional)

#### 8.1 GitHub Actions Workflow

```yaml
name: Build Android APK

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Install dependencies
        run: npm install
      - name: Build Android APK
        run: cd android && ./gradlew assembleRelease
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release.apk
          path: android/app/build/outputs/apk/release/app-release.apk
```

#### 8.2 Fastlane Setup (Advanced)

```ruby
# Fastfile
default_platform(:android)

platform :android do
  desc "Build release APK"
  lane :build do
    gradle(
      task: "assembleRelease",
      project_dir: "android"
    )
  end

  desc "Deploy to Play Store"
  lane :deploy do
    gradle(task: "bundleRelease")
    upload_to_play_store(
      track: 'internal',
      aab: 'android/app/build/outputs/bundle/release/app-release.aab'
    )
  end
end
```

### Phase 9: Documentation Updates

#### 9.1 Update Documentation
- [ ] Update README.md with RN CLI commands
- [ ] Update CLAUDE.md mobile app section
- [ ] Document new build process
- [ ] Create troubleshooting guide
- [ ] Document development setup requirements

#### 9.2 Update Scripts in package.json
```json
{
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start",
    "build:android:debug": "cd android && ./gradlew assembleDebug",
    "build:android:release": "cd android && ./gradlew assembleRelease",
    "build:android:bundle": "cd android && ./gradlew bundleRelease",
    "clean:android": "cd android && ./gradlew clean",
    "install:android": "react-native run-android --variant=release"
  }
}
```

## Timeline Estimate

| Phase | Duration | Complexity |
|-------|----------|-----------|
| Phase 1: Assessment | 1-2 days | Low |
| Phase 2: New Project Setup | 0.5 day | Low |
| Phase 3: Code Migration | 2-3 days | Medium |
| Phase 4: Asset Migration | 1 day | Low |
| Phase 5: Native Config | 1-2 days | Medium |
| Phase 6: Testing | 2-3 days | High |
| Phase 7: Build Setup | 1-2 days | Medium |
| Phase 8: CI/CD (Optional) | 1-2 days | Medium |
| Phase 9: Documentation | 1 day | Low |
| **Total** | **10-18 days** | - |

## Risks & Mitigation

### Risk 1: Navigation Issues
- **Risk**: React Navigation might behave differently
- **Mitigation**: Test thoroughly, keep navigation simple, use same versions

### Risk 2: Data Loading Failures
- **Risk**: 196 JSON files might not bundle correctly
- **Mitigation**: Test require() statements early, use metro bundler config

### Risk 3: Performance Regression
- **Risk**: App might be slower than Expo version
- **Mitigation**: Profile with Flipper, optimize bundle, use Hermes engine

### Risk 4: Native Build Errors
- **Risk**: Android/iOS builds might fail with cryptic errors
- **Mitigation**: Clean builds frequently, check native logs, use stable versions

### Risk 5: AsyncStorage Migration
- **Risk**: User data might not persist after migration
- **Mitigation**: Test storage thoroughly, provide data export/import feature

## Rollback Plan

If migration fails or has critical issues:

1. **Immediate**: Switch back to `main` branch (Expo version)
2. **Short-term**: Fix critical issues in RN CLI version
3. **Long-term**: Complete migration with fixes or stay on Expo

**Rollback triggers:**
- Data loss or corruption
- App crashes on launch
- Cannot build release APK
- Critical features broken
- Performance significantly worse

## Success Criteria

Migration is successful when:

- ✅ All 196 JSON files load correctly in production
- ✅ All features work identically to Expo version
- ✅ Release APK builds successfully
- ✅ App size reduced by at least 20%
- ✅ Startup time same or faster
- ✅ No user data loss
- ✅ Can deploy to Google Play Store
- ✅ Development workflow improved

## Post-Migration Benefits

1. **Control**: Direct access to native code
2. **Performance**: Smaller bundle, faster startup
3. **Flexibility**: Add any native module needed
4. **Build Speed**: Faster development builds
5. **Learning**: Better understanding of React Native internals
6. **Cost**: No EAS Build subscription needed

## Alternative: Expo Prebuild

Instead of full migration, consider **Expo Prebuild** (expo-dev-client):

**Pros:**
- Keep Expo developer experience
- Access native code when needed
- Easier migration path
- Keep EAS Build integration

**Cons:**
- Still has Expo overhead
- Not as lightweight as pure RN CLI
- Less control than full RN CLI

**Command:**
```bash
npx expo prebuild
# Generates android/ and ios/ directories
# Can customize native code
# Still use Expo APIs
```

## Recommended Approach

### Option A: Full React Native CLI Migration
**Best for:**
- Maximum control and customization
- Minimal bundle size critical
- Team has native development experience
- Long-term project with complex native requirements

### Option B: Expo Prebuild (Hybrid)
**Best for:**
- Want some native control but keep Expo DX
- Need occasional native module customization
- Prefer EAS Build convenience
- Smaller team without deep native expertise

### Option C: Stay on Expo Managed
**Best for:**
- Current setup works perfectly
- No need for custom native code
- Rapid development priority
- Prefer managed updates and builds

## Recommendation for This Project

**Start with Option B (Expo Prebuild)** because:

1. Data loading already works with explicit requires
2. No custom native modules needed yet
3. Keeps EAS Build integration
4. Easier rollback if issues occur
5. Can migrate to full RN CLI later if needed

**Migration to full RN CLI** if:
- Need significant bundle size reduction
- Want to eliminate EAS Build costs
- Plan to add complex native features
- Have Android Studio/Xcode expertise

## Next Steps

1. **Decision**: Choose migration approach (A, B, or C)
2. **Timeline**: Schedule migration window
3. **Backup**: Create git branch and tag current version
4. **Setup**: Prepare development environment
5. **Execute**: Follow phase-by-phase plan
6. **Test**: Comprehensive testing on devices
7. **Deploy**: Release new version
8. **Monitor**: Watch for user issues post-migration

## Questions to Answer Before Migration

- [ ] Is current Expo setup causing problems?
- [ ] Do we need custom native modules?
- [ ] Is bundle size a critical concern?
- [ ] Do we have Android Studio/Xcode expertise?
- [ ] Is EAS Build cost a concern?
- [ ] How much time can we allocate?
- [ ] What's the risk tolerance for user disruption?

## Resources

- React Native CLI Docs: https://reactnative.dev/docs/environment-setup
- Expo to React Native CLI: https://docs.expo.dev/bare/hello-world/
- React Navigation: https://reactnavigation.org/docs/getting-started
- Android Build Guide: https://reactnative.dev/docs/signed-apk-android
- iOS Build Guide: https://reactnative.dev/docs/publishing-to-app-store
