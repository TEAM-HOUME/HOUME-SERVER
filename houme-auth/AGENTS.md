# houme-auth 작업 가이드

## 역할
인증/인가 계층. JWT 발급·검증 필터와 Spring Security 사용자 어댑터가 삽니다.

컴파일 의존: `domain`, `common`

## 여기 둬야 하는 것
- `JWTUtil`/`JWTFilter` 등 토큰 발급·검증
- `CustomUserDetails`/`CustomUserDetailsService` (`domain/user/presentation/controller/dto/`)
- 인증 관련 로깅/유틸

## 핵심 설계
- `CustomUserDetails` 는 **순수 도메인 `User`** (houme-domain)를 래핑합니다. 덕분에 전 서비스/컨트롤러 시그니처가 JPA 엔티티가 아닌 순수 `User` 를 받습니다. 이 래핑 대상을 엔티티로 되돌리지 마세요.
- 토큰 저장소(Refresh/Blacklist/SignupSession)는 **인터페이스가 domain 포트로**, Redis 구현(`Redis*`)은 houme-infra에 있습니다. 저장소 로직을 이 모듈에 추가하지 마세요.

## 규칙
- **필터 레벨 예외는 `GlobalExceptionHandler` 가 잡지 못합니다.** 필터 내부에서 `ApiResponse` 포맷(`code`/`msg`/`data`)으로 직접 응답을 써야 합니다.
- Security 정책 변경 시 이 모듈의 `WhiteListConfig` 와 houme-api의 `SecurityConfig`(필터 등록·CORS)를 함께 검토하세요.
- 민감정보(secret key 등)는 프로파일 yml/Secrets 주입, 하드코딩 금지.
- `CustomUserDetails(User)` 생성자는 스프링이 파라미터명으로 바인딩합니다 — 루트의 `-parameters` 컴파일 옵션에 의존하므로 제거 금지.

## 참조
- 전체 구조: `docs/architecture.md`, 공통 규칙: 루트 `AGENTS.md`
