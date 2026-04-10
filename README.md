# food-app-backend

Gradle + Spring Boot backend for a Home Food & Subscription Marketplace.

## Tech
- Spring Boot 3
- Spring Data JPA
- MySQL
- Spring Security + BCrypt
- JWT token generation

## Run
1. Default run (no MySQL required, uses persistent H2 local profile):
   - `./gradlew bootRun` (Linux/macOS)
   - `gradlew.bat bootRun` (Windows)
2. Run with MySQL profile:
   - `gradlew.bat bootRun --args="--spring.profiles.active=mysql"`
3. Optional env vars for MySQL:
   - `DB_USERNAME`
   - `DB_PASSWORD`

### If MySQL is not running
- You will get `Communications link failure` / `Connection refused`.
- Start MySQL service and ensure port `3306` is open.
- Without MySQL, keep default `local` profile.
- Local profile stores data in project folder: `food-app-backend/data/`.

## Core APIs
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/sellers/nearby?lat=..&lon=..&radiusKm=..`
- `POST /api/v1/subscriptions` (header `X-User-Id`)
- `PATCH /api/v1/subscriptions/{id}/pause`
- `PATCH /api/v1/subscriptions/{id}/resume`
- `PATCH /api/v1/subscriptions/{id}/cancel`
- `PATCH /api/v1/subscriptions/{id}/skip`

## Database
- Normalized schema file: `src/main/resources/schema.sql`
- Required tables included:
  - users, sellers, addresses, menus, menu_items
  - subscription_plans, subscriptions, subscription_deliveries
  - reviews, payments