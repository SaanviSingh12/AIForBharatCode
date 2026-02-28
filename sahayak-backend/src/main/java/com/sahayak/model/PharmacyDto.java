package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyDto {
    private String id;
    private String name;
    private String type;        // "government" | "commercial"
    private double distance;
    private String phone;
    private String address;
    private String timings;
}
