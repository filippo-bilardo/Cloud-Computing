# Cloud Computing - Indice del Corso

Questo corso offre una formazione completa sulle tecnologie cloud computing, dalla teoria fondamentale alle applicazioni pratiche. Gli studenti acquisiranno competenze nelle principali piattaforme (AWS, Azure, GCP) e nelle tecnologie emergenti come container, Kubernetes e serverless computing. Il corso combina lezioni teoriche con laboratori pratici intensivi, preparando gli studenti a progettare, implementare e gestire infrastrutture cloud-native in ambienti aziendali moderni.

---

## **Modulo 1: Introduzione al Cloud Computing**

### 1.1 [Concetti Fondamentali](01-Introduzione_al_Cloud_Computing/01-Concetti_Fondamentali.md)
- Definizione di Cloud Computing
- Storia ed evoluzione del Cloud
- Introduzione ai Datacenter
- Caratteristiche essenziali (on-demand, elasticità, pay-per-use)
- Virtualizzazione e Container
- Vantaggi e svantaggi del Cloud Computing

### 1.2 [Modelli di Servizio](01-Introduzione_al_Cloud_Computing/02-Modelli_di_Servizio.md)
- **IaaS** (Infrastructure as a Service)
- **PaaS** (Platform as a Service)
- **SaaS** (Software as a Service)
- **FaaS/Serverless** (Function as a Service)
- Confronto tra i modelli

### 1.3 [Modelli di Deployment](01-Introduzione_al_Cloud_Computing/03-Modelli_di_Deployment.md)
- Public Cloud
- Private Cloud
- Hybrid Cloud
- Multi-Cloud
- Edge Computing

---

## **Modulo 2: Virtualizzazione**

### 2.1 [Fondamenti di Virtualizzazione](02-Virtualizzazione/01-Fondamenti_di_Virtualizzazione.md)
- Storia e evoluzione della virtualizzazione
- Concetti di astrazione hardware
- Benefici della virtualizzazione (consolidamento, isolamento, flessibilità)
- Virtualizzazione vs emulazione
- Virtualizzazione hardware vs software
- Paravirtualizzazione

### 2.2 [Hypervisor e Tecnologie](02-Virtualizzazione/02-Hypervisor_e_Tecnologie.md)
- **Type 1 Hypervisor (Bare-metal)**
  - VMware ESXi
  - Microsoft Hyper-V
  - KVM (Kernel-based Virtual Machine)
  - Xen
  - Proxmox VE
- **Type 2 Hypervisor (Hosted)**
  - VMware Workstation/Fusion
  - Oracle VirtualBox
  - QEMU
- Confronto tra Type 1 e Type 2

### 2.3 [Architettura delle Virtual Machine](02-Virtualizzazione/03-Architettura_delle_Virtual_Machine.md)
- Struttura di una VM
- Virtual CPU (vCPU)
- Virtual Memory
- Virtual Network Interface
- Virtual Disk
- Snapshot e cloning
- Template di VM

### 2.4 [Gestione delle Risorse](02-Virtualizzazione/04-Gestione_delle_Risorse.md)
- CPU scheduling
- Memory management (ballooning, swapping)
- Storage management
- Network virtualization (vSwitch, VLAN)
- Resource pooling e allocation
- Quality of Service (QoS)

### 2.5 [Virtualizzazione della Rete](02-Virtualizzazione/05-Virtualizzazione_della_Rete.md)
- Virtual Switch
- VLAN e network segmentation
- Software-Defined Networking (SDN)
- Network Function Virtualization (NFV)
- Virtual Router e Virtual Firewall

### 2.6 [Virtualizzazione dello Storage](02-Virtualizzazione/06-Virtualizzazione_dello_Storage.md)
- Storage Area Network (SAN)
- Network Attached Storage (NAS)
- Virtual SAN
- Thin provisioning
- Storage migration
- Data deduplication

### 2.7 [Virtualizzazione nel Cloud](02-Virtualizzazione/07-Virtualizzazione_nel_Cloud.md)
- Come i cloud provider usano la virtualizzazione
- Nested virtualization
- Hardware-assisted virtualization (Intel VT-x, AMD-V)
- Virtual Machine su AWS (EC2), Azure, GCP
- Bare-metal instances

### 2.8 [Performance e Ottimizzazione](02-Virtualizzazione/08-Performance_e_Ottimizzazione.md)
- Overhead della virtualizzazione
- Performance tuning
- Right-sizing delle VM
- NUMA (Non-Uniform Memory Access)
- SR-IOV (Single Root I/O Virtualization)
- GPU virtualization

### 2.9 [Sicurezza nella Virtualizzazione](02-Virtualizzazione/09-Sicurezza_nella_Virtualizzazione.md)
- VM isolation
- Hypervisor security
- VM escape attacks
- Patch management
- Secure boot
- Encrypted VM

### 2.10 [Container vs Virtual Machine](02-Virtualizzazione/10-Container_vs_Virtual_Machine.md)
- Architettura a confronto
- Vantaggi e svantaggi
- Use cases appropriati
- Overhead e performance
- Portabilità

### 2.11 [Esercitazione 1 con VirtualBox](02-Virtualizzazione/11-Esercitazione_1_con_VirtualBox.md)
- Abilitazione Virtualizzazione nel BIOS
- Creare una Macchina Virtuale
- Installare Windows su VM
- Installare Linux su VM
- Installare VMware Tools
- Gestione Quotidiana delle VM

---

## **Modulo 3: Architetture Cloud e Container**

### 3.1 Container
- Introduzione ai container
- Namespaces e cgroups in Linux
- Container runtime (Docker, containerd, CRI-O)
- Differenze tra container e VM

### 3.2 Docker
- Docker architecture
- Immagini e Dockerfile
- Container lifecycle
- Docker Registry e Docker Hub
- Docker Compose
- Docker networking e volumes

### 3.3 Orchestrazione con Kubernetes
- Architettura di Kubernetes
- Componenti del Control Plane
- Componenti dei Worker Nodes
- Pod, ReplicaSet, Deployment
- Service e Ingress
- ConfigMap e Secret
- StatefulSet e DaemonSet
- Persistent Volumes

### 3.4 Microservizi
- Architettura monolitica vs microservizi
- Design patterns per microservizi
- API Gateway e Service Mesh
- Comunicazione tra microservizi (REST, gRPC, message queues)
- Service discovery
- Circuit breaker pattern

---

## **Modulo 4: Principali Provider Cloud**

### 4.1 [Amazon Web Services (AWS)](04-Principali_Provider_Cloud/01-Amazon_Web_Services.md)
- Storia ed evoluzione di AWS
- Architettura globale: Regioni, Availability Zones, Edge Locations
- **Compute**: EC2, Lambda, ECS, EKS, Fargate, Elastic Beanstalk
- **Storage**: S3, EBS, EFS, Glacier
- **Database**: RDS, Aurora, DynamoDB, ElastiCache, Redshift
- **Networking**: VPC, Route 53, CloudFront, Load Balancing
- **Security**: IAM, KMS, Secrets Manager, WAF, Shield
- **Monitoring**: CloudWatch, CloudTrail, X-Ray
- **DevOps**: CodePipeline, CodeBuild, CloudFormation
- **Analytics**: Athena, EMR, Kinesis
- **AI/ML**: SageMaker, Rekognition, Comprehend
- Modelli di pricing e Reserved Instances
- Well-Architected Framework
- AWS CLI e esempi pratici
- Certificazioni AWS
- Troubleshooting

### 4.2 [Microsoft Azure](04-Principali_Provider_Cloud/02-Microsoft_Azure.md)
- Storia ed evoluzione di Azure
- Architettura globale: Geografie, Regioni, Availability Zones
- **Compute**: Virtual Machines, App Service, Functions, AKS, Container Instances
- **Storage**: Blob Storage, Azure Files, Disk Storage
- **Database**: SQL Database, Cosmos DB, PostgreSQL, MySQL
- **Networking**: Virtual Network, Load Balancer, Application Gateway
- **Security**: Azure AD, Key Vault, Security Center
- **Monitoring**: Azure Monitor, Application Insights
- **DevOps**: Azure DevOps, Pipelines
- Azure Resource Manager (ARM) e Bicep
- Pricing e cost management
- Well-Architected Framework Azure
- Azure CLI e PowerShell
- Certificazioni Azure
- Esempi pratici completi

### 4.3 [Google Cloud Platform (GCP)](04-Principali_Provider_Cloud/03-Google_Cloud_Platform.md)
- Storia ed evoluzione di GCP
- Architettura globale: Regioni e Zone
- **Compute**: Compute Engine, Cloud Functions, GKE, Cloud Run
- **Storage**: Cloud Storage, Persistent Disk
- **Database**: Cloud SQL, Cloud Spanner, Firestore, Bigtable
- **Networking**: VPC, Cloud Load Balancing, Cloud CDN
- **Big Data**: BigQuery, Dataflow, Pub/Sub
- **AI/ML**: Vertex AI, AutoML, Vision AI, Natural Language
- Pricing e sustained use discounts
- gcloud CLI e esempi pratici
- Certificazioni GCP
- Best practices

### 4.4 [Altri Provider Cloud](04-Principali_Provider_Cloud/04-Altri_Provider.md)
- **Provider tradizionali**: IBM Cloud, Oracle Cloud, Alibaba Cloud
- **Developer-friendly**: DigitalOcean, Linode, Hetzner Cloud
- **Europei/Italiani**: OVHcloud, Scaleway, Aruba Cloud
- **Specializzati**: Heroku, Netlify, Vercel, Cloudflare Workers, Railway
- Tabella comparativa multi-cloud
- Strategie multi-cloud e hybrid cloud
- Vendor lock-in: rischi e mitigazione
- Quando scegliere provider alternativi
- Cost comparison
- Portabilità e interoperabilità

---

## **Modulo 5: Storage e Database nel Cloud**

### 5.1 [Storage](05-Storage_e_Database_nel_Cloud/01-Storage.md)
- **Tipi di Storage**: Object (S3), Block (EBS), File (EFS)
- Storage classes & tiering
- Lifecycle policies
- Versioning e replication
- Encryption at-rest
- Best practices

### 5.2 [Database Relazionali](05-Storage_e_Database_nel_Cloud/02-Database_Relazionali.md)
- AWS RDS, Aurora
- Azure SQL Database
- Google Cloud SQL
- Multi-AZ & Read Replicas
- Backup & Point-in-Time Recovery
- Performance optimization

### 5.3 [Database NoSQL](05-Storage_e_Database_nel_Cloud/03-Database_NoSQL.md)
- **Key-Value**: DynamoDB, Redis
- **Document**: MongoDB, CosmosDB, Firestore
- **Column-family**: Cassandra, Bigtable
- **Graph**: Neo4j, Neptune
- Quando usare NoSQL vs SQL
- Best practices NoSQL

---

## **Modulo 6: Networking nel Cloud**

### 6.1 [Reti Virtuali](06-Networking_nel_Cloud/01-Reti_Virtuali.md)
- **VPC**: CIDR, Subnets (public/private)
- Internet Gateway & NAT Gateway
- Route tables
- Security Groups vs NACLs
- VPN & Direct Connect
- VPC Peering
- Best practices multi-AZ

### 6.2 [Load Balancing](06-Networking_nel_Cloud/02-Load_Balancing.md)
- **Application Load Balancer** (Layer 7): HTTP/HTTPS, path-based routing
- **Network Load Balancer** (Layer 4): TCP/UDP, ultra-low latency
- Target groups & Health checks
- Auto Scaling Groups
- Scaling policies
- Connection draining

### 6.3 [DNS e Gestione del Traffico](06-Networking_nel_Cloud/03-DNS_e_Gestione_del_Traffico.md)
- **Route 53**: Record types (A, AAAA, CNAME, Alias)
- Routing policies: Simple, Weighted, Latency, Geolocation, Failover
- Health checks & monitoring
- Traffic Flow visual editor
- Failover & disaster recovery
- Best practices

---

## **Modulo 7: Sicurezza nel Cloud**

### 7.1 [Fondamenti di Sicurezza Cloud](07-Sicurezza_nel_Cloud/01-Fondamenti_di_Sicurezza_Cloud.md)
- Modello di responsabilità condivisa
- Identity and Access Management (IAM)
- Principio del minimo privilegio (Least Privilege)
- Multi-factor authentication (MFA)
- Network Security: Security Groups, Firewalls
- Zero Trust Security
- Incident Response
- Monitoring e Logging per sicurezza
- Best practices

### 7.2 [Crittografia](07-Sicurezza_nel_Cloud/02-Crittografia.md)
- Concetti: Encryption, Encoding, Hashing
- Encryption at Rest (SSE, client-side)
- Encryption in Transit (TLS/SSL)
- Key Management Service (KMS)
- Hardware Security Modules (HSM)
- Certificate Management
- Secrets Management
- Data Loss Prevention (DLP)
- Compliance encryption requirements

### 7.3 [Compliance e Governance](07-Sicurezza_nel_Cloud/03-Compliance_e_Governance.md)
- Framework: ISO 27001, SOC 2, PCI DSS, HIPAA
- GDPR e privacy
- Data governance e classification
- Data residency e retention
- Policy as Code (OPA, Checkov)
- Audit e logging immutabile
- Access reviews
- Compliance automation
- Cost governance e tagging

---

## **Modulo 8: DevOps e Automazione**

### 8.1 [Infrastructure as Code (IaC)](08-DevOps_e_Automazione/01-Infrastructure_as_Code.md)
- Principi IaC: versionamento, riproducibilità
- Terraform: provider, state, modules
- CloudFormation (AWS): templates, stacks
- Azure Resource Manager (ARM) e Bicep
- Ansible e Pulumi
- GitOps workflow
- Testing e validation
- Best practices IaC

### 8.2 [CI/CD nel Cloud](08-DevOps_e_Automazione/02-CI_CD_nel_Cloud.md)
- Pipeline di CI/CD
- GitHub Actions, GitLab CI/CD
- Azure DevOps Pipelines
- AWS CodePipeline e CodeBuild
- Deployment strategies: Blue/Green, Canary
- Rolling updates
- Feature flags
- Rollback strategies

### 8.3 [Monitoring e Logging](08-DevOps_e_Automazione/03-Monitoring_e_Logging.md)
- Observability: metrics, logs, traces
- CloudWatch, Azure Monitor, Google Cloud Monitoring
- Prometheus e Grafana
- Distributed tracing (Jaeger, X-Ray)
- Log aggregation (ELK, CloudWatch Logs)
- Alerting e incident management
- SLI, SLO, SLA
- Troubleshooting best practices
- Application Performance Monitoring (APM)

---

## **Modulo 9: Serverless Computing**

### 9.1 [Introduzione al Serverless](09-Serverless_Computing/01-Introduzione_al_Serverless.md)
- Cos'è il Serverless?
- Vantaggi: zero gestione server, pay-per-execution
- Event-driven architecture
- Stateless vs Stateful
- Use cases ideali
- Limitazioni e trade-offs

### 9.2 [Function as a Service (FaaS)](09-Serverless_Computing/02-Function_as_a_Service.md)
- AWS Lambda: runtime, trigger, layers
- Azure Functions: bindings, Durable Functions
- Google Cloud Functions
- Trigger: HTTP, eventi, timer, code
- Cold start optimization
- Pricing models
- Best practices FaaS

### 9.3 [Backend as a Service (BaaS)](09-Serverless_Computing/03-Backend_as_a_Service.md)
- Firebase: Realtime Database, Firestore, Auth
- AWS Amplify: backend completo per mobile/web
- Supabase: alternative open-source a Firebase
- Auth as a Service (Auth0, Cognito)
- Database managed (DynamoDB, CosmosDB)
- Storage as a Service (S3, Blob Storage)
- API Gateway serverless

---

## **Modulo 10: Big Data e Machine Learning nel Cloud**

### 10.1 [Big Data](10-Big_Data_e_Machine_Learning_nel_Cloud/01-Big_Data.md)
- **Data Lakes vs Data Warehouses**: S3/ADLS vs Redshift/BigQuery
- Batch processing: EMR, Dataproc, Databricks
- Stream processing: Kinesis, Event Hubs, Pub/Sub
- ETL/ELT tools: Glue, Data Factory, Dataflow
- Distributed computing: Spark, Hadoop
- Best practices & partitioning

### 10.2 [Machine Learning](10-Big_Data_e_Machine_Learning_nel_Cloud/02-Machine_Learning.md)
- **AWS SageMaker**: Training, deployment, model monitoring
- **Azure ML**: AutoML, Designer, MLOps
- **Google Vertex AI**: Pipelines, Feature Store
- ML lifecycle: data prep, training, deployment
- AutoML & transfer learning
- Model serving & inference optimization
- MLOps best practices

### 10.3 [Analytics](10-Big_Data_e_Machine_Learning_nel_Cloud/03-Analytics.md)
- **BI Tools**: QuickSight, Power BI, Looker
- Real-time analytics: Kinesis Analytics, Stream Analytics
- BigQuery ML & serverless analytics
- Data visualization & dashboards
- Cost optimization strategies
- Best practices

---

## **Modulo 11: Costi e Ottimizzazione**

### 11.1 [Modelli di Pricing](11-Costi_e_Ottimizzazione/01-Modelli_di_Pricing.md)
- **On-Demand**: Pay-as-you-go, flessibilità massima
- **Reserved Instances**: 1-3 anni, fino a 72% sconto
- **Spot Instances**: fino a 90% sconto, workload interrompibili
- **Savings Plans**: commitment flessibile
- Confronto AWS vs Azure vs GCP
- Strategie di ottimizzazione costi

### 11.2 [Cost Management](11-Costi_e_Ottimizzazione/02-Cost_Management.md)
- **Cost Explorer & Budgets**: analisi e monitoraggio
- Tagging strategy & cost allocation
- Billing alerts & notifications
- TCO (Total Cost of Ownership)
- FinOps best practices
- Cost anomaly detection

### 11.3 [Ottimizzazione delle Risorse](11-Costi_e_Ottimizzazione/03-Ottimizzazione_delle_Risorse.md)
- **Right-sizing**: dimensionamento ottimale
- Auto-scaling policies
- Storage optimization & lifecycle
- Reserved capacity planning
- Serverless vs container cost analysis
- Monitoring & cost optimization tools
- FinOps best practices

---

## **Modulo 12: Casi di Studio e Progetti Pratici**

### 12.1 [Architetture Cloud Reali](12-Casi_di_Studio_e_Progetti_Pratici/01-Architetture_Cloud_Reali.md)
- **E-commerce Platform**: Three-tier architecture, autoscaling, CDN
- **Fintech Application**: High availability, compliance, disaster recovery
- **IoT Platform**: Real-time ingestion, stream processing, analytics
- **Gaming Backend**: Global low-latency, matchmaking, leaderboards
- Patterns: CQRS, Event Sourcing, Saga
- Best practices architetturali

### 12.2 [Migrazione al Cloud](12-Casi_di_Studio_e_Progetti_Pratici/02-Migrazione_al_Cloud.md)
- **6 R's Strategy**: Rehost, Replatform, Refactor, Repurchase, Retire, Retain
- Assessment & planning: discovery, dependency mapping
- Migration tools: AWS DMS, Azure Migrate, Cloud Endure
- Database migration: downtime minimization, cutover planning
- Post-migration optimization & validation
- Case study: On-prem to AWS migration

### 12.3 [Progetto Finale](12-Casi_di_Studio_e_Progetti_Pratici/03-Progetto_Finale.md)
- **Progetto E-commerce Cloud-Native**
- Architettura completa: microservizi, serverless, containers
- Terraform IaC per infrastruttura multi-AZ
- CI/CD pipeline con GitHub Actions
- Monitoring, logging, alerting
- Security & compliance
- Criteri di valutazione & timeline

---

## **Modulo 13: Tendenze e Futuro del Cloud**

### 13.1 [Tecnologie Emergenti](13-Tendenze_e_Futuro_del_Cloud/01-Tecnologie_Emergenti.md)
- **Edge Computing**: CDN++, AWS Wavelength, Azure Edge Zones
- **Quantum Computing**: AWS Braket, Azure Quantum, IonQ
- **AI/ML Advances**: GPT-4, AutoML, Edge AI
- **WebAssembly (WASM)**: Cloudflare Workers, Fastly Compute@Edge
- 5G & Cloud integration
- Confidential computing

### 13.2 [Sostenibilità](13-Tendenze_e_Futuro_del_Cloud/02-Sostenibilita.md)
- **Green Cloud Computing**: carbon neutrality goals
- PUE (Power Usage Effectiveness) metrics
- Renewable energy data centers
- Carbon-aware scheduling & workload optimization
- AWS Sustainability, Azure Carbon Optimization, Google Carbon Footprint
- Best practices per ridurre impatto ambientale

### 13.3 [Multi-Cloud e Cloud-Native](13-Tendenze_e_Futuro_del_Cloud/03-Multi_Cloud_e_Cloud_Native.md)
- **Multi-Cloud Strategy**: vendor diversification, best-of-breed
- Cloud portability & abstraction layers (Terraform, Pulumi)
- **Cloud-Native Principles**: 12-factor app, microservices, containers
- Service Mesh: Istio, Linkerd, Consul
- CNCF (Cloud Native Computing Foundation) landscape
- Future trends & conclusioni corso

---

## **Modalità di Valutazione**

- **Laboratori pratici**: 30%
- **Progetto finale**: 40%
- **Esame scritto/orale**: 30%

---

## **Prerequisiti del Corso**

- Conoscenze di base di reti di computer
- Familiarità con sistemi operativi (Linux/Windows)
- Programmazione di base (Python, Java, o JavaScript)
- Concetti di database

---

## **Strumenti e Risorse**

### Software e Piattaforme
- Account free tier AWS/Azure/GCP
- Docker Desktop
- Git e GitHub
- IDE (VSCode, IntelliJ)

### Libri di Testo Consigliati
- "Cloud Computing: Concepts, Technology & Architecture" - Thomas Erl
- "Architecting the Cloud" - Michael J. Kavis
- "The Phoenix Project" - Gene Kim

### Risorse Online
- Documentazione ufficiale AWS/Azure/GCP
- Cloud Academy / A Cloud Guru
- Qwiklabs per esercitazioni pratiche

---

## **Carico Didattico**

- **CFU**: 6-9 crediti
- **Ore totali**: 60-90 ore
- **Lezioni frontali**: 40-60 ore
- **Laboratorio**: 20-30 ore
- **Studio individuale**: 90-135 ore

---

*Indice aggiornato per l'anno accademico 2025-2026*

