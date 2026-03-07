package com.sahayak.service;

import com.sahayak.model.MedicineDto;
import com.sahayak.model.PharmacyDto;
import com.sahayak.model.PrescriptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PrescriptionService {

    private final TextractService textractService;
    private final BedrockService bedrockService;
    private final PollyService pollyService;
    private final S3Service s3Service;
    private final MockDataService mockDataService;

    @Value("${sahayak.use-real-aws:false}")
    private boolean useRealAws;

    public PrescriptionService(TextractService textractService,
                                BedrockService bedrockService,
                                PollyService pollyService,
                                S3Service s3Service,
                                MockDataService mockDataService) {
        this.textractService = textractService;
        this.bedrockService = bedrockService;
        this.pollyService = pollyService;
        this.s3Service = s3Service;
        this.mockDataService = mockDataService;
    }

    /**
     * Main prescription analysis flow:
     * 1. Upload image to S3 (or use bytes directly)
     * 2. Extract text via Textract
     * 3. Parse medicines via Bedrock
     * 4. Look up generic prices
     * 5. Generate audio response via Polly
     */
    public PrescriptionResponse analyzePrescription(MultipartFile imageFile, String language,
                                                     String lat, String lng) {
        String s3Key = null;
        try {
            String extractedText;

            if (useRealAws) {
                // Real AWS flow
                s3Key = s3Service.uploadImage(imageFile);
                extractedText = textractService.extractTextFromS3(s3Key);
                log.info("Textract extracted: {}", extractedText);
            } else {
                // Mock mode — extract text from bytes directly (Textract still works without real creds if mocked)
                // For pure mock, return simulated extracted text
                log.info("Mock mode: simulating prescription extraction");
                extractedText = "Tab. Paracetamol 500mg - twice daily\nTab. Amoxicillin 500mg - thrice daily\nOmeprazole 20mg - once daily before food";
            }

            // AI analysis for generic alternatives (location-aware for kendra context)
            String lang = language != null ? language : "hi-IN";
            Double userLat = safeParseDbl(lat);
            Double userLng = safeParseDbl(lng);
            Map<String, Object> medicineData = bedrockService.processMedicines(extractedText, lang, userLat, userLng);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> medicinesList =
                    (List<Map<String, Object>>) medicineData.getOrDefault("medicines", List.of());

            List<MedicineDto> medicines = medicinesList.stream()
                    .map(this::toMedicineDto)
                    .toList();

            int totalBrandCost = ((Number) medicineData.getOrDefault("totalBrandCost", 0)).intValue();
            int totalGenericCost = ((Number) medicineData.getOrDefault("totalGenericCost", 0)).intValue();
            int totalSavingsPercent = ((Number) medicineData.getOrDefault("totalSavingsPercent", 0)).intValue();
            String responseText = (String) medicineData.getOrDefault("responseInLanguage", "");
            // For audio: use Hindi version if available (Polly only supports Hindi/English)
            String audioText = (String) medicineData.getOrDefault("responseForAudio", responseText);

            // Jan Aushadhi locations (filtered by user location)
            List<PharmacyDto> janAushadhiLocations = mockDataService.getPharmacies(lat, lng);

            // Audio response — use Hindi text for TTS when original language isn't supported
            String audioBase64 = null;
            try {
                audioBase64 = pollyService.synthesize(audioText, lang);
            } catch (Exception e) {
                log.warn("Polly TTS failed: {}", e.getMessage());
            }

            return PrescriptionResponse.builder()
                    .success(true)
                    .extractedText(extractedText)
                    .medicines(medicines)
                    .totalBrandCost(totalBrandCost)
                    .totalGenericCost(totalGenericCost)
                    .totalSavingsPercent(totalSavingsPercent)
                    .responseText(responseText)
                    .audioBase64(audioBase64)
                    .janAushadhiLocations(janAushadhiLocations)
                    .build();

        } catch (Exception e) {
            log.error("PrescriptionService error: {}", e.getMessage(), e);
            return PrescriptionResponse.builder()
                    .success(false)
                    .error("Failed to process prescription: " + e.getMessage())
                    .build();
        } finally {
            if (s3Key != null) {
                s3Service.deleteFile(s3Key);
            }
        }
    }

    /**
     * Analyze prescription text directly (no image upload required).
     * Useful when prescription text is already extracted or provided as input.
     *
     * @param prescription The prescription text to analyze
     * @param language     Language code (e.g., "hi-IN", "en-IN")
     * @param lat          User latitude
     * @param lng          User longitude
     * @return PrescriptionResponse with medicines, pricing, and audio response
     */
    public PrescriptionResponse analyzePrescription(String prescription, String language,
                                                     String lat, String lng) {
        try {
            String extractedText = prescription;
            String lang = language != null ? language : "hi-IN";

            // AI analysis for generic alternatives
            Map<String, Object> medicineData = bedrockService.processMedicines(extractedText, lang);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> medicinesList =
                    (List<Map<String, Object>>) medicineData.getOrDefault("medicines", List.of());

            List<MedicineDto> medicines = medicinesList.stream()
                    .map(this::toMedicineDto)
                    .toList();

            int totalBrandCost = ((Number) medicineData.getOrDefault("totalBrandCost", 0)).intValue();
            int totalGenericCost = ((Number) medicineData.getOrDefault("totalGenericCost", 0)).intValue();
            int totalSavingsPercent = ((Number) medicineData.getOrDefault("totalSavingsPercent", 0)).intValue();
            String responseText = (String) medicineData.getOrDefault("responseInLanguage", "");
            // For audio: use Hindi version if available (Polly only supports Hindi/English)
            String audioText = (String) medicineData.getOrDefault("responseForAudio", responseText);

            // Jan Aushadhi locations (filtered by user location)
            List<PharmacyDto> janAushadhiLocations = mockDataService.getPharmacies(lat, lng);

            // Audio response — use Hindi text for TTS when original language isn't supported
            String audioBase64 = null;
            try {
                audioBase64 = pollyService.synthesize(audioText, lang);
            } catch (Exception e) {
                log.warn("Polly TTS failed: {}", e.getMessage());
            }

            return PrescriptionResponse.builder()
                    .success(true)
                    .extractedText(extractedText)
                    .medicines(medicines)
                    .totalBrandCost(totalBrandCost)
                    .totalGenericCost(totalGenericCost)
                    .totalSavingsPercent(totalSavingsPercent)
                    .responseText(responseText)
                    .audioBase64(audioBase64)
                    .janAushadhiLocations(janAushadhiLocations)
                    .build();

        } catch (Exception e) {
            log.error("PrescriptionService error (text input): {}", e.getMessage(), e);
            return PrescriptionResponse.builder()
                    .success(false)
                    .error("Failed to process prescription: " + e.getMessage())
                    .build();
        }
    }

    

    private MedicineDto toMedicineDto(Map<String, Object> m) {
        int brandPrice = ((Number) m.getOrDefault("brandPrice", 0)).intValue();
        int genericPrice = ((Number) m.getOrDefault("genericPrice", 0)).intValue();
        int savingsAmount = brandPrice - genericPrice;
        int savingsPercent = brandPrice > 0 ? (savingsAmount * 100) / brandPrice : 0;

        return MedicineDto.builder()
                .brandName((String) m.getOrDefault("brandName", "Unknown"))
                .genericName((String) m.getOrDefault("genericName", "Unknown"))
                .dosage((String) m.getOrDefault("dosage", ""))
                .brandPrice(brandPrice)
                .genericPrice(genericPrice)
                .savingsPercent(((Number) m.getOrDefault("savingsPercent", savingsPercent)).intValue())
                .savingsAmount(((Number) m.getOrDefault("savingsAmount", savingsAmount)).intValue())
                .build();
    }

    public List<MedicineDto> searchMedicines(String query, String language) {
        log.info("Searching medicines for query: {}, language: {}", query, language);
        
        // Mock medicine data for demonstration
        List<MedicineDto> allMedicines = List.of(
                MedicineDto.builder()
                        .brandName("Crocin")
                        .genericName("Paracetamol")
                        .dosage("500mg")
                        .brandPrice(30)
                        .genericPrice(5)
                        .savingsPercent(83)
                        .savingsAmount(25)
                        .build(),
                MedicineDto.builder()
                        .brandName("Dolo 650")
                        .genericName("Paracetamol")
                        .dosage("650mg")
                        .brandPrice(35)
                        .genericPrice(8)
                        .savingsPercent(77)
                        .savingsAmount(27)
                        .build(),
                MedicineDto.builder()
                        .brandName("Augmentin")
                        .genericName("Amoxicillin + Clavulanic Acid")
                        .dosage("625mg")
                        .brandPrice(180)
                        .genericPrice(45)
                        .savingsPercent(75)
                        .savingsAmount(135)
                        .build(),
                MedicineDto.builder()
                        .brandName("Azithral")
                        .genericName("Azithromycin")
                        .dosage("500mg")
                        .brandPrice(120)
                        .genericPrice(25)
                        .savingsPercent(79)
                        .savingsAmount(95)
                        .build(),
                MedicineDto.builder()
                        .brandName("Pan D")
                        .genericName("Pantoprazole + Domperidone")
                        .dosage("40mg")
                        .brandPrice(150)
                        .genericPrice(30)
                        .savingsPercent(80)
                        .savingsAmount(120)
                        .build(),
                MedicineDto.builder()
                        .brandName("Omez")
                        .genericName("Omeprazole")
                        .dosage("20mg")
                        .brandPrice(85)
                        .genericPrice(12)
                        .savingsPercent(86)
                        .savingsAmount(73)
                        .build(),
                MedicineDto.builder()
                        .brandName("Combiflam")
                        .genericName("Ibuprofen + Paracetamol")
                        .dosage("400mg+325mg")
                        .brandPrice(45)
                        .genericPrice(10)
                        .savingsPercent(78)
                        .savingsAmount(35)
                        .build(),
                MedicineDto.builder()
                        .brandName("Allegra")
                        .genericName("Fexofenadine")
                        .dosage("120mg")
                        .brandPrice(180)
                        .genericPrice(35)
                        .savingsPercent(81)
                        .savingsAmount(145)
                        .build()
        );

        // Filter medicines based on query (case-insensitive search on brand/generic name)
        String searchTerm = query.toLowerCase().trim();
        return allMedicines.stream()
                .filter(m -> m.getBrandName().toLowerCase().contains(searchTerm) ||
                             m.getGenericName().toLowerCase().contains(searchTerm))
                .toList();
    }

    public List<PharmacyDto> getNearbyPharmacies(String lat, String lng) {
        log.info("Getting nearby pharmacies for lat: {}, lng: {}", lat, lng);
        return mockDataService.getPharmacies(lat, lng);
    }

    private Double safeParseDbl(String val) {
        if (val == null || val.isBlank()) return null;
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { return null; }
    }
}
