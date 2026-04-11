# Database Relazionali nel Cloud

## Managed RDBMS

### AWS RDS
Supporta: MySQL, PostgreSQL, MariaDB, Oracle, SQL Server

```bash
aws rds create-db-instance \
  --db-instance-identifier mydb \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username admin \
  --master-user-password MyPass123! \
  --allocated-storage 20
```

### Aurora (AWS)
- **5x faster** than MySQL
- **3x faster** than PostgreSQL
- Auto-scaling storage
- Up to 15 read replicas

### Azure SQL Database
```bash
az sql server create \
  --name myserver \
  --resource-group myRG \
  --location westeurope \
  --admin-user admin \
  --admin-password MyPass123!

az sql db create \
  --resource-group myRG \
  --server myserver \
  --name mydb \
  --service-objective S0
```

## High Availability

### Multi-AZ
```
Primary (AZ-A)     Standby (AZ-B)
┌──────────┐      ┌──────────┐
│ Master   │═════▶│ Standby  │
│ (RW)     │      │ (sync)   │
└──────────┘      └──────────┘
```

### Read Replicas
```
Master ────┬────▶ Replica 1 (read)
           ├────▶ Replica 2 (read)
           └────▶ Replica 3 (read)
```

## Backup & Recovery

### Automated Backups
```bash
# RDS automated backup (retention 7-35 days)
aws rds modify-db-instance \
  --db-instance-identifier mydb \
  --backup-retention-period 30
```

### Point-in-Time Recovery
```bash
# Restore to specific timestamp
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier mydb \
  --target-db-instance-identifier mydb-restored \
  --restore-time 2024-01-15T14:30:00Z
```

## Performance Optimization

1. **Indexing**: Proper indexes
2. **Connection pooling**: Reuse connections
3. **Read replicas**: Offload reads
4. **Caching**: Redis/Memcached
5. **Query optimization**: EXPLAIN plans

## Best Practices
- Enable Multi-AZ for production
- Regular backups
- Monitor performance (CloudWatch)
- Use parameter groups
- Encryption at-rest
- SSL/TLS connections
