from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

# Import all models to ensure they are registered with Base
from ..models import user, contribution, audit_log, category, sub_translation, analytics
from ..models import content_rating, morphology, webhook