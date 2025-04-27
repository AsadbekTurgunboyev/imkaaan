package com.example.taxi.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taxi.R
import com.example.taxi.components.service.DriveBackGroundService
import com.example.taxi.ui.home.HomeActivity

object NotificationUtils {

    private const val FOREGROUND_CHANNEL_ID = "Speedometer"
    private const val FOREGROUND_CHANNEL_NAME = "Speedometer Service Channel"
    private const val REQ_CODE_OPEN_ACTIVITY = 1
    const val TAXI_RACE_NOTIFICATION_ID = 1211

    fun checkAndCreateChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.getSystemService(NotificationManager::class.java)?.let { notificationManager ->

                if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {

                    val serviceChannel = NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    )

                    notificationManager.createNotificationChannel(serviceChannel)
                }
            }
        }

    }


    fun getRacingNotification(
        context: Context,
    ): Notification {

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }


        return notificationBuilder
            .setContentTitle(context.getString(R.string.driver_running))
            .setContentText(context.getString(R.string.info_in_driver_running))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_bekjaanlogo)
            .setOngoing(true)
            .build()

    }


    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        contentText: String,
        pendingIntent: PendingIntent?,
        smallIcon: Int
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentText(contentText)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1, notification)
        }
    }

    fun createPendingIntent(
        context: Context,
        targetActivity: Class<*>,
        extras: Bundle
    ): PendingIntent {
        val intent = Intent(context, targetActivity).apply {
            putExtras(extras)
        }
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
    }
}