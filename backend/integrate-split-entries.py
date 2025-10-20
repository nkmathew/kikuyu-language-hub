#!/usr/bin/env python3
"""
Integrate Split Entries Script

This script takes the split entries from manual_split_*.json and split_flagged_*.json
files and integrates them back into their original source files. This ensures that
the Android app can see all entries in both study and flashcard modes.

Usage:
    python integrate-split-entries.py

"""

import json
import re
import os
from pathlib import Path
import datetime

# Constants
CURATED_CONTENT_DIR = "curated-content-modern"
SPLIT_FILE_PATTERNS = ["manual_split_*.json", "split_flagged_*.json"]

def find_project_root():
    """Find the project root directory by looking for key markers"""
    # Start with the directory containing this script
    current_dir = Path(__file__).resolve().parent

    # Go up until we find the project root
    # We'll identify the root by the presence of both 'backend' and 'android-kikuyuflashcards' directories
    max_levels = 5  # Prevent infinite loop if markers are never found
    for _ in range(max_levels):
        # If we're in the backend directory, check one level up
        if current_dir.name == "backend":
            potential_root = current_dir.parent
        else:
            potential_root = current_dir

        # Check if we have the expected project structure
        if (potential_root / "backend").exists() and (potential_root / "android-kikuyuflashcards").exists():
            return potential_root

        # Move up a directory
        if current_dir.parent == current_dir:  # Root of filesystem
            break
        current_dir = current_dir.parent

    # If we got here, we couldn't find the project root
    print("Error: Could not find project root directory.")
    print("This script must be run from within the kikuyu-language-hub repository.")
    exit(1)

def load_json_file(file_path):
    """Load a JSON file and return the parsed content"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading {file_path}: {e}")
        return None

def save_json_file(file_path, content):
    """Save content to a JSON file"""
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(content, f, indent=2, ensure_ascii=False)
        return True
    except Exception as e:
        print(f"Error saving {file_path}: {e}")
        return False

def find_split_files(project_root):
    """Find all split files matching the patterns"""
    split_files = []
    backend_dir = project_root / "backend"

    for pattern in SPLIT_FILE_PATTERNS:
        for category_dir in (backend_dir / CURATED_CONTENT_DIR).glob("*"):
            if category_dir.is_dir():
                split_files.extend(category_dir.glob(pattern))

    return split_files

def find_batch_files(project_root):
    """Find all batch files in the curated content directory"""
    batch_files = []
    backend_dir = project_root / "backend"

    for category_dir in (backend_dir / CURATED_CONTENT_DIR).glob("*"):
        if category_dir.is_dir():
            batch_files.extend(category_dir.glob("easy_kikuyu_batch_*_*.json"))

    return batch_files

def extract_batch_info(entry_id):
    """Extract category and batch number from an entry ID"""
    # Examples: vocab-010-001, phrase-019-001
    match = re.match(r'([a-z]+)-(\d+)(?:-\d+)?(?:_\d+)?$', entry_id)

    if match:
        category_short, batch_num = match.groups()
        batch_num = int(batch_num)

        # Map short category name to full name
        category_map = {
            "vocab": "vocabulary",
            "phrase": "phrases",
            "grammar": "grammar",
            "proverb": "proverbs",
            "conj": "conjugations",
            "cultural": "cultural"
        }

        category = category_map.get(category_short, category_short)
        return category, batch_num

    return None, None

def find_source_file_for_entry(entry_id, batch_files):
    """Find the source file for an entry based on its ID"""
    category, batch_num = extract_batch_info(entry_id)

    if not category or not batch_num:
        print(f"  Warning: Could not extract category and batch from ID: {entry_id}")
        return None

    # Try different file name patterns
    for file_path in batch_files:
        file_name = file_path.name

        # Pattern 1: easy_kikuyu_batch_XXX_category.json
        if category in file_name and f"batch_{batch_num:03d}_" in file_name:
            return file_path

        # Pattern 2: easy_kikuyu_batch_XX_category.json (2-digit batch)
        if category in file_name and f"batch_{batch_num:02d}_" in file_name:
            return file_path

        # Pattern 3: easy_kikuyu_batch_X_category.json (1-digit batch)
        if category in file_name and f"batch_{batch_num}_" in file_name:
            return file_path

        # More flexible matching - just check for the batch number anywhere in the name
        if category in file_name and f"_{batch_num}_" in file_name:
            return file_path

        # Even more flexible - check for batch number at the end
        if category in file_name and file_name.endswith(f"_{batch_num}_{category}.json"):
            return file_path

    # If none of the above patterns matched, use a different approach - create a new consolidated file
    output_dir = Path(file_path).parent
    consolidated_file = output_dir / f"consolidated_batch_{batch_num:02d}_{category}.json"

    # If this is the first time we're seeing this batch/category combo, create a new file
    if str(consolidated_file) not in getattr(find_source_file_for_entry, "created_files", set()):
        # Initialize the set if it doesn't exist
        if not hasattr(find_source_file_for_entry, "created_files"):
            find_source_file_for_entry.created_files = set()

        # Mark this file as created
        find_source_file_for_entry.created_files.add(str(consolidated_file))

        # Create a new consolidated file with minimal structure
        now = datetime.datetime.now().isoformat()
        new_content = {
            "metadata": {
                "schema_version": "1.0",
                "created_date": now,
                "curator": "Split Entry Integration Script",
                "source_files": ["flagged-translations.txt"],
                "total_entries": 0,
                "description": f"Consolidated entries for {category} batch {batch_num}"
            },
            "entries": []
        }

        # Save the new file
        with open(consolidated_file, 'w', encoding='utf-8') as f:
            json.dump(new_content, f, indent=2, ensure_ascii=False)

        print(f"  Created new consolidated file: {consolidated_file}")

    return consolidated_file

def integrate_split_entries(project_root):
    """Integrate split entries into their original source files"""
    # Find all split files
    split_files = find_split_files(project_root)
    if not split_files:
        print("No split files found.")
        return

    print(f"Found {len(split_files)} split files.")

    # Find all batch files
    batch_files = find_batch_files(project_root)
    if not batch_files:
        print("No batch files found.")
        return

    print(f"Found {len(batch_files)} batch files.")

    # Load split entries
    split_entries = []
    for file_path in split_files:
        content = load_json_file(file_path)
        if content and "entries" in content:
            split_entries.extend(content["entries"])

    print(f"Loaded {len(split_entries)} split entries.")

    # Group entries by source file
    source_file_entries = {}
    for entry in split_entries:
        entry_id = entry.get("id", "")

        # For entries with _01, _02, etc. suffixes, get the base ID
        base_id = re.sub(r'_\d+$', '', entry_id)

        # Find source file
        source_file = find_source_file_for_entry(base_id, batch_files)
        if source_file:
            if source_file not in source_file_entries:
                source_file_entries[source_file] = []
            source_file_entries[source_file].append(entry)

    # Update source files with split entries
    files_updated = 0
    entries_added = 0

    for source_file, entries in source_file_entries.items():
        # Load source file
        source_content = load_json_file(source_file)
        if not source_content:
            continue

        original_entry_count = len(source_content.get("entries", []))

        # Check which entries already exist
        existing_ids = set(entry.get("id") for entry in source_content.get("entries", []))

        # Add new entries
        new_entries = []
        for entry in entries:
            entry_id = entry.get("id")
            if entry_id not in existing_ids:
                new_entries.append(entry)
                existing_ids.add(entry_id)

        # Update source file
        if new_entries:
            source_content["entries"].extend(new_entries)

            # Update metadata
            if "metadata" in source_content:
                source_content["metadata"]["total_entries"] = len(source_content["entries"])
                source_content["metadata"]["last_updated"] = datetime.datetime.now().isoformat()

                # Add note about integration
                description = source_content["metadata"].get("description", "")
                if "Integrated split entries" not in description:
                    source_content["metadata"]["description"] = description + " (Integrated split entries)"

            # Save updated file
            if save_json_file(source_file, source_content):
                files_updated += 1
                entries_added += len(new_entries)
                print(f"Updated {source_file} with {len(new_entries)} entries (total now: {len(source_content.get('entries', []))}, original: {original_entry_count})")

    print(f"\nIntegration complete: Updated {files_updated} files with {entries_added} entries.")

    return {
        "files_updated": files_updated,
        "entries_added": entries_added
    }

def main():
    """Main function"""
    print("Integrating Split Entries into Original Files")
    print("=" * 50)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Integrate split entries
    print("\nIntegrating split entries...")
    result = integrate_split_entries(project_root)

    if result:
        print("\nNext steps:")
        print("1. Run the Android and web copy scripts to update the apps:")
        print("   - python backend/copy-modern-to-android.py")
        print("   - python backend/copy-modern-to-web.py")

if __name__ == "__main__":
    main()