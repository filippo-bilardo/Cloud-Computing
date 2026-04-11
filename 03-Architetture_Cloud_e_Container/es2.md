### 🧪 Lab 2: "Dockerize Everything" - Containerizzazione di Applicazioni Reali
> **Durata**: 60 min | **Difficoltà**: ⭐⭐

**Obiettivo**: Trasformare applicazioni esistenti in container ottimizzati

```bash
# Scenario: Migrare un'applicazione monolitica verso i container

📦 Struttura progetto:
my-app/
├── src/ (codice esistente Python/Node/Java)
├── requirements.txt o package.json
├── Dockerfile (da creare)
├── .dockerignore (da creare)
└── docker-compose.yml (per dipendenze)
```

**Attività**:
1. Analizzare l'applicazione e identificare dipendenze
2. Scrivere un Dockerfile multi-stage per ridurre le dimensioni
3. Configurare `.dockerignore` per escludere file inutili
4. Testare l'immagine con `docker run` e health check
5. Ottimizzare: usare immagini distroless, utente non-root, layer caching

**Sfida bonus**: 
```dockerfile
# Ridurre l'immagine finale sotto i 100MB
# Implementare scan sicurezza con: docker scan o trivy
```

**Deliverable**: Immagine Docker funzionante + report dimensioni prima/dopo
