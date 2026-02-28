package com.sahayak.controller;

import com.sahayak.model.TriageRequest;
import com.sahayak.model.TriageResponse;
import com.sahayak.service.VoiceTriageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TriageController {

    private final VoiceTriageService voiceTriageService;

    public TriageController(VoiceTriageService voiceTriageService) {
        this.voiceTriageService = voiceTriageService;
    }

    /**
     * POST /api/v1/triage
     *
     * Accepts multipart form data:
     *   - audio: optional audio file (WAV/MP3/WebM)
     *   - language: language code (hi-IN, en-IN, ta-IN, etc.)
     *   - lat: user latitude
     *   - lng: user longitude
     *   - directText: optional text input (bypasses transcription)
     */
    @PostMapping(value = "/triage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TriageResponse> triage(
            @RequestPart(value = "audio", required = false) MultipartFile audioFile,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng,
            @RequestParam(value = "directText", required = false) String directText) {

        log.info("Triage request - language: {}, directText: {}, hasAudio: {}",
                language, directText != null, audioFile != null && !audioFile.isEmpty());

        TriageRequest request = TriageRequest.builder()
                .language(language)
                .lat(lat)
                .lng(lng)
                .directText(directText)
                .build();

        TriageResponse response = voiceTriageService.processVoiceTriage(audioFile, request);
        return ResponseEntity.ok(response);
    }
}
