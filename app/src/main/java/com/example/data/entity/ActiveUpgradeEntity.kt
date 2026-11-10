package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_upgrades")
data class ActiveUpgradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val buildingId: Long,
    val buildingName: String,
    val fromLevel: Int,
    val toLevel: Int,
    val startTimestamp: Long = System.currentTimeMillis(),
    val finishTimestamp: Long,
    val isCompleted: Boolean = false
) {
    val remainingMillis: Long
        get() = maxOf(0L, finishTimestamp - System.currentTimeMillis())

    val isFinished: Boolean
        get() = System.currentTimeMillis() >= finishTimestamp
}
