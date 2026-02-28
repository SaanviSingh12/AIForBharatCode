package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDto {
    private String brandName;
    private String genericName;
    private String dosage;
    private int brandPrice;
    private int genericPrice;
    private int savingsPercent;
    private int savingsAmount;
}
