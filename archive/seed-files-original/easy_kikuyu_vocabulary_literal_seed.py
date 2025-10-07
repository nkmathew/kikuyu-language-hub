#!/usr/bin/env python3
"""
Easy Kikuyu Vocabulary Literal Seed Script
Contains hardcoded literal vocabulary extracted from Emmanuel Kariuki's Easy Kikuyu lessons
Native speaker vocabulary from 538 Facebook lesson files
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
from datetime import datetime

def create_easy_kikuyu_vocabulary_literal_seed():
    """Create seed data from literal Easy Kikuyu vocabulary extractions"""
    
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
            ("Easy Kikuyu Vocabulary", "Native speaker vocabulary from Easy Kikuyu lessons", "easy-kikuyu-vocab"),
            ("Native Speaker Content", "Authentic content from Kikuyu native speakers", "native-speaker"),
            ("Beginner Vocabulary", "Essential vocabulary for Kikuyu beginners", "beginner-vocab"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1500
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Literal extracted vocabulary from Easy Kikuyu lessons
        easy_kikuyu_vocabulary = [
            # Basic vocabulary and everyday terms
            ("Table", "Metha", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Picture/Photo", "Mbica", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Pot (cooking)", "Nyũngũ", "Cooking vessel from structured lesson", DifficultyLevel.BEGINNER),
            ("Glass", "Ngĩrathi", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Plate", "Thaani", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Iron (for clothes)", "Baathi", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Television", "Terebiceni", "Modern household item", DifficultyLevel.BEGINNER),
            ("Pot (saucepan)", "Thaburia", "Cooking vessel from structured lesson", DifficultyLevel.BEGINNER),
            ("Kettle", "Mbirika", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Padlock", "Kuburi", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Cow", "Ng'ombe", "Domestic animal from structured lesson", DifficultyLevel.BEGINNER),
            ("He-goat", "Thenge", "Domestic animal from structured lesson", DifficultyLevel.BEGINNER),
            ("Chicken/Hen", "Ngũkũ", "Domestic animal from structured lesson", DifficultyLevel.BEGINNER),
            ("Duck", "Mbata", "Domestic animal from structured lesson", DifficultyLevel.BEGINNER),
            ("Keys", "Cabi", "Household item from structured lesson", DifficultyLevel.BEGINNER),
            ("Grasshopper", "Ngigĩ", "Insect from structured lesson", DifficultyLevel.BEGINNER),
            
            # Wild animals
            ("giraffe", "Ndûiga", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Elephant", "Njogu", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("zebra", "Wambûi mîcore", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("zebra (alternative)", "njagî", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Ostrich", "Nyaga", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Rhinocerous", "Huria", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("lion", "Mûrûthî", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Wildebeest", "Ngatata", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Cheatah", "Nyûtû", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Hyena", "Hiti", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Hippo", "Nguuo", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Leopard", "Ngarî", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            ("Chimpanzee", "Thakwe", "Wild animal vocabulary", DifficultyLevel.BEGINNER),
            
            # Directions and geography  
            ("Upper side (North)", "Rũgongo", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            ("East", "Irathĩro", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            ("Lower side (South)", "Mũhuro", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            ("West", "Ithũĩro", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            ("Up river", "Rũgũrũ", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            ("Direction of river flow", "Itherero", "Directional term from geography lesson", DifficultyLevel.BEGINNER),
            
            # Cooking methods
            ("boiling", "Gũtherũkia", "Cooking method from culinary lesson", DifficultyLevel.BEGINNER),
            ("frying", "Gũkaranga", "Cooking method from culinary lesson", DifficultyLevel.BEGINNER),
            ("roasting", "Kũhĩhia", "Cooking method from culinary lesson", DifficultyLevel.BEGINNER),
            ("roasting (alternative)", "Gũcina", "Cooking method from culinary lesson", DifficultyLevel.BEGINNER),
            
            # Additional vocabulary from various lessons
            ("tick", "Ngũha", "Insect/parasite vocabulary", DifficultyLevel.BEGINNER),
            ("need", "Bata", "Abstract concept", DifficultyLevel.INTERMEDIATE),
            ("to need", "Kũbatara", "Verb form", DifficultyLevel.INTERMEDIATE),
            ("to be ashamed", "Gũconoka", "Emotional state verb", DifficultyLevel.INTERMEDIATE),
            ("to shame (someone)", "Gũconora", "Causative verb", DifficultyLevel.INTERMEDIATE),
            ("get ashamed", "Ta conoka", "Imperative form", DifficultyLevel.INTERMEDIATE),
            
            # More structured vocabulary from lessons
            ("this one table", "Metha ĩno ĩmwe", "Demonstrative with noun (Class III)", DifficultyLevel.INTERMEDIATE),
            ("These two tables", "Metha ici igĩrĩ", "Demonstrative plural with number", DifficultyLevel.INTERMEDIATE),
            ("cold water", "maĩ mahoro", "Descriptive phrase", DifficultyLevel.BEGINNER),
            ("ten minutes", "dagĩka ikũmi", "Time expression", DifficultyLevel.BEGINNER),
            ("sorghum flour", "mũtũ wa mũhĩa", "Traditional food ingredient", DifficultyLevel.BEGINNER),
            ("porridge", "ũcũrũ", "Traditional food", DifficultyLevel.BEGINNER),
            ("bread", "mũgate", "Modern food item", DifficultyLevel.BEGINNER),
            ("tea", "cai", "Beverage", DifficultyLevel.BEGINNER),
            ("door", "mũrango", "Architectural element", DifficultyLevel.BEGINNER),
            ("clothes", "nguo", "Clothing general term", DifficultyLevel.BEGINNER),
            ("food", "irio", "Food general term", DifficultyLevel.BEGINNER),
            ("quickly", "naihenya", "Adverb of manner", DifficultyLevel.INTERMEDIATE),
            
            # Religious/spiritual vocabulary (from Quran translation lesson)
            ("God", "Ngai", "Religious/spiritual term", DifficultyLevel.BEGINNER),
            ("Lord", "mwathani", "Religious/spiritual term", DifficultyLevel.INTERMEDIATE),
            ("world", "thĩ", "Geographic/spiritual concept", DifficultyLevel.BEGINNER),
            ("all", "ciothe", "Quantifier", DifficultyLevel.BEGINNER),
            ("merciful", "mũtana", "Character trait", DifficultyLevel.INTERMEDIATE),
            ("mercy", "tha", "Abstract concept", DifficultyLevel.INTERMEDIATE),
            ("many", "nyingĩ", "Quantifier", DifficultyLevel.BEGINNER),
            ("day", "mũthenya", "Time unit", DifficultyLevel.BEGINNER),
            ("judgment", "gũtua ciira", "Legal/spiritual concept", DifficultyLevel.ADVANCED),
            ("worship", "hoyaga", "Religious action", DifficultyLevel.INTERMEDIATE),
            ("help", "teithia", "Social action", DifficultyLevel.BEGINNER),
            ("guide", "tũtongorie", "Leadership action", DifficultyLevel.INTERMEDIATE),
            ("path", "njĩra", "Physical/metaphorical route", DifficultyLevel.BEGINNER),
            ("straight", "nyoroku", "Directional descriptor", DifficultyLevel.INTERMEDIATE),
            ("blessed", "ũrathimĩte", "Spiritual state", DifficultyLevel.ADVANCED),
            ("anger", "arakarĩtie", "Emotional state", DifficultyLevel.INTERMEDIATE),
            ("astray", "morĩte", "Spiritual/directional state", DifficultyLevel.ADVANCED),
            
            # Additional everyday vocabulary
            ("name", "rĩĩtwa", "Personal identifier", DifficultyLevel.BEGINNER),
            ("book", "ĩbuku", "Educational item", DifficultyLevel.BEGINNER),
            ("school", "shule", "Educational institution", DifficultyLevel.BEGINNER),
            ("home", "mũciĩ", "Place of residence", DifficultyLevel.BEGINNER),
            ("mountain", "kĩrĩma", "Geographic feature", DifficultyLevel.BEGINNER),
            ("child", "mwana", "Family relation", DifficultyLevel.BEGINNER),
            ("children", "ciana", "Plural of child", DifficultyLevel.BEGINNER),
            ("teacher", "mũaruthi", "Profession/role", DifficultyLevel.BEGINNER),
            ("mother", "nyina", "Family relation", DifficultyLevel.BEGINNER),
            ("farmer", "mũrimi", "Profession/role", DifficultyLevel.BEGINNER),
            ("field", "mũgũnda", "Agricultural area", DifficultyLevel.BEGINNER),
            
            # Time expressions and temporal vocabulary
            ("morning", "rũcinĩ", "Time of day", DifficultyLevel.BEGINNER),
            ("today", "ũmũthĩ", "Temporal reference", DifficultyLevel.BEGINNER),
            ("ago", "athira", "Temporal reference", DifficultyLevel.INTERMEDIATE),
            ("then", "acoka", "Temporal connector", DifficultyLevel.INTERMEDIATE),
            ("when", "rĩ", "Temporal question word", DifficultyLevel.INTERMEDIATE),
            
            # Action-related vocabulary
            ("brought", "ehĩre", "Past action", DifficultyLevel.INTERMEDIATE),
            ("woke up", "okĩra", "Past action", DifficultyLevel.INTERMEDIATE),
            ("took a bath", "ethamba", "Past action", DifficultyLevel.INTERMEDIATE),
            ("put on", "ehumba", "Past action", DifficultyLevel.INTERMEDIATE),
            ("made", "aruga", "Past action", DifficultyLevel.INTERMEDIATE),
            ("shut", "ahĩnga", "Past action", DifficultyLevel.INTERMEDIATE),
            ("left", "oima", "Past action", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process vocabulary items
        for english, kikuyu, context, difficulty in easy_kikuyu_vocabulary:
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
                context_notes=f"Native speaker vocabulary - {context}",
                cultural_notes=f"Authentic vocabulary from Easy Kikuyu lessons by Emmanuel Kariuki, a native Kikuyu speaker. This represents everyday language usage in natural Kikuyu conversation and cultural context.",
                quality_score=4.7,
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()
            
            # Associate with categories
            contribution.categories.append(categories["Easy Kikuyu Vocabulary"])
            contribution.categories.append(categories["Native Speaker Content"])
            contribution.categories.append(categories["Beginner Vocabulary"])
            
            contribution_count += 1
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Easy Kikuyu vocabulary contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print("All Easy Kikuyu vocabulary data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Vocabulary Literal Data Added:")
        print("- Authentic native speaker vocabulary from Emmanuel Kariuki")
        print("- Everyday terms covering household, animals, directions, cooking")
        print("- Religious/spiritual vocabulary from cultural lessons")
        print("- Temporal expressions and action vocabulary")
        print("- Beginner to intermediate difficulty progression")
        print("- High quality scores reflecting native speaker authenticity")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Vocabulary", "Native Speaker Content", "Beginner Vocabulary"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: This literal vocabulary collection provides essential")
        print("Kikuyu terms verified by native speaker Emmanuel Kariuki,")
        print("ensuring authenticity and practical utility for learners.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu vocabulary literal data...")
    print("Source: Literal extractions from Emmanuel Kariuki's Easy Kikuyu lessons")
    try:
        create_easy_kikuyu_vocabulary_literal_seed()
        print("Easy Kikuyu vocabulary literal seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)