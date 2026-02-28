# Sahayak — Backend & AWS Setup Guide
### Java Spring Boot + AWS Services for Hackathon POC
> Generated: 28 February 2026

Steps to run
cd /Users/I578082/Desktop/AIForBharat/sahayak-backend
mvn spring-boot:run

cd "/Users/I578082/Desktop/AIForBharat/Healthcare Access Mobile App"
npm run dev

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Phase 1 — Local Java Backend Setup](#phase-1--local-java-backend-setup)
3. [Phase 2 — Implement AWS Service Integrations](#phase-2--implement-aws-service-integrations)
4. [Phase 3 — AWS Console Setup](#phase-3--aws-console-setup)
5. [Phase 4 — Connect Frontend to Backend](#phase-4--connect-frontend-to-backend)
6. [Phase 5 — Deploy to AWS](#phase-5--deploy-to-aws)
7. [Hackathon Mock Strategy](#hackathon-mock-strategy)
8. [Day-by-Day Checklist](#day-by-day-checklist)
9. [Key Tips & Gotchas](#key-tips--gotchas)

---

## Architecture Overview

```
React Frontend (Vite + TypeScript) ✅ Already Built
          ↓  HTTP/REST (multipart form data)
   AWS API Gateway  (or direct EC2/Beanstalk URL)
          ↓
Java Spring Boot Backend (EC2 / Elastic Beanstalk)
          ↓
┌─────────────────────────────────────────────────────────┐
│                     AWS Services                        │
│                                                         │
│  Amazon Bedrock (Claude 3.5 Sonnet) — AI Brain          │
│  Amazon Transcribe — Voice → Text (Hindi/Tamil)         │
│  Amazon Textract  — Prescription OCR                    │
│  Amazon Polly     — Text → Voice (TTS)                  │
│  Amazon S3        — Temp image storage                  │
│  Amazon DynamoDB  — Session caching                     │
│  Amazon Location  — Distance calculations               │
└─────────────────────────────────────────────────────────┘
          ↓
External: UHI / Beckn Protocol (mocked for hackathon)
```

### Two Core API Endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/v1/triage` | POST | Voice symptoms → AI analysis → doctor list |
| `/api/v1/prescription` | POST | Prescription image → medicine prices |
| `/api/v1/health` | GET | Health check |

---

## Phase 1 — Local Java Backend Setup

### Step 1: Generate the Spring Boot Project

Go to **[https://start.spring.io](https://start.spring.io)** and configure:

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.3.x (latest stable) |
| Java | 21 |
| Group | `com.sahayak` |
| Artifact | `sahayak-backend` |
| Packaging | Jar |

**Add these dependencies on the site:**
- Spring Web
- Spring Boot DevTools
- Lombok
- Spring Validation

Click **Generate**, download and unzip the project.

---

### Step 2: Add AWS SDK Dependencies to `pom.xml`

Open `pom.xml` and add the AWS SDK BOM inside `<dependencyManagement>`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.25.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then add individual AWS SDK modules inside `<dependencies>`:

```xml
<!-- Amazon Bedrock (Claude 3.5 Sonnet) -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>bedrockruntime</artifactId>
</dependency>

<!-- Amazon Transcribe (Speech-to-Text) -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>transcribe</artifactId>
</dependency>

<!-- Amazon Textract (OCR) -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>textract</artifactId>
</dependency>

<!-- Amazon Polly (Text-to-Speech) -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>polly</artifactId>
</dependency>

<!-- Amazon S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
</dependency>

<!-- Amazon DynamoDB -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>dynamodb</artifactId>
</dependency>

<!-- Amazon Location Service -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>location</artifactId>
</dependency>

<!-- JSON processing for Bedrock payloads -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

### Step 3: Create the Project Folder Structure

Create this directory layout under `src/main/java/com/sahayak/`:

```
sahayak-backend/
└── src/
    └── main/
        ├── java/com/sahayak/
        │   ├── SahayakApplication.java          ← Main entry point (auto-generated)
        │   │
        │   ├── controller/
        │   │   ├── TriageController.java         ← POST /api/v1/triage
        │   │   ├── PrescriptionController.java   ← POST /api/v1/prescription
        │   │   └── HealthController.java         ← GET  /api/v1/health
        │   │
        │   ├── service/
        │   │   ├── VoiceTriageService.java       ← Orchestrates full voice flow
        │   │   ├── PrescriptionService.java      ← Orchestrates full prescription flow
        │   │   ├── TranscribeService.java        ← AWS Transcribe wrapper
        │   │   ├── TextractService.java          ← AWS Textract wrapper
        │   │   ├── BedrockService.java           ← AWS Bedrock / Claude 3.5 Sonnet
        │   │   ├── PollyService.java             ← AWS Polly TTS
        │   │   ├── S3Service.java               ← S3 temp upload/delete
        │   │   └── MedicineService.java          ← Brand-to-generic mapping
        │   │
        │   ├── model/
        │   │   ├── TriageRequest.java
        │   │   ├── TriageResponse.java
        │   │   ├── PrescriptionRequest.java
        │   │   ├── PrescriptionResponse.java
        │   │   ├── Doctor.java
        │   │   ├── Hospital.java
        │   │   └── MedicineComparison.java
        │   │
        │   └── config/
        │       ├── AwsConfig.java               ← AWS SDK bean wiring
        │       └── CorsConfig.java              ← Allow React frontend origin
        │
        └── resources/
            ├── application.yml                  ← App configuration
            └── data/
                ├── mock-hospitals.json          ← Mock UHI data
                └── mock-medicines.json          ← Mock Jan Aushadhi data
```

---

### Step 4: Configure `application.yml`

Replace the contents of `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

aws:
  region: ap-south-1      # Mumbai — best for India latency
  s3:
    bucket: sahayak-prescriptions-temp
  dynamodb:
    table-name: sahayak-sessions
  bedrock:
    model-id: anthropic.claude-3-5-sonnet-20240620-v1:0

spring:
  application:
    name: sahayak-backend
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

logging:
  level:
    com.sahayak: DEBUG
    software.amazon.awssdk: WARN
```

---

### Step 5: AWS Configuration Bean (`AwsConfig.java`)

```java
package com.sahayak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public TranscribeClient transcribeClient() {
        return TranscribeClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}
```

---

### Step 6: CORS Configuration (`CorsConfig.java`)

```java
package com.sahayak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",    // Vite dev server
                    "http://localhost:4173",    // Vite preview
                    "*"                         // Replace with your deployed frontend URL
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

---

## Phase 2 — Implement AWS Service Integrations

**Priority order for hackathon (build in this sequence):**

| Priority | Service | Impact |
|---|---|---|
| 🔴 1st | Amazon Bedrock | Core AI — symptom analysis & medicine mapping |
| 🔴 2nd | Amazon Transcribe | Voice input in Hindi/Tamil |
| 🔴 3rd | Amazon Textract | Prescription OCR |
| 🟡 4th | Amazon Polly | Audio responses |
| 🟡 5th | Amazon S3 | Prescription image temp storage |
| 🟢 6th | Amazon DynamoDB | Session caching |
| 🟢 7th | Amazon Location | Distance calculations |

---

### BedrockService.java (Most Important)

```java
package com.sahayak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

@Service
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.bedrock.model-id}")
    private String modelId;

    public BedrockService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
    }

    /**
     * Analyzes symptoms and returns specialist type + emergency flag
     */
    public Map<String, Object> analyzeSymptoms(String symptomText, String language, String location) {
        String prompt = """
            You are Sahayak, a healthcare triage assistant for rural India.
            A patient describes their symptoms in %s: "%s"
            Their location is: %s
            
            Analyze the symptoms and respond ONLY with this exact JSON format:
            {
              "specialist": "General Physician",
              "isEmergency": false,
              "urgencyLevel": "low",
              "summary": "Brief English summary of the issue",
              "responseInLanguage": "Response to patient in their language (%s)",
              "searchKeyword": "keyword to search in UHI network"
            }
            
            specialist must be one of: General Physician, Cardiologist, Dermatologist,
            Pediatrician, Gynecologist, Orthopedic, ENT, Ophthalmologist, Dentist,
            Psychiatrist, Gastroenterologist, Neurologist
            
            isEmergency = true if: chest pain, difficulty breathing, severe bleeding,
            loss of consciousness, stroke symptoms, severe allergic reaction.
            urgencyLevel: "emergency", "urgent", or "low"
            """.formatted(language, symptomText, location, language);

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 1024,
                "messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
                }
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            // Parse the Claude response to extract the JSON content
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            // Extract text from Claude's response structure
            // Claude returns: { "content": [{ "text": "..." }] }
            String textContent = extractTextFromClaudeResponse(responseMap);
            return objectMapper.readValue(textContent, Map.class);

        } catch (Exception e) {
            // Fallback for demo stability
            return Map.of(
                "specialist", "General Physician",
                "isEmergency", false,
                "urgencyLevel", "low",
                "summary", "Symptoms noted. Recommending General Physician.",
                "responseInLanguage", "Aapke lakshan sun liye gaye hain. Kripya ek doctor se mile.",
                "searchKeyword", "General Physician"
            );
        }
    }

    /**
     * Processes extracted medicine names and maps brands to generics
     */
    public Map<String, Object> processMedicines(String extractedText, String language) {
        String prompt = """
            You are a pharmacy assistant for rural India.
            The following text was extracted from a prescription: "%s"
            
            Extract all medicine names and respond ONLY with this JSON:
            {
              "medicines": [
                {
                  "brandName": "Crocin",
                  "genericName": "Paracetamol",
                  "dosage": "500mg",
                  "brandPrice": 45,
                  "genericPrice": 8,
                  "savingsPercent": 82
                }
              ],
              "totalBrandCost": 150,
              "totalGenericCost": 28,
              "totalSavingsPercent": 81,
              "responseInLanguage": "Response in %s language"
            }
            
            For brandPrice and genericPrice use realistic Indian market prices in INR.
            Generic price should be the Jan Aushadhi Kendra price (typically 50-90%% cheaper).
            """.formatted(extractedText, language);

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 2048,
                "messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
                }
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String textContent = extractTextFromClaudeResponse(
                objectMapper.readValue(response.body().asUtf8String(), Map.class)
            );
            return objectMapper.readValue(textContent, Map.class);

        } catch (Exception e) {
            return Map.of("medicines", java.util.List.of(), "error", "Could not process prescription");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromClaudeResponse(Map<String, Object> response) {
        var content = (java.util.List<Map<String, Object>>) response.get("content");
        return (String) content.get(0).get("text");
    }
}
```

---

### TranscribeService.java

```java
package com.sahayak.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.util.UUID;

@Service
public class TranscribeService {

    private final TranscribeClient transcribeClient;

    @Value("${aws.s3.bucket}")
    private String s3Bucket;

    public TranscribeService(TranscribeClient transcribeClient) {
        this.transcribeClient = transcribeClient;
    }

    /**
     * Transcribes audio file already uploaded to S3
     * @param s3Key  - key of the audio file in S3
     * @param languageCode - "hi-IN" for Hindi, "ta-IN" for Tamil, "en-IN" for English
     * @return Transcribed text
     */
    public String transcribeAudio(String s3Key, String languageCode) throws InterruptedException {
        String jobName = "sahayak-" + UUID.randomUUID();
        String s3Uri = "s3://" + s3Bucket + "/" + s3Key;

        StartTranscriptionJobRequest startRequest = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .media(Media.builder().mediaFileUri(s3Uri).build())
                .languageCode(LanguageCode.fromValue(languageCode))
                .outputBucketName(s3Bucket)
                .outputKey("transcripts/" + jobName + ".json")
                .build();

        transcribeClient.startTranscriptionJob(startRequest);

        // Poll for completion (max 30 seconds for hackathon demo)
        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
            GetTranscriptionJobResponse response = transcribeClient.getTranscriptionJob(
                GetTranscriptionJobRequest.builder().transcriptionJobName(jobName).build()
            );
            TranscriptionJobStatus status = response.transcriptionJob().transcriptionJobStatus();

            if (status == TranscriptionJobStatus.COMPLETED) {
                // Fetch transcript from S3 and parse
                return fetchTranscriptFromS3("transcripts/" + jobName + ".json");
            } else if (status == TranscriptionJobStatus.FAILED) {
                throw new RuntimeException("Transcription failed: " + response.transcriptionJob().failureReason());
            }
        }
        throw new RuntimeException("Transcription timed out after 30 seconds");
    }

    private String fetchTranscriptFromS3(String key) {
        // Use S3Service to download and parse the transcript JSON
        // Transcript JSON has structure: { "results": { "transcripts": [{ "transcript": "..." }] } }
        return "Transcribed text placeholder"; // implement with S3Service
    }
}
```

---

### TextractService.java

```java
package com.sahayak.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.util.stream.Collectors;

@Service
public class TextractService {

    private final TextractClient textractClient;

    public TextractService(TextractClient textractClient) {
        this.textractClient = textractClient;
    }

    /**
     * Extracts text from prescription image bytes
     * @param imageBytes - raw image bytes (JPEG/PNG)
     * @return Extracted text joined as single string
     */
    public String extractTextFromImage(byte[] imageBytes) {
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .build();

        DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

        // Filter only LINE blocks (not individual words) for cleaner output
        return response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Extracts text from a prescription image already in S3
     */
    public String extractTextFromS3(String s3Bucket, String s3Key) {
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder()
                                .bucket(s3Bucket)
                                .name(s3Key)
                                .build())
                        .build())
                .build();

        DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

        return response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }
}
```

---

### PollyService.java

```java
package com.sahayak.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.util.Base64;

@Service
public class PollyService {

    private final PollyClient pollyClient;

    public PollyService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    /**
     * Converts text to speech and returns Base64-encoded MP3
     * @param text     - text to speak
     * @param language - "hi-IN" or "ta-IN"
     * @return Base64 encoded MP3 audio string (send to frontend as-is)
     */
    public String synthesizeSpeech(String text, String language) {
        VoiceId voiceId = switch (language) {
            case "hi-IN" -> VoiceId.KAJAL;   // Hindi neural voice
            case "ta-IN" -> VoiceId.KAJAL;   // Use Kajal for Tamil too (or check Polly docs)
            default      -> VoiceId.KAJAL;
        };

        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                .text(text)
                .voiceId(voiceId)
                .outputFormat(OutputFormat.MP3)
                .engine(Engine.NEURAL)
                .languageCode(LanguageCode.fromValue(language.equals("hi-IN") ? "hi-IN" : "en-IN"))
                .build();

        SynthesizeSpeechResponse response = pollyClient.synthesizeSpeech(request);
        byte[] audioBytes = response.audioStream().readAllBytes();
        return Base64.getEncoder().encodeToString(audioBytes);
    }
}
```

---

### TriageController.java

```java
package com.sahayak.controller;

import com.sahayak.service.BedrockService;
import com.sahayak.service.PollyService;
import com.sahayak.service.S3Service;
import com.sahayak.service.TranscribeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class TriageController {

    private final S3Service s3Service;
    private final TranscribeService transcribeService;
    private final BedrockService bedrockService;
    private final PollyService pollyService;

    public TriageController(S3Service s3Service, TranscribeService transcribeService,
                            BedrockService bedrockService, PollyService pollyService) {
        this.s3Service = s3Service;
        this.transcribeService = transcribeService;
        this.bedrockService = bedrockService;
        this.pollyService = pollyService;
    }

    /**
     * Main voice triage endpoint
     * Accepts: audio file + language + location
     * Returns: specialist type, hospital list, audio response
     */
    @PostMapping("/triage")
    public ResponseEntity<Map<String, Object>> triage(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language,
            @RequestParam(value = "lat", defaultValue = "28.6139") String lat,
            @RequestParam(value = "lng", defaultValue = "77.2090") String lng,
            @RequestParam(value = "text", required = false) String directText // bypass voice for demo
    ) {
        try {
            String symptomText;

            // Option A: Direct text input (useful for demo/testing)
            if (directText != null && !directText.isEmpty()) {
                symptomText = directText;
            } else {
                // Option B: Voice → S3 → Transcribe
                String s3Key = s3Service.uploadAudio(audioFile);
                symptomText = transcribeService.transcribeAudio(s3Key, language);
                s3Service.deleteFile(s3Key); // Clean up immediately
            }

            // Analyze with Bedrock (Claude 3.5 Sonnet)
            String location = lat + "," + lng;
            Map<String, Object> analysis = bedrockService.analyzeSymptoms(symptomText, language, location);

            // Get mock hospital list (replace with real UHI call when available)
            List<Map<String, Object>> hospitals = getMockHospitals(
                (String) analysis.get("specialist"),
                (Boolean) analysis.get("isEmergency")
            );

            // Generate audio response
            String responseText = (String) analysis.getOrDefault("responseInLanguage",
                "Aapke lakshan ke aadhar par doctor ki salah li ja rahi hai.");
            String audioBase64 = pollyService.synthesizeSpeech(responseText, language);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symptomText", symptomText);
            response.put("specialist", analysis.get("specialist"));
            response.put("isEmergency", analysis.get("isEmergency"));
            response.put("urgencyLevel", analysis.get("urgencyLevel"));
            response.put("summary", analysis.get("summary"));
            response.put("responseText", responseText);
            response.put("audioBase64", audioBase64);
            response.put("hospitals", hospitals);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Triage failed: " + e.getMessage(),
                "emergencyHelpline", "108"
            ));
        }
    }

    private List<Map<String, Object>> getMockHospitals(String specialist, Boolean isEmergency) {
        // TODO: Replace with real UHI/Beckn Protocol call
        return List.of(
            Map.of("name", "Government District Hospital", "type", "government",
                   "specialist", specialist, "distance", 1.2, "free", true,
                   "phone", "+91-9876543210", "address", "Sector 10, District Hospital"),
            Map.of("name", "Primary Health Centre", "type", "government",
                   "specialist", "General Physician", "distance", 2.5, "free", true,
                   "phone", "+91-9876543211", "address", "Nehru Nagar PHC"),
            Map.of("name", "City Medical Centre", "type", "private",
                   "specialist", specialist, "distance", 3.1, "free", false,
                   "fee", 300, "phone", "+91-9876543212", "address", "MG Road")
        );
    }
}
```

---

### PrescriptionController.java

```java
package com.sahayak.controller;

import com.sahayak.service.BedrockService;
import com.sahayak.service.PollyService;
import com.sahayak.service.S3Service;
import com.sahayak.service.TextractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PrescriptionController {

    private final TextractService textractService;
    private final BedrockService bedrockService;
    private final PollyService pollyService;
    private final S3Service s3Service;

    public PrescriptionController(TextractService textractService, BedrockService bedrockService,
                                  PollyService pollyService, S3Service s3Service) {
        this.textractService = textractService;
        this.bedrockService = bedrockService;
        this.pollyService = pollyService;
        this.s3Service = s3Service;
    }

    /**
     * Prescription image processing endpoint
     * Accepts: image file + language + location
     * Returns: medicine list with generic alternatives + Jan Aushadhi prices
     */
    @PostMapping("/prescription")
    public ResponseEntity<Map<String, Object>> processPrescription(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language,
            @RequestParam(value = "lat", defaultValue = "28.6139") String lat,
            @RequestParam(value = "lng", defaultValue = "77.2090") String lng
    ) {
        String s3Key = null;
        try {
            // Step 1: Upload to S3 temporarily
            s3Key = s3Service.uploadImage(imageFile);

            // Step 2: OCR with Textract
            String extractedText = textractService.extractTextFromImage(imageFile.getBytes());

            if (extractedText == null || extractedText.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Could not read prescription. Please take a clearer photo in good lighting.",
                    "tips", "Ensure good lighting, hold camera steady, and keep text fully visible."
                ));
            }

            // Step 3: Process with Bedrock (brand → generic mapping + pricing)
            Map<String, Object> medicineData = bedrockService.processMedicines(extractedText, language);

            // Step 4: Generate audio summary
            String responseText = (String) medicineData.getOrDefault("responseInLanguage",
                "Aapki dawaiyon ki list taiyar hai. Sasta vikalp mil gaya.");
            String audioBase64 = pollyService.synthesizeSpeech(responseText, language);

            // Step 5: Add mock Jan Aushadhi locations (replace with real location service)
            var janAushadhiLocations = getMockJanAushadhiLocations(lat, lng);

            Map<String, Object> response = new HashMap<>(medicineData);
            response.put("success", true);
            response.put("extractedText", extractedText);
            response.put("audioBase64", audioBase64);
            response.put("janAushadhiLocations", janAushadhiLocations);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Prescription processing failed: " + e.getMessage()
            ));
        } finally {
            // Always delete temp S3 file
            if (s3Key != null) s3Service.deleteFile(s3Key);
        }
    }

    private java.util.List<Map<String, Object>> getMockJanAushadhiLocations(String lat, String lng) {
        return java.util.List.of(
            Map.of("name", "Jan Aushadhi Kendra - Sector 5", "distance", 0.8,
                   "address", "Near Bus Stand, Sector 5", "phone", "+91-9876543220"),
            Map.of("name", "Pradhan Mantri Bhartiya Janaushadhi Kendra", "distance", 1.5,
                   "address", "Railway Station Road", "phone", "+91-9876543221"),
            Map.of("name", "Jan Aushadhi Kendra - Civil Hospital", "distance", 2.1,
                   "address", "Civil Hospital Campus", "phone", "+91-9876543222")
        );
    }
}
```

---

### HealthController.java

```java
package com.sahayak.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "healthy",
            "service", "Sahayak Backend",
            "timestamp", Instant.now().toString(),
            "version", "1.0.0-hackathon"
        );
    }
}
```

---

## Phase 3 — AWS Console Setup

### Step 7: Enable Required AWS Services

Log into **[https://console.aws.amazon.com](https://console.aws.amazon.com)** and switch region to **`ap-south-1` (Mumbai)**.

#### 7a. Amazon Bedrock — Enable Claude 3.5 Sonnet ⚠️ Do this FIRST

1. Go to **Amazon Bedrock** in the console
2. In the left sidebar, click **Model access**
3. Click **Modify model access**
4. Find **Anthropic → Claude 3.5 Sonnet** → check the box
5. Click **Save changes**
6. Wait for status to change to **Access granted** (usually 1–5 minutes)

#### 7b. Amazon S3 — Create Temp Bucket

1. Go to **S3** → **Create bucket**
2. Bucket name: `sahayak-prescriptions-temp`
3. Region: `ap-south-1`
4. Block all public access: ✅ (keep enabled)
5. Create bucket
6. After creation, go to **Management** → **Lifecycle rules** → **Create lifecycle rule**
   - Rule name: `delete-temp-files`
   - Prefix: (leave empty — applies to all)
   - Actions: **Expire current versions of objects**
   - Days after object creation: **1**
   - Save

#### 7c. Amazon DynamoDB — Create Sessions Table

1. Go to **DynamoDB** → **Create table**
2. Table name: `sahayak-sessions`
3. Partition key: `sessionId` (String)
4. Table settings: **On-demand** (pay per request — no capacity planning needed)
5. Create table

#### 7d. Amazon Transcribe, Textract, Polly

These are **ready to use** — no setup needed. Just ensure your IAM user has permission (see Step 8).

---

### Step 8: Create IAM Policy and User

#### 8a. Create the Policy

1. Go to **IAM** → **Policies** → **Create policy**
2. Click **JSON** tab and paste:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "BedrockAccess",
      "Effect": "Allow",
      "Action": ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"],
      "Resource": "*"
    },
    {
      "Sid": "TranscribeAccess",
      "Effect": "Allow",
      "Action": [
        "transcribe:StartTranscriptionJob",
        "transcribe:GetTranscriptionJob",
        "transcribe:DeleteTranscriptionJob"
      ],
      "Resource": "*"
    },
    {
      "Sid": "TextractAccess",
      "Effect": "Allow",
      "Action": ["textract:DetectDocumentText", "textract:AnalyzeDocument"],
      "Resource": "*"
    },
    {
      "Sid": "PollyAccess",
      "Effect": "Allow",
      "Action": ["polly:SynthesizeSpeech", "polly:DescribeVoices"],
      "Resource": "*"
    },
    {
      "Sid": "S3Access",
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::sahayak-prescriptions-temp",
        "arn:aws:s3:::sahayak-prescriptions-temp/*"
      ]
    },
    {
      "Sid": "DynamoDBAccess",
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem", "dynamodb:GetItem",
        "dynamodb:UpdateItem", "dynamodb:DeleteItem", "dynamodb:Query"
      ],
      "Resource": "arn:aws:dynamodb:ap-south-1:*:table/sahayak-sessions"
    }
  ]
}
```

3. Policy name: `SahayakBackendPolicy`
4. Create policy

#### 8b. Create IAM User (for local development)

1. Go to **IAM** → **Users** → **Create user**
2. Username: `sahayak-backend-dev`
3. Select **Attach policies directly** → find and attach `SahayakBackendPolicy`
4. Create user
5. Click the user → **Security credentials** → **Create access key**
6. Use case: **Application running outside AWS**
7. **Copy the Access Key ID and Secret Access Key** (shown only once!)

#### 8c. Configure AWS CLI locally

```bash
aws configure
# AWS Access Key ID:     [paste your access key]
# AWS Secret Access Key: [paste your secret key]
# Default region name:   ap-south-1
# Default output format: json
```

Test it works:
```bash
aws bedrock list-foundation-models --region ap-south-1 | head -20
```

---

## Phase 4 — Connect Frontend to Backend

### Step 9: Create the API service file in React

Create a new file: `src/app/services/api.ts`

```typescript
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// ──────────────────────────────────────────
// Voice Triage API Call
// ──────────────────────────────────────────
export interface TriageResponse {
  success: boolean;
  symptomText: string;
  specialist: string;
  isEmergency: boolean;
  urgencyLevel: 'emergency' | 'urgent' | 'low';
  summary: string;
  responseText: string;
  audioBase64: string;
  hospitals: Hospital[];
  error?: string;
}

export interface Hospital {
  name: string;
  type: 'government' | 'private';
  specialist: string;
  distance: number;
  free: boolean;
  fee?: number;
  phone: string;
  address: string;
}

export const analyzeSymptoms = async (
  audioBlob: Blob | null,
  language: string,
  location: { lat: number; lng: number },
  directText?: string
): Promise<TriageResponse> => {
  const formData = new FormData();
  if (audioBlob) {
    formData.append('audio', audioBlob, 'recording.webm');
  } else {
    formData.append('audio', new Blob([]), 'empty.webm');
  }
  if (directText) formData.append('text', directText);
  formData.append('language', language);
  formData.append('lat', String(location.lat));
  formData.append('lng', String(location.lng));

  const res = await fetch(`${API_BASE}/api/v1/triage`, {
    method: 'POST',
    body: formData,
  });
  return res.json();
};

// ──────────────────────────────────────────
// Prescription API Call
// ──────────────────────────────────────────
export interface MedicineComparison {
  brandName: string;
  genericName: string;
  dosage: string;
  brandPrice: number;
  genericPrice: number;
  savingsPercent: number;
}

export interface PrescriptionResponse {
  success: boolean;
  extractedText: string;
  medicines: MedicineComparison[];
  totalBrandCost: number;
  totalGenericCost: number;
  totalSavingsPercent: number;
  responseText: string;
  audioBase64: string;
  janAushadhiLocations: JanAushadhiLocation[];
  error?: string;
}

export interface JanAushadhiLocation {
  name: string;
  distance: number;
  address: string;
  phone: string;
}

export const analyzePrescription = async (
  imageFile: File,
  language: string,
  location: { lat: number; lng: number }
): Promise<PrescriptionResponse> => {
  const formData = new FormData();
  formData.append('image', imageFile);
  formData.append('language', language);
  formData.append('lat', String(location.lat));
  formData.append('lng', String(location.lng));

  const res = await fetch(`${API_BASE}/api/v1/prescription`, {
    method: 'POST',
    body: formData,
  });
  return res.json();
};

// ──────────────────────────────────────────
// Play Audio Response from Base64 MP3
// ──────────────────────────────────────────
export const playAudioResponse = (base64Audio: string) => {
  const audioBlob = new Blob(
    [Uint8Array.from(atob(base64Audio), c => c.charCodeAt(0))],
    { type: 'audio/mp3' }
  );
  const url = URL.createObjectURL(audioBlob);
  const audio = new Audio(url);
  audio.play();
  audio.onended = () => URL.revokeObjectURL(url);
};
```

### Step 10: Add the `.env` file for frontend

Create `.env.local` in the React project root:

```bash
# Development
VITE_API_URL=http://localhost:8080

# After deployment, change to your EC2 or Beanstalk URL:
# VITE_API_URL=http://your-beanstalk-url.ap-south-1.elasticbeanstalk.com
```

---

## Phase 5 — Deploy to AWS

### Option A: AWS Elastic Beanstalk (✅ Recommended for Hackathon)

Easiest deployment — no server management, auto-scaling, HTTPS ready.

```bash
# 1. Install Elastic Beanstalk CLI
pip install awsebcli

# 2. Build your JAR
cd sahayak-backend
mvn clean package -DskipTests

# 3. Initialize Elastic Beanstalk
eb init sahayak-backend \
  --platform "java-21" \
  --region ap-south-1

# 4. Create environment and deploy
eb create sahayak-prod \
  --instance-type t3.small \
  --single

# 5. Deploy updates
eb deploy

# 6. Get your URL
eb status
# Look for: CNAME: sahayak-prod.ap-south-1.elasticbeanstalk.com
```

**Attach the IAM role to your Beanstalk environment:**
1. Go to **EC2** → **Instances** → find your Beanstalk instance
2. **Actions** → **Security** → **Modify IAM role** → attach `SahayakBackendPolicy`

### Option B: EC2 (Simple, full control)

```bash
# 1. Launch EC2 in AWS Console
#    - AMI: Amazon Linux 2023
#    - Instance type: t3.small
#    - Key pair: create new, download .pem file
#    - Security group: allow ports 22 (SSH) and 8080 (HTTP)
#    - IAM role: create role with SahayakBackendPolicy attached

# 2. Build JAR locally
mvn clean package -DskipTests

# 3. Copy JAR to EC2
scp -i your-key.pem target/sahayak-backend-0.0.1-SNAPSHOT.jar \
  ec2-user@<EC2-PUBLIC-IP>:/home/ec2-user/

# 4. SSH into EC2 and run
ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>
sudo dnf install java-21-amazon-corretto -y
java -jar sahayak-backend-0.0.1-SNAPSHOT.jar

# 5. Run as background service
nohup java -jar sahayak-backend-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

---

## Hackathon Mock Strategy

Since the UHI/Beckn Protocol is complex to integrate quickly, use this approach:

| Component | Demo Approach |
|---|---|
| **Bedrock / Claude 3.5** | ✅ Real — most impressive part of the demo |
| **Transcribe** | ✅ Real — test with a pre-recorded Hindi audio clip |
| **Textract** | ✅ Real — use a clear printed prescription photo |
| **Polly** | ✅ Real — Hindi voice response |
| **UHI Network** | 🟡 Mock — return JSON from `mock-hospitals.json` |
| **Jan Aushadhi Prices** | 🟡 Mock — hardcoded price data |
| **Location Service** | 🟡 Mock — hardcode Delhi coordinates for demo |

**Demo Happy Path:**
1. Record Hindi audio: *"Mujhe seene mein dard ho raha hai"* (chest pain — triggers Emergency Mode)
2. Upload a printed prescription with medicines like Crocin, Metformin, Atorvastatin
3. Show the Emergency Mode (red UI) vs Savings Mode (green UI)
4. Show the cost savings: ₹450 branded vs ₹65 generic

---

## Day-by-Day Checklist

### Day 1 — Local Backend Working
- [ ] Spring Boot project created and compiles
- [ ] AWS credentials configured (`aws configure`)
- [ ] Bedrock access granted in AWS Console
- [ ] `AwsConfig.java` — all beans wiring without errors
- [ ] `BedrockService.java` — test with a simple curl call
- [ ] `TextractService.java` — test with a sample prescription image
- [ ] `TranscribeService.java` — test with a sample Hindi audio
- [ ] Both endpoints return valid JSON at `localhost:8080`

### Day 2 — Full Integration
- [ ] `PollyService.java` — audio response working
- [ ] `S3Service.java` — upload and delete working
- [ ] CORS configured, React frontend calling the real backend
- [ ] Emergency Mode triggered on test symptom "chest pain"
- [ ] Prescription savings displayed correctly
- [ ] End-to-end flow tested locally

### Day 3 — Deployment and Demo Prep
- [ ] JAR built: `mvn clean package -DskipTests`
- [ ] Deployed to Elastic Beanstalk or EC2
- [ ] Frontend `.env.local` updated with deployed URL
- [ ] Frontend redeployed (or Vite preview pointing at AWS backend)
- [ ] `GET /api/v1/health` returns 200 from deployed URL
- [ ] Demo script rehearsed with happy-path scenarios
- [ ] Emergency helpline 108 visible in Emergency Mode

---

## Key Tips & Gotchas

| Tip | Details |
|---|---|
| ⚠️ **Request Bedrock access first** | Go to Bedrock → Model access and request Claude 3.5 Sonnet immediately — it can take a few minutes |
| 🌏 **Use `ap-south-1` (Mumbai)** | Lower latency for India, better Transcribe accuracy for Hindi/Tamil |
| 🎤 **Transcribe language codes** | Hindi: `hi-IN`, Tamil: `ta-IN`, English: `en-IN` |
| 🔊 **Polly Hindi voice** | Use `Kajal` (Neural) — best Hindi voice available |
| 🖼️ **Textract limits** | Max 10MB per image. Compress images before upload |
| 🚀 **Cold starts** | Spring Boot takes ~10-15 sec to start on Lambda. Use EC2/Beanstalk instead |
| 💰 **Cost control** | Bedrock charges per token. Keep prompts concise. Set max_tokens to 1024 |
| 🔐 **Never commit credentials** | Add `application-local.yml` with real credentials to `.gitignore` |
| 🎯 **Demo reliability** | Pre-record your Hindi audio clip and use a printed prescription, not handwritten |
| 🏥 **Emergency Mode** | Keywords: "seene mein dard" (chest pain), "saans lena mushkil" (breathing difficulty) |

---

## Estimated AWS Costs for Hackathon Demo

| Service | Usage | Estimated Cost |
|---|---|---|
| Amazon Bedrock (Claude 3.5 Sonnet) | ~100 requests | ~$1–3 |
| Amazon Transcribe | ~20 audio clips | <$0.50 |
| Amazon Textract | ~20 prescription images | <$0.50 |
| Amazon Polly | ~100 responses | <$0.10 |
| Amazon S3 | Temp storage | <$0.01 |
| DynamoDB | On-demand | <$0.01 |
| EC2 t3.small (1 day) | Demo day | ~$0.50 |
| **Total** | | **~$3–5** |

Well within AWS hackathon credits. 🎉

---

*Document prepared for Sahayak — AI Health Agent for Rural India*
*Hackathon POC | AIForBharat*
