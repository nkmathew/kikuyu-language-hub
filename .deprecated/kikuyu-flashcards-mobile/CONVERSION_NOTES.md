# Next.js to React Native Conversion Notes

## Overview

This document details the conversion of the Kikuyu Flashcards Next.js web app to React Native mobile app.

## Files Converted

### ✅ Directly Reusable (No Changes)
- `src/types/flashcard.ts` - Type definitions work identically
- JSON data files - Same format, just bundled differently

### 🔄 Adapted (Minor Changes)
- `src/lib/dataLoader.ts` - Changed from fetch to bundled imports
  - **Before:** `fetch('/data/curated/...')`
  - **After:** `import data from '../assets/data/...'`

### 🆕 Newly Created (RN-Specific)
- `src/navigation/AppNavigator.tsx` - React Navigation setup
- `src/screens/HomeScreen.tsx` - Category browser
- `src/screens/CategoryScreen.tsx` - Difficulty selector
- `src/screens/FlashcardScreen.tsx` - Study interface with animations
- `src/screens/ProgressScreen.tsx` - Progress tracking (placeholder)
- `App.tsx` - Entry point

## Component Conversions

### Example 1: Home Screen

**Next.js Version** (Hypothetical):
```tsx
// app/page.tsx
export default function Home() {
  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold">Categories</h1>
      <div className="grid grid-cols-2 gap-4">
        {categories.map(cat => (
          <Link href={`/${cat.key}`} key={cat.key}>
            <div className="card p-4">
              <h2>{cat.title}</h2>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
```

**React Native Version**:
```tsx
// src/screens/HomeScreen.tsx
export default function HomeScreen({ navigation }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Categories</Text>
      <FlatList
        data={categories}
        numColumns={2}
        renderItem={({ item }) => (
          <TouchableOpacity
            onPress={() => navigation.navigate('Category', { category: item.key })}
          >
            <View style={styles.card}>
              <Text>{item.title}</Text>
            </View>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  title: { fontSize: 24, fontWeight: 'bold' },
  card: { padding: 16, margin: 8 },
});
```

### Example 2: Flashcard Flip Animation

**Next.js** (CSS-based):
```tsx
<div className="card-container">
  <div className={`card ${flipped ? 'flipped' : ''}`}>
    <div className="front">{card.kikuyu}</div>
    <div className="back">{card.english}</div>
  </div>
</div>

// CSS
.card { transition: transform 0.6s; transform-style: preserve-3d; }
.card.flipped { transform: rotateY(180deg); }
.front, .back { backface-visibility: hidden; }
```

**React Native** (Animated API):
```tsx
const flipAnimation = useRef(new Animated.Value(0)).current;

const flipCard = () => {
  Animated.timing(flipAnimation, {
    toValue: isFlipped ? 0 : 1,
    duration: 300,
    useNativeDriver: true,
  }).start();
};

const frontRotate = flipAnimation.interpolate({
  inputRange: [0, 1],
  outputRange: ['0deg', '180deg'],
});

<Animated.View style={{ transform: [{ rotateY: frontRotate }] }}>
  <Text>{card.kikuyu}</Text>
</Animated.View>
```

## UI Element Mapping

| Next.js/HTML | React Native | Notes |
|--------------|--------------|-------|
| `<div>` | `<View>` | Container |
| `<p>`, `<span>` | `<Text>` | All text must be in Text |
| `<button>` | `<TouchableOpacity>` | Pressable alternative |
| `<a>` | `<TouchableOpacity>` + navigation | No href |
| `<input>` | `<TextInput>` | Different props |
| `className` | `style` | StyleSheet object |
| CSS | StyleSheet.create() | JavaScript object |
| Tailwind | Manual styles | No utility CSS |
| `onClick` | `onPress` | Different event name |
| `onChange` | `onChangeText` | Different signature |

## Styling Comparison

### Next.js (Tailwind):
```tsx
<div className="flex items-center justify-between p-4 bg-white rounded-lg shadow-md">
  <h2 className="text-xl font-bold text-gray-900">Title</h2>
  <span className="text-blue-600">→</span>
</div>
```

### React Native (StyleSheet):
```tsx
<View style={styles.container}>
  <Text style={styles.title}>Title</Text>
  <Text style={styles.arrow}>→</Text>
</View>

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    backgroundColor: '#fff',
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    elevation: 3, // Android shadow
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  arrow: {
    color: '#2563eb',
  },
});
```

## Navigation Comparison

### Next.js App Router:
```
app/
├── page.tsx              → Home (/)
├── [category]/
│   └── page.tsx          → Category (/:category)
└── study/
    └── page.tsx          → Study (/study)
```

```tsx
// Navigation
<Link href="/vocabulary">Go to Vocabulary</Link>
router.push('/vocabulary');
```

### React Navigation:
```tsx
// Define types
type RootStackParamList = {
  Home: undefined;
  Category: { category: CategoryType };
  Flashcard: { category: CategoryType; difficulties: DifficultyLevel[] };
};

// Navigation
navigation.navigate('Category', { category: 'vocabulary' });
navigation.goBack();
```

## Data Loading Strategies

### Strategy 1: Bundled (Current POC)
**Pros:**
- Works offline immediately
- Fast load times
- Simple implementation

**Cons:**
- Larger app bundle (~2-3MB for 307 cards)
- Updates require app update
- All data loaded on install

**Implementation:**
```tsx
import batch001 from '../assets/data/curated/vocabulary/batch_001.json';
```

### Strategy 2: API Fetch (Future)
**Pros:**
- Smaller app bundle
- Easy content updates
- Analytics on usage

**Cons:**
- Requires internet on first load
- Need backend API
- Caching complexity

**Implementation:**
```tsx
const response = await fetch(`${API_URL}/data/curated/${category}/${batch}.json`);
const data = await response.json();
await AsyncStorage.setItem(`batch_${batch}`, JSON.stringify(data));
```

### Strategy 3: Hybrid (Recommended)
**Pros:**
- Best of both worlds
- Offline first, updates second
- Graceful degradation

**Implementation:**
```tsx
// 1. Load bundled data first
let data = bundledData[category];

// 2. Check for updates from API
try {
  const response = await fetch(`${API_URL}/updates/${category}/latest.json`);
  const updates = await response.json();

  // 3. Merge with bundled data
  data = mergeUpdates(data, updates);

  // 4. Cache merged data
  await AsyncStorage.setItem(`${category}_data`, JSON.stringify(data));
} catch (error) {
  // Fallback to bundled data
}
```

## State Management

### Current (POC): Local State
```tsx
const [cards, setCards] = useState<Flashcard[]>([]);
```

### Future Options:

**1. Context API (Simple)**
```tsx
const StudyContext = createContext();

export function StudyProvider({ children }) {
  const [progress, setProgress] = useState({});
  return <StudyContext.Provider value={{ progress, setProgress }}>{children}</StudyContext.Provider>;
}
```

**2. Redux Toolkit (Complex Apps)**
```tsx
const progressSlice = createSlice({
  name: 'progress',
  initialState: {},
  reducers: {
    updateCardProgress: (state, action) => {
      state[action.payload.cardId] = action.payload.progress;
    },
  },
});
```

**3. Zustand (Recommended)**
```tsx
import create from 'zustand';

const useStore = create((set) => ({
  progress: {},
  updateProgress: (cardId, data) => set((state) => ({
    progress: { ...state.progress, [cardId]: data }
  })),
}));
```

## Storage Patterns

### AsyncStorage (React Native)
```tsx
// Save
await AsyncStorage.setItem('progress', JSON.stringify(progressData));

// Load
const data = await AsyncStorage.getItem('progress');
const progress = data ? JSON.parse(data) : {};

// Remove
await AsyncStorage.removeItem('progress');

// Clear all
await AsyncStorage.clear();
```

### LocalStorage (Web - for comparison)
```tsx
// Save
localStorage.setItem('progress', JSON.stringify(progressData));

// Load
const data = localStorage.getItem('progress');
const progress = data ? JSON.parse(data) : {};
```

## Performance Optimizations

### 1. List Rendering
```tsx
// Use FlatList instead of map
<FlatList
  data={cards}
  renderItem={({ item }) => <Card card={item} />}
  keyExtractor={item => item.id}
  removeClippedSubviews={true}
  maxToRenderPerBatch={10}
  windowSize={10}
/>
```

### 2. Image Loading
```tsx
// Use FastImage for better performance
import FastImage from 'react-native-fast-image';

<FastImage
  source={{ uri: imageUrl, priority: FastImage.priority.normal }}
  resizeMode={FastImage.resizeMode.contain}
/>
```

### 3. Memoization
```tsx
const Card = React.memo(({ card }) => {
  // Component only re-renders if card changes
  return <View>...</View>;
});
```

## Testing Approaches

### Next.js
```tsx
// Jest + React Testing Library
import { render, screen } from '@testing-library/react';

test('renders category title', () => {
  render(<CategoryScreen category="vocabulary" />);
  expect(screen.getByText('Vocabulary')).toBeInTheDocument();
});
```

### React Native
```tsx
// Jest + React Native Testing Library
import { render, fireEvent } from '@testing-library/react-native';

test('renders category title', () => {
  const { getByText } = render(<CategoryScreen category="vocabulary" />);
  expect(getByText('Vocabulary')).toBeTruthy();
});
```

## Deployment

### Next.js Web
```bash
npm run build  # Creates .next/ folder
# Deploy to Netlify/Vercel
```

### React Native
```bash
# Development build
eas build --profile development --platform ios
eas build --profile development --platform android

# Production build
eas build --profile production --platform all

# Submit to stores
eas submit --platform ios
eas submit --platform android
```

## App Size Comparison

| Component | Next.js | React Native |
|-----------|---------|--------------|
| Initial bundle | ~500KB | N/A (full install) |
| Full app | N/A | ~15-20MB |
| With assets | Variable | +2-3MB (images) |
| With flashcards | N/A | +2-3MB (JSON) |
| **Total** | ~500KB - 2MB | ~20-25MB |

## Cost Breakdown

### Development
- React Native knowledge: 1-2 weeks learning curve
- Initial conversion: 2-4 weeks
- Testing/refinement: 1-2 weeks

### Distribution
- **Apple Developer:** $99/year
- **Google Play:** $25 one-time
- **Expo EAS Build:** Free tier available, $29/month for production

### Maintenance
- App updates: More complex than web
- Platform compatibility: Test on iOS & Android
- Store review process: 1-7 days per update

## Conclusion

The POC demonstrates successful conversion of core functionality. The architecture is sound and can be extended to include all features from the Next.js version plus mobile-specific enhancements (offline, push notifications, etc.).

**Recommendation:** Proceed with full conversion using hybrid data strategy for optimal user experience.
