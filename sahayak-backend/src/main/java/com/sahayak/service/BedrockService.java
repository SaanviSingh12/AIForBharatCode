package com.sahayak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

@Slf4j
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
     * Analyzes symptoms and returns AI triage: specialist, emergency flag, response text.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeSymptoms(String symptomText, String language) {
        String prompt = """
            You are Sahayak, an AI health triage assistant for rural India.
            A patient describes their symptoms in %s language: "%s"
            
            Analyze carefully and respond ONLY with this exact JSON (no extra text):
            {
              "specialist": "General Physician",
              "isEmergency": false,
              "urgencyLevel": "low",
              "summary": "Brief English clinical summary of the issue",
              "responseInLanguage": "Friendly response to patient in %s language"
            }
            
            specialist must be exactly one of: General Physician, Cardiologist, Dermatologist,
            Pediatrician, Gynecologist, Orthopedic, ENT, Ophthalmologist, Dentist,
            Psychiatrist, Gastroenterologist, Neurologist
            
            Set isEmergency=true and urgencyLevel="emergency" for: chest pain, difficulty breathing,
            severe bleeding, loss of consciousness, stroke symptoms (facial drooping, arm weakness,
            speech difficulty), severe allergic reaction, poisoning.
            Set urgencyLevel="urgent" for symptoms needing same-day care.
            Set urgencyLevel="low" for routine consultations.
            
            responseInLanguage should be warm, simple, and in plain spoken %s.
            IMPORTANT: Return ONLY the JSON object, nothing else.
            """.formatted(language, symptomText, language, language);

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", 800,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String rawBody = response.body().asUtf8String();
            log.debug("Bedrock raw response: {}", rawBody);

            Map<String, Object> responseMap = objectMapper.readValue(rawBody, Map.class);
            String textContent = extractText(responseMap);

            // Claude sometimes wraps JSON in markdown code blocks — strip them
            textContent = textContent.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(textContent, Map.class);

        } catch (Exception e) {
            log.error("Bedrock symptom analysis failed", e);
            return getFallbackTriageResponse(language);
        }
    }

    /**
     * Processes extracted prescription text — maps brands to generics, generates pricing.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> processMedicines(String extractedText, String language) {
        String prompt = """
            You are a pharmacy assistant for rural India helping patients find affordable medicines.
            The following text was extracted from a prescription: "%s"
            
            Extract all medicine names and respond ONLY with this exact JSON (no extra text):
            {
              "medicines": [
                {
                  "brandName": "Crocin",
                  "genericName": "Paracetamol",
                  "dosage": "500mg",
                  "brandPrice": 45,
                  "genericPrice": 8,
                  "savingsPercent": 82,
                  "savingsAmount": 37
                }
              ],
              "totalBrandCost": 150,
              "totalGenericCost": 28,
              "totalSavingsPercent": 81,
              "responseInLanguage": "Friendly savings summary for patient in %s"
            }
            
            Rules:
            - brandPrice and genericPrice are in Indian Rupees (INR) per standard pack
            - genericPrice is the Jan Aushadhi Kendra price (50-90%% cheaper than brand)
            - savingsPercent = round(((brandPrice - genericPrice) / brandPrice) * 100)
            - savingsAmount = brandPrice - genericPrice
            - If you cannot identify a medicine name clearly, use best guess but keep it
            - responseInLanguage should mention total savings in %s language
            IMPORTANT: Return ONLY the JSON object, nothing else.
            """.formatted(extractedText, language, language);

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", 1500,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String rawBody = response.body().asUtf8String();
            log.debug("Bedrock medicine response: {}", rawBody);

            Map<String, Object> responseMap = objectMapper.readValue(rawBody, Map.class);
            String textContent = extractText(responseMap);
            textContent = textContent.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(textContent, Map.class);

        } catch (Exception e) {
            log.error("Bedrock medicine processing failed", e);
            return getFallbackMedicineResponse();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> claudeResponse) {
        List<Map<String, Object>> content = (List<Map<String, Object>>) claudeResponse.get("content");
        return (String) content.get(0).get("text");
    }

    private Map<String, Object> getFallbackTriageResponse(String language) {
        String response = language.startsWith("hi")
                ? "Aapke lakshan note kar liye gaye hain. Kripya ek General Physician se mile."
                : language.startsWith("ta")
                ? "உங்கள் அறிகுறிகள் பதிவு செய்யப்பட்டன. ஒரு மருத்துவரை சந்திக்கவும்."
                : "Your symptoms have been noted. Please consult a General Physician.";

        return Map.of(
                "specialist", "General Physician",
                "isEmergency", false,
                "urgencyLevel", "low",
                "summary", "Symptoms noted. General physician consultation recommended.",
                "responseInLanguage", response
        );
    }

    private Map<String, Object> getFallbackMedicineResponse() {
        return Map.of(
                "medicines", List.of(),
                "totalBrandCost", 0,
                "totalGenericCost", 0,
                "totalSavingsPercent", 0,
                "responseInLanguage", "Prescription processed. Please check Jan Aushadhi Kendra for generic options."
        );
    }
}
