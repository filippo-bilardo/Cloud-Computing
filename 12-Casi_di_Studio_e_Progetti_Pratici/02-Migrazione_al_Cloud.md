# Migrazione al Cloud

## Strategia di Migrazione

### Assessment Phase
1. **Discovery**: Inventario applicazioni
2. **Analysis**: Dipendenze, performance
3. **Business case**: ROI, TCO
4. **Planning**: Roadmap, priorità

### AWS Migration Hub
```bash
# Start discovery
aws discovery start-data-collection-by-agent-ids \
  --agent-ids agent-1234

# Get application inventory
aws discovery describe-configurations \
  --configuration-ids config-1234
```

## Pattern di Migrazione

### 1. Rehost (Lift-and-Shift)
```bash
# AWS Application Migration Service (MGN)
# Install agent su source server
wget https://aws-application-migration-service.s3.amazonaws.com/latest/linux/aws-replication-installer-init.py
sudo python3 aws-replication-installer-init.py

# Configura replication
aws mgn initialize-service --region eu-west-1

# Launch test instance
aws mgn start-test \
  --source-server-id s-1234567890abcdef0
```

### 2. Replatform
```yaml
# Esempio: SQL Server → RDS
# Database Migration Service (DMS)

# Create replication instance
aws dms create-replication-instance \
  --replication-instance-identifier dms-instance \
  --replication-instance-class dms.t3.medium \
  --allocated-storage 100

# Create endpoints
aws dms create-endpoint \
  --endpoint-identifier source-db \
  --endpoint-type source \
  --engine-name sqlserver \
  --server-name source.example.com \
  --port 1433 \
  --username admin \
  --password secret

aws dms create-endpoint \
  --endpoint-identifier target-db \
  --endpoint-type target \
  --engine-name aurora \
  --server-name target.rds.amazonaws.com \
  --port 3306 \
  --username admin \
  --password secret

# Create migration task
aws dms create-replication-task \
  --replication-task-identifier migration-task \
  --source-endpoint-arn arn:aws:dms:...:endpoint:source-db \
  --target-endpoint-arn arn:aws:dms:...:endpoint:target-db \
  --replication-instance-arn arn:aws:dms:...:rep:dms-instance \
  --migration-type full-load-and-cdc \
  --table-mappings file://table-mappings.json
```

### 3. Refactor
```
Monolith → Microservices

┌─────────────────┐         ┌────────┐ ┌────────┐
│   Monolithic    │    →    │Service │ │Service │
│   Application   │         │   1    │ │   2    │
└─────────────────┘         └────────┘ └────────┘
                            ┌────────┐ ┌────────┐
                            │Service │ │Service │
                            │   3    │ │   4    │
                            └────────┘ └────────┘
```

## Database Migration

### Schema Conversion Tool (AWS SCT)
```bash
# Convert Oracle → PostgreSQL schema
aws-schema-conversion-tool \
  --source-engine oracle \
  --target-engine postgresql \
  --source-schema MYSCHEMA \
  --output-file converted-schema.sql
```

### Azure Database Migration Service
```bash
# Create migration project
az datamigration project create \
  --resource-group myRG \
  --service-name my-dms \
  --name my-migration \
  --source-platform SQL \
  --target-platform SQLDB \
  --location westeurope
```

## Data Transfer

### AWS DataSync
```bash
# Create location (on-premises)
aws datasync create-location-nfs \
  --server-hostname 192.168.1.100 \
  --subdirectory /data \
  --on-prem-config AgentArns=arn:aws:datasync:...:agent/agent-123

# Create location (S3)
aws datasync create-location-s3 \
  --s3-bucket-arn arn:aws:s3:::my-bucket \
  --s3-config BucketAccessRoleArn=arn:aws:iam::...:role/datasync

# Create sync task
aws datasync create-task \
  --source-location-arn arn:aws:datasync:...:location/loc-source \
  --destination-location-arn arn:aws:datasync:...:location/loc-dest \
  --name my-sync-task

# Start execution
aws datasync start-task-execution \
  --task-arn arn:aws:datasync:...:task/task-123
```

### Azure Data Box
- Dispositivo fisico per trasferimento offline
- Fino a 100TB per Data Box
- Ideale per > 40TB

## Application Migration

### Containerizzazione
```dockerfile
# Dockerizza app legacy
FROM openjdk:11
COPY app.jar /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

```bash
# Build e push
docker build -t myapp:v1 .
docker tag myapp:v1 123456789012.dkr.ecr.eu-west-1.amazonaws.com/myapp:v1
docker push 123456789012.dkr.ecr.eu-west-1.amazonaws.com/myapp:v1

# Deploy su ECS/EKS
kubectl apply -f deployment.yaml
```

## Cutover Plan

### Fasi
1. **Pre-cutover**: Final sync, testing
2. **Cutover window**: DNS switch, monitoring
3. **Post-cutover**: Validation, rollback plan

### DNS Migration
```bash
# Gradual traffic shift con Route 53 weighted routing
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456 \
  --change-batch '{
    "Changes": [{
      "Action": "UPSERT",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "On-premises",
        "Weight": 90,
        "AliasTarget": {
          "DNSName": "old-server.example.com"
        }
      }
    }, {
      "Action": "UPSERT",
      "ResourceRecordSet": {
        "Name": "www.example.com",
        "Type": "A",
        "SetIdentifier": "AWS",
        "Weight": 10,
        "AliasTarget": {
          "DNSName": "alb-123.eu-west-1.elb.amazonaws.com"
        }
      }
    }]
  }'
```

## Disaster Recovery

### RTO e RPO
- **RTO** (Recovery Time Objective): Quanto downtime tolleri?
- **RPO** (Recovery Point Objective): Quanti dati puoi perdere?

### DR Strategies
```
┌────────────────┬─────────┬─────────┬─────────┐
│ Strategy       │ RTO     │ RPO     │ Cost    │
├────────────────┼─────────┼─────────┼─────────┤
│ Backup/Restore │ Hours   │ Hours   │ $       │
│ Pilot Light    │ Minutes │ Minutes │ $$      │
│ Warm Standby   │ Minutes │ Seconds │ $$$     │
│ Multi-Site     │ Seconds │ Seconds │ $$$$    │
└────────────────┴─────────┴─────────┴─────────┘
```

## Testing

### Migration Testing Checklist
- [ ] Performance testing
- [ ] Security testing
- [ ] Data validation
- [ ] Failover testing
- [ ] Rollback procedure
- [ ] User acceptance testing

## Post-Migration Optimization

### Week 1-2
- Monitor performance
- Fix issues
- Optimize costs

### Month 1-3
- Right-size resources
- Implement auto-scaling
- Setup monitoring/alerts

### Month 3-6
- Reserved instances
- Architecture review
- Security hardening

## Best Practices

1. **Start small**: Pilot project
2. **Automated testing**: CI/CD
3. **Backup everything**: Before cutover
4. **Monitor closely**: First 48h critical
5. **Document**: Runbooks, architecture
6. **Train team**: Cloud skills

## Esercizi
1. Plan migration per web app 3-tier
2. Setup DMS per database migration
3. Create DataSync task per file migration
4. Design DR strategy (RTO < 1h)
5. Execute blue-green deployment

## Domande
1. Quando usi rehost vs refactor?
2. Come gestisci database migration con zero downtime?
3. Qual è la differenza tra pilot light e warm standby?
4. Come testi una migrazione?
5. Quali sono i rischi principali?
