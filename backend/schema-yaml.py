#!/usr/bin/env python3
"""
Schema YAML Generator for Kikuyu Language Hub Curated Content

This script analyzes all JSON files in backend/curated-content/, builds a unified
YAML schema from their structures, unifies field types, and saves canonical schemas
to .schema-types/ directory for future reference and comparison.

Usage:
    python schema-yaml.py

Output:
    .schema-types/
    ├── canonical-schema.yaml          # Unified schema from all files
    ├── field-types.yaml              # All field types discovered
    ├── schema-stats.yaml             # Statistics about schema analysis
    └── file-schemas/                 # Individual file schemas
        ├── conjugations-schema.yaml
        ├── vocabulary-schema.yaml
        ├── grammar-schema.yaml
        └── ...
"""

import json
import yaml
import os
import sys
from pathlib import Path
from collections import defaultdict, Counter
from typing import Dict, List, Any, Set, Tuple
from datetime import datetime

class SchemaAnalyzer:
    """Analyzes JSON files and builds unified schemas"""

    def __init__(self, source_dir: str = "curated-content", output_dir: str = ".schema-types"):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir)
        self.all_schemas = {}  # Individual file schemas
        self.field_types = defaultdict(set)  # Field name -> set of types
        self.field_values = defaultdict(set)  # Field name -> set of values
        self.required_fields = defaultdict(set)  # Field name -> count of files requiring it
        self.optional_fields = defaultdict(set)  # Field name -> count of files where it's optional
        self.nested_structures = {}  # Complex nested field analysis
        self.enum_candidates = {}  # Fields that could be enums

    def analyze_all_files(self) -> None:
        """Analyze all JSON files in the source directory"""
        print(f"🔍 Analyzing JSON files in: {self.source_dir}")

        json_files = list(self.source_dir.rglob("*.json"))
        if not json_files:
            print(f"❌ No JSON files found in {self.source_dir}")
            sys.exit(1)

        print(f"📄 Found {len(json_files)} JSON files")

        for json_file in json_files:
            try:
                self.analyze_json_file(json_file)
            except Exception as e:
                print(f"⚠️  Error analyzing {json_file}: {e}")

    def analyze_json_file(self, file_path: Path) -> None:
        """Analyze a single JSON file and extract its schema"""
        print(f"🔍 Analyzing: {file_path.relative_to(self.source_dir)}")

        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # Extract schema from this file
        schema = self.extract_schema(data, file_path.name)
        category = self.get_category_from_path(file_path)

        # Store individual schema
        self.all_schemas[category] = {
            'file_path': str(file_path.relative_to(self.source_dir)),
            'schema': schema,
            'metadata': {
                'file_size': file_path.stat().st_size,
                'last_modified': datetime.fromtimestamp(file_path.stat().st_mtime).isoformat()
            }
        }

    def extract_schema(self, data: Any, context: str = "root") -> Dict:
        """Extract schema structure from JSON data"""
        if isinstance(data, dict):
            schema = {"type": "object", "properties": {}}
            required_fields = []

            for key, value in data.items():
                # Track field presence and types
                self.field_types[key].add(self.get_type_name(value))

                # Track field values for enum detection
                if isinstance(value, (str, int, float, bool)):
                    self.field_values[key].add(str(value))

                # Recursively extract nested schemas
                schema["properties"][key] = self.extract_schema(value, f"{context}.{key}")

                # Track required fields (appears in all instances)
                required_fields.append(key)

            schema["required"] = required_fields
            return schema

        elif isinstance(data, list):
            if data:
                # Analyze first item as representative
                return {
                    "type": "array",
                    "items": self.extract_schema(data[0], f"{context}[]")
                }
            else:
                return {"type": "array", "items": {}}

        else:
            return {"type": self.get_type_name(data)}

    def get_type_name(self, value: Any) -> str:
        """Get JSON schema type name for a value"""
        if isinstance(value, str):
            return "string"
        elif isinstance(value, int):
            return "integer"
        elif isinstance(value, float):
            return "number"
        elif isinstance(value, bool):
            return "boolean"
        elif isinstance(value, list):
            return "array"
        elif isinstance(value, dict):
            return "object"
        elif value is None:
            return "null"
        else:
            return "unknown"

    def get_category_from_path(self, file_path: Path) -> str:
        """Extract category from file path"""
        parts = file_path.parts
        if len(parts) > 1:
            return parts[-2]  # Parent directory name
        return file_path.stem

    def build_canonical_schema(self) -> Dict:
        """Build unified canonical schema from all analyzed files"""
        print("🏗️  Building canonical schema...")

        canonical = {
            "schema_metadata": {
                "generated_at": datetime.now().isoformat(),
                "source_files_analyzed": len(self.all_schemas),
                "total_fields_discovered": len(self.field_types),
                "generation_script": "schema-yaml.py"
            },
            "root_structure": {
                "type": "object",
                "properties": {},
                "required": []
            },
            "field_analysis": {
                "field_types": {},
                "enum_candidates": {},
                "nested_structures": {}
            }
        }

        # Analyze field patterns across all files
        for field_name, types in self.field_types.items():
            field_info = {
                "types": sorted(list(types)),
                "appears_in_files": [],
                "sample_values": list(self.field_values[field_name])[:10],
                "is_enum_candidate": False,
                "enum_values": []
            }

            # Determine if field could be an enum
            if len(self.field_values[field_name]) <= 50 and len(self.field_values[field_name]) > 1:
                if all(isinstance(v, str) for v in self.field_values[field_name]):
                    field_info["is_enum_candidate"] = True
                    field_info["enum_values"] = sorted(list(self.field_values[field_name]))

            canonical["field_analysis"]["field_types"][field_name] = field_info

        # Build root structure by finding common patterns
        root_properties = {}
        root_required = []

        # Analyze root-level structures from individual schemas
        for category, info in self.all_schemas.items():
            schema = info["schema"]
            if "properties" in schema:
                for prop_name, prop_schema in schema["properties"].items():
                    if prop_name not in root_properties:
                        root_properties[prop_name] = {
                            "type": prop_schema.get("type", "unknown"),
                            "description": f"Field found in: {', '.join(self.get_files_with_field(prop_name))}",
                            "appears_in": len(self.get_files_with_field(prop_name))
                        }

                        # Add nested properties if this is an object
                        if prop_schema.get("type") == "object" and "properties" in prop_schema:
                            root_properties[prop_name]["properties"] = prop_schema["properties"]

                        # Add items if this is an array
                        if prop_schema.get("type") == "array" and "items" in prop_schema:
                            root_properties[prop_name]["items"] = prop_schema["items"]

        # Determine required fields (appear in most files)
        for field_name in self.field_types:
            file_count = len(self.get_files_with_field(field_name))
            total_files = len(self.all_schemas)
            if file_count > total_files * 0.8:  # Appears in 80%+ of files
                root_required.append(field_name)

        canonical["root_structure"]["properties"] = root_properties
        canonical["root_structure"]["required"] = root_required

        return canonical

    def get_files_with_field(self, field_name: str) -> List[str]:
        """Get list of files that contain a specific field"""
        files_with_field = []
        for category, info in self.all_schemas.items():
            if self.field_appears_in_schema(info["schema"], field_name):
                files_with_field.append(info["file_path"])
        return files_with_field

    def field_appears_in_schema(self, schema: Dict, field_name: str) -> bool:
        """Recursively check if a field appears in a schema"""
        if isinstance(schema, dict):
            if "properties" in schema and field_name in schema["properties"]:
                return True
            for value in schema.values():
                if self.field_appears_in_schema(value, field_name):
                    return True
        elif isinstance(schema, list):
            for item in schema:
                if self.field_appears_in_schema(item, field_name):
                    return True
        return False

    def generate_statistics(self) -> Dict:
        """Generate statistics about the schema analysis"""
        stats = {
            "analysis_summary": {
                "total_json_files": len(self.all_schemas),
                "unique_field_names": len(self.field_types),
                "total_field_occurrences": sum(len(types) for types in self.field_types.values()),
                "enum_candidates": len([f for f, info in self.field_types.items()
                                       if len(self.field_values[f]) <= 50 and len(self.field_values[f]) > 1])
            },
            "field_type_distribution": {},
            "category_breakdown": {},
            "complexity_metrics": {
                "max_nesting_depth": 0,
                "average_fields_per_object": 0
            }
        }

        # Field type distribution
        type_counter = Counter()
        for types in self.field_types.values():
            for type_name in types:
                type_counter[type_name] += 1
        stats["field_type_distribution"] = dict(type_counter.most_common())

        # Category breakdown
        for category, info in self.all_schemas.items():
            field_count = len(self.get_all_fields_in_schema(info["schema"]))
            stats["category_breakdown"][category] = {
                "file_path": info["file_path"],
                "field_count": field_count,
                "file_size_kb": round(info["metadata"]["file_size"] / 1024, 2)
            }

        return stats

    def get_all_fields_in_schema(self, schema: Dict) -> Set[str]:
        """Recursively get all field names in a schema"""
        fields = set()
        if isinstance(schema, dict):
            if "properties" in schema:
                fields.update(schema["properties"].keys())
                for prop_schema in schema["properties"].values():
                    fields.update(self.get_all_fields_in_schema(prop_schema))
            else:
                for value in schema.values():
                    fields.update(self.get_all_fields_in_schema(value))
        elif isinstance(schema, list):
            for item in schema:
                fields.update(self.get_all_fields_in_schema(item))
        return fields

    def save_schemas(self) -> None:
        """Save all generated schemas to output directory"""
        print(f"💾 Saving schemas to: {self.output_dir}")

        # Create output directory
        self.output_dir.mkdir(exist_ok=True)
        (self.output_dir / "file-schemas").mkdir(exist_ok=True)

        # Save canonical schema
        canonical_schema = self.build_canonical_schema()
        with open(self.output_dir / "canonical-schema.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(canonical_schema, f, default_flow_style=False, sort_keys=False, indent=2)

        # Save field types analysis
        field_types_data = {
            "field_types": {k: sorted(list(v)) for k, v in self.field_types.items()},
            "field_values": {k: sorted(list(v)) for k, v in self.field_values.items()},
            "enum_candidates": {
                k: sorted(list(v))
                for k, v in self.field_values.items()
                if len(v) <= 50 and len(v) > 1
            }
        }
        with open(self.output_dir / "field-types.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(field_types_data, f, default_flow_style=False, sort_keys=True, indent=2)

        # Save statistics
        stats = self.generate_statistics()
        with open(self.output_dir / "schema-stats.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(stats, f, default_flow_style=False, sort_keys=False, indent=2)

        # Save individual file schemas
        for category, info in self.all_schemas.items():
            schema_file = self.output_dir / "file-schemas" / f"{category}-schema.yaml"
            with open(schema_file, 'w', encoding='utf-8') as f:
                yaml.dump(info, f, default_flow_style=False, sort_keys=False, indent=2)

        # Save summary report
        self.save_summary_report(canonical_schema, stats)

    def save_summary_report(self, canonical_schema: Dict, stats: Dict) -> None:
        """Save a human-readable summary report"""
        report = []
        report.append("# Kikuyu Language Hub - Schema Analysis Report")
        report.append(f"Generated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        report.append("")

        # Summary
        report.append("## Summary")
        report.append(f"- **JSON files analyzed**: {stats['analysis_summary']['total_json_files']}")
        report.append(f"- **Unique field names**: {stats['analysis_summary']['unique_field_names']}")
        report.append(f"- **Enum candidates**: {stats['analysis_summary']['enum_candidates']}")
        report.append("")

        # Most common field types
        report.append("## Most Common Field Types")
        for field_type, count in list(stats['field_type_distribution'].items())[:10]:
            report.append(f"- **{field_type}**: {count} occurrences")
        report.append("")

        # Categories analyzed
        report.append("## Categories Analyzed")
        for category, info in stats['category_breakdown'].items():
            report.append(f"### {category}")
            report.append(f"- File: `{info['file_path']}`")
            report.append(f"- Fields: {info['field_count']}")
            report.append(f"- Size: {info['file_size_kb']} KB")
            report.append("")

        # Enum candidates
        report.append("## Enum Candidates")
        enum_candidates = canonical_schema["field_analysis"]["enum_candidates"]
        if enum_candidates:
            for field_name, values in list(enum_candidates.items())[:10]:
                report.append(f"### {field_name}")
                for value in values[:5]:
                    report.append(f"- `{value}`")
                if len(values) > 5:
                    report.append(f"- ... and {len(values) - 5} more")
                report.append("")
        else:
            report.append("No enum candidates found.")
            report.append("")

        # Files created
        report.append("## Generated Files")
        report.append("- `canonical-schema.yaml` - Unified schema from all files")
        report.append("- `field-types.yaml` - Field types and enum candidates")
        report.append("- `schema-stats.yaml` - Detailed statistics")
        report.append("- `file-schemas/` - Individual file schemas")
        report.append("- `README.md` - This summary report")

        # Save report
        with open(self.output_dir / "README.md", 'w', encoding='utf-8') as f:
            f.write('\n'.join(report))

    def run(self) -> None:
        """Run the complete schema analysis pipeline"""
        try:
            print("🚀 Starting Schema YAML Generation")
            print("=" * 50)

            # Analyze all files
            self.analyze_all_files()

            # Generate and save schemas
            self.save_schemas()

            print("=" * 50)
            print("✅ Schema analysis completed successfully!")
            print(f"📁 Results saved to: {self.output_dir}")
            print("📋 Generated files:")
            print("   - canonical-schema.yaml")
            print("   - field-types.yaml")
            print("   - schema-stats.yaml")
            print("   - README.md")
            print("   - file-schemas/ (directory)")

        except Exception as e:
            print(f"❌ Error during schema analysis: {e}")
            sys.exit(1)

def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(
        description="Generate YAML schema from curated content JSON files"
    )
    parser.add_argument(
        "--source",
        default="curated-content",
        help="Source directory containing JSON files (default: curated-content)"
    )
    parser.add_argument(
        "--output",
        default=".schema-types",
        help="Output directory for schema files (default: .schema-types)"
    )

    args = parser.parse_args()

    # Change to backend directory if script is run from elsewhere
    script_dir = Path(__file__).parent
    if script_dir.name == "backend":
        os.chdir(script_dir)

    analyzer = SchemaAnalyzer(args.source, args.output)
    analyzer.run()

if __name__ == "__main__":
    main()