package com.example.recorder_in_bg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.recorder_in_bg.R

class AudioRecordingService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private val CHANNEL_ID = "AudioRecordingChannel"
    private val NOTIFICATION_ID = 1
    private var outputFilename: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                outputFilename = intent.getStringExtra("pathname")
                startForeground(NOTIFICATION_ID, createNotification())
                startRecording()
            }
            "STOP" -> {
                stopRecording()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Audio Recording Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Audio Recording")
        .setContentText("Recording in progress")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                setOutputFile(outputFilename ?: "${getExternalFilesDir(null)?.absolutePath}/audio_record.m4a")
                // setOutputFile("${getExternalFilesDir(null)?.absolutePath}/audio_record.m4a")
                prepare()
                start()
            }
        } else {
            // Handle the case where permission is not granted
            stopSelf()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    override fun onDestroy() {
        stopRecording()
        stopForeground(true)
        super.onDestroy()
    }
}