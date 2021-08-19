package ru.unit.usd_listener.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.unit.usd_listener.R
import ru.unit.usd_listener.service.UsdNotificationWorker

class MainActivity : AppCompatActivity() {
    companion object {
        const val CHANNEL_ID = "USDNotification"
        const val WORKER_TAG = "ru.unit.usd_listener.USDNotificationWorker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        UsdNotificationWorker.createOrDisable(applicationContext)
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}