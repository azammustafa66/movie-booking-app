#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo "Missing .env. Copy .env.example to .env and set the deployment secrets first." >&2
  exit 1
fi

if ! docker network inspect movie-booking >/dev/null 2>&1; then
  docker network create movie-booking >/dev/null
fi

docker compose up -d --build
