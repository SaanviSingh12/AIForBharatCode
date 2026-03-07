package com.sahayak.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahayak.model.HospitalDto;
import com.sahayak.model.MedicineDto;
import com.sahayak.model.PharmacyDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MockDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDbHospitalService dynamoDbHospitalService;
    private final DynamoDbKendraService dynamoDbKendraService;

    private List<HospitalDto> hospitals = new ArrayList<>();
    private List<PharmacyDto> pharmacies = new ArrayList<>();
    private List<MedicineDto> medicines = new ArrayList<>();

    @org.springframework.beans.factory.annotation.Value("${sahayak.use-real-aws:false}")
    private boolean useRealAws;

    // Max distance in km — only show hospitals/pharmacies within this radius
    private static final double MAX_DISTANCE_KM = 50.0;
    // Max results to return per query
    private static final int MAX_RESULTS = 20;

    public MockDataService(DynamoDbHospitalService dynamoDbHospitalService,
                           DynamoDbKendraService dynamoDbKendraService) {
        this.dynamoDbHospitalService = dynamoDbHospitalService;
        this.dynamoDbKendraService = dynamoDbKendraService;
    }

    @PostConstruct
    public void loadData() {
        if (useRealAws) {
            log.info("USE_REAL_AWS=true — skipping local JSON load, using DynamoDB for hospitals & kendras");
            // Still load medicines (not in DynamoDB yet)
            try {
                ClassPathResource medicinesRes = new ClassPathResource("data/mock-medicines.json");
                medicines = objectMapper.readValue(medicinesRes.getInputStream(),
                        new TypeReference<List<MedicineDto>>() {});
                log.info("Loaded {} mock medicines", medicines.size());
            } catch (Exception e) {
                log.warn("Could not load mock medicines: {}", e.getMessage());
            }
            return;
        }

        try {
            ClassPathResource hospitalsRes = new ClassPathResource("data/mock-hospitals.json");
            hospitals = objectMapper.readValue(hospitalsRes.getInputStream(),
                    new TypeReference<List<HospitalDto>>() {});
            log.info("Loaded {} mock hospitals", hospitals.size());

            ClassPathResource pharmaciesRes = new ClassPathResource("data/mock-pharmacies.json");
            pharmacies = objectMapper.readValue(pharmaciesRes.getInputStream(),
                    new TypeReference<List<PharmacyDto>>() {});
            log.info("Loaded {} mock pharmacies", pharmacies.size());

            ClassPathResource medicinesRes = new ClassPathResource("data/mock-medicines.json");
            medicines = objectMapper.readValue(medicinesRes.getInputStream(),
                    new TypeReference<List<MedicineDto>>() {});
            log.info("Loaded {} mock medicines", medicines.size());

        } catch (Exception e) {
            log.error("Failed to load mock data: {}", e.getMessage());
            loadHardcodedFallback();
        }
    }

    /**
     * Get hospitals filtered by specialist, emergency status,
     * and sorted by distance from user's location.
     */
    public List<HospitalDto> getHospitals(String specialist, boolean isEmergency) {
        return getHospitals(specialist, isEmergency, null, null);
    }

    /**
     * Get hospitals filtered by specialist, emergency, and user location.
     * When useRealAws=true and location is provided, queries DynamoDB.
     * Otherwise falls back to in-memory JSON data.
     */
    public List<HospitalDto> getHospitals(String specialist, boolean isEmergency,
                                           String userLat, String userLng) {
        Double lat = parseCoord(userLat);
        Double lng = parseCoord(userLng);
        boolean hasLocation = (lat != null && lng != null);

        log.info("Getting hospitals - specialist: {}, emergency: {}, userLat: {}, userLng: {}, hasLocation: {}, useRealAws: {}",
                specialist, isEmergency, userLat, userLng, hasLocation, useRealAws);

        // ── DynamoDB path (real AWS mode with location) ──
        if (useRealAws && hasLocation) {
            try {
                List<HospitalDto> dynamoResults = dynamoDbHospitalService.getHospitalsByLocation(
                        specialist, isEmergency, lat, lng);
                if (!dynamoResults.isEmpty()) {
                    log.info("DynamoDB returned {} hospitals", dynamoResults.size());
                    return dynamoResults;
                }
                log.warn("DynamoDB returned 0 results, falling back to JSON data");
            } catch (Exception e) {
                log.error("DynamoDB query failed, falling back to JSON: {}", e.getMessage());
            }
        }

        // ── In-memory JSON fallback ──

        List<HospitalDto> result = hospitals.stream()
                .map(h -> {
                    if (hasLocation && h.getLatitude() != 0 && h.getLongitude() != 0) {
                        double dist = haversine(lat, lng, h.getLatitude(), h.getLongitude());
                        return HospitalDto.builder()
                                .id(h.getId())
                                .name(h.getName())
                                .type(h.getType())
                                .specialist(h.getSpecialist())
                                .distance(Math.round(dist * 10.0) / 10.0) // round to 1 decimal
                                .free(h.isFree())
                                .fee(h.getFee())
                                .phone(h.getPhone())
                                .address(h.getAddress())
                                .hasEmergency(h.isHasEmergency())
                                .pmjayStatus(h.getPmjayStatus())
                                .latitude(h.getLatitude())
                                .longitude(h.getLongitude())
                                .build();
                    }
                    return h;
                })
                .filter(h -> !hasLocation || h.getDistance() <= MAX_DISTANCE_KM)
                .filter(h -> !isEmergency || h.isHasEmergency())
                .filter(h -> specialist == null
                        || h.getSpecialist() == null
                        || h.getSpecialist().equalsIgnoreCase(specialist)
                        || "General Physician".equalsIgnoreCase(h.getSpecialist()))
                .sorted(Comparator
                        .comparing((HospitalDto h) -> !"government".equals(h.getType()))
                        .thenComparing(HospitalDto::getDistance))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        log.info("Returning {} hospitals (from {} total)", result.size(), hospitals.size());
        return result.isEmpty() ? hospitals.subList(0, Math.min(5, hospitals.size())) : result;
    }

    /**
     * Get pharmacies, optionally filtered/sorted by user location.
     */
    public List<PharmacyDto> getPharmacies() {
        return getPharmacies(null, null);
    }

    public List<PharmacyDto> getPharmacies(String userLat, String userLng) {
        Double lat = parseCoord(userLat);
        Double lng = parseCoord(userLng);
        boolean hasLocation = (lat != null && lng != null);

        // ── DynamoDB path: real Jan Aushadhi Kendra data (18K+ locations) ──
        if (useRealAws && hasLocation) {
            try {
                List<PharmacyDto> dynamoResults = dynamoDbKendraService.getKendrasByLocation(lat, lng);
                if (!dynamoResults.isEmpty()) {
                    log.info("Returning {} kendras from DynamoDB", dynamoResults.size());
                    return dynamoResults;
                }
                log.warn("DynamoDB returned 0 kendras, falling back to local JSON");
            } catch (Exception e) {
                log.warn("DynamoDB kendra query failed, falling back: {}", e.getMessage());
            }
        }

        // ── Fallback: local mock-pharmacies.json ──
        return pharmacies.stream()
                .map(p -> {
                    if (hasLocation && p.getLatitude() != 0 && p.getLongitude() != 0) {
                        double dist = haversine(lat, lng, p.getLatitude(), p.getLongitude());
                        return PharmacyDto.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .type(p.getType())
                                .distance(Math.round(dist * 10.0) / 10.0)
                                .phone(p.getPhone())
                                .address(p.getAddress())
                                .timings(p.getTimings())
                                .latitude(p.getLatitude())
                                .longitude(p.getLongitude())
                                .build();
                    }
                    return p;
                })
                .filter(p -> !hasLocation || p.getDistance() <= MAX_DISTANCE_KM)
                .sorted(Comparator
                        .comparing((PharmacyDto p) -> !"jan-aushadhi".equals(p.getType()))
                        .thenComparing(PharmacyDto::getDistance))
                .collect(Collectors.toList());
    }

    public List<MedicineDto> getMedicines() {
        return medicines;
    }

    // ── Haversine formula ─────────────────────────

    /**
     * Calculate distance between two lat/lng points in kilometers.
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Double parseCoord(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void loadHardcodedFallback() {
        // Hospitals — mix of Delhi and Bangalore
        hospitals = List.of(
            HospitalDto.builder()
                .id("h1").name("District Government Hospital").type("government")
                .specialist("General Physician").distance(0).free(true).fee(0)
                .phone("+91-11-23456789").address("Civil Lines, New Delhi")
                .hasEmergency(true).pmjayStatus("Empanelled under PMJAY")
                .latitude(28.6862).longitude(77.2217)
                .build(),
            HospitalDto.builder()
                .id("h9").name("Victoria Hospital").type("government")
                .specialist("General Physician").distance(0).free(true).fee(0)
                .phone("+91-80-26701150").address("Fort Road, Bengaluru - 560002")
                .hasEmergency(true).pmjayStatus("Empanelled under PMJAY - Free services")
                .latitude(12.9567).longitude(77.5780)
                .build(),
            HospitalDto.builder()
                .id("h10").name("Bowring & Lady Curzon Hospital").type("government")
                .specialist("General Physician").distance(0).free(true).fee(0)
                .phone("+91-80-25591325").address("Shivaji Nagar, Bengaluru - 560001")
                .hasEmergency(true).pmjayStatus("Government Hospital - Free Services")
                .latitude(12.9833).longitude(77.6056)
                .build(),
            HospitalDto.builder()
                .id("h4").name("AIIMS Emergency").type("government")
                .specialist("Emergency Medicine").distance(0).free(true).fee(0)
                .phone("112").address("Ansari Nagar, New Delhi")
                .hasEmergency(true).pmjayStatus("National Reference Hospital")
                .latitude(28.5672).longitude(77.2100)
                .build()
        );

        // Pharmacies
        pharmacies = List.of(
            PharmacyDto.builder()
                .id("p1").name("Jan Aushadhi Kendra - Sector 4").type("jan-aushadhi")
                .distance(0).phone("+91-9876543210")
                .address("Shop 12, Sector 4 Market, New Delhi").timings("8 AM - 8 PM")
                .latitude(28.6280).longitude(77.2180)
                .build(),
            PharmacyDto.builder()
                .id("p6").name("Jan Aushadhi Kendra - Jayanagar").type("jan-aushadhi")
                .distance(0).phone("+91-80-41234567")
                .address("Jayanagar 4th Block, Bengaluru - 560041").timings("8 AM - 9 PM")
                .latitude(12.9250).longitude(77.5834)
                .build(),
            PharmacyDto.builder()
                .id("p3").name("MedPlus Pharmacy").type("private")
                .distance(0).phone("+91-9876543212")
                .address("Main Market, Connaught Place, New Delhi").timings("24 Hours")
                .latitude(28.6300).longitude(77.2160)
                .build()
        );

        // Medicines
        medicines = List.of(
            MedicineDto.builder()
                .brandName("Crocin").genericName("Paracetamol 500mg").dosage("1 tablet twice daily")
                .brandPrice(35).genericPrice(8).savingsPercent(77).savingsAmount(27)
                .build(),
            MedicineDto.builder()
                .brandName("Augmentin").genericName("Amoxicillin + Clavulanic Acid 625mg").dosage("1 tablet twice daily")
                .brandPrice(180).genericPrice(45).savingsPercent(75).savingsAmount(135)
                .build(),
            MedicineDto.builder()
                .brandName("Omez").genericName("Omeprazole 20mg").dosage("1 capsule before breakfast")
                .brandPrice(95).genericPrice(12).savingsPercent(87).savingsAmount(83)
                .build()
        );

        log.info("Loaded hardcoded fallback mock data");
    }
}
