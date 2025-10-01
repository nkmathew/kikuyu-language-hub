from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .core.config import settings
from .api.routes import auth_router, contributions_router, export_router


def create_app() -> FastAPI:
    app = FastAPI(title="Kikuyu Language Hub API", version="0.1.0")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=[
            settings.frontend_origin,
            "http://localhost:3000",
            "http://localhost:10001",
        ],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Include API routes
    app.include_router(auth_router, prefix=settings.api_v1_prefix)
    app.include_router(contributions_router, prefix=settings.api_v1_prefix)
    app.include_router(export_router, prefix=settings.api_v1_prefix)

    @app.get(f"{settings.api_v1_prefix}/health")
    def health() -> dict:
        return {"status": "ok"}

    return app


app = create_app()


