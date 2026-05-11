# Esercitazione 2: Virtualizzazione con VMware Workstation 16

## Obiettivi dell'Esercitazione

In questa esercitazione pratica imparerai a:
1. Installare e configurare VMware Workstation 16
2. Abilitare la virtualizzazione nel BIOS/UEFI
3. Creare e configurare macchine virtuali
4. Installare sistemi operativi guest (Windows e Linux)
5. Installare e configurare VMware Tools
6. Gestire le operazioni quotidiane delle VM (snapshot, cloni, backup)

**Durata stimata:** 3-4 ore

---

## 📋 Modalità di Consegna

Per questa esercitazione dovrai produrre un **documento Google** contenente:

1. **Copertina:** Nome, Cognome, Classe, Data
2. **Indice** numerato con tutte le sezioni
3. **Screenshot numerati** (📸) con annotazioni (frecce, cerchi, didascalie) — segui i marcatori nel testo
4. **Risposte** alle domande di riflessione (❓) con spiegazioni complete
5. **Conclusioni personali** (almeno 10 righe) sull'esperienza con VMware Workstation 16

**Requisiti screenshot:**
- Risoluzione minima: 1280×720 pixel
- Evidenzia gli elementi importanti con frecce o cerchi
- Aggiungi una breve didascalia descrittiva sotto ogni screenshot

**Consegna:**
- Esporta il documento in **PDF**
- Nome file: `VMware_Lab_[CognomeNome]_[Classe]_[Data].pdf`
- Carica su Google Drive e condividi il link (permesso: "chiunque con il link può visualizzare")
- Invia il link al docente entro la scadenza indicata

---

## Parte 1: Preparazione dell'Ambiente

### 1.1 Requisiti di Sistema

**Hardware minimo:**
- CPU: Processore con supporto virtualizzazione (Intel VT-x o AMD-V), 64-bit
- RAM: 8 GB (consigliato 16 GB)
- Storage: 50-100 GB di spazio libero su disco
- Sistema operativo host: Windows 10/11 (64-bit) o Linux (kernel 3.10+)

> ⚠️ VMware Workstation 16 **non è disponibile per macOS**. Per macOS usa VMware Fusion.

**Software necessario:**
- VMware Workstation 16 Pro (a pagamento) o VMware Workstation 16 Player (gratuito per uso personale)
- ISO dei sistemi operativi guest:
  - Ubuntu Desktop LTS (esempio: 22.04)
  - Windows 10/11 (evaluation o licenza)

### 1.2 Download VMware Workstation 16

1. Visita: https://www.vmware.com/products/workstation-pro.html
2. Scarica la versione per il tuo sistema operativo (Windows o Linux)
3. Per la versione Player gratuita: https://www.vmware.com/products/workstation-player.html

**Note sulle licenze:**
- **Workstation Pro:** a pagamento, funzionalità complete (snapshot avanzati, cloni, cifratura, teams)
- **Workstation Player:** gratuito per uso personale e accademico, funzionalità base
- **Trial Pro:** 30 giorni gratuiti con funzionalità complete

### 1.3 Installazione VMware Workstation 16

#### Su Windows:
```cmd
1. Esegui VMware-workstation-full-16.x.x-xxxxxxx.exe
2. Accetta l'EULA
3. Scegli "Typical" per installazione standard
   (oppure "Custom" per personalizzare percorsi e funzionalità)
4. Aggiornamenti automatici: scegli la preferenza
5. User Experience Improvement Program: opzionale
6. Scorciatoie: scegli dove creare i collegamenti
7. Click "Install"
8. Al termine inserisci il numero di licenza (o salta per trial)
9. Riavvia se richiesto
```

#### Su Linux (Ubuntu/Debian):
```bash
# Scarica il bundle di installazione
# Esempio: VMware-Workstation-Full-16.x.x-xxxxxxx.x86_64.bundle

# Rendi il file eseguibile
chmod +x VMware-Workstation-Full-16.x.x-xxxxxxx.x86_64.bundle

# Installa le dipendenze necessarie
sudo apt update
sudo apt install -y build-essential linux-headers-$(uname -r) \
    gcc make gcc-12 g++-12

# Esegui l'installer
sudo ./VMware-Workstation-Full-16.x.x-xxxxxxx.x86_64.bundle

# Segui il wizard testuale / grafico
# Riavvia se richiesto
```

#### Verifica installazione:
```cmd
# Windows: avvia VMware Workstation dal menu Start o dal desktop
# Linux:
vmware --version
# Output atteso: VMware Workstation 16.x.x build-xxxxxxx
```

📸 **SCREENSHOT 1:** Schermata principale di VMware Workstation 16 dopo l'installazione (homepage con le opzioni "Create a New Virtual Machine", "Open a Virtual Machine", ecc.).

📸 **SCREENSHOT 2:** Finestra di installazione di VMware Workstation 16 (wizard di setup con barra di avanzamento oppure schermata finale di completamento con pulsante "Finish").

❓ **Domande di Riflessione 1**

**R1.1** Qual è la differenza tra un hypervisor di tipo 1 (bare-metal) e uno di tipo 2 (hosted)? In quale categoria rientra VMware Workstation 16?

**R1.2** In quali scenari è giustificato acquistare VMware Workstation **Pro** rispetto al Player gratuito? Elenca almeno tre funzionalità esclusive della versione Pro.

**R1.3** Perché VMware Workstation non è disponibile per macOS? Quale prodotto VMware copre questa esigenza su macOS?

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
# Verifica supporto Intel VT-x o AMD-V
egrep -c '(vmx|svm)' /proc/cpuinfo
# Se il risultato è > 0, la virtualizzazione è supportata

# Verifica se è abilitata
lscpu | grep Virtualization

# Verifica moduli kernel
lsmod | grep kvm
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

### 2.3 VMware e Hyper-V su Windows

A differenza di VirtualBox, **VMware Workstation 16 è compatibile con Hyper-V** su Windows 10/11 (versione 2004 o successive) grazie al supporto per Windows Hypervisor Platform (WHP). Tuttavia, per prestazioni ottimali è consigliato disabilitarlo se non necessario:

```powershell
# Apri PowerShell come Amministratore

# Disabilita Hyper-V (per massima performance VMware)
bcdedit /set hypervisorlaunchtype off

# Riavvia
shutdown /r /t 0

# Per riabilitare (se necessario, es. per WSL2)
bcdedit /set hypervisorlaunchtype auto
```

> 💡 **Nota:** Se usi WSL2 o Windows Sandbox, potresti aver bisogno di Hyper-V. In quel caso lascialo attivo; VMware 16 funzionerà comunque, ma con performance leggermente ridotte.

📸 **SCREENSHOT 3:** Schermata BIOS/UEFI con la voce di virtualizzazione (Intel VT-x, AMD-V o SVM) impostata su "Enabled". In alternativa, screenshot del Task Manager (tab Prestazioni → CPU) che mostra "Virtualizzazione: Abilitata".

❓ **Domande di Riflessione 2**

**R2.1** Cosa succede se si prova a creare una VM a 64-bit su un host con virtualizzazione hardware disabilitata nel BIOS? Quale messaggio di errore mostrerà VMware?

**R2.2** Qual è la differenza tecnica tra Intel VT-x e AMD-V? Sono compatibili tra loro o riguardano architetture CPU diverse?

**R2.3** Perché VMware Workstation 16 può coesistere con Hyper-V (tramite Windows Hypervisor Platform), mentre le versioni precedenti non potevano? Cosa cambia a livello architetturale?

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
1. Apri VMware Workstation 16
2. Click su "Create a New Virtual Machine"
   (o File → New Virtual Machine)

STEP 1: Wizard Type
- ⚪ Typical (recommended)   ← scegli questa
- ⚪ Custom (advanced)
- Click "Next"

STEP 2: Guest OS Installation
- ⚪ Install from disc or image
  → Browse: seleziona ubuntu-22.04-desktop-amd64.iso
  VMware rileva automaticamente "Ubuntu 64-bit"
  e propone Easy Install (installazione automatica!)
- Inserisci:
  - Full name: Student
  - User name: student
  - Password: [scegli una password]
- Click "Next"

STEP 3: Nome e Posizione
- Virtual machine name: Ubuntu-22.04-Lab
- Location: [scegli la cartella, es: C:\VMs\Ubuntu-22.04-Lab]
- Click "Next"

STEP 4: Dimensione Disco
- Maximum disk size: 25 GB (minimo per Ubuntu Desktop)
- ⚪ Store virtual disk as a single file
- ⚪ Split virtual disk into multiple files ← consigliato per portabilità
- Click "Next"

STEP 5: Riepilogo
- Controlla le impostazioni
- Click "Customize Hardware..." per modifiche avanzate
- Click "Finish"
```

### 3.3 Configurazione Avanzata della VM (Customize Hardware)

Accedi tramite: VM → Settings (o Ctrl+D)

```
🖥️ MEMORY:
- Memory: 2048 MB (minimo) - 4096 MB (consigliato)
  💡 VMware mostra in verde la quantità consigliata

⚙️ PROCESSORS:
- Number of processors: 1
- Number of cores per processor: 2
  (totale 2 vCPU; non superare il numero di core fisici)
- ✅ Virtualize Intel VT-x/EPT or AMD-V/RVI
- ✅ Virtualize CPU performance counters (opzionale)

💽 HARD DISK (SCSI):
- Tipo: SCSI (predefinito, prestazioni migliori)
  Alternativa: NVMe (per sistemi moderni)
- Già configurato dal wizard

📀 CD/DVD (SATA):
- Connection:
  ⚪ Use ISO image file → seleziona il .iso Ubuntu
  ⚪ Use physical drive

🌐 NETWORK ADAPTER:
- ⚪ NAT: Used to share the host's IP address ← default
- ⚪ Bridged: Connected directly to the physical network
- ⚪ Host-only: A private network shared with the host
- ✅ Connect at power on

🖼️ DISPLAY:
- ✅ Accelerate 3D graphics
- Monitors: Use host settings
```

### 3.4 Easy Install (Installazione Automatizzata)

VMware Workstation 16 offre l'**Easy Install**: quando rileva l'ISO di un OS supportato (Ubuntu, Windows, ecc.), automatizza l'installazione senza intervento manuale.

```
1. Avvia la VM (Play virtual machine)
2. Easy Install eseguirà automaticamente:
   - Boot da ISO
   - Partizionamento disco
   - Installazione Ubuntu
   - Creazione utente (con le credenziali inserite nel wizard)
   - Installazione automatica di VMware Tools
3. Attendi 10-20 minuti
4. La VM si riavvierà e arriverai al login
```

> ⚠️ Se preferisci un'installazione manuale (per imparare il processo):
> - Nel wizard, scegli "I will install the operating system later"
> - Associa l'ISO in un secondo momento da VM → Settings → CD/DVD
> - Esegui l'installazione manuale come faresti su un PC fisico

### 3.5 Installazione Manuale Ubuntu (Alternativa)

Se non hai usato Easy Install:

```
BOOT:
- Avvia la VM
- Aspetta il caricamento del LiveCD
- Click "Installa Ubuntu"

INSTALLAZIONE:
1. Lingua tastiera: Italiana → Continua
2. Aggiornamenti:
   - ✅ Installazione normale
   - ✅ Scarica aggiornamenti durante installazione
   - ✅ Installa software di terze parti
   → Continua
3. Tipo installazione:
   - ⚪ Cancella disco e installa Ubuntu
   → Installa ora → Continua
4. Fuso orario: seleziona la tua città → Continua
5. Informazioni utente:
   - Nome: Student
   - Nome computer: ubuntu-vm
   - Nome utente: student
   - Password: [scegli]
   → Continua
6. Attendi 10-20 minuti
7. Click "Riavvia ora"
8. Premi INVIO quando richiesto
9. Login con le credenziali impostate
```

### 3.6 Prime Verifiche

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

📸 **SCREENSHOT 4:** Wizard di creazione della nuova VM — step Easy Install con nome utente e password inseriti e il rilevamento automatico di "Ubuntu 64-bit" da parte di VMware.

📸 **SCREENSHOT 5:** VM Ubuntu avviata con successo — desktop Ubuntu visibile nella finestra VMware Workstation (primo avvio dopo Easy Install o installazione manuale).

❓ **Domande di Riflessione 3**

**R3.1** Quali vantaggi offre la funzione Easy Install di VMware rispetto a un'installazione manuale? In quale scenario preferiresti eseguire un'installazione manuale?

**R3.2** Qual è la differenza tra memorizzare il disco virtuale in un singolo file e dividerlo in più file da 2 GB? Quando è preferibile l'uno rispetto all'altro?

**R3.3** Perché si raccomanda di non assegnare alla VM più vCPU della metà dei core fisici disponibili sull'host? Quali problemi di performance potrebbe causare un overcommit?

---

## Parte 4: Installazione VMware Tools

VMware Tools è l'equivalente delle Guest Additions di VirtualBox. Migliora drasticamente l'esperienza:
- Schermo a risoluzione completa e ridimensionamento automatico
- Clipboard condivisa (copia/incolla tra host e guest)
- Drag & Drop di file
- Cartelle condivise
- Migliore performance grafica e di I/O
- Sincronizzazione orologio

### 4.1 Installazione su Ubuntu (Metodo consigliato: open-vm-tools)

```bash
# Metodo 1: open-vm-tools (consigliato per Linux)
# È la versione open source, aggiornata automaticamente con apt

sudo apt update
sudo apt install -y open-vm-tools open-vm-tools-desktop

# Riavvia la VM
sudo reboot

# Verifica installazione
vmware-toolsd --version
# Output atteso: VMware Tools daemon, version 11.x.x ...
```

### 4.2 Installazione VMware Tools da Menù (Metodo alternativo)

Se preferisci installare i VMware Tools ufficiali dal pacchetto VMware:

```
1. Con la VM accesa, vai al menu:
   VM → Install VMware Tools
   (o tasto destro sulla VM nell'elenco → Install VMware Tools)

2. Apparirà un CD virtuale nel guest Ubuntu

3. Nel terminale Ubuntu:
```

```bash
# Monta il CD (se non montato automaticamente)
sudo mkdir -p /media/vmtools
sudo mount /dev/cdrom /media/vmtools

# Copia il tarball nella home
cp /media/vmtools/VMwareTools-*.tar.gz ~/
cd ~

# Estrai
tar -xzf VMwareTools-*.tar.gz

# Installa
cd vmware-tools-distrib
sudo ./vmware-install.pl

# Accetta tutti i default premendo INVIO
# Al termine:
sudo reboot
```

**Verifica installazione:**
```bash
# Controlla moduli caricati
lsmod | grep vmw

# Output atteso (tra gli altri):
# vmw_balloon
# vmw_vmci
# vmwgfx

# Verifica servizio
systemctl status vmware-tools
# oppure con open-vm-tools:
systemctl status open-vm-tools
```

### 4.3 Configurazione Funzionalità

**Clipboard condivisa:**
```
VM → Settings → Options → Guest Isolation
✅ Enable copy and paste
✅ Enable drag and drop
```

Oppure dalla barra in alto durante l'esecuzione:
```
VM → Unity (o Shared Folders per le cartelle)
```

**Schermo intero:**
```
Premi: Ctrl + Alt + Enter
O: View → Full Screen
```

**Risoluzione automatica:**
```
View → Autofit Guest
→ La finestra si ridimensionerà e il guest si adatterà
```

📸 **SCREENSHOT 6:** Output del comando `vmware-toolsd --version` nel terminale di Ubuntu (mostra la versione installata di VMware Tools o open-vm-tools).

📸 **SCREENSHOT 7:** Dimostrazione della clipboard condivisa funzionante — testo copiato dall'host e incollato nel guest Ubuntu (o viceversa).

📸 **SCREENSHOT 8:** Finestra VM → Settings → Options → Shared Folders con almeno una cartella condivisa configurata e abilitata.

📸 **SCREENSHOT 9:** Terminale Ubuntu con output del comando `ls /mnt/hgfs/` che mostra la cartella condivisa montata e accessibile.

❓ **Domande di Riflessione 4**

**R4.1** Qual è la differenza tra `open-vm-tools` (installato via apt) e i VMware Tools ufficiali (installati da CD virtuale VMware)? Quali sono i vantaggi di usare `open-vm-tools` su Ubuntu?

**R4.2** Elenca almeno cinque funzionalità della VM che non sarebbero disponibili senza VMware Tools installati.

**R4.3** Come funziona tecnicamente la clipboard condivisa tra host e guest? Il meccanismo potrebbe presentare rischi di sicurezza? In quale scenario è consigliabile disabilitarla?

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

### 5.2 Creazione VM Windows con Easy Install

```
1. File → New Virtual Machine → Typical
2. Installer disc image file (iso): seleziona ISO Windows
   VMware rileva automaticamente il sistema
3. Easy Install per Windows:
   - Windows product key: [inserisci o lascia vuoto per trial]
   - Windows version: Windows 10 Pro / Windows 11 Pro
   - Personalize Windows:
     - Full name: Student
     - Password: [scegli]
4. Nome VM: Windows-11-Lab
5. Disco: 60 GB, split multiple files
6. Customize Hardware:
   - Memory: 4096 MB (requisito minimo Win11)
   - Processors: 2 core
7. Finish → la VM si avvierà e installerà automaticamente
```

### 5.3 Impostazioni Speciali per Windows 11

Windows 11 richiede TPM 2.0 e Secure Boot. VMware Workstation 16 li supporta nativamente:

```
VM → Settings → Options → Access Control
→ Encrypt this virtual machine (necessario per vTPM)

VM → Settings → Hardware → Trusted Platform Module
→ Add TPM (aggiungi il dispositivo)

VM → Settings → Options → Advanced
→ Firmware type: UEFI
✅ Enable secure boot
```

> 💡 Se non riesci ad aggiungere TPM, assicurati di aver prima cifrato la VM (Access Control → Encrypt).

### 5.4 Installazione VMware Tools su Windows

Con Easy Install, VMware Tools viene installato **automaticamente**. Se necessario, installali manualmente:

```
1. Con VM Windows accesa
2. VM → Install VMware Tools
3. Si apre l'autorun del CD virtuale in Windows
4. Se non parte automaticamente: apri Esplora File → CD Drive
5. Esegui setup.exe o setup64.exe
6. Wizard:
   - Next → Typical → Next → Install
   - Accetta UAC
   - Attendi installazione
7. Finish → Riavvia quando richiesto
```

**Verifica:**
```
- Pannello di controllo → Programmi → VMware Tools
- Ridimensiona finestra → risoluzione si adatta automaticamente
- Testa copia/incolla tra host e guest
- Testa drag & drop di file
```

📸 **SCREENSHOT 10:** Desktop di Windows (10 o 11) in esecuzione nella VM VMware — schermata iniziale o desktop con taskbar visibile.

📸 **SCREENSHOT 11:** Pannello di controllo Windows → Programmi con VMware Tools nell'elenco (oppure: Gestione Dispositivi con i dispositivi VMware visibili).

📸 **SCREENSHOT 12:** Unity Mode attivo — finestre di applicazioni Windows visibili direttamente nel desktop dell'host, con il taskbar VMware nell'angolo.

❓ **Domande di Riflessione 5**

**R5.1** Perché Windows 11 richiede TPM 2.0 e Secure Boot? Quali minacce di sicurezza mitigano questi requisiti hardware?

**R5.2** Come VMware Workstation 16 emula un chip TPM 2.0 virtuale (vTPM) senza che l'host abbia necessariamente un chip TPM fisico? Perché è necessario cifrare la VM prima di aggiungere il vTPM?

**R5.3** Unity Mode è una funzionalità esclusiva di VMware. Quale funzionalità analoga offre VirtualBox (Seamless Mode)? Descrivi le differenze pratiche.

---

## Parte 6: Gestione Quotidiana delle VM

### 6.1 Snapshot

Gli snapshot salvano lo stato completo della VM (disco + RAM se accesa).

**Creare uno snapshot (Workstation Pro):**
```
1. VM accesa o spenta
2. VM → Snapshot → Take Snapshot
   (o: icona macchina fotografica nella toolbar)
3. Name: "Ubuntu Fresh Install"
4. Description: "Subito dopo installazione e VMware Tools"
5. ✅ Snapshot the virtual machine's memory
   (disponibile solo se VM accesa; salva anche lo stato della RAM)
6. Click "Take Snapshot"
```

**Snapshot Manager:**
```
VM → Snapshot → Snapshot Manager
(o: Ctrl + M)
→ Vista grafica ad albero di tutti gli snapshot
→ Possibilità di navigare, ripristinare, eliminare
```

**Ripristinare uno snapshot:**
```
1. VM → Snapshot → Snapshot Manager
2. Seleziona lo snapshot desiderato
3. Click "Go To"
4. Conferma
→ La VM tornerà esattamente allo stato dello snapshot
```

**Eliminare uno snapshot:**
```
1. Snapshot Manager
2. Seleziona snapshot
3. Click "Delete"
4. ⚠️ Consolidation: i dati dello snapshot vengono uniti al disco base
   (può richiedere tempo in base alla dimensione)
```

> **Workstation Player:** gli snapshot non sono disponibili nella versione gratuita. Usa la versione Pro o il trial.

### 6.2 Clonazione VM (Pro)

Crea una copia indipendente o collegata della VM.

```
1. Ferma la VM
2. VM → Manage → Clone
   (o: click destro → Manage → Clone)
3. Wizard:
   - Clone from: Current state / Existing snapshot
   - Click "Next"
   
4. Clone type:
   - ⚪ Create a linked clone
     Risparmia spazio, usa il disco della VM originale come base
     ⚠️ La VM originale non può essere spostata/eliminata
   - ⚪ Create a full clone
     Copia completa e indipendente; richiede spazio aggiuntivo
   - Click "Next"
   
5. Nome: Ubuntu-22.04-Clone
6. Posizione: [scegli cartella]
7. Click "Finish"
8. Attendi completamento
```

### 6.3 Export/Import (OVF/OVA)

Per spostare VM tra hypervisor diversi (VMware, VirtualBox, ecc.) o fare backup portabili.

**Esportare VM:**
```
1. Ferma la VM
2. File → Export to OVF
3. Scegli nome e posizione
4. Formato:
   - OVF: cartella con più file (.ovf, .vmdk, .mf)
   - OVA: singolo archivio compresso (più comodo per spostamenti)
5. Click "Save"
6. Attendi (può richiedere diversi minuti)
```

**Importare VM:**
```
1. File → Open
2. Seleziona file .ovf o .ova
3. Nome e percorso della nuova VM
4. Click "Import"
5. Attendi il completamento
```

> 💡 VMware può importare anche file .ova/.ovf creati con VirtualBox, e viceversa (con possibili avvisi di compatibilità).

### 6.4 Cartelle Condivise

Condividi cartelle tra host e guest senza bisogno di rete.

**Configurazione:**
```
1. VM → Settings → Options → Shared Folders
2. Folder sharing: ⚪ Always enabled (o Enabled until next power off)
3. Click "Add"
4. Wizard:
   - Host path: scegli cartella (es: C:\Shared o /home/user/shared)
   - Name: shared
   - ✅ Enable this share
   - ✅ Read-only (se vuoi sola lettura)
5. Finish → OK
```

**Accesso da Ubuntu:**
```bash
# Con open-vm-tools, le cartelle condivise sono montate in:
ls /mnt/hgfs/

# Se non sono visibili:
sudo vmhgfs-fuse .host:/ /mnt/hgfs/ -o allow_other -o uid=$(id -u)

# Mount permanente (aggiungere a /etc/fstab):
echo ".host:/ /mnt/hgfs fuse.vmhgfs-fuse allow_other,uid=1000 0 0" | sudo tee -a /etc/fstab

# Accedi alla cartella condivisa
cd /mnt/hgfs/shared
ls
```

**Accesso da Windows:**
```
Esplora File → \\vmware-host\Shared Folders\shared
Oppure:
Z: → mappato automaticamente su \\vmware-host\Shared Folders
```

### 6.5 Unity Mode (solo Windows guest)

Una funzionalità esclusiva di VMware: le finestre del guest Windows appaiono **direttamente sul desktop dell'host**, come se fossero applicazioni native.

```
1. Con VM Windows accesa e VMware Tools installati
2. View → Unity
   (o: Ctrl + Shift + U)
3. Le applicazioni Windows appaiono nel taskbar dell'host
4. Per uscire da Unity: tasto destro sull'icona VMware nel system tray
   → Exit Unity
```

### 6.6 Stati della VM

**Avvio:**
```
- Power On (Play):        avvia la VM normalmente
- Power On to Firmware:   accede direttamente al BIOS/UEFI
- Resume:                 riprende da uno stato sospeso
```

**Salvataggio stato:**
```
- Suspend (Ctrl+Z):       salva stato RAM su disco e mette in pausa
                          Riprendibile con Play; come ibernazione
- Shut Down Guest:        invia segnale ACPI al SO guest (spegnimento pulito)
- Power Off:              spegnimento forzato (come staccare la spina)
                          ⚠️ Solo in caso di blocco
```

### 6.7 Gestione Risorse

**Modificare RAM/CPU:**
```
1. Ferma la VM
2. VM → Settings → Hardware
3. Memory: modifica valore
4. Processors: modifica numero core
5. OK
```

**Espandere il disco virtuale:**
```
1. Ferma la VM
2. VM → Settings → Hardware → Hard Disk
3. Click "Expand..."
4. Inserisci la nuova dimensione (es: 50 GB)
5. Click "Expand"
6. ⚠️ Ricorda: espandere il disco virtuale NON espande la partizione del SO!
```

**Espandere la partizione nel guest (Ubuntu):**
```bash
# Metodo 1: con GParted
sudo apt install gparted
sudo gparted
# GUI: ridimensiona la partizione per usare tutto lo spazio

# Metodo 2: con growpart e resize2fs (senza riavvio)
sudo apt install cloud-guest-utils
sudo growpart /dev/sda 1     # espandi la partizione 1 del disco sda
sudo resize2fs /dev/sda1     # ridimensiona il filesystem
df -h                         # verifica il nuovo spazio
```

📸 **SCREENSHOT 13:** Snapshot Manager (VM → Snapshot → Snapshot Manager) con almeno 2-3 snapshot visibili nell'albero gerarchico.

📸 **SCREENSHOT 14:** Procedura di clone completata — la VM clonata visibile nell'elenco delle VM in VMware Workstation, con il tipo di clone indicato.

📸 **SCREENSHOT 15:** Cartella o file manager dell'host con il file `.ova` (o la cartella `.ovf`) dell'esportazione completata, con nome file e dimensione visibili.

❓ **Domande di Riflessione 6**

**R6.1** Qual è la differenza tra un **linked clone** e un **full clone**? In quale scenario useresti ciascuno? Quali sono i rischi pratici del linked clone?

**R6.2** Perché è importante consolidare o eliminare snapshot vecchi regolarmente? Come impattano negativamente sulle performance delle operazioni su disco?

**R6.3** Il formato OVF/OVA garantisce sempre la compatibilità tra hypervisor diversi (es. VMware → VirtualBox)? Quali problemi concreti potrebbero emergere durante l'importazione?

---

## Parte 7: Networking

### 7.1 Modalità di Rete in VMware

VMware offre tre modalità principali, configurabili in **VM → Settings → Network Adapter**:

**NAT (Network Address Translation):**
```
- La VM condivide l'IP dell'host tramite NAT
- VM accede a Internet
- VM non è raggiungibile dall'esterno
- Più VM NAT non comunicano tra loro per default
- Subnet di default: 192.168.x.0/24 (x dipende da VMnet8)
Use case: navigazione web, download aggiornamenti
```

**Bridged:**
```
- La VM appare come dispositivo fisico sulla rete
- Ottiene IP dal DHCP router/server di rete
- Pienamente visibile e raggiungibile da altri dispositivi
- Configurabile in: Edit → Virtual Network Editor → VMnet0 (bridged)
Use case: server, test rete reale, ambienti che richiedono IP dedicato
```

**Host-Only:**
```
- La VM comunica SOLO con l'host
- Rete privata isolata (subnet VMnet1)
- Nessun accesso a Internet per default
- Possibile aggiungere NAT manualmente
Use case: test sicuri, ambienti isolati, lab di sicurezza
```

### 7.2 Virtual Network Editor (solo Pro)

VMware Workstation Pro include un editor di reti virtuali avanzato:

```
Edit → Virtual Network Editor
(oppure: avvia "Virtual Network Editor" come amministratore)

Reti predefinite:
- VMnet0: Bridged (connessa all'adattatore fisico)
- VMnet1: Host-only
- VMnet8: NAT

Creare una nuova rete:
1. Click "Add Network"
2. Seleziona VMnet (es: VMnet2)
3. Tipo: Host-only o NAT
4. Subnet: es. 192.168.100.0 / 255.255.255.0
5. ✅ Use local DHCP service
6. Apply → OK

Assegnare la rete a una VM:
VM → Settings → Network Adapter → Custom: VMnet2
```

### 7.3 Port Forwarding con NAT

Per accedere a servizi nella VM dall'host:

```
1. Edit → Virtual Network Editor
2. Seleziona VMnet8 (NAT)
3. Click "NAT Settings"
4. Sezione "Port Forwarding" → Add
5. Esempio per SSH:
   - Host port: 2222
   - Type: TCP
   - Virtual machine IP address: [IP della VM, es: 192.168.171.128]
   - Virtual machine port: 22
   - Description: SSH Ubuntu
6. OK → Apply

Ora da host puoi accedere:
ssh -p 2222 student@localhost
```

**Trovare l'IP della VM:**
```bash
# Nel guest Ubuntu:
ip addr show ens33
# o
hostname -I
```

📸 **SCREENSHOT 16:** Virtual Network Editor (Edit → Virtual Network Editor) con le reti virtuali configurate (VMnet0, VMnet1, VMnet8) e i relativi tipi e subnet.

📸 **SCREENSHOT 17:** Finestra NAT Settings → Port Forwarding con almeno una regola configurata (es. porta 2222 → porta 22 della VM).

📸 **SCREENSHOT 18:** Terminale dell'host con connessione SSH riuscita alla VM tramite port forwarding (comando `ssh -p 2222 student@localhost` con prompt del guest Ubuntu visibile).

❓ **Domande di Riflessione 7**

**R7.1** Hai una VM con un server web che deve essere raggiungibile da altri computer nella rete locale. Quale modalità di rete useresti e perché? Descrivi pro e contro.

**R7.2** Qual è la differenza pratica tra NAT e Bridged in termini di indirizzo IP assegnato alla VM? Con quale modalità la VM risulta "visibile" sulla rete come se fosse un dispositivo fisico?

**R7.3** Descrivi uno scenario reale di laboratorio di sicurezza informatica in cui la rete Host-Only è preferibile alle altre modalità. Perché l'isolamento è fondamentale in quel contesto?

---

## Parte 8: Troubleshooting Comuni

### Problema: VM molto lenta

**Soluzioni:**
```
1. Aumenta RAM (almeno 2 GB per Linux, 4 GB per Windows)
2. Aumenta CPU (almeno 2 vCPU)
3. Abilita VT-x/AMD-V nel BIOS
4. Installa VMware Tools
5. Abilita accelerazione 3D: VM → Settings → Display → Accelerate 3D
6. Usa disco SCSI o NVMe invece di IDE
7. Consolida snapshot vecchi (rallentano le operazioni su disco)
```

### Problema: Schermo piccolo, non ridimensionabile

**Soluzione:**
```
1. Installa/reinstalla VMware Tools
2. Riavvia la VM
3. View → Autofit Guest
4. Su Ubuntu, verifica il servizio:
   systemctl status open-vm-tools
```

### Problema: Clipboard non funziona

**Soluzione:**
```
1. Reinstalla VMware Tools
2. VM → Settings → Options → Guest Isolation
   ✅ Enable copy and paste
   ✅ Enable drag and drop
3. Su Linux, verifica che VGAuthService sia attivo:
   systemctl status vmware-vmblock-fuse
   systemctl status open-vm-tools
```

### Problema: Cartelle condivise non visibili (/mnt/hgfs vuoto)

**Soluzione:**
```bash
# Verifica che le cartelle condivise siano abilitate:
# VM → Settings → Options → Shared Folders → Always enabled

# Rimonta le cartelle condivise:
sudo vmhgfs-fuse .host:/ /mnt/hgfs/ -o allow_other -o uid=$(id -u)

# Verifica:
ls /mnt/hgfs/
```

### Problema: VT-x non disponibile o errore di virtualizzazione

**Soluzione:**
```
1. Riavvia e accedi al BIOS
2. Abilita Intel VT-x o AMD-V
3. Su Windows, verifica Hyper-V:
   bcdedit /set hypervisorlaunchtype off
   (riavvia)
4. Assicurati che nessun altro hypervisor stia usando VT-x
   (altri software di virtualizzazione aperti contemporaneamente)
```

### Problema: La VM non si avvia (errore "VMX not supported")

**Soluzione:**
```
1. Verifica che la versione di VMware Workstation supporti il hardware level
   della VM: VM → Settings → Options → Advanced → Hardware compatibility
2. Riduci l'hardware compatibility level se necessario
3. Verifica che il file .vmx non sia corrotto:
   Apri il file .vmx con un editor di testo e controlla la sintassi
```

### Problema: Errore TPM/Secure Boot con Windows 11

**Soluzione:**
```
1. VM → Settings → Options → Access Control → Encrypt
   (imposta una password)
2. VM → Settings → Hardware → Add → Trusted Platform Module
3. VM → Settings → Options → Advanced → UEFI firmware
   ✅ Enable Secure Boot
4. Riavvia la VM
```

---

## Parte 9: Best Practices

### 9.1 Organizzazione

```
Struttura cartelle consigliata:
C:\VirtualMachines\
├── Ubuntu-22.04-Lab\
│   ├── Ubuntu-22.04-Lab.vmx
│   ├── Ubuntu-22.04-Lab.vmdk
│   └── Snapshots\ (gestiti automaticamente da VMware)
├── Windows-11-Lab\
│   ├── Windows-11-Lab.vmx
│   └── Windows-11-Lab.vmdk
└── ISOs\
    ├── ubuntu-22.04-desktop-amd64.iso
    └── Win11_Eval.iso
```

### 9.2 Snapshot Strategy

```
1. Snapshot "Fresh Install" - dopo installazione OS e VMware Tools
2. Snapshot "Post Configuration" - dopo configurazione base
3. Snapshot "Before Update" - prima di aggiornamenti maggiori
4. Snapshot "Before Experiment" - prima di test rischiosi

⚠️ Troppi snapshot rallentano le operazioni disco
   Consolida o elimina quelli non più necessari
💡 Gli snapshot di VMware sono in formato .vmem (memoria) + .vmsn (stato)
```

### 9.3 Performance

```
✅ DO:
- Usa allocazione dinamica per risparmiare spazio (thin provisioning)
- Installa sempre VMware Tools
- Assegna RAM e CPU proporzionate alle esigenze
- Usa SSD per i file delle VM
- Usa SCSI o NVMe come controller disco (più veloce di IDE)
- Abilita accelerazione 3D per GUI pesanti
- Consolida snapshot periodicamente

❌ DON'T:
- Non overcommit la RAM (lascia almeno 2-4 GB per l'host)
- Non eseguire troppe VM contemporaneamente
- Non usare snapshot come unico sistema di backup
- Non dimenticare di aggiornare VMware Tools dopo aggiornamenti del SO
```

### 9.4 Sicurezza

```
1. Snapshot prima di testare software non fidato
2. Usa VM isolate (Host-Only) per test di sicurezza
3. Crittografa VM con dati sensibili:
   VM → Settings → Options → Access Control → Encrypt
4. Non condividere file .vmx/.vmdk contenenti dati sensibili
5. Usa password per il Snapshot Manager (Pro)
6. Mantieni VMware Workstation aggiornato per patch di sicurezza
```

### 9.5 Backup

```
1. Esporta VM come .ova regolarmente (File → Export to OVF)
2. Copia i file .vmdk su storage esterno
3. Documenta le configurazioni speciali nel file README nella cartella VM
4. Tieni traccia del software installato (snapshots commentati)
5. Usa versioning: Ubuntu-Lab-v1, Ubuntu-Lab-v2
```

---

## Parte 10: Confronto VMware Workstation vs VirtualBox

| Funzionalità | VMware Workstation 16 Pro | VMware Player 16 | VirtualBox 7 |
|---|---|---|---|
| Costo | A pagamento | Gratuito (uso personale) | Gratuito |
| Snapshot | ✅ Avanzati (albero) | ❌ | ✅ |
| Cloni | ✅ (collegati e completi) | ❌ | ✅ |
| Unity/Seamless | ✅ (Windows guest) | ✅ | ✅ |
| Virtual Network Editor | ✅ | ❌ | ✅ (limitato) |
| 3D Graphics | ✅ DirectX 11, OpenGL 4.1 | ✅ | ✅ limitato |
| Cifratura VM | ✅ | ❌ | ✅ (Pro) |
| macOS host | ❌ (usa Fusion) | ❌ | ✅ |
| Easy Install | ✅ | ✅ | ❌ |
| Performance | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## Parte 11: Esercizi Pratici

### Esercizio 1: Setup Completo
```
1. Crea VM Ubuntu usando Easy Install
2. Verifica l'installazione automatica di VMware Tools
3. Configura cartella condivisa con l'host
4. Crea 3 snapshot in momenti diversi
5. Testa ripristino snapshot
6. Esporta VM come .ova
```

### Esercizio 2: Networking
```
1. Crea 2 VM Ubuntu (clona la prima con clone completo)
2. Configura entrambe con Host-Only networking (stessa VMnet)
3. Assegna IP statici a entrambe le VM
4. Verifica ping tra le due VM
5. Installa SSH su una VM
6. Connettiti da host tramite port forwarding
```

### Esercizio 3: Windows con VMware
```
1. Crea VM Windows 11 con Easy Install
2. Verifica installazione automatica VMware Tools
3. Configura risoluzione automatica
4. Testa clipboard e drag&drop
5. Prova Unity Mode
6. Crea snapshot
```

### Esercizio 4: Multi-OS Environment
```
1. VM Ubuntu con server web (nginx o apache2)
   sudo apt install nginx
   sudo systemctl start nginx
2. VM Windows con browser
3. Configura entrambe in modalità Bridged
4. Accedi al server web Ubuntu dal browser in Windows
5. Documenta gli IP e la configurazione
```

### Esercizio 5: Snapshot e Disaster Recovery
```
1. Crea VM e configurala completamente
2. Crea snapshot "Configurazione Base"
3. Installa software aggiuntivo
4. Crea snapshot "Con Software"
5. "Rompi" qualcosa intenzionalmente (es: elimina file di sistema)
6. Ripristina snapshot "Configurazione Base"
7. Verifica che la VM funzioni correttamente
8. Dal Snapshot Manager, esplora l'albero degli snapshot
```

### Esercizio 6: Confronto VMware vs VirtualBox
```
1. Crea la stessa VM (Ubuntu) in VMware e in VirtualBox
2. Misura il tempo di avvio (boot time)
3. Esegui un benchmark semplice (es: sysbench)
4. Confronta la velocità di trasferimento delle cartelle condivise
5. Documenta le differenze di interfaccia e funzionalità
6. Scrivi un breve report comparativo
```

---

## Conclusioni

Hai imparato a:
- ✅ Installare e configurare VMware Workstation 16
- ✅ Abilitare la virtualizzazione hardware
- ✅ Creare VM Linux e Windows con Easy Install
- ✅ Installare VMware Tools (open-vm-tools e pacchetto ufficiale)
- ✅ Gestire snapshot, cloni e backup
- ✅ Configurare reti virtuali (NAT, Bridged, Host-Only)
- ✅ Risolvere problemi comuni
- ✅ Applicare best practices

**Prossimi passi:**
1. Esplora il Virtual Network Editor per configurazioni di rete avanzate
2. Sperimenta con distribuzioni Linux server (senza GUI)
3. Crea un lab multi-VM per simulare ambienti di produzione
4. Prova la cifratura delle VM per proteggere dati sensibili
5. Confronta VMware con le alternative (VirtualBox, Hyper-V, KVM)

---

## Risorse Aggiuntive

**Documentazione ufficiale:**
- VMware Workstation Pro Docs: https://docs.vmware.com/en/VMware-Workstation-Pro/
- VMware Tools Guide: https://docs.vmware.com/en/VMware-Tools/
- Virtual Network Editor: https://docs.vmware.com/en/VMware-Workstation-Pro/16.0/com.vmware.ws.using.doc/GUID-D9A4A32C-91C0-4E5A-B44A-87BC42163C38.html

**Community:**
- VMware Community Forums: https://communities.vmware.com/
- Reddit r/vmware

**Differenze tra versioni:**
- Feature Comparison: https://www.vmware.com/products/workstation-pro/faqs.html

**Alternative:**
- VirtualBox (gratuito, cross-platform): https://www.virtualbox.org/
- VMware Fusion (macOS): https://www.vmware.com/products/fusion.html
- Hyper-V (Windows Pro/Enterprise, incluso in Windows)
- KVM/QEMU (Linux, alta performance)

---

## Checklist Finale

Prima di completare l'esercitazione, verifica di aver:

- [ ] Installato VMware Workstation 16
- [ ] Abilitato la virtualizzazione nel BIOS
- [ ] Creato almeno una VM Linux con Easy Install
- [ ] Creato almeno una VM Windows
- [ ] Verificato l'installazione di VMware Tools su entrambe
- [ ] Testato clipboard condivisa
- [ ] Configurato almeno una cartella condivisa
- [ ] Creato almeno uno snapshot
- [ ] Ripristinato uno snapshot con successo
- [ ] Clonato una VM (versione Pro)
- [ ] Esportato una VM in formato .ova
- [ ] Testato almeno due modalità di rete (NAT e Bridged)
- [ ] Risolto almeno un problema comune

**Congratulazioni! Hai completato l'esercitazione sulla virtualizzazione con VMware Workstation 16!** 🎉

---

## Parte 12: Documentazione Finale e Consegna

### 12.1 Template Documento Google

Struttura il tuo documento come segue:

```
1. COPERTINA
   - Titolo: "Esercitazione 2 - Virtualizzazione con VMware Workstation 16"
   - Nome e Cognome: _______________
   - Classe: _______________
   - Data: _______________

2. INDICE
   - Elenco numerato di tutte le sezioni con numero di pagina

3. INTRODUZIONE (5-10 righe)
   - Breve descrizione degli obiettivi dell'esercitazione
   - Configurazione dell'ambiente usato (specifiche del PC host)

4. PARTI 1-11
   Per ogni parte includi:
   - Screenshot numerati (📸) con annotazioni e didascalia
   - Risposte complete alle domande di riflessione (❓)

5. CONCLUSIONI (minimo 10 righe)
   - Cosa hai imparato
   - Differenze rispetto a VirtualBox (se già usato in precedenza)
   - Difficoltà incontrate e come le hai risolte
   - Possibili usi pratici di VMware Workstation 16
```

### 12.2 Checklist di Controllo

Prima di consegnare, verifica di aver incluso tutti gli elementi richiesti:

**Screenshot richiesti:**
- [ ] 📸 SCREENSHOT 1: Schermata principale di VMware Workstation 16 installato
- [ ] 📸 SCREENSHOT 2: Wizard di installazione VMware (in corso o completato)
- [ ] 📸 SCREENSHOT 3: BIOS/UEFI con virtualizzazione abilitata (o Task Manager)
- [ ] 📸 SCREENSHOT 4: Wizard Easy Install con rilevamento Ubuntu automatico
- [ ] 📸 SCREENSHOT 5: VM Ubuntu avviata (desktop Ubuntu visibile)
- [ ] 📸 SCREENSHOT 6: Output `vmware-toolsd --version` nel terminale
- [ ] 📸 SCREENSHOT 7: Clipboard condivisa funzionante (copia/incolla host ↔ guest)
- [ ] 📸 SCREENSHOT 8: VM Settings → Shared Folders con cartella configurata
- [ ] 📸 SCREENSHOT 9: Output `ls /mnt/hgfs/` con cartella condivisa visibile
- [ ] 📸 SCREENSHOT 10: Desktop Windows in esecuzione nella VM
- [ ] 📸 SCREENSHOT 11: VMware Tools installati su Windows (Pannello di Controllo)
- [ ] 📸 SCREENSHOT 12: Unity Mode attivo (finestre Windows nel desktop host)
- [ ] 📸 SCREENSHOT 13: Snapshot Manager con albero degli snapshot (almeno 2-3)
- [ ] 📸 SCREENSHOT 14: VM clonata visibile nell'elenco VMware Workstation
- [ ] 📸 SCREENSHOT 15: File OVA/OVF esportato (con nome e dimensione visibili)
- [ ] 📸 SCREENSHOT 16: Virtual Network Editor con reti VMnet configurate
- [ ] 📸 SCREENSHOT 17: Port forwarding configurato (NAT Settings)
- [ ] 📸 SCREENSHOT 18: Connessione SSH tramite port forwarding riuscita

**Domande di riflessione:**
- [ ] ❓ Domande 1 (R1.1, R1.2, R1.3) — Preparazione Ambiente
- [ ] ❓ Domande 2 (R2.1, R2.2, R2.3) — Virtualizzazione BIOS
- [ ] ❓ Domande 3 (R3.1, R3.2, R3.3) — Creazione VM Linux
- [ ] ❓ Domande 4 (R4.1, R4.2, R4.3) — VMware Tools
- [ ] ❓ Domande 5 (R5.1, R5.2, R5.3) — VM Windows
- [ ] ❓ Domande 6 (R6.1, R6.2, R6.3) — Gestione Quotidiana
- [ ] ❓ Domande 7 (R7.1, R7.2, R7.3) — Networking

**Altre verifiche:**
- [ ] Almeno un Esercizio Pratico (Parte 11) completato con documentazione
- [ ] Conclusioni personali (minimo 10 righe)
- [ ] Tutti gli screenshot hanno una didascalia descrittiva
- [ ] Il documento è esportato come PDF

### 12.3 Criteri di Valutazione

| Criterio | Descrizione | Peso |
|---|---|---|
| **Completezza** | Tutti gli screenshot e le domande sono presenti | 25% |
| **Correttezza** | Le risposte sono tecnicamente accurate | 25% |
| **Approfondimento** | Le risposte mostrano comprensione profonda, non solo definizioni | 20% |
| **Chiarezza** | Screenshot ben annotati, testo chiaro e ben organizzato | 15% |
| **Sperimentazione** | Esercizi pratici completati con documentazione | 10% |
| **Riflessione critica** | Confronto con VirtualBox, considerazioni personali motivate | 5% |

### 12.4 Consegna

```
1. Apri il documento su Google Docs
2. File → Scarica → Documento PDF (.pdf)
3. Rinomina il file: VMware_Lab_[CognomeNome]_[Classe]_[Data].pdf
   Esempio: VMware_Lab_RossiMario_3A_2024-03-15.pdf
4. Carica il PDF su Google Drive (nella cartella della classe)
5. Tasto destro sul file → Condividi → "Chiunque con il link può visualizzare"
6. Copia il link e invialo al docente tramite il canale indicato
   (email / Google Classroom / registro elettronico)
```

> ⚠️ **Importante:** Invia il link al **file PDF** su Google Drive, non il link al documento Google Docs.
> La condivisione deve essere impostata **prima** di inviare il link.
