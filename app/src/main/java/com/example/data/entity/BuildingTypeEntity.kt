package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building_types")
data class BuildingTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val village: String = "HOME",
    val iconKey: String = "DEFENCE",
    val priority: Int = 1
)
