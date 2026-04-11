# Altri Provider Cloud

## Introduzione

Oltre ai "Big Three" (AWS, Azure, GCP), esistono numerosi provider cloud alternativi che offrono vantaggi specifici in termini di prezzo, semplicità, privacy, o specializzazione.

## Provider Cloud Tradizionali

### IBM Cloud

Piattaforma enterprise con focus su hybrid cloud e AI.

**Servizi principali:**
- **Compute**: Virtual Servers, Bare Metal, Code Engine
- **Storage**: Cloud Object Storage, Block Storage
- **Database**: Db2, Cloudant (CouchDB)
- **AI**: Watson Assistant, Watson Studio, Natural Language Understanding
- **Container**: Red Hat OpenShift on IBM Cloud
- **Blockchain**: IBM Blockchain Platform

**Vantaggi:**
- Forte nel mercato enterprise
- Integrazione Red Hat OpenShift
- Watson AI/ML avanzato
- Compliance e sicurezza

**Pricing**: Competitivo per workload enterprise

### Oracle Cloud Infrastructure (OCI)

Focus su database e applicazioni enterprise.

**Servizi principali:**
- **Autonomous Database**: Self-driving, self-securing, self-repairing
- **Compute**: VM, Bare Metal, Container Engine
- **Oracle Cloud VMware Solution**
- **Oracle Functions**
- **MySQL HeatWave**: Analytics integrato

**Vantaggi:**
- **Autonomous Database**: Gestione automatizzata completa
- Performance database eccellenti
- Bring Your Own License (BYOL)
- Pricing competitivo (vs AWS/Azure)

**Use Cases:**
- Oracle workloads esistenti
- Database-intensive applications
- ERP (Oracle EBS, PeopleSoft, JD Edwards)

### Alibaba Cloud

Leader in Asia, parte di Alibaba Group.

**Servizi principali:**
- **ECS** (Elastic Compute Service)
- **OSS** (Object Storage Service)
- **ApsaraDB**: RDS, PolarDB, MongoDB
- **MaxCompute**: Big data platform
- **DataWorks**: Data integration

**Vantaggi:**
- Presenza forte in Cina e Asia
- Pricing competitivo
- Compliance locale per mercato cinese

**Market share**: ~4% globale, leader in Asia

## Provider Developer-Friendly

### DigitalOcean

Cloud semplificato per sviluppatori e startup.

**Servizi:**
- **Droplets**: VM semplici e veloci
- **Kubernetes**: Managed Kubernetes
- **App Platform**: PaaS per deploy rapidi
- **Managed Databases**: PostgreSQL, MySQL, MongoDB, Redis
- **Spaces**: Object storage
- **Load Balancers**

**Pricing Droplets:**
```
Basic Droplet:
- $4/mese: 1 vCPU, 512 MB RAM, 10 GB SSD
- $6/mese: 1 vCPU, 1 GB RAM, 25 GB SSD
- $12/mese: 1 vCPU, 2 GB RAM, 50 GB SSD
- $24/mese: 2 vCPU, 4 GB RAM, 80 GB SSD
```

**Vantaggi:**
- **Semplicità**: UI/UX eccellente
- **Pricing trasparente**: Nessun costo nascosto
- **Documentation**: Guide e tutorial di qualità
- **Community**: Forum attivo
- **Predictable costs**

**Use Cases:**
- Progetti personali e startup
- Dev/test environments
- Small-medium applications
- Learning cloud

**CLI (doctl):**
```bash
# Creare droplet
doctl compute droplet create my-droplet \
    --image ubuntu-22-04-x64 \
    --size s-1vcpu-1gb \
    --region fra1 \
    --ssh-keys your-ssh-key-id

# List droplets
doctl compute droplet list
```

### Linode (Akamai)

VM ad alte performance, acquisito da Akamai nel 2022.

**Servizi:**
- **Compute Instances** (Linodes)
- **Kubernetes** (LKE)
- **Object Storage**
- **Block Storage**
- **NodeBalancers**
- **Managed Databases**

**Pricing:**
```
Shared CPU:
- $5/mese: 1 vCPU, 1 GB RAM, 25 GB storage
- $10/mese: 1 vCPU, 2 GB RAM, 50 GB storage

Dedicated CPU:
- $30/mese: 2 vCPU, 4 GB RAM, 80 GB storage
- $60/mese: 4 vCPU, 8 GB RAM, 160 GB storage
```

**Vantaggi:**
- Performance eccellenti
- Network speed (40 Gbps)
- Pricing chiaro
- Support di qualità
- 11 datacenter globali

### Hetzner Cloud

Provider tedesco con eccellente rapporto qualità/prezzo.

**Servizi:**
- **Cloud Servers** (CX, CPX, CCX series)
- **Volumes** (Block Storage)
- **Load Balancers**
- **Networks** (Private Networks)
- **Firewalls**

**Pricing (incredibilmente competitivo):**
```
CX Series (Shared vCPU):
- €3.79/mese: 1 vCPU, 2 GB RAM, 20 GB SSD
- €5.83/mese: 1 vCPU, 4 GB RAM, 40 GB SSD
- €11.59/mese: 2 vCPU, 8 GB RAM, 80 GB SSD

CPX Series (AMD EPYC):
- €4.90/mese: 2 vCPU, 2 GB RAM, 40 GB SSD
- €8.90/mese: 3 vCPU, 4 GB RAM, 80 GB SSD

CCX Series (Dedicated AMD EPYC):
- €47.40/mese: 8 vCPU, 16 GB RAM, 240 GB NVMe
```

**Vantaggi:**
- **Prezzo**: Tra i più economici
- **Performance**: Hardware moderno
- **Network**: 20 TB traffic incluso
- **GDPR compliant**: Datacenter EU
- **Support**: Tedesco/Inglese

**Location**: Germania, Finlandia, USA

**CLI (hcloud):**
```bash
# Creare server
hcloud server create \
    --name my-server \
    --type cx11 \
    --image ubuntu-22.04 \
    --ssh-key my-key

# List servers
hcloud server list
```

### OVHcloud

Provider europeo, leader in Europa.

**Servizi:**
- **Public Cloud**: VM, Kubernetes
- **Private Cloud**: VMware-based
- **Bare Metal Cloud**
- **Web Hosting**
- **Object Storage (Swift/S3)**

**Vantaggi:**
- Presenza europea forte
- GDPR compliance nativo
- Pricing competitivo
- Datacenter in Europa, Canada, Asia, Australia

### Scaleway

Provider francese, parte di Iliad Group.

**Servizi:**
- **Compute**: Instances, GPU instances
- **Kubernetes Kapsule**
- **Object Storage**
- **Managed Databases**: PostgreSQL, MySQL
- **Serverless**: Functions, Containers
- **IoT Hub**

**Innovazioni:**
- **ARM instances**: Basati su Apple Silicon
- **Elastic Metal**: Bare metal elastico
- **Sustainability**: Focus su impatto ambientale

## Provider Specializzati

### Heroku (Salesforce)

PaaS pioniere, ora parte di Salesforce.

**Caratteristiche:**
- **Buildpacks**: Deploy automatizzato
- **Add-ons marketplace**: 200+ servizi
- **Dyno**: Container units
- **Git-based deployment**

**Supporto linguaggi:**
- Ruby, Node.js, Python, Java, PHP, Go, Scala, Clojure

**Pricing:**
```
Dyno Types:
- Eco: $5/mese (1000 ore condivise)
- Basic: $7/dyno/mese
- Standard: $25-50/dyno/mese
- Performance: $250-500/dyno/mese
```

**Pro:**
- Deploy semplicissimo (`git push heroku main`)
- Ecosistema add-ons ricco
- Developer experience eccellente

**Contro:**
- Più costoso vs alternatives
- Vendor lock-in
- Limited control

### Netlify

Piattaforma per JAMstack e frontend.

**Caratteristiche:**
- **Static site hosting**
- **Serverless functions**
- **Form handling**
- **Identity/Authentication**
- **Split testing**
- **Edge Functions**

**Pricing:**
```
Free tier:
- 100 GB bandwidth/mese
- 300 build minutes/mese
- Unlimited sites

Pro: $19/mese/utente
Business: $99/mese/utente
```

**Deploy:**
```bash
# Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod
```

**Vantaggi:**
- Deploy automatico da Git
- CDN globale
- Preview deployments
- Instant rollback

### Vercel

Platform per Next.js e frontend frameworks.

**Caratteristiche:**
- **Edge Network** globale
- **Serverless Functions**
- **Edge Middleware**
- **Analytics integrato**
- **Preview deployments**

**Framework supportati:**
- Next.js (creatori)
- React, Vue, Svelte, Angular
- Static sites

**Pricing:**
```
Hobby: Free
- 100 GB bandwidth
- Serverless functions

Pro: $20/utente/mese
Enterprise: Custom
```

### Cloudflare Workers / Pages

Edge computing e hosting.

**Cloudflare Workers:**
- JavaScript/WebAssembly at the edge
- 300+ locations
- Cold start < 1ms

```javascript
// worker.js
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})

async function handleRequest(request) {
  return new Response('Hello from the edge!', {
    headers: { 'content-type': 'text/plain' }
  })
}
```

**Cloudflare Pages:**
- Static site hosting
- Git integration
- Edge functions

**Pricing:**
```
Workers Free:
- 100,000 requests/giorno
- 10ms CPU time

Workers Paid: $5/mese
- 10M requests/mese incluse
- Nessun limite CPU time

R2 Storage:
- 10 GB storage gratis/mese
- $0.015/GB/mese (molto più economico di S3)
```

### Railway

Platform-as-a-Service moderno.

**Caratteristiche:**
- **Instant deployments**
- **Database integrati**: PostgreSQL, MySQL, MongoDB, Redis
- **Cron jobs**
- **Private networking**

**Pricing**: Pay-per-use, ~$0.000231/GB-hour

### Render

Alternativa moderna a Heroku.

**Servizi:**
- **Web Services**
- **Static Sites**
- **Background Workers**
- **Cron Jobs**
- **PostgreSQL** databases

**Pricing:**
```
Free tier:
- Static sites illimitati
- Web service: 750 ore/mese

Starter: $7/mese
Standard: $25/mese
```

## Provider Europei e Italiani

### Aruba Cloud

Provider italiano, leader in Italia.

**Servizi:**
- **Cloud Compute**: VM Windows/Linux
- **Cloud Object Storage**: S3-compatible
- **Smart Cloud**: Managed Kubernetes
- **Hosting**: Shared, VPS, Dedicated

**Datacenter**: Italia (Arezzo, Ponte San Pietro)

**Vantaggi:**
- GDPR compliant
- Supporto italiano
- Datacenter in Italia
- Prezzi competitivi per mercato italiano

### Register.it Cloud

Provider italiano.

**Servizi:**
- Cloud Hosting
- VPS
- Server Dedicati
- Object Storage

### Seeweb

Provider italiano specializzato in housing e cloud.

**Servizi:**
- Cloud Server
- Object Storage
- CDN
- Managed Services

**Datacenter**: Italia (Frosinone)

## Tabella Comparativa Multi-Cloud

| Provider | Market Share | Regioni | Forza Principale | Pricing |
|----------|--------------|---------|------------------|---------|
| **AWS** | 32% | 33 | Completezza servizi, Mature | $$$ |
| **Azure** | 21% | 60+ | Enterprise, Hybrid cloud | $$$ |
| **GCP** | 10% | 40+ | Big Data, ML, Kubernetes | $$ |
| **IBM Cloud** | 4% | 19 | Watson AI, Enterprise | $$$ |
| **Oracle Cloud** | 2% | 41 | Database, Autonomous | $$ |
| **Alibaba Cloud** | 4% | 27 | Asia, E-commerce | $$ |
| **DigitalOcean** | <1% | 15 | Semplicità, Dev-friendly | $ |
| **Linode** | <1% | 11 | Performance, Value | $ |
| **Hetzner** | <1% | 5 | Prezzo, Europa | $ |
| **Heroku** | <1% | Global | PaaS, Developer UX | $$$ |
| **Netlify** | <1% | Global | JAMstack, Frontend | $$ |
| **Vercel** | <1% | Global | Next.js, Edge | $$ |
| **Cloudflare** | <1% | 300+ | Edge, Performance | $ |

## Strategie Multi-Cloud e Hybrid Cloud

### Multi-Cloud

Usare più provider cloud contemporaneamente.

**Vantaggi:**
- **Avoid vendor lock-in**
- **Redundancy**: Failover cross-cloud
- **Best-of-breed**: Scegli migliore servizio per use case
- **Geographic reach**: Coverage globale
- **Compliance**: Data residency requirements
- **Negotiating power**: Leverage competition

**Svantaggi:**
- **Complessità**: Gestione multiple piattaforme
- **Costi**: Orchestrazione, networking
- **Competenze**: Team deve conoscere più piattaforme
- **Data transfer costs**: Egress fees

**Use Cases:**
- **Disaster Recovery**: Primary su AWS, DR su Azure
- **Workload optimization**: ML su GCP, Enterprise su Azure
- **Geographic requirements**: GDPR in EU cloud, US in AWS

### Hybrid Cloud

Integrazione on-premise con cloud.

**Soluzioni:**
- **AWS Outposts**: AWS on-premise
- **Azure Arc**: Gestione unified multi-cloud/hybrid
- **Google Anthos**: Kubernetes ovunque
- **VMware Cloud**: Multi-cloud su VMware

**Use Cases:**
- Migrazione graduale
- Data sovereignty
- Latency-sensitive applications
- Legacy systems integration

## Vendor Lock-in: Rischi e Mitigazione

### Rischi Lock-in

1. **Servizi proprietari**
   - Lambda, Azure Functions specifiche
   - DynamoDB, Cosmos DB API proprietarie
   - Managed services non portable

2. **Costi switching**
   - Re-architettura applicazioni
   - Re-training team
   - Data migration costs

3. **Egress costs**
   - AWS: $0.09/GB oltre free tier
   - Azure: $0.087/GB
   - GCP: $0.12/GB

### Strategie Mitigazione

#### 1. Containerization

```dockerfile
# App containerizzata è portable
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
CMD ["node", "server.js"]
```

Deploy ovunque: AWS ECS, Azure AKS, GCP GKE, DigitalOcean K8s

#### 2. Kubernetes

Standard de facto per orchestrazione. Portabile tra:
- AWS EKS
- Azure AKS
- GCP GKE
- DigitalOcean Kubernetes
- On-premise

#### 3. Infrastructure as Code Multi-Cloud

**Terraform:**
```hcl
# Stesso codice per multiple clouds
resource "aws_instance" "web" {
  count = var.cloud == "aws" ? 1 : 0
  ami = "ami-xxx"
  instance_type = "t3.micro"
}

resource "azurerm_virtual_machine" "web" {
  count = var.cloud == "azure" ? 1 : 0
  name = "web-vm"
  vm_size = "Standard_B1s"
}

resource "google_compute_instance" "web" {
  count = var.cloud == "gcp" ? 1 : 0
  name = "web-vm"
  machine_type = "e2-micro"
}
```

#### 4. Open Source dove possibile

- **Database**: PostgreSQL, MySQL, MongoDB (portable)
- **Cache**: Redis, Memcached
- **Message Queue**: RabbitMQ, Kafka
- **Object Storage**: MinIO (S3-compatible)

#### 5. Abstraction Layers

**Apache Libcloud**: Python library multi-cloud
```python
from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver

# AWS
cls = get_driver(Provider.EC2)
driver = cls('api_key', 'api_secret', region='us-west-2')

# GCP
cls = get_driver(Provider.GCE)
driver = cls('email', 'key', project='project-id')

# Stesso codice per creare VM
node = driver.create_node(name='test', size=small_size, image=ubuntu_image)
```

## Quando Scegliere Provider Alternativi

### Scegli DigitalOcean/Linode/Hetzner quando:
- ✅ Budget limitato
- ✅ Applicazione semplice (web app, API)
- ✅ Team piccolo
- ✅ Vuoi semplicità vs feature advanced
- ✅ Traffic prevedibile
- ✅ Non serve global scale immediato

### Scegli Heroku/Render/Railway quando:
- ✅ Focus su product, non infra
- ✅ Team di sviluppatori, non DevOps
- ✅ Prototipazione rapida
- ✅ Startup MVP

### Scegli Netlify/Vercel quando:
- ✅ Frontend/JAMstack application
- ✅ Static sites con API serverless
- ✅ Deploy automatici da Git

### Scegli Cloudflare quando:
- ✅ Edge computing
- ✅ Global distribution
- ✅ DDoS protection
- ✅ Low-latency critical

### Rimani su AWS/Azure/GCP quando:
- ✅ Enterprise requirements
- ✅ Servizi managed avanzati
- ✅ Compliance strict (SOC2, HIPAA, etc)
- ✅ Global scale day 1
- ✅ Budget enterprise
- ✅ Team DevOps skilled

## Costo Comparison Esempio

**Scenario**: Web app con database

### DigitalOcean
```
Droplet (2 vCPU, 4 GB): $24/mese
Managed PostgreSQL: $15/mese
Load Balancer: $12/mese
Total: $51/mese
```

### AWS
```
EC2 t3.medium: ~$30/mese
RDS db.t3.micro: ~$15/mese
ALB: ~$22/mese
Total: ~$67/mese (+ data transfer)
```

### Azure
```
VM B2s: ~$30/mese
SQL Database S0: ~$15/mese
Load Balancer: ~$20/mese
Total: ~$65/mese
```

### Hetzner
```
CX21 (2 vCPU, 4 GB): €5.83/mese (~$6.5)
PostgreSQL self-managed: €0
Load Balancer: €5.39/mese (~$6)
Total: ~$12.5/mese
```

**Nota**: Hetzner richiede gestione database manuale.

## Esercizi Pratici

### Esercizio 1: Deploy su Multiple Clouds

Deploy stessa app Node.js su:
1. DigitalOcean App Platform
2. Heroku
3. Render
4. Vercel

Confronta: tempo deploy, pricing, features.

### Esercizio 2: Object Storage Comparison

Upload stesso dataset (1 GB) su:
- AWS S3
- Azure Blob Storage
- GCP Cloud Storage
- DigitalOcean Spaces
- Cloudflare R2

Confronta: pricing, performance, API compatibility.

### Esercizio 3: Kubernetes Multi-Cloud

Deploy stessa app Kubernetes su:
- AWS EKS
- GCP GKE
- DigitalOcean Kubernetes

Usa stesso manifest YAML.

### Esercizio 4: Terraform Multi-Cloud

Scrivere modulo Terraform che:
- Crea VM su AWS/Azure/GCP
- Usa variable per selezionare provider
- Output: IP pubblico

### Esercizio 5: Cost Analysis

Per applicazione web esempio:
- Calcola costo su AWS, Azure, GCP
- Calcola costo su DigitalOcean, Linode, Hetzner
- Confronta TCO 1 anno, 3 anni

## Domande di Verifica

1. Quali sono vantaggi e svantaggi di una strategia multi-cloud?
2. Come mitigare vendor lock-in?
3. Quando sceglieresti DigitalOcean invece di AWS?
4. Differenza tra Heroku, Netlify e Vercel?
5. Come funziona edge computing con Cloudflare Workers?
6. Quali sono i principali cloud provider europei?
7. Strategie per data portability tra clouds?
8. Costi nascosti da considerare nel cloud?
9. Come Kubernetes aiuta portabilità?
10. Vantaggi di provider specifici (es: GCP per ML)?

## Risorse

### Comparison Tools
- [CloudOrado](https://www.cloudorado.com/)
- [ec2instances.info](https://instances.vantage.sh/)

### Community
- [r/devops](https://www.reddit.com/r/devops/)
- [HackerNews](https://news.ycombinator.com/)

### Blogs
- [High Scalability](http://highscalability.com/)
- [The New Stack](https://thenewstack.io/)

---

*Documento aggiornato - 2024*
