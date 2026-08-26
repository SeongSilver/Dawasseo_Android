#!/bin/bash
# T3: late-night awareness test. Run manually after 00:30.
# Same call as T1 (선릉역 -> 천마산역); saves status / route presence / subway inclusion.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/load_key.sh"
RESULTS="$SCRIPT_DIR/results"
COORDS="$RESULTS/coords.json"

if [ ! -f "$COORDS" ]; then
  echo "ERROR: run 01_fetch_coords.sh first" >&2
  exit 1
fi

TS=$(date +%Y%m%d_%H%M%S)
RAW="$RESULTS/night_raw_${TS}.json"
OUT="$RESULTS/night_${TS}.json"

SEOL_X=$(jq -r '.seolleung.x' "$COORDS"); SEOL_Y=$(jq -r '.seolleung.y' "$COORDS")
CHEON_X=$(jq -r '.cheonmasan.x' "$COORDS"); CHEON_Y=$(jq -r '.cheonmasan.y' "$COORDS")

HTTP_CODE=$(curl -sS -G "https://dapi.kakao.com/v2/routing/publictraffic" \
  -H "Authorization: KakaoAK ${KAKAO_REST_API_KEY}" \
  --data-urlencode "start_x=${SEOL_X}" \
  --data-urlencode "start_y=${SEOL_Y}" \
  --data-urlencode "end_x=${CHEON_X}" \
  --data-urlencode "end_y=${CHEON_Y}" \
  -w "%{http_code}" -o "$RAW")

jq \
  --arg executed_at "$(date '+%Y-%m-%d %H:%M:%S %z')" \
  --arg http_code "$HTTP_CODE" \
  '{
    executed_at: $executed_at,
    http_code: $http_code,
    status: (.status // "unknown"),
    route_count: ((.routes // []) | length),
    counts_by_type: (.properties | {bus, subway, busAndSubway}),
    has_routes: (((.routes // []) | length) > 0),
    has_subway_route: ([(.routes // [])[] | select(.properties.type == "SUBWAY" or .properties.type == "BUS_AND_SUBWAY")] | length > 0),
    night_bus_vehicles: ([(.routes // [])[].steps[].properties.vehicles[]? | select(.name | contains("심야")) | .name] | unique),
    route_summaries: [(.routes // [])[] | {
      type: .properties.type,
      transfers: .properties.transfers,
      totalTime: .properties.totalTime,
      step_types: [.steps[].properties.type]
    }]
  }' "$RAW" > "$OUT" || jq -n \
  --arg executed_at "$(date '+%Y-%m-%d %H:%M:%S %z')" \
  --arg http_code "$HTTP_CODE" \
  '{executed_at: $executed_at, http_code: $http_code, note: "raw parse failed, see night_raw file"}' > "$OUT"

echo "saved: $OUT (raw: $RAW)"
cat "$OUT"
