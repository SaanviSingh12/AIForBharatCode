package com.sahayak.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sahayak.android.data.model.HospitalDto
import com.sahayak.android.data.model.TriageResponse
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.audio.AudioRecorderManager
import com.sahayak.android.ui.audio.WaveformVisualizer
import com.sahayak.android.ui.theme.EmergencyRed
import com.sahayak.android.ui.theme.GovernmentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomEntryScreen(
    viewModel: SahayakViewModel,
    onEmergencyDetected: () -> Unit,
    onHospitalClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    var symptomText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    // Audio recorder — retained across recompositions, released on dispose
    val audioManager = remember { AudioRecorderManager(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlayingTts by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var hasRecordedPreview by remember { mutableStateOf(false) }

    // Waveform amplitude data — collected live during recording
    val amplitudes = remember { mutableStateListOf<Float>() }

    DisposableEffect(Unit) {
        audioManager.onAmplitude { amp -> amplitudes.add(amp) }
        onDispose { audioManager.release() }
    }

    // Microphone permission
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        // Auto-start recording once permission is freshly granted
        if (granted && !isRecording) {
            audioManager.startRecording()
            isRecording = true
        }
    }

    // Check for emergency redirect — fires once when triageResult changes
    val triageResult = uiState.triageResult
    LaunchedEffect(triageResult) {
        if (triageResult?.isEmergency == true) {
            onEmergencyDetected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.symptomChecker) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
        floatingActionButton = {
            if (!hasRecordedPreview) {              // hide FAB while preview card is shown
                FloatingActionButton(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@FloatingActionButton
                        }
                        if (isRecording) {
                            // Stop recording → show preview (don't send yet)
                            audioManager.stopRecording()
                            isRecording = false
                            hasRecordedPreview = true
                        } else {
                            // Start recording
                            amplitudes.clear()
                            audioManager.startRecording()
                            isRecording = true
                        }
                    },
                    containerColor = if (isRecording) EmergencyRed else MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) strings.recording else strings.startRecording,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = strings.speakSymptoms,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            // ── Live recording waveform ──────────
            AnimatedVisibility(visible = isRecording) {
                Column {
                    RecordingIndicator(label = strings.recording)
                    Spacer(Modifier.height(8.dp))
                    WaveformVisualizer(
                        amplitudes = amplitudes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 4.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Recorded audio preview card ──────
            AnimatedVisibility(visible = hasRecordedPreview && !isRecording) {
                audioManager.lastRecordedFile?.let { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // Frozen waveform snapshot
                            WaveformVisualizer(
                                amplitudes = amplitudes,
                                barColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            )
                            Spacer(Modifier.height(12.dp))

                            // Playback + Send + Discard buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Play / Stop preview
                                OutlinedButton(
                                    onClick = {
                                        if (isPlayingPreview) {
                                            audioManager.stopPlayback()
                                            isPlayingPreview = false
                                        } else {
                                            isPlayingPreview = true
                                            audioManager.playFile(file) { isPlayingPreview = false }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingPreview) Icons.Default.Stop
                                            else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isPlayingPreview) strings.cancel else strings.startRecording)
                                }
                                // Send to backend
                                Button(
                                    onClick = {
                                        audioManager.stopPlayback()
                                        isPlayingPreview = false
                                        hasRecordedPreview = false
                                        viewModel.analyzeSymptomAudio(file)
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.submit)
                                }
                                // Discard
                                OutlinedButton(
                                    onClick = {
                                        audioManager.stopPlayback()
                                        isPlayingPreview = false
                                        audioManager.discardRecording()
                                        hasRecordedPreview = false
                                        amplitudes.clear()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = EmergencyRed,
                                    ),
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = strings.cancel, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            OutlinedTextField(
                value = symptomText,
                onValueChange = { symptomText = it },
                label = { Text(strings.typeSymptoms) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.analyzeSymptoms(symptomText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = symptomText.isNotBlank() && !uiState.triageLoading,
            ) {
                if (uiState.triageLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(strings.analyzing)
                } else {
                    Text(strings.submit)
                }
            }

            // Error
            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Results
            AnimatedVisibility(visible = triageResult != null && triageResult.isEmergency == false) {
                triageResult?.let { result ->
                    Spacer(Modifier.height(24.dp))
                    TriageResultCard(result = result, strings = strings, onHospitalClick = onHospitalClick)

                    // Play audio response button
                    result.audioBase64?.let { audio ->
                        Spacer(Modifier.height(12.dp))
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
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = if (isPlayingTts) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isPlayingTts) strings.cancel else "🔊 Listen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriageResultCard(
    result: TriageResponse,
    strings: com.sahayak.android.i18n.Strings,
    onHospitalClick: (String) -> Unit,
) {
    Column {
        // Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                result.specialist?.let {
                    Text(
                        text = "Recommended: $it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                result.urgencyLevel?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Urgency: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (it == "emergency") EmergencyRed
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                result.responseText?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Hospitals list
        if (result.hospitals.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Nearby Hospitals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            result.hospitals.forEach { hospital ->
                HospitalCard(hospital, onClick = { onHospitalClick(hospital.id) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HospitalCard(hospital: HospitalDto, onClick: () -> Unit) {
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
                        text = "Govt",
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
                    text = "${hospital.distance} km",
                    style = MaterialTheme.typography.labelSmall,
                )
                if (hospital.free) {
                    Text(
                        text = "FREE",
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

// ── Recording pulse indicator ─────────────────

@Composable
private fun RecordingIndicator(label: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording-pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = EmergencyRed,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = EmergencyRed,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}