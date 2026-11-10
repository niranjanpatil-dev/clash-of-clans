package com.example.network

data class GroundingSource(
    val title: String,
    val url: String
)

data class BuildingDiff(
    val buildingId: Long,
    val buildingName: String,
    val categoryName: String,
    val currentLevel: Int,
    val oldMaxLevel: Int,
    val newMaxLevel: Int
) {
    val hasChanged: Boolean = oldMaxLevel != newMaxLevel
    val levelDelta: Int = newMaxLevel - oldMaxLevel
}

sealed class GroundingSyncResult {
    data class Success(
        val townHallLevel: Int,
        val village: String,
        val diffs: List<BuildingDiff>,
        val sources: List<GroundingSource>,
        val searchQueries: List<String>,
        val rawSummary: String
    ) : GroundingSyncResult()

    data class Error(val message: String) : GroundingSyncResult()
}
