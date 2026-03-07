package com.sahayak.android.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahayak.android.data.model.MedicineDto
import com.sahayak.android.data.model.PrescriptionResponse
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen
import com.sahayak.android.ui.theme.SavingsGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionResultsScreen(
    viewModel: SahayakViewModel,
    onPharmacyResults: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    val result = uiState.prescriptionResult

    // Handle system/gesture back — clear result so SearchScreen doesn't re-navigate
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        if (result == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No results available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // ── Savings summary banner ───────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SavingsGreen.copy(alpha = 0.1f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "💰", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "You can save ${result.totalSavingsPercent}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SavingsGreen,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Brand Cost",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "₹${result.totalBrandCost}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Generic Cost",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SavingsGreen,
                                )
                                Text(
                                    text = "₹${result.totalGenericCost}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SavingsGreen,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Medicine list header ─────────────
                Text(
                    text = "Medicines (${result.medicines.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))

                // ── Medicine cards ───────────────────
                result.medicines.forEach { medicine ->
                    ResultMedicineCard(medicine, strings)
                    Spacer(Modifier.height(10.dp))
                }

                // ── Response text ────────────────────
                result.responseText?.let { text ->
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                // ── Find nearby pharmacies button ────
                if (result.medicines.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onPharmacyResults,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(strings.nearbyPharmacies)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMedicineCard(
    medicine: MedicineDto,
    strings: com.sahayak.android.i18n.Strings,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            // Brand name + price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = medicine.brandName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "₹${medicine.brandPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Generic alternative + price + savings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.genericAlternative}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SavingsGreen,
                    )
                    Text(
                        text = medicine.genericName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SavingsGreen,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${medicine.genericPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SavingsGreen,
                    )
                    Text(
                        text = "Save ${medicine.savingsPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = SavingsGreen,
                    )
                }
            }

            // Dosage
            if (medicine.dosage.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = medicine.dosage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
