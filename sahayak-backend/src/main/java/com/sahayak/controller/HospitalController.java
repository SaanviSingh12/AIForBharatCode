package com.sahayak.controller;

import com.sahayak.model.HospitalDto;
import com.sahayak.model.HospitalPageResponse;
import com.sahayak.service.DynamoDbHospitalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/hospitals")
public class HospitalController {

    private final DynamoDbHospitalService hospitalService;

    public HospitalController(DynamoDbHospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    /**
     * GET /api/v1/hospitals
     *
     * Paginated hospital listing with optional type/query filters.
     * Default page size: 50, max page size: 50.
     *
     * @param page  0-based page index (default 0)
     * @param size  page size (default 50, max 50)
     * @param type  optional filter: "government" or "private"
     * @param query optional text search against name/address
     */
    @GetMapping
    public ResponseEntity<HospitalPageResponse> getHospitals(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "query", required = false) String query) {

        log.info("Hospital list request — page: {}, size: {}, type: {}, query: {}",
                page, size, type, query);

        HospitalPageResponse response = hospitalService.getHospitalsPaginated(page, size, type, query);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/hospitals/{id}
     *
     * Get a single hospital by ID (e.g. "MH-00001").
     */
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDto> getHospitalById(@PathVariable String id) {
        log.info("Hospital detail request — id: {}", id);

        return hospitalService.getHospitalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/hospitals/count
     *
     * Get hospital count from DynamoDB table metadata.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getHospitalCount() {
        return ResponseEntity.ok(hospitalService.getHospitalCount());
    }
}
