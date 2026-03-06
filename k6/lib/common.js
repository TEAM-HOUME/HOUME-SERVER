import http from 'k6/http';
import { check, fail } from 'k6';

// 별도 payload 주입이 없을 때 사용하는 기본 이미지 생성 요청값.
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

// BASE_URL/TARGET_PATH를 분리해 시나리오 파일 변경 없이 대상 서버만 바꿔 실행할 수 있게 한다.
export const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
export const TARGET_PATH = __ENV.TARGET_PATH || '/api/v3/generated-images/generate/gemini';

// ACCESS_TOKENS는 쉼표로 여러 토큰을 입력할 수 있고, ACCESS_TOKEN은 단일 토큰 실행용이다.
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

  // 각 VU가 고정된 토큰을 재사용하도록 분산해 인증 요청 부하를 줄인다.
  const index = (__VU - 1) % TOKENS.length;
  return TOKENS[index];
}

export function authParams(extraTags = {}) {
  return {
    headers: {
      Authorization: `Bearer ${pickToken()}`,
      'Content-Type': 'application/json',
    },
    // 시나리오/엔드포인트 태그를 붙여 k6 결과에서 필터링하기 쉽게 만든다.
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
  // 상태코드 + ApiResponse.code(200)를 동시에 확인해 애플리케이션 레벨 성공까지 검증한다.
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
