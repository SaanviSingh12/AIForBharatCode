package com.sahayak.android.ui.navigation

/** Destination route constants. */
object Routes {
    const val LANGUAGE_SELECTION = "language_selection"
    const val HOME               = "home"
    const val SYMPTOM_ENTRY      = "symptom_entry"
    const val TRIAGE_RESULTS     = "triage_results"
    const val DOCTOR_SEARCH      = "doctor_search"
    const val DOCTOR_DETAILS     = "doctor_details/{doctorId}"
    const val PRESCRIPTION_SEARCH  = "prescription_search"
    const val PRESCRIPTION_RESULTS = "prescription_results"
    const val PHARMACY_RESULTS     = "pharmacy_results"
    const val EMERGENCY          = "emergency"
    const val USER_PROFILE       = "user_profile"
    const val HOSPITAL_DETAIL    = "hospital_detail/{hospitalId}"

    fun doctorDetails(id: String) = "doctor_details/$id"
    fun hospitalDetail(id: String) = "hospital_detail/$id"
}

/** Bottom-navigation destinations shown on the main scaffold. */
enum class BottomNavItem(
    val route: String,
    val labelResKey: String,  // i18n key
    val icon: String,         // Material icon name (resolved in composable)
) {
    HOME("home", "home", "home"),
    SYMPTOMS("symptom_entry", "symptomChecker", "medical_services"),
    DOCTORS("doctor_search", "findDoctors", "local_hospital"),
    PRESCRIPTION("prescription_search", "prescriptionScan", "medication"),
    PROFILE("user_profile", "profile", "person"),
}
