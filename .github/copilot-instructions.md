# Copilot Instructions — Sahayak AI Health Agent

## Project Overview
Sahayak is a **voice-first, multilingual AI healthcare application** for rural India. It enables users to:
1. **Speak symptoms** in native languages → get specialist recommendations + nearby government hospitals
2. **Upload prescriptions** → find generic medicine alternatives at Jan Aushadhi Kendras (up to 80% savings)

Target users: Non-English speakers in rural areas with limited digital/medical literacy.

## Architecture

### Monorepo Structure
```
├── Healthcare Access Mobile App/   # React + Vite frontend (TypeScript)
├── sahayak-backend/                # Spring Boot 3.3 + Java 21 backend
└── guidelines/                     # Requirements + design docs
```

### Data Flow
1. **Voice Triage**: Audio/Text → `TranscribeService` → `BedrockService` (Claude AI) → `MockDataService` (hospitals) → `PollyService` (TTS response)
2. **Prescription**: Image → `TextractService` (OCR) → `BedrockService` (medicine parsing) → generic price lookup → pharmacy locations

### Key Integration Points
- Frontend calls backend via `src/app/services/api.ts` → `http://localhost:8080/api/v1/{triage,prescription}`
- Backend toggles AWS real/mock mode via `sahayak.use-real-aws` property (default: mock)
- All AWS services use **ap-south-1 (Mumbai)** region

## Development Workflow

### Running Locally
```bash
# Terminal 1 — Backend (mock mode, no AWS creds needed)
cd sahayak-backend && ./mvnw spring-boot:run

# Terminal 2 — Frontend
cd "Healthcare Access Mobile App" && npm run dev
```
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

### Testing Endpoints
```bash
# Text-based triage
curl -X POST http://localhost:8080/api/v1/triage \
  -F "directText=मुझे सिरदर्द है" -F "language=hi-IN"

# Prescription analysis
curl -X POST http://localhost:8080/api/v1/prescription \
  -F "image=@prescription.jpg" -F "language=hi-IN"
```

## Code Conventions

### Backend (Java/Spring Boot)
- **Service orchestration pattern**: `VoiceTriageService` and `PrescriptionService` orchestrate multiple AWS services
- **Constructor injection**: Use constructor DI (no `@Autowired` on fields) — see [VoiceTriageService.java](sahayak-backend/src/main/java/com/sahayak/service/VoiceTriageService.java)
- **Response builders**: Use Lombok `@Builder` for response DTOs
- **Mock/Real toggle**: Check `@Value("${sahayak.use-real-aws:false}")` before calling AWS

### Frontend (React/TypeScript)
- **State management**: `AppContext` holds global state (language, triageResult, prescriptionResult)
- **API layer**: All backend calls in [api.ts](Healthcare%20Access%20Mobile%20App/src/app/services/api.ts) with typed DTOs
- **Language mapping**: Use `mapLanguageCode()` to convert `"hi"` → `"hi-IN"` for AWS compatibility
- **UI components**: shadcn/ui in `src/app/components/ui/` (Radix + Tailwind)

### Multilingual Support
- Supported languages: `hi-IN`, `ta-IN`, `te-IN`, `kn-IN`, `mr-IN`, `bn-IN`, `gu-IN`, `ml-IN`, `pa-IN`, `en-IN`
- All AI responses must include `responseInLanguage` in the user's native language
- TTS uses AWS Polly with neural Hindi voice (Kajal)

## Critical Patterns

### Emergency Detection
When symptoms indicate emergencies (chest pain, stroke signs, severe bleeding), the AI must:
1. Set `isEmergency: true` and `urgencyLevel: "emergency"` in response
2. Frontend redirects to `/emergency` page with red UI theme
3. Always over-estimate urgency for patient safety

### Government Healthcare Priority
- Always list **government hospitals** before private clinics in results
- Highlight `free: true` status for PMJAY-eligible facilities
- Emphasize Jan Aushadhi savings percentage prominently

### Mock Data
Mock data files at `sahayak-backend/src/main/resources/data/`:
- `mock-hospitals.json`: Hospital directory with specialist filtering
- `mock-medicines.json`: Brand-to-generic mappings
- `mock-pharmacies.json`: Jan Aushadhi Kendra locations

## File Reference
| Purpose | Location |
|---------|----------|
| API endpoints | `sahayak-backend/src/main/java/com/sahayak/controller/` |
| AI prompts | `BedrockService.java` → `analyzeSymptoms()`, `processMedicines()` |
| Frontend routes | `src/app/routes.ts` |
| Design requirements | `guidelines/requirements.md`, `guidelines/design.md` |
