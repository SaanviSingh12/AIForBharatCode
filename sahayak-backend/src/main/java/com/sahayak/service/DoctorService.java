package com.sahayak.service;

import com.sahayak.model.DoctorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DoctorService {

    private static final double MAX_DISTANCE_KM = 50.0;

    /**
     * Get all doctors, sorted by type (government first) and distance.
     */
    public List<DoctorDto> getAllDoctors() {
        return getAllDoctors(null, null);
    }

    public List<DoctorDto> getAllDoctors(String userLat, String userLng) {
        return applyLocation(getMockDoctors(), userLat, userLng).stream()
                .sorted(Comparator
                        .comparing((DoctorDto d) -> !"government".equals(d.getType()))
                        .thenComparing(DoctorDto::getDistance))
                .toList();
    }

    /**
     * Get a doctor by ID.
     */
    public Optional<DoctorDto> getDoctorById(String id) {
        return getMockDoctors().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    /**
     * Search doctors by name or specialty.
     */
    public List<DoctorDto> searchDoctors(String query) {
        return searchDoctors(query, null, null);
    }

    public List<DoctorDto> searchDoctors(String query, String userLat, String userLng) {
        String searchTerm = query.toLowerCase().trim();
        log.info("Searching doctors for query: {}", searchTerm);

        return applyLocation(getMockDoctors(), userLat, userLng).stream()
                .filter(d -> d.getName().toLowerCase().contains(searchTerm) ||
                             d.getSpecialty().toLowerCase().contains(searchTerm))
                .sorted(Comparator
                        .comparing((DoctorDto d) -> !"government".equals(d.getType()))
                        .thenComparing(DoctorDto::getDistance))
                .toList();
    }

    /**
     * Get doctors by specialty.
     */
    public List<DoctorDto> getDoctorsBySpecialty(String specialty) {
        return getDoctorsBySpecialty(specialty, null, null);
    }

    public List<DoctorDto> getDoctorsBySpecialty(String specialty, String userLat, String userLng) {
        log.info("Finding doctors for specialty: {}", specialty);

        return applyLocation(getMockDoctors(), userLat, userLng).stream()
                .filter(d -> d.getSpecialty().equalsIgnoreCase(specialty))
                .sorted(Comparator
                        .comparing((DoctorDto d) -> !"government".equals(d.getType()))
                        .thenComparing(DoctorDto::getDistance))
                .toList();
    }

    /**
     * Get only government doctors (PMJAY eligible).
     */
    public List<DoctorDto> getGovernmentDoctors() {
        return getGovernmentDoctors(null, null);
    }

    public List<DoctorDto> getGovernmentDoctors(String userLat, String userLng) {
        return applyLocation(getMockDoctors(), userLat, userLng).stream()
                .filter(d -> "government".equals(d.getType()))
                .sorted(Comparator.comparing(DoctorDto::getDistance))
                .toList();
    }

    // ── Location helpers ──────────────────────────

    private List<DoctorDto> applyLocation(List<DoctorDto> doctors, String userLat, String userLng) {
        Double lat = parseCoord(userLat);
        Double lng = parseCoord(userLng);
        if (lat == null || lng == null) return doctors;

        return doctors.stream()
                .map(d -> {
                    if (d.getLatitude() != 0 && d.getLongitude() != 0) {
                        double dist = haversine(lat, lng, d.getLatitude(), d.getLongitude());
                        return DoctorDto.builder()
                                .id(d.getId()).name(d.getName()).specialty(d.getSpecialty())
                                .type(d.getType()).distance(Math.round(dist * 10.0) / 10.0)
                                .available(d.isAvailable()).fee(d.getFee()).phone(d.getPhone())
                                .address(d.getAddress()).pmjay(d.isPmjay()).rating(d.getRating())
                                .waitTime(d.getWaitTime()).experience(d.getExperience())
                                .languages(d.getLanguages())
                                .latitude(d.getLatitude()).longitude(d.getLongitude())
                                .build();
                    }
                    return d;
                })
                .filter(d -> d.getDistance() <= MAX_DISTANCE_KM)
                .toList();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
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
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Mock doctor data — includes Delhi/Gurgaon and Bangalore doctors.
     */
    private List<DoctorDto> getMockDoctors() {
        return List.of(
                // ── Delhi / Gurgaon doctors ──
                DoctorDto.builder()
                        .id("doc-1").name("Dr. Priya Sharma").specialty("General Physician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 98765 43210").address("Primary Health Centre, Sector 5, Gurgaon")
                        .pmjay(true).rating(4.5).waitTime("15 min").experience(12)
                        .languages(List.of("Hindi", "English", "Punjabi"))
                        .latitude(28.4595).longitude(77.0266)
                        .build(),
                DoctorDto.builder()
                        .id("doc-2").name("Dr. Rajesh Kumar").specialty("Cardiologist")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 98765 43211").address("District Hospital, Civil Lines, Gurgaon")
                        .pmjay(true).rating(4.7).waitTime("30 min").experience(18)
                        .languages(List.of("Hindi", "English"))
                        .latitude(28.4510).longitude(77.0120)
                        .build(),
                DoctorDto.builder()
                        .id("doc-3").name("Dr. Anita Desai").specialty("Pediatrician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 98765 43212").address("Community Health Centre, Sector 12, Gurgaon")
                        .pmjay(true).rating(4.8).waitTime("20 min").experience(15)
                        .languages(List.of("Hindi", "English", "Marathi"))
                        .latitude(28.4620).longitude(77.0380)
                        .build(),
                DoctorDto.builder()
                        .id("doc-4").name("Dr. Sunil Verma").specialty("Orthopedic")
                        .type("independent").distance(0).available(true).fee(500)
                        .phone("+91 98765 43213").address("Verma Clinic, Main Market, Gurgaon")
                        .pmjay(false).rating(4.3).waitTime("25 min").experience(10)
                        .languages(List.of("Hindi", "English"))
                        .latitude(28.4700).longitude(77.0400)
                        .build(),
                DoctorDto.builder()
                        .id("doc-5").name("Dr. Meena Gupta").specialty("Gynecologist")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 98765 43214").address("Women's Hospital, Sector 9, Gurgaon")
                        .pmjay(true).rating(4.6).waitTime("45 min").experience(20)
                        .languages(List.of("Hindi", "English", "Bengali"))
                        .latitude(28.4580).longitude(77.0300)
                        .build(),
                DoctorDto.builder()
                        .id("doc-6").name("Dr. Amit Patel").specialty("Dermatologist")
                        .type("commercial").distance(0).available(true).fee(800)
                        .phone("+91 98765 43215").address("Medanta Hospital, DLF Phase 2, Gurgaon")
                        .pmjay(false).rating(4.4).waitTime("10 min").experience(8)
                        .languages(List.of("Hindi", "English", "Gujarati"))
                        .latitude(28.4430).longitude(77.0370)
                        .build(),
                DoctorDto.builder()
                        .id("doc-7").name("Dr. Kavita Singh").specialty("ENT")
                        .type("independent").distance(0).available(true).fee(400)
                        .phone("+91 98765 43216").address("Singh ENT Clinic, Sector 14, Gurgaon")
                        .pmjay(false).rating(4.2).waitTime("20 min").experience(14)
                        .languages(List.of("Hindi", "English"))
                        .latitude(28.4650).longitude(77.0220)
                        .build(),
                DoctorDto.builder()
                        .id("doc-8").name("Dr. Ramesh Yadav").specialty("General Physician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 98765 43217").address("PHC Subhash Nagar, Gurgaon")
                        .pmjay(true).rating(4.1).waitTime("30 min").experience(22)
                        .languages(List.of("Hindi", "Haryanvi"))
                        .latitude(28.4500).longitude(77.0150)
                        .build(),

                // ── Bangalore doctors ──
                DoctorDto.builder()
                        .id("doc-blr-1").name("Dr. Lakshmi Narayan").specialty("General Physician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2670 1150").address("Victoria Hospital, Fort Road, Bengaluru")
                        .pmjay(true).rating(4.4).waitTime("20 min").experience(16)
                        .languages(List.of("Kannada", "Hindi", "English"))
                        .latitude(12.9567).longitude(77.5780)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-2").name("Dr. Suresh Babu").specialty("Cardiologist")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2653 4508").address("Jayadeva Institute, Jayanagar, Bengaluru")
                        .pmjay(true).rating(4.8).waitTime("40 min").experience(20)
                        .languages(List.of("Kannada", "Hindi", "English", "Telugu"))
                        .latitude(12.9254).longitude(77.5938)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-3").name("Dr. Kavitha Rao").specialty("Pediatrician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2559 1325").address("Bowring Hospital, Shivaji Nagar, Bengaluru")
                        .pmjay(true).rating(4.6).waitTime("25 min").experience(14)
                        .languages(List.of("Kannada", "English", "Tamil"))
                        .latitude(12.9833).longitude(77.6056)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-4").name("Dr. Arun Kumar").specialty("Neurologist")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2699 5000").address("NIMHANS, Hosur Road, Bengaluru")
                        .pmjay(true).rating(4.9).waitTime("60 min").experience(25)
                        .languages(List.of("Kannada", "Hindi", "English"))
                        .latitude(12.9416).longitude(77.5968)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-5").name("Dr. Meghana Shetty").specialty("Gynecologist")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2670 1137").address("Vani Vilas Hospital, Fort Road, Bengaluru")
                        .pmjay(true).rating(4.5).waitTime("30 min").experience(18)
                        .languages(List.of("Kannada", "English", "Tulu"))
                        .latitude(12.9580).longitude(77.5750)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-6").name("Dr. Vinay Hegde").specialty("Orthopedic")
                        .type("commercial").distance(0).available(true).fee(700)
                        .phone("+91 80-2502 4444").address("Manipal Hospital, Old Airport Road, Bengaluru")
                        .pmjay(false).rating(4.5).waitTime("15 min").experience(12)
                        .languages(List.of("Kannada", "Hindi", "English"))
                        .latitude(12.9611).longitude(77.6472)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-7").name("Dr. Preethi Ramesh").specialty("General Physician")
                        .type("government").distance(0).available(true).fee(0)
                        .phone("+91 80-2334 0017").address("KC General Hospital, Malleshwaram, Bengaluru")
                        .pmjay(true).rating(4.3).waitTime("20 min").experience(10)
                        .languages(List.of("Kannada", "Hindi", "English"))
                        .latitude(13.0033).longitude(77.5717)
                        .build(),
                DoctorDto.builder()
                        .id("doc-blr-8").name("Dr. Ravi Shankar").specialty("Dermatologist")
                        .type("independent").distance(0).available(true).fee(500)
                        .phone("+91 80-4123 4572").address("Skin Clinic, Indiranagar, Bengaluru")
                        .pmjay(false).rating(4.4).waitTime("10 min").experience(9)
                        .languages(List.of("Kannada", "English", "Hindi"))
                        .latitude(12.9784).longitude(77.6408)
                        .build()
        );
    }
}
