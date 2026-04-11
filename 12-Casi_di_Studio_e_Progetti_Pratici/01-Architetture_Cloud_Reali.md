# Architetture Cloud Reali

## Caso di Studio 1: E-commerce Platform (Netflix-style)

### Requisiti
- 10M utenti attivi
- 99.99% availability
- Global delivery
- Real-time recommendations

### Architettura AWS
```
┌────────────────────────────────────────┐
│ Route 53 (DNS)                         │
└────────────┬───────────────────────────┘
             │
┌────────────▼───────────────────────────┐
│ CloudFront (CDN)                       │
└────────────┬───────────────────────────┘
             │
┌────────────▼───────────────────────────┐
│ ALB (Load Balancer)                    │
└────┬───────────────────┬───────────────┘
     │                   │
┌────▼────────┐  ┌───────▼──────┐
│ ECS Fargate │  │ ECS Fargate  │
│ (Web API)   │  │ (Recommendations) │
└────┬────────┘  └───────┬──────┘
     │                   │
┌────▼────────┐  ┌───────▼──────┐
│ RDS Aurora  │  │ ElastiCache  │
│ (Multi-AZ)  │  │ (Redis)      │
└─────────────┘  └──────────────┘
```

### Componenti chiave
- **Frontend**: React su S3 + CloudFront
- **API**: ECS Fargate con auto-scaling
- **Database**: Aurora PostgreSQL Multi-AZ
- **Cache**: Redis ElastiCache
- **Search**: Elasticsearch
- **Queue**: SQS per async processing
- **Storage**: S3 per immagini prodotti

## Caso di Studio 2: IoT Platform

### Requisiti
- 100K dispositivi connessi
- Real-time data ingestion (1M eventi/sec)
- Anomaly detection
- Dashboard real-time

### Architettura Azure
```
┌────────────────────────────────────────┐
│ IoT Devices (sensors)                  │
└────────────┬───────────────────────────┘
             │
┌────────────▼───────────────────────────┐
│ IoT Hub (ingestion)                    │
└────────────┬───────────────────────────┘
             │
┌────────────▼───────────────────────────┐
│ Stream Analytics                       │
└────┬───────────────────┬───────────────┘
     │                   │
┌────▼────────┐  ┌───────▼──────┐
│ Cosmos DB   │  │ Event Hubs   │
│ (Hot path)  │  │ (Processing) │
└─────────────┘  └───────┬──────┘
                         │
                 ┌───────▼──────┐
                 │ Functions    │
                 │ (Alerts)     │
                 └──────────────┘
```

## Caso di Studio 3: Media Streaming

### Architettura GCP (YouTube-style)
- **Upload**: Cloud Storage
- **Transcoding**: Compute Engine + FFmpeg
- **Storage**: Multi-region buckets
- **CDN**: Cloud CDN
- **Analytics**: BigQuery
- **Recommendations**: Vertex AI

## Caso di Studio 4: Fintech App

### Requisiti speciali
- PCI-DSS compliance
- High security
- Zero downtime
- Audit trail completo

### Architettura Multi-Region
```
Region 1 (Primary)         Region 2 (DR)
┌─────────────┐           ┌─────────────┐
│ Application │◀────────▶│ Application │
│ Cluster     │           │ Cluster     │
└──────┬──────┘           └──────┬──────┘
       │                         │
┌──────▼──────┐           ┌──────▼──────┐
│ RDS Aurora  │═════════▶│ RDS Aurora  │
│ (Primary)   │           │ (Replica)   │
└─────────────┘           └─────────────┘
```

### Security Layers
1. **Network**: VPC, Private subnets, NACLs
2. **Application**: WAF, API Gateway
3. **Data**: Encryption at-rest/in-transit
4. **Access**: IAM, MFA, SSO
5. **Monitoring**: CloudTrail, GuardDuty
6. **Compliance**: Config Rules, Security Hub

## Pattern Comuni

### Microservices Architecture
```
┌─────────────────────────────────────────┐
│ API Gateway                             │
└────┬────────┬────────┬──────────────────┘
     │        │        │
┌────▼───┐ ┌──▼──┐ ┌───▼────┐
│ Users  │ │Order│ │Payment │
│Service │ │Svc  │ │Service │
└────┬───┘ └──┬──┘ └───┬────┘
     │        │        │
┌────▼────────▼────────▼──────┐
│ Event Bus (SNS/EventBridge) │
└─────────────────────────────┘
```

### CQRS + Event Sourcing
```
Write Model              Read Model
┌──────────┐            ┌──────────┐
│ Command  │            │ Query    │
│ Service  │            │ Service  │
└────┬─────┘            └────▲─────┘
     │                       │
┌────▼─────┐            ┌────┴─────┐
│ Event    │═══════════▶Projection │
│ Store    │            │ Service  │
└──────────┘            └──────────┘
```

## Best Practices Architetturali

### 1. Well-Architected Framework (AWS)
- **Operational Excellence**: IaC, monitoring
- **Security**: Defense in depth
- **Reliability**: Multi-AZ, backups
- **Performance**: Right-sizing, caching
- **Cost Optimization**: Auto-scaling, RI
- **Sustainability**: Efficient resource use

### 2. 12-Factor App
1. Codebase in version control
2. Dependencies explicitly declared
3. Config in environment
4. Backing services as attached resources
5. Strict separation build/run
6. Stateless processes
7. Port binding
8. Concurrency via processes
9. Fast startup/graceful shutdown
10. Dev/prod parity
11. Logs as event streams
12. Admin tasks as one-off processes

### 3. Design Patterns
- **Circuit Breaker**: Prevent cascade failures
- **Bulkhead**: Isolate resources
- **Retry with backoff**: Handle transient failures
- **Cache-Aside**: Optimize reads
- **CQRS**: Separate read/write models
- **Event Sourcing**: Audit trail
- **Saga**: Distributed transactions

## Migration Strategies

### 6 R's of Migration
1. **Rehost** (Lift-and-shift): VM → Cloud VM
2. **Replatform** (Lift-tinker-shift): Minor optimizations
3. **Repurchase** (Drop and shop): Move to SaaS
4. **Refactor**: Re-architect for cloud-native
5. **Retire**: Decommission
6. **Retain**: Keep on-premises

## Esercizi
1. Design architecture per social network (100M users)
2. Plan disaster recovery strategy (RTO < 1h, RPO < 5min)
3. Architect multi-tenant SaaS platform
4. Design serverless data pipeline
5. Create hybrid cloud architecture

## Domande
1. Come gestisci cross-region failover?
2. Quali pattern usi per microservices communication?
3. Come implementi API Gateway pattern?
4. Quando usi CQRS?
5. Come garantisci PCI-DSS compliance?
