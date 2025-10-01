from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Text, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from ..db.base import Base


class ContributionStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class Contribution(Base):
    __tablename__ = "contributions"

    id = Column(Integer, primary_key=True, index=True)
    source_text = Column(Text, nullable=False)
    target_text = Column(Text, nullable=False)
    status = Column(SQLEnum(ContributionStatus), default=ContributionStatus.PENDING, nullable=False)
    language = Column(String, default="kikuyu", nullable=False)
    created_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    created_by = relationship("User", back_populates="contributions")
    audit_logs = relationship("AuditLog", back_populates="contribution")