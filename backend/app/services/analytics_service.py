"""Analytics Service - Business logic for analytics and dashboard functionality"""
import json
from datetime import datetime, timedelta, timezone
from typing import List, Optional, Dict, Any, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import func, and_, or_, desc, asc, text, distinct
from sqlalchemy.sql import extract

from ..models.analytics import (
    UserAnalytics, CategoryAnalytics, AnalyticsEvent, UserActivity,
    SystemMetrics, ContributionMetrics, UserEngagementMetrics,
    LanguageLearningMetrics, DailyMetricsSnapshot, AnalyticsEventType
)
from ..models.user import User
from ..models.contribution import Contribution, ContributionStatus
from ..models.category import Category
from ..models.morphology import Verb, VerbConjugation
from ..schemas.analytics import (
    AnalyticsEventCreate, UserActivityCreate, SystemMetricCreate,
    AnalyticsFilter, TimePeriod, DashboardOverview, RealTimeMetrics,
    TrendAnalysis, MetricsAggregation, UserPerformanceMetrics,
    CategoryPerformanceMetrics, QualityAnalytics, LearningAnalytics
)
from ..core.cache import cache
import logging

logger = logging.getLogger(__name__)


class AnalyticsService:
    """Service for analytics data processing and retrieval"""

    @staticmethod
    def create_analytics_event(
        db: Session, 
        event_data: AnalyticsEventCreate
    ) -> AnalyticsEvent:
        """Create a new analytics event"""
        try:
            event = AnalyticsEvent(
                event_type=event_data.event_type,
                user_id=event_data.user_id,
                contribution_id=event_data.contribution_id,
                category_id=event_data.category_id,
                event_metadata=json.dumps(event_data.event_metadata) if event_data.event_metadata else None
            )
            db.add(event)
            db.commit()
            db.refresh(event)
            return event
        except Exception as e:
            logger.error(f"Error creating analytics event: {e}")
            db.rollback()
            raise

    @staticmethod
    def create_user_activity(
        db: Session,
        user_id: int,
        activity_data: UserActivityCreate
    ) -> UserActivity:
        """Create a new user activity record"""
        try:
            activity = UserActivity(
                user_id=user_id,
                activity_type=activity_data.activity_type,
                resource_type=activity_data.resource_type,
                resource_id=activity_data.resource_id,
                session_id=activity_data.session_id,
                ip_address=activity_data.ip_address,
                user_agent=activity_data.user_agent,
                duration_seconds=activity_data.duration_seconds,
                activity_metadata=json.dumps(activity_data.activity_metadata) if activity_data.activity_metadata else None
            )
            db.add(activity)
            db.commit()
            db.refresh(activity)
            return activity
        except Exception as e:
            logger.error(f"Error creating user activity: {e}")
            db.rollback()
            raise

    @staticmethod
    def get_dashboard_overview(db: Session, cache_minutes: int = 5) -> DashboardOverview:
        """Get comprehensive dashboard overview with caching"""
        cache_key = f"dashboard_overview_{datetime.now().strftime('%Y%m%d_%H%M')}"
        
        # Try cache first
        cached_data = cache.get(cache_key)
        if cached_data:
            return DashboardOverview(**cached_data)

        try:
            # Current date calculations
            now = datetime.now(timezone.utc)
            today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
            week_start = today_start - timedelta(days=now.weekday())

            # Basic counts
            total_users = db.query(func.count(User.id)).scalar() or 0
            total_contributions = db.query(func.count(Contribution.id)).scalar() or 0

            # Today's active users
            active_users_today = db.query(func.count(distinct(UserActivity.user_id))).filter(
                UserActivity.created_at >= today_start
            ).scalar() or 0

            # This week's active users
            active_users_this_week = db.query(func.count(distinct(UserActivity.user_id))).filter(
                UserActivity.created_at >= week_start
            ).scalar() or 0

            # Today's contributions
            contributions_today = db.query(func.count(Contribution.id)).filter(
                Contribution.created_at >= today_start
            ).scalar() or 0

            # This week's contributions
            contributions_this_week = db.query(func.count(Contribution.id)).filter(
                Contribution.created_at >= week_start
            ).scalar() or 0

            # Approval rates
            today_approved = db.query(func.count(Contribution.id)).filter(
                and_(
                    Contribution.status == ContributionStatus.APPROVED,
                    Contribution.updated_at >= today_start
                )
            ).scalar() or 0

            week_approved = db.query(func.count(Contribution.id)).filter(
                and_(
                    Contribution.status == ContributionStatus.APPROVED,
                    Contribution.updated_at >= week_start
                )
            ).scalar() or 0

            approval_rate_today = (today_approved / contributions_today * 100) if contributions_today > 0 else 0
            approval_rate_this_week = (week_approved / contributions_this_week * 100) if contributions_this_week > 0 else 0

            # Average quality score
            avg_quality = db.query(func.avg(ContributionMetrics.quality_score)).join(
                Contribution
            ).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).scalar()

            # Top contributors today
            top_contributors_today = db.query(
                User.id,
                User.username,
                func.count(Contribution.id).label('contribution_count')
            ).join(Contribution).filter(
                Contribution.created_at >= today_start
            ).group_by(User.id, User.username).order_by(
                desc('contribution_count')
            ).limit(5).all()

            top_contributors_list = [
                {
                    "user_id": contrib.id,
                    "username": contrib.username,
                    "contributions": contrib.contribution_count
                }
                for contrib in top_contributors_today
            ]

            # Popular categories
            popular_categories = db.query(
                Category.id,
                Category.name,
                func.count(Contribution.id).label('contribution_count')
            ).join(Contribution).filter(
                Contribution.created_at >= week_start
            ).group_by(Category.id, Category.name).order_by(
                desc('contribution_count')
            ).limit(5).all()

            popular_categories_list = [
                {
                    "category_id": cat.id,
                    "name": cat.name,
                    "contributions": cat.contribution_count
                }
                for cat in popular_categories
            ]

            # System health metrics
            pending_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.PENDING
            ).scalar() or 0

            moderator_count = db.query(func.count(User.id)).filter(
                or_(User.role == "moderator", User.role == "admin")
            ).scalar() or 1

            system_health = {
                "pending_contributions": pending_contributions,
                "moderator_workload": pending_contributions / moderator_count,
                "system_status": "healthy" if pending_contributions < 100 else "busy"
            }

            # Real-time metrics
            real_time_metrics = AnalyticsService.get_real_time_metrics(db)

            dashboard_data = DashboardOverview(
                total_users=total_users,
                active_users_today=active_users_today,
                active_users_this_week=active_users_this_week,
                total_contributions=total_contributions,
                contributions_today=contributions_today,
                contributions_this_week=contributions_this_week,
                approval_rate_today=approval_rate_today,
                approval_rate_this_week=approval_rate_this_week,
                average_quality_score=avg_quality,
                top_contributors_today=top_contributors_list,
                popular_categories=popular_categories_list,
                system_health=system_health,
                real_time_metrics=real_time_metrics
            )

            # Cache for specified minutes
            cache.set(cache_key, dashboard_data.dict(), ttl=cache_minutes * 60)

            return dashboard_data

        except Exception as e:
            logger.error(f"Error generating dashboard overview: {e}")
            raise

    @staticmethod
    def get_real_time_metrics(db: Session) -> RealTimeMetrics:
        """Get real-time system metrics"""
        try:
            now = datetime.now(timezone.utc)
            hour_ago = now - timedelta(hours=1)

            # Active sessions in last hour
            active_sessions = db.query(func.count(distinct(UserActivity.session_id))).filter(
                and_(
                    UserActivity.created_at >= hour_ago,
                    UserActivity.session_id.isnot(None)
                )
            ).scalar() or 0

            # Current online users (activity in last 15 minutes)
            online_threshold = now - timedelta(minutes=15)
            current_online_users = db.query(func.count(distinct(UserActivity.user_id))).filter(
                UserActivity.created_at >= online_threshold
            ).scalar() or 0

            # Contributions last hour
            contributions_last_hour = db.query(func.count(Contribution.id)).filter(
                Contribution.created_at >= hour_ago
            ).scalar() or 0

            # Approvals last hour
            approvals_last_hour = db.query(func.count(Contribution.id)).filter(
                and_(
                    Contribution.status == ContributionStatus.APPROVED,
                    Contribution.updated_at >= hour_ago
                )
            ).scalar() or 0

            # Rejections last hour
            rejections_last_hour = db.query(func.count(Contribution.id)).filter(
                and_(
                    Contribution.status == ContributionStatus.REJECTED,
                    Contribution.updated_at >= hour_ago
                )
            ).scalar() or 0

            # System performance metrics (mock data for now)
            return RealTimeMetrics(
                current_online_users=current_online_users,
                active_sessions=active_sessions,
                contributions_last_hour=contributions_last_hour,
                approvals_last_hour=approvals_last_hour,
                rejections_last_hour=rejections_last_hour,
                average_response_time_ms=125.5,  # Would be calculated from actual performance data
                cache_hit_rate=0.85,  # Would be retrieved from cache service
                system_load={
                    "cpu": 0.45,
                    "memory": 0.67,
                    "disk": 0.23
                },
                last_updated=now
            )

        except Exception as e:
            logger.error(f"Error getting real-time metrics: {e}")
            raise

    @staticmethod
    def get_user_performance_metrics(
        db: Session,
        user_id: int,
        period_start: datetime,
        period_end: datetime
    ) -> UserPerformanceMetrics:
        """Get detailed user performance metrics"""
        try:
            # Basic contribution stats
            contributions = db.query(Contribution).filter(
                and_(
                    Contribution.created_by_id == user_id,
                    Contribution.created_at >= period_start,
                    Contribution.created_at <= period_end
                )
            ).all()

            contributions_count = len(contributions)
            approved_count = len([c for c in contributions if c.status == ContributionStatus.APPROVED])
            approval_rate = (approved_count / contributions_count * 100) if contributions_count > 0 else 0

            # Quality metrics
            quality_scores = db.query(ContributionMetrics.quality_score).join(
                Contribution
            ).filter(
                and_(
                    Contribution.created_by_id == user_id,
                    Contribution.created_at >= period_start,
                    Contribution.created_at <= period_end,
                    ContributionMetrics.quality_score.isnot(None)
                )
            ).all()

            avg_quality_score = sum(score[0] for score in quality_scores) / len(quality_scores) if quality_scores else None

            # Most active category
            category_stats = db.query(
                Category.name,
                func.count(Contribution.id).label('count')
            ).join(Contribution).filter(
                and_(
                    Contribution.created_by_id == user_id,
                    Contribution.created_at >= period_start,
                    Contribution.created_at <= period_end
                )
            ).group_by(Category.name).order_by(desc('count')).first()

            most_active_category = category_stats.name if category_stats else None

            # Learning progress (mock data structure)
            learning_progress = {
                "verbs_practiced": 15,
                "noun_classes_studied": 8,
                "vocabulary_items": 45,
                "completion_rate": 0.75
            }

            # Engagement score calculation
            activity_count = db.query(func.count(UserActivity.id)).filter(
                and_(
                    UserActivity.user_id == user_id,
                    UserActivity.created_at >= period_start,
                    UserActivity.created_at <= period_end
                )
            ).scalar() or 0

            engagement_score = min(100, (activity_count * 2.5 + contributions_count * 10))

            # Streak data
            streak_data = {
                "current_streak": 5,
                "longest_streak": 12,
                "last_activity": period_end
            }

            return UserPerformanceMetrics(
                user_id=user_id,
                period_start=period_start,
                period_end=period_end,
                contributions_count=contributions_count,
                approval_rate=approval_rate,
                average_quality_score=avg_quality_score,
                time_to_approval_avg_hours=None,  # Would require approval timestamp tracking
                most_active_category=most_active_category,
                learning_progress=learning_progress,
                engagement_score=engagement_score,
                streak_data=streak_data
            )

        except Exception as e:
            logger.error(f"Error getting user performance metrics for user {user_id}: {e}")
            raise

    @staticmethod
    def get_trend_analysis(
        db: Session,
        metric_name: str,
        start_date: datetime,
        end_date: datetime,
        granularity: TimePeriod = TimePeriod.DAY
    ) -> TrendAnalysis:
        """Generate trend analysis for a specific metric"""
        try:
            time_series = []
            
            # Generate date periods based on granularity
            current_date = start_date
            while current_date <= end_date:
                if granularity == TimePeriod.DAY:
                    period_end = current_date + timedelta(days=1)
                    period_label = current_date.strftime("%Y-%m-%d")
                elif granularity == TimePeriod.WEEK:
                    period_end = current_date + timedelta(weeks=1)
                    period_label = current_date.strftime("%Y-W%U")
                elif granularity == TimePeriod.MONTH:
                    period_end = current_date + timedelta(days=30)  # Approximate
                    period_label = current_date.strftime("%Y-%m")
                else:
                    period_end = current_date + timedelta(hours=1)
                    period_label = current_date.strftime("%Y-%m-%d %H:00")

                # Calculate metric value for this period
                if metric_name == "contributions":
                    value = db.query(func.count(Contribution.id)).filter(
                        and_(
                            Contribution.created_at >= current_date,
                            Contribution.created_at < period_end
                        )
                    ).scalar() or 0
                elif metric_name == "approvals":
                    value = db.query(func.count(Contribution.id)).filter(
                        and_(
                            Contribution.status == ContributionStatus.APPROVED,
                            Contribution.updated_at >= current_date,
                            Contribution.updated_at < period_end
                        )
                    ).scalar() or 0
                elif metric_name == "active_users":
                    value = db.query(func.count(distinct(UserActivity.user_id))).filter(
                        and_(
                            UserActivity.created_at >= current_date,
                            UserActivity.created_at < period_end
                        )
                    ).scalar() or 0
                else:
                    value = 0

                # Calculate percentage change from previous period
                percentage_change = None
                if len(time_series) > 0:
                    prev_value = time_series[-1].value
                    if prev_value > 0:
                        percentage_change = ((value - prev_value) / prev_value) * 100

                # Determine trend
                trend = "stable"
                if percentage_change is not None:
                    if percentage_change > 5:
                        trend = "up"
                    elif percentage_change < -5:
                        trend = "down"

                time_series.append(MetricsAggregation(
                    period=period_label,
                    value=float(value),
                    percentage_change=percentage_change,
                    trend=trend
                ))

                # Move to next period
                if granularity == TimePeriod.DAY:
                    current_date += timedelta(days=1)
                elif granularity == TimePeriod.WEEK:
                    current_date += timedelta(weeks=1)
                elif granularity == TimePeriod.MONTH:
                    current_date += timedelta(days=30)
                else:
                    current_date += timedelta(hours=1)

            # Calculate overall trend
            if len(time_series) >= 2:
                first_value = time_series[0].value
                last_value = time_series[-1].value
                if last_value > first_value * 1.1:
                    overall_trend = "growing"
                elif last_value < first_value * 0.9:
                    overall_trend = "declining"
                else:
                    overall_trend = "stable"
            else:
                overall_trend = "insufficient_data"

            # Calculate growth rate
            growth_rate = None
            if len(time_series) >= 2:
                first_value = time_series[0].value
                last_value = time_series[-1].value
                if first_value > 0:
                    growth_rate = ((last_value - first_value) / first_value) * 100

            return TrendAnalysis(
                metric_name=metric_name,
                time_series=time_series,
                overall_trend=overall_trend,
                growth_rate=growth_rate,
                seasonality_detected=False  # Would require more sophisticated analysis
            )

        except Exception as e:
            logger.error(f"Error generating trend analysis for {metric_name}: {e}")
            raise

    @staticmethod
    def update_user_analytics(db: Session, user_id: int) -> UserAnalytics:
        """Update analytics for a specific user"""
        try:
            # Get or create user analytics record
            user_analytics = db.query(UserAnalytics).filter(
                UserAnalytics.user_id == user_id
            ).first()

            if not user_analytics:
                user_analytics = UserAnalytics(user_id=user_id)
                db.add(user_analytics)

            # Calculate current stats
            contributions = db.query(Contribution).filter(
                Contribution.created_by_id == user_id
            ).all()

            user_analytics.total_contributions = len(contributions)
            user_analytics.approved_contributions = len([
                c for c in contributions if c.status == ContributionStatus.APPROVED
            ])
            user_analytics.rejected_contributions = len([
                c for c in contributions if c.status == ContributionStatus.REJECTED
            ])

            # Calculate approval rate
            if user_analytics.total_contributions > 0:
                user_analytics.average_approval_rate = (
                    user_analytics.approved_contributions / user_analytics.total_contributions
                ) * 100

            # Update timestamps
            if contributions:
                user_analytics.last_contribution_date = max(c.created_at for c in contributions)

            user_analytics.updated_at = datetime.utcnow()

            db.commit()
            db.refresh(user_analytics)
            return user_analytics

        except Exception as e:
            logger.error(f"Error updating user analytics for user {user_id}: {e}")
            db.rollback()
            raise

    @staticmethod
    def get_learning_analytics(db: Session) -> LearningAnalytics:
        """Get comprehensive learning analytics"""
        try:
            # Count total learners (users with learning activity)
            total_learners = db.query(func.count(distinct(UserActivity.user_id))).filter(
                UserActivity.activity_type.in_(['verb_practiced', 'morphology_studied', 'pronunciation_attempted'])
            ).scalar() or 0

            # Active learners today
            today_start = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
            active_learners_today = db.query(func.count(distinct(UserActivity.user_id))).filter(
                and_(
                    UserActivity.activity_type.in_(['verb_practiced', 'morphology_studied', 'pronunciation_attempted']),
                    UserActivity.created_at >= today_start
                )
            ).scalar() or 0

            # Completion rates by difficulty (mock data)
            completion_rates = {
                "beginner": 0.85,
                "intermediate": 0.65,
                "advanced": 0.45
            }

            # Popular learning areas
            popular_learning_areas = [
                {"area": "Verb Conjugation", "users": 120, "completion_rate": 0.75},
                {"area": "Noun Classes", "users": 95, "completion_rate": 0.68},
                {"area": "Pronunciation", "users": 80, "completion_rate": 0.60},
                {"area": "Vocabulary", "users": 150, "completion_rate": 0.80}
            ]

            # Average study time
            study_activities = db.query(UserActivity.duration_seconds).filter(
                and_(
                    UserActivity.activity_type.in_(['verb_practiced', 'morphology_studied']),
                    UserActivity.duration_seconds.isnot(None)
                )
            ).all()

            total_seconds = sum(activity.duration_seconds for activity in study_activities)
            average_study_time_minutes = (total_seconds / len(study_activities) / 60) if study_activities else 0

            # Verb mastery rates (mock data)
            verb_mastery_rates = {
                "present_tense": 0.78,
                "past_tense": 0.65,
                "future_tense": 0.55,
                "conditional": 0.42
            }

            # Learning path effectiveness (mock data)
            learning_path_effectiveness = {
                "structured_lessons": 0.82,
                "practice_exercises": 0.75,
                "interactive_games": 0.68,
                "peer_learning": 0.71
            }

            return LearningAnalytics(
                total_learners=total_learners,
                active_learners_today=active_learners_today,
                completion_rates=completion_rates,
                popular_learning_areas=popular_learning_areas,
                average_study_time_minutes=average_study_time_minutes,
                pronunciation_accuracy=0.72,  # Mock data
                verb_mastery_rates=verb_mastery_rates,
                learning_path_effectiveness=learning_path_effectiveness
            )

        except Exception as e:
            logger.error(f"Error getting learning analytics: {e}")
            raise