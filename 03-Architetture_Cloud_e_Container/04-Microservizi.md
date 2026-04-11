# Architetture a Microservizi

## Introduzione ai Microservizi

I microservizi rappresentano un approccio architetturale per sviluppare applicazioni come insieme di servizi piccoli, autonomi e collaborativi.

### Cos'è un'Architettura a Microservizi?

Un'**architettura a microservizi** è:
- Un insieme di servizi piccoli e indipendenti
- Ogni servizio esegue un processo specifico
- Comunicazione tramite API ben definite
- Deploy, scaling e gestione indipendenti
- Organizzati attorno a capability di business

### Evoluzione Architetturale

```
Monolite → SOA → Microservizi → Serverless
```

**Timeline:**
- **2000s**: Service-Oriented Architecture (SOA)
- **2011**: Microservizi term coined
- **2014**: Netflix, Amazon adozione massiva
- **2015**: Docker + Kubernetes enablers
- **Oggi**: Standard per cloud-native apps

## Monolite vs Microservizi

### Architettura Monolitica

```
┌─────────────────────────────────────────┐
│         APPLICAZIONE MONOLITICA          │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │         User Interface              │ │
│  └────────────────────────────────────┘ │
│  ┌────────────────────────────────────┐ │
│  │       Business Logic               │ │
│  │  • Auth    • Orders    • Catalog   │ │
│  │  • Users   • Payment   • Shipping  │ │
│  └────────────────────────────────────┘ │
│  ┌────────────────────────────────────┐ │
│  │       Data Access Layer            │ │
│  └────────────────────────────────────┘ │
│                  │                       │
└──────────────────┼───────────────────────┘
                   │
         ┌─────────▼─────────┐
         │  Single Database   │
         └───────────────────┘
```

### Architettura a Microservizi

```
┌──────────────────────────────────────────────────────────┐
│                     API Gateway                           │
└───┬──────┬───────┬──────┬──────┬──────┬──────┬──────────┘
    │      │       │      │      │      │      │
┌───▼───┐┌─▼────┐┌─▼───┐┌─▼───┐┌─▼───┐┌─▼───┐┌─▼────┐
│ Auth  ││User  ││Order││Prod ││Pay  ││Ship ││Notif │
│Service││Svc   ││Svc  ││Svc  ││Svc  ││Svc  ││Svc   │
└───┬───┘└──┬───┘└──┬──┘└──┬──┘└──┬──┘└──┬──┘└──┬───┘
    │       │       │      │      │      │      │
┌───▼──┐┌──▼──┐┌──▼─┐┌──▼─┐┌──▼─┐┌──▼─┐┌──▼──┐
│AuthDB││User ││Ord ││Prod││Pay ││Ship││Notif│
│      ││DB   ││DB  ││DB  ││DB  ││DB  ││DB   │
└──────┘└─────┘└────┘└────┘└────┘└────┘└─────┘
```

### Confronto

| Aspetto | Monolite | Microservizi |
|---------|----------|--------------|
| **Deployment** | Tutto insieme | Indipendente |
| **Scalabilità** | Verticale | Orizzontale e granulare |
| **Tecnologie** | Stack unico | Polyglot |
| **Database** | Condiviso | Per servizio |
| **Complessità** | Bassa iniziale | Alta gestionale |
| **Team** | Unico grande | Piccoli autonomi |
| **Fault tolerance** | Limitata | Alta |
| **Deploy velocity** | Lenta | Rapida |

## Principi dei Microservizi

### 1. Single Responsibility

Ogni servizio ha una responsabilità chiara e definita.

```
✅ CORRETTO:
- UserService: Gestione utenti
- OrderService: Gestione ordini
- PaymentService: Gestione pagamenti

❌ SBAGLIATO:
- UserOrderPaymentService: Fa tutto
```

### 2. Autonomia

Ogni servizio deve essere indipendente e auto-contenuto.

- Proprio database
- Proprio deployment
- Proprie dipendenze
- Propria logica di business

### 3. Decentralizzazione

Decisioni distribuite, non centralizzate.

- **Data**: Database per servizio
- **Governance**: Team autonomi
- **Technology**: Polyglot programming

### 4. Failure Isolation

Il fallimento di un servizio non deve compromettere l'intero sistema.

```python
# Circuit Breaker Pattern
from circuitbreaker import circuit

@circuit(failure_threshold=5, recovery_timeout=60)
def call_external_service():
    response = requests.get('http://external-service/api')
    return response.json()
```

### 5. Continuous Evolution

Servizi evolvono indipendentemente.

- Versioning API
- Backward compatibility
- Canary deployments
- Blue-Green deployments

## Scomposizione in Microservizi

### Domain-Driven Design (DDD)

**Bounded Context**: Confine logico di un dominio.

```
E-commerce Domain:

┌─────────────────────┐  ┌─────────────────────┐
│  Identity & Access  │  │   Product Catalog   │
│                     │  │                     │
│  • User            │  │  • Product         │
│  • Authentication  │  │  • Category        │
│  • Authorization   │  │  • Inventory       │
└─────────────────────┘  └─────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐
│   Order Management  │  │   Payment           │
│                     │  │                     │
│  • Order           │  │  • Transaction     │
│  • Cart            │  │  • Invoice         │
│  • OrderItem       │  │  • Refund          │
└─────────────────────┘  └─────────────────────┘
```

### Strategie di Decomposizione

#### 1. By Business Capability

```
Organization Structure → Service Structure

Marketing Dept    → Marketing Service
Sales Dept        → Sales Service
Inventory Dept    → Inventory Service
```

#### 2. By Subdomain

```
Core Domain       → Core Services (alta priorità)
Supporting Domain → Supporting Services
Generic Domain    → Shared Services
```

#### 3. By Use Case

```
User Registration → UserRegistrationService
Order Processing  → OrderProcessingService
Payment           → PaymentService
```

### Anti-Pattern: Wrong Boundaries

```
❌ TROPPO GRANULARE:
- GetUserNameService
- GetUserEmailService
- GetUserPhoneService

✅ GIUSTO BILANCIAMENTO:
- UserService (gestisce tutti gli attributi utente)

❌ TROPPO GRANDE:
- EcommerceService (fa tutto)
```

## Comunicazione tra Microservizi

### 1. Sincrone: REST API

```python
# Flask REST API
from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/api/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    user = UserRepository.find_by_id(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404
    return jsonify(user.to_dict())

@app.route('/api/users', methods=['POST'])
def create_user():
    data = request.get_json()
    user = UserService.create_user(data)
    return jsonify(user.to_dict()), 201
```

**Chiamata tra servizi:**

```python
# Order Service chiama User Service
import requests

def get_user_details(user_id):
    try:
        response = requests.get(
            f'http://user-service:8080/api/users/{user_id}',
            timeout=5
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        logger.error(f"Error calling user service: {e}")
        raise ServiceUnavailableError("User service unavailable")
```

### 2. Sincrone: gRPC

```protobuf
// user.proto
syntax = "proto3";

package user;

service UserService {
  rpc GetUser (GetUserRequest) returns (UserResponse);
  rpc CreateUser (CreateUserRequest) returns (UserResponse);
  rpc ListUsers (ListUsersRequest) returns (ListUsersResponse);
}

message GetUserRequest {
  int64 user_id = 1;
}

message UserResponse {
  int64 id = 1;
  string name = 2;
  string email = 3;
  string created_at = 4;
}

message CreateUserRequest {
  string name = 1;
  string email = 2;
  string password = 3;
}
```

```python
# Python gRPC Server
import grpc
from concurrent import futures
import user_pb2
import user_pb2_grpc

class UserServicer(user_pb2_grpc.UserServiceServicer):
    def GetUser(self, request, context):
        user = UserRepository.find_by_id(request.user_id)
        if not user:
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details('User not found')
            return user_pb2.UserResponse()
        
        return user_pb2.UserResponse(
            id=user.id,
            name=user.name,
            email=user.email,
            created_at=str(user.created_at)
        )

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserServiceServicer_to_server(
        UserServicer(), server
    )
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()
```

### 3. Asincrone: Message Queue

```python
# RabbitMQ Publisher (Order Service)
import pika
import json

def publish_order_created(order):
    connection = pika.BlockingConnection(
        pika.ConnectionParameters('rabbitmq')
    )
    channel = connection.channel()
    
    channel.exchange_declare(
        exchange='orders',
        exchange_type='topic'
    )
    
    message = {
        'order_id': order.id,
        'user_id': order.user_id,
        'total': order.total,
        'items': [item.to_dict() for item in order.items]
    }
    
    channel.basic_publish(
        exchange='orders',
        routing_key='order.created',
        body=json.dumps(message)
    )
    
    connection.close()
```

```python
# RabbitMQ Consumer (Notification Service)
import pika
import json

def callback(ch, method, properties, body):
    message = json.loads(body)
    order_id = message['order_id']
    user_id = message['user_id']
    
    # Send notification
    NotificationService.send_order_confirmation(user_id, order_id)
    
    ch.basic_ack(delivery_tag=method.delivery_tag)

connection = pika.BlockingConnection(
    pika.ConnectionParameters('rabbitmq')
)
channel = connection.channel()

channel.exchange_declare(exchange='orders', exchange_type='topic')
channel.queue_declare(queue='notifications')
channel.queue_bind(
    exchange='orders',
    queue='notifications',
    routing_key='order.created'
)

channel.basic_consume(
    queue='notifications',
    on_message_callback=callback
)

channel.start_consuming()
```

### 4. Event-Driven con Kafka

```python
# Kafka Producer (Order Service)
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=['kafka:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

def publish_order_event(order):
    event = {
        'event_type': 'OrderCreated',
        'order_id': order.id,
        'user_id': order.user_id,
        'total': order.total,
        'timestamp': datetime.now().isoformat()
    }
    
    producer.send('order-events', value=event)
    producer.flush()
```

```python
# Kafka Consumer (Payment Service)
from kafka import KafkaConsumer
import json

consumer = KafkaConsumer(
    'order-events',
    bootstrap_servers=['kafka:9092'],
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    group_id='payment-service'
)

for message in consumer:
    event = message.value
    
    if event['event_type'] == 'OrderCreated':
        order_id = event['order_id']
        total = event['total']
        
        # Process payment
        PaymentService.process_payment(order_id, total)
```

### Confronto Comunicazione

| Tipo | Vantaggi | Svantaggi | Casi d'Uso |
|------|----------|-----------|------------|
| **REST** | Semplice, standard | Accoppiamento temporale | CRUD operations |
| **gRPC** | Performante, type-safe | Meno human-readable | Comunicazione interna |
| **Message Queue** | Asincrono, decoupled | Complessità | Background tasks |
| **Event Streaming** | Scalabile, replay | Eventual consistency | Real-time, analytics |

## Pattern Architetturali

### 1. API Gateway

L'**API Gateway** è il single entry point per i client.

```python
# API Gateway con Flask
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

# Service Registry
SERVICES = {
    'user': 'http://user-service:8080',
    'order': 'http://order-service:8080',
    'product': 'http://product-service:8080',
}

@app.route('/api/users/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def user_proxy(path):
    return proxy_request('user', path)

@app.route('/api/orders/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def order_proxy(path):
    return proxy_request('order', path)

@app.route('/api/products/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def product_proxy(path):
    return proxy_request('product', path)

def proxy_request(service, path):
    url = f"{SERVICES[service]}/api/{path}"
    
    # Forward request
    resp = requests.request(
        method=request.method,
        url=url,
        headers={key: value for (key, value) in request.headers if key != 'Host'},
        data=request.get_data(),
        cookies=request.cookies,
        allow_redirects=False
    )
    
    # Return response
    return (resp.content, resp.status_code, resp.headers.items())

# Rate Limiting
from flask_limiter import Limiter

limiter = Limiter(app, key_func=lambda: request.headers.get('X-API-Key'))

@app.route('/api/limited')
@limiter.limit("100 per hour")
def limited_endpoint():
    return jsonify({'message': 'This endpoint is rate limited'})
```

**Responsabilità API Gateway:**
- Routing
- Authentication/Authorization
- Rate Limiting
- Caching
- Load Balancing
- Request/Response transformation
- Monitoring

### 2. Service Discovery

```python
# Consul Service Discovery
import consul

# Service Registration
def register_service():
    c = consul.Consul(host='consul', port=8500)
    
    c.agent.service.register(
        name='order-service',
        service_id='order-service-1',
        address='order-service',
        port=8080,
        check=consul.Check.http(
            'http://order-service:8080/health',
            interval='10s'
        )
    )

# Service Discovery
def discover_service(service_name):
    c = consul.Consul(host='consul', port=8500)
    _, services = c.health.service(service_name, passing=True)
    
    if not services:
        raise ServiceNotFoundException(f"Service {service_name} not found")
    
    # Load balance (simple random)
    import random
    service = random.choice(services)
    
    return f"http://{service['Service']['Address']}:{service['Service']['Port']}"
```

**Strumenti:**
- **Consul**: HashiCorp, full-featured
- **Eureka**: Netflix, Java-centric
- **etcd**: CNCF, lightweight
- **Kubernetes DNS**: Built-in K8s

### 3. Circuit Breaker

Previene cascading failures.

```python
# PyBreaker
from pybreaker import CircuitBreaker

# Configurazione
breaker = CircuitBreaker(
    fail_max=5,              # Fallimenti prima di aprire
    reset_timeout=60,        # Secondi prima di tentare recovery
    exclude=[ValueError]     # Eccezioni da escludere
)

@breaker
def call_user_service(user_id):
    response = requests.get(
        f'http://user-service/api/users/{user_id}',
        timeout=2
    )
    response.raise_for_status()
    return response.json()

# Uso con fallback
def get_user_with_fallback(user_id):
    try:
        return call_user_service(user_id)
    except CircuitBreakerError:
        logger.warning(f"Circuit breaker open for user service")
        return get_user_from_cache(user_id)
    except Exception as e:
        logger.error(f"Error calling user service: {e}")
        return None
```

**Stati Circuit Breaker:**
```
┌─────────┐    Failure    ┌──────┐    Timeout    ┌────────┐
│ Closed  │──────────────>│ Open │──────────────>│Half-Open│
│(Normal) │               │(Fail)│               │(Testing)│
└─────────┘               └──────┘               └────────┘
     ▲                                                 │
     │                                                 │
     └─────────────────Success─────────────────────────┘
```

### 4. Saga Pattern

Gestisce transazioni distribuite.

#### Choreography-Based Saga

```python
# Order Service
def create_order(order_data):
    order = Order.create(order_data)
    order.status = 'PENDING'
    order.save()
    
    # Publish event
    publish_event('OrderCreated', {
        'order_id': order.id,
        'user_id': order.user_id,
        'total': order.total
    })
    
    return order

# Listen for events
@event_handler('PaymentFailed')
def handle_payment_failed(event):
    order_id = event['order_id']
    order = Order.find(order_id)
    order.status = 'CANCELLED'
    order.save()
    
    publish_event('OrderCancelled', {'order_id': order_id})
```

```python
# Payment Service
@event_handler('OrderCreated')
def handle_order_created(event):
    order_id = event['order_id']
    total = event['total']
    
    try:
        payment = process_payment(order_id, total)
        publish_event('PaymentCompleted', {
            'order_id': order_id,
            'payment_id': payment.id
        })
    except PaymentError as e:
        publish_event('PaymentFailed', {
            'order_id': order_id,
            'reason': str(e)
        })
```

#### Orchestration-Based Saga

```python
# Saga Orchestrator
class OrderSaga:
    def __init__(self, order_id):
        self.order_id = order_id
        self.steps = []
    
    def execute(self):
        try:
            # Step 1: Reserve inventory
            self.reserve_inventory()
            
            # Step 2: Process payment
            self.process_payment()
            
            # Step 3: Ship order
            self.ship_order()
            
            # Complete
            self.complete_order()
            
        except Exception as e:
            logger.error(f"Saga failed: {e}")
            self.compensate()
    
    def reserve_inventory(self):
        result = InventoryService.reserve(self.order_id)
        self.steps.append({
            'action': 'reserve_inventory',
            'compensate': lambda: InventoryService.release(self.order_id)
        })
        return result
    
    def process_payment(self):
        result = PaymentService.charge(self.order_id)
        self.steps.append({
            'action': 'process_payment',
            'compensate': lambda: PaymentService.refund(self.order_id)
        })
        return result
    
    def ship_order(self):
        result = ShippingService.ship(self.order_id)
        self.steps.append({
            'action': 'ship_order',
            'compensate': lambda: ShippingService.cancel(self.order_id)
        })
        return result
    
    def compensate(self):
        # Rollback in reverse order
        for step in reversed(self.steps):
            try:
                step['compensate']()
            except Exception as e:
                logger.error(f"Compensation failed for {step['action']}: {e}")
```

### 5. CQRS (Command Query Responsibility Segregation)

Separa read e write models.

```python
# Write Model (Commands)
class CreateOrderCommand:
    def __init__(self, user_id, items):
        self.user_id = user_id
        self.items = items

class OrderCommandHandler:
    def handle(self, command):
        # Validate
        if not self.validate_items(command.items):
            raise InvalidOrderError("Invalid items")
        
        # Create order
        order = Order.create(
            user_id=command.user_id,
            items=command.items
        )
        
        # Publish event
        publish_event('OrderCreated', {
            'order_id': order.id,
            'user_id': order.user_id,
            'items': order.items,
            'total': order.total
        })
        
        return order

# Read Model (Queries)
class OrderQueryService:
    def get_order(self, order_id):
        # Read from optimized read database
        return read_db.orders.find_one({'id': order_id})
    
    def get_user_orders(self, user_id):
        # Can use denormalized data
        return read_db.user_orders.find({'user_id': user_id})
    
    def get_order_statistics(self, user_id):
        # Pre-computed aggregations
        return read_db.order_stats.find_one({'user_id': user_id})

# Event Handler (Updates Read Model)
@event_handler('OrderCreated')
def update_read_model(event):
    order_data = {
        'id': event['order_id'],
        'user_id': event['user_id'],
        'items': event['items'],
        'total': event['total'],
        'status': 'PENDING',
        'created_at': datetime.now()
    }
    
    # Update read database
    read_db.orders.insert_one(order_data)
    
    # Update user orders denormalized view
    read_db.user_orders.update_one(
        {'user_id': event['user_id']},
        {'$push': {'orders': order_data}},
        upsert=True
    )
```

### 6. Backend for Frontend (BFF)

```python
# Mobile BFF
@app.route('/mobile/api/home')
def mobile_home():
    # Optimized for mobile
    user = get_user(request.user_id)
    products = get_featured_products(limit=10)  # Less data
    
    return jsonify({
        'user': {
            'name': user['name'],
            'avatar_url': user['avatar_thumbnail']  # Smaller image
        },
        'products': [{
            'id': p['id'],
            'name': p['name'],
            'price': p['price'],
            'image': p['thumbnail']
        } for p in products]
    })

# Web BFF
@app.route('/web/api/home')
def web_home():
    # Optimized for web
    user = get_user(request.user_id)
    products = get_featured_products(limit=50)  # More data
    recommendations = get_recommendations(request.user_id)
    
    return jsonify({
        'user': user,  # Full user data
        'products': products,  # Full product data
        'recommendations': recommendations,
        'metadata': {
            'total_products': count_products(),
            'categories': get_categories()
        }
    })
```

## Database per Microservizio

### Database per Service Pattern

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│UserService   │     │OrderService  │     │ProductService│
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
┌──────▼───────┐     ┌──────▼───────┐     ┌──────▼───────┐
│  User DB     │     │  Order DB    │     │ Product DB   │
│ (PostgreSQL) │     │  (MongoDB)   │     │ (PostgreSQL) │
└──────────────┘     └──────────────┘     └──────────────┘
```

### Shared Database Anti-Pattern

```
❌ DA EVITARE:

┌────────────┐  ┌────────────┐  ┌────────────┐
│UserService │  │OrderService│  │ProductSvc  │
└─────┬──────┘  └─────┬──────┘  └─────┬──────┘
      │               │               │
      └───────────────┼───────────────┘
                      │
              ┌───────▼───────┐
              │ Shared Database│
              └───────────────┘
              
Problemi:
- Accoppiamento stretto
- Difficoltà scaling
- Schema migration complesse
```

### Data Consistency Patterns

#### 1. Event Sourcing

```python
# Event Store
class OrderEvent:
    def __init__(self, event_type, data):
        self.event_type = event_type
        self.data = data
        self.timestamp = datetime.now()

class OrderAggregate:
    def __init__(self, order_id):
        self.order_id = order_id
        self.events = []
        self.state = {}
    
    def create_order(self, user_id, items):
        event = OrderEvent('OrderCreated', {
            'order_id': self.order_id,
            'user_id': user_id,
            'items': items
        })
        self.apply_event(event)
        self.events.append(event)
    
    def add_item(self, item):
        event = OrderEvent('ItemAdded', {'item': item})
        self.apply_event(event)
        self.events.append(event)
    
    def apply_event(self, event):
        if event.event_type == 'OrderCreated':
            self.state = {
                'order_id': event.data['order_id'],
                'user_id': event.data['user_id'],
                'items': event.data['items'],
                'status': 'PENDING'
            }
        elif event.event_type == 'ItemAdded':
            self.state['items'].append(event.data['item'])
    
    def rebuild_from_events(self, events):
        for event in events:
            self.apply_event(event)
```

#### 2. Change Data Capture (CDC)

```python
# Debezium CDC with Kafka
# Database changes automatically captured and published

# Consumer
from kafka import KafkaConsumer

consumer = KafkaConsumer(
    'dbserver1.inventory.products',
    bootstrap_servers=['kafka:9092']
)

for message in consumer:
    change = message.value
    
    if change['op'] == 'c':  # Create
        product = change['after']
        update_search_index(product)
    
    elif change['op'] == 'u':  # Update
        product = change['after']
        update_search_index(product)
    
    elif change['op'] == 'd':  # Delete
        product_id = change['before']['id']
        delete_from_search_index(product_id)
```

## Resilienza e Fault Tolerance

### 1. Retry Pattern

```python
from tenacity import retry, stop_after_attempt, wait_exponential

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10)
)
def call_external_api():
    response = requests.get('http://external-api/data')
    response.raise_for_status()
    return response.json()
```

### 2. Timeout Pattern

```python
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

def create_session():
    session = requests.Session()
    
    retry = Retry(
        total=3,
        backoff_factor=0.3,
        status_forcelist=[500, 502, 503, 504]
    )
    
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.mount('https://', adapter)
    
    return session

def call_service():
    session = create_session()
    try:
        response = session.get(
            'http://service/api',
            timeout=(3.05, 27)  # (connect, read) timeout
        )
        return response.json()
    except requests.exceptions.Timeout:
        logger.error("Service timeout")
        raise
```

### 3. Bulkhead Pattern

```python
from concurrent.futures import ThreadPoolExecutor

# Separate thread pools per service
user_service_executor = ThreadPoolExecutor(max_workers=10)
order_service_executor = ThreadPoolExecutor(max_workers=20)
payment_service_executor = ThreadPoolExecutor(max_workers=5)

def call_user_service(user_id):
    future = user_service_executor.submit(
        _fetch_user, user_id
    )
    return future.result(timeout=5)

def call_order_service(order_id):
    future = order_service_executor.submit(
        _fetch_order, order_id
    )
    return future.result(timeout=5)
```

### 4. Health Checks

```python
from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/health')
def health():
    return jsonify({'status': 'healthy'}), 200

@app.route('/health/ready')
def ready():
    # Check dependencies
    checks = {
        'database': check_database(),
        'redis': check_redis(),
        'message_queue': check_rabbitmq()
    }
    
    if all(checks.values()):
        return jsonify({
            'status': 'ready',
            'checks': checks
        }), 200
    else:
        return jsonify({
            'status': 'not ready',
            'checks': checks
        }), 503

@app.route('/health/live')
def live():
    # Basic liveness check
    return jsonify({'status': 'alive'}), 200

def check_database():
    try:
        db.session.execute('SELECT 1')
        return True
    except:
        return False

def check_redis():
    try:
        redis_client.ping()
        return True
    except:
        return False

def check_rabbitmq():
    try:
        connection = pika.BlockingConnection(
            pika.ConnectionParameters('rabbitmq')
        )
        connection.close()
        return True
    except:
        return False
```

## Security nei Microservizi

### 1. Authentication & Authorization

```python
# JWT Authentication
import jwt
from functools import wraps
from flask import request, jsonify

SECRET_KEY = 'your-secret-key'

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        
        if not token:
            return jsonify({'message': 'Token missing'}), 401
        
        try:
            token = token.split(' ')[1]  # Bearer <token>
            data = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
            request.user_id = data['user_id']
            request.user_roles = data['roles']
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Token expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Invalid token'}), 401
        
        return f(*args, **kwargs)
    
    return decorated

def role_required(role):
    def decorator(f):
        @wraps(f)
        @token_required
        def decorated(*args, **kwargs):
            if role not in request.user_roles:
                return jsonify({'message': 'Insufficient permissions'}), 403
            return f(*args, **kwargs)
        return decorated
    return decorator

@app.route('/api/admin/users')
@role_required('admin')
def admin_users():
    users = User.query.all()
    return jsonify([u.to_dict() for u in users])
```

### 2. Service-to-Service Authentication

```python
# Mutual TLS (mTLS)
import requests

def call_service_with_mtls():
    cert = ('/path/to/client.crt', '/path/to/client.key')
    
    response = requests.get(
        'https://service/api',
        cert=cert,
        verify='/path/to/ca.crt'
    )
    
    return response.json()
```

### 3. API Key Management

```python
# API Key middleware
def verify_api_key(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        api_key = request.headers.get('X-API-Key')
        
        if not api_key:
            return jsonify({'error': 'API key missing'}), 401
        
        # Validate API key
        if not ApiKey.validate(api_key):
            return jsonify({'error': 'Invalid API key'}), 401
        
        # Check rate limit
        if not RateLimiter.check(api_key):
            return jsonify({'error': 'Rate limit exceeded'}), 429
        
        return f(*args, **kwargs)
    
    return decorated
```

### 4. Secrets Management

```python
# HashiCorp Vault integration
import hvac

client = hvac.Client(url='http://vault:8200')
client.token = 'your-vault-token'

# Read secret
secret = client.secrets.kv.v2.read_secret_version(
    path='database/credentials'
)

db_password = secret['data']['data']['password']

# Dynamic secrets
db_creds = client.secrets.database.generate_credentials(
    name='my-database-role'
)

username = db_creds['data']['username']
password = db_creds['data']['password']
```

## Monitoring e Observability

### 1. Logging

```python
import logging
import json
from pythonjsonlogger import jsonlogger

# Structured logging
logger = logging.getLogger()

logHandler = logging.StreamHandler()
formatter = jsonlogger.JsonFormatter()
logHandler.setFormatter(formatter)
logger.addHandler(logHandler)
logger.setLevel(logging.INFO)

# Usage
logger.info('Order created', extra={
    'order_id': order.id,
    'user_id': order.user_id,
    'total': order.total,
    'service': 'order-service',
    'trace_id': request.trace_id
})
```

### 2. Distributed Tracing

```python
# OpenTelemetry
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.jaeger.thrift import JaegerExporter

# Setup tracer
trace.set_tracer_provider(TracerProvider())
tracer = trace.get_tracer(__name__)

jaeger_exporter = JaegerExporter(
    agent_host_name='jaeger',
    agent_port=6831,
)

trace.get_tracer_provider().add_span_processor(
    BatchSpanProcessor(jaeger_exporter)
)

# Trace request
@app.route('/api/orders/<order_id>')
def get_order(order_id):
    with tracer.start_as_current_span("get_order") as span:
        span.set_attribute("order_id", order_id)
        
        # Get user
        with tracer.start_as_current_span("get_user"):
            user = UserService.get_user(order.user_id)
        
        # Get products
        with tracer.start_as_current_span("get_products"):
            products = ProductService.get_products(order.product_ids)
        
        return jsonify(order.to_dict())
```

### 3. Metrics

```python
# Prometheus metrics
from prometheus_client import Counter, Histogram, Gauge
from prometheus_client import make_wsgi_app
from werkzeug.middleware.dispatcher import DispatcherMiddleware

# Metrics
request_count = Counter(
    'http_requests_total',
    'Total HTTP requests',
    ['method', 'endpoint', 'status']
)

request_duration = Histogram(
    'http_request_duration_seconds',
    'HTTP request duration',
    ['method', 'endpoint']
)

active_requests = Gauge(
    'http_requests_active',
    'Active HTTP requests'
)

# Middleware
@app.before_request
def before_request():
    request.start_time = time.time()
    active_requests.inc()

@app.after_request
def after_request(response):
    request_duration.labels(
        method=request.method,
        endpoint=request.endpoint
    ).observe(time.time() - request.start_time)
    
    request_count.labels(
        method=request.method,
        endpoint=request.endpoint,
        status=response.status_code
    ).inc()
    
    active_requests.dec()
    
    return response

# Expose metrics endpoint
app.wsgi_app = DispatcherMiddleware(app.wsgi_app, {
    '/metrics': make_wsgi_app()
})
```

## Testing Microservizi

### 1. Unit Testing

```python
import unittest
from unittest.mock import Mock, patch

class OrderServiceTest(unittest.TestCase):
    def setUp(self):
        self.order_service = OrderService()
    
    @patch('services.UserService.get_user')
    def test_create_order(self, mock_get_user):
        # Mock user service
        mock_get_user.return_value = {
            'id': 1,
            'name': 'John Doe',
            'email': 'john@example.com'
        }
        
        # Create order
        order = self.order_service.create_order(
            user_id=1,
            items=[{'product_id': 1, 'quantity': 2}]
        )
        
        # Assertions
        self.assertIsNotNone(order)
        self.assertEqual(order.user_id, 1)
        mock_get_user.assert_called_once_with(1)
```

### 2. Integration Testing

```python
# Test con database
import pytest
from app import create_app, db

@pytest.fixture
def app():
    app = create_app('testing')
    with app.app_context():
        db.create_all()
        yield app
        db.drop_all()

@pytest.fixture
def client(app):
    return app.test_client()

def test_create_order_integration(client):
    # Create user
    user_response = client.post('/api/users', json={
        'name': 'John Doe',
        'email': 'john@example.com'
    })
    user_id = user_response.json['id']
    
    # Create order
    order_response = client.post('/api/orders', json={
        'user_id': user_id,
        'items': [{'product_id': 1, 'quantity': 2}]
    })
    
    assert order_response.status_code == 201
    assert order_response.json['user_id'] == user_id
```

### 3. Contract Testing

```python
# Pact - Consumer side
from pact import Consumer, Provider

pact = Consumer('OrderService').has_pact_with(Provider('UserService'))

def test_get_user_contract():
    (pact
     .given('user 123 exists')
     .upon_receiving('a request for user 123')
     .with_request('GET', '/api/users/123')
     .will_respond_with(200, body={
         'id': 123,
         'name': 'John Doe',
         'email': 'john@example.com'
     }))
    
    with pact:
        user = UserServiceClient.get_user(123)
        assert user['id'] == 123
```

### 4. End-to-End Testing

```python
# Playwright E2E testing
from playwright.sync_api import sync_playwright

def test_complete_order_flow():
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        
        # Login
        page.goto('http://localhost:3000/login')
        page.fill('[name=email]', 'test@example.com')
        page.fill('[name=password]', 'password')
        page.click('button[type=submit]')
        
        # Add to cart
        page.goto('http://localhost:3000/products/1')
        page.click('button.add-to-cart')
        
        # Checkout
        page.goto('http://localhost:3000/cart')
        page.click('button.checkout')
        
        # Verify order
        page.wait_for_selector('.order-confirmation')
        assert 'Order confirmed' in page.content()
        
        browser.close()
```

## Deployment Patterns

### 1. Blue-Green Deployment

```yaml
# Kubernetes Blue-Green
# Blue (current)
apiVersion: v1
kind: Service
metadata:
  name: app-service
spec:
  selector:
    app: myapp
    version: blue  # Points to blue
  ports:
  - port: 80

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
      version: blue
  template:
    metadata:
      labels:
        app: myapp
        version: blue
    spec:
      containers:
      - name: app
        image: myapp:v1.0

---
# Green (new)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-green
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
      version: green
  template:
    metadata:
      labels:
        app: myapp
        version: green
    spec:
      containers:
      - name: app
        image: myapp:v2.0

# Switch: kubectl patch service app-service -p '{"spec":{"selector":{"version":"green"}}}'
```

### 2. Canary Deployment

```yaml
# Istio Canary
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: app-vs
spec:
  hosts:
  - app-service
  http:
  - match:
    - headers:
        x-canary:
          exact: "true"
    route:
    - destination:
        host: app-service
        subset: v2
  - route:
    - destination:
        host: app-service
        subset: v1
      weight: 90
    - destination:
        host: app-service
        subset: v2
      weight: 10  # 10% canary traffic
```

## Best Practices

### Design

1. **Single Responsibility**: Un servizio, una funzione
2. **Loose Coupling**: Minimizza dipendenze tra servizi
3. **High Cohesion**: Raggruppa funzionalità correlate
4. **API First**: Progetta API prima dell'implementazione
5. **Stateless**: Servizi senza stato quando possibile

### Development

1. **Standardize**: Convenzioni comuni tra team
2. **Automate**: CI/CD per ogni servizio
3. **Version APIs**: Semantic versioning
4. **Document**: OpenAPI/Swagger specs
5. **Test**: Unit, integration, contract, e2e

### Operations

1. **Monitor Everything**: Metrics, logs, traces
2. **Automate Deployment**: Blue-green, canary
3. **Disaster Recovery**: Backup e restore procedures
4. **Capacity Planning**: Load testing regolare
5. **Security**: Defense in depth

### Communication

1. **Async quando possibile**: Decouple services
2. **Idempotency**: Operazioni ripetibili
3. **Timeouts**: Su tutte le chiamate
4. **Circuit Breakers**: Prevent cascading failures
5. **Retry with Backoff**: Exponential backoff

## Anti-Patterns

### 1. Distributed Monolith

```
❌ Microservizi troppo accoppiati che devono deployare insieme
```

### 2. Chatty Services

```
❌ Troppe chiamate sincrone tra servizi
✅ Usa messaging asincrono o aggregazioni
```

### 3. Shared Database

```
❌ Servizi che condividono lo stesso database
✅ Database per servizio pattern
```

### 4. No API Gateway

```
❌ Client chiamano direttamente i servizi
✅ Usa API Gateway per routing e sicurezza
```

### 5. Ignoring Network Failures

```
❌ Assumere che la rete sia affidabile
✅ Implementa retry, timeout, circuit breaker
```

## Esercizi Pratici

### Esercizio 1: E-commerce Microservizi

Implementa un sistema e-commerce con:
- User Service
- Product Service
- Order Service
- Payment Service
- Notification Service

### Esercizio 2: Event-Driven Architecture

Implementa un sistema di notifiche event-driven:
- Event producer (Order Service)
- Event bus (Kafka/RabbitMQ)
- Multiple consumers (Email, SMS, Push)

### Esercizio 3: API Gateway

Crea un API Gateway con:
- Routing
- Authentication
- Rate limiting
- Request transformation

## Domande di Verifica

1. Quali sono i principali vantaggi e svantaggi dei microservizi?
2. Spiega la differenza tra comunicazione sincrona e asincrona
3. Cos'è il pattern Circuit Breaker e quando usarlo?
4. Come gestiresti transazioni distribuite nei microservizi?
5. Spiega il pattern Database per Service
6. Qual è il ruolo dell'API Gateway?
7. Come implementeresti service discovery?
8. Spiega il pattern Saga per transazioni distribuite
9. Quali strategie useresti per garantire la resilienza?
10. Come monitoreresti un sistema di microservizi?

## Risorse Aggiuntive

- [Microservices.io](https://microservices.io/) - Patterns
- [Martin Fowler - Microservices](https://martinfowler.com/articles/microservices.html)
- [Building Microservices (Book)](https://www.oreilly.com/library/view/building-microservices/9781491950340/)
- [CNCF Cloud Native Trail Map](https://github.com/cncf/trailmap)
- [The Twelve-Factor App](https://12factor.net/)

## Conclusioni

Le architetture a microservizi offrono scalabilità, flessibilità e resilienza, ma introducono complessità nella gestione, comunicazione e deployment. Il successo richiede una comprensione profonda dei pattern, best practices e strumenti appropriati. La scelta di adottare microservizi dovrebbe essere guidata dalle reali necessità di business e dalla capacità organizzativa di gestire la complessità distribuita.
