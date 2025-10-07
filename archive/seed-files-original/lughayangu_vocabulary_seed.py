#!/usr/bin/env python3
"""
Seed script for Kikuyu vocabulary from lughayangu.com
Includes words with contextual examples and usage sentences
Focus on verbs, nouns, adjectives, and practical terms with real-world applications
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


def create_lughayangu_vocabulary_seed():
    """Create seed data from lughayangu.com vocabulary with contextual examples"""
    
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
            ("Verbs & Actions", "Action words and their practical applications", "verbs-actions"),
            ("Body Parts & Health", "Anatomical terms and health-related vocabulary", "body-health"),
            ("Directions & Geography", "Directional terms and geographical vocabulary", "directions-geography"),
            ("Legal & Civic Terms", "Legal system and civic vocabulary", "legal-civic"),
            ("Infrastructure & Buildings", "Built environment and infrastructure terms", "infrastructure-buildings"),
            ("Nature & Environment", "Natural world and environmental terms", "nature-environment"),
            ("Practical Objects", "Everyday items and tools", "practical-objects"),
            ("Descriptive Terms", "Adjectives and descriptive vocabulary", "descriptive-terms"),
            ("Professional & Work", "Occupational and work-related terms", "professional-work"),
            ("Medical & Diseases", "Medical conditions and health terminology", "medical-diseases"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1000  # Put after existing categories
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Vocabulary from lughayangu.com with contextual examples (preserving all accented characters)
        lughayangu_vocabulary = [
            # Directional and spatial terms
            ("right", "úrío", "Directional term - right side", "Directions & Geography", DifficultyLevel.BEGINNER),
            ("north", "gathigathini", "Cardinal direction - north", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("south", "gúthini", "Cardinal direction - south", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("inside", "thíinií", "Spatial term - interior location", "Directions & Geography", DifficultyLevel.BEGINNER),
            ("slope", "múikúrúko", "Geographical feature - inclined surface", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("bend", "kona", "Curved section, especially of road", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("bridge", "ndaraca", "Structure spanning water or gap", "Infrastructure & Buildings", DifficultyLevel.INTERMEDIATE),
            ("road", "barabara", "Paved pathway for vehicles", "Infrastructure & Buildings", DifficultyLevel.BEGINNER),
            ("building", "mwako", "Constructed structure", "Infrastructure & Buildings", DifficultyLevel.INTERMEDIATE),
            
            # Action verbs with practical contexts
            ("hit", "gútha", "To strike forcefully", "Verbs & Actions", DifficultyLevel.BEGINNER),
            ("please", "kenia", "To satisfy or make happy", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("lay", "rekia", "To place down, especially eggs", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("model", "úmba", "To shape or form", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("warm", "raria", "To heat gently", "Verbs & Actions", DifficultyLevel.BEGINNER),
            ("dilute", "twekia", "To thin with liquid", "Verbs & Actions", DifficultyLevel.ADVANCED),
            ("march", "thoitha", "To walk in formation", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("accept", "ítíkíra", "To agree to receive", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("postpone", "tíria", "To delay or defer", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("run", "teng'era", "To move quickly on foot", "Verbs & Actions", DifficultyLevel.BEGINNER),
            ("tilt", "inamia", "To lean or angle", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("kiss", "mumunya", "To touch with lips affectionately", "Verbs & Actions", DifficultyLevel.BEGINNER),
            ("arrest", "gúikia ngono", "To take into custody", "Legal & Civic Terms", DifficultyLevel.ADVANCED),
            
            # Objects and tools
            ("bulb", "ngirobu", "Electric lighting device", "Practical Objects", DifficultyLevel.INTERMEDIATE),
            ("noose", "kíana", "Loop for tightening", "Practical Objects", DifficultyLevel.ADVANCED),
            ("suit", "thuti", "Formal clothing ensemble", "Practical Objects", DifficultyLevel.INTERMEDIATE),
            ("bell", "ngengere", "Sound-making device", "Practical Objects", DifficultyLevel.INTERMEDIATE),
            ("bullet", "rithathi", "Projectile ammunition", "Practical Objects", DifficultyLevel.ADVANCED),
            
            # Body parts and health
            ("molar", "ikamburu", "Back grinding tooth", "Body Parts & Health", DifficultyLevel.INTERMEDIATE),
            ("smallpox", "mútúng'ú", "Infectious disease", "Medical & Diseases", DifficultyLevel.ADVANCED),
            ("mumps", "múngai", "Viral infection affecting glands", "Medical & Diseases", DifficultyLevel.ADVANCED),
            ("leprosy", "mangú", "Chronic infectious disease", "Medical & Diseases", DifficultyLevel.ADVANCED),
            
            # Nature and environment
            ("branch", "rúhonge", "Tree limb", "Nature & Environment", DifficultyLevel.BEGINNER),
            
            # Legal and civic terms
            ("case", "ciira", "Legal proceeding", "Legal & Civic Terms", DifficultyLevel.INTERMEDIATE),
            ("prison", "njera", "Detention facility", "Legal & Civic Terms", DifficultyLevel.INTERMEDIATE),
            
            # Professional terms
            ("driver", "dereba", "Vehicle operator", "Professional & Work", DifficultyLevel.BEGINNER),
            
            # Descriptive terms
            ("cheap", "raithi", "Low cost or inexpensive", "Descriptive Terms", DifficultyLevel.BEGINNER),
            ("tall", "-raihu", "Having great height", "Descriptive Terms", DifficultyLevel.BEGINNER),
            ("round", "-thiúrúrí", "Circular in shape", "Descriptive Terms", DifficultyLevel.BEGINNER),
            ("blemish", "kameni", "Mark or flaw", "Descriptive Terms", DifficultyLevel.INTERMEDIATE),
            ("spot", "kameni", "Small mark or stain", "Descriptive Terms", DifficultyLevel.INTERMEDIATE),
            
            # Time and duration
            ("forever and ever", "míndí na míndi", "For all eternity", "Descriptive Terms", DifficultyLevel.ADVANCED),
        ]
        
        # Contextual example phrases extracted from the file (preserving all accented characters)
        contextual_phrases = [
            # Direction examples
            ("lift up your right hand!", "oya guoko gwaku kwa úrío na igúrú", "Command with directional reference", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("hit the rock again", "gútha ihiga ríngí", "Action command with repetition", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("everyone wants to please their boss", "múndú wothe endaga gúkenia múmwandíki", "Workplace relationship dynamics", "Professional & Work", DifficultyLevel.ADVANCED),
            ("a hen will lay an egg", "ngúkú nííkúrekia itumbí", "Natural animal behavior", "Nature & Environment", DifficultyLevel.INTERMEDIATE),
            ("model a pot", "úmba nyúngú", "Craft instruction", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("warm the baby's drinking water", "raria maí ma kúhe mwana", "Childcare instruction", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("dilute the paint before applying it to the wall", "twekia rangi úcio mbere ya úhake rúthingo", "Home improvement instruction", "Verbs & Actions", DifficultyLevel.ADVANCED),
            ("we watched the police march all day long", "twíroreire bolrithi magíthoitha múthenya wothe", "Past observation", "Verbs & Actions", DifficultyLevel.ADVANCED),
            ("the bulb gives a lot of light", "útheri wa ngirobu ní múingí", "Technical description", "Practical Objects", DifficultyLevel.INTERMEDIATE),
            ("this slope is very steep", "múikúrúko úyú ní múinamu múno", "Geographical description", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("that bend on the road has a signage", "kona íyo ya bara ína kíbaú", "Traffic observation", "Directions & Geography", DifficultyLevel.INTERMEDIATE),
            ("if you accept to go you will be paid", "wetíkíra gúthi;i níúkúríhwo", "Conditional agreement", "Verbs & Actions", DifficultyLevel.ADVANCED),
            ("the chief has postponed the meeting", "cibú níatíria múcemanio", "Administrative announcement", "Professional & Work", DifficultyLevel.ADVANCED),
            ("run to save yourself", "teng'era wíthare", "Emergency instruction", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("tilt the bottle to pour water", "inamia cuba maí maitíke", "Practical instruction", "Verbs & Actions", DifficultyLevel.INTERMEDIATE),
            ("kiss the cheek", "mumunya ikai", "Affectionate gesture", "Verbs & Actions", DifficultyLevel.BEGINNER),
            ("police will arrest any lawbreaker", "borithi nímegúikia ngono muni watho o wothe", "Legal warning", "Legal & Civic Terms", DifficultyLevel.ADVANCED),
            ("the noose is tight on the pole", "kíana kíu níkírúmu gítingíní", "Technical description", "Practical Objects", DifficultyLevel.ADVANCED),
            ("the politician is in an expensive suit", "muteti ekíríte thuti ya goro", "Social observation", "Professional & Work", DifficultyLevel.INTERMEDIATE),
            ("his molar has a cavity", "ikamburu ríake níríenjeku", "Medical description", "Body Parts & Health", DifficultyLevel.INTERMEDIATE),
            ("a fig tree branch has broken", "rúhonge rwa múkúyú ní reaunika", "Natural observation", "Nature & Environment", DifficultyLevel.INTERMEDIATE),
            ("Christians believe they will be with Jesus forever and ever", "akristú metíkítie gúgatúrania na Jesú tene na tene", "Religious statement", "Descriptive Terms", DifficultyLevel.ADVANCED),
            ("the driver has been fired for misconduct", "dereba níabutwo wíra níúndú wa mahítia", "Employment situation", "Professional & Work", DifficultyLevel.ADVANCED),
            ("the government has bought two million bullets", "thirikari níígúríte rithathi mirioni i'girí", "Government procurement", "Legal & Civic Terms", DifficultyLevel.ADVANCED),
            ("his case will be determined tomorrow", "ciira wake úgatuo rúciú", "Legal proceeding", "Legal & Civic Terms", DifficultyLevel.INTERMEDIATE),
            ("he has been incarcerated in prison for ten years", "ohwo njera míaka ikúmi", "Legal consequence", "Legal & Civic Terms", DifficultyLevel.ADVANCED),
            ("she has bought white clothe without blemish", "agúra nguo njerú ítarí kameni", "Shopping description", "Descriptive Terms", DifficultyLevel.INTERMEDIATE),
            ("the bell has rung", "ngengere níyahúrwo", "Past action description", "Practical Objects", DifficultyLevel.INTERMEDIATE),
            ("there is peace in the north of that country", "kwína thayú gathigathini ka búrúri úcio", "Geographic and political statement", "Directions & Geography", DifficultyLevel.ADVANCED),
            ("second hand clothes are cheap", "nguo cia mútumba cií raithi", "Economic observation", "Descriptive Terms", DifficultyLevel.INTERMEDIATE),
            ("that young man is tall", "mwanake úcio ní múraihu", "Physical description", "Descriptive Terms", DifficultyLevel.BEGINNER),
            ("that building is costly to put up", "mwako úcio wí goro kúwaka", "Construction cost assessment", "Infrastructure & Buildings", DifficultyLevel.INTERMEDIATE),
            ("the road is being widened", "barabara ní-íraramio", "Infrastructure development", "Infrastructure & Buildings", DifficultyLevel.ADVANCED),
            ("let's cross the bridge when we reach it", "reke túringe ndaraca twamíkinyíra", "Idiomatic expression about timing", "Directions & Geography", DifficultyLevel.ADVANCED),
            ("the wind is blowing from the south", "rúhuho rúrauma gúthini", "Weather description", "Nature & Environment", DifficultyLevel.INTERMEDIATE),
            ("the house is beautiful on the inside", "nyumba ní thaka thíinií", "Interior description", "Infrastructure & Buildings", DifficultyLevel.INTERMEDIATE),
            ("smallpox was completely eradicated", "múrimú wa mútúng'ú niwahukire kaimana", "Medical history", "Medical & Diseases", DifficultyLevel.ADVANCED),
            ("mumps is prevalent among children", "múngai ní únyitaga ciana kaingí", "Medical epidemiology", "Medical & Diseases", DifficultyLevel.ADVANCED),
            ("leprosy is contagious", "mangú ní magwatanagio", "Medical knowledge", "Medical & Diseases", DifficultyLevel.ADVANCED),
            ("vehicle's wheels are round", "magúrú ma ngari ní mathíúrúrí", "Technical description", "Practical Objects", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process all vocabulary and phrases
        all_data = lughayangu_vocabulary + contextual_phrases
        
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
                    # Default to Verbs & Actions if category not found
                    category = categories["Verbs & Actions"]
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=context,
                cultural_notes="Practical vocabulary from lughayangu.com with contextual examples demonstrating real-world usage. Includes verbs, nouns, adjectives, and complete phrases showing natural language patterns.",
                quality_score=4.5,  # High quality with contextual examples
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with category
            contribution.categories.append(category)
            
            contribution_count += 1
        
        # Create sub-translations for complex phrases and compound words (preserving accented characters)
        complex_phrase_patterns = [
            # Compound directional phrase
            ("lift up your right hand!", "oya guoko gwaku kwa úrío na igúrú", [
                ("oya", "oya", 0, "Imperative verb - lift/raise"),
                ("guoko", "guoko", 1, "Hand/arm"),
                ("gwaku", "gwaku", 2, "Your (possessive)"),
                ("kwa úrío", "kwa úrío", 3, "To the right"),
                ("na igúrú", "na igúrú", 4, "And upward")
            ]),
            # Medical compound
            ("mumps is prevalent among children", "múngai ní únyitaga ciana kaingí", [
                ("múngai", "múngai", 0, "Mumps (disease)"),
                ("ní", "ní", 1, "Is (copula)"),
                ("únyitaga", "únyitaga", 2, "Affects/catches"),
                ("ciana", "ciana", 3, "Children"),
                ("kaingí", "kaingí", 4, "Often/frequently")
            ]),
            # Infrastructure phrase
            ("the road is being widened", "barabara ní-íraramio", [
                ("barabara", "barabara", 0, "Road/highway"),
                ("ní-íraramio", "ní-íraramio", 1, "Is being widened (passive progressive)")
            ]),
            # Idiomatic expression
            ("forever and ever", "míndí na míndi", [
                ("míndí", "míndí", 0, "Forever/eternity"),
                ("na", "na", 1, "And (conjunction)"),
                ("míndi", "míndi", 2, "Forever/eternity (repeated for emphasis)")
            ]),
            # Government/legal phrase
            ("the government has bought two million bullets", "thirikari níígúríte rithathi mirioni i'girí", [
                ("thirikari", "thirikari", 0, "Government"),
                ("níígúríte", "níígúríte", 1, "Has bought (perfect tense)"),
                ("rithathi", "rithathi", 2, "Bullets"),
                ("mirioni", "mirioni", 3, "Million"),
                ("i'girí", "i'girí", 4, "Two")
            ])
        ]
        
        for source, target, sub_parts in complex_phrase_patterns:
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
        
        print(f"Successfully created {contribution_count} new lughayangu.com vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added detailed analysis for {len(complex_phrase_patterns)} complex phrases")
        print("All vocabulary marked as approved for immediate use")
        
        # Print content analysis
        print("\nLughayangu.com Vocabulary Added:")
        print("- Practical action verbs with contextual usage")
        print("- Directional and geographical terminology")
        print("- Body parts and medical conditions")
        print("- Legal and civic vocabulary")
        print("- Infrastructure and building terms")
        print("- Professional and work-related terms")
        print("- Descriptive adjectives and qualifiers")
        print("- Complete contextual phrases and sentences")
        print("- Real-world application examples")
        
        # Print category summary
        print("\nNew categories:")
        new_categories = ["Verbs & Actions", "Body Parts & Health", "Directions & Geography", 
                         "Legal & Civic Terms", "Infrastructure & Buildings", "Nature & Environment",
                         "Practical Objects", "Descriptive Terms", "Professional & Work", "Medical & Diseases"]
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
        
        print("\nNote: This collection emphasizes practical vocabulary with")
        print("contextual examples, demonstrating real-world usage patterns")
        print("and natural language applications from lughayangu.com.")


if __name__ == "__main__":
    print("Seeding database with lughayangu.com vocabulary...")
    print("Source: lughayangu.com - Kikuyu words with contextual examples")
    try:
        create_lughayangu_vocabulary_seed()
        print("Lughayangu vocabulary seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)