# 2.8 Performance e Ottimizzazione

## Indice
- [Overhead della Virtualizzazione](#overhead-della-virtualizzazione)
- [Performance Tuning](#performance-tuning)
- [Right-Sizing delle VM](#right-sizing-delle-vm)
- [NUMA (Non-Uniform Memory Access)](#numa-non-uniform-memory-access)
- [SR-IOV (Single Root I/O Virtualization)](#sr-iov-single-root-io-virtualization)
- [GPU Virtualization](#gpu-virtualization)

---

## Overhead della Virtualizzazione

### Sources of Overhead

```
Performance Impact Chain:

Application
    ↓ ~1-2%
Guest OS
    ↓ ~2-3%
Virtual Hardware Layer
    ↓ ~2-5%
Hypervisor
    ↓ ~1-2%
Physical Hardware

Total typical overhead: 5-10%
Optimized (with hardware assist): 1-3%
```

### Benchmarking: Native vs VM

**CPU Performance:**
```
Benchmark: SPEC CPU2017

Native (Bare-Metal):     100% (baseline)
VMware ESXi (hardware virt):  97-98%
KVM (with KVM):               96-98%
Xen HVM:                      95-97%
VirtualBox (software):        85-90%

Modern hypervisors with VT-x/AMD-V approach native performance
```

**Memory Performance:**
```
Benchmark: STREAM (memory bandwidth)

Native:              100% (50 GB/s)
VM with EPT/NPT:     95-97% (47-48 GB/s)
VM without EPT/NPT:  70-80% (35-40 GB/s)

Hardware-assisted memory virt is critical!
```

**I/O Performance:**
```
Storage IOPS:
Native NVMe:         500,000 IOPS
Paravirt (virtio):   450,000 IOPS (90%)
Emulated (IDE):      10,000 IOPS (2%)

Network Throughput:
Native 10GbE:        9.8 Gbps
SR-IOV:              9.5 Gbps (97%)
Paravirt (virtio):   8.5 Gbps (87%)
Emulated (e1000):    1.2 Gbps (12%)

Lesson: Use paravirtualized or SR-IOV drivers!
```

### CPU Overhead Components

#### 1. Context Switching

```
VM Exit (guest → hypervisor):
1. Save guest state (registers, flags)
2. Switch to hypervisor mode
3. Handle event (I/O, interrupt, exception)
4. Restore guest state
5. VM Entry (hypervisor → guest)

Time: 500-2000 CPU cycles (~200-800ns)
Frequent exits = performance degradation
```

**Reducing VM Exits:**
```
Techniques:
✅ Paravirtualization (reduce traps)
✅ Hardware virtualization (VT-x handles more in hardware)
✅ APIC virtualization (interrupt handling)
✅ Posted interrupts (direct interrupt injection)
```

#### 2. Memory Management Overhead

**Shadow Page Tables (legacy, without EPT/NPT):**
```
Guest Virtual → Guest Physical → Host Physical

Hypervisor maintains shadow page tables:
- Track guest page table updates
- Many VM exits
- High overhead (20-30%)

Solution: EPT/NPT (hardware two-level translation)
- Single-pass translation
- Minimal VM exits
- Overhead <3%
```

#### 3. I/O Overhead

```
Emulated I/O:
Guest → Trap → Hypervisor → Emulate device → Hardware
                  ↑ VM exits, context switches

Paravirtualized I/O:
Guest → Driver knows it's virtualized → Hypervisor → Hardware
        (fewer traps, batch operations)

Direct Assignment (SR-IOV):
Guest → Hardware (DMA directly)
        (minimal overhead)
```

---

## Performance Tuning

### Hypervisor Configuration

#### VMware vSphere Tuning

**CPU Configuration:**
```ini
# .vmx file optimizations

# CPU latency sensitivity (for real-time workloads)
sched.cpu.latencySensitivity = "high"
# Reserves CPU, reduces scheduling latency
# Use sparingly (limits host utilization)

# Disable side-channel mitigations if not needed (security vs performance)
ulm.disableMitigations = "TRUE"
# WARNING: Only for trusted environments

# CPU affinity for NUMA
sched.cpu.affinity = "0,1,2,3"  # Pin to specific cores

# Expose hardware CPU to guest
cpuid.coresPerSocket = "4"  # Match physical topology
```

**Memory Configuration:**
```ini
# Reserve all memory (no swapping)
sched.mem.min = "8192"  # MB (equal to memSize)
sched.mem.minSize = "8192"

# Disable memory ballooning for critical VMs
sched.mem.maxmemctl = "0"

# Use large pages (2MB instead of 4KB)
sched.mem.lpage.maxSharedPages = "unlimited"

# Disable page sharing for performance-critical VMs
sched.mem.pshare.enable = "FALSE"
```

**Storage Configuration:**
```ini
# Use paravirtual SCSI adapter (best performance)
scsi0.virtualDev = "pvscsi"

# Disable disk shrinking (performance hit)
isolation.tools.diskShrink.disable = "TRUE"
isolation.tools.diskWiper.disable = "TRUE"

# Async I/O for better throughput
scsi0:0.ctkEnabled = "FALSE"  # Disable change tracking if not needed
```

**Network Configuration:**
```ini
# Use VMXNET3 (paravirtualized, best performance)
ethernet0.virtualDev = "vmxnet3"

# Enable jumbo frames (9000 MTU)
ethernet0.mtu = "9000"

# Disable LRO/TSO if issues
ethernet0.disableLro = "FALSE"  # Keep enabled for performance
```

#### KVM/QEMU Tuning

**CPU Pinning and Isolation:**
```bash
# Pin vCPUs to specific pCPUs
virsh vcpupin myvm 0 4
virsh vcpupin myvm 1 5
virsh vcpupin myvm 2 6
virsh vcpupin myvm 3 7

# Isolate CPUs for VM (edit /etc/default/grub)
GRUB_CMDLINE_LINUX="isolcpus=4-7 nohz_full=4-7 rcu_nocbs=4-7"
update-grub && reboot

# CPU governor (performance mode)
cpupower frequency-set -g performance
```

**Memory Configuration:**
```xml
<!-- Huge pages for better TLB efficiency -->
<memoryBacking>
  <hugepages>
    <page size='2048' unit='KiB'/>  <!-- 2MB pages -->
  </hugepages>
  <locked/>  <!-- Lock in RAM, no swapping -->
</memoryBacking>

<!-- NUMA node pinning -->
<numatune>
  <memory mode='strict' nodeset='0'/>
  <memnode cellid='0' mode='strict' nodeset='0'/>
</numatune>
```

**Virtio Optimizations:**
```xml
<!-- Disk with optimal settings -->
<disk type='file' device='disk'>
  <driver name='qemu' type='qcow2' cache='none' io='native' discard='unmap'/>
  <source file='/var/lib/libvirt/images/vm.qcow2'/>
  <target dev='vda' bus='virtio'/>
  <driver name='qemu' type='qcow2'>
    <driver queues='4'/>  <!-- Multi-queue -->
  </driver>
</disk>

<!-- Network with multi-queue -->
<interface type='bridge'>
  <model type='virtio'/>
  <driver name='vhost' queues='4'/>  <!-- Match vCPU count -->
</interface>
```

**Enable vhost-net (kernel-space virtio):**
```bash
# Load vhost-net module
modprobe vhost-net

# Verify
lsmod | grep vhost
```

#### Hyper-V Tuning

**PowerShell Optimizations:**
```powershell
# Set VM to high priority
Set-VMProcessor -VMName "MyVM" -CompatibilityForMigrationEnabled $false
Set-VMProcessor -VMName "MyVM" -CompatibilityForOlderOperatingSystemsEnabled $false

# Reserve memory (no dynamic memory for performance-critical)
Set-VMMemory -VMName "MyVM" -DynamicMemoryEnabled $false -StartupBytes 8GB

# Enable NUMA spanning if needed (generally disable for performance)
Set-VMMemory -VMName "MyVM" -NumaSpanningEnabled $false

# Optimize network
Set-VMNetworkAdapter -VMName "MyVM" -VrssEnabled $true  # Virtual RSS
Set-VMNetworkAdapter -VMName "MyVM" -VmmqEnabled $true  # Virtual MMQ

# Disable unnecessary integration services for performance
Disable-VMIntegrationService -VMName "MyVM" -Name "Time Synchronization"
```

### Guest OS Tuning

#### Linux Guest

**CPU Tuning:**
```bash
# Install VM-specific kernel
apt-get install linux-generic-hwe-20.04

# CPU governor
cpupower frequency-set -g performance

# Disable CPU mitigations (if safe)
# /etc/default/grub
GRUB_CMDLINE_LINUX="mitigations=off"

# IRQ affinity (pin interrupts to specific CPUs)
echo 2 > /proc/irq/30/smp_affinity  # Pin IRQ 30 to CPU 1
```

**Memory Tuning:**
```bash
# Disable zone reclaim (better for NUMA)
echo 0 > /proc/sys/vm/zone_reclaim_mode

# Transparent huge pages
echo always > /sys/kernel/mm/transparent_hugepage/enabled
echo always > /sys/kernel/mm/transparent_hugepage/defrag

# Swappiness (less swapping)
sysctl vm.swappiness=10

# Dirty ratio (writeback tuning)
sysctl vm.dirty_ratio=10
sysctl vm.dirty_background_ratio=5
```

**Network Tuning:**
```bash
# Increase buffers
sysctl -w net.core.rmem_max=134217728
sysctl -w net.core.wmem_max=134217728
sysctl -w net.ipv4.tcp_rmem="4096 87380 134217728"
sysctl -w net.ipv4.tcp_wmem="4096 65536 134217728"

# Enable TCP window scaling
sysctl -w net.ipv4.tcp_window_scaling=1

# Congestion control (BBR for better throughput)
sysctl -w net.ipv4.tcp_congestion_control=bbr

# Multi-queue NIC (ethtool)
ethtool -L eth0 combined 4  # 4 queues
```

**Storage Tuning:**
```bash
# I/O scheduler (for virtio-blk, use none or mq-deadline)
echo none > /sys/block/vda/queue/scheduler

# Disable barriers (if safe, e.g., battery-backed cache)
# mount -o nobarrier /dev/vda1 /mnt

# Increase read-ahead
blockdev --setra 8192 /dev/vda

# Disable NUMA balancing for databases
echo 0 > /proc/sys/kernel/numa_balancing
```

#### Windows Guest

**Power Plan:**
```powershell
# Set to High Performance
powercfg /setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c

# Disable USB selective suspend
powercfg /setacvalueindex 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0

# Disable PCI Express link state power management
powercfg /setacvalueindex 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c 501a4d13-42af-4429-9fd1-a8218c268e20 ee12f906-d277-404b-b6da-e5fa1a576df5 0
```

**Network Tuning:**
```powershell
# Disable offloads if problematic (usually keep enabled)
Set-NetAdapterAdvancedProperty -Name "Ethernet" -DisplayName "Large Send Offload V2 (IPv4)" -DisplayValue "Enabled"

# RSS (Receive Side Scaling)
Set-NetAdapterRss -Name "Ethernet" -Enabled $true -NumberOfReceiveQueues 4
```

**Storage:**
```
- Use NTFS for best performance
- Disable defragmentation on VM disks
- Disable 8.3 name creation: fsutil behavior set disable8dot3 1
```

---

## Right-Sizing delle VM

### Over-Provisioning Problems

```
Symptom: VM configured with 8 vCPU, but only using 10%

Problems:
├─ CPU scheduling overhead (8 vCPU to schedule)
├─ Co-scheduling skew (waiting for all 8 vCPU)
├─ Larger memory footprint
├─ Higher licensing costs
└─ Wasted resources (could be used by other VMs)

Solution: Right-size to 2 vCPU
Result: Better performance, lower costs
```

### CPU Right-Sizing

**Metrics to Monitor:**
```
Key metrics:
├─ CPU Utilization: Should avg 40-60% (peaks to 80% OK)
├─ CPU Ready Time: Should be <5% (waiting for physical CPU)
├─ CPU Co-Stop: Should be <3% (waiting for other vCPUs)
└─ CPU Steal: Should be <5% (Linux, time stolen by hypervisor)

VMware:
esxtop → CPU view
- %RDY: Ready time (high = overcommit)
- %CSTP: Co-stop time (high = too many vCPU)

Linux:
top, mpstat, sar -u
- %steal: Time waiting for CPU

Windows:
Performance Monitor
- Processor Queue Length (should be <2 per vCPU)
```

**Right-Sizing Process:**
```
1. Monitor for 2-4 weeks (capture workload patterns)
2. Analyze peak vs average usage
3. Decision:
   - Avg <20%: Reduce vCPU
   - Avg 40-60%: Good
   - Avg >80%: Increase vCPU
4. Make changes during maintenance window
5. Monitor post-change
```

**Example:**
```
Before:
VM: 8 vCPU, 32 GB RAM
Avg CPU: 15%
CPU Ready: 12% (high contention)

After:
VM: 4 vCPU, 32 GB RAM
Avg CPU: 30%
CPU Ready: 2% (excellent)

Result: Better performance, freed 4 vCPU for other VMs
```

### Memory Right-Sizing

**Metrics:**
```
Key metrics:
├─ Active Memory: Actually used by guest (not just allocated)
├─ Consumed Memory: Host physical RAM allocated
├─ Balloon: Memory reclaimed by balloon driver
├─ Swapped: Memory swapped by hypervisor (BAD)
└─ Swap In Rate: MB/s swapped in (should be 0)

VMware:
- Active memory: What guest is using
- If Active < 50% of allocated: Over-provisioned
- If Ballooning > 0: Under-provisioned or host memory pressure

Linux:
free -h
- available: Real available memory
- buff/cache: Can be reclaimed

Windows:
Task Manager → Performance → Memory
- Available: Free memory
- Committed: Actual usage
```

**Right-Sizing:**
```
Rule of Thumb:
- Allocate: Active + 20% buffer
- Monitor swap activity (should be 0)

Example:
Current: 16 GB allocated
Active: 6 GB
Recommendation: 8 GB (6 GB + 33% buffer)

Before change: Ensure no memory pressure
After change: Monitor for swapping
```

### Storage Right-Sizing

**Metrics:**
```
Key metrics:
├─ IOPS: I/O operations per second
├─ Latency: Response time (ms)
├─ Throughput: MB/s
└─ Queue Depth: Pending I/O

Thresholds:
- Latency <10ms: Excellent
- Latency 10-20ms: Good
- Latency 20-50ms: Warning (check workload)
- Latency >50ms: Critical (bottleneck)
```

**Optimization:**
```
Solutions for high latency:
1. Move to faster storage tier (SATA → SAS → SSD → NVMe)
2. Increase IOPS allocation (if QoS limited)
3. Reduce queue depth if too high
4. Use paravirt drivers (virtio-scsi)
5. Check for storage contention (other VMs)
```

---

## NUMA (Non-Uniform Memory Access)

### NUMA Architecture

```
Modern servers with multiple CPU sockets:

Node 0:                      Node 1:
┌────────────────┐           ┌────────────────┐
│  CPU 0 (8 core)│           │  CPU 1 (8 core)│
│  ┌──────────┐  │           │  ┌──────────┐  │
│  │L3 Cache  │  │           │  │L3 Cache  │  │
│  └──────────┘  │           │  └──────────┘  │
│  ┌──────────┐  │           │  ┌──────────┐  │
│  │ Memory   │  │           │  │ Memory   │  │
│  │ 64 GB    │  │           │  │ 64 GB    │  │
│  └──────────┘  │           │  └──────────┘  │
└────────┬───────┘           └────────┬───────┘
         └──────────────┬──────────────┘
                  Interconnect (QPI/UPI)

Access Latency:
- Local (same node): 100ns
- Remote (different node): 300ns (3x slower!)

Problem for VMs:
VM spans both nodes → 50% remote memory access → performance degradation
```

### NUMA Best Practices

**Keep VM within single NUMA node:**
```
Server: 2 NUMA nodes, 16 cores, 128 GB each

Good:
VM1: 8 vCPU, 64 GB → Fits in Node 0
VM2: 8 vCPU, 64 GB → Fits in Node 1

Bad:
VM3: 20 vCPU, 180 GB → Spans both nodes
     Remote memory access = performance hit
```

**VMware NUMA Configuration:**
```
Automatic NUMA scheduling (default):
- vNUMA presents NUMA topology to guest
- Guest OS can optimize for NUMA
- Best for large VMs (>8 vCPU)

Disable vNUMA for small VMs:
numa.vcpu.min = "9"  # Only enable vNUMA if VM has 9+ vCPU

Manual NUMA affinity:
numa.nodeAffinity = "0"  # Pin VM to NUMA node 0
```

**Check NUMA configuration:**
```bash
# VMware (esxtop)
esxtop → m (memory) → f (fields) → N (NUMA stats)
- NHN: NUMA home node
- NMIG: NUMA migrations (should be low)
- NRMEM: Remote memory % (should be <10%)

# Linux guest
numactl --hardware
numastat -p <pid>

# Windows guest
# Task Manager → Performance → CPU → NUMA nodes
```

**KVM NUMA Configuration:**
```xml
<!-- Define NUMA topology -->
<cpu mode='host-passthrough'>
  <topology sockets='2' cores='4' threads='1'/>
  <numa>
    <cell id='0' cpus='0-3' memory='32' unit='GiB'/>
    <cell id='1' cpus='4-7' memory='32' unit='GiB'/>
  </numa>
</cpu>

<!-- Pin to specific NUMA node -->
<numatune>
  <memory mode='strict' nodeset='0'/>
</numatune>
```

---

## SR-IOV (Single Root I/O Virtualization)

### Performance Benefits

```
Benchmark: 10GbE Network Performance

Emulated e1000:
- Throughput: 1.2 Gbps
- CPU usage: 95%
- Latency: 200 μs

Paravirt virtio-net:
- Throughput: 8.5 Gbps
- CPU usage: 45%
- Latency: 20 μs

SR-IOV VF:
- Throughput: 9.8 Gbps (line rate)
- CPU usage: 5%
- Latency: 2 μs

SR-IOV = 8x throughput, 20x lower CPU, 100x lower latency
```

### Configuration

**Enable SR-IOV (Intel X710 NIC):**
```bash
# Check NIC supports SR-IOV
lspci -vvv | grep -i sriov
SR-IOV Cap: VFEn+ ARI+ NumVFs=64

# Enable VFs
echo 8 > /sys/class/net/ens1f0/device/sriov_numvfs

# Verify
ip link show
# Should see ens1f0v0, ens1f0v1, ... (Virtual Functions)

# Persistent (udev rule)
cat > /etc/udev/rules.d/80-sriov.rules <<EOF
ACTION=="add", SUBSYSTEM=="net", ENV{ID_NET_DRIVER}=="i40e", ATTR{device/sriov_numvfs}="8"
EOF
```

**Assign VF to VM (KVM):**
```xml
<interface type='hostdev' managed='yes'>
  <source>
    <address type='pci' domain='0x0000' bus='0x04' slot='0x10' function='0x1'/>
  </source>
  <mac address='52:54:00:12:34:56'/>
</interface>
```

**VMware SR-IOV:**
```
1. Enable SR-IOV in BIOS
2. Enable on vSwitch:
   esxcli network sriovnic vf add -n ens1f0 -v 8

3. Edit VM settings:
   - Network Adapter → Adapter Type → SR-IOV passthrough

4. Assign VF to VM
```

**Limitations:**
```
❌ No vMotion (VF tied to physical NIC)
❌ Limited VFs per NIC (32-256 depending on model)
❌ Requires guest driver support
❌ Lose some visibility (vSwitch features don't apply)
```

**Use Cases:**
```
✅ High-frequency trading (ultra-low latency)
✅ Network-intensive workloads (video streaming, CDN)
✅ NFV (Network Function Virtualization)
✅ Storage over network (NFS, iSCSI) performance-critical
❌ Not for VMs that need live migration
❌ Not needed for typical web/app servers
```

---

## GPU Virtualization

### GPU Virtualization Methods

#### 1. GPU Passthrough (Full Device)

```
Concept: Assign entire GPU to one VM

┌────────────────────┐
│       VM 1         │
│                    │
│   ┌────────────┐   │
│   │ GPU Driver │   │
│   └──────┬─────┘   │
└──────────┼─────────┘
           │ Direct access
┌──────────▼─────────┐
│   Physical GPU     │
│   (NVIDIA RTX A4000)
└────────────────────┘

Pros:
✅ Native performance (100%)
✅ Full GPU features
✅ Simple configuration

Cons:
❌ One GPU = one VM (no sharing)
❌ No live migration
❌ Expensive (need GPU per VM)
```

**Configuration (KVM):**
```xml
<!-- GPU passthrough -->
<hostdev mode='subsystem' type='pci' managed='yes'>
  <source>
    <address domain='0x0000' bus='0x01' slot='0x00' function='0x0'/>
  </source>
</hostdev>

<!-- Also need to pass GPU audio device -->
<hostdev mode='subsystem' type='pci' managed='yes'>
  <source>
    <address domain='0x0000' bus='0x01' slot='0x00' function='0x1'/>
  </source>
</hostdev>
```

**Enable IOMMU:**
```bash
# /etc/default/grub
GRUB_CMDLINE_LINUX="intel_iommu=on iommu=pt"

update-grub
reboot

# Bind GPU to vfio-pci driver (unbind from host)
echo "10de 2204" > /sys/bus/pci/drivers/vfio-pci/new_id
echo "0000:01:00.0" > /sys/bus/pci/devices/0000:01:00.0/driver/unbind
echo "0000:01:00.0" > /sys/bus/pci/drivers/vfio-pci/bind
```

#### 2. GPU Partitioning (vGPU)

```
Concept: Share one GPU among multiple VMs

┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  VM 1  │ │  VM 2  │ │  VM 3  │ │  VM 4  │
│ vGPU   │ │ vGPU   │ │ vGPU   │ │ vGPU   │
│ (4GB)  │ │ (4GB)  │ │ (4GB)  │ │ (4GB)  │
└───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
    └──────────┴──────────┴──────────┘
              │ vGPU Manager
┌─────────────▼──────────────┐
│   Physical GPU (16GB)      │
│   (NVIDIA Tesla V100)      │
└────────────────────────────┘

Pros:
✅ Multiple VMs share GPU
✅ Live migration possible
✅ Cost-effective

Cons:
❌ Performance overhead (~10-20%)
❌ Requires enterprise GPU (Tesla, A-series)
❌ License cost (NVIDIA GRID)
```

**NVIDIA vGPU Profiles:**
```
Tesla V100 (16GB) vGPU profiles:

V100-16Q:  1 VM  × 16GB (full GPU)
V100-8Q:   2 VMs × 8GB  (1/2 GPU each)
V100-4Q:   4 VMs × 4GB  (1/4 GPU each)
V100-2Q:   8 VMs × 2GB  (1/8 GPU each)
V100-1Q:  16 VMs × 1GB  (1/16 GPU each)

Use cases:
- 16Q: ML training, rendering
- 8Q: CAD, engineering
- 4Q: Virtual workstations
- 2Q/1Q: VDI (virtual desktop)
```

**VMware vGPU Configuration:**
```
1. Install NVIDIA vGPU Manager on ESXi
2. Reboot ESXi
3. Edit VM settings:
   - Add PCI Device → NVIDIA GRID vGPU
   - Select profile (e.g., V100-4Q)
4. Install NVIDIA drivers in guest
5. License with NVIDIA vGPU License Server
```

#### 3. GPU API Forwarding (virgl, Venus)

```
Concept: Virtualize GPU API calls

┌────────────────────┐
│       VM           │
│   ┌────────────┐   │
│   │ OpenGL app │   │
│   └──────┬─────┘   │
│   ┌──────▼─────┐   │
│   │ virglrenderer│  │ Virtual GPU driver
│   └──────┬─────┘   │
└──────────┼─────────┘
           │ API calls forwarded
┌──────────▼─────────┐
│   Host GPU         │
│   (any GPU)        │
└────────────────────┘

Pros:
✅ No special GPU needed
✅ Works with consumer GPUs
✅ Live migration

Cons:
❌ Limited API support (OpenGL, Vulkan)
❌ Performance overhead (30-50%)
❌ No CUDA support
```

**Use case**: Development, testing, not production graphics workloads

### GPU Benchmarks

```
Benchmark: Blender BMW Render

Bare-metal (NVIDIA RTX 3090):       42 seconds
GPU Passthrough:                    43 seconds (98%)
vGPU (1/4 of Tesla V100):          58 seconds (72%)
Software rendering (CPU):           18 minutes (2%)

Lesson: Passthrough near-native, vGPU good for sharing, avoid software rendering
```

---

## Monitoring and Troubleshooting

### Performance Monitoring Tools

**VMware:**
```bash
# esxtop (real-time)
esxtop
- c: CPU view
- m: Memory view
- d: Disk view
- n: Network view

# vscsiStats (storage latency)
vscsiStats -s -w 3600 -i 5 <VM-SCSI-device>

# PowerCLI
Get-Stat -Entity "VM-Name" -Stat "cpu.usage.average" -Start (Get-Date).AddHours(-24)
```

**KVM:**
```bash
# virt-top (like top for VMs)
virt-top

# virsh stats
virsh domstats myvm --cpu --block --net

# perf (Linux profiling)
perf top -p <qemu-pid>
```

**General Linux:**
```bash
# CPU
mpstat 1
sar -u 1 10

# Memory
vmstat 1
free -h

# Disk
iostat -x 1
iotop

# Network
iftop
nethogs
```

### Common Performance Issues

**High CPU Ready Time:**
```
Symptom: VM slow despite low CPU usage

Cause: CPU overcommitment on host

Solution:
1. Reduce vCPU count (right-size)
2. Set CPU reservation
3. Add more host capacity
4. vMotion to less loaded host
```

**Memory Swapping:**
```
Symptom: Extreme slowness, disk thrashing

Cause: Hypervisor swapping VM memory to disk

Solution:
1. Add more host RAM
2. Set memory reservation
3. Reduce VM memory allocation (if over-provisioned)
4. Enable ballooning (less bad than swapping)
```

**Storage Latency:**
```
Symptom: High disk latency (>20ms)

Cause: Slow storage or contention

Solution:
1. Move to faster storage tier
2. Check for other VMs causing contention
3. Verify multipathing is working
4. Check SIOC configuration
```

**Network Drops:**
```
Symptom: Packet loss, low throughput

Cause: Oversubscribed uplinks or misconfiguration

Solution:
1. Check uplink utilization
2. Enable NIOC (QoS)
3. Verify NIC teaming
4. Check for duplex mismatches
```

---

## Best Practices Summary

### CPU
✅ Right-size (start small, grow as needed)  
✅ Monitor CPU Ready time (<5%)  
✅ Use hardware virtualization (VT-x/AMD-V)  
✅ Consider NUMA for large VMs  
✅ Avoid over-provisioning vCPU  

### Memory
✅ Allocate Active + 20% buffer  
✅ Use reservations for critical VMs only  
✅ Enable huge pages  
✅ Monitor ballooning and swapping (should be 0)  
✅ Disable TPS if security concern  

### Storage
✅ Use paravirtualized drivers (virtio, pvscsi)  
✅ Place VMs on appropriate tier  
✅ Monitor latency (<10ms ideal)  
✅ Use SIOC for shared datastores  
✅ Avoid snapshot chains  

### Network
✅ Use paravirtualized drivers (VMXNET3, virtio)  
✅ Enable jumbo frames (9000 MTU) where possible  
✅ Consider SR-IOV for high-performance  
✅ Monitor bandwidth utilization  
✅ Use NIOC for shared uplinks  

### General
✅ Monitor continuously (establish baseline)  
✅ Regular performance reviews  
✅ Document optimizations  
✅ Test changes in non-production first  
✅ Keep hypervisor/guest tools updated  

---

## Conclusioni

Performance optimization della virtualizzazione richiede:

- **Understanding overhead**: Conoscere dove l'overhead si manifesta
- **Right-sizing**: Allocare risorse appropriate, non eccessive
- **Hardware assist**: Sfruttare VT-x, EPT, SR-IOV
- **NUMA awareness**: Mantenere VMs dentro NUMA nodes
- **Monitoring**: Continuo, per identificare bottleneck
- **Tuning**: Hypervisor, guest OS, applicazioni

Con le tecniche moderne e hardware appropriato, le performance delle VM possono raggiungere il 95-99% del bare-metal.

---

## Domande di Autovalutazione

1. Quali sono le principali fonti di overhead nella virtualizzazione?
2. Come si identifica una VM over-provisioned?
3. Cos'è il CPU Ready time e perché è importante?
4. Spiega il problema NUMA e come mitigarlo
5. Quando usare SR-IOV vs paravirtualized drivers?
6. Quali sono le differenze tra GPU passthrough e vGPU?
7. Come si ottimizza la performance storage per una VM?

---

## Esercizi Pratici

### Lab 1: Performance Baseline
1. Create VM standard
2. Run CPU, memory, disk, network benchmarks
3. Document baseline performance
4. Compare with bare-metal

### Lab 2: CPU Right-Sizing
1. Create VM with 8 vCPU
2. Run workload monitoring for 1 week
3. Analyze CPU usage patterns
4. Right-size based on data
5. Measure performance improvement

### Lab 3: Storage Performance Tuning
1. Measure disk latency (baseline)
2. Change from emulated to virtio
3. Enable caching optimizations
4. Measure improvement

### Lab 4: Network Performance
1. Test with emulated NIC
2. Switch to paravirtualized driver
3. Enable jumbo frames
4. Measure throughput improvement

---

## Risorse Aggiuntive

- [VMware Performance Best Practices](https://www.vmware.com/content/dam/digitalmarketing/vmware/en/pdf/techpaper/performance/vsphere-esxi-vcenter-server-70-performance-best-practices.pdf)
- [Red Hat KVM Performance Tuning Guide](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/8/html/configuring_and_managing_virtualization/optimizing-virtual-machine-performance-in-rhel_configuring-and-managing-virtualization)
- [NUMA Deep Dive](https://frankdenneman.nl/2016/07/06/introduction-2016-numa-deep-dive-series/)
- [Intel VT-x Performance](https://www.intel.com/content/www/us/en/virtualization/virtualization-technology/intel-virtualization-technology.html)
- [NVIDIA Virtual GPU Documentation](https://docs.nvidia.com/grid/)
