from typing import Dict, Any, List, Optional
from fastapi import APIRouter, Depends, Query
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func
import json
from ...models.contribution import ContributionStatus, Contribution
from ...models.category import Category
from ...services.contribution_service import ContributionService
from ...schemas.contribution import ContributionExport
from ...db.session import get_db

router = APIRouter(prefix="/export", tags=["export"])


@router.get("/translations.json")
def export_translations_legacy(db: Session = Depends(get_db)):
    """Legacy export format for backward compatibility"""
    # Get all approved contributions
    approved_contributions = ContributionService.get_contributions(
        db, status=ContributionStatus.APPROVED, limit=10000
    )
    
    # Transform to simple key-value format
    translations = {}
    for contribution in approved_contributions:
        # Use source text as key, target text as value
        translations[contribution.source_text] = contribution.target_text
    
    response_data = {
        "translations": translations,
        "count": len(translations),
        "language": "kikuyu"
    }
    
    # Add caching headers
    headers = {
        "Cache-Control": "public, max-age=3600",  # Cache for 1 hour
        "ETag": f'"{hash(str(sorted(translations.items())))}"'
    }
    
    return JSONResponse(content=response_data, headers=headers)


@router.get("/flashcards.json", response_model=List[ContributionExport])
def export_for_flashcards(
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    difficulty: Optional[str] = Query(None, description="Filter by difficulty level"),
    min_quality_score: Optional[float] = Query(0.0, description="Minimum quality score"),
    include_sub_translations: bool = Query(False, description="Include word-level translations"),
    db: Session = Depends(get_db)
):
    """Export translations in flashcard app compatible format"""
    query = db.query(Contribution).options(
        joinedload(Contribution.categories),
        joinedload(Contribution.sub_translations)
    ).filter(
        Contribution.status == ContributionStatus.APPROVED,
        Contribution.quality_score >= min_quality_score
    )
    
    # Apply filters
    if category_id:
        query = query.join(Contribution.categories).filter(Category.id == category_id)
    
    if difficulty:
        query = query.filter(Contribution.difficulty_level == difficulty)
    
    contributions = query.order_by(Contribution.difficulty_level, Contribution.source_text).all()
    
    # Transform to flashcard format
    flashcard_data = []
    for contribution in contributions:
        # Get primary category name
        category_name = None
        if contribution.categories:
            category_name = contribution.categories[0].slug
        
        # Parse usage examples
        usage_examples = []
        if contribution.usage_examples:
            try:
                usage_examples = json.loads(contribution.usage_examples)
                if not isinstance(usage_examples, list):
                    usage_examples = [str(usage_examples)]
            except (json.JSONDecodeError, TypeError):
                usage_examples = [contribution.usage_examples] if contribution.usage_examples else []
        
        flashcard_item = ContributionExport(
            english=contribution.target_text,
            kikuyu=contribution.source_text,
            category=category_name,
            difficulty=contribution.difficulty_level.value if contribution.difficulty_level else "beginner",
            pronunciation=contribution.pronunciation_guide,
            cultural_notes=contribution.cultural_notes,
            usage_examples=usage_examples
        )
        
        flashcard_data.append(flashcard_item)
        
        # Add sub-translations if requested
        if include_sub_translations and contribution.sub_translations:
            for sub_trans in contribution.sub_translations:
                sub_category = sub_trans.category.slug if sub_trans.category else category_name
                
                sub_flashcard = ContributionExport(
                    english=sub_trans.target_word,
                    kikuyu=sub_trans.source_word,
                    category=sub_category,
                    difficulty=sub_trans.difficulty_level.value,
                    pronunciation=None,
                    cultural_notes=sub_trans.context,
                    usage_examples=[]
                )
                
                flashcard_data.append(sub_flashcard)
    
    return flashcard_data


@router.get("/corpus/full.json")
def export_full_corpus(
    format_version: str = Query("v2", description="Export format version"),
    db: Session = Depends(get_db)
):
    """Export complete corpus with all metadata for advanced applications"""
    contributions = db.query(Contribution).options(
        joinedload(Contribution.categories),
        joinedload(Contribution.sub_translations),
        joinedload(Contribution.created_by)
    ).filter(Contribution.status == ContributionStatus.APPROVED).all()
    
    # Get category statistics
    category_stats = db.query(
        Category.id,
        Category.name,
        Category.slug,
        func.count(Contribution.id).label('contribution_count')
    ).outerjoin(
        Contribution.categories
    ).group_by(Category.id, Category.name, Category.slug).all()
    
    categories_data = {
        cat.id: {
            "name": cat.name,
            "slug": cat.slug,
            "contribution_count": cat.contribution_count
        }
        for cat in category_stats
    }
    
    # Transform contributions
    contributions_data = []
    for contrib in contributions:
        contrib_data = {
            "id": contrib.id,
            "source": contrib.source_text,
            "target": contrib.target_text,
            "language": contrib.language,
            "difficulty": contrib.difficulty_level.value if contrib.difficulty_level else "beginner",
            "is_phrase": contrib.is_phrase,
            "word_count": contrib.word_count,
            "quality_score": contrib.quality_score,
            "categories": [cat.slug for cat in contrib.categories],
            "metadata": {
                "context_notes": contrib.context_notes,
                "cultural_notes": contrib.cultural_notes,
                "pronunciation_guide": contrib.pronunciation_guide,
                "usage_examples": contrib.usage_examples,
                "created_at": contrib.created_at.isoformat(),
                "contributor": contrib.created_by.name if contrib.created_by else None
            }
        }
        
        # Add sub-translations
        if contrib.sub_translations:
            contrib_data["sub_translations"] = [
                {
                    "source_word": st.source_word,
                    "target_word": st.target_word,
                    "position": st.word_position,
                    "difficulty": st.difficulty_level.value,
                    "confidence": st.confidence_score,
                    "context": st.context,
                    "category": st.category.slug if st.category else None
                }
                for st in contrib.sub_translations
            ]
        
        contributions_data.append(contrib_data)
    
    response_data = {
        "format_version": format_version,
        "exported_at": func.now(),
        "total_contributions": len(contributions_data),
        "categories": categories_data,
        "contributions": contributions_data,
        "statistics": {
            "total_phrases": sum(1 for c in contributions if c.is_phrase),
            "total_words": sum(c.word_count for c in contributions),
            "avg_quality_score": sum(c.quality_score for c in contributions) / len(contributions) if contributions else 0,
            "difficulty_distribution": {
                level.value: sum(1 for c in contributions if c.difficulty_level == level)
                for level in [None] + list(contrib.difficulty_level.__class__)
                if level is not None
            }
        }
    }
    
    headers = {
        "Content-Disposition": f"attachment; filename=kikuyu_corpus_{format_version}.json",
        "Cache-Control": "public, max-age=1800"  # Cache for 30 minutes
    }
    
    return JSONResponse(content=response_data, headers=headers)


@router.get("/stats")
def export_statistics(db: Session = Depends(get_db)):
    """Get export-related statistics"""
    total_approved = db.query(func.count(Contribution.id)).filter(
        Contribution.status == ContributionStatus.APPROVED
    ).scalar()
    
    total_sub_translations = db.query(func.count()).select_from(
        db.query(Contribution).join(Contribution.sub_translations).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).subquery()
    ).scalar()
    
    categories_with_content = db.query(func.count(func.distinct(Category.id))).select_from(
        Category.__table__.join(Contribution.categories).join(Contribution.__table__)
    ).filter(Contribution.status == ContributionStatus.APPROVED).scalar()
    
    return {
        "total_approved_contributions": total_approved,
        "total_sub_translations": total_sub_translations,
        "categories_with_content": categories_with_content,
        "export_formats": [
            "translations.json (legacy)",
            "flashcards.json (flashcard app)",
            "corpus/full.json (complete corpus)"
        ]
    }