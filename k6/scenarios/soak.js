import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

export const options = {
  scenarios: {
    soak: {
      executor: 'constant-vus',
      vus: 100,
      duration: '2h',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<130000', 'p(99)<170000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'soak' });
  checkGenerateImageResponse(response);
  sleep(1);
}
