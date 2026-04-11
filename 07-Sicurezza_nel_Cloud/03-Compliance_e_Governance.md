# Compliance e Governance nel Cloud

## Introduzione

Compliance e governance sono essenziali per garantire che l'uso del cloud rispetti normative, standard e policy aziendali.

## Framework di Compliance

### Standard Internazionali

**ISO 27001/27002**
- Information Security Management System (ISMS)
- Controlli di sicurezza
- Certificazione audit

**SOC 2 (Service Organization Control)**
- Type I: Design dei controlli
- Type II: Efficacia operativa
- Trust Service Criteria (TSC)

**PCI DSS**
- Payment Card Industry Data Security Standard
- 12 requisiti principali
- Mandatory per chi gestisce carte di credito

**HIPAA**
- Health Insurance Portability and Accountability Act
- Protezione ePHI (electronic Protected Health Information)
- Privacy e Security Rule

**GDPR**
- General Data Protection Regulation
- Protezione dati personali EU
- Right to erasure, portability
- Multe fino a 4% fatturato globale

## Compliance nei Cloud Provider

### AWS Compliance Programs

**Certificazioni:**
- ISO 27001, 27017, 27018
- SOC 1, 2, 3
- PCI DSS Level 1
- HIPAA eligible
- FedRAMP (government)

**AWS Artifact:**

Repository centrale compliance.

```bash
# List compliance reports
aws artifact list-reports

# Get report
aws artifact get-report \
    --report-id report-id \
    --termination-token token
```

**AWS Config:**

Compliance monitoring automatico.

```bash
# Enable Config
aws configservice put-configuration-recorder \
    --configuration-recorder name=default,roleARN=arn:aws:iam::... \
    --recording-group allSupported=true,includeGlobalResourceTypes=true

# Create rule
aws configservice put-config-rule \
    --config-rule '{
        "ConfigRuleName": "encrypted-volumes",
        "Source": {
            "Owner": "AWS",
            "SourceIdentifier": "ENCRYPTED_VOLUMES"
        }
    }'

# Get compliance
aws configservice describe-compliance-by-config-rule \
    --config-rule-names encrypted-volumes
```

### Azure Compliance

**Certificazioni:**
- ISO 27001, 27018
- SOC 1, 2, 3
- PCI DSS
- HIPAA/HITECH
- FedRAMP High

**Azure Policy:**

Governance as code.

```bash
# Create policy definition
az policy definition create \
    --name "require-tag" \
    --rules '{
        "if": {
            "field": "tags",
            "exists": "false"
        },
        "then": {
            "effect": "deny"
        }
    }'

# Assign policy
az policy assignment create \
    --name "enforce-tags" \
    --policy "require-tag" \
    --scope /subscriptions/xxx

# Check compliance
az policy state list \
    --resource-group mygroup
```

**Azure Blueprints:**

Package di governance (policies, roles, ARM templates).

### GCP Compliance

**Certificazioni:**
- ISO 27001, 27017, 27018
- SOC 1, 2, 3
- PCI DSS
- HIPAA

**Security Command Center:**

Unified security e compliance.

```bash
# List findings
gcloud scc findings list organizations/123456 \
    --filter="state=\"ACTIVE\"" \
    --page-size=10

# Get compliance posture
gcloud scc posture-deployments describe DEPLOYMENT_NAME \
    --organization=123456 \
    --location=global
```

## Data Governance

### Data Classification

**Livelli:**
1. **Public**: Informazioni pubbliche
2. **Internal**: Solo per dipendenti
3. **Confidential**: Dati sensibili business
4. **Restricted**: Dati critici (PII, PHI, financial)

**Tagging:**
```bash
# AWS S3 object tagging
aws s3api put-object-tagging \
    --bucket my-bucket \
    --key document.pdf \
    --tagging 'TagSet=[{Key=Classification,Value=Confidential},{Key=Owner,Value=Finance}]'
```

### Data Residency

Dove i dati risiedono geograficamente.

**GDPR Requirements:**
- Dati personali EU devono rimanere in EU
- Eccezioni con Standard Contractual Clauses

**Implementazione:**
```bash
# AWS - Limitare regioni
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Deny",
    "Action": "*",
    "Resource": "*",
    "Condition": {
      "StringNotEquals": {
        "aws:RequestedRegion": ["eu-west-1", "eu-central-1"]
      }
    }
  }]
}
```

### Data Retention

Policy di conservazione dati.

**S3 Lifecycle Policy:**
```json
{
  "Rules": [{
    "Id": "Retention-Policy",
    "Status": "Enabled",
    "Filter": {
      "Prefix": "logs/"
    },
    "Transitions": [{
      "Days": 30,
      "StorageClass": "STANDARD_IA"
    }, {
      "Days": 90,
      "StorageClass": "GLACIER"
    }],
    "Expiration": {
      "Days": 2555
    }
  }]
}
```

## Governance Tools

### Infrastructure as Code Scanning

**Terraform Compliance:**
```python
# terraform-compliance
Feature: S3 Bucket Encryption
  Scenario: Ensure all S3 buckets have encryption
    Given I have aws_s3_bucket defined
    Then it must have server_side_encryption_configuration
```

**Checkov:**
```bash
# Scan Terraform
checkov -d . --framework terraform

# Scan CloudFormation
checkov -f template.yaml --framework cloudformation

# Custom policies
checkov -d . --external-checks-dir ./custom-policies
```

**tfsec:**
```bash
tfsec .

# Output:
# ❌ AWS001: S3 Bucket has public access
# ❌ AWS002: S3 Bucket encryption not enabled
```

### Policy as Code

**Open Policy Agent (OPA):**
```rego
package terraform.analysis

import input as tfplan

deny[msg] {
    resource := tfplan.resource_changes[_]
    resource.type == "aws_s3_bucket"
    not resource.change.after.versioning[_].enabled
    msg := sprintf("S3 bucket %s must have versioning enabled", [resource.address])
}

deny[msg] {
    resource := tfplan.resource_changes[_]
    resource.type == "aws_db_instance"
    resource.change.after.publicly_accessible == true
    msg := sprintf("RDS instance %s must not be publicly accessible", [resource.address])
}
```

**Usage:**
```bash
# Generate plan
terraform plan -out=tfplan.binary
terraform show -json tfplan.binary > tfplan.json

# Validate with OPA
opa eval --data policy.rego --input tfplan.json "data.terraform.analysis.deny"
```

### Cloud Custodian

Policy engine multi-cloud.

**Example policy:**
```yaml
policies:
  - name: s3-encryption-required
    resource: s3
    filters:
      - type: missing-statement
        statement_ids:
          - RequireEncryptedPutObject
    actions:
      - type: notify
        to:
          - security@company.com
        subject: "S3 Bucket Missing Encryption Policy"
      - type: tag
        key: compliance-status
        value: non-compliant

  - name: unused-ebs-volumes
    resource: ebs
    filters:
      - State: available
      - type: value
        key: CreateTime
        op: greater-than
        value_type: age
        value: 30
    actions:
      - type: delete
```

**Run:**
```bash
custodian run -s output policy.yml
```

## Audit e Logging

### Centralized Logging

**Architecture:**
```
┌────────────┐  ┌────────────┐  ┌────────────┐
│ CloudTrail │  │  VPC Flow  │  │   App      │
│   Logs     │  │   Logs     │  │   Logs     │
└──────┬─────┘  └──────┬─────┘  └──────┬─────┘
       │               │               │
       └───────────────┼───────────────┘
                       ▼
               ┌───────────────┐
               │  S3 Bucket    │
               │  (Archive)    │
               └───────┬───────┘
                       │
                       ▼
               ┌───────────────┐
               │  Elasticsearch│
               │  (Search)     │
               └───────┬───────┘
                       │
                       ▼
               ┌───────────────┐
               │    Kibana     │
               │ (Visualization)│
               └───────────────┘
```

**AWS CloudWatch Logs:**
```bash
# Create log group
aws logs create-log-group --log-group-name /aws/compliance

# Retention
aws logs put-retention-policy \
    --log-group-name /aws/compliance \
    --retention-in-days 2555  # 7 years

# Export to S3
aws logs create-export-task \
    --log-group-name /aws/compliance \
    --from 1609459200000 \
    --to 1612137600000 \
    --destination compliance-logs-bucket
```

### Immutable Logs

Logs non modificabili per audit.

**S3 Object Lock:**
```bash
# Enable Object Lock (must be on bucket creation)
aws s3api create-bucket \
    --bucket compliance-logs \
    --object-lock-enabled-for-bucket

# Set retention
aws s3api put-object-lock-configuration \
    --bucket compliance-logs \
    --object-lock-configuration '{
        "ObjectLockEnabled": "Enabled",
        "Rule": {
            "DefaultRetention": {
                "Mode": "COMPLIANCE",
                "Years": 7
            }
        }
    }'
```

## Access Reviews

### Periodic Access Reviews

**IAM Access Analyzer:**
```bash
# Create analyzer
aws accessanalyzer create-analyzer \
    --analyzer-name OrgAnalyzer \
    --type ORGANIZATION

# List findings
aws accessanalyzer list-findings \
    --analyzer-arn arn:aws:access-analyzer:...

# Get finding
aws accessanalyzer get-finding \
    --analyzer-arn arn:aws:access-analyzer:... \
    --id finding-id
```

**Azure AD Access Reviews:**
```bash
# Create access review
az ad access-review create \
    --display-name "Quarterly Admin Review" \
    --scope-type GroupMembers \
    --group-id admin-group-id
```

### Unused Permissions

**AWS IAM Access Advisor:**
```bash
# Generate report
aws iam generate-service-last-accessed-details \
    --arn arn:aws:iam::123456789012:user/alice

# Get report
aws iam get-service-last-accessed-details \
    --job-id job-id
```

## Compliance Automation

### Continuous Compliance

**AWS Security Hub:**

Aggregator di findings compliance.

```bash
# Enable Security Hub
aws securityhub enable-security-hub

# Enable compliance standards
aws securityhub batch-enable-standards \
    --standards-subscription-requests '[
        {"StandardsArn": "arn:aws:securityhub:us-east-1::standards/cis-aws-foundations-benchmark/v/1.2.0"},
        {"StandardsArn": "arn:aws:securityhub:::ruleset/pci-dss/v/3.2.1"}
    ]'

# Get compliance score
aws securityhub get-findings \
    --filters '{"ComplianceStatus": [{"Value": "FAILED", "Comparison": "EQUALS"}]}'
```

### Remediation Automatica

**AWS Config Remediation:**
```yaml
RemediationConfigurations:
  - ConfigRuleName: s3-bucket-public-read-prohibited
    TargetType: SSM_DOCUMENT
    TargetIdentifier: AWS-PublishSNSNotification
    Parameters:
      AutomationAssumeRole:
        StaticValue:
          Values:
            - arn:aws:iam::123456789012:role/AutoRemediationRole
      TopicArn:
        StaticValue:
          Values:
            - arn:aws:sns:us-east-1:123456789012:ComplianceTopic
```

**Automated Remediation Lambda:**
```python
import boto3

s3 = boto3.client('s3')

def lambda_handler(event, context):
    """
    Remediate public S3 buckets
    """
    # Parse Config event
    bucket_name = event['detail']['requestParameters']['bucketName']
    
    # Block public access
    s3.put_public_access_block(
        Bucket=bucket_name,
        PublicAccessBlockConfiguration={
            'BlockPublicAcls': True,
            'IgnorePublicAcls': True,
            'BlockPublicPolicy': True,
            'RestrictPublicBuckets': True
        }
    )
    
    # Notify
    sns = boto3.client('sns')
    sns.publish(
        TopicArn='arn:aws:sns:...',
        Subject=f'Remediated Public Bucket: {bucket_name}',
        Message=f'Public access blocked for bucket {bucket_name}'
    )
    
    return {'statusCode': 200}
```

## Cost Governance

### Budget e Alerting

**AWS Budgets:**
```bash
# Create budget
aws budgets create-budget \
    --account-id 123456789012 \
    --budget file://budget.json \
    --notifications-with-subscribers file://notifications.json
```

**budget.json:**
```json
{
  "BudgetName": "Monthly-Budget",
  "BudgetLimit": {
    "Amount": "1000",
    "Unit": "USD"
  },
  "TimeUnit": "MONTHLY",
  "BudgetType": "COST"
}
```

### Tagging Policy

Resource tagging enforcement.

**AWS Organizations Tag Policies:**
```json
{
  "tags": {
    "Environment": {
      "tag_key": {
        "@@assign": "Environment"
      },
      "tag_value": {
        "@@assign": ["Production", "Development", "Staging"]
      },
      "enforced_for": {
        "@@assign": ["ec2:instance", "s3:bucket", "rds:db"]
      }
    },
    "Owner": {
      "tag_key": {
        "@@assign": "Owner"
      },
      "enforced_for": {
        "@@assign": ["*"]
      }
    }
  }
}
```

## Regulatory Compliance

### GDPR Compliance

**Requisiti:**
1. **Lawful basis** per processing
2. **Consent** management
3. **Right to access** (data portability)
4. **Right to erasure** ("right to be forgotten")
5. **Data breach notification** (72 ore)
6. **Privacy by design**
7. **DPO** (Data Protection Officer)

**Implementazione:**
```python
# Right to erasure
def delete_user_data(user_id):
    # Delete from all systems
    db.delete_user(user_id)
    s3.delete_objects_by_tag('UserId', user_id)
    logs.purge_user_logs(user_id)
    
    # Crypto shredding (alternative)
    kms.delete_key(f'user-{user_id}-key')  # Rende dati inaccessibili
    
    # Audit trail
    audit_log.record({
        'event': 'user_data_deleted',
        'user_id': user_id,
        'timestamp': datetime.now(),
        'requester': current_user
    })
```

### PCI DSS Compliance

**Requisiti chiave:**
1. Install and maintain firewall
2. Don't use default passwords
3. Protect stored cardholder data
4. Encrypt transmission
5. Anti-virus
6. Secure systems
7. Restrict access
8. Unique IDs
9. Restrict physical access
10. Track access
11. Test security
12. Information security policy

**Cardholder Data Environment (CDE):**
```
┌─────────────────────────────────┐
│  CDE (PCI DSS Scope)            │
│                                 │
│  ┌─────────────────────────┐   │
│  │  Payment Processing     │   │
│  │  - Card data encrypted  │   │
│  │  - Tokenization         │   │
│  │  - Audit logging        │   │
│  └─────────────────────────┘   │
│                                 │
│  Network segmentation           │
│  Firewall rules                 │
│  No direct internet access      │
└─────────────────────────────────┘
```

### HIPAA Compliance

**HIPAA Requirements:**
- **Administrative Safeguards**: Policies, training
- **Physical Safeguards**: Datacenter security
- **Technical Safeguards**: Encryption, access control
- **Breach Notification Rule**

**BAA (Business Associate Agreement):**

Richiesto tra covered entity e AWS/Azure/GCP.

## Governance Best Practices

### 1. Centralized Governance

Multi-account/subscription strategy.

**AWS Organizations:**
```
Root
├── Production OU
│   ├── App Production Account
│   └── Data Production Account
├── Development OU
│   ├── Dev Account
│   └── Test Account
└── Security OU
    ├── Logging Account
    └── Security Tools Account
```

**Service Control Policies (SCP):**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Deny",
    "Action": [
      "ec2:RunInstances"
    ],
    "Resource": "*",
    "Condition": {
      "StringNotEquals": {
        "ec2:InstanceType": [
          "t3.micro",
          "t3.small",
          "t3.medium"
        ]
      }
    }
  }]
}
```

### 2. Least Privilege

Default deny, explicit allow.

### 3. Separation of Duties

Production changes require approval.

### 4. Automated Compliance Checks

CI/CD integration.

### 5. Regular Audits

Quarterly compliance reviews.

### 6. Incident Response Plan

Documented procedures.

### 7. Training

Security awareness programs.

## Esercizi Pratici

1. **AWS Config**: Setup compliance rules
2. **Azure Policy**: Enforce tagging
3. **GCP Security Command Center**: Review findings
4. **IaC Scanning**: Integrate checkov in CI/CD
5. **Automated Remediation**: Lambda per compliance
6. **Audit Logs**: Setup immutable logging
7. **Access Review**: IAM permissions audit
8. **GDPR**: Implement right to erasure
9. **Cost Governance**: Budget alerts
10. **Multi-account**: Organizations setup

## Domande di Verifica

1. Differenza tra ISO 27001 e SOC 2?
2. GDPR: Quali sono i diritti degli utenti?
3. Come implementare data residency in AWS?
4. Cosa sono le SCPs in AWS Organizations?
5. Come automatizzare compliance checking?
6. Requisiti PCI DSS per encryption?
7. Object Lock vs Versioning in S3?
8. Come implementare separation of duties?
9. Audit trails: quali log sono essenziali?
10. Crypto shredding per GDPR right to erasure?

## Risorse

- [AWS Compliance Programs](https://aws.amazon.com/compliance/programs/)
- [Azure Compliance](https://docs.microsoft.com/azure/compliance/)
- [GDPR Official Text](https://gdpr-info.eu/)
- [PCI Security Standards](https://www.pcisecuritystandards.org/)
- [NIST Frameworks](https://www.nist.gov/cyberframework)

---

*Documento aggiornato - 2024*
