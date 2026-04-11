# Modelli di Pricing Cloud

## Introduzione

Comprendere i modelli di pricing è fondamentale per ottimizzare i costi cloud ed evitare sorprese nelle fatture.

## AWS Pricing Models

### 1. On-Demand
- **Pay-per-use**: Paghi solo per le ore di utilizzo
- **Nessun impegno**: Puoi terminare quando vuoi
- **Elasticità**: Ideale per workload imprevedibili

```
EC2 t3.medium: $0.0416/ora
= $30/mese (se sempre attivo)
```

### 2. Reserved Instances (RI)
- **Sconto 40-75%** rispetto a On-Demand
- **Commitment**: 1 anno o 3 anni
- **Payment**: All upfront, Partial upfront, No upfront

```
EC2 t3.medium On-Demand: $30/mese
EC2 t3.medium RI 1-year: $18/mese (40% sconto)
EC2 t3.medium RI 3-year: $12/mese (60% sconto)
```

### 3. Savings Plans
- **Flessibilità**: Cambi istanza type, regione, OS
- **Commitment**: $/ora per 1 o 3 anni
- **Sconto simile a RI**: 40-66%

### 4. Spot Instances
- **Sconto 50-90%** rispetto a On-Demand
- **Interruptible**: AWS può terminare con 2 minuti di preavviso
- **Use case**: Batch processing, big data, CI/CD

```python
# AWS Spot Instance request
import boto3

ec2 = boto3.client('ec2')

response = ec2.request_spot_instances(
    SpotPrice='0.02',  # Max price
    InstanceCount=5,
    LaunchSpecification={
        'ImageId': 'ami-12345678',
        'InstanceType': 't3.medium',
        'KeyName': 'my-key'
    }
)
```

## Azure Pricing

### Pay-As-You-Go
Standard pricing, simile a AWS On-Demand

### Reserved VM Instances
- 1 o 3 anni commitment
- Sconto fino a 72%

### Azure Hybrid Benefit
- Usa licenze Windows Server/SQL esistenti
- Risparmio fino al 40%

### Spot VMs
- Sconto fino all'90%
- Ideale per batch jobs

## Google Cloud Pricing

### On-Demand
Standard pricing

### Committed Use Discounts (CUD)
- 1 o 3 anni
- Sconto fino al 57% per Compute Engine
- Sconto fino al 70% per GKE

### Sustained Use Discounts
- **Automatico**: Sconto applicato senza commit
- VM attive > 25% del mese: Sconto progressivo fino al 30%

### Preemptible VMs
- Sconto fino all'80%
- Max 24 ore di vita
- Possono essere terminate in qualsiasi momento

## Confronto Prezzi

| Service | AWS | Azure | GCP |
|---------|-----|-------|-----|
| VM (2 vCPU, 8GB) | $0.0832/ora | $0.096/ora | $0.0475/ora |
| Storage (GB/mese) | $0.023 | $0.0208 | $0.020 |
| Data transfer out | $0.09/GB | $0.087/GB | $0.12/GB |

## Costi Nascosti

### Data Transfer
```
Inbound: Gratis (tutti i provider)
Outbound to Internet: $0.08-0.12/GB
Cross-region: $0.01-0.02/GB
Cross-AZ: $0.01/GB (AWS)
```

### API Calls
```
S3 GET: $0.0004/1000 requests
DynamoDB Read: $0.25/million requests
Lambda invocations: $0.20/million
```

### Storage Classes
```
S3 Standard: $0.023/GB/mese
S3 Infrequent Access: $0.0125/GB/mese
S3 Glacier: $0.004/GB/mese
S3 Deep Archive: $0.00099/GB/mese
```

## Best Practices

1. **Right-sizing**: Non sovradimensionare
2. **Auto-scaling**: Scala in base al carico
3. **Scheduled start/stop**: Spegni risorse non usate
4. **Use Spot/Preemptible**: Per workload fault-tolerant
5. **Reserved capacity**: Per workload predicibili

## Esercizi
1. Calcola costo mensile per app web (ALB + EC2 + RDS)
2. Confronta On-Demand vs Reserved vs Spot
3. Stima savings con auto-scaling
4. Analizza data transfer costs

## Domande
1. Quando usi Spot Instances?
2. Qual è la differenza tra RI e Savings Plans?
3. Come funzionano i Sustained Use Discounts di GCP?
4. Quali sono i costi nascosti più comuni?
5. Come ottimizzi i costi di storage?
