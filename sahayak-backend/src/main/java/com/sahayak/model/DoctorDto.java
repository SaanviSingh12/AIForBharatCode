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
public class DoctorDto {
    private String id;
    private String name;
    private String specialty;
    private String type;  // "government", "independent", "commercial"
    private double distance;
    private boolean available;
    private int fee;
    private String phone;
    private String address;
    private boolean pmjay;
    private double rating;
    private String waitTime;
    private int experience;
    private List<String> languages;
}
