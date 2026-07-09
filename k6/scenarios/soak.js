import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 장시간(2h) 동안 안정적으로 버티는지 확인하는 내구성 시나리오.
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
  // 장시간 테스트에서 일정한 요청 리듬을 유지한다.
  sleep(1);
}
