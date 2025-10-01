# CLAUDE.md - Kikuyu Language Hub

## 1. Project Overview

The Kikuyu Language Hub is a collaborative translation contribution platform for Kikuyu-English translations. It's designed as a modern full-stack web application with Progressive Web App (PWA) capabilities.

### Main Technologies
- **Backend**: FastAPI 0.112.2 (Python 3.11+)
- **Frontend**: Next.js 14.2.6 with TypeScript 5.5.4
- **Database**: PostgreSQL 16+ with SQLAlchemy 2.0.34
- **Authentication**: JWT tokens with role-based access control
- **Deployment**: Docker Compose with multi-service orchestration

### Target Environment
- Web browsers (desktop/mobile)
- Progressive Web App for mobile installation
- API backend for potential native mobile apps

### Key Dependencies
- **FastAPI**: High-performance Python web framework
- **SQLAlchemy**: Python ORM for database operations
- **Alembic**: Database migration management
- **Pydantic**: Data validation and settings management
- **Next.js**: React framework with App Router
- **JWT**: Authentication tokens
- **bcrypt**: Password hashing

## 2. Architecture & Structure

### High-Level Architecture
```
Frontend (Next.js) ←→ Backend API (FastAPI) ←→ Database (PostgreSQL)
        ↓                      ↓                      ↓
    PWA Manifest          JWT Auth + RBAC        Alembic Migrations
```

### Directory Structure
```
kikuyu-language-hub/
├── backend/                    # FastAPI backend
│   ├── app/
│   │   ├── api/routes/        # HTTP endpoints (auth, contributions, export)
│   │   ├── core/              # Configuration and security
│   │   ├── db/                # Database session and base
│   │   ├── models/            # SQLAlchemy ORM models
│   │   ├── schemas/           # Pydantic validation schemas
│   │   ├── services/          # Business logic layer
│   │   ├── main.py            # FastAPI app factory
│   │   └── seed.py            # Database seeding
│   ├── alembic/               # Database migrations
│   └── pyproject.toml         # Python dependencies
├── frontend/                  # Next.js frontend
│   ├── app/                   # App Router pages
│   ├── lib/                   # API client and utilities
│   └── public/                # Static assets + PWA manifest
├── infra/                     # Docker Compose
└── docs/                      # Documentation
```

### File Patterns
- **Models**: `models/{entity}.py` (User, Contribution, AuditLog)
- **Schemas**: `schemas/{entity}.py` (Pydantic models)
- **Services**: `services/{entity}_service.py` (business logic)
- **Routes**: `api/routes/{feature}.py` (API endpoints)
- **Migrations**: `alembic/versions/{hash}_{description}.py`

### Module Organization
- **Layered architecture**: API → Services → Models → Database
- **Dependency injection**: Security, database sessions
- **Separation of concerns**: Validation, business logic, data access

## 3. Development Commands

### Backend Commands
```bash
cd backend

# Environment setup
python -m venv .venv
source .venv/bin/activate  # or .venv\Scripts\Activate.ps1 on Windows
pip install -e ".[dev]"

# Development server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Database operations
alembic upgrade head                    # Apply migrations
alembic revision --autogenerate -m ""  # Create migration
python -m app.seed                     # Seed database

# Code quality
black .                                 # Format code
isort .                                 # Sort imports  
flake8 .                               # Lint code
mypy .                                 # Type checking
pytest                                 # Run tests
```

### Frontend Commands
```bash
cd frontend

# Development
npm install                            # Install dependencies
npm run dev                           # Development server (port 3000)

# Code quality
npm run lint                          # ESLint
npm run type-check                    # TypeScript checking
npm test                              # Run tests

# Production
npm run build                         # Build for production
npm start                             # Start production server
```

### Docker Commands
```bash
# Full stack development
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml exec backend python -m alembic upgrade head
docker compose -f infra/docker-compose.yml exec backend python -m app.seed

# Individual services
docker compose -f infra/docker-compose.yml up backend -d
docker compose -f infra/docker-compose.yml logs -f frontend
```

## 4. Coding Standards & Conventions

### Python (Backend)
- **Line length**: 100 characters (configured in pyproject.toml)
- **Formatting**: Black with isort (black profile)
- **Type hints**: Required for all functions and class methods
- **Imports**: Absolute imports, isort with black profile
- **Naming**: snake_case for variables/functions, PascalCase for classes

### Example Backend Patterns
```python
# Service pattern
class ContributionService:
    @staticmethod
    def create_contribution(db: Session, data: ContributionCreate, user: User) -> Contribution:
        # Implementation
        pass

# Route pattern
@router.post("/", response_model=ContributionResponse)
def create_contribution(
    data: ContributionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return ContributionService.create_contribution(db, data, current_user)

# Model pattern
class Contribution(Base):
    __tablename__ = "contributions"
    
    id = Column(Integer, primary_key=True, index=True)
    # ... fields
```

### TypeScript (Frontend)
- **Components**: PascalCase, functional components preferred
- **Files**: kebab-case for files, PascalCase for components
- **Imports**: Absolute imports from project root
- **Types**: Interfaces for object shapes, types for unions

### Error Handling
- **Backend**: HTTPException with appropriate status codes
- **Frontend**: Try-catch with user-friendly error messages
- **Database**: Let SQLAlchemy handle constraint violations

## 5. Development Workflow

### Git Workflow
- **Main branch**: `main` (production-ready code)
- **Feature branches**: `feature/{description}` or `{username}/{feature}`
- **Bug fixes**: `fix/{description}`
- **Documentation**: `docs/{description}`

### Commit Message Format
```
type(scope): description

- feat: new feature
- fix: bug fix
- docs: documentation
- style: formatting
- refactor: code restructuring
- test: adding tests
- chore: maintenance
```

### Code Review Guidelines
- All changes require PR review
- Ensure tests pass and linting is clean
- Check that migrations are included for model changes
- Verify API documentation is updated
- Test authentication/authorization changes thoroughly

## 6. Configuration & Environment

### Backend Environment Variables (.env)
```env
DATABASE_URL=postgresql+psycopg://postgres:postgres@localhost:5432/kikuyu_language_hub
SECRET_KEY=your-super-secret-jwt-key
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=1440
FRONTEND_ORIGIN=http://localhost:3000
```

### Frontend Environment Variables (.env.local)
```env
NEXT_PUBLIC_API_URL=http://localhost:45891/api/v1
NEXT_PUBLIC_APP_NAME="Kikuyu Language Hub"
```

### Database Setup
1. Install PostgreSQL 16+
2. Create database: `CREATE DATABASE kikuyu_language_hub;`
3. Run migrations: `alembic upgrade head`
4. Seed data: `python -m app.seed`

### Default Test Accounts (after seeding)
- **Admin**: admin@kikuyu.hub / admin123
- **Moderator**: moderator@kikuyu.hub / mod123
- **Contributor**: contributor@kikuyu.hub / contrib123

## 7. Common Tasks & Patterns

### Adding a New API Endpoint
1. Define Pydantic schema in `schemas/`
2. Add business logic to `services/`
3. Create route handler in `api/routes/`
4. Add route to `main.py`
5. Test with appropriate authentication

### Adding a New Database Model
1. Create model in `models/{entity}.py`
2. Import in `db/base.py`
3. Generate migration: `alembic revision --autogenerate -m "Add {entity}"`
4. Review and apply migration: `alembic upgrade head`

### Authentication Pattern
```python
# Require authentication
current_user: User = Depends(get_current_user)

# Require specific role
moderator: User = Depends(require_moderator_or_admin)

# Custom role check
admin: User = Depends(require_role([UserRole.ADMIN]))
```

### Data Fetching (Frontend)
```typescript
// API client pattern (implement in lib/api/)
import { apiClient } from '@/lib/api/client'

const contributions = await apiClient.get('/contributions')
```

### Adding Frontend Pages
1. Create page in `app/{route}/page.tsx`
2. Add layout if needed: `app/{route}/layout.tsx`
3. Implement authentication guards if required
4. Add to navigation if applicable

## 8. File Access Guidelines

### Safe to Modify
- `app/` directories (both backend and frontend)
- `docs/` documentation files
- Environment example files
- Docker Compose configuration
- README and project documentation

### Require Caution
- `alembic/env.py` (migration configuration)
- `pyproject.toml` dependencies
- Database migration files (once applied)
- Docker build configurations

### Do Not Modify
- `alembic/versions/*` migration files (after they've been applied)
- `.venv/` and `node_modules/` directories
- Generated files and build outputs

### Generated Files to Ignore
- `__pycache__/` Python cache
- `.next/` Next.js build cache
- `node_modules/` dependencies
- `.mypy_cache/` type checking cache
- Database files if running locally

## 9. Known Issues & Gotchas

### Database Migration Issues
- Always review auto-generated migrations before applying
- Enum changes require special handling in PostgreSQL
- Foreign key constraints may require data migration

### Authentication Gotchas
- JWT tokens expire after 24 hours by default
- Role changes require new token (logout/login)
- CORS must include credentials for auth headers

### Development Environment
- PostgreSQL must be running before starting backend
- Database connection URL format is specific to psycopg3
- Windows may require binary psycopg installation

### Performance Considerations
- Export endpoint loads all approved translations (consider pagination for large datasets)
- No query optimization implemented yet
- Database connections use basic pooling

## 10. Quick Reference

### Most Frequently Used Commands
```bash
# Start development environment
docker compose -f infra/docker-compose.yml up -d

# Backend development
cd backend && uvicorn app.main:app --reload --host 0.0.0.0 --port 45891

# Frontend development  
cd frontend && npm run dev

# Database reset
cd backend && alembic downgrade base && alembic upgrade head && python -m app.seed

# Code quality check
cd backend && black . && isort . && flake8 . && mypy .
```

### Key File Locations
- **API Routes**: `backend/app/api/routes/`
- **Database Models**: `backend/app/models/`
- **Business Logic**: `backend/app/services/`
- **Frontend Pages**: `frontend/app/`
- **Configuration**: `backend/app/core/config.py`
- **Database Config**: `backend/app/db/`

### Important URLs (Development)
- **Frontend**: http://localhost:45890
- **Backend API**: http://localhost:45891
- **API Documentation**: http://localhost:45891/docs
- **Health Check**: http://localhost:45891/api/v1/health
- **Export Endpoint**: http://localhost:45891/api/v1/export/translations.json

### Useful Debugging Commands
```bash
# Backend logs
docker compose -f infra/docker-compose.yml logs -f backend

# Database connection test
cd backend && python -c "from app.db.session import engine; print(engine.execute('SELECT 1').scalar())"

# Frontend build issues
cd frontend && npm run build --verbose

# Check running services
docker compose -f infra/docker-compose.yml ps
```

### Role-Based API Access
- **Public**: `/api/v1/export/translations.json`, `/api/v1/auth/*`
- **Authenticated**: `/api/v1/contributions/` (filtered by role)
- **Moderator+**: `/api/v1/contributions/{id}/approve|reject`
- **Admin**: All endpoints

### Database Schema Quick Reference
- **Users**: id, email, password_hash, role, timestamps
- **Contributions**: id, source_text, target_text, status, language, created_by_id, timestamps
- **Audit Logs**: id, contribution_id, action, moderator_id, reason, created_at