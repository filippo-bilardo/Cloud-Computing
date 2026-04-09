# 2.6 Virtualizzazione dello Storage

## Indice
- [Introduzione allo Storage Virtualizzato](#introduzione-allo-storage-virtualizzato)
- [Storage Area Network (SAN)](#storage-area-network-san)
- [Network Attached Storage (NAS)](#network-attached-storage-nas)
- [Virtual SAN](#virtual-san)
- [Thin Provisioning](#thin-provisioning)
- [Storage Migration](#storage-migration)
- [Data Deduplication](#data-deduplication)

---

## Introduzione allo Storage Virtualizzato

Lo **storage virtualizzato** astrae lo storage fisico, presentando risorse unificate e gestibili centralmente.

### Storage Stack Virtualizzato

```
┌────────────────────────────────────────┐
│    Virtual Machine                     │
│    ┌────────────────────────────────┐  │
│    │  Guest OS Filesystem           │  │
│    │  (NTFS, ext4, XFS)             │  │
│    └──────────────┬─────────────────┘  │
│    ┌──────────────▼─────────────────┐  │
│    │  Virtual Disk (VMDK/QCOW2)     │  │
│    └──────────────┬─────────────────┘  │
└───────────────────┼────────────────────┘
                    │
┌───────────────────▼────────────────────┐
│    Hypervisor Storage Layer            │
│    - Volume Manager                    │
│    - Filesystem (VMFS, ReFS, ext4)     │
└───────────────────┬────────────────────┘
                    │
┌───────────────────▼────────────────────┐
│    Storage Virtualization Layer        │
│    - Pooling                           │
│    - Tiering                           │
│    - Replication                       │
└───────────────────┬────────────────────┘
                    │
┌───────────────────▼────────────────────┐
│    Physical Storage                    │
│    - Disks (SSD, HDD)                  │
│    - Controllers (RAID)                │
│    - Network (FC, iSCSI, NFS)          │
└────────────────────────────────────────┘
```

### Benefici Storage Virtualization

✅ **Consolidamento**: Pool multipli array in singola risorsa  
✅ **Flessibilità**: Provisioning dinamico  
✅ **Efficiency**: Thin provisioning, deduplication  
✅ **Mobility**: Storage vMotion, live migration  
✅ **Protection**: Snapshot, replication integrati  
✅ **Performance**: Tiering automatico  

---

## Storage Area Network (SAN)

### Architettura SAN

```
                Fibre Channel Fabric
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    ┌───▼────┐      ┌───▼────┐      ┌───▼────┐
    │ESXi 1  │      │ESXi 2  │      │ESXi 3  │
    │HBA     │      │HBA     │      │HBA     │
    └────────┘      └────────┘      └────────┘
        │               │               │
        └───────────────┼───────────────┘
                        │
                ┌───────▼────────┐
                │  SAN Switch    │
                │  (FC Switch)   │
                └───────┬────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    ┌───▼────┐      ┌───▼────┐      ┌───▼────┐
    │Storage │      │Storage │      │Storage │
    │Array 1 │      │Array 2 │      │Array 3 │
    └────────┘      └────────┘      └────────┘
```

### Fibre Channel (FC)

**Caratteristiche:**
- **Velocità**: 8/16/32 Gbps (64 Gbps emergente)
- **Distanza**: fino a 10km (single-mode fiber)
- **Protocollo**: Block-level (SCSI over FC)
- **Latenza**: Ultra-bassa (<1ms)
- **Costo**: Alto (HBA, switch, cabling)

**Componenti:**
```
HBA (Host Bus Adapter):
- PCI Express card
- Fibre Channel ports
- WWPN (World Wide Port Name) identifier

FC Switch:
- Managed switch
- Zoning configuration
- ISL (Inter-Switch Links)

Storage Array:
- LUNs (Logical Unit Numbers)
- FC target ports
- RAID controllers
```

**Zoning Configuration:**
```
Zone "ESXi_Cluster_1":
├─ ESXi1_HBA0: 50:00:00:01:02:03:04:05
├─ ESXi2_HBA0: 50:00:00:01:02:03:04:06
├─ ESXi3_HBA0: 50:00:00:01:02:03:04:07
└─ Storage_Port1: 50:00:00:AA:BB:CC:DD:01

Zone "ESXi_Cluster_2":
├─ ESXi4_HBA0: 50:00:00:01:02:03:04:08
└─ Storage_Port2: 50:00:00:AA:BB:CC:DD:02

Benefit: Isolation, security, performance
```

### iSCSI (Internet Small Computer System Interface)

**Caratteristiche:**
- **Velocità**: 1/10/25/40 Gbps (Ethernet)
- **Protocollo**: SCSI over TCP/IP
- **Costo**: Basso (standard Ethernet)
- **Setup**: Più semplice di FC

**Topologia:**
```
┌────────────────────────────────────────┐
│    ESXi Hosts (iSCSI Initiators)       │
│    Software/Hardware iSCSI adapters    │
└───────────────┬────────────────────────┘
                │ Ethernet (Jumbo Frames 9000 MTU)
┌───────────────▼────────────────────────┐
│    Dedicated iSCSI Network             │
│    10GbE Switch (isolated VLAN)        │
└───────────────┬────────────────────────┘
                │
┌───────────────▼────────────────────────┐
│    iSCSI Storage Target                │
│    IP: 192.168.100.10                  │
│    IQN: iqn.2020-01.com.example:storage│
└────────────────────────────────────────┘
```

**VMware iSCSI Configuration:**
```bash
# Enable software iSCSI adapter
esxcli iscsi software set --enabled=true

# Add dynamic target
esxcli iscsi adapter discovery sendtarget add \
  --address=192.168.100.10 \
  --adapter=vmhba33

# Rescan
esxcli storage core adapter rescan --adapter=vmhba33

# Configure CHAP authentication
esxcli iscsi adapter auth chap set \
  --level=required \
  --authname=username \
  --secret=password \
  --adapter=vmhba33
```

**Linux iSCSI Initiator:**
```bash
# Install
apt-get install open-iscsi

# Discover targets
iscsiadm -m discovery -t st -p 192.168.100.10

# Login
iscsiadm -m node -T iqn.2020-01.com.example:storage -p 192.168.100.10 --login

# Verify
lsblk
fdisk -l
```

### FCoE (Fibre Channel over Ethernet)

Bridge tra FC e Ethernet:

```
Benefits:
✅ Single cable infrastructure (convergence)
✅ Ethernet economics
✅ FC performance (lossless Ethernet)

Requirements:
- DCB (Data Center Bridging)
- CNA (Converged Network Adapter)
- FCoE-capable switches
```

### Multipathing

Ridondanza e load balancing per SAN:

```
ESXi Host
├─ HBA0 → FC Switch A → Storage Port 1
│                          (Active)
└─ HBA1 → FC Switch B → Storage Port 2
                          (Standby/Active)

Path Selection Policies:
1. Fixed: Single preferred path
2. MRU (Most Recently Used): Stick to working path
3. Round Robin: Load balance across all paths
4. Weighted: Custom weight per path
```

**VMware Multipathing (NMP):**
```bash
# Show paths
esxcli storage nmp path list

# Set policy to Round Robin
esxcli storage nmp device set --device naa.xxx --psp VMW_PSP_RR

# Verify
esxcli storage nmp device list
```

**Linux Multipath (DM-Multipath):**
```bash
# Install
apt-get install multipath-tools

# Configure /etc/multipath.conf
defaults {
    path_grouping_policy    multibus
    path_selector           "round-robin 0"
    failback                immediate
    no_path_retry           5
}

# Start service
systemctl start multipathd

# Show paths
multipath -ll
```

---

## Network Attached Storage (NAS)

### NFS (Network File System)

**Caratteristiche:**
- **Protocollo**: File-level
- **Versioni**: NFSv3 (legacy), NFSv4.1 (modern)
- **Use case**: VMware VMFS alternative, Linux native

**Architettura:**
```
┌────────────────────────────────────────┐
│    ESXi Hosts                          │
│    NFS Client                          │
└───────────┬────────────────────────────┘
            │ Ethernet (1/10/25 GbE)
┌───────────▼────────────────────────────┐
│    NFS Server                          │
│    - Linux (NFS daemon)                │
│    - NetApp/EMC/FreeNAS                │
│    - Export: /mnt/vmstore              │
└───────────┬────────────────────────────┘
            │
┌───────────▼────────────────────────────┐
│    Physical Storage                    │
│    - ZFS/Btrfs/ext4                    │
│    - RAID array                        │
└────────────────────────────────────────┘
```

**NFS Server Configuration (Linux):**
```bash
# Install
apt-get install nfs-kernel-server

# Export configuration (/etc/exports)
/mnt/vmstore 192.168.1.0/24(rw,sync,no_root_squash,no_subtree_check)

# Apply
exportfs -a
systemctl restart nfs-kernel-server

# Verify
showmount -e localhost
```

**Mount NFS on ESXi:**
```bash
# Via CLI
esxcli storage nfs add \
  --host=192.168.1.100 \
  --share=/mnt/vmstore \
  --volume-name=VMSTORE_NFS

# Via GUI
Storage → New Datastore → NFS
```

**NFSv3 vs NFSv4.1:**

| Feature | NFSv3 | NFSv4.1 |
|---------|-------|---------|
| **Locking** | Stateless | Stateful |
| **Security** | AUTH_SYS | Kerberos support |
| **Performance** | Good | Better (session trunking) |
| **Multipathing** | No native | Yes (trunking) |
| **VMware support** | Yes | Yes (vSphere 6.0+) |

### SMB/CIFS

**Use case**: Hyper-V VMs su SMB shares

**Configuration (Hyper-V):**
```powershell
# Create SMB share on file server
New-SmbShare -Name "VMStore" -Path "D:\VMs" -FullAccess "DOMAIN\HyperV-Hosts"

# Configure Hyper-V to use SMB
New-VM -Name "TestVM" -MemoryStartupBytes 2GB `
  -VHDPath "\\fileserver\VMStore\TestVM\disk.vhdx"

# Best practices:
- SMB 3.0+ (transparent failover, SMB Direct)
- Dedicated network
- Jumbo frames
- RDMA (iWARP/RoCE) for SMB Direct
```

**SMB Multichannel:**
```
Automatic load balancing across multiple NICs:

Hyper-V Host
├─ NIC1 (10 GbE) ──┐
└─ NIC2 (10 GbE) ──┤
                   ├─→ SMB Share
File Server        │   (20 Gbps aggregate)
├─ NIC1 (10 GbE) ──┤
└─ NIC2 (10 GbE) ──┘

No configuration needed (automatic in SMB 3.0+)
```

---

## Virtual SAN

### VMware vSAN

**Concept**: Software-defined storage using local disks.

```
vSAN Cluster:

┌──────────────────────────────────────────┐
│          ESXi Host 1                     │
│  ┌────────┐  ┌────────┐  ┌────────┐      │
│  │ Cache  │  │Capacity│  │Capacity│      │
│  │ SSD    │  │ SSD    │  │ SSD    │      │
│  │ 400GB  │  │  1TB   │  │  1TB   │      │
│  └────────┘  └────────┘  └────────┘      │
└──────────────────┬───────────────────────┘
                   │
                   │ vSAN Network
                   │
        ┌──────────┼──────────┐
        │          │          │
┌───────▼──────────▼──────────▼────────────┐
│       vSAN Distributed Datastore         │
│       - Object storage                   │
│       - Distributed RAID                 │
│       - Deduplication                    │
│       - Compression                      │
│       - Erasure coding                   │
└──────────────────────────────────────────┘
```

**vSAN Architecture Components:**

1. **Disk Groups:**
   - 1 cache tier (SSD/NVMe)
   - 1-7 capacity tier (SSD/HDD)

2. **Storage Objects:**
   - VM Home
   - VMDK
   - Swap
   - Snapshot delta

3. **Storage Policies:**
```
Policy: Mission Critical
├─ Failures to Tolerate (FTT): 2
├─ Stripe width: 2
├─ Disk stripes per object: 2
├─ Flash read cache: 100%
└─ Encryption: Enabled

Policy: Standard
├─ FTT: 1 (RAID-1 mirroring)
├─ Stripe width: 1
└─ Encryption: Optional

Policy: Archive
├─ FTT: 1 (Erasure Coding RAID-5)
└─ Compression: Enabled
```

**RAID Options:**

```
RAID-1 (Mirroring):
VM Disk (10 GB)
├─ Replica 1 (Host A) - 10 GB
├─ Replica 2 (Host B) - 10 GB
└─ Witness (Host C) - 0 GB (metadata)
Total capacity: 20 GB (2x overhead)

RAID-5 (Erasure Coding):
VM Disk (10 GB)
├─ Data stripe 1 (Host A) - 5 GB
├─ Data stripe 2 (Host B) - 5 GB
└─ Parity (Host C) - 5 GB
Total capacity: 15 GB (1.5x overhead)
Requires: 4+ hosts

RAID-6 (Dual Parity):
VM Disk (10 GB)
├─ Data stripe 1 (Host A) - 3.33 GB
├─ Data stripe 2 (Host B) - 3.33 GB
├─ Data stripe 3 (Host C) - 3.33 GB
├─ Parity 1 (Host D) - 3.33 GB
└─ Parity 2 (Host E) - 3.33 GB
Total capacity: 16.66 GB (1.67x overhead)
Requires: 6+ hosts
FTT: 2
```

### Microsoft Storage Spaces Direct (S2D)

Equivalent di vSAN per Hyper-V:

```
S2D Cluster:

┌─────────────────────────────────────────┐
│        Hyper-V Node 1                   │
│  ┌────────┐  ┌────────┐  ┌────────┐     │
│  │ NVMe   │  │  SSD   │  │  HDD   │     │
│  │ Tier   │  │ Tier   │  │ Tier   │     │
│  └────────┘  └────────┘  └────────┘     │
└───────────────────┬─────────────────────┘
                    │
                    │ RDMA Network
                    │
┌───────────────────▼─────────────────────┐
│     Storage Spaces Direct Pool          │
│     - ReFS filesystem                   │
│     - Mirroring / Parity                │
│     - Tiering (hot/cold)                │
│     - Deduplication                     │
└─────────────────────────────────────────┘
```

**Resiliency Types:**
- Two-way mirror (2 copies)
- Three-way mirror (3 copies)
- Parity (similar to RAID-5)
- Dual parity (similar to RAID-6)

**Configuration (PowerShell):**
```powershell
# Enable S2D
Enable-ClusterStorageSpacesDirect

# Create volume
New-Volume -FriendlyName "VMStore" `
  -FileSystem ReFS `
  -StoragePoolFriendlyName S2D* `
  -Size 1TB `
  -ResiliencySettingName Mirror `
  -ProvisioningType Thin

# Create CSV
Add-ClusterSharedVolume -Name "Cluster Virtual Disk (VMStore)"
```

### Ceph

Open-source distributed storage:

```
Ceph Cluster:

┌────────────────────────────────────────┐
│         Ceph Monitor Nodes             │
│         (Cluster state, quorum)        │
└──────────────┬─────────────────────────┘
               │
        ┌──────┼──────┐
        │      │      │
┌───────▼──────▼──────▼────────────────┐
│        Ceph OSD Nodes                │
│        (Object Storage Daemons)      │
│  ┌──────┐  ┌──────┐  ┌──────┐        │
│  │ OSD 1│  │ OSD 2│  │ OSD 3│ ...    │
│  │Disk 1│  │Disk 2│  │Disk 3│        │
│  └──────┘  └──────┘  └──────┘        │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│     RADOS (Object Store)             │
│     ├─ RBD (Block Device for VMs)    │
│     ├─ CephFS (File System)          │
│     └─ RGW (Object Gateway S3)       │
└──────────────────────────────────────┘
```

**Use with KVM:**
```bash
# Create RBD pool
ceph osd pool create rbd 128 128

# Create RBD image
rbd create --size 10240 myvm-disk

# Map to host
rbd map myvm-disk

# Use in libvirt
<disk type='network' device='disk'>
  <source protocol='rbd' name='rbd/myvm-disk'>
    <host name='192.168.1.101' port='6789'/>
    <host name='192.168.1.102' port='6789'/>
    <host name='192.168.1.103' port='6789'/>
  </source>
  <target dev='vda' bus='virtio'/>
</disk>
```

---

## Thin Provisioning

### Concept

```
Thick Provisioning:
VM needs: 100 GB
Allocated: 100 GB immediately
┌────────────────────────────────────┐
│████████████████████░░░░░░░░░░░░░░░░│ Storage
│ Used: 60GB   Free: 40GB            │
└────────────────────────────────────┘
Wasted: 40 GB allocated but unused

Thin Provisioning:
VM needs: 100 GB (maximum)
Allocated: 60 GB (actual usage)
┌────────────────────────────────────┐
│████████████████████                │ Storage
│ Used: 60GB   Available: pool       │
└────────────────────────────────────┘
Grows on-demand up to 100 GB
```

### Implementation

**VMware Thin VMDK:**
```bash
# Create thin disk
vmkfstools -c 100G -d thin myvm-thin.vmdk

# Convert thick to thin
vmkfstools -i source-thick.vmdk -d thin dest-thin.vmdk

# Check provisioning
ls -lh myvm-thin.vmdk        # Descriptor
ls -lh myvm-thin-flat.vmdk   # Actual data
```

**QCOW2 (naturally thin):**
```bash
# Create
qemu-img create -f qcow2 disk.qcow2 100G

# Check actual size
qemu-img info disk.qcow2
virtual size: 100 GiB
disk size: 1.2 GiB  ← Actual space used
```

**VHD/VHDX Dynamic:**
```powershell
# Create dynamic VHDX
New-VHD -Path C:\VMs\disk.vhdx -SizeBytes 100GB -Dynamic

# Convert fixed to dynamic
Convert-VHD -Path thick.vhdx -DestinationPath thin.vhdx -VHDType Dynamic
```

### Monitoring and Alerts

**Critical**: Avoid running out of physical space!

```
Storage Pool: 10 TB physical
Thin provisioned: 30 TB (3:1 overcommit)
Actually used: 7 TB (70% utilization)

Alerts:
├─ 75% utilization → Warning
├─ 85% utilization → Critical (add storage)
└─ 95% utilization → Emergency (VM pauses!)

Tools:
- VMware vCenter capacity reports
- Storage array monitoring
- Datastore alarms
```

**VMware Alarm:**
```
Datastore Disk Usage:
- Warning: > 75%
- Alert: > 85%
- Action: Send email, SNMP trap
```

---

## Storage Migration

### Live Migration (Storage vMotion)

Spostare VM disk senza downtime:

```
Before:
┌──────────────┐
│   ESXi Host  │
│   ┌────────┐ │
│   │   VM   │ │ Running
│   └────┬───┘ │
└────────┼─────┘
         │
┌────────▼─────┐
│ Datastore A  │ ← VM disk here
└──────────────┘

During:
┌──────────────┐
│   ESXi Host  │
│   ┌────────┐ │
│   │   VM   │ │ Still running!
│   └────┬───┘ │
└────────┼─────┘
         │
┌────────▼─────┐  ┌──────────────┐
│ Datastore A  │──┤Mirror writes │─→ Datastore B
│ (Source)     │  │              │   (Destination)
└──────────────┘  └──────────────┘

After:
┌──────────────┐
│   ESXi Host  │
│   ┌────────┐ │
│   │   VM   │ │ Running
│   └────┬───┘ │
└────────┼─────┘
         │
┌────────▼─────┐
│ Datastore B  │ ← VM disk now here
└──────────────┘
```

**VMware Storage vMotion:**
```bash
# Via govc
govc vm.migrate -ds=Datastore_B vm-name

# PowerCLI
Move-VM -VM "vm-name" -Datastore "Datastore_B"
```

**Process:**
1. **Setup shadow VM** on destination
2. **Copy disk** blocks (bulk transfer)
3. **Mirror writes** (changed block tracking)
4. **Sync final** changes
5. **Switchover** (milliseconds downtime)
6. **Cleanup** source disk

### KVM Live Block Migration

```bash
# Storage migration (virsh)
virsh migrate --live \
  --copy-storage-all \
  --persistent \
  --undefinesource \
  myvm qemu+ssh://destination-host/system

# Block copy (active VM)
virsh blockcopy myvm vda /new/path/disk.qcow2 --wait --verbose
virsh blockjob myvm vda --pivot  # Switch to new disk
```

### Hyper-V Storage Migration

```powershell
# Live storage migration
Move-VMStorage -VMName "TestVM" -DestinationStoragePath "D:\NewLocation"

# Multiple VHDs
Move-VMStorage -VMName "TestVM" `
  -VirtualMachinePath "D:\VMs\TestVM" `
  -VHDs @{"C:\Old\disk1.vhdx"="D:\New\disk1.vhdx"; 
          "C:\Old\disk2.vhdx"="D:\New\disk2.vhdx"}
```

---

## Data Deduplication

### Inline vs Post-Process

```
Inline Deduplication:
Write → Dedupe Engine → Storage
        (Real-time)      (Unique blocks only)

Pros: Immediate space savings
Cons: Write latency impact

Post-Process Deduplication:
Write → Storage → Dedupe Engine
        (Full)    (Scheduled job finds duplicates)

Pros: No write impact
Cons: Delayed savings, requires extra space initially
```

### Block-Level Deduplication

```
Before Dedup:
VM1: [Block A][Block B][Block C]
VM2: [Block A][Block B][Block D]
VM3: [Block A][Block E][Block F]
Total: 9 blocks

After Dedup:
Unique Blocks: [A][B][C][D][E][F]
Total: 6 blocks (33% savings)

VM1 → [Ref A][Ref B][Ref C]
VM2 → [Ref A][Ref B][Ref D]
VM3 → [Ref A][Ref E][Ref F]
```

### Hash-Based Deduplication

```
1. Chunk data into fixed/variable blocks
2. Calculate hash (SHA-256) of each block
3. Store hash → block mapping
4. New write:
   - Calculate hash
   - If hash exists → reference existing block
   - If new → store block + hash
```

### VMware vSAN Deduplication

```bash
# Enable deduplication (requires All-Flash)
# Per disk group:
Storage → vSAN → Disk Management
→ Select Disk Group
→ Enable Deduplication and Compression

# Inline processing (no scheduling needed)
# Savings: 2-5x typical for VDI workloads
```

**Dedup + Compression:**
```
Original data: 100 GB
↓ Deduplication (4:1)
After dedup: 25 GB
↓ Compression (2:1)
Final: 12.5 GB
Overall: 8:1 space reduction
```

### Windows Server Deduplication

```powershell
# Enable on volume
Enable-DedupVolume -Volume "D:"

# Set optimization
Set-DedupVolume -Volume "D:" -OptimizeFor Default
# Options: Default, HyperV, Backup

# Manual optimization
Start-DedupJob -Volume "D:" -Type Optimization

# Check savings
Get-DedupStatus -Volume "D:"

# Output example:
SavedSpace        : 450 GB
OptimizedFilesSavingsRate : 75%
DedupRate         : 4.2
```

### Ceph Deduplication (Experimental)

```bash
# Enable BlueStore deduplication
ceph osd pool set rbd compression_mode aggressive

# Monitor
ceph osd pool get rbd compression_algorithm
ceph osd pool get rbd compression_mode
```

### Best Practices

✅ **Enable on appropriate workloads**: VDI, file servers, backups  
✅ **Don't enable on**:  
   - Database datastores (low dedup ratio, performance impact)
   - Already compressed data (video, images)
✅ **Monitor dedup ratio**: <2:1 = not worth it  
✅ **Plan for overhead**: CPU/RAM for dedup engine  
✅ **All-flash recommended**: Dedup metadata lookups are I/O intensive  

---

## Advanced Storage Features

### Storage DRS (VMware)

Automatic load balancing for datastores:

```
Scenario:
Datastore 1: 90% full, 5000 IOPS
Datastore 2: 50% full, 1000 IOPS
Datastore 3: 40% full, 800 IOPS

Storage DRS actions:
1. Identify imbalanced datastores
2. Recommend VM migrations
3. Automatically migrate (if automation enabled)
4. Balance capacity and I/O

Result:
All datastores: ~60% full, ~2200 IOPS each
```

**Configuration:**
```
Datastore Cluster → Configure
├─ Automation: Manual/Automatic
├─ I/O metric: Enabled
├─ Utilization threshold: 80%
├─ I/O latency threshold: 15ms
└─ Advanced options: affinity/anti-affinity rules
```

### Storage I/O Control (SIOC)

QoS per VM/datastore:

```
Under congestion (latency > 30ms):

Production VM:   2000 shares → Gets 50% IOPS
Development VM:  1000 shares → Gets 25% IOPS
Test VM:         1000 shares → Gets 25% IOPS

Total: 4000 shares = 100%
```

### Snapshot Management

```
Best Practices:
✅ Short-lived: Delete within 24-72 hours
✅ Not backups: Use proper backup solutions
✅ Limit chain depth: <3 snapshots
✅ Monitor snapshot size: Growth = I/O overhead
❌ Don't use in production long-term

Consolidation:
When deleting snapshot, merge delta disk back to base:
Base (100 GB) + Delta (5 GB) = New Base (105 GB)
```

---

## Conclusioni

Lo storage virtualizzato è fondamentale per cloud e datacenter moderni:

- **SAN**: Performance, FC/iSCSI
- **NAS**: Semplicità, NFS/SMB
- **vSAN/S2D**: Software-defined, local disks
- **Thin Provisioning**: Efficienza, monitoraggio critico
- **Migration**: Flexibility, zero downtime
- **Deduplication**: Space savings per workload appropriati

La scelta dipende da: performance requirements, budget, existing infrastructure, expertise.

---

## Domande di Autovalutazione

1. Quali sono le differenze principali tra SAN e NAS?
2. Come funziona il multipathing e quali sono i benefici?
3. Spiega il concetto di thin provisioning e i rischi associati
4. Quando è appropriato usare vSAN vs traditional SAN?
5. Come funziona Storage vMotion?
6. Quali workload beneficiano di più dalla deduplication?
7. Confronta FC e iSCSI per storage access

---

## Esercizi Pratici

### Lab 1: iSCSI Configuration
1. Setup Linux iSCSI target (targetcli)
2. Configure initiator su ESXi/KVM
3. Create LUN e mount come datastore
4. Test performance

### Lab 2: NFS Datastore
1. Configure NFS export su Linux
2. Mount su ESXi/Hyper-V
3. Create VM su NFS storage
4. Compare performance vs local disk

### Lab 3: Thin Provisioning
1. Create thick e thin disks
2. Monitor space utilization
3. Fill guest filesystem
4. Observe growth patterns

### Lab 4: Storage Migration
1. Setup 2 datastores
2. Create running VM
3. Perform live storage migration
4. Verify no downtime

---

## Risorse Aggiuntive

- [VMware vSAN Documentation](https://docs.vmware.com/en/VMware-vSAN/)
- [Microsoft Storage Spaces Direct](https://docs.microsoft.com/en-us/windows-server/storage/storage-spaces/storage-spaces-direct-overview)
- [Ceph Documentation](https://docs.ceph.com/)
- [Linux iSCSI Target Guide](https://www.kernel.org/doc/Documentation/target/tcmu-design.txt)
- [EMC Storage Architecture Guide](https://www.dell.com/support/home/en-us/product-support/product/unity-300/docs)
- [NetApp ONTAP Documentation](https://docs.netapp.com/)
