# Google Cloud Platform (GCP)

## Introduzione a GCP

Google Cloud Platform è la piattaforma cloud di Google, costruita sulla stessa infrastruttura che alimenta i servizi Google come Search, Gmail e YouTube.

**Timeline:**
- **2008**: Google App Engine (primo servizio PaaS)
- **2012**: Google Compute Engine
- **2013**: Rinominato Google Cloud Platform
- **2016**: Google Kubernetes Engine (GKE)
- **2018**: Cloud Run
- **2024**: 10% market share, terzo provider cloud

### Vantaggi Competitivi GCP

- **Network globale privato**: Rete fiber privata di Google
- **Innovazione Kubernetes**: Nato in Google, GKE è leader
- **Big Data e ML**: BigQuery, TensorFlow, Vertex AI
- **Pricing trasparente**: Sustained use discounts automatici
- **Live migration**: VM migrate senza downtime

## Architettura Globale

### Regioni e Zone

**40+ regioni** e **121+ zone** nel mondo.

**Regioni principali:**
```
Americas: us-central1, us-east1, us-west1, southamerica-east1
Europe: europe-west1, europe-west4, europe-southwest1 (Madrid)
Asia: asia-southeast1, asia-northeast1, australia-southeast1
```

**Zone**: ogni regione ha 3+ zone (es: us-central1-a, us-central1-b, us-central1-c)

## Servizi Principali

### 1. Compute

#### Compute Engine (VM)

**Machine Types:**
- **General Purpose**: E2, N2, N2D, N1
- **Compute Optimized**: C2, C2D
- **Memory Optimized**: M1, M2
- **Accelerator Optimized**: A2 (GPU)

**Esempio gcloud:**
```bash
# Creare VM
gcloud compute instances create my-instance \
    --zone=europe-west1-b \
    --machine-type=e2-medium \
    --image-family=ubuntu-2204-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=20GB \
    --boot-disk-type=pd-standard

# SSH
gcloud compute ssh my-instance --zone=europe-west1-b

# Stop
gcloud compute instances stop my-instance --zone=europe-west1-b

# Delete
gcloud compute instances delete my-instance --zone=europe-west1-b
```

#### Cloud Functions

Serverless FaaS.

```python
# main.py
import functions_framework

@functions_framework.http
def hello_http(request):
    name = request.args.get('name', 'World')
    return f'Hello {name}!'
```

```bash
# Deploy
gcloud functions deploy hello-http \
    --runtime python311 \
    --trigger-http \
    --allow-unauthenticated \
    --region europe-west1
```

#### Google Kubernetes Engine (GKE)

```bash
# Creare cluster
gcloud container clusters create my-cluster \
    --zone europe-west1-b \
    --num-nodes 3 \
    --machine-type e2-medium \
    --enable-autoscaling \
    --min-nodes 1 \
    --max-nodes 5

# Get credentials
gcloud container clusters get-credentials my-cluster --zone europe-west1-b

# Deploy
kubectl apply -f deployment.yaml
```

#### Cloud Run

Serverless containers.

```bash
# Deploy da container
gcloud run deploy myservice \
    --image gcr.io/project/image:tag \
    --platform managed \
    --region europe-west1 \
    --allow-unauthenticated
```

### 2. Storage

#### Cloud Storage

Object storage (equivalente S3).

**Storage Classes:**
- **Standard**: Accesso frequente
- **Nearline**: < 1 volta/mese
- **Coldline**: < 1 volta/trimestre
- **Archive**: < 1 volta/anno

```bash
# Creare bucket
gsutil mb -l europe-west1 gs://my-unique-bucket-name/

# Upload
gsutil cp file.txt gs://my-unique-bucket-name/

# Download
gsutil cp gs://my-unique-bucket-name/file.txt ./

# Sync
gsutil rsync -r ./dir gs://my-unique-bucket-name/dir/
```

### 3. Database

#### Cloud SQL

MySQL, PostgreSQL, SQL Server gestiti.

```bash
# Creare istanza PostgreSQL
gcloud sql instances create my-instance \
    --database-version=POSTGRES_15 \
    --tier=db-f1-micro \
    --region=europe-west1

# Creare database
gcloud sql databases create mydb --instance=my-instance

# Connettersi
gcloud sql connect my-instance --user=postgres
```

#### Cloud Spanner

Database relazionale globally distributed.

**Caratteristiche:**
- Strong consistency
- Horizontal scaling
- 99.999% availability
- SQL standard

#### Firestore

Database NoSQL document-oriented.

```bash
# Creare database
gcloud firestore databases create --region=europe-west1
```

#### Bigtable

NoSQL wide-column per big data.

### 4. Networking

#### VPC

```bash
# Creare VPC
gcloud compute networks create my-vpc --subnet-mode=custom

# Creare subnet
gcloud compute networks subnets create my-subnet \
    --network=my-vpc \
    --region=europe-west1 \
    --range=10.0.1.0/24

# Firewall rule
gcloud compute firewall-rules create allow-http \
    --network=my-vpc \
    --allow=tcp:80 \
    --source-ranges=0.0.0.0/0
```

#### Cloud Load Balancing

Global load balancer con anycast IP.

### 5. Big Data & Analytics

#### BigQuery

Data warehouse serverless.

```bash
# Query
bq query --use_legacy_sql=false \
'SELECT name, COUNT(*) as count
FROM `project.dataset.table`
GROUP BY name
ORDER BY count DESC
LIMIT 10'

# Load data
bq load --source_format=CSV dataset.table gs://bucket/file.csv schema.json
```

#### Dataflow

Apache Beam gestito per stream e batch processing.

#### Pub/Sub

Message queue e streaming.

```bash
# Creare topic
gcloud pubsub topics create my-topic

# Subscribe
gcloud pubsub subscriptions create my-sub --topic=my-topic

# Publish
gcloud pubsub topics publish my-topic --message="Hello World"

# Pull
gcloud pubsub subscriptions pull my-sub --auto-ack
```

### 6. AI & Machine Learning

#### Vertex AI

Piattaforma ML unificata.

**Servizi:**
- AutoML
- Custom Training
- Model deployment
- Feature Store
- Pipelines

#### Pre-trained APIs

- Vision AI
- Natural Language AI
- Translation AI
- Speech-to-Text / Text-to-Speech

## Pricing

### Sustained Use Discounts

Sconto automatico fino a 30% per VM che girano costantemente.

### Committed Use Discounts

1-3 anni commitment, fino a 57% sconto.

### Preemptible VMs / Spot VMs

Fino a 91% sconto, possono essere terminate.

### Free Tier

- Compute Engine: 1 f1-micro/mese
- Cloud Storage: 5 GB
- BigQuery: 1 TB query/mese
- Cloud Functions: 2M invocations/mese

## Certificazioni GCP

### Professional Cloud Architect

Certificazione più richiesta.

**Domini:**
- Design and plan (24%)
- Manage and provision (20%)
- Security and compliance (20%)
- Analyze and optimize (20%)
- Implementation (16%)

**Costo**: $200
**Durata**: 120 minuti

### Altre Certificazioni

- Associate Cloud Engineer
- Professional Data Engineer
- Professional Cloud Developer
- Professional Cloud DevOps Engineer
- Professional Cloud Security Engineer
- Professional Machine Learning Engineer

## Esercizi Pratici

1. Deploy app su Cloud Run
2. Setup BigQuery data warehouse
3. Creare ML model con Vertex AI
4. Implementare Pub/Sub pipeline
5. Deploy Kubernetes app su GKE

## Domande di Verifica

1. Differenza tra Cloud Run e Cloud Functions?
2. Quando usare Cloud Spanner vs Cloud SQL?
3. Come funzionano i sustained use discounts?
4. Vantaggi di BigQuery rispetto a database tradizionali?
5. Architettura di GKE Autopilot vs Standard?

## Risorse

- [GCP Documentation](https://cloud.google.com/docs)
- [Qwiklabs](https://www.qwiklabs.com/)
- [Coursera GCP Specialization](https://www.coursera.org/googlecloud)

---

*Documento aggiornato - 2024*
