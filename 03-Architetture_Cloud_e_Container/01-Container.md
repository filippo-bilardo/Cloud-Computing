# Container: Fondamenti e Concetti

## Introduzione ai Container

I container sono unità standardizzate di software che impacchettano il codice e tutte le sue dipendenze in modo che l'applicazione possa essere eseguita in modo rapido e affidabile da un ambiente di computing all'altro.

### Definizione

Un **container** è un'unità software leggera e standalone che include:
- Codice applicativo
- Runtime
- Strumenti di sistema
- Librerie
- Configurazioni

### Storia dei Container

- **2000**: FreeBSD Jails
- **2004**: Solaris Containers
- **2006**: Process Containers (Google), poi rinominato cgroups
- **2008**: LXC (Linux Containers)
- **2013**: Docker - rivoluzione nella containerizzazione
- **2014**: Kubernetes
- **2015**: Open Container Initiative (OCI)

## Architettura dei Container

### Componenti Chiave

#### 1. Kernel Linux Features

**Namespaces**
- Isolamento delle risorse di sistema
- Tipi di namespace:
  - **PID**: Isolamento dei processi
  - **NET**: Stack di rete
  - **MNT**: Mount points
  - **UTS**: Hostname e domainname
  - **IPC**: Inter-process communication
  - **USER**: User e group IDs

**Control Groups (cgroups)**
- Limitazione delle risorse
- Accounting
- Prioritizzazione
- Controllo

#### 2. Image Layers

```
┌─────────────────────────┐
│   Container Layer (RW)  │  ← Modifiche runtime
├─────────────────────────┤
│   Application Layer     │
├─────────────────────────┤
│   Dependencies Layer    │
├─────────────────────────┤
│   Base OS Layer         │
└─────────────────────────┘
```

### Container Runtime

- **Low-level runtime**: runc, crun
- **High-level runtime**: containerd, CRI-O
- **Container engine**: Docker, Podman

## Vantaggi dei Container

### 1. Portabilità
- "Build once, run anywhere"
- Indipendenza dall'infrastruttura
- Stesso comportamento in dev, test, prod

### 2. Efficienza
- Condivisione del kernel
- Startup veloce (millisecondi)
- Footprint ridotto

### 3. Scalabilità
- Avvio rapido di istanze
- Gestione dinamica del carico
- Orchestrazione automatica

### 4. Isolamento
- Applicazioni separate
- Dipendenze indipendenti
- Sicurezza migliorata

### 5. Consistenza
- Ambiente standardizzato
- Eliminazione del "works on my machine"
- Facilita CI/CD

## Container vs Virtual Machines

| Caratteristica | Container | Virtual Machine |
|----------------|-----------|-----------------|
| **Isolamento** | Process-level | Hardware-level |
| **OS** | Condiviso | Separato per VM |
| **Dimensione** | MB | GB |
| **Startup** | Secondi/millisecondi | Minuti |
| **Overhead** | Minimo | Significativo |
| **Densità** | Alta (centinaia per host) | Bassa (decine per host) |
| **Sicurezza** | Buona | Eccellente |

### Quando Usare Cosa?

**Container - Ideali per:**
- Microservizi
- Applicazioni cloud-native
- CI/CD pipelines
- Sviluppo e testing
- Applicazioni stateless

**Virtual Machines - Ideali per:**
- Applicazioni legacy
- Isolamento completo richiesto
- Diversi OS sulla stessa macchina
- Sicurezza estrema
- Applicazioni stateful

## Ciclo di Vita di un Container

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Created │ -> │ Running  │ -> │  Paused  │ -> │ Stopped  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                      │                               │
                      └───────────────────────────────┘
                              (Restart)
```

### Stati
1. **Created**: Container creato ma non avviato
2. **Running**: Container in esecuzione
3. **Paused**: Processi sospesi
4. **Stopped**: Container terminato
5. **Deleted**: Container rimosso

## Storage nei Container

### Tipi di Storage

#### 1. Container Layer (Ephemeral)
- Dati volatili
- Persi al riavvio
- Per dati temporanei

#### 2. Volumes
- Persistenza dei dati
- Gestiti dal container engine
- Indipendenti dal ciclo di vita

#### 3. Bind Mounts
- Mapping di directory host
- Condivisione file
- Sviluppo locale

#### 4. tmpfs Mounts
- Storage in memoria
- Velocissimo
- Per dati sensibili temporanei

## Networking nei Container

### Modalità di Rete

#### 1. Bridge (default)
- Rete privata interna
- NAT verso esterno
- Container comunicano tra loro

#### 2. Host
- Usa network stack dell'host
- Nessun isolamento di rete
- Massime performance

#### 3. None
- Nessuna rete
- Isolamento completo
- Configurazione custom

#### 4. Overlay
- Multi-host networking
- Per cluster distribuiti
- Usato in orchestrazione

## Sicurezza nei Container

### Best Practices

1. **Immagini Base Minime**
   - Alpine Linux
   - Distroless images
   - Scratch images

2. **Non Eseguire come Root**
   ```dockerfile
   USER nonroot:nonroot
   ```

3. **Scan delle Vulnerabilità**
   - Trivy
   - Clair
   - Snyk

4. **Limitazione Risorse**
   - CPU limits
   - Memory limits
   - Storage quotas

5. **Read-only Filesystem**
   ```bash
   docker run --read-only myapp
   ```

6. **Security Profiles**
   - AppArmor
   - SELinux
   - Seccomp

## Container Registry

### Pubblici
- Docker Hub
- GitHub Container Registry
- Google Container Registry
- Amazon ECR Public

### Privati
- Harbor
- Nexus Repository
- JFrog Artifactory
- GitLab Container Registry

### Gestione delle Immagini

```
┌─────────────┐
│   Build     │  → Crea immagine
├─────────────┤
│   Tag       │  → Versiona
├─────────────┤
│   Push      │  → Carica su registry
├─────────────┤
│   Pull      │  → Scarica da registry
├─────────────┤
│   Run       │  → Esegui container
└─────────────┘
```

## Standard e Specifiche

### OCI (Open Container Initiative)

#### 1. Runtime Specification
- Come eseguire un container
- Configurazione
- Lifecycle

#### 2. Image Specification
- Formato delle immagini
- Manifest
- Layers

#### 3. Distribution Specification
- Come distribuire immagini
- Registry API
- Content discovery

## Casi d'Uso dei Container

### 1. Microservizi
- Servizi indipendenti
- Deploy separati
- Scalabilità granulare

### 2. CI/CD
- Build reproducibili
- Test environment consistenti
- Deploy automatizzati

### 3. Ambienti di Sviluppo
- Parità dev/prod
- Onboarding rapido
- Configurazione as code

### 4. Applicazioni Legacy
- Modernizzazione graduale
- Isolamento dipendenze
- Migration cloud

### 5. Big Data e ML
- Processing distribuito
- Training models
- Jupyter notebooks

## Limitazioni dei Container

### 1. Sicurezza
- Condivisione kernel
- Escape vulnerabilities
- Privilege escalation

### 2. Persistenza
- Dati volatili per default
- Complessità storage distribuito
- Backup e recovery

### 3. Networking
- Overhead in overlay networks
- Complessità configurazione
- Debugging difficile

### 4. Compatibilità OS
- Solo Linux per Linux containers
- Windows containers per Windows
- Non completa astrazione hardware

## Strumenti dell'Ecosistema

### Container Engines
- **Docker**: Il più popolare
- **Podman**: Daemonless, rootless
- **LXC/LXD**: System containers

### Orchestrazione
- **Kubernetes**: Standard de facto
- **Docker Swarm**: Semplice
- **Apache Mesos**: Enterprise

### Monitoring
- **Prometheus**: Metrics
- **Grafana**: Visualization
- **ELK Stack**: Logs

### Security
- **Falco**: Runtime security
- **Aqua Security**: Comprehensive
- **Twistlock**: Cloud native

## Tendenze Future

1. **Serverless Containers**
   - AWS Fargate
   - Azure Container Instances
   - Google Cloud Run

2. **WebAssembly**
   - Alternative ai container
   - Più leggero
   - Multi-platform

3. **Confidential Computing**
   - Container crittografati
   - Secure enclaves
   - Privacy preserving

4. **Edge Computing**
   - Container su edge devices
   - IoT applications
   - 5G networks

## Esercizi Pratici

### Esercizio 1: Esplorare Namespaces
```bash
# Verificare namespaces di un processo
ls -l /proc/$$/ns/

# Creare un nuovo namespace
unshare --fork --pid --mount-proc bash
ps aux  # Mostra solo processi nel namespace
```

### Esercizio 2: Limitare Risorse con cgroups
```bash
# Creare un cgroup
sudo cgcreate -g memory:/mygroup
echo 100M > /sys/fs/cgroup/memory/mygroup/memory.limit_in_bytes

# Eseguire processo nel cgroup
sudo cgexec -g memory:mygroup stress --vm 1 --vm-bytes 150M
```

### Esercizio 3: Confronto Container vs VM
```bash
# Misurare tempo di avvio container
time docker run --rm alpine echo "Hello"

# Misurare dimensione
docker images alpine
```

## Conclusioni

I container hanno rivoluzionato il modo in cui sviluppiamo, distribuiamo e gestiamo le applicazioni. Offrono un equilibrio ottimale tra isolamento, efficienza e portabilità, rendendoli la scelta ideale per applicazioni cloud-native e architetture moderne.

## Risorse Aggiuntive

- [Open Container Initiative](https://opencontainers.org/)
- [CNCF Cloud Native Landscape](https://landscape.cncf.io/)
- [Container Training](https://container.training/)
- [Linux Containers](https://linuxcontainers.org/)

## Domande di Verifica

1. Quali sono le principali differenze tra container e virtual machine?
2. Spiega il ruolo di namespaces e cgroups nella containerizzazione
3. Quali sono i vantaggi e gli svantaggi dei container?
4. Quando sceglieresti un container invece di una VM?
5. Descrivi il ciclo di vita di un container
