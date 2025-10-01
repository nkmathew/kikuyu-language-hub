from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from ...db.session import get_db
from ...models.user import User
from ...models.sub_translation import DifficultyLevel
from ...services.sub_translation_service import SubTranslationService
from ...schemas.sub_translation import (
    SubTranslationCreate, SubTranslationUpdate, SubTranslationResponse,
    SubTranslationBatch, WordSegmentation, SubTranslationStats
)
from ...core.security import get_current_user

router = APIRouter(prefix="/sub-translations", tags=["sub-translations"])


@router.post("/", response_model=SubTranslationResponse)
def create_sub_translation(
    sub_translation_data: SubTranslationCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Create a new sub-translation"""
    return SubTranslationService.create_sub_translation(
        db=db, 
        sub_translation_data=sub_translation_data, 
        user=current_user
    )


@router.post("/batch", response_model=List[SubTranslationResponse])
def create_sub_translations_batch(
    batch_data: SubTranslationBatch,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Create multiple sub-translations for a contribution"""
    return SubTranslationService.create_sub_translations_batch(
        db=db,
        batch_data=batch_data,
        user=current_user
    )


@router.get("/contribution/{contribution_id}", response_model=List[SubTranslationResponse])
def get_sub_translations_by_contribution(
    contribution_id: int,
    db: Session = Depends(get_db)
):
    """Get all sub-translations for a specific contribution"""
    return SubTranslationService.get_sub_translations_by_contribution(
        db=db, 
        contribution_id=contribution_id
    )


@router.get("/{sub_translation_id}", response_model=SubTranslationResponse)
def get_sub_translation(
    sub_translation_id: int,
    db: Session = Depends(get_db)
):
    """Get a specific sub-translation by ID"""
    sub_translation = SubTranslationService.get_sub_translation_by_id(
        db=db, 
        sub_translation_id=sub_translation_id
    )
    if not sub_translation:
        raise HTTPException(status_code=404, detail="Sub-translation not found")
    return sub_translation


@router.put("/{sub_translation_id}", response_model=SubTranslationResponse)
def update_sub_translation(
    sub_translation_id: int,
    update_data: SubTranslationUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update a sub-translation"""
    sub_translation = SubTranslationService.update_sub_translation(
        db=db,
        sub_translation_id=sub_translation_id,
        update_data=update_data
    )
    if not sub_translation:
        raise HTTPException(status_code=404, detail="Sub-translation not found")
    return sub_translation


@router.delete("/{sub_translation_id}")
def delete_sub_translation(
    sub_translation_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete a sub-translation"""
    success = SubTranslationService.delete_sub_translation(
        db=db, 
        sub_translation_id=sub_translation_id
    )
    if not success:
        raise HTTPException(status_code=404, detail="Sub-translation not found")
    return {"message": "Sub-translation deleted successfully"}


@router.post("/segment", response_model=WordSegmentation)
def segment_text(
    text: str,
    current_user: User = Depends(get_current_user)
):
    """Automatically segment text into words with translation suggestions"""
    if not text or not text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")
    
    return SubTranslationService.segment_text(text.strip())


@router.get("/stats/overview", response_model=SubTranslationStats)
def get_sub_translation_stats(
    contribution_id: Optional[int] = Query(None, description="Filter by contribution ID"),
    db: Session = Depends(get_db)
):
    """Get statistics for sub-translations"""
    return SubTranslationService.get_sub_translation_stats(
        db=db, 
        contribution_id=contribution_id
    )


@router.get("/search/", response_model=List[SubTranslationResponse])
def search_sub_translations(
    q: str = Query(..., min_length=1, description="Search query"),
    difficulty_level: Optional[DifficultyLevel] = Query(None, description="Filter by difficulty"),
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    limit: int = Query(50, ge=1, le=200, description="Maximum results to return"),
    db: Session = Depends(get_db)
):
    """Search sub-translations by source or target word"""
    return SubTranslationService.search_sub_translations(
        db=db,
        search_query=q,
        difficulty_level=difficulty_level,
        category_id=category_id,
        limit=limit
    )


@router.get("/popular/words", response_model=List[Dict[str, Any]])
def get_popular_translations(
    limit: int = Query(20, ge=1, le=100, description="Number of popular translations to return"),
    db: Session = Depends(get_db)
):
    """Get the most frequently translated words"""
    return SubTranslationService.get_popular_translations(db=db, limit=limit)