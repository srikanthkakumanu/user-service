# user-service

The identity context: who someone is. User lifecycle (self-registration, admin create, search, update, enable, disable, lock, unlock, delete), the extended profile, account actions (verification and password-reset emails, required actions) and administration of other users' credentials. It never changes roles, groups or permissions.

Keycloak is the source of truth for identity, account state and credentials and is reached only through `IdentityProviderPort`. The extended profile lives in this service's own database, `user_db`.

API: `/api/v1/users/**`. OpenAPI at `/v3/api-docs`, Swagger UI at `/swagger-ui.html`.

Part of the identity platform; the platform root is [`../micro-services`](../micro-services/README.md). This repository must sit next to it, because the version catalog and the shared security starter are read from there.

## Run

The usual way is the whole stack: `make up` in `../micro-services`. The service then listens on port 9121.

To run it from source against that stack, stop its container and start it with its Vault token:

```bash
cd ../micro-services && docker compose stop user-service && . ./.env && cd ../user-service
VAULT_TOKEN=$USER_SERVICE_VAULT_TOKEN ./gradlew bootRun
```

## Test

```bash
./gradlew build
```

Needs Docker. The build runs domain and use-case unit tests, ArchUnit layering rules, the Keycloak adapter against a real Keycloak loaded with the platform realm file, the profile repository against Postgres, controller slice tests and full-context tests with Keycloak-issued tokens. It fails if line coverage of `domain` and `application` drops below 80%.

## Configuration

Non-secret settings come from the Config Server (`service-configs/user-service*.yml` and `application*.yml`). Secrets come from Vault at `secret/user-service`: `spring.datasource.username`, `spring.datasource.password`, `platform.keycloak.client-secret`.

| Variable | Default | Meaning |
| --- | --- | --- |
| `CONFIG_SERVER_URL` | `http://localhost:9311` | Config Server |
| `VAULT_URI` | `http://localhost:8200` | Vault |
| `VAULT_TOKEN` | none | This service's Vault token |
| `SPRING_PROFILES_ACTIVE` | none | `docker` or `k8s` inside those environments |
| `SERVER_PORT` | `9121` | HTTP port |

## Layout

```
com.users
├── domain          value objects, User and UserProfile, policies, events, ports. Pure Java.
├── application     one class per use case. Depends only on domain.
├── infrastructure  Keycloak adapter, JPA, event publisher, use-case wiring
└── interfaces      REST controllers, request and response models, errors, security
```

Errors are RFC 9457 problem details with a stable `type` and `code`.
