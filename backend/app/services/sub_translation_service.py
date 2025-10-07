from typing import List, Optional, Dict, Any
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func
import re
import json
from ..models.sub_translation import SubTranslation, DifficultyLevel
from ..models.contribution import Contribution
from ..models.user import User
from ..schemas.sub_translation import (
    SubTranslationCreate, SubTranslationUpdate, SubTranslationBatch,
    WordSegmentation, SubTranslationStats
)


class SubTranslationService:
    @staticmethod
    def create_sub_translation(
        db: Session, 
        sub_translation_data: SubTranslationCreate, 
        user: User
    ) -> SubTranslation:
        """Create a single sub-translation"""
        db_sub_translation = SubTranslation(
            parent_contribution_id=sub_translation_data.parent_contribution_id,
            source_word=sub_translation_data.source_word,
            target_word=sub_translation_data.target_word,
            context=sub_translation_data.context,
            word_position=sub_translation_data.word_position,
            difficulty_level=sub_translation_data.difficulty_level,
            confidence_score=sub_translation_data.confidence_score,
            category_id=sub_translation_data.category_id,
            created_by_id=user.id
        )
        db.add(db_sub_translation)
        db.commit()
        db.refresh(db_sub_translation)
        
        # Update parent contribution's has_sub_translations flag
        contribution = db.query(Contribution).filter(
            Contribution.id == sub_translation_data.parent_contribution_id
        ).first()
        if contribution:
            contribution.has_sub_translations = True
            db.commit()
        
        return db_sub_translation
    
    @staticmethod
    def create_sub_translations_batch(
        db: Session,
        batch_data: SubTranslationBatch,
        user: User
    ) -> List[SubTranslation]:
        """Create multiple sub-translations for a contribution"""
        created_sub_translations = []
        
        for i, sub_trans_data in enumerate(batch_data.sub_translations):
            sub_translation = SubTranslation(
                parent_contribution_id=batch_data.parent_contribution_id,
                source_word=sub_trans_data.source_word,
                target_word=sub_trans_data.target_word,
                context=sub_trans_data.context,
                word_position=sub_trans_data.word_position or i,
                difficulty_level=sub_trans_data.difficulty_level,
                confidence_score=sub_trans_data.confidence_score,
                category_id=sub_trans_data.category_id,
                created_by_id=user.id
            )
            db.add(sub_translation)
            created_sub_translations.append(sub_translation)
        
        # Update parent contribution
        contribution = db.query(Contribution).filter(
            Contribution.id == batch_data.parent_contribution_id
        ).first()
        if contribution:
            contribution.has_sub_translations = True
            db.commit()
        
        db.commit()
        for sub_trans in created_sub_translations:
            db.refresh(sub_trans)
        
        return created_sub_translations
    
    @staticmethod
    def get_sub_translations_by_contribution(
        db: Session,
        contribution_id: int
    ) -> List[SubTranslation]:
        """Get all sub-translations for a contribution"""
        return db.query(SubTranslation).options(
            joinedload(SubTranslation.category)
        ).filter(
            SubTranslation.parent_contribution_id == contribution_id
        ).order_by(SubTranslation.word_position).all()
    
    @staticmethod
    def get_sub_translation_by_id(db: Session, sub_translation_id: int) -> Optional[SubTranslation]:
        """Get a specific sub-translation by ID"""
        return db.query(SubTranslation).options(
            joinedload(SubTranslation.category),
            joinedload(SubTranslation.parent_contribution)
        ).filter(SubTranslation.id == sub_translation_id).first()
    
    @staticmethod
    def update_sub_translation(
        db: Session,
        sub_translation_id: int,
        update_data: SubTranslationUpdate
    ) -> Optional[SubTranslation]:
        """Update a sub-translation"""
        sub_translation = db.query(SubTranslation).filter(
            SubTranslation.id == sub_translation_id
        ).first()
        
        if not sub_translation:
            return None
        
        update_dict = update_data.dict(exclude_unset=True)
        for field, value in update_dict.items():
            setattr(sub_translation, field, value)
        
        db.commit()
        db.refresh(sub_translation)
        return sub_translation
    
    @staticmethod
    def delete_sub_translation(db: Session, sub_translation_id: int) -> bool:
        """Delete a sub-translation"""
        sub_translation = db.query(SubTranslation).filter(
            SubTranslation.id == sub_translation_id
        ).first()
        
        if not sub_translation:
            return False
        
        contribution_id = sub_translation.parent_contribution_id
        db.delete(sub_translation)
        db.commit()
        
        # Check if this was the last sub-translation for the contribution
        remaining_count = db.query(func.count(SubTranslation.id)).filter(
            SubTranslation.parent_contribution_id == contribution_id
        ).scalar()
        
        if remaining_count == 0:
            contribution = db.query(Contribution).filter(
                Contribution.id == contribution_id
            ).first()
            if contribution:
                contribution.has_sub_translations = False
                db.commit()
        
        return True
    
    @staticmethod
    def segment_text(text: str) -> WordSegmentation:
        """Automatically segment text into words with position information"""
        # Simple word segmentation for Kikuyu text
        # This can be enhanced with more sophisticated NLP techniques
        
        # Clean and split text
        words = re.findall(r'\b\w+\b', text.strip())
        
        segments = []
        suggested_translations = {}
        
        # Basic translation suggestions (can be enhanced with ML/dictionary lookup)
        common_translations = {
            'Nĩngwenda': 'I want',
            'mũtumia': 'woman',
            'mũringa': 'beautiful',
            'Wĩrĩ': 'Hello',
            'mũciĩ': 'home',
            'mwega': 'good',
            'nĩ': 'is/it is',
            'mũndũ': 'person',
            'kana': 'or',
            'na': 'and/with',
            'rĩu': 'now',
            'tene': 'long ago',
            'igũrũ': 'above/sky',
            'thĩ': 'earth/ground'
        }
        
        for i, word in enumerate(words):
            suggested_translation = common_translations.get(word, "")
            
            segments.append({
                "word": word,
                "position": i,
                "suggested_translation": suggested_translation
            })
            
            if suggested_translation:
                suggested_translations[word] = suggested_translation
        
        return WordSegmentation(
            original_text=text,
            segments=segments,
            suggested_translations=suggested_translations
        )
    
    @staticmethod
    def get_sub_translation_stats(db: Session, contribution_id: Optional[int] = None) -> SubTranslationStats:
        """Get statistics for sub-translations"""
        query = db.query(SubTranslation)
        
        if contribution_id:
            query = query.filter(SubTranslation.parent_contribution_id == contribution_id)
        
        total_sub_translations = query.count()
        
        # Get counts by difficulty
        difficulty_counts = db.query(
            SubTranslation.difficulty_level,
            func.count(SubTranslation.id)
        ).group_by(SubTranslation.difficulty_level)
        
        if contribution_id:
            difficulty_counts = difficulty_counts.filter(
                SubTranslation.parent_contribution_id == contribution_id
            )
        
        by_difficulty = {
            difficulty.value: count 
            for difficulty, count in difficulty_counts.all()
        }
        
        # Get counts by category
        category_counts = db.query(
            func.coalesce(SubTranslation.category_id, 0).label('category_id'),
            func.count(SubTranslation.id)
        ).group_by('category_id')
        
        if contribution_id:
            category_counts = category_counts.filter(
                SubTranslation.parent_contribution_id == contribution_id
            )
        
        by_category = {
            str(cat_id) if cat_id != 0 else "uncategorized": count
            for cat_id, count in category_counts.all()
        }
        
        # Get average confidence
        avg_confidence = query.with_entities(
            func.avg(SubTranslation.confidence_score)
        ).scalar() or 0.0
        
        return SubTranslationStats(
            total_sub_translations=total_sub_translations,
            by_difficulty=by_difficulty,
            by_category=by_category,
            average_confidence=avg_confidence
        )
    
    @staticmethod
    def search_sub_translations(
        db: Session,
        search_query: str,
        difficulty_level: Optional[DifficultyLevel] = None,
        category_id: Optional[int] = None,
        limit: int = 50
    ) -> List[SubTranslation]:
        """Search sub-translations by source or target word"""
        query = db.query(SubTranslation).options(
            joinedload(SubTranslation.category)
        )
        
        # Text search
        query = query.filter(
            SubTranslation.source_word.ilike(f"%{search_query}%") |
            SubTranslation.target_word.ilike(f"%{search_query}%")
        )
        
        # Filters
        if difficulty_level:
            query = query.filter(SubTranslation.difficulty_level == difficulty_level)
        
        if category_id:
            query = query.filter(SubTranslation.category_id == category_id)
        
        return query.order_by(SubTranslation.source_word).limit(limit).all()
    
    @staticmethod
    def get_popular_translations(
        db: Session,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """Get the most frequently translated words"""
        popular = db.query(
            SubTranslation.source_word,
            SubTranslation.target_word,
            func.count(SubTranslation.id).label('frequency')
        ).group_by(
            SubTranslation.source_word,
            SubTranslation.target_word
        ).order_by(
            func.count(SubTranslation.id).desc()
        ).limit(limit).all()
        
        return [
            {
                "source_word": source,
                "target_word": target,
                "frequency": freq
            }
            for source, target, freq in popular
        ]