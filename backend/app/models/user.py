from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Enum as SQLEnum, Boolean, Text
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
    
    # Enhanced user profile fields
    display_name = Column(String(100))
    google_id = Column(String(100), unique=True, nullable=True, index=True)  # For Google OAuth
    avatar_url = Column(String(500))
    bio = Column(Text)
    is_active = Column(Boolean, default=True, nullable=False)
    is_email_verified = Column(Boolean, default=False, nullable=False)
    last_login = Column(DateTime)
    
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    contributions = relationship("Contribution", back_populates="created_by")
    audit_logs = relationship("AuditLog", back_populates="moderator")
    analytics = relationship("UserAnalytics", back_populates="user", uselist=False)
    content_filter = relationship("ContentFilter", back_populates="user", uselist=False)
    activities = relationship("UserActivity", back_populates="user")
    engagement_metrics = relationship("UserEngagementMetrics", back_populates="user")
    learning_metrics = relationship("LanguageLearningMetrics", back_populates="user")
    webhooks = relationship("Webhook", back_populates="created_by")
    
    def __repr__(self):
        return f"<User(id={self.id}, email='{self.email}', role={self.role})>"
    
    @property
    def name(self):
        """Get the display name or fallback to email"""
        return self.display_name or self.email.split('@')[0]