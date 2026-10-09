# Postman / Swagger Manual Checklist

Once Spring Boot is available, verify these flows manually:

1. Register guest → `201`.
2. Login guest → `200` token.
3. Search `Goa` → at least Sunset Palms and City Inn.
4. Query rooms for seeded booking dates → overlapping room excluded.
5. Create booking → booking reference + nights × price.
6. `GET /bookings/mine` → booking appears.
7. Cancel booking → `CANCELLED`.
8. Repeat availability query → room available again.
9. Login manager → room create/update/delete/status toggle.
10. Manager upcoming bookings → only manager's hotel.
11. Login admin → hotel create/update/delete.
12. Send invalid dates/IDs/unauthorized role → structured error JSON.
13. Use Postman Runner or multiple browser tabs for the requested light local concurrency check (5–10 requests).
