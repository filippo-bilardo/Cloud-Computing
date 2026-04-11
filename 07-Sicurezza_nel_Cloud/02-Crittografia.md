# Crittografia nel Cloud

## Introduzione

La crittografia è fondamentale per proteggere dati nel cloud, sia at rest che in transit.

## Concetti Fondamentali

### Encryption vs Encoding vs Hashing

**Encryption**: Reversibile con chiave
- Simmetrica: Stessa chiave encrypt/decrypt
- Asimmetrica: Coppia chiavi pubblica/privata

**Encoding**: Trasformazione reversibile senza chiave
- Base64, URL encoding
- Non è sicurezza!

**Hashing**: Irreversibile, one-way
- SHA-256, bcrypt
- Per password, integrity check

## Encryption at Rest

### Dati a Riposo

Protezione dati memorizzati su disco.

### Metodi

**1. Server-Side Encryption (SSE)**

Provider gestisce encryption.

**AWS S3 SSE:**
```bash
# SSE-S3 (chiavi gestite da AWS)
aws s3 cp file.txt s3://bucket/ --sse AES256

# SSE-KMS (AWS Key Management Service)
aws s3 cp file.txt s3://bucket/ --sse aws:kms --sse-kms-key-id key-id

# SSE-C (chiavi fornite dal cliente)
aws s3 cp file.txt s3://bucket/ --sse-c AES256 --sse-c-key fileb://key.bin
```

**2. Client-Side Encryption**

Cliente encrypta prima di upload.

```python
from cryptography.fernet import Fernet

# Generare chiave
key = Fernet.generate_key()
cipher = Fernet(key)

# Encrypt
data = b"Sensitive data"
encrypted = cipher.encrypt(data)

# Upload encrypted data
s3.put_object(Bucket='bucket', Key='file.enc', Body=encrypted)

# Download e decrypt
obj = s3.get_object(Bucket='bucket', Key='file.enc')
encrypted_data = obj['Body'].read()
decrypted = cipher.decrypt(encrypted_data)
```

### Database Encryption

**Transparent Data Encryption (TDE)**

Encrypts database files.

**AWS RDS:**
```bash
aws rds create-db-instance \
    --db-instance-identifier mydb \
    --storage-encrypted \
    --kms-key-id arn:aws:kms:region:account:key/key-id
```

**Field-Level Encryption**

Encrypt singoli campi.

```sql
-- PostgreSQL pgcrypto
CREATE EXTENSION pgcrypto;

INSERT INTO users (email, ssn) VALUES (
    'user@example.com',
    pgp_sym_encrypt('123-45-6789', 'encryption-key')
);

SELECT email, pgp_sym_decrypt(ssn, 'encryption-key') 
FROM users;
```

## Encryption in Transit

### Dati in Movimento

Protezione durante trasmissione.

### TLS/SSL

**Transport Layer Security**

**Best Practices:**
- TLS 1.2+ only (deprecare TLS 1.0/1.1)
- Strong cipher suites
- Certificate management
- Certificate pinning per mobile apps

**Enforce HTTPS:**

**AWS S3 Bucket Policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Deny",
    "Principal": "*",
    "Action": "s3:*",
    "Resource": "arn:aws:s3:::bucket/*",
    "Condition": {
      "Bool": {
        "aws:SecureTransport": "false"
      }
    }
  }]
}
```

**Application Load Balancer HTTPS:**
```bash
aws elbv2 create-listener \
    --load-balancer-arn arn:aws:elasticloadbalancing:... \
    --protocol HTTPS \
    --port 443 \
    --certificates CertificateArn=arn:aws:acm:... \
    --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:...
```

### VPN Encryption

**IPSec** per Site-to-Site VPN

**Algoritmi:**
- Encryption: AES-256
- Authentication: SHA-256
- Key Exchange: Diffie-Hellman Group 14+

## Key Management

### Key Management Service (KMS)

Gestione centralizzata chiavi crittografiche.

**AWS KMS:**
```bash
# Creare chiave
aws kms create-key \
    --description "Encryption key for app data"

# Creare alias
aws kms create-alias \
    --alias-name alias/app-data-key \
    --target-key-id key-id

# Encrypt data
aws kms encrypt \
    --key-id alias/app-data-key \
    --plaintext fileb://plaintext.txt \
    --output text \
    --query CiphertextBlob | base64 --decode > encrypted.bin

# Decrypt
aws kms decrypt \
    --ciphertext-blob fileb://encrypted.bin \
    --output text \
    --query Plaintext | base64 --decode > decrypted.txt
```

**Azure Key Vault:**
```bash
# Creare key vault
az keyvault create \
    --name myvault \
    --resource-group mygroup

# Creare chiave
az keyvault key create \
    --vault-name myvault \
    --name mykey \
    --protection software

# Encrypt
az keyvault key encrypt \
    --vault-name myvault \
    --name mykey \
    --algorithm RSA-OAEP \
    --value "secret text"
```

### Key Hierarchy

```
┌─────────────────────────┐
│   Master Key (HSM)      │  ← Root of trust
└───────────┬─────────────┘
            │ encrypts
┌───────────▼─────────────┐
│   Data Encryption Keys  │  ← Per-object keys
│   (DEK)                 │
└───────────┬─────────────┘
            │ encrypts
┌───────────▼─────────────┐
│   Actual Data           │
└─────────────────────────┘
```

**Envelope Encryption:**
1. Generate DEK
2. Encrypt data with DEK
3. Encrypt DEK with master key
4. Store encrypted DEK with encrypted data

### Key Rotation

Cambiare chiavi periodicamente.

**AWS KMS automatic rotation:**
```bash
aws kms enable-key-rotation --key-id key-id
```

**Best Practices:**
- Rotate keys annualmente (minimum)
- Automatic rotation dove possibile
- Versioning keys
- Audit key usage

## Hardware Security Modules (HSM)

### Cloud HSM

Dedicated hardware per chiavi crittografiche.

**AWS CloudHSM:**
```bash
# Creare cluster
aws cloudhsmv2 create-cluster \
    --hsm-type hsm1.medium \
    --subnet-ids subnet-id1 subnet-id2

# Creare HSM
aws cloudhsmv2 create-hsm \
    --cluster-id cluster-id \
    --availability-zone us-east-1a
```

**Use Cases:**
- Compliance requirements (FIPS 140-2 Level 3)
- Chiavi mai esposte outside HSM
- Signing operations
- Custom crypto operations

## Certificate Management

### SSL/TLS Certificates

**AWS Certificate Manager (ACM):**
```bash
# Request certificate
aws acm request-certificate \
    --domain-name example.com \
    --subject-alternative-names www.example.com \
    --validation-method DNS

# List certificates  
aws acm list-certificates

# Describe certificate
aws acm describe-certificate \
    --certificate-arn arn:aws:acm:...
```

**Let's Encrypt (Free):**
```bash
# Certbot
sudo certbot certonly \
    --standalone \
    -d example.com \
    -d www.example.com

# Auto-renewal
sudo certbot renew
```

### Certificate Transparency

Public logs di tutti i certificati SSL.

**Monitoring:**
- https://crt.sh/
- Certificate Transparency logs

## Secrets Management

### Secrets Managers

**AWS Secrets Manager:**
```bash
# Creare secret
aws secretsmanager create-secret \
    --name prod/db/password \
    --secret-string '{"username":"admin","password":"MySecurePass"}'

# Retrieve secret
aws secretsmanager get-secret-value \
    --secret-id prod/db/password

# Rotate secret
aws secretsmanager rotate-secret \
    --secret-id prod/db/password \
    --rotation-lambda-arn arn:aws:lambda:...
```

**Azure Key Vault:**
```bash
# Aggiungere secret
az keyvault secret set \
    --vault-name myvault \
    --name dbpassword \
    --value "MySecurePass"

# Retrieve
az keyvault secret show \
    --vault-name myvault \
    --name dbpassword
```

**HashiCorp Vault:**
```bash
# Start Vault
vault server -dev

# Write secret
vault kv put secret/myapp/config \
    username=admin \
    password=secret

# Read secret
vault kv get secret/myapp/config

# Dynamic secrets (database)
vault read database/creds/my-role
```

### Best Practices Secrets

1. **Never** commit secrets in Git
2. Use secrets managers
3. Rotate secrets regularly
4. Least privilege access
5. Audit access to secrets
6. Encrypt secrets at rest
7. Use short-lived credentials

## Data Loss Prevention (DLP)

### Prevenire Data Leakage

**Tecniche:**
- Data classification
- Content inspection
- Policy enforcement
- Masking/Tokenization

**AWS Macie:**

ML-powered per trovare sensitive data in S3.

```bash
# Enable Macie
aws macie2 enable-macie

# Create classification job
aws macie2 create-classification-job \
    --s3-job-definition '{
        "bucketDefinitions": [{
            "accountId": "123456789012",
            "buckets": ["my-bucket"]
        }]
    }'
```

### Data Masking

**Static Masking:**
```sql
-- Mask credit card
SELECT CONCAT(
    '****-****-****-',
    RIGHT(credit_card, 4)
) AS masked_cc
FROM orders;
```

**Dynamic Masking:**
```sql
-- SQL Server Dynamic Data Masking
CREATE TABLE Customers (
    email VARCHAR(100) MASKED WITH (FUNCTION = 'email()'),
    phone VARCHAR(15) MASKED WITH (FUNCTION = 'partial(0,"XXX-XXX-",4)')
);
```

**Tokenization:**

Replace sensitive data with tokens.

```python
import secrets

# Token vault
token_vault = {}

def tokenize(data):
    token = secrets.token_hex(16)
    token_vault[token] = data
    return token

def detokenize(token):
    return token_vault.get(token)

# Usage
cc_number = "1234-5678-9012-3456"
token = tokenize(cc_number)  # "a3f8bc9d2e1f..."
# Store token in database
# Later retrieve original
original = detokenize(token)
```

## Compliance Encryption

### Requisiti Regolamentari

**GDPR:**
- Encryption of personal data
- Right to erasure (crypto shredding)

**PCI DSS:**
- Encrypt cardholder data at rest
- TLS for transmission
- Key management procedures

**HIPAA:**
- Encryption of ePHI
- Access controls
- Audit trails

## Quantum-Safe Cryptography

### Post-Quantum Crypto

Preparazione per computer quantistici.

**Algoritmi resistenti:**
- Lattice-based
- Hash-based
- Code-based
- Multivariate polynomial

**Timeline:**
- NIST standardization in corso
- Migration pianificata 2030+

## Esercizi Pratici

### Esercizio 1: S3 Encryption
1. Creare bucket S3
2. Abilitare default encryption
3. Upload file con diverse opzioni SSE
4. Testare bucket policy per HTTPS only

### Esercizio 2: KMS
1. Creare KMS key
2. Encrypt file con KMS
3. Setup key rotation
4. Configurare key policy

### Esercizio 3: Secrets Manager
1. Store database password in Secrets Manager
2. Applicazione retrieve secret
3. Setup automatic rotation
4. Audit access logs

### Esercizio 4: TLS/SSL
1. Request certificate con ACM
2. Configure ALB con HTTPS
3. Force redirect HTTP → HTTPS
4. Test SSL Labs rating

### Esercizio 5: Client-Side Encryption
1. Implement encryption library
2. Encrypt data before S3 upload
3. Download and decrypt
4. Key management strategy

## Domande di Verifica

1. Differenza tra encryption at rest e in transit?
2. Cosa sono SSE-S3, SSE-KMS, SSE-C?
3. Come funziona envelope encryption?
4. Perché usare HSM invece di software keys?
5. Best practices per secrets management?
6. Cos'è certificate transparency?
7. Come implementare data masking?
8. Key rotation: perché e come?
9. Differenza tra hashing e encryption?
10. Compliance requirements per PCI DSS encryption?

## Risorse

- [AWS KMS Documentation](https://docs.aws.amazon.com/kms/)
- [Azure Key Vault](https://docs.microsoft.com/azure/key-vault/)
- [OWASP Cryptographic Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)

---

*Documento aggiornato - 2024*
