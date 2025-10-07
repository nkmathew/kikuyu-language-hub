"""
Pydantic schemas for Kikuyu verb morphology system
"""
from pydantic import BaseModel, Field, validator
from typing import List, Optional, Dict, Any, Union
from datetime import datetime
from enum import Enum


# Enums (matching SQLAlchemy enums)
class WordType(str, Enum):
    NOUN = "noun"
    VERB = "verb"
    ADJECTIVE = "adjective"
    ADVERB = "adverb"
    PRONOUN = "pronoun"
    PREPOSITION = "preposition"
    CONJUNCTION = "conjunction"
    INTERJECTION = "interjection"


class TenseType(str, Enum):
    PRESENT = "present"
    PAST = "past"
    FUTURE = "future"
    HABITUAL = "habitual"


class AspectType(str, Enum):
    SIMPLE = "simple"
    CONTINUOUS = "continuous"
    PERFECT = "perfect"
    PERFECT_CONTINUOUS = "perfect_continuous"


class MoodType(str, Enum):
    INDICATIVE = "indicative"
    IMPERATIVE = "imperative"
    SUBJUNCTIVE = "subjunctive"
    CONDITIONAL = "conditional"


class NumberType(str, Enum):
    SINGULAR = "singular"
    PLURAL = "plural"


class PersonType(str, Enum):
    FIRST = "first"
    SECOND = "second"
    THIRD = "third"


class PolarityType(str, Enum):
    AFFIRMATIVE = "affirmative"
    NEGATIVE = "negative"


# Base schemas
class BaseSchema(BaseModel):
    class Config:
        from_attributes = True


# Morphological component schemas
class MorphologicalBreakdown(BaseModel):
    prefix: Optional[str] = None
    stem: Optional[str] = None
    suffix: Optional[str] = None
    infix: Optional[str] = None
    meaning: Optional[str] = None
    type: Optional[str] = None  # subject_marker, tense_marker, aspect_marker, etc.


class ConjugatedForm(BaseModel):
    person: PersonType
    number: NumberType
    form: str = Field(..., min_length=1, max_length=500)
    morphology: List[str] = Field(default_factory=list)
    breakdown: List[MorphologicalBreakdown] = Field(default_factory=list)
    object_person: Optional[PersonType] = None
    object_number: Optional[NumberType] = None
    has_object: bool = False
    usage_context: Optional[str] = None
    frequency: int = Field(default=1, ge=1, le=5)
    is_common: bool = False
    audio_url: Optional[str] = None


class ConjugationSet(BaseModel):
    tense: TenseType
    aspect: AspectType
    mood: MoodType
    polarity: PolarityType
    forms: List[ConjugatedForm] = Field(..., min_items=1)


class DerivedForm(BaseModel):
    type: str = Field(..., min_length=1, max_length=100)  # agent_noun, patient_noun, abstract_noun, etc.
    form: str = Field(..., min_length=1, max_length=200)
    english: str = Field(..., min_length=1, max_length=500)
    formation: str = Field(..., min_length=1, max_length=500)
    noun_class: Optional[str] = None
    examples: Optional[List[Dict[str, str]]] = None
    audio_url: Optional[str] = None


class VerbExample(BaseModel):
    kikuyu: str = Field(..., min_length=1, max_length=1000)
    english: str = Field(..., min_length=1, max_length=1000)
    context_description: Optional[str] = None
    register: Optional[str] = None
    verb_form_used: Optional[str] = None
    tense_aspect_mood: Optional[str] = None
    audio_url: Optional[str] = None


# Word Class schemas
class WordClassBase(BaseSchema):
    word_type: WordType
    kikuyu_term: str = Field(..., min_length=1, max_length=100)
    english_term: str = Field(..., min_length=1, max_length=100)
    description: Optional[str] = None
    examples: Optional[List[Dict[str, str]]] = None


class WordClassCreate(WordClassBase):
    pass


class WordClassUpdate(BaseSchema):
    kikuyu_term: Optional[str] = Field(None, min_length=1, max_length=100)
    english_term: Optional[str] = Field(None, min_length=1, max_length=100)
    description: Optional[str] = None
    examples: Optional[List[Dict[str, str]]] = None


class WordClass(WordClassBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None


# Verb schemas
class VerbBase(BaseSchema):
    base_form: str = Field(..., min_length=1, max_length=200)
    english_meaning: str = Field(..., min_length=1, max_length=500)
    word_class_id: Optional[int] = None
    verb_class: Optional[str] = None
    consonant_pattern: Optional[str] = None
    is_transitive: bool = True
    is_stative: bool = False
    semantic_field: Optional[str] = None
    register: Optional[str] = None
    pronunciation_guide: Optional[str] = None
    audio_url: Optional[str] = None


class VerbCreate(VerbBase):
    conjugations: Optional[List[ConjugationSet]] = None
    derived_forms: Optional[List[DerivedForm]] = None
    examples: Optional[List[VerbExample]] = None


class VerbUpdate(BaseSchema):
    english_meaning: Optional[str] = Field(None, min_length=1, max_length=500)
    word_class_id: Optional[int] = None
    verb_class: Optional[str] = None
    consonant_pattern: Optional[str] = None
    is_transitive: Optional[bool] = None
    is_stative: Optional[bool] = None
    semantic_field: Optional[str] = None
    register: Optional[str] = None
    pronunciation_guide: Optional[str] = None
    audio_url: Optional[str] = None
    conjugations: Optional[List[ConjugationSet]] = None
    derived_forms: Optional[List[DerivedForm]] = None
    examples: Optional[List[VerbExample]] = None


class Verb(VerbBase):
    id: int
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class VerbDetail(Verb):
    conjugations: Optional[List[Dict[str, Any]]] = None
    noun_forms: Optional[List[Dict[str, Any]]] = None
    examples: Optional[List[Dict[str, Any]]] = None
    word_class: Optional[WordClass] = None


# Verb Conjugation schemas
class VerbConjugationBase(BaseSchema):
    tense: TenseType
    aspect: AspectType
    mood: MoodType
    polarity: PolarityType
    person: PersonType
    number: NumberType
    object_person: Optional[PersonType] = None
    object_number: Optional[NumberType] = None
    has_object: bool = False
    conjugated_form: str = Field(..., min_length=1, max_length=500)
    morphological_breakdown: Optional[List[MorphologicalBreakdown]] = None
    usage_context: Optional[str] = None
    frequency: int = Field(default=1, ge=1, le=5)
    is_common: bool = False
    audio_url: Optional[str] = None


class VerbConjugationCreate(VerbConjugationBase):
    pass


class VerbConjugationUpdate(BaseSchema):
    conjugated_form: Optional[str] = Field(None, min_length=1, max_length=500)
    morphological_breakdown: Optional[List[MorphologicalBreakdown]] = None
    usage_context: Optional[str] = None
    frequency: Optional[int] = Field(None, ge=1, le=5)
    is_common: Optional[bool] = None
    audio_url: Optional[str] = None


class VerbConjugation(VerbConjugationBase):
    id: int
    verb_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None


# Noun Form schemas
class NounFormBase(BaseSchema):
    noun_form: str = Field(..., min_length=1, max_length=200)
    english_meaning: str = Field(..., min_length=1, max_length=500)
    noun_class: Optional[str] = None
    derivation_type: Optional[str] = None
    morphological_pattern: Optional[str] = None
    formation_rule: Optional[str] = None
    examples: Optional[List[Dict[str, str]]] = None
    audio_url: Optional[str] = None


class NounFormCreate(NounFormBase):
    related_verb_id: Optional[int] = None


class NounFormUpdate(BaseSchema):
    noun_form: Optional[str] = Field(None, min_length=1, max_length=200)
    english_meaning: Optional[str] = Field(None, min_length=1, max_length=500)
    noun_class: Optional[str] = None
    derivation_type: Optional[str] = None
    morphological_pattern: Optional[str] = None
    formation_rule: Optional[str] = None
    examples: Optional[List[Dict[str, str]]] = None
    audio_url: Optional[str] = None


class NounForm(NounFormBase):
    id: int
    related_verb_id: Optional[int] = None
    created_at: datetime
    updated_at: Optional[datetime] = None


# Verb Example schemas
class VerbExampleBase(BaseSchema):
    kikuyu_sentence: str = Field(..., min_length=1, max_length=2000)
    english_translation: str = Field(..., min_length=1, max_length=2000)
    context_description: Optional[str] = None
    register: Optional[str] = None
    verb_form_used: Optional[str] = None
    tense_aspect_mood: Optional[str] = None
    audio_url: Optional[str] = None


class VerbExampleCreate(VerbExampleBase):
    verb_id: int


class VerbExampleUpdate(BaseSchema):
    kikuyu_sentence: Optional[str] = Field(None, min_length=1, max_length=2000)
    english_translation: Optional[str] = Field(None, min_length=1, max_length=2000)
    context_description: Optional[str] = None
    register: Optional[str] = None
    verb_form_used: Optional[str] = None
    tense_aspect_mood: Optional[str] = None
    audio_url: Optional[str] = None


class VerbExample(VerbExampleBase):
    id: int
    verb_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None


# Morphological Submission schemas
class MorphologicalSubmissionBase(BaseSchema):
    submission_type: str = Field(..., min_length=1, max_length=50)
    base_form: str = Field(..., min_length=1, max_length=200)
    english_meaning: str = Field(..., min_length=1, max_length=500)
    morphological_data: Dict[str, Any] = Field(...)
    context_notes: Optional[str] = None
    source_reference: Optional[str] = None
    confidence_level: int = Field(default=3, ge=1, le=5)


class MorphologicalSubmissionCreate(MorphologicalSubmissionBase):
    pass


class MorphologicalSubmissionUpdate(BaseSchema):
    english_meaning: Optional[str] = Field(None, min_length=1, max_length=500)
    morphological_data: Optional[Dict[str, Any]] = None
    context_notes: Optional[str] = None
    source_reference: Optional[str] = None
    confidence_level: Optional[int] = Field(None, ge=1, le=5)
    status: Optional[str] = None
    review_notes: Optional[str] = None


class MorphologicalSubmission(MorphologicalSubmissionBase):
    id: int
    created_by_id: int
    status: str
    reviewed_by_id: Optional[int] = None
    review_notes: Optional[str] = None
    created_at: datetime
    updated_at: Optional[datetime] = None


# Search and Filter schemas
class VerbSearch(BaseModel):
    query: Optional[str] = None
    english_meaning: Optional[str] = None
    verb_class: Optional[str] = None
    semantic_field: Optional[str] = None
    is_transitive: Optional[bool] = None
    is_stative: Optional[bool] = None
    register: Optional[str] = None
    tense: Optional[TenseType] = None
    aspect: Optional[AspectType] = None
    mood: Optional[MoodType] = None
    polarity: Optional[PolarityType] = None
    has_conjugations: Optional[bool] = None
    has_examples: Optional[bool] = None
    limit: int = Field(default=20, ge=1, le=100)
    offset: int = Field(default=0, ge=0)


class ConjugationSearch(BaseModel):
    verb_id: Optional[int] = None
    base_form: Optional[str] = None
    tense: Optional[TenseType] = None
    aspect: Optional[AspectType] = None
    mood: Optional[MoodType] = None
    polarity: Optional[PolarityType] = None
    person: Optional[PersonType] = None
    number: Optional[NumberType] = None
    is_common: Optional[bool] = None
    min_frequency: Optional[int] = Field(None, ge=1, le=5)
    max_frequency: Optional[int] = Field(None, ge=1, le=5)
    limit: int = Field(default=50, ge=1, le=200)
    offset: int = Field(default=0, ge=0)


# Validation schemas
class VerbValidation(BaseModel):
    base_form: str
    conjugation_forms: List[str]
    validation_errors: List[str] = Field(default_factory=list)
    warnings: List[str] = Field(default_factory=list)
    suggestions: List[str] = Field(default_factory=list)
    confidence_score: float = Field(ge=0.0, le=1.0)


# Response schemas
class VerbListResponse(BaseModel):
    verbs: List[VerbDetail]
    total: int
    limit: int
    offset: int
    has_next: bool
    has_previous: bool


class ConjugationListResponse(BaseModel):
    conjugations: List[VerbConjugation]
    total: int
    limit: int
    offset: int
    has_next: bool
    has_previous: bool


class MorphologicalSubmissionResponse(BaseModel):
    submission: MorphologicalSubmission
    validation: Optional[VerbValidation] = None
    similar_existing: Optional[List[Verb]] = None
    confidence_score: Optional[float] = None


# Export schemas
class VerbExport(BaseModel):
    base_form: str
    english_meaning: str
    verb_class: Optional[str] = None
    all_conjugations: Dict[str, List[Dict[str, str]]]  # Organized by tense/aspect/mood
    examples: List[Dict[str, str]]
    derived_forms: List[Dict[str, str]]
    pronunciation_guide: Optional[str] = None


class MorphologyExport(BaseModel):
    verbs: List[VerbExport]
    export_date: datetime
    total_count: int
    export_format: str = "json"