# Deploy Sahayak to AWS Lambda

## Quick Deploy (3 steps)

### 1. Install Tools (one-time)
```bash
brew install awscli aws-sam-cli
aws configure  # Enter your AWS credentials
```

### 2. Deploy
```bash
cd sahayak-backend
./deploy.sh
```

### 3. Update Frontend
Copy the API URL from deploy output and update:

**File:** `Healthcare Access Mobile App/src/app/services/api.ts`

```typescript
const API_BASE_URL = import.meta.env.PROD 
  ? 'https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/api'
  : 'http://localhost:8080/api';
```

---

## Costs (Demo/Prototype)

- **When idle:** $0/month
- **During demos:** ~$2-5/month
- **vs EC2 24/7:** $40+/month

Only pay when you use it! Perfect for demos.

---

## Cleanup (Stop All Charges)

```bash
sam delete
```

Deletes everything. Redeploy anytime with `./deploy.sh`

---

## Test Your Deployment

```bash
# Health check
curl https://YOUR_URL.execute-api.us-east-1.amazonaws.com/api/health

# Symptom analysis
curl -X POST https://YOUR_URL/api/triage/analyze \
  -H "Content-Type: application/json" \
  -d '{"textInput":"fever and headache","language":"en"}'
```

---

## View Logs

```bash
sam logs --tail
```

Or: AWS Console → CloudWatch → Log groups
