from datetime import datetime
from pydantic import BaseModel
from typing import Optional, Dict, Any
from ..models.contribution import ContributionStatus
from .user import UserResponse


class ContributionBase(BaseModel):
    source_text: str
    target_text: str
    language: str = "kikuyu"


class ContributionCreate(ContributionBase):
    pass


class ContributionUpdate(BaseModel):
    source_text: Optional[str] = None
    target_text: Optional[str] = None
    language: Optional[str] = None
    reason: Optional[str] = None


class ContributionResponse(ContributionBase):
    id: int
    status: ContributionStatus
    created_by_id: int
    created_at: datetime
    updated_at: datetime
    audit_log: Optional[Dict[str, Any]] = None
    created_by: Optional[UserResponse] = None

    class Config:
        from_attributes = True