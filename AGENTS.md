# Development Notes

## AI Assistance

- Cursor (Grok)
- ChatGPT (architecture brainstorming during planning)

## Tasks Assisted

- Project architecture and layered package layout
- JPA entity modelling and relationships
- REST API design and role-based security
- JWT authentication wiring
- Checkout inventory reservation strategy
- Global exception handling
- Unit and integration test scaffolding
- README / AGENTS / SKILLS documentation

## Human Decisions

- Feature prioritization across 12 incremental commits
- H2 as default local DB with optional MySQL profile
- Soft-delete products (`active=false`) instead of hard delete
- Tax = 10% after discount
- Simulated payment always succeeds on checkout
- Pessimistic locking + multi-warehouse allocation during checkout
- Returns only allowed for `DELIVERED` orders
- Orders cannot be cancelled after shipping
- Async notifications/audit after checkout (non-blocking)

## Assumptions Documented in README

All meaningful product/business assumptions are listed in `README.md` under **Assumptions**.
