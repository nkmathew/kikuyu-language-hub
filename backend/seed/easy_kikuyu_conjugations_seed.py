#!/usr/bin/env python3
"""
Easy Kikuyu Conjugations Seed Script
Seeds database with verb conjugations and examples from Easy Kikuyu lessons
Native speaker verb patterns and tense examples from Emmanuel Kariuki's teachings
"""

import os
import sys
import json
from pathlib import Path
from typing import List, Tuple

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

def load_conjugation_data():
    """Load extracted conjugation data"""
    extraction_file = project_root / "easy_kikuyu_extracted.json"
    
    if not extraction_file.exists():
        print(f"Extraction file not found: {extraction_file}")
        print("Please run easy_kikuyu_extractor.py first")
        return []
    
    with open(extraction_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Get conjugations from 'conjugations' category and relevant 'mixed' items
    conjugation_items = data.get('conjugations', [])
    mixed_items = data.get('mixed', [])
    
    # Filter mixed items for conjugation content
    conjugations_from_mixed = [
        item for item in mixed_items 
        if item.get('difficulty') == 'INTERMEDIATE' and 
        ('conjugation' in item.get('context', '').lower() or 
         'verb' in item.get('context', '').lower() or
         'pattern' in item.get('context', '').lower())
    ]
    
    all_conjugations = conjugation_items + conjugations_from_mixed
    print(f"Loaded {len(all_conjugations)} conjugation items")
    
    return all_conjugations

def analyze_verb_structure(kikuyu_text: str) -> List[Tuple[str, str, str]]:
    """Analyze Kikuyu verb structure for morphological breakdown"""
    morphemes = []
    
    # Common patterns for first person (Nd-)
    if kikuyu_text.startswith('Nd'):
        morphemes.append(('Nd-', 'first person subject marker', 'Subject prefix'))
        remaining = kikuyu_text[2:]
        
        # Common verb root patterns
        if remaining.startswith('a'):
            morphemes.append(('a', 'present/past tense marker', 'Tense marker'))
            remaining = remaining[1:]
        elif remaining.startswith('e'):
            morphemes.append(('e', 'past tense marker', 'Tense marker'))
            remaining = remaining[1:]
        
        # Add root (simplified analysis)
        if len(remaining) > 2:
            morphemes.append((remaining, 'verb root and extensions', 'Root + Extensions'))
    
    # Pattern for recent past (Nj-)
    elif kikuyu_text.startswith('Nj'):
        morphemes.append(('Nj-', 'first person recent past', 'Subject + Tense'))
        remaining = kikuyu_text[2:]
        if len(remaining) > 2:
            morphemes.append((remaining, 'verb root and extensions', 'Root + Extensions'))
    
    # Other patterns
    else:
        # Simple fallback - just identify major parts
        if len(kikuyu_text) > 4:
            mid_point = len(kikuyu_text) // 2
            morphemes.append((kikuyu_text[:mid_point], 'prefix and markers', 'Prefix complex'))
            morphemes.append((kikuyu_text[mid_point:], 'root and suffixes', 'Root complex'))
    
    return morphemes

def create_easy_kikuyu_conjugations_seed():
    """Create seed data from Easy Kikuyu conjugation extractions"""
    
    # Load extracted data
    conjugation_data = load_conjugation_data()
    
    if not conjugation_data:
        print("No conjugation data to seed!")
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
            ("Easy Kikuyu Conjugations", "Verb conjugations from Easy Kikuyu lessons by Emmanuel Kariuki", "easy-kikuyu-conjugations"),
            ("Verb Patterns", "Kikuyu verb conjugation patterns and examples", "verb-patterns"),
            ("Tense Examples", "Examples of different tenses in Kikuyu", "tense-examples"),
            ("Native Speaker Grammar", "Grammatical patterns from native speaker content", "native-speaker-grammar"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1700  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Process conjugation items
        contribution_count = 0
        skipped_count = 0
        morphology_count = 0
        
        # Categorize conjugations by tense patterns
        tense_patterns = {
            'recent_past': [],      # Moments ago
            'earlier_today': [],    # Earlier today  
            'present': [],          # Present tense
            'general_patterns': [], # General verb patterns
            'first_person': [],     # First person examples
            'other': []
        }
        
        # Analyze and categorize conjugations
        for item in conjugation_data:
            context = item.get('context', '').lower()
            kikuyu = item.get('kikuyu', '')
            
            # Classify by tense/pattern type
            if 'recent past' in context or 'moments ago' in context:
                tense_patterns['recent_past'].append(item)
            elif 'earlier today' in context or 'early today' in context:
                tense_patterns['earlier_today'].append(item)
            elif 'present' in context:
                tense_patterns['present'].append(item)
            elif 'first person' in context:
                tense_patterns['first_person'].append(item)
            elif 'pattern' in context:
                tense_patterns['general_patterns'].append(item)
            else:
                tense_patterns['other'].append(item)
        
        # Process each tense pattern
        for pattern_type, items in tense_patterns.items():
            if not items:
                continue
                
            print(f"Processing {len(items)} {pattern_type.replace('_', ' ')} conjugations...")
            
            for item in items:
                english = item.get('english', '').strip()
                kikuyu = item.get('kikuyu', '').strip()
                context = item.get('context', '').strip()
                cultural_notes = item.get('cultural_notes', '').strip()
                quality_score = item.get('quality_score', 4.5)
                
                # Skip if empty or too short
                if len(english) < 3 or len(kikuyu) < 3:
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
                
                # Enhance context with pattern information
                enhanced_context = f"Verb conjugation - {pattern_type.replace('_', ' ').title()} - {context}"
                
                # Enhance cultural notes with grammatical explanation
                enhanced_cultural_notes = (
                    f"Native speaker verb conjugation from Easy Kikuyu lessons by Emmanuel Kariuki. "
                    f"{cultural_notes} This example demonstrates {pattern_type.replace('_', ' ')} "
                    f"conjugation patterns in natural Kikuyu speech, showing authentic usage "
                    f"and proper morphological structure."
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
                contribution.categories.append(categories["Easy Kikuyu Conjugations"])
                contribution.categories.append(categories["Verb Patterns"])
                contribution.categories.append(categories["Native Speaker Grammar"])
                
                # Add tense-specific category
                if pattern_type in ['recent_past', 'earlier_today', 'present']:
                    contribution.categories.append(categories["Tense Examples"])
                
                contribution_count += 1
                
                # Add morphological analysis for interesting verbs
                if (len(kikuyu.split()) == 1 and len(kikuyu) > 4 and 
                    (kikuyu.startswith('Nd') or kikuyu.startswith('Nj'))):
                    
                    morphemes = analyze_verb_structure(kikuyu)
                    
                    for i, (morpheme, meaning, category) in enumerate(morphemes):
                        sub_translation = SubTranslation(
                            parent_contribution_id=contribution.id,
                            source_word=meaning,
                            target_word=morpheme,
                            word_position=i,
                            context=f"{category} - morphological analysis",
                            created_by_id=admin_user.id
                        )
                        db.add(sub_translation)
                        morphology_count += 1
                    
                    # Mark parent as having sub-translations
                    contribution.has_sub_translations = True
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Easy Kikuyu conjugation contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate or invalid entries")
        if morphology_count > 0:
            print(f"Added morphological analysis for {morphology_count} morphemes")
        print("All Easy Kikuyu conjugation data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Conjugations Data Added:")
        print("- Native speaker verb conjugations from Emmanuel Kariuki's lessons")
        print("- Authentic tense patterns and morphological structures")
        print("- Pattern-based organization by tense and person")
        print("- Intermediate difficulty level for grammar learners")
        print("- Morphological breakdowns for complex verbs")
        print("- Real usage examples in natural contexts")
        
        # Print pattern summary
        print("\nPattern Distribution:")
        for pattern_type, items in tense_patterns.items():
            if items:
                print(f"  {pattern_type.replace('_', ' ').title()}: {len(items)} examples")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Conjugations", "Verb Patterns", "Tense Examples", "Native Speaker Grammar"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: These conjugations provide essential patterns for")
        print("understanding Kikuyu verb morphology, offering learners")
        print("authentic examples of how verbs change across different")
        print("tenses and grammatical contexts in natural speech.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu conjugations...")
    print("Source: Native speaker lessons from Emmanuel Kariuki (Easy Kikuyu)")
    try:
        create_easy_kikuyu_conjugations_seed()
        print("Easy Kikuyu conjugations seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)