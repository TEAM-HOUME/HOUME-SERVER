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
3. **TasteTag** — ✅ **완료**. `or.sopt.houme.tastetag.{domain, domain.port.out, infra.persistence}` 슬라이스. 순수 `TasteTag`(tasteId/tagId Long 참조) + `TasteTagJpaEntity`(@Table taste_tags, taste_id/tag_id Long 컬럼, @ManyToOne 제거) + `TasteTagRepositoryPort`/`Adapter`/`Mapper`/`JpaRepository`/`QueryRepository`(findBestTasteId/findBestTasteIdList/findDistinctTagsByTasteIdIn 을 **id 명시 조인**으로 재작성, TagJpaEntity 반환→TagMapper 매핑). TasteTagServiceImpl→포트. AdminMoodBoard: 생성 시 taste 저장 후 `TasteTag.of(savedTaste.getId(), tag.getId())` 순서로, 삭제 시 `deleteAllByTasteId`. **TagQueryRepository**의 tasteTag.tag/tasteTag.taste 네비게이션→tagId/tasteId id 조인 동반 수정. **특성화 테스트**(`TasteTagCustomRepositoryTest`, 커버리지 없던 3쿼리 동작 고정) 신규→전환 전후 동일 통과 확인. TasteTagArchUnit 추가. → **seam 2개 제거 완료** (TasteTag는 이제 Tag/Taste를 id로만 참조).
4. **FurnitureTag → Tag seam 절단** — ✅ **완료 (Step A)**. 기존 `FurnitureTag` @Entity 유지한 채 `@ManyToOne TagJpaEntity tag` → `@Column(name="tag_id") Long tagId` 로 절단. 소비처 재배선: `AdminFurnitureServiceImpl.getFurniture`/`getFurnitureTagsByType`(태그명 tagId 로 일괄조회), `AdminFurnitureTagOptionResponse.of(ft, tagNameKr)`, `AdminCurationRawProductFurnitureTagResponse.of(mapping, tagNameKr)`(3콜러+`AdminCurationRawProductServiceImpl`에 `TagJpaRepository` 주입/`resolveTagName`), `GenerateImageResultServiceImpl.getSimilarItems`(`getTag().getId()`→`getTagId()`). QueryDSL id-join 재작성: `FurnitureTagRepositoryImpl`(tag.id→tagId, tag fetchJoin 제거), `FurnitureCustomRepositoryImpl.findAllWithTags`(tag fetchJoin 제거), `CurationRawProductRepositoryImpl.findAllSimilarByTagIds`(tag 조인 제거, `furnitureTag.tagId` 필터), `CurationRawProductFurnitureTagRepository` JPQL(`left join fetch furnitureTag.tag` 제거). `createByAdminFurniturePromptRequestDTO(...,Long tagId,...)`. **무커버리지였던 `AdminCurationRawProductFurnitureTagResponse` 에 특성화 테스트 선작성**(`AdminCurationRawProductFurnitureTagResponseTest`). 테스트 빌더 `.tag(...)`→`.tagId(...)` 다수 파일. → **마지막 Tag seam 제거 = Tag 도메인 엄격 순수화 완성**(어떤 JPA 엔티티도 TagJpaEntity 연관 미보유). 단위/통합(curation API·moodboard)·컨트롤러 안전망 green.
   - **Furniture 자체 순수화는 House 단계로 이관 (사용자 결정 2026-07-18)**. 근거: Furniture 의 cross-domain JPA 연관은 딱 2개였는데 ①FurnitureTag→Tag 는 Step A 로 절단 완료, ②HouseFurniture(house)→Furniture 는 House 전환 시 절단. 나머지(Furniture↔FurnitureType↔FurnitureTag↔Curation*)는 전부 furniture 도메인 내부. pure Furniture 를 실제 소비하는 쪽이 House 라, House 와 함께 하면 재작업/churn 최소. **정밀 매핑 결과(재개 시 그대로 사용):** `.getFurnitureType()` 실제 메서드 read 는 **13곳뿐**(FurnitureServiceImpl:91[getId만],:200[nameEng]; CurationProductServiceImpl:394,398; CurationProductTokenService:94; GenerateImageResultServiceImpl:141[getId만]; FurnitureItem:21; AdminCurationRawProductFurnitureResponse:22-23; AdminCurationRawProductFurnitureTagResponse:32-33; AdminFurnitureTagOptionResponse:40) — **전부 persistence 에서 얻은 Furniture(레포/시엄 getFurniture())라 FurnitureJpaEntity 로 두면 그대로 동작**(대부분 CurationRawProductFurnitureTag/HouseFurniture 그래프 순회). seam 엔티티=ActivityFurniture/CurationFurniture(→FurnitureTag)/CurationRawProductFurniture/CurationRawProductFurnitureTag(→FurnitureTag)/HouseFurniture(→Furniture). **Jjym·RecommendFurniture 는 Furniture 무관(RecommendFurniture 만 참조) — false positive**. `Furniture.builder().furnitureType(...)` 쓰는 테스트 6파일이 컴파일 깨짐(예상). `furniture` 슬라이스: 순수 Furniture(furnitureTypeId+scalars) + FurnitureJpaEntity(@ManyToOne FurnitureType, @OneToMany FurnitureTagJpaEntity infra 유지) + Port/Adapter/Mapper/JpaRepo/QueryRepo. FurnitureTag 도 슬라이스화(furnitureId Long, tagId 는 이미 Long).
5. **Carousel / CarouselTag** — ✅ **완료**. `or.sopt.houme.carousel.{domain, domain.port.out, infra.persistence}` 슬라이스. 순수 `Carousel`(carouselTypeId 참조) + `CarouselJpaEntity`(@Table carousels, @ManyToOne CarouselType 는 infra 에 유지) + `CarouselRepositoryPort`/`Adapter`/`Mapper`/`JpaRepository`. CarouselCacheService→포트(타입 그룹핑을 carouselTypeId 로), CarouselServiceImpl.findCarousel 은 JPA 자식(CarouselPreference) 빌드용 `CarouselJpaRepository`. `CarouselTag` 는 코드 미사용 매핑이라 `carouseltag.infra.persistence.CarouselTagJpaEntity`(carousel_id/tag_id Long)로만 전환 → **Tag seam 제거**. CarouselPreference.carousel→CarouselJpaEntity. CarouselArchUnit 추가. 안전망(CarouselApiIntegrationTest)+낙관락 파사드 테스트 green.
6. **Furniture / FurnitureTag** — cross-domain 디커플링 ✅ 완료(Step A, 커밋 `7cdf153`). 엔티티 완전 순수화는 **7번 House 단계에 묶음**(위 4번 하위 항목의 정밀 매핑 참고).
7. **House / HouseTaste (+ Furniture 순수화 동반)** — **클러스터 마지막·가장 무거움**. 앱 전역에서 참조(GenerateImage·FloorPlan·User history·facade). House는 Banner·User도 참조(클러스터 밖 → seam 유지). 이 단계에서 함께: (a) HouseFurniture.furniture(@ManyToOne Furniture)→Long furnitureId 절단, (b) Furniture 완전 순수화(pure Furniture+FurnitureJpaEntity+Port, House가 포트로 소비), (c) FurnitureTag 슬라이스화(furnitureId Long), (d) House/HouseTaste 순수화. 4번 하위 항목의 13개 getFurnitureType read 매핑을 그대로 활용.
   - **(a) 완료 — 커밋 `6e54db0`.** HouseFurniture `@ManyToOne Furniture`→`furniture_id(Long)`. read 3곳(CarouselCandidateService id-only, UserServiceImpl FULL_FUNNEL 가구명 fallback 2곳→FurnitureRepository 주입 후 furnitureId 일괄 이름조회), QueryDSL(HouseFurnitureRepositoryImpl furniture/furnitureType fetchJoin 제거, FurnitureCustomRepositoryImpl.findAllByHouseId id 명시 조인), write seam(HouseServiceImpl.saveHouseFurniture `.furnitureId(f.getId())`, findAllById 실존필터 동작 보존). 전체 스위트 green.
   - **HouseTaste→Taste seam 완료 — 커밋 `a95c519`.** HouseTaste `@ManyToOne TasteJpaEntity`→`taste_id(Long)`. read 0곳. HouseTasteRepository `findAllByTaste(TasteJpaEntity)`→`findAllByTasteId(Long)`(+AdminMoodBoardServiceImpl.delete 콜러), TagQueryRepository `houseTaste.taste.eq(taste)`→`houseTaste.tasteId.eq(taste.id)` 2곳, write seam(saveHouseTaste `.tasteId(t.getId())`). @DataJpaTest(TagRepositoryImplTest/UserRepositoryImplTest) 실 DB green.
   - **HouseFurniture/HouseTaste→House seam 완료 (12a) — 커밋 `e45972a`.** 두 매핑 `@ManyToOne House`→`house_id(Long)`. 이제 **완전 순수 매핑 슬라이스**(HouseFurniture=houseId+furnitureId, HouseTaste=houseId+tasteId, 모두 Long). read: UserServiceImpl 가구명 fallback `getHouse().getId()`→`getHouseId()`. QueryDSL: HouseFurnitureRepositoryImpl/FurnitureCustomRepositoryImpl `houseFurniture.house.id`→`houseFurniture.houseId`, TagQueryRepository `houseTaste.house` 조인/필터→`houseTaste.houseId`(`join(house).on(houseTaste.houseId.eq(house.id))`/`where houseTaste.houseId.eq`). write: save{Furniture,Taste} `.houseId(house.getId())`. 전체 스위트 green.
   - **사용자 결정 2026-07-18: 남은 무거운 순수화 3건 중 House 슬라이스(#12) 먼저.** Furniture/FurnitureTag 완전 순수화(#10/#11)는 이후 별도 furniture-도메인 패스로 연기(근거: #8 절단 후 House 는 Furniture 를 소비하지 않아 독립적; Furniture 는 cross-module 연관이 이미 다 끊겨 모듈 경계 clean).
   - **12b-1 완료 — 커밋 `2d296bc`.** `House` @Entity → `or.sopt.houme.house.infra.persistence.HouseJpaEntity` 리네임(infra 슬라이스 이전). `QHouse`→`QHouseJpaEntity`(정적 접근자 `QHouse.house`→`QHouseJpaEntity.houseJpaEntity`), `clearBannerReference` JPQL 엔티티명 `House`→`HouseJpaEntity`, 자식 @ManyToOne(GenerateImage/HouseFloorPlan/PromptPreference)·QueryDSL(HouseCustomRepositoryImpl/GenerateImageRepositoryImpl/UserRepositoryImpl/PreferenceRepositoryImpl/TagQueryRepository)·서비스·테스트 33파일 전 소비처 리네임. @ManyToOne User/Banner + @OneToMany generateImages/houseFloorPlans 유지. 동작/스키마 불변, 전체 스위트 green(9m31s).
   - **12b-2(순수 House 도메인+Port/Adapter/Mapper)는 3단계로 이관 — 사용자 결정 2026-07-18.** 근거: House 에 남은 JPA 연관(GenerateImage→House, PromptPreference→House, House→User/Banner)은 **전부 아직 미전환 도메인(GenerateImage/Preference/User/Banner) 소유**라 각 도메인 3단계 전환 때 함께 절단하는 게 자연스럽다. write-path(`houseService.createTemplateHouse`→GenerateImageTransactionService/Facade가 `HouseJpaEntity` 보유→`createGenerateImage(request,house)`→`GenerateImage.house` @ManyToOne)가 미전환 GenerateImage 도메인과 깊게 얽혀 있어, 순수 House 를 지금 도입하면 `getReferenceById` 우회를 넣어야 하는데 이는 3단계에서 어차피 걷어낼 임시코드. 순수 House 의 실익(HouseService→application 계층화)은 House 도메인 헥사고날화 때 발현. read 소비처는 대부분 `house.getId()` 스칼라(FurnitureServiceImpl:243, CarouselCandidateService:32, UserDeletionService, User history)라 순수화 자체는 쉬우나 write-path 얽힘 때문에 함께 미룸.
   - **3단계 House 도메인 헥사고날화 시 할 일(예약):** 순수 `House` 도메인(id/activity/userId/bannerId/isValid/housePrompt) + `HouseRepositoryPort`/`HousePersistenceAdapter`/`HouseMapper` + HouseService→application UseCase. 이때 GenerateImage→House / PromptPreference→House seam 을 houseId(Long) 로 절단, House→User/Banner 는 User/Banner 전환에 맞춰 절단. `createGenerateImage` write-path 는 `houseJpaRepository.getReferenceById(house.id)` 로 관리참조 해소(또는 GenerateImage 가 houseId 로 저장하도록 전환).
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
