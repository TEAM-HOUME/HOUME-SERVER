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
ROUNDS="${ROUNDS:-3}"
IMAGE_VUS="${IMAGE_VUS:-5}"
IHOP_PERCENT="${IHOP_PERCENT:-30}"
SKIP_DEFAULT="${SKIP_DEFAULT:-false}"
BASELINE_IDLE_SECONDS="${BASELINE_IDLE_SECONDS:-60}"
RECOVERY_IDLE_SECONDS="${RECOVERY_IDLE_SECONDS:-60}"
SAMPLE_INTERVAL_SECONDS="${SAMPLE_INTERVAL_SECONDS:-2}"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
RESULT_DIR="${RESULT_DIR:-/private/tmp/houme-perf/g1-ihop-ab-${RUN_ID}}"
SSH_ARGS=(-i "${PERF_SSH_KEY}" -o BatchMode=yes -o StrictHostKeyChecking=no)
REMOTE_OVERRIDE="/opt/houme-perf/docker-compose.g1-ihop.override.yml"
SAMPLER_PID=""

mkdir -p "${RESULT_DIR}"

stop_sampler() {
  if [[ -n "${SAMPLER_PID}" ]]; then
    kill "${SAMPLER_PID}" 2>/dev/null || true
    wait "${SAMPLER_PID}" 2>/dev/null || true
    SAMPLER_PID=""
  fi
}

restore_default_mode() {
  stop_sampler
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
    cd /opt/houme-perf
    JAVA_TOOL_OPTIONS="" docker compose \
      -f docker-compose.app.yml \
      -f docker-compose.real-gemini.yml \
      -f docker-compose.g1-ihop.override.yml \
      up -d --force-recreate app
  ' >/dev/null 2>&1 || true
}
trap restore_default_mode EXIT

wait_until_ready() {
  local attempt
  for attempt in {1..60}; do
    if /usr/bin/curl -fsS "${BASE_URL}/actuator/health" >/dev/null; then
      return 0
    fi
    sleep 5
  done
  echo "perf 앱이 기동하지 않았습니다." >&2
  return 1
}

wait_seconds() {
  local label="$1"
  local seconds="$2"
  local elapsed=0
  echo "[idle] ${label} ${seconds}초"
  while (( elapsed < seconds )); do
    local remaining=$((seconds - elapsed))
    local step=10
    if (( remaining < step )); then step="${remaining}"; fi
    sleep "${step}"
    elapsed=$((elapsed + step))
    echo "[idle] ${label} ${elapsed}/${seconds}초"
  done
}

deploy_override() {
  scp "${SSH_ARGS[@]}" \
    "k6/config/docker-compose.g1-ihop.override.yml" \
    "${PERF_USER}@${PERF_HOST}:${REMOTE_OVERRIDE}"
}

restart_mode() {
  local mode="$1"
  local options="$2"
  echo "[restart] ${mode}: ${options:-default adaptive IHOP}"
  local compose_command="
    cd /opt/houme-perf
    JAVA_TOOL_OPTIONS='${options}' docker compose \\
      -f docker-compose.app.yml \\
      -f docker-compose.real-gemini.yml \\
      -f docker-compose.g1-ihop.override.yml \\
      up -d --force-recreate app
  "
  if ! ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" "${compose_command}"; then
    # Docker가 직전 recreate의 컨테이너 ID를 정리하는 짧은 구간에는 재시도한다.
    sleep 5
    ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" "${compose_command}"
  fi
  wait_until_ready
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" \
    "docker exec houme-perf-app jcmd 1 VM.flags | tr ' ' '\\n' | grep -E 'G1UseAdaptiveIHOP|InitiatingHeapOccupancyPercent|UseG1GC|MaxHeapSize' || true" \
    > "${RESULT_DIR}/${mode}-vm-flags.txt"
}

snapshot() {
  local name="$1"
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
    date -Iseconds
    curl -fsS http://localhost:8080/actuator/prometheus |
      awk "/^jvm_memory_used_bytes.*G1 (Old Gen|Eden Space|Survivor Space)|^jvm_gc_live_data_size_bytes|^jvm_gc_max_data_size_bytes|^jvm_gc_pause_seconds_(count|sum)|^jvm_gc_memory_(allocated|promoted)_bytes_total|^hikaricp_connections_(active|pending|timeout)/ {print}"
    free -b
    awk "/pswpin|pswpout/ {print}" /proc/vmstat
  ' > "${RESULT_DIR}/${name}.txt"
}

start_sampler() {
  local name="$1"
  while true; do
    local now
    now="$(date +%s)"
    /usr/bin/curl -fsS "${BASE_URL}/actuator/prometheus" | awk -v now="${now}" '
      /^jvm_memory_used_bytes/ && /id="G1 Old Gen"/ {old=$NF}
      /^jvm_memory_used_bytes/ && /id="G1 Eden Space"/ {eden=$NF}
      /^jvm_gc_live_data_size_bytes/ {live=$NF}
      /^jvm_gc_pause_seconds_count/ {gc_count+=$NF}
      /^jvm_gc_pause_seconds_sum/ {gc_sum+=$NF}
      /^jvm_gc_pause_seconds_count/ && /cause="G1 Humongous Allocation"/ {hum_count+=$NF}
      /^jvm_gc_pause_seconds_sum/ && /cause="G1 Humongous Allocation"/ {hum_sum+=$NF}
      /^jvm_gc_memory_allocated_bytes_total/ {allocated=$NF}
      /^jvm_gc_memory_promoted_bytes_total/ {promoted=$NF}
      END {printf "%s %.0f %.0f %.0f %.0f %.9f %.0f %.9f %.0f %.0f\\n", now, old, eden, live, gc_count, gc_sum, hum_count, hum_sum, allocated, promoted}
    ' >> "${RESULT_DIR}/${name}-timeseries.tsv"
    sleep "${SAMPLE_INTERVAL_SECONDS}"
  done &
  SAMPLER_PID="$!"
}

run_mode() {
  local mode="$1"
  local options="$2"
  local round
  restart_mode "${mode}" "${options}"
  wait_seconds "${mode}-baseline" "${BASELINE_IDLE_SECONDS}"
  snapshot "${mode}-00-baseline"

  for ((round = 1; round <= ROUNDS; round += 1)); do
    echo "[load] ${mode} ${round}/${ROUNDS}: 실제 Gemini V4 동시 ${IMAGE_VUS}건"
    start_sampler "${mode}-round-${round}"
    CONFIRM_PERF=true BASE_URL="${BASE_URL}" IMAGE_API=v4 TEST_MODE=burst \
      BURST_VUS="${IMAGE_VUS}" REQUEST_TIMEOUT=240s \
      k6 run --quiet --summary-export "${RESULT_DIR}/${mode}-round-${round}.json" \
      k6/scenarios/perf_image_generation_step.js
    stop_sampler
    snapshot "${mode}-round-${round}-immediate"
    wait_seconds "${mode}-round-${round}-recovery" "${RECOVERY_IDLE_SECONDS}"
    snapshot "${mode}-round-${round}-recovered"
  done
}

echo "[start] result-dir=${RESULT_DIR}"
printf '%s\\n' 'epoch old eden live gc_count gc_sum hum_count hum_sum allocated promoted' \
  > "${RESULT_DIR}/timeseries-columns.txt"
deploy_override
if [[ "${SKIP_DEFAULT}" != "true" ]]; then
  run_mode "default" ""
fi
run_mode "ihop${IHOP_PERCENT}" "-XX:-G1UseAdaptiveIHOP -XX:InitiatingHeapOccupancyPercent=${IHOP_PERCENT}"

restore_default_mode
trap - EXIT
echo "[complete] ${RESULT_DIR}"
