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
6. **Furniture / FurnitureTag** — cross-domain 디커플링 ✅ 완료(Step A, 커밋 `7cdf153`). **F-2(3단계 실용범위 순수화) ✅ 완료 — 커밋 `5219089`**: 순수 `or.sopt.houme.furniture.domain.Furniture`(furnitureTypeId+scalars, `reconstitute` 팩토리) + `domain.port.out.FurnitureRepositoryPort`(현재 `findAllById(List<Long>)→List<Furniture>` 만 노출) + `infra.persistence.FurnitureMapper`/`FurniturePersistenceAdapter`(기존 `FurnitureRepository` JpaRepo 재사용, 경계 넘을 때만 매핑). **cross-domain 소비처 2곳만 포트 전환**: `HouseServiceImpl.saveHouseFurniture`(getId), `UserServiceImpl` 가구명 매핑(getFurnitureNameKr)+`resolveFurnitureSummaryName(Furniture)`. `UserServiceImplTest` mock/빌더 6곳 순수 Furniture 로 전환. `FurnitureArchitectureTest` 추가(furniture.domain→JPA/Spring/infra 금지). 전체 스위트 green(9m53s).
   - **범위 결정(사용자 2026-07-18): 실용범위 채택.** 근거: 실제 `FurnitureRepository` 소비처 7곳 중 순수 도메인이 깔끔히 서빙하는 건 스칼라만 읽는 cross-domain 2곳(House/User)뿐. 나머지(FurnitureServiceImpl·CurationProduct/RawProductFurniture·AdminFurniture·AdminCurationRawProduct)는 `getFurnitureType()`/`getFurnitureTags()` 그래프 순회이고 **전부 furniture 도메인 내부** → 모듈 경계는 이미 clean. FurnitureTag/ActivityFurniture/CurationRawProductFurniture(Tag) 의 `@ManyToOne FurnitureJpaEntity` 는 전부 furniture 내부 연관이라 절단 불필요(gradle 분리 무영향). **완전 순수화(그 @ManyToOne→furnitureId(Long) 절단 + 13개 getFurnitureType read 재작성 + QueryDSL)는 curation 서브도메인 헥사고날화 시로 이관** = 아래 #10/#11 잔여.
   - **주의(잔여 cross-domain 그래프 순회):** `GenerateImageResultServiceImpl:139-143` 이 `CurationRawProductFurnitureTag.getFurnitureTag().getFurniture().getFurnitureType().getId()` 로 furniture JPA 그래프를 순회(리포 아님). curation read-model/포트화 때 함께 해소 필요.
   - **(이하 F-1 상세)** **F-1(엔티티 리네임) ✅ 완료 — 커밋 `cd3563e`**: `Furniture` @Entity → `or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity`(House 12b-1 과 동일 패턴). `QFurniture`→`QFurnitureJpaEntity`(정적 접근자 `.furniture`→`.furnitureJpaEntity`), 자식 @ManyToOne(FurnitureTag/CurationRawProductFurniture/ActivityFurniture)·QueryDSL(FurnitureCustom/FurnitureTag/CurationRawProduct/CurationRawProductFurniture RepositoryImpl)·서비스·DTO·테스트 34파일 리네임. @ManyToOne FurnitureType + @OneToMany FurnitureTag 는 infra 내부 유지. JPQL 은 전부 필드/별칭 네비게이션이라 무영향. 전체 스위트 green(9m52s). **순수 Furniture 도메인+Port/Adapter/Mapper + FurnitureTag 순수화(furnitureId Long)는 3단계 furniture 도메인 헥사고날화로 이관** — House 12b-2 와 동일 판단(#8 로 유일한 cross-cluster seam(HouseFurniture→Furniture) 이미 절단 → 남은 연관 전부 furniture 내부 = 모듈 경계 clean, 지금 순수화해도 cross-domain write 얽힘 없어 throwaway 는 아니나 stage-3 성격). 3단계 착수 시 아래 4번 하위 항목의 13개 getFurnitureType read 정밀 매핑 그대로 활용.
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

## 3단계 실행계획 (확정 2026-07-21, 사용자 승인)
- **목표 모듈 = 레이어 기반 7모듈**(문서 §4): api/application/auth/infra/external/domain/common. `application`은 `infra`를 의존하지 않음 → **모든 서비스가 JpaEntity 참조 불가**. 즉 남은 전 도메인 **정석 완전 헥사고날화** 필요(단순 seam 절단 아님).
- **작업 원칙**(사용자 지시): ① 커밋+푸시까지만(배포·릴리즈는 사용자) ② **각 API 영역 리팩터 전 통합테스트 선행**(계약 무결성 방어선) ③ 갈림길은 추천안으로 진행+기록(멈추지 않음).
- **도메인 순서 재조정(2026-07-21):** 원래 banner→...였으나, **banner가 furniture/curation의 읽기 소비자**(CurationRawProduct 상품/색상, Jjym/RecommendFurniture 좋아요)임을 발견 → 의존도 기반으로 변경. 서비스들이 서로 cross-domain read로 얽혀 있어(순환), cross-domain read는 **소유 도메인의 조회 포트(inbound query port)** 를 infra 어댑터가 read-model 반환으로 구현해 해소(strangler).
  - **확정 순서: user → furniture/curation → house(12b-2) → generateImage → preference → banner → generateImageResult.** (foundational 먼저; banner는 curation 의존이라 후순위)
- **도메인 1개 사이클:** 안전망 통합테스트 선행 → 순수 모델+포트(domain)/JpaEntity·어댑터·QueryDSL(infra)/서비스→application(JpaEntity 참조 0)/나가는 cross-module @ManyToOne→Long 절단/슬라이스 ArchUnit → 타깃+전체 green → 커밋·푸시 → 이 문서+태스크 갱신.
- **참고(조사완료, banner 차례에 활용):** BannerServiceImpl은 CurationRawProduct(getId/getProductId), CurationRawProductColor(색상명), RecommendFurniture/Jjym(좋아요), ProductColorResponse, OtherStyleDetailProductResponse.from(rawProduct,...) 에 의존. banner 자체 aggregate 는 Banner/BannerCurationRawProduct(+BannerType) 로 작음. BannerCurationRawProduct→CurationRawProduct seam 절단(curation_raw_product_id Long) + curation 조회 포트 소비로 전환 예정.

### 3단계 진행현황 (도메인별)
- **user** ✅ **완료** (U-1 `0232a36` / U-2 `497e4f5` / U-2b `01d23ca` / U-3 `109a7b6`):
  - ✅ **U-1 UserJpaEntity 리네임 완료 — 커밋 `0232a36`.** `User` @Entity → `or.sopt.houme.user.infra.persistence.UserJpaEntity`. QUser→QUserJpaEntity(`QUser.user`→`QUserJpaEntity.userJpaEntity`). enum(Gender/Role/SocialType/UserStatus)은 `domain.user.model.entity` 유지. 99파일, 무동작변경, 전체 green(9m55s). **주의:** User는 인증 principal이라 대부분 서비스 시그니처가 지금 `UserJpaEntity`(임시) — 순수 User로의 시그니처 이관은 Sweep 2.
    - 리네임 함정 기록: 소비처가 명시 import뿐 아니라 **와일드카드 `entity.*`·FQN**도 있어 3패스 필요했음. import 삽입 perl은 `perl -0777 -i -pe`(‐pi ‐e 형식은 실패) 사용. 문자열 `"User-Agent"`/`"User's"` 사후복구.
  - U-2: 순수 User+Port/Adapter/Mapper+ArchUnit 완료. U-2b: 7개 엔티티(House/Jjym/PaymentBtnClickLog/FurnitureRecommendBtnClickLog/InvalidHouseRequest/Address/CarouselLikeLog) user→user_id(Long) 절단+QueryDSL id-조인 완료 — 어떤 엔티티도 UserJpaEntity 연관 없음. U-3: CustomUserDetails 가 순수 User 래핑, 전 서비스/컨트롤러 시그니처 User 로 플립, user 서비스들 포트 소비(saveAndFlush 로 닉네임태그 재시도 타이밍 보존). UserJpaEntity 는 user.infra 내부 전용.
  - 잔여(Sweep 2): UserServiceImpl mypage-history 의 house/generateImage/preference 리포 직접소비 → 각 도메인 조회 포트로(해당 도메인 전환 시).
- **잔여 도메인:** furniture/curation → house(12b-2) → generateImage → preference → banner → generateImageResult. 각 도메인 진입 시 incoming `@ManyToOne UserJpaEntity`→userId(Long) 절단(house/furniture/credit PaymentBtnClickLog).

### 4단계 gradle 7모듈 물리 분리 → dev 배포 검증(사용자) → prod 릴리즈(사용자)

## 재개 체크리스트
1. `git switch feat/#582/hexagonal-multimodule` (원격에 최신) 확인.
2. develop과 drift 있으면 rebase/merge (그동안 #609 등 머지될 수 있음).
3. 이 문서의 "다음 할 일"부터, 각 단계 컴파일+영향 테스트 그린 유지하며 커밋.
4. Docker(로컬 PG 5433/Redis 6379) 필요 시 재기동 (테스트는 Testcontainers 자동).

## 참고 운영 이슈 (별건, 코드무관)
- prod Gemini v4 이미지생성이 "월 지출 상한(spending cap) 초과 429"로 실패 중 — AI Studio에서 상한 상향 필요(운영). 알림이 ChatGptException으로 오탐(이름 오해). 개선 후보: 예외 cause 체이닝/알림명 정정/429 전용 처리.

## 핵심 설계 결정 (2026-07-22, 레이어 7모듈 확정 이후)
- **레이어 분리에서 절단 대상은 "서비스(application)→엔티티/리포" 의존이지, infra 내부 엔티티↔엔티티 연관이 아니다.** FurnitureTag→Furniture, CurationRawProduct↔매핑, Carousel→CarouselType 같은 **동일-infra 내부 JPA 연관은 유지**한다(전부 houme-infra 한 모듈에 같이 살게 됨). cross-domain 연관은 스테이지2에서 이미 전부 절단됨.
- **패턴:** application 서비스는 `<domain>.domain.port.out.XxxPort`(순수 모델/View 반환)만 소비. **어댑터는 infra 에서 엔티티 그래프(fetch join 포함)를 자유롭게 순회**해 View 로 평탄화(N+1 회피 유지). 무거운 QueryDSL 은 재작성하지 않고 어댑터 뒤로 숨김.
- View 네이밍: `XxxView`(flat record/불변). DTO 팩토리(presentation)는 View/순수 모델만 받는다.
- 서비스별 전환 레시피: ①사용 리포 메서드 목록화 ②포트에 미러 메서드(View 반환) ③어댑터 위임+매핑 ④서비스 타입/임포트 스왑 ⑤DTO 팩토리 View 화 ⑥목 테스트 플립.
- P2 모듈 배정(패키지 이동 아님, source-set 배정): 엔티티/리포/어댑터/QueryDSL/크롤러·외부클라이언트 오케스트레이션=infra, 서비스·파사드·response DTO=application, 컨트롤러=api, JWT/Security=auth, 순수 도메인+포트=domain.

## 2026-07-22 진행 로그 (스테이지3, run-to-completion)
- **user 완료**: U-2(순수 User+포트 `497e4f5`) U-2b(7엔티티 user_id 절단 `01d23ca`) U-3(principal·전서비스 순수 User 플립 `109a7b6`) + **히스토리 조립 UserImageHistoryQueryPort/Adapter 이관 `6761a25`**(UserServiceImpl 완전 엔티티-프리, updateUser v1 저장누락 버그 수정 포함).
- **furniture/curation**: FC-1(Jjym·RF 순수+포트, Jjym→RF 절단 `c54d67d`) FC-2/2b(RF·Jjym 소비처 전부 포트 `f71f349`,`d682c89`) FC-3(FurnitureService 완전 포트/View, CurationFurniture 계열 공개계약 View·리포 id-기반 `cd0ef2c`). **infra 내부 연관(FurnitureTag→Furniture, CurationRawProduct↔매핑, ActivityFurniture→Furniture 등)은 유지가 설계 결정**(레이어 분리에 불필요).
- **api 레이어 격리 완료 `7189412`**: presentation DTO 18파일 엔티티-프리(팩토리 제거/인라인/View화/infra 매퍼 이관). presentation 에서 @Entity 직접참조 0. 컨트롤러 엔티티 import 는 순수 enum 뿐(P2 때 domain 모듈 배정).
- **분류 원칙(P2 파일→모듈 배정)**: 서비스가 엔티티/리포 import 하면 infra(컨트롤러가 부르면 인터페이스 파일은 domain 배정), 엔티티-프리면 application. 공유 response 레코드는 common. houme-application 은 `api project(':houme-domain')` 로 domain 재노출.
- **남은 필수 전환**: ①HouseServiceImpl(쓰기 오케스트레이션: 순수 House 12b-2+HouseFloorPlan/InvalidHouseRequest/FloorPlan/Taste 포트) ②GenerateImage 핵심(Facade/TransactionService/ServiceImpl/LikeFacade — GenerateImagePort 커맨드·조회, house 연관은 infra 유지, createGenerateImage(houseId) 커맨드화) ③잔여 서비스는 infra 분류로 충분(Banner/Carousel/Address/Preference/GIR/Admin — 인터페이스 존재 확인만).
- 게이트 정책: 중간커밋=컴파일+타깃테스트, 전체 스위트=도메인 완료·푸시 전(-x jacocoTestReport 로컬 생략). 병렬화는 팀이 롤백한 결정이라 안 건드림.

## P2 물리 7모듈 분리 — 실행 설계 (확정)
**모듈 의존 (doc §4 에서 1개 수정: infra→application 허용 — 어댑터가 application 인터페이스/DTO 구현·반환하는 고전 헥사고날 방향. 근거: infra-분류 서비스(Banner/Curation/Admin/GIR 등)가 컨트롤러-공유 인터페이스와 response DTO 를 갖기 때문. application→infra 는 여전히 금지라 비순환.)**
```
houme-api ───────▶ application, auth, domain, common (runtimeOnly: infra, external)  [bootJar, main, 컨트롤러, advice, web/swagger config]
houme-application ▶ domain, common  [엔티티-프리 서비스·파사드·인터페이스·presentation DTO, spring core/tx/cache/security-core/dao]
houme-auth ──────▶ domain, common  [SecurityConfig, JWT util/filter, CustomUserDetails(+Service)]
houme-infra ─────▶ application, domain, external, common  [@Entity·리포·어댑터·QueryDSL·스케줄러·크롤러·엔티티-사용 서비스, JPA/Redis/Redisson/S3/H2/PG config]
houme-external ──▶ common  [Feign 클라이언트(KaKao/Naver/FastApi)·Gemini·Discord + 외부 DTO]
houme-domain ────▶ common  [or.sopt.houme.*.domain.**, 순수 enum(Gender/Role/Activity/Form/... model.entity 에서 파일단위 분리), View/포트]
houme-common ────▶ (없음)  [ApiResponse/ErrorCode/예외타입/공용 유틸 중 무프레임워크]
```
**파일 분류 규칙(스크립트)**: ①`or/sopt/houme/*/domain/**`→domain ②`*/infra/**`→infra ③`*Controller.java`+`HoumeApplication`+advice/web·swagger config→api ④`@Entity|Repository|QueryDSL|Scheduler|crawler` 포함→infra ⑤jwt/security/CustomUserDetails*→auth ⑥`infrastructure/client|dto/external`→external ⑦presentation dto·엔티티-프리 서비스/파사드/인터페이스→application ⑧global: api(ApiResponse 등)→common, config 는 성격별(JPA/Redis/S3→infra, Security→auth, Web/Swagger→api) ⑨순수 enum 파일→domain. 스플릿 패키지 허용(JPMS 아님).
**테스트**: 전부 houme-api/src/test 로 이동(통합·@DataJpaTest 가 전체 클래스패스 필요; api 는 testImplementation 으로 infra/external 접근). Testcontainers/Jacoco/jib 설정도 api 로.
**QueryDSL**: annotationProcessor 는 infra 모듈에만; generated dir 도 infra.
**순서**: settings.gradle→모듈 build.gradle 뼈대→분류 스크립트로 git mv→모듈별 컴파일 에러 순회 수정→bootJar→전체 테스트→커밋·푸시.

## P2 실행 로그 (2026-07-22)
- 650파일 git mv 완료(7모듈), 테스트 전체 houme-api/src/test 로, 리소스 houme-api/src/main/resources.
- **zsh 함정: 루프 변수명 `path` 금지($PATH 클로버)** — `src_file` 등으로.
- 전 모듈 main 컴파일 green + bootJar green. 컴파일 캐스케이드에서 확정된 조정:
  - **의존 편차(기록)**: application→external(클라이언트 직접 소비, 포트화 후속), application→auth(JWTUtil/CustomUserDetails), infra→auth(BCrypt/JWTUtil), api→external(플랜용 외부 DTO 반환 컨트롤러), common 은 경량 spring-web/context/boot+jackson+slf4j 허용.
  - 재배정: GlobalExceptionHandler·SecurityConfig→api(부트 조립), TraceIdFilter→auth(JWTFilter 가 사용), JWTConfig/CookieConfig→common, KaKao DTO 3종→common, 히스토리 응답 DTO 4종+Prompt DTO 2종→domain, OAuth/JWT/UserLanding 서비스→application(포트 인터페이스 import 가 리포 규칙 오탐), AdminServiceImpl/UserDeletionServiceImpl/FurnitureFacadeImpl/openai 파사드 Impl→infra.
  - 추가 인터페이스 추출: SoozipCrawlingService, CurationRawProductService(컨트롤러 소비 인바운드 계약; Impl 은 infra).
  - 락 파사드는 Impl 대신 인터페이스(JjymService/CarouselService) 소비로 수정.
  - 테스트 클래스패스: houme-api testImplementation 에 JPA/AWS/QueryDSL/Redisson/PG/H2 부여.
  - Jacoco/test 설정(maxParallelForks=1 등) houme-api 로 이식. jib 는 houme-api 플러그인.
- 다음: 전체 스위트 게이트(진행 중) → 실패 수정 → 커밋·푸시. **동시 gradle 실행 절대 금지(게이트 오염 2회 발생)**.

## ✅ P2 완료 (2026-07-22) — #582 코드 작업 종료
- 커밋 `1682afc`(7모듈 분리) + `b9c5b8f`(generated ignore). **전체 스위트 green(10m24s), bootJar green, 실패 0.** 원격 푸시됨.
- 결정적 버그와 해결: **부트 플러그인이 api 에만 있어 타 모듈이 `-parameters` 없이 컴파일 → @AuthenticationPrincipal(CustomUserDetails 생성자) DataBinder 파라미터명 소실로 슬라이스 테스트 500.** 루트 subprojects 에 `-parameters` 전역 적용으로 해결.
- 남은 것(코드 외): dev 배포 검증→prod 릴리즈(사용자 몫). jib 이미지 빌드는 `:houme-api:jib*`. CI 스크립트가 `./gradlew build`/`test` 경로 가정이면 그대로 동작(루트 위임), bootJar 산출물 경로만 houme-api/build/libs 로 변경 주의.
- 후속(선택): application→external 직접 소비의 포트화, ArchUnit 모듈경계 테스트는 이제 불필요(컴파일 강제)하나 슬라이스 규칙 테스트는 유지.
