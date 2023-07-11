package com.example.facedectector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facedectector.databinding.ActivityFaceDetectorBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer : ImageAnalysis.Analyzer {
    var context: Context? = null
    var binding: ActivityFaceDetectorBinding? = null
    private val TAG = "com.example.facedectector.FaceAnalyzer"
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    binding?.faceBoxOverlay?.setFaces(faces)
                }
                .addOnFailureListener { e ->
                    // Xử lý khi có lỗi xảy ra trong quá trình nhận diện khuôn mặt
                    Log.e(TAG, "Lỗi trong quá trình nhận diện khuôn mặt: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}