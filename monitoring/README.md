# HOUME 모니터링

이 디렉터리는 하우미 모니터링 설정을 코드로 관리하기 위한 공간입니다.

## 디렉터리 구조

- `metric/`: Prometheus 설정과 Grafana 메트릭 대시보드
- `logging/`: Loki, Alloy 설정과 Grafana 로그 대시보드
- `docker-compose.monitoring.yml`: dev/prod 공통 모니터링 스택

## 서버별 차이

- Dev 서버 `3.38.226.162`
  - 애플리케이션 컨테이너: `houme`
  - 애플리케이션 포트: `8080`
  - 기존 compose 경로: `/home/ubuntu/infra/docker-compose.yml`
- Prod 서버 `3.39.135.165`
  - 애플리케이션 컨테이너: `blue`, `green`
  - 애플리케이션 포트: `8080`, `8081`
  - 기존 compose 경로: `/home/ubuntu/deploy/docker-compose-{blue,green,monitoring}.yml`

Prometheus는 환경별 설정 파일을 사용합니다.

- `metric/prometheus/prometheus.dev.yml`
- `metric/prometheus/prometheus.prod.yml`

Alloy는 공통 설정 파일 하나를 사용하고, 로그에 아래 label을 붙입니다.

- `env`: `dev` 또는 `prod`
- `host`: `houme-dev` 또는 `houme-prod`
- `service`: `spring`, `nginx`, `grafana`, `prometheus`, `loki`, `alloy` 등

## 로그 수집 대상

Alloy는 아래 로그를 Loki로 전송합니다.

- Docker 컨테이너 로그
- `/var/log/nginx/access.log`
- `/var/log/nginx/error.log`

Spring Boot 로그는 Docker stdout을 통해 수집합니다. 애플리케이션 로그는 아직 JSON 로그가 아니므로,
`event=...` 필드를 기준으로 Loki에서 검색할 수 있게 구성합니다.

## 실행 예시

Dev:

```bash
cd /home/ubuntu/infra/monitoring
cp .env.dev.example .env
docker compose --env-file .env -f docker-compose.monitoring.yml up -d
```

Prod:

```bash
cd /home/ubuntu/deploy/monitoring
cp .env.prod.example .env
docker compose --env-file .env -f docker-compose.monitoring.yml up -d
```

Grafana, Prometheus, Loki는 기본적으로 `127.0.0.1`에만 바인딩합니다.
외부 공개가 필요하면 nginx 프록시 또는 SSH 터널링을 명시적으로 구성한 뒤 변경합니다.

## 자주 쓰는 LogQL

```logql
{service="spring"} |= "event=exception"
{service="spring"} |= "event=auth.failed"
{service="spring"} |= "event=image.generation.fallback"
{service="spring"} |= "event=image.ai.request.failed"
{service="nginx", log_type="error"}
```
