#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo "Missing .env. Copy .env.example to .env and set the deployment secrets first." >&2
  exit 1
fi

docker compose -f infrastructure/docker-compose.yml up -d
docker compose -f discovery-service/docker-compose.yml up -d --build
docker compose -f user-service/docker-compose.yml up -d --build
docker compose -f catalog-service/docker-compose.yml up -d --build
docker compose -f booking-service/docker-compose.yml up -d --build
docker compose -f notification-service/docker-compose.yml up -d --build
docker compose -f api-gateway/docker-compose.yml up -d --build
