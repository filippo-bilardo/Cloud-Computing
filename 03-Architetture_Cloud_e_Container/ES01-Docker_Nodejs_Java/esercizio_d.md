# Esercizio LAMP: Ambiente di Sviluppo con Docker Compose e GitHub Codespaces

## Obiettivo

Realizzare un ambiente di sviluppo **LAMP** (Linux, Apache, MySQL, PHP) completo e riproducibile, orchestrato con **Docker Compose** e accessibile tramite **GitHub Codespaces**. Lo studente imparerà a:

- Configurare un **Dev Container** per GitHub Codespaces
- Orchestrare più container (web, database, phpMyAdmin) con **Docker Compose**
- Sviluppare e testare un'applicazione PHP con persistenza dei dati
- Utilizzare gli strumenti di sviluppo nel cloud

## Competenze

✅ Creare e gestire repository GitHub  
✅ Scrivere file `devcontainer.json` e `docker-compose.yml`  
✅ Comprendere il ruolo di Apache, PHP, MySQL in uno stack LAMP  
✅ Usare volumi Docker per la persistenza e lo sviluppo live  
✅ Esporre e inoltrare porte in Codespaces  
✅ Debug di container tramite log e comandi interattivi

## Durata stimata

**45-60 minuti** (configurazione iniziale) + tempo per personalizzazioni

## Prerequisiti teorici

### Cos'è lo stack LAMP?
LAMP è un acronimo che indica un insieme di tecnologie open source per eseguire applicazioni web dinamiche:
- **L**inux (sistema operativo)
- **A**pache (web server)
- **M**ySQL (database relazionale)
- **P**HP (linguaggio di scripting lato server)

Ogni componente è sostituibile (es. MariaDB al posto di MySQL, PostgreSQL, Nginx, Python/Perl) ma LAMP rimane lo stack più classico per lo sviluppo web.

### Perché Docker e Docker Compose?
- **Docker** containerizza ogni servizio, isolandoli ma facendoli comunicare via rete.
- **Docker Compose** permette di definire e avviare multi-container con un unico file YAML, specificando dipendenze, volumi, reti, variabili d'ambiente.
- Vantaggi: ambiente identico su qualsiasi macchina (locale, cloud, Codespaces), eliminazione del "funziona sul mio PC", facile condivisione con il team.

### GitHub Codespaces
Ambiente di sviluppo cloud basato su container. Un file `.devcontainer/devcontainer.json` dice a Codespaces come configurare l'ambiente (quali container avviare, quali estensioni VSCode installare, porte da esporre). Codespaces costruisce e avvia automaticamente i container definiti in `docker-compose.yml`.

---

## Parte 1: Setup del Repository GitHub

### Step 1.1: Creare un nuovo repository pubblico

1. Accedi a [GitHub](https://github.com)
2. Clicca su **New repository** (pulsante verde)
3. Compila:
   - **Repository name:** `lamp-codespaces-lab`
   - **Description:** "Ambiente LAMP con Docker Compose per Codespaces"
   - **Public** ✅
   - **Initialize this repository with a README** ✅
4. Clicca **Create repository**

### Step 1.2: Clonare localmente (opzionale) o usare l'editor web

Per modifiche rapide, puoi usare l'editor web di GitHub premendo il tasto `.` (punto) sulla tastiera mentre sei sul repository.  
In alternativa, clona in locale:

```bash
git clone https://github.com/TUO_USERNAME/lamp-codespaces-lab.git
cd lamp-codespaces-lab
```

---

## Parte 2: Configurare il Dev Container e Docker Compose

### Step 2.1: Creare la struttura delle cartelle

Crea le seguenti directory e file all'interno del repository:

```
lamp-codespaces-lab/
├── .devcontainer/
│   ├── devcontainer.json
│   └── docker-compose.yml
├── web/
│   └── index.php
└── README.md
```

### Step 2.2: Dev Container

Il file `.devcontainer/devcontainer.json` istruisce GitHub Codespaces su come costruire l'ambiente. Le proprietà principali:

- `"name"`: nome visualizzato in Codespaces
- `"dockerComposeFile"`: percorso del file docker-compose.yml da usare
- `"service"`: quale servizio del compose sarà il **workspace** (quello a cui si attacca VS Code)
- `"workspaceFolder"`: percorso dentro il container che verrà aperto come root del progetto
- `"remoteUser"`: utente con cui eseguire i comandi (qui `root` per evitare permessi)
- `"forwardPorts"`: porte che Codespaces deve automaticamente inoltrare al browser
- `"customizations.vscode.extensions"`: estensioni VS Code preinstallate

### Step 2.3: Creare `.devcontainer/devcontainer.json`

```json
{
    "name": "LAMP Stack",
    "dockerComposeFile": "docker-compose.yml",
    "service": "web",
    "workspaceFolder": "/workspace",
    "shutdownAction": "stopCompose",
    "remoteUser": "root",
    "customizations": {
        "vscode": {
            "extensions": [
                "bmewburn.vscode-intelephense-client",
                "xdebug.php-debug"
            ]
        }
    },
    "forwardPorts": [80, 8080]
}
```

**Note importanti:**
- `workspaceFolder` è impostato su `/workspace` perché questa cartella esiste sempre nei container di Codespaces (è il punto di mount del repository). In questo modo evitiamo l'errore "workspace does not exist".
- Il servizio `web` sarà il container principale a cui si attacca VS Code.
- Le porte 80 (Apache) e 8080 (phpMyAdmin) saranno automaticamente accessibili tramite URL generati da Codespaces.

### Step 2.4: Docker Compose

Il file `docker-compose.yml` definisce tre servizi:

1. **web** – container Apache+PHP. Usiamo l'immagine ufficiale `php:8.2-apache` e al momento dell'avvio eseguiamo comandi per installare le estensioni MySQL e abilitare mod_rewrite. Montiamo la cartella locale `./web` nella directory `/var/www/html` di Apache, così possiamo modificare i file PHP dal nostro editor e vederli immediatamente riflessi.
2. **db** – container MySQL 8.0. Impostiamo variabili d'ambiente per root password, database di default, utente e password. Un volume `db-data` garantisce che i dati persistano anche se il container viene ricreato.
3. **phpmyadmin** – interfaccia web per gestire MySQL. Collegato al servizio `db` tramite variabile `PMA_HOST`.

Le **reti** (`lamp-network`) permettono ai container di comunicare tra loro usando i nomi dei servizi come hostname (es. `db` è raggiungibile dal container `web`).

### Step 2.5: Creare `.devcontainer/docker-compose.yml`

```yaml
version: '3.8'

services:
  web:
    image: php:8.2-apache
    ports:
      - "80:80"
    volumes:
      - .:/workspace               # monta l'intero repo in /workspace per l'editor
      - ./web:/var/www/html        # monta la cartella web nella DocumentRoot di Apache
    command: >
      bash -c "docker-php-ext-install mysqli pdo pdo_mysql &&
               a2enmod rewrite &&
               apache2-foreground"
    depends_on:
      - db
    networks:
      - lamp-network

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: lamp_db
      MYSQL_USER: lamp_user
      MYSQL_PASSWORD: lamp_password
    ports:
      - "3306:3306"
    volumes:
      - db-data:/var/lib/mysql
    networks:
      - lamp-network

  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    ports:
      - "8080:80"
    environment:
      PMA_HOST: db
      PMA_PORT: 3306
    depends_on:
      - db
    networks:
      - lamp-network

volumes:
  db-data:

networks:
  lamp-network:
```

**Spiegazione comando `web`:**  
- `docker-php-ext-install` è uno script incluso nell'immagine ufficiale PHP che compila e abilita estensioni.
- `a2enmod rewrite` abilita il modulo mod_rewrite di Apache (utile per URL puliti).
- `apache2-foreground` avvia Apache in primo piano (il processo principale del container).

**Volumi:**
- Il primo volume `.:/workspace` è necessario affinché VS Code (che cerca `/workspace`) possa vedere i file del repository.
- Il secondo volume `./web:/var/www/html` sovrascrive la DocumentRoot di Apache con i nostri file PHP.

---

## Parte 3: Creare l'Applicazione PHP di Test

### Step 3.1: Connessione PHP a MySQL

All'interno del container `web`, PHP può connettersi al database usando l'hostname `db` (grazie alla rete Docker personalizzata). Utilizzeremo **PDO** (PHP Data Objects) per l'accesso al database, che è più sicuro e portabile rispetto alle funzioni mysql_*.

### Step 3.2: Creare `web/index.php`

```php
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>LAMP Stack su Codespaces</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
        .container { background: white; padding: 20px; border-radius: 8px; max-width: 800px; margin: auto; }
        h1 { color: #333; }
        .success { color: green; }
        .error { color: red; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
<div class="container">
    <h1>✅ Ambiente LAMP funzionante!</h1>
    <p>Apache + PHP + MySQL in esecuzione su GitHub Codespaces con Docker Compose.</p>
    
    <h2>Info PHP</h2>
    <p>Versione PHP: <strong><?php echo phpversion(); ?></strong></p>
    
    <h2>Test connessione MySQL</h2>
    <?php
    $host = 'db';
    $dbname = 'lamp_db';
    $user = 'lamp_user';
    $pass = 'lamp_password';
    
    try {
        $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        // Crea una tabella di esempio se non esiste
        $pdo->exec("CREATE TABLE IF NOT EXISTS test_table (
            id INT AUTO_INCREMENT PRIMARY KEY,
            messaggio VARCHAR(255) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )");
        
        // Inserisci un record di esempio solo se la tabella è vuota
        $stmt = $pdo->query("SELECT COUNT(*) FROM test_table");
        $count = $stmt->fetchColumn();
        if ($count == 0) {
            $pdo->exec("INSERT INTO test_table (messaggio) VALUES ('Connessione al database riuscita!')");
        }
        
        // Leggi e mostra i record
        $stmt = $pdo->query("SELECT id, messaggio, created_at FROM test_table ORDER BY id DESC");
        echo "<p class='success'>✅ Connessione a MySQL riuscita!</p>";
        echo "<h3>Record nella tabella 'test_table':</h3>";
        echo "<table><tr><th>ID</th><th>Messaggio</th><th>Data creazione</th></tr>";
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            echo "<tr><td>{$row['id']}</td><td>{$row['messaggio']}</td><td>{$row['created_at']}</td></tr>";
        }
        echo "</table>";
        
    } catch (PDOException $e) {
        echo "<p class='error'>❌ Errore di connessione: " . $e->getMessage() . "</p>";
    }
    ?>
    
    <h2>Informazioni di sistema</h2>
    <ul>
        <li><strong>Apache:</strong> <?php echo $_SERVER['SERVER_SOFTWARE']; ?></li>
        <li><strong>Indirizzo IP server:</strong> <?php echo $_SERVER['SERVER_ADDR']; ?></li>
        <li><strong>Document Root:</strong> <?php echo $_SERVER['DOCUMENT_ROOT']; ?></li>
    </ul>
    
    <p>🔧 phpMyAdmin disponibile sulla porta <strong>8080</strong> (utente: <code>lamp_user</code>, password: <code>lamp_password</code>)</p>
</div>
</body>
</html>
```

**Spiegazione del codice PHP:**
- `new PDO(...)` crea una connessione al database MySQL usando l'host `db`.
- La tabella `test_table` viene creata se non esiste.
- Viene inserito un messaggio di esempio solo la prima volta.
- I risultati vengono mostrati in una tabella HTML.

---

## Parte 4: Avviare e Testare in GitHub Codespaces

### Step 4.1: Creare un nuovo Codespace

1. Vai sul repository GitHub.
2. Clicca sul pulsante verde **Code**.
3. Seleziona il tab **Codespaces**.
4. Clicca su **Create codespace on main**.

**Cosa succede in background?**  
GitHub legge il file `.devcontainer/devcontainer.json`, esegue `docker-compose up -d` con il file specificato, costruisce i container (se non esistono), monta i volumi, avvia i servizi e infine attacca VS Code al container `web` nella cartella `/workspace`.

### Step 4.2: Verificare che tutto sia attivo

Dopo circa 1-2 minuti, VS Code si apre nel browser. Apri il terminale integrato (`Ctrl+` ` o `Cmd+``) ed esegui:

```bash
docker ps

mysql -h db -u root -prootpassword --ssl=0
```

Dovresti vedere tre container in esecuzione: `web`, `db`, `phpmyadmin`.

### Step 4.3: Testare l'applicazione web

- Nella sezione **Porte** di VS Code (in basso), vedrai la porta **80** (Apache) e **8080** (phpMyAdmin).  
- Clicca sull'icona a forma di 🌍 accanto alla porta 80 per aprire l'applicazione nel browser.

Vedrai la pagina PHP che conferma la connessione al database e mostra il record inserito.

### Step 4.4: Testare phpMyAdmin

Clicca sull'icona 🌍 della porta 8080. Si aprirà phpMyAdmin. Login con:
- **Utente:** `lamp_user`
- **Password:** `lamp_password`

Oppure come root: `root` / `rootpassword`. Potrai esplorare il database `lamp_db` e la tabella `test_table`.

### Step 4.5: Testare MySQL da terminale

```bash
docker exec -it lamp-codespaces-lab-db-1 mysql -u lamp_user -p
# Password: lamp_password
```

Poi esegui:
```sql
SHOW DATABASES;
USE lamp_db;
SHOW TABLES;
SELECT * FROM test_table;
EXIT;
```

---

## Parte 5: Comandi Utili per la Gestione

### Riavviare i servizi

```bash
# Riavvio solo del web server
docker-compose -f .devcontainer/docker-compose.yml restart web

# Riavvio di tutti i servizi
docker-compose -f .devcontainer/docker-compose.yml restart
```

### Visualizzare i log in tempo reale

```bash
docker-compose -f .devcontainer/docker-compose.yml logs -f
```

Per vedere solo i log di Apache:
```bash
docker-compose -f .devcontainer/docker-compose.yml logs web
```

### Entrare in una shell interattiva nel container web

```bash
docker exec -it lamp-codespaces-lab-web-1 bash
# Ora sei dentro il container; puoi fare ls /var/www/html, tail -f /var/log/apache2/error.log, etc.
```

### Fermare e avviare i container

```bash
docker-compose -f .devcontainer/docker-compose.yml stop
docker-compose -f .devcontainer/docker-compose.yml start
```

### Ricostruire le immagini dopo modifiche al Dockerfile (anche se qui non abbiamo Dockerfile personalizzato, se lo aggiungerai)

```bash
docker-compose -f .devcontainer/docker-compose.yml build --no-cache
docker-compose -f .devcontainer/docker-compose.yml up -d
```

---

## Parte 6: Approfondimenti Teorici Avanzati

### 6.1 Volumi Docker: tipi e utilizzo

Nel nostro `docker-compose.yml` usiamo due tipi di volumi:
- **Bind mount** (`./web:/var/www/html`): collega una directory del sistema host (il repository) a una directory del container. Le modifiche sono visibili su entrambi i lati. È ideale per lo sviluppo.
- **Volume nominato** (`db-data`): gestito interamente da Docker, persistente e isolato. Perfetto per i dati del database.

### 6.2 Variabili d'ambiente per MySQL

Le variabili `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` sono utilizzate dall'immagine ufficiale MySQL per inizializzare il database al primo avvio. Se il volume `db-data` esiste già, queste variabili vengono ignorate (i dati sono già presenti). Questo garantisce che ricreando il container senza cancellare il volume, i dati restano intatti.

### 6.3 Reti Docker personalizzate

Creando una rete dedicata (`lamp-network`), i container possono risolvere i nomi dei servizi come hostname. Ad esempio, dal container `web` possiamo usare `db` come indirizzo per connetterci a MySQL, senza bisogno di conoscere l'IP. Inoltre, i container sulla stessa rete possono comunicare tra loro, ma non con container esterni a meno che non si espongano le porte.

### 6.4 Differenza tra `CMD`, `RUN` e `command` in Docker

- `RUN` viene eseguito durante la build dell'immagine.
- `CMD` definisce il comando di default quando il container parte.
- Nel nostro `docker-compose.yml` usiamo `command:` per sovrascrivere il comando di default dell'immagine `php:apache`. In questo modo possiamo eseguire comandi di setup (installazione estensioni, abilitazione moduli) prima di avviare Apache.

---

## Parte 7: Personalizzazione e Estensioni

### Aggiungere un nuovo file PHP

Crea `web/info.php` con:
```php
<?php phpinfo(); ?>
```
Accedi a `http://localhost:80/info.php` per vedere la configurazione completa di PHP.

### Modificare la configurazione di Apache

Puoi aggiungere un file di configurazione personalizzato montandolo come volume. Ad esempio, crea `web/.htaccess` o aggiungi nel `docker-compose.yml`:

```yaml
volumes:
  - ./apache-config.conf:/etc/apache2/sites-available/000-default.conf
```

### Installare estensioni PHP aggiuntive

Modifica il `command` nel servizio `web` per includere altre estensioni, ad esempio:
```yaml
command: >
  bash -c "docker-php-ext-install mysqli pdo pdo_mysql gd zip &&
           a2enmod rewrite &&
           apache2-foreground"
```

---

## Verifica Finale delle Competenze

- [ ] Repository GitHub creato con la struttura corretta
- [ ] File `.devcontainer/devcontainer.json` e `docker-compose.yml` presenti
- [ ] Container `web`, `db`, `phpmyadmin` in esecuzione
- [ ] Pagina `index.php` accessibile e mostra la connessione a MySQL riuscita
- [ ] phpMyAdmin accessibile e permette di navigare nel database
- [ ] È possibile eseguire comandi SQL da terminale
- [ ] Modificando `index.php`, la pagina si aggiorna senza riavviare i container

## Screenshot da Includere nella Consegna

1. **Repository su GitHub** con l'elenco dei file (struttura ad albero)
2. **Codespaces aperto** con VS Code nel browser
3. **Browser con la pagina `index.php`** (mostrare il messaggio di successo e la tabella)
4. **Browser con phpMyAdmin** (mostrare il database `lamp_db` e la tabella)
5. **Terminale** con il comando `docker ps` che mostra i tre container attivi

## Troubleshooting Avanzato

### Problema: La porta 80 non si apre automaticamente
**Soluzione:** Nella vista "Porte" di VS Code, clicca su "Aggiungi porta" e inserisci 80. Poi fai clic sull'icona del mondo.

### Problema: "Connection refused" da PHP a MySQL
**Causa:** MySQL potrebbe non essere ancora pronto quando PHP prova a connettersi.  
**Soluzione:** Aggiungi nel codice PHP un ritardo o usa uno script di attesa. Per semplicità, ricarica la pagina dopo qualche secondo.

### Problema: Modifiche ai file PHP non si vedono nel browser
**Causa:** Potrebbe essere la cache del browser o il volume non montato correttamente.  
**Soluzione:** Controlla che il file sia stato salvato e che il volume `./web:/var/www/html` sia attivo (`docker inspect lamp-codespaces-lab-web-1`).

### Problema: Codespaces si blocca in "Starting"
**Soluzione:** Cancella il Codespace e ricrealo. A volte GitHub ha rallentamenti. Se persiste, verifica che il file `docker-compose.yml` sia valido (indentazione YAML corretta).

## Riferimenti e Risorse

- [Documentazione ufficiale Docker Compose](https://docs.docker.com/compose/)
- [Immagine Docker ufficiale PHP](https://hub.docker.com/_/php)
- [Immagine Docker ufficiale MySQL](https://hub.docker.com/_/mysql)
- [Guida di GitHub Codespaces](https://docs.github.com/en/codespaces)
- [PHP PDO Tutorial](https://www.php.net/manual/en/book.pdo.php)

## Conclusione

Hai realizzato un ambiente di sviluppo LAMP completo e riproducibile, sfruttando le tecnologie container e il cloud. Questo setup può essere utilizzato per qualsiasi progetto PHP/MySQL, condiviso con il team e persino portato in produzione con piccoli adattamenti. Ora puoi sviluppare applicazioni web complesse senza preoccuparti dell'ambiente locale. Buon coding!