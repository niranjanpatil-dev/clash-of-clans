package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "buildings",
    foreignKeys = [
        ForeignKey(
            entity = BuildingTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["typeId"]), Index(value = ["village"])]
)
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val typeId: Long,
    val name: String,
    val currentLevel: Int = 1,
    val maxLevel: Int = 1,
    val village: String = "HOME",
    val notes: String = ""
)
