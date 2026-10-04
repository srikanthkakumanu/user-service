# User Service

User Service manages application users and profiles and coordinates identity lifecycle operations with Keycloak. It does not issue access tokens, verify user passwords, or replace Keycloak's authentication endpoints.

## Responsibilities And Relationships

- Keycloak owns credentials, authentication, realm roles, and user enablement.
- This service persists application user/profile data in PostgreSQL `userdb`.
- `api-gateway` forwards user API calls with the caller's bearer token.
- `auth-service` owns the separate fine-grained authorization catalog; its roles are not automatically synchronized with this service's legacy role table.
- Shared configuration and database initialization are maintained in [service-configs](../service-configs/README.md) and [micro-services](../micro-services/README.md).
## Technology

| Component | Current repository baseline |
| --- | --- |
| Java | 27 |
| Spring Boot | 4.1.1 |
| Gradle | 8.14.3; Groovy DSL and this repository's own wrapper |

| MapStruct | 1.6.3 |
| springdoc-openapi | 2.8.5 |

The agreed target is Java 27, a compatible current Spring Boot/Spring Cloud stack, and domain-driven clean architecture. Versions above describe the checked-in build, not a claim that every migration is complete.
## Architecture And Storage

The current implementation uses controllers, DTOs, MapStruct mappers, services, JPA domain entities, repositories, and a Keycloak integration adapter. It has not yet completed the framework-independent domain/ports/adapters refactor.

`KeycloakAdminClient` uses a service-account client-credentials token and Keycloak Admin REST calls for creation, updates, enablement, password resets, and realm-role changes. Local database transactions cannot atomically commit Keycloak operations; compensation/reconciliation remains pending.

Flyway manages PostgreSQL schema initialization. Runtime database access uses `theuser` and migrations use `useradmin` in the shared initialization contract. Match passwords to local Vault/configuration; never commit actual secrets.

## Ports And Deployment Modes

| Endpoint | Shared platform | Service-local Compose |
| --- | --- | --- |
| HTTP API | 9121 | 9121 |
| PostgreSQL host port | 5432 | 15432 |
| Keycloak host port | 8080 | 18080 |

The local dependency ports can be overridden with `USER_DB_HOST_PORT` and `USER_KEYCLOAK_HOST_PORT`. Container-to-container ports remain PostgreSQL 5432 and Keycloak 8080. Run this API once per port; shared and local deployments are alternatives.

## API

All paths below are relative to `http://localhost:9121`.

| Method | Path | Operation |
| --- | --- | --- |
| GET | `/api/users/ping` | Public connectivity check |
| GET / POST | `/api/users` | List / create users |
| GET / PUT / DELETE | `/api/users/{id}` | Read / update / disable user |
| GET | `/api/users/email/{email}` | Find user by email |
| POST | `/api/users/{id}/lock` | Disable identity and local user |
| POST | `/api/users/{id}/unlock` | Re-enable user |
| POST | `/api/users/{id}/reset-password` | Reset Keycloak password |
| POST / DELETE | `/api/users/{id}/roles` | Assign / remove Keycloak realm role |
| GET | `/api/users/{userId}/profile` | Profile by user |
| GET | `/api/users/email/{email}/profile` | Profile by email |
| GET / PUT | `/api/users/profile/{id}` | Read / update profile |
| GET / PUT | `/api/users/profile/address/{id}` | Read / update address |

Creation returns 201. Password reset and realm-role changes return 204. Deletion is a soft disable, not a hard delete. Reset payload contains `temporaryPassword` and `temporary`; realm-role payload contains `role`.

User creation requires an email-formatted `loginId` and `userAgentType` (`API`, `WEB`, or `MOBILE`). Optional fields include `temporaryPassword`, `roles`, `profile`, and `status`. Supply passwords only over protected connections and never log request bodies containing them.

The legacy `/api/users/roles` CRUD API is transitional. Its two single-segment GET mappings (ID versus role name) need disambiguation.

## Security

JWTs are validated against `KEYCLOAK_ISSUER_URI`. User creation and password resets require `ADMIN`; lock/unlock and realm-role assignments allow `ADMIN` or `MANAGER`. Other non-public operations currently require authentication, but ownership/administrative enforcement is not yet comprehensive.

Health and ping are public. The custom `/api-docs` path is not currently included in the public documentation allowlist. springdoc 2.8.5 compatibility with Boot 4 remains pending.
## Build And Verification

Run commands from this repository's root; do not use another service's Gradle wrapper.

```bash
bash ./gradlew clean test bootJar
```

The application JAR is written to `build/libs/`. Dockerfiles consume that JAR, so build it before building an image. Java 27 is the target toolchain for migrated services. The current Gradle 8.x wrapper may need a supported older JVM to launch Gradle while the configured toolchain compiles with Java 27; do not assume Gradle itself can run on JDK 27.

Container recipes use layered-JAR extraction. The complete Docker image/startup path still needs verification after the Spring Boot upgrade.
## Local Execution

Provide PostgreSQL and Keycloak before starting the process. The source datasource defaults still point to `user_service` credentials/database, so explicitly override them for the standardized `userdb` setup.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:15432/userdb
export SPRING_DATASOURCE_USERNAME=theuser
export SPRING_DATASOURCE_PASSWORD='<runtime password from local Vault/config>'
export SPRING_FLYWAY_USER=useradmin
export SPRING_FLYWAY_PASSWORD='<migration password from local Vault/config>'
export KEYCLOAK_BASE_URL=http://localhost:18080
export KEYCLOAK_ISSUER_URI=http://localhost:18080/realms/company-platform
export KEYCLOAK_REALM=company-platform
export KEYCLOAK_ADMIN_CLIENT_ID=user-service
export KEYCLOAK_ADMIN_CLIENT_SECRET='<Keycloak service-account secret>'
export SPRING_DOCKER_COMPOSE_ENABLED=false
bash ./gradlew bootRun
```

For shared dependencies, replace ports 15432/18080 with 5432/8080. The issuer must exactly match the token's `iss`, and the configured service account needs appropriate Keycloak admin privileges.

The repository's Compose file provides a service-local deployment recipe:

```bash
docker compose config --quiet
docker compose up -d --build
```

It depends on the sibling `micro-services/postgres-init` directory. These are deployment commands, not confirmation of a successful full-stack smoke test.

## Pending Work And Troubleshooting

- Finish DDD/clean architecture, fine-grained authorization, ownership checks, and Keycloak/database compensation.
- Upgrade the documentation library for Boot 4 and resolve legacy role-route ambiguity.
- Add Config Client/Vault/Eureka dependencies or explicitly supply environment settings; this build does not currently consume shared config through those clients.
- A 401 commonly indicates an issuer/token mismatch; a Keycloak admin failure requires checking client credentials and service-account permissions.
- Keep passwords, admin client secrets, build artifacts, and editor settings out of Git.

See the [implementation checkpoint](../micro-services/IAM_IMPLEMENTATION_CHECKPOINT.md) for remaining platform work.

## Architecture Reference

```text
user-service
  controller/                 User, profile, role, and ping controllers
  service/                    Application services for users, profiles, addresses, roles
  domain/                     JPA-backed user/profile/address/role entities
  repository/                 Spring Data JPA repositories
  mapper/                     MapStruct DTO/entity mappers
  integration/keycloak/       Keycloak Admin REST client and provisioning adapter
  config/                     Security and OpenAPI wiring
  stats/                      Custom actuator-style endpoint
```

Current request flow for user creation:

```text
POST /api/users
  -> ADMIN JWT required
  -> UserController validates NewUserDTO
  -> UserService coordinates local profile/user persistence
  -> KeycloakUserProvisioningAdapter creates/updates identity through Keycloak Admin API
  -> local database stores app-facing user/profile state
```

The Keycloak call and database transaction are not a distributed transaction. If one side succeeds and the other fails, reconciliation or compensation is an operational concern until that workflow is hardened.

## Configuration Reference

| Variable | Default / role |
| --- | --- |
| `SERVER_PORT` | `9121` |
| `SPRING_DATASOURCE_URL` | Source default is `jdbc:postgresql://localhost:5432/user_service`; override to `userdb` for platform runs. |
| `SPRING_DATASOURCE_USERNAME/PASSWORD` | Runtime DB credentials. |
| `SPRING_FLYWAY_USER/PASSWORD` | Migration credentials, expected role `useradmin` in shared provisioning. |
| `KEYCLOAK_ISSUER_URI` | JWT issuer for resource-server validation. |
| `KEYCLOAK_BASE_URL` | Keycloak Admin REST base URL. |
| `KEYCLOAK_REALM` | Realm, default `company-platform`. |
| `KEYCLOAK_ADMIN_CLIENT_ID` | Service-account client, default `user-service`. |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | Service-account secret; never commit the real value. |

## Command Reference

| Task | Command |
| --- | --- |
| Unit/MVC tests | `bash ./gradlew test` |
| Build executable JAR | `bash ./gradlew bootJar` |
| Full build | `bash ./gradlew clean test bootJar` |
| Build image | `docker build -t user-service:latest .` |
| Validate service-local Compose | `docker compose config --quiet` |
| Start service-local stack | `docker compose up -d --build` |
| Health | `curl http://localhost:9121/actuator/health` |
| Public ping | `curl http://localhost:9121/api/users/ping` |

Representative protected checks:

```bash
curl -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:9121/api/users
curl -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:9121/api/users/email/alice@example.com
```

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| `401` | Missing/invalid token or issuer mismatch. |
| `403` on creation/reset | Token lacks the required `ADMIN` or `MANAGER` role. |
| Keycloak admin `401`/`403` | Service-account client secret, realm, or client service-account roles are wrong. |
| Duplicate user failure | `login_id` or `keycloak_user_id` already exists. |
| Startup uses wrong DB | Explicitly override the source defaults to the platform `userdb` URL/credentials. |
| Role controller ambiguity | `/api/users/roles/{id}` and `/api/users/roles/{role}` overlap and need a route cleanup. |
