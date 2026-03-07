package com.sahayak.android.data.api

import com.sahayak.android.data.model.DoctorDto
import com.sahayak.android.data.model.HealthResponse
import com.sahayak.android.data.model.PharmacyDto
import com.sahayak.android.data.model.PrescriptionResponse
import com.sahayak.android.data.model.TriageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SahayakApi {

    // ── Health ───────────────────────────────────
    @GET("api/v1/health")
    suspend fun health(): HealthResponse

    // ── Triage ───────────────────────────────────
    @Multipart
    @POST("api/v1/triage")
    suspend fun triageWithAudio(
        @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody,
        @Part("lat") latitude: RequestBody? = null,
        @Part("lng") longitude: RequestBody? = null,
    ): TriageResponse

    @Multipart
    @POST("api/v1/triage")
    suspend fun triageWithText(
        @Part("directText") directText: RequestBody,
        @Part("language") language: RequestBody,
        @Part("lat") latitude: RequestBody? = null,
        @Part("lng") longitude: RequestBody? = null,
    ): TriageResponse

    // ── Prescription (image) ─────────────────────
    @Multipart
    @POST("api/v1/prescription")
    suspend fun prescriptionWithImage(
        @Part image: MultipartBody.Part,
        @Part("language") language: RequestBody,
        @Part("lat") latitude: RequestBody? = null,
        @Part("lng") longitude: RequestBody? = null,
    ): PrescriptionResponse

    // ── Prescription (text) ──────────────────────
    @Multipart
    @POST("api/v1/prescriptionText")
    suspend fun prescriptionWithText(
        @Part("prescription") medicineText: RequestBody,
        @Part("language") language: RequestBody,
        @Part("lat") latitude: RequestBody? = null,
        @Part("lng") longitude: RequestBody? = null,
    ): PrescriptionResponse

    // ── Medicine search ──────────────────────────
    @GET("api/v1/prescription/medicineSearch")
    suspend fun medicineSearch(
        @Query("query") query: String,
        @Query("language") language: String = "en-IN",
    ): PrescriptionResponse

    // ── Nearby pharmacies ────────────────────────
    @GET("api/v1/prescription/nearbyPharmacies")
    suspend fun nearbyPharmacies(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
    ): List<PharmacyDto>

    // ── Doctors ──────────────────────────────────
    @GET("api/v1/doctors")
    suspend fun getDoctors(
        @Query("specialty") specialty: String? = null,
        @Query("type") type: String? = null,
        @Query("available") available: Boolean? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): List<DoctorDto>

    @GET("api/v1/doctors/{id}")
    suspend fun getDoctor(@Path("id") id: String): DoctorDto
}
