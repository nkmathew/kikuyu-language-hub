"""
Efficient pagination utilities for large datasets
"""
from typing import TypeVar, Generic, List, Optional, Dict, Any
from pydantic import BaseModel, Field
from sqlalchemy.orm import Query
from sqlalchemy import func, desc, asc
from math import ceil

T = TypeVar('T')


class PaginationParams(BaseModel):
    """
    Standard pagination parameters
    """
    page: int = Field(1, ge=1, description="Page number (1-based)")
    limit: int = Field(20, ge=1, le=100, description="Items per page")
    sort_by: Optional[str] = Field(None, description="Field to sort by")
    sort_order: str = Field("asc", regex="^(asc|desc)$", description="Sort order")


class CursorPaginationParams(BaseModel):
    """
    Cursor-based pagination parameters for large datasets
    """
    cursor: Optional[str] = Field(None, description="Cursor for next page")
    limit: int = Field(20, ge=1, le=100, description="Items per page")
    sort_by: str = Field("id", description="Field to sort by (must be unique)")
    sort_order: str = Field("asc", regex="^(asc|desc)$", description="Sort order")


class PaginatedResponse(BaseModel, Generic[T]):
    """
    Standard paginated response format
    """
    items: List[T]
    total: int
    page: int
    limit: int
    total_pages: int
    has_next: bool
    has_previous: bool
    
    @classmethod
    def create(cls, items: List[T], total: int, page: int, limit: int):
        total_pages = ceil(total / limit) if total > 0 else 0
        return cls(
            items=items,
            total=total,
            page=page,
            limit=limit,
            total_pages=total_pages,
            has_next=page < total_pages,
            has_previous=page > 1
        )


class CursorPaginatedResponse(BaseModel, Generic[T]):
    """
    Cursor-based paginated response format
    """
    items: List[T]
    next_cursor: Optional[str]
    has_next: bool
    limit: int
    
    @classmethod
    def create(cls, items: List[T], next_cursor: Optional[str], limit: int):
        return cls(
            items=items,
            next_cursor=next_cursor,
            has_next=next_cursor is not None,
            limit=limit
        )


class Paginator:
    """
    Efficient pagination utility with support for both offset and cursor pagination
    """
    
    @staticmethod
    def paginate_query(
        query: Query,
        params: PaginationParams,
        count_query: Optional[Query] = None
    ) -> PaginatedResponse:
        """
        Apply offset-based pagination to a SQLAlchemy query
        """
        # Apply sorting
        if params.sort_by:
            sort_column = getattr(query.column_descriptions[0]['type'], params.sort_by, None)
            if sort_column:
                if params.sort_order == "desc":
                    query = query.order_by(desc(sort_column))
                else:
                    query = query.order_by(asc(sort_column))
        
        # Count total items
        if count_query is None:
            count_query = query.statement.with_only_columns(func.count()).order_by(None)
            total = query.session.execute(count_query).scalar()
        else:
            total = count_query.scalar()
        
        # Apply pagination
        offset = (params.page - 1) * params.limit
        items = query.offset(offset).limit(params.limit).all()
        
        return PaginatedResponse.create(items, total, params.page, params.limit)
    
    @staticmethod
    def cursor_paginate_query(
        query: Query,
        params: CursorPaginationParams,
        cursor_column: str = None
    ) -> CursorPaginatedResponse:
        """
        Apply cursor-based pagination to a SQLAlchemy query
        More efficient for large datasets
        """
        cursor_column = cursor_column or params.sort_by
        sort_column = getattr(query.column_descriptions[0]['type'], cursor_column)
        
        # Apply cursor filtering
        if params.cursor:
            try:
                cursor_value = int(params.cursor)  # Assuming integer cursor
                if params.sort_order == "desc":
                    query = query.filter(sort_column < cursor_value)
                else:
                    query = query.filter(sort_column > cursor_value)
            except ValueError:
                pass  # Invalid cursor, ignore
        
        # Apply sorting
        if params.sort_order == "desc":
            query = query.order_by(desc(sort_column))
        else:
            query = query.order_by(asc(sort_column))
        
        # Fetch one extra item to determine if there's a next page
        items = query.limit(params.limit + 1).all()
        
        has_next = len(items) > params.limit
        if has_next:
            items = items[:-1]  # Remove the extra item
        
        # Generate next cursor
        next_cursor = None
        if has_next and items:
            cursor_value = getattr(items[-1], cursor_column)
            next_cursor = str(cursor_value)
        
        return CursorPaginatedResponse.create(items, next_cursor, params.limit)


class SearchPaginator(Paginator):
    """
    Enhanced paginator with search and filtering capabilities
    """
    
    @staticmethod
    def paginate_search_results(
        query: Query,
        search_params: Dict[str, Any],
        pagination_params: PaginationParams
    ) -> PaginatedResponse:
        """
        Paginate search results with filters applied
        """
        # Apply search filters
        filtered_query = SearchPaginator._apply_search_filters(query, search_params)
        
        # Apply pagination
        return Paginator.paginate_query(filtered_query, pagination_params)
    
    @staticmethod
    def _apply_search_filters(query: Query, filters: Dict[str, Any]) -> Query:
        """
        Apply search filters to query
        """
        for field, value in filters.items():
            if value is not None:
                if isinstance(value, str) and value.strip():
                    # Text search with LIKE
                    column = getattr(query.column_descriptions[0]['type'], field, None)
                    if column:
                        query = query.filter(column.ilike(f"%{value}%"))
                elif isinstance(value, (int, float, bool)):
                    # Exact match for numeric/boolean values
                    column = getattr(query.column_descriptions[0]['type'], field, None)
                    if column:
                        query = query.filter(column == value)
                elif isinstance(value, list):
                    # IN clause for list values
                    column = getattr(query.column_descriptions[0]['type'], field, None)
                    if column and value:
                        query = query.filter(column.in_(value))
        
        return query


class PaginationCache:
    """
    Caching for pagination results to improve performance
    """
    
    def __init__(self, cache_backend=None):
        self.cache = cache_backend or {}  # Simple dict cache for now
    
    def get_cached_page(self, cache_key: str) -> Optional[Dict[str, Any]]:
        """
        Get cached pagination result
        """
        return self.cache.get(cache_key)
    
    def cache_page(self, cache_key: str, result: Dict[str, Any], ttl: int = 300):
        """
        Cache pagination result
        """
        # Simple cache implementation - in production, use Redis
        self.cache[cache_key] = {
            'data': result,
            'expires_at': None  # Would implement TTL with Redis
        }
    
    def generate_cache_key(self, query_hash: str, params: PaginationParams) -> str:
        """
        Generate cache key for pagination result
        """
        return f"pagination:{query_hash}:{params.page}:{params.limit}:{params.sort_by}:{params.sort_order}"


# Global pagination cache instance
pagination_cache = PaginationCache()


def paginate(
    query: Query,
    page: int = 1,
    limit: int = 20,
    sort_by: Optional[str] = None,
    sort_order: str = "asc",
    use_cache: bool = False
) -> PaginatedResponse:
    """
    Convenience function for quick pagination
    """
    params = PaginationParams(
        page=page,
        limit=limit,
        sort_by=sort_by,
        sort_order=sort_order
    )
    
    if use_cache:
        cache_key = pagination_cache.generate_cache_key(str(hash(str(query))), params)
        cached_result = pagination_cache.get_cached_page(cache_key)
        if cached_result:
            return PaginatedResponse(**cached_result['data'])
    
    result = Paginator.paginate_query(query, params)
    
    if use_cache:
        pagination_cache.cache_page(cache_key, result.dict())
    
    return result


def cursor_paginate(
    query: Query,
    cursor: Optional[str] = None,
    limit: int = 20,
    sort_by: str = "id",
    sort_order: str = "asc"
) -> CursorPaginatedResponse:
    """
    Convenience function for cursor-based pagination
    """
    params = CursorPaginationParams(
        cursor=cursor,
        limit=limit,
        sort_by=sort_by,
        sort_order=sort_order
    )
    
    return Paginator.cursor_paginate_query(query, params)