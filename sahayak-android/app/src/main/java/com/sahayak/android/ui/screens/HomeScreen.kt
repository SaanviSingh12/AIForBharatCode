package com.sahayak.android.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahayak.android.i18n.Strings
import com.sahayak.android.ui.theme.EmergencyRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    strings: Strings,
    onNavigateToSymptoms: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToPrescription: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    // ── Staggered entrance animation (6 items) ──
    val itemCount = 6
    val alphas = remember { List(itemCount) { Animatable(0f) } }
    val offsets = remember { List(itemCount) { Animatable(40f) } }

    LaunchedEffect(Unit) {
        for (i in 0 until itemCount) {
            delay(i * 60L)
            launch {
                alphas[i].animateTo(1f, tween(400, easing = EaseOutCubic))
            }
            launch {
                offsets[i].animateTo(0f, tween(400, easing = EaseOutCubic))
            }
        }
    }

    fun Modifier.staggerAnim(index: Int): Modifier {
        val idx = index.coerceIn(0, itemCount - 1)
        return this
            .alpha(alphas[idx].value)
            .offset { IntOffset(0, offsets[idx].value.toInt()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        // Header
        Column(modifier = Modifier.staggerAnim(0)) {
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
        }

        Spacer(Modifier.height(20.dp))

        // ── Emergency banner (full-width, 2-column span) ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .staggerAnim(1)
                .clickable(onClick = onNavigateToEmergency),
            colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.12f)),
            shape = MaterialTheme.shapes.large,
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
//        Spacer(Modifier.height(12.dp))

        // ── Services section ──
        Text(
            text = strings.services,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.staggerAnim(2),
        )
        Spacer(Modifier.height(12.dp))

        // ── 2-column grid of square cards ──
        val gridSpacing = 12.dp

        // Row 1: Symptoms | Doctors
        Row(
            modifier = Modifier.fillMaxWidth().staggerAnim(3),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            SquareCard(
                icon = Icons.Default.MedicalServices,
                iconTint = Color(0xFF2196F3),
                iconBg = Color(0xFF2196F3).copy(alpha = 0.12f),
                title = strings.symptomChecker,
                subtitle = strings.aiPoweredAnalysis,
                onClick = onNavigateToSymptoms,
                modifier = Modifier.weight(1f),
            )
            SquareCard(
                icon = Icons.Default.LocalHospital,
                iconTint = Color(0xFF4CAF50),
                iconBg = Color(0xFF4CAF50).copy(alpha = 0.12f),
                title = strings.findDoctors,
                subtitle = strings.govtAndPrivateDoctors,
                onClick = onNavigateToDoctors,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(gridSpacing))

        // Row 2: Prescription | Profile
        Row(
            modifier = Modifier.fillMaxWidth().staggerAnim(4),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            SquareCard(
                icon = Icons.Default.Medication,
                iconTint = Color(0xFFFF9800),
                iconBg = Color(0xFFFF9800).copy(alpha = 0.12f),
                title = strings.prescriptionScan,
                subtitle = strings.findCheapGenericMedicines,
                onClick = onNavigateToPrescription,
                modifier = Modifier.weight(1f),
            )
            SquareCard(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF9C27B0),
                iconBg = Color(0xFF9C27B0).copy(alpha = 0.12f),
                title = strings.profile,
                subtitle = strings.yourDetails,
                onClick = onNavigateToProfile,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Government Schemes section ──
        Column(modifier = Modifier.staggerAnim(5)) {
        Text(
            text = strings.governmentSchemes,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD),
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = strings.pmJay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.pmJayDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9),
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = strings.janAushadhi,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.janAushadhiDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF388E3C),
                )
            }
        }
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardScale",
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    },
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
