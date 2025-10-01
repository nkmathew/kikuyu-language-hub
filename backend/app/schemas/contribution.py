from datetime import datetime
from pydantic import BaseModel
from ..models.contribution import ContributionStatus


class ContributionBase(BaseModel):
    source_text: str
    target_text: str
    language: str = "kikuyu"


class ContributionCreate(ContributionBase):
    pass


class ContributionUpdate(BaseModel):
    status: ContributionStatus
    reason: str | None = None


class ContributionResponse(ContributionBase):
    id: int
    status: ContributionStatus
    created_by_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True