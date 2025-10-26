# Curated Content Import Guide

This document explains how to import curated content from the backend into the Android app.

## Overview

The Android app uses a `CuratedContentManager` that automatically loads all JSON files from the `assets/curated-content` directory. The content is organized by category (vocabulary, grammar, conjugations, phrases, proverbs, cultural).

## Directory Structure

```
app/src/main/assets/curated-content/
├── schema.json
├── vocabulary/
│   ├── easy_kikuyu_batch_001_vocab.json
│   ├── easy_kikuyu_batch_002_vocab.json
│   └── ...
├── grammar/
│   ├── easy_kikuyu_batch_002_grammar.json
│   └── ...
├── conjugations/
│   ├── easy_kikuyu_batch_001_conjugations.json
│   └── ...
├── phrases/
│   ├── common_greetings.json
│   ├── easy_kikuyu_batch_002_phrases.json
│   └── ...
├── proverbs/
│   ├── easy_kikuyu_001_proverb.json
│   └── ...
└── cultural/
    ├── easy_kikuyu_batch_001_cultural.json
    └── ...
```

## Import Methods

### Method 1: Using the Update Script (Recommended)

Run the PowerShell script to automatically copy all curated content:

```powershell
powershell -ExecutionPolicy Bypass -File update-curated-content.ps1
```

This script will:
1. Remove existing curated content
2. Copy all JSON files from `../backend/curated-content` to `app/src/main/assets/curated-content`
3. Preserve the directory structure
4. Show a summary of copied files

### Method 2: Manual Copy

Use robocopy to copy files manually:

```cmd
robocopy "../backend/curated-content" "app/src/main/assets/curated-content" "*.json" /S
```

### Method 3: Individual File Copy

Copy specific files or directories as needed:

```cmd
# Copy a specific category
robocopy "../backend/curated-content/vocabulary" "app/src/main/assets/curated-content/vocabulary" "*.json" /S

# Copy a specific file
copy "../backend/curated-content/schema.json" "app/src/main/assets/curated-content/schema.json"
```

## How It Works

1. **Automatic Loading**: The `CuratedContentManager` automatically scans the `assets/curated-content` directory on app startup
2. **Category Organization**: Files are organized by category subdirectories
3. **JSON Parsing**: Each JSON file is parsed using Gson and converted to `FlashcardEntry` objects
4. **Data Access**: The app can access all entries through the `CuratedContentManager`

## Content Format

Each JSON file should follow the curated content schema:

```json
{
  "metadata": {
    "schema_version": "1.0",
    "created_date": "2025-01-01",
    "curator": "curator_name",
    "total_entries": 10
  },
  "entries": [
    {
      "id": "unique_id",
      "kikuyu": "Kikuyu text",
      "english": "English translation",
      "category": "vocabulary",
      "difficulty": "beginner",
      "context": "Usage context",
      "cultural_notes": "Cultural information",
      "examples": [
        {
          "kikuyu": "Example in Kikuyu",
          "english": "Example in English"
        }
      ]
    }
  ]
}
```

## Verification

After importing, verify the content by:

1. **Check File Count**: Ensure all expected files are copied
2. **Test App**: Run the app and verify content loads correctly
3. **Check Logs**: Look for any parsing errors in the Android logs

## Troubleshooting

### Common Issues

1. **Files Not Loading**: Check that files are in the correct directory structure
2. **JSON Parse Errors**: Validate JSON syntax in the source files
3. **Missing Categories**: Ensure all category directories exist
4. **Permission Issues**: Make sure the script has write permissions

### Debug Steps

1. Check the Android logs for `CuratedContentManager` messages
2. Verify file paths and permissions
3. Test with a small subset of files first
4. Ensure JSON files are valid

## Maintenance

- **Regular Updates**: Run the update script when backend content changes
- **Version Control**: Commit the assets directory to version control
- **Backup**: Keep backups of the curated content before major updates
- **Testing**: Always test the app after importing new content

## File Counts

As of the last import:
- **Total Files**: 197 JSON files
- **Categories**: 6 (vocabulary, grammar, conjugations, phrases, proverbs, cultural)
- **Schema**: 1 schema.json file
- **Size**: ~1.18 MB total
