# ADR-003: Customer Identity Uses Explicit Development and JWT/OIDC Profiles

Date: 2026-08-23  
Status: Accepted

## Context

The initial demo accepts `X-Customer-Id` directly from the client. This is suitable only for local debugging: a production client could impersonate another customer by changing the header. The next iteration requires a standards-based identity source while preserving a frictionless local workflow.

## Decision

1. The application exposes a `CustomerIdentity` port.
2. The explicit `dev` profile permits all HTTP requests and resolves identity from `X-Customer-Id` for local debugging only.
3. The explicit `jwt` profile runs as an OAuth2 Resource Server. It ignores `X-Customer-Id`, resolves the customer from a verified JWT claim, and requires the configured support authority.
4. No profile is a valid identity mode. Deployments must explicitly choose `dev` or `jwt`; production deployments must use `jwt` and provide `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
5. The claim name and required authority are configurable through `customer-service.identity.customer-id-claim` and `customer-service.identity.required-authority`.

## Consequences

- Local VS Code and Docker workflows continue with the explicit `dev` profile.
- Production must configure an issuer, a customer claim, and the support authority mapping before it can serve customer APIs.
- A client-supplied customer header cannot override a JWT-derived customer identity.
