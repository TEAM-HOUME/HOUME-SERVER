import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 3차 테스트: 10초 stub 환경에서 저부하 임계점을 더 짧은 유지시간으로 재측정한다.
export const options = {
  scenarios: {
    step_low_load_v3: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 1 },
        { duration: '2m', target: 1 },
        { duration: '1m', target: 2 },
        { duration: '2m', target: 2 },
        { duration: '1m', target: 4 },
        { duration: '2m', target: 4 },
        { duration: '1m', target: 6 },
        { duration: '2m', target: 6 },
        { duration: '1m', target: 8 },
        { duration: '2m', target: 8 },
        { duration: '1m', target: 10 },
        { duration: '2m', target: 10 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<30000', 'p(99)<45000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'step_low_load_v3' });
  checkGenerateImageResponse(response);
  // 각 VU가 응답 직후 즉시 재요청하지 않도록 1초 간격을 둔다.
  sleep(1);
}
