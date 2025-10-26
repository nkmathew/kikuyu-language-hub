# CLAUDE.md - Kikuyu Language Hub

## Context Limits

- **Maximum Token Limit**: 200,000 tokens (Claude 3.7 Sonnet)
- **Maximum JSON Object Size**: 100 MB
- **Maximum Files Per Session**: 500 files
- **Maximum Content Depth**: Unlimited (will process all nested structures)

## Project Overview

The Kikuyu Language Hub consists of:

1. **Flashcards App (PRODUCTION)**: Next.js application with curated flashcards for learning Kikuyu.
2. **Android Mobile App (PRODUCTION)**: Native Android application with offline learning capabilities.
3. **Translation Platform (DEVELOPMENT)**: FastAPI backend for collaborative content creation.

## Key Technologies

- **Frontend**: Next.js 15.5.4, TypeScript 5.9.2, Tailwind CSS
- **Mobile**: React Native with Expo, AsyncStorage
- **Backend**: FastAPI, PostgreSQL, SQLAlchemy
- **Data**: 196 curated JSON files with vocabulary, grammar, conjugations, and phrases
- **Deployment**: Netlify (web), EAS Build (mobile), Docker Compose (backend)

## Directory Structure

```
kikuyu-language-hub/
├── flashcards-app/            # Web flashcards app
├── android-kikuyuflashcards/  # Native Android app
├── backend/                   # FastAPI backend
│   ├── app/                   # API routes, models, services
│   ├── seed/                  # Seed scripts
│   └── alembic/               # Migrations
├── raw-data/                  # Source materials
└── docs/                      # Documentation
```

## Development Commands

```bash
# Web App
cd flashcards-app
npm install
npm run dev

# Android App
cd android-kikuyuflashcards
./gradlew installDebug

# Backend
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
uvicorn app.main:app --reload
```

## File Patterns

- **Batched Content**: `backend/curated-content-modern/{category}/easy_kikuyu_batch_{number}_{category}.json`
- **Flagged Translations**: Managed in `FlaggedTranslationsActivity.kt` with position badges and count header
- **Sync Script**: `sync-curated-content.py` syncs content to mobile app

## Common Tasks

- Adding flashcards: Update JSON files in `backend/curated-content-modern/`
- Flagged translations: Review and fix issues reported by users
- Mobile UI updates: Edit Android layouts and adapters

## Key Features

- **Study Modes**: Flashcard Mode, List Mode, Sort Options
- **Categories**: Vocabulary, Proverbs, Grammar, Conjugations, Phrases
- **Flagged Translations**: Mark, export, and share problematic content
- **Offline Learning**: All content bundled in APK for offline access