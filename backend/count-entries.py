#!/usr/bin/env python3
"""
Count Entries in Content Files

This script counts the total number of entries in each category and file,
to help understand why the Android app is only showing 380 entries.

Usage:
    python count-entries.py

"""

import json
import os
import re
import glob
from pathlib import Path
import argparse

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

def count_entries(project_root, android_assets=False):
    """Count entries in all content files"""
    # Choose the directory to analyze
    base_dir = project_root
    if android_assets:
        dir_path = base_dir / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"
        print(f"Analyzing files in Android assets: {dir_path}")
    else:
        dir_path = base_dir / "backend" / "curated-content-modern"
        print(f"Analyzing files in backend: {dir_path}")

    # Find all JSON files
    json_files = list(dir_path.glob("**/*.json"))
    print(f"Found {len(json_files)} JSON files")

    # Count entries by category and file type
    total_entries = 0
    categories = {}
    file_types = {
        "original": {"count": 0, "entries": 0},
        "consolidated": {"count": 0, "entries": 0},
        "manual_split": {"count": 0, "entries": 0},
        "split_flagged": {"count": 0, "entries": 0},
        "other": {"count": 0, "entries": 0}
    }

    # Print details for each file
    print("\nFile details:")
    print("{:<60} {:<15} {:<10}".format("File", "Category", "Entries"))
    print("-" * 85)

    # Process each file
    for file_path in sorted(json_files):
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = json.load(f)

            # Get metadata and entry count
            metadata = content.get("metadata", {})
            entry_count = metadata.get("total_entries", 0)

            # If total_entries is 0, count the entries manually
            if entry_count == 0 and "entries" in content:
                entry_count = len(content["entries"])

            # Update category counts
            category = file_path.parent.name
            if category not in categories:
                categories[category] = 0
            categories[category] += entry_count

            # Update file type counts
            file_name = file_path.name
            if "consolidated_batch" in file_name:
                file_type = "consolidated"
            elif "manual_split" in file_name:
                file_type = "manual_split"
            elif "split_flagged" in file_name:
                file_type = "split_flagged"
            elif "batch" in file_name:
                file_type = "original"
            else:
                file_type = "other"

            file_types[file_type]["count"] += 1
            file_types[file_type]["entries"] += entry_count

            # Print details
            print("{:<60} {:<15} {:<10}".format(
                str(file_path.relative_to(dir_path)),
                category,
                entry_count
            ))

            # Update total count
            total_entries += entry_count

        except Exception as e:
            print(f"Error processing {file_path}: {e}")

    # Print summary
    print("\nSummary:")
    print("-" * 40)
    print("Total entries:", total_entries)
    print("\nEntries by category:")
    for category, count in sorted(categories.items()):
        print(f"  {category}: {count}")

    print("\nFile types:")
    for file_type, data in file_types.items():
        print(f"  {file_type}: {data['count']} files, {data['entries']} entries")

    return total_entries, categories, file_types

def main():
    parser = argparse.ArgumentParser(description='Count entries in content files')
    parser.add_argument('--android', action='store_true', help='Count entries in Android assets instead of backend files')
    args = parser.parse_args()

    print("Content File Analysis")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Count entries
    count_entries(project_root, android_assets=args.android)

if __name__ == "__main__":
    main()