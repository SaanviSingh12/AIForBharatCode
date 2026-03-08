package com.sahayak.android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
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
import com.sahayak.android.ui.screens.PharmacyDetailScreen
import com.sahayak.android.ui.screens.PharmacyResultsScreen
import com.sahayak.android.ui.screens.PrescriptionResultsScreen
import com.sahayak.android.ui.screens.PrescriptionSearchScreen
import com.sahayak.android.ui.screens.SymptomEntryScreen
import com.sahayak.android.ui.screens.TriageResultsScreen
import com.sahayak.android.ui.screens.UserProfileScreen

// ── Transition constants ────────────────────────────────────
private const val NAV_ANIM_DURATION = 350
private const val NAV_ANIM_OFFSET_RATIO = 4  // 1/4 of screen width

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromRight(): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(NAV_ANIM_DURATION, easing = EaseInOut),
        initialOffset = { it / NAV_ANIM_OFFSET_RATIO },
    ) + fadeIn(tween(NAV_ANIM_DURATION, easing = EaseInOut))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToLeft(): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(NAV_ANIM_DURATION, easing = EaseInOut),
        targetOffset = { it / NAV_ANIM_OFFSET_RATIO },
    ) + fadeOut(tween(NAV_ANIM_DURATION, easing = EaseInOut))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromLeft(): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(NAV_ANIM_DURATION, easing = EaseInOut),
        initialOffset = { it / NAV_ANIM_OFFSET_RATIO },
    ) + fadeIn(tween(NAV_ANIM_DURATION, easing = EaseInOut))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToRight(): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(NAV_ANIM_DURATION, easing = EaseInOut),
        targetOffset = { it / NAV_ANIM_OFFSET_RATIO },
    ) + fadeOut(tween(NAV_ANIM_DURATION, easing = EaseInOut))

// Emergency: scale + fade (alert feel)
private fun emergencyEnter(): EnterTransition =
    scaleIn(
        animationSpec = tween(300, easing = EaseInOut),
        initialScale = 0.85f,
    ) + fadeIn(tween(300))

private fun emergencyExit(): ExitTransition =
    scaleOut(
        animationSpec = tween(250, easing = EaseInOut),
        targetScale = 0.85f,
    ) + fadeOut(tween(250))

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
        // Default transitions for all routes
        enterTransition = { slideInFromRight() },
        exitTransition = { slideOutToLeft() },
        popEnterTransition = { slideInFromLeft() },
        popExitTransition = { slideOutToRight() },
    ) {

        composable(
            Routes.LANGUAGE_SELECTION,
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(300)) },
        ) {
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
                onTriageResults = { navController.navigate(Routes.TRIAGE_RESULTS) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.TRIAGE_RESULTS) {
            TriageResultsScreen(
                viewModel = viewModel,
                onHospitalClick = { id -> navController.navigate(Routes.hospitalDetail(id)) },
                onBack = {
                    viewModel.clearTriageResult()
                    navController.popBackStack()
                },
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
                onPharmacyClick = { id -> navController.navigate(Routes.pharmacyDetail(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.PHARMACY_DETAIL,
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val pharmacyId = backStackEntry.arguments?.getString("pharmacyId") ?: ""
            PharmacyDetailScreen(
                pharmacyId = pharmacyId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Routes.EMERGENCY,
            enterTransition = { emergencyEnter() },
            exitTransition = { emergencyExit() },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { emergencyExit() },
        ) {
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
                onChangeLanguage = {
                    navController.navigate(Routes.LANGUAGE_SELECTION)
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
