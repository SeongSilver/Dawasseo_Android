#!/bin/bash
# Load KAKAO_REST_API_KEY from local.properties into env. Never prints the key.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROP_FILE="$SCRIPT_DIR/../local.properties"

if [ ! -f "$PROP_FILE" ]; then
  echo "ERROR: local.properties not found" >&2
  exit 1
fi

KAKAO_REST_API_KEY="$(grep '^KAKAO_REST_API_KEY=' "$PROP_FILE" | cut -d'=' -f2- | tr -d '[:space:]')"
export KAKAO_REST_API_KEY

if [ -z "$KAKAO_REST_API_KEY" ]; then
  echo "ERROR: KAKAO_REST_API_KEY is empty" >&2
  exit 1
fi
