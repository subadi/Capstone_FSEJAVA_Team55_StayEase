# StayEase Frontend QA Matrix

This matrix is the manual smoke-test plan for the Foundation capstone. Run it against the mock API first, then repeat the same flows against Spring Boot when available.

## Guest

| Area | Check | Expected |
|---|---|---|
| Register | Name/email/password | Guest account created; no password exposed in UI state |
| Register | Duplicate email | Friendly validation error |
| Login | Guest credentials | Redirects to Home and shows My Stays |
| Login | Wrong password | Error message; no protected access |
| Search | City + valid check-in/out | Hotel list loads; dates remain in URL |
| Search | Check-out <= check-in | Search disabled / validation prevents request |
| Hotel card | Existing hotel | Name, stars, image, starting price, View Rooms |
| Hotel card | Hotel with no rooms | Shows `Rooms not added yet`, never fake ₹0 |
| Detail | Selected dates | Description, amenities, available rooms, price and occupancy |
| Availability | Overlapping booked room | Room is absent |
| Availability | Boundary checkout/check-in | Room is available when dates only touch |
| Booking | Logged-in guest | Review modal shows nights × nightly price |
| Booking | Confirm | Booking reference and total displayed |
| Booking | Double booking race/mock conflict | Friendly unavailable error |
| My Stays | Confirmed booking | Hotel, room type, dates, total, status shown |
| Cancel | Upcoming confirmed booking | Status becomes CANCELLED and room becomes available |
| Cancel | Past booking | Cancel action unavailable / API rejects |
| Logout | Guest | Session cleared; protected route redirects to login |

## Manager

| Area | Check | Expected |
|---|---|---|
| Access | Guest/Admin opens `/manager/rooms` | Redirects away |
| Read | Manager login | Only rooms for manager's hotels are shown |
| Add room | Every room field | Room appears with correct number/type/price/occupancy/description/image/active state |
| Validation | Price <= 0 / occupancy <= 0 | Save rejected |
| Validation | Duplicate room number within hotel | Save rejected |
| Edit | Room fields | Changes persist after reload/load |
| Status | Active toggle | Room disappears from availability when inactive |
| Delete | Room | Room removed from manager list and availability |
| Ownership | Manager attempts another manager's room/hotel | API rejects |
| Upcoming bookings | Existing booking | Booking ref, hotel, dates, total, status shown |

## Admin

| Area | Check | Expected |
|---|---|---|
| Access | Guest/Manager opens `/admin/hotels` | Redirects away |
| Add hotel | Name/city/stars/description/cover/manager/amenities | Hotel appears in directory |
| Image | Valid cover URL | Preview and guest card show image |
| Image | Broken cover URL | Placeholder shown, no broken-image UI |
| Price | New hotel with no rooms | `Rooms not added yet`; never ₹0 |
| Price | Manager adds active room | Hotel starting price becomes cheapest active room |
| Edit | Hotel fields | Changes persist |
| Delete | Hotel | Hotel removed; its rooms are removed in mock |
| Validation | Invalid stars / missing manager | Save rejected |

## Cross-cutting

- Refresh after every CRUD operation.
- Verify no password is rendered or persisted in the auth context object.
- Verify role-protected routes.
- Verify structured API errors render as user-friendly messages.
- Verify image fallback for hotel and room images.
- Run `npm run build`, `npm run lint`, `npm test`, and `npm run test:coverage` in an environment with dependencies installed.
- Coverage gate is 80% lines/statements/functions and 75% branches.

## Known backend contract extensions

The supplied capstone API outline does not define read endpoints for manager room/bookings, a hotel detail endpoint, or a current-user profile endpoint for token-only authentication. These are isolated in the service layer and listed in `API-CONTRACT.md`; confirm the exact Spring Boot paths/DTOs with the backend team before switching off mock mode.
