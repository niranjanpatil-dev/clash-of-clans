package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpgradeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val upgradeId = intent.getLongExtra("upgradeId", 0L)
        val buildingName = intent.getStringExtra("buildingName") ?: "Building"
        val toLevel = intent.getIntExtra("toLevel", 1)

        NotificationHelper.showUpgradeCompletedNotification(
            context = context,
            notificationId = upgradeId.toInt().coerceAtLeast(100),
            buildingName = buildingName,
            toLevel = toLevel
        )
    }
}
