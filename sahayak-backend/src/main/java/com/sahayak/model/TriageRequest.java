package com.sahayak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for the /api/v1/triage endpoint.
 * Audio is received as MultipartFile in the controller directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequest {
    private String language;      // e.g. "hi-IN", "ta-IN", "en-IN"
    private String lat;           // User latitude
    private String lng;           // User longitude
    private String directText;    // Optional: bypass voice, send text directly
}
