# 2.4 Gestione delle Risorse

## Indice
- [CPU Scheduling](#cpu-scheduling)
- [Memory Management](#memory-management)
- [Storage Management](#storage-management)
- [Network Virtualization](#network-virtualization)
- [Resource Pooling e Allocation](#resource-pooling-e-allocation)
- [Quality of Service (QoS)](#quality-of-service-qos)

---

## CPU Scheduling

### Scheduler Hypervisor

L'hypervisor scheduler determina quale vCPU esegue su quale pCPU e quando.

```
Time-slicing:
pCPU 0: [VM1-vCPU0][VM2-vCPU0][VM3-vCPU0][VM1-vCPU0]...
         ←10ms→     ←10ms→     ←10ms→     ←10ms→

Context Switch:
- Save vCPU state
- Load next vCPU state
- Overhead: 1-5%
```

### Scheduling Algorithms

#### 1. Fair Share Scheduling

Risorse CPU distribuite equamente:

```
3 VM con priorità uguale:
├─ VM1: 33.3% CPU
├─ VM2: 33.3% CPU
└─ VM3: 33.3% CPU
```

#### 2. Priority-Based Scheduling

```
VM Priority:
├─ VM1 (High):   50% CPU guaranteed
├─ VM2 (Normal): 30% CPU
└─ VM3 (Low):    20% CPU

Under contention, high priority gets resources first
```

#### 3. Shares-Based Scheduling (VMware)

```
Shares determine relative priority:
├─ VM1: 2000 shares → 50% (2000/4000)
├─ VM2: 1000 shares → 25% (1000/4000)
└─ VM3: 1000 shares → 25% (1000/4000)
         Total: 4000

When idle, unused shares go to active VMs
```

### CPU Reservation & Limits

**Reservation:** Minimum guaranteed CPU
**Limit:** Maximum allowed CPU

```
VM Configuration:
├─ vCPU: 4
├─ Reservation: 2 GHz (guaranteed)
├─ Limit: 6 GHz (max)
└─ Shares: Normal (1000 per vCPU)

Behavior:
- Always gets ≥ 2 GHz
- Never exceeds 6 GHz
- Competes with others between 2-6 GHz based on shares
```

**VMware Example:**
```bash
# Set reservation (MHz)
vim-cmd vmsvc/get.config <vmid>
sched.cpu.min = "2000"  # 2 GHz

# Set limit
sched.cpu.max = "6000"  # 6 GHz

# Set shares
sched.cpu.shares = "normal"  # or "high", "low", or custom number
```

**KVM Example:**
```bash
# CPU shares (cgroups)
virsh schedinfo myvm --set cpu_shares=2048

# CPU period/quota (limit)
virsh schedinfo myvm --set vcpu_period=100000 --set vcpu_quota=200000
# quota/period = 200000/100000 = 2 vCPU max
```

### CPU Affinity

Vincolare VM a specific pCPU:

```
Physical CPUs: 0-15

VM1 affinity: 0-7   (NUMA node 0)
VM2 affinity: 8-15  (NUMA node 1)

Benefits:
- NUMA locality
- Predictable performance
- Isolation for critical workloads
```

**Set Affinity:**
```bash
# VMware
sched.cpu.affinity = "0,1,2,3"  # in .vmx

# KVM
virsh vcpupin myvm 0 0-3  # vCPU 0 pins to pCPU 0-3
virsh vcpupin myvm 1 0-3  # vCPU 1 pins to pCPU 0-3

# Check
virsh vcpupin myvm
```

### Co-Scheduling

Per vCPU multiple, scheduler cerca di eseguirle simultaneamente:

```
VM with 4 vCPU ideally runs on 4 pCPU simultaneously:

pCPU 0: [vCPU0]
pCPU 1: [vCPU1]  ← All 4 running together
pCPU 2: [vCPU2]     (co-scheduled)
pCPU 3: [vCPU3]

Benefits: No vCPU waiting for others
Issue: Hard with many VMs (skew problem)
```

---

## Memory Management

### Memory Allocation

```
Memory Hierarchy:

VM Configured Memory: 8 GB  ← What guest sees
          ↓
Active Memory: 4 GB         ← Actually used
          ↓
Host Physical Memory: 3 GB  ← After compression/dedup
          ↓
Swap: 1 GB                  ← Swapped to disk
```

### Memory Reservation

**Reservation:** Guaranteed physical RAM

```
Server with 64 GB RAM:
├─ VM1: 16 GB allocated, 8 GB reserved
├─ VM2: 16 GB allocated, 0 GB reserved
├─ VM3: 32 GB allocated, 16 GB reserved
└─ Available reservable: 64 - 24 = 40 GB

VM1 always gets 8 GB physical RAM
VM2 may get swapped under pressure
VM3 always gets 16 GB physical RAM
```

**Configuration:**
```bash
# VMware
sched.mem.min = "8192"  # MB reserved

# KVM (via numatune/cgroups)
virsh memtune myvm --hard-limit 8388608  # KB
```

### Memory Shares

Relative priority during contention:

```
Under memory pressure:
├─ VM1: 2000 shares → 50% (2000/4000)
├─ VM2: 1000 shares → 25%
└─ VM3: 1000 shares → 25%
```

### Memory Limit

Maximum memory VM can consume:

```
VM: 16 GB configured
Limit: 8 GB

VM sees 16 GB but hypervisor enforces 8 GB max
Use case: Overcommitment control, cost management
```

### Memory Reclamation Techniques

#### Priority Order:
1. **Transparent Page Sharing (TPS)** - Deduplicate (best performance)
2. **Ballooning** - Guest cooperates (good performance)
3. **Compression** - Compress pages in RAM (medium performance)
4. **Swapping** - Swap to disk (worst performance)

```
Memory Pressure Response:
┌────────────────────────────┐
│ Free memory > 6%           │ → No action
├────────────────────────────┤
│ Free memory 6%-4%          │ → TPS active
├────────────────────────────┤
│ Free memory 4%-2%          │ → Ballooning starts
├────────────────────────────┤
│ Free memory 2%-1%          │ → Compression active
├────────────────────────────┤
│ Free memory < 1%           │ → Swapping (last resort)
└────────────────────────────┘
```

### Large Pages / Huge Pages

Normal pages: 4 KB
Large/Huge pages: 2 MB (x86) or 1 GB

**Benefits:**
- Reduced TLB misses
- Lower memory management overhead
- Better performance (5-10% for memory-intensive)

**Configuration:**
```bash
# Linux host: Enable huge pages
echo 1024 > /proc/sys/vm/nr_hugepages  # 2GB (1024 × 2MB)

# KVM: Use huge pages
<memoryBacking>
  <hugepages/>
</memoryBacking>

# VMware: Automatic large pages
sched.mem.lpage.enable = "TRUE"
```

---

## Storage Management

### Storage Allocation Models

#### 1. Thick Provisioning

```
Allocate full capacity upfront:

10 VMs × 100 GB = 1000 GB required
┌──────────────────────────────┐
│ Storage: 1000 GB allocated   │
│ ▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░│
│ Used: 400 GB                 │
│ Wasted: 600 GB (allocated but unused)
└──────────────────────────────┘
```

#### 2. Thin Provisioning

```
Allocate on-demand:

10 VMs × 100 GB = 1000 GB promised
┌──────────────────────────────┐
│ Storage: 400 GB allocated    │
│ ▓▓▓▓▓▓▓▓▓▓                   │
│ Used: 400 GB                 │
│ Saved: 600 GB                │
└──────────────────────────────┘

Risk: Over-provisioning
→ Monitor and set alerts
```

### Storage Tiering

```
Performance Tiers:

Hot Tier (SSD/NVMe):
├─ OS disks
├─ Database transaction logs
└─ High IOPS workloads

Warm Tier (SAS):
├─ General purpose VMs
├─ Application data
└─ Medium IOPS

Cold Tier (SATA):
├─ Archives
├─ Backups
└─ Low IOPS
```

### Storage I/O Control (SIOC)

Prioritize storage bandwidth:

```
Under contention:
├─ Production VMs: High priority (more IOPS)
├─ Dev VMs: Normal priority
└─ Test VMs: Low priority (throttled)

SIOC monitors latency:
If latency > 30ms → activate QoS
Distribute IOPS based on shares
```

**VMware SIOC:**
```
Datastore → Configure → Storage I/O Control
- Enable SIOC
- Latency threshold: 30 ms
- VM shares: High/Normal/Low
```

### Storage Policies

```
Policy: Gold
├─ RAID 10 (performance)
├─ SSD storage
├─ Replication: 3 copies
└─ Backup: Daily

Policy: Silver
├─ RAID 5
├─ SAS storage
├─ Replication: 2 copies
└─ Backup: Weekly

Policy: Bronze
├─ RAID 6
├─ SATA storage
├─ No replication
└─ Backup: Monthly
```

---

## Network Virtualization

### Virtual Switch Architecture

```
┌─────────────────────────────────────┐
│        Virtual Switch               │
│  ┌─────────────────────────────┐   │
│  │  Port Groups                │   │
│  │  ┌──────┐ ┌──────┐ ┌──────┐│   │
│  │  │ VM1  │ │ VM2  │ │ VM3  ││   │
│  │  └───┬──┘ └───┬──┘ └───┬──┘│   │
│  └──────┼────────┼────────┼────┘   │
│  ┌──────▼────────▼────────▼────┐   │
│  │   Switching/Forwarding      │   │
│  │   - MAC learning            │   │
│  │   - VLAN tagging            │   │
│  │   - Security policies       │   │
│  └──────┬──────────────────────┘   │
│         │                          │
└─────────┼──────────────────────────┘
          │
    ┌─────▼──────┐
    │Physical NIC│
    └────────────┘
```

### VLAN Configuration

```
Virtual Switch with VLANs:

┌───────────────────────────────┐
│  Port Group "Production"      │
│  VLAN 100                     │
│  ├─ VM1 (Web)                 │
│  └─ VM2 (App)                 │
├───────────────────────────────┤
│  Port Group "Database"        │
│  VLAN 200                     │
│  ├─ VM3 (DB Primary)          │
│  └─ VM4 (DB Standby)          │
├───────────────────────────────┤
│  Port Group "Management"      │
│  VLAN 10                      │
│  └─ Hypervisor mgmt          │
└───────────────────────────────┘
```

**VMware Configuration:**
```bash
# Create port group with VLAN
esxcli network vswitch standard portgroup add --portgroup-name=Production --vswitch-name=vSwitch0
esxcli network vswitch standard portgroup set --portgroup-name=Production --vlan-id=100
```

### Network I/O Control (NIOC)

Prioritize network bandwidth:

```
10 Gbps uplink:

├─ vMotion traffic: 40% (4 Gbps) reserved
├─ VM traffic: 30% (3 Gbps)
├─ Management: 10% (1 Gbps)
├─ Storage (NFS): 10% (1 Gbps)
└─ Fault Tolerance: 10% (1 Gbps)

Under contention, shares determine distribution
```

### Traffic Shaping

Limit bandwidth per VM:

```
VM1:
├─ Average: 100 Mbps
├─ Peak: 200 Mbps (burst)
└─ Burst size: 100 MB

Traffic pattern:
 200 Mbps │     ██
          │    █  █
 100 Mbps │████    ████████
          └──────────────── Time
             ↑ Burst allowed briefly
```

**Configuration:**
```bash
# VMware traffic shaping (outbound)
esxcli network vswitch standard policy shaping set \
  --enabled=true \
  --avg-bandwidth=100000 \    # 100 Mbps in Kbps
  --peak-bandwidth=200000 \   # 200 Mbps
  --burst-size=102400 \       # 100 MB in KB
  --vswitch-name=vSwitch0
```

---

## Resource Pooling e Allocation

### Resource Pools

Raggruppamento gerarchico per gestire risorse:

```
Cluster Resources (Total):
CPU: 320 GHz, RAM: 2 TB

├─ Production Pool (70%)
│  ├─ CPU: 224 GHz
│  ├─ RAM: 1.4 TB
│  │
│  ├─ Web Tier Pool (40%)
│  │  └─ VM1, VM2, VM3
│  │
│  └─ DB Tier Pool (60%)
│     └─ VM4, VM5
│
└─ Dev/Test Pool (30%)
   ├─ CPU: 96 GHz
   ├─ RAM: 600 GB
   │
   ├─ Dev Pool (50%)
   │  └─ VM6, VM7
   │
   └─ Test Pool (50%)
      └─ VM8, VM9, VM10
```

### Shares, Reservation, Limits at Pool Level

```
Production Pool:
├─ Reservation: 200 GHz CPU, 1 TB RAM (guaranteed)
├─ Limit: None (can use all if available)
└─ Shares: 8000 (high priority)

Dev Pool:
├─ Reservation: 0 (no guarantee)
├─ Limit: 100 GHz CPU, 500 GB RAM (capped)
└─ Shares: 2000 (low priority)
```

### Admission Control

Previene over-provisioning:

```
Cluster: 320 GHz available
Reserved: 250 GHz (by VMs/pools)

Attempt to power on VM needing 100 GHz:
→ 250 + 100 = 350 > 320
→ DENIED (insufficient resources)

Must free resources or reduce reservation
```

**VMware HA Admission Control:**
```
Policy: Host Failures Tolerated = 1

Cluster must reserve enough to handle 1 host failure:
- 5 hosts × 64 GHz = 320 GHz total
- Must keep 64 GHz free for failover
- Max committable: 256 GHz
```

---

## Quality of Service (QoS)

### Multi-Dimensional QoS

```
┌─────────────────────────────────┐
│         VM Priority             │
├─────────────────────────────────┤
│  CPU    Memory   Storage   Net  │
│  High   High     High      High │  ← Mission-critical
│  Normal Normal   Normal    Normal│  ← Standard
│  Low    Low      Low       Low  │  ← Dev/Test
└─────────────────────────────────┘
```

### SLA-Based Resource Allocation

```
SLA Tier 1 (99.99% uptime):
├─ CPU reservation: 100%
├─ Memory reservation: 100%
├─ Storage: SSD, RAID 10
├─ Network: 10 Gbps dedicated
└─ HA: Immediate failover

SLA Tier 2 (99.9% uptime):
├─ CPU reservation: 50%
├─ Memory reservation: 50%
├─ Storage: SAS, RAID 5
├─ Network: 1 Gbps shared
└─ HA: 5-minute RTO

SLA Tier 3 (Best effort):
├─ CPU reservation: 0%
├─ Memory reservation: 0%
├─ Storage: SATA, RAID 6
├─ Network: Shared
└─ HA: Manual restart
```

### Monitoring e Enforcement

```
Monitoring Metrics:
├─ CPU: Usage %, Ready time, Co-stop
├─ Memory: Active, Consumed, Ballooning, Swapping
├─ Storage: IOPS, Latency, Throughput
└─ Network: Throughput, Packet loss, Latency

Alerts:
├─ CPU usage > 80% for 10 min → Scale-up
├─ Memory swapping > 0 → Add RAM
├─ Storage latency > 20ms → Move to faster tier
└─ Network drops > 1% → Check bandwidth
```

**VMware vRealize Operations:**
- Automated workload optimization
- Right-sizing recommendations
- Capacity planning
- Performance troubleshooting

---

## Best Practices

### CPU Management
✅ Don't over-allocate vCPU (start small, grow as needed)  
✅ Monitor CPU ready time (>5% = contention)  
✅ Use reservations sparingly (reduces flexibility)  
✅ Consider NUMA boundaries for large VMs  

### Memory Management
✅ Right-size VMs (unused RAM is wasted)  
✅ Enable memory ballooning (install VMware Tools)  
✅ Set reservations only for critical VMs  
✅ Monitor swapping (should be 0 in production)  
✅ Use large pages for memory-intensive workloads  

### Storage Management
✅ Use thin provisioning with monitoring  
✅ Place VMs on appropriate storage tiers  
✅ Enable SIOC for shared datastores  
✅ Monitor IOPS and latency  
✅ Plan for growth (75% capacity max)  

### Network Management
✅ Separate traffic types (vMotion, management, VM)  
✅ Use VLANs for isolation  
✅ Enable NIOC on shared uplinks  
✅ Monitor bandwidth utilization  
✅ Consider SR-IOV for high-performance workloads  

---

## Conclusioni

La gestione delle risorse in ambienti virtualizzati richiede:

- **Bilanciamento**: Performance vs over-provisioning
- **Monitoring**: Continuous visibility
- **Automation**: Dynamic resource allocation
- **Planning**: Capacity management
- **Prioritization**: QoS for critical workloads

Gli hypervisor moderni forniscono strumenti sofisticati per ottimizzare l'utilizzo delle risorse mantenendo SLA.

---

## Domande di Autovalutazione

1. Spiega la differenza tra reservation e limit
2. Come funziona il sistema di shares in VMware?
3. Quali sono le tecniche di memory reclamation in ordine di preferenza?
4. Quando useresti CPU affinity?
5. Cos'è SIOC e quando è utile?
6. Descrivi lo scenario in cui admission control blocca il power-on di una VM

---

## Esercizi Pratici

### Lab 1: Resource Contention
1. Crea 3 VM su host con risorse limitate
2. Configura shares diverse (high, normal, low)
3. Genera carico CPU
4. Monitora distribuzione risorse

### Lab 2: Memory Management
1. Crea VM con 4GB RAM
2. Genera memory pressure
3. Osserva ballooning
4. Confronta con e senza ballooning driver

---

## Risorse Aggiuntive

- [VMware Resource Management Guide](https://docs.vmware.com/en/VMware-vSphere/7.0/vsphere-esxi-vcenter-server-70-resource-management-guide.pdf)
- [KVM Resource Management](https://www.linux-kvm.org/page/TuningKVM)
- [Hyper-V Resource Controls](https://docs.microsoft.com/en-us/windows-server/virtualization/hyper-v/manage/manage-hyper-v-minroot-2016)
