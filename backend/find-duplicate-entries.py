#!/usr/bin/env python3
"""
Find Duplicate Entries

This script analyzes all content files to identify duplicate entries based on
various criteria such as ID, content, or both. This helps understand why the
total entries count is higher than expected.

Usage:
    python find-duplicate-entries.py

"""

import json
import os
import glob
from pathlib import Path
from collections import defaultdict

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

def collect_entries_by_file(project_root):
    """Collect entries from all files, grouped by file path"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    entries_by_file = {}
    total_entries = 0
    file_count = 0

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

            # Skip the all_entries.json file
            if file_path.name == "all_entries.json":
                continue

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    relative_path = file_path.relative_to(dir_path)
                    entries = content["entries"]
                    entries_by_file[str(relative_path)] = entries
                    total_entries += len(entries)
                    file_count += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from {file_count} files")
    return entries_by_file

def find_duplicate_entries(entries_by_file):
    """Find entries that appear in multiple files"""
    # Track entries by ID
    entries_by_id = defaultdict(list)

    # Track entries by content (kikuyu and english)
    entries_by_content = defaultdict(list)

    # Process each file
    for file_path, entries in entries_by_file.items():
        for entry in entries:
            # Track by ID
            entry_id = entry.get("id")
            if entry_id:
                entries_by_id[entry_id].append((file_path, entry))

            # Track by content
            kikuyu = entry.get("kikuyu", "").strip()
            english = entry.get("english", "").strip()
            if kikuyu and english:
                content_key = f"{kikuyu}|{english}"
                entries_by_content[content_key].append((file_path, entry))

    # Find duplicate entries by ID
    duplicate_ids = {id: entries for id, entries in entries_by_id.items() if len(entries) > 1}

    # Find duplicate entries by content
    duplicate_contents = {content: entries for content, entries in entries_by_content.items() if len(entries) > 1}

    # Count totals
    unique_ids = len(entries_by_id)
    unique_contents = len(entries_by_content)
    duplicate_id_count = len(duplicate_ids)
    duplicate_content_count = len(duplicate_contents)
    duplicate_content_entries = sum(len(entries) for entries in duplicate_contents.values())
    duplicate_id_entries = sum(len(entries) for entries in duplicate_ids.values())

    return {
        "unique_ids": unique_ids,
        "unique_contents": unique_contents,
        "duplicate_ids": duplicate_ids,
        "duplicate_contents": duplicate_contents,
        "duplicate_id_count": duplicate_id_count,
        "duplicate_content_count": duplicate_content_count,
        "duplicate_id_entries": duplicate_id_entries,
        "duplicate_content_entries": duplicate_content_entries
    }

def analyze_duplication_patterns(duplicate_info):
    """Analyze patterns in the duplication"""
    # Analyze which file types are causing most duplication
    file_type_counts = defaultdict(int)

    # For each duplicate ID, count occurrences by file type
    for entry_id, entries in duplicate_info["duplicate_ids"].items():
        for file_path, _ in entries:
            if "consolidated" in file_path:
                file_type_counts["consolidated"] += 1
            elif "manual_split" in file_path:
                file_type_counts["manual_split"] += 1
            elif "split_flagged" in file_path:
                file_type_counts["split_flagged"] += 1
            elif "batch" in file_path:
                file_type_counts["original_batch"] += 1
            else:
                file_type_counts["other"] += 1

    # Count file patterns in duplicate content
    content_file_patterns = defaultdict(int)
    for content_key, entries in duplicate_info["duplicate_contents"].items():
        pattern = []
        for file_path, _ in entries:
            if "consolidated" in file_path:
                pattern.append("consolidated")
            elif "manual_split" in file_path:
                pattern.append("manual_split")
            elif "split_flagged" in file_path:
                pattern.append("split_flagged")
            elif "batch_999" in file_path:
                pattern.append("batch_999")
            elif "batch" in file_path:
                pattern.append("original_batch")
            else:
                pattern.append("other")
        pattern_key = "+".join(sorted(pattern))
        content_file_patterns[pattern_key] += 1

    return {
        "file_type_counts": dict(file_type_counts),
        "content_file_patterns": dict(content_file_patterns)
    }

def recommend_cleanup(duplicate_info, pattern_analysis):
    """Recommend how to clean up the duplication"""
    # Identify which files to keep based on duplication patterns
    recommendations = []

    # Recommend removing duplicates, keeping files in a specific priority
    recommendations.append("Based on the duplication analysis, consider:")

    # If we have many duplicates between original and consolidated/split files
    if "original_batch+consolidated" in pattern_analysis["content_file_patterns"] or \
       "original_batch+split_flagged" in pattern_analysis["content_file_patterns"] or \
       "original_batch+manual_split" in pattern_analysis["content_file_patterns"]:
        recommendations.append("1. Keep only the consolidated files and remove split_flagged and manual_split files")
        recommendations.append("2. OR keep only the original_batch files and the split_flagged/manual_split files for entries not in batches")

    # If we have duplicates between split_flagged and manual_split
    if "manual_split+split_flagged" in pattern_analysis["content_file_patterns"]:
        recommendations.append("3. Choose either manual_split or split_flagged files, but not both")

    # If we have duplicates within consolidated files
    if "consolidated+consolidated" in pattern_analysis["content_file_patterns"]:
        recommendations.append("4. Remove duplicate entries from the consolidated files")

    # If there are too many file types causing duplication
    if len(pattern_analysis["file_type_counts"]) > 2:
        recommendations.append("5. Consider using a single approach (either consolidated files or original+split files)")

    # General recommendation
    recommendations.append("\nIdeal approach:")
    recommendations.append("- Remove all *_split_* and manual_split* files")
    recommendations.append("- Keep batch_999 files which contain all entries without duplication")
    recommendations.append("- Make sure the app loads the batch_999 files")

    return recommendations

def print_examples(duplicate_info, max_examples=5):
    """Print examples of duplicate entries"""
    print("\nExamples of duplicate entries by ID:")
    print("-" * 50)

    count = 0
    for entry_id, entries in list(duplicate_info["duplicate_ids"].items())[:max_examples]:
        try:
            print(f"ID: {entry_id}")
            for i, (file_path, entry) in enumerate(entries):
                print(f"  {i+1}. File: {file_path}")

                # Skip printing content details to avoid encoding issues
                print(f"     Entry type: {entry.get('category', 'unknown')}")
            print()
            count += 1
        except Exception as e:
            print(f"Error printing entry: {e}")

    if count < len(duplicate_info["duplicate_ids"]):
        print(f"... and {len(duplicate_info['duplicate_ids']) - count} more duplicate IDs")

    print("\nExamples of duplicate entries by content:")
    print("-" * 50)

    count = 0
    for content_key, entries in list(duplicate_info["duplicate_contents"].items())[:max_examples]:
        try:
            # Skip printing actual content to avoid encoding issues
            print(f"Content hash: {hash(content_key)}")
            for i, (file_path, entry) in enumerate(entries):
                print(f"  {i+1}. File: {file_path}")
                print(f"     ID: {entry.get('id', 'No ID')}")
            print()
            count += 1
        except Exception as e:
            print(f"Error printing content: {e}")

    if count < len(duplicate_info["duplicate_contents"]):
        print(f"... and {len(duplicate_info['duplicate_contents']) - count} more duplicate contents")

def main():
    """Main function"""
    print("Finding Duplicate Entries")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect entries
    print("Collecting entries from all files...")
    entries_by_file = collect_entries_by_file(project_root)

    # Find duplicates
    print("\nFinding duplicate entries...")
    duplicate_info = find_duplicate_entries(entries_by_file)

    # Print summary
    print("\nDuplication Summary:")
    print("-" * 40)
    print(f"Total unique entry IDs: {duplicate_info['unique_ids']}")
    print(f"Total unique entry contents: {duplicate_info['unique_contents']}")
    print(f"Number of duplicate IDs: {duplicate_info['duplicate_id_count']}")
    print(f"Total entries with duplicate IDs: {duplicate_info['duplicate_id_entries']}")
    print(f"Number of duplicate contents: {duplicate_info['duplicate_content_count']}")
    print(f"Total entries with duplicate contents: {duplicate_info['duplicate_content_entries']}")

    # Analyze patterns
    print("\nAnalyzing duplication patterns...")
    pattern_analysis = analyze_duplication_patterns(duplicate_info)

    print("\nFile types involved in ID duplication:")
    for file_type, count in pattern_analysis["file_type_counts"].items():
        print(f"  {file_type}: {count}")

    print("\nDuplication patterns by file combination:")
    for pattern, count in pattern_analysis["content_file_patterns"].items():
        print(f"  {pattern}: {count}")

    # Print examples
    print_examples(duplicate_info)

    # Recommendations
    print("\nRecommendations:")
    print("-" * 40)
    for recommendation in recommend_cleanup(duplicate_info, pattern_analysis):
        print(recommendation)

if __name__ == "__main__":
    main()