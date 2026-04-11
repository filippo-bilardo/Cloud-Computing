# Introduzione al Serverless

## Cos'è il Serverless?

**Serverless** è un modello di esecuzione cloud in cui il provider gestisce completamente l'infrastruttura, permettendo agli sviluppatori di concentrarsi solo sul codice.

### Definizione

> "Serverless = FaaS + BaaS + Managed Services"

- **Non significa "nessun server"** → I server esistono, ma sono completamente astratti
- **Pay-per-execution** → Paghi solo per il tempo di esecuzione effettivo
- **Auto-scaling automatico** → Da zero a migliaia di esecuzioni
- **Zero gestione infrastruttura** → Nessun provisioning, patching, scaling manuale

### Evoluzione del Cloud Computing

```
┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│  Physical   │ → │   Virtual   │ → │ Container   │ → │ Serverless  │
│   Servers   │   │   Machines  │   │             │   │             │
├─────────────┤   ├─────────────┤   ├─────────────┤   ├─────────────┤
│ Gestisci    │   │ Gestisci    │   │ Gestisci    │   │ Scrivi      │
│ hardware,   │   │ OS, runtime │   │ container   │   │ solo codice │
│ OS, app     │   │ app         │   │ e runtime   │   │             │
└─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
```

---

## Caratteristiche del Serverless

### 1. Event-Driven Architecture

Le funzioni serverless sono attivate da **eventi**:

```
┌──────────────┐     ┌──────────────┐    ┌──────────────┐
│ HTTP Request │───▶│   Lambda     │───▶│   Response   │
└──────────────┘     │   Function   │    └──────────────┘
                     └──────────────┘

┌──────────────┐     ┌──────────────┐    ┌──────────────┐
│ S3 Upload    │───▶│   Process    │───▶│ Store in DB  │
└──────────────┘     │   Image      │    └──────────────┘
                     └──────────────┘

┌──────────────┐     ┌──────────────┐    ┌──────────────┐
│ Scheduled    │───▶│   Cleanup    │───▶│   Complete   │
│ (Cron)       │     │   Function   │    └──────────────┘
└──────────────┘     └──────────────┘
```

**Eventi comuni**:
- HTTP/API Gateway
- Database changes (DynamoDB Streams)
- File upload (S3)
- Message queue (SQS, SNS)
- Scheduled (CloudWatch Events/EventBridge)
- IoT events
- Auth events (Cognito)

### 2. Stateless

Ogni esecuzione è **indipendente**:
- ✅ Nessuno stato condiviso tra invocazioni
- ✅ Scalabilità orizzontale illimitata
- ❌ Stato persistente richiede storage esterno (DB, cache)

```javascript
// ❌ BAD - State in memoria (perso tra invocazioni)
let counter = 0;

exports.handler = async (event) => {
  counter++;  // Non affidabile!
  return { count: counter };
};

// ✅ GOOD - State in database
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
  await dynamodb.update({
    TableName: 'Counters',
    Key: { id: 'visits' },
    UpdateExpression: 'ADD #count :inc',
    ExpressionAttributeNames: { '#count': 'count' },
    ExpressionAttributeValues: { ':inc': 1 }
  }).promise();
  
  return { message: 'Counter incremented' };
};
```

### 3. Auto-Scaling

```
Traffico basso:              Traffico alto:
┌─────┐                      ┌─────┬─────┬─────┬─────┬─────┐
│  1  │                      │  1  │  2  │  3  │  4  │  5  │
└─────┘                      ├─────┼─────┼─────┼─────┼─────┤
                             │  6  │  7  │  8  │  9  │ 10  │
                             └─────┴─────┴─────┴─────┴─────┘

Scale automatico: 0 → 1000+ istanze in secondi
```

### 4. Pay-per-Execution

**Modello di pricing**:
- **Invocazioni**: Numero di esecuzioni
- **Duration**: GB-secondi (memoria × tempo)
- **NO costi a idle**: Se non esegue, non paghi

```
Esempio AWS Lambda:
- 1M invocazioni gratis/mese
- $0.20 per 1M invocazioni aggiuntive
- $0.0000166667 per GB-secondo

Funzione 128MB, 200ms esecuzione, 1M invocazioni/mese:
- Invocazioni: $0 (free tier)
- Compute: 1M × 0.2s × 0.125GB × $0.0000166667 = $0.42
Totale: ~$0.42/mese
```

---

## Vantaggi del Serverless

### 1. ✅ Riduzione Costi Operativi

- **No idle costs**: Server tradizionale paga 24/7, serverless solo quando esegue
- **No gestione infrastruttura**: Tempo sviluppatori su business logic, non DevOps
- **Auto-scaling gratuito**: Nessun provisioning capacity planning

### 2. ✅ Scalabilità Automatica

- **Da zero a migliaia** di istanze automaticamente
- **Nessuna configurazione** di Auto Scaling Groups
- **Resilienza built-in**: Multi-AZ di default

### 3. ✅ Time to Market Veloce

```javascript
// Deploy completo in minuti
exports.handler = async (event) => {
  return {
    statusCode: 200,
    body: JSON.stringify({ message: 'Hello World' })
  };
};
```

### 4. ✅ Focus su Business Logic

Sviluppatori concentrati su:
- ✅ Codice applicativo
- ✅ Logica di business
- ✅ User experience

Non su:
- ❌ Provisioning server
- ❌ Patching OS
- ❌ Load balancer configuration
- ❌ Capacity planning

---

## Limitazioni e Trade-offs

### 1. ❌ Cold Start

**Problema**: Prima invocazione (o dopo idle) richiede inizializzazione.

```
┌──────────────────────────────────────────────┐
│ Cold Start (500ms - 3s)                      │
├──────────────┬───────────────────────────────┤
│ Download     │ Initialize  │ Execute         │
│ code         │ runtime     │ function        │
└──────────────┴─────────────┴─────────────────┘

┌──────────────────────────────────────────────┐
│ Warm Start (< 10ms)                          │
├──────────────────────────────────────────────┤
│                           │ Execute          │
└───────────────────────────┴──────────────────┘
```

**Mitigazioni**:
- Provisioned Concurrency (AWS Lambda)
- Minimum instances (Google Cloud Functions)
- Linguaggi compilati (Go, Rust) vs interpretati (Python, Node.js)
- Ridurre dimensione deployment package

### 2. ❌ Timeout Limitati

| Provider | Max Timeout |
|----------|-------------|
| AWS Lambda | 15 minuti |
| Azure Functions | 10 minuti (Consumption) |
| Google Cloud Functions | 9 minuti |

**Soluzioni**:
- Spezzare task lunghi in step (Step Functions)
- Usare worker asincroni (SQS + Lambda)
- Considerare Fargate/Cloud Run per task lunghi

### 3. ❌ Stateless

- Nessuna sessione tra invocazioni
- Richiede DB/cache esterno per stato
- Può aumentare latency per I/O

### 4. ❌ Vendor Lock-in

Codice specifico per provider:

```javascript
// AWS Lambda
const AWS = require('aws-sdk');
exports.handler = async (event) => { /* ... */ };

// Azure Functions
module.exports = async function (context, req) { /* ... */ };

// GCP Cloud Functions
exports.helloWorld = (req, res) => { /* ... */ };
```

**Mitigazione**: Framework multi-cloud (Serverless Framework, Terraform)

### 5. ❌ Debugging Complesso

- Nessun accesso diretto al server
- Debugging locale limitato
- Dipendenza da logging e tracing

### 6. ❌ Costi Imprevedibili

```
Traffic spike improvviso:
  Normal: 10K invocazioni/giorno = $2/mese
  Spike: 10M invocazioni/giorno = $2000/giorno!

Soluzione: Rate limiting, reserved concurrency
```

---

## Use Cases Ideali

### ✅ Quando Usare Serverless

1. **API Backend**
   ```
   API Gateway + Lambda + DynamoDB
   - Auto-scaling
   - Pay-per-request
   - Bassa manutenzione
   ```

2. **Data Processing**
   ```
   S3 Upload → Lambda → Process → Store
   - ETL pipelines
   - Image/video processing
   - Log analysis
   ```

3. **Scheduled Tasks**
   ```
   CloudWatch Event (cron) → Lambda
   - Backup notturni
   - Report generation
   - Cleanup jobs
   ```

4. **Event-Driven Workflows**
   ```
   IoT → Lambda → Process → Alert
   - Real-time processing
   - Notification systems
   ```

5. **Chatbots e Alexa Skills**
   ```
   Alexa/Slack → API Gateway → Lambda
   ```

6. **Mobile/Web Backend**
   ```
   AppSync/API Gateway + Lambda + Cognito
   - Authentication
   - CRUD operations
   - Push notifications
   ```

### ❌ Quando NON Usare Serverless

1. **Long-running tasks** (> 15 minuti)
   - Video encoding complessi
   - ML training
   → Usa: ECS/Fargate, Batch

2. **Stateful applications**
   - WebSocket persistenti
   - Game servers
   → Usa: EC2, ECS

3. **Latency-critical** (< 10ms)
   - High-frequency trading
   - Real-time gaming
   → Usa: Dedicated servers

4. **Workload costante 24/7**
   - Se CPU > 50% costante, server dedicato costa meno

5. **Compliance requirements**
   - Requisiti hardware specifici
   - Controllo completo infrastruttura

---

## Serverless vs Containers vs VMs

| Caratteristica | VMs (EC2) | Containers (ECS/K8s) | Serverless (Lambda) |
|----------------|-----------|----------------------|---------------------|
| **Gestione** | Completa | Orchestration | Zero |
| **Scaling** | Manuale/ASG | Orchestrator | Automatico |
| **Pricing** | Oraria | Oraria | Per esecuzione |
| **Cold Start** | No | Minimo | Sì (100ms-3s) |
| **Max Runtime** | Illimitato | Illimitato | 15 min |
| **Customizzazione** | Totale | Alta | Limitata |
| **Vendor Lock** | Basso | Medio | Alto |

---

## Pattern Architetturali Serverless

### 1. API Backend

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Client     │───▶│ API Gateway  │───▶│   Lambda     │
│ (Web/Mobile) │    │              │    │   Functions  │
└──────────────┘    └──────────────┘    └──────┬───────┘
                                               │
                                        ┌──────▼───────┐
                                        │   DynamoDB   │
                                        └──────────────┘
```

### 2. Event-Driven Processing

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  S3 Upload   │───▶│   Lambda     │───▶│  S3 Output   │
│  (Image)     │    │  (Resize)    │    │  (Thumbnail) │
└──────────────┘    └──────────────┘    └──────────────┘
                           │
                    ┌──────▼───────┐
                    │   DynamoDB   │
                    │  (Metadata)  │
                    └──────────────┘
```

### 3. Fan-Out/Fan-In

```
                    ┌──────────────┐
        ┌──────────▶│  Lambda 1    │──────────┐
        │           └──────────────┘          │
┌───────┴──────┐                              ▼
│   SNS/SQS    │   ┌──────────────┐    ┌──────────────┐
│   Message    │──▶│  Lambda 2    │───▶│  Aggregate   │
└───────┬──────┘   └──────────────┘    │   Results    │
        │                               └──────────────┘
        └──────────▶┌──────────────┐          ▲
                    │  Lambda 3    │──────────┘
                    └──────────────┘
```

### 4. CQRS (Command Query Responsibility Segregation)

```
Write:
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Client  │───▶│  Lambda  │───▶│ DynamoDB │
└──────────┘    │ (Write)  │    │ (Write)  │
                └──────────┘    └────┬─────┘
                                     │
                                Stream│
                                     │
Read:                           ┌────▼─────┐    ┌──────────┐
┌──────────┐    ┌──────────┐   │ Lambda   │───▶│ElasticS. │
│  Client  │───▶│  Lambda  │◀──│(Update   │    │ (Read)   │
└──────────┘    │  (Read)  │   │ Index)   │    └──────────┘
                └──────────┘   └──────────┘
```

---

## Best Practices

### 1. Minimizza Dimensione Deployment

```javascript
// ✅ GOOD - Solo dipendenze necessarie
const AWS = require('aws-sdk');

// ❌ BAD - Intera lodash (70KB+)
const _ = require('lodash');

// ✅ GOOD - Solo funzioni necessarie (5KB)
const get = require('lodash/get');
```

### 2. Riusa Connessioni

```javascript
// ✅ GOOD - Connessione riutilizzata tra invocazioni
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();  // Fuori handler

exports.handler = async (event) => {
  // Usa connessione già aperta
  return await dynamodb.get({ /* ... */ }).promise();
};

// ❌ BAD - Nuova connessione ogni volta
exports.handler = async (event) => {
  const dynamodb = new AWS.DynamoDB.DocumentClient();  // Dentro handler
  return await dynamodb.get({ /* ... */ }).promise();
};
```

### 3. Async Processing

```javascript
// ✅ GOOD - Non aspettare se non necessario
exports.handler = async (event) => {
  // Fire and forget
  sns.publish({ Message: 'Log this' }).promise();  // No await
  
  // Return subito
  return { statusCode: 200 };
};
```

### 4. Error Handling

```javascript
exports.handler = async (event) => {
  try {
    const result = await processData(event);
    return {
      statusCode: 200,
      body: JSON.stringify(result)
    };
  } catch (error) {
    console.error('Error:', error);
    
    // Retry-able error (SQS riproverà)
    if (error.code === 'ProvisionedThroughputExceededException') {
      throw error;  // Lambda riprova
    }
    
    // Non retry-able (dead letter queue)
    return {
      statusCode: 500,
      body: JSON.stringify({ error: error.message })
    };
  }
};
```

### 5. Provisioned Concurrency per Latency-Sensitive

```bash
# AWS Lambda con Provisioned Concurrency
aws lambda put-provisioned-concurrency-config \
  --function-name my-function \
  --provisioned-concurrent-executions 10
```

---

## Esercizi

1. **Hello World Serverless**: Deploy Lambda function con API Gateway
2. **Image Resizer**: S3 upload trigger → Lambda resize → S3 output
3. **Scheduled Reporter**: CloudWatch Event → Lambda → SES email report
4. **API CRUD**: API Gateway + Lambda + DynamoDB full REST API
5. **Event-Driven Workflow**: SNS → multiple Lambdas → aggregate results
6. **Cost Analysis**: Calcola costo serverless vs EC2 per vari scenari

---

## Domande di Verifica

1. Cosa significa "serverless" e cosa NON significa?
2. Quali sono i vantaggi principali del serverless?
3. Cos'è il cold start e come mitigarlo?
4. Quando NON dovresti usare serverless?
5. Qual è la differenza tra stateless e stateful?
6. Come funziona il pricing pay-per-execution?
7. Quali eventi possono triggerare una Lambda function?
8. Perché riutilizzare le connessioni fuori dall'handler?
9. Qual è la differenza tra serverless e containers?
10. Come gestisci errori e retry in serverless?

---

## Risorse Aggiuntive

- [AWS Serverless Application Lens](https://docs.aws.amazon.com/wellarchitected/latest/serverless-applications-lens/)
- [Serverless Framework](https://www.serverless.com/)
- [Azure Serverless Computing Cookbook](https://azure.microsoft.com/en-us/solutions/serverless/)
- [Google Cloud Functions Best Practices](https://cloud.google.com/functions/docs/bestpractices)
- [The Serverless Book - Chris Munns](https://www.manning.com/books/serverless-applications-with-node-js)
