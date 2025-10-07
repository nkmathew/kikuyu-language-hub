from datetime import datetime
from pydantic import BaseModel
from typing import Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from .user import UserResponse


class AuditLogBase(BaseModel):
    action: str
    reason: Optional[str] = None


class AuditLogResponse(AuditLogBase):
    id: int
    contribution_id: int
    moderator_id: int
    created_at: datetime
    moderator: Optional["UserResponse"] = None

    class Config:
        from_attributes = True