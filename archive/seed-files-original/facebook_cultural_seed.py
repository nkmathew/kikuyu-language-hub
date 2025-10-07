#!/usr/bin/env python3
"""
Seed script for Kikuyu vocabulary from Facebook cultural lesson
Adds vocabulary about thieves, places, and everyday situations
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


def create_facebook_cultural_seed():
    """Create seed data from Facebook cultural lesson about thieves and related terms"""
    
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
            ("Proverbs", "Traditional Kikuyu proverbs and sayings", "proverbs"),
            ("Cultural Terms", "Cultural and social terminology", "cultural-terms"),
            ("Crime & Security", "Terms related to crime and security", "crime-security"),
            ("Locations", "Place names and geographical terms", "locations"),
            ("Past Tenses", "Past tense verb forms and examples", "past-tenses"),
            ("Questions", "Question words and interrogative forms", "questions"),
            ("Daily Activities", "Common daily activities and routines", "daily-activities"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 300  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Proverb and cultural wisdom
        proverb_data = [
            ("The thief makes even the poisoner change residence", "Mũici athamagia mũrogi", "Traditional proverb about the despised nature of thieves", "Proverbs", DifficultyLevel.ADVANCED),
            ("Proverb for today", "Thimo ya ũmũthĩ", "Daily proverb - cultural learning phrase", "Proverbs", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Crime and security vocabulary
        crime_vocabulary = [
            ("thief", "mũici", "Person who steals", "Crime & Security", DifficultyLevel.BEGINNER),
            ("thieves", "aici", "Plural form of thief", "Crime & Security", DifficultyLevel.BEGINNER),
            ("to steal", "kũiya", "Verb meaning to take unlawfully", "Crime & Security", DifficultyLevel.BEGINNER),
            ("one who takes by force", "mũtunyani", "Person who takes by guile, force or from those with less power", "Crime & Security", DifficultyLevel.INTERMEDIATE),
            ("to take away by force", "gũtunya", "Verb - to take by guile, force or from those with less power", "Crime & Security", DifficultyLevel.INTERMEDIATE),
            ("a thief takes by force", "Mũici nĩ mũtunyani", "Explanation of thief's behavior", "Crime & Security", DifficultyLevel.INTERMEDIATE),
            ("poisoner/evil person", "mũrogi", "Traditional term for someone who uses harmful magic", "Cultural Terms", DifficultyLevel.ADVANCED),
            ("traditional doctor", "mũgo", "Respected traditional healer (contrast to mũrogi)", "Cultural Terms", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Movement and relocation terms
        movement_vocabulary = [
            ("he/she makes one move", "athamagia", "3rd person - causes relocation", "Daily Activities", DifficultyLevel.ADVANCED),
            ("to change residence", "gũthama", "Verb - to move house/relocate", "Daily Activities", DifficultyLevel.INTERMEDIATE),
            ("one who changes residence", "mũthami", "Person who relocates", "Daily Activities", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Threat and warning expressions
        warnings_data = [
            ("take from me and see", "ndunya wone", "Warning threat - 'try it and see what happens'", "Cultural Terms", DifficultyLevel.ADVANCED),
            ("do not take from me", "ndũkandunye", "Negative command - prohibition", "Cultural Terms", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Location and spatial terms
        location_vocabulary = [
            ("here (large area)", "gũkũ", "Spatial term for big geographical area (town, country)", "Locations", DifficultyLevel.BEGINNER),
            ("where?", "kũ?", "Question word for place/location", "Questions", DifficultyLevel.BEGINNER),
            ("there", "kũũrĩa", "Spatial term - over there (distant)", "Locations", DifficultyLevel.BEGINNER),
            ("here (small area)", "haha", "Spatial term for small area (where I'm standing)", "Locations", DifficultyLevel.BEGINNER),
            ("over there", "harĩa", "Spatial term - over there (visible)", "Locations", DifficultyLevel.BEGINNER),
            ("where? (specific)", "ha?", "Question word for specific location", "Questions", DifficultyLevel.BEGINNER),
        ]
        
        # Past tense questions about phone theft
        past_tense_questions = [
            ("Where was your phone taken from you? (today)", "Ũtunyĩirwo thimũ kũ?", "Recent past tense question", "Past Tenses", DifficultyLevel.INTERMEDIATE),
            ("Where was your phone taken from you? (yesterday)", "Ũratunyĩirwo thimũ kũ?", "Near past tense question", "Past Tenses", DifficultyLevel.INTERMEDIATE),
            ("Where was your phone taken from you? (distant past)", "Watunyĩirwo thimũ kũ?", "Remote past tense question", "Past Tenses", DifficultyLevel.ADVANCED),
        ]
        
        # Question words and expressions
        question_vocabulary = [
            ("what?", "atĩa?", "Basic question word", "Questions", DifficultyLevel.BEGINNER),
            ("like this/who?", "ũũ", "Context-dependent: 'like this' or 'who?' (tone-dependent)", "Questions", DifficultyLevel.INTERMEDIATE),
            ("what did you say?", "atĩ atĩa", "Expression of wonder or shock", "Questions", DifficultyLevel.INTERMEDIATE),
            ("how is it?", "nĩ atĩa", "Greeting form with response 'ni kwega'", "Questions", DifficultyLevel.INTERMEDIATE),
            ("what were you doing when dispossessed?", "Ũgwĩkaga atĩa ũgĩtunywo", "Complex past continuous question", "Past Tenses", DifficultyLevel.ADVANCED),
        ]
        
        # Daily activities and destinations
        activities_vocabulary = [
            ("I was going to work", "Ngũthiaga wĩra", "Past continuous - going to work", "Daily Activities", DifficultyLevel.INTERMEDIATE),
            ("you were going home", "Ũgũthiaga mũciĩ", "Past continuous - going home", "Daily Activities", DifficultyLevel.INTERMEDIATE),
            ("he/she was going to school", "Egũthiaga thukuru", "Past continuous - going to school", "Daily Activities", DifficultyLevel.INTERMEDIATE),
            ("they were going to church", "Megũthiaga kanitha", "Past continuous - going to church", "Daily Activities", DifficultyLevel.INTERMEDIATE),
            ("we were going to the mosque", "Tũgũthiaga mũthigiti", "Past continuous - going to mosque", "Daily Activities", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Standard responses
        responses_data = [
            ("it is good", "ni kwega", "Standard response to 'nĩ atĩa' greeting", "Responses", DifficultyLevel.BEGINNER),
        ]
        
        # Combine all data sets
        all_cultural_data = [
            (proverb_data, "proverbs"),
            (crime_vocabulary, "crime vocabulary"),
            (movement_vocabulary, "movement terms"),
            (warnings_data, "warnings"),
            (location_vocabulary, "locations"),
            (past_tense_questions, "past tense questions"),
            (question_vocabulary, "questions"),
            (activities_vocabulary, "daily activities"),
            (responses_data, "responses"),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for data_set, data_type in all_cultural_data:
            for english, kikuyu, context, category_name, difficulty in data_set:
                # Check if this contribution already exists to avoid duplicates
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    skipped_count += 1
                    continue  # Skip duplicates silently
                
                # Get the category - check both new and existing categories
                if category_name in categories:
                    category = categories[category_name]
                else:
                    # Try to find existing category
                    existing_category = db.query(Category).filter(Category.name == category_name).first()
                    if existing_category:
                        category = existing_category
                    else:
                        # Default to Questions if category not found
                        category = categories["Questions"]
                
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,  # Pre-approved seed data
                    language="kikuyu",
                    difficulty_level=difficulty,
                    context_notes=context,
                    cultural_notes="Extracted from Facebook cultural lesson by Kikuyu language teacher",
                    quality_score=4.6,  # High quality cultural content
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()  # Get the ID
                
                # Associate with category
                contribution.categories.append(category)
                
                contribution_count += 1
        
        # Create sub-translations for complex cultural patterns
        cultural_patterns = [
            # Proverb breakdown
            ("The thief makes even the poisoner change residence", "Mũici athamagia mũrogi", [
                ("thief", "Mũici", 0, "Subject - the one who steals"),
                ("makes move", "athamagia", 1, "Causative verb - forces relocation"),
                ("poisoner", "mũrogi", 2, "Traditional evil person/sorcerer")
            ]),
            # Past tense construction
            ("Where was your phone taken from you? (today)", "Ũtunyĩirwo thimũ kũ?", [
                ("you were taken from", "Ũtunyĩirwo", 0, "2nd person passive past tense"),
                ("phone", "thimũ", 1, "Object - mobile phone"),
                ("where?", "kũ?", 2, "Location question word")
            ]),
            # Complex question pattern
            ("what were you doing when dispossessed?", "Ũgwĩkaga atĩa ũgĩtunywo", [
                ("you were doing", "Ũgwĩkaga", 0, "Past continuous 2nd person"),
                ("what", "atĩa", 1, "Question word"),
                ("when you were dispossessed", "ũgĩtunywo", 2, "Temporal clause - when robbed")
            ]),
            # Activity pattern
            ("I was going to work", "Ngũthiaga wĩra", [
                ("I was going", "Ngũthiaga", 0, "1st person past continuous"),
                ("work", "wĩra", 1, "Destination/purpose")
            ])
        ]
        
        for source, target, sub_parts in cultural_patterns:
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
        
        print(f"Successfully created {contribution_count} new cultural contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added cultural analysis for {len(cultural_patterns)} complex patterns")
        print("All data marked as approved for immediate use")
        
        # Print cultural content analysis
        print("\nCultural Content Added:")
        print("- Traditional proverbs with moral lessons")
        print("- Crime and security vocabulary")
        print("- Spatial/location terminology")
        print("- Past tense constructions (recent, near, remote)")
        print("- Daily activities and destinations")
        print("- Question words and interrogative patterns")
        print("- Cultural context about traditional healers vs sorcerers")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Proverbs", "Cultural Terms", "Crime & Security", "Past Tenses"]
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
    print("Seeding database with Kikuyu cultural lesson from Facebook...")
    try:
        create_facebook_cultural_seed()
        print("Cultural lesson seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)