package com.sahayak.android.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahayak.android.ui.navigation.BottomNavItem
import com.sahayak.android.ui.navigation.Routes
import com.sahayak.android.ui.navigation.SahayakNavHost
import com.sahayak.android.ui.theme.SahayakTheme

@Composable
fun SahayakApp(viewModel: SahayakViewModel = hiltViewModel()) {
    SahayakTheme {
        val navController = rememberNavController()
        val uiState by viewModel.state.collectAsState()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // ── Request location permission + fetch coordinates ──
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.any { it }
            if (granted) viewModel.refreshLocation()
        }

        LaunchedEffect(Unit) {
            if (viewModel.locationProvider.hasPermission()) {
                viewModel.refreshLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        }

        // Hide bottom bar on language-selection and emergency screens
        val showBottomBar = currentDestination?.route != null &&
            currentDestination.route != Routes.LANGUAGE_SELECTION &&
            currentDestination.route != Routes.EMERGENCY

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    SahayakBottomBar(
                        currentRoute = currentDestination?.route,
                        strings = uiState.strings,
                        onItemClick = { item ->
                            if (item.route == Routes.HOME) {
                                // Pop everything back to Home
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            SahayakNavHost(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

// ─────────────────────────────────────────────────

@Composable
private fun SahayakBottomBar(
    currentRoute: String?,
    strings: com.sahayak.android.i18n.Strings,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            val label = when (item) {
                BottomNavItem.HOME         -> strings.home
                BottomNavItem.SYMPTOMS     -> strings.symptomChecker
                BottomNavItem.DOCTORS      -> strings.findDoctor
                BottomNavItem.PRESCRIPTION -> strings.prescription
                BottomNavItem.PROFILE      -> strings.profile
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = { Icon(imageVector = item.iconVector(), contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}

private fun BottomNavItem.iconVector(): ImageVector = when (this) {
    BottomNavItem.HOME         -> Icons.Default.Home
    BottomNavItem.SYMPTOMS     -> Icons.Default.Favorite
    BottomNavItem.DOCTORS      -> Icons.Default.MedicalServices
    BottomNavItem.PRESCRIPTION -> Icons.Default.Medication
    BottomNavItem.PROFILE      -> Icons.Default.Person
}
