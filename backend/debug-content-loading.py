#!/usr/bin/env python3
"""
Debug Content Loading

This script creates a series of debug files to help diagnose why
the app is only loading a subset of entries.

Usage:
    python debug-content-loading.py

"""

import json
import os
from pathlib import Path
import shutil
from collections import Counter

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
    """Collect all entries from all JSON files and analyze them"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    all_entries = []
    total_entries = 0
    categories = Counter()
    difficulties = Counter()

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

            # Skip all_entries.json and other special files
            if file_path.name in ["all_entries.json", "kikuyu-phrases-all.json"]:
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

                        # Add difficulty if missing
                        if "difficulty" not in entry:
                            entry["difficulty"] = "beginner"

                        # Add to counters
                        categories[entry.get("category", "unknown")] += 1
                        difficulties[entry.get("difficulty", "unknown")] += 1

                        all_entries.append(entry)
                        total_entries += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from all files")
    print("\nCategories:")
    for category, count in sorted(categories.items()):
        print(f"  {category}: {count}")

    print("\nDifficulties:")
    for difficulty, count in sorted(difficulties.items()):
        print(f"  {difficulty}: {count}")

    return all_entries, categories, difficulties

def create_debug_files(project_root, all_entries, categories, difficulties):
    """Create debug files to diagnose content loading issues"""
    # Target directory is android assets
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "debug"

    # Create directory if it doesn't exist
    target_dir.mkdir(parents=True, exist_ok=True)

    # Create a summary file
    summary_file = target_dir / "content_summary.json"
    summary = {
        "total_entries": len(all_entries),
        "categories": dict(categories),
        "difficulties": dict(difficulties)
    }
    with open(summary_file, 'w', encoding='utf-8') as f:
        json.dump(summary, f, indent=2, ensure_ascii=False)

    print(f"Created summary file: {summary_file}")

    # Create separate files for each category
    for category in categories.keys():
        category_entries = [entry for entry in all_entries if entry.get("category") == category]
        category_file = target_dir / f"{category}_entries.json"

        with open(category_file, 'w', encoding='utf-8') as f:
            json.dump({
                "metadata": {
                    "category": category,
                    "total_entries": len(category_entries)
                },
                "entries": category_entries
            }, f, indent=2, ensure_ascii=False)

        print(f"Created category file: {category_file} with {len(category_entries)} entries")

    # Create separate files for each difficulty
    for difficulty in difficulties.keys():
        difficulty_entries = [entry for entry in all_entries if entry.get("difficulty") == difficulty]
        difficulty_file = target_dir / f"{difficulty}_entries.json"

        with open(difficulty_file, 'w', encoding='utf-8') as f:
            json.dump({
                "metadata": {
                    "difficulty": difficulty,
                    "total_entries": len(difficulty_entries)
                },
                "entries": difficulty_entries
            }, f, indent=2, ensure_ascii=False)

        print(f"Created difficulty file: {difficulty_file} with {len(difficulty_entries)} entries")

    # Create a special format file to match the legacy format
    legacy_file = target_dir / "debug_phrases.json"
    legacy_entries = []

    for entry in all_entries:
        legacy_entry = {
            "kikuyu": entry.get("kikuyu", ""),
            "english": entry.get("english", ""),
            "category": entry.get("category", "unknown"),
            "difficulty": entry.get("difficulty", "beginner"),
            "debug_id": entry.get("id", "unknown")
        }
        legacy_entries.append(legacy_entry)

    with open(legacy_file, 'w', encoding='utf-8') as f:
        json.dump(legacy_entries, f, indent=2, ensure_ascii=False)

    print(f"Created legacy format file: {legacy_file}")

def main():
    """Main function"""
    print("Debug Content Loading")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect all entries and analyze
    print("\nCollecting and analyzing entries...")
    all_entries, categories, difficulties = collect_all_entries(project_root)

    # Create debug files
    print("\nCreating debug files...")
    create_debug_files(project_root, all_entries, categories, difficulties)

    print("\nDebug files created.")
    print("Please reinstall the app to ensure the new files are included.")

if __name__ == "__main__":
    main()