# 1.1 Concetti Fondamentali del Cloud Computing

## Indice
- [Definizione di Cloud Computing](#definizione-di-cloud-computing)
- [Storia ed Evoluzione del Cloud](#storia-ed-evoluzione-del-cloud)
- [Introduzione ai Datacenter](#introduzione-ai-datacenter)
- [Caratteristiche Essenziali](#caratteristiche-essenziali)
- [Virtualizzazione e Container](#virtualizzazione-e-container)
- [Vantaggi e Svantaggi del Cloud Computing](#vantaggi-e-svantaggi-del-cloud-computing)

---

## Definizione di Cloud Computing

Il **Cloud Computing** è un modello di erogazione di servizi IT che consente l'accesso on-demand a un pool condiviso di risorse computazionali configurabili (ad esempio reti, server, storage, applicazioni e servizi) che possono essere rapidamente fornite e rilasciate con un minimo sforzo di gestione o interazione con il fornitore del servizio.

### Definizione NIST
Secondo il **National Institute of Standards and Technology (NIST)**, il cloud computing è caratterizzato da:

> *"Un modello per abilitare l'accesso di rete ubiquo, conveniente e on-demand a un pool condiviso di risorse computazionali configurabili che possono essere rapidamente fornite e rilasciate con un minimo sforzo di gestione o interazione con il fornitore del servizio."*

### Concetti Chiave
- **On-demand self-service**: gli utenti possono ottenere risorse automaticamente senza richiedere interazione umana
- **Broad network access**: le risorse sono disponibili attraverso la rete e accessibili tramite meccanismi standard
- **Resource pooling**: le risorse del provider sono raggruppate per servire più consumatori
- **Rapid elasticity**: le capacità possono essere scalate elasticamente, spesso automaticamente
- **Measured service**: l'utilizzo delle risorse viene monitorato, controllato e rendicontato

---

## Storia ed Evoluzione del Cloud

### Timeline Storica

#### Anni '60: Le Origini
- **1961**: John McCarthy al MIT suggerisce che il computing potrebbe essere organizzato come una "utility pubblica"
- **1969**: J.C.R. Licklider sviluppa ARPANET, precursore di Internet

#### Anni '90: Le Basi
- **1997**: Il termine "cloud computing" viene utilizzato per la prima volta da Ramnath Chellappa
- **1999**: Salesforce.com lancia applicazioni aziendali via web, pioniere del SaaS

#### Anni 2000: La Nascita del Cloud Moderno
- **2002**: Amazon lancia AWS (Amazon Web Services) con servizi di storage e compute
- **2006**: Amazon EC2 (Elastic Compute Cloud) diventa pubblicamente disponibile
- **2006**: Google introduce Google Apps (ora Google Workspace)
- **2008**: Google lancia Google App Engine
- **2009**: Microsoft lancia Azure (inizialmente Windows Azure)

#### Anni 2010: Maturità e Diffusione
- **2010**: Microsoft rinomina la piattaforma in Windows Azure
- **2011**: IBM introduce SmartCloud
- **2013**: Google Compute Engine diventa disponibile
- **2014**: Microsoft rinomina Windows Azure in Microsoft Azure
- **2015**: AWS raggiunge 10 miliardi di dollari di fatturato annuo

#### 2020 e Oltre: Cloud-Native e Multi-Cloud
- Esplosione di adozione durante la pandemia COVID-19
- Crescita di strategie multi-cloud e hybrid cloud
- Enfasi su edge computing e serverless
- Integrazione con AI/ML e IoT

---

## Introduzione ai Datacenter

### Cos'è un Datacenter?

Un **datacenter** è una struttura fisica utilizzata per ospitare sistemi informatici e componenti associati, come sistemi di telecomunicazioni e storage. Un datacenter cloud moderno è progettato per offrire alta disponibilità, scalabilità e sicurezza.

### Componenti di un Datacenter

#### 1. Infrastruttura IT
- **Server**: rack di server fisici (spesso migliaia o decine di migliaia)
- **Storage**: sistemi di storage SAN/NAS per petabyte di dati
- **Networking**: switch, router, load balancer, firewall
- **Cablaggio**: fibra ottica e cavi ethernet strutturati

#### 2. Infrastruttura di Supporto
- **Alimentazione elettrica**: 
  - Connessioni multiple alla rete elettrica
  - UPS (Uninterruptible Power Supply)
  - Generatori diesel di backup
  - PDU (Power Distribution Unit)

- **Raffreddamento**:
  - Sistemi HVAC (Heating, Ventilation, Air Conditioning)
  - Free cooling (raffreddamento naturale)
  - Liquid cooling per server ad alte prestazioni
  - Containment (hot aisle/cold aisle)

- **Sicurezza fisica**:
  - Controllo accessi biometrico
  - Videosorveglianza 24/7
  - Recinzioni e barriere fisiche
  - Guardie di sicurezza

- **Rilevamento e soppressione incendi**:
  - Sistemi di rilevamento fumo
  - Sistemi di soppressione a gas inerte

### Tier di Classificazione dei Datacenter

L'**Uptime Institute** ha definito 4 tier di datacenter:

#### Tier 1: Basic Capacity
- Disponibilità: 99.671% (28.8 ore/anno di downtime)
- Singolo percorso per alimentazione e raffreddamento
- Nessuna ridondanza
- Manutenzione richiede shutdown

#### Tier 2: Redundant Capacity Components
- Disponibilità: 99.741% (22 ore/anno di downtime)
- Singolo percorso con componenti ridondanti
- Parziale ridondanza (N+1)
- Manutenzione richiede shutdown parziale

#### Tier 3: Concurrently Maintainable
- Disponibilità: 99.982% (1.6 ore/anno di downtime)
- Percorsi multipli (uno attivo)
- Componenti ridondanti
- Manutenzione senza shutdown

#### Tier 4: Fault Tolerant
- Disponibilità: 99.995% (0.4 ore/anno di downtime)
- Percorsi multipli attivi (2N+1)
- Tolleranza ai guasti
- Nessun punto singolo di fallimento

### Datacenter dei Cloud Provider

I principali cloud provider gestiscono datacenter in tutto il mondo:

- **AWS**: oltre 30 regioni geografiche, 96+ zone di disponibilità
- **Microsoft Azure**: 60+ regioni, più di qualsiasi altro provider cloud
- **Google Cloud**: 35+ regioni, 106+ zone

### Regioni e Zone di Disponibilità

#### Regione (Region)
- Area geografica che contiene uno o più datacenter
- Esempio: `eu-west-1` (Irlanda), `us-east-1` (Virginia)
- Isolamento geografico per disaster recovery

#### Zona di Disponibilità (Availability Zone)
- Uno o più datacenter discreti all'interno di una regione
- Connessioni a bassa latenza tra zone della stessa regione
- Isolamento da guasti (alimentazione, rete separata)
- Alta disponibilità distribuendo risorse su più zone

---

## Caratteristiche Essenziali

### 1. On-Demand Self-Service

Gli utenti possono **provision automaticamente** le risorse secondo necessità senza richiedere interazione umana con il provider.

**Esempi:**
- Avviare una VM in pochi minuti tramite console web
- Aumentare storage automaticamente quando serve
- Creare un database con pochi click

**Vantaggi:**
- Riduzione del time-to-market
- Agilità operativa
- Autonomia degli sviluppatori

### 2. Elasticità (Elastic Scalability)

La capacità di **scalare risorse dinamicamente** in base alla domanda, sia in aumento (scale-up/out) che in diminuzione (scale-down/in).

#### Scaling Verticale (Scale-Up/Down)
- Aggiungere più CPU, RAM, storage a un singolo server
- Limiti fisici del hardware
- Spesso richiede downtime

#### Scaling Orizzontale (Scale-Out/In)
- Aggiungere o rimuovere istanze/server
- Praticamente illimitato
- Può essere fatto senza downtime con load balancing

**Auto-Scaling:**
```
Se utilizzo CPU > 80% per 5 minuti:
    → Aggiungi 2 istanze
Se utilizzo CPU < 30% per 10 minuti:
    → Rimuovi 1 istanza
```

### 3. Pay-Per-Use (Consumption-Based Pricing)

Gli utenti pagano solo per le risorse effettivamente utilizzate, simile a una bolletta elettrica.

**Modelli di pricing:**
- **Pay-as-you-go**: tariffazione oraria o al secondo
- **Reserved**: impegno a lungo termine con sconto
- **Spot/Preemptible**: istanze a basso costo ma interrompibili

**Esempio AWS EC2:**
- t3.medium on-demand: $0.0416/ora
- t3.medium reserved 1 anno: $0.027/ora (35% sconto)
- t3.medium spot: $0.0125/ora (70% sconto, ma può essere interrotta)

### 4. Resource Pooling (Multi-Tenancy)

Le risorse fisiche del provider sono **condivise tra più clienti** (tenant), con isolamento logico tramite virtualizzazione.

**Benefici:**
- Maggiore efficienza delle risorse
- Costi ridotti tramite economie di scala
- Miglior utilizzo dell'hardware

**Isolamento:**
- Virtualizzazione assicura che i tenant non possano accedere ai dati degli altri
- VLAN e VPC per isolamento di rete
- Encryption per protezione dati

### 5. Broad Network Access

Servizi accessibili attraverso **la rete tramite meccanismi standard** (HTTP/HTTPS) da diversi dispositivi.

**Accesso tramite:**
- Browser web (console)
- API REST/GraphQL
- CLI (Command Line Interface)
- SDK per vari linguaggi (Python, Java, JavaScript, etc.)
- Mobile app

### 6. Measured Service

L'utilizzo delle risorse è **monitorato, controllato e reportato**, fornendo trasparenza per provider e consumatori.

**Metriche tipiche:**
- Ore di compute utilizzate
- GB di storage consumati
- GB di data transfer in uscita
- Numero di richieste API
- Numero di transazioni database

**Strumenti:**
- AWS CloudWatch
- Azure Monitor
- Google Cloud Monitoring
- Dashboard di billing dettagliate

---

## Virtualizzazione e Container

### Virtualizzazione

La **virtualizzazione** è la tecnologia fondamentale che abilita il cloud computing, permettendo di eseguire multiple istanze di sistemi operativi su un singolo server fisico.

#### Componenti della Virtualizzazione

1. **Host OS**: Sistema operativo del server fisico
2. **Hypervisor**: Software che gestisce le VM
   - Type 1 (bare-metal): ESXi, Hyper-V, KVM
   - Type 2 (hosted): VirtualBox, VMware Workstation
3. **Guest OS**: Sistema operativo di ogni VM
4. **Virtual Hardware**: CPU, RAM, disco, rete virtualizzati

#### Vantaggi della Virtualizzazione
- **Consolidamento**: più VM su meno server fisici
- **Isolamento**: guasti in una VM non impattano le altre
- **Portabilità**: VM possono essere migrate tra host
- **Snapshot**: stato della VM può essere salvato e ripristinato

### Container

I **container** rappresentano un'evoluzione della virtualizzazione, fornendo isolamento a livello di processo anziché di sistema operativo completo.

#### Caratteristiche dei Container

- **Lightweight**: condividono il kernel dell'OS host
- **Rapidi**: avvio in secondi vs minuti delle VM
- **Portabili**: "build once, run anywhere"
- **Efficienza**: maggiore densità (più container per host)

#### Container vs Virtual Machine

| Caratteristica | Virtual Machine | Container |
|---|---|---|
| **OS** | OS completo per ogni VM | Condivide kernel host |
| **Dimensione** | GB (gigabyte) | MB (megabyte) |
| **Startup** | Minuti | Secondi |
| **Isolamento** | Forte (hypervisor) | Medio (namespace/cgroups) |
| **Performance** | Overhead ~10-15% | Overhead ~1-2% |
| **Portabilità** | Buona | Eccellente |

#### Tecnologie Container
- **Docker**: runtime container più popolare
- **containerd**: runtime low-level
- **Kubernetes**: orchestrazione container
- **Podman**: alternativa daemon-less a Docker

---

## Vantaggi e Svantaggi del Cloud Computing

### Vantaggi

#### 1. **Riduzione dei Costi Iniziali (CapEx → OpEx)**
- Nessun investimento iniziale in hardware
- Passaggio da Capital Expenditure a Operational Expenditure
- Pay-per-use elimina sprechi

**Esempio:**
```
On-Premise:
- Server: €50,000
- Storage: €30,000
- Networking: €20,000
- Datacenter: €100,000+
TOTALE: €200,000+ upfront

Cloud:
- €500/mese per iniziare
- Scala con la crescita
```

#### 2. **Scalabilità e Elasticità**
- Scale-up/down in minuti
- Handle peak traffic senza sovra-provisioning
- Auto-scaling automatico

#### 3. **Focus sul Business**
- IT team si concentra su valore business, non su manutenzione infrastruttura
- Meno tempo su patching, aggiornamenti hardware
- Innovazione più rapida

#### 4. **Alta Disponibilità e Disaster Recovery**
- SLA del 99.9% - 99.99%
- Backup automatici
- Distribuzione geografica
- Failover automatico

#### 5. **Accesso Globale**
- Deploy in multiple regioni in minuti
- Bassa latenza per utenti globali
- CDN integrato

#### 6. **Sicurezza Enterprise-Grade**
- Certificazioni: ISO 27001, SOC 2, PCI DSS
- Security team dedicati
- Patching e aggiornamenti automatici
- Encryption at rest e in transit

#### 7. **Innovazione Rapida**
- Accesso a servizi AI/ML, IoT, Big Data
- Sperimentazione a basso costo
- Time-to-market ridotto

#### 8. **Sostenibilità**
- Datacenter ottimizzati per efficienza energetica
- Utilizzo condiviso delle risorse
- Impegni carbon-neutral dei provider

### Svantaggi

#### 1. **Vendor Lock-In**
- Difficoltà a migrare tra provider
- Servizi proprietari non portabili
- API specifiche del vendor

**Mitigazione:**
- Multi-cloud strategy
- Uso di standard aperti
- Container e Kubernetes

#### 2. **Dipendenza dalla Connettività**
- Richiede connessione Internet affidabile
- Latenza per applicazioni sensibili
- Downtime se connessione cade

#### 3. **Sicurezza e Privacy**
- Dati fuori dal controllo diretto
- Compliance e regolamentazioni (GDPR)
- Rischi di data breach condivisi

#### 4. **Costi Variabili e Potenzialmente Imprevedibili**
- Bill shock se non monitorato
- Difficile prevedere costi esatti
- Costi di data transfer possono sommarsi

**Best Practice:**
- Budget alerts
- Cost monitoring tools
- Reserved instances per workload prevedibili

#### 5. **Prestazioni Variabili**
- "Noisy neighbor" problem in ambienti multi-tenant
- Latenza variabile
- Throttling in caso di spike

#### 6. **Limitato Controllo**
- Meno controllo su infrastruttura sottostante
- Configurazioni limitate in servizi managed
- Dipendenza dal provider per fix/updates

#### 7. **Complessità di Migrazione**
- Migrazione legacy applications può essere complessa
- Refactoring necessario per applicazioni non cloud-native
- Downtime durante migrazione

#### 8. **Problemi di Governance**
- Shadow IT: dipartimenti che creano risorse non autorizzate
- Difficoltà nel tracking delle risorse
- Compliance multi-regione

---

## Quando Usare il Cloud?

### ✅ Scenari Ideali per il Cloud

1. **Startup e nuovi progetti**
   - Zero investimento iniziale
   - Crescita rapida e imprevedibile

2. **Workload variabili**
   - Picchi stagionali (e-commerce, tax software)
   - Testing e sviluppo

3. **Applicazioni web e mobile**
   - Scalabilità globale
   - Alta disponibilità

4. **Big Data e Analytics**
   - Risorse intensive temporanee
   - Storage massivo

5. **Disaster Recovery**
   - Backup offsite
   - Costi ridotti vs datacenter secondario

### ❌ Scenari Meno Adatti

1. **Workload ultra-sensibili alla latenza**
   - Trading ad alta frequenza
   - Real-time control systems

2. **Regolamentazioni estreme**
   - Dati che non possono lasciare il paese
   - Settori iper-regolamentati

3. **Workload stabili a lungo termine**
   - Può essere più economico on-premise dopo 3-5 anni
   - Depreciation favorevole

---

## Conclusioni

Il Cloud Computing ha trasformato il modo in cui le organizzazioni progettano, implementano e gestiscono l'infrastruttura IT. Comprendere i concetti fondamentali - dalla definizione NIST alle caratteristiche essenziali, dalla virtualizzazione ai container, dai vantaggi agli svantaggi - è cruciale per prendere decisioni informate sull'adozione del cloud.

Nei prossimi capitoli, esploreremo i diversi modelli di servizio (IaaS, PaaS, SaaS) e i modelli di deployment (Public, Private, Hybrid Cloud) che costituiscono l'ecosistema cloud moderno.

---

## Domande di Autovalutazione

1. Quali sono le 5 caratteristiche essenziali del cloud computing secondo il NIST?
2. Qual è la differenza tra scaling verticale e orizzontale?
3. Confronta una VM e un container in termini di dimensione, tempo di avvio e isolamento
4. Spiega il concetto di "pay-per-use" e come differisce dai modelli tradizionali IT
5. Quali sono le differenze tra un datacenter Tier 3 e Tier 4?
6. Descrivi 3 vantaggi e 3 svantaggi del cloud computing
7. Cosa si intende per "vendor lock-in" e come può essere mitigato?
8. Quando il cloud è la scelta migliore? Quando invece non lo è?

---

## Risorse Aggiuntive

- [NIST Definition of Cloud Computing](https://csrc.nist.gov/publications/detail/sp/800-145/final)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [Azure Architecture Center](https://docs.microsoft.com/azure/architecture/)
- [Google Cloud Architecture Framework](https://cloud.google.com/architecture/framework)
