package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_records")
data class SyncRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val townHallLevel: Int,
    val village: String,
    val syncedAt: Long = System.currentTimeMillis(),
    val sourcesJson: String = "[]",
    val updatedBuildingsCount: Int = 0
)
