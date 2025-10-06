#!/usr/bin/env python3
"""
Easy Kikuyu Comprehensive Seed Script
Seeds database with all remaining content from Easy Kikuyu lessons
Includes grammar examples, mixed content, and cultural notes
"""

import os
import sys
import json
from pathlib import Path

# Add the project root to Python path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from sqlalchemy.orm import Session
from app.db.session import engine
from app.models.contribution import Contribution, ContributionStatus, DifficultyLevel
from app.models.category import Category
from app.models.user import User, UserRole
from app.models.sub_translation import SubTranslation
from datetime import datetime

def load_comprehensive_data():
    """Load all remaining extracted data"""
    extraction_file = project_root / "easy_kikuyu_extracted.json"
    
    if not extraction_file.exists():
        print(f"Extraction file not found: {extraction_file}")
        print("Please run easy_kikuyu_extractor.py first")
        return {}
    
    with open(extraction_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Get remaining content categories
    remaining_content = {
        'grammar': data.get('grammar', []),
        'mixed_advanced': [  # Advanced mixed content not yet processed
            item for item in data.get('mixed', [])
            if item.get('difficulty') == 'ADVANCED' and 
            'proverb' not in item.get('context', '').lower()
        ],
        'mixed_general': [  # Other mixed content
            item for item in data.get('mixed', [])
            if (item.get('difficulty') != 'ADVANCED' and 
                item.get('difficulty') != 'BEGINNER' and
                'vocabulary' not in item.get('context', '').lower() and
                'conjugation' not in item.get('context', '').lower())
        ]
    }
    
    total_items = sum(len(items) for items in remaining_content.values())
    print(f"Loaded {total_items} remaining items across {len(remaining_content)} categories")
    
    return remaining_content

def create_easy_kikuyu_comprehensive_seed():
    """Create seed data from remaining Easy Kikuyu content"""
    
    # Load extracted data
    comprehensive_data = load_comprehensive_data()
    
    if not comprehensive_data or not any(comprehensive_data.values()):
        print("No remaining data to seed!")
        return
    
    # Create database session
    with Session(engine) as db:
        
        # Get or create admin user for seeding
        admin_user = db.query(User).filter(User.role == UserRole.ADMIN).first()
        if not admin_user:
            print("No admin user found. Creating seed admin user...")
            admin_user = User(
                email="seed_admin@kikuyu.hub",
                password_hash="$2b$12$dummy_hash_for_seeding",
                role=UserRole.ADMIN,
                display_name="Seed Admin"
            )
            db.add(admin_user)
            db.commit()
            db.refresh(admin_user)
        
        # Get or create categories
        categories_data = [
            ("Easy Kikuyu Grammar", "Grammar rules and examples from Easy Kikuyu lessons", "easy-kikuyu-grammar"),
            ("Easy Kikuyu Advanced", "Advanced content from Easy Kikuyu lessons", "easy-kikuyu-advanced"),
            ("Easy Kikuyu General", "General content from Easy Kikuyu lessons", "easy-kikuyu-general"),
            ("Kikuyu Language Rules", "Grammatical rules and linguistic patterns", "kikuyu-language-rules"),
            ("Educational Content", "Educational materials for Kikuyu language learning", "educational-content"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1800  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Process each content type
        total_contribution_count = 0
        total_skipped_count = 0
        total_morphology_count = 0
        
        # Process grammar content
        grammar_items = comprehensive_data.get('grammar', [])
        if grammar_items:
            print(f"Processing {len(grammar_items)} grammar items...")
            
            for item in grammar_items:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.3)
                
                # Skip if empty or too short
                if len(english) < 3 or len(kikuyu) < 3:
                    total_skipped_count += 1
                    continue
                
                # Check if this contribution already exists
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    total_skipped_count += 1
                    continue
                
                # Enhance context and notes for grammar
                enhanced_context = f"Grammar rule/example - {context}"
                enhanced_cultural_notes = (
                    f"Grammatical explanation from Easy Kikuyu lessons by Emmanuel Kariuki. "
                    f"{cultural_notes} This demonstrates important structural patterns "
                    f"in Kikuyu grammar and syntax."
                )
                
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,
                    language="kikuyu",
                    difficulty_level=DifficultyLevel.INTERMEDIATE,
                    context_notes=enhanced_context,
                    cultural_notes=enhanced_cultural_notes,
                    quality_score=quality_score,
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()
                
                # Associate with categories
                contribution.categories.append(categories["Easy Kikuyu Grammar"])
                contribution.categories.append(categories["Kikuyu Language Rules"])
                contribution.categories.append(categories["Educational Content"])
                
                total_contribution_count += 1
        
        # Process advanced mixed content
        mixed_advanced = comprehensive_data.get('mixed_advanced', [])
        if mixed_advanced:
            print(f"Processing {len(mixed_advanced)} advanced mixed items...")
            
            for item in mixed_advanced:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.6)
                
                # Skip if empty or too short
                if len(english) < 3 or len(kikuyu) < 3:
                    total_skipped_count += 1
                    continue
                
                # Check if this contribution already exists
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    total_skipped_count += 1
                    continue
                
                # Enhance context and notes for advanced content
                enhanced_context = f"Advanced content - {context}"
                enhanced_cultural_notes = (
                    f"Advanced Kikuyu content from Easy Kikuyu lessons by Emmanuel Kariuki. "
                    f"{cultural_notes} This represents sophisticated language usage "
                    f"requiring deeper cultural and linguistic understanding."
                )
                
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,
                    language="kikuyu",
                    difficulty_level=DifficultyLevel.ADVANCED,
                    context_notes=enhanced_context,
                    cultural_notes=enhanced_cultural_notes,
                    quality_score=quality_score,
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()
                
                # Associate with categories
                contribution.categories.append(categories["Easy Kikuyu Advanced"])
                contribution.categories.append(categories["Educational Content"])
                
                total_contribution_count += 1
        
        # Process general mixed content
        mixed_general = comprehensive_data.get('mixed_general', [])
        if mixed_general:
            print(f"Processing {len(mixed_general)} general mixed items...")
            
            for item in mixed_general:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.4)
                difficulty = item.get('difficulty', 'INTERMEDIATE')
                
                # Skip if empty or too short
                if len(english) < 3 or len(kikuyu) < 3:
                    total_skipped_count += 1
                    continue
                
                # Check if this contribution already exists
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    total_skipped_count += 1
                    continue
                
                # Map difficulty to enum
                difficulty_level = DifficultyLevel.INTERMEDIATE
                if difficulty == 'BEGINNER':
                    difficulty_level = DifficultyLevel.BEGINNER
                elif difficulty == 'ADVANCED':
                    difficulty_level = DifficultyLevel.ADVANCED
                
                # Enhance context and notes for general content
                enhanced_context = f"General content - {context}"
                enhanced_cultural_notes = (
                    f"General Kikuyu content from Easy Kikuyu lessons by Emmanuel Kariuki. "
                    f"{cultural_notes} This provides additional learning material "
                    f"for comprehensive language acquisition."
                )
                
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,
                    language="kikuyu",
                    difficulty_level=difficulty_level,
                    context_notes=enhanced_context,
                    cultural_notes=enhanced_cultural_notes,
                    quality_score=quality_score,
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()
                
                # Associate with categories
                contribution.categories.append(categories["Easy Kikuyu General"])
                contribution.categories.append(categories["Educational Content"])
                
                total_contribution_count += 1
        
        db.commit()
        
        print(f"Successfully created {total_contribution_count} new Easy Kikuyu comprehensive contributions")
        if total_skipped_count > 0:
            print(f"Skipped {total_skipped_count} duplicate or invalid entries")
        print("All Easy Kikuyu comprehensive data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Comprehensive Data Added:")
        print("- Grammar rules and linguistic explanations")
        print("- Advanced cultural and linguistic content")
        print("- General educational materials")
        print("- Mixed difficulty levels for diverse learners")
        print("- Native speaker authenticity throughout")
        
        # Print category breakdown
        content_breakdown = {
            'Grammar': len(grammar_items),
            'Advanced Mixed': len(mixed_advanced),
            'General Mixed': len(mixed_general)
        }
        
        print("\nContent Type Distribution:")
        for content_type, count in content_breakdown.items():
            if count > 0:
                print(f"  {content_type}: {count} items")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Grammar", "Easy Kikuyu Advanced", "Easy Kikuyu General", "Educational Content"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: This comprehensive collection completes the Easy")
        print("Kikuyu lesson extraction, providing a full spectrum of")
        print("educational content from basic vocabulary to advanced")
        print("cultural and grammatical concepts.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu comprehensive content...")
    print("Source: Native speaker lessons from Emmanuel Kariuki (Easy Kikuyu)")
    try:
        create_easy_kikuyu_comprehensive_seed()
        print("Easy Kikuyu comprehensive seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)