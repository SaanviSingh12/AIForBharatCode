package com.sahayak.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahayak.android.data.api.SahayakRepository
import com.sahayak.android.data.model.DoctorDto
import com.sahayak.android.data.model.PrescriptionResponse
import com.sahayak.android.data.model.TriageResponse
import com.sahayak.android.data.model.UserProfile
import com.sahayak.android.i18n.Strings
import com.sahayak.android.i18n.getAwsLanguageCode
import com.sahayak.android.i18n.getStrings
import com.sahayak.android.ui.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ── UI State ─────────────────────────────────

data class SahayakUiState(
    // Language
    val languageCode: String = "hi",          // short code
    val strings: Strings = getStrings("hi"),

    // User
    val userProfile: UserProfile = UserProfile(),

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAvailable: Boolean = false,

    // Triage
    val triageResult: TriageResponse? = null,
    val triageLoading: Boolean = false,

    // Prescription
    val prescriptionResult: PrescriptionResponse? = null,
    val prescriptionLoading: Boolean = false,

    // Doctors
    val doctors: List<DoctorDto> = emptyList(),
    val selectedDoctor: DoctorDto? = null,
    val doctorsLoading: Boolean = false,

    // Global
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ── ViewModel ────────────────────────────────

@HiltViewModel
class SahayakViewModel @Inject constructor(
    application: Application,
    private val repo: SahayakRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SahayakUiState())
    val state: StateFlow<SahayakUiState> = _state.asStateFlow()

    val locationProvider = LocationProvider(application)

    // ── Location ─────────────────────────────

    /** Fetch the device location and store in state. Call after permission is granted. */
    fun refreshLocation() {
        viewModelScope.launch {
            val location = locationProvider.getLastLocation()
            if (location != null) {
                _state.update {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationAvailable = true,
                    )
                }
            }
        }
    }

    /** Convenience accessors for the current cached coordinates. */
    private val lat get() = _state.value.latitude
    private val lng get() = _state.value.longitude

    // ── Language ──────────────────────────────

    fun setLanguage(code: String) {
        _state.update { it.copy(languageCode = code, strings = getStrings(code)) }
    }

    private val awsLang get() = getAwsLanguageCode(_state.value.languageCode)

    // ── User Profile ─────────────────────────

    fun updateProfile(profile: UserProfile) {
        _state.update { it.copy(userProfile = profile) }
    }

    // ── Triage (text) ────────────────────────

    fun analyzeSymptoms(
        text: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(triageLoading = true, error = null) }
            repo.triageText(text, awsLang, latitude ?: lat, longitude ?: lng)
                .onSuccess { result ->
                    _state.update { it.copy(triageResult = result, triageLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(triageLoading = false, error = ex.message) }
                }
        }
    }

    // ── Triage (audio) ───────────────────────

    fun analyzeSymptomAudio(
        audioFile: File,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(triageLoading = true, error = null) }
            repo.triageAudio(audioFile, awsLang, latitude ?: lat, longitude ?: lng)
                .onSuccess { result ->
                    _state.update { it.copy(triageResult = result, triageLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(triageLoading = false, error = ex.message) }
                }
        }
    }

    // ── Prescription (image) ─────────────────

    fun analyzePrescriptionImage(
        imageFile: File,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(prescriptionLoading = true, error = null) }
            repo.prescriptionImage(imageFile, awsLang, latitude ?: lat, longitude ?: lng)
                .onSuccess { result ->
                    _state.update { it.copy(prescriptionResult = result, prescriptionLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(prescriptionLoading = false, error = ex.message) }
                }
        }
    }

    // ── Prescription (text) ──────────────────

    fun analyzePrescriptionText(
        medicineText: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(prescriptionLoading = true, error = null) }
            repo.prescriptionText(medicineText, awsLang, latitude ?: lat, longitude ?: lng)
                .onSuccess { result ->
                    _state.update { it.copy(prescriptionResult = result, prescriptionLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(prescriptionLoading = false, error = ex.message) }
                }
        }
    }

    // ── Medicine search ──────────────────────

    fun searchMedicine(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(prescriptionLoading = true, error = null) }
            repo.medicineSearch(query, awsLang)
                .onSuccess { result ->
                    _state.update { it.copy(prescriptionResult = result, prescriptionLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(prescriptionLoading = false, error = ex.message) }
                }
        }
    }

    // ── Doctors ──────────────────────────────

    fun loadDoctors(
        specialty: String? = null,
        type: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(doctorsLoading = true, error = null) }
            repo.getDoctors(specialty, type, available = true, latitude ?: lat, longitude ?: lng)
                .onSuccess { list ->
                    _state.update { it.copy(doctors = list, doctorsLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(doctorsLoading = false, error = ex.message) }
                }
        }
    }

    fun loadDoctor(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(doctorsLoading = true, error = null) }
            repo.getDoctor(id)
                .onSuccess { doc ->
                    _state.update { it.copy(selectedDoctor = doc, doctorsLoading = false) }
                }
                .onFailure { ex ->
                    _state.update { it.copy(doctorsLoading = false, error = ex.message) }
                }
        }
    }

    // ── Clear ────────────────────────────────

    fun clearTriageResult() {
        _state.update { it.copy(triageResult = null) }
    }

    fun clearPrescriptionResult() {
        _state.update { it.copy(prescriptionResult = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
