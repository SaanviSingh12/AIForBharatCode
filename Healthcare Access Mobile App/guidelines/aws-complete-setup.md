# 🩺 Sahayak — Complete AWS Setup & Running Guide

> **Everything you need** to connect all AWS services, run the backend, and launch the frontend — end to end.

---

## 📋 Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│  FRONTEND (React + Vite)    http://localhost:5173            │
│  ├── Symptom Entry (Voice / Text)                            │
│  ├── Prescription Upload (Camera / Gallery)                  │
│  └── Doctor Search, Emergency Mode                           │
│                         │ API calls                          │
│                         ▼                                    │
│  BACKEND (Spring Boot)   http://localhost:8080               │
│  ├── /api/v1/triage         → Bedrock + Polly + Hospitals    │
│  ├── /api/v1/prescription   → S3 + Textract + Bedrock + Polly│
│  ├── /api/v1/prescriptionText → Bedrock + Polly              │
│  ├── /api/v1/doctors        → Mock data                      │
│  └── /api/v1/health         → Health check                   │
│                         │                                    │
│                         ▼                                    │
│  AWS SERVICES                                                │
│  ├── Amazon Bedrock (Nova Lite) — us-east-1                  │
│  ├── Amazon Polly (TTS)         — ap-south-1                 │
│  ├── Amazon S3 (temp storage)   — ap-south-1                 │
│  ├── Amazon Textract (OCR)      — ap-south-1                 │
│  ├── Amazon Transcribe (STT)    — ap-south-1                 │
│  └── Amazon DynamoDB (sessions) — ap-south-1                 │
└──────────────────────────────────────────────────────────────┘
```

### Region Map

| AWS Service        | Region        | Why?                                            |
|--------------------|---------------|-------------------------------------------------|
| **Bedrock**        | `us-east-1`   | Nova Lite model available here, NOT in ap-south-1 |
| **Polly**          | `ap-south-1`  | Supports Indian voices (Kajal), low latency      |
| **S3**             | `ap-south-1`  | Closest to Indian users                         |
| **Textract**       | `ap-south-1`  | Available in Mumbai region                      |
| **Transcribe**     | `ap-south-1`  | Supports hi-IN, ta-IN, en-IN languages          |
| **DynamoDB**       | `ap-south-1`  | Low-latency session storage                     |

---

## STEP 1: Install Prerequisites

### 1.1 — Install AWS CLI
```bash
# macOS
brew install awscli

# Verify
aws --version
```

### 1.2 — Install Java 21 & Maven
```bash
# macOS
brew install openjdk@21 maven

# Verify
java --version   # Should show 21.x
mvn --version    # Should show 3.x
```

### 1.3 — Install Node.js (for frontend)
```bash
brew install node

# Verify
node --version   # Should show 18+ or 20+
npm --version
```

---

## STEP 2: Configure AWS CLI Credentials

### 2.1 — Create IAM User (if not done)

1. Go to **AWS Console → IAM → Users → Create user**
2. User name: `Sahayak_Dev` (or your name)
3. Attach these **managed policies**:
   - `AmazonBedrockFullAccess`
   - `AmazonS3FullAccess`
   - `AmazonTextractFullAccess`
   - `AmazonTranscribeFullAccess`
   - `AmazonPollyFullAccess`
   - `AmazonDynamoDBFullAccess`
4. Go to **Security Credentials → Create access key → CLI**
5. Save the **Access Key ID** and **Secret Access Key**

### 2.2 — Configure AWS CLI

```bash
aws configure
```

Enter:
```
AWS Access Key ID:     <your-access-key-id>
AWS Secret Access Key: <your-secret-access-key>
Default region name:   ap-south-1
Default output format: json
```

### 2.3 — Verify credentials work
```bash
aws sts get-caller-identity
```
Expected output:
```json

```

---

## STEP 3: Enable Amazon Bedrock Model

### 3.1 — Enable Nova Lite in us-east-1

1. Go to: **AWS Console → Amazon Bedrock** (make sure region is **US East - N. Virginia / us-east-1**)
2. Left sidebar → **Model access** → **Manage model access**
3. Find **Amazon → Nova Lite** → Check it → **Save changes**
4. Wait 1-2 minutes for it to activate

### 3.2 — Verify model access
```bash
aws bedrock list-foundation-models \
  --region us-east-1 \
  --query "modelSummaries[?modelId=='amazon.nova-lite-v1:0'].{id:modelId,name:modelName,status:modelLifecycle.status}" \
  --output table
```

### 3.3 — Test the model works
```bash
aws bedrock-runtime invoke-model \
  --region us-east-1 \
  --model-id amazon.nova-lite-v1:0 \
  --content-type "application/json" \
  --body '{"messages":[{"role":"user","content":[{"text":"Say hello in Hindi"}]}],"inferenceConfig":{"maxTokens":100}}' \
  /tmp/nova-test.json && cat /tmp/nova-test.json
```

You should see a JSON response with Hindi text. ✅

---

## STEP 4: Create S3 Bucket

### 4.1 — Create the bucket

```bash
aws s3 mb s3://sahayak-prescriptions-temp --region ap-south-1
```

### 4.2 — Verify
```bash
aws s3 ls | grep sahayak
```

> **Note:** The app uses S3 for **temporary** storage only — files are uploaded for Textract/Transcribe processing, then immediately deleted.

---

## STEP 5: Create DynamoDB Table

```bash
aws dynamodb create-table \
  --table-name sahayak-sessions \
  --attribute-definitions AttributeName=sessionId,AttributeType=S \
  --key-schema AttributeName=sessionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-south-1
```

### Verify
```bash
aws dynamodb describe-table --table-name sahayak-sessions --region ap-south-1 \
  --query "Table.{Name:TableName,Status:TableStatus}"
```

---

## STEP 6: Verify Polly Works

```bash
aws polly synthesize-speech \
  --region ap-south-1 \
  --output-format mp3 \
  --voice-id Kajal \
  --engine neural \
  --language-code hi-IN \
  --text "नमस्ते, साहायक आपकी सेवा में है" \
  /tmp/polly-test.mp3

# Play the audio (macOS)
afplay /tmp/polly-test.mp3
```

You should hear Hindi speech. ✅

---

## STEP 7: Verify Textract Works

```bash
# Take a photo of any prescription or use a test image
# Then run:
aws textract detect-document-text \
  --region ap-south-1 \
  --document '{"Bytes": "'$(base64 -i /path/to/prescription.jpg)'"}'  \
  --query "Blocks[?BlockType=='LINE'].Text" \
  --output text
```

> If you don't have a prescription image handy, this will be tested end-to-end through the app.

---

## STEP 8: Run the Backend

### 8.1 — Navigate to backend
```bash
cd /Users/I578082/Desktop/AIForBharat/sahayak-backend
```

### 8.2 — Build the project
```bash
mvn clean compile -q
```

### 8.3 — Run with real AWS enabled
```bash
USE_REAL_AWS=true BEDROCK_REGION=us-east-1 mvn spring-boot:run
```

### What each env variable does:
| Variable          | Value       | Purpose                                     |
|-------------------|-------------|---------------------------------------------|
| `USE_REAL_AWS`    | `true`      | Enables real Bedrock, S3, Textract, Polly   |
| `USE_REAL_AWS`    | `false`     | Uses mock data only (no AWS calls)          |
| `BEDROCK_REGION`  | `us-east-1` | Points Bedrock client to correct region     |

### 8.4 — Verify backend is running
Open a **new terminal tab** and run:
```bash
curl -s http://localhost:8080/api/v1/health | python3 -m json.tool
```

Expected:
```json
{
    "status": "UP",
    "service": "sahayak-backend"
}
```

---

## STEP 9: Test Backend API Endpoints

> ⚠️ Keep the server running in its own terminal. Run these tests in a **separate terminal**.

### 9.1 — Test Triage (Symptom Analysis)
```bash
curl -s -X POST http://localhost:8080/api/v1/triage \
  -F "language=en-IN" \
  -F "directText=I have severe headache and fever for 3 days" \
  | python3 -m json.tool
```

**What happens behind the scenes:**
```
Text Input → Bedrock (Nova Lite) analyzes symptoms
           → Returns specialist + urgency
           → MockDataService finds matching hospitals
           → Polly converts response to audio
           → Returns JSON with everything
```

### 9.2 — Test Prescription (Text Input)
```bash
curl -s -X POST http://localhost:8080/api/v1/prescriptionText \
  -d "prescription=Tab Crocin 500mg twice daily, Cap Amoxicillin 250mg thrice daily" \
  -d "language=en-IN" \
  | python3 -m json.tool
```

**What happens behind the scenes:**
```
Prescription Text → Bedrock maps brands to generics
                  → Calculates price savings
                  → Polly converts response to audio
                  → Returns medicines + pharmacies
```

### 9.3 — Test Prescription (Image Upload — requires S3 + Textract)
```bash
# Only works with USE_REAL_AWS=true and a real prescription image
curl -s -X POST http://localhost:8080/api/v1/prescription \
  -F "image=@/path/to/prescription.jpg" \
  -F "language=en-IN" \
  | python3 -m json.tool
```

**What happens behind the scenes:**
```
Image → S3 upload → Textract OCR extracts text
      → Bedrock maps brands to generics
      → Polly converts response to audio
      → S3 file deleted (cleanup)
      → Returns medicines + pharmacies
```

### 9.4 — Test Doctors API
```bash
# All doctors
curl -s http://localhost:8080/api/v1/doctors | python3 -m json.tool

# Government doctors only
curl -s http://localhost:8080/api/v1/doctors/government | python3 -m json.tool

# By specialty
curl -s http://localhost:8080/api/v1/doctors/specialty/Cardiologist | python3 -m json.tool
```

---

## STEP 10: Run the Frontend

### 10.1 — Open a new terminal
```bash
cd "/Users/I578082/Desktop/AIForBharat/Healthcare Access Mobile App"
```

### 10.2 — Install dependencies
```bash
npm install
```

### 10.3 — Check .env.local
The file already has:
```
VITE_API_URL=http://localhost:8080
```
This tells the frontend to call the local backend.

### 10.4 — Start the frontend
```bash
npm run dev
```

### 10.5 — Open in browser
Go to: **http://localhost:5173**

---

## STEP 11: End-to-End Testing in the App

### Flow 1: Voice/Text Symptom Triage
1. Open the app → **Select Language** (e.g., English/Hindi)
2. Tap **"Describe Symptoms"**
3. Either **speak** (voice recording) or **type** your symptoms
4. Hit **Submit**
5. The app calls `POST /api/v1/triage` →
   - Audio → S3 → Transcribe → text (if voice)
   - Text → Bedrock Nova Lite → AI analysis
   - Analysis → Hospital lookup
   - Response → Polly TTS → audio playback
6. You see: **Specialist recommendation + Nearby hospitals + Audio response**

### Flow 2: Prescription Scanner
1. Tap **"Scan Prescription"**
2. **Take a photo** or upload a prescription image
3. Hit **Submit**
4. The app calls `POST /api/v1/prescription` →
   - Image → S3 → Textract OCR → extracted text
   - Text → Bedrock → Generic alternatives + savings
   - Response → Polly TTS
5. You see: **Medicine list with generic prices + Savings + Jan Aushadhi pharmacy locations**

### Flow 3: Doctor Search
1. Tap **"Find Doctors"**
2. Search by name or specialty
3. Filter by government/private
4. See PMJAY-eligible doctors

---

## 📁 Key Configuration Files

| File | Purpose |
|------|---------|
| `sahayak-backend/src/main/resources/application.yml` | All AWS config, regions, model ID, feature flags |
| `sahayak-backend/src/main/java/com/sahayak/config/AwsConfig.java` | Creates AWS SDK clients with correct regions |
| `Healthcare Access Mobile App/.env.local` | Frontend → Backend URL (`VITE_API_URL`) |
| `~/.aws/credentials` | Your AWS access key & secret (local machine) |
| `~/.aws/config` | Default region setting |

---

## 🔧 AWS Service Details for Your Account

| Setting | Value |
|---------|-------|
| **AWS Account** | `361151528355` |
| **IAM User** | `Saanvi_Singh` |
| **General Region** | `ap-south-1` (Mumbai) |
| **Bedrock Region** | `us-east-1` (N. Virginia) |
| **Bedrock Model** | `amazon.nova-lite-v1:0` (Amazon Nova Lite) |
| **S3 Bucket** | `sahayak-prescriptions-temp` |
| **DynamoDB Table** | `sahayak-sessions` |
| **Polly Voice** | `Kajal` (Neural, Hindi/English Indian) |

---

## 🏃 Quick Start (TL;DR)

```bash
# Terminal 1 — Backend
cd /Users/I578082/Desktop/AIForBharat/sahayak-backend
USE_REAL_AWS=true BEDROCK_REGION=us-east-1 mvn spring-boot:run

# Terminal 2 — Frontend
cd "/Users/I578082/Desktop/AIForBharat/Healthcare Access Mobile App"
npm run dev

# Terminal 3 — Test
curl -s http://localhost:8080/api/v1/health | python3 -m json.tool
```

Then open **http://localhost:5173** in your browser.

---

## ⚠️ Troubleshooting

| Issue | Fix |
|-------|-----|
| `UnrecognizedClientException` | Run `aws configure` and re-enter credentials |
| `AccessDeniedException` on Bedrock | Add `AmazonBedrockFullAccess` policy to your IAM user |
| `NoSuchBucket` on S3 | Run `aws s3 mb s3://sahayak-prescriptions-temp --region ap-south-1` |
| `Model not found` on Bedrock | Enable Nova Lite in Bedrock console (us-east-1) |
| Frontend can't reach backend | Check `.env.local` has `VITE_API_URL=http://localhost:8080` |
| CORS error in browser | Backend has `CorsConfig.java` allowing `localhost:5173` |
| Polly returns null | Polly gracefully degrades — text response still works |
| `Port 8080 already in use` | Kill old process: `lsof -ti:8080 \| xargs kill -9` |

---

## 💰 AWS Cost Estimate (Development)

| Service | Pricing | Monthly Est. |
|---------|---------|--------------|
| **Bedrock (Nova Lite)** | $0.06/1M input + $0.24/1M output tokens | ~$0.50 |
| **Polly** | $4/1M characters (Neural) | ~$0.10 |
| **S3** | $0.023/GB | ~$0.01 |
| **Textract** | $1.50/1000 pages | ~$0.15 |
| **Transcribe** | $0.024/minute | ~$0.10 |
| **DynamoDB** | Pay-per-request | ~$0.01 |
| **Total** | | **~$1/month for dev** |

> All services have a **free tier** for new AWS accounts (12 months).
