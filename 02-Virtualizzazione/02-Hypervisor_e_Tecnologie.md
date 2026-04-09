# 2.2 Hypervisor e Tecnologie

## Indice
- [Cos'è un Hypervisor](#cosè-un-hypervisor)
- [Type 1 Hypervisor (Bare-Metal)](#type-1-hypervisor-bare-metal)
- [Type 2 Hypervisor (Hosted)](#type-2-hypervisor-hosted)
- [Confronto Type 1 vs Type 2](#confronto-type-1-vs-type-2)
- [Architetture Ibride](#architetture-ibride)

---

## Cos'è un Hypervisor

Un **hypervisor** (o Virtual Machine Monitor - VMM) è il software che crea ed esegue macchine virtuali, gestendo la condivisione delle risorse hardware fisiche tra multiple VM.

### Funzioni Principali

1. **Resource Allocation**: Assegna CPU, RAM, storage, network alle VM
2. **Isolation**: Garantisce che le VM siano isolate tra loro
3. **Hardware Abstraction**: Presenta virtual hardware alle VM
4. **Scheduling**: Gestisce l'accesso delle VM alle risorse fisiche
5. **Memory Management**: Gestisce la memoria fisica e virtuale

### Componenti Architetturali

```
┌─────────────────────────────────────────┐
│          Virtual Machines               │
├─────────────────────────────────────────┤
│      Virtual Hardware Interface         │
├─────────────────────────────────────────┤
│     ┌─────────────────────────┐        │
│     │   VM Monitor (VMM)      │        │
│     ├─────────────────────────┤        │
│     │   CPU Scheduler         │        │
│     │   Memory Manager        │        │  HYPERVISOR
│     │   I/O Manager           │        │
│     │   Network Manager       │        │
│     └─────────────────────────┘        │
├─────────────────────────────────────────┤
│      Hardware Drivers (optional)        │
└─────────────────────────────────────────┘
```

---

## Type 1 Hypervisor (Bare-Metal)

### Definizione

**Type 1 hypervisor** (o bare-metal hypervisor) viene installato **direttamente sull'hardware fisico**, senza un sistema operativo sottostante.

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│     VM 1     │  │     VM 2     │  │     VM 3     │
│   Windows    │  │    Linux     │  │    Linux     │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       └─────────────┬────────────────────┘
              ┌──────▼─────────┐
              │  Hypervisor    │  ← Bare-metal
              │  (Type 1)      │
              └──────┬─────────┘
              ┌──────▼─────────┐
              │   Hardware     │
              └────────────────┘
```

### Caratteristiche

✅ **Performance**: Accesso diretto all'hardware, overhead minimo  
✅ **Efficienza**: Ottimizzazione risorse  
✅ **Scalabilità**: Supporta centinaia di VM  
✅ **Affidabilità**: Meno layer = meno punti di failure  
✅ **Security**: Superficie di attacco ridotta  

### Prodotti Type 1

---

## VMware ESXi

### Overview

**VMware ESXi** (Elastic Sky X Integrated) è il leader di mercato per virtualizzazione enterprise.

### Caratteristiche Principali

- **Footprint**: ~150 MB (minimal OS)
- **Architecture**: Proprietary VMkernel
- **Management**: vCenter Server
- **API**: vSphere API
- **Licensing**: Commercial (con free tier limitato)

### Architettura ESXi

```
┌────────────────────────────────────────┐
│         Virtual Machines               │
├────────────────────────────────────────┤
│       Virtual Hardware Layer           │
├────────────────────────────────────────┤
│  ┌──────────────────────────────────┐ │
│  │        VMkernel                   │ │
│  │  ┌────────────┐  ┌────────────┐  │ │
│  │  │ CPU        │  │  Memory    │  │ │
│  │  │ Scheduler  │  │  Manager   │  │ │
│  │  └────────────┘  └────────────┘  │ │
│  │  ┌────────────┐  ┌────────────┐  │ │
│  │  │ Storage    │  │  Network   │  │ │
│  │  │ Stack      │  │  Stack     │  │ │
│  │  └────────────┘  └────────────┘  │ │
│  └──────────────────────────────────┘ │
├────────────────────────────────────────┤
│          Device Drivers                │
├────────────────────────────────────────┤
│       Physical Hardware                │
└────────────────────────────────────────┘
```

### Funzionalità Enterprise

#### vMotion
Live migration di VM tra host senza downtime:
```
Host A                    Host B
┌────────┐               ┌────────┐
│  VM    │   vMotion     │        │
│ ┌────┐ │──────────────>│ ┌────┐ │
│ │App │ │               │ │App │ │
│ └────┘ │               │ └────┘ │
└────────┘               └────────┘
    ↓                         ↑
Downtime: 0 secondi (seamless)
```

#### High Availability (HA)
- Failover automatico in caso di host failure
- Heartbeat tra host
- VM restart su host funzionante

#### Distributed Resource Scheduler (DRS)
- Load balancing automatico
- vMotion automatico per ottimizzare utilizzo risorse

#### Storage vMotion
- Migrazione storage VM senza downtime

### Installazione ESXi

```bash
# 1. Download ISO da VMware
# 2. Boot da USB/CD
# 3. Installazione guidata
# 4. Configurazione IP management

# Accesso:
https://<esxi-ip>/ui

# CLI (SSH - da abilitare):
ssh root@<esxi-ip>
```

### Gestione via CLI

```bash
# Lista VM
vim-cmd vmsvc/getallvms

# Stato VM
vim-cmd vmsvc/power.getstate <vmid>

# Power on VM
vim-cmd vmsvc/power.on <vmid>

# Power off VM
vim-cmd vmsvc/power.off <vmid>

# Lista datastore
esxcli storage filesystem list

# Lista network
esxcli network vswitch standard list
```

### Pricing

- **ESXi Free**: Funzionalità base, no vCenter, no vMotion
- **vSphere Standard**: ~$1,000/CPU
- **vSphere Enterprise Plus**: ~$4,000/CPU

---

## Microsoft Hyper-V

### Overview

**Hyper-V** è l'hypervisor di Microsoft, integrato in Windows Server e Windows 10/11 Pro.

### Architetture

#### Windows Server con Hyper-V Role
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Guest VM  │  │  Guest VM   │  │  Guest VM   │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       └────────────┬───────────────────┘
         ┌──────────▼──────────┐
         │   Root Partition    │  ← Windows Server
         │  (Parent/Dom0)      │  ← Management OS
         └──────────┬──────────┘
         ┌──────────▼──────────┐
         │    Hyper-V          │  ← Hypervisor
         └──────────┬──────────┘
         ┌──────────▼──────────┐
         │    Hardware         │
         └─────────────────────┘
```

#### Hyper-V Server (Free)
- Stand-alone hypervisor gratuito
- Nessuna GUI (solo PowerShell/CLI)
- Gestibile da RSAT o System Center

### Caratteristiche Principali

- **Integration Services**: Driver per VM guest
- **Live Migration**: Simile a vMotion
- **Replica**: Disaster recovery integrato
- **Shielded VMs**: VM crittografate con TPM
- **Nested Virtualization**: VM dentro VM

### Generazioni VM

#### Generation 1 (Legacy)
- BIOS-based
- IDE boot
- Compatibilità con OS vecchi

#### Generation 2 (UEFI)
- UEFI boot
- Secure Boot
- SCSI boot (più veloce)
- Solo per Windows 8+ e Linux recenti

### Installazione Hyper-V

#### Windows Server
```powershell
# Installa ruolo Hyper-V
Install-WindowsFeature -Name Hyper-V -IncludeManagementTools -Restart

# Crea Virtual Switch
New-VMSwitch -Name "External" -NetAdapterName "Ethernet" -AllowManagementOS $true

# Crea VM
New-VM -Name "TestVM" -MemoryStartupBytes 2GB -Generation 2 -NewVHDPath "C:\VMs\TestVM.vhdx" -NewVHDSizeBytes 60GB

# Avvia VM
Start-VM -Name "TestVM"
```

#### Windows 10/11 Pro
```powershell
# Abilita Hyper-V feature
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

### Gestione PowerShell

```powershell
# Lista VM
Get-VM

# Stato VM
Get-VM -Name "TestVM" | Select-Object State

# Snapshot
Checkpoint-VM -Name "TestVM" -SnapshotName "BeforeUpdate"

# Export VM
Export-VM -Name "TestVM" -Path "D:\Backup"

# Import VM
Import-VM -Path "D:\Backup\TestVM\Virtual Machines\*.vmcx"

# Live Migration
Move-VM -Name "TestVM" -DestinationHost "HyperV02" -IncludeStorage -DestinationStoragePath "D:\VMs"
```

### Pricing

- **Hyper-V Server**: Gratuito
- **Windows Server Standard**: 2 VM incluse
- **Windows Server Datacenter**: VM illimitate

---

## KVM (Kernel-based Virtual Machine)

### Overview

**KVM** trasforma il kernel Linux in un hypervisor Type 1, sfruttando hardware virtualization (VT-x/AMD-V).

### Architettura

```
┌──────────────┐  ┌──────────────┐
│   Guest VM   │  │   Guest VM   │
│   (QEMU)     │  │   (QEMU)     │
└──────┬───────┘  └──────┬───────┘
       └──────────┬───────────┘
        ┌─────────▼─────────┐
        │   QEMU Process    │  ← User space
        └─────────┬─────────┘
═══════════════════╪═══════════════ Kernel space
        ┌─────────▼─────────┐
        │   KVM Module      │  ← /dev/kvm
        │  (kvm.ko)         │
        └─────────┬─────────┘
        ┌─────────▼─────────┐
        │   Linux Kernel    │
        └─────────┬─────────┘
        ┌─────────▼─────────┐
        │   Hardware        │
        └───────────────────┘
```

### Componenti

1. **KVM kernel module**: Gestisce VT-x/AMD-V
2. **QEMU**: Emulatore device hardware
3. **libvirt**: API di gestione
4. **virsh**: CLI per gestione VM

### Installazione KVM (Ubuntu/Debian)

```bash
# Verifica supporto virtualizzazione
egrep -c '(vmx|svm)' /proc/cpuinfo
# Output > 0 = supportato

# Installa KVM e tools
sudo apt update
sudo apt install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils virt-manager

# Aggiungi utente a gruppi
sudo usermod -aG libvirt $USER
sudo usermod -aG kvm $USER

# Verifica installazione
sudo systemctl status libvirtd
virsh list --all
```

### Installazione KVM (RHEL/CentOS)

```bash
# Installa pacchetti
sudo yum install -y qemu-kvm libvirt libvirt-python libguestfs-tools virt-install

# Avvia servizi
sudo systemctl enable libvirtd
sudo systemctl start libvirtd
```

### Gestione VM con virsh

```bash
# Crea VM da ISO
virt-install \
  --name ubuntu-vm \
  --ram 2048 \
  --vcpus 2 \
  --disk path=/var/lib/libvirt/images/ubuntu-vm.qcow2,size=20 \
  --os-variant ubuntu22.04 \
  --network bridge=virbr0 \
  --graphics vnc \
  --cdrom /path/to/ubuntu-22.04.iso

# Lista VM
virsh list --all

# Avvia VM
virsh start ubuntu-vm

# Connetti console
virsh console ubuntu-vm

# Spegni VM
virsh shutdown ubuntu-vm

# Forza spegnimento
virsh destroy ubuntu-vm

# Snapshot
virsh snapshot-create-as ubuntu-vm snap1 "Before update"

# Lista snapshot
virsh snapshot-list ubuntu-vm

# Ripristina snapshot
virsh snapshot-revert ubuntu-vm snap1

# Elimina VM
virsh undefine ubuntu-vm --remove-all-storage
```

### Gestione Network

```bash
# Lista network
virsh net-list --all

# Default network (NAT)
virsh net-info default

# Crea bridge network
cat > br0.xml <<EOF
<network>
  <name>br0</name>
  <forward mode='bridge'/>
  <bridge name='br0'/>
</network>
EOF

virsh net-define br0.xml
virsh net-start br0
virsh net-autostart br0
```

### Vantaggi KVM

✅ **Open Source**: Gratuito, kernel Linux incluso  
✅ **Performance**: Pari a VMware  
✅ **Integrazione**: Parte del kernel Linux  
✅ **Flessibilità**: Ampia customizzazione  
✅ **Supporto**: Red Hat, Ubuntu, SUSE  
✅ **Cloud**: Usato da AWS, GCP, OpenStack  

---

## Xen

### Overview

**Xen** è un hypervisor open-source, pioniere della paravirtualizzazione, usato da AWS per EC2.

### Architettura

```
┌──────────┐ ┌──────────┐ ┌──────────┐
│  DomU 1  │ │  DomU 2  │ │  DomU N  │  ← Guest VM
│  Linux   │ │ Windows  │ │  Linux   │
└────┬─────┘ └────┬─────┘ └────┬─────┘
     └────────┬──────────────┬──┘
         ┌────▼──────────────▼───┐
         │       Dom0            │  ← Control Domain
         │   (Linux/NetBSD)      │  ← Hardware drivers
         └────┬──────────────────┘
         ┌────▼──────────────────┐
         │   Xen Hypervisor      │
         └────┬──────────────────┘
         ┌────▼──────────────────┐
         │     Hardware          │
         └───────────────────────┘
```

### Modalità Virtualizzazione

#### PV (Paravirtualization)
- Guest OS modificato
- Hypercall dirette
- Performance eccellenti (pre-VT-x)

#### HVM (Hardware Virtual Machine)
- Full virtualization con VT-x/AMD-V
- Guest OS non modificati

#### PVHVM (Hybrid)
- HVM + PV drivers per I/O
- Best performance moderno

### Installazione Xen (Debian/Ubuntu)

```bash
# Installa Xen hypervisor
sudo apt install xen-system-amd64 xen-tools

# Verifica boot
sudo update-grub
sudo reboot

# Verifica Xen running
xl info

# Lista VM
xl list
```

### Gestione VM con xl

```bash
# Crea VM config
cat > /etc/xen/ubuntu-vm.cfg <<EOF
name = "ubuntu-vm"
memory = 2048
vcpus = 2
disk = [ '/var/lib/xen/images/ubuntu-vm.img,raw,xvda,rw' ]
vif = [ 'bridge=xenbr0' ]
kernel = "/boot/vmlinuz"
ramdisk = "/boot/initrd.img"
extra = "root=/dev/xvda1"
EOF

# Crea VM
xl create /etc/xen/ubuntu-vm.cfg

# Console
xl console ubuntu-vm

# Shutdown
xl shutdown ubuntu-vm

# Lista
xl list
```

### Utilizzo nel Cloud

- **AWS EC2** (originale): Basato su Xen
- **AWS Nitro**: Successore custom di Xen
- **Rackspace Cloud**
- **Linode** (passato)

---

## Proxmox VE

### Overview

**Proxmox Virtual Environment** è una piattaforma di virtualizzazione open-source enterprise che combina KVM e LXC container.

### Caratteristiche

- **KVM**: Per VM complete
- **LXC**: Per container system
- **Web GUI**: Gestione completa via browser
- **Clustering**: HA cluster integrato
- **Backup**: Snapshot e backup automatici
- **Storage**: ZFS, Ceph, NFS, iSCSI
- **Open Source**: Licenza AGPLv3

### Architettura

```
┌────────────────────────────────────────┐
│         Proxmox Web Interface          │
│         (Port 8006, HTTPS)             │
└──────────────┬─────────────────────────┘
               │
┌──────────────▼─────────────────────────┐
│          Proxmox VE Node               │
│  ┌────────────┐     ┌──────────────┐  │
│  │  KVM VMs   │     │ LXC Container│  │
│  │ ┌────┐     │     │  ┌────┐      │  │
│  │ │VM 1│ ... │     │  │CT 1│ ...  │  │
│  │ └────┘     │     │  └────┘      │  │
│  └────────────┘     └──────────────┘  │
├────────────────────────────────────────┤
│       Debian Linux Base                │
└──────────────┬─────────────────────────┘
               │
┌──────────────▼─────────────────────────┐
│          Hardware                      │
└────────────────────────────────────────┘
```

### Installazione Proxmox

```bash
# 1. Download ISO da proxmox.com
# 2. Boot e installazione guidata
# 3. Accesso web interface:
https://<proxmox-ip>:8006

# Login:
Username: root
Password: <set during install>
```

### Gestione via CLI

```bash
# Lista VM
qm list

# Crea VM
qm create 100 --name test-vm --memory 2048 --cores 2 --net0 virtio,bridge=vmbr0

# Start VM
qm start 100

# Stop VM
qm stop 100

# Snapshot
qm snapshot 100 snap1

# Lista container LXC
pct list

# Crea container
pct create 101 local:vztmpl/ubuntu-22.04-standard_22.04-1_amd64.tar.zst --hostname ubuntu-ct --memory 1024 --rootfs local-lvm:8

# Start container
pct start 101

# Enter container
pct enter 101
```

### Cluster HA

```bash
# Crea cluster (node1)
pvecm create mycluster

# Aggiungi nodo (node2)
pvecm add <node1-ip>

# Status cluster
pvecm status

# Quorum
pvecm expected 1
```

### Vantaggi Proxmox

✅ **Gratuito**: No licensing costs  
✅ **Web GUI**: User-friendly  
✅ **KVM + LXC**: Flessibilità  
✅ **HA Clustering**: Integrato  
✅ **Backup**: Built-in  
✅ **Community**: Ampia e attiva  

---

## Type 2 Hypervisor (Hosted)

### Definizione

**Type 2 hypervisor** gira **sopra un sistema operativo** esistente, come una normale applicazione.

```
┌──────────────┐  ┌──────────────┐
│   Guest VM   │  │   Guest VM   │
│   Windows    │  │    Linux     │
└──────┬───────┘  └──────┬───────┘
       └──────────┬───────┘
          ┌───────▼────────┐
          │  Hypervisor    │  ← Application
          │   (Type 2)     │
          └───────┬────────┘
          ┌───────▼────────┐
          │   Host OS      │  ← Windows/macOS/Linux
          │ (Windows/Mac)  │
          └───────┬────────┘
          ┌───────▼────────┐
          │   Hardware     │
          └────────────────┘
```

### Caratteristiche

❌ **Performance**: Overhead maggiore (due OS layer)  
✅ **Facilità**: Installazione semplice  
✅ **Desktop**: Ideale per laptop/workstation  
❌ **Scalabilità**: Limitata  
✅ **Development**: Ottimo per dev/test  

---

## VMware Workstation / Fusion

### Overview

- **Workstation**: Windows/Linux host
- **Fusion**: macOS host
- Commercial product

### Caratteristiche

- **Snapshots**: Multiple snapshot tree
- **Cloning**: Full e linked clones
- **Unity Mode**: App guest integrate in host
- **Shared Folders**: Facile file sharing
- **Network**: NAT, Bridged, Host-only
- **USB**: Passthrough devices

### Installazione

```bash
# Linux
sudo chmod +x VMware-Workstation-*.bundle
sudo ./VMware-Workstation-*.bundle

# Windows: installer .exe

# macOS: DMG per Fusion
```

### Gestione CLI (vmrun)

```bash
# Lista VM running
vmrun list

# Start VM
vmrun start /path/to/vm.vmx nogui

# Stop VM
vmrun stop /path/to/vm.vmx soft

# Snapshot
vmrun snapshot /path/to/vm.vmx snap1

# Revert
vmrun revertToSnapshot /path/to/vm.vmx snap1
```

### Pricing

- **Workstation Pro**: ~$250
- **Fusion Pro**: ~$200
- **Player/Free**: Limited features

---

## Oracle VirtualBox

### Overview

**VirtualBox** è un hypervisor Type 2 gratuito e open-source (GPLv2 + proprietario).

### Caratteristiche

- **Cross-platform**: Windows, macOS, Linux, Solaris
- **Open Source**: Gratuito per uso personale/educativo
- **Guest Additions**: Driver per integrazione
- **Snapshots**: Illimitati
- **USB**: Support USB 2.0/3.0 (Extension Pack)
- **RDP**: Remote access integrato

### Installazione

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install virtualbox virtualbox-ext-pack

# Fedora/RHEL
sudo dnf install VirtualBox

# macOS: Download DMG da virtualbox.org

# Windows: Download .exe installer
```

### Gestione CLI (VBoxManage)

```bash
# Lista VM
vboxmanage list vms

# Crea VM
vboxmanage createvm --name "TestVM" --ostype "Ubuntu_64" --register

# Configura
vboxmanage modifyvm "TestVM" --memory 2048 --cpus 2 --nic1 nat

# Crea disco
vboxmanage createhd --filename "TestVM.vdi" --size 20480

# Attach disco
vboxmanage storagectl "TestVM" --name "SATA" --add sata --controller IntelAhci
vboxmanage storageattach "TestVM" --storagectl "SATA" --port 0 --device 0 --type hdd --medium "TestVM.vdi"

# Start VM
vboxmanage startvm "TestVM" --type headless

# Stop VM
vboxmanage controlvm "TestVM" poweroff

# Snapshot
vboxmanage snapshot "TestVM" take "snap1"

# Restore
vboxmanage snapshot "TestVM" restore "snap1"
```

### Extension Pack

Funzionalità proprietarie (gratis per uso personale):
- USB 2.0/3.0
- RDP server
- Disk encryption
- NVMe

```bash
# Download e installa
wget https://download.virtualbox.org/virtualbox/7.0.12/Oracle_VM_VirtualBox_Extension_Pack-7.0.12.vbox-extpack
sudo vboxmanage extpack install Oracle_VM_VirtualBox_Extension_Pack-7.0.12.vbox-extpack
```

---

## QEMU

### Overview

**QEMU** (Quick EMUlator) è un emulatore e virtualizer open-source.

### Modalità

1. **Full Emulation**: Emula architetture diverse (ARM su x86)
2. **Virtualization**: Con KVM per performance native

### Utilizzo con KVM

```bash
# Crea disco
qemu-img create -f qcow2 disk.qcow2 20G

# Avvia VM
qemu-system-x86_64 \
  -enable-kvm \
  -m 2048 \
  -cpu host \
  -smp 2 \
  -drive file=disk.qcow2,format=qcow2 \
  -cdrom ubuntu-22.04.iso \
  -boot d \
  -net nic -net user

# Con accelerazione KVM
qemu-system-x86_64 -enable-kvm -m 2G -hda disk.qcow2
```

### Formati Disco

```bash
# Converti formati
qemu-img convert -f vmdk -O qcow2 source.vmdk dest.qcow2

# Info disco
qemu-img info disk.qcow2

# Resize
qemu-img resize disk.qcow2 +10G

# Snapshot (interno)
qemu-img snapshot -c snap1 disk.qcow2
```

---

## Confronto Type 1 vs Type 2

### Tabella Comparativa

| Caratteristica | Type 1 (Bare-Metal) | Type 2 (Hosted) |
|----------------|---------------------|-----------------|
| **Performance** | Eccellente (overhead 1-5%) | Buona (overhead 10-20%) |
| **Utilizzo** | Datacenter, produzione | Desktop, dev/test |
| **Scalabilità** | Centinaia di VM | Decine di VM |
| **Gestione** | Complessa | Semplice |
| **Costo** | Alto (enterprise) | Basso/gratuito |
| **Hardware dedicato** | Sì | No |
| **Esempio** | ESXi, Hyper-V, KVM, Xen | VirtualBox, VMware Workstation |

### Quando Usare Type 1

✅ Production workload  
✅ Server consolidation  
✅ Cloud infrastructure  
✅ High availability requirements  
✅ Performance-critical applications  

### Quando Usare Type 2

✅ Development & testing  
✅ Desktop virtualization  
✅ Training & demos  
✅ Personal use  
✅ Occasional VM usage  

---

## Architetture Ibride

### KVM: Type 1 o Type 2?

KVM è **controverso**:
- Integrato in kernel Linux → Type 1 behavior
- Richiede Linux OS → Type 2 structure

**Classificazione**: **Type 1 con Linux come control domain** (simile a Xen Dom0)

### macOS Hypervisor.framework

Apple fornisce **Hypervisor.framework** nativo:
```
┌──────────────┐
│   Guest VM   │
├──────────────┤
│ Hypervisor   │  ← Framework nativo
│ .framework   │
├──────────────┤
│   macOS      │
├──────────────┤
│ Apple Silicon│  ← M1/M2/M3
└──────────────┘
```

Usato da:
- **Parallels Desktop**
- **VMware Fusion** (ARM version)
- **UTM**

### Windows Hyper-V + WSL2

```
┌─────────────┐ ┌─────────────┐
│  Linux WSL2 │ │  Windows VM │
└──────┬──────┘ └──────┬──────┘
       └────────┬───────┘
         ┌──────▼──────┐
         │   Hyper-V   │  ← Type 1
         └──────┬──────┘
         ┌──────▼──────┐
         │  Hardware   │
         └─────────────┘
```

Windows 11 usa Hyper-V anche per WSL2, rendendolo Type 1 anche su desktop.

---

## Conclusioni

Gli hypervisor sono il cuore della virtualizzazione:

- **Type 1** (ESXi, Hyper-V, KVM, Xen, Proxmox): Production, performance, scalabilità
- **Type 2** (VirtualBox, VMware Workstation): Development, testing, desktop

La scelta dipende da:
- **Workload**: Production vs dev/test
- **Performance**: Critical vs tolerable overhead
- **Budget**: Enterprise vs free/low-cost
- **Skill**: IT team expertise
- **Scale**: Datacenter vs single machine

Nel cloud moderno, Type 1 hypervisor (specialmente KVM e Xen/Nitro) dominano, mentre Type 2 resta essenziale per sviluppatori.

---

## Domande di Autovalutazione

1. Qual è la differenza architetturale principale tra Type 1 e Type 2 hypervisor?
2. Perché KVM è considerato Type 1 anche se richiede Linux?
3. Confronta VMware ESXi e Proxmox VE per un'azienda SMB
4. Quali sono i vantaggi di VirtualBox per uno sviluppatore?
5. Spiega l'architettura Dom0/DomU di Xen
6. Quando sceglieresti Hyper-V invece di KVM?

---

## Esercizi Pratici

### Lab 1: Installazione VirtualBox
1. Installa VirtualBox sul tuo sistema
2. Crea una VM Ubuntu
3. Installa Guest Additions
4. Prova snapshot e cloning

### Lab 2: KVM su Linux
1. Verifica supporto virtualizzazione
2. Installa KVM e libvirt
3. Crea VM con virt-install
4. Gestisci con virsh

---

## Risorse Aggiuntive

- [VMware vSphere Documentation](https://docs.vmware.com/en/VMware-vSphere/)
- [Microsoft Hyper-V Documentation](https://docs.microsoft.com/en-us/virtualization/hyper-v-on-windows/)
- [KVM Documentation](https://www.linux-kvm.org/page/Documents)
- [Xen Project Documentation](https://wiki.xenproject.org/)
- [Proxmox VE Documentation](https://pve.proxmox.com/pve-docs/)
- [VirtualBox User Manual](https://www.virtualbox.org/manual/)
