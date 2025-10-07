"""Merge multiple migration heads

Revision ID: 4f9a97ff4219
Revises: a1b2c3d4e5f6, a1b2c3d4e5f7, a1b2c3d4e5f8
Create Date: 2025-10-02 18:38:00.340168

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '4f9a97ff4219'
down_revision: Union[str, Sequence[str], None] = ('a1b2c3d4e5f6', 'a1b2c3d4e5f7', 'a1b2c3d4e5f8')
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    pass


def downgrade() -> None:
    """Downgrade schema."""
    pass
