# houme-application 작업 가이드

## 역할
유즈케이스 계층. 서비스/파사드와 request/response DTO가 삽니다.
**JPA 엔티티/리포지토리를 import 할 수 없습니다** — build.gradle에 infra 의존이 없어 컴파일로 강제됩니다.

컴파일 의존: `domain`(api), `external`, `auth`, `common`

## 여기 둬야 하는 것
- `XxxService` 인터페이스 + `XxxServiceImpl` — 단, **엔티티-프리로 작성 가능한 것만**. 도메인 포트(`port.out`)만 소비합니다.
- 파사드(여러 서비스/포트 조합 오케스트레이션, 낙관락 재시도 정책 등)
- **인바운드 계약 인터페이스**: 본질적으로 엔티티/외부IO를 다루는 무거운 로직은 억지로 순수화하지 않고, 인터페이스만 여기에 두고 구현은 infra에 둡니다.
  - 예: `domain/generateImage/service/facade/GenerateImageFacade.java`(계약, 여기) ↔ `GenerateImageFacadeImpl`(houme-infra)
  - 같은 패턴: `SoozipCrawlingService`, `CurationRawProductService`, `UserDeletionService` 등
- request/response DTO (`record` 중심, 검증 메시지는 한국어)

## 두면 안 되는 것
- JPA 엔티티를 만지는 코드 전부 → infra 어댑터/Impl로
- DB 조회 로직 → 도메인 포트 정의(houme-domain) + 어댑터(houme-infra)
- 응답 DTO ← 엔티티 직접 매핑 → infra의 `*Mapper` 또는 응답 DTO를 직접 반환하는 QueryPort 로

## 규칙
- 서비스 시그니처는 순수 도메인 모델/View/DTO만. 인증 유저는 `CustomUserDetails`가 래핑한 **순수 `User`** 를 받습니다.
- 읽기 메서드는 `@Transactional(readOnly = true)` 기본.
- **순수 모델 변이 후 저장을 잊지 마세요**: JPA 더티체킹이 없으므로 `user.updateX(...)` 후 반드시 `userRepositoryPort.save(user)` 를 호출해야 합니다. (실제로 이 누락 버그가 있었음)
- 무거운 조회가 필요하면 서비스에서 조립하지 말고 View+QueryPort 패턴(infra가 fetch join 후 평탄화 View 반환)을 쓰세요.
- 실제 유즈케이스를 실행하는 클래스에만 `*Service` 명칭을 사용합니다. 보조 컴포넌트는 역할이 드러나는 이름(`*Mapper`, `*Validator`, `*Provider` 등).
- external 클라이언트 직접 소비는 현존 코드에 한해 허용된 편차입니다. 신규 코드는 가급적 도메인 포트 뒤로 숨기는 방향을 우선 검토하세요.

## 참조
- 전체 구조: `docs/architecture.md`, 새 도메인 절차: `docs/hexagonal-pattern-guide.md`, 공통 규칙: 루트 `AGENTS.md`
