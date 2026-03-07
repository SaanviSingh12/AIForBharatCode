package com.sahayak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponse {
    private boolean success;
    private String symptomText;          // Transcribed / input text
    private String specialist;           // e.g. "Cardiologist"

    @JsonProperty("isEmergency")
    private boolean isEmergency;

    private String urgencyLevel;         // "emergency" | "urgent" | "low"
    private String summary;              // English summary
    private String responseText;         // Response in user's language
    private String audioBase64;          // Base64 MP3 from Polly (can be null if TTS fails)
    private List<HospitalDto> hospitals; // Sorted: govt first, then by distance
    private String error;                // Set if success=false
}
