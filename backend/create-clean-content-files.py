#!/usr/bin/env python3
"""
Create Clean Content Files

This script creates a clean set of content files with no duplication,
keeping only the original batch files and adding only the new split entries.

Usage:
    python create-clean-content-files.py

"""

import json
import os
import glob
from pathlib import Path
import datetime
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

def backup_content_dir(project_root):
    """Backup the curated-content-modern directory"""
    # Create a backup of the current directory
    src_dir = project_root / "backend" / "curated-content-modern"
    backup_dir = project_root / "backend" / "curated-content-modern-backup"

    # Remove previous backup if it exists
    if backup_dir.exists():
        shutil.rmtree(backup_dir)

    # Create backup
    shutil.copytree(src_dir, backup_dir)
    print(f"Backup created at {backup_dir}")

    return backup_dir

def collect_original_entries(project_root):
    """Collect entries from original batch files"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    original_entries = {}
    total_entries = 0

    # Process each category
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name
        if category not in original_entries:
            original_entries[category] = []

        # Process each original batch file in the category
        for file_path in category_dir.glob("easy_kikuyu_batch_[0-9][0-9][0-9]_*.json"):
            # Skip batch_999 files
            if "batch_999" in file_path.name:
                continue

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    # Add each entry to the category list
                    original_entries[category].extend(content["entries"])
                    total_entries += len(content["entries"])
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from original batch files")
    return original_entries

def collect_split_entries(project_root):
    """Collect entries from split files that aren't in original batch files"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    split_entries = {}
    total_entries = 0

    # Process each category
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name
        if category not in split_entries:
            split_entries[category] = []

        # Process manual_split files
        for file_path in category_dir.glob("manual_split_*.json"):
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    # Add each entry to the category list
                    split_entries[category].extend(content["entries"])
                    total_entries += len(content["entries"])
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from manual_split files")
    return split_entries

def create_clean_files(project_root, original_entries, split_entries):
    """Create clean files with no duplication"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Clean up existing files
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name

        # Remove split_flagged, manual_split, and batch_999 files
        for file_path in category_dir.glob("split_flagged_*.json"):
            file_path.unlink()
            print(f"Removed {file_path}")

        for file_path in category_dir.glob("manual_split_*.json"):
            file_path.unlink()
            print(f"Removed {file_path}")

        for file_path in category_dir.glob("*batch_999_*.json"):
            file_path.unlink()
            print(f"Removed {file_path}")

        for file_path in category_dir.glob("consolidated_batch_*.json"):
            file_path.unlink()
            print(f"Removed {file_path}")

    # Create clean files
    total_entries = 0
    for category, entries in split_entries.items():
        if not entries:
            continue

        # Create category directory if it doesn't exist
        category_dir = dir_path / category
        if not category_dir.exists():
            category_dir.mkdir(parents=True, exist_ok=True)

        # Create a clean file for the split entries
        file_path = category_dir / f"easy_kikuyu_batch_999_{category}.json"

        # Create content with entries
        content = {
            "metadata": {
                "schema_version": "1.0",
                "created_date": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "curator": "Clean Content Files Script",
                "source_files": ["manual_split_*.json"],
                "total_entries": len(entries),
                "description": f"Clean file with split entries for {category}"
            },
            "entries": entries
        }

        # Save to file
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(content, f, indent=2, ensure_ascii=False)

        print(f"Created clean file with {len(entries)} entries: {file_path}")
        total_entries += len(entries)

    print(f"Total entries in clean files: {total_entries}")

def run_copy_scripts(project_root):
    """Run copy scripts to update apps"""
    # Run copy scripts
    os.system(f"cd {project_root} && python backend/copy-modern-to-android.py")
    os.system(f"cd {project_root} && python backend/copy-modern-to-web.py")
    print("Copy scripts executed.")

def main():
    """Main function"""
    print("Creating Clean Content Files")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Backup content directory
    print("Backing up content directory...")
    backup_dir = backup_content_dir(project_root)

    # Collect original entries
    print("\nCollecting original entries...")
    original_entries = collect_original_entries(project_root)

    # Collect split entries
    print("\nCollecting split entries...")
    split_entries = collect_split_entries(project_root)

    # Create clean files
    print("\nCreating clean files...")
    create_clean_files(project_root, original_entries, split_entries)

    # Run copy scripts
    print("\nRunning copy scripts...")
    run_copy_scripts(project_root)

    print("\nClean files created and copied to apps.")
    print(f"Original content files backed up at {backup_dir}")

if __name__ == "__main__":
    main()