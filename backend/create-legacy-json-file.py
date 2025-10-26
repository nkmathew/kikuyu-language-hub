#!/usr/bin/env python3
"""
Create Legacy JSON File

This script creates a legacy kikuyu-phrases.json file with all entries
from the curated content files. This bypasses the modern loading mechanism
and works with the legacy FlashCardManager class.

Usage:
    python create-legacy-json-file.py

"""

import json
import os
from pathlib import Path
import shutil

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

def collect_all_entries(project_root):
    """Collect all entries from all JSON files"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    all_entries = []
    total_entries = 0

    # Process each category
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name

        # Process each file in the category
        for file_path in category_dir.glob("*.json"):
            # Skip schema file and special files
            if file_path.name in ["schema.json", "all_entries.json", "kikuyu-phrases-all.json"]:
                continue

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    for entry in content["entries"]:
                        # Add category if missing
                        if "category" not in entry:
                            entry["category"] = category

                        all_entries.append(entry)
                        total_entries += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from all files")
    return all_entries

def create_legacy_json_file(project_root, all_entries):
    """Create a legacy JSON file with all entries"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "kikuyu-phrases.json"

    # Create simplified entries in legacy format
    legacy_entries = []
    for entry in all_entries:
        legacy_entry = {
            "kikuyu": entry.get("kikuyu", ""),
            "english": entry.get("english", ""),
            "category": entry.get("category", "general")  # Include category
        }
        legacy_entries.append(legacy_entry)

    # First create a legacy object with a phrases array
    legacy_object = {"phrases": legacy_entries}

    # Save as array to match expected format
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(legacy_entries, f, indent=2, ensure_ascii=False)

    print(f"Created legacy JSON file with {len(legacy_entries)} entries: {target_file}")
    return target_file

def main():
    """Main function"""
    print("Creating Legacy JSON File")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect all entries
    print("Collecting all entries...")
    all_entries = collect_all_entries(project_root)

    # Create legacy JSON file
    print("\nCreating legacy JSON file...")
    legacy_file = create_legacy_json_file(project_root, all_entries)

    print("\nLegacy JSON file created.")
    print("Please rebuild and reinstall the app to ensure the new file is included.")

if __name__ == "__main__":
    main()