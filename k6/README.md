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
- `GENERATE_IMAGE_PAYLOAD` (JSON 문자열, 미지정 시 기본 payload 사용)
- `CONFIRM_REAL_GEMINI=true` (`gemini_canary_real.js` 실행 시 필수)

## 실행 순서
1. `k6 run k6/scenarios/smoke.js`
2. `k6 run k6/scenarios/ramp.js`
3. `k6 run k6/scenarios/stress.js`
4. `k6 run k6/scenarios/soak.js`
5. `k6 run k6/scenarios/gemini_canary_real.js` (실제 Gemini 소량 검증)

## 예시
```bash
BASE_URL=https://loadtest.houme.kr \
ACCESS_TOKENS="token1,token2,token3" \
k6 run k6/scenarios/smoke.js
```

```bash
BASE_URL=https://loadtest.houme.kr \
ACCESS_TOKEN="your_token" \
CONFIRM_REAL_GEMINI=true \
k6 run k6/scenarios/gemini_canary_real.js
```
