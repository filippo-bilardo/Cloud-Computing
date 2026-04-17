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
2. Nome: `lamp-stack`
3. Description: "LAMP development environment"
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
  "name": "LAMP Stack Dev",
  "image": "mcr.microsoft.com/devcontainers/base:ubuntu",
  
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  },
  
  "customizations": {
    "vscode": {
      "extensions": [
        "felixfbecker.php-debug",
        "bmewburn.vscode-intelephense-client",
        "ms-azuretools.vscode-docker"
      ]
    }
  },
  
  "forwardPorts": [80, 3306],
  
  "postCreateCommand": "docker-compose up -d && echo '✅ LAMP stack started!'"
}
```
  },
  
  "forwardPorts": [80, 3306],
  "portsAttributes": {
    "80": {
      "label": "Apache PHP"
    },
    "3306": {
      "label": "MariaDB"
    }
  "forwardPorts": [80, 3306],
  
  "postCreateCommand": "docker-compose up -d && echo '✅ LAMP stack started!'"
}
```

> **📝 Nota**: Questo dev container usa Docker-in-Docker per gestire i container LAMP dall'esterno, non entrando nel container webserver. Questo permette di usare `docker-compose` e `docker` normalmente.

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

## Parte 5: Creazione Codespace e Testing

### Step 5.1: Crea Codespace

1. Vai sul tuo repository GitHub
2. Click su **Code** (verde)
3. Tab **Codespaces**
4. Click **Create codespace on main**
5. Attendi 2-3 minuti → VS Code si apre nel browser!

Il dev container:
- Legge `docker-compose.yml`
- Avvia i servizi (Apache + MariaDB)
- Monta i volumi
- Forward delle porte

### Step 5.2: Verifica Container Attivi

Nel terminal Codespace:

```bash
# Controlla i container
docker-compose ps

# Output atteso:
# NAME              IMAGE                           STATUS
# lamp_webserver    php:8.2.5-apache-bullseye      Up
# lamp_mariadb      mariadb                         Up

# Logs in tempo reale
docker-compose logs -f

# Stop logs: Ctrl+C
```

**Attendi startup**:
- MariaDB: ~10-15s
- Apache: ~20-30s (installa extensions PHP)

### Step 5.3: Crea Database di Esempio

```bash
# Entra nel container MariaDB
docker exec -it lamp_mariadb mysql -u lamp_user -plamp_password lamp_db
```

> **📝 Nota**: Il comando `mysql` non è installato nell'host Codespace. Usiamo `docker exec` per eseguire il client MySQL **dentro** il container MariaDB.

```sql
# Verifica che il database esista
SHOW DATABASES;

# Verifica tabelle create da init.sql
SHOW TABLES;

# Verifica dati nelle tabelle
SELECT * FROM users;
SELECT * FROM products;

# Esci
exit;
```

**Output atteso**:
```
mysql> SELECT * FROM users;
+----+-----------+-----------------------+
| id | name      | email                 |
+----+-----------+-----------------------+
|  1 | John Doe  | john@example.com      |
|  2 | Jane Smith| jane@example.com      |
+----+-----------+-----------------------+

mysql> SELECT * FROM products;
+----+-----------+--------+
| id | name      | price  |
+----+-----------+--------+
|  1 | Laptop    | 999.99 |
|  2 | Mouse     |  29.99 |
+----+-----------+--------+
```

### Step 5.4: Verifica Sito Web

**Metodo 1: curl (dal terminal)**

```bash
# Test homepage
curl http://localhost

# Output atteso: HTML con titolo "LAMP Stack - Home"
```

**Metodo 2: Browser (Codespaces)**

1. Guarda le notifiche in basso a destra: **"Porta 80 disponibile"**
2. Click su **"Open in Browser"** o sull'icona 🌐
3. URL formato: `https://<codespace>-80.preview.app.github.dev`

**Cosa dovresti vedere**:
- Homepage con titolo "LAMP Stack - Home"
- Lista utenti dal database
- Lista prodotti dal database
- Informazioni PHP (versione, estensioni)

### Step 5.5: Test Connessione Database dalla Web App

Nel browser, vai su:
```
https://<codespace>-80.preview.app.github.dev/db-test.php
```

Crea il file di test:

```bash
cat > www/db-test.php << 'EOF'
<?php
$host = getenv('DB_HOST');
$user = getenv('DB_USERNAME');
$pass = getenv('DB_PASSWORD');
$db = getenv('DB_DATABASE');

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db", $user, $pass);
    echo "<h1>✅ Connessione Database OK!</h1>";
    
    // Query users
    $stmt = $pdo->query("SELECT * FROM users");
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<h2>Utenti:</h2><ul>";
    foreach($users as $user) {
        echo "<li>{$user['name']} - {$user['email']}</li>";
    }
    echo "</ul>";
    
} catch(PDOException $e) {
    echo "<h1>❌ Errore: " . $e->getMessage() . "</h1>";
}
?>
EOF
```

Refresh browser → Dovresti vedere la lista utenti!

---

## Parte 6: Development Workflow

### Modifica Codice PHP

```bash
# Modifica index.php (dal workspace root)
code www/index.php

# I file sono montati come volume in docker-compose.yml
# Le modifiche sono visibili IMMEDIATAMENTE nel container
# → Refresh browser per vedere cambiamenti
```

> **💡 Tip**: Non devi entrare nel container per modificare i file! Lavora direttamente dalla cartella `www/` nel tuo workspace Codespaces. I volumi Docker sincronizzano automaticamente.

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

### Accesso al Database Durante Sviluppo

**Opzione 1: Docker exec (veloce)**
```bash
docker exec -it lamp_mariadb mysql -u lamp_user -plamp_password lamp_db
```

**Opzione 2: MySQL Workbench / DBeaver (GUI remota)**
```
Host: localhost (via port forward Codespaces)
Port: 3306
User: lamp_user
Password: lamp_password
Database: lamp_db
```

**Opzione 3: VS Code Extension (SQLTools)**
L'estensione è già configurata in `devcontainer.json`!
1. Apri "SQLTools" nella sidebar
2. Click su "MariaDB" connection
3. Esplora tabelle e query!

---

## Parte 7: Troubleshooting

### ❌ `docker-compose: command not found`

**Causa**: Stai usando il vecchio devcontainer.json che entra nel service webserver (che non ha Docker).

**Soluzione**: Aggiorna `.devcontainer/devcontainer.json` con la configurazione corretta:

```json
{
  "name": "LAMP Stack Dev",
  "image": "mcr.microsoft.com/devcontainers/base:ubuntu",
  
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  },
  
  "customizations": {
    "vscode": {
      "extensions": [
        "felixfbecker.php-debug",
        "bmewburn.vscode-intelephense-client",
        "ms-azuretools.vscode-docker"
      ]
    }
  },
  
  "forwardPorts": [80, 3306],
  
  "postCreateCommand": "docker-compose up -d && echo '✅ LAMP stack started!'"
}
```

Poi **Rebuild Container**: `Cmd/Ctrl + Shift + P` → "Codespaces: Rebuild Container"

### ❌ `mysql: command not found`

**Causa**: Il client MySQL non è installato nell'host Codespace.

**Soluzione 1 - Usa docker exec** (consigliato):
```bash
# Esegui mysql client DENTRO il container MariaDB
docker exec -it lamp_mariadb mysql -u lamp_user -plamp_password lamp_db
```

**Soluzione 2 - Installa mysql-client nell'host** (opzionale):
```bash
# Installa client MySQL nell'ambiente Codespace
sudo apt-get update
sudo apt-get install -y mysql-client

# Ora puoi connetterti direttamente
mysql -h 127.0.0.1 -P 3306 -u lamp_user -plamp_password lamp_db
```

**Soluzione 3 - Usa phpMyAdmin**:
Aggiungi al `docker-compose.yml`:
```yaml
  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    container_name: lamp_phpmyadmin
    environment:
      - PMA_HOST=mariadb
      - PMA_USER=lamp_user
      - PMA_PASSWORD=lamp_password
    ports:
      - "8080:80"
    depends_on:
      - mariadb
    networks:
      - lamp_network
```
Poi accedi via browser: `http://localhost:8080`

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
- [ ] Apache configurato
- [ ] App PHP funzionante
- [ ] Database MariaDB operativo
- [ ] Test connessione database superato
- [ ] Accesso da browser Codespaces OK

---

## 📸 Consegna

1. **Repository GitHub** con tutti i file
2. **Screenshot Codespaces** con tutti i container attivi (`docker-compose ps`)
3. **Screenshot Browser**:
   - Apache home page
   - PHP app con lista utenti
4. **File RELAZIONE.md** con:
   - Architettura spiegata
   - Scelte tecniche
   - Problemi risolti
   - Screenshot

---

## 🎯 Bonus

- [ ] Aggiungere phpMyAdmin per gestione DB visuale
- [ ] Implementare autenticazione con sessioni PHP
- [ ] Aggiungere Redis per caching
- [ ] SSL/TLS con certificati auto-firmati
- [ ] Monitoring con Prometheus + Grafana
- [ ] CI/CD con GitHub Actions

---

## 🚀 Prossimi Passi

Dopo aver completato questo esercizio, sarai in grado di:
- Creare stack multi-servizio con docker-compose
- Configurare networking tra container Docker
- Integrare PHP con database per applicazioni web
- Deployare su ambiente cloud (AWS, Azure, GCP)
- Scalare orizzontalmente con Kubernetes

**Complimenti!** Hai creato un ambiente di sviluppo LAMP completo! 🎉
