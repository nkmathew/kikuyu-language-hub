#!/usr/bin/env python3
"""
Seed script for Kikuyu proverbs from African Manners collection
Selected high-value proverbs with cultural significance
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


def create_proverbs_collection_seed():
    """Create seed data for selected Kikuyu proverbs from the comprehensive collection"""
    
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
        
        # Get or create Proverbs category
        proverbs_category = db.query(Category).filter(Category.name == "Proverbs").first()
        if not proverbs_category:
            proverbs_category = Category(
                name="Proverbs",
                description="Traditional Kikuyu proverbs and sayings",
                slug="proverbs",
                sort_order=700
            )
            db.add(proverbs_category)
            db.commit()
        
        # Get Traditional Wisdom category
        wisdom_category = db.query(Category).filter(Category.name == "Traditional Wisdom").first()
        if not wisdom_category:
            wisdom_category = Category(
                name="Traditional Wisdom",
                description="Cultural sayings and traditional knowledge",
                slug="traditional-wisdom",
                sort_order=701
            )
            db.add(wisdom_category)
            db.commit()
        
        # Selected proverbs from the African Manners collection
        # Choosing culturally significant and linguistically valuable examples
        selected_proverbs = [
            # Life and wisdom
            ("home affairs must not go into the open", "cia mucii itiumaga ndira", "Private matters should remain private", DifficultyLevel.ADVANCED),
            ("the widow's sons have no tears", "ciana cia ndigwa itiri maithori", "Hardship teaches early resilience", DifficultyLevel.ADVANCED),
            ("bought things do not fill the granary", "cia thuguri itiyuraga ikumbi", "Self-reliance is essential for prosperity", DifficultyLevel.ADVANCED),
            ("a long lawsuit breeds poverty", "cira munene ni ukia", "Prolonged conflicts cause financial ruin", DifficultyLevel.ADVANCED),
            ("every case begins from the stomach", "cira wothe wambagiririo na nda", "Food and sustenance are fundamental to all matters", DifficultyLevel.ADVANCED),
            
            # Work and effort
            ("travelling is learning", "guceera ni kuhiga", "Experience through travel brings knowledge", DifficultyLevel.INTERMEDIATE),
            ("to get the warmth of fire one must stir the embers", "guota mwaki ni gucera", "Effort is required to gain benefits", DifficultyLevel.ADVANCED),
            ("if you help yourself you will be helped", "guteithagio witeithitie", "Self-reliance attracts assistance", DifficultyLevel.INTERMEDIATE),
            ("riches are found in cultivating together", "indo ni kurimithanio", "Cooperation leads to prosperity", DifficultyLevel.ADVANCED),
            ("many people together lift up the ndiri", "kamuingi koyaga ndiri", "Unity accomplishes great tasks", DifficultyLevel.ADVANCED),
            
            # Character and behavior
            ("virtue is better than riches", "guthinga kurugite gutonga", "Moral character surpasses material wealth", DifficultyLevel.ADVANCED),
            ("virtue is power", "guthinga kikuo kihoto", "Good character is true strength", DifficultyLevel.ADVANCED),
            ("to keep one's tongue is worthy of praise", "gukira kuri ngatho", "Discretion in speech is valuable", DifficultyLevel.ADVANCED),
            ("not to talk is to hate", "gukira ni guthurana", "Silence can indicate dislike", DifficultyLevel.INTERMEDIATE),
            ("cutting by the tongue is different from cutting by the knife", "gutema na kanua ti gutema na rihiu", "Words wound differently than actions", DifficultyLevel.ADVANCED),
            
            # Family and relationships
            ("like father like son", "kaana ka ngari gakunyaga ta nyina", "Children resemble their parents", DifficultyLevel.INTERMEDIATE),
            ("one finger does not kill a louse", "kaara kamwe gatingiyuragira ndaa", "Cooperation is necessary for success", DifficultyLevel.INTERMEDIATE),
            ("nobody is born wise", "gutiri uciaragwo ari mugi", "Wisdom is acquired through experience", DifficultyLevel.INTERMEDIATE),
            ("there is no man that cannot become an orphan", "gutiri mundu utangutuika wa ndigwa", "Everyone faces loss and hardship", DifficultyLevel.ADVANCED),
            ("nobody can see his own goodness", "gutiri mundu wonaga wega wake, no kuonwo wonagwo", "Self-awareness has limitations", DifficultyLevel.ADVANCED),
            
            # Time and change
            ("no day dawns like another", "gutiri muthenya ukiaga ta ungi", "Each day brings new opportunities", DifficultyLevel.INTERMEDIATE),
            ("one ages every night one lives", "gutiri gukura na kurara keri", "Time passes continuously", DifficultyLevel.ADVANCED),
            ("there is no thing which does not cause another to exist", "gutiri gitatuirie kingi", "Everything is interconnected", DifficultyLevel.ADVANCED),
            ("there is nothing without a cause", "gutiri undu utari kihumo", "Everything has a reason or origin", DifficultyLevel.INTERMEDIATE),
            ("no evil, but only the good will last", "gutiri uru utuuraga, no wega utuuraga", "Good endures while evil passes", DifficultyLevel.ADVANCED),
            
            # Nature and animals
            ("the hyena does not eat its baby", "hiti ndiriaga mwana, na mui uria iri ngoroku", "Even the most savage protect their young", DifficultyLevel.ADVANCED),
            ("when hyenas go away jackals rejoice", "hiti ciathii mbwe ciegangara", "Opportunity comes when obstacles are removed", DifficultyLevel.ADVANCED),
            ("the pot calling the kettle black", "hiti itaga iria ingi ya mutiri", "Criticizing others for faults you share", DifficultyLevel.ADVANCED),
            ("vultures arrive at the place where the goat is slaughtered", "hungu ireraga haria mburi irathinjirwo", "Opportunists gather where there is gain", DifficultyLevel.ADVANCED),
            
            # Practical wisdom
            ("there is no bow without its meat", "gutiri uta utari nyama", "Preparation and effort bring results", DifficultyLevel.INTERMEDIATE),
            ("good ware makes a quick market", "ikururio ti noru", "Quality sells itself", DifficultyLevel.INTERMEDIATE),
            ("where there is a will there is a way", "ireragira ruku-ini na ikaya kuigana", "Determination overcomes obstacles", DifficultyLevel.ADVANCED),
            ("a drowning man will catch at a straw", "iri kuhuma ndiri muti itangigwatirira", "Desperation accepts any help", DifficultyLevel.ADVANCED),
            ("misfortunes come by forties", "iri kuruga ni iguita, iguitirira ni nguu", "Problems often come in multiples", DifficultyLevel.ADVANCED),
            
            # Traditional customs
            ("circumcision is a hard appointment", "giathi kiumu no kia murokero", "Important rituals require courage", DifficultyLevel.ADVANCED),
            ("to wait is not to tremble", "gieterero ti kiinaino", "Patience differs from fear", DifficultyLevel.ADVANCED),
            ("a piece of land is not a little thing", "gicigo kia mugunda gitinyihaga", "Land ownership has great value", DifficultyLevel.ADVANCED),
            ("you cannot make an appointment with death", "gikuu gitiraragirio", "Death comes unexpectedly", DifficultyLevel.ADVANCED),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, explanation, difficulty in selected_proverbs:
            # Check if this contribution already exists to avoid duplicates
            existing = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if existing:
                skipped_count += 1
                continue  # Skip duplicates silently
            
            # Create contribution
            contribution = Contribution(
                source_text=english,
                target_text=kikuyu,
                status=ContributionStatus.APPROVED,  # Pre-approved seed data
                language="kikuyu",
                difficulty_level=difficulty,
                context_notes=explanation,
                cultural_notes="Traditional Kikuyu proverb from the comprehensive African Manners collection of 1000+ proverbs. These represent centuries of accumulated wisdom and cultural values.",
                quality_score=4.9,  # Highest quality - traditional cultural heritage
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with both categories
            contribution.categories.append(proverbs_category)
            contribution.categories.append(wisdom_category)
            
            contribution_count += 1
        
        # Create some sub-translations for particularly complex proverbs
        complex_proverb_patterns = [
            # Complex proverb analysis
            ("every case begins from the stomach", "cira wothe wambagiririo na nda", [
                ("case/matter", "cira", 0, "Legal or social matter requiring resolution"),
                ("all", "wothe", 1, "Universal quantifier - every single one"),
                ("begins", "wambagiririo", 2, "Passive verb - is started/initiated"),
                ("from stomach", "na nda", 3, "Source/origin - from the belly/hunger")
            ]),
            ("to get the warmth of fire one must stir the embers", "guota mwaki ni gucera", [
                ("to get warmth", "guota", 0, "Infinitive - to warm oneself"),
                ("fire", "mwaki", 1, "Source of heat and light"),
                ("it is", "ni", 2, "Copula - linking verb"),
                ("to stir", "gucera", 3, "Infinitive - to stir up/activate")
            ]),
            ("virtue is better than riches", "guthinga kurugite gutonga", [
                ("virtue/goodness", "guthinga", 0, "Moral character and righteousness"),
                ("surpasses", "kurugite", 1, "Comparative verb - is better than"),
                ("wealth", "gutonga", 2, "Material riches and prosperity")
            ])
        ]
        
        for source, target, sub_parts in complex_proverb_patterns:
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
        
        print(f"Successfully created {contribution_count} new proverb contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added detailed analysis for {len(complex_proverb_patterns)} complex proverbs")
        print("All proverbs marked as approved for immediate use")
        
        # Print content analysis
        print("\nTraditional Proverbs Collection Added:")
        print("- Life wisdom and practical guidance")
        print("- Work ethics and cooperation principles") 
        print("- Character development and moral teachings")
        print("- Family relationships and social dynamics")
        print("- Time, change, and life philosophy")
        print("- Nature observations and animal behavior")
        print("- Traditional customs and cultural practices")
        print("- Practical wisdom for daily living")
        
        # Print category summary
        proverbs_count = db.query(Contribution).join(Contribution.categories).filter(
            Category.name == "Proverbs"
        ).count()
        print(f"\nTotal Proverbs in database: {proverbs_count}")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"Total contributions in database: {total_contributions}")
        
        print(f"\nNote: This represents selected high-value proverbs from the African Manners")
        print(f"collection of 1000+ traditional Kikuyu proverbs. Additional proverbs can be")
        print(f"added systematically to preserve this invaluable cultural heritage.")


if __name__ == "__main__":
    print("Seeding database with selected Kikuyu proverbs...")
    print("Source: African Manners collection of 1000+ traditional proverbs")
    try:
        create_proverbs_collection_seed()
        print("Proverbs collection seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)