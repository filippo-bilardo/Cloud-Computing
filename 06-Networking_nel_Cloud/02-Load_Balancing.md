# Load Balancing

## Tipi di Load Balancer

### 1. Application Load Balancer (Layer 7)
- HTTP/HTTPS
- Path-based routing
- Host-based routing
- WebSockets support

### 2. Network Load Balancer (Layer 4)
- TCP/UDP/TLS
- Ultra-low latency
- Millions req/sec
- Static IP support

### 3. Gateway Load Balancer
- Layer 3
- Firewall, IDS/IPS

## AWS ALB

```bash
# Create target group
aws elbv2 create-target-group \
  --name my-targets \
  --protocol HTTP \
  --port 80 \
  --vpc-id vpc-123456 \
  --health-check-path /health

# Register targets
aws elbv2 register-targets \
  --target-group-arn arn:aws:elasticloadbalancing:... \
  --targets Id=i-123456,Port=80 Id=i-789012,Port=80

# Create ALB
aws elbv2 create-load-balancer \
  --name my-alb \
  --subnets subnet-aaa subnet-bbb \
  --security-groups sg-123456

# Create listener
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:... \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=arn:...
```

## Path-Based Routing

```bash
# Route /api → API target group
aws elbv2 create-rule \
  --listener-arn arn:... \
  --priority 10 \
  --conditions Field=path-pattern,Values='/api/*' \
  --actions Type=forward,TargetGroupArn=arn:...api-targets

# Route /images → Static target group
aws elbv2 create-rule \
  --listener-arn arn:... \
  --priority 20 \
  --conditions Field=path-pattern,Values='/images/*' \
  --actions Type=forward,TargetGroupArn=arn:...static-targets
```

## Health Checks

```bash
aws elbv2 modify-target-group \
  --target-group-arn arn:... \
  --health-check-protocol HTTP \
  --health-check-path /health \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3
```

## Auto Scaling

```bash
# Create launch template
aws ec2 create-launch-template \
  --launch-template-name my-template \
  --version-description v1 \
  --launch-template-data '{
    "ImageId": "ami-123456",
    "InstanceType": "t3.micro"
  }'

# Create Auto Scaling Group
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name my-asg \
  --launch-template LaunchTemplateName=my-template \
  --min-size 2 \
  --max-size 10 \
  --desired-capacity 2 \
  --target-group-arns arn:... \
  --vpc-zone-identifier "subnet-aaa,subnet-bbb"

# Target tracking policy
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name my-asg \
  --policy-name cpu-target \
  --policy-type TargetTrackingScaling \
  --target-tracking-configuration '{
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ASGAverageCPUUtilization"
    },
    "TargetValue": 50.0
  }'
```

## Best Practices
1. Multi-AZ deployment
2. Health checks configured
3. SSL/TLS termination at LB
4. Connection draining enabled
5. Monitor unhealthy targets
6. Use target tracking scaling
