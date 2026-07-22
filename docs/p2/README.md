# P2 물리 7모듈 분리 — 실행 아티팩트 (#582)

- `classification.txt` — main 639파일 → 모듈 배정(1열=모듈). `review-global` 항목은 `global-assignments.txt` 로 확정.
- `*.build.gradle.draft`, `settings.gradle.draft`, `root-build.gradle.draft` — 모듈 빌드 스크립트 초안.
- 실행 절차는 docs/582-handoff.md "P2 물리 7모듈 분리 — 실행 설계" 절 참조.
- 이동은 `git mv src/main/java/... houme-<mod>/src/main/java/...` (패키지 유지, 스플릿 패키지 허용).
- 테스트는 전부 houme-api/src/test 로. QueryDSL/generated 는 infra.
