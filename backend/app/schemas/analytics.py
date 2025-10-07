from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from enum import Enum
from ..models.analytics import AnalyticsEventType


class TimePeriod(str, Enum):
    HOUR = "hour"
    DAY = "day"
    WEEK = "week"
    MONTH = "month"
    YEAR = "year"


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


# Real-time Analytics Schemas
class RealTimeMetrics(BaseModel):
    """Real-time system metrics"""
    current_online_users: int
    active_sessions: int
    contributions_last_hour: int
    approvals_last_hour: int
    rejections_last_hour: int
    average_response_time_ms: float
    cache_hit_rate: float
    system_load: Dict[str, float]
    last_updated: datetime


class MetricsAggregation(BaseModel):
    """Aggregated metrics response"""
    period: str
    value: float
    percentage_change: Optional[float] = None
    trend: Optional[str] = None  # "up", "down", "stable"


class TrendAnalysis(BaseModel):
    """Trend analysis for metrics over time"""
    metric_name: str
    time_series: List[MetricsAggregation]
    overall_trend: str
    growth_rate: Optional[float] = None
    seasonality_detected: bool = False


class AnalyticsFilter(BaseModel):
    """Filter options for analytics queries"""
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    user_ids: Optional[List[int]] = None
    category_ids: Optional[List[int]] = None
    contribution_ids: Optional[List[int]] = None
    event_types: Optional[List[AnalyticsEventType]] = None
    activity_types: Optional[List[str]] = None
    granularity: Optional[TimePeriod] = TimePeriod.DAY


class UserActivityCreate(BaseModel):
    activity_type: str
    resource_type: Optional[str] = None
    resource_id: Optional[int] = None
    session_id: Optional[str] = None
    ip_address: Optional[str] = None
    user_agent: Optional[str] = None
    duration_seconds: Optional[int] = None
    activity_metadata: Optional[Dict[str, Any]] = None


class UserActivityResponse(BaseModel):
    id: int
    user_id: int
    activity_type: str
    resource_type: Optional[str] = None
    resource_id: Optional[int] = None
    session_id: Optional[str] = None
    ip_address: Optional[str] = None
    user_agent: Optional[str] = None
    duration_seconds: Optional[int] = None
    activity_metadata: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True


class SystemMetricCreate(BaseModel):
    metric_name: str
    metric_value: float
    metric_unit: Optional[str] = None
    category: str
    period_start: datetime
    period_end: datetime
    granularity: TimePeriod


class SystemMetricResponse(BaseModel):
    id: int
    metric_name: str
    metric_value: float
    metric_unit: Optional[str] = None
    category: str
    period_start: datetime
    period_end: datetime
    granularity: str
    created_at: datetime

    class Config:
        from_attributes = True


class DashboardOverview(BaseModel):
    """Enhanced dashboard overview with real-time metrics"""
    total_users: int
    active_users_today: int
    active_users_this_week: int
    total_contributions: int
    contributions_today: int
    contributions_this_week: int
    approval_rate_today: float
    approval_rate_this_week: float
    average_quality_score: Optional[float] = None
    top_contributors_today: List[Dict[str, Any]]
    popular_categories: List[Dict[str, Any]]
    system_health: Dict[str, Any]
    real_time_metrics: RealTimeMetrics


class UserPerformanceMetrics(BaseModel):
    """Detailed user performance analytics"""
    user_id: int
    period_start: datetime
    period_end: datetime
    contributions_count: int
    approval_rate: float
    average_quality_score: Optional[float] = None
    time_to_approval_avg_hours: Optional[float] = None
    most_active_category: Optional[str] = None
    learning_progress: Dict[str, Any]
    engagement_score: float
    streak_data: Dict[str, Any]


class CategoryPerformanceMetrics(BaseModel):
    """Category-specific performance metrics"""
    category_id: int
    category_name: str
    period_start: datetime
    period_end: datetime
    total_contributions: int
    approval_rate: float
    unique_contributors: int
    average_quality_score: Optional[float] = None
    difficulty_distribution: Dict[str, int]
    engagement_metrics: Dict[str, Any]


class QualityAnalytics(BaseModel):
    """Quality assessment analytics"""
    period_start: datetime
    period_end: datetime
    overall_quality_score: float
    quality_by_category: Dict[str, float]
    quality_by_user_role: Dict[str, float]
    common_issues: List[Dict[str, Any]]
    improvement_suggestions: List[str]
    quality_trends: TrendAnalysis


class LearningAnalytics(BaseModel):
    """Language learning analytics"""
    total_learners: int
    active_learners_today: int
    completion_rates: Dict[str, float]  # by difficulty level
    popular_learning_areas: List[Dict[str, Any]]
    average_study_time_minutes: float
    pronunciation_accuracy: Optional[float] = None
    verb_mastery_rates: Dict[str, float]
    learning_path_effectiveness: Dict[str, float]