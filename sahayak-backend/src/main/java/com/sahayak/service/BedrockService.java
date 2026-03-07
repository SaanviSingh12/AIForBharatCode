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
    private final DynamoDbHospitalService dynamoDbHospitalService;
    private final DynamoDbKendraService dynamoDbKendraService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.bedrock.model-id}")
    private String modelId;

    @Value("${sahayak.use-real-aws:false}")
    private boolean useRealAws;

    /**
     * Maps BCP-47 language codes to full language names for Bedrock prompts.
     * Without this, the AI sees "te-IN" instead of "Telugu" and defaults to Hindi.
     */
    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "hi-IN" -> "Hindi";
            case "en-IN" -> "English";
            case "te-IN" -> "Telugu";
            case "ta-IN" -> "Tamil";
            case "kn-IN" -> "Kannada";
            case "mr-IN" -> "Marathi";
            case "bn-IN" -> "Bengali";
            case "gu-IN" -> "Gujarati";
            case "ml-IN" -> "Malayalam";
            case "pa-IN" -> "Punjabi";
            default      -> "Hindi";  // safe fallback
        };
    }

    public BedrockService(BedrockRuntimeClient bedrockClient,
                          DynamoDbHospitalService dynamoDbHospitalService,
                          DynamoDbKendraService dynamoDbKendraService) {
        this.bedrockClient = bedrockClient;
        this.dynamoDbHospitalService = dynamoDbHospitalService;
        this.dynamoDbKendraService = dynamoDbKendraService;
    }

    /**
     * Returns true if AWS Polly can directly speak this language.
     * Polly's Kajal voice only supports Hindi (hi-IN) and English (en-IN).
     */
    private boolean isPollySupported(String language) {
        return "hi-IN".equals(language) || "en-IN".equals(language);
    }

    /**
     * Analyzes symptoms and returns AI triage: specialist, emergency flag, response text.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeSymptoms(String symptomText, String language) {
        return analyzeSymptoms(symptomText, language, null, null);
    }

    /**
     * Location-aware symptom analysis.
     * When useRealAws=true and coordinates are provided, fetches nearby hospitals from
     * DynamoDB and includes them in the Bedrock prompt for context-aware responses.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeSymptoms(String symptomText, String language,
                                                Double userLat, Double userLng) {
        // Build hospital context from DynamoDB if available
        String hospitalContext = "";
        if (useRealAws && userLat != null && userLng != null) {
            try {
                var nearbyHospitals = dynamoDbHospitalService.getHospitalsByLocation(
                        null, false, userLat, userLng);
                if (!nearbyHospitals.isEmpty()) {
                    StringBuilder sb = new StringBuilder("\n\nNearby hospitals available for the patient:\n");
                    for (int i = 0; i < Math.min(5, nearbyHospitals.size()); i++) {
                        var h = nearbyHospitals.get(i);
                        sb.append(String.format("- %s (%s, %s, %.1fkm away, %s)\n",
                                h.getName(), h.getType(), h.getSpecialist(),
                                h.getDistance(),
                                h.isHasEmergency() ? "has emergency" : "no emergency"));
                    }
                    sb.append("\nMention the most relevant nearby hospital by name in your response.");
                    hospitalContext = sb.toString();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch nearby hospitals for Bedrock context: {}", e.getMessage());
            }
        }

        String prompt = """
            You are Sahayak, an expert AI health triage assistant for rural India.
            A patient describes their symptoms in %s: "%s"
            %s
            
            STEP 1: Identify the most appropriate specialist based on these rules:
            
            SYMPTOM → SPECIALIST MAPPING (follow strictly):
            - Headache, migraine, dizziness, seizure, numbness, tingling, memory loss, fainting → Neurologist
            - Chest pain, palpitations, high BP, shortness of breath with chest tightness → Cardiologist
            - Skin rash, acne, eczema, itching, fungal infection, hair loss → Dermatologist
            - Child fever, child cough, infant issues, vaccination → Pediatrician
            - Pregnancy, menstrual problems, PCOS, breast lump → Gynecologist
            - Joint pain, fracture, back pain, sprain, arthritis, knee pain → Orthopedic
            - Ear pain, sore throat, sinus, hearing loss, tonsils, voice hoarseness → ENT
            - Eye pain, blurred vision, redness in eye, cataract → Ophthalmologist
            - Tooth pain, gum bleeding, cavity, jaw pain → Dentist
            - Anxiety, depression, insomnia, panic attacks, stress → Psychiatrist
            - Stomach pain, acidity, vomiting, diarrhea, constipation, bloating → Gastroenterologist
            - Cough, cold, fever, body ache, general weakness, fatigue → General Physician
            
            IMPORTANT: Do NOT default to General Physician unless symptoms truly don't match any specialist above.
            For example: "migraine" or "extreme headache" → Neurologist (NOT General Physician).
            
            STEP 2: Determine urgency:
            - isEmergency=true + urgencyLevel="emergency" for: chest pain, difficulty breathing,
              severe bleeding, loss of consciousness, stroke symptoms, heart attack, poisoning,
              severe allergic reaction, very high fever (>104°F)
            - urgencyLevel="urgent" for symptoms needing same-day care
            - urgencyLevel="low" for routine consultations
            
            STEP 3: Respond ONLY with this exact JSON (no extra text, no markdown):
            {
              "specialist": "<specialist from the mapping above>",
              "isEmergency": false,
              "urgencyLevel": "low",
              "summary": "Brief clinical summary in English explaining the likely condition and recommended specialist",
              "responseInLanguage": "Warm, simple response to the patient in %s. Mention the recommended specialist type and any nearby hospital if available."%s
            }
            
            The "summary" field MUST be a clear 1-2 sentence clinical explanation in English.
            Example: "Patient reports severe migraine symptoms. Neurologist consultation recommended for proper diagnosis and treatment plan."
            
            The "responseInLanguage" should be warm, empathetic, and in simple spoken %s.
            IMPORTANT: Return ONLY the JSON object, nothing else.
            """.formatted(getLanguageName(language), symptomText, hospitalContext,
                          getLanguageName(language),
                          isPollySupported(language) ? "" :
                              ",\n              \"responseForAudio\": \"Same response as responseInLanguage but translated to Hindi (Devanagari script). This will be used for text-to-speech since audio is only available in Hindi.\"",
                          getLanguageName(language));

        try {
            String payload = buildNovaPayload(prompt, 800);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String rawBody = response.body().asUtf8String();
            log.debug("Bedrock raw response: {}", rawBody);

            String textContent = extractNovaText(rawBody);

            // Strip markdown code blocks if present
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
        return processMedicines(extractedText, language, null, null);
    }

    /**
     * Location-aware prescription processing.
     * When coordinates are provided, includes nearby Jan Aushadhi Kendras in the prompt.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> processMedicines(String extractedText, String language,
                                                 Double userLat, Double userLng) {
        // Build kendra context from DynamoDB if available
        String kendraContext = "";
        if (useRealAws && userLat != null && userLng != null) {
            try {
                var nearbyKendras = dynamoDbKendraService.getKendrasByLocation(userLat, userLng);
                if (!nearbyKendras.isEmpty()) {
                    StringBuilder sb = new StringBuilder(
                            "\n\nNearby Jan Aushadhi Kendras where the patient can buy affordable generic medicines:\n");
                    for (int i = 0; i < Math.min(3, nearbyKendras.size()); i++) {
                        var k = nearbyKendras.get(i);
                        sb.append(String.format("- %s, %s (%.1fkm away)\n",
                                k.getName(), k.getAddress(), k.getDistance()));
                    }
                    sb.append("\nMention the nearest Jan Aushadhi Kendra by name and address in your responseInLanguage.");
                    kendraContext = sb.toString();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch nearby kendras for Bedrock context: {}", e.getMessage());
            }
        }

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
              "responseInLanguage": "Friendly savings summary for patient in %s"%s
            }
            
            Rules:
            - brandPrice and genericPrice are in Indian Rupees (INR) per standard pack
            - genericPrice is the Jan Aushadhi Kendra price (50-90%% cheaper than brand)
            - savingsPercent = round(((brandPrice - genericPrice) / brandPrice) * 100)
            - savingsAmount = brandPrice - genericPrice
            - If you cannot identify a medicine name clearly, use best guess but keep it
            - responseInLanguage should mention total savings in %s
            %s
            IMPORTANT: Return ONLY the JSON object, nothing else.
            """.formatted(extractedText, getLanguageName(language),
                          isPollySupported(language) ? "" :
                              ",\n              \"responseForAudio\": \"Same content as responseInLanguage but translated to Hindi (Devanagari). Used for text-to-speech audio since audio is only available in Hindi.\"",
                          getLanguageName(language), kendraContext);

        try {
            String payload = buildNovaPayload(prompt, 1500);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String rawBody = response.body().asUtf8String();
            log.debug("Bedrock medicine response: {}", rawBody);

            String textContent = extractNovaText(rawBody);
            textContent = textContent.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(textContent, Map.class);

        } catch (Exception e) {
            log.error("Bedrock medicine processing failed", e);
            return getFallbackMedicineResponse();
        }
    }

    /**
     * Builds request payload for Amazon Nova models (Messages API format).
     */
    private String buildNovaPayload(String prompt, int maxTokens) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "inferenceConfig", Map.of("maxTokens", maxTokens)
        ));
    }

    /**
     * Extracts text from Amazon Nova response format.
     * Nova format: {"output":{"message":{"content":[{"text":"..."}]}},...}
     */
    @SuppressWarnings("unchecked")
    private String extractNovaText(String rawBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(rawBody, Map.class);
        Map<String, Object> output = (Map<String, Object>) responseMap.get("output");
        Map<String, Object> message = (Map<String, Object>) output.get("message");
        List<Map<String, Object>> content = (List<Map<String, Object>>) message.get("content");
        return (String) content.get(0).get("text");
    }

    private Map<String, Object> getFallbackTriageResponse(String language) {
        String response = switch (language) {
            case "hi-IN" -> "आपके लक्षण नोट कर लिए गए हैं। कृपया एक जनरल फिजिशियन से मिलें।";
            case "te-IN" -> "మీ లక్షణాలు నమోదు చేయబడ్డాయి. దయచేసి జనరల్ ఫిజీషియన్‌ను సంప్రదించండి.";
            case "ta-IN" -> "உங்கள் அறிகுறிகள் பதிவு செய்யப்பட்டன. ஒரு மருத்துவரை சந்திக்கவும்.";
            case "kn-IN" -> "ನಿಮ್ಮ ಲಕ್ಷಣಗಳನ್ನು ದಾಖಲಿಸಲಾಗಿದೆ. ದಯವಿಟ್ಟು ಜನರಲ್ ಫಿಸಿಶಿಯನ್ ಅನ್ನು ಸಂಪರ್ಕಿಸಿ.";
            case "mr-IN" -> "तुमची लक्षणे नोंदवली गेली आहेत. कृपया जनरल फिजिशियनला भेटा.";
            case "bn-IN" -> "আপনার উপসর্গগুলি নথিভুক্ত করা হয়েছে। অনুগ্রহ করে একজন জেনারেল ফিজিশিয়ানের সাথে পরামর্শ করুন।";
            case "gu-IN" -> "તમારા લક્ષણો નોંધવામાં આવ્યા છે. કૃપા કરીને જનરલ ફિઝિશિયનનો સંપર્ક કરો.";
            case "ml-IN" -> "നിങ്ങളുടെ ലക്ഷണങ്ങൾ രേഖപ്പെടുത്തിയിട്ടുണ്ട്. ദയവായി ഒരു ജനറൽ ഫിസിഷ്യനെ സമീപിക്കുക.";
            case "pa-IN" -> "ਤੁਹਾਡੇ ਲੱਛਣ ਨੋਟ ਕਰ ਲਏ ਗਏ ਹਨ। ਕਿਰਪਾ ਕਰਕੇ ਜਨਰਲ ਫਿਜ਼ੀਸ਼ੀਅਨ ਨੂੰ ਮਿਲੋ।";
            default -> "Your symptoms have been noted. Please consult a General Physician.";
        };

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
