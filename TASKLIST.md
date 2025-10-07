### MVP Implementation Tasklist (Translation Contribution Platform)

- [ ] Project scaffolding
  - [ ] Initialize mono-repo structure: `backend/`, `frontend/`, `infra/`, `docs/`
  - [ ] Add top-level `README.md`, `.editorconfig`, `.gitignore`, `LICENSE`
  - [ ] Create shared `.env.example` files and environment strategy

- [ ] Backend: FastAPI foundation
  - [ ] Scaffold FastAPI app in `backend/app/`
    - [ ] `main.py` with versioned router `/api/v1`
    - [ ] `core/config.py` for settings (Pydantic `BaseSettings`)
    - [ ] `core/security.py` for JWT utilities
    - [ ] `db/session.py` SQLAlchemy engine/session + async support decision
    - [ ] `db/base.py` import models for Alembic
  - [ ] Dependencies and packaging
    - [ ] `pyproject.toml` with FastAPI, Uvicorn, SQLAlchemy, Alembic, Psycopg, Passlib, PyJWT/Python-JOSE
    - [ ] `alembic/` initialized with `env.py` targeting `db.base:Base`
    - [ ] `pre-commit` hooks (black, isort, flake8, mypy)

- [ ] Database schema and migrations
  - [ ] Models in `backend/app/models/`
    - [ ] `user.py`: `id`, `email`, `password_hash`, `role` enum, timestamps
    - [ ] `contribution.py`: `id`, `source_text`, `target_text`, `status` enum, `language`, `created_by_id`, timestamps
    - [ ] `audit_log.py`: `id`, `contribution_id`, `action` enum (approve/reject), `moderator_id`, `reason`, timestamps
  - [ ] Create initial Alembic migration
  - [ ] Seed script in `backend/app/seed.py` (admin/moderator/contributor + sample contributions)

- [ ] Auth: JWT + roles
  - [ ] `schemas/auth.py`: login/signup DTOs, tokens
  - [ ] `services/auth_service.py`: signup, hash verify, create access token
  - [ ] `api/routes/auth.py`: `/signup`, `/login`, `/me`
  - [ ] Role-based dependency: `require_role(roles: list[str])`

- [ ] Contribution workflow API
  - [ ] `schemas/contribution.py`: create/read/update models
  - [ ] `services/contribution_service.py`: submit, list, approve, reject
  - [ ] `api/routes/contributions.py`:
    - [ ] POST `/contributions` (Contributor)
    - [ ] GET `/contributions?status=pending|approved|rejected` (role-filtered)
    - [ ] POST `/contributions/{id}/approve` (Moderator/Admin)
    - [ ] POST `/contributions/{id}/reject` with reason (Moderator/Admin)
  - [ ] `services/audit_service.py`: write audit entries

- [ ] Export endpoint
  - [ ] `api/routes/export.py`: GET `/export/translations.json` (approved only)
  - [ ] Transform to Android-friendly JSON shape (key → translation or array)
  - [ ] Add ETag/Cache-Control headers

- [ ] Backend quality gates
  - [ ] Unit tests: services and auth deps (`pytest`)
  - [ ] Integration tests: API routes (`httpx`/`pytest-asyncio` if async)
  - [ ] Static checks: mypy, flake8, black, isort configuration
  - [ ] Minimal rate-limiting or brute-force guard on `/login`

- [ ] Frontend: Next.js foundation (TypeScript)
  - [ ] Scaffold `frontend/` with Next.js App Router
    - [ ] `app/` routes: `login`, `signup`, `dashboard`, `moderator`, `contributions`
    - [ ] `components/` (forms, lists, layout)
    - [ ] `lib/api/` for fetch client and typed endpoints
    - [ ] `lib/auth/` for token storage/refresh, role guards
  - [ ] UI framework and styling
    - [ ] Tailwind CSS or CSS Modules configured
    - [ ] Mobile-first layout + accessible components
  - [ ] PWA
    - [ ] `manifest.json`, service worker, icons
    - [ ] Install prompt and offline shell for core screens

- [ ] Frontend data layer (reusable for React Native)
  - [ ] `lib/api/client.ts` with base URL, auth header injector, error handling
  - [ ] Hooks:
    - [ ] `useAuth()` for login/logout/me and token state
    - [ ] `useSubmitContribution()`
    - [ ] `useContributions({ status })`
    - [ ] `useModerationQueue()`, `useApprove()`, `useReject()`
  - [ ] Types mirrored from backend `schemas` (generate via OpenAPI or hand-maintained)

- [ ] Frontend screens
  - [ ] Auth: `login`, `signup`, protected route guard
  - [ ] Contributor:
    - [ ] Submit contribution form (validation)
    - [ ] My submissions list (pending/approved/rejected)
  - [ ] Moderator/Admin:
    - [ ] Review queue with approve/reject + reason
    - [ ] Audit log view (basic)
  - [ ] Toasts/loading/empty states/error boundaries

- [ ] Example JSON export
  - [ ] Seed approved translations
  - [ ] Verify `/api/v1/export/translations.json` response matches spec
  - [ ] Store example output in `docs/examples/translations.json`

- [ ] Docker and local dev
  - [ ] `infra/docker-compose.yml` with services:
    - [ ] `db` (PostgreSQL + volume)
    - [ ] `backend` (Uvicorn) with env and DB dependency
    - [ ] `frontend` (Next.js dev or production) with API URL env
  - [ ] `backend/Dockerfile` (multi-stage, non-root, gunicorn/uvicorn worker)
  - [ ] `frontend/Dockerfile` (multi-stage with `next build`/`next start`)
  - [ ] Health checks for services
  - [ ] Makefile or `pwsh` scripts for common tasks (migrate, seed)

- [ ] Security and ops
  - [ ] CORS config locked to frontend origin
  - [ ] Secure cookie or `Authorization` header strategy; XSS/CSRF considerations
  - [ ] Production-ready logging and error handling (structured logs)
  - [ ] Basic request size/time limits
  - [ ] `.env.example` for backend and frontend; do not commit secrets

- [ ] CI (optional but recommended for MVP)
  - [ ] Lint/test backend workflow
  - [ ] Lint/build frontend workflow
  - [ ] Docker build for both images
  - [ ] Simple smoke test (curl export endpoint)

- [ ] Documentation
  - [ ] `docs/ARCHITECTURE.md` with backend/fe layering and future hooks
  - [ ] `docs/API.md` (Auth, Contribution, Export endpoints)
  - [ ] `docs/LOCAL_DEV.md` with Docker Compose instructions
  - [ ] `docs/SEEDING.md` and example user roles

- [ ] Future-proofing hooks (design only, no implementation)
  - [ ] Reserve `services/reputation_service.py` interface
  - [ ] Reserve `services/discussion_service.py` interfaces
  - [ ] Add placeholders/types for media fields (not used yet)
  - [ ] Plan offline caching layer in data hooks (commented stubs)
  - [ ] Notification interface abstraction (no provider yet)

### Definition of Done
- [ ] Backend API supports signup/login, contribution submit/list, approve/reject, and JSON export of approved items.
- [ ] Frontend provides auth, submission form, lists, and moderator dashboard; mobile-first and installable PWA.
- [ ] Running `docker compose up` brings up DB, backend, frontend; migrations and seed steps documented and reproducible.
- [ ] Example JSON export file matches live `/export/translations.json`.
- [ ] Code passes linters/tests; clear docs for local dev.

