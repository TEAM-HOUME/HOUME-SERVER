# houme-common 작업 가이드

## 역할
전 모듈이 공유하는 최하단 모듈. **다른 houme 모듈에 의존하지 않으며**, 외부 의존도 경량(spring-web, jackson, slf4j 수준)만 갖습니다.

## 여기 둬야 하는 것
- `ApiResponse<T>` (`code`/`msg`/`data`), `ErrorCode`, `GeneralException` 등 예외 타입
- 전 모듈이 쓰는 플레인 공유 DTO, 상수, 순수 유틸

## 두면 안 되는 것
- 특정 도메인에만 쓰이는 것 → 그 도메인이 사는 모듈로
- JPA/QueryDSL/Redis/AWS 등 **무거운 의존이 필요한 코드** — build.gradle에 새 의존을 추가하고 싶어지면 이 모듈이 아닌 곳에 둬야 한다는 신호입니다. common의 의존은 전 모듈로 전파됩니다.
- 비즈니스 로직, 상태를 가지는 컴포넌트

## 규칙
- 여기 있는 타입은 전 모듈의 컴파일 의존입니다 — 시그니처 변경은 전 모듈 영향이므로 하위 호환을 우선하세요.
- `ApiResponse.ok(...)` 기본 메시지 `응답 성공`, `ErrorCode` 체계는 API 계약입니다. 임의 변경 금지.
- 에러 코드 추가는 기존 코드 번호 체계를 따릅니다. 도메인 예외(`UserException` 등)도 여기(`global/api/handler/`)에 살며, `GeneralException` 베이스를 따릅니다.

## 참조
- 전체 구조: `docs/architecture.md`, 공통 규칙: 루트 `AGENTS.md`
