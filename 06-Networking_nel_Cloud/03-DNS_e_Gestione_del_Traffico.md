# DNS e Gestione del Traffico

## Route 53 (AWS)

### Record Types
- **A**: IPv4 address
- **AAAA**: IPv6 address
- **CNAME**: Canonical name (alias)
- **MX**: Mail exchange
- **TXT**: Text records
- **Alias**: AWS-specific (no charge)

### Create Record
```bash
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456 \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "AliasTarget": {
          "HostedZoneId": "Z789012",
          "DNSName": "my-alb-123.eu-west-1.elb.amazonaws.com",
          "EvaluateTargetHealth": true
        }
      }
    }]
  }'
```

## Routing Policies

### 1. Simple Routing
```
www.example.com → 192.0.2.1
```

### 2. Weighted Routing
```
www.example.com → 70% → Server A
                → 30% → Server B
```

```bash
# Server A (70%)
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123 \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "Server-A",
        "Weight": 70,
        "TTL": 60,
        "ResourceRecords": [{"Value": "192.0.2.1"}]
      }
    }]
  }'
```

### 3. Latency-Based Routing
Indirizza al server con latency più bassa

```bash
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123 \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "EU-West",
        "Region": "eu-west-1",
        "TTL": 60,
        "ResourceRecords": [{"Value": "192.0.2.1"}]
      }
    }]
  }'
```

### 4. Geolocation Routing
Indirizza based su location

```bash
# Europe → EU server
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123 \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "Europe",
        "GeoLocation": {"ContinentCode": "EU"},
        "TTL": 60,
        "ResourceRecords": [{"Value": "192.0.2.1"}]
      }
    }]
  }'
```

### 5. Failover Routing
```
Primary (healthy) → 192.0.2.1
Secondary (failover) → 192.0.2.2
```

```bash
# Primary
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123 \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "Primary",
        "Failover": "PRIMARY",
        "HealthCheckId": "hc-123",
        "TTL": 60,
        "ResourceRecords": [{"Value": "192.0.2.1"}]
      }
    }]
  }'
```

## Health Checks

```bash
# Create health check
aws route53 create-health-check \
  --health-check-config '{
    "Type": "HTTP",
    "ResourcePath": "/health",
    "FullyQualifiedDomainName": "www.example.com",
    "Port": 80,
    "RequestInterval": 30,
    "FailureThreshold": 3
  }'

# CloudWatch alarm on health check
aws cloudwatch put-metric-alarm \
  --alarm-name website-down \
  --alarm-description "Website unhealthy" \
  --metric-name HealthCheckStatus \
  --namespace AWS/Route53 \
  --statistic Minimum \
  --period 60 \
  --threshold 1 \
  --comparison-operator LessThanThreshold \
  --evaluation-periods 1 \
  --dimensions Name=HealthCheckId,Value=hc-123 \
  --alarm-actions arn:aws:sns:...:alerts
```

## Traffic Flow

Visual editor per complex routing

```yaml
# Traffic policy
Name: geo-weighted-policy
Version: 1
Type: A

Rules:
  - Geolocation: US
    Weighted:
      - Weight: 70, Endpoint: us-east-alb
      - Weight: 30, Endpoint: us-west-alb
  
  - Geolocation: EU
    Failover:
      - Primary: eu-primary-alb, HealthCheck: hc-eu-1
      - Secondary: eu-secondary-alb
  
  - Default:
    Endpoint: global-alb
```

## Best Practices
1. Use Alias records (free queries)
2. Low TTL during migrations
3. Health checks for failover
4. Geo-routing per compliance
5. Weighted routing per blue-green deployments
6. Monitor query metrics

---

✅ **Tutti i 13 moduli completati!**
