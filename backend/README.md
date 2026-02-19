# Backend + Supabase Setup

This backend is configured to use Supabase Postgres via Spring datasource settings.

## 1) Create DB table in Supabase

Run `backend/supabase/schema.sql` in the Supabase SQL Editor.

## 2) Set environment variables

Use `backend/.env.example` as reference:

- `SUPABASE_DB_URL`
- `SUPABASE_DB_USER`
- `SUPABASE_DB_PASSWORD`

Example URL format:

`jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require`

## 3) Start backend with Supabase profile

From `backend/`:

```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USER="postgres"
$env:SUPABASE_DB_PASSWORD="<YOUR_DB_PASSWORD>"
./mvnw spring-boot:run "-Dspring-boot.run.profiles=supabase"
```

## 4) Verify connection

Open:

`http://localhost:8080/api/health/db`

Expected response includes:

- `"status": "ok"`
- `"database": "supabase-postgres"`
- `"dbTime": "<timestamp from database>"`

## 5) Use centralized attendee APIs

- `GET /api/attendees` list all attendees and scan status
- `POST /api/attendees` add one attendee
- `POST /api/attendees/import` bulk import attendee names
- `POST /api/attendees/scan` mark entry/food/gift scan by name
