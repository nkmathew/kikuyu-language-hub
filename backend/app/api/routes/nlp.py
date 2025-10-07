"""
NLP API routes for advanced language processing features
"""
from typing import List, Dict, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
from ...models.user import User
from ...models.contribution import Contribution, DifficultyLevel
from ...services.nlp_service import NLPService
from ...core.security import get_current_user
from ...db.session import get_db
from ...core.cache import cache, CacheConfig

router = APIRouter(prefix="/nlp", tags=["nlp"])


class TranslationSuggestion(BaseModel):
    """Translation suggestion from NLP analysis"""
    source_text: str
    target_text: str
    similarity_score: float
    match_type: str
    context: Optional[str] = None


class QualityAnalysis(BaseModel):
    """Text quality analysis result"""
    source_analysis: Dict
    target_analysis: Dict
    translation_quality: Dict
    suggestions: List[Dict]


class ValidationResult(BaseModel):
    """Translation validation result"""
    is_valid: bool
    quality_score: float
    warnings: List[Dict]
    errors: List[str]


class DifficultyPrediction(BaseModel):
    """Difficulty level prediction"""
    suggested_level: str
    confidence: float
    analysis: Dict


class SubTranslationSuggestion(BaseModel):
    """Sub-translation suggestion"""
    source_word: str
    target_word: str
    word_position: int
    target_position: Optional[int]
    confidence_score: float
    difficulty_level: str
    morphology: Optional[Dict]
    auto_approved: bool


@router.post("/initialize", dependencies=[Depends(get_current_user)])
def initialize_nlp_models(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Initialize NLP models with existing data (Admin only)"""
    if current_user.role.value not in ['admin', 'moderator']:
        raise HTTPException(status_code=403, detail="Insufficient permissions")
    
    try:
        NLPService.initialize_nlp_models(db)
        
        # Clear related caches
        cache.delete_pattern("nlp_*")
        
        return {"message": "NLP models initialized successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to initialize NLP models: {str(e)}")


@router.get("/suggestions/similar", response_model=List[TranslationSuggestion])
def get_similar_translations(
    source_text: str = Query(..., description="Source text to find similarities for"),
    threshold: float = Query(0.7, ge=0.0, le=1.0, description="Minimum similarity threshold"),
    limit: int = Query(5, ge=1, le=20, description="Maximum number of suggestions"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get similar translations from translation memory"""
    try:
        suggestions = NLPService.find_similar_translations(
            source_text=source_text,
            threshold=threshold,
            limit=limit
        )
        
        return [
            TranslationSuggestion(
                source_text=suggestion['source_text'],
                target_text=suggestion['target_text'],
                similarity_score=suggestion['similarity_score'],
                match_type=suggestion['match_type'],
                context=suggestion.get('context')
            )
            for suggestion in suggestions
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get suggestions: {str(e)}")


class QualityAnalysisRequest(BaseModel):
    source_text: str = Field(..., description="Source text in Kikuyu")
    target_text: str = Field(..., description="Target text in English")

@router.post("/analyze/quality", response_model=QualityAnalysis)
def analyze_translation_quality(
    request: QualityAnalysisRequest,
    current_user: User = Depends(get_current_user)
):
    """Analyze quality of a translation pair"""
    try:
        analysis = NLPService.analyze_text_quality(request.source_text, request.target_text)
        return QualityAnalysis(**analysis)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to analyze quality: {str(e)}")


class ValidationRequest(BaseModel):
    source_text: str = Field(..., description="Source text in Kikuyu")
    target_text: str = Field(..., description="Target text in English")

@router.post("/validate", response_model=ValidationResult)
def validate_translation(
    request: ValidationRequest,
    current_user: User = Depends(get_current_user)
):
    """Validate a translation pair for common issues"""
    try:
        validation = NLPService.validate_translation_pair(request.source_text, request.target_text)
        return ValidationResult(**validation)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to validate translation: {str(e)}")


class DifficultyRequest(BaseModel):
    source_text: str = Field(..., description="Source text in Kikuyu")

@router.post("/difficulty/predict", response_model=DifficultyPrediction)
def predict_difficulty(
    request: DifficultyRequest,
    current_user: User = Depends(get_current_user)
):
    """Predict difficulty level for Kikuyu text"""
    try:
        level, confidence = NLPService.suggest_difficulty_level(request.source_text)
        
        # Get detailed analysis
        from ...utils.nlp import difficulty_analyzer
        analysis = difficulty_analyzer.analyze_difficulty(request.source_text)
        
        return DifficultyPrediction(
            suggested_level=level.value,
            confidence=confidence,
            analysis=analysis
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to predict difficulty: {str(e)}")


class SubTranslationRequest(BaseModel):
    contribution_id: int = Field(..., description="Contribution ID")
    auto_approve_threshold: float = Field(0.9, ge=0.0, le=1.0, description="Auto-approval threshold")

@router.post("/sub-translations/generate", response_model=List[SubTranslationSuggestion])
def generate_sub_translations(
    request: SubTranslationRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Generate sub-word translations for a contribution"""
    # Check if contribution exists and user has access
    contribution = db.query(Contribution).filter(
        Contribution.id == request.contribution_id
    ).first()
    
    if not contribution:
        raise HTTPException(status_code=404, detail="Contribution not found")
    
    # Only allow access to own contributions or moderator/admin
    if (contribution.created_by_id != current_user.id and 
        current_user.role.value not in ['admin', 'moderator']):
        raise HTTPException(status_code=403, detail="Insufficient permissions")
    
    try:
        suggestions = NLPService.generate_sub_translations(
            db=db,
            contribution_id=request.contribution_id,
            auto_approve_threshold=request.auto_approve_threshold
        )
        
        return [
            SubTranslationSuggestion(**suggestion)
            for suggestion in suggestions
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to generate sub-translations: {str(e)}")


@router.post("/memory/update", dependencies=[Depends(get_current_user)])
def update_translation_memory(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Update translation memory with new approved translations"""
    if current_user.role.value not in ['admin', 'moderator']:
        raise HTTPException(status_code=403, detail="Insufficient permissions")
    
    try:
        count = NLPService.update_translation_memory(db)
        
        # Clear related caches
        cache.delete_pattern("nlp_*")
        
        return {
            "message": f"Translation memory updated with {count} new translations",
            "new_translations": count
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to update translation memory: {str(e)}")


@router.get("/corpus/analyze")
def analyze_corpus(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get corpus-wide linguistic analysis and statistics"""
    try:
        analysis = NLPService.analyze_corpus_statistics(db)
        return analysis
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to analyze corpus: {str(e)}")


@router.get("/tokenize")
def tokenize_text(
    text: str = Query(..., description="Text to tokenize"),
    current_user: User = Depends(get_current_user)
):
    """Tokenize Kikuyu text and return linguistic analysis"""
    try:
        from ...utils.nlp import kikuyu_tokenizer
        
        words = kikuyu_tokenizer.tokenize(text)
        
        return {
            "text": text,
            "word_count": len(words),
            "words": [
                {
                    "text": word.text,
                    "normalized": word.normalized,
                    "tokens": word.tokens,
                    "syllables": word.syllables,
                    "tone_pattern": word.tone_pattern,
                    "morphology": word.morphology
                }
                for word in words
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to tokenize text: {str(e)}")


@router.get("/spell-check")
def check_spelling(
    text: str = Query(..., description="Text to spell-check"),
    current_user: User = Depends(get_current_user)
):
    """Check spelling of Kikuyu text"""
    try:
        from ...utils.nlp import spell_checker
        
        errors = spell_checker.check_text(text)
        
        return {
            "text": text,
            "error_count": len(errors),
            "errors": errors,
            "is_correct": len(errors) == 0
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to check spelling: {str(e)}")


@router.get("/stats")
def get_nlp_stats(
    current_user: User = Depends(get_current_user)
):
    """Get NLP system statistics"""
    try:
        from ...utils.nlp import translation_memory, spell_checker
        
        return {
            "translation_memory": {
                "total_translations": len(translation_memory.memory),
                "index_size": {
                    "source_terms": len(translation_memory.source_index),
                    "target_terms": len(translation_memory.target_index)
                }
            },
            "spell_checker": {
                "dictionary_size": len(spell_checker.dictionary),
                "word_frequency_entries": len(spell_checker.word_frequency),
                "common_errors": len(spell_checker.common_errors)
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get NLP stats: {str(e)}")