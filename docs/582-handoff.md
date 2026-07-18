# #582 헥사고날+멀티모듈 전환 — 이어가기 노트 (compact 후 재개용)

> 세션 compact로 세부 맥락이 소실돼도 이 문서로 정확히 재개한다. 항상 최신 상태로 갱신할 것.

## 목표
- #582: HOUME 서버를 **헥사고날(포트&어댑터) + gradle 7모듈**로 전환.
- 3단계 계획: 1)#584 테스트인프라(완료) 2)#581 credit 파일럿(머지됨, develop `3d6e38f`) 3)#582(이 작업).

## 확정된 결정 (사용자 지시)
1. **PR은 단 하나** — 브랜치 `feat/#582/hexagonal-multimodule` 하나에 도메인별로 커밋만 잘게 쪼개고, 최종 1개 PR로 낸다. (도메인별 PR 분리 안 함)
2. **엄격 순수** — 엔티티를 순수 도메인모델 + id 참조 + read model로. JPA 연관 절단. (credit 패턴 그대로)
3. **얽힌 클러스터 통째 전환** — House·Taste·Tag·Furniture·Carousel·HouseTaste·TasteTag가 JPA 연관으로 도메인 경계를 넘나들어, 한 도메인씩이 아니라 **클러스터 단위로** 전환.
4. 물리 gradle 모듈 분리는 **맨 마지막 한 번**.
5. **API 계약/DB 스키마 불변** (ddl-auto=update, JPA 매핑 기존 스키마와 정확히 일치).

### 실행 방식 (strangler / 점진적 병렬 변경) — 중요
- 목적지는 "엄격 순수 + 클러스터 전체"지만, **도달 경로는 리프부터 green 커밋**으로 쌓는다.
- 리프 엔티티를 순수화할 때, 아직 전환 안 된 **자식은 잠정적으로 `XxxJpaEntity`를 @ManyToOne 으로 참조**(임시 seam). 그 자식이 자기 차례에 전환되면 그 연관을 **id(Long) 참조로 절단**하며 seam 제거.
- 단일 PR이라 부분 상태가 develop에 안 나가고, PR 최종 시점엔 모든 seam이 제거되어 엄격 순수 달성.
- 각 커밋은 `compileJava`+`compileTestJava`+영향 테스트 그린 유지.

## 기준 패턴 (credit, 이미 완료)
- 슬라이스: `or.sopt.houme.credit.{domain, application, infra}` — domain(순수 모델+port.out), application(UseCase+Service), infra(JpaEntity+Mapper+PersistenceAdapter+LockAdapter).
- ArchUnit: `CreditArchitectureTest` (domain→JPA/Spring 금지, application→infra 금지).
- 코드 표준: `docs/hexagonal-pattern-guide.md`.
- 아키텍처 당위성 문서: `~/Desktop/왜_멀티모듈로_가야하는가.md`.

## 지금까지 한 것 (이 브랜치, 원격 푸시됨)
- 1단계 안전망 통합테스트 6개(5파일) 신규, 전부 그린:
  - `MoodboardApiIntegrationTest` (GET /moodboard-images)
  - `HouseOptionsApiIntegrationTest` (GET /housing-options)
  - `HouseTemplatesApiIntegrationTest` (GET /api/v2/house-templates)
  - `CarouselApiIntegrationTest` (GET /api/v1/carousels, 인증)
  - `CurationProductApiIntegrationTest` (GET /curations/products, /filters)
- 안전망 작성 팁: HTTP 계약만 검증(리팩 후 불변). 인증 필요 시 `User` 시드 + `JWTUtil.createJwt("access", userId, role, 86_400_000L)` + `Authorization: Bearer`. 격리는 `@Transactional`. IntegrationTestSupport 상속(실 PG/Redis, 외부 클라이언트/S3만 @MockBean).
- 기존 401개 테스트도 리팩 내내 그린 유지해야 함(추가 회귀망).

## 다음 할 일 (재개 시)
### 1단계 잔여 안전망 (선택, 무거움 — 픽스처+인증 필요)
- carousel like/hate, jjym(v1/v2), housing-selections(POST), 이미지생성 v1~v4, curation product 상세/dashboard.

### 2단계 본체 — 클러스터 순수화 (리프부터, 가리켜지는 순서 역순)
0. **엔티티→presentation DTO 역방향 의존 제거** — 완료 (커밋 `f3ea0d6`). Tag.update/Taste.createByPreSignedURL/Furniture·FurnitureTag 팩토리를 primitive 파라미터로.
1. **Tag** 순수화 — ✅ **완료 (커밋 `2b599c1`)**. `or.sopt.houme.tag.{domain, domain.port.out, infra.persistence}` 슬라이스 신설. 순수 `Tag`(도메인) + `TagJpaEntity`(infra, @Table tags) + `TagRepositoryPort`/`TagPersistenceAdapter`/`TagMapper`/`TagJpaRepository`/`TagQueryRepository`(6-조인 QueryDSL 이관). 소비처 28+파일 포트/도메인 재배선. 자식(TasteTag/FurnitureTag/CarouselTag)은 아직 `@ManyToOne TagJpaEntity`(임시 seam) — 각 자식 전환 시 tagId 절단 예정. `FurnitureTagRepository.findByFurnitureAndTag`→`findByFurnitureAndTagId(Furniture,Long)`. Tag 도메인/JpaEntity 둘 다 `@Builder`(테스트 편의+보일러플레이트). 단위/리포지토리/통합 테스트 green. **ArchUnit(tag 슬라이스 domain→JPA/Spring 금지) 아직 미작성 — 추가 필요.**
2. **Taste** — ✅ **완료**. `or.sopt.houme.taste.{domain, domain.port.out, infra.persistence}` 슬라이스. 순수 `Taste` + `TasteJpaEntity`(@Table tastes) + `TasteRepositoryPort`/`TastePersistenceAdapter`/`TasteMapper`/`TasteJpaRepository`/`TasteQueryRepository`(findTasteByCursor 이관). TasteService/Impl·facade·imageGenerationLog·MoodBoardResponse는 포트+순수도메인. AdminMoodBoard/HouseService는 JPA 자식(HouseTaste/TasteTag) 빌드용 `TasteJpaEntity`+`TasteJpaRepository`. 자식 HouseTaste·TasteTag는 `@ManyToOne TasteJpaEntity`(임시 seam). `HouseTasteRepository`/`TasteTagRepository.findAllByTaste(TasteJpaEntity)`. **TagQueryRepository의 QTaste→QTasteJpaEntity** 동반 수정. TasteArchUnit 추가. 단위/리포지토리/통합(Moodboard 안전망) green.
3. **TasteTag** (다음) — Taste/Tag 참조를 id로 절단(현재 `@ManyToOne TagJpaEntity`+`@ManyToOne TasteJpaEntity` seam 2개 제거 → tagId/tasteId). `TasteTagCustomRepositoryImpl`(QTagJpaEntity 조인 + TagMapper) 재작성. `TagQueryRepository`/`TasteTagRepository`가 `tasteTag.taste`/`tasteTag.tag` 네비게이션 사용 중 → id 조인으로 변경 필요.
4. **Furniture / FurnitureTag** (FurnitureTag의 TagJpaEntity seam 제거 → tagId; `.getTag()` 5곳/DTO 2곳 read model로). Furniture QueryDSL(FurnitureCustom/CurationRawProduct/FurnitureTag Impl) 다수.
5. **Carousel / CarouselTag** (CarouselTag의 TagJpaEntity seam 제거).
6. **House / HouseTaste** (House는 Banner·User도 참조 — Banner/User는 클러스터 밖, 유지/보류 판단).
- 각 엔티티: 순수 domain 모델 + `port.out` + infra(JpaEntity+Mapper+Adapter). 슬라이스 패키지 네이밍은 credit처럼 도메인 루트(`or.sopt.houme.<domain>.{domain,application,infra}`) 또는 협의.
- 크로스도메인 QueryDSL(예: `TagRepositoryImpl.findTagByUserIdAndImageId` = House·GenerateImage·Tag·TasteTag·Taste·HouseTaste 6조인)은 infra read model로 재작성. 조회 책임 위치 결정 필요.
- `GenerateImageFacade`가 taste 3서비스(TasteService/TagService/TasteTagService)를 깊게 소비 → UseCase로 재배선.
- 엔티티→presentation DTO 역방향 import 제거: `Taste.createByPreSignedURL`(AdminMoodBoardCreateRequestDTO), `Tag.update`(AdminTagUpdateRequestDTO).

### 3단계 나머지 도메인 → 4단계 gradle 7모듈 물리 분리 → dev 배포 검증 → prod 릴리즈

## 재개 체크리스트
1. `git switch feat/#582/hexagonal-multimodule` (원격에 최신) 확인.
2. develop과 drift 있으면 rebase/merge (그동안 #609 등 머지될 수 있음).
3. 이 문서의 "다음 할 일"부터, 각 단계 컴파일+영향 테스트 그린 유지하며 커밋.
4. Docker(로컬 PG 5433/Redis 6379) 필요 시 재기동 (테스트는 Testcontainers 자동).

## 참고 운영 이슈 (별건, 코드무관)
- prod Gemini v4 이미지생성이 "월 지출 상한(spending cap) 초과 429"로 실패 중 — AI Studio에서 상한 상향 필요(운영). 알림이 ChatGptException으로 오탐(이름 오해). 개선 후보: 예외 cause 체이닝/알림명 정정/429 전용 처리.
