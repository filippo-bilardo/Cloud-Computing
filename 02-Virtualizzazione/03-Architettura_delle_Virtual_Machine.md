# 2.3 Architettura delle Virtual Machine

## Indice
- [Struttura di una VM](#struttura-di-una-vm)
- [Virtual CPU (vCPU)](#virtual-cpu-vcpu)
- [Virtual Memory](#virtual-memory)
- [Virtual Network Interface](#virtual-network-interface)
- [Virtual Disk](#virtual-disk)
- [Snapshot e Cloning](#snapshot-e-cloning)
- [Template di VM](#template-di-vm)

---

## Struttura di una VM

Una **Virtual Machine** è composta da diversi file e componenti che insieme simulano un computer fisico completo.

### Componenti Principali

```
┌─────────────────────────────────────────┐
│         Virtual Machine                 │
├─────────────────────────────────────────┤
│  Guest OS (Linux/Windows)               │
├─────────────────────────────────────────┤
│  Virtual Hardware:                      │
│  ┌────────┐  ┌──────────┐  ┌─────────┐  │
│  │ vCPU   │  │ vMemory  │  │ vNIC    │  │
│  └────────┘  └──────────┘  └─────────┘  │
│  ┌────────┐  ┌──────────┐               │
│  │ vDisk  │  │ vGPU     │               │
│  └────────┘  └──────────┘               │
└─────────────────────────────────────────┘
```

### File della VM

#### VMware VM Files
```
myvm/
├── myvm.vmx            ← Configuration file (CPU, RAM, devices)
├── myvm.vmdk           ← Virtual disk descriptor
├── myvm-flat.vmdk      ← Actual disk data
├── myvm.nvram          ← BIOS/UEFI settings
├── myvm.vmsd           ← Snapshot metadata
├── myvm-000001.vmdk    ← Snapshot delta disk
├── myvm.vmss           ← Suspended state (RAM dump)
└── vmware.log          ← VM log file
```

#### VirtualBox VM Files
```
myvm/
├── myvm.vbox           ← Configuration (XML)
├── myvm.vdi            ← Virtual disk image
├── myvm.vbox-prev      ← Previous configuration backup
└── Snapshots/
    ├── {uuid}.vdi      ← Snapshot differencing disks
    └── {uuid}.sav      ← Saved state
```

#### KVM/QEMU VM Files
```
/var/lib/libvirt/images/
├── myvm.qcow2          ← Disk image (QCOW2 format)
└── myvm.xml            ← Domain XML definition

/etc/libvirt/qemu/
└── myvm.xml            ← VM configuration
```

### Formato File Configurazione VMX (VMware)

```ini
# myvm.vmx
.encoding = "UTF-8"
config.version = "8"
virtualHW.version = "19"

# CPU Configuration
numvcpus = "4"
cpuid.coresPerSocket = "2"

# Memory Configuration
memSize = "8192"

# Disk Configuration
scsi0.present = "TRUE"
scsi0:0.fileName = "myvm.vmdk"
scsi0:0.present = "TRUE"

# Network Configuration
ethernet0.present = "TRUE"
ethernet0.connectionType = "nat"
ethernet0.virtualDev = "e1000e"
ethernet0.addressType = "generated"

# Guest OS
guestOS = "ubuntu-64"

# Display
svga.vramSize = "268435456"
```

---

## Virtual CPU (vCPU)

### Architettura vCPU

```
Physical CPU (16 cores)
├─ Core 0  ←→  vCPU 0 (VM1), vCPU 4 (VM2)
├─ Core 1  ←→  vCPU 1 (VM1), vCPU 5 (VM2)
├─ Core 2  ←→  vCPU 2 (VM1), vCPU 0 (VM3)
├─ Core 3  ←→  vCPU 3 (VM1), vCPU 1 (VM3)
│  ...
└─ Core 15

Hypervisor CPU Scheduler:
- Time-slicing
- Fair-share scheduling
- Priority-based allocation
```

### vCPU Configuration

#### Cores vs Sockets

```
Scenario 1: 4 vCPU = 1 socket × 4 cores
┌──────────────┐
│   Socket 0   │
│ ┌──┐┌──┐┌──┐│
│ │C0││C1││C2││C3│
│ └──┘└──┘└──┘└──┘
└──────────────┘

Scenario 2: 4 vCPU = 2 sockets × 2 cores
┌─────────┐  ┌─────────┐
│Socket 0 │  │Socket 1 │
│ ┌──┐┌──┐│  │┌──┐┌──┐│
│ │C0││C1││  ││C2││C3││
│ └──┘└──┘│  │└──┘└──┘│
└─────────┘  └─────────┘
```

**Impatto licensing:**
- Alcune app (SQL Server, Oracle) licensiate per socket
- 1 socket × 8 cores = 1 license
- 8 sockets × 1 core = 8 licenses!

#### CPU Configuration Examples

**VMware:**
```bash
# 4 vCPU = 2 sockets × 2 cores/socket
vim-cmd vmsvc/get.config <vmid> | grep cpu
numvcpus = "4"
cpuid.coresPerSocket = "2"
```

**VirtualBox:**
```bash
VBoxManage modifyvm "myvm" --cpus 4
VBoxManage modifyvm "myvm" --cpu-profile "Intel Core i7-6700K"
```

**KVM/virsh:**
```xml
<vcpu placement='static'>4</vcpu>
<cpu mode='host-passthrough'>
  <topology sockets='2' cores='2' threads='1'/>
</cpu>
```

### CPU Features

#### CPU Masking/Pass-through

```
Physical CPU: Intel Xeon with AVX512
↓
Hypervisor can:
1. Hide features (masking)
   → Guest sees generic CPU
2. Expose features (pass-through)
   → Guest sees AVX512

Why mask?
- vMotion compatibility (different CPU generations)
- Security (mitigate Spectre/Meltdown)
```

#### CPU Pinning

Assegnare vCPU a pCPU specifici:

```bash
# KVM: Pin vCPU 0 to pCPU 2
virsh vcpupin myvm 0 2

# Check pinning
virsh vcpupin myvm
 VCPU   CPU Affinity
----------------------
 0      2
 1      0-15
 2      0-15
 3      0-15
```

**Use case:**
- Performance-critical workloads
- NUMA optimization
- Real-time applications

### CPU Hot-Plug

Aggiungere vCPU senza reboot:

```bash
# VMware (solo aumento, requires vmtools)
vim-cmd vmsvc/device.hotplug <vmid> cpu add 2

# KVM
virsh setvcpus myvm 8 --live

# Guest OS deve supportare CPU hotplug
# Linux: CONFIG_HOTPLUG_CPU=y
echo 1 > /sys/devices/system/cpu/cpu4/online
```

---

## Virtual Memory

### Memory Virtualization

```
┌─────────────────────────────────────────┐
│  Guest Virtual Memory (GVA)             │
│  Application view: 0x00000000...        │
└──────────────┬──────────────────────────┘
               │ Guest Page Tables
┌──────────────▼──────────────────────────┐
│  Guest Physical Memory (GPA)            │
│  Guest OS view: 0x00000000...           │
└──────────────┬──────────────────────────┘
               │ Hypervisor (EPT/NPT)
┌──────────────▼──────────────────────────┐
│  Host Physical Memory (HPA)             │
│  Real RAM: 0x100000000...               │
└─────────────────────────────────────────┘

Three levels of translation:
GVA → GPA → HPA
```

### Memory Management Techniques

#### 1. Memory Overcommitment

Allocare più memoria virtuale che RAM fisica:

```
Physical RAM: 64 GB
├─ VM1: 32 GB allocated
├─ VM2: 32 GB allocated
├─ VM3: 16 GB allocated
└─ VM4: 16 GB allocated
   ───────────────────
   Total: 96 GB (1.5x overcommit)

Possible because VMs don't use all allocated RAM simultaneously
```

#### 2. Ballooning

Driver nel guest "gonfia" balloon per reclamare memoria:

```
VM has 8 GB allocated
Currently using 6 GB

Hypervisor needs memory:
┌──────────────────┐
│  Guest OS        │
│  ┌────────────┐  │
│  │ Balloon    │  │ ← Driver "inflates"
│  │ Driver     │  │    Requests guest to free pages
│  │ [2 GB]     │  │    Guest thinks it's memory pressure
│  └────────────┘  │
│  Used: 4 GB      │
│  Free: 2 GB      │
│  Balloon: 2 GB   │ ← Returned to hypervisor
└──────────────────┘
```

**VMware Ballooning:**
```bash
# Check balloon status
esxcli vm process list
vmware-vmx -sched.mem.ballon.enable="TRUE"

# Inside guest (Linux)
cat /proc/meminfo | grep Balloon
```

#### 3. Memory Swapping

Hypervisor swappa memoria VM su disco:

```
Hypervisor Level Swapping:
┌─────────────┐
│  VM Memory  │
│  8 GB       │
└──────┬──────┘
       │ Less frequently used pages
       ▼
┌─────────────┐
│  .vswp file │ ← VM swap file (separate from guest swap)
│  on datastore
└─────────────┘

Performance: Slowest reclamation method
```

#### 4. Memory Compression

Comprimi pagine in memoria prima di swapping:

```
Original: 4 pages × 4KB = 16 KB
↓ Compression
Compressed: 4 KB (4:1 ratio)

┌──────────────────────┐
│  Active Pages        │ 75% uncompressed
├──────────────────────┤
│  Compressed Pages    │ 20% compressed
├──────────────────────┤
│  Swapped Pages       │ 5% to disk
└──────────────────────┘
```

**VMware:**
```bash
esxcli system settings kernel set -s memCompEnable -v TRUE
```

#### 5. Transparent Page Sharing (TPS)

Deduplica pagine identiche tra VM:

```
VM1: [Page A] [Page B] [Page C]
VM2: [Page A] [Page B] [Page D]
VM3: [Page A] [Page E] [Page F]

After TPS:
┌──────────┐
│ Page A   │ ← Shared by VM1, VM2, VM3 (3 copies → 1 copy)
├──────────┤
│ Page B   │ ← Shared by VM1, VM2
├──────────┤
│ Page C   │ ← VM1 only
│ Page D   │ ← VM2 only
│ Page E   │ ← VM3 only
│ Page F   │ ← VM3 only
└──────────┘

Savings: Common OS pages, libraries
Security: Disabled by default (Spectre concerns)
```

### Memory Configuration

**VMware:**
```ini
# myvm.vmx
memSize = "8192"                    # 8 GB
sched.mem.min = "4096"              # 4 GB reservation
sched.mem.shares = "normal"         # Priority
mem.hotadd = "TRUE"                 # Hot-add support
```

**KVM:**
```xml
<memory unit='GiB'>8</memory>
<currentMemory unit='GiB'>8</currentMemory>
<memoryBacking>
  <hugepages/>                      <!-- Use huge pages (2MB) -->
</memoryBacking>
```

**VirtualBox:**
```bash
VBoxManage modifyvm "myvm" --memory 8192
VBoxManage modifyvm "myvm" --page-fusion on  # TPS equivalent
```

### Memory Hot-Add

Aggiungere RAM senza reboot:

```bash
# VMware: Enable in .vmx
mem.hotadd = "TRUE"

# Then hot-add
vim-cmd vmsvc/device.hotplug <vmid> memory add 4096

# KVM
virsh setmem myvm 12G --live

# Guest Linux detection
echo 1 > /sys/devices/system/memory/memory32/online
```

### NUMA Awareness

```
Physical Server with 2 NUMA Nodes:

Node 0: CPU 0-7,  RAM 0-64GB     Node 1: CPU 8-15, RAM 64-128GB
┌──────────────────┐              ┌──────────────────┐
│  Local Memory    │              │  Local Memory    │
│  Fast access     │              │  Fast access     │
└──────────────────┘              └──────────────────┘

VM with 8 vCPU, 32GB:
Best practice: Fit in single NUMA node
- vCPU 0-7 on pCPU 0-7
- Memory from Node 0
- Avoid remote memory access (slower)
```

**VMware NUMA config:**
```ini
numa.vcpu.maxPerVirtualNode = "8"
```

**KVM NUMA:**
```xml
<numatune>
  <memory mode='strict' nodeset='0'/>
</numatune>
```

---

## Virtual Network Interface

### vNIC Architecture

```
┌──────────────────────────────────────────┐
│           Virtual Machine                │
│  ┌────────────────────────────────────┐  │
│  │  Guest OS Network Stack            │  │
│  │  (IP: 192.168.1.100)               │  │
│  └─────────────┬──────────────────────┘  │
│                │                          │
│  ┌─────────────▼──────────────────────┐  │
│  │  Virtual NIC Driver                │  │
│  │  (vmxnet3, virtio-net, e1000)      │  │
│  └─────────────┬──────────────────────┘  │
└────────────────┼─────────────────────────┘
                 │
      ┌──────────▼──────────┐
      │  Virtual Switch     │
      │  (vSwitch/OVS)      │
      └──────────┬──────────┘
                 │
      ┌──────────▼──────────┐
      │  Physical NIC       │
      │  (eth0)             │
      └─────────────────────┘
```

### vNIC Types

#### VMware vNIC Types

| Type | Description | Performance | Use Case |
|------|-------------|-------------|----------|
| **E1000** | Emulated Intel NIC | Low | Legacy OS |
| **E1000E** | Enhanced E1000 | Medium | Modern OS |
| **VMXNET3** | Paravirtualized | High | Best performance |
| **VMXNET2** | Older paravirt | Medium | Legacy |

```bash
# Change NIC type (VMware)
vim-cmd vmsvc/device.getdevices <vmid>
# Edit .vmx:
ethernet0.virtualDev = "vmxnet3"
```

#### KVM/QEMU vNIC Types

| Type | Description | Performance |
|------|-------------|-------------|
| **e1000** | Intel emulation | Low |
| **rtl8139** | Realtek emulation | Low |
| **virtio-net** | Paravirtualized | High |

```xml
<!-- KVM virtio-net -->
<interface type='network'>
  <model type='virtio'/>
  <source network='default'/>
</interface>
```

#### VirtualBox vNIC Types

- **Intel PRO/1000 MT** (82540EM)
- **PCnet-PCI II** (Am79C970A)
- **PCnet-FAST III** (Am79C973)
- **Paravirtualized Network** (virtio-net)

```bash
VBoxManage modifyvm "myvm" --nictype1 82540EM
VBoxManage modifyvm "myvm" --nictype1 virtio
```

### Network Modes

#### 1. Bridged Mode

VM sulla stessa rete fisica dell'host:

```
Physical Network: 192.168.1.0/24
├─ Router: 192.168.1.1
├─ Host: 192.168.1.10
├─ VM1: 192.168.1.20   ← Direct access to LAN
└─ VM2: 192.168.1.21   ← Can communicate with other devices
```

**Configuration:**
```bash
# VMware
ethernet0.connectionType = "bridged"

# VirtualBox
VBoxManage modifyvm "myvm" --nic1 bridged --bridgeadapter1 "eth0"

# KVM
<interface type='bridge'>
  <source bridge='br0'/>
</interface>
```

#### 2. NAT Mode

VM dietro NAT, host fa routing:

```
External Network
      ↓
┌─────────────────┐
│  Host           │
│  eth0: 192.168.1.10
│                 │
│  ┌───────────┐  │
│  │ NAT Router│  │
│  │ 10.0.2.1  │  │
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │ VM        │  │
│  │ 10.0.2.15 │  │ ← Outbound OK, inbound needs port forwarding
│  └───────────┘  │
└─────────────────┘
```

**Port Forwarding:**
```bash
# VirtualBox: Forward host:8080 → VM:80
VBoxManage modifyvm "myvm" --natpf1 "web,tcp,,8080,,80"

# VMware: Edit .vmx
ethernet0.nat.port.8080 = "80"
```

#### 3. Host-Only Mode

Network isolato tra host e VM:

```
┌──────────────────────┐
│  Host                │
│  vboxnet0: 192.168.56.1
│                      │
│  ┌────────┐ ┌──────┐│
│  │ VM1    │ │ VM2  ││
│  │.56.101 │ │.56.102
│  └────────┘ └──────┘│
│                      │
│  No internet access  │
└──────────────────────┘
```

**Use case:** Isolated testing environment

```bash
# VirtualBox: Create host-only network
VBoxManage hostonlyif create
VBoxManage hostonlyif ipconfig vboxnet0 --ip 192.168.56.1

# Assign to VM
VBoxManage modifyvm "myvm" --nic1 hostonly --hostonlyadapter1 vboxnet0
```

#### 4. Internal Network

Network isolato solo tra VM:

```
┌──────────────────────┐
│  Internal Network    │
│  "intnet"            │
│                      │
│  ┌────────┐ ┌──────┐│
│  │ VM1    │ │ VM2  ││ ← Can communicate
│  └────────┘ └──────┘│
│                      │
│  Host has NO access  │
└──────────────────────┘
```

```bash
# VirtualBox
VBoxManage modifyvm "myvm" --nic1 intnet --intnet1 "intnet"
```

### Advanced Networking

#### SR-IOV (Single Root I/O Virtualization)

Direct hardware access bypass hypervisor:

```
Physical NIC with SR-IOV
├─ PF (Physical Function)     ← Managed by hypervisor
└─ VF (Virtual Functions)
   ├─ VF1 → VM1 (direct pass-through)
   ├─ VF2 → VM2
   └─ VF3 → VM3

Performance: Near-native (10-40 Gbps)
Limitation: No live migration
```

**KVM SR-IOV:**
```xml
<interface type='hostdev' managed='yes'>
  <source>
    <address type='pci' domain='0x0000' bus='0x03' slot='0x10' function='0x1'/>
  </source>
</interface>
```

#### NIC Teaming/Bonding

Multiple vNIC per HA/load balancing:

```
VM with 2 vNIC:
├─ vNIC0 → pNIC0 (Active)
└─ vNIC1 → pNIC1 (Standby/Load-balanced)
```

---

## Virtual Disk

### Virtual Disk Formats

#### VMDK (VMware Virtual Disk)

```
Types:
1. Thick Provision Lazy Zeroed
   - Full size allocated immediately
   - Zeroed on first write
   - Fast creation, medium performance

2. Thick Provision Eager Zeroed
   - Full size allocated + zeroed immediately
   - Slow creation, best performance
   - Required for FT (Fault Tolerance)

3. Thin Provision
   - Allocated on-demand
   - Fast creation, saves space
   - Performance penalty (growth overhead)
```

**Create VMDK:**
```bash
# VMware
vmkfstools -c 20G -d thin myvm.vmdk

# Convert
vmkfstools -i source.vmdk -d eagerzeroedthick dest.vmdk
```

#### QCOW2 (QEMU Copy-On-Write)

```
Features:
- Thin provisioning (sparse)
- Internal snapshots
- Compression
- Encryption
- Backing files (linked clones)
```

**Create QCOW2:**
```bash
# Create 20GB disk
qemu-img create -f qcow2 myvm.qcow2 20G

# With backing file (linked clone)
qemu-img create -f qcow2 -b base.qcow2 -F qcow2 delta.qcow2

# Convert
qemu-img convert -f vmdk -O qcow2 source.vmdk dest.qcow2

# Compress
qemu-img convert -f qcow2 -O qcow2 -c source.qcow2 compressed.qcow2
```

#### VDI (VirtualBox Disk Image)

```bash
# Create VDI
VBoxManage createhd --filename myvm.vdi --size 20480 --variant Standard

# Thin provisioned
--variant Standard

# Fixed size
--variant Fixed

# Clone
VBoxManage clonehd source.vdi dest.vdi
```

#### VHD/VHDX (Microsoft Virtual Hard Disk)

```
VHD (legacy):
- Max 2TB
- Used by Hyper-V, Azure

VHDX (modern):
- Max 64TB
- Better performance
- Resilience to power failures
```

**PowerShell:**
```powershell
# Create VHDX
New-VHD -Path C:\VMs\myvm.vhdx -SizeBytes 20GB -Dynamic

# Fixed size
New-VHD -Path C:\VMs\myvm.vhdx -SizeBytes 20GB -Fixed

# Convert VHD → VHDX
Convert-VHD -Path old.vhd -DestinationPath new.vhdx
```

### Disk Controllers

```
IDE (Legacy):
- Max 4 disks
- Slow (100 MB/s)
- Wide compatibility

SCSI:
- Max 15 disks per controller
- Fast (400+ MB/s)
- Most common

SATA:
- Max 30 disks
- Medium speed (600 MB/s)
- Good compatibility

NVMe (Modern):
- Max 256 devices
- Very fast (3500+ MB/s)
- Requires modern OS
```

**VMware:**
```ini
# SCSI controller
scsi0.present = "TRUE"
scsi0.virtualDev = "lsilogic"  # or "pvscsi" for paravirtual

# Attach disk
scsi0:0.fileName = "myvm.vmdk"
scsi0:0.present = "TRUE"
```

**KVM:**
```xml
<disk type='file' device='disk'>
  <driver name='qemu' type='qcow2' cache='none' io='native'/>
  <source file='/var/lib/libvirt/images/myvm.qcow2'/>
  <target dev='vda' bus='virtio'/>  <!-- virtio for performance -->
</disk>
```

### Disk Operations

#### Expand Disk

```bash
# VMware
vmkfstools -X 30G myvm.vmdk  # Expand to 30GB

# QCOW2
qemu-img resize myvm.qcow2 +10G  # Add 10GB

# VirtualBox
VBoxManage modifyhd myvm.vdi --resize 30720  # 30GB in MB

# VHDX
Resize-VHD -Path myvm.vhdx -SizeBytes 30GB

# Then resize partition inside guest OS
```

#### Compact/Shrink Disk

```bash
# 1. Inside guest: Zero free space
# Linux:
dd if=/dev/zero of=/zero.file bs=1M
rm /zero.file

# Windows:
sdelete -z C:

# 2. Compact on host
# VMware
vmkfstools --punchzero myvm-flat.vmdk

# VirtualBox
VBoxManage modifymedium disk myvm.vdi --compact

# QCOW2
qemu-img convert -O qcow2 old.qcow2 new.qcow2
```

### Storage Thin Provisioning

```
Scenario: 10 VMs × 100GB disks = 1TB allocated
Actually using: 200GB

Traditional (Thick):
Storage Required: 1TB

Thin Provisioning:
Storage Required: 200GB (grows on-demand)

┌────────────────────────────┐
│  Storage Pool (1TB)        │
│  ┌──────────────────────┐  │
│  │ Used: 200GB          │  │
│  ├──────────────────────┤  │
│  │ Free: 800GB          │  │
│  │ (available for growth)│  │
│  └──────────────────────┘  │
└────────────────────────────┘

Risk: Over-provisioning → Run out of space
Solution: Monitoring + alerts
```

---

## Snapshot e Cloning

### Snapshots

Un **snapshot** cattura lo stato della VM in un momento specifico.

#### Snapshot Architecture

```
Base Disk (myvm.vmdk)
│
├─ Snapshot 1 (Before Update)
│  │ Delta: myvm-000001.vmdk (only changes)
│  │ Memory: myvm-Snapshot1.vmsn (RAM state)
│  │
│  ├─ Snapshot 2 (After Config)
│  │  │ Delta: myvm-000002.vmdk
│  │  │
│  │  └─ Snapshot 3 (Current)
│  │     Delta: myvm-000003.vmdk (running writes here)
│  │
│  └─ Snapshot 2-alt (Alternative path)
│     Delta: myvm-000002-alt.vmdk
```

#### Snapshot Types

**Crash-Consistent Snapshot:**
- VM state not captured
- Like power-off
- Fast, no memory dump

**Memory Snapshot:**
- Includes RAM state
- Can resume VM
- Larger, slower

**Quiesced Snapshot:**
- Guest filesystem sync (via VMware Tools)
- Application-consistent (VSS on Windows)
- Best for databases

#### Create Snapshots

**VMware:**
```bash
# CLI
vim-cmd vmsvc/snapshot.create <vmid> "snapshot1" "Before update" 1 1
# Parameters: includeMemory quiesce

# Revert
vim-cmd vmsvc/snapshot.revert <vmid> 0 0

# Delete
vim-cmd vmsvc/snapshot.remove <vmid> 0 1
```

**VirtualBox:**
```bash
# Create
VBoxManage snapshot "myvm" take "snapshot1" --description "Before update"

# Restore
VBoxManage snapshot "myvm" restore "snapshot1"

# Delete
VBoxManage snapshot "myvm" delete "snapshot1"

# List
VBoxManage snapshot "myvm" list
```

**KVM:**
```bash
# Internal snapshot (inside qcow2)
virsh snapshot-create-as myvm snap1 "Before update"

# External snapshot (separate file)
virsh snapshot-create-as myvm snap1 --disk-only --atomic

# Revert
virsh snapshot-revert myvm snap1

# List
virsh snapshot-list myvm

# Delete
virsh snapshot-delete myvm snap1
```

**Hyper-V:**
```powershell
# Standard checkpoint
Checkpoint-VM -Name "myvm" -SnapshotName "snap1"

# Production checkpoint (VSS)
Checkpoint-VM -Name "myvm" -SnapshotName "snap1" -CheckpointType Production

# Restore
Restore-VMSnapshot -Name "snap1" -VMName "myvm" -Confirm:$false

# Delete
Remove-VMSnapshot -VMName "myvm" -Name "snap1"
```

### Snapshot Best Practices

✅ **Short-lived:** Delete after testing  
✅ **Not backups:** Use proper backup solutions  
✅ **Monitor chains:** Long chains hurt performance  
✅ **Consolidate:** Merge old snapshots  
❌ **Don't run production on snapshots**  

### Cloning

**Cloning** crea una copia completa della VM.

#### Clone Types

**Full Clone:**
```
Source VM (10GB)
     ↓ Full Copy
Clone VM (10GB)

- Complete independent copy
- No dependency on source
- Requires full disk space
```

**Linked Clone:**
```
Base VM (10GB) ← Read-only
     ↓ Reference
Clone VM (delta disk, 100MB)

- Shares base disk
- Only differences stored
- Space efficient
- Fast creation
- Source must remain
```

#### Clone Operations

**VMware:**
```bash
# Full clone via govc
govc vm.clone -vm source-vm -folder /Datacenter/vm clone-vm

# Linked clone (requires snapshot)
govc vm.clone -vm source-vm -folder /Datacenter/vm -linked clone-vm
```

**VirtualBox:**
```bash
# Full clone
VBoxManage clonevm "source" --name "clone" --register

# Linked clone
VBoxManage clonevm "source" --snapshot "snap1" --options link --name "linked-clone" --register
```

**KVM:**
```bash
# Full clone
virt-clone --original source-vm --name clone-vm --auto-clone

# Linked clone (backing file)
qemu-img create -f qcow2 -b /path/to/base.qcow2 -F qcow2 clone.qcow2
```

**Hyper-V:**
```powershell
# Export then Import (full clone)
Export-VM -Name "source" -Path C:\Export
Import-VM -Path C:\Export\source\*.vmcx -Copy -GenerateNewId

# Or copy VHD and create new VM
Copy-Item source.vhdx clone.vhdx
New-VM -Name "clone" -VHDPath clone.vhdx
```

---

## Template di VM

Un **template** è una VM master (golden image) usata come base per deploy rapidi.

### Workflow Template

```
1. Create Base VM
   ↓
2. Install OS + Updates
   ↓
3. Install Software
   ↓
4. Sysprep/Generalize
   ↓
5. Convert to Template
   ↓
6. Deploy VMs from Template
```

### Sysprep (Windows)

Generalizza Windows per clonazione:

```powershell
# Sysprep command
C:\Windows\System32\Sysprep\sysprep.exe /generalize /oobe /shutdown

Effects:
- Remove computer SID
- Remove computer name
- Remove activation
- Reset OOBE (Out-of-Box Experience)
```

### Cloud-Init (Linux)

Configuration management per cloud instances:

```yaml
# cloud-init user-data
#cloud-config
hostname: myvm
fqdn: myvm.example.com
manage_etc_hosts: true

users:
  - name: admin
    groups: sudo
    shell: /bin/bash
    sudo: ['ALL=(ALL) NOPASSWD:ALL']
    ssh_authorized_keys:
      - ssh-rsa AAAAB3N...

packages:
  - nginx
  - git

runcmd:
  - systemctl enable nginx
  - systemctl start nginx
```

### Template Creation

**VMware:**
```bash
# Convert VM to template (requires vCenter)
govc vm.markastemplate /Datacenter/vm/template-vm

# Deploy from template
govc vm.create -template /Datacenter/vm/template-vm new-vm
```

**VirtualBox:**
```bash
# Export as OVA (template format)
VBoxManage export template-vm -o template.ova

# Import
VBoxManage import template.ova --vsys 0 --vmname new-vm
```

**KVM:**
```bash
# Create template directory
mkdir -p /var/lib/libvirt/templates

# Copy and prepare
cp /var/lib/libvirt/images/template.qcow2 /var/lib/libvirt/templates/

# Seal Linux template
virt-sysprep -d template-vm
```

**Hyper-V:**
```powershell
# Export template
Export-VM -Name "template-vm" -Path C:\Templates

# Deploy from template
Import-VM -Path C:\Templates\template-vm\*.vmcx -Copy -GenerateNewId -VhdDestinationPath C:\VMs\new-vm
```

### Template Best Practices

✅ **Minimal install:** Only essential software  
✅ **Updates:** Keep template patched  
✅ **Sysprep/Cloud-init:** Proper generalization  
✅ **Documentation:** Document installed software  
✅ **Versioning:** template-centos7-v1.0, v1.1, etc.  
✅ **Testing:** Validate template deploys correctly  

---

## Conclusioni

L'architettura delle VM è complessa ma modulare:

- **vCPU**: Scheduling, pinning, NUMA awareness
- **Memory**: Overcommitment, ballooning, TPS
- **vNIC**: Vari driver, network modes
- **vDisk**: Formati, thin/thick provisioning
- **Snapshots**: Testing, rollback (non backup!)
- **Cloning**: Rapid deployment
- **Templates**: Golden images standardizzate

Comprendere questi componenti è essenziale per gestire infrastrutture virtualizzate efficientemente.

---

## Domande di Autovalutazione

1. Quali sono i pro e contro del thin provisioning?
2. Spiega la differenza tra snapshot e clone
3. Come funziona il memory ballooning?
4. Quando useresti un linked clone vs full clone?
5. Cos'è il sysprep e perché è necessario?
6. Confronta i formati VMDK, QCOW2, VHDX

---

## Esercizi Pratici

### Lab 1: Snapshot Workflow
1. Crea VM Ubuntu
2. Installa software
3. Snapshot "before-update"
4. Rompi qualcosa
5. Restore snapshot

### Lab 2: Template Creation
1. Installa CentOS pulito
2. Update system
3. Install base tools
4. Generalize (cloud-init)
5. Clone 3 VMs da template

### Lab 3: Disk Management
1. Crea VM con 10GB disk
2. Riempila con dati
3. Expand a 20GB
4. Extend filesystem guest

---

## Risorse Aggiuntive

- [VMware Virtual Machine Files](https://docs.vmware.com/en/VMware-vSphere/7.0/com.vmware.vsphere.vm_admin.doc/GUID-B9B71111-82F1-4C8B-9F76-BF46B84D2D1B.html)
- [QEMU Documentation](https://www.qemu.org/documentation/)
- [VirtualBox Manual - Chapter 5](https://www.virtualbox.org/manual/ch05.html)
- [Hyper-V Documentation](https://docs.microsoft.com/en-us/virtualization/hyper-v-on-windows/)
