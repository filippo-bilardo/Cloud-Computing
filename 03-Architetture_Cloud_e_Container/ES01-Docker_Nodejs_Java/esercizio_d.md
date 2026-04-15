# 🐘 Esercizio D: Stack LAMP Classico

## Obiettivo

Creare un ambiente di sviluppo **LAMP stack** (Linux, Apache, MySQL, PHP) completo, orchestrato con **docker-compose** e accessibile tramite GitHub Codespaces.

## Architettura

```
┌─────────────────┐
│  Client Browser │
└────────┬────────┘
         │ HTTP :80
┌────────▼────────┐
│  Apache + PHP   │
│   (Port 80)     │
└────────┬────────┘
         │
┌────────▼────────┐
│     MariaDB     │
│   (Port 3306)   │
└─────────────────┘
```

**Componenti:**
- **Apache + PHP 8.2**: Web server per applicazioni PHP
- **MariaDB**: Database relazionale MySQL-compatible

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
lamp-stack/
├── .devcontainer/
│   └── devcontainer.json
├── docker-compose.yml
├── apache/
│   └── sites-available/
│       └── 000-default.conf
├── www/
│   └── index.php
├── mysql/
│   └── init.sql
├── .env.example
├── .env
└── README.md
```

### Step 1.3: Crea `.devcontainer/devcontainer.json`

```json
{
  "name": "LAMP Stack",
  "dockerComposeFile": "../docker-compose.yml",
  "service": "webserver",
  "workspaceFolder": "/var/www/html",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "felixfbecker.php-debug",
        "bmewburn.vscode-intelephense-client",
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
  
  "forwardPorts": [80, 3306],
  "portsAttributes": {
    "80": {
      "label": "Apache PHP"
    },
    "3306": {
      "label": "MariaDB"
    }
  },
  
  "postCreateCommand": "echo '✅ LAMP Stack ready!'"
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

### Step 2.2: Crea `docker-compose.yml`

```yaml
version: "3.8"

networks:
  lamp_network:
    driver: bridge

services:
  # Apache + PHP Web Server
  webserver:
    image: php:8.2-apache-bullseye
    container_name: lamp_webserver
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      - mariadb
    environment:
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_HOST=mariadb
      - DB_DATABASE=${DB_DATABASE}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 20s
    volumes:
      - ./www:/var/www/html
      - ./apache/sites-available/000-default.conf:/etc/apache2/sites-available/000-default.conf
    command: >
      bash -c "
      apt-get update &&
      apt-get install -y curl &&
      docker-php-ext-install mysqli pdo pdo_mysql &&
      a2enmod rewrite &&
      apache2-foreground
      "
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

---

## Parte 3: Applicazioni di Esempio

### Step 3.1: PHP App - `www/index.php`

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
            <p style="color: #718096;">LAMP Stack: Linux + Apache + MySQL + PHP</p>
        </div>
    </div>
</body>
</html>
```

### Step 3.2: Database Init - `mysql/init.sql`

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

## Parte 4: Apache Configuration

### Step 4.1: `apache/sites-available/000-default.conf`

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

## Parte 5: Testing e Deploy

### Step 5.1: Avvia lo Stack

```bash
# In Codespaces terminal (o locale)
docker-compose up -d

# Controlla i container
docker-compose ps

# Logs
docker-compose logs -f
```

**Attendi**:
- MariaDB: ~10s
- Apache: ~20s (installa extensions PHP)

### Step 5.2: Test Applicazione

```bash
# Test PHP app
curl http://localhost

# Apri nel browser (Codespaces)
# Click sulla notifica "Porta 80 disponibile"
```

### Step 5.3: Test Database Connection

```bash
# Entra nel container MariaDB
docker exec -it lamp_mariadb mysql -u lamp_user -plamp_password lamp_db

# Query
SELECT * FROM users;
SELECT * FROM products;
exit;
```

### Step 5.4: Accesso da Browser (Codespaces)

Codespaces forward automaticamente la porta 80:

- **Porta 80** (Apache): Click "Open in Browser"

URL formato: `https://<codespace>-80.preview.app.github.dev`

---

## Parte 6: Development Workflow

### Modifica Codice PHP

```bash
# Modifica index.php
vim www/index.php
# → Auto-reload (volume mounted)
# → Refresh browser per vedere cambiamenti
```

### Aggiungi Nuova Pagina PHP

```bash
# Crea nuova cartella
mkdir -p www/blog

# Crea index.php
cat > www/blog/index.php << 'EOF'
<?php
echo "<h1>My Blog</h1>";
echo "PHP Version: " . phpversion();
?>
EOF

# Accessibile su: http://localhost/blog/
```

### Aggiungi Script PHP (CRUD Example)

```bash
# Crea API PHP per gestione prodotti
cat > www/api.php << 'EOF'
<?php
header('Content-Type: application/json');

$host = getenv('DB_HOST') ?: 'mariadb';
$dbname = getenv('DB_DATABASE') ?: 'lamp_db';
$username = getenv('DB_USERNAME') ?: 'lamp_user';
$password = getenv('DB_PASSWORD') ?: 'lamp_password';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // GET all products
    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        $stmt = $pdo->query("SELECT * FROM products");
        $products = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo json_encode($products);
    }
    
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}
?>
EOF

# Test: curl http://localhost/api.php
```

---

## Parte 7: Troubleshooting

### Container non si avvia

```bash
# Controlla logs
docker-compose logs webserver
docker-compose logs mariadb

# Riavvia servizi
docker-compose restart
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

### Apache non risponde

```bash
# Verifica container attivo
docker-compose ps

# Check logs Apache
docker-compose logs webserver

# Entra nel container per debug
docker exec -it lamp_webserver bash
curl http://localhost
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
