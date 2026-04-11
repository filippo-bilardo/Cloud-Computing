# Tecnologie Emergenti nel Cloud

## Edge Computing

### Definizione
Elaborazione dati vicino alla sorgente anziché in datacenter centralizzati.

### AWS Services
- **CloudFront Functions**: JavaScript lightweight (< 1ms)
- **Lambda@Edge**: Node.js/Python su edge locations
- **AWS Wavelength**: 5G edge computing
- **AWS Outposts**: Hardware AWS on-premises

```javascript
// CloudFront Function example
function handler(event) {
  var request = event.request;
  var uri = request.uri;
  
  // Add index.html to requests ending in /
  if (uri.endsWith('/')) {
    request.uri += 'index.html';
  }
  
  return request;
}
```

### Azure Edge
- **Azure IoT Edge**: ML models on edge devices
- **Azure Stack Edge**: AI-enabled edge appliance

### Use Cases
- IoT data processing
- Real-time video analytics
- Gaming (low latency)
- Autonomous vehicles

## Quantum Computing

### Cloud Quantum Services
- **AWS Braket**: Quantum computing service
- **Azure Quantum**: Quantum algorithms development
- **Google Quantum AI**: Quantum processors

```python
# AWS Braket example
from braket.circuits import Circuit
from braket.aws import AwsDevice

# Create Bell state
bell = Circuit().h(0).cnot(0, 1)

# Run on quantum device
device = AwsDevice("arn:aws:braket:::device/quantum-simulator/amazon/sv1")
task = device.run(bell, shots=1000)

result = task.result()
print(result.measurement_counts)
```

## AI/ML Advances

### AutoML Evolution
- **Automated feature engineering**
- **Neural architecture search (NAS)**
- **Hyperparameter optimization at scale**

### Responsible AI
- **Bias detection**: AWS SageMaker Clarify
- **Explainability**: SHAP, LIME
- **Model governance**: Audit trails, versioning

### Generative AI
- **Large Language Models**: GPT, Claude, Gemini
- **Image generation**: DALL-E, Stable Diffusion
- **Code generation**: GitHub Copilot

```python
# Bedrock (AWS Generative AI)
import boto3

bedrock = boto3.client('bedrock-runtime')

response = bedrock.invoke_model(
    modelId='anthropic.claude-v2',
    body=json.dumps({
        "prompt": "Explain quantum computing",
        "max_tokens_to_sample": 500
    })
)

print(response['body'].read())
```

## WebAssembly (Wasm)

### Serverless + Wasm
- **Faster cold starts**: < 1ms
- **Multi-language**: Rust, C++, Go compiled to Wasm
- **Portability**: Run anywhere

### Cloudflare Workers
```javascript
// Wasm on Cloudflare Workers
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})

async function handleRequest(request) {
  const wasm = await WebAssembly.instantiate(wasmModule);
  const result = wasm.instance.exports.compute(42);
  
  return new Response(`Result: ${result}`)
}
```

## Confidential Computing

### Trusted Execution Environments (TEE)
- **AWS Nitro Enclaves**: Isolated compute environments
- **Azure Confidential Computing**: Intel SGX
- **Google Confidential VMs**: AMD SEV

```bash
# AWS Nitro Enclave
nitro-cli build-enclave --docker-uri my-app:latest --output-file app.eif

nitro-cli run-enclave \
  --cpu-count 2 \
  --memory 4096 \
  --enclave-cif app.eif \
  --debug-mode
```

### Use Cases
- Healthcare data processing
- Financial transactions
- Sensitive ML model inference
- Multi-party computation

## Green Cloud / Sustainable Computing

### Carbon-Aware Computing
```python
# Example: Schedule workloads during low-carbon hours
from carbon_intensity_api import get_carbon_intensity

def should_run_job():
    intensity = get_carbon_intensity(region='eu-west-1')
    
    if intensity < 100:  # Low carbon intensity
        return True
    else:
        # Delay non-urgent jobs
        return False
```

### Sustainability Services
- **AWS Customer Carbon Footprint Tool**
- **Azure Sustainability Calculator**
- **Google Cloud Carbon Footprint**

### Best Practices
1. **Right-sizing**: No over-provisioning
2. **Spot instances**: Use spare capacity
3. **Regions**: Choose low-carbon regions
4. **Scheduling**: Run batch jobs during off-peak

## Service Mesh Evolution

### Istio, Linkerd
- **Zero-trust networking**
- **Observability built-in**
- **Traffic management**

```yaml
# Istio VirtualService
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          exact: jason
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

## eBPF in Cloud

### Observability & Security
- **Network monitoring** senza agents
- **Security policies** kernel-level
- **Performance tracing**

```c
// eBPF program example
SEC("kprobe/tcp_sendmsg")
int trace_tcp_sendmsg(struct pt_regs *ctx) {
    u64 pid = bpf_get_current_pid_tgid() >> 32;
    
    bpf_trace_printk("TCP send from PID: %d\\n", pid);
    return 0;
}
```

## Esercizi
1. Deploy Lambda@Edge function
2. Experiment with AWS Braket quantum simulator
3. Use Bedrock per generative AI
4. Setup Nitro Enclave per confidential computing
5. Analyze carbon footprint della tua infra

## Domande
1. Cos'è edge computing e perché è importante?
2. Come funziona quantum computing nel cloud?
3. Quali sono le sfide di Responsible AI?
4. Cos'è confidential computing?
5. Come riduci carbon footprint?
