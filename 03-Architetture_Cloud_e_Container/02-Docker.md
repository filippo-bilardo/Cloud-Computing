# Docker: La Piattaforma di Containerizzazione

## Introduzione a Docker

Docker è una piattaforma open-source che automatizza il deployment, la scalabilità e la gestione di applicazioni all'interno di container software. Lanciato nel 2013, Docker ha rivoluzionato il modo in cui le applicazioni vengono sviluppate e distribuite.

### Cos'è Docker?

**Docker** è:
- Una piattaforma per sviluppare, spedire ed eseguire applicazioni
- Un insieme di strumenti per gestire container
- Un ecosistema completo per la containerizzazione
- Uno standard de facto per i container

### Filosofia di Docker

**"Build, Ship, Run"**
1. **Build**: Crea immagini delle applicazioni
2. **Ship**: Distribuisci tramite registry
3. **Run**: Esegui ovunque in modo consistente

## Architettura di Docker

### Componenti Principali

```
┌────────────────────────────────────────────────┐
│              Docker Client (CLI)               │
│                docker commands                 │
└───────────────────┬────────────────────────────┘
                    │ REST API
┌───────────────────▼────────────────────────────┐
│            Docker Daemon (dockerd)             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Images   │  │Container │  │ Networks │      │
│  │ Manager  │  │ Manager  │  │ Manager  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
└───────────────────┬────────────────────────────┘
                    │
┌───────────────────▼────────────────────────────┐
│              containerd                        │
│         Container Runtime (OCI)                │
└───────────────────┬────────────────────────────┘
                    │
┌───────────────────▼────────────────────────────┐
│                runc                            │
│         Low-level Runtime                      │
└────────────────────────────────────────────────┘
```

### 1. Docker Client

Il **Docker Client** (`docker`) è l'interfaccia principale per interagire con Docker:
- Interpreta i comandi dell'utente
- Comunica con il Docker Daemon via REST API
- Può connettersi a daemon remoti

### 2. Docker Daemon

Il **Docker Daemon** (`dockerd`) è il processo server che:
- Gestisce oggetti Docker (immagini, container, reti, volumi)
- Ascolta le richieste API
- Comunica con altri daemon
- Delega l'esecuzione a containerd

### 3. containerd

**containerd** è il runtime di livello intermedio che:
- Gestisce il ciclo di vita dei container
- Pull e push delle immagini
- Storage e networking
- Integrazione con runc

### 4. runc

**runc** è il runtime OCI-compliant che:
- Crea e avvia container
- Implementa le specifiche OCI
- Gestisce namespaces e cgroups

## Docker Images

### Struttura delle Immagini

Le immagini Docker sono composte da **layer** read-only sovrapposti:

```
┌─────────────────────────────────────┐
│  Container Layer (Read-Write)       │ ← Layer modificabile
├─────────────────────────────────────┤
│  Layer 4: COPY app.py /app/         │
├─────────────────────────────────────┤
│  Layer 3: RUN pip install flask     │
├─────────────────────────────────────┤
│  Layer 2: RUN apt-get update        │
├─────────────────────────────────────┤
│  Layer 1: Base image (python:3.9)   │
└─────────────────────────────────────┘
```

### Copy-on-Write (CoW)

- I layer sono condivisi tra container
- Le modifiche creano nuovi layer
- Ottimizzazione dello spazio disco
- Caching efficiente

### Image Naming

```
[registry]/[repository]:[tag]
docker.io/library/nginx:1.21-alpine
│           │       │     │
│           │       │     └─ Tag (versione)
│           │       └─────── Repository (nome immagine)
│           └─────────────── Namespace (utente/org)
└─────────────────────────── Registry
```

## Dockerfile: Building Images

### Struttura Base

```dockerfile
# Immagine base
FROM python:3.9-slim

# Metadata
LABEL maintainer="team@example.com"
LABEL version="1.0"
LABEL description="My Python Application"

# Variabili di build
ARG APP_VERSION=1.0.0
ARG BUILD_DATE

# Variabili d'ambiente
ENV APP_HOME=/app \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# Working directory
WORKDIR ${APP_HOME}

# Installazione dipendenze di sistema
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        gcc \
        libc-dev && \
    rm -rf /var/lib/apt/lists/*

# Copia file dipendenze
COPY requirements.txt .

# Installazione dipendenze Python
RUN pip install --no-cache-dir -r requirements.txt

# Copia codice applicazione
COPY . .

# Crea utente non-privilegiato
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser ${APP_HOME}

# Cambia utente
USER appuser

# Esponi porta
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Volume per dati persistenti
VOLUME ["/app/data"]

# Comando di default
CMD ["python", "app.py"]
```

### Istruzioni Dockerfile

#### FROM
```dockerfile
# Immagine base specifica
FROM ubuntu:20.04

# Multi-stage: builder
FROM golang:1.19 AS builder

# Immagine scratch (vuota)
FROM scratch
```

#### RUN
```dockerfile
# Esecuzione shell
RUN apt-get update && apt-get install -y nginx

# Exec form (preferita)
RUN ["apt-get", "update"]

# Multi-line con backslash
RUN apt-get update && \
    apt-get install -y \
        package1 \
        package2 && \
    apt-get clean
```

#### COPY vs ADD
```dockerfile
# COPY: semplice copia (preferita)
COPY src/ /app/
COPY package*.json ./

# ADD: con funzionalità extra
ADD https://example.com/file.tar.gz /tmp/  # Download URL
ADD archive.tar.gz /opt/                    # Auto-estrazione
```

#### CMD vs ENTRYPOINT
```dockerfile
# CMD: comando di default (sovrascrivibile)
CMD ["nginx", "-g", "daemon off;"]
CMD echo "Hello World"

# ENTRYPOINT: comando fisso
ENTRYPOINT ["python"]
CMD ["app.py"]  # Argomenti di default per ENTRYPOINT

# Combinazione
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
```

#### ENV e ARG
```dockerfile
# ARG: solo durante build
ARG VERSION=1.0
ARG BUILD_DATE

# ENV: disponibile anche al runtime
ENV APP_VERSION=${VERSION}
ENV NODE_ENV=production

# Uso
RUN echo "Building version ${VERSION}"
```

#### WORKDIR
```dockerfile
# Imposta directory di lavoro
WORKDIR /app

# Relativa alla precedente
WORKDIR /app
WORKDIR src  # Ora in /app/src
```

#### USER
```dockerfile
# Esegui come utente specifico
USER node
USER 1000:1000
```

#### EXPOSE
```dockerfile
# Documenta porte (non apre effettivamente)
EXPOSE 80
EXPOSE 443
EXPOSE 8080/tcp
EXPOSE 8081/udp
```

#### VOLUME
```dockerfile
# Dichiara volume
VOLUME ["/data"]
VOLUME /var/log /var/db
```

#### HEALTHCHECK
```dockerfile
# Check salute container
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost/ || exit 1

# Disabilita healthcheck
HEALTHCHECK NONE
```

### Multi-Stage Builds

```dockerfile
# Stage 1: Build
FROM node:16 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# Stage 2: Test
FROM builder AS tester
RUN npm ci
RUN npm test

# Stage 3: Production
FROM node:16-alpine AS production
WORKDIR /app

# Copia solo artefatti necessari
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY package*.json ./

USER node
EXPOSE 3000
CMD ["node", "dist/index.js"]
```

### Build Ottimizzato

```dockerfile
FROM node:16-alpine

WORKDIR /app

# 1. Prima copia dipendenze (layer cacheable)
COPY package*.json ./
RUN npm ci --only=production

# 2. Poi copia codice (cambia spesso)
COPY . .

# 3. Build
RUN npm run build

# 4. Cleanup
RUN rm -rf /tmp/* /var/cache/apk/*

USER node
CMD ["npm", "start"]
```

## Comandi Docker

### Gestione Immagini

```bash
# Build immagine
docker build -t myapp:1.0 .
docker build -f Dockerfile.prod -t myapp:prod .
docker build --no-cache -t myapp:latest .

# Build con argumenti
docker build --build-arg VERSION=2.0 -t myapp:2.0 .

# Build multi-platform
docker buildx build --platform linux/amd64,linux/arm64 -t myapp:latest .

# Listare immagini
docker images
docker images --filter "dangling=true"
docker images --format "{{.Repository}}:{{.Tag}}"

# Pull immagine
docker pull nginx:latest
docker pull redis:6.2-alpine

# Push immagine
docker tag myapp:latest username/myapp:latest
docker push username/myapp:latest

# Rimuovere immagini
docker rmi myapp:1.0
docker rmi $(docker images -q -f "dangling=true")  # Rimuovi dangling

# Inspect immagine
docker inspect nginx:latest
docker history nginx:latest

# Save/Load immagini
docker save -o myapp.tar myapp:latest
docker load -i myapp.tar

# Export/Import container
docker export mycontainer > mycontainer.tar
docker import mycontainer.tar myimage:latest
```

### Gestione Container

```bash
# Eseguire container
docker run nginx
docker run -d nginx                          # Detached mode
docker run -d -p 8080:80 nginx              # Port mapping
docker run -d -p 8080:80 --name web nginx   # Con nome
docker run -it ubuntu bash                   # Interactive

# Con variabili d'ambiente
docker run -e NODE_ENV=production -e PORT=3000 myapp

# Con volume
docker run -v /host/path:/container/path nginx
docker run -v myvolume:/data nginx

# Con limiti risorse
docker run -m 512m --cpus="1.5" nginx

# Eseguire comando in container running
docker exec -it mycontainer bash
docker exec mycontainer ls /app

# Listare container
docker ps                    # Running
docker ps -a                # Tutti
docker ps -q                # Solo IDs
docker ps --filter "status=exited"

# Logs
docker logs mycontainer
docker logs -f mycontainer           # Follow
docker logs --tail 100 mycontainer  # Ultime 100 righe
docker logs --since 30m mycontainer # Ultimi 30 minuti

# Start/Stop/Restart
docker start mycontainer
docker stop mycontainer              # Graceful (SIGTERM)
docker stop -t 0 mycontainer        # Immediate
docker restart mycontainer
docker kill mycontainer             # Force (SIGKILL)

# Pause/Unpause
docker pause mycontainer
docker unpause mycontainer

# Rimuovere container
docker rm mycontainer
docker rm -f mycontainer            # Force (running)
docker rm $(docker ps -aq)          # Tutti

# Inspect container
docker inspect mycontainer
docker inspect --format='{{.State.Running}}' mycontainer

# Stats
docker stats                        # Tutti i container
docker stats mycontainer           # Container specifico

# Top (processi)
docker top mycontainer

# Diff (modifiche filesystem)
docker diff mycontainer

# Commit (crea immagine da container)
docker commit mycontainer myimage:snapshot
```

### Gestione Volumi

```bash
# Creare volume
docker volume create myvolume

# Listare volumi
docker volume ls
docker volume ls --filter "dangling=true"

# Inspect volume
docker volume inspect myvolume

# Rimuovere volume
docker volume rm myvolume
docker volume prune  # Rimuovi volumi non usati

# Usare volume
docker run -v myvolume:/data nginx

# Bind mount
docker run -v /host/path:/container/path nginx
docker run -v $(pwd):/app node

# Tmpfs mount (in memoria)
docker run --tmpfs /tmp nginx

# Volume read-only
docker run -v myvolume:/data:ro nginx
```

### Gestione Network

```bash
# Creare network
docker network create mynetwork
docker network create --driver bridge mybridge
docker network create --driver overlay myoverlay

# Listare networks
docker network ls

# Inspect network
docker network inspect mynetwork

# Connettere container a network
docker network connect mynetwork mycontainer

# Disconnettere
docker network disconnect mynetwork mycontainer

# Rimuovere network
docker network rm mynetwork
docker network prune

# Eseguire container su network specifico
docker run --network mynetwork nginx
docker run --network host nginx  # Host networking
```

### System Commands

```bash
# Info sistema
docker info
docker version

# Disk usage
docker system df
docker system df -v

# Pulizia
docker system prune              # Rimuovi dati non usati
docker system prune -a          # Include immagini non usate
docker system prune --volumes   # Include volumi

# Eventi
docker events
docker events --filter 'event=start'

# Login/Logout registry
docker login
docker login registry.example.com
docker logout
```

## Docker Compose

### Cos'è Docker Compose?

Docker Compose è uno strumento per definire ed eseguire applicazioni multi-container attraverso file YAML.

### File docker-compose.yml

```yaml
version: '3.9'

services:
  # Web Application
  web:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        - VERSION=1.0.0
    image: myapp:latest
    container_name: webapp
    restart: unless-stopped
    ports:
      - "8080:8000"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/mydb
      - REDIS_URL=redis://redis:6379
      - NODE_ENV=production
    env_file:
      - .env
    volumes:
      - ./app:/app
      - static_volume:/app/static
      - media_volume:/app/media
    depends_on:
      - db
      - redis
    networks:
      - backend
      - frontend
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    deploy:
      resources:
        limits:
          cpus: '0.50'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M

  # Database
  db:
    image: postgres:14-alpine
    container_name: postgres
    restart: unless-stopped
    environment:
      - POSTGRES_DB=mydb
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD_FILE=/run/secrets/db_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    networks:
      - backend
    secrets:
      - db_password
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: redis
    restart: unless-stopped
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - backend
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3

  # Nginx Reverse Proxy
  nginx:
    image: nginx:alpine
    container_name: nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - static_volume:/app/static:ro
      - media_volume:/app/media:ro
      - ssl_certs:/etc/nginx/ssl:ro
    depends_on:
      - web
    networks:
      - frontend
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Worker (Background Tasks)
  worker:
    build:
      context: .
      dockerfile: Dockerfile
    command: celery -A myapp worker --loglevel=info
    restart: unless-stopped
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/mydb
      - REDIS_URL=redis://redis:6379
    volumes:
      - ./app:/app
    depends_on:
      - db
      - redis
    networks:
      - backend
    deploy:
      replicas: 2

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
  static_volume:
  media_volume:
  ssl_certs:
    external: true

networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
    internal: true

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

### Comandi Docker Compose

```bash
# Avvio servizi
docker-compose up
docker-compose up -d                      # Detached
docker-compose up --build                # Rebuild images
docker-compose up --scale worker=3       # Scale service

# Stop servizi
docker-compose stop
docker-compose down                      # Stop e rimuovi
docker-compose down -v                   # Include volumi
docker-compose down --rmi all           # Include immagini

# Build
docker-compose build
docker-compose build --no-cache
docker-compose build web                # Service specifico

# Logs
docker-compose logs
docker-compose logs -f                  # Follow
docker-compose logs -f web             # Service specifico
docker-compose logs --tail=100 web     # Ultime 100 righe

# Eseguire comandi
docker-compose exec web bash
docker-compose exec db psql -U user mydb
docker-compose run web python manage.py migrate

# Restart servizi
docker-compose restart
docker-compose restart web

# Pause/Unpause
docker-compose pause
docker-compose unpause

# Listare servizi
docker-compose ps
docker-compose ps -a

# Top (processi)
docker-compose top

# Config
docker-compose config                   # Valida e mostra config
docker-compose config --services        # Lista servizi

# Pull immagini
docker-compose pull

# Push immagini
docker-compose push
```

### Override Files

```yaml
# docker-compose.override.yml (automatico in dev)
version: '3.9'

services:
  web:
    volumes:
      - ./app:/app:delegated  # Performance su macOS
    environment:
      - DEBUG=true
    command: python manage.py runserver 0.0.0.0:8000

  db:
    ports:
      - "5432:5432"  # Esponi per debugging
```

```bash
# Usare file specifici
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Esempio Applicazione Completa

```yaml
# docker-compose.yml per stack MEAN
version: '3.9'

services:
  # MongoDB
  mongodb:
    image: mongo:5
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: secret
    volumes:
      - mongo_data:/data/db
    networks:
      - app-network

  # Express API
  api:
    build: ./api
    restart: always
    ports:
      - "3000:3000"
    environment:
      - MONGODB_URI=mongodb://admin:secret@mongodb:27017/
      - JWT_SECRET=supersecret
    depends_on:
      - mongodb
    networks:
      - app-network

  # Angular Frontend
  frontend:
    build: ./frontend
    restart: always
    ports:
      - "4200:80"
    depends_on:
      - api
    networks:
      - app-network

volumes:
  mongo_data:

networks:
  app-network:
    driver: bridge
```

## Networking in Docker

### Network Drivers

#### 1. Bridge (Default)

```bash
# Creare bridge network
docker network create --driver bridge my-bridge

# Configurazione avanzata
docker network create \
  --driver bridge \
  --subnet 172.20.0.0/16 \
  --ip-range 172.20.240.0/20 \
  --gateway 172.20.0.1 \
  my-custom-bridge

# Usare
docker run --network my-bridge nginx
```

#### 2. Host

```bash
# Usa network stack dell'host
docker run --network host nginx
```

#### 3. Overlay

```bash
# Per Docker Swarm
docker network create --driver overlay my-overlay

# Con encryption
docker network create \
  --driver overlay \
  --opt encrypted \
  secure-overlay
```

#### 4. Macvlan

```bash
# Assegna MAC address al container
docker network create -d macvlan \
  --subnet=192.168.1.0/24 \
  --gateway=192.168.1.1 \
  -o parent=eth0 \
  my-macvlan-net
```

### DNS e Service Discovery

```bash
# I container si trovano per nome
docker network create mynet
docker run -d --name web --network mynet nginx
docker run --network mynet alpine ping web  # Risolve!

# Aliases
docker run -d --name web --network mynet --network-alias webapp nginx
docker run --network mynet alpine ping webapp
```

### Port Publishing

```bash
# Publish su interfaccia specifica
docker run -p 127.0.0.1:8080:80 nginx

# Publish su porta random
docker run -P nginx  # Usa porte da EXPOSE

# Publish range
docker run -p 8080-8090:8080-8090 myapp

# UDP
docker run -p 8080:8080/udp myapp

# Multipli
docker run -p 80:80 -p 443:443 nginx
```

### Isolamento Network

```bash
# Network isolata (no internet)
docker network create --internal private-net

# Container senza rete
docker run --network none alpine
```

## Storage in Docker

### Volumes

```bash
# Named volume
docker volume create mydata
docker run -v mydata:/data nginx

# Anonymous volume
docker run -v /data nginx

# Inspect
docker volume inspect mydata

# Backup volume
docker run --rm \
  -v mydata:/source \
  -v $(pwd):/backup \
  alpine tar czf /backup/backup.tar.gz -C /source .

# Restore volume
docker run --rm \
  -v mydata:/target \
  -v $(pwd):/backup \
  alpine tar xzf /backup/backup.tar.gz -C /target
```

### Bind Mounts

```bash
# Mount directory
docker run -v /host/path:/container/path nginx

# Read-only
docker run -v /host/path:/container/path:ro nginx

# Con opzioni
docker run -v /host/path:/container/path:rw,Z nginx  # SELinux label
```

### tmpfs Mounts

```bash
# Storage in RAM
docker run --tmpfs /tmp nginx

# Con opzioni
docker run --tmpfs /tmp:rw,size=100m,mode=1777 nginx
```

### Storage Drivers

- **overlay2**: Default, performante
- **aufs**: Legacy
- **devicemapper**: Enterprise, thin provisioning
- **btrfs**: Copy-on-write filesystem
- **zfs**: Avanzato, snapshot

```bash
# Verificare driver
docker info | grep "Storage Driver"
```

## Sicurezza in Docker

### Best Practices

#### 1. Immagini Base Minime

```dockerfile
# ❌ Evita
FROM ubuntu:latest

# ✅ Preferisci
FROM alpine:3.17
FROM gcr.io/distroless/python3
FROM scratch  # Per Go binaries
```

#### 2. Non Root User

```dockerfile
FROM node:16-alpine

# Crea utente
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

# Cambia ownership
COPY --chown=nodejs:nodejs . .

# Switch user
USER nodejs

CMD ["node", "server.js"]
```

#### 3. Multi-stage per Sicurezza

```dockerfile
# Build stage
FROM golang:1.19 AS builder
WORKDIR /app
COPY . .
RUN CGO_ENABLED=0 go build -o app

# Production stage
FROM scratch
COPY --from=builder /app/app /app
USER 1000:1000
ENTRYPOINT ["/app"]
```

#### 4. Scan Vulnerabilità

```bash
# Docker scan (Snyk)
docker scan nginx:latest

# Trivy
trivy image nginx:latest

# Grype
grype nginx:latest

# Clair
clairctl analyze nginx:latest
```

#### 5. Secrets Management

```bash
# Docker secrets (Swarm)
echo "mypassword" | docker secret create db_password -

# Usare in container
docker service create \
  --secret db_password \
  postgres

# Docker Compose
docker-compose --env-file .env.prod up
```

```yaml
# docker-compose.yml
services:
  app:
    environment:
      - DB_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_password

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

#### 6. Read-only Filesystem

```bash
# Container read-only
docker run --read-only nginx

# Con tmpfs per scritture temporanee
docker run --read-only --tmpfs /tmp nginx
```

#### 7. Capabilities

```bash
# Drop tutte, aggiungi solo necessarie
docker run --cap-drop ALL --cap-add NET_BIND_SERVICE nginx

# Drop specifiche
docker run --cap-drop CHOWN --cap-drop SETUID nginx
```

#### 8. Security Options

```bash
# AppArmor
docker run --security-opt apparmor=docker-default nginx

# SELinux
docker run --security-opt label=level:s0:c100,c200 nginx

# Seccomp profile
docker run --security-opt seccomp=/path/to/profile.json nginx

# No new privileges
docker run --security-opt no-new-privileges nginx
```

#### 9. Resource Limits

```bash
# Memory limit
docker run -m 512m nginx

# CPU limit
docker run --cpus="1.5" nginx

# PID limit
docker run --pids-limit 100 nginx
```

#### 10. Network Security

```bash
# Network isolata
docker network create --internal backend

# No outbound traffic
docker run --network none nginx
```

### Content Trust

```bash
# Abilita Docker Content Trust
export DOCKER_CONTENT_TRUST=1

# Pull solo immagini firmate
docker pull nginx:latest

# Firmare immagini
docker trust sign myimage:latest
```

### Dockerfile Security Scanning

```bash
# Hadolint
hadolint Dockerfile

# Dockle
dockle myimage:latest
```

## Monitoring e Logging

### Container Stats

```bash
# Stats real-time
docker stats

# Formato custom
docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"

# No stream (snapshot)
docker stats --no-stream
```

### Logging

#### Log Drivers

```bash
# json-file (default)
docker run --log-driver json-file nginx

# syslog
docker run --log-driver syslog --log-opt syslog-address=udp://logs.example.com:514 nginx

# journald
docker run --log-driver journald nginx

# fluentd
docker run --log-driver fluentd --log-opt fluentd-address=localhost:24224 nginx

# awslogs
docker run --log-driver awslogs \
  --log-opt awslogs-region=us-east-1 \
  --log-opt awslogs-group=myapp \
  nginx

# Disable logging
docker run --log-driver none nginx
```

#### Configurazione Logging

```json
// /etc/docker/daemon.json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3",
    "labels": "production_status",
    "env": "os,customer"
  }
}
```

### Prometheus Monitoring

```yaml
# docker-compose.yml
version: '3.9'

services:
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"

  node-exporter:
    image: prom/node-exporter
    ports:
      - "9100:9100"

  cadvisor:
    image: gcr.io/cadvisor/cadvisor
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
    ports:
      - "8080:8080"

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=secret
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  prometheus_data:
  grafana_data:
```

## Performance Optimization

### Build Cache

```dockerfile
# ✅ Ottimizzato - dipendenze prima
FROM node:16
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ❌ Non ottimizzato
FROM node:16
WORKDIR /app
COPY . .
RUN npm ci && npm run build
```

### Layer Optimization

```dockerfile
# ❌ Tanti layer
RUN apt-get update
RUN apt-get install -y package1
RUN apt-get install -y package2
RUN rm -rf /var/lib/apt/lists/*

# ✅ Layer unico
RUN apt-get update && \
    apt-get install -y \
        package1 \
        package2 && \
    rm -rf /var/lib/apt/lists/*
```

### .dockerignore

```
# .dockerignore
node_modules
npm-debug.log
.git
.gitignore
README.md
.env
.env.local
coverage/
.vscode/
.idea/
*.md
.DS_Store
```

### BuildKit

```bash
# Abilita BuildKit
export DOCKER_BUILDKIT=1
docker build -t myapp .

# BuildKit features
docker build --secret id=mysecret,src=/path/to/secret .
docker build --ssh default .
docker build --cache-from myapp:cache .
```

## Troubleshooting

### Debug Container

```bash
# Logs dettagliati
docker logs -f --details mycontainer

# Inspect
docker inspect mycontainer

# Processi
docker top mycontainer

# Statistiche
docker stats mycontainer

# Eventi
docker events --filter container=mycontainer

# Filesystem changes
docker diff mycontainer
```

### Container Non Si Avvia

```bash
# Verificare logs
docker logs mycontainer

# Eseguire con override
docker run -it --entrypoint /bin/sh myimage

# Verificare healthcheck
docker inspect --format='{{json .State.Health}}' mycontainer
```

### Network Issues

```bash
# Testare connettività
docker run --rm --network container:mycontainer nicolaka/netshoot ping google.com

# DNS
docker exec mycontainer nslookup google.com

# Port mapping
docker port mycontainer

# Network inspect
docker network inspect mynetwork
```

### Performance Issues

```bash
# Controllare stats
docker stats

# Verificare limiti
docker inspect --format='{{.HostConfig.Memory}}' mycontainer

# Disk usage
docker system df

# Cleanup
docker system prune -a
```

### Storage Issues

```bash
# Verificare volumi
docker volume ls
docker volume inspect myvolume

# Cleanup volumi
docker volume prune

# Verificare driver
docker info | grep "Storage Driver"
```

## Best Practices

### Development

1. **Hot Reload**
```yaml
services:
  web:
    volumes:
      - ./app:/app
    environment:
      - FLASK_DEBUG=1
```

2. **Override per Dev**
```yaml
# docker-compose.override.yml
version: '3.9'
services:
  web:
    build:
      target: development
    ports:
      - "8000:8000"
```

### Production

1. **Health Checks**
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost/ || exit 1
```

2. **Restart Policies**
```bash
docker run --restart unless-stopped nginx
```

3. **Resource Limits**
```yaml
deploy:
  resources:
    limits:
      cpus: '0.50'
      memory: 512M
```

4. **Logging Configurato**
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

5. **Non Root**
```dockerfile
USER nonroot:nonroot
```

### Sicurezza

1. **Scan Regolari**
```bash
# CI/CD pipeline
trivy image --severity HIGH,CRITICAL myapp:latest
```

2. **Immagini Firmate**
```bash
export DOCKER_CONTENT_TRUST=1
```

3. **Secrets Sicuri**
```bash
# Non hardcodare in Dockerfile!
# Usa secrets, env vars, vault
```

4. **Aggiornamenti**
```bash
# Rebuild regolare con base aggiornate
docker pull python:3.9-slim
docker build --pull -t myapp .
```

## Esercizi Pratici

### Esercizio 1: Applicazione Python Flask

```dockerfile
# Dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

CMD ["python", "app.py"]
```

```python
# app.py
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello():
    return "Hello from Docker!"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

```
# requirements.txt
Flask==2.3.0
```

```bash
# Build e run
docker build -t flask-app .
docker run -d -p 5000:5000 --name myflask flask-app
curl http://localhost:5000
```

### Esercizio 2: Multi-stage Node.js

```dockerfile
# Build stage
FROM node:16 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM node:16-alpine
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY package*.json ./
EXPOSE 3000
USER node
CMD ["node", "dist/index.js"]
```

### Esercizio 3: Stack WordPress

```yaml
version: '3.9'

services:
  db:
    image: mysql:8
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: wordpress
      MYSQL_USER: wpuser
      MYSQL_PASSWORD: wppass
    volumes:
      - db_data:/var/lib/mysql

  wordpress:
    depends_on:
      - db
    image: wordpress:latest
    restart: always
    ports:
      - "8080:80"
    environment:
      WORDPRESS_DB_HOST: db:3306
      WORDPRESS_DB_USER: wpuser
      WORDPRESS_DB_PASSWORD: wppass
      WORDPRESS_DB_NAME: wordpress
    volumes:
      - wp_data:/var/www/html

volumes:
  db_data:
  wp_data:
```

### Esercizio 4: Applicazione con Redis

```python
# app.py
from flask import Flask
import redis

app = Flask(__name__)
cache = redis.Redis(host='redis', port=6379)

@app.route('/')
def hello():
    count = cache.incr('hits')
    return f'Hello! Visits: {count}'

if __name__ == '__main__':
    app.run(host='0.0.0.0')
```

```yaml
# docker-compose.yml
version: '3.9'

services:
  web:
    build: .
    ports:
      - "5000:5000"
    depends_on:
      - redis
    environment:
      - FLASK_ENV=development

  redis:
    image: redis:alpine
```

## Domande di Verifica

1. Spiega l'architettura di Docker e il ruolo di ciascun componente
2. Qual è la differenza tra CMD e ENTRYPOINT in un Dockerfile?
3. Come funziona il layer caching e come ottimizzarlo?
4. Quali sono le best practices per la sicurezza dei container?
5. Quando usare volumes, bind mounts o tmpfs?
6. Spiega la differenza tra docker run e docker-compose up
7. Come gestiresti i secrets in produzione?
8. Quali strategie useresti per minimizzare la dimensione delle immagini?
9. Come debuggeresti un container che crasha all'avvio?
10. Qual è la differenza tra bridge e overlay networking?

## Risorse Aggiuntive

- [Docker Documentation](https://docs.docker.com/)
- [Docker Hub](https://hub.docker.com/)
- [Dockerfile Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Security](https://docs.docker.com/engine/security/)
- [Play with Docker](https://labs.play-with-docker.com/)
- [Docker Compose Specification](https://docs.docker.com/compose/compose-file/)

## Conclusioni

Docker ha trasformato il modo in cui sviluppiamo e distribuiamo applicazioni, rendendo i deployment più veloci, affidabili e portabili. La padronanza di Docker è essenziale per qualsiasi professionista che lavora con architetture cloud-native e microservizi.
