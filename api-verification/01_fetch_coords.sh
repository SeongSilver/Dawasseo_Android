#!/bin/bash
# Test 0: fetch station coordinates via Kakao keyword place search (SW8 = subway station)
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/load_key.sh"
RESULTS="$SCRIPT_DIR/results"
mkdir -p "$RESULTS"

search_station() {
  local query="$1" outfile="$2"
  curl -sS -G "https://dapi.kakao.com/v2/local/search/keyword.json" \
    -H "Authorization: KakaoAK ${KAKAO_REST_API_KEY}" \
    --data-urlencode "query=${query}" \
    --data-urlencode "category_group_code=SW8" \
    -o "$outfile"
}

search_station "선릉역" "$RESULTS/place_seolleung.json"
search_station "강남역" "$RESULTS/place_gangnam.json"
search_station "천마산역" "$RESULTS/place_cheonmasan.json"

# Pick best match: prefer exact station-name prefix match; for 천마산역 require 남양주 address
pick() {
  local file="$1" name_prefix="$2" addr_filter="${3:-}"
  jq --arg p "$name_prefix" --arg a "$addr_filter" '
    [.documents[]
     | select(.place_name | startswith($p))
     | select(($a == "") or (.address_name | contains($a)))
    ][0] // .documents[0]
    | {place_name, address_name, x, y}' "$file"
}

jq -n \
  --argjson seolleung "$(pick "$RESULTS/place_seolleung.json" "선릉역")" \
  --argjson gangnam   "$(pick "$RESULTS/place_gangnam.json" "강남역")" \
  --argjson cheonmasan "$(pick "$RESULTS/place_cheonmasan.json" "천마산역" "남양주")" \
  '{seolleung: $seolleung, gangnam: $gangnam, cheonmasan: $cheonmasan}' \
  > "$RESULTS/coords.json"

echo "--- resolved coordinates ---"
cat "$RESULTS/coords.json"
