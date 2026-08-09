#!/bin/bash
# T4 extension: HTTP-level error cases (401 auth, 400 bad request).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/load_key.sh"
RESULTS="$SCRIPT_DIR/results"
COORDS="$RESULTS/coords.json"
mkdir -p "$RESULTS"

SEOL_X=$(jq -r '.seolleung.x' "$COORDS"); SEOL_Y=$(jq -r '.seolleung.y' "$COORDS")
CHEON_X=$(jq -r '.cheonmasan.x' "$COORDS"); CHEON_Y=$(jq -r '.cheonmasan.y' "$COORDS")

# Generic caller: takes an auth header value and a query-string, records http code + body
call() {
  local label="$1" auth="$2" outfile="$3"; shift 3
  local args=("$@")
  local http_code
  http_code=$(curl -sS -G "https://dapi.kakao.com/v2/routing/publictraffic" \
    -H "Authorization: ${auth}" \
    "${args[@]}" \
    -w "%{http_code}" -o "$outfile")
  echo "[$label] HTTP ${http_code} -> $(basename "$outfile")"
}

echo "== E1: 401 invalid key =="
call "E1 invalid-key" "KakaoAK deadbeefdeadbeefdeadbeefdeadbeef" "$RESULTS/e1_401_invalid_key.json" \
  --data-urlencode "start_x=${SEOL_X}" --data-urlencode "start_y=${SEOL_Y}" \
  --data-urlencode "end_x=${CHEON_X}" --data-urlencode "end_y=${CHEON_Y}"

echo "== E2: 401 missing Authorization header =="
# empty auth header value
call "E2 no-auth" "" "$RESULTS/e2_401_no_auth.json" \
  --data-urlencode "start_x=${SEOL_X}" --data-urlencode "start_y=${SEOL_Y}" \
  --data-urlencode "end_x=${CHEON_X}" --data-urlencode "end_y=${CHEON_Y}"

echo "== E3: 400 missing required params (only start_x) =="
call "E3 missing-params" "KakaoAK ${KAKAO_REST_API_KEY}" "$RESULTS/e3_400_missing_params.json" \
  --data-urlencode "start_x=${SEOL_X}"

echo "== E4: 400 non-numeric coordinate =="
call "E4 bad-coord" "KakaoAK ${KAKAO_REST_API_KEY}" "$RESULTS/e4_400_bad_coord.json" \
  --data-urlencode "start_x=abc" --data-urlencode "start_y=${SEOL_Y}" \
  --data-urlencode "end_x=${CHEON_X}" --data-urlencode "end_y=${CHEON_Y}"

echo "== E5: 400 out-of-range coordinate (lat/lng swapped-ish, x=999) =="
call "E5 out-of-range" "KakaoAK ${KAKAO_REST_API_KEY}" "$RESULTS/e5_400_out_of_range.json" \
  --data-urlencode "start_x=999" --data-urlencode "start_y=999" \
  --data-urlencode "end_x=${CHEON_X}" --data-urlencode "end_y=${CHEON_Y}"

echo "--- bodies ---"
for f in e1_401_invalid_key e2_401_no_auth e3_400_missing_params e4_400_bad_coord e5_400_out_of_range; do
  echo "### $f"
  cat "$RESULTS/$f.json" 2>/dev/null; echo ""
done
