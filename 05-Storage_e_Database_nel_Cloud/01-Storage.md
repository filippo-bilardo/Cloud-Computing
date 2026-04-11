# Storage nel Cloud

## Tipi di Storage

### 1. Object Storage (S3-like)
- **Flat namespace**: Key-value
- **Illimitato**: Petabyte scale
- **HTTP/REST API**
- **Use case**: Backup, media, data lake

**AWS S3, Azure Blob, Google Cloud Storage**

### 2. Block Storage (EBS-like)
- **Low latency**: ms
- **Attached to VM**
- **Use case**: Database, boot volumes

**AWS EBS, Azure Disk, GCP Persistent Disk**

### 3. File Storage (NFS/SMB)
- **Shared filesystem**
- **Multi-attach**
- **Use case**: Shared content, home directories

**AWS EFS, Azure Files, GCP Filestore**

## Storage Classes & Tiering

### S3 Storage Classes
```
Standard → $0.023/GB        (frequent access)
IA       → $0.0125/GB       (infrequent access, 30d min)
Glacier  → $0.004/GB        (archive, minutes retrieval)
Deep Ar. → $0.00099/GB      (long-term, hours retrieval)
```

### Lifecycle Policies
```json
{
  "Rules": [{
    "Id": "Archive old data",
    "Filter": {"Prefix": "logs/"},
    "Status": "Enabled",
    "Transitions": [
      {"Days": 30, "StorageClass": "STANDARD_IA"},
      {"Days": 90, "StorageClass": "GLACIER"}
    ],
    "Expiration": {"Days": 365}
  }]
}
```

## Best Practices
1. Use appropriate storage class
2. Enable versioning
3. Lifecycle policies
4. Encryption at-rest
5. Access logging
6. Cross-region replication
