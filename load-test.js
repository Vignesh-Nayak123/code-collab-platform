import http from 'k6/http';
import { check } from 'k6';

export const options = { vus: 20, duration: '30s' };

export default function () {
    const r = http.get('http://localhost:8080/api/v1/rooms/R45VVJCJ');
    check(r, { 'HTTP 200': (res) => res.status === 200 });
}