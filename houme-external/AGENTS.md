# houme-external 작업 가이드

## 역할
외부 시스템 클라이언트 계층. Feign 클라이언트(Kakao·Naver·FastApi), Gemini/OpenAI 이미지 생성, Discord 알림과 각 외부 API의 요청/응답 DTO가 삽니다.

컴파일 의존: `domain`, `common`

## 여기 둬야 하는 것
- Feign/HTTP 클라이언트와 그 설정
- 외부 API 전용 요청/응답 DTO (예: `domain/furniture/infrastructure/dto/external/naverShop/**`)
- 외부 응답 → 내부 표현으로의 1차 변환

## 두면 안 되는 것
- 유즈케이스/비즈니스 로직 → houme-application 또는 houme-infra
- DB 접근 → houme-infra

## 규칙
- **외부 응답 DTO를 모듈 밖 계약에 노출하지 마세요.** application/도메인이 필요로 하는 값은 내부 DTO/도메인 타입으로 변환해 반환하는 것을 우선합니다. (기존 노출 지점은 허용된 편차 — 신규 코드에서 확대 금지)
- 외부 API 호출은 DB 트랜잭션/커밋 경계와 분리합니다 — 트랜잭션 안에서 외부 호출 금지.
- API Key 등 인증정보는 프로파일 yml/Secrets 주입, 하드코딩 금지.
- 타임아웃/재시도 정책은 클라이언트 설정에 명시하고, 실패는 도메인 예외(`GenerateImageException` 등)로 변환합니다.
- `InterruptedException` 은 `Thread.currentThread().interrupt()` 후 도메인 예외로 변환.

## 참조
- 전체 구조: `docs/architecture.md`, 공통 규칙: 루트 `AGENTS.md`
