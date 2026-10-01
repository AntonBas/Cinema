import http from 'k6/http';
import { check, fail } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USERS = Number(__ENV.USERS || 200);
const ROUNDS = Number(__ENV.ROUNDS || 10);
const PASSWORD = __ENV.PASSWORD || 'user';
const LOGIN_BATCH_SIZE = 20;
const CLIENT_KEY_HEADER = 'X-Load-Test-Client';
const CSRF_HEADER = { 'X-Requested-With': 'XMLHttpRequest' };

const holdWon = new Counter('hold_won');
const holdConflict = new Counter('hold_conflict');
const holdError = new Counter('hold_error');
const holdDuration = new Trend('hold_duration', true);

export const options = {
    setupTimeout: '5m',
    scenarios: {
        seat_race: {
            executor: 'per-vu-iterations',
            vus: USERS,
            iterations: ROUNDS,
            maxDuration: '5m',
        },
    },
    thresholds: {
        hold_won: [`count==${ROUNDS}`],
        hold_error: ['count==0'],
        hold_duration: ['p(95)<1000'],
    },
};

function findSession() {
    if (__ENV.SESSION_ID) {
        return __ENV.SESSION_ID;
    }
    const response = http.get(`${BASE_URL}/api/sessions`);
    if (response.status !== 200) {
        fail(`GET /api/sessions returned ${response.status}`);
    }
    const now = Date.now();
    const session = response.json()
        .filter((candidate) => new Date(candidate.startTime).getTime() > now)
        .find((candidate) => candidate.availableSeats >= ROUNDS);
    if (!session) {
        fail(`No upcoming session with at least ${ROUNDS} free seats; create one in the admin panel or pass SESSION_ID`);
    }
    return session.publicId;
}

function findTargetSeats(sessionId) {
    const response = http.get(`${BASE_URL}/api/sessions/${sessionId}/seats`);
    if (response.status !== 200) {
        fail(`GET /api/sessions/${sessionId}/seats returned ${response.status}`);
    }
    const seats = response.json('seats').filter((seat) => seat.available).map((seat) => seat.id);
    if (seats.length < ROUNDS) {
        fail(`Session ${sessionId} has only ${seats.length} free seats, need ${ROUNDS}`);
    }
    return seats.slice(0, ROUNDS);
}

function loginAll() {
    const tokens = [];
    for (let start = 1; start <= USERS; start += LOGIN_BATCH_SIZE) {
        const requests = [];
        for (let index = start; index < start + LOGIN_BATCH_SIZE && index <= USERS; index++) {
            requests.push({
                method: 'POST',
                url: `${BASE_URL}/api/auth/login`,
                body: JSON.stringify({ email: `loadtest-${index}@test.com`, password: PASSWORD }),
                params: {
                    headers: { ...CSRF_HEADER, 'Content-Type': 'application/json', [CLIENT_KEY_HEADER]: `loadtest-${index}` },
                    tags: { name: 'login' },
                },
            });
        }
        http.batch(requests).forEach((response, offset) => {
            const jwt = response.cookies.jwt && response.cookies.jwt[0];
            if (response.status !== 200 || !jwt) {
                fail(`Login failed for loadtest-${start + offset}@test.com: ${response.status}; run seed-users.sql first`);
            }
            tokens.push(jwt.value);
        });
    }
    return tokens;
}

export function setup() {
    const sessionId = findSession();
    const seatIds = findTargetSeats(sessionId);
    const tokens = loginAll();
    console.info(`Session ${sessionId}: ${USERS} users racing for seats ${seatIds.join(', ')}`);
    return { sessionId, seatIds, tokens };
}

export default function (data) {
    const token = data.tokens[__VU - 1];
    const seatId = data.seatIds[__ITER];
    const response = http.post(`${BASE_URL}/api/sessions/${data.sessionId}/seats/${seatId}/hold`, null, {
        headers: { ...CSRF_HEADER, Cookie: `jwt=${token}` },
        tags: { name: 'hold' },
        responseCallback: http.expectedStatuses(200, 409),
    });

    holdDuration.add(response.timings.duration);
    if (response.status === 200) {
        holdWon.add(1);
    } else if (response.status === 409) {
        holdConflict.add(1);
    } else {
        holdError.add(1);
    }
    check(response, { 'hold returns 200 or 409': (res) => res.status === 200 || res.status === 409 });
}
