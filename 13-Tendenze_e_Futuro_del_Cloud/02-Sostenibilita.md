# Sostenibilità nel Cloud

## Cloud e Ambiente

### Impatto Ambientale
- Datacenter consumano ~1% elettricità globale
- Produzione CO₂ settore IT ≈ aviazione
- Trend crescente: +20% annuo

### Vantaggi Cloud
- **Efficienza**: Datacenter cloud > on-premises
- **Condivisione risorse**: Multi-tenancy
- **Energie rinnovabili**: Provider investono in green energy

## Provider Commitment

### AWS
- **100% rinnovabili entro 2025**
- **Carbon neutral entro 2040**
- **79+ progetti energia solare/eolica**

### Microsoft Azure
- **Carbon negative entro 2030**
- **100% rinnovabili entro 2025**
- **Underwater datacenter** (Project Natick)

### Google Cloud
- **Carbon neutral dal 2007**
- **24/7 carbon-free energy entro 2030**
- **Datacenter più efficienti**: PUE 1.10

## Metriche Sostenibilità

### Power Usage Effectiveness (PUE)
```
PUE = Total Facility Energy / IT Equipment Energy

Ideale: PUE = 1.0 (impossibile)
Google: PUE = 1.10
Industry avg: PUE = 1.67
```

### Water Usage Effectiveness (WUE)
```
WUE = Annual Water Usage (liters) / IT Equipment Energy (kWh)

Google: WUE < 0.1 L/kWh (evaporative cooling)
```

### Carbon Usage Effectiveness (CUE)
```
CUE = Total CO₂ Emissions / IT Equipment Energy
```

## Strumenti di Misurazione

### AWS Customer Carbon Footprint Tool
```bash
# Get carbon emissions data
aws sustainability get-carbon-footprint \
  --start-date 2024-01-01 \
  --end-date 2024-12-31

# Returns:
# - Total carbon emissions (metric tons CO2e)
# - Breakdown by service
# - Trend analysis
```

### Azure Sustainability Calculator
- Stima emissioni risparmiate vs on-premises
- Report per service group
- Exportable data per reporting

### Google Cloud Carbon Footprint
- Dashboard con gross emissions
- Scope 1, 2, 3 emissions
- Region comparison

## Best Practices

### 1. Region Selection
```python
# Choose low-carbon regions
CARBON_INTENSITY = {
    'eu-west-1': 'LOW',      # Ireland (wind)
    'eu-north-1': 'VERY_LOW', # Sweden (hydro)
    'us-west-1': 'MEDIUM',    # California (mixed)
    'ap-southeast-1': 'HIGH'  # Singapore (gas)
}

def select_region(workload_type):
    if workload_type == 'batch':
        # Can run in any region
        return 'eu-north-1'  # Lowest carbon
    else:
        # Latency-sensitive
        return closest_low_carbon_region()
```

### 2. Right-Sizing
```bash
# AWS Compute Optimizer recommendations
aws compute-optimizer get-ec2-instance-recommendations \
  --instance-arns arn:aws:ec2:...:instance/i-1234567890

# Downsize over-provisioned instances = less energy
```

### 3. Auto-Scaling
```yaml
# Scale to zero when not in use
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app
  minReplicas: 0  # Scale to zero
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 50
```

### 4. Scheduled Shutdown
```python
# Lambda: Stop dev/test instances overnight
import boto3
from datetime import datetime

ec2 = boto3.client('ec2')

def lambda_handler(event, context):
    hour = datetime.now().hour
    
    if 20 <= hour or hour < 8:  # 8 PM - 8 AM
        # Stop non-production instances
        response = ec2.describe_instances(
            Filters=[
                {'Name': 'tag:Environment', 'Values': ['dev', 'test']},
                {'Name': 'instance-state-name', 'Values': ['running']}
            ]
        )
        
        for reservation in response['Reservations']:
            for instance in reservation['Instances']:
                ec2.stop_instances(InstanceIds=[instance['InstanceId']])
                print(f"Stopped {instance['InstanceId']}")
```

### 5. Storage Optimization
```bash
# S3 Intelligent-Tiering: auto-move to cheaper tiers
aws s3api put-bucket-intelligent-tiering-configuration \
  --bucket my-bucket \
  --id rule-1 \
  --intelligent-tiering-configuration '{
    "Id": "rule-1",
    "Status": "Enabled",
    "Tierings": [
      {
        "Days": 90,
        "AccessTier": "ARCHIVE_ACCESS"
      },
      {
        "Days": 180,
        "AccessTier": "DEEP_ARCHIVE_ACCESS"
      }
    ]
  }'

# Delete old data
aws s3api put-bucket-lifecycle-configuration \
  --bucket my-bucket \
  --lifecycle-configuration '{
    "Rules": [{
      "Id": "Delete old logs",
      "Status": "Enabled",
      "Filter": {"Prefix": "logs/"},
      "Expiration": {"Days": 365}
    }]
  }'
```

### 6. Serverless > Always-On
```
Traditional:
┌────────────────────────────────┐
│ Server always running (24/7)   │ ← Waste energy
└────────────────────────────────┘

Serverless:
┌────┐  ┌────┐      ┌────┐
│Run │  │Idle│ ...  │Run │ ← Energy only when needed
└────┘  └────┘      └────┘
```

## Carbon-Aware Scheduling

### Temporal Shifting
```python
# Google Cloud Carbon-Aware Jobs
from google.cloud import batch_v1

def create_carbon_aware_job():
    client = batch_v1.BatchServiceClient()
    
    job = batch_v1.Job()
    job.task_groups = [create_task_group()]
    
    # Run when carbon intensity is low
    job.scheduling_policy.carbon_awareness.enabled = True
    job.scheduling_policy.carbon_awareness.target_carbon_intensity = 50
    
    request = batch_v1.CreateJobRequest(
        parent=f"projects/{project}/locations/{region}",
        job=job
    )
    
    return client.create_job(request=request)
```

### Spatial Shifting
```python
# Run in region with current lowest carbon
import requests

def get_lowest_carbon_region():
    regions = ['eu-west-1', 'eu-north-1', 'us-west-2']
    
    intensities = {}
    for region in regions:
        # Get real-time carbon intensity
        response = requests.get(
            f'https://api.carbonintensity.org.uk/regional/{region}'
        )
        intensities[region] = response.json()['data']['intensity']
    
    return min(intensities, key=intensities.get)

# Deploy to lowest-carbon region
deploy_region = get_lowest_carbon_region()
```

## Reporting e Compliance

### GHG Protocol
- **Scope 1**: Direct emissions (generators)
- **Scope 2**: Purchased electricity
- **Scope 3**: Indirect (cloud providers)

### Reporting
```python
# Generate sustainability report
import pandas as pd

def generate_sustainability_report(year):
    # Get AWS carbon footprint
    aws_emissions = get_aws_emissions(year)
    
    # Get Azure emissions
    azure_emissions = get_azure_emissions(year)
    
    # Create report
    report = pd.DataFrame({
        'Provider': ['AWS', 'Azure'],
        'Emissions (tCO2e)': [aws_emissions, azure_emissions],
        'Renewable %': [100, 100],  # Both committed to 100%
        'Savings vs On-Prem': [88, 93]  # Microsoft study
    })
    
    report.to_csv(f'sustainability_report_{year}.csv')
    return report
```

## Future Trends

### 1. Liquid Cooling
- **2-phase immersion cooling**: 50% energy savings
- **Direct-to-chip liquid**: Precision cooling

### 2. AI for Efficiency
- **Google DeepMind**: 40% datacenter cooling reduction
- **Predictive maintenance**: Prevent waste

### 3. Circular Economy
- **Server recycling**: 90%+ materials recovered
- **Refurbishment**: Extend hardware life

### 4. Hydrogen Fuel Cells
- **Zero emissions** backup power
- **Microsoft**: Testing hydrogen fuel cells

## Esercizi
1. Calculate carbon footprint della tua app
2. Optimize storage con lifecycle policies
3. Implement scheduled shutdown per dev/test
4. Compare carbon intensity across regions
5. Create sustainability dashboard

## Domande
1. Come misuriamo efficienza energetica datacenter?
2. Qual è differenza tra Scope 1, 2, 3 emissions?
3. Come scegli region più sostenibile?
4. Quali tecniche riducono energy consumption?
5. Cos'è carbon-aware scheduling?
