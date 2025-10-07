from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.contribution import Contribution, ContributionStatus
from ..models.user import User
from ..schemas.contribution import ContributionCreate, ContributionUpdate
from ..core.cache import cache, cached, CacheConfig, invalidate_cache_on_change, cache_manager


class ContributionService:
    @staticmethod
    @invalidate_cache_on_change(["contributions:*", "popular_translations:*", "export_data:*", "category_stats:*"])
    def create_contribution(db: Session, contribution_data: ContributionCreate, user: User) -> Contribution:
        db_contribution = Contribution(
            source_text=contribution_data.source_text,
            target_text=contribution_data.target_text,
            language=contribution_data.language,
            created_by_id=user.id
        )
        db.add(db_contribution)
        db.commit()
        db.refresh(db_contribution)
        return db_contribution
    
    @staticmethod
    @cached(ttl=CacheConfig.DEFAULT_TTL, key_prefix="contributions")
    def get_contributions(
        db: Session, 
        status: Optional[ContributionStatus] = None,
        user_id: Optional[int] = None,
        skip: int = 0,
        limit: int = 100
    ) -> List[Contribution]:
        query = db.query(Contribution)
        
        if status:
            query = query.filter(Contribution.status == status)
        
        if user_id:
            query = query.filter(Contribution.created_by_id == user_id)
        
        return query.offset(skip).limit(limit).all()
    
    @staticmethod
    @cached(ttl=CacheConfig.DEFAULT_TTL, key_prefix="contribution")
    def get_contribution_by_id(db: Session, contribution_id: int) -> Optional[Contribution]:
        return db.query(Contribution).filter(Contribution.id == contribution_id).first()
    
    @staticmethod
    @invalidate_cache_on_change(["contributions:*", "contribution:*", "popular_translations:*", "export_data:*", "category_stats:*"])
    def update_contribution_status(
        db: Session, 
        contribution_id: int, 
        status: ContributionStatus
    ) -> Optional[Contribution]:
        contribution = db.query(Contribution).filter(Contribution.id == contribution_id).first()
        if contribution:
            contribution.status = status
            db.commit()
            db.refresh(contribution)
        return contribution
    
    @staticmethod
    @invalidate_cache_on_change(["contributions:*", "contribution:*", "popular_translations:*", "export_data:*", "category_stats:*"])
    def update_contribution(
        db: Session, 
        contribution_id: int, 
        update_data: ContributionUpdate
    ) -> Optional[Contribution]:
        contribution = db.query(Contribution).filter(Contribution.id == contribution_id).first()
        if contribution:
            # Update only provided fields
            if update_data.source_text is not None:
                contribution.source_text = update_data.source_text
            if update_data.target_text is not None:
                contribution.target_text = update_data.target_text
            if update_data.language is not None:
                contribution.language = update_data.language
            
            db.commit()
            db.refresh(contribution)
        return contribution
    
    @staticmethod
    @invalidate_cache_on_change(["contributions:*", "contribution:*", "popular_translations:*", "export_data:*", "category_stats:*"])
    def delete_contribution(db: Session, contribution_id: int) -> bool:
        contribution = db.query(Contribution).filter(Contribution.id == contribution_id).first()
        if contribution:
            db.delete(contribution)
            db.commit()
            return True
        return False