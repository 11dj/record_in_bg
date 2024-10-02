package com.example.recorder_in_bg

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.recorder_in_bg.AudioRecordingService


class MainActivity: FlutterActivity(){
    private val CHANNEL = "com.example.recorder_in_bg/recording"
    private val RECORD_AUDIO_REQUEST_CODE = 123

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startRecording" -> {
                    val filename = call.argument<String>("pathname")
                    val intent = Intent(this, AudioRecordingService::class.java).apply {
                        putExtra("pathname", filename)
                        action = "START"
                    }
                    startService(intent)
                    result.success(null)
                }
                "stopRecording" -> {
                    val intent = Intent(this, AudioRecordingService::class.java).apply {
                        action = "STOP"
                    }
                    startService(intent)
                    result.success(null)
                }
                "dispose" -> {
                    stopRecordingService()
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can start recording
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

    private fun startRecordingService() {
        val intent = Intent(this, AudioRecordingService::class.java)
        startService(intent)
    }

    private fun stopRecordingService() {
        val intent = Intent(this, AudioRecordingService::class.java)
        stopService(intent)
    }
}