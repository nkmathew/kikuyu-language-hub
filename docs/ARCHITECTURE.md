## Architecture Overview

- Backend (`backend/`): FastAPI, SQLAlchemy, PostgreSQL. Exposes REST at `/api/v1`.
- Frontend (`frontend/`): Next.js (App Router, TS). Consumes API via `lib/api`.
- Infra (`infra/`): Docker Compose orchestrates `db`, `api`, `web`.

Separation of concerns:
- `app/services/` holds domain logic (auth, contributions, moderation).
- `app/api/` holds thin routers that call services.
- Frontend `lib/api/` exports a fetch wrapper and hooks (later) reusable by RN.

Future hooks are reserved via placeholders (reputation, discussion, media fields).


