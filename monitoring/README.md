# HOUME 모니터링 (Grafana + Loki + Prometheus + Promtail)

로그(traceId 검색)와 메트릭(JVM)을 한 곳에서 보는 모니터링 축. **모든 설정이 코드로 관리**되며
Grafana 는 프로비저닝으로 자동 구성된다 (UI 수동 설정 금지 — 대시보드 수정 시 JSON 갱신).

```
[dev EC2]  houme ── promtail(dev/) ───┐        ┌── Grafana(:3000) ─ 대시보드/검색
                                      ├─> stack/ ├── Loki(:3100) ─── 로그 3일 보관
[prod EC2] blue/green ─ promtail(prod/)┘        └── Prometheus(:9090) ─ 메트릭 3일
[내 맥]    bootRun ── promtail(local/) ┘   (stack 은 prod EC2 에서 실행, 로컬 검증도 동일 파일)
```

## 디렉토리 구조
```
monitoring/
├── stack/     🏢 중앙 스택 (Grafana+Loki+Prometheus, 프로비저닝 포함) — prod EC2 / 로컬 공용
├── local/     💻 promtail — bootRun 로그 파일 수집
├── dev/       ☁️ promtail — houme 컨테이너 수집 → 10.0.1.11:3100
└── prod/      ☁️ promtail — blue/green 컨테이너 수집 → 같은 호스트 Loki
```
**local/dev/prod 는 구조가 완전히 동일**하다 (docker-compose.yml + promtail-config.yml).
차이는 config 내용(수집 대상·Loki 주소)뿐. 수집 정책 수정 시 세 곳 모두 반영할 것.

## 서버 적용 절차 (최초 1회)

### 1. prod EC2 — 기존 grafana 정리 + 중앙 스택 기동
```bash
docker rm -f grafana        # 기존 UI 설정은 폐기 (코드 관리로 전환, 승인됨)
cd monitoring/stack
GRAFANA_ADMIN_PASSWORD=<팀비번> docker compose up -d   # 비번 env 주입 필수 (미지정 시 admin — 로컬 전용)
curl -fsS --retry 5 --retry-delay 2 --max-time 5 localhost:3100/ready
curl -fsS --retry 5 --retry-delay 2 --max-time 5 localhost:3000/api/health
```

### 2. 각 앱 서버 — promtail
```bash
# dev EC2:  cd monitoring/dev  && docker compose up -d
# prod EC2: cd monitoring/prod && docker compose up -d
```

### 3. 보안그룹
- Loki 3100: **dev EC2 보안그룹에서만** 인바운드 허용
- Grafana 3000: 팀 접근 범위만 (기존 정책 유지)

## 사용법
- **traceId 추적**: 디스코드 알림/에러 응답의 traceId → "HOUME 로그 (Loki)" 대시보드의 `traceId 검색` 변수에 입력
- Explore 직접 질의: `{app="houme", env="dev"} | json | traceId="abc12345"`
- 에러만: `{app="houme", env="dev", level="ERROR"}`
- JVM 메트릭: "JVM (Micrometer)" 대시보드 (Prometheus)

## 로컬에서 전체 검증
```bash
cd monitoring/stack && docker compose up -d     # 중앙 스택 (서버와 동일 파일)
cd ../local && docker compose up -d             # 수집기
mkdir -p logs && SPRING_PROFILES_ACTIVE=local,dev ./gradlew bootRun > logs/houme-local.log 2>&1
# → http://localhost:3000 (admin/admin), env=local
```
⚠️ macOS 한정: 바인드마운트 fsnotify 미전달로 실시간 tail 이 멈출 수 있음 —
`docker restart promtail` 로 캐치업. 리눅스 서버에서는 발생하지 않음.

## 운영 메모
- 보존: 로그(Loki)·메트릭(Prometheus) 모두 **3일** — 디스크 여유 부족으로 짧게 유지
- traceId 는 라벨이 아니라 JSON 필드 (라벨화 시 카디널리티 폭발)
- 메모리 제한: loki 256m / prometheus 192m / grafana 192m / promtail 64m
- **대시보드 수정 = stack/grafana/dashboards/*.json 수정** (UI 에서 고쳤으면 Export 해서 반영)

## 서버 적용 시 함께 할 것 (디스크 관리)
1. **도커 로그 로테이션** (현재 무제한 — 필수): 각 앱 서버 `/etc/docker/daemon.json`
   ```json
   { "log-driver": "json-file", "log-opts": { "max-size": "20m", "max-file": "2" } }
   ```
   `sudo systemctl restart docker` 후 **기존 컨테이너는 재생성해야 적용됨** — `docker compose up -d --force-recreate` (log-opts 는 컨테이너 생성 시점에 고정되므로 재시작만으로는 무제한 로그가 계속 쌓임)
2. **디스크 점검**: `docker system df` → `docker image prune -a --filter "until=168h"`
