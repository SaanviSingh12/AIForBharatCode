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
    private List<HospitalDto> hospitals = new ArrayList<>();
    private List<PharmacyDto> pharmacies = new ArrayList<>();
    private List<MedicineDto> medicines = new ArrayList<>();

    @PostConstruct
    public void loadData() {
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

    public List<HospitalDto> getHospitals(String specialist, boolean isEmergency) {
        List<HospitalDto> result = hospitals.stream()
                .filter(h -> !isEmergency || h.isHasEmergency())
                .filter(h -> specialist == null
                        || h.getSpecialist() == null
                        || h.getSpecialist().equalsIgnoreCase(specialist)
                        || "General Physician".equalsIgnoreCase(h.getSpecialist()))
                .sorted(Comparator.comparing(h -> !"government".equals(h.getType())))
                .collect(Collectors.toList());

        return result.isEmpty() ? hospitals : result;
    }

    public List<PharmacyDto> getPharmacies() {
        return pharmacies;
    }

    public List<MedicineDto> getMedicines() {
        return medicines;
    }

    private void loadHardcodedFallback() {
        // Hospitals
        hospitals = List.of(
            HospitalDto.builder()
                .id("h1").name("District Government Hospital").type("government")
                .specialist("General Physician").distance(0.8).free(true).fee(0)
                .phone("+91-11-23456789").address("Civil Lines, New Delhi")
                .hasEmergency(true).pmjayStatus("Empanelled under PMJAY")
                .build(),
            HospitalDto.builder()
                .id("h2").name("Primary Health Centre - Sector 5").type("government")
                .specialist("General Physician").distance(1.2).free(true).fee(0)
                .phone("+91-11-23456780").address("Sector 5, New Delhi")
                .hasEmergency(false).pmjayStatus("Government PHC - Free Services")
                .build(),
            HospitalDto.builder()
                .id("h3").name("Apollo Clinic").type("private")
                .specialist("General Physician").distance(2.1).free(false).fee(500)
                .phone("+91-11-40501234").address("Connaught Place, New Delhi")
                .hasEmergency(false).pmjayStatus(null)
                .build(),
            HospitalDto.builder()
                .id("h4").name("AIIMS Emergency").type("government")
                .specialist("Emergency Medicine").distance(3.5).free(true).fee(0)
                .phone("112").address("Ansari Nagar, New Delhi")
                .hasEmergency(true).pmjayStatus("National Reference Hospital")
                .build()
        );

        // Pharmacies
        pharmacies = List.of(
            PharmacyDto.builder()
                .id("p1").name("Jan Aushadhi Kendra - Sector 4").type("jan-aushadhi")
                .distance(0.5).phone("+91-9876543210")
                .address("Shop 12, Sector 4 Market, New Delhi").timings("8 AM - 8 PM")
                .build(),
            PharmacyDto.builder()
                .id("p2").name("Pradhan Mantri Bhartiya Janaushadhi Kendra").type("jan-aushadhi")
                .distance(1.1).phone("+91-9876543211")
                .address("Block B, Civil Lines, New Delhi").timings("7 AM - 9 PM")
                .build(),
            PharmacyDto.builder()
                .id("p3").name("MedPlus Pharmacy").type("private")
                .distance(1.8).phone("+91-9876543212")
                .address("Main Market, Connaught Place, New Delhi").timings("24 Hours")
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
