import { sleep } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 4차 테스트: 가상 스레드 기반에서 중부하 구간(10~30 VU)의 임계점을 찾는다.
// 각 단계는 "1분 램프업 + 2분 유지"로 구성해 60초 stub 환경에서도 비교 가능한 샘플을 확보한다.
export const options = {
  scenarios: {
    step_mid_load_v4: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 10 },
        { duration: '2m', target: 10 },
        { duration: '1m', target: 15 },
        { duration: '2m', target: 15 },
        { duration: '1m', target: 20 },
        { duration: '2m', target: 20 },
        { duration: '1m', target: 25 },
        { duration: '2m', target: 25 },
        { duration: '1m', target: 30 },
        { duration: '2m', target: 30 },
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
  const response = requestGenerateImage({ scenario: 'step_mid_load_v4' });
  checkGenerateImageResponse(response);
  // 요청 직후 즉시 재호출하지 않도록 1초 간격을 둔다.
  sleep(1);
}
