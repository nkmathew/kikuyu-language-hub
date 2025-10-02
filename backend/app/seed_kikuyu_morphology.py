"""
Kikuyu verb morphology seed data based on the language guide
"""
import json
import sys
from sqlalchemy.orm import Session
from app.db.base import Base
from app.db.connection import engine
from app.db.session import SessionLocal
from app.models.morphology import (
    Verb, VerbConjugation, NounForm, VerbExample, WordClass,
    MorphologicalPattern
)
from app.models.user import User


# Create tables if they don't exist
Base.metadata.create_all(bind=engine)


def create_word_classes(db: Session):
    """Create word classes based on Kikuyu classification"""
    word_classes_data = [
        {
            "word_type": "verb",
            "kikuyu_term": "Ithimi",
            "english_term": "Verbs",
            "description": "Action words describing what subjects do",
            "examples": [
                {"kikuyu": "thiĩ", "english": "go"},
                {"kikuyu": "rĩra", "english": "cry"}
            ]
        },
        {
            "word_type": "noun", 
            "kikuyu_term": "Andũ",
            "english_term": "People",
            "description": "Class I nouns - human beings",
            "examples": [
                {"kikuyu": "Mũndũ", "english": "person"},
                {"kikuyu": "Mũtumia", "english": "married woman"}
            ]
        },
        {
            "word_type": "noun",
            "kikuyu_term": "Mĩtĩ",
            "english_term": "Trees",
            "description": "Class II nouns - large trees and plants",
            "examples": [
                {"kikuyu": "Mũkũyũ", "english": "fig tree"},
                {"kikuyu": "Mũrimũ", "english": "epidemic disease"}
            ]
        }
    ]
    
    word_classes = []
    for wc_data in word_classes_data:
        # Check if word class already exists
        existing = db.query(WordClass).filter(WordClass.kikuyu_term == wc_data["kikuyu_term"]).first()
        if existing:
            word_classes.append(existing)
            print(f"  Using existing word class: {existing.kikuyu_term}")
        else:
            word_class = WordClass(**wc_data)
            db.add(word_class)
            db.flush()
            word_classes.append(word_class)
            print(f"  Created new word class: {word_class.kikuyu_term}")
    
    return word_classes


def create_morphological_patterns(db: Session):
    """Create morphological patterns found in Kikuyu"""
    patterns_data = [
        {
            "pattern_name": "Present Continuous Subject Markers",
            "pattern_type": "prefix",
            "description": "Subject markers for present continuous tense",
            "rule": "Subject marker + ndĩ + r + verb stem",
            "examples": [
                {
                    "base": "oka (come)",
                    "transformed": "ndĩroka",
                    "explanation": "ni + ndĩ + r + oka = I am coming"
                },
                {
                    "base": "oka (come)",
                    "transformed": "ũroka", 
                    "explanation": "u + ndĩ + r + oka = you are coming"
                }
            ],
            "applies_to": ["verb_class_monosyllabic"],
            "conditions": {"tense": "present", "aspect": "continuous"}
        },
        {
            "pattern_name": "Past Tense Marker",
            "pattern_type": "suffix",
            "description": "Past tense formation with -ire suffix",
            "rule": "Conjugated form + ire",
            "examples": [
                {
                    "base": "ka (eat)",
                    "transformed": "kaiire", 
                    "explanation": "ka + ire = ate"
                },
                {
                    "base": "oka (come)",
                    "transformed": "okire",
                    "explanation": "oka + ire = came"
                }
            ],
            "applies_to": ["verb_class_monosyllabic", "verb_class_disyllabic"],
            "conditions": {"tense": "past", "aspect": "simple"}
        }
    ]
    
    patterns = []
    for p_data in patterns_data:
        # Check if pattern already exists
        existing = db.query(MorphologicalPattern).filter(
            MorphologicalPattern.pattern_name == p_data["pattern_name"]
        ).first()
        if existing:
            patterns.append(existing)
            print(f"  Using existing pattern: {existing.pattern_name}")
        else:
            pattern = MorphologicalPattern(**p_data)
            db.add(pattern)
            db.flush()
            patterns.append(pattern)
            print(f"  Created new pattern: {pattern.pattern_name}")
    
    return patterns


def create_sample_verbs(db: Session, word_classes):
    """Create sample verbs with real Kikuyu examples from the guide"""
    verbs_data = [
        {
            "base_form": "oka",
            "english_meaning": "to come",
            "verb_class": "monosyllabic",
            "consonant_pattern": "k",
            "is_transitive": False,
            "is_stative": False,
            "semantic_field": "motion",
            "register": "common",
            "conjugations": [
                {
                    "tense": "present",
                    "aspect": "continuous",
                    "mood": "indicative",
                    "polarity": "affirmative",
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "ndĩroka",
                            "morphology": ["ni", "ndĩ", "r", "oka"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present continuous", "type": "aspect_marker"},
                                {"stem": "oka", "meaning": "come", "type": "verb_stem"}
                            ]
                        },
                        {
                            "person": "second", 
                            "number": "singular",
                            "form": "ũroka",
                            "morphology": ["ũ", "ndĩ", "r", "oka"],
                            "breakdown": [
                                {"prefix": "ũ", "meaning": "you", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present continuous", "type": "aspect_marker"},
                                {"stem": "oka", "meaning": "come", "type": "verb_stem"}
                            ]
                        },
                        {
                            "person": "third",
                            "number": "singular",
                            "form": "aroka", 
                            "morphology": ["a", "ndĩ", "r", "oka"],
                            "breakdown": [
                                {"prefix": "a", "meaning": "he/she", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present continuous", "type": "aspect_marker"},
                                {"stem": "oka", "meaning": "come", "type": "verb_stem"}
                            ]
                        }
                    ]
                },
                {
                    "tense": "present",
                    "aspect": "simple",
                    "mood": "indicative", 
                    "polarity": "affirmative",
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "ndoka",
                            "morphology": ["ni", "nd", "oka"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"infix": "nd", "meaning": "present simple", "type": "tense_marker"},
                                {"stem": "oka", "meaning": "come", "type": "verb_stem"}
                            ]
                        }
                    ]
                },
                {
                    "tense": "past",
                    "aspect": "simple",
                    "mood": "indicative",
                    "polarity": "affirmative", 
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "nokire",
                            "morphology": ["ni", "ok", "ire"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"stem": "ok", "meaning": "come", "type": "verb_stem"},
                                {"suffix": "ire", "meaning": "past", "type": "tense_marker"}
                            ]
                        }
                    ]
                }
            ],
            "examples": [
                {
                    "kikuyu_sentence": "Nĩ ndĩroka thukuru.",
                    "english_translation": "I am coming to school.",
                    "context_description": "Present continuous tense example",
                    "register": "formal",
                    "verb_form_used": "ndĩroka",
                    "tense_aspect_mood": "present continuous indicative"
                }
            ]
        },
        {
            "base_form": "rĩra",
            "english_meaning": "to cry",
            "verb_class": "disyllabic",
            "consonant_pattern": "r",
            "is_transitive": False,
            "is_stative": False,
            "semantic_field": "emotion",
            "register": "common",
            "conjugations": [
                {
                    "tense": "present",
                    "aspect": "simple",
                    "mood": "indicative",
                    "polarity": "affirmative",
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "ndĩrarĩra",
                            "morphology": ["ni", "ndĩ", "r", "a", "rĩra"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present simple", "type": "tense_marker"},
                                {"stem": "rĩra", "meaning": "cry", "type": "verb_stem"}
                            ]
                        }
                    ]
                }
            ]
        },
        {
            "base_form": "thiĩ",
            "english_meaning": "to go",
            "verb_class": "monosyllabic",
            "consonant_pattern": "th",
            "is_transitive": False,
            "is_stative": False,
            "semantic_field": "motion",
            "register": "common",
            "conjugations": [
                {
                    "tense": "present",
                    "aspect": "simple",
                    "mood": "indicative",
                    "polarity": "affirmative",
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "ndĩthiĩ",
                            "morphology": ["ni", "ndĩ", "thiĩ"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present simple", "type": "tense_marker"},
                                {"stem": "thiĩ", "meaning": "go", "type": "verb_stem"}
                            ]
                        }
                    ]
                }
            ]
        },
        {
            "base_form": "rĩa",
            "english_meaning": "to eat",
            "verb_class": "monosyllabic",
            "consonant_pattern": "r",
            "is_transitive": True,
            "is_stative": False,
            "semantic_field": "basic_actions",
            "register": "common",
            "conjugations": [
                {
                    "tense": "present",
                    "aspect": "simple",
                    "mood": "indicative",
                    "polarity": "affirmative",
                    "forms": [
                        {
                            "person": "first",
                            "number": "singular",
                            "form": "ndĩrĩa",
                            "morphology": ["ni", "ndĩ", "rĩa"],
                            "breakdown": [
                                {"prefix": "ni", "meaning": "I", "type": "subject_marker"},
                                {"infix": "ndĩ", "meaning": "present simple", "type": "tense_marker"},
                                {"stem": "rĩa", "meaning": "eat", "type": "verb_stem"}
                            ]
                        }
                    ]
                }
            ]
        }
    ]
    
    verbs = []
    for v_data in verbs_data:
        # Check if verb already exists
        existing = db.query(Verb).filter(Verb.base_form == v_data["base_form"]).first()
        is_new = False
        
        if existing:
            verb = existing
            print(f"  Using existing verb: {verb.base_form}")
        else:
            is_new = True
            verb = Verb(
                base_form=v_data["base_form"],
                english_meaning=v_data["english_meaning"],
                verb_class=v_data["verb_class"],
                consonant_pattern=v_data["consonant_pattern"],
                is_transitive=v_data["is_transitive"],
                is_stative=v_data["is_stative"],
                semantic_field=v_data["semantic_field"],
                register=v_data["register"]
            )
            db.add(verb)
            db.flush()
            print(f"  Created new verb: {verb.base_form}")
        
        verbs.append(verb)
        
        # Only add related data if this is a new verb
        if is_new:
            # Add conjugations
            if "conjugations" in v_data:
                for conj_set in v_data["conjugations"]:
                    for form_data in conj_set["forms"]:
                        conjugation = VerbConjugation(
                            verb_id=verb.id,
                            tense=conj_set["tense"],
                            aspect=conj_set["aspect"],
                            mood=conj_set["mood"],
                            polarity=conj_set["polarity"],
                            person=form_data["person"],
                            number=form_data["number"],
                            conjugated_form=form_data["form"],
                            morphological_breakdown=form_data["breakdown"],
                            usage_context=form_data.get("context_description"),
                            is_common=True
                        )
                        db.add(conjugation)
            
            # Add examples
            if "examples" in v_data:
                for ex_data in v_data["examples"]:
                    example = VerbExample(
                        verb_id=verb.id,
                        kikuyu_sentence=ex_data["kikuyu_sentence"],
                        english_translation=ex_data["english_translation"],
                        context_description=ex_data["context_description"],
                        register=ex_data["register"],
                        verb_form_used=ex_data["verb_form_used"],
                        tense_aspect_mood=ex_data["tense_aspect_mood"]
                    )
                    db.add(example)
    
    return verbs


def create_derived_forms(db: Session, verbs):
    """Create derived noun forms from verbs"""
    derived_forms_data = [
        {
            "related_verb_id": verbs[0].id,  # oka (to come)
            "noun_form": "ũkoro",
            "english_meaning": "act of coming, arrival",
            "noun_class": "abstract",
            "derivation_type": "abstract_action",
            "formation_rule": "ũk + verb stem"
        },
        {
            "related_verb_id": verbs[1].id,  # rĩra (to cry)
            "noun_form": "ũkĩra",
            "english_meaning": "act of crying, weeping",
            "noun_class": "abstract", 
            "derivation_type": "abstract_action",
            "formation_rule": "ũk + verb stem"
        },
        {
            "related_verb_id": verbs[3].id,  # rĩa (to eat)
            "noun_form": "ĩrĩo",
            "english_meaning": "food, something to eat",
            "noun_class": "concrete",
            "derivation_type": "action_object",
            "formation_rule": "ĩ + verb stem + o"
        }
    ]
    
    derived_forms = []
    for df_data in derived_forms_data:
        derived_form = NounForm(**df_data)
        db.add(derived_form)
        db.flush()
        derived_forms.append(derived_form)
    
    return derived_forms


def main():
    """Main function to seed the database"""
    # Set UTF-8 encoding for output
    sys.stdout.reconfigure(encoding='utf-8')
    
    db = SessionLocal()
    
    try:
        print("Creating word classes...")
        word_classes = create_word_classes(db)
        print(f"Created {len(word_classes)} word classes")
        
        print("Creating morphological patterns...")
        patterns = create_morphological_patterns(db)
        print(f"Created {len(patterns)} morphological patterns")
        
        print("Creating sample verbs...")
        verbs = create_sample_verbs(db, word_classes)
        print(f"Created {len(verbs)} verbs with conjugations and examples")
        
        print("Creating derived forms...")
        derived_forms = create_derived_forms(db, verbs)
        print(f"Created {len(derived_forms)} derived noun forms")
        
        db.commit()
        print("SUCCESS: Kikuyu morphology seed data created successfully!")
        
        # Print summary
        print("\nDatabase Summary:")
        print(f"  Word Classes: {len(word_classes)}")
        print(f"  Morphological Patterns: {len(patterns)}")
        print(f"  Verbs: {len(verbs)}")
        print(f"  Derived Forms: {len(derived_forms)}")
        
        # Count conjugations and examples
        total_conjugations = db.query(VerbConjugation).count()
        total_examples = db.query(VerbExample).count()
        print(f"  Conjugations: {total_conjugations}")
        print(f"  Examples: {total_examples}")
        
    except Exception as e:
        db.rollback()
        print(f"ERROR: Error creating seed data: {e}")
        raise
    finally:
        db.close()


if __name__ == "__main__":
    main()