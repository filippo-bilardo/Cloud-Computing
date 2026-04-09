# 1.2 Modelli di Servizio del Cloud Computing

## Indice
- [Introduzione ai Modelli di Servizio](#introduzione-ai-modelli-di-servizio)
- [IaaS - Infrastructure as a Service](#iaas---infrastructure-as-a-service)
- [PaaS - Platform as a Service](#paas---platform-as-a-service)
- [SaaS - Software as a Service](#saas---software-as-a-service)
- [FaaS/Serverless - Function as a Service](#faasserverless---function-as-a-service)
- [Confronto tra i Modelli](#confronto-tra-i-modelli)
- [Altri Modelli XaaS](#altri-modelli-xaas)

---

## Introduzione ai Modelli di Servizio

I **modelli di servizio** (Service Models) definiscono **cosa** viene fornito dal cloud provider e **chi** è responsabile della gestione dei vari livelli dello stack tecnologico.

### Lo Stack del Cloud Computing

Ogni applicazione cloud si basa su diversi livelli:

```
┌─────────────────────────────┐
│      Applications           │  ← Software applicativo
├─────────────────────────────┤
│      Data                   │  ← Database, file
├─────────────────────────────┤
│      Runtime                │  ← Framework, librerie
├─────────────────────────────┤
│      Middleware             │  ← Web server, app server
├─────────────────────────────┤
│      Operating System       │  ← Linux, Windows
├─────────────────────────────┤
│      Virtualization         │  ← Hypervisor, container
├─────────────────────────────┤
│      Servers                │  ← Hardware fisico
├─────────────────────────────┤
│      Storage                │  ← Dischi, SAN
├─────────────────────────────┤
│      Networking             │  ← Switch, router, firewall
└─────────────────────────────┘
```

I diversi modelli di servizio determinano **quale parte dello stack è gestita dal provider** e quale dal cliente.

### Modello di Responsabilità Condivisa

```
                On-Premise    IaaS         PaaS         SaaS
Applications    [Cliente]     [Cliente]    [Cliente]    [Provider]
Data            [Cliente]     [Cliente]    [Cliente]    [Provider]
Runtime         [Cliente]     [Cliente]    [Provider]   [Provider]
Middleware      [Cliente]     [Cliente]    [Provider]   [Provider]
OS              [Cliente]     [Cliente]    [Provider]   [Provider]
Virtualization  [Cliente]     [Provider]   [Provider]   [Provider]
Servers         [Cliente]     [Provider]   [Provider]   [Provider]
Storage         [Cliente]     [Provider]   [Provider]   [Provider]
Networking      [Cliente]     [Provider]   [Provider]   [Provider]
```

---

## IaaS - Infrastructure as a Service

### Definizione

**IaaS** fornisce risorse computazionali virtualizzate su cui i clienti possono installare e gestire i propri sistemi operativi, middleware e applicazioni.

Il provider gestisce: hardware fisico, virtualizzazione, storage, networking  
Il cliente gestisce: OS, runtime, middleware, applicazioni, dati

### Caratteristiche Principali

#### 1. **Risorse Virtualizzate**
- Virtual Machine (VM)
- Virtual Storage
- Virtual Network (VPC, Subnet)
- Load Balancer
- Firewall

#### 2. **Self-Service Provisioning**
- Creazione istanze in minuti
- Configurazione risorse via console/API
- Automazione tramite IaC (Infrastructure as Code)

#### 3. **Scalabilità Dinamica**
- Vertical scaling (resize VM)
- Horizontal scaling (aggiungere/rimuovere VM)
- Auto-scaling basato su metriche

#### 4. **Pay-as-you-go**
- Fatturazione al secondo/ora
- Costi basati su vCPU, RAM, storage, network utilizzati

### Componenti Tipici IaaS

#### Compute
- **Virtual Machines**: istanze con vCPU, RAM configurabili
  - AWS: EC2 (Elastic Compute Cloud)
  - Azure: Virtual Machines
  - GCP: Compute Engine

#### Storage
- **Block Storage**: volumi disco per VM
  - AWS: EBS (Elastic Block Store)
  - Azure: Managed Disks
  - GCP: Persistent Disk

- **Object Storage**: storage scalabile per file
  - AWS: S3 (Simple Storage Service)
  - Azure: Blob Storage
  - GCP: Cloud Storage

#### Networking
- **Virtual Network**: rete privata isolata
  - AWS: VPC (Virtual Private Cloud)
  - Azure: Virtual Network
  - GCP: VPC

- **Load Balancer**: distribuzione traffico
- **VPN Gateway**: connessione sicura on-premise-cloud
- **Direct Connect**: connessione dedicata

### Tipologie di Istanze

I provider offrono diversi "tipi" di VM ottimizzate per workload specifici:

#### General Purpose
- Bilanciamento CPU/RAM
- **AWS**: t3, m5, m6i
- **Use case**: web server, piccoli database

#### Compute Optimized
- Alto rapporto CPU/RAM
- **AWS**: c5, c6i
- **Use case**: batch processing, gaming, HPC

#### Memory Optimized
- Alto rapporto RAM/CPU
- **AWS**: r5, r6i, x1
- **Use case**: database in-memory, big data

#### Storage Optimized
- Alto throughput disco
- **AWS**: i3, d2
- **Use case**: data warehousing, log processing

#### GPU Instances
- GPU per calcolo parallelo
- **AWS**: p4, g5
- **Use case**: ML training, rendering 3D

### Esempio: Creare una VM AWS EC2

```bash
# Tramite AWS CLI
aws ec2 run-instances \
    --image-id ami-0abcdef1234567890 \
    --instance-type t3.medium \
    --key-name my-key-pair \
    --security-group-ids sg-0123456789abcdef0 \
    --subnet-id subnet-0bb1c79de3EXAMPLE \
    --count 1
```

```python
# Tramite Python Boto3 SDK
import boto3

ec2 = boto3.resource('ec2')

instance = ec2.create_instances(
    ImageId='ami-0abcdef1234567890',
    InstanceType='t3.medium',
    MinCount=1,
    MaxCount=1,
    KeyName='my-key-pair',
    SecurityGroupIds=['sg-0123456789abcdef0'],
    SubnetId='subnet-0bb1c79de3EXAMPLE'
)
```

### Vantaggi IaaS

✅ **Controllo completo** sull'OS e configurazione  
✅ **Flessibilità** massima nelle scelte tecnologiche  
✅ **Compatibilità** con applicazioni legacy  
✅ **Nessun lock-in** applicativo (solo infrastrutturale)  
✅ **Lift-and-shift** migration possibile  

### Svantaggi IaaS

❌ **Gestione OS** (patching, security)  
❌ **Complessità** operativa maggiore  
❌ **Responsabilità** su availability applicazione  
❌ **Tempo** per setup e configurazione  

### Use Case IaaS

1. **Development & Testing**
   - Ambienti temporanei
   - Clonazione rapida

2. **Web Hosting**
   - Server web configurabili
   - Scale based on traffic

3. **Backup & Recovery**
   - Storage offsite
   - Snapshot automatici

4. **High-Performance Computing**
   - Cluster temporanei
   - GPU on-demand

5. **Big Data Analytics**
   - Cluster Hadoop/Spark
   - Storage massivo

---

## PaaS - Platform as a Service

### Definizione

**PaaS** fornisce una piattaforma completa per sviluppare, testare e deployare applicazioni senza gestire l'infrastruttura sottostante.

Il provider gestisce: infrastruttura, OS, runtime, middleware  
Il cliente gestisce: applicazioni, dati

### Caratteristiche Principali

#### 1. **Astrazione dell'Infrastruttura**
- Nessuna gestione VM
- Patching automatico
- Scaling automatico

#### 2. **Development Tools Integrati**
- IDE online
- Version control integration
- CI/CD built-in

#### 3. **Middleware Preconfigurato**
- Web server
- Application server
- Database connections
- Message queues

#### 4. **Multi-Tenancy**
- Isolamento tra tenant
- Condivisione risorse

### Componenti Tipici PaaS

#### Application Platform
- **AWS**: Elastic Beanstalk
- **Azure**: App Service
- **GCP**: App Engine
- **Heroku**: Heroku Platform

#### Database Platform
- **AWS**: RDS, DynamoDB
- **Azure**: Azure SQL Database, Cosmos DB
- **GCP**: Cloud SQL, Firestore

#### Container Platform
- **AWS**: ECS (Elastic Container Service), EKS (Elastic Kubernetes Service)
- **Azure**: Azure Container Instances, AKS (Azure Kubernetes Service)
- **GCP**: Cloud Run, GKE (Google Kubernetes Engine)

#### Integration Platform
- **AWS**: API Gateway, SNS, SQS
- **Azure**: Logic Apps, Service Bus
- **GCP**: Cloud Pub/Sub, Workflows

### Esempio: Deploy su Heroku (PaaS)

```bash
# 1. Inizializza app
heroku create my-app

# 2. Deploy tramite Git
git push heroku main

# 3. Scale
heroku ps:scale web=2

# 4. Aggiungi database
heroku addons:create heroku-postgresql:hobby-dev
```

L'applicazione è online senza configurare VM, OS, web server!

### Esempio: Azure App Service

```bash
# Crea App Service Plan
az appservice plan create \
    --name myAppServicePlan \
    --resource-group myResourceGroup \
    --sku B1

# Deploy web app
az webapp create \
    --name myWebApp \
    --resource-group myResourceGroup \
    --plan myAppServicePlan

# Deploy da Git
az webapp deployment source config \
    --name myWebApp \
    --resource-group myResourceGroup \
    --repo-url https://github.com/user/repo \
    --branch main
```

### Vantaggi PaaS

✅ **Produttività** aumentata (focus su codice)  
✅ **Time-to-market** ridotto  
✅ **Gestione** semplificata (no OS patching)  
✅ **Scaling** automatico  
✅ **Built-in** HA e failover  
✅ **Costi** operativi ridotti  

### Svantaggi PaaS

❌ **Vendor lock-in** forte (API proprietarie)  
❌ **Controllo** limitato sull'infrastruttura  
❌ **Vincoli** sulle tecnologie supportate  
❌ **Customizzazione** limitata  
❌ **Migrazione** complessa tra provider  

### Use Case PaaS

1. **API Development**
   - RESTful APIs
   - Microservizi
   - GraphQL endpoints

2. **Web Applications**
   - E-commerce
   - Content Management
   - SaaS applications

3. **Mobile Backend**
   - Push notifications
   - User authentication
   - Data sync

4. **IoT Applications**
   - Device management
   - Data ingestion
   - Real-time analytics

---

## SaaS - Software as a Service

### Definizione

**SaaS** fornisce applicazioni complete accessibili via browser o API, completamente gestite dal provider.

Il provider gestisce: tutto lo stack  
Il cliente gestisce: solo i propri dati e configurazioni utente

### Caratteristiche Principali

#### 1. **Multi-Tenancy Architecture**
- Singola istanza serve multipli clienti
- Dati isolati tra tenant
- Customizzazione per tenant

#### 2. **Accessibilità Web**
- Browser-based
- Nessuna installazione locale
- Aggiornamenti automatici

#### 3. **Subscription-Based**
- Modello SaaS tipicamente subscription
- Per utente/mese
- Nessun costo di licenza upfront

#### 4. **Configurabilità**
- Personalizzazione workflow
- Branding customizzabile
- Integrazioni via API

### Categorie di Applicazioni SaaS

#### Productivity & Collaboration
- **Email & Calendar**: Gmail, Outlook 365
- **Office Suite**: Google Workspace, Microsoft 365
- **File Sharing**: Dropbox, Google Drive, OneDrive
- **Collaboration**: Slack, Microsoft Teams, Zoom

#### CRM (Customer Relationship Management)
- **Salesforce**: leader di mercato
- **HubSpot**: inbound marketing + CRM
- **Zoho CRM**: alternativa economica
- **Microsoft Dynamics 365**: enterprise CRM

#### ERP (Enterprise Resource Planning)
- **SAP S/4HANA Cloud**
- **Oracle NetSuite**
- **Microsoft Dynamics 365**
- **Workday**: HR & Finance

#### Project Management
- **Jira**: agile project management
- **Asana**: task management
- **Trello**: kanban boards
- **Monday.com**: work OS

#### HR & Recruiting
- **Workday**: HCM (Human Capital Management)
- **BambooHR**: HR per PMI
- **Greenhouse**: recruiting
- **LinkedIn Recruiter**

#### Marketing Automation
- **HubSpot Marketing Hub**
- **Marketo** (Adobe)
- **Pardot** (Salesforce)
- **Mailchimp**: email marketing

#### Accounting & Finance
- **QuickBooks Online**
- **Xero**
- **FreshBooks**
- **Wave** (free)

#### Development & IT
- **GitHub**: code hosting
- **GitLab**: DevOps platform
- **Jira Software**: development tracking
- **Datadog**: monitoring & analytics

### Esempio: Utilizzo SaaS Salesforce

Un'azienda adotta Salesforce:

1. **Signup**: registrazione online in minuti
2. **Configuration**: setup campi custom, workflow, automazioni
3. **Import Data**: migrazione dati esistenti via CSV/API
4. **User Onboarding**: creazione utenti, assegnazione licenze
5. **Integration**: connessione con Gmail, Slack, marketing tools
6. **Usage**: accesso via browser/mobile app

**Nessun server da gestire, nessun software da installare!**

### Vantaggi SaaS

✅ **Zero manutenzione** per utente finale  
✅ **Accessibilità** da qualsiasi dispositivo  
✅ **Aggiornamenti** automatici e continui  
✅ **Costo** prevedibile (subscription)  
✅ **Scalabilità** automatica (add/remove users)  
✅ **Quick deployment** (giorni vs mesi)  
✅ **Best practices** built-in  

### Svantaggi SaaS

❌ **Customizzazione** molto limitata  
❌ **Vendor lock-in** massimo  
❌ **Controllo dati** fuori dall'azienda  
❌ **Offline access** limitato  
❌ **Integration** può essere complessa  
❌ **Compliance** challenges in settori regolamentati  
❌ **Performance** dipendente da Internet  

### Use Case SaaS

1. **Email & Collaboration** (praticamente universale)
2. **CRM** per sales teams
3. **Project Management** per team distribuiti
4. **HR Management** per onboarding e payroll
5. **Accounting** per PMI
6. **Marketing** automation

---

## FaaS/Serverless - Function as a Service

### Definizione

**FaaS** (Function as a Service) o **Serverless** permette di eseguire codice in risposta a eventi senza gestire server. Il codice viene eseguito in container effimeri.

### Caratteristiche Principali

#### 1. **Event-Driven**
- Esecuzione triggered da eventi
- HTTP request, file upload, database change, scheduled task

#### 2. **Stateless**
- Ogni invocazione è indipendente
- Nessuno stato persistente nella funzione

#### 3. **Auto-Scaling Infinito**
- Scala automaticamente da 0 a migliaia di istanze
- Concorrenza gestita dal provider

#### 4. **Pay-per-Execution**
- Fatturazione per invocazione e tempo di esecuzione
- Nessun costo quando idle

#### 5. **Managed Execution Environment**
- Runtime preconfigurati (Node.js, Python, Java, etc.)
- Patching automatico

### Piattaforme FaaS

#### AWS Lambda
```javascript
// Lambda function handler
exports.handler = async (event) => {
    const name = event.queryStringParameters.name || 'World';
    return {
        statusCode: 200,
        body: JSON.stringify({
            message: `Hello ${name}!`
        })
    };
};
```

**Trigger supportati:**
- API Gateway (HTTP)
- S3 (object created/deleted)
- DynamoDB Streams
- SNS/SQS messages
- CloudWatch Events (scheduled)
- Alexa Skills

**Pricing:**
- Free tier: 1M richieste/mese, 400,000 GB-secondi
- $0.20 per 1M richieste
- $0.00001667 per GB-secondo

#### Azure Functions
```csharp
[FunctionName("HelloWorld")]
public static async Task<IActionResult> Run(
    [HttpTrigger(AuthorizationLevel.Function, "get", "post")] HttpRequest req,
    ILogger log)
{
    string name = req.Query["name"];
    return new OkObjectResult($"Hello, {name}!");
}
```

#### Google Cloud Functions
```python
def hello_world(request):
    name = request.args.get('name', 'World')
    return f'Hello {name}!'
```

### Event-Driven Architecture

```
┌──────────────┐
│ User uploads │
│  file to S3  │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌───────────────┐
│ S3 triggers  │────>│ Lambda resizes│
│   Lambda     │     │    image      │
└──────────────┘     └───────┬───────┘
                             │
                             ▼
                     ┌───────────────┐
                     │ Save to S3    │
                     │ thumbnails/   │
                     └───────────────┘
```

### Serverless Framework

Framework per deploy e gestione applicazioni serverless multi-cloud:

```yaml
# serverless.yml
service: my-service

provider:
  name: aws
  runtime: nodejs18.x
  region: eu-west-1

functions:
  hello:
    handler: handler.hello
    events:
      - http:
          path: hello
          method: get
  
  processImage:
    handler: handler.processImage
    events:
      - s3:
          bucket: my-bucket
          event: s3:ObjectCreated:*
```

Deploy:
```bash
serverless deploy
```

### Vantaggi FaaS

✅ **Zero gestione server**  
✅ **Auto-scaling** estremo  
✅ **Pay-per-use** reale (no idle cost)  
✅ **Time-to-market** velocissimo  
✅ **Focus** 100% su business logic  
✅ **Integrazione** facile con altri servizi cloud  

### Svantaggi FaaS

❌ **Cold start** latency (100ms-3s)  
❌ **Timeout** limitato (AWS: 15 min max)  
❌ **Stateless** constraint  
❌ **Vendor lock-in** altissimo  
❌ **Debugging** e testing complesso  
❌ **Costi** imprevedibili con alto volume  
❌ **Limited runtime** (CPU, RAM)  

### Use Case FaaS

1. **API Backend**
   - RESTful endpoints
   - Webhooks

2. **Data Processing**
   - Image/video processing
   - ETL pipelines
   - Log processing

3. **Scheduled Tasks**
   - Cron jobs
   - Backup automation

4. **Real-time Stream Processing**
   - IoT data
   - Clickstream analysis

5. **Chatbots & Alexa Skills**
   - NLP processing
   - Intent handling

---

## Confronto tra i Modelli

### Tabella Comparativa

| Caratteristica | IaaS | PaaS | SaaS | FaaS |
|---|---|---|---|---|
| **Controllo** | Alto | Medio | Basso | Basso |
| **Flessibilità** | Massima | Media | Minima | Media |
| **Gestione** | Cliente | Condivisa | Provider | Provider |
| **Scalabilità** | Manuale/Auto | Auto | Auto | Auto Infinita |
| **Time-to-Market** | Settimane | Giorni | Ore | Ore |
| **Skill Required** | Sys Admin | Developer | End User | Developer |
| **Lock-in** | Basso | Medio | Alto | Altissimo |
| **Costo Entry** | Medio | Basso | Molto Basso | Gratuito |
| **Use Case** | Legacy, Custom | Web/Mobile Apps | Business Apps | Event-Driven |

### Matrice di Responsabilità

```
Componente        On-Prem   IaaS    PaaS    SaaS    FaaS
─────────────────────────────────────────────────────────
Applications        You      You     You    Vendor   You*
Data                You      You     You    Vendor   You
Runtime             You      You    Vendor  Vendor  Vendor
Middleware          You      You    Vendor  Vendor  Vendor
OS                  You      You    Vendor  Vendor  Vendor
Virtualization      You     Vendor  Vendor  Vendor  Vendor
Servers             You     Vendor  Vendor  Vendor  Vendor
Storage             You     Vendor  Vendor  Vendor  Vendor
Networking          You     Vendor  Vendor  Vendor  Vendor

* In FaaS gestisci solo il codice della funzione
```

### Quando Scegliere Cosa?

#### Scegli **IaaS** se:
- Hai applicazioni legacy da migrare
- Necessiti controllo completo su OS e configurazione
- Hai compliance requirements specifici
- Vuoi flessibilità massima

**Esempi:**
- Migrazione database Oracle complesso
- Applicazione Windows legacy
- Testing con configurazioni OS custom

#### Scegli **PaaS** se:
- Sviluppi nuove applicazioni web/mobile
- Vuoi focus su codice, non infrastruttura
- Hai team di sviluppo ma non operations
- Vuoi deployment rapidi

**Esempi:**
- API REST per mobile app
- Web application Node.js/Python
- Microservizi containerizzati

#### Scegli **SaaS** se:
- Necessiti soluzioni business standard
- Non hai team IT per manutenzione
- Vuoi deployment immediato
- Il software standard soddisfa le tue esigenze

**Esempi:**
- Email aziendale
- CRM per sales team
- Project management
- Accounting

#### Scegli **FaaS** se:
- Hai workload event-driven
- Il traffico è intermittente o imprevedibile
- Vuoi pagare solo per esecuzioni
- Non hai bisogno di stato persistente

**Esempi:**
- Image thumbnail generation
- Webhook handlers
- Scheduled data exports
- IoT data processing

### Approccio Ibrido

Molte architetture moderne combinano più modelli:

```
┌─────────────────────────────────────────┐
│              Frontend (SaaS)            │
│         CloudFront + S3 Static          │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│           API Layer (FaaS)              │
│         AWS Lambda + API Gateway        │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│        Business Logic (PaaS)            │
│      ECS/EKS Container Platform         │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│        Database (PaaS/IaaS)             │
│     RDS (PaaS) / EC2 + DB (IaaS)        │
└─────────────────────────────────────────┘
```

---

## Altri Modelli XaaS

Oltre a IaaS, PaaS, SaaS e FaaS, esistono molti altri modelli "as a Service":

### DaaS - Database as a Service
- Database gestiti senza gestire VM
- **Esempi**: Amazon RDS, Azure SQL Database, MongoDB Atlas

### CaaS - Container as a Service
- Orchestrazione container gestita
- **Esempi**: AWS ECS/EKS, Azure AKS, Google GKE

### STaaS - Storage as a Service
- Storage cloud dedicato
- **Esempi**: Dropbox, Google Drive (consumer), AWS S3 (enterprise)

### DBaaS - Desktop as a Service
- Desktop virtuali nel cloud
- **Esempi**: Amazon WorkSpaces, Azure Virtual Desktop, Citrix

### SECaaS - Security as a Service
- Sicurezza gestita
- **Esempi**: Cloudflare, AWS WAF, Okta (identity)

### AIaaS - AI as a Service
- Modelli AI pre-addestrati
- **Esempi**: OpenAI API, AWS Rekognition, Azure Cognitive Services

### BaaS - Backend as a Service
- Backend mobile gestito
- **Esempi**: Firebase, AWS Amplify, Supabase

---

## Conclusioni

I modelli di servizio rappresentano diversi livelli di astrazione e responsabilità nel cloud computing:

- **IaaS** offre massimo controllo e flessibilità, ma richiede gestione
- **PaaS** bilancia produttività e controllo, ideale per sviluppatori
- **SaaS** massimizza semplicità d'uso, ideale per utenti business
- **FaaS** minimizza gestione e costi per workload event-driven

La scelta del modello giusto dipende da:
- **Competenze** del team
- **Requisiti** di controllo e customizzazione
- **Time-to-market** desiderato
- **Budget** disponibile
- **Natura** del workload

Molte organizzazioni moderne adottano un approccio **multi-model**, utilizzando il modello più appropriato per ogni workload specifico.

---

## Domande di Autovalutazione

1. Quali sono le principali differenze tra IaaS, PaaS e SaaS in termini di responsabilità?
2. Quando sceglieresti IaaS invece di PaaS?
3. Spiega il concetto di "cold start" in FaaS e quando può essere problematico
4. Quali sono i vantaggi e svantaggi del vendor lock-in in PaaS?
5. Fornisci 3 esempi di applicazioni SaaS che probabilmente usi quotidianamente
6. Come funziona il modello di pricing in FaaS rispetto a IaaS?
7. Descrivi uno scenario in cui un approccio multi-model (IaaS + PaaS + FaaS) sarebbe ideale

---

## Laboratorio Pratico

### Esercizio 1: Deploy Multi-Model
Crea una semplice applicazione utilizzando tutti e tre i modelli:
1. **IaaS**: Crea una VM e installa un database PostgreSQL
2. **PaaS**: Deploy un'API Node.js su Heroku o Azure App Service
3. **FaaS**: Crea una Lambda function per processing immagini
4. **SaaS**: Utilizza GitHub per version control

### Esercizio 2: Analisi Costi
Confronta i costi per hostare un'applicazione web per 1 anno:
- Scenario A: IaaS (EC2 + RDS)
- Scenario B: PaaS (Elastic Beanstalk)
- Scenario C: FaaS (Lambda + API Gateway + DynamoDB)

Considera: 100,000 richieste/giorno, 10GB database, 50GB storage

---

## Risorse Aggiuntive

- [AWS Service Models Overview](https://aws.amazon.com/types-of-cloud-computing/)
- [Azure Service Models](https://azure.microsoft.com/overview/what-is-iaas/)
- [Google Cloud Solutions](https://cloud.google.com/solutions)
- [Serverless Framework Documentation](https://www.serverless.com/framework/docs)
- [CNCF Cloud Native Landscape](https://landscape.cncf.io/)
