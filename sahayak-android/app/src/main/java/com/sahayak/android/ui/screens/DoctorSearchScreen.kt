package com.sahayak.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahayak.android.data.model.DoctorDto
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(
    viewModel: SahayakViewModel,
    onDoctorClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }

    // Load doctors on first composition
    LaunchedEffect(Unit) {
        viewModel.loadDoctors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.findDoctors) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchDoctors) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null; viewModel.loadDoctors() },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = selectedType == "government",
                    onClick = {
                        selectedType = "government"
                        viewModel.loadDoctors(type = "government")
                    },
                    label = { Text(strings.government) },
                )
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.doctorsLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp),
                )
            } else {
                val filtered = uiState.doctors.filter {
                    searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.specialty.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Text(
                        text = strings.noResults,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(32.dp),
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.id }) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                strings = strings,
                                onClick = { onDoctorClick(doctor.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorCard(
    doctor: DoctorDto,
    strings: com.sahayak.android.i18n.Strings,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = doctor.specialty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (doctor.type == "government") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = GovernmentGreen.copy(alpha = 0.15f),
                        ),
                    ) {
                        Text(
                            text = strings.government,
                            style = MaterialTheme.typography.labelSmall,
                            color = GovernmentGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${doctor.rating}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${doctor.distance} ${strings.distance}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (doctor.fee == 0) {
                    Text(
                        text = strings.free,
                        style = MaterialTheme.typography.labelSmall,
                        color = GovernmentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = "₹${doctor.fee}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
