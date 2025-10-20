#!/usr/bin/env python3
"""
Create Direct Asset File

This script creates a special file that will be loaded directly by the Android app
without any filtering or processing. It places the file in a location where the app
will find it and load it directly.

Usage:
    python create-direct-asset-file.py

"""

import json
import os
import glob
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
            # Skip schema file
            if file_path.name == "schema.json":
                continue

            # Skip existing all_entries.json files
            if file_path.name == "all_entries.json":
                continue

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    # Add each entry to the all_entries list
                    for entry in content["entries"]:
                        # Make sure the entry has the correct category
                        if "category" in entry:
                            entry["category"] = category
                        all_entries.append(entry)
                        total_entries += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from all files")
    return all_entries

def create_android_asset_file(project_root, all_entries):
    """Create a special file directly in the Android assets directory"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "kikuyu_flashcards.json"

    # Create a simplified format for the entries
    simplified_content = {
        "version": "1.0",
        "count": len(all_entries),
        "phrases": all_entries
    }

    # Save to file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(simplified_content, f, indent=2, ensure_ascii=False)

    print(f"Created direct asset file with {len(all_entries)} entries: {target_file}")
    return target_file

def create_phrases_json(project_root, all_entries):
    """Create a phrases.json file that the app may be looking for"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "phrases.json"

    # Create a simplified format for the entries
    simplified_content = []

    for entry in all_entries:
        simplified_entry = {
            "kikuyu": entry.get("kikuyu", ""),
            "english": entry.get("english", "")
        }
        simplified_content.append(simplified_entry)

    # Save to file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(simplified_content, f, indent=2, ensure_ascii=False)

    print(f"Created phrases.json file with {len(simplified_content)} entries: {target_file}")
    return target_file

def create_default_format_file(project_root, all_entries):
    """Create a file in the default format that the app might expect"""
    # Target is both the backend curated content and Android assets directories
    backend_dir = project_root / "backend" / "curated-content-modern"
    target_file = backend_dir / "kikuyu-phrases-all.json"

    # Create content in the format used by the app's original example files
    content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": "2025-10-20T00:00:00.000000+00:00",
            "curator": "Direct Asset File Script",
            "source_files": ["all_files"],
            "total_entries": len(all_entries),
            "description": "Complete collection of all phrases for direct loading"
        },
        "entries": all_entries
    }

    # Save to backend file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(content, f, indent=2, ensure_ascii=False)
    print(f"Created default format file: {target_file}")

    # Copy to Android assets
    android_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"
    android_file = android_dir / "kikuyu-phrases-all.json"

    # Make sure the directory exists
    android_dir.mkdir(parents=True, exist_ok=True)

    # Copy the file
    shutil.copy(target_file, android_file)
    print(f"Copied to Android assets: {android_file}")

    return target_file

def main():
    """Main function"""
    print("Creating Direct Asset Files")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect all entries
    print("Collecting all entries...")
    all_entries = collect_all_entries(project_root)

    # Create special files
    print("\nCreating special files...")
    android_file = create_android_asset_file(project_root, all_entries)
    phrases_file = create_phrases_json(project_root, all_entries)
    default_file = create_default_format_file(project_root, all_entries)

    print("\nDirect asset files created.")
    print("Please reinstall the app to ensure the new files are included.")

if __name__ == "__main__":
    main()