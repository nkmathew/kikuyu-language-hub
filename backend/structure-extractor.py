#!/usr/bin/env python3
"""
JSON Structure Extractor for Kikuyu Language Hub

This script reads each JSON file in curated-content/, extracts its structure/shape
(not the data), deduplicates identical structures, and outputs the different
variations to .schema-types/ directory.

Usage:
    python structure-extractor.py

Output:
    .schema-types/
    ├── structure-variations.yaml      # All unique structures found
    ├── structure-summary.yaml         # Summary of findings
    └── structures/                    # Individual structure files
        ├── structure-001.yaml         # First unique structure
        ├── structure-002.yaml         # Second unique structure
        └── ...
"""

import json
import yaml
import os
import sys
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Any, Tuple
from datetime import datetime
import hashlib

class StructureExtractor:
    """Extracts and deduplicates JSON structures"""

    def __init__(self, source_dir: str = "curated-content", output_dir: str = ".schema-types"):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir)
        self.structures = []  # List of (structure_hash, structure_shape, file_paths)
        self.file_count = 0
        self.unique_count = 0

    def extract_structure(self, data: Any, max_depth: int = 10, current_depth: int = 0) -> Any:
        """Extract the structure/shape from JSON data (ignore values, keep types)"""
        if current_depth > max_depth:
            return "max_depth_reached"

        if isinstance(data, dict):
            structure = {}
            for key, value in sorted(data.items()):  # Sort for consistent hashing
                structure[key] = self.extract_structure(value, max_depth, current_depth + 1)
            return {"type": "object", "properties": structure}

        elif isinstance(data, list):
            if data:
                # Use first element as representative, but note it's an array
                return {
                    "type": "array",
                    "items": self.extract_structure(data[0], max_depth, current_depth + 1)
                }
            else:
                return {"type": "array", "items": "empty"}

        elif isinstance(data, str):
            return {"type": "string"}

        elif isinstance(data, int):
            return {"type": "integer"}

        elif isinstance(data, float):
            return {"type": "number"}

        elif isinstance(data, bool):
            return {"type": "boolean"}

        elif data is None:
            return {"type": "null"}

        else:
            return {"type": "unknown"}

    def get_structure_hash(self, structure: Any) -> str:
        """Generate hash for structure (for deduplication)"""
        structure_str = json.dumps(structure, sort_keys=True, separators=(',', ':'))
        return hashlib.md5(structure_str.encode()).hexdigest()[:12]

    def analyze_all_files(self) -> None:
        """Analyze all JSON files and extract their structures"""
        print(f"🔍 Analyzing JSON files in: {self.source_dir}")

        json_files = list(self.source_dir.rglob("*.json"))
        if not json_files:
            print(f"❌ No JSON files found in {self.source_dir}")
            sys.exit(1)

        print(f"📄 Found {len(json_files)} JSON files")

        structure_map = {}  # hash -> (structure, file_paths)

        for json_file in json_files:
            try:
                self.file_count += 1
                print(f"🔍 Processing: {json_file.relative_to(self.source_dir)}")

                with open(json_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)

                # Extract structure
                structure = self.extract_structure(data)
                structure_hash = self.get_structure_hash(structure)

                # Group by structure
                if structure_hash in structure_map:
                    structure_map[structure_hash][1].append(str(json_file.relative_to(self.source_dir)))
                else:
                    structure_map[structure_hash] = (structure, [str(json_file.relative_to(self.source_dir))])

            except Exception as e:
                print(f"⚠️  Error processing {json_file}: {e}")

        # Convert to list and sort by file count (most common first)
        self.structures = [
            (hash_val, structure, file_paths)
            for hash_val, (structure, file_paths) in structure_map.items()
        ]
        self.structures.sort(key=lambda x: len(x[2]), reverse=True)
        self.unique_count = len(self.structures)

        print(f"✅ Found {self.unique_count} unique structures from {self.file_count} files")

    def save_structures(self) -> None:
        """Save all unique structures to output directory"""
        print(f"💾 Saving structures to: {self.output_dir}")

        # Create output directory
        self.output_dir.mkdir(exist_ok=True)
        (self.output_dir / "structures").mkdir(exist_ok=True)

        # Save main variations file
        variations_data = {
            "summary": {
                "generated_at": datetime.now().isoformat(),
                "total_files_analyzed": self.file_count,
                "unique_structures_found": self.unique_count,
                "most_common_structure_count": len(self.structures[0][2]) if self.structures else 0
            },
            "unique_structures": []
        }

        # Save each unique structure
        for i, (structure_hash, structure, file_paths) in enumerate(self.structures):
            structure_name = f"structure-{i+1:03d}"

            # Add to main variations file
            variations_data["unique_structures"].append({
                "name": structure_name,
                "hash": structure_hash,
                "file_count": len(file_paths),
                "files": file_paths,
                "structure": structure
            })

            # Save individual structure file
            structure_data = {
                "name": structure_name,
                "hash": structure_hash,
                "file_count": len(file_paths),
                "files": file_paths,
                "structure": structure,
                "generated_at": datetime.now().isoformat()
            }

            structure_file = self.output_dir / "structures" / f"{structure_name}.yaml"
            with open(structure_file, 'w', encoding='utf-8') as f:
                yaml.dump(structure_data, f, default_flow_style=False, sort_keys=False, indent=2)

        # Save main variations file
        with open(self.output_dir / "structure-variations.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(variations_data, f, default_flow_style=False, sort_keys=False, indent=2)

        # Save summary report
        self.save_summary_report()

    def save_summary_report(self) -> None:
        """Save human-readable summary"""
        summary = {
            "analysis_summary": {
                "generated_at": datetime.now().isoformat(),
                "total_files": self.file_count,
                "unique_structures": self.unique_count,
                "compression_ratio": round(self.unique_count / self.file_count * 100, 1) if self.file_count > 0 else 0
            },
            "structure_breakdown": []
        }

        for i, (structure_hash, structure, file_paths) in enumerate(self.structures):
            summary["structure_breakdown"].append({
                "rank": i + 1,
                "name": f"structure-{i+1:03d}",
                "file_count": len(file_paths),
                "percentage": round(len(file_paths) / self.file_count * 100, 1),
                "files": file_paths,
                "top_level_keys": list(structure.get("properties", {}).keys()) if structure.get("type") == "object" else []
            })

        with open(self.output_dir / "structure-summary.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(summary, f, default_flow_style=False, sort_keys=False, indent=2)

        # Also create a readable README
        readme_content = f"""# JSON Structure Analysis - Kikuyu Language Hub

Generated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## Summary

- **Total JSON files analyzed**: {self.file_count}
- **Unique structures found**: {self.unique_count}
- **Compression ratio**: {round(self.unique_count / self.file_count * 100, 1) if self.file_count > 0 else 0}% (unique structures / total files)

## Structure Variations

"""
        for i, (structure_hash, structure, file_paths) in enumerate(self.structures[:10]):  # Top 10
            readme_content += f"""
### {i+1}. Structure-{i+1:03d} (Most Common)

- **Files**: {len(file_paths)} ({round(len(file_paths) / self.file_count * 100, 1)}%)
- **Hash**: `{structure_hash}`
- **Sample files**:
"""
            for file_path in file_paths[:3]:
                readme_content += f"  - `{file_path}`\n"
            if len(file_paths) > 3:
                readme_content += f"  - ... and {len(file_paths) - 3} more\n"

            # Show structure preview
            readme_content += f"- **Structure**:\n```yaml\n{yaml.dump(structure, default_flow_style=False, sort_keys=False)}\n```\n"

        if len(self.structures) > 10:
            readme_content += f"\n... and {len(self.structures) - 10} more structures.\n"

        readme_content += """
## Files Generated

- `structure-variations.yaml` - All unique structures with file mappings
- `structure-summary.yaml` - Summary statistics and breakdown
- `structures/` - Individual files for each unique structure
- `README.md` - This summary report

## Usage

Use these structure variations to:
1. Understand the different JSON formats in your curated content
2. Create validation schemas for each structure type
3. Identify which files need to be standardized
4. Track schema evolution over time
"""

        with open(self.output_dir / "README.md", 'w', encoding='utf-8') as f:
            f.write(readme_content)

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
            print(f"📊 Found {self.unique_count} unique structures from {self.file_count} files")
            print("📋 Generated files:")
            print("   - structure-variations.yaml")
            print("   - structure-summary.yaml")
            print("   - README.md")
            print("   - structures/ (directory)")

        except Exception as e:
            print(f"❌ Error during structure extraction: {e}")
            sys.exit(1)

def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(
        description="Extract and deduplicate JSON structures from curated content"
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