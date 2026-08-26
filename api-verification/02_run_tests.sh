#!/bin/bash
# T1/T2/T4: Kakao public transit routing API verification
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/load_key.sh"
RESULTS="$SCRIPT_DIR/results"
COORDS="$RESULTS/coords.json"

if [ ! -f "$COORDS" ]; then
  echo "ERROR: run 01_fetch_coords.sh first" >&2
  exit 1
fi

SEOL_X=$(jq -r '.seolleung.x' "$COORDS"); SEOL_Y=$(jq -r '.seolleung.y' "$COORDS")
GANG_X=$(jq -r '.gangnam.x' "$COORDS");   GANG_Y=$(jq -r '.gangnam.y' "$COORDS")
CHEON_X=$(jq -r '.cheonmasan.x' "$COORDS"); CHEON_Y=$(jq -r '.cheonmasan.y' "$COORDS")

route() {
  local sx="$1" sy="$2" ex="$3" ey="$4" outfile="$5"
  local http_code
  http_code=$(curl -sS -G "https://dapi.kakao.com/v2/routing/publictraffic" \
    -H "Authorization: KakaoAK ${KAKAO_REST_API_KEY}" \
    --data-urlencode "start_x=${sx}" \
    --data-urlencode "start_y=${sy}" \
    --data-urlencode "end_x=${ex}" \
    --data-urlencode "end_y=${ey}" \
    -w "%{http_code}" -o "$outfile")
  echo "HTTP ${http_code} -> $(basename "$outfile")"
}

echo "== T1: 선릉역 -> 천마산역 =="
route "$SEOL_X" "$SEOL_Y" "$CHEON_X" "$CHEON_Y" "$RESULTS/t1_seolleung_cheonmasan.json"

echo "== T2: 강남역 -> 선릉역 =="
route "$GANG_X" "$GANG_Y" "$SEOL_X" "$SEOL_Y" "$RESULTS/t2_gangnam_seolleung.json"

echo "== T4a: EQUAL_POINTS (선릉역 -> 선릉역) =="
route "$SEOL_X" "$SEOL_Y" "$SEOL_X" "$SEOL_Y" "$RESULTS/t4a_equal_points.json"

echo "== T4b: NO_RESULTS (선릉역 -> 제주시청) =="
# Jeju City Hall coords via keyword search (not hardcoded)
curl -sS -G "https://dapi.kakao.com/v2/local/search/keyword.json" \
  -H "Authorization: KakaoAK ${KAKAO_REST_API_KEY}" \
  --data-urlencode "query=제주시청" \
  -o "$RESULTS/place_jeju_cityhall.json"
JEJU_X=$(jq -r '[.documents[] | select(.address_name | contains("제주"))][0].x' "$RESULTS/place_jeju_cityhall.json")
JEJU_Y=$(jq -r '[.documents[] | select(.address_name | contains("제주"))][0].y' "$RESULTS/place_jeju_cityhall.json")
route "$SEOL_X" "$SEOL_Y" "$JEJU_X" "$JEJU_Y" "$RESULTS/t4b_no_results.json"

echo "done"
