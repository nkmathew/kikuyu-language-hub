# Kikuyu Language Hub

A translation contribution platform for collaborative Kikuyu-English translation work. Built with FastAPI backend, Next.js frontend, and PostgreSQL database.

## Features

- **User Authentication**: JWT-based auth with role-based access (Admin/Moderator/Contributor)
- **Translation Contribution**: Submit and manage translation contributions
- **Moderation Workflow**: Approve/reject translations with audit logging
- **Export API**: JSON export of approved translations for mobile apps
- **PWA Support**: Progressive Web App capabilities for mobile use

## Quick Start with Docker

```bash
# Clone the repository
git clone <repo-url>
cd kikuyu-language-hub

# Start all services with Docker Compose
docker compose -f infra/docker-compose.yml up -d

# Run database migrations and seed data
docker compose -f infra/docker-compose.yml exec backend python -m alembic upgrade head
docker compose -f infra/docker-compose.yml exec backend python -m app.seed

# Access the application
# Frontend: http://localhost:10001
# Backend API: http://localhost:10000
# API Docs: http://localhost:10000/docs
```

## Development Setup (Windows/Local)

### Prerequisites
- Python 3.11+ (`python --version`)
- Node.js 20+ (`node --version`) 
- PowerShell 7 (`pwsh`) - recommended

**Note**: This project now uses SQLite for the database, so no separate database installation is required! 🎉

### 1. Backend Setup (FastAPI)


```powershell
cd backend

# Create virtual environment (recommended)
python -m venv .venv
./.venv/Scripts/Activate.ps1

# Install dependencies
pip install -e ".[dev]"

# Create environment file
if (!(Test-Path .env)) { Copy-Item .env.example .env }

# Update .env file if needed (SQLite works out of the box!)
# DATABASE_URL=sqlite:///./kikuyu_language_hub.db
# SECRET_KEY=your-secret-key-here
# FRONTEND_ORIGIN=http://localhost:3000

# Run database migrations (creates SQLite database automatically)
alembic upgrade head

# Seed the database with sample data
python -m app.seed

# Start the development server
uvicorn app.main:app --reload --host 0.0.0.0 --port 10000
```

The backend will be available at:
- API: http://localhost:10000
- Interactive Docs: http://localhost:10000/docs
- Health Check: http://localhost:10000/api/v1/health

### 3. Frontend Setup (Next.js)

```powershell
cd frontend

# Install dependencies
npm install

# Create environment file
if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }

# Update .env.local if needed
# NEXT_PUBLIC_API_URL=http://localhost:10000/api/v1

# Start the development server (on port 10001)
npm run dev -- --port 10001
```

The frontend will be available at http://localhost:10001

### 4. Default Login Credentials

After running the seed script, you can login with these test accounts:

- **Admin**: `admin@kikuyu.hub` / `admin123`
- **Moderator**: `moderator@kikuyu.hub` / `mod123`  
- **Contributor**: `contributor@kikuyu.hub` / `contrib123`

## Development Workflow

### Backend Development

```powershell
cd backend

# Run tests
pytest

# Code formatting
black .
isort .

# Type checking
mypy .

# Linting
flake8 .

# Create new migration
alembic revision --autogenerate -m "Description of changes"

# Apply migrations
alembic upgrade head
```

### Frontend Development

```powershell
cd frontend

# Run tests
npm test

# Type checking
npm run type-check

# Linting
npm run lint

# Build for production
npm run build
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/signup` - User registration
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Get current user info

### Contributions
- `POST /api/v1/contributions/` - Submit translation
- `GET /api/v1/contributions/` - List contributions (filtered by role)
- `POST /api/v1/contributions/{id}/approve` - Approve translation (Moderator+)
- `POST /api/v1/contributions/{id}/reject` - Reject translation (Moderator+)

### Export
- `GET /api/v1/export/translations.json` - Export approved translations

## Project Structure

```
kikuyu-language-hub/
├── backend/                 # FastAPI backend
│   ├── app/
│   │   ├── api/            # API routes
│   │   ├── core/           # Configuration and security
│   │   ├── db/             # Database configuration
│   │   ├── models/         # SQLAlchemy models
│   │   ├── schemas/        # Pydantic schemas
│   │   ├── services/       # Business logic
│   │   └── main.py         # FastAPI application
│   ├── alembic/            # Database migrations
│   └── pyproject.toml      # Python dependencies
├── frontend/               # Next.js frontend
│   ├── app/               # App Router pages
│   ├── components/        # React components
│   ├── lib/              # Utilities and API client
│   └── package.json      # Node.js dependencies
├── infra/                # Infrastructure
│   └── docker-compose.yml # Docker Compose configuration
└── docs/                 # Documentation
```

## Troubleshooting

### Common Issues

**Database Connection Errors**
- SQLite database is created automatically when running migrations
- Check database URL in `.env` file (should be `sqlite:///./kikuyu_language_hub.db`)
- Ensure the backend directory is writable for SQLite file creation

**CORS Errors**
- Check `FRONTEND_ORIGIN` in backend `.env`
- Ensure frontend URL matches CORS configuration

**Port Conflicts**
- Backend default: 10000
- Frontend default: 10001
- Change ports in configuration if needed

**Windows-Specific Issues**
- Ensure PowerShell execution policy allows scripts
- SQLite requires no additional setup or PATH configuration

### Logs and Debugging

```powershell
# Backend logs
cd backend
uvicorn app.main:app --reload --log-level debug

# Database logs
# SQLite logs are minimal - check backend logs for database errors

# Frontend logs
cd frontend
npm run dev
# Check browser console for client-side errors
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and linting
5. Submit a pull request

## License

[Add your license information here]

---

### Project Layout
- `backend/`: FastAPI app, SQLAlchemy, config, DB session
- `frontend/`: Next.js app (App Router), PWA manifest, basic API client
- `infra/`: Docker Compose (db, api, web)
- `docs/`: Architecture and local development docs


