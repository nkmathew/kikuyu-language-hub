"""Analytics API Routes - Dashboard and metrics endpoints"""
from datetime import datetime, timedelta, timezone
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from ...core.security import get_current_user, require_moderator_or_admin
from ...db.session import get_db
from ...models.user import User
from ...schemas.analytics import (
    AnalyticsEventCreate, AnalyticsEventResponse, UserActivityCreate, 
    UserActivityResponse, SystemMetricCreate, SystemMetricResponse,
    DashboardOverview, RealTimeMetrics, TrendAnalysis, AnalyticsFilter,
    TimePeriod, UserPerformanceMetrics, CategoryPerformanceMetrics,
    QualityAnalytics, LearningAnalytics, DashboardStats, UserDashboardStats,
    PlatformHealth, LeaderboardEntry
)
from ...services.analytics_service import AnalyticsService
from ...core.cache import cache
import logging

router = APIRouter(prefix="/analytics", tags=["analytics"])
logger = logging.getLogger(__name__)


@router.get("/dashboard/overview", response_model=DashboardOverview)
def get_dashboard_overview(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get comprehensive dashboard overview with real-time metrics"""
    try:
        return AnalyticsService.get_dashboard_overview(db)
    except Exception as e:
        logger.error(f"Error getting dashboard overview: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve dashboard overview"
        )


@router.get("/dashboard/realtime", response_model=RealTimeMetrics)
def get_real_time_metrics(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get real-time system metrics"""
    try:
        return AnalyticsService.get_real_time_metrics(db)
    except Exception as e:
        logger.error(f"Error getting real-time metrics: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve real-time metrics"
        )


@router.get("/dashboard/stats", response_model=DashboardStats)
def get_dashboard_stats(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get basic dashboard statistics (legacy endpoint)"""
    try:
        from ..services.analytics_service_legacy import AnalyticsServiceLegacy
        return AnalyticsServiceLegacy.get_dashboard_stats(db)
    except Exception as e:
        logger.error(f"Error getting dashboard stats: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve dashboard statistics"
        )


@router.get("/user/{user_id}", response_model=UserDashboardStats)
def get_user_dashboard_stats(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get user-specific dashboard statistics"""
    # Users can only access their own stats unless they're moderator/admin
    if current_user.id != user_id and current_user.role not in ["moderator", "admin"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied"
        )
    
    try:
        from ..services.analytics_service_legacy import AnalyticsServiceLegacy
        return AnalyticsServiceLegacy.get_user_dashboard_stats(db, user_id)
    except Exception as e:
        logger.error(f"Error getting user dashboard stats for user {user_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve user statistics"
        )


@router.get("/user/{user_id}/performance", response_model=UserPerformanceMetrics)
def get_user_performance_metrics(
    user_id: int,
    period_start: datetime = Query(..., description="Start of analysis period"),
    period_end: datetime = Query(..., description="End of analysis period"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get detailed user performance metrics for a specific period"""
    # Users can only access their own metrics unless they're moderator/admin
    if current_user.id != user_id and current_user.role not in ["moderator", "admin"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied"
        )
    
    try:
        return AnalyticsService.get_user_performance_metrics(db, user_id, period_start, period_end)
    except Exception as e:
        logger.error(f"Error getting user performance metrics for user {user_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve user performance metrics"
        )


@router.get("/trends/{metric_name}", response_model=TrendAnalysis)
def get_trend_analysis(
    metric_name: str,
    start_date: datetime = Query(..., description="Start date for trend analysis"),
    end_date: datetime = Query(..., description="End date for trend analysis"),
    granularity: TimePeriod = Query(TimePeriod.DAY, description="Time granularity"),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get trend analysis for a specific metric over time"""
    try:
        return AnalyticsService.get_trend_analysis(db, metric_name, start_date, end_date, granularity)
    except Exception as e:
        logger.error(f"Error getting trend analysis for {metric_name}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve trend analysis"
        )


@router.get("/learning", response_model=LearningAnalytics)
def get_learning_analytics(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get comprehensive language learning analytics"""
    try:
        return AnalyticsService.get_learning_analytics(db)
    except Exception as e:
        logger.error(f"Error getting learning analytics: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve learning analytics"
        )


@router.get("/platform/health", response_model=PlatformHealth)
def get_platform_health(
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Get overall platform health metrics"""
    try:
        from ..services.analytics_service_legacy import AnalyticsServiceLegacy
        return AnalyticsServiceLegacy.get_platform_health(db)
    except Exception as e:
        logger.error(f"Error getting platform health: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve platform health metrics"
        )


@router.get("/leaderboard", response_model=List[LeaderboardEntry])
def get_leaderboard(
    limit: int = Query(10, ge=1, le=100, description="Number of top users to return"),
    period_days: int = Query(30, ge=1, le=365, description="Period in days for ranking"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get user leaderboard based on contributions"""
    try:
        from ..services.analytics_service_legacy import AnalyticsServiceLegacy
        return AnalyticsServiceLegacy.get_leaderboard(db, limit, period_days)
    except Exception as e:
        logger.error(f"Error getting leaderboard: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve leaderboard"
        )


# Event and Activity Tracking Endpoints

@router.post("/events", response_model=AnalyticsEventResponse)
def create_analytics_event(
    event_data: AnalyticsEventCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Create a new analytics event"""
    try:
        # Set user_id from current user if not specified
        if not event_data.user_id:
            event_data.user_id = current_user.id
            
        event = AnalyticsService.create_analytics_event(db, event_data)
        return AnalyticsEventResponse.from_orm(event)
    except Exception as e:
        logger.error(f"Error creating analytics event: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create analytics event"
        )


@router.post("/activities", response_model=UserActivityResponse)
def create_user_activity(
    activity_data: UserActivityCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Create a new user activity record"""
    try:
        activity = AnalyticsService.create_user_activity(db, current_user.id, activity_data)
        return UserActivityResponse.from_orm(activity)
    except Exception as e:
        logger.error(f"Error creating user activity: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create user activity record"
        )


@router.get("/activities/my", response_model=List[UserActivityResponse])
def get_my_activities(
    limit: int = Query(50, ge=1, le=500, description="Number of activities to return"),
    offset: int = Query(0, ge=0, description="Number of activities to skip"),
    activity_type: Optional[str] = Query(None, description="Filter by activity type"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get current user's activities"""
    try:
        from ..models.analytics import UserActivity
        
        query = db.query(UserActivity).filter(UserActivity.user_id == current_user.id)
        
        if activity_type:
            query = query.filter(UserActivity.activity_type == activity_type)
            
        activities = query.order_by(UserActivity.created_at.desc()).offset(offset).limit(limit).all()
        
        return [UserActivityResponse.from_orm(activity) for activity in activities]
    except Exception as e:
        logger.error(f"Error getting user activities: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve user activities"
        )


# Admin-only endpoints

@router.post("/system/metrics", response_model=SystemMetricResponse)
def create_system_metric(
    metric_data: SystemMetricCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Create a new system metric record (admin only)"""
    try:
        from ..models.analytics import SystemMetrics
        
        metric = SystemMetrics(
            metric_name=metric_data.metric_name,
            metric_value=metric_data.metric_value,
            metric_unit=metric_data.metric_unit,
            category=metric_data.category,
            period_start=metric_data.period_start,
            period_end=metric_data.period_end,
            granularity=metric_data.granularity.value
        )
        
        db.add(metric)
        db.commit()
        db.refresh(metric)
        
        return SystemMetricResponse.from_orm(metric)
    except Exception as e:
        logger.error(f"Error creating system metric: {e}")
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create system metric"
        )


@router.put("/user/{user_id}/analytics/refresh")
def refresh_user_analytics(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Refresh analytics for a specific user (admin only)"""
    try:
        updated_analytics = AnalyticsService.update_user_analytics(db, user_id)
        return {"message": f"Analytics refreshed for user {user_id}", "updated_at": updated_analytics.updated_at}
    except Exception as e:
        logger.error(f"Error refreshing user analytics for user {user_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to refresh user analytics"
        )


@router.post("/cache/clear")
def clear_analytics_cache(
    cache_pattern: str = Query("analytics:*", description="Cache pattern to clear"),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Clear analytics cache (admin only)"""
    try:
        # Clear specific cache patterns
        if cache_pattern == "dashboard":
            cache.delete_pattern("dashboard_overview_*")
        elif cache_pattern == "trends":
            cache.delete_pattern("trend_analysis_*")
        elif cache_pattern == "all":
            cache.delete_pattern("analytics:*")
            cache.delete_pattern("dashboard_*")
            cache.delete_pattern("trend_*")
        else:
            cache.delete_pattern(cache_pattern)
            
        return {"message": f"Cache cleared for pattern: {cache_pattern}"}
    except Exception as e:
        logger.error(f"Error clearing analytics cache: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to clear analytics cache"
        )


# Data export endpoints

@router.get("/export/dashboard")
def export_dashboard_data(
    format: str = Query("json", description="Export format: json, csv"),
    period_days: int = Query(30, ge=1, le=365, description="Period in days"),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_moderator_or_admin)
):
    """Export dashboard data in various formats"""
    try:
        end_date = datetime.now(timezone.utc)
        start_date = end_date - timedelta(days=period_days)
        
        # Get comprehensive analytics data
        dashboard_data = AnalyticsService.get_dashboard_overview(db)
        user_metrics = AnalyticsService.get_user_performance_metrics(
            db, current_user.id, start_date, end_date
        )
        learning_analytics = AnalyticsService.get_learning_analytics(db)
        
        export_data = {
            "export_timestamp": datetime.now(timezone.utc).isoformat(),
            "period_start": start_date.isoformat(),
            "period_end": end_date.isoformat(),
            "dashboard_overview": dashboard_data.dict(),
            "learning_analytics": learning_analytics.dict(),
            "export_format": format
        }
        
        if format.lower() == "csv":
            # For CSV format, we'd need to flatten the data structure
            # This is a simplified version - full implementation would require proper CSV formatting
            return {"message": "CSV export not fully implemented", "data": export_data}
        else:
            return export_data
            
    except Exception as e:
        logger.error(f"Error exporting dashboard data: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to export dashboard data"
        )