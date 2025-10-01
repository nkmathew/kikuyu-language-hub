from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, Float, Enum as SQLEnum
from sqlalchemy.orm import relationship
from ..db.base import Base


class AnalyticsEventType(str, Enum):
    CONTRIBUTION_CREATED = "contribution_created"
    CONTRIBUTION_APPROVED = "contribution_approved"
    CONTRIBUTION_REJECTED = "contribution_rejected"
    SUB_TRANSLATION_CREATED = "sub_translation_created"
    EXPORT_REQUESTED = "export_requested"
    CATEGORY_ASSIGNED = "category_assigned"
    USER_LOGIN = "user_login"
    USER_REGISTERED = "user_registered"


class UserAnalytics(Base):
    __tablename__ = "user_analytics"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    total_contributions = Column(Integer, default=0, nullable=False)
    approved_contributions = Column(Integer, default=0, nullable=False)
    rejected_contributions = Column(Integer, default=0, nullable=False)
    total_sub_translations = Column(Integer, default=0, nullable=False)
    total_words_contributed = Column(Integer, default=0, nullable=False)
    average_approval_rate = Column(Float, default=0.0, nullable=False)
    streak_days = Column(Integer, default=0, nullable=False)
    last_contribution_date = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    user = relationship("User", back_populates="analytics")
    
    def __repr__(self):
        return f"<UserAnalytics(user_id={self.user_id}, contributions={self.total_contributions})>"


class CategoryAnalytics(Base):
    __tablename__ = "category_analytics"

    id = Column(Integer, primary_key=True, index=True)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=False)
    total_contributions = Column(Integer, default=0, nullable=False)
    approved_contributions = Column(Integer, default=0, nullable=False)
    total_sub_translations = Column(Integer, default=0, nullable=False)
    unique_contributors = Column(Integer, default=0, nullable=False)
    average_quality_score = Column(Float, default=0.0, nullable=False)
    last_contribution_date = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    category = relationship("Category")
    
    def __repr__(self):
        return f"<CategoryAnalytics(category_id={self.category_id}, contributions={self.total_contributions})>"


class AnalyticsEvent(Base):
    __tablename__ = "analytics_events"

    id = Column(Integer, primary_key=True, index=True)
    event_type = Column(SQLEnum(AnalyticsEventType), nullable=False, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=True)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=True)
    event_metadata = Column(Text)  # JSON metadata for additional event data
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    # Relationships
    user = relationship("User")
    contribution = relationship("Contribution")
    category = relationship("Category")
    
    def __repr__(self):
        return f"<AnalyticsEvent(type={self.event_type}, user_id={self.user_id}, timestamp={self.timestamp})>"