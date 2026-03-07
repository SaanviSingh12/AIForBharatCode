package com.sahayak.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.screens.DoctorDetailsScreen
import com.sahayak.android.ui.screens.DoctorSearchScreen
import com.sahayak.android.ui.screens.EmergencyScreen
import com.sahayak.android.ui.screens.HomeScreen
import com.sahayak.android.ui.screens.HospitalDetailScreen
import com.sahayak.android.ui.screens.LanguageSelectionScreen
import com.sahayak.android.ui.screens.PharmacyResultsScreen
import com.sahayak.android.ui.screens.PrescriptionResultsScreen
import com.sahayak.android.ui.screens.PrescriptionSearchScreen
import com.sahayak.android.ui.screens.SymptomEntryScreen
import com.sahayak.android.ui.screens.UserProfileScreen

@Composable
fun SahayakNavHost(
    navController: NavHostController,
    viewModel: SahayakViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LANGUAGE_SELECTION,
        modifier = modifier,
    ) {

        composable(Routes.LANGUAGE_SELECTION) {
            LanguageSelectionScreen(
                strings = uiState.strings,
                onLanguageSelected = { code ->
                    viewModel.setLanguage(code)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LANGUAGE_SELECTION) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                strings = uiState.strings,
                onNavigateToSymptoms = { navController.navigate(Routes.SYMPTOM_ENTRY) },
                onNavigateToDoctors = { navController.navigate(Routes.DOCTOR_SEARCH) },
                onNavigateToPrescription = { navController.navigate(Routes.PRESCRIPTION_SEARCH) },
                onNavigateToEmergency = { navController.navigate(Routes.EMERGENCY) },
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
            )
        }

        composable(Routes.SYMPTOM_ENTRY) {
            SymptomEntryScreen(
                viewModel = viewModel,
                onEmergencyDetected = {
                    navController.navigate(Routes.EMERGENCY) {
                        popUpTo(Routes.HOME)
                    }
                },
                onHospitalClick = { id -> navController.navigate(Routes.hospitalDetail(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.HOSPITAL_DETAIL,
            arguments = listOf(navArgument("hospitalId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
            HospitalDetailScreen(
                hospitalId = hospitalId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DOCTOR_SEARCH) {
            DoctorSearchScreen(
                viewModel = viewModel,
                onDoctorClick = { id -> navController.navigate(Routes.doctorDetails(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.DOCTOR_DETAILS,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            DoctorDetailsScreen(
                doctorId = doctorId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PRESCRIPTION_SEARCH) {
            PrescriptionSearchScreen(
                viewModel = viewModel,
                onPharmacyResults = { navController.navigate(Routes.PHARMACY_RESULTS) },
                onPrescriptionResults = { navController.navigate(Routes.PRESCRIPTION_RESULTS) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PRESCRIPTION_RESULTS) {
            PrescriptionResultsScreen(
                viewModel = viewModel,
                onPharmacyResults = { navController.navigate(Routes.PHARMACY_RESULTS) },
                onBack = {
                    viewModel.clearPrescriptionResult()
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.PHARMACY_RESULTS) {
            PharmacyResultsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.EMERGENCY) {
            EmergencyScreen(
                strings = uiState.strings,
                triageResult = uiState.triageResult,
                onBack = {
                    viewModel.clearTriageResult()
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
