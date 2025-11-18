# HOUME 멀티모듈 리팩터링 요약

이 저장소는 Gradle 멀티모듈(4모듈) 구조로 리팩터링되었습니다.

## 모듈 구성

- `houme-api`
  - 목적: 웹/API 진입점(컨트롤러, 예외 핸들러, Web/Security/Swagger 설정, Spring Boot 메인, 리소스/템플릿)
  - 산출물: Spring Boot `bootJar` (Jib로 컨테이너 이미지 빌드 가능)
  - 의존: `houme-domain`, `houme-infrastructure`, `houme-common`

- `houme-domain`
  - 목적: 도메인 모델과 계약(엔티티, 리포지토리 인터페이스, 서비스/퍼사드 인터페이스, 밸리데이터, 도메인 DTO)
  - Querydsl APT 설정: `src/main/generated`에 Q 클래스 생성
  - 의존: `houme-common`

- `houme-infrastructure`
  - 목적: 구현 및 외부 연동(JPA 리포지토리/서비스/퍼사드 구현체, Feign/외부 클라이언트, Redis/S3/JWT/AWS, Resilience4j, Querydsl 런타임, 인프라 설정/AOP)
  - 의존: `houme-domain`, `houme-common`

- `houme-common`
  - 목적: 횡단 관심사(공용 BaseEntity, 공용 DTO, ErrorCode/GeneralException 및 도메인 예외, 공용 유틸/상수)

## Gradle 레이아웃

- `settings.gradle`: `include 'houme-api', 'houme-domain', 'houme-infrastructure', 'houme-common'`
- 루트 `build.gradle`: 집계 전용(저장소/Java toolchain 공통 설정)
- 모듈별 `build.gradle`:
  - `houme-api`: Spring Boot + Web/Security/Swagger/Actuator/Jib/Jacoco + OpenFeign
  - `houme-domain`: `java-library` + Querydsl APT(생성 소스 연결)
  - `houme-infrastructure`: `java-library` + JPA/Redis/Web/Security/Feign/JWT/AWS/Resilience4j/Querydsl 런타임
  - `houme-common`: `java-library` + jakarta validation/persistence + Lombok + Querydsl APT(QBaseEntity용)

## 주요 이동 내역

- 애플리케이션/리소스
  - `src/main/resources/**` → `houme-api/src/main/resources/**`
  - `HoumeApplication.java` → `houme-api/src/main/java/or/sopt/houme/HoumeApplication.java`

- Web/API 레이어 → `houme-api`
  - 컨트롤러: `domain/**/controller/**`
  - 글로벌 웹: `global/api/GlobalExceptionHandler.java`, `HealthCheckController.java`, `ApiResponse.java`
  - 웹 설정: `global/config/{SecurityConfig,SwaggerConfig,WhiteListConfig}.java`

- 도메인 레이어 → `houme-domain`
  - `domain/**`(엔티티, 리포지토리 인터페이스, 서비스/퍼사드 인터페이스, 컨트롤러에 종속되지 않는 DTO, 생일 밸리데이터)

- 인프라 레이어 → `houme-infrastructure`
  - 구현체: `*ServiceImpl.java`, `*RepositoryImpl.java`, `*FacadeImpl.java`
  - 외부 연동: `domain/**/client/**`
  - 인프라 설정: `global/config/{Async,Cache,Feign,JpaAuditing,JWT,KaKao,NaverProperties,OpenAiImage,Querydsl,Redis,S3,S3Presigned,Cookie}Config.java`
  - 인프라 패키지: `global/jwt/**`, `global/aop/**`
  - 유틸 구현체: `global/util/{BCryptImpl,S3UtilImpl,S3PresignedUtil}.java`

- 공용 레이어 → `houme-common`
  - 공용 베이스: `global/entity/BaseEntity.java`
  - 공용 DTO: `global/dto/**`
  - 예외: `global/api/{ErrorCode,GeneralException}.java`, `global/api/handler/*.java`
  - 공용 유틸/상수: `global/util/{BCrypt,S3Util,CookieUtil,HtmlTextCleaner}.java`, `global/util/constant/**`

## 의존성 방향

- `houme-api` → `houme-domain`, `houme-infrastructure`, `houme-common`
- `houme-infrastructure` → `houme-domain`, `houme-common`
- `houme-domain` → `houme-common`

위 방향으로 순환 의존을 방지합니다.

## 테스트 구조(정리됨)

- `houme-api/src/test/java`: 컨트롤러/웹 관련 테스트, HealthCheck, JWT/OAuth 컨트롤러 등
- `houme-infrastructure/src/test/java`: 서비스/리포지토리/퍼사드 구현체, 외부 연동 관련 테스트, 유틸 구현체(S3 등)
- `houme-domain/src/test/java`: 도메인 엔티티/DTO/밸리데이션 등 순수 도메인 테스트

기존 `src/test/java/**`는 해당 레이어로 이동되었습니다(컨트롤러 → api, 구현체/레포 → infra, DTO/엔티티 → domain 등).

## 빌드/실행

- 전체 빌드: `./gradlew build` (또는 `./gradlew :houme-api:build`)
- 로컬 실행: `./gradlew :houme-api:bootRun`
- 컨테이너 이미지: `./gradlew :houme-api:jib -PjibToImage=레지스트리/리포 -PjibTag=태그`

참고: Querydsl Q 클래스는 `houme-domain/src/main/generated`와 `houme-common/src/main/generated`에 생성됩니다.

## 추가 작업

- 테스트 모듈화 및 의존성 정리 완료(모듈별 testImplementation 정리)
- CI 템플릿 추가: `.github/workflows/gradle.yml`에서 JDK 17 + Gradle 캐시 + `./gradlew build`
- 모듈 간 의존 범위 최소화 및 compileOnly 활용으로 경계 강화

