from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from ..models.contribution import ContributionStatus, DifficultyLevel
from .user import UserResponse
from .category import CategoryResponse
from .sub_translation import SubTranslationResponse


class ContributionBase(BaseModel):
    source_text: str
    target_text: str
    language: str = "kikuyu"
    difficulty_level: DifficultyLevel = DifficultyLevel.BEGINNER
    context_notes: Optional[str] = None
    cultural_notes: Optional[str] = None
    pronunciation_guide: Optional[str] = None
    usage_examples: Optional[str] = None  # JSON string
    is_phrase: bool = False


class ContributionCreate(ContributionBase):
    category_ids: List[int] = []  # Categories to assign to this contribution


class ContributionUpdate(BaseModel):
    source_text: Optional[str] = None
    target_text: Optional[str] = None
    language: Optional[str] = None
    difficulty_level: Optional[DifficultyLevel] = None
    context_notes: Optional[str] = None
    cultural_notes: Optional[str] = None
    pronunciation_guide: Optional[str] = None
    usage_examples: Optional[str] = None
    is_phrase: Optional[bool] = None
    category_ids: Optional[List[int]] = None
    reason: Optional[str] = None


class ContributionWithSubTranslations(ContributionCreate):
    """For creating contributions with sub-translations in one request"""
    auto_segment: bool = False  # Whether to automatically segment into words
    sub_translations: List[Dict[str, Any]] = []  # Manual sub-translations


class ContributionResponse(ContributionBase):
    id: int
    status: ContributionStatus
    created_by_id: int
    quality_score: float
    has_sub_translations: bool
    word_count: int
    created_at: datetime
    updated_at: datetime
    
    # Relationships
    created_by: Optional[UserResponse] = None
    categories: List[CategoryResponse] = []
    sub_translations: List[SubTranslationResponse] = []
    audit_log: Optional[Dict[str, Any]] = None

    class Config:
        from_attributes = True


class ContributionListResponse(BaseModel):
    """Simplified response for list views"""
    id: int
    source_text: str
    target_text: str
    status: ContributionStatus
    difficulty_level: DifficultyLevel
    is_phrase: bool
    word_count: int
    quality_score: float
    categories: List[str] = []  # Just category names
    created_by: Optional[str] = None  # Just username
    created_at: datetime
    
    class Config:
        from_attributes = True


class ContributionExport(BaseModel):
    """Export format for flashcard app compatibility"""
    english: str
    kikuyu: str
    category: Optional[str] = None
    difficulty: str = "beginner"
    pronunciation: Optional[str] = None
    cultural_notes: Optional[str] = None
    usage_examples: List[str] = []
    
    class Config:
        schema_extra = {
            "example": {
                "english": "Hello",
                "kikuyu": "Wĩrĩ",
                "category": "greetings",
                "difficulty": "beginner",
                "pronunciation": "wee-ree",
                "cultural_notes": "Standard greeting used any time of day",
                "usage_examples": ["Wĩrĩ mũciĩ", "Wĩrĩ mwega"]
            }
        }


class ContributionFilter(BaseModel):
    """Filtering options for contributions"""
    status: Optional[ContributionStatus] = None
    difficulty_level: Optional[DifficultyLevel] = None
    category_id: Optional[int] = None
    created_by_id: Optional[int] = None
    is_phrase: Optional[bool] = None
    has_sub_translations: Optional[bool] = None
    min_quality_score: Optional[float] = Field(None, ge=0.0, le=5.0)
    search_text: Optional[str] = None  # Search in source_text or target_text
    
    
class BulkContributionOperation(BaseModel):
    """For bulk operations on contributions"""
    contribution_ids: List[int]
    action: str  # "approve", "reject", "assign_category", "set_difficulty"
    parameters: Dict[str, Any] = {}  # Additional parameters for the action