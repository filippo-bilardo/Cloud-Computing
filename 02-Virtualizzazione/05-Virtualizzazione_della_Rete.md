# 2.5 Virtualizzazione della Rete

## Indice
- [Virtual Switch](#virtual-switch)
- [VLAN e Network Segmentation](#vlan-e-network-segmentation)
- [Software-Defined Networking (SDN)](#software-defined-networking-sdn)
- [Network Function Virtualization (NFV)](#network-function-virtualization-nfv)
- [Virtual Router e Virtual Firewall](#virtual-router-e-virtual-firewall)

---

## Virtual Switch

### Architettura vSwitch

```
┌────────────────────────────────────────┐
│         Virtual Switch (Layer 2)       │
├────────────────────────────────────────┤
│  ┌──────────────────────────────────┐  │
│  │  Control Plane                   │  │
│  │  - MAC learning                  │  │
│  │  - VLAN handling                 │  │
│  │  - Port mirroring                │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  Data Plane                      │  │
│  │  - Packet forwarding             │  │
│  │  - ACLs                          │  │
│  │  - QoS                           │  │
│  └──────────────────────────────────┘  │
└────────────────┬───────────────────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
┌───▼───┐    ┌───▼───┐    ┌───▼───┐
│  VM1  │    │  VM2  │    │  VM3  │
└───────┘    └───────┘    └───────┘
```

### VMware vSwitch

#### Standard vSwitch (vSS)
- Per-host configuration
- Gestito singolarmente
- Gratuito

```bash
# Crea vSwitch
esxcli network vswitch standard add --vswitch-name=vSwitch1

# Aggiungi uplink (physical NIC)
esxcli network vswitch standard uplink add --uplink-name=vmnic1 --vswitch-name=vSwitch1

# Crea port group
esxcli network vswitch standard portgroup add --portgroup-name=Production --vswitch-name=vSwitch1

# Configura VLAN
esxcli network vswitch standard portgroup set --portgroup-name=Production --vlan-id=100
```

#### Distributed vSwitch (vDS)
- Gestione centralizzata (vCenter)
- Configurazione consistente
- Funzionalità avanzate
- Licensing required

```
┌──────────────────────────────────┐
│      vCenter Server              │
│   (Distributed vSwitch Control)  │
└───────┬────────┬────────┬────────┘
        │        │        │
   ┌────▼───┐┌───▼───┐┌───▼───┐
   │ ESXi 1 ││ESXi 2 ││ESXi 3 │
   │ [vDS]  ││[vDS]  ││[vDS]  │
   └────────┘└───────┘└───────┘
   
Single pane of glass management
```

### Linux Bridge

```bash
# Crea bridge
brctl addbr br0

# Aggiungi physical interface
brctl addif br0 eth0

# Aggiungi VM interface (automatico con libvirt)
brctl show br0
```

**Configurazione persistente (/etc/network/interfaces):**
```bash
auto br0
iface br0 inet static
    address 192.168.1.10
    netmask 255.255.255.0
    gateway 192.168.1.1
    bridge_ports eth0
    bridge_stp off
    bridge_fd 0
```

### Open vSwitch (OVS)

Open-source multilayer virtual switch con funzionalità SDN:

```bash
# Installa OVS
apt-get install openvswitch-switch

# Crea bridge
ovs-vsctl add-br ovsbr0

# Aggiungi port
ovs-vsctl add-port ovsbr0 eth0

# VLAN tagging
ovs-vsctl set port vnet0 tag=100

# OpenFlow controller
ovs-vsctl set-controller ovsbr0 tcp:192.168.1.1:6633

# Mostra configurazione
ovs-vsctl show
```

**Caratteristiche OVS:**
- OpenFlow support
- VXLAN/GRE tunneling
- Port mirroring (SPAN)
- QoS
- NetFlow/sFlow
- Bonding/LACP

---

## VLAN e Network Segmentation

### VLAN 802.1Q

```
Single Physical Network → Multiple Logical Networks

Physical Switch Port (Trunk)
         │
    ┌────▼─────┐
    │ vSwitch  │
    └────┬─────┘
         │
    ┌────┼──────┬──────┬──────┐
    │    │      │      │      │
VLAN 10  20    30    40    50
 Mgmt   Prod   Dev   Test  DMZ
```

### VLAN Configuration Examples

**VMware:**
```bash
# Port group con VLAN
esxcli network vswitch standard portgroup set \
  --portgroup-name=Production \
  --vlan-id=100

# Trunk (all VLANs)
--vlan-id=4095
```

**Linux (OVS):**
```bash
# Access port (untagged)
ovs-vsctl set port vnet0 tag=100

# Trunk port (tagged)
ovs-vsctl set port eth0 trunks=10,20,30,40
```

**KVM libvirt:**
```xml
<interface type='network'>
  <source network='default'/>
  <vlan>
    <tag id='100'/>
  </vlan>
</interface>
```

### Network Isolation

```
3-Tier Application:

┌─────────────────────────────────┐
│  Web Tier (VLAN 100 - DMZ)      │
│  Public access                  │
└──────────┬──────────────────────┘
           │ Firewall rules
┌──────────▼──────────────────────┐
│  App Tier (VLAN 200 - Internal) │
│  No direct internet access      │
└──────────┬──────────────────────┘
           │ DB firewall
┌──────────▼──────────────────────┐
│  DB Tier (VLAN 300 - Secure)    │
│  Highly restricted              │
└─────────────────────────────────┘
```

---

## Software-Defined Networking (SDN)

### SDN Architecture

```
┌────────────────────────────────────┐
│   Application Layer                │
│   (Business Applications)          │
└────────────┬───────────────────────┘
             │ APIs
┌────────────▼───────────────────────┐
│   Control Layer (SDN Controller)   │
│   - Centralized brain              │
│   - Network logic                  │
│   - OpenFlow controller            │
└────────────┬───────────────────────┘
             │ OpenFlow / OVSDB
┌────────────▼───────────────────────┐
│   Infrastructure Layer             │
│   (vSwitches, physical switches)   │
│   - Packet forwarding              │
│   - Flow tables                    │
└────────────────────────────────────┘
```

### OpenFlow

Protocol per programmare flow tables:

```
Flow Table Entry:
┌──────────────┬─────────┬───────────┐
│ Match Fields │ Actions │ Counters  │
├──────────────┼─────────┼───────────┤
│ src_ip=A     │ forward │ packets:  │
│ dst_ip=B     │ port 3  │ 1523      │
│ protocol=TCP │         │ bytes:    │
│ dst_port=80  │         │ 892340    │
└──────────────┴─────────┴───────────┘
```

**Example flow (block traffic):**
```bash
# OVS OpenFlow rule
ovs-ofctl add-flow br0 "priority=100,ip,nw_src=10.0.0.5,actions=drop"

# Allow HTTP to web server
ovs-ofctl add-flow br0 "priority=200,tcp,nw_dst=192.168.1.10,tp_dst=80,actions=output:3"
```

### SDN Controllers

#### OpenDaylight
```
Java-based SDN controller
- OpenFlow 1.0/1.3
- OVSDB
- BGP/PCEP protocols
- REST APIs
```

#### ONOS (Open Network Operating System)
```
Carrier-grade SDN:
- High availability
- Scalability
- Performance
- Intent-based networking
```

#### VMware NSX
```
Commercial SDN platform:
- Overlay networking (VXLAN)
- Distributed firewall
- Load balancing
- VPN
```

### Overlay Networks (VXLAN)

```
VXLAN encapsulation:

┌───────────────────────────────────┐
│  Inner: Ethernet Frame            │
│  [Src MAC][Dst MAC][IP][Data]     │
└──────────┬────────────────────────┘
           │ Encapsulation
┌──────────▼────────────────────────┐
│  VXLAN Header (VNI: 10000)        │
├───────────────────────────────────┤
│  UDP Header (Port 4789)           │
├───────────────────────────────────┤
│  IP Header (VTEP A → VTEP B)      │
├───────────────────────────────────┤
│  Ethernet Header                  │
└───────────────────────────────────┘

VNI = VXLAN Network Identifier (16M networks vs 4K VLANs)
```

**Configure VXLAN (Linux):**
```bash
# Create VXLAN interface
ip link add vxlan10 type vxlan \
  id 10 \
  dev eth0 \
  remote 192.168.1.20 \
  local 192.168.1.10 \
  dstport 4789

# Add to bridge
brctl addif br0 vxlan10
```

---

## Network Function Virtualization (NFV)

### Concept

```
Traditional:
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Firewall │→ │   IDS    │→ │Load Bal. │
│(Hardware)│  │(Hardware)│  │(Hardware)│
└──────────┘  └──────────┘  └──────────┘
   Vendor       Vendor        Vendor
   Lock-in      Lock-in       Lock-in

NFV:
┌───────────────────────────────────────┐
│       Virtualization Platform         │
│  ┌────────┐  ┌─────┐  ┌────────────┐ │
│  │Firewall│→ │ IDS │→ │Load Balance│ │
│  │  (VM)  │  │(VM) │  │    (VM)    │ │
│  └────────┘  └─────┘  └────────────┘ │
│       Software-based, flexible        │
└───────────────────────────────────────┘
```

### VNF Examples

#### Virtual Firewall (pfSense, VyOS)
```
Deployed as VM:
- Packet filtering
- NAT
- VPN termination
- Traffic shaping
```

#### Virtual Load Balancer (HAProxy, NGINX)
```
Features:
- L4/L7 load balancing
- SSL termination
- Health checks
- Auto-scaling integration
```

#### Virtual Router (VyOS, VNF-router)
```
Routing functions:
- BGP/OSPF
- Static routes
- Policy-based routing
- QoS
```

### NFV MANO (Management and Orchestration)

```
┌──────────────────────────────────┐
│    NFV Orchestrator              │
│    (Service orchestration)       │
└───────────┬──────────────────────┘
            │
┌───────────▼──────────────────────┐
│    VNF Manager                   │
│    (Lifecycle management)        │
│    - Instantiation               │
│    - Scaling                     │
│    - Healing                     │
│    - Termination                 │
└───────────┬──────────────────────┘
            │
┌───────────▼──────────────────────┐
│    Virtualized Infrastructure    │
│    Manager (VIM)                 │
│    (Resource management)         │
└──────────────────────────────────┘
```

---

## Virtual Router e Virtual Firewall

### Virtual Router

#### VyOS

Open-source network OS:

```bash
# Install VyOS as VM

# Basic configuration
configure

# Set interfaces
set interfaces ethernet eth0 address '192.168.1.1/24'
set interfaces ethernet eth1 address '10.0.0.1/24'

# NAT
set nat source rule 10 outbound-interface 'eth0'
set nat source rule 10 source address '10.0.0.0/24'
set nat source rule 10 translation address 'masquerade'

# Static route
set protocols static route 0.0.0.0/0 next-hop 192.168.1.254

# Commit
commit
save
```

#### MikroTik CHR (Cloud Hosted Router)

```
Deployed as VM in cloud:
- Full RouterOS features
- BGP/OSPF
- VPN (IPSec, OpenVPN, PPTP)
- Firewall
- QoS
- Licensing: Pay-per-performance
```

### Virtual Firewall

#### pfSense

```
Features:
- Stateful firewall
- NAT/PAT
- VPN (IPSec, OpenVPN)
- Traffic shaper
- Captive portal
- HA (CARP)
```

**Deployment:**
```
1. Download pfSense ISO
2. Create VM:
   - 2 vCPU
   - 2 GB RAM
   - 2 NIC (WAN + LAN)
   - 8 GB disk
3. Install
4. Configure via web interface (https://lan-ip)
```

**Firewall Rules Example:**
```
WAN Rules:
├─ Block all incoming (default)
├─ Allow established/related
└─ Allow specific ports (VPN, web)

LAN Rules:
├─ Allow all outgoing
├─ Block to firewall except web UI
└─ Allow inter-VLAN as needed
```

#### OPNsense

Fork of pfSense with:
- Modern UI
- Inline IPS (Suricata)
- Two-factor authentication
- Business support

### Virtual IPS/IDS

#### Suricata

```bash
# Install as VM or container
apt-get install suricata

# Configure interface (/etc/suricata/suricata.yaml)
af-packet:
  - interface: eth0
    threads: auto
    cluster-id: 99

# Update rules
suricata-update

# Start
systemctl start suricata

# Monitor
tail -f /var/log/suricata/fast.log
```

#### Snort

```
Inline mode:
Internet → vNIC1 → Snort VM → vNIC2 → Internal Network

Alert/Block malicious traffic:
- Signature-based detection
- Protocol anomaly detection
- Rules engine
```

### Service Chaining

```
Traffic flow through VNFs:

Internet
   ↓
Firewall VNF (permit/deny)
   ↓
IDS/IPS VNF (inspect)
   ↓
Load Balancer VNF (distribute)
   ↓
Web Server VMs
   ↓
Application VNF (process)
   ↓
Database VM
```

**Implementation (NSX):**
```
1. Define service chain policy
2. Traffic matching criteria
3. Order of VNFs
4. Steering method (encapsulation/routing)
```

---

## Best Practices

### vSwitch Design
✅ Separate traffic types (management, vMotion, VM, storage)  
✅ Use VLANs for isolation  
✅ Enable NIC teaming for redundancy  
✅ Monitor bandwidth utilization  
✅ Document port group naming conventions  

### SDN/NFV
✅ Start small (pilot project)  
✅ Plan for controller redundancy  
✅ Automate configuration (IaC)  
✅ Monitor flow tables  
✅ Version control network configs  

### Virtual Appliances
✅ Right-size resources (don't over-provision)  
✅ Place on appropriate network segments  
✅ Regular updates/patching  
✅ Backup configurations  
✅ Test failover procedures  

---

## Conclusioni

La virtualizzazione di rete trasforma datacenter tradizionali:

- **vSwitch**: Foundation di networking virtuale
- **VLAN**: Network segmentation e isolation
- **SDN**: Programmabilità e automazione
- **NFV**: Flessibilità e costi ridotti
- **Virtual appliances**: Security e routing software-based

Il futuro è software-defined, agile, automated.

---

## Domande di Autovalutazione

1. Qual è la differenza tra vSwitch standard e distributed?
2. Come funziona VXLAN e perché è vantaggioso rispetto a VLAN?
3. Descrivi l'architettura SDN a 3 livelli
4. Quali sono i benefici di NFV rispetto a appliance hardware?
5. Quando useresti un virtual router invece di un router fisico?

---

## Esercizi Pratici

### Lab 1: vSwitch Configuration
1. Crea vSwitch con 2 port groups
2. Assegna VLAN diverse
3. Testa connectivity tra VM stessa VLAN
4. Verifica isolation tra VLAN

### Lab 2: Open vSwitch + OpenFlow
1. Installa OVS su Linux
2. Configura controller (Ryu/POX)
3. Scrivi flow rules
4. Monitor traffic

### Lab 3: pfSense Deployment
1. Deploy pfSense VM
2. Configura WAN/LAN
3. Crea firewall rules
4. Setup VPN

---

## Risorse Aggiuntive

- [VMware NSX Documentation](https://docs.vmware.com/en/VMware-NSX/)
- [Open vSwitch Documentation](https://docs.openvswitch.org/)
- [OpenDaylight SDN Platform](https://www.opendaylight.org/)
- [pfSense Documentation](https://docs.netgate.com/pfsense/)
- [VyOS User Guide](https://docs.vyos.io/)
- [NFV White Paper (ETSI)](https://www.etsi.org/technologies/nfv)
