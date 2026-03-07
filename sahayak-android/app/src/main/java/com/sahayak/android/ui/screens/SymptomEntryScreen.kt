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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.audio.AudioRecorderManager
import com.sahayak.android.ui.audio.WaveformVisualizer
import com.sahayak.android.ui.theme.EmergencyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomEntryScreen(
    viewModel: SahayakViewModel,
    onEmergencyDetected: () -> Unit,
    onTriageResults: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    var symptomText by rememberSaveable { mutableStateOf("") }
    var hasNavigatedToResults by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Audio recorder — retained across recompositions, released on dispose
    val audioManager = remember { AudioRecorderManager(context) }
    var isRecording by remember { mutableStateOf(false) }
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

    // Check for emergency redirect or navigate to results
    val triageResult = uiState.triageResult
    LaunchedEffect(triageResult) {
        if (triageResult?.isEmergency == true) {
            onEmergencyDetected()
        } else if (triageResult != null && !hasNavigatedToResults) {
            hasNavigatedToResults = true
            onTriageResults()
        }
        if (triageResult == null) {
            hasNavigatedToResults = false
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
                windowInsets = WindowInsets(0),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Large microphone button ──────────
            if (!hasRecordedPreview) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@Button
                        }
                        if (isRecording) {
                            audioManager.stopRecording()
                            isRecording = false
                            hasRecordedPreview = true
                        } else {
                            amplitudes.clear()
                            audioManager.startRecording()
                            isRecording = true
                        }
                    },
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) EmergencyRed
                            else MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) strings.recording else strings.startRecording,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (isRecording) strings.recording else strings.speakSymptoms,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
            }

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

            Spacer(Modifier.height(24.dp))

            // ── OR divider ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Type your symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

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

            // ── Emergency attention card ─────────
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = EmergencyRed.copy(alpha = 0.08f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ATTENTION!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "If you are experiencing severe or life-threatening symptoms, please call emergency services immediately at 108",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
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