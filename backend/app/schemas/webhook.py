from pydantic import BaseModel, HttpUrl, validator
from typing import List, Dict, Any, Optional
from datetime import datetime
from ..models.webhook import WebhookEvent, WebhookStatus


class WebhookCreate(BaseModel):
    name: str
    url: HttpUrl
    events: List[WebhookEvent]
    secret: Optional[str] = None
    headers: Optional[Dict[str, str]] = None
    timeout_seconds: int = 30
    retry_count: int = 3
    
    @validator('name')
    def validate_name(cls, v):
        if not v or len(v.strip()) < 3:
            raise ValueError('Name must be at least 3 characters')
        return v.strip()
    
    @validator('events')
    def validate_events(cls, v):
        if not v:
            raise ValueError('At least one event must be selected')
        return v
    
    @validator('timeout_seconds')
    def validate_timeout(cls, v):
        if v < 5 or v > 300:
            raise ValueError('Timeout must be between 5 and 300 seconds')
        return v
    
    @validator('retry_count')
    def validate_retry_count(cls, v):
        if v < 0 or v > 10:
            raise ValueError('Retry count must be between 0 and 10')
        return v


class WebhookUpdate(BaseModel):
    name: Optional[str] = None
    url: Optional[HttpUrl] = None
    events: Optional[List[WebhookEvent]] = None
    secret: Optional[str] = None
    headers: Optional[Dict[str, str]] = None
    timeout_seconds: Optional[int] = None
    retry_count: Optional[int] = None
    status: Optional[WebhookStatus] = None


class WebhookResponse(BaseModel):
    id: int
    name: str
    url: str
    events: List[str]
    status: WebhookStatus
    timeout_seconds: int
    retry_count: int
    created_at: datetime
    updated_at: Optional[datetime]
    last_triggered_at: Optional[datetime]
    total_calls: int
    successful_calls: int
    failed_calls: int
    
    class Config:
        from_attributes = True


class WebhookDeliveryResponse(BaseModel):
    id: int
    event_type: WebhookEvent
    status_code: Optional[int]
    error_message: Optional[str]
    created_at: datetime
    delivered_at: Optional[datetime]
    duration_ms: Optional[int]
    attempt_number: int
    is_successful: bool
    
    class Config:
        from_attributes = True


class WebhookStatsResponse(BaseModel):
    webhook_id: int
    name: str
    status: str
    total_calls: int
    successful_calls: int
    failed_calls: int
    success_rate: float
    recent_success_rate: float
    last_triggered: Optional[str]
    avg_response_time: Optional[float]


class WebhookPayload(BaseModel):
    """Base webhook payload structure"""
    event: str
    timestamp: str
    data: Dict[str, Any]


class ContributionWebhookData(BaseModel):
    """Webhook data for contribution events"""
    contribution_id: int
    source_text: str
    target_text: str
    language: str
    status: str
    contributor_id: int
    contributor_name: str
    categories: List[str]
    difficulty_level: str
    quality_score: Optional[float]


class UserWebhookData(BaseModel):
    """Webhook data for user events"""
    user_id: int
    email: str
    display_name: str
    role: str
    created_at: str


class StatsWebhookData(BaseModel):
    """Webhook data for statistics events"""
    date: str
    total_contributions: int
    approved_contributions: int
    pending_contributions: int
    new_users: int
    active_contributors: int
    categories_count: int