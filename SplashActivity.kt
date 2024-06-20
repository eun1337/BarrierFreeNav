package com.example.capstone_kotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class SplashActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var faceDetection: FaceDetection
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val SPLASH_DELAY = 3000L // 3초 동안 Splash 화면 표시
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // OpenCV 카메라 뷰 초기화
        mOpenCvCameraView = findViewById(R.id.opencv_camera_view)
        mOpenCvCameraView.visibility = CameraBridgeViewBase.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)

        faceDetection = FaceDetection(mOpenCvCameraView)
        faceDetection.initializeOpenCVDependencies()

        // 카메라 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            startFaceDetection()
        }
    }

    private fun startFaceDetection() {
        Handler().postDelayed({
            mOpenCvCameraView.enableView()
            Handler().postDelayed({
                mOpenCvCameraView.disableView()
                startMainActivity()
            }, SPLASH_DELAY)
        }, 500L)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFaceDetection()
                } else {
                    startMainActivity()
                }
            }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        // Do nothing
    }

    override fun onCameraViewStopped() {
        // Do nothing
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return faceDetection.onCameraFrame(inputFrame)
    }

    override fun onResume() {
        super.onResume()
        mOpenCvCameraView.enableView()
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }
}