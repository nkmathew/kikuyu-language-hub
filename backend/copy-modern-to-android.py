#!/usr/bin/env python3
"""
Copy modern schema curated content to Android app assets directory

This script copies the converted modern schema JSON files to the Android app's assets/curated-content directory,
updating the existing curated content with the new modern schema format.

Usage:
    python copy-modern-to-android.py
"""

import os
import shutil
from pathlib import Path

def copy_modern_content():
    """Copy modern schema files to Android app"""

    # Paths
    modern_source = Path("curated-content-modern")
    android_target = Path("../android-kikuyuflashcards/app/src/main/assets/curated-content")

    print("Copying modern schema curated content to Android app...")
    print(f"Source: {modern_source.resolve()}")
    print(f"Target: {android_target.resolve()}")

    if not modern_source.exists():
        print(f"Error: Modern content directory not found: {modern_source}")
        return False

    if not android_target.exists():
        print(f"Error: Android assets directory not found: {android_target}")
        print("Please ensure the Android project structure exists.")
        return False

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
    print(f"Target directory: {android_target.resolve()}")

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

    # Change to backend directory if script is run from elsewhere
    script_dir = Path(__file__).parent
    if script_dir.name == "backend":
        os.chdir(script_dir)

    success = copy_modern_content()

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