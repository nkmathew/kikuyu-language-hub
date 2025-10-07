#!/usr/bin/env python3
"""
Seed script for Kikuyu cooking methods vocabulary
Methods of cooking - Mĩrugĩre
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


def create_cooking_methods_seed():
    """Create seed data for Kikuyu cooking methods vocabulary"""
    
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
            ("Cooking Methods", "Traditional and modern cooking techniques", "cooking-methods"),
            ("Kitchen Activities", "Food preparation and cooking activities", "kitchen-activities"),
            ("Culinary Terms", "Food and cooking related terminology", "culinary-terms"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 500  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Cooking methods vocabulary data
        cooking_vocabulary = [
            # Main category header
            ("methods of cooking", "mĩrugĩre", "General term for cooking techniques and methods", "Culinary Terms", DifficultyLevel.INTERMEDIATE),
            
            # Primary cooking methods
            ("boiling", "gũtherũkia", "Cooking method using boiling water", "Cooking Methods", DifficultyLevel.BEGINNER),
            ("frying", "gũkaranga", "Cooking method using oil or fat in a pan", "Cooking Methods", DifficultyLevel.BEGINNER),
            ("roasting", "kũhĩhia", "Cooking method using dry heat (primary term)", "Cooking Methods", DifficultyLevel.BEGINNER),
            ("roasting", "gũcina", "Cooking method using dry heat (alternative term)", "Cooking Methods", DifficultyLevel.BEGINNER),
            
            # Related cooking terms for context
            ("to cook", "gũruga", "General verb for cooking/preparing food", "Kitchen Activities", DifficultyLevel.BEGINNER),
            ("cooking", "ũrugĩri", "The act or process of cooking", "Kitchen Activities", DifficultyLevel.BEGINNER),
            ("food preparation", "gũthondeka irio", "Preparing food for cooking", "Kitchen Activities", DifficultyLevel.INTERMEDIATE),
            ("kitchen", "mucii wa gũrugĩra", "Place where cooking happens", "Culinary Terms", DifficultyLevel.INTERMEDIATE),
            
            # Cooking implements and related terms
            ("pot", "nyũngũ", "Cooking vessel for boiling", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("pan", "karango", "Flat cooking vessel for frying", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("fire", "mwaki", "Heat source for cooking", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("water", "mai", "Essential cooking ingredient", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("oil", "maguta", "Fat used for frying", "Culinary Terms", DifficultyLevel.BEGINNER),
            
            # Food states and preparation
            ("cooked", "rũgĩte", "Food that has been prepared", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("raw", "mbichi", "Uncooked food", "Culinary Terms", DifficultyLevel.BEGINNER),
            ("hot", "ya ũrugarĩ", "Temperature of cooked food", "Culinary Terms", DifficultyLevel.BEGINNER),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, context, category_name, difficulty in cooking_vocabulary:
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
                    # Default to Culinary Terms if category not found
                    category = categories["Culinary Terms"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Traditional Kikuyu cooking methods and culinary terminology",
                quality_score=4.7,  # High quality culinary content
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for compound cooking terms
        cooking_patterns = [
            # Compound cooking terms
            ("food preparation", "gũthondeka irio", [
                ("to prepare", "gũthondeka", 0, "Verb - to prepare or make ready"),
                ("food", "irio", 1, "Noun - food or meal")
            ]),
            ("kitchen", "mucii wa gũrugĩra", [
                ("house/place", "mucii", 0, "Location - house or designated place"),
                ("of", "wa", 1, "Possessive marker"),
                ("cooking", "gũrugĩra", 2, "Gerund - the act of cooking")
            ]),
            ("hot (food)", "ya ũrugarĩ", [
                ("of", "ya", 0, "Possessive/descriptive marker"),
                ("heat/warmth", "ũrugarĩ", 1, "Noun - heat or warmth")
            ])
        ]
        
        for source, target, sub_parts in cooking_patterns:
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
        
        print(f"Successfully created {contribution_count} new cooking vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(cooking_patterns)} compound terms")
        print("All data marked as approved for immediate use")
        
        # Print content analysis
        print("\nCooking Methods Vocabulary Added:")
        print("- Primary cooking methods: boiling (gũtherũkia), frying (gũkaranga), roasting (kũhĩhia/gũcina)")
        print("- Kitchen activities and food preparation terms")
        print("- Cooking implements (pots, pans, utensils)")
        print("- Food states (cooked, raw, hot)")
        print("- Cultural context for traditional Kikuyu cooking")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Cooking Methods", "Kitchen Activities", "Culinary Terms"]
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
        
        # Print cooking method details
        print("\nCooking Methods Detail:")
        print("• Gũtherũkia (boiling) - Using water/liquid at high temperature")
        print("• Gũkaranga (frying) - Using oil/fat in a pan")
        print("• Kũhĩhia/Gũcina (roasting) - Using dry heat (two terms available)")


if __name__ == "__main__":
    print("Seeding database with Kikuyu cooking methods vocabulary...")
    print("MIRUGIRE - METHODS OF COOKING")
    try:
        create_cooking_methods_seed()
        print("Cooking methods vocabulary seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)