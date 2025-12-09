package com.example.safedrive.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.safedrive.analyzer.DrowsinessAnalyzer
import com.example.safedrive.utils.AlarmManager
import java.util.concurrent.Executors

@Composable
fun SafeDriveScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // UI States
    var hasPermission by remember { mutableStateOf(false) }
    var isDrowsy by remember { mutableStateOf(false) }
    var eyeOpenness by remember { mutableStateOf(1.0f) } // 0.0 to 1.0

    // Alarm Helper
    val alarmManager = remember { AlarmManager(context) }

    // Animated Colors for Smooth UI
    val borderColor by animateColorAsState(if (isDrowsy) Color.Red else Color.Green)
    val statusColor by animateColorAsState(if (isDrowsy) Color.Red else Color(0xFF00C853))

    // Permission Logic
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Alarm Trigger Logic
    LaunchedEffect(isDrowsy) {
        if (isDrowsy) alarmManager.playAlarm() else alarmManager.stopAlarm()
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Allow Camera to start SafeDrive") }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Preview Layer
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    // Connect Analyzer
                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                        DrowsinessAnalyzer { drowsy, openness ->
                            isDrowsy = drowsy
                            eyeOpenness = openness
                        }
                    )

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // 2. Heads-Up Display (HUD) Overlay
        // DANGER BORDER
        if (isDrowsy) {
            Box(Modifier.fillMaxSize().border(15.dp, Color.Red))

            // Big Warning Icon
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(120.dp))
                Text("WAKE UP!", color = Color.Red, fontSize = 50.sp, fontWeight = FontWeight.Bold)
            }
        }

        // STATUS DASHBOARD (Bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDrowsy) "STATUS: DANGER" else "STATUS: ACTIVE",
                    color = statusColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Eye Openness Meter
            Text("Eye Alertness Level: ${(eyeOpenness * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { eyeOpenness },
                modifier = Modifier.fillMaxWidth().height(12.dp).padding(top = 4.dp),
                color = statusColor,
                trackColor = Color.DarkGray,
            )
        }
    }
}