"""add morphology tables for kikuyu verb system

Revision ID: a1b2c3d4e5f6
Revises: 97bca6d06987
Create Date: 2025-10-02 10:45:00.000000

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import sqlite

# revision identifiers, used by Alembic.
revision = 'a1b2c3d4e5f6'
down_revision = '97bca6d06987'
branch_labels = None
depends_on = None


def upgrade():
    # For SQLite, we'll use string enums instead of native enum types

    # Create word_classes table
    op.create_table('word_classes',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('word_type', sa.Enum('noun', 'verb', 'adjective', 'adverb', 'pronoun', 'preposition', 'conjunction', 'interjection', name='wordtype'), nullable=False),
        sa.Column('kikuyu_term', sa.String(length=100), nullable=False),
        sa.Column('english_term', sa.String(length=100), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('examples', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('kikuyu_term')
    )
    
    # Create indexes for word_classes
    op.create_index('ix_word_classes_word_type', 'word_classes', ['word_type'])
    op.create_index('ix_word_classes_created_at', 'word_classes', ['created_at'])

    # Create verbs table
    op.create_table('verbs',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('base_form', sa.String(length=200), nullable=False),
        sa.Column('english_meaning', sa.String(length=500), nullable=False),
        sa.Column('word_class_id', sa.Integer(), nullable=True),
        sa.Column('verb_class', sa.String(length=50), nullable=True),
        sa.Column('consonant_pattern', sa.String(length=100), nullable=True),
        sa.Column('is_transitive', sa.Boolean(), nullable=True, server_default=sa.text('true')),
        sa.Column('is_stative', sa.Boolean(), nullable=True, server_default=sa.text('false')),
        sa.Column('semantic_field', sa.String(length=100), nullable=True),
        sa.Column('register', sa.String(length=50), nullable=True),
        sa.Column('pronunciation_guide', sa.String(length=500), nullable=True),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['word_class_id'], ['word_classes.id'], ),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('base_form')
    )
    
    # Create indexes for verbs
    op.create_index('ix_verbs_base_form', 'verbs', ['base_form'])
    op.create_index('ix_verbs_english_meaning', 'verbs', ['english_meaning'])
    op.create_index('ix_verbs_verb_class', 'verbs', ['verb_class'])
    op.create_index('ix_verbs_semantic_field', 'verbs', ['semantic_field'])
    op.create_index('ix_verbs_is_transitive', 'verbs', ['is_transitive'])
    op.create_index('ix_verbs_is_stative', 'verbs', ['is_stative'])
    op.create_index('ix_verbs_created_at', 'verbs', ['created_at'])

    # Create verb_conjugations table
    op.create_table('verb_conjugations',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('verb_id', sa.Integer(), nullable=False),
        sa.Column('tense', sa.Enum('present', 'past', 'future', 'habitual', name='tensetype'), nullable=False),
        sa.Column('aspect', sa.Enum('simple', 'continuous', 'perfect', 'perfect_continuous', name='aspecttype'), nullable=False),
        sa.Column('mood', sa.Enum('indicative', 'imperative', 'subjunctive', 'conditional', name='moodtype'), nullable=False),
        sa.Column('polarity', sa.Enum('affirmative', 'negative', name='polaritytype'), nullable=False),
        sa.Column('person', sa.Enum('first', 'second', 'third', name='persontype'), nullable=False),
        sa.Column('number', sa.Enum('singular', 'plural', name='numbertype'), nullable=False),
        sa.Column('object_person', sa.Enum('first', 'second', 'third', name='persontype'), nullable=True),
        sa.Column('object_number', sa.Enum('singular', 'plural', name='numbertype'), nullable=True),
        sa.Column('has_object', sa.Boolean(), nullable=True, server_default=sa.text('false')),
        sa.Column('conjugated_form', sa.String(length=500), nullable=False),
        sa.Column('morphological_breakdown', sa.JSON(), nullable=True),
        sa.Column('usage_context', sa.String(length=200), nullable=True),
        sa.Column('frequency', sa.Integer(), nullable=True, server_default=sa.text('1')),
        sa.Column('is_common', sa.Boolean(), nullable=True, server_default=sa.text('false')),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['verb_id'], ['verbs.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Create indexes for verb_conjugations
    op.create_index('ix_verb_conjugations_verb_id', 'verb_conjugations', ['verb_id'])
    op.create_index('ix_verb_conjugations_tense_aspect_mood', 'verb_conjugations', ['tense', 'aspect', 'mood'])
    op.create_index('ix_verb_conjugations_polarity', 'verb_conjugations', ['polarity'])
    op.create_index('ix_verb_conjugations_person_number', 'verb_conjugations', ['person', 'number'])
    op.create_index('ix_verb_conjugations_conjugated_form', 'verb_conjugations', ['conjugated_form'])
    op.create_index('ix_verb_conjugations_frequency', 'verb_conjugations', ['frequency'])
    op.create_index('ix_verb_conjugations_is_common', 'verb_conjugations', ['is_common'])
    op.create_index('ix_verb_conjugations_created_at', 'verb_conjugations', ['created_at'])

    # Create noun_forms table
    op.create_table('noun_forms',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('related_verb_id', sa.Integer(), nullable=True),
        sa.Column('noun_form', sa.String(length=200), nullable=False),
        sa.Column('english_meaning', sa.String(length=500), nullable=False),
        sa.Column('noun_class', sa.String(length=50), nullable=True),
        sa.Column('derivation_type', sa.String(length=100), nullable=True),
        sa.Column('morphological_pattern', sa.String(length=200), nullable=True),
        sa.Column('formation_rule', sa.String(length=500), nullable=True),
        sa.Column('examples', sa.JSON(), nullable=True),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['related_verb_id'], ['verbs.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Create indexes for noun_forms
    op.create_index('ix_noun_forms_related_verb_id', 'noun_forms', ['related_verb_id'])
    op.create_index('ix_noun_forms_noun_form', 'noun_forms', ['noun_form'])
    op.create_index('ix_noun_forms_derivation_type', 'noun_forms', ['derivation_type'])
    op.create_index('ix_noun_forms_created_at', 'noun_forms', ['created_at'])

    # Create verb_examples table
    op.create_table('verb_examples',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('verb_id', sa.Integer(), nullable=False),
        sa.Column('kikuyu_sentence', sa.Text(), nullable=False),
        sa.Column('english_translation', sa.Text(), nullable=False),
        sa.Column('context_description', sa.String(length=300), nullable=True),
        sa.Column('register', sa.String(length=50), nullable=True),
        sa.Column('verb_form_used', sa.String(length=200), nullable=True),
        sa.Column('tense_aspect_mood', sa.String(length=200), nullable=True),
        sa.Column('audio_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['verb_id'], ['verbs.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Create indexes for verb_examples
    op.create_index('ix_verb_examples_verb_id', 'verb_examples', ['verb_id'])
    op.create_index('ix_verb_examples_register', 'verb_examples', ['register'])
    op.create_index('ix_verb_examples_created_at', 'verb_examples', ['created_at'])

    # Create morphological_patterns table
    op.create_table('morphological_patterns',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('pattern_name', sa.String(length=100), nullable=False),
        sa.Column('pattern_type', sa.String(length=50), nullable=True),
        sa.Column('description', sa.Text(), nullable=False),
        sa.Column('rule', sa.String(length=500), nullable=True),
        sa.Column('examples', sa.JSON(), nullable=True),
        sa.Column('applies_to', sa.JSON(), nullable=True),
        sa.Column('conditions', sa.JSON(), nullable=True),
        sa.Column('audio_examples', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('pattern_name')
    )
    
    # Create indexes for morphological_patterns
    op.create_index('ix_morphological_patterns_pattern_type', 'morphological_patterns', ['pattern_type'])
    op.create_index('ix_morphological_patterns_created_at', 'morphological_patterns', ['created_at'])

    # Create morphological_submissions table
    op.create_table('morphological_submissions',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('submission_type', sa.String(length=50), nullable=False),
        sa.Column('created_by_id', sa.Integer(), nullable=False),
        sa.Column('base_form', sa.String(length=200), nullable=False),
        sa.Column('english_meaning', sa.String(length=500), nullable=False),
        sa.Column('morphological_data', sa.JSON(), nullable=False),
        sa.Column('context_notes', sa.Text(), nullable=True),
        sa.Column('source_reference', sa.String(length=300), nullable=True),
        sa.Column('confidence_level', sa.Integer(), nullable=True, server_default=sa.text('3')),
        sa.Column('status', sa.String(length=50), nullable=True, server_default=sa.text('pending')),
        sa.Column('reviewed_by_id', sa.Integer(), nullable=True),
        sa.Column('review_notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
        sa.ForeignKeyConstraint(['reviewed_by_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Create indexes for morphological_submissions
    op.create_index('ix_morphological_submissions_submission_type', 'morphological_submissions', ['submission_type'])
    op.create_index('ix_morphological_submissions_created_by_id', 'morphological_submissions', ['created_by_id'])
    op.create_index('ix_morphological_submissions_status', 'morphological_submissions', ['status'])
    op.create_index('ix_morphological_submissions_confidence_level', 'morphological_submissions', ['confidence_level'])
    op.create_index('ix_morphological_submissions_created_at', 'morphological_submissions', ['created_at'])


def downgrade():
    # Drop tables in reverse order
    op.drop_table('morphological_submissions')
    op.drop_table('morphological_patterns')
    op.drop_table('verb_examples')
    op.drop_table('noun_forms')
    op.drop_table('verb_conjugations')
    op.drop_table('verbs')
    op.drop_table('word_classes')
    
    # Drop enums
    sa.Enum(name='polaritytype').drop(op.get_bind())
    sa.Enum(name='persontype').drop(op.get_bind())
    sa.Enum(name='numbertype').drop(op.get_bind())
    sa.Enum(name='moodtype').drop(op.get_bind())
    sa.Enum(name='aspecttype').drop(op.get_bind())
    sa.Enum(name='tensetype').drop(op.get_bind())
    sa.Enum(name='wordtype').drop(op.get_bind())