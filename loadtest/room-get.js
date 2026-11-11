import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || '30s';
const ROOM_CODE = __ENV.ROOM_CODE || 'R45VVJCJ';

export const options = {
  vus: VUS,
  duration: DURATION,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
};

export default function () {
  const r = http.get(`${BASE_URL}/api/v1/rooms/${ROOM_CODE}`);
  check(r, {
    'HTTP 200': (res) => res.status === 200,
    'valid room payload': (res) => {
      if (res.status !== 200) return false;
      try {
        return res.json('roomCode') === ROOM_CODE;
      } catch (e) {
        return false;
      }
    },
  });
}
