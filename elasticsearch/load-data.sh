#!/bin/bash

ES_URL="${ELASTICSEARCH_URIS}"
ES_AUTH="${ELASTICSEARCH_USERNAME}:${ELASTICSEARCH_PASSWORD}"

# Cargar mapping primero
echo "Cargando mapping..."
curl -X PUT "${ES_URL}/items" \
  -H "Content-Type: application/json" \
  -u "${ES_AUTH}" \
  -d @mapping-items.json

# Cargar datos
echo "Cargando datos..."
while IFS= read -r line; do
  if [ -n "$line" ]; then
    curl -X POST "${ES_URL}/items/_doc" \
      -H "Content-Type: application/json" \
      -u "${ES_AUTH}" \
      -d "$line"
    echo ""
  fi
done < sample-data.ndjson

echo "Carga completada"