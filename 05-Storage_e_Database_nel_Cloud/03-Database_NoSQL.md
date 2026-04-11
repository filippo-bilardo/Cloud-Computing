# Database NoSQL nel Cloud

## Tipi di NoSQL

### 1. Key-Value (DynamoDB, Redis)
- **Fast**: Low latency
- **Simple**: Key → Value
- **Use case**: Session store, caching

### 2. Document (MongoDB, CosmosDB)
- **Flexible schema**: JSON documents
- **Use case**: Content management, catalogs

### 3. Column-family (Cassandra, Bigtable)
- **Wide-column**: Billions of rows
- **Use case**: Time-series, analytics

### 4. Graph (Neo4j, Neptune)
- **Relationships**: Nodes + edges
- **Use case**: Social networks, recommendations

## AWS DynamoDB

```python
import boto3

dynamodb = boto3.resource('dynamodb')

# Create table
table = dynamodb.create_table(
    TableName='Users',
    KeySchema=[
        {'AttributeName': 'userId', 'KeyType': 'HASH'}
    ],
    AttributeDefinitions=[
        {'AttributeName': 'userId', 'AttributeType': 'S'}
    ],
    BillingMode='PAY_PER_REQUEST'
)

# Put item
table.put_item(Item={
    'userId': 'user123',
    'name': 'John Doe',
    'email': 'john@example.com'
})

# Get item
response = table.get_item(Key={'userId': 'user123'})
print(response['Item'])

# Query
response = table.query(
    KeyConditionExpression='userId = :uid',
    ExpressionAttributeValues={':uid': 'user123'}
)
```

### DynamoDB Streams
```python
# Trigger Lambda on DynamoDB changes
def lambda_handler(event, context):
    for record in event['Records']:
        if record['eventName'] == 'INSERT':
            print('New item:', record['dynamodb']['NewImage'])
```

## Azure Cosmos DB

Multi-model: Document, Key-Value, Graph, Column-family

```python
from azure.cosmos import CosmosClient

client = CosmosClient(url, key)
database = client.get_database_client('mydb')
container = database.get_container_client('users')

# Create
container.create_item({
    'id': 'user123',
    'name': 'John Doe',
    'email': 'john@example.com'
})

# Read
item = container.read_item('user123', partition_key='user123')

# Query
query = "SELECT * FROM c WHERE c.name = 'John Doe'"
items = list(container.query_items(query, enable_cross_partition_query=True))
```

## Google Cloud Firestore

```javascript
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

// Add document
await db.collection('users').doc('user123').set({
  name: 'John Doe',
  email: 'john@example.com'
});

// Get document
const doc = await db.collection('users').doc('user123').get();
console.log(doc.data());

// Query
const snapshot = await db.collection('users')
  .where('name', '==', 'John Doe')
  .get();

snapshot.forEach(doc => console.log(doc.data()));
```

## Quando usare NoSQL?

✅ **Use NoSQL quando:**
- Schema flessibile
- Horizontal scaling necessario
- High throughput (millions req/sec)
- Semi-structured data

❌ **Use SQL quando:**
- Complex queries (JOINs)
- ACID transactions critical
- Structured data
- Reporting/analytics

## Best Practices

1. **Design for access patterns**: NoSQL ≠ schema-less
2. **Partition key**: Distribute data evenly
3. **Avoid hot partitions**
4. **Use batch operations**: Reduce API calls
5. **Monitor throttling**: Set alarms
6. **Backup regularly**

## Confronto

| Feature | DynamoDB | CosmosDB | Firestore |
|---------|----------|----------|-----------|
| Latency | < 10ms | < 10ms | < 100ms |
| Scaling | Automatic | Automatic | Automatic |
| Consistency | Eventual/Strong | 5 levels | Eventual/Strong |
| Pricing | Request-based | RU-based | Document-based |
