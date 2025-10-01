from pydantic import BaseSettings, Field


class Settings(BaseSettings):
    app_name: str = "Kikuyu Language Hub API"
    api_v1_prefix: str = "/api/v1"
    secret_key: str = Field("changeme", env="SECRET_KEY")
    access_token_expires_minutes: int = 60 * 24

    # Database
    database_url: str = Field(
        "postgresql+psycopg://postgres:postgres@db:5432/kikuyu",
        env="DATABASE_URL",
    )

    # CORS
    frontend_origin: str = Field("http://localhost:3000", env="FRONTEND_ORIGIN")

    class Config:
        env_file = ".env"


settings = Settings()


