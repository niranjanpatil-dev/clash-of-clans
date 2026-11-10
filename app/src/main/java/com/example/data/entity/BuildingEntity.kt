package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class LevelCount(
    val level: Int,
    val count: Int
)

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
    val totalCount: Int = 1,
    val currentLevel: Int = 1,
    val maxLevel: Int = 1,
    val levelDistributionJson: String = "[]",
    val village: String = "HOME",
    val notes: String = ""
) {
    fun parseLevelDistributions(): List<LevelCount> {
        val list = mutableListOf<LevelCount>()
        try {
            val jsonArray = JSONArray(levelDistributionJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val lv = obj.optInt("level", currentLevel)
                val cnt = obj.optInt("count", 0)
                if (cnt > 0) {
                    list.add(LevelCount(level = lv, count = cnt))
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.add(LevelCount(level = currentLevel, count = maxOf(1, totalCount)))
        }
        return list.sortedBy { it.level }
    }

    companion object {
        fun serializeLevelDistributions(distributions: List<LevelCount>): String {
            val array = JSONArray()
            distributions.filter { it.count > 0 }.forEach { item ->
                val obj = JSONObject().apply {
                    put("level", item.level)
                    put("count", item.count)
                }
                array.put(obj)
            }
            return array.toString()
        }
    }
}
