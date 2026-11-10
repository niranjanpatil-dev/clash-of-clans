package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "town_halls")
data class TownHallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: Int,
    val name: String,
    val village: String = "HOME",
    val isActive: Boolean = false
)
