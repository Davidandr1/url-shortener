# SwiftURL: Link Shortener

A full-stack URL shortener built the way a production service would be. Users register, log in, and turn long links into short Base62 codes. Redirects go through a Redis-cached lookup, and every click is published to RabbitMQ and saved asynchronously, so redirects stay fast even when the database is slow. Links stop working after 72 hours.


## Website Preview
<img width="1061" height="462" alt="Screenshot 2026-11-06 at 13 05 38" src="https://github.com/user-attachments/assets/8016db44-fa2c-41b1-b80e-853ae0f95722" />
<img width="1064" height="516" alt="Screenshot 2026-11-06 at 13 06 18" src="https://github.com/user-attachments/assets/d99e411a-bacd-4763-8631-54949fd36546" />


## Tech Stack

- **Backend:** Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA
- **Data and messaging:** PostgreSQL, Redis (cache), RabbitMQ (queue)
- **Frontend:** React (Vite)
- **Infrastructure:** Docker, Docker Compose


## Features

- Registration and login with JWT authentication
- Base62 short codes
- 72-hour expiry (404 for unknown codes, 410 for expired links)
- Redis-cached redirects
- Async click tracking through RabbitMQ
- Hashed visitor IPs (HMAC-SHA256), so raw addresses are never stored


## Run Locally (Docker)

Requires Docker.

1. Clone the repo.
2. Copy `.env.example` to `.env` in the **repo root** and fill in the values (no spaces around `=`):
   ```
   DB_PASSWORD=choose-a-password
   JWT_SECRET=<generate with: openssl rand -base64 129 | tr -d '\n'>
   IP_HASH_SECRET=<generate with: openssl rand -base64 32>
   ```
3. Start everything:
   ```bash
   docker compose up --build
   ```
4. Open http://localhost:3000.


## Website Diagram
<img width="1124" height="248" alt="Screenshot 2026-11-06 at 13 29 44" src="https://github.com/user-attachments/assets/bc36cbda-df48-4d4e-bf99-633739b0dabb" />


## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/user/register` | Public | Create an account (`email`, `password` as query params) |
| POST | `/user/login` | Public | Log in with JSON `{email, password}`, returns `{token}` |
| POST | `/links` | Bearer token | Create a short link from JSON `{url}` |
| GET | `/links` | Bearer token | List the logged-in user's links |
| GET | `/s/{code}` | Public | Redirect to the original URL. Returns 404 for unknown codes and 410 for expired links |

## Design Decisions and Tradeoffs

**Redis cache on redirects.** Every redirect needs a `shortCode -> originalUrl` lookup, so this is the hot path. Caching it in Redis keeps most redirects off Postgres. Entries have a 1 hour TTL. The expiry check only runs on a cache miss, so a cached link can keep working for up to an hour after it expires.

**RabbitMQ for click tracking.** The redirect handler publishes a click event and returns immediately. A separate consumer writes it to Postgres. Redirects stay fast and keep working if the database is slow, and traffic spikes queue up instead of hitting the database. The tradeoffs are that click data is eventually consistent, and a message that fails in the consumer is discarded instead of retried forever, so a click event can occasionally be lost.

**Base62 of the database id.** The short code is the row id encoded in Base62, so codes are unique without any collision handling. The tradeoff is that codes are sequential and guessable, so one link's code reveals its neighbors.

**HMAC-hashed IP addresses.** Visitor IPs are stored as HMAC-SHA256 hashes with a secret key. The same visitor produces the same hash, so unique visitors can still be counted, but raw IPs are never stored. The key matters because an unkeyed hash of an IPv4 address can be brute-forced quickly.

**Stateless JWT auth.** The API keeps no server-side sessions. Each request carries a signed token, which makes it easy to scale horizontally.

## Known Limitations and Next Steps

- Schema is managed with `ddl-auto: update`. A production setup would use Flyway or Liquibase migrations.
- The JWT is stored in `localStorage`, which is exposed to XSS. HttpOnly cookies would be safer.
- No rate limiting yet. Planned with Bucket4j on link creation.
- No automated tests beyond the default context test. Integration tests for register, login, create and redirect are planned.
- All users share one hardcoded tenant id, so multi-tenancy is modeled in the schema but not implemented.
- Click events are collected but not yet shown in the UI.

## Project Structure

```
.
├── backend/
│   ├── src/main/java/com/davidandriychuk/url_shortener/
│   │   ├── controller/   REST endpoints
│   │   ├── service/      business logic (cached lookup)
│   │   ├── repository/   Spring Data JPA repositories
│   │   ├── entity/       User, Link, ClickEvent
│   │   ├── security/     JWT filter and generator
│   │   ├── messaging/    RabbitMQ click consumer
│   │   ├── config/       RabbitMQ and CORS configuration
│   │   ├── dto/          request and response objects
│   │   └── util/         IP hashing
│   ├── Dockerfile
│   └── build.gradle
├── frontend/             React app, Dockerfile, nginx.conf
├── docker-compose.yml    app, frontend, Postgres, Redis, RabbitMQ
└── .env.example
```

