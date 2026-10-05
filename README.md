# user-service

The identity context of the identity platform: **who someone is**. It owns the user as an identity: lifecycle, profile, account state, account actions and the administration of other users' credentials.

It does not decide what anyone may do. Roles, groups, permissions, login and sessions belong to [`auth-service`](../auth-service/README.md); this service never changes them.

Keycloak is the identity provider behind this service and is an implementation detail: nothing outside one adapter package knows it exists, and no caller ever needs the Keycloak console or Admin API.

## Contents

- [Responsibilities](#responsibilities)
- [API](#api)
- [Errors](#errors)
- [Rules the service enforces](#rules-the-service-enforces)
- [Architecture](#architecture)
- [Data](#data)
- [Configuration](#configuration)
- [Run](#run)
- [Test](#test)
- [Build and image](#build-and-image)

## Responsibilities

| Area | What it does |
| --- | --- |
| Registration | Anyone can register; a verification email is sent and the account cannot sign in until the address is verified |
| User lifecycle | Create, read, search, update, enable, disable, lock, unlock, delete |
| Profile | Extended attributes (phone, job title, department, locale, time zone, bio) in this service's own database |
| Account actions | Send verification email, send password-reset email, set required actions |
| Credentials (admin) | Set a permanent or temporary password, list credential types, remove a credential |
| Password reset request | Public; answers the same whether or not the address is registered |

Sources of truth: Keycloak for identity, account state and credentials; this service's database for the extended profile.

## API

Base path `/api/v1/users`. Reach it through the gateway (`http://localhost:9211`); the service itself listens on 9121. OpenAPI at `/v3/api-docs`, Swagger UI at `/swagger-ui.html` (off in `prod`).

"Self" means the `{id}` in the path is the caller's own user ID.

| Method and path | Purpose | Needs |
| --- | --- | --- |
| `POST /register` | Register yourself. `201` with `Location`. | public |
| `POST /password-reset-requests` | Ask for a password-reset email. Always `202`. | public |
| `GET /me` | Your identity and profile | any valid token |
| `PUT /me` | Update your name and profile | any valid token |
| `POST /` | Create a user; a supplied password is temporary | `users:write` |
| `GET /` | Search: `q`, `enabled`, `page`, `size`, `sort=username,asc` | `users:read` |
| `GET /{id}` | Read a user | `users:read` or self |
| `PUT /{id}` | Change email and name; a new email must be verified again | `users:write` |
| `POST /{id}/enable`, `/disable` | Switch an account on or off; disabling ends its sessions | `users:write` |
| `POST /{id}/lock`, `/unlock` | Suspend until explicitly unlocked; locking ends its sessions | `users:write` |
| `DELETE /{id}` | Delete identity and profile | `users:write` |
| `GET /{id}/profile` | Read a profile | `users:read` or self |
| `PUT /{id}/profile` | Update a profile | `users:write` or self |
| `POST /{id}/actions/send-verify-email` | Send the verification email. `202`. | `users:write` |
| `POST /{id}/actions/send-reset-password-email` | Send a password-reset link. `202`. | `users:write` |
| `PUT /{id}/actions/required` | Replace required actions: `VERIFY_EMAIL`, `UPDATE_PASSWORD`, `UPDATE_PROFILE` | `users:write` |
| `GET /{id}/credentials` | List credentials, without secrets | `users:read` |
| `PUT /{id}/credentials/password` | Set a password: `{"password": "...", "temporary": true}` | `users:write` |
| `DELETE /{id}/credentials/{credentialId}` | Remove a credential; ends the user's sessions | `users:write` |

A user's roles, groups and permissions are at `/api/v1/users/{id}/roles`, `/groups` and `/permissions`. Those paths are served by `auth-service`; the gateway routes them there.

Lists are paged with `page` (from 0) and `size` (1 to 100) and return `items`, `total`, `page`, `size`. Users can only be sorted by username ascending, because that is the only order Keycloak's search offers; any other `sort` is a `400`.

Example:

```bash
curl -s -X POST localhost:9211/api/v1/users -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"username":"bob","email":"bob@example.com","firstName":"Bob","lastName":"Roe","temporaryPassword":"Temp!Passw0rd1","emailVerified":true}'
```

## Errors

Every error is an RFC 9457 problem (`application/problem+json`) with a stable `type` (`https://platform.local/problems/<code>`) and a matching `code`. Clients should switch on `code`, never on the text. Keycloak's own error bodies and stack traces are never returned.

| `code` | Status | When |
| --- | --- | --- |
| `invalid-value` | 400 | Validation failed; `errors` lists `field` and `message` |
| `unauthorized` | 401 | No token, or one that cannot be verified |
| `invalid-token` | 401 | The token's session has ended (checked on sensitive operations) |
| `forbidden` | 403 | The token lacks the permission |
| `operation-not-permitted` | 403 | A domain guard refused it, whatever the permissions |
| `user-not-found`, `credential-not-found` | 404 | |
| `duplicate-user` | 409 | Username or email already taken |
| `invalid-state` | 409 | For example enabling a locked user |
| `password-policy` | 422 | The password does not meet the realm policy |
| `identity-provider-unavailable` | 503 | Keycloak failed or could not be reached |
| `internal-error` | 500 | Anything unexpected |

## Rules the service enforces

These are domain rules, tested without Spring:

- **Account states:** `ACTIVE`, `DISABLED`, `LOCKED`. A locked user can only be unlocked, not enabled or disabled; a disabled user cannot be locked. Neither can sign in.
- **Changing the email** resets verification.
- **No self-harm:** nobody disables, locks or deletes their own account.
- **The last platform administrator** cannot be disabled, locked or deleted.
- **No takeover:** only a `PLATFORM_ADMIN` may change the email, password, credentials or state of a `PLATFORM_ADMIN`. Holding `users:write` is not enough.
- **Creating a user is all or nothing.** The identity is created in Keycloak, then the profile row, then (for registration) the email. If a later step fails the identity is deleted again ([ADR 0006](../micro-services/docs/adr/0006-user-creation-consistency.md)).
- **Sensitive operations check the token with Keycloak.** Creating, changing and deleting users, credential changes and account actions ask whether the token is still active, so a token from an ended session is refused before it expires ([ADR 0009](../micro-services/docs/adr/0009-revocation.md)).

## Architecture

Clean architecture; dependencies point inward and ArchUnit fails the build if they do not.

```
com.users
├── domain            Pure Java. No Spring, JPA or Keycloak.
│   ├── model         User, UserProfile, UserId, Username, Email, PersonName, Password, AccountStatus, ...
│   ├── policy        AdministrationPolicy (the guards above)
│   ├── event         UserRegistered, UserDisabled, UserDeleted, ... (published in-process)
│   ├── port          IdentityProviderPort, UserProfileRepository, TokenStatusPort, DomainEventPublisher
│   └── exception     One type per error code
├── application       One class per use case (RegisterUser, CreateUser, DisableUser, ...). Depends only on domain.
├── infrastructure
│   ├── keycloak      KeycloakIdentityProviderAdapter: the only code that talks to Keycloak
│   ├── persistence   JPA entity and repository for the profile
│   ├── event         Logs and publishes domain events
│   └── config        Wires the use cases as beans
└── interfaces
    ├── rest          Controllers, request and response models, problem-detail error handling
    └── security      Filter chain, method-security expressions, introspection on sensitive paths
```

Swapping Keycloak for another identity provider means writing another implementation of `IdentityProviderPort` and `TokenStatusPort`; nothing else changes.

Tokens are validated with the shared `platform-security-starter` from `micro-services`: signature against the JWKS, RS256 only, exact issuer, `user-service` in the audience, expiry with clock skew, and `typ` `Bearer`. Realm roles become `ROLE_<name>`; permissions such as `users:read` are used as authorities directly.

## Data

Database `user_db`, owned by user `user_service`. Flyway owns the schema (`src/main/resources/db/migration`); Hibernate only validates it.

| Table | Columns |
| --- | --- |
| `user_profile` | `user_id` (the Keycloak user ID, primary key), `phone_number`, `job_title`, `department`, `locale`, `time_zone`, `bio`, `created_at`, `updated_at`, `version` |

A user created outside this service (the bootstrap administrator) has no row; reading the profile then returns an empty one.

In Keycloak this service uses its own client `user-service` with a service account holding `manage-users`, `view-users`, `query-users` and `view-realm` ([ADR 0007](../micro-services/docs/adr/0007-keycloak-admin-access.md)).

## Configuration

Split by environment ([ADR 0014](../micro-services/docs/adr/0014-environment-profiles.md)):

| File | Holds |
| --- | --- |
| `application.yml` | What is common: name, port, JPA settings, graceful shutdown, client ID, audience |
| `application-dev.yml` | Config Server and Vault imports, optional, with localhost defaults |
| `application-qa.yml`, `application-prod.yml` | The same imports, required, with no defaults |

More settings come from the Config Server (`service-configs/application*.yml` and `user-service*.yml`, split the same way) and secrets from Vault.

| Variable | Default in `dev` | Meaning |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev`, `qa` or `prod` |
| `SERVER_PORT` | `9121` | HTTP port |
| `CONFIG_SERVER_URL` | `http://localhost:9311` | Config Server |
| `VAULT_URI`, `VAULT_TOKEN` | `http://localhost:8200`, none | Vault and this service's token |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/user_db` | Its database |
| `KEYCLOAK_URL` | `http://localhost:8080` | Where it reaches Keycloak |
| `KEYCLOAK_PUBLIC_URL` | `http://localhost:8080` | The issuer in tokens; compared exactly |
| `EUREKA_URL` | `http://localhost:9111/eureka/` | Registry |

In `qa` and `prod` none of the addresses has a default; a missing one stops the service at startup.

Secrets, read from Vault at `secret/user-service`: `spring.datasource.username`, `spring.datasource.password`, `platform.keycloak.client-secret`.

## Run

This repository must sit next to [`micro-services`](../micro-services/README.md), which holds the version catalog and the shared starter.

**With the whole platform** (the usual way):

```bash
cd ../micro-services && make up
```

**From source, against the running platform:**

```bash
cd ../micro-services && scripts/run-from-source.sh user-service
```

That stops this service's container and runs `./gradlew bootRun` here with the stack's settings and Vault token. Ctrl-C stops it; `scripts/start.sh user-service` puts the container back.

**Restart just this service** after a change: `cd ../micro-services && scripts/restart.sh --build user-service`.

The service shuts down gracefully: on stop it finishes requests in flight (up to 30 seconds) and deregisters from Eureka.

## Test

```bash
./gradlew build
```

Needs Docker for Testcontainers. 139 tests; none are skipped.

| Kind | Tests | Against |
| --- | --- | --- |
| Domain | 50 | Plain Java |
| Use cases | 34 | In-memory port fakes |
| Architecture (ArchUnit) | 7 rules | The compiled classes |
| Keycloak adapter | 15 | Keycloak 26.8.0 loaded with the platform realm file, plus Mailpit |
| Profile repository | 5 | Postgres 18, schema from Flyway |
| Controllers (`@WebMvcTest`) | 22 | Mocked use cases: validation, error mapping, authorization |
| Whole service | 6 | Postgres, Keycloak and Keycloak-issued tokens over HTTP |

The build fails if line coverage of `domain` and `application` drops below 80% (currently 99%). Reports: `build/reports/tests/test/index.html` and `build/reports/jacoco/test/html/index.html`.

## Build and image

- Java 27, Gradle 9.8.0 (wrapper), Spring Boot 4.1.1. Versions come from `../micro-services/gradle/libs.versions.toml`.
- `Dockerfile` is multi-stage: build on JDK 27, run on a JRE 27 Alpine image as a non-root user, with a health check on `/actuator/health/readiness`. It needs the platform root as a named build context, which `docker-compose.yml` supplies:

```bash
docker build --build-context platform=../micro-services -t user-service .
```

- Health: `/actuator/health`, with `liveness` and `readiness` groups.
