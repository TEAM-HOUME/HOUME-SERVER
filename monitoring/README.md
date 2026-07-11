# HOUME 로그 수집 (Loki + Promtail + Grafana)

traceId 기반 로그 검색 파이프라인. 앱은 dev/prod 프로파일에서 JSON 로그(LogstashEncoder)를 stdout 으로 출력하고(#583),
Promtail 이 도커 로그를 수집해 Loki 로 보내며, 기존 Grafana 에서 검색/대시보드로 확인한다.

```
[dev EC2]  houme 컨테이너 ── promtail(dev/) ──┐
                                             ├──> Loki(:3100, prod EC2) <── Grafana(기존)
[prod EC2] blue/green ── promtail(prod/) ────┘
```

## 디렉토리 구조 — "실행하는 곳" 기준
```
monitoring/
├── dashboards/   📊 Grafana 대시보드 JSON (공용 자산 — 모든 환경에서 import)
├── local/        💻 내 맥 — 파이프라인 전체 검증 스택 (Loki+Promtail+Grafana 원터치)
├── dev/          ☁️ dev EC2 — promtail (env=dev, Loki 주소 하드코딩)
└── prod/         ☁️ prod EC2(=모니터링 서버) — loki/ + promtail/ + grafana/(datasource)
```
각 환경 디렉토리는 자기완결적이다: 그 서버에 가서 `cd 해당폴더 && docker compose up -d` 하면 끝.
(promtail 설정이 dev/prod 에 중복되는 건 의도 — 값 하드코딩으로 명확성을 택함. 수정 시 양쪽 다 반영할 것)

## 적용 절차

### 1. prod EC2 — Loki
```bash
cd monitoring/prod/loki && docker compose up -d
curl -s localhost:3100/ready
```

### 2. 각 앱 서버 — Promtail
```bash
# dev EC2:
cd monitoring/dev && docker compose up -d
# prod EC2:
cd monitoring/prod/promtail && docker compose up -d
```

### 3. 보안그룹
- Loki 3100 은 **dev EC2 보안그룹에서만** 인바운드 허용 (외부/0.0.0.0 금지)

### 4. Grafana (기존 인스턴스)
- 데이터소스: `prod/grafana/loki-datasource.yml` 을 provisioning 디렉토리에 넣거나, UI 에서 Loki 수동 등록
- 대시보드: `dashboards/houme-logs.json` Import (Dashboards > Import)

## 사용법
- **traceId 추적**: 디스코드 알림/에러 응답의 traceId 를 대시보드 상단 `traceId 검색` 변수에 입력
  → 해당 요청의 전체 로그(액세스/서비스 흐름/에러)가 시간순으로 나온다
- Explore 직접 질의: `{app="houme", env="dev"} | json | traceId="abc12345"`
- 에러만: `{app="houme", env="dev", level="ERROR"}`

## 로컬에서 전체 파이프라인 검증
```bash
# 1) 스택 기동 (Loki + Promtail + Grafana(:3002, 익명 Admin, 대시보드 자동 등록))
cd monitoring/local && docker compose up -d
# 2) 앱을 JSON 로그로 실행 (프로젝트 루트에서 — dev 프로파일은 logback JSON 선택용)
mkdir -p logs && SPRING_PROFILES_ACTIVE=local,dev ./gradlew bootRun > logs/houme-local.log 2>&1
# 3) http://localhost:3002 → "HOUME 로그 (Loki)" 대시보드, env=local
```
⚠️ macOS 한정: Docker 바인드마운트의 fsnotify 미전달로 **실시간 tail 이 멈출 수 있음** —
`docker restart houme-local-promtail` 하면 그 시점까지 캐치업된다. 리눅스 서버에서는 발생하지 않는 문제.
(local+dev 동시 프로파일이라 콘솔에 플레인/JSON 이중 출력되는 것도 로컬 전용 현상)

## 대시보드 코드 관리 (dashboard-as-code)
- `dashboards/jvm-micrometer.json` — 라이브 Grafana 에서 추출한 JVM 메트릭 대시보드 (2026-07-11)
- `dashboards/houme-logs.json` — 로그 검색 대시보드 (#583 신규)
- k6 부하테스트 대시보드는 `k6/monitoring/grafana/dashboards/` 에 기존부터 관리 중
- **Grafana UI 에서 대시보드를 수정하면 여기 JSON 도 함께 갱신할 것** (Share > Export > Save to file)

## 운영 메모
- 보존기간 3일(`prod/loki/loki-config.yml` retention_period 72h) — 디스크 여유가 적어 짧게 유지, 상황 보고 조정
- traceId 는 라벨이 아니라 JSON 필드로 검색한다 (라벨로 만들면 카디널리티 폭발)
- promtail 메모리 64m 제한 — 두 EC2 모두 RAM 여유가 적음

## 서버 적용 시 함께 할 것 (디스크 관리)
1. **도커 로그 로테이션** (현재 무제한 — 필수): 각 앱 서버 `/etc/docker/daemon.json`
   ```json
   { "log-driver": "json-file", "log-opts": { "max-size": "20m", "max-file": "2" } }
   ```
   적용: `sudo systemctl restart docker` 후 컨테이너 재기동 (daemon.json 은 신규 컨테이너부터 적용됨)
   → 원본은 컨테이너당 최대 40MB, 과거 로그 검색은 Loki(3일)가 담당
2. **디스크 점검**: `docker system df` 로 옛 이미지 누적 확인 → `docker image prune -a --filter "until=168h"`
   (dev EC2 85% 사용의 주범은 로그보다 배포 이미지 누적일 가능성이 큼)
