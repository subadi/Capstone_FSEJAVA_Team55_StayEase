# StayEase Backend

Spring Boot 3.5 / Java 17 / Spring Web / Spring Data JPA / PostgreSQL / H2 / BCrypt / JWT.

## Run locally
Requires Java 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Default profile uses H2 in-memory DB. PostgreSQL is configured in `application-prod.yml`.

## Seed accounts
- Guest: guest1@stayease.com / guest123
- Manager: mgr1@stayease.com / manager123
- Admin: admin@stayease.com / admin123

## API contract
The requested endpoints are implemented exactly:
- POST `/api/auth/register` -> 201
- POST `/api/auth/login` -> 200 `{token}`
- POST `/api/auth/logout` -> 204
- GET `/api/hotels?city=`
- GET `/api/hotels/{id}/rooms?checkIn=&checkOut=`
- POST/PUT/DELETE `/api/hotels`
- POST `/api/hotels/{id}/rooms`
- PUT/DELETE/PATCH `/api/rooms/{id}` / `/api/rooms/{id}/status`
- POST `/api/bookings`
- GET `/api/bookings/mine`
- PUT `/api/bookings/{id}/cancel`

Two supporting read endpoints are also included because the specification requires manager dashboard read functionality but does not list read endpoints for manager rooms/upcoming bookings:
- GET `/api/manager/rooms`
- GET `/api/manager/bookings?upcoming=true`

JWT is sent as `Authorization: Bearer <token>`.

## Security
- BCrypt password hashing
- Stateless JWT authentication
- Role authorization in Spring Security + ownership checks in services
- Structured error JSON: timestamp/path/error/message
- Booking overlap enforced transactionally with a pessimistic room lock

## Tests
```bash
mvn test
```
