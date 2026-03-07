package com.sahayak.controller;

import com.sahayak.model.MedicineDto;
import com.sahayak.model.PharmacyDto;
import com.sahayak.model.PrescriptionResponse;
import com.sahayak.service.PrescriptionService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

        PrescriptionResponse response = prescriptionService.analyzePrescription(imageFile, language, lat, lng);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/prescriptionText
     *
     * Accepts form data with prescription text directly:
     *   - prescription: prescription text to analyze
     *   - language: language code for response (hi-IN, en-IN, etc.)
     *   - lat: user latitude (for nearby pharmacy lookup)
     *   - lng: user longitude
     */
    @PostMapping(value = "/prescriptionText")
    public ResponseEntity<PrescriptionResponse> analyzeTextPrescription(
            @RequestParam(value = "prescription") String prescription,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {

        log.info("Prescription text request - language: {}, text length: {} chars",
                language,
                prescription.length());

        PrescriptionResponse response = prescriptionService.analyzePrescription(prescription, language, lat, lng);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prescription/medicineSearch")
    public ResponseEntity<List<MedicineDto>> searchMedicines(
            @RequestParam("query") String query,
            @RequestParam(value = "language", defaultValue = "hi-IN") String language) {

        log.info("Medicine search - query: {}, language: {}", query, language);
        List<MedicineDto> response = prescriptionService.searchMedicines(query, language);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prescription/nearbyPharmacies")
    public ResponseEntity<List<PharmacyDto>> getNearbyPharmacies(
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {

        log.info("Nearby pharmacies request - lat: {}, lng: {}", lat, lng);
        List<PharmacyDto> response = prescriptionService.getNearbyPharmacies(lat, lng);
        return ResponseEntity.ok(response);
    }
}