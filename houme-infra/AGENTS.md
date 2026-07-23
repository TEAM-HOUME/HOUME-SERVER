# houme-infra 작업 가이드

## 역할
영속성/기술 어댑터 계층. **JPA가 등장하는 유일한 main 모듈**입니다.
houme-domain의 포트와 houme-application의 인바운드 계약을 구현합니다.

컴파일 의존: `application`, `domain`, `external`, `auth`, `common`

## 여기 둬야 하는 것
- `@Entity` 클래스 — 순수 도메인 모델과 이름이 겹치는 것은 `XxxJpaEntity` 로 명명 (예: `FurnitureJpaEntity`)
- Spring Data 리포지토리, Custom Repository + QueryDSL 구현(`*RepositoryImpl`)
- **영속 어댑터**: 포트 구현 + 도메인↔엔티티 매핑.
  - 예: `user/infra/persistence/UserPersistenceAdapter.java` (implements `UserRepositoryPort`)
  - 쓰기 패턴: 조회 → 순수 모델 상태를 엔티티에 적용 → 더티체킹 (id 없으면 INSERT)
- **QueryAdapter (View+QueryPort 패턴)**: 무거운 조회는 여기서 fetch join으로 자유롭게 순회한 뒤 평탄화 View/응답 DTO를 반환 (예: `UserImageHistoryQueryAdapter`)
- 인바운드 계약의 무거운 구현체: `GenerateImageFacadeImpl`, `SoozipCrawlingServiceImpl` 등
- 스케줄러, 크롤러, JPA/Redis/S3 설정, 캐시 서비스(예: `FurnitureMasterCacheService`)
- QueryDSL Q-클래스 생성(annotation processor)은 이 모듈 전용 — `houme-infra/src/main/generated` (gitignore)

## 두면 안 되는 것
- 컨트롤러/advice → houme-api
- 외부 API 클라이언트 자체 → houme-external (여기서는 호출·조합만)
- 도메인 규칙 — 어댑터는 매핑과 조회 조립만. 규칙은 houme-domain 모델로.

## 엔티티 연관 규칙 (중요)
- **도메인 경계를 넘는 `@ManyToOne` 금지** — `xxx_id`(Long) 컬럼 참조로 절단되어 있습니다 (예: `Jjym.user_id`). DB FK 제약은 살아있으므로 스키마는 그대로입니다.
- **같은 도메인 내부** 엔티티 연관(예: `FurnitureTag → FurnitureJpaEntity`)은 유지·허용 — 둘 다 이 모듈 안에 살아 모듈 경계를 해치지 않고, fetch join 성능 이점을 지킵니다.
- 다른 도메인의 데이터가 필요하면 **그 도메인의 포트**로 id 조회하세요.

## 규칙
- 새 포트 구현 시 시그니처의 순수 모델/View 반환을 지키고, JPA 엔티티를 모듈 밖으로 새게 하지 마세요 (test 스코프 제외).
- 엔티티는 `BaseEntity` 감사 필드, `@Enumerated(EnumType.STRING)`, 제약 네이밍(`idx_*`, `uk_*`, `chk_*`) 등 기존 패턴을 따릅니다.
- 단순 조회는 Spring Data 메서드 우선, 복잡 조회만 QueryDSL.

## 참조
- 전체 구조: `docs/architecture.md`, 새 도메인 절차: `docs/hexagonal-pattern-guide.md`, 공통 규칙: 루트 `AGENTS.md`
