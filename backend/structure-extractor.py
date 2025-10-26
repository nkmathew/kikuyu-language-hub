#!/usr/bin/env python3
"""
JSON Structure Extractor for Kikuyu Language Hub

This script reads each JSON file in curated-content/, extracts its structure/shape
(not the data), deduplicates identical structures, and outputs the different
variations to .schema-types/ directory as simple JSON.

Usage:
    python structure-extractor.py

Output:
    .schema-types/
    ├── structures.json                # Simple JSON output: {filename: structure}
    └── summary.json                   # Basic summary info
"""

import json
import os
import sys
from pathlib import Path
from typing import Dict, Any
from datetime import datetime
import hashlib

class StructureExtractor:
    """Extracts and deduplicates JSON structures"""

    def __init__(self, source_dir: str = "curated-content", output_dir: str = ".schema-types"):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir)
        self.structures = {}  # filename -> structure

    def describe_structure(self, data, level=0):
        """Recursively describe structure of JSON data."""
        if isinstance(data, dict):
            return {k: self.describe_structure(v, level + 1) for k, v in data.items()}
        elif isinstance(data, list):
            if not data:
                return []
            # summarize by the union of element structures
            return [self.describe_structure(data[0], level + 1)]
        else:
            return type(data).__name__

    def analyze_all_files(self) -> None:
        """Analyze all JSON files and extract their structures"""
        print(f"🔍 Analyzing JSON files in: {self.source_dir}")

        json_files = list(self.source_dir.rglob("*.json"))
        if not json_files:
            print(f"❌ No JSON files found in {self.source_dir}")
            sys.exit(1)

        print(f"📄 Found {len(json_files)} JSON files")

        for json_file in json_files:
            try:
                print(f"🔍 Processing: {json_file.relative_to(self.source_dir)}")

                with open(json_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)

                # Extract structure
                structure = self.describe_structure(data)
                filename = str(json_file.relative_to(self.source_dir))
                self.structures[filename] = structure

            except Exception as e:
                print(f"⚠️  Error processing {json_file}: {e}")

        print(f"✅ Extracted structures from {len(self.structures)} files")

    def save_structures(self) -> None:
        """Save structures to output directory"""
        print(f"💾 Saving structures to: {self.output_dir}")

        # Create output directory
        self.output_dir.mkdir(exist_ok=True)

        # Save main structures JSON
        with open(self.output_dir / "structures.json", 'w', encoding='utf-8') as f:
            json.dump(self.structures, f, indent=2, sort_keys=True)

        # Save summary
        summary = {
            "generated_at": datetime.now().isoformat(),
            "total_files": len(self.structures),
            "unique_structures": len(set(json.dumps(s, sort_keys=True) for s in self.structures.values())),
            "files": list(self.structures.keys())
        }

        with open(self.output_dir / "summary.json", 'w', encoding='utf-8') as f:
            json.dump(summary, f, indent=2, sort_keys=True)

        print(f"✅ Saved {len(self.structures)} structures")

    def run(self) -> None:
        """Run the complete structure extraction pipeline"""
        try:
            print("🚀 Starting JSON Structure Extraction")
            print("=" * 50)

            # Analyze all files
            self.analyze_all_files()

            # Save results
            self.save_structures()

            print("=" * 50)
            print("✅ Structure extraction completed successfully!")
            print(f"📁 Results saved to: {self.output_dir}")
            print("📋 Generated files:")
            print("   - structures.json  # {filename: structure} mapping")
            print("   - summary.json     # Basic summary info")

        except Exception as e:
            print(f"❌ Error during structure extraction: {e}")
            sys.exit(1)

def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(
        description="Extract JSON structures from curated content"
    )
    parser.add_argument(
        "--source",
        default="curated-content",
        help="Source directory containing JSON files (default: curated-content)"
    )
    parser.add_argument(
        "--output",
        default=".schema-types",
        help="Output directory for structure files (default: .schema-types)"
    )

    args = parser.parse_args()

    # Change to backend directory if script is run from elsewhere
    script_dir = Path(__file__).parent
    if script_dir.name == "backend":
        os.chdir(script_dir)

    extractor = StructureExtractor(args.source, args.output)
    extractor.run()

if __name__ == "__main__":
    main()