package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.ActiveUpgradeEntity

object NotificationHelper {
    const val CHANNEL_ID = "clash_upgrades_channel"
    const val CHANNEL_NAME = "Building Upgrades"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a Clash of Clans building upgrade completes"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleUpgradeAlarm(context: Context, upgrade: ActiveUpgradeEntity) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, UpgradeAlarmReceiver::class.java).apply {
            putExtra("upgradeId", upgrade.id)
            putExtra("buildingName", upgrade.buildingName)
            putExtra("toLevel", upgrade.toLevel)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            upgrade.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    upgrade.finishTimestamp,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    upgrade.finishTimestamp,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback for permissions if exact alarm is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                upgrade.finishTimestamp,
                pendingIntent
            )
        }
    }

    fun cancelUpgradeAlarm(context: Context, upgradeId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UpgradeAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            upgradeId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showUpgradeCompletedNotification(
        context: Context,
        notificationId: Int,
        buildingName: String,
        toLevel: Int
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔨 Builder Finished: $buildingName")
            .setContentText("Your $buildingName upgrade to Level $toLevel might be finished! Please check your village.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission denied on Android 13+
        }
    }
}
