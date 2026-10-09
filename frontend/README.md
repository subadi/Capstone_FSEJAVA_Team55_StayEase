# StayEase Frontend

This version uses the real Spring Boot API by default. Mock API code has been removed from runtime.

## Run
```bash
npm install
npm run dev
```
Backend must be running at `http://localhost:8080` or set `VITE_API_BASE_URL`.

See the root `backend/README.md` for backend setup and credentials.

# StayEase Frontend — Foundation Capstone

Production-style React + TypeScript frontend for the StayEase hotel booking specification. The UI is wired through a single API service boundary so the mock implementation can be replaced by the Spring Boot backend without rewriting pages/components.

## Spec coverage

This frontend covers the 23-item capstone structure, including authentication, hotel search, date-based availability, booking/cancellation, My Stays, manager room CRUD/upcoming bookings, admin hotel CRUD, role-based routing, validation/error states, responsive wireframe-aligned UI, tests, Git hygiene, README/API documentation, and stretch-goal-ready date/price calculation.

The source specification asks for React + Vite/CRA or Angular, routing, hooks/context, TypeScript, selected dates via query parameters, backend-computed availability, REST endpoints, and structured API errors. See the supplied capstone document for the authoritative backend requirements.

## Run

```bash
npm install
npm run dev
```

Default mock mode is enabled. Demo accounts:

| Role | Email | Password |
|---|---|---|
| Guest | guest1@stayease.com | guest123 |
| Manager | mgr1@stayease.com | manager123 |
| Admin | admin@stayease.com | admin123 |

## Quality checks

```bash
npm run build
npm run lint
npm test
npm run test:coverage
```

The Vitest coverage gate is configured at 80% lines/statements/functions and 75% branches. The specification itself sets a 60% backend target and only asks for minimal optional frontend unit tests; this project intentionally raises the frontend gate to 80% as requested for review quality.

Coverage HTML report: `coverage/index.html` after `npm run test:coverage`.

## Mock → Spring Boot integration

The page layer imports only `api` from `src/services/api.ts`.

- `VITE_USE_MOCK_API=true` → `src/mock/api.ts`
- `VITE_USE_MOCK_API=false` → `src/services/http.ts` + Spring Boot REST API
- `VITE_API_BASE_URL=http://localhost:8080/api`

Copy `.env.example` to `.env.local` and set:

```env
VITE_USE_MOCK_API=false
VITE_API_BASE_URL=http://localhost:8080/api
```

### Contract mapped from the specification

| Frontend operation | Backend contract |
|---|---|
| Register | `POST /api/auth/register` |
| Login | `POST /api/auth/login` |
| Logout | `POST /api/auth/logout` |
| Hotel search | `GET /api/hotels?city=` |
| Available rooms | `GET /api/hotels/{id}/rooms?checkIn=&checkOut=` |
| Create hotel | `POST /api/hotels` (ADMIN) |
| Update hotel | `PUT /api/hotels/{id}` (ADMIN) |
| Delete hotel | `DELETE /api/hotels/{id}` (ADMIN) |
| Create room | `POST /api/hotels/{id}/rooms` (MANAGER) |
| Update room | `PUT /api/rooms/{id}` (MANAGER) |
| Delete room | `DELETE /api/rooms/{id}` (MANAGER) |
| Toggle room | `PATCH /api/rooms/{id}/status` (MANAGER) |
| Create booking | `POST /api/bookings` |
| My bookings | `GET /api/bookings/mine` |
| Cancel booking | `PUT /api/bookings/{id}/cancel` |

### Backend contract gaps to confirm with the backend team

The capstone API outline does not explicitly define endpoints for:

1. Fetching a single hotel by ID, although the Hotel Detail screen requires it.
2. Listing all manager-owned rooms without availability dates.
3. Listing upcoming manager bookings.
4. Returning room type/hotel summary alongside each item in `GET /api/bookings/mine`.

This frontend currently uses clearly isolated mock-only adapter methods for these screens. Before switching to real data, agree on those endpoints/response DTOs with the backend team rather than scattering assumptions through React components.

Recommended additions (if backend team agrees):

- `GET /api/hotels/{id}`
- `GET /api/manager/rooms` (manager inferred from authenticated user)
- `GET /api/manager/bookings?upcoming=true`
- Include `hotelName` and `roomType` in the booking response DTO, or expose a booking-detail DTO.

## Error contract

The HTTP adapter expects the specification's structured error shape:

```json
{
  "timestamp": "2025-09-12T12:00:00Z",
  "path": "/api/...",
  "error": "VALIDATION_ERROR",
  "message": "Field is required"
}
```

## Availability rule

The mock follows the required overlap rule and ignores cancelled bookings:

`existing.checkIn < requested.checkOut && existing.checkOut > requested.checkIn`

The authoritative availability calculation should remain on the backend.

## Auth/security note

The mock uses browser storage only to simulate an authenticated session. Do not treat this as production security. The capstone allows a stateless JWT or session cookie; for the real deployment, align with the backend team. An HttpOnly secure cookie is preferable when the backend owns the session lifecycle.

## Git hygiene

`.gitignore` excludes dependencies, build output, coverage, local environment files, logs, IDE files, and OS metadata. Do not commit real secrets or production credentials.

Suggested workflow:

```text
main
feature/<short-description>
```

Open a PR, run build/lint/tests, get review, then merge.

## Wireframe / acceptance coverage

- Home: City + Check-in + Check-out + Search Hotels
- Hotel list: cover, name, stars, city, starting price, View Rooms
- Detail: cover, description, amenities, available room type/price/max occupancy, Book
- My Stays: hotel, room type, dates, total, status, Cancel
- Manager: room CRUD, active toggle, upcoming bookings
- Admin: hotel CRUD

Seed data includes the required Goa hotels Sunset Palms and City Inn and the demo roles/accounts.

## Important data ownership
Hotel CRUD does not accept a price field because the capstone data model puts `pricePerNight` on Room. A hotel's search-card starting price is derived from its active rooms in the mock API. A newly-created hotel therefore shows `Rooms not added yet` until its manager adds an active room.
