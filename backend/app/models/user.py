from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Enum as SQLEnum
from sqlalchemy.orm import relationship
from ..db.base import Base


class UserRole(str, Enum):
    ADMIN = "admin"
    MODERATOR = "moderator"
    CONTRIBUTOR = "contributor"


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=False)
    role = Column(SQLEnum(UserRole), default=UserRole.CONTRIBUTOR, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    contributions = relationship("Contribution", back_populates="created_by")
    audit_logs = relationship("AuditLog", back_populates="moderator")