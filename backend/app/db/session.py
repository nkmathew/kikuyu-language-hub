from sqlalchemy.orm import sessionmaker
from .connection import engine

# Use the optimized engine with connection pooling and performance monitoring
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


