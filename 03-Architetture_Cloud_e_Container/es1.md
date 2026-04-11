# 🧪 Lab 1: GitHub Codespace Multi-Language (Node.js + Java)

> **Durata stimata**: 45-60 minuti  
> **Livello**: Principiante  
> **Obiettivo**: Configurare un ambiente di sviluppo cloud-ready con GitHub Codespaces, supportando contemporaneamente Node.js e Java.

---

## 📋 Obiettivi di Apprendimento

Al termine di questo lab, lo studente sarà in grado di:
- ✅ Creare un repository GitHub e abilitare GitHub Codespaces
- ✅ Configurare un ambiente di sviluppo containerizzato con `.devcontainer`
- ✅ Installare e configurare runtime multipli (Node.js + Java) nello stesso Codespace
- ✅ Eseguire e testare applicazioni in entrambi i linguaggi
- ✅ Comprendere i vantaggi dello sviluppo cloud-native

---

## 🛠️ Prerequisiti

- Account GitHub attivo
- Browser moderno (Chrome, Firefox, Edge)
- Conoscenza base di terminale e Git
- (Opzionale) GitHub CLI installata localmente

---

## 🚀 Guida Passo-Passo

### 🔹 Step 1: Creare il Repository GitHub

1. Accedi a [github.com](https://github.com)
2. Clicca sul pulsante **"+"** in alto a destra → **"New repository"**
3. Compila i campi:
   - **Repository name**: `cloud-lab-codespace`
   - **Description**: `Ambiente Codespace multi-language per Cloud Computing Lab`
   - **Public/Private**: Scegli in base alle policy del corso
   - ✅ **"Add a README file"**
4. Clicca **"Create repository"**

---

### 🔹 Step 2: Abilitare GitHub Codespaces

1. Nel repository appena creato, vai su **Settings** → **Codespaces**
2. Verifica che **"Allow users to create codespaces"** sia abilitato
3. (Opzionale) Configura le policy di retention se necessario

---

### 🔹 Step 3: Configurare l'Ambiente con `.devcontainer`

Creiamo la cartella di configurazione per Codespaces:

```bash
# Nel repository, crea questa struttura:
.devcontainer/
├── devcontainer.json
└── Dockerfile
```

#### 📄 File: `.devcontainer/Dockerfile`

```dockerfile
# Immagine base con Ubuntu
FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Installa Node.js (LTS)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Installa Java 17 (OpenJDK)
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
    apt-get install -y openjdk-17-jdk maven && \
    rm -rf /var/lib/apt/lists/*

# Configura variabili d'ambiente
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# Installa tool utili
RUN npm install -g nodemon typescript ts-node

# Utente non-root (best practice security)
USER vscode
```

#### 📄 File: `.devcontainer/devcontainer.json`

```json
{
  "name": "Cloud Lab - Node.js + Java",
  "build": {
    "dockerfile": "Dockerfile",
    "context": ".."
  },
  "features": {
    "ghcr.io/devcontainers/features/git:1": {},
    "ghcr.io/devcontainers/features/github-cli:1": {}
  },
  "customizations": {
    "vscode": {
      "extensions": [
        "dbaeumer.vscode-eslint",
        "vscjava.vscode-java-pack",
        "vmware.vscode-boot-dev-pack",
        "esbenp.prettier-vscode",
        "ms-vscode.vscode-node-azure-pack"
      ],
      "settings": {
        "terminal.integrated.defaultProfile.linux": "bash",
        "java.configuration.updateBuildConfiguration": "automatic"
      }
    }
  },
  "forwardPorts": [3000, 8080],
  "postCreateCommand": "echo '✅ Codespace pronto! Node: ' && node -v && echo '✅ Java: ' && java -version",
  "remoteUser": "vscode"
}
```

> 💡 **Spiegazione chiave**:  
> - `forwardPorts`: espone automaticamente le porte per app web  
> - `postCreateCommand`: esegue comandi dopo la creazione per verificare l'ambiente  
> - `extensions`: pre-installa estensioni VS Code per migliorare l'esperienza

---

### 🔹 Step 4: Creare il Codespace

1. Nel repository GitHub, clicca sul pulsante verde **"<> Code"**
2. Seleziona la tab **"Codespaces"**
3. Clicca **"Create codespace on main"**
4. Attendi 2-4 minuti: GitHub costruirà il container con la tua configurazione

🎉 **Risultato**: Si aprirà un editor VS Code nel browser, connesso al tuo container cloud!

---

### 🔹 Step 5: Verificare l'Ambiente

Apri il terminale integrato (`Ctrl + ù` o **Terminal → New Terminal**) ed esegui:

```bash
# Verifica Node.js
node --version
npm --version

# Verifica Java
java -version
javac -version
mvn --version

# Verifica strumenti aggiuntivi
git --version
gh --version
```

✅ Se tutti i comandi restituiscono versioni, l'ambiente è pronto!

---

## 🧪 Esempi da Testare

### 🟢 Esempio 1: Hello World Node.js

#### 1. Crea la struttura:
```bash
mkdir -p examples/nodejs-hello
cd examples/nodejs-hello
npm init -y
```

#### 2. Crea `index.js`:
```javascript
// examples/nodejs-hello/index.js
const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.get('/', (req, res) => {
  res.json({
    message: '🚀 Hello from Node.js in GitHub Codespace!',
    timestamp: new Date().toISOString(),
    env: process.env.NODE_ENV || 'development'
  });
});

app.get('/health', (req, res) => {
  res.status(200).json({ status: 'OK' });
});

app.listen(PORT, () => {
  console.log(`✅ Server Node.js attivo su http://localhost:${PORT}`);
});
```

#### 3. Installa dipendenze e avvia:
```bash
npm install express
node index.js
```

#### 4. Testa l'app:
- Clicca sul popup **"Open in Browser"** sulla porta 3000
- Oppure: `curl http://localhost:3000`

📋 **Output atteso**:
```json
{
  "message": "🚀 Hello from Node.js in GitHub Codespace!",
  "timestamp": "2024-04-12T10:30:00.000Z",
  "env": "development"
}
```

---

### 🔵 Esempio 2: Hello World Java (Spring Boot)

#### 1. Crea il progetto Maven:
```bash
mkdir -p examples/java-hello
cd examples/java-hello
mvn archetype:generate \
  -DgroupId=com.cloudlab \
  -DartifactId=java-hello \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
cd java-hello
```

#### 2. Modifica `pom.xml` (aggiungi Spring Boot):
```xml
<!-- Dentro <project> → <dependencies> -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <version>3.2.4</version>
</dependency>
```

#### 3. Crea `src/main/java/com/cloudlab/Application.java`:
```java
package com.cloudlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public HelloResponse hello() {
        return new HelloResponse(
            "☕ Hello from Java in GitHub Codespace!",
            java.time.Instant.now().toString(),
            System.getenv("NODE_ENV") != null ? "cloud" : "local"
        );
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}

// Record per le risposte JSON (Java 17+)
record HelloResponse(String message, String timestamp, String environment) {}
record HealthResponse(String status) {}
```

#### 4. Avvia l'applicazione:
```bash
mvn spring-boot:run
```

#### 5. Testa l'app:
- Porta predefinita: **8080**
- `curl http://localhost:8080`

📋 **Output atteso**:
```json
{
  "message": "☕ Hello from Java in GitHub Codespace!",
  "timestamp": "2024-04-12T10:35:00.00Z",
  "environment": "local"
}
```

---

### 🟣 Esempio 3: Interazione Cross-Language (Bonus)

Crea un endpoint Node.js che chiama il servizio Java:

#### `examples/nodejs-hello/proxy.js`
```javascript
const express = require('express');
const http = require('http');
const app = express();

const JAVA_SERVICE = process.env.JAVA_SERVICE_URL || 'http://localhost:8080';

app.get('/api/java-hello', (req, res) => {
  http.get(JAVA_SERVICE, (javaRes) => {
    let data = '';
    javaRes.on('data', chunk => data += chunk);
    javaRes.on('end', () => {
      res.json({
        source: 'nodejs-proxy',
        javaResponse: JSON.parse(data)
      });
    });
  }).on('error', (err) => {
    res.status(502).json({ error: 'Java service unreachable', details: err.message });
  });
});

app.listen(3001, () => {
  console.log('🔗 Proxy attivo su http://localhost:3001');
});
```

#### Test:
```bash
# In un nuovo terminale:
node examples/nodejs-hello/proxy.js

# Poi:
curl http://localhost:3001/api/java-hello
```

---

## 🔍 Troubleshooting

| Problema | Soluzione |
|----------|-----------|
| ❌ Codespace non si avvia | Verifica che `.devcontainer/` sia nella root e i file siano validi JSON |
| ❌ Porta non accessibile | Controlla che sia in `forwardPorts` e clicca "Open in Browser" |
| ❌ Java non trovato | Esegui `echo $JAVA_HOME` e verifica il PATH nel Dockerfile |
| ❌ npm install lento | Codespaces ha cache: riprova o usa `npm ci` |
| ❌ Permessi denied | Assicurati che il Dockerfile imposti `USER vscode` |

**Comandi utili di debug**:
```bash
# Ricostruire il Codespace
# Pannello Command Palette (Ctrl+Shift+P) → "Codespaces: Rebuild Container"

# Verificare variabili d'ambiente
printenv | grep -E "JAVA|NODE|PORT"

# Testare connettività di rete
curl -v http://localhost:8080/health
```

---

## 📝 Consegna del Lab

Per completare l'esercitazione, lo studente deve:

1. ✅ Pushare il repository con la cartella `.devcontainer/` configurata
2. ✅ Fornire il link al Codespace attivo (o screenshot dell'ambiente)
3. ✅ Eseguire almeno 2 dei 3 esempi e mostrare l'output
4. ✅ Rispondere a una domanda riflessiva:
   > *"Quali vantaggi offre lo sviluppo in Codespace rispetto a un ambiente locale per un team distribuito?"*

---

## 🎯 Criteri di Valutazione

| Criterio | Punti | Descrizione |
|----------|-------|-------------|
| Configurazione `.devcontainer` | 30% | Dockerfile e JSON validi e funzionali |
| Ambiente multi-language | 25% | Node.js e Java funzionanti contemporaneamente |
| Esempi eseguiti | 25% | Almeno 2 esempi testati con output documentato |
| Documentazione | 10% | README chiaro con istruzioni di avvio |
| Riflessione | 10% | Risposta pertinente alla domanda finale |

---

## 📚 Risorse Aggiuntive

- [Documentazione ufficiale GitHub Codespaces](https://docs.github.com/en/codespaces)
- [Dev Container Specification](https://containers.dev)
- [GitHub Codespaces Features](https://github.com/devcontainers/features)
- [Esempi di devcontainer](https://github.com/devcontainers/templates)

---

> 💡 **Pro Tip per il docente**:  
> Prepara un repository "template" con la configurazione già pronta, così gli studenti possono partire subito con `Use this template` → ridurre i tempi di setup del 70%!
