"""
Content rating and adult content tagging API routes
"""
from typing import List, Dict, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
from ...models.user import User
from ...models.contribution import ContributionStatus
from ...models.content_rating import ContentRating, ContentWarningType
from ...services.content_rating_service import ContentRatingService
from ...core.security import get_current_user, require_moderator_or_admin
from ...db.session import get_db
from ...core.cache import cache

router = APIRouter(prefix="/content-rating", tags=["content-rating"])


class ContentAnalysisResponse(BaseModel):
    """Content analysis response"""
    suggested_rating: str
    content_warnings: List[str]
    confidence: float
    is_adult_content: bool
    requires_warning: bool


class RatingRequest(BaseModel):
    """Content rating assignment request"""
    contribution_id: int
    content_rating: str = Field(..., description="Content rating level")
    content_warnings: List[str] = Field(default=[], description="Content warning types")
    rating_reason: str = Field("", description="Reason for rating assignment")


class ContentFilterRequest(BaseModel):
    """User content filter preferences"""
    max_content_rating: str = Field("general", description="Maximum content rating to show")
    hide_adult_content: bool = Field(True, description="Hide adult content")
    hide_content_warnings: bool = Field(False, description="Hide content with warnings")
    hidden_warning_types: List[str] = Field(default=[], description="Warning types to hide")


class BulkRatingRequest(BaseModel):
    """Bulk auto-rating request"""
    limit: int = Field(100, ge=1, le=500, description="Maximum contributions to process")
    status_filter: str = Field("approved", description="Status filter for contributions")


class ContentAnalysisRequest(BaseModel):
    """Content analysis request"""
    source_text: str = Field(..., description="Source text to analyze")
    target_text: str = Field("", description="Target text to analyze")
    context_notes: str = Field("", description="Context notes to analyze")

@router.post("/analyze", response_model=ContentAnalysisResponse)
def analyze_content(
    request: ContentAnalysisRequest,
    current_user: User = Depends(get_current_user)
):
    """Analyze content and suggest rating and warnings"""
    try:
        rating, warnings, confidence = ContentRatingService.analyze_content_rating(
            request.source_text, request.target_text, request.context_notes
        )
        
        return ContentAnalysisResponse(
            suggested_rating=rating.value,
            content_warnings=[w.value for w in warnings],
            confidence=confidence,
            is_adult_content=rating in [ContentRating.MATURE, ContentRating.ADULT_ONLY],
            requires_warning=len(warnings) > 0
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Content analysis failed: {str(e)}")


@router.post("/rate")
def rate_contribution(
    request: RatingRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Assign content rating to a contribution"""
    try:
        # Validate rating and warnings
        try:
            content_rating = ContentRating(request.content_rating)
        except ValueError:
            raise HTTPException(status_code=400, detail=f"Invalid content rating: {request.content_rating}")
        
        try:
            warnings = [ContentWarningType(w) for w in request.content_warnings]
        except ValueError as e:
            raise HTTPException(status_code=400, detail=f"Invalid content warning: {str(e)}")
        
        rating = ContentRatingService.rate_contribution(
            db=db,
            contribution_id=request.contribution_id,
            content_rating=content_rating,
            content_warnings=warnings,
            rating_reason=request.rating_reason,
            reviewer_id=current_user.id,
            auto_rated=False
        )
        
        # Clear relevant caches
        cache.delete_pattern(f"contribution:{request.contribution_id}:*")
        cache.delete_pattern("content_rating_*")
        
        return {
            "success": True,
            "rating_id": rating.id,
            "content_rating": rating.content_rating.value,
            "is_adult_content": rating.is_adult_content,
            "message": "Content rating assigned successfully"
        }
    
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Rating assignment failed: {str(e)}")


@router.post("/auto-rate/{contribution_id}")
def auto_rate_contribution(
    contribution_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Automatically rate a contribution using AI analysis"""
    try:
        rating = ContentRatingService.auto_rate_contribution(db, contribution_id)
        
        # Clear relevant caches
        cache.delete_pattern(f"contribution:{contribution_id}:*")
        cache.delete_pattern("content_rating_*")
        
        return {
            "success": True,
            "rating_id": rating.id,
            "content_rating": rating.content_rating.value,
            "content_warnings": rating.content_warnings,
            "confidence": rating.rating_confidence,
            "message": "Contribution auto-rated successfully"
        }
    
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Auto-rating failed: {str(e)}")


@router.get("/contributions/filtered")
def get_filtered_contributions(
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    status: Optional[str] = Query(None, description="Filter by status"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get contributions filtered by user's content preferences"""
    try:
        status_filter = None
        if status:
            try:
                status_filter = ContributionStatus(status)
            except ValueError:
                raise HTTPException(status_code=400, detail=f"Invalid status: {status}")
        
        result = ContentRatingService.get_filtered_contributions(
            db=db,
            user_id=current_user.id,
            page=page,
            limit=limit,
            status_filter=status_filter
        )
        
        return result
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get filtered contributions: {str(e)}")


@router.post("/filters")
def update_content_filters(
    request: ContentFilterRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Update user's content filtering preferences"""
    try:
        # Validate rating
        try:
            max_rating = ContentRating(request.max_content_rating)
        except ValueError:
            raise HTTPException(status_code=400, detail=f"Invalid max content rating: {request.max_content_rating}")
        
        # Validate warning types
        try:
            warning_types = [ContentWarningType(w) for w in request.hidden_warning_types]
        except ValueError as e:
            raise HTTPException(status_code=400, detail=f"Invalid warning type: {str(e)}")
        
        content_filter = ContentRatingService.update_user_content_filter(
            db=db,
            user_id=current_user.id,
            max_content_rating=max_rating,
            hide_adult_content=request.hide_adult_content,
            hide_content_warnings=request.hide_content_warnings,
            hidden_warning_types=warning_types
        )
        
        return {
            "success": True,
            "filter_id": content_filter.id,
            "max_content_rating": content_filter.max_content_rating.value,
            "hide_adult_content": content_filter.hide_adult_content,
            "message": "Content filters updated successfully"
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to update content filters: {str(e)}")


@router.get("/filters")
def get_content_filters(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get user's current content filtering preferences"""
    try:
        from ...models.content_rating import ContentFilter
        import json
        
        content_filter = db.query(ContentFilter).filter(
            ContentFilter.user_id == current_user.id
        ).first()
        
        if not content_filter:
            # Return default settings
            return {
                "max_content_rating": "general",
                "hide_adult_content": True,
                "hide_content_warnings": False,
                "hidden_warning_types": [],
                "is_parental_controlled": False
            }
        
        hidden_warnings = []
        if content_filter.hidden_warning_types:
            try:
                hidden_warnings = json.loads(content_filter.hidden_warning_types)
            except json.JSONDecodeError:
                hidden_warnings = []
        
        return {
            "max_content_rating": content_filter.max_content_rating.value,
            "hide_adult_content": content_filter.hide_adult_content,
            "hide_content_warnings": content_filter.hide_content_warnings,
            "hidden_warning_types": hidden_warnings,
            "is_parental_controlled": content_filter.is_parental_controlled
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get content filters: {str(e)}")


@router.get("/statistics")
def get_content_rating_statistics(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get content rating statistics"""
    try:
        stats = ContentRatingService.get_content_rating_statistics(db)
        return stats
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get content rating statistics: {str(e)}")


@router.post("/bulk-auto-rate")
def bulk_auto_rate(
    request: BulkRatingRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Bulk auto-rate contributions that don't have ratings"""
    try:
        # Validate status filter
        try:
            status_filter = ContributionStatus(request.status_filter)
        except ValueError:
            raise HTTPException(status_code=400, detail=f"Invalid status filter: {request.status_filter}")
        
        result = ContentRatingService.bulk_auto_rate_contributions(
            db=db,
            limit=request.limit,
            status_filter=status_filter
        )
        
        # Clear relevant caches
        cache.delete_pattern("content_rating_*")
        cache.delete_pattern("contributions:*")
        
        return result
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Bulk auto-rating failed: {str(e)}")


@router.get("/ratings")
def get_available_ratings():
    """Get all available content ratings and warning types"""
    return {
        "content_ratings": [
            {
                "value": rating.value,
                "name": rating.value.replace('_', ' ').title(),
                "description": ContentRatingService._get_rating_description(rating)
            }
            for rating in ContentRating
        ],
        "warning_types": [
            {
                "value": warning.value,
                "name": warning.value.replace('_', ' ').title(),
                "description": ContentRatingService._get_warning_description(warning)
            }
            for warning in ContentWarningType
        ]
    }


@router.get("/contribution/{contribution_id}/rating")
def get_contribution_rating(
    contribution_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get content rating for a specific contribution"""
    try:
        from ...models.content_rating import ContributionRating
        import json
        
        rating = db.query(ContributionRating).filter(
            ContributionRating.contribution_id == contribution_id
        ).first()
        
        if not rating:
            return {
                "has_rating": False,
                "content_rating": "general",
                "is_adult_content": False,
                "requires_warning": False,
                "content_warnings": []
            }
        
        warnings = []
        if rating.content_warnings:
            try:
                warnings = json.loads(rating.content_warnings)
            except json.JSONDecodeError:
                warnings = []
        
        return {
            "has_rating": True,
            "content_rating": rating.content_rating.value,
            "is_adult_content": rating.is_adult_content,
            "requires_warning": rating.requires_warning,
            "content_warnings": warnings,
            "rating_reason": rating.rating_reason,
            "auto_rated": rating.auto_rated,
            "confidence": rating.rating_confidence,
            "created_at": rating.created_at.isoformat(),
            "updated_at": rating.updated_at.isoformat() if rating.updated_at else None
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get contribution rating: {str(e)}")


# Helper methods for ContentRatingService
def _get_rating_description(rating: ContentRating) -> str:
    """Get description for content rating"""
    descriptions = {
        ContentRating.GENERAL: "Suitable for all audiences",
        ContentRating.PARENTAL_GUIDANCE: "Parental guidance suggested",
        ContentRating.TEENS: "Suitable for teens (13+)",
        ContentRating.MATURE: "Mature content (17+)",
        ContentRating.ADULT_ONLY: "Adult only content (18+)"
    }
    return descriptions.get(rating, "Content rating")


def _get_warning_description(warning: ContentWarningType) -> str:
    """Get description for content warning type"""
    descriptions = {
        ContentWarningType.STRONG_LANGUAGE: "Contains strong language or profanity",
        ContentWarningType.SEXUAL_CONTENT: "Contains sexual content or themes",
        ContentWarningType.VIOLENCE: "Contains violence or violent themes",
        ContentWarningType.SUBSTANCE_USE: "Contains substance use or references",
        ContentWarningType.CULTURAL_SENSITIVE: "Contains culturally sensitive content",
        ContentWarningType.RELIGIOUS_CONTENT: "Contains religious content or themes",
        ContentWarningType.POLITICAL_CONTENT: "Contains political content or themes",
        ContentWarningType.MATURE_THEMES: "Contains mature themes"
    }
    return descriptions.get(warning, "Content warning")


# Add helper methods to service
ContentRatingService._get_rating_description = staticmethod(_get_rating_description)
ContentRatingService._get_warning_description = staticmethod(_get_warning_description)