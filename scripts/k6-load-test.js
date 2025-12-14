import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 20,
  duration: '60s',
};

export default function () {
  const res = http.get('http://host.docker.internal:8080/api/doctors?latitude=48.8566&longitude=2.3522&radiusKm=10');
  check(res, { 'status was 200': (r) => r.status === 200 });
  sleep(1);
}
