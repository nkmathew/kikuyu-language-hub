#!/usr/bin/env python3
"""
Seed script for additional Kikuyu verb with linguistic details
Adds verb with etymology, pronunciation, and derived forms
"""

import os
import sys
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
import json


def create_linguistic_verb_seed():
    """Create seed data for Kikuyu verb with detailed linguistic information"""
    
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
            ("Infinitives", "Infinitive verb forms", "infinitives"),
            ("Etymology", "Word origins and historical development", "etymology-linguistic"),
            ("Pronunciation Guide", "Phonetic information and pronunciation guides", "pronunciation-guide"),
            ("Derived Forms", "Words derived from base forms", "derived-forms-linguistic"),
            ("Noun Classes", "Examples of different noun class patterns", "noun-classes"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 200  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Main verb entry with full linguistic information
        main_verb_data = [
            # Primary verb form
            ("to begin/to start", "kwambĩrĩria", "Infinitive form - transitive verb meaning to begin or start something", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("begin/start (verb stem)", "ambĩrĩria", "Verb stem without infinitive prefix", "Infinitives", DifficultyLevel.ADVANCED),
            
            # Etymology information
            ("etymology: from 'to do first'", "kwamba 'to do first'", "Historical origin - derived from kwamba meaning 'to do first'", "Etymology", DifficultyLevel.ADVANCED),
            
            # Pronunciation guide
            ("pronunciation", "/aᵐbeɾeɾia/", "IPA phonetic transcription of verb stem", "Pronunciation Guide", DifficultyLevel.ADVANCED),
            
            # Derived noun forms
            ("beginning (class 7)", "kĩambĩrĩria", "Noun derived from verb - class 7 (kĩ- prefix)", "Derived Forms", DifficultyLevel.INTERMEDIATE),
            ("beginning (class 3)", "mwambĩrĩrio", "Noun derived from verb - class 3 (mũ- prefix)", "Derived Forms", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Additional related vocabulary for completeness
        related_vocabulary = [
            # Related concepts
            ("first", "wa mbere", "Ordinal number - first position", "Numbers", DifficultyLevel.BEGINNER),
            ("beginning", "kĩambĩrĩria", "The start or commencement of something", "Derived Forms", DifficultyLevel.INTERMEDIATE),
            ("starter/initiator", "mwambĩrĩria", "Person who begins something (class 1)", "Derived Forms", DifficultyLevel.ADVANCED),
            
            # Usage examples
            ("I will begin", "nĩngwambĩrĩria", "Future tense conjugation of 'to begin'", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("we are beginning", "nĩ twambĩrĩria", "Present progressive of 'to begin'", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("he/she began", "ambĩrĩririe", "Past tense 3rd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Combine all data
        all_linguistic_data = main_verb_data + related_vocabulary
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, context, category_name, difficulty in all_linguistic_data:
            # Check if this contribution already exists to avoid duplicates
            existing = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if existing:
                skipped_count += 1
                continue  # Skip duplicates silently
            
            # Get the category
            if category_name in categories:
                category = categories[category_name]
            else:
                # Default to Infinitives if category not found
                category = categories["Infinitives"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Linguistic data with etymology and derived forms from academic sources",
                quality_score=5.0,  # Highest quality - academic source
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for morphological analysis
        morphological_patterns = [
            # Infinitive construction
            ("to begin/to start", "kwambĩrĩria", [
                ("infinitive prefix", "kw-", 0, "Infinitive marker (kũ- → kw- before vowels)"),
                ("verb stem", "ambĩrĩria", 1, "Root meaning 'to begin/start'")
            ]),
            # Class 7 derivation
            ("beginning (class 7)", "kĩambĩrĩria", [
                ("class 7 prefix", "kĩ-", 0, "Noun class 7 prefix (diminutive/tool)"),
                ("verb stem", "ambĩrĩria", 1, "Derived from verb 'to begin'")
            ]),
            # Class 3 derivation  
            ("beginning (class 3)", "mwambĩrĩrio", [
                ("class 3 prefix", "mw-", 0, "Noun class 3 prefix (mũ- → mw- before vowels)"),
                ("verb stem", "ambĩrĩri-", 1, "Modified verb stem"),
                ("nominalizer", "-o", 2, "Suffix creating abstract noun")
            ]),
            # Future tense conjugation
            ("I will begin", "nĩngwambĩrĩria", [
                ("focus marker", "nĩ", 0, "Focus/emphasis particle"),
                ("I will", "ngw-", 1, "1st person future + infinitive prefix"),
                ("begin", "ambĩrĩria", 2, "Verb stem")
            ])
        ]
        
        for source, target, sub_parts in morphological_patterns:
            # Find the parent contribution
            parent = db.query(Contribution).filter(
                Contribution.source_text == source,
                Contribution.target_text == target
            ).first()
            
            if parent:
                for sub_source, sub_target, position, explanation in sub_parts:
                    sub_translation = SubTranslation(
                        parent_contribution_id=parent.id,
                        source_word=sub_source,
                        target_word=sub_target,
                        word_position=position,
                        context=explanation,
                        created_by_id=admin_user.id
                    )
                    db.add(sub_translation)
                
                # Mark parent as having sub-translations
                parent.has_sub_translations = True
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new linguistic contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(morphological_patterns)} forms")
        print("All data marked as approved for immediate use")
        
        # Print linguistic features analysis
        print("\nLinguistic Features Added:")
        print("- Etymology: Historical word development from 'kwamba'")
        print("- IPA Pronunciation: /aᵐbeɾeɾia/ with prenasalized sounds")
        print("- Derivational morphology: Class 3 and 7 noun formation")
        print("- Infinitive alternation: kũ- → kw- before vowels")
        print("- Academic source: T.G. Benson (1964) dictionary reference")
        
        # Print category summary
        print("\nCategory additions:")
        new_categories = ["Etymology", "Pronunciation Guide", "Derived Forms"]
        for cat_name in new_categories:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")


if __name__ == "__main__":
    print("Seeding database with detailed linguistic verb information...")
    try:
        create_linguistic_verb_seed()
        print("Linguistic verb seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)