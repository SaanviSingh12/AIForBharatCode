package com.sahayak.android.data.model

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────
// Triage (Symptom Analysis)
// ─────────────────────────────────────────────

@Serializable
data class TriageResponse(
    val success: Boolean = false,
    val symptomText: String? = null,
    val specialist: String? = null,
    val isEmergency: Boolean = false,
    val urgencyLevel: String? = null,
    val summary: String? = null,
    val responseText: String? = null,
    val audioBase64: String? = null,
    val hospitals: List<HospitalDto> = emptyList(),
    val error: String? = null,
)

@Serializable
data class HospitalDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",                // "government" | "private"
    val specialist: String = "",
    val distance: Double = 0.0,
    val free: Boolean = false,
    val fee: Int? = null,
    val phone: String = "",
    val address: String = "",
    val hasEmergency: Boolean = false,
    val pmjayStatus: String = "",         // "empanelled" | "not-empanelled"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

@Serializable
data class HospitalPageResponse(
    val hospitals: List<HospitalDto> = emptyList(),
    val page: Int = 0,
    val pageSize: Int = 50,
    val totalCount: Long = 0,
    val hasMore: Boolean = false,
)

// ─────────────────────────────────────────────
// Prescription
// ─────────────────────────────────────────────

@Serializable
data class PrescriptionResponse(
    val success: Boolean = false,
    val extractedText: String? = null,
    val medicines: List<MedicineDto> = emptyList(),
    val totalBrandCost: Int = 0,
    val totalGenericCost: Int = 0,
    val totalSavingsPercent: Int = 0,
    val responseText: String? = null,
    val audioBase64: String? = null,
    val janAushadhiLocations: List<PharmacyDto> = emptyList(),
    val error: String? = null,
)

@Serializable
data class MedicineDto(
    val brandName: String = "",
    val genericName: String = "",
    val dosage: String = "",
    val brandPrice: Int = 0,
    val genericPrice: Int = 0,
    val savingsPercent: Int = 0,
    val savingsAmount: Int = 0,
)

@Serializable
data class PharmacyDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",                // "government" | "commercial" | "jan-aushadhi"
    val distance: Double = 0.0,
    val phone: String = "",
    val address: String = "",
    val timings: String = "",
)

// ─────────────────────────────────────────────
// Doctors
// ─────────────────────────────────────────────

@Serializable
data class DoctorDto(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val type: String = "",                // "government" | "independent" | "commercial"
    val distance: Double = 0.0,
    val available: Boolean = true,
    val fee: Int = 0,
    val phone: String = "",
    val address: String = "",
    val pmjay: Boolean = false,
    val rating: Double = 0.0,
    val waitTime: String = "",
    val experience: Int = 0,
    val languages: List<String> = emptyList(),
)

// ─────────────────────────────────────────────
// User Profile
// ─────────────────────────────────────────────

data class UserProfile(
    val name: String = "",
    val age: String = "",
    val gender: String = "",
)

// ─────────────────────────────────────────────
// Health Check
// ─────────────────────────────────────────────

@Serializable
data class HealthResponse(
    val status: String = "",
    val service: String = "",
    val version: String = "",
    val timestamp: String = "",
)
