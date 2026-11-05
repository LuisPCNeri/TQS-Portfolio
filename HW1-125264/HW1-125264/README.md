# TuttiBox (HW1-125264)

TuttiBox is a smart-locker parcel management system built for the TQS course.
It exposes a REST API (Spring Boot + PostgreSQL) and a React single-page UI
covering parcel registration, drop-off, pick-up, tracking, locker search and an
operator dashboard.

## Import assumptions

Read this before importing or building the project.

- **Java 21** is required. The backend is compiled with `java.version=21`
  (`pom.xml`, `Dockerfile`, `.settings/org.eclipse.jdt.core.prefs`).
- **Maven**: use the bundled wrapper. `.mvn/wrapper/maven-wrapper.properties`
  uses `distributionType=only-script`, so the wrapper JAR is intentionally not
  committed and Maven 3.10.0 is downloaded on first use. The first `./mvnw`
  invocation therefore needs internet access. A local Maven 3.9+ can be used
  instead if the wrapper cannot download.
- **IDE metadata is not versioned.** `.project`, `.classpath`, `.settings` and
  `.idea` are gitignored. A fresh clone has no IDE project files: import the
  repository as an **Existing Maven Project** (Eclipse) or open `pom.xml`
  (IntelliJ IDEA). Do not rely on committed IDE files.
- **Package/artifact naming**: the Java base package is
  `ua.pt.tqs.HW1_125264` and the artifact is `pt.ua.tqs:HW1-125264`. The
  generated `HELP.md` mentions an older, invalid package name
  (`io.github.LuisPCNeri...`); ignore it.
- **Database**: PostgreSQL is expected. `application.properties` defaults to
  `jdbc:postgresql://localhost:5432/tuttibox` with `postgres/postgres`.
  `compose.yaml` instead creates the `hw1_125264` database and passes it via
  environment variables. Override `SPRING_DATASOURCE_URL`,
  `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` when needed.
- **Seed data**: `src/main/resources/data.sql` runs on every startup
  (`spring.sql.init.mode=always`) and idempotently inserts three stations,
  their compartments, and the backing schema (`ddl-auto=update`).
- **External geocoding**: the default profile uses the public Nominatim API
  (`https://nominatim.openstreetmap.org`) through
  `NominatimGeocodingAdapter`. This requires internet access and respect for
  Nominatim's usage policy. Override `nominatim.base-url` to point elsewhere.
  The `prod` profile excludes this adapter (`@Profile("!prod")`), so a
  production adapter must be supplied.
- **Frontend assumptions**: the React app calls the backend with absolute URLs
  at `http://localhost:8080`, and the controllers only allow CORS from
  `http://localhost:3000`. When running the Vite dev server, use port 3000.
  Nginx proxies `/api/` to `app:8080` in Docker, but the compiled app still uses
  the absolute `localhost:8080` URL.
- **Docker** is needed for the full stack, for integration tests
  (Testcontainers) and for the Playwright end-to-end tests. Docker daemon
  access is required.
- **Node.js 20+ and npm** are needed to build the frontend locally (the
  frontend Dockerfile uses `node:20-alpine`).
- **First-run network access** is needed for Maven/npm dependencies, Docker
  images (`postgres:17`, `postgres:18`, `maven:3.9-eclipse-temurin-21`,
  `eclipse-temurin:21-jre-alpine`, `node:20-alpine`, `nginx:alpine`), and
  Playwright browser binaries.

## Prerequisites

- JDK 21 (`java -version`)
- Docker Engine with Docker Compose v2
- Node.js 20+ and npm
- Internet access for the first build

## Build and run with Docker Compose (recommended)

```bash
docker compose up -d --build
```

This starts:

| Service   | URL                                              |
|-----------|--------------------------------------------------|
| Frontend  | http://localhost:3000                            |
| Backend   | http://localhost:8080                            |
| Swagger UI| http://localhost:8080/swagger-ui/index.html      |
| OpenAPI   | http://localhost:8080/v3/api-docs                |
| Health    | http://localhost:8080/actuator/health            |
| PostgreSQL| localhost:5432 (db `hw1_125264`, user `postgres`)|

Stop the stack with `docker compose down`. To also delete the database volume
and re-run the seed data from scratch, use `docker compose down -v`.

## Build the backend locally

```bash
# Compile, run unit tests and produce the executable JAR
./mvnw clean package

# Unit + integration tests, Testcontainers, and the JaCoCo 70% line gate
./mvnw clean verify

# Build without tests
./mvnw clean package -DskipTests
```

The JAR is written to `target/HW1-125264-0.0.1-SNAPSHOT.jar`.

Run it against the PostgreSQL container:

```bash
docker compose up -d postgres

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hw1_125264
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

java -jar target/HW1-125264-0.0.1-SNAPSHOT.jar
# or, without packaging first:
./mvnw spring-boot:run
```

Without those variables the app expects a local `tuttibox` database on
`localhost:5432`.

## Build and run the frontend locally

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev -- --port 3000
```

The dev server must listen on port 3000 to satisfy the backend CORS
configuration. The backend must be reachable at `http://localhost:8080`.

Production build and lint:

```bash
npm run build
npm run lint
```

## Tests

| Command | What it runs | Requires Docker |
|---------|--------------|-----------------|
| `./mvnw test` | Fast unit tests (JUnit/Mockito/MockMvc) | No |
| `./mvnw verify` | Unit + integration tests (`*IT`: Testcontainers, WireMock, Cucumber) + JaCoCo check | Yes |
| `./mvnw verify -Dit.test=BrowserUseCaseIT -Dbrowser.tests=true` | Playwright end-to-end tests against a running stack | Yes, plus a running stack |

Integration details:

- Integration tests use Testcontainers and pull `postgres:17` / `postgres:18`.
- `GeocodingClientIT` stubs Nominatim with WireMock on port 8089
  (`application-test.properties`).
- Cucumber acceptance tests live in `src/test/resources/features` and are run
  by `CucumberIT`; they use a stub geocoding port and a Testcontainers database.
- Playwright end-to-end tests are disabled unless `-Dbrowser.tests=true` is
  set. Start the stack first with `docker compose up -d --build`. Override the
  targets with `-De2e.frontend.url`, `-De2e.backend.url`, `-De2e.db.url`,
  `-De2e.db.user` and `-De2e.db.password` if needed.
- The JaCoCo quality gate fails `verify` below 70% line coverage. Skip analysis
  with `-Djacoco.skip=true` if necessary.

## API overview

Base path: `/api/v1`. Parcel sizes are `S`, `M`, `L`.

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/stations?addr=...` | Find nearest locker stations for an address |
| `POST` | `/parcels?stationId=...` | Register a parcel (`size`, `recipientContact`) |
| `PUT` | `/parcels/{trackingCode}/dropoff?actor=...` | Drop a parcel off |
| `PUT` | `/parcels/{trackingCode}/pickup?pickupCode=...&actor=...` | Pick a parcel up |
| `GET` | `/parcels/{trackingCode}` | Track a parcel and read its history |
| `GET` | `/operator/stations` | List all stations |
| `GET` | `/operator/stations/{stationId}/compartments` | List station compartments |
| `PUT` | `/operator/compartments/{compartmentId}/out-of-service` | Mark a compartment faulty |
| `GET` | `/operator/parcels?station=...&state=...` | Filter parcels |
| `GET` | `/operator/parcels/expired` | List expired parcels |
| `PUT` | `/operator/parcels/{trackingCode}/return` | Register an expired parcel's removal |

Parcel states: `CREATED`, `IN_TRANSIT`, `READY_FOR_PICKUP`, `PICKED_UP`,
`EXPIRED`, `RETURNED`.

Configuration keys in `application.properties`:

| Property | Default | Meaning |
|----------|---------|---------|
| `tuttibox.pickup-deadline-hours` | `48` | Hours to pick up after drop-off |
| `tuttibox.return-deadline-hours` | `72` | Hours before an expired parcel is returned |
| `tuttibox.expiry-check-interval-ms` | `60000` | Scheduler interval for expiry checks |
| `nominatim.base-url` | `https://nominatim.openstreetmap.org` | Geocoding endpoint |

## Load test (optional)

With the stack running and the seed data loaded (station id `1`), run:

```bash
k6 run load/track-parcel.js
```

The script creates a parcel, then reads it with 20 virtual users for 30 seconds
and enforces p95 < 200 ms and error rate < 5%.

## Project structure

```text
src/main/java/ua/pt/tqs/HW1_125264/
  Controllers/      REST endpoints
  Services/         Business logic
  Entities/         JPA entities and parcel state machine
  Repositories/     Spring Data JPA repositories
  DTOs/             API payloads
  GeoCoding/        Geocoding port and Nominatim adapter
  LockerHardwareSim/Locker hardware simulation
  Schedulers/       Expiry scheduler
src/main/resources/ application.properties, application-test.properties, data.sql
src/test/java/      Unit, integration, Cucumber and Playwright tests
src/test/resources/features/ Cucumber feature files
frontend/           React + TypeScript + Vite UI
load/               k6 load test
```
