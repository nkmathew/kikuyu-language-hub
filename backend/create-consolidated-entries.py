#!/usr/bin/env python3
"""
Create Consolidated Entries File

This script creates a consolidated file with all entries that will be recognized
by the Android app. It uses the easy_kikuyu_batch_XXX format that the app recognizes.

Usage:
    python create-consolidated-entries.py

"""

import json
import os
import glob
from pathlib import Path

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
    all_entries = {}
    total_entries = 0

    # Process each category
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name
        if category not in all_entries:
            all_entries[category] = []

        # Process each file in the category
        for file_path in category_dir.glob("*.json"):
            # Skip schema file
            if file_path.name == "schema.json":
                continue

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.load(f)

                # Get entries
                if "entries" in content:
                    # Add each entry to the category list
                    for entry in content["entries"]:
                        # Make sure the entry has the correct category
                        if "category" in entry:
                            entry["category"] = category
                        all_entries[category].append(entry)
                        total_entries += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from all files")
    return all_entries

def create_consolidated_files(project_root, all_entries):
    """Create consolidated files for each category"""
    # Base directory for content
    output_dir = project_root / "backend" / "curated-content-modern"

    # Create a consolidated file for each category
    for category, entries in all_entries.items():
        # Create output directory if it doesn't exist
        category_dir = output_dir / category
        if not category_dir.exists():
            category_dir.mkdir(parents=True, exist_ok=True)

        # Create consolidated file
        file_name = f"easy_kikuyu_batch_999_{category}.json"
        file_path = category_dir / file_name

        # Create consolidated content
        content = {
            "metadata": {
                "schema_version": "1.0",
                "created_date": "2025-10-20T20:30:00.000000+00:00",
                "curator": "Consolidated Entries Script",
                "source_files": ["all_files"],
                "total_entries": len(entries),
                "description": f"Consolidated entries for {category}"
            },
            "entries": entries
        }

        # Save to file
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(content, f, indent=2, ensure_ascii=False)

        print(f"Created consolidated file for {category} with {len(entries)} entries: {file_path}")

def run_copy_scripts(project_root):
    """Run copy scripts to update apps"""
    # Run copy scripts
    os.system(f"cd {project_root} && python backend/copy-modern-to-android.py")
    os.system(f"cd {project_root} && python backend/copy-modern-to-web.py")
    print("Copy scripts executed.")

def main():
    """Main function"""
    print("Creating Consolidated Entries File")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect all entries
    print("Collecting all entries...")
    all_entries = collect_all_entries(project_root)

    # Create consolidated files
    print("\nCreating consolidated files...")
    create_consolidated_files(project_root, all_entries)

    # Run copy scripts
    print("\nRunning copy scripts...")
    run_copy_scripts(project_root)

    print("\nConsolidated files created and copied to apps.")
    print("The apps should now show all entries.")

if __name__ == "__main__":
    main()