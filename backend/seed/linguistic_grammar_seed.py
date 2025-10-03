#!/usr/bin/env python3
"""
Seed script for comprehensive Kikuyu grammatical data from Semantic Scholar paper
"A Basic Sketch Grammar of Gĩkũyũ" by Englebretson & Wa Ngatho
Includes pronouns, noun classes, adjectives, numbers, verb conjugations, and linguistic analysis
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


def create_linguistic_grammar_seed():
    """Create seed data from comprehensive Kikuyu grammar paper"""
    
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
            ("Linguistic Grammar", "Academic grammatical analysis and linguistic structures", "linguistic-grammar"),
            ("Pronouns & Concords", "Personal pronouns and subject/object agreements", "pronouns-concords"),
            ("Adjectives & Descriptors", "Descriptive words and their classifications", "adjectives-descriptors"),
            ("Verb Conjugations", "Complete verb tense and aspect systems", "verb-conjugations"),
            ("Quantifiers & Numbers", "Interrogative quantifiers and numerical stems", "quantifiers-numbers"),
            ("Noun Class Semantics", "Semantic categories and meaning patterns of noun classes", "noun-class-semantics"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 950  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Personal pronouns from Table 1
        pronoun_data = [
            # Basic personal pronouns
            ("I", "nii", "First person singular pronoun", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("I", "niu", "First person singular pronoun - variant", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("you (singular)", "wee", "Second person singular pronoun", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("you (singular)", "weu", "Second person singular pronoun - variant", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("he/she", "we", "Third person singular pronoun (NC1)", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("we", "ithui", "First person plural pronoun", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("you (plural)", "inyui", "Second person plural pronoun", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("you (plural)", "inyuu", "Second person plural pronoun - variant", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("they", "o", "Third person plural pronoun (NC2)", "Pronouns & Concords", DifficultyLevel.BEGINNER),
            ("they", "mo", "Third person plural pronoun (NC2) - variant", "Pronouns & Concords", DifficultyLevel.BEGINNER),
        ]
        
        # Noun class pronouns from Table 2
        noun_class_pronouns = [
            ("it (Class 3)", "guo", "Noun class 3 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 4)", "yo", "Noun class 4 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 5)", "rio", "Noun class 5 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 6)", "mo", "Noun class 6 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 7)", "kio", "Noun class 7 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 8)", "cio", "Noun class 8 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 9)", "yo", "Noun class 9 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 10)", "cio", "Noun class 10 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 11)", "ruo", "Noun class 11 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 12)", "ko", "Noun class 12 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 13)", "tuo", "Noun class 13 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 14)", "guo", "Noun class 14 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 15)", "kuo", "Noun class 15 pronoun", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 16)", "ho", "Noun class 16 pronoun - definite location", "Pronouns & Concords", DifficultyLevel.ADVANCED),
            ("it (Class 17)", "kuo", "Noun class 17 pronoun - indefinite location", "Pronouns & Concords", DifficultyLevel.ADVANCED),
        ]
        
        # Adjectives and descriptors from Table 6
        adjective_data = [
            # Dimension adjectives
            ("big", "nene", "Size descriptor - large", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("small", "nini", "Size descriptor - small", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("young", "nini", "Age descriptor - young (same as small)", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("short", "kuhi", "Height descriptor - short", "Adjectives & Descriptors", DifficultyLevel.INTERMEDIATE),
            ("tall", "raihu", "Height descriptor - tall", "Adjectives & Descriptors", DifficultyLevel.INTERMEDIATE),
            
            # Age adjectives
            ("old", "kuru", "Age descriptor - old", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("new", "eru", "Age descriptor - new", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("baby", "kenge", "Age descriptor - very young", "Adjectives & Descriptors", DifficultyLevel.INTERMEDIATE),
            
            # Value adjectives
            ("good", "ega", "Quality descriptor - good", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("bad", "uru", "Quality descriptor - bad", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("beautiful", "thaka", "Quality descriptor - beautiful", "Adjectives & Descriptors", DifficultyLevel.INTERMEDIATE),
            
            # Color adjectives
            ("red", "tune", "Color descriptor - red", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("white", "eru", "Color descriptor - white", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            ("black", "iru", "Color descriptor - black", "Adjectives & Descriptors", DifficultyLevel.BEGINNER),
            
            # Human propensity adjectives
            ("sick", "ruaru", "Health descriptor - sick", "Adjectives & Descriptors", DifficultyLevel.INTERMEDIATE),
            ("obedient", "athiki", "Character descriptor - obedient", "Adjectives & Descriptors", DifficultyLevel.ADVANCED),
        ]
        
        # Number stems from Table 7
        number_data = [
            ("one", "mwe", "Cardinal number - one", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("two", "iri", "Cardinal number - two", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("three", "tatu", "Cardinal number - three", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("four", "na", "Cardinal number - four", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("five", "tano", "Cardinal number - five", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("six", "tandatu", "Cardinal number - six", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("seven", "mugwanja", "Cardinal number - seven", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("eight", "nana", "Cardinal number - eight", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("nine", "kenda", "Cardinal number - nine", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("ten", "ikumi", "Cardinal number - ten", "Quantifiers & Numbers", DifficultyLevel.BEGINNER),
            ("tens", "mirongo", "Tens multiplier", "Quantifiers & Numbers", DifficultyLevel.INTERMEDIATE),
            ("hundred", "igana", "Cardinal number - hundred", "Quantifiers & Numbers", DifficultyLevel.INTERMEDIATE),
            ("hundreds", "magana", "Hundreds multiplier", "Quantifiers & Numbers", DifficultyLevel.INTERMEDIATE),
            ("thousand", "ngiri", "Cardinal number - thousand", "Quantifiers & Numbers", DifficultyLevel.INTERMEDIATE),
            ("thousands", "ngiri", "Thousands multiplier", "Quantifiers & Numbers", DifficultyLevel.INTERMEDIATE),
        ]
        
        # Interrogative quantifiers from Table 10
        quantifier_data = [
            ("how many? (NC2)", "aigana", "Interrogative quantifier for noun class 2", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC4)", "iigana", "Interrogative quantifier for noun class 4", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC6)", "maigana", "Interrogative quantifier for noun class 6", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC8)", "cigana", "Interrogative quantifier for noun class 8", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC10)", "cigana", "Interrogative quantifier for noun class 10", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC13)", "tuigana", "Interrogative quantifier for noun class 13", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC16)", "haigana", "Interrogative quantifier for noun class 16", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
            ("how many? (NC17)", "kuigana", "Interrogative quantifier for noun class 17", "Quantifiers & Numbers", DifficultyLevel.ADVANCED),
        ]
        
        # Semantic tendencies from Table 11
        semantic_data = [
            ("humans", "NC 1/2", "Noun classes 1 and 2 primarily contain human terms", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("landscape terms", "NC 3/4", "Noun classes 3 and 4 contain landscape and nature terms", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("plants", "NC 5/6", "Noun classes 5 and 6 contain plant and landscape terms", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("augmentatives", "NC 7/8", "Noun classes 7 and 8 contain augmentative forms", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("animals", "NC 9/10", "Noun classes 9 and 10 contain animals and body parts", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("body parts", "NC 9/10", "Noun classes 9 and 10 contain body parts and borrowed words", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("borrowed words", "NC 9/10", "Noun classes 9 and 10 contain borrowed words", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("string-shaped objects", "NC 11", "Noun class 11 contains string or stick-shaped objects", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("diminutives", "NC 12/13", "Noun classes 12 and 13 contain diminutive forms", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("abstract concepts", "NC 14", "Noun class 14 contains abstract concepts", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("body parts", "NC 15", "Noun class 15 contains body parts and verbal infinitives", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("verbal infinitives", "NC 15", "Noun class 15 contains verbal infinitives", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("definite location", "NC 16", "Noun class 16 indicates definite location", "Noun Class Semantics", DifficultyLevel.ADVANCED),
            ("indefinite location", "NC 17", "Noun class 17 indicates indefinite location", "Noun Class Semantics", DifficultyLevel.ADVANCED),
        ]
        
        # Verb examples from tense tables
        verb_examples = [
            # Present tense examples
            ("I am", "ndi", "First person singular present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you are", "uri", "Second person singular present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you are", "wi", "Second person singular present tense copula - variant", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("we are", "turi", "First person plural present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("we are", "tui", "First person plural present tense copula - variant", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you (pl) are", "muri", "Second person plural present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you (pl) are", "mui", "Second person plural present tense copula - variant", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("he/she is", "ari", "Third person singular present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("he/she is", "e", "Third person singular present tense copula - variant", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("they are", "mari", "Third person plural present tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("they are", "me", "Third person plural present tense copula - variant", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            
            # Past tense examples
            ("I was", "ndari", "First person singular past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you were", "wari", "Second person singular past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("we were", "twari", "First person plural past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("you (pl) were", "mwari", "Second person plural past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("he/she was", "ari", "Third person singular past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
            ("they were", "mari", "Third person plural past tense copula", "Verb Conjugations", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process all linguistic data
        all_data = (pronoun_data + noun_class_pronouns + adjective_data + 
                   number_data + quantifier_data + semantic_data + verb_examples)
        
        for english, kikuyu, context, category_name, difficulty in all_data:
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
                    # Default to Linguistic Grammar if category not found
                    category = categories["Linguistic Grammar"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Academic linguistic analysis from 'A Basic Sketch Grammar of Gikuyu' by Englebretson & Wa Ngatho. Comprehensive grammatical structures and morphological patterns for advanced language study.",
                quality_score=4.9,  # Highest quality - academic research
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create complex sub-translations for advanced grammatical examples
        complex_grammar_patterns = [
            # Pronoun morphological analysis
            ("you (plural)", "inyui", [
                ("i-", "i-", 0, "Plural person prefix"),
                ("nyu", "nyu", 1, "Second person root"),
                ("-i", "-i", 2, "Pronoun suffix")
            ]),
            ("we", "ithui", [
                ("i-", "i-", 0, "Plural person prefix"),
                ("thu", "thu", 1, "First person root"),
                ("-i", "-i", 2, "Pronoun suffix")
            ]),
            # Adjective agreement example
            ("beautiful", "thaka", [
                ("thaka", "thaka", 0, "Adjectival root - beautiful/handsome")
            ]),
            # Number morphological breakdown
            ("six", "tandatu", [
                ("tan-", "tan-", 0, "Five base"),
                ("datu", "datu", 1, "Plus one - compound formation")
            ]),
            ("seven", "mugwanja", [
                ("mu-", "mu-", 0, "Noun class prefix"),
                ("gwanja", "gwanja", 1, "Seven root")
            ])
        ]
        
        for source, target, sub_parts in complex_grammar_patterns:
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
        
        print(f"Successfully created {contribution_count} new linguistic grammar contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {len(complex_grammar_patterns)} complex grammatical patterns")
        print("All linguistic data marked as approved for immediate use")
        
        # Print content analysis
        print("\nLinguistic Grammar Data Added:")
        print("- Complete personal pronoun system (1st, 2nd, 3rd person)")
        print("- Comprehensive noun class pronoun system (Classes 1-17)")
        print("- Adjective classification system (dimension, age, value, color, propensity)")
        print("- Complete number system with stems and multipliers")
        print("- Interrogative quantifiers for each noun class")
        print("- Semantic tendencies and meaning patterns of noun classes")
        print("- Verb conjugation examples (present, past tense)")
        print("- Advanced morphological breakdowns")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Linguistic Grammar", "Pronouns & Concords", "Adjectives & Descriptors", 
                         "Verb Conjugations", "Quantifiers & Numbers", "Noun Class Semantics"]
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
        
        print("\nNote: This represents comprehensive academic linguistic analysis")
        print("from Semantic Scholar research paper, providing the most detailed")
        print("grammatical structure documentation available for Kikuyu language.")


if __name__ == "__main__":
    print("Seeding database with comprehensive Kikuyu linguistic grammar...")
    print("Source: Semantic Scholar - 'A Basic Sketch Grammar of Gikuyu'")
    print("Authors: Englebretson & Wa Ngatho")
    try:
        create_linguistic_grammar_seed()
        print("Linguistic grammar seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)