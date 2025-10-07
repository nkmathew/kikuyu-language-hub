#!/usr/bin/env python3
"""
Seed script for Kikuyu vocabulary from learn-kikuyu.netlify.app
Structured learning content organized by categories with audio references
Includes family, animals, household items, numbers, and foods
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


def create_learn_kikuyu_app_seed():
    """Create seed data from learn-kikuyu.netlify.app structured learning content"""
    
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
            ("Family Members", "Extended family relationships and kinship terms", "family-members"),
            ("Wild Animals", "Wildlife and animals found in nature", "wild-animals"),
            ("Domestic Animals", "Farm animals and household pets", "domestic-animals"),
            ("Foods & Fruits", "Food items, fruits, and culinary terms", "foods-fruits"),
            ("Kitchen Utensils", "Cooking and eating implements", "kitchen-utensils"),
            ("Household Items", "Common household objects and furniture", "household-items"),
            ("Numbers & Counting", "Cardinal numbers and counting system", "numbers-counting"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1100  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Vocabulary from learn-kikuyu.netlify.app (preserving all accented characters)
        learn_kikuyu_vocabulary = [
            # Family members
            ("father", "baba", "Male parent", "Family Members", DifficultyLevel.BEGINNER),
            ("mother", "mami", "Female parent - informal", "Family Members", DifficultyLevel.BEGINNER),
            ("mother", "maitũ", "Female parent - formal/respectful", "Family Members", DifficultyLevel.BEGINNER),
            ("son", "mũriũ", "Male offspring", "Family Members", DifficultyLevel.BEGINNER),
            ("daughter", "mwarĩ", "Female offspring", "Family Members", DifficultyLevel.BEGINNER),
            ("sister", "mwarĩ wa nyina", "Female sibling (literally: daughter of mother)", "Family Members", DifficultyLevel.INTERMEDIATE),
            ("brother", "mũrũ wa nyina", "Male sibling (literally: son of mother)", "Family Members", DifficultyLevel.INTERMEDIATE),
            ("grandfather", "guka", "Male grandparent", "Family Members", DifficultyLevel.BEGINNER),
            ("grandmother", "cũcũ", "Female grandparent", "Family Members", DifficultyLevel.BEGINNER),
            ("uncle", "mama", "Father's brother", "Family Members", DifficultyLevel.BEGINNER),
            ("aunt", "tata", "Father's sister", "Family Members", DifficultyLevel.BEGINNER),
            ("cousin (male)", "mũrũ wa tata", "Male cousin (literally: son of aunt)", "Family Members", DifficultyLevel.INTERMEDIATE),
            ("cousin (female)", "mwarĩ wa tata", "Female cousin (literally: daughter of aunt)", "Family Members", DifficultyLevel.INTERMEDIATE),
            ("nephew", "mũihwa", "Brother's or sister's son", "Family Members", DifficultyLevel.INTERMEDIATE),
            ("niece", "mũihwa", "Brother's or sister's daughter", "Family Members", DifficultyLevel.INTERMEDIATE),
            
            # Wild animals
            ("lion", "mũrũthi", "Large predatory cat", "Wild Animals", DifficultyLevel.BEGINNER),
            ("hyena", "hiti", "Scavenging carnivore", "Wild Animals", DifficultyLevel.INTERMEDIATE),
            ("elephant", "njogu", "Large pachyderm", "Wild Animals", DifficultyLevel.BEGINNER),
            ("rhino", "munyi", "Horned mammal", "Wild Animals", DifficultyLevel.INTERMEDIATE),
            ("hippo", "nguũ", "Large aquatic mammal", "Wild Animals", DifficultyLevel.INTERMEDIATE),
            ("giraffe", "mũitĩrĩro", "Tall-necked mammal", "Wild Animals", DifficultyLevel.INTERMEDIATE),
            
            # Domestic animals
            ("cat", "nyau", "Domestic feline", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("dog", "ngui", "Domestic canine", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("chicken", "ngũkũ", "Domestic fowl", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("goat", "mbũri", "Small ruminant", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("cow", "ng'ombe", "Large domestic bovine", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("sheep", "ngo'ndu", "Woolly ruminant", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("rabbit", "mbũkũ", "Small mammal", "Domestic Animals", DifficultyLevel.BEGINNER),
            ("camel", "ngamĩra", "Desert transport animal", "Domestic Animals", DifficultyLevel.INTERMEDIATE),
            ("dove", "ndutura", "Small peaceful bird", "Domestic Animals", DifficultyLevel.INTERMEDIATE),
            ("duck", "bata", "Aquatic bird", "Domestic Animals", DifficultyLevel.BEGINNER),
            
            # Household items
            ("house", "nyũmba", "Dwelling structure", "Household Items", DifficultyLevel.BEGINNER),
            ("clock", "thaa", "Timekeeping device", "Household Items", DifficultyLevel.BEGINNER),
            ("bed", "ũrĩrĩ", "Sleeping furniture", "Household Items", DifficultyLevel.BEGINNER),
            ("bed", "gĩtanda", "Sleeping furniture - alternative", "Household Items", DifficultyLevel.BEGINNER),
            ("chair", "gĩtĩ", "Seating furniture", "Household Items", DifficultyLevel.BEGINNER),
            ("table", "metha", "Flat-surfaced furniture", "Household Items", DifficultyLevel.BEGINNER),
            ("tank", "itangi", "Water storage container", "Household Items", DifficultyLevel.INTERMEDIATE),
            ("bottle", "cuba", "Liquid container", "Household Items", DifficultyLevel.BEGINNER),
            ("glass", "girathi", "Drinking vessel", "Household Items", DifficultyLevel.BEGINNER),
            
            # Kitchen utensils
            ("plate", "thani", "Eating dish", "Kitchen Utensils", DifficultyLevel.BEGINNER),
            ("fork", "hũma", "Eating utensil with prongs", "Kitchen Utensils", DifficultyLevel.BEGINNER),
            ("pestle", "mũindũri", "Grinding tool", "Kitchen Utensils", DifficultyLevel.INTERMEDIATE),
            ("mortar", "ndĩrĩ", "Grinding bowl", "Kitchen Utensils", DifficultyLevel.INTERMEDIATE),
            ("bowl", "mbakũri", "Round container", "Kitchen Utensils", DifficultyLevel.BEGINNER),
            
            # Numbers (cardinal with proper accents)
            ("zero", "kĩbũgũ", "Number 0", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("one", "ĩmwe", "Number 1", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("two", "igĩrĩ", "Number 2", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("three", "ithatũ", "Number 3", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("four", "inya", "Number 4", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("five", "ithano", "Number 5", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("six", "ithathatũ", "Number 6", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("seven", "mũgwanja", "Number 7", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("eight", "inyanya", "Number 8", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("nine", "kenda", "Number 9", "Numbers & Counting", DifficultyLevel.BEGINNER),
            ("ten", "ikũmi", "Number 10", "Numbers & Counting", DifficultyLevel.BEGINNER),
            
            # Foods and fruits
            ("milk", "iria", "Dairy product", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("meat", "nyama", "Animal protein", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("beans", "mboco", "Legume food", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("maize", "mbembe", "Corn grain", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("greengrams", "ndengũ", "Small green legumes", "Foods & Fruits", DifficultyLevel.INTERMEDIATE),
            ("ugali", "ngima", "Maize flour staple", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("pineapple", "inanathi", "Tropical fruit", "Foods & Fruits", DifficultyLevel.INTERMEDIATE),
            ("banana", "irigũ", "Yellow curved fruit", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("orange", "icungwa", "Citrus fruit", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("mango", "iembe", "Sweet tropical fruit", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("lemon", "ndimũ", "Sour citrus fruit", "Foods & Fruits", DifficultyLevel.BEGINNER),
            ("avocado", "ikondo", "Green fatty fruit", "Foods & Fruits", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, context, category_name, difficulty in learn_kikuyu_vocabulary:
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
                    # Default to Family Members if category not found
                    category = categories["Family Members"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Interactive learning vocabulary from learn-kikuyu.netlify.app with audio support. Organized by thematic categories for systematic language acquisition with visual and auditory learning aids.",
                quality_score=4.6,  # High quality structured learning content
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for compound family terms showing relationship structure
        family_relationship_patterns = [
            # Sister - compound relationship
            ("sister", "mwarĩ wa nyina", [
                ("mwarĩ", "mwarĩ", 0, "Daughter/female offspring"),
                ("wa", "wa", 1, "Of/belonging to (possessive)"),
                ("nyina", "nyina", 2, "Mother (same mother)")
            ]),
            # Brother - compound relationship
            ("brother", "mũrũ wa nyina", [
                ("mũrũ", "mũrũ", 0, "Son/male offspring"),
                ("wa", "wa", 1, "Of/belonging to (possessive)"),
                ("nyina", "nyina", 2, "Mother (same mother)")
            ]),
            # Male cousin - compound relationship
            ("cousin (male)", "mũrũ wa tata", [
                ("mũrũ", "mũrũ", 0, "Son/male offspring"),
                ("wa", "wa", 1, "Of/belonging to (possessive)"),
                ("tata", "tata", 2, "Aunt (father's sister)")
            ]),
            # Female cousin - compound relationship
            ("cousin (female)", "mwarĩ wa tata", [
                ("mwarĩ", "mwarĩ", 0, "Daughter/female offspring"),
                ("wa", "wa", 1, "Of/belonging to (possessive)"),
                ("tata", "tata", 2, "Aunt (father's sister)")
            ]),
            # Number analysis - six
            ("six", "ithathatũ", [
                ("i-", "i-", 0, "Number class prefix"),
                ("thatha", "thatha", 1, "Double of three"),
                ("-tũ", "-tũ", 2, "Number suffix")
            ])
        ]
        
        for source, target, sub_parts in family_relationship_patterns:
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
        
        print(f"Successfully created {contribution_count} new learn-kikuyu.netlify.app vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(family_relationship_patterns)} relationship structures")
        print("All vocabulary marked as approved for immediate use")
        
        # Print content analysis
        print("\nLearn-Kikuyu.netlify.app Content Added:")
        print("- Complete family relationship system")
        print("- Wild and domestic animal vocabulary")
        print("- Household items and furniture")
        print("- Kitchen utensils and cooking implements")
        print("- Complete number system (0-10)")
        print("- Food items and fruits")
        print("- Structured learning content with audio references")
        print("- Compound relationship terms with morphological analysis")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Family Members", "Wild Animals", "Domestic Animals", 
                         "Foods & Fruits", "Kitchen Utensils", "Household Items", "Numbers & Counting"]
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
        
        print("\nNote: This collection provides structured thematic vocabulary")
        print("with audio support for interactive learning. Each category")
        print("builds systematically from basic to intermediate concepts.")


if __name__ == "__main__":
    print("Seeding database with learn-kikuyu.netlify.app vocabulary...")
    print("Source: learn-kikuyu.netlify.app - structured interactive learning content")
    try:
        create_learn_kikuyu_app_seed()
        print("Learn-Kikuyu app vocabulary seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)