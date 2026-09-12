# Perf 이미지 생성 부하 테스트

## 목적

- 부하는 로컬 Mac에서 발생시키고, perf EC2의 JVM·GC·Hikari·호스트 메모리 지표를 Grafana에서 관측한다.
- perf 환경은 `load_test` Gemini Stub(약 30초)을 사용한다. 따라서 이 단계는 외부 Gemini 비용 없이 동시 장시간 요청에 대한 서버 자원 한계를 확인한다.

## 사전 확인

Grafana 대시보드에서 다음을 선택한다.

- `환경`: `perf`
- `app_instance`: `13.209.178.230:8080`

시나리오는 `/access?userId=`에서 perf 전용 액세스 토큰을 런타임에 발급한다. 기본값은 `557~561` 다섯 사용자이며, VU별로 다른 토큰을 사용해 사용자별 크레딧 락이 동시성을 직렬화하지 않도록 한다. 토큰을 파일이나 셸 히스토리에 저장할 필요가 없다.

테스트 전 perf DB에 사용자를 seed한다.

```bash
ssh -i houme-perf-key.pem ubuntu@13.209.178.230 \
  'docker exec -i houme-perf-postgres psql -U houme_perf -d houme_perf' \
  < k6/seeds/perf_load_users.sql
```

## V4 smoke

```bash
CONFIRM_PERF=true \
TEST_MODE=smoke \
k6 run k6/scenarios/perf_image_generation_step.js
```

## API별 초기 단계 부하

초기 실험은 아래처럼 VU당 정확히 한 건만 보내는 burst로 실행합니다. 사용자별 크레딧 락 재진입을 피하고, 해당 동시성의 JVM·GC 반응을 분리하기 위함입니다. 각 실행 뒤 5분 회복 구간을 둡니다.

```bash
CONFIRM_PERF=true \
IMAGE_API=v4 \
TEST_MODE=burst \
BURST_VUS=1 \
REQUEST_TIMEOUT=90s \
k6 run k6/scenarios/perf_image_generation_step.js
```

`BURST_VUS`를 `1 → 3 → 5` 순서로 바꿔 실행합니다. 각 VU에는 서로 다른 perf 사용자 토큰이 배정됩니다. 결과에서는 `image_generation_duration`, `image_generation_failed` custom metric을 기준으로 판단합니다. 반복 장시간 부하는 이 단계가 모두 통과한 뒤에만 기본 `TEST_MODE=step`으로 진행합니다.

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

## 이미지 생성 간섭 테스트

실제 Gemini 이미지 생성이 일반 조회 API의 tail latency에 주는 영향을 확인한다. Probe는 perf에서 단건 200을 확인한 `/api/v1/landings`를 기본값으로 사용하며, baseline과 interference를 같은 요청률로 비교한다.

```bash
# 기준선: 조회 API만 4 rps, 75초
CONFIRM_PERF=true \
TEST_MODE=baseline \
k6 run k6/scenarios/perf_image_generation_interference.js

# 간섭 구간: 동일한 조회 API 4 rps + 시작 15초 뒤 V4 동시 5건
CONFIRM_PERF=true \
TEST_MODE=interference \
IMAGE_VUS=5 \
k6 run k6/scenarios/perf_image_generation_interference.js
```

결과에서는 `probe_latency`의 p95/p99를 baseline과 비교하고, 동일 시간대의 GC pause와 `Heap after GC`를 함께 확인한다. 실제 Gemini 호출과 perf S3 저장이 발생하므로 상한 부하(예: 8 VU)는 5 VU 결과에 오류·Full GC·Hikari timeout이 없을 때만 별도로 실행한다.

## Fresh JVM 회복 + 간섭 통합 실험

앱을 재생성한 뒤 `10분 idle → V4 동시 5건 → 10분 idle`을 3회 반복하고, 마지막에 조회 API baseline/간섭을 같은 JVM에서 비교한다. 실제 Gemini 호출과 perf S3 저장이 발생한다.

```bash
CONFIRM_PERF=true \
PERF_SSH_KEY=/absolute/path/to/houme-perf-key.pem \
bash k6/scripts/run_perf_recovery_and_interference.sh
```

각 구간의 JVM·Hikari·호스트 스냅샷과 k6 결과는 기본적으로 `/private/tmp/houme-perf/recovery-<실행시각>/`에 저장된다. idle 이후 `Heap after GC`가 80% 이상이면 다음 부하를 실행하지 않고 중단한다. 첫 Old 수집 전이라 live-data가 아직 없을 때는 현재 전체 힙 사용률 90%를 보조 중단 기준으로 사용한다.

## 참고 이미지 최적화 A/B 실험

동일한 이미지·동시성에서 원본 입력과 하이브리드 최적화 입력의 JVM 메모리·GC 차이를 비교한다. 각 모드는 새 JVM으로 시작하며, 원본 모드와 최적화 모드에서 각각 `V4 동시 5건 → idle 회복`을 3회 반복한다. 테스트 종료 시 앱은 항상 최적화 모드로 복구된다.

```bash
CONFIRM_PERF=true \
PERF_SSH_KEY=/absolute/path/to/houme-perf-key.pem \
bash k6/scripts/run_perf_image_optimization_ab.sh
```

결과는 `/private/tmp/houme-perf/optimization-ab-<실행시각>/`에 저장된다. 각 요청 구간의 `*-timeseries.tsv`는 2초 간격의 Old/Eden/Survivor, live data, GC count/누적 pause, Humongous GC, allocation/promotion 누적량, RSS를 포함한다. 비교할 핵심 값은 다음과 같다.

- 입력 관측 로그: `referenceSourceBytes`, `referenceOptimizedBytes`, `referenceBase64Bytes`
- 요청 중 peak: heap·Old·RSS 및 allocation 증가량
- GC: 전체/Humongous 발생 횟수와 누적 pause 증가량
- 회복: idle 뒤 Old, Heap after GC, live data가 각 모드 기준선으로 돌아오는지

## G1 IHOP A/B 실험

이미지 입력 최적화 뒤에도 Old 사용량이 높게 남는 경우에만, G1의 concurrent marking 시작점을 앞당기는 효과를 확인한다. 기본 adaptive IHOP와 정적 30% IHOP를 각각 새 JVM에서 실제 Gemini V4 동시 5건씩 3회 실행해 비교한다. `GC.class_histogram`은 Heap Inspection GC를 유발해 결과를 오염시키므로 이 A/B에는 사용하지 않는다.

```bash
CONFIRM_PERF=true \
PERF_SSH_KEY=/absolute/path/to/houme-perf-key.pem \
bash k6/scripts/run_perf_g1_ihop_ab.sh
```

기존 default 기준선이 같은 코드·데이터에서 이미 확보돼 있다면, 추가 후보값만 짧게 실행할 수 있다.

```bash
CONFIRM_PERF=true \
PERF_SSH_KEY=/absolute/path/to/houme-perf-key.pem \
SKIP_DEFAULT=true IHOP_PERCENT=40 \
bash k6/scripts/run_perf_g1_ihop_ab.sh
```

결과는 `/private/tmp/houme-perf/g1-ihop-ab-<실행시각>/`에 저장된다. 평가는 `Old`, GC pause 횟수·누적 시간, Humongous GC, allocation/promotion, 회복 후 힙을 함께 본다. `jvm_gc_live_data_size_bytes`는 가장 최근 GC의 값이라 GC가 없는 구간에서는 최신 실제 점유량이 아닐 수 있으므로 단독 판단 기준으로 사용하지 않는다.
