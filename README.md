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
# Frontend: http://localhost:3000
# Backend API: http://localhost:8000
# API Docs: http://localhost:8000/docs
```

## Development Setup (Windows/Local)

### Prerequisites
- Python 3.11+ (`python --version`)
- Node.js 20+ (`node --version`) 
- PostgreSQL 16+ (see installation instructions below)
- PowerShell 7 (`pwsh`) - recommended

### 0. PostgreSQL Installation

#### Windows
**Option 1: Official PostgreSQL Installer (Recommended)**
1. Download from [postgresql.org/download/windows](https://www.postgresql.org/download/windows/)
2. Run the installer and follow the setup wizard
3. During installation:
   - Remember the password you set for the `postgres` user
   - Default port 5432 is fine
   - Install pgAdmin (optional but helpful for GUI management)
4. Add PostgreSQL to PATH:
   - Add `C:\Program Files\PostgreSQL\16\bin` to your system PATH
   - Test with: `psql --version`

**Option 2: Using Chocolatey**
```powershell
# Install Chocolatey if not already installed
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install PostgreSQL
choco install postgresql --params '/Password:postgres'
```

**Option 3: Using Windows Subsystem for Linux (WSL)**
```bash
# In WSL Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL service
sudo service postgresql start

# Set password for postgres user
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
```

#### macOS
**Option 1: Using Homebrew (Recommended)**
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install PostgreSQL
brew install postgresql@16

# Start PostgreSQL service
brew services start postgresql@16

# Create postgres user with password
createuser -s postgres
psql postgres -c "ALTER USER postgres PASSWORD 'postgres';"
```

**Option 2: Postgres.app**
1. Download from [postgresapp.com](https://postgresapp.com/)
2. Install and start the app
3. Initialize a new server (PostgreSQL 16)
4. Add to PATH: Add `/Applications/Postgres.app/Contents/Versions/16/bin` to your PATH

#### Linux (Ubuntu/Debian)
```bash
# Update package index
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start and enable PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Set password for postgres user
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"

# Configure PostgreSQL to accept local connections (if needed)
sudo nano /etc/postgresql/16/main/pg_hba.conf
# Change 'peer' to 'md5' for local connections
sudo systemctl restart postgresql
```

#### Linux (RHEL/CentOS/Fedora)
```bash
# Install PostgreSQL
sudo dnf install postgresql postgresql-server  # Fedora
# OR
sudo yum install postgresql postgresql-server  # RHEL/CentOS

# Initialize database
sudo postgresql-setup initdb

# Start and enable service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Set postgres user password
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
```

#### Docker Alternative (Any Platform)
If you prefer to use Docker for PostgreSQL only:
```bash
# Run PostgreSQL in Docker
docker run --name kikuyu-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=kikuyu_language_hub \
  -p 5432:5432 \
  -v kikuyu_postgres_data:/var/lib/postgresql/data \
  -d postgres:16-alpine

# Verify it's running
docker ps
```

#### Verify Installation
Test your PostgreSQL installation:
```bash
# Test connection
psql -U postgres -h localhost -c "SELECT version();"

# You should see PostgreSQL version information
```

### 1. Database Setup

Create the database and configure user:

```powershell
# Create database
psql -U postgres -h localhost -c "CREATE DATABASE kikuyu_language_hub;" 
psql -U postgres -h localhost -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

**Troubleshooting Database Connection:**
- If `psql` command not found: Add PostgreSQL bin directory to your PATH
- If connection refused: Ensure PostgreSQL service is running
- If authentication failed: Check if you're using the correct password
- On Windows: You may need to use `psql -U postgres` and enter password when prompted

### 2. Backend Setup (FastAPI)

```powershell
cd backend

# Create virtual environment (recommended)
python -m venv .venv
./.venv/Scripts/Activate.ps1

# Install dependencies
pip install -e ".[dev]"

# Create environment file
if (!(Test-Path .env)) { Copy-Item .env.example .env }

# Update .env file with your database credentials
# DATABASE_URL=postgresql+psycopg://postgres:postgres@localhost:5432/kikuyu_language_hub
# SECRET_KEY=your-secret-key-here
# FRONTEND_ORIGIN=http://localhost:3000

# Run database migrations
alembic upgrade head

# Seed the database with sample data
python -m app.seed

# Start the development server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The backend will be available at:
- API: http://localhost:8000
- Interactive Docs: http://localhost:8000/docs
- Health Check: http://localhost:8000/api/v1/health

### 3. Frontend Setup (Next.js)

```powershell
cd frontend

# Install dependencies
npm install

# Create environment file
if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }

# Update .env.local if needed
# NEXT_PUBLIC_API_URL=http://localhost:8000/api/v1

# Start the development server
npm run dev
```

The frontend will be available at http://localhost:3000

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
- Ensure PostgreSQL is running
- Check database URL in `.env` file
- Verify database exists and user has permissions

**CORS Errors**
- Check `FRONTEND_ORIGIN` in backend `.env`
- Ensure frontend URL matches CORS configuration

**Port Conflicts**
- Backend default: 8000
- Frontend default: 3000
- Database default: 5432
- Change ports in configuration if needed

**Windows-Specific Issues**
- Use `psycopg[binary]` to avoid build issues
- Ensure PowerShell execution policy allows scripts
- Add PostgreSQL bin directory to PATH

### Logs and Debugging

```powershell
# Backend logs
cd backend
uvicorn app.main:app --reload --log-level debug

# Database logs
# Check PostgreSQL logs in data directory

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


