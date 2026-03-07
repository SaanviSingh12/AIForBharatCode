package com.sahayak.controller;

import com.sahayak.model.DoctorDto;
import com.sahayak.service.DoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * GET /api/v1/doctors
     * 
     * Get all doctors, sorted by type (government first) and distance.
     * Optional query param to search by name or specialty.
     */
    @GetMapping
    public ResponseEntity<List<DoctorDto>> getDoctors(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "specialty", required = false) String specialty,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {

        log.info("Doctors request - query: {}, specialty: {}, lat: {}, lng: {}", query, specialty, lat, lng);

        List<DoctorDto> doctors;

        if (query != null && !query.isBlank()) {
            doctors = doctorService.searchDoctors(query, lat, lng);
        } else if (specialty != null && !specialty.isBlank()) {
            doctors = doctorService.getDoctorsBySpecialty(specialty, lat, lng);
        } else {
            doctors = doctorService.getAllDoctors(lat, lng);
        }

        return ResponseEntity.ok(doctors);
    }

    /**
     * GET /api/v1/doctors/{id}
     * 
     * Get a specific doctor by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable String id) {
        log.info("Doctor detail request - id: {}", id);

        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/doctors/government
     * 
     * Get only government doctors (PMJAY eligible, free consultation).
     */
    @GetMapping("/government")
    public ResponseEntity<List<DoctorDto>> getGovernmentDoctors(
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {
        log.info("Government doctors request - lat: {}, lng: {}", lat, lng);

        List<DoctorDto> doctors = doctorService.getGovernmentDoctors(lat, lng);
        return ResponseEntity.ok(doctors);
    }

    /**
     * GET /api/v1/doctors/specialty/{specialty}
     * 
     * Get doctors by specialty.
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorDto>> getDoctorsBySpecialty(
            @PathVariable String specialty,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng) {
        log.info("Doctors by specialty request - specialty: {}, lat: {}, lng: {}", specialty, lat, lng);

        List<DoctorDto> doctors = doctorService.getDoctorsBySpecialty(specialty, lat, lng);
        return ResponseEntity.ok(doctors);
    }
}
