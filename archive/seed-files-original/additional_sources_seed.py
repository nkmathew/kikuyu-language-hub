#!/usr/bin/env python3
"""
Seed script for additional Kikuyu translations from mustgo.com and hubpages.com
Includes greetings, family terms, and noun class vocabulary
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


def create_additional_sources_seed():
    """Create seed data from mustgo.com and hubpages.com sources"""
    
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
            ("Greetings & Courtesy", "Polite expressions and greetings for different times", "greetings-courtesy"),
            ("Family & Relationships", "Family members and relationship terms", "family-relationships"),
            ("Noun Classification", "Kikuyu noun classes with examples", "noun-classification"),
            ("Grammar Examples", "Demonstrative pronouns and grammatical structures", "grammar-examples"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 900  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Translations from mustgo.com - greetings and basic phrases
        mustgo_translations = [
            # Time-specific greetings with variants
            ("good morning", "we mwega rũciinĩ", "Morning greeting - early hours", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("good morning", "we mwega kĩroko", "Morning greeting - alternative form", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("good afternoon", "we mwega umũthĩ", "Afternoon greeting", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("good evening", "we mwega hwaĩinĩ", "Evening greeting", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            
            # Basic responses and courtesy
            ("OK", "nĩwega", "Agreement or acknowledgment", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("alright", "nĩwega", "Acceptance or agreement", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("thank you", "nĩ ngatho", "Expression of gratitude", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("how are you?", "wĩ mwega?", "Standard health inquiry", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            ("no", "aacha", "Negative response", "Greetings & Courtesy", DifficultyLevel.BEGINNER),
            
            # Family terms
            ("father", "baba", "Male parent", "Family & Relationships", DifficultyLevel.BEGINNER),
            ("mother", "maitũ", "Female parent - formal/respectful", "Family & Relationships", DifficultyLevel.BEGINNER),
            ("mother", "mami", "Female parent - informal", "Family & Relationships", DifficultyLevel.BEGINNER),
        ]
        
        # Translations from hubpages.com - noun classes and grammar
        hubpages_translations = [
            # Class I nouns - humans
            ("person", "mũndũ", "Individual human being - Class I noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("woman", "mũtumia", "Adult female - Class I noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("man", "mũthuuri", "Adult male - Class I noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("girl", "mũirĩtu", "Young female - Class I noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("young man", "mwanake", "Young male - Class I noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            
            # Class II nouns - trees, diseases, borrowed words
            ("tree", "mũtĩ", "Woody plant - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("orange tree", "mũcungwa", "Citrus tree - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("mango tree", "mũembe", "Mango tree - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("door", "mũrango", "Entrance/exit - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("leg", "mũgogo", "Lower limb - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("ball", "mũbira", "Spherical object - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("bread", "mũgate", "Baked food - Class II borrowed word", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("lion", "mũrũthi", "Large cat - Class II noun", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            ("car", "mũtoka", "Motor vehicle - Class II borrowed word", "Noun Classification", DifficultyLevel.INTERMEDIATE),
            
            # Demonstrative pronouns and grammar examples
            ("this (Class I)", "ũyũ", "Demonstrative pronoun for Class I singular", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("these (Class I)", "aya", "Demonstrative pronoun for Class I plural", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("this (Class II)", "ũyũ", "Demonstrative pronoun for Class II singular", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("these (Class II)", "ĩno", "Demonstrative pronoun for Class II plural", "Grammar Examples", DifficultyLevel.ADVANCED),
            
            # Complete example phrases
            ("this one person", "mũndũ ũyũ ũmwe", "Demonstrative + numeral example", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("these two people", "andũ aya erĩ", "Plural demonstrative + numeral", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("this one tree", "mũtĩ ũyũ ũmwe", "Class II demonstrative example", "Grammar Examples", DifficultyLevel.ADVANCED),
            ("these two trees", "mĩtĩ ĩno ĩrĩ", "Class II plural demonstrative", "Grammar Examples", DifficultyLevel.ADVANCED),
            
            # Learning phrases
            ("learn Kikuyu", "wĩrute gĩkũyũ", "Imperative - learn the language", "Grammar Examples", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process all translations
        all_translations = mustgo_translations + hubpages_translations
        
        for english, kikuyu, context, category_name, difficulty in all_translations:
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
                    # Default to Basic Vocabulary if category not found
                    basic_vocab = db.query(Category).filter(Category.name == "Basic Vocabulary").first()
                    category = basic_vocab if basic_vocab else categories["Greetings & Courtesy"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Educational content from mustgo.com and hubpages.com covering essential greetings, family terms, and Kikuyu noun classification system with grammatical examples.",
                quality_score=4.4,  # High quality educational content
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for noun class examples
        noun_class_patterns = [
            # Class I examples with morphological breakdown
            ("person", "mũndũ", [
                ("mũ-", "mũ-", 0, "Class I singular prefix"),
                ("ndũ", "ndũ", 1, "Root: person/human being")
            ]),
            ("these two people", "andũ aya erĩ", [
                ("a-", "a-", 0, "Class I plural prefix"),
                ("ndũ", "ndũ", 1, "Root: person/human being"),
                ("aya", "aya", 2, "Class I plural demonstrative"),
                ("erĩ", "erĩ", 3, "Numeral: two")
            ]),
            # Class II examples with morphological breakdown
            ("tree", "mũtĩ", [
                ("mũ-", "mũ-", 0, "Class II singular prefix"),
                ("tĩ", "tĩ", 1, "Root: tree/wood")
            ]),
            ("these two trees", "mĩtĩ ĩno ĩrĩ", [
                ("mĩ-", "mĩ-", 0, "Class II plural prefix"),
                ("tĩ", "tĩ", 1, "Root: tree/wood"),
                ("ĩno", "ĩno", 2, "Class II plural demonstrative"),
                ("ĩrĩ", "ĩrĩ", 3, "Numeral: two (Class II agreement)")
            ])
        ]
        
        for source, target, sub_parts in noun_class_patterns:
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
        
        print(f"Successfully created {contribution_count} new contributions from additional sources")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(noun_class_patterns)} noun class examples")
        print("All vocabulary marked as approved for immediate use")
        
        # Print content analysis
        print("\nAdditional Sources Content Added:")
        print("- Time-specific greetings (morning, afternoon, evening)")
        print("- Basic courtesy expressions and responses")
        print("- Family relationship terms")
        print("- Class I nouns (humans) with examples")
        print("- Class II nouns (trees, objects, borrowed words)")
        print("- Demonstrative pronouns for both noun classes")
        print("- Complete grammatical example phrases")
        print("- Noun class morphological patterns")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Greetings & Courtesy", "Family & Relationships", "Noun Classification", "Grammar Examples"]
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
        
        print("\nNote: This collection combines practical greetings from mustgo.com")
        print("with detailed noun classification examples from hubpages.com,")
        print("providing both everyday usage and grammatical structure.")


if __name__ == "__main__":
    print("Seeding database with additional Kikuyu translations...")
    print("Sources: mustgo.com and hubpages.com educational content")
    try:
        create_additional_sources_seed()
        print("Additional sources seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)