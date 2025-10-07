from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, Text, ForeignKey, Enum as SQLEnum, Float
from sqlalchemy.orm import relationship
from ..db.base import Base


class DifficultyLevel(str, Enum):
    BEGINNER = "beginner"
    INTERMEDIATE = "intermediate"
    ADVANCED = "advanced"


class SubTranslation(Base):
    __tablename__ = "sub_translations"

    id = Column(Integer, primary_key=True, index=True)
    parent_contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=False)
    source_word = Column(String(200), nullable=False, index=True)
    target_word = Column(String(200), nullable=False, index=True)
    context = Column(Text)  # Context within the original sentence
    word_position = Column(Integer, nullable=False)  # Position in the original sentence
    difficulty_level = Column(SQLEnum(DifficultyLevel), default=DifficultyLevel.BEGINNER)
    confidence_score = Column(Float, default=1.0)  # User confidence in translation (0.0-1.0)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=True)
    created_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    parent_contribution = relationship("Contribution", back_populates="sub_translations")
    category = relationship("Category", back_populates="sub_translations")
    created_by = relationship("User")
    
    def __repr__(self):
        return f"<SubTranslation(id={self.id}, source='{self.source_word}', target='{self.target_word}')>"
    
    @property
    def difficulty_score(self):
        """Convert difficulty level to numeric score for sorting"""
        difficulty_scores = {
            DifficultyLevel.BEGINNER: 1,
            DifficultyLevel.INTERMEDIATE: 2,
            DifficultyLevel.ADVANCED: 3
        }
        return difficulty_scores.get(self.difficulty_level, 1)