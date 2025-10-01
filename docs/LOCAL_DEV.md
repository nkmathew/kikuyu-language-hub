## Local Development

Prerequisites: Docker Desktop.

1. Copy envs:
   - `cp backend/.env.example backend/.env`
   - `cp frontend/.env.example frontend/.env.local`
2. Start services:
   - `cd infra && docker compose up --build`
3. Services:
   - API: http://localhost:8000/api/v1/health
   - Web: http://localhost:3000
   - DB: localhost:5432 (postgres/postgres)

Migrations and seeding will be added in the next step.


