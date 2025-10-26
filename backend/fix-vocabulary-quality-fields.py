#!/usr/bin/env python3
"""
Fix Vocabulary Quality Fields

This script fixes the problematic quality fields in vocabulary files that are causing
JSON parsing errors in the Android app.
"""

import json
from pathlib import Path

def fix_quality_fields():
    """Fix quality fields from numbers to proper objects"""
    curated_dir = Path("../android-kikuyuflashcards/app/src/main/assets/curated-content")

    # Files that have quality parsing issues
    problematic_files = [
        "vocabulary/easy_kikuyu_batch_999_vocabulary.json",
        "vocabulary/numbered_entries.json",
        "vocabulary/study_mode_vocab.json"
    ]

    for file_path in problematic_files:
        full_path = curated_dir / file_path
        if not full_path.exists():
            print(f"File not found: {file_path}")
            continue

        print(f"\nFixing {file_path}...")

        # Read and parse
        with open(full_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        if not isinstance(data, dict) or 'entries' not in data:
            print(f"  No entries found in {file_path}")
            continue

        # Fix quality fields
        entries = data['entries']
        fixed_count = 0

        for i, entry in enumerate(entries):
            if not isinstance(entry, dict):
                continue

            if 'quality' in entry and not isinstance(entry['quality'], dict):
                old_quality = entry['quality']
                # Convert to proper QualityInfo structure
                entry['quality'] = {
                    "verified": old_quality >= 0.8 if isinstance(old_quality, (int, float)) else True,
                    "confidence_score": float(old_quality) if isinstance(old_quality, (int, float)) else 1.0,
                    "source_quality": "auto" if isinstance(old_quality, (int, float)) else "manual",
                    "reviewer": None,
                    "review_date": None
                }
                fixed_count += 1

        print(f"  Fixed {fixed_count} quality fields in {len(entries)} entries")

        # Create backup
        backup_path = full_path.with_suffix('.json.quality-backup')
        with open(backup_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"  Created backup: {backup_path}")

        # Write fixed file
        with open(full_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)

        print(f"  Updated {file_path}")

if __name__ == "__main__":
    fix_quality_fields()