#!/usr/bin/env python3
"""
Seed script for Kikuyu numbers from masteranylanguage.com
Populates the database with numerical vocabulary
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


def create_numbers_seed_data():
    """Create seed data for Kikuyu numbers from masteranylanguage.com"""
    
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
        
        # Get or create Numbers category
        numbers_category = db.query(Category).filter(Category.name == "Numbers").first()
        if not numbers_category:
            numbers_category = Category(
                name="Numbers",
                description="Numerical vocabulary and counting system",
                slug="numbers",
                sort_order=100  # Put at end of categories
            )
            db.add(numbers_category)
            db.commit()
            db.refresh(numbers_category)
        
        # Kikuyu numbers extracted from the file
        kikuyu_numbers = [
            # Basic numbers 1-10
            ("one", "ĩmwe", "Cardinal number 1", DifficultyLevel.BEGINNER),
            ("two", "ĩgiri", "Cardinal number 2", DifficultyLevel.BEGINNER),
            ("three", "ĩthatu", "Cardinal number 3", DifficultyLevel.BEGINNER),
            ("four", "ĩnya", "Cardinal number 4", DifficultyLevel.BEGINNER),
            ("five", "ĩthano", "Cardinal number 5", DifficultyLevel.BEGINNER),
            ("six", "ĩthathatũ", "Cardinal number 6", DifficultyLevel.BEGINNER),
            ("seven", "mũgwanja", "Cardinal number 7", DifficultyLevel.BEGINNER),
            ("eight", "inyanya", "Cardinal number 8", DifficultyLevel.BEGINNER),
            ("nine", "kenda", "Cardinal number 9", DifficultyLevel.BEGINNER),
            ("ten", "ikũmi", "Cardinal number 10", DifficultyLevel.BEGINNER),
            
            # Compound numbers 11-18
            ("eleven", "ikũmi na ĩmwe", "10 + 1: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("twelve", "ikũmi na ĩgiri", "10 + 2: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("thirteen", "ikũmi na ĩthatu", "10 + 3: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("fourteen", "ikũmi na ĩnya", "10 + 4: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("fifteen", "ikũmi na ĩthano", "10 + 5: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("sixteen", "ikũmi na ĩthathatũ", "10 + 6: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("seventeen", "ikũmi na mũgwanja", "10 + 7: Compound number construction", DifficultyLevel.INTERMEDIATE),
            ("eighteen", "ikũmi na inyanya", "10 + 8: Compound number construction", DifficultyLevel.INTERMEDIATE),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        for english, kikuyu, context, difficulty in kikuyu_numbers:
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
                context_notes=context,
                cultural_notes="Extracted from masteranylanguage.com Kikuyu numbers resource",
                quality_score=4.9,  # High quality educational content
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()  # Get the ID
            
            # Associate with Numbers category
            contribution.categories.append(numbers_category)
            
            contribution_count += 1
        
        # Create sub-translations for compound numbers to show construction pattern
        compound_numbers = [
            ("eleven", "ikũmi na ĩmwe", [
                ("ten", "ikũmi", 0, "Base number 10"),
                ("and", "na", 1, "Conjunction connecting numbers"),
                ("one", "ĩmwe", 2, "Additional unit")
            ]),
            ("twelve", "ikũmi na ĩgiri", [
                ("ten", "ikũmi", 0, "Base number 10"),
                ("and", "na", 1, "Conjunction connecting numbers"),
                ("two", "ĩgiri", 2, "Additional units")
            ]),
            ("fifteen", "ikũmi na ĩthano", [
                ("ten", "ikũmi", 0, "Base number 10"),
                ("and", "na", 1, "Conjunction connecting numbers"),
                ("five", "ĩthano", 2, "Additional units")
            ]),
            ("eighteen", "ikũmi na inyanya", [
                ("ten", "ikũmi", 0, "Base number 10"),
                ("and", "na", 1, "Conjunction connecting numbers"),
                ("eight", "inyanya", 2, "Additional units")
            ])
        ]
        
        for source, target, sub_parts in compound_numbers:
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
        
        print(f"Successfully created {contribution_count} new number contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added sub-translations for {len(compound_numbers)} compound numbers")
        print("All data marked as approved for immediate use")
        
        # Print number system analysis
        print("\nKikuyu Number System Analysis:")
        print("- Basic numbers 1-10: Simple cardinal forms")
        print("- Compound numbers 11+: 'ikumi na X' pattern (ten and X)")
        print("- Construction follows additive principle")
        print("- Conjunction 'na' (and) connects base ten with units")
        
        # Print total counts
        numbers_count = db.query(Contribution).join(Contribution.categories).filter(
            Category.name == "Numbers"
        ).count()
        total_contributions = db.query(Contribution).count()
        
        print(f"\nNumbers category: {numbers_count} contributions")
        print(f"Total contributions in database: {total_contributions}")


if __name__ == "__main__":
    print("Seeding database with Kikuyu numbers from masteranylanguage.com...")
    try:
        create_numbers_seed_data()
        print("Numbers seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)