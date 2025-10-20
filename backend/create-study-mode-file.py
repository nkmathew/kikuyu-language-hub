#!/usr/bin/env python3
"""
Create Study Mode File

This script creates files specifically designed for the Study Mode view,
with explicit entry counts and debugging info to help diagnose the display issue.

Usage:
    python create-study-mode-file.py

"""

import json
import os
from pathlib import Path
import shutil
import datetime

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

def collect_beginner_entries(project_root):
    """Collect entries with beginner difficulty"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    beginner_entries = []

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
                        # Check if beginner
                        if entry.get("difficulty") == "beginner":
                            # Add category if missing
                            if "category" not in entry:
                                entry["category"] = category

                            beginner_entries.append(entry)
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {len(beginner_entries)} beginner entries")
    return beginner_entries

def create_direct_study_file(project_root, beginner_entries):
    """Create a study mode file with beginner entries"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "study_mode_entries.json"

    # Create a simplified format for the study mode
    study_content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": datetime.datetime.now().isoformat(),
            "curator": "Study Mode Entries Script",
            "source_files": ["beginner_entries"],
            "total_entries": len(beginner_entries),
            "description": "Entries for study mode view"
        },
        "entries": beginner_entries
    }

    # Save to file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(study_content, f, indent=2, ensure_ascii=False)

    print(f"Created study mode file with {len(beginner_entries)} entries: {target_file}")
    return target_file

def create_numbered_entries(project_root):
    """Create a set of explicitly numbered entries for debugging"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "numbered_entries.json"

    # Create 500 numbered entries
    numbered_entries = []
    for i in range(1, 501):
        numbered_entries.append({
            "id": f"numbered-{i:03d}",
            "kikuyu": f"Numbered Entry {i} (Kikuyu)",
            "english": f"Numbered Entry {i} (English)",
            "category": "vocabulary" if i % 3 == 0 else "phrases" if i % 3 == 1 else "grammar",
            "difficulty": "beginner",
            "source": {"origin": "Numbered Entries Debug"},
            "quality": 3
        })

    # Create content
    numbered_content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": datetime.datetime.now().isoformat(),
            "curator": "Numbered Entries Script",
            "source_files": ["numbered_entries"],
            "total_entries": len(numbered_entries),
            "description": "Explicitly numbered entries for debugging"
        },
        "entries": numbered_entries
    }

    # Save to file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(numbered_content, f, indent=2, ensure_ascii=False)

    print(f"Created numbered entries file with {len(numbered_entries)} entries: {target_file}")

    # Create in the curated-content directory as well
    curated_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content" / "vocabulary"
    curated_dir.mkdir(parents=True, exist_ok=True)
    curated_file = curated_dir / "numbered_entries.json"

    # Save to curated file
    with open(curated_file, 'w', encoding='utf-8') as f:
        json.dump(numbered_content, f, indent=2, ensure_ascii=False)

    print(f"Created numbered entries in curated directory: {curated_file}")

    return target_file

def create_legacy_format(project_root, beginner_entries):
    """Create a file in the legacy format that FlashCardManager might be using"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    target_file = target_dir / "kikuyu-phrases.json"

    # Create simplified entries
    legacy_entries = []
    for i, entry in enumerate(beginner_entries[:400]):  # Limit to 400 entries
        legacy_entries.append({
            "kikuyu": entry.get("kikuyu", ""),
            "english": entry.get("english", "")
        })

    # Save to file
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(legacy_entries, f, indent=2, ensure_ascii=False)

    print(f"Created legacy format file with {len(legacy_entries)} entries: {target_file}")
    return target_file

def create_sample_file(project_root):
    """Create a file that uses a sample structure from the original app"""
    # Target is the Android assets directory
    target_dir = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"
    target_dir.mkdir(parents=True, exist_ok=True)

    # Create a vocabulary directory if it doesn't exist
    vocab_dir = target_dir / "vocabulary"
    vocab_dir.mkdir(parents=True, exist_ok=True)

    # Create a sample file for vocabulary
    vocab_file = vocab_dir / "study_mode_vocab.json"

    # Create 400 sample entries
    sample_entries = []
    for i in range(1, 401):
        sample_entries.append({
            "id": f"sample-{i:03d}",
            "kikuyu": f"Sample Vocab {i} (Kikuyu)",
            "english": f"Sample Vocab {i} (English)",
            "category": "vocabulary",
            "difficulty": "beginner",
            "source": {"origin": "Sample Entries Debug"},
            "quality": 3
        })

    # Create content
    sample_content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": datetime.datetime.now().isoformat(),
            "curator": "Sample Entries Script",
            "source_files": ["sample_entries"],
            "total_entries": len(sample_entries),
            "description": "Sample vocabulary entries for debugging"
        },
        "entries": sample_entries
    }

    # Save to file
    with open(vocab_file, 'w', encoding='utf-8') as f:
        json.dump(sample_content, f, indent=2, ensure_ascii=False)

    print(f"Created sample vocabulary file with {len(sample_entries)} entries: {vocab_file}")
    return vocab_file

def main():
    """Main function"""
    print("Creating Study Mode File")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect beginner entries
    print("\nCollecting beginner entries...")
    beginner_entries = collect_beginner_entries(project_root)

    # Create study mode file
    print("\nCreating study mode file...")
    study_file = create_direct_study_file(project_root, beginner_entries)

    # Create numbered entries
    print("\nCreating numbered entries file...")
    numbered_file = create_numbered_entries(project_root)

    # Create legacy format file
    print("\nCreating legacy format file...")
    legacy_file = create_legacy_format(project_root, beginner_entries)

    # Create sample file
    print("\nCreating sample file...")
    sample_file = create_sample_file(project_root)

    print("\nStudy mode files created.")
    print("Please reinstall the app to ensure the new files are included.")

if __name__ == "__main__":
    main()