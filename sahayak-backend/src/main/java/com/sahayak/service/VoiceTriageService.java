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

            // Step 3: Get hospitals
            List<HospitalDto> hospitals = mockDataService.getHospitals(
                    specialist, isEmergency, request.getLat(), request.getLng());

            // Step 4: Generate audio response (TTS)
            String audioBase64 = null;
            try {
                audioBase64 = pollyService.synthesize(responseText, language);
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
}
