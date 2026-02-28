# Sahayak Backend — Spring Boot API

AI-powered health access system for rural India. Java 21 + Spring Boot 3.3 + AWS SDK v2.

---

## 🚀 Quick Start (Mock Mode — No AWS Needed)

```bash
cd sahayak-backend
./mvnw spring-boot:run
```

Server starts at **http://localhost:8080**. All AWS calls are mocked automatically.

Test it:
```bash
curl http://localhost:8080/api/v1/health
```

---

## 🔗 API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/health` | Health check |
| `POST` | `/api/v1/triage` | Voice/text symptom analysis |
| `POST` | `/api/v1/prescription` | Prescription image analysis |

### POST /api/v1/triage

**Form Data (multipart):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `audio` | File | No | Audio file (WebM/WAV/MP3) |
| `directText` | String | No | Text input (bypasses transcription) |
| `language` | String | No | Language code (default: `hi-IN`) |
| `lat` | String | No | User latitude |
| `lng` | String | No | User longitude |

**Response:**
```json
{
  "success": true,
  "symptomText": "मुझे बुखार है",
  "specialist": "General Physician",
  "isEmergency": false,
  "urgencyLevel": "medium",
  "summary": "Patient reports fever...",
  "responseText": "आपको बुखार की समस्या है...",
  "audioBase64": "base64-encoded-mp3...",
  "hospitals": [...]
}
```

### POST /api/v1/prescription

**Form Data (multipart):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `image` | File | **Yes** | Prescription image (JPEG/PNG/PDF) |
| `language` | String | No | Language code (default: `hi-IN`) |
| `lat` | String | No | User latitude |
| `lng` | String | No | User longitude |

**Response:**
```json
{
  "success": true,
  "extractedText": "Paracetamol 500mg...",
  "medicines": [...],
  "totalBrandCost": 310,
  "totalGenericCost": 65,
  "totalSavingsPercent": 79,
  "responseText": "आपकी दवाइयाँ...",
  "audioBase64": "base64-encoded-mp3...",
  "janAushadhiLocations": [...]
}
```

---

## ⚙️ Configuration

### Mock Mode (default)
No AWS credentials required. All services return realistic mock data.

```yaml
# application.yml
sahayak:
  use-real-aws: false  # default
```

### Real AWS Mode
Set environment variable before starting:

```bash
export USE_REAL_AWS=true
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
./mvnw spring-boot:run
```

Or create an `.env` file and use:
```bash
USE_REAL_AWS=true AWS_REGION=ap-south-1 ./mvnw spring-boot:run
```

---

## 🏗️ Project Structure

```
src/main/java/com/sahayak/
├── SahayakApplication.java       # Entry point
├── config/
│   ├── AwsConfig.java            # AWS SDK v2 client beans
│   └── CorsConfig.java           # CORS for frontend (localhost:5173)
├── controller/
│   ├── HealthController.java     # GET /api/v1/health
│   ├── TriageController.java     # POST /api/v1/triage
│   └── PrescriptionController.java # POST /api/v1/prescription
├── service/
│   ├── VoiceTriageService.java   # Orchestrates triage flow
│   ├── PrescriptionService.java  # Orchestrates prescription flow
│   ├── BedrockService.java       # Claude 3.5 Sonnet AI analysis
│   ├── TextractService.java      # OCR for prescription images
│   ├── PollyService.java         # Text-to-speech (Hindi/regional)
│   ├── TranscribeService.java    # Speech-to-text
│   ├── S3Service.java            # Temp file storage
│   └── MockDataService.java      # Mock hospitals/pharmacies/medicines
└── model/
    ├── TriageRequest.java
    ├── TriageResponse.java
    ├── HospitalDto.java
    ├── PrescriptionResponse.java
    ├── MedicineDto.java
    └── PharmacyDto.java

src/main/resources/
├── application.yml
└── data/
    ├── mock-hospitals.json
    ├── mock-pharmacies.json
    └── mock-medicines.json
```

---

## 🔑 AWS Services Used

| Service | Purpose | Free Tier |
|---------|---------|-----------|
| **Bedrock** (Claude 3.5) | Symptom analysis + medicine parsing | Pay per token |
| **Transcribe** | Hindi/Tamil voice → text | 60 min/month free |
| **Textract** | Prescription OCR | 1,000 pages/month free |
| **Polly** (Kajal) | Hindi TTS responses | 5M chars/month free |
| **S3** | Temp file storage | 5 GB free |

All in region: **ap-south-1 (Mumbai)**

---

## 🛠️ Prerequisites

- **Java 21** (`java -version`)
- **Maven 3.8+** or use included `./mvnw`

### Install Java 21 (macOS)
```bash
brew install openjdk@21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
```

### Run
```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

### Build JAR
```bash
./mvnw clean package -DskipTests
java -jar target/sahayak-backend-1.0.0.jar
```

---

## 🧪 Test Endpoints

```bash
# Health check
curl http://localhost:8080/api/v1/health

# Triage with text
curl -X POST http://localhost:8080/api/v1/triage \
  -F "directText=मुझे सिरदर्द और बुखार है" \
  -F "language=hi-IN"

# Triage with audio file
curl -X POST http://localhost:8080/api/v1/triage \
  -F "audio=@recording.webm" \
  -F "language=hi-IN"

# Prescription analysis
curl -X POST http://localhost:8080/api/v1/prescription \
  -F "image=@prescription.jpg" \
  -F "language=hi-IN"
```

---

## 🌐 Frontend Connection

The React frontend at `Healthcare Access Mobile App/` connects to this backend via:

```
VITE_API_URL=http://localhost:8080
```

Set in `Healthcare Access Mobile App/.env.local`.

Start both together:
```bash
# Terminal 1 — Backend
cd sahayak-backend && ./mvnw spring-boot:run

# Terminal 2 — Frontend  
cd "Healthcare Access Mobile App" && npm run dev
```

Frontend: http://localhost:5173  
Backend: http://localhost:8080
