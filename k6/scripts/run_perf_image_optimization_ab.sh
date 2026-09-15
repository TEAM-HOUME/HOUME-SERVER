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
BASELINE_IDLE_MINUTES="${BASELINE_IDLE_MINUTES:-5}"
RECOVERY_IDLE_MINUTES="${RECOVERY_IDLE_MINUTES:-5}"
SAMPLE_INTERVAL_SECONDS="${SAMPLE_INTERVAL_SECONDS:-2}"
SAMPLER_CURL_TIMEOUT_SECONDS="${SAMPLER_CURL_TIMEOUT_SECONDS:-10}"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
RESULT_DIR="${RESULT_DIR:-/private/tmp/houme-perf/optimization-ab-${RUN_ID}}"
SSH_ARGS=(-i "${PERF_SSH_KEY}" -o BatchMode=yes -o StrictHostKeyChecking=no)
SAMPLER_PID=""

mkdir -p "${RESULT_DIR}"

stop_sampler() {
  if [[ -n "${SAMPLER_PID}" ]]; then
    local sampler_pid="${SAMPLER_PID}"
    local sampler_status
    SAMPLER_PID=""

    if kill -0 "${sampler_pid}" 2>/dev/null; then
      if kill "${sampler_pid}"; then
        if wait "${sampler_pid}"; then
          return 0
        fi
        sampler_status=$?
        # stop_sampler가 보낸 SIGTERM은 정상 종료로 취급한다.
        if [[ "${sampler_status}" -eq 143 ]]; then
          return 0
        fi
        return "${sampler_status}"
      fi
    fi

    wait "${sampler_pid}"
  fi
}

restore_optimized_mode() {
  local restore_status
  stop_sampler
  if ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
    cd /opt/houme-perf
    IMAGE_GEMINI_OPTIMIZATION_ENABLED=true \
      docker compose -f docker-compose.app.yml -f docker-compose.real-gemini.yml up -d --force-recreate app
  ' >/dev/null 2>&1; then
    return 0
  else
    restore_status=$?
  fi
  echo "[failed] optimized mode 복구 실패(status=${restore_status})" >&2
  return "${restore_status}"
}

restore_optimized_mode_on_exit() {
  stop_sampler || true
  restore_optimized_mode || true
}
trap restore_optimized_mode_on_exit EXIT

wait_minutes() {
  local label="$1"
  local minutes="$2"
  local minute
  for ((minute = 1; minute <= minutes; minute += 1)); do
    sleep 60
    echo "[idle] ${label} ${minute}/${minutes}분"
  done
}

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

restart_mode() {
  local enabled="$1"
  echo "[restart] optimization-enabled=${enabled}"
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" "
    cd /opt/houme-perf
    IMAGE_GEMINI_OPTIMIZATION_ENABLED=${enabled} \\
      docker compose -f docker-compose.app.yml -f docker-compose.real-gemini.yml up -d --force-recreate app
  "
  wait_until_ready
}

snapshot() {
  local name="$1"
  ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" '
    date -Iseconds
    curl -fsS http://localhost:8080/actuator/prometheus |
      awk "/^jvm_memory_used_bytes.*G1 (Old Gen|Eden Space|Survivor Space)|^jvm_memory_committed_bytes.*area=\"heap\"|^jvm_gc_live_data_size_bytes|^jvm_gc_max_data_size_bytes|^jvm_gc_pause_seconds_(count|sum)|^jvm_gc_memory_(allocated|promoted)_bytes_total|^process_resident_memory_bytes|^process_cpu_usage|^system_cpu_usage|^hikaricp_connections_(active|pending|timeout)/ {print}"
    free -b
    awk "/pswpin|pswpout/ {print}" /proc/vmstat
    docker stats --no-stream --format "{{.Name}} {{.MemUsage}} {{.MemPerc}} {{.CPUPerc}}"
  ' > "${RESULT_DIR}/${name}.txt"
}

start_sampler() {
  local name="$1"
  while true; do
    local now
    now="$(date +%s)"
    if /usr/bin/curl -fsS --max-time "${SAMPLER_CURL_TIMEOUT_SECONDS}" "${BASE_URL}/actuator/prometheus" | awk -v now="${now}" '
        /^jvm_memory_used_bytes/ && /id="G1 Old Gen"/ {old=$NF}
        /^jvm_memory_used_bytes/ && /id="G1 Eden Space"/ {eden=$NF}
        /^jvm_memory_used_bytes/ && /id="G1 Survivor Space"/ {survivor=$NF}
        /^jvm_gc_live_data_size_bytes/ {live=$NF}
        /^jvm_gc_max_data_size_bytes/ {max=$NF}
        /^jvm_gc_pause_seconds_count/ {gc_count+=$NF}
        /^jvm_gc_pause_seconds_sum/ {gc_sum+=$NF}
        /^jvm_gc_pause_seconds_count/ && /cause="G1 Humongous Allocation"/ {hum_count+=$NF}
        /^jvm_gc_pause_seconds_sum/ && /cause="G1 Humongous Allocation"/ {hum_sum+=$NF}
        /^jvm_gc_memory_allocated_bytes_total/ {allocated=$NF}
        /^jvm_gc_memory_promoted_bytes_total/ {promoted=$NF}
        /^process_resident_memory_bytes/ {rss=$NF}
        END {printf "%s %.0f %.0f %.0f %.0f %.0f %.0f %.9f %.0f %.9f %.0f %.0f %.0f\n", now, old, eden, survivor, live, max, gc_count, gc_sum, hum_count, hum_sum, allocated, promoted, rss}
      ' >> "${RESULT_DIR}/${name}-timeseries.tsv"; then
      :
    else
      local sampler_status=$?
      echo "[sampler] ${name} Prometheus 수집 실패(status=${sampler_status})" >&2
      return "${sampler_status}"
    fi
    sleep "${SAMPLE_INTERVAL_SECONDS}"
  done &
  SAMPLER_PID="$!"
}

heap_guard() {
  local metrics
  local live
  local max
  local used
  local percent
  metrics="$(ssh "${SSH_ARGS[@]}" "${PERF_USER}@${PERF_HOST}" 'curl -fsS http://localhost:8080/actuator/prometheus')"
  live="$(printf '%s\n' "${metrics}" | awk '/^jvm_gc_live_data_size_bytes/ {print $2; exit}')"
  max="$(printf '%s\n' "${metrics}" | awk '/^jvm_gc_max_data_size_bytes/ {print $2; exit}')"
  used="$(printf '%s\n' "${metrics}" | awk '/^jvm_memory_used_bytes/ && /area="heap"/ {sum += $NF} END {print sum + 0}')"
  if awk -v live="${live:-0}" 'BEGIN {exit !(live > 0)}'; then
    percent="$(awk -v live="${live}" -v max="${max}" 'BEGIN {printf "%.2f", live / max * 100}')"
    echo "[guard] heap-after-gc=${percent}%"
  else
    percent="$(awk -v used="${used}" -v max="${max}" 'BEGIN {printf "%.2f", used / max * 100}')"
    echo "[guard] current-heap=${percent}%"
  fi
  if awk -v percent="${percent}" 'BEGIN {exit !(percent >= 80)}'; then
    echo "[stop] 힙 점유율이 80% 이상이므로 다음 요청을 중단합니다." >&2
    return 1
  fi
}

run_mode() {
  local mode="$1"
  local enabled="$2"
  local round
  local sampler_status

  restart_mode "${enabled}"
  wait_minutes "${mode}-baseline" "${BASELINE_IDLE_MINUTES}"
  snapshot "${mode}-00-baseline"
  heap_guard

  for ((round = 1; round <= ROUNDS; round += 1)); do
    echo "[load] ${mode} round ${round}/${ROUNDS}: V4 동시 ${IMAGE_VUS}건"
    start_sampler "${mode}-round-${round}"
    CONFIRM_PERF=true \
      BASE_URL="${BASE_URL}" \
      IMAGE_API=v4 \
      TEST_MODE=burst \
      BURST_VUS="${IMAGE_VUS}" \
      REQUEST_TIMEOUT=240s \
      k6 run --quiet \
        --summary-export "${RESULT_DIR}/${mode}-round-${round}.json" \
        k6/scenarios/perf_image_generation_step.js
    if stop_sampler; then
      :
    else
      sampler_status=$?
      echo "[failed] ${mode} round ${round}/${ROUNDS}: sampler 종료(status=${sampler_status})" >&2
      return "${sampler_status}"
    fi
    snapshot "${mode}-round-${round}-immediate"
    wait_minutes "${mode}-round-${round}-recovery" "${RECOVERY_IDLE_MINUTES}"
    snapshot "${mode}-round-${round}-recovered"
    heap_guard
  done
}

echo "[start] result-dir=${RESULT_DIR}"
printf '%s\n' 'epoch old eden survivor live max gc_count gc_sum hum_count hum_sum allocated promoted rss' \
  > "${RESULT_DIR}/timeseries-columns.txt"

run_mode "unoptimized" "false"
run_mode "optimized" "true"

restore_optimized_mode
trap - EXIT
echo "[complete] ${RESULT_DIR}"
