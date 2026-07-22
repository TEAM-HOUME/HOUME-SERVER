# HOUME 서버 아키텍처 설명 (#582 전환 후)

> 2026-07-22 기준. 헥사고날(포트&어댑터) + gradle 7모듈 전환이 완료된 현재 구조를 설명한다.
> "왜 이렇게 갔는가"는 `왜_멀티모듈로_가야하는가.md`, 전환 과정 기록은 repo 의 `docs/582-handoff.md` 참조.

---

## 1. 한 장 요약

```
        [ HTTP 요청 ]
             │
   ┌─────────▼─────────┐
   │     houme-api      │  컨트롤러, 예외 advice, Security/Web/Swagger 설정, main()
   │   (유일한 bootJar)  │
   └───┬────────────┬───┘
       │            │
┌──────▼─────┐ ┌────▼──────┐
│houme-       │ │houme-auth │  JWT 발급/검증 필터, CustomUserDetails(순수 User 래핑)
│application  │ │           │
│(서비스·파사드)│ └────┬──────┘
└──────┬─────┘      │
       │            │
   ┌───▼────────────▼───┐
   │    houme-domain     │  순수 도메인 모델 + 포트(인터페이스) + View
   │  (Spring/JPA 의존 0) │
   └───▲────────────────┘
       │ implements (포트 구현)
   ┌───┴────────────────┐     ┌──────────────┐
   │    houme-infra      │────▶│houme-external │  Kakao/Naver/FastApi/
   │ @Entity·리포·어댑터· │     │ (Feign 등)    │  Gemini/Discord 클라이언트
   │ QueryDSL·스케줄러    │     └──────────────┘
   └────────────────────┘
              (houme-common: ApiResponse/ErrorCode/공유 DTO — 모두가 봄)
```

**핵심 규칙 하나만 기억하면 된다:**
> **서비스(application)는 JPA 엔티티/리포지토리를 import 할 수 없다.**
> 서비스는 `domain` 의 순수 모델과 포트만 보고, 실제 DB 접근은 `infra` 의 어댑터가 포트를 구현해서 제공한다.
> 이 규칙은 gradle 의존 그래프로 강제되어, 어기면 **컴파일 자체가 실패**한다.

---

## 2. 모듈별 역할과 의존

| 모듈 | 들어있는 것 | 컴파일 의존 |
|---|---|---|
| **houme-api** | `*Controller`, `GlobalExceptionHandler`, `SecurityConfig`/`WebConfig`/`SwaggerConfig`, `HoumeApplication` | application, auth, external (+ **runtimeOnly** infra — 빈 조립용, 코드 참조는 불가) |
| **houme-application** | `XxxService(Impl)`·파사드 중 **엔티티-프리**인 것, 인바운드 계약 인터페이스, request/response DTO | domain, common, external, auth |
| **houme-auth** | `JWTUtil`/`JWTFilter`, `CustomUserDetails(+Service)`, 토큰 관련 설정 | domain, common |
| **houme-domain** | 순수 모델(`User`, `House`, `Furniture`, `Credit`…), `port.out.*` 인터페이스, `XxxView` read model, 순수 enum | common **뿐** (Spring/JPA 의존 자체가 없음) |
| **houme-infra** | `@Entity`(`XxxJpaEntity` 포함), Spring Data 리포지토리, QueryDSL, `XxxPersistenceAdapter`/`XxxQueryAdapter`, 스케줄러·크롤러, JPA/Redis/S3 설정 | application, domain, external, auth, common |
| **houme-external** | Feign 클라이언트(Kakao·Naver·FastApi), Gemini, Discord 알림, 외부 응답 DTO | domain, common |
| **houme-common** | `ApiResponse`/`ErrorCode`/예외 타입, 공유 플레인 DTO, 상수·유틸 | (경량 spring-web·jackson·slf4j 만) |

- 패키지 이름은 기존(`or.sopt.houme.…`)을 유지한 채 **파일만 모듈로 이동**했다(스플릿 패키지 허용). git 히스토리는 rename 으로 이어진다.
- 테스트는 전부 `houme-api/src/test` 에 있다 — 통합/슬라이스 테스트가 전체 클래스패스를 필요로 하기 때문(test 스코프에서만 infra 를 직접 봄).
- QueryDSL Q-클래스 생성은 infra 모듈 전용(annotation processor 도 infra 에만).

## 3. 한 요청의 흐름 (예: 찜 토글 `POST /jjym`)

```
JjymController(api)
  → JjymOptimisticLockFacade(application)     … 낙관락 재시도 정책
    → JjymService 인터페이스(application)
      = JjymServiceImpl(application, 엔티티-프리)
        → UserRepositoryPort / RecommendFurniturePort / JjymRepositoryPort (domain 포트)
          = JjymPersistenceAdapter 등 (infra)   … 여기서 처음 JPA 등장
            → JjymRepository(JPA) → PostgreSQL
```

- 서비스가 다루는 타입: 순수 `Jjym(userId, recommendFurnitureId)`, 순수 `RecommendFurniture` — JPA 프록시/영속성 컨텍스트 걱정이 서비스 계층에서 사라짐.
- 인증도 동일: `CustomUserDetails` 가 **순수 `User`** 를 래핑하므로, 모든 서비스 시그니처가 엔티티가 아닌 도메인 `User` 를 받는다.

## 4. 자주 쓰는 패턴 3가지

### ① 순수 도메인 + 영속 어댑터 (쓰기 모델)
```java
// domain: 규칙을 도메인이 소유
public class Credit {
    public void reserve() { if (status != ACTIVE) throw …; this.status = PENDING; }
}
// infra: 조회→적용→더티체킹 (id 없으면 INSERT)
class CreditPersistenceAdapter implements CreditRepositoryPort {
    public void save(Credit c) { entity = repo.findById(c.getId()).orElseThrow(…); entity.updateStatus(c.getStatus()); }
}
```

### ② View + QueryPort (읽기 모델)
엔티티 그래프를 조립해야 하는 무거운 조회(마이페이지 히스토리, 가구 대시보드, 큐레이션 상품)는
**infra 어댑터가 fetch join 으로 자유롭게 순회한 뒤 평탄화된 `XxxView`(record)로 반환**한다.
→ N+1 없이, 서비스는 엔티티를 모른 채 화면 데이터를 얻는다.
```java
List<FurnitureWithTypeView> findAllWithType();      // FurnitureRepositoryPort
UserImageHistoryListResponse getUserImageHistoryList(Long userId);  // 응답 DTO 를 직접 반환하는 조회 포트
```

### ③ 인바운드 계약 + infra 구현 (무거운 오케스트레이션)
크롤러·이미지생성 파사드처럼 본질적으로 엔티티/외부IO 를 다루는 로직은 억지로 순수화하지 않고,
**인터페이스(계약)는 application, 구현은 infra** 에 둔다. 컨트롤러는 계약만 본다.
예: `GenerateImageFacade`(계약) ↔ `GenerateImageFacadeImpl`(infra), `SoozipCrawlingService` ↔ `Impl`.

## 5. cross-domain 참조 규칙

- 도메인 경계를 넘는 JPA `@ManyToOne` 은 **전부 절단**되어 `xxx_id(Long)` 컬럼 참조다 (FK 는 DB 가 계속 강제 → 스키마 불변).
  - 예: `Jjym.user` → `user_id`, `HouseFurniture.furniture` → `furniture_id`, `GenerateImage… → houseId` 등.
- **같은 도메인 내부**의 엔티티 연관(예: `FurnitureTag → FurnitureJpaEntity`, `CurationRawProduct ↔ 매핑`)은 유지한다.
  둘 다 houme-infra 안에 살기 때문에 모듈 경계를 해치지 않고, fetch join 성능 이점을 지킨다.
- 다른 도메인의 데이터가 필요하면 **그 도메인의 포트**를 통해 id 로 조회한다 (예: House 저장 시 가구 확인 → `FurnitureRepositoryPort.findAllById`).

## 6. 빌드·배포에서 달라진 것

| 항목 | Before | After |
|---|---|---|
| 실행 jar | `build/libs/houme-*.jar` | **`houme-api/build/libs/houme-api-*.jar`** |
| jib | `./gradlew jib` | **`./gradlew :houme-api:jib`** |
| 전체 테스트 | `./gradlew test` | 동일 (루트가 위임, 결과는 houme-api 아래) |
| Q-클래스 | `src/main/generated` | `houme-infra/src/main/generated` (gitignore) |
| 컴파일 옵션 | 부트 플러그인이 전역 `-parameters` | 루트에서 전 모듈 `-parameters` 명시(스프링 바인딩에 필수) |

## 7. 알아두면 좋은 함정·결정 기록

- **`-parameters`**: 부트 플러그인이 api 에만 있으면 다른 모듈이 파라미터명 없이 컴파일되어 `@AuthenticationPrincipal`/DataBinder 가 500 을 낸다. 루트 build.gradle 이 전 모듈에 강제하고 있으니 지우지 말 것.
- **의존 편차 4건**(설계 문서 §4 대비): application→external·auth, infra→application, api→external. 각각의 근거는 `docs/582-handoff.md` "P2 실행 로그" 절에 있다. 핵심 불변식(application↛infra, domain 무의존)은 그대로다.
- ArchUnit 슬라이스 테스트(`*ArchitectureTest`)는 모듈 강제와 별개로 도메인 내부 레이어 규칙 문서화용으로 유지한다.
- 새 도메인을 추가할 때의 표준 절차는 `docs/hexagonal-pattern-guide.md`(credit 파일럿 기준)를 따른다:
  `domain(모델+port.out) → infra(JpaEntity+Mapper+Adapter) → application(Service) → api(Controller)` 순서로.

## 8. 다음 과제 (선택)

1. application 이 external 클라이언트를 직접 소비하는 지점의 포트화(도메인 포트 뒤로).
2. curation 서브도메인(12개 엔티티) 내부 그래프 서비스의 심화 분해.
3. CI 에서 모듈별 병렬 빌드/테스트 활용(변경 모듈만 재빌드되는 이점 취하기).
