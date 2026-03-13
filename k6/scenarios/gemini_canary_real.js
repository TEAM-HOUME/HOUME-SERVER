import { sleep, fail } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

// 실제 Gemini 비용이 발생하는 테스트이므로 명시적 승인 플래그가 없으면 즉시 중단한다.
if ((__ENV.CONFIRM_REAL_GEMINI || '').toLowerCase() !== 'true') {
  fail('실제 Gemini 호출 비용 방지를 위해 CONFIRM_REAL_GEMINI=true 설정이 필요합니다.');
}

// 소량 트래픽으로만 실제 외부 연동 상태를 점검하는 Canary 시나리오.
export const options = {
  scenarios: {
    gemini_canary_real: {
      executor: 'constant-vus',
      vus: 3,
      duration: '15m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<170000'],
  },
};

export default function () {
  const response = requestGenerateImage({ scenario: 'gemini_canary_real', mode: 'real' });
  checkGenerateImageResponse(response);
  // 실제 호출 비용을 통제하기 위해 요청 빈도를 제한한다.
  sleep(1);
}
