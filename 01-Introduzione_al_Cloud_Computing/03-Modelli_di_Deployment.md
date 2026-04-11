# 1.3 Modelli di Deployment del Cloud Computing

## Indice
- [Introduzione ai Modelli di Deployment](#introduzione-ai-modelli-di-deployment)
- [Public Cloud](#public-cloud)
- [Private Cloud](#private-cloud)
- [Hybrid Cloud](#hybrid-cloud)
- [Multi-Cloud](#multi-cloud)
- [Edge Computing](#edge-computing)
- [Confronto e Scelta del Modello](#confronto-e-scelta-del-modello)

---

## Introduzione ai Modelli di Deployment

I **modelli di deployment** definiscono **dove** l'infrastruttura cloud è localizzata e **chi** la gestisce e vi accede.

### I Quattro Modelli Principali

```
┌────────────────────────────────────────────────┐
│                                                │
│  Public Cloud     → AWS, Azure, GCP            │
│  Private Cloud    → On-premise, dedicato       │
│  Hybrid Cloud     → Public + Private           │
│  Multi-Cloud      → Multiple Public Clouds     │
│                                                │
└────────────────────────────────────────────────┘
```

### Criteri di Scelta

- **Costi**: CapEx vs OpEx
- **Controllo**: Livello di gestione richiesto
- **Sicurezza**: Requisiti compliance
- **Scalabilità**: Crescita prevista
- **Latenza**: Prestazioni geografiche
- **Skill**: Competenze team IT

---

## Public Cloud

### Definizione

**Public Cloud** è un'infrastruttura cloud gestita da provider terzi e condivisa tra più organizzazioni (multi-tenant).

```
                     Internet
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    ┌───▼───┐       ┌───▼───┐      ┌───▼───┐
    │Client │       │Client │      │Client │
    │   A   │       │   B   │      │   C   │
    └───────┘       └───────┘      └───────┘
                        │
              ┌─────────▼─────────┐
              │   Public Cloud    │
              │  (AWS/Azure/GCP)  │
              │                   │
              │ [Shared Infra]    │
              └───────────────────┘
```

### Caratteristiche

✅ **Multi-tenancy**: Risorse condivise tra clienti  
✅ **Self-service**: Provisioning automatico  
✅ **Elasticità**: Scala on-demand  
✅ **Pay-per-use**: Costi variabili  
✅ **Accessibilità**: Disponibile via Internet  
✅ **Manutenzione**: Gestita dal provider  

### Principali Provider

#### Amazon Web Services (AWS)
- **Fondato**: 2006
- **Market Share**: ~32% (leader)
- **Regioni**: 30+
- **Servizi**: 200+
- **Clienti**: Netflix, Airbnb, NASA

#### Microsoft Azure
- **Fondato**: 2010
- **Market Share**: ~23%
- **Regioni**: 60+
- **Integrazione**: Office 365, Active Directory
- **Clienti**: Adobe, BMW, Samsung

#### Google Cloud Platform (GCP)
- **Fondato**: 2008
- **Market Share**: ~10%
- **Forza**: Big Data, ML/AI, Kubernetes
- **Clienti**: Spotify, Twitter, Snapchat

#### Altri Provider

- **Alibaba Cloud**: Leader in Asia (~6% market share, Q3 2023)
- **Oracle Cloud**: Focus database e enterprise (~3% market share, Q3 2023)
- **IBM Cloud**: Focus enterprise e hybrid (~2% market share, Q3 2023)
- **DigitalOcean**: Developer-friendly, semplice (<1% market share, Q3 2023)

### Vantaggi Public Cloud

✅ **Costi iniziali bassi**: No CapEx  
✅ **Scalabilità infinita**: Risorse illimitate  
✅ **Global reach**: Deploy mondiale in minuti  
✅ **Aggiornamenti automatici**: Sempre up-to-date  
✅ **Affidabilità**: SLA 99.9-99.99%  
✅ **Innovazione**: Accesso a servizi avanzati (AI, IoT)  

### Svantaggi Public Cloud

❌ **Controllo limitato**: Infrastruttura del provider  
❌ **Vendor lock-in**: Difficile migrazione  
❌ **Sicurezza percepita**: Dati fuori dal controllo diretto  
❌ **Compliance**: Possibili restrizioni normative  
❌ **Costi imprevedibili**: Bill shock se non monitorato  
❌ **Performance variabile**: Noisy neighbor effect  

### Use Case Public Cloud

1. **Startup**: No budget per datacenter
2. **Web/Mobile app**: Traffico variabile
3. **Development/Test**: Ambienti temporanei
4. **Big Data**: Workload analytics intensivi
5. **Disaster Recovery**: Backup offsite economico

### Esempio Architettura Public Cloud

```
                  ┌─────────────────────┐
                  │    Route 53 (DNS)   │
                  └──────────┬──────────┘
                             │
                  ┌──────────▼──────────┐
                  │  CloudFront (CDN)   │
                  └──────────┬──────────┘
                             │
              ┌──────────────▼──────────────┐
              │   Application Load Balancer │
              └──────────┬──────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
    ┌───▼───┐        ┌───▼───┐        ┌───▼───┐
    │ EC2   │        │ EC2   │        │ EC2   │  Auto-scaling
    │ (AZ-1)│        │ (AZ-2)│        │ (AZ-3)│
    └───┬───┘        └───┬───┘        └───┬───┘
        └────────────────┼────────────────┘
                         │
                  ┌──────▼──────┐
                  │     RDS     │  Multi-AZ
                  │ (Database)  │
                  └─────────────┘
```

---

## Private Cloud

### Definizione

**Private Cloud** è un'infrastruttura cloud dedicata a una singola organizzazione, ospitata on-premise o presso provider terzi.

```
┌──────────────────────────────────────┐
│         Organization Network         │
│                                      │
│   ┌──────────────────────────┐       │
│   │   Private Cloud          │       │
│   │   ┌────┐  ┌────┐  ┌────┐ │       │
│   │   │VM1 │  │VM2 │  │VM3 │ │       │
│   │   └────┘  └────┘  └────┘ │       │
│   │                          │       │
│   │   [Dedicated Hardware]   │       │
│   └──────────────────────────┘       │
│                                      │
│   Access: Only Company Employees     │
└──────────────────────────────────────┘
```

### Tipologie

#### 1. On-Premise Private Cloud
- Datacenter aziendale
- Hardware di proprietà
- Gestione interna completa

#### 2. Hosted Private Cloud
- Datacenter provider terzo
- Hardware dedicato
- Gestione ibrida (provider gestisce infra, cliente gestisce OS/app)

#### 3. Managed Private Cloud
- Provider gestisce tutto
- Cliente mantiene isolamento
- Esempio: AWS Outposts, Azure Stack

### Piattaforme Private Cloud

#### VMware vCloud/vSphere
```
┌────────────────────────────────┐
│   vCenter Server (Management)  │
├────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐  │
│  │ESXi 1│  │ESXi 2│  │ESXi N│  │
│  └──────┘  └──────┘  └──────┘  │
└────────────────────────────────┘
```

#### OpenStack
Open-source cloud platform:
- **Nova**: Compute
- **Swift**: Object storage
- **Cinder**: Block storage
- **Neutron**: Networking
- **Keystone**: Identity

```bash
# Deploy VM su OpenStack
openstack server create \
  --flavor m1.medium \
  --image ubuntu-22.04 \
  --network private \
  my-instance
```

#### Microsoft Azure Stack
Azure on-premise:
- API compatibili con Azure pubblico
- Hybrid applications
- Disconnected scenarios

#### Red Hat OpenShift
Kubernetes-based PaaS:
```
┌────────────────────────────┐
│   OpenShift Platform       │
│   ┌────────────────────┐   │
│   │   Kubernetes       │   │
│   └────────────────────┘   │
│   ┌────────────────────┐   │
│   │   RHEL CoreOS      │   │
│   └────────────────────┘   │
└────────────────────────────┘
```

### Vantaggi Private Cloud

✅ **Controllo completo**: Infrastruttura, sicurezza, compliance  
✅ **Sicurezza**: Dati non lasciano organizzazione  
✅ **Performance prevedibili**: No noisy neighbor  
✅ **Customizzazione**: Configurazione ad-hoc  
✅ **Compliance**: Requisiti normativi stringenti  

### Svantaggi Private Cloud

❌ **Costi elevati**: CapEx significativo  
❌ **Scalabilità limitata**: Vincolata all'hardware  
❌ **Gestione complessa**: Richiede team IT specializzato  
❌ **Time-to-market**: Setup più lento  
❌ **Manutenzione**: Responsabilità interna  
❌ **Obsolescenza**: Refresh hardware ogni 3-5 anni  

### Use Case Private Cloud

1. **Settore bancario/finanziario**: Compliance rigorosa
2. **Healthcare**: HIPAA, dati sensibili
3. **Governo**: Sicurezza nazionale
4. **Industria pesante**: Latenza critica, control systems
5. **Legacy applications**: Difficili da migrare

### Esempio Architettura Private Cloud

```
Corporate Datacenter

┌───────────────────────────────────────────┐
│  Management Layer (vCenter/OpenStack)     │
└─────────────────┬─────────────────────────┘
                  │
      ┌───────────┼───────────┐
      │           │           │
┌─────▼────┐ ┌────▼────┐ ┌───▼──────┐
│ Compute  │ │ Storage │ │ Network  │
│ Cluster  │ │ Cluster │ │ Fabric   │
│          │ │         │ │          │
│ 20 Hosts │ │ SAN     │ │ Switches │
│ 1000 VMs │ │ 500TB   │ │ Routers  │
└──────────┘ └─────────┘ └──────────┘
```

---

## Hybrid Cloud

### Definizione

**Hybrid Cloud** combina infrastrutture public e private cloud, integrate da tecnologia che permette portabilità di dati e applicazioni.

```
┌────────────────────────────────────────┐
│         Hybrid Cloud                   │
│                                        │
│  ┌──────────────┐   ┌───────────────┐  │
│  │Private Cloud │◄──┤  Integration  │  │
│  │ On-Premise   │   │    Layer      │  │
│  │              │   │  (VPN/DirectC)│  │
│  └──────────────┘   └────────┬──────┘  │
│                              │         │
│  ┌──────────────┐            │         │
│  │Public Cloud  │◄───────────┘         │
│  │ AWS/Azure/GCP│                      │
│  │              │                      │
│  └──────────────┘                      │
└────────────────────────────────────────┘
```

### Componenti Chiave

#### 1. Connettività
- **VPN**: Site-to-site VPN
- **Direct Connect**: Connessione dedicata (AWS Direct Connect, Azure ExpressRoute)
- **SD-WAN**: Software-defined networking

#### 2. Identity Management
- **Federated Identity**: SSO tra ambienti
- **Azure AD Connect**: Sync on-prem AD con Azure
- **AWS Directory Service**

#### 3. Data Synchronization
- **Replication**: Database replication
- **Data Pipeline**: ETL tra ambienti
- **Hybrid Storage**: (AWS Storage Gateway, Azure File Sync)

### Scenari Hybrid Cloud

#### 1. Cloud Bursting
```
Normal Load                Peak Load
┌────────────┐            ┌────────────┐
│  Private   │            │  Private   │ ← At capacity
│   Cloud    │            │   Cloud    │
│            │            │            │
│ 70% usage  │            │ 100% usage │
└────────────┘            └─────┬──────┘
                                │ Burst
                          ┌─────▼──────┐
                          │   Public   │ ← Handle overflow
                          │   Cloud    │
                          └────────────┘
```

**Esempio:**
- E-commerce: Black Friday spike
- Tax software: Periodo fiscale
- Streaming: Live events

#### 2. Disaster Recovery
```
Primary Site (On-Premise)       DR Site (Public Cloud)
┌────────────────────┐          ┌────────────────────┐
│  Production VMs    │          │  Standby VMs       │
│  ┌──┐ ┌──┐ ┌──┐    │──Replica─┤  ┌──┐ ┌──┐ ┌──┐    │
│  │ 1│ │ 2│ │ 3│    │          │  │ 1│ │ 2│ │ 3│    │
│  └──┘ └──┘ └──┘    │          │  └──┘ └──┘ └──┘    │
│   (Active)         │          │   (Standby)        │
└────────────────────┘          └────────────────────┘
                     Failover →  (Active on failure)
```

#### 3. Data Tiering
```
┌─────────────────────┐
│ Hot Data (Private)  │  ← Frequent access, low latency
│ Active databases    │
└──────────┬──────────┘
           │ Age/Archive
┌──────────▼──────────┐
│ Warm Data (Public)  │  ← Occasional access
│ S3 Standard         │
└──────────┬──────────┘
           │ 90 days
┌──────────▼──────────┐
│ Cold Data (Glacier) │  ← Rare access, compliance
│ Long-term archive   │
└─────────────────────┘
```

#### 4. Development in Cloud, Production On-Premise
```
Dev/Test (Public Cloud)      Production (Private)
┌────────────────┐           ┌────────────────┐
│  Agile dev     │  Deploy   │  Stable prod   │
│  Quick spin-up │──────────▶ Controlled    │
│  Cost-effective│           │  Secure        │
└────────────────┘           └────────────────┘
```

### Tecnologie Hybrid Cloud

#### AWS Hybrid
- **AWS Outposts**: AWS hardware on-premise
- **AWS Storage Gateway**: Hybrid storage
- **AWS Direct Connect**: Dedicated network
- **VMware Cloud on AWS**: VMware workload su AWS

#### Azure Hybrid
- **Azure Stack**: Azure on-premise
- **Azure Arc**: Manage any infrastructure from Azure
- **Azure ExpressRoute**: Private connection
- **Azure Site Recovery**: DR solution

#### Google Anthos
- **Multi-cloud Kubernetes**: Run su GCP, AWS, Azure, on-prem
- **Unified management**: Single pane of glass
- **Service mesh**: Istio-based

### Vantaggi Hybrid Cloud

✅ **Flessibilità**: Best of both worlds  
✅ **Ottimizzazione costi**: Right workload, right place  
✅ **Compliance**: Dati sensibili on-prem, resto in cloud  
✅ **Gradual migration**: Migrazione incrementale  
✅ **Disaster Recovery**: DR economico  
✅ **Scalabilità dinamica**: Cloud bursting  

### Svantaggi Hybrid Cloud

❌ **Complessità**: Gestione di due ambienti  
❌ **Networking**: Latenza, bandwidth, costi  
❌ **Security**: Superficie di attacco maggiore  
❌ **Orchestrazione**: Tool di gestione unificata necessari  
❌ **Skill**: Competenze su entrambi gli ambienti  

---

## Multi-Cloud

### Definizione

**Multi-Cloud** utilizza servizi di **multiple cloud provider** simultaneamente, per evitare lock-in e ottimizzare costi/performance.

```
┌──────────────────────────────────────┐
│        Multi-Cloud Strategy          │
│                                      │
│  ┌───────────┐  ┌───────────┐        │
│  │    AWS    │  │   Azure   │        │
│  │ Compute   │  │   AI/ML   │        │
│  │ Storage   │  │   AD      │        │
│  └─────┬─────┘  └─────┬─────┘        │
│        └────────┬──────┘             │
│            ┌────▼────┐               │
│            │   GCP   │               │
│            │Big Data │               │
│            │Analytics│               │
│            └─────────┘               │
└──────────────────────────────────────┘
```

### Differenza vs Hybrid

| Hybrid Cloud | Multi-Cloud |
|--------------|-------------|
| Private + Public | Multiple Public |
| Integration focus | Diversification focus |
| Single workload spans | Separate workloads |
| Portability driven | Best-of-breed driven |

### Strategie Multi-Cloud

#### 1. Diversification
Mitigare rischio vendor lock-in:
```
Primary: AWS (80%)
Backup:  Azure (20%) ← Disaster recovery, vendor leverage
```

#### 2. Best-of-Breed
Scegliere servizio migliore per ogni use case:
```
- Compute: AWS EC2 (maturo, variety)
- AI/ML: Google Cloud (TensorFlow, TPU)
- Enterprise: Azure (Office365, AD integration)
- Storage: AWS S3 (reliability, ecosystem)
```

#### 3. Geographic Coverage
```
Region          Provider
US East         AWS (us-east-1)
Europe          Azure (europe-west)
Asia-Pacific    Alibaba Cloud (shanghai)
```

#### 4. Cost Optimization
```
Workload          Provider (Reason)
CI/CD             GCP (preemptible VMs, cheap)
Production        AWS (reliability)
Dev/Test          DigitalOcean (simplicity, cost)
```

### Multi-Cloud Management Tools

#### Terraform (HashiCorp)
Infrastructure as Code multi-cloud:
```hcl
# AWS resources
resource "aws_instance" "web" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
}

# Azure resources
resource "azurerm_virtual_machine" "main" {
  name                  = "my-vm"
  location              = azurerm_resource_group.main.location
  resource_group_name   = azurerm_resource_group.main.name
  # ...
}

# GCP resources
resource "google_compute_instance" "default" {
  name         = "test"
  machine_type = "n1-standard-1"
  zone         = "us-central1-a"
  # ...
}
```

#### Kubernetes
Container orchestration multi-cloud:
```
┌────────────────────────────────┐
│      Kubernetes Cluster        │
│                                │
│  Nodes:                        │
│  - AWS EKS (us-east-1)         │
│  - Azure AKS (europe-west)     │
│  - GCP GKE (asia-southeast1)   │
│                                │
│  Application deploys anywhere  │
└────────────────────────────────┘
```

#### Cloud Management Platforms
- **CloudHealth**: Cost management, optimization
- **Morpheus**: Multi-cloud orchestration
- **RightScale**: Cloud management (Flexera)
- **Scalr**: Terraform management
- **Spot.io**: Cost optimization

### Vantaggi Multi-Cloud

✅ **No vendor lock-in**: Libertà di scelta  
✅ **Negotiation power**: Leverage con provider  
✅ **Best-of-breed**: Servizio ottimale per ogni need  
✅ **Risk mitigation**: Provider outage isolation  
✅ **Geographic reach**: Presenza globale ottimale  
✅ **Compliance**: Requisiti multi-region  

### Svantaggi Multi-Cloud

❌ **Complessità**: Gestione exponenzialmente più complessa  
❌ **Costi operativi**: Team skills su multiple piattaforme  
❌ **Data transfer**: Costoso tra cloud  
❌ **Consistency**: Differenze tra provider  
❌ **Security**: Multiple IAM, audit trails  
❌ **Tool sprawl**: Gestione di molteplici dashboard  

---

## Edge Computing

### Definizione

**Edge Computing** porta elaborazione e storage **vicino alla sorgente dei dati** anziché datacenter centralizzato.

```
                      ┌───────────────┐
                      │ Cloud Core    │
                      │ (Datacenter)  │
                      └───────┬───────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
        ┌─────▼─────┐   ┌─────▼─────┐  ┌─────▼─────┐
        │Edge Node  │   │Edge Node  │  │Edge Node  │
        │(Regional) │   │(Regional) │  │(Regional) │
        └─────┬─────┘   └─────┬─────┘  └─────┬─────┘
              │               │               │
        ┌─────▼──────┬────────▼────────┬──────▼─────┐
        │            │                 │            │
    ┌───▼──┐    ┌───▼──┐         ┌───▼──┐    ┌───▼──┐
    │Device│    │Device│   ...   │Device│    │Device│
    │(IoT) │    │(IoT) │         │(IoT) │    │(IoT) │
    └──────┘    └──────┘         └──────┘    └──────┘
```

### Caratteristiche Edge

- **Low Latency**: <10ms rispetto a 50-100ms cloud
- **Bandwidth Optimization**: Solo dati aggregati al cloud
- **Offline Operation**: Funziona senza connessione cloud
- **Real-time Processing**: Decisioni immediate

### Use Case Edge Computing

#### 1. Autonomous Vehicles
```
Car Sensors → Edge Processing (milliseconds) → Immediate action
           ↓
    Cloud Analytics (batch, non real-time)
```

#### 2. Smart Factories (Industry 4.0)
```
Machinery → Edge Gateway → Real-time monitoring
                         → Predictive maintenance
                         → Quality control
         ↓
   Cloud for long-term analytics
```

#### 3. Retail
```
Store Cameras → Edge AI → Customer behavior analysis
                        → Inventory management
                        → Loss prevention
```

#### 4. Healthcare
```
Medical Devices → Edge Processing → Real-time alerts
                                  → Patient monitoring
                ↓
         Cloud for records/ML training
```

#### 5. Smart Cities
```
Traffic Sensors → Edge → Traffic light optimization
                       → Emergency response
                       → Pollution monitoring
```

### Piattaforme Edge

#### AWS Wavelength
Edge computing nella rete 5G:
```
┌──────────────────────────┐
│  5G Network Edge         │
│  ┌────────────────────┐  │
│  │ AWS Wavelength     │  │
│  │ EC2, ECS           │  │
│  └────────────────────┘  │
└──────────────────────────┘
    Ultra-low latency (<10ms)
```

#### Azure Edge Zones
```
Azure Region → Metro Edge Zone → Carrier Edge
(Central)      (City)            (5G Tower)
```

#### Google Distributed Cloud Edge
```
Cloud → Regional edge → Customer edge
```

#### AWS IoT Greengrass
Edge runtime per IoT:
```python
# Lambda function runs on edge
def function_handler(event, context):
    # Process IoT data locally
    temperature = event['temperature']
    if temperature > 80:
        trigger_alert()
        send_to_cloud_if_connected(event)
```

### Edge vs Cloud vs Fog

```
Device Layer    ┌──────┐ ┌──────┐ ┌──────┐
(IoT Sensors)   │Device│ │Device│ │Device│
                └───┬──┘ └───┬──┘ └───┬──┘
                    └────┬────┘────────┘
                         │
Fog Layer       ┌────────▼────────┐
(Intermediate)  │  Fog Nodes      │  ← Local processing
                │  (Gateways)     │
                └────────┬────────┘
                         │
Edge Layer      ┌────────▼────────┐
(Regional)      │  Edge Datacenters│ ← Regional processing
                └────────┬────────┘
                         │
Cloud Layer     ┌────────▼────────┐
(Centralized)   │  Cloud Core     │  ← Central processing
                │  (Datacenter)   │
                └─────────────────┘
```

---

## Confronto e Scelta del Modello

### Tabella Comparativa Completa

| Caratteristica | Public | Private | Hybrid | Multi-Cloud | Edge |
|----------------|--------|---------|--------|-------------|------|
| **Costo Iniziale** | Basso | Alto | Medio | Medio | Medio |
| **Costo Operativo** | Variabile | Alto | Alto | Molto Alto | Medio |
| **Scalabilità** | Infinita | Limitata | Flessibile | Alta | Limitata |
| **Controllo** | Basso | Alto | Medio | Medio | Alto |
| **Sicurezza** | Condivisa | Massima | Bilanciata | Complessa | Alta |
| **Performance** | Variabile | Prevedibile | Mista | Ottimizzata | Ottima |
| **Latenza** | 50-100ms | <1ms local | Mista | Variabile | <10ms |
| **Compliance** | Dipende | Pieno | Selettivo | Complesso | Localizzato |
| **Complessità** | Bassa | Media | Alta | Molto Alta | Media |
| **Use Case** | Startup, Web | Banking, Gov | Enterprise | Global, Risk | IoT, Real-time |

### Decision Tree

```
Start: Nuova applicazione?
│
├─ Sì, startup/budget limitato
│  └─► PUBLIC CLOUD (AWS/Azure/GCP)
│
├─ Compliance rigorosa? (Banking, Healthcare)
│  └─► PRIVATE CLOUD (On-premise/Hosted)
│
├─ Applicazioni esistenti + cloud?
│  └─► HYBRID CLOUD (Gradual migration)
│
├─ Evitare vendor lock-in?
│  └─► MULTI-CLOUD (Best-of-breed strategy)
│
└─ Latenza critica? (IoT, Autonomous)
   └─► EDGE COMPUTING (Regional/Local processing)
```

### Esempi Reali

#### Netflix (Public Cloud - AWS)
- 100% AWS
- Global scaling
- Cost-effective per utilizzo variabile

#### Bank of America (Private Cloud)
- Datacenter proprietari
- Compliance finanziaria
- Controllo totale

#### GE (Hybrid Cloud)
- Manufacturing on-premise (latency, control)
- Analytics su Azure
- Predix IoT platform

#### Spotify (Multi-Cloud)
- Primary: GCP (data analytics, ML)
- Backup/DR: AWS
- Geographic optimization

#### Tesla (Edge Computing)
- Autonomous driving: edge processing in-car
- Fleet learning: data to cloud
- OTA updates: cloud to edge

---

## Best Practices per Scelta

### 1. Assessment Iniziale
```
□ Workload analysis (performance, scale, security)
□ Budget (CapEx vs OpEx tolerance)
□ Compliance requirements
□ Team skills
□ Business timeline
```

### 2. Pilot Project
Non migrare tutto subito:
```
Phase 1: Non-critical workload (dev/test)
Phase 2: Evaluate (cost, performance, operations)
Phase 3: Decide next steps
```

### 3. Exit Strategy
Sempre pianificare uscita:
- Data export procedures
- Application portability (containers, standards)
- Documentation

### 4. Security First
```
Public:   → Encryption, IAM, MFA, audit logs
Private:  → Physical security, access control
Hybrid:   → Secure connectivity (VPN/Direct Connect)
Multi:    → Unified identity, consistent policies
Edge:     → Device security, secure boot, updates
```

---

## Conclusioni

La scelta del modello di deployment è cruciale:

- **Public Cloud**: Default per startup e applicazioni moderne
- **Private Cloud**: Compliance, controllo, legacy
- **Hybrid Cloud**: Transizione graduale, best of both
- **Multi-Cloud**: Diversificazione rischio, best-of-breed
- **Edge Computing**: IoT, real-time, latency-critical

La tendenza è verso **hybrid/multi-cloud** come standard enterprise, con edge computing per use case specifici.

---

## Domande di Autovalutazione

1. Quali sono le principali differenze tra public e private cloud?
2. In quali scenari è appropriato un modello hybrid cloud?
3. Spiega il concetto di "cloud bursting"
4. Quali sono i rischi del multi-cloud?
5. Descrivi 3 use case per edge computing
6. Come sceglieresti tra public e private cloud per una banca?

---

## Esercizi Pratici

### Esercizio 1: Analisi Costo
Confronta costo public vs private cloud per:
- 50 VM, 2 vCPU, 4GB RAM ciascuna
- 10TB storage
- 100TB data transfer/mese
- 3 anni time horizon

### Esercizio 2: Architettura Hybrid
Progetta architettura hybrid per e-commerce:
- Database: Private (compliance)
- Web tier: Public (scalability)
- Analytics: Public (ML services)

---

## Risorse Aggiuntive

- [AWS Hybrid Cloud Solutions](https://aws.amazon.com/hybrid/)
- [Azure Hybrid Cloud](https://azure.microsoft.com/en-us/solutions/hybrid-cloud-app/)
- [Google Anthos](https://cloud.google.com/anthos)
- [Gartner Multi-Cloud Strategy](https://www.gartner.com/en/documents/3980382)
- [Edge Computing Consortium](https://www.edgecomputingworld.com/)
