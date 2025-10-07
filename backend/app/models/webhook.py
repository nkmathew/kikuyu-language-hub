from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from ..db.base import Base
import enum


class WebhookEvent(str, enum.Enum):
    CONTRIBUTION_CREATED = "contribution.created"
    CONTRIBUTION_APPROVED = "contribution.approved"
    CONTRIBUTION_REJECTED = "contribution.rejected"
    CONTRIBUTION_UPDATED = "contribution.updated"
    USER_REGISTERED = "user.registered"
    QUALITY_THRESHOLD_REACHED = "quality.threshold_reached"
    DAILY_STATS_UPDATE = "stats.daily_update"


class WebhookStatus(str, enum.Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    FAILED = "failed"


class Webhook(Base):
    __tablename__ = "webhooks"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    url = Column(String(500), nullable=False)
    events = Column(Text, nullable=False)  # JSON array of WebhookEvent values
    secret = Column(String(100), nullable=True)  # For HMAC verification
    status = Column(SQLEnum(WebhookStatus), default=WebhookStatus.ACTIVE, index=True)
    
    # Configuration
    timeout_seconds = Column(Integer, default=30)
    retry_count = Column(Integer, default=3)
    headers = Column(Text, nullable=True)  # JSON object for custom headers
    
    # Tracking
    created_by_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    last_triggered_at = Column(DateTime(timezone=True), nullable=True)
    total_calls = Column(Integer, default=0)
    successful_calls = Column(Integer, default=0)
    failed_calls = Column(Integer, default=0)
    
    # Relationships
    created_by = relationship("User", back_populates="webhooks")
    deliveries = relationship("WebhookDelivery", back_populates="webhook", cascade="all, delete-orphan")


class WebhookDelivery(Base):
    __tablename__ = "webhook_deliveries"
    
    id = Column(Integer, primary_key=True, index=True)
    webhook_id = Column(Integer, ForeignKey("webhooks.id"), nullable=False, index=True)
    event_type = Column(SQLEnum(WebhookEvent), nullable=False, index=True)
    
    # Request details
    payload = Column(Text, nullable=False)  # JSON payload sent
    headers_sent = Column(Text, nullable=True)  # JSON object of headers sent
    
    # Response details
    status_code = Column(Integer, nullable=True)
    response_body = Column(Text, nullable=True)
    response_headers = Column(Text, nullable=True)  # JSON object
    error_message = Column(Text, nullable=True)
    
    # Timing
    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    delivered_at = Column(DateTime(timezone=True), nullable=True)
    duration_ms = Column(Integer, nullable=True)
    
    # Retry tracking
    attempt_number = Column(Integer, default=1)
    is_successful = Column(Boolean, default=False, index=True)
    
    # Relationships
    webhook = relationship("Webhook", back_populates="deliveries")