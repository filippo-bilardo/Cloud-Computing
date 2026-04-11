# Infrastructure as Code (IaC)

## Introduzione

L'**Infrastructure as Code** (IaC) è la pratica di gestire e provisioning dell'infrastruttura attraverso file di definizione leggibili dalla macchina, anziché attraverso configurazione manuale o strumenti interattivi.

### Vantaggi dell'IaC

- **Versionamento**: Infrastructure tracciata in Git come il codice
- **Riproducibilità**: Stesso codice → stessa infrastruttura
- **Automazione**: Deploy automatizzati, no errori manuali
- **Documentazione**: Il codice documenta l'infrastruttura
- **Testing**: Validazione e testing prima del deploy
- **Collaboration**: Code review e approval process
- **Rollback**: Ripristino rapido a versioni precedenti
- **Consistency**: Ambienti dev/staging/prod identici

### Approcci IaC

1. **Declarative** (Cosa): Terraform, CloudFormation, ARM
   - Si dichiara lo stato desiderato
   - Il tool calcola come raggiungerlo

2. **Imperative** (Come): Ansible, Scripts
   - Si specificano i passi esatti
   - Maggiore controllo, più complessità

3. **Hybrid**: Pulumi (codice imperativo, risultato dichiarativo)

---

## Terraform

**Terraform** di HashiCorp è uno strumento IaC multi-cloud open source che usa HCL (HashiCorp Configuration Language).

### Architettura Terraform

```
┌─────────────┐
│ .tf files   │  ← Configuration
└──────┬──────┘
       │
┌──────▼──────┐
│  Terraform  │
│    Core     │
└──────┬──────┘
       │
   ┌───┴────┐
   │ State  │  ← terraform.tfstate
   └───┬────┘
       │
┌──────▼──────────────────┐
│  Providers (AWS, Azure) │
└─────────────────────────┘
```

### Esempio Base: EC2 su AWS

```hcl
# provider.tf
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# variables.tf
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

# main.tf
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "main-vpc"
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "public-subnet"
  }
}

resource "aws_security_group" "web" {
  name        = "web-sg"
  description = "Allow HTTP/HTTPS inbound"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

resource "aws_instance" "web" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.web.id]

  user_data = <<-EOF
              #!/bin/bash
              apt-get update
              apt-get install -y nginx
              systemctl start nginx
              EOF

  tags = {
    Name = "web-server"
  }
}

# outputs.tf
output "instance_public_ip" {
  description = "Public IP of web server"
  value       = aws_instance.web.public_ip
}

output "vpc_id" {
  value = aws_vpc.main.id
}
```

### Comandi Terraform

```bash
# Inizializzazione (download providers)
terraform init

# Validazione sintassi
terraform validate

# Formattazione codice
terraform fmt

# Piano di esecuzione (dry-run)
terraform plan

# Applicare modifiche
terraform apply

# Applicare senza conferma
terraform apply -auto-approve

# Distruggere infrastruttura
terraform destroy

# Mostrare stato corrente
terraform show

# Elencare risorse gestite
terraform state list

# Refresh dello stato
terraform refresh

# Output di variabili
terraform output
```

### Terraform Modules

I **modules** permettono di riutilizzare configurazioni:

```hcl
# modules/vpc/main.tf
variable "vpc_cidr" {
  type = string
}

variable "vpc_name" {
  type = string
}

resource "aws_vpc" "this" {
  cidr_block = var.vpc_cidr
  
  tags = {
    Name = var.vpc_name
  }
}

output "vpc_id" {
  value = aws_vpc.this.id
}

# Root main.tf - uso del module
module "production_vpc" {
  source   = "./modules/vpc"
  vpc_cidr = "10.0.0.0/16"
  vpc_name = "production"
}

module "staging_vpc" {
  source   = "./modules/vpc"
  vpc_cidr = "10.1.0.0/16"
  vpc_name = "staging"
}

output "prod_vpc_id" {
  value = module.production_vpc.vpc_id
}
```

### Remote State e Backend

```hcl
# Backend S3 con lock DynamoDB
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}

# Configurazione workspace
terraform workspace new production
terraform workspace new staging
terraform workspace select production
```

---

## AWS CloudFormation

**CloudFormation** è il servizio IaC nativo di AWS che usa template YAML o JSON.

### Template CloudFormation: VPC + EC2

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'VPC with EC2 instance'

Parameters:
  InstanceType:
    Type: String
    Default: t3.micro
    AllowedValues:
      - t3.micro
      - t3.small
      - t3.medium
    Description: EC2 instance type

  LatestAmiId:
    Type: AWS::SSM::Parameter::Value<AWS::EC2::Image::Id>
    Default: /aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2

Mappings:
  RegionMap:
    eu-west-1:
      CIDR: 10.0.0.0/16
    us-east-1:
      CIDR: 10.1.0.0/16

Resources:
  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: !FindInMap [RegionMap, !Ref 'AWS::Region', CIDR]
      EnableDnsHostnames: true
      EnableDnsSupport: true
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-VPC'

  InternetGateway:
    Type: AWS::EC2::InternetGateway
    Properties:
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-IGW'

  AttachGateway:
    Type: AWS::EC2::VPCGatewayAttachment
    Properties:
      VpcId: !Ref VPC
      InternetGatewayId: !Ref InternetGateway

  PublicSubnet:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Select [0, !Cidr [!GetAtt VPC.CidrBlock, 4, 8]]
      AvailabilityZone: !Select [0, !GetAZs '']
      MapPublicIpOnLaunch: true
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-PublicSubnet'

  PublicRouteTable:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-PublicRT'

  PublicRoute:
    Type: AWS::EC2::Route
    DependsOn: AttachGateway
    Properties:
      RouteTableId: !Ref PublicRouteTable
      DestinationCidrBlock: 0.0.0.0/0
      GatewayId: !Ref InternetGateway

  SubnetRouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet
      RouteTableId: !Ref PublicRouteTable

  WebServerSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Allow HTTP/HTTPS
      VpcId: !Ref VPC
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 80
          ToPort: 80
          CidrIp: 0.0.0.0/0
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          CidrIp: 0.0.0.0/0
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-WebSG'

  WebServer:
    Type: AWS::EC2::Instance
    Properties:
      InstanceType: !Ref InstanceType
      ImageId: !Ref LatestAmiId
      SubnetId: !Ref PublicSubnet
      SecurityGroupIds:
        - !Ref WebServerSecurityGroup
      UserData:
        Fn::Base64: !Sub |
          #!/bin/bash
          yum update -y
          yum install -y httpd
          systemctl start httpd
          systemctl enable httpd
          echo "<h1>CloudFormation Stack: ${AWS::StackName}</h1>" > /var/www/html/index.html
      Tags:
        - Key: Name
          Value: !Sub '${AWS::StackName}-WebServer'

Outputs:
  VPCId:
    Description: VPC ID
    Value: !Ref VPC
    Export:
      Name: !Sub '${AWS::StackName}-VPCID'

  WebServerPublicIP:
    Description: Public IP address
    Value: !GetAtt WebServer.PublicIp

  WebServerURL:
    Description: Web Server URL
    Value: !Sub 'http://${WebServer.PublicDnsName}'
```

### Comandi AWS CLI per CloudFormation

```bash
# Validare template
aws cloudformation validate-template --template-body file://template.yaml

# Creare stack
aws cloudformation create-stack \
  --stack-name my-web-stack \
  --template-body file://template.yaml \
  --parameters ParameterKey=InstanceType,ParameterValue=t3.small

# Aggiornare stack
aws cloudformation update-stack \
  --stack-name my-web-stack \
  --template-body file://template.yaml

# Eliminare stack
aws cloudformation delete-stack --stack-name my-web-stack

# Descrivere stack
aws cloudformation describe-stacks --stack-name my-web-stack

# Eventi dello stack
aws cloudformation describe-stack-events --stack-name my-web-stack

# Risorse dello stack
aws cloudformation list-stack-resources --stack-name my-web-stack

# Drift detection (differenze tra stato desiderato e reale)
aws cloudformation detect-stack-drift --stack-name my-web-stack
```

---

## Azure Resource Manager (ARM) e Bicep

### ARM Template (JSON)

```json
{
  "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "vmSize": {
      "type": "string",
      "defaultValue": "Standard_B2s",
      "metadata": {
        "description": "VM size"
      }
    },
    "adminUsername": {
      "type": "string",
      "metadata": {
        "description": "Admin username"
      }
    },
    "adminPassword": {
      "type": "securestring",
      "metadata": {
        "description": "Admin password"
      }
    }
  },
  "variables": {
    "vnetName": "myVNet",
    "subnetName": "mySubnet",
    "nicName": "myNIC",
    "vmName": "myVM"
  },
  "resources": [
    {
      "type": "Microsoft.Network/virtualNetworks",
      "apiVersion": "2021-02-01",
      "name": "[variables('vnetName')]",
      "location": "[resourceGroup().location]",
      "properties": {
        "addressSpace": {
          "addressPrefixes": ["10.0.0.0/16"]
        },
        "subnets": [
          {
            "name": "[variables('subnetName')]",
            "properties": {
              "addressPrefix": "10.0.1.0/24"
            }
          }
        ]
      }
    }
  ],
  "outputs": {
    "vnetId": {
      "type": "string",
      "value": "[resourceId('Microsoft.Network/virtualNetworks', variables('vnetName'))]"
    }
  }
}
```

### Bicep (Sintassi Semplificata)

**Bicep** è un DSL per ARM che compila in JSON, più leggibile di ARM templates.

```bicep
// parameters.bicep
@description('Azure region')
param location string = resourceGroup().location

@description('VM size')
@allowed([
  'Standard_B2s'
  'Standard_D2s_v3'
  'Standard_D4s_v3'
])
param vmSize string = 'Standard_B2s'

@description('Admin username')
param adminUsername string

@secure()
@description('Admin password')
param adminPassword string

// Variables
var vnetName = 'myVNet'
var subnetName = 'mySubnet'
var nicName = 'myNIC'
var vmName = 'myVM'
var nsgName = 'myNSG'
var publicIPName = 'myPublicIP'

// Network Security Group
resource nsg 'Microsoft.Network/networkSecurityGroups@2021-02-01' = {
  name: nsgName
  location: location
  properties: {
    securityRules: [
      {
        name: 'AllowSSH'
        properties: {
          priority: 1000
          protocol: 'Tcp'
          access: 'Allow'
          direction: 'Inbound'
          sourceAddressPrefix: '*'
          sourcePortRange: '*'
          destinationAddressPrefix: '*'
          destinationPortRange: '22'
        }
      }
      {
        name: 'AllowHTTP'
        properties: {
          priority: 1001
          protocol: 'Tcp'
          access: 'Allow'
          direction: 'Inbound'
          sourceAddressPrefix: '*'
          sourcePortRange: '*'
          destinationAddressPrefix: '*'
          destinationPortRange: '80'
        }
      }
    ]
  }
}

// Virtual Network
resource vnet 'Microsoft.Network/virtualNetworks@2021-02-01' = {
  name: vnetName
  location: location
  properties: {
    addressSpace: {
      addressPrefixes: [
        '10.0.0.0/16'
      ]
    }
    subnets: [
      {
        name: subnetName
        properties: {
          addressPrefix: '10.0.1.0/24'
          networkSecurityGroup: {
            id: nsg.id
          }
        }
      }
    ]
  }
}

// Public IP
resource publicIP 'Microsoft.Network/publicIPAddresses@2021-02-01' = {
  name: publicIPName
  location: location
  sku: {
    name: 'Standard'
  }
  properties: {
    publicIPAllocationMethod: 'Static'
    dnsSettings: {
      domainNameLabel: toLower('${vmName}-${uniqueString(resourceGroup().id)}')
    }
  }
}

// Network Interface
resource nic 'Microsoft.Network/networkInterfaces@2021-02-01' = {
  name: nicName
  location: location
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: {
            id: vnet.properties.subnets[0].id
          }
          privateIPAllocationMethod: 'Dynamic'
          publicIPAddress: {
            id: publicIP.id
          }
        }
      }
    ]
  }
}

// Virtual Machine
resource vm 'Microsoft.Compute/virtualMachines@2021-03-01' = {
  name: vmName
  location: location
  properties: {
    hardwareProfile: {
      vmSize: vmSize
    }
    osProfile: {
      computerName: vmName
      adminUsername: adminUsername
      adminPassword: adminPassword
    }
    storageProfile: {
      imageReference: {
        publisher: 'Canonical'
        offer: 'UbuntuServer'
        sku: '18.04-LTS'
        version: 'latest'
      }
      osDisk: {
        createOption: 'FromImage'
        managedDisk: {
          storageAccountType: 'Premium_LRS'
        }
      }
    }
    networkProfile: {
      networkInterfaces: [
        {
          id: nic.id
        }
      ]
    }
  }
}

// Outputs
output vmId string = vm.id
output publicIP string = publicIP.properties.ipAddress
output fqdn string = publicIP.properties.dnsSettings.fqdn
```

### Comandi Bicep e Azure CLI

```bash
# Installare Bicep
az bicep install

# Compilare Bicep → ARM JSON
az bicep build --file main.bicep

# Deploy diretto di Bicep
az deployment group create \
  --resource-group myResourceGroup \
  --template-file main.bicep \
  --parameters adminUsername=azureuser adminPassword='P@ssw0rd123!'

# What-if (dry-run)
az deployment group what-if \
  --resource-group myResourceGroup \
  --template-file main.bicep

# Validare template
az deployment group validate \
  --resource-group myResourceGroup \
  --template-file main.bicep

# Elencare deployment
az deployment group list --resource-group myResourceGroup

# Eliminare deployment
az deployment group delete \
  --resource-group myResourceGroup \
  --name deploymentName
```

---

## Ansible

**Ansible** è uno strumento di configuration management e orchestration agentless (usa SSH).

### Ansible Playbook: Setup Web Server

```yaml
# inventory.ini
[webservers]
web1 ansible_host=192.168.1.10 ansible_user=ubuntu
web2 ansible_host=192.168.1.11 ansible_user=ubuntu

[databases]
db1 ansible_host=192.168.1.20 ansible_user=ubuntu

[all:vars]
ansible_python_interpreter=/usr/bin/python3

# playbook.yml
---
- name: Configure web servers
  hosts: webservers
  become: yes
  vars:
    http_port: 80
    app_name: myapp

  tasks:
    - name: Update apt cache
      apt:
        update_cache: yes
        cache_valid_time: 3600

    - name: Install Nginx
      apt:
        name: nginx
        state: present

    - name: Install Python packages
      apt:
        name:
          - python3-pip
          - python3-venv
        state: present

    - name: Create app directory
      file:
        path: "/var/www/{{ app_name }}"
        state: directory
        owner: www-data
        group: www-data
        mode: '0755'

    - name: Copy application files
      copy:
        src: ./app/
        dest: "/var/www/{{ app_name }}/"
        owner: www-data
        group: www-data

    - name: Template Nginx config
      template:
        src: templates/nginx.conf.j2
        dest: /etc/nginx/sites-available/{{ app_name }}
      notify: Reload Nginx

    - name: Enable site
      file:
        src: /etc/nginx/sites-available/{{ app_name }}
        dest: /etc/nginx/sites-enabled/{{ app_name }}
        state: link
      notify: Reload Nginx

    - name: Ensure Nginx is running
      service:
        name: nginx
        state: started
        enabled: yes

  handlers:
    - name: Reload Nginx
      service:
        name: nginx
        state: reloaded

# templates/nginx.conf.j2
server {
    listen {{ http_port }};
    server_name {{ ansible_fqdn }};

    root /var/www/{{ app_name }};
    index index.html;

    location / {
        try_files $uri $uri/ =404;
    }

    access_log /var/log/nginx/{{ app_name }}_access.log;
    error_log /var/log/nginx/{{ app_name }}_error.log;
}
```

### Ansible per Provisioning AWS

```yaml
# aws-provision.yml
---
- name: Provision AWS infrastructure
  hosts: localhost
  gather_facts: no
  vars:
    region: eu-west-1
    vpc_cidr: 10.0.0.0/16
    subnet_cidr: 10.0.1.0/24

  tasks:
    - name: Create VPC
      amazon.aws.ec2_vpc_net:
        name: ansible-vpc
        cidr_block: "{{ vpc_cidr }}"
        region: "{{ region }}"
        tags:
          Environment: production
      register: vpc

    - name: Create subnet
      amazon.aws.ec2_vpc_subnet:
        vpc_id: "{{ vpc.vpc.id }}"
        cidr: "{{ subnet_cidr }}"
        region: "{{ region }}"
        tags:
          Name: ansible-subnet
      register: subnet

    - name: Create security group
      amazon.aws.ec2_security_group:
        name: web-sg
        description: Allow HTTP/HTTPS
        vpc_id: "{{ vpc.vpc.id }}"
        region: "{{ region }}"
        rules:
          - proto: tcp
            ports: [80, 443]
            cidr_ip: 0.0.0.0/0
      register: sg

    - name: Launch EC2 instance
      amazon.aws.ec2_instance:
        name: web-server
        instance_type: t3.micro
        image_id: ami-0c55b159cbfafe1f0  # Amazon Linux 2
        region: "{{ region }}"
        vpc_subnet_id: "{{ subnet.subnet.id }}"
        security_group: "{{ sg.group_id }}"
        key_name: my-key
        wait: yes
        tags:
          Environment: production
      register: ec2
```

### Comandi Ansible

```bash
# Eseguire playbook
ansible-playbook -i inventory.ini playbook.yml

# Check mode (dry-run)
ansible-playbook -i inventory.ini playbook.yml --check

# Con verbose
ansible-playbook -i inventory.ini playbook.yml -vvv

# Eseguire task specifici (tags)
ansible-playbook playbook.yml --tags "nginx"

# Ad-hoc commands
ansible webservers -i inventory.ini -m ping
ansible webservers -i inventory.ini -m shell -a "uptime"
ansible webservers -i inventory.ini -m apt -a "name=nginx state=latest" --become
```

---

## Pulumi

**Pulumi** permette di usare linguaggi di programmazione reali (TypeScript, Python, Go, C#) per definire infrastruttura.

### Esempio Pulumi (TypeScript): AWS VPC + EC2

```typescript
// index.ts
import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

// Configurazione
const config = new pulumi.Config();
const instanceType = config.get("instanceType") || "t3.micro";

// VPC
const vpc = new aws.ec2.Vpc("main-vpc", {
    cidrBlock: "10.0.0.0/16",
    enableDnsHostnames: true,
    enableDnsSupport: true,
    tags: {
        Name: "main-vpc",
    },
});

// Internet Gateway
const igw = new aws.ec2.InternetGateway("main-igw", {
    vpcId: vpc.id,
    tags: {
        Name: "main-igw",
    },
});

// Public Subnet
const publicSubnet = new aws.ec2.Subnet("public-subnet", {
    vpcId: vpc.id,
    cidrBlock: "10.0.1.0/24",
    availabilityZone: "eu-west-1a",
    mapPublicIpOnLaunch: true,
    tags: {
        Name: "public-subnet",
    },
});

// Route Table
const publicRouteTable = new aws.ec2.RouteTable("public-rt", {
    vpcId: vpc.id,
    routes: [{
        cidrBlock: "0.0.0.0/0",
        gatewayId: igw.id,
    }],
    tags: {
        Name: "public-rt",
    },
});

const rtAssociation = new aws.ec2.RouteTableAssociation("rt-association", {
    subnetId: publicSubnet.id,
    routeTableId: publicRouteTable.id,
});

// Security Group
const webSecurityGroup = new aws.ec2.SecurityGroup("web-sg", {
    vpcId: vpc.id,
    description: "Allow HTTP/HTTPS inbound",
    ingress: [
        { protocol: "tcp", fromPort: 80, toPort: 80, cidrBlocks: ["0.0.0.0/0"] },
        { protocol: "tcp", fromPort: 443, toPort: 443, cidrBlocks: ["0.0.0.0/0"] },
        { protocol: "tcp", fromPort: 22, toPort: 22, cidrBlocks: ["0.0.0.0/0"] },
    ],
    egress: [{
        protocol: "-1",
        fromPort: 0,
        toPort: 0,
        cidrBlocks: ["0.0.0.0/0"],
    }],
});

// Get latest Ubuntu AMI
const ami = aws.ec2.getAmi({
    mostRecent: true,
    owners: ["099720109477"], // Canonical
    filters: [{
        name: "name",
        values: ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"],
    }],
});

// User data script
const userData = `#!/bin/bash
apt-get update
apt-get install -y nginx
systemctl start nginx
echo "<h1>Deployed with Pulumi</h1>" > /var/www/html/index.html
`;

// EC2 Instance
const webServer = new aws.ec2.Instance("web-server", {
    instanceType: instanceType,
    ami: ami.then(a => a.id),
    subnetId: publicSubnet.id,
    vpcSecurityGroupIds: [webSecurityGroup.id],
    userData: userData,
    tags: {
        Name: "web-server",
    },
});

// Exports
export const vpcId = vpc.id;
export const publicIp = webServer.publicIp;
export const url = pulumi.interpolate`http://${webServer.publicDnsName}`;
```

### Pulumi Python Example

```python
# __main__.py
import pulumi
import pulumi_aws as aws

# Configurazione
config = pulumi.Config()
instance_type = config.get("instanceType") or "t3.micro"

# VPC
vpc = aws.ec2.Vpc("main-vpc",
    cidr_block="10.0.0.0/16",
    enable_dns_hostnames=True,
    enable_dns_support=True,
    tags={"Name": "main-vpc"}
)

# Subnet
subnet = aws.ec2.Subnet("public-subnet",
    vpc_id=vpc.id,
    cidr_block="10.0.1.0/24",
    availability_zone="eu-west-1a",
    map_public_ip_on_launch=True,
    tags={"Name": "public-subnet"}
)

# Security Group
sg = aws.ec2.SecurityGroup("web-sg",
    vpc_id=vpc.id,
    description="Allow HTTP/HTTPS",
    ingress=[
        {"protocol": "tcp", "from_port": 80, "to_port": 80, "cidr_blocks": ["0.0.0.0/0"]},
        {"protocol": "tcp", "from_port": 443, "to_port": 443, "cidr_blocks": ["0.0.0.0/0"]},
    ],
    egress=[{"protocol": "-1", "from_port": 0, "to_port": 0, "cidr_blocks": ["0.0.0.0/0"]}]
)

# AMI
ami = aws.ec2.get_ami(
    most_recent=True,
    owners=["099720109477"],
    filters=[{"name": "name", "values": ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-*"]}]
)

# EC2
instance = aws.ec2.Instance("web-server",
    instance_type=instance_type,
    ami=ami.id,
    subnet_id=subnet.id,
    vpc_security_group_ids=[sg.id],
    user_data="""#!/bin/bash
    apt-get update
    apt-get install -y nginx
    """,
    tags={"Name": "web-server"}
)

# Outputs
pulumi.export("vpc_id", vpc.id)
pulumi.export("public_ip", instance.public_ip)
```

### Comandi Pulumi

```bash
# Creare nuovo progetto
pulumi new aws-typescript
pulumi new aws-python

# Preview (simile a terraform plan)
pulumi preview

# Deploy
pulumi up

# Distruggere stack
pulumi destroy

# Visualizzare outputs
pulumi stack output

# Refresh dello stato
pulumi refresh

# Elencare stack
pulumi stack ls

# Configurazione
pulumi config set aws:region eu-west-1
pulumi config set instanceType t3.small --secret
```

---

## Best Practices IaC

### 1. Versionamento e Git

```bash
# .gitignore per Terraform
.terraform/
*.tfstate
*.tfstate.backup
.terraform.lock.hcl
*.tfvars  # se contengono secrets

# .gitignore per Ansible
*.retry
*.log
vault-password.txt

# Usa Git Tags per releases
git tag -a v1.0.0 -m "Production release 1.0.0"
git push origin v1.0.0
```

### 2. Gestione Secrets

```bash
# Terraform: usa variabili sensibili
variable "db_password" {
  type      = string
  sensitive = true
}

# O meglio: secret manager
data "aws_secretsmanager_secret_version" "db_password" {
  secret_id = "prod/db/password"
}

# Ansible Vault
ansible-vault create secrets.yml
ansible-vault encrypt vars/production.yml
ansible-playbook playbook.yml --ask-vault-pass

# Pulumi Config Secrets
pulumi config set --secret dbPassword 'myP@ssw0rd'
```

### 3. Modularizzazione

```
infrastructure/
├── modules/
│   ├── vpc/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ec2/
│   └── rds/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   └── production/
└── README.md
```

### 4. Testing e Validation

```bash
# Terraform
terraform fmt -check
terraform validate
tflint  # https://github.com/terraform-linters/tflint

# Terratest (Go)
cd test
go test -v -timeout 30m

# Checkov (security scanning)
checkov -d .

# Ansible
ansible-playbook playbook.yml --syntax-check
ansible-lint playbook.yml
```

### 5. CI/CD per IaC

```yaml
# .github/workflows/terraform.yml
name: Terraform CI

on:
  pull_request:
    paths:
      - 'terraform/**'

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2
        with:
          terraform_version: 1.5.0
      
      - name: Terraform Init
        run: terraform init
        working-directory: ./terraform
      
      - name: Terraform Validate
        run: terraform validate
        working-directory: ./terraform
      
      - name: Terraform Plan
        run: terraform plan -no-color
        working-directory: ./terraform
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
      
      - name: Run Checkov
        uses: bridgecrewio/checkov-action@master
        with:
          directory: terraform/
          framework: terraform
```

### 6. State Management

- **Remote State**: Mai committare `.tfstate` in Git
- **Locking**: Usa DynamoDB (AWS) o equivalenti per evitare race conditions
- **Backup**: State backup automatici
- **Encryption**: State sempre cifrato at-rest

### 7. Naming Conventions

```hcl
# Consistente, descrittivo
resource "aws_instance" "web_server" {  # NON "i", "instance", "server"
  tags = {
    Name        = "${var.environment}-web-server-${count.index + 1}"
    Environment = var.environment
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}
```

---

## Esercizi

1. **Terraform Multi-Tier App**: Crea VPC, subnets public/private, ALB, ASG, RDS
2. **CloudFormation Nested Stacks**: Stack modulari riutilizzabili
3. **Bicep Deployment**: VM Windows con SQL Server usando Bicep
4. **Ansible Dynamic Inventory**: Inventory dinamico da AWS EC2
5. **Pulumi Stack References**: Condividere outputs tra stack diversi
6. **IaC Testing**: Implementa test con Terratest o Pulumi test framework

---

## Domande di Verifica

1. Qual è la differenza tra approccio dichiarativo e imperativo in IaC?
2. Come funziona lo state in Terraform e perché è importante?
3. Quali sono i vantaggi di CloudFormation rispetto a Terraform su AWS?
4. Quando preferire Bicep rispetto a ARM templates JSON?
5. Come gestisci secrets in Terraform in modo sicuro?
6. Qual è lo scopo dei modules in Terraform?
7. Come implementi ambienti multipli (dev/staging/prod) con IaC?
8. Cosa sono gli Ansible handlers e quando si usano?
9. Qual è il vantaggio di Pulumi rispetto a Terraform?
10. Come testi il codice IaC prima del deploy in produzione?

---

## Risorse Aggiuntive

- [Terraform Documentation](https://www.terraform.io/docs)
- [AWS CloudFormation User Guide](https://docs.aws.amazon.com/cloudformation/)
- [Bicep Documentation](https://learn.microsoft.com/en-us/azure/azure-resource-manager/bicep/)
- [Ansible Documentation](https://docs.ansible.com/)
- [Pulumi Documentation](https://www.pulumi.com/docs/)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)
- [Infrastructure as Code Book - Kief Morris](https://infrastructure-as-code.com/)
