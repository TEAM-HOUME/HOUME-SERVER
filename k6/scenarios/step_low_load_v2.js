import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 2차 테스트: 저부하 구간에서 임계점을 찾기 위해 VU를 단계적으로 올린다.
// 각 단계는 "1분 램프업 + 5분 유지"로 구성한다.
export const options = {
  scenarios: {
    step_low_load_v2: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 1 },
        { duration: '5m', target: 1 },
        { duration: '1m', target: 2 },
        { duration: '5m', target: 2 },
        { duration: '1m', target: 4 },
        { duration: '5m', target: 4 },
        { duration: '1m', target: 6 },
        { duration: '5m', target: 6 },
        { duration: '1m', target: 8 },
        { duration: '5m', target: 8 },
        { duration: '1m', target: 10 },
        { duration: '5m', target: 10 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<90000', 'p(99)<120000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'step_low_load_v2' });
  checkGenerateImageResponse(response);
  // 이미지 생성 API 특성상 과도한 루프 폭주를 막기 위해 간격을 둔다.
  sleep(1);
}
