# Load test: seat race

[k6](https://k6.io) scenario that proves the two-stage seat locking holds under load: `USERS` authenticated users hit
`POST /api/sessions/{id}/seats/{seatId}/hold` for the same seat at the same time, for `ROUNDS` different seats.

Pass criteria (k6 thresholds):

- `hold_won == ROUNDS` — exactly one winner per seat
- `hold_error == 0` — every loser gets a clean `409`, no `5xx`
- `hold_duration p(95) < 1000ms` — the latency reflects worst-case lock contention (200 users waiting for the same row lock) on a local machine, not normal request speed

Then `verify.sql` checks the database directly: no seat has more than one active reservation.

## Run

```bash
brew install k6
docker compose -f docker-compose.yml -f load-test/docker-compose.loadtest.yml up -d
docker compose exec -T postgres psql -U cinema -d cinema_db < load-test/seed-users.sql
k6 run load-test/seat-race.js
docker compose exec -T postgres psql -U cinema -d cinema_db < load-test/verify.sql
docker compose exec -T postgres psql -U cinema -d cinema_db < load-test/cleanup.sql
```

The target session is the first upcoming one with enough free seats (create one in the admin panel if there is
none), or pass `-e SESSION_ID=<publicId>`. Tunables: `-e USERS=200 -e ROUNDS=10 -e BASE_URL=http://localhost:8080`.
`USERS` is capped at 500 by `seed-users.sql`, `ROUNDS` at 30 by the per-user hold rate limit.

`docker-compose.loadtest.yml` sets `APP_RATE_LIMIT_CLIENT_IP_HEADER` so each virtual user gets its own login rate-limit
bucket; never set it in production, the header is client-controlled.
