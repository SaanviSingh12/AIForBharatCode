# Complete AWS Lambda Deployment Steps for Sahayak Backend

## Overview
This guide will deploy your Spring Boot healthcare backend to AWS Lambda.
**Cost:** $0 when idle, ~$2-5/month during demo use.

---

## STEP 1: Prerequisites (One-Time Setup)

### 1.1 Install AWS CLI
```bash
brew install awscli
```

Verify installation:
```bash
aws --version
# Should show: aws-cli/2.x.x
```

### 1.2 Install AWS SAM CLI
```bash
brew install aws-sam-cli
```

Verify installation:
```bash
sam --version
# Should show: SAM CLI, version 1.x.x
```

### 1.3 Ensure Docker is Running
Open Docker Desktop and make sure it's running.

Verify:
```bash
docker info
# Should show Docker version and info (not an error)
```

### 1.4 Configure AWS Credentials
```bash
aws configure
```

You'll be prompted for:
- **AWS Access Key ID:** [Get from AWS Console → IAM → Users → Security credentials]
- **AWS Secret Access Key:** [From same place as above]
- **Default region name:** `us-east-1` (recommended)
- **Default output format:** `json`

Verify configuration:
```bash
aws sts get-caller-identity
# Should show your AWS account ID
```

---

## STEP 2: Deploy to AWS Lambda

### 2.1 Navigate to Backend Directory
```bash
cd /Users/I578085/Documents/AIForBharatCode/sahayak-backend
```

### 2.2 Run Deployment Script
```bash
./deploy.sh
```

**What happens:**
1. Builds your Spring Boot app in a Docker container
2. Creates container image with Lambda Web Adapter
3. Uploads to AWS ECR (container registry)
4. Creates Lambda function
5. Creates API Gateway HTTP API
6. Creates S3 bucket for uploads
7. Sets up IAM permissions for Bedrock, Polly, Textract, etc.

**First-time deployment:**
- When asked "Stack Name", press Enter (uses default: sahayak-backend)
- When asked "AWS Region", press Enter (uses: us-east-1)
- When asked "Confirm changes", type `y` and press Enter
- When asked "Allow SAM CLI IAM role creation", type `y` and press Enter
- When asked "Save arguments to configuration file", type `y` and press Enter
- When asked "SAM configuration file", press Enter (uses: samconfig.toml)
- When asked "SAM configuration environment", press Enter (uses: default)

**Deployment time:** 3-5 minutes

### 2.3 Save Your API URL
At the end, you'll see output like:

```
Outputs
-----------------------------------------------------------------
ApiUrl = https://abc123xyz456.execute-api.us-east-1.amazonaws.com/
-----------------------------------------------------------------
```

**IMPORTANT:** Copy this URL! You'll need it in Step 3.

---

## STEP 3: Update Frontend Configuration

### 3.1 Open Frontend API Configuration
```bash
# Open in your editor
open "Healthcare Access Mobile App/src/app/services/api.ts"
```

### 3.2 Update the API URL
Find this line (around line 3-5):
```typescript
const API_BASE_URL = import.meta.env.PROD 
  ? 'http://localhost:8080/api'
  : 'http://localhost:8080/api';
```

Replace with:
```typescript
const API_BASE_URL = import.meta.env.PROD 
  ? 'https://abc123xyz456.execute-api.us-east-1.amazonaws.com/api'  // ← Your Lambda URL + /api
  : 'http://localhost:8080/api';
```

**Important:** Add `/api` at the end of your Lambda URL!

### 3.3 Save the File

---

## STEP 4: Test Your Deployment

### 4.1 Test Health Endpoint
Replace `YOUR_URL` with your actual API URL:

```bash
curl https://abc123xyz456.execute-api.us-east-1.amazonaws.com/api/health
```

**Expected response:**
```json
{"status":"OK","message":"Sahayak Backend is running"}
```

**If you get an error:**
- Wait 30 seconds (first request takes time)
- Try again
- Check if URL ends with `/api/health`

### 4.2 Test Symptom Analysis
```bash
curl -X POST https://abc123xyz456.execute-api.us-east-1.amazonaws.com/api/triage/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "textInput": "I have fever and headache",
    "language": "en"
  }'
```

**Expected response:** JSON with analysis results, hospitals, etc.

### 4.3 Test Doctors Endpoint
```bash
curl https://abc123xyz456.execute-api.us-east-1.amazonaws.com/api/doctors
```

**Expected response:** JSON array of doctors/hospitals

---

## STEP 5: Test Frontend Integration

### 5.1 Start Frontend Development Server
```bash
cd /Users/I578085/Documents/AIForBharatCode/"Healthcare Access Mobile App"
npm run dev
```

### 5.2 Open in Browser
```
http://localhost:5173
```

### 5.3 Test Complete Flow
1. Select language (Hindi/English)
2. Go to Symptom Entry
3. Enter symptoms (text or voice)
4. Click "Analyze Symptoms"
5. Wait for AI analysis
6. Should navigate to Doctor Search page
7. Verify you see the summary banner with "Play Audio" button
8. Click Play Audio to test AWS Polly integration

**What's happening behind the scenes:**
- Frontend sends request to Lambda
- Lambda processes with AWS Bedrock (AI)
- Lambda generates audio with AWS Polly
- Frontend displays results and plays audio

---

## STEP 6: View Logs (Optional)

### 6.1 Live Tail Logs
```bash
cd /Users/I578085/Documents/AIForBharatCode/sahayak-backend
sam logs --tail
```

This shows real-time logs from your Lambda function. Great for debugging!

### 6.2 View Logs in AWS Console
1. Go to: https://console.aws.amazon.com/cloudwatch
2. Click "Log groups" in left sidebar
3. Find `/aws/lambda/sahayak-backend-dev`
4. Click to see all logs

---

## STEP 7: Monitor Costs (Optional)

### 7.1 Check Current Costs
```bash
aws ce get-cost-and-usage \
  --time-period Start=2026-03-01,End=2026-03-31 \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --group-by Type=SERVICE
```

### 7.2 Set Up Billing Alert
1. Go to: https://console.aws.amazon.com/billing
2. Click "Budgets" → "Create budget"
3. Choose "Zero spend budget" or set a custom amount ($10)
4. Enter your email for alerts

---

## STEP 8: Cleanup (When Done with Demo)

### 8.1 Delete All Resources
```bash
cd /Users/I578085/Documents/AIForBharatCode/sahayak-backend
./cleanup.sh
```

Type `yes` when prompted.

**What gets deleted:**
- Lambda function
- API Gateway
- S3 bucket
- CloudWatch logs
- IAM roles
- All CloudFormation resources

**Result:** $0/month charges. Everything is gone.

### 8.2 Verify Deletion
```bash
aws cloudformation list-stacks --stack-status-filter DELETE_COMPLETE | grep sahayak
```

Should show your stack as `DELETE_COMPLETE`.

---

## TROUBLESHOOTING

### Problem: "sam: command not found"
**Solution:**
```bash
brew install aws-sam-cli
```

### Problem: "Error: Docker is not running"
**Solution:**
- Open Docker Desktop
- Wait for it to fully start (whale icon in menu bar)

### Problem: "AccessDenied" errors
**Solution:**
- Check AWS credentials: `aws configure list`
- Ensure your IAM user has these policies:
  - AWSLambda_FullAccess
  - AmazonAPIGatewayAdministrator
  - AmazonS3FullAccess
  - IAMFullAccess
  - CloudFormationFullAccess

### Problem: Lambda always returns 503
**Solution:**
- Check logs: `sam logs --tail`
- Look for Java errors
- Ensure memory is set to 2048MB (in template.yaml)

### Problem: Cold starts (3-5 seconds first request)
**Solution:**
- This is normal for Lambda
- Subsequent requests are fast (<500ms)
- For demos, call health endpoint before presenting to "warm up"

### Problem: CORS errors in frontend
**Solution:**
- Check `sahayak-backend/src/main/java/com/sahayak/config/CorsConfig.java`
- Ensure your Lambda URL is in `allowedOrigins`
- Or use `*` for testing: `.allowedOrigins("*")`

### Problem: Can't find API URL
**Solution:**
```bash
aws cloudformation describe-stacks \
  --stack-name sahayak-backend \
  --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" \
  --output text
```

---

## REDEPLOYMENT (Future Updates)

When you make code changes:

```bash
cd /Users/I578085/Documents/AIForBharatCode/sahayak-backend
./deploy.sh
```

That's it! SAM will:
1. Rebuild your app
2. Push new container image
3. Update Lambda function
4. Keep same API URL (no frontend changes needed)

---

## COST BREAKDOWN

### Free Tier (12 months)
- ✅ 1M Lambda requests/month FREE
- ✅ 400,000 GB-seconds compute FREE
- ✅ More than enough for demos!

### After Free Tier (or if exceeded)
- Lambda: $0.0000166667 per GB-second
- API Gateway: $1.00 per million requests
- S3: $0.023 per GB/month
- Bedrock (AI): ~$0.01-0.03 per request
- Polly (TTS): $4.00 per 1M characters

### Real Demo Cost Example
```
Demo day (2 hours, 200 requests):
  Lambda compute:    $0.03
  API Gateway:       $0.00
  Bedrock calls:     $2.00
  Polly audio:       $0.20
  S3 storage:        $0.01
  ──────────────────────────
  Total:            ~$2.24

When idle (not being used):
  All services:      $0.00
  ──────────────────────────
  Total:             $0.00 🎉
```

---

## NEXT STEPS

1. ✅ Complete Steps 1-3 to deploy
2. ✅ Test with Step 4
3. ✅ Integrate frontend with Step 5
4. 📱 Deploy frontend to Vercel/Netlify (optional)
5. 🎬 Practice your demo
6. 🧹 Run cleanup.sh when done

---

## QUICK REFERENCE COMMANDS

```bash
# Deploy
cd sahayak-backend && ./deploy.sh

# View logs
sam logs --tail

# Test health
curl https://YOUR_URL/api/health

# Delete everything
./cleanup.sh

# Get API URL
aws cloudformation describe-stacks --stack-name sahayak-backend \
  --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" --output text
```

---

## SUPPORT

If you get stuck:
1. Check logs: `sam logs --tail`
2. Check AWS Console: https://console.aws.amazon.com/lambda
3. View CloudFormation: https://console.aws.amazon.com/cloudformation
4. Check deployment guide: `LAMBDA_DEPLOY.md`

**Remember:** For a demo/prototype, you only pay when you use it. Perfect for your use case! 🎉
