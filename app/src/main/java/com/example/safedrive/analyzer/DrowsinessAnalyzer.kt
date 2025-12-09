package com.example.safedrive.analyzer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

// Callback now returns TWO things:
// 1. isDrowsy: Boolean (Should we sound the alarm?)
// 2. eyeOpenProb: Float (For the UI Progress Bar)
class DrowsinessAnalyzer(
    private val onStatusChanged: (Boolean, Float) -> Unit
) : ImageAnalysis.Analyzer {

    // 1. Configure ML Kit to detect "Landmarks" (Eyes) and "Classification" (Open/Closed)
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // <--- CRITICAL
        .build()

    private val detector = FaceDetection.getClient(options)

    private var firstClosedTime: Long = 0
    private val TIME_THRESHOLD = 1500L // 1.5 Seconds to trigger alarm
    private val EYE_OPEN_THRESHOLD = 0.5f // Below 0.5 = Eyes are likely closed

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]

                        // Get probability (default to 1.0 if null)
                        val leftEye = face.leftEyeOpenProbability ?: 1.0f
                        val rightEye = face.rightEyeOpenProbability ?: 1.0f

                        // Average open probability for UI display
                        val averageOpen = (leftEye + rightEye) / 2.0f

                        // Check if BOTH eyes are closed
                        if (leftEye < EYE_OPEN_THRESHOLD && rightEye < EYE_OPEN_THRESHOLD) {
                            if (firstClosedTime == 0L) {
                                firstClosedTime = System.currentTimeMillis()
                            } else {
                                val duration = System.currentTimeMillis() - firstClosedTime
                                if (duration > TIME_THRESHOLD) {
                                    // ALARM! Eyes closed for > 1.5s
                                    onStatusChanged(true, averageOpen)
                                } else {
                                    // Eyes closed, but waiting for threshold (Blinking?)
                                    onStatusChanged(false, averageOpen)
                                }
                            }
                        } else {
                            // Eyes are open
                            firstClosedTime = 0L
                            onStatusChanged(false, averageOpen)
                        }
                    } else {
                        // No face detected
                        onStatusChanged(false, 0f)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}