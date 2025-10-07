#!/usr/bin/env python3
"""
Seed script for additional Kikuyu vocabulary
Includes place/time expressions, tribal terminology, and blessings
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


def create_additional_vocabulary_seed():
    """Create seed data for additional Kikuyu vocabulary"""
    
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
            ("Time Expressions", "Temporal and time-related phrases", "time-expressions"),
            ("Place Expressions", "Locational and spatial terminology", "place-expressions"),
            ("Tribal Terms", "Terms related to tribes and communities", "tribal-terms"),
            ("Blessings", "Blessing and ceremonial expressions", "blessings"),
            ("Physical Descriptions", "Physical characteristics and descriptions", "physical-descriptions"),
            ("Activities", "Activities and actions", "activities"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 400  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Additional vocabulary data
        vocabulary_data = [
            # Place and time expressions
            ("at the entrance", "mũromoinĩ", "Locational expression - at the entrance/doorway", "Place Expressions", DifficultyLevel.INTERMEDIATE),
            ("in the early morning hours", "mathaainĩ ma mĩaraho", "Temporal expression for early morning period", "Time Expressions", DifficultyLevel.ADVANCED),
            ("during herding of livestock", "rũũruinĩ", "Temporal/activity expression - while herding animals", "Activities", DifficultyLevel.INTERMEDIATE),
            
            # Physical descriptions
            ("slippery", "nyoroku", "Physical characteristic - having a slippery surface", "Physical Descriptions", DifficultyLevel.BEGINNER),
            
            # Tribal terminology
            ("a big tribe", "rũũrĩrĩ rũnene", "Large tribal group or community", "Tribal Terms", DifficultyLevel.INTERMEDIATE),
            ("tribes", "ndũũrĩrĩ", "Plural form - multiple tribes or communities", "Tribal Terms", DifficultyLevel.INTERMEDIATE),
            
            # Blessings and ceremonial language
            ("may you have good and holy actions", "cĩĩkage cĩĩko njagĩrĩru na cia ũthingu", "Traditional blessing for righteous and holy conduct", "Blessings", DifficultyLevel.ADVANCED),
            
            # Additional related terms for context
            ("entrance", "mũromo", "Entry point or doorway", "Place Expressions", DifficultyLevel.BEGINNER),
            ("early morning", "mathaainĩ", "Early hours of the morning", "Time Expressions", DifficultyLevel.BEGINNER),
            ("herding", "rũũru", "Activity of tending livestock", "Activities", DifficultyLevel.INTERMEDIATE),
            ("tribe", "rũũrĩrĩ", "Community or tribal group", "Tribal Terms", DifficultyLevel.BEGINNER),
            ("big/large", "rũnene", "Size descriptor - large or big", "Physical Descriptions", DifficultyLevel.BEGINNER),
            ("good actions", "cĩĩko njagĩrĩru", "Righteous or good deeds", "Blessings", DifficultyLevel.INTERMEDIATE),
            ("holy", "ũthingu", "Sacred or righteous quality", "Blessings", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, context, category_name, difficulty in vocabulary_data:
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
                # Try to find existing category
                existing_category = db.query(Category).filter(Category.name == category_name).first()
                if existing_category:
                    category = existing_category
                else:
                    # Default to Activities if category not found
                    category = categories["Activities"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Additional vocabulary from community contributions and linguistic analysis",
                quality_score=4.5,  # High quality content
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for complex expressions
        complex_patterns = [
            # Early morning expression breakdown
            ("in the early morning hours", "mathaainĩ ma mĩaraho", [
                ("in the morning", "mathaainĩ", 0, "Time reference - morning period"),
                ("of", "ma", 1, "Possessive/genitive marker"),
                ("early hours", "mĩaraho", 2, "Specific early time period")
            ]),
            # Big tribe expression breakdown
            ("a big tribe", "rũũrĩrĩ rũnene", [
                ("tribe", "rũũrĩrĩ", 0, "Community or clan group"),
                ("big", "rũnene", 1, "Size adjective - large")
            ]),
            # Blessing breakdown
            ("may you have good and holy actions", "cĩĩkage cĩĩko njagĩrĩru na cia ũthingu", [
                ("may you have", "cĩĩkage", 0, "Optative/subjunctive - expressing wish"),
                ("actions", "cĩĩko", 1, "Deeds or actions"),
                ("good", "njagĩrĩru", 2, "Positive quality - good/righteous"),
                ("and", "na", 3, "Conjunction - and/with"),
                ("of holiness", "cia ũthingu", 4, "Sacred or holy quality")
            ])
        ]
        
        for source, target, sub_parts in complex_patterns:
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
        
        print(f"Successfully created {contribution_count} new vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(complex_patterns)} complex expressions")
        print("All data marked as approved for immediate use")
        
        # Print content analysis
        print("\nVocabulary Content Added:")
        print("- Place and time expressions (entrance, early morning, herding time)")
        print("- Tribal terminology (tribes, communities)")
        print("- Physical descriptions (slippery, big)")
        print("- Traditional blessings and ceremonial language")
        print("- Morphological breakdowns for complex expressions")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Time Expressions", "Place Expressions", "Tribal Terms", "Blessings", "Physical Descriptions"]
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
        
        # Note about unclear term
        print("\nNote: 'Marũngĩĩ' was noted as unclear - excluded pending spelling verification")


if __name__ == "__main__":
    print("Seeding database with additional Kikuyu vocabulary...")
    try:
        create_additional_vocabulary_seed()
        print("Additional vocabulary seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)