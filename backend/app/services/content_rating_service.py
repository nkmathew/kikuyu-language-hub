"""
Content rating and adult content tagging service
"""
from typing import List, Dict, Optional, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, func
import json
import re
from datetime import datetime

from ..models.contribution import Contribution, ContributionStatus
from ..models.user import User, UserRole
from ..models.content_rating import (
    ContributionRating, ContentFilter, ContentAuditLog,
    ContentRating, ContentWarningType
)
from ..core.cache import cached, CacheConfig, invalidate_cache_on_change


class ContentRatingService:
    """
    Service for managing content ratings and adult content tagging
    """
    
    # Adult content detection patterns
    ADULT_LANGUAGE_PATTERNS = [
        # Explicit sexual content patterns (in both English and Kikuyu)
        r'\b(sex|sexual|intercourse|intimate|erotic)\b',
        r'\b(genitals?|penis|vagina|breast|nipple)\b',
        r'\b(orgasm|climax|arousal|pleasure)\b',
        
        # Strong profanity patterns
        r'\b(fuck|shit|damn|bitch|ass|hell)\b',
        r'\b(fucking|motherfucker|asshole|bastard)\b',
        
        # Violence patterns
        r'\b(kill|murder|death|violence|blood|weapon)\b',
        r'\b(gun|knife|sword|bomb|explosive)\b',
        
        # Substance use patterns
        r'\b(drug|cocaine|heroin|marijuana|alcohol|drunk)\b',
        r'\b(drinking|smoking|addiction|intoxicated)\b',
        
        # Mature themes
        r'\b(adult|mature|explicit|graphic|disturbing)\b',
    ]
    
    # Cultural and religious sensitivity patterns
    CULTURAL_SENSITIVE_PATTERNS = [
        r'\b(god|jesus|christ|allah|religion|religious)\b',
        r'\b(politics|political|government|leader)\b',
        r'\b(tradition|ritual|ceremony|sacred)\b',
    ]
    
    @staticmethod
    def analyze_content_rating(
        content_text: str,
        target_text: str = "",
        context_notes: str = ""
    ) -> Tuple[ContentRating, List[ContentWarningType], float]:
        """
        Analyze content and suggest appropriate rating and warnings
        
        Returns:
            - Suggested content rating
            - List of content warnings
            - Confidence score (0.0-1.0)
        """
        combined_text = f"{content_text} {target_text} {context_notes}".lower()
        
        warnings = []
        rating = ContentRating.GENERAL
        confidence = 0.8
        
        # Check for adult content patterns
        adult_matches = 0
        for pattern in ContentRatingService.ADULT_LANGUAGE_PATTERNS:
            if re.search(pattern, combined_text, re.IGNORECASE):
                adult_matches += 1
        
        # Check for cultural sensitivity
        cultural_matches = 0
        for pattern in ContentRatingService.CULTURAL_SENSITIVE_PATTERNS:
            if re.search(pattern, combined_text, re.IGNORECASE):
                cultural_matches += 1
        
        # Determine content warnings
        if re.search(r'\b(sex|sexual|intercourse|intimate|erotic|genitals?|penis|vagina|breast|nipple|orgasm)\b', combined_text, re.IGNORECASE):
            warnings.append(ContentWarningType.SEXUAL_CONTENT)
            rating = ContentRating.MATURE
        
        if re.search(r'\b(fuck|shit|damn|bitch|ass|hell|fucking|motherfucker|asshole|bastard)\b', combined_text, re.IGNORECASE):
            warnings.append(ContentWarningType.STRONG_LANGUAGE)
            if rating == ContentRating.GENERAL:
                rating = ContentRating.PARENTAL_GUIDANCE
        
        if re.search(r'\b(kill|murder|death|violence|blood|weapon|gun|knife|sword|bomb)\b', combined_text, re.IGNORECASE):
            warnings.append(ContentWarningType.VIOLENCE)
            if rating in [ContentRating.GENERAL, ContentRating.PARENTAL_GUIDANCE]:
                rating = ContentRating.TEENS
        
        if re.search(r'\b(drug|cocaine|heroin|marijuana|alcohol|drunk|drinking|smoking|addiction)\b', combined_text, re.IGNORECASE):
            warnings.append(ContentWarningType.SUBSTANCE_USE)
            if rating in [ContentRating.GENERAL, ContentRating.PARENTAL_GUIDANCE]:
                rating = ContentRating.TEENS
        
        if cultural_matches > 0:
            if re.search(r'\b(god|jesus|christ|allah|religion|religious)\b', combined_text, re.IGNORECASE):
                warnings.append(ContentWarningType.RELIGIOUS_CONTENT)
            if re.search(r'\b(politics|political|government|leader)\b', combined_text, re.IGNORECASE):
                warnings.append(ContentWarningType.POLITICAL_CONTENT)
            if re.search(r'\b(tradition|ritual|ceremony|sacred)\b', combined_text, re.IGNORECASE):
                warnings.append(ContentWarningType.CULTURAL_SENSITIVE)
        
        # Determine final rating based on warning severity
        if ContentWarningType.SEXUAL_CONTENT in warnings:
            rating = ContentRating.ADULT_ONLY if adult_matches > 3 else ContentRating.MATURE
        elif len(warnings) >= 3:
            rating = ContentRating.MATURE
        elif len(warnings) >= 2:
            rating = ContentRating.TEENS
        elif len(warnings) >= 1:
            rating = ContentRating.PARENTAL_GUIDANCE
        
        # Adjust confidence based on pattern matches
        if adult_matches > 0 or cultural_matches > 0:
            confidence = min(0.9, 0.6 + (adult_matches + cultural_matches) * 0.1)
        
        return rating, warnings, confidence
    
    @staticmethod
    def rate_contribution(
        db: Session,
        contribution_id: int,
        content_rating: ContentRating,
        content_warnings: List[ContentWarningType],
        rating_reason: str = "",
        reviewer_id: Optional[int] = None,
        auto_rated: bool = False
    ) -> ContributionRating:
        """
        Assign content rating to a contribution
        """
        # Check if rating already exists
        existing_rating = db.query(ContributionRating).filter(
            ContributionRating.contribution_id == contribution_id
        ).first()
        
        if existing_rating:
            # Log the change
            audit_log = ContentAuditLog(
                contribution_id=contribution_id,
                old_rating=existing_rating.content_rating,
                new_rating=content_rating,
                old_warnings=existing_rating.content_warnings,
                new_warnings=json.dumps([w.value for w in content_warnings]),
                changed_by_id=reviewer_id,
                change_reason=rating_reason,
                auto_generated=auto_rated
            )
            db.add(audit_log)
            
            # Update existing rating
            existing_rating.content_rating = content_rating
            existing_rating.is_adult_content = content_rating in [ContentRating.MATURE, ContentRating.ADULT_ONLY]
            existing_rating.requires_warning = len(content_warnings) > 0
            existing_rating.content_warnings = json.dumps([w.value for w in content_warnings])
            existing_rating.rating_reason = rating_reason
            existing_rating.rated_by_id = reviewer_id
            existing_rating.auto_rated = auto_rated
            existing_rating.updated_at = datetime.utcnow()
            
            db.commit()
            return existing_rating
        
        # Create new rating
        rating = ContributionRating(
            contribution_id=contribution_id,
            content_rating=content_rating,
            is_adult_content=content_rating in [ContentRating.MATURE, ContentRating.ADULT_ONLY],
            requires_warning=len(content_warnings) > 0,
            content_warnings=json.dumps([w.value for w in content_warnings]),
            rating_reason=rating_reason,
            rated_by_id=reviewer_id,
            auto_rated=auto_rated,
            rating_confidence=90 if not auto_rated else 70
        )
        
        db.add(rating)
        db.commit()
        db.refresh(rating)
        
        return rating
    
    @staticmethod
    def auto_rate_contribution(
        db: Session,
        contribution_id: int
    ) -> ContributionRating:
        """
        Automatically analyze and rate a contribution
        """
        contribution = db.query(Contribution).filter(
            Contribution.id == contribution_id
        ).first()
        
        if not contribution:
            raise ValueError(f"Contribution {contribution_id} not found")
        
        # Analyze content
        suggested_rating, warnings, confidence = ContentRatingService.analyze_content_rating(
            contribution.source_text,
            contribution.target_text,
            contribution.context_notes or ""
        )
        
        # Create rating
        return ContentRatingService.rate_contribution(
            db=db,
            contribution_id=contribution_id,
            content_rating=suggested_rating,
            content_warnings=warnings,
            rating_reason=f"Automatically rated (confidence: {confidence:.2f})",
            auto_rated=True
        )
    
    @staticmethod
    def get_filtered_contributions(
        db: Session,
        user_id: int,
        page: int = 1,
        limit: int = 20,
        status_filter: Optional[ContributionStatus] = None
    ) -> Dict:
        """
        Get contributions filtered by user's content preferences
        """
        # Get user's content filter settings
        content_filter = db.query(ContentFilter).filter(
            ContentFilter.user_id == user_id
        ).first()
        
        if not content_filter:
            # Default safe settings for users without explicit preferences
            max_rating = ContentRating.GENERAL
            hide_adult = True
            hidden_warnings = []
        else:
            max_rating = content_filter.max_content_rating
            hide_adult = content_filter.hide_adult_content
            hidden_warnings = json.loads(content_filter.hidden_warning_types or "[]")
        
        # Build query
        query = db.query(Contribution).join(
            ContributionRating, 
            Contribution.id == ContributionRating.contribution_id,
            isouter=True
        )
        
        if status_filter:
            query = query.filter(Contribution.status == status_filter)
        
        # Apply content filters
        if hide_adult:
            query = query.filter(
                or_(
                    ContributionRating.is_adult_content == False,
                    ContributionRating.is_adult_content.is_(None)
                )
            )
        
        # Filter by max rating
        rating_order = {
            ContentRating.GENERAL: 1,
            ContentRating.PARENTAL_GUIDANCE: 2,
            ContentRating.TEENS: 3,
            ContentRating.MATURE: 4,
            ContentRating.ADULT_ONLY: 5
        }
        
        max_rating_value = rating_order[max_rating]
        allowed_ratings = [rating for rating, value in rating_order.items() if value <= max_rating_value]
        
        query = query.filter(
            or_(
                ContributionRating.content_rating.in_(allowed_ratings),
                ContributionRating.content_rating.is_(None)
            )
        )
        
        # Apply pagination
        offset = (page - 1) * limit
        contributions = query.offset(offset).limit(limit).all()
        total = query.count()
        
        return {
            "contributions": contributions,
            "total": total,
            "page": page,
            "limit": limit,
            "total_pages": (total + limit - 1) // limit,
            "filtered_by": {
                "max_rating": max_rating.value,
                "hide_adult": hide_adult,
                "hidden_warnings": hidden_warnings
            }
        }
    
    @staticmethod
    def update_user_content_filter(
        db: Session,
        user_id: int,
        max_content_rating: ContentRating,
        hide_adult_content: bool = True,
        hide_content_warnings: bool = False,
        hidden_warning_types: List[ContentWarningType] = None
    ) -> ContentFilter:
        """
        Update user's content filtering preferences
        """
        content_filter = db.query(ContentFilter).filter(
            ContentFilter.user_id == user_id
        ).first()
        
        if not content_filter:
            content_filter = ContentFilter(user_id=user_id)
            db.add(content_filter)
        
        content_filter.max_content_rating = max_content_rating
        content_filter.hide_adult_content = hide_adult_content
        content_filter.hide_content_warnings = hide_content_warnings
        content_filter.hidden_warning_types = json.dumps([
            w.value for w in (hidden_warning_types or [])
        ])
        content_filter.updated_at = datetime.utcnow()
        
        db.commit()
        db.refresh(content_filter)
        
        return content_filter
    
    @staticmethod
    @cached(ttl=CacheConfig.ANALYTICS_TTL, key_prefix="content_rating_stats")
    def get_content_rating_statistics(db: Session) -> Dict:
        """
        Get content rating statistics
        """
        # Rating distribution
        rating_counts = db.query(
            ContributionRating.content_rating,
            func.count(ContributionRating.id)
        ).group_by(ContributionRating.content_rating).all()
        
        rating_distribution = {
            rating.value: count for rating, count in rating_counts
        }
        
        # Warning type distribution
        warning_counts = {}
        ratings_with_warnings = db.query(ContributionRating).filter(
            ContributionRating.content_warnings.isnot(None)
        ).all()
        
        for rating in ratings_with_warnings:
            try:
                warnings = json.loads(rating.content_warnings)
                for warning in warnings:
                    warning_counts[warning] = warning_counts.get(warning, 0) + 1
            except (json.JSONDecodeError, TypeError):
                continue
        
        # Adult content statistics
        adult_content_count = db.query(func.count(ContributionRating.id)).filter(
            ContributionRating.is_adult_content == True
        ).scalar()
        
        total_rated = db.query(func.count(ContributionRating.id)).scalar()
        total_contributions = db.query(func.count(Contribution.id)).scalar()
        
        return {
            "total_contributions": total_contributions,
            "total_rated": total_rated,
            "unrated_count": total_contributions - total_rated,
            "rating_distribution": rating_distribution,
            "warning_distribution": warning_counts,
            "adult_content_count": adult_content_count,
            "adult_content_percentage": round(
                (adult_content_count / max(total_rated, 1)) * 100, 1
            ),
            "rating_coverage": round(
                (total_rated / max(total_contributions, 1)) * 100, 1
            )
        }
    
    @staticmethod
    def bulk_auto_rate_contributions(
        db: Session,
        limit: int = 100,
        status_filter: ContributionStatus = ContributionStatus.APPROVED
    ) -> Dict:
        """
        Bulk auto-rate contributions that don't have ratings
        """
        # Find unrated contributions
        unrated_contributions = db.query(Contribution).outerjoin(
            ContributionRating,
            Contribution.id == ContributionRating.contribution_id
        ).filter(
            and_(
                ContributionRating.id.is_(None),
                Contribution.status == status_filter
            )
        ).limit(limit).all()
        
        processed = 0
        errors = []
        rating_summary = {rating.value: 0 for rating in ContentRating}
        
        for contribution in unrated_contributions:
            try:
                rating = ContentRatingService.auto_rate_contribution(
                    db, contribution.id
                )
                rating_summary[rating.content_rating.value] += 1
                processed += 1
            except Exception as e:
                errors.append(f"Error rating contribution {contribution.id}: {str(e)}")
        
        return {
            "processed": processed,
            "total_unrated": len(unrated_contributions),
            "rating_summary": rating_summary,
            "errors": errors
        }