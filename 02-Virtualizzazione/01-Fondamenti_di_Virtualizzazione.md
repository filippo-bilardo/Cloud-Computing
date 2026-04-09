# 2.1 Fondamenti di Virtualizzazione

## Indice
- [Introduzione alla Virtualizzazione](#introduzione-alla-virtualizzazione)
- [Storia ed Evoluzione](#storia-ed-evoluzione)
- [Concetti di Astrazione Hardware](#concetti-di-astrazione-hardware)
- [Benefici della Virtualizzazione](#benefici-della-virtualizzazione)
- [Virtualizzazione vs Emulazione](#virtualizzazione-vs-emulazione)
- [Virtualizzazione Hardware vs Software](#virtualizzazione-hardware-vs-software)
- [Paravirtualizzazione](#paravirtualizzazione)

---

## Introduzione alla Virtualizzazione

La **virtualizzazione** è una tecnologia che consente di creare versioni virtuali di risorse hardware fisiche, permettendo a più sistemi operativi e applicazioni di condividere le stesse risorse hardware in modo isolato e sicuro.

### Definizione Formale

> *"La virtualizzazione è il processo di creazione di una rappresentazione virtuale (piuttosto che fisica) di qualcosa, inclusi hardware platform, sistemi operativi, dispositivi di storage e risorse di rete."*

### Concetto Base

```
┌─────────────────────────────────────────────┐
│         Hardware Fisico (Server)            │
│  CPU: 32 cores | RAM: 256GB | Disk: 4TB     │
└──────────────────┬──────────────────────────┘
                   │
        ┌──────────▼──────────┐
        │     Hypervisor      │  ← Layer di virtualizzazione
        └──────────┬──────────┘
                   │
    ┌──────────────┼──────────────┐
    │              │              │
┌───▼────┐     ┌───▼────┐     ┌───▼────┐
│  VM 1  │     │  VM 2  │     │  VM 3  │
│ Linux  │     │Windows │     │ Linux  │
│ 8 cores│     │ 8 cores│     │16 cores│
│ 32 GB  │     │ 64 GB  │     │128 GB  │
└────────┘     └────────┘     └────────┘
```

Un singolo server fisico può ospitare multiple macchine virtuali, ciascuna con il proprio sistema operativo e applicazioni.

---

## Storia ed Evoluzione

### Anni '60: Le Origini

#### 1960s - Mainframe IBM
- **1967**: IBM introduce **CP-67** per mainframe System/360
- Primo sistema di virtualizzazione commerciale
- Time-sharing per utilizzatori multipli
- Ogni utente aveva una "virtual machine" isolata

**Motivazione originale:**
- Mainframe costavano milioni di dollari
- Necessità di massimizzare utilizzo
- Condivisione risorse tra dipartimenti

### Anni '70-80: Maturità Mainframe

- **1972**: IBM VM/370 - evoluzione di CP-67
- Virtualizzazione diventa standard per mainframe
- Capacità di eseguire sistemi operativi diversi contemporaneamente

### Anni '90: Il Declino (apparente)

#### Avvento dei PC e Server x86
- PC e server diventano economici
- Ogni applicazione ottiene il proprio server fisico
- Virtualizzazione considerata "obsoleta"
- Problemi emergenti:
  - **Sprawl** dei server (proliferazione incontrollata)
  - **Utilizzo basso** (5-15% tipico)
  - **Costi** di gestione elevati
  - **Sprechi** energetici

### 1998-1999: Il Ritorno

#### VMware Workstation (1999)
- Prima virtualizzazione x86 commerciale
- Permetteva di eseguire Windows, Linux su desktop
- Dimostrazione che virtualizzazione x86 era possibile

**Sfida tecnica:**
- Architettura x86 non progettata per virtualizzazione
- Alcune istruzioni non virtualizzabili
- VMware usava **binary translation**

### Anni 2000: Rivoluzione Datacenter

#### 2001: VMware ESXi
- Hypervisor bare-metal per server
- Consolidamento server in datacenter
- ROI significativo

#### 2003: Xen Project
- Hypervisor open-source
- Paravirtualizzazione
- Adottato da AWS per EC2

#### 2005-2006: Hardware-Assisted Virtualization
- **Intel VT-x** (Vanderpool)
- **AMD-V** (Pacifica)
- CPU con supporto nativo virtualizzazione
- Performance vicine al bare-metal

#### 2008: KVM nel Linux Kernel
- Kernel-based Virtual Machine
- Integrato in Linux kernel 2.6.20
- Trasforma Linux in hypervisor

### Anni 2010: Cloud Computing Era

- **2006**: Amazon EC2 usa Xen
- **2010**: Microsoft Hyper-V matura
- **2013**: Docker rivoluziona container
- **2014**: Kubernetes per orchestrazione
- Virtualizzazione diventa **fondamento del cloud**

### 2020+: Specializzazione

- **Microservizi** e container
- **Serverless** (virtualizzazione estrema)
- **Edge computing** (virtualizzazione distribuita)
- **GPU virtualization** per AI/ML
- **Confidential computing** (VM crittografate)

---

## Concetti di Astrazione Hardware

### Livelli di Astrazione

La virtualizzazione introduce livelli di **astrazione** tra hardware fisico e software:

```
┌─────────────────────────────────────┐
│         Applications                │  ← Livello 4
├─────────────────────────────────────┤
│       Operating System              │  ← Livello 3
├─────────────────────────────────────┤
│      Virtual Hardware               │  ← Livello 2 (Astrazione)
├─────────────────────────────────────┤
│         Hypervisor                  │  ← Livello 1
├─────────────────────────────────────┤
│      Physical Hardware              │  ← Livello 0
└─────────────────────────────────────┘
```

### Astrazione delle Risorse

#### CPU Virtualization
- **vCPU** (virtual CPU) mappate su pCPU (physical CPU)
- CPU scheduler distribuisce tempo CPU
- **Overcommitment**: più vCPU che pCPU fisiche

```
Physical: 16 cores
↓
VM1: 4 vCPU  ┐
VM2: 8 vCPU  ├─ 28 vCPU totali (overcommit 1.75x)
VM3: 4 vCPU  │
VM4: 12 vCPU ┘
```

#### Memory Virtualization
- **Virtual memory** per ogni VM
- **Memory management unit (MMU)** virtualizzata
- Tecniche: ballooning, swapping, compression

#### Storage Virtualization
- **Virtual disk** come file su filesystem host
- Formati: VMDK (VMware), VHD/VHDX (Hyper-V), QCOW2 (QEMU)
- Thin provisioning: disco cresce on-demand

#### Network Virtualization
- **Virtual NIC** (vNIC) per ogni VM
- **Virtual Switch** connette VM
- VLAN, routing virtualizzato

### Instruction Set Architecture (ISA)

L'hypervisor deve **virtualizzare l'ISA** del processore:

#### Ring Protection (x86)
```
Ring 0: Kernel (privileged)        ← OS vuole eseguire qui
Ring 1: Device drivers (unused)
Ring 2: Device drivers (unused)
Ring 3: Applications (user mode)   ← Apps eseguono qui
```

**Problema**: OS guest vuole Ring 0, ma hypervisor occupa Ring 0!

**Soluzioni:**
1. **Binary Translation**: riscrive istruzioni privilegiate al volo
2. **Paravirtualizzazione**: modifica OS guest per chiamare hypervisor
3. **Hardware Assist**: CPU con Ring -1 per hypervisor (VT-x/AMD-V)

---

## Benefici della Virtualizzazione

### 1. Consolidamento Server

**Problema**: Sprawl dei server fisici
- 1 applicazione = 1 server fisico
- Utilizzo medio CPU: 5-15%
- Costi hardware, spazio, energia, cooling

**Soluzione**: Consolidamento con virtualizzazione
- 10-20 VM su 1 server fisico
- Utilizzo CPU: 60-80%
- **Riduzione costi** 70-80%

**Esempio Concreto:**
```
Prima (100 server fisici):
- Hardware: €500,000
- Energia: €100,000/anno
- Spazio datacenter: 10 rack
- Gestione: 5 sysadmin

Dopo (10 server fisici + virtualizzazione):
- Hardware: €100,000
- Energia: €15,000/anno
- Spazio: 1 rack
- Gestione: 2 sysadmin
- ROI: 12-18 mesi
```

### 2. Isolamento e Sicurezza

Ogni VM è **completamente isolata**:
- **Fault isolation**: crash di una VM non impatta altre
- **Security isolation**: breach in una VM non compromette altre
- **Resource isolation**: una VM non può "rubare" risorse di altre

**Scenario:**
```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Web Server │  │  App Server  │  │   Database   │
│   (Public)   │  │  (Internal)  │  │  (Critical)  │
├──────────────┤  ├──────────────┤  ├──────────────┤
│   DMZ VLAN   │  │  App VLAN    │  │   DB VLAN    │
└──────────────┘  └──────────────┘  └──────────────┘
```

Se web server compromesso, attacker non può direttamente accedere database.

### 3. Flessibilità e Agilità

#### Provisioning Rapido
- Nuova VM in **minuti** vs giorni/settimane per server fisico
- Template e cloning
- Automazione completa

#### Live Migration
- Spostare VM tra host **senza downtime**
- Manutenzione hardware senza interruzione servizio
- Load balancing dinamico

#### Snapshot e Backup
```bash
# Snapshot prima di update
virsh snapshot-create-as vm1 "before-update"

# Update applicazione
# ...se qualcosa va storto...

# Restore snapshot
virsh snapshot-revert vm1 "before-update"
```

Ritorno allo stato precedente in secondi!

### 4. Alta Disponibilità

#### Fault Tolerance
- VM replicate su host multipli
- Failover automatico in caso di guasto hardware

#### Disaster Recovery
- Replica VM su datacenter secondario
- RTO (Recovery Time Objective): minuti
- RPO (Recovery Point Objective): secondi

**Architettura HA:**
```
Site A (Primary)               Site B (DR)
┌──────────────┐              ┌──────────────┐
│   Host 1     │              │   Host 3     │
│  ┌─────────┐ │ Replication  │  ┌─────────┐ │
│  │ VM-1    │◄├──────────────┤─>│ VM-1    │ │
│  │(active) │ │              │  │(standby)│ │
│  └─────────┘ │              │  └─────────┘ │
└──────────────┘              └──────────────┘
```

### 5. Testing e Sviluppo

- **Ambienti multipli** su singolo laptop
- **Clonazione** rapida per testing
- **Snapshot** per rollback
- **Isolamento** tra ambienti (dev, test, staging)

**Workflow sviluppatore:**
```
1. Clone production template
2. Develop su VM locale
3. Test su VM clonata
4. Snapshot before deploy
5. Deploy to staging VM
6. Test, iterate
7. Deploy to production
```

### 6. Efficienza Energetica (Green IT)

**Consolidamento = Riduzione Consumo Energetico**

```
100 server fisici @ 300W = 30,000W
↓ consolidamento
10 server fisici @ 500W = 5,000W

Risparmio: 25,000W = 83%
+ riduzione cooling (40% energia datacenter)
```

**Power Usage Effectiveness (PUE):**
- Senza virtualizzazione: PUE ~2.0
- Con virtualizzazione ottimizzata: PUE ~1.2-1.5

---

## Virtualizzazione vs Emulazione

### Emulazione

**Emulazione** simula completamente un'architettura hardware diversa.

**Esempio**: Eseguire software ARM su CPU x86

```
┌─────────────────────┐
│   ARM Application   │
├─────────────────────┤
│   ARM Linux OS      │
├─────────────────────┤
│   ARM Emulator      │  ← Traduce OGNI istruzione ARM in x86
├─────────────────────┤
│   x86 Host OS       │
├─────────────────────┤
│   x86 Hardware      │
└─────────────────────┘
```

**Caratteristiche:**
- **Performance**: Molto lenta (10-100x overhead)
- **Compatibilità**: Totale (diversa architettura CPU)
- **Uso**: Gaming retro, sviluppo embedded, archeologia software

**Esempi:**
- **QEMU** (full system emulation)
- **MAME** (arcade machine emulator)
- **Dolphin** (Nintendo Wii/GameCube)
- **Android Emulator** (ARM su x86)

### Virtualizzazione

**Virtualizzazione** esegue codice nativo con minimo overhead.

```
┌─────────────────────┐
│  x86 Application    │
├─────────────────────┤
│  x86 Linux OS       │
├─────────────────────┤
│    Hypervisor       │  ← Minima traduzione, esecuzione diretta
├─────────────────────┤
│  x86 Hardware       │
└─────────────────────┘
```

**Caratteristiche:**
- **Performance**: Alta (1-10% overhead)
- **Compatibilità**: Stessa architettura CPU
- **Uso**: Datacenter, cloud, server consolidation

### Confronto Diretto

| Aspetto | Emulazione | Virtualizzazione |
|---------|-----------|-----------------|
| **Architettura** | Può essere diversa | Deve essere uguale |
| **Performance** | 10-100x overhead | 1-10% overhead |
| **Compatibilità** | Totale | Solo stessa arch |
| **Uso tipico** | Dev, gaming retro | Production datacenter |
| **Esempi** | QEMU, Android Emu | VMware, KVM, Hyper-V |

### Hardware-Assisted Virtualization

Estensioni CPU per virtualizzazione efficiente:

#### Intel VT-x (Virtualization Technology)
- VMX (Virtual Machine Extensions)
- EPT (Extended Page Tables) per memory virtualization
- VPID (Virtual Processor ID) per TLB efficiency

#### AMD-V (AMD Virtualization)
- SVM (Secure Virtual Machine)
- NPT (Nested Page Tables)
- ASID (Address Space ID)

**Abilitare in BIOS:**
```
BIOS/UEFI Settings:
→ Advanced
  → CPU Configuration
    → Intel Virtualization Technology: [Enabled]
    → VT-d (Intel VT for Directed I/O): [Enabled]
```

**Verificare supporto Linux:**
```bash
# Intel VT-x
grep -E 'vmx' /proc/cpuinfo

# AMD-V
grep -E 'svm' /proc/cpuinfo

# Se output presente, virtualizzazione supportata
```

---

## Virtualizzazione Hardware vs Software

### Virtualizzazione Software (Binary Translation)

**Senza supporto hardware**, l'hypervisor usa **binary translation**:

1. **Scan**: Analizza codice guest
2. **Translate**: Riscrive istruzioni privilegiate
3. **Cache**: Memorizza codice tradotto
4. **Execute**: Esegue codice modificato

**Esempio VMware pre-VT-x:**
```
Guest OS: mov cr3, eax    ← Istruzione privilegiata (cambia page table)
          ↓ Binary Translation
Hypervisor: call vmm_set_cr3(eax)  ← Chiamata hypervisor
```

**Pro:**
- Funziona su CPU senza VT-x/AMD-V
- Compatibilità ampia

**Contro:**
- Overhead 10-30%
- Complessità implementativa

### Virtualizzazione Hardware

**Con VT-x/AMD-V**, CPU esegue direttamente codice guest:

- **VMX root mode**: Hypervisor (Ring -1)
- **VMX non-root mode**: Guest OS (Ring 0 virtualizzato)
- **VM Exit**: Switch da guest a hypervisor
- **VM Entry**: Switch da hypervisor a guest

**Istruzioni speciali:**
- `VMLAUNCH`: Avvia VM
- `VMRESUME`: Riprende VM
- `VMCALL`: Guest chiama hypervisor

**Pro:**
- Performance ~native (overhead 1-5%)
- Implementazione semplificata

**Contro:**
- Richiede CPU recente (post-2006)

### Passtrough I/O Virtualization

#### SR-IOV (Single Root I/O Virtualization)

Permette a device PCI (NIC, GPU) di presentarsi come **multiple virtual functions**:

```
Physical NIC (Intel X710)
├─ PF (Physical Function) ← Gestito da hypervisor
└─ VF1 ─┬─ Assegnata a VM1 (direct access)
   VF2  │─ Assegnata a VM2
   VF3  └─ Assegnata a VM3
```

**Vantaggi:**
- **Performance native**: No overhead virtualizzazione
- **Latenza bassa**: Direct access
- **Throughput alto**: 10-40 Gbps per VM

**Use case:**
- Network-intensive workload
- High-frequency trading
- NFV (Network Function Virtualization)

---

## Paravirtualizzazione

### Concetto

**Paravirtualizzazione**: Guest OS **modificato** per essere "consapevole" della virtualizzazione e cooperare con l'hypervisor.

```
Traditional Virtualization:
┌────────────┐
│  Guest OS  │ ← Crede di essere su hardware reale
├────────────┤
│ Hypervisor │ ← Deve "ingannare" guest
└────────────┘

Paravirtualization:
┌────────────┐
│  Guest OS  │ ← "Sa" di essere virtualizzato
│  (modified)│ ← Chiama direttamente hypervisor (hypercalls)
├────────────┤
│ Hypervisor │
└────────────┘
```

### Hypercalls

Invece di istruzioni privilegiate, guest fa **hypercall**:

```c
// Traditional: istruzione privilegiata
outb(PORT, value);  // Trap → hypervisor emula

// Paravirtualization: hypercall diretta
xen_hypercall_io_write(PORT, value);  // Chiamata diretta
```

### Xen Paravirtualization

**Xen** fu pioniere della paravirtualizzazione:

```
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   DomU 1    │ │   DomU 2    │ │   DomU N    │
│(Guest Linux)│ │(Guest Linux)│ │(Guest Win)  │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       └───────────┬────────────────────┘
            ┌──────▼──────┐
            │   Dom0      │  ← Privileged domain
            │ (Linux)     │  ← Hardware drivers
            └──────┬──────┘
            ┌──────▼──────┐
            │ Xen Hypervi │
            └──────┬──────┘
            ┌──────▼──────┐
            │  Hardware   │
            └─────────────┘
```

**Dom0**: Domain 0, VM privilegiata con driver hardware  
**DomU**: Domain User, VM guest

### Virtio

**Virtio** è standard para-virtualization per device I/O:

```
┌────────────────────┐
│   Guest Driver     │  virtio-net, virtio-blk, virtio-scsi
├────────────────────┤
│   Virtio Layer     │  ← Standard interface
└──────────┬─────────┘
┌──────────▼─────────┐
│  Host Backend      │  vhost-net, vhost-blk
└────────────────────┘
```

**Vantaggi:**
- Performance vicine a native
- Portabile tra hypervisor (KVM, QEMU, VirtualBox)
- Driver inclusi in Linux kernel

**Performance comparison:**
```
E1000 (emulated Intel NIC):  1 Gbps
Virtio-net:                 10 Gbps
SR-IOV:                     40 Gbps
```

### Pro e Contro Paravirtualizzazione

#### Pro:
✅ **Performance** superiore (pre-VT-x era)  
✅ **Overhead** minimo  
✅ **Scalabilità** migliore  

#### Contro:
❌ **Guest OS** deve essere modificato (no Windows closed-source)  
❌ **Compatibilità** limitata  
❌ **Manutenzione** guest OS modificati  

### Stato Attuale

Con **VT-x/AMD-V**, pure virtualization ha performance simili a paravirtualization.

**Uso moderno di paravirtualization:**
- **I/O virtualization** (virtio) ancora rilevante
- **KVM** usa virtio per performance
- **Enlightenments** in Windows (Hyper-V aware)

---

## Conclusioni

La virtualizzazione è una tecnologia fondamentale che ha trasformato l'IT moderno:

- **Storia**: Da mainframe IBM anni '60 a fondamento del cloud
- **Astrazione**: Separa software da hardware fisico
- **Benefici**: Consolidamento, isolamento, flessibilità, HA
- **Tecniche**: Hardware-assisted, binary translation, paravirtualization
- **Evoluzione**: Da virtualizzazione completa a container e serverless

Comprendere i fondamenti è essenziale per progettare e gestire infrastrutture cloud moderne.

---

## Domande di Autovalutazione

1. Qual è la differenza fondamentale tra virtualizzazione ed emulazione?
2. Spiega il problema del "Ring 0" nella virtualizzazione x86
3. Quali sono i tre principali benefici della virtualizzazione per un datacenter?
4. Come funziona la binary translation in VMware?
5. Cosa sono Intel VT-x e AMD-V e perché sono importanti?
6. Spiega il concetto di paravirtualizzazione e fai un esempio
7. Quali sono i vantaggi del consolidamento server?
8. Descrivi lo scenario storico che ha portato al "ritorno" della virtualizzazione negli anni 2000

---

## Esercizi Pratici

### Esercizio 1: Verifica Supporto Virtualizzazione
```bash
# Linux
lscpu | grep Virtualization
cat /proc/cpuinfo | grep -E 'vmx|svm'

# Windows
systeminfo | findstr /C:"Virtualization"
```

### Esercizio 2: Calcolo ROI Consolidamento
Scenario:
- 50 server fisici, utilizzo medio 10%
- Costo server: €5,000 cad
- Energia: €200/server/anno
- Possibile consolidamento: 10:1

Calcola:
- Risparmio hardware
- Risparmio energia annuo
- ROI periodo

---

## Risorse Aggiuntive

- [Intel VT-x Specifications](https://www.intel.com/content/www/us/en/virtualization/virtualization-technology/intel-virtualization-technology.html)
- [AMD Virtualization (AMD-V) Technology](https://www.amd.com/en/technologies/virtualization)
- [Xen Project Documentation](https://wiki.xenproject.org/)
- [KVM (Kernel Virtual Machine)](https://www.linux-kvm.org/)
- [Paper: "Formal Requirements for Virtualizable Third Generation Architectures" - Popek & Goldberg, 1974](https://dl.acm.org/doi/10.1145/361011.361073)
