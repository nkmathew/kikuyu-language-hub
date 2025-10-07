"""
Kikuyu verb morphology models for comprehensive verb conjugation and inflection
"""
from sqlalchemy import Column, Integer, String, Text, Boolean, ForeignKey, DateTime, Enum, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from enum import Enum as PyEnum
from app.db.base import Base


class WordType(str, PyEnum):
    NOUN = "noun"
    VERB = "verb"
    ADJECTIVE = "adjective"
    ADVERB = "adverb"
    PRONOUN = "pronoun"
    PREPOSITION = "preposition"
    CONJUNCTION = "conjunction"
    INTERJECTION = "interjection"


class TenseType(str, PyEnum):
    PRESENT = "present"
    PAST = "past"
    FUTURE = "future"
    HABITUAL = "habitual"


class AspectType(str, PyEnum):
    SIMPLE = "simple"
    CONTINUOUS = "continuous"
    PERFECT = "perfect"
    PERFECT_CONTINUOUS = "perfect_continuous"


class MoodType(str, PyEnum):
    INDICATIVE = "indicative"
    IMPERATIVE = "imperative"
    SUBJUNCTIVE = "subjunctive"
    CONDITIONAL = "conditional"


class NumberType(str, PyEnum):
    SINGULAR = "singular"
    PLURAL = "plural"


class PersonType(str, PyEnum):
    FIRST = "first"
    SECOND = "second"
    THIRD = "third"


class PolarityType(str, PyEnum):
    AFFIRMATIVE = "affirmative"
    NEGATIVE = "negative"


class WordClass(Base):
    __tablename__ = "word_classes"
    
    id = Column(Integer, primary_key=True, index=True)
    word_type = Column(Enum(WordType), nullable=False)
    kikuyu_term = Column(String(100), nullable=False, unique=True)
    english_term = Column(String(100), nullable=False)
    description = Column(Text)
    examples = Column(JSON)  # [{"kikuyu": "", "english": "", "audio_url": ""}]
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class Verb(Base):
    __tablename__ = "verbs"
    
    id = Column(Integer, primary_key=True, index=True)
    # Dictionary form (infinitive/base form)
    base_form = Column(String(200), nullable=False, unique=True, index=True)
    english_meaning = Column(String(500), nullable=False)
    word_class_id = Column(Integer, ForeignKey("word_classes.id"))
    
    # Morphological classification
    verb_class = Column(String(50))  # Kikuyu verb class (e.g., monosyllabic, disyllabic)
    consonant_pattern = Column(String(100))  # Pattern classification
    is_transitive = Column(Boolean, default=True)
    is_stative = Column(Boolean, default=False)
    
    # Semantic information
    semantic_field = Column(String(100))  # e.g., 'motion', 'communication', 'perception'
    register = Column(String(50))  # formal, informal, ceremonial
    
    # Audio and pronunciation
    pronunciation_guide = Column(String(500))
    audio_url = Column(String(500))
    
    # Relationships
    word_class = relationship("WordClass")
    conjugations = relationship("VerbConjugation", back_populates="verb", cascade="all, delete-orphan")
    noun_forms = relationship("NounForm", back_populates="related_verb", cascade="all, delete-orphan")
    examples = relationship("VerbExample", back_populates="verb", cascade="all, delete-orphan")
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class VerbConjugation(Base):
    __tablename__ = "verb_conjugations"
    
    id = Column(Integer, primary_key=True, index=True)
    verb_id = Column(Integer, ForeignKey("verbs.id"), nullable=False)
    
    # Conjugation features
    tense = Column(Enum(TenseType), nullable=False, index=True)
    aspect = Column(Enum(AspectType), nullable=False, index=True)
    mood = Column(Enum(MoodType), nullable=False, index=True)
    polarity = Column(Enum(PolarityType), nullable=False, index=True)
    
    # Subject agreement
    person = Column(Enum(PersonType), nullable=False, index=True)
    number = Column(Enum(NumberType), nullable=False, index=True)
    
    # Conjugated form
    conjugated_form = Column(String(500), nullable=False)
    morphological_breakdown = Column(JSON)  # [{"prefix": "", "stem": "", "suffix": "", "meaning": ""}]
    
    # Usage notes
    usage_context = Column(String(200))
    frequency = Column(Integer, default=1)  # 1-5 scale
    is_common = Column(Boolean, default=False)
    
    # Audio
    audio_url = Column(String(500))
    
    # Relationships
    verb = relationship("Verb", back_populates="conjugations")
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Unique constraint to prevent duplicates
    __table_args__ = (
        {"extend_existing": True}
    )


class NounForm(Base):
    __tablename__ = "noun_forms"
    
    id = Column(Integer, primary_key=True, index=True)
    related_verb_id = Column(Integer, ForeignKey("verbs.id"))
    
    # Noun form information
    noun_form = Column(String(200), nullable=False, index=True)
    english_meaning = Column(String(500), nullable=False)
    noun_class = Column(String(50))  # Kikuyu noun class (e.g., mu-mi, ki-vi)
    
    # Derivation type
    derivation_type = Column(String(100))  # agent, patient, instrument, abstract, etc.
    
    # Morphological information
    morphological_pattern = Column(String(200))
    formation_rule = Column(String(500))
    
    # Usage
    examples = Column(JSON)  # [{"kikuyu": "", "english": ""}]
    audio_url = Column(String(500))
    
    # Relationships
    related_verb = relationship("Verb", back_populates="noun_forms")
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class VerbExample(Base):
    __tablename__ = "verb_examples"
    
    id = Column(Integer, primary_key=True, index=True)
    verb_id = Column(Integer, ForeignKey("verbs.id"), nullable=False)
    
    # Example sentence
    kikuyu_sentence = Column(Text, nullable=False)
    english_translation = Column(Text, nullable=False)
    
    # Context information
    context_description = Column(String(300))
    register = Column(String(50))  # formal, informal, ceremonial
    
    # Grammatical analysis
    verb_form_used = Column(String(200))  # Which conjugation form appears
    tense_aspect_mood = Column(String(200))  # Description of tense/aspect/mood combination
    
    # Audio
    audio_url = Column(String(500))
    
    # Relationships
    verb = relationship("Verb", back_populates="examples")
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class MorphologicalPattern(Base):
    __tablename__ = "morphological_patterns"
    
    id = Column(Integer, primary_key=True, index=True)
    
    # Pattern identification
    pattern_name = Column(String(100), nullable=False, unique=True)
    pattern_type = Column(String(50))  # prefix, suffix, infix, circumfix, stem_change
    
    # Pattern description
    description = Column(Text, nullable=False)
    rule = Column(String(500))  # Linguistic rule
    
    # Examples
    examples = Column(JSON)  # [{"base": "", "transformed": "", "explanation": ""}]
    
    # Conditions
    applies_to = Column(JSON)  # ["verb_classes", "word_types"]
    conditions = Column(JSON)  # {"syllable_count": 2, "ending_pattern": "-a"}
    
    # Audio examples
    audio_examples = Column(JSON)  # [{"pattern": "", "audio_url": ""}]
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class MorphologicalSubmission(Base):
    __tablename__ = "morphological_submissions"
    
    id = Column(Integer, primary_key=True, index=True)
    
    # Submission metadata
    submission_type = Column(String(50), nullable=False)  # verb, noun, adjective, etc.
    created_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    
    # Core data
    base_form = Column(String(200), nullable=False)
    english_meaning = Column(String(500), nullable=False)
    
    # Detailed morphology data
    morphological_data = Column(JSON, nullable=False)  # Complex nested structure
    
    # Supporting information
    context_notes = Column(Text)
    source_reference = Column(String(300))
    confidence_level = Column(Integer, default=3)  # 1-5 scale
    
    # Review status
    status = Column(String(50), default="pending")  # pending, approved, rejected, needs_revision
    reviewed_by_id = Column(Integer, ForeignKey("users.id"))
    review_notes = Column(Text)
    
    # Relationships
    created_by = relationship("User", foreign_keys=[created_by_id])
    reviewed_by = relationship("User", foreign_keys=[reviewed_by_id])
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


# Sample morphological data structure for submissions
MORPHOLOGICAL_SUBMISSION_EXAMPLE = {
    "submission_type": "verb",
    "base_form": "thi",
    "english_meaning": "to do/make",
    "morphological_data": {
        "verb_class": "monosyllabic",
        "consonant_pattern": "th-",
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
                        "form": "nithi",
                        "morphology": ["ni-", "thi"],
                        "breakdown": [{"prefix": "ni-", "meaning": "I", "type": "subject_marker"}]
                    },
                    {
                        "person": "second",
                        "number": "singular", 
                        "form": "wuthi",
                        "morphology": ["wu-", "thi"],
                        "breakdown": [{"prefix": "wu-", "meaning": "you", "type": "subject_marker"}]
                    },
                    {
                        "person": "third",
                        "number": "singular",
                        "form": "arithi", 
                        "morphology": ["a-", "thi"],
                        "breakdown": [{"prefix": "a-", "meaning": "he/she", "type": "subject_marker"}]
                    },
                    {
                        "person": "first",
                        "number": "plural",
                        "form": "thuthi",
                        "morphology": ["thu-", "thi"],
                        "breakdown": [{"prefix": "thu-", "meaning": "we", "type": "subject_marker"}]
                    },
                    {
                        "person": "second", 
                        "number": "plural",
                        "form": "muthi",
                        "morphology": ["mu-", "thi"],
                        "breakdown": [{"prefix": "mu-", "meaning": "you (plural)", "type": "subject_marker"}]
                    },
                    {
                        "person": "third",
                        "number": "plural",
                        "form": "mathi",
                        "morphology": ["ma-", "thi"],
                        "breakdown": [{"prefix": "ma-", "meaning": "they", "type": "subject_marker"}]
                    }
                ]
            },
            {
                "tense": "present",
                "aspect": "continuous",
                "mood": "indicative", 
                "polarity": "affirmative",
                "forms": [
                    {
                        "person": "first",
                        "number": "singular",
                        "form": "nindithi",
                        "morphology": ["ni-", "-ndi-", "thi"],
                        "breakdown": [
                            {"prefix": "ni-", "meaning": "I", "type": "subject_marker"},
                            {"infix": "-ndi-", "meaning": "continuous", "type": "aspect_marker"}
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
                        "form": "nithiire",
                        "morphology": ["ni-", "thi", "-ire"],
                        "breakdown": [
                            {"prefix": "ni-", "meaning": "I", "type": "subject_marker"},
                            {"suffix": "-ire", "meaning": "past", "type": "tense_marker"}
                        ]
                    }
                ]
            }
        ],
        "derived_forms": [
            {
                "type": "agent_noun",
                "form": "muthi",
                "english": "doer/maker",
                "formation": "mu- + verb stem"
            },
            {
                "type": "abstract_noun", 
                "form": "mathi",
                "english": "act of doing/making",
                "formation": "ma- + verb stem"
            }
        ],
        "examples": [
            {
                "kikuyu": "Nithi kana.",
                "english": "I am doing it.",
                "context": "present tense simple",
                "conjugation_used": "present_simple_first_singular"
            },
            {
                "kikuyu": "Wathire muthenya.",
                "english": "You did it yesterday.",
                "context": "past tense simple",
                "conjugation_used": "past_simple_second_singular"
            }
        ]
    }
}