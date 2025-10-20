#!/usr/bin/env python3
"""
Process flagged translations and split them into individual entries

This script reads flagged translations from flagged-translations.txt and splits entries
marked with "split into subtranslations" into individual entries in the modern schema format.

Usage:
    python process-flagged-translations.py

Output:
    Creates/updates JSON files in the curated-content-modern directory structure.
"""

import json
import os
import re
import uuid
import datetime
from pathlib import Path

# Constants
FLAGGED_FILE = "curated-content-modern/flagged-translations.txt"
OUTPUT_DIR = "curated-content-modern"
SCHEMA_FILE = "curated-content-modern/schema.json"
SPLITTER_PATTERNS = [
    r', ',          # Simple comma+space
    r'; ',          # Semicolon+space
    r'\), ',        # closing parenthesis+comma+space
    r'\. ',         # period+space
    r'\? ',         # question mark+space
    r'[\n\r]+',     # newlines
]

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

def get_empty_schema():
    """Get an empty schema template"""
    now = datetime.datetime.now(datetime.timezone.utc).isoformat()

    return {
        "metadata": {
            "schema_version": "1.0",
            "created_date": now,
            "curator": "Flagged Translations Processor",
            "source_files": ["flagged-translations.txt"],
            "total_entries": 0,
            "description": "Generated from flagged translations"
        },
        "entries": []
    }

def clean_text(text):
    """Clean text by removing extra spaces and standardizing whitespace"""
    return re.sub(r'\s+', ' ', text).strip()

def split_entry(entry):
    """Split a flagged entry into individual entries

    Args:
        entry (dict): The original entry with multiple examples

    Returns:
        list: List of individual entries
    """
    # Get the original values
    orig_id = entry["id"]
    orig_kikuyu = entry["kikuyu"]
    orig_english = entry["english"]
    category = entry["category"]
    difficulty = entry["difficulty"]
    flag_reason = entry.get("flag_reason", "")

    # Handle "cultural context, not translation" differently
    if "cultural context" in flag_reason:
        # This is likely a glossary or cultural note, so don't split it
        # Just make a copy of the original
        new_entry = entry.copy()
        new_entry["id"] = f"{orig_id}_glossary"
        return [new_entry]

    # Determine the split type based on the content
    # Some entries are conversation-like with multiple sentences
    # Others are comma-separated lists of words/phrases

    # Handle cases with paired translations
    if " - " in orig_english and orig_kikuyu.endswith(")"):
        # Pattern suggests paired translations in parentheses
        splitter = None
        english_parts = []
        kikuyu_parts = []

        # Try to extract paired translations using regex
        paired_pattern = r'([^()]+?) \(([^()]+?)\)'
        paired_matches = re.findall(paired_pattern, orig_english)

        if paired_matches:
            for eng, kik in paired_matches:
                english_parts.append(eng.strip())
                kikuyu_parts.append(kik.strip())
        else:
            # If no paired matches, split by patterns
            for pattern in SPLITTER_PATTERNS:
                if re.search(pattern, orig_english):
                    splitter = pattern
                    english_parts = [clean_text(p) for p in re.split(pattern, orig_english) if p.strip()]
                    kikuyu_parts = [clean_text(p) for p in re.split(pattern, orig_kikuyu) if p.strip()]
                    if len(english_parts) == len(kikuyu_parts) and len(english_parts) > 1:
                        break

        # If we still don't have matching parts, fall back to original
        if len(english_parts) != len(kikuyu_parts) or len(english_parts) <= 1:
            english_parts = [orig_english]
            kikuyu_parts = [orig_kikuyu]

    # Special case: Phrase entries with conversations
    elif category == "phrases" and "Conversation" in orig_kikuyu:
        # Split by typical dialogue indicators
        english_parts = []
        kikuyu_parts = []

        # First try splitting by parentheses pattern
        split_pattern = r'([^()]+?) \(([^()]+?)\)'
        pairs = re.findall(split_pattern, orig_english)

        if pairs and len(pairs) > 1:
            for eng, kik in pairs:
                english_parts.append(f"{eng.strip()} ({kik.strip()})")
                # Use same for kikuyu since we don't have a direct mapping
                kikuyu_parts.append(f"{eng.strip()} ({kik.strip()})")
        else:
            # Alternative approach for conversation text
            # Split on commas, periods at end of sentences
            split_pattern = r'([^,.?!]+[,.?!])'
            english_sentences = re.findall(split_pattern, orig_english)
            if english_sentences and len(english_sentences) > 1:
                english_parts = [clean_text(s) for s in english_sentences]
                # Since we can't match exactly, we'll need to use the whole kikuyu string
                # for each part with appropriate context
                kikuyu_parts = [f"{orig_kikuyu} - {i+1}/{len(english_sentences)}"
                               for i in range(len(english_sentences))]
            else:
                # Fall back to using the whole thing
                english_parts = [orig_english]
                kikuyu_parts = [orig_kikuyu]

    # Regular word lists (most vocabulary entries)
    else:
        # Try all splitter patterns to see which one gives meaningful splits
        best_splitter = None
        english_parts = [orig_english]  # Default if no splitting works
        kikuyu_parts = [orig_kikuyu]    # Default if no splitting works

        for pattern in SPLITTER_PATTERNS:
            if re.search(pattern, orig_english):
                candidate_parts = [clean_text(p) for p in re.split(pattern, orig_english) if p.strip()]
                # Only use this splitter if it creates reasonable splits (more than one and less than too many)
                if len(candidate_parts) > 1 and len(candidate_parts) < 30:
                    best_splitter = pattern
                    english_parts = candidate_parts

                    # Try to split kikuyu text with the same pattern
                    kikuyu_candidate_parts = [clean_text(p) for p in re.split(pattern, orig_kikuyu) if p.strip()]

                    # Only use the kikuyu split if it matches the english split count
                    if len(kikuyu_candidate_parts) == len(candidate_parts):
                        kikuyu_parts = kikuyu_candidate_parts
                    else:
                        # If counts don't match, duplicate the kikuyu title for context
                        kikuyu_parts = [f"{orig_kikuyu} - {i+1}/{len(candidate_parts)}"
                                      for i in range(len(candidate_parts))]
                    break

    # Create new entries
    new_entries = []

    for i, (english, kikuyu) in enumerate(zip(english_parts, kikuyu_parts)):
        # Skip empty items
        if not english.strip() or not kikuyu.strip():
            continue

        # Clean up the text - remove leading/trailing periods, clean whitespace
        english = re.sub(r'^[\s.]+|[\s.]+$', '', english).strip()
        kikuyu = re.sub(r'^[\s.]+|[\s.]+$', '', kikuyu).strip()

        # Skip if after cleaning there's nothing left
        if not english or not kikuyu:
            continue

        # Create a new ID based on the original ID and index
        new_id = f"{orig_id}_{i+1:02d}"

        # For vocabulary items, try to extract actual Kikuyu term and English translation
        # from paired formats like "Term (meaning)" or "Meaning (term)"
        actual_kikuyu = kikuyu
        actual_english = english

        if category == "vocabulary":
            # Try to parse common patterns
            paired_match = re.match(r'^(.+?)\s*\((.+?)\)$', english)

            if paired_match:
                term, meaning = paired_match.groups()

                # Check if the term looks like Kikuyu (has special characters)
                if re.search(r'[ĩũāēīōūĀĒĪŌŪ]', term):
                    actual_kikuyu = term
                    actual_english = meaning
                else:
                    # English term with Kikuyu in parentheses
                    actual_english = term
                    # Only update Kikuyu if we don't already have a proper term
                    if "/" in kikuyu or "-" in kikuyu or kikuyu.startswith(orig_kikuyu):
                        actual_kikuyu = meaning

            # Clean up title-like kikuyu terms with numbers
            if re.search(r'^\w+ \d+/\d+$', kikuyu) or re.search(r' - \d+/\d+$', kikuyu):
                # Try to use the entry title as the Kikuyu term
                if "(" in orig_kikuyu and ")" in orig_kikuyu:
                    # Extract what's inside parentheses if it looks like the actual term
                    parens_match = re.search(r'\(([^)]+)\)', orig_kikuyu)
                    if parens_match:
                        actual_kikuyu = parens_match.group(1)
                else:
                    # Remove numbering and use the main title
                    title_parts = orig_kikuyu.split(" - ")[0].strip()
                    if title_parts and not re.search(r'\d+/\d+$', title_parts):
                        actual_kikuyu = title_parts

        # Create a new entry
        new_entry = {
            "id": new_id,
            "kikuyu": actual_kikuyu,
            "english": actual_english,
            "category": category,
            "difficulty": difficulty,
            "cultural_notes": entry.get("cultural_notes"),
            "source": entry.get("source"),
            "quality": 3  # Default quality for converted entries
        }

        # Strip out any None values
        new_entry = {k: v for k, v in new_entry.items() if v is not None}

        # For entries with special cultural terms, add cultural notes
        if category == "vocabulary" and "(" in english and ")" in english:
            if not new_entry.get("cultural_notes"):
                new_entry["cultural_notes"] = f"Split from original entry: {orig_kikuyu}"

        # For entries that are part of a conversation, add context
        if category == "phrases" and "Conversation" in orig_kikuyu:
            if not new_entry.get("cultural_notes"):
                new_entry["cultural_notes"] = f"From conversation: {orig_kikuyu}"

            # If this is a dialog line and doesn't already have a speaker indicator
            if not re.search(r'^[A-Za-z]+:', actual_english) and ":" not in actual_english:
                # Try to guess if it's a question or statement to suggest dialog context
                if "?" in actual_english:
                    new_entry["cultural_notes"] += " (Question)"
                elif "!" in actual_english:
                    new_entry["cultural_notes"] += " (Exclamation)"

        new_entries.append(new_entry)

    # If we couldn't split it properly, just return the original
    if len(new_entries) <= 1:
        return [entry.copy()]

    return new_entries

def process_flagged_translations(project_root):
    """Process flagged translations and split into individual entries

    Args:
        project_root (Path): Path to the project root

    Returns:
        dict: Statistics about the processing
    """
    # Load flagged translations
    data = load_flagged_translations(project_root)

    if not data or "flagged_translations" not in data:
        print("Error: No flagged translations found in file")
        exit(1)

    flagged_entries = data["flagged_translations"]

    # Categorize and process flagged entries
    categories = {}
    stats = {
        "total_entries": len(flagged_entries),
        "entries_split": 0,
        "total_new_entries": 0,
        "categories": {}
    }

    # First pass: Group by category and prepare category directories
    for entry in flagged_entries:
        category = entry.get("category", "unknown")

        if category not in categories:
            categories[category] = []

            # Make sure category directory exists
            category_dir = project_root / "backend" / OUTPUT_DIR / category
            category_dir.mkdir(parents=True, exist_ok=True)

            if category not in stats["categories"]:
                stats["categories"][category] = 0

        # Process the entry and add it to its category
        new_entries = split_entry(entry)
        categories[category].extend(new_entries)

        # Update stats
        if len(new_entries) > 1:
            stats["entries_split"] += 1
        stats["total_new_entries"] += len(new_entries)
        stats["categories"][category] += len(new_entries)

    # Second pass: Write each category to its own file
    for category, entries in categories.items():
        # Skip if no entries
        if not entries:
            continue

        # Sort entries by ID
        entries.sort(key=lambda e: e.get("id", ""))

        # Create output schema
        schema = get_empty_schema()
        schema["metadata"]["total_entries"] = len(entries)
        schema["metadata"]["description"] = f"Split flagged translations for category: {category}"
        schema["entries"] = entries

        # Write to output file
        output_file = project_root / "backend" / OUTPUT_DIR / category / f"split_flagged_{category}.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(schema, f, indent=2, ensure_ascii=False)

        print(f"Wrote {len(entries)} entries to {output_file}")

    return stats

def main():
    """Main function"""
    print("Processing Flagged Translations")
    print("=" * 40)

    # Find project root directory
    project_root = find_project_root()
    print(f"Project root: {project_root}")

    # Process flagged translations
    print("Processing flagged translations...")
    stats = process_flagged_translations(project_root)

    # Print stats
    print("\nProcessing Complete")
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