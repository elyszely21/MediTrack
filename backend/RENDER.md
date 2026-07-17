# MediTrack Backend (Render) deployment

This backend is configured to run on Render using the existing `backend/Dockerfile`.

## 1) Render service type
Create a **Web Service (Docker)** and build from this repo using the `backend/` folder.

- The app listens on `PORT` injected by Render.
- Health endpoint: `GET /api/health`

## 2) Required environment variables
### Database
- `DATABASE_URL`
  - Provided by Render Postgres when you attach it to the service.

### JWT
- `JWT_SECRET`
- `JWT_EXPIRATION_MS` (optional; defaults to `86400000`)

### Admin seeding (optional but recommended)
Used by `AdminSeeder` to create the initial `SUPER_ADMIN` user (only if none exists yet):
- `ADMIN_EMAIL` (optional)
- `ADMIN_PASSWORD` (optional)
- `ADMIN_FULL_NAME` (optional)

### CORS
- `CORS_ALLOWED_ORIGINS` (comma-separated)
  - Example: `https://your-frontend.onrender.com,http://localhost:5173`

### Schema / migrations
- `JPA_DDL_AUTO` (optional)
  - Current default is `update`.
  - Example: `validate` or `none` for production hardening.

## 3) Smoke test after deploy
Once Render shows the service as running:
- `curl <render-url>/api/health`

Example:
- Response: `{ "status": "UP" }`

## 4) Notes
- Avoid relying on fallback defaults for production secrets. Set `JWT_SECRET` and `ADMIN_PASSWORD` explicitly in Render.

