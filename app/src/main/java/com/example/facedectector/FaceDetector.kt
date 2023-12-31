package com.example.facedectector

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedectector.databinding.ActivityFaceDetectorBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class FaceDetector : AppCompatActivity() {
    lateinit var binding: ActivityFaceDetectorBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var facingDefault = true
    private var isVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectorBinding.inflate(layoutInflater)
        makeFullScreen()
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        requestAllPermissions()

        binding.buttonSwitchCam.setOnClickListener {
            if (isBackCam) {
                cameraFacing = CameraSelector.DEFAULT_FRONT_CAMERA
                isBackCam = false
                startCamera()
            } else {
                cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA
                isBackCam = true
                startCamera()
            }
        }

        binding.buttonSwitchVisible.setOnClickListener {
            if (isVisible) {
                isVisible = false
                binding.buttonSwitchVisible.setImageResource(R.drawable.ic_visible)
                binding.camPreview.visibility = View.INVISIBLE
            } else {
                isVisible = true
                binding.buttonSwitchVisible.setImageResource(R.drawable.ic_invisible)
                binding.camPreview.visibility = View.VISIBLE
            }
        }
    }

    private fun requestAllPermissions() {
        if (checkPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                Constants.REQUIRED_PERMISSION,
                Constants.CAMERA_REQUEST_CODE_PERMISSION)
        }
    }

    private fun checkPermission() =
        Constants.REQUIRED_PERMISSION.all{
            ContextCompat.checkSelfPermission(
                baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.camPreview.surfaceProvider)
                }

            val faceAnalyzer = FaceAnalyzer()
            faceAnalyzer.context = this@FaceDetector
            faceAnalyzer.binding = binding

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceAnalyzer)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraFacing,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CAMERA_REQUEST_CODE_PERMISSION) {
            if (checkPermission()) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
                startCamera()
            } else {
                Toast.makeText(this, "App does not have permission to access camera!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        var isBackCam = true
    }

    private fun makeFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()
    }
}