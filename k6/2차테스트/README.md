# k6 2차 테스트 아카이브

- `2-1차테스트/2-1차_부하테스트_결과.md`: 비동기 스레드풀 확장 후 `step_low_load_v2` 재측정 결과
- `2-1차테스트/raw/step_low_load_v2_20260308_122756.json`: k6 summary export 원본
- `2-1차테스트/raw/server_metrics_post_run_20260308_0406.txt`: 테스트 종료 직후 서버 메트릭 스냅샷
- `2-2차테스트/2-2차_부하테스트_결과.md`: 가상 스레드 전환 후 `step_low_load_v2` 재측정 결과
- `2-2차테스트/raw/step_low_load_v2_20260308_141628.json`: k6 summary export 원본
- `2-2차테스트/raw/server_metrics_post_run_20260308_1455.txt`: Prometheus/로그 기준 서버 메트릭 스냅샷
- `2-3차테스트/2-3차_부하테스트_결과.md`: 가상 스레드 유지 상태에서 `10 -> 30 VU` 확장 결과와 DB 병목 분석
- `2-3차테스트/raw/step_mid_load_v4_20260308_170218.json`: k6 summary export 원본
- `2-3차테스트/raw/server_metrics_post_run_20260308_1719.txt`: 종료 직후 호스트 스냅샷 + Prometheus 시계열/로그 수집 결과

참고:
- 이번 묶음은 `feature/#434/load-test-bootstrap` 브랜치에서 비동기 스레드풀(`core=8, max=16, queue=8`) 확장 후 재실행한 결과다.
- 기준 비교 대상은 `k6/1차테스트/1-2차테스트/2차_부하테스트_결과.md` 이다.

- `2-4차테스트/2-4차_부하테스트_결과.md`: OSIV 비활성화 + pool 20 + virtual 32 조합으로 `10 -> 30 VU` 재검증 결과
- `2-4차테스트/raw/step_mid_load_v4_osiv_off_20260308_223136.json`: k6 summary export 원본
- `2-4차테스트/raw/server_metrics_post_run_20260308_230103.txt`: Prometheus 시계열 + 종료 직후 호스트 스냅샷
