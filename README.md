# StayEase Full Stack

This archive contains the React frontend wired directly to the Spring Boot backend. Mock API/data is no longer used by the frontend runtime.

## Start backend
```bash
cd backend
mvn spring-boot:run
```

## Start frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend expects `http://localhost:8080/api` by default. Override with `VITE_API_BASE_URL`.

## Seed accounts
Guest `guest1@stayease.com / guest123`  
Manager `mgr1@stayease.com / manager123`  
Admin `admin@stayease.com / admin123`

## Important contract note
The supplied API outline does not define GET endpoints for manager room lists or manager upcoming bookings, although the Manager Dashboard requires those reads. This project keeps the requested endpoints and adds only these two supporting read endpoints so the manager feature remains fully functional after refresh:
- GET `/api/manager/rooms`
- GET `/api/manager/bookings?upcoming=true`
