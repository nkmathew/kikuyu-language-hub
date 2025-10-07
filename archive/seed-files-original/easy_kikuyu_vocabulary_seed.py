#!/usr/bin/env python3
"""
Easy Kikuyu Vocabulary Seed Script
Seeds database with vocabulary items extracted from Easy Kikuyu lessons
Native speaker content from Emmanuel Kariuki's Facebook lessons
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
from datetime import datetime

def load_extracted_data():
    """Load extracted vocabulary data"""
    extraction_file = project_root / "easy_kikuyu_extracted.json"
    
    if not extraction_file.exists():
        print(f"Extraction file not found: {extraction_file}")
        print("Please run easy_kikuyu_extractor.py first")
        return []
    
    with open(extraction_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Combine vocabulary from 'vocabulary' and relevant 'mixed' items
    vocabulary_items = data.get('vocabulary', [])
    mixed_items = data.get('mixed', [])
    
    # Filter mixed items for vocabulary content
    vocab_from_mixed = [
        item for item in mixed_items 
        if item.get('difficulty') == 'BEGINNER' or 'vocabulary' in item.get('context', '').lower()
    ]
    
    all_vocabulary = vocabulary_items + vocab_from_mixed
    print(f"Loaded {len(all_vocabulary)} vocabulary items")
    
    return all_vocabulary

def create_easy_kikuyu_vocabulary_seed():
    """Create seed data from Easy Kikuyu vocabulary extractions"""
    
    # Load extracted data
    vocabulary_data = load_extracted_data()
    
    if not vocabulary_data:
        print("No vocabulary data to seed!")
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
            ("Easy Kikuyu Vocabulary", "Native speaker vocabulary from Easy Kikuyu lessons by Emmanuel Kariuki", "easy-kikuyu-vocabulary"),
            ("Native Speaker Content", "Authentic content from native Kikuyu speakers", "native-speaker-content"),
            ("Beginner Vocabulary", "Essential vocabulary for Kikuyu language beginners", "beginner-vocabulary"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1500  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Process vocabulary items
        contribution_count = 0
        skipped_count = 0
        
        # Group vocabulary by themes for better organization
        themed_vocabulary = {
            'cooking': [],
            'animals': [],
            'directions': [],
            'household': [],
            'general': []
        }
        
        # Categorize vocabulary by themes
        for item in vocabulary_data:
            context = item.get('context', '').lower()
            cultural_notes = item.get('cultural_notes', '').lower()
            
            if 'cooking' in context or 'cooking' in cultural_notes:
                themed_vocabulary['cooking'].append(item)
            elif 'animal' in context or 'animal' in cultural_notes:
                themed_vocabulary['animals'].append(item)
            elif 'direction' in context or 'geography' in context:
                themed_vocabulary['directions'].append(item)
            elif 'household' in context or 'table' in cultural_notes or 'Class III' in context:
                themed_vocabulary['household'].append(item)
            else:
                themed_vocabulary['general'].append(item)
        
        # Process each theme
        for theme, items in themed_vocabulary.items():
            if not items:
                continue
                
            print(f"Processing {len(items)} {theme} vocabulary items...")
            
            for item in items:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.5)
                
                # Skip if empty or too short
                if len(english) < 2 or len(kikuyu) < 2:
                    skipped_count += 1
                    continue
                
                # Check if this contribution already exists
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    skipped_count += 1
                    continue
                
                # Enhance context with theme information
                enhanced_context = f"{context}"
                if theme != 'general':
                    enhanced_context = f"Theme: {theme.title()} - {context}"
                
                # Enhance cultural notes
                enhanced_cultural_notes = f"Native speaker vocabulary from Easy Kikuyu lessons by Emmanuel Kariuki. {cultural_notes}"
                if theme == 'cooking':
                    enhanced_cultural_notes += " Essential cooking and food preparation terminology."
                elif theme == 'animals':
                    enhanced_cultural_notes += " Traditional animal names and wildlife vocabulary."
                elif theme == 'directions':
                    enhanced_cultural_notes += " Geographic and directional terms rooted in Kikuyu landscape."
                elif theme == 'household':
                    enhanced_cultural_notes += " Everyday household items and domestic vocabulary."
                
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,
                    language="kikuyu",
                    difficulty_level=DifficultyLevel.BEGINNER,
                    context_notes=enhanced_context,
                    cultural_notes=enhanced_cultural_notes,
                    quality_score=quality_score,
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()
                
                # Associate with categories
                contribution.categories.append(categories["Easy Kikuyu Vocabulary"])
                contribution.categories.append(categories["Native Speaker Content"])
                contribution.categories.append(categories["Beginner Vocabulary"])
                
                contribution_count += 1
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Easy Kikuyu vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate or invalid entries")
        print("All Easy Kikuyu vocabulary data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Vocabulary Data Added:")
        print("- Native speaker vocabulary from Emmanuel Kariuki's lessons")
        print("- Authentic everyday terms and expressions")
        print("- Thematic organization (cooking, animals, directions, household)")
        print("- Beginner-friendly difficulty level")
        print("- High quality scores from native speaker source")
        
        # Print theme summary
        print("\nThematic Distribution:")
        for theme, items in themed_vocabulary.items():
            if items:
                print(f"  {theme.title()}: {len(items)} items")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Vocabulary", "Native Speaker Content", "Beginner Vocabulary"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: This vocabulary represents authentic native speaker")
        print("content, providing learners with practical, everyday Kikuyu")
        print("terms used in natural conversation and daily life.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu vocabulary...")
    print("Source: Native speaker lessons from Emmanuel Kariuki (Easy Kikuyu)")
    try:
        create_easy_kikuyu_vocabulary_seed()
        print("Easy Kikuyu vocabulary seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)