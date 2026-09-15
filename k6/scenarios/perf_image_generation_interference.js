import http from 'k6/http';
import { check, fail } from 'k6';
import exec from 'k6/execution';
import { Rate, Trend } from 'k6/metrics';

if ((__ENV.CONFIRM_PERF || '').toLowerCase() !== 'true') {
  fail('perf 환경 실행 확인을 위해 CONFIRM_PERF=true를 설정하세요.');
}

const BASE_URL = (__ENV.BASE_URL || 'http://13.209.178.230:8080').replace(/\/$/, '');
const TEST_MODE = __ENV.TEST_MODE || 'baseline';
const PROBE_PATH = __ENV.PROBE_PATH || '/api/v1/landings';
const PROBE_RATE = Number(__ENV.PROBE_RATE || '4');
const PROBE_DURATION = __ENV.PROBE_DURATION || '75s';
const IMAGE_START_DELAY = __ENV.IMAGE_START_DELAY || '15s';
const IMAGE_VUS = Number(__ENV.IMAGE_VUS || '5');
const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '240s';
const PERF_USER_IDS = (__ENV.PERF_USER_IDS || '557,558,559,560,561')
  .split(',')
  .map((userId) => userId.trim())
  .filter((userId) => userId.length > 0);

if (!['baseline', 'interference'].includes(TEST_MODE)) {
  fail('TEST_MODE는 baseline 또는 interference여야 합니다.');
}
if (TEST_MODE === 'interference' && PERF_USER_IDS.length < IMAGE_VUS) {
  fail(`IMAGE_VUS=${IMAGE_VUS}에 필요한 PERF_USER_IDS가 부족합니다.`);
}

const probeLatency = new Trend('probe_latency', true);
const probeFailed = new Rate('probe_failed');
const imageGenerationDuration = new Trend('image_generation_duration', true);
const imageGenerationFailed = new Rate('image_generation_failed');

const V4_PAYLOAD = {
  floorPlanId: 6,
  floorPlanView: '기본 뷰',
  isMirror: false,
  moodBoardIds: [1, 3, 11, 12],
  activity: 'REMOTE_WORK',
  furnitureIds: [1, 5, 7, 8, 17],
};

const scenarios = {
  probe: {
    executor: 'constant-arrival-rate',
    exec: 'runProbe',
    rate: PROBE_RATE,
    timeUnit: '1s',
    duration: PROBE_DURATION,
    preAllocatedVUs: Math.max(2, PROBE_RATE),
    maxVUs: Math.max(4, PROBE_RATE * 2),
  },
};

if (TEST_MODE === 'interference') {
  scenarios.image_generation = {
    executor: 'per-vu-iterations',
    exec: 'runImageGeneration',
    vus: IMAGE_VUS,
    iterations: 1,
    startTime: IMAGE_START_DELAY,
    maxDuration: REQUEST_TIMEOUT,
  };
}

export const options = {
  scenarios,
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  thresholds: TEST_MODE === 'interference'
    ? {
        probe_failed: ['rate==0'],
        dropped_iterations: ['count<10'],
        image_generation_failed: ['rate==0'],
        image_generation_duration: ['p(95)<45000'],
      }
    : {
        probe_failed: ['rate==0'],
        dropped_iterations: ['count<10'],
      },
};

function getAccessToken(response) {
  const headerName = Object.keys(response.headers)
    .find((name) => name.toLowerCase() === 'access-token');
  return headerName ? response.headers[headerName] : null;
}

export function setup() {
  if (TEST_MODE === 'baseline') {
    return {};
  }

  const accessTokens = PERF_USER_IDS.slice(0, IMAGE_VUS).map((userId) => {
    const response = http.get(`${BASE_URL}/access?userId=${userId}`, { timeout: '10s' });
    const accessToken = getAccessToken(response);
    if (response.status !== 200 || !accessToken) {
      fail(`perf 테스트 토큰 발급 실패(userId=${userId}): HTTP ${response.status}`);
    }
    return accessToken;
  });
  return { accessTokens };
}

export function runProbe() {
  const response = http.get(`${BASE_URL}${PROBE_PATH}`, {
    tags: { endpoint: PROBE_PATH, scenario: `probe-${TEST_MODE}` },
    timeout: '10s',
  });
  const succeeded = check(response, {
    'probe status is 200': (result) => result.status === 200,
  });
  probeLatency.add(response.timings.duration);
  probeFailed.add(!succeeded);
}

export function runImageGeneration(data) {
  // Probe 시나리오의 VU도 전역 __VU를 점유하므로, 이미지 시나리오 내에서만 고유한 iteration 번호를 사용한다.
  const accessToken = data.accessTokens[exec.scenario.iterationInTest % data.accessTokens.length];
  const response = http.post(
    `${BASE_URL}/api/v4/generated-images/generate`,
    JSON.stringify(V4_PAYLOAD),
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      tags: { endpoint: '/api/v4/generated-images/generate', scenario: 'image-interference' },
      timeout: REQUEST_TIMEOUT,
    },
  );
  const succeeded = check(response, {
    'image status is 200': (result) => result.status === 200,
    'image response has success code': (result) => {
      try {
        return JSON.parse(result.body).code === 200;
      } catch (error) {
        return false;
      }
    },
  });
  imageGenerationDuration.add(response.timings.duration);
  imageGenerationFailed.add(!succeeded);
}
