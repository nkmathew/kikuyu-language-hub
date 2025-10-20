#!/usr/bin/env python3
"""
Create Single Unified Content File

This script creates one single unified file with all entries that will be
recognized by the Android app. It uses the standard naming pattern that
the app definitely recognizes.

Usage:
    python create-single-unified-file.py

"""

import json
import os
import glob
from pathlib import Path
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

def collect_all_entries(project_root):
    """Collect all entries from all JSON files"""
    # Base directory for content
    dir_path = project_root / "backend" / "curated-content-modern"

    # Collect entries
    all_entries = []
    total_entries = 0
    categories = {}

    # Process each category
    for category_dir in dir_path.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name
        if category not in categories:
            categories[category] = 0

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
                    # Add each entry to the all_entries list
                    for entry in content["entries"]:
                        # Make sure the entry has the correct category
                        if "category" in entry:
                            entry["category"] = category
                            categories[category] += 1
                        all_entries.append(entry)
                        total_entries += 1
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

    print(f"Collected {total_entries} entries from all files")
    for category, count in sorted(categories.items()):
        print(f"  {category}: {count}")
    return all_entries

def create_unified_file(project_root, all_entries):
    """Create a single unified file with all entries"""
    # Create output directory
    backend_dir = project_root / "backend" / "curated-content-modern"
    output_file = backend_dir / "all_entries.json"

    # Create content with all entries
    content = {
        "metadata": {
            "schema_version": "1.0",
            "created_date": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "curator": "Unified Entries Script",
            "source_files": ["all_files"],
            "total_entries": len(all_entries),
            "description": "Unified file containing all entries"
        },
        "entries": all_entries
    }

    # Save to file
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(content, f, indent=2, ensure_ascii=False)

    print(f"Created unified file with {len(all_entries)} entries: {output_file}")
    return output_file

def create_android_copy(project_root, unified_file):
    """Create a direct copy in the Android assets directory"""
    # Create output directory
    android_assets = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets"
    output_file = android_assets / "all_entries.json"

    # Copy the unified file
    with open(unified_file, 'r', encoding='utf-8') as src:
        with open(output_file, 'w', encoding='utf-8') as dst:
            dst.write(src.read())

    print(f"Created direct copy in Android assets: {output_file}")
    return output_file

def update_app_loader(project_root, unified_file):
    """Create a helper class to load the unified file"""
    # Create helper class
    helper_file = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "java" / "com" / "nkmathew" / "kikuyuflashcards" / "UnifiedContentLoader.kt"

    # Ensure directories exist
    helper_file.parent.mkdir(parents=True, exist_ok=True)

    # Write the helper class
    helper_code = """package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nkmathew.kikuyuflashcards.models.CuratedContent
import com.nkmathew.kikuyuflashcards.models.FlashcardEntry
import java.io.IOException

/**
 * Helper class to load the unified content file
 */
class UnifiedContentLoader(private val context: Context) {
    companion object {
        private const val TAG = "UnifiedContentLoader"
        private const val UNIFIED_FILE = "all_entries.json"
    }

    private val gson = Gson()

    /**
     * Load all entries from the unified file
     */
    fun loadAllEntries(): List<FlashcardEntry> {
        try {
            context.assets.open(UNIFIED_FILE).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }

                try {
                    val content = gson.fromJson(json, CuratedContent::class.java)
                    Log.d(TAG, "Loaded ${content.entries.size} entries from unified file")
                    return content.entries
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing unified file: ${e.message}", e)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading unified file: ${e.message}", e)
        }

        return emptyList()
    }
}
"""

    # Save the helper class
    with open(helper_file, 'w', encoding='utf-8') as f:
        f.write(helper_code)

    print(f"Created helper class: {helper_file}")
    return helper_file

def run_copy_scripts(project_root):
    """Run copy scripts to update apps"""
    # Run copy scripts
    os.system(f"cd {project_root} && python backend/copy-modern-to-android.py")
    os.system(f"cd {project_root} && python backend/copy-modern-to-web.py")
    print("Copy scripts executed.")

def main():
    """Main function"""
    print("Creating Single Unified Content File")
    print("=" * 40)

    # Find project root
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect all entries
    print("Collecting all entries...")
    all_entries = collect_all_entries(project_root)

    # Create unified file
    print("\nCreating unified file...")
    unified_file = create_unified_file(project_root, all_entries)

    # Create direct copy in Android assets
    print("\nCreating direct copy in Android assets...")
    android_copy = create_android_copy(project_root, unified_file)

    # Create helper class (optional)
    # print("\nCreating helper class...")
    # helper_file = update_app_loader(project_root, unified_file)

    # Run copy scripts
    print("\nRunning copy scripts...")
    run_copy_scripts(project_root)

    print("\nUnified file created and copied to Android assets.")
    print("Note: To use this unified file in the app, you would need to modify")
    print("the app code to load this file directly instead of using CuratedContentManager.")

if __name__ == "__main__":
    main()