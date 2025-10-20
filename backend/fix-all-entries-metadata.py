#!/usr/bin/env python3
"""
Fix all_entries.json with Proper Metadata

This script adds the required metadata structure to all_entries.json
so it can be properly parsed by the Android app.
"""

import json
from pathlib import Path

def fix_all_entries_json():
    """Add proper metadata to all_entries.json"""
    android_file = Path("C:/myrepos/kikuyu-language-hub/android-kikuyuflashcards/app/src/main/assets/curated-content/all_entries.json")

    print("Reading current all_entries.json...")
    with open(android_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    entries = data.get('entries', [])
    print(f"Found {len(entries)} entries")

    # Create proper metadata structure
    curated_content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": "2025-10-20T00:00:00.000000+00:00",
            "last_updated": "2025-10-20T00:00:00.000000+00:00",
            "curator": "Backend Script",
            "source_files": ["backend-curated-content-modern"],
            "total_entries": len(entries),
            "description": "All cleaned entries from backend curated content with proper metadata structure for Android parsing"
        },
        "entries": entries
    }

    print("Adding proper metadata structure...")

    # Create backup
    backup_file = android_file.with_suffix('.json.metadata-backup')
    with open(backup_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    print(f"Created backup: {backup_file}")

    # Write fixed version
    with open(android_file, 'w', encoding='utf-8') as f:
        json.dump(curated_content, f, indent=2, ensure_ascii=False)

    print(f"Updated {android_file} with proper metadata structure")
    print(f"File now has {len(curated_content['entries'])} entries with complete metadata")
    return curated_content

if __name__ == "__main__":
    fix_all_entries_json()