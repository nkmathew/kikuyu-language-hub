#!/usr/bin/env python3
"""
Debug Android Loading Process

This script creates a simple test to verify what entries would be loaded
by the Android app logic and understand why only 379 are shown.
"""

import json
from pathlib import Path

def test_android_loading_logic():
    """Test what the Android CuratedContentManager would load"""

    # Simulate the Android app's loading process
    project_root = Path("C:/myrepos/kikuyu-language-hub")
    curated_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"

    all_entries = []

    print("Simulating Android CuratedContentManager loading...")
    print("=" * 60)

    # List all files like Android does
    curated_files = []

    # Simulate the listCuratedFiles() method
    try:
        files = list(curated_dir.glob("*"))
        for file_name in files:
            if file_name.is_dir():
                # It's a directory, add all JSON files
                for sub_file in file_name.glob("*.json"):
                    if sub_file.name != "schema.json":  # Skip schema
                        curated_files.append(sub_file)
            elif file_name.name.endswith(".json"):
                # It's a JSON file directly in the curated-content directory
                if file_name.name != "schema.json":  # Skip schema
                    curated_files.append(file_name)
    except Exception as e:
        print(f"Error listing files: {e}")
        return

    print(f"Found {len(curated_files)} curated content files:")
    for file_path in curated_files:
        print(f"  - {file_path.relative_to(curated_dir)}")

    print(f"\nLoading entries...")

    # Load each file like Android does
    for file_path in curated_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                json_content = f.read()
                data = json.loads(json_content)

            # Parse like Android does (expecting CuratedContent format)
            if "entries" in data:
                entries = data["entries"]
                print(f"  {file_path.name}: {len(entries)} entries")
                all_entries.extend(entries)
            elif "phrases" in data:
                entries = data["phrases"]
                print(f"  {file_path.name}: {len(entries)} entries (phrases)")
                all_entries.extend(entries)
            else:
                print(f"  {file_path.name}: no entries/phrases found")

        except Exception as e:
            print(f"  {file_path.name}: ERROR - {e}")

    print(f"\nTotal entries loaded: {len(all_entries)}")

    # Test filtering like Android does (no filters initially)
    print(f"\nTesting Android filtering logic...")

    # Simulate initial state (no filters)
    currentCategory = None
    currentDifficulty = None
    isFlashcardMode = False

    print(f"Current Category: {currentCategory}")
    print(f"Current Difficulty: {currentDifficulty}")
    print(f"Is Flashcard Mode: {isFlashcardMode}")

    # Apply filters like Android does
    sourceEntries = all_entries  # In study mode, use all entries

    filtered = sourceEntries
    if currentCategory:
        filtered = [e for e in filtered if e.get('category') == currentCategory]
    if currentDifficulty:
        filtered = [e for e in filtered if e.get('difficulty') == currentDifficulty]

    print(f"\nSource entries: {len(sourceEntries)}")
    print(f"Filtered entries: {len(filtered)}")

    # Show breakdown by category
    from collections import Counter
    categories = Counter(e.get('category', 'general') for e in filtered)
    difficulties = Counter(e.get('difficulty', 'medium') for e in filtered)

    print(f"\nCategories breakdown:")
    for cat, count in categories.most_common():
        print(f"  {cat}: {count}")

    print(f"\nDifficulty breakdown:")
    for diff, count in difficulties.most_common():
        print(f"  {diff}: {count}")

    # If we still get more than 379, there might be something else going on
    if len(filtered) > 379:
        print(f"\n⚠️  Expected result: {len(filtered)} entries, but app shows 379")
        print(f"    Something else is limiting the display!")
    elif len(filtered) == 379:
        print(f"\n✅ Match found: {len(filtered)} entries")
        print(f"    This matches what the app shows!")
    else:
        print(f"\n❌ Mismatch: {len(filtered)} entries vs app shows 379")
        print(f"    App is showing more entries than expected!")

    return all_entries, filtered

if __name__ == "__main__":
    test_android_loading_logic()