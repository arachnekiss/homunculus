package com.animeai.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.animeai.app.model.Emotion
import com.animeai.app.engine.CameraReactionEngine
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.launch

@Composable
fun CameraView(
    onEmotionDetected: (Emotion) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Request camera permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        // Set up the preview use case
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        // Set up the image analysis use case
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        
                        val cameraReactionEngine = CameraReactionEngine()
                        
                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            coroutineScope.launch {
                                val emotion = cameraReactionEngine.analyzeUserExpression(imageProxy)
                                onEmotionDetected(emotion)
                            }
                        }
                        
                        // Select front camera
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()
                        
                        try {
                            // Unbind all use cases before rebinding
                            cameraProvider.unbindAll()
                            
                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("CameraView", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close Camera",
                    tint = Color.White
                )
            }
            
            // Information text
            Text(
                text = "Looking for facial expressions...",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.White
            )
        } else {
            // Show message if no camera permission
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Request Permission")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            }
        }
    }
}
