# 🔬 Esercizio A: Setup Repository e GitHub Codespace

## Obiettivo

Creare un repository GitHub con ambiente di sviluppo containerizzato (Dev Container) per Node.js e Java, accessibile tramite GitHub Codespaces.

## Competenze

✅ Creare repository GitHub  
✅ Configurare Dev Container  
✅ Testare applicazioni in Codespaces  
✅ Fare commit e push  

---

## Parte 1: Setup Repository GitHub

### Step 1.1: Crea Repository

1. Vai su [github.com](https://github.com) e accedi
2. Click su **New repository** (verde)
3. Compila:
   - **Repository name**: `cloud-computing-lab`
   - **Description**: "Node.js + Java Docker lab"
   - **Public** ✅
   - **Initialize with README** ✅
4. Click **Create repository**

### Step 1.2: Clone Repository (Opzionale Locale)

```bash
git clone https://github.com/TUO_USERNAME/cloud-computing-lab.git
cd cloud-computing-lab
```

> ⚠️ **Nota**: Puoi anche usare l'editor web GitHub (tasto `.` sul repository)

---

## Parte 2: Configurare Dev Container

### Step 2.1: Crea Struttura Cartelle

Direttamente su GitHub (crea file per creare cartelle):

```
cloud-computing-lab/
├── .devcontainer/
│   └── devcontainer.json
├── nodejs-app/
│   ├── package.json
│   ├── server.js
│   └── Dockerfile
└── java-app/
    ├── pom.xml
    ├── src/main/java/com/example/app/Application.java
    └── Dockerfile
```

### Step 2.2: Crea `.devcontainer/devcontainer.json`

GitHub → **Add file** → **Create new file**  
Nome: `.devcontainer/devcontainer.json`

```json
{
  "name": "Node.js + Java Lab",
  "image": "mcr.microsoft.com/devcontainers/universal:2",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "dbaeumer.vscode-eslint",
        "redhat.java",
        "ms-azuretools.vscode-docker"
      ]
    }
  },
  
  "forwardPorts": [3000, 8080]
}
```

**Commit**: Messaggio "Add devcontainer config"

> **📝 Nota**: L'immagine `universal:2` include già Node.js (v20) e Java (OpenJDK 21) pre-installati, quindi non servono features aggiuntive. Questo accelera il setup del Codespace!

---

## Parte 3: Creare App Node.js

### Step 3.1: `nodejs-app/package.json`

```json
{
  "name": "nodejs-api",
  "version": "1.0.0",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2"
  }
}
```

### Step 3.2: `nodejs-app/server.js`

```javascript
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.json({ 
    message: 'Hello from Node.js!',
    service: 'nodejs-api'
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`✅ Node.js app running on port ${PORT}`);
});
```

### Step 3.3: `nodejs-app/Dockerfile`

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

**Commit**: "Add Node.js app"

---

## Parte 4: Creare App Java

### Step 4.1: `java-app/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>java-api</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 4.2: `java-app/src/main/java/com/example/app/Application.java`

```java
package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class HelloController {
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "message", "Hello from Java!",
            "service", "java-api"
        );
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
```

### Step 4.3: `java-app/Dockerfile`

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

**Commit**: "Add Java app"

---

## Parte 5: Aprire in Codespaces

### Step 5.1: Crea Codespace

1. Vai sul tuo repository GitHub
2. Click su **Code** (verde)
3. Tab **Codespaces**
4. Click **Create codespace on main**
5. Attendi 1-2 minuti → VS Code si apre nel browser!

### Step 5.2: Verifica Installazioni

Nel terminal Codespace:

```bash
node --version   # v20.x
npm --version    # 10.x
java --version   # OpenJDK 21
mvn --version    # Maven 3.9.x
docker --version # Docker 24.x
```

✅ Tutto installato automaticamente!

---

## Parte 6: Testare le App

### Test Node.js

```bash
cd nodejs-app
npm install
npm start
```

VS Code mostra notifica: **"Porta 3000 disponibile"** → Click per aprire

Nel browser: `https://CODESPACE-3000.preview.app.github.dev`

Output:
```json
{
  "message": "Hello from Node.js!",
  "service": "nodejs-api"
}
```

**Ctrl+C** per fermare

### Test Java

```bash
cd ../java-app
mvn spring-boot:run
```

Attendi startup (~20s). Notifica: **"Porta 8080 disponibile"**

Nel browser: `https://CODESPACE-8080.preview.app.github.dev`

Output:
```json
{
  "message": "Hello from Java!",
  "service": "java-api"
}
```

**Ctrl+C** per fermare

---

## Parte 7: Docker Build (Avanzato)

### Build Node.js Image

```bash
cd nodejs-app
docker build -t nodejs-api:1.0 .
docker run -d -p 3001:3000 --name nodejs-test nodejs-api:1.0
curl http://localhost:3001
docker stop nodejs-test && docker rm nodejs-test
```

### Build Java Image

```bash
cd ../java-app
docker build -t java-api:1.0 .
docker run -d -p 8081:8080 --name java-test java-api:1.0
# Attendi 30s per startup
curl http://localhost:8081
docker stop java-test && docker rm java-test
```

---

## ✅ Verifica Completamento

- [ ] Repository GitHub creato
- [ ] Dev Container configurato (`.devcontainer/devcontainer.json`)
- [ ] App Node.js funzionante
- [ ] App Java funzionante
- [ ] Codespace aperto e testato
- [ ] Commit e push effettuati
- [ ] Docker build testato (opzionale)

---

## 📸 Screenshot da Consegnare

1. Repository GitHub (mostra file structure)
2. Codespace aperto (VS Code nel browser)
3. Terminal con `node --version` e `java --version`
4. Browser con output Node.js API
5. Browser con output Java API

---

## ⚠️ Troubleshooting

### Problema: "Recovery mode due to configuration error"

**Causa**: Il devcontainer.json ha errori di sintassi o features non disponibili.

**Soluzione**:

1. **Usa la configurazione semplificata** (già nell'esercizio):
   ```json
   {
     "name": "Node.js + Java Lab",
     "image": "mcr.microsoft.com/devcontainers/universal:2",
     "customizations": {
       "vscode": {
         "extensions": [
           "dbaeumer.vscode-eslint",
           "redhat.java",
           "ms-azuretools.vscode-docker"
         ]
       }
     },
     "forwardPorts": [3000, 8080]
   }
   ```

2. **Ricostruisci il container**:
   - In Codespaces: `Cmd/Ctrl + Shift + P` → **"Codespaces: Rebuild Container"**
   - Oppure: Click su **"Rebuild Container"** nella notifica di errore

3. **Verifica il file**:
   - Controlla che `.devcontainer/devcontainer.json` esista
   - Verifica che il JSON sia valido (usa [jsonlint.com](https://jsonlint.com/))
   - Rimuovi features non necessarie

### Problema: `./mvnw: No such file or directory`

**Causa**: Il progetto non ha il Maven Wrapper configurato.

**Soluzione**: Usa direttamente `mvn` invece di `./mvnw`:

```bash
cd java-app
mvn spring-boot:run
```

Il Codespace ha già Maven installato globalmente (`mvn --version`), quindi non serve il wrapper.

**Alternativa - Aggiungere Maven Wrapper** (opzionale):
```bash
cd java-app
mvn wrapper:wrapper
./mvnw spring-boot:run  # Ora funziona!
```

### Problema: Node.js o Java non trovati

**Verifica versioni**:
```bash
node --version    # Deve mostrare v20.x
npm --version     # Deve mostrare 10.x
java --version    # Deve mostrare OpenJDK 21
mvn --version     # Deve mostrare Maven 3.x
```

Se mancanti, l'immagine `universal:2` include già tutto! Probabilmente il Codespace non ha finito il setup.

### Problema: Port forwarding non funziona

1. **Check ports exposed**:
   ```bash
   # Nel terminal Codespace
   cd nodejs-app
   npm start
   ```

2. **Guarda le notifiche** VS Code in basso a destra: "Port 3000 is available"

3. **Manual port forward**:
   - Ports tab → Add Port → 3000
   - Click sull'icona 🌐 per aprire nel browser

---

## 🎯 Prossimi Passi

Completa **Esercizio B** per creare un API Gateway che orchestra Node.js e Java!
