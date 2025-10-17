# AWS Deployment Guide

Complete guide for deploying the Incident Response Coordinator to AWS.

## 🎯 Deployment Options

| Option | Best For | Complexity | Cost |
|--------|----------|------------|------|
| **ECS Fargate** | Serverless containers | Low | $ |
| **EKS (Kubernetes)** | Full K8s features | High | $$$ |
| **EC2 + Docker** | Simple, full control | Medium | $$ |
| **Elastic Beanstalk** | Managed platform | Low | $$ |

## 📊 Recommended: ECS Fargate

**Why ECS Fargate:**
- ✅ Serverless - no server management
- ✅ Pay only for running containers
- ✅ Auto-scaling built-in
- ✅ Integrates with AWS services
- ✅ Production-ready with minimal setup
- ✅ Lower cost than EKS

**Architecture:**
```
Internet → ALB (Load Balancer) → ECS Fargate Tasks → RDS PostgreSQL
                                        ↓
                                  CloudWatch Logs
```

## 📚 Detailed Guides

- [ECS Fargate Deployment](./ecs-fargate.md) ⭐ Recommended
- [EKS Deployment](./eks-deployment.md)
- [EC2 Deployment](./ec2-deployment.md)
- [Cost Estimates](./cost-estimates.md)

## 🗄️ Database Options

### Development/Testing
- **H2 In-Memory** (current)
  - No persistence
  - Free
  - Data lost on restart

### Production
- **RDS PostgreSQL** ⭐ Recommended
  - Managed database
  - Automated backups
  - Multi-AZ for high availability
  - ~$25-50/month

- **Aurora Serverless**
  - Auto-scaling
  - Pay per second
  - Great for variable workloads

## 🔐 Security Considerations

1. **VPC Configuration**
   - Private subnets for containers
   - Public subnets for load balancer
   - NAT Gateway for outbound internet

2. **Security Groups**
   - ALB: Allow 80/443 from internet
   - ECS Tasks: Allow 8080 from ALB only
   - RDS: Allow 5432 from ECS only

3. **IAM Roles**
   - ECS Task Role: Access to CloudWatch, RDS
   - ECS Execution Role: Pull images from ECR

4. **Secrets Management**
   - Use AWS Secrets Manager for DB credentials
   - Never hardcode passwords

## 📈 Monitoring & Logging

- **CloudWatch Logs** - Application logs
- **CloudWatch Metrics** - CPU, memory, request count
- **CloudWatch Alarms** - Alert on errors/high CPU
- **X-Ray** - Distributed tracing (optional)

## 💰 Cost Optimization

1. **Use Fargate Spot** - 70% cheaper for non-critical tasks
2. **Right-size resources** - Start small, scale up
3. **Use RDS reserved instances** - 40% savings
4. **Enable auto-scaling** - Scale down during low traffic
5. **Use CloudWatch Logs retention** - Delete old logs

## 🚀 Quick Start

See [ECS Fargate Deployment](./ecs-fargate.md) for step-by-step instructions.