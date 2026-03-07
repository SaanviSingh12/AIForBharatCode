package com.sahayak.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionSearchScreen(
    viewModel: SahayakViewModel,
    onPharmacyResults: () -> Unit,
    onPrescriptionResults: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    var medicineText by rememberSaveable { mutableStateOf("") }
    var hasNavigatedToResults by rememberSaveable { mutableStateOf(false) }

    // Navigate to results screen when prescription result arrives
    LaunchedEffect(uiState.prescriptionResult) {
        if (uiState.prescriptionResult != null && !hasNavigatedToResults) {
            hasNavigatedToResults = true
            onPrescriptionResults()
        }
        if (uiState.prescriptionResult == null) {
            hasNavigatedToResults = false
        }
    }
    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            viewModel.analyzePrescriptionImage(file)
        }
    }

    // Camera capture
    var cameraFile by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraFile?.let { path ->
                viewModel.analyzePrescriptionImage(File(path))
            }
        }
    }

    // Camera permission
    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            val file = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraFile = file.absolutePath
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val file = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraFile = file.absolutePath
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.prescriptionScan) },
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
        ) {
            // ── 2×2 Action Grid ────────────────
            val gridSpacing = 12.dp

            // Row 1: Take Photo (full-width, 2-col span)
            Card(
                onClick = { launchCamera() },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "📷", fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Take photo of prescription from camera",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(gridSpacing))

            // Row 2: Upload from Gallery | Browse Pharmacies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            ) {
                ActionSquareCard(
                    icon = Icons.Default.Image,
                    iconTint = Color(0xFF2196F3),
                    iconBg = Color(0xFF2196F3).copy(alpha = 0.12f),
                    title = "Upload from Gallery",
                    subtitle = null,
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                )
                ActionSquareCard(
                    icon = Icons.Default.Medication,
                    iconTint = Color(0xFF4CAF50),
                    iconBg = Color(0xFF4CAF50).copy(alpha = 0.12f),
                    title = "Browse Pharmacies",
                    subtitle = null,
                    onClick = onPharmacyResults,
                    modifier = Modifier.weight(1f),
                )
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

            // ── Text input section ────────────
            Text(
                text = strings.orTypeMedicine,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = medicineText,
                onValueChange = { medicineText = it },
                label = { Text(strings.typeMedicine) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.analyzePrescriptionText(medicineText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = medicineText.isNotBlank() && !uiState.prescriptionLoading,
            ) {
                if (uiState.prescriptionLoading) {
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

            Spacer(Modifier.height(24.dp))

            // ── Did you know? card ────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9),
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "💡", fontSize = 32.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Did you know?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Generic medicines at Jan Aushadhi Kendras save you up to 80% compared to branded medicines with the same quality!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C),
                        )
                    }
                }
            }

            // Error
            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ActionSquareCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f),
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
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

