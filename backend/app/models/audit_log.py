from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Text, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from ..db.base import Base


class AuditAction(str, Enum):
    APPROVE = "approve"
    REJECT = "reject"


class AuditLog(Base):
    __tablename__ = "audit_logs"

    id = Column(Integer, primary_key=True, index=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=False)
    action = Column(SQLEnum(AuditAction), nullable=False)
    moderator_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    reason = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    contribution = relationship("Contribution", back_populates="audit_logs")
    moderator = relationship("User", back_populates="audit_logs")