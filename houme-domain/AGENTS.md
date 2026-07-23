# houme-domain 작업 가이드

## 역할
순수 도메인 계층. **Spring/JPA 의존이 0인 모듈**이며, 컴파일 의존은 `houme-common` 뿐입니다.
비즈니스 규칙의 최종 소유자이자, infra가 구현해야 하는 계약(포트)의 정의처입니다.

## 여기 둬야 하는 것
- **순수 도메인 모델**: `User`, `House`, `Furniture`, `Credit`, `Jjym` 등. JPA 어노테이션 없이 규칙을 소유합니다.
  - 예: `credit/domain/Credit.java` — `reserve()` 가 상태 검증 후 전이를 수행
  - 예: `user/domain/User.java` — 프로필 갱신 규칙 소유
- **아웃바운드 포트** (`{도메인}/domain/port/out/*Port.java`): infra가 구현할 인터페이스.
  - 예: `user/domain/port/out/UserRepositoryPort.java`
- **View (read model)**: 무거운 조회 결과를 평탄화한 record.
  - 예: `furniture/domain/CurationRawProductColorView.java` — `resolveColorName` 같은 표현 규칙도 View가 소유
- 순수 enum, VO

## 두면 안 되는 것
- `@Entity`, Spring Data 리포지토리, QueryDSL → `houme-infra`
- `@Service`/`@Component`/`@Transactional` 이 붙는 클래스 → `houme-application` 또는 `houme-infra`
- 외부 API DTO → `houme-external`
- **어떤 형태로든 Spring/JPA import** — build.gradle에 의존이 없어 컴파일이 실패하지만, 필요해 보인다고 의존을 추가하지 마세요. 필요하다면 설계가 잘못된 것입니다.

## 규칙
- 도메인 규칙(상태 전이, 검증, 대표값 선택, 중복 방지)은 모델 메서드가 책임집니다. 서비스에 규칙을 흘리지 마세요.
- 검증 실패는 도메인 예외(`UserException` 등, houme-common의 `ErrorCode` 기반)로 던집니다. `IllegalArgumentException` 금지.
- 포트 시그니처는 순수 모델/View/원시 타입만 사용합니다. JPA 엔티티가 시그니처에 등장하면 안 됩니다.
- 포트에 저장 의미가 있는 메서드(`save`, `saveAndFlush`)를 추가할 때는 트랜잭션 타이밍 의미(예: 유니크 제약 재시도용 flush)를 주석 대신 메서드명으로 드러냅니다.

## 참조
- 새 도메인 추가 절차: `docs/hexagonal-pattern-guide.md`
- 전체 구조: `docs/architecture.md`, 공통 규칙: 루트 `AGENTS.md`
