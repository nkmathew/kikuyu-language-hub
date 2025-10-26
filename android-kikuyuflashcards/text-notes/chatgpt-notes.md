# Android Dependencies for Flash Card App

## UI Architecture & Lifecycle

### ViewModel and LiveData for reactive UI
- **lifecycle-viewmodel**: Manages UI-related data and survives configuration changes
- **lifecycle-livedata**: Observable data holder that respects lifecycle
- **lifecycle-viewmodel-savedstate**: Preserves ViewModel state across process death

```gradle
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0'
```

### Navigation Component for better navigation
- **navigation-fragment**: Handles navigation between destinations
- **navigation-ui**: Integrates with Material Design components

```gradle
implementation 'androidx.navigation:navigation-fragment:2.7.7'
implementation 'androidx.navigation:navigation-ui:2.7.7'
```

## Data Layer

### Room for local database
- **room-runtime**: Core Room library for database operations
- **room-ktx**: Kotlin extensions for Room
- **room-compiler**: Annotation processor for code generation

```gradle
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'
```

### Network & API
- **Retrofit**: Type-safe HTTP client for REST APIs
- **Gson Converter**: JSON to object conversion
- **Logging Interceptor**: HTTP request/response logging
- **Gson**: Google's JSON library for Java/Kotlin object serialization

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

### Data Storage
- **DataStore**: Coroutine-based data storage with type safety (modern replacement for SharedPreferences)

```gradle
implementation 'androidx.datastore:datastore-preferences:1.0.0'
```

## UI & Animations

### Animation Libraries
- **Lottie**: Renders After Effects animations as native Android views
- **Shimmer**: Facebook's shimmer effect for loading placeholders
- **Material Motion**: Material Design motion system for animations

```gradle
implementation 'com.airbnb.android:lottie:6.3.0'
implementation 'com.facebook.shimmer:shimmer:0.5.0'
implementation 'com.google.android.material:material-motion:1.0.0'
```

### UI Components
- **ViewPager2**: Modern ViewPager with better performance and RTL support (perfect for card swiping)

```gradle
implementation 'androidx.viewpager2:viewpager2:1.0.0'
```

## Development Tools

### Dependency Injection
- **Hilt**: Android-specific DI library built on top of Dagger
- **Hilt Compiler**: Annotation processor for code generation

```gradle
implementation 'com.google.dagger:hilt-android:2.50'
annotationProcessor 'com.google.dagger:hilt-compiler:2.50'
```

### Testing
- **Mockito Core**: Mocking framework for unit tests
- **Mockito Inline**: Supports mocking final classes and static methods

```gradle
testImplementation 'org.mockito:mockito-core:5.8.0'
testImplementation 'org.mockito:mockito-inline:5.2.0'
```

### Debugging & Logging
- **LeakCanary**: Detects memory leaks in debug builds
- **Timber**: Enhanced logging library with tree-based architecture

```gradle
debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
implementation 'com.jakewharton.timber:timber:5.0.1'
```

## Additional Features

### Image Loading
- **Coil**: Kotlin-first image loading library with coroutines

```gradle
implementation 'io.coil-kt:coil:2.5.0'
```

### Background Processing
- **WorkManager**: Deferrable background work with constraints and scheduling

```gradle
implementation 'androidx.work:work-runtime:2.9.0'
```

### App Initialization
- **Startup**: App startup library for initializing components

```gradle
implementation 'androidx.startup:startup-runtime:1.1.1'
```

## Analytics & Monitoring

### Firebase
- **Firebase BOM**: Bill of Materials for consistent Firebase versioning
- **Firebase Analytics**: User behavior tracking and app insights
- **Firebase Crashlytics**: Real-time crash reporting and analytics

```gradle
implementation platform('com.google.firebase:firebase-bom:32.7.2')
implementation 'com.google.firebase:firebase-analytics'
implementation 'com.google.firebase:firebase-crashlytics'
```
```

I've reformatted the content as proper markdown with:
- Clear hierarchical structure using headers
- Bullet points for descriptions
- Code blocks for Gradle dependencies
- Logical grouping by functionality
- Better readability and organization
