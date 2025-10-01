from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

from ..models.user import User
from ..models.contribution import Contribution  
from ..models.audit_log import AuditLog