from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from ...models.user import User, UserRole
from ...models.contribution import ContributionStatus
from ...models.audit_log import AuditAction
from ...schemas.contribution import ContributionCreate, ContributionResponse, ContributionUpdate
from ...services.contribution_service import ContributionService
from ...services.audit_service import AuditService
from ...core.security import get_current_user, require_moderator_or_admin
from ...db.session import get_db

router = APIRouter(prefix="/contributions", tags=["contributions"])


@router.post("/", response_model=ContributionResponse)
def create_contribution(
    contribution_data: ContributionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.create_contribution(db, contribution_data, current_user)
    return contribution


@router.get("/", response_model=List[ContributionResponse])
def get_contributions(
    status: Optional[ContributionStatus] = Query(None),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    # Contributors can only see their own contributions
    if current_user.role == UserRole.CONTRIBUTOR:
        contributions = ContributionService.get_contributions(
            db, status=status, user_id=current_user.id, skip=skip, limit=limit
        )
    else:
        # Moderators and admins can see all contributions
        contributions = ContributionService.get_contributions(
            db, status=status, skip=skip, limit=limit
        )
    
    return contributions


@router.post("/{contribution_id}/approve", response_model=ContributionResponse)
def approve_contribution(
    contribution_id: int,
    moderator: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.get_contribution_by_id(db, contribution_id)
    if not contribution:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Contribution not found"
        )
    
    if contribution.status != ContributionStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only pending contributions can be approved"
        )
    
    # Update contribution status
    contribution = ContributionService.update_contribution_status(
        db, contribution_id, ContributionStatus.APPROVED
    )
    
    # Create audit log
    audit_log = AuditService.create_audit_log(
        db, contribution, AuditAction.APPROVE, moderator
    )
    
    # Include audit log in response
    contribution.audit_log = audit_log
    return contribution


@router.post("/{contribution_id}/reject", response_model=ContributionResponse)
def reject_contribution(
    contribution_id: int,
    update_data: ContributionUpdate,
    moderator: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.get_contribution_by_id(db, contribution_id)
    if not contribution:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Contribution not found"
        )
    
    if contribution.status != ContributionStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only pending contributions can be rejected"
        )
    
    # Update contribution status
    contribution = ContributionService.update_contribution_status(
        db, contribution_id, ContributionStatus.REJECTED
    )
    
    # Create audit log
    audit_log = AuditService.create_audit_log(
        db, contribution, AuditAction.REJECT, moderator, update_data.reason
    )
    
    # Include audit log in response
    contribution.audit_log = audit_log
    return contribution


@router.get("/my", response_model=List[ContributionResponse])
def get_my_contributions(
    status: Optional[ContributionStatus] = Query(None),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get current user's contributions only"""
    contributions = ContributionService.get_contributions(
        db, status=status, user_id=current_user.id, skip=skip, limit=limit
    )
    return contributions


@router.get("/{contribution_id}", response_model=ContributionResponse)
def get_contribution(
    contribution_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.get_contribution_by_id(db, contribution_id)
    if not contribution:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Contribution not found"
        )
    
    # Check permissions: users can only see their own contributions unless they're moderator/admin
    if (current_user.role == UserRole.CONTRIBUTOR and 
        contribution.created_by_id != current_user.id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to view this contribution"
        )
    
    return contribution


@router.put("/{contribution_id}", response_model=ContributionResponse)
def update_contribution(
    contribution_id: int,
    update_data: ContributionUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.get_contribution_by_id(db, contribution_id)
    if not contribution:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Contribution not found"
        )
    
    # Only allow editing own contributions and only if pending
    if (contribution.created_by_id != current_user.id or 
        contribution.status != ContributionStatus.PENDING):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Can only edit own pending contributions"
        )
    
    contribution = ContributionService.update_contribution(
        db, contribution_id, update_data
    )
    return contribution


@router.delete("/{contribution_id}")
def delete_contribution(
    contribution_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    contribution = ContributionService.get_contribution_by_id(db, contribution_id)
    if not contribution:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Contribution not found"
        )
    
    # Only allow deleting own contributions and only if pending
    if (contribution.created_by_id != current_user.id or 
        contribution.status != ContributionStatus.PENDING):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Can only delete own pending contributions"
        )
    
    ContributionService.delete_contribution(db, contribution_id)
    return {"message": "Contribution deleted successfully"}