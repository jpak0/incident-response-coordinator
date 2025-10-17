# AWS Cost Estimates

Detailed cost breakdown for running Incident Response Coordinator on AWS.

## 💵 Monthly Cost Comparison

| Deployment Type | Cost/Month | Best For |
|----------------|------------|----------|
| **Dev/Test** | $30-80 | Learning, demos |
| **Small Production** | $100-200 | <10k requests/day |
| **Medium Production** | $200-500 | 10k-100k requests/day |
| **Large Production** | $500-2000 | >100k requests/day |

---

## 🧮 Development/Testing Setup

**Configuration:**
- 1 ECS Fargate task (0.5 vCPU, 1GB RAM)
- Application Load Balancer
- H2 in-memory database (no RDS)
- Minimal data transfer

**Cost Breakdown:**
```
ECS Fargate:
  - CPU: 0.5 vCPU × $0.04048/hour × 730 hours = $14.78
  - Memory: 1 GB × $0.004445/hour × 730 hours = $3.24
  
Application Load Balancer:
  - Fixed: $16.20/month
  - LCU: ~$3/month (low traffic)
  
Data Transfer:
  - First 1GB free
  - Next 9GB × $0.09/GB = $0.81
  
Total: ~$38/month
```

---

## 🏢 Small Production Setup

**Configuration:**
- 2 ECS Fargate tasks (0.5 vCPU, 1GB RAM each)
- Application Load Balancer
- RDS db.t3.micro PostgreSQL (Single-AZ)
- Automated backups (7 days)
- CloudWatch logs

**Cost Breakdown:**
```
ECS Fargate (2 tasks):
  - CPU: 1 vCPU × $0.04048/hour × 730 hours = $29.55
  - Memory: 2 GB × $0.004445/hour × 730 hours = $6.49
  
Application Load Balancer:
  - Fixed: $16.20/month
  - LCU: ~$5/month
  
RDS db.t3.micro:
  - Instance: $0.017/hour × 730 hours = $12.41
  - Storage: 20 GB × $0.115/GB = $2.30
  - Backups: 20 GB × $0.095/GB = $1.90
  
CloudWatch Logs:
  - Ingestion: 5 GB × $0.50/GB = $2.50
  - Storage: 5 GB × $0.03/GB = $0.15
  
Data Transfer: ~$5/month
  
Total: ~$82/month
```

---

## 🚀 Medium Production Setup

**Configuration:**
- 4 ECS Fargate tasks (1 vCPU, 2GB RAM each)
- Application Load Balancer with SSL
- RDS db.t3.small Multi-AZ
- NAT Gateway
- Auto-scaling enabled
- Enhanced monitoring

**Cost Breakdown:**
```
ECS Fargate (4 tasks):
  - CPU: 4 vCPU × $0.04048/hour × 730 hours = $118.20
  - Memory: 8 GB × $0.004445/hour × 730 hours = $25.94
  
Application Load Balancer:
  - Fixed: $16.20/month
  - LCU: ~$10/month
  
RDS db.t3.small Multi-AZ:
  - Instance: $0.068/hour × 730 hours × 2 = $99.28
  - Storage: 50 GB × $0.115/GB × 2 = $11.50
  - Backups: 50 GB × $0.095/GB = $4.75
  
NAT Gateway:
  - Instance: $0.045/hour × 730 hours = $32.85
  - Data: 10 GB × $0.045/GB = $0.45
  
CloudWatch:
  - Logs: ~$5/month
  - Metrics & Alarms: ~$2/month
  
Data Transfer: ~$20/month
  
Total: ~$346/month
```

---

## 📊 Cost Optimization Tips

### 1. Use Fargate Spot (70% cheaper)
```bash
# Good for non-critical workloads
# Can save ~$80/month on medium setup
```

### 2. RDS Reserved Instances (40% savings)
```bash
# 1-year commitment: 40% off
# 3-year commitment: 60% off
# Saves ~$40/month on db.t3.small
```

### 3. Savings Plans
```bash
# Compute Savings Plan: Up to 66% off
# EC2 Instance Savings Plan: Up to 72% off
```

### 4. Right-Sizing
```bash
# Start with minimal resources
# Scale up based on actual usage
# Can save 30-50% by not over-provisioning
```

### 5. Auto-Scaling
```bash
# Scale down during off-peak hours
# Example: 4 tasks during day, 2 at night
# Saves ~$60/month
```

### 6. CloudWatch Logs Retention
```bash
# Set retention to 7 days instead of infinite
# Saves ~$5-10/month
```

---

## 💡 Free Tier Benefits

**First 12 months (new AWS accounts):**
```
ECS/Fargate: Not included in free tier

RDS:
  - 750 hours/month of db.t2.micro, db.t3.micro, or db.t4g.micro
  - 20 GB of storage
  - 20 GB of backups
  
ALB:
  - 750 hours/month
  - 15 GB of data processing
  
CloudWatch:
  - 10 custom metrics
  - 10 alarms
  - 5 GB log ingestion
  
Data Transfer:
  - 1 GB/month free
  - 15 GB/month outbound (aggregated)
```

**Estimated first-year cost with free tier: ~$20-30/month**

---

## 🎯 Cost Comparison: ECS vs EKS

| Feature | ECS Fargate | EKS |
|---------|-------------|-----|
| **Control Plane** | Free | $0.10/hour ($73/month) |
| **Worker Nodes** | Pay per task | EC2 or Fargate |
| **Minimum Cost** | ~$30/month | ~$120/month |
| **Best For** | AWS-native apps | K8s portability |

**For this project: ECS Fargate is more cost-effective**

---

## 📈 Scaling Cost Examples

### Scenario 1: Startup (1000 requests/day)
- 1 ECS task
- db.t3.micro RDS
- **Cost: ~$40/month**

### Scenario 2: Growing (10,000 requests/day)
- 2 ECS tasks
- db.t3.small RDS
- **Cost: ~$90/month**

### Scenario 3: Production (100,000 requests/day)
- 4-8 ECS tasks with auto-scaling
- db.t3.medium Multi-AZ RDS
- **Cost: ~$350/month**

### Scenario 4: Enterprise (1M+ requests/day)
- 10-20 ECS tasks
- db.r5.large Multi-AZ RDS
- WAF, CloudFront CDN
- **Cost: ~$1500-2000/month**

---

## 🧾 Sample Invoice Breakdown

**Medium Production (1 month):**
```
AWS Invoice - Incident Response System

ECS Fargate:
  Task CPU (4 vCPU)              $118.20
  Task Memory (8 GB)              $25.94

Elastic Load Balancing:
  Application Load Balancer       $16.20
  Load Balancer Capacity Units    $10.00

RDS PostgreSQL:
  db.t3.small Multi-AZ            $99.28
  Storage (50 GB)                 $11.50
  Automated Backups               $4.75

VPC:
  NAT Gateway Hours               $32.85
  NAT Gateway Data Processing     $0.45

CloudWatch:
  Logs Ingestion (10 GB)          $5.00
  Logs Storage                    $0.30
  Metrics & Alarms                $2.00

Data Transfer:
  Out to Internet                 $20.00

────────────────────────────────────────
Subtotal:                         $346.47
Tax (varies by region):           $0.00
────────────────────────────────────────
Total:                            $346.47
```

---

## 🎓 For BAE Interview

**Key points to mention:**

1. **Cost-conscious design**
   - Started with minimal resources
   - Auto-scaling prevents over-provisioning
   - Can run for <$100/month in production

2. **Optimization strategies**
   - Fargate Spot for 70% savings
   - RDS reserved instances
   - CloudWatch log retention policies

3. **Scaling economics**
   - Linear cost scaling with load
   - Can handle 10x traffic for 3x cost
   - Auto-scaling provides cost efficiency

4. **ROI for BAE**
   - $300-500/month operational cost
   - Replaces manual incident tracking
   - Reduces response time
   - Complete audit trail for compliance