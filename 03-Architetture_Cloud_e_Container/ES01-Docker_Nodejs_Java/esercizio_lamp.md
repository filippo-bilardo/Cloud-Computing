# 🔬 Esercizio LAMP: Setup Stack LAMP con GitHub Codespace

## Obiettivo

Creare un repository GitHub con ambiente di sviluppo LAMP (Linux, Apache, MySQL, PHP) containerizzato, accessibile tramite GitHub Codespaces.

**Approccio Step-by-Step**: Partiamo da un container semplice e aggiungiamo funzionalità progressivamente.

## Competenze

✅ Creare repository GitHub  
✅ Configurare Dev Container base  
✅ Installare Apache e PHP  
✅ Creare applicazioni PHP  
✅ Aggiungere MySQL (opzionale)  
✅ Testare applicazioni web in Codespaces  

---

## Parte 1: Setup Repository GitHub

### Step 1.1: Crea Repository

1. Vai su [github.com](https://github.com) e accedi
2. Click su **New repository** (verde)
3. Compila:
   - **Repository name**: `lamp-stack-lab`
   - **Description**: "LAMP Stack with Docker and Git"
   - **Public** ✅
   - **Initialize with README** ✅
4. Click **Create repository**

### Step 1.2: Clone Repository (Opzionale Locale)

```bash
git clone https://github.com/TUO_USERNAME/lamp-stack-lab.git
cd lamp-stack-lab
```

> ⚠️ **Nota**: Puoi anche usare l'editor web GitHub (tasto `.` sul repository)

---

## Parte 2: Configurare Dev Container Semplice

### Step 2.1: Crea Struttura Cartelle

Direttamente su GitHub (crea file per creare cartelle):

```
lamp-stack-lab/
├── .devcontainer/
│   └── devcontainer.json
└── www/
    └── index.php
```

### Step 2.2: Crea `.devcontainer/devcontainer.json`

GitHub → **Add file** → **Create new file**  
Nome: `.devcontainer/devcontainer.json`

```json
{
  "name": "LAMP Stack Lab",
  "image": "php:8.2-apache",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "bmewburn.vscode-intelephense-client"
      ]
    }
  },
  
  "forwardPorts": [80],
  
  "postCreateCommand": "echo 'Container ready!'"
}
```

**Commit**: Messaggio "Add simple devcontainer config"

> **📝 Nota**: Usiamo l'immagine ufficiale `php:8.2-apache` che include già Apache e PHP configurati. Niente Docker Compose per iniziare - solo il minimo necessario!

---

## Parte 3: Configurare Docker Compose

### Step 3.1: `.devcontainer/docker-compose.yml`

```yaml
version: '3.8'

services:
  php-apache:
    build:
      context: ..
      dockerfile: .devcontainer/Dockerfile
    container_name: lamp-php-apache
    ports:
      - "80:80"
    volumes:
      - ../www:/var/www/html
    depends_on:
      - mysql
    networks:
      - lamp-network

  mysql:
    image: mysql:8.0
    container_name: lamp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: testdb
      MYSQL_USER: testuser
      MYSQL_PASSWORD: testpass
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ../database/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - lamp-network

  phpmyadmin:
    image: phpmyadmin/phpmyadmin:latest
    container_name: lamp-phpmyadmin
    environment:
      PMA_HOST: mysql
      PMA_PORT: 3306
      PMA_USER: root
      PMA_PASSWORD: rootpassword
    ports:
      - "8080:80"
    depends_on:
      - mysql
    networks:
      - lamp-network

volumes:
  mysql-data:

networks:
  lamp-network:
    driver: bridge
```

**Commit**: "Add docker-compose configuration"

---

## Parte 4: Creare Dockerfile per LAMP

### Step 4.1: `.devcontainer/Dockerfile`

```dockerfile
FROM php:8.2-apache

# Installa estensioni PHP necessarie
RUN docker-php-ext-install mysqli pdo pdo_mysql

# Abilita mod_rewrite di Apache
RUN a2enmod rewrite

# Installa Git e Docker
RUN apt-get update && apt-get install -y \
    git \
    curl \
    nano \
    vim \
    && rm -rf /var/lib/apt/lists/*

# Configura permessi
RUN chown -R www-data:www-data /var/www/html

# Esponi porta 80
EXPOSE 80

# Avvia Apache
CMD ["apache2-foreground"]
```

**Commit**: "Add Dockerfile for LAMP stack"

---

## Parte 5: Creare Database di Test

### Step 5.1: `database/init.sql`

```sql
-- Crea tabella utenti
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserisci dati di esempio
INSERT INTO users (username, email) VALUES
    ('mario_rossi', 'mario@example.com'),
    ('laura_bianchi', 'laura@example.com'),
    ('giuseppe_verdi', 'giuseppe@example.com');

-- Crea tabella prodotti
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0
);

-- Inserisci prodotti di esempio
INSERT INTO products (name, price, stock) VALUES
    ('Laptop Dell', 899.99, 15),
    ('Mouse Logitech', 29.99, 50),
    ('Tastiera Meccanica', 149.99, 25);
```

**Commit**: "Add database initialization script"

---

## Parte 6: Creare Applicazioni PHP

### Step 3.1: `www/index.php`

```php
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LAMP Stack Lab</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .container {
            background: rgba(255, 255, 255, 0.1);
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
        }
        h1 {
            text-align: center;
            margin-bottom: 30px;
        }
        .info-box {
            background: rgba(255, 255, 255, 0.2);
            padding: 15px;
            margin: 10px 0;
            border-radius: 5px;
        }
        .success {
            color: #00ff00;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 LAMP Stack Lab</h1>
        
        <div class="info-box">
            <h2>✅ Server Information</h2>
            <p><strong>Server Software:</strong> <?php echo $_SERVER['SERVER_SOFTWARE']; ?></p>
            <p><strong>PHP Version:</strong> <?php echo phpversion(); ?></p>
            <p><strong>Document Root:</strong> <?php echo $_SERVER['DOCUMENT_ROOT']; ?></p>
        </div>
        
        <div class="info-box">
            <h2>⏰ Current Time</h2>
            <p class="success"><?php echo date('Y-m-d H:i:s'); ?></p>
        </div>
        
        <div class="info-box">
            <h2>🎉 Successo!</h2>
            <p>Il tuo stack LAMP è configurato correttamente!</p>
        </div>
    </div>
</body>
</html>
```

**Commit**: "Add simple PHP homepage"

---

## Parte 4: Aprire in Codespaces

### Step 4.1: Crea Codespace

1. Vai sul tuo repository GitHub
2. Click su **Code** (verde)
3. Tab **Codespaces**
4. Click **Create codespace on main**
5. Attendi 2-3 minuti → VS Code si apre nel browser!

### Step 4.2: Verifica Installazioni

Nel terminal Codespace:

```bash
php --version      # PHP 8.2.x
apache2 -v         # Apache 2.4.x
```

✅ Apache e PHP installati automaticamente!

---

## Parte 5: Testare l'Applicazione

### Test Homepage PHP

Il Codespace avvia automaticamente Apache. VS Code mostra notifica: **"Porta 80 disponibile"** → Click per aprire

Nel browser: `https://CODESPACE-80.preview.app.github.dev`

Dovresti vedere:
- ✅ Informazioni del server Apache
- ✅ Versione PHP 8.2.x
- ✅ Data e ora corrente
- ✅ Messaggio di successo

---

---

## 🎯 FERMATI QUI - Versione Base Completata!

Se hai raggiunto questo punto con successo, hai completato la **versione base** dello stack LAMP!

✅ Checkpoint:
- Container con Apache e PHP funzionante
- Applicazione PHP visualizzata nel browser
- Codespace configurato correttamente

---

## 📦 PARTE OPZIONALE: Funzionalità Avanzate

Le sezioni seguenti sono **opzionali** e aggiungono funzionalità avanzate come MySQL, phpMyAdmin, Docker e Git.

Procedi solo se la versione base funziona perfettamente!

---

## OPZIONALE - Parte 6: Aggiungere MySQL

### Step 6.1: Aggiorna `devcontainer.json`

Modifica `.devcontainer/devcontainer.json` per usare Docker Compose:

```json
{
  "name": "LAMP Stack Lab - Full",
  "dockerComposeFile": "docker-compose.yml",
  "service": "php-apache",
  "workspaceFolder": "/var/www/html",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "bmewburn.vscode-intelephense-client"
      ]
    }
  },
  
  "forwardPorts": [80, 3306, 8080]
}
```

### Step 6.2: Crea `.devcontainer/docker-compose.yml`

```yaml
version: '3.8'

services:
  php-apache:
    image: php:8.2-apache
    container_name: lamp-php-apache
    ports:
      - "80:80"
    volumes:
      - ../www:/var/www/html
    depends_on:
      - mysql
    command: >
      bash -c "docker-php-ext-install mysqli pdo pdo_mysql 
      && apache2-foreground"

  mysql:
    image: mysql:8.0
    container_name: lamp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: testdb
      MYSQL_USER: testuser
      MYSQL_PASSWORD: testpass
    ports:
      - "3306:3306"

  phpmyadmin:
    image: phpmyadmin/phpmyadmin:latest
    container_name: lamp-phpmyadmin
    environment:
      PMA_HOST: mysql
    ports:
      - "8080:80"
    depends_on:
      - mysql
```

### Step 6.3: Ricostruisci il Codespace

1. In VS Code Codespace: `Cmd/Ctrl + Shift + P`
2. Digita: **"Codespaces: Rebuild Container"**
3. Attendi il rebuild (2-3 minuti)

### Step 6.4: Crea `www/database.php`

```php
<?php
$host = 'mysql';
$dbname = 'testdb';
$username = 'testuser';
$password = 'testpass';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    echo "<h1>✅ MySQL Connected!</h1>";
    echo "<p>Database: $dbname</p>";
} catch (PDOException $e) {
    echo "<h1>❌ Connection Failed</h1>";
    echo "<p>" . $e->getMessage() . "</p>";
}
?>
```

Testa su: `https://CODESPACE-80.preview.app.github.dev/database.php`

---

## OPZIONALE - Parte 7: Aggiungere Git e Docker

### Step 7.1: Crea Dockerfile Personalizzato

Crea `.devcontainer/Dockerfile`:

```dockerfile
FROM php:8.2-apache

# Installa estensioni PHP
RUN docker-php-ext-install mysqli pdo pdo_mysql

# Installa Git e Docker CLI
RUN apt-get update && apt-get install -y \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Abilita mod_rewrite
RUN a2enmod rewrite
```

### Step 7.2: Aggiorna docker-compose.yml

Cambia il servizio `php-apache`:

```yaml
  php-apache:
    build:
      context: .
      dockerfile: Dockerfile
    # resto invariato...
```

### Step 7.3: Test Git

```bash
git --version
git config --global user.name "Tuo Nome"
git config --global user.email "tua@email.com"
```

---

## ✅ Verifica Completamento - Versione Base

- [ ] Repository GitHub creato
- [ ] Dev Container configurato (`.devcontainer/devcontainer.json`)
- [ ] File `www/index.php` creato
- [ ] Codespace aperto e funzionante
- [ ] Homepage PHP visibile nel browser
- [ ] Apache e PHP verificati con comandi

## ✅ Verifica Completamento - Versione Avanzata (Opzionale)

- [ ] Docker Compose configurato
- [ ] MySQL container funzionante
- [ ] phpMyAdmin accessibile
- [ ] Git installato
- [ ] Connessione database testata

---

## 📸 Screenshot da Consegnare - Versione Base

1. Repository GitHub (mostra file structure con `.devcontainer/` e `www/`)
2. Codespace aperto (VS Code nel browser)
3. Terminal con `php --version` e `apache2 -v`
4. Browser con homepage PHP funzionante (index.php)

## 📸 Screenshot da Consegnare - Versione Avanzata (Opzionale)

5. Terminal con output di `docker-compose ps` (3 container attivi)
6. Browser con database.php (connessione MySQL riuscita)
7. Browser con phpMyAdmin aperto

---

## ⚠️ Troubleshooting

### Problema: Container MySQL non si avvia

**Causa**: Conflitto con MySQL già installato o porta 3306 occupata.

**Soluzione**:

```bash
# Vai nella cartella .devcontainer
cd .devcontainer

# Verifica porte in uso
docker-compose ps

# Ferma tutto e ricrea
docker-compose down -v
docker-compose up -d

# Verifica logs MySQL
docker-compose logs mysql
```

### Problema: Errore connessione database

**Causa**: Il container MySQL non ha finito l'inizializzazione.

**Soluzione**: Attendi 30-60 secondi dopo `docker-compose up` prima di accedere a database.php

```bash
# Verifica che MySQL sia ready (dalla cartella .devcontainer)
cd .devcontainer
docker-compose logs mysql | grep "ready for connections"
```

### Problema: "Access denied for user"

**Verifica credenziali** in `.devcontainer/docker-compose.yml` e `database.php`:

```yaml
# .devcontainer/docker-compose.yml
MYSQL_USER: testuser
MYSQL_PASSWORD: testpass
MYSQL_DATABASE: testdb
```

```php
// database.php
$username = 'testuser';
$password = 'testpass';
$dbname = 'testdb';
```

### Problema: Modifiche al codice non visibili

**Soluzione**: I volumi sono già configurati in `.devcontainer/docker-compose.yml`:

```yaml
volumes:
  - ../www:/var/www/html
```

Le modifiche ai file in `www/` sono immediatamente visibili. Se non funziona:

```bash
# Restart Apache (dalla cartella .devcontainer)
cd .devcontainer
docker-compose restart php-apache
```

### Problema: Port forwarding non funziona

1. **Verifica porte esposte** nel terminal:
   ```bash
   docker-compose ps
   ```

2. **Manual port forward** in VS Code:
   - Ports tab → Add Port → 80
   - Click sull'icona 🌐 per aprire nel browser

3. **Verifica che i servizi siano in ascolto**:
   ```bash
   cd .devcontainer
   docker-compose ps
   docker exec lamp-php-apache netstat -tlnp
   ```

### Problema: Docker not found nel container

**Causa**: Il postCreateCommand non è stato eseguito.

**Soluzione**:

```bash
# Manualmente nel terminal
apt-get update
apt-get install -y docker.io
docker --version
```

---

## 🎯 Prossimi Passi

Dopo aver completato la versione base:

1. ✅ Prova ad aggiungere una nuova pagina `www/test.php`
2. ✅ Modifica lo stile CSS di `index.php`
3. ✅ Quando tutto funziona, passa alla **Parte Opzionale** per aggiungere MySQL
4. ✅ Sperimenta con phpMyAdmin per gestire il database
5. ✅ Prova a creare una semplice applicazione CRUD (Create, Read, Update, Delete)

---

## 🎓 Concetti Appresi

### Versione Base:
1. **Dev Containers**: Ambienti di sviluppo containerizzati
2. **Apache**: Web server per servire applicazioni PHP
3. **PHP**: Linguaggio server-side per applicazioni web
4. **GitHub Codespaces**: Ambiente cloud per sviluppo
5. **Port Forwarding**: Accesso ai servizi da remoto

### Versione Avanzata (Opzionale):
6. **Docker Compose**: Orchestrazione multi-container
7. **MySQL**: Database relazionale
8. **Networking**: Comunicazione tra container
9. **phpMyAdmin**: Interfaccia web per MySQL

---

## 📚 Risorse Utili

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PHP Official Docker Images](https://hub.docker.com/_/php)
- [MySQL Docker Documentation](https://hub.docker.com/_/mysql)
- [Dev Containers Specification](https://containers.dev/)

---

## 🎯 Prossimi Passi

Completa gli esercizi successivi per:
- Aggiungere Redis per caching
- Implementare autenticazione utenti
- Creare una web app completa con CRUD
- Deploy su cloud provider
