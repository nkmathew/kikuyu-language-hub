#!/usr/bin/env python3
"""Seed script for the Kikuyu Language Hub database."""

from sqlalchemy.orm import Session
from .db.session import SessionLocal, engine
from .models.user import User, UserRole
from .models.contribution import Contribution, ContributionStatus
from .services.auth_service import AuthService
from .db.base import Base


def create_tables():
    """Create database tables."""
    Base.metadata.create_all(bind=engine)


def seed_users(db: Session):
    """Create initial users."""
    # Check if admin already exists
    admin = db.query(User).filter(User.email == "admin@kikuyu.hub").first()
    if not admin:
        admin = User(
            email="admin@kikuyu.hub",
            password_hash=AuthService.get_password_hash("admin123"),
            role=UserRole.ADMIN
        )
        db.add(admin)
    
    # Check if moderator already exists
    moderator = db.query(User).filter(User.email == "moderator@kikuyu.hub").first()
    if not moderator:
        moderator = User(
            email="moderator@kikuyu.hub",
            password_hash=AuthService.get_password_hash("mod123"),
            role=UserRole.MODERATOR
        )
        db.add(moderator)
    
    # Check if contributor already exists
    contributor = db.query(User).filter(User.email == "contributor@kikuyu.hub").first()
    if not contributor:
        contributor = User(
            email="contributor@kikuyu.hub",
            password_hash=AuthService.get_password_hash("contrib123"),
            role=UserRole.CONTRIBUTOR
        )
        db.add(contributor)
    
    db.commit()
    return admin, moderator, contributor


def seed_contributions(db: Session, contributor: User):
    """Create sample contributions."""
    sample_contributions = [
        {
            "source_text": "Hello",
            "target_text": "Wĩra",
            "status": ContributionStatus.APPROVED
        },
        {
            "source_text": "Good morning",
            "target_text": "Rũciinĩ rũega",
            "status": ContributionStatus.APPROVED
        },
        {
            "source_text": "Thank you",
            "target_text": "Nĩ ngũkũmenyithia",
            "status": ContributionStatus.APPROVED
        },
        {
            "source_text": "Water",
            "target_text": "Maaĩ",
            "status": ContributionStatus.PENDING
        },
        {
            "source_text": "Food",
            "target_text": "Irio",
            "status": ContributionStatus.PENDING
        }
    ]
    
    for contrib_data in sample_contributions:
        existing = db.query(Contribution).filter(
            Contribution.source_text == contrib_data["source_text"]
        ).first()
        
        if not existing:
            contribution = Contribution(
                source_text=contrib_data["source_text"],
                target_text=contrib_data["target_text"],
                status=contrib_data["status"],
                language="kikuyu",
                created_by_id=contributor.id
            )
            db.add(contribution)
    
    db.commit()


def main():
    """Main seeding function."""
    print("Creating database tables...")
    create_tables()
    
    print("Seeding database...")
    db = SessionLocal()
    try:
        admin, moderator, contributor = seed_users(db)
        print(f"Created users: admin, moderator, contributor")
        
        seed_contributions(db, contributor)
        print("Created sample contributions")
        
        print("Database seeding completed successfully!")
        print("\nDefault login credentials:")
        print("Admin: admin@kikuyu.hub / admin123")
        print("Moderator: moderator@kikuyu.hub / mod123")
        print("Contributor: contributor@kikuyu.hub / contrib123")
        
    finally:
        db.close()


if __name__ == "__main__":
    main()