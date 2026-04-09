# 2.7 Virtualizzazione nel Cloud

## Indice
- [Come i Cloud Provider Usano la Virtualizzazione](#come-i-cloud-provider-usano-la-virtualizzazione)
- [Nested Virtualization](#nested-virtualization)
- [Hardware-Assisted Virtualization](#hardware-assisted-virtualization)
- [VM su AWS (EC2), Azure, GCP](#vm-su-aws-ec2-azure-gcp)
- [Bare-Metal Instances](#bare-metal-instances)

---

## Come i Cloud Provider Usano la Virtualizzazione

### Architettura Cloud Multi-Tenant

```
Physical Server (Cloud Provider Datacenter)
┌─────────────────────────────────────────┐
│         Hypervisor Layer                │
│         (Xen, KVM, Hyper-V)             │
└─────────┬───────────────────────────────┘
          │
    ┌─────┼───────┬───────┬───────┐
    │     │       │       │       │
┌───▼──┐┌─▼────┐┌─▼────┐┌─▼────┐┌─▼────┐
│Tenant││Tenant││Tenant││Tenant││Tenant│
│  A   ││  B   ││  C   ││  D   ││  E   │
│ VM1  ││ VM1  ││ VM1  ││ VM1  ││ VM1  │
└──────┘└──────┘└──────┘└──────┘└──────┘

Isolation, security, performance per tenant
```

### Evolution of Cloud Virtualization

#### Prima Generazione: Full Virtualization

**AWS EC2 (2006-2017): Xen-based**
```
┌────────────────────────────────────┐
│   Xen Hypervisor                   │
│   ┌──────────────────────────┐     │
│   │   Dom0 (Control Domain)  │     │
│   │   - Hardware drivers     │     │
│   │   - Toolstack            │     │
│   └──────────────────────────┘     │
│   ┌──────┐ ┌──────┐ ┌──────┐       │
│   │DomU 1│ │DomU 2│ │DomU 3│       │
│   │(VM)  │ │(VM)  │ │(VM)  │       │
│   └──────┘ └──────┘ └──────┘       │
└────────────────────────────────────┘

Characteristics:
- Paravirtualization for best performance
- HVM mode for Windows
- Overhead: ~5-10%
```

**Google Compute Engine: KVM-based**
```
┌────────────────────────────────────┐
│   KVM (Kernel-based)               │
│   ┌──────────────────────────┐     │
│   │   Host Linux Kernel      │     │
│   │   + KVM module          │     │
│   └──────────────────────────┘     │
│   ┌──────┐ ┌──────┐ ┌──────┐      │
│   │ VM 1 │ │ VM 2 │ │ VM 3 │      │
│   │(QEMU)│ │(QEMU)│ │(QEMU)│      │
│   └──────┘ └──────┘ └──────┘      │
└────────────────────────────────────┘

Advantages:
- Open source
- Mainline Linux kernel
- Performance near-native
```

#### Seconda Generazione: Hardware Virtualization Offload

**AWS Nitro System (2017+)**
```
┌─────────────────────────────────────────┐
│   Nitro Hypervisor (lightweight)       │
│   - No Dom0                             │
│   - Minimal CPU overhead                │
└──────────────┬──────────────────────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───▼─────────────┐  ┌────▼────────────┐
│  Customer VMs   │  │ Nitro Cards     │
│                 │  │ (dedicated HW)  │
│  - Full CPU     │  │ - Storage       │
│  - Full RAM     │  │ - Network       │
│  - Near bare-   │  │ - Security      │
│    metal perf   │  │ - Management    │
└─────────────────┘  └─────────────────┘

Performance: 99%+ of bare-metal
Overhead: <1%
```

**Azure Hypervisor Evolution**
```
Traditional Hyper-V:
┌──────────────────────────────────┐
│  Root Partition (Parent OS)     │
│  - Drivers, Management           │
└───────┬──────────────────────────┘
        │
┌───────▼──────────┐
│ Hypervisor       │
└──────────────────┘

Modern Azure:
┌──────────────────────────────────┐
│  Lightweight Hypervisor          │
│  - FPGA offload                  │
│  - SmartNIC (network offload)    │
└──────────────────────────────────┘

Result: More resources for customer VMs
```

### Customized Linux Kernels

Cloud providers optimize Linux:

**AWS Bottlerocket**
```
Purpose-built Linux for containers:
- Minimal OS (~50MB)
- Automatic updates (via A/B partitions)
- dm-verity for immutability
- cgroups v2
- No SSH by default (admin container)

Use case: ECS, EKS host OS
```

**Google Container-Optimized OS**
```
Hardened Chromium OS based:
- Docker pre-installed
- Automatic updates
- Minimal attack surface
- Read-only root filesystem

Use case: GKE node OS
```

**Azure Linux (CBL-Mariner)**
```
Microsoft's own Linux distribution:
- Lightweight
- Security-focused
- Optimized for Azure
- Open source

Use case: Azure internal services, AKS
```

---

## Nested Virtualization

**Nested virtualization**: Eseguire VM dentro VM (hypervisor inside hypervisor).

### Architettura Nested

```
┌──────────────────────────────────────────┐
│   Physical Host (L0 Hypervisor)         │
│   └──────────────────────────────────────│
│         ┌────────────────────────────┐   │
│         │  L1 VM (Guest)             │   │
│         │  ┌──────────────────────┐  │   │
│         │  │ L1 Hypervisor        │  │   │
│         │  │ (ESXi/KVM/Hyper-V)  │  │   │
│         │  └────┬─────────────────┘  │   │
│         │       │                    │   │
│         │  ┌────▼──────┐  ┌──────┐  │   │
│         │  │ L2 VM 1   │  │L2 VM2│  │   │
│         │  │ (nested)  │  │      │  │   │
│         │  └───────────┘  └──────┘  │   │
│         └────────────────────────────┘   │
└──────────────────────────────────────────┘

Levels:
L0 = Physical host hypervisor
L1 = First-level VM (can be hypervisor)
L2 = Nested VM
```

### Use Cases Nested Virtualization

#### 1. Development & Testing
```
Developer Laptop:
└─ VirtualBox (L0)
   └─ Ubuntu VM (L1)
      └─ KVM (L1 hypervisor)
         └─ Test VMs (L2)

Benefit: Test hypervisor configs without physical hardware
```

#### 2. Cloud-Based Lab Environments
```
AWS EC2 Instance:
└─ Nitro hypervisor (L0)
   └─ Ubuntu VM with nested virt enabled (L1)
      └─ VMware ESXi (L1)
         └─ Windows VMs (L2)

Benefit: VMware training/testing in cloud
```

#### 3. Kubernetes on VMs
```
GCP Compute Engine:
└─ KVM (L0)
   └─ VM running Docker (L1)
      └─ Container (light virtualization) (L2)
```

### Enabling Nested Virtualization

#### AWS EC2
```bash
# Only specific instance types support nested virt:
# - .metal instances (bare-metal)
# - Some instances with specific AMIs

# Check if nested virt is enabled (in VM)
cat /sys/module/kvm_intel/parameters/nested
# Y = enabled

# Not generally available for regular instances
# Use .metal instances for full nested support
```

#### Azure
```powershell
# Supported on:
# - Dv3, Ev3, Dav4, Eav4 series

# Enable nested virtualization (must be done when VM is stopped)
Stop-AzVM -ResourceGroupName "MyRG" -Name "MyVM"
Set-AzVMProcessor -ResourceGroupName "MyRG" -VMName "MyVM" -ExposeVirtualizationExtensions $true
Start-AzVM -ResourceGroupName "MyRG" -Name "MyVM"

# Verify in VM (PowerShell)
Get-VMProcessor
# ExposeVirtualizationExtensions: True
```

#### GCP
```bash
# Enable nested virtualization on instance
gcloud compute instances create nested-vm \
    --zone=us-central1-a \
    --min-cpu-platform="Intel Haswell" \
    --image-family=ubuntu-2004-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=200GB \
    --boot-disk-type=pd-standard \
    --machine-type=n1-standard-4 \
    --enable-nested-virtualization

# Or enable on existing disk
gcloud compute images create nested-vm-image \
  --source-disk=source-disk \
  --source-disk-zone=us-central1-a \
  --licenses="https://www.googleapis.com/compute/v1/projects/vm-options/global/licenses/enable-vmx"

# Verify
grep -E 'vmx|svm' /proc/cpuinfo
```

#### Local (VirtualBox/VMware)

**VirtualBox:**
```bash
# Enable nested VT-x/AMD-V
VBoxManage modifyvm "VM-Name" --nested-hw-virt on

# Verify
VBoxManage showvminfo "VM-Name" | grep "Nested Paging"
```

**VMware Workstation:**
```
# Edit .vmx file:
vhv.enable = "TRUE"
vvt.enable = "TRUE"

# Or via GUI:
VM → Settings → Processors
☑ Virtualize Intel VT-x/EPT or AMD-V/RVI
☑ Virtualize CPU performance counters
```

### Performance Impact

```
Performance Comparison:
Native (L0):           100%
L1 VM:                 95-98%
L2 VM (nested):        70-85%
L3+ VM:                40-60% (highly discouraged)

Overhead sources:
- Double memory translation (NPT/EPT)
- I/O overhead
- Interrupt handling
```

### Best Practices Nested Virtualization

✅ **Use for**: Development, testing, training  
✅ **Enable CPU features**: VT-x/AMD-V, EPT/NPT  
✅ **Allocate sufficient resources**: L1 VM needs enough for all L2 VMs  
✅ **Monitor performance**: Nested VMs are slower  
❌ **Don't use for production**: Performance penalty too high  
❌ **Avoid L3+**: Extreme overhead  

---

## Hardware-Assisted Virtualization

### CPU Virtualization Extensions

#### Intel VT-x (Virtualization Technology)

**Features:**
```
VMX (Virtual Machine Extensions):
├─ VMX root mode: Hypervisor
└─ VMX non-root mode: Guest

EPT (Extended Page Tables):
- Hardware-assisted memory virtualization
- Guest physical → Host physical translation
- Eliminates shadow page tables
- ~40% performance improvement

VPID (Virtual Processor ID):
- Tags TLB entries with VM ID
- Reduces TLB flushes on VM switches
- Improved context-switch performance
```

**CPU Instructions:**
```assembly
; Enter VM (VMX non-root mode)
VMLAUNCH    ; First time
VMRESUME    ; Subsequent times

; Exit VM (to VMX root mode)
; Automatically on certain events:
- I/O access
- Exception
- Interrupt
- VMCALL (hypercall from guest)

; Guest wants privileged operation
VMCALL      ; Hypercall to hypervisor
```

#### AMD-V (AMD Virtualization)

**Features:**
```
SVM (Secure Virtual Machine):
├─ Host mode: Hypervisor
└─ Guest mode: VM

NPT (Nested Page Tables):
- Same concept as Intel EPT
- Two-dimensional page table walking
- Hardware guest→host translation

ASID (Address Space ID):
- Equivalent to Intel VPID
- TLB tagging per VM
```

**Comparison:**

| Feature | Intel VT-x | AMD-V |
|---------|------------|-------|
| **Base tech** | VMX | SVM |
| **Memory virt** | EPT | NPT |
| **TLB tagging** | VPID | ASID |
| **Performance** | Excellent | Excellent |
| **Availability** | 2006+ CPUs | 2006+ CPUs |

### I/O Virtualization

#### Intel VT-d (Directed I/O)

**IOMMU (I/O Memory Management Unit)**
```
Scenario: VM directly accessing device

Without VT-d:
VM → Hypervisor → Device
     (software emulation, slow)

With VT-d:
VM → Device (direct, fast)
     IOMMU enforces isolation

Benefits:
- PCI passthrough (GPU, NIC, storage)
- DMA remapping (security)
- Interrupt remapping
```

**Use Cases:**
1. **GPU Passthrough**: Gaming VMs, ML training
2. **SR-IOV NICs**: High-performance networking
3. **Direct-Attached Storage**: NVMe passthrough

**Configuration (KVM):**
```xml
<!-- PCI device passthrough -->
<hostdev mode='subsystem' type='pci' managed='yes'>
  <source>
    <address domain='0x0000' bus='0x01' slot='0x00' function='0x0'/>
  </source>
</hostdev>
```

```bash
# Enable IOMMU in GRUB
# /etc/default/grub
GRUB_CMDLINE_LINUX="intel_iommu=on"

# Update GRUB
update-grub
reboot

# Verify
dmesg | grep -i iommu
```

#### AMD-Vi (I/O Virtualization)

Equivalent to VT-d:
```
Features:
- IOMMU (same concept)
- Device assignment
- DMA protection
- Interrupt remapping

Enable in BIOS:
AMD-Vi / IOMMU: [Enabled]
```

### SR-IOV (Single Root I/O Virtualization)

**Concept**: Physical device presents as multiple virtual devices

```
Physical NIC (e.g., Intel X710 10GbE)
├─ PF (Physical Function)     ← Managed by host
│  - Configuration
│  - Monitoring
└─ VFs (Virtual Functions)    ← Assigned to VMs
   ├─ VF0 → VM1 (direct access, no hypervisor)
   ├─ VF1 → VM2
   ├─ VF2 → VM3
   └─ VF3 → VM4

Each VF:
- Own MAC address
- Own VLAN
- Direct hardware access
- Near line-rate performance
```

**Benefits:**
```
Traditional (emulated NIC):
VM → vSwitch → Physical NIC
     ↑ CPU overhead, latency

SR-IOV (VF passthrough):
VM → VF → Physical NIC
     ↓ Direct, no overhead

Results:
- Latency: ~1-2 μs (vs ~20 μs emulated)
- Throughput: 10 Gbps+ per VM
- CPU usage: Minimal
```

**Limitations:**
```
❌ No VM migration (VF tied to physical NIC)
❌ Limited number of VFs (typically 32-128)
❌ Requires NIC, hypervisor, guest support
```

**Configuration (VMware):**
```
1. Enable SR-IOV in BIOS
2. Configure NIC for SR-IOV (esxcli)
3. Add VFs to VM (vmx):
   sriov.numVFsEnabled = "TRUE"
4. Install drivers in guest
```

**Configuration (KVM):**
```xml
<interface type='hostdev' managed='yes'>
  <source>
    <address type='pci' domain='0x0000' bus='0x05' slot='0x10' function='0x0'/>
  </source>
</interface>
```

```bash
# Enable VFs on NIC
echo 8 > /sys/class/net/eth0/device/sriov_numvfs

# List VFs
lspci | grep Virtual
```

---

## VM su AWS (EC2), Azure, GCP

### AWS EC2

#### Instance Types

```
General Purpose (T, M series):
├─ t3.micro:  2 vCPU, 1 GB RAM    ($0.0104/h)
├─ t3.medium: 2 vCPU, 4 GB RAM    ($0.0416/h)
└─ m6i.large: 2 vCPU, 8 GB RAM    ($0.096/h)

Compute Optimized (C series):
└─ c6i.xlarge: 4 vCPU, 8 GB RAM   ($0.17/h)

Memory Optimized (R, X series):
├─ r6i.xlarge: 4 vCPU, 32 GB RAM  ($0.252/h)
└─ x2iedn.xlarge: 4 vCPU, 128 GB  ($1.00/h)

Storage Optimized (I, D series):
└─ i3.xlarge: 4 vCPU, 30 GB RAM, 950 GB NVMe SSD

GPU Instances (P, G series):
├─ p3.2xlarge: 8 vCPU, 61 GB, V100 GPU  ($3.06/h)
└─ g4dn.xlarge: 4 vCPU, 16 GB, T4 GPU   ($0.526/h)
```

#### Launch Instance (AWS CLI)

```bash
# Launch EC2 instance
aws ec2 run-instances \
    --image-id ami-0c55b159cbfafe1f0 \
    --count 1 \
    --instance-type t3.medium \
    --key-name MyKeyPair \
    --security-group-ids sg-903004f8 \
    --subnet-id subnet-6e7f829e \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=MyWebServer}]'

# List instances
aws ec2 describe-instances \
    --filters "Name=instance-state-name,Values=running" \
    --query "Reservations[].Instances[].[InstanceId,InstanceType,PublicIpAddress,State.Name]" \
    --output table

# Connect
ssh -i MyKeyPair.pem ec2-user@<public-ip>
```

#### Nitro System

```
Components:
1. Nitro Hypervisor
   - Lightweight, based on KVM
   - <1% overhead

2. Nitro Cards (dedicated hardware)
   ├─ Nitro Card for VPC: Networking
   ├─ Nitro Card for EBS: Storage
   ├─ Nitro Card for Security: Encryption
   └─ Nitro Controller: Management

3. Nitro Security Chip
   - Hardware root of trust
   - Firmware verification
   - No persistent storage

Benefits:
- More resources for customer
- Enhanced security
- Bare-metal performance
- Support for .metal instances
```

### Microsoft Azure Virtual Machines

#### VM Sizes

```
General Purpose (B, D series):
├─ B2s:  2 vCPU, 4 GB RAM         ($0.048/h)
├─ D2s_v5: 2 vCPU, 8 GB RAM       ($0.096/h)
└─ D4s_v5: 4 vCPU, 16 GB RAM      ($0.192/h)

Compute Optimized (F series):
└─ F4s_v2: 4 vCPU, 8 GB RAM       ($0.169/h)

Memory Optimized (E series):
├─ E4s_v5: 4 vCPU, 32 GB RAM      ($0.252/h)
└─ M128ms: 128 vCPU, 3.8 TB RAM   ($48.6/h)

GPU (N series):
├─ NC6: 6 vCPU, 56 GB, K80 GPU    ($0.90/h)
└─ ND40rs_v2: 40 vCPU, 672 GB, 8×V100  ($22.03/h)
```

#### Create VM (Azure CLI)

```bash
# Create resource group
az group create --name MyResourceGroup --location eastus

# Create VM
az vm create \
  --resource-group MyResourceGroup \
  --name MyVM \
  --image UbuntuLTS \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys \
  --public-ip-sku Standard

# Open port 80
az vm open-port --port 80 --resource-group MyResourceGroup --name MyVM

# Get IP
az vm show -d -g MyResourceGroup -n MyVM --query publicIps -o tsv

# Connect
ssh azureuser@<public-ip>

# List VMs
az vm list -o table
```

#### Azure Hypervisor

```
Based on: Microsoft Hyper-V

Custom optimizations:
├─ FPGA acceleration (Bing, Azure networking)
├─ SmartNIC (network offload)
├─ Custom firmware
└─ Hardware security (secure boot, encryption)

Features:
- Live migration
- Rapid provisioning (<1 min)
- Isolated compute for confidential VMs (AMD SEV)
```

### Google Cloud Platform (GCP)

#### Machine Types

```
General Purpose (E2, N series):
├─ e2-micro:  2 vCPU shared, 1 GB   ($0.0084/h)
├─ e2-medium: 2 vCPU shared, 4 GB   ($0.0335/h)
├─ n2-standard-4: 4 vCPU, 16 GB     ($0.194/h)
└─ n2-highmem-4: 4 vCPU, 32 GB      ($0.261/h)

Compute Optimized (C2):
└─ c2-standard-8: 8 vCPU, 32 GB     ($0.43/h)

Memory Optimized (M2):
└─ m2-ultramem-208: 208 vCPU, 5.7TB ($40.74/h)

GPU:
├─ n1-standard-4 + T4: 4 vCPU + T4   ($0.429/h)
└─ n1-standard-8 + V100: 8 vCPU + V100 ($2.75/h)
```

#### Create Instance (gcloud)

```bash
# Create VM
gcloud compute instances create my-vm \
    --zone=us-central1-a \
    --machine-type=e2-medium \
    --image-family=ubuntu-2004-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=20GB \
    --boot-disk-type=pd-standard \
    --tags=http-server

# Create firewall rule
gcloud compute firewall-rules create allow-http \
    --allow=tcp:80 \
    --target-tags=http-server

# SSH
gcloud compute ssh my-vm --zone=us-central1-a

# List instances
gcloud compute instances list
```

#### GCP Infrastructure

```
Based on: KVM

Custom components:
├─ gVisor (sandboxing for containers)
├─ Custom Linux kernel (tuned)
├─ Network virtualization (Andromeda)
└─ Live migration (transparent)

Features:
- Live migration (no downtime)
- Custom machine types
- Preemptible VMs (80% discount)
- Sole-tenant nodes (dedicated hardware)
```

### Comparison Table

| Feature | AWS EC2 | Azure VM | GCP Compute |
|---------|---------|----------|-------------|
| **Hypervisor** | Nitro (KVM-based) | Hyper-V | KVM |
| **Instance variety** | Highest | High | Medium |
| **Pricing model** | On-demand, Reserved, Spot | Pay-as-you-go, Reserved | On-demand, Committed, Preemptible |
| **Billing** | Per-second (min 60s) | Per-second | Per-second |
| **Live migration** | Limited | Yes | Yes (automatic) |
| **Custom CPU/RAM** | No | No | Yes |
| **Regions** | 30+ | 60+ | 35+ |
| **Market share** | ~32% | ~23% | ~10% |

---

## Bare-Metal Instances

### Concept

**Bare-metal**: Physical server dedicato, no hypervisor overhead.

```
Traditional VM:
┌──────────────────────┐
│   Your VM            │
├──────────────────────┤
│   Hypervisor (5% OH) │
├──────────────────────┤
│   Physical Server    │
└──────────────────────┘

Bare-Metal:
┌──────────────────────┐
│   Your OS directly   │
│   (100% resources)   │
├──────────────────────┤
│   Physical Server    │
└──────────────────────┘
```

### AWS EC2 .metal Instances

```
Available types:
├─ m5.metal:  96 vCPU, 384 GB RAM
├─ c5.metal:  96 vCPU, 192 GB RAM
├─ r5.metal:  96 vCPU, 768 GB RAM
└─ i3.metal:  72 vCPU, 512 GB RAM, 8×1.9TB NVMe

Use cases:
✅ Performance-critical workloads
✅ Licensing (per-socket licensing)
✅ Run your own hypervisor (VMware, KVM)
✅ Security compliance
✅ Low-latency applications
```

**Launch .metal instance:**
```bash
aws ec2 run-instances \
    --image-id ami-0c55b159cbfafe1f0 \
    --instance-type m5.metal \
    --key-name MyKeyPair \
    --subnet-id subnet-6e7f829e
```

### Azure Dedicated Hosts

```
Scenario: You want entire physical server

Azure Dedicated Host:
┌────────────────────────────────────┐
│   Dedicated Physical Server        │
│   ┌──────┐ ┌──────┐ ┌──────┐      │
│   │ VM 1 │ │ VM 2 │ │ VM 3 │ ...  │
│   └──────┘ └──────┘ └──────┘      │
│   Your VMs only (single-tenant)    │
└────────────────────────────────────┘

Benefits:
- Hardware isolation
- Compliance (regulatory)
- Licensing (BYOL Windows Server)
- Control over maintenance
```

**Create Dedicated Host:**
```powershell
# Create host group
New-AzHostGroup -Name "MyHostGroup" `
  -Location "EastUS" `
  -ResourceGroupName "MyRG" `
  -PlatformFaultDomainCount 2

# Create dedicated host
New-AzHost -HostGroupName "MyHostGroup" `
  -Name "MyHost1" `
  -Location "EastUS" `
  -ResourceGroupName "MyRG" `
  -Sku "DSv3-Type1"

# Create VM on dedicated host
New-AzVM -ResourceGroupName "MyRG" `
  -Name "MyVM" `
  -Location "EastUS" `
  -HostId "/subscriptions/.../MyHost1" `
  -Image "Win2019Datacenter" `
  -Size "Standard_D4s_v3"
```

### GCP Sole-Tenant Nodes

```
Similar concept to Azure Dedicated Hosts

Node types:
├─ n1-node-96-624:  96 vCPU, 624 GB
├─ c2-node-60-240:  60 vCPU, 240 GB
└─ m1-megamem-96:   96 vCPU, 1.4 TB

Create sole-tenant node:
gcloud compute sole-tenancy node-templates create my-template \
    --node-type=n1-node-96-624 \
    --region=us-central1

gcloud compute sole-tenancy node-groups create my-group \
    --node-template=my-template \
    --target-size=2 \
    --zone=us-central1-a

Create VM on sole-tenant node:
gcloud compute instances create my-vm \
    --zone=us-central1-a \
    --node-group=my-group \
    --machine-type=n1-standard-4
```

### Use Cases Bare-Metal

✅ **Performance**: No virtualization overhead  
✅ **Licensing**: Oracle DB, SQL Server per-socket  
✅ **Security**: Physical isolation  
✅ **Compliance**: Regulatory requirements  
✅ **Run own hypervisor**: VMware on AWS, nested virt  
✅ **GPU workloads**: Direct hardware access  

❌ **Not for**: Cost-sensitive, small workloads (expensive)

---

## Conclusioni

La virtualizzazione è il fondamento del cloud computing moderno:

- **Cloud providers** usano hypervisor customizzati per performance e isolation
- **Hardware-assisted virtualization** (VT-x, AMD-V) rende possibile overhead <1%
- **Nested virtualization** abilita use case avanzati (testing, training)
- **Bare-metal** offre performance massime quando necessario

La continua evoluzione (Nitro, custom silicon) spinge le performance sempre più vicino al bare-metal.

---

## Domande di Autovalutazione

1. Come ha evoluto AWS la sua architettura da Xen a Nitro?
2. Qual è la differenza tra VT-x e VT-d?
3. Quando useresti nested virtualization?
4. Confronta VM tradizionali e bare-metal instances
5. Come funziona SR-IOV e quali sono i benefici?
6. Perché i cloud provider creano custom Linux kernel?

---

## Esercizi Pratici

### Lab 1: Launch Cloud VMs
1. Create account AWS/Azure/GCP (free tier)
2. Launch VM via console
3. Launch VM via CLI
4. Connect via SSH
5. Install web server
6. Test connectivity

### Lab 2: Nested Virtualization
1. Launch GCP instance with nested virt
2. Install KVM inside VM
3. Create nested VM
4. Test performance vs native

### Lab 3: Instance Types
1. Launch t3.micro (burstable)
2. Generate CPU load
3. Monitor credits
4. Compare with c6i.large

---

## Risorse Aggiuntive

- [AWS Nitro System](https://aws.amazon.com/ec2/nitro/)
- [Azure Virtual Machine Documentation](https://docs.microsoft.com/en-us/azure/virtual-machines/)
- [GCP Compute Engine Docs](https://cloud.google.com/compute/docs)
- [Intel VT-x Specifications](https://www.intel.com/content/www/us/en/virtualization/virtualization-technology/intel-virtualization-technology.html)
- [AMD-V Technology](https://www.amd.com/en/technologies/virtualization)
- [SR-IOV Networking Guide](https://www.intel.com/content/www/us/en/support/articles/000005722/network-and-i-o/ethernet-products.html)
