package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BuildingTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingTypeDao {
    @Query("SELECT * FROM building_types WHERE village = :village ORDER BY id ASC")
    fun getByVillageFlow(village: String): Flow<List<BuildingTypeEntity>>

    @Query("SELECT * FROM building_types ORDER BY id ASC")
    fun getAllFlow(): Flow<List<BuildingTypeEntity>>

    @Query("SELECT * FROM building_types WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BuildingTypeEntity?

    @Query("SELECT COUNT(*) FROM building_types")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(buildingType: BuildingTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(buildingTypes: List<BuildingTypeEntity>)

    @Update
    suspend fun update(buildingType: BuildingTypeEntity)

    @Delete
    suspend fun delete(buildingType: BuildingTypeEntity)
}
