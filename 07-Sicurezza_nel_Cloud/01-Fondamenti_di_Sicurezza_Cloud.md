# Fondamenti di Sicurezza nel Cloud

## Introduzione

La sicurezza nel cloud è una responsabilità condivisa tra provider e cliente. Questo capitolo copre i principi fondamentali necessari per proteggere risorse, dati e applicazioni nel cloud.

## Modello di Responsabilità Condivisa

### Responsabilità del Provider
- Sicurezza fisica datacenter
- Hardware e infrastruttura
- Hypervisor
- Network infrastructure

### Responsabilità del Cliente  
- Dati e encryption
- IAM e access control
- OS patching (per IaaS)
- Applicazioni
- Network configuration

## Identity and Access Management (IAM)

### Principi Fondamentali

**Least Privilege**: Dare solo i permessi minimi necessari

**Separation of Duties**: Dividere responsabilità critiche

**Defense in Depth**: Multipli livelli di sicurezza

### Componenti IAM

**Users**: Identità permanenti per persone

**Groups**: Collezioni di users

**Roles**: Identità temporanee assumibili  

**Policies**: Documenti JSON con permessi

### Multi-Factor Authentication (MFA)

Richiede 2+ fattori:
1. Qualcosa che conosci (password)
2. Qualcosa che possiedi (token, smartphone)
3. Qualcosa che sei (biometria)

## Network Security

### Security Groups
- Firewall stateful
- Livello istanza
- Allow rules only

### Network ACLs
- Firewall stateless
- Livello subnet
- Allow e deny rules

### VPN e Private Connectivity
- Site-to-Site VPN
- Client VPN
- Direct Connect / ExpressRoute
- Private Link

## Monitoring e Logging

### Log Essenziali
- Access logs
- API logs (CloudTrail, Activity Log)
- VPC Flow logs
- Application logs

### SIEM
- Splunk
- Elastic Stack
- Azure Sentinel
- AWS Security Hub

## Incident Response

### Fasi
1. Preparation
2. Detection
3. Containment
4. Eradication  
5. Recovery
6. Lessons Learned

## Zero Trust Security

**Never trust, always verify**

Principi:
- Verify explicitly
- Least privilege access
- Assume breach

## Best Practices

1. Usa MFA per tutti gli utenti
2. Implementa least privilege
3. Abilita logging e monitoring
4. Encrypta data at rest e in transit
5. Segmenta network
6. Patch regolarmente
7. Backup e disaster recovery
8. Training sicurezza per team

## Esercizi

1. Configurare IAM con least privilege
2. Setup MFA per utenti
3. Creare network segmentation
4. Configurare monitoring e alerting
5. Simulare incident response

## Domande di Verifica

1. Spiega il modello di responsabilità condivisa
2. Differenza tra autenticazione e autorizzazione?
3. Perché MFA è importante?
4. Cosa significa least privilege?
5. Differenza tra security groups e NACLs?

---

*Documento aggiornato - 2024*
