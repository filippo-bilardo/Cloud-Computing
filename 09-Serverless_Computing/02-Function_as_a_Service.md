# Function as a Service (FaaS)

## Introduzione a FaaS

**Function as a Service** (FaaS) è il paradigma serverless dove si eseguono funzioni individuali in risposta a eventi, senza gestire server.

### Principi FaaS

- **Single-purpose functions**: Ogni funzione fa una cosa sola
- **Event-driven**: Trigger basati su eventi
- **Stateless**: Nessuno stato tra invocazioni
- **Short-lived**: Esecuzioni brevi (secondi/minuti)
- **Auto-scaling**: Scaling automatico e trasparente

---

## AWS Lambda

**AWS Lambda** è il servizio FaaS di Amazon, lanciato nel 2014.

### Architettura Lambda

```
┌─────────────────────────────────────────────┐
│              Event Sources                  │
├──────────┬──────────┬──────────┬────────────┤
│ API GW   │ S3       │ DynamoDB │ SQS/SNS    │
└────┬─────┴────┬─────┴────┬─────┴────┬───────┘
     │          │          │          │
     └──────────┴──────────┴──────────┘
                    │
            ┌───────▼────────┐
            │  Lambda        │
            │  Function      │
            └───────┬────────┘
                    │
     ┌──────────────┴──────────────┐
     │                             │
┌────▼─────┐              ┌────────▼──────┐
│ VPC      │              │ AWS Services  │
│ Resources│              │ (S3, DynamoDB)│
└──────────┘              └───────────────┘
```

### Hello World Lambda (Node.js)

```javascript
// index.js
exports.handler = async (event) => {
    console.log('Event:', JSON.stringify(event));
    
    const response = {
        statusCode: 200,
        headers: {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
        body: JSON.stringify({
            message: 'Hello from Lambda!',
            timestamp: new Date().toISOString(),
            event: event
        })
    };
    
    return response;
};
```

### Lambda con API Gateway

```javascript
// REST API handler
exports.handler = async (event) => {
    const method = event.httpMethod;
    const path = event.path;
    const body = event.body ? JSON.parse(event.body) : null;
    
    console.log(`${method} ${path}`, body);
    
    switch (method) {
        case 'GET':
            return {
                statusCode: 200,
                body: JSON.stringify({ items: await getItems() })
            };
        
        case 'POST':
            const newItem = await createItem(body);
            return {
                statusCode: 201,
                body: JSON.stringify(newItem)
            };
        
        default:
            return {
                statusCode: 405,
                body: JSON.stringify({ error: 'Method not allowed' })
            };
    }
};

async function getItems() {
    const AWS = require('aws-sdk');
    const dynamodb = new AWS.DynamoDB.DocumentClient();
    
    const result = await dynamodb.scan({
        TableName: process.env.TABLE_NAME
    }).promise();
    
    return result.Items;
}

async function createItem(item) {
    const AWS = require('aws-sdk');
    const dynamodb = new AWS.DynamoDB.DocumentClient();
    const { v4: uuidv4 } = require('uuid');
    
    const newItem = {
        ...item,
        id: uuidv4(),
        createdAt: new Date().toISOString()
    };
    
    await dynamodb.put({
        TableName: process.env.TABLE_NAME,
        Item: newItem
    }).promise();
    
    return newItem;
}
```

### Lambda con S3 Trigger

```javascript
// Image resizer
const AWS = require('aws-sdk');
const sharp = require('sharp');

const s3 = new AWS.S3();

exports.handler = async (event) => {
    // S3 event contiene info su file caricato
    const bucket = event.Records[0].s3.bucket.name;
    const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));
    
    console.log(`Processing ${bucket}/${key}`);
    
    // Download immagine originale
    const originalImage = await s3.getObject({
        Bucket: bucket,
        Key: key
    }).promise();
    
    // Resize con sharp
    const resized = await sharp(originalImage.Body)
        .resize(300, 300, { fit: 'inside' })
        .jpeg({ quality: 80 })
        .toBuffer();
    
    // Upload thumbnail
    const thumbnailKey = `thumbnails/${key}`;
    await s3.putObject({
        Bucket: bucket,
        Key: thumbnailKey,
        Body: resized,
        ContentType: 'image/jpeg'
    }).promise();
    
    console.log(`Thumbnail created: ${thumbnailKey}`);
    
    return {
        statusCode: 200,
        body: JSON.stringify({
            original: key,
            thumbnail: thumbnailKey
        })
    };
};
```

### Lambda con DynamoDB Streams

```javascript
// Real-time aggregation
exports.handler = async (event) => {
    const AWS = require('aws-sdk');
    const dynamodb = new AWS.DynamoDB.DocumentClient();
    
    for (const record of event.Records) {
        if (record.eventName === 'INSERT') {
            const newItem = AWS.DynamoDB.Converter.unmarshall(record.dynamodb.NewImage);
            
            console.log('New item:', newItem);
            
            // Update aggregate
            await dynamodb.update({
                TableName: process.env.AGGREGATE_TABLE,
                Key: { category: newItem.category },
                UpdateExpression: 'ADD #count :inc, #total :amount',
                ExpressionAttributeNames: {
                    '#count': 'count',
                    '#total': 'total'
                },
                ExpressionAttributeValues: {
                    ':inc': 1,
                    ':amount': newItem.amount || 0
                }
            }).promise();
        }
    }
    
    return { statusCode: 200 };
};
```

### Lambda Layers

**Layers** permettono di condividere codice tra funzioni:

```bash
# Struttura layer
layer/
└── nodejs/
    └── node_modules/
        ├── aws-sdk/
        ├── lodash/
        └── moment/

# Creare layer
zip -r layer.zip nodejs/

# Pubblicare layer
aws lambda publish-layer-version \
  --layer-name my-dependencies \
  --zip-file fileb://layer.zip \
  --compatible-runtimes nodejs18.x

# Usare layer in funzione
aws lambda update-function-configuration \
  --function-name my-function \
  --layers arn:aws:lambda:eu-west-1:123456789012:layer:my-dependencies:1
```

### Lambda Provisioned Concurrency

```bash
# Elimina cold start con istanze pre-warm
aws lambda put-provisioned-concurrency-config \
  --function-name my-function \
  --provisioned-concurrent-executions 5

# Alias con versione specifica
aws lambda create-alias \
  --function-name my-function \
  --name production \
  --function-version 3

# Provisioned concurrency su alias
aws lambda put-provisioned-concurrency-config \
  --function-name my-function:production \
  --provisioned-concurrent-executions 10
```

### Lambda con VPC

```javascript
// Lambda in VPC per accesso RDS
const mysql = require('mysql2/promise');

let connection;

exports.handler = async (event) => {
    // Riusa connessione
    if (!connection) {
        connection = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME
        });
    }
    
    const [rows] = await connection.execute(
        'SELECT * FROM users WHERE id = ?',
        [event.userId]
    );
    
    return {
        statusCode: 200,
        body: JSON.stringify(rows[0])
    };
};
```

---

## Azure Functions

**Azure Functions** supporta più linguaggi e modelli di hosting.

### Hosting Plans

1. **Consumption Plan**: Serverless puro, pay-per-execution
2. **Premium Plan**: Provisioned instances, no cold start
3. **Dedicated Plan**: App Service Plan, costi fissi

### HTTP Trigger (Node.js)

```javascript
// index.js
module.exports = async function (context, req) {
    context.log('HTTP trigger function processed a request.');

    const name = (req.query.name || (req.body && req.body.name));
    
    if (name) {
        context.res = {
            status: 200,
            body: {
                message: `Hello, ${name}!`,
                timestamp: new Date().toISOString()
            }
        };
    } else {
        context.res = {
            status: 400,
            body: { error: "Please pass a name" }
        };
    }
};

// function.json
{
  "bindings": [
    {
      "authLevel": "function",
      "type": "httpTrigger",
      "direction": "in",
      "name": "req",
      "methods": ["get", "post"]
    },
    {
      "type": "http",
      "direction": "out",
      "name": "res"
    }
  ]
}
```

### Bindings (Input/Output)

```javascript
// Cosmos DB input binding + Queue output
module.exports = async function (context, req) {
    // Input: leggi da Cosmos DB (automatico via binding)
    const document = context.bindings.inputDocument;
    
    // Business logic
    const processed = {
        ...document,
        processed: true,
        processedAt: new Date().toISOString()
    };
    
    // Output: scrivi in queue (automatico via binding)
    context.bindings.outputQueueItem = processed;
    
    context.res = {
        status: 200,
        body: processed
    };
};

// function.json
{
  "bindings": [
    {
      "type": "httpTrigger",
      "direction": "in",
      "name": "req"
    },
    {
      "type": "cosmosDB",
      "direction": "in",
      "name": "inputDocument",
      "databaseName": "MyDatabase",
      "collectionName": "MyCollection",
      "id": "{Query.id}",
      "connectionStringSetting": "CosmosDBConnection"
    },
    {
      "type": "queue",
      "direction": "out",
      "name": "outputQueueItem",
      "queueName": "processed-items",
      "connection": "AzureWebJobsStorage"
    },
    {
      "type": "http",
      "direction": "out",
      "name": "res"
    }
  ]
}
```

### Durable Functions (Stateful Workflows)

```javascript
// Orchestrator
const df = require("durable-functions");

module.exports = df.orchestrator(function* (context) {
    const outputs = [];
    
    // Sequenziale
    outputs.push(yield context.df.callActivity("Step1", "data"));
    outputs.push(yield context.df.callActivity("Step2", outputs[0]));
    
    // Parallelo
    const parallelTasks = [
        context.df.callActivity("TaskA", null),
        context.df.callActivity("TaskB", null),
        context.df.callActivity("TaskC", null)
    ];
    outputs.push(yield context.df.Task.all(parallelTasks));
    
    return outputs;
});

// Activity function
module.exports = async function (context) {
    const input = context.bindings.input;
    
    // Long running task
    await processData(input);
    
    return { status: 'completed', data: input };
};
```

### Timer Trigger

```javascript
// Cron schedule
module.exports = async function (context, myTimer) {
    const timeStamp = new Date().toISOString();
    
    if (myTimer.isPastDue) {
        context.log('Function is running late!');
    }
    
    context.log('Timer function executed at:', timeStamp);
    
    // Cleanup job
    await cleanupOldData();
};

// function.json
{
  "bindings": [
    {
      "name": "myTimer",
      "type": "timerTrigger",
      "direction": "in",
      "schedule": "0 */5 * * * *"  // Every 5 minutes
    }
  ]
}
```

---

## Google Cloud Functions

### HTTP Function

```javascript
// index.js
exports.helloWorld = (req, res) => {
    const name = req.query.name || req.body.name || 'World';
    
    res.status(200).send({
        message: `Hello ${name}!`,
        timestamp: new Date().toISOString()
    });
};

// Deploy
// gcloud functions deploy helloWorld \
//   --runtime nodejs18 \
//   --trigger-http \
//   --allow-unauthenticated
```

### Cloud Storage Trigger

```javascript
// Trigger on file upload
exports.processFile = async (file, context) => {
    const { Storage } = require('@google-cloud/storage');
    const storage = new Storage();
    
    const bucketName = file.bucket;
    const fileName = file.name;
    
    console.log(`Processing ${bucketName}/${fileName}`);
    
    if (!fileName.endsWith('.csv')) {
        console.log('Not a CSV file, skipping');
        return;
    }
    
    // Download file
    const [fileContent] = await storage
        .bucket(bucketName)
        .file(fileName)
        .download();
    
    // Process CSV
    const rows = fileContent.toString().split('\n');
    console.log(`Processing ${rows.length} rows`);
    
    // Upload result
    await storage
        .bucket(bucketName)
        .file(`processed/${fileName}`)
        .save(`Processed ${rows.length} rows`);
};

// Deploy
// gcloud functions deploy processFile \
//   --runtime nodejs18 \
//   --trigger-resource my-bucket \
//   --trigger-event google.storage.object.finalize
```

### Pub/Sub Trigger

```javascript
// Message processing
exports.processPubSubMessage = (message, context) => {
    const data = message.data
        ? Buffer.from(message.data, 'base64').toString()
        : '{}';
    
    const parsedData = JSON.parse(data);
    
    console.log('Message received:', parsedData);
    console.log('Attributes:', message.attributes);
    
    // Process message
    processOrder(parsedData);
};

// Deploy
// gcloud functions deploy processPubSubMessage \
//   --runtime nodejs18 \
//   --trigger-topic my-topic
```

### Background Functions

```javascript
// Firestore trigger
exports.onUserCreate = (change, context) => {
    const newValue = change.after.data();
    const previousValue = change.before.data();
    
    console.log('Document created/updated:', context.params.userId);
    
    // Send welcome email
    if (!previousValue) {
        sendWelcomeEmail(newValue.email);
    }
};

// Deploy
// gcloud functions deploy onUserCreate \
//   --runtime nodejs18 \
//   --trigger-event providers/cloud.firestore/eventTypes/document.write \
//   --trigger-resource projects/MY_PROJECT/databases/(default)/documents/users/{userId}
```

---

## Comparazione Providers

| Feature | AWS Lambda | Azure Functions | GCP Cloud Functions |
|---------|-----------|-----------------|---------------------|
| **Max Timeout** | 15 min | 10 min (Consumption) | 9 min (2nd gen: 60 min) |
| **Max Memory** | 10 GB | 4 GB | 8 GB (2nd gen: 32 GB) |
| **Free Tier** | 1M req + 400K GB-s | 1M req + 400K GB-s | 2M req + 400K GB-s |
| **Cold Start** | 100-500ms | 200ms-2s | 500ms-2s |
| **Concurrency** | 1000 default (aumentabile) | 200 per function | 1000 (aumentabile) |
| **Runtimes** | Node, Python, Go, Java, .NET, Ruby | Node, Python, .NET, Java, PowerShell | Node, Python, Go, Java, .NET, Ruby, PHP |
| **VPC** | Sì | Sì (Premium/Dedicated) | Sì (Serverless VPC Connector) |

---

## Best Practices FaaS

### 1. Minimize Cold Start

```javascript
// ✅ GOOD - Dipendenze minimali
const { DynamoDB } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocument } = require('@aws-sdk/lib-dynamodb');

// ❌ BAD - Dipendenze pesanti
const _ = require('lodash');  // 70KB
const moment = require('moment');  // 160KB
```

### 2. Riusa Connessioni

```javascript
// ✅ GOOD - Connessione globale
const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const client = new DynamoDBClient({ region: 'eu-west-1' });

exports.handler = async (event) => {
    // Usa client globale
};

// ❌ BAD - Nuova connessione ogni volta
exports.handler = async (event) => {
    const client = new DynamoDBClient({ region: 'eu-west-1' });
};
```

### 3. Environment Variables

```javascript
// ✅ GOOD - Configurazione via env
const TABLE_NAME = process.env.TABLE_NAME;
const REGION = process.env.AWS_REGION;
const DEBUG = process.env.DEBUG === 'true';

exports.handler = async (event) => {
    if (DEBUG) {
        console.log('Debug:', event);
    }
};
```

### 4. Error Handling e Retry

```javascript
exports.handler = async (event) => {
    try {
        return await processEvent(event);
    } catch (error) {
        console.error('Error:', error);
        
        // Transient error: Lambda riprova automaticamente
        if (error.code === 'ThrottlingException') {
            throw error;
        }
        
        // Permanent error: invia a DLQ
        await sendToDeadLetterQueue(event, error);
        return { statusCode: 500 };
    }
};
```

### 5. Monitoring e Logging

```javascript
exports.handler = async (event, context) => {
    // Structured logging
    console.log(JSON.stringify({
        level: 'INFO',
        requestId: context.requestId,
        event: event,
        timestamp: new Date().toISOString()
    }));
    
    // Custom metrics
    const { CloudWatch } = require('@aws-sdk/client-cloudwatch');
    const cloudwatch = new CloudWatch();
    
    await cloudwatch.putMetricData({
        Namespace: 'MyApp',
        MetricData: [{
            MetricName: 'ProcessedItems',
            Value: event.Records.length,
            Unit: 'Count',
            Timestamp: new Date()
        }]
    });
};
```

### 6. Idempotency

```javascript
// Usa idempotency key per evitare duplicati
const processedIds = new Set();

exports.handler = async (event) => {
    const idempotencyKey = event.requestId;
    
    if (processedIds.has(idempotencyKey)) {
        console.log('Duplicate request, skipping');
        return { statusCode: 200, body: 'Already processed' };
    }
    
    await processRequest(event);
    processedIds.add(idempotencyKey);
    
    return { statusCode: 200 };
};
```

---

## Advanced Patterns

### 1. Lambda Destinations

```javascript
// Terraform: Success/Failure destinations
resource "aws_lambda_function" "processor" {
  # ... function config
}

resource "aws_lambda_function_event_invoke_config" "destinations" {
  function_name = aws_lambda_function.processor.function_name
  
  destination_config {
    on_success {
      destination = aws_sqs_queue.success_queue.arn
    }
    
    on_failure {
      destination = aws_sqs_queue.dlq.arn
    }
  }
}
```

### 2. Lambda Extensions

```javascript
// Custom extension per logging, monitoring, secrets
// Extension script (bash)
#!/bin/bash
set -euo pipefail

# Register extension
HEADERS="$(mktemp)"
curl -sS -LD "$HEADERS" -XPOST "http://${AWS_LAMBDA_RUNTIME_API}/2020-01-01/extension/register" \
  --header "Lambda-Extension-Name: my-extension" \
  -d '{ "events": ["INVOKE", "SHUTDOWN"] }'

EXTENSION_ID=$(grep -Fi Lambda-Extension-Identifier "$HEADERS" | tr -d '[:space:]' | cut -d: -f2)

# Event loop
while true; do
  EVENT_DATA=$(curl -sS -XGET "http://${AWS_LAMBDA_RUNTIME_API}/2020-01-01/extension/event/next" \
    --header "Lambda-Extension-Identifier: ${EXTENSION_ID}")
  
  EVENT_TYPE=$(echo "$EVENT_DATA" | jq -r '.eventType')
  
  if [[ "$EVENT_TYPE" == "SHUTDOWN" ]]; then
    exit 0
  fi
  
  # Process event (e.g., send logs to external service)
done
```

### 3. Step Functions Integration

```javascript
// Lambda as step in State Machine
exports.handler = async (event) => {
    // Validate input
    if (!event.orderId) {
        throw new Error('Missing orderId');
    }
    
    // Process step
    const order = await getOrder(event.orderId);
    const charged = await chargeCustomer(order);
    
    return {
        orderId: event.orderId,
        status: 'charged',
        transactionId: charged.id
    };
};

// State Machine definition (ASL)
{
  "StartAt": "ValidateOrder",
  "States": {
    "ValidateOrder": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:function:ValidateOrder",
      "Next": "ChargeCustomer"
    },
    "ChargeCustomer": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:function:ChargeCustomer",
      "Next": "FulfillOrder",
      "Catch": [{
        "ErrorEquals": ["PaymentFailed"],
        "Next": "RefundCustomer"
      }]
    },
    "FulfillOrder": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:function:FulfillOrder",
      "End": true
    },
    "RefundCustomer": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:function:RefundCustomer",
      "End": true
    }
  }
}
```

---

## Esercizi

1. **API REST completa**: CRUD con Lambda + API Gateway + DynamoDB
2. **Image Pipeline**: S3 trigger → resize → thumbnail → watermark
3. **Real-time Analytics**: DynamoDB Streams → Lambda → ElasticSearch
4. **Scheduled Reports**: CloudWatch Event → Lambda → generate report → SES
5. **Durable Workflow**: Azure Durable Functions per order processing
6. **Multi-cloud**: Stessa funzione deployata su AWS, Azure, GCP

---

## Domande di Verifica

1. Qual è la differenza tra AWS Lambda, Azure Functions e GCP Cloud Functions?
2. Come funzionano i Lambda Layers?
3. Cos'è Provisioned Concurrency e quando usarlo?
4. Come gestisci connessioni database in Lambda?
5. Quali sono i trigger disponibili per Lambda?
6. Come implementi retry e error handling in FaaS?
7. Cosa sono i bindings in Azure Functions?
8. Come minimizzi i cold start?
9. Qual è la differenza tra stateless e Durable Functions?
10. Come monitori le performance di funzioni serverless?

---

## Risorse Aggiuntive

- [AWS Lambda Developer Guide](https://docs.aws.amazon.com/lambda/)
- [Azure Functions Documentation](https://learn.microsoft.com/en-us/azure/azure-functions/)
- [Google Cloud Functions](https://cloud.google.com/functions/docs)
- [Serverless Framework](https://www.serverless.com/)
- [AWS SAM](https://aws.amazon.com/serverless/sam/)
