package com.mobosafe.dashcam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.util.ConnectCheckerRtmp
import com.pedro.rtplibrary.view.OpenGlView

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp {

    private lateinit var rtmpCamera2: RtmpCamera2
    private lateinit var openGlView: OpenGlView
    private lateinit var btnStart: Button
    private lateinit var btnSwitch: Button
    private lateinit var etRollNo: EditText
    private lateinit var tvStatus: TextView

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openGlView = findViewById(R.id.openGlView)
        btnStart = findViewById(R.id.btnStart)
        btnSwitch = findViewById(R.id.btnSwitch)
        etRollNo = findViewById(R.id.etRollNo)
        tvStatus = findViewById(R.id.tvStatus)

        rtmpCamera2 = RtmpCamera2(openGlView, this)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
        }

        btnStart.setOnClickListener {
            if (!rtmpCamera2.isStreaming) {
                startStreaming()
            } else {
                stopStreaming()
            }
        }

        btnSwitch.setOnClickListener {
            try {
                rtmpCamera2.switchCamera()
            } catch (e: Exception) {
                Toast.makeText(this, "Switch camera failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Camera and mic permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Camera and microphone permissions are required to stream",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startStreaming() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
            return
        }

        val rtmpUrl = "rtmp://15.207.177.194:1936/hackathon/btech25135_front"

        val audioReady = rtmpCamera2.prepareAudio()
        val videoReady = rtmpCamera2.prepareVideo(
            1280, 720,
            25,
            1500 * 1024,
            false,
            CameraHelper.Facing.BACK
        )

        if (audioReady && videoReady) {
            rtmpCamera2.startStream(rtmpUrl)
            btnStart.text = "Stop Streaming"
            tvStatus.text = "Connecting to: $rtmpUrl"
        } else {
            Toast.makeText(this, "Failed to prepare audio/video encoders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopStreaming() {
        if (rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
        btnStart.text = "Start Streaming"
        tvStatus.text = "Stopped"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::rtmpCamera2.isInitialized && rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            tvStatus.text = "Connected - streaming live"
            Log.d("RTMP", "Connection success")
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            tvStatus.text = "Connection failed: $reason"
            Toast.makeText(this, "Connection failed: $reason", Toast.LENGTH_LONG).show()
            stopStreaming()
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {}

    override fun onDisconnectRtmp() {
        runOnUiThread {
            tvStatus.text = "Disconnected"
        }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            tvStatus.text = "Auth error"
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            tvStatus.text = "Auth success"
        }
    }
}