# HOUME-SERVER 작업 가이드 (AGENTS.md)

## 1. 문서 목적
이 문서는 HOUME-SERVER 레포에서 작업할 때 지켜야 하는 공통 기준입니다.
이슈 요구사항, 현재 코드 구조, CI/CD 흐름을 기준으로 작성되었습니다.

## 2. Golden Rule
이슈 우선, 최소 변경, 검증 기반으로 작업합니다.
요청된 이슈 범위를 벗어나는 리팩터링/설계 변경은 하지 않고, 변경한 내용은 반드시 재현 가능한 방식으로 검증합니다.

## 3. 프로젝트 구조 요약
- 아키텍처: Spring Boot 모놀리식 구조
- 언어/빌드: Java 17, Gradle
- 메인 패키지: `src/main/java/or/sopt/houme`
- 도메인 모듈: `domain/credit`, `domain/furniture`, `domain/generateImage`, `domain/house`, `domain/preference`, `domain/user`
- 공통 모듈: `global/api`, `global/config`, `global/jwt`, `global/util`, `global/entity`
- QueryDSL 생성 코드: `src/main/generated`
- 리소스: `src/main/resources`

## 4. 레이어별 책임 규칙
- `presentation/controller`
  - 요청 매핑, 입력 검증(`@Valid`), 인증 객체 주입(`@AuthenticationPrincipal`)만 담당합니다.
  - 비즈니스 로직은 서비스/파사드로 위임합니다.
- `presentation/dto`
  - 요청/응답 DTO는 현재 코드베이스처럼 `record` 중심으로 작성합니다.
  - 검증 메시지는 한국어 기준을 유지합니다.
- `service`
  - 도메인 규칙, 트랜잭션 경계, 엔티티 변경을 담당합니다.
  - 읽기 메서드는 `@Transactional(readOnly = true)`를 기본으로 고려합니다.
- `service/facade`
  - 여러 서비스/외부 인프라를 조합하는 유스케이스 오케스트레이션을 담당합니다.
- `repository`
  - 단순 조회는 Spring Data JPA 메서드를 우선 사용합니다.
  - 복잡 조회는 Custom Repository + QueryDSL 구현(`*RepositoryImpl`)로 분리합니다.
- `infrastructure`
  - 외부 API(OpenAI/Gemini/Naver/FastAPI), 스케줄러, 크롤링 등 외부 연동 로직을 위치시킵니다.

## 5. API/응답/예외 컨벤션
- API 응답은 `ApiResponse<T>` 포맷(`code`, `msg`, `data`)을 사용합니다.
- 정상 응답은 `ApiResponse.ok(...)`를 사용하고, 기본 메시지 `응답 성공`을 유지합니다.
- 에러는 `ErrorCode` 기반으로 관리합니다.
- 예외는 `GeneralException` 또는 도메인별 예외(`UserException`, `GenerateImageException` 등)로 감싸서 던집니다.
- `IllegalArgumentException`을 서비스/엔티티에서 직접 던지지 않습니다. 입력/도메인 검증 실패는 반드시 도메인 예외(`UserException`, `FurnitureException`, `ValidException` 등) + `ErrorCode`로 변환합니다.
- 전역 예외 처리는 `GlobalExceptionHandler`를 통해 일관되게 반환합니다.
- 필터(JWT) 레벨 예외는 컨트롤러 예외 처리기가 잡지 못하므로 필터 내부 응답 형식을 준수합니다.

## 6. API 경로/버전 규칙
- 기본 prefix는 `/api/v1`을 사용합니다.
- 기존 버전(`v2`, `v3`)이 이미 운영 중인 엔드포인트는 하위 호환을 깨지 않도록 유지합니다.
- 기존 엔드포인트를 변경해야 할 경우, 우선 신규 버전 경로 추가를 검토합니다.

## 7. 도메인/엔티티 규칙
- 엔티티는 `domain/*/model/entity`에 위치시킵니다.
- 공통 시간 필드는 `BaseEntity` 감사 필드를 사용합니다.
- Enum 저장은 현재 패턴대로 `@Enumerated(EnumType.STRING)`을 우선 사용합니다.
- 테이블/인덱스/유니크 제약 이름은 현재 네이밍 패턴(`idx_*`, `uk_*`, `chk_*`)을 따릅니다.

## 8. DB 스키마 변경 시 필수 규칙
- 현재 `ddl-auto: update` 기반 운영이므로, 파괴적 변경은 특히 신중히 처리합니다.
- `NOT NULL` 컬럼 추가 시 반드시 아래 순서를 지킵니다.
  1. nullable 컬럼 추가
  2. 기존 데이터 백필(update)
  3. `NOT NULL`/check/unique 제약 추가
- 운영 DB 반영 전 백업 덤프를 먼저 생성합니다.
- 유니크 인덱스 추가 전 중복 데이터를 먼저 정리합니다.

## 9. 큐레이션/원천데이터 작업 규칙
- Raw 원천 데이터는 `curation_raw_products`를 기준으로 사용합니다.
- Raw 다중 태그 매핑은 `curation_raw_product_furniture_tags` 테이블로 관리합니다.
- 색상 데이터는 `curation_raw_product_colors`로 분리하여 관리합니다.
- 큐레이션 source 구분 로직(`NAVER`, `RAW`)은 `CurationSource` enum을 기준으로 유지합니다.

## 10. 트랜잭션/동시성 규칙
- 외부 API 호출과 DB 커밋 경계를 명확히 분리합니다.
- 동시성 이슈가 있는 로직은 기존 패턴(낙관적 락 + 재시도 + 백오프)을 유지합니다.
- `InterruptedException` 발생 시 `Thread.currentThread().interrupt()` 호출 후 도메인 예외로 변환합니다.
- REQUIRES_NEW 트랜잭션은 현재 사용 중인 영역(예: 이미지 생성 저장)처럼 필요한 경우에만 제한적으로 사용합니다.

## 11. 보안/설정 규칙
- 민감정보(API Key, Secret, DB 인증정보)는 코드에 신규 하드코딩하지 않습니다.
- 환경별 설정은 프로파일 파일과 GitHub Secrets를 통해 주입합니다.
- CORS/Whitelist/Security 정책 변경 시 `SecurityConfig`, `WhiteListConfig`, 실제 프론트 도메인을 함께 검토합니다.

## 12. 테스트 규칙
- 테스트 프레임워크: JUnit5 + Mockito + Spring Test (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`).
- 테스트명은 현재 코드 스타일처럼 `@DisplayName` 한글 서술형을 권장합니다.
- 비즈니스 로직 변경 시 최소 1개 이상의 관련 테스트를 추가/수정합니다.
- 로컬 검증 기본 명령: `./gradlew clean build -Dspring.profiles.active=test`

## 13. Git 작업 순서 및 네이밍
반드시 아래 순서를 지킵니다.
1. 이슈 생성 (반드시 `.github/ISSUE_TEMPLATE` 사용)
2. 브랜치 생성 (`develop` 기준)
3. 작업/검증
4. PR 생성 (`develop` 대상으로, 템플릿 준수)

네이밍 규칙:
- 브랜치: `feat/#이슈번호/작업요약`
- 커밋: `git commit -m "feat:#이슈번호 작업요약"`

## 14. PR/이슈 템플릿 준수 규칙
- 이슈는 템플릿의 섹션(`목적`, `작업 상세 내용`, `유의사항`)을 반드시 채웁니다.
- PR은 `.github/PULL_REQUEST_TEMPLATE.md`를 반드시 그대로 따릅니다.
  - `## 📣 Related Issue` (예: `- close #426`)
  - `## 📝 Summary`
  - `## 🙏 Question & PR point`
  - `## 📬 Postman`

## 15. 머지/배포 전략
- 기본 흐름: `작업 브랜치 -> develop(default) -> prod`
- `prod` 머지는 이슈 단건 기준이 아니라, `develop` 변경사항이 충분히 쌓인 릴리즈 시점에 진행합니다.
- CI/CD 워크플로우는 `.github/workflows` 기준을 따릅니다.
  - develop CI: 테스트/커버리지
  - develop CD: dev 배포
  - prod CD: blue/green 배포

## 16. 작업 전/후 체크리스트
작업 전:
- 이슈 범위를 1~2문장으로 재정의했는가
- 영향받는 모듈/엔드포인트/테이블을 확인했는가

작업 후:
- 불필요한 파일 변경이 없는가
- 기존 API 응답 포맷을 깨지 않았는가
- 예외 처리/로그/트랜잭션 경계가 일관적인가
- 템플릿을 지킨 이슈/PR 본문을 작성했는가
