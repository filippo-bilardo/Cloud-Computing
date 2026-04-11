# Orchestrazione con Kubernetes

## Introduzione a Kubernetes

Kubernetes (K8s) è una piattaforma open-source per l'automazione del deployment, scaling e gestione di applicazioni containerizzate. Originariamente sviluppato da Google e donato alla Cloud Native Computing Foundation (CNCF) nel 2014.

### Cos'è Kubernetes?

**Kubernetes** è:
- Un orchestratore di container
- Una piattaforma per gestire workload containerizzati
- Un sistema distribuito resiliente
- Lo standard de facto per container orchestration

### Perché Kubernetes?

**Problemi Risolti:**
- Deployment manuale di container
- Scaling manuale
- Load balancing
- Self-healing
- Rolling updates
- Service discovery
- Configuration management
- Storage orchestration

### Origini

- **2003-2004**: Google sviluppa Borg
- **2013**: Google sviluppa Omega
- **2014**: Google rilascia Kubernetes open-source
- **2015**: Kubernetes v1.0 e fondazione CNCF
- **2018**: Kubernetes diventa primo progetto CNCF graduated

## Architettura di Kubernetes

### Cluster Kubernetes

```
┌─────────────────────────────────────────────────────────┐
│                    KUBERNETES CLUSTER                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────┐         │
│  │         CONTROL PLANE (Master)             │         │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │         │
│  │  │ API      │  │Scheduler │  │Controller│ │         │
│  │  │ Server   │  │          │  │ Manager  │ │         │
│  │  └──────────┘  └──────────┘  └──────────┘ │         │
│  │  ┌──────────┐  ┌──────────┐               │         │
│  │  │  etcd    │  │cloud-    │               │         │
│  │  │          │  │controller│               │         │
│  │  └──────────┘  └──────────┘               │         │
│  └────────────────────────────────────────────┘         │
│                         │                                │
│                         │ (API calls)                    │
│                         │                                │
│  ┌──────────────────────┼─────────────────────────────┐ │
│  │                   WORKER NODES                      │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │ │
│  │  │   Node 1    │  │   Node 2    │  │   Node 3    │ │ │
│  │  │ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │ │ │
│  │  │ │ Kubelet │ │  │ │ Kubelet │ │  │ │ Kubelet │ │ │ │
│  │  │ ├─────────┤ │  │ ├─────────┤ │  │ ├─────────┤ │ │ │
│  │  │ │Kube-proxy││  │ Kube-proxy│ │  │ Kube-proxy│ │ │ │
│  │  │ ├─────────┤ │  │ ├─────────┤ │  │ ├─────────┤ │ │ │
│  │  │ │Container│ │  │ │Container│ │  │ │Container│ │ │ │
│  │  │ │ Runtime │ │  │ │ Runtime │ │  │ │ Runtime │ │ │ │
│  │  │ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │ │ │
│  │  │   Pods...   │  │   Pods...   │  │   Pods...   │ │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘ │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Control Plane Components

#### 1. API Server (kube-apiserver)

**Funzioni:**
- Front-end del control plane
- Espone API Kubernetes (REST)
- Punto di ingresso per tutti i comandi
- Autentica e autorizza richieste
- Valida configurazioni

```bash
# Interazione con API Server
kubectl get pods
kubectl apply -f deployment.yaml
```

#### 2. etcd

**Funzioni:**
- Database distribuito key-value
- Memorizza stato del cluster
- Configurazioni
- Secrets
- Metadata

**Caratteristiche:**
- Consistent e highly-available
- Raft consensus algorithm
- Backup critici!

#### 3. Scheduler (kube-scheduler)

**Funzioni:**
- Assegna Pod ai Node
- Considera requisiti risorse
- Constraints e affinity rules
- Taints e tolerations
- Bilanciamento carico

**Processo Scheduling:**
1. Filtra Node non idonei
2. Assegna score ai Node
3. Seleziona Node migliore
4. Binding del Pod

#### 4. Controller Manager (kube-controller-manager)

**Controllers Principali:**
- **Node Controller**: Monitora health dei node
- **Replication Controller**: Mantiene numero corretto di pod
- **Endpoints Controller**: Popola Endpoints
- **Service Account Controller**: Crea account di default
- **Deployment Controller**: Gestisce deployment
- **StatefulSet Controller**: Gestisce stateful apps

#### 5. Cloud Controller Manager

**Funzioni:**
- Integrazione cloud-specific
- Node management
- Route management
- Load balancer provisioning
- Volume provisioning

### Node Components

#### 1. Kubelet

**Funzioni:**
- Agente su ogni node
- Comunica con API Server
- Gestisce Pod lifecycle
- Monitora health container
- Riporta status

#### 2. Kube-proxy

**Funzioni:**
- Network proxy su ogni node
- Implementa Service abstraction
- Gestisce network rules
- Load balancing

**Modalità:**
- userspace
- iptables (default)
- IPVS

#### 3. Container Runtime

**Supportati:**
- **containerd**: Default, CNCF graduated
- **CRI-O**: OCI-based, lightweight
- **Docker**: Via cri-dockerd

## Oggetti Kubernetes Fondamentali

### Pod

Il **Pod** è l'unità base di deployment in Kubernetes.

```yaml
# pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-pod
  labels:
    app: nginx
    environment: production
  annotations:
    description: "Nginx web server"
spec:
  containers:
  - name: nginx
    image: nginx:1.21
    ports:
    - containerPort: 80
      name: http
      protocol: TCP
    resources:
      requests:
        memory: "64Mi"
        cpu: "250m"
      limits:
        memory: "128Mi"
        cpu: "500m"
    env:
    - name: ENVIRONMENT
      value: "production"
    volumeMounts:
    - name: config
      mountPath: /etc/nginx/nginx.conf
      subPath: nginx.conf
    livenessProbe:
      httpGet:
        path: /
        port: 80
      initialDelaySeconds: 15
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /
        port: 80
      initialDelaySeconds: 5
      periodSeconds: 5
  
  # Init containers
  initContainers:
  - name: init-config
    image: busybox
    command: ['sh', '-c', 'echo "Initializing..." && sleep 5']
  
  volumes:
  - name: config
    configMap:
      name: nginx-config
  
  # Restart policy
  restartPolicy: Always
  
  # Node selection
  nodeSelector:
    disktype: ssd
  
  # Tolerations
  tolerations:
  - key: "key1"
    operator: "Equal"
    value: "value1"
    effect: "NoSchedule"
```

**Pod Patterns:**

1. **Sidecar Pattern**
```yaml
spec:
  containers:
  - name: main-app
    image: myapp
  - name: log-shipper
    image: fluentd
    volumeMounts:
    - name: logs
      mountPath: /var/log
```

2. **Ambassador Pattern**
```yaml
spec:
  containers:
  - name: main-app
    image: myapp
  - name: proxy
    image: ambassador
```

3. **Adapter Pattern**
```yaml
spec:
  containers:
  - name: main-app
    image: myapp
  - name: adapter
    image: metrics-adapter
```

### ReplicaSet

Mantiene un numero stabile di Pod repliche.

```yaml
# replicaset.yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: nginx-rs
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.21
        ports:
        - containerPort: 80
```

### Deployment

Gestisce deployments dichiarativi di ReplicaSet.

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 3
  
  # Selector per Pod
  selector:
    matchLabels:
      app: nginx
  
  # Strategia di deployment
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # Max pod extra durante update
      maxUnavailable: 1  # Max pod non disponibili
  
  # Revisioni salvate
  revisionHistoryLimit: 10
  
  # Template Pod
  template:
    metadata:
      labels:
        app: nginx
        version: v1
    spec:
      containers:
      - name: nginx
        image: nginx:1.21
        ports:
        - containerPort: 80
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 200m
            memory: 256Mi
        livenessProbe:
          httpGet:
            path: /healthz
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
```

**Comandi Deployment:**

```bash
# Creare deployment
kubectl create deployment nginx --image=nginx:1.21 --replicas=3

# Applicare da file
kubectl apply -f deployment.yaml

# Scalare
kubectl scale deployment nginx-deployment --replicas=5

# Update immagine
kubectl set image deployment/nginx-deployment nginx=nginx:1.22

# Rollout status
kubectl rollout status deployment/nginx-deployment

# Rollout history
kubectl rollout history deployment/nginx-deployment

# Rollback
kubectl rollout undo deployment/nginx-deployment
kubectl rollout undo deployment/nginx-deployment --to-revision=2

# Pause/Resume
kubectl rollout pause deployment/nginx-deployment
kubectl rollout resume deployment/nginx-deployment
```

### Service

Espone un'applicazione running in un set di Pod.

#### 1. ClusterIP (Default)

```yaml
# service-clusterip.yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  type: ClusterIP
  selector:
    app: nginx
  ports:
  - protocol: TCP
    port: 80          # Service port
    targetPort: 80    # Container port
  sessionAffinity: ClientIP
```

#### 2. NodePort

```yaml
# service-nodeport.yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-nodeport
spec:
  type: NodePort
  selector:
    app: nginx
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
    nodePort: 30080  # 30000-32767
```

#### 3. LoadBalancer

```yaml
# service-loadbalancer.yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-lb
spec:
  type: LoadBalancer
  selector:
    app: nginx
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
  loadBalancerIP: 192.168.1.100  # Optional
```

#### 4. ExternalName

```yaml
# service-externalname.yaml
apiVersion: v1
kind: Service
metadata:
  name: external-db
spec:
  type: ExternalName
  externalName: database.example.com
```

#### Headless Service

```yaml
# headless-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-headless
spec:
  clusterIP: None  # Headless!
  selector:
    app: nginx
  ports:
  - port: 80
```

### ConfigMap

Gestisce configurazioni non-confidenziali.

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  # Key-value pairs
  database_url: "postgres://db:5432/mydb"
  log_level: "info"
  
  # File-like keys
  nginx.conf: |
    server {
      listen 80;
      server_name example.com;
      location / {
        proxy_pass http://backend:8080;
      }
    }
  
  app.properties: |
    app.name=MyApp
    app.version=1.0
```

**Uso ConfigMap:**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
  - name: app
    image: myapp
    
    # Env da ConfigMap
    env:
    - name: DATABASE_URL
      valueFrom:
        configMapKeyRef:
          name: app-config
          key: database_url
    
    # Tutte le keys come env
    envFrom:
    - configMapRef:
        name: app-config
    
    # Mount come volume
    volumeMounts:
    - name: config
      mountPath: /etc/config
  
  volumes:
  - name: config
    configMap:
      name: app-config
```

```bash
# Creare ConfigMap
kubectl create configmap app-config --from-literal=key1=value1 --from-literal=key2=value2
kubectl create configmap nginx-config --from-file=nginx.conf
kubectl create configmap app-configs --from-file=configs/
```

### Secret

Gestisce dati sensibili.

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  # Base64 encoded
  username: YWRtaW4=          # admin
  password: cGFzc3dvcmQxMjM=  # password123

---
# TLS Secret
apiVersion: v1
kind: Secret
metadata:
  name: tls-secret
type: kubernetes.io/tls
data:
  tls.crt: |
    LS0tLS1CRUdJTi...
  tls.key: |
    LS0tLS1CRUdJTi...

---
# Docker Registry Secret
apiVersion: v1
kind: Secret
metadata:
  name: registry-secret
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: |
    eyJhdXRocyI6eyJod...
```

**Uso Secret:**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
  - name: app
    image: myapp
    
    # Env da Secret
    env:
    - name: DB_PASSWORD
      valueFrom:
        secretKeyRef:
          name: app-secrets
          key: password
    
    # Mount come volume
    volumeMounts:
    - name: secrets
      mountPath: /etc/secrets
      readOnly: true
  
  volumes:
  - name: secrets
    secret:
      secretName: app-secrets
  
  # Image pull secret
  imagePullSecrets:
  - name: registry-secret
```

```bash
# Creare Secret
kubectl create secret generic app-secrets \
  --from-literal=username=admin \
  --from-literal=password=password123

kubectl create secret tls tls-secret \
  --cert=path/to/cert.crt \
  --key=path/to/cert.key

kubectl create secret docker-registry registry-secret \
  --docker-server=registry.example.com \
  --docker-username=user \
  --docker-password=pass \
  --docker-email=user@example.com

# Encode/Decode
echo -n 'admin' | base64        # YWRtaW4=
echo 'YWRtaW4=' | base64 -d     # admin
```

### Namespace

Organizza risorse in cluster virtuali.

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: development
  labels:
    environment: dev
```

```bash
# Namespace commands
kubectl create namespace production
kubectl get namespaces
kubectl config set-context --current --namespace=development

# Resource quota per namespace
kubectl create quota dev-quota \
  --hard=pods=10,requests.cpu=4,requests.memory=8Gi \
  --namespace=development
```

## Networking in Kubernetes

### Network Model

**Requisiti Kubernetes:**
1. Pods comunicano senza NAT
2. Nodes comunicano con Pods senza NAT
3. IP visto dal Pod è lo stesso visto dagli altri

### CNI Plugins

**Popolari:**
- **Calico**: Network policy, eBPF
- **Flannel**: Semplice, overlay
- **Weave Net**: Mesh networking
- **Cilium**: eBPF-based, avanzato
- **Antrea**: VMware, enterprise

### Ingress

Gestisce accesso HTTP/HTTPS al cluster.

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  
  # TLS
  tls:
  - hosts:
    - example.com
    - www.example.com
    secretName: tls-secret
  
  # Rules
  rules:
  - host: example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 80
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8080
  
  - host: admin.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: admin
            port:
              number: 3000
```

**Ingress Controllers:**
- **NGINX Ingress Controller**
- **Traefik**
- **HAProxy**
- **Istio Gateway**
- **Kong**

```bash
# Installare NGINX Ingress
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

### Network Policies

Controllo traffico tra Pod.

```yaml
# networkpolicy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: api-network-policy
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: api
  
  policyTypes:
  - Ingress
  - Egress
  
  # Traffico in ingresso
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: production
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 8080
  
  # Traffico in uscita
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: database
    ports:
    - protocol: TCP
      port: 5432
  
  - to:  # DNS
    - namespaceSelector:
        matchLabels:
          name: kube-system
    - podSelector:
        matchLabels:
          k8s-app: kube-dns
    ports:
    - protocol: UDP
      port: 53
```

## Storage in Kubernetes

### Volumes

**Tipi:**
- **emptyDir**: Temporaneo, condiviso tra container
- **hostPath**: Mount da node (dev only!)
- **configMap/secret**: Configurazioni
- **persistentVolumeClaim**: Storage persistente

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: volume-pod
spec:
  containers:
  - name: app
    image: nginx
    volumeMounts:
    - name: cache
      mountPath: /cache
    - name: data
      mountPath: /data
    - name: config
      mountPath: /config
  
  volumes:
  - name: cache
    emptyDir: {}
  
  - name: data
    persistentVolumeClaim:
      claimName: data-pvc
  
  - name: config
    configMap:
      name: app-config
```

### PersistentVolume (PV)

```yaml
# pv.yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-data
spec:
  capacity:
    storage: 10Gi
  
  volumeMode: Filesystem
  
  accessModes:
  - ReadWriteOnce     # RWO: singolo node R/W
  # - ReadOnlyMany    # ROX: multi node R
  # - ReadWriteMany   # RWX: multi node R/W
  
  persistentVolumeReclaimPolicy: Retain  # Retain, Delete, Recycle
  
  storageClassName: fast-ssd
  
  # Locale
  hostPath:
    path: /mnt/data
  
  # NFS
  # nfs:
  #   server: nfs-server.example.com
  #   path: /exports/data
  
  # Cloud (AWS EBS)
  # awsElasticBlockStore:
  #   volumeID: vol-0123456789
  #   fsType: ext4
```

### PersistentVolumeClaim (PVC)

```yaml
# pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: data-pvc
spec:
  accessModes:
  - ReadWriteOnce
  
  resources:
    requests:
      storage: 5Gi
  
  storageClassName: fast-ssd
  
  # Selettore opzionale
  selector:
    matchLabels:
      environment: production
```

### StorageClass

```yaml
# storageclass.yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: fast-ssd
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp3
  iops: "3000"
  throughput: "125"
  encrypted: "true"
volumeBindingMode: WaitForFirstConsumer
allowVolumeExpansion: true
reclaimPolicy: Delete
```

**Dynamic Provisioning:**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: dynamic-pvc
spec:
  accessModes:
  - ReadWriteOnce
  storageClassName: fast-ssd  # Auto-provisioning!
  resources:
    requests:
      storage: 10Gi
```

### StatefulSet

Per applicazioni stateful con storage persistente.

```yaml
# statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mongodb
spec:
  serviceName: mongodb
  replicas: 3
  
  selector:
    matchLabels:
      app: mongodb
  
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
      - name: mongodb
        image: mongo:5
        ports:
        - containerPort: 27017
        volumeMounts:
        - name: data
          mountPath: /data/db
  
  # Volume Claim Templates
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: fast-ssd
      resources:
        requests:
          storage: 10Gi
```

**Caratteristiche StatefulSet:**
- Pod con identità stabile (mongodb-0, mongodb-1, mongodb-2)
- Storage persistente per Pod
- Ordine deployment/scaling garantito
- Network identities stabili

```bash
# Headless service per StatefulSet
kubectl create service clusterip mongodb --tcp=27017 --clusterip=None

# Scale
kubectl scale statefulset mongodb --replicas=5

# Update
kubectl patch statefulset mongodb -p '{"spec":{"updateStrategy":{"type":"RollingUpdate"}}}'
```

## Scaling in Kubernetes

### Horizontal Pod Autoscaler (HPA)

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app-deployment
  
  minReplicas: 2
  maxReplicas: 10
  
  metrics:
  # CPU-based
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  
  # Memory-based
  - type: Resource
    resource:
      name: memory
      target:
        type: AverageValue
        averageValue: 500Mi
  
  # Custom metrics
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: 1000
  
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 15
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 4
        periodSeconds: 15
      selectPolicy: Max
```

```bash
# Creare HPA
kubectl autoscale deployment app --cpu-percent=70 --min=2 --max=10

# Status
kubectl get hpa
kubectl describe hpa app-hpa

# Metrics Server (richiesto per HPA)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Vertical Pod Autoscaler (VPA)

```yaml
# vpa.yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: app-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app-deployment
  
  updatePolicy:
    updateMode: "Auto"  # Auto, Initial, Off
  
  resourcePolicy:
    containerPolicies:
    - containerName: app
      minAllowed:
        cpu: 100m
        memory: 128Mi
      maxAllowed:
        cpu: 2
        memory: 2Gi
```

### Cluster Autoscaler

Scala automaticamente i nodi del cluster.

```yaml
# Configurazione cloud-specific
# AWS Auto Scaling Groups
# GCP Instance Groups
# Azure Scale Sets
```

## Comandi kubectl Avanzati

### Gestione Risorse

```bash
# Get resources
kubectl get pods
kubectl get pods -o wide
kubectl get pods -o yaml
kubectl get pods -o json
kubectl get pods --selector=app=nginx
kubectl get pods -A  # All namespaces

# Describe
kubectl describe pod nginx-pod
kubectl describe deployment nginx-deployment

# Create/Apply
kubectl create -f resource.yaml
kubectl apply -f resource.yaml
kubectl apply -f directory/
kubectl apply -k kustomize/

# Delete
kubectl delete pod nginx-pod
kubectl delete -f resource.yaml
kubectl delete pods --all

# Edit
kubectl edit deployment nginx-deployment

# Patch
kubectl patch deployment nginx -p '{"spec":{"replicas":5}}'

# Replace
kubectl replace -f resource.yaml --force
```

### Debug e Troubleshooting

```bash
# Logs
kubectl logs pod-name
kubectl logs pod-name -c container-name
kubectl logs -f pod-name  # Follow
kubectl logs --previous pod-name  # Previous instance
kubectl logs -l app=nginx  # By label

# Exec
kubectl exec pod-name -- ls /
kubectl exec -it pod-name -- /bin/bash
kubectl exec -it pod-name -c container-name -- sh

# Port Forward
kubectl port-forward pod-name 8080:80
kubectl port-forward svc/nginx-service 8080:80
kubectl port-forward deployment/nginx 8080:80

# Top
kubectl top nodes
kubectl top pods
kubectl top pods -A --sort-by=cpu

# Events
kubectl get events
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl get events --field-selector type=Warning

# Debug
kubectl debug pod-name -it --image=busybox
kubectl debug node/node-name -it --image=ubuntu

# Explain
kubectl explain pod
kubectl explain pod.spec.containers

# Diff
kubectl diff -f resource.yaml

# Dry run
kubectl apply -f resource.yaml --dry-run=client
kubectl create deployment nginx --image=nginx --dry-run=client -o yaml
```

### Contexts e Config

```bash
# Contexts
kubectl config get-contexts
kubectl config current-context
kubectl config use-context prod-cluster
kubectl config set-context --current --namespace=production

# Cluster info
kubectl cluster-info
kubectl cluster-info dump

# API resources
kubectl api-resources
kubectl api-versions
```

### Gestione Avanzata

```bash
# Rollout
kubectl rollout status deployment/nginx
kubectl rollout history deployment/nginx
kubectl rollout undo deployment/nginx
kubectl rollout restart deployment/nginx

# Scale
kubectl scale deployment nginx --replicas=5
kubectl scale --replicas=3 -f deployment.yaml

# Autoscale
kubectl autoscale deployment nginx --min=2 --max=10 --cpu-percent=80

# Label e Annotate
kubectl label pods nginx-pod env=production
kubectl annotate pods nginx-pod description="Production nginx"

# Taint e Toleration
kubectl taint nodes node1 key=value:NoSchedule
kubectl taint nodes node1 key-  # Remove

# Cordon/Uncordon
kubectl cordon node1  # Mark unschedulable
kubectl uncordon node1

# Drain
kubectl drain node1 --ignore-daemonsets --delete-emptydir-data
```

## Gestione Applicazioni

### Helm

Package manager per Kubernetes.

```bash
# Installare Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Aggiungere repository
helm repo add stable https://charts.helm.sh/stable
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Cercare charts
helm search repo nginx
helm search hub wordpress

# Installare chart
helm install my-release bitnami/nginx
helm install my-db bitnami/postgresql --set auth.postgresPassword=secret

# List releases
helm list
helm list -A

# Status
helm status my-release

# Upgrade
helm upgrade my-release bitnami/nginx --set replicaCount=3

# Rollback
helm rollback my-release 1

# Uninstall
helm uninstall my-release

# Creare chart
helm create mychart

# Package
helm package mychart/

# Template
helm template my-release bitnami/nginx
```

### Kustomize

Gestione configurazioni senza template.

```yaml
# kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
- deployment.yaml
- service.yaml

namePrefix: dev-

commonLabels:
  environment: development

configMapGenerator:
- name: app-config
  literals:
  - LOG_LEVEL=debug
  - DATABASE_URL=postgres://localhost/devdb

images:
- name: myapp
  newTag: v1.2.3

replicas:
- name: myapp-deployment
  count: 3
```

```bash
# Build
kubectl kustomize ./
kubectl apply -k ./

# Overlays
# base/
#   kustomization.yaml
#   deployment.yaml
# overlays/
#   dev/
#     kustomization.yaml
#   prod/
#     kustomization.yaml

kubectl apply -k overlays/prod/
```

## Sicurezza in Kubernetes

### RBAC (Role-Based Access Control)

```yaml
# role.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader
  namespace: default
rules:
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list", "watch"]

---
# rolebinding.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: read-pods
  namespace: default
subjects:
- kind: User
  name: jane
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io

---
# clusterrole.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: cluster-admin
rules:
- apiGroups: ["*"]
  resources: ["*"]
  verbs: ["*"]

---
# serviceaccount.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: app-sa
  namespace: default
```

```bash
# Verificare permessi
kubectl auth can-i create deployments
kubectl auth can-i delete pods --as=jane
kubectl auth can-i list secrets --as=system:serviceaccount:default:app-sa
```

### Security Context

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: security-pod
spec:
  securityContext:
    runAsUser: 1000
    runAsGroup: 3000
    fsGroup: 2000
    seccompProfile:
      type: RuntimeDefault
  
  containers:
  - name: app
    image: myapp
    securityContext:
      allowPrivilegeEscalation: false
      capabilities:
        drop:
        - ALL
        add:
        - NET_BIND_SERVICE
      readOnlyRootFilesystem: true
      runAsNonRoot: true
```

### Pod Security Standards

```yaml
# Pod Security Admission
apiVersion: v1
kind: Namespace
metadata:
  name: production
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

**Livelli:**
- **Privileged**: Senza restrizioni
- **Baseline**: Minimamente restrittivo
- **Restricted**: Massima sicurezza

## Monitoring e Logging

### Prometheus + Grafana

```yaml
# prometheus-stack via Helm
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack

# ServiceMonitor example
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: app-metrics
spec:
  selector:
    matchLabels:
      app: myapp
  endpoints:
  - port: metrics
    interval: 30s
```

### Logging Stack

```yaml
# EFK Stack (Elasticsearch, Fluentd, Kibana)
# O ELK Stack (Elasticsearch, Logstash, Kibana)
# O Loki Stack (Grafana Loki)

# Fluentd DaemonSet
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluentd
  namespace: kube-system
spec:
  selector:
    matchLabels:
      app: fluentd
  template:
    metadata:
      labels:
        app: fluentd
    spec:
      containers:
      - name: fluentd
        image: fluent/fluentd-kubernetes-daemonset:v1-debian-elasticsearch
        env:
        - name: FLUENT_ELASTICSEARCH_HOST
          value: "elasticsearch.logging"
        volumeMounts:
        - name: varlog
          mountPath: /var/log
        - name: varlibdockercontainers
          mountPath: /var/lib/docker/containers
          readOnly: true
      volumes:
      - name: varlog
        hostPath:
          path: /var/log
      - name: varlibdockercontainers
        hostPath:
          path: /var/lib/docker/containers
```

## Best Practices

### Deployment

1. **Usa Deployment, non Pod diretti**
2. **Definisci Resource Requests/Limits**
3. **Implementa Health Checks**
4. **Usa Rolling Updates**
5. **Mantieni Storia Revisioni**

### Sicurezza

1. **Non eseguire come root**
2. **Usa RBAC**
3. **Scan immagini per vulnerabilità**
4. **Usa Network Policies**
5. **Abilita Pod Security**
6. **Gestisci Secrets correttamente**

### Configurazione

1. **Usa ConfigMaps e Secrets**
2. **Esternalizza configurazioni**
3. **Versiona configurazioni**
4. **Usa Namespaces per separazione**

### Networking

1. **Usa Services per comunicazione**
2. **Implementa Network Policies**
3. **Usa Ingress per traffico HTTP**
4. **TLS everywhere**

### Storage

1. **Usa PVC per persistenza**
2. **Implementa backup strategy**
3. **Scegli StorageClass appropriato**
4. **Monitora spazio disco**

## Troubleshooting

### Pod in CrashLoopBackOff

```bash
# Verificare logs
kubectl logs pod-name --previous

# Verificare eventi
kubectl describe pod pod-name

# Debug con container temporaneo
kubectl debug pod-name -it --image=busybox --share-processes
```

### Problemi Networking

```bash
# Test connettività
kubectl run test --rm -it --image=busybox -- sh
wget -O- http://service-name

# DNS
kubectl run test --rm -it --image=busybox -- nslookup service-name

# Network policies
kubectl describe networkpolicy
```

### Problemi Storage

```bash
# PVC status
kubectl get pvc
kubectl describe pvc pvc-name

# PV status
kubectl get pv
kubectl describe pv pv-name
```

## Esercizi Pratici

### Esercizio 1: Deploy Applicazione Completa

```yaml
# Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: webapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: webapp
  template:
    metadata:
      labels:
        app: webapp
    spec:
      containers:
      - name: webapp
        image: nginx:alpine
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 200m
            memory: 256Mi

---
# Service
apiVersion: v1
kind: Service
metadata:
  name: webapp-service
spec:
  selector:
    app: webapp
  ports:
  - port: 80
  type: LoadBalancer

---
# HPA
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: webapp-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: webapp
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Esercizio 2: StatefulSet MongoDB

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mongodb
spec:
  clusterIP: None
  selector:
    app: mongodb
  ports:
  - port: 27017

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mongodb
spec:
  serviceName: mongodb
  replicas: 3
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
      - name: mongodb
        image: mongo:5
        ports:
        - containerPort: 27017
        volumeMounts:
        - name: data
          mountPath: /data/db
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
```

## Domande di Verifica

1. Spiega l'architettura di Kubernetes e il ruolo di ciascun componente
2. Qual è la differenza tra Deployment e StatefulSet?
3. Come funziona il Service Discovery in Kubernetes?
4. Spiega la differenza tra ConfigMap e Secret
5. Come implementeresti autoscaling in Kubernetes?
6. Qual è la differenza tra PV e PVC?
7. Come gestiresti rolling updates e rollback?
8. Spiega i diversi tipi di Service in Kubernetes
9. Come implementeresti network security in un cluster?
10. Quali sono le best practices per la sicurezza in Kubernetes?

## Risorse Aggiuntive

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kubernetes the Hard Way](https://github.com/kelseyhightower/kubernetes-the-hard-way)
- [CNCF Landscape](https://landscape.cncf.io/)
- [Kubernetes Patterns](https://www.redhat.com/en/resources/oreilly-kubernetes-patterns-guide)
- [Play with Kubernetes](https://labs.play-with-k8s.com/)

## Conclusioni

Kubernetes è diventato lo standard per l'orchestrazione di container, fornendo una piattaforma potente e flessibile per gestire applicazioni containerizzate su larga scala. La sua architettura estensibile e il ricco ecosistema lo rendono la scelta ideale per deployment cloud-native.
