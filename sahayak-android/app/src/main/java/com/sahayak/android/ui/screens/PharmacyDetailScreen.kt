package com.sahayak.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sahayak.android.data.model.PharmacyDto
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyDetailScreen(
    pharmacyId: String,
    viewModel: SahayakViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    val context = LocalContext.current

    // Resolve pharmacy from both sources (same logic as PharmacyResultsScreen)
    val allPharmacies = uiState.pharmacies.ifEmpty {
        uiState.prescriptionResult?.janAushadhiLocations ?: emptyList()
    }
    val pharmacy = allPharmacies.firstOrNull { it.id == pharmacyId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pharmacy?.name ?: strings.pharmacyDetails) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
    ) { padding ->
        if (pharmacy == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = strings.pharmacyNotFound,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        } else {
            PharmacyDetailContent(
                pharmacy = pharmacy,
                strings = strings,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                onOpenInMaps = {
                    val query = Uri.encode("${pharmacy.name}, ${pharmacy.address}")
                    val uri = Uri.parse("geo:0,0?q=$query")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val webUri = Uri.parse(
                            "https://www.google.com/maps/search/?api=1&query=$query",
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                onCall = {
                    if (pharmacy.phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pharmacy.phone}"))
                        context.startActivity(intent)
                    }
                },
            )
        }
    }
}

@Composable
private fun PharmacyDetailContent(
    pharmacy: PharmacyDto,
    strings: com.sahayak.android.i18n.Strings,
    modifier: Modifier = Modifier,
    onOpenInMaps: () -> Unit,
    onCall: () -> Unit,
) {
    // Staggered entrance — 4 sections
    val sectionCount = 4
    val alphas = remember { List(sectionCount) { Animatable(0f) } }
    val offsets = remember { List(sectionCount) { Animatable(30f) } }

    LaunchedEffect(Unit) {
        for (i in 0 until sectionCount) {
            delay(i * 70L)
            launch { alphas[i].animateTo(1f, tween(400, easing = EaseOutCubic)) }
            launch { offsets[i].animateTo(0f, tween(400, easing = EaseOutCubic)) }
        }
    }

    fun Modifier.sectionAnim(idx: Int): Modifier {
        val i = idx.coerceIn(0, sectionCount - 1)
        return this
            .alpha(alphas[i].value)
            .offset { IntOffset(0, offsets[i].value.toInt()) }
    }

    val isJanAushadhi = pharmacy.type == "jan-aushadhi"

    Column(modifier = modifier) {

        // ── Header card ──
        Card(
            modifier = Modifier.fillMaxWidth().sectionAnim(0),
            colors = CardDefaults.cardColors(
                containerColor = if (isJanAushadhi) GovernmentGreen.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (isJanAushadhi) GovernmentGreen
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = pharmacy.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = when (pharmacy.type) {
                                "jan-aushadhi" -> strings.janAushadhiKendra
                                "government" -> strings.governmentPharmacy
                                else -> strings.pharmacy
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isJanAushadhi) GovernmentGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Details section ──
        Card(modifier = Modifier.fillMaxWidth().sectionAnim(1)) {
            Column(Modifier.padding(16.dp)) {
                DetailRow(label = strings.distanceLabel, value = "${pharmacy.distance} ${strings.distance}")
                if (pharmacy.timings.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow(label = strings.timings, value = pharmacy.timings)
                }
                if (pharmacy.phone.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow(label = strings.phone, value = pharmacy.phone)
                }
                if (isJanAushadhi) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = strings.savings,
                        value = strings.savingsUpTo85,
                        valueColor = GovernmentGreen,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Address card ──
        Card(modifier = Modifier.fillMaxWidth().sectionAnim(2)) {
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
                    text = pharmacy.address.ifBlank { strings.addressNotAvailable },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Action buttons ──
        Column(modifier = Modifier.sectionAnim(3)) {
            // Open in Google Maps
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
                Text(strings.openInGoogleMaps, style = MaterialTheme.typography.titleMedium)
            }

            // Call button
            if (pharmacy.phone.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
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
                    Text(strings.callPharmacy, style = MaterialTheme.typography.titleMedium)
                }
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
