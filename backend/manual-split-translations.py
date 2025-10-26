#!/usr/bin/env python3
"""
Manual Split Translations

This script manually splits flagged translations into individual entries
with careful attention to formatting and structure. It creates separate JSON
files for each category (vocabulary, phrases, etc.) in the modern schema format.

Usage:
    python manual-split-translations.py

"""

import json
import os
import re
import datetime
from pathlib import Path

# Constants
FLAGGED_FILE = "curated-content-modern/flagged-translations.txt"
OUTPUT_DIR = "curated-content-modern"

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

def load_flagged_translations(project_root):
    """Load flagged translations from the text file"""
    flagged_file = project_root / "backend" / FLAGGED_FILE

    if not flagged_file.exists():
        print(f"Error: Flagged translations file not found: {flagged_file}")
        exit(1)

    try:
        with open(flagged_file, 'r', encoding='utf-8') as f:
            content = f.read()
            # Fix potential BOM or encoding issues
            content = content.strip()
            if content.startswith('\ufeff'):  # BOM character
                content = content[1:]

        # Try to parse as JSON
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

    except Exception as e:
        print(f"Error reading flagged translations: {e}")
        exit(1)

def get_empty_schema():
    """Get an empty schema template"""
    now = datetime.datetime.now(datetime.timezone.utc).isoformat()

    return {
        "metadata": {
            "schema_version": "1.0",
            "created_date": now,
            "curator": "Manual Split Translations",
            "source_files": ["flagged-translations.txt"],
            "total_entries": 0,
            "description": "Manually split flagged translations"
        },
        "entries": []
    }

def clean_text(text):
    """Clean text by removing extra spaces and standardizing whitespace"""
    return re.sub(r'\s+', ' ', text).strip()

def manually_split_entry(entry, split_index=0):
    """Manually split an entry into individual entries based on content type

    Args:
        entry (dict): Original flagged entry
        split_index (int): Running index to ensure unique IDs

    Returns:
        list: List of split entries
    """
    # Extract basic information
    orig_id = entry.get("id")
    orig_kikuyu = entry.get("kikuyu", "")
    orig_english = entry.get("english", "")
    category = entry.get("category", "")
    difficulty = entry.get("difficulty", "beginner")
    source = entry.get("source", {"origin": "Manual split"})

    # Skip entries that aren't flagged for splitting
    if "split into subtranslations" not in entry.get("flag_reason", ""):
        # Just keep the original for other flag reasons like cultural context
        return [entry]

    new_entries = []

    # Different splitting strategies based on content
    if category == "phrases" and "Conversation" in orig_kikuyu:
        # ============ PHRASE CONVERSATIONS ============
        # Identify if it's dot-separated or comma-separated
        if "). " in orig_english:
            # Dot-separated pattern: "Phrase (translation). "
            parts = re.split(r'\.\s+', orig_english)
        else:
            # Comma-separated pattern: "Phrase (translation), "
            parts = [p.strip() for p in re.split(r',\s+', orig_english)]

        # Clean up parts
        parts = [p for p in parts if p.strip()]

        # Create new entry for each part
        for i, part in enumerate(parts):
            # Make sure it has the pattern "Phrase (translation)"
            if "(" in part and ")" in part:
                part = part.strip()
                # Remove trailing punctuation
                part = re.sub(r'[,.?!]+$', '', part)

                # Separate Kikuyu phrase and English translation
                phrase_match = re.match(r'([^(]+)\s*\(([^)]+)\)', part)

                if phrase_match:
                    kikuyu_phrase = phrase_match.group(1).strip()
                    english_trans = phrase_match.group(2).strip()

                    # For conversational context
                    is_question = "?" in part
                    is_exclamation = "!" in part
                    context_note = f"From conversation: {orig_kikuyu}"

                    if is_question:
                        context_note += " (Question)"
                    elif is_exclamation:
                        context_note += " (Exclamation)"

                    # Create entry
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": kikuyu_phrase,
                        "english": english_trans,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,  # Default quality
                        "cultural_notes": context_note
                    }
                    new_entries.append(new_entry)
                else:
                    # If no pattern match, use the whole part
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": part,
                        "english": part,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,
                        "cultural_notes": f"From conversation: {orig_kikuyu}"
                    }
                    new_entries.append(new_entry)

    elif category == "vocabulary":
        # ============ VOCABULARY LISTS ============
        # Most vocab entries are comma-separated lists of terms

        # Check if it's a list of pairs like "Term (meaning), "
        if re.search(r'\([^)]+\),', orig_english):
            # Split by comma and clean
            parts = [p.strip() for p in re.split(r',\s+', orig_english)]
            parts = [p for p in parts if p.strip()]

            for i, part in enumerate(parts):
                # Remove trailing punctuation
                part = re.sub(r'[,.;]+$', '', part)

                # Extract term and meaning
                term_match = re.match(r'([^(]+)\s*\(([^)]+)\)', part)

                if term_match:
                    term = term_match.group(1).strip()
                    meaning = term_match.group(2).strip()

                    # Special case for Kikuyu language terms
                    # If term has special characters, it's likely Kikuyu
                    # If not, it's likely English with Kikuyu in parentheses
                    if re.search(r'[ĩũāēīōūĀĒĪŌŪ]', term) or re.search(r'[ĩũāēīōūĀĒĪŌŪ]', orig_kikuyu):
                        kikuyu_term = term
                        english_meaning = meaning
                    else:
                        kikuyu_term = meaning
                        english_meaning = term

                    # Create entry
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": kikuyu_term,
                        "english": english_meaning,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,
                        "cultural_notes": f"From {orig_kikuyu}"
                    }
                    new_entries.append(new_entry)
                else:
                    # If no pattern match, use the whole part
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": part,
                        "english": part,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,
                        "cultural_notes": f"From {orig_kikuyu}"
                    }
                    new_entries.append(new_entry)

    # If we couldn't split using the specific methods, use a generic approach
    if not new_entries:
        # Generic splitting by common separators
        separators = [", ", "; ", " / ", ". "]

        # Find which separator is used most in this text
        best_sep = None
        most_parts = 1

        for sep in separators:
            parts = orig_english.split(sep)
            if len(parts) > most_parts:
                most_parts = len(parts)
                best_sep = sep

        # If we found a good separator with multiple parts
        if best_sep and most_parts > 1:
            parts = [p.strip() for p in orig_english.split(best_sep)]
            parts = [p for p in parts if p.strip()]

            for i, part in enumerate(parts):
                # Clean up and create entry
                part = re.sub(r'[,.;]+$', '', part)

                # Check for parenthetical pattern
                part_match = re.match(r'([^(]+)\s*\(([^)]+)\)', part)

                if part_match:
                    term = part_match.group(1).strip()
                    meaning = part_match.group(2).strip()

                    # Create entry with extracted term/meaning
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": term,
                        "english": meaning,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,
                        "cultural_notes": f"From {orig_kikuyu}"
                    }
                else:
                    # Use the whole part
                    new_entry = {
                        "id": f"{orig_id}_{i+1:02d}",
                        "kikuyu": part,
                        "english": part,
                        "category": category,
                        "difficulty": difficulty,
                        "source": source,
                        "quality": 3,
                        "cultural_notes": f"From {orig_kikuyu}"
                    }

                new_entries.append(new_entry)

        # If all else fails, just use the original entry
        if not new_entries:
            new_entries = [entry]

    # Final processing for all entries: remove any None values
    for i, entry in enumerate(new_entries):
        new_entries[i] = {k: v for k, v in entry.items() if v is not None}

    return new_entries

def process_flagged_translations(project_root):
    """Process and manually split flagged translations

    Args:
        project_root (Path): Project root directory

    Returns:
        dict: Statistics about the processing
    """
    # Load flagged translations
    data = load_flagged_translations(project_root)

    if not data or "flagged_translations" not in data:
        print("Error: No flagged translations found in file")
        exit(1)

    flagged_entries = data["flagged_translations"]

    # Group entries by category
    categories = {}
    stats = {
        "total_entries": len(flagged_entries),
        "entries_split": 0,
        "total_new_entries": 0,
        "categories": {}
    }

    # Process each entry and group by category
    split_index = 0
    for entry in flagged_entries:
        category = entry.get("category", "unknown")

        if category not in categories:
            categories[category] = []
            # Make sure category directory exists
            category_dir = project_root / "backend" / OUTPUT_DIR / category
            category_dir.mkdir(parents=True, exist_ok=True)

            if category not in stats["categories"]:
                stats["categories"][category] = 0

        # Manually split the entry
        split_entries = manually_split_entry(entry, split_index)
        split_index += len(split_entries)

        categories[category].extend(split_entries)

        # Update stats
        if len(split_entries) > 1:
            stats["entries_split"] += 1
        stats["total_new_entries"] += len(split_entries)
        stats["categories"][category] += len(split_entries)

    # Write each category to its own file
    for category, entries in categories.items():
        # Skip if no entries
        if not entries:
            continue

        # Sort entries by ID
        entries.sort(key=lambda e: e.get("id", ""))

        # Create output schema
        schema = get_empty_schema()
        schema["metadata"]["total_entries"] = len(entries)
        schema["metadata"]["description"] = f"Manually split flagged translations for category: {category}"
        schema["entries"] = entries

        # Write to output file
        output_file = project_root / "backend" / OUTPUT_DIR / category / f"manual_split_{category}.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(schema, f, indent=2, ensure_ascii=False)

        print(f"Wrote {len(entries)} entries to {output_file}")

    return stats

def main():
    """Main function"""
    print("Manual Split Translations")
    print("=" * 40)

    # Find project root directory
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Process flagged translations
    print("Processing flagged translations...")
    stats = process_flagged_translations(project_root)

    # Print stats
    print("\nManual Split Complete")
    print("=" * 40)
    print(f"Total flagged entries: {stats['total_entries']}")
    print(f"Entries split: {stats['entries_split']}")
    print(f"Total new entries: {stats['total_new_entries']}")
    print("\nEntries by category:")
    for category, count in stats["categories"].items():
        print(f"  {category}: {count} entries")

    print("\nNext steps:")
    print("1. Verify the split entries in each category file")
    print("2. Run the Android and web copy scripts to update the apps:")
    print("   - python backend/copy-modern-to-android.py")
    print("   - python backend/copy-modern-to-web.py")

if __name__ == "__main__":
    main()