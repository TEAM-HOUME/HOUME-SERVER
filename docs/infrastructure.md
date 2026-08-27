# HOUME-SERVER 인프라 구조

> PR #584(Testcontainers), #585(traceId 로깅) 머지 이후 기준으로 작성

---

## 전체 구성도

```
                   ┌──────────────────────────────────────┐
                   │           GitHub Actions              │
                   │  develop push → CI → CD(dev 배포)    │
                   │  prod PR merge → Blue/Green 배포      │
                   └──────────────┬───────────────────────┘
                                  │ Jib (Docker 이미지)
                        ┌─────────▼─────────┐
                        │      AWS ECR       │
                        │  (이미지 레지스트리) │
                        └──────┬──────┬──────┘
                               │      │
              ┌────────────────▼──┐  ┌▼───────────────────────────┐
              │     dev EC2       │  │         prod EC2            │
              │  ┌─────────────┐  │  │  ┌────────┐  ┌──────────┐  │
              │  │houme:8080   │  │  │  │ blue   │  │  green   │  │
              │  │(Docker)     │  │  │  │ :8080  │  │  :8081   │  │
              │  └──────┬──────┘  │  │  └───┬────┘  └────┬─────┘  │
              │         │ 로그    │  │      └──────┬──────┘        │
              │  ┌──────▼──────┐  │  │            │               │
              │  │  Promtail   │──┼──┤  ┌─────────▼─────────┐    │
              │  │ (dev, 64m)  │  │  │  │      Nginx         │    │
              │  └─────────────┘  │  │  │  (upstream 전환)   │    │
              │                   │  │  └───────────────────┘    │
              │  ┌─────────────┐  │  │                            │
              │  │ PostgreSQL  │  │  │  ┌─────────────────────┐  │
              │  │ (VPC 내부)  │  │  │  │    모니터링 스택      │  │
              │  └─────────────┘  │  │  │  Loki    :3100      │  │
              │  ┌─────────────┐  │  │  │  Prometheus :9090   │  │
              │  │   Redis     │  │  │  │  Grafana  :3000     │  │
              │  └─────────────┘  │  │  │  Promtail (prod,64m)│  │
              └───────────────────┘  │  └─────────────────────┘  │
                                     └────────────────────────────┘
```

---

## 환경별 구성

| 환경 | 서버 | Spring 프로파일 | DB | 포트 |
|---|---|---|---|---|
| **local** | 개발자 맥 | `local` | 로컬 PostgreSQL | 8080 |
| **dev** | dev EC2 (t2.micro) | `dev` | PostgreSQL (VPC 내부 172.18.0.1:5432) | 8080 |
| **prod-blue** | prod EC2 | `prod` + `blue` | PostgreSQL | 8080 |
| **prod-green** | prod EC2 | `prod` + `green` | PostgreSQL | 8081 |

prod 프로파일 그룹 (`application.yml`):
```yaml
profiles:
  group:
    blue: [prod, blue]
    green: [prod, green]
```

---

## CI/CD 파이프라인

### develop 브랜치 (dev 배포)

```
develop push
  → [CI] Testcontainers(PostgreSQL+Redis) 위에서 전체 테스트
  → [CD] Gradle 빌드 (테스트 스킵)
      → Jib로 ECR push (태그: runid-{run_id})
      → dev EC2 SSH
          → ECR pull
          → docker compose up
          → /actuator/health 헬스체크 (20회 × 3초)
```

> CI는 PR #584 머지 이후 GitHub Actions의 Redis 서비스 컨테이너와 APPLICATION_TEST 시크릿 주입이 제거됨. Testcontainers가 PostgreSQL 16 + Redis 7을 자동 기동.

### prod 브랜치 (Blue/Green 배포)

```
prod PR merge
  → 현재 active 환경(blue/green) 감지
      ↳ /api/v1/env 응답으로 판단
  → ECR 이미지 빌드 & push
  → inactive 쪽 컨테이너 기동
  → 헬스체크 통과 (15회 × 5초)
  → Nginx upstream 전환 (/etc/nginx/sites-enabled/service-env.inc)
  → 기존 컨테이너 중지 & 제거
  → Discord 배포 결과 알림
```

이미지 빌드 도구: **Jib** (Dockerfile 없이 Gradle 플러그인으로 ECR 직접 push)

---

## 외부 연동 서비스

| 서비스 | 용도 | 설정 위치 |
|---|---|---|
| **AWS S3** | 이미지 업로드/다운로드 | `cloud.aws.s3` |
| **AWS ECR** | Docker 이미지 레지스트리 | GitHub Secrets |
| **Kakao OAuth** | 소셜 로그인 | `spring.security.oauth2` |
| **OpenAI API** | 이미지 생성 (gpt-image-1) | `openai.api-key` |
| **Gemini API** | 이미지 생성 (gemini-3-pro-image-preview) | `gemini.api-key` |
| **Naver API** | 큐레이션 상품 검색 (롯데ON 등) | `naver.client-id` |
| **FastAPI 서버** | 이미지 분석/CLIPScore (3.38.226.162:80) | `external.image-api.base-url` |
| **Sentry** | 에러 트래킹 | `sentry.dsn` |
| **Discord Webhook** | 5xx 에러 즉시 알림 (5분 쿨다운) | `discord.webhook-url` |

---

## 모니터링 스택 (PR #585 이후)

prod EC2 한 곳에서 중앙 스택 운영. dev/prod 로그를 모두 수집.

### 컴포넌트

| 컴포넌트 | 역할 | 포트 | 메모리 제한 |
|---|---|---|---|
| **Loki** | 로그 저장 (3일 보관) | 3100 | 256m |
| **Prometheus** | 메트릭 수집 (3일 보관) | 9090 (localhost only) | 192m |
| **Grafana** | 대시보드/검색 UI | 3000 | 192m |
| **Promtail** (dev EC2) | 로그 수집 → Loki 전송 | - | 64m |
| **Promtail** (prod EC2) | 로그 수집 → Loki 전송 | - | 64m |

### 로그 흐름 (traceId 추적)

```
HTTP 요청 진입
  → TraceIdFilter: traceId 생성 (UUID 앞 8자리) → MDC 주입
  → JWTFilter: 인증 성공 시 userId → MDC 추가 주입
  → 비동기 스레드 전환: MdcTaskDecorator / 가상 스레드 직접 복사
  → logback-spring.xml: dev/prod = JSON 포맷 (Loki 수집용)
  → Promtail: 컨테이너 로그 수집 → Loki push
  → Grafana "HOUME 로그" 대시보드: traceId 검색

에러(500) 발생 시:
  → GlobalExceptionHandler: ErrorAlertNotifier.notifyServerError()
  → Discord Webhook (traceId + API + userId + 에러 메시지)
  → 에러 응답 body에도 traceId 포함 (프론트 → 사용자에게 번호 표시 가능)
```

### Promtail → Loki 전송 경로

| 환경 | Promtail 위치 | Loki 주소 |
|---|---|---|
| local | `monitoring/local/` | `localhost:3100` |
| dev | `monitoring/dev/` | `10.0.1.11:3100` (prod EC2 VPC 내부 IP) |
| prod | `monitoring/prod/` | `172.17.0.1:3100` (같은 호스트 docker0) |

### Grafana 대시보드

- **HOUME 로그 (Loki)**: 레벨별 로그 볼륨, 에러 추이, traceId 검색, 최근 에러 로그
- **JVM (Micrometer)**: Heap/Non-Heap, GC, 스레드, CPU, HTTP rate/error/latency

---

## 비동기 실행 구성

이미지 생성처럼 오래 걸리는 작업은 별도 실행기로 처리.

| 실행기 | 설정 키 | 스레드 유형 | 용도 |
|---|---|---|---|
| `ImageGenerator-*` | `houme.async.image-generation` | 플랫폼 or 가상 스레드 | AI 이미지 생성 |
| `TokenRefresh-*` | (AsyncConfig 내 고정) | 플랫폼 스레드 | 토큰 갱신 |
| `discord-error-alert` | (ErrorAlertNotifier 내 고정) | 데몬 스레드 | Discord 알림 발송 |

이미지 생성 실행기 설정 (dev 기준):
```yaml
houme:
  async:
    image-generation:
      mode: virtual          # virtual / platform 선택 가능
      platform:
        core-size: 8
        max-size: 16
        queue-capacity: 8
      virtual:
        concurrency-limit: 32
```

이미지 생성 API에는 **Circuit Breaker** (Resilience4j) 적용:
- 슬라이딩 윈도우 10회, 실패율 50% 초과 시 OPEN
- OPEN 유지 10초 후 HALF_OPEN(3회 허용) → 회복 시 CLOSED
- 타임아웃: 120초

---

## 테스트 인프라 (PR #584 이후)

로컬과 CI 모두 동일한 방식으로 테스트 실행.

```
./gradlew clean build -Dspring.profiles.active=test
  → TestContainersInitializer 자동 실행
      → PostgreSQL 16 컨테이너 기동
      → Redis 7-alpine 컨테이너 기동
  → 전체 스프링 컨텍스트 기동 (실제 DB 사용)
  → 외부 API(OpenAI, Gemini, Kakao, S3 등)는 MockBean 대체
  → 테스트 완료 후 Ryuk이 컨테이너 자동 정리
```

GitHub Actions CI에서도 동일한 명령어로 돌아가며, APPLICATION_TEST 시크릿 주입이 더 이상 필요 없음.

---

## 관련 설정 파일 위치

| 파일 | 용도 |
|---|---|
| `src/main/resources/application.yml` | 프로파일 그룹 정의 |
| `src/main/resources/application-dev.yml` | dev 환경 설정 (DB, Redis, API 키 등) |
| `src/main/resources/application-local.yml` | 로컬 환경 설정 (환경 변수 참조) |
| `src/main/resources/application-blue.yml` | prod blue 설정 |
| `src/main/resources/application-green.yml` | prod green 설정 |
| `src/test/resources/application-test.yml` | 테스트 환경 설정 (더미값, Testcontainers 등록) |
| `src/main/resources/logback-spring.xml` | 로그 포맷 (local=plain, dev/prod=JSON) |
| `monitoring/stack/docker-compose.yml` | Grafana + Loki + Prometheus (prod EC2 실행) |
| `monitoring/dev/docker-compose.yml` | Promtail (dev EC2 실행) |
| `monitoring/prod/docker-compose.yml` | Promtail (prod EC2 실행) |
| `monitoring/local/docker-compose.yml` | Promtail (로컬 검증용) |
| `.github/workflows/develop-ci.yml` | develop 브랜치 CI |
| `.github/workflows/develop-cd.yml` | develop 브랜치 CD (dev 배포) |
| `.github/workflows/prod-cd.yml` | prod 브랜치 CD (Blue/Green 배포) |
