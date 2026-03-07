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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahayak.android.i18n.Strings
import com.sahayak.android.ui.theme.EmergencyRed

@Composable
fun HomeScreen(
    strings: Strings,
    onNavigateToSymptoms: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToPrescription: () -> Unit,
    onNavigateToEmergency: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        // Header
        Text(
            text = "🏥 ${strings.welcome}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = strings.healthcareForAll,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        // Emergency card (prominent)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToEmergency),
            colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.1f)),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = strings.emergency,
                    tint = EmergencyRed,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = strings.emergency,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRed,
                    )
                    Text(
                        text = strings.callEmergency,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmergencyRed.copy(alpha = 0.8f),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Feature cards in a 2-column style
        QuickActionCard(
            icon = Icons.Default.MedicalServices,
            title = strings.symptomChecker,
            subtitle = strings.speakSymptoms,
            onClick = onNavigateToSymptoms,
        )
        Spacer(Modifier.height(12.dp))

        QuickActionCard(
            icon = Icons.Default.LocalHospital,
            title = strings.findDoctors,
            subtitle = strings.searchDoctors,
            onClick = onNavigateToDoctors,
        )
        Spacer(Modifier.height(12.dp))

        QuickActionCard(
            icon = Icons.Default.Medication,
            title = strings.prescriptionScan,
            subtitle = strings.genericAlternative,
            onClick = onNavigateToPrescription,
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
