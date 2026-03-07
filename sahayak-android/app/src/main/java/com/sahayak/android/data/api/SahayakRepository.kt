package com.sahayak.android.data.api

import com.sahayak.android.data.model.DoctorDto
import com.sahayak.android.data.model.PharmacyDto
import com.sahayak.android.data.model.PrescriptionResponse
import com.sahayak.android.data.model.TriageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that wraps [SahayakApi] with convenient helpers
 * for multipart uploads and error handling.
 */
@Singleton
class SahayakRepository @Inject constructor(
    private val api: SahayakApi,
) {

    // ── Triage ───────────────────────────────────

    suspend fun triageText(
        text: String,
        language: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<TriageResponse> = runCatching {
        withContext(Dispatchers.IO) {
            api.triageWithText(
                directText = text.toPlainBody(),
                language = language.toPlainBody(),
                latitude = latitude?.toString()?.toPlainBody(),
                longitude = longitude?.toString()?.toPlainBody(),
            )
        }
    }

    suspend fun triageAudio(
        audioFile: File,
        language: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<TriageResponse> = runCatching {
        withContext(Dispatchers.IO) {
            val audioPart = MultipartBody.Part.createFormData(
                "audio",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaType()),
            )
            api.triageWithAudio(
                audio = audioPart,
                language = language.toPlainBody(),
                latitude = latitude?.toString()?.toPlainBody(),
                longitude = longitude?.toString()?.toPlainBody(),
            )
        }
    }

    // ── Prescription ─────────────────────────────

    suspend fun prescriptionImage(
        imageFile: File,
        language: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<PrescriptionResponse> = runCatching {
        withContext(Dispatchers.IO) {
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/jpeg".toMediaType()),
            )
            api.prescriptionWithImage(
                image = imagePart,
                language = language.toPlainBody(),
                latitude = latitude?.toString()?.toPlainBody(),
                longitude = longitude?.toString()?.toPlainBody(),
            )
        }
    }

    suspend fun prescriptionText(
        medicineText: String,
        language: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<PrescriptionResponse> = runCatching {
        withContext(Dispatchers.IO) {
            api.prescriptionWithText(
                medicineText = medicineText.toPlainBody(),
                language = language.toPlainBody(),
                latitude = latitude?.toString()?.toPlainBody(),
                longitude = longitude?.toString()?.toPlainBody(),
            )
        }
    }

    suspend fun medicineSearch(
        query: String,
        language: String = "en-IN",
    ): Result<PrescriptionResponse> = runCatching {
        withContext(Dispatchers.IO) { api.medicineSearch(query, language) }
    }

    suspend fun nearbyPharmacies(
        latitude: Double,
        longitude: Double,
    ): Result<List<PharmacyDto>> = runCatching {
        withContext(Dispatchers.IO) { api.nearbyPharmacies(latitude, longitude) }
    }

    // ── Doctors ──────────────────────────────────

    suspend fun getDoctors(
        specialty: String? = null,
        type: String? = null,
        available: Boolean? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<List<DoctorDto>> = runCatching {
        withContext(Dispatchers.IO) { api.getDoctors(specialty, type, available, latitude, longitude) }
    }

    suspend fun getDoctor(id: String): Result<DoctorDto> = runCatching {
        withContext(Dispatchers.IO) { api.getDoctor(id) }
    }

    // ── Helpers ──────────────────────────────────

    private fun String.toPlainBody() =
        toRequestBody("text/plain".toMediaType())
}
