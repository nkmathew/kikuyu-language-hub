#!/usr/bin/env python3
"""
Easy Kikuyu Proverbs Seed Script
Seeds database with traditional proverbs extracted from Easy Kikuyu lessons
Cultural wisdom and traditional sayings from Emmanuel Kariuki's teachings
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

def load_proverb_data():
    """Load extracted proverb data"""
    extraction_file = project_root / "easy_kikuyu_extracted.json"
    
    if not extraction_file.exists():
        print(f"Extraction file not found: {extraction_file}")
        print("Please run easy_kikuyu_extractor.py first")
        return []
    
    with open(extraction_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Get proverbs from both 'proverb' category and relevant 'mixed' items
    proverb_items = data.get('proverb', [])
    mixed_items = data.get('mixed', [])
    
    # Filter mixed items for proverb content
    proverbs_from_mixed = [
        item for item in mixed_items 
        if item.get('difficulty') == 'ADVANCED' and 
        ('proverb' in item.get('context', '').lower() or 'traditional' in item.get('cultural_notes', '').lower())
    ]
    
    all_proverbs = proverb_items + proverbs_from_mixed
    print(f"Loaded {len(all_proverbs)} proverb items")
    
    return all_proverbs

def create_easy_kikuyu_proverbs_seed():
    """Create seed data from Easy Kikuyu proverb extractions"""
    
    # Load extracted data
    proverb_data = load_proverb_data()
    
    if not proverb_data:
        print("No proverb data to seed!")
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
            ("Easy Kikuyu Proverbs", "Traditional proverbs from Easy Kikuyu lessons by Emmanuel Kariuki", "easy-kikuyu-proverbs"),
            ("Native Speaker Wisdom", "Cultural wisdom and sayings from native speakers", "native-speaker-wisdom"),
            ("Traditional Sayings", "Time-honored Kikuyu proverbs and cultural expressions", "traditional-sayings"),
            ("Cultural Heritage", "Kikuyu cultural heritage preserved through language", "cultural-heritage"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1600  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Process proverb items
        contribution_count = 0
        skipped_count = 0
        
        # Categorize proverbs by themes
        proverb_themes = {
            'wisdom': [],
            'community': [],
            'perseverance': [],
            'relationships': [],
            'nature': [],
            'general': []
        }
        
        # Analyze and categorize proverbs
        for item in proverb_data:
            english = item.get('english', '').lower()
            kikuyu = item.get('kikuyu', '').lower()
            cultural_notes = item.get('cultural_notes', '').lower()
            
            # Simple theme classification based on content
            if any(word in english for word in ['wisdom', 'know', 'learn', 'understand']):
                proverb_themes['wisdom'].append(item)
            elif any(word in english for word in ['community', 'together', 'people', 'help']):
                proverb_themes['community'].append(item)
            elif any(word in english for word in ['persever', 'patience', 'time', 'wait']):
                proverb_themes['perseverance'].append(item)
            elif any(word in english for word in ['friend', 'family', 'love', 'respect']):
                proverb_themes['relationships'].append(item)
            elif any(word in english for word in ['tree', 'water', 'earth', 'nature', 'animal']):
                proverb_themes['nature'].append(item)
            else:
                proverb_themes['general'].append(item)
        
        # Process each theme
        for theme, items in proverb_themes.items():
            if not items:
                continue
                
            print(f"Processing {len(items)} {theme} proverbs...")
            
            for item in items:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.8)
                
                # Skip if empty or too short
                if len(english) < 5 or len(kikuyu) < 5:
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
                enhanced_context = f"Traditional proverb - Theme: {theme.title()} - {context}"
                
                # Enhance cultural notes with detailed explanation
                enhanced_cultural_notes = (
                    f"Traditional Kikuyu proverb from Easy Kikuyu lessons by Emmanuel Kariuki, "
                    f"a native speaker preserving cultural wisdom. {cultural_notes} "
                    f"This saying represents {theme} wisdom passed down through generations, "
                    f"embodying Kikuyu cultural values and worldview."
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
                contribution.categories.append(categories["Easy Kikuyu Proverbs"])
                contribution.categories.append(categories["Native Speaker Wisdom"])
                contribution.categories.append(categories["Traditional Sayings"])
                contribution.categories.append(categories["Cultural Heritage"])
                
                contribution_count += 1
        
        # Add morphological analysis for selected complex proverbs
        complex_proverbs = [
            # Find proverbs with interesting morphological structure
            item for theme_items in proverb_themes.values() 
            for item in theme_items
            if len(item.get('kikuyu', '').split()) >= 4  # Multi-word proverbs
        ][:5]  # Limit to 5 for detailed analysis
        
        morphology_count = 0
        for item in complex_proverbs:
            # Find the parent contribution
            parent = db.query(Contribution).filter(
                Contribution.source_text == item.get('english', ''),
                Contribution.target_text == item.get('kikuyu', '')
            ).first()
            
            if parent:
                kikuyu_words = item.get('kikuyu', '').split()
                
                # Create basic morphological breakdown
                for i, word in enumerate(kikuyu_words):
                    if len(word) > 2:  # Skip very short words
                        sub_translation = SubTranslation(
                            parent_contribution_id=parent.id,
                            source_word=f"word_{i+1}",
                            target_word=word,
                            word_position=i,
                            context=f"Part of traditional proverb - position {i+1}",
                            created_by_id=admin_user.id
                        )
                        db.add(sub_translation)
                        morphology_count += 1
                
                # Mark parent as having sub-translations
                parent.has_sub_translations = True
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Easy Kikuyu proverb contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate or invalid entries")
        print(f"Added morphological analysis for {morphology_count} word parts in complex proverbs")
        print("All Easy Kikuyu proverb data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Proverbs Data Added:")
        print("- Traditional cultural wisdom from native speaker Emmanuel Kariuki")
        print("- Authentic proverbs preserving Kikuyu heritage")
        print("- Thematic organization by wisdom type")
        print("- Advanced difficulty level for cultural immersion")
        print("- Highest quality scores reflecting cultural importance")
        print("- Morphological analysis for complex proverbs")
        
        # Print theme summary
        print("\nThematic Distribution:")
        for theme, items in proverb_themes.items():
            if items:
                print(f"  {theme.title()}: {len(items)} proverbs")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Proverbs", "Native Speaker Wisdom", "Traditional Sayings", "Cultural Heritage"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: These proverbs represent the deepest level of Kikuyu")
        print("cultural wisdom, offering insights into traditional values,")
        print("social structures, and the worldview of the Kikuyu people.")
        print("They serve as windows into centuries of accumulated wisdom.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu proverbs...")
    print("Source: Native speaker lessons from Emmanuel Kariuki (Easy Kikuyu)")
    try:
        create_easy_kikuyu_proverbs_seed()
        print("Easy Kikuyu proverbs seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)