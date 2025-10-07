#!/usr/bin/env python3
"""
Seed script for Kikuyu verbs from Quizlet flash cards
Populates the database with verb vocabulary and conjugation patterns
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


def create_verbs_seed_data():
    """Create seed data for Kikuyu verbs from Quizlet flash cards"""
    
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
            ("Verb Forms", "Verb conjugations and tenses", "verb-forms"),
            ("Infinitives", "Infinitive verb forms", "infinitives"),
            ("Grammar Patterns", "Grammatical constructions and patterns", "grammar-patterns"),
            ("Commands", "Imperative forms and commands", "commands"),
            ("Expressions", "Common verbal expressions", "expressions"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 100  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Verb infinitives extracted from the file
        verb_infinitives = [
            ("to go", "gũthii", "Basic movement verb - infinitive form", "Infinitives", DifficultyLevel.BEGINNER),
            ("to do", "gũĩka", "General action verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to stay waiting", "gũtinda", "Verb expressing waiting/delaying", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to cut", "gũtinia", "Action verb for cutting", "Infinitives", DifficultyLevel.BEGINNER),
            ("to take something somewhere", "gũtwara", "Verb for transporting/carrying", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to love/to like", "kũenda", "Emotional verb - love/like", "Infinitives", DifficultyLevel.BEGINNER),
            ("to buy", "kũgũra", "Commercial transaction verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to be alike", "kũhaana", "Comparative verb - resemblance", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to climb", "kũhaica", "Movement verb - upward motion", "Infinitives", DifficultyLevel.BEGINNER),
            ("to brush", "kũhara", "Action verb - cleaning/brushing", "Infinitives", DifficultyLevel.BEGINNER),
            ("to be married", "kũhika", "State verb - marital status", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to be able", "kũhota", "Modal verb - ability/capability", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to dress", "kũhumba", "Action verb - clothing", "Infinitives", DifficultyLevel.BEGINNER),
            ("to drink", "kũnywa", "Basic consumption verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to finish/to decide", "kũrĩkia", "Completion/decision verb", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to work", "kũruta wĩra", "Activity verb - labor/work", "Infinitives", DifficultyLevel.BEGINNER),
            ("to arrive/to reach", "gũkinya", "Movement verb - arrival", "Infinitives", DifficultyLevel.BEGINNER),
            ("to lead", "gũtongoria", "Action verb - leadership", "Infinitives", DifficultyLevel.ADVANCED),
            ("to come", "gũũka", "Basic movement verb - approach", "Infinitives", DifficultyLevel.BEGINNER),
            ("to greet", "kũgeithia", "Social interaction verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to hide", "kũhitha", "Action verb - concealment", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to explain", "gũtarĩria", "Communication verb", "Infinitives", DifficultyLevel.ADVANCED),
            ("to die", "gũkua", "State change verb", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to read/to study", "gũthoma", "Learning activity verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to carry", "gũkuua", "Physical action verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to finish", "kũnina", "Completion verb (alternative)", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to eat", "kũrĩa", "Basic consumption verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to laugh", "gũtheka", "Emotional expression verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to cook", "kũruga", "Food preparation verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to educate/to teach", "kũrutana", "Educational activity verb", "Infinitives", DifficultyLevel.INTERMEDIATE),
            ("to answer a question", "gũcokia kĩũria", "Communication verb phrase", "Infinitives", DifficultyLevel.ADVANCED),
            ("to wake up", "ukira", "State change verb", "Infinitives", DifficultyLevel.BEGINNER),
            ("to sleep", "koma", "State verb - rest", "Infinitives", DifficultyLevel.BEGINNER),
        ]
        
        # Conjugated verb examples
        verb_conjugations = [
            # "To go" conjugations - present progressive
            ("I am going", "ni ndirathii", "Present progressive 1st person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you are going", "ni urathii", "Present progressive 2nd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("s/he is going", "ni arathii", "Present progressive 3rd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("we are going", "ni turathii", "Present progressive 1st person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you (pl) are going", "ni murathii", "Present progressive 2nd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("they are going", "ni marathii", "Present progressive 3rd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            
            # "To go" conjugations - past
            ("I went", "ni ndirathiire", "Past tense 1st person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you went", "ni urathiire", "Past tense 2nd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("s/he went", "ni arathiire", "Past tense 3rd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("we went", "ni turathiire", "Past tense 1st person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you (pl) went", "ni murathiire", "Past tense 2nd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("they went", "ni marathiire", "Past tense 3rd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            
            # "To go" conjugations - future
            ("I will go", "ni ngathii", "Future tense 1st person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you will go", "ni ugathii", "Future tense 2nd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("s/he will go", "ni agathii", "Future tense 3rd person singular", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("we will go", "ni tugathii", "Future tense 1st person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("you (pl) will go", "ni mugathii", "Future tense 2nd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
            ("they will go", "ni magathii", "Future tense 3rd person plural", "Verb Forms", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Copulative "to be" forms - affirmative
        copulative_affirmative = [
            ("I am a teacher", "Ndĩrĩ mũrutani", "Copulative present affirmative 1st person", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
            ("you are a teacher", "Ũrĩ mũrutani", "Copulative present affirmative 2nd person", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
            ("he is a teacher", "Nĩ mũrutani", "Copulative present affirmative 3rd person", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
            ("we are teachers", "Tũrĩ arutani", "Copulative present affirmative 1st plural", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
            ("you are teachers", "Mũrĩ arutani", "Copulative present affirmative 2nd plural", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
            ("they are teachers", "Marĩ arutani", "Copulative present affirmative 3rd plural", "Grammar Patterns", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Copulative "to be" forms - negative
        copulative_negative = [
            ("I am not a teacher", "Ndirĩ mũrutani", "Copulative present negative 1st person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("you are not a teacher", "Ndũrĩ mũrutani", "Copulative present negative 2nd person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("he is not a teacher", "Ti mũrutani", "Copulative present negative 3rd person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("we are not teachers", "Tũtirĩ arutani", "Copulative present negative 1st plural", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("you are not teachers", "Mũtirĩ arutani", "Copulative present negative 2nd plural", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("they are not teachers", "Matirĩ arutani", "Copulative present negative 3rd plural", "Grammar Patterns", DifficultyLevel.ADVANCED),
        ]
        
        # Perfect tense examples
        perfect_tense = [
            ("I have already eaten bananas", "Ndĩra-korwo nd-aarĩa marigũ", "Present perfect 1st person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("you have already eaten bananas", "Ũra-korwo w-aarĩa marigũ", "Present perfect 2nd person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("he has already eaten bananas", "Ara-korwo a-arĩa marigũ", "Present perfect 3rd person", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("we have already eaten bananas", "Tũra-korwo tw-aarĩa marĩgũ", "Present perfect 1st plural", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("they have already eaten bananas", "Mũra-korwo mw-aarĩa marigũ", "Present perfect 3rd plural", "Grammar Patterns", DifficultyLevel.ADVANCED),
        ]
        
        # Negative infinitives
        negative_infinitives = [
            ("not to read", "kwaga gũthoma", "Negative infinitive - reading", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("not to do", "kwaga gwĩka", "Negative infinitive - doing", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("not to answer", "kwaga gũcokia kĩũria", "Negative infinitive - answering", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("not to come back", "kwaga gũcoka", "Negative infinitive - returning", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("not to eat", "kwaga kũrĩa", "Negative infinitive - eating", "Grammar Patterns", DifficultyLevel.ADVANCED),
            ("not to die", "kwaga gũkua", "Negative infinitive - dying", "Grammar Patterns", DifficultyLevel.ADVANCED),
        ]
        
        # Commands and expressions
        commands_expressions = [
            ("come here", "Ũka haha", "Imperative command - approach", "Commands", DifficultyLevel.BEGINNER),
            ("have a seat", "Ikara thi", "Polite command - sitting", "Commands", DifficultyLevel.BEGINNER),
            ("tell me", "Ta njira atiriri", "Command requesting information", "Commands", DifficultyLevel.INTERMEDIATE),
            ("show me/direct me", "Nyonia", "Command for guidance", "Commands", DifficultyLevel.INTERMEDIATE),
            ("help me", "Ndeithia", "Request for assistance", "Commands", DifficultyLevel.BEGINNER),
            ("I love you", "Nĩngwendete", "Expression of affection", "Expressions", DifficultyLevel.INTERMEDIATE),
            ("I am hungry", "Ndĩ mũhũtu", "Expression of state/need", "Expressions", DifficultyLevel.BEGINNER),
        ]
        
        # Combine all data sets
        all_verb_data = [
            (verb_infinitives, "verb infinitives"),
            (verb_conjugations, "verb conjugations"),
            (copulative_affirmative, "copulative affirmative"),
            (copulative_negative, "copulative negative"),
            (perfect_tense, "perfect tense"),
            (negative_infinitives, "negative infinitives"),
            (commands_expressions, "commands and expressions"),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for data_set, data_type in all_verb_data:
            for english, kikuyu, context, category_name, difficulty in data_set:
                # Check if this contribution already exists to avoid duplicates
                existing = db.query(Contribution).filter(
                    Contribution.source_text == english,
                    Contribution.target_text == kikuyu
                ).first()
                
                if existing:
                    skipped_count += 1
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
                    cultural_notes="Extracted from Quizlet Kikuyu verbs flash cards",
                    quality_score=4.7,  # High quality flash card data
                    created_by_id=admin_user.id
                )
                
                db.add(contribution)
                db.flush()  # Get the ID
                
                # Associate with category
                contribution.categories.append(category)
                
                contribution_count += 1
        
        # Create sub-translations for complex grammatical patterns
        complex_patterns = [
            # Perfect tense breakdown
            ("I have already eaten bananas", "Ndĩra-korwo nd-aarĩa marigũ", [
                ("I", "Ndĩ-", 0, "1st person subject marker"),
                ("have (perfect)", "-ra-korwo", 1, "Perfect tense auxiliary"),
                ("I", "nd-", 2, "1st person subject agreement"),
                ("ate", "-aarĩa", 3, "Past tense of 'eat'"),
                ("bananas", "marigũ", 4, "Object noun - bananas")
            ]),
            # Future tense breakdown
            ("I will go", "ni ngathii", [
                ("focus marker", "ni", 0, "Focus/emphasis particle"),
                ("I will", "nga-", 1, "1st person future tense"),
                ("go", "-thii", 2, "Verb stem 'go'")
            ]),
            # Negative infinitive pattern
            ("not to read", "kwaga gũthoma", [
                ("failure/lack", "kwaga", 0, "Negative infinitive marker"),
                ("to read", "gũthoma", 1, "Infinitive verb form")
            ]),
            # Complex verb phrase
            ("to work", "kũruta wĩra", [
                ("to do/perform", "kũruta", 0, "Infinitive 'to do'"),
                ("work/job", "wĩra", 1, "Noun for work/labor")
            ]),
            # Compound infinitive
            ("to answer a question", "gũcokia kĩũria", [
                ("to return/reply", "gũcokia", 0, "Infinitive 'to return/answer'"),
                ("question", "kĩũria", 1, "Noun for question")
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
        
        print(f"Successfully created {contribution_count} new verb contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added sub-translations for {len(complex_patterns)} complex patterns")
        print("All data marked as approved for immediate use")
        
        # Print summary by category
        print("\nSummary by category:")
        for category_name, category in categories.items():
            count = db.query(Contribution).join(Contribution.categories).filter(
                Category.name == category_name
            ).count()
            if count > 0:
                print(f"   {category_name}: {count} contributions")
        
        # Print verb system analysis
        print("\nKikuyu Verb System Features Added:")
        print("- Infinitive forms with kũ-/gũ- prefixes")
        print("- Complete conjugation paradigms (present, past, future)")
        print("- Copulative 'to be' (affirmative and negative)")
        print("- Perfect tense with 'korwo' auxiliary")
        print("- Negative infinitives with 'kwaga'")
        print("- Focus particles and agreement markers")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")


if __name__ == "__main__":
    print("Seeding database with Kikuyu verbs from Quizlet flash cards...")
    try:
        create_verbs_seed_data()
        print("Verbs seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)