# Perf 이미지 생성 부하 테스트

## 목적

- 부하는 로컬 Mac에서 발생시키고, perf EC2의 JVM·GC·Hikari·호스트 메모리 지표를 Grafana에서 관측한다.
- perf 환경은 `load_test` Gemini Stub(약 30초)을 사용한다. 따라서 이 단계는 외부 Gemini 비용 없이 동시 장시간 요청에 대한 서버 자원 한계를 확인한다.

## 사전 확인

Grafana 대시보드에서 다음을 선택한다.

- `환경`: `perf`
- `app_instance`: `13.209.178.230:8080`

시나리오는 `/access?userId=557`에서 perf 전용 액세스 토큰을 런타임에 발급한다. 토큰을 파일이나 셸 히스토리에 저장할 필요가 없다.

## V4 smoke

```bash
CONFIRM_PERF=true \
TEST_MODE=smoke \
k6 run k6/scenarios/perf_image_generation_step.js
```

## API별 단계 부하

기본 단계는 `1 VU(2분) → 3 VU(3분) → 5 VU(3분)`이며, 각 단계 사이에 1분 램프업이 있습니다.

```bash
CONFIRM_PERF=true \
IMAGE_API=v4 \
REQUEST_TIMEOUT=90s \
k6 run k6/scenarios/perf_image_generation_step.js
```

기본값은 perf seed 데이터에 맞춰 네 API 모두 준비되어 있습니다. API만 바꿔 실행합니다.

```bash
CONFIRM_PERF=true \
IMAGE_API=products \
k6 run k6/scenarios/perf_image_generation_step.js
```

| `IMAGE_API` | perf 기본 요청 |
| --- | --- |
| `v4` | 도면 6, 기본 뷰, 취향 1·3·11·12, 재택근무, 가구 5종 |
| `banner` | 배너 20, answer chip 1, 도면 6 |
| `otherStyle` | 스타일 배너 10, 도면 6 |
| `products` | 도면 6, 상품 277~282 6개 |

다른 데이터 조합이 필요할 때만 `GENERATE_IMAGE_PAYLOAD`에 JSON을 지정합니다.

## 관측 순서

1. 실행 전 10분의 `Heap after GC`, `Old`, `available memory`, `swap used`를 기준선으로 기록한다.
2. 각 단계 중 `allocated bytes`, GC pause count/max, Hikari pending/timeout, p95/p99를 확인한다.
3. 실행이 끝난 뒤 5분 동안 Heap after GC·Old·swap이 기준선 쪽으로 회복되는지 확인한다.
4. 다음 API 또는 다음 동시성은 이전 단계가 5xx·Full GC·Hikari timeout 없이 끝난 경우에만 진행한다.

## 실제 Gemini 경로 검증

현재 perf는 Stub 모드이므로, 실제 이미지 다운로드·WebP 변환·Base64·S3 업로드는 측정하지 않는다. 실호출은 비용과 S3 쓰기가 발생하므로, 별도의 승인 후 1~3건 Canary로만 전환한다.
