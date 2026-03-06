import { sleep, fail } from 'k6';
import { checkGenerateImageResponse, requestGenerateImage } from '../lib/common.js';

if ((__ENV.CONFIRM_REAL_GEMINI || '').toLowerCase() !== 'true') {
  fail('실제 Gemini 호출 비용 방지를 위해 CONFIRM_REAL_GEMINI=true 설정이 필요합니다.');
}

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
  sleep(1);
}
