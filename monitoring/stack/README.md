# 모니터링 스택 (Grafana + Loki + Prometheus + Alertmanager)

prod EC2 에서 실행하는 중앙 모니터링 스택입니다.

```bash
cd monitoring/stack
GRAFANA_ADMIN_PASSWORD=<팀비번> docker compose up -d
```

## 구성

- **Prometheus** (`prometheus.yml`) — prod/dev 앱 `/actuator/prometheus` + node-exporter 스크랩, `rules/` 알림 룰 평가
- **Alertmanager** (`alertmanager/`) — 룰 발화 시 Discord 로 알림 (native `discord_configs`)
- **node-exporter** — 호스트(prod EC2) CPU/디스크/메모리 지표
- **Grafana / Loki** — 대시보드 및 로그

## 알림 룰 (`rules/houme-alerts.yml`)

| alert | 조건 | 심각도 |
|---|---|---|
| HoumeAppDown | job 전체 인스턴스 다운 2분 | critical |
| HoumeHighJvmHeap | JVM Heap > 80% 3분 | warning |
| HighHostCpuUsage | 호스트 CPU > 80% 5분 | warning |
| HighHostDiskUsage | 루트 디스크 > 85% 5분 | warning |

## Discord 웹훅 시크릿 (⚠️ git 에 커밋 금지)

Alertmanager 는 설정 파일에서 환경변수를 읽지 못하므로, 웹훅 URL 은
`alertmanager/secrets/discord_webhook_url` **파일**로 읽습니다.
이 파일은 `.gitignore` 로 제외되며 **서버에만** 배치합니다.

```bash
# 서버(스택 배포 경로)에서 1회 생성
printf '%s' 'https://discord.com/api/webhooks/<CHANNEL_ID>/<TOKEN>' \
  > alertmanager/secrets/discord_webhook_url
chmod 600 alertmanager/secrets/discord_webhook_url

# alertmanager 재시작(파일 반영)
docker compose up -d alertmanager
```

파일이 없으면 alertmanager 가 기동 실패하므로, 스택 기동 전에 배치해야 합니다.

## 발화 테스트

```bash
# amtool 없이 Alertmanager API 로 테스트 알림 전송
curl -XPOST http://127.0.0.1:9093/api/v2/alerts -H 'Content-Type: application/json' -d '[{
  "labels": {"alertname":"TestAlert","service":"houme-api","severity":"warning","job":"houme-dev"},
  "annotations": {"summary":"테스트","description":"디스코드 연동 확인용"}
}]'
```

> dev 호스트 CPU/디스크 지표가 필요하면 dev EC2 에도 node-exporter 를 띄우고
> `prometheus.yml` 에 스크랩 타깃을 추가하면 됩니다 (본 스택은 prod 호스트 기준).
