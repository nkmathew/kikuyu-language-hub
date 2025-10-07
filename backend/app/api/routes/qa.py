"""
Quality Assurance API routes for automated checks and moderation tools
"""
from typing import List, Dict, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session
from ...models.user import User
from ...models.contribution import ContributionStatus
from ...services.qa_service import QualityAssuranceService, QualityIssueType
from ...core.security import get_current_user, require_moderator_or_admin
from ...db.session import get_db
from ...core.cache import cache

router = APIRouter(prefix="/qa", tags=["quality-assurance"])


class QualityIssueResponse(BaseModel):
    """Quality issue response model"""
    issue_type: str
    severity: str
    message: str
    suggestion: Optional[str]
    confidence: float
    auto_fixable: bool


class QualityReportResponse(BaseModel):
    """Quality report response model"""
    contribution_id: int
    overall_score: float
    issues: List[QualityIssueResponse]
    recommendations: List[str]
    auto_approve_eligible: bool
    requires_review: bool


class BatchAnalysisRequest(BaseModel):
    """Batch analysis request model"""
    status_filter: Optional[str] = Field(None, description="Filter by contribution status")
    limit: int = Field(100, ge=1, le=500, description="Maximum contributions to analyze")
    min_quality_threshold: float = Field(0.0, ge=0.0, le=1.0, description="Minimum quality threshold")


class AutoFixRequest(BaseModel):
    """Auto-fix request model"""
    contribution_id: int = Field(..., description="Contribution ID to auto-fix")


class ModerationQueueItem(BaseModel):
    """Moderation queue item"""
    contribution_id: int
    source_text: str
    target_text: str
    quality_score: float
    priority: str
    issue_count: int
    created_at: str
    contributor: str
    auto_approve_eligible: bool
    requires_review: bool


@router.get("/analyze/{contribution_id}", response_model=QualityReportResponse)
def analyze_contribution_quality(
    contribution_id: int,
    detailed: bool = Query(True, description="Include detailed analysis"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Analyze quality of a specific contribution"""
    try:
        report = QualityAssuranceService.analyze_contribution_quality(
            db=db,
            contribution_id=contribution_id,
            detailed=detailed
        )
        
        return QualityReportResponse(
            contribution_id=report.contribution_id,
            overall_score=report.overall_score,
            issues=[
                QualityIssueResponse(
                    issue_type=issue.issue_type.value,
                    severity=issue.severity,
                    message=issue.message,
                    suggestion=issue.suggestion,
                    confidence=issue.confidence,
                    auto_fixable=issue.auto_fixable
                )
                for issue in report.issues
            ],
            recommendations=report.recommendations,
            auto_approve_eligible=report.auto_approve_eligible,
            requires_review=report.requires_review
        )
    
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Quality analysis failed: {str(e)}")


@router.post("/batch-analyze")
def batch_quality_analysis(
    request: BatchAnalysisRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Perform batch quality analysis on multiple contributions"""
    try:
        # Convert string status to enum if provided
        status_filter = None
        if request.status_filter:
            try:
                status_filter = ContributionStatus(request.status_filter)
            except ValueError:
                raise HTTPException(status_code=400, detail=f"Invalid status: {request.status_filter}")
        
        results = QualityAssuranceService.batch_quality_analysis(
            db=db,
            status_filter=status_filter,
            limit=request.limit,
            min_quality_threshold=request.min_quality_threshold
        )
        
        return results
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Batch analysis failed: {str(e)}")


@router.post("/auto-fix")
def auto_fix_contribution(
    request: AutoFixRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Automatically fix issues in a contribution"""
    try:
        result = QualityAssuranceService.auto_fix_contribution(
            db=db,
            contribution_id=request.contribution_id,
            user_id=current_user.id
        )
        
        # Clear relevant caches
        cache.delete_pattern(f"contribution:{request.contribution_id}:*")
        cache.delete_pattern("qa_*")
        
        return result
    
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Auto-fix failed: {str(e)}")


@router.get("/moderation-queue", response_model=List[ModerationQueueItem])
def get_moderation_queue(
    priority_filter: Optional[str] = Query(None, description="Filter by priority: high, medium, low, auto"),
    limit: int = Query(50, ge=1, le=200, description="Maximum items to return"),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get prioritized moderation queue"""
    try:
        queue = QualityAssuranceService.get_moderation_queue(
            db=db,
            priority_filter=priority_filter,
            limit=limit
        )
        
        return [ModerationQueueItem(**item) for item in queue]
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get moderation queue: {str(e)}")


@router.get("/statistics")
def get_quality_statistics(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get overall quality statistics"""
    try:
        stats = QualityAssuranceService.get_quality_statistics(db)
        return stats
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get quality statistics: {str(e)}")


@router.get("/issue-types")
def get_quality_issue_types():
    """Get all available quality issue types"""
    return {
        "issue_types": [
            {
                "value": issue_type.value,
                "name": issue_type.value.replace('_', ' ').title(),
                "description": QualityAssuranceService._get_issue_description(issue_type)
            }
            for issue_type in QualityIssueType
        ]
    }


class BulkApprovalRequest(BaseModel):
    contribution_ids: List[int] = Field(..., description="List of contribution IDs to approve")
    reason: str = Field("Bulk approval after quality check", description="Reason for bulk approval")

@router.post("/bulk-approve")
def bulk_approve_contributions(
    request: BulkApprovalRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Bulk approve contributions that meet quality standards"""
    try:
        from ...models.contribution import Contribution
        from ...models.audit_log import AuditLog
        import json
        
        approved_count = 0
        skipped_count = 0
        errors = []
        
        for contrib_id in request.contribution_ids:
            try:
                # Check if contribution exists and is pending
                contribution = db.query(Contribution).filter(
                    Contribution.id == contrib_id,
                    Contribution.status == ContributionStatus.PENDING
                ).first()
                
                if not contribution:
                    skipped_count += 1
                    errors.append(f"Contribution {contrib_id} not found or not pending")
                    continue
                
                # Analyze quality
                report = QualityAssuranceService.analyze_contribution_quality(
                    db, contrib_id, detailed=False
                )
                
                # Only approve if eligible
                if report.auto_approve_eligible:
                    contribution.status = ContributionStatus.APPROVED
                    
                    # Log the approval
                    audit_log = AuditLog(
                        contribution_id=contrib_id,
                        action='approve',
                        moderator_id=current_user.id,
                        reason=f"{request.reason} (Quality score: {report.overall_score:.2f})",
                        metadata=json.dumps({
                            'bulk_approval': True,
                            'quality_score': report.overall_score,
                            'auto_approve_eligible': True
                        })
                    )
                    db.add(audit_log)
                    approved_count += 1
                else:
                    skipped_count += 1
                    errors.append(f"Contribution {contrib_id} does not meet auto-approval criteria")
            
            except Exception as e:
                skipped_count += 1
                errors.append(f"Error processing contribution {contrib_id}: {str(e)}")
        
        if approved_count > 0:
            db.commit()
            
            # Clear relevant caches
            cache.delete_pattern("contributions:*")
            cache.delete_pattern("qa_*")
        
        return {
            "approved_count": approved_count,
            "skipped_count": skipped_count,
            "total_processed": len(request.contribution_ids),
            "errors": errors
        }
    
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Bulk approval failed: {str(e)}")


class BulkRejectionRequest(BaseModel):
    contribution_ids: List[int] = Field(..., description="List of contribution IDs to reject")
    reason: str = Field(..., description="Reason for bulk rejection")

@router.post("/bulk-reject")
def bulk_reject_contributions(
    request: BulkRejectionRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Bulk reject contributions with quality issues"""
    try:
        from ...models.contribution import Contribution
        from ...models.audit_log import AuditLog
        import json
        
        rejected_count = 0
        skipped_count = 0
        errors = []
        
        for contrib_id in request.contribution_ids:
            try:
                # Check if contribution exists and is pending
                contribution = db.query(Contribution).filter(
                    Contribution.id == contrib_id,
                    Contribution.status == ContributionStatus.PENDING
                ).first()
                
                if not contribution:
                    skipped_count += 1
                    errors.append(f"Contribution {contrib_id} not found or not pending")
                    continue
                
                # Analyze quality for logging
                report = QualityAssuranceService.analyze_contribution_quality(
                    db, contrib_id, detailed=False
                )
                
                contribution.status = ContributionStatus.REJECTED
                
                # Log the rejection
                audit_log = AuditLog(
                    contribution_id=contrib_id,
                    action='reject',
                    moderator_id=current_user.id,
                    reason=f"{request.reason} (Quality score: {report.overall_score:.2f})",
                    metadata=json.dumps({
                        'bulk_rejection': True,
                        'quality_score': report.overall_score,
                        'issue_count': len(report.issues)
                    })
                )
                db.add(audit_log)
                rejected_count += 1
            
            except Exception as e:
                skipped_count += 1
                errors.append(f"Error processing contribution {contrib_id}: {str(e)}")
        
        if rejected_count > 0:
            db.commit()
            
            # Clear relevant caches
            cache.delete_pattern("contributions:*")
            cache.delete_pattern("qa_*")
        
        return {
            "rejected_count": rejected_count,
            "skipped_count": skipped_count,
            "total_processed": len(request.contribution_ids),
            "errors": errors
        }
    
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Bulk rejection failed: {str(e)}")


# Add helper method to QualityAssuranceService
def _get_issue_description(issue_type: QualityIssueType) -> str:
    """Get description for quality issue type"""
    descriptions = {
        QualityIssueType.SPELLING_ERROR: "Potential spelling errors in the source text",
        QualityIssueType.LENGTH_MISMATCH: "Unusual length ratio between source and target text",
        QualityIssueType.DUPLICATE_CONTENT: "Duplicate or very similar content found",
        QualityIssueType.INAPPROPRIATE_CONTENT: "Content may be inappropriate or low quality",
        QualityIssueType.FORMATTING_ERROR: "Formatting issues like extra whitespace",
        QualityIssueType.DIFFICULTY_MISMATCH: "Difficulty level doesn't match content complexity",
        QualityIssueType.CATEGORY_MISMATCH: "Categories may not be relevant to content",
        QualityIssueType.TRANSLATION_ACCURACY: "Potential translation accuracy issues",
        QualityIssueType.MISSING_CONTEXT: "Missing context or usage information",
        QualityIssueType.LOW_QUALITY_SCORE: "Overall quality score below threshold"
    }
    return descriptions.get(issue_type, "Quality issue")

# Add the method to the service class
QualityAssuranceService._get_issue_description = staticmethod(_get_issue_description)