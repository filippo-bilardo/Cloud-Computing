# Cost Management

## AWS Cost Management

### AWS Cost Explorer
```bash
# Get cost and usage
aws ce get-cost-and-usage \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --group-by Type=SERVICE

# Cost forecast
aws ce get-cost-forecast \
  --time-period Start=2024-02-01,End=2024-02-28 \
  --metric BLENDED_COST \
  --granularity MONTHLY
```

### AWS Budgets
```python
import boto3

budgets = boto3.client('budgets')

response = budgets.create_budget(
    AccountId='123456789012',
    Budget={
        'BudgetName': 'Monthly-Budget',
        'BudgetLimit': {
            'Amount': '1000',
            'Unit': 'USD'
        },
        'TimeUnit': 'MONTHLY',
        'BudgetType': 'COST'
    },
    NotificationsWithSubscribers=[
        {
            'Notification': {
                'NotificationType': 'ACTUAL',
                'ComparisonOperator': 'GREATER_THAN',
                'Threshold': 80,
                'ThresholdType': 'PERCENTAGE'
            },
            'Subscribers': [
                {
                    'SubscriptionType': 'EMAIL',
                    'Address': 'alerts@example.com'
                }
            ]
        }
    ]
)
```

### Cost Allocation Tags
```bash
# Tag resources
aws ec2 create-tags \
  --resources i-1234567890abcdef0 \
  --tags Key=Environment,Value=Production Key=Project,Value=WebApp

# Activate cost allocation tags
aws ce update-cost-allocation-tags-status \
  --cost-allocation-tags-status \
    TagKey=Environment,Status=Active \
    TagKey=Project,Status=Active
```

## Azure Cost Management

### Cost Analysis
```bash
# Get costs
az consumption usage list \
  --start-date 2024-01-01 \
  --end-date 2024-01-31

# Create budget
az consumption budget create \
  --budget-name monthly-budget \
  --amount 1000 \
  --time-grain Monthly \
  --start-date 2024-01-01 \
  --end-date 2024-12-31 \
  --notifications \
    threshold=80 \
    operator=GreaterThan \
    contact-emails="alerts@example.com"
```

## Google Cloud Cost Management

### Budgets and Alerts
```bash
# Create budget
gcloud billing budgets create \
  --billing-account=012345-6789AB-CDEF01 \
  --display-name="Monthly Budget" \
  --budget-amount=1000USD \
  --threshold-rule=percent=50 \
  --threshold-rule=percent=80 \
  --threshold-rule=percent=100
```

## Cost Optimization Tools

### AWS Trusted Advisor
- Cost optimization recommendations
- Idle resources identification
- Reserved Instance optimization

### AWS Compute Optimizer
- Right-sizing recommendations
- ML-based analysis

### Azure Advisor
- Cost recommendations
- Performance optimization

### Google Cloud Recommender
- Cost saving opportunities
- Security recommendations

## FinOps Best Practices

1. **Visibility**: Tag everything
2. **Accountability**: Chargeback/showback per team
3. **Optimization**: Continuous cost review
4. **Automation**: Automated shutdown/scaling
5. **Culture**: Cost awareness in organization

## Esercizi
1. Setup AWS Budget con alerts
2. Analizza costi per servizio con Cost Explorer
3. Implementa tagging strategy
4. Create cost dashboard
