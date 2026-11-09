# Modernization Plan: TRONNO Owed-Debt Tracking Rebuild

**Project**: TRONNO

## Technical Framework

- **Language**: Java 17 (backend) and JavaScript (frontend)
- **Framework**: Spring Boot 4.1.1; React 18
- **Build Tool**: Maven; npm/Vite
- **Database**: MySQL with Flyway-managed schema
- **Key Dependencies**: Spring Security/JWT, Spring Data JPA, Spring Mail, React Router

## Overview

Rebuild TRONNO as a private, single-sided debt tracker for a signed-in person who owes money. Replace misleading creditor-side and two-way debt flows with owner-scoped records and straightforward debt creation, reliable payment tracking, reminders, and good-faith repayment negotiation drafts.

The existing account and debt data may be discarded as authorized for this rebuild. The application must not reintroduce demo users, known passwords, or sample personal debts. Database and mail credentials must remain external to source control.

## Migration Impact Summary

| Area | Current state | Target outcome |
|---|---|---|
| Debt data | Mixed debt and creditor/P2P concepts | Only the signed-in user's owed debts |
| Debt creation | Existing feature-rich flows | Empty, simple form for interest-free or interest-bearing debt |
| Reminders | Existing reminder facilities | Daily email from 5 days before due through full recorded payoff |
| Negotiation | Existing escape/trick wording | User-reviewed, good-faith letter/message drafts |
| Data and config | Existing Flyway schema and seeder | Rebuilt schema/seeds; secrets supplied outside source |

## Scope and Constraints

- Scope includes frontend, backend, schema/migrations/seeding, configuration, and automated tests for the requested behavior.
- Enforce ownership from the authenticated server-side identity for every debt, payment, reminder, and draft operation; never rely on client-supplied owner IDs.
- A debt remains unpaid until recorded payments fully satisfy its balance. Reminders continue daily after the due date until then.
- Do not deploy or provision cloud infrastructure as part of this plan.
- Keep negotiation drafts user-reviewed and factual; do not send messages automatically.

## Planned Work

See `.metadata/tasks.json` for the sequenced, executable task breakdown and validation criteria.

## Validation

Build and run the relevant automated tests after each task. Cover cross-account access denial, owed-only behavior, empty-state debt creation for both interest options, reminder boundary/recurrence/stop conditions, and negotiation draft generation.
