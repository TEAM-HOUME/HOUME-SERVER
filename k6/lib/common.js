import http from 'k6/http';
import { check, fail } from 'k6';

const DEFAULT_PAYLOAD = {
  houseId: 1813,
  equilibrium: 'UNDER_5',
  floorPlan: {
    floorPlanId: 1,
    isMirror: true,
  },
  moodBoardIds: [1],
  activity: 'REMOTE_WORK',
  selectiveIds: [1, 2, 3],
};

export const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
export const TARGET_PATH = __ENV.TARGET_PATH || '/api/v3/generated-images/generate/gemini';

const TOKENS = (__ENV.ACCESS_TOKENS || __ENV.ACCESS_TOKEN || '')
  .split(',')
  .map((token) => token.trim())
  .filter((token) => token.length > 0);

function parsePayload() {
  const rawPayload = __ENV.GENERATE_IMAGE_PAYLOAD;
  if (!rawPayload) {
    return DEFAULT_PAYLOAD;
  }

  try {
    return JSON.parse(rawPayload);
  } catch (e) {
    fail(`GENERATE_IMAGE_PAYLOAD JSON 파싱 실패: ${e.message}`);
  }
}

export const REQUEST_PAYLOAD = parsePayload();

export function pickToken() {
  if (TOKENS.length === 0) {
    fail('ACCESS_TOKEN 또는 ACCESS_TOKENS 환경변수는 필수입니다.');
  }

  const index = (__VU - 1) % TOKENS.length;
  return TOKENS[index];
}

export function authParams(extraTags = {}) {
  return {
    headers: {
      Authorization: `Bearer ${pickToken()}`,
      'Content-Type': 'application/json',
    },
    tags: {
      endpoint: TARGET_PATH,
      ...extraTags,
    },
  };
}

export function requestGenerateImage(extraTags = {}) {
  return http.post(
    `${BASE_URL}${TARGET_PATH}`,
    JSON.stringify(REQUEST_PAYLOAD),
    authParams(extraTags),
  );
}

export function checkGenerateImageResponse(response) {
  return check(response, {
    'status is 200': (r) => r.status === 200,
    'response has success code': (r) => {
      if (!r.body) {
        return false;
      }

      try {
        const parsed = JSON.parse(r.body);
        return parsed.code === 200;
      } catch (e) {
        return false;
      }
    },
  });
}
