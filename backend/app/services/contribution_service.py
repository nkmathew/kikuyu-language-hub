from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.contribution import Contribution, ContributionStatus
from ..models.user import User
from ..schemas.contribution import ContributionCreate, ContributionUpdate


class ContributionService:
    @staticmethod
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
    def get_contribution_by_id(db: Session, contribution_id: int) -> Optional[Contribution]:
        return db.query(Contribution).filter(Contribution.id == contribution_id).first()
    
    @staticmethod
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