#!/usr/bin/env bash
#
# CI-gated pull deployment (runs on the server from cron, every 5 minutes).
#
#   */5 * * * * /home/dkhal/kh-ecommerce-microservices/scripts/deploy.sh
#
# 1. Ask GitHub for the latest successful CI run on main (public API, no token).
# 2. Check that every check run on that commit is green.
# 3. Compare with the last SUCCESSFUL deploy (state file, not git HEAD), so a
#    failed deploy is retried on the next run.
# 4. Check out exactly that tested commit, BUILD while the old stack keeps
#    running, then `up -d` (only changed containers are replaced; no `down`).
# 5. Wait until no container is starting/unhealthy, then record the commit.
#
# Everything is inside main(), called on the last line: the script lives in the
# repo and `git reset` replaces this file while it runs, so bash must have read
# the whole function before anything happens.

set -euo pipefail

REPO="KhaldounDamach2/kh-ecommerce-microservices"
WORKFLOW="ci.yml"
BRANCH="main"
REPO_DIR="${REPO_DIR:-$(cd "$(dirname "$0")/.." && pwd)}"
LOG_FILE="${LOG_FILE:-$HOME/auto-deploy.log}"
STATE_FILE="${STATE_FILE:-$HOME/.kh-deploy-last-success}"
LOCK_FILE="${LOCK_FILE:-/tmp/kh-deploy.lock}"
HEALTH_TIMEOUT_SECONDS=360 # Java services may need up to 3 minutes to become healthy

log() {
  echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*"
}

wait_until_healthy() {
  local deadline=$((SECONDS + HEALTH_TIMEOUT_SECONDS))
  local not_ready
  while true; do
    # Not ready: any container that is not running (crashed, restarting) or whose
    # healthcheck says starting/unhealthy. Containers without a healthcheck only need to run.
    not_ready=$(docker compose ps -a --format '{{.Name}} {{.State}} {{.Health}}' |
      awk '$2 != "running" || $3 == "starting" || $3 == "unhealthy" {print $1}')
    if [ -z "$not_ready" ]; then
      return 0
    fi
    if [ "$SECONDS" -ge "$deadline" ]; then
      log "Still not healthy after ${HEALTH_TIMEOUT_SECONDS}s: $(echo "$not_ready" | tr '\n' ' ')"
      return 1
    fi
    sleep 10
  done
}

main() {
  # Cron has no terminal: everything (also Docker's errors) goes to the log file.
  # Run by hand, the output is shown on screen and written to the log.
  if [ -t 1 ]; then
    exec > >(tee -a "$LOG_FILE") 2>&1
  else
    exec >>"$LOG_FILE" 2>&1
  fi

  # Only one deploy at a time: a second copy started by cron just leaves.
  exec 9>"$LOCK_FILE"
  if ! flock -n 9; then
    exit 0
  fi

  cd "$REPO_DIR"

  local run_json signal_commit
  run_json=$(curl -fsSL "https://api.github.com/repos/${REPO}/actions/workflows/${WORKFLOW}/runs?branch=${BRANCH}&status=success&per_page=1")
  signal_commit=$(echo "$run_json" | jq -r '.workflow_runs[0].head_sha // empty')
  if [ -z "$signal_commit" ]; then
    log "No successful ${WORKFLOW} run on ${BRANCH}. Skipping."
    exit 0
  fi

  local deployed_commit=""
  [ -f "$STATE_FILE" ] && deployed_commit=$(cat "$STATE_FILE")
  if [ "$deployed_commit" = "$signal_commit" ]; then
    exit 0 # nothing to do; stay quiet so the log only shows real events
  fi

  local checks_json total green
  checks_json=$(curl -fsSL "https://api.github.com/repos/${REPO}/commits/${signal_commit}/check-runs")
  total=$(echo "$checks_json" | jq '.total_count // 0')
  green=$(echo "$checks_json" | jq '[.check_runs[] | select(.conclusion == "success" or .conclusion == "neutral" or .conclusion == "skipped")] | length')
  if [ "$total" -eq 0 ] || [ "$green" -lt "$total" ]; then
    log "Checks for ${signal_commit} not all green (${green}/${total}). Skipping."
    exit 0
  fi

  local started=$SECONDS
  log "Deploying ${signal_commit} (last successful: ${deployed_commit:-none})"

  git fetch --quiet origin "$BRANCH"
  git reset --quiet --hard "$signal_commit" # exactly the commit CI tested

  # Build while the old containers keep serving: the slow part causes no downtime.
  if ! docker compose build --quiet; then
    log "BUILD FAILED for ${signal_commit}; running containers were not touched. Will retry."
    exit 1
  fi

  # Replace only the containers whose image or configuration changed.
  docker compose up -d --remove-orphans

  if ! wait_until_healthy; then
    log "DEPLOY UNHEALTHY for ${signal_commit}. Will retry; check: docker compose ps"
    exit 1
  fi

  echo "$signal_commit" >"$STATE_FILE"
  docker image prune -f >/dev/null # remove the old, now unused image layers
  log "Deploy complete: ${signal_commit} in $((SECONDS - started))s"
}

main "$@"
exit $?
