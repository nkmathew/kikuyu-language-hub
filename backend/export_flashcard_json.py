#!/usr/bin/env python3
"""
Export Flashcard JSON
Exports database content to static JSON files optimized for flashcard application
Creates chunked files by category for better performance
"""

import os
import sys
import json
from pathlib import Path
from collections import defaultdict

# Add the project root to Python path
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from sqlalchemy.orm import Session
from app.db.session import engine
from app.models.contribution import Contribution, ContributionStatus, DifficultyLevel
from app.models.category import Category
from app.models.sub_translation import SubTranslation

def export_flashcard_data():
    """Export all approved contributions as flashcard-optimized JSON"""
    
    with Session(engine) as db:
        
        # Get all approved contributions with their categories and sub-translations
        contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).all()
        
        print(f"Found {len(contributions)} approved contributions")
        
        # Organize data by categories
        categorized_data = defaultdict(list)
        all_categories = set()
        
        for contribution in contributions:
            # Convert to flashcard format
            flashcard = {
                "id": contribution.id,
                "english": contribution.source_text,
                "kikuyu": contribution.target_text,
                "difficulty": contribution.difficulty_level.value if contribution.difficulty_level else "INTERMEDIATE",
                "context": contribution.context_notes or "",
                "cultural_notes": contribution.cultural_notes or "",
                "quality_score": float(contribution.quality_score) if contribution.quality_score else 4.0,
                "categories": [cat.name for cat in contribution.categories],
                "has_sub_translations": contribution.has_sub_translations or False
            }
            
            # Add sub-translations if they exist
            if contribution.has_sub_translations:
                sub_translations = db.query(SubTranslation).filter(
                    SubTranslation.parent_contribution_id == contribution.id
                ).order_by(SubTranslation.word_position).all()
                
                flashcard["sub_translations"] = [
                    {
                        "source": sub.source_word,
                        "target": sub.target_word,
                        "position": sub.word_position,
                        "context": sub.context or ""
                    }
                    for sub in sub_translations
                ]
            
            # Categorize based on primary category or content analysis
            primary_category = None
            categories = [cat.name for cat in contribution.categories]
            all_categories.update(categories)
            
            # Determine primary category for organization
            if any("vocabulary" in cat.lower() or "beginner" in cat.lower() for cat in categories):
                primary_category = "vocabulary"
            elif any("proverb" in cat.lower() or "wisdom" in cat.lower() or "saying" in cat.lower() for cat in categories):
                primary_category = "proverbs"
            elif any("conjugation" in cat.lower() or "verb" in cat.lower() or "tense" in cat.lower() for cat in categories):
                primary_category = "conjugations"
            elif any("grammar" in cat.lower() or "rule" in cat.lower() for cat in categories):
                primary_category = "grammar"
            else:
                # Analyze content to determine category
                if len(contribution.source_text.split()) <= 3 and len(contribution.target_text.split()) <= 3:
                    primary_category = "vocabulary"
                elif any(word in contribution.source_text.lower() for word in ["proverb", "saying", "traditional", "wisdom"]):
                    primary_category = "proverbs"
                elif "conjugation" in contribution.context_notes.lower() or "verb" in contribution.context_notes.lower():
                    primary_category = "conjugations"
                else:
                    primary_category = "general"
            
            categorized_data[primary_category].append(flashcard)
        
        # Create output directory
        output_dir = Path(__file__).parent.parent / "kikuyu-flashcards" / "data"
        output_dir.mkdir(parents=True, exist_ok=True)
        
        # Export chunked files by category
        category_stats = {}
        
        for category, items in categorized_data.items():
            filename = f"{category}.json"
            filepath = output_dir / filename
            
            # Sort by difficulty (beginner first) and quality score
            def sort_key(item):
                difficulty_order = {"BEGINNER": 0, "INTERMEDIATE": 1, "ADVANCED": 2}
                return (difficulty_order.get(item["difficulty"], 1), -item["quality_score"])
            
            items.sort(key=sort_key)
            
            # Split by difficulty for easier filtering
            beginner_items = [item for item in items if item["difficulty"] == "BEGINNER"]
            intermediate_items = [item for item in items if item["difficulty"] == "INTERMEDIATE"]
            advanced_items = [item for item in items if item["difficulty"] == "ADVANCED"]
            
            category_data = {
                "category": category,
                "total_count": len(items),
                "difficulty_counts": {
                    "beginner": len(beginner_items),
                    "intermediate": len(intermediate_items),
                    "advanced": len(advanced_items)
                },
                "items": {
                    "beginner": beginner_items,
                    "intermediate": intermediate_items,
                    "advanced": advanced_items,
                    "all": items
                }
            }
            
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(category_data, f, indent=2, ensure_ascii=False)
            
            category_stats[category] = {
                "count": len(items),
                "beginner": len(beginner_items),
                "intermediate": len(intermediate_items),
                "advanced": len(advanced_items),
                "file": filename
            }
            
            print(f"Exported {len(items)} items to {filename}")
        
        # Export categories metadata
        categories_metadata = {
            "categories": list(all_categories),
            "primary_categories": list(categorized_data.keys()),
            "statistics": category_stats,
            "total_contributions": len(contributions),
            "export_timestamp": str(Path(__file__).stat().st_mtime)
        }
        
        categories_file = output_dir / "categories.json"
        with open(categories_file, 'w', encoding='utf-8') as f:
            json.dump(categories_metadata, f, indent=2, ensure_ascii=False)
        
        # Export a combined file for smaller datasets
        combined_data = {
            "metadata": categories_metadata,
            "content": dict(categorized_data)
        }
        
        combined_file = output_dir / "all_content.json"
        with open(combined_file, 'w', encoding='utf-8') as f:
            json.dump(combined_data, f, indent=2, ensure_ascii=False)
        
        print(f"\n{'='*60}")
        print("FLASHCARD EXPORT SUMMARY")
        print(f"{'='*60}")
        print(f"Total contributions exported: {len(contributions)}")
        print(f"Output directory: {output_dir}")
        print(f"\nCategory breakdown:")
        
        for category, stats in category_stats.items():
            print(f"  {category.upper()}:")
            print(f"    Total: {stats['count']}")
            print(f"    Beginner: {stats['beginner']}")
            print(f"    Intermediate: {stats['intermediate']}")
            print(f"    Advanced: {stats['advanced']}")
            print(f"    File: {stats['file']}")
        
        print(f"\nFiles created:")
        print(f"  - categories.json (metadata)")
        print(f"  - all_content.json (combined)")
        for category in categorized_data.keys():
            print(f"  - {category}.json")
        
        print(f"\n✅ Flashcard JSON export completed successfully!")
        return output_dir

if __name__ == "__main__":
    print("Exporting database content to flashcard JSON files...")
    try:
        output_dir = export_flashcard_data()
        print(f"\n🎯 Ready for flashcard application!")
        print(f"📁 Data files location: {output_dir}")
    except Exception as e:
        print(f"❌ Error during export: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)