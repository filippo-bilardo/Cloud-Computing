### 🧪 Lab 7: "Pipeline as Code" - CI/CD con GitHub Actions
> **Durata**: 90 min | **Difficoltà**: ⭐⭐⭐

**Obiettivo**: Automatizzare build, test e deploy di applicazioni containerizzate

```yaml
# File: .github/workflows/ci-cd.yml
name: Cloud Lab CI/CD

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Build & Test Docker Image
      run: |
        docker build -t myapp:${{ github.sha }} .
        docker run myapp:${{ github.sha }} npm test
        
  security-scan:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: 'myapp:${{ github.sha }}'
        format: 'table'
        exit-code: '1'
        ignore-unfixed: true
        
  deploy-staging:
    needs: security-scan
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
    - name: Deploy to Kubernetes (simulato)
      run: |
        echo "🚀 Deploying ${{ github.sha }} to staging"
        # kubectl apply -f k8s/ --dry-run=client
```

**Attività**:
1. Configurare workflow multi-stage: test → security → deploy
2. Aggiungere matrix strategy per test multipli (Node versions, Java versions)
3. Implementare cache per accelerare build Docker
4. Configurare environment protection rules
5. (Bonus) Integrare notifiche Slack/Teams

**Deliverable**: Pipeline verde + screenshot run completato

---

### 🧪 Lab 8: "GitOps Intro" - Deploy con ArgoCD (simulato)
> **Durata**: 60 min | **Difficoltà**: ⭐⭐⭐

**Obiettivo**: Comprendere i principi GitOps attraverso un flusso dichiarativo

```bash
# Scenario semplificato (senza cluster reale):
# 1. Repository "app-config" con manifest Kubernetes
# 2. Repository "app-source" con codice applicativo
# 3. Simulare ArgoCD che sincronizza config → cluster

# Attività:
1. Strutturare repo Git con kustomize overlays (dev/staging/prod)
2. Creare PR che modifica replica count → osservare "desired state"
3. Simulare drift detection: modificare manualmente il cluster
4. Implementare approval workflow per production deploy
```

**Struttura kustomize**:
```
k8s/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
├── overlays/
│   ├── dev/
│   │   ├── replica-patch.yaml
│   │   └── kustomization.yaml
│   └── prod/
│       ├── replica-patch.yaml
│       ├── resources-limits.yaml
│       └── kustomization.yaml
```

---

## 🔹 AREA 4: Monitoring & Observability

### 🧪 Lab 9: "Monitoraggio Cloud-Native" - Prometheus + Grafana
> **Durata**: 75 min | **Difficoltà**: ⭐⭐⭐

**Obiettivo**: Implementare osservabilità di base per applicazioni containerizzate

```bash
# Setup con Helm (fornire values.yaml preconfigurato):
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace \
  -f monitoring-values.yaml
```

**Attività**:
1. Instrumentare app con metrics endpoint (`/metrics` formato Prometheus)
2. Configurare ServiceMonitor per discovery automatico
3. Creare dashboard Grafana con:
   - Request rate, error rate, latency (RED method)
   - Pod resource usage (CPU/memory)
   - Custom business metrics
4. Configurare alerting rule semplice (es. error rate > 5%)

**Esempio metrica custom** (Node.js):
```javascript
const client = require('prom-client');
const httpRequestDuration = new client.Histogram({
  name: 'http_request_duration_seconds',
  help: 'Duration of HTTP requests in seconds',
  labelNames: ['method', 'route', 'status_code']
});

// Middleware Express
app.use((req, res, next) => {
  const end = httpRequestDuration.startTimer();
  res.on('finish', () => {
    end({ 
      method: req.method, 
      route: req.route?.path || req.path, 
      status_code: res.statusCode 
    });
  });
  next();
});
```

**Deliverable**: Screenshot dashboard + query PromQL utilizzata

---

## 🔹 AREA 5: Security & Best Practices

### 🧪 Lab 10: "Secure by Design" - Security Hardening per Container
> **Durata**: 60 min | **Difficoltà**: ⭐⭐⭐

**Obiettivo**: Applicare principi di sicurezza a ogni layer dello stack

```bash
# Checklist pratica da implementare:

🔐 Dockerfile Security:
- [ ] Usare immagini base ufficiali e minimali
- [ ] Specificare USER non-root
- [ ] Rimuovere package manager dopo install
- [ ] Aggiungere HEALTHCHECK
- [ ] Usare COPY --chown invece di RUN chown

🔐 Kubernetes Security:
- [ ] PodSecurityContext: runAsNonRoot, readOnlyRootFilesystem
- [ ] SecurityContext: allowPrivilegeEscalation: false
- [ ] NetworkPolicy: limitare traffico in/out
- [ ] Resource limits: prevenire DoS accidentali

🔐 Supply Chain:
- [ ] Scan immagini con Trivy/Grype
- [ ] Firmare immagini con Cosign (demo)
- [ ] Usare pinned versions (no :latest)
```

**Esercizio "Capture The Flag"**:
> "Trova 5 vulnerabilità in questo Dockerfile/K8s manifest volutamente insicuro"

```dockerfile
# ❌ Dockerfile insicuro (da correggere)
FROM ubuntu:latest  # ❌ latest + non-minimal
RUN apt-get update && apt-get install -y curl vim sudo  # ❌ tool non necessari
COPY app/ /app  # ❌ permessi root
CMD ["/app/start.sh"]  # ❌ no HEALTHCHECK, no USER
```

---

## 🔹 AREA 6: Progetti Integrati (Capstone)

### 🏆 Progetto Finale: "Cloud-Native Microservices Platform"
> **Durata**: 3-4 sessioni | **Difficoltà**: ⭐⭐⭐⭐

**Scenario**: Costruire una piattaforma e-commerce minimale con architettura cloud-native

```
📦 Architettura Richiesta:
┌─────────────────────────────────────┐
│ Ingress Controller (nginx/traefik)  │
├─────────────────────────────────────┤
│ 🎨 Frontend (React)                 │
│    - Deployment + HPA               │
│    - ConfigMap per API endpoint     │
├─────────────────────────────────────┤
│ 🔌 API Gateway (Node.js/Express)    │
│    - Routing ai microservizi        │
│    - Rate limiting base             │
│    - /health e /metrics endpoints   │
├─────────────────────────────────────┤
│ 📦 Microservizi:                    │
│    • Catalog Service (Java/Spring)  │
│    • Order Service (Node.js)        │
│    • Payment Service (mock)         │
│    → Ognuno: Deployment + Service   │
├─────────────────────────────────────┤
│ 🗄️ Data Layer:                      │
│    • PostgreSQL (StatefulSet + PVC) │
│    • Redis (Deployment + Service)   │
├─────────────────────────────────────┤
│ ⚙️ Infrastructure:                  │
│    • Namespace isolato              │
│    • NetworkPolicy per segmentazione│
│    • ResourceQuota per namespace    │
│    • ConfigMaps + Secrets           │
└─────────────────────────────────────┘
```

**Consegne**:
```markdown
📁 Repository Structure:
my-cloud-platform/
├── README.md                 # Documentazione architetturale
├── diagrams/                 # Architettura (draw.io / mermaid)
├── services/                 # Codice microservizi
│   ├── catalog/
│   ├── orders/
│   └── gateway/
├── k8s/                      # Manifest Kubernetes
│   ├── base/                 # Configurazione comune
│   ├── overlays/             # dev/staging/prod
│   └── kustomization.yaml
├── .github/workflows/        # CI/CD pipeline
├── docker-compose.yml        # Ambiente locale dev
├── .devcontainer/            # Codespace config
└── SECURITY.md               # Decisioni security
```

**Criteri di valutazione**:
| Area | Peso | Criteri |
|------|------|---------|
| Architettura | 25% | Coerenza microservizi, separazione concern |
| Kubernetes | 25% | Manifest validi, best practice, HPA/probe |
| CI/CD | 20% | Pipeline funzionante, test, security scan |
| Security | 15% | Hardening container, RBAC, network policies |
| Documentazione | 15% | README chiaro, diagrammi, decisioni |

---

## 🎯 Bonus: Esercitazioni "Lightning Lab" (15-20 min)

Per riempire tempi morti o come warm-up:

| Nome | Obiettivo | Comandi chiave |
|------|-----------|---------------|
| 🔍 `docker inspect` deep dive | Capire metadata container | `docker inspect <id> \| jq` |
| 🔄 `kubectl rollout` | Gestire aggiornamenti senza downtime | `rollout status`, `undo`, `history` |
| 📦 `docker buildx` | Build multi-architetture | `buildx create`, `--platform linux/amd64,arm64` |
| 🧹 `docker system prune` | Gestione spazio e cleanup | `prune`, `df`, `images --filter` |
| 🌐 `kubectl port-forward` | Debug servizi senza expose | `port-forward service/myapp 8080:80` |
| 🔐 `docker secret` | Gestione credenziali in swarm | `secret create`, `--secret in service` |

---

## 🛠️ Toolkit Didattico Consigliato

```yaml
# Per ogni lab, fornisci:
starter-kit/
├── README.md          # Istruzioni passo-passo
├── solution/          # Cartella con soluzione (per docente)
├── tests/             # Script di verifica automatica
├── troubleshooting.md # FAQ e soluzioni errori comuni
└── quiz.md           # 3-5 domande di verifica concetti
```

**Strumenti per il docente**:
- ✅ **GitHub Classroom**: per distribuire repo personali a ogni studente
- ✅ **Killercoda / Katacoda**: ambienti temporanei per chi non ha risorse locali
- ✅ **Kind + Tilt**: per sviluppo locale rapido su Kubernetes
- ✅ **Octant / Lens**: UI per monitorare cluster durante le esercitazioni
