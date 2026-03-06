import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 동접을 단계적으로 올려 병목이 시작되는 구간(10->50->100->150 VU)을 찾는 시나리오.
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
  // 각 VU가 요청 간 최소 간격을 두고 반복하도록 고정한다.
  sleep(1);
}
