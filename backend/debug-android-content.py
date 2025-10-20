#!/usr/bin/env python3
"""
Debug Android Content Loading

This script analyzes the content files that the Android app is actually loading
from the curated-content directory to understand why only 379 entries are shown.
"""

import json
import os
from pathlib import Path
from collections import defaultdict, Counter

def find_project_root():
    """Find the project root directory"""
    current_dir = Path(__file__).resolve().parent

    max_levels = 5
    for _ in range(max_levels):
        if current_dir.name == "backend":
            return current_dir.parent

        if current_dir.parent == current_dir:
            break
        current_dir = current_dir.parent

    print("Error: Could not find project root directory.")
    exit(1)

def analyze_curated_content(project_root):
    """Analyze the curated content that Android app loads"""
    # Path to Android assets
    curated_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"

    if not curated_dir.exists():
        print(f"Curated content directory not found: {curated_dir}")
        return

    print(f"Analyzing curated content in: {curated_dir}")
    print("=" * 60)

    # Collect all entries
    all_entries = []
    category_counts = Counter()
    difficulty_counts = Counter()

    # Process all JSON files
    for json_file in curated_dir.glob("**/*.json"):
        rel_path = json_file.relative_to(curated_dir)

        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # Handle different JSON formats
            entries = []
            if isinstance(data, dict):
                if "entries" in data:
                    entries = data["entries"]
                elif "phrases" in data:
                    entries = data["phrases"]
                else:
                    # Skip files without recognizable entry structure
                    print(f"Skipping {rel_path}: no 'entries' or 'phrases' key found")
                    continue
            elif isinstance(data, list):
                entries = data
            else:
                print(f"Skipping {rel_path}: unexpected JSON structure")
                continue

            print(f"File: {rel_path} - {len(entries)} entries")

            for entry in entries:
                # Normalize entry structure
                if isinstance(entry, dict):
                    # Add file info for debugging
                    entry['_source_file'] = str(rel_path)

                    # Count categories and difficulties
                    category = entry.get('category', 'general')
                    difficulty = entry.get('difficulty', 'medium')

                    category_counts[category] += 1
                    difficulty_counts[difficulty] += 1

                    all_entries.append(entry)

        except Exception as e:
            print(f"Error processing {rel_path}: {e}")

    print(f"\nTotal entries found: {len(all_entries)}")
    print(f"Categories: {dict(category_counts)}")
    print(f"Difficulties: {dict(difficulty_counts)}")

    # Show first 20 entries for debugging
    print(f"\nFirst 20 entries:")
    for i, entry in enumerate(all_entries[:20]):
        kikuyu = entry.get('kikuyu', 'NO KIKUYU')
        english = entry.get('english', 'NO ENGLISH')
        category = entry.get('category', 'general')
        difficulty = entry.get('difficulty', 'medium')
        source = entry.get('_source_file', 'unknown')

        print(f"{i+1:3d}. {kikuyu[:30]:30s} | {english[:30]:30s} | {category:10s} | {difficulty:8s} | {source}")

    # Check if there are entries with specific category/difficulty combinations
    print(f"\nCategory-Difficulty breakdown:")
    for category in sorted(category_counts.keys()):
        category_entries = [e for e in all_entries if e.get('category', 'general') == category]
        difficulties = Counter(e.get('difficulty', 'medium') for e in category_entries)
        print(f"  {category}: {len(category_entries)} entries, difficulties: {dict(difficulties)}")

    return all_entries

def main():
    """Main function"""
    print("Debug Android Content Loading")
    print("=" * 40)

    project_root = find_project_root()
    print(f"Project root: {project_root}")

    all_entries = analyze_curated_content(project_root)

if __name__ == "__main__":
    main()