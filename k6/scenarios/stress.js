import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 목표 동접(150)보다 높은 부하(200)까지 밀어 시스템 한계 지점을 확인한다.
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
  // 고부하에서도 요청 패턴을 일정하게 유지하기 위한 간격.
  sleep(1);
}
