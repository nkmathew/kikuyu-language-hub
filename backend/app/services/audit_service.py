from typing import Optional
from sqlalchemy.orm import Session
from ..models.audit_log import AuditLog, AuditAction
from ..models.user import User
from ..models.contribution import Contribution


class AuditService:
    @staticmethod
    def create_audit_log(
        db: Session,
        contribution: Contribution,
        action: AuditAction,
        moderator: User,
        reason: Optional[str] = None
    ) -> AuditLog:
        audit_log = AuditLog(
            contribution_id=contribution.id,
            action=action,
            moderator_id=moderator.id,
            reason=reason
        )
        db.add(audit_log)
        db.commit()
        db.refresh(audit_log)
        return audit_log