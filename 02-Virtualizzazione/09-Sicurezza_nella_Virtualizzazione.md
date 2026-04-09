# 2.9 Sicurezza nella Virtualizzazione

## Indice
- [VM Isolation](#vm-isolation)
- [Hypervisor Security](#hypervisor-security)
- [VM Escape Attacks](#vm-escape-attacks)
- [Patch Management](#patch-management)
- [Secure Boot](#secure-boot)
- [Encrypted VM](#encrypted-vm)

---

## VM Isolation

### Principi di Isolamento

```
Obiettivo: VM devono essere completamente isolate

┌────────────┐  ┌────────────┐  ┌────────────┐
│    VM 1    │  │    VM 2    │  │    VM 3    │
│  (Tenant A)│  │  (Tenant B)│  │  (Tenant C)│
└──────┬─────┘  └──────┬─────┘  └──────┬─────┘
       │               │               │
       └───────────────┼───────────────┘
              ┌────────▼────────┐
              │   Hypervisor    │  ← Security boundary
              │   - CPU isolation
              │   - Memory isolation
              │   - Network isolation
              │   - Storage isolation
              └────────┬────────┘
              ┌────────▼────────┐
              │    Hardware     │
              └─────────────────┘

VM 1 NON può:
❌ Accedere alla memoria di VM 2
❌ Leggere il disco di VM 3
❌ Sniffare il traffico di rete di altre VM
❌ Consumare tutte le risorse CPU
```

### Meccanismi di Isolamento

#### 1. CPU Isolation

**Hardware-Assisted (VT-x/AMD-V):**
```
Intel VT-x fornisce:
- VMX root mode (hypervisor - Ring -1)
- VMX non-root mode (guest - Ring 0 virtualizzato)

Guest che tenta istruzione privilegiata:
1. CPU genera VM Exit
2. Controllo passa a hypervisor
3. Hypervisor verifica permessi
4. Se non autorizzato: blocca
5. Se autorizzato: emula in modo sicuro

Esempio: Guest cerca di modificare CR3 (page table)
→ VM Exit
→ Hypervisor controlla se guest può farlo
→ Se sì, hypervisor aggiorna shadow page table
```

**Resource Limits:**
```bash
# VMware: Limita CPU per VM
sched.cpu.max = "2000"  # Max 2 GHz

# KVM: CPU quota (cgroups)
virsh schedinfo myvm --set vcpu_quota=200000
# quota/period = 200000/100000 = 2 vCPU max

Previene: CPU starvation di altre VM
```

#### 2. Memory Isolation

**MMU Virtualization:**
```
Senza EPT/NPT (Shadow Page Tables):
┌─────────────────────────────────┐
│ Guest Virtual → Guest Physical  │  Guest page tables
└─────────────┬───────────────────┘
              │
┌─────────────▼───────────────────┐
│ Guest Physical → Host Physical  │  Shadow page tables (hypervisor)
└─────────────────────────────────┘

Hypervisor maintains shadow tables:
- Guest cannot see real physical addresses
- Guest modifications trapped by hypervisor
- Ensures VM cannot access other VM memory

Con EPT/NPT (Hardware):
┌──────────────────────────────────┐
│ Guest Virtual → Guest Physical   │  Guest page tables
└─────────────┬────────────────────┘
              │
┌─────────────▼────────────────────┐
│ Guest Physical → Host Physical   │  EPT/NPT (hardware)
└──────────────────────────────────┘

Hardware enforces isolation:
- Two-level page table walk
- VM cannot bypass hypervisor
- Invalid translations cause fault
```

**Memory Encryption:**
```
AMD SEV (Secure Encrypted Virtualization):

┌──────────────────────────────────┐
│        VM Memory                 │
│        Encrypted with VM key     │
│        ┌──────────────────────┐  │
│        │  Plaintext in VM     │  │
│        └──────────────────────┘  │
└────────────────┬─────────────────┘
                 │ Encrypted
┌────────────────▼─────────────────┐
│       Physical RAM                │
│       Encrypted (hypervisor       │
│       cannot read VM memory)      │
└───────────────────────────────────┘

Protection against:
- Malicious hypervisor
- Physical memory attacks (cold boot)
- Memory snapshots
```

#### 3. Network Isolation

**VLAN Segmentation:**
```
┌─────────────────────────────────────┐
│         Virtual Switch              │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Port Group "Finance" VLAN 100 │ │
│  │  ├─ VM1 (Finance DB)          │ │
│  │  └─ VM2 (Finance App)         │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ Port Group "DMZ" VLAN 200     │ │
│  │  ├─ VM3 (Web Server)          │ │
│  │  └─ VM4 (Web Server)          │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ Port Group "Dev" VLAN 300     │ │
│  │  └─ VM5 (Test Server)         │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘

VM1 cannot communicate with VM3 (different VLANs)
VM3 can communicate with VM4 (same VLAN)
```

**Distributed Firewall (NSX):**
```
Micro-segmentation at VM level:

Rules for VM "Finance-DB":
├─ Allow: VM "Finance-App" → port 3306
├─ Allow: VM "Backup-Server" → port 3306
├─ Deny: All other traffic
└─ Applied at vNIC (before traffic enters VM)

Even if attacker compromises one VM,
cannot access other VMs (zero-trust model)
```

**Private VLANs (PVLAN):**
```
Scenario: Multiple customer VMs on same VLAN

Regular VLAN: All VMs can communicate

PVLAN Isolated ports:
┌──────────┐ ┌──────────┐ ┌──────────┐
│Customer A│ │Customer B│ │Customer C│
│   VM     │ │   VM     │ │   VM     │
└─────┬────┘ └─────┬────┘ └─────┬────┘
      │            │            │
      └────────────┼────────────┘
            ┌──────▼──────┐
            │  Gateway    │  ← Promiscuous port
            │  (can talk  │
            │   to all)   │
            └─────────────┘

Result:
- Customer A VM cannot see Customer B traffic
- All can reach gateway
- Isolation within same VLAN
```

#### 4. Storage Isolation

**Datastore Permissions:**
```
VMware example:

Datastore "Finance-Data":
├─ Permissions:
│  ├─ Finance group: Read/Write
│  └─ Others: No access
├─ VMs:
│  ├─ Finance-VM1 ✓
│  ├─ Finance-VM2 ✓
│  └─ HR-VM1 ✗ (denied access)

Prevents: Unauthorized VM from reading datastore
```

**Storage-Level Encryption:**
```
Architecture:

┌──────────────────────────────────┐
│        VM (encrypted VMDK)       │
│        VM sees plaintext         │
└────────────────┬─────────────────┘
                 │ Encrypted I/O
┌────────────────▼─────────────────┐
│     Datastore (encrypted)        │
│     - At-rest encryption         │
│     - Key managed by KMS         │
└──────────────────────────────────┘

Benefits:
✓ Protection if datastore stolen
✓ Compliance (GDPR, HIPAA)
✓ Encryption transparent to VM
```

---

## Hypervisor Security

### Attack Surface

```
Hypervisor is critical security boundary:

┌──────────────────────────────────────┐
│      Untrusted VMs (guests)          │
│      Potentially malicious           │
└──────────────┬───────────────────────┘
               │ VM interface
┌──────────────▼───────────────────────┐
│      Hypervisor (TCB)                │  ← Trust boundary
│      Must be secure!                 │
│      - Device emulation              │  ← Attack vectors
│      - Virtual device drivers        │
│      - Management interface          │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│      Hardware                        │
└──────────────────────────────────────┘

If hypervisor compromised:
→ All VMs compromised
→ Complete system breach
```

### Hypervisor Hardening

#### 1. Minimize Attack Surface

**VMware ESXi:**
```bash
# Disable unnecessary services
esxcli system service list
esxcli system service set --enabled=false --service=SSH

# Firewall rules (only needed ports)
esxcli network firewall ruleset set --ruleset-id=sshServer --enabled=false

# Remove unnecessary packages
esxcli software vib list
esxcli software vib remove --vibname=unwanted-package

# Lockdown mode (disable direct host access)
vim-cmd hostsvc/advopt/update Annotations.PowerSystem.LockdownMode bool true
```

**KVM/QEMU:**
```bash
# Run QEMU as non-root user
USER=qemu
GROUP=qemu

# AppArmor/SELinux profiles
# /etc/apparmor.d/usr.bin.qemu-system-x86_64

# Seccomp filtering (limit syscalls)
<seclabel type='dynamic' model='selinux' relabel='yes'>
  <label>system_u:system_r:svirt_t:s0:c100,c200</label>
</seclabel>

# Disable unnecessary QEMU devices
-device virtio-net-pci  # Only what's needed
-nodefaults             # No default devices
```

#### 2. Secure Configuration

**ESXi Security Baseline:**
```bash
# Strong passwords
esxcli system account set --id=root --password='ComplexP@ssw0rd!'

# Account lockout (after failed attempts)
/etc/pam.d/system-auth-generic:
account required pam_tally2.so deny=3 unlock_time=900

# Audit logging
esxcli system syslog config set --loghost='udp://syslog-server:514'

# NTP (prevent time-based attacks)
esxcli system ntp set --server=time.example.com
esxcli system ntp set --enabled=true

# Certificate-based authentication
# Replace default certificates with proper CA-signed certs
```

**Hyper-V Hardening:**
```powershell
# Credential Guard (protect credentials)
Enable-WindowsOptionalFeature -Online -FeatureName IsolatedUserMode

# Shielded VMs (encrypted, secure boot)
New-VM -Name "SecureVM" -Generation 2
Set-VMSecurity -VMName "SecureVM" -Shielded $true

# Disable unnecessary integration services
Disable-VMIntegrationService -VMName "MyVM" -Name "Time Synchronization"

# Configure firewall
New-NetFirewallRule -DisplayName "Block-All-Inbound" -Direction Inbound -Action Block
```

#### 3. Access Control

**Role-Based Access Control (RBAC):**
```
VMware vCenter Roles:

Administrator:
├─ Full control (all permissions)
└─ Use sparingly

VM Administrator:
├─ VM power operations
├─ VM configuration
└─ No host/network config

Read-Only:
├─ View resources
└─ No modifications

Custom Role "Backup Operator":
├─ VM snapshot
├─ VM backup (VADP)
└─ No VM power/config
```

**Principle of Least Privilege:**
```
Bad:
Everyone has Administrator role

Good:
├─ Admins: Administrator (2-3 people)
├─ VM owners: VM Power User (per-VM permissions)
├─ Developers: Read-Only + specific VMs
├─ Backup system: Backup Operator role
└─ Monitoring: Read-Only
```

#### 4. Network Security

**Management Network Isolation:**
```
Physical Network Separation:

Management Network (VLAN 10):
├─ ESXi management interfaces
├─ vCenter Server
├─ Domain controllers
└─ Restricted access (admin workstations only)

Production Network (VLAN 100):
├─ VM traffic
└─ General user access

DMZ (VLAN 200):
├─ Public-facing VMs
└─ Internet access

Never mix management and production traffic!
```

**Firewall Rules:**
```bash
# ESXi firewall (only allow needed IPs)
esxcli network firewall ruleset set --ruleset-id=vSphereClient --allowed-all=false
esxcli network firewall ruleset allowedip add --ruleset-id=vSphereClient --ip-address=192.168.10.0/24

# Block all by default, allow specific
esxcli network firewall set --default-action=false
esxcli network firewall set --enabled=true
```

---

## VM Escape Attacks

### What is VM Escape?

```
VM Escape: Attacker in VM breaks out to hypervisor

Normal isolation:
┌──────────────┐
│  Malicious   │
│     VM       │  ← Contained
└──────────────┘

VM Escape:
┌──────────────┐
│  Malicious   │
│     VM       │
│      ↓       │  ← Exploits vulnerability
│  [Exploit]   │
└──────┬───────┘
       │ Escapes
┌──────▼───────┐
│ Hypervisor   │  ← Now compromised!
└──────┬───────┘
       │ Can access
┌──────▼───────┐  ┌─────────────┐
│ Other VMs    │  │ Other VMs   │  ← All compromised
└──────────────┘  └─────────────┘
```

### Historical VM Escape Vulnerabilities

#### CVE-2008-0923 (VMware)
```
Vulnerability: Buffer overflow in VMware display driver

Attack:
1. Malicious code in VM
2. Trigger overflow via graphics operation
3. Overflow escapes VM, executes on host
4. Attacker gains host control

Fix: Patch VMware to fix overflow
Lesson: Device emulation is complex, prone to bugs
```

#### CVE-2011-1751 (Xen)
```
Vulnerability: Pygrub (bootloader) command injection

Attack:
1. Attacker modifies VM kernel cmdline
2. Injects commands into pygrub
3. Commands execute on Dom0 (control domain)
4. Attacker escalates to Dom0 root

Fix: Sanitize input to pygrub
Lesson: Trust boundary between guest and host
```

#### CVE-2018-3646 (Foreshadow-VMM)
```
Vulnerability: CPU speculative execution (Intel L1TF)

Attack:
1. Malicious VM executes speculative code
2. Speculative execution bypasses permission checks
3. Reads L1 cache from other VMs
4. Leaks sensitive data (encryption keys, passwords)

Fix: Microcode update + hypervisor patches
Lesson: Hardware vulnerabilities affect virtualization
```

### Protection Against VM Escape

#### 1. Keep Systems Patched

```bash
# VMware ESXi patches
esxcli software vib update --depot=/vmfs/volumes/datastore/VMware-ESXi-*.zip

# Check current version
vmware -v

# Automatic update (vCenter Update Manager)
# Or manual update during maintenance window
```

```bash
# KVM/QEMU updates
apt-get update
apt-get upgrade qemu-kvm libvirt-daemon

# Check versions
qemu-system-x86_64 --version
libvirtd --version
```

#### 2. Minimize Device Emulation

```xml
<!-- KVM: Reduce attack surface -->
<devices>
  <!-- Use paravirtualized devices (simpler code) -->
  <disk type='file' device='disk'>
    <driver name='qemu' type='qcow2'/>
    <target dev='vda' bus='virtio'/>  ← Paravirt (secure)
  </disk>
  
  <!-- Avoid emulated devices when possible -->
  <disk type='file' device='disk'>
    <target dev='hda' bus='ide'/>  ← Emulated (more complex, risky)
  </disk>
  
  <!-- Disable unnecessary devices -->
  <sound model='ich6'/>  ← Remove if not needed
  <video>  ← Remove if headless VM
```

#### 3. Run VMs as Non-Root (KVM)

```bash
# /etc/libvirt/qemu.conf
user = "qemu"
group = "qemu"

# If VM escapes, attacker gets qemu user (not root)
# Limited damage
```

#### 4. Use Security Features

**SELinux/AppArmor:**
```bash
# SELinux for KVM (sVirt)
getenforce
# Enforcing

# Each VM gets unique SELinux label
ps -efZ | grep qemu
system_u:system_r:svirt_t:s0:c100,c200

# VM can only access files with matching label
ls -lZ /var/lib/libvirt/images/
system_u:object_r:svirt_image_t:s0:c100,c200 vm1.qcow2
```

**Seccomp Filtering:**
```
Limit syscalls QEMU can make:
- Blocks dangerous syscalls
- Reduces exploit surface

Enable in libvirt:
<seclabel type='dynamic' model='selinux' relabel='yes'>
  <label>system_u:system_r:svirt_t:s0:c100,c200</label>
</seclabel>
```

---

## Patch Management

### Patching Strategy

```
Patching Layers:

┌──────────────────────────────────┐
│  Guest OS (Windows/Linux)        │  ← Layer 3: Monthly
├──────────────────────────────────┤
│  Guest Tools (VMware Tools)      │  ← Layer 2: Quarterly
├──────────────────────────────────┤
│  Hypervisor (ESXi, KVM)          │  ← Layer 1: Quarterly (critical: immediately)
├──────────────────────────────────┤
│  Firmware/BIOS                   │  ← Layer 0: Annually (critical: immediately)
└──────────────────────────────────┘
```

### Hypervisor Patching

#### VMware ESXi

**Manual Patching:**
```bash
# 1. Download patch from VMware
# 2. Upload to datastore

# 3. Enable SSH (temporarily)
vim-cmd hostsvc/advopt/update UserVars.SuppressShellWarning long 1

# 4. Enter maintenance mode
esxcli system maintenanceMode set --enable=true

# 5. Apply patch
esxcli software vib update --depot=/vmfs/volumes/datastore/ESXi700-202012001.zip

# 6. Reboot
reboot

# 7. Exit maintenance mode
esxcli system maintenanceMode set --enable=false

# 8. Disable SSH
/etc/init.d/SSH stop
```

**Automated with vCenter Update Manager (VUM):**
```
1. Attach baseline to cluster
2. Scan for compliance
3. Remediate (automatic):
   - vMotion VMs to other hosts
   - Enter maintenance mode
   - Apply patches
   - Reboot
   - Exit maintenance mode
   - Move to next host

Zero downtime for HA cluster!
```

#### KVM/Libvirt

```bash
# Ubuntu
apt-get update
apt-get install --only-upgrade qemu-kvm libvirt-daemon

# RHEL/CentOS
yum update qemu-kvm libvirt

# Reboot may be needed for kernel updates
# Live migration VMs to other host first
virsh migrate --live myvm qemu+ssh://other-host/system

# After migration, reboot
reboot
```

### Guest OS Patching

**Windows Update:**
```powershell
# Enable Windows Update
Set-Service wuauserv -StartupType Automatic
Start-Service wuauserv

# Check for updates
Install-Module PSWindowsUpdate
Get-WindowsUpdate

# Install updates
Install-WindowsUpdate -AcceptAll -AutoReboot

# Schedule monthly patching (Patch Tuesday)
```

**Linux Updates:**
```bash
# Ubuntu/Debian
apt-get update && apt-get upgrade -y

# RHEL/CentOS
yum update -y

# Automated with unattended-upgrades (Ubuntu)
apt-get install unattended-upgrades
dpkg-reconfigure --priority=low unattended-upgrades

# Or Ansible/Puppet for fleet management
```

### VMware Tools / Guest Additions

```bash
# VMware Tools update
# Option 1: Auto-update (recommended)
# VM → Edit Settings → VM Options → VMware Tools
# "Update Tools → Automatically"

# Option 2: Manual
# Mount VMware Tools ISO
mount /dev/cdrom /mnt
tar xzf /mnt/VMwareTools-*.tar.gz
cd vmware-tools-distrib
./vmware-install.pl

# VirtualBox Guest Additions
# Insert Guest Additions CD
mount /dev/cdrom /mnt
sh /mnt/VBoxLinuxAdditions.run
```

### Patch Testing Process

```
Test Environment:
1. Apply patch to dev/test cluster
2. Test for 1-2 weeks
3. Monitor for issues

Staging Environment:
4. Apply to staging (prod-like)
5. Test critical workloads
6. Monitor for 1 week

Production:
7. Apply during maintenance window
8. Rolling update (HA cluster)
9. Monitor closely
10. Rollback plan ready

Critical Security Patch:
→ Expedited process (test in days, not weeks)
```

---

## Secure Boot

### Concept

```
Secure Boot: Verify boot chain integrity

┌──────────────────────────────────┐
│  1. UEFI Firmware                │
│     - Has vendor public key      │
│     - Verifies bootloader        │
└──────────────┬───────────────────┘
               │ Signature valid?
┌──────────────▼───────────────────┐
│  2. Bootloader (GRUB/Bootmgr)    │
│     - Signed by vendor           │
│     - Verifies kernel            │
└──────────────┬───────────────────┘
               │ Signature valid?
┌──────────────▼───────────────────┐
│  3. OS Kernel                    │
│     - Signed by OS vendor        │
│     - Verifies drivers           │
└──────────────┬───────────────────┘
               │ Signature valid?
┌──────────────▼───────────────────┐
│  4. Drivers/Modules              │
│     - Signed drivers load        │
│     - Unsigned: blocked          │
└──────────────────────────────────┘

Blocks: Rootkits, bootkits, malware
```

### Enabling Secure Boot

#### VMware vSphere

```bash
# Requires:
- vSphere 6.5+ with EFI firmware
- Windows Server 2016+ or recent Linux

# Enable via vCenter:
VM → Edit Settings → VM Options
→ Boot Options → Firmware: EFI
→ Boot Options → Secure Boot: ✓ Enabled

# Verify in guest (Windows)
System Information → Secure Boot State: On

# Verify in guest (Linux)
mokutil --sb-state
# SecureBoot enabled
```

#### Hyper-V

```powershell
# Generation 2 VM required
New-VM -Name "SecureVM" -Generation 2 -MemoryStartupBytes 4GB

# Enable Secure Boot
Set-VMFirmware -VMName "SecureVM" -EnableSecureBoot On

# Choose template
Set-VMFirmware -VMName "SecureVM" -SecureBootTemplate "MicrosoftWindows"
# or "MicrosoftUEFICertificateAuthority" for Linux

# Verify
Get-VMFirmware -VMName "SecureVM" | Select-Object SecureBoot
```

#### KVM/Libvirt

```xml
<!-- Requires OVMF (UEFI firmware for VMs) -->
<os firmware='efi'>
  <type arch='x86_64' machine='q35'>hvm</type>
  <loader readonly='yes' secure='yes' type='pflash'>/usr/share/OVMF/OVMF_CODE.secboot.fd</loader>
  <nvram template='/usr/share/OVMF/OVMF_VARS.fd'>/var/lib/libvirt/qemu/nvram/myvm_VARS.fd</nvram>
  <boot dev='hd'/>
</os>
```

```bash
# Install OVMF
apt-get install ovmf

# Verify in guest
dmesg | grep -i secure
# [    0.000000] secureboot: Secure boot enabled
```

### Secure Boot Challenges

**Unsigned Drivers:**
```
Problem: Third-party unsigned drivers won't load

Example: NVIDIA proprietary driver, VirtualBox modules

Solutions:
1. Disable Secure Boot (not recommended)
2. Sign drivers with your own key
3. Use signed drivers from vendor
4. Add exception (MOK - Machine Owner Key)
```

**Signing Drivers (Linux):**
```bash
# Generate signing key
openssl req -new -x509 -newkey rsa:2048 -keyout MOK.priv -outform DER -out MOK.der -days 36500 -subj "/CN=My Signing Key/"

# Enroll key
mokutil --import MOK.der
# Reboot → MOK Manager → Enroll key

# Sign module
/usr/src/linux-headers-$(uname -r)/scripts/sign-file sha256 ./MOK.priv ./MOK.der /path/to/module.ko

# Load signed module
insmod /path/to/module.ko
```

---

## Encrypted VM

### Full VM Encryption

#### VMware vSphere Encryption

```
Architecture:

┌──────────────────────────────────────┐
│       Encrypted VM                   │
│  ┌────────────────────────────────┐  │
│  │  VM Home (VMX)  - encrypted    │  │
│  │  VMDK           - encrypted    │  │
│  │  Snapshots      - encrypted    │  │
│  │  Swap           - encrypted    │  │
│  │  Memory (vMotion) - encrypted  │  │
│  └────────────────────────────────┘  │
└──────────────────┬───────────────────┘
                   │ Encrypted
┌──────────────────▼───────────────────┐
│       Datastore (encrypted)          │
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│   Key Management Server (KMS)        │
│   - Stores encryption keys           │
│   - KMIP protocol                    │
│   - e.g., vCenter KMS, HashiCorp     │
└──────────────────────────────────────┘

Key per VM: Unique encryption key
ESXi requests key from KMS to decrypt
```

**Enable VM Encryption:**
```
Prerequisites:
1. KMS configured in vCenter
2. Host encryption mode enabled

Steps:
1. VM → Actions → VM Policies → Edit VM Storage Policies
2. Select "VM Encryption Policy"
3. OK → VM will be encrypted

Or via PowerCLI:
New-VM -Name "EncryptedVM" -VMHost $vmhost -Datastore $ds -DiskStorageFormat Thin
$spec = New-Object VMware.Vim.VirtualMachineConfigSpec
$spec.crypto = New-Object VMware.Vim.CryptoSpecEncrypt
Set-VM -VM "EncryptedVM" -AdvancedOption $spec
```

**Benefits:**
```
✓ Compliance (GDPR, HIPAA)
✓ Protection at rest (stolen disk)
✓ Protection in motion (vMotion encrypted)
✓ Transparent to guest
```

**Performance Impact:**
```
Encryption overhead: 5-10%
- CPU has AES-NI: ~5%
- CPU without AES-NI: ~20%

Modern CPUs (2010+) have hardware AES
→ Minimal impact
```

#### BitLocker / LUKS (Guest-Level Encryption)

**Windows BitLocker:**
```powershell
# Enable BitLocker on VM disk
Enable-BitLocker -MountPoint "C:" -EncryptionMethod XtsAes256 -UsedSpaceOnly -RecoveryPasswordProtector

# Store recovery key
$key = (Get-BitLockerVolume -MountPoint "C:").KeyProtector
$key | Out-File C:\BitLocker-Recovery-Key.txt

# Verify
Get-BitLockerVolume -MountPoint "C:"
```

**Linux LUKS:**
```bash
# Encrypt volume
cryptsetup luksFormat /dev/vdb

# Open encrypted volume
cryptsetup luksOpen /dev/vdb encrypted_vol

# Create filesystem
mkfs.ext4 /dev/mapper/encrypted_vol

# Mount
mount /dev/mapper/encrypted_vol /mnt

# /etc/crypttab (auto-mount)
encrypted_vol /dev/vdb none luks

# /etc/fstab
/dev/mapper/encrypted_vol /data ext4 defaults 0 2
```

**Comparison: VM Encryption vs Guest Encryption**

| Feature | VM Encryption | Guest Encryption |
|---------|---------------|------------------|
| **Scope** | Entire VM (disk, mem, swap) | Guest volumes only |
| **Transparency** | Transparent to guest | Guest must support |
| **Performance** | 5-10% | 5-15% |
| **Management** | Centralized (vCenter KMS) | Per-VM management |
| **Boot process** | Normal | Password/key needed |
| **Snapshots** | Encrypted | Encrypted |
| **vMotion** | Encrypted | Guest stays encrypted |
| **Use case** | Multi-tenant cloud | Single VM security |

### Confidential Computing

**AMD SEV (Secure Encrypted Virtualization):**
```
Encryption in use (RAM encrypted):

┌──────────────────────────────────┐
│   VM Memory (encrypted)          │
│   - Unique key per VM            │
│   - Hypervisor cannot read       │
└──────────────┬───────────────────┘
               │ Encrypted
┌──────────────▼───────────────────┐
│   Physical RAM (encrypted)       │
│   - Even with physical access,   │
│     cannot read VM memory        │
└──────────────────────────────────┘

Protection:
✓ Malicious hypervisor
✓ Physical memory attacks
✓ DMA attacks
```

**Intel SGX (Software Guard Extensions):**
```
Enclave: Secure area in memory

Application:
├─ Normal code
└─ Sensitive code in enclave (encrypted)

Even OS/hypervisor cannot access enclave
Use case: Process sensitive data (encryption keys, PII)
```

**Azure Confidential Computing:**
```
VMs with AMD SEV-SNP or Intel SGX:
- DC-series (SGX)
- DCasv5/DCadsv5-series (AMD SEV-SNP)

Use case:
- Multi-party computation
- Secure enclaves for sensitive data
- Blockchain applications
```

---

## Security Best Practices

### Defense in Depth

```
Layered Security:

Layer 7: Application Security
├─ Input validation
├─ SQL injection prevention
└─ XSS protection

Layer 6: Guest OS Security
├─ OS hardening
├─ Antivirus/EDR
└─ Host firewall

Layer 5: VM Security
├─ Encrypted VMs
├─ Secure boot
└─ vTPM

Layer 4: Network Security
├─ Micro-segmentation
├─ Distributed firewall
└─ IDS/IPS

Layer 3: Hypervisor Security
├─ Hardened hypervisor
├─ Minimal attack surface
└─ Regular patching

Layer 2: Physical Security
├─ Datacenter access control
├─ Secure boot (firmware)
└─ Hardware security modules

Layer 1: Personnel Security
├─ RBAC
├─ Least privilege
└─ Audit logging

Compromise of one layer doesn't breach entire system
```

### Security Checklist

**Hypervisor:**
- [ ] Latest patches applied
- [ ] Unnecessary services disabled
- [ ] Firewall configured (minimal ports)
- [ ] Strong passwords / certificate auth
- [ ] Management network isolated
- [ ] Audit logging enabled
- [ ] NTP configured
- [ ] RBAC implemented

**VMs:**
- [ ] Guest OS patched
- [ ] Antivirus/EDR installed
- [ ] Host firewall enabled
- [ ] Unnecessary services disabled
- [ ] Strong passwords
- [ ] Secure Boot enabled (if supported)
- [ ] Encryption enabled (if needed)

**Network:**
- [ ] VLANs configured
- [ ] Micro-segmentation (NSX/etc)
- [ ] Traffic inspection (IDS/IPS)
- [ ] Network monitoring

**Operations:**
- [ ] Regular vulnerability scans
- [ ] Penetration testing (annually)
- [ ] Incident response plan
- [ ] Disaster recovery tested
- [ ] Security awareness training

---

## Compliance and Regulations

### GDPR (General Data Protection Regulation)

**Requirements:**
- Data encryption (at rest, in transit)
- Access controls (RBAC)
- Audit logging (who accessed what)
- Data deletion (right to be forgotten)
- Breach notification (72 hours)

**VM Security Measures:**
```
✓ Encrypted VMs for personal data
✓ Network isolation (VLANs)
✓ Access logs (vCenter audit)
✓ Secure deletion (overwrite VM disks)
✓ Backup encryption
```

### HIPAA (Healthcare)

**Requirements:**
- PHI encryption
- Access controls
- Audit trails
- Disaster recovery

**VM Measures:**
```
✓ Encrypted VMs
✓ RBAC (only authorized access to patient VMs)
✓ Audit every access
✓ Backup/DR tested
✓ BAA (Business Associate Agreement) with cloud provider
```

### PCI DSS (Payment Card Industry)

**Requirements:**
- Cardholder data encryption
- Network segmentation
- Access controls
- Regular security testing

**VM Measures:**
```
✓ Isolated VLAN for payment processing VMs
✓ Encrypted VMs
✓ Distributed firewall (micro-segmentation)
✓ Regular vulnerability scans
✓ Penetration testing
```

---

## Conclusioni

La sicurezza nella virtualizzazione richiede un approccio multi-livello:

- **Isolation**: Hardware-assisted, VLAN, micro-segmentation
- **Hypervisor hardening**: Minimal attack surface, patching, RBAC
- **VM security**: Secure Boot, encryption, antivirus
- **Network security**: Firewall, IDS/IPS, traffic inspection
- **Compliance**: GDPR, HIPAA, PCI DSS
- **Defense in depth**: Multiple layers, assume breach

La sicurezza non è un prodotto, è un processo continuo.

---

## Domande di Autovalutazione

1. Quali sono i meccanismi di isolamento tra VM?
2. Cos'è un attacco di VM escape e come proteggersi?
3. Qual è la differenza tra VM encryption e guest encryption?
4. Come funziona Secure Boot e perché è importante?
5. Descrivi il principio di defense in depth
6. Quali sono i requisiti GDPR per VMs contenenti dati personali?
7. Come si hardening un hypervisor ESXi?

---

## Esercizi Pratici

### Lab 1: VM Isolation Testing
1. Create 2 VMs su VLANs diverse
2. Tenta ping tra VMs
3. Verify isolation
4. Configure firewall rules

### Lab 2: Secure Boot
1. Create VM con EFI firmware
2. Enable Secure Boot
3. Install Linux/Windows
4. Verify Secure Boot status
5. Try loading unsigned module (should fail)

### Lab 3: VM Encryption
1. Configure KMS
2. Encrypt existing VM
3. Verify encryption
4. Test vMotion (should be encrypted)
5. Measure performance impact

### Lab 4: Hypervisor Hardening
1. Audit current ESXi/KVM configuration
2. Apply hardening checklist
3. Document changes
4. Verify security posture improved

---

## Risorse Aggiuntive

- [VMware vSphere Security Guide](https://docs.vmware.com/en/VMware-vSphere/7.0/vsphere-esxi-vcenter-server-70-security-guide.pdf)
- [CIS Benchmarks](https://www.cisecurity.org/benchmark/vmware)
- [NIST Virtualization Security Guidelines](https://csrc.nist.gov/publications/detail/sp/800-125/final)
- [AMD SEV Documentation](https://developer.amd.com/sev/)
- [Intel SGX](https://www.intel.com/content/www/us/en/architecture-and-technology/software-guard-extensions.html)
- [OWASP Virtualization Security](https://owasp.org/www-community/vulnerabilities/Insecure_Virtualization)
