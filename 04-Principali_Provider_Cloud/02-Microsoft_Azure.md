# Microsoft Azure

## Introduzione a Microsoft Azure

### Storia ed Evoluzione

Microsoft Azure (precedentemente Windows Azure) è la piattaforma cloud di Microsoft, lanciata ufficialmente nel 2010.

**Timeline chiave:**
- **2008**: Annuncio di Windows Azure
- **2010**: Lancio commerciale
- **2014**: Rinominato in Microsoft Azure
- **2015**: Azure Resource Manager (ARM)
- **2017**: Azure Functions (serverless)
- **2019**: Azure Arc (hybrid cloud)
- **2023**: 21% market share, secondo provider cloud mondiale
- **2024**: 60+ regioni, oltre 200 servizi

### Posizione nel Mercato

Azure è il **secondo provider cloud** al mondo:
- **21%** di market share globale
- **Oltre 95%** delle Fortune 500 usano Azure
- **60+ regioni** geografiche
- Fatturato annuo di oltre **$60 miliardi**
- Focus su hybrid cloud e enterprise

### Filosofia Azure

Principi fondamentali di Azure:
1. **Hybrid cloud first**: Integrazione on-premise e cloud
2. **Enterprise ready**: Compliance e governance
3. **Open and flexible**: Supporto multi-piattaforma
4. **AI-powered**: Intelligenza artificiale integrata
5. **Developer friendly**: Integrazione Visual Studio e GitHub

## Architettura Globale Azure

### Geografie e Regioni

Azure organizza l'infrastruttura in **Geografie** e **Regioni**.

**Geografia**: Area geopolitica con requisiti di data residency
**Regione**: Set di datacenter connessi

**Geografie principali:**
```
Americas:
  - United States (30+ regioni)
  - Canada (2 regioni)
  - Brazil (2 regioni)

Europe:
  - UK (2 regioni)
  - France (2 regioni)
  - Germany (2 regioni)
  - Italy (1 regione) ← Milano!
  - Switzerland (2 regioni)
  - Norway (2 regioni)
  - Sweden (1 regione)

Asia Pacific:
  - Japan (2 regioni)
  - Australia (3 regioni)
  - India (3 regioni)
  - Singapore, Korea, etc.

Middle East & Africa:
  - UAE (2 regioni)
  - South Africa (2 regioni)
```

### Availability Zones

Datacenter fisicamente separati all'interno di una regione.

**Caratteristiche:**
- **3+ zone** per regione (dove supportato)
- Latenza < 2ms tra zone
- Alimentazione, rete, cooling indipendenti
- Protezione da guasti datacenter

```
┌───────────────────────────────────────┐
│      Region: West Europe (Amsterdam)  │
│                                       │
│  ┌──────┐    ┌──────┐    ┌──────┐     │
│  │ AZ 1 │    │ AZ 2 │    │ AZ 3 │     │
│  │      │    │      │    │      │     │
│  │ DC   │    │ DC   │    │ DC   │     │
│  │ DC   │    │ DC   │    │ DC   │     │
│  └──────┘    └──────┘    └──────┘     │
│         Low-latency network           │
└───────────────────────────────────────┘
```

### Region Pairs

Ogni regione è accoppiata con un'altra per disaster recovery.

**Esempi:**
- West Europe ↔ North Europe
- East US ↔ West US
- UK South ↔ UK West

**Vantaggi:**
- Update sequenziali (mai entrambe insieme)
- Priority recovery in caso di outage
- Data replication automatica per alcuni servizi

### Edge Locations

**Azure CDN**: 200+ PoP globali
**Azure Front Door**: Global load balancing e WAF

## Servizi Azure per Categoria

### 1. Compute

#### Azure Virtual Machines

Server virtuali IaaS.

**Serie VM principali:**

| Serie | Tipo | vCPU | RAM | Use Case | Prezzo/ora |
|-------|------|------|-----|----------|------------|
| **B-series** (Burstable) | General | 1-20 | 0.5-80 GB | Dev/test | €0.004+ |
| **D-series** | General Purpose | 2-64 | 8-256 GB | App general | €0.042+ |
| **E-series** | Memory Optimized | 2-64 | 16-432 GB | Database, cache | €0.050+ |
| **F-series** | Compute Optimized | 2-72 | 4-144 GB | Batch, analytics | €0.045+ |
| **N-series** | GPU | 6-24 | 56-448 GB | ML, rendering | €0.900+ |
| **M-series** | Large Memory | 8-416 | 0.2-11.4 TB | SAP HANA | €3.000+ |

**Pricing Models:**
- **Pay-as-you-go**: Prezzo orario standard
- **Reserved VM Instances**: 1-3 anni, fino a 72% sconto
- **Spot VMs**: Capacità inutilizzata, fino a 90% sconto
- **Azure Hybrid Benefit**: Riuso licenze Windows Server

**Esempio - Creare VM con Azure CLI:**
```bash
# Login
az login

# Creare resource group
az group create \
    --name MyResourceGroup \
    --location westeurope

# Creare VM
az vm create \
    --resource-group MyResourceGroup \
    --name MyVM \
    --image Ubuntu2204 \
    --size Standard_D2s_v3 \
    --admin-username azureuser \
    --generate-ssh-keys \
    --public-ip-sku Standard

# Aprire porta 80
az vm open-port \
    --resource-group MyResourceGroup \
    --name MyVM \
    --port 80

# Ottenere IP pubblico
az vm show \
    --resource-group MyResourceGroup \
    --name MyVM \
    --show-details \
    --query publicIps \
    --output tsv

# Connettersi
ssh azureuser@<public-ip>

# Installare web server
sudo apt update
sudo apt install -y nginx
```

#### Azure App Service

PaaS per web apps e API.

**Caratteristiche:**
- Supporto: .NET, Java, Node.js, Python, PHP, Ruby
- Deployment slots (staging, production)
- Auto-scaling integrato
- CI/CD integrato
- Custom domains e SSL

**Pricing Tiers:**
- **Free/Shared**: Dev/test
- **Basic**: Piccole app
- **Standard**: Production
- **Premium**: High performance
- **Isolated**: Dedicated environment

**Deploy Web App:**
```bash
# Creare App Service Plan
az appservice plan create \
    --name MyAppServicePlan \
    --resource-group MyResourceGroup \
    --sku B1 \
    --is-linux

# Creare Web App
az webapp create \
    --name myuniquewebapp123 \
    --resource-group MyResourceGroup \
    --plan MyAppServicePlan \
    --runtime "NODE:18-lts"

# Deploy da GitHub
az webapp deployment source config \
    --name myuniquewebapp123 \
    --resource-group MyResourceGroup \
    --repo-url https://github.com/user/repo \
    --branch main \
    --manual-integration

# Configurare variabili ambiente
az webapp config appsettings set \
    --name myuniquewebapp123 \
    --resource-group MyResourceGroup \
    --settings DB_HOST=mydb.postgres.database.azure.com

# Vedere logs
az webapp log tail \
    --name myuniquewebapp123 \
    --resource-group MyResourceGroup
```

#### Azure Functions

Serverless compute - FaaS.

**Caratteristiche:**
- Runtime: .NET, Java, Node.js, Python, PowerShell
- Triggers: HTTP, Timer, Queue, Blob, Event Grid
- Timeout: 5 min (Consumption), 30 min+ (Premium)
- Cold start: ~1-2 secondi

**Hosting Plans:**
- **Consumption**: Pay per execution, auto-scale
- **Premium**: Pre-warmed instances, VNet integration
- **Dedicated**: App Service Plan

**Pricing Consumption:**
- Esecuzioni: €0.169 per milione
- GB-secondo: €0.000014
- Free tier: 1M esecuzioni/mese + 400,000 GB-sec

**Esempio Function Python:**
```python
import logging
import azure.functions as func

def main(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('Python HTTP trigger function processed a request.')
    
    name = req.params.get('name')
    if not name:
        try:
            req_body = req.get_json()
            name = req_body.get('name')
        except ValueError:
            pass
    
    if name:
        return func.HttpResponse(
            f"Hello, {name}!",
            status_code=200
        )
    else:
        return func.HttpResponse(
            "Please pass a name parameter",
            status_code=400
        )
```

**Deploy:**
```bash
# Creare Function App
az functionapp create \
    --resource-group MyResourceGroup \
    --consumption-plan-location westeurope \
    --runtime python \
    --runtime-version 3.11 \
    --functions-version 4 \
    --name myuniquefunctionapp123 \
    --storage-account mystorageaccount

# Deploy code (da directory locale)
func azure functionapp publish myuniquefunctionapp123

# Invocare
curl https://myuniquefunctionapp123.azurewebsites.net/api/HttpTrigger?name=Azure
```

#### Azure Kubernetes Service (AKS)

Kubernetes gestito.

**Caratteristiche:**
- Control plane gratuito
- Integrazione con Azure AD, Azure Monitor
- Auto-upgrade cluster
- Node pools multipli
- Virtual Nodes (Serverless Kubernetes)

**Creare cluster:**
```bash
# Creare AKS cluster
az aks create \
    --resource-group MyResourceGroup \
    --name MyAKSCluster \
    --node-count 3 \
    --node-vm-size Standard_D2s_v3 \
    --enable-managed-identity \
    --enable-cluster-autoscaler \
    --min-count 1 \
    --max-count 5 \
    --network-plugin azure \
    --enable-addons monitoring

# Ottenere credenziali
az aks get-credentials \
    --resource-group MyResourceGroup \
    --name MyAKSCluster

# Verificare
kubectl get nodes

# Deploy app
kubectl apply -f deployment.yaml
```

#### Azure Container Instances (ACI)

Serverless containers - lancio rapido senza orchestrazione.

**Caratteristiche:**
- Startup in secondi
- Billing per secondo
- Linux e Windows containers
- Persistent storage con Azure Files

```bash
# Creare container
az container create \
    --resource-group MyResourceGroup \
    --name mycontainer \
    --image nginx:latest \
    --dns-name-label myuniquedns123 \
    --ports 80

# Verificare
az container show \
    --resource-group MyResourceGroup \
    --name mycontainer \
    --query instanceView.state

# Logs
az container logs \
    --resource-group MyResourceGroup \
    --name mycontainer
```

### 2. Storage

#### Azure Blob Storage

Object storage scalabile.

**Access Tiers:**

| Tier | Use Case | Costo GB/mese | Retrieval Cost |
|------|----------|---------------|----------------|
| **Hot** | Accesso frequente | €0.0184 | Basso |
| **Cool** | Accesso raro (30+ giorni) | €0.01 | Medio |
| **Cold** | Archivio (90+ giorni) | €0.0036 | Alto |
| **Archive** | Long-term (180+ giorni) | €0.00099 | Molto alto |

**Tipi di Blob:**
- **Block blobs**: File generici, media
- **Append blobs**: Log files
- **Page blobs**: Dischi VM

**Esempi CLI:**
```bash
# Creare storage account
az storage account create \
    --name mystorageaccount123 \
    --resource-group MyResourceGroup \
    --location westeurope \
    --sku Standard_LRS \
    --kind StorageV2

# Ottenere connection string
CONN_STR=$(az storage account show-connection-string \
    --name mystorageaccount123 \
    --resource-group MyResourceGroup \
    --query connectionString -o tsv)

# Creare container
az storage container create \
    --name mycontainer \
    --connection-string "$CONN_STR"

# Upload blob
az storage blob upload \
    --container-name mycontainer \
    --name myfile.txt \
    --file ./local-file.txt \
    --connection-string "$CONN_STR"

# List blobs
az storage blob list \
    --container-name mycontainer \
    --connection-string "$CONN_STR" \
    --output table

# Download
az storage blob download \
    --container-name mycontainer \
    --name myfile.txt \
    --file ./downloaded.txt \
    --connection-string "$CONN_STR"

# Configurare lifecycle policy
az storage account management-policy create \
    --account-name mystorageaccount123 \
    --resource-group MyResourceGroup \
    --policy @policy.json
```

**policy.json:**
```json
{
  "rules": [
    {
      "enabled": true,
      "name": "move-to-cool",
      "type": "Lifecycle",
      "definition": {
        "actions": {
          "baseBlob": {
            "tierToCool": {
              "daysAfterModificationGreaterThan": 30
            },
            "tierToArchive": {
              "daysAfterModificationGreaterThan": 90
            },
            "delete": {
              "daysAfterModificationGreaterThan": 365
            }
          }
        },
        "filters": {
          "blobTypes": ["blockBlob"],
          "prefixMatch": ["logs/"]
        }
      }
    }
  ]
}
```

#### Azure Files

File share gestito con protocollo SMB e NFS.

**Caratteristiche:**
- Montabile su Windows, Linux, macOS
- Condivisione cross-platform
- Snapshot per backup
- Sync con Azure File Sync

**Tiers:**
- **Premium**: SSD, bassa latenza
- **Transaction optimized**: Workload transazionali
- **Hot**: General purpose
- **Cool**: Archivio

```bash
# Creare file share
az storage share create \
    --name myshare \
    --account-name mystorageaccount123 \
    --quota 100

# Mount su Linux
sudo mkdir /mnt/azurefiles
sudo mount -t cifs //mystorageaccount123.file.core.windows.net/myshare /mnt/azurefiles \
    -o username=mystorageaccount123,password=<storage-key>,dir_mode=0777,file_mode=0777
```

#### Azure Disk Storage

Block storage per VM (simile a EBS di AWS).

**Tipi:**
- **Ultra Disk**: IOPS massime, latenza < 1ms
- **Premium SSD v2**: Performance configurabile
- **Premium SSD**: Production workloads
- **Standard SSD**: Dev/test
- **Standard HDD**: Backup, archivio

### 3. Database

#### Azure SQL Database

SQL Server gestito come PaaS.

**Deployment Options:**
- **Single Database**: Singolo database isolato
- **Elastic Pool**: Risorse condivise tra DB
- **Managed Instance**: SQL Server completo gestito

**Pricing Tiers:**
- **DTU-based**: Database Transaction Units
  - Basic: Dev/test
  - Standard: General purpose
  - Premium: Production, HA
- **vCore-based**: CPU + Memory
  - General Purpose
  - Business Critical
  - Hyperscale (fino a 100 TB)

**Creare database:**
```bash
# Creare SQL Server
az sql server create \
    --name myuniquesqlserver123 \
    --resource-group MyResourceGroup \
    --location westeurope \
    --admin-user sqladmin \
    --admin-password MySecureP@ssw0rd!

# Configurare firewall
az sql server firewall-rule create \
    --resource-group MyResourceGroup \
    --server myuniquesqlserver123 \
    --name AllowMyIP \
    --start-ip-address $(curl -s ifconfig.me) \
    --end-ip-address $(curl -s ifconfig.me)

# Creare database
az sql db create \
    --resource-group MyResourceGroup \
    --server myuniquesqlserver123 \
    --name MyDatabase \
    --service-objective S0 \
    --backup-storage-redundancy Zone

# Connettersi
sqlcmd -S myuniquesqlserver123.database.windows.net -U sqladmin -P MySecureP@ssw0rd! -d MyDatabase
```

#### Azure Cosmos DB

Database NoSQL globally distributed.

**API supportate:**
- **Core (SQL)**: Document database
- **MongoDB**: Compatibile MongoDB
- **Cassandra**: Wide-column
- **Gremlin**: Graph database
- **Table**: Key-value

**Caratteristiche:**
- Multi-region replication
- Latenza < 10ms (P99)
- 5 consistency levels
- Automatic indexing
- Serverless disponibile

**Pricing:**
- **Provisioned throughput**: RU/s (Request Units)
- **Serverless**: Pay per request
- **Autoscale**: Scale automatico

```bash
# Creare Cosmos account
az cosmosdb create \
    --name myuniquecosmosdb123 \
    --resource-group MyResourceGroup \
    --locations regionName=westeurope failoverPriority=0 \
    --default-consistency-level Session

# Creare database
az cosmosdb sql database create \
    --account-name myuniquecosmosdb123 \
    --resource-group MyResourceGroup \
    --name MyDatabase

# Creare container
az cosmosdb sql container create \
    --account-name myuniquecosmosdb123 \
    --resource-group MyResourceGroup \
    --database-name MyDatabase \
    --name MyContainer \
    --partition-key-path "/userId" \
    --throughput 400
```

#### Azure Database for PostgreSQL/MySQL/MariaDB

Database open-source gestiti.

**Deployment:**
- **Single Server**: Semplice setup (deprecato)
- **Flexible Server**: Controllo granulare, HA zone-redundant

```bash
# PostgreSQL Flexible Server
az postgres flexible-server create \
    --name myuniquepostgres123 \
    --resource-group MyResourceGroup \
    --location westeurope \
    --admin-user myadmin \
    --admin-password MySecureP@ssw0rd! \
    --sku-name Standard_D2s_v3 \
    --tier GeneralPurpose \
    --version 15 \
    --storage-size 128 \
    --high-availability ZoneRedundant

# Connettersi
psql "host=myuniquepostgres123.postgres.database.azure.com port=5432 dbname=postgres user=myadmin password=MySecureP@ssw0rd! sslmode=require"
```

#### Azure Cache for Redis

In-memory cache gestito.

**Tiers:**
- **Basic**: Single node, no SLA
- **Standard**: 2 nodi con replication
- **Premium**: Cluster, persistence, VNet
- **Enterprise**: Redis Enterprise

### 4. Networking

#### Azure Virtual Network (VNet)

Rete virtuale isolata.

**Componenti:**
- **Subnets**: Segmentazione rete
- **Network Security Groups (NSG)**: Firewall regole
- **Route Tables**: Custom routing
- **VNet Peering**: Connessione tra VNet
- **VPN Gateway**: Site-to-site, point-to-site
- **ExpressRoute**: Connessione privata dedicata

**Creare VNet:**
```bash
# Creare VNet
az network vnet create \
    --resource-group MyResourceGroup \
    --name MyVNet \
    --address-prefix 10.0.0.0/16 \
    --subnet-name FrontendSubnet \
    --subnet-prefix 10.0.1.0/24

# Aggiungere subnet
az network vnet subnet create \
    --resource-group MyResourceGroup \
    --vnet-name MyVNet \
    --name BackendSubnet \
    --address-prefix 10.0.2.0/24

# Creare NSG
az network nsg create \
    --resource-group MyResourceGroup \
    --name MyNSG

# Aggiungere regola
az network nsg rule create \
    --resource-group MyResourceGroup \
    --nsg-name MyNSG \
    --name AllowHTTP \
    --priority 100 \
    --source-address-prefixes '*' \
    --source-port-ranges '*' \
    --destination-address-prefixes '*' \
    --destination-port-ranges 80 \
    --access Allow \
    --protocol Tcp \
    --direction Inbound

# Associare NSG a subnet
az network vnet subnet update \
    --resource-group MyResourceGroup \
    --vnet-name MyVNet \
    --name FrontendSubnet \
    --network-security-group MyNSG
```

#### Azure Load Balancer

Load balancing Layer 4.

**SKU:**
- **Basic**: Free, no SLA
- **Standard**: SLA 99.99%, zone-redundant

**Tipi:**
- **Public**: Internet-facing
- **Internal**: Private

```bash
# Creare public load balancer
az network lb create \
    --resource-group MyResourceGroup \
    --name MyLoadBalancer \
    --sku Standard \
    --public-ip-address MyPublicIP \
    --frontend-ip-name MyFrontEnd \
    --backend-pool-name MyBackEndPool

# Creare health probe
az network lb probe create \
    --resource-group MyResourceGroup \
    --lb-name MyLoadBalancer \
    --name MyHealthProbe \
    --protocol tcp \
    --port 80

# Creare load balancing rule
az network lb rule create \
    --resource-group MyResourceGroup \
    --lb-name MyLoadBalancer \
    --name MyHTTPRule \
    --protocol tcp \
    --frontend-port 80 \
    --backend-port 80 \
    --frontend-ip-name MyFrontEnd \
    --backend-pool-name MyBackEndPool \
    --probe-name MyHealthProbe
```

#### Azure Application Gateway

Load balancer Layer 7 con WAF.

**Features:**
- URL-based routing
- SSL termination
- Session affinity
- WebSocket support
- Web Application Firewall (WAF)

#### Azure Front Door

Global load balancer e CDN.

**Caratteristiche:**
- Global anycast
- SSL offload
- WAF integrato
- URL routing
- Caching

### 5. Security & Identity

#### Azure Active Directory (Azure AD)

Identity and access management.

**Caratteristiche:**
- Single Sign-On (SSO)
- Multi-Factor Authentication (MFA)
- Conditional Access
- Identity Protection
- Privileged Identity Management (PIM)

**Tiers:**
- **Free**: Base features
- **Premium P1**: Hybrid identities, advanced security
- **Premium P2**: Identity Protection, PIM

#### Azure Key Vault

Gestione secrets, keys, certificates.

**Caratteristiche:**
- Hardware Security Modules (HSM)
- Access policies granulari
- Soft delete
- Audit logging

```bash
# Creare Key Vault
az keyvault create \
    --name myuniquekeyvault123 \
    --resource-group MyResourceGroup \
    --location westeurope

# Aggiungere secret
az keyvault secret set \
    --vault-name myuniquekeyvault123 \
    --name DatabasePassword \
    --value "MySecureP@ssw0rd!"

# Retrieve secret
az keyvault secret show \
    --vault-name myuniquekeyvault123 \
    --name DatabasePassword \
    --query value -o tsv

# Aggiungere key
az keyvault key create \
    --vault-name myuniquekeyvault123 \
    --name MyEncryptionKey \
    --protection software
```

#### Azure Security Center / Microsoft Defender for Cloud

Security posture management e threat protection.

**Caratteristiche:**
- Secure score
- Vulnerability assessment
- Threat detection
- Compliance dashboard
- JIT VM access

### 6. Monitoring & Management

#### Azure Monitor

Piattaforma di monitoring completa.

**Componenti:**
- **Metrics**: Time-series data
- **Logs**: Log Analytics workspace
- **Alerts**: Notifiche basate su condizioni
- **Dashboards**: Visualizzazione
- **Workbooks**: Report interattivi

```bash
# Creare Log Analytics workspace
az monitor log-analytics workspace create \
    --resource-group MyResourceGroup \
    --workspace-name MyWorkspace

# Creare alert
az monitor metrics alert create \
    --name HighCPU \
    --resource-group MyResourceGroup \
    --scopes /subscriptions/.../resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVM \
    --condition "avg Percentage CPU > 80" \
    --description "Alert when CPU exceeds 80%" \
    --evaluation-frequency 5m \
    --window-size 15m \
    --severity 2

# Query logs (KQL)
az monitor log-analytics query \
    --workspace MyWorkspace \
    --analytics-query "AzureActivity | where TimeGenerated > ago(1h) | summarize count() by OperationName"
```

#### Application Insights

APM per applicazioni.

**Features:**
- Request tracking
- Dependency tracking
- Exception logging
- Custom events
- Live metrics
- Availability tests

### 7. DevOps

#### Azure DevOps

Suite completa per DevOps.

**Servizi:**
- **Azure Boards**: Agile planning
- **Azure Repos**: Git repositories
- **Azure Pipelines**: CI/CD
- **Azure Test Plans**: Testing
- **Azure Artifacts**: Package management

**azure-pipelines.yml esempio:**
```yaml
trigger:
  - main

pool:
  vmImage: 'ubuntu-latest'

variables:
  buildConfiguration: 'Release'

stages:
  - stage: Build
    jobs:
      - job: BuildJob
        steps:
          - task: UseDotNet@2
            inputs:
              version: '7.x'
          
          - task: DotNetCoreCLI@2
            displayName: 'Build project'
            inputs:
              command: 'build'
              projects: '**/*.csproj'
              arguments: '--configuration $(buildConfiguration)'
          
          - task: DotNetCoreCLI@2
            displayName: 'Run tests'
            inputs:
              command: 'test'
              projects: '**/*Tests.csproj'
          
          - task: DotNetCoreCLI@2
            displayName: 'Publish'
            inputs:
              command: 'publish'
              publishWebProjects: true
              arguments: '--configuration $(buildConfiguration) --output $(Build.ArtifactStagingDirectory)'
          
          - task: PublishBuildArtifacts@1
            inputs:
              PathtoPublish: '$(Build.ArtifactStagingDirectory)'
              ArtifactName: 'drop'

  - stage: Deploy
    dependsOn: Build
    jobs:
      - deployment: DeployWeb
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  inputs:
                    azureSubscription: 'MyAzureSubscription'
                    appName: 'mywebapp'
                    package: '$(Pipeline.Workspace)/drop/**/*.zip'
```

## Modelli di Pricing Azure

### Azure Pricing Calculator

Tool online: https://azure.microsoft.com/pricing/calculator/

### Reserved Instances

Commitment 1 o 3 anni con sconti fino a 72%.

**Servizi supportati:**
- Virtual Machines
- SQL Database
- Cosmos DB
- App Service
- Azure Synapse Analytics

### Azure Hybrid Benefit

Riuso licenze on-premise:
- Windows Server
- SQL Server
- Red Hat Enterprise Linux
- SUSE Linux

**Risparmi:** fino a 85%

### Spot Virtual Machines

Capacità inutilizzata con sconti fino a 90%.

### Azure Cost Management

Tool per monitorare e ottimizzare costi:
- Cost analysis
- Budgets e alerts
- Recommendations
- Export automatici

## Azure Resource Manager (ARM)

Infrastructure as Code nativo di Azure.

### ARM Template

```json
{
  "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "vmName": {
      "type": "string",
      "defaultValue": "myVM"
    },
    "vmSize": {
      "type": "string",
      "defaultValue": "Standard_D2s_v3",
      "allowedValues": [
        "Standard_D2s_v3",
        "Standard_D4s_v3"
      ]
    }
  },
  "resources": [
    {
      "type": "Microsoft.Compute/virtualMachines",
      "apiVersion": "2021-11-01",
      "name": "[parameters('vmName')]",
      "location": "[resourceGroup().location]",
      "properties": {
        "hardwareProfile": {
          "vmSize": "[parameters('vmSize')]"
        },
        "storageProfile": {
          "imageReference": {
            "publisher": "Canonical",
            "offer": "0001-com-ubuntu-server-focal",
            "sku": "20_04-lts-gen2",
            "version": "latest"
          }
        },
        "osProfile": {
          "computerName": "[parameters('vmName')]",
          "adminUsername": "azureuser",
          "linuxConfiguration": {
            "disablePasswordAuthentication": true,
            "ssh": {
              "publicKeys": [
                {
                  "path": "/home/azureuser/.ssh/authorized_keys",
                  "keyData": "[parameters('sshPublicKey')]"
                }
              ]
            }
          }
        }
      }
    }
  ]
}
```

**Deploy:**
```bash
az deployment group create \
    --resource-group MyResourceGroup \
    --template-file template.json \
    --parameters vmName=myVM vmSize=Standard_D4s_v3
```

### Bicep

Linguaggio DSL più semplice di ARM JSON.

```bicep
param location string = resourceGroup().location
param vmName string = 'myVM'

@allowed([
  'Standard_D2s_v3'
  'Standard_D4s_v3'
])
param vmSize string = 'Standard_D2s_v3'

resource vm 'Microsoft.Compute/virtualMachines@2021-11-01' = {
  name: vmName
  location: location
  properties: {
    hardwareProfile: {
      vmSize: vmSize
    }
    storageProfile: {
      imageReference: {
        publisher: 'Canonical'
        offer: '0001-com-ubuntu-server-focal'
        sku: '20_04-lts-gen2'
        version: 'latest'
      }
    }
  }
}
```

## Azure Well-Architected Framework

5 pilastri per architetture cloud eccellenti.

### 1. Cost Optimization

**Principi:**
- Right-size resources
- Reserved capacity
- Auto-scaling
- Serverless where appropriate

**Tools:**
- Azure Cost Management
- Azure Advisor

### 2. Operational Excellence

**Principi:**
- IaC (ARM, Bicep, Terraform)
- CI/CD
- Monitoring e alerting
- Automation

**Tools:**
- Azure DevOps
- Azure Monitor
- Azure Automation

### 3. Performance Efficiency

**Principi:**
- Scale appropriato
- CDN per contenuti statici
- Database caching
- Async processing

**Tools:**
- Azure Front Door
- Azure Cache for Redis
- Azure Functions

### 4. Reliability

**Principi:**
- Multi-region deployment
- Availability Zones
- Backup e DR
- Health checks

**Tools:**
- Azure Site Recovery
- Azure Backup
- Traffic Manager

### 5. Security

**Principi:**
- Zero Trust
- Defense in depth
- Least privilege
- Encryption everywhere

**Tools:**
- Azure AD
- Key Vault
- Microsoft Defender for Cloud

## Certificazioni Azure

### Percorso Certificazioni

```
Fundamentals
    ↓
┌──────────────────────┐
│ Azure Fundamentals   │ AZ-900
│ (Entry level)        │
└──────────────────────┘
    ↓
Associate
    ↓
┌─────────────────┬──────────────────┬──────────────────┐
│ Administrator   │  Developer       │ Solutions        │
│ Associate       │  Associate       │ Architect        │
│ AZ-104          │  AZ-204          │ Expert           │
│                 │                  │ AZ-305           │
└─────────────────┴──────────────────┴──────────────────┘
    ↓
Expert / Specialty
    ↓
┌──────────────┬──────────────┬─────────────┬────────────┐
│ DevOps       │ Security     │ Data        │ AI         │
│ Engineer     │ Engineer     │ Engineer    │ Engineer   │
│ Expert       │ Associate    │ Associate   │ Associate  │
│ AZ-400       │ AZ-500       │ DP-203      │ AI-102     │
└──────────────┴──────────────┴─────────────┴────────────┘
```

### Dettaglio Certificazioni

#### AZ-900: Azure Fundamentals

**Livello**: Foundational
**Costo**: $99
**Durata**: 60 minuti
**Prerequisiti**: Nessuno

**Domini:**
- Cloud concepts (25-30%)
- Core Azure services (15-20%)
- Security, privacy, compliance (30-35%)
- Pricing and support (20-25%)

#### AZ-104: Azure Administrator

**Livello**: Associate
**Costo**: $165
**Durata**: 120 minuti

**Domini:**
- Manage Azure identities (20-25%)
- Implement storage (15-20%)
- Deploy and manage compute (20-25%)
- Configure and manage virtual networking (15-20%)
- Monitor and maintain Azure resources (10-15%)

#### AZ-204: Azure Developer

Focus su sviluppo applicazioni cloud.

**Domini:**
- Azure compute solutions (25-30%)
- Azure storage (15-20%)
- Azure security (20-25%)
- Monitor, troubleshoot (15-20%)
- Connect to Azure services (10-15%)

#### AZ-305: Azure Solutions Architect Expert

**Livello**: Expert
**Costo**: $165
**Prerequisito**: AZ-104

Certificazione avanzata per architetti.

## Esempi Pratici

### Esempio 1: Web App con Database

Deploy completo con App Service e SQL Database.

```bash
# Variabili
RG="MyWebAppRG"
LOCATION="westeurope"
PLAN="MyAppPlan"
WEBAPP="myuniquewebapp$(date +%s)"
SQLSERVER="mysqlserver$(date +%s)"
SQLADMIN="sqladmin"
SQLPASS="MySecure$(openssl rand -base64 12)!"

# Creare resource group
az group create --name $RG --location $LOCATION

# Creare SQL Server
az sql server create \
    --name $SQLSERVER \
    --resource-group $RG \
    --location $LOCATION \
    --admin-user $SQLADMIN \
    --admin-password "$SQLPASS"

# Firewall per Azure services
az sql server firewall-rule create \
    --resource-group $RG \
    --server $SQLSERVER \
    --name AllowAzureServices \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0

# Creare database
az sql db create \
    --resource-group $RG \
    --server $SQLSERVER \
    --name ProductionDB \
    --service-objective S0

# Creare App Service Plan
az appservice plan create \
    --name $PLAN \
    --resource-group $RG \
    --sku S1 \
    --is-linux

# Creare Web App
az webapp create \
    --resource-group $RG \
    --plan $PLAN \
    --name $WEBAPP \
    --runtime "NODE:18-lts"

# Configurare connection string
CONN_STR="Server=tcp:$SQLSERVER.database.windows.net,1433;Database=ProductionDB;User ID=$SQLADMIN;Password=$SQLPASS;Encrypt=true;"
az webapp config connection-string set \
    --resource-group $RG \
    --name $WEBAPP \
    --connection-string-type SQLAzure \
    --settings DefaultConnection="$CONN_STR"

# Enable logging
az webapp log config \
    --resource-group $RG \
    --name $WEBAPP \
    --application-logging filesystem \
    --detailed-error-messages true \
    --failed-request-tracing true \
    --web-server-logging filesystem

echo "Web App URL: https://$WEBAPP.azurewebsites.net"
echo "SQL Server: $SQLSERVER.database.windows.net"
echo "SQL Admin: $SQLADMIN"
echo "SQL Password: $SQLPASS"
```

### Esempio 2: Function App con Cosmos DB

```bash
RG="MyFunctionRG"
LOCATION="westeurope"
STORAGE="mystorage$(date +%s)"
FUNCTION="myfunc$(date +%s)"
COSMOS="mycosmosdb$(date +%s)"

# Resource group
az group create --name $RG --location $LOCATION

# Storage account
az storage account create \
    --name $STORAGE \
    --resource-group $RG \
    --location $LOCATION \
    --sku Standard_LRS

# Cosmos DB
az cosmosdb create \
    --name $COSMOS \
    --resource-group $RG \
    --locations regionName=$LOCATION

az cosmosdb sql database create \
    --account-name $COSMOS \
    --resource-group $RG \
    --name MyDatabase

az cosmosdb sql container create \
    --account-name $COSMOS \
    --resource-group $RG \
    --database-name MyDatabase \
    --name MyContainer \
    --partition-key-path "/id"

# Function App
az functionapp create \
    --resource-group $RG \
    --consumption-plan-location $LOCATION \
    --runtime node \
    --runtime-version 18 \
    --functions-version 4 \
    --name $FUNCTION \
    --storage-account $STORAGE

# Configurare Cosmos DB connection
COSMOS_CONN=$(az cosmosdb keys list \
    --name $COSMOS \
    --resource-group $RG \
    --type connection-strings \
    --query "connectionStrings[0].connectionString" -o tsv)

az functionapp config appsettings set \
    --name $FUNCTION \
    --resource-group $RG \
    --settings "CosmosDBConnection=$COSMOS_CONN"

echo "Function App: https://$FUNCTION.azurewebsites.net"
```

## Troubleshooting

### VM Non Accessibile

```bash
# Verificare stato
az vm get-instance-view \
    --resource-group MyResourceGroup \
    --name MyVM \
    --query instanceView.statuses

# Verificare NSG
az network nsg rule list \
    --resource-group MyResourceGroup \
    --nsg-name MyNSG \
    --output table

# Boot diagnostics
az vm boot-diagnostics get-boot-log \
    --resource-group MyResourceGroup \
    --name MyVM
```

### App Service 500 Errors

```bash
# Enable logging
az webapp log config \
    --resource-group MyResourceGroup \
    --name MyWebApp \
    --application-logging filesystem \
    --level verbose

# Stream logs
az webapp log tail \
    --resource-group MyResourceGroup \
    --name MyWebApp

# Download logs
az webapp log download \
    --resource-group MyResourceGroup \
    --name MyWebApp \
    --log-file logs.zip
```

## Esercizi Pratici

### Esercizio 1: Deploy Multi-Tier App
Deploy app con Load Balancer, VM Scale Set, SQL Database.

### Esercizio 2: Serverless API
Creare API REST con Azure Functions e Cosmos DB.

### Esercizio 3: CI/CD Pipeline
Implementare pipeline completa con Azure DevOps.

### Esercizio 4: VNet Peering
Configurare comunicazione tra VNet in regioni diverse.

### Esercizio 5: Monitoring Dashboard
Setup dashboard con Application Insights e Log Analytics.

## Domande di Verifica

1. Qual è la differenza tra Availability Zone e Region Pair?
2. Confronta App Service vs Azure Functions vs Container Instances
3. Quali sono i blob storage tiers e quando usarli?
4. Come implementare HA per applicazione web su Azure?
5. Spiega Azure AD e i suoi use cases principali
6. Differenza tra ARM templates e Bicep?
7. Quali sono i pilastri del Well-Architected Framework?
8. Come monitorare performance di un'applicazione?
9. Strategie di cost optimization su Azure?
10. Quando usare Cosmos DB vs SQL Database?

## Risorse

### Documentazione
- [Azure Documentation](https://docs.microsoft.com/azure/)
- [Azure Architecture Center](https://docs.microsoft.com/azure/architecture/)

### Training
- [Microsoft Learn](https://docs.microsoft.com/learn/)
- [Pluralsight Azure](https://www.pluralsight.com/paths/microsoft-azure)

### Tools
- [Azure Portal](https://portal.azure.com/)
- [Azure CLI Docs](https://docs.microsoft.com/cli/azure/)

### Community
- [Azure Tech Community](https://techcommunity.microsoft.com/t5/azure/ct-p/Azure)
- [Stack Overflow - Azure](https://stackoverflow.com/questions/tagged/azure)

---

*Documento aggiornato - 2024*
