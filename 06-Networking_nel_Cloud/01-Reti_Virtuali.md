# Reti Virtuali nel Cloud

## VPC (Virtual Private Cloud)

### Concetti Base
- **CIDR block**: Range IP privato (es. 10.0.0.0/16)
- **Subnets**: Segmenti della VPC
  - **Public**: Con accesso Internet (via IGW)
  - **Private**: Senza accesso Internet diretto
- **Route tables**: Routing rules
- **Internet Gateway (IGW)**: Accesso Internet
- **NAT Gateway**: Internet access per private subnets

### Architettura Tipica
```
VPC 10.0.0.0/16
├── Public Subnet (10.0.1.0/24) [AZ-A]
│   ├── Web Server
│   └── NAT Gateway
├── Private Subnet (10.0.2.0/24) [AZ-A]
│   └── App Server
├── Public Subnet (10.0.3.0/24) [AZ-B]
│   ├── Web Server
│   └── NAT Gateway
└── Private Subnet (10.0.4.0/24) [AZ-B]
    └── App Server
```

## AWS VPC

```bash
# Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16

# Create subnet
aws ec2 create-subnet \
  --vpc-id vpc-123456 \
  --cidr-block 10.0.1.0/24 \
  --availability-zone eu-west-1a

# Create Internet Gateway
aws ec2 create-internet-gateway
aws ec2 attach-internet-gateway \
  --vpc-id vpc-123456 \
  --internet-gateway-id igw-123456

# Route table
aws ec2 create-route \
  --route-table-id rtb-123456 \
  --destination-cidr-block 0.0.0.0/0 \
  --gateway-id igw-123456
```

## Security Groups vs NACLs

### Security Groups (Stateful)
```bash
# Allow HTTP from anywhere
aws ec2 authorize-security-group-ingress \
  --group-id sg-123456 \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0
```

### NACLs (Stateless)
```bash
# Create NACL rule
aws ec2 create-network-acl-entry \
  --network-acl-id acl-123456 \
  --rule-number 100 \
  --protocol tcp \
  --port-range From=80,To=80 \
  --cidr-block 0.0.0.0/0 \
  --egress false \
  --rule-action allow
```

## VPN & Direct Connect

### Site-to-Site VPN
```
On-Premises ←VPN tunnel→ VGW (Virtual Private Gateway) ←→ VPC
```

### Direct Connect
Connessione dedicata fisica (1Gbps - 100Gbps)

## VPC Peering

```
VPC-A (10.0.0.0/16) ←Peering→ VPC-B (10.1.0.0/16)
```

```bash
# Create peering connection
aws ec2 create-vpc-peering-connection \
  --vpc-id vpc-aaaa \
  --peer-vpc-id vpc-bbbb

# Accept
aws ec2 accept-vpc-peering-connection \
  --vpc-peering-connection-id pcx-123456

# Update route tables
aws ec2 create-route \
  --route-table-id rtb-aaaa \
  --destination-cidr-block 10.1.0.0/16 \
  --vpc-peering-connection-id pcx-123456
```

## Best Practices
1. Use multiple AZs
2. Separate public/private subnets
3. Least privilege security groups
4. Enable VPC Flow Logs
5. Use NAT Gateways (not instances)
6. Plan CIDR carefully (no overlap)
