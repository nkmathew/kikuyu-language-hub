"""add simple morphology tables

Revision ID: a1b2c3d4e5f8
Revises: 97bca6d06987
Create Date: 2025-10-02 10:48:00.000000

"""
from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision = 'a1b2c3d4e5f8'
down_revision = '97bca6d06987'
branch_labels = None
depends_on = None


def upgrade():
    # Create word_classes table
    op.create_table('word_classes',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('word_type', sa.String(length=50), nullable=False),
        sa.Column('kikuyu_term', sa.String(length=100), nullable=False),
        sa.Column('english_term', sa.String(length=100), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('examples', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('kikuyu_term')
    )

    # Create verbs table
    op.create_table('verbs',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('base_form', sa.String(length=200), nullable=False),
        sa.Column('english_meaning', sa.String(length=500), nullable=False),
        sa.Column('word_class_id', sa.Integer(), nullable=True),
        sa.Column('verb_class', sa.String(length=50), nullable=True),
        sa.Column('consonant_pattern', sa.String(length=100), nullable=True),
        sa.Column('is_transitive', sa.Boolean(), nullable=True),
        sa.Column('is_stative', sa.Boolean(), nullable=True),
        sa.Column('semantic_field', sa.String(length=100), nullable=True),
        sa.Column('register', sa.String(length=50), nullable=True),
        sa.Column('pronunciation_guide', sa.String(length=500), nullable=True),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['word_class_id'], ['word_classes.id'], ),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('base_form')
    )
    op.create_index('ix_verbs_base_form', 'verbs', ['base_form'])

    # Create verb_conjugations table
    op.create_table('verb_conjugations',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('verb_id', sa.Integer(), nullable=False),
        sa.Column('tense', sa.String(length=50), nullable=False),
        sa.Column('aspect', sa.String(length=50), nullable=False),
        sa.Column('mood', sa.String(length=50), nullable=False),
        sa.Column('polarity', sa.String(length=50), nullable=False),
        sa.Column('person', sa.String(length=50), nullable=False),
        sa.Column('number', sa.String(length=50), nullable=False),
        sa.Column('conjugated_form', sa.String(length=500), nullable=False),
        sa.Column('morphological_breakdown', sa.Text(), nullable=True),
        sa.Column('usage_context', sa.String(length=200), nullable=True),
        sa.Column('frequency', sa.Integer(), nullable=True),
        sa.Column('is_common', sa.Boolean(), nullable=True),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['verb_id'], ['verbs.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_verb_conjugations_verb_id', 'verb_conjugations', ['verb_id'])

    # Create morphological_submissions table
    op.create_table('morphological_submissions',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('submission_type', sa.String(length=50), nullable=False),
        sa.Column('created_by_id', sa.Integer(), nullable=False),
        sa.Column('base_form', sa.String(length=200), nullable=False),
        sa.Column('english_meaning', sa.String(length=500), nullable=False),
        sa.Column('morphological_data', sa.Text(), nullable=False),
        sa.Column('context_notes', sa.Text(), nullable=True),
        sa.Column('source_reference', sa.String(length=300), nullable=True),
        sa.Column('confidence_level', sa.Integer(), nullable=True),
        sa.Column('status', sa.String(length=50), nullable=True),
        sa.Column('reviewed_by_id', sa.Integer(), nullable=True),
        sa.Column('review_notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
        sa.ForeignKeyConstraint(['reviewed_by_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_morphological_submissions_status', 'morphological_submissions', ['status'])


def downgrade():
    # Drop tables in reverse order
    op.drop_table('morphological_submissions')
    op.drop_table('verb_conjugations')
    op.drop_table('verbs')
    op.drop_table('word_classes')