# Flagged Translations Processing Workflow

This document explains the workflow for processing flagged translations in the Kikuyu Language Hub project.

## Overview

The Kikuyu Language Hub allows users to flag translations that need to be split into multiple individual entries. This workflow describes the process of processing these flagged translations, splitting them into individual entries, and removing the original entries to avoid duplication.

## Workflow Steps

1. **Identify Flagged Translations**

   Entries marked with "split into subtranslations" in the `flagged_reason` field are candidates for splitting.
   These entries are stored in `backend/curated-content-modern/flagged-translations.txt`.

2. **Process Flagged Translations**

   Run the `process-flagged-translations.py` script to:
   - Read the flagged translations from the text file
   - Split each entry into multiple individual entries based on content patterns
   - Organize entries by category
   - Write output files to the appropriate category directories with the split entries

   ```bash
   python backend/process-flagged-translations.py
   ```

   This creates output files like:
   - `backend/curated-content-modern/vocabulary/split_flagged_vocabulary.json`
   - `backend/curated-content-modern/phrases/split_flagged_phrases.json`

3. **Remove Original Entries**

   Run the `remove-flagged-originals.py` script to:
   - Read the flagged translations from the text file
   - Identify the source files containing the original entries
   - Remove the original entries from their source files
   - Update the metadata counts in the modified files

   ```bash
   python backend/remove-flagged-originals.py
   ```

4. **Update Applications**

   Run the copy scripts to update both the Android and web applications with the modified content:

   ```bash
   python backend/copy-modern-to-android.py
   python backend/copy-modern-to-web.py
   ```

## Script Details

### `process-flagged-translations.py`

This script processes flagged translations and splits them into individual entries based on content patterns.

- **Input**: `flagged-translations.txt` file
- **Output**:
  - Split entries in category-specific files (e.g., `split_flagged_vocabulary.json`)
  - Each entry maintains a reference to its original source through ID structure and cultural notes
  - Split patterns are based on content type (conversations, word lists, paired translations)

### `remove-flagged-originals.py`

This script removes the original flagged entries from their source files to avoid duplicating content.

- **Input**:
  - `flagged-translations.txt` file
  - Original content files containing the flagged entries
- **Output**:
  - Modified content files with flagged entries removed
  - Updated metadata counts in the modified files

## Best Practices

1. Always run both scripts in sequence to ensure content integrity
2. Always run the copy scripts after processing to update both applications
3. Verify the split entries before removing the originals
4. Consider backing up the original content files before running the removal script

## Troubleshooting

- If the JSON parsing fails, the scripts will attempt to fix common formatting issues
- If the automatic ID-based source file detection fails, the script will search all files in the category
- If entries are not found, check the format of the entry IDs in the flagged translations file