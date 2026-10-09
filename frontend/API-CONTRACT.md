# StayEase Frontend API Contract Notes

The capstone document is the source of truth. This file records exactly what the frontend expects and isolates the few API additions needed for screens whose endpoints are not explicit in the supplied outline.

## Base URL
`VITE_API_BASE_URL` defaults to `http://localhost:8080/api`.

## Authentication
- `POST /auth/register` body `{ email, password, name }` → `201` response. Frontend accepts `{ token, user }`; if backend returns token only, frontend calls the optional `GET /auth/me` extension to hydrate the profile.
- `POST /auth/login` body `{ email, password }` → `200 { token }` per spec. Frontend accepts `{ token, user }` too; token-only responses are normalized through `GET /auth/me`.
- `POST /auth/logout` → `204`

## Guest
- `GET /hotels?city={city}`
- `GET /hotels/{id}/rooms?checkIn={YYYY-MM-DD}&checkOut={YYYY-MM-DD}`
- `POST /bookings` body `{ roomId, checkInDate, checkOutDate }`
- `GET /bookings/mine`
- `PUT /bookings/{id}/cancel`

## Manager
The supplied outline defines room mutations but not manager read endpoints. The frontend adapter keeps these calls isolated:
- `GET /manager/rooms`
- `GET /manager/bookings?upcoming=true`
These manager read endpoints are frontend-required extensions because the supplied API outline lists only room mutations for managers.

## Admin
- `POST /hotels`
- `PUT /hotels/{id}`
- `DELETE /hotels/{id}`

## Suggested detail endpoint
`GET /hotels/{id}` is recommended because the detail screen needs the hotel's description, amenities and cover independently of a city search.

## Error JSON
All non-2xx responses should use:

```json
{
  "timestamp": "2025-09-12T12:00:00Z",
  "path": "/api/…",
  "error": "VALIDATION_ERROR",
  "message": "Field is required"
}
```

### Auth profile extension
- `GET /auth/me` is an optional frontend integration endpoint used only when register/login returns a token without a user profile.

### Price ownership
`pricePerNight` belongs to Room, not Hotel. Hotel search results may expose an optional `startingPrice`; the mock derives it from the cheapest active room. A hotel with no active rooms must not display a fake `0` price; the UI shows `Rooms not added yet`.
