package com.sahayak.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahayak.android.data.model.HospitalDto
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.components.ShimmerLoadingList
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

    // Load first page on composition
    LaunchedEffect(Unit) {
        viewModel.loadHospitals()
    }

    // Lazy list state for infinite scroll
    val listState = rememberLazyListState()

    // Detect when user scrolls near the bottom → load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 5 && !uiState.hospitalsLoading && uiState.hasMoreHospitals
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreHospitals(type = selectedType, query = searchQuery.ifBlank { null })
        }
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
                windowInsets = WindowInsets(0),
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
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.loadHospitals(type = selectedType, query = query.ifBlank { null })
                },
                placeholder = { Text(strings.searchDoctors) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = {
                        selectedType = null
                        viewModel.loadHospitals(query = searchQuery.ifBlank { null })
                    },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = selectedType == "government",
                    onClick = {
                        selectedType = "government"
                        viewModel.loadHospitals(type = "government", query = searchQuery.ifBlank { null })
                    },
                    label = { Text(strings.government) },
                )
            }

            // Total count badge
            if (uiState.hospitalTotalCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${uiState.hospitalTotalCount} hospitals found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.hospitalsLoading && uiState.hospitals.isEmpty()) {
                ShimmerLoadingList(
                    itemCount = 4,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else if (uiState.hospitals.isEmpty()) {
                Text(
                    text = strings.noResults,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(32.dp),
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(uiState.hospitals, key = { _, h -> h.id }) { index, hospital ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(350, delayMillis = (index % 50) * 50, easing = EaseOutCubic)) +
                                slideInVertically(tween(350, delayMillis = (index % 50) * 50, easing = EaseOutCubic)) { it / 3 },
                        ) {
                            HospitalCard(
                                hospital = hospital,
                                strings = strings,
                                onClick = { onDoctorClick(hospital.id) },
                            )
                        }
                    }

                    // Loading indicator at the bottom while fetching next page
                    if (uiState.hospitalsLoading && uiState.hospitals.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalCard(
    hospital: HospitalDto,
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
                        text = hospital.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = hospital.specialist.ifBlank { "General" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (hospital.type == "government") {
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
                if (hospital.hasEmergency) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "Emergency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (hospital.distance > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${hospital.distance} ${strings.distance}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (hospital.free) {
                    Text(
                        text = strings.free,
                        style = MaterialTheme.typography.labelSmall,
                        color = GovernmentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                } else if (hospital.fee != null && hospital.fee > 0) {
                    Text(
                        text = "₹${hospital.fee}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Address line
            if (hospital.address.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hospital.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
