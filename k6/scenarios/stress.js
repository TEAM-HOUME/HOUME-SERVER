import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

export const options = {
  scenarios: {
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '3m', target: 150 },
        { duration: '10m', target: 200 },
        { duration: '2m', target: 0 },
      ],
      gracefulRampDown: '1m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<170000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'stress' });
  checkGenerateImageResponse(response);
  sleep(1);
}
