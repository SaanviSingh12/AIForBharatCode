package com.sahayak.controller;

import com.sahayak.model.PrescriptionResponse;
import com.sahayak.service.PrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * POST /api/v1/prescription
     *
     * Accepts multipart form data:
     *   - image: prescription image file (JPEG/PNG/PDF)
     *   - language: language code for response (hi-IN, en-IN, etc.)
     *   - lat: user latitude (for nearby pharmacy lookup)
     *   - lng: user longitude
     */
    @PostMapping(value = "/prescription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionResponse> analyzePrescription(
            @RequestPart("image") MultipartFile imageFile,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {

        log.info("Prescription request - language: {}, image: {} ({} bytes)",
                language,
                imageFile.getOriginalFilename(),
                imageFile.getSize());

        PrescriptionResponse response = prescriptionService.analyzePrescription(imageFile, language);
        return ResponseEntity.ok(response);
    }
}
