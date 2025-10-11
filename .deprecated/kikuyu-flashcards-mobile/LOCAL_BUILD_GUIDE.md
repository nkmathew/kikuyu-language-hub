# Local APK Build Guide

Complete guide for building Android APKs locally without EAS Build.

## Prerequisites

### 1. Android SDK Setup

**Required Components:**
- Android SDK Platform-Tools
- Android SDK Build-Tools 36.0.0
- Android SDK Platform API 36
- Android NDK 27.1.12297006

**Installation:**

Download Android Studio or install SDK via command line tools:
- https://developer.android.com/studio

**SDK Location:**
- Windows: `C:\Users\<username>\AppData\Local\Android\Sdk`
- Mac/Linux: `~/Android/Sdk` or `~/Library/Android/sdk`

### 2. Environment Configuration

Create `android/local.properties`:
```properties
sdk.dir=C\\:\\Users\\Monk\\AppData\\Local\\Android\\Sdk
```

**Note**: On Windows, use double backslashes to escape paths.

### 3. Release Keystore

**Location**: `android/app/kikuyu-flashcards-release.keystore`

**Configuration** (already set up in `android/app/build.gradle`):
```gradle
signingConfigs {
    release {
        storeFile file('kikuyu-flashcards-release.keystore')
        storePassword 'android123'
        keyAlias 'kikuyu-flashcards'
        keyPassword 'android123'
    }
}
```

**⚠️ Security Note**: Change password for production deployment!

## Build Commands

All commands run from project root: `kikuyu-flashcards-mobile/`

### Debug Build (Fast Testing)

```bash
npm run build:android:debug

# Output: android/app/build/outputs/apk/debug/app-debug.apk
# Size: ~50-60 MB (unminified)
# Signing: Debug keystore (for testing only)
```

### Release Build (Production)

```bash
npm run build:android:release

# Output: android/app/build/outputs/apk/release/app-release.apk
# Size: ~20-30 MB (minified with ProGuard)
# Signing: Release keystore (production-ready)
```

### App Bundle (For Google Play)

```bash
npm run build:android:bundle

# Output: android/app/build/outputs/bundle/release/app-release.aab
# Size: ~15-20 MB (optimized for Play Store)
# Google Play generates optimized APKs per device
```

### Clean Build

```bash
npm run clean:android

# Removes build cache and temporary files
# Use when builds fail or after updating dependencies
```

## Build Process Steps

### First-Time Build

1. **Install Dependencies**
```bash
npm install
npx expo prebuild --platform android
```

2. **Configure SDK**
Create `android/local.properties` with SDK path

3. **Build Release APK**
```bash
npm run build:android:release
```

**Expected Duration:**
- First build: 5-10 minutes (downloads NDK ~1GB + dependencies)
- Subsequent builds: 1-3 minutes

### Regular Builds

```bash
# Clean previous build
npm run clean:android

# Build release APK
npm run build:android:release
```

## Data Bundling Verification

The build includes all 196 JSON flashcard files. Verify with:

```bash
# Export bundle to check module count
npx expo export --platform android

# Expected output:
# Successfully bundled 1038 modules (2.98 MB)
# Includes all 196 JSON files from src/assets/data/curated/
```

## ProGuard Configuration

**File**: `android/app/proguard-rules.pro`

```proguard
# Keep JSON data files in assets
-keep class **.assets.data.curated.** { *; }
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod *;
}

# Don't warn about missing classes
-dontwarn com.facebook.react.**
```

This ensures the 196 JSON files are not stripped during minification.

## Installing APK

### Via ADB (Android Debug Bridge)

```bash
# Install on connected device
adb install android/app/build/outputs/apk/release/app-release.apk

# Install and replace existing app
adb install -r android/app/build/outputs/apk/release/app-release.apk

# Uninstall first
adb uninstall com.kikuyulanguagehub.flashcards
```

### Manual Installation

1. Copy APK to device via USB or cloud storage
2. Open file on Android device
3. Allow "Install from Unknown Sources" if prompted
4. Tap "Install"

## Troubleshooting

### Build Fails: SDK Not Found

**Error**: `SDK location not found`

**Solution**: Create `android/local.properties` with correct SDK path

### Build Fails: NDK Not Installed

**Error**: `NDK (Side by side) 27.1.12297006 not found`

**Solution**: Gradle will auto-download NDK on first build (wait 5-10 minutes)

Or manually install:
```bash
sdkmanager "ndk;27.1.12297006"
```

### Build Fails: Gradle Lock

**Error**: `Timeout waiting to lock build logic queue`

**Solution**: Stop all Gradle daemons
```bash
cd android
./gradlew --stop
```

### Build Fails: Out of Memory

**Error**: `Java heap space` or `Out of memory`

**Solution**: Increase Gradle memory in `android/gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### APK Installs But Data Missing

**Error**: Flashcards show zero count

**Solution**: Verify bundle includes data:
```bash
npx expo export --platform android
# Check output shows 1038 modules
```

If missing, regenerate dataLoader.ts:
```bash
node generate-data-loader.js
```

### Keystore Not Found

**Error**: `kikuyu-flashcards-release.keystore not found`

**Solution**: Generate new keystore:
```bash
cd android/app
keytool -genkeypair -v -storetype PKCS12 \
  -keystore kikuyu-flashcards-release.keystore \
  -alias kikuyu-flashcards \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android123 -keypass android123
```

## Build Optimization

### Enable Hermes Engine (Default)

Hermes is already enabled in `android/app/build.gradle`:
```gradle
project.ext.react = [
    enableHermes: true
]
```

**Benefits:**
- Faster app startup (2-3x)
- Smaller bundle size (20-30% reduction)
- Lower memory usage

### Enable R8 Code Shrinking

Already enabled for release builds:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
    }
}
```

**Size Reduction:** 40-50% smaller APK

### Split APKs by Architecture

To create separate APKs for each CPU architecture:

Edit `android/app/build.gradle`:
```gradle
android {
    splits {
        abi {
            enable true
            reset()
            include "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
            universalApk false
        }
    }
}
```

Generates 4 APKs instead of 1 universal:
- arm64-v8a (64-bit ARM - most modern devices) ~15 MB
- armeabi-v7a (32-bit ARM - older devices) ~14 MB
- x86_64 (64-bit Intel - emulators) ~16 MB
- x86 (32-bit Intel - old tablets) ~15 MB

## APK Signing for Play Store

### Generate Production Keystore

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore kikuyu-flashcards-prod.keystore \
  -alias kikuyu-flashcards-prod \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Kikuyu Language Hub, OU=Development, O=Kikuyu Language Hub, L=Nairobi, ST=Nairobi, C=KE"
```

**⚠️ Important**: Store keystore and password securely! If lost, you cannot update your app on Play Store.

### Update Signing Config

Edit `android/app/build.gradle`:
```gradle
signingConfigs {
    release {
        storeFile file('kikuyu-flashcards-prod.keystore')
        storePassword System.getenv("KEYSTORE_PASSWORD")
        keyAlias "kikuyu-flashcards-prod"
        keyPassword System.getenv("KEY_PASSWORD")
    }
}
```

Build with environment variables:
```bash
export KEYSTORE_PASSWORD='your-secure-password'
export KEY_PASSWORD='your-secure-password'
npm run build:android:release
```

## Version Management

Update version in `android/app/build.gradle`:
```gradle
defaultConfig {
    versionCode 2      // Increment for each release
    versionName "1.1.0"  // User-facing version
}
```

**Version Code Rules:**
- Must increment for every Play Store upload
- Cannot decrease
- Typically: 1, 2, 3, 4...

**Version Name Format:**
- MAJOR.MINOR.PATCH (semantic versioning)
- Example: 1.0.0 → 1.0.1 (bugfix) → 1.1.0 (feature) → 2.0.0 (breaking)

## CI/CD Integration (Optional)

### GitHub Actions

Create `.github/workflows/android-build.yml`:
```yaml
name: Android Build

on:
  push:
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

      - name: Setup Android SDK
        uses: android-actions/setup-android@v2

      - name: Install dependencies
        run: npm install
        working-directory: kikuyu-flashcards-mobile

      - name: Build Release APK
        run: npm run build:android:release
        working-directory: kikuyu-flashcards-mobile

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release.apk
          path: kikuyu-flashcards-mobile/android/app/build/outputs/apk/release/app-release.apk
```

## Comparison: Local vs EAS Build

| Feature | Local Build | EAS Build |
|---------|------------|-----------|
| Cost | Free | $29/month (after free tier) |
| Build Time | 2-5 minutes | 10-20 minutes (queue + build) |
| Requirements | Android Studio/SDK | Internet connection only |
| Control | Full native access | Limited to Expo config |
| Offline | Yes | No (cloud service) |
| CI/CD | Self-hosted | Built-in |
| Learning Curve | Moderate | Easy |

## Summary

**Local APK builds are now the primary method** for this project.

**Quick Build:**
```bash
npm run build:android:release
```

**Output**: `android/app/build/outputs/apk/release/app-release.apk`

**Install**: Transfer to device and install, or use `adb install`

**No EAS required** ✅
