from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    app_name: str = "Kikuyu Language Hub API"
    api_v1_prefix: str = "/api/v1"
    
    # Environment
    debug: bool = Field(False, env="DEBUG")
    
    # JWT
    SECRET_KEY: str = Field("changeme", env="SECRET_KEY")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24

    # Database
    database_url: str = Field(
        "sqlite:///./kikuyu_language_hub.db",
        env="DATABASE_URL",
    )
    
    # Redis
    redis_url: str = Field("redis://localhost:6379/0", env="REDIS_URL")

    # CORS
    frontend_origin: str = Field("http://localhost:3000", env="FRONTEND_ORIGIN")

    class Config:
        env_file = ".env"


settings = Settings()


