"""Legacy Analytics Service - For backward compatibility with existing endpoints"""
from datetime import datetime, timedelta, timezone
from typing import List, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import func, and_, desc, distinct

from ..models.user import User, UserRole
from ..models.contribution import Contribution, ContributionStatus
from ..models.category import Category
from ..models.analytics import UserAnalytics, CategoryAnalytics
from ..schemas.analytics import (
    DashboardStats, UserDashboardStats, PlatformHealth, LeaderboardEntry,
    CategoryContributionStats, ExportStats
)
import logging

logger = logging.getLogger(__name__)


class AnalyticsServiceLegacy:
    """Legacy analytics service for existing endpoints"""

    @staticmethod
    def get_dashboard_stats(db: Session) -> DashboardStats:
        """Get basic dashboard statistics"""
        try:
            # Basic counts
            total_users = db.query(func.count(User.id)).scalar() or 0
            total_contributions = db.query(func.count(Contribution.id)).scalar() or 0
            approved_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).scalar() or 0
            pending_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.PENDING
            ).scalar() or 0
            rejected_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.REJECTED
            ).scalar() or 0

            # Sub-translations count (if the model exists)
            try:
                from ..models.sub_translation import SubTranslation
                total_sub_translations = db.query(func.count(SubTranslation.id)).scalar() or 0
            except ImportError:
                total_sub_translations = 0

            # Categories count
            total_categories = db.query(func.count(Category.id)).scalar() or 0

            # Active contributors this month
            month_start = datetime.now(timezone.utc).replace(day=1, hour=0, minute=0, second=0, microsecond=0)
            active_contributors_this_month = db.query(func.count(distinct(Contribution.created_by_id))).filter(
                Contribution.created_at >= month_start
            ).scalar() or 0

            # Approval rate
            approval_rate = (approved_contributions / total_contributions * 100) if total_contributions > 0 else 0

            return DashboardStats(
                total_users=total_users,
                total_contributions=total_contributions,
                approved_contributions=approved_contributions,
                pending_contributions=pending_contributions,
                rejected_contributions=rejected_contributions,
                total_sub_translations=total_sub_translations,
                total_categories=total_categories,
                active_contributors_this_month=active_contributors_this_month,
                approval_rate=approval_rate
            )

        except Exception as e:
            logger.error(f"Error getting dashboard stats: {e}")
            raise

    @staticmethod
    def get_user_dashboard_stats(db: Session, user_id: int) -> UserDashboardStats:
        """Get user-specific dashboard statistics"""
        try:
            # User's contributions
            user_contributions = db.query(Contribution).filter(
                Contribution.created_by_id == user_id
            ).all()

            total_contributions = len(user_contributions)
            approved_contributions = len([c for c in user_contributions if c.status == ContributionStatus.APPROVED])
            pending_contributions = len([c for c in user_contributions if c.status == ContributionStatus.PENDING])
            rejected_contributions = len([c for c in user_contributions if c.status == ContributionStatus.REJECTED])

            # Sub-translations count
            try:
                from ..models.sub_translation import SubTranslation
                total_sub_translations = db.query(func.count(SubTranslation.id)).join(
                    Contribution
                ).filter(Contribution.created_by_id == user_id).scalar() or 0
            except ImportError:
                total_sub_translations = 0

            # Approval rate
            approval_rate = (approved_contributions / total_contributions * 100) if total_contributions > 0 else 0

            # Streak days (mock for now)
            streak_days = 5

            # User rank (simplified calculation)
            user_rank_query = db.query(
                Contribution.created_by_id,
                func.count(Contribution.id).label('contribution_count')
            ).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).group_by(Contribution.created_by_id).order_by(desc('contribution_count')).all()

            user_rank = None
            for i, (uid, count) in enumerate(user_rank_query, 1):
                if uid == user_id:
                    user_rank = i
                    break

            # Contribution trend (last 7 days)
            trend_data = []
            for i in range(7):
                date = datetime.now(timezone.utc) - timedelta(days=i)
                day_start = date.replace(hour=0, minute=0, second=0, microsecond=0)
                day_end = day_start + timedelta(days=1)
                
                day_contributions = db.query(func.count(Contribution.id)).filter(
                    and_(
                        Contribution.created_by_id == user_id,
                        Contribution.created_at >= day_start,
                        Contribution.created_at < day_end
                    )
                ).scalar() or 0

                trend_data.append({
                    "date": day_start.isoformat(),
                    "contributions": day_contributions
                })

            return UserDashboardStats(
                user_id=user_id,
                total_contributions=total_contributions,
                approved_contributions=approved_contributions,
                pending_contributions=pending_contributions,
                rejected_contributions=rejected_contributions,
                total_sub_translations=total_sub_translations,
                approval_rate=approval_rate,
                streak_days=streak_days,
                rank=user_rank,
                contribution_trend=trend_data
            )

        except Exception as e:
            logger.error(f"Error getting user dashboard stats for user {user_id}: {e}")
            raise

    @staticmethod
    def get_platform_health(db: Session) -> PlatformHealth:
        """Get overall platform health metrics"""
        try:
            now = datetime.now(timezone.utc)
            
            # Active users calculations
            daily_threshold = now - timedelta(days=1)
            weekly_threshold = now - timedelta(days=7)
            monthly_threshold = now - timedelta(days=30)

            # For now, we'll use contribution activity as a proxy for user activity
            daily_active_users = db.query(func.count(distinct(Contribution.created_by_id))).filter(
                Contribution.created_at >= daily_threshold
            ).scalar() or 0

            weekly_active_users = db.query(func.count(distinct(Contribution.created_by_id))).filter(
                Contribution.created_at >= weekly_threshold
            ).scalar() or 0

            monthly_active_users = db.query(func.count(distinct(Contribution.created_by_id))).filter(
                Contribution.created_at >= monthly_threshold
            ).scalar() or 0

            # Average time to approval (mock data for now)
            average_time_to_approval = 24.5  # hours

            # Moderator workload
            pending_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.PENDING
            ).scalar() or 0

            moderator_count = db.query(func.count(User.id)).filter(
                User.role.in_([UserRole.MODERATOR, UserRole.ADMIN])
            ).scalar() or 1

            moderator_workload = pending_contributions / moderator_count

            # Quality score (average of approved contributions)
            avg_quality = db.query(func.avg(Contribution.quality_score)).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).scalar() or 0.0

            # Growth rate (contributions this month vs last month)
            this_month_start = now.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
            last_month_start = (this_month_start - timedelta(days=1)).replace(day=1)

            this_month_contributions = db.query(func.count(Contribution.id)).filter(
                Contribution.created_at >= this_month_start
            ).scalar() or 0

            last_month_contributions = db.query(func.count(Contribution.id)).filter(
                and_(
                    Contribution.created_at >= last_month_start,
                    Contribution.created_at < this_month_start
                )
            ).scalar() or 0

            growth_rate = ((this_month_contributions - last_month_contributions) / 
                          last_month_contributions * 100) if last_month_contributions > 0 else 0

            return PlatformHealth(
                daily_active_users=daily_active_users,
                weekly_active_users=weekly_active_users,
                monthly_active_users=monthly_active_users,
                average_time_to_approval=average_time_to_approval,
                moderator_workload=moderator_workload,
                quality_score=avg_quality,
                growth_rate=growth_rate
            )

        except Exception as e:
            logger.error(f"Error getting platform health: {e}")
            raise

    @staticmethod
    def get_leaderboard(db: Session, limit: int = 10, period_days: int = 30) -> List[LeaderboardEntry]:
        """Get user leaderboard based on contributions"""
        try:
            cutoff_date = datetime.now(timezone.utc) - timedelta(days=period_days)

            # Get top contributors with their stats
            leaderboard_query = db.query(
                User.id,
                User.display_name,
                User.email,
                func.count(Contribution.id).label('total_contributions'),
                func.count(
                    func.case((Contribution.status == ContributionStatus.APPROVED, 1))
                ).label('approved_contributions')
            ).join(Contribution).filter(
                Contribution.created_at >= cutoff_date
            ).group_by(User.id, User.display_name, User.email).order_by(
                desc('approved_contributions')
            ).limit(limit).all()

            leaderboard = []
            for rank, entry in enumerate(leaderboard_query, 1):
                # Calculate approval rate
                approval_rate = (entry.approved_contributions / entry.total_contributions * 100) if entry.total_contributions > 0 else 0

                # Get user analytics for streak data
                user_analytics = db.query(UserAnalytics).filter(
                    UserAnalytics.user_id == entry.id
                ).first()

                streak_days = user_analytics.streak_days if user_analytics else 0

                leaderboard.append(LeaderboardEntry(
                    user_id=entry.id,
                    username=entry.display_name or entry.email.split('@')[0],
                    total_contributions=entry.total_contributions,
                    approved_contributions=entry.approved_contributions,
                    approval_rate=approval_rate,
                    streak_days=streak_days,
                    rank=rank
                ))

            return leaderboard

        except Exception as e:
            logger.error(f"Error getting leaderboard: {e}")
            raise

    @staticmethod
    def get_category_contribution_stats(db: Session) -> List[CategoryContributionStats]:
        """Get contribution statistics by category"""
        try:
            # Query contributions by category
            from ..models.contribution import contribution_categories
            
            category_stats = db.query(
                Category.id,
                Category.name,
                func.count(Contribution.id).label('total_contributions'),
                func.count(
                    func.case((Contribution.status == ContributionStatus.APPROVED, 1))
                ).label('approved_contributions'),
                func.count(
                    func.case((Contribution.status == ContributionStatus.PENDING, 1))
                ).label('pending_contributions'),
                func.count(distinct(Contribution.created_by_id)).label('unique_contributors')
            ).join(
                contribution_categories, Category.id == contribution_categories.c.category_id
            ).join(
                Contribution, Contribution.id == contribution_categories.c.contribution_id
            ).group_by(Category.id, Category.name).all()

            return [
                CategoryContributionStats(
                    category_id=stat.id,
                    category_name=stat.name,
                    total_contributions=stat.total_contributions,
                    approved_contributions=stat.approved_contributions,
                    pending_contributions=stat.pending_contributions,
                    unique_contributors=stat.unique_contributors
                )
                for stat in category_stats
            ]

        except Exception as e:
            logger.error(f"Error getting category contribution stats: {e}")
            raise

    @staticmethod
    def get_export_stats(db: Session) -> ExportStats:
        """Get statistics for exports"""
        try:
            # Total approved translations
            total_approved = db.query(func.count(Contribution.id)).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).scalar() or 0

            # Translations by category
            from ..models.contribution import contribution_categories
            
            category_counts = db.query(
                Category.name,
                func.count(Contribution.id).label('count')
            ).join(
                contribution_categories, Category.id == contribution_categories.c.category_id
            ).join(
                Contribution, Contribution.id == contribution_categories.c.contribution_id
            ).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).group_by(Category.name).all()

            translations_by_category = {cat.name: cat.count for cat in category_counts}

            # Translations by difficulty
            difficulty_counts = db.query(
                Contribution.difficulty_level,
                func.count(Contribution.id).label('count')
            ).filter(
                Contribution.status == ContributionStatus.APPROVED
            ).group_by(Contribution.difficulty_level).all()

            translations_by_difficulty = {diff.difficulty_level.value if diff.difficulty_level else 'unknown': diff.count 
                                        for diff in difficulty_counts}

            # Mock data for export format usage
            export_format_usage = {
                "json": 150,
                "csv": 75,
                "txt": 25
            }

            return ExportStats(
                total_approved_translations=total_approved,
                translations_by_category=translations_by_category,
                translations_by_difficulty=translations_by_difficulty,
                last_export_date=None,  # Would track actual export events
                export_format_usage=export_format_usage
            )

        except Exception as e:
            logger.error(f"Error getting export stats: {e}")
            raise