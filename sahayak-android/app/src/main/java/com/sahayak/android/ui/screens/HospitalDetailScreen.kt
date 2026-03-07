package com.sahayak.android.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahayak.android.data.model.HospitalDto
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.EmergencyRed
import com.sahayak.android.ui.theme.GovernmentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDetailScreen(
    hospitalId: String,
    viewModel: SahayakViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    val context = LocalContext.current

    // Find the hospital from the current triage result
    val hospital = uiState.triageResult?.hospitals?.firstOrNull { it.id == hospitalId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hospital?.name ?: "Hospital Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        if (hospital == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Hospital not found",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        } else {
            HospitalDetailContent(
                hospital = hospital,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                onOpenInMaps = {
                    val query = Uri.encode("${hospital.name}, ${hospital.address}")
                    val uri = Uri.parse("geo:0,0?q=$query")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    // Fall back to browser if Google Maps is not installed
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val webUri = Uri.parse(
                            "https://www.google.com/maps/search/?api=1&query=$query"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                onCall = {
                    if (hospital.phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.phone}"))
                        context.startActivity(intent)
                    }
                },
            )
        }
    }
}

@Composable
private fun HospitalDetailContent(
    hospital: HospitalDto,
    modifier: Modifier = Modifier,
    onOpenInMaps: () -> Unit,
    onCall: () -> Unit,
) {
    Column(modifier = modifier) {

        // ── Header card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hospital.type == "government")
                    GovernmentGreen.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = if (hospital.type == "government") GovernmentGreen
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = hospital.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (hospital.type == "government") "Government Hospital"
                            else "Private Hospital",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hospital.type == "government") GovernmentGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Details section ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                DetailRow(label = "Specialist", value = hospital.specialist)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow(label = "Distance", value = "${hospital.distance} km away")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = "Fee",
                    value = if (hospital.free) "FREE" else "₹${hospital.fee ?: "N/A"}",
                    valueColor = if (hospital.free) GovernmentGreen else null,
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = "Emergency",
                    value = if (hospital.hasEmergency) "Available" else "Not available",
                    valueColor = if (hospital.hasEmergency) GovernmentGreen else EmergencyRed,
                )
                if (hospital.pmjayStatus.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow(label = "PMJAY Status", value = hospital.pmjayStatus)
                }
                if (hospital.phone.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow(label = "Phone", value = hospital.phone)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Address card ──
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = hospital.address,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Open in Google Maps button ──
        Button(
            onClick = onOpenInMaps,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Open in Google Maps", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(12.dp))

        // ── Call button ──
        if (hospital.phone.isNotBlank()) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Call Hospital", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}
