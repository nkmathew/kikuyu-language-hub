#!/usr/bin/env python3
"""
Convert curated JSON files to modern optimal schema

This script converts legacy curated JSON files (batch_info/flashcards or metadata/entries)
to the optimal modern schema defined in optimal-flashcard-schema.json.

Usage:
    python convert-to-modern-schema.py

Output:
    curated-content-modern/  # Files converted to modern schema
"""

import json
import os
import sys
from pathlib import Path
from typing import Dict, Any, List
from datetime import datetime, timezone

class SchemaConverter:
    """Converts curated JSON files to modern optimal schema"""

    def __init__(self, source_dir: str = "curated-content", output_dir: str = "curated-content-modern"):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir).resolve()
        self.schema_version = "1.0"
        self.curator = "Schema Migration v1.0"

    def load_optimal_schema(self) -> Dict:
        """Load the optimal schema definition"""
        schema_file = Path("optimal-flashcard-schema.json")
        if not schema_file.exists():
            raise FileNotFoundError(f"Optimal schema file not found: {schema_file}")

        with open(schema_file, 'r', encoding='utf-8') as f:
            schema_data = json.load(f)

        return schema_data["optimal_schema"]

    def convert_legacy_to_modern(self, data: Dict, file_path: Path) -> Dict:
        """Convert legacy schema to modern schema"""

        # Initialize modern structure
        modern_data = {
            "metadata": {
                "schema_version": self.schema_version,
                "created_date": datetime.now(timezone.utc).isoformat(),
                "curator": self.curator,
                "source_files": [],
                "total_entries": 0,
                "description": f"Converted from {file_path.name} - {datetime.now().strftime('%Y-%m-%d')}"
            },
            "entries": []
        }

        # Handle different legacy formats
        if "batch_info" in data and "flashcards" in data:
            # Legacy format: batch_info + flashcards
            batch_info = data["batch_info"]
            flashcards = data["flashcards"]

            # Convert batch_info to metadata
            modern_data["metadata"].update({
                "source_files": batch_info.get("source_files", []),
                "total_entries": len(flashcards),
                "last_updated": batch_info.get("last_updated", batch_info.get("created_date")),
                "description": batch_info.get("description", f"Converted from {file_path.name}")
            })

            # Add curator from author if available
            if "author" in data:
                modern_data["metadata"]["curator"] = f"{data['author']} (Original) → {self.curator} (Converted)"

            # Convert flashcards to entries
            modern_data["entries"] = [self.convert_card_to_entry(card, batch_info, data) for card in flashcards]

        elif "metadata" in data and "entries" in data:
            # Modern format: metadata + entries (already in modern format, just ensure compliance)
            metadata = data["metadata"]
            entries = data["entries"]

            modern_data["metadata"].update({
                "source_files": metadata.get("source_files", []),
                "total_entries": len(entries),
                "last_updated": metadata.get("last_updated", metadata.get("created_date")),
                "description": metadata.get("description", f"Converted from {file_path.name}")
            })

            if "curator" in metadata:
                modern_data["metadata"]["curator"] = f"{metadata['curator']} (Original) → {self.curator} (Converted)"

            # Ensure entries comply with modern schema
            modern_data["entries"] = [self.convert_card_to_entry(card, metadata, data) for card in entries]

        else:
            # Simple format (rare, but handle it)
            # Check if it's a single flashcard (not an array)
            if self.is_single_flashcard(data):
                # Convert single flashcard to array format
                card = data
                modern_data["metadata"]["total_entries"] = 1
                modern_data["entries"] = [self.convert_card_to_entry(card, {}, data)]
            else:
                # Try to find entries-like structure
                entries = data.get("entries", data.get("flashcards", []))
                if isinstance(entries, list):
                    modern_data["metadata"]["total_entries"] = len(entries)
                    modern_data["entries"] = [self.convert_card_to_entry(card, {}, data) for card in entries]
                else:
                    # Create a single entry from the data itself
                    modern_data["metadata"]["total_entries"] = 1
                    modern_data["entries"] = [self.convert_card_to_entry(data, {}, data)]

        return modern_data

    def convert_card_to_entry(self, card: Dict, batch_metadata: Dict = {}, file_data: Dict = {}) -> Dict:
        """Convert individual card to modern entry format"""

        # Base entry with required fields
        entry = {
            "id": card.get("id", f"unknown-{self.simple_hash(str(card))[:8]}"),
            "english": card.get("english", card.get("source_text", "")),
            "kikuyu": card.get("kikuyu", card.get("target_text", "")),
            "category": card.get("category", "vocabulary"),
            "difficulty": card.get("difficulty", "beginner"),
            "source": self.create_source_info(card, batch_metadata, file_data)
        }

        # Add optional fields if present
        optional_fields = [
            "subcategory", "context", "cultural_notes", "examples",
            "grammatical_info", "tags", "quality"
        ]

        for field in optional_fields:
            if field in card:
                entry[field] = card[field]

        # Ensure quality field has required properties
        if "quality" not in entry:
            entry["quality"] = {
                "verified": False,
                "confidence_score": 4.0,
                "source_quality": "community"
            }

        # Ensure quality has required fields
        quality = entry["quality"]
        if "verified" not in quality:
            quality["verified"] = False
        if "confidence_score" not in quality:
            quality["confidence_score"] = 4.0
        if "source_quality" not in quality:
            quality["source_quality"] = "community"

        return entry

    def create_source_info(self, card: Dict, batch_metadata: Dict = {}, file_data: Dict = {}) -> Dict:
        """Create source information from various sources"""

        source_info = {
            "origin": "Unknown",
            "created_date": None,
            "last_updated": None
        }

        # Try to get source from card level
        if "source" in card:
            source_info.update(card["source"])

        # Try batch metadata level
        if "origin" in batch_metadata:
            source_info["origin"] = batch_metadata["origin"]
        if "attribution" in batch_metadata:
            source_info["attribution"] = batch_metadata["attribution"]
        if "license" in batch_metadata:
            source_info["license"] = batch_metadata["license"]
        if "created_date" in batch_metadata:
            source_info["created_date"] = batch_metadata["created_date"]
        if "last_updated" in batch_metadata:
            source_info["last_updated"] = batch_metadata["last_updated"]

        # Try file data level (for legacy files)
        if "author" in file_data:
            source_info["attribution"] = file_data["author"]
        if "created_date" in file_data:
            source_info["created_date"] = file_data["created_date"]
        if "last_updated" in file_data:
            source_info["last_updated"] = file_data["last_updated"]
        if "source" in file_data:
            source_info["origin"] = file_data["source"]

        return source_info

    def is_single_flashcard(self, data: Dict) -> bool:
        """Check if data represents a single flashcard rather than a collection"""
        # Single flashcard cards have direct fields like source_text, target_text
        single_card_indicators = [
            "source_text", "target_text", "english", "kikuyu",
            "context", "difficulty", "category"
        ]

        # Collection structures that indicate multiple cards
        collection_indicators = ["batch_info", "entries", "flashcards"]
        # Note: metadata alone doesn't indicate collection - could be single card metadata

        has_direct_fields = any(field in data for field in single_card_indicators)
        has_collection_structure = any(key in data for key in collection_indicators)

        # If it has direct card fields but no clear collection structure, it's likely a single card
        return has_direct_fields and not has_collection_structure

    def simple_hash(self, text: str) -> str:
        """Simple hash function for generating IDs (avoids recursion issues)"""
        # Use a simple rolling hash approach to avoid recursion
        hash_val = 0
        for char in text[:50]:  # Limit to first 50 chars to avoid long processing
            hash_val = (hash_val * 31 + ord(char)) % 1000000007
        return str(hash_val)[-8:]

    def convert_files(self, file_limit: int = 5) -> None:
        """Convert first N files to modern schema"""
        print(f"Converting first {file_limit} files to modern schema")
        print(f"Source: {self.source_dir}")
        print(f"Output: {self.output_dir}")

        # Create output directory
        self.output_dir.mkdir(exist_ok=True)

        # Get all JSON files
        json_files = list(self.source_dir.rglob("*.json"))
        if not json_files:
            print(f"No JSON files found in {self.source_dir}")
            return

        # Sort files to get consistent order
        json_files.sort()

        # Convert first N files
        converted_count = 0
        for json_file in json_files[:file_limit]:
            try:
                print(f"Converting: {json_file.relative_to(self.source_dir)}")

                # Load original file
                with open(json_file, 'r', encoding='utf-8') as f:
                    original_data = json.load(f)

                # Convert to modern schema
                modern_data = self.convert_legacy_to_modern(original_data, json_file)

                # Determine output path (preserve directory structure)
                relative_path = json_file.relative_to(self.source_dir)
                output_path = self.output_dir / relative_path

                # Create output directory if needed
                output_path.parent.mkdir(parents=True, exist_ok=True)

                # Save converted file
                with open(output_path, 'w', encoding='utf-8') as f:
                    json.dump(modern_data, f, indent=2, ensure_ascii=False)

                print(f"Saved: {output_path.name}")
                converted_count += 1

            except Exception as e:
                print(f"Error converting {json_file}: {e}")

        print(f"\nSuccessfully converted {converted_count} files to modern schema!")
        print(f"Output directory: {self.output_dir}")

def hash(text: str) -> str:
    """Simple hash function for generating IDs"""
    return str(hash(text))[-8:]

def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(
        description="Convert curated JSON files to modern optimal schema"
    )
    parser.add_argument(
        "--source",
        default="curated-content",
        help="Source directory containing JSON files (default: curated-content)"
    )
    parser.add_argument(
        "--output",
        default="curated-content-modern",
        help="Output directory for converted files (default: curated-content-modern)"
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=5,
        help="Number of files to convert (default: 5)"
    )

    args = parser.parse_args()

    # Change to backend directory if script is run from elsewhere
    script_dir = Path(__file__).parent
    if script_dir.name == "backend":
        os.chdir(script_dir)

    converter = SchemaConverter(args.source, args.output)
    converter.convert_files(args.limit)

if __name__ == "__main__":
    main()