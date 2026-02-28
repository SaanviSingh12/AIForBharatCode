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
    public PrescriptionResponse analyzePrescription(MultipartFile imageFile, String language) {
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

            // AI analysis for generic alternatives
            String lang = language != null ? language : "hi-IN";
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

            // Jan Aushadhi locations
            List<PharmacyDto> janAushadhiLocations = mockDataService.getPharmacies();

            // Audio response
            String audioBase64 = null;
            try {
                audioBase64 = pollyService.synthesize(responseText, lang);
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
}
