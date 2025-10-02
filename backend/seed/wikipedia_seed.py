#!/usr/bin/env python3
"""
Seed script for Wikipedia Kikuyu language data
Populates the database with extracted vocabulary, phrases, and linguistic information
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


def create_seed_data():
    """Create comprehensive seed data from Wikipedia extraction"""
    
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
        
        # Create categories for different types of content
        categories_data = [
            ("Greetings", "Basic greetings and social interactions", "greetings"),
            ("Questions", "Common questions and inquiries", "questions"),
            ("Responses", "Standard responses and reactions", "responses"),
            ("Time & Nature", "Time expressions and natural phenomena", "time-nature"),
            ("Spiritual & Cultural", "Religious and cultural terms", "spiritual-cultural"),
            ("Grammar Examples", "Examples demonstrating grammatical structures", "grammar-examples"),
            ("Pronunciation Guide", "Words for pronunciation practice", "pronunciation"),
            ("Noun Classes", "Examples of different noun class patterns", "noun-classes"),
            ("Verb Forms", "Verb conjugation examples", "verb-forms"),
            ("Cultural Expressions", "Traditional sayings and cultural phrases", "cultural-expressions")
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
        
        # Basic greetings and social interactions
        greetings_data = [
            ("How are you?", "Ũhoro waku", "Standard greeting asking about wellbeing", DifficultyLevel.BEGINNER),
            ("How are you? (alternative)", "kũhana atĩa?", "Alternative form of greeting", DifficultyLevel.BEGINNER),
            ("How are you doing?", "Ũrĩ mwega?", "Asking about someone's condition", DifficultyLevel.BEGINNER),
            ("How are you doing? (alternative)", "Wĩ mwega", "Alternative form", DifficultyLevel.BEGINNER),
            ("I am good", "Ndĩ mwega", "Standard positive response", DifficultyLevel.BEGINNER),
            ("Are you a friend?", "Wĩ mũrata?", "Asking about friendship", DifficultyLevel.INTERMEDIATE),
            ("Thank you", "Thengiũ", "Basic thank you expression", DifficultyLevel.BEGINNER),
            ("Thank you (formal)", "Nĩ wega", "Formal gratitude expression", DifficultyLevel.INTERMEDIATE),
            ("Thank you (traditional)", "Nĩ ngaatho", "Traditional gratitude", DifficultyLevel.INTERMEDIATE),
            ("I give thanks", "Nĩndacokia ngatho", "Extended gratitude expression", DifficultyLevel.ADVANCED),
            ("I'm blessed", "Ndĩĩ mũrathime", "Expression of being blessed", DifficultyLevel.INTERMEDIATE),
            ("Bye, be blessed", "Tigwo na wega", "Farewell blessing", DifficultyLevel.INTERMEDIATE),
            ("Bye, be blessed (alternative)", "Tigwo na thaayũ", "Alternative farewell", DifficultyLevel.INTERMEDIATE),
            ("Go in peace", "Thiĩ na thaayũ", "Peaceful farewell", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Common requests and needs
        requests_data = [
            ("Give me water", "He maaĩ", "Basic request for water", DifficultyLevel.BEGINNER),
            ("I am hungry", "Ndĩ mũhũtu", "Expression of hunger", DifficultyLevel.BEGINNER),
            ("Help me", "Ndeithia", "Request for assistance", DifficultyLevel.BEGINNER),
            ("Give me money", "He mbeca", "Request for money", DifficultyLevel.BEGINNER),
            ("Give me money (alternative)", "He mbia", "Alternative form", DifficultyLevel.BEGINNER),
            ("Come here", "Ũka haha", "Command to come", DifficultyLevel.BEGINNER),
            ("I will phone you", "Nĩngũkũhũrĩra thimũ", "Future communication", DifficultyLevel.ADVANCED),
        ]
        
        # Commands and behavioral directions
        commands_data = [
            ("Stop nonsense", "Tiga wana", "Command to stop silly behavior", DifficultyLevel.INTERMEDIATE),
            ("Stop nonsense (alternative)", "tiga ũrimũ", "Alternative form", DifficultyLevel.INTERMEDIATE),
            ("Don't laugh", "Ndũgatheke", "Negative command", DifficultyLevel.INTERMEDIATE),
            ("You are learned", "Wĩ mũthomu", "Compliment on education", DifficultyLevel.ADVANCED),
        ]
        
        # Emotional expressions
        emotions_data = [
            ("I love you", "Nĩngwendete", "Expression of love", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Time and nature vocabulary
        time_nature_data = [
            ("Day", "Mũthenya", "Daytime period", DifficultyLevel.BEGINNER),
            ("Night", "Ũtukũ", "Nighttime period", DifficultyLevel.BEGINNER),
        ]
        
        # Spiritual and cultural terms
        spiritual_data = [
            ("God", "Ngai", "Supreme deity in Kikuyu tradition", DifficultyLevel.BEGINNER),
            ("Ancestral Spirits", "Ngomi", "Spirits of ancestors", DifficultyLevel.ADVANCED),
            ("Country/State/Nation", "Bũrũri", "Political/geographical entity", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Personal identifiers demonstrating noun classes
        identity_data = [
            ("A Kikuyu person", "MũGĩkũyũ", "Class 1 noun (mũ- prefix, singular human)", DifficultyLevel.INTERMEDIATE),
            ("Kikuyu people", "AGĩkũyũ", "Class 2 noun (a- prefix, plural human)", DifficultyLevel.INTERMEDIATE),
            ("Kikuyu language", "GĩGĩkũyũ", "Language name", DifficultyLevel.INTERMEDIATE),
            ("Land of Kikuyu", "Bũrũrĩ Wa Gĩkũyũ", "Traditional homeland", DifficultyLevel.ADVANCED),
        ]
        
        # Geographic terms
        geography_data = [
            ("Mount Kenya", "Kĩrĩmanyaga", "Sacred mountain in Kikuyu culture", DifficultyLevel.ADVANCED),
        ]
        
        # Sample religious/cultural text
        cultural_text_data = [
            ("The Gikuyu believe in God", "Gĩkũyũ nĩ gĩtĩkĩtie Ngai", "Religious belief statement", DifficultyLevel.ADVANCED),
            ("creator of heaven and earth", "mumbi wa Igũrũ na Thĩ na mũheani wa indo ciothe", "Description of God's role", DifficultyLevel.ADVANCED),
        ]
        
        # Combine all data sets
        all_contributions = [
            (greetings_data, "Greetings"),
            (requests_data, "Questions"),
            (commands_data, "Responses"),
            (emotions_data, "Responses"),
            (time_nature_data, "Time & Nature"),
            (spiritual_data, "Spiritual & Cultural"),
            (identity_data, "Noun Classes"),
            (geography_data, "Cultural Expressions"),
            (cultural_text_data, "Cultural Expressions"),
        ]
        
        contribution_count = 0
        
        for data_set, category_name in all_contributions:
            category = categories[category_name]
            
            for english, kikuyu, context, difficulty in data_set:
                # Create contribution
                contribution = Contribution(
                    source_text=english,
                    target_text=kikuyu,
                    status=ContributionStatus.APPROVED,  # Pre-approved seed data
                    language="kikuyu",
                    difficulty_level=difficulty,
                    context_notes=context,
                    cultural_notes="Extracted from Wikipedia Kikuyu language article",
                    quality_score=5.0,  # High quality seed data
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()  # Get the ID
                
                # Associate with category
                contribution.categories.append(category)
                
                contribution_count += 1
        
        # Create sub-translations for complex phrases to help with learning
        sub_translations_data = [
            # For "Nĩngũkũhũrĩra thimũ" (I will phone you)
            ("I will phone you", "Nĩngũkũhũrĩra thimũ", [
                ("I will", "Nĩngũ-", 0, "Future tense marker with subject agreement"),
                ("phone", "-kũhũrĩra", 1, "Verb stem for calling/phoning"),
                ("you", "thimũ", 2, "Object pronoun 'you'")
            ]),
            # For "Gĩkũyũ nĩ gĩtĩkĩtie Ngai" (The Gikuyu believe in God)
            ("The Gikuyu believe in God", "Gĩkũyũ nĩ gĩtĩkĩtie Ngai", [
                ("Gikuyu", "Gĩkũyũ", 0, "The Kikuyu people"),
                ("believe", "nĩ gĩtĩkĩtie", 1, "Present perfect tense 'have believed'"),
                ("God", "Ngai", 2, "Supreme deity")
            ]),
            # For "mumbi wa Igũrũ na Thĩ na mũheani wa indo ciothe"
            ("creator of heaven and earth, the giver of all things", "mumbi wa Igũrũ na Thĩ na mũheani wa indo ciothe", [
                ("creator", "mumbi", 0, "One who creates/builds"),
                ("of heaven", "wa Igũrũ", 1, "Possessive: of the sky/heaven"),
                ("and earth", "na Thĩ", 2, "And the earth"),
                ("and giver", "na mũheani", 3, "And the one who gives"),
                ("of all things", "wa indo ciothe", 4, "Of all things/possessions")
            ])
        ]
        
        for source, target, sub_parts in sub_translations_data:
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
                        context=explanation,  # Use 'context' field instead of 'explanation'
                        created_by_id=admin_user.id
                    )
                    db.add(sub_translation)
                
                # Mark parent as having sub-translations
                parent.has_sub_translations = True
        
        db.commit()
        
        print(f"Successfully created {contribution_count} contributions from Wikipedia Kikuyu data")
        print(f"Created {len(categories_data)} categories")
        print(f"Added sub-translations for complex phrases")
        print("All data marked as approved for immediate use")
        
        # Print summary by category
        print("\nSummary by category:")
        for category_name, category in categories.items():
            count = db.query(Contribution).join(Contribution.categories).filter(
                Category.name == category_name
            ).count()
            print(f"   {category_name}: {count} contributions")


if __name__ == "__main__":
    print("Seeding database with Wikipedia Kikuyu language data...")
    try:
        create_seed_data()
        print("Seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)