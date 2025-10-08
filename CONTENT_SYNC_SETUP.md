# Content Sync Setup Documentation

## Overview

This document explains the content synchronization system that maintains consistency across multiple apps in the Kikuyu Language Hub project.

## Problem Solved

Previously, curated flashcard content existed in three separate locations:
- `backend/curated-content/`
- `flashcards-app/public/data/curated/`
- `kikuyu-flashcards-mobile/src/assets/data/curated/`

This created risks:
- ❌ Inconsistent data across apps
- ❌ Accidental edits in wrong location
- ❌ Merge conflicts
- ❌ Confusion about which version is correct

## Solution

**Single Source of Truth**: `backend/curated-content/`

All curated content is now:
1. ✅ Edited only in `backend/curated-content/`
2. ✅ Synced to other apps via script
3. ✅ Other directories gitignored to prevent accidental edits

## Directory Structure

```
kikuyu-language-hub/
├── backend/
│   └── curated-content/           # ✅ SOURCE OF TRUTH (tracked by git)
│       ├── conjugations/
│       ├── cultural/
│       ├── grammar/
│       ├── phrases/
│       ├── proverbs/
│       ├── vocabulary/
│       ├── schema.json
│       ├── README.md
│       ├── CURATION_SUMMARY.md
│       ├── EASY_KIKUYU_BATCH_001.md
│       └── EASY_KIKUYU_PROGRESS.md
├── flashcards-app/
│   └── public/data/curated/       # ❌ SYNCED COPY (gitignored)
│       └── [same structure]
└── kikuyu-flashcards-mobile/
    └── src/assets/data/curated/   # ❌ SYNCED COPY (gitignored)
        └── [same structure]
```

## Sync Scripts

Two scripts are provided for different environments:

### Windows (Git Bash/MSYS)
```bash
./sync-curated-content.bat
```

### Linux/macOS
```bash
chmod +x sync-curated-content.sh
./sync-curated-content.sh
```

## Workflow

### Adding/Editing Content

1. **Edit source files** in `backend/curated-content/`:
   ```bash
   cd backend/curated-content
   # Edit files in conjugations/, vocabulary/, etc.
   ```

2. **Run sync script** to copy to other apps:
   ```bash
   # From project root
   ./sync-curated-content.bat  # Windows
   # OR
   ./sync-curated-content.sh   # Linux/macOS
   ```

3. **Verify sync** worked:
   ```bash
   # Check that files were copied
   ls flashcards-app/public/data/curated/vocabulary/
   ls kikuyu-flashcards-mobile/src/assets/data/curated/vocabulary/
   ```

4. **Test locally**:
   ```bash
   cd flashcards-app
   npm run dev
   # Visit http://localhost:3000 and verify new content appears
   ```

5. **Commit everything**:
   ```bash
   git add backend/curated-content/
   git add sync-curated-content.*
   git add README.md .gitignore
   git commit -m "Add new curated content and sync to apps"
   git push
   ```

### What Gets Committed

✅ **Tracked by git**:
- `backend/curated-content/**/*` (source of truth)
- `sync-curated-content.bat` (Windows sync script)
- `sync-curated-content.sh` (Linux/macOS sync script)
- `.gitignore` (configuration)

❌ **Gitignored** (auto-synced):
- `flashcards-app/public/data/curated/**/*`
- `kikuyu-flashcards-mobile/src/assets/data/curated/**/*`

## Sync Script Details

### What Gets Synced

The script copies these directories:
- `conjugations/`
- `cultural/`
- `grammar/`
- `phrases/`
- `proverbs/`
- `vocabulary/`

And these files:
- `schema.json`
- `CURATION_SUMMARY.md`
- `EASY_KIKUYU_BATCH_001.md`
- `EASY_KIKUYU_PROGRESS.md`

### Sync Behavior

- **Windows (`xcopy`)**: Copies files and overwrites existing
- **Linux/macOS (`rsync`)**: Syncs with `--delete` flag (removes files not in source)

## Git Configuration

### .gitignore Changes

Added to `.gitignore`:
```gitignore
# Curated content (source of truth: backend/curated-content/)
# Other copies are synced via sync-curated-content script
flashcards-app/public/data/curated/
kikuyu-flashcards-mobile/src/assets/data/curated/
```

## Troubleshooting

### "Content not appearing in app"

1. Check source exists:
   ```bash
   ls backend/curated-content/vocabulary/your_file.json
   ```

2. Run sync script:
   ```bash
   ./sync-curated-content.bat
   ```

3. Verify copied:
   ```bash
   ls flashcards-app/public/data/curated/vocabulary/your_file.json
   ```

4. Check dataLoader.ts includes the file:
   ```bash
   grep "your_file.json" flashcards-app/src/lib/dataLoader.ts
   ```

### "Accidentally edited wrong directory"

1. Discard changes in synced directory:
   ```bash
   git checkout flashcards-app/public/data/curated/
   ```

2. Make edits in backend:
   ```bash
   # Edit backend/curated-content/your_file.json
   ```

3. Re-sync:
   ```bash
   ./sync-curated-content.bat
   ```

### "Merge conflicts in curated content"

This shouldn't happen anymore since synced directories are gitignored. If it does:

1. Accept backend version (source of truth)
2. Run sync script to propagate

## Migration Notes

### What Was Done

1. ✅ Copied `EASY_KIKUYU_PROGRESS.md` from flashcards-app to backend
2. ✅ Created sync scripts (Windows + Linux)
3. ✅ Updated `.gitignore` to ignore synced directories
4. ✅ Ran initial sync to populate all apps
5. ✅ Created `backend/curated-content/README.md`
6. ✅ Updated main `README.md` with sync instructions
7. ✅ Created this documentation

### Current State

- **Backend**: 54 vocabulary batches (001-054), complete set
- **Flashcards App**: Synced from backend
- **Mobile App**: Synced from backend
- **All apps**: Now in sync! 🎉

## Best Practices

1. ✅ **ALWAYS** edit in `backend/curated-content/` only
2. ✅ Run sync script after every edit
3. ✅ Test in flashcards-app before committing
4. ✅ Commit backend source + scripts (git will ignore synced copies)
5. ✅ Document major changes in CURATION_SUMMARY.md

## Future Improvements

- [ ] Add pre-commit hook to auto-run sync
- [ ] Add CI/CD validation that synced content matches source
- [ ] Create npm script `npm run sync-content` in flashcards-app
- [ ] Add content validation script (check schema compliance)

## Questions?

See:
- `backend/curated-content/README.md` - Content organization
- `README.md` - Project overview
- `CLAUDE.md` - Development guidelines
