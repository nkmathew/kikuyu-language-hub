from datetime import datetime
from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, Float, Enum as SQLEnum, Boolean
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from ..db.base import Base


class AnalyticsEventType(str, Enum):
    CONTRIBUTION_CREATED = "contribution_created"
    CONTRIBUTION_APPROVED = "contribution_approved"
    CONTRIBUTION_REJECTED = "contribution_rejected"
    SUB_TRANSLATION_CREATED = "sub_translation_created"
    EXPORT_REQUESTED = "export_requested"
    CATEGORY_ASSIGNED = "category_assigned"
    USER_LOGIN = "user_login"
    USER_LOGOUT = "user_logout"
    USER_REGISTERED = "user_registered"
    VERB_PRACTICED = "verb_practiced"
    MORPHOLOGY_STUDIED = "morphology_studied"
    PRONUNCIATION_ATTEMPTED = "pronunciation_attempted"
    DIFFICULTY_COMPLETED = "difficulty_completed"
    SEARCH_PERFORMED = "search_performed"
    CONTENT_RATED = "content_rated"
    API_CALLED = "api_called"


class UserAnalytics(Base):
    __tablename__ = "user_analytics"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    total_contributions = Column(Integer, default=0, nullable=False)
    approved_contributions = Column(Integer, default=0, nullable=False)
    rejected_contributions = Column(Integer, default=0, nullable=False)
    total_sub_translations = Column(Integer, default=0, nullable=False)
    total_words_contributed = Column(Integer, default=0, nullable=False)
    average_approval_rate = Column(Float, default=0.0, nullable=False)
    streak_days = Column(Integer, default=0, nullable=False)
    last_contribution_date = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    user = relationship("User", back_populates="analytics")
    
    def __repr__(self):
        return f"<UserAnalytics(user_id={self.user_id}, contributions={self.total_contributions})>"


class CategoryAnalytics(Base):
    __tablename__ = "category_analytics"

    id = Column(Integer, primary_key=True, index=True)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=False)
    total_contributions = Column(Integer, default=0, nullable=False)
    approved_contributions = Column(Integer, default=0, nullable=False)
    total_sub_translations = Column(Integer, default=0, nullable=False)
    unique_contributors = Column(Integer, default=0, nullable=False)
    average_quality_score = Column(Float, default=0.0, nullable=False)
    last_contribution_date = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    category = relationship("Category")
    
    def __repr__(self):
        return f"<CategoryAnalytics(category_id={self.category_id}, contributions={self.total_contributions})>"


class AnalyticsEvent(Base):
    __tablename__ = "analytics_events"

    id = Column(Integer, primary_key=True, index=True)
    event_type = Column(SQLEnum(AnalyticsEventType), nullable=False, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=True)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=True)
    event_metadata = Column(Text)  # JSON metadata for additional event data
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    # Relationships
    user = relationship("User")
    contribution = relationship("Contribution")
    category = relationship("Category")
    
    def __repr__(self):
        return f"<AnalyticsEvent(type={self.event_type}, user_id={self.user_id}, timestamp={self.timestamp})>"


class UserActivity(Base):
    """Track detailed user activity for analytics"""
    __tablename__ = "user_activities"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    activity_type = Column(String(50), nullable=False, index=True)  # login, logout, contribution, review, export
    resource_type = Column(String(50), nullable=True, index=True)  # contribution, verb, morphology
    resource_id = Column(Integer, nullable=True, index=True)
    session_id = Column(String(255), nullable=True, index=True)
    ip_address = Column(String(45), nullable=True)
    user_agent = Column(Text, nullable=True)
    duration_seconds = Column(Integer, nullable=True)  # For activities with duration
    activity_metadata = Column(Text, nullable=True)  # JSON string for additional data
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False, index=True)
    
    # Relationships
    user = relationship("User", back_populates="activities")


class SystemMetrics(Base):
    """Store system-wide metrics snapshots"""
    __tablename__ = "system_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    metric_name = Column(String(100), nullable=False, index=True)
    metric_value = Column(Float, nullable=False)
    metric_unit = Column(String(20), nullable=True)  # count, percentage, seconds, etc.
    category = Column(String(50), nullable=False, index=True)  # users, contributions, performance, quality
    period_start = Column(DateTime(timezone=True), nullable=False, index=True)
    period_end = Column(DateTime(timezone=True), nullable=False, index=True)
    granularity = Column(String(20), nullable=False, index=True)  # hour, day, week, month
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


class ContributionMetrics(Base):
    """Detailed metrics for individual contributions"""
    __tablename__ = "contribution_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    contribution_id = Column(Integer, ForeignKey("contributions.id"), nullable=False, unique=True, index=True)
    
    # Quality metrics
    quality_score = Column(Float, nullable=True)
    complexity_score = Column(Float, nullable=True)
    similarity_score = Column(Float, nullable=True)  # To existing translations
    
    # Engagement metrics
    view_count = Column(Integer, default=0, nullable=False)
    review_count = Column(Integer, default=0, nullable=False)
    approval_time_minutes = Column(Integer, nullable=True)
    
    # Processing metrics
    nlp_processing_time_ms = Column(Integer, nullable=True)
    qa_issues_count = Column(Integer, default=0, nullable=False)
    qa_warnings_count = Column(Integer, default=0, nullable=False)
    
    # Content metrics
    source_word_count = Column(Integer, nullable=True)
    target_word_count = Column(Integer, nullable=True)
    character_count = Column(Integer, nullable=True)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    contribution = relationship("Contribution", back_populates="metrics")


class UserEngagementMetrics(Base):
    """Track user engagement patterns"""
    __tablename__ = "user_engagement_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    
    # Time-based metrics (for specific period)
    period_start = Column(DateTime(timezone=True), nullable=False, index=True)
    period_end = Column(DateTime(timezone=True), nullable=False, index=True)
    
    # Activity metrics
    login_count = Column(Integer, default=0, nullable=False)
    session_duration_minutes = Column(Integer, default=0, nullable=False)
    contributions_submitted = Column(Integer, default=0, nullable=False)
    contributions_approved = Column(Integer, default=0, nullable=False)
    contributions_rejected = Column(Integer, default=0, nullable=False)
    reviews_performed = Column(Integer, default=0, nullable=False)
    
    # Quality metrics
    average_quality_score = Column(Float, nullable=True)
    approval_rate = Column(Float, nullable=True)  # Percentage
    
    # Streak metrics
    consecutive_days_active = Column(Integer, default=0, nullable=False)
    longest_streak_days = Column(Integer, default=0, nullable=False)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    user = relationship("User", back_populates="engagement_metrics")


class LanguageLearningMetrics(Base):
    """Track language learning progress and patterns"""
    __tablename__ = "language_learning_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    
    # Learning focus areas
    verb_forms_practiced = Column(Integer, default=0, nullable=False)
    noun_classes_practiced = Column(Integer, default=0, nullable=False)
    vocabulary_items_learned = Column(Integer, default=0, nullable=False)
    pronunciation_attempts = Column(Integer, default=0, nullable=False)
    
    # Difficulty progression
    beginner_completions = Column(Integer, default=0, nullable=False)
    intermediate_completions = Column(Integer, default=0, nullable=False)
    advanced_completions = Column(Integer, default=0, nullable=False)
    
    # Performance metrics
    average_accuracy = Column(Float, nullable=True)
    improvement_rate = Column(Float, nullable=True)  # Weekly improvement percentage
    
    # Preferred learning areas
    favorite_category = Column(String(50), nullable=True)
    weakest_area = Column(String(50), nullable=True)
    
    # Time tracking
    total_study_time_minutes = Column(Integer, default=0, nullable=False)
    last_study_session = Column(DateTime(timezone=True), nullable=True)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    user = relationship("User", back_populates="learning_metrics")


class DailyMetricsSnapshot(Base):
    """Daily aggregated metrics for performance tracking"""
    __tablename__ = "daily_metrics_snapshots"
    
    id = Column(Integer, primary_key=True, index=True)
    date = Column(DateTime(timezone=True), nullable=False, unique=True, index=True)
    
    # User metrics
    total_users = Column(Integer, default=0, nullable=False)
    active_users_today = Column(Integer, default=0, nullable=False)
    new_registrations = Column(Integer, default=0, nullable=False)
    
    # Contribution metrics
    total_contributions = Column(Integer, default=0, nullable=False)
    contributions_today = Column(Integer, default=0, nullable=False)
    contributions_approved_today = Column(Integer, default=0, nullable=False)
    contributions_rejected_today = Column(Integer, default=0, nullable=False)
    
    # Quality metrics
    average_quality_score = Column(Float, nullable=True)
    approval_rate_today = Column(Float, nullable=True)
    
    # Performance metrics
    average_response_time_ms = Column(Float, nullable=True)
    api_calls_today = Column(Integer, default=0, nullable=False)
    export_requests_today = Column(Integer, default=0, nullable=False)
    
    # Content metrics
    total_vocabulary_items = Column(Integer, default=0, nullable=False)
    total_verb_forms = Column(Integer, default=0, nullable=False)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)