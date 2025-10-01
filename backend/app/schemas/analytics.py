from datetime import datetime
from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from ..models.analytics import AnalyticsEventType


class UserAnalyticsResponse(BaseModel):
    id: int
    user_id: int
    total_contributions: int
    approved_contributions: int
    rejected_contributions: int
    total_sub_translations: int
    total_words_contributed: int
    average_approval_rate: float
    streak_days: int
    last_contribution_date: Optional[datetime]
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True


class CategoryAnalyticsResponse(BaseModel):
    id: int
    category_id: int
    total_contributions: int
    approved_contributions: int
    total_sub_translations: int
    unique_contributors: int
    average_quality_score: float
    last_contribution_date: Optional[datetime]
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True


class AnalyticsEventCreate(BaseModel):
    event_type: AnalyticsEventType
    user_id: Optional[int] = None
    contribution_id: Optional[int] = None
    category_id: Optional[int] = None
    event_metadata: Optional[str] = None  # JSON string


class AnalyticsEventResponse(BaseModel):
    id: int
    event_type: AnalyticsEventType
    user_id: Optional[int]
    contribution_id: Optional[int]
    category_id: Optional[int]
    event_metadata: Optional[str]
    timestamp: datetime
    
    class Config:
        from_attributes = True


class DashboardStats(BaseModel):
    """Main dashboard statistics"""
    total_users: int
    total_contributions: int
    approved_contributions: int
    pending_contributions: int
    rejected_contributions: int
    total_sub_translations: int
    total_categories: int
    active_contributors_this_month: int
    approval_rate: float
    
    
class UserDashboardStats(BaseModel):
    """User-specific dashboard statistics"""
    user_id: int
    total_contributions: int
    approved_contributions: int
    pending_contributions: int
    rejected_contributions: int
    total_sub_translations: int
    approval_rate: float
    streak_days: int
    rank: Optional[int] = None
    contribution_trend: List[Dict[str, Any]] = []  # Date and count data
    
    
class CategoryContributionStats(BaseModel):
    """Contribution statistics by category"""
    category_id: int
    category_name: str
    total_contributions: int
    approved_contributions: int
    pending_contributions: int
    unique_contributors: int
    
    
class ContributionTrend(BaseModel):
    """Time-series data for contribution trends"""
    date: datetime
    contributions: int
    approvals: int
    rejections: int
    
    
class ExportStats(BaseModel):
    """Statistics for exports"""
    total_approved_translations: int
    translations_by_category: Dict[str, int]
    translations_by_difficulty: Dict[str, int]
    last_export_date: Optional[datetime]
    export_format_usage: Dict[str, int]
    
    
class LeaderboardEntry(BaseModel):
    """User leaderboard entry"""
    user_id: int
    username: str
    total_contributions: int
    approved_contributions: int
    approval_rate: float
    streak_days: int
    rank: int
    
    
class PlatformHealth(BaseModel):
    """Overall platform health metrics"""
    daily_active_users: int
    weekly_active_users: int
    monthly_active_users: int
    average_time_to_approval: float  # in hours
    moderator_workload: int  # pending contributions per moderator
    quality_score: float  # average quality of approved contributions
    growth_rate: float  # percentage growth in contributions this month