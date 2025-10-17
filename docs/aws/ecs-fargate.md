# ECS Fargate Deployment

Step-by-step guide for deploying to AWS ECS Fargate.

## 📋 Prerequisites

- AWS Account
- AWS CLI installed and configured
- Docker image built locally
- Basic AWS knowledge

## 🏗️ Architecture
```
┌─────────────────────────────────────────────────────┐
│                    AWS Cloud                        │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │              VPC (10.0.0.0/16)              │  │
│  │                                              │  │
│  │  ┌────────────┐       ┌─────────────────┐  │  │
│  │  │ Public     │       │ Private         │  │  │
│  │  │ Subnet     │       │ Subnet          │  │  │
│  │  │            │       │                 │  │  │
│  │  │  ┌─────┐   │       │  ┌──────────┐  │  │  │
│  │  │  │ ALB │───┼───────┼─→│ ECS Task │  │  │  │
│  │  │  └─────┘   │       │  │ Fargate  │  │  │  │
│  │  │            │       │  └────┬─────┘  │  │  │
│  │  └────────────┘       │       │        │  │  │
│  │                       │       ↓        │  │  │
│  │                       │  ┌──────────┐  │  │  │
│  │                       │  │   RDS    │  │  │  │
│  │                       │  │PostgreSQL│  │  │  │
│  │                       │  └──────────┘  │  │  │
│  │                       └─────────────────┘  │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## 🚀 Step 1: Push Image to ECR
```bash
# Create ECR repository
aws ecr create-repository --repository-name incident-coordinator

# Get login command
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag incident-coordinator:1.0.0 \
  <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/incident-coordinator:1.0.0

# Push image
docker push \
  <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/incident-coordinator:1.0.0
```

## 📝 Step 2: Create RDS Database (Optional)

**For production use PostgreSQL instead of H2:**
```bash
# Create RDS PostgreSQL instance
aws rds create-db-instance \
  --db-instance-identifier incident-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 15.4 \
  --master-username admin \
  --master-user-password <SECURE_PASSWORD> \
  --allocated-storage 20 \
  --vpc-security-group-ids <SECURITY_GROUP_ID> \
  --db-subnet-group-name <SUBNET_GROUP_NAME> \
  --backup-retention-period 7 \
  --publicly-accessible false
```

**Update application.properties for PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://<RDS_ENDPOINT>:5432/incidentdb
spring.datasource.username=admin
spring.datasource.password=<PASSWORD_FROM_SECRETS_MANAGER>
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 🎯 Step 3: Create ECS Cluster
```bash
# Create cluster
aws ecs create-cluster --cluster-name incident-response-cluster
```

## 📋 Step 4: Create Task Definition

**File:** `task-definition.json`
```json
{
  "family": "incident-coordinator",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "incident-coordinator",
      "image": "<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/incident-coordinator:1.0.0",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "production"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:<ACCOUNT_ID>:secret:rds-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/incident-coordinator",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/api/incidents || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

**Register task definition:**
```bash
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

## 🌐 Step 5: Create Application Load Balancer
```bash
# Create ALB
aws elbv2 create-load-balancer \
  --name incident-coordinator-alb \
  --subnets <PUBLIC_SUBNET_1> <PUBLIC_SUBNET_2> \
  --security-groups <ALB_SECURITY_GROUP>

# Create target group
aws elbv2 create-target-group \
  --name incident-coordinator-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id <VPC_ID> \
  --target-type ip \
  --health-check-path /api/incidents

# Create listener
aws elbv2 create-listener \
  --load-balancer-arn <ALB_ARN> \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=<TARGET_GROUP_ARN>
```

## 🚀 Step 6: Create ECS Service
```bash
aws ecs create-service \
  --cluster incident-response-cluster \
  --service-name incident-coordinator-service \
  --task-definition incident-coordinator \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[<PRIVATE_SUBNET_1>,<PRIVATE_SUBNET_2>],
    securityGroups=[<ECS_SECURITY_GROUP>],
    assignPublicIp=DISABLED
  }" \
  --load-balancers "targetGroupArn=<TARGET_GROUP_ARN>,containerName=incident-coordinator,containerPort=8080"
```

## 🔍 Step 7: Verify Deployment
```bash
# Check service status
aws ecs describe-services \
  --cluster incident-response-cluster \
  --services incident-coordinator-service

# Get ALB DNS name
aws elbv2 describe-load-balancers \
  --names incident-coordinator-alb \
  --query 'LoadBalancers[0].DNSName' \
  --output text

# Access application
# http://<ALB_DNS_NAME>/swagger-ui.html
```

## 📊 Step 8: Enable Auto Scaling
```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/incident-response-cluster/incident-coordinator-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# Create scaling policy (CPU-based)
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/incident-response-cluster/incident-coordinator-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }'
```

## 🗑️ Cleanup (Delete Everything)
```bash
# Delete ECS service
aws ecs delete-service \
  --cluster incident-response-cluster \
  --service incident-coordinator-service \
  --force

# Delete ECS cluster
aws ecs delete-cluster --cluster incident-response-cluster

# Delete ALB
aws elbv2 delete-load-balancer --load-balancer-arn <ALB_ARN>

# Delete target group
aws elbv2 delete-target-group --target-group-arn <TARGET_GROUP_ARN>

# Delete RDS instance
aws rds delete-db-instance \
  --db-instance-identifier incident-db \
  --skip-final-snapshot

# Delete ECR repository
aws ecr delete-repository \
  --repository-name incident-coordinator \
  --force
```

## 💰 Estimated Monthly Cost

**Minimal setup (development):**
- ECS Fargate (2 tasks, 0.5 vCPU, 1GB): ~$30/month
- Application Load Balancer: ~$20/month
- RDS t3.micro PostgreSQL: ~$25/month
- Data transfer: ~$5/month
- **Total: ~$80/month**

**Production setup:**
- ECS Fargate (4 tasks, 1 vCPU, 2GB): ~$120/month
- Application Load Balancer: ~$20/month
- RDS t3.small Multi-AZ: ~$60/month
- NAT Gateway: ~$30/month
- Data transfer: ~$20/month
- **Total: ~$250/month**

## 🎯 Production Checklist

- [ ] Use RDS PostgreSQL (not H2)
- [ ] Enable Multi-AZ for RDS
- [ ] Configure automated backups
- [ ] Set up CloudWatch alarms
- [ ] Enable container insights
- [ ] Use HTTPS with ACM certificate
- [ ] Implement WAF for security
- [ ] Set up CI/CD pipeline
- [ ] Configure log retention
- [ ] Enable X-Ray tracing
- [ ] Test disaster recovery
- [ ] Document runbooks

## 🔗 Additional Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Fargate Pricing](https://aws.amazon.com/fargate/pricing/)
- [RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)