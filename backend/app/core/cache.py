"""
Redis caching layer for improved performance
"""
import json
import redis
from typing import Any, Optional, Dict, List, Union
from functools import wraps
import hashlib
import pickle
from datetime import timedelta
import logging
from .config import settings

logger = logging.getLogger(__name__)


class CacheConfig:
    """
    Cache configuration and TTL settings
    """
    DEFAULT_TTL = 300  # 5 minutes
    CATEGORY_HIERARCHY_TTL = 3600  # 1 hour
    POPULAR_TRANSLATIONS_TTL = 1800  # 30 minutes
    USER_SESSION_TTL = 86400  # 24 hours
    EXPORT_DATA_TTL = 3600  # 1 hour
    TRANSLATION_SUGGESTIONS_TTL = 7200  # 2 hours
    ANALYTICS_TTL = 900  # 15 minutes


class RedisCache:
    """
    Redis cache manager with connection pooling and error handling
    """
    
    def __init__(self, redis_url: str = None):
        self.redis_url = redis_url or getattr(settings, 'redis_url', 'redis://localhost:6379/0')
        self._redis_client = None
        self._connection_pool = None
    
    @property
    def redis_client(self) -> redis.Redis:
        """
        Get Redis client with connection pooling
        """
        if self._redis_client is None:
            try:
                self._connection_pool = redis.ConnectionPool.from_url(
                    self.redis_url,
                    max_connections=20,
                    retry_on_timeout=True,
                    health_check_interval=30
                )
                self._redis_client = redis.Redis(
                    connection_pool=self._connection_pool,
                    decode_responses=True,
                    socket_connect_timeout=5,
                    socket_timeout=5
                )
                # Test connection
                self._redis_client.ping()
                logger.info("Redis connection established successfully")
            except Exception as e:
                logger.warning(f"Redis connection failed: {e}. Falling back to in-memory cache.")
                self._redis_client = DummyRedis()  # Fallback to dummy implementation
        
        return self._redis_client
    
    def get(self, key: str) -> Optional[Any]:
        """
        Get value from cache
        """
        try:
            value = self.redis_client.get(key)
            if value is not None:
                return json.loads(value)
        except Exception as e:
            logger.error(f"Cache get error for key {key}: {e}")
        return None
    
    def set(self, key: str, value: Any, ttl: int = CacheConfig.DEFAULT_TTL) -> bool:
        """
        Set value in cache with TTL
        """
        try:
            serialized_value = json.dumps(value, default=str)
            return self.redis_client.setex(key, ttl, serialized_value)
        except Exception as e:
            logger.error(f"Cache set error for key {key}: {e}")
            return False
    
    def delete(self, key: str) -> bool:
        """
        Delete value from cache
        """
        try:
            return bool(self.redis_client.delete(key))
        except Exception as e:
            logger.error(f"Cache delete error for key {key}: {e}")
            return False
    
    def delete_pattern(self, pattern: str) -> int:
        """
        Delete all keys matching pattern
        """
        try:
            keys = self.redis_client.keys(pattern)
            if keys:
                return self.redis_client.delete(*keys)
            return 0
        except Exception as e:
            logger.error(f"Cache delete pattern error for pattern {pattern}: {e}")
            return 0
    
    def exists(self, key: str) -> bool:
        """
        Check if key exists in cache
        """
        try:
            return bool(self.redis_client.exists(key))
        except Exception as e:
            logger.error(f"Cache exists error for key {key}: {e}")
            return False
    
    def increment(self, key: str, amount: int = 1, ttl: int = None) -> int:
        """
        Increment counter with optional TTL
        """
        try:
            value = self.redis_client.incr(key, amount)
            if ttl and value == amount:  # First time setting the key
                self.redis_client.expire(key, ttl)
            return value
        except Exception as e:
            logger.error(f"Cache increment error for key {key}: {e}")
            return 0
    
    def get_multiple(self, keys: List[str]) -> Dict[str, Any]:
        """
        Get multiple values from cache
        """
        try:
            values = self.redis_client.mget(keys)
            result = {}
            for key, value in zip(keys, values):
                if value is not None:
                    try:
                        result[key] = json.loads(value)
                    except json.JSONDecodeError:
                        pass
            return result
        except Exception as e:
            logger.error(f"Cache get_multiple error: {e}")
            return {}
    
    def set_multiple(self, mapping: Dict[str, Any], ttl: int = CacheConfig.DEFAULT_TTL) -> bool:
        """
        Set multiple values in cache
        """
        try:
            pipe = self.redis_client.pipeline()
            for key, value in mapping.items():
                serialized_value = json.dumps(value, default=str)
                pipe.setex(key, ttl, serialized_value)
            pipe.execute()
            return True
        except Exception as e:
            logger.error(f"Cache set_multiple error: {e}")
            return False


class DummyRedis:
    """
    Dummy Redis implementation for fallback when Redis is unavailable
    """
    
    def __init__(self):
        self._cache = {}
    
    def ping(self):
        return True
    
    def get(self, key: str):
        return self._cache.get(key)
    
    def setex(self, key: str, ttl: int, value: str):
        self._cache[key] = value
        return True
    
    def delete(self, *keys):
        count = 0
        for key in keys:
            if key in self._cache:
                del self._cache[key]
                count += 1
        return count
    
    def exists(self, key: str):
        return key in self._cache
    
    def incr(self, key: str, amount: int = 1):
        current = int(self._cache.get(key, 0))
        self._cache[key] = str(current + amount)
        return current + amount
    
    def expire(self, key: str, ttl: int):
        return True
    
    def keys(self, pattern: str):
        # Simple pattern matching for dummy implementation
        import fnmatch
        return [k for k in self._cache.keys() if fnmatch.fnmatch(k, pattern)]
    
    def mget(self, keys: List[str]):
        return [self._cache.get(key) for key in keys]
    
    def pipeline(self):
        return self


# Global cache instance
cache = RedisCache()


def cache_key(*args, **kwargs) -> str:
    """
    Generate cache key from arguments
    """
    key_parts = []
    
    # Add positional arguments
    for arg in args:
        if isinstance(arg, (str, int, float, bool)):
            key_parts.append(str(arg))
        else:
            key_parts.append(hashlib.md5(str(arg).encode()).hexdigest()[:8])
    
    # Add keyword arguments
    for k, v in sorted(kwargs.items()):
        key_parts.append(f"{k}={v}")
    
    return ":".join(key_parts)


def cached(ttl: int = CacheConfig.DEFAULT_TTL, key_prefix: str = ""):
    """
    Decorator for caching function results
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # Generate cache key
            func_key = f"{key_prefix}:{func.__name__}" if key_prefix else func.__name__
            cache_key_str = f"{func_key}:{cache_key(*args, **kwargs)}"
            
            # Try to get from cache
            cached_result = cache.get(cache_key_str)
            if cached_result is not None:
                logger.debug(f"Cache hit for {cache_key_str}")
                return cached_result
            
            # Execute function and cache result
            logger.debug(f"Cache miss for {cache_key_str}")
            result = func(*args, **kwargs)
            cache.set(cache_key_str, result, ttl)
            
            return result
        
        # Add cache invalidation method
        wrapper.invalidate_cache = lambda *args, **kwargs: cache.delete(
            f"{key_prefix}:{func.__name__}:{cache_key(*args, **kwargs)}" if key_prefix else f"{func.__name__}:{cache_key(*args, **kwargs)}"
        )
        
        return wrapper
    return decorator


class CacheManager:
    """
    High-level cache management for specific application domains
    """
    
    @staticmethod
    def invalidate_category_cache():
        """
        Invalidate all category-related cache entries
        """
        patterns = [
            "categories:*",
            "category_hierarchy:*",
            "category_stats:*"
        ]
        
        for pattern in patterns:
            cache.delete_pattern(pattern)
        
        logger.info("Category cache invalidated")
    
    @staticmethod
    def invalidate_contribution_cache(contribution_id: int = None):
        """
        Invalidate contribution-related cache entries
        """
        patterns = [
            "contributions:*",
            "popular_translations:*",
            "export_data:*"
        ]
        
        if contribution_id:
            patterns.append(f"contribution:{contribution_id}:*")
        
        for pattern in patterns:
            cache.delete_pattern(pattern)
        
        logger.info(f"Contribution cache invalidated for ID: {contribution_id}")
    
    @staticmethod
    def invalidate_user_cache(user_id: int):
        """
        Invalidate user-related cache entries
        """
        patterns = [
            f"user:{user_id}:*",
            f"user_analytics:{user_id}:*",
            f"user_session:{user_id}:*"
        ]
        
        for pattern in patterns:
            cache.delete_pattern(pattern)
        
        logger.info(f"User cache invalidated for ID: {user_id}")
    
    @staticmethod
    def warm_cache():
        """
        Pre-populate cache with frequently accessed data
        """
        logger.info("Starting cache warming...")
        
        # This would typically be called on application startup
        # to pre-populate frequently accessed data
        
        # Example: Pre-load category hierarchy
        # categories = CategoryService.get_category_hierarchy(db)
        # cache.set("category_hierarchy:all", categories, CacheConfig.CATEGORY_HIERARCHY_TTL)
        
        logger.info("Cache warming completed")
    
    @staticmethod
    def get_cache_stats() -> Dict[str, Any]:
        """
        Get cache statistics and health information
        """
        try:
            info = cache.redis_client.info()
            return {
                "connected_clients": info.get("connected_clients", 0),
                "used_memory": info.get("used_memory_human", "0B"),
                "keyspace_hits": info.get("keyspace_hits", 0),
                "keyspace_misses": info.get("keyspace_misses", 0),
                "hit_rate": info.get("keyspace_hits", 0) / max(1, info.get("keyspace_hits", 0) + info.get("keyspace_misses", 0)) * 100
            }
        except Exception as e:
            logger.error(f"Error getting cache stats: {e}")
            return {"error": str(e)}


# Cache invalidation decorators
def invalidate_cache_on_change(cache_patterns: List[str]):
    """
    Decorator to invalidate cache when data changes
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            result = func(*args, **kwargs)
            
            # Invalidate cache patterns
            for pattern in cache_patterns:
                cache.delete_pattern(pattern)
            
            return result
        return wrapper
    return decorator


# Export cache manager instance
cache_manager = CacheManager()