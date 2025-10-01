### Kikuyu Language Hub

Production-ready scaffold for a translation contribution platform.

This README includes Windows (no Docker) setup instructions. For Docker Compose, see `docs/LOCAL_DEV.md`.

---

## Prerequisites (Windows without Docker)
- PowerShell 7 (`pwsh`)
- Python 3.11+ (`python --version`)
- Node.js 20+ (`node --version`)
- PostgreSQL 16+ (ensure `psql` in PATH)

## 1) Database setup (PostgreSQL)
Create a database and user that match the backend defaults.

```powershell
psql -U postgres -h localhost -c "CREATE DATABASE kikuyu;" 
psql -U postgres -h localhost -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

Note: If `psql` prompts for a password, it is your local Postgres superuser password.

## 2) Backend (FastAPI) – run locally
From `backend/`:

```powershell
cd backend
Copy-Item .env.example .env -Force

# (Optional) create a venv
python -m venv .venv
./.venv/Scripts/Activate.ps1

# Install dependencies (no Docker)
pip install --upgrade pip
pip install fastapi==0.112.2 uvicorn[standard]==0.30.6 SQLAlchemy==2.0.34 `
  psycopg[binary]==3.2.1 pydantic==2.9.2 python-jose==3.3.0 passlib[bcrypt]==1.7.4

# Dev tools (optional)
pip install alembic==1.13.2 black==24.8.0 isort==5.13.2 flake8==7.1.1 mypy==1.11.2 pytest==8.3.2 httpx==0.27.2

# Run API on http://localhost:8000
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

If your Postgres is not on default localhost:5432 or uses different credentials, edit `backend/.env`:

```env
DATABASE_URL=postgresql+psycopg://<user>:<password>@<host>:<port>/<db>
FRONTEND_ORIGIN=http://localhost:3000
SECRET_KEY=replace-me
```

## 3) Frontend (Next.js) – run locally
From `frontend/`:

```powershell
cd frontend
Copy-Item .env.example .env.local -Force

# Ensure the API URL points to your backend
# .env.local
# NEXT_PUBLIC_API_URL=http://localhost:8000/api/v1

npm install
npm run dev  # http://localhost:3000
```

## 4) CORS and connectivity
- Backend allows CORS from `http://localhost:3000` by default.
- Frontend reads `NEXT_PUBLIC_API_URL` for API base.

## 5) Common Windows issues
- psycopg build errors: use the `psycopg[binary]` extra as listed above.
- `psql` not found: add PostgreSQL `bin` directory to PATH or use PgAdmin to run SQL.
- Port already in use: stop any process on 3000/8000 or change ports.

## 6) Without Docker – how to run everything
- Start Postgres service (Windows Service Manager or manually).
- Start backend with Uvicorn in `backend/`.
- Start frontend with `npm run dev` in `frontend/`.

## 7) With Docker (optional)
Use `infra/docker-compose.yml` when you have Docker Desktop installed. See `docs/LOCAL_DEV.md`.

---

### Project Layout
- `backend/`: FastAPI app, SQLAlchemy, config, DB session
- `frontend/`: Next.js app (App Router), PWA manifest, basic API client
- `infra/`: Docker Compose (db, api, web)
- `docs/`: Architecture and local development docs


