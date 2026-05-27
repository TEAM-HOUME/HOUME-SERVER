# HOUME Monitoring

This directory keeps the monitoring stack as code.

## Layout

- `metric/`: Prometheus and Grafana metric dashboards.
- `logging/`: Loki, Alloy, and Grafana logging dashboards.
- `docker-compose.monitoring.yml`: shared monitoring stack for dev/prod.

## Server Differences

- Dev server `3.38.226.162`
  - App container: `houme`
  - App port: `8080`
  - Existing compose path: `/home/ubuntu/infra/docker-compose.yml`
- Prod server `3.39.135.165`
  - App containers: `blue`, `green`
  - App ports: `8080`, `8081`
  - Existing compose path: `/home/ubuntu/deploy/docker-compose-{blue,green,monitoring}.yml`

Prometheus uses environment-specific configs:

- `metric/prometheus/prometheus.dev.yml`
- `metric/prometheus/prometheus.prod.yml`

Alloy uses one shared config and labels logs with:

- `env`: `dev` or `prod`
- `host`: `houme-dev` or `houme-prod`
- `service`: `spring`, `nginx`, `grafana`, `prometheus`, `loki`, `alloy`, etc.

## What Gets Collected

Alloy sends these logs to Loki:

- Docker container logs
- `/var/log/nginx/access.log`
- `/var/log/nginx/error.log`

Spring Boot logs are collected from Docker stdout. Application logs use
`event=...` fields so Loki can query logs before JSON logging is added.

## Example Commands

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

Keep Grafana, Prometheus, and Loki bound to `127.0.0.1` unless nginx or SSH
tunneling is configured intentionally.

## Useful LogQL

```logql
{service="spring"} |= "event=exception"
{service="spring"} |= "event=auth.failed"
{service="spring"} |= "event=image.generation.fallback"
{service="spring"} |= "event=image.ai.request.failed"
{service="nginx", log_type="error"}
```
