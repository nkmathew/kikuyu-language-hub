#!/usr/bin/env python3
"""
Seed script for Quizlet Kikuyu greetings flash cards data
Populates the database with additional vocabulary from Quizlet study materials
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


def create_quizlet_seed_data():
    """Create seed data from Quizlet Kikuyu greetings flash cards"""
    
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
            ("Greetings", "Basic greetings and social interactions", "greetings"),
            ("Questions", "Common questions and inquiries", "questions"),
            ("Responses", "Standard responses and reactions", "responses"),
            ("Courtesy", "Polite expressions and courtesy phrases", "courtesy"),
            ("Directions", "Navigation and direction-related phrases", "directions"),
            ("Commerce", "Trading and commercial expressions", "commerce"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Quizlet flash cards data - extracted from the file
        quizlet_data = [
            # Basic greetings
            ("Hi", "Niatia?", "Casual greeting - informal way to say hello", "Greetings", DifficultyLevel.BEGINNER),
            ("Good morning", "We mwega rũciinĩ", "Morning greeting with time specification", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Good morning (alternative)", "We mwega kĩroko", "Alternative morning greeting", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Good afternoon", "We mwega umũthĩ", "Afternoon greeting", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Good evening", "We mwega hwaĩinĩ", "Evening greeting", "Greetings", DifficultyLevel.INTERMEDIATE),
            
            # Questions about wellbeing
            ("How are you?", "Ũhoro waku?", "Standard inquiry about someone's wellbeing", "Questions", DifficultyLevel.BEGINNER),
            ("How are you doing?", "Ûrĩ mwega?", "Alternative way to ask about wellbeing", "Questions", DifficultyLevel.BEGINNER),
            ("How are you? (informal)", "Wĩ mwega?", "Informal version of wellbeing inquiry", "Questions", DifficultyLevel.BEGINNER),
            ("How are you (many people)?", "muriega?", "Plural form for asking multiple people", "Questions", DifficultyLevel.INTERMEDIATE),
            ("How are you all?", "uhoro wanyu?", "Formal plural greeting inquiry", "Questions", DifficultyLevel.INTERMEDIATE),
            
            # Responses to greetings
            ("I am good", "Ndĩ mwega", "Standard positive response to greeting", "Responses", DifficultyLevel.BEGINNER),
            ("I am fine", "ni kwega", "Alternative positive response", "Responses", DifficultyLevel.BEGINNER),
            ("I am fine (colloquial)", "ti kuru", "Colloquial way to say 'it's not bad'", "Responses", DifficultyLevel.INTERMEDIATE),
            
            # Farewells
            ("Bye, be blessed", "Tigwo na wega", "Farewell with blessing", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Safe journey", "Thii na wega", "Wishing safe travel", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Go in peace", "Thii na thayu", "Peaceful farewell", "Greetings", DifficultyLevel.INTERMEDIATE),
            
            # Social expressions
            ("greetings", "ngeithi", "General term for greetings", "Greetings", DifficultyLevel.INTERMEDIATE),
            ("Thank you", "Nĩ ngatho", "Expression of gratitude", "Courtesy", DifficultyLevel.BEGINNER),
            ("Are you a friend?", "Wĩ mũrata?", "Inquiry about friendship", "Questions", DifficultyLevel.INTERMEDIATE),
            
            # Basic interactions
            ("Come here", "Ũka haha", "Command to approach", "Directions", DifficultyLevel.BEGINNER),
            ("a lot", "muno", "Quantifier meaning 'much/many'", "Responses", DifficultyLevel.BEGINNER),
            ("Help me", "Ndeithia", "Request for assistance", "Questions", DifficultyLevel.BEGINNER),
            ("Help me (alternative)", "Ndiethia", "Alternative form of help request", "Questions", DifficultyLevel.BEGINNER),
            ("I love you", "Nĩngwendete", "Expression of love/affection", "Responses", DifficultyLevel.INTERMEDIATE),
            ("I will phone you", "Nĩngũkũhũrĩra thimû", "Future communication promise", "Responses", DifficultyLevel.ADVANCED),
            
            # Agreement and acknowledgment
            ("OK, alright", "nĩwega", "Agreement/acknowledgment", "Responses", DifficultyLevel.BEGINNER),
            ("goodness", "wega", "Abstract concept of goodness", "Responses", DifficultyLevel.INTERMEDIATE),
            ("Yes", "ii", "Affirmative response", "Responses", DifficultyLevel.BEGINNER),
            ("Yes (emphatic)", "niguo", "Emphatic affirmative", "Responses", DifficultyLevel.INTERMEDIATE),
            ("No", "Ai", "Negative response", "Responses", DifficultyLevel.BEGINNER),
            ("No (alternative)", "Aca", "Alternative negative", "Responses", DifficultyLevel.BEGINNER),
            ("No (emphatic)", "Aacha", "Emphatic negative", "Responses", DifficultyLevel.INTERMEDIATE),
            ("OK", "Haya", "General agreement/OK", "Responses", DifficultyLevel.BEGINNER),
            
            # Information requests
            ("Tell me", "Ta njira atiriri", "Request for information", "Questions", DifficultyLevel.ADVANCED),
            ("Show me", "Nyonia", "Request to be shown something", "Directions", DifficultyLevel.INTERMEDIATE),
            ("Direct me", "Nyonia", "Request for directions", "Directions", DifficultyLevel.INTERMEDIATE),
            
            # Hospitality
            ("Have a seat", "Ikara thi", "Invitation to sit down", "Courtesy", DifficultyLevel.INTERMEDIATE),
            
            # Commercial/practical
            ("How much is this (money)?", "Ni mbeca cigana?", "Price inquiry in monetary terms", "Commerce", DifficultyLevel.INTERMEDIATE),
            ("How much is this (quantity)?", "Cigana atia?", "Quantity inquiry", "Commerce", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        
        for english, kikuyu, context, category_name, difficulty in quizlet_data:
            # Check if this contribution already exists to avoid duplicates
            existing = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if existing:
                continue  # Skip duplicates silently
            
            category = categories[category_name]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Extracted from Quizlet Kikuyu greetings flash cards",
                quality_score=4.8,  # High quality flash card data
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for complex phrases
        complex_phrases = [
            # Time-specific greetings with morphological breakdown
            ("Good morning", "We mwega rũciinĩ", [
                ("You", "We", 0, "Second person subject pronoun"),
                ("good", "mwega", 1, "Adjective meaning good/well"),
                ("morning", "rũciinĩ", 2, "Time expression for morning")
            ]),
            ("Good afternoon", "We mwega umũthĩ", [
                ("You", "We", 0, "Second person subject pronoun"),
                ("good", "mwega", 1, "Adjective meaning good/well"),
                ("today/afternoon", "umũthĩ", 2, "Time expression for today/afternoon")
            ]),
            ("Good evening", "We mwega hwaĩinĩ", [
                ("You", "We", 0, "Second person subject pronoun"),
                ("good", "mwega", 1, "Adjective meaning good/well"),
                ("evening", "hwaĩinĩ", 2, "Time expression for evening")
            ]),
            # Future tense construction
            ("I will phone you", "Nĩngũkũhũrĩra thimû", [
                ("I will", "Nĩngũ-", 0, "First person future tense marker"),
                ("call/phone", "-kũhũrĩra", 1, "Verb stem for calling"),
                ("you", "thimû", 2, "Second person object pronoun")
            ]),
            # Complex question construction
            ("How much is this (money)?", "Ni mbeca cigana?", [
                ("Is", "Ni", 0, "Copula/linking verb"),
                ("money", "mbeca", 1, "Noun for money/currency"),
                ("how much", "cigana", 2, "Question word for quantity/amount")
            ])
        ]
        
        for source, target, sub_parts in complex_phrases:
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
        
        print(f"Successfully created {contribution_count} new contributions from Quizlet flash cards")
        print(f"Added sub-translations for {len(complex_phrases)} complex phrases")
        print("All data marked as approved for immediate use")
        
        # Print summary by category
        print("\nSummary by category:")
        for category_name, category in categories.items():
            count = db.query(Contribution).join(Contribution.categories).filter(
                Category.name == category_name
            ).count()
            print(f"   {category_name}: {count} contributions")
        
        # Print total count
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")


if __name__ == "__main__":
    print("Seeding database with Quizlet Kikuyu greetings flash cards...")
    try:
        create_quizlet_seed_data()
        print("Quizlet seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)