#!/usr/bin/env python3
"""
Copy modern schema curated content to web flashcards app public directory

This script copies the converted modern schema JSON files to the web app's public/data/curated directory,
updating the existing curated content with the new modern schema format.

Usage:
    python copy-modern-to-web.py
"""

import os
import shutil
from pathlib import Path

def copy_modern_content():
    """Copy modern schema files to web app"""

    # Paths
    modern_source = Path("curated-content-modern")
    web_target = Path("../flashcards-app/public/data/curated")

    print("Copying modern schema curated content to web flashcards app...")
    print(f"Source: {modern_source.resolve()}")
    print(f"Target: {web_target.resolve()}")

    if not modern_source.exists():
        print(f"Error: Modern content directory not found: {modern_source}")
        return False

    if not web_target.exists():
        print(f"Error: Web app curated directory not found: {web_target}")
        print("Please ensure the web app structure exists.")
        return False

    # Remove existing curated content in web app
    if web_target.exists():
        print(f"Cleaning existing curated content...")
        shutil.rmtree(web_target)

    # Create fresh directory
    web_target.mkdir(parents=True, exist_ok=True)

    # Copy all modern content files
    copied_count = 0
    error_count = 0

    for item in modern_source.rglob("*"):
        # Create relative path
        relative_path = item.relative_to(modern_source)
        target_path = web_target / relative_path

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
    print(f"Target directory: {web_target.resolve()}")

    # Show category breakdown
    print(f"\nCategories copied:")
    if web_target.exists():
        for category_dir in web_target.iterdir():
            if category_dir.is_dir():
                file_count = len(list(category_dir.glob("*.json")))
                print(f"   {category_dir.name}: {file_count} files")

    return error_count == 0

def main():
    """Main entry point"""
    print("Copying Modern Schema Curated Content to Web Flashcards App")
    print("=" * 60)

    # Change to backend directory if script is run from elsewhere
    script_dir = Path(__file__).parent
    if script_dir.name == "backend":
        os.chdir(script_dir)

    success = copy_modern_content()

    if success:
        print("\nSuccess! Modern schema content copied to web flashcards app.")
        print("The web app now uses the modern schema format.")
        print("\nWeb app is ready for:")
        print("   • Modern schema compliance")
        print("   • Enhanced metadata support")
        print("   • Quality scoring system")
        print("   • Improved source attribution")
        print("   • Better SEO and performance")
    else:
        print("\nCopy completed with errors. Please check the messages above.")

if __name__ == "__main__":
    main()