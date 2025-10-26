# Curated Content - Source of Truth

This directory (`backend/curated-content/`) is the **single source of truth** for all curated Kikuyu language flashcard content.

## Directory Structure

```
backend/curated-content/
├── conjugations/         # Verb conjugation flashcards
├── cultural/            # Cultural context and traditions
├── grammar/             # Grammar rules and patterns
├── phrases/             # Common phrases and expressions
├── proverbs/            # Traditional Kikuyu proverbs
├── vocabulary/          # Vocabulary flashcards
├── schema.json          # JSON schema for flashcard format
├── CURATION_SUMMARY.md  # Overall curation summary
├── EASY_KIKUYU_BATCH_001.md  # Batch 001 documentation
└── EASY_KIKUYU_PROGRESS.md   # Curation progress tracker
```

## Syncing to Other Apps

Content from this directory is automatically synced to:
- **Next.js App**: `flashcards-app/public/data/curated/`
- **React Native App**: `kikuyu-flashcards-mobile/src/assets/data/curated/`

### How to Sync

**All platforms (requires Python 3.8+ with `rich`):**
```bash
python sync-curated-content.py
```

Install requirements if needed:
```bash
pip install rich
```

### After Making Changes

1. ✅ **ALWAYS** edit content in `backend/curated-content/` only
2. ✅ Run the sync script to copy to other apps
3. ✅ Commit all changes (backend source + synced copies)

### Important Notes

⚠️ **DO NOT** edit curated content directly in:
- `flashcards-app/public/data/curated/`
- `kikuyu-flashcards-mobile/src/assets/data/curated/`

These directories are gitignored and will be overwritten by the sync script.

The sync script provides a detailed summary showing:
- ✅ File counts per category
- ✅ Sync status for each app
- ✅ Total files synced
- ✅ Timestamp of sync operation

## Content Categories

### Vocabulary
Batch files containing vocabulary flashcards extracted from Easy Kikuyu lessons.
- Format: `easy_kikuyu_batch_[XXX]_vocab.json`
- Batches: 001-054 (538 files, 100% complete)

### Conjugations
Verb conjugation patterns and examples.
- Format: `easy_kikuyu_batch_[XXX]_conjugations.json`

### Phrases
Common phrases, greetings, and conversational expressions.
- Format: `easy_kikuyu_batch_[XXX]_phrases.json`

### Proverbs
Traditional Kikuyu proverbs and wisdom sayings.
- Format: `easy_kikuyu_batch_[XXX]_proverbs.json`

### Grammar
Grammar rules, patterns, and linguistic explanations.
- Format: `easy_kikuyu_batch_[XXX]_grammar.json`

### Cultural
Cultural context, traditions, and customs.
- Format: `easy_kikuyu_batch_[XXX]_cultural.json`

## Source Attribution

All content from **Easy Kikuyu** by Emmanuel Kariuki
- Facebook: @EasyKikuyu
- © Emmanuel Kariuki

## Schema

See `schema.json` for the complete flashcard JSON schema definition.
