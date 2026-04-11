# CI/CD nel Cloud

## Introduzione

**CI/CD** (Continuous Integration / Continuous Deployment) è la pratica di automatizzare l'integrazione, testing e deployment del codice.

### Continuous Integration (CI)

- **Build automatiche** ad ogni commit
- **Test automatici** (unit, integration, e2e)
- **Code quality checks** (linting, security scan)
- **Feedback rapido** agli sviluppatori

### Continuous Delivery vs Continuous Deployment

- **Continuous Delivery**: Deploy automatico fino a staging, manual approval per production
- **Continuous Deployment**: Deploy completamente automatico in production

### Benefici CI/CD

- ✅ **Rilasci frequenti**: Da mesi a giorni/ore
- ✅ **Meno bug in produzione**: Testing automatizzato
- ✅ **Feedback rapido**: Problemi rilevati subito
- ✅ **Riproducibilità**: Stesso processo ogni volta
- ✅ **Rollback veloce**: Versioni precedenti facilmente ripristinabili
- ✅ **Collaboration**: Tutti i team seguono stesso workflow

---

## Pipeline CI/CD Tipica

```
┌──────────┐     ┌─────────┐    ┌──────────┐     ┌─────────┐    ┌─────────┐
│   Code   │───▶│  Build  │───▶│   Test   │───▶│  Stage  │───▶│  Deploy │
│  Commit  │     │         │    │          │     │         │    │  Prod   │
└──────────┘     └─────────┘    └──────────┘     └─────────┘    └─────────┘
     │               │              │                │              │
     │          Compile         Unit Tests      Integration    Produzione
     │          Package         E2E Tests        Approval
     │          Lint            Security Scan    
```

### Fasi Tipiche

1. **Source**: Trigger da Git (push, PR, merge)
2. **Build**: Compilazione, packaging (Docker image, JAR, etc.)
3. **Test**: Unit, integration, e2e, security, performance
4. **Artifact Storage**: Registry (Docker Hub, ECR, Artifactory)
5. **Deploy to Staging**: Ambiente simile a produzione
6. **Approval** (opzionale): Manual gate per production
7. **Deploy to Production**: Blue/green, canary, rolling update
8. **Monitor**: Health checks, metrics, logs

---

## GitHub Actions

**GitHub Actions** è il servizio CI/CD nativo di GitHub, basato su workflow YAML.

### Workflow Base

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  NODE_VERSION: '18'

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Lint code
        run: npm run lint
      
      - name: Run tests
        run: npm test -- --coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/lcov.info
      
      - name: Build application
        run: npm run build
      
      - name: Upload build artifacts
        uses: actions/upload-artifact@v3
        with:
          name: build-output
          path: dist/
          retention-days: 7

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Snyk security scan
        uses: snyk/actions/node@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
```

### Deploy su AWS con GitHub Actions

```yaml
# .github/workflows/deploy-aws.yml
name: Deploy to AWS

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: eu-west-1
      
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1
      
      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: my-app
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
      
      - name: Deploy to ECS
        run: |
          aws ecs update-service \
            --cluster my-cluster \
            --service my-service \
            --force-new-deployment
      
      - name: Wait for deployment
        run: |
          aws ecs wait services-stable \
            --cluster my-cluster \
            --services my-service

  notify:
    needs: deploy
    runs-on: ubuntu-latest
    if: always()
    steps:
      - name: Send Slack notification
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: 'Deploy completed!'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Matrix Strategy (Multi-version Testing)

```yaml
name: Multi-version Tests

on: [push]

jobs:
  test:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
        node-version: [16, 18, 20]
        exclude:
          - os: macos-latest
            node-version: 16
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js ${{ matrix.node-version }}
        uses: actions/setup-node@v3
        with:
          node-version: ${{ matrix.node-version }}
      
      - run: npm ci
      - run: npm test
```

---

## GitLab CI/CD

**GitLab CI/CD** usa `.gitlab-ci.yml` per definire pipeline.

### Pipeline GitLab Completa

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - security
  - deploy-staging
  - deploy-production

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"

# Template per deploy
.deploy_template: &deploy_template
  image: alpine:latest
  before_script:
    - apk add --no-cache curl
  script:
    - echo "Deploying to $ENVIRONMENT"
    - curl -X POST $DEPLOY_WEBHOOK

build:
  stage: build
  image: node:18
  cache:
    paths:
      - node_modules/
  script:
    - npm ci
    - npm run build
  artifacts:
    paths:
      - dist/
    expire_in: 1 week
  only:
    - main
    - develop
    - merge_requests

lint:
  stage: build
  image: node:18
  cache:
    paths:
      - node_modules/
  script:
    - npm ci
    - npm run lint
    - npm run format:check

unit-tests:
  stage: test
  image: node:18
  services:
    - postgres:14
    - redis:7
  variables:
    POSTGRES_DB: testdb
    POSTGRES_USER: testuser
    POSTGRES_PASSWORD: testpass
  script:
    - npm ci
    - npm run test:unit -- --coverage
  coverage: '/Lines\s*:\s*(\d+\.\d+)%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: coverage/cobertura-coverage.xml

integration-tests:
  stage: test
  image: node:18
  script:
    - npm ci
    - npm run test:integration
  only:
    - main
    - merge_requests

e2e-tests:
  stage: test
  image: cypress/browsers:node18.12.0-chrome107
  script:
    - npm ci
    - npm run test:e2e
  artifacts:
    when: always
    paths:
      - cypress/videos/
      - cypress/screenshots/
    expire_in: 7 days

security-scan:
  stage: security
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t myapp:$CI_COMMIT_SHA .
    - docker run --rm aquasec/trivy image myapp:$CI_COMMIT_SHA
  allow_failure: false

sast:
  stage: security
  image: returntocorp/semgrep
  script:
    - semgrep --config=auto --json --output=sast-report.json .
  artifacts:
    reports:
      sast: sast-report.json

deploy-staging:
  <<: *deploy_template
  stage: deploy-staging
  environment:
    name: staging
    url: https://staging.example.com
  variables:
    ENVIRONMENT: staging
  only:
    - develop

deploy-production:
  <<: *deploy_template
  stage: deploy-production
  environment:
    name: production
    url: https://example.com
  variables:
    ENVIRONMENT: production
  when: manual  # Manual approval
  only:
    - main
```

### GitLab + Kubernetes Deploy

```yaml
deploy-k8s:
  stage: deploy
  image: bitnami/kubectl:latest
  before_script:
    - kubectl config set-cluster k8s --server="$K8S_SERVER"
    - kubectl config set-credentials gitlab --token="$K8S_TOKEN"
    - kubectl config set-context default --cluster=k8s --user=gitlab
    - kubectl config use-context default
  script:
    - kubectl set image deployment/myapp myapp=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA -n production
    - kubectl rollout status deployment/myapp -n production
  environment:
    name: production
    kubernetes:
      namespace: production
  only:
    - main
```

---

## Azure DevOps Pipelines

**Azure Pipelines** supporta YAML e UI classica.

### Azure Pipeline: Build e Deploy

```yaml
# azure-pipelines.yml
trigger:
  branches:
    include:
      - main
      - develop

pool:
  vmImage: 'ubuntu-latest'

variables:
  buildConfiguration: 'Release'
  azureSubscription: 'MyAzureConnection'
  appName: 'mywebapp'

stages:
  - stage: Build
    displayName: 'Build and Test'
    jobs:
      - job: BuildJob
        steps:
          - task: UseDotNet@2
            displayName: 'Use .NET 7'
            inputs:
              version: '7.x'
          
          - task: DotNetCoreCLI@2
            displayName: 'Restore packages'
            inputs:
              command: 'restore'
              projects: '**/*.csproj'
          
          - task: DotNetCoreCLI@2
            displayName: 'Build solution'
            inputs:
              command: 'build'
              projects: '**/*.csproj'
              arguments: '--configuration $(buildConfiguration)'
          
          - task: DotNetCoreCLI@2
            displayName: 'Run tests'
            inputs:
              command: 'test'
              projects: '**/*Tests.csproj'
              arguments: '--configuration $(buildConfiguration) --collect:"XPlat Code Coverage"'
          
          - task: PublishCodeCoverageResults@1
            displayName: 'Publish coverage'
            inputs:
              codeCoverageTool: 'Cobertura'
              summaryFileLocation: '$(Agent.TempDirectory)/**/coverage.cobertura.xml'
          
          - task: DotNetCoreCLI@2
            displayName: 'Publish app'
            inputs:
              command: 'publish'
              publishWebProjects: true
              arguments: '--configuration $(buildConfiguration) --output $(Build.ArtifactStagingDirectory)'
          
          - task: PublishBuildArtifacts@1
            displayName: 'Publish artifacts'
            inputs:
              PathtoPublish: '$(Build.ArtifactStagingDirectory)'
              ArtifactName: 'drop'

  - stage: DeployStaging
    displayName: 'Deploy to Staging'
    dependsOn: Build
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/develop'))
    jobs:
      - deployment: DeployStaging
        environment: 'staging'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  displayName: 'Deploy to Azure App Service'
                  inputs:
                    azureSubscription: '$(azureSubscription)'
                    appType: 'webAppLinux'
                    appName: '$(appName)-staging'
                    package: '$(Pipeline.Workspace)/drop/**/*.zip'

  - stage: DeployProduction
    displayName: 'Deploy to Production'
    dependsOn: DeployStaging
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/main'))
    jobs:
      - deployment: DeployProduction
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  displayName: 'Deploy to Azure App Service'
                  inputs:
                    azureSubscription: '$(azureSubscription)'
                    appType: 'webAppLinux'
                    appName: '$(appName)'
                    package: '$(Pipeline.Workspace)/drop/**/*.zip'
                    deploymentMethod: 'zipDeploy'
```

### Multi-stage con Approvals

```yaml
stages:
  - stage: Build
    jobs:
      - job: BuildApp
        steps:
          - script: echo "Building..."

  - stage: DeployDev
    dependsOn: Build
    jobs:
      - deployment: DeployDevJob
        environment: development  # Auto-deploy
        strategy:
          runOnce:
            deploy:
              steps:
                - script: echo "Deploy to dev"

  - stage: DeployProd
    dependsOn: DeployDev
    jobs:
      - deployment: DeployProdJob
        environment: production  # Requires manual approval in UI
        strategy:
          runOnce:
            deploy:
              steps:
                - script: echo "Deploy to production"
```

---

## AWS CodePipeline + CodeBuild

### CodeBuild: buildspec.yml

```yaml
# buildspec.yml
version: 0.2

phases:
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
      - REPOSITORY_URI=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME
      - COMMIT_HASH=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)
      - IMAGE_TAG=${COMMIT_HASH:=latest}
  
  build:
    commands:
      - echo Build started on `date`
      - echo Running tests...
      - npm ci
      - npm test
      - echo Building Docker image...
      - docker build -t $REPOSITORY_URI:latest .
      - docker tag $REPOSITORY_URI:latest $REPOSITORY_URI:$IMAGE_TAG
  
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing Docker images...
      - docker push $REPOSITORY_URI:latest
      - docker push $REPOSITORY_URI:$IMAGE_TAG
      - echo Writing image definitions file...
      - printf '[{"name":"my-container","imageUri":"%s"}]' $REPOSITORY_URI:$IMAGE_TAG > imagedefinitions.json

artifacts:
  files:
    - imagedefinitions.json
    - appspec.yml
    - taskdef.json

cache:
  paths:
    - '/root/.npm/**/*'
    - 'node_modules/**/*'
```

### CodePipeline con Terraform

```hcl
# CodePipeline resource
resource "aws_codepipeline" "pipeline" {
  name     = "my-pipeline"
  role_arn = aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.artifacts.bucket
    type     = "S3"
  }

  stage {
    name = "Source"

    action {
      name             = "Source"
      category         = "Source"
      owner            = "AWS"
      provider         = "CodeStarSourceConnection"
      version          = "1"
      output_artifacts = ["source_output"]

      configuration = {
        ConnectionArn    = aws_codestarconnections_connection.github.arn
        FullRepositoryId = "myorg/myrepo"
        BranchName       = "main"
      }
    }
  }

  stage {
    name = "Build"

    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["source_output"]
      output_artifacts = ["build_output"]

      configuration = {
        ProjectName = aws_codebuild_project.build.name
      }
    }
  }

  stage {
    name = "Deploy"

    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "ECS"
      version         = "1"
      input_artifacts = ["build_output"]

      configuration = {
        ClusterName = aws_ecs_cluster.main.name
        ServiceName = aws_ecs_service.app.name
        FileName    = "imagedefinitions.json"
      }
    }
  }
}
```

---

## Deployment Strategies

### 1. Blue/Green Deployment

**Due ambienti identici**: Blue (attuale) e Green (nuovo).

```
┌─────────────┐
│ Load        │
│ Balancer    │
└──────┬──────┘
       │
   ┌───┴────┐
   │        │
┌──▼──┐  ┌──▼───┐
│Blue │  │Green │
│ V1  │  │  V2  │  ← Deploy nuova versione
└─────┘  └──────┘
              │
         Test, verify
              │
         Switch traffic ──────┐
                               │
┌─────────────┐                │
│ Load        │◀───────────────┘
│ Balancer    │
└──────┬──────┘
       │
   ┌───┴────┐
   │        │
┌──▼──┐  ┌──▼───┐
│Blue │  │Green │
│ V1  │  │  V2  │ ← Production
└─────┘  └──────┘
```

**AWS ECS Blue/Green con CodeDeploy**:

```yaml
# appspec.yml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:region:account:task-definition/my-task:2"
        LoadBalancerInfo:
          ContainerName: "my-container"
          ContainerPort: 80
        PlatformVersion: "LATEST"

Hooks:
  - BeforeInstall: "LambdaFunctionToValidateBeforeInstall"
  - AfterInstall: "LambdaFunctionToValidateAfterTrafficShift"
  - AfterAllowTestTraffic: "LambdaFunctionToValidateAfterTestTrafficStarts"
  - BeforeAllowTraffic: "LambdaFunctionToValidateBeforeAllowingProductionTraffic"
  - AfterAllowTraffic: "LambdaFunctionToValidateAfterAllowingProductionTraffic"
```

### 2. Canary Deployment

**Deploy graduale** a percentuale di utenti crescente.

```
Step 1: 10% traffic → V2
┌────────────┐
│ 90%  │ 10% │
│  V1  │ V2  │
└────────────┘

Step 2: 50% traffic → V2
┌────────────┐
│ 50% │ 50% │
│ V1  │ V2  │
└────────────┘

Step 3: 100% traffic → V2
┌────────────┐
│    100%    │
│     V2     │
└────────────┘
```

**GitLab Canary con Kubernetes**:

```yaml
deploy-canary:
  stage: deploy
  script:
    - kubectl apply -f k8s/canary-deployment.yml
    - kubectl patch deployment myapp-canary -p '{"spec":{"replicas":1}}'  # 10% traffic
  environment:
    name: production-canary

deploy-production:
  stage: deploy
  when: manual
  script:
    - kubectl apply -f k8s/production-deployment.yml
    - kubectl scale deployment myapp-canary --replicas=0  # Remove canary
  environment:
    name: production
```

### 3. Rolling Update

**Aggiornamento progressivo** delle istanze.

```yaml
# Kubernetes rolling update
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 10
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1  # Max 1 pod down
      maxSurge: 2        # Max 2 pods extra durante update
  template:
    spec:
      containers:
        - name: myapp
          image: myapp:v2
```

### 4. Feature Flags

**Deploy codice disabilitato**, attiva con flag.

```typescript
// Using LaunchDarkly or similar
import { LDClient } from 'launchdarkly-node-server-sdk';

const client = LDClient.init(process.env.LD_SDK_KEY);

app.get('/api/feature', async (req, res) => {
  const user = { key: req.user.id };
  const showNewFeature = await client.variation('new-feature', user, false);
  
  if (showNewFeature) {
    // New feature code
    res.json({ feature: 'new' });
  } else {
    // Old code
    res.json({ feature: 'old' });
  }
});
```

---

## Best Practices CI/CD

### 1. Pipeline as Code

```yaml
# Versiona le pipeline in Git insieme al codice
# Ogni branch può avere pipeline diverse
```

### 2. Fail Fast

```yaml
stages:
  - lint       # Fast, cheap
  - unit-test  # Fast
  - build      # Slower
  - e2e-test   # Slowest, expensive
```

### 3. Parallelizzazione

```yaml
# GitLab
test:
  parallel:
    matrix:
      - SERVICE: [api, frontend, worker]
  script:
    - npm run test:$SERVICE
```

### 4. Caching Intelligente

```yaml
# GitHub Actions
- uses: actions/cache@v3
  with:
    path: ~/.npm
    key: ${{ runner.os }}-node-${{ hashFiles('**/package-lock.json') }}
    restore-keys: |
      ${{ runner.os }}-node-
```

### 5. Secrets Management

```bash
# MAI committare secrets!
# Usa secret managers:
# - GitHub Secrets
# - GitLab CI/CD Variables
# - Azure Key Vault
# - AWS Secrets Manager

# In pipeline
${{ secrets.API_KEY }}  # GitHub
$CI_SECRET_VAR          # GitLab
$(secretName)           # Azure
```

### 6. Notifications

```yaml
# Slack notification on failure
- name: Notify failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 7. Artifact Management

```yaml
# Conserva build artifacts per debug
artifacts:
  paths:
    - dist/
    - logs/
  expire_in: 30 days
  when: always  # Anche se job fallisce
```

---

## Esercizi

1. **GitHub Actions Full Pipeline**: Build, test, Docker build, deploy su AWS ECS
2. **GitLab Multi-environment**: Dev, staging, production con approval manuale
3. **Azure DevOps**: .NET app con deploy su Azure App Service
4. **Blue/Green Deploy**: Implementa su AWS con CodeDeploy
5. **Canary Release**: Deploy graduale su Kubernetes con Istio
6. **Pipeline Monitoring**: Integra metriche pipeline in Grafana

---

## Domande di Verifica

1. Qual è la differenza tra Continuous Delivery e Continuous Deployment?
2. Come implementi un rollback rapido in una pipeline CI/CD?
3. Quali sono i vantaggi del Blue/Green deployment?
4. Quando useresti Canary deployment invece di Rolling update?
5. Come gestisci secrets in modo sicuro nelle pipeline?
6. Cosa sono gli artifacts in una pipeline e perché sono utili?
7. Come testi una pipeline prima di mergiare in main?
8. Qual è lo scopo degli approval gates nelle pipeline?
9. Come ottimizzi i tempi di esecuzione di una pipeline?
10. Quali metriche monitora una pipeline CI/CD efficace?

---

## Risorse Aggiuntive

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [Azure DevOps Pipelines](https://learn.microsoft.com/en-us/azure/devops/pipelines/)
- [AWS CodePipeline](https://aws.amazon.com/codepipeline/)
- [Continuous Delivery Book - Jez Humble](https://continuousdelivery.com/)
- [The DevOps Handbook](https://itrevolution.com/product/the-devops-handbook/)
