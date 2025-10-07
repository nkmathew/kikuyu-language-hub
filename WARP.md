# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Repository overview
- Full-stack app: FastAPI backend (Python 3.11+) and Next.js frontend (TypeScript). Infra via Docker Compose.
- Local development defaults to SQLite (no external DB). Docker stack uses PostgreSQL.

Ports and URLs
- Local (frontend dev script): http://localhost:45890
- Local (backend recommended): http://localhost:45891 (API docs at /docs, health at /api/v1/health)
- Docker Compose: web http://localhost:3000, api http://localhost:8000, db on 5432

Environment variables
- Backend .env (backend/.env):
  - DATABASE_URL defaults to sqlite:///./kikuyu_language_hub.db (local)
  - SECRET_KEY, FRONTEND_ORIGIN
- Frontend .env.local (frontend/.env.local):
  - NEXT_PUBLIC_API_URL (e.g., http://localhost:45891/api/v1 for local, http://localhost:8000/api/v1 for Docker)

Backend (FastAPI) — common commands (PowerShell on Windows)
- First-time setup
  - cd backend
  - python -m venv .venv
  - ./.venv/Scripts/Activate.ps1
  - pip install -e ".[dev]"
  - if (!(Test-Path .env)) { Copy-Item .env.example .env }
- Run dev server (SQLite by default)
  - uvicorn app.main:app --reload --host 0.0.0.0 --port 45891
- Database migrations (Alembic)
  - alembic upgrade head
  - alembic revision --autogenerate -m "<message>"
- Seed data
  - python -m app.seed
- Lint/format/type-check
  - black .
  - isort .
  - flake8 .
  - mypy .
- Tests (pytest)
  - Run all: pytest
  - Single test by pattern: pytest -k "<pattern>"
  - Single test by node id: pytest path/to/test_file.py::TestClass::test_case

Frontend (Next.js) — common commands
- First-time setup
  - cd frontend
  - npm install
  - if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }
  - Ensure NEXT_PUBLIC_API_URL matches backend URL (see Environment variables above)
- Dev server (default script port):
  - npm run dev   # serves on http://localhost:45890
- Build/start
  - npm run build
  - npm run start # default script port 45890
- Lint and type-check
  - npm run lint
  - npm run type-check

Docker Compose (full stack)
- Start services
  - docker compose -f infra/docker-compose.yml up -d
- Apply migrations and seed (inside backend container)
  - docker compose -f infra/docker-compose.yml exec api python -m alembic upgrade head
  - docker compose -f infra/docker-compose.yml exec api python -m app.seed
- Logs / status
  - docker compose -f infra/docker-compose.yml logs -f api
  - docker compose -f infra/docker-compose.yml ps

High-level architecture
- Backend layering
  - Entry: app/main.py creates FastAPI app, sets CORS, mounts routers under settings.api_v1_prefix (default /api/v1).
  - Routes: app/api/routes/* (auth, contributions, categories, sub_translations, nlp, qa, content_rating, export). Routes depend on get_db and auth dependencies.
  - Services: app/services/* encapsulate business logic (e.g., CategoryService) and handle caching invalidation and computations.
  - Schemas: app/schemas/* define Pydantic models for request/response.
  - Models: app/models/* define SQLAlchemy ORM models and relationships.
  - DB and performance: app/db/connection.py configures SQLAlchemy engine (pooling, query monitoring, SQLite PRAGMAs); app/db/session.py exposes SessionLocal and get_db.
  - Config: app/core/config.py (pydantic-settings) centralizes env, defaults to SQLite for local.
  - Security: app/core/security.py provides auth dependencies (JWT) used by routes (e.g., get_current_user, role checks).
  - Migrations: backend/alembic/*, with autogenerate workflow via Alembic.
- Frontend structure (Next.js App Router)
  - app/* contains route segments (e.g., login, signup, dashboard, contributions, moderator). Pages call backend via NEXT_PUBLIC_API_URL.
  - TypeScript and ESLint configured via tsconfig.json and Next lint config; scripts expose dev/build/lint/type-check.
- Infra
  - infra/docker-compose.yml defines db (Postgres 16), api (backend Dockerfile), and web (frontend Dockerfile). Exposes ports 5432, 8000, 3000. API container uses DATABASE_URL pointing at db.

Notes synthesized from repo docs (README.md, CLAUDE.md)
- Local development uses SQLite with zero setup; Docker orchestration uses PostgreSQL. Align NEXT_PUBLIC_API_URL and FRONTEND_ORIGIN with the ports you run.
- Black line length is 100; isort uses the black profile (see pyproject.toml). Use black/isort/flake8/mypy for code quality.
- Health check endpoint: GET /api/v1/health. OpenAPI docs at /docs on the backend.

Quick references
- Start local (separate processes):
  - Backend: cd backend; ./.venv/Scripts/Activate.ps1; uvicorn app.main:app --reload --port 45891
  - Frontend: cd frontend; npm run dev (serves on 45890); set NEXT_PUBLIC_API_URL=http://localhost:45891/api/v1
- Start full stack (Docker):
  - docker compose -f infra/docker-compose.yml up -d; then apply migrations and seed via exec commands above.
