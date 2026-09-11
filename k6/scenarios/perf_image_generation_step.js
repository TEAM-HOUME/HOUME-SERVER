import http from 'k6/http';
import { check, fail, sleep } from 'k6';

// perf 전용 시나리오다. 실수로 운영 환경에 요청을 보내지 않도록 명시적인 확인값을 요구한다.
if ((__ENV.CONFIRM_PERF || '').toLowerCase() !== 'true') {
  fail('perf 환경 실행 확인을 위해 CONFIRM_PERF=true를 설정하세요.');
}

const BASE_URL = (__ENV.BASE_URL || 'http://13.209.178.230:8080').replace(/\/$/, '');
const IMAGE_API = __ENV.IMAGE_API || 'v4';
const PERF_USER_ID = __ENV.PERF_USER_ID || '557';
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '90s';
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || '1');
const TEST_MODE = __ENV.TEST_MODE || 'step';

const API_PATHS = {
  v4: '/api/v4/generated-images/generate',
  banner: '/api/v1/generated-images/generate/banner',
  otherStyle: '/api/v1/generated-images/generate/other-style',
  products: '/api/v1/generated-images/generate/products',
};

// perf DB에 seed된 값이다. 외부 환경에서는 GENERATE_IMAGE_PAYLOAD로 명시적으로 교체한다.
const DEFAULT_PAYLOADS = {
  v4: {
    floorPlanId: 6,
    floorPlanView: '기본 뷰',
    isMirror: false,
    moodBoardIds: [1, 3, 11, 12],
    activity: 'REMOTE_WORK',
    furnitureIds: [1, 5, 7, 8, 17],
  },
  banner: {
    bannerId: 20,
    answerId: 1,
    floorPlanId: 6,
    floorPlanView: '기본 뷰',
    isMirror: false,
  },
  otherStyle: {
    bannerId: 10,
    floorPlanId: 6,
    floorPlanView: '기본 뷰',
    isMirror: false,
  },
  products: {
    floorPlanId: 6,
    floorPlanView: '기본 뷰',
    isMirror: false,
    productIds: [277, 278, 279, 280, 281, 282],
  },
};

function parsePayload() {
  if (!API_PATHS[IMAGE_API]) {
    fail(`지원하지 않는 IMAGE_API=${IMAGE_API}. v4, banner, otherStyle, products 중 하나를 사용하세요.`);
  }

  const rawPayload = __ENV.GENERATE_IMAGE_PAYLOAD;
  if (!rawPayload && DEFAULT_PAYLOADS[IMAGE_API]) {
    return DEFAULT_PAYLOADS[IMAGE_API];
  }
  if (!rawPayload) {
    fail(`${IMAGE_API} API는 GENERATE_IMAGE_PAYLOAD JSON 환경변수가 필요합니다.`);
  }

  try {
    return JSON.parse(rawPayload);
  } catch (error) {
    fail(`GENERATE_IMAGE_PAYLOAD JSON 파싱 실패: ${error.message}`);
  }
}

const TARGET_PATH = API_PATHS[IMAGE_API];
const REQUEST_PAYLOAD = parsePayload();

export const options = TEST_MODE === 'smoke'
  ? {
      scenarios: {
        perf_smoke: {
          executor: 'per-vu-iterations',
          vus: 1,
          iterations: 1,
          maxDuration: REQUEST_TIMEOUT,
        },
      },
      thresholds: {
        http_req_failed: ['rate==0'],
      },
    }
  : {
      scenarios: {
        perf_step: {
          executor: 'ramping-vus',
          startVUs: 0,
          stages: [
            { duration: '1m', target: 1 },
            { duration: '2m', target: 1 },
            { duration: '1m', target: 3 },
            { duration: '3m', target: 3 },
            { duration: '1m', target: 5 },
            { duration: '3m', target: 5 },
          ],
          gracefulRampDown: '30s',
        },
      },
      thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<45000', 'p(99)<60000'],
      },
    };

function getAccessToken(response) {
  const headerName = Object.keys(response.headers)
    .find((name) => name.toLowerCase() === 'access-token');
  return headerName ? response.headers[headerName] : null;
}

export function setup() {
  const response = http.get(`${BASE_URL}/access?userId=${PERF_USER_ID}`, {
    tags: { endpoint: '/access', scenario: 'perf-token' },
    timeout: '10s',
  });
  const accessToken = getAccessToken(response);

  if (response.status !== 200 || !accessToken) {
    fail(`perf 테스트 토큰 발급 실패: HTTP ${response.status}`);
  }
  return { accessToken };
}

export default function (data) {
  const response = http.post(
    `${BASE_URL}${TARGET_PATH}`,
    JSON.stringify(REQUEST_PAYLOAD),
    {
      headers: {
        Authorization: `Bearer ${data.accessToken}`,
        'Content-Type': 'application/json',
      },
      tags: { endpoint: TARGET_PATH, image_api: IMAGE_API, scenario: `perf-${TEST_MODE}` },
      timeout: REQUEST_TIMEOUT,
    },
  );

  check(response, {
    'status is 200': (result) => result.status === 200,
    'response has success code': (result) => {
      try {
        return JSON.parse(result.body).code === 200;
      } catch (error) {
        return false;
      }
    },
  });

  if (THINK_TIME_SECONDS > 0) {
    sleep(THINK_TIME_SECONDS);
  }
}
