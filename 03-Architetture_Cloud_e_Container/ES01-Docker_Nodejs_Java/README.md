# 01 — Docker Multi-Runtime: Node.js + Java

> **Materia**: Cloud Computing — Classe 5ª  
> **Parte**: 03 — Architetture Cloud e Container

---

## 📖 Guide Teoriche

| # | File | Argomento |
|---|------|-----------|
| 01 | [Docker Multi-Stage Build](docs/01_docker_multistage.md) | Multi-stage build, ottimizzazione immagini |
| 02 | [GitHub Codespaces](docs/02_github_codespaces.md) | Dev Container, configurazione ambiente cloud |
| 03 | [Node.js in Docker](docs/03_nodejs_docker.md) | Best practices per containerizzare app Node.js |
| 04 | [Java in Docker](docs/04_java_docker.md) | OpenJDK, Spring Boot, ottimizzazione JVM |

---

## 🏋️ Esercitazioni

### ES01 — Container Multi-Runtime con GitHub Codespaces

> Crea un ambiente di sviluppo cloud-based con Node.js e Java, containerizzato e versionato su GitHub.

| File | Descrizione |
|------|-------------|
| [README.md](README.md) | Introduzione, competenze, sequenza di studio |
| [docs/01_docker_multistage.md](docs/01_docker_multistage.md) | Multi-stage build per ottimizzare immagini |
| [docs/02_github_codespaces.md](docs/02_github_codespaces.md) | Setup Codespaces e Dev Container |
| [docs/03_nodejs_docker.md](docs/03_nodejs_docker.md) | Containerizzazione app Node.js/Express |
| [docs/04_java_docker.md](docs/04_java_docker.md) | Containerizzazione app Java/Spring Boot |
| [configs/devcontainer.json](configs/devcontainer.json) | Configurazione Dev Container per Codespaces |
| [configs/Dockerfile](configs/Dockerfile) | Dockerfile multi-runtime Node.js + Java |
| [configs/docker-compose.yml](configs/docker-compose.yml) | Orchestrazione container multi-servizio |
| [esempi/nodejs-app/](esempi/nodejs-app/) | App Node.js di esempio (Express REST API) |
| [esempi/java-app/](esempi/java-app/) | App Java di esempio (Spring Boot REST API) |
| [esercizio_a.md](esercizio_a.md) | 🔬 Lab guidato — Setup repository e Codespace |
| [esercizio_b.md](esercizio_b.md) | 🏗️ Progetto autonomo — API Gateway multi-runtime |
| [esercizio_c.md](esercizio_c.md) | 📖 Teoria — Domande su Docker, container, DevOps |
| [esercizio_d.md](esercizio_d.md) | 🚀 Progetto avanzato — Stack LAMP + Node.js + Nginx Proxy |
| [esercizio_e.md](esercizio_e.md) | 🔐 Progetto production — WireGuard VPN Server + SSL/TLS |

---

## 🗂️ Struttura Cartella

```
01-Docker_Nodejs_Java/
├── README.md                          ← Questo file
│
├── docs/                              ← Guide teoriche
│   ├── 01_docker_multistage.md
│   ├── 02_github_codespaces.md
│   ├── 03_nodejs_docker.md
│   └── 04_java_docker.md
│
├── configs/                           ← Configurazioni container
│   ├── devcontainer.json              ← GitHub Codespaces config
│   ├── Dockerfile                     ← Multi-runtime image
│   └── docker-compose.yml             ← Orchestrazione multi-servizio
│
├── esempi/                            ← Applicazioni di esempio
│   ├── nodejs-app/
│   │   ├── package.json
│   │   ├── server.js
│   │   └── Dockerfile
│   └── java-app/
│       ├── pom.xml
│       ├── src/main/java/...
│       └── Dockerfile
│
├── esercizio_a.md                     ← Lab: Setup base
├── esercizio_b.md                     ← Progetto: API Gateway
├── esercizio_c.md                     ← Teoria e verifica
├── esercizio_d.md                     ← Progetto avanzato: LAMP + Node.js stack
└── esercizio_e.md                     ← Progetto production: WireGuard VPN server
```

---

## 🔑 Concetti Chiave

| Concetto | Descrizione breve |
|----------|-------------------|
| **Docker** | Piattaforma per containerizzare applicazioni |
| **Dockerfile** | Ricetta per costruire immagini container |
| **Multi-stage build** | Tecnica per ridurre dimensione immagini (build stage + runtime stage) |
| **Dev Container** | Ambiente di sviluppo containerizzato (VS Code/Codespaces) |
| **GitHub Codespaces** | IDE cloud-based su container configurabili |
| **Node.js** | Runtime JavaScript server-side, ideale per microservizi |
| **Java/OpenJDK** | Linguaggio enterprise-grade per backend scalabili |
| **docker-compose** | Tool per orchestrare multi-container application |
| **Image layers** | Ogni comando Dockerfile crea un layer — cache per build veloci |
| **.dockerignore** | File per escludere file non necessari dall'immagine |

---

## 🎯 Competenze da Acquisire

✅ **Containerizzazione applicazioni** multi-runtime (Node.js + Java)  
✅ **GitHub Codespaces** per sviluppo cloud-based  
✅ **Docker best practices**: multi-stage, layer caching, security  
✅ **DevOps workflow**: versioning, CI/CD readiness  
✅ **Microservizi**: comunicazione inter-container, REST API  
✅ **Debugging** container: logs, exec, inspect  

---

## 📚 Sequenza di Studio Consigliata

1. **Teoria Docker** → Leggi [docs/01_docker_multistage.md](docs/01_docker_multistage.md)
2. **GitHub Codespaces** → Leggi [docs/02_github_codespaces.md](docs/02_github_codespaces.md)
3. **Lab Guidato** → Segui [esercizio_a.md](esercizio_a.md) passo-passo
4. **Approfondimenti** → Leggi [docs/03_nodejs_docker.md](docs/03_nodejs_docker.md) e [docs/04_java_docker.md](docs/04_java_docker.md)
5. **Progetto Autonomo** → Completa [esercizio_b.md](esercizio_b.md)
6. **Verifica** → Rispondi alle domande in [esercizio_c.md](esercizio_c.md)
7. **Progetto Avanzato** (opzionale) → [esercizio_d.md](esercizio_d.md) - LAMP Stack completo
8. **Progetto Production** (opzionale) → [esercizio_e.md](esercizio_e.md) - VPN Server con SSL/TLS

---

## 🚀 Quick Start

```bash
# 1. Clona il repository (sostituisci con il tuo)
git clone https://github.com/tuousername/cloud-computing-lab.git
cd cloud-computing-lab

# 2. Apri in GitHub Codespaces
# → Vai su GitHub > Code > Codespaces > Create codespace on main

# 3. Verifica installazioni
node --version   # v20.x
java --version   # OpenJDK 21

# 4. Testa app Node.js
cd esempi/nodejs-app
npm install
npm start
# → http://localhost:3000

# 5. Testa app Java
cd esempi/java-app
./mvnw spring-boot:run
# → http://localhost:8080
```

---

## 📦 Requisiti

- Account GitHub (gratuito)
- GitHub Codespaces (60h/mese gratuiti)
- Nessuna installazione locale richiesta!

_Alternative senza Codespaces:_
- Docker Desktop installato localmente
- VS Code + estensione "Dev Containers"

---

## 🔗 Risorse Utili

- [Docker Docs](https://docs.docker.com/)
- [GitHub Codespaces](https://github.com/features/codespaces)
- [Node.js Best Practices](https://github.com/goldbergyoni/nodebestpractices)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)

---

_Parte 03 — Architetture Cloud e Container | Cloud Computing 5ª_
