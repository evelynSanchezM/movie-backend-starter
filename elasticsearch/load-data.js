const { Client } = require('@elastic/elasticsearch');
const fs = require('fs');

const client = new Client({
  node: process.env.ELASTICSEARCH_URIS,
  auth: {
    username: process.env.ELASTICSEARCH_USERNAME,
    password: process.env.ELASTICSEARCH_PASSWORD
  }
});

async function loadData() {
  try {
    // Leer el archivo NDJSON
    const data = fs.readFileSync('sample-data.ndjson', 'utf8');
    const lines = data.split('\n').filter(line => line.trim());
    
    // Cargar datos
    for (const line of lines) {
      const doc = JSON.parse(line);
      await client.index({
        index: 'items',
        body: doc
      });
      console.log('Documento insertado:', doc.id || doc.name);
    }
    
    console.log('Carga de datos completada');
  } catch (error) {
    console.error('Error:', error);
  }
}

loadData();