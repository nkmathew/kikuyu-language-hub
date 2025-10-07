from datetime import datetime
from pydantic import BaseModel
from typing import Optional, List


class CategoryBase(BaseModel):
    name: str
    description: Optional[str] = None
    slug: str
    parent_id: Optional[int] = None
    is_active: bool = True
    sort_order: int = 0


class CategoryCreate(CategoryBase):
    pass


class CategoryUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    slug: Optional[str] = None
    parent_id: Optional[int] = None
    is_active: Optional[bool] = None
    sort_order: Optional[int] = None


class CategoryResponse(CategoryBase):
    id: int
    created_at: datetime
    updated_at: datetime
    parent: Optional['CategoryResponse'] = None
    children: List['CategoryResponse'] = []
    contribution_count: Optional[int] = None
    
    class Config:
        from_attributes = True


class CategoryHierarchy(BaseModel):
    """Flattened category with full path information"""
    id: int
    name: str
    description: Optional[str]
    slug: str
    full_path: str
    level: int
    is_active: bool
    sort_order: int
    contribution_count: int = 0
    
    class Config:
        from_attributes = True


class CategoryStats(BaseModel):
    """Category statistics for analytics"""
    category_id: int
    category_name: str
    total_contributions: int
    approved_contributions: int
    pending_contributions: int
    rejection_rate: float
    unique_contributors: int
    
    class Config:
        from_attributes = True


# Forward reference resolution
CategoryResponse.model_rebuild()