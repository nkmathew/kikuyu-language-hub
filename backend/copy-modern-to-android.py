#!/usr/bin/env python3
"""
Copy modern schema curated content to Android app assets directory

This script copies the converted modern schema JSON files to the Android app's assets/curated-content directory,
updating the existing curated content with the new modern schema format.

Usage:
    python copy-modern-to-android.py

This script can be run from any directory, and will automatically find the project root.
"""

import os
import sys
import shutil
from pathlib import Path

def find_project_root():
    """Find the project root directory by looking for key markers

    Returns:
        Path: The absolute path to the project root directory
    """
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
    sys.exit(1)

def copy_modern_content(project_root):
    """Copy modern schema files to Android app

    Args:
        project_root (Path): The absolute path to the project root directory

    Returns:
        bool: True if the copy operation was successful, False otherwise
    """
    # Absolute paths based on project root
    modern_source = project_root / "backend" / "curated-content-modern"
    android_target = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content"

    print("Copying modern schema curated content to Android app...")
    print(f"Source: {modern_source}")
    print(f"Target: {android_target}")

    if not modern_source.exists():
        print(f"Error: Modern content directory not found: {modern_source}")
        print("Make sure you're running this script from the kikuyu-language-hub repository.")
        return False

    # Create parent directories if needed
    android_target.parent.mkdir(parents=True, exist_ok=True)

    # Remove existing curated content in Android
    if android_target.exists():
        print(f"Cleaning existing curated content...")
        shutil.rmtree(android_target)

    # Create fresh directory
    android_target.mkdir(parents=True, exist_ok=True)

    # Copy all modern content files
    copied_count = 0
    error_count = 0

    for item in modern_source.rglob("*"):
        # Create relative path
        relative_path = item.relative_to(modern_source)
        target_path = android_target / relative_path

        if item.is_file():
            # Create directory if needed
            target_path.parent.mkdir(parents=True, exist_ok=True)

            try:
                shutil.copy2(item, target_path)
                print(f"Copied: {relative_path}")
                copied_count += 1
            except Exception as e:
                print(f"Error copying {relative_path}: {e}")
                error_count += 1

    print(f"\nCopy Summary:")
    print(f"Successfully copied: {copied_count} files")
    if error_count > 0:
        print(f"Errors: {error_count} files")
    print(f"Target directory: {android_target}")

    # Show category breakdown
    print(f"\nCategories copied:")
    if android_target.exists():
        for category_dir in android_target.iterdir():
            if category_dir.is_dir():
                file_count = len(list(category_dir.glob("*.json")))
                print(f"   {category_dir.name}: {file_count} files")

    return error_count == 0

def main():
    """Main entry point"""
    print("Copying Modern Schema Curated Content to Android App")
    print("=" * 60)

    # Find project root regardless of where script is run from
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # No need to change directory, using absolute paths instead
    success = copy_modern_content(project_root)

    if success:
        print("\nSuccess! Modern schema content copied to Android app.")
        print("The Android app now uses the modern schema format.")
        print("\nAndroid app is ready for:")
        print("   • Modern schema compliance")
        print("   • Enhanced metadata support")
        print("   • Quality scoring system")
        print("   • Improved source attribution")
    else:
        print("\nCopy completed with errors. Please check the messages above.")

if __name__ == "__main__":
    main()