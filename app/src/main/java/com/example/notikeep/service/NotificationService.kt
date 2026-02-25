package com.example.notikeep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.notikeep.R

class NotificationService : Service() {
    private val CHANNEL_ID = "persistent_channel"

    override fun onCreate(){
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "Notification"
        val message = intent?.getStringExtra("message") ?: "Message"

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Permanent notification",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

