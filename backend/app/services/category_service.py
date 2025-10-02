from typing import List, Optional
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func
from ..models.category import Category
from ..models.contribution import Contribution, contribution_categories
from ..schemas.category import CategoryCreate, CategoryUpdate, CategoryHierarchy, CategoryStats
from ..core.cache import cache, cached, CacheConfig, invalidate_cache_on_change, cache_manager
from slugify import slugify


class CategoryService:
    @staticmethod
    @invalidate_cache_on_change(["categories:*", "category_hierarchy:*"])
    def create_category(db: Session, category_data: CategoryCreate) -> Category:
        # Auto-generate slug if not provided
        if not category_data.slug:
            category_data.slug = slugify(category_data.name)
        
        # Ensure slug is unique
        base_slug = category_data.slug
        counter = 1
        while db.query(Category).filter(Category.slug == category_data.slug).first():
            category_data.slug = f"{base_slug}-{counter}"
            counter += 1
        
        db_category = Category(
            name=category_data.name,
            description=category_data.description,
            slug=category_data.slug,
            parent_id=category_data.parent_id,
            is_active=category_data.is_active,
            sort_order=category_data.sort_order
        )
        db.add(db_category)
        db.commit()
        db.refresh(db_category)
        return db_category
    
    @staticmethod
    @cached(ttl=CacheConfig.CATEGORY_HIERARCHY_TTL, key_prefix="categories")
    def get_categories(
        db: Session,
        include_inactive: bool = False,
        parent_id: Optional[int] = None,
        skip: int = 0,
        limit: int = 100
    ) -> List[Category]:
        query = db.query(Category).options(
            joinedload(Category.parent),
            joinedload(Category.children)
        )
        
        if not include_inactive:
            query = query.filter(Category.is_active == True)
        
        if parent_id is not None:
            query = query.filter(Category.parent_id == parent_id)
        
        return query.order_by(Category.sort_order, Category.name).offset(skip).limit(limit).all()
    
    @staticmethod
    @cached(ttl=CacheConfig.CATEGORY_HIERARCHY_TTL, key_prefix="category_hierarchy")
    def get_category_hierarchy(db: Session, include_inactive: bool = False) -> List[CategoryHierarchy]:
        """Get flattened category hierarchy with contribution counts"""
        categories = CategoryService.get_categories(db, include_inactive=include_inactive, limit=1000)
        hierarchy = []
        
        def process_category(category: Category, level: int = 0):
            # Count contributions for this category
            contribution_count = db.query(func.count(contribution_categories.c.contribution_id))\
                .filter(contribution_categories.c.category_id == category.id)\
                .scalar() or 0
            
            hierarchy.append(CategoryHierarchy(
                id=category.id,
                name=category.name,
                description=category.description,
                slug=category.slug,
                full_path=category.full_path,
                level=level,
                is_active=category.is_active,
                sort_order=category.sort_order,
                contribution_count=contribution_count
            ))
            
            # Process children
            for child in sorted(category.children, key=lambda x: (x.sort_order, x.name)):
                if include_inactive or child.is_active:
                    process_category(child, level + 1)
        
        # Process root categories (no parent)
        root_categories = [cat for cat in categories if cat.parent_id is None]
        for category in sorted(root_categories, key=lambda x: (x.sort_order, x.name)):
            process_category(category)
        
        return hierarchy
    
    @staticmethod
    @cached(ttl=CacheConfig.CATEGORY_HIERARCHY_TTL, key_prefix="category")
    def get_category_by_id(db: Session, category_id: int) -> Optional[Category]:
        return db.query(Category).options(
            joinedload(Category.parent),
            joinedload(Category.children)
        ).filter(Category.id == category_id).first()
    
    @staticmethod
    @cached(ttl=CacheConfig.CATEGORY_HIERARCHY_TTL, key_prefix="category_slug")
    def get_category_by_slug(db: Session, slug: str) -> Optional[Category]:
        return db.query(Category).filter(Category.slug == slug).first()
    
    @staticmethod
    @invalidate_cache_on_change(["categories:*", "category_hierarchy:*", "category:*", "category_slug:*"])
    def update_category(
        db: Session,
        category_id: int,
        update_data: CategoryUpdate
    ) -> Optional[Category]:
        category = db.query(Category).filter(Category.id == category_id).first()
        if not category:
            return None
        
        # Update fields
        update_dict = update_data.dict(exclude_unset=True)
        
        # Handle slug update
        if 'name' in update_dict and 'slug' not in update_dict:
            update_dict['slug'] = slugify(update_dict['name'])
        elif 'slug' in update_dict:
            update_dict['slug'] = slugify(update_dict['slug'])
        
        # Ensure slug uniqueness
        if 'slug' in update_dict:
            base_slug = update_dict['slug']
            counter = 1
            while db.query(Category).filter(
                Category.slug == update_dict['slug'],
                Category.id != category_id
            ).first():
                update_dict['slug'] = f"{base_slug}-{counter}"
                counter += 1
        
        for field, value in update_dict.items():
            setattr(category, field, value)
        
        db.commit()
        db.refresh(category)
        return category
    
    @staticmethod
    @invalidate_cache_on_change(["categories:*", "category_hierarchy:*", "category:*", "category_slug:*"])
    def delete_category(db: Session, category_id: int) -> bool:
        """Soft delete by setting is_active=False"""
        category = db.query(Category).filter(Category.id == category_id).first()
        if not category:
            return False
        
        # Also deactivate all children
        def deactivate_recursively(cat: Category):
            cat.is_active = False
            for child in cat.children:
                deactivate_recursively(child)
        
        deactivate_recursively(category)
        db.commit()
        return True
    
    @staticmethod
    @cached(ttl=CacheConfig.ANALYTICS_TTL, key_prefix="category_stats")
    def get_category_stats(db: Session, category_id: int) -> Optional[CategoryStats]:
        """Get detailed statistics for a category"""
        category = db.query(Category).filter(Category.id == category_id).first()
        if not category:
            return None
        
        # Get contribution stats
        total_contributions = db.query(func.count(contribution_categories.c.contribution_id))\
            .filter(contribution_categories.c.category_id == category_id)\
            .scalar() or 0
        
        approved_contributions = db.query(func.count(Contribution.id))\
            .join(contribution_categories, Contribution.id == contribution_categories.c.contribution_id)\
            .filter(
                contribution_categories.c.category_id == category_id,
                Contribution.status == 'approved'
            ).scalar() or 0
        
        pending_contributions = db.query(func.count(Contribution.id))\
            .join(contribution_categories, Contribution.id == contribution_categories.c.contribution_id)\
            .filter(
                contribution_categories.c.category_id == category_id,
                Contribution.status == 'pending'
            ).scalar() or 0
        
        rejection_rate = 0.0
        if total_contributions > 0:
            rejected = total_contributions - approved_contributions - pending_contributions
            rejection_rate = (rejected / total_contributions) * 100
        
        unique_contributors = db.query(func.count(func.distinct(Contribution.created_by_id)))\
            .join(contribution_categories, Contribution.id == contribution_categories.c.contribution_id)\
            .filter(contribution_categories.c.category_id == category_id)\
            .scalar() or 0
        
        return CategoryStats(
            category_id=category.id,
            category_name=category.name,
            total_contributions=total_contributions,
            approved_contributions=approved_contributions,
            pending_contributions=pending_contributions,
            rejection_rate=rejection_rate,
            unique_contributors=unique_contributors
        )
    
    @staticmethod
    def search_categories(db: Session, query: str, limit: int = 20) -> List[Category]:
        """Search categories by name or description"""
        return db.query(Category).filter(
            Category.is_active == True,
            func.lower(Category.name).contains(query.lower()) |
            func.lower(Category.description).contains(query.lower())
        ).order_by(Category.name).limit(limit).all()
    
    @staticmethod
    @invalidate_cache_on_change(["categories:*", "category_hierarchy:*"])
    def reorder_categories(db: Session, category_orders: List[dict]) -> bool:
        """Update sort order for multiple categories"""
        try:
            for item in category_orders:
                category = db.query(Category).filter(Category.id == item['id']).first()
                if category:
                    category.sort_order = item['sort_order']
            
            db.commit()
            return True
        except Exception:
            db.rollback()
            return False