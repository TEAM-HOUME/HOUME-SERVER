# k6 Load Test Scenarios (Issue #434)

## 목적
- 1차 부하테스트 환경에서 Gemini 이미지 생성 API의 동접/지연 특성을 검증합니다.
- `load_test` 프로필에서는 Gemini API를 Stub으로 대체해 비용 없이 테스트합니다.
- 실제 Gemini 호출은 `gemini_canary_real.js`에서만 소량으로 검증합니다.

## 사전 준비
1. 테스트 서버 실행: `--spring.profiles.active=load_test`
2. 부하테스트용 계정 토큰 준비
3. k6 설치

## 환경변수
- `BASE_URL` (기본: `http://localhost:8080`)
- `TARGET_PATH` (기본: `/api/v3/generated-images/generate/gemini`)
- `ACCESS_TOKEN` 또는 `ACCESS_TOKENS`(쉼표 구분)
- `REQUEST_TIMEOUT` (기본: `220s`, Gemini Stub 지연 대비)
- `GENERATE_IMAGE_PAYLOAD` (JSON 문자열, 미지정 시 기본 payload 사용)
- `CONFIRM_REAL_GEMINI=true` (`gemini_canary_real.js` 실행 시 필수)

## 데이터 시드
`/api/v3/generated-images/generate/gemini` 부하테스트는 house/moodboard/tag/floorplan/credit이 비어 있으면 실패합니다.
아래 스크립트로 최소 데이터를 채운 뒤 실행합니다.

```bash
DB_HOST=YOUR_DB_HOST \
DB_PORT=5432 \
DB_NAME=houme \
DB_USER=root \
DB_PASSWORD=YOUR_PASSWORD \
SEED_USER_START=1 \
SEED_USER_END=200 \
CREDITS_PER_USER=300 \
HOUSE_ID=1813 \
HOUSE_OWNER_ID=1 \
bash k6/scripts/seed_load_test_data.sh
```

기본값으로 `houseId=1813`, `floorPlanId=1`, `moodBoardIds=[1]` 요청이 바로 동작하도록 맞춰집니다.

## 실행 순서
1. `k6 run k6/scenarios/smoke.js`
2. `k6 run k6/scenarios/ramp.js`
3. `k6 run k6/scenarios/stress.js`
4. `k6 run k6/scenarios/soak.js`
5. `k6 run k6/scenarios/gemini_canary_real.js` (실제 Gemini 소량 검증)

## 2차 테스트(저부하 스텝)
- 시나리오: `k6/scenarios/step_low_load_v2.js`
- 구간: `1 -> 2 -> 4 -> 6 -> 8 -> 10 VU`
- 구성: 각 구간 `1분 램프업 + 5분 유지`
- 목적: 락 경합/풀 고갈이 시작되는 임계점을 저부하에서 재측정

```bash
BASE_URL=http://YOUR_LOADTEST_HOST \
ACCESS_TOKENS="token1,token2,token3,token4,token5,token6,token7,token8,token9,token10" \
REQUEST_TIMEOUT=220s \
k6 run k6/scenarios/step_low_load_v2.js
```

## 3차 테스트(압축 저부하 스텝)
- 시나리오: `k6/scenarios/step_low_load_v3.js`
- 구간: `1 -> 2 -> 4 -> 6 -> 8 -> 10 VU`
- 구성: 각 구간 `1분 램프업 + 2분 유지`
- 목적: Stub 지연을 `10초`로 낮춘 상태에서 애플리케이션 순수 처리 병목을 더 짧은 시간 안에 재측정

```bash
BASE_URL=http://YOUR_LOADTEST_HOST \
ACCESS_TOKENS="token1,token2,token3,token4,token5,token6,token7,token8,token9,token10" \
REQUEST_TIMEOUT=120s \
k6 run k6/scenarios/step_low_load_v3.js
```

## 예시
```bash
BASE_URL=https://loadtest.houme.kr \
ACCESS_TOKENS="token1,token2,token3" \
REQUEST_TIMEOUT=220s \
k6 run k6/scenarios/smoke.js
```

```bash
BASE_URL=https://loadtest.houme.kr \
ACCESS_TOKEN="your_token" \
CONFIRM_REAL_GEMINI=true \
k6 run k6/scenarios/gemini_canary_real.js
```

## 테스트 결과 아카이브
- 1차 테스트 묶음: `k6/1차테스트`
- 1-1 테스트(기존 1차): `k6/1차테스트/1-1차테스트`
- 1-2 테스트(기존 2차): `k6/1차테스트/1-2차테스트`
- 1-3 테스트(기존 3차): `k6/1차테스트/1-3차테스트`
- 2차 테스트 묶음: `k6/2차테스트`
- 2-1 테스트(비동기 스레드풀 확장 후): `k6/2차테스트/2-1차테스트`
