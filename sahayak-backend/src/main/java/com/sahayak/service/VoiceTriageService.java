package com.sahayak.service;

import com.sahayak.model.HospitalDto;
import com.sahayak.model.TriageRequest;
import com.sahayak.model.TriageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VoiceTriageService {

    private final BedrockService bedrockService;
    private final PollyService pollyService;
    private final S3Service s3Service;
    private final TranscribeService transcribeService;
    private final MockDataService mockDataService;

    @Value("${sahayak.use-real-aws:false}")
    private boolean useRealAws;

    public VoiceTriageService(BedrockService bedrockService,
                               PollyService pollyService,
                               S3Service s3Service,
                               TranscribeService transcribeService,
                               MockDataService mockDataService) {
        this.bedrockService = bedrockService;
        this.pollyService = pollyService;
        this.s3Service = s3Service;
        this.transcribeService = transcribeService;
        this.mockDataService = mockDataService;
    }

    /**
     * Main entry point — handles both audio blob and direct text input.
     */
    public TriageResponse processVoiceTriage(MultipartFile audioFile,
                                             TriageRequest request) {
        try {
            String symptomText;

            // Step 1: Get symptom text — either from direct text or audio transcription
            if (request.getDirectText() != null && !request.getDirectText().isBlank()) {
                symptomText = request.getDirectText();
                log.info("Using direct text input: {}", symptomText);
            } else if (audioFile != null && !audioFile.isEmpty()) {
                symptomText = transcribeAudio(audioFile, request.getLanguage());
            } else {
                return TriageResponse.builder()
                        .success(false)
                        .error("No audio or text input provided")
                        .build();
            }

            // Step 2: AI analysis via Bedrock (with location context for hospital-aware responses)
            String language = request.getLanguage() != null ? request.getLanguage() : "hi-IN";
            Double userLat = safeParseDbl(request.getLat());
            Double userLng = safeParseDbl(request.getLng());
            Map<String, Object> analysis = bedrockService.analyzeSymptoms(
                    symptomText, language, userLat, userLng);

            String specialist = (String) analysis.getOrDefault("specialist", "General Physician");
            boolean isEmergency = Boolean.parseBoolean(
                    String.valueOf(analysis.getOrDefault("isEmergency", "false")));
            String urgencyLevel = (String) analysis.getOrDefault("urgencyLevel", "medium");
            String summary = (String) analysis.getOrDefault("summary", "");
            String responseText = (String) analysis.getOrDefault("responseInLanguage", summary);

            // For audio: use Hindi version if available (Polly only supports Hindi/English),
            // otherwise fall back to the display response text
            String audioText = (String) analysis.getOrDefault("responseForAudio", responseText);

            // Step 3: Get hospitals
            List<HospitalDto> hospitals = mockDataService.getHospitals(
                    specialist, isEmergency, request.getLat(), request.getLng());

            // Smart specialist assignment: infer from hospital name if possible,
            // otherwise fall back to the AI-recommended specialist.
            // The data.gov.in directory has no specialist info (all default to "General Physician"),
            // but many hospital names reveal their specialty (e.g. "Eye Hospital" → Ophthalmologist).
            for (HospitalDto h : hospitals) {
                String inferred = inferSpecialistFromName(h.getName());
                h.setSpecialist(inferred != null ? inferred : specialist);
            }

            // Step 4: Generate audio response (TTS)
            // Use Hindi text for audio if available (Polly only supports Hindi/English voices)
            String audioBase64 = null;
            try {
                audioBase64 = pollyService.synthesize(audioText, language);
            } catch (Exception e) {
                log.warn("Polly TTS failed, proceeding without audio: {}", e.getMessage());
            }

            return TriageResponse.builder()
                    .success(true)
                    .symptomText(symptomText)
                    .specialist(specialist)
                    .isEmergency(isEmergency)
                    .urgencyLevel(urgencyLevel)
                    .summary(summary)
                    .responseText(responseText)
                    .audioBase64(audioBase64)
                    .hospitals(hospitals)
                    .build();

        } catch (Exception e) {
            log.error("VoiceTriageService error: {}", e.getMessage(), e);
            return TriageResponse.builder()
                    .success(false)
                    .error("Service error: " + e.getMessage())
                    .build();
        }
    }

    private String transcribeAudio(MultipartFile audioFile, String language) throws Exception {
        if (useRealAws) {
            String s3Key = null;
            try {
                s3Key = s3Service.uploadAudio(audioFile);
                String transcript = transcribeService.transcribeAudio(s3Key, language);
                return transcript.isBlank() ? "Could not transcribe audio" : transcript;
            } finally {
                if (s3Key != null) {
                    s3Service.deleteFile(s3Key);
                }
            }
        } else {
            // Mock: return a simulated transcription based on file name
            log.info("Mock mode: simulating transcription");
            return "मुझे बुखार और सिरदर्द है"; // "I have fever and headache" in Hindi
        }
    }

    private Double safeParseDbl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Infers the specialist type from a hospital's name.
     * Many hospitals in the data.gov.in directory have descriptive names
     * (e.g. "Sankara Eye Hospital", "City Dental Clinic", "Heart Care Centre").
     * Returns null if no specialty can be inferred — caller should use AI specialist as fallback.
     */
    private String inferSpecialistFromName(String hospitalName) {
        if (hospitalName == null || hospitalName.isBlank()) return null;
        String lower = hospitalName.toLowerCase();

        // Eye / Vision / Ophthalmic
        if (lower.contains("eye") || lower.contains("ophthal") || lower.contains("netralaya")
                || lower.contains("netra") || lower.contains("vision") || lower.contains("nayan")) {
            return "Ophthalmologist";
        }
        // Dental / Tooth
        if (lower.contains("dental") || lower.contains("dent") || lower.contains("tooth")
                || lower.contains("orthodon")) {
            return "Dentist";
        }
        // Heart / Cardiac
        if (lower.contains("heart") || lower.contains("cardiac") || lower.contains("cardio")
                || lower.contains("hrudaya") || lower.contains("hriday")) {
            return "Cardiologist";
        }
        // Ortho / Bone / Joint
        if (lower.contains("ortho") || lower.contains("bone") || lower.contains("joint")
                || lower.contains("fracture") || lower.contains("spine") || lower.contains("asthi")) {
            return "Orthopedic";
        }
        // Skin / Derma
        if (lower.contains("skin") || lower.contains("derma") || lower.contains("tvach")
                || lower.contains("charm")) {
            return "Dermatologist";
        }
        // ENT / Ear-Nose-Throat
        if (lower.contains("ent ") || lower.contains("e.n.t") || lower.contains("ear nose")
                || lower.contains("ear, nose")) {
            return "ENT";
        }
        // Child / Paediatric
        if (lower.contains("child") || lower.contains("pediatr") || lower.contains("paediatr")
                || lower.contains("kids") || lower.contains("bal") || lower.contains("shishu")) {
            return "Pediatrician";
        }
        // Women / Maternity / Gynae
        if (lower.contains("matern") || lower.contains("gynae") || lower.contains("gynec")
                || lower.contains("women") || lower.contains("obstetri") || lower.contains("stri")
                || lower.contains("prasuti")) {
            return "Gynecologist";
        }
        // Neuro / Brain
        if (lower.contains("neuro") || lower.contains("brain")) {
            return "Neurologist";
        }
        // Mental / Psychiatric
        if (lower.contains("psych") || lower.contains("mental") || lower.contains("manasik")) {
            return "Psychiatrist";
        }
        // Cancer / Oncology
        if (lower.contains("cancer") || lower.contains("oncol")) {
            return "Oncologist";
        }
        // Kidney / Nephro / Urology
        if (lower.contains("kidney") || lower.contains("nephro") || lower.contains("urolog")
                || lower.contains("renal")) {
            return "Nephrologist";
        }
        // Gastro / Liver / Digestive
        if (lower.contains("gastro") || lower.contains("liver") || lower.contains("digestive")) {
            return "Gastroenterologist";
        }
        // Chest / Pulmo / Lung / TB
        if (lower.contains("chest") || lower.contains("pulmon") || lower.contains("lung")
                || lower.contains("tb ") || lower.contains("tuberculosis")) {
            return "Pulmonologist";
        }
        // No specialty detected from name
        return null;
    }
}
