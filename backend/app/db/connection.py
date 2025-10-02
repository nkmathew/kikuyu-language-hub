"""
Database connection management with pooling and performance optimization
"""
from sqlalchemy import create_engine, event, text
from sqlalchemy.engine import Engine
from sqlalchemy.pool import QueuePool
import time
import logging
from typing import Dict, Any, Optional
from ..core.config import settings

logger = logging.getLogger(__name__)

# Database performance monitoring
query_stats: Dict[str, Dict[str, Any]] = {}


def configure_database_engine(database_url: str) -> Engine:
    """
    Configure SQLAlchemy engine with connection pooling and performance optimization
    """
    engine_kwargs = {
        'pool_class': QueuePool,
        'pool_size': 20,  # Number of connections to maintain in the pool
        'max_overflow': 30,  # Additional connections that can be created on demand
        'pool_pre_ping': True,  # Validate connections before use
        'pool_recycle': 3600,  # Recycle connections after 1 hour
        'echo': settings.debug,  # Log SQL queries in debug mode
    }
    
    # SQLite-specific optimizations
    if database_url.startswith('sqlite'):
        engine_kwargs = {
            'echo': settings.debug,  # Log SQL queries in debug mode
            'connect_args': {
                'check_same_thread': False,
                'timeout': 20,
                'isolation_level': None,  # Autocommit mode for better performance
            }
        }
    
    engine = create_engine(database_url, **engine_kwargs)
    
    # Add performance monitoring
    setup_query_monitoring(engine)
    
    # Apply database-specific optimizations
    setup_database_optimizations(engine)
    
    return engine


def setup_query_monitoring(engine: Engine) -> None:
    """
    Set up query performance monitoring and logging
    """
    @event.listens_for(engine, "before_cursor_execute")
    def receive_before_cursor_execute(conn, cursor, statement, parameters, context, executemany):
        context._query_start_time = time.time()
        
        # Log slow queries in development
        if settings.debug:
            logger.debug(f"Executing query: {statement[:200]}...")
    
    @event.listens_for(engine, "after_cursor_execute")
    def receive_after_cursor_execute(conn, cursor, statement, parameters, context, executemany):
        total_time = time.time() - context._query_start_time
        
        # Track query statistics
        query_type = statement.strip().split()[0].upper()
        if query_type not in query_stats:
            query_stats[query_type] = {
                'count': 0,
                'total_time': 0,
                'avg_time': 0,
                'max_time': 0
            }
        
        stats = query_stats[query_type]
        stats['count'] += 1
        stats['total_time'] += total_time
        stats['avg_time'] = stats['total_time'] / stats['count']
        stats['max_time'] = max(stats['max_time'], total_time)
        
        # Log slow queries
        if total_time > 0.1:  # Log queries taking more than 100ms
            logger.warning(f"Slow query ({total_time:.3f}s): {statement[:200]}...")
    
    @event.listens_for(engine, "handle_error")
    def receive_handle_error(exception_context):
        logger.error(f"Database error: {exception_context.original_exception}")


def setup_database_optimizations(engine: Engine) -> None:
    """
    Apply database-specific performance optimizations
    """
    @event.listens_for(engine, "connect")
    def set_sqlite_pragma(dbapi_connection, connection_record):
        if engine.url.drivername.startswith('sqlite'):
            cursor = dbapi_connection.cursor()
            # SQLite performance optimizations
            cursor.execute("PRAGMA journal_mode=WAL")  # Write-Ahead Logging
            cursor.execute("PRAGMA synchronous=NORMAL")  # Faster commits
            cursor.execute("PRAGMA cache_size=10000")  # Larger cache
            cursor.execute("PRAGMA temp_store=memory")  # Use memory for temp tables
            cursor.execute("PRAGMA mmap_size=268435456")  # Memory-mapped I/O (256MB)
            cursor.close()


def get_query_statistics() -> Dict[str, Any]:
    """
    Get database query performance statistics
    """
    return {
        'query_stats': query_stats.copy(),
        'total_queries': sum(stats['count'] for stats in query_stats.values()),
        'avg_query_time': sum(stats['avg_time'] * stats['count'] for stats in query_stats.values()) / max(1, sum(stats['count'] for stats in query_stats.values()))
    }


def reset_query_statistics() -> None:
    """
    Reset query performance statistics
    """
    global query_stats
    query_stats.clear()


class DatabaseHealthCheck:
    """
    Database health monitoring and diagnostics
    """
    
    def __init__(self, engine: Engine):
        self.engine = engine
    
    def check_connection(self) -> bool:
        """
        Check if database connection is healthy
        """
        try:
            with self.engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            return True
        except Exception as e:
            logger.error(f"Database health check failed: {e}")
            return False
    
    def get_connection_pool_status(self) -> Dict[str, Any]:
        """
        Get connection pool status information
        """
        pool = self.engine.pool
        return {
            'pool_size': pool.size(),
            'checked_in': pool.checkedin(),
            'checked_out': pool.checkedout(),
            'overflow': pool.overflow(),
            'total_connections': pool.size() + pool.overflow()
        }
    
    def analyze_query_performance(self) -> Dict[str, Any]:
        """
        Analyze query performance and provide recommendations
        """
        stats = get_query_statistics()
        recommendations = []
        
        # Check for slow queries
        for query_type, query_stat in stats['query_stats'].items():
            if query_stat['avg_time'] > 0.1:
                recommendations.append(f"Consider optimizing {query_type} queries (avg: {query_stat['avg_time']:.3f}s)")
        
        # Check for excessive query count
        total_queries = stats['total_queries']
        if total_queries > 10000:
            recommendations.append("High query volume detected - consider implementing caching")
        
        return {
            'statistics': stats,
            'recommendations': recommendations,
            'health_score': min(100, max(0, 100 - len(recommendations) * 20))
        }


class QueryOptimizer:
    """
    Query optimization utilities and helpers
    """
    
    @staticmethod
    def suggest_indexes(slow_queries: list) -> list:
        """
        Suggest database indexes based on slow query patterns
        """
        suggestions = []
        
        for query in slow_queries:
            if 'WHERE' in query.upper():
                # Extract potential index candidates from WHERE clauses
                # This is a simplified example - real implementation would use SQL parsing
                suggestions.append("Consider adding indexes on frequently filtered columns")
        
        return suggestions
    
    @staticmethod
    def optimize_join_queries(query: str) -> str:
        """
        Suggest optimizations for JOIN queries
        """
        # Placeholder for query optimization logic
        # Real implementation would analyze JOIN patterns and suggest improvements
        return query


# Export configured engine
engine = configure_database_engine(settings.database_url)
health_checker = DatabaseHealthCheck(engine)