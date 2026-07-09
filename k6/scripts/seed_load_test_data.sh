#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/../seeds/load_test_seed.sql"

if [[ ! -f "${SQL_FILE}" ]]; then
  echo "[ERROR] SQL 파일을 찾을 수 없습니다: ${SQL_FILE}" >&2
  exit 1
fi

require_env() {
  local key="$1"
  if [[ -z "${!key:-}" ]]; then
    echo "[ERROR] 환경변수 ${key} 가 필요합니다." >&2
    exit 1
  fi
}

require_env DB_HOST
require_env DB_NAME
require_env DB_USER
require_env DB_PASSWORD

DB_PORT="${DB_PORT:-5432}"
SEED_USER_START="${SEED_USER_START:-1}"
SEED_USER_END="${SEED_USER_END:-200}"
HOUSE_ID="${HOUSE_ID:-1813}"
HOUSE_OWNER_ID="${HOUSE_OWNER_ID:-1}"
CREDITS_PER_USER="${CREDITS_PER_USER:-300}"

if (( SEED_USER_START > SEED_USER_END )); then
  echo "[ERROR] SEED_USER_START는 SEED_USER_END보다 클 수 없습니다." >&2
  exit 1
fi

if (( HOUSE_OWNER_ID < SEED_USER_START || HOUSE_OWNER_ID > SEED_USER_END )); then
  echo "[ERROR] HOUSE_OWNER_ID는 시드 사용자 범위 안에 있어야 합니다." >&2
  exit 1
fi

run_psql_native() {
  PGPASSWORD="${DB_PASSWORD}" psql \
    -h "${DB_HOST}" \
    -p "${DB_PORT}" \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -v ON_ERROR_STOP=1 \
    -v seed_user_start="${SEED_USER_START}" \
    -v seed_user_end="${SEED_USER_END}" \
    -v credits_per_user="${CREDITS_PER_USER}" \
    -v house_id="${HOUSE_ID}" \
    -v house_owner_id="${HOUSE_OWNER_ID}" \
    -f "${SQL_FILE}"
}

run_psql_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "[ERROR] psql이 없고 docker도 없어 시드를 실행할 수 없습니다." >&2
    exit 1
  fi

  docker run --rm \
    --network host \
    -v "${SQL_FILE}:/tmp/load_test_seed.sql:ro" \
    -e PGPASSWORD="${DB_PASSWORD}" \
    postgres:16-alpine \
    psql \
      -h "${DB_HOST}" \
      -p "${DB_PORT}" \
      -U "${DB_USER}" \
      -d "${DB_NAME}" \
      -v ON_ERROR_STOP=1 \
      -v seed_user_start="${SEED_USER_START}" \
      -v seed_user_end="${SEED_USER_END}" \
      -v credits_per_user="${CREDITS_PER_USER}" \
      -v house_id="${HOUSE_ID}" \
      -v house_owner_id="${HOUSE_OWNER_ID}" \
      -f /tmp/load_test_seed.sql
}

echo "[INFO] load test seed 시작"
echo "[INFO] target=${DB_HOST}:${DB_PORT}/${DB_NAME} user-range=${SEED_USER_START}-${SEED_USER_END} credits-per-user=${CREDITS_PER_USER}"

if command -v psql >/dev/null 2>&1; then
  run_psql_native
else
  run_psql_docker
fi

echo "[INFO] load test seed 완료"
