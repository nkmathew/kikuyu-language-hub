from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from ...db.session import get_db
from ...models.user import User
from ...services.category_service import CategoryService
from ...schemas.category import (
    CategoryCreate, CategoryUpdate, CategoryResponse, 
    CategoryHierarchy, CategoryStats
)
from ...core.security import get_current_user, require_moderator_or_admin

router = APIRouter(prefix="/categories", tags=["categories"])


@router.get("/", response_model=List[CategoryResponse])
def get_categories(
    include_inactive: bool = Query(False, description="Include inactive categories"),
    parent_id: Optional[int] = Query(None, description="Filter by parent category ID"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    """Get all categories with optional filtering"""
    return CategoryService.get_categories(
        db=db,
        include_inactive=include_inactive,
        parent_id=parent_id,
        skip=skip,
        limit=limit
    )


@router.get("/hierarchy", response_model=List[CategoryHierarchy])
def get_category_hierarchy(
    include_inactive: bool = Query(False, description="Include inactive categories"),
    db: Session = Depends(get_db)
):
    """Get flattened category hierarchy with contribution counts"""
    return CategoryService.get_category_hierarchy(db=db, include_inactive=include_inactive)


@router.get("/search", response_model=List[CategoryResponse])
def search_categories(
    q: str = Query(..., min_length=1, description="Search query"),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """Search categories by name or description"""
    return CategoryService.search_categories(db=db, query=q, limit=limit)


@router.get("/{category_id}", response_model=CategoryResponse)
def get_category(
    category_id: int,
    db: Session = Depends(get_db)
):
    """Get a specific category by ID"""
    category = CategoryService.get_category_by_id(db=db, category_id=category_id)
    if not category:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@router.get("/slug/{slug}", response_model=CategoryResponse)
def get_category_by_slug(
    slug: str,
    db: Session = Depends(get_db)
):
    """Get a specific category by slug"""
    category = CategoryService.get_category_by_slug(db=db, slug=slug)
    if not category:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@router.get("/{category_id}/stats", response_model=CategoryStats)
def get_category_stats(
    category_id: int,
    db: Session = Depends(get_db)
):
    """Get detailed statistics for a category"""
    stats = CategoryService.get_category_stats(db=db, category_id=category_id)
    if not stats:
        raise HTTPException(status_code=404, detail="Category not found")
    return stats


@router.post("/", response_model=CategoryResponse)
def create_category(
    category_data: CategoryCreate,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Create a new category (moderator/admin only)"""
    try:
        return CategoryService.create_category(db=db, category_data=category_data)
    except Exception as e:
        if "UNIQUE constraint failed" in str(e):
            raise HTTPException(status_code=400, detail="Category slug already exists")
        raise HTTPException(status_code=400, detail=f"Failed to create category: {str(e)}")


@router.put("/{category_id}", response_model=CategoryResponse)
def update_category(
    category_id: int,
    update_data: CategoryUpdate,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Update a category (moderator/admin only)"""
    category = CategoryService.update_category(
        db=db,
        category_id=category_id,
        update_data=update_data
    )
    if not category:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@router.delete("/{category_id}")
def delete_category(
    category_id: int,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Soft delete a category (moderator/admin only)"""
    success = CategoryService.delete_category(db=db, category_id=category_id)
    if not success:
        raise HTTPException(status_code=404, detail="Category not found")
    return {"message": "Category deleted successfully"}


@router.post("/reorder")
def reorder_categories(
    category_orders: List[dict],
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Update sort order for multiple categories (moderator/admin only)"""
    success = CategoryService.reorder_categories(db=db, category_orders=category_orders)
    if not success:
        raise HTTPException(status_code=400, detail="Failed to reorder categories")
    return {"message": "Categories reordered successfully"}