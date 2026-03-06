import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 5,
      duration: '5m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<130000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'smoke' });
  checkGenerateImageResponse(response);
  sleep(1);
}
