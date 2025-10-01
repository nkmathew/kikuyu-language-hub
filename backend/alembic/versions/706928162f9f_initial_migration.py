"""Initial migration

Revision ID: 706928162f9f
Revises: 
Create Date: 2025-10-01 17:43:31.573274

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '706928162f9f'
down_revision: Union[str, Sequence[str], None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # Create users table
    op.create_table('users',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('email', sa.String(), nullable=False),
    sa.Column('password_hash', sa.String(), nullable=False),
    sa.Column('role', sa.Enum('admin', 'moderator', 'contributor', name='userrole'), nullable=False),
    sa.Column('created_at', sa.DateTime(), nullable=False),
    sa.Column('updated_at', sa.DateTime(), nullable=False),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_users_id'), 'users', ['id'], unique=False)
    op.create_index(op.f('ix_users_email'), 'users', ['email'], unique=True)
    
    # Create contributions table
    op.create_table('contributions',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('source_text', sa.Text(), nullable=False),
    sa.Column('target_text', sa.Text(), nullable=False),
    sa.Column('status', sa.Enum('pending', 'approved', 'rejected', name='contributionstatus'), nullable=False),
    sa.Column('language', sa.String(), nullable=False),
    sa.Column('created_by_id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(), nullable=False),
    sa.Column('updated_at', sa.DateTime(), nullable=False),
    sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_contributions_id'), 'contributions', ['id'], unique=False)
    
    # Create audit_logs table
    op.create_table('audit_logs',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('contribution_id', sa.Integer(), nullable=False),
    sa.Column('action', sa.Enum('approve', 'reject', name='auditaction'), nullable=False),
    sa.Column('moderator_id', sa.Integer(), nullable=False),
    sa.Column('reason', sa.Text(), nullable=True),
    sa.Column('created_at', sa.DateTime(), nullable=False),
    sa.ForeignKeyConstraint(['contribution_id'], ['contributions.id'], ),
    sa.ForeignKeyConstraint(['moderator_id'], ['users.id'], ),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_audit_logs_id'), 'audit_logs', ['id'], unique=False)


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_index(op.f('ix_audit_logs_id'), table_name='audit_logs')
    op.drop_table('audit_logs')
    op.drop_index(op.f('ix_contributions_id'), table_name='contributions')
    op.drop_table('contributions')
    op.drop_index(op.f('ix_users_email'), table_name='users')
    op.drop_index(op.f('ix_users_id'), table_name='users')
    op.drop_table('users')
