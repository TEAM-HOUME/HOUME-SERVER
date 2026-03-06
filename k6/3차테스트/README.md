# k6 3차 테스트 아카이브

- `3차_부하테스트_결과.md`: 3차 테스트 최종 수치와 해석 정리
- `raw/step3_low_load_20260306_104513_extracted.json`: HTML dashboard export에서 복원한 누적 메트릭

참고:
- 3차 테스트는 `1 -> 2 -> 4 -> 6 -> 8 -> 10 VU` 저부하 스텝 시나리오로 실행했다.
- Gemini Stub 지연은 `10초` 고정, 토큰은 `200개 사용자 풀`로 분산했다.
- k6 `summary-export` JSON이 저장되지 않아, 원격 HTML 리포트(`/home/ubuntu/loadtest/logs/step3_low_load_20260306_104513.html`)에서 메트릭을 복원했다.
