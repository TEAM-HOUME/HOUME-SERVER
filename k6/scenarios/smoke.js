import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 기본 연결/인증/응답 구조가 정상인지 빠르게 확인하는 사전 점검 시나리오.
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
  // 과도한 루프 폭주를 막고 실제 사용자 간격과 유사한 템포를 만든다.
  sleep(1);
}
