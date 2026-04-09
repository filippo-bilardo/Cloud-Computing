# Esercitazione 1: Virtualizzazione con VirtualBox

## Obiettivi dell'Esercitazione

In questa esercitazione pratica imparerai a:
1. Installare e configurare VirtualBox
2. Abilitare la virtualizzazione nel BIOS/UEFI
3. Creare e configurare macchine virtuali
4. Installare sistemi operativi guest (Windows e Linux)
5. Installare e configurare Guest Additions/VMware Tools
6. Gestire le operazioni quotidiane delle VM (snapshot, cloni, backup)

**Durata stimata:** 3-4 ore

---

## Parte 1: Preparazione dell'Ambiente

### 1.1 Requisiti di Sistema

**Hardware minimo:**
- CPU: Processore con supporto virtualizzazione (Intel VT-x o AMD-V)
- RAM: 8 GB (consigliato 16 GB)
- Storage: 50-100 GB di spazio libero
- Sistema operativo: Windows 10/11, macOS, o Linux

**Software necessario:**
- VirtualBox (ultima versione stabile)
- ISO dei sistemi operativi guest:
  - Ubuntu Desktop LTS (esempio: 22.04)
  - Windows 10/11 (evaluation o licenza)

### 1.2 Download VirtualBox

1. Visita: https://www.virtualbox.org/
2. Scarica la versione per il tuo sistema operativo
3. Scarica anche il VirtualBox Extension Pack

**Link diretti:**
```
VirtualBox: https://www.virtualbox.org/wiki/Downloads
Extension Pack: Stesso link, sezione Extension Pack
```

### 1.3 Installazione VirtualBox

#### Su Windows:
```cmd
1. Esegui VirtualBox-x.x.x-Win.exe
2. Segui la procedura guidata
3. Accetta tutte le impostazioni predefinite
4. Durante l'installazione potrebbero essere installati driver di rete
   (connessione temporaneamente interrotta)
5. Completa l'installazione e riavvia se richiesto
```

#### Su macOS:
```bash
1. Apri VirtualBox-x.x.x-macOS.dmg
2. Trascina VirtualBox nelle Applicazioni
3. Vai in Preferenze di Sistema → Sicurezza e Privacy
4. Autorizza il software Oracle
5. Riavvia se necessario
```

#### Su Linux (Ubuntu/Debian):
```bash
# Metodo 1: Repository ufficiale
sudo apt update
sudo apt install virtualbox virtualbox-ext-pack

# Metodo 2: Download manuale
wget https://download.virtualbox.org/virtualbox/7.0.x/virtualbox-7.0_xxx_amd64.deb
sudo dpkg -i virtualbox-7.0_xxx_amd64.deb
sudo apt-get install -f
```

### 1.4 Installazione Extension Pack

```
1. Apri VirtualBox
2. File → Preferenze → Estensioni
3. Click sul "+" per aggiungere
4. Seleziona il file .vbox-extpack scaricato
5. Accetta la licenza
6. Inserisci la password se richiesta
```

**Funzionalità dell'Extension Pack:**
- Supporto USB 2.0 e 3.0
- VirtualBox RDP (Remote Desktop Protocol)
- Crittografia disco
- Boot PXE per schede Intel

---

## Parte 2: Abilitazione Virtualizzazione nel BIOS

### 2.1 Verifica Supporto Virtualizzazione

#### Su Windows:
```cmd
# Apri Task Manager (Ctrl + Shift + Esc)
# Vai alla tab "Prestazioni" → CPU
# Controlla "Virtualizzazione: Abilitata/Disabilitata"

# Oppure usa systeminfo
systeminfo | findstr /i "Hyper-V"
```

#### Su Linux:
```bash
# Verifica supporto Intel VT-x
egrep -c '(vmx|svm)' /proc/cpuinfo
# Se il risultato è > 0, la virtualizzazione è supportata

# Verifica se è abilitata
lscpu | grep Virtualization

# Verifica moduli kernel
lsmod | grep kvm
```

#### Su macOS:
```bash
sysctl -a | grep machdep.cpu.features | grep VMX
# Se VMX è presente, la virtualizzazione è supportata
```

### 2.2 Accesso al BIOS/UEFI

**Tasti comuni per entrare nel BIOS:**
- Dell: F2 o F12
- HP: F10 o Esc
- Lenovo: F1 o F2
- ASUS: F2 o Del
- Acer: F2 o Del
- MSI: Del

**Procedura:**
```
1. Riavvia il computer
2. Premi ripetutamente il tasto appropriato all'avvio
3. Cerca la sezione "Advanced" o "CPU Configuration"
4. Cerca voci come:
   - Intel Virtualization Technology (VT-x)
   - AMD-V
   - SVM Mode
   - Virtualization Extensions
5. Imposta su "Enabled"
6. Salva (solitamente F10) e riavvia
```

### 2.3 Disabilitazione Hyper-V (Solo Windows)

Se hai Hyper-V abilitato, VirtualBox potrebbe non funzionare correttamente.

```powershell
# Apri PowerShell come Amministratore
# Disabilita Hyper-V
bcdedit /set hypervisorlaunchtype off

# Riavvia il computer
shutdown /r /t 0

# Per riabilitare Hyper-V (se necessario)
bcdedit /set hypervisorlaunchtype auto
```

**Alternativa con GUI:**
```
1. Pannello di Controllo
2. Programmi → Attiva o disattiva funzionalità Windows
3. Deseleziona "Hyper-V"
4. Riavvia
```

---

## Parte 3: Creazione della Prima VM (Linux Ubuntu)

### 3.1 Download ISO Ubuntu

```
1. Vai su: https://ubuntu.com/download/desktop
2. Scarica Ubuntu 22.04 LTS Desktop (circa 4 GB)
3. Salva il file .iso in una cartella dedicata
```

### 3.2 Creazione Nuova Macchina Virtuale

**Procedura passo-passo:**

```
1. Apri VirtualBox
2. Click su "Nuova" (o Machine → New)

STEP 1: Nome e Sistema Operativo
- Nome: Ubuntu-22.04-Lab
- Cartella: [Scegli dove salvare le VM]
- Tipo: Linux
- Versione: Ubuntu (64-bit)
  ⚠️ Se vedi solo versioni (32-bit), la virtualizzazione non è abilitata!
- Click "Avanti"

STEP 2: Dimensione Memoria
- RAM: 2048 MB (minimo) - 4096 MB (consigliato)
- Regola il cursore o inserisci il valore
  💡 Lascia almeno 4 GB per il sistema host
- Click "Avanti"

STEP 3: Disco Rigido
- Seleziona: "Crea subito un disco rigido virtuale"
- Click "Crea"

STEP 4: Tipo di File Disco
- Seleziona: VDI (VirtualBox Disk Image)
- Click "Avanti"

STEP 5: Archiviazione su Disco Fisico
- Seleziona: "Allocato dinamicamente"
  💡 Il file crescerà man mano che usi spazio
- Click "Avanti"

STEP 6: Dimensione File e Posizione
- Dimensione: 25 GB (minimo per Ubuntu Desktop)
- Posizione: [Conferma o modifica]
- Click "Crea"
```

### 3.3 Configurazione Avanzata della VM

Prima di avviare, ottimizza le impostazioni:

```
1. Seleziona la VM "Ubuntu-22.04-Lab"
2. Click su "Impostazioni"

🖥️ SISTEMA:
- Scheda Madre:
  - Ordine di avvio: Disco ottico, Disco rigido
  - Chipset: PIIX3 o ICH9
  - Dispositivo di puntamento: Tablet USB
  
- Processore:
  - CPU: 2 (se ne hai 4 o più)
  - Limite di esecuzione: 100%
  - ✅ Abilita PAE/NX
  
- Accelerazione:
  - ✅ Abilita VT-x/AMD-V
  - ✅ Abilita Paginazione Nidificata

📀 ARCHIVIAZIONE:
- Controller: IDE / SATA
- Click sull'icona disco vuota sotto "Controller IDE"
- Click sull'icona disco a destra (Ottico)
- Scegli "Scegli/Crea un disco ottico virtuale"
- Click "Aggiungi" e seleziona ubuntu-22.04.x-desktop-amd64.iso
- Click "Scegli"

🖼️ SCHERMO:
- Memoria Video: 128 MB
- ✅ Abilita Accelerazione 3D (opzionale)
- Monitor: 1
- Fattore di scala: 100%

🌐 RETE:
- Scheda 1:
  - ✅ Abilita scheda di rete
  - Connessa a: NAT
  - Tipo scheda: Intel PRO/1000 MT Desktop

💾 CARTELLE CONDIVISE (configureremo dopo):
- Lascia vuoto per ora

Click "OK" per salvare
```

### 3.4 Installazione Ubuntu

**Avvio della VM:**
```
1. Seleziona "Ubuntu-22.04-Lab"
2. Click "Avvia"
3. La VM farà boot dal file ISO
```

**Procedura di installazione:**

```
BOOT:
- Aspetta il caricamento del LiveCD
- Seleziona lingua: "Italiano" o "English"
- Click "Prova Ubuntu" o "Installa Ubuntu"

INSTALLAZIONE:
1. Lingua tastiera: Italiana
   - Click "Continua"

2. Aggiornamenti e altro software:
   - ✅ Installazione normale
   - ✅ Scarica aggiornamenti durante l'installazione
   - ✅ Installa software di terze parti
   - Click "Continua"

3. Tipo di installazione:
   - ⚪ Cancella disco e installa Ubuntu
   - Click "Installa ora"
   - Conferma modifiche al disco: "Continua"

4. Fuso orario:
   - Seleziona la tua città
   - Click "Continua"

5. Informazioni utente:
   - Nome: [tuo nome]
   - Nome computer: ubuntu-vm
   - Nome utente: student
   - Password: [scegli una password]
   - ⚪ Richiedere password per accedere
   - Click "Continua"

6. Installazione in corso:
   - Aspetta 10-20 minuti
   - Al termine: Click "Riavvia ora"

7. Rimozione supporto:
   - Premi INVIO quando richiesto
   - La VM si riavvierà

8. Login:
   - Inserisci la password
   - Completa la configurazione iniziale
```

### 3.5 Prime Verifiche

Dopo il login, apri il terminale (Ctrl + Alt + T):

```bash
# Verifica versione sistema
lsb_release -a

# Verifica CPU
lscpu | grep "CPU(s)"

# Verifica RAM
free -h

# Verifica disco
df -h

# Verifica rete
ip addr show
ping -c 4 google.com

# Aggiorna sistema
sudo apt update
sudo apt upgrade -y
```

---

## Parte 4: Installazione Guest Additions

Le Guest Additions migliorano drasticamente l'esperienza:
- Schermo a risoluzione completa
- Clipboard condivisa
- Drag & Drop
- Cartelle condivise
- Migliore performance grafica

### 4.1 Installazione su Ubuntu

```bash
# Installa dipendenze
sudo apt update
sudo apt install -y build-essential dkms linux-headers-$(uname -r)

# Dal menu VirtualBox della VM:
# Dispositivi → Inserisci immagine CD Guest Additions

# Monta il CD (se non automatico)
sudo mkdir -p /media/cdrom
sudo mount /dev/cdrom /media/cdrom

# Esegui l'installer
cd /media/cdrom
sudo ./VBoxLinuxAdditions.run

# Attendi il completamento
# Output: "VirtualBox Guest Additions: Starting."
# Output: "VirtualBox Guest Additions: Building the modules for kernel..."

# Riavvia
sudo reboot
```

**Verifica installazione:**
```bash
# Controlla moduli caricati
lsmod | grep vbox

# Output atteso:
# vboxguest
# vboxsf
# vboxvideo

# Verifica versione
VBoxClient --version
```

### 4.2 Configurazione Funzionalità

**Clipboard condivisa:**
```
VM → Dispositivi → Appunti condivisi → Bidirezionale
```

**Drag & Drop:**
```
VM → Dispositivi → Drag and Drop → Bidirezionale
```

**Schermo intero:**
```
Premi: Host+F (Host = Ctrl destro su Windows/Linux, Cmd su macOS)
O: Visualizza → Schermo intero
```

**Risoluzione automatica:**
```
Ridimensiona la finestra VirtualBox
→ La risoluzione del guest si adatterà automaticamente
```

---

## Parte 5: Creazione VM Windows

### 5.1 Download ISO Windows

**Windows 11 Evaluation:**
```
URL: https://www.microsoft.com/en-us/evalcenter/evaluate-windows-11-enterprise
Validità: 90 giorni (può essere estesa)
```

**Windows 10 Media Creation Tool:**
```
URL: https://www.microsoft.com/software-download/windows10
Crea ISO tramite tool ufficiale
```

### 5.2 Creazione VM Windows

```
CONFIGURAZIONE:
- Nome: Windows-11-Lab
- Tipo: Microsoft Windows
- Versione: Windows 11 (64-bit)
- RAM: 4096 MB (minimo per Win11)
- Disco: 50 GB (allocato dinamicamente)

IMPOSTAZIONI SPECIALI PER WINDOWS 11:
Sistema → Scheda Madre:
- ✅ Abilita EFI
  
Sistema → Processore:
- CPU: 2 o più

Sistema → Sicurezza (tab aggiuntivo):
- ✅ Abilita Secure Boot
- ✅ Abilita TPM 2.0 (VirtualBox 7.0+)
```

### 5.3 Installazione Windows

```
1. Avvia la VM
2. Boot da ISO Windows
3. Premi un tasto per avviare da CD/DVD
4. Seleziona lingua: Italiano
5. Click "Installa"
6. Inserisci product key o "Non ho un codice"
7. Seleziona versione: Windows 11 Pro
8. Accetta licenza
9. Tipo installazione: Personalizzata
10. Seleziona disco VirtualBox (non partizionato)
11. Click "Avanti"
12. Attendi installazione (15-30 min)
13. Configurazione OOBE:
    - Regione: Italia
    - Layout tastiera: Italiana
    - Nome PC: WIN11-VM
    - Account locale (se possibile)
14. Completa configurazione privacy
```

### 5.4 Installazione Guest Additions su Windows

```
1. Dopo il login in Windows
2. VirtualBox menu: Dispositivi → Inserisci immagine CD Guest Additions
3. Apri Esplora File
4. Apri unità CD (D:)
5. Esegui: VBoxWindowsAdditions.exe
6. Segui procedura guidata:
   - Next → Next → Install
   - Accetta installazione driver (più volte)
7. Riavvia quando richiesto
```

**Verifica:**
```
- Prova a ridimensionare finestra → risoluzione si adatta
- Copia/incolla tra host e guest
- Drag & drop file
```

---

## Parte 6: Gestione Quotidiana delle VM

### 6.1 Snapshot (Istantanee)

Gli snapshot salvano lo stato completo della VM.

**Creare uno snapshot:**
```
1. Con VM in esecuzione o spenta
2. Click su "Snapshots" (icona hamburger accanto al nome VM)
3. Click "Scatta" (icona macchina fotografica)
4. Nome: "Ubuntu Fresh Install"
5. Descrizione: "Subito dopo installazione e Guest Additions"
6. ✅ Includi RAM della macchina (se VM accesa)
7. Click "OK"
```

**Ripristinare snapshot:**
```
1. Ferma la VM (se accesa)
2. Vai in "Snapshot"
3. Seleziona lo snapshot desiderato
4. Click "Ripristina"
5. ✅ Crea snapshot dello stato attuale (opzionale)
6. Click "Ripristina"
```

**Eliminare snapshot:**
```
1. Seleziona snapshot da eliminare
2. Click "Elimina"
3. Conferma
⚠️ L'eliminazione potrebbe richiedere tempo (merge dei dischi)
```

### 6.2 Clonazione VM

Crea una copia indipendente della VM.

**Clona VM:**
```
1. Ferma la VM
2. Click destro sulla VM → "Clona"
3. Nome: "Ubuntu-22.04-Clone"
4. Cartella: [scegli posizione]
5. Politica indirizzo MAC: "Genera nuovi indirizzi MAC"
6. ✅ Mantieni nomi interfacce di rete
7. Click "Avanti"
8. Tipo di clone:
   - ⚪ Clone completo (copia completa, indipendente)
   - ⚪ Clone collegato (risparmia spazio, dipende dall'originale)
9. Snapshot da clonare:
   - ⚪ Stato macchina attuale
   - ⚪ Tutti i dati
10. Click "Clona"
11. Attendi completamento
```

**Differenze:**
- **Clone completo:** VM totalmente indipendente, richiede spazio disco completo
- **Clone collegato:** Usa l'originale come base, occupa meno spazio, ma dipende dalla VM originale

### 6.3 Export/Import (OVA)

Per spostare VM tra computer o fare backup.

**Esportare VM:**
```
1. Ferma la VM
2. File → Esporta applicazione virtuale
3. Seleziona la VM da esportare
4. Click "Avanti"
5. Formato:
   - Open Virtualization Format 1.0
   - Open Virtualization Format 2.0
   - Oracle Cloud Infrastructure
6. File: scegli nome e posizione (es: ubuntu-vm.ova)
7. Opzioni:
   - Politica indirizzo MAC: Strip all
   - ✅ Includi manifest
   - ☐ Includi immagini ISO (opzionale)
8. Click "Avanti"
9. Controlla informazioni appliance
10. Click "Esporta"
11. Attendi (può richiedere molto tempo)
```

**Importare VM:**
```
1. File → Importa applicazione virtuale
2. Scegli file .ova
3. Click "Avanti"
4. Controlla/modifica impostazioni:
   - Nome VM
   - CPU
   - RAM
   - Cartella base VM
5. Politica indirizzo MAC: "Genera nuovi indirizzi MAC"
6. Click "Importa"
7. Attendi completamento
```

### 6.4 Stati della VM

**Avvio:**
```
- Avvio normale: avvia la VM con interfaccia grafica
- Avvio headless: avvia senza interfaccia (background)
- Avvio detached: avvia in finestra separata
```

**Salvataggio stato:**
```
- Salva stato macchina: congela la VM (RAM salvata su disco)
  Equivalente a "ibernazione"
- Arresto ACPI: invia segnale spegnimento al SO guest
- Spegni: spegnimento forzato (come staccare la spina)
  ⚠️ Usare solo in caso di blocco
```

### 6.5 Cartelle Condivise

Condividi cartelle tra host e guest.

**Configurazione:**
```
1. Con VM spenta o accesa
2. Impostazioni → Cartelle condivise
3. Click icona "+" (aggiungi)
4. Percorso cartella: scegli cartella su host (es: C:\SharedFolder)
5. Nome cartella: shared (nome che apparirà nel guest)
6. ✅ Automatico
7. ✅ Rendi permanente
8. Click "OK"
```

**Accesso da Ubuntu:**
```bash
# Aggiungi utente al gruppo vboxsf
sudo usermod -aG vboxsf $USER

# Logout e login

# Accedi alla cartella
cd /media/sf_shared

# Oppure crea mount point manuale
sudo mkdir /home/$USER/shared
sudo mount -t vboxsf shared /home/$USER/shared

# Mount automatico al boot
echo "shared /home/$USER/shared vboxsf defaults 0 0" | sudo tee -a /etc/fstab
```

**Accesso da Windows:**
```
- Esplora file → Rete → VBOXSVR → shared
Oppure:
- Map network drive: \\VBOXSVR\shared
```

### 6.6 Gestione Risorse

**Modificare RAM:**
```
1. Ferma la VM
2. Impostazioni → Sistema → Scheda Madre
3. Modifica "Memoria base"
4. OK
```

**Modificare CPU:**
```
1. Ferma la VM
2. Impostazioni → Sistema → Processore
3. Modifica numero di CPU
4. OK
```

**Ridimensionare disco:**
```bash
# Da terminale host (VM spenta)
# Percorso VirtualBox installation
cd "C:\Program Files\Oracle\VirtualBox"

# Su Linux/macOS
cd /usr/lib/virtualbox

# Comando resize (esempio: espandi a 50 GB)
VBoxManage modifymedium disk "C:\path\to\ubuntu.vdi" --resize 51200

# 51200 MB = 50 GB
# Dopo aver espanso, devi ridimensionare la partizione nel guest
```

**Ridimensionare partizione nel guest (Ubuntu):**
```bash
# Installa GParted
sudo apt install gparted

# Esegui GParted
sudo gparted

# GUI: ridimensiona la partizione per usare tutto lo spazio
```

---

## Parte 7: Networking

### 7.1 Modalità di Rete

**NAT (default):**
```
- VM può accedere a Internet
- VM non è accessibile dall'esterno
- Più VM NAT non comunicano tra loro
Use case: navigazione web, download
```

**NAT Network:**
```
1. File → Preferenze → Rete
2. Tab "NAT Networks"
3. Click "+" per aggiungere
4. Nome: NatNetwork
5. CIDR: 10.0.2.0/24
6. ✅ Supporta DHCP
7. OK

Per ogni VM:
- Impostazioni → Rete → Scheda 1
- Connessa a: Rete NAT
- Nome: NatNetwork

Use case: VM che devono comunicare tra loro e con Internet
```

**Bridged (Con bridge):**
```
- VM appare come computer separato sulla rete fisica
- Ottiene IP dal router di rete
- Accessibile da altri dispositivi sulla rete
Use case: server accessibili, testing rete reale
```

**Host-Only:**
```
- VM comunica solo con host
- Rete privata isolata
Use case: test sicuri, ambiente isolato
```

**Internal Network:**
```
- VM comunicano solo tra loro
- Nessun accesso host o esterno
Use case: simulazione rete privata
```

### 7.2 Port Forwarding (con NAT)

Per accedere a servizi nella VM:

```
1. Impostazioni → Rete → Scheda 1 (NAT)
2. Avanzate → Port forwarding
3. Click "+"
4. Esempio per SSH:
   - Nome: SSH
   - Protocollo: TCP
   - IP host: 127.0.0.1
   - Porta host: 2222
   - IP guest: (lascia vuoto)
   - Porta guest: 22
5. OK

Ora puoi accedere:
ssh -p 2222 student@localhost
```

---

## Parte 8: Troubleshooting Comuni

### Problema: VM molto lenta

**Soluzioni:**
```
1. Aumenta RAM (almeno 2 GB per Linux, 4 GB per Windows)
2. Aumenta CPU (almeno 2 core)
3. Abilita VT-x/AMD-V nel BIOS
4. Installa Guest Additions
5. Usa allocazione dinamica disco
6. Disabilita effetti grafici nel guest
```

### Problema: Schermo piccolo, non ridimensionabile

**Soluzione:**
```
1. Installa Guest Additions
2. Riavvia VM
3. Abilita: Visualizza → Schermo ridimensionato automaticamente
```

### Problema: Clipboard non funziona

**Soluzione:**
```
1. Installa/reinstalla Guest Additions
2. Dispositivi → Appunti condivisi → Bidirezionale
3. Su Linux, potrebbe servire:
   ps aux | grep VBoxClient
   VBoxClient --clipboard
```

### Problema: USB non funziona

**Soluzione:**
```
1. Installa Extension Pack
2. Dispositivi → USB → Seleziona dispositivo
3. Su Linux host, aggiungi utente al gruppo:
   sudo usermod -aG vboxusers $USER
   Logout/login
```

### Problema: VT-x non disponibile

**Soluzione:**
```
1. Riavvia e entra nel BIOS
2. Abilita Intel VT-x o AMD-V
3. Su Windows, disabilita Hyper-V:
   bcdedit /set hypervisorlaunchtype off
4. Riavvia
```

### Problema: VM non si avvia dopo aggiornamento

**Soluzione:**
```
1. VBoxManage list vms  # lista VM
2. VBoxManage startvm "NomeVM" --type headless
3. Controlla log: click destro VM → Mostra log
4. Ricostruisci configurazione:
   File → Strumenti → Media Manager
   Verifica integrità dischi
```

---

## Parte 9: Best Practices

### 9.1 Organizzazione

```
Struttura cartelle consigliata:
C:\VirtualMachines\
├── Ubuntu-22.04-Lab\
│   ├── Ubuntu-22.04-Lab.vdi
│   ├── Ubuntu-22.04-Lab.vbox
│   └── Snapshots\
├── Windows-11-Lab\
│   ├── Windows-11-Lab.vdi
│   └── Windows-11-Lab.vbox
└── ISOs\
    ├── ubuntu-22.04-desktop.iso
    └── windows-11.iso
```

### 9.2 Snapshot Strategy

```
1. Snapshot "Fresh Install" - dopo installazione OS
2. Snapshot "Post Configuration" - dopo setup base
3. Snapshot "Before Update" - prima di aggiornamenti maggiori
4. Snapshot "Before Experiment" - prima di test rischiosi

⚠️ Non tenere troppi snapshot (rallenta VM)
Elimina snapshot vecchi dopo merge
```

### 9.3 Performance

```
✅ DO:
- Usa allocazione dinamica per risparmiare spazio
- Installa sempre Guest Additions
- Assegna CPU/RAM appropriate
- Usa SSD per file VM
- Disabilita effetti grafici non necessari nel guest

❌ DON'T:
- Non overcommit RAM (lascia abbastanza per host)
- Non eseguire troppe VM contemporaneamente
- Non usare snapshot come backup permanente
- Non dimenticare di aggiornare Guest Additions
```

### 9.4 Sicurezza

```
1. Snapshot prima di navigare in siti sospetti
2. Usa VM per testare software non fidato
3. Rete Host-Only per isolamento completo
4. Crittografa VM con dati sensibili:
   Impostazioni → Generale → Encryption
5. Non condividere VM con dati personali
```

### 9.5 Backup

```
1. Export VM come .ova regolarmente
2. Backup file .vdi su storage esterno
3. Documenta configurazioni speciali
4. Tieni lista di software installato
5. Usa versioning (VM-v1, VM-v2)
```

---

## Parte 10: Esercizi Pratici

### Esercizio 1: Setup Completo
```
1. Crea VM Ubuntu
2. Installa Guest Additions
3. Configura cartella condivisa
4. Crea 3 snapshot in momenti diversi
5. Testa ripristino snapshot
6. Esporta VM come .ova
```

### Esercizio 2: Networking
```
1. Crea 2 VM Ubuntu (clona la prima)
2. Configura NAT Network
3. Verifica ping tra le due VM
4. Configura SSH
5. Setup port forwarding per accedere da host
```

### Esercizio 3: Windows
```
1. Crea VM Windows
2. Installa Guest Additions
3. Configura risoluzione automatica
4. Testa clipboard e drag&drop
5. Installa software a scelta
6. Crea snapshot
```

### Esercizio 4: Multi-OS Environment
```
1. VM Ubuntu con web server (nginx)
2. VM Windows con browser
3. Configura rete bridged
4. Accedi al web server da Windows VM
5. Documenta IP e configurazione
```

### Esercizio 5: Disaster Recovery
```
1. Crea VM e configura completamente
2. Installa software vario
3. Crea contenuto (file, configurazioni)
4. Snapshot "GOOD"
5. Rompi qualcosa intenzionalmente
6. Ripristina snapshot
7. Verifica che tutto funziona
```

---

## Conclusioni

Hai imparato a:
- ✅ Installare e configurare VirtualBox
- ✅ Abilitare virtualizzazione hardware
- ✅ Creare VM Linux e Windows
- ✅ Installare Guest Additions
- ✅ Gestire snapshot e cloni
- ✅ Configurare networking
- ✅ Troubleshooting problemi comuni
- ✅ Applicare best practices

**Prossimi passi:**
1. Sperimenta con diverse distribuzioni Linux
2. Prova configurazioni di rete avanzate
3. Crea un lab multi-VM per simulare ambienti reali
4. Esplora scripting con VBoxManage
5. Prova altre soluzioni di virtualizzazione (VMware, Hyper-V, KVM)

---

## Risorse Aggiuntive

**Documentazione:**
- VirtualBox Manual: https://www.virtualbox.org/manual/
- VBoxManage CLI: https://www.virtualbox.org/manual/ch08.html

**Community:**
- VirtualBox Forums: https://forums.virtualbox.org/
- Reddit r/virtualbox

**Video Tutorial:**
- VirtualBox Official Channel
- YouTube: "VirtualBox Tutorial for Beginners"

**Alternative:**
- VMware Workstation Player (free per uso personale)
- QEMU/KVM (Linux)
- Hyper-V (Windows Pro/Enterprise)
- Parallels (macOS)

---

## Checklist Finale

Prima di completare l'esercitazione, verifica di aver:

- [ ] Installato VirtualBox e Extension Pack
- [ ] Abilitato virtualizzazione nel BIOS
- [ ] Creato almeno una VM Linux funzionante
- [ ] Creato almeno una VM Windows funzionante
- [ ] Installato Guest Additions su entrambe
- [ ] Testato clipboard condivisa
- [ ] Configurato almeno una cartella condivisa
- [ ] Creato almeno uno snapshot
- [ ] Clonato una VM
- [ ] Esportato una VM in formato .ova
- [ ] Testato diverse modalità di rete
- [ ] Risolto almeno un problema comune

**Congratulazioni! Hai completato l'esercitazione sulla virtualizzazione con VirtualBox!** 🎉