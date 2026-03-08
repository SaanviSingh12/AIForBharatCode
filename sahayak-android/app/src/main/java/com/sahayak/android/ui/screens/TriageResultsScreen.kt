package com.sahayak.android.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahayak.android.data.model.HospitalDto
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.audio.AudioRecorderManager
import com.sahayak.android.ui.theme.EmergencyRed
import com.sahayak.android.ui.theme.GovernmentGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageResultsScreen(
    viewModel: SahayakViewModel,
    onHospitalClick: (String) -> Unit,
    onEmergencyDetected: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    val result = uiState.triageResult
    val context = LocalContext.current

    // Audio player for TTS response
    val audioManager = remember { AudioRecorderManager(context) }
    var isPlayingTts by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { audioManager.release() }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.symptomChecker) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                windowInsets = WindowInsets(0),
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
                    text = strings.noResultsAvailable,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // Staggered entrance — 4 sections: banner, analysis, listen btn, hospitals
            val sectionCount = 4
            val sectionAlphas = remember { List(sectionCount) { Animatable(0f) } }
            val sectionOffsets = remember { List(sectionCount) { Animatable(30f) } }

            LaunchedEffect(Unit) {
                for (i in 0 until sectionCount) {
                    delay(i * 80L)
                    launch {
                        sectionAlphas[i].animateTo(1f, tween(400, easing = EaseOutCubic))
                    }
                    launch {
                        sectionOffsets[i].animateTo(0f, tween(400, easing = EaseOutCubic))
                    }
                }
            }

            fun Modifier.sectionAnim(idx: Int): Modifier {
                val i = idx.coerceIn(0, sectionCount - 1)
                return this
                    .alpha(sectionAlphas[i].value)
                    .offset { IntOffset(0, sectionOffsets[i].value.toInt()) }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // ── Emergency detected card ──────────
                if (result.isEmergency == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth().sectionAnim(0),
                        colors = CardDefaults.cardColors(
                            containerColor = EmergencyRed,
                        ),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = strings.emergencyDetected,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(16.dp))

                            // Symptom summary in white card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White,
                                ),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = result.summary
                                        ?: result.responseText ?: "",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Wide "Go to Emergency Mode" button
                            Button(
                                onClick = { onEmergencyDetected() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = EmergencyRed,
                                ),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.goToEmergencyMode,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Specialist recommendation banner ─────
                Card(
                    modifier = Modifier.fillMaxWidth().sectionAnim(0),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "🩺", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))

                        result.specialist?.let {
                            Text(
                                text = "${strings.recommended}: $it",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        result.urgencyLevel?.let { urgency ->
                            val urgencyColor = when (urgency.lowercase()) {
                                "emergency" -> EmergencyRed
                                "high" -> Color(0xFFFF9800)
                                else -> GovernmentGreen
                            }
                            Text(
                                text = "${strings.urgency}: $urgency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = urgencyColor,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Explanation / response text ──────────
                result.responseText?.let { text ->
                    Card(
                        modifier = Modifier.fillMaxWidth().sectionAnim(1),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = strings.analysis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Listen to response button ───────────
                result.audioBase64?.let { audio ->
                    Button(
                        onClick = {
                            if (isPlayingTts) {
                                audioManager.stopPlayback()
                                isPlayingTts = false
                            } else {
                                isPlayingTts = true
                                audioManager.playBase64Audio(audio) { isPlayingTts = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().sectionAnim(2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingTts) EmergencyRed
                            else MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isPlayingTts) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPlayingTts) strings.cancel else "🔊 ${strings.listenToResponse}")
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Nearby hospitals ─────────────────────
                if (result.hospitals.isNotEmpty()) {
                    Column(modifier = Modifier.sectionAnim(3)) {
                    Text(
                        text = strings.nearbyHospitals,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(10.dp))
                    result.hospitals.forEachIndexed { index, hospital ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(350, delayMillis = index * 60, easing = EaseOutCubic)) +
                                slideInVertically(tween(350, delayMillis = index * 60, easing = EaseOutCubic)) { it / 3 },
                        ) {
                            TriageHospitalCard(hospital, strings = strings, onClick = { onHospitalClick(hospital.id) })
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriageHospitalCard(hospital: HospitalDto, strings: com.sahayak.android.i18n.Strings, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = hospital.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (hospital.type == "government") {
                    Text(
                        text = strings.govt,
                        style = MaterialTheme.typography.labelSmall,
                        color = GovernmentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = hospital.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${hospital.distance} ${strings.distance}",
                    style = MaterialTheme.typography.labelSmall,
                )
                if (hospital.free) {
                    Text(
                        text = strings.freeUppercase,
                        style = MaterialTheme.typography.labelSmall,
                        color = GovernmentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    hospital.fee?.let {
                        Text("₹$it", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
