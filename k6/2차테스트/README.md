# k6 2차 테스트 아카이브

- `2-1차테스트/2-1차_부하테스트_결과.md`: 비동기 스레드풀 확장 후 `step_low_load_v2` 재측정 결과
- `2-1차테스트/raw/step_low_load_v2_20260308_122756.json`: k6 summary export 원본
- `2-1차테스트/raw/server_metrics_post_run_20260308_0406.txt`: 테스트 종료 직후 서버 메트릭 스냅샷

참고:
- 이번 묶음은 `feature/#434/load-test-bootstrap` 브랜치에서 비동기 스레드풀(`core=8, max=16, queue=8`) 확장 후 재실행한 결과다.
- 기준 비교 대상은 `k6/1차테스트/1-2차테스트/2차_부하테스트_결과.md` 이다.
