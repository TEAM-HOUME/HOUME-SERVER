# houme-api 작업 가이드

## 역할
HTTP 진입점이자 **유일한 bootJar 모듈**. 컨트롤러, 전역 예외 advice, Security/Web/Swagger 설정, `HoumeApplication(main)` 이 삽니다.
빌드/배포(Jib, CD, jacoco)도 이 모듈 기준으로 돌아갑니다.

컴파일 의존: `application`, `auth`, `external`, `common` + **runtimeOnly** `infra`

## runtimeOnly infra 의 의미
infra는 런타임 빈 조립을 위해서만 클래스패스에 올라갑니다. **main 코드에서 infra 클래스를 import 하면 컴파일이 실패합니다** — 이는 의도된 강제입니다. 컨트롤러가 infra 기능이 필요하면 application의 인터페이스(계약)를 주입받으세요. (test 스코프에서만 infra 직접 참조 허용)

## 여기 둬야 하는 것
- `*Controller`: 요청 매핑, `@Valid` 입력 검증, `@AuthenticationPrincipal` 주입만. 로직은 서비스/파사드로 위임.
- `GlobalExceptionHandler`, `SecurityConfig`/`WebConfig`/`SwaggerConfig` 등 앱 조립 설정
- **모든 테스트** (`src/test`): 통합/슬라이스 테스트가 전체 클래스패스를 필요로 하기 때문에 테스트는 전부 이 모듈에 둡니다.

## 두면 안 되는 것
- 비즈니스 로직 → houme-application
- DTO 정의 → houme-application (컨트롤러는 소비만)

## 규칙
- API 경로는 `/api/v1` prefix, 기존 `v2`/`v3` 하위 호환 유지 (루트 AGENTS.md §6).
- 컨트롤러 시그니처의 인증 유저는 `CustomUserDetails` → 순수 `User`. JPA 엔티티가 컨트롤러에 등장할 수 없습니다.
- 테스트 실행: `./gradlew clean build -Dspring.profiles.active=test` (전체 약 10분, Testcontainers). **gradle 동시 실행 금지**, `maxParallelForks=1` 유지.
- 리팩터링 전에는 대상 API의 계약(응답 JSON) 안전망 통합테스트를 먼저 확보합니다.

## 빌드/배포에서 이 모듈이 특별한 점
- 실행 jar: `houme-api/build/libs/houme-api-*.jar`
- Jib: `./gradlew :houme-api:jib` — `src/main/jib/app/bin/cwebp` 바이너리가 이미지 `/app/bin/cwebp`(755)로 들어갑니다. **지우지 마세요.**
- CD가 GitHub Secrets에서 `houme-api/src/main/resources/application-{profile}.yml` 을 생성합니다 (dev/prod yml이 로컬에 없는 것은 정상).
- jacoco 리포트: `houme-api/build/reports/jacoco/`
- 루트 build.gradle의 전 모듈 `-parameters` 옵션 제거 금지 — 부트 플러그인이 없는 모듈의 파라미터명이 사라져 `@AuthenticationPrincipal` 바인딩이 500을 냅니다.

## 참조
- 전체 구조: `docs/architecture.md`, 공통 규칙: 루트 `AGENTS.md`
