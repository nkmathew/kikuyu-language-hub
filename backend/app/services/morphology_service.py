"""
Business logic service for Kikuyu verb morphology system
"""
from typing import List, Optional, Dict, Any, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, func
import json
import logging

from app.models.morphology import (
    Verb, VerbConjugation, NounForm, VerbExample, 
    MorphologicalSubmission, MorphologicalPattern, WordClass
)
from app.models.user import User
from app.schemas.morphology import (
    VerbCreate, VerbUpdate, VerbConjugationCreate, VerbConjugationUpdate,
    MorphologicalSubmissionCreate, VerbValidation
)
from app.services.nlp_service import NLPService
from app.core.cache import cache_manager

logger = logging.getLogger(__name__)


class MorphologyService:
    """Service for handling verb morphology operations"""
    
    @staticmethod
    def create_verb(db: Session, verb_data: VerbCreate, user_id: int) -> Verb:
        """Create a new verb with its conjugations and derived forms"""
        try:
            # Create the verb
            verb = Verb(
                base_form=verb_data.base_form,
                english_meaning=verb_data.english_meaning,
                word_class_id=verb_data.word_class_id,
                verb_class=verb_data.verb_class,
                consonant_pattern=verb_data.consonant_pattern,
                is_transitive=verb_data.is_transitive,
                is_stative=verb_data.is_stative,
                semantic_field=verb_data.semantic_field,
                register=verb_data.register,
                pronunciation_guide=verb_data.pronunciation_guide,
                audio_url=verb_data.audio_url
            )
            
            db.add(verb)
            db.flush()  # Get the ID without committing
            
            # Create conjugations if provided
            if verb_data.conjugations:
                for conj_set in verb_data.conjugations:
                    for form in conj_set.forms:
                        conjugation = VerbConjugation(
                            verb_id=verb.id,
                            tense=conj_set.tense,
                            aspect=conj_set.aspect,
                            mood=conj_set.mood,
                            polarity=conj_set.polarity,
                            person=form.person,
                            number=form.number,
                            object_person=form.object_person,
                            object_number=form.object_number,
                            has_object=form.has_object,
                            conjugated_form=form.form,
                            morphological_breakdown=[b.dict() for b in form.breakdown],
                            usage_context=form.usage_context,
                            frequency=form.frequency,
                            is_common=form.is_common,
                            audio_url=form.audio_url
                        )
                        db.add(conjugation)
            
            # Create examples if provided
            if verb_data.examples:
                for example in verb_data.examples:
                    verb_example = VerbExample(
                        verb_id=verb.id,
                        kikuyu_sentence=example.kikuyu,
                        english_translation=example.english,
                        context_description=example.context_description,
                        register=example.register,
                        verb_form_used=example.verb_form_used,
                        tense_aspect_mood=example.tense_aspect_mood,
                        audio_url=example.audio_url
                    )
                    db.add(verb_example)
            
            db.commit()
            db.refresh(verb)
            
            # Clear relevant cache
            cache_manager.delete_pattern("verbs:list:*")
            cache_manager.delete_pattern("verbs:stats")
            
            logger.info(f"Created verb '{verb.base_form}' with {len(verb_data.conjugations or [])} conjugation sets")
            return verb
            
        except Exception as e:
            db.rollback()
            logger.error(f"Error creating verb '{verb_data.base_form}': {e}")
            raise
    
    @staticmethod
    def update_verb(db: Session, verb: Verb, verb_data: VerbUpdate, user_id: int) -> Verb:
        """Update verb information"""
        try:
            # Update basic verb information
            update_data = verb_data.dict(exclude_unset=True, exclude={
                'conjugations', 'derived_forms', 'examples'
            })
            
            for field, value in update_data.items():
                setattr(verb, field, value)
            
            # Update conjugations if provided
            if verb_data.conjugations is not None:
                # Remove existing conjugations
                db.query(VerbConjugation).filter(VerbConjugation.verb_id == verb.id).delete()
                
                # Add new conjugations
                for conj_set in verb_data.conjugations:
                    for form in conj_set.forms:
                        conjugation = VerbConjugation(
                            verb_id=verb.id,
                            tense=conj_set.tense,
                            aspect=conj_set.aspect,
                            mood=conj_set.mood,
                            polarity=conj_set.polarity,
                            person=form.person,
                            number=form.number,
                            object_person=form.object_person,
                            object_number=form.object_number,
                            has_object=form.has_object,
                            conjugated_form=form.form,
                            morphological_breakdown=[b.dict() for b in form.breakdown],
                            usage_context=form.usage_context,
                            frequency=form.frequency,
                            is_common=form.is_common,
                            audio_url=form.audio_url
                        )
                        db.add(conjugation)
            
            # Update examples if provided
            if verb_data.examples is not None:
                # Remove existing examples
                db.query(VerbExample).filter(VerbExample.verb_id == verb.id).delete()
                
                # Add new examples
                for example in verb_data.examples:
                    verb_example = VerbExample(
                        verb_id=verb.id,
                        kikuyu_sentence=example.kikuyu,
                        english_translation=example.english,
                        context_description=example.context_description,
                        register=example.register,
                        verb_form_used=example.verb_form_used,
                        tense_aspect_mood=example.tense_aspect_mood,
                        audio_url=example.audio_url
                    )
                    db.add(verb_example)
            
            db.commit()
            db.refresh(verb)
            
            # Clear relevant cache
            cache_manager.delete_pattern(f"verb:detail:{verb.id}:*")
            cache_manager.delete_pattern("verbs:list:*")
            
            logger.info(f"Updated verb '{verb.base_form}'")
            return verb
            
        except Exception as e:
            db.rollback()
            logger.error(f"Error updating verb '{verb.base_form}': {e}")
            raise
    
    @staticmethod
    def create_conjugation(db: Session, conjugation_data: VerbConjugationCreate, user_id: int) -> VerbConjugation:
        """Add a new conjugation to an existing verb"""
        try:
            # Verify verb exists
            verb = db.query(Verb).filter(Verb.id == conjugation_data.verb_id).first()
            if not verb:
                raise ValueError(f"Verb with ID {conjugation_data.verb_id} not found")
            
            conjugation = VerbConjugation(
                verb_id=conjugation_data.verb_id,
                tense=conjugation_data.tense,
                aspect=conjugation_data.aspect,
                mood=conjugation_data.mood,
                polarity=conjugation_data.polarity,
                person=conjugation_data.person,
                number=conjugation_data.number,
                object_person=conjugation_data.object_person,
                object_number=conjugation_data.object_number,
                has_object=conjugation_data.has_object,
                conjugated_form=conjugation_data.conjugated_form,
                morphological_breakdown=conjugation_data.morphological_breakdown,
                usage_context=conjugation_data.usage_context,
                frequency=conjugation_data.frequency,
                is_common=conjugation_data.is_common,
                audio_url=conjugation_data.audio_url
            )
            
            db.add(conjugation)
            db.commit()
            db.refresh(conjugation)
            
            # Clear relevant cache
            cache_manager.delete_pattern(f"verb:detail:{conjugation_data.verb_id}:*")
            cache_manager.delete_pattern("conjugations:list:*")
            
            logger.info(f"Created conjugation '{conjugation_data.conjugated_form}' for verb '{verb.base_form}'")
            return conjugation
            
        except Exception as e:
            db.rollback()
            logger.error(f"Error creating conjugation: {e}")
            raise
    
    @staticmethod
    def validate_submission(submission_data: MorphologicalSubmissionCreate) -> VerbValidation:
        """Validate a morphological submission using NLP analysis"""
        try:
            errors = []
            warnings = []
            suggestions = []
            confidence_score = 1.0
            
            # Basic validation
            if not submission_data.base_form or len(submission_data.base_form.strip()) < 1:
                errors.append("Base form cannot be empty")
                confidence_score -= 0.3
            
            if not submission_data.english_meaning or len(submission_data.english_meaning.strip()) < 1:
                errors.append("English meaning cannot be empty")
                confidence_score -= 0.3
            
            if submission_data.submission_type == "verb":
                confidence_score += MorphologyService._validate_verb_submission(
                    submission_data, errors, warnings, suggestions
                )
            
            # Ensure confidence score is within bounds
            confidence_score = max(0.0, min(1.0, confidence_score))
            
            return VerbValidation(
                base_form=submission_data.base_form,
                conjugation_forms=[],
                validation_errors=errors,
                warnings=warnings,
                suggestions=suggestions,
                confidence_score=confidence_score
            )
            
        except Exception as e:
            logger.error(f"Error validating submission: {e}")
            return VerbValidation(
                base_form=submission_data.base_form,
                conjugation_forms=[],
                validation_errors=["Validation error occurred"],
                warnings=[],
                suggestions=[],
                confidence_score=0.0
            )
    
    @staticmethod
    def _validate_verb_submission(submission_data: MorphologicalSubmissionCreate, 
                                 errors: List[str], warnings: List[str], 
                                 suggestions: List[str]) -> float:
        """Validate verb-specific submission data"""
        confidence_boost = 0.0
        morph_data = submission_data.morphological_data
        
        # Check conjugations completeness
        conjugations = morph_data.get("conjugations", [])
        if not conjugations:
            warnings.append("No conjugations provided - consider adding basic forms")
            confidence_boost -= 0.2
        else:
            # Check for present tense conjugations (most common)
            has_present = any(
                conj.get("tense") == "present" and conj.get("aspect") == "simple"
                for conj in conjugations
            )
            if has_present:
                confidence_boost += 0.2
            else:
                suggestions.append("Consider adding present simple tense conjugations")
            
            # Check conjugation completeness for each tense
            for conj_set in conjugations:
                forms = conj_set.get("forms", [])
                if len(forms) < 6:  # Expecting 6 basic forms (3 persons x 2 numbers)
                    warnings.append(f"Incomplete conjugation set for {conj_set.get('tense', 'unknown')} tense")
        
        # Check derived forms
        derived_forms = morph_data.get("derived_forms", [])
        if not derived_forms:
            suggestions.append("Consider adding derived nouns (agent, patient, abstract forms)")
        else:
            confidence_boost += 0.1
        
        # Check examples
        examples = morph_data.get("examples", [])
        if not examples:
            warnings.append("No example sentences provided - examples help with context")
            confidence_boost -= 0.1
        elif len(examples) < 2:
            suggestions.append("Add more example sentences to show different contexts")
        else:
            confidence_boost += 0.1
        
        return confidence_boost
    
    @staticmethod
    def find_similar_verbs(db: Session, base_form: str, english_meaning: str, limit: int = 5) -> List[Verb]:
        """Find similar existing verbs"""
        try:
            # Search by base form similarity and meaning similarity
            similar_by_form = db.query(Verb).filter(
                Verb.base_form.ilike(f"%{base_form}%")
            ).limit(limit).all()
            
            similar_by_meaning = db.query(Verb).filter(
                Verb.english_meaning.ilike(f"%{english_meaning}%")
            ).limit(limit).all()
            
            # Combine and deduplicate
            all_similar = similar_by_form + similar_by_meaning
            unique_similar = []
            seen_ids = set()
            
            for verb in all_similar:
                if verb.id not in seen_ids:
                    unique_similar.append(verb)
                    seen_ids.add(verb.id)
            
            return unique_similar[:limit]
            
        except Exception as e:
            logger.error(f"Error finding similar verbs: {e}")
            return []
    
    @staticmethod
    def approve_submission(db: Session, submission: MorphologicalSubmission, 
                          reviewer_id: int, review_notes: Optional[str] = None) -> Dict[str, Any]:
        """Approve a morphological submission and create the actual verb/form"""
        try:
            morph_data = submission.morphological_data
            
            if submission.submission_type == "verb":
                # Create verb from submission
                verb_data = VerbCreate(
                    base_form=submission.base_form,
                    english_meaning=submission.english_meaning,
                    verb_class=morph_data.get("verb_class"),
                    consonant_pattern=morph_data.get("consonant_pattern"),
                    semantic_field=morph_data.get("semantic_field"),
                    register=morph_data.get("register"),
                    pronunciation_guide=morph_data.get("pronunciation_guide"),
                    audio_url=morph_data.get("audio_url")
                )
                
                # Process conjugations
                if "conjugations" in morph_data:
                    verb_data.conjugations = []
                    for conj_set in morph_data["conjugations"]:
                        conj_set_obj = {
                            "tense": conj_set.get("tense"),
                            "aspect": conj_set.get("aspect"),
                            "mood": conj_set.get("mood"),
                            "polarity": conj_set.get("polarity"),
                            "forms": []
                        }
                        
                        for form_data in conj_set.get("forms", []):
                            form_obj = {
                                "person": form_data.get("person"),
                                "number": form_data.get("number"),
                                "form": form_data.get("form"),
                                "object_person": form_data.get("object_person"),
                                "object_number": form_data.get("object_number"),
                                "has_object": form_data.get("has_object", False),
                                "usage_context": form_data.get("usage_context"),
                                "frequency": form_data.get("frequency", 1),
                                "is_common": form_data.get("is_common", False),
                                "audio_url": form_data.get("audio_url"),
                                "breakdown": form_data.get("breakdown", [])
                            }
                            conj_set_obj["forms"].append(form_obj)
                        
                        verb_data.conjugations.append(conj_set_obj)
                
                # Process examples
                if "examples" in morph_data:
                    verb_data.examples = []
                    for example_data in morph_data["examples"]:
                        example_obj = {
                            "kikuyu": example_data.get("kikuyu"),
                            "english": example_data.get("english"),
                            "context_description": example_data.get("context_description"),
                            "register": example_data.get("register"),
                            "verb_form_used": example_data.get("verb_form_used"),
                            "tense_aspect_mood": example_data.get("tense_aspect_mood"),
                            "audio_url": example_data.get("audio_url")
                        }
                        verb_data.examples.append(example_obj)
                
                # Create the verb
                verb = MorphologyService.create_verb(db, verb_data, submission.created_by_id)
                
                # Update submission status
                submission.status = "approved"
                submission.reviewed_by_id = reviewer_id
                submission.review_notes = review_notes
                db.commit()
                
                # Clear cache
                cache_manager.delete_pattern("verbs:*")
                cache_manager.delete_pattern("morphology:*")
                
                logger.info(f"Approved submission for verb '{submission.base_form}' - created verb ID {verb.id}")
                
                return {
                    "type": "verb",
                    "id": verb.id,
                    "base_form": verb.base_form
                }
            
            else:
                # Handle other submission types (nouns, adjectives, etc.)
                # TODO: Implement other submission types
                raise ValueError(f"Submission type '{submission.submission_type}' not yet supported")
            
        except Exception as e:
            db.rollback()
            logger.error(f"Error approving submission {submission.id}: {e}")
            raise
    
    @staticmethod
    def process_submission_background(submission_id: int, confidence_score: float):
        """Background task to process submission (e.g., automatic approval for high confidence)"""
        try:
            # This would run in the background
            # Auto-approve submissions with very high confidence scores
            if confidence_score >= 0.95:
                # TODO: Implement auto-approval logic
                pass
        except Exception as e:
            logger.error(f"Error in background processing of submission {submission_id}: {e}")
    
    @staticmethod
    def get_verb_by_form(db: Session, conjugated_form: str) -> Optional[Verb]:
        """Find verb by any of its conjugated forms"""
        try:
            conjugation = db.query(VerbConjugation).filter(
                VerbConjugation.conjugated_form == conjugated_form
            ).first()
            
            if conjugation:
                return db.query(Verb).filter(Verb.id == conjugation.verb_id).first()
            
            return None
            
        except Exception as e:
            logger.error(f"Error finding verb by form '{conjugated_form}': {e}")
            return None
    
    @staticmethod
    def analyze_verb_pattern(db: Session, verb_id: int) -> Dict[str, Any]:
        """Analyze the morphological pattern of a verb"""
        try:
            verb = db.query(Verb).filter(Verb.id == verb_id).first()
            if not verb:
                return {}
            
            conjugations = db.query(VerbConjugation).filter(VerbConjugation.verb_id == verb_id).all()
            
            analysis = {
                "base_form": verb.base_form,
                "verb_class": verb.verb_class,
                "syllable_count": NLPService.count_syllables(verb.base_form),
                "consonant_pattern": verb.consonant_pattern,
                "patterns_found": {},
                "irregularities": []
            }
            
            # Analyze conjugation patterns
            for conj in conjugations:
                pattern_key = f"{conj.tense}_{conj.aspect}_{conj.polarity}"
                if pattern_key not in analysis["patterns_found"]:
                    analysis["patterns_found"][pattern_key] = {
                        "prefixes": set(),
                        "suffixes": set(),
                        "infixes": set(),
                        "stem_changes": set()
                    }
                
                # Analyze morphology
                if conj.morphological_breakdown:
                    for morpheme in conj.morphological_breakdown:
                        if morpheme.get("type") == "prefix":
                            analysis["patterns_found"][pattern_key]["prefixes"].add(morpheme.get("morpheme", ""))
                        elif morpheme.get("type") == "suffix":
                            analysis["patterns_found"][pattern_key]["suffixes"].add(morpheme.get("morpheme", ""))
                        elif morpheme.get("type") == "infix":
                            analysis["patterns_found"][pattern_key]["infixes"].add(morpheme.get("morpheme", ""))
            
            # Convert sets to lists for JSON serialization
            for pattern in analysis["patterns_found"]:
                for key in analysis["patterns_found"][pattern]:
                    analysis["patterns_found"][pattern][key] = list(analysis["patterns_found"][pattern][key])
            
            return analysis
            
        except Exception as e:
            logger.error(f"Error analyzing verb pattern for ID {verb_id}: {e}")
            return {}
    
    @staticmethod
    def generate_conjugation_exercise(db: Session, verb_id: int, tense: str, aspect: str) -> Dict[str, Any]:
        """Generate a conjugation exercise for a specific verb"""
        try:
            verb = db.query(Verb).filter(Verb.id == verb_id).first()
            if not verb:
                return {}
            
            # Get the target conjugations
            target_conjugations = db.query(VerbConjugation).filter(
                and_(
                    VerbConjugation.verb_id == verb_id,
                    VerbConjugation.tense == tense,
                    VerbConjugation.aspect == aspect
                )
            ).all()
            
            if not target_conjugations:
                return {"error": f"No conjugations found for {tense} {aspect}"}
            
            # Create exercise with blanks
            exercise = {
                "verb": verb.base_form,
                "english_meaning": verb.english_meaning,
                "tense": tense,
                "aspect": aspect,
                "instructions": f"Complete the {tense} {aspect} conjugation of '{verb.base_form}'",
                "questions": []
            }
            
            for conj in target_conjugations:
                question = {
                    "person": conj.person,
                    "number": conj.number,
                    "prompt": f"{conj.person.capitalize()} person {conj.number}:",
                    "answer": conj.conjugated_form,
                    "hint": f"Subject marker: " + MorphologyService._get_subject_hint(conj.person, conj.number)
                }
                exercise["questions"].append(question)
            
            return exercise
            
        except Exception as e:
            logger.error(f"Error generating conjugation exercise: {e}")
            return {"error": "Failed to generate exercise"}
    
    @staticmethod
    def _get_subject_hint(person: str, number: str) -> str:
        """Get subject marker hint for conjugation exercises"""
        hints = {
            ("first", "singular"): "ni- (I)",
            ("second", "singular"): "wu- (you)", 
            ("third", "singular"): "a- (he/she)",
            ("first", "plural"): "thu- (we)",
            ("second", "plural"): "mu- (you plural)",
            ("third", "plural"): "ma- (they)"
        }
        return hints.get((person, number), "Unknown")