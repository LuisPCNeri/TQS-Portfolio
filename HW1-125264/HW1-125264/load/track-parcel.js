import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('error_rate');

export let options = {
    vus: 20,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<200'],
        http_req_failed:   ['rate<0.05'],
        error_rate:        ['rate<0.05'],
    },
};

export function setup() {
    // Verifique se os campos correspondem exatamente ao seu DTO/Controller
    const payload = JSON.stringify({
        size: 'M',
        recipientContact: 'load@test.com',
    });

    // Se o seu endpoint exigir stationId nos query params:
    const res = http.post('http://localhost:8080/api/v1/parcels?stationId=1', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    console.log(`[SETUP] Status POST: ${res.status}`);
    console.log(`[SETUP] Body POST: ${res.body}`);

    if (res.status !== 201 && res.status !== 200) {
        throw new Error(`Falha ao criar encomenda inicial no setup: ${res.status} ${res.body}`);
    }

    const body = JSON.parse(res.body);
    return { trackingCode: body.trackingCode };
}

export default function (data) {
    const res = http.get(`http://localhost:8080/api/v1/parcels/${data.trackingCode}`);

    if (res.status !== 200) {
        console.log(`[TEST] GET falhou (${res.status}): ${res.body}`);
    }

    const success = res.status === 200;
    errorRate.add(!success);

    check(res, {
        'status is 200 OK': () => success,
        'response time < 200ms': () => res.timings.duration < 200,
    });

    sleep(0.5);
}
