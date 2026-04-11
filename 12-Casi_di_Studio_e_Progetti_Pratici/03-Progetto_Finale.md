# Progetto Finale

## Obiettivo

Progettare e implementare un'applicazione cloud-native completa che dimostri competenze in:
- Architettura cloud
- DevOps/CI/CD
- Sicurezza
- Monitoring
- Cost optimization

## Progetto: E-Commerce Platform

### Requisiti Funzionali
1. **User Management**: Registrazione, login, profilo
2. **Product Catalog**: Browse, search, filter
3. **Shopping Cart**: Add/remove items
4. **Orders**: Checkout, payment, tracking
5. **Admin Dashboard**: Inventory, analytics

### Requisiti Non-Funzionali
- **Scalability**: 10K concurrent users
- **Availability**: 99.9% uptime
- **Performance**: < 200ms API response
- **Security**: HTTPS, encryption, GDPR
- **Cost**: < $200/mese per MVP

## Architettura Proposta

### High-Level Design
```
┌─────────────────────────────────────────┐
│ CloudFront + S3 (Frontend)              │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│ API Gateway + Lambda (Backend)          │
└────┬────────────────┬───────────────────┘
     │                │
┌────▼─────┐    ┌─────▼──────┐
│ DynamoDB │    │ S3 (Images)│
└──────────┘    └────────────┘
```

### Tech Stack
- **Frontend**: React + TypeScript
- **Backend**: Node.js + Express (Lambda)
- **Database**: DynamoDB
- **Auth**: Cognito
- **Storage**: S3
- **CDN**: CloudFront
- **CI/CD**: GitHub Actions
- **IaC**: Terraform

## Implementazione

### 1. Infrastructure (Terraform)
```hcl
# main.tf
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket = "my-terraform-state"
    key    = "ecommerce/terraform.tfstate"
    region = "eu-west-1"
  }
}

# DynamoDB tables
resource "aws_dynamodb_table" "products" {
  name         = "products"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "productId"

  attribute {
    name = "productId"
    type = "S"
  }

  tags = {
    Environment = "production"
    Project     = "ecommerce"
  }
}

# Lambda functions
resource "aws_lambda_function" "api" {
  function_name = "ecommerce-api"
  runtime       = "nodejs18.x"
  handler       = "index.handler"
  role          = aws_iam_role.lambda_role.arn
  filename      = "lambda.zip"

  environment {
    variables = {
      TABLE_NAME = aws_dynamodb_table.products.name
    }
  }
}

# API Gateway
resource "aws_apigatewayv2_api" "api" {
  name          = "ecommerce-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "DELETE"]
    allow_headers = ["*"]
  }
}
```

### 2. Backend API (Lambda)
```javascript
// src/api/products.js
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
  const { httpMethod, path, body } = event;

  try {
    switch (httpMethod) {
      case 'GET':
        if (path === '/products') {
          return await getProducts();
        } else if (path.startsWith('/products/')) {
          const productId = path.split('/')[2];
          return await getProduct(productId);
        }
        break;

      case 'POST':
        if (path === '/products') {
          return await createProduct(JSON.parse(body));
        }
        break;

      case 'PUT':
        if (path.startsWith('/products/')) {
          const productId = path.split('/')[2];
          return await updateProduct(productId, JSON.parse(body));
        }
        break;

      case 'DELETE':
        if (path.startsWith('/products/')) {
          const productId = path.split('/')[2];
          return await deleteProduct(productId);
        }
        break;
    }

    return {
      statusCode: 404,
      body: JSON.stringify({ error: 'Not found' })
    };
  } catch (error) {
    console.error(error);
    return {
      statusCode: 500,
      body: JSON.stringify({ error: error.message })
    };
  }
};

async function getProducts() {
  const result = await dynamodb.scan({
    TableName: process.env.TABLE_NAME
  }).promise();

  return {
    statusCode: 200,
    body: JSON.stringify(result.Items)
  };
}

async function getProduct(productId) {
  const result = await dynamodb.get({
    TableName: process.env.TABLE_NAME,
    Key: { productId }
  }).promise();

  if (!result.Item) {
    return {
      statusCode: 404,
      body: JSON.stringify({ error: 'Product not found' })
    };
  }

  return {
    statusCode: 200,
    body: JSON.stringify(result.Item)
  };
}

async function createProduct(product) {
  const { v4: uuidv4 } = require('uuid');
  
  const item = {
    productId: uuidv4(),
    ...product,
    createdAt: new Date().toISOString()
  };

  await dynamodb.put({
    TableName: process.env.TABLE_NAME,
    Item: item
  }).promise();

  return {
    statusCode: 201,
    body: JSON.stringify(item)
  };
}
```

### 3. Frontend (React)
```jsx
// src/App.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL;

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    const response = await axios.get(`${API_URL}/products`);
    setProducts(response.data);
  };

  const addToCart = (product) => {
    setCart([...cart, product]);
  };

  return (
    <div className="App">
      <h1>E-Commerce Store</h1>
      
      <div className="products">
        {products.map(product => (
          <div key={product.productId} className="product-card">
            <img src={product.imageUrl} alt={product.name} />
            <h3>{product.name}</h3>
            <p>${product.price}</p>
            <button onClick={() => addToCart(product)}>
              Add to Cart
            </button>
          </div>
        ))}
      </div>

      <div className="cart">
        <h2>Cart ({cart.length})</h2>
        <p>Total: ${cart.reduce((sum, p) => sum + p.price, 0)}</p>
      </div>
    </div>
  );
}

export default App;
```

### 4. CI/CD (GitHub Actions)
```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        run: npm ci

      - name: Run tests
        run: npm test

      - name: Build frontend
        run: |
          cd frontend
          npm run build

      - name: Deploy to S3
        uses: jakejarvis/s3-sync-action@master
        with:
          args: --delete
        env:
          AWS_S3_BUCKET: ${{ secrets.S3_BUCKET }}
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          SOURCE_DIR: 'frontend/build'

      - name: Deploy Lambda
        run: |
          cd backend
          zip -r lambda.zip .
          aws lambda update-function-code \
            --function-name ecommerce-api \
            --zip-file fileb://lambda.zip

      - name: Invalidate CloudFront
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ secrets.CF_DISTRIBUTION_ID }} \
            --paths "/*"
```

### 5. Monitoring
```yaml
# cloudwatch-dashboard.json
{
  "widgets": [
    {
      "type": "metric",
      "properties": {
        "metrics": [
          ["AWS/Lambda", "Invocations", {"stat": "Sum"}],
          [".", "Errors", {"stat": "Sum"}],
          [".", "Duration", {"stat": "Average"}]
        ],
        "period": 300,
        "stat": "Average",
        "region": "eu-west-1",
        "title": "Lambda Metrics"
      }
    },
    {
      "type": "metric",
      "properties": {
        "metrics": [
          ["AWS/DynamoDB", "ConsumedReadCapacityUnits"],
          [".", "ConsumedWriteCapacityUnits"]
        ],
        "period": 300,
        "stat": "Sum",
        "region": "eu-west-1",
        "title": "DynamoDB Metrics"
      }
    }
  ]
}
```

## Testing

### Unit Tests
```javascript
// tests/products.test.js
const { handler } = require('../src/api/products');

describe('Products API', () => {
  test('GET /products returns list', async () => {
    const event = {
      httpMethod: 'GET',
      path: '/products'
    };

    const response = await handler(event);
    
    expect(response.statusCode).toBe(200);
    expect(JSON.parse(response.body)).toBeInstanceOf(Array);
  });
});
```

### Load Testing (k6)
```javascript
// load-test.js
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export default function () {
  let response = http.get('https://api.example.com/products');
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });
}
```

## Deliverables

### 1. Codice Sorgente (GitHub)
- Frontend React
- Backend Lambda
- Infrastructure Terraform
- CI/CD pipelines

### 2. Documentazione
- Architecture diagram
- API documentation
- Deployment guide
- Runbook

### 3. Demo
- Live application URL
- Admin dashboard
- Monitoring dashboard

### 4. Presentation
- Architecture decisions
- Scalability strategy
- Security measures
- Cost analysis
- Lessons learned

## Valutazione

- **Architettura** (30%): Scalability, resilience
- **Implementazione** (25%): Code quality, best practices
- **Security** (15%): HTTPS, auth, data protection
- **DevOps** (15%): CI/CD, IaC, monitoring
- **Documentazione** (10%): Clarity, completeness
- **Demo** (5%): Working application

## Timeline
- **Week 1-2**: Design e planning
- **Week 3-4**: Infrastructure setup
- **Week 5-6**: Backend implementation
- **Week 7-8**: Frontend development
- **Week 9**: Testing e optimization
- **Week 10**: Documentation e presentation

Buon lavoro! 🚀
