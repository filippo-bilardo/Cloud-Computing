# Ottimizzazione delle Risorse

## Right-Sizing

### AWS Compute Optimizer
```python
import boto3

compute_optimizer = boto3.client('compute-optimizer')

# Get EC2 recommendations
response = compute_optimizer.get_ec2_instance_recommendations()

for recommendation in response['instanceRecommendations']:
    instance_arn = recommendation['instanceArn']
    current = recommendation['currentInstanceType']
    
    print(f"Instance: {instance_arn}")
    print(f"Current: {current}")
    
    for option in recommendation['recommendationOptions']:
        print(f"Recommended: {option['instanceType']}")
        print(f"Performance risk: {option['performanceRisk']}")
        print(f"Savings: ${option['projectedUtilizationMetrics']}")
```

## Auto-Scaling

### AWS Auto Scaling
```bash
# Create launch template
aws ec2 create-launch-template \
  --launch-template-name my-template \
  --version-description v1 \
  --launch-template-data '{
    "ImageId": "ami-12345678",
    "InstanceType": "t3.micro",
    "SecurityGroupIds": ["sg-12345678"]
  }'

# Create Auto Scaling Group
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name my-asg \
  --launch-template LaunchTemplateName=my-template \
  --min-size 1 \
  --max-size 10 \
  --desired-capacity 2 \
  --target-group-arns arn:aws:elasticloadbalancing:... \
  --health-check-type ELB \
  --health-check-grace-period 300

# Target tracking policy
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name my-asg \
  --policy-name cpu-target-tracking \
  --policy-type TargetTrackingScaling \
  --target-tracking-configuration '{
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ASGAverageCPUUtilization"
    },
    "TargetValue": 50.0
  }'
```

## Scheduled Shutdown

### Lambda per Auto-Shutdown
```python
import boto3
from datetime import datetime

ec2 = boto3.client('ec2')

def lambda_handler(event, context):
    # Get instances with auto-stop tag
    filters = [
        {'Name': 'tag:AutoStop', 'Values': ['true']},
        {'Name': 'instance-state-name', 'Values': ['running']}
    ]
    
    instances = ec2.describe_instances(Filters=filters)
    instance_ids = []
    
    for reservation in instances['Reservations']:
        for instance in reservation['Instances']:
            instance_ids.append(instance['InstanceId'])
    
    if instance_ids:
        ec2.stop_instances(InstanceIds=instance_ids)
        print(f'Stopped instances: {instance_ids}')
    
    return {'statusCode': 200}

# EventBridge rule: cron(0 20 * * ? *)  # 20:00 UTC daily
```

## Storage Optimization

### S3 Lifecycle Policies
```json
{
  "Rules": [
    {
      "Id": "Archive old logs",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "logs/"
      },
      "Transitions": [
        {
          "Days": 30,
          "StorageClass": "STANDARD_IA"
        },
        {
          "Days": 90,
          "StorageClass": "GLACIER"
        }
      ],
      "Expiration": {
        "Days": 365
      }
    }
  ]
}
```

### RDS Storage Auto-Scaling
```bash
aws rds modify-db-instance \
  --db-instance-identifier mydb \
  --max-allocated-storage 1000 \
  --apply-immediately
```

## Reserved Capacity

### Purchase Reserved Instances
```bash
# AWS
aws ec2 purchase-reserved-instances-offering \
  --reserved-instances-offering-id 12345678-1234-1234-1234-123456789012 \
  --instance-count 5

# Calculate savings
aws ce get-reservation-utilization \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY
```

## Spot/Preemptible Instances

### Spot Fleet
```python
import boto3

ec2 = boto3.client('ec2')

response = ec2.request_spot_fleet(
    SpotFleetRequestConfig={
        'IamFleetRole': 'arn:aws:iam::...:role/spot-fleet-role',
        'TargetCapacity': 10,
        'SpotPrice': '0.05',
        'LaunchSpecifications': [
            {
                'ImageId': 'ami-12345678',
                'InstanceType': 't3.medium',
                'KeyName': 'my-key',
                'SpotPrice': '0.05'
            },
            {
                'ImageId': 'ami-12345678',
                'InstanceType': 't3.large',
                'KeyName': 'my-key',
                'SpotPrice': '0.08'
            }
        ],
        'AllocationStrategy': 'lowestPrice'
    }
)
```

## Database Optimization

### RDS Instance Right-Sizing
```sql
-- Identify unused indexes
SELECT 
  schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY pg_relation_size(indexrelid) DESC;

-- Find slow queries
SELECT 
  query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;
```

### DynamoDB On-Demand vs Provisioned
```python
# Switch to on-demand
dynamodb = boto3.client('dynamodb')

dynamodb.update_table(
    TableName='my-table',
    BillingMode='PAY_PER_REQUEST'
)

# Or provisioned with auto-scaling
dynamodb.update_table(
    TableName='my-table',
    BillingMode='PROVISIONED',
    ProvisionedThroughput={
        'ReadCapacityUnits': 5,
        'WriteCapacityUnits': 5
    }
)
```

## Best Practices Summary

1. **Monitor continuously**: CloudWatch, Azure Monitor
2. **Right-size regularly**: Review recommendations monthly
3. **Use auto-scaling**: Scale based on demand
4. **Schedule resources**: Stop non-production overnight
5. **Optimize storage**: Lifecycle policies, compression
6. **Reserved capacity**: For predictable workloads
7. **Spot instances**: For fault-tolerant workloads
8. **Delete unused resources**: EBS volumes, snapshots

## Esercizi
1. Implement auto-scaling group con target tracking
2. Create Lambda per nightly shutdown
3. Setup S3 lifecycle policy
4. Analyze RDS performance e right-size
5. Calculate ROI per Reserved Instances
