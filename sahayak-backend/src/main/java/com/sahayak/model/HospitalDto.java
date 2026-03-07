package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDto {
    private String id;
    private String name;
    private String type;         // "government" | "private"
    private String specialist;
    private double distance;     // km (calculated from user location)
    private boolean free;        // true for govt/PMJAY hospitals
    private Integer fee;         // consultation fee for private (null if free)
    private String phone;
    private String address;
    private boolean hasEmergency;
    private String pmjayStatus;  // "empanelled" | "not-empanelled"
    private double latitude;     // hospital latitude
    private double longitude;    // hospital longitude
}
