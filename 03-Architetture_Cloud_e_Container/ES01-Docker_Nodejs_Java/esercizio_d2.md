# 🚀 Esercizio D: Stack LAMP + Node.js con Nginx Proxy

## Obiettivo

Creare un ambiente di sviluppo completo con **LAMP stack** (Linux, Apache, MySQL, PHP) + **Node.js**, orchestrato con **docker-compose** e accessibile tramite **Nginx reverse proxy** in GitHub Codespaces.

## Architettura

```
                    ┌─────────────────┐
                    │  Nginx Proxy    │
                    │   (Port 80)     │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
           ┌────────▼────────┐   ┌───▼──────────┐
           │  Apache + PHP   │   │   Node.js    │
           │   (LAMP Web)    │   │  (Port 3000) │
           │   (Port 80)     │   └──────┬───────┘
           └────────┬────────┘          │
                    │                   │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼────────┐
                    │     MariaDB      │
                    │   (Port 3306)    │
                    └──────────────────┘
```

**Componenti:**
- **Apache + PHP 8.2**: Web server per applicazioni PHP
- **Node.js**: Runtime JavaScript per backend API
- **MariaDB**: Database relazionale
- **Nginx**: Reverse proxy per routing richieste

---

## Parte 1: Setup Repository e Dev Container

### Step 1.1: Crea Repository GitHub

1. GitHub → **New repository**
2. Nome: `lamp-nodejs-stack`
3. Description: "LAMP + Node.js development environment"
4. Public ✅
5. Initialize with README ✅
6. Create repository

### Step 1.2: Crea Struttura Cartelle

Crea file `STRUCTURE.md` per visualizzare la struttura:

```
lamp-nodejs-stack/
├── .devcontainer/
│   ├── devcontainer.json
│   └── Dockerfile
├── docker/
│   ├── docker-compose.yml
│   ├── nginx/
│   │   └── nginx.conf
│   └── apache/
│       └── sites-available/
│           └── 000-default.conf
├── www/
│   ├── php/
│   │   └── index.php
│   └── nodejs/
│       ├── package.json
│       └── server.js
├── .env.example
└── README.md
```

### Step 1.3: Crea `.devcontainer/devcontainer.json`

```json
{
  "name": "LAMP + Node.js Stack",
  "dockerComposeFile": "../docker/docker-compose.yml",
  "service": "devcontainer",
  "workspaceFolder": "/workspace",
  
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  },
  
  "customizations": {
    "vscode": {
      "extensions": [
        "felixfbecker.php-debug",
        "bmewburn.vscode-intelephense-client",
        "dbaeumer.vscode-eslint",
        "ms-azuretools.vscode-docker",
        "mtxr.sqltools",
        "mtxr.sqltools-driver-mysql"
      ],
      "settings": {
        "php.validate.executablePath": "/usr/local/bin/php",
        "sqltools.connections": [
          {
            "name": "MariaDB",
            "driver": "MySQL",
            "server": "mariadb",
            "port": 3306,
            "database": "lamp_db",
            "username": "lamp_user",
            "password": "lamp_password"
          }
        ]
      }
    }
  },
  
  "forwardPorts": [80, 3000, 3306, 8080],
  "portsAttributes": {
    "80": {
      "label": "Nginx Proxy"
    },
    "3000": {
      "label": "Node.js App"
    },
    "8080": {
      "label": "Apache PHP"
    }
  },
  
  "postCreateCommand": "echo '✅ LAMP + Node.js Stack ready!' && docker-compose version"
}
```

---

## Parte 2: Configurazione Docker Compose

### Step 2.1: Crea `.env.example`

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=rootpassword123
DB_USERNAME=lamp_user
DB_PASSWORD=lamp_password
DB_DATABASE=lamp_db
DB_HOST=mariadb

# Webserver Admin Credentials
WEBSERVER_ADMIN_USERNAME=admin
WEBSERVER_ADMIN_PASSWORD=admin123

# Node.js Environment
NODE_ENV=development
```

**Importante**: Copia `.env.example` → `.env` (git-ignored)

```bash
cp .env.example .env
```

### Step 2.2: Crea `docker/docker-compose.yml`

```yaml
version: "3.8"

networks:
  lamp_network:
    driver: bridge

services:
  # Dev Container - ambiente di sviluppo principale
  devcontainer:
    build:
      context: ..
      dockerfile: .devcontainer/Dockerfile
    container_name: lamp_devcontainer
    volumes:
      - ..:/workspace:cached
    command: sleep infinity
    networks:
      - lamp_network

  # Nginx Reverse Proxy
  nginx:
    image: nginx:1.25-alpine
    container_name: lamp_nginx
    restart: unless-stopped
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - webserver
      - nodejs
    networks:
      - lamp_network

  # Apache + PHP Web Server
  webserver:
    image: php:8.2-apache-bullseye
    container_name: lamp_webserver
    restart: unless-stopped
    expose:
      - "80"
      - "8080"
    ports:
      - "8080:80"  # Accesso diretto (debug)
    depends_on:
      - mariadb
    environment:
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_HOST=mariadb
      - DB_DATABASE=${DB_DATABASE}
      - WEBSERVER_ADMIN_USERNAME=${WEBSERVER_ADMIN_USERNAME}
      - WEBSERVER_ADMIN_PASSWORD=${WEBSERVER_ADMIN_PASSWORD}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 20s
    volumes:
      - ../www/php:/var/www/html
      - ./apache/sites-available/000-default.conf:/etc/apache2/sites-available/000-default.conf
    command: >
      bash -c "
      docker-php-ext-install mysqli pdo pdo_mysql &&
      a2enmod rewrite &&
      apache2-foreground
      "
    networks:
      - lamp_network

  # Node.js Application Server
  nodejs:
    image: node:20-bullseye
    container_name: lamp_nodejs
    restart: unless-stopped
    expose:
      - "3000"
      - "3001"
      - "3002"
    ports:
      - "3000:3000"  # Porta principale
      - "3001:3001"  # Porte aggiuntive
      - "3002:3002"
    environment:
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_HOST=mariadb
      - DB_DATABASE=${DB_DATABASE}
      - NODE_ENV=development
    working_dir: /app
    command: bash -c "npm install && npm start"
    volumes:
      - ../www/nodejs:/app
    depends_on:
      - mariadb
    networks:
      - lamp_network

  # MariaDB Database
  mariadb:
    image: mariadb:11.2
    container_name: lamp_mariadb
    restart: unless-stopped
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_USER=${DB_USERNAME}
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=${DB_DATABASE}
    command: 
      - --max-allowed-packet=128M
      - --innodb-log-file-size=64M
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - lamp_network

volumes:
  mariadb_data:
    driver: local
```

### Step 2.3: Crea `.devcontainer/Dockerfile`

```dockerfile
FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install Docker CLI
RUN apt-get update && apt-get install -y \
    docker.io \
    docker-compose \
    curl \
    wget \
    git \
    && apt-get clean

# Install PHP CLI (per testing)
RUN apt-get install -y php8.1-cli php8.1-mysql php8.1-curl

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

USER vscode
```

---

## Parte 3: Configurazione Nginx Proxy

### Step 3.1: Crea `docker/nginx/nginx.conf`

```nginx
events {
    worker_connections 1024;
}

http {
    # Upstream per Apache/PHP
    upstream php_backend {
        server webserver:80;
    }

    # Upstream per Node.js
    upstream nodejs_backend {
        server nodejs:3000;
    }

    # Server principale - Reverse Proxy
    server {
        listen 80;
        server_name localhost;

        # Log configuration
        access_log /var/log/nginx/access.log;
        error_log /var/log/nginx/error.log;

        # Routing per applicazioni PHP
        location /php/ {
            proxy_pass http://php_backend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Routing per Node.js API
        location /api/ {
            proxy_pass http://nodejs_backend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # WebSocket support
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }

        # Home page
        location / {
            return 200 '
<!DOCTYPE html>
<html>
<head>
    <title>LAMP + Node.js Stack</title>
    <style>
        body { font-family: Arial; padding: 40px; background: #f0f0f0; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
        h1 { color: #333; }
        .service { background: #e8f4f8; padding: 15px; margin: 10px 0; border-left: 4px solid #2196F3; }
        a { color: #2196F3; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 LAMP + Node.js Stack</h1>
        <p>Welcome to your development environment!</p>
        
        <div class="service">
            <h3>📦 PHP Application</h3>
            <a href="/php/" target="_blank">Open PHP App →</a>
        </div>
        
        <div class="service">
            <h3>⚡ Node.js API</h3>
            <a href="/api/" target="_blank">Open Node.js API →</a>
        </div>
        
        <div class="service">
            <h3>🗄️ Database</h3>
            <p>MariaDB running on port 3306</p>
        </div>
    </div>
</body>
</html>
            ';
            add_header Content-Type text/html;
        }
    }
}
```

---

## Parte 4: Applicazioni di Esempio

### Step 4.1: PHP App - `www/php/index.php`

```php
<?php
// Database connection
$host = getenv('DB_HOST') ?: 'mariadb';
$dbname = getenv('DB_DATABASE') ?: 'lamp_db';
$username = getenv('DB_USERNAME') ?: 'lamp_user';
$password = getenv('DB_PASSWORD') ?: 'lamp_password';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $db_status = "✅ Connected to MariaDB";
    
    // Create table if not exists
    $pdo->exec("
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100),
            email VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ");
    
    // Insert sample data
    $stmt = $pdo->query("SELECT COUNT(*) FROM users");
    if ($stmt->fetchColumn() == 0) {
        $pdo->exec("
            INSERT INTO users (name, email) VALUES 
            ('Alice', 'alice@example.com'),
            ('Bob', 'bob@example.com')
        ");
    }
    
    // Fetch users
    $users = $pdo->query("SELECT * FROM users")->fetchAll(PDO::FETCH_ASSOC);
    
} catch(PDOException $e) {
    $db_status = "❌ Connection failed: " . $e->getMessage();
    $users = [];
}
?>
<!DOCTYPE html>
<html>
<head>
    <title>PHP Application</title>
    <style>
        body { font-family: Arial; padding: 40px; background: #f9f9f9; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #4A5568; }
        .status { padding: 15px; margin: 20px 0; border-radius: 5px; background: #E6FFFA; border-left: 4px solid #38B2AC; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #4299E1; color: white; }
        tr:hover { background: #f5f5f5; }
        .info { background: #EBF8FF; padding: 15px; border-radius: 5px; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🐘 PHP Application</h1>
        
        <div class="info">
            <strong>PHP Version:</strong> <?php echo phpversion(); ?><br>
            <strong>Server:</strong> <?php echo $_SERVER['SERVER_SOFTWARE']; ?>
        </div>
        
        <div class="status">
            <strong>Database Status:</strong> <?php echo $db_status; ?>
        </div>
        
        <h2>👥 Users from Database</h2>
        <?php if (!empty($users)): ?>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Created At</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach($users as $user): ?>
                <tr>
                    <td><?php echo $user['id']; ?></td>
                    <td><?php echo htmlspecialchars($user['name']); ?></td>
                    <td><?php echo htmlspecialchars($user['email']); ?></td>
                    <td><?php echo $user['created_at']; ?></td>
                </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
        <?php else: ?>
        <p>No users found.</p>
        <?php endif; ?>
        
        <div style="margin-top: 30px;">
            <a href="/api/" style="color: #4299E1; text-decoration: none;">→ View Node.js API</a>
        </div>
    </div>
</body>
</html>
```

### Step 4.2: Node.js App - `www/nodejs/package.json`

```json
{
  "name": "nodejs-lamp-app",
  "version": "1.0.0",
  "description": "Node.js app for LAMP stack",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.5",
    "cors": "^2.8.5"
  },
  "devDependencies": {
    "nodemon": "^3.0.2"
  }
}
```

### Step 4.3: Node.js App - `www/nodejs/server.js`

```javascript
const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

// Database configuration
const dbConfig = {
  host: process.env.DB_HOST || 'mariadb',
  user: process.env.DB_USERNAME || 'lamp_user',
  password: process.env.DB_PASSWORD || 'lamp_password',
  database: process.env.DB_DATABASE || 'lamp_db'
};

// Create connection pool
const pool = mysql.createPool(dbConfig);

// Routes
app.get('/', async (req, res) => {
  res.json({
    service: 'Node.js API',
    version: '1.0.0',
    environment: process.env.NODE_ENV,
    database: dbConfig.host,
    endpoints: {
      users: '/api/users',
      products: '/api/products',
      health: '/health'
    }
  });
});

app.get('/health', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1');
    res.json({ 
      status: 'ok', 
      database: 'connected',
      uptime: process.uptime()
    });
  } catch (error) {
    res.status(500).json({ 
      status: 'error', 
      database: 'disconnected',
      error: error.message 
    });
  }
});

// Users CRUD
app.get('/api/users', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM users');
    res.json(rows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/users', async (req, res) => {
  const { name, email } = req.body;
  try {
    const [result] = await pool.query(
      'INSERT INTO users (name, email) VALUES (?, ?)',
      [name, email]
    );
    res.status(201).json({ id: result.insertId, name, email });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Products table (esempio aggiuntivo)
app.get('/api/products', async (req, res) => {
  try {
    // Create table if not exists
    await pool.query(`
      CREATE TABLE IF NOT EXISTS products (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100),
        price DECIMAL(10,2),
        category VARCHAR(50),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    
    // Insert sample data if empty
    const [count] = await pool.query('SELECT COUNT(*) as count FROM products');
    if (count[0].count === 0) {
      await pool.query(`
        INSERT INTO products (name, price, category) VALUES
        ('Laptop', 999.99, 'Electronics'),
        ('Mouse', 29.99, 'Electronics'),
        ('Desk', 299.99, 'Furniture')
      `);
    }
    
    const [rows] = await pool.query('SELECT * FROM products');
    res.json(rows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
  console.log('═══════════════════════════════════════════');
  console.log('✅ Node.js API Server Started');
  console.log('═══════════════════════════════════════════');
  console.log(`🌐 Server: http://localhost:${PORT}`);
  console.log(`📊 Health: http://localhost:${PORT}/health`);
  console.log(`👥 Users: http://localhost:${PORT}/api/users`);
  console.log(`📦 Products: http://localhost:${PORT}/api/products`);
  console.log('═══════════════════════════════════════════');
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('🛑 SIGTERM received, closing server...');
  pool.end();
  process.exit(0);
});
```

### Step 4.4: Database Init - `docker/mysql/init.sql`

```sql
-- Initial database setup
CREATE DATABASE IF NOT EXISTS lamp_db;
USE lamp_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT IGNORE INTO users (name, email) VALUES
    ('Alice Smith', 'alice@example.com'),
    ('Bob Johnson', 'bob@example.com'),
    ('Charlie Brown', 'charlie@example.com');

INSERT IGNORE INTO products (name, price, category) VALUES
    ('Laptop', 999.99, 'Electronics'),
    ('Mouse', 29.99, 'Electronics'),
    ('Keyboard', 79.99, 'Electronics'),
    ('Desk', 299.99, 'Furniture');

-- Create indexes
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_category ON products(category);
```

---

## Parte 5: Apache Configuration

### `docker/apache/sites-available/000-default.conf`

```apache
<VirtualHost *:80>
    ServerAdmin webmaster@localhost
    DocumentRoot /var/www/html

    <Directory /var/www/html>
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    # Logging
    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined

    # PHP Settings
    <FilesMatch \.php$>
        SetHandler application/x-httpd-php
    </FilesMatch>
</VirtualHost>
```

---

## Parte 6: Testing e Deploy

### Step 6.1: Avvia lo Stack

```bash
# In Codespaces terminal
cd docker
docker-compose up -d

# Controlla i container
docker-compose ps

# Logs
docker-compose logs -f
```

**Attendi**:
- MariaDB: ~10s
- Apache: ~15s
- Node.js: ~20s (npm install)
- Nginx: ~5s

### Step 6.2: Test Servizi

```bash
# Test Nginx home
curl http://localhost

# Test PHP app
curl http://localhost/php/

# Test Node.js API
curl http://localhost/api/
curl http://localhost/api/users
curl http://localhost/api/products

# Accesso diretto Apache (debug)
curl http://localhost:8080

# Accesso diretto Node.js (debug)
curl http://localhost:3000
```

### Step 6.3: Test Database Connection

```bash
# Entra nel container MariaDB
docker exec -it lamp_mariadb mysql -u lamp_user -plamp_password lamp_db

# Query
SELECT * FROM users;
SELECT * FROM products;
exit;
```

### Step 6.4: Accesso da Browser (Codespaces)

Codespaces forward automaticamente le porte:

- **Porta 80** (Nginx): Click "Open in Browser"
- **Porta 8080** (Apache): Accesso diretto PHP
- **Porta 3000** (Node.js): Accesso diretto API

URL formato: `https://<codespace>-80.preview.app.github.dev`

---

## Parte 7: Development Workflow

### Modifica Codice

```bash
# Modifica PHP
vim www/php/index.php
# → Auto-reload (volume mounted)

# Modifica Node.js
vim www/nodejs/server.js
# → Riavvia container: docker-compose restart nodejs
```

### Aggiungi Nuova Applicazione PHP

```bash
# Crea nuova cartella
mkdir -p www/php/blog

# Crea index.php
cat > www/php/blog/index.php << 'EOF'
<?php
echo "<h1>My Blog</h1>";
echo "PHP Version: " . phpversion();
?>
EOF

# Accessibile su: http://localhost/php/blog/
```

### Aggiungi Porta Node.js

Modifica `docker-compose.yml`:

```yaml
nodejs:
  ports:
    - "3000:3000"
    - "3001:3001"  # Nuova porta
```

Riavvia:
```bash
docker-compose up -d
```

---

## Parte 8: Troubleshooting

### Container non si avvia

```bash
# Controlla logs
docker-compose logs webserver
docker-compose logs nodejs
docker-compose logs mariadb

# Ricostruisci immagini
docker-compose build --no-cache
docker-compose up -d
```

### Database connection failed

```bash
# Verifica credenziali
cat .env

# Test connessione manuale
docker exec -it lamp_mariadb mysql -u root -prootpassword123

# Reset database
docker-compose down -v  # ATTENZIONE: cancella dati!
docker-compose up -d
```

### Nginx 502 Bad Gateway

```bash
# Verifica upstream services
docker-compose ps

# Test connessione interna
docker exec -it lamp_nginx wget -O- http://webserver
docker exec -it lamp_nginx wget -O- http://nodejs:3000
```

---

## ✅ Checklist Completamento

- [ ] Repository GitHub creato
- [ ] `.devcontainer/devcontainer.json` configurato
- [ ] `docker-compose.yml` completo
- [ ] File `.env` creato (da `.env.example`)
- [ ] Nginx configurato
- [ ] App PHP funzionante
- [ ] App Node.js funzionante
- [ ] Database MariaDB operativo
- [ ] Nginx proxy routing corretto
- [ ] Test su tutte le porte superati
- [ ] Accesso da browser Codespaces OK

---

## 📸 Consegna

1. **Repository GitHub** con tutti i file
2. **Screenshot Codespaces** con tutti i container attivi (`docker-compose ps`)
3. **Screenshot Browser**:
   - Nginx home page
   - PHP app con lista utenti
   - Node.js API JSON response
4. **File RELAZIONE.md** con:
   - Architettura spiegata
   - Scelte tecniche
   - Problemi risolti
   - Screenshot

---

## 🎯 Bonus

- [ ] Aggiungere phpMyAdmin per gestione DB visuale
- [ ] Implementare autenticazione JWT nell'API Node.js
- [ ] Aggiungere Redis per caching
- [ ] SSL/TLS con certificati auto-firmati
- [ ] Monitoring con Prometheus + Grafana
- [ ] CI/CD con GitHub Actions

---

## 🚀 Prossimi Passi

Dopo aver completato questo esercizio, sarai in grado di:
- Creare stack multi-servizio con docker-compose
- Configurare reverse proxy Nginx
- Integrare PHP e Node.js con database condiviso
- Deployare su ambiente cloud (AWS, Azure, GCP)
- Scalare orizzontalmente con Kubernetes

**Complimenti!** Hai creato un ambiente di sviluppo LAMP completo! 🎉
