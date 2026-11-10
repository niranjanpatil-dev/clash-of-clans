package com.example.data

import com.example.data.dao.BuildingDao
import com.example.data.dao.BuildingTypeDao
import com.example.data.dao.SyncRecordDao
import com.example.data.dao.TownHallDao
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import com.example.data.entity.SyncRecordEntity
import com.example.data.entity.TownHallEntity
import com.example.network.BuildingDiff
import com.example.network.GeminiGroundingService
import com.example.network.GroundingSource
import com.example.network.GroundingSyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class ClashRepository(
    private val database: AppDatabase,
    private val groundingService: GeminiGroundingService = GeminiGroundingService()
) {
    private val typeDao: BuildingTypeDao = database.buildingTypeDao()
    private val thDao: TownHallDao = database.townHallDao()
    private val buildingDao: BuildingDao = database.buildingDao()
    private val syncDao: SyncRecordDao = database.syncRecordDao()

    // Building Types
    fun getBuildingTypes(village: String): Flow<List<BuildingTypeEntity>> =
        typeDao.getByVillageFlow(village)

    fun getAllBuildingTypes(): Flow<List<BuildingTypeEntity>> =
        typeDao.getAllFlow()

    suspend fun insertBuildingType(type: BuildingTypeEntity): Long =
        typeDao.insert(type)

    suspend fun updateBuildingType(type: BuildingTypeEntity) =
        typeDao.update(type)

    suspend fun deleteBuildingType(type: BuildingTypeEntity) =
        typeDao.delete(type)

    // Town Halls
    fun getTownHalls(village: String): Flow<List<TownHallEntity>> =
        thDao.getByVillageFlow(village)

    fun getActiveTownHall(village: String): Flow<TownHallEntity?> =
        thDao.getActiveFlow(village)

    suspend fun setActiveTownHall(id: Long, village: String) =
        thDao.setActiveTownHall(id, village)

    suspend fun insertTownHall(townHall: TownHallEntity): Long =
        thDao.insert(townHall)

    suspend fun updateTownHall(townHall: TownHallEntity) =
        thDao.update(townHall)

    suspend fun deleteTownHall(townHall: TownHallEntity) =
        thDao.delete(townHall)

    // Buildings
    fun getBuildings(village: String): Flow<List<BuildingEntity>> =
        buildingDao.getByVillageFlow(village)

    suspend fun insertBuilding(building: BuildingEntity): Long =
        buildingDao.insert(building)

    suspend fun updateBuilding(building: BuildingEntity) =
        buildingDao.update(building)

    suspend fun deleteBuilding(building: BuildingEntity) =
        buildingDao.delete(building)

    // Sync History
    fun getLatestSync(village: String): Flow<SyncRecordEntity?> =
        syncDao.getLatestSyncFlow(village)

    // Google Search Grounding Sync trigger
    suspend fun querySearchGrounding(townHallLevel: Int, village: String): GroundingSyncResult {
        val currentBuildings = buildingDao.getByVillage(village)
        val currentTypes = typeDao.getByVillageFlow(village).firstOrNull() ?: emptyList()
        return groundingService.syncBuildingMaxLevels(
            townHallLevel = townHallLevel,
            village = village,
            buildings = currentBuildings,
            buildingTypes = currentTypes
        )
    }

    // Apply confirmed diffs to Room database
    suspend fun applyConfirmedSyncDiffs(
        diffs: List<BuildingDiff>,
        townHallLevel: Int,
        village: String,
        sources: List<GroundingSource>
    ): Int {
        val currentBuildings = buildingDao.getByVillage(village).associateBy { it.id }.toMutableMap()
        val toUpdate = mutableListOf<BuildingEntity>()

        for (diff in diffs) {
            val existing = currentBuildings[diff.buildingId]
            if (existing != null && existing.maxLevel != diff.newMaxLevel) {
                // IMPORTANT: Preserve existing currentLevel! Only update maxLevel.
                toUpdate.add(existing.copy(maxLevel = diff.newMaxLevel))
            }
        }

        if (toUpdate.isNotEmpty()) {
            buildingDao.updateAll(toUpdate)
        }

        // Save sync record
        val sourcesJsonArray = JSONArray().apply {
            sources.forEach { s ->
                put(JSONObject().apply {
                    put("title", s.title)
                    put("url", s.url)
                })
            }
        }

        val record = SyncRecordEntity(
            townHallLevel = townHallLevel,
            village = village,
            syncedAt = System.currentTimeMillis(),
            sourcesJson = sourcesJsonArray.toString(),
            updatedBuildingsCount = toUpdate.size
        )
        syncDao.insert(record)

        return toUpdate.size
    }
}
