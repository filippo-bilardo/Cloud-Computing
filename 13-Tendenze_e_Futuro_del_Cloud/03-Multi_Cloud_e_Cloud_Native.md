# Multi-Cloud e Cloud Native

## Multi-Cloud Strategy

### Definizioni
- **Multi-Cloud**: Uso di multiple cloud (AWS + Azure + GCP)
- **Hybrid Cloud**: Cloud + on-premises
- **Poly-Cloud**: Best-of-breed per ogni service

### Motivazioni
1. **Avoid vendor lock-in**
2. **Compliance**: Data residency requirements
3. **Resilience**: No single point of failure
4. **Cost optimization**: Best pricing per workload
5. **Best-of-breed**: Use best services from each provider

## Multi-Cloud Architecture

### Example
```
┌─────────────────────────────────────┐
│ Application Layer                   │
│ - Common APIs                       │
│ - Abstraction layer                 │
└──────────┬──────────────────────────┘
           │
    ┌──────┴──────┬──────────┐
    │             │          │
┌───▼───┐   ┌─────▼────┐   ┌─▼────┐
│  AWS  │   │  Azure   │   │ GCP  │
├───────┤   ├──────────┤   ├──────┤
│EC2    │   │VM        │   │GCE   │
│S3     │   │Blob      │   │GCS   │
│RDS    │   │SQL DB    │   │Cloud │
│       │   │          │   │SQL   │
└───────┘   └──────────┘   └──────┘
```

## Orchestration Tools

### Kubernetes (Multi-Cloud)
```yaml
# Deploy su multiple cloud con K8s
apiVersion: v1
kind: Pod
metadata:
  name: app
spec:
  nodeSelector:
    cloud-provider: aws  # or azure, gcp
  containers:
  - name: app
    image: myapp:v1
```

### Terraform (Multi-Cloud IaC)
```hcl
# Provider AWS
provider "aws" {
  region = "eu-west-1"
}

# Provider Azure
provider "azurerm" {
  features {}
}

# Provider GCP
provider "google" {
  project = "my-project"
  region  = "europe-west1"
}

# Deploy su AWS
resource "aws_instance" "app_aws" {
  ami           = "ami-12345678"
  instance_type = "t3.micro"
}

# Deploy su Azure
resource "azurerm_linux_virtual_machine" "app_azure" {
  name                = "app-vm"
  resource_group_name = azurerm_resource_group.main.name
  location            = "westeurope"
  size                = "Standard_B2s"
}

# Deploy su GCP
resource "google_compute_instance" "app_gcp" {
  name         = "app-instance"
  machine_type = "e2-medium"
  zone         = "europe-west1-b"
}
```

### Crossplane (Kubernetes-native IaC)
```yaml
# AWS RDS via Crossplane
apiVersion: database.aws.crossplane.io/v1beta1
kind: RDSInstance
metadata:
  name: mydb
spec:
  forProvider:
    region: eu-west-1
    dbInstanceClass: db.t3.micro
    engine: postgres
    masterUsername: admin
  writeConnectionSecretToRef:
    name: db-conn
---
# Azure SQL via Crossplane
apiVersion: database.azure.crossplane.io/v1beta1
kind: SQLServer
metadata:
  name: mydb-azure
spec:
  forProvider:
    location: westeurope
    administratorLogin: admin
```

## Cloud Native Principles

### 12-Factor App
1. **Codebase**: One codebase in version control
2. **Dependencies**: Explicitly declare
3. **Config**: Store in environment
4. **Backing services**: Treat as attached resources
5. **Build, release, run**: Strict separation
6. **Processes**: Execute as stateless processes
7. **Port binding**: Export services via port
8. **Concurrency**: Scale out via process model
9. **Disposability**: Fast startup, graceful shutdown
10. **Dev/prod parity**: Keep environments similar
11. **Logs**: Treat logs as event streams
12. **Admin processes**: Run as one-off processes

### Cloud Native Architecture
```
┌─────────────────────────────────────┐
│ Service Mesh (Istio/Linkerd)        │
└──────────┬──────────────────────────┘
           │
    ┌──────┴───────┬────────────┐
┌───▼───┐    ┌─────▼────┐    ┌──▼────┐
│Service│    │ Service  │    │Service│
│   A   │◀─▶│    B     │◀─▶│   C   │
└───────┘    └──────────┘    └───────┘
    │            │             │
┌───▼────────────▼─────────────▼─────┐
│ Kubernetes Cluster                 │
└────────────────────────────────────┘
```

## Multi-Cloud Data

### Data Replication
```python
# Replicate data across clouds
import boto3
import azure.storage.blob
from google.cloud import storage

def replicate_object(key, data):
    # AWS S3
    s3 = boto3.client('s3')
    s3.put_object(Bucket='my-bucket-aws', Key=key, Body=data)
    
    # Azure Blob
    blob_client = azure.storage.blob.BlobClient(
        account_url="https://myaccount.blob.core.windows.net",
        container_name="my-container",
        blob_name=key
    )
    blob_client.upload_blob(data, overwrite=True)
    
    # GCP Storage
    gcs_client = storage.Client()
    bucket = gcs_client.bucket('my-bucket-gcp')
    blob = bucket.blob(key)
    blob.upload_from_string(data)
```

### Multi-Cloud Database
```yaml
# CockroachDB (multi-cloud distributed SQL)
apiVersion: v1
kind: Service
metadata:
  name: cockroachdb
spec:
  clusterIP: None
  selector:
    app: cockroachdb
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: cockroachdb
spec:
  serviceName: cockroachdb
  replicas: 9  # 3 per cloud
  template:
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - cockroachdb
            topologyKey: cloud-provider
```

## Service Mesh

### Istio Multi-Cluster
```yaml
# Connect clusters across clouds
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  values:
    global:
      multiCluster:
        clusterName: aws-cluster
      network: network1
---
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  values:
    global:
      multiCluster:
        clusterName: azure-cluster
      network: network2
```

## Challenges

### 1. Complexity
- **Different APIs**: Each provider unique
- **Networking**: VPN/Interconnect setup
- **Security**: Unified IAM challenging

### 2. Cost
- **Data transfer**: Expensive between clouds
- **Complexity overhead**: Extra tools/team

### 3. Compliance
- **Data residency**: Where data stored?
- **Audit**: Multiple systems to audit

## Tools Ecosystem

### Abstraction Layers
- **Pulumi**: Multi-cloud IaC with real code
- **Crossplane**: K8s-native resource provisioning
- **Terraform**: Most popular multi-cloud IaC

### Monitoring
- **Datadog**: Unified monitoring
- **New Relic**: Cross-cloud observability
- **Prometheus + Grafana**: Open-source

### Security
- **HashiCorp Vault**: Multi-cloud secrets
- **Snyk**: Multi-cloud security scanning

## Future Trends

### 1. FinOps Maturity
- **Unified cost management** across clouds
- **Automated optimization**

### 2. AI-Driven Operations
- **Auto-remediation**
- **Predictive scaling**
- **Cost forecasting**

### 3. Wasm on Edge
- **Portable workloads**
- **Run anywhere**

### 4. Distributed Cloud
- **Cloud extends to edge**
- **Unified control plane**

## Best Practices

1. **Start simple**: Single cloud first
2. **Abstract early**: Use K8s, Terraform
3. **Automate everything**: IaC, CI/CD
4. **Monitor holistically**: Unified observability
5. **Plan data strategy**: Minimize cross-cloud transfer
6. **Skills investment**: Train team on multiple platforms

## Esercizi
1. Deploy app su AWS + Azure con Terraform
2. Setup Kubernetes federation
3. Implement multi-cloud monitoring
4. Design disaster recovery across clouds
5. Compare costs: single vs multi-cloud

## Domande
1. Quando ha senso multi-cloud?
2. Come gestisci networking tra clouds?
3. Quali tool usi per multi-cloud IaC?
4. Come unified monitoring?
5. Quali sono challenges principali?

---

## Conclusione del Corso

Congratulazioni! 🎉 Hai completato il corso di Cloud Computing.

### Hai imparato:
- ✅ Fondamenti cloud (IaaS, PaaS, SaaS)
- ✅ Virtualizzazione e container
- ✅ AWS, Azure, GCP services
- ✅ Storage, database, networking
- ✅ Security e compliance
- ✅ DevOps, CI/CD, IaC
- ✅ Serverless computing
- ✅ Big Data e ML nel cloud
- ✅ Cost optimization
- ✅ Architetture cloud-native
- ✅ Tendenze future

### Prossimi Passi:
1. **Certificazioni**: AWS Solutions Architect, Azure Administrator
2. **Progetti pratici**: Build & deploy real applications
3. **Community**: Contribute to open-source cloud projects
4. **Stay updated**: Cloud evolves fast!

**Buon Cloud Computing! ☁️🚀**
