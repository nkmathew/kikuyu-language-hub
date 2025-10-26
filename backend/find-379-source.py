#!/usr/bin/env python3
"""
Find the Source of 379 Entries

This script tries to determine what exactly produces 379 entries
in the Android app's loading logic.
"""

import json
from pathlib import Path
from collections import defaultdict

def find_379_source():
    """Try to find what combination produces exactly 379 entries"""
    curated_dir = Path("../android-kikuyuflashcards/app/src/main/assets/curated-content")

    # Collect all entries with full details
    all_entries = []

    print("Loading all entries with full details...")
    for json_file in curated_dir.glob("**/*.json"):
        if "schema" in json_file.name:
            continue

        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            entries = []
            if isinstance(data, dict) and "entries" in data:
                entries = data["entries"]
            elif isinstance(data, dict) and "phrases" in data:
                entries = data["phrases"]
            elif isinstance(data, list):
                entries = data

            for entry in entries:
                if isinstance(entry, dict):
                    entry['_source_file'] = str(json_file.relative_to(curated_dir))
                    all_entries.append(entry)

        except Exception as e:
            print(f"Error with {json_file}: {e}")
            continue

    print(f"Total entries loaded: {len(all_entries)}")

    # Check various filtering scenarios
    print("\n=== TESTING FILTERING SCENARIOS ===")

    # Scenario 1: Only entries with both kikuyu and english
    complete_entries = [e for e in all_entries if e.get('kikuyu') and e.get('english')]
    print(f"1. Entries with both kikuyu and english: {len(complete_entries)}")

    # Scenario 2: Only entries with valid ID
    with_id = [e for e in complete_entries if e.get('id')]
    print(f"2. Entries with ID: {len(with_id)}")

    # Scenario 3: Only entries with specific categories
    valid_categories = {'vocabulary', 'phrases', 'grammar', 'conjugations', 'proverbs', 'cultural'}
    valid_cat = [e for e in with_id if e.get('category') in valid_categories]
    print(f"3. Entries with valid categories: {len(valid_cat)}")

    # Scenario 4: Only entries with specific difficulties
    valid_difficulties = {'beginner', 'intermediate', 'advanced', 'easy', 'medium', 'hard'}
    valid_diff = [e for e in valid_cat if e.get('difficulty') in valid_difficulties]
    print(f"4. Entries with valid difficulties: {len(valid_diff)}")

    # Scenario 5: Check each category individually
    print("\n=== BY CATEGORY ===")
    category_counts = defaultdict(int)
    for entry in valid_diff:
        cat = entry.get('category', 'general')
        category_counts[cat] += 1

        if category_counts[cat] == 379:
            print(f"FOUND! Category '{cat}' has exactly 379 entries")
        elif category_counts[cat] > 379 and category_counts[cat] - 1 == 379:
            print(f"FOUND! Category '{cat}' had 379 entries before this one")

    for cat, count in sorted(category_counts.items()):
        print(f"  {cat}: {count}")

    # Scenario 6: Check by difficulty
    print("\n=== BY DIFFICULTY ===")
    difficulty_counts = defaultdict(int)
    for entry in valid_diff:
        diff = entry.get('difficulty', 'medium')
        difficulty_counts[diff] += 1

        if difficulty_counts[diff] == 379:
            print(f"FOUND! Difficulty '{diff}' has exactly 379 entries")

    for diff, count in sorted(difficulty_counts.items()):
        print(f"  {diff}: {count}")

    # Scenario 7: Check if it's the first 379 entries (some limit)
    if len(valid_diff) > 379:
        print(f"\n7. First 379 entries from {len(valid_diff)} total")
        print(f"   Entry 379: {valid_diff[378].get('english', 'NO ENGLISH')[:50]}...")
        print(f"   Entry 380: {valid_diff[379].get('english', 'NO ENGLISH')[:50]}...")

    # Scenario 8: Check specific file combinations
    print("\n=== FILE ANALYSIS ===")
    file_counts = defaultdict(int)
    for entry in all_entries:
        source = entry.get('_source_file', 'unknown')
        file_counts[source] += 1
        if file_counts[source] == 379:
            print(f"FOUND! File '{source}' has exactly 379 entries")

    # Show files with counts around 379
    for file_path, count in sorted(file_counts.items(), key=lambda x: x[1], reverse=True):
        if 350 <= count <= 450:
            print(f"  {file_path}: {count}")

    # Check if all_entries.json specifically has 379
    all_entries_file = curated_dir / "all_entries.json"
    if all_entries_file.exists():
        with open(all_entries_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        entries_count = len(data.get('entries', []))
        print(f"\n8. all_entries.json file: {entries_count} entries")
        if entries_count == 379:
            print("   FOUND! all_entries.json has exactly 379 entries")

if __name__ == "__main__":
    find_379_source()