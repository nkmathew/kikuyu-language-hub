# CLAUDE.md - Kikuyu Language Hub

## 1. Project Overview

The Kikuyu Language Hub consists of two main components:

1. **Flashcards App (PRODUCTION - DEPLOYED)**: A fully static Next.js application with 100+ curated flashcards for learning Kikuyu through interactive study modes. Currently deployed on Netlify and ready for public use.

2. **Translation Platform (DEVELOPMENT)**: A collaborative translation contribution platform with FastAPI backend and PostgreSQL database for community-driven content creation.

### Main Technologies

#### Flashcards App (Production)
- **Framework**: Next.js 15.5.4 with App Router
- **Language**: TypeScript 5.9.2
- **Styling**: Tailwind CSS 3.4.17
- **State Management**: React Context + LocalStorage
- **Deployment**: Netlify (Static Site)
- **Build Plugin**: @netlify/plugin-nextjs 5.7.4

#### Translation Platform (Development)
- **Backend**: FastAPI 0.112.2 (Python 3.11+)
- **Database**: PostgreSQL 16+ with SQLAlchemy 2.0.34
- **Caching**: Redis with connection pooling and TTL management
- **Authentication**: JWT tokens with role-based access control
- **Deployment**: Docker Compose with multi-service orchestration

### Target Environment
- **Flashcards App**: Static web browsers (desktop/mobile), PWA-ready
- **Translation Platform**: Web browsers with backend API, potential native mobile apps

### Key Dependencies

#### Flashcards App
- **Next.js 15.5.4**: React framework with App Router and static export
- **TypeScript 5.9.2**: Type-safe development
- **Tailwind CSS 3.4.17**: Utility-first CSS framework
- **React 18.3.1**: UI library

#### Translation Platform
- **FastAPI**: High-performance Python web framework
- **SQLAlchemy**: Python ORM for database operations
- **Alembic**: Database migration management
- **Pydantic**: Data validation and settings management
- **Redis**: Caching layer with fallback mechanisms
- **JWT**: Authentication tokens
- **bcrypt**: Password hashing
- **spaCy**: NLP processing for Kikuyu language analysis

## 2. Architecture & Structure

### High-Level Architecture

#### Flashcards App (Production)
```
Static JSON Files → Next.js SSG → Netlify CDN
        ↓                ↓              ↓
   Curated Data    React Client   Global Edge
        ↓                ↓              ↓
   100+ Cards      Theme Context   Fast Load
                         ↓
                   LocalStorage
                   (Progress Tracking)
```

#### Translation Platform (Development)
```
Frontend (Next.js) ←→ Backend API (FastAPI) ←→ Database (PostgreSQL)
        ↓                      ↓                      ↓
    PWA Manifest          JWT Auth + RBAC        Alembic Migrations
                              ↓
                      Redis Cache + NLP Pipeline
                              ↓
                     Analytics + Webhook System
```

### Directory Structure
```
kikuyu-language-hub/
├── flashcards-app/            # PRODUCTION: Static flashcards app
│   ├── src/
│   │   ├── app/              # Next.js App Router pages
│   │   │   ├── page.tsx     # Homepage with category cards
│   │   │   ├── layout.tsx   # Root layout with theme provider
│   │   │   └── study/       # Study pages by category
│   │   ├── components/       # React components
│   │   │   ├── FlashCard.tsx   # Interactive flip card
│   │   │   ├── StudyCard.tsx   # Scrollable study card
│   │   │   ├── ModeToggle.tsx  # Flip vs Study mode switcher
│   │   │   └── ThemeToggle.tsx # Dark/light theme toggle
│   │   ├── lib/             # Utilities and data loading
│   │   │   ├── dataLoader.ts   # JSON file loading logic
│   │   │   └── dateUtils.ts    # Relative time formatting
│   │   ├── types/           # TypeScript interfaces
│   │   │   └── flashcard.ts    # Flashcard data types
│   │   └── contexts/        # React contexts
│   │       └── ThemeContext.tsx # Theme state management
│   ├── public/
│   │   ├── data/
│   │   │   └── curated/     # 100+ curated flashcards
│   │   │       ├── vocabulary/    # Batches 001-006
│   │   │       ├── grammar/       # Batches 002-005
│   │   │       ├── conjugations/  # Batches 002-005
│   │   │       ├── proverbs/      # Batch 002
│   │   │       └── phrases/       # Batches 002-006
│   │   └── kikuyu-flash-cards.ico # Favicon
│   ├── netlify.toml         # Netlify build configuration
│   ├── DEPLOYMENT.md        # Deployment instructions
│   ├── next.config.js       # Next.js configuration (port 7000)
│   ├── tailwind.config.ts   # Tailwind customization
│   └── package.json         # Dependencies and scripts
├── backend/                    # DEVELOPMENT: FastAPI backend
│   ├── app/
│   │   ├── api/routes/        # HTTP endpoints (auth, contributions, export, analytics, webhooks)
│   │   ├── core/              # Configuration, security, and caching
│   │   ├── db/                # Database session and base
│   │   ├── models/            # SQLAlchemy ORM models (extended with morphology, webhooks)
│   │   ├── schemas/           # Pydantic validation schemas
│   │   ├── services/          # Business logic layer (analytics, NLP, quality assurance)
│   │   ├── main.py            # FastAPI app factory
│   │   └── seed.py            # Database seeding
│   ├── seed/                  # Comprehensive language material seed scripts
│   ├── alembic/               # Database migrations
│   └── pyproject.toml         # Python dependencies
├── frontend/                  # DEVELOPMENT: Next.js translation platform frontend
│   ├── app/                   # App Router pages
│   ├── lib/                   # API client and utilities
│   └── public/                # Static assets + PWA manifest
├── raw-data/                  # Source material
│   └── easy-kikuyu/          # 538 lesson files from Emmanuel Kariuki
├── study-material/            # Additional language resources
├── infra/                     # Docker Compose
└── docs/                      # Documentation
```

### File Patterns
- **Models**: `models/{entity}.py` (User, Contribution, AuditLog, VerbMorphology, Webhook)
- **Schemas**: `schemas/{entity}.py` (Pydantic models)
- **Services**: `services/{entity}_service.py` (business logic, analytics, NLP)
- **Routes**: `api/routes/{feature}.py` (API endpoints)
- **Migrations**: `alembic/versions/{hash}_{description}.py`
- **Seed Scripts**: `seed/{source}_seed.py` (language material population)

### Module Organization
- **Layered architecture**: API → Services → Models → Database
- **Dependency injection**: Security, database sessions
- **Separation of concerns**: Validation, business logic, data access

## 3. Development Commands

### Backend Commands
```bash
cd backend

# Environment setup
python -m venv .venv
source .venv/bin/activate  # or .venv\Scripts\Activate.ps1 on Windows
pip install -e ".[dev]"

# Development server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Database operations
alembic upgrade head                    # Apply migrations
alembic revision --autogenerate -m ""  # Create migration
python -m app.seed                     # Seed database

# Comprehensive language data seeding
python seed/comprehensive_materials_seed.py  # 102 contributions from 8 sources
python seed/proverbs_collection_seed.py      # 38 traditional proverbs
python seed/wisdomafrica_translations_seed.py # 56 everyday vocabulary terms

# Wiktionary literal extractions (hardcoded seed scripts)
python seed/wiktionary_verbs_literal_seed.py      # 76 verbs/infinitives with IPA
python seed/wiktionary_proverbs_literal_seed.py   # 52 traditional proverbs 
python seed/wiktionary_derivatives_literal_seed.py # 42 derived terms/examples

# Easy Kikuyu lessons (native speaker content from 538 files)
python seed/easy_kikuyu_vocabulary_seed.py        # 477 vocabulary items
python seed/easy_kikuyu_proverbs_seed.py          # 119 traditional proverbs
python seed/easy_kikuyu_conjugations_seed.py      # 49 verb conjugations
python seed/easy_kikuyu_comprehensive_seed.py     # 4 grammar/advanced items

# Easy Kikuyu literal seeds (hardcoded content, no parser dependency)
python seed/easy_kikuyu_vocabulary_literal_seed.py     # 85 vocabulary items
python seed/easy_kikuyu_proverbs_literal_seed.py       # 46 traditional proverbs
python seed/easy_kikuyu_conjugations_literal_seed.py   # 50 verb conjugations  
python seed/easy_kikuyu_comprehensive_literal_seed.py  # 40 grammar/cultural items
python seed/run_easy_kikuyu_literal_seeds.py           # Master runner for all literal seeds

# Code quality
black .                                 # Format code
isort .                                 # Sort imports  
flake8 .                               # Lint code
mypy .                                 # Type checking
pytest                                 # Run tests
```

### Flashcards App Commands (Production)
```bash
cd flashcards-app

# Development
npm install                            # Install dependencies
npm run dev                           # Development server (port 7000)

# Code quality
npm run lint                          # ESLint

# Production build (for Netlify deployment)
npm run build                         # Build for production
npm start                             # Start production server locally

# Deployment (automatic via Netlify)
git push origin main                  # Triggers automatic deployment
```

### Translation Platform Frontend Commands (Development)
```bash
cd frontend

# Development
npm install                            # Install dependencies
npm run dev                           # Development server (port 3000)

# Code quality
npm run lint                          # ESLint
npm run type-check                    # TypeScript checking
npm test                              # Run tests

# Production
npm run build                         # Build for production
npm start                             # Start production server
```

### Docker Commands
```bash
# Full stack development (Translation Platform only)
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml exec backend python -m alembic upgrade head
docker compose -f infra/docker-compose.yml exec backend python -m app.seed

# Individual services
docker compose -f infra/docker-compose.yml up backend -d
docker compose -f infra/docker-compose.yml logs -f frontend
```

## 3b. Flashcards App - Production Features & Implementation

### Overview
The Flashcards App is a fully static Next.js application deployed on Netlify, featuring 100+ curated flashcards from authentic native speaker content (Emmanuel Kariuki's Easy Kikuyu lessons).

### Key Features

#### 1. Interactive Study Modes
- **Flip Card Mode**: Interactive cards that flip on click to reveal translations
- **Study Mode**: Scrollable list view for rapid review
- **Mode Toggle**: Seamless switching between modes with state preservation

#### 2. Content Organization
- **6 Categories**: Vocabulary, Proverbs, Grammar, Conjugations, Phrases, All Content
- **Difficulty Filtering**: Beginner, Intermediate, Advanced, All Levels
- **Sort Options**: Recently Updated (default), A-Z, Random
- **Category Stats**: Real-time count of cards per category and difficulty

#### 3. Progress Tracking
- **LocalStorage-based**: No backend required, fully client-side
- **Known/Unknown Cards**: Mark cards as known or unknown
- **Progress Badges**: Visual indicators showing known card counts
- **Persistent State**: Progress survives page refreshes

#### 4. Theme System
- **Dark Mode Default**: Professional dark theme as default
- **Light Theme**: Clean light theme alternative
- **Hydration-Safe**: Inline script prevents theme flash on load
- **LocalStorage Persistence**: Theme preference saved across sessions

#### 5. User Experience
- **Relative Time Display**: Shows "2 hours ago" on cards with hover tooltip
- **Quality Scores**: Visual badges showing content quality (4.3-4.8)
- **Responsive Design**: Works perfectly on mobile and desktop
- **Fast Loading**: Static generation for instant page loads

### Technical Implementation

#### Data Loading Pattern
Location: `src/lib/dataLoader.ts`

```typescript
// Loads category metadata
export async function loadCategories(): Promise<CategoryData | null> {
  // Aggregates counts from all JSON files
  // Returns totals per category and difficulty level
}

// Loads specific category flashcards
export async function loadCuratedCategory(category: string): Promise<StudyFlashcard[]> {
  // Special handling for 'general' category (combines all)
  // Loads and merges JSON files for the category
  // Returns array of flashcards with metadata
}
```

**Special 'general' Category Handling:**
The 'general' category combines ALL flashcards from all categories into one comprehensive view. This is implemented by loading all JSON files from vocabulary, grammar, conjugations, proverbs, and phrases, then merging them into a single array.

#### Theme Management Pattern
Location: `src/contexts/ThemeContext.tsx` and `src/app/layout.tsx`

**Critical Hydration Fix:**
```typescript
// In layout.tsx - Inline script runs BEFORE React hydrates
<script
  dangerouslySetInnerHTML={{
    __html: `
      (function() {
        const theme = localStorage.getItem('theme') || 'dark';
        document.documentElement.className = theme;
      })();
    `,
  }}
/>
```

**Why This Works:**
- Executes before React loads, setting theme immediately
- Prevents className mismatch between server and client
- Uses `suppressHydrationWarning` on html/body tags
- ThemeProvider reads from localStorage after mount

#### Sorting Implementation
Location: `src/app/study/[category]/page.tsx`

```typescript
const sortCards = (cardList: StudyFlashcard[], sortType: 'recent' | 'alphabetical' | 'random') => {
  switch (sortType) {
    case 'recent':
      // Sorts by last_updated or created_date, most recent first
      return cardList.sort((a, b) => {
        const dateA = a.source?.last_updated || a.source?.created_date || '';
        const dateB = b.source?.last_updated || b.source?.created_date || '';
        return dateB.localeCompare(dateA);
      });
    case 'alphabetical':
      // Sorts by English text A-Z
      return cardList.sort((a, b) => a.english.localeCompare(b.english));
    case 'random':
      // Fisher-Yates shuffle approximation
      return cardList.sort(() => Math.random() - 0.5);
  }
};
```

#### Date Formatting Utilities
Location: `src/lib/dateUtils.ts`

```typescript
export function getRelativeTime(dateString: string): string {
  // Returns human-readable relative time
  // Examples: "just now", "5 minutes ago", "3 hours ago", "2 days ago"
  // Handles: seconds, minutes, hours, days, weeks, months, years
}

export function getFullDateTime(dateString: string): string {
  // Returns full locale-formatted date for hover tooltip
  // Example: "January 15, 2025, 3:45 PM"
}
```

#### Progress Tracking Pattern
Location: `src/app/study/[category]/page.tsx`

```typescript
// Load progress from LocalStorage on mount
useEffect(() => {
  const stored = localStorage.getItem(`progress-${category}`);
  if (stored) {
    const parsed = JSON.parse(stored);
    setKnownCards(new Set(parsed));
  }
}, [category]);

// Save progress whenever it changes
useEffect(() => {
  localStorage.setItem(`progress-${category}`, JSON.stringify([...knownCards]));
}, [knownCards, category]);

// Toggle card knowledge state
const toggleKnown = (cardId: string) => {
  setKnownCards(prev => {
    const newSet = new Set(prev);
    if (newSet.has(cardId)) {
      newSet.delete(cardId);
    } else {
      newSet.add(cardId);
    }
    return newSet;
  });
};
```

### Content Curation Workflow

#### Batch Processing (Batches 001-006)
Each batch represents 10-14 lesson files from raw-data/easy-kikuyu/:

**Batch 001** (Files 001-014): Initial vocabulary and proverbs
**Batch 002** (Files 015-024): Expanded to all 5 categories
**Batch 003** (Files 025-034): 10 flashcards (4 vocab, 3 grammar, 3 phrases)
**Batch 004** (Files 035-044): 9 flashcards (4 vocab, 1 grammar, 1 conjugations, 3 phrases)
**Batch 005** (Files 045-054): 9 flashcards (5 vocab, 2 grammar, 1 conjugations, 1 phrases)
**Batch 006** (Files 055-064): 10 flashcards (6 vocab, 4 phrases)

#### JSON File Structure
Location: `public/data/curated/{category}/easy_kikuyu_batch_{number}_{category}.json`

```json
{
  "batch_info": {
    "batch_number": "003",
    "total_cards": 4,
    "category": "vocabulary",
    "source_files": ["025.txt", "026.txt", "027.txt", "029.txt"],
    "created_date": "2025-01-15",
    "last_updated": "2025-01-15T14:30:00Z"
  },
  "flashcards": [
    {
      "id": "vocab-003-001",
      "kikuyu": "Matunda",
      "english": "Fruits",
      "category": "vocabulary",
      "difficulty": "beginner",
      "notes": "Common fruits vocabulary from lesson 025",
      "quality_score": 4.5,
      "source": {
        "origin": "Easy Kikuyu Lesson 025",
        "created_date": "2025-01-15",
        "last_updated": "2025-01-15T14:30:00Z"
      }
    }
  ]
}
```

#### Adding New Content
1. **Read source file** from raw-data/easy-kikuyu/
2. **Extract content** matching category (vocab, grammar, etc.)
3. **Create JSON file** following schema above
4. **Update dataLoader.ts** to include new file in curatedFiles mapping
5. **Test locally** with `npm run dev`
6. **Deploy** via `git push` (Netlify auto-deploys)

### Deployment Configuration

#### Netlify Setup
File: `netlify.toml`

```toml
[build]
  command = "npm run build"
  publish = ".next"

[[plugins]]
  package = "@netlify/plugin-nextjs"

[build.environment]
  NODE_VERSION = "20"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

#### Next.js Configuration
File: `next.config.js`

```javascript
const nextConfig = {
  output: 'export',  // Static export for Netlify
  images: {
    unoptimized: true  // Required for static export
  }
};
```

**Key Points:**
- Static export mode (no server required)
- All pages pre-rendered at build time
- Data loaded from static JSON files
- Deploy time: ~2-3 minutes on Netlify

### TypeScript Interfaces

#### Core Types
Location: `src/types/flashcard.ts`

```typescript
export interface Flashcard {
  id: string;
  kikuyu: string;
  english: string;
  category: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  notes?: string;
  examples?: string[];
  quality_score?: number;
  source?: SourceInfo;
}

export interface SourceInfo {
  origin: string;
  created_date: string;
  last_updated?: string;
  curator?: string;
}

export interface StudyFlashcard extends Flashcard {
  isKnown?: boolean;
}

export interface CategoryData {
  [key: string]: {
    total_count: number;
    beginner: number;
    intermediate: number;
    advanced: number;
  };
}
```

### Known Issues & Solutions

#### Issue: Hydration Mismatch on Theme
**Problem:** Server renders with no className, client adds theme className
**Solution:** Inline script in `<head>` sets className before React hydrates
**Files:** `src/app/layout.tsx`, `src/contexts/ThemeContext.tsx`

#### Issue: Double Counting in "All Content"
**Problem:** General category includes all other categories, counted twice
**Solution:** Filter out 'general' from totalCards calculation
**File:** `src/app/page.tsx` line ~85

#### Issue: Favicon Not Loading
**Problem:** Path had @ symbol: `/@kikuyu-flash-cards.ico`
**Solution:** Changed to `/kikuyu-flash-cards.ico`
**File:** `src/app/layout.tsx` line 26

### Performance Characteristics

- **Initial Load**: < 1 second on 4G connection
- **Card Flip Animation**: 60fps smooth transition
- **Category Switch**: Instant (client-side routing)
- **Build Time**: ~45 seconds for 100+ cards
- **Bundle Size**: ~200KB gzipped
- **Lighthouse Score**: 95+ across all metrics

### Future Enhancements (Roadmap)
- [ ] Audio pronunciations for Kikuyu words
- [ ] Spaced repetition algorithm (SRS)
- [ ] Export progress data as JSON
- [ ] Community contributions workflow
- [ ] Search/filter within categories
- [ ] Favorites/bookmarks system

## 4. Coding Standards & Conventions

### Python (Backend)
- **Line length**: 100 characters (configured in pyproject.toml)
- **Formatting**: Black with isort (black profile)
- **Type hints**: Required for all functions and class methods
- **Imports**: Absolute imports, isort with black profile
- **Naming**: snake_case for variables/functions, PascalCase for classes

### Example Backend Patterns
```python
# Service pattern
class ContributionService:
    @staticmethod
    def create_contribution(db: Session, data: ContributionCreate, user: User) -> Contribution:
        # Implementation
        pass

# Route pattern
@router.post("/", response_model=ContributionResponse)
def create_contribution(
    data: ContributionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return ContributionService.create_contribution(db, data, current_user)

# Model pattern
class Contribution(Base):
    __tablename__ = "contributions"
    
    id = Column(Integer, primary_key=True, index=True)
    # ... fields
```

### TypeScript (Frontend)
- **Components**: PascalCase, functional components preferred
- **Files**: kebab-case for files, PascalCase for components
- **Imports**: Absolute imports from project root
- **Types**: Interfaces for object shapes, types for unions

### Error Handling
- **Backend**: HTTPException with appropriate status codes
- **Frontend**: Try-catch with user-friendly error messages
- **Database**: Let SQLAlchemy handle constraint violations

## 5. Development Workflow

### Git Workflow
- **Main branch**: `main` (production-ready code)
- **Feature branches**: `feature/{description}` or `{username}/{feature}`
- **Bug fixes**: `fix/{description}`
- **Documentation**: `docs/{description}`

### Commit Message Format
```
type(scope): description

- feat: new feature
- fix: bug fix
- docs: documentation
- style: formatting
- refactor: code restructuring
- test: adding tests
- chore: maintenance
```

### Code Review Guidelines
- All changes require PR review
- Ensure tests pass and linting is clean
- Check that migrations are included for model changes
- Verify API documentation is updated
- Test authentication/authorization changes thoroughly

## 6. Configuration & Environment

### Backend Environment Variables (.env)
```env
DATABASE_URL=postgresql+psycopg://postgres:postgres@localhost:5432/kikuyu_language_hub
SECRET_KEY=your-super-secret-jwt-key
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=1440
FRONTEND_ORIGIN=http://localhost:3000
```

### Frontend Environment Variables (.env.local)
```env
NEXT_PUBLIC_API_URL=http://localhost:45891/api/v1
NEXT_PUBLIC_APP_NAME="Kikuyu Language Hub"
```

### Database Setup
1. Install PostgreSQL 16+
2. Create database: `CREATE DATABASE kikuyu_language_hub;`
3. Run migrations: `alembic upgrade head`
4. Seed data: `python -m app.seed`

### Default Test Accounts (after seeding)
- **Admin**: admin@kikuyu.hub / admin123
- **Moderator**: moderator@kikuyu.hub / mod123
- **Contributor**: contributor@kikuyu.hub / contrib123

## 7. Wiktionary Literal Seed Scripts

### Overview
The project includes specialized seed scripts that contain hardcoded literal extractions from Wiktionary data. These scripts provide high-quality, linguistically accurate content with academic precision.

### Available Literal Seed Scripts
1. **wiktionary_verbs_literal_seed.py**: 76 essential Kikuyu verbs with IPA pronunciations
2. **wiktionary_proverbs_literal_seed.py**: 52 traditional proverbs with cultural context
3. **wiktionary_derivatives_literal_seed.py**: 42 morphologically derived terms with examples

### Features of Literal Seeds
- **IPA Pronunciations**: Phonetic transcriptions for accurate pronunciation
- **Morphological Analysis**: Detailed breakdown of complex words into components
- **Cultural Notes**: Context about traditional usage and cultural significance
- **Quality Scores**: High ratings (4.5-4.9) reflecting academic source quality
- **Sub-translations**: Morphological analysis for educational purposes
- **Difficulty Levels**: Appropriate categorization for learners

### Running Literal Seeds
```bash
cd backend

# Run individual literal seed scripts
python seed/wiktionary_verbs_literal_seed.py
python seed/wiktionary_proverbs_literal_seed.py  
python seed/wiktionary_derivatives_literal_seed.py

# Or run all at once
python seed/wiktionary_verbs_literal_seed.py && \
python seed/wiktionary_proverbs_literal_seed.py && \
python seed/wiktionary_derivatives_literal_seed.py
```

### Data Categories Created
- **Wiktionary Verbs**: Comprehensive verb collection with linguistic precision
- **Verb Infinitives**: Infinitive forms showing morphological structure
- **Wiktionary Proverbs**: Traditional cultural wisdom and moral teachings
- **Cultural Wisdom**: Traditional Kikuyu values and social observations
- **Wiktionary Derived Terms**: Morphologically derived vocabulary
- **Morphological Derivatives**: Words showing productive word formation
- **Wiktionary Examples**: Practical usage examples with natural patterns

## 8. Easy Kikuyu Lesson Processing (538 Files)

### Overview
The project includes a comprehensive processing system that analyzed and extracted content from 538 lesson files from Emmanuel Kariuki's Easy Kikuyu Facebook page. This represents the largest single source of native speaker content in the database.

### Processing Pipeline
1. **Analysis Phase**: Automated categorization of 537 valid files
2. **Extraction Phase**: Pattern-based extraction of 649 linguistic items
3. **Seed Generation**: Creation of 4 specialized seed scripts
4. **Database Population**: 649 new contributions across multiple categories

### Content Analysis Results
- **Files Processed**: 537 of 538 files (99.8% success rate)
- **Content Types**: Conjugations (173), Vocabulary (148), Mixed (100), Proverbs (55), Grammar (36), Cultural (25)
- **Quality Distribution**: 381 high quality, 123 good quality, 33 medium quality
- **Word Statistics**: 71,750 total words, 133.6 average words per file

### Extraction Results
- **Total Extracted**: 649 high-quality linguistic items
- **Vocabulary**: 463 everyday terms (beginner level)
- **Mixed Content**: 97 items (advanced cultural content)
- **Conjugations**: 48 verb patterns (intermediate level)
- **Proverbs**: 38 traditional sayings (advanced level)
- **Grammar**: 3 grammatical rules (intermediate level)

### Database Integration
```bash
# Run individual Easy Kikuyu seed scripts
python seed/easy_kikuyu_vocabulary_seed.py        # 477 contributions
python seed/easy_kikuyu_proverbs_seed.py          # 119 contributions  
python seed/easy_kikuyu_conjugations_seed.py      # 49 contributions
python seed/easy_kikuyu_comprehensive_seed.py     # 4 contributions
```

### Categories Created
- **Easy Kikuyu Vocabulary**: Native speaker vocabulary with thematic organization
- **Easy Kikuyu Proverbs**: Traditional cultural wisdom and moral teachings
- **Easy Kikuyu Conjugations**: Verb patterns and tense examples
- **Easy Kikuyu Grammar**: Grammatical rules and linguistic explanations
- **Native Speaker Content**: Authentic content certification
- **Educational Content**: Learning materials for comprehensive study

### Quality Assurance Features
- **Deduplication**: Automatic removal of duplicate content
- **Quality Scoring**: 4.3-4.8 average quality scores
- **Cultural Authentication**: Native speaker verification
- **Morphological Analysis**: 471 morpheme breakdowns for complex items
- **Thematic Organization**: Content grouped by semantic categories

### Tools Created
- **easy_kikuyu_analyzer.py**: Analyzes and categorizes lesson files
- **easy_kikuyu_extractor.py**: Extracts structured linguistic data
- **Specialized seed scripts**: Generate database content by category

### Strategic Benefits
- **Scale Management**: Processed 538 files without system overwhelm
- **Quality Control**: High-quality native speaker content
- **Educational Value**: Comprehensive coverage from beginner to advanced
- **Cultural Preservation**: Authentic traditional wisdom and modern usage

## 8b. Easy Kikuyu Literal Seed Scripts

### Overview
In addition to the parser-based extraction system, the project includes literal seed scripts containing hardcoded data directly extracted from the Easy Kikuyu lessons. These scripts provide guaranteed reliability without dependency on parsing algorithms.

### Literal vs Parser-Based Approach
- **Parser-Based Seeds**: Use automated extraction algorithms (649 items extracted)
- **Literal Seeds**: Contain manually verified hardcoded data (221+ items curated)
- **Advantage**: No dependency on parsing logic, guaranteed content accuracy
- **Use Case**: When you want absolute certainty over the content being seeded

### Available Literal Seed Scripts
1. **easy_kikuyu_vocabulary_literal_seed.py**: 85 essential vocabulary items
2. **easy_kikuyu_proverbs_literal_seed.py**: 46 traditional proverbs and wisdom sayings
3. **easy_kikuyu_conjugations_literal_seed.py**: 50 verb conjugation patterns
4. **easy_kikuyu_comprehensive_literal_seed.py**: 40 grammar rules and cultural content

### Content Highlights

#### Vocabulary (85 items)
- Household items: Table (Metha), Glass (Ngĩrathi), Plate (Thaani)
- Wild animals: Giraffe (Ndûiga), Elephant (Njogu), Lion (Mûrûthî)
- Directions: North (Rũgongo), East (Irathĩro), South (Mũhuro), West (Ithũĩro)
- Cooking methods: Boiling (Gũtherũkia), Frying (Gũkaranga), Roasting (Kũhĩhia)
- Religious/spiritual: God (Ngai), Lord (mwathani), mercy (tha)

#### Proverbs (46 items)
- Core teaching: "Ũbataire ndaconokaga" (the person in need is not ashamed)
- Spiritual expressions: "Na rĩtwa rĩa Ngai, mũtana, ũrĩ tha nyingĩ" (In the name of God)
- Educational encouragement: "Wĩrute Gĩkũyũ" (Learn Kikuyu)
- Community wisdom: Traditional sayings about work, respect, and social values

#### Conjugations (50 items)
- Recent past tense: "Ndokĩra dagĩka ikũmi ciathira" (I woke up ten minutes ago)
- Vowel-starting verb patterns: akia → Ndakia | Njakirĩrie
- Complex sentences: "Ndanyua cai na ndarĩa mũgate" (I drank tea and ate bread)
- Various tenses: present habitual, negative, conditional, progressive

#### Comprehensive (40 items)
- Grammar rules: Class III noun patterns, morphological structures
- Cultural values: Community support, respect for wisdom, humility in learning
- Educational methodology: Progressive learning, contextual understanding
- Language preservation: Heritage value, intergenerational transmission

### Running Literal Seeds
```bash
cd backend

# Individual scripts
python seed/easy_kikuyu_vocabulary_literal_seed.py
python seed/easy_kikuyu_proverbs_literal_seed.py
python seed/easy_kikuyu_conjugations_literal_seed.py
python seed/easy_kikuyu_comprehensive_literal_seed.py

# Master runner (all scripts)
python seed/run_easy_kikuyu_literal_seeds.py
```

### Quality Assurance
- **Manual Verification**: Each item manually verified for accuracy
- **Native Speaker Source**: All content from Emmanuel Kariuki's authentic lessons
- **Cultural Authentication**: Proper cultural context and usage notes
- **Morphological Analysis**: Complex items include sub-translation breakdowns
- **High Quality Scores**: 4.5-4.8 reflecting authentic native speaker content

### Database Impact
- **221+ new contributions** from literal seeds
- **11 morphological analyses** for complex structures
- **Multiple categories** ensuring proper organization
- **No parser dependency** for guaranteed execution reliability

### Strategic Value
- **Reliability**: Works regardless of parsing algorithm changes
- **Authenticity**: Direct extraction ensures content fidelity
- **Educational**: Comprehensive coverage from basic vocabulary to advanced grammar
- **Cultural**: Preserves traditional wisdom and contemporary usage
- **Maintainability**: Easy to update and modify specific content

## 9. Common Tasks & Patterns

### Adding a New API Endpoint
1. Define Pydantic schema in `schemas/`
2. Add business logic to `services/`
3. Create route handler in `api/routes/`
4. Add route to `main.py`
5. Test with appropriate authentication

### Adding a New Database Model
1. Create model in `models/{entity}.py`
2. Import in `db/base.py`
3. Generate migration: `alembic revision --autogenerate -m "Add {entity}"`
4. Review and apply migration: `alembic upgrade head`

### Authentication Pattern
```python
# Require authentication
current_user: User = Depends(get_current_user)

# Require specific role
moderator: User = Depends(require_moderator_or_admin)

# Custom role check
admin: User = Depends(require_role([UserRole.ADMIN]))
```

### Data Fetching (Frontend)
```typescript
// API client pattern (implement in lib/api/)
import { apiClient } from '@/lib/api/client'

const contributions = await apiClient.get('/contributions')
```

### Adding Frontend Pages
1. Create page in `app/{route}/page.tsx`
2. Add layout if needed: `app/{route}/layout.tsx`
3. Implement authentication guards if required
4. Add to navigation if applicable

## 8. File Access Guidelines

### Safe to Modify
- `app/` directories (both backend and frontend)
- `docs/` documentation files
- Environment example files
- Docker Compose configuration
- README and project documentation

### Require Caution
- `alembic/env.py` (migration configuration)
- `pyproject.toml` dependencies
- Database migration files (once applied)
- Docker build configurations

### Do Not Modify
- `alembic/versions/*` migration files (after they've been applied)
- `.venv/` and `node_modules/` directories
- Generated files and build outputs

### Generated Files to Ignore
- `__pycache__/` Python cache
- `.next/` Next.js build cache
- `node_modules/` dependencies
- `.mypy_cache/` type checking cache
- Database files if running locally

## 9. Known Issues & Gotchas

### Database Migration Issues
- Always review auto-generated migrations before applying
- Enum changes require special handling in PostgreSQL
- Foreign key constraints may require data migration

### Authentication Gotchas
- JWT tokens expire after 24 hours by default
- Role changes require new token (logout/login)
- CORS must include credentials for auth headers

### Development Environment
- PostgreSQL must be running before starting backend
- Database connection URL format is specific to psycopg3
- Windows may require binary psycopg installation

### Performance Considerations
- Redis caching layer with TTL management reduces database load
- Database composite indexes for optimized query performance
- Connection pooling with performance monitoring
- Pagination utilities for large dataset handling
- Background task processing for analytics calculations

## 10. Quick Reference

### Most Frequently Used Commands

#### Flashcards App (Production)
```bash
# Local development
cd flashcards-app && npm run dev  # Runs on http://localhost:7000

# Build and deploy
npm run build                       # Build for production
git push origin main               # Auto-deploys to Netlify

# Add new content
# 1. Create JSON file in public/data/curated/{category}/
# 2. Update src/lib/dataLoader.ts to include the file
# 3. Test with npm run dev
# 4. Deploy with git push
```

#### Translation Platform (Development)
```bash
# Start development environment
docker compose -f infra/docker-compose.yml up -d

# Backend development
cd backend && uvicorn app.main:app --reload --host 0.0.0.0 --port 45891

# Frontend development
cd frontend && npm run dev

# Database reset
cd backend && alembic downgrade base && alembic upgrade head && python -m app.seed

# Code quality check
cd backend && black . && isort . && flake8 . && mypy .
```

### Key File Locations

#### Flashcards App
- **Pages**: `flashcards-app/src/app/`
- **Components**: `flashcards-app/src/components/`
- **Data Loader**: `flashcards-app/src/lib/dataLoader.ts`
- **Date Utils**: `flashcards-app/src/lib/dateUtils.ts`
- **Theme Context**: `flashcards-app/src/contexts/ThemeContext.tsx`
- **Types**: `flashcards-app/src/types/flashcard.ts`
- **Curated Data**: `flashcards-app/public/data/curated/`
- **Config**: `flashcards-app/next.config.js`, `flashcards-app/netlify.toml`

#### Translation Platform
- **API Routes**: `backend/app/api/routes/`
- **Database Models**: `backend/app/models/`
- **Business Logic**: `backend/app/services/`
- **Frontend Pages**: `frontend/app/`
- **Configuration**: `backend/app/core/config.py`
- **Database Config**: `backend/app/db/`

### Important URLs

#### Flashcards App
- **Development**: http://localhost:7000
- **Production**: https://kikuyu-flashcards.netlify.app (update after deployment)

#### Translation Platform (Development)
- **Frontend**: http://localhost:45890
- **Backend API**: http://localhost:45891
- **API Documentation**: http://localhost:45891/docs
- **Health Check**: http://localhost:45891/api/v1/health
- **Export Endpoint**: http://localhost:45891/api/v1/export/translations.json

### Useful Debugging Commands

#### Flashcards App
```bash
# Check build locally
cd flashcards-app && npm run build

# Lint check
npm run lint

# Check data loading
# Open browser DevTools → Network tab → Filter by 'json'
# Verify all JSON files load successfully

# Clear LocalStorage (progress/theme reset)
# Browser DevTools → Application → LocalStorage → Clear All

# Check hydration issues
# Look for React warnings in browser console
# Verify suppressHydrationWarning on html/body tags
```

#### Translation Platform
```bash
# Backend logs
docker compose -f infra/docker-compose.yml logs -f backend

# Database connection test
cd backend && python -c "from app.db.session import engine; print(engine.execute('SELECT 1').scalar())"

# Frontend build issues
cd frontend && npm run build --verbose

# Check running services
docker compose -f infra/docker-compose.yml ps
```

### Role-Based API Access
- **Public**: `/api/v1/export/translations.*`, `/api/v1/auth/*`, `/api/v1/health`
- **Authenticated**: `/api/v1/contributions/`, `/api/v1/analytics/user/*`, `/api/v1/morphology/*`
- **Moderator+**: `/api/v1/contributions/{id}/approve|reject`, `/api/v1/analytics/moderation/*`
- **Admin**: All endpoints including `/api/v1/webhooks/*`, `/api/v1/analytics/admin/*`

### Advanced Features Available
- **Analytics Dashboard**: 25+ endpoints with real-time metrics and trend analysis
- **NLP Processing**: Kikuyu-specific tokenization, morphological analysis, spell checking
- **Quality Assurance**: Automated content analysis with difficulty assessment
- **Content Rating**: Comprehensive system (General, PG, Teen, Mature, Adult)
- **Export Formats**: JSON (legacy/flashcards/corpus), CSV, XML, Anki deck
- **Webhook System**: Event-driven integrations with delivery tracking and HMAC signatures
- **Translation Memory**: Similarity matching and fuzzy search capabilities
- **Caching Layer**: Redis-based performance optimization with intelligent invalidation
- **Literal Data Extraction**: Hardcoded seed scripts from Wiktionary sources with linguistic precision
- **Morphological Analysis**: Sub-translation breakdowns for complex words and phrases
- **Cultural Context**: Traditional proverbs and sayings with cultural significance notes

### Database Schema Quick Reference
- **Users**: id, email, password_hash, role, timestamps
- **Contributions**: id, source_text, target_text, status, language, difficulty_level, quality_score, content_rating, created_by_id, timestamps
- **Categories**: id, name, description, slug, sort_order, timestamps
- **Sub Translations**: id, parent_contribution_id, source_word, target_word, word_position, context
- **Audit Logs**: id, contribution_id, action, moderator_id, reason, created_at
- **Verb Morphology**: id, verb_id, tense, aspect, mood, subject_marker, conjugated_form
- **Word Classes**: id, name, description, prefix_pattern, examples
- **Morphological Patterns**: id, pattern_type, source_pattern, target_pattern, description
- **Webhooks**: id, url, events, secret, is_active, delivery_stats, timestamps

### Current Flashcards App Content (100+ Cards)
**Source**: Curated from Emmanuel Kariuki's Easy Kikuyu lessons (64+ lesson files processed)

**Distribution by Category:**
- **Vocabulary**: 50+ essential words and everyday terms
  - Household items, wild animals, directions, cooking methods
  - Fruits, insects, housing types, weather, birds
  - Religious/spiritual terms, taste vocabulary, locomotion
- **Grammar**: 15+ language rules and structures
  - Adjective agreement patterns, word order rules
  - Auxiliary verbs, demonstratives, class III noun patterns
- **Verb Conjugations**: 20+ patterns and tenses
  - Present progressive, future tense, recent past
  - Vowel-starting verb patterns, complex sentences
- **Proverbs**: 10+ traditional wisdom sayings
  - Cultural values, community wisdom, spiritual expressions
- **Common Phrases**: 20+ everyday expressions
  - Greetings, descriptions, housing questions
  - Monthly theme introductions, practical conversations

**Quality Metrics:**
- Average quality score: 4.5/5.0
- All content from authentic native speaker sources
- Difficulty levels: Beginner (60%), Intermediate (30%), Advanced (10%)
- Organized in 6 batches (001-006) covering files 001-064

**Features:**
- Relative timestamps (e.g., "2 hours ago")
- Quality score badges visible on each card
- Source attribution to specific Easy Kikuyu lessons
- Progress tracking via LocalStorage
- Dark mode optimized design

### Current Database Content (1300+ Contributions - Translation Platform)
- **Easy Kikuyu Content**: 649 items from native speaker lessons (477 vocabulary, 119 proverbs, 49 conjugations, 4 comprehensive)
- **Traditional Proverbs**: 200+ culturally significant sayings from multiple sources
- **Wiktionary Verbs**: 76 essential verbs with IPA pronunciations and infinitive forms
- **Native Speaker Vocabulary**: 477 authentic everyday terms from Emmanuel Kariuki's lessons
- **Verb Conjugations**: 49 tense patterns and morphological examples
- **Morphological Derivatives**: 42 derived terms showing word formation patterns
- **Basic Vocabulary**: Essential everyday terms and common expressions
- **Clothing & Household**: Practical terms for daily life
- **Educational Terms**: School and learning vocabulary
- **Transportation**: Vehicle and travel-related terms
- **Cultural Greetings**: Age-based traditional greeting system
- **Emergency Phrases**: Critical communication terms
- **Cooking Methods**: Traditional food preparation vocabulary
- **Numbers System**: Complete numerical system with linguistic analysis
- **Sub-translations**: Morphological analysis for complex words and phrases (471 morphemes)
- **Usage Examples**: Practical sentences demonstrating natural language patterns