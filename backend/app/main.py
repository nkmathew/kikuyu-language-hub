from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .core.config import settings
from .api.routes import auth_router, contributions_router, export_router, analytics_router
from .api.routes.categories import router as categories_router
from .api.routes.sub_translations import router as sub_translations_router
from .api.routes.nlp import router as nlp_router
from .api.routes.qa import router as qa_router
from .api.routes.content_rating import router as content_rating_router
from .api.routes.morphology import router as morphology_router
from .api.routes.webhooks import router as webhooks_router


def create_app() -> FastAPI:
    app = FastAPI(title="Kikuyu Language Hub API", version="0.1.0")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=[
            settings.frontend_origin,
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:10001",
            "http://localhost:45890",
        ],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Include API routes
    app.include_router(auth_router, prefix=settings.api_v1_prefix)
    app.include_router(contributions_router, prefix=settings.api_v1_prefix)
    app.include_router(categories_router, prefix=settings.api_v1_prefix)
    app.include_router(sub_translations_router, prefix=settings.api_v1_prefix)
    app.include_router(nlp_router, prefix=settings.api_v1_prefix)
    app.include_router(qa_router, prefix=settings.api_v1_prefix)
    app.include_router(content_rating_router, prefix=settings.api_v1_prefix)
    app.include_router(morphology_router, prefix=settings.api_v1_prefix)
    app.include_router(webhooks_router, prefix=settings.api_v1_prefix)
    app.include_router(analytics_router, prefix=settings.api_v1_prefix)
    app.include_router(export_router, prefix=settings.api_v1_prefix)

    @app.get(f"{settings.api_v1_prefix}/health")
    def health() -> dict:
        return {"status": "ok"}

    return app


app = create_app()


