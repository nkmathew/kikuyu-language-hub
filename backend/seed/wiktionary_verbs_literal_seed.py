#!/usr/bin/env python3
"""
Literal hardcoded seed script for Wiktionary verbs
Contains actual extracted verb data from Wiktionary parsing
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


def create_wiktionary_verbs_literal_seed():
    """Create seed data from literal Wiktionary verb extractions"""
    
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
            ("Wiktionary Verbs", "Comprehensive verb collection from Wiktionary with IPA pronunciations", "wiktionary-verbs"),
            ("Verb Infinitives", "Infinitive forms of Kikuyu verbs", "verb-infinitives"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1300  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Literal extracted verb data from Wiktionary
        wiktionary_verbs = [
            ("stretch and peg a hide", "amba", "Infinitive: kwamba | IPA: /aᵐba/", DifficultyLevel.ADVANCED),
            ("do", "amba", "Infinitive: kwamba | IPA: /aᵐba/", DifficultyLevel.BEGINNER),
            ("peg (out)", "ambata", "Infinitive: kwambata | IPA: /aᵐbata/", DifficultyLevel.INTERMEDIATE),
            ("stretch out", "ambata", "Infinitive: kwambata | IPA: /aᵐbata/", DifficultyLevel.INTERMEDIATE),
            ("do first", "ambĩrĩria", "Infinitive: kwambĩrĩria | IPA: /aᵐbeɾeɾia/", DifficultyLevel.INTERMEDIATE),
            ("write", "andĩka", "Infinitive: kwandĩka | IPA: /aⁿdeka/", DifficultyLevel.BEGINNER),
            ("be", "butha", "Infinitive: kũbutha | IPA: /βuða/", DifficultyLevel.BEGINNER),
            ("hold", "cokia", "IPA: /ɕɔːkia/", DifficultyLevel.INTERMEDIATE),
            ("be", "conoka", "IPA: /ɕɔnɔka/", DifficultyLevel.BEGINNER),
            ("go", "enda", "Infinitive: kũenda | IPA: /ɛⁿda/", DifficultyLevel.BEGINNER),
            ("like", "enda", "Infinitive: kũenda | IPA: /ɛⁿda/", DifficultyLevel.BEGINNER),
            ("want", "enda", "Infinitive: kũenda | IPA: /ɛⁿda/", DifficultyLevel.BEGINNER),
            ("dig", "enja", "Infinitive: kwenja | IPA: /ɛᶮdʑa/", DifficultyLevel.BEGINNER),
            ("shave", "enja", "Infinitive: kwenja | IPA: /ɛᶮdʑa/", DifficultyLevel.INTERMEDIATE),
            ("try", "geria", "Infinitive: kũgeria | IPA: /ɣɛɾia/", DifficultyLevel.BEGINNER),
            ("kill", "geria", "Infinitive: kũgeria | IPA: /ɣɛɾia/", DifficultyLevel.INTERMEDIATE),
            ("buy", "gũra", "Infinitive: kũgũra | IPA: /ɣuɾa/", DifficultyLevel.BEGINNER),
            ("catch", "gwata", "Infinitive: kũgwata | IPA: /ɣʷata/", DifficultyLevel.BEGINNER),
            ("become", "haka", "Infinitive: kũhaka | IPA: /haka/", DifficultyLevel.INTERMEDIATE),
            ("think", "hitha", "Infinitive: kũhitha | IPA: /hiða/", DifficultyLevel.BEGINNER),
            ("be able", "hota", "Infinitive: kũhota | IPA: /hɔta/", DifficultyLevel.BEGINNER),
            ("can", "hota", "Infinitive: kũhota | IPA: /hɔta/", DifficultyLevel.BEGINNER),
            ("request", "hoya", "Infinitive: kũhoya | IPA: /hɔːja/", DifficultyLevel.INTERMEDIATE),
            ("ask", "hoya", "Infinitive: kũhoya | IPA: /hɔːja/", DifficultyLevel.BEGINNER),
            ("learn", "iga", "Infinitive: kwĩga | IPA: /iɣa/", DifficultyLevel.BEGINNER),
            ("put", "iga", "Infinitive: kwĩga | IPA: /iɣa/", DifficultyLevel.BEGINNER),
            ("hear", "igua", "Infinitive: kwĩgua | IPA: /iɣua/", DifficultyLevel.BEGINNER),
            ("feel", "igua", "Infinitive: kwĩgua | IPA: /iɣua/", DifficultyLevel.BEGINNER),
            ("make", "ĩka", "Infinitive: kwĩka | IPA: /eka/", DifficultyLevel.BEGINNER),
            ("do", "ĩka", "Infinitive: kwĩka | IPA: /eka/", DifficultyLevel.BEGINNER),
            ("sit", "ikara", "Infinitive: kwĩkara | IPA: /ikara/", DifficultyLevel.BEGINNER),
            ("stay", "ikara", "Infinitive: kwĩkara | IPA: /ikara/", DifficultyLevel.BEGINNER),
            ("live", "ikara", "Infinitive: kwĩkara | IPA: /ikara/", DifficultyLevel.BEGINNER),
            ("sing", "ina", "Infinitive: kwĩna | IPA: /ina/", DifficultyLevel.BEGINNER),
            ("dance", "ina", "Infinitive: kwĩna | IPA: /ina/", DifficultyLevel.BEGINNER),
            ("come", "ũka", "Infinitive: kũũka | IPA: /uːka/", DifficultyLevel.BEGINNER),
            ("call", "ĩta", "Infinitive: kwĩta | IPA: /eta/", DifficultyLevel.BEGINNER),
            ("name", "ĩta", "Infinitive: kwĩta | IPA: /eta/", DifficultyLevel.BEGINNER),
            ("steal", "iya", "Infinitive: kwĩya | IPA: /ija/", DifficultyLevel.INTERMEDIATE),
            ("be happy", "kena", "Infinitive: gũkena | IPA: /kɛna/", DifficultyLevel.BEGINNER),
            ("laugh", "kena", "Infinitive: gũkena | IPA: /kɛna/", DifficultyLevel.BEGINNER),
            ("pass", "kira", "Infinitive: gũkira | IPA: /kira/", DifficultyLevel.BEGINNER),
            ("bite", "koma", "Infinitive: gũkoma | IPA: /kɔma/", DifficultyLevel.BEGINNER),
            ("grow", "kũra", "Infinitive: gũkũra | IPA: /kuɾa/", DifficultyLevel.BEGINNER),
            ("die", "kua", "Infinitive: gũkua | IPA: /kua/", DifficultyLevel.BEGINNER),
            ("hurt", "kumia", "Infinitive: gũkumia | IPA: /kumia/", DifficultyLevel.INTERMEDIATE),
            ("know", "menya", "Infinitive: kũmenya | IPA: /mɛnja/", DifficultyLevel.BEGINNER),
            ("see", "ona", "Infinitive: kũona | IPA: /ɔna/", DifficultyLevel.BEGINNER),
            ("take", "oya", "Infinitive: kũoya | IPA: /ɔja/", DifficultyLevel.BEGINNER),
            ("get", "oya", "Infinitive: kũoya | IPA: /ɔja/", DifficultyLevel.BEGINNER),
            ("lie down", "rara", "Infinitive: gũrara | IPA: /ɾaɾa/", DifficultyLevel.BEGINNER),
            ("sleep", "rara", "Infinitive: gũrara | IPA: /ɾaɾa/", DifficultyLevel.BEGINNER),
            ("refuse", "rega", "Infinitive: kũrega | IPA: /ɾɛɣa/", DifficultyLevel.BEGINNER),
            ("deny", "rega", "Infinitive: kũrega | IPA: /ɾɛɣa/", DifficultyLevel.INTERMEDIATE),
            ("eat", "rĩa", "Infinitive: kũrĩa | IPA: /ɾea/", DifficultyLevel.BEGINNER),
            ("hurt", "rĩa", "Infinitive: kũrĩa | IPA: /ɾea/", DifficultyLevel.INTERMEDIATE),
            ("harm", "rĩa", "Infinitive: kũrĩa | IPA: /ɾea/", DifficultyLevel.INTERMEDIATE),
            ("damage", "rĩa", "Infinitive: kũrĩa | IPA: /ɾea/", DifficultyLevel.INTERMEDIATE),
            ("look", "rora", "Infinitive: kũrora | IPA: /ɾɔɾa/", DifficultyLevel.BEGINNER),
            ("look at", "rora", "Infinitive: kũrora | IPA: /ɾɔɾa/", DifficultyLevel.BEGINNER),
            ("stand", "rũgama", "Infinitive: gũrũgama | IPA: /ɾuɣama/", DifficultyLevel.BEGINNER),
            ("run", "teng'era", "Infinitive: gũteng'era | IPA: /tɛⁿɣeɾa/", DifficultyLevel.BEGINNER),
            ("go", "thiĩ", "Infinitive: gũthiĩ | IPA: /ðiɲ/", DifficultyLevel.BEGINNER),
            ("begin", "thoma", "Infinitive: gũthoma | IPA: /ðɔma/", DifficultyLevel.BEGINNER),
            ("start", "thoma", "Infinitive: gũthoma | IPA: /ðɔma/", DifficultyLevel.BEGINNER),
            ("read", "thoma", "Infinitive: gũthoma | IPA: /ðɔma/", DifficultyLevel.BEGINNER),
            ("leave", "tiga", "Infinitive: gũtiga | IPA: /tiɣa/", DifficultyLevel.BEGINNER),
            ("send", "tũma", "Infinitive: gũtũma | IPA: /tuma/", DifficultyLevel.BEGINNER),
            ("carry", "twara", "Infinitive: gũtwara | IPA: /tʷaɾa/", DifficultyLevel.BEGINNER),
            ("come from", "ũka", "Infinitive: kũũka | IPA: /uːka/", DifficultyLevel.BEGINNER),
            ("know", "ũĩ", "Infinitive: gũũĩ | IPA: /uː e/", DifficultyLevel.BEGINNER),
        ]
        
        # Infinitive forms
        wiktionary_infinitives = [
            ("infinitive of amba", "kwamba", "Infinitive form of the verb 'amba'"),
            ("infinitive of ambata", "kwambata", "Infinitive form of the verb 'ambata'"),
            ("infinitive of ambĩrĩria", "kwambĩrĩria", "Infinitive form of the verb 'ambĩrĩria'"),
            ("infinitive of andĩka", "kwandĩka", "Infinitive form of the verb 'andĩka'"),
            ("infinitive of butha", "kũbutha", "Infinitive form of the verb 'butha'"),
            ("infinitive of enda", "kũenda", "Infinitive form of the verb 'enda'"),
            ("infinitive of enja", "kwenja", "Infinitive form of the verb 'enja'"),
            ("infinitive of geria", "kũgeria", "Infinitive form of the verb 'geria'"),
            ("infinitive of gũra", "kũgũra", "Infinitive form of the verb 'gũra'"),
            ("infinitive of gwata", "kũgwata", "Infinitive form of the verb 'gwata'"),
            ("infinitive of haka", "kũhaka", "Infinitive form of the verb 'haka'"),
            ("infinitive of hitha", "kũhitha", "Infinitive form of the verb 'hitha'"),
            ("infinitive of hota", "kũhota", "Infinitive form of the verb 'hota'"),
            ("infinitive of hoya", "kũhoya", "Infinitive form of the verb 'hoya'"),
            ("infinitive of iga", "kwĩga", "Infinitive form of the verb 'iga'"),
            ("infinitive of igua", "kwĩgua", "Infinitive form of the verb 'igua'"),
            ("infinitive of ĩka", "kwĩka", "Infinitive form of the verb 'ĩka'"),
            ("infinitive of ikara", "kwĩkara", "Infinitive form of the verb 'ikara'"),
            ("infinitive of ina", "kwĩna", "Infinitive form of the verb 'ina'"),
            ("infinitive of ũka", "kũũka", "Infinitive form of the verb 'ũka'"),
            ("infinitive of ĩta", "kwĩta", "Infinitive form of the verb 'ĩta'"),
            ("infinitive of iya", "kwĩya", "Infinitive form of the verb 'iya'"),
            ("infinitive of kena", "gũkena", "Infinitive form of the verb 'kena'"),
            ("infinitive of kira", "gũkira", "Infinitive form of the verb 'kira'"),
            ("infinitive of koma", "gũkoma", "Infinitive form of the verb 'koma'"),
            ("infinitive of kũra", "gũkũra", "Infinitive form of the verb 'kũra'"),
            ("infinitive of kua", "gũkua", "Infinitive form of the verb 'kua'"),
            ("infinitive of kumia", "gũkumia", "Infinitive form of the verb 'kumia'"),
            ("infinitive of menya", "kũmenya", "Infinitive form of the verb 'menya'"),
            ("infinitive of ona", "kũona", "Infinitive form of the verb 'ona'"),
            ("infinitive of oya", "kũoya", "Infinitive form of the verb 'oya'"),
            ("infinitive of rara", "gũrara", "Infinitive form of the verb 'rara'"),
            ("infinitive of rega", "kũrega", "Infinitive form of the verb 'rega'"),
            ("infinitive of rĩa", "kũrĩa", "Infinitive form of the verb 'rĩa'"),
            ("infinitive of rora", "kũrora", "Infinitive form of the verb 'rora'"),
            ("infinitive of rũgama", "gũrũgama", "Infinitive form of the verb 'rũgama'"),
            ("infinitive of teng'era", "gũteng'era", "Infinitive form of the verb 'teng'era'"),
            ("infinitive of thiĩ", "gũthiĩ", "Infinitive form of the verb 'thiĩ'"),
            ("infinitive of thoma", "gũthoma", "Infinitive form of the verb 'thoma'"),
            ("infinitive of tiga", "gũtiga", "Infinitive form of the verb 'tiga'"),
            ("infinitive of tũma", "gũtũma", "Infinitive form of the verb 'tũma'"),
            ("infinitive of twara", "gũtwara", "Infinitive form of the verb 'twara'"),
            ("infinitive of ũĩ", "gũũĩ", "Infinitive form of the verb 'ũĩ'"),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process main verbs
        for english, kikuyu, context, difficulty in wiktionary_verbs:
            # Check if this contribution already exists
            existing = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if existing:
                skipped_count += 1
                continue
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Comprehensive verb from Wiktionary with IPA pronunciation and morphological analysis. Academic dictionary source with linguistic precision.",
                quality_score=4.8,
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()
            
            # Associate with category
            contribution.categories.append(categories["Wiktionary Verbs"])
            
            contribution_count += 1
        
        # Process infinitives
        for english, kikuyu, context in wiktionary_infinitives:
            # Check if this contribution already exists
            existing = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if existing:
                skipped_count += 1
                continue
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,
                language="kikuyu",
                difficulty_level=DifficultyLevel.INTERMEDIATE,
                context_notes=context,
                cultural_notes="Infinitive verb form from Wiktionary showing morphological structure of Kikuyu verbs.",
                quality_score=4.7,
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()
            
            # Associate with category
            contribution.categories.append(categories["Verb Infinitives"])
            
            contribution_count += 1
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Wiktionary verb and infinitive contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print("All Wiktionary verb data marked as approved for immediate use")
        
        # Print content analysis
        print("\nWiktionary Verbs Literal Data Added:")
        print("- Essential verbs with IPA pronunciations")
        print("- Infinitive forms with morphological structure")
        print("- Academic dictionary quality")
        print("- Multiple meanings per verb")
        print("- Phonetic transcriptions for pronunciation")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Wiktionary Verbs", "Verb Infinitives"]:
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
    print("Seeding database with Wiktionary verbs literal data...")
    print("Source: Hardcoded literal extractions from Wiktionary")
    try:
        create_wiktionary_verbs_literal_seed()
        print("Wiktionary verbs literal seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)