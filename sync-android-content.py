#!/usr/bin/env python
"""
Syncs curated content from backend or web app to the Android app assets directory.

This script ensures that all curated JSON content is properly copied into the
Android app's assets/curated-content directory, organizing the files by category.
"""

import os
import json
import shutil
import argparse
from pathlib import Path
from datetime import datetime

# Directory paths
BASE_DIR = Path(__file__).resolve().parent
BACKEND_CURATED_DIR = BASE_DIR / "backend" / "curated-content"
WEBAPP_CURATED_DIR = BASE_DIR / "flashcards-app" / "public" / "data" / "curated"
ANDROID_ASSETS_DIR = BASE_DIR / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"

# Config
CATEGORIES = [
    "vocabulary",
    "proverbs",
    "grammar",
    "conjugations",
    "cultural",
    "phrases",
    "numbers"
]

def setup_android_directories():
    """Create necessary directories in Android assets folder"""
    # Ensure the main curated content directory exists
    curated_dir = ANDROID_ASSETS_DIR / "curated-content"
    os.makedirs(curated_dir, exist_ok=True)

    # Create category subdirectories
    for category in CATEGORIES:
        os.makedirs(curated_dir / category, exist_ok=True)

    print(f"✓ Set up directory structure in {curated_dir}")


def copy_curated_content(source_dir):
    """
    Copy curated content from source directory to Android assets

    Args:
        source_dir (Path): Path to source directory containing curated content
    """
    if not source_dir.exists():
        print(f"Error: Source directory {source_dir} does not exist")
        return False

    source_categories = [d for d in source_dir.iterdir() if d.is_dir() and d.name in CATEGORIES]
    target_dir = ANDROID_ASSETS_DIR / "curated-content"

    files_copied = 0

    # Process each category directory
    for category_dir in source_categories:
        category = category_dir.name
        target_category_dir = target_dir / category

        # Process each JSON file in the category
        for json_file in category_dir.glob("*.json"):
            # Skip schema.json
            if json_file.name == "schema.json":
                continue

            # Copy the file
            target_file = target_category_dir / json_file.name
            shutil.copy2(json_file, target_file)
            files_copied += 1

            print(f"  Copied: {category}/{json_file.name}")

    return files_copied


def process_file_registry():
    """Create a file registry JSON that lists all available content files"""
    target_dir = ANDROID_ASSETS_DIR / "curated-content"
    registry = {
        "metadata": {
            "generated_date": datetime.now().isoformat(),
            "total_files": 0
        },
        "categories": {}
    }

    total_files = 0

    # Scan each category directory
    for category in CATEGORIES:
        category_dir = target_dir / category
        if not category_dir.exists():
            continue

        # Get list of JSON files in this category
        json_files = list(category_dir.glob("*.json"))
        file_names = [f.name for f in json_files]
        file_names.sort()  # Sort alphabetically

        registry["categories"][category] = {
            "file_count": len(file_names),
            "files": file_names
        }

        total_files += len(file_names)

    registry["metadata"]["total_files"] = total_files

    # Write registry file
    registry_file = target_dir / "file_registry.json"
    with open(registry_file, "w") as f:
        json.dump(registry, f, indent=2)

    print(f"✓ Generated file registry with {total_files} files")
    return total_files


def main():
    parser = argparse.ArgumentParser(description="Sync curated content to Android assets")
    parser.add_argument(
        "--source",
        choices=["backend", "webapp"],
        default="webapp",
        help="Source directory to copy from (default: webapp)"
    )
    args = parser.parse_args()

    print(f"🔄 Syncing curated content to Android assets from {args.source}")

    # Determine source directory
    source_dir = WEBAPP_CURATED_DIR if args.source == "webapp" else BACKEND_CURATED_DIR

    # Ensure directory structure exists
    setup_android_directories()

    # Copy content
    files_copied = copy_curated_content(source_dir)

    if files_copied:
        # Generate file registry
        total_files = process_file_registry()

        # Create empty legacy file if needed
        legacy_file = ANDROID_ASSETS_DIR / "kikuyu-phrases.json"
        if not legacy_file.exists():
            with open(legacy_file, "w") as f:
                f.write('{"phrases":[]}')
            print("✓ Created empty legacy phrases file")

        print(f"\n✅ Successfully synced {files_copied} files across {len(CATEGORIES)} categories")
        print(f"   Target directory: {ANDROID_ASSETS_DIR}/curated-content")
    else:
        print(f"\n❌ No files copied. Please check that {source_dir} exists and contains category folders")


if __name__ == "__main__":
    main()