#!/usr/bin/env python3
"""
Update Android Curated Content

This script updates the all_entries.json file in the Android app's
curated-content directory with the cleaned entries from the backend.
"""

import json
import os
from pathlib import Path
import shutil

def find_project_root():
    """Find the project root directory"""
    current_dir = Path(__file__).resolve().parent
    max_levels = 5
    for _ in range(max_levels):
        if current_dir.name == "backend":
            return current_dir.parent
        if current_dir.parent == current_dir:
            break
        current_dir = current_dir.parent
    print("Error: Could not find project root directory.")
    exit(1)

def collect_cleaned_entries(project_root):
    """Collect all cleaned entries from backend curated content"""
    backend_dir = project_root / "backend" / "curated-content-modern"

    all_entries = []
    category_counts = {}

    print("Collecting cleaned entries from backend...")

    for category_dir in backend_dir.glob("*"):
        if not category_dir.is_dir():
            continue

        category = category_dir.name
        category_entries = []

        for json_file in category_dir.glob("*.json"):
            if json_file.name in ["schema.json", "all_entries.json"]:
                continue

            try:
                with open(json_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)

                if "entries" in data:
                    for entry in data["entries"]:
                        if not entry.get("kikuyu") or not entry.get("english"):
                            continue
                        entry["category"] = category
                        category_entries.append(entry)
            except Exception as e:
                print(f"Error processing {json_file}: {e}")

        print(f"  {category}: {len(category_entries)} entries")
        all_entries.extend(category_entries)
        category_counts[category] = len(category_entries)

    print(f"\nTotal cleaned entries: {len(all_entries)}")
    print(f"Categories: {category_counts}")
    return all_entries

def update_android_curated_content(project_root, all_entries):
    """Update the Android app's all_entries.json file"""
    android_file = project_root / "android-kikuyuflashcards" / "app" / "src" / "main" / "assets" / "curated-content" / "all_entries.json"

    # Create backup
    backup_file = android_file.with_suffix('.json.backup')
    if android_file.exists():
        shutil.copy2(android_file, backup_file)
        print(f"Created backup: {backup_file}")

    # Create curated content structure
    curated_content = {
        "entries": all_entries,
        "metadata": {
            "version": "1.0",
            "created_date": "2025-10-20T00:00:00.000000+00:00",
            "last_updated": "2025-10-20T00:00:00.000000+00:00",
            "source": "backend-curated-content-modern",
            "total_entries": len(all_entries),
            "categories": list(set(entry.get("category", "general") for entry in all_entries)),
            "description": "All cleaned entries from backend curated content"
        }
    }

    # Write to Android directory
    with open(android_file, 'w', encoding='utf-8') as f:
        json.dump(curated_content, f, indent=2, ensure_ascii=False)

    print(f"Updated {android_file} with {len(all_entries)} entries")
    return android_file

def main():
    """Main function"""
    print("Update Android Curated Content")
    print("=" * 40)

    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Collect cleaned entries
    all_entries = collect_cleaned_entries(project_root)

    if not all_entries:
        print("No entries found to update!")
        return

    # Update Android curated content
    android_file = update_android_curated_content(project_root, all_entries)

    print(f"\nSuccessfully updated Android curated content!")
    print(f"Rebuild and reinstall the app to see the changes.")

if __name__ == "__main__":
    main()