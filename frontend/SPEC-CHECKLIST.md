# StayEase Spec Traceability Checklist

| # | Specification area | Frontend status |
|---|---|---|
| 1 | About Us | StayEase branding and purpose reflected in UI/README |
| 2 | Problem Statement | Search → availability → booking flow implemented |
| 3 | Roles | GUEST / MANAGER / ADMIN; protected routes and role-specific navigation |
| 4 | In-Scope Features | Auth, browse, availability, booking, cancellation, manager/admin CRUD |
| 5 | Happy Path | Search → detail → auth → confirm → My Stays; manager/admin paths |
| 6 | Section numbering gap | Source document jumps 5 → 7; no separate #6 section exists |
| 7 | Architecture | React, Context, routing, service/API boundary |
| 8 | Tech Stack | React + Vite + TypeScript + CSS |
| 9 | Data Model | Hotel→Room, User→Booking, Room→Booking represented in types/mock |
| 10 | API Outline | Spec endpoints mapped; extra read endpoints isolated/documented |
| 11 | NFRs & SLAs | UI validation, error states, structured error adapter, README; performance/load must be validated with real backend |
| 12 | Testing | Vitest + Testing Library; frontend coverage gate set to 80% lines/statements/functions, 75% branches |
| 13 | Engineering PoV | Service boundary, typed models, lint config, Git hygiene |
| 14 | Delivery Plan | Sprint-oriented feature separation reflected in pages/services |
| 15 | Acceptance Criteria | Goa seed data, overlap exclusion, cancellation reopening, booking total/ref, My Stays, manager access |
| 16 | Seed Data | Sunset Palms, City Inn, Royal Orchid + demo users/rooms |
| 17 | Wireframe | Home search, hotel cards, detail/rooms, My Stays table, Manager tables, Admin CRUD |
| 18 | User Stories | Guest browse/book/cancel; manager room management; admin directory |
| 19 | DoR / DoD | README, API notes, tests, lint/build scripts; peer review/manual Postman remain team activities |
| 20 | Evaluation Rubric | Routing/hooks/context/TS/component communication/API boundary demonstrated |
| 21 | Numbering gap | Source document jumps 20 → 22; no separate #21 section exists |
| 22 | Stretch Goals | Dynamic nights/total included; star filter/printable summary left optional |
| 23 | Getting Started | npm install/dev/build/test/coverage and env setup documented |

## Backend handoff items

Before switching `VITE_USE_MOCK_API=false`, backend team should confirm the four contract gaps documented in `API-CONTRACT.md`: hotel detail read, manager room read, manager upcoming-booking read, and booking DTO room/hotel summary.


### Validation pass
- Guest: search/date query params, hotel detail, availability overlap, booking total/ref, My Stays, cancellation/re-availability.
- Manager: own-hotel room create/edit/delete/status, all room fields, upcoming bookings, ownership enforcement.
- Admin: hotel create/edit/delete, all hotel fields, image preview/fallback, manager assignment, derived starting price.
- Cross-role: protected routes and mock authorization.
- No fake `₹0` when a hotel has no rooms; price is room-owned.
