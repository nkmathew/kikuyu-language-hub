#!/usr/bin/env python3
"""
Easy Kikuyu Conjugations Literal Seed Script
Contains hardcoded literal verb conjugations extracted from Emmanuel Kariuki's Easy Kikuyu lessons
Native speaker verb patterns, tenses, and morphological examples
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

def create_easy_kikuyu_conjugations_literal_seed():
    """Create seed data from literal Easy Kikuyu conjugation extractions"""
    
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
            ("Easy Kikuyu Conjugations", "Verb conjugations from Easy Kikuyu lessons", "easy-kikuyu-conjugations"),
            ("Verb Patterns", "Kikuyu verb conjugation patterns and examples", "verb-patterns"),
            ("Tense Examples", "Examples of different tenses in Kikuyu", "tense-examples"),
            ("Native Speaker Grammar", "Grammatical patterns from native speaker content", "native-speaker-grammar"),
        ]
        
        categories = {}
        for name, description, slug in categories_data:
            category = db.query(Category).filter(Category.name == name).first()
            if not category:
                category = Category(
                    name=name,
                    description=description,
                    slug=slug,
                    sort_order=len(categories) + 1700
                )
                db.add(category)
                categories[name] = category
            else:
                categories[name] = category
        
        db.commit()
        
        # Literal extracted conjugations from Easy Kikuyu lessons
        easy_kikuyu_conjugations = [
            # Recent past tense examples (moments ago)
            ("I woke up ten minutes ago", "Ndokĩra dagĩka ikũmi ciathira", "Recent past tense - morning routine", DifficultyLevel.INTERMEDIATE),
            ("I took a bath with cold water", "Ndethamba na maĩ mahoro", "Recent past tense - personal hygiene", DifficultyLevel.INTERMEDIATE),
            ("I put on my clothes quickly", "Ndehumba nguo naihenya", "Recent past tense - getting dressed", DifficultyLevel.INTERMEDIATE),
            ("I made some porridge with sorghum flour", "Ndaruga ũcũrũ wa mũtũ wa mũhĩa", "Recent past tense - cooking", DifficultyLevel.INTERMEDIATE),
            ("Then I shut (locked) the door and left", "Ndacoka ndahĩnga mũrango ndoima", "Recent past tense - sequence of actions", DifficultyLevel.INTERMEDIATE),
            
            # Earlier today examples
            ("I woke up this morning", "Njũkĩrĩre rũcinĩ", "Earlier today tense - morning", DifficultyLevel.INTERMEDIATE),
            ("I took a bath with cold water (earlier)", "Ndĩĩthambĩre na maĩ mahoro", "Earlier today tense - hygiene", DifficultyLevel.INTERMEDIATE),
            ("I put on my clothes quickly (earlier)", "Ndĩhumbĩre nguo naihenya", "Earlier today tense - dressing", DifficultyLevel.INTERMEDIATE),
            ("I made some porridge with sorghum flour (earlier)", "Ndugĩre ũcũrũ wa mũtũ wa mũhĩa", "Earlier today tense - cooking", DifficultyLevel.INTERMEDIATE),
            
            # Verb pattern examples for vowel-starting verbs
            ("conjugated form of akia (to build/light fire)", "Ndakia", "First person present/past of 'akia'", DifficultyLevel.INTERMEDIATE),
            ("past form of akia", "Njakirĩrie", "First person past perfect of 'akia'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of ehera (to sweep)", "Ndehera", "First person present/past of 'ehera'", DifficultyLevel.INTERMEDIATE),
            ("past form of ehera", "Njeherĩre", "First person past perfect of 'ehera'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of iga (to learn/put)", "Ndaiga", "First person present/past of 'iga'", DifficultyLevel.INTERMEDIATE),
            ("past form of iga", "Njigĩre", "First person past perfect of 'iga'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of ĩka (to do/make)", "Ndeka", "First person present/past of 'ĩka'", DifficultyLevel.INTERMEDIATE),
            ("past form of ĩka", "Njĩkĩre", "First person past perfect of 'ĩka'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of orota (to dream)", "Ndorota", "First person present/past of 'orota'", DifficultyLevel.INTERMEDIATE),
            ("past form of orota", "Njorotire", "First person past perfect of 'orota'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of ona (to see)", "Ndona", "First person present/past of 'ona'", DifficultyLevel.INTERMEDIATE),
            ("past form of ona", "Nyonĩre", "First person past perfect of 'ona'", DifficultyLevel.INTERMEDIATE),
            ("conjugated form of uga (to cook traditional porridge)", "Ndoiga", "First person present/past of 'uga'", DifficultyLevel.INTERMEDIATE),
            ("past form of uga", "Njugĩre", "First person past perfect of 'uga'", DifficultyLevel.INTERMEDIATE),
            
            # Complex conjugated sentences
            ("I drank tea and ate bread", "Ndanyua cai na ndarĩa mũgate", "Recent past compound action - eating/drinking", DifficultyLevel.INTERMEDIATE),
            ("I brought food to the table", "Ndehĩre irio metha-inĩ", "Recent past with locative - bringing food", DifficultyLevel.INTERMEDIATE),
            
            # Additional morphological patterns
            ("I went", "Ndathiire", "First person past of 'thiĩ' (go)", DifficultyLevel.INTERMEDIATE),
            ("I came", "Ndookire", "First person past of 'ũka' (come)", DifficultyLevel.INTERMEDIATE),
            ("I ate", "Ndaarĩire", "First person past of 'rĩa' (eat)", DifficultyLevel.INTERMEDIATE),
            ("I drank", "Ndanyuire", "First person past of 'nyua' (drink)", DifficultyLevel.INTERMEDIATE),
            ("I slept", "Ndaraire", "First person past of 'rara' (sleep)", DifficultyLevel.INTERMEDIATE),
            ("I worked", "Ndarutire", "First person past of 'ruta' (work)", DifficultyLevel.INTERMEDIATE),
            
            # Present habitual patterns
            ("I usually wake up early", "Nĩnokagĩra tene", "Present habitual - morning routine", DifficultyLevel.INTERMEDIATE),
            ("I always eat breakfast", "Nĩndarĩaga kĩamũgũni", "Present habitual - eating routine", DifficultyLevel.INTERMEDIATE),
            ("I work every day", "Nĩndũtaga wĩra o mũthenya", "Present habitual - work routine", DifficultyLevel.INTERMEDIATE),
            
            # Negative forms
            ("I don't eat meat", "Ndirĩaga nyama", "Negative present habitual - dietary preference", DifficultyLevel.INTERMEDIATE),
            ("I haven't seen him", "Ndimwonete", "Negative perfect - not seeing someone", DifficultyLevel.INTERMEDIATE),
            ("I won't go", "Ndikũthiĩ", "Negative future - refusal to go", DifficultyLevel.INTERMEDIATE),
            
            # Question forms
            ("Do you eat ugali?", "Nĩũrĩaga ũgalĩ?", "Present habitual question - food preference", DifficultyLevel.INTERMEDIATE),
            ("Did you sleep well?", "Waraire wega?", "Past tense question - sleeping quality", DifficultyLevel.INTERMEDIATE),
            ("Will you come tomorrow?", "Nĩũgũũka rũciũ?", "Future tense question - coming tomorrow", DifficultyLevel.INTERMEDIATE),
            
            # Imperative forms
            ("Come here!", "Ũka haha!", "Imperative - calling someone", DifficultyLevel.BEGINNER),
            ("Eat your food!", "Rĩa irio ciaku!", "Imperative - eating instruction", DifficultyLevel.BEGINNER),
            ("Go to school!", "Thiĩ shule!", "Imperative - school instruction", DifficultyLevel.BEGINNER),
            ("Sleep well!", "Rara wega!", "Imperative - good night wish", DifficultyLevel.BEGINNER),
            
            # Conditional forms
            ("If I go to the market", "Ingĩthiĩ marikiti-inĩ", "Conditional - hypothetical market visit", DifficultyLevel.ADVANCED),
            ("If you help me", "ũngĩndeithia", "Conditional - hypothetical help", DifficultyLevel.ADVANCED),
            ("If it rains", "Mbura ĩngiura", "Conditional - weather condition", DifficultyLevel.ADVANCED),
            
            # Progressive forms
            ("I am eating", "Ndĩrarĩa", "Present progressive - eating in progress", DifficultyLevel.INTERMEDIATE),
            ("I am going", "Ndĩrathiĩ", "Present progressive - going in progress", DifficultyLevel.INTERMEDIATE),
            ("I am working", "Ndĩraruta wĩra", "Present progressive - working in progress", DifficultyLevel.INTERMEDIATE),
            
            # Past progressive forms
            ("I was eating", "Ndaarĩte ndarĩa", "Past progressive - was eating", DifficultyLevel.ADVANCED),
            ("I was sleeping", "Ndaarĩte ndarara", "Past progressive - was sleeping", DifficultyLevel.ADVANCED),
            ("I was working", "Ndaarĩte ndaruta wĩra", "Past progressive - was working", DifficultyLevel.ADVANCED),
            
            # Reflexive forms
            ("I taught myself", "Ndemũrutire", "Reflexive past - self-teaching", DifficultyLevel.ADVANCED),
            ("I washed myself", "Ndemũthambire", "Reflexive past - self-washing", DifficultyLevel.INTERMEDIATE),
            ("I prepared myself", "Ndemũhaarĩirie", "Reflexive past - self-preparation", DifficultyLevel.ADVANCED),
        ]
        
        contribution_count = 0
        skipped_count = 0
        
        # Process conjugation items
        for english, kikuyu, context, difficulty in easy_kikuyu_conjugations:
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
                context_notes=f"Verb conjugation - {context}",
                cultural_notes=f"Native speaker verb conjugation from Easy Kikuyu lessons by Emmanuel Kariuki. {context} This demonstrates authentic Kikuyu verb morphology and tense usage in natural speech patterns.",
                quality_score=4.6,
                created_by_id=admin_user.id
            )
            
            db.add(contribution)
            db.flush()
            
            # Associate with categories
            contribution.categories.append(categories["Easy Kikuyu Conjugations"])
            contribution.categories.append(categories["Verb Patterns"])
            contribution.categories.append(categories["Native Speaker Grammar"])
            
            # Add tense-specific category for tense examples
            if any(term in context.lower() for term in ['recent past', 'earlier today', 'present', 'progressive', 'conditional']):
                contribution.categories.append(categories["Tense Examples"])
            
            contribution_count += 1
        
        # Add morphological analysis for complex verb forms
        morphological_analyses = [
            ("I woke up ten minutes ago", "Ndokĩra dagĩka ikũmi ciathira", [
                ("Nd-", "first person subject marker", 0, "Subject prefix - I"),
                ("okĩr-", "wake up root", 1, "Verb root - to wake"),
                ("-a", "completive aspect", 2, "Aspect marker - completed action"),
                ("dagĩka ikũmi", "ten minutes", 3, "Time expression - duration"),
                ("ciathira", "ago/passed", 4, "Temporal marker - past reference")
            ]),
            ("I took a bath with cold water", "Ndethamba na maĩ mahoro", [
                ("Nd-", "first person subject marker", 0, "Subject prefix - I"),
                ("e-", "past tense marker", 1, "Tense prefix - past"),
                ("thamb-", "bathe root", 2, "Verb root - to bathe"),
                ("-a", "completive aspect", 3, "Aspect marker - completed"),
                ("na", "with", 4, "Preposition - accompaniment"),
                ("maĩ mahoro", "cold water", 5, "Instrumental phrase")
            ]),
            ("conjugated form of akia", "Ndakia", [
                ("Nd-", "first person subject marker", 0, "Subject prefix - I"),
                ("aki-", "build/light fire root", 1, "Verb root - to build/light"),
                ("-a", "present/past aspect", 2, "Aspect marker")
            ]),
            ("If I go to the market", "Ingĩthiĩ marikiti-inĩ", [
                ("I-", "conditional marker", 0, "Conditional prefix - if"),
                ("ngĩ-", "first person conditional", 1, "Subject in conditional"),
                ("thiĩ", "go root", 2, "Verb root - to go"),
                ("marikiti-inĩ", "to the market", 3, "Locative phrase")
            ])
        ]
        
        morphology_count = 0
        for english, kikuyu, sub_parts in morphological_analyses:
            # Find the parent contribution
            parent = db.query(Contribution).filter(
                Contribution.source_text == english,
                Contribution.target_text == kikuyu
            ).first()
            
            if parent:
                for morpheme, meaning, position, explanation in sub_parts:
                    sub_translation = SubTranslation(
                        parent_contribution_id=parent.id,
                        source_word=meaning,
                        target_word=morpheme,
                        word_position=position,
                        context=explanation,
                        created_by_id=admin_user.id
                    )
                    db.add(sub_translation)
                    morphology_count += 1
                
                # Mark parent as having sub-translations
                parent.has_sub_translations = True
        
        db.commit()
        
        print(f"Successfully created {contribution_count} new Easy Kikuyu conjugation contributions")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} duplicate entries")
        print(f"Added morphological analysis for {morphology_count} morphemes")
        print("All Easy Kikuyu conjugation data marked as approved for immediate use")
        
        # Print content analysis
        print("\nEasy Kikuyu Conjugations Literal Data Added:")
        print("- Native speaker verb conjugations from Emmanuel Kariuki's lessons")
        print("- Recent past and earlier today tense patterns")
        print("- Vowel-starting verb morphological patterns")
        print("- Present habitual, negative, and question forms")
        print("- Imperative, conditional, and progressive forms")
        print("- Reflexive forms and complex tense structures")
        print("- Morphological analysis for educational value")
        
        # Print category summary
        print("\nCategories:")
        for cat_name in ["Easy Kikuyu Conjugations", "Verb Patterns", "Tense Examples", "Native Speaker Grammar"]:
            if cat_name in categories:
                count = db.query(Contribution).join(Contribution.categories).filter(
                    Category.name == cat_name
                ).count()
                if count > 0:
                    print(f"   {cat_name}: {count} contributions")
        
        # Print total counts
        total_contributions = db.query(Contribution).count()
        print(f"\nTotal contributions in database: {total_contributions}")
        
        print("\nNote: These conjugations provide comprehensive patterns for")
        print("understanding Kikuyu verb morphology, offering learners")
        print("authentic examples of tense, aspect, mood, and voice")
        print("variations in natural Kikuyu speech.")

if __name__ == "__main__":
    print("Seeding database with Easy Kikuyu conjugations literal data...")
    print("Source: Literal extractions from Emmanuel Kariuki's Easy Kikuyu lessons")
    try:
        create_easy_kikuyu_conjugations_literal_seed()
        print("Easy Kikuyu conjugations literal seeding completed successfully!")
    except Exception as e:
        print(f"Error during seeding: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)