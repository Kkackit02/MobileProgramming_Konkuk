package com.example.week13

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

fun makeNotification(context: Context, msg: String, pendingIntent:PendingIntent) {
    val channelId = "MyChannel"
    val channelName = "MyChannel"
    val notificationId = 0

    val notificationChannel =
        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

    val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.baseline_access_alarm_24)
        .setContentTitle("메세지 알림")
        .setContentText(msg)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(notificationId, notification)
}