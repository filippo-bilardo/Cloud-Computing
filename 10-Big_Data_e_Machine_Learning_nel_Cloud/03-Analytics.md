# Analytics nel Cloud

## Business Intelligence e Analytics

Il Cloud offre strumenti per trasformare dati in insights di business attraverso dashboards, report e analisi interattive.

---

## Amazon QuickSight

**QuickSight** è il servizio BI serverless di AWS.

### Setup e Connessione Dati

```bash
# Create analysis
aws quicksight create-data-source \
  --aws-account-id 123456789012 \
  --data-source-id my-redshift-source \
  --name "Sales Database" \
  --type REDSHIFT \
  --data-source-parameters '{
    "RedshiftParameters": {
      "Host": "mycluster.xyz.eu-west-1.redshift.amazonaws.com",
      "Port": 5439,
      "Database": "sales"
    }
  }' \
  --credentials '{
    "CredentialPair": {
      "Username": "admin",
      "Password": "MyPassword123!"
    }
  }'

# Create dataset
aws quicksight create-data-set \
  --aws-account-id 123456789012 \
  --data-set-id sales-dataset \
  --name "Sales Data" \
  --physical-table-map '{
    "sales-table": {
      "RelationalTable": {
        "DataSourceArn": "arn:aws:quicksight:...:datasource/my-redshift-source",
        "Schema": "public",
        "Name": "sales",
        "InputColumns": [
          {"Name": "sale_date", "Type": "DATETIME"},
          {"Name": "amount", "Type": "DECIMAL"},
          {"Name": "product_id", "Type": "INTEGER"},
          {"Name": "customer_id", "Type": "INTEGER"}
        ]
      }
    }
  }' \
  --import-mode SPICE
```

### Calculated Fields

```json
{
  "Name": "Revenue",
  "Expression": "quantity * price"
}

{
  "Name": "Profit Margin",
  "Expression": "(revenue - cost) / revenue * 100"
}

{
  "Name": "Quarter",
  "Expression": "quarter(sale_date)"
}
```

---

## Power BI su Azure

### Power BI Embedded

```csharp
// ASP.NET Core integration
using Microsoft.PowerBI.Api;
using Microsoft.Rest;

public class PowerBIService
{
    private readonly IConfiguration _config;

    public async Task<EmbedConfig> GetEmbedConfig()
    {
        var credential = new UserPasswordCredential(
            _config["PowerBI:Username"],
            _config["PowerBI:Password"]
        );

        var authContext = new AuthenticationContext(_config["PowerBI:AuthorityUrl"]);
        var authResult = await authContext.AcquireTokenAsync(
            _config["PowerBI:ResourceUrl"],
            _config["PowerBI:ClientId"],
            credential
        );

        var tokenCredentials = new TokenCredentials(authResult.AccessToken, "Bearer");
        var client = new PowerBIClient(new Uri(_config["PowerBI:ApiUrl"]), tokenCredentials);

        // Get report
        var report = await client.Reports.GetReportInGroupAsync(
            Guid.Parse(_config["PowerBI:WorkspaceId"]),
            Guid.Parse(_config["PowerBI:ReportId"])
        );

        // Generate embed token
        var generateTokenRequest = new GenerateTokenRequest(
            accessLevel: "View"
        );

        var embedToken = await client.Reports.GenerateTokenInGroupAsync(
            Guid.Parse(_config["PowerBI:WorkspaceId"]),
            Guid.Parse(_config["PowerBI:ReportId"]),
            generateTokenRequest
        );

        return new EmbedConfig
        {
            EmbedUrl = report.EmbedUrl,
            EmbedToken = embedToken.Token,
            ReportId = report.Id.ToString()
        };
    }
}
```

### DAX Measures

```dax
-- Total Sales
Total Sales = SUM(Sales[Amount])

-- Sales YTD (Year-to-Date)
Sales YTD = TOTALYTD([Total Sales], Calendar[Date])

-- Sales vs Previous Year
Sales vs PY = 
VAR CurrentSales = [Total Sales]
VAR PreviousYearSales = 
    CALCULATE(
        [Total Sales],
        SAMEPERIODLASTYEAR(Calendar[Date])
    )
RETURN
    DIVIDE(CurrentSales - PreviousYearSales, PreviousYearSales)

-- Top 10 Products
Top 10 Products = 
CALCULATE(
    [Total Sales],
    TOPN(10, ALL(Products[ProductName]), [Total Sales], DESC)
)

-- Moving Average (3 months)
Moving Avg 3M = 
AVERAGEX(
    DATESINPERIOD(
        Calendar[Date],
        LASTDATE(Calendar[Date]),
        -3,
        MONTH
    ),
    [Total Sales]
)
```

---

## Google Data Studio (Looker Studio)

### Connessione BigQuery

```javascript
// Custom connector (Apps Script)
var connector = DataStudioApp.createCommunityConnector();

function getConfig() {
  var config = connector.getConfig();
  
  config.newInfo()
    .setId('instructions')
    .setText('Enter your BigQuery project details');
  
  config.newTextInput()
    .setId('projectId')
    .setName('Project ID')
    .setPlaceholder('my-project-id');
  
  config.newTextInput()
    .setId('datasetId')
    .setName('Dataset ID')
    .setPlaceholder('my-dataset');
  
  return config.build();
}

function getSchema(request) {
  return {
    schema: [
      { name: 'date', label: 'Date', dataType: 'STRING' },
      { name: 'revenue', label: 'Revenue', dataType: 'NUMBER' },
      { name: 'orders', label: 'Orders', dataType: 'NUMBER' }
    ]
  };
}

function getData(request) {
  var projectId = request.configParams.projectId;
  var datasetId = request.configParams.datasetId;
  
  var query = `
    SELECT 
      FORMAT_DATE('%Y-%m-%d', sale_date) as date,
      SUM(amount) as revenue,
      COUNT(*) as orders
    FROM \`${projectId}.${datasetId}.sales\`
    GROUP BY date
    ORDER BY date
  `;
  
  var queryResults = BigQuery.Jobs.query({query: query}, projectId);
  
  var data = [];
  queryResults.rows.forEach(function(row) {
    data.push({
      values: [row.f[0].v, row.f[1].v, row.f[2].v]
    });
  });
  
  return {
    schema: getSchema().schema,
    rows: data
  };
}
```

---

## Amazon Athena

**Athena** permette query SQL su dati S3 senza infrastructure.

### Setup e Query

```sql
-- Create database
CREATE DATABASE sales_analytics;

-- Create external table (Parquet)
CREATE EXTERNAL TABLE sales_analytics.transactions (
  transaction_id STRING,
  customer_id STRING,
  product_id STRING,
  amount DECIMAL(10,2),
  quantity INT,
  transaction_date DATE
)
PARTITIONED BY (year INT, month INT)
STORED AS PARQUET
LOCATION 's3://my-datalake/sales/';

-- Add partitions
ALTER TABLE sales_analytics.transactions 
ADD PARTITION (year=2024, month=1) 
LOCATION 's3://my-datalake/sales/year=2024/month=01/';

-- Query
SELECT 
  year,
  month,
  COUNT(*) as transaction_count,
  SUM(amount) as total_revenue,
  AVG(amount) as avg_transaction
FROM sales_analytics.transactions
WHERE year = 2024
GROUP BY year, month
ORDER BY month;

-- Window functions
SELECT 
  transaction_date,
  customer_id,
  amount,
  SUM(amount) OVER (
    PARTITION BY customer_id 
    ORDER BY transaction_date
    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
  ) as cumulative_spending
FROM sales_analytics.transactions
WHERE year = 2024;

-- CTEs (Common Table Expressions)
WITH monthly_revenue AS (
  SELECT 
    year,
    month,
    SUM(amount) as revenue
  FROM sales_analytics.transactions
  GROUP BY year, month
),
prev_month AS (
  SELECT 
    year,
    month,
    revenue,
    LAG(revenue) OVER (ORDER BY year, month) as prev_revenue
  FROM monthly_revenue
)
SELECT 
  year,
  month,
  revenue,
  prev_revenue,
  (revenue - prev_revenue) / prev_revenue * 100 as growth_pct
FROM prev_month
WHERE prev_revenue IS NOT NULL;
```

### Athena con Python (boto3)

```python
import boto3
import time

athena = boto3.client('athena')

def run_query(query, database, output_location):
    # Start query execution
    response = athena.start_query_execution(
        QueryString=query,
        QueryExecutionContext={'Database': database},
        ResultConfiguration={'OutputLocation': output_location}
    )
    
    query_execution_id = response['QueryExecutionId']
    
    # Wait for completion
    while True:
        result = athena.get_query_execution(QueryExecutionId=query_execution_id)
        status = result['QueryExecution']['Status']['State']
        
        if status in ['SUCCEEDED', 'FAILED', 'CANCELLED']:
            break
        
        time.sleep(1)
    
    if status == 'SUCCEEDED':
        # Get results
        results = athena.get_query_results(QueryExecutionId=query_execution_id)
        return results
    else:
        raise Exception(f"Query failed with status: {status}")

# Usage
query = """
SELECT 
  product_id,
  SUM(amount) as total_sales
FROM transactions
WHERE year = 2024
GROUP BY product_id
ORDER BY total_sales DESC
LIMIT 10
"""

results = run_query(
    query=query,
    database='sales_analytics',
    output_location='s3://my-bucket/athena-results/'
)

# Parse results
for row in results['ResultSet']['Rows'][1:]:  # Skip header
    product_id = row['Data'][0]['VarCharValue']
    total_sales = row['Data'][1]['VarCharValue']
    print(f"{product_id}: ${total_sales}")
```

---

## Elasticsearch e Kibana

### Elasticsearch Analytics

```python
from elasticsearch import Elasticsearch

es = Elasticsearch(['https://my-cluster.es.amazonaws.com'])

# Aggregations
response = es.search(index='sales', body={
    "size": 0,
    "aggs": {
        "sales_by_month": {
            "date_histogram": {
                "field": "sale_date",
                "calendar_interval": "month"
            },
            "aggs": {
                "total_revenue": {
                    "sum": {
                        "field": "amount"
                    }
                },
                "avg_order_value": {
                    "avg": {
                        "field": "amount"
                    }
                }
            }
        },
        "top_products": {
            "terms": {
                "field": "product_name.keyword",
                "size": 10,
                "order": {
                    "total_sales": "desc"
                }
            },
            "aggs": {
                "total_sales": {
                    "sum": {
                        "field": "amount"
                    }
                }
            }
        },
        "customer_segments": {
            "range": {
                "field": "total_spent",
                "ranges": [
                    {"to": 100, "key": "low"},
                    {"from": 100, "to": 1000, "key": "medium"},
                    {"from": 1000, "key": "high"}
                ]
            }
        }
    }
})

# Parse results
for bucket in response['aggregations']['sales_by_month']['buckets']:
    month = bucket['key_as_string']
    revenue = bucket['total_revenue']['value']
    avg_value = bucket['avg_order_value']['value']
    print(f"{month}: Revenue=${revenue:.2f}, Avg=${avg_value:.2f}")
```

### Kibana Visualizations (JSON)

```json
{
  "title": "Sales Dashboard",
  "type": "dashboard",
  "panels": [
    {
      "type": "visualization",
      "title": "Revenue Over Time",
      "visState": {
        "type": "line",
        "params": {
          "type": "line",
          "grid": { "categoryLines": true },
          "categoryAxes": [{
            "id": "CategoryAxis-1",
            "type": "category",
            "position": "bottom",
            "title": { "text": "Date" }
          }],
          "valueAxes": [{
            "id": "ValueAxis-1",
            "type": "value",
            "position": "left",
            "title": { "text": "Revenue" }
          }]
        },
        "aggs": [
          {
            "id": "1",
            "type": "sum",
            "schema": "metric",
            "params": { "field": "amount" }
          },
          {
            "id": "2",
            "type": "date_histogram",
            "schema": "segment",
            "params": {
              "field": "sale_date",
              "interval": "day"
            }
          }
        ]
      }
    },
    {
      "type": "visualization",
      "title": "Top Products",
      "visState": {
        "type": "pie",
        "aggs": [
          {
            "type": "sum",
            "schema": "metric",
            "params": { "field": "amount" }
          },
          {
            "type": "terms",
            "schema": "segment",
            "params": {
              "field": "product_name.keyword",
              "size": 10,
              "order": "desc",
              "orderBy": "1"
            }
          }
        ]
      }
    }
  ]
}
```

---

## Real-Time Analytics

### AWS Kinesis Analytics

```sql
-- Create Kinesis Analytics application
CREATE OR REPLACE STREAM "DESTINATION_STREAM" (
  product_id VARCHAR(64),
  sales_count BIGINT,
  total_amount DOUBLE,
  avg_amount DOUBLE
);

-- Aggregate streaming data (5-minute tumbling window)
CREATE OR REPLACE PUMP "STREAM_PUMP" AS 
INSERT INTO "DESTINATION_STREAM"
SELECT STREAM 
  product_id,
  COUNT(*) as sales_count,
  SUM(amount) as total_amount,
  AVG(amount) as avg_amount
FROM "SOURCE_STREAM"
GROUP BY 
  product_id,
  FLOOR("SOURCE_STREAM".ROWTIME TO MINUTE / 5);

-- Anomaly detection
CREATE OR REPLACE STREAM "ANOMALY_STREAM" (
  product_id VARCHAR(64),
  amount DOUBLE,
  anomaly_score DOUBLE
);

CREATE OR REPLACE PUMP "ANOMALY_PUMP" AS
INSERT INTO "ANOMALY_STREAM"
SELECT STREAM 
  product_id,
  amount,
  ANOMALY_SCORE
FROM TABLE(
  RANDOM_CUT_FOREST(
    CURSOR(SELECT STREAM * FROM "SOURCE_STREAM"),
    100,  -- Number of trees
    256,  -- Subsample size
    100000,  -- Time decay
    1,  -- Shingle size
    false  -- Verbose
  )
);
```

### Apache Flink

```python
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment, DataTypes
from pyflink.table.descriptors import Schema, Kafka, Json

env = StreamExecutionEnvironment.get_execution_environment()
t_env = StreamTableEnvironment.create(env)

# Source table (Kafka)
t_env.execute_sql("""
    CREATE TABLE sales_stream (
        transaction_id STRING,
        product_id STRING,
        amount DECIMAL(10, 2),
        event_time TIMESTAMP(3),
        WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
    ) WITH (
        'connector' = 'kafka',
        'topic' = 'sales',
        'properties.bootstrap.servers' = 'localhost:9092',
        'properties.group.id' = 'analytics',
        'format' = 'json'
    )
""")

# Windowed aggregation
result = t_env.sql_query("""
    SELECT 
        product_id,
        TUMBLE_END(event_time, INTERVAL '5' MINUTE) as window_end,
        COUNT(*) as sales_count,
        SUM(amount) as total_revenue
    FROM sales_stream
    GROUP BY 
        product_id,
        TUMBLE(event_time, INTERVAL '5' MINUTE)
""")

# Sink table (Elasticsearch)
t_env.execute_sql("""
    CREATE TABLE sales_analytics (
        product_id STRING,
        window_end TIMESTAMP(3),
        sales_count BIGINT,
        total_revenue DECIMAL(10, 2)
    ) WITH (
        'connector' = 'elasticsearch-7',
        'hosts' = 'http://localhost:9200',
        'index' = 'sales-analytics'
    )
""")

# Execute
result.execute_insert('sales_analytics')
```

---

## Best Practices

### 1. Partitioning e Bucketing

```sql
-- BigQuery partitioned table
CREATE TABLE sales.transactions
PARTITION BY DATE(transaction_date)
CLUSTER BY customer_id, product_id
AS SELECT * FROM sales.raw_transactions;

-- Athena partitioned table
CREATE EXTERNAL TABLE sales (...)
PARTITIONED BY (year INT, month INT, day INT)
STORED AS PARQUET;
```

### 2. Materialized Views

```sql
-- Redshift materialized view
CREATE MATERIALIZED VIEW daily_sales_summary AS
SELECT 
  DATE_TRUNC('day', sale_date) as day,
  product_category,
  SUM(amount) as total_revenue,
  COUNT(*) as order_count
FROM sales
GROUP BY 1, 2;

REFRESH MATERIALIZED VIEW daily_sales_summary;
```

### 3. Query Optimization

```sql
-- ❌ BAD - Full table scan
SELECT * FROM sales WHERE YEAR(sale_date) = 2024;

-- ✅ GOOD - Partition pruning
SELECT * FROM sales WHERE sale_date >= '2024-01-01' AND sale_date < '2025-01-01';
```

### 4. Caching

```python
# QuickSight SPICE (in-memory)
aws quicksight create-data-set \
  --import-mode SPICE  # vs DIRECT_QUERY

# BigQuery BI Engine
bq query --use_cache --use_legacy_sql=false 'SELECT ...'
```

---

## Esercizi

1. **Dashboard Sales**: QuickSight/Power BI con dati Redshift/BigQuery
2. **Real-time Analytics**: Kinesis Analytics per streaming metrics
3. **Log Analysis**: Elasticsearch + Kibana per application logs
4. **Athena Queries**: Analisi dati S3 con partitioning ottimizzato
5. **Custom Metrics**: Flink job per complex event processing
6. **Predictive Analytics**: Integra ML model in dashboard

---

## Domande di Verifica

1. Qual è la differenza tra OLTP e OLAP?
2. Come funziona SPICE in QuickSight?
3. Quando usi Athena invece di Redshift?
4. Cosa sono le materialized views?
5. Come ottimizzi query su big data?
6. Qual è la differenza tra batch e streaming analytics?
7. Come implementi real-time dashboards?
8. Cosa sono aggregations in Elasticsearch?
9. Come gestisci partitioning in BigQuery?
10. Quali sono le best practices per dashboard performance?

---

## Risorse Aggiuntive

- [Amazon QuickSight](https://docs.aws.amazon.com/quicksight/)
- [Power BI Documentation](https://learn.microsoft.com/power-bi/)
- [Looker Studio](https://support.google.com/looker-studio)
- [Athena User Guide](https://docs.aws.amazon.com/athena/)
- [Elasticsearch Guide](https://www.elastic.co/guide/)
- [Apache Flink](https://flink.apache.org/documentation.html)
