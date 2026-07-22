# Moderated comments API

The Spring Boot service stores anonymous dormitory and dining comments in MySQL. Public submissions are always created with `PENDING` status. Only `APPROVED` comments are returned by the public API.

## Required environment

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_MIGRATION_USERNAME`
- `DB_MIGRATION_PASSWORD`
- `COMMENT_ADMIN_TOKEN` (at least 32 characters)
- `COMMENT_IP_HMAC_SECRET` (at least 32 characters)
- `COMMENT_FORM_HMAC_SECRET` (at least 32 characters)
- `COMMENT_ALLOWED_ORIGINS`

The service binds to `127.0.0.1:8080`. Nginx is the only public entry point.

## Build

```bash
mvn clean test package
```

## API behavior

- `GET /api/comments/form-token`: short-lived signed form token.
- `GET /api/comments`: approved comments for a whitelisted target.
- `POST /api/comments`: rate-limited submission, always pending moderation.
- `GET /api/admin/comments`: pending comments; requires `X-Admin-Token`.
- `PATCH /api/admin/comments/{id}`: approve or reject; requires `X-Admin-Token`.
- `GET /api/health`: verifies database connectivity.

Raw IP addresses are never stored. The browser renders all submitted content with `textContent`, not HTML.
