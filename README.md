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

### 4.1 Amazon Web Services (AWS)
- Servizi principali: EC2, S3, RDS, Lambda
- IAM e gestione degli accessi
- VPC e networking
- AWS Console e CLI

### 4.2 Microsoft Azure
- Servizi principali: Virtual Machines, Blob Storage, Azure Functions
- Azure Active Directory
- Resource Groups e gestione risorse
- Azure Portal e Azure CLI

### 4.3 Google Cloud Platform (GCP)
- Servizi principali: Compute Engine, Cloud Storage, Cloud Functions
- IAM e progetti
- VPC e networking
- GCP Console e gcloud CLI

### 4.4 Altri Provider
- IBM Cloud
- Oracle Cloud
- Alibaba Cloud
- Provider Cloud italiani ed europei

---

## **Modulo 5: Storage e Database nel Cloud**

### 5.1 Storage
- Object Storage (S3, Azure Blob, GCS)
- Block Storage
- File Storage
- CDN (Content Delivery Network)

### 5.2 Database Relazionali
- Database gestiti (RDS, Cloud SQL, Azure SQL)
- Scalabilità verticale e orizzontale
- Backup e disaster recovery

### 5.3 Database NoSQL
- Document Database (MongoDB, DynamoDB)
- Key-Value Store (Redis, Memcached)
- Column-family (Cassandra, HBase)
- Graph Database (Neo4j)

---

## **Modulo 6: Networking nel Cloud**

### 6.1 Reti Virtuali
- VPC (Virtual Private Cloud)
- Subnet pubbliche e private
- Internet Gateway e NAT Gateway
- Peering e VPN

### 6.2 Load Balancing
- Application Load Balancer
- Network Load Balancer
- Auto Scaling
- Health Checks

### 6.3 DNS e Gestione del Traffico
- DNS nel Cloud (Route 53, Cloud DNS)
- Routing policies
- Failover e disaster recovery

---

## **Modulo 7: Sicurezza nel Cloud**

### 7.1 Fondamenti di Sicurezza Cloud
- Modello di responsabilità condivisa
- Identity and Access Management (IAM)
- Principio del minimo privilegio
- Multi-factor authentication (MFA)

### 7.2 Crittografia
- Crittografia dei dati at rest
- Crittografia dei dati in transit
- Key Management Service (KMS)
- Certificate Management

### 7.3 Compliance e Governance
- GDPR e privacy
- Certificazioni (ISO 27001, SOC 2)
- Audit e logging
- Security best practices

---

## **Modulo 8: DevOps e Automazione**

### 8.1 Infrastructure as Code (IaC)
- Terraform
- CloudFormation (AWS)
- Azure Resource Manager (ARM)
- Ansible

### 8.2 CI/CD nel Cloud
- Pipeline di CI/CD
- Jenkins, GitLab CI, GitHub Actions
- Azure DevOps, AWS CodePipeline
- Deployment strategies (Blue/Green, Canary)

### 8.3 Monitoring e Logging
- CloudWatch, Azure Monitor, Stackdriver
- Metriche e allarmi
- Log aggregation
- Application Performance Monitoring (APM)

---

## **Modulo 9: Serverless Computing**

### 9.1 Introduzione al Serverless
- Cos'è il Serverless
- Vantaggi e limitazioni
- Use cases

### 9.2 Function as a Service (FaaS)
- AWS Lambda
- Azure Functions
- Google Cloud Functions
- Event-driven architecture

### 9.3 Backend as a Service (BaaS)
- Firebase
- AWS Amplify
- Azure Mobile Apps
- Supabase

---

## **Modulo 10: Big Data e Machine Learning nel Cloud**

### 10.1 Big Data
- Data Lakes
- Data Warehousing (Redshift, BigQuery, Synapse)
- ETL/ELT nel Cloud
- Apache Spark nel Cloud

### 10.2 Machine Learning
- ML as a Service
- AWS SageMaker
- Azure Machine Learning
- Google AI Platform
- AutoML

### 10.3 Analytics
- Real-time analytics
- Batch processing
- Stream processing (Kinesis, Event Hubs, Dataflow)

---

## **Modulo 11: Costi e Ottimizzazione**

### 11.1 Modelli di Pricing
- Pay-as-you-go
- Reserved Instances
- Spot Instances
- Savings Plans

### 11.2 Cost Management
- Cost Explorer e budget
- Tagging delle risorse
- Cost allocation
- TCO (Total Cost of Ownership)

### 11.3 Ottimizzazione delle Risorse
- Right-sizing
- Auto-scaling
- Utilizzo di servizi gestiti
- FinOps best practices

---

## **Modulo 12: Casi di Studio e Progetti Pratici**

### 12.1 Architetture Cloud Reali
- E-commerce platform
- SaaS application
- Media streaming service
- IoT architecture

### 12.2 Migrazione al Cloud
- Strategie di migrazione (6R: Rehost, Replatform, Refactor, etc.)
- Assessment e planning
- Migration tools
- Post-migration optimization

### 12.3 Progetto Finale
- Progettazione di un'applicazione cloud-native
- Implementazione pratica
- Presentazione e discussione

---

## **Modulo 13: Tendenze e Futuro del Cloud**

### 13.1 Tecnologie Emergenti
- Edge Computing e IoT
- Quantum Computing nel Cloud
- AI/ML Integration
- 5G e Cloud

### 13.2 Sostenibilità
- Green Cloud Computing
- Carbon footprint
- Efficienza energetica

### 13.3 Multi-Cloud e Cloud-Native
- Strategie multi-cloud
- Cloud-native applications
- Service Mesh (Istio, Linkerd)
- Cloud Native Computing Foundation (CNCF)

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

