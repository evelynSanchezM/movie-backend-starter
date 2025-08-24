#!/usr/bin/env bash
set -euo pipefail
ES_URL="${ES_URL:-http://localhost:9200}"
curl -s -X PUT "$ES_URL/items" -H "Content-Type: application/json" -d @elasticsearch/mapping-items.json
curl -s -X POST "$ES_URL/_bulk" -H "Content-Type: application/x-ndjson" --data-binary @elasticsearch/sample-data.ndjson
echo OK
