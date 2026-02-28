package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private boolean success;
    private String extractedText;
    private List<MedicineDto> medicines;
    private int totalBrandCost;
    private int totalGenericCost;
    private int totalSavingsPercent;
    private String responseText;
    private String audioBase64;
    private List<PharmacyDto> janAushadhiLocations;
    private String error;
}
