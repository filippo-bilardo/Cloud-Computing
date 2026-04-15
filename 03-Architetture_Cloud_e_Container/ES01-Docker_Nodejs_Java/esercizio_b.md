# 🏗️ Esercizio B: API Gateway Multi-Runtime

## Obiettivo

Creare un sistema distribuito con:
- **Gateway API** (Node.js) → orchestrazione richieste
- **Service Users** (Node.js) → gestione utenti
- **Service Products** (Java) → gestione prodotti
- **docker-compose** → orchestrazione multi-container

## Architettura

```
┌─────────────────┐
│   Client App    │
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│  API Gateway    │ (Node.js :3000)
│   (Express)     │
└────┬──────┬─────┘
     │      │
     │      └─────────────────┐
     │                        │
┌────▼────────┐      ┌────────▼────────┐
│ Users API   │      │  Products API   │
│ (Node.js)   │      │   (Java/Spring) │
│   :3001     │      │      :8080      │
└─────────────┘      └─────────────────┘
```

---

## Parte 1: API Gateway (Node.js)

### `gateway/package.json`

```json
{
  "name": "api-gateway",
  "version": "1.0.0",
  "main": "gateway.js",
  "dependencies": {
    "express": "^4.18.2",
    "axios": "^1.6.0"
  }
}
```

### `gateway/gateway.js`

```javascript
const express = require('express');
const axios = require('axios');
const app = express();

app.use(express.json());

const USERS_SERVICE = process.env.USERS_SERVICE || 'http://localhost:3001';
const PRODUCTS_SERVICE = process.env.PRODUCTS_SERVICE || 'http://localhost:8080';

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'api-gateway' });
});

// Route to Users Service
app.all('/api/users*', async (req, res) => {
  try {
    const response = await axios({
      method: req.method,
      url: `${USERS_SERVICE}${req.url}`,
      data: req.body,
      headers: { 'Content-Type': 'application/json' }
    });
    res.status(response.status).json(response.data);
  } catch (error) {
    res.status(error.response?.status || 500).json({
      error: 'Users service error',
      details: error.message
    });
  }
});

// Route to Products Service
app.all('/api/products*', async (req, res) => {
  try {
    const response = await axios({
      method: req.method,
      url: `${PRODUCTS_SERVICE}${req.url}`,
      data: req.body,
      headers: { 'Content-Type': 'application/json' }
    });
    res.status(response.status).json(response.data);
  } catch (error) {
    res.status(error.response?.status || 500).json({
      error: 'Products service error',
      details: error.message
    });
  }
});

// Home
app.get('/', (req, res) => {
  res.json({
    service: 'API Gateway',
    endpoints: {
      users: '/api/users',
      products: '/api/products'
    }
  });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`✅ API Gateway running on port ${PORT}`);
  console.log(`   Users Service: ${USERS_SERVICE}`);
  console.log(`   Products Service: ${PRODUCTS_SERVICE}`);
});
```

### `gateway/Dockerfile`

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["node", "gateway.js"]
```

---

## Parte 2: Users Service (Node.js)

### `users-service/package.json`

```json
{
  "name": "users-service",
  "version": "1.0.0",
  "main": "server.js",
  "dependencies": {
    "express": "^4.18.2"
  }
}
```

### `users-service/server.js`

```javascript
const express = require('express');
const app = express();
app.use(express.json());

let users = [
  { id: 1, name: 'Alice', email: 'alice@example.com', role: 'admin' },
  { id: 2, name: 'Bob', email: 'bob@example.com', role: 'user' }
];

app.get('/api/users', (req, res) => res.json(users));
app.get('/api/users/:id', (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json(user);
});

app.post('/api/users', (req, res) => {
  const newUser = { id: users.length + 1, ...req.body };
  users.push(newUser);
  res.status(201).json(newUser);
});

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`✅ Users service on port ${PORT}`));
```

### `users-service/Dockerfile`

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3001
CMD ["node", "server.js"]
```

---

## Parte 3: Products Service (Java/Spring Boot)

Usa il codice dell'**Esercizio A** (`java-app`), ma assicurati che:

- Porta 8080
- Endpoint `/api/products`
- CRUD completo

---

## Parte 4: docker-compose.yml

### Root del repository: `docker-compose.yml`

```yaml
version: '3.8'

services:
  gateway:
    build: ./gateway
    container_name: api-gateway
    ports:
      - "3000:3000"
    environment:
      - USERS_SERVICE=http://users-service:3001
      - PRODUCTS_SERVICE=http://products-service:8080
    depends_on:
      - users-service
      - products-service
    networks:
      - app-network

  users-service:
    build: ./users-service
    container_name: users-service
    ports:
      - "3001:3001"
    networks:
      - app-network

  products-service:
    build: ./java-app
    container_name: products-service
    ports:
      - "8080:8080"
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

---

## Parte 5: Testing

### Start All Services

```bash
docker-compose up --build
```

Attendi:
- Gateway: ~5s
- Users: ~5s
- Products: ~30s (Java startup)

### Test Gateway

```bash
# Home
curl http://localhost:3000

# Users (via Gateway)
curl http://localhost:3000/api/users
curl http://localhost:3000/api/users/1

# Products (via Gateway)
curl http://localhost:3000/api/products
curl http://localhost:3000/api/products/1

# Create user
curl -X POST http://localhost:3000/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Charlie","email":"charlie@example.com","role":"user"}'

# Create product
curl -X POST http://localhost:3000/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Tablet","price":599.99,"category":"Electronics"}'
```

### Stop

```bash
docker-compose down
```

---

## Parte 6: Advanced - Monitoring Dashboard

### `dashboard/package.json`

```json
{
  "name": "monitoring-dashboard",
  "version": "1.0.0",
  "dependencies": {
    "express": "^4.18.2",
    "axios": "^1.6.0"
  }
}
```

### `dashboard/server.js`

```javascript
const express = require('express');
const axios = require('axios');
const app = express();

app.get('/', async (req, res) => {
  const services = [
    { name: 'Gateway', url: 'http://gateway:3000/health' },
    { name: 'Users', url: 'http://users-service:3001/api/users' },
    { name: 'Products', url: 'http://products-service:8080/api/products' }
  ];

  const statuses = await Promise.all(
    services.map(async (service) => {
      try {
        const response = await axios.get(service.url, { timeout: 2000 });
        return { ...service, status: 'UP', code: response.status };
      } catch (error) {
        return { ...service, status: 'DOWN', error: error.message };
      }
    })
  );

  res.send(`
    <html>
      <head><title>Services Dashboard</title></head>
      <body style="font-family: Arial; padding: 20px;">
        <h1>🎛️ Services Dashboard</h1>
        ${statuses.map(s => `
          <div style="padding: 10px; margin: 10px 0; background: ${s.status === 'UP' ? '#d4edda' : '#f8d7da'}; border-radius: 5px;">
            <strong>${s.name}</strong>: ${s.status} ${s.code ? `(${s.code})` : `- ${s.error}`}
          </div>
        `).join('')}
      </body>
    </html>
  `);
});

app.listen(4000, () => console.log('✅ Dashboard on http://localhost:4000'));
```

Aggiungi al `docker-compose.yml`:

```yaml
  dashboard:
    build: ./dashboard
    container_name: dashboard
    ports:
      - "4000:4000"
    depends_on:
      - gateway
    networks:
      - app-network
```

---

## ✅ Checklist Completamento

- [ ] API Gateway implementato
- [ ] Users Service implementato
- [ ] Products Service integrato
- [ ] docker-compose.yml configurato
- [ ] `docker-compose up` funzionante
- [ ] Test CRUD via Gateway superati
- [ ] Dashboard (opzionale) funzionante

---

## 📸 Consegna

1. Repository GitHub con tutti i file
2. Screenshot `docker-compose up` (tutti i container attivi)
3. Screenshot test API (curl/Postman)
4. Screenshot Dashboard (opzionale)
5. File `RELAZIONE.md` con architettura e scelte tecniche

---

## 🎯 Bonus

- [ ] Aggiungere Redis per caching
- [ ] Rate limiting sul Gateway
- [ ] Logging centralizzato (ELK)
- [ ] JWT authentication
