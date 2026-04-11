# Machine Learning nel Cloud

## Introduzione al ML nel Cloud

Il **Cloud** democratizza il Machine Learning fornendo:
- **Compute scalabile**: GPU/TPU on-demand
- **Servizi gestiti**: Training e deployment senza infrastruttura
- **AutoML**: ML accessibile senza expertise avanzato
- **Pre-trained models**: Vision, NLP, Speech pronti all'uso

---

## AWS Machine Learning Services

### Amazon SageMaker

**SageMaker** è la piattaforma ML completa di AWS.

```python
# Training con SageMaker
import sagemaker
from sagemaker.sklearn import SKLearn

# Session
sagemaker_session = sagemaker.Session()
role = 'arn:aws:iam::123456789012:role/SageMakerRole'

# Training script
sklearn_estimator = SKLearn(
    entry_point='train.py',
    role=role,
    instance_type='ml.m5.xlarge',
    framework_version='1.0-1',
    py_version='py3',
    hyperparameters={
        'max_depth': 5,
        'n_estimators': 100
    }
)

# Train
sklearn_estimator.fit({'training': 's3://bucket/training-data/'})

# Deploy
predictor = sklearn_estimator.deploy(
    initial_instance_count=1,
    instance_type='ml.t2.medium'
)

# Predict
result = predictor.predict([[5.1, 3.5, 1.4, 0.2]])
print(result)
```

### SageMaker Training Script

```python
# train.py
import argparse
import joblib
import pandas as pd
from sklearn.ensemble import RandomForestClassifier

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--max-depth', type=int, default=5)
    parser.add_argument('--n-estimators', type=int, default=100)
    parser.add_argument('--model-dir', type=str, default='/opt/ml/model')
    parser.add_argument('--train', type=str, default='/opt/ml/input/data/training')
    
    args = parser.parse_args()
    
    # Load data
    train_df = pd.read_csv(f'{args.train}/train.csv')
    X_train = train_df.drop('target', axis=1)
    y_train = train_df['target']
    
    # Train model
    model = RandomForestClassifier(
        max_depth=args.max_depth,
        n_estimators=args.n_estimators,
        random_state=42
    )
    model.fit(X_train, y_train)
    
    # Save model
    joblib.dump(model, f'{args.model_dir}/model.joblib')
    print('Model trained and saved')
```

### SageMaker Autopilot (AutoML)

```python
# AutoML automatico
from sagemaker import AutoML

automl = AutoML(
    role=role,
    target_attribute_name='target',
    max_candidates=10,
    max_runtime_per_training_job_in_seconds=3600
)

# Lancia AutoML
automl.fit(
    inputs='s3://bucket/training-data/train.csv',
    job_name='autopilot-job-1'
)

# Deploy miglior modello
predictor = automl.deploy(
    initial_instance_count=1,
    instance_type='ml.m5.large'
)
```

### SageMaker Pipelines (MLOps)

```python
from sagemaker.workflow.pipeline import Pipeline
from sagemaker.workflow.steps import ProcessingStep, TrainingStep
from sagemaker.workflow.parameters import ParameterInteger

# Parameters
max_depth = ParameterInteger(name='MaxDepth', default_value=5)

# Processing step
processing_step = ProcessingStep(
    name='PreprocessData',
    processor=sklearn_processor,
    inputs=[...],
    outputs=[...],
    code='preprocess.py'
)

# Training step
training_step = TrainingStep(
    name='TrainModel',
    estimator=sklearn_estimator,
    inputs={
        'training': processing_step.properties.ProcessingOutputConfig.Outputs['train'].S3Output.S3Uri
    }
)

# Pipeline
pipeline = Pipeline(
    name='ml-pipeline',
    parameters=[max_depth],
    steps=[processing_step, training_step]
)

pipeline.upsert(role_arn=role)
execution = pipeline.start()
```

### Amazon Rekognition (Computer Vision)

```python
import boto3

rekognition = boto3.client('rekognition')

# Detect labels (objects)
response = rekognition.detect_labels(
    Image={'S3Object': {'Bucket': 'my-bucket', 'Name': 'image.jpg'}},
    MaxLabels=10,
    MinConfidence=90
)

for label in response['Labels']:
    print(f"{label['Name']}: {label['Confidence']:.2f}%")

# Face detection
response = rekognition.detect_faces(
    Image={'S3Object': {'Bucket': 'my-bucket', 'Name': 'face.jpg'}},
    Attributes=['ALL']
)

for face in response['FaceDetails']:
    print(f"Age: {face['AgeRange']['Low']}-{face['AgeRange']['High']}")
    print(f"Gender: {face['Gender']['Value']} ({face['Gender']['Confidence']:.1f}%)")
    print(f"Emotions: {face['Emotions'][0]['Type']}")

# Text detection (OCR)
response = rekognition.detect_text(
    Image={'S3Object': {'Bucket': 'my-bucket', 'Name': 'document.jpg'}}
)

for text in response['TextDetections']:
    if text['Type'] == 'LINE':
        print(text['DetectedText'])
```

### Amazon Comprehend (NLP)

```python
import boto3

comprehend = boto3.client('comprehend')

# Sentiment analysis
text = "I love this product! It's amazing and works perfectly."
response = comprehend.detect_sentiment(
    Text=text,
    LanguageCode='en'
)

print(f"Sentiment: {response['Sentiment']}")  # POSITIVE
print(f"Scores: {response['SentimentScore']}")

# Entity detection
response = comprehend.detect_entities(
    Text="Amazon Web Services is based in Seattle, Washington.",
    LanguageCode='en'
)

for entity in response['Entities']:
    print(f"{entity['Type']}: {entity['Text']} ({entity['Score']:.2f})")

# Key phrases
response = comprehend.detect_key_phrases(
    Text="The cloud computing market is growing rapidly.",
    LanguageCode='en'
)

for phrase in response['KeyPhrases']:
    print(phrase['Text'])
```

---

## Azure Machine Learning

### Azure ML Workspace

```python
from azureml.core import Workspace, Experiment, ScriptRunConfig, Environment
from azureml.core.compute import ComputeTarget, AmlCompute

# Connect to workspace
ws = Workspace.from_config()

# Create compute cluster
compute_config = AmlCompute.provisioning_configuration(
    vm_size='STANDARD_D2_V2',
    max_nodes=4
)

compute_target = ComputeTarget.create(ws, 'cpu-cluster', compute_config)
compute_target.wait_for_completion(show_output=True)

# Environment
env = Environment.from_conda_specification(
    name='training-env',
    file_path='environment.yml'
)

# Training script config
config = ScriptRunConfig(
    source_directory='./src',
    script='train.py',
    compute_target=compute_target,
    environment=env,
    arguments=[
        '--learning-rate', 0.01,
        '--epochs', 10
    ]
)

# Run experiment
experiment = Experiment(workspace=ws, name='classification-experiment')
run = experiment.submit(config)
run.wait_for_completion(show_output=True)
```

### Azure ML Training Script

```python
# train.py
import argparse
from azureml.core import Run
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.metrics import accuracy_score
import joblib

# Get run context
run = Run.get_context()

# Parse arguments
parser = argparse.ArgumentParser()
parser.add_argument('--learning-rate', type=float, default=0.1)
parser.add_argument('--epochs', type=int, default=100)
args = parser.parse_args()

# Load data (from datastore)
train_df = run.input_datasets['training'].to_pandas_dataframe()
X_train = train_df.drop('target', axis=1)
y_train = train_df['target']

# Train
model = GradientBoostingClassifier(
    learning_rate=args.learning_rate,
    n_estimators=args.epochs
)
model.fit(X_train, y_train)

# Evaluate
accuracy = accuracy_score(y_train, model.predict(X_train))

# Log metrics
run.log('accuracy', accuracy)
run.log('learning_rate', args.learning_rate)

# Save model
joblib.dump(model, 'outputs/model.pkl')
run.upload_file('model.pkl', 'outputs/model.pkl')

run.complete()
```

### Azure Cognitive Services

```python
from azure.cognitiveservices.vision.computervision import ComputerVisionClient
from msrest.authentication import CognitiveServicesCredentials

# Computer Vision
credentials = CognitiveServicesCredentials(subscription_key)
cv_client = ComputerVisionClient(endpoint, credentials)

# Analyze image
image_url = "https://example.com/image.jpg"
analysis = cv_client.analyze_image(
    image_url,
    visual_features=['categories', 'description', 'objects', 'tags']
)

print(f"Description: {analysis.description.captions[0].text}")
for tag in analysis.tags:
    print(f"Tag: {tag.name} ({tag.confidence:.2f})")

# OCR
read_response = cv_client.read(image_url, raw=True)
operation_location = read_response.headers['Operation-Location']
operation_id = operation_location.split('/')[-1]

# Wait for completion
import time
while True:
    result = cv_client.get_read_result(operation_id)
    if result.status not in ['notStarted', 'running']:
        break
    time.sleep(1)

# Extract text
for page in result.analyze_result.read_results:
    for line in page.lines:
        print(line.text)
```

---

## Google Cloud AI/ML

### Vertex AI

```python
from google.cloud import aiplatform

# Initialize
aiplatform.init(project='my-project', location='us-central1')

# Create dataset
dataset = aiplatform.TabularDataset.create(
    display_name='my-dataset',
    gcs_source=['gs://my-bucket/data.csv']
)

# AutoML training
job = aiplatform.AutoMLTabularTrainingJob(
    display_name='automl-job',
    optimization_prediction_type='classification',
    optimization_objective='maximize-au-prc'
)

model = job.run(
    dataset=dataset,
    target_column='label',
    training_fraction_split=0.8,
    validation_fraction_split=0.1,
    test_fraction_split=0.1
)

# Deploy
endpoint = model.deploy(
    deployed_model_display_name='my-model',
    machine_type='n1-standard-4',
    min_replica_count=1,
    max_replica_count=5
)

# Predict
instances = [{'feature1': 1.0, 'feature2': 2.0}]
prediction = endpoint.predict(instances=instances)
print(prediction.predictions)
```

### Custom Training on Vertex AI

```python
# training_script.py (containerized)
import tensorflow as tf
from tensorflow import keras

# Load data
(x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()
x_train = x_train.reshape(-1, 28, 28, 1).astype('float32') / 255

# Build model
model = keras.Sequential([
    keras.layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1)),
    keras.layers.MaxPooling2D((2, 2)),
    keras.layers.Flatten(),
    keras.layers.Dense(128, activation='relu'),
    keras.layers.Dense(10, activation='softmax')
])

model.compile(
    optimizer='adam',
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

# Train
model.fit(x_train, y_train, epochs=5, validation_split=0.2)

# Save
model.save('/gcs/my-bucket/model')
```

```python
# Submit custom training job
from google.cloud import aiplatform

job = aiplatform.CustomTrainingJob(
    display_name='custom-mnist-training',
    container_uri='gcr.io/my-project/trainer:latest',
    model_serving_container_image_uri='gcr.io/my-project/predictor:latest'
)

model = job.run(
    model_display_name='mnist-model',
    replica_count=1,
    machine_type='n1-standard-4',
    accelerator_type='NVIDIA_TESLA_K80',
    accelerator_count=1
)
```

### Google Cloud Vision API

```python
from google.cloud import vision

client = vision.ImageAnnotatorClient()

# Load image
with open('image.jpg', 'rb') as image_file:
    content = image_file.read()

image = vision.Image(content=content)

# Label detection
response = client.label_detection(image=image)
for label in response.label_annotations:
    print(f"{label.description}: {label.score:.2f}")

# Text detection (OCR)
response = client.text_detection(image=image)
for text in response.text_annotations:
    print(text.description)

# Face detection
response = client.face_detection(image=image)
for face in response.face_annotations:
    print(f"Joy: {face.joy_likelihood}")
    print(f"Anger: {face.anger_likelihood}")
```

---

## MLOps Best Practices

### 1. Experiment Tracking

```python
# MLflow
import mlflow

mlflow.set_experiment('classification-experiment')

with mlflow.start_run():
    # Log parameters
    mlflow.log_param('learning_rate', 0.01)
    mlflow.log_param('epochs', 100)
    
    # Train model
    model.fit(X_train, y_train)
    
    # Log metrics
    accuracy = model.score(X_test, y_test)
    mlflow.log_metric('accuracy', accuracy)
    
    # Log model
    mlflow.sklearn.log_model(model, 'model')
    
    # Log artifacts
    mlflow.log_artifact('feature_importance.png')
```

### 2. Model Versioning

```python
# SageMaker Model Registry
from sagemaker.model import Model

model = Model(
    image_uri=training_job.image_uri,
    model_data=training_job.model_data,
    role=role
)

# Register model
model_package = model.register(
    content_types=['application/json'],
    response_types=['application/json'],
    inference_instances=['ml.t2.medium'],
    transform_instances=['ml.m5.large'],
    model_package_group_name='my-model-group',
    approval_status='PendingManualApproval',
    description='Random Forest Classifier v1.2'
)
```

### 3. A/B Testing

```python
# SageMaker Multi-Model Endpoint
from sagemaker.multidatamodel import MultiDataModel

mdm = MultiDataModel(
    name='ab-testing-endpoint',
    model_data_prefix='s3://bucket/models/',
    image_uri=container_image
)

predictor = mdm.deploy(
    initial_instance_count=2,
    instance_type='ml.m5.large',
    endpoint_name='ab-test-endpoint'
)

# Route traffic
response = predictor.predict(data, target_model='model-v1.tar.gz')
```

### 4. Model Monitoring

```python
# SageMaker Model Monitor
from sagemaker.model_monitor import DefaultModelMonitor
from sagemaker.model_monitor.dataset_format import DatasetFormat

monitor = DefaultModelMonitor(
    role=role,
    instance_count=1,
    instance_type='ml.m5.xlarge',
    max_runtime_in_seconds=3600
)

# Create baseline
monitor.suggest_baseline(
    baseline_dataset='s3://bucket/training-data/baseline.csv',
    dataset_format=DatasetFormat.csv(header=True),
    output_s3_uri='s3://bucket/baseline-results'
)

# Schedule monitoring
monitor.create_monitoring_schedule(
    monitor_schedule_name='daily-monitoring',
    endpoint_input=predictor.endpoint_name,
    output_s3_uri='s3://bucket/monitoring-results',
    statistics=monitor.baseline_statistics(),
    constraints=monitor.suggested_constraints(),
    schedule_cron_expression='cron(0 0 * * ? *)'  # Daily
)
```

---

## Deep Learning nel Cloud

### TensorFlow su Cloud

```python
import tensorflow as tf
from tensorflow import keras

# Distributed training strategy
strategy = tf.distribute.MirroredStrategy()

with strategy.scope():
    # Model
    model = keras.Sequential([
        keras.layers.Dense(512, activation='relu', input_shape=(784,)),
        keras.layers.Dropout(0.2),
        keras.layers.Dense(10, activation='softmax')
    ])
    
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

# Train
model.fit(
    train_dataset,
    epochs=10,
    validation_data=val_dataset,
    callbacks=[
        keras.callbacks.ModelCheckpoint('s3://bucket/checkpoints/'),
        keras.callbacks.TensorBoard(log_dir='s3://bucket/logs/')
    ]
)
```

### PyTorch su Cloud

```python
import torch
import torch.nn as nn
from torch.utils.data import DataLoader

# Model
class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.fc1 = nn.Linear(784, 512)
        self.fc2 = nn.Linear(512, 10)
        self.dropout = nn.Dropout(0.2)
    
    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = self.dropout(x)
        x = self.fc2(x)
        return x

# Training
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model = Net().to(device)
optimizer = torch.optim.Adam(model.parameters())
criterion = nn.CrossEntropyLoss()

for epoch in range(10):
    for batch in train_loader:
        data, target = batch
        data, target = data.to(device), target.to(device)
        
        optimizer.zero_grad()
        output = model(data)
        loss = criterion(output, target)
        loss.backward()
        optimizer.step()
    
    print(f'Epoch {epoch}, Loss: {loss.item()}')

# Save to S3
torch.save(model.state_dict(), 's3://bucket/model.pth')
```

---

## Esercizi

1. **Image Classification**: SageMaker + ResNet su dataset custom
2. **Sentiment Analysis**: Comprehend/Text Analytics su reviews
3. **Object Detection**: Rekognition/Vision API per inventory
4. **AutoML**: Vertex AI AutoML per tabular data
5. **MLOps Pipeline**: CI/CD per model deployment
6. **Real-time Inference**: Lambda + SageMaker endpoint

---

## Domande di Verifica

1. Qual è la differenza tra SageMaker e servizi pre-trained?
2. Come funziona AutoML?
3. Cosa sono MLOps e perché sono importanti?
4. Come implementi A/B testing per modelli ML?
5. Qual è la differenza tra batch e real-time inference?
6. Come monitori model drift?
7. Quando usi GPU vs TPU vs CPU?
8. Come gestisci experiment tracking?
9. Qual è il vantaggio di Vertex AI Pipelines?
10. Come ottimizzi i costi di ML training?

---

## Risorse Aggiuntive

- [AWS SageMaker Documentation](https://docs.aws.amazon.com/sagemaker/)
- [Azure ML Documentation](https://learn.microsoft.com/azure/machine-learning/)
- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [MLOps: Continuous Delivery for ML](https://martinfowler.com/articles/cd4ml.html)
- [TensorFlow Documentation](https://www.tensorflow.org/)
- [PyTorch Documentation](https://pytorch.org/docs/)
