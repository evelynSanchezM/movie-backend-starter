#!/bin/bash

# Configuración para Elasticsearch sin autenticación
ES_URL="${ELASTICSEARCH_URIS:-http://localhost:9200}"
INDEX_NAME="items"

echo "🔗 Conectando a Elasticsearch sin autenticación: $ES_URL"

# Función para esperar a Elasticsearch
wait_for_es() {
  until curl -s "$ES_URL/_cluster/health" | grep -q '"status":"green\|yellow"'; do
    echo "⏰ Esperando que Elasticsearch esté disponible..."
    sleep 5
  done
}

# Función para cargar datos
load_data() {
  echo "📋 Creando índice con mapping..."
  
  # Crear índice (eliminar primero si existe)
  curl -X DELETE "$ES_URL/$INDEX_NAME" --silent
  curl -X PUT "$ES_URL/$INDEX_NAME" \
    -H "Content-Type: application/json" \
    -d @mapping-items.json \
    --fail --silent
  
  echo "📊 Cargando datos..."
  
  # Cargar datos en formato NDJSON
  curl -X POST "$ES_URL/_bulk" \
    -H "Content-Type: application/x-ndjson" \
    --data-binary @sample-data.ndjson \
    --fail --silent
  
  echo "✅ ¡Datos cargados exitosamente!"
  
  # Verificar la carga
  echo "🔍 Verificando datos..."
  curl -s "$ES_URL/$INDEX_NAME/_count" | jq .
}

# Ejecutar
wait_for_es
load_data

echo "🎉 Proceso completado!"
echo "📊 Puedes acceder a los datos en: $ES_URL/$INDEX_NAME/_search"

# Mantener el contenedor vivo para debugging
sleep 3600