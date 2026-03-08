package com.sahayak.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailsScreen(
    doctorId: String,
    viewModel: SahayakViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    val context = LocalContext.current

    LaunchedEffect(doctorId) {
        viewModel.loadHospital(doctorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.findDoctor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
    ) { padding ->
        if (uiState.hospitalsLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(64.dp)
                    .size(48.dp),
            )
        } else {
            val hospital = uiState.selectedHospital
            if (hospital == null) {
                Text(
                    text = strings.noResults,
                    modifier = Modifier.padding(padding).padding(32.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    // Hospital name & specialist
                    Text(
                        text = hospital.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = hospital.specialist.ifBlank { strings.general },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(Modifier.height(16.dp))

                    // Info chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (hospital.hasEmergency) {
                            InfoChip(
                                icon = Icons.Default.LocalHospital,
                                text = strings.emergencyLabel,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (hospital.distance > 0) {
                            InfoChip(
                                icon = Icons.Default.LocationOn,
                                text = "${hospital.distance} ${strings.distance}",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            DetailRow(strings.type, hospital.type.replaceFirstChar { it.uppercase() })
                            DetailRow(strings.fee, if (hospital.free) strings.free else if (hospital.fee != null) "₹${hospital.fee}" else strings.notAvailable)
                            DetailRow(strings.pmjay, when (hospital.pmjayStatus) {
                                "empanelled" -> "✅ ${strings.empanelled}"
                                else -> "❌ ${strings.notEmpanelled}"
                            })
                            DetailRow(strings.emergencyLabel, if (hospital.hasEmergency) "✅ ${strings.available}" else "❌ ${strings.notAvailableStatus}")
                            if (hospital.address.isNotBlank()) {
                                DetailRow(strings.address, hospital.address)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Open in Google Maps button
                    if (hospital.latitude != 0.0 && hospital.longitude != 0.0) {
                        Button(
                            onClick = {
                                val query = Uri.encode("${hospital.name}, ${hospital.address}")
                                val uri = Uri.parse("geo:${hospital.latitude},${hospital.longitude}?q=$query")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val webUri = Uri.parse(
                                        "https://www.google.com/maps/search/?api=1&query=${hospital.latitude},${hospital.longitude}"
                                    )
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.openInGoogleMaps)
                        }

                        Spacer(Modifier.height(12.dp))
                    } else if (hospital.address.isNotBlank()) {
                        Button(
                            onClick = {
                                val query = Uri.encode("${hospital.name}, ${hospital.address}")
                                val uri = Uri.parse("geo:0,0?q=$query")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val webUri = Uri.parse(
                                        "https://www.google.com/maps/search/?api=1&query=$query"
                                    )
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.openInGoogleMaps)
                        }

                        Spacer(Modifier.height(12.dp))
                    }

                    // Call button
                    if (hospital.phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("${strings.call} ${hospital.phone}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
