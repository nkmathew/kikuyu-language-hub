"""
Content rating and tagging models for age-appropriate content management
"""
from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Text, ForeignKey, Enum as SQLEnum, Boolean
from sqlalchemy.orm import relationship
from ..db.base import Base


class ContentRating(str, Enum):
    """Content rating levels"""
    GENERAL = "general"         # G - General audiences, all ages
    PARENTAL_GUIDANCE = "pg"    # PG - Parental guidance suggested
    TEENS = "teens"             # T - Teen content (13+)
    MATURE = "mature"           # M - Mature content (17+)
    ADULT_ONLY = "adult"        # AO - Adult only (18+)


class ContentWarningType(str, Enum):
    """Specific content warning types"""
    STRONG_LANGUAGE = "strong_language"
    SEXUAL_CONTENT = "sexual_content"
    VIOLENCE = "violence"
    SUBSTANCE_USE = "substance_use"
    CULTURAL_SENSITIVE = "cultural_sensitive"
    RELIGIOUS_CONTENT = "religious_content"
    POLITICAL_CONTENT = "political_content"
    MATURE_THEMES = "mature_themes"


class ContributionRating(Base):
    """Content rating for contributions"""
    __tablename__ = "contribution_ratings"
    
    id = Column(Integer, primary_key=True, index=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=False, unique=True)
    
    # Rating information
    content_rating = Column(SQLEnum(ContentRating), default=ContentRating.GENERAL, nullable=False)
    is_adult_content = Column(Boolean, default=False, nullable=False)
    requires_warning = Column(Boolean, default=False, nullable=False)
    
    # Content warnings (JSON array of ContentWarningType values)
    content_warnings = Column(Text)  # JSON array of warning types
    
    # Rating details
    rating_reason = Column(Text)  # Explanation for the rating
    reviewer_notes = Column(Text)  # Internal moderator notes
    
    # Rating metadata
    rated_by_id = Column(Integer, ForeignKey("users.id"))  # Who assigned the rating
    auto_rated = Column(Boolean, default=False)  # Whether automatically assigned
    rating_confidence = Column(Integer, default=100)  # Confidence in rating (0-100)
    
    # Timestamps
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    contribution = relationship("Contribution", back_populates="rating")
    rated_by = relationship("User")


class ContentFilter(Base):
    """User content filtering preferences"""
    __tablename__ = "content_filters"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, unique=True)
    
    # Filter settings
    max_content_rating = Column(SQLEnum(ContentRating), default=ContentRating.GENERAL, nullable=False)
    hide_adult_content = Column(Boolean, default=True, nullable=False)
    hide_content_warnings = Column(Boolean, default=False, nullable=False)
    
    # Specific warning filters (JSON array of ContentWarningType values to hide)
    hidden_warning_types = Column(Text)  # JSON array
    
    # Parental controls
    is_parental_controlled = Column(Boolean, default=False, nullable=False)
    parental_pin = Column(String(255))  # Hashed PIN for parental controls
    
    # Timestamps
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    user = relationship("User", back_populates="content_filter")


class ContentAuditLog(Base):
    """Audit log for content rating changes"""
    __tablename__ = "content_audit_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=False)
    
    # Rating change details
    old_rating = Column(SQLEnum(ContentRating))
    new_rating = Column(SQLEnum(ContentRating))
    old_warnings = Column(Text)  # JSON array
    new_warnings = Column(Text)  # JSON array
    
    # Change metadata
    changed_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    change_reason = Column(Text)
    auto_generated = Column(Boolean, default=False)
    
    # Timestamp
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    
    # Relationships
    contribution = relationship("Contribution")
    changed_by = relationship("User")