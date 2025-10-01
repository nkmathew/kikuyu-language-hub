from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional
from ..models.sub_translation import DifficultyLevel
from .category import CategoryResponse


class SubTranslationBase(BaseModel):
    source_word: str = Field(..., min_length=1, max_length=200)
    target_word: str = Field(..., min_length=1, max_length=200)
    context: Optional[str] = None
    word_position: int = Field(..., ge=0)
    difficulty_level: DifficultyLevel = DifficultyLevel.BEGINNER
    confidence_score: float = Field(1.0, ge=0.0, le=1.0)
    category_id: Optional[int] = None


class SubTranslationCreate(SubTranslationBase):
    parent_contribution_id: int


class SubTranslationUpdate(BaseModel):
    source_word: Optional[str] = Field(None, min_length=1, max_length=200)
    target_word: Optional[str] = Field(None, min_length=1, max_length=200)
    context: Optional[str] = None
    difficulty_level: Optional[DifficultyLevel] = None
    confidence_score: Optional[float] = Field(None, ge=0.0, le=1.0)
    category_id: Optional[int] = None


class SubTranslationResponse(SubTranslationBase):
    id: int
    parent_contribution_id: int
    created_by_id: int
    created_at: datetime
    updated_at: datetime
    category: Optional[CategoryResponse] = None
    
    class Config:
        from_attributes = True


class SubTranslationBatch(BaseModel):
    """For creating multiple sub-translations at once"""
    parent_contribution_id: int
    sub_translations: list[SubTranslationBase]


class WordSegmentation(BaseModel):
    """Response for word segmentation endpoint"""
    original_text: str
    segments: list[dict] = Field(default_factory=list)
    suggested_translations: dict = Field(default_factory=dict)
    
    class Config:
        schema_extra = {
            "example": {
                "original_text": "Nĩngwenda mũtumia mũringa",
                "segments": [
                    {"word": "Nĩngwenda", "position": 0, "suggested_translation": "I want"},
                    {"word": "mũtumia", "position": 1, "suggested_translation": "woman"},
                    {"word": "mũringa", "position": 2, "suggested_translation": "beautiful"}
                ],
                "suggested_translations": {
                    "Nĩngwenda": "I want",
                    "mũtumia": "woman", 
                    "mũringa": "beautiful"
                }
            }
        }


class SubTranslationStats(BaseModel):
    """Statistics for sub-translations"""
    total_sub_translations: int
    by_difficulty: dict[str, int]
    by_category: dict[str, int]
    average_confidence: float
    
    class Config:
        from_attributes = True