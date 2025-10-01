from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Text, ForeignKey, Enum as SQLEnum, Table, Float, Boolean
from sqlalchemy.orm import relationship
from ..db.base import Base


class ContributionStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class DifficultyLevel(str, Enum):
    BEGINNER = "beginner"
    INTERMEDIATE = "intermediate"
    ADVANCED = "advanced"


# Association table for many-to-many relationship between contributions and categories
contribution_categories = Table(
    'contribution_categories',
    Base.metadata,
    Column('contribution_id', Integer, ForeignKey('contributions.id'), primary_key=True),
    Column('category_id', Integer, ForeignKey('categories.id'), primary_key=True),
    Column('created_at', DateTime, default=datetime.utcnow)
)


class Contribution(Base):
    __tablename__ = "contributions"

    id = Column(Integer, primary_key=True, index=True)
    source_text = Column(Text, nullable=False)
    target_text = Column(Text, nullable=False)
    status = Column(SQLEnum(ContributionStatus), default=ContributionStatus.PENDING, nullable=False)
    language = Column(String, default="kikuyu", nullable=False)
    
    # Enhanced metadata fields
    difficulty_level = Column(SQLEnum(DifficultyLevel), default=DifficultyLevel.BEGINNER)
    context_notes = Column(Text)  # Additional context or usage notes
    cultural_notes = Column(Text)  # Cultural significance or usage
    pronunciation_guide = Column(String(500))  # Phonetic pronunciation
    usage_examples = Column(Text)  # JSON array of usage examples
    quality_score = Column(Float, default=0.0)  # Community-rated quality (0.0-5.0)
    is_phrase = Column(Boolean, default=False)  # Whether this is a phrase vs single word
    has_sub_translations = Column(Boolean, default=False)  # Whether sub-translations exist
    
    created_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    created_by = relationship("User", back_populates="contributions")
    audit_logs = relationship("AuditLog", back_populates="contribution")
    categories = relationship("Category", secondary=contribution_categories, back_populates="contributions")
    sub_translations = relationship("SubTranslation", back_populates="parent_contribution", cascade="all, delete-orphan")
    
    def __repr__(self):
        return f"<Contribution(id={self.id}, status={self.status}, source='{self.source_text[:50]}...')>"
    
    @property
    def word_count(self):
        """Count words in the source text"""
        return len(self.source_text.split()) if self.source_text else 0
    
    @property
    def primary_category(self):
        """Get the first category assigned to this contribution"""
        return self.categories[0] if self.categories else None