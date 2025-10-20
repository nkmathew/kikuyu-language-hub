#!/usr/bin/env python3
"""
Remove original flagged translations from source files

This script reads the flagged translations from flagged-translations.txt,
identifies the original source files containing these entries, and removes
them from the source files. This is intended to be used after running the
process-flagged-translations.py script to avoid having duplicate entries
in the database (both the original and the split versions).

The script:
1. Reads the flagged-translations.txt file to get the list of entries to remove
2. For each flagged entry, identifies the source file based on the entry ID
3. Modifies the source file to remove the flagged entry
4. Updates the metadata in the modified file
5. Saves the modified file

After running this script, you should run the copy scripts to update the apps:
- copy-modern-to-android.py
- copy-modern-to-web.py

Usage:
    python remove-flagged-originals.py

Requirements:
    - The project structure with backend/curated-content-modern directory
    - A valid flagged-translations.txt file in the curated-content-modern directory

Output:
    - Modified content files with flagged entries removed
    - Updated metadata counts in the modified files
"""

import json
import os
import re
from pathlib import Path
from collections import defaultdict

# Constants
FLAGGED_FILE = "curated-content-modern/flagged-translations.txt"
CONTENT_DIR = "curated-content-modern"

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
    exit(1)

def load_flagged_translations(project_root):
    """Load flagged translations from the text file

    Args:
        project_root (Path): Path to the project root

    Returns:
        dict: The parsed JSON data
    """
    flagged_file = project_root / "backend" / FLAGGED_FILE

    if not flagged_file.exists():
        print(f"Error: Flagged translations file not found: {flagged_file}")
        exit(1)

    with open(flagged_file, 'r', encoding='utf-8') as f:
        content = f.read()
        # Fix potential BOM or encoding issues
        content = content.strip()
        if content.startswith('\ufeff'):  # BOM character
            content = content[1:]

    try:
        data = json.loads(content)
        return data
    except json.JSONDecodeError as e:
        print(f"Error parsing JSON: {e}")

        # Try to fix common JSON issues and retry
        print("Attempting to fix JSON formatting issues...")
        # Remove potential hidden characters, replace tabs, fix line endings
        clean_content = re.sub(r'[\r\n\t]+', ' ', content)
        # Fix potential missing commas or quotes
        clean_content = re.sub(r'}\s*{', '}, {', clean_content)

        try:
            data = json.loads(clean_content)
            print("Successfully fixed JSON formatting.")
            return data
        except json.JSONDecodeError:
            print("Failed to fix JSON. Trying manual parsing...")

            # As last resort, try to manually parse the content
            manual_data = {"flagged_translations": []}

            # Try to extract entries one by one
            entry_pattern = r'\{\s*"id":\s*"([^"]+)",\s*"kikuyu":\s*"([^"]+)",\s*"english":\s*"([^"]+)",\s*"category":\s*"([^"]+)",\s*"difficulty":\s*"([^"]+)",\s*"flag_reason":\s*"([^"]+)"'

            matches = re.findall(entry_pattern, content, re.DOTALL)

            if not matches:
                print("Manual parsing failed. Could not extract entries.")
                exit(1)

            for match in matches:
                entry_id, kikuyu, english, category, difficulty, flag_reason = match
                entry = {
                    "id": entry_id,
                    "kikuyu": kikuyu,
                    "english": english,
                    "category": category,
                    "difficulty": difficulty,
                    "flag_reason": flag_reason,
                    "source": {"origin": "Manual extraction"}
                }
                manual_data["flagged_translations"].append(entry)

            print(f"Manually extracted {len(manual_data['flagged_translations'])} entries.")
            return manual_data

def find_content_files(project_root):
    """Find all content files in the project

    Args:
        project_root (Path): Path to the project root

    Returns:
        dict: Dictionary mapping category to list of content files
    """
    content_dir = project_root / "backend" / CONTENT_DIR
    content_files = {}

    for category_dir in content_dir.iterdir():
        if category_dir.is_dir():
            category = category_dir.name
            content_files[category] = []

            # Find all JSON files in this category directory
            for json_file in category_dir.glob("*.json"):
                # Skip the split_flagged files
                if not json_file.name.startswith("split_flagged_"):
                    content_files[category].append(json_file)

    return content_files

def locate_entry_source(entry_id, content_files):
    """Locate the source file for a given entry ID

    Args:
        entry_id (str): The ID of the entry to locate
        content_files (dict): Dictionary mapping category to list of content files

    Returns:
        tuple: (file_path, file_data) or (None, None) if not found
    """
    # Parse the ID to get the category and batch number
    match = re.match(r'([a-z]+)-(\d+)-', entry_id)
    if match:
        category_short, batch_num = match.groups()
        batch_num = int(batch_num)
    else:
        # Try another pattern
        match = re.match(r'([a-z]+)-(\d+)', entry_id)
        if match:
            category_short, batch_num = match.groups()
            batch_num = int(batch_num)
        else:
            print(f"Warning: Could not parse ID format for '{entry_id}'")
            return None, None

    # Map short category name to full name
    category_map = {
        "vocab": "vocabulary",
        "phrase": "phrases",
        "grammar": "grammar",
        "proverb": "proverbs",
        "conj": "conjugations",
        "cultural": "cultural"
    }

    category = category_map.get(category_short, category_short)

    if category not in content_files:
        print(f"Warning: Category '{category}' not found in content files")
        return None, None

    # Look for batch files that match the batch number
    for file_path in content_files[category]:
        # Extract batch number from filename
        batch_match = re.search(r'batch_(\d+)', file_path.name)
        if batch_match:
            file_batch = int(batch_match.group(1))
            if file_batch == batch_num:
                # Found a matching batch file, now check if it contains the entry
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        file_data = json.load(f)

                    # Check if entry exists in this file
                    for entry in file_data.get("entries", []):
                        if entry.get("id") == entry_id:
                            return file_path, file_data
                except Exception as e:
                    print(f"Error reading file {file_path}: {e}")

    # If we got here, we didn't find the entry in any file with a matching batch number
    # Try searching all files in the category
    for file_path in content_files[category]:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                file_data = json.load(f)

            # Check if entry exists in this file
            for entry in file_data.get("entries", []):
                if entry.get("id") == entry_id:
                    return file_path, file_data
        except Exception as e:
            print(f"Error reading file {file_path}: {e}")

    print(f"Warning: Could not find source file for entry '{entry_id}'")
    return None, None

def remove_flagged_entries(project_root):
    """Remove flagged entries from their source files

    Args:
        project_root (Path): Path to the project root

    Returns:
        dict: Statistics about the removal process
    """
    # Load flagged translations
    data = load_flagged_translations(project_root)
    flagged_entries = data.get("flagged_translations", [])

    if not flagged_entries:
        print("No flagged translations found.")
        return {}

    # Find all content files
    content_files = find_content_files(project_root)

    # Track statistics and modified files
    stats = {
        "total_flagged": len(flagged_entries),
        "entries_removed": 0,
        "entries_not_found": 0,
        "files_modified": set()
    }

    # Group entries by source file for efficiency
    file_entry_map = defaultdict(list)
    entry_source_map = {}

    for entry in flagged_entries:
        entry_id = entry.get("id")
        if not entry_id:
            continue

        file_path, file_data = locate_entry_source(entry_id, content_files)
        if file_path and file_data:
            file_entry_map[file_path].append(entry_id)
            entry_source_map[entry_id] = file_path
        else:
            stats["entries_not_found"] += 1
            print(f"Could not find source for entry: {entry_id}")

    # Now process each file that needs modification
    for file_path, entry_ids in file_entry_map.items():
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                file_data = json.load(f)

            original_count = len(file_data.get("entries", []))

            # Filter out the flagged entries
            new_entries = [entry for entry in file_data.get("entries", [])
                           if entry.get("id") not in entry_ids]

            removed_count = original_count - len(new_entries)

            if removed_count > 0:
                # Update the entries and metadata
                file_data["entries"] = new_entries
                if "metadata" in file_data:
                    file_data["metadata"]["total_entries"] = len(new_entries)

                # Write the modified file
                with open(file_path, 'w', encoding='utf-8') as f:
                    json.dump(file_data, f, indent=2, ensure_ascii=False)

                stats["entries_removed"] += removed_count
                stats["files_modified"].add(str(file_path))
                print(f"Removed {removed_count} entries from {file_path}")
        except Exception as e:
            print(f"Error processing file {file_path}: {e}")

    # Convert set to list for JSON serialization
    stats["files_modified"] = list(stats["files_modified"])
    return stats

def main():
    """Main function"""
    print("Removing Original Flagged Translations")
    print("=" * 40)

    # Find project root directory
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Remove flagged entries from source files
    print("Processing flagged translations...")
    stats = remove_flagged_entries(project_root)

    # Print stats
    print("\nRemoval Complete")
    print("=" * 40)
    print(f"Total flagged entries: {stats.get('total_flagged', 0)}")
    print(f"Entries removed: {stats.get('entries_removed', 0)}")
    print(f"Entries not found: {stats.get('entries_not_found', 0)}")
    print(f"Files modified: {len(stats.get('files_modified', []))}")

    print("\nNext steps:")
    print("1. Run the Android and web copy scripts to update the apps:")
    print("   - python backend/copy-modern-to-android.py")
    print("   - python backend/copy-modern-to-web.py")

if __name__ == "__main__":
    main()