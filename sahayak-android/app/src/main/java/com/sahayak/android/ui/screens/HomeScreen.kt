package com.sahayak.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahayak.android.i18n.Strings
import com.sahayak.android.ui.theme.EmergencyRed

@Composable
fun HomeScreen(
    strings: Strings,
    onNavigateToSymptoms: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToPrescription: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        // Header
        Text(
            text = strings.welcome,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = strings.healthcareForAll,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // ── Emergency banner (full-width, 2-column span) ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToEmergency),
            colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.12f)),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = strings.emergency,
                        tint = EmergencyRed,
                        modifier = Modifier.size(28.dp),
                    )
                }
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

        Spacer(Modifier.height(16.dp))

        // ── 2-column grid of square cards ──
        val gridSpacing = 12.dp

        // Row 1: Symptoms | Doctors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            SquareCard(
                icon = Icons.Default.MedicalServices,
                iconTint = Color(0xFF2196F3),
                iconBg = Color(0xFF2196F3).copy(alpha = 0.12f),
                title = strings.symptomChecker,
                subtitle = "AI-Powered Analysis",
                onClick = onNavigateToSymptoms,
                modifier = Modifier.weight(1f),
            )
            SquareCard(
                icon = Icons.Default.LocalHospital,
                iconTint = Color(0xFF4CAF50),
                iconBg = Color(0xFF4CAF50).copy(alpha = 0.12f),
                title = strings.findDoctors,
                subtitle = "Govt and private doctors",
                onClick = onNavigateToDoctors,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(gridSpacing))

        // Row 2: Prescription | Profile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            SquareCard(
                icon = Icons.Default.Medication,
                iconTint = Color(0xFFFF9800),
                iconBg = Color(0xFFFF9800).copy(alpha = 0.12f),
                title = strings.prescriptionScan,
                subtitle = "Find cheap generic medicines",
                onClick = onNavigateToPrescription,
                modifier = Modifier.weight(1f),
            )
            SquareCard(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF9C27B0),
                iconBg = Color(0xFF9C27B0).copy(alpha = 0.12f),
                title = strings.profile,
                subtitle = "Your details",
                onClick = onNavigateToProfile,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SquareCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
            )
        }
    }
}
