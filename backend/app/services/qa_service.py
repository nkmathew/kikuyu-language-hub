"""
Quality Assurance service for automated checks and bulk moderation
"""
from typing import List, Dict, Optional, Tuple, Any
from sqlalchemy.orm import Session
from sqlalchemy import func, and_, or_, desc, text
from datetime import datetime, timedelta
from dataclasses import dataclass
from enum import Enum
import re
import json

from ..models.contribution import Contribution, ContributionStatus, DifficultyLevel
from ..models.user import User, UserRole
from ..models.audit_log import AuditLog
from ..models.category import Category
from ..models.sub_translation import SubTranslation
from ..services.nlp_service import NLPService
from ..utils.nlp import spell_checker, difficulty_analyzer
from ..core.cache import cached, CacheConfig, invalidate_cache_on_change


class QualityIssueType(Enum):
    SPELLING_ERROR = "spelling_error"
    LENGTH_MISMATCH = "length_mismatch"
    DUPLICATE_CONTENT = "duplicate_content"
    INAPPROPRIATE_CONTENT = "inappropriate_content"
    FORMATTING_ERROR = "formatting_error"
    DIFFICULTY_MISMATCH = "difficulty_mismatch"
    CATEGORY_MISMATCH = "category_mismatch"
    TRANSLATION_ACCURACY = "translation_accuracy"
    MISSING_CONTEXT = "missing_context"
    LOW_QUALITY_SCORE = "low_quality_score"


@dataclass
class QualityIssue:
    """Represents a quality issue found in a contribution"""
    issue_type: QualityIssueType
    severity: str  # high, medium, low
    message: str
    suggestion: Optional[str] = None
    confidence: float = 1.0
    auto_fixable: bool = False


@dataclass
class QualityReport:
    """Quality assessment report for a contribution"""
    contribution_id: int
    overall_score: float
    issues: List[QualityIssue]
    recommendations: List[str]
    auto_approve_eligible: bool
    requires_review: bool


class QualityAssuranceService:
    """
    Service for automated quality checks and moderation assistance
    """
    
    # Quality thresholds
    AUTO_APPROVE_THRESHOLD = 0.85
    REQUIRES_REVIEW_THRESHOLD = 0.6
    
    # Content filters
    INAPPROPRIATE_PATTERNS = [
        r'\b(spam|test|testing|asdf|qwerty)\b',
        r'^.{1,2}$',  # Too short
        r'^(.)\1{4,}',  # Repeated characters
    ]
    
    @staticmethod
    def analyze_contribution_quality(
        db: Session,
        contribution_id: int,
        detailed: bool = True
    ) -> QualityReport:
        """Perform comprehensive quality analysis on a contribution"""
        contribution = db.query(Contribution).filter(
            Contribution.id == contribution_id
        ).first()
        
        if not contribution:
            raise ValueError(f"Contribution {contribution_id} not found")
        
        issues = []
        overall_score = 1.0
        
        # Run all quality checks
        issues.extend(QualityAssuranceService._check_spelling(contribution))
        issues.extend(QualityAssuranceService._check_length_balance(contribution))
        issues.extend(QualityAssuranceService._check_duplicates(db, contribution))
        issues.extend(QualityAssuranceService._check_inappropriate_content(contribution))
        issues.extend(QualityAssuranceService._check_formatting(contribution))
        issues.extend(QualityAssuranceService._check_difficulty_consistency(contribution))
        issues.extend(QualityAssuranceService._check_category_relevance(contribution))
        issues.extend(QualityAssuranceService._check_translation_completeness(contribution))
        
        # Calculate overall score based on issues
        for issue in issues:
            if issue.severity == 'high':
                overall_score -= 0.3
            elif issue.severity == 'medium':
                overall_score -= 0.15
            elif issue.severity == 'low':
                overall_score -= 0.05
        
        overall_score = max(0.0, overall_score)
        
        # Generate recommendations
        recommendations = QualityAssuranceService._generate_recommendations(issues)
        
        # Determine approval eligibility
        auto_approve_eligible = (
            overall_score >= QualityAssuranceService.AUTO_APPROVE_THRESHOLD and
            not any(issue.severity == 'high' for issue in issues)
        )
        
        requires_review = overall_score < QualityAssuranceService.REQUIRES_REVIEW_THRESHOLD
        
        return QualityReport(
            contribution_id=contribution_id,
            overall_score=overall_score,
            issues=issues,
            recommendations=recommendations,
            auto_approve_eligible=auto_approve_eligible,
            requires_review=requires_review
        )
    
    @staticmethod
    def _check_spelling(contribution: Contribution) -> List[QualityIssue]:
        """Check for spelling errors in source text"""
        issues = []
        
        try:
            errors = spell_checker.check_text(contribution.source_text)
            
            if errors:
                severity = 'high' if len(errors) > 3 else 'medium' if len(errors) > 1 else 'low'
                
                issues.append(QualityIssue(
                    issue_type=QualityIssueType.SPELLING_ERROR,
                    severity=severity,
                    message=f"Found {len(errors)} potential spelling errors",
                    suggestion="Review and correct spelling errors",
                    confidence=max(error['confidence'] for error in errors) if errors else 0.0,
                    auto_fixable=any(error['confidence'] > 0.9 for error in errors)
                ))
        except Exception:
            pass  # Skip if spell checker fails
        
        return issues
    
    @staticmethod
    def _check_length_balance(contribution: Contribution) -> List[QualityIssue]:
        """Check for appropriate length balance between source and target"""
        issues = []
        
        source_len = len(contribution.source_text)
        target_len = len(contribution.target_text)
        
        if source_len == 0 or target_len == 0:
            issues.append(QualityIssue(
                issue_type=QualityIssueType.LENGTH_MISMATCH,
                severity='high',
                message="Source or target text is empty",
                suggestion="Ensure both source and target texts are provided"
            ))
            return issues
        
        ratio = source_len / target_len
        
        if ratio > 4 or ratio < 0.25:
            severity = 'high' if ratio > 6 or ratio < 0.15 else 'medium'
            
            issues.append(QualityIssue(
                issue_type=QualityIssueType.LENGTH_MISMATCH,
                severity=severity,
                message=f"Unusual length ratio: {ratio:.1f}",
                suggestion="Review translation for completeness and accuracy"
            ))
        
        return issues
    
    @staticmethod
    def _check_duplicates(db: Session, contribution: Contribution) -> List[QualityIssue]:
        """Check for duplicate or very similar content"""
        issues = []
        
        # Check for exact duplicates
        exact_duplicate = db.query(Contribution).filter(
            and_(
                Contribution.id != contribution.id,
                or_(
                    Contribution.source_text == contribution.source_text,
                    Contribution.target_text == contribution.target_text
                )
            )
        ).first()
        
        if exact_duplicate:
            issues.append(QualityIssue(
                issue_type=QualityIssueType.DUPLICATE_CONTENT,
                severity='high',
                message="Exact duplicate content found",
                suggestion=f"Consider if this differs from contribution #{exact_duplicate.id}",
                confidence=1.0
            ))
        
        # Check for similar content using NLP
        try:
            similar = NLPService.find_similar_translations(
                contribution.source_text, threshold=0.9, limit=3
            )
            
            if similar:
                high_similarity = [s for s in similar if s['similarity_score'] > 0.95]
                if high_similarity:
                    issues.append(QualityIssue(
                        issue_type=QualityIssueType.DUPLICATE_CONTENT,
                        severity='medium',
                        message=f"Found {len(high_similarity)} very similar translations",
                        suggestion="Review for uniqueness",
                        confidence=max(s['similarity_score'] for s in high_similarity)
                    ))
        except Exception:
            pass  # Skip if NLP service fails
        
        return issues
    
    @staticmethod
    def _check_inappropriate_content(contribution: Contribution) -> List[QualityIssue]:
        """Check for inappropriate or low-quality content"""
        issues = []
        
        combined_text = f"{contribution.source_text} {contribution.target_text}".lower()
        
        for pattern in QualityAssuranceService.INAPPROPRIATE_PATTERNS:
            if re.search(pattern, combined_text, re.IGNORECASE):
                issues.append(QualityIssue(
                    issue_type=QualityIssueType.INAPPROPRIATE_CONTENT,
                    severity='high',
                    message="Content appears to be inappropriate or low quality",
                    suggestion="Review content for appropriateness and completeness"
                ))
                break
        
        return issues
    
    @staticmethod
    def _check_formatting(contribution: Contribution) -> List[QualityIssue]:
        """Check for formatting issues"""
        issues = []
        
        # Check for excessive whitespace
        if re.search(r'\s{3,}', contribution.source_text) or re.search(r'\s{3,}', contribution.target_text):
            issues.append(QualityIssue(
                issue_type=QualityIssueType.FORMATTING_ERROR,
                severity='low',
                message="Excessive whitespace found",
                suggestion="Clean up spacing",
                auto_fixable=True
            ))
        
        # Check for leading/trailing whitespace
        if (contribution.source_text != contribution.source_text.strip() or
            contribution.target_text != contribution.target_text.strip()):
            issues.append(QualityIssue(
                issue_type=QualityIssueType.FORMATTING_ERROR,
                severity='low',
                message="Leading or trailing whitespace found",
                suggestion="Trim whitespace",
                auto_fixable=True
            ))
        
        return issues
    
    @staticmethod
    def _check_difficulty_consistency(contribution: Contribution) -> List[QualityIssue]:
        """Check if difficulty level matches content complexity"""
        issues = []
        
        if not contribution.difficulty_level:
            return issues
        
        try:
            suggested_level, confidence = NLPService.suggest_difficulty_level(
                contribution.source_text
            )
            
            if suggested_level != contribution.difficulty_level and confidence > 0.7:
                issues.append(QualityIssue(
                    issue_type=QualityIssueType.DIFFICULTY_MISMATCH,
                    severity='medium',
                    message=f"Difficulty mismatch: marked as {contribution.difficulty_level.value}, "
                            f"suggests {suggested_level.value}",
                    suggestion=f"Consider changing difficulty to {suggested_level.value}",
                    confidence=confidence
                ))
        except Exception:
            pass
        
        return issues
    
    @staticmethod
    def _check_category_relevance(contribution: Contribution) -> List[QualityIssue]:
        """Check if categories are relevant to content"""
        issues = []
        
        if not contribution.categories:
            issues.append(QualityIssue(
                issue_type=QualityIssueType.CATEGORY_MISMATCH,
                severity='medium',
                message="No categories assigned",
                suggestion="Assign relevant categories"
            ))
        
        return issues
    
    @staticmethod
    def _check_translation_completeness(contribution: Contribution) -> List[QualityIssue]:
        """Check for translation completeness and accuracy markers"""
        issues = []
        
        # Check for missing context when it might be needed
        if (len(contribution.source_text.split()) > 5 and 
            not contribution.context_notes and 
            not contribution.cultural_notes):
            issues.append(QualityIssue(
                issue_type=QualityIssueType.MISSING_CONTEXT,
                severity='low',
                message="Consider adding context notes for longer phrases",
                suggestion="Add context or cultural notes to help users understand usage"
            ))
        
        return issues
    
    @staticmethod
    def _generate_recommendations(issues: List[QualityIssue]) -> List[str]:
        """Generate actionable recommendations based on issues"""
        recommendations = []
        
        high_issues = [i for i in issues if i.severity == 'high']
        if high_issues:
            recommendations.append("Address high-priority issues before approval")
        
        spelling_issues = [i for i in issues if i.issue_type == QualityIssueType.SPELLING_ERROR]
        if spelling_issues:
            recommendations.append("Review and correct spelling errors")
        
        auto_fixable = [i for i in issues if i.auto_fixable]
        if auto_fixable:
            recommendations.append("Some issues can be automatically fixed")
        
        if not issues:
            recommendations.append("Quality looks good - ready for approval")
        
        return recommendations
    
    @staticmethod
    @cached(ttl=CacheConfig.ANALYTICS_TTL, key_prefix="qa_batch_analysis")
    def batch_quality_analysis(
        db: Session,
        status_filter: Optional[ContributionStatus] = ContributionStatus.PENDING,
        limit: int = 100,
        min_quality_threshold: float = 0.0
    ) -> Dict[str, Any]:
        """Perform quality analysis on multiple contributions"""
        query = db.query(Contribution)
        
        if status_filter:
            query = query.filter(Contribution.status == status_filter)
        
        contributions = query.order_by(desc(Contribution.created_at)).limit(limit).all()
        
        results = {
            'total_analyzed': len(contributions),
            'quality_distribution': {'high': 0, 'medium': 0, 'low': 0},
            'auto_approve_eligible': 0,
            'requires_review': 0,
            'common_issues': {},
            'recommendations': [],
            'contributions': []
        }
        
        issue_counts = {}
        
        for contribution in contributions:
            try:
                report = QualityAssuranceService.analyze_contribution_quality(
                    db, contribution.id, detailed=False
                )
                
                if report.overall_score >= min_quality_threshold:
                    # Categorize quality
                    if report.overall_score >= 0.8:
                        results['quality_distribution']['high'] += 1
                    elif report.overall_score >= 0.6:
                        results['quality_distribution']['medium'] += 1
                    else:
                        results['quality_distribution']['low'] += 1
                    
                    if report.auto_approve_eligible:
                        results['auto_approve_eligible'] += 1
                    
                    if report.requires_review:
                        results['requires_review'] += 1
                    
                    # Count issue types
                    for issue in report.issues:
                        issue_type = issue.issue_type.value
                        issue_counts[issue_type] = issue_counts.get(issue_type, 0) + 1
                    
                    results['contributions'].append({
                        'id': contribution.id,
                        'quality_score': report.overall_score,
                        'issue_count': len(report.issues),
                        'auto_approve_eligible': report.auto_approve_eligible,
                        'requires_review': report.requires_review
                    })
            
            except Exception as e:
                # Skip problematic contributions
                continue
        
        # Identify common issues
        total_contributions = len(results['contributions'])
        if total_contributions > 0:
            for issue_type, count in issue_counts.items():
                percentage = (count / total_contributions) * 100
                if percentage >= 20:  # Common if affecting 20%+ of contributions
                    results['common_issues'][issue_type] = {
                        'count': count,
                        'percentage': round(percentage, 1)
                    }
        
        # Generate batch recommendations
        if results['auto_approve_eligible'] > 0:
            results['recommendations'].append(
                f"{results['auto_approve_eligible']} contributions eligible for auto-approval"
            )
        
        if results['requires_review'] > 0:
            results['recommendations'].append(
                f"{results['requires_review']} contributions need manual review"
            )
        
        if results['common_issues']:
            most_common = max(results['common_issues'].items(), key=lambda x: x[1]['count'])
            results['recommendations'].append(
                f"Most common issue: {most_common[0]} ({most_common[1]['percentage']}%)"
            )
        
        return results
    
    @staticmethod
    def auto_fix_contribution(
        db: Session,
        contribution_id: int,
        user_id: int
    ) -> Dict[str, Any]:
        """Automatically fix issues that can be safely corrected"""
        contribution = db.query(Contribution).filter(
            Contribution.id == contribution_id
        ).first()
        
        if not contribution:
            raise ValueError(f"Contribution {contribution_id} not found")
        
        report = QualityAssuranceService.analyze_contribution_quality(
            db, contribution_id, detailed=True
        )
        
        fixes_applied = []
        
        # Fix auto-fixable issues
        for issue in report.issues:
            if issue.auto_fixable:
                if issue.issue_type == QualityIssueType.FORMATTING_ERROR:
                    # Fix whitespace issues
                    original_source = contribution.source_text
                    original_target = contribution.target_text
                    
                    contribution.source_text = re.sub(r'\s+', ' ', contribution.source_text.strip())
                    contribution.target_text = re.sub(r'\s+', ' ', contribution.target_text.strip())
                    
                    if (contribution.source_text != original_source or 
                        contribution.target_text != original_target):
                        fixes_applied.append("Fixed whitespace formatting")
        
        # Save changes if any fixes were applied
        if fixes_applied:
            db.commit()
            
            # Log the auto-fix action
            audit_log = AuditLog(
                contribution_id=contribution_id,
                action='auto_fix',
                moderator_id=user_id,
                reason=f"Auto-fixed: {', '.join(fixes_applied)}",
                metadata=json.dumps({
                    'fixes_applied': fixes_applied,
                    'quality_score_before': report.overall_score
                })
            )
            db.add(audit_log)
            db.commit()
        
        return {
            'fixes_applied': fixes_applied,
            'contribution_id': contribution_id,
            'quality_score': report.overall_score
        }
    
    @staticmethod
    def get_moderation_queue(
        db: Session,
        priority_filter: Optional[str] = None,
        limit: int = 50
    ) -> List[Dict[str, Any]]:
        """Get prioritized moderation queue based on quality analysis"""
        contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.PENDING
        ).order_by(desc(Contribution.created_at)).limit(limit).all()
        
        queue = []
        
        for contribution in contributions:
            try:
                report = QualityAssuranceService.analyze_contribution_quality(
                    db, contribution.id, detailed=False
                )
                
                # Determine priority
                priority = 'low'
                if report.requires_review:
                    priority = 'high'
                elif report.auto_approve_eligible:
                    priority = 'auto'
                elif report.overall_score >= 0.7:
                    priority = 'medium'
                
                # Apply priority filter
                if priority_filter and priority != priority_filter:
                    continue
                
                queue.append({
                    'contribution_id': contribution.id,
                    'source_text': contribution.source_text[:100] + '...' if len(contribution.source_text) > 100 else contribution.source_text,
                    'target_text': contribution.target_text[:100] + '...' if len(contribution.target_text) > 100 else contribution.target_text,
                    'quality_score': report.overall_score,
                    'priority': priority,
                    'issue_count': len(report.issues),
                    'created_at': contribution.created_at.isoformat(),
                    'contributor': contribution.created_by.name if contribution.created_by else 'Unknown',
                    'auto_approve_eligible': report.auto_approve_eligible,
                    'requires_review': report.requires_review
                })
            
            except Exception:
                # Skip problematic contributions
                continue
        
        # Sort by priority and quality score
        priority_order = {'high': 0, 'medium': 1, 'low': 2, 'auto': 3}
        queue.sort(key=lambda x: (priority_order.get(x['priority'], 4), -x['quality_score']))
        
        return queue
    
    @staticmethod
    @cached(ttl=CacheConfig.ANALYTICS_TTL, key_prefix="qa_statistics")
    def get_quality_statistics(db: Session) -> Dict[str, Any]:
        """Get overall quality statistics"""
        total_contributions = db.query(func.count(Contribution.id)).scalar()
        
        # Status distribution
        status_counts = db.query(
            Contribution.status,
            func.count(Contribution.id)
        ).group_by(Contribution.status).all()
        
        status_distribution = {status.value: count for status, count in status_counts}
        
        # Quality score analysis for approved contributions
        approved_contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).all()
        
        quality_scores = []
        for contrib in approved_contributions[:100]:  # Sample for performance
            try:
                report = QualityAssuranceService.analyze_contribution_quality(
                    db, contrib.id, detailed=False
                )
                quality_scores.append(report.overall_score)
            except Exception:
                continue
        
        avg_quality = sum(quality_scores) / len(quality_scores) if quality_scores else 0
        
        return {
            'total_contributions': total_contributions,
            'status_distribution': status_distribution,
            'average_quality_score': round(avg_quality, 3),
            'quality_sample_size': len(quality_scores),
            'auto_approve_threshold': QualityAssuranceService.AUTO_APPROVE_THRESHOLD,
            'review_threshold': QualityAssuranceService.REQUIRES_REVIEW_THRESHOLD
        }