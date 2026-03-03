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

    /**
     * Get all doctors, sorted by type (government first) and distance.
     */
    public List<DoctorDto> getAllDoctors() {
        return getMockDoctors().stream()
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
        String searchTerm = query.toLowerCase().trim();
        log.info("Searching doctors for query: {}", searchTerm);

        return getMockDoctors().stream()
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
        log.info("Finding doctors for specialty: {}", specialty);

        return getMockDoctors().stream()
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
        return getMockDoctors().stream()
                .filter(d -> "government".equals(d.getType()))
                .sorted(Comparator.comparing(DoctorDto::getDistance))
                .toList();
    }

    /**
     * Mock doctor data for demonstration.
     */
    private List<DoctorDto> getMockDoctors() {
        return List.of(
                DoctorDto.builder()
                        .id("doc-1")
                        .name("Dr. Priya Sharma")
                        .specialty("General Physician")
                        .type("government")
                        .distance(2.5)
                        .available(true)
                        .fee(0)
                        .phone("+91 98765 43210")
                        .address("Primary Health Centre, Sector 5, Gurgaon")
                        .pmjay(true)
                        .rating(4.5)
                        .waitTime("15 min")
                        .experience(12)
                        .languages(List.of("Hindi", "English", "Punjabi"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-2")
                        .name("Dr. Rajesh Kumar")
                        .specialty("Cardiologist")
                        .type("government")
                        .distance(4.2)
                        .available(true)
                        .fee(0)
                        .phone("+91 98765 43211")
                        .address("District Hospital, Civil Lines, Gurgaon")
                        .pmjay(true)
                        .rating(4.7)
                        .waitTime("30 min")
                        .experience(18)
                        .languages(List.of("Hindi", "English"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-3")
                        .name("Dr. Anita Desai")
                        .specialty("Pediatrician")
                        .type("government")
                        .distance(3.8)
                        .available(true)
                        .fee(0)
                        .phone("+91 98765 43212")
                        .address("Community Health Centre, Sector 12, Gurgaon")
                        .pmjay(true)
                        .rating(4.8)
                        .waitTime("20 min")
                        .experience(15)
                        .languages(List.of("Hindi", "English", "Marathi"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-4")
                        .name("Dr. Sunil Verma")
                        .specialty("Orthopedic")
                        .type("independent")
                        .distance(5.1)
                        .available(true)
                        .fee(500)
                        .phone("+91 98765 43213")
                        .address("Verma Clinic, Main Market, Gurgaon")
                        .pmjay(false)
                        .rating(4.3)
                        .waitTime("25 min")
                        .experience(10)
                        .languages(List.of("Hindi", "English"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-5")
                        .name("Dr. Meena Gupta")
                        .specialty("Gynecologist")
                        .type("government")
                        .distance(6.0)
                        .available(true)
                        .fee(0)
                        .phone("+91 98765 43214")
                        .address("Women's Hospital, Sector 9, Gurgaon")
                        .pmjay(true)
                        .rating(4.6)
                        .waitTime("45 min")
                        .experience(20)
                        .languages(List.of("Hindi", "English", "Bengali"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-6")
                        .name("Dr. Amit Patel")
                        .specialty("Dermatologist")
                        .type("commercial")
                        .distance(3.2)
                        .available(true)
                        .fee(800)
                        .phone("+91 98765 43215")
                        .address("Medanta Hospital, DLF Phase 2, Gurgaon")
                        .pmjay(false)
                        .rating(4.4)
                        .waitTime("10 min")
                        .experience(8)
                        .languages(List.of("Hindi", "English", "Gujarati"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-7")
                        .name("Dr. Kavita Singh")
                        .specialty("ENT")
                        .type("independent")
                        .distance(4.5)
                        .available(true)
                        .fee(400)
                        .phone("+91 98765 43216")
                        .address("Singh ENT Clinic, Sector 14, Gurgaon")
                        .pmjay(false)
                        .rating(4.2)
                        .waitTime("20 min")
                        .experience(14)
                        .languages(List.of("Hindi", "English"))
                        .build(),
                DoctorDto.builder()
                        .id("doc-8")
                        .name("Dr. Ramesh Yadav")
                        .specialty("General Physician")
                        .type("government")
                        .distance(1.8)
                        .available(true)
                        .fee(0)
                        .phone("+91 98765 43217")
                        .address("PHC Subhash Nagar, Gurgaon")
                        .pmjay(true)
                        .rating(4.1)
                        .waitTime("30 min")
                        .experience(22)
                        .languages(List.of("Hindi", "Haryanvi"))
                        .build()
        );
    }
}
