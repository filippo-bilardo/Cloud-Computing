# Big Data nel Cloud

## Introduzione al Big Data

**Big Data** si riferisce a dataset così grandi e complessi da richiedere tecnologie specializzate per storage, processing e analisi.

### Le 5 V del Big Data

```
┌───────────────────────────────────────────────┐
│               Big Data: 5 V                   │
├──────────┬──────────┬──────────┬──────────────┤
│ Volume   │ Velocity │ Variety  │ Veracity     │
│          │          │          │ Value        │
├──────────┼──────────┼──────────┼──────────────┤
│ Petabyte │ Real-    │ Struct.  │ Qualità      │
│ Exabyte  │ time     │ Semi-    │ Affidabilità │
│          │ Batch    │ Unstruct │ Business Val │
└──────────┴──────────┴──────────┴──────────────┘
```

1. **Volume**: Terabytes, Petabytes, Exabytes
2. **Velocity**: Data in arrivo ad alta velocità (streaming)
3. **Variety**: Strutturati, semi-strutturati, non strutturati
4. **Veracity**: Qualità e affidabilità dei dati
5. **Value**: Valore di business estraibile

---

## Data Lakes

**Data Lake** è un repository centralizzato per dati strutturati e non strutturati in formato nativo.

### Data Lake vs Data Warehouse

| **Data Lake** | **Data Warehouse** |
|---------------|-------------------|
| Schema-on-read | Schema-on-write |
| Dati raw | Dati processati |
| Tutti i formati | Strutturati |
| ELT | ETL |
| Economico | Più costoso |
| Data scientists | Business analysts |

### AWS Data Lake (S3)

```bash
# Struttura tipica Data Lake
s3://my-datalake/
├── raw/                  # Dati grezzi
│   ├── logs/
│   │   └── 2024/01/15/
│   ├── events/
│   └── transactions/
├── processed/            # Dati processati
│   ├── cleaned/
│   ├── enriched/
│   └── aggregated/
└── curated/             # Dati pronti per analisi
    ├── reports/
    └── ml-datasets/

# Upload dati
aws s3 cp data.csv s3://my-datalake/raw/transactions/2024/01/15/

# Sync directory
aws s3 sync ./local-data/ s3://my-datalake/raw/logs/

# Lifecycle policy (Terraform)
resource "aws_s3_bucket_lifecycle_configuration" "datalake" {
  bucket = aws_s3_bucket.datalake.id

  rule {
    id     = "transition-to-glacier"
    status = "Enabled"

    transition {
      days          = 90
      storage_class = "GLACIER"
    }

    transition {
      days          = 180
      storage_class = "DEEP_ARCHIVE"
    }
  }
}
```

### Azure Data Lake Storage (ADLS Gen2)

```bash
# Create storage account con hierarchical namespace
az storage account create \
  --name mydatalake \
  --resource-group myRG \
  --location westeurope \
  --sku Standard_LRS \
  --kind StorageV2 \
  --hierarchical-namespace true

# Create filesystem
az storage fs create \
  --name raw-data \
  --account-name mydatalake

# Upload file
az storage fs file upload \
  --source ./data.csv \
  --path transactions/2024/01/15/data.csv \
  --file-system raw-data \
  --account-name mydatalake

# ACLs per directory
az storage fs access set \
  --acl "user::rwx,group::r-x,other::---" \
  --path transactions/ \
  --file-system raw-data \
  --account-name mydatalake
```

---

## Data Warehousing

### Amazon Redshift

**Redshift** è il data warehouse colonnare di AWS, ottimizzato per analisi OLAP.

```sql
-- Create cluster (Terraform)
resource "aws_redshift_cluster" "analytics" {
  cluster_identifier = "analytics-cluster"
  database_name      = "analytics"
  master_username    = "admin"
  master_password    = var.db_password
  node_type          = "dc2.large"
  number_of_nodes    = 2
  
  cluster_subnet_group_name = aws_redshift_subnet_group.main.name
  vpc_security_group_ids    = [aws_security_group.redshift.id]
  
  skip_final_snapshot = true
}

-- Create tables
CREATE TABLE sales (
  sale_id BIGINT IDENTITY(1,1),
  product_id INT,
  customer_id INT,
  sale_date DATE,
  amount DECIMAL(10,2),
  quantity INT
)
DISTKEY(customer_id)
SORTKEY(sale_date);

-- Load from S3
COPY sales
FROM 's3://my-bucket/sales/'
IAM_ROLE 'arn:aws:iam::123456789012:role/RedshiftRole'
FORMAT AS CSV
DELIMITER ','
IGNOREHEADER 1;

-- Query
SELECT 
  DATE_TRUNC('month', sale_date) AS month,
  SUM(amount) AS total_revenue,
  COUNT(DISTINCT customer_id) AS unique_customers
FROM sales
WHERE sale_date >= '2024-01-01'
GROUP BY 1
ORDER BY 1;

-- Unload to S3 (export results)
UNLOAD ('SELECT * FROM sales WHERE sale_date >= CURRENT_DATE - 30')
TO 's3://my-bucket/exports/recent-sales-'
IAM_ROLE 'arn:aws:iam::123456789012:role/RedshiftRole'
PARALLEL OFF
FORMAT AS PARQUET;
```

### Google BigQuery

**BigQuery** è il data warehouse serverless di Google con SQL standard.

```sql
-- Create dataset
bq mk --dataset --location=EU my_dataset

-- Create table from GCS
bq load \
  --source_format=CSV \
  --skip_leading_rows=1 \
  my_dataset.sales \
  gs://my-bucket/sales.csv \
  sale_id:INTEGER,product_id:INTEGER,amount:FLOAT,sale_date:DATE

-- Query con partitioning
CREATE TABLE my_dataset.sales_partitioned
PARTITION BY DATE(sale_date)
AS
SELECT * FROM my_dataset.sales;

-- Query ottimizzata
SELECT 
  FORMAT_DATE('%Y-%m', sale_date) AS month,
  SUM(amount) AS total_revenue,
  COUNT(DISTINCT customer_id) AS customers
FROM my_dataset.sales_partitioned
WHERE sale_date BETWEEN '2024-01-01' AND '2024-12-31'
GROUP BY 1
ORDER BY 1;

-- Clustering (per query performance)
CREATE TABLE my_dataset.sales_clustered
PARTITION BY DATE(sale_date)
CLUSTER BY customer_id, product_id
AS SELECT * FROM my_dataset.sales;

-- Scheduled queries
CREATE OR REPLACE TABLE my_dataset.daily_summary
OPTIONS(
  expiration_timestamp=TIMESTAMP_ADD(CURRENT_TIMESTAMP(), INTERVAL 90 DAY)
)
AS
SELECT 
  CURRENT_DATE() AS report_date,
  SUM(amount) AS total_sales
FROM my_dataset.sales
WHERE sale_date = CURRENT_DATE();
```

### Azure Synapse Analytics

```sql
-- Create dedicated SQL pool
CREATE MASTER KEY ENCRYPTION BY PASSWORD = 'StrongPassword!';

CREATE DATABASE SCOPED CREDENTIAL AzureStorageCredential
WITH IDENTITY = 'SHARED ACCESS SIGNATURE',
SECRET = 'sv=2019-12-12&ss=b...';

CREATE EXTERNAL DATA SOURCE MyDataLake
WITH (
  TYPE = HADOOP,
  LOCATION = 'wasbs://data@mystorageaccount.blob.core.windows.net',
  CREDENTIAL = AzureStorageCredential
);

CREATE EXTERNAL FILE FORMAT ParquetFormat
WITH (
  FORMAT_TYPE = PARQUET,
  DATA_COMPRESSION = 'org.apache.hadoop.io.compress.SnappyCodec'
);

-- External table
CREATE EXTERNAL TABLE ext_sales (
  sale_id INT,
  amount DECIMAL(10,2),
  sale_date DATE
)
WITH (
  LOCATION = '/sales/',
  DATA_SOURCE = MyDataLake,
  FILE_FORMAT = ParquetFormat
);

-- Query
SELECT 
  DATEPART(month, sale_date) AS month,
  SUM(amount) AS revenue
FROM ext_sales
WHERE YEAR(sale_date) = 2024
GROUP BY DATEPART(month, sale_date);
```

---

## Batch Processing

### AWS EMR (Elastic MapReduce)

**EMR** gestisce cluster Hadoop/Spark per processing batch.

```python
# PySpark su EMR
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, sum, count, avg

# Initialize Spark
spark = SparkSession.builder \
    .appName("Sales Analysis") \
    .getOrCreate()

# Read from S3
df = spark.read.parquet("s3://my-bucket/sales/")

# Transformations
monthly_sales = df \
    .filter(col("sale_date") >= "2024-01-01") \
    .groupBy("product_category") \
    .agg(
        sum("amount").alias("total_revenue"),
        count("sale_id").alias("num_sales"),
        avg("amount").alias("avg_sale")
    ) \
    .orderBy(col("total_revenue").desc())

# Write results
monthly_sales.write \
    .mode("overwrite") \
    .parquet("s3://my-bucket/results/monthly-sales/")

# Stop
spark.stop()
```

```bash
# Launch EMR cluster
aws emr create-cluster \
  --name "Sales Processing" \
  --release-label emr-6.10.0 \
  --applications Name=Spark Name=Hadoop \
  --ec2-attributes KeyName=mykey \
  --instance-type m5.xlarge \
  --instance-count 3 \
  --use-default-roles \
  --steps Type=Spark,Name="Sales Analysis",ActionOnFailure=CONTINUE,Args=[--deploy-mode,cluster,--master,yarn,s3://my-bucket/scripts/analysis.py] \
  --auto-terminate

# Submit step to existing cluster
aws emr add-steps \
  --cluster-id j-XXXXXXXXXXXXX \
  --steps Type=Spark,Name="Daily Report",Args=[s3://my-bucket/scripts/daily.py]
```

### Azure Databricks

```python
# Databricks notebook
from pyspark.sql.functions import *

# Read from ADLS
df = spark.read.format("delta") \
    .load("abfss://data@mystorageaccount.dfs.core.windows.net/sales")

# Delta Lake optimizations
df.write.format("delta") \
    .mode("overwrite") \
    .partitionBy("sale_date") \
    .save("/delta/sales")

# Optimize
spark.sql("OPTIMIZE delta.`/delta/sales`")
spark.sql("VACUUM delta.`/delta/sales` RETAIN 168 HOURS")

# Z-ordering (clustering)
spark.sql("OPTIMIZE delta.`/delta/sales` ZORDER BY (customer_id)")

# Time travel
df_yesterday = spark.read.format("delta") \
    .option("versionAsOf", 1) \
    .load("/delta/sales")
```

---

## Stream Processing

### AWS Kinesis

```python
# Producer: Send data to Kinesis
import boto3
import json
from datetime import datetime

kinesis = boto3.client('kinesis', region_name='eu-west-1')

def send_event(event_data):
    kinesis.put_record(
        StreamName='user-events',
        Data=json.dumps(event_data),
        PartitionKey=event_data['user_id']
    )

# Send event
send_event({
    'user_id': 'user123',
    'event_type': 'page_view',
    'page': '/products/123',
    'timestamp': datetime.utcnow().isoformat()
})

# Consumer: Lambda function
import base64

def lambda_handler(event, context):
    for record in event['Records']:
        # Decode data
        payload = base64.b64decode(record['kinesis']['data'])
        event_data = json.loads(payload)
        
        print(f"Processing event: {event_data}")
        
        # Process event (e.g., update DynamoDB, send to analytics)
        process_user_event(event_data)
    
    return {'statusCode': 200}
```

### Apache Kafka on AWS (MSK)

```python
# Kafka producer
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=['b-1.mycluster.kafka.eu-west-1.amazonaws.com:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Send message
producer.send('events', {
    'user_id': 'user123',
    'event': 'purchase',
    'amount': 99.99
})

producer.flush()

# Kafka consumer
from kafka import KafkaConsumer

consumer = KafkaConsumer(
    'events',
    bootstrap_servers=['b-1.mycluster.kafka.eu-west-1.amazonaws.com:9092'],
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    auto_offset_reset='earliest',
    group_id='my-group'
)

for message in consumer:
    event = message.value
    print(f"Received: {event}")
    process_event(event)
```

### Azure Event Hubs + Stream Analytics

```sql
-- Stream Analytics query
SELECT
    UserId,
    COUNT(*) AS EventCount,
    System.Timestamp() AS WindowEnd
INTO
    [OutputBlob]
FROM
    [InputEventHub]
TIMESTAMP BY EventTimestamp
GROUP BY
    UserId,
    TumblingWindow(minute, 5)
HAVING
    COUNT(*) > 100;  -- Alert on high activity
```

---

## Data Processing Patterns

### Lambda Architecture

```
┌─────────────────────────────────────────┐
│          Data Sources                    │
└────────────┬────────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
┌─────▼─────┐  ┌───▼──────┐
│  Batch    │  │ Stream   │
│  Layer    │  │ Layer    │
│ (Hadoop)  │  │(Kinesis) │
└─────┬─────┘  └────┬─────┘
      │             │
      │        ┌────▼─────┐
      │        │ Speed    │
      │        │ View     │
      │        │(DynamoDB)│
      │        └────┬─────┘
┌─────▼─────┐       │
│  Batch    │       │
│  View     │       │
│(Redshift) │       │
└─────┬─────┘       │
      │             │
      └──────┬──────┘
             │
      ┌──────▼──────┐
      │  Serving    │
      │  Layer      │
      │  (Query)    │
      └─────────────┘
```

### Kappa Architecture (Stream-only)

```
┌─────────────────────────────────────────┐
│          Data Sources                    │
└────────────┬────────────────────────────┘
             │
      ┌──────▼──────┐
      │   Kafka     │
      │  (Stream)   │
      └──────┬──────┘
             │
      ┌──────▼──────┐
      │  Stream     │
      │  Processing │
      │  (Flink)    │
      └──────┬──────┘
             │
      ┌──────▼──────┐
      │  Serving    │
      │  Layer      │
      └─────────────┘
```

---

## Apache Spark

### Spark DataFrame API

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.window import Window

spark = SparkSession.builder.appName("Analytics").getOrCreate()

# Read data
df = spark.read.parquet("s3://bucket/data/")

# Transformations
result = df \
    .filter(col("status") == "completed") \
    .withColumn("month", date_format("order_date", "yyyy-MM")) \
    .groupBy("month", "product_category") \
    .agg(
        sum("amount").alias("revenue"),
        count("order_id").alias("orders"),
        countDistinct("customer_id").alias("customers")
    )

# Window functions
window_spec = Window.partitionBy("customer_id").orderBy(col("order_date").desc())

customer_analysis = df \
    .withColumn("order_rank", row_number().over(window_spec)) \
    .withColumn("days_since_last_order", 
                datediff(current_date(), col("order_date")))

# Write
result.write \
    .mode("overwrite") \
    .partitionBy("month") \
    .parquet("s3://bucket/results/")
```

### Spark Streaming

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import *

spark = SparkSession.builder \
    .appName("StreamProcessing") \
    .getOrCreate()

# Read stream from Kafka
df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "localhost:9092") \
    .option("subscribe", "events") \
    .load()

# Parse JSON
events = df.selectExpr("CAST(value AS STRING)") \
    .select(from_json(col("value"), schema).alias("data")) \
    .select("data.*")

# Aggregations
windowed_counts = events \
    .withWatermark("timestamp", "10 minutes") \
    .groupBy(
        window("timestamp", "5 minutes", "1 minute"),
        "event_type"
    ) \
    .count()

# Write stream
query = windowed_counts.writeStream \
    .outputMode("append") \
    .format("parquet") \
    .option("path", "s3://bucket/streaming-results/") \
    .option("checkpointLocation", "s3://bucket/checkpoints/") \
    .start()

query.awaitTermination()
```

---

## Best Practices

### 1. Partitioning

```python
# ✅ GOOD - Partitioned by date
df.write.partitionBy("year", "month", "day").parquet("s3://bucket/data/")

# Query efficiente
spark.read.parquet("s3://bucket/data/year=2024/month=01/")
```

### 2. Compression

```python
# Parquet con Snappy compression
df.write \
    .option("compression", "snappy") \
    .parquet("s3://bucket/data/")

# Gzip per long-term storage
df.write \
    .option("compression", "gzip") \
    .parquet("s3://bucket/archive/")
```

### 3. Caching

```python
# Cache dataset usato frequentemente
df_cached = df.filter(col("status") == "active").cache()

# Multiple operations on cached data
result1 = df_cached.groupBy("category").count()
result2 = df_cached.groupBy("region").agg(sum("amount"))
```

### 4. Schema Evolution

```python
# Merge schema (nuove colonne)
df.write \
    .mode("append") \
    .option("mergeSchema", "true") \
    .parquet("s3://bucket/data/")
```

---

## Esercizi

1. **Data Lake Setup**: S3 + Glue Catalog + Athena
2. **ETL Pipeline**: EMR Spark job per transformare dati
3. **Real-time Analytics**: Kinesis → Lambda → DynamoDB
4. **Data Warehouse**: Redshift con fact/dimension tables
5. **Streaming**: Kafka → Spark Streaming → S3
6. **BigQuery**: Analisi web analytics con partitioning

---

## Domande di Verifica

1. Qual è la differenza tra Data Lake e Data Warehouse?
2. Cosa sono le 5 V del Big Data?
3. Come funziona il partitioning in BigQuery?
4. Qual è la differenza tra batch e stream processing?
5. Cosa sono Lambda e Kappa architecture?
6. Come ottimizzi query su Redshift?
7. Quando usi Parquet invece di CSV?
8. Come gestisci schema evolution?
9. Qual è la differenza tra EMR e Databricks?
10. Come implementi real-time analytics?

---

## Risorse Aggiuntive

- [AWS Big Data Blog](https://aws.amazon.com/blogs/big-data/)
- [Google BigQuery Documentation](https://cloud.google.com/bigquery/docs)
- [Apache Spark Documentation](https://spark.apache.org/docs/latest/)
- [Databricks Academy](https://www.databricks.com/learn/training)
- [Data Engineering on AWS](https://aws.amazon.com/big-data/datalakes-and-analytics/)
