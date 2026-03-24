package com.borg.budget.ui.screens

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.borg.budget.ui.models.ExpenseCategory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    onReceiptScanned: (String, Double) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var isProcessing by remember { mutableStateOf(false) }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                        
                        // Action de capture
                        previewView.setOnClickListener {
                            if (isProcessing) return@setOnClickListener
                            isProcessing = true
                            
                            imageCapture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        processImage(image, recognizer) { title, amount ->
                                            onReceiptScanned(title, amount)
                                            isProcessing = false
                                        }
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("Scanner", "Capture failed", exception)
                                        isProcessing = false
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Scanner", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onClose, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
                IconButton(onClick = { /* Flash */ }, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                    Icon(Icons.Default.FlashOn, null, tint = Color.White)
                }
            }

            // Scanner Frame
            Box(
                modifier = Modifier
                    .size(280.dp, 400.dp)
                    .background(Color.Transparent)
                    .padding(2.dp)
            ) {
                // Border/Corners could be added here
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Analyse du ticket...", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text("Tapez sur l'écran pour scanner", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onResult: (String, Double) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val lines = visionText.text.split("\n")
                var totalAmount = 0.0
                var merchant = "Magasin inconnu"

                // Basic Logic to find amount (Look for "TOTAL" or numbers with decimal)
                val amountRegex = """\d+[\.,]\d{2}""".toRegex()
                
                for (line in lines) {
                    val upperLine = line.uppercase()
                    if (upperLine.contains("TOTAL") || upperLine.contains("EUR") || upperLine.contains("€")) {
                        val match = amountRegex.find(line)
                        if (match != null) {
                            totalAmount = match.value.replace(",", ".").toDouble()
                        }
                    }
                }
                
                if (lines.isNotEmpty()) merchant = lines[0] // Often the first line is the name

                onResult(merchant, totalAmount)
                imageProxy.close()
            }
            .addOnFailureListener {
                imageProxy.close()
            }
    }
}
