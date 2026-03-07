package com.sahayak.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahayak.model.HospitalDto;
import com.sahayak.service.DynamoDbHospitalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for managing hospital data in DynamoDB.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DynamoDbHospitalService dynamoDbHospitalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminController(DynamoDbHospitalService dynamoDbHospitalService) {
        this.dynamoDbHospitalService = dynamoDbHospitalService;
    }

    /**
     * POST /api/admin/sync-hospitals
     *
     * Loads hospitals from mock-hospitals.json and batch-writes to DynamoDB.
     * This is a one-time setup endpoint.
     */
    @PostMapping("/sync-hospitals")
    public ResponseEntity<Map<String, Object>> syncHospitals() {
        try {
            log.info("Starting hospital sync to DynamoDB...");

            ClassPathResource resource = new ClassPathResource("data/mock-hospitals.json");
            List<HospitalDto> hospitals = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<HospitalDto>>() {});

            log.info("Loaded {} hospitals from JSON, uploading to DynamoDB...", hospitals.size());

            int written = dynamoDbHospitalService.batchWriteHospitals(hospitals);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalLoaded", hospitals.size());
            result.put("totalWritten", written);
            result.put("message", "Hospital sync completed successfully");

            log.info("Hospital sync completed: {} written", written);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Hospital sync failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/admin/hospital-stats
     *
     * Returns stats about the hospital data in DynamoDB.
     */
    @GetMapping("/hospital-stats")
    public ResponseEntity<Map<String, Object>> getHospitalStats() {
        try {
            long count = dynamoDbHospitalService.getHospitalCount();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalHospitals", count);
            stats.put("tableName", "sahayak-hospitals");
            stats.put("status", count > 0 ? "active" : "empty");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
