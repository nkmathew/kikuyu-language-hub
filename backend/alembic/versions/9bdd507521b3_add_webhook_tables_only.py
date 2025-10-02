"""Add webhook tables only

Revision ID: 9bdd507521b3
Revises: 5717bfe7580b
Create Date: 2025-10-03 00:55:45.351854

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '9bdd507521b3'
down_revision: Union[str, Sequence[str], None] = 'a75278c496f6'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Add webhook tables."""
    # Create webhooks table
    op.create_table('webhooks',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('url', sa.String(length=500), nullable=False),
        sa.Column('events', sa.Text(), nullable=False),
        sa.Column('secret', sa.String(length=100), nullable=True),
        sa.Column('status', sa.Enum('ACTIVE', 'INACTIVE', 'FAILED', name='webhookstatus'), nullable=True),
        sa.Column('timeout_seconds', sa.Integer(), nullable=True),
        sa.Column('retry_count', sa.Integer(), nullable=True),
        sa.Column('headers', sa.Text(), nullable=True),
        sa.Column('created_by_id', sa.Integer(), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('last_triggered_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('total_calls', sa.Integer(), nullable=True),
        sa.Column('successful_calls', sa.Integer(), nullable=True),
        sa.Column('failed_calls', sa.Integer(), nullable=True),
        sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_webhooks_id'), 'webhooks', ['id'], unique=False)
    op.create_index(op.f('ix_webhooks_status'), 'webhooks', ['status'], unique=False)
    
    # Create webhook_deliveries table
    op.create_table('webhook_deliveries',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('webhook_id', sa.Integer(), nullable=False),
        sa.Column('event_type', sa.Enum('CONTRIBUTION_CREATED', 'CONTRIBUTION_APPROVED', 'CONTRIBUTION_REJECTED', 'CONTRIBUTION_UPDATED', 'USER_REGISTERED', 'QUALITY_THRESHOLD_REACHED', 'DAILY_STATS_UPDATE', name='webhookevent'), nullable=False),
        sa.Column('payload', sa.Text(), nullable=False),
        sa.Column('headers_sent', sa.Text(), nullable=True),
        sa.Column('status_code', sa.Integer(), nullable=True),
        sa.Column('response_body', sa.Text(), nullable=True),
        sa.Column('response_headers', sa.Text(), nullable=True),
        sa.Column('error_message', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('(CURRENT_TIMESTAMP)'), nullable=True),
        sa.Column('delivered_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('duration_ms', sa.Integer(), nullable=True),
        sa.Column('attempt_number', sa.Integer(), nullable=True),
        sa.Column('is_successful', sa.Boolean(), nullable=True),
        sa.ForeignKeyConstraint(['webhook_id'], ['webhooks.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_webhook_deliveries_created_at'), 'webhook_deliveries', ['created_at'], unique=False)
    op.create_index(op.f('ix_webhook_deliveries_event_type'), 'webhook_deliveries', ['event_type'], unique=False)
    op.create_index(op.f('ix_webhook_deliveries_id'), 'webhook_deliveries', ['id'], unique=False)
    op.create_index(op.f('ix_webhook_deliveries_is_successful'), 'webhook_deliveries', ['is_successful'], unique=False)
    op.create_index(op.f('ix_webhook_deliveries_webhook_id'), 'webhook_deliveries', ['webhook_id'], unique=False)


def downgrade() -> None:
    """Remove webhook tables."""
    op.drop_index(op.f('ix_webhook_deliveries_webhook_id'), table_name='webhook_deliveries')
    op.drop_index(op.f('ix_webhook_deliveries_is_successful'), table_name='webhook_deliveries')
    op.drop_index(op.f('ix_webhook_deliveries_id'), table_name='webhook_deliveries')
    op.drop_index(op.f('ix_webhook_deliveries_event_type'), table_name='webhook_deliveries')
    op.drop_index(op.f('ix_webhook_deliveries_created_at'), table_name='webhook_deliveries')
    op.drop_table('webhook_deliveries')
    op.drop_index(op.f('ix_webhooks_status'), table_name='webhooks')
    op.drop_index(op.f('ix_webhooks_id'), table_name='webhooks')
    op.drop_table('webhooks')
