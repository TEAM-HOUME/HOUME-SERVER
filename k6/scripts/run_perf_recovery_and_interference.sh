#!/usr/bin/env bash

set -euo pipefail

if [[ "${CONFIRM_PERF:-}" != "true" ]]; then
  echo "CONFIRM_PERF=true가 필요합니다." >&2
  exit 1
fi

: "${PERF_SSH_KEY:?PERF_SSH_KEY가 필요합니다.}"

PERF_HOST="${PERF_HOST:-13.209.178.230}"
PERF_USER="${PERF_USER:-ubuntu}"
BASE_URL="${BASE_URL:-http://${PERF_HOST}:8080}"
IDLE_MINUTES="${IDLE_MINUTES:-10}"
IMAGE_VUS="${IMAGE_VUS:-5}"
PROBE_RATE="${PROBE_RATE:-4}"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
RESULT_DIR="${RESULT_DIR:-/private/tmp/houme-perf/recovery-${RUN_ID}}"
SSH_ARGS=(-i "${PERF_SSH_KEY}" -o BatchMode=yes -o StrictHostKeyChecking=no)

mkdir -p "${RESULT_DIR}"

snapshot() {
  local name="$1"
  echo "[snapshot] ${name}"
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
    curl -fsS http://localhost:8080/actuator/prometheus |
      awk "/^jvm_memory_used_bytes.*G1 (Old Gen|Eden Space|Survivor Space)|^jvm_gc_live_data_size_bytes|^jvm_gc_max_data_size_bytes|^jvm_gc_pause_seconds_(count|sum)|^jvm_gc_memory_allocated_bytes_total|^hikaricp_connections_(active|pending|timeout)|^process_cpu_usage|^system_cpu_usage/ {print}"
    free -b
    docker stats --no-stream --format "{{.Name}} {{.MemUsage}} {{.MemPerc}} {{.CPUPerc}}"
  ' > "${RESULT_DIR}/${name}.txt"
}

heap_guard_status() {
  local metrics
  local live_data
  local max_data
  local current_heap
  metrics="$(ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" \
    'curl -fsS http://localhost:8080/actuator/prometheus')"
  live_data="$(printf '%s\n' "${metrics}" | awk '/^jvm_gc_live_data_size_bytes/ { print $2; exit }')"
  max_data="$(printf '%s\n' "${metrics}" | awk '/^jvm_gc_max_data_size_bytes/ { print $2; exit }')"
  current_heap="$(printf '%s\n' "${metrics}" |
    awk '/^jvm_memory_used_bytes/ && /area="heap"/ { sum += $NF } END { print sum + 0 }')"
  awk -v live="${live_data:-0}" -v used="${current_heap:-0}" -v max="${max_data:-0}" '
    BEGIN {
      if (live > 0 && max > 0) printf "after_gc %.2f", live / max * 100
      else if (max > 0) printf "current_heap %.2f", used / max * 100
      else print "unavailable 0"
    }'
}

wait_idle() {
  local label="$1"
  local minute
  for ((minute = 1; minute <= IDLE_MINUTES; minute += 1)); do
    sleep 60
    echo "[idle] ${label} ${minute}/${IDLE_MINUTES}분"
  done
}

check_stop_condition() {
  local phase="$1"
  local metric_kind
  local metric_value
  read -r metric_kind metric_value <<< "$(heap_guard_status)"
  echo "[check] ${phase} ${metric_kind}=${metric_value}%"
  if [[ "${metric_kind}" == "after_gc" ]] &&
      awk -v value="${metric_value}" 'BEGIN { exit !(value >= 80) }'; then
    echo "[stop] heap-after-gc가 idle 후에도 80% 이상입니다." >&2
    exit 2
  fi
  if [[ "${metric_kind}" == "current_heap" ]] &&
      awk -v value="${metric_value}" 'BEGIN { exit !(value >= 90) }'; then
    echo "[stop] live-data가 아직 없고 현재 힙 사용률이 90% 이상입니다." >&2
    exit 2
  fi
}

echo "[start] result-dir=${RESULT_DIR}"
ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
  cd /opt/houme-perf
  docker compose -f docker-compose.app.yml -f docker-compose.real-gemini.yml up -d --force-recreate app
'

for attempt in {1..60}; do
  if /usr/bin/curl -fsS "${BASE_URL}/actuator/health" >/dev/null; then
    echo "[ready] perf app"
    break
  fi
  if [[ "${attempt}" == "60" ]]; then
    echo "perf 앱이 기동하지 않았습니다." >&2
    exit 1
  fi
  sleep 5
done

snapshot "00-after-restart"
wait_idle "baseline"
snapshot "01-baseline-after-idle"
check_stop_condition "baseline"

for round in 1 2 3; do
  echo "[load] recovery round ${round}/3"
  CONFIRM_PERF=true \
    BASE_URL="${BASE_URL}" \
    IMAGE_API=v4 \
    TEST_MODE=burst \
    BURST_VUS="${IMAGE_VUS}" \
    REQUEST_TIMEOUT=240s \
    k6 run --quiet \
      --summary-export "${RESULT_DIR}/round-${round}.json" \
      k6/scenarios/perf_image_generation_step.js
  snapshot "round-${round}-immediate"
  wait_idle "round-${round}"
  snapshot "round-${round}-after-idle"
  check_stop_condition "round-${round}"
done

echo "[probe] fresh-JVM 누적 상태의 baseline"
CONFIRM_PERF=true \
  BASE_URL="${BASE_URL}" \
  TEST_MODE=baseline \
  PROBE_RATE="${PROBE_RATE}" \
  PROBE_DURATION=75s \
  k6 run --quiet \
    --summary-export "${RESULT_DIR}/probe-baseline.json" \
    k6/scenarios/perf_image_generation_interference.js

echo "[probe] V4 동시 ${IMAGE_VUS}건 간섭"
CONFIRM_PERF=true \
  BASE_URL="${BASE_URL}" \
  TEST_MODE=interference \
  IMAGE_VUS="${IMAGE_VUS}" \
  PROBE_RATE="${PROBE_RATE}" \
  PROBE_DURATION=75s \
  IMAGE_START_DELAY=15s \
  REQUEST_TIMEOUT=240s \
  k6 run --quiet \
    --summary-export "${RESULT_DIR}/probe-interference.json" \
    k6/scenarios/perf_image_generation_interference.js

snapshot "99-final"
echo "[complete] ${RESULT_DIR}"
