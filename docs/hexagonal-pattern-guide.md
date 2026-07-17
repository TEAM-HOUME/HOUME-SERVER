# 헥사고날 전환 패턴 가이드 (credit 파일럿 기준)

> #581에서 credit 도메인으로 확립한 표준 패턴. 이후 전 도메인 전환(#582)은 이 문서를 기준으로 진행한다.

## 목표 레이어와 의존 방향

```
api ──────> application, auth          (조립·컨트롤러·웹 DTO)
application > domain, common           (유스케이스·트랜잭션, infra를 모름)
auth ──────> domain, common
infra ─────> domain, external, common  (JpaEntity·QueryDSL·Redis·S3 어댑터)
external ──> common
domain ────> common                    (순수 도메인 + 포트, JPA 없음)
common ────> (없음)
```

의존은 **항상 안쪽으로만**. domain은 프레임워크(JPA/Spring)를 모른다.

## 패키지 구조 (모놀리스 내 미리보기)

물리 모듈 분리(#582) 전까지, 도메인별로 아래 슬라이스를 만든다. credit 예시:

```
or.sopt.houme.credit
├── domain                     # 순수 도메인 (JPA/Spring 없음)
│   ├── Credit                 #   도메인 모델 — 상태 전이 규칙 소유 (reserve/restore)
│   ├── CreditStatus
│   ├── CreditReservation      #   값 객체(핸들) — 엔티티를 밖으로 노출하지 않기 위함
│   └── port.out               #   아웃바운드 포트
│       ├── CreditRepositoryPort
│       └── CreditLockPort
├── application                # 유스케이스 (트랜잭션·락 오케스트레이션)
│   ├── CreditUseCase          #   인바운드 포트 (다른 도메인/api가 의존하는 유일한 표면)
│   └── CreditService          #   구현 (@Service, @Transactional)
└── infra                      # 어댑터 (포트 구현)
    ├── persistence
    │   ├── CreditJpaEntity    #   영속 전용 타입 (도메인 모델과 분리)
    │   ├── CreditJpaRepository #  Spring Data
    │   ├── CreditQueryRepository # QueryDSL
    │   ├── CreditMapper        #   엔티티 ↔ 도메인
    │   └── CreditPersistenceAdapter  # implements CreditRepositoryPort
    └── lock
        └── RedissonCreditLockAdapter # implements CreditLockPort
```

## 핵심 규칙

1. **도메인 모델 ≠ JPA 엔티티.** 도메인 모델(`Credit`)에는 JPA 어노테이션이 없다. 영속화는 `CreditJpaEntity` + 매퍼가 담당한다. lazy loading이 사라지므로, 조회 화면은 명시적 read model로 재작성한다.
2. **엔티티를 레이어 밖으로 노출하지 않는다.** 다른 도메인에는 원시 식별자(`Long userId`)나 값 객체(`CreditReservation`)만 넘긴다. (기존엔 `Credit` JPA 엔티티가 generateImage까지 새어 있었다.)
3. **포트로만 소통.** application은 `CreditRepositoryPort`/`CreditLockPort`(아웃바운드)만 알고, 다른 도메인은 `CreditUseCase`(인바운드)만 안다. 구현은 infra가 주입.
4. **트랜잭션/락은 application이 오케스트레이션.** 도메인은 순수 규칙만. Redisson 락의 tx-동기화 해제 같은 Spring 결합은 infra 어댑터(`RedissonCreditLockAdapter`)에 가둔다.
5. **DB 스키마 불변.** ddl-auto=update 환경이므로 JpaEntity의 테이블/컬럼 매핑이 기존 스키마와 완전히 일치해야 한다 (credits: id/status/user_id/created_at/updated_at). user는 `@ManyToOne` 대신 `user_id` 컬럼(Long)으로만 참조.
6. **API 계약 불변.** 컨트롤러/응답 포맷/엔드포인트는 그대로. 리팩 전 HTTP 통합 테스트를 먼저 그린으로 고정하고(→ `CreditApiIntegrationTest`), 리팩 후 동일 테스트로 정합성을 증명한다.

## 강제 수단 (이중)

- **ArchUnit** (`CreditArchitectureTest`): domain→JPA/Spring 금지, application→infra 금지. 전 도메인 전환 시 이 규칙을 확대한다.
- **컴파일 타임(모듈 의존)**: #582에서 gradle 7-모듈 물리 분리 후엔 레이어 위반이 컴파일 자체가 안 된다.

## 전환 절차 (도메인 1개 = PR 1개)

1. 해당 도메인의 **HTTP 계약 통합 테스트**를 현재 코드 기준으로 작성 → 그린 확인 (안전망).
2. `domain`(순수 모델+포트) → `infra`(어댑터) → `application`(유스케이스) 순으로 신규 작성.
3. 호출부를 인바운드 포트(UseCase)로 재배선하고, 엔티티 누수를 값 객체/식별자로 대체.
4. 옛 엔티티/서비스/리포지토리 삭제 (도메인 외부에서 쓰이던 별개 관심사는 보존).
5. ArchUnit 규칙 확대, 통합 테스트 그린 유지 확인.
6. 물리 분리(#582 마지막 단계) 전까지는 모놀리스 패키지 구조로만 진행.

## 참고: 크레딧 예약(reserve) 흐름

이미지 생성은 `reserve`(ACTIVE→PENDING, 락 획득) → 성공 시 `commit`(삭제) / 실패 시 `rollback`(ACTIVE 복구) → `releaseLock` 순으로 동작한다. 호출자(generateImage)는 `CreditReservation` 핸들만 들고, 상태 검사·엔티티 접근을 하지 않는다. `rollback`은 멱등(이미 커밋/복구됐으면 무시)이라 호출자의 상태 분기가 필요 없다.
