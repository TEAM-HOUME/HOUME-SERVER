import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10m', target: 10 },
        { duration: '10m', target: 50 },
        { duration: '10m', target: 100 },
        { duration: '10m', target: 150 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<130000', 'p(99)<170000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'ramp' });
  checkGenerateImageResponse(response);
  sleep(1);
}
