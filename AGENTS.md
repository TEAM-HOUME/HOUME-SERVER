# HOUME-SERVER 작업 가이드 (AGENTS.md)

## 1. 문서 목적과 구조
이 문서는 HOUME-SERVER 레포 **전체에 공통으로 적용되는 규칙**과, 모듈별 상세 규칙으로 가는 **라우팅 테이블**입니다.
각 모듈 디렉토리에는 그 모듈 안에서 작업할 때의 로컬 규칙을 담은 `AGENTS.md`가 별도로 있습니다(가장 가까운 문서가 우선 적용).
아키텍처의 배경 서사와 전체 그림은 `docs/architecture.md`, 새 도메인을 헥사고날로 추가하는 표준 절차는 `docs/hexagonal-pattern-guide.md`를 참조하세요.

## 2. Golden Rule
이슈 우선, 최소 변경, 검증 기반으로 작업합니다.
요청된 이슈 범위를 벗어나는 리팩터링/설계 변경은 하지 않고, 변경한 내용은 반드시 재현 가능한 방식으로 검증합니다.

## 3. 아키텍처 핵심 불변식 (컴파일로 강제됨)
> **서비스(houme-application)는 JPA 엔티티/리포지토리를 import 할 수 없다.**
> 서비스는 `houme-domain`의 순수 모델과 포트(인터페이스)만 보고, 실제 DB 접근은 `houme-infra`의 어댑터가 포트를 구현해서 제공한다.
> 이 규칙은 gradle 의존 그래프로 강제되어, 어기면 컴파일 자체가 실패한다.

- `houme-domain`은 Spring/JPA 의존이 0이어야 합니다.
- 루트 `build.gradle`의 전 모듈 `-parameters` 컴파일 옵션은 **절대 제거 금지** — 제거 시 `@AuthenticationPrincipal`/DataBinder 파라미터명 소실로 런타임 500이 납니다.

## 4. 모듈 구조와 라우팅

아키텍처: Spring Boot + 헥사고날(포트&어댑터) + gradle 7모듈. 언어/빌드: Java 21, Gradle.
패키지 루트는 `or.sopt.houme` 를 전 모듈이 공유합니다(스플릿 패키지 허용, 파일 위치 = 모듈).

| 하려는 작업 | 시작할 모듈 / 문서 |
|---|---|
| 컨트롤러, 예외 advice, Security/Web/Swagger 설정, main | [`houme-api/AGENTS.md`](houme-api/AGENTS.md) |
| 서비스/파사드(유즈케이스), request/response DTO | [`houme-application/AGENTS.md`](houme-application/AGENTS.md) |
| 도메인 규칙, 순수 모델, 포트(인터페이스), View | [`houme-domain/AGENTS.md`](houme-domain/AGENTS.md) |
| `@Entity`, 리포지토리, QueryDSL, 어댑터, 스케줄러/크롤러 | [`houme-infra/AGENTS.md`](houme-infra/AGENTS.md) |
| JWT, 인증 필터, CustomUserDetails | [`houme-auth/AGENTS.md`](houme-auth/AGENTS.md) |
| 외부 API 클라이언트(Kakao/Naver/FastApi/Gemini/Discord) | [`houme-external/AGENTS.md`](houme-external/AGENTS.md) |
| ApiResponse/ErrorCode/공유 DTO/상수 | [`houme-common/AGENTS.md`](houme-common/AGENTS.md) |
| 통합/슬라이스 테스트 (전부 여기 있음) | `houme-api/src/test` — [`houme-api/AGENTS.md`](houme-api/AGENTS.md) |
| 부하테스트(k6) | `k6/README.md` |

컴파일 의존 방향(화살표 = 의존):

```
api ─▶ application, auth, external   (+ runtimeOnly infra — 코드 참조 불가)
application ─▶ domain(api), external, auth, common
infra ─▶ application, domain, external, auth, common
auth ─▶ domain, common
external ─▶ domain, common
domain ─▶ common
common ─▶ (없음 — 경량 spring-web/jackson/slf4j 만)
```

새 도메인 추가 순서: `domain(모델+port.out) → infra(JpaEntity+Mapper+Adapter) → application(Service) → api(Controller)` — 상세는 `docs/hexagonal-pattern-guide.md`.

## 5. API/응답/예외 컨벤션
- API 응답은 `ApiResponse<T>` 포맷(`code`, `msg`, `data`)을 사용합니다.
- 정상 응답은 `ApiResponse.ok(...)`를 사용하고, 기본 메시지 `응답 성공`을 유지합니다.
- 에러는 `ErrorCode` 기반으로 관리합니다.
- 예외는 `GeneralException` 또는 도메인별 예외(`UserException`, `GenerateImageException` 등)로 감싸서 던집니다.
- `IllegalArgumentException`을 서비스/도메인에서 직접 던지지 않습니다. 입력/도메인 검증 실패는 반드시 도메인 예외 + `ErrorCode`로 변환합니다.
- 전역 예외 처리는 `GlobalExceptionHandler`(houme-api)를 통해 일관되게 반환합니다.
- 필터(JWT) 레벨 예외는 컨트롤러 예외 처리기가 잡지 못하므로 필터 내부 응답 형식을 준수합니다.

## 6. API 경로/버전 규칙
- 기본 prefix는 `/api/v1`을 사용합니다.
- 기존 버전(`v2`, `v3`)이 이미 운영 중인 엔드포인트는 하위 호환을 깨지 않도록 유지합니다.
- 기존 엔드포인트를 변경해야 할 경우, 우선 신규 버전 경로 추가를 검토합니다.

## 7. DB 스키마 변경 시 필수 규칙
- 현재 `ddl-auto: update` 기반 운영이므로, 파괴적 변경은 특히 신중히 처리합니다.
- `NOT NULL` 컬럼 추가 시 반드시 아래 순서를 지킵니다.
  1. nullable 컬럼 추가
  2. 기존 데이터 백필(update)
  3. `NOT NULL`/check/unique 제약 추가
- 운영 DB 반영 전 백업 덤프를 먼저 생성합니다.
- 유니크 인덱스 추가 전 중복 데이터를 먼저 정리합니다.
- 신규 인덱스/복합 유니크 제약은 요청 사항 또는 명확한 무결성/성능 근거가 있을 때만 추가합니다.
- cross-domain FK 컬럼(`user_id` 등)은 JPA 연관이 아닌 `Long` 컬럼이지만 **DB FK 제약은 유지**됩니다 — 스키마를 임의로 바꾸지 마세요.

## 8. 트랜잭션/동시성 규칙
- 외부 API 호출과 DB 커밋 경계를 명확히 분리합니다.
- 동시성 이슈가 있는 로직은 기존 패턴(낙관적 락 + 재시도 + 백오프)을 유지합니다.
- `InterruptedException` 발생 시 `Thread.currentThread().interrupt()` 호출 후 도메인 예외로 변환합니다.
- REQUIRES_NEW 트랜잭션은 현재 사용 중인 영역(예: 이미지 생성 저장)처럼 필요한 경우에만 제한적으로 사용합니다.

## 9. 보안/설정 규칙
- 민감정보(API Key, Secret, DB 인증정보)는 코드에 신규 하드코딩하지 않습니다.
- 환경별 설정은 프로파일 파일과 GitHub Secrets를 통해 주입합니다. CD가 Secrets에서 `houme-api/src/main/resources/application-{profile}.yml`을 생성합니다.
- CORS/Whitelist/Security 정책 변경 시 `SecurityConfig`, `WhiteListConfig`, 실제 프론트 도메인을 함께 검토합니다.

## 10. 테스트 규칙
- 테스트는 **전부 `houme-api/src/test`에 있습니다** — 통합/슬라이스 테스트가 전체 클래스패스를 필요로 하기 때문(test 스코프에서만 infra를 직접 봄).
- 테스트 프레임워크: JUnit5 + Mockito + Spring Test (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`) + Testcontainers.
- 테스트명은 `@DisplayName` 한글 서술형을 권장합니다.
- 비즈니스 로직 변경 시 최소 1개 이상의 관련 테스트를 추가/수정합니다.
- 리팩터링 시에는 대상 API의 계약(응답 JSON) 안전망 통합테스트를 **먼저** 확보한 뒤 진행합니다.
- 전체 검증 명령: `./gradlew clean build -Dspring.profiles.active=test` (약 10분 소요).
  - 중간 검증은 `--tests` 로 타깃 테스트만, 전체 스위트는 도메인 단위 완료/푸시 전에 실행합니다.
  - **gradle 을 동시에 2개 이상 실행하지 마세요** — Testcontainers 간섭으로 거짓 FAILED가 납니다(`maxParallelForks=1`은 팀 결정).

## 11. Git 작업 순서 및 네이밍
반드시 아래 순서를 지킵니다.
1. 이슈 생성 (반드시 `.github/ISSUE_TEMPLATE` 사용)
2. 브랜치 생성 (`develop` 기준)
3. 작업/검증
4. PR 생성 (`develop` 대상으로, 템플릿 준수)

네이밍 규칙:
- 브랜치: `<실행태그>/#이슈번호/작업요약` 형식을 사용합니다.
- 브랜치 실행 태그는 `feature`, `refactor`, `fix`, `chore` 중 작업 성격에 맞는 값을 사용합니다.
- `codex/*` prefix 브랜치는 사용하지 않습니다.
- 커밋: `git commit -m "feat:#이슈번호 작업요약"`

## 12. PR/이슈 템플릿 준수 규칙
- 이슈는 템플릿의 섹션(`목적`, `작업 상세 내용`, `유의사항`)을 반드시 채웁니다.
- PR은 `.github/PULL_REQUEST_TEMPLATE.md`를 반드시 그대로 따릅니다.
  - `## 📣 Related Issue` (예: `- close #426`)
  - `## 📝 Summary`
  - `## 🙏 Question & PR point`
  - `## 📬 Postman`

## 13. 머지/배포 전략
- 기본 흐름: `작업 브랜치 -> develop(default) -> prod`
- 머지는 **squash** 를 사용합니다(merge commit 금지 정책).
- `prod` 머지는 이슈 단건 기준이 아니라, `develop` 변경사항이 충분히 쌓인 릴리즈 시점에 진행합니다.
- CI/CD 워크플로우는 `.github/workflows` 기준을 따릅니다.
  - develop CI: 테스트/커버리지 (jacoco 리포트는 `houme-api/build/reports/jacoco/` 아래)
  - develop CD: dev 배포 (Jib → ECR → EC2, `./gradlew :houme-api:jib`)
  - prod CD: blue/green 배포
- 실행 jar는 `houme-api/build/libs/houme-api-*.jar` 하나뿐입니다(유일한 bootJar 모듈).

## 14. 작업 전/후 체크리스트
작업 전:
- 이슈 범위를 1~2문장으로 재정의했는가
- 어느 모듈에 파일이 생기는지 확인하고 해당 모듈 `AGENTS.md`를 읽었는가

작업 후:
- 불필요한 파일 변경이 없는가
- 기존 API 응답 포맷을 깨지 않았는가
- 모듈 의존 방향(§4)을 어기는 import 가 없는가
- 예외 처리/로그/트랜잭션 경계가 일관적인가
- 템플릿을 지킨 이슈/PR 본문을 작성했는가
