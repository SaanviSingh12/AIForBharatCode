package com.sahayak.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sahayak.android.data.model.MedicineDto
import com.sahayak.android.data.model.PrescriptionResponse
import com.sahayak.android.ui.SahayakViewModel
import com.sahayak.android.ui.theme.GovernmentGreen
import com.sahayak.android.ui.theme.SavingsGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionSearchScreen(
    viewModel: SahayakViewModel,
    onPharmacyResults: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val strings = uiState.strings
    var medicineText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy to cache file and analyze
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
            // Permission just granted — launch camera now
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
            // Upload section
            Text(
                text = strings.uploadPrescription,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.uploadPrescription)
                }
                OutlinedButton(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            val file = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            cameraFile = file.absolutePath
                            cameraLauncher.launch(uri)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.takePicture)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Text input section
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

            // Error
            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Results
            AnimatedVisibility(visible = uiState.prescriptionResult != null) {
                uiState.prescriptionResult?.let { result ->
                    Spacer(Modifier.height(24.dp))
                    PrescriptionResultCard(result, strings, onPharmacyResults)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionResultCard(
    result: PrescriptionResponse,
    strings: com.sahayak.android.i18n.Strings,
    onPharmacyResults: () -> Unit,
) {
    Column {
        // Savings summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SavingsGreen.copy(alpha = 0.1f),
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "${strings.savings}: ${result.totalSavingsPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SavingsGreen,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Brand: ₹${result.totalBrandCost}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Generic: ₹${result.totalGenericCost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SavingsGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Medicine list
        result.medicines.forEach { medicine ->
            MedicineCard(medicine, strings)
            Spacer(Modifier.height(8.dp))
        }

        // Find pharmacies button
        if (result.medicines.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPharmacyResults,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.nearbyPharmacies)
            }
        }

        // Response text
        result.responseText?.let { text ->
            Spacer(Modifier.height(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MedicineCard(
    medicine: MedicineDto,
    strings: com.sahayak.android.i18n.Strings,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = medicine.brandName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "₹${medicine.brandPrice}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${strings.genericAlternative}: ${medicine.genericName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SavingsGreen,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "₹${medicine.genericPrice} (−${medicine.savingsPercent}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SavingsGreen,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (medicine.dosage.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = medicine.dosage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
