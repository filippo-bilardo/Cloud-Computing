# Amazon Web Services (AWS)

## Introduzione ad AWS

### Storia ed Evoluzione

Amazon Web Services (AWS) è stata lanciata ufficialmente nel 2006, anche se i primi servizi erano disponibili già dal 2004. AWS è nato dall'infrastruttura interna che Amazon aveva sviluppato per supportare il proprio business di e-commerce.

**Timeline chiave:**
- **2002**: Lancio interno dei servizi
- **2004**: SQS (Simple Queue Service) - primo servizio pubblico
- **2006**: Lancio ufficiale con EC2 e S3
- **2009**: VPC, CloudWatch
- **2012**: DynamoDB, Redshift
- **2014**: Lambda (rivoluzione serverless)
- **2015**: Aurora
- **2017**: Fargate
- **2020**: Leadership nel mercato cloud con 32% market share
- **2024**: Oltre 200 servizi disponibili

### Posizione nel Mercato

AWS è il leader indiscusso del mercato cloud con:
- **32%** di market share globale (Q4 2023)
- **Oltre 1 milione** di clienti attivi
- Presenza in **33 regioni geografiche**
- **105 Availability Zones**
- Fatturato annuo di oltre **$90 miliardi**

### Filosofia AWS

AWS segue alcuni principi fondamentali:
1. **Customer obsession**: Focus sul cliente
2. **Innovation**: Continua evoluzione dei servizi
3. **Pay-as-you-go**: Paghi solo ciò che usi
4. **Global infrastructure**: Presenza globale
5. **Security**: Sicurezza integrata by design

## Architettura Globale AWS

### Regioni (Regions)

AWS opera in **33+ regioni** geografiche nel mondo. Ogni regione è completamente indipendente.

**Regioni principali:**
```
Americas:
  - us-east-1 (Virginia del Nord) - La più vecchia e completa
  - us-west-2 (Oregon)
  - ca-central-1 (Canada)
  - sa-east-1 (São Paulo)

Europe:
  - eu-west-1 (Irlanda)
  - eu-central-1 (Francoforte)
  - eu-south-1 (Milano) ← Italia!
  - eu-north-1 (Stoccolma)

Asia Pacific:
  - ap-southeast-1 (Singapore)
  - ap-northeast-1 (Tokyo)
  - ap-south-1 (Mumbai)

Middle East & Africa:
  - me-south-1 (Bahrain)
  - af-south-1 (Città del Capo)
```

### Availability Zones (AZ)

Ogni regione contiene **2-6 Availability Zones** (solitamente 3).

**Caratteristiche AZ:**
- Datacenter fisicamente separati
- Distanza: 100km max tra AZ
- Connessione ad alta velocità (< 2ms latenza)
- Isolamento da guasti
- Alimentazione e rete ridondanti

```
```
┌─────────────────────────────────────────┐
│         Region: eu-west-1               │
│                                         │
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │   AZ-a   │  │   AZ-b   │  │ AZ-c   │ │
│  │          │  │          │  │        │ │
│  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌────┐ │ │
│  │ │ DC 1 │ │  │ │ DC 3 │ │  │ │DC 5│ │ │
│  │ │ DC 2 │ │  │ │ DC 4 │ │  │ │DC 6│ │ │
│  │ └──────┘ │  │ └──────┘ │  │ └────┘ │ │
│  └──────────┘  └──────────┘  └────────┘ │
│         High-speed fiber                │
└─────────────────────────────────────────┘
```

### Edge Locations

AWS ha **400+ Edge Locations** in tutto il mondo per:
- CloudFront (CDN)
- Route 53 (DNS)
- AWS Global Accelerator
- Lambda@Edge

### Local Zones

Estensioni delle regioni AWS più vicine alle grandi città per applicazioni a bassissima latenza.

### Wavelength Zones

Integrazione con reti 5G per applicazioni mobile ultra-low latency.

## Servizi AWS per Categoria

### 1. Compute

#### Amazon EC2 (Elastic Compute Cloud)

Server virtuali nel cloud. Il servizio fondamentale di AWS.

**Tipi di Istanze:**

| Famiglia | Tipo | vCPU | Memoria | Use Case |
|----------|------|------|---------|----------|
| **t3.micro** | General Purpose | 2 | 1 GB | Test, small apps |
| **t3.medium** | General Purpose | 2 | 4 GB | Web servers |
| **m5.large** | General Purpose | 2 | 8 GB | Applicazioni bilanciate |
| **c5.xlarge** | Compute Optimized | 4 | 8 GB | CPU intensive |
| **r5.xlarge** | Memory Optimized | 4 | 32 GB | Database, cache |
| **g4dn.xlarge** | GPU | 4 | 16 GB | ML, rendering |
| **i3.large** | Storage Optimized | 2 | 15.25 GB | NoSQL, data warehouse |

**Modelli di Pricing:**
- **On-Demand**: $0.096/ora (t3.medium)
- **Reserved** (1 anno): -40% sconto
- **Reserved** (3 anni): -60% sconto
- **Spot Instances**: fino a -90% sconto

**Esempio CLI - Lanciare un'istanza:**
```bash
# Creare security group
aws ec2 create-security-group \
    --group-name WebServerSG \
    --description "Security group for web server"

# Aggiungere regola SSH
aws ec2 authorize-security-group-ingress \
    --group-name WebServerSG \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0

# Lanciare istanza
aws ec2 run-instances \
    --image-id ami-0c55b159cbfafe1f0 \
    --instance-type t3.micro \
    --key-name MyKeyPair \
    --security-groups WebServerSG \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=WebServer}]'

# Ottenere IP pubblico
aws ec2 describe-instances \
    --filters "Name=tag:Name,Values=WebServer" \
    --query 'Reservations[*].Instances[*].[PublicIpAddress]' \
    --output text

# Connettersi
ssh -i MyKeyPair.pem ec2-user@<public-ip>
```

#### AWS Lambda

Serverless computing - esegui codice senza gestire server.

**Caratteristiche:**
- Supporto linguaggi: Python, Node.js, Java, Go, .NET, Ruby
- Timeout max: 15 minuti
- Memoria: 128 MB - 10 GB
- Storage temporaneo: 512 MB - 10 GB
- Concorrenza: 1000 esecuzioni simultanee (default)

**Pricing:**
- **Richieste**: $0.20 per 1M richieste
- **Compute**: $0.0000166667 per GB-secondo
- **Free tier**: 1M richieste/mese + 400,000 GB-sec/mese

**Esempio - Lambda Function:**
```python
import json

def lambda_handler(event, context):
    """
    Elabora eventi da API Gateway
    """
    # Parse body
    body = json.loads(event.get('body', '{}'))
    name = body.get('name', 'World')
    
    # Logica business
    message = f"Hello, {name}!"
    
    # Response
    return {
        'statusCode': 200,
        'headers': {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
        'body': json.dumps({
            'message': message
        })
    }
```

**Deploy con AWS CLI:**
```bash
# Creare pacchetto
zip function.zip lambda_function.py

# Creare funzione
aws lambda create-function \
    --function-name HelloWorldFunction \
    --runtime python3.11 \
    --role arn:aws:iam::123456789012:role/lambda-role \
    --handler lambda_function.lambda_handler \
    --zip-file fileb://function.zip

# Invocare
aws lambda invoke \
    --function-name HelloWorldFunction \
    --payload '{"body": "{\"name\": \"Alice\"}"}' \
    response.json

# Vedere risposta
cat response.json
```

#### Amazon ECS (Elastic Container Service)

Orchestrazione container gestita.

**Componenti:**
- **Task Definition**: Blueprint del container
- **Service**: Gestisce task in esecuzione
- **Cluster**: Gruppo di istanze EC2 o Fargate

**Esempio Task Definition:**
```json
{
  "family": "webapp",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "nginx",
      "image": "nginx:latest",
      "portMappings": [
        {
          "containerPort": 80,
          "protocol": "tcp"
        }
      ],
      "essential": true,
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/webapp",
          "awslogs-region": "eu-west-1",
          "awslogs-stream-prefix": "nginx"
        }
      }
    }
  ]
}
```

#### Amazon EKS (Elastic Kubernetes Service)

Kubernetes gestito da AWS.

**Caratteristiche:**
- Control plane gestito
- Integrazione con servizi AWS
- Supporto per Fargate
- Multi-AZ per HA

**Creazione cluster:**
```bash
# Creare cluster
eksctl create cluster \
    --name my-cluster \
    --region eu-west-1 \
    --nodegroup-name standard-workers \
    --node-type t3.medium \
    --nodes 3 \
    --nodes-min 1 \
    --nodes-max 4 \
    --managed

# Configurare kubectl
aws eks update-kubeconfig --name my-cluster --region eu-west-1

# Deploy applicazione
kubectl apply -f deployment.yaml
```

#### AWS Fargate

Serverless container - nessun server da gestire.

**Vantaggi:**
- Nessuna gestione istanze EC2
- Paghi solo per le risorse container
- Scaling automatico
- Sicurezza migliorata

#### Elastic Beanstalk

PaaS - Deploy applicazioni senza gestire infrastruttura.

**Piattaforme supportate:**
- Node.js, Python, Ruby, PHP, Go
- Java, .NET
- Docker

### 2. Storage

#### Amazon S3 (Simple Storage Service)

Object storage scalabile e durevole.

**Caratteristiche:**
- **Durabilità**: 99.999999999% (11 nove)
- **Disponibilità**: 99.99%
- **Oggetti**: fino a 5 TB
- **Bucket**: Illimitati

**Classi di Storage:**

| Classe | Use Case | Costo (GB/mese) | Retrieval |
|--------|----------|-----------------|-----------|
| **S3 Standard** | Accesso frequente | $0.023 | Immediato |
| **S3 Intelligent-Tiering** | Pattern sconosciuto | $0.023 + $0.0025 | Immediato |
| **S3 Standard-IA** | Accesso raro | $0.0125 | Immediato |
| **S3 One Zone-IA** | Dati non critici | $0.01 | Immediato |
| **S3 Glacier Instant** | Archivio accesso istantaneo | $0.004 | Immediato |
| **S3 Glacier Flexible** | Archivio long-term | $0.0036 | Minuti-ore |
| **S3 Glacier Deep Archive** | Archivio 7-10 anni | $0.00099 | 12 ore |

**Esempi CLI:**
```bash
# Creare bucket
aws s3 mb s3://my-unique-bucket-name-12345

# Upload file
aws s3 cp myfile.txt s3://my-unique-bucket-name-12345/

# Upload directory
aws s3 sync ./mydir s3://my-unique-bucket-name-12345/mydir/

# Listing
aws s3 ls s3://my-unique-bucket-name-12345/

# Download
aws s3 cp s3://my-unique-bucket-name-12345/myfile.txt ./downloaded.txt

# Configurare lifecycle policy
aws s3api put-bucket-lifecycle-configuration \
    --bucket my-unique-bucket-name-12345 \
    --lifecycle-configuration file://lifecycle.json
```

**lifecycle.json:**
```json
{
  "Rules": [
    {
      "Id": "Archive old logs",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "logs/"
      },
      "Transitions": [
        {
          "Days": 30,
          "StorageClass": "STANDARD_IA"
        },
        {
          "Days": 90,
          "StorageClass": "GLACIER"
        }
      ],
      "Expiration": {
        "Days": 365
      }
    }
  ]
}
```

#### Amazon EBS (Elastic Block Store)

Storage a blocchi per EC2.

**Tipi di Volume:**

| Tipo | IOPS | Throughput | Use Case | Prezzo GB/mese |
|------|------|------------|----------|----------------|
| **gp3** (General Purpose SSD) | 3,000-16,000 | 125-1,000 MB/s | Generale | $0.08 |
| **gp2** (General Purpose SSD) | 100-16,000 | 128-250 MB/s | Generale | $0.10 |
| **io2** (Provisioned IOPS SSD) | 100-64,000 | 256-4,000 MB/s | Database critici | $0.125 |
| **st1** (Throughput Optimized HDD) | 500 | 500 MB/s | Big data | $0.045 |
| **sc1** (Cold HDD) | 250 | 250 MB/s | Archivio | $0.015 |

**Esempio:**
```bash
# Creare volume
aws ec2 create-volume \
    --availability-zone eu-west-1a \
    --size 100 \
    --volume-type gp3 \
    --iops 3000 \
    --tag-specifications 'ResourceType=volume,Tags=[{Key=Name,Value=MyDataVolume}]'

# Attach a EC2
aws ec2 attach-volume \
    --volume-id vol-0123456789abcdef \
    --instance-id i-0123456789abcdef \
    --device /dev/sdf
```

#### Amazon EFS (Elastic File System)

File system NFS gestito.

**Caratteristiche:**
- File system condiviso
- Scalabilità automatica (petabyte)
- Multi-AZ
- Pay-per-use

**Classi di storage:**
- **EFS Standard**: Accesso frequente
- **EFS IA** (Infrequent Access): -92% costo per GB

#### Amazon FSx

File system gestiti specializzati:
- **FSx for Windows File Server**: File server Windows
- **FSx for Lustre**: High-performance computing
- **FSx for NetApp ONTAP**: Enterprise storage
- **FSx for OpenZFS**: ZFS file system

### 3. Database

#### Amazon RDS (Relational Database Service)

Database relazionali gestiti.

**Engine supportati:**
- Amazon Aurora (MySQL/PostgreSQL compatibile)
- MySQL
- PostgreSQL
- MariaDB
- Oracle
- Microsoft SQL Server

**Caratteristiche:**
- Backup automatici
- Patching automatico
- Multi-AZ per HA
- Read replicas
- Encryption at rest

**Esempio - Creare database:**
```bash
# Creare DB PostgreSQL
aws rds create-db-instance \
    --db-instance-identifier mypostgres \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.3 \
    --master-username admin \
    --master-user-password MySecurePass123! \
    --allocated-storage 20 \
    --storage-type gp3 \
    --vpc-security-group-ids sg-0123456789abcdef \
    --db-subnet-group-name my-db-subnet-group \
    --backup-retention-period 7 \
    --multi-az \
    --storage-encrypted

# Ottenere endpoint
aws rds describe-db-instances \
    --db-instance-identifier mypostgres \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text
```

#### Amazon Aurora

Database relazionale MySQL/PostgreSQL compatibile ad alte prestazioni.

**Vantaggi:**
- **5x** più veloce di MySQL standard
- **3x** più veloce di PostgreSQL standard
- Storage auto-scaling (fino a 128 TB)
- Fino a 15 read replicas
- Global database (cross-region)
- Serverless v2 disponibile

**Aurora Serverless:**
```bash
aws rds create-db-cluster \
    --db-cluster-identifier aurora-serverless-cluster \
    --engine aurora-postgresql \
    --engine-mode serverless \
    --scaling-configuration MinCapacity=2,MaxCapacity=16,AutoPause=true \
    --master-username admin \
    --master-user-password MySecurePass123!
```

#### Amazon DynamoDB

Database NoSQL key-value e document.

**Caratteristiche:**
- Latenza < 10ms
- Scalabilità illimitata
- Fully managed
- Global tables (multi-region)
- Point-in-time recovery

**Capacity Modes:**
- **On-demand**: Pay per request
- **Provisioned**: Capacità riservata (più economico)

**Esempio - Creare tabella:**
```bash
# Creare tabella
aws dynamodb create-table \
    --table-name Users \
    --attribute-definitions \
        AttributeName=UserId,AttributeType=S \
        AttributeName=Email,AttributeType=S \
    --key-schema \
        AttributeName=UserId,KeyType=HASH \
    --global-secondary-indexes \
        IndexName=EmailIndex,Keys=[{AttributeName=Email,KeyType=HASH}],Projection={ProjectionType=ALL} \
    --billing-mode PAY_PER_REQUEST

# Insert item
aws dynamodb put-item \
    --table-name Users \
    --item '{
        "UserId": {"S": "user123"},
        "Email": {"S": "user@example.com"},
        "Name": {"S": "John Doe"},
        "Age": {"N": "30"}
    }'

# Query
aws dynamodb get-item \
    --table-name Users \
    --key '{"UserId": {"S": "user123"}}'
```

#### Amazon ElastiCache

In-memory cache gestito.

**Engine:**
- **Redis**: Strutture dati avanzate, persistenza
- **Memcached**: Cache semplice, multi-thread

**Use Cases:**
- Session store
- Database caching
- Real-time analytics
- Leaderboards

#### Amazon Redshift

Data warehouse per analytics.

**Caratteristiche:**
- Colonnare
- Massively parallel processing (MPP)
- Integrazione con S3
- Redshift Spectrum (query S3 direttamente)

### 4. Networking

#### Amazon VPC (Virtual Private Cloud)

Rete virtuale isolata in AWS.

**Componenti:**
- **Subnets**: Pubbliche e private
- **Route Tables**: Routing del traffico
- **Internet Gateway**: Accesso internet
- **NAT Gateway**: Outbound per subnet private
- **Security Groups**: Firewall stateful
- **Network ACLs**: Firewall stateless

**Architettura VPC tipica:**
```
┌─────────────────────── VPC (10.0.0.0/16) ───────────────────────┐
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              Availability Zone A                           │  │
│  │                                                            │  │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐   │  │
│  │  │ Public Subnet       │  │ Private Subnet           │   │  │
│  │  │ 10.0.1.0/24         │  │ 10.0.3.0/24              │   │  │
│  │  │                     │  │                          │   │  │
│  │  │ ┌─────────────┐     │  │ ┌──────────────────┐    │   │  │
│  │  │ │ NAT Gateway │     │  │ │  App Server      │    │   │  │
│  │  │ └─────────────┘     │  │ └──────────────────┘    │   │  │
│  │  │ ┌─────────────┐     │  │ ┌──────────────────┐    │   │  │
│  │  │ │ Load Bal.   │     │  │ │  App Server      │    │   │  │
│  │  │ └─────────────┘     │  │ └──────────────────┘    │   │  │
│  │  └─────────────────────┘  └──────────────────────────┘   │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              Availability Zone B                           │  │
│  │                                                            │  │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐   │  │
│  │  │ Public Subnet       │  │ Private Subnet           │   │  │
│  │  │ 10.0.2.0/24         │  │ 10.0.4.0/24              │   │  │
│  │  │ ┌─────────────┐     │  │ ┌──────────────────┐    │   │  │
│  │  │ │ NAT Gateway │     │  │ │  RDS Primary     │    │   │  │
│  │  │ └─────────────┘     │  │ └──────────────────┘    │   │  │
│  │  └─────────────────────┘  │ ┌──────────────────┐    │   │  │
│  │                            │ │  RDS Standby     │    │   │  │
│  │                            │ └──────────────────┘    │   │  │
│  │                            └──────────────────────────┘   │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  Internet Gateway                                                │
└───────────────────────────────────────────────────────────────────┘
```

**Creare VPC:**
```bash
# Creare VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16

VPC_ID=$(aws ec2 describe-vpcs \
    --filters "Name=cidr,Values=10.0.0.0/16" \
    --query 'Vpcs[0].VpcId' --output text)

# Creare subnet pubblica
aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.1.0/24 \
    --availability-zone eu-west-1a

# Creare Internet Gateway
aws ec2 create-internet-gateway
IGW_ID=$(aws ec2 describe-internet-gateways \
    --query 'InternetGateways[0].InternetGatewayId' --output text)

# Attach a VPC
aws ec2 attach-internet-gateway \
    --vpc-id $VPC_ID \
    --internet-gateway-id $IGW_ID

# Creare route table
aws ec2 create-route-table --vpc-id $VPC_ID
RTB_ID=$(aws ec2 describe-route-tables \
    --filters "Name=vpc-id,Values=$VPC_ID" \
    --query 'RouteTables[0].RouteTableId' --output text)

# Aggiungere route a internet
aws ec2 create-route \
    --route-table-id $RTB_ID \
    --destination-cidr-block 0.0.0.0/0 \
    --gateway-id $IGW_ID
```

#### Elastic Load Balancing (ELB)

Distribuzione automatica del traffico.

**Tipi:**
- **Application Load Balancer (ALB)**: Layer 7 (HTTP/HTTPS)
- **Network Load Balancer (NLB)**: Layer 4 (TCP/UDP)
- **Gateway Load Balancer (GLB)**: Layer 3 (virtual appliances)
- **Classic Load Balancer**: Legacy

**ALB Features:**
- Host-based routing
- Path-based routing
- WebSocket support
- HTTP/2
- Lambda target

**Esempio ALB:**
```bash
# Creare target group
aws elbv2 create-target-group \
    --name my-targets \
    --protocol HTTP \
    --port 80 \
    --vpc-id $VPC_ID \
    --health-check-path /health

# Creare ALB
aws elbv2 create-load-balancer \
    --name my-alb \
    --subnets subnet-12345 subnet-67890 \
    --security-groups sg-12345

# Creare listener
aws elbv2 create-listener \
    --load-balancer-arn arn:aws:elasticloadbalancing:... \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:...
```

#### Amazon Route 53

DNS gestito e domain registration.

**Routing Policies:**
- **Simple**: Un record -> un IP
- **Weighted**: Distribuzione traffico percentuale
- **Latency**: Routing basato su latenza
- **Failover**: HA con health check
- **Geolocation**: Basato su posizione utente
- **Geoproximity**: Basato su prossimità geografica
- **Multi-value**: Più valori con health check

#### Amazon CloudFront

CDN globale di AWS.

**Caratteristiche:**
- 400+ Edge Locations
- Integrazione con S3, ALB, EC2
- Lambda@Edge
- Cache invalidation
- SSL/TLS
- DDoS protection (AWS Shield)

#### AWS Direct Connect

Connessione dedicata on-premise -> AWS.

**Vantaggi:**
- Bandwidth dedicato (1 Gbps - 100 Gbps)
- Latenza ridotta e consistente
- Costi di trasferimento dati ridotti
- Hybrid cloud

### 5. Security & Identity

#### AWS IAM (Identity and Access Management)

Gestione accessi e permessi.

**Componenti:**
- **Users**: Identità permanenti
- **Groups**: Collezioni di users
- **Roles**: Identità assumibili
- **Policies**: Documenti JSON con permessi

**Policy Example:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::my-bucket/*"
    },
    {
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::my-bucket"
    }
  ]
}
```

**Best Practices:**
- **Principle of Least Privilege**: Permessi minimi necessari
- **MFA** per account root e utenti privilegiati
- **Roles** invece di credenziali hardcoded
- **Password policy** forte
- **Rotate credentials** regolarmente
- **CloudTrail** per audit

**Esempio CLI:**
```bash
# Creare user
aws iam create-user --user-name developer

# Creare gruppo
aws iam create-group --group-name developers

# Aggiungere user a gruppo
aws iam add-user-to-group \
    --user-name developer \
    --group-name developers

# Attach policy a gruppo
aws iam attach-group-policy \
    --group-name developers \
    --policy-arn arn:aws:iam::aws:policy/PowerUserAccess

# Creare access key
aws iam create-access-key --user-name developer
```

#### AWS KMS (Key Management Service)

Gestione chiavi di crittografia.

**Caratteristiche:**
- Hardware Security Modules (HSM)
- Integrazione con servizi AWS
- Audit con CloudTrail
- Automatic key rotation

#### AWS Secrets Manager

Gestione secret (password, API keys, certificati).

**Vantaggi:**
- Rotation automatica
- Fine-grained access control
- Encryption at rest
- Audit

```bash
# Creare secret
aws secretsmanager create-secret \
    --name prod/db/password \
    --secret-string '{"username":"admin","password":"MySecurePass123!"}'

# Retrieve secret
aws secretsmanager get-secret-value \
    --secret-id prod/db/password
```

#### AWS WAF (Web Application Firewall)

Firewall per applicazioni web.

**Protezioni:**
- SQL injection
- Cross-site scripting (XSS)
- DDoS (rate limiting)
- Geo-blocking
- Bot detection

#### AWS Shield

Protezione DDoS gestita.

**Tiers:**
- **Shield Standard**: Gratuito, protezione base
- **Shield Advanced**: $3,000/mese, protezione avanzata + DDoS Response Team

### 6. Monitoring & Management

#### Amazon CloudWatch

Monitoring e observability.

**Componenti:**
- **Metrics**: Metriche di sistema e custom
- **Logs**: Aggregazione e analisi log
- **Alarms**: Notifiche basate su threshold
- **Events/EventBridge**: Event-driven automation
- **Dashboards**: Visualizzazione

**Esempio - Creare allarme:**
```bash
# Allarme CPU alta
aws cloudwatch put-metric-alarm \
    --alarm-name HighCPU \
    --alarm-description "Alert when CPU exceeds 80%" \
    --metric-name CPUUtilization \
    --namespace AWS/EC2 \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --dimensions Name=InstanceId,Value=i-1234567890abcdef \
    --alarm-actions arn:aws:sns:eu-west-1:123456789012:my-sns-topic

# Publish custom metric
aws cloudwatch put-metric-data \
    --namespace MyApp \
    --metric-name OrdersProcessed \
    --value 42 \
    --timestamp $(date -u +"%Y-%m-%dT%H:%M:%S")
```

#### AWS CloudTrail

Audit e compliance - log di tutte le API calls.

**Caratteristiche:**
- Event history (90 giorni gratuiti)
- Trail per archiviazione S3
- Integrazione CloudWatch Logs
- Event selectors per filtraggio

#### AWS X-Ray

Distributed tracing per microservizi.

**Vantaggi:**
- Service map
- Latency analysis
- Error detection
- Request tracking

### 7. DevOps & Automation

#### AWS CodePipeline

CI/CD pipeline gestita.

**Stages:**
- Source (CodeCommit, GitHub, S3)
- Build (CodeBuild)
- Test
- Deploy (CodeDeploy, ECS, Lambda)

#### AWS CodeBuild

Build service gestito.

**buildspec.yml:**
```yaml
version: 0.2

phases:
  install:
    runtime-versions:
      nodejs: 18
    commands:
      - npm install
  pre_build:
    commands:
      - npm run lint
  build:
    commands:
      - npm run build
  post_build:
    commands:
      - npm test

artifacts:
  files:
    - '**/*'
  base-directory: dist
```

#### AWS CloudFormation

Infrastructure as Code (IaC).

**Template esempio:**
```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Simple web server'

Parameters:
  InstanceType:
    Type: String
    Default: t3.micro
    AllowedValues:
      - t3.micro
      - t3.small
      - t3.medium

Resources:
  WebServer:
    Type: AWS::EC2::Instance
    Properties:
      ImageId: ami-0c55b159cbfafe1f0
      InstanceType: !Ref InstanceType
      SecurityGroups:
        - !Ref WebServerSecurityGroup
      UserData:
        Fn::Base64: |
          #!/bin/bash
          yum update -y
          yum install -y httpd
          systemctl start httpd
          systemctl enable httpd
          echo "Hello from CloudFormation" > /var/www/html/index.html

  WebServerSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Enable HTTP access
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 80
          ToPort: 80
          CidrIp: 0.0.0.0/0

Outputs:
  WebsiteURL:
    Description: URL of the website
    Value: !Sub 'http://${WebServer.PublicDnsName}'
```

**Deploy:**
```bash
aws cloudformation create-stack \
    --stack-name my-web-server \
    --template-body file://template.yaml \
    --parameters ParameterKey=InstanceType,ParameterValue=t3.small
```

### 8. Analytics & Big Data

#### Amazon Athena

Query SQL su S3.

**Caratteristiche:**
- Serverless
- Pay per query ($5 per TB scanned)
- Supporto formati: Parquet, ORC, JSON, CSV

#### Amazon EMR (Elastic MapReduce)

Hadoop/Spark gestito.

**Framework supportati:**
- Apache Spark
- Apache Hive
- Apache HBase
- Presto

#### Amazon Kinesis

Streaming data platform.

**Servizi:**
- **Kinesis Data Streams**: Real-time streaming
- **Kinesis Data Firehose**: Load data in AWS
- **Kinesis Data Analytics**: SQL su stream

#### Amazon Redshift

Data warehouse (vedi sezione Database).

### 9. AI & Machine Learning

#### Amazon SageMaker

Piattaforma ML completa.

**Funzionalità:**
- Notebook Jupyter
- Training distribuito
- Hyperparameter tuning
- Model deployment
- SageMaker Autopilot (AutoML)

#### Servizi AI Pre-trained:

- **Rekognition**: Computer vision
- **Comprehend**: NLP
- **Polly**: Text-to-speech
- **Transcribe**: Speech-to-text
- **Translate**: Traduzione
- **Lex**: Chatbot
- **Forecast**: Time series forecasting

### 10. Integration & Messaging

#### Amazon SQS (Simple Queue Service)

Message queue gestita.

**Tipi:**
- **Standard**: Best-effort ordering, at-least-once delivery
- **FIFO**: Ordinamento garantito, exactly-once delivery

```bash
# Creare coda
aws sqs create-queue --queue-name MyQueue

# Send message
aws sqs send-message \
    --queue-url https://sqs.eu-west-1.amazonaws.com/123456789012/MyQueue \
    --message-body "Hello from SQS"

# Receive message
aws sqs receive-message \
    --queue-url https://sqs.eu-west-1.amazonaws.com/123456789012/MyQueue
```

#### Amazon SNS (Simple Notification Service)

Pub/sub messaging.

**Protocolli:**
- Email
- SMS
- HTTP/HTTPS
- Lambda
- SQS

#### Amazon EventBridge

Event bus serverless.

**Use Cases:**
- Event-driven architectures
- SaaS integration
- Scheduled events (cron)

#### AWS Step Functions

Orchestrazione workflow serverless.

## Modelli di Pricing

### 1. On-Demand

Paghi per quello che usi, senza impegni.

**Pro:**
- Flessibilità massima
- Nessun upfront cost
- Ideale per workload variabili

**Contro:**
- Costo più alto

### 2. Reserved Instances (RI)

Commitment 1 o 3 anni.

**Risparmi:**
- 1 anno: ~40% sconto
- 3 anni: ~60% sconto

**Opzioni pagamento:**
- All upfront (massimo sconto)
- Partial upfront
- No upfront

**Convertible RI**: Possibilità cambiare tipo istanza

### 3. Savings Plans

Commitment su $ spesi/ora.

**Tipi:**
- **Compute Savings Plans**: EC2, Lambda, Fargate
- **EC2 Instance Savings Plans**: Specifico per EC2

### 4. Spot Instances

Capacità inutilizzata AWS con sconti fino a 90%.

**Caratteristiche:**
- Possono essere interrotte con 2 minuti di preavviso
- Ideale per: batch, big data, CI/CD

### 5. Free Tier

**Always Free:**
- Lambda: 1M richieste/mese
- DynamoDB: 25 GB storage
- CloudFront: 1 TB transfer/mese

**12 mesi gratis:**
- EC2: 750 ore/mese t2.micro o t3.micro
- S3: 5 GB Standard storage
- RDS: 750 ore/mese db.t2.micro

## AWS Well-Architected Framework

Framework per costruire architetture cloud robuste.

### 6 Pilastri:

#### 1. Operational Excellence

**Principi:**
- Operazioni as code
- Cambiamenti frequenti e piccoli
- Miglioramento continuo
- Anticipare failure

**Servizi:**
- CloudFormation
- CodePipeline
- Systems Manager

#### 2. Security

**Principi:**
- Identity forte (IAM)
- Traceability (CloudTrail)
- Security layers
- Encryption everywhere
- Automation

**Servizi:**
- IAM
- KMS
- WAF/Shield
- GuardDuty

#### 3. Reliability

**Principi:**
- Testing recovery
- Automatic recovery
- Scaling orizzontale
- Stop guessing capacity

**Servizi:**
- Multi-AZ deployments
- Auto Scaling
- Route 53
- RDS Multi-AZ

#### 4. Performance Efficiency

**Principi:**
- Democratizzare tecnologie avanzate
- Global in minuti
- Serverless architectures
- Sperimentazione

**Servizi:**
- Lambda
- DynamoDB
- CloudFront

#### 5. Cost Optimization

**Principi:**
- Consumption models
- Measure efficiency
- Eliminate unnecessary spend
- Analyze expenditure

**Servizi:**
- Cost Explorer
- Trusted Advisor
- Reserved Instances
- Auto Scaling

#### 6. Sustainability

**Principi:**
- Minimize environmental impact
- Efficient use of resources
- Renewable energy

## AWS CLI - Esempi Pratici

### Installazione

```bash
# Linux/macOS
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Verifica
aws --version
```

### Configurazione

```bash
# Configure con access keys
aws configure

# Output:
# AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
# AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
# Default region name [None]: eu-west-1
# Default output format [None]: json

# Multiple profiles
aws configure --profile production
```

### Comandi Comuni

```bash
# S3
aws s3 ls
aws s3 cp file.txt s3://bucket/
aws s3 sync ./dir s3://bucket/dir/

# EC2
aws ec2 describe-instances
aws ec2 start-instances --instance-ids i-1234567890abcdef
aws ec2 stop-instances --instance-ids i-1234567890abcdef

# Lambda
aws lambda list-functions
aws lambda invoke --function-name MyFunction output.json

# CloudWatch
aws cloudwatch get-metric-statistics \
    --namespace AWS/EC2 \
    --metric-name CPUUtilization \
    --dimensions Name=InstanceId,Value=i-1234567890abcdef \
    --statistics Average \
    --start-time 2024-01-01T00:00:00Z \
    --end-time 2024-01-01T23:59:59Z \
    --period 3600

# IAM
aws iam list-users
aws iam list-roles

# RDS
aws rds describe-db-instances
```

## Certificazioni AWS

### Percorso Certificazioni

```
Foundational
    ↓
┌─────────────────────┐
│ Cloud Practitioner  │ ← Consigliato per iniziare
└─────────────────────┘
    ↓
Associate Level
    ↓
┌──────────────────────┬──────────────────────┬─────────────────────┐
│ Solutions Architect  │  Developer           │  SysOps Admin       │
│  Associate           │  Associate           │  Associate          │
└──────────────────────┴──────────────────────┴─────────────────────┘
    ↓
Professional Level
    ↓
┌─────────────────────────────┬──────────────────────────────────┐
│ Solutions Architect Pro     │  DevOps Engineer Pro             │
└─────────────────────────────┴──────────────────────────────────┘
    ↓
Specialty
    ↓
┌──────────┬──────────┬──────────┬───────────┬────────────┬────────┐
│Advanced  │Security  │Machine   │Data       │Database    │SAP on  │
│Networking│Specialty │Learning  │Analytics  │Specialty   │AWS     │
└──────────┴──────────┴──────────┴───────────┴────────────┴────────┘
```

### Dettaglio Certificazioni:

#### AWS Certified Cloud Practitioner

**Livello**: Entry
**Costo**: $100
**Durata esame**: 90 minuti
**Validità**: 3 anni

**Domini:**
- Cloud concepts (26%)
- Security and compliance (25%)
- Technology (33%)
- Billing and pricing (16%)

#### AWS Certified Solutions Architect - Associate

**Livello**: Associate
**Costo**: $150
**Durata esame**: 130 minuti

**Domini:**
- Design resilient architectures (30%)
- Design high-performing architectures (28%)
- Design secure applications (24%)
- Design cost-optimized architectures (18%)

**Preparazione:**
- Esperienza pratica: 6-12 mesi
- Training: AWS Training, A Cloud Guru, Udemy
- Practice exams
- Hands-on labs

#### AWS Certified Developer - Associate

Focus su sviluppo applicazioni cloud.

**Domini:**
- Development with AWS services (32%)
- Security (26%)
- Deployment (24%)
- Troubleshooting and optimization (18%)

#### AWS Certified Solutions Architect - Professional

**Livello**: Professional
**Costo**: $300
**Durata**: 180 minuti

Certificazione avanzata per architetti cloud.

## Esempi Pratici Completi

### Esempio 1: Deploy Applicazione Web 3-Tier

Architettura completa con Load Balancer, Auto Scaling, RDS.

**Architecture:**
```
Internet
    ↓
CloudFront (CDN)
    ↓
Application Load Balancer
    ↓
┌────────────────────────────────┐
│      Auto Scaling Group        │
│  ┌──────┐  ┌──────┐  ┌──────┐ │
│  │ EC2  │  │ EC2  │  │ EC2  │ │
│  └──────┘  └──────┘  └──────┘ │
└────────────────────────────────┘
    ↓
┌─────────────────┐
│  RDS (Multi-AZ) │
│  ┌────┐  ┌────┐ │
│  │ M  │  │ S  │ │
│  └────┘  └────┘ │
└─────────────────┘
```

**Step 1: VPC e Networking**
```bash
# Crea VPC
VPC_ID=$(aws ec2 create-vpc \
    --cidr-block 10.0.0.0/16 \
    --query 'Vpc.VpcId' \
    --output text)

# Subnet pubbliche (per ALB)
SUBNET_PUB_1=$(aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.1.0/24 \
    --availability-zone eu-west-1a \
    --query 'Subnet.SubnetId' \
    --output text)

SUBNET_PUB_2=$(aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.2.0/24 \
    --availability-zone eu-west-1b \
    --query 'Subnet.SubnetId' \
    --output text)

# Subnet private (per EC2)
SUBNET_PRIV_1=$(aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.3.0/24 \
    --availability-zone eu-west-1a \
    --query 'Subnet.SubnetId' \
    --output text)

SUBNET_PRIV_2=$(aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.4.0/24 \
    --availability-zone eu-west-1b \
    --query 'Subnet.SubnetId' \
    --output text)

# Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
    --query 'InternetGateway.InternetGatewayId' \
    --output text)

aws ec2 attach-internet-gateway \
    --vpc-id $VPC_ID \
    --internet-gateway-id $IGW_ID
```

**Step 2: Security Groups**
```bash
# SG per ALB
ALB_SG=$(aws ec2 create-security-group \
    --group-name alb-sg \
    --description "Security group for ALB" \
    --vpc-id $VPC_ID \
    --query 'GroupId' \
    --output text)

aws ec2 authorize-security-group-ingress \
    --group-id $ALB_SG \
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0/0

# SG per EC2
EC2_SG=$(aws ec2 create-security-group \
    --group-name ec2-sg \
    --description "Security group for EC2" \
    --vpc-id $VPC_ID \
    --query 'GroupId' \
    --output text)

aws ec2 authorize-security-group-ingress \
    --group-id $EC2_SG \
    --protocol tcp \
    --port 80 \
    --source-group $ALB_SG

# SG per RDS
RDS_SG=$(aws ec2 create-security-group \
    --group-name rds-sg \
    --description "Security group for RDS" \
    --vpc-id $VPC_ID \
    --query 'GroupId' \
    --output text)

aws ec2 authorize-security-group-ingress \
    --group-id $RDS_SG \
    --protocol tcp \
    --port 5432 \
    --source-group $EC2_SG
```

**Step 3: RDS Database**
```bash
# Crea DB subnet group
aws rds create-db-subnet-group \
    --db-subnet-group-name my-db-subnet \
    --db-subnet-group-description "Subnet group for RDS" \
    --subnet-ids $SUBNET_PRIV_1 $SUBNET_PRIV_2

# Crea database
aws rds create-db-instance \
    --db-instance-identifier myapp-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --master-username admin \
    --master-user-password MySecurePass123! \
    --allocated-storage 20 \
    --vpc-security-group-ids $RDS_SG \
    --db-subnet-group-name my-db-subnet \
    --multi-az \
    --backup-retention-period 7
```

**Step 4: Launch Template per EC2**
```bash
# User data script
cat > user-data.sh << 'EOF'
#!/bin/bash
yum update -y
yum install -y httpd
systemctl start httpd
systemctl enable httpd
echo "<h1>Hello from $(hostname -f)</h1>" > /var/www/html/index.html
EOF

# Crea launch template
aws ec2 create-launch-template \
    --launch-template-name web-server-template \
    --version-description "Version 1" \
    --launch-template-data '{
        "ImageId": "ami-0c55b159cbfafe1f0",
        "InstanceType": "t3.micro",
        "SecurityGroupIds": ["'$EC2_SG'"],
        "UserData": "'$(base64 -w 0 user-data.sh)'",
        "TagSpecifications": [{
            "ResourceType": "instance",
            "Tags": [{"Key": "Name", "Value": "WebServer"}]
        }]
    }'
```

**Step 5: Application Load Balancer**
```bash
# Crea ALB
ALB_ARN=$(aws elbv2 create-load-balancer \
    --name my-alb \
    --subnets $SUBNET_PUB_1 $SUBNET_PUB_2 \
    --security-groups $ALB_SG \
    --query 'LoadBalancers[0].LoadBalancerArn' \
    --output text)

# Crea target group
TG_ARN=$(aws elbv2 create-target-group \
    --name web-tg \
    --protocol HTTP \
    --port 80 \
    --vpc-id $VPC_ID \
    --health-check-path / \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text)

# Crea listener
aws elbv2 create-listener \
    --load-balancer-arn $ALB_ARN \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=$TG_ARN
```

**Step 6: Auto Scaling Group**
```bash
# Crea Auto Scaling Group
aws autoscaling create-auto-scaling-group \
    --auto-scaling-group-name web-asg \
    --launch-template LaunchTemplateName=web-server-template,Version='$Latest' \
    --min-size 2 \
    --max-size 6 \
    --desired-capacity 2 \
    --vpc-zone-identifier "$SUBNET_PRIV_1,$SUBNET_PRIV_2" \
    --target-group-arns $TG_ARN \
    --health-check-type ELB \
    --health-check-grace-period 300

# Scaling policy
aws autoscaling put-scaling-policy \
    --auto-scaling-group-name web-asg \
    --policy-name cpu-scale-out \
    --policy-type TargetTrackingScaling \
    --target-tracking-configuration '{
        "PredefinedMetricSpecification": {
            "PredefinedMetricType": "ASGAverageCPUUtilization"
        },
        "TargetValue": 70.0
    }'
```

### Esempio 2: Serverless API con Lambda e API Gateway

**Step 1: Lambda Function**
```python
# lambda_function.py
import json
import boto3
from decimal import Decimal

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('Users')

def lambda_handler(event, context):
    http_method = event['httpMethod']
    path = event['path']
    
    if http_method == 'GET' and path == '/users':
        # List users
        response = table.scan()
        return {
            'statusCode': 200,
            'headers': {'Content-Type': 'application/json'},
            'body': json.dumps(response['Items'], default=decimal_default)
        }
    
    elif http_method == 'POST' and path == '/users':
        # Create user
        body = json.loads(event['body'])
        table.put_item(Item=body)
        return {
            'statusCode': 201,
            'headers': {'Content-Type': 'application/json'},
            'body': json.dumps({'message': 'User created'})
        }
    
    return {
        'statusCode': 404,
        'body': json.dumps({'error': 'Not found'})
    }

def decimal_default(obj):
    if isinstance(obj, Decimal):
        return float(obj)
    raise TypeError
```

**Step 2: Deploy**
```bash
# Package
zip function.zip lambda_function.py

# Create IAM role
aws iam create-role \
    --role-name lambda-dynamodb-role \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"Service": "lambda.amazonaws.com"},
            "Action": "sts:AssumeRole"
        }]
    }'

# Attach policies
aws iam attach-role-policy \
    --role-name lambda-dynamodb-role \
    --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

aws iam attach-role-policy \
    --role-name lambda-dynamodb-role \
    --policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess

# Create Lambda function
aws lambda create-function \
    --function-name UsersAPI \
    --runtime python3.11 \
    --role arn:aws:iam::123456789012:role/lambda-dynamodb-role \
    --handler lambda_function.lambda_handler \
    --zip-file fileb://function.zip

# Create DynamoDB table
aws dynamodb create-table \
    --table-name Users \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
```

**Step 3: API Gateway**
```bash
# Create REST API
API_ID=$(aws apigateway create-rest-api \
    --name UsersAPI \
    --query 'id' \
    --output text)

# Get root resource
ROOT_ID=$(aws apigateway get-resources \
    --rest-api-id $API_ID \
    --query 'items[0].id' \
    --output text)

# Create /users resource
RESOURCE_ID=$(aws apigateway create-resource \
    --rest-api-id $API_ID \
    --parent-id $ROOT_ID \
    --path-part users \
    --query 'id' \
    --output text)

# Create GET method
aws apigateway put-method \
    --rest-api-id $API_ID \
    --resource-id $RESOURCE_ID \
    --http-method GET \
    --authorization-type NONE

# Integrate with Lambda
aws apigateway put-integration \
    --rest-api-id $API_ID \
    --resource-id $RESOURCE_ID \
    --http-method GET \
    --type AWS_PROXY \
    --integration-http-method POST \
    --uri arn:aws:apigateway:eu-west-1:lambda:path/2015-03-31/functions/arn:aws:lambda:eu-west-1:123456789012:function:UsersAPI/invocations

# Deploy API
aws apigateway create-deployment \
    --rest-api-id $API_ID \
    --stage-name prod

# Grant API Gateway permission to invoke Lambda
aws lambda add-permission \
    --function-name UsersAPI \
    --statement-id apigateway-invoke \
    --action lambda:InvokeFunction \
    --principal apigateway.amazonaws.com \
    --source-arn "arn:aws:execute-api:eu-west-1:123456789012:$API_ID/*/*"

# Test
curl https://$API_ID.execute-api.eu-west-1.amazonaws.com/prod/users
```

## Troubleshooting Comune

### EC2 Instance Non Raggiungibile

**Checklist:**
1. ✅ Istanza running?
   ```bash
   aws ec2 describe-instance-status --instance-ids i-xxx
   ```

2. ✅ Security Group permette traffico?
   ```bash
   aws ec2 describe-security-groups --group-ids sg-xxx
   ```

3. ✅ Network ACL?
   ```bash
   aws ec2 describe-network-acls
   ```

4. ✅ Route table corretto?
   ```bash
   aws ec2 describe-route-tables
   ```

5. ✅ Public IP assegnato?
   ```bash
   aws ec2 describe-instances --instance-ids i-xxx \
       --query 'Reservations[].Instances[].PublicIpAddress'
   ```

### S3 Access Denied

**Possibili cause:**
- IAM policy insufficienti
- Bucket policy restrittive
- Block Public Access attivo

**Debug:**
```bash
# Check bucket policy
aws s3api get-bucket-policy --bucket my-bucket

# Check public access block
aws s3api get-public-access-block --bucket my-bucket

# Check IAM permissions
aws iam simulate-principal-policy \
    --policy-source-arn arn:aws:iam::123456789012:user/myuser \
    --action-names s3:GetObject \
    --resource-arns arn:aws:s3:::my-bucket/*
```

### Lambda Timeout

**Soluzioni:**
- Aumentare timeout (max 15 min)
- Aumentare memoria (aumenta anche CPU)
- Ottimizzare codice
- Usare async processing

```bash
# Aumentare timeout e memoria
aws lambda update-function-configuration \
    --function-name MyFunction \
    --timeout 300 \
    --memory-size 1024
```

### RDS Connection Issues

**Checklist:**
- Security group permette porta database
- Istanza publicly accessible (se necessario)
- Endpoint corretto
- Credenziali corrette

```bash
# Test connessione da EC2
sudo yum install -y postgresql
psql -h mydb.xxxx.eu-west-1.rds.amazonaws.com -U admin -d mydb
```

## Esercizi Pratici

### Esercizio 1: Deploy Sito Statico su S3 + CloudFront

**Obiettivo**: Hostare un sito statico con CDN globale.

**Steps:**
1. Creare bucket S3
2. Abilitare static website hosting
3. Upload contenuti
4. Creare CloudFront distribution
5. Configurare custom domain (Route 53)

### Esercizio 2: EC2 con Auto Scaling

**Obiettivo**: Setup auto scaling basato su CPU.

**Steps:**
1. Creare launch template
2. Creare target group
3. Creare load balancer
4. Creare auto scaling group
5. Configurare scaling policy
6. Test con stress tool

### Esercizio 3: Serverless REST API

**Obiettivo**: API completa con Lambda + DynamoDB + API Gateway.

**Steps:**
1. Creare DynamoDB table
2. Scrivere Lambda functions (CRUD)
3. Configurare API Gateway
4. Implementare authentication (Cognito)
5. Deploy e test

### Esercizio 4: CI/CD Pipeline

**Obiettivo**: Pipeline automatica per deploy applicazione.

**Steps:**
1. Setup CodeCommit repository
2. Creare CodeBuild project
3. Configurare CodeDeploy
4. Creare CodePipeline
5. Test con commit

### Esercizio 5: VPC Multi-Tier

**Obiettivo**: Network architecture completa.

**Steps:**
1. Creare VPC
2. Setup subnet pubbliche e private
3. Configurare Internet Gateway e NAT Gateway
4. Creare route tables
5. Deploy applicazione multi-tier
6. Test connectivity

## Domande di Verifica

1. **Qual è la differenza tra Availability Zone e Region?**

2. **Spiega i diversi tipi di EBS volumes e quando usarli.**

3. **Come implementeresti un'architettura highly available su AWS?**

4. **Quali sono le classi di storage S3 e i relativi use cases?**

5. **Descrivi il funzionamento di AWS Lambda e i suoi limiti.**

6. **Come funziona IAM e quali sono le best practices?**

7. **Spiega la differenza tra Security Groups e Network ACLs.**

8. **Quali sono i pilastri del Well-Architected Framework?**

9. **Come ottimizzare i costi su AWS?**

10. **Quando useresti EC2 vs Lambda vs Fargate?**

## Risorse Aggiuntive

### Documentazione Ufficiale
- [AWS Documentation](https://docs.aws.amazon.com/)
- [AWS Architecture Center](https://aws.amazon.com/architecture/)
- [AWS Well-Architected](https://aws.amazon.com/architecture/well-architected/)

### Training
- [AWS Skill Builder](https://skillbuilder.aws/)
- [A Cloud Guru](https://acloudguru.com/)
- [Udemy - Stephane Maarek](https://www.udemy.com/user/stephane-maarek/)

### Hands-on
- [AWS Free Tier](https://aws.amazon.com/free/)
- [AWS Workshop Studio](https://workshops.aws/)
- [Qwiklabs](https://www.qwiklabs.com/)

### Community
- [AWS Reddit](https://www.reddit.com/r/aws/)
- [AWS re:Post](https://repost.aws/)
- [Stack Overflow - AWS tag](https://stackoverflow.com/questions/tagged/amazon-web-services)

### Blog e News
- [AWS Blog](https://aws.amazon.com/blogs/)
- [AWS News Blog](https://aws.amazon.com/blogs/aws/)
- [Last Week in AWS](https://www.lastweekinaws.com/)

---

*Documento aggiornato - 2024*
