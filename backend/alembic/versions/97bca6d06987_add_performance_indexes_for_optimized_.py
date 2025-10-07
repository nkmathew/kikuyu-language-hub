"""Add performance indexes for optimized queries

Revision ID: 97bca6d06987
Revises: 429159612ffc
Create Date: 2025-10-02 02:48:03.110241

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '97bca6d06987'
down_revision: Union[str, Sequence[str], None] = '429159612ffc'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # Performance indexes for optimized queries
    
    # Contributions table indexes
    op.create_index('ix_contributions_status_created_quality', 'contributions', 
                   ['status', 'created_at', 'quality_score'])
    op.create_index('ix_contributions_source_target_text', 'contributions', 
                   ['source_text', 'target_text'])
    op.create_index('ix_contributions_difficulty_status', 'contributions',
                   ['difficulty_level', 'status'])
    
    # Sub-translations table indexes  
    op.create_index('ix_subtrans_contrib_position', 'sub_translations',
                   ['parent_contribution_id', 'word_position'])
    op.create_index('ix_subtrans_source_target', 'sub_translations',
                   ['source_word', 'target_word'])
    
    # Categories table indexes
    op.create_index('ix_categories_parent_active_sort', 'categories',
                   ['parent_id', 'is_active', 'sort_order'])
    
    # Analytics events table indexes
    op.create_index('ix_analytics_type_time_user', 'analytics_events',
                   ['event_type', 'timestamp', 'user_id'])
    op.create_index('ix_analytics_timestamp_desc', 'analytics_events',
                   [sa.text('timestamp DESC')])
    
    # User analytics table indexes
    op.create_index('ix_user_analytics_contributions', 'user_analytics',
                   ['total_contributions', 'approved_contributions'])
    
    # Contribution categories association table indexes
    op.create_index('ix_contrib_cat_contrib_id', 'contribution_categories',
                   ['contribution_id'])
    op.create_index('ix_contrib_cat_category_id', 'contribution_categories', 
                   ['category_id'])


def downgrade() -> None:
    """Downgrade schema."""
    # Drop performance indexes
    op.drop_index('ix_contrib_cat_category_id', 'contribution_categories')
    op.drop_index('ix_contrib_cat_contrib_id', 'contribution_categories')
    op.drop_index('ix_user_analytics_contributions', 'user_analytics')
    op.drop_index('ix_analytics_timestamp_desc', 'analytics_events')
    op.drop_index('ix_analytics_type_time_user', 'analytics_events')
    op.drop_index('ix_categories_parent_active_sort', 'categories')
    op.drop_index('ix_subtrans_source_target', 'sub_translations')
    op.drop_index('ix_subtrans_contrib_position', 'sub_translations')
    op.drop_index('ix_contributions_difficulty_status', 'contributions')
    op.drop_index('ix_contributions_source_target_text', 'contributions')
    op.drop_index('ix_contributions_status_created_quality', 'contributions')
