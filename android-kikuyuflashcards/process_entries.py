#!/usr/bin/env python3
import json
import os

# Constants
JSON_PATH = "app/src/main/assets/all_entries.json"

# IDs to remove completely (pronunciation guides and others)
IDS_TO_REMOVE = [
    "vocab-014-001_01", "vocab-014-001_02", "vocab-014-001_03",
    "vocab-014-001_04", "vocab-014-001_05", "vocab-014-001_06",
    "vocab-014-001_07", "vocab-014-001_08", "phrase-017-014",
    "vocab-018-001", "vocab-025-001", "vocab-009-004",
    "vocab-015-003_09"
]

# IDs that need content_type: section_header
SECTION_HEADER_IDS = [
    "conj-022-002", "conj-025-001", "conj-026-001", "conj-026-002",
    "gram-010-001", "gram-010-002", "grammar-012-001", "grammar-012-002",
    "grammar-013-001", "grammar-014-001", "grammar-014-002", "grammar-014-003",
    "grammar-016-001", "grammar-017-001", "grammar-017-002", "grammar-017-003",
    "grammar-018-001", "grammar-018-002", "grammar-019-001", "grammar-019-002",
    "grammar-020-001", "grammar-020-002", "grammar-021-001", "grammar-021-002",
    "grammar-022-001", "grammar-022-002", "grammar-023-001", "grammar-023-002",
    "grammar-024-001", "grammar-025-001", "grammar-027-001", "grammar-027-002",
    "unknown-61085435", "vocab-011-005"
]

def process_entries():
    # Load the entries file
    with open(JSON_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Get original count
    original_count = len(data["entries"])
    print(f"Original entry count: {original_count}")

    # Create backup
    with open(f"{JSON_PATH}.bak", "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Created backup at {JSON_PATH}.bak")

    # Filter out the entries to remove
    filtered_entries = []
    for entry in data["entries"]:
        if entry["id"] in IDS_TO_REMOVE:
            try:
                print(f"Removing entry: {entry['id']} - {entry['english'][:30]}...")
            except UnicodeEncodeError:
                print(f"Removing entry: {entry['id']} (Unicode characters)")
            continue

        # Add content_type field for section headers
        if entry["id"] in SECTION_HEADER_IDS:
            entry["content_type"] = "section_header"
            print(f"Added content_type: section_header to {entry['id']}")
        else:
            # Add the default content_type for regular translations
            entry["content_type"] = "translation"

        filtered_entries.append(entry)

    # Update the entries in the data structure
    data["entries"] = filtered_entries
    data["metadata"]["total_entries"] = len(filtered_entries)

    # Write the updated data back to the file
    with open(JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Updated entry count: {len(filtered_entries)}")
    print(f"Removed {original_count - len(filtered_entries)} entries")
    print(f"Added content_type field to {len(filtered_entries)} entries")
    print(f"Updated metadata total_entries to {len(filtered_entries)}")
    print("Done!")

if __name__ == "__main__":
    process_entries()