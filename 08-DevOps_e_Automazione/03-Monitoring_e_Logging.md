# Monitoring e Logging

## Introduzione all'Observability

L'**Observability** è la capacità di comprendere lo stato interno di un sistema analizzando i suoi output esterni.

### I Tre Pilastri dell'Observability

1. **Metrics** (Metriche): Valori numerici aggregati (CPU, latenza, throughput)
2. **Logs**: Eventi discreti con timestamp
3. **Traces**: Percorso richieste attraverso microservizi

### Golden Signals (Google SRE)

- **Latency**: Tempo di risposta
- **Traffic**: Richieste al secondo  
- **Errors**: Tasso di errore
- **Saturation**: Utilizzo risorse (CPU, RAM, Disk)

---

## AWS CloudWatch

### CloudWatch Metrics

```bash
# Pubblicare metrica custom
aws cloudwatch put-metric-data \
  --namespace "MyApp" \
  --metric-name "OrdersProcessed" \
  --value 142 \
  --unit Count

# Query metriche
aws cloudwatch get-metric-statistics \
  --namespace AWS/EC2 \
  --metric-name CPUUtilization \
  --dimensions Name=InstanceId,Value=i-1234567890 \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-01T23:59:59Z \
  --period 3600 \
  --statistics Average
```

### CloudWatch Alarms

```bash
# Alarm su CPU alta
aws cloudwatch put-metric-alarm \
  --alarm-name cpu-high \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --alarm-actions arn:aws:sns:eu-west-1:123456789012:alerts
```

### CloudWatch Logs Insights

```sql
-- Errori nelle ultime 24h
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 100

-- P95 latency
fields @timestamp, @duration
| stats avg(@duration), pct(@duration, 95)

-- Richieste per endpoint
fields requestPath, statusCode
| stats count() by requestPath, statusCode
```

---

## Azure Monitor

### Azure Monitor Metrics

```bash
# Query metriche VM
az monitor metrics list \
  --resource /subscriptions/{sub}/resourceGroups/myRG/providers/Microsoft.Compute/virtualMachines/myVM \
  --metric "Percentage CPU"
```

### Azure Log Analytics (KQL)

```kql
// Errori ultimi 7 giorni
AppTraces
| where TimeGenerated > ago(7d)
| where SeverityLevel >= 3
| summarize count() by bin(TimeGenerated, 1h)
| render timechart

// Performance issues
AppRequests
| where Success == false or Duration > 3000
| project TimeGenerated, Name, Duration, ResultCode
| order by Duration desc
```

### Application Insights

```csharp
using Microsoft.ApplicationInsights;

public class OrderService
{
    private readonly TelemetryClient _telemetry;

    public async Task<Order> ProcessOrder(OrderRequest request)
    {
        var operation = _telemetry.StartOperation<RequestTelemetry>("ProcessOrder");
        
        try
        {
            _telemetry.TrackEvent("OrderStarted", new Dictionary<string, string>
            {
                { "orderId", request.Id }
            });

            var order = await _orderRepository.Create(request);
            _telemetry.TrackMetric("OrderValue", order.TotalAmount);
            
            return order;
        }
        catch (Exception ex)
        {
            _telemetry.TrackException(ex);
            throw;
        }
        finally
        {
            _telemetry.StopOperation(operation);
        }
    }
}
```

---

## Prometheus + Grafana

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
      - role: pod
```

### Prometheus Alerts

```yaml
# alerts.yml
groups:
  - name: example
    rules:
      - alert: HighCPU
        expr: node_cpu_seconds_total{mode="idle"} < 20
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU on {{ $labels.instance }}"

      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
```

### Application Instrumentation

```javascript
const promClient = require('prom-client');

// Default metrics
promClient.collectDefaultMetrics();

// Custom metrics
const httpDuration = new promClient.Histogram({
  name: 'http_request_duration_seconds',
  help: 'Duration of HTTP requests',
  labelNames: ['method', 'route', 'status_code'],
  buckets: [0.1, 0.5, 1, 2, 5]
});

const ordersCounter = new promClient.Counter({
  name: 'orders_processed_total',
  help: 'Total orders processed',
  labelNames: ['status']
});

// Middleware
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = (Date.now() - start) / 1000;
    httpDuration.labels(req.method, req.path, res.statusCode).observe(duration);
  });
  next();
});

// Metrics endpoint
app.get('/metrics', async (req, res) => {
  res.set('Content-Type', promClient.register.contentType);
  res.end(await promClient.register.metrics());
});
```

### PromQL Queries

```promql
# CPU usage
rate(node_cpu_seconds_total{mode!="idle"}[5m])

# Memory usage %
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100

# HTTP requests/sec
rate(http_requests_total[5m])

# P95 latency
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))

# Error rate
sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))
```

---

## Distributed Tracing

### OpenTelemetry

```javascript
const { NodeTracerProvider } = require('@opentelemetry/sdk-trace-node');
const { JaegerExporter } = require('@opentelemetry/exporter-jaeger');

const provider = new NodeTracerProvider();
provider.addSpanProcessor(
  new BatchSpanProcessor(
    new JaegerExporter({
      endpoint: 'http://jaeger:14268/api/traces'
    })
  )
);
provider.register();

const tracer = provider.getTracer('my-app');

app.post('/orders', async (req, res) => {
  const span = tracer.startSpan('process-order');
  
  try {
    const order = await db.orders.create(req.body);
    
    const paymentSpan = tracer.startSpan('process-payment', {
      parent: span
    });
    await processPayment(order);
    paymentSpan.end();
    
    res.json(order);
  } catch (err) {
    span.recordException(err);
    throw err;
  } finally {
    span.end();
  }
});
```

### AWS X-Ray

```javascript
const AWSXRay = require('aws-xray-sdk-core');
const AWS = AWSXRay.captureAWS(require('aws-sdk'));

app.use(AWSXRay.express.openSegment('MyApp'));

app.get('/users/:id', async (req, res) => {
  const dynamodb = new AWS.DynamoDB.DocumentClient();
  const user = await dynamodb.get({
    TableName: 'Users',
    Key: { id: req.params.id }
  }).promise();
  
  res.json({ user });
});

app.use(AWSXRay.express.closeSegment());
```

---

## SLIs, SLOs, SLAs

### Service Level Indicators (SLI)

Metriche quantificabili:
- **Availability**: % richieste con successo
- **Latency**: % richieste < 200ms
- **Error Rate**: % richieste fallite

### Service Level Objectives (SLO)

```yaml
SLOs:
  - name: "API Availability"
    target: 99.9%
    window: 30d
  
  - name: "API Latency"
    target: 95%  # 95% richieste < 200ms
    window: 7d
```

### Error Budget

```
Error Budget = 100% - SLO

SLO: 99.9% availability
Error Budget: 0.1% = 43.2 minuti/mese downtime

Budget > 0: Deploy veloce, esperimenti
Budget ≈ 0: Freeze feature, focus reliability
```

---

## Best Practices

### 1. Structured Logging

```javascript
// ✅ GOOD (JSON structured)
logger.info('user_login', {
  userId: userId,
  timestamp: new Date().toISOString(),
  ip: req.ip
});
```

### 2. Correlation IDs

```javascript
const correlationId = req.headers['x-correlation-id'] || uuid();
logger = logger.child({ correlationId });

await fetch('https://api.service2.com', {
  headers: { 'X-Correlation-ID': correlationId }
});
```

### 3. Health Checks

```javascript
app.get('/health', async (req, res) => {
  const checks = {
    database: await checkDatabase(),
    redis: await checkRedis()
  };
  
  const healthy = Object.values(checks).every(c => c.healthy);
  res.status(healthy ? 200 : 503).json({ status: healthy ? 'healthy' : 'unhealthy', checks });
});
```

### 4. Sampling per High-Volume

```javascript
// Trace 1% richieste o 100% errori
const shouldTrace = Math.random() < 0.01 || res.statusCode >= 500;
```

---

## Esercizi

1. **CloudWatch Dashboard**: Dashboard per Lambda, API Gateway, DynamoDB
2. **Prometheus + Grafana**: Setup su Kubernetes
3. **Application Insights**: Integra in app .NET
4. **Distributed Tracing**: OpenTelemetry su 3 microservizi
5. **SLO Monitoring**: Definisci SLIs/SLOs e alert
6. **Log Aggregation**: ELK stack o CloudWatch Logs Insights

---

## Domande di Verifica

1. Qual è la differenza tra monitoring e observability?
2. Cosa sono i "three pillars" dell'observability?
3. Come funziona distributed tracing?
4. Qual è la differenza tra SLI, SLO e SLA?
5. Cosa sono i Golden Signals?
6. Come usi Prometheus per Kubernetes?
7. Quando usi sampling nel tracing?
8. Come implementi correlation IDs?
9. Cos'è l'error budget in SRE?
10. Come ottimizzi i costi di logging?

---

## Risorse Aggiuntive

- [Google SRE Book](https://sre.google/sre-book/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [OpenTelemetry](https://opentelemetry.io/)
- [AWS CloudWatch](https://docs.aws.amazon.com/cloudwatch/)
- [Azure Monitor](https://learn.microsoft.com/azure/azure-monitor/)
